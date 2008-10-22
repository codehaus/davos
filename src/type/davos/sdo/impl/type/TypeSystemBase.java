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
package davos.sdo.impl.type;

import davos.sdo.PropertyXML;
import davos.sdo.SDOContextFactory;
import davos.sdo.TypeXML;
import davos.sdo.impl.common.Names;
import davos.sdo.impl.util.PrimitiveCodes;
import davos.sdo.impl.binaryVersioning.Versions;
import davos.sdo.impl.binaryVersioning.VersionedDataOutputStream;
import davos.sdo.impl.binaryVersioning.VersionedDataInputStream;
import davos.sdo.type.TypeSystem;
import davos.sdo.util.Filer;
import org.apache.xmlbeans.ResourceLoader;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.impl.common.QNameHelper;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.SortedSet;
import java.util.Comparator;

import javax.sdo.Type;

/**
 * @author Cezar Andrei (cezar dot andrei at gmail dot com)
 *         Date: Jan 24, 2006
 */
public class TypeSystemBase
    implements TypeSystem
{
    private Map<QName, TypeXML> _qnameToType = new HashMap<QName, TypeXML>();
    private Map<QName, TypeXML> _schemaNameToType = new HashMap<QName, TypeXML>();
    private Map<QName, PropertyXML> _elemQNameToGlobalProperty = new HashMap<QName, PropertyXML>();
    private Map<QName, PropertyXML> _attrQNameToGlobalProperty = new HashMap<QName, PropertyXML>();
    private Map<QName, PropertyXML> _globalPropertiesBySdoQName = new HashMap<QName, PropertyXML>();
    private SchemaTypeLoader _schemaTypeLoader;
    private ClassLoader _cl;


    protected TypeSystemBase()
    {}


    public void setClassLoader(ClassLoader cl)
    {
        _cl = cl;
    }

    public static TypeSystemBase createEmptyTypeSystem()
    {
        return new TypeSystemBase();
    }

    public void addTypeSystem(TypeSystem ts)
    {
        addTypeSystem(ts, true);
    }

    public void addTypeSystem(TypeSystem ts, boolean addSTS)
    {
        // Not sure if we need to be extra clean at this point
        TypeSystemBase tsb = (TypeSystemBase) ts;
        // First, check if *any* of the names in the new typesystem collide with an
        // existing name. If yes, then don't load anything, like a transaction
        for (QName key : tsb._qnameToType.keySet())
        {
            if (_qnameToType.get(key) != null)
                throw new IllegalArgumentException("A type with this name '" + key + "' already exists in this type system.");
        }
        // Ok, so all types are new; now check the global properties
        for (QName key : tsb._elemQNameToGlobalProperty.keySet())
        {
            if (_elemQNameToGlobalProperty.get(key) != null)
                throw new IllegalArgumentException("A global element property with this name '" + key + "' already exists in this type system.");
        }
        for (QName key : tsb._attrQNameToGlobalProperty.keySet())
        {
            if (_attrQNameToGlobalProperty.get(key) != null)
                throw new IllegalArgumentException("A global attribute property with this name '" + key + "' already exists in this type system.");
        }
        for (QName key : tsb._globalPropertiesBySdoQName.keySet())
        {
            if (_globalPropertiesBySdoQName.get(key) != null)
                throw new IllegalArgumentException("A global property with this sdo name '" + key + "' already exists in this type system.");
        }
        // The checks are done, let's register the types and properties. Two options:
        // 1. Copy the references from the parameter TypeSystem into this one
        // 2. Add a reference to the parameter TypeSystem
        // We do 1.
        for (Map.Entry<QName, TypeXML> entry : tsb._qnameToType.entrySet())
            addTypeNoSchemaMapping(entry.getValue());
        _schemaNameToType.putAll(tsb._schemaNameToType);
        for (Map.Entry<QName, PropertyXML> entry : tsb._elemQNameToGlobalProperty.entrySet())
            _elemQNameToGlobalProperty.put(entry.getKey(), entry.getValue());
        for (Map.Entry<QName, PropertyXML> entry : tsb._attrQNameToGlobalProperty.entrySet())
            _attrQNameToGlobalProperty.put(entry.getKey(), entry.getValue());
        for (Map.Entry<QName, PropertyXML> entry : tsb._globalPropertiesBySdoQName.entrySet())
            _globalPropertiesBySdoQName.put(entry.getKey(), entry.getValue());

        // Add the SchemaTypeSystem to the current SchemaTypeLoader
        if (addSTS && tsb.getSchemaTypeLoader() != null)
            _schemaTypeLoader = XmlBeans.typeLoaderUnion(new SchemaTypeLoader[] {_schemaTypeLoader,
                tsb.getSchemaTypeLoader()});
    }

    public void addTypeMapping(TypeXML type)
    {
        assert type!=null;

        TypeXML existingType = _qnameToType.get(new QName(type.getURI(), type.getName()));
        if ( existingType!=null && type!=existingType)
            throw new IllegalArgumentException("A type with this name '" + type.getName() + "@" + type.getURI() + "' already exists in this type system.");

        addTypeMappingNoCheck(type);
    }

    private void addTypeMappingNoCheck(TypeXML type)
    {
        addTypeNoSchemaMapping(type);

        SchemaType st = type.getXMLSchemaType();
        if (st != null && st.getName() != null)
        {
            // Global schema type, add it to the list
            _schemaNameToType.put(st.getName(), type);
        }
        else if (type.getXMLSchemaTypeName()!=null)
        {
            _schemaNameToType.put(type.getXMLSchemaTypeName(), type);
        }
    }

    protected void addTypeNoSchemaMapping(TypeXML type)
    {
        _qnameToType.put(new QName(type.getURI(), type.getName()), type);
    }

    public void addGlobalProperty(PropertyXML globalProperty)
    {
        assert globalProperty != null;
        assert globalProperty.isGlobal();
        assert globalProperty.isOpenContent();
        // globalProperty.getXMLNamespaceURI() can be null;

        QName globalPropXMLQName = new QName(globalProperty.getXMLNamespaceURI(), globalProperty.getXMLName());
        QName globalPropSDOQName = new QName(globalProperty.getXMLNamespaceURI(), globalProperty.getName());

        if ( globalProperty.isXMLElement() && _elemQNameToGlobalProperty.get( globalPropXMLQName ) != null )
            throw new IllegalArgumentException("Duplicate define of global element property: " + globalPropXMLQName);
        else if ( !globalProperty.isXMLElement() && _attrQNameToGlobalProperty.get(globalPropXMLQName) != null)
            throw new IllegalArgumentException("Duplicate define of global attribute property: " + globalPropXMLQName);

        if ( _globalPropertiesBySdoQName.get(globalPropSDOQName)!=null)
            throw new IllegalArgumentException("Duplicate define, a property with the same SDO name already exists: " + globalPropSDOQName);
        else
            _globalPropertiesBySdoQName.put(globalPropSDOQName, globalProperty);

        if (globalProperty.isXMLElement())
            _elemQNameToGlobalProperty.put(globalPropXMLQName, globalProperty);
        else
            _attrQNameToGlobalProperty.put(globalPropXMLQName, globalProperty);
    }

    protected void addSpecialTypeMapping(String uri, String name, TypeXML type)
    {
        _schemaNameToType.put(new QName(uri, name), type);
    }

    public TypeXML getTypeXML(String uri, String typeName)
    {
        QName typeQName = new QName(uri, typeName);
        return _qnameToType.get(typeQName);
    }

    // TODO(radup) I'd prefer this to return a List; XSDHelper needs a List and it seems
    // that a Set is never really required
    public Set<TypeXML> getAllTypes()
    {
        Set<TypeXML> result = new HashSet<TypeXML>();
        result.addAll(_qnameToType.values());
        return result;
    }

    public PropertyXML getGlobalPropertyByTopLevelElemQName(String uri, String elemName)
    {
        return _elemQNameToGlobalProperty.get(new QName(uri, elemName));
    }

    public PropertyXML getGlobalPropertyByTopLevelAttrQName(String uri, String attrName)
    {
        return _attrQNameToGlobalProperty.get(new QName(uri, attrName));
    }

    public PropertyXML getGlobalPropertyBySdoQName(String uri, String sdoName)
    {
        return _globalPropertiesBySdoQName.get(new QName(uri, sdoName));
    }

    public TypeXML getTypeBySchemaTypeName(String schemaTypeUri, String schemaTypeLocalName)
    {
        QName schemaTypeQName = new QName(schemaTypeUri, schemaTypeLocalName);
        return _schemaNameToType.get(schemaTypeQName);
    }

    public SchemaTypeLoader getSchemaTypeLoader()
    {
        return _schemaTypeLoader;
    }

    public void setSchemaTypeLoader(SchemaTypeLoader schemaTypeLoader)
    {
        _schemaTypeLoader = schemaTypeLoader;
    }

    public TypeXML getTypeXML(Type type)
    {
        if (type==null)
            return null;

        if (type instanceof TypeXML)
            return (TypeXML)type;

        TypeXML result = _qnameToType.get(new QName(type.getURI(), type.getName()));
        if (result!=null)
            return result;

        throw new IllegalArgumentException("Other Type implementations not supported.");

//        TypeImpl typeImpl = TypeImpl.create();
//        typeImpl.init(type.getName(), type.getURI(), BuiltInTypeSystem.TYPECODE_USERDEFINED,
//            type.getInstanceClass(), type.isDataType(), type.isOpen(),
//            type.isSequenced(), type.isAbstract(), false, type.isSequenced(),
//            (List<TypeXML>)type.getBaseTypes(), (List<PropertyXML>)type.getDeclaredProperties(),
//            type.getAliasNames(), null, this);
//
//        typeImpl.makeImmutable();
//
//        return typeImpl;
    }

    // persistance code
    // WARNING: when modifying this code make sure to increment the file version number and
    //          make sure to have equivalent changes on the write and load sides
    public boolean saveTypeSystemWithName(Filer filer, String sdoTypeSystemName, String sdoId, Map<TypeXML, String> typeToInstanceClass)
        throws IOException
    {
        String resName = getResourceNameForTSName(sdoTypeSystemName);
        writeIdToResource(filer, resName, sdoId);

        //System.out.println("Writing typesystem file.");
        OutputStream idFileStream = filer.createBinaryFile(getResourceNameForTSId(sdoId));
        if (idFileStream==null)
            return false;

        boolean result = saveTypeSystem(idFileStream, typeToInstanceClass);
        idFileStream.close();

        // create a new file for each root Element to be able to identify the right .sdotsb file at runtime
        // this doesn't have a corespondent in loadTypeSystemFromName, but it's used in loadTypeSystemFromTopLevelQName()
        for (QName topLevelElemQName :_elemQNameToGlobalProperty.keySet())
        {
            String resourceName = getResourceNameForTopLevelElemQName(topLevelElemQName);
            writeIdToResource(filer, resourceName, sdoId);
        }
        for (QName topLevelAttrQName :_attrQNameToGlobalProperty.keySet())
        {
            String resourceName = getResourceNameForTopLevelAttrQName(topLevelAttrQName);
            writeIdToResource(filer, resourceName, sdoId);
        }
        for (QName globalPropQName :_globalPropertiesBySdoQName.keySet())
        {
            String resourceName = getResourceNameForGlobalPropertiesSdoQName(globalPropQName);
            writeIdToResource(filer, resourceName, sdoId);
        }

        // create a new file for each type to be able to identify its .sdotsb file at runtime
        // this doesn't have a corespondent in loadTypeSystemFromName, but it's used in loadTypeSystemFromTypeQName()
        for (QName typeQName :_qnameToType.keySet())
        {
            String resourceName = getResourceNameForTypeQName(typeQName);
            writeIdToResource(filer, resourceName, sdoId);
        }

        // create a new file for each schema type to be able to identify its .sdotsb file at runtime
        // this doesn't have a corespondent in loadTypeSystemFromName, but it's used in loadTypeSystemFromSchemaTypeQName()
        for (QName schemaTypeQName :_schemaNameToType.keySet())
        {
            String resourceName = getResourceNameForSchemaTypeQName(schemaTypeQName);
            writeIdToResource(filer, resourceName, sdoId);
        }
        return result;
    }

    private void writeIdToResource(Filer filer, String resourceName, String sdoId)
        throws IOException
    {
        OutputStream topLevelStream = filer.createBinaryFile(resourceName);
        VersionedDataOutputStream vdos = new VersionedDataOutputStream(topLevelStream);
        vdos.writeInt(Versions.FILE_MAGIC_NUMBER);
        vdos.writeInt(Versions.CODE_VERSION_MAJOR);
        vdos.writeInt(Versions.CODE_VERSION_MINOR);
        vdos.writeInt(Versions.FILE_TYPE_ID);
        vdos.writeUTF(sdoId);

        vdos.writeEmptyWildcard();

        vdos.close();
        topLevelStream.close();
    }

    public boolean loadTypeSystemFromName(ResourceLoader resourceLoader, String sdoTSName)
        throws IOException
    {
        //System.out.println("Reading typesystem name file.");
        // find the name file
        String nameResource = getResourceNameForTSName(sdoTSName);
        String id = readTSIdFromResource(resourceLoader, nameResource);

        return loadTypeSystemFromId(resourceLoader, id);
    }

    // the write side of this is in saveTypeSystemWithName()
    public boolean loadTypeSystemFromTopLevelElemQName(ResourceLoader resourceLoader, QName topLevelElemQName)
        throws IOException
    {
        String id = readIdForTopLevelElemQName(resourceLoader, topLevelElemQName);
        return loadTypeSystemFromId(resourceLoader, id);
    }

    // the write side of this is in saveTypeSystemWithName()
    public boolean loadTypeSystemFromTopLevelAttrQName(ResourceLoader resourceLoader, QName topLevelAttrQName)
        throws IOException
    {
        String id = readIdForTopLevelAttrQName(resourceLoader, topLevelAttrQName);
        return loadTypeSystemFromId(resourceLoader, id);
    }

    // the write side of this is in saveTypeSystemWithName()
    public boolean loadTypeSystemFromTypeQName(ResourceLoader resourceLoader, QName typeQName)
        throws IOException
    {
        String id = readIdForTypeQName(resourceLoader, typeQName);
        return loadTypeSystemFromId(resourceLoader, id);
    }

    // the write side of this is in saveTypeSystemWithName()
    public boolean loadTypeSystemFromSchemaTypeQName(ResourceLoader resourceLoader, QName schemaTypeQName)
        throws IOException
    {
        String id = readIdForSchemaTypeQName(resourceLoader, schemaTypeQName);
        return loadTypeSystemFromId(resourceLoader, id);
    }

    public String readIdForTopLevelElemQName(ResourceLoader resourceLoader, QName topLevelElemQName)
        throws IOException
    {
        return readTSIdFromResource(resourceLoader, getResourceNameForTopLevelElemQName(topLevelElemQName));
    }

    public String readIdForTopLevelAttrQName(ResourceLoader resourceLoader, QName topLevelAttrQName)
        throws IOException
    {
        return readTSIdFromResource(resourceLoader, getResourceNameForTopLevelAttrQName(topLevelAttrQName));
    }

    public String readIdForGlobalPropertySdoQName(ResourceLoader resourceLoader, QName sdoQName)
        throws IOException
    {
        return readTSIdFromResource(resourceLoader, getResourceNameForGlobalPropertiesSdoQName(sdoQName));
    }

    public String readIdForTypeQName(ResourceLoader resourceLoader, QName typeQName)
        throws IOException
    {
        return readTSIdFromResource(resourceLoader, getResourceNameForTypeQName(typeQName));
    }

    public String readIdForSchemaTypeQName(ResourceLoader resourceLoader, QName schemaTypeQName)
        throws IOException
    {
        return readTSIdFromResource(resourceLoader, getResourceNameForSchemaTypeQName(schemaTypeQName));
    }

    private String readTSIdFromResource(ResourceLoader resourceLoader, String resourceName)
        throws IOException
    {
        InputStream inputStream = resourceLoader.getResourceAsStream(resourceName);

        if (inputStream==null)
            return null;

        // read id from file
        VersionedDataInputStream vdis = new VersionedDataInputStream(inputStream);

        int magic = vdis.readInt();
        if (magic!=Versions.FILE_MAGIC_NUMBER)
            throw new RuntimeException("Corrupt file - wrong magic number.");

        int fileVersionMajor = vdis.readInt();
        if (fileVersionMajor !=Versions.CODE_VERSION_MAJOR)
            throw new RuntimeException("Unsuported fileVersionMajor number: " + fileVersionMajor);

        int fileVersionMinor = vdis.readInt();
        vdis.setFileVersionMinor(fileVersionMinor);

        String sdoId;
        int fileType = vdis.readInt();

        switch (fileType)
        {
        case Versions.FILE_TYPE_ID:
            sdoId = vdis.readUTF();

            vdis.readSkipWildcard(Versions.CODE_VERSION_MINOR_V0);

            break;

        default:
            throw new RuntimeException("Unknown file type: " + fileType);
        }

        vdis.close();
        //System.out.println("ID = " + id);
        return sdoId;
    }


    public boolean loadTypeSystemFromId(ResourceLoader resourceLoader, String sdoTSId)
    {
        return loadTypeSystemFromResourceName(resourceLoader, getResourceNameForTSId(sdoTSId));
    }

    private boolean loadTypeSystemFromResourceName(ResourceLoader resourceLoader, String resourceName)
    {
        //System.out.println("  Load TypeSystem from: " + resourceName);
        InputStream is = resourceLoader.getResourceAsStream(resourceName);
        if (is == null)
            return false;

        try
        {
            loadTypeSystem(is);
        }
        catch (StreamCorruptedException e)
        {
            throw new RuntimeException("Exception during loading of TypeSystem from: " + resourceName + ". ERROR: " + e.getMessage(), e);
        }
        catch (IOException e)
        {
            System.err.println("  Load TypeSystem from: " + resourceName + ". ERROR.");
            e.printStackTrace();
            return false;
        }

        //System.out.println("  Load TypeSystem from: " + resourceName + ". Done.");
        return true;
    }

    private boolean saveTypeSystem(OutputStream outputStream, Map<TypeXML, String> typeToInstanceClass)
        throws IOException
    {
        VersionedDataOutputStream vdos = new VersionedDataOutputStream(outputStream);

        vdos.writeInt(Versions.FILE_MAGIC_NUMBER);
        vdos.writeInt(Versions.CODE_VERSION_MAJOR);
        vdos.writeInt(Versions.CODE_VERSION_MINOR);
        vdos.writeInt(Versions.FILE_TYPE_TYPESYSTEM);

        vdos.writeEmptyWildcard();

        writeTypeSystem(vdos, typeToInstanceClass);

        vdos.writeEmptyWildcard();

        vdos.close();

        return true;
    }

    private void loadTypeSystem(InputStream is)
        throws IOException
    {
        VersionedDataInputStream vdis = new VersionedDataInputStream(is);

        int magic = vdis.readInt();
        if (magic!=Versions.FILE_MAGIC_NUMBER)
            throw new RuntimeException("Corrupt file - wrong magic number.");

        int fileVersionMajor = vdis.readInt();
        if (fileVersionMajor !=Versions.CODE_VERSION_MAJOR)
            throw new RuntimeException("Unsuported fileVersionMajor number: " + fileVersionMajor);

        int fileVersionMinor = vdis.readInt();
        vdis.setFileVersionMinor(fileVersionMinor);

        int fileType = vdis.readInt();
        switch (fileType)
        {
        case Versions.FILE_TYPE_TYPESYSTEM:

            vdis.readSkipWildcard(Versions.CODE_VERSION_MINOR_V0);

            readTypeSystem(vdis);

            vdis.readSkipWildcard(Versions.CODE_VERSION_MINOR_V0);

            break;

        default:
            throw new RuntimeException("Unknown file type: " + fileType);
        }

        vdis.close();
    }

    private void writeTypeSystem(VersionedDataOutputStream vdos, Map<TypeXML, String> typeToInstanceClass)
        throws IOException
    {
        //System.out.println("Writing SDOTypeSystem");

        FileStructure fs = FileStructure.createForWrite(this, typeToInstanceClass);
        prepareDataForWrite(fs);                    // to add all the string to the StringPool
        fs.writeTo(vdos);                            // then write starting with StringPool everything else pointing to the string table
    }

    private void readTypeSystem(VersionedDataInputStream dis)
        throws IOException
    {
        //System.out.println("Reading SDOTypeSystem");

        // Using the classloader that was passed in from the SDO Context
//        FileStructure fs = FileStructure.createForRead(this.getClass().getClassLoader());
        FileStructure fs = FileStructure.createForRead(_cl == null ? this.getClass().getClassLoader() : _cl);
        fs.readFrom(dis, this);                           // read everything starting with the strings table
        finalizeSetup(fs, this);                                // fill out the holes
    }

    private void finalizeSetup(FileStructure fs, TypeSystemBase sdoTypeSystem)
    {
        for (TypeXML type : fs._types)
        {
            if (sdoTypeSystem.getTypeXML(type.getURI(), type.getName())==null)
            {
                //System.out.println("  Type makeImmutable: " + type.getName() + " @ " + type.getURI());
                ((TypeImpl)type).makeImmutable();
            }
        }

        synchronized (this)
        {
            for (TypeXML type : fs._types)
            {
                if (type.getName().equals(Names.NAME_OF_CONTAINING_TYPE_FOR_GLOBAL_PROPERTIES))
                    continue; // skip special containing types og global properties

                if (sdoTypeSystem.getTypeXML(type.getURI(), type.getName())==null)
                {
                    //System.out.println("  Type add to TS: " + type.getName() + " @ " + type.getURI());
                    addTypeMapping(type);
                }
            }

            Map<QName, PropertyXML> loadedElemQNameToGlobalProperty = fs.getElemQNameToGlobalPropMap();
            for (QName elemQName : loadedElemQNameToGlobalProperty.keySet())
            {
                PropertyXML existingElemGlobalProp = _elemQNameToGlobalProperty.get(elemQName);
                PropertyXML loadedElemGlobalProp = loadedElemQNameToGlobalProperty.get(elemQName);
                if (existingElemGlobalProp==null)
                {
                    addGlobalProperty(loadedElemGlobalProp);
                }
                else
                    if (existingElemGlobalProp!=loadedElemGlobalProp)
                        throw new IllegalStateException("Global property already loaded for element name: " + elemQName);
                    // else do nothing existingElemGlobalProp already in map
            }

            Map<QName, PropertyXML> loadedAttrQNameToGlobalProperty = fs.getAttrQNameToGlobalPropMap();
            for (QName attrQName : loadedAttrQNameToGlobalProperty.keySet())
            {
                PropertyXML existingAttrGlobalProp = _attrQNameToGlobalProperty.get(attrQName);
                PropertyXML loadedAttrGlobalProp = loadedAttrQNameToGlobalProperty.get(attrQName);
                if (existingAttrGlobalProp==null)
                    addGlobalProperty(loadedAttrGlobalProp);
                else
                    if (existingAttrGlobalProp!=loadedAttrGlobalProp)
                        throw new IllegalStateException("Global property already loaded for attribute name: " + attrQName);
                    // else do nothing existingAttrGlobalProp already in map
            }
        }
    }

    private void prepareDataForWrite(FileStructure fs)
    {
        for (TypeXML type : getAllTypes())
        {
            fs.prepareType(type);
        }

        fs.prepareProperties();

        fs.setElemAndAttrQNameToGlobalPropMap(_elemQNameToGlobalProperty, _attrQNameToGlobalProperty);

        fs.prepareElemAndAttrQNames();
    }

    static class FileStructure
    {
        private StringPool _stringPool;
        private List<TypeXML> _types;
        private List<PropertyXML> _properties;
        private Map<QName, PropertyXML> _elemQNameToGlobalProp;
        private Map<QName, PropertyXML> _attrQNameToGlobalProp;

        // helpers
        private Map<TypeXML, Integer> _typesToInts;
        private Map<PropertyXML, Integer> _propertiesToInts;
        private ClassLoader _classLoader;
        private boolean _immutable;
        private TypeSystem _contextSDOTypeSystem;
        private Map<TypeXML, String> _typeToInstanceClass;

        private FileStructure()
        {}

        public static FileStructure createForWrite(TypeSystem contextTypeSystem, Map<TypeXML, String> typeToInstanceClass)
        {
            FileStructure fs = new FileStructure();
            fs._stringPool = new StringPool();
            fs._types = new ArrayList<TypeXML>();
            fs._properties = new ArrayList<PropertyXML>();

            // helpers
            fs._typesToInts = new HashMap<TypeXML, Integer>();
            fs._propertiesToInts = new HashMap<PropertyXML, Integer>();
            fs._immutable = false;
            fs._contextSDOTypeSystem = contextTypeSystem;
            fs._typeToInstanceClass = typeToInstanceClass;

            return fs;
        }

        public static FileStructure createForRead(ClassLoader cl)
        {
            FileStructure fs = new FileStructure();
            fs._stringPool = new StringPool();
            fs._types = new ArrayList<TypeXML>();
            fs._properties = new ArrayList<PropertyXML>();

            // helpers
            fs._typesToInts = new HashMap<TypeXML, Integer>();
            fs._propertiesToInts = new HashMap<PropertyXML, Integer>();
            fs._classLoader = cl;
            fs._immutable = false;

            return fs;
        }

        void prepareType(TypeXML type)
        {
            if (_typesToInts.containsKey(type))
                return;

            codeForType(type);

            _stringPool.codeForString(type.getName());
            _stringPool.codeForString(type.getURI());

            _stringPool.codeForString(type.getXMLSchemaTypeSignature());

            QName schemaTypeName = type.getXMLSchemaTypeName();
            if (schemaTypeName!=null)
            {
                _stringPool.codeForString(schemaTypeName.getPrefix());
                _stringPool.codeForString(schemaTypeName.getLocalPart());
                _stringPool.codeForString(schemaTypeName.getNamespaceURI());
            }

            for( String aliasName : (List<String>)type.getAliasNames() )
            {
                _stringPool.codeForString(aliasName);
            }

            for( TypeXML baseType : (List<TypeXML>)type.getBaseTypes() )
            {
                prepareRefType(baseType);
            }

            String className = type.getInstanceClass()!=null ?
                    type.getInstanceClass().getName() :
                    _typeToInstanceClass.get(type);
            _stringPool.codeForString(className);

            prepareRefType(type.getListItemType());

            // metadata properties
            List<PropertyXML> metadataProps = (List<PropertyXML>)type.getInstanceProperties();
            for (int i = 0; metadataProps!=null && i < metadataProps.size(); i++)
            {
                PropertyXML metadataProp = (PropertyXML) metadataProps.get(i);
                prepareProperty(metadataProp);
            }
        }

        void prepareRefType(TypeXML refType)
        {
            if (refType==null)
                return;
            
            if ( refType.getSDOTypeSystem()==_contextSDOTypeSystem)
                prepareType(refType);
            else
            {
                _stringPool.codeForString(refType.getName());
                _stringPool.codeForString(refType.getURI());
            }
        }

        void prepareProperties()
        {
            for ( int i=0; i<_types.size(); i++)
            {
                for ( PropertyXML prop : _types.get(i).getDeclaredPropertiesXML() )
                {
                    prepareProperty(prop);
                }
            }
        }

        void prepareProperty(PropertyXML prop)
        {
            if (prop==null)
                return;

            if (_propertiesToInts.containsKey(prop))
                return;

            codeForProperty(prop);

            _stringPool.codeForString(prop.getName());

            prepareRefType(prop.getTypeXML());
            prepareRefType(prop.getContainingTypeXML());

            PropertyXML oposite = prop.getOppositeXML();
            prepareProperty(oposite);

            for (String aliasName : (List<String>)prop.getAliasNames())
            {
                _stringPool.codeForString(aliasName);
            }

            _stringPool.codeForString(prop.getXMLName());
            _stringPool.codeForString(prop.getXMLNamespaceURI());

            // prepare substitutions
            PropertyXML[] substs = prop.getAcceptedSubstitutions();
            if (substs!=null)
                for (int i = 0; i<substs.length; i++)
                    prepareProperty(substs[i]);

            // metadata properties
            List<PropertyXML> metadataProps = (List<PropertyXML>)prop.getInstanceProperties();
            for (int i = 0; i < metadataProps.size(); i++)
            {
                PropertyXML metadataProp = (PropertyXML) metadataProps.get(i);
                preparePropertyRef(metadataProp);
            }
        }

        void preparePropertyRef(PropertyXML refProp)
        {
            assert refProp!=null : "Metadata properties can't be null.";

            if ( refProp.getContainingTypeXML().getSDOTypeSystem()==_contextSDOTypeSystem)
                prepareProperty(refProp);
            else
            {
                if (refProp.isGlobal())
                {
                    _stringPool.codeForString(refProp.getName());
                    _stringPool.codeForString(refProp.getXMLName());
                    _stringPool.codeForString(refProp.getXMLNamespaceURI());
                }
                else
                    throw new IllegalStateException("Property '" + refProp + "' is not in the same type system and is not global, it cannot be found at load time.");
            }
        }

        int codeForType(TypeXML type)
        {
            if (type==null)
                return -1;

            Integer index = _typesToInts.get(type);

            if (index==null)
            {
                if (_immutable)
                    throw new IllegalStateException("Try to modify after prepare for write!");

                index = _types.size();
                _types.add(type);
                _typesToInts.put(type,  index);
            }

            return index;
        }

        int codeForProperty(PropertyXML prop)
        {
            if (prop ==null)
                return -1;

            Integer index = _propertiesToInts.get(prop);

            if (index==null)
            {
                if (_immutable)
                    throw new IllegalStateException();

                index = _properties.size();
                _properties.add(prop);
                _propertiesToInts.put(prop, index);
            }

            return index;
        }

        TypeXML typeForCode(int code)
        {
            if (code==-1)
                return null;

            return _types.get(code);
        }

        PropertyXML propertyForCode(int code)
        {
            if (code==-1)
                return null;

            return _properties.get(code);
        }


        private Map<QName, PropertyXML> getElemQNameToGlobalPropMap()
        {
            return _elemQNameToGlobalProp;
        }

        private void setElemAndAttrQNameToGlobalPropMap(Map<QName, PropertyXML> elemQNameToGlobalProp,
            Map<QName, PropertyXML> attrQNameToGlobalProp)
        {
            _elemQNameToGlobalProp = elemQNameToGlobalProp;
            _attrQNameToGlobalProp = attrQNameToGlobalProp;
        }

        private Map<QName, PropertyXML> getAttrQNameToGlobalPropMap()
        {
            return _attrQNameToGlobalProp;
        }

        public void writeTo(VersionedDataOutputStream vdos)
            throws IOException
        {
            // This order has to be respected since the read has to be in this order
            // 1. write the strings
            // 2. write wildcard
            // 3. write the types
            // 4. write the properties
            // 5. write the elemQnameToGlobalProp
            // 6. and attrQnameToGlobalProp maps

            makeImmutable();

            // 1.
            _stringPool.writeTo(vdos);

            // 2.
            vdos.writeEmptyWildcard();

            // 3.
            //System.out.println(" Types size: " + _types.size());
            //System.out.println(" Props size: " + _properties.size());
            vdos.writeInt(_types.size());
            vdos.writeInt(_properties.size());

            for (TypeXML type : _types)
            {
                writeTypeXML(vdos, type);
            }

            // 4.
            for (PropertyXML prop : _properties)
            {
                writePropertyXML(vdos, prop);
            }

            // 5.
            Set<QName> elemQNames = _elemQNameToGlobalProp.keySet();
            vdos.writeInt(elemQNames.size());
            for (QName elemQName : elemQNames)
            {
                vdos.writeInt(_stringPool.codeForString(elemQName.getPrefix()));
                vdos.writeInt(_stringPool.codeForString(elemQName.getLocalPart()));
                vdos.writeInt(_stringPool.codeForString(elemQName.getNamespaceURI()));

                PropertyXML elemGlobalProp = _elemQNameToGlobalProp.get(elemQName);
                //vdos.writeInt(_stringPool.codeForString(elemGlobalProp.getContainingTypeXML().getURI()));
                vdos.writeInt(codeForProperty(elemGlobalProp));

                vdos.writeEmptyWildcard();
            }

            // 6.
            Set<QName> attrQNames = _attrQNameToGlobalProp.keySet();
            vdos.writeInt(attrQNames.size());
            for (QName attrQName : attrQNames)
            {
                vdos.writeInt(_stringPool.codeForString(attrQName.getPrefix()));
                vdos.writeInt(_stringPool.codeForString(attrQName.getLocalPart()));
                vdos.writeInt(_stringPool.codeForString(attrQName.getNamespaceURI()));

                PropertyXML attrGlobalProp = _attrQNameToGlobalProp.get(attrQName);
                //vdos.writeInt(_stringPool.codeForString(attrGlobalProp.getContainingTypeXML().getURI()));
                vdos.writeInt(codeForProperty(attrGlobalProp));

                vdos.writeEmptyWildcard();
            }
        }

        public void readFrom(VersionedDataInputStream vdis, TypeSystemBase sdoTypeSystem)
            throws IOException
        {
            // This order has to be respected since readTypeXML and readPropertyXML uses references
            // to the objects in _types and _properties
            // 1. read the strings
            // 2. read wildcard
            // 3. read the types
            // 4. read the properties
            // 5. read the elemQNameToRootProp map
            // 6. and attrQNameToGlobalProp map

            // 1.
            _stringPool.readFrom(vdis);

            // 2.
            vdis.readSkipWildcard(Versions.CODE_VERSION_MINOR_V0);


            // 3.
            int sizeTypes = vdis.readInt();
            int sizeProperties = vdis.readInt();
            //System.out.println(" Types size: " + sizeTypes);
            //System.out.println(" Props size: " + sizeProperties);

            // prepare
            for (int i=0; i<sizeTypes; i++)
            {
                _types.add(TypeImpl.create());
            }

            for (int i=0; i<sizeProperties; i++)
            {
                _properties.add(PropertyImpl.create());
            }

            // read
            for (int i=0; i<sizeTypes; i++)
            {
                readTypeXML(vdis, (TypeImpl)_types.get(i), i, sdoTypeSystem);
            }

            // 4.
            for (int i=0; i<sizeProperties; i++)
            {
                readPropertyXML(vdis, i);
            }

            // 5.
            int sizeElemMap = vdis.readInt();
            _elemQNameToGlobalProp = new HashMap<QName, PropertyXML>(sizeElemMap);
            for (int i=0; i<sizeElemMap; i++)
            {
                String prefix = _stringPool.stringForCode(vdis.readInt());
                String local  = _stringPool.stringForCode(vdis.readInt());
                String uri    = _stringPool.stringForCode(vdis.readInt());

                PropertyXML elemGlobalProp = propertyForCode(vdis.readInt());

                vdis.readSkipWildcard(Versions.CODE_VERSION_MINOR_V0);

                PropertyXML existingElemGlobalProp = sdoTypeSystem.getGlobalPropertyByTopLevelElemQName(uri, local);
                if (existingElemGlobalProp!=null)
                    elemGlobalProp = existingElemGlobalProp;

                _elemQNameToGlobalProp.put(new QName(uri, local, prefix), elemGlobalProp);
            }

            // 6.
            int sizeAttrMap = vdis.readInt();
            _attrQNameToGlobalProp = new HashMap<QName, PropertyXML>(sizeAttrMap);
            for (int i=0; i<sizeAttrMap; i++)
            {
                String prefix = _stringPool.stringForCode(vdis.readInt());
                String local  = _stringPool.stringForCode(vdis.readInt());
                String uri    = _stringPool.stringForCode(vdis.readInt());

                PropertyXML attrGlobalProp = propertyForCode(vdis.readInt());

                vdis.readSkipWildcard(Versions.CODE_VERSION_MINOR_V0);

                PropertyXML existingAttrGlobalProp = sdoTypeSystem.getGlobalPropertyByTopLevelAttrQName(uri, local);
                if (existingAttrGlobalProp!=null)
                    attrGlobalProp = existingAttrGlobalProp;

                _attrQNameToGlobalProp.put(new QName(uri, local, prefix), attrGlobalProp);
            }
        }

        private void writeTypeXML(VersionedDataOutputStream vdos, TypeXML type)
            throws IOException
        {
            //System.out.println("    writing Type: " + type.getName() + " @ " + type.getURI());
            vdos.writeInt(_stringPool.codeForString(type.getName()));
            vdos.writeInt(_stringPool.codeForString(type.getURI()));

            vdos.writeInt(_stringPool.codeForString(type.getXMLSchemaTypeSignature()));
            QName schemaTypeName = type.getXMLSchemaTypeName();
            writeQName(vdos, schemaTypeName);

            List<String> aliasNames = (List<String>)type.getAliasNames();
            vdos.writeInt(aliasNames.size());
            for( String alias : aliasNames)
            {
                vdos.writeInt(_stringPool.codeForString(alias));
            }

            List<TypeXML> baseTypes = (List<TypeXML>)type.getBaseTypes();
            vdos.writeInt(baseTypes.size());
            //System.out.println("       base types size: " + baseTypes.size());
            for( TypeXML baseType : baseTypes)
            {
                assert baseType!=null;
                writeTypeRef(vdos, baseType);
            }

            String instClassString = null;
            Class instClass = type.getInstanceClass();

            if (instClass!=null)
                instClassString = instClass.getName();
            else
                instClassString = _typeToInstanceClass.get(type);

            if ( instClassString == null )
                vdos.writeInt(0);
            else if ( instClass!=null && (instClass.isPrimitive() || instClass==byte[].class))
            {
                vdos.writeInt(1);
                vdos.writeInt( PrimitiveCodes.codeForPrimitiveClass(instClass.getSimpleName()) );
            }
            else
            {
                vdos.writeInt(2);
                vdos.writeInt(_stringPool.codeForString(instClassString));
                //System.out.println("       className: " + instClassName);
            }

            List<PropertyXML> props = type.getDeclaredPropertiesXML();
            vdos.writeInt(props.size());
            //System.out.println("       props size: " + props.size());
            for (PropertyXML prop : props)
            {
                assert prop!=null;
                vdos.writeInt(codeForProperty(prop));
                //System.out.println("         prop: " + prop.getName());
            }

            vdos.writeInt(type.getTypeCode());
            vdos.writeBoolean(type.isAbstract());
            vdos.writeBoolean(type.isDataType());
            vdos.writeBoolean(type.isOpen());
            vdos.writeBoolean(type.isSequenced());
            vdos.writeBoolean(type.isSimpleContent());
            vdos.writeBoolean(type.isMixedContent());
            vdos.writeBoolean(type.hasCustomizedInstanceClass());
            writeTypeRef(vdos, type.getListItemType());

            //System.out.println("      :" + (type.isAbstract() ? " abstract" : "") + (type.isDataType() ? " datatype" : "") +
            //    (type.isOpen() ? " open" : "") + (type.isSequenced() ? " seq" : ""));

            // metadata properties
            List<PropertyXML> metadataProps = type.getInstanceProperties();
            vdos.writeInt(metadataProps==null ? 0 : metadataProps.size());
            //System.out.println("       metadataProps size: " + metadataProps.size());
            for (int i=0;  metadataProps!=null && i<metadataProps.size(); i++)
            {
                PropertyXML metadataProp = metadataProps.get(i);
                assert metadataProp !=null;
                vdos.writeInt(codeForProperty(metadataProp));
                writeObjectAsByteArray(vdos, type.get(metadataProp));
                //System.out.println("         metadatProp name: " + metadatProp.getName() + " value: " + type.get(metadataProp));
            }

            vdos.writeEmptyWildcard();
        }

        private void readTypeXML(VersionedDataInputStream vdis, TypeImpl type, int index, TypeSystemBase sdoTypeSystem)
            throws IOException
        {
            //System.out.print("    reading Type: ");
            String name = _stringPool.stringForCode(vdis.readInt());
            //System.out.print(name);
            String uri = _stringPool.stringForCode(vdis.readInt());
            //System.out.println(" @ " + uri);

            String schemaTypeSignature = _stringPool.stringForCode(vdis.readInt());
            //System.out.println("      schema sig: " + schemaTypeSignature);
            QName schemaTypeName = readQName(vdis);
            //System.out.println("      schema name: " + schemaTypeName);

            int size = vdis.readInt();
            //System.out.println("      alias names size: " + size);
            List<String> aliasNames = new ArrayList<String>(size);
            for (int i=0 ; i<size; i++)
            {
                String aliasName = _stringPool.stringForCode(vdis.readInt());
                aliasNames.add(aliasName);
                //System.out.println("        alias name: " + aliasName);
            }

            size = vdis.readInt();
            //System.out.println("      base types size: " + size);
            List<TypeXML> baseTypes = new ArrayList<TypeXML>(size);
            for (int i=0 ; i<size; i++)
            {
                TypeXML baseType = readTypeRef(vdis);
                assert baseType!=null;
                baseTypes.add(baseType);
            }

            Class cls;
            int classType = vdis.readInt();
            switch (classType)
            {
            case 0:
                cls = null;
                break;
            case 1:
                cls = PrimitiveCodes.primitiveClassForCode(vdis.readInt());
                break;
            case 2:
                String className = _stringPool.stringForCode(vdis.readInt());
                //System.out.println("      class name: " + className);
                assert className!=null;
                try
                {
                    cls = _classLoader.loadClass(className);
                }
                catch (ClassNotFoundException e)
                {

                    throw new RuntimeException("Class '" + className + "' not found when loading " +
                        "type '" + name + "@" + uri + "'", e);
                }
                break;
            default:
                throw new IllegalStateException("Unknown classType code.");
            }

            size = vdis.readInt();
            //System.out.println("      props size: " + size);
            List<PropertyXML> props = new ArrayList<PropertyXML>(size);
            for (int i=0 ; i<size; i++)
            {
                int propId = vdis.readInt();
                PropertyXML prop = propertyForCode(propId);
                props.add(prop);
                assert prop!=null;
                //System.out.println("        propId: " + propId);
            }

            int typeCode = vdis.readInt();
            boolean isAbstract = vdis.readBoolean();
            boolean isDataType = vdis.readBoolean();
            boolean isOpen = vdis.readBoolean();
            boolean isSequenced = vdis.readBoolean();
            boolean isSimpleContent = vdis.readBoolean();
            boolean isMixedContent = vdis.readBoolean();
            boolean hasCustomizedInstanceClass = vdis.readBoolean();
            TypeXML listItemType = readTypeRef(vdis);

            // metadata properties
            size = vdis.readInt();
            //System.out.println("        metadataProps size: " + size);
            List<PropertyXML> metadataProps = new ArrayList<PropertyXML>(size);
            List<Object> metadataValues = new ArrayList<Object>(size);
            for (int i=0 ; i<size; i++)
            {
                int propId = vdis.readInt();
                PropertyXML metadataProp = propertyForCode(propId);
                metadataProps.add(metadataProp);
                assert metadataProp!=null;
                Object metadataValue = readObjectAsByteArray(vdis);
                metadataValues.add(metadataValue);
                //System.out.println("        metadataPropId: " + propId + " value : " + metadataValue);
            }

            vdis.readSkipWildcard(Versions.CODE_VERSION_MINOR_V0);



            TypeXML existingType = sdoTypeSystem.getTypeXML(uri, name);
            if (existingType!=null)
            {   // if one is already in the context use that one
                //System.out.println("      : already loaded.");
                _types.set(index, existingType);
                assert existingType.getTypeCode() == typeCode            : "Type " + uri + "@" + name + " from classpath different: typeCode: " + existingType.getTypeCode() + " vs " + typeCode;
                assert existingType.isAbstract() == isAbstract           : "Type " + uri + "@" + name + " from classpath different: isAbstract: " + existingType.isAbstract() + " vs " + isAbstract;
                assert existingType.isDataType() == isDataType           : "Type " + uri + "@" + name + " from classpath different: isDataType: " + existingType.isDataType() + " vs " + isDataType;
                assert existingType.isOpen() == isOpen                   : "Type " + uri + "@" + name + " from classpath different: isOpen: " + existingType.isOpen() + " vs " + isOpen;
                assert existingType.isSequenced() == isSequenced         : "Type " + uri + "@" + name + " from classpath different: isSequenced: " + existingType.isSequenced() + " vs " + isSequenced;
                assert existingType.isSimpleContent() == isSimpleContent : "Type " + uri + "@" + name + " from classpath different: isSimpleContent: " + existingType.isSimpleContent() + " vs " + isSimpleContent;
                assert existingType.isMixedContent() == isMixedContent   : "Type " + uri + "@" + name + " from classpath different: isMixedContent: " + existingType.isMixedContent() + " vs " + isMixedContent;
                assert existingType.hasCustomizedInstanceClass() == hasCustomizedInstanceClass : "Type " + uri + "@" + name + " from classpath different: hasCustomizedInstanceClass: " + existingType.hasCustomizedInstanceClass() + " vs " + hasCustomizedInstanceClass;
                assert existingType.getDeclaredPropertiesXML().size() == props.size() : "Type " + uri + "@" + name + " from classpath different: number of declared properties: " + existingType.getDeclaredPropertiesXML().size() + " vs " + props.size();
            }
            else
            {
                //System.out.println("      :" + (isAbstract ? " abstract" : "") + (isDataType ? " datatype" : "") +
                //    (isOpen ? " open" : "") + (isSequenced ? " seq" : ""));
                type.init(name, uri, typeCode, cls, isDataType, isOpen, isSequenced, isAbstract, isSimpleContent, isMixedContent,
                baseTypes, props, aliasNames, null, sdoTypeSystem, listItemType, hasCustomizedInstanceClass);
                type.setXMLSchemaType(schemaTypeSignature, schemaTypeName);

                for (int i = 0; i < metadataProps.size(); i++)
                {
                    PropertyXML metadataProp = (PropertyXML) metadataProps.get(i);
                    PropertyXML metadataValue = (PropertyXML) metadataProps.get(i);
                    type.addPropertyValue(metadataProp, metadataValue);
                }                
            }
        }

        private void writeTypeRef(VersionedDataOutputStream vdos, TypeXML refType)
            throws IOException
        {
            boolean internal = (refType==null || refType.getSDOTypeSystem()==_contextSDOTypeSystem);
            vdos.writeBoolean(internal);
            if (internal)
            {
                vdos.writeInt(codeForType(refType));
                //System.out.println("         i: " + refType.getName() + " @ " + refType.getURI());
            }
            else
            {
                //external
                vdos.writeInt(_stringPool.codeForString(refType.getName()));
                vdos.writeInt(_stringPool.codeForString(refType.getURI()));
                //System.out.println("         e: " + refType.getName() + " @ " + refType.getURI());
            }
        }

        private TypeXML readTypeRef(VersionedDataInputStream vdis)
            throws IOException
        {
            boolean internal = vdis.readBoolean();
            if (internal)
            {
                int baseTypeId = vdis.readInt();
                //System.out.println("        i: " + baseTypeId);
                return typeForCode(baseTypeId);
            }
            else
            {
                String baseTypeName = _stringPool.stringForCode(vdis.readInt());
                String baseTypeURI = _stringPool.stringForCode(vdis.readInt());
                //System.out.println("        e: " + baseTypeName + " @ " + baseTypeURI);

                //todo cezar This has to use the current context to get to its binding system                
                TypeXML type = SDOContextFactory.getGlobalSDOContext().getBindingSystem().loadTypeByTypeName(baseTypeURI, baseTypeName);

                if (type == null )
                    throw new IllegalArgumentException("Required referenced type not found in the current context: " +
                        baseTypeName + " @ " + baseTypeURI);

                return type;
            }
        }

        private void writePropertyXML(VersionedDataOutputStream vdos, PropertyXML prop)
            throws IOException
        {
            boolean nullProperty = prop==null;
            vdos.writeBoolean(nullProperty);
            //System.out.println("    writing Property: " + (nullProperty ? "null" : ""));

            if (nullProperty)
                return;

            //System.out.println("      name: " + prop.getName() + " in\t\t" + prop.getContainingTypeXML().getName() + " @ " + prop.getContainingTypeXML().getURI());

            TypeXML propType = prop.getTypeXML();
            writeTypeRef(vdos, propType);

            vdos.writeInt(_stringPool.codeForString(prop.getName()));
            vdos.writeBoolean(prop.isMany());
            vdos.writeBoolean(prop.isContainment());

            writeTypeRef(vdos, prop.getContainingTypeXML());
            vdos.writeBoolean(prop.isReadOnly());
            vdos.writeBoolean(prop.isNullable());

            vdos.writeInt(codeForProperty(prop.getOppositeXML()));

            List<String> aliasNames = (List<String>)prop.getAliasNames();
            vdos.writeInt(aliasNames.size());
            for (String aliasName : aliasNames)
            {
                vdos.writeInt(_stringPool.codeForString(aliasName));
            }

            // default value
            Object defaultValue = prop.getDefault();
            writeObjectAsByteArray(vdos, defaultValue);

            // PropertyXML extensions
            vdos.writeInt(_stringPool.codeForString(prop.getXMLName()));
            vdos.writeInt(_stringPool.codeForString(prop.getXMLNamespaceURI()));

            vdos.writeInt(prop.getSchemaTypeCode());
            vdos.writeBoolean(prop.isXMLElement());
            vdos.writeBoolean(prop.isDynamic());
            vdos.writeBoolean(prop.isGlobal());

            // write substitutions
            PropertyXML[] substs = prop.getAcceptedSubstitutions();
            if (substs==null)
                vdos.writeInt(-1);
            else
            {
                vdos.writeInt(substs.length);
                for (int i = 0; i < substs.length; i++)
                {
                    vdos.writeInt(codeForProperty(substs[i]));
                }
            }

            // metadata properties
            writeMetadataProperties(vdos, (MetadataHolder)prop);

            vdos.writeEmptyWildcard();
        }

        private void readPropertyXML(VersionedDataInputStream vdis, int index)
            throws IOException
        {
            //System.out.println("    reading Property: " + index);
            boolean nullProperty = vdis.readBoolean();
            //System.out.println("      null: " + nullProperty);
            if (nullProperty)
            {
                _properties.set(index, null);
                return;
            }

            PropertyImpl prop = (PropertyImpl)_properties.get(index);

            TypeXML propType = readTypeRef(vdis);

            String name = _stringPool.stringForCode(vdis.readInt());
            //System.out.println("      name: " + name);
            boolean isMany = vdis.readBoolean();
            boolean isContainment = vdis.readBoolean();

            TypeXML containingType = readTypeRef(vdis);
            //System.out.println("      containingType: " + containingType);
            boolean isReadOnly = vdis.readBoolean();
            boolean isNullable = vdis.readBoolean();

            PropertyXML opposite = propertyForCode(vdis.readInt());

            int size = vdis.readInt();
            //System.out.println("      alias names size: " + name);
            List<String> aliasNames = new ArrayList<String>(size);
            for (int i=0; i<size; i++)
            {
                aliasNames.add(_stringPool.stringForCode(vdis.readInt()));
            }

            // default value
            Object defaultValue = readObjectAsByteArray(vdis);

            // PropertyXML extensions
            String nameXML = _stringPool.stringForCode(vdis.readInt());
            //System.out.println("      xml name: " + nameXML);
            String uriXML = _stringPool.stringForCode(vdis.readInt());
            //System.out.println("      xml uri: " + uriXML);

            int schemaTypeCode = vdis.readInt();
            boolean isElement = vdis.readBoolean();
            boolean isDynamic = vdis.readBoolean();
            boolean isGlobal = vdis.readBoolean();

            // read substitutions
            int length = vdis.readInt();
            PropertyXML[] substs;
            if (length<0)
                substs = null;
            else
            {
                substs = new PropertyXML[length];
                for (int i = 0; i < length; i++)
                {
                    substs[i] = propertyForCode(vdis.readInt());
                }
            }

            // metadata properties
            List<PropertyXML> metadataProps = new ArrayList<PropertyXML>();
            List<Object> metadataValues = new ArrayList<Object>();
            readMetadataProperties(vdis, metadataProps, metadataValues);


            vdis.readSkipWildcard(Versions.CODE_VERSION_MINOR_V0);


            prop.initMutable(propType, name, isMany, isContainment, containingType, defaultValue, isReadOnly, isNullable,
                opposite, aliasNames, isGlobal, nameXML, uriXML, schemaTypeCode, isElement, isDynamic);
            prop.initSetAcceptedSubstitutions(substs);

            assert metadataProps.size()==metadataValues.size() : "Number of metadata properties and values should be the same.";
            for (int i = 0; i < metadataProps.size(); i++)
            {
                PropertyXML metadataProp = (PropertyXML) metadataProps.get(i);
                Object metadataValue = metadataValues.get(i);
                prop.addPropertyValue(metadataProp, metadataValue);
            }

            prop.makeImmutable();
        }

        private void writeQName(VersionedDataOutputStream dos, QName qname)
            throws IOException
        {
            boolean isNull = qname==null;
            dos.writeBoolean(isNull);
            if(!isNull)
            {
                dos.writeInt(_stringPool.codeForString(qname.getPrefix()));
                dos.writeInt(_stringPool.codeForString(qname.getLocalPart()));
                dos.writeInt(_stringPool.codeForString(qname.getNamespaceURI()));
            }
        }

        private QName readQName(VersionedDataInputStream vdis)
            throws IOException
        {
            boolean isNull = vdis.readBoolean();
            QName res = null;
            if(!isNull)
            {
                String prefix = _stringPool.stringForCode(vdis.readInt());
                String local = _stringPool.stringForCode(vdis.readInt());
                String uri = _stringPool.stringForCode(vdis.readInt());
                res = new QName(uri, local, prefix);
            }

            return res;
        }

        private void writeObjectAsByteArray(VersionedDataOutputStream vdos, Object value)
            throws IOException
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(value);
            oos.flush();

            byte[] ba = baos.toByteArray();
            vdos.writeInt(ba.length);
            vdos.write(ba, 0, ba.length);
        }

        private Object readObjectAsByteArray(VersionedDataInputStream vdis)
            throws IOException
        {
            int balength = vdis.readInt();
            byte[] ba = new byte[balength];

            int actualReadBytes = 0;
            do
            {
                actualReadBytes += vdis.read(ba, 0 + actualReadBytes, balength - actualReadBytes);
            }
            while(actualReadBytes<balength);

            ObjectInputStream ois = new ObjectInputStream( new ByteArrayInputStream(ba));
            try
            {
                return ois.readObject();
            }
            catch (ClassNotFoundException e)
            {
                throw new RuntimeException(e);
            }
        }

        private void writeMetadataProperties(VersionedDataOutputStream vdos, MetadataHolder holder)
            throws IOException
        {
            List<PropertyXML> metadataProps = (List<PropertyXML>)holder.getInstanceProperties();
            vdos.writeInt(metadataProps.size());
            //System.out.println("       metadataProps size: " + metadataProps.size());
            for (PropertyXML metadataProp : metadataProps)
            {
                assert metadataProp !=null;

                writePropertyRef(vdos, metadataProp);
                writeObjectAsByteArray(vdos, holder.get(metadataProp));
                //System.out.println("           metadatProp name: " + metadatProp.getName() + " value: " + type.get(metadataProp));
            }
        }

        private void readMetadataProperties(VersionedDataInputStream vdis, List<PropertyXML> props, List<Object> values)
            throws IOException
        {
            int size = vdis.readInt();
            //System.out.println("        metadataProps size: " + size);
            List<PropertyXML> metadataProps = new ArrayList<PropertyXML>(size);
            List<Object> metadataValues = new ArrayList<Object>(size);
            for (int i=0 ; i<size; i++)
            {
                PropertyXML metadataProp = readPropertyRef(vdis);
                Object metadataValue = readObjectAsByteArray(vdis);

                metadataProps.add(metadataProp);
                metadataValues.add(metadataValue);
                //System.out.println("          metadataPropId: " + propId + " value : " + metadataValue);
            }

            props.addAll(metadataProps);
            values.addAll(metadataValues);
        }

        private void writePropertyRef(VersionedDataOutputStream vdos, PropertyXML propRef)
            throws IOException
        {
            if ( propRef.getContainingTypeXML().getSDOTypeSystem()==_contextSDOTypeSystem)
            {
                vdos.writeInt(0);
                vdos.writeInt(codeForProperty(propRef));
            }
            else
            {
                if (propRef.isGlobal())
                {
                    vdos.writeInt(1);
                    vdos.writeInt(_stringPool.codeForString(propRef.getName()));
                    vdos.writeBoolean(propRef.isXMLElement());
                    vdos.writeInt(_stringPool.codeForString(propRef.getXMLName()));
                    vdos.writeInt(_stringPool.codeForString(propRef.getXMLNamespaceURI()));
                }
                else
                    throw new IllegalStateException("Property '" + propRef + "' is not in the same type system and is not global, it cannot be found at load time.");
            }
        }

        private PropertyXML readPropertyRef(VersionedDataInputStream vdis)
            throws IOException
        {
            PropertyXML propRef;
            int typeOfProperty = vdis.readInt();
            switch (typeOfProperty)
            {
                case 0:
                    int propId = vdis.readInt();
                    propRef = propertyForCode(propId);
                    assert propRef!=null;
                    break;
                case 1:
                    String mPropName = _stringPool.stringForCode(vdis.readInt());
                    boolean mPropIsXMLElement = vdis.readBoolean();
                    String mPropXMLName = _stringPool.stringForCode(vdis.readInt());
                    String mPropXMLUri = _stringPool.stringForCode(vdis.readInt());

                    if (mPropIsXMLElement)
                    {
                        //todo cezar This has to use the current context to get to its binding system
                        propRef = SDOContextFactory.getGlobalSDOContext().getBindingSystem().
                            loadGlobalPropertyByTopLevelElemQName(mPropXMLUri, mPropXMLName);
                    }
                    else
                    {
                        //todo cezar This has to use the current context to get to its binding system
                        propRef = SDOContextFactory.getGlobalSDOContext().getBindingSystem().
                            loadGlobalPropertyByTopLevelAttrQName(mPropXMLUri, mPropXMLName);
                    }

                    break;
                default:
                    throw new IllegalStateException("Unknown code for ref properties.");
            }

            return propRef;
        }

        public void prepareElemAndAttrQNames()
        {
            for (QName elemQName : _elemQNameToGlobalProp.keySet())
            {
                _stringPool.codeForString(elemQName.getPrefix());
                _stringPool.codeForString(elemQName.getLocalPart());
                _stringPool.codeForString(elemQName.getNamespaceURI());

                PropertyXML elemGlobalProp = _elemQNameToGlobalProp.get(elemQName);
                _stringPool.codeForString(elemGlobalProp.getContainingTypeXML().getURI());
                prepareRefType(elemGlobalProp.getContainingTypeXML());
                prepareProperty(elemGlobalProp);
            }
            for (QName attrQName : _attrQNameToGlobalProp.keySet())
            {
                _stringPool.codeForString(attrQName.getPrefix());
                _stringPool.codeForString(attrQName.getLocalPart());
                _stringPool.codeForString(attrQName.getNamespaceURI());

                PropertyXML attrGlobalProp = _attrQNameToGlobalProp.get(attrQName);
                _stringPool.codeForString(attrGlobalProp.getContainingTypeXML().getURI());
                prepareRefType(attrGlobalProp.getContainingTypeXML());
                prepareProperty(attrGlobalProp);
            }
        }

        // makeImmutable is called just before writing to chatch potential errors while still writing
        private void makeImmutable()
        {
            _immutable = true;
            _stringPool.makeImmutable();
        }
    }

    public static class StringPool
    {
        private List<String> intsToStrings = new ArrayList<String>();
        private Map<String, Integer> stringsToInts = new HashMap<String, Integer>();
        private boolean _immutable = false;

        /**
         * Constructs an empty StringPool to be filled with strings.
         */
        public StringPool()
        {
            intsToStrings.add(null);
        }

        public int codeForString(String str)
        {
            if (str == null)
                return 0;
            Integer result = (Integer)stringsToInts.get(str);
            if (result == null)
            {
                if (_immutable)
                    throw new IllegalStateException("Try to modify while immutable!");

                result = new Integer(intsToStrings.size());
                intsToStrings.add(str);
                stringsToInts.put(str, result);
            }
            return result.intValue();
        }

        public String stringForCode(int code)
        {
            if (code == 0)
                return null;
            return (String)intsToStrings.get(code);
        }

        public void writeTo(VersionedDataOutputStream vdos)
            throws IOException
        {
            if (intsToStrings.size() > Short.MAX_VALUE)
                throw new RuntimeException("Too many strings (" + intsToStrings.size() + ") ");

            vdos.writeShort(intsToStrings.size());
            Iterator i = intsToStrings.iterator();
            for (i.next(); i.hasNext(); )
            {
                String str = (String)i.next();
                vdos.writeUTF(str);
            }
        }

        public void readFrom(VersionedDataInputStream vdis)
            throws IOException
        {
            if (intsToStrings.size() != 1 || stringsToInts.size() != 0)
                throw new IllegalStateException();

            int size = vdis.readShort();
            for (int i = 1; i < size; i++)
            {
                String str = vdis.readUTF().intern();
                int code = codeForString(str);
                if (code != i)
                    throw new IllegalStateException();
            }
        }

        void makeImmutable()
        {
            _immutable = true;
        }
    }


