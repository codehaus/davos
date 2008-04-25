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
package davos.sdo.impl.data;

import davos.sdo.DataObjectXML;
import davos.sdo.PropertyXML;
import davos.sdo.SDOContext;
import davos.sdo.TypeXML;
import davos.sdo.binding.BindingSystem;
import davos.sdo.impl.common.Common;
import davos.sdo.impl.type.BuiltInTypeSystem;
import davos.sdo.impl.type.PropertyImpl;
import davos.sdo.impl.type.TypeImpl;
import davos.sdo.impl.type.TypeSystemBase;
import davos.sdo.type.TypeSystem;
import javax.sdo.DataGraph;
import javax.sdo.DataObject;
import javax.sdo.Type;
import javax.sdo.helper.DataFactory;
import org.apache.xmlbeans.SchemaType;


/**
 * @author Cezar Andrei (cezar dot andrei at gmail dot com)
 *         Date: Jan 13, 2006
 */
public class DataFactoryImpl
    implements DataFactory
{
    private SDOContext _sdoContext;
    private BindingSystem _bindingSystem;

    public DataFactoryImpl(SDOContext sdoContext)
    {
        _sdoContext = sdoContext;
        _bindingSystem = _sdoContext.getBindingSystem();
    }

    // DataFactory methods

    // create methods
    public DataObject create(String uri, String typeName)
    {
        Type type = _bindingSystem.loadTypeByTypeName(uri, typeName);

        if (type==null)
        {
            throw new IllegalArgumentException("Type not found in the current context: " + typeName + "@" + uri);
        }

        return create(type);
    }

    public DataObject create(Class interfaceClass)
    {
        TypeXML type = _bindingSystem.getType(interfaceClass);
        if ( type==null )
            throw new IllegalArgumentException("SDO type not availble in the current context for class " + interfaceClass + " .");

        return create(type);
    }

    public DataObject create(Type type)
    {
        assert type!=null;

        if (_sdoContext.getTypeSystem().getTypeXML(type.getURI(), type.getName())!=type)
            throw new IllegalArgumentException("Type '" + type + "' not from this context: " + _sdoContext);

        if (type.isAbstract())
            throw new IllegalArgumentException("The type " + type.getName() + "@" + type.getURI() + " is an abstract type and it cannot be instantiated.");

        if (type.isDataType())
            throw new IllegalArgumentException("The type " + type.getName() + "@" + type.getURI() + " is a DataType, it cannot be used to create a DataObject.");

        DataGraphImpl dataGraph = null;
        if (type == BuiltInTypeSystem.DATAGRAPHTYPE)
            dataGraph = new DataGraphImpl(_sdoContext);
        DataObject root = create(_sdoContext.getTypeSystem().getTypeXML(type), dataGraph);
        if (dataGraph != null)
            dataGraph.setRootObject(root);
        return root;
    }

    DataObjectXML create(String uri, String typeName, DataGraph datagraph)
    {
        TypeXML type = _bindingSystem.loadTypeByTypeName(uri, typeName);
        return create(type, datagraph);
    }

    DataObjectXML create(TypeXML type, DataGraph dataGraph)
    {
        return _bindingSystem.createDataObjectForType(type, dataGraph);
    }

    // root related methods
    public TypeXML getDocumentRootType(String uri)
    {
        TypeSystem sdoTypeSystem = _sdoContext.getTypeSystem();

        TypeXML docType = sdoTypeSystem.getTypeXML(uri, "DocumentRoot");

        if (docType!=null)
            return docType;


        TypeImpl docTypeImpl = TypeImpl.create();

        docTypeImpl.init("DocumentRoot", uri, BuiltInTypeSystem.TYPECODE_USERDEFINED,
            DataObjectGeneral.class, false, true, true, false, false, true, Common.EMPTY_TYPEXML_LIST,
            Common.EMPTY_PROPERTYXML_LIST, Common.EMPTY_STRING_LIST,
            null, sdoTypeSystem);

        docTypeImpl.makeImmutable();

        synchronized (sdoTypeSystem)
        {
            docType = sdoTypeSystem.getTypeXML(uri, "DocumentRoot");

            if (docType!=null)
                return docType;

            ((TypeSystemBase)sdoTypeSystem).addTypeMapping(docTypeImpl);

            return docTypeImpl;
        }
    }

    public DataObject getDocumentRoot(String uri)
    {
        Type docType = getDocumentRootType(uri);

        return create(docType);
    }

    public PropertyXML getRootProperty(String uri, String localName, TypeXML propType, boolean elem)
    {
        TypeXML docType = getDocumentRootType(uri);
        PropertyXML prop = docType.getPropertyXML(localName);
        if (prop != null)
            return prop;

        if (propType==null)
            propType = BuiltInTypeSystem.DATAOBJECT;

        return PropertyImpl.create(propType, localName, elem, !propType.isDataType(), docType,
            null, false, true, null, Common.EMPTY_STRING_LIST, localName, uri, /*anyType*/1, elem,
            true, false);
    }

    public PropertyXML getValueProperty(TypeXML propType)
    {
        return PropertyImpl.create(propType, davos.sdo.impl.common.Names.SIMPLE_CONTENT_PROP_NAME,
            false, false, null, null, false, true, null, Common.EMPTY_STRING_LIST, true, false);
    }

    public TypeXML getTypeByName(String uri, String typeName)
    {
        return _bindingSystem.loadTypeByTypeName(uri, typeName);
    }

    public PropertyXML getGlobalElementByTopLevelElementName(String uri, String elemName)
    {
        return _bindingSystem.loadGlobalPropertyByTopLevelElemQName(uri, elemName);
    }

    //create child methods
    public DataObject createChild(DataObjectImpl parent, String propertyName)
    {
        PropertyXML prop = parent.getTypeXML().getPropertyXML(propertyName);

        if (prop==null && !parent.getTypeXML().isOpen())
            throw new IllegalStateException("Trying to add a new property to a non open type: " + propertyName + " on type: " + parent.getTypeXML());

        if (prop==null)
        {
            prop = PropertyImpl.create(BuiltInTypeSystem.DATAOBJECT, propertyName, false, true,
                parent.getTypeXML(), null, false, true, null, Common.EMPTY_STRING_LIST,
                propertyName, null, SchemaType.BTC_NOT_BUILTIN /* BEADATAOBJECT has schemaType==null*/, true, true, false);
        }
        
        return createChild(parent, prop);
    }

    public DataObject createChild(DataObjectImpl parent, String propertyName, TypeXML derivedPropType)
    {
        PropertyXML prop = parent.getTypeXML().getPropertyXML(propertyName);

        if (prop==null && !parent.getTypeXML().isOpen())
            throw new IllegalStateException("Trying to add a new property to a non open type: " + propertyName + " on type: " + parent.getTypeXML());

        if (prop==null)
        {
//            prop = PropertyImpl.create(derivedPropType, propertyName, false, true,
//                parent.getTypeXML(), null, false, true, null, Common.EMPTY_STRING_LIST,
//                propertyName, null, SchemaType.BTC_NOT_BUILTIN /* BEADATAOBJECT has schemaType==null*/, true, true, false);
            prop = PropertyImpl.createOnDemand( propertyName, true, !derivedPropType.isDataType(), derivedPropType);
        }
        
        return createChild(parent, prop, derivedPropType);
    }

    public DataObject createChild(DataObjectImpl parent, int propertyIndex, TypeXML propType)
    {
        PropertyXML prop = (PropertyXML)parent.getType().getProperties().get(propertyIndex);
        return createChild(parent, prop, propType);
    }

    public DataObject createChild(DataObjectImpl parent, PropertyXML prop)
    {
        return createChild(parent, prop, prop.getTypeXML());
    }

    public DataObject createChild(DataObjectImpl parent, PropertyXML prop, TypeXML propertyType)
    {
        if (!prop.getTypeXML().isAssignableFrom(propertyType))
            throw new IllegalArgumentException("Type '" + propertyType + "' cannot assiged to '" + prop.getTypeXML() +"'");

        return _bindingSystem.createChildForDataObject(parent, prop, propertyType);
    }
}
