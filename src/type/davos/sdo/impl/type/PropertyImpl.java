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
import davos.sdo.SDOContext;
import davos.sdo.TypeXML;
import davos.sdo.DataObjectXML;
import davos.sdo.binding.BindingSystem;
import davos.sdo.impl.util.PrimitiveCodes;
import davos.sdo.impl.common.Common;
import javax.sdo.DataObject;
import javax.sdo.Property;
import javax.sdo.Type;
import javax.sdo.helper.TypeHelper;
import org.apache.xmlbeans.SchemaType;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * @author Cezar Andrei (cezar dot andrei at gmail dot com)
 *         Date: Jan 10, 2006
 */
public class PropertyImpl
    extends MetadataHolder
    implements Property, PropertyXML
{
    private static int ST_ANY_SIMPLE_TYPE = SchemaType.BTC_ANY_SIMPLE;

    private TypeXML _type;
    private String _name;
    private boolean _isMany;
    private boolean _isContainment;
    private TypeXML _containingType;
    private Object _default;
    private boolean _isReadOnly;
    private PropertyXML _opposite;
    private List<String> _aliasNames;
    private PropertyXML[] _acceptedSubstitutions;

    private String _nameXML;
    private String _uriXML;

    private int _schemaTypeCode;
    private boolean _isElement;
    private boolean _isDynamic;
    private boolean _isGlobal;
    private boolean _isNullable;

    private PropertyImpl(){}

    public static PropertyImpl create()
    {
        return new PropertyImpl();
    }

    public void initMutable(TypeXML type, String name, boolean isMany, boolean isContainment, TypeXML containingType,
        Object defaultObj, boolean isReadOnly, boolean isNullable, PropertyXML opposite, List<String> aliasNames,
        boolean isGlobal, String nameXML, String uriXML, int schemaTypeCode, boolean isElement, boolean isDynamic)
    {
        assert type!=null;
        assert name!=null;
        _type = type;
        _name = name;
        _isMany = /*isGlobal ? true :*/ isMany;
        _isContainment = isContainment;
        _containingType = containingType;
        _default = defaultObj;
        _isReadOnly = isReadOnly;
        _opposite = opposite;

        assert aliasNames!=null;
        _aliasNames = aliasNames;

        _nameXML = nameXML;
        _uriXML = uriXML;
        _schemaTypeCode = schemaTypeCode;
        _isElement = isElement;
        _isDynamic = isDynamic;
        _isGlobal = isGlobal;
        _isNullable = isNullable;
    }

    public void initMutable(TypeXML type, String name, boolean isMany, boolean isContainment, TypeXML containingType,
        Object defaultObj, boolean isReadOnly, boolean isNullable, PropertyXML opposite, List<String> aliasNames,
        boolean isGlobal, boolean isDynamic)
    {
        boolean isElement = isElementByDefault(type, isMany, isNullable, isGlobal);
        initMutable(type, name, isMany, isContainment, containingType, defaultObj, isReadOnly, isNullable, opposite,
            aliasNames, isGlobal, name, getPropertyURI(containingType, isGlobal, isElement), ST_ANY_SIMPLE_TYPE,
            isElement, isDynamic);
    }

    void init(TypeXML type, String name, boolean isMany, boolean isContainment, TypeXML containingType,
        Object defaultObj, boolean isReadOnly, boolean isNullable, PropertyXML opposite, List<String> aliasNames,
        boolean isGlobal, String nameXML, String uriXML, int schemaTypeCode, boolean isElement, boolean isDynamic)
    {
        initMutable(type, name, isMany, isContainment, containingType, defaultObj, isReadOnly, isNullable, opposite,
            aliasNames, isGlobal, nameXML, uriXML, schemaTypeCode, isElement, isDynamic);
        super.makeImmutable();
    }

    void init(TypeXML type, String name, boolean isMany, boolean isContainment, TypeXML containingType,
        Object defaultObj, boolean isReadOnly, boolean isNullable, PropertyXML opposite, List<String> aliasNames,
        boolean isGlobal)
    {
        boolean isElement = isElementByDefault(type, isMany, isNullable, isGlobal);
        init(type, name, isMany, isContainment, containingType, defaultObj, isReadOnly, isNullable, opposite,
            aliasNames, isGlobal, name, getPropertyURI(containingType, isGlobal, isElement), ST_ANY_SIMPLE_TYPE,
            isElement, false);
    }

    public static PropertyImpl create(
        TypeXML type, String name, boolean isMany, boolean isContainment, TypeXML containingType,
        Object defaultObj, boolean isReadOnly, boolean isNullable, PropertyXML opposite, List<String> aliasNames)
    {
        boolean isGlobal = false;
        boolean isDynamic = false;
        boolean isElement = isElementByDefault(type, isMany, isNullable, isGlobal);

        return create(type, name, isMany, isContainment, containingType, defaultObj, isReadOnly, isNullable, opposite,
            aliasNames, name, getPropertyURI(containingType, isGlobal, isElement), ST_ANY_SIMPLE_TYPE,
            isElement, isDynamic, isGlobal);
    }

    public static PropertyImpl create(
        TypeXML type, String name, boolean isMany, boolean isContainment, TypeXML containingType,
        Object defaultObj, boolean isReadOnly, boolean isNullable, PropertyXML opposite, List<String> aliasNames,
        boolean isDynamic, boolean isGlobal)
    {
        boolean isElement = isElementByDefault(type, isMany, isNullable, isGlobal);
        return create(type, name, isMany, isContainment, containingType, defaultObj, isReadOnly, isNullable, opposite,
            aliasNames, name, getPropertyURI(containingType, isGlobal, isElement), ST_ANY_SIMPLE_TYPE,
            isElement, isDynamic, isGlobal);
    }

    public static PropertyImpl create(
        TypeXML type, String name, boolean isMany, boolean isContainment, TypeXML containingType,
        Object defaultObj, boolean isReadOnly, boolean isNullable, PropertyXML opposite, List<String> aliasNames,
        boolean isElement, boolean isDynamic, boolean isGlobal)
    {
        return create(type, name, isMany, isContainment, containingType, defaultObj, isReadOnly, isNullable, opposite,
            aliasNames, name, getPropertyURI(containingType, isGlobal, isElement), ST_ANY_SIMPLE_TYPE,
            isElement, isDynamic, isGlobal);
    }

    public static PropertyImpl create(
        TypeXML type, String name, boolean isMany, boolean isContainment, TypeXML containingType,
        Object defaultObj, boolean isReadOnly, boolean isNullable, PropertyXML opposite, List<String> aliasNames,
        String nameXML, String uriXML, int schemaTypeCode, boolean isElement, boolean isDynamic, boolean isGlobal)
    {
        PropertyImpl prop = create();
        prop.init(type, name, isMany, isContainment, containingType, defaultObj, isReadOnly, isNullable, opposite,
            aliasNames, isGlobal, nameXML, uriXML, schemaTypeCode, isElement, isDynamic);
        return prop;
    }

    public static PropertyXML createOnDemand(SDOContext sdoContext, String name, Object value, boolean forSequence)
    {
        BindingSystem bsys = sdoContext.getBindingSystem();

        // need to find the type based on value
        TypeXML type;
        if (value==null)
        {
            type = BuiltInTypeSystem.DATAOBJECT;
        }
        else if (value instanceof List )
        {
            List list = (List)value;
            if (list.size()==0)
                type = BuiltInTypeSystem.DATAOBJECT;
            else
            {
                Object firstItem = list.get(0);
                if (firstItem==null)
                    type = BuiltInTypeSystem.DATAOBJECT;
                else
                {
                    type = bsys.getType(bigToSmallClassConvert(firstItem.getClass()));
                    for (int i = 1; i < list.size(); i++)
                    {
                        Object item = list.get(i);
                        TypeXML itemType;
                        if (item==null)
                            itemType = BuiltInTypeSystem.DATAOBJECT;
                        else
                        {
                            itemType = bsys.getType(bigToSmallClassConvert(item.getClass()));
                        }

                        if (type==itemType)
                            continue;
                        else
                        {   // they are different
                            if (type.isDataType() && itemType.isDataType())
                                type = BuiltInTypeSystem.OBJECT;
                            else // at least one is not data type, i.e., is a data object
                                type = BuiltInTypeSystem.DATAOBJECT;
                        }

                        if (type==BuiltInTypeSystem.DATAOBJECT)
                            break; // most general, so break the for loop
                    }
                }
            }
        }
        else
        {
            // unwrap, change type from Integer to int (fix for TSK issue)
            Class classOfValue = value.getClass();
            classOfValue = bigToSmallClassConvert(classOfValue);

            type = bsys.getType(classOfValue);
            if (type==null)
            {
                if (value instanceof DataObjectXML)
                {
                    DataObjectXML dobj = (DataObjectXML)value;
                    type = dobj.getTypeXML();
                }
                else
                    type = BuiltInTypeSystem.OBJECT;
            }
        }

        // Spec2.1 page 19: The property's isMany value will be true for DataObject.set(List) or Sequence.add(),
        // false otherwise.
        boolean isMany = forSequence || value instanceof List;
        // Spec2.1 page 19: If the value is a DataObject that is not contained,
        // the new property will have isContainment set to true, false otherwise.
        boolean isContainment = (value instanceof DataObject && ((DataObject)value).getContainer() == null ) ||
            (value instanceof List && !type.isDataType());


        return createOnDemand(name, isMany, isContainment, type);
    }

    private static Class bigToSmallClassConvert(Class classOfValue)
    {
        if (Integer.class == classOfValue )
        {
            classOfValue = int.class;
        }
        else if (Boolean.class == classOfValue )
        {
            classOfValue = boolean.class;
        }
        else if (Byte.class == classOfValue )
        {
            classOfValue = byte.class;
        }
        else if (Double.class == classOfValue )
        {
            classOfValue = double.class;
        }
        else if (Float.class == classOfValue )
        {
            classOfValue = float.class;
        }
        else if (Long.class == classOfValue )
        {
            classOfValue = long.class;
        }
        else if (Character.class == classOfValue )
        {
            classOfValue = char.class;
        }

        return classOfValue;
    }

    public static PropertyXML createOnDemand(String name, boolean isMany, boolean isContainment, TypeXML type)
    {
        return PropertyImpl.create(type, name, isMany, isContainment, null, null, false, false, null,
            Common.EMPTY_STRING_LIST, true, true, false);
    }

    public void initSetAcceptedSubstitutions(PropertyXML[] acceptedSubstitutions)
    {
        _acceptedSubstitutions = acceptedSubstitutions;
    }

    public void makeImmutable()
    {
        super.makeImmutable();
    }

    private static boolean isElementByDefault(TypeXML type, boolean isMany, boolean isNullable, boolean isGlobal)
    {
        if ( isMany || isNullable || !type.isDataType() || isGlobal ||
                BuiltInTypeSystem.CHANGESUMMARYTYPE.equals(type))
            return true;

        return false;
    }

    private static String getPropertyURI(TypeXML containingType, boolean isGlobal, boolean isElement)
    {
        if ( isGlobal || isElement )
            if ( containingType!=null )
                return containingType.getURI();

        return null;
    }

    public static PropertyXML getPropertyXML(Property property)
    {
        if (property == null)
            return null;

        if (property instanceof PropertyXML)
            return (PropertyXML)property;

        throw new IllegalArgumentException("Other Property implementations not supported.");
//        return PropertyImpl.create(TypeImpl.getTypeXML(property.getType()), property.getName(), property.isMany(),
//                property.isContainment(), TypeImpl.getTypeXML(property.getContainingType()), property.getDefault(),
//                property.isReadOnly(), getPropertyXML(property.getOpposite()),
//                (List<String>)property.getAliasNames());
    }

    public String getName()
    {
        return _name;
    }

    public Type getType()
    {
        return getTypeXML();
    }

    public TypeXML getTypeXML()
    {
        return _type;
    }

    public boolean isMany()
    {
        return _isMany;
    }

    public boolean isContainment()
    {
        return _isContainment;
    }

    public Type getContainingType()
    {
        return _containingType;
    }

    public TypeXML getContainingTypeXML()
    {
        return _containingType;
    }

    public Object getDefault()
    {
        if ( _default!=null )
            return _default;

/**        TypeXML propType = getTypeXML();

        if ( propType.isDataType() )
        {
            Class instanceClass = propType.getInstanceClass();

---    in case they change their mind again and want default values based on SDOTypes.
            if ( BuiltInTypeSystem.BOOLEAN.isAssignableFrom(propType) )
                if ( instanceClass == boolean.class)
                    return Boolean.FALSE;
                else
                    return constructUserDefinedClass(instanceClass, "false");

            if ( BuiltInTypeSystem.BYTE.isAssignableFrom(propType) )
                if ( instanceClass == byte.class)
                    return PrimitiveCodes.ZERO_BYTE;
                else
                    return constructUserDefinedClass(instanceClass, "0");

            if ( BuiltInTypeSystem.CHARACTER.isAssignableFrom(propType) )
                if ( instanceClass == char.class)
                    return PrimitiveCodes.ZERO_CHARACTER;
                else
                {
                    try
                    {
                        Constructor constructor = instanceClass.getConstructor(Character.TYPE);
                        return constructor.newInstance(0);
                    }
                    catch (Exception e)
                    {
                    }
                }

            if ( BuiltInTypeSystem.DOUBLE.isAssignableFrom(propType) )
                if ( instanceClass == double.class)
                    return PrimitiveCodes.ZERO_DOUBLE;
                else
                    return constructUserDefinedClass(instanceClass, "0");

            if ( BuiltInTypeSystem.FLOAT.isAssignableFrom(propType) )
                if ( instanceClass == float.class)
                    return PrimitiveCodes.ZERO_FLOAT;
                else
                    return constructUserDefinedClass(instanceClass, "0");

            if ( BuiltInTypeSystem.INT.isAssignableFrom(propType) )
                if ( instanceClass == int.class)
                    return PrimitiveCodes.ZERO_INTEGER;
                else
                    return constructUserDefinedClass(instanceClass, "0");

            if ( BuiltInTypeSystem.LONG.isAssignableFrom(propType) )
                if ( instanceClass == long.class)
                    return PrimitiveCodes.ZERO_LONG;
                else
                    return constructUserDefinedClass(instanceClass, "0");

            if ( BuiltInTypeSystem.SHORT.isAssignableFrom(propType) )
                if ( instanceClass == short.class)
                    return PrimitiveCodes.ZERO_SHORT;
                else
                    return constructUserDefinedClass(instanceClass, "0");
--- in case it has to return a value based on instance class 
            if ( instanceClass == boolean.class)
                return Boolean.FALSE;
            if ( instanceClass == byte.class)
                return PrimitiveCodes.ZERO_BYTE;
            if ( instanceClass == char.class)
                return PrimitiveCodes.ZERO_CHARACTER;
            if ( instanceClass == double.class)
                return PrimitiveCodes.ZERO_DOUBLE;
            if ( instanceClass == float.class)
                return PrimitiveCodes.ZERO_FLOAT;
            if ( instanceClass == int.class)
                return PrimitiveCodes.ZERO_INTEGER;
            if ( instanceClass == long.class)
                return PrimitiveCodes.ZERO_LONG;
            if ( instanceClass == short.class)
                return PrimitiveCodes.ZERO_SHORT;
        }
*/
        return null;
    }

    private static Object constructUserDefinedClass(Class instanceClass, String value)
    {
        try
        {
            Constructor constructor = instanceClass.getConstructor(String.class);
            return constructor.newInstance(value);
        }
        catch (NoSuchMethodException e)
        {
            throw new RuntimeException("Instance class "+ instanceClass + " must have a public String constructor.");
        }
        catch (InstantiationException e)
        {
            throw new RuntimeException("Instance class "+ instanceClass + " must have a public String constructor.", e);
        }
        catch (IllegalAccessException e)
        {
            throw new RuntimeException("Instance class "+ instanceClass + " must have a public String constructor.", e);
        }
        catch (InvocationTargetException e)
        {
            throw new RuntimeException("Instance class "+ instanceClass + " must have a public String constructor.", e);
        }
    }

    public boolean isReadOnly()
    {
        return _isReadOnly;
    }

    public Property getOpposite()
    {
        return _opposite;
    }

    public PropertyXML getOppositeXML()
    {
        return _opposite;
    }

    public void setOppositeXML(PropertyXML oppositeProperty)
    {
        _opposite = oppositeProperty;
    }

    public List /*String*/ getAliasNames()
    {
        return _aliasNames;
    }

    public PropertyXML[] getAcceptedSubstitutions()
    {
        return _acceptedSubstitutions;
    }

    public boolean isNullable()
    {
        return _isNullable;
    }

    public boolean isOpenContent()
    {
        return isGlobal() || isDynamic();
    }

    public boolean equals(Object o)
    {
        if (this==o)
            return true;

        if ( !(o instanceof Property))
            return false;

        Property p = (Property)o;

        return getName().equals(p.getName()) &&
            ( ( getContainingType()==null && p.getContainingType()==null ) ||
              ( getContainingType().equals(p.getContainingType()) && getType().equals(p.getType()) )
            );
    }

    public int hashCode()
    {
        String name = getName();
        if (name == null)
            return super.hashCode();
        else
        {
            return name.hashCode(); // + getContainingType().hashCode();
        }
    }

    public String toString()
    {
        return _name + " " + _type + (_isMany ? " many" : "") + (_isReadOnly ? " readonly" : "") +
            (_isContainment ? " containment" :"");
    }

    public void dump()
    {
        System.out.print((isGlobal() ? "Global" : "") + "Property: " + _name + " " + _type +
            (_isMany ? " many" : "") + (_isReadOnly ? " readonly" : "") + (_isContainment ? " containment" :""));

        if (_containingType != null)
            System.out.print(" containingType: " + (isOpenContent() ? _containingType.getURI() : _containingType));
        if ( _default != null)
            System.out.print(" default" + _default);

        if (_opposite != null)
            System.out.print(" opposite" + _opposite);

        System.out.print("\n  aliasNames:");
        for (String aliasName : _aliasNames)
        {
            System.out.print(" " + aliasName);
        }

        System.out.println("");
    }

    public boolean isXMLElement()
    {
        return _isElement;
    }

    void setXMLElement(boolean isXMLElement)
    {
        _isElement = isXMLElement;
    }

    public String getXMLName()
    {
        return _nameXML;
    }

    public String getXMLNamespaceURI()
    {
        return _uriXML;
    }

    public int getSchemaTypeCode()
    {
        return _schemaTypeCode;
    }

    public boolean isDynamic()
    {
        return _isDynamic;
    }

    public boolean isGlobal()
    {
        return _isGlobal;
    }
}
