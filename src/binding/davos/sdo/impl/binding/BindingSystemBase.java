/*   Copyright 2008 BEA Systems Inc.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package davos.sdo.impl.binding;

import davos.sdo.DataObjectXML;
import davos.sdo.PropertyXML;
import davos.sdo.SDOContext;
import davos.sdo.TypeXML;
import davos.sdo.binding.BindingSystem;
import davos.sdo.impl.binaryVersioning.VersionedDataInputStream;
import davos.sdo.impl.binaryVersioning.VersionedDataOutputStream;
import davos.sdo.impl.binaryVersioning.Versions;
import davos.sdo.impl.data.DataObjectImpl;
import davos.sdo.impl.type.BuiltInTypeSystem;
import davos.sdo.impl.type.TypeSystemBase;
import davos.sdo.impl.util.PrimitiveCodes;
import davos.sdo.impl.util.LRUCacheMap;
import davos.sdo.type.TypeSystem;
import davos.sdo.util.Filer;
import javax.sdo.DataGraph;
import javax.sdo.DataObject;
import org.apache.xmlbeans.ResourceLoader;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author Cezar Andrei (cezar dot andrei at gmail dot com)
 *         Date: Jul 26, 2006
 */
public abstract class BindingSystemBase
    implements BindingSystem
{
    // member vars
    protected SDOContext _sdoContext;
    private ResourceLoader _resourceLoader;
    private TypeSystem _ts;
    private LRUCacheMap<QName, QName> _sdoTypeNamesNotFound;
    private LRUCacheMap<QName, QName> _elemQNamesNotFound;
    private LRUCacheMap<QName, QName> _attrQNamesNotFound;
    private LRUCacheMap<QName, QName> _globalPropSdoQNamesNotFound;
    private LRUCacheMap<QName, QName> _schemaTypeQNamesNotFound;

    protected BindingSystemBase(SDOContext sdoContext, ResourceLoader resourceLoader, TypeSystem typeSystem)
    {
        _sdoContext = sdoContext;
        _resourceLoader = resourceLoader;
        _ts = typeSystem;
        _sdoTypeNamesNotFound = new LRUCacheMap<QName, QName>(200);
        _elemQNamesNotFound = new LRUCacheMap<QName, QName>(200);
        _attrQNamesNotFound = new LRUCacheMap<QName, QName>(200);
        _globalPropSdoQNamesNotFound = new LRUCacheMap<QName, QName>(200);
        _schemaTypeQNamesNotFound = new LRUCacheMap<QName, QName>(200);
    }

    public TypeSystem getTypeSystem()
    {
        return _ts;
    }

    public DataObjectXML createDataObjectForType(TypeXML type, DataGraph datagraph)
    {
        return getDataObjectForType(type, datagraph, null, null);
    }

    public DataObjectXML createChildForDataObject(DataObjectXML parent, PropertyXML prop, TypeXML childType)
    {
        return createChildForDataObject(parent, prop, childType, null, prop);
    }

    public DataObjectXML createChildForDataObject(DataObjectXML parent, PropertyXML prop, TypeXML childType,
        String prefix, PropertyXML substitution)
    {
        DataObjectXML child = getDataObjectForType(childType, parent.getDataGraph(), parent, prop);

        ((DataObjectImpl)parent).getStore().storeAddNew(prop, child, prefix, substitution);

        return child;
    }

    public TypeXML getType(Class interfaceClass)
    {
        //if (!interfaceClass.isInterface())
        //    throw new IllegalArgumentException("Class '" + interfaceClass + "' is not an interface.");

        try
        {
            interfaceClass = _sdoContext.getClassLoader().loadClass(getImplClassName(interfaceClass));
        }
        catch (ClassNotFoundException e)
        {
            //throw new IllegalArgumentException("Implementation class not found for: " + instanceClass.getCanonicalName());
        }


        try
        {
            Field uriField = interfaceClass.getField("typeUri");
            Field nameField = interfaceClass.getField("typeName");
            if (uriField!=null && String.class.isAssignableFrom(uriField.getType()) &&
                nameField!=null && String.class.isAssignableFrom(nameField.getType()) )
            {
                return loadTypeByTypeName((String)uriField.get(null), (String)nameField.get(null));
            }
        }
        catch (NoSuchFieldException e)
        {
            //throw new IllegalStateException("Missing fields 'typeUri' and 'typeName' in class: " + instanceClass, e);
        }
        catch (IllegalAccessException e)
        {
            //throw new IllegalStateException("Illegal access in class: " + instanceClass, e);
        }

        // return type still null try one of the well known types
        //if ( DataObject.class.isAssignableFrom(interfaceClass) )
        //    return BuiltInTypeSystem.BEADATAOBJECT;
        
        return BuiltInTypeSystem.INSTANCECLASSTOTYPE.get(interfaceClass);
    }

    private String getImplClassName(Class instClass)
    {
        if (instClass.isLocalClass())
            throw new IllegalArgumentException("SDO Type not supported for local classes. " + instClass.getCanonicalName());

        if (!instClass.isMemberClass())
        {
            Package pkg = instClass.getPackage();
            return (pkg==null ? "" : pkg.getName()) + ".impl." + instClass.getSimpleName() + "Impl";
        }

        return getImplClassName(instClass.getEnclosingClass()) + "$" + instClass.getSimpleName() + "Impl";
    }

    public TypeXML loadTypeByTypeName(String uri, String typeName)
    {
        TypeXML type = getTypeSystem().getTypeXML(uri, typeName);
        if (type==null)
        {
            QName qname = new QName(uri, typeName);
            if (_sdoTypeNamesNotFound.containsValue(qname))
                return null; //don't try to load it, it will not be found

            synchronized(this)
            {
                type = getTypeSystem().getTypeXML(uri, typeName);
                if (type!=null)
                    return type;  // loaded by another thread

                //not loaded yet, load it up
                //System.out.println("  loadTypeByTypeName:            " + typeName + " @ "+ uri);
                if (!loadFromTypeQName(uri, typeName))
                {
                    // not found put it in the notFoundCache
                    _sdoTypeNamesNotFound.put(qname, qname);
                    return null;
                }
            }
            type = getTypeSystem().getTypeXML(uri, typeName);
        }
        return type;
    }

    public PropertyXML loadGlobalPropertyByTopLevelElemQName(String uri, String elemName)
    {
        PropertyXML globalProperty = getTypeSystem().getGlobalPropertyByTopLevelElemQName(uri, elemName);
        if (globalProperty==null)
        {
            QName qname = new QName(uri, elemName);
            if (_elemQNamesNotFound.containsValue(qname))
                return null; //don't try to load it, it will not be found

            synchronized(this)
            {
                globalProperty = getTypeSystem().getGlobalPropertyByTopLevelElemQName(uri, elemName);
                if (globalProperty!=null)
                    return globalProperty; // loaded by another thread

                //not loaded yet, load it up
                //System.out.println("  loadGlobalPropertyByTopLevelElemQName: " + elemName + " @ "+ uri);
                if (!loadFromTopLevelElemQName(uri, elemName))
                {
                    // not found put it in the notFoundCache
                    _elemQNamesNotFound.put(qname, qname);
                    return null;
                }
            }
            globalProperty = getTypeSystem().getGlobalPropertyByTopLevelElemQName(uri, elemName);
        }
        return globalProperty;
    }

    public PropertyXML loadGlobalPropertyByTopLevelAttrQName(String uri, String attrName)
    {
        PropertyXML globalProperty = getTypeSystem().getGlobalPropertyByTopLevelAttrQName(uri, attrName);

        if (globalProperty==null)
        {
            QName qname = new QName(uri, attrName);
            if (_attrQNamesNotFound.containsValue(qname))
                return null; //don't try to load it, it will not be found

            synchronized(this)
            {
                globalProperty = getTypeSystem().getGlobalPropertyByTopLevelAttrQName(uri, attrName);
                if (globalProperty!=null)
                    return globalProperty; // loaded by another thread

                //not loaded yet, load it up
                //System.out.println("  loadGlobalPropertyByTopLevelAttrQName: " + attrName + " @ "+ uri);
                if (!loadFromTopLevelAttrQName(uri, attrName))
                {
                    // not found put it in the notFoundCache
                    _attrQNamesNotFound.put(qname, qname);
                    return null;
                }
            }
            globalProperty = getTypeSystem().getGlobalPropertyByTopLevelAttrQName(uri, attrName);
        }
        return globalProperty;
    }

    public PropertyXML loadGlobalPropertyBySdoQName(String uri, String sdoName)
    {
        PropertyXML globalProperty = getTypeSystem().getGlobalPropertyBySdoQName(uri, sdoName);
        if (globalProperty==null)
        {
            QName qname = new QName(uri, sdoName);
            if (_globalPropSdoQNamesNotFound.containsValue(qname))
                return null; //don't try to load it, it will not be found

            synchronized(this)
            {
                globalProperty = getTypeSystem().getGlobalPropertyBySdoQName(uri, sdoName);
                if (globalProperty!=null)
                    return globalProperty; // loaded by another thread

                //not loaded yet, load it up
                //System.out.println("  loadFromGlobalPropertySdoQName: " + sdoName + " @ "+ uri);
                if (!loadFromGlobalPropertySdoQName(uri, sdoName))
                {
                    // not found put it in the notFoundCache
                    _globalPropSdoQNamesNotFound.put(qname, qname);
                    return null;
                }
            }
            globalProperty = getTypeSystem().getGlobalPropertyBySdoQName(uri, sdoName);
        }
        return globalProperty;
    }

    public TypeXML loadTypeBySchemaTypeName(String uri, String localName)
    {
        TypeXML type = getTypeSystem().getTypeBySchemaTypeName(uri, localName);

        if (type==null)
        {
            QName qname = new QName(uri, localName);
            if (_schemaTypeQNamesNotFound.containsValue(qname))
                return null; //don't try to load it, it will not be found

            synchronized(this)
            {
                type = getTypeSystem().getTypeBySchemaTypeName(uri, localName);
                if (type!=null)
                    return type;

                //not loaded yet, load it up
                //System.out.println("  loadTypeBySchemaTypeName:      " + localName + " @ "+ uri);
                if (!loadFromSchemaTypeName(uri, localName))
                {
                    // not found put it in the notFoundCache
                    _schemaTypeQNamesNotFound.put(qname, qname);
                    return null;
                }
            }
            type = getTypeSystem().getTypeBySchemaTypeName(uri, localName);
        }
        return type;
    }

    protected boolean loadFromTypeQName(String uri, String localName)
    {
        TypeSystemBase ts = (TypeSystemBase)getTypeSystem();

        try
        {
            String id = ts.readIdForTypeQName(_resourceLoader, new QName(uri, localName));
            if (id==null)
                return false;  //todo can this case be optimized? Map of unsuccesful uris

            if (!ts.loadTypeSystemFromId(_resourceLoader, id))
                return false;

            if (!loadBindingSystemWithId(id))
                return false;
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }

        return true;
    }

    protected boolean loadFromTopLevelElemQName(String uri, String localName)
    {
        TypeSystemBase ts = (TypeSystemBase)getTypeSystem();

        try
        {
            String id = ts.readIdForTopLevelElemQName(_resourceLoader, new QName(uri, localName));
            if (id==null)
                return false;  //todo can this case be optimized? Map of unsuccesful uris

            if (!ts.loadTypeSystemFromId(_resourceLoader, id))
                return false;

            if (!loadBindingSystemWithId(id))
                return false;
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }

        return true;
    }

    protected boolean loadFromTopLevelAttrQName(String uri, String localName)
    {
        TypeSystemBase ts = (TypeSystemBase)getTypeSystem();

        try
        {
            String id = ts.readIdForTopLevelAttrQName(_resourceLoader, new QName(uri, localName));
            if (id==null)
                return false;  //todo can this case be optimized? Map of unsuccesful uris

            if (!ts.loadTypeSystemFromId(_resourceLoader, id))
                return false;

            if (!loadBindingSystemWithId(id))
                return false;
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }

        return true;
    }

    protected boolean loadFromGlobalPropertySdoQName(String uri, String sdoName)
    {
        TypeSystemBase ts = (TypeSystemBase)getTypeSystem();

        try
        {
            String id = ts.readIdForGlobalPropertySdoQName(_resourceLoader, new QName(uri, sdoName));
            if (id==null)
                return false;  //todo can this case be optimized? Map of unsuccesful uris

            if (!ts.loadTypeSystemFromId(_resourceLoader, id))
                return false;

            if (!loadBindingSystemWithId(id))
                return false;
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }

        return true;
    }

    protected boolean loadFromSchemaTypeName(String uri, String localName)
    {
        TypeSystemBase ts = (TypeSystemBase)getTypeSystem();

        try
        {
            String id = ts.readIdForSchemaTypeQName(_resourceLoader, new QName(uri, localName));
            if (id==null)
                return false;  //todo can this case be optimized? Map of unsuccesful uris

            // todo cezar A context has to be passed into so that external referenced types can be loaded
            if (!ts.loadTypeSystemFromId(_resourceLoader, id))
                return false;

            if (!loadBindingSystemWithId(id))
                return false;
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }

        return true;
    }

    protected abstract DataObjectXML getDataObjectForType(TypeXML typeXML, DataGraph datagraph, DataObjectXML container, PropertyXML containmentProperty);

    // persistance code
    static boolean saveBindingSystemWithId(Filer filer, String sdoBSId, TypeJavaMapping typeToJavaNames)
        throws IOException
    {
        OutputStream idFileStream = filer.createBinaryFile(getResourceNameForId(sdoBSId));
        if (idFileStream==null)
            return false;

        boolean result = saveBindingSystem(idFileStream, typeToJavaNames);
        idFileStream.close();

        return result;
    }

    public boolean loadBindingSystemWithId(String sdoBSId)
        throws IOException
    {
        InputStream idStream = _resourceLoader.getResourceAsStream(getResourceNameForId(sdoBSId));
        if (idStream==null)
            return false;

        loadBindingSystem(idStream);
        idStream.close();

        return true;
    }

    private static boolean saveBindingSystem(OutputStream outputStream, TypeJavaMapping typeToJavaNames)
        throws IOException
    {
        VersionedDataOutputStream vdos = new VersionedDataOutputStream(outputStream);

        vdos.writeInt(Versions.FILE_MAGIC_NUMBER);
        vdos.writeInt(Versions.CODE_VERSION_MAJOR);
        vdos.writeInt(Versions.CODE_VERSION_MINOR);
        vdos.writeInt(Versions.FILE_TYPE_BINDSYSTEM);

        vdos.writeEmptyWildcard();

        writeBindingSystem(vdos, typeToJavaNames);

        vdos.writeEmptyWildcard();

        vdos.close();

        return true;
    }

    private void loadBindingSystem(InputStream inputStream)
        throws IOException
    {
        VersionedDataInputStream vdis = new VersionedDataInputStream(inputStream);

        int fileMagicNumber = vdis.readInt();
        if (fileMagicNumber !=Versions.FILE_MAGIC_NUMBER)
            throw new RuntimeException("Corrupt file - wrong file magic number.");

        int fileVersionMajor = vdis.readInt();
        if (fileVersionMajor !=Versions.CODE_VERSION_MAJOR)
            throw new RuntimeException("Unsuported fileVersionMajor number: " + fileVersionMajor);

        int fileVersionMinor = vdis.readInt();
        vdis.setFileVersionMinor(fileVersionMinor);

        int fileType = vdis.readInt();
        switch (fileType)
        {
        case Versions.FILE_TYPE_BINDSYSTEM:

            vdis.readSkipWildcard(Versions.CODE_VERSION_MINOR_V0);

            readBindingSystem(vdis);

            vdis.readSkipWildcard(Versions.CODE_VERSION_MINOR_V0);

            break;

        default:
            throw new RuntimeException("Unknown file type: " + fileType);
        }

        vdis.close();
    }

    private static void writeBindingSystem(VersionedDataOutputStream vdos, TypeJavaMapping typeToJavaNames)
        throws IOException
    {
        TypeSystemBase.StringPool stringPool = new TypeSystemBase.StringPool();

        prepare(stringPool, typeToJavaNames);

        stringPool.writeTo(vdos);

        vdos.writeEmptyWildcard();

        Set<TypeXML> types = typeToJavaNames.getAllTypes();
        List<TypeXML> typesToBeWritten = new ArrayList<TypeXML>();

        for( TypeXML t : types )
        {
            if (!t.isBuiltinType())
                typesToBeWritten.add(t);
        }

        vdos.writeInt(typesToBeWritten.size());
        for( TypeXML t : typesToBeWritten )
        {
            vdos.writeInt(stringPool.codeForString(t.getName()));
            vdos.writeInt(stringPool.codeForString(t.getURI()));

            JavaClassName jcn = typeToJavaNames.getJavaClass(t);
            boolean isPrimitive = jcn.isPrimitive();

            vdos.writeBoolean(isPrimitive);

            if (isPrimitive)
            {
                vdos.writeInt(PrimitiveCodes.codeForPrimitiveClass(jcn.getIntfFullName()));
            }
            else
            {
                vdos.writeInt(stringPool.codeForString(jcn.getIntfFullName()));
                vdos.writeInt(stringPool.codeForString(jcn.getImplFullName()));
            }
            //System.out.println("Writing type binding: " + t.getName() + "@" + t.getURI());
            //System.out.println("                    : " + jcn.getIntfFullName() + " : " + jcn.getImplFullName());

            vdos.writeEmptyWildcard();
        }
    }

    private void readBindingSystem(VersionedDataInputStream vdis)
        throws IOException
    {
        TypeSystemBase.StringPool stringPool = new TypeSystemBase.StringPool();

        stringPool.readFrom(vdis);

        vdis.readSkipWildcard(Versions.CODE_VERSION_MINOR_V0);

        int size = vdis.readInt();
        for( int i=0; i<size; i++ )
        {
            String typeName = stringPool.stringForCode(vdis.readInt());
            String typeUri = stringPool.stringForCode(vdis.readInt());

            String intfFullName = null;
            String implFullName = null;
            Class  primitiveClass = null;

            boolean isPrimitive = vdis.readBoolean();

            if (isPrimitive)
            {
                primitiveClass = PrimitiveCodes.primitiveClassForCode(vdis.readInt());
                intfFullName = implFullName = primitiveClass.getSimpleName();
            }
            else
            {
                intfFullName = stringPool.stringForCode(vdis.readInt());
                implFullName = stringPool.stringForCode(vdis.readInt());
            }

            TypeXML type = getTypeSystem().getTypeXML(typeUri, typeName);

            if ( type == null )
            {
                //might be on classpath
                type = loadTypeByTypeName(typeUri, typeName);
                if ( type == null )
                    throw new RuntimeException("SDO Type " + typeName + "@" + typeUri + " not found. Make sure all required jars are on the classpath.");

                // this type should already have been mapped
                assert getIntfFullNameForType(type)!=null;
            }
            else if (type.isBuiltinType())
            {
                assert false;
            }
            else if (isPrimitive)
            {
                assert primitiveClass!=null;
                addMapping(type, primitiveClass);
            }
            else
            {
                assert intfFullName !=null;
                assert implFullName !=null;
                addMapping(type, intfFullName, implFullName);
            }

            //System.out.println("Reading type binding: " + type.getName() + "@" + type.getURI());
            //System.out.println("                    : " + intfFullName + " : " + implFullName);

            vdis.readSkipWildcard(Versions.CODE_VERSION_MINOR_V0);
        }
    }


    public abstract void addMapping(TypeXML type, Class intfImplName);
    public abstract void addMapping(TypeXML type, String fullIntfName, String fullImplName);

    private static void prepare(TypeSystemBase.StringPool stringPool, TypeJavaMapping typeToJavaNames)
    {
        Set<TypeXML> types = typeToJavaNames.getAllTypes();
        for(TypeXML t : types)
        {
            if (t.isBuiltinType())
                continue;

            stringPool.codeForString(t.getName());
            stringPool.codeForString(t.getURI());
            JavaClassName jcn = typeToJavaNames.getJavaClass(t);
            if (jcn!=null)
            {
                stringPool.codeForString( jcn.getIntfFullName() );
                stringPool.codeForString( jcn.getImplFullName() );
            }
        }
    }

    private static String getResourceNameForId(String sdoBSId)
    {
        return "metadata/id/" + sdoBSId + ".sdobsb";
    }
    // end persistance code

    public abstract void dump();
}
