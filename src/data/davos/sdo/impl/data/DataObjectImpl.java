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
import davos.sdo.ListXMLIterator;
import davos.sdo.PropertyXML;
import davos.sdo.SDOContext;
import davos.sdo.SequenceXML;
import davos.sdo.TypeXML;
import davos.sdo.impl.helpers.DataHelperImpl;
import davos.sdo.impl.path.Parser;
import davos.sdo.impl.path.Path;
import davos.sdo.impl.type.BuiltInTypeSystem;
import davos.sdo.impl.type.PropertyImpl;
import javax.sdo.ChangeSummary;
import javax.sdo.DataGraph;
import javax.sdo.DataObject;
import javax.sdo.Property;
import javax.sdo.Sequence;
import javax.sdo.Type;
import javax.sdo.impl.ExternalizableDelegator;
import org.apache.xmlbeans.impl.util.HexBin;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;

/**
 * @author Cezar Andrei (cezar dot andrei at gmail dot com)
 *         Date: Jan 6, 2006
 */
public abstract class DataObjectImpl
    implements DataObject, DataObjectXML, Serializable
{
    private SDOContext _sdoContext;
    private TypeXML _type;
    // non-null only if this is the root object
    private DataGraph _dataGraph;
    private DataObjectXML _container;
    private PropertyXML _containmentProperty;
    // non-null only if this has a ChangeSummaryProperty
    private ChangeSummary _changeSummary;
    private boolean _ignoreOpposites = false;

    public void init(TypeXML type, DataGraph dataGraph, DataObjectXML container, PropertyXML containmentProperty)
    {
        assert type!=null;

        _type = type;
        _dataGraph = dataGraph;
        _container = container;
        _containmentProperty = containmentProperty;
        _changeSummary = initChangeSummary();
    }

    public void setSDOContext(SDOContext sdoContext)
    {
        _sdoContext = sdoContext;
    }

    public SDOContext getSDOContext()
    {
        return _sdoContext;
    }

    public void setContainmentProperty(PropertyXML containmentProperty)
    {
        if (_containmentProperty == null)
            _containmentProperty = containmentProperty;
    }

    // DataObject impl
    public Object get(String path)
    {
        return Path.executeGet(this, path);
    }

    public void set(String path, Object value)
    {
        Path.executeSet(getSDOContext(), this, path, Path.SETTER_UNTYPED, value);
    }

    public boolean isSet(String path)
    {
        return Path.executeIsSet(this, path);
    }

    public void unset(String path)
    {
        Path.executeUnset(this, path);
    }

    public boolean getBoolean(String path)
    {
        Object value = get(path);
        if (value==null)
            return false;

        if ( value instanceof List )
        {
            List l = (List)value;
            switch (l.size())
            {
            case 0:  return false;
            case 1:  value = l.get(0); break;
            default: //fall off in CastClassException
            }
        }

        if ( value instanceof String)
            return Boolean.parseBoolean((String)value);

        return ((Boolean)value).booleanValue();
    }

    public byte getByte(String path)
    {
        Object value = get(path);
        if (value==null)
            return 0;

        if ( value instanceof List )
        {
            List l = (List)value;
            switch (l.size())
            {
            case 0:  return 0;
            case 1:  value = l.get(0); break;
            default: //fall off in CastClassException
            }
        }

        if ( value instanceof Byte)
            return ((Byte)value).byteValue();
        if ( value instanceof String)
            return Byte.parseByte((String)value);
        if ( value instanceof Double)
            return ((Double)value).byteValue();
        if ( value instanceof Float)
            return ((Float)value).byteValue();
        if ( value instanceof Integer)
            return ((Integer)value).byteValue();
        if ( value instanceof Long)
            return ((Long)value).byteValue();
        if ( value instanceof Short)
            return ((Short)value).byteValue();
        if ( value instanceof BigDecimal)
            return ((BigDecimal)value).byteValue();
        if ( value instanceof BigInteger)
            return ((BigInteger)value).byteValue();

        return (Byte)value;
    }

    public char getChar(String path)
    {
        Object value = get(path);
        if (value==null)
            return '\0';

        if ( value instanceof List )
        {
            List l = (List)value;
            switch (l.size())
            {
            case 0:  return '\0';
            case 1:  value = l.get(0); break;
            default: //fall off in CastClassException
            }
        }

        if ( value instanceof String)
        {
            String stringValue = (String)value;
            if ( stringValue.length() == 0 )
                return '\0';
            else
                return stringValue.charAt(0);
        }

        return ((Character)value).charValue();
    }

    public double getDouble(String path)
    {
        Object value = get(path);
        if (value==null)
            return 0;

        if ( value instanceof List )
        {
            List l = (List)value;
            switch (l.size())
            {
            case 0:  return 0;
            case 1:  value = l.get(0); break;
            default: //fall off in CastClassException
            }
        }

        if ( value instanceof Double)
            return ((Double)value).doubleValue();
        if ( value instanceof String)
            return Double.parseDouble((String)value);
        if ( value instanceof Byte)
            return ((Byte)value).doubleValue();
        if ( value instanceof Float)
            return ((Float)value).doubleValue();
        if ( value instanceof Integer)
            return ((Integer)value).doubleValue();
        if ( value instanceof Long)
            return ((Long)value).doubleValue();
        if ( value instanceof Short)
            return ((Short)value).doubleValue();
        if ( value instanceof BigDecimal)
            return ((BigDecimal)value).doubleValue();
        if ( value instanceof BigInteger)
            return ((BigInteger)value).doubleValue();

        return (Double)value;
    }

    public float getFloat(String path)
    {
        Object value = get(path);
        if (value==null)
            return 0;

        if ( value instanceof List )
        {
            List l = (List)value;
            switch (l.size())
            {
            case 0:  return 0;
            case 1:  value = l.get(0); break;
            default: //fall off in CastClassException
            }
        }

        if ( value instanceof Float)
            return ((Float)value).floatValue();
        if ( value instanceof String)
            return Float.parseFloat((String)value);
        if ( value instanceof Byte)
            return ((Byte)value).floatValue();
        if ( value instanceof Double)
            return ((Double)value).floatValue();
        if ( value instanceof Integer)
            return ((Integer)value).floatValue();
        if ( value instanceof Long)
            return ((Long)value).floatValue();
        if ( value instanceof Short)
            return ((Short)value).floatValue();
        if ( value instanceof BigDecimal)
            return ((BigDecimal)value).floatValue();
        if ( value instanceof BigInteger)
            return ((BigInteger)value).floatValue();

        return (Float)value;
    }

    public int getInt(String path)
    {
        Object value = get(path);
        if (value==null)
            return 0;

        if ( value instanceof List )
        {
            List l = (List)value;
            switch (l.size())
            {
            case 0:  return 0;
            case 1:  value = l.get(0); break;
            default: //fall off in CastClassException
            }
        }

        if ( value instanceof Integer)
            return ((Integer)value).intValue();
        if ( value instanceof String)
            return Integer.parseInt((String)value);
        if ( value instanceof Byte)
            return ((Byte)value).intValue();
        if ( value instanceof Double)
            return ((Double)value).intValue();
        if ( value instanceof Float)
            return ((Float)value).intValue();
        if ( value instanceof Long)
            return ((Long)value).intValue();
        if ( value instanceof Short)
            return ((Short)value).intValue();
        if ( value instanceof BigDecimal)
            return ((BigDecimal)value).intValue();
        if ( value instanceof BigInteger)
            return ((BigInteger)value).intValue();

        return (Integer)value;
    }

    public long getLong(String path)
    {
        Object value = get(path);
        if (value==null)
            return 0;

        if ( value instanceof List )
        {
            List l = (List)value;
            switch (l.size())
            {
            case 0:  return 0;
            case 1:  value = l.get(0); break;
            default: //fall off in CastClassException
            }
        }

        if ( value instanceof Long)
            return ((Long)value).longValue();
        if ( value instanceof String)
            return Long.parseLong((String)value);
        if ( value instanceof Byte)
            return ((Byte)value).longValue();
        if ( value instanceof Double)
            return ((Double)value).longValue();
        if ( value instanceof Float)
            return ((Float)value).longValue();
        if ( value instanceof Integer)
            return ((Integer)value).longValue();
        if ( value instanceof Short)
            return ((Short)value).longValue();
        if ( value instanceof BigDecimal)
            return ((BigDecimal)value).longValue();
        if ( value instanceof BigInteger)
            return ((BigInteger)value).longValue();
        if ( value instanceof Date)
            return ((Date)value).getTime();

        return (Long)value;
    }

    public short getShort(String path)
    {
        Object value = get(path);
        if (value==null)
            return 0;

        if ( value instanceof List )
        {
            List l = (List)value;
            switch (l.size())
            {
            case 0:  return 0;
            case 1:  value = l.get(0); break;
            default: //fall off in CastClassException
            }
        }

        if ( value instanceof Short)
            return ((Short)value).shortValue();
        if ( value instanceof String)
            return Short.parseShort((String)value);
        if ( value instanceof Byte)
            return ((Byte)value).shortValue();
        if ( value instanceof Double)
            return ((Double)value).shortValue();
        if ( value instanceof Float)
            return ((Float)value).shortValue();
        if ( value instanceof Integer)
            return ((Integer)value).shortValue();
        if ( value instanceof Long)
            return ((Long)value).shortValue();
        if ( value instanceof BigDecimal)
            return ((BigDecimal)value).shortValue();
        if ( value instanceof BigInteger)
            return ((BigInteger)value).shortValue();

        return (Short)value;
    }

    public byte[] getBytes(String path)
    {
        Object value = get(path);
        if (value==null)
            return null;

        if ( value instanceof List )
        {
            List l = (List)value;
            switch (l.size())
            {
            case 0:  return null;
            case 1:  value = l.get(0); break;
            default: //fall off in CastClassException
            }
        }

        if ( value instanceof BigInteger)
            return ((BigInteger)value).toByteArray();
        if ( value instanceof String)
            return HexBin.stringToBytes((String)value);

        return (byte[])value;
    }

    public BigDecimal getBigDecimal(String path)
    {
        Object value = get(path);
        if (value==null)
            return null;

        if ( value instanceof List )
        {
            List l = (List)value;
            switch (l.size())
            {
            case 0:  return null;
            case 1:  value = l.get(0); break;
            default: //fall off in CastClassException
            }
        }

        if ( value instanceof BigDecimal)
            return (BigDecimal)value;
        if ( value instanceof String)
            return new BigDecimal((String)value);
        if ( value instanceof Byte)
            return BigDecimal.valueOf((Byte)value);
        if ( value instanceof Double)
            return BigDecimal.valueOf((Double)value);
        if ( value instanceof Float)
            return BigDecimal.valueOf((Float)value);
        if ( value instanceof Integer)
            return BigDecimal.valueOf((Integer)value);
        if ( value instanceof Long)
            return BigDecimal.valueOf((Long)value);
        if ( value instanceof Short)
            return BigDecimal.valueOf((Short)value);
        if ( value instanceof BigInteger)
            return new BigDecimal((BigInteger)value);

        return (BigDecimal)value;
    }

    public BigInteger getBigInteger(String path)
    {
        Object value = get(path);
        if (value==null)
            return null;

        if ( value instanceof List )
        {
            List l = (List)value;
            switch (l.size())
            {
            case 0:  return null;
            case 1:  value = l.get(0); break;
            default: //fall off in CastClassException
            }
        }

        if ( value instanceof BigInteger)
            return (BigInteger)value;
        if ( value instanceof String)
            return new BigInteger((String)value);
        if ( value instanceof Byte)
            return BigInteger.valueOf((Byte)value);
        if ( value instanceof Double)
            return BigInteger.valueOf(((Double)value).longValue());
        if ( value instanceof Float)
            return BigInteger.valueOf(((Float)value).longValue());
        if ( value instanceof Integer)
            return BigInteger.valueOf((Integer)value);
        if ( value instanceof Long)
            return BigInteger.valueOf((Long)value);
        if ( value instanceof Short)
            return BigInteger.valueOf((Short)value);
        if ( value instanceof BigDecimal)
            return ((BigDecimal)value).toBigInteger();
        if ( value instanceof byte[])
            return new BigInteger((byte[])value);

        return (BigInteger)value;
    }

    public DataObject getDataObject(String path)
    {
        Object value = get(path);
        if ( value instanceof List )
        {
            List l = (List)value;
            switch (l.size())
            {
            case 0:  return null;
            case 1:  value = l.get(0); break;
            default: //fall off in CastClassException
            }
        }
        return (DataObject)value;
    }

    public Date getDate(String path)
    {
        Object value = get(path);
        if (value==null)
            return null;

        if ( value instanceof List )
        {
            List l = (List)value;
            switch (l.size())
            {
            case 0:  return null;
            case 1:  value = l.get(0); break;
            default: //fall off in CastClassException
            }
        }

        if ( value instanceof Date)
            return (Date)value;
        if ( value instanceof String)
        {
            try
            {
                return DataHelperImpl._toDate((String)value);
            }
            catch (IllegalArgumentException e)
            {
                throw new ClassCastException("Unable to convert value '" + value + "' to a java.util.Date");
            }
        }
        if ( value instanceof Long)
            return new Date((Long)value);

        return (Date)value;
    }

    public String getString(String path)
    {
        Object value = get(path);
        if (value==null)
            return null;

        if ( value instanceof String)
            return (String)value;
        if ( value instanceof Boolean)
            return ((Boolean)value).toString();
        if ( value instanceof Byte)
            return ((Byte)value).toString();
        if ( value instanceof Character)
        {
            if (((Character)value).charValue() == '\0')
                return "";
            else
                return ((Character)value).toString();
        }
        if ( value instanceof Double)
            return ((Double)value).toString();
        if ( value instanceof Float)
            return ((Float)value).toString();
        if ( value instanceof Integer)
            return ((Integer)value).toString();
        if ( value instanceof Long)
            return ((Long)value).toString();
        if ( value instanceof Short)
            return ((Short)value).toString();
        if ( value instanceof byte[])
            return HexBin.bytesToString((byte[])value);
        if ( value instanceof BigDecimal)
            return ((BigDecimal)value).toString();
        if ( value instanceof BigInteger)
            return ((BigInteger)value).toString();
        if ( value instanceof Date)
            return DataHelperImpl._toDateTime((Date)value);
        if ( value instanceof List)
        {
            // NOTE (w.y.): We want to test if the property is of type STRINGS.
            // Unfortunately, there does not seem to be a way to determine 
            // the property (and thus its type) from the path.

            List valueList = (List)value;
            if (valueList.size() == 0) return null;
            boolean cce = false;
            String res = "";
            try
            {
                for (int i = 0; i < valueList.size(); i++)
                {
                    if (i>0)
                        res += " ";
                    String s = (String)valueList.get(i); // could throw CCE
                    res += (s == null ? "" : s);
                }
            }
            catch (ClassCastException e)
            {
                cce = true;
            }
            if (!cce) return res;
            else return ((List)value).toString();
        }

        return value.toString(); //(String)value;
    }

    public List getList(String path)
    {
        try
        {
            Path rez = Path.execute(this, path, Path.OP_GET);
            if( rez.isSuccesfull() )
                return (List)rez.getValue();
            else
            {
                if (rez.canCreateOnDemandProperty())
                {
                    PropertyXML onDemandProperty = PropertyImpl.createOnDemand(getSDOContext(),
                        rez.getOnDemandPropertyName(), Collections.EMPTY_LIST, false);
                    assert onDemandProperty.isMany();
                    return (List)get(onDemandProperty);
                }

                return null;
            }
        }
        catch (Parser.SDOPathException e)
        {   //do nothing just
            return null;
        }
        catch (IllegalArgumentException e)
        {   // do nothing just
            return null;
        }
    }

    public void setBoolean(String path, boolean value)
    {
        Path.executeSet(getSDOContext(), this, path, Path.SETTER_BOOLEAN, value);
    }

    public void setByte(String path, byte value)
    {
        Path.executeSet(getSDOContext(), this, path, Path.SETTER_BYTE, value);
    }

    public void setChar(String path, char value)
    {
        Path.executeSet(getSDOContext(), this, path, Path.SETTER_CHAR, value);
    }

    public void setDouble(String path, double value)
    {
        Path.executeSet(getSDOContext(), this, path, Path.SETTER_DOUBLE, value);
    }

    public void setFloat(String path, float value)
    {
        Path.executeSet(getSDOContext(), this, path, Path.SETTER_FLOAT, value);
    }

    public void setInt(String path, int value)
    {
        Path.executeSet(getSDOContext(), this, path, Path.SETTER_INT, value);
    }

    public void setLong(String path, long value)
    {
        Path.executeSet(getSDOContext(), this, path, Path.SETTER_LONG, value);
    }

    public void setShort(String path, short value)
    {
        Path.executeSet(getSDOContext(), this, path, Path.SETTER_SHORT, value);
    }

    public void setBytes(String path, byte[] value)
    {
        Path.executeSet(getSDOContext(), this, path, Path.SETTER_BYTES, value);
    }

    public void setBigDecimal(String path, BigDecimal value)
    {
        Path.executeSet(getSDOContext(), this, path, Path.SETTER_BIGDECIMAL, value);
    }

    public void setBigInteger(String path, BigInteger value)
    {
        Path.executeSet(getSDOContext(), this, path, Path.SETTER_BIGINTEGER, value);
    }

    public void setDataObject(String path, DataObject value)
    {
        Path.executeSet(getSDOContext(), this, path, Path.SETTER_DATAOBJECT, value);
    }

    public void setDate(String path, Date value)
    {
        Path.executeSet(getSDOContext(), this, path, Path.SETTER_DATE, value);
    }

    public void setString(String path, String value)
    {
        Path.executeSet(getSDOContext(), this, path, Path.SETTER_STRING, value);
    }

    public void setList(String path, List value)
    {
        Path.executeSet(getSDOContext(), this, path, Path.SETTER_LIST, value);
    }

    public Object get(int propertyIndex)
    {
        return get(getPropertyForPropertyIndex(propertyIndex));
    }

    public void set(int propertyIndex, Object value)
    {
        set(getPropertyForPropertyIndex(propertyIndex), value);
    }

    public boolean isSet(int propertyIndex)
    {
        return isSet(getPropertyForPropertyIndex(propertyIndex));
    }

    public void unset(int propertyIndex)
    {
        unset(getPropertyForPropertyIndex(propertyIndex));
    }

    public boolean getBoolean(int propertyIndex)
    {
        return getBoolean(getPropertyForPropertyIndex(propertyIndex));
    }

    public byte getByte(int propertyIndex)
    {
        return getByte(getPropertyForPropertyIndex(propertyIndex));
    }

    public char getChar(int propertyIndex)
    {
        return getChar(getPropertyForPropertyIndex(propertyIndex));
    }

    public double getDouble(int propertyIndex)
    {
        return getDouble(getPropertyForPropertyIndex(propertyIndex));
    }

    public float getFloat(int propertyIndex)
    {
        return getFloat(getPropertyForPropertyIndex(propertyIndex));
    }

    public int getInt(int propertyIndex)
    {
        return getInt(getPropertyForPropertyIndex(propertyIndex));
    }

    public long getLong(int propertyIndex)
    {
        return getLong(getPropertyForPropertyIndex(propertyIndex));
    }

    public short getShort(int propertyIndex)
    {
        return getShort(getPropertyForPropertyIndex(propertyIndex));
    }

    public byte[] getBytes(int propertyIndex)
    {
        return getBytes(getPropertyForPropertyIndex(propertyIndex));
    }

    public BigDecimal getBigDecimal(int propertyIndex)
    {
        return getBigDecimal(getPropertyForPropertyIndex(propertyIndex));
    }

    public BigInteger getBigInteger(int propertyIndex)
    {
        return getBigInteger(getPropertyForPropertyIndex(propertyIndex));
    }

    public DataObject getDataObject(int propertyIndex)
    {
        return getDataObject(getPropertyForPropertyIndex(propertyIndex));
    }

    public Date getDate(int propertyIndex)
    {
        return getDate(getPropertyForPropertyIndex(propertyIndex));
    }

    public String getString(int propertyIndex)
    {
        return getString(getPropertyForPropertyIndex(propertyIndex));
    }

    public List getList(int propertyIndex)
    {
        return (List)get(propertyIndex);
    }

    public void setBoolean(int propertyIndex, boolean value)
    {
        setBoolean(getPropertyForPropertyIndex(propertyIndex), value);
    }

    public void setByte(int propertyIndex, byte value)
    {
        setByte(getPropertyForPropertyIndex(propertyIndex), value);
    }

    public void setChar(int propertyIndex, char value)
    {
        setChar(getPropertyForPropertyIndex(propertyIndex), value);
    }

    public void setDouble(int propertyIndex, double value)
    {
        setDouble(getPropertyForPropertyIndex(propertyIndex), value);
    }

    public void setFloat(int propertyIndex, float value)
    {
        setFloat(getPropertyForPropertyIndex(propertyIndex), value);
    }

    public void setInt(int propertyIndex, int value)
    {
        setInt(getPropertyForPropertyIndex(propertyIndex), value);
    }

    public void setLong(int propertyIndex, long value)
    {
        setLong(getPropertyForPropertyIndex(propertyIndex), value);
    }

    public void setShort(int propertyIndex, short value)
    {
        setShort(getPropertyForPropertyIndex(propertyIndex), value);
    }

    public void setBytes(int propertyIndex, byte[] value)
    {
        setBytes(getPropertyForPropertyIndex(propertyIndex), value);
    }

    public void setBigDecimal(int propertyIndex, BigDecimal value)
    {
        setBigDecimal(getPropertyForPropertyIndex(propertyIndex), value);
    }

    public void setBigInteger(int propertyIndex, BigInteger value)
    {
        setBigInteger(getPropertyForPropertyIndex(propertyIndex), value);
    }

    public void setDataObject(int propertyIndex, DataObject value)
    {
        setDataObject(getPropertyForPropertyIndex(propertyIndex), value);
    }

    public void setDate(int propertyIndex, Date value)
    {
        setDate(getPropertyForPropertyIndex(propertyIndex), value);
    }

    public void setString(int propertyIndex, String value)
    {
        setString(getPropertyForPropertyIndex(propertyIndex), value);
    }

    public void setList(int propertyIndex, List value)
    {
        setList(getPropertyForPropertyIndex(propertyIndex), value);
    }

    public Object get(Property property)
    {
        return getStore().storeGet(PropertyImpl.getPropertyXML(property));
    }

    public void set(Property property, Object newValue)
    {
        if (property.isReadOnly())
            throw new UnsupportedOperationException("Read-only property '" + property + "' cannot be modified.");

        PropertyXML propXml = PropertyImpl.getPropertyXML(property);
        checkOppositeUniqueObjectConstraint(this, propXml, newValue);
        Object oldValue = getOldValueForOppositeSet(propXml, newValue);
        getStore().storeSet(propXml, newValue);
        setOppositeProperty(this, propXml, newValue, oldValue);
    }

    public boolean isSet(Property property)
    {
        return getStore().storeIsSet(property);
    }

    public void unset(Property property)
    {
        if (property.isReadOnly())
            throw new UnsupportedOperationException("Read-only property '" + property + "' cannot be unset.");

        PropertyXML propXml = PropertyImpl.getPropertyXML(property);

        Object oldValue = getStore().storeGet(propXml);
        if (propXml.isMany())
        {
            // oldValue must be a list and we need to back up the values
            List l = new ArrayList();
            l.addAll((List)oldValue);
            oldValue = l;
        }

        getStore().storeUnset(propXml);
        unsetOppositeProperty(this, propXml, oldValue);
    }

    public boolean getBoolean(Property property)
    {
        Object value = get(property);
        if (value==null)
            return false;

        if ( value instanceof List )
        {
            List l = (List)value;
            switch (l.size())
            {
            case 0:  return false;
            case 1:  value = l.get(0); break;
            default: //fall off in CastClassException
            }
        }

        if ( value instanceof String)
            return Boolean.parseBoolean((String)value);

        return ((Boolean)value).booleanValue();
    }

    public byte getByte(Property property)
    {
        Object value = get(property);
        if (value==null)
            return 0;

        if ( value instanceof List )
        {
            List l = (List)value;
            switch (l.size())
            {
            case 0:  return 0;
            case 1:  value = l.get(0); break;
            default: //fall off in CastClassException
            }
        }

        if ( value instanceof Byte)
            return ((Byte)value).byteValue();
        if ( value instanceof String)
            return Byte.parseByte((String)value);
        if ( value instanceof Double)
            return ((Double)value).byteValue();
        if ( value instanceof Float)
            return ((Float)value).byteValue();
        if ( value instanceof Integer)
            return ((Integer)value).byteValue();
        if ( value instanceof Long)
            return ((Long)value).byteValue();
        if ( value instanceof Short)
            return ((Short)value).byteValue();
        if ( value instanceof BigDecimal)
            return ((BigDecimal)value).byteValue();
        if ( value instanceof BigInteger)
            return ((BigInteger)value).byteValue();

        return (Byte)value;
    }

    public char getChar(Property property)
    {
        Object value = get(property);
        if (value==null)
            return '\0';

        if ( value instanceof List )
        {
            List l = (List)value;
            switch (l.size())
            {
            case 0:  return '\0';
            case 1:  value = l.get(0); break;
            default: //fall off in CastClassException
            }
        }

        if ( value instanceof String)
        {
            String stringValue = (String)value;
            if ( stringValue.length() == 0 )
                return '\0';
            else
                return stringValue.charAt(0);
        }

        return ((Character)value).charValue();
    }

    public double getDouble(Property property)
    {
        Object value = get(property);
        if (value==null)
            return 0;

        if ( value instanceof List )
        {
            List l = (List)value;
            switch (l.size())
            {
            case 0:  return 0;
            case 1:  value = l.get(0); break;
            default: //fall off in CastClassException
            }
        }

        if ( value instanceof Double)
            return ((Double)value).doubleValue();
        if ( value instanceof String)
            return Double.parseDouble((String)value);
        if ( value instanceof Byte)
            return ((Byte)value).doubleValue();
        if ( value instanceof Float)
            return ((Float)value).doubleValue();
        if ( value instanceof Integer)
            return ((Integer)value).doubleValue();
        if ( value instanceof Long)
            return ((Long)value).doubleValue();
        if ( value instanceof Short)
            return ((Short)value).doubleValue();
        if ( value instanceof BigDecimal)
            return ((BigDecimal)value).doubleValue();
        if ( value instanceof BigInteger)
            return ((BigInteger)value).doubleValue();

        return (Double)value;
    }

    public float getFloat(Property property)
    {
        Object value = get(property);
        if (value==null)
            return 0;

        if ( value instanceof List )
        {
            List l = (List)value;
            switch (l.size())
            {
            case 0:  return 0;
            case 1:  value = l.get(0); break;
            default: //fall off in CastClassException
            }
        }

        if ( value instanceof Float)
            return ((Float)value).floatValue();
        if ( value instanceof String)
            return Float.parseFloat((String)value);
        if ( value instanceof Byte)
            return ((Byte)value).floatValue();
        if ( value instanceof Double)
            return ((Double)value).floatValue();
        if ( value instanceof Integer)
            return ((Integer)value).floatValue();
        if ( value instanceof Long)
            return ((Long)value).floatValue();
        if ( value instanceof Short)
            return ((Short)value).floatValue();
        if ( value instanceof BigDecimal)
            return ((BigDecimal)value).floatValue();
        if ( value instanceof BigInteger)
            return ((BigInteger)value).floatValue();

        return (Float)value;
    }

    public int getInt(Property property)
    {
        Object value = get(property);
        if (value==null)
            return 0;

        if ( value instanceof List )
        {
            List l = (List)value;
            switch (l.size())
            {
            case 0:  return 0;
            case 1:  value = l.get(0); break;
            default: //fall off in CastClassException
            }
        }

        if ( value instanceof Integer)
            return ((Integer)value).intValue();
        if ( value instanceof String)
            return Integer.parseInt((String)value);
        if ( value instanceof Byte)
            return ((Byte)value).intValue();
        if ( value instanceof Double)
            return ((Double)value).intValue();
        if ( value instanceof Float)
            return ((Float)value).intValue();
        if ( value instanceof Long)
            return ((Long)value).intValue();
        if ( value instanceof Short)
            return ((Short)value).intValue();
        if ( value instanceof BigDecimal)
            return ((BigDecimal)value).intValue();
        if ( value instanceof BigInteger)
            return ((BigInteger)value).intValue();

        return (Integer)value;
    }

    public long getLong(Property property)
    {
        Object value = get(property);
        if (value==null)
            return 0;

        if ( value instanceof List )
        {
            List l = (List)value;
            switch (l.size())
            {
            case 0:  return 0;
            case 1:  value = l.get(0); break;
            default: //fall off in CastClassException
            }
        }

        if ( value instanceof Long)
            return ((Long)value).longValue();
        if ( value instanceof String)
            return Long.parseLong((String)value);
        if ( value instanceof Byte)
            return ((Byte)value).longValue();
        if ( value instanceof Double)
            return ((Double)value).longValue();
        if ( value instanceof Float)
            return ((Float)value).longValue();
        if ( value instanceof Integer)
            return ((Integer)value).longValue();
        if ( value instanceof Short)
            return ((Short)value).longValue();
        if ( value instanceof BigDecimal)
            return ((BigDecimal)value).longValue();
        if ( value instanceof BigInteger)
            return ((BigInteger)value).longValue();
        if ( value instanceof Date)
            return ((Date)value).getTime();

        return (Long)value;
    }

    public short getShort(Property property)
    {
        Object value = get(property);
        if (value==null)
            return 0;

        if ( value instanceof List )
        {
            List l = (List)value;
            switch (l.size())
            {
            case 0:  return 0;
            case 1:  value = l.get(0); break;
            default: //fall off in CastClassException
            }
        }

        if ( value instanceof Short)
            return ((Short)value).shortValue();
        if ( value instanceof String)
            return Short.parseShort((String)value);
        if ( value instanceof Byte)
            return ((Byte)value).shortValue();
        if ( value instanceof Double)
            return ((Double)value).shortValue();
        if ( value instanceof Float)
            return ((Float)value).shortValue();
        if ( value instanceof Integer)
            return ((Integer)value).shortValue();
        if ( value instanceof Long)
            return ((Long)value).shortValue();
        if ( value instanceof BigDecimal)
            return ((BigDecimal)value).shortValue();
        if ( value instanceof BigInteger)
            return ((BigInteger)value).shortValue();

        return (Short)value;
    }

    public byte[] getBytes(Property property)
    {
        Object value = get(property);
        if (value==null)
            return null;

        if ( value instanceof List )
        {
            List l = (List)value;
            switch (l.size())
            {
            case 0:  return null;
            case 1:  value = l.get(0); break;
            default: //fall off in CastClassException
            }
        }

        if ( value instanceof BigInteger)
            return ((BigInteger)value).toByteArray();
        if ( value instanceof String)
            return HexBin.stringToBytes((String)value);

        return (byte[])value;
    }

    public BigDecimal getBigDecimal(Property property)
    {
        Object value = get(property);
        if (value==null)
            return null;

        if ( value instanceof List )
        {
            List l = (List)value;
            switch (l.size())
            {
            case 0:  return null;
            case 1:  value = l.get(0); break;
            default: //fall off in CastClassException
            }
        }

        if ( value instanceof BigDecimal)
            return (BigDecimal)value;
        if ( value instanceof String)
            return new BigDecimal((String)value);
        if ( value instanceof Double)
            return BigDecimal.valueOf(((Double)value));
        if ( value instanceof Float)
            return BigDecimal.valueOf(((Float)value));
        if ( value instanceof Integer)
            return BigDecimal.valueOf((Integer)value);
        if ( value instanceof Long)
            return BigDecimal.valueOf(((Long)value));
        if ( value instanceof Short)
            return BigDecimal.valueOf((Short)value);
        if ( value instanceof Byte)
            return BigDecimal.valueOf((Byte)value);
        if ( value instanceof BigInteger)
            return new BigDecimal((BigInteger)value);

        return (BigDecimal)value;
    }

    public BigInteger getBigInteger(Property property)
    {
        Object value = get(property);
        if (value==null)
            return null;

        if ( value instanceof List )
        {
            List l = (List)value;
            switch (l.size())
            {
            case 0:  return null;
            case 1:  value = l.get(0); break;
            default: //fall off in CastClassException
            }
        }

        if ( value instanceof BigInteger)
            return (BigInteger)value;
        if ( value instanceof String)
            return new BigInteger((String)value);
        if ( value instanceof Double)
            return BigInteger.valueOf(((Double)value).longValue());
        if ( value instanceof Float)
            return BigInteger.valueOf(((Float)value).longValue());
        if ( value instanceof Integer)
            return BigInteger.valueOf((Integer)value);
        if ( value instanceof Long)
            return BigInteger.valueOf((Long)value);
        if ( value instanceof Short)
            return BigInteger.valueOf((Short)value);
        if ( value instanceof Byte)
            return BigInteger.valueOf((Byte)value);
        if ( value instanceof BigDecimal)
            return ((BigDecimal)value).toBigInteger();
        if ( value instanceof byte[])
            return new BigInteger((byte[])value);

        return (BigInteger)value;
    }

    public DataObject getDataObject(Property property)
    {
        Object value = get(property);

        if ( value instanceof List )
        {
            List l = (List)value;
            switch (l.size())
            {
            case 0:  return null;
            case 1:  value = l.get(0); break;
            default: //fall off in CastClassException
            }
        }

        return (DataObject)value;
    }

    public Date getDate(Property property)
    {
        Object value = get(property);
        if (value==null)
            return null;

        if ( value instanceof List )
        {
            List l = (List)value;
            switch (l.size())
            {
            case 0:  return null;
            case 1:  value = l.get(0); break;
            default: //fall off in CastClassException
            }
        }

        if ( value instanceof Date)
            return (Date)value;
        if ( value instanceof Long)
            return new Date((Long)value);
        if ( value instanceof String)
        {
            try
            {
                return DataHelperImpl._toDate((String)value);
            }
            catch (IllegalArgumentException e)
            {
                throw new ClassCastException("Unable to convert value '" + value + "' to a java.util.Date");
            }
        }

        return (Date)value;
    }

    public String getString(Property property)
    {
        Object value = get(property);
        if (value==null)
            return null;

        if ( value instanceof String)
            return (String)value;
        if ( value instanceof Boolean)
            return ((Boolean)value).toString();
        if ( value instanceof Byte)
            return ((Byte)value).toString();
        if ( value instanceof Character)
        {
            if (((Character)value).charValue() == '\0')
                return "";
            else
                return ((Character)value).toString();
        }
        if ( value instanceof Double)
            return ((Double)value).toString();
        if ( value instanceof Float)
            return ((Float)value).toString();
        if ( value instanceof Integer)
            return ((Integer)value).toString();
        if ( value instanceof Long)
            return ((Long)value).toString();
        if ( value instanceof Short)
            return ((Short)value).toString();
        if ( value instanceof byte[])
            return HexBin.bytesToString((byte[])value);
        if ( value instanceof BigDecimal)
            return ((BigDecimal)value).toString();
        if ( value instanceof BigInteger)
            return ((BigInteger)value).toString();
        if ( value instanceof Date)
            return DataHelperImpl._toDateTime((Date)value);
        if ( value instanceof List)
        {
            TypeXML pType = _sdoContext.getTypeSystem().getTypeXML(property.getType());
            if (BuiltInTypeSystem.STRINGS.isAssignableFrom(pType))
            {
                List valueList = ((List)value);
                if (valueList.size() == 0) return null;
                String res = "";
                for (int i = 0; i < valueList.size(); i++)
                {
                    if (i>0)
                        res += " ";
                    Object s = valueList.get(i);
                    res += (s == null ? "" : s);
                }
                return res;
            }
            else
            {
                List l = (List) value;
                switch (l.size())
                {
                case 0: return null;
                case 1: return l.get(0).toString();
                default: return l.toString();
                }
            }
        }

        return value.toString(); //(String)value;
    }

    public List getList(Property property)
    {
        return (List)get(property);
    }

    public void setBoolean(Property property, boolean value)
    {
        TypeXML type = PropertyImpl.getPropertyXML(property).getTypeXML();

        if ( BuiltInTypeSystem.BOOLEAN.isAssignableFrom(type) )
            set(property, value);
        else if ( BuiltInTypeSystem.STRING.isAssignableFrom(type) )
            set(property, String.valueOf(value));
        else if ( BuiltInTypeSystem.OBJECT.equals(type) )
            set(property, value);
        else
            throw new ClassCastException("Unable to convert boolean to type '" + type + "'");
    }

    public void setByte(Property property, byte value)
    {
        TypeXML type = PropertyImpl.getPropertyXML(property).getTypeXML();

        if ( BuiltInTypeSystem.BYTE.isAssignableFrom(type) )
            set(property, value);
        else if ( BuiltInTypeSystem.STRING.isAssignableFrom(type) )
            set(property, String.valueOf(value));
        else if ( BuiltInTypeSystem.DOUBLE.isAssignableFrom(type) )
            set(property, Double.valueOf((double)value));
        else if ( BuiltInTypeSystem.FLOAT.isAssignableFrom(type) )
            set(property, Float.valueOf((float)value));
        else if ( BuiltInTypeSystem.LONG.isAssignableFrom(type) )
            set(property, Long.valueOf((long)value));
        else if ( BuiltInTypeSystem.INT.isAssignableFrom(type) )
            set(property, Integer.valueOf((int)value));
        else if ( BuiltInTypeSystem.SHORT.isAssignableFrom(type) )
            set(property, Short.valueOf((short)value));
        else if ( BuiltInTypeSystem.DECIMAL.isAssignableFrom(type) )
            set(property, BigDecimal.valueOf(value));
        else if ( BuiltInTypeSystem.INTEGER.isAssignableFrom(type) )
            set(property, BigInteger.valueOf((long)value));
        else if ( BuiltInTypeSystem.OBJECT.equals(type) )
            set(property, value);
        else
            throw new ClassCastException("Unable to convert byte to type '" + type + "'");
    }

    public void setChar(Property property, char value)
    {
        TypeXML type = PropertyImpl.getPropertyXML(property).getTypeXML();

        if ( BuiltInTypeSystem.CHARACTER.isAssignableFrom(type) )
            set(property, value);
        else if ( BuiltInTypeSystem.STRING.isAssignableFrom(type) )
        {
            if ( value=='\0' )
                set (property, "");
            else
                set(property, String.valueOf(value));
        }
        else if ( BuiltInTypeSystem.OBJECT.equals(type) )
            set(property, value);
        else
            throw new ClassCastException("Unable to convert char to type '" + type + "'");
    }

    public void setDouble(Property property, double value)
    {
        TypeXML type = PropertyImpl.getPropertyXML(property).getTypeXML();

        if ( BuiltInTypeSystem.DOUBLE.isAssignableFrom(type) )
            set(property, value);
        else if ( BuiltInTypeSystem.STRING.isAssignableFrom(type) )
            set(property, String.valueOf(value));
        else if ( BuiltInTypeSystem.BYTE.isAssignableFrom(type) )
            set(property, Byte.valueOf((byte)value));
        else if ( BuiltInTypeSystem.FLOAT.isAssignableFrom(type) )
            set(property, Float.valueOf((float)value));
        else if ( BuiltInTypeSystem.LONG.isAssignableFrom(type) )
            set(property, Long.valueOf((long)value));
        else if ( BuiltInTypeSystem.INT.isAssignableFrom(type) )
            set(property, Integer.valueOf((int)value));
        else if ( BuiltInTypeSystem.SHORT.isAssignableFrom(type) )
            set(property, Short.valueOf((short)value));
        else if ( BuiltInTypeSystem.DECIMAL.isAssignableFrom(type) )
            set(property, BigDecimal.valueOf(value));
        else if ( BuiltInTypeSystem.INTEGER.isAssignableFrom(type) )
            set(property, BigInteger.valueOf((long)value));
        else if ( BuiltInTypeSystem.OBJECT.equals(type) )
            set(property, value);
        else
            throw new ClassCastException("Unable to convert double to type '" + type + "'");
    }

    public void setFloat(Property property, float value)
    {
        TypeXML type = PropertyImpl.getPropertyXML(property).getTypeXML();

        if ( BuiltInTypeSystem.FLOAT.isAssignableFrom(type) )
            set(property, value);
        else if ( BuiltInTypeSystem.STRING.isAssignableFrom(type) )
            set(property, String.valueOf(value));
        else if ( BuiltInTypeSystem.BYTE.isAssignableFrom(type) )
            set(property, Byte.valueOf((byte)value));
        else if ( BuiltInTypeSystem.DOUBLE.isAssignableFrom(type) )
            set(property, Double.valueOf((double)value));
        else if ( BuiltInTypeSystem.LONG.isAssignableFrom(type) )
            set(property, Long.valueOf((long)value));
        else if ( BuiltInTypeSystem.INT.isAssignableFrom(type) )
            set(property, Integer.valueOf((int)value));
        else if ( BuiltInTypeSystem.SHORT.isAssignableFrom(type) )
            set(property, Short.valueOf((short)value));
        else if ( BuiltInTypeSystem.DECIMAL.isAssignableFrom(type) )
            set(property, BigDecimal.valueOf(value));
        else if ( BuiltInTypeSystem.INTEGER.isAssignableFrom(type) )
            set(property, BigInteger.valueOf((long)value));
        else if ( BuiltInTypeSystem.OBJECT.equals(type) )
            set(property, value);
        else
            throw new ClassCastException("Unable to convert float to type '" + type + "'");
    }

    public void setInt(Property property, int value)
    {
        TypeXML type = PropertyImpl.getPropertyXML(property).getTypeXML();

        if ( BuiltInTypeSystem.INT.isAssignableFrom(type) )
            set(property, value);
        else if (type.getInstanceClass() == int.class)
            set(property, value);
        else if ( BuiltInTypeSystem.STRING.isAssignableFrom(type) )
            set(property, String.valueOf(value));
        else if ( BuiltInTypeSystem.BYTE.isAssignableFrom(type) )
            set(property, Byte.valueOf((byte)value));
        else if ( BuiltInTypeSystem.DOUBLE.isAssignableFrom(type) )
            set(property, Double.valueOf((double)value));
        else if ( BuiltInTypeSystem.LONG.isAssignableFrom(type) )
            set(property, Long.valueOf((long)value));
        else if ( BuiltInTypeSystem.FLOAT.isAssignableFrom(type) )
            set(property, Float.valueOf((float)value));
        else if ( BuiltInTypeSystem.SHORT.isAssignableFrom(type) )
            set(property, Short.valueOf((short)value));
        else if ( BuiltInTypeSystem.DECIMAL.isAssignableFrom(type) )
            set(property, BigDecimal.valueOf(value));
        else if ( BuiltInTypeSystem.INTEGER.isAssignableFrom(type) )
            set(property, BigInteger.valueOf((long)value));
        else if ( BuiltInTypeSystem.OBJECT.equals(type) )
            set(property, value);
        else
            throw new ClassCastException("Unable to convert int to type '" + type + "'");
    }

    public void setLong(Property property, long value)
    {
        TypeXML type = PropertyImpl.getPropertyXML(property).getTypeXML();

        if ( BuiltInTypeSystem.LONG.isAssignableFrom(type) )
            set(property, value);
        else if ( BuiltInTypeSystem.STRING.isAssignableFrom(type) )
            set(property, String.valueOf(value));
        else if ( BuiltInTypeSystem.BYTE.isAssignableFrom(type) )
            set(property, Byte.valueOf((byte)value));
        else if ( BuiltInTypeSystem.DOUBLE.isAssignableFrom(type) )
            set(property, Double.valueOf((double)value));
        else if ( BuiltInTypeSystem.INT.isAssignableFrom(type) )
            set(property, Integer.valueOf((int)value));
        else if ( BuiltInTypeSystem.FLOAT.isAssignableFrom(type) )
            set(property, Float.valueOf((float)value));
        else if ( BuiltInTypeSystem.SHORT.isAssignableFrom(type) )
            set(property, Short.valueOf((short)value));
        else if ( BuiltInTypeSystem.DECIMAL.isAssignableFrom(type) )
            set(property, BigDecimal.valueOf(value));
        else if ( BuiltInTypeSystem.INTEGER.isAssignableFrom(type) )
            set(property, BigInteger.valueOf((long)value));
        else if ( BuiltInTypeSystem.DATE.isAssignableFrom(type) )
            set(property, new Date(value));
        else if ( BuiltInTypeSystem.OBJECT.equals(type) )
            set(property, value);
        else
            throw new ClassCastException("Unable to convert long to type '" + type + "'");
    }

    public void setShort(Property property, short value)
    {
        TypeXML type = PropertyImpl.getPropertyXML(property).getTypeXML();

        if ( BuiltInTypeSystem.SHORT.isAssignableFrom(type) )
            set(property, value);
        else if ( BuiltInTypeSystem.STRING.isAssignableFrom(type) )
            set(property, String.valueOf(value));
        else if ( BuiltInTypeSystem.BYTE.isAssignableFrom(type) )
            set(property, Byte.valueOf((byte)value));
        else if ( BuiltInTypeSystem.DOUBLE.isAssignableFrom(type) )
            set(property, Double.valueOf((double)value));
        else if ( BuiltInTypeSystem.INT.isAssignableFrom(type) )
            set(property, Integer.valueOf((int)value));
        else if ( BuiltInTypeSystem.FLOAT.isAssignableFrom(type) )
            set(property, Float.valueOf((float)value));
        else if ( BuiltInTypeSystem.LONG.isAssignableFrom(type) )
            set(property, Long.valueOf((long)value));
        else if ( BuiltInTypeSystem.DECIMAL.isAssignableFrom(type) )
            set(property, BigDecimal.valueOf(value));
        else if ( BuiltInTypeSystem.INTEGER.isAssignableFrom(type) )
            set(property, BigInteger.valueOf((long)value));
        else if ( BuiltInTypeSystem.OBJECT.equals(type) )
            set(property, value);
        else
            throw new ClassCastException("Unable to convert short to type '" + type + "'");
    }

    public void setBytes(Property property, byte[] value)
    {
        TypeXML type = PropertyImpl.getPropertyXML(property).getTypeXML();

        if ( BuiltInTypeSystem.BYTES.isAssignableFrom(type) )
            set(property, value);
        else if ( BuiltInTypeSystem.STRING.isAssignableFrom(type) )
            set(property, HexBin.bytesToString(value));
        else if ( BuiltInTypeSystem.INTEGER.isAssignableFrom(type) )
            set(property, new BigInteger(value));
        else if ( BuiltInTypeSystem.OBJECT.equals(type) )
            set(property, value);
        else
            throw new ClassCastException("Unable to convert byte[] to type '" + type + "'");
    }

    public void setBigDecimal(Property property, BigDecimal value)
    {
        TypeXML type = PropertyImpl.getPropertyXML(property).getTypeXML();

        if ( BuiltInTypeSystem.DECIMAL.isAssignableFrom(type) )
            set(property, value);
        else if ( BuiltInTypeSystem.STRING.isAssignableFrom(type) )
            set(property, String.valueOf(value));
        else if ( BuiltInTypeSystem.BYTE.isAssignableFrom(type) )
            set(property, value.byteValue());
        else if ( BuiltInTypeSystem.DOUBLE.isAssignableFrom(type) )
            set(property, value.doubleValue());
        else if ( BuiltInTypeSystem.FLOAT.isAssignableFrom(type) )
            set(property, value.floatValue());
        else if ( BuiltInTypeSystem.LONG.isAssignableFrom(type) )
            set(property, value.longValue());
        else if ( BuiltInTypeSystem.INT.isAssignableFrom(type) )
            set(property, value.intValue());
        else if ( BuiltInTypeSystem.SHORT.isAssignableFrom(type) )
            set(property, value.shortValue());
        else if ( BuiltInTypeSystem.INTEGER.isAssignableFrom(type) )
            set(property, value.toBigInteger());
        else if ( BuiltInTypeSystem.OBJECT.equals(type) )
            set(property, value);
        else
            throw new ClassCastException("Unable to convert BigDecimal to type '" + type + "'");
    }

    public void setBigInteger(Property property, BigInteger value)
    {
        TypeXML type = PropertyImpl.getPropertyXML(property).getTypeXML();

        if ( BuiltInTypeSystem.INTEGER.isAssignableFrom(type) )
            set(property, value);
        else if ( BuiltInTypeSystem.STRING.isAssignableFrom(type) )
            set(property, String.valueOf(value));
        else if ( BuiltInTypeSystem.BYTE.isAssignableFrom(type) )
            set(property, value.byteValue());
        else if ( BuiltInTypeSystem.DOUBLE.isAssignableFrom(type) )
            set(property, value.doubleValue());
        else if ( BuiltInTypeSystem.FLOAT.isAssignableFrom(type) )
            set(property, value.floatValue());
        else if ( BuiltInTypeSystem.LONG.isAssignableFrom(type) )
            set(property, value.longValue());
        else if ( BuiltInTypeSystem.INT.isAssignableFrom(type) )
            set(property, value.intValue());
        else if ( BuiltInTypeSystem.SHORT.isAssignableFrom(type) )
            set(property, value.shortValue());
        else if ( BuiltInTypeSystem.DECIMAL.isAssignableFrom(type) )
            set(property, new BigDecimal(value));
        else if ( BuiltInTypeSystem.BYTES.isAssignableFrom(type) )
            set(property, value.toByteArray());
        else if ( BuiltInTypeSystem.OBJECT.equals(type) )
            set(property, value);
        else
            throw new ClassCastException("Unable to convert BigInteger to type '" + type + "'");
    }

    public void setDataObject(Property property, DataObject value)
    {
        set(property, value);
    }

    public void setDate(Property property, Date value)
    {
        TypeXML type = PropertyImpl.getPropertyXML(property).getTypeXML();

        if ( BuiltInTypeSystem.DATE.isAssignableFrom(type) )
            set(property, value);
        else if ( BuiltInTypeSystem.STRING.isAssignableFrom(type) )
            set(property, DataHelperImpl._toDateTime(value));
        else if ( BuiltInTypeSystem.LONG.isAssignableFrom(type) )
            set(property, value.getTime());
        else if ( BuiltInTypeSystem.DAY.isAssignableFrom(type) )
            set(property, DataHelperImpl._toDay(value));
        else if ( BuiltInTypeSystem.DATETIME.isAssignableFrom(type) )
            set(property, DataHelperImpl._toDateTime(value));
        else if ( BuiltInTypeSystem.DURATION.isAssignableFrom(type) )
            set(property, DataHelperImpl._toDuration(value));
        else if ( BuiltInTypeSystem.MONTH.isAssignableFrom(type) )
            set(property, DataHelperImpl._toMonth(value));
        else if ( BuiltInTypeSystem.MONTHDAY.isAssignableFrom(type) )
            set(property, DataHelperImpl._toMonthDay(value));
        else if ( BuiltInTypeSystem.TIME.isAssignableFrom(type) )
            set(property, DataHelperImpl._toTime(value));
        else if ( BuiltInTypeSystem.YEAR.isAssignableFrom(type) )
            set(property, DataHelperImpl._toYear(value));
        else if ( BuiltInTypeSystem.YEARMONTH.isAssignableFrom(type) )
            set(property, DataHelperImpl._toYearMonth(value));
        else if ( BuiltInTypeSystem.YEARMONTHDAY.isAssignableFrom(type) )
            set(property, DataHelperImpl._toYearMonthDay(value));
        else if ( BuiltInTypeSystem.OBJECT.equals(type) )
            set(property, value);
        else
            throw new ClassCastException("Unable to convert java.util.Date to type '" + type + "'");
    }

    public void setString(Property property, String value)
    {
        TypeXML type = PropertyImpl.getPropertyXML(property).getTypeXML();

        try
        {
            if ( BuiltInTypeSystem.STRING.isAssignableFrom(type) )
                set(property, value);
            else if ( BuiltInTypeSystem.BOOLEAN.isAssignableFrom(type) )
            {
                //set(property, Boolean.valueOf(value));
                boolean b = getBooleanFromString(value);
                set(property, Boolean.valueOf(b));
            }
            else if ( BuiltInTypeSystem.BYTE.isAssignableFrom(type) )
                set(property, Byte.valueOf(value));
            else if ( BuiltInTypeSystem.CHARACTER.isAssignableFrom(type) )
            {
                if (value == null || value.length() > 1)
                    throw new ClassCastException("Only a String of length 1 may be converted to type '" + type + "'");
                else if ( value.length() == 0 )
                    set(property, '\0');
                else
                    set(property, Character.valueOf(value.charAt(0)));
            }
            else if ( BuiltInTypeSystem.DOUBLE.isAssignableFrom(type) )
                set(property, Double.valueOf(value));
            else if ( BuiltInTypeSystem.FLOAT.isAssignableFrom(type) )
                set(property, Float.valueOf(value));
            else if ( BuiltInTypeSystem.INT.isAssignableFrom(type) )
                set(property, Integer.valueOf(value));
            else if ( BuiltInTypeSystem.LONG.isAssignableFrom(type) )
                set(property, Long.valueOf(value));
            else if ( BuiltInTypeSystem.SHORT.isAssignableFrom(type) )
                set(property, Short.valueOf(value));
            else if ( BuiltInTypeSystem.BYTES.isAssignableFrom(type) )
                set(property, HexBin.stringToBytes(value));
            else if ( BuiltInTypeSystem.DECIMAL.isAssignableFrom(type) )
                set(property, new BigDecimal(value));
            else if ( BuiltInTypeSystem.INTEGER.isAssignableFrom(type) )
                set(property, new BigInteger(value));
            else if ( BuiltInTypeSystem.DATE.isAssignableFrom(type) )
                set(property, DataHelperImpl._toDate(value));
            else if ( BuiltInTypeSystem.DAY.isAssignableFrom(type) )
                set(property, DataHelperImpl._toDay(value));
            else if ( BuiltInTypeSystem.DATETIME.isAssignableFrom(type) )
                set(property, DataHelperImpl._toDateTime(value));
            else if ( BuiltInTypeSystem.DURATION.isAssignableFrom(type) )
                set(property, DataHelperImpl._toDuration(value));
            else if ( BuiltInTypeSystem.MONTH.isAssignableFrom(type) )
                set(property, DataHelperImpl._toMonth(value));
            else if ( BuiltInTypeSystem.MONTHDAY.isAssignableFrom(type) )
                set(property, DataHelperImpl._toMonthDay(value));
            else if ( BuiltInTypeSystem.TIME.isAssignableFrom(type) )
                set(property, DataHelperImpl._toTime(value));
            else if ( BuiltInTypeSystem.YEAR.isAssignableFrom(type) )
                set(property, DataHelperImpl._toYear(value));
            else if ( BuiltInTypeSystem.YEARMONTH.isAssignableFrom(type) )
                set(property, DataHelperImpl._toYearMonth(value));
            else if ( BuiltInTypeSystem.YEARMONTHDAY.isAssignableFrom(type) )
                set(property, DataHelperImpl._toYearMonthDay(value));
            else if ( BuiltInTypeSystem.STRINGS.isAssignableFrom(type) )
            {
                if (value == null)
                    set(property, null);
                else
                    set(property, Arrays.asList(value.trim().split("\\s+")));
            }
            else if ( type.isDataType() )
            {
                // in case this type was defined using javaClass instance property
                // instanceClass has to have a public String constructor
                try
                {
                    set(property, type.getInstanceClass().getConstructor(String.class).newInstance(value));
                }
                catch (InstantiationException e)
                {
                    if ( !setStringOnObjectProperty(property, type, value) )
                    {
                        String msg =
                            "Unable to convert String to type '" + type +
                            "'. Unable to instantiate " + type.getInstanceClass() +
                            ((e.getMessage() != null) ?
                             " due to: " + e.getMessage() : ".");
                        throw new ClassCastException(msg);
                    }
                }
                catch (IllegalAccessException e)
                {
                    if ( !setStringOnObjectProperty(property, type, value) )
                    {
                        String msg =
                            "Unable to convert String to type '" + type +
                            "'. Unable to instantiate " + type.getInstanceClass() +
                            ((e.getMessage() != null) ?
                             " due to: " + e.getMessage() : ".");
                        throw new ClassCastException(msg);
                    }
                }
                catch (InvocationTargetException e)
                {
                    if ( !setStringOnObjectProperty(property, type, value) )
                    {
                        String msg =
                            "Unable to convert String to type '" + type +
                            "'. Unable to instantiate " + type.getInstanceClass() +
                            ((e.getMessage() != null) ?
                             " due to: " + e.getMessage() : ".");
                        throw new ClassCastException(msg);
                    }
                }
                catch (NoSuchMethodException e)
                {
                    if ( !setStringOnObjectProperty(property, type, value) )
                    {
                        String msg =
                            "Unable to convert String to type '" + type +
                            "'. Unable to instantiate " + type.getInstanceClass() +
                            ((e.getMessage() != null) ?
                             " due to: " + e.getMessage() : ".");
                        throw new ClassCastException(msg);
                    }
                }
            }
            else
                throw new ClassCastException("Unable to convert String to type '" + type + "'");
        }
        catch (IllegalArgumentException e)
        {
            throw new ClassCastException("Unable to convert value '" + value + "' to type '" + type + "'");
        }
    }

    private boolean getBooleanFromString(String value)
    {
        if (value == null)
            throw new ClassCastException("Invalid value: " + value);
        switch (value.length())
        {
          case 1:  // "0" or "1"
            final char c = value.charAt(0);
            if ('0' == c) return false;
            if ('1' == c) return true;
            break;
          case 4:  //"true"
            if ('t' == value.charAt(0) &&
                'r' == value.charAt(1) &&
                'u' == value.charAt(2) &&
                'e' == value.charAt(3)) {
                return true;
            }
            break;
          case 5:  //"false"
            if ('f' == value.charAt(0) &&
                'a' == value.charAt(1) &&
                'l' == value.charAt(2) &&
                's' == value.charAt(3) &&
                'e' == value.charAt(4)) {
                return false;
            }
            break;
        }

        //reaching here means an invalid value for boolean
        throw new ClassCastException("Invalid value: " + value);
    }

    private boolean setStringOnObjectProperty(Property property, TypeXML type, Object value)
    {
        if ( BuiltInTypeSystem.OBJECT.equals(type) )
        {
            set(property, value);
            return true;
        }
        return false;
    }

    public void setList(Property property, List value)
    {
        set(property, value);
    }

    public DataObject createDataObject(String propertyName)
    {
        //todo cezar this should be as if a call to
        //createDataObject(propertyName, BuiltInTypeSystem.BEADATAOBJECT.getURI(), BuiltInTypeSystem.BEADATAOBJECT.getName());

        DataObject child = getDataFactoryImpl().createChild(this, propertyName);
        //checkOppositeUniqueObjectConstraint not required
        PropertyXML prop = PropertyImpl.getPropertyXML(getInstanceProperty(propertyName));
        setOppositeProperty(this, prop, child, null);
        return child;
    }

    public DataObject createDataObject(int propertyIndex)
    {
        PropertyXML prop = (PropertyXML)getType().getProperties().get(propertyIndex);
        // checkOppositeUniqueObjectConstraint
        DataObject child = getDataFactoryImpl().createChild(this, prop);
        setOppositeProperty(this, prop, child, null);
        return child;
    }

    public DataObject createDataObject(Property property)
    {
        PropertyXML propertyXML = PropertyImpl.getPropertyXML(property);
        // checkOppositeUniqueObjectConstraint not required
        DataObject child = getDataFactoryImpl().createChild(this, PropertyImpl.getPropertyXML(property));
        setOppositeProperty(this, propertyXML, child, null);
        return child;
    }

    public DataObject createDataObject(String propertyName, String namespaceURI, String typeName)
    {
        TypeXML type = getDataFactoryImpl().getTypeByName(namespaceURI, typeName);
        // checkOppositeUniqueObjectConstraint not required
        DataObject child = getDataFactoryImpl().createChild(this, propertyName, type);
        PropertyXML prop = PropertyImpl.getPropertyXML(getProperty(propertyName));
        setOppositeProperty(this, prop, child, null);
        return child;
    }

    public DataObject createDataObject(int propertyIndex, String namespaceURI, String typeName)
    {
        TypeXML type = getDataFactoryImpl().getTypeByName(namespaceURI, typeName);
        PropertyXML prop = PropertyImpl.getPropertyXML((Property)getType().getProperties().get(propertyIndex));
        // checkOppositeUniqueObjectConstraint not required
        DataObject child = getDataFactoryImpl().createChild(this, propertyIndex, type);
        setOppositeProperty(this, prop, child, null);
        return child;
    }

    public DataObject createDataObject(Property property, Type type)
    {
        PropertyXML propertyXML = PropertyImpl.getPropertyXML(property);
        // checkOppositeUniqueObjectConstraint not required
        DataObject child = getDataFactoryImpl().createChild(this, propertyXML, _sdoContext.getTypeSystem().getTypeXML(type));
        setOppositeProperty(this, propertyXML, child, null);
        return child;
    }

    public void delete()
    {
        // This is tricky: if this object is contained via a non-read-only property, then:
        // - call detach()
        // - disable change tracking
        // - unset all children not contained via read-only properties
        // - re-enable change tracking
        // However, if this object is contained via a read-only property, then:
        // - don't call detach()
        // - don't disable change tracking
        // - unset all children not contained via read-only properties; this will have as
        //     side-effect change logging for all the deletions that will potentially happen
        boolean changeEnabled = false;
        ChangeSummaryImpl c = (ChangeSummaryImpl) getChangeSummary();
        Property contProp = getContainmentProperty();

        if (contProp != null && !contProp.isReadOnly())
        {
            detach();

            if (c != null && c.isLogging())
            {
                changeEnabled = true;
                c.setLogging(false);
            }
        }

        for (Property instProp : (List<Property>)getInstanceProperties())
        {
            if (instProp.isReadOnly())
            {
                // Even if the current property is read-only, we still need to recursively call
                // delete on that respectve child so that it can have all the non-read-only
                // properties cleared
                if (instProp.isContainment() && !instProp.getType().isDataType())
                {
                    if (instProp.isMany())
                    {
                        List<DataObject> childDataObjects = (List<DataObject>)this.getList(instProp);
                        for (DataObject childDataObject : childDataObjects)
                            if (childDataObject != null)
                                childDataObject.delete();
                    }
                    else
                    {
                        DataObject childDataObj = this.getDataObject(instProp);
                        if (childDataObj != null)
                            childDataObj.delete();
                    }
                }
            }
            else
            {
                if (instProp.getType().isDataType())
                    this.unset(instProp);
                else
                {
                    if (instProp.isMany())
                    {
                        List<DataObject> childDataObjects = (List<DataObject>)this.getList(instProp);
                        int size = childDataObjects.size();
                        for (int i=0; i<size; i++)
                        {
                            DataObject childDataObject = childDataObjects.get(0);
                            childDataObjects.remove(0);
                            if (childDataObject != null && instProp.isContainment())
                                childDataObject.delete();
                        }
                    }
                    else
                    {
                        DataObject childDataObj = this.getDataObject(instProp);
                        if (childDataObj != null && instProp.isContainment())
                            childDataObj.delete();
                    }
                }
            }
        }
        if (changeEnabled)
            c.setLogging(true);
    }

    public DataObject getContainer()
    {
        return _container;
    }

    public DataObjectXML getContainerXML()
    {
        return _container;
    }

    public Property getContainmentProperty()
    {
        return _container == null ? null : _containmentProperty;
    }

    public PropertyXML getContainmentPropertyXML()
    {
        return _containmentProperty;
    }

    public DataGraph getDataGraph()
    {
        return _container != null ? getRootObject().getDataGraph() : _dataGraph;
    }

    public Type getType()
    {
        return getTypeXML();
    }

    public List /* Property */ getInstanceProperties()
    {
        return getStore().storeGetInstanceProperties();
    }

    public Property getInstanceProperty(String propertyName)
    {
        return getStore().storeGetProperty(propertyName);
    }

    /**
     * @deprecated
     */
    public List<PropertyXML> getInstancePropertiesXML()
    {
        return getStore().storeGetInstanceProperties();
    }

    /**
     * @deprecated
     */
    public Property getProperty(String propertyName)
    {
        return getInstanceProperty(propertyName);
    }

    public DataObject getRootObject()
    {
        DataObject dobj = this;
        DataObject parent;
        while ((parent = dobj.getContainer()) != null)
            dobj = parent;
        return dobj;
    }

    public ChangeSummary getChangeSummary()
    {
        if (_container == null)
        {
            return _changeSummary;
        }
        else
        {
            ChangeSummary parentcs = _container.getChangeSummary();
            return parentcs != null ? parentcs : _changeSummary;
        }
    }

    public void detach()
    {
        Property contProp = getContainmentProperty();

        if (contProp == null)
            return;

        // If this object is contained via a read-only property, then the spec says it doesn't
        // actually get detached
        if (contProp.isReadOnly() || !contProp.isContainment())
            return;

        if (contProp.isMany())
            getContainer().getList(contProp).remove(this);
        else
            getContainer().unset(contProp);
    }

    public Sequence getSequence()
    {
        return getSequenceXML();
    }

    /**
     * @deprecated
     */
    public Sequence getSequence(Property property)
    {
        throw new UnsupportedOperationException();
    }

    /**
     * @deprecated
     */
    public Sequence getSequence(String path)
    {
        throw new UnsupportedOperationException();
    }

    /**
     * @deprecated
     */
    public Sequence getSequence(int propertyIndex)
    {
        throw new UnsupportedOperationException();
    }

    //DataObjectXML methods
    public TypeXML getTypeXML()
    {
        return _type;
    }

    public SequenceXML getSequenceXML()
    {
        if (getType().isSequenced())
            return getStore().storeGetSequenceXML();
        else
            return null;
    }

    public ListXMLIterator getUnsequencedXML()
    {
        return getStore().storeGetUnsequencedXML();
    }

    public ListXMLIterator getListXMLIterator(PropertyXML property)
    {
        return getStore().storeGetListXMLIterator(property);
    }

    public String getXMLPrefix(int index)
    {
        return getStore().storeSequenceGetXMLPrefix(index);
    }

    public DataObjectXML createDataObjectXML(PropertyXML propertyXML, String prefix, PropertyXML substitution)
    {
        // checkOppositeUniqueObjectConstraint not required
        DataObjectXML child = _sdoContext.getBindingSystem().createChildForDataObject(this, propertyXML, propertyXML.getTypeXML(), prefix, substitution);

        setOppositeProperty(this, propertyXML, child, null);

        return child;
    }

    public DataObjectXML createDataObjectXML(PropertyXML propertyXML, TypeXML typeXML, String prefix, PropertyXML substitution)
    {
        // checkOppositeUniqueObjectConstraint not required
        DataObjectXML child = _sdoContext.getBindingSystem().createChildForDataObject(this, propertyXML, typeXML, prefix, substitution);

        setOppositeProperty(this, propertyXML, child, null);

        return child;
    }

    public void setXML(PropertyXML propertyXML, Object value, String prefix, PropertyXML substitution)
    {
        checkOppositeUniqueObjectConstraint(this, propertyXML, value);
        Object oldOppositeObject = getOldValueForOppositeSet(propertyXML, value);
        getStore().storeSet(propertyXML, value, prefix, substitution);
        setOppositeProperty(this, propertyXML, value, oldOppositeObject);
    }

    // methods used for SDOList implementation
    // we need them since opposites are encapsulated at this level
    protected boolean addNewProperty(PropertyXML propertyXML, Object value, String prefix, PropertyXML substitution)
    {
        checkOppositeUniqueObjectConstraint(this, propertyXML, value);
        boolean result = getStore().storeAddNew(propertyXML, value, prefix, substitution);
        setOppositeProperty(this, propertyXML, value, null);
        return result;
    }

    protected boolean sequenceAddNew(PropertyXML property, int sequenceIndex, Object value,
        String prefix, PropertyXML substitution)
    {
        checkOppositeUniqueObjectConstraint(this, property, value);
        boolean result = getStore().storeSequenceAddNew(sequenceIndex, property, value, prefix, substitution);
        setOppositeProperty(this, property, value, null);
        return result;
    }

    protected void sequenceSet(PropertyXML property, int sequenceIndex, Object value)
    {
        checkOppositeUniqueObjectConstraint(this, property, value);
        Object oldValue = getStore().storeSequenceGetValue(sequenceIndex);
        PropertyXML oldProperty = getStore().storeSequenceGetPropertyXML(sequenceIndex);
        unsetOppositePropertySeq(this, oldProperty, oldValue);

        getStore().storeSequenceSet(sequenceIndex, value);
        setOppositeProperty(this, property, value, null);
    }

    protected void sequenceUnset(PropertyXML property, int sequenceIndex)
    {
        Object value = getStore().storeSequenceGetValue(sequenceIndex);
        unsetOppositePropertySeq(this, property, value);
        getStore().storeSequenceUnset(sequenceIndex);
    }
    // end methods used for SDOList implementation


    public void setContainmentPropertyXML(PropertyXML prop)
    {
        // TODO(radup) Also check that when assigning a DataObject to another, the
        // containment property on the setee is not already set, if it makes sense
        if (_container != null)
            throw new IllegalArgumentException("Could not set containment property on a " +
                "DataObject that is already contained");
        if (!prop.isGlobal())
            throw new IllegalArgumentException("Property \"" + prop.getName() + "\" must be global");
        if (!prop.isContainment())
            throw new IllegalArgumentException("Property \"" + prop.getName() + "\" must be containment");
        setContainmentProperty(prop);
    }

/*    public void _setChangeSummary(ChangeSummary changeSummary)
    {
        // Sets an external change summary, this happens when this object is
        // set on another parent
        // The rules are:
        // - if the parent has a change summary set (its own or inheriten, then this
        //     change summary is set on this object and recursively on all its descendants;
        //     otherwise
        // - if this object has an inherited change summary, then the change summary
        //     is deleted recursively; otherwise
        // - if this object has its own change summary, then it is preserved; otherwise
        // - nothing happens
        if (changeSummary == null && (_changeSummary == null ||
            _changeSummary == findChangeSummary()))
            return;
        if (changeSummary == this._changeSummary)
            return;
        this._changeSummary = changeSummary;
        for (Object o : getInstanceProperties())
        {
            Property p = (Property) o;
            if (!p.getType().isDataType())
            {
                if (p.isMany())
                {
                    List l = getList(p);
                    for (Object oo : l)
                        ((DataObjectImpl) oo)._setChangeSummary(changeSummary);
                }
                else
                {
                    ((DataObjectImpl) get(p))._setChangeSummary(changeSummary);
                }
            }
        }
    }
*/
    // abstract methods
    public abstract Store getStore();

    public void setOppositeIgnore(boolean ignoreOpposites)
    {
        _ignoreOpposites = ignoreOpposites;
    }

    // protected methods
    protected void setOppositeProperty(DataObjectXML parent, PropertyXML prop, Object newValue, Object oldValue)
    {
        if (_ignoreOpposites)
            return;

        if (prop==null)
            return;

        PropertyXML oppositeProp = prop.getOppositeXML();
        if (oppositeProp==null)
            return;

        // check constraint
        assert newValue==null || newValue instanceof DataObject : "Properties that are bidirectional require type.dataType=false";
        // check constraint
        assert !(prop.isContainment() && oppositeProp.isContainment()) : "Properties that are bidirectional require that no more than one end has containment=true";
        // check constraint
        assert prop.isReadOnly() == oppositeProp.isReadOnly() : "Properties that are bidirectional require that both ends have the same value for readOnly";
        // check constraint
        assert (!prop.isContainment() && !oppositeProp.isContainment()) ||
            (prop.isContainment() && !oppositeProp.isMany()) || (oppositeProp.isContainment() && !prop.isMany()) :
            "Properties that are bidirectional with containment require that the noncontainment Property has many=false.";

        // we're using the Store here to avoid recursivity

        if (newValue ==null)
        {
            if (oldValue!=null)
            {
                if ( !prop.isMany() )
                {
                    if ( oppositeProp.isNullable() )
                        ((Store)oldValue).storeSet(oppositeProp, newValue /*i.e. null*/);
                    else
                        ((Store)oldValue).storeUnset(oppositeProp);
                }
                else
                {
                    // this is the case when the entire list is to be set to null
                    // need to null or unset all objests' oposite properties pointing to parent
                    List<DataObjectXML> listOfPropValues = (List<DataObjectXML>)oldValue;
                    for ( DataObjectXML itemInListOfPropValues : listOfPropValues )
                    {
                        if (itemInListOfPropValues == null )
                            continue;
                        
                        // and since prop.isMany(), oppositeProp must be !isMany() so we can just set or unset it directly
                        assert !oppositeProp.isMany(); 
                        if ( oppositeProp.isNullable() )
                            ((Store)itemInListOfPropValues).storeSet(oppositeProp, newValue /*i.e. null*/);
                        else
                            ((Store)itemInListOfPropValues).storeUnset(oppositeProp);
                    }
                }
            }
        }
        else
        {
            Store valueStore = (Store) newValue;
            if ( oppositeProp.isContainment() && oppositeProp.isMany() )
            {
                //expensive constraint check: Values of bidirectional Properties with many=true must be unique objects within the same list.
                //checkUniqueObjectsConstraint(valueStore, oppositeProp, (Store)parent);

                valueStore.storeAddNew(oppositeProp, parent);
            }
            else
                if (!oppositeProp.isMany())
                    valueStore.storeSet(oppositeProp, parent);
                else
                {
                    List li = (List)valueStore.storeGet(oppositeProp);
                    if (li.contains(parent))
                        return;
                    else
                        valueStore.storeAddNew(oppositeProp, parent);
                }

            assert assertOppositeProperties((Store)parent, prop, valueStore) : "Opposite property not set corectly.";
        }
    }

    /**
     * Only if newValue is null and prop has opposite prop returns the old object
     * else null
     */
    private Object getOldValueForOppositeSet(PropertyXML propXml, Object newValue)
    {
        if (newValue != null)
            return null;

        if ( propXml.getOppositeXML() == null )
            return null;

        if ( !propXml.isMany() )
            return getStore().storeGet(propXml);
        else
        {   // same thing but this will be a list
            List oldValueList = (List)(getStore().storeGet(propXml));
            return new ArrayList(oldValueList); //make a copy of live list
        }
    }

    private static boolean assertOppositeProperties(Store dObjStore, PropertyXML prop, Store oppoStore)
    {
        PropertyXML oppositeProp = prop.getOppositeXML();
        int dObjStoreSize = dObjStore.storeSequenceSize();
        for (int i = 0; i<dObjStoreSize; i++ )
        {
            if (prop.equals(dObjStore.storeSequenceGetPropertyXML(i)) && dObjStore.storeSequenceGetValue(i)==oppoStore)
            {
                int oppoStoreSize = oppoStore.storeSequenceSize();
                for (int j = 0; j<oppoStoreSize; j++ )
                {
                    if (oppositeProp.equals(oppoStore.storeSequenceGetPropertyXML(j)) && oppoStore.storeSequenceGetValue(j)==dObjStore)
                    {
                        assert dObjStore.storeSequenceGetValue(i)==oppoStore;
                        assert oppoStore.storeSequenceGetValue(j)==dObjStore;
                        return true;
                    }
                }
            }
        }
        assert false : "Values of opposite properties not set corectly!";
        return false;
    }

    protected static void checkOppositeUniqueObjectConstraint(DataObject parent, PropertyXML prop, Object value)
    {
        if (prop==null)
            return;

        PropertyXML oppositeProp = prop.getOppositeXML();
        if (prop.getOppositeXML()==null)
            return;

        if ( value == null )
            return; // do nothing

        // value can be a DataObject or a list if it has an opposite prop
        if (value instanceof Store)
        {
            Store valueStore = (Store)value;
            if ( oppositeProp.isMany() )
            {
                //expensive constraint check: Values of bidirectional Properties with many=true must be unique objects within the same list.
                checkUniqueObjectsConstraint(valueStore, oppositeProp, (Store)parent);
            }

            if ( prop.isMany() )
            {
                //expensive constraint check: Values of bidirectional Properties with many=true must be unique objects within the same list.
                checkUniqueObjectsConstraint((Store)parent, prop, valueStore);
            }
        }
        else if (value instanceof List)
        {
            // all values in the list must be unique
            List list = (List)value;
            int size = list.size();
            for (int i = 0; i < size; i++)
            {
                for (int j = i+1; j < size; j++)
                {
                    if (list.get(i)==list.get(j))
                        throw new IllegalArgumentException("When setting a list for properties with many=true must be unique objects within the list.");
                }                
            }
        }
        else
            throw new IllegalStateException("Unhandled instance type: " + (value != null ? value.getClass() : value ));
    }

    private static void checkUniqueObjectsConstraint(Store dObjStore, PropertyXML prop, Store oppoStore)
    {
        int dObjStoreSize = dObjStore.storeSequenceSize();
        for (int i = 0; i<dObjStoreSize; i++ )
        {
            if (prop.equals(dObjStore.storeSequenceGetPropertyXML(i)) && dObjStore.storeSequenceGetValue(i)==oppoStore)
            {
                throw new IllegalArgumentException("Values of bidirectional Properties with many=true must be unique objects within the same list.");
            }
        }
    }


    protected void unsetOppositeProperty(DataObjectXML parent, PropertyXML prop, Object value)
    {
        if (_ignoreOpposites)
            return;

        if (prop==null)
            return;

        PropertyXML oppositeProp = prop.getOppositeXML();
        if (oppositeProp==null || value==null)
            return;

        // check constraint
        assert !prop.getType().isDataType() && !oppositeProp.getType().isDataType() : "Properties that are bidirectional require type.dataType=false";
        assert value instanceof DataObject || value instanceof List : "Properties that are bidirectional require DataObject instances.";
        // check constraint
        assert !(prop.isContainment() && oppositeProp.isContainment()) : "Properties that are bidirectional require that no more than one end has containment=true";
        // check constraint
        assert prop.isReadOnly() == oppositeProp.isReadOnly() : "Properties that are bidirectional require that both ends have the same value for readOnly";
        // check constraint
        assert (!prop.isContainment() && !oppositeProp.isContainment()) ||
            (prop.isContainment() && !oppositeProp.isMany()) || (oppositeProp.isContainment() && !prop.isMany()) :
            "Properties that are bidirectional with containment require that the noncontainment Property has many=false.";

        if (!prop.isMany())
        {
            DataObject oppositeInstance = (DataObject)value;

//            if ( !oppositeProp.isMany() )
//            {
//                oppositeInstance.unset(oppositeProp);
//            }
//            else
//            {
//                oppositeInstance.getList(oppositeProp).remove(parent);
//            }
            unsetOppositePropertySeq(parent, prop, oppositeInstance);
        }
        else
        {
            List valueList = (List)value;
            for (int i = 0; i < valueList.size(); i++)
            {
                DataObject oppositeInstance = (DataObject) valueList.get(i);
//                if ( !oppositeProp.isMany() )
//                {
//                    oppositeInstance.unset(oppositeProp);
//                }
//                else
//                {
//                    oppositeInstance.getList(oppositeProp).remove(parent);
//                }
                unsetOppositePropertySeq(parent, prop, oppositeInstance);
            }
        }
    }

    protected void unsetOppositePropertySingle(DataObjectXML parent, PropertyXML prop, Object value)
    {
        if (_ignoreOpposites)
            return;

        if (prop==null)
            return;

        PropertyXML oppositeProp = prop.getOppositeXML();
        if (oppositeProp==null || value==null)
            return;

        // check constraint
        assert value instanceof DataObject : "Properties that are bidirectional require type.dataType=false";
        // check constraint
        assert !(prop.isContainment() && oppositeProp.isContainment()) : "Properties that are bidirectional require that no more than one end has containment=true";
        // check constraint
        assert prop.isReadOnly() == oppositeProp.isReadOnly() : "Properties that are bidirectional require that both ends have the same value for readOnly";
        // check constraint
        assert (!prop.isContainment() && !oppositeProp.isContainment()) ||
            (prop.isContainment() && !oppositeProp.isMany()) || (oppositeProp.isContainment() && !prop.isMany()) :
            "Properties that are bidirectional with containment require that the noncontainment Property has many=false.";

        DataObject oppositeInstance = (DataObject)value;

        if ( !oppositeProp.isMany() )
        {
            oppositeInstance.unset(oppositeProp);
        }
        else
        {
            oppositeInstance.getList(oppositeProp).remove(parent);
        }
    }

    /**
     * Unsets the opposite property of property. I.e. the property of toValue that has the value fromDObj
     */
    protected void unsetOppositePropertySeq(DataObjectXML fromDObj, PropertyXML prop, Object toValue)
    {
        if (_ignoreOpposites)
            return;

        if (prop==null)
            return;

        PropertyXML oppositeProp = prop.getOppositeXML();
        if (oppositeProp==null || toValue==null)
            return;

        // check constraint
        assert toValue instanceof DataObject : "Properties that are bidirectional require type.dataType=false";
        // check constraint
        assert !(prop.isContainment() && oppositeProp.isContainment()) : "Properties that are bidirectional require that no more than one end has containment=true";
        // check constraint
        assert prop.isReadOnly() == oppositeProp.isReadOnly() : "Properties that are bidirectional require that both ends have the same value for readOnly";
        // check constraint
        assert (!prop.isContainment() && !oppositeProp.isContainment()) ||
            (prop.isContainment() && !oppositeProp.isMany()) || (oppositeProp.isContainment() && !prop.isMany()) :
            "Properties that are bidirectional with containment require that the noncontainment Property has many=false.";


        Store oppoStore = ((DataObjectImpl) toValue).getStore();
        int size = oppoStore.storeSequenceSize();
        for (int i=0; i<size; i++)
        {
            if (oppositeProp.equals(oppoStore.storeSequenceGetPropertyXML(i)) && fromDObj==oppoStore.storeSequenceGetValue(i))
            {
                oppoStore.storeSequenceUnset(i);
                return;
            }
        }
        assert false : "Since this method is called because of opposite value the previous loop has to find it";
    }

    // private methods
    private ChangeSummary initChangeSummary()
    {
        ChangeSummary result = null;
        for (PropertyXML p : _type.getPropertiesXML())
        {
            if (p.getType() == BuiltInTypeSystem.CHANGESUMMARYTYPE)
            {
               result = new ChangeSummaryImpl(this);
               getStore().storeSet(p, result);
            }
        }
        return result;
    }

    private PropertyXML getPropertyForPropertyIndex(int propertyIndex)
    {
        List props = getType().getProperties();
        if (propertyIndex>=0 && propertyIndex < props.size())
            return (PropertyXML)props.get(propertyIndex);
        else
        {
            List instanceProps = getInstanceProperties();
            if (propertyIndex>=0 && propertyIndex < instanceProps.size())
                return (PropertyXML)instanceProps.get(propertyIndex);
            else
                throw new IllegalArgumentException("Index out of bounds: -1 > " + propertyIndex + " > " + instanceProps.size());
        }
    }

    void clearContainer()
    {
        _container = null;
        _containmentProperty = null;
        _dataGraph = null;
        _changeSummary = null;
    }

    //todo cezar change to use type's context
    private DataFactoryImpl getDataFactoryImpl()
    {
        return ((DataFactoryImpl)_sdoContext.getDataFactory());
    }

    // java Serialization

    // java Serialization will first call this method which will replace the object to be persisted with
    // an ExternalizableDelegator object that delegates to a ResolvableImpl object
    protected Object writeReplace()
        throws ObjectStreamException
    {
        return new ExternalizableDelegator(this);
    }
}
