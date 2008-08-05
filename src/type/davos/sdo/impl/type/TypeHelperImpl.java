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

import davos.sdo.DataObjectXML;
import davos.sdo.PropertyXML;
import davos.sdo.SDOContext;
import davos.sdo.TypeXML;
import davos.sdo.impl.common.Common;
import davos.sdo.impl.common.Names;
import davos.sdo.type.TypeSystem;
import javax.sdo.DataObject;
import javax.sdo.Property;
import javax.sdo.Type;
import javax.sdo.helper.TypeHelper;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Cezar Andrei (cezar dot andrei at gmail dot com)
 *         Date: Jan 13, 2006
 */
public class TypeHelperImpl
    implements TypeHelper
{
    private SDOContext _sdoContext;

    public TypeHelperImpl(SDOContext sdoContext)
    {
        _sdoContext = sdoContext;
    }


    public Type getType(String uri, String typeName)
    {
        return _sdoContext.getBindingSystem().loadTypeByTypeName(uri, typeName);
    }

    public Type getType(Class interfaceClass)
    {
        return _sdoContext.getBindingSystem().getType(interfaceClass);
    }

    public Type define(DataObject obj)
    {
        assert obj !=null;

        List<DataObject> objList = new ArrayList<DataObject>();
        objList.add(obj);

        return (Type)define(objList).get(0);
    }

    public List /*Type*/ define(List /*DataObject*/ types)
    {
        List<DataObject> typesToBeDefined = new ArrayList<DataObject>();
        List<Type> typesDefined = new ArrayList<Type>();

        typesToBeDefined.addAll((List<DataObject>)types);

        Map<QName, TypeImpl> namesToTypes = new HashMap<QName, TypeImpl>();
        Map<PropertyKey, PropertyImpl> propertyKeysToProperties = new HashMap<PropertyKey, PropertyImpl>();

        for(DataObject dobj : typesToBeDefined)
        {
            if (dobj==null)
                continue;

            String typeName = dobj.getString(BuiltInTypeSystem.P_TYPE_NAME);
            String typeUri = dobj.getString(BuiltInTypeSystem.P_TYPE_URI);

            if (typeName ==null)
                throw new IllegalArgumentException("Cannot define types without a name.");

            QName qname = new QName(typeUri,typeName);

            if (namesToTypes.get(qname)!=null)
                throw new IllegalArgumentException("There are more the one definitions for types with name '" + typeName + " @ " + typeUri + "'");

            namesToTypes.put(qname, TypeImpl.create());
        }

        TypeSystem typeSystem = _sdoContext.getTypeSystem();

        synchronized (typeSystem)
        {

            while(typesToBeDefined.size()>0)
            {
                DataObject obj = typesToBeDefined.remove(0);

                String typeName = obj.getString(BuiltInTypeSystem.P_TYPE_NAME);
                String typeUri = obj.getString(BuiltInTypeSystem.P_TYPE_URI);

                //System.out.println("Defining type: " + typeName);

                TypeImpl type = namesToTypes.get(new QName(typeUri, typeName));

                List<PropertyXML> declaredProps = new ArrayList<PropertyXML>();
                type.init(obj.getString(BuiltInTypeSystem.P_TYPE_NAME),
                    obj.getString(BuiltInTypeSystem.P_TYPE_URI),
                    BuiltInTypeSystem.TYPECODE_USERDEFINED,
                    getJavaClassFromDefinition(obj),
                    obj.getBoolean(BuiltInTypeSystem.P_TYPE_DATATYPE),
                    obj.getBoolean(BuiltInTypeSystem.P_TYPE_OPEN),
                    obj.getBoolean(BuiltInTypeSystem.P_TYPE_SEQUENCED),
                    obj.getBoolean(BuiltInTypeSystem.P_TYPE_ABSTRACT),
                    false,
                    obj.getBoolean(BuiltInTypeSystem.P_TYPE_SEQUENCED),
                    (List<TypeXML>)obj.getList(BuiltInTypeSystem.P_TYPE_BASETYPE),
                    declaredProps,
                    (List<String>)obj.getList(BuiltInTypeSystem.P_TYPE_ALIASNAME),
                    null,
                    typeSystem);

                List<DataObjectXML> props = (List<DataObjectXML>)obj.getList(BuiltInTypeSystem.P_TYPE_PROPERTY);
                for (DataObject p : props)
                {
                    defineProperty(p, namesToTypes, typesToBeDefined, propertyKeysToProperties, typeName, typeUri, type, declaredProps, false);
                }

                // add metadata property values
                List typeProps = obj.getType().getInstanceProperties();
                List instProps = obj.getInstanceProperties();
                if (instProps.size() > typeProps.size())
                {
                    for ( int i = typeProps.size() ; i<instProps.size() ; i++)
                    {
                        Property instProp = (Property)instProps.get(i);
                        if (instProp == BuiltInTypeSystem.P_TYPE_ABSTRACT ||
                            instProp == BuiltInTypeSystem.P_TYPE_ALIASNAME ||
                            instProp == BuiltInTypeSystem.P_TYPE_BASETYPE ||
                            instProp == BuiltInTypeSystem.P_TYPE_DATATYPE ||
                            instProp == BuiltInTypeSystem.P_TYPE_NAME ||
                            instProp == BuiltInTypeSystem.P_TYPE_OPEN ||
                            instProp == BuiltInTypeSystem.P_TYPE_PROPERTY ||
                            instProp == BuiltInTypeSystem.P_TYPE_SEQUENCED ||
                            instProp == BuiltInTypeSystem.P_TYPE_URI )
                            continue;

                        type.addPropertyValue(instProp, obj.get(instProp));
                    }
                }

                type.makeImmutable();

                checkConstraints(type);

                TypeXML existingType = typeSystem.getTypeXML(typeUri, typeName);
                if (existingType==null)
                {
                    ((TypeSystemBase)typeSystem).addTypeMapping(type);

                    typesDefined.add(type);
                }
                else
                    typesDefined.add(existingType);
            }
        }
        
        return typesDefined;
    }

    private void defineProperty(DataObject p, Map<QName, TypeImpl> namesToTypes, List<DataObject> typesToBeDefined,
        Map<PropertyKey, PropertyImpl> propertyKeysToProperties, String containingTypeName, String containingTypeUri,
        TypeImpl containingType, List<PropertyXML> declaredProps, boolean isGlobal)
    {
        //System.out.print("  adding prop: " + p.getString("name"));
        Object propTypeValue = p.get(BuiltInTypeSystem.P_PROPERTY_TYPE);

        TypeXML propType;
        String propTypeName;
        String propTypeUri;
        boolean propTypeIsDatatype;

        if (propTypeValue instanceof Type)
        {
            // this is the actual property containingType
            propType = _sdoContext.getTypeSystem().getTypeXML((Type)propTypeValue);
            propTypeName = propType.getName();
            propTypeUri = propType.getURI();
            propTypeIsDatatype = propType.isDataType();
        }
        else if (propTypeValue instanceof DataObject)
        {
            // this is the definition of the property containingType
            DataObject propTypeDefinition = (DataObject)propTypeValue;
            propTypeName = propTypeDefinition.getString(BuiltInTypeSystem.P_TYPE_NAME);
            propTypeUri = propTypeDefinition.getString(BuiltInTypeSystem.P_TYPE_URI);
            propTypeIsDatatype = propTypeDefinition.getBoolean(BuiltInTypeSystem.P_TYPE_DATATYPE);

            if (propTypeName ==null)
                throw new IllegalArgumentException("Cannot define types without a name.");

            QName propTypeDefinitionQname = new QName(propTypeUri, propTypeName);

            TypeImpl propTypeImpl = namesToTypes.get(propTypeDefinitionQname);
            if ( propTypeImpl == null )
            {
                propTypeImpl = TypeImpl.create();
                namesToTypes.put(propTypeDefinitionQname, propTypeImpl);
                typesToBeDefined.add(propTypeDefinition);
            }

            propType = propTypeImpl;
        }
        else
            throw new IllegalArgumentException("The value of 'containingType' property on 'Property@javax.sdo' named '"
                + p.getString("name") + "' must be instanceof Type or DataObject.");

        String propName = p.getString(BuiltInTypeSystem.P_PROPERTY_NAME);
        boolean propMany = p.getBoolean(BuiltInTypeSystem.P_PROPERTY_MANY);
        boolean propContainment = p.getBoolean(BuiltInTypeSystem.P_PROPERTY_CONTAINMENT);
        if (propTypeIsDatatype)
            propContainment = false;  // force containment false if type is dataType

        Object propDefault = p.get(BuiltInTypeSystem.P_PROPERTY_DEFAULT);
        boolean propReadOnly = p.getBoolean(BuiltInTypeSystem.P_PROPERTY_READONLY);
        boolean propNullable = p.getBoolean(BuiltInTypeSystem.P_PROPERTY_NULLABLE);
        List<String> propAliasNames = (List<String>)p.getList(BuiltInTypeSystem.P_PROPERTY_ALIASNAME);

        Object oppositeValue = p.get(BuiltInTypeSystem.P_PROPERTY_OPPOSITE);
        PropertyXML propOpposite = null;

        if (oppositeValue instanceof Property)
            propOpposite = PropertyImpl.getPropertyXML((Property)oppositeValue);
        else if (oppositeValue instanceof DataObject)
        {
            DataObject oppoPropDef = (DataObject)oppositeValue;

            PropertyKey oppoPropKey = getPropertyKey(oppoPropDef);
            propOpposite = propertyKeysToProperties.get(oppoPropKey);
            if (propOpposite ==null)
            {
                PropertyImpl newEmptyProperty = PropertyImpl.create();
                propertyKeysToProperties.put(oppoPropKey, newEmptyProperty);
                propOpposite = newEmptyProperty;
            }
        }
        else if (oppositeValue!=null)
            throw new IllegalArgumentException("The value of opposite property for '" + propName + "' must be an instanceof Property or DataObject.");


        PropertyKey propKey = new PropertyKey(containingTypeName, containingTypeUri, propName, propTypeName, propTypeUri, propMany, propContainment, propReadOnly);
        PropertyImpl declaredProp = propertyKeysToProperties.get(propKey);

        if(declaredProp==null)
        {
            declaredProp = PropertyImpl.create();
            propertyKeysToProperties.put(propKey, declaredProp);
        }

        boolean isDynamic = isGlobal;
        boolean isIndeedGlobal = isGlobal && containingTypeUri != null;

        declaredProp.initMutable( propType, propName, propMany, propContainment, containingType,
            propDefault, propReadOnly, propNullable, propOpposite, propAliasNames, isIndeedGlobal, isDynamic);

        // add metadata property values
        List typeProps = p.getType().getInstanceProperties();
        List instProps = p.getInstanceProperties();
        if (instProps.size() > typeProps.size())
        {
            for ( int i = typeProps.size() ; i<instProps.size() ; i++)
            {
                Property instProp = (Property)instProps.get(i);
                if (instProp == BuiltInTypeSystem.P_PROPERTY_ALIASNAME ||
                    instProp == BuiltInTypeSystem.P_PROPERTY_CONTAINMENT ||
                    instProp == BuiltInTypeSystem.P_PROPERTY_DEFAULT ||
                    instProp == BuiltInTypeSystem.P_PROPERTY_MANY ||
                    instProp == BuiltInTypeSystem.P_PROPERTY_NAME ||
                    instProp == BuiltInTypeSystem.P_PROPERTY_OPPOSITE ||
                    instProp == BuiltInTypeSystem.P_PROPERTY_READONLY ||
                    instProp == BuiltInTypeSystem.P_PROPERTY_NULLABLE ||
                    instProp == BuiltInTypeSystem.P_PROPERTY_TYPE )
                    continue;

                declaredProp.addPropertyValue(instProp, p.get(instProp));

                if (instProp == BuiltInTypeSystem.P_PROPERTY_XMLELEMENT)
                    declaredProp.setXMLElement(p.getBoolean(instProp));
//                {
//                    boolean isElement = false;
//                    List l = p.getList(instProp);
//                    Object value;
//                    if ( l!=null && l.size()==1 && (value=l.get(0)) instanceof Boolean )
//                        isElement = ((Boolean)value).booleanValue();
//                    declaredProp.setXMLElement(isElement);
//                }
            }
        }

        declaredProp.makeImmutable();
        
        declaredProps.add(declaredProp);
        //System.out.println("\t   " + (propContainment ? "" : "REF") + "  of containingType: " + propTypeName + " @ " + propTypeUri + (propOpposite != null ? " \t Oppo: " + propOpposite : ""));
    }

    private static class PropertyKey
    {
        String _containingTypeName;
        String _containingTypeUri;
        String _propertyName;
        String _propertyTypeName;
        String _propertyTypeUri;
        Boolean _isMany;
        Boolean _isContainment;
        Boolean _isReadOnly;

        PropertyKey(String containingTypeName, String containingTypeUri, String propertyName, String propertyTypeName,
            String propertyTypeUri, Boolean isMany, Boolean isContainment, Boolean isReadOnly)
        {
            _containingTypeName = containingTypeName;
            _containingTypeUri = containingTypeUri;
            _propertyName = propertyName;
            _propertyTypeName = propertyTypeName;
            _propertyTypeUri = propertyTypeUri;
            _isMany = isMany;
            _isContainment = isContainment;
            _isReadOnly = isReadOnly;
        }

        public int hashCode()
        {
            return 31 * (_containingTypeName==null ? 1 : _containingTypeName.hashCode()) +
                31 * (_containingTypeUri==null ? 2 : _containingTypeUri.hashCode()) +
                31 * _propertyName.hashCode() + 31*_propertyTypeName.hashCode() +
                31 * (_propertyTypeUri == null ? 3 : _propertyTypeUri.hashCode()) +
                31 * (_isMany ? 3 : 5) +
                31 * (_isContainment ? 7 : 11) +
                31 * (_isReadOnly ? 13 : 17);
        }

        public boolean equals(Object o)
        {
            if (o==this)
                return true;

            if (o==null)
                return false;

            if (!(o instanceof PropertyKey))
                return false;

            PropertyKey pk = (PropertyKey)o;

            return _containingTypeName.equals(pk._containingTypeName) &&
                _containingTypeUri.equals(pk._containingTypeUri) &&
                _propertyName.equals(pk._propertyName) &&
                _propertyTypeName.equals(pk._propertyTypeName) &&
                _propertyTypeUri.equals(pk._propertyTypeUri) &&
                _isMany == pk._isMany && _isContainment == pk._isContainment && _isReadOnly == pk._isReadOnly;
        }

        public String toString()
        {
            return "PropertyKey: " + _containingTypeName + " " + _containingTypeUri + " " + _propertyName + " " +
                _propertyTypeName + " " + _propertyTypeUri + " " + _isMany + " " + _isContainment + " " + _isReadOnly;
        }
    }

    private static PropertyKey getPropertyKey(DataObject propDefinition)
    {
        String propContTypeName = propDefinition.getContainer().getString(BuiltInTypeSystem.P_TYPE_NAME);
        String propContTypeUri = propDefinition.getContainer().getString(BuiltInTypeSystem.P_TYPE_URI);

        String propName = propDefinition.getString(BuiltInTypeSystem.P_PROPERTY_NAME);

        Object propTypeValue = propDefinition.get(BuiltInTypeSystem.P_PROPERTY_TYPE);

        String propTypeName;
        String propTypeUri;

        if (propTypeValue instanceof Type)
        {
            // this is the actual property type
            Type propType = (Type)propTypeValue;
            propTypeName = propType.getName();
            propTypeUri = propType.getURI();
        }
        else
        {
            if (propTypeValue instanceof DataObject)
            {
                // this is the definition of the property type
                DataObject propTypeDefinition = (DataObject)propTypeValue;
                propTypeName = propTypeDefinition.getString(BuiltInTypeSystem.P_TYPE_NAME);
                propTypeUri = propTypeDefinition.getString(BuiltInTypeSystem.P_TYPE_URI);

                if (propTypeName ==null)
                    throw new IllegalArgumentException("Cannot define types without a name.");
            }
            else
                throw new IllegalArgumentException("The value of 'type' property on 'Property@javax.sdo' named '"
                    + propDefinition.getString("name") + "' must be instanceof Type or DataObject.");
        }

        boolean propMany = propDefinition.getBoolean(BuiltInTypeSystem.P_PROPERTY_MANY);
        boolean propContainment = propDefinition.getBoolean(BuiltInTypeSystem.P_PROPERTY_CONTAINMENT);
        boolean propReadonly = propDefinition.getBoolean(BuiltInTypeSystem.P_PROPERTY_READONLY);

        PropertyKey oppoPropKey = new PropertyKey(propContTypeName, propContTypeUri, propName, propTypeName, propTypeUri, propMany, propContainment, propReadonly);
        return oppoPropKey;
    }

    private void checkConstraints(Type type)
    {
/* todo

SDO Type and Property constraints

There are several restrictions on SDO Types and Properties. These restrictions ensure Types and
Properties for DataObjects are consistent with their API behavior. Behavior of
ChangeSummaryType Properties is defined.
- Instances of Types with dataType=false must implement the DataObject interface and
have isInstance(DataObject) return true.
- If a Type's instance Class is not null, isInstance(DataObject) can only be true when
instanceClass.isInstance(DataObject) is true.
- Values of bidirectional Properties with type.dataType=false and many=true must be
unique objects within the same list.
- Values of Properties with type.dataType=false and many=true cannot contain null.
- Property.containment has no effect unless type.dataType=false
- Property.default!=null requires type.dataType=true and many=false
- Types with dataType=true cannot contain properties, and must have open and
sequenced=false.
- Type.dataType and sequenced must have the same value as their base Types' dataType
and sequenced.
- Type.open may only be false when the base Types' open are also false.
- Instance classes in Java must mirror the extension relationship of the base Types.
- Properties that are bi-directional require type.dataType=false
- Properties that are bi-directional require that no more than one end has containment=true
- Properties that are bi-directional require that both ends have the same value for readOnly
- Properties that are bi-directional with containment require that the non-contaiment
Property has many=false.
- Names and aliasNames must all be unique within Type.getProperties()
ChangeSummaryType Properties:
- Types may contain one property with type ChangeSummaryType.
- A property with type ChangeSummaryType must have many=false and readOnly=true.
- The scope of ChangeSummaries may never overlap. The scope of a ChangeSummary for a
DataGraph is all the DataObjects in the DataGraph. If a DataObject has a property of type
ChangeSummary, the scope of its ChangeSummary is that DataObject and all contained
DataObjects. If a DataObject has a property of type ChangeSummay, it cannot contain any
other DataObjects that have a property of type ChangeSummay and it cannot be within a
DataGraph.
- ChangeSummaries collect changes for only the DataObjects within their scope.
- The scope is the same whether logging is on or off.
- Serialization of a DataObjects with a property of type ChangeSummaryType follows the
normal rules for serializing a ChangeSummary.
*/
    List props = type.getDeclaredProperties();
    for (int j = 0; j < props.size(); j++)
    {
        Property property = (Property) props.get(j);
        Property opposite = property.getOpposite();
        if (opposite!=null)
        {
            // Properties that are bi-directional require type.dataType=false
            if ( property.getType()!=null && property.getType().isDataType() )
                throw new IllegalArgumentException("Invalid constraint: Type " + property.getType()  + " of bidirectional property " + property + " is datatype.");
            if ( opposite.getType()!=null && opposite.getType().isDataType() )
                throw new IllegalArgumentException("Invalid constraint: Type " + opposite.getType()  + " of bidirectional property " + opposite + " is datatype.");

            // Properties that are bi-directional require that no more than one end has containment=true
            if ( property.isContainment() && opposite.isContainment() )
                throw new IllegalArgumentException("Invalid constraint: Only one of the two bidirectional porperties can be containment: " + property + " and " + opposite + ".");

            // Properties that are bi-directional require that both ends have the same value for readOnly
            if ( property.isReadOnly() != opposite.isReadOnly() )
                throw new IllegalArgumentException("Invalid constraint: The two bidirectional porperties should have the same readOnly bit " + property + " and " + opposite + ".");

            // Properties that are bi-directional with containment require that the non-contaiment Property has many=false.
            if ( property.isContainment() && opposite.isMany() )
                throw new IllegalArgumentException("Invalid constraint: bi-directional with containment require that the non-contaiment Property has many=false. Property with many=true: " + opposite + ".");
            if ( opposite.isContainment() && property.isMany() )
                throw new IllegalArgumentException("Invalid constraint: bi-directional with containment require that the non-contaiment Property has many=false. Property with many=true: " + property + ".");

//            // Properties that are bi-directional require that they are not nullable.
//            // todo cezar check for nullable opposite props
//            if ( property.isNullable() )
//                throw new IllegalArgumentException("Invalid constraint: bi-directional require nullable=false. Property with nullable=true: " + property + ".");
//            if ( opposite.isNullable() )
//                throw new IllegalArgumentException("Invalid constraint: bi-directional require nullable=false. Property with nullable=true: " + opposite + ".");
        }
    }
}

    public Property getOpenContentProperty(String uri, String propertyName)
    {
        // todo cezar This might be a bug, in the case when the property name is not the same as the XML name

        // first try to find them in both element and attribute sets
        Property ocProp = _sdoContext.getTypeSystem().getGlobalPropertyByTopLevelElemQName(uri, propertyName);
        if ( ocProp==null )
            ocProp = _sdoContext.getTypeSystem().getGlobalPropertyByTopLevelAttrQName(uri, propertyName);

        // not found try to load them
        if ( ocProp==null )
            ocProp = _sdoContext.getBindingSystem().loadGlobalPropertyByTopLevelElemQName(uri, propertyName);
        if ( ocProp == null )
            ocProp = _sdoContext.getBindingSystem().loadGlobalPropertyByTopLevelAttrQName(uri, propertyName);

        return ocProp;
    }

    public Property defineOpenContentProperty(String uri, DataObject propertyDef)
    {
        List<DataObject> typesToBeDefined = new ArrayList<DataObject>();
        Map<QName, TypeImpl> namesToTypes = new HashMap<QName, TypeImpl>();
        Map<PropertyKey, PropertyImpl> propertyKeysToProperties = new HashMap<PropertyKey, PropertyImpl>();
        List<PropertyXML> declaredProps = new ArrayList<PropertyXML>();

        String propName = propertyDef.getString(BuiltInTypeSystem.P_PROPERTY_NAME);
        if (propName==null)
            throw new IllegalArgumentException("When defining a new property it has to have a name.");

        List<PropertyXML> ctDeclProps = new ArrayList<PropertyXML>();
        TypeImpl containingType = null;
        String containingTypeName = null;
        if ( uri!=null )
        {
            containingTypeName = Names.NAME_OF_CONTAINING_TYPE_FOR_GLOBAL_PROPERTIES;

            containingType = TypeImpl.create();
            containingType.init(containingTypeName, uri, BuiltInTypeSystem.TYPECODE_USERDEFINED, DataObject.class,
                false, false, false, false, false, false, Common.EMPTY_TYPEXML_LIST, ctDeclProps,
                Common.EMPTY_STRING_LIST, null, _sdoContext.getTypeSystem());
        }

        defineProperty(propertyDef, namesToTypes, typesToBeDefined, propertyKeysToProperties,
            containingTypeName, uri, containingType, declaredProps, true);

        define(typesToBeDefined);

        PropertyXML result = declaredProps.get(0);

        ctDeclProps.add(result);
        if ( containingType !=null )
            containingType.makeImmutable();

        if ( uri != null )
        {
            // check to see if a property with this name already exists
            PropertyXML existingProp =
                _sdoContext.getBindingSystem().loadGlobalPropertyByTopLevelElemQName(uri, propName);
            if (existingProp==null )
                existingProp =
                    _sdoContext.getBindingSystem().loadGlobalPropertyByTopLevelAttrQName(uri, propName);

            if ( existingProp==null )
                ((TypeSystemBase)_sdoContext.getTypeSystem()).addGlobalProperty(result);
            else
            {
                if ( !existingProp.getType().equals(result.getType()) )
                    throw new IllegalArgumentException("Redefinition o property '" + result + "' with a different type is not allwed.");

                result = existingProp;
            }
        }

        return result;
    }

    private Class getJavaClassFromDefinition(DataObject typeDefinition)
    {
        if (!typeDefinition.getBoolean(BuiltInTypeSystem.P_TYPE_DATATYPE))
            return null;

        String javaClass;
        if (typeDefinition.isSet(BuiltInTypeSystem.P_TYPE_JAVACLASS))
        {
            javaClass = typeDefinition.getString(BuiltInTypeSystem.P_TYPE_JAVACLASS);
            return getJavaClassFromString(javaClass);
        }
        else
        {
            if (typeDefinition.isSet(BuiltInTypeSystem.P_TYPE_BASETYPE))
            {
                List baseTypes = typeDefinition.getList(BuiltInTypeSystem.P_TYPE_BASETYPE);
                if (baseTypes!=null && baseTypes.size()>1)
                    throw new IllegalArgumentException("Multiple inheritance not supported in Java implementations of SDO.");

                Object baseType = baseTypes.get(0);

                if (baseType instanceof DataObject)
                {
                    return getJavaClassFromDefinition((DataObject)baseType);
                }
                else if (baseType instanceof Type)
                {
                    return ((Type)baseType).getInstanceClass();
                }
                else
                {
                    throw new IllegalArgumentException("Unsuported instance for property baseType.");
                }
            }
            else
            {
                return null;
            }
        }
    }

    private Class getJavaClassFromString(String className)
    {
        if (className==null)
            return null;

        try
        {
            Class cls = _sdoContext.getClassLoader().loadClass(className);
            cls.getConstructor(String.class);
            return cls;
        }
        catch (ClassNotFoundException e)
        {
            throw new IllegalArgumentException("Class '" + className + "' not found.", e);
        }
        catch (NoSuchMethodException e)
        {
            throw new IllegalArgumentException("Class '" + className + "' is required to have a public String constructor.");
        }
    }
}