//    private boolean ensureDirs(File nameFile)
//    {
//        File parent = nameFile.isDirectory() ? nameFile : nameFile.getParentFile();
//
//        if (parent!=null && parent.mkdirs())
//            return true;
//
//        if (parent!=null && parent.exists())
//            return true;
//
//        return false;
//    }

    public String computeId()
    {
        long hash = 0;
        int i = 0;

        for (QName elementName : _elemQNameToGlobalProperty.keySet())
        {
            i++;
            hash += 31*i + elementName.getLocalPart().hashCode();
            hash += 31*i + elementName.getNamespaceURI().hashCode();
            hash += 31*i + _elemQNameToGlobalProperty.get(elementName).hashCode();
        }
        for (QName attributeName : _attrQNameToGlobalProperty.keySet())
        {
            i++;
            hash += 31*i + attributeName.getLocalPart().hashCode();
            hash += 31*i + attributeName.getNamespaceURI().hashCode();
            hash += 31*i + _attrQNameToGlobalProperty.get(attributeName).hashCode();
        }

        for (QName typeName : _qnameToType.keySet())
        {
            i++;
            hash += 31*i + typeName.getLocalPart().hashCode();
            hash += 31*i + typeName.getNamespaceURI().hashCode();
            hash += 31*i + _qnameToType.get(typeName).hashCode();
        }

        for (QName schemaTypeName : _schemaNameToType.keySet())
        {
            i++;
            hash += 31*i + schemaTypeName.hashCode();
            TypeXML type = _schemaNameToType.get(schemaTypeName);
            hash += 31*i + (type==null ? 1 : type.hashCode());
        }

        hash += 31*i + (_schemaTypeLoader==null ? 3 : _schemaTypeLoader.hashCode());

        hash += Math.random() * 1000;

        String id = QNameHelper.hexsafe("" + (hash > 0 ? 1 : 0 ) + "" + Math.abs(hash) );
        //System.out.println("ID: " + id);

        return id;
    }

    private String getResourceNameForTSId(String sdoTSId)
    {
        return "metadata/id/" + sdoTSId + ".sdotsb";
    }

    private String getResourceNameForTSName(String sdoTypeSystemName)
    {
        return "metadata/name/" + sdoTypeSystemName + ".sdotsid";
    }

    private String getResourceNameForTypeQName(QName typeQName)
    {
        String hexSafeDir = hexsafedirWithDomain(typeQName, "sdotypes");
        return "metadata/uris/" + hexSafeDir + ".sdotsid";
    }

    private String getResourceNameForTopLevelElemQName(QName topLevelElemName)
    {
        String hexSafeDir = hexsafedirWithDomain(topLevelElemName, "xsdelems");
        return "metadata/uris/" + hexSafeDir + ".sdotsid";
    }

    private String getResourceNameForTopLevelAttrQName(QName topLevelAttrName)
    {
        String hexSafeDir = hexsafedirWithDomain(topLevelAttrName, "xsdattrs");
        return "metadata/uris/" + hexSafeDir + ".sdotsid";
    }

    private String getResourceNameForGlobalPropertiesSdoQName(QName globalPropertyQName)
    {
        String hexSafeDir = hexsafedirWithDomain(globalPropertyQName, "sdogprops");
        return "metadata/uris/" + hexSafeDir + ".sdotsid";
    }

    private String getResourceNameForSchemaTypeQName(QName schemaTypeQName)
    {
        String hexSafeDir = hexsafedirWithDomain(schemaTypeQName, "xsdtypes");
        return "metadata/uris/" + hexSafeDir + ".sdotsid";
    }

    public static String hexsafedirWithDomain(QName name, String domain)
    {
        String hexUri;
        if (name.getNamespaceURI() == null || name.getNamespaceURI().length() == 0)
            hexUri = "_nons";
        else
            hexUri = QNameHelper.hexsafe(name.getNamespaceURI());
        return hexUri + "/" + domain + "/" + QNameHelper.hexsafe(name.getLocalPart());
    }
    // end persistance

    public void dump()
    {
        dump(false);
    }

    public void dump(boolean excludeBuiltinTypes)
    {
        System.out.println("TypeSystem: " + this.getClass().getName());

        SortedSet<QName> sortedKeys = new TreeSet<QName>(new Comparator<QName>(){
            public int compare(QName o1, QName o2)
            {
                if ( o1.getLocalPart().equals(o2.getLocalPart()))
                    return o1.getNamespaceURI().compareTo(o2.getNamespaceURI());
                return o1.getLocalPart().compareTo(o2.getLocalPart());
            }
        });

        System.out.println("  Types: ");
        sortedKeys.addAll(_qnameToType.keySet());
        for ( QName q : sortedKeys )
        {
            if (excludeBuiltinTypes && _qnameToType.get(q).isBuiltinType())
                continue;

            System.out.println("    " + q.getLocalPart() + " @ " + q.getNamespaceURI());
        }

        System.out.println("  SchemaName -> Type: ");
        sortedKeys.clear();
        sortedKeys.addAll(_schemaNameToType.keySet());
        for ( QName q : sortedKeys )
        {
            if (excludeBuiltinTypes && _schemaNameToType.get(q).isBuiltinType())
                continue;

            System.out.println("    " + q.getLocalPart() + " @ " + q.getNamespaceURI() + " -> " + _schemaNameToType.get(q));
        }

        System.out.println("  Top level element name -> GlobalProperty: ");
        sortedKeys.clear();
        sortedKeys.addAll(_elemQNameToGlobalProperty.keySet());
        for ( QName q : sortedKeys )
        {
            System.out.println("    " + q.getLocalPart() + " @ " + q.getNamespaceURI() + " -> " + _elemQNameToGlobalProperty.get(q));
        }

        System.out.println("  Top level attribute name -> GlobalProperty: ");
        sortedKeys.clear();
        sortedKeys.addAll(_attrQNameToGlobalProperty.keySet());
        for ( QName q : sortedKeys )
        {
            System.out.println("    " + q.getLocalPart() + " @ " + q.getNamespaceURI() + " -> " + _attrQNameToGlobalProperty.get(q));
        }
    }

    public void dumpWithoutBuiltinTypes()
    {
        dump(true);
    }
}
