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
package davos.sdo.impl.path;

import javax.sdo.Property;
import javax.sdo.DataObject;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Date;
import java.util.Arrays;
import java.lang.reflect.InvocationTargetException;

import davos.sdo.impl.type.PropertyImpl;
import davos.sdo.impl.type.BuiltInTypeSystem;
import davos.sdo.impl.helpers.DataHelperImpl;
import davos.sdo.TypeXML;
import org.apache.xmlbeans.impl.util.HexBin;

/**
 * @author Cezar Andrei (cezar dot andrei at gmail dot com)
 *         Date: Jul 7, 2006
 */
public abstract class Step
    implements PathPlan
{
    protected Step _childStep;

    public abstract boolean execute(Path context);
    
    public void optimize()
    {}

    public boolean isSimplePropertyName()
    {
        return false;
    }

    public String getSimplePropertyName()
    {
        return null;
    }

    void setChildStep(Step childStep)
    {
        _childStep = childStep;
    }

    String childToString()
    {
        return ( _childStep != null ? "/" + _childStep.toString() : "");
    }

    public static class RootStep
        extends Step
    {
        public boolean execute(Path context)
        {
            DataObject root = context.getParentNode().getRootObject();
            assert root!= null;
            context.setParentNode(root);

            assert _childStep!=null;

            return _childStep.execute(context);
        }

        public String toString()
        {
            return "/" + childToString();
        }
    }

    public static class PropertyStep
        extends Step
    {
        String _propertyName;
        PropertyStep(String property)
        {
            _propertyName = property;
        }

        public boolean execute(Path context)
        {
            DataObject parent = context.getParentNode();
            Property prop = parent.getInstanceProperty(_propertyName);

            if (_childStep!=null)
            {
                // it's not the last step
                if (prop==null)
                    return false;

                if (prop.isMany())
                {
                    List items = parent.getList(prop);
                    for ( Object o : items)
                    {
                        if ( !(o instanceof DataObject))
                            continue;

                        DataObject item = (DataObject)o;

                        if (!checkValue(item))
                            continue;

                        context.setParentNode(item);

                        if (_childStep.execute(context))
                            return true;  // succesfull return
                    }

                    return false;
                }
                else
                {
                    Object o = parent.get(prop);
                    if ( !(o instanceof DataObject))
                        return false;

                    DataObject item = (DataObject)o;

                    if (!checkValue(item))
                        return false;

                    context.setParentNode(item);

                    return _childStep.execute(context);
                }
            }

            // it's the last step
            if ( prop==null && context.getOperation()==Path.OP_SET )
            {
                if ( parent.getType().isOpen() )
                    prop = PropertyImpl.createOnDemand(context.getSDOContext(),
                        _propertyName, context.getValue(), false);
                else
                    throw new IllegalArgumentException("Property '" + _propertyName +
                        "' not allowed on this type: " + parent.getType());
            }

            if ( prop==null )
                return false;

            if ( hasCheck() && prop.isMany() )
            {
                List items = parent.getList(prop);
                for ( Object item : items )
                {
                    if (executeItem(context, parent, prop, item))
                        return true;
                }

                return false;
            }

            return executeItem(context, parent, prop, parent.get(prop));
        }

        private boolean executeItem(Path context, DataObject parent, Property prop, Object item)
        {
            if( !checkValue(item) )
                return false;

            switch ( context.getOperation() )
            {
            case Path.OP_GET:
                context.setValue(item);
                return true;
            case Path.OP_SET:
                executeItemSet(context, parent, prop);
                return true;
            case Path.OP_ISSET:
                boolean isSet = parent.isSet(prop);
                context.setValue(isSet);
                return isSet;
            case Path.OP_UNSET:
                parent.unset(prop);
                return true;
            default:
                throw new IllegalStateException("Unknown operation :" + context.getOperation());
            }
        }

        private void executeItemSet(Path context, DataObject parent, Property prop)
        {
            assert context.getOperation() == Path.OP_SET;

            switch(context.getTypedSetter())
            {
            case Path.SETTER_UNTYPED:               
                parent.set(prop, context.getValue());
                return;
            case Path.SETTER_BOOLEAN:
                parent.setBoolean(prop, (Boolean)(context.getValue()));
                return;
            case Path.SETTER_BYTE:
                parent.setByte(prop, (Byte)(context.getValue()));
                return;
            case Path.SETTER_CHAR:
                parent.setChar(prop, (Character)(context.getValue()));
                return;
            case Path.SETTER_DOUBLE:
                parent.setDouble(prop, (Double)(context.getValue()));
                return;
            case Path.SETTER_INT:
                parent.setInt(prop, (Integer)(context.getValue()));
                return;
            case Path.SETTER_FLOAT:
                parent.setFloat(prop, (Float)(context.getValue()));
                return;
            case Path.SETTER_LONG:
                parent.setLong(prop, (Long)(context.getValue()));
                return;
            case Path.SETTER_SHORT:
                parent.setShort(prop, (Short)(context.getValue()));
                return;
            case Path.SETTER_BYTES:
                parent.setBytes(prop, (byte[])(context.getValue()));
                return;
            case Path.SETTER_BIGDECIMAL:
                parent.setBigDecimal(prop, (BigDecimal)(context.getValue()));
                return;
            case Path.SETTER_BIGINTEGER:
                parent.setBigInteger(prop, (BigInteger)(context.getValue()));
                return;
            case Path.SETTER_DATAOBJECT:
                parent.setDataObject(prop, (DataObject)(context.getValue()));
                return;
            case Path.SETTER_DATE:
                parent.setDate(prop, (Date)(context.getValue()));
                return;
            case Path.SETTER_STRING:
                parent.setString(prop, (String)(context.getValue()));
                return;
            case Path.SETTER_LIST:
                parent.setList(prop, (List)(context.getValue()));
                return;
            default:
                throw new IllegalStateException("Unknown typed setter code :" + context.getOperation());
            }
        }

        boolean hasCheck()
        {
            return false;
        }

        boolean checkValue(Object obj)
        {
            return true; // doesn't have an attribute to check against
        }

        public boolean isSimplePropertyName()
        {
            return this.getClass()==PropertyStep.class && _childStep==null;
        }

        public String getSimplePropertyName()
        {
            return isSimplePropertyName() ? _propertyName : null;
        }

        public String toString()
        {
            return _propertyName + childToString();
        }
    }

    public static class Index0Step
        extends PropertyStep
    {
        int _index;
        Index0Step(String property, int index)
        {
            super(property);
            _index = index;
        }

        public boolean execute(Path context)
        {
            DataObject parent = context.getParentNode();
            Property prop = parent.getInstanceProperty(_propertyName);

            if (prop==null)
                return false;

            if (prop.isMany())
            {
                List<Object> items = parent.getList(prop);

                if (_index<items.size())
                {
                    Object o = items.get(_index);

                    if (_childStep!=null)
                    {
                        if ( !(o instanceof DataObject))
                            return false;

                        DataObject item = (DataObject)o;

                        context.setParentNode(item);

                        if (_childStep.execute(context))
                            return true;  // succesfull return
                    }
                    else
                    {
                        // it's the last step
                        switch (context.getOperation())
                        {
                        case Path.OP_GET:
                            context.setValue(o);
                            return true;
                        case Path.OP_SET:

                            detachValueIfContainment(context.getValue(), prop);

                            Object typedValue = getTypedValue(context, prop);

                            items.set(_index, typedValue);
                            return true;


                        case Path.OP_ISSET:
                            context.setValue(/*_index<items.size() in this case is*/ true);
                            return true;
                        case Path.OP_UNSET:
                            items.remove(_index);
                            return true;
                        default:
                            throw new IllegalStateException("Unknown operation :" + context.getOperation());
                        }
                    }
                }
                else if (context.getOperation()==Path.OP_ISSET)
                {
                    context.setValue(/*_index<items.size() in this case is*/ false);
                    return false;
                }
                else if (context.getOperation()==Path.OP_SET)
                {
                    throw new IndexOutOfBoundsException("Trying to set index:'" + _index +
                        "' on a list of size:'" + items.size() + "'.");
                }

                return false;
            }
            else
            {
                Object o = parent.get(prop);
                if ( !(o instanceof DataObject))
                    return false;

                DataObject item = (DataObject)o;
                context.setParentNode(item);

                return _childStep.execute(context);
            }
        }

        private Object getTypedValue(Path context, Property prop)
        {
            Object typedValue = null;
            TypeXML type = null;

            switch (context.getTypedSetter())
            {
            case Path.SETTER_BOOLEAN:
                boolean booleanValue = (Boolean)context.getValue();
                type = PropertyImpl.getPropertyXML(prop).getTypeXML();

                if ( BuiltInTypeSystem.BOOLEAN.isAssignableFrom(type) )
                    typedValue = booleanValue;
                else if ( BuiltInTypeSystem.STRING.isAssignableFrom(type) )
                    typedValue = String.valueOf(booleanValue);
                else
                    throw new ClassCastException("Cannot convert boolean to type '" + type + "'");
                break;

            case Path.SETTER_BYTE:
                byte byteValue = (Byte)context.getValue();
                type = PropertyImpl.getPropertyXML(prop).getTypeXML();

                if ( BuiltInTypeSystem.BYTE.isAssignableFrom(type) )
                    typedValue = byteValue;
                else if ( BuiltInTypeSystem.STRING.isAssignableFrom(type) )
                    typedValue = String.valueOf(byteValue);
                else if ( BuiltInTypeSystem.DOUBLE.isAssignableFrom(type) )
                    typedValue = Double.valueOf((double)byteValue);
                else if ( BuiltInTypeSystem.FLOAT.isAssignableFrom(type) )
                    typedValue = Float.valueOf((float)byteValue);
                else if ( BuiltInTypeSystem.LONG.isAssignableFrom(type) )
                    typedValue = Long.valueOf((long)byteValue);
                else if ( BuiltInTypeSystem.INT.isAssignableFrom(type) )
                    typedValue = Integer.valueOf((int)byteValue);
                else if ( BuiltInTypeSystem.SHORT.isAssignableFrom(type) )
                    typedValue = Short.valueOf((short)byteValue);
                else
                    throw new ClassCastException("Cannot convert byte to type '" + type + "'");
                break;

            case Path.SETTER_CHAR:
                char charValue = (Character)context.getValue();
                type = PropertyImpl.getPropertyXML(prop).getTypeXML();

                if ( BuiltInTypeSystem.CHARACTER.isAssignableFrom(type) )
                    typedValue = charValue;
                else if ( BuiltInTypeSystem.STRING.isAssignableFrom(type) )
                    typedValue = String.valueOf(charValue);
                else
                    throw new ClassCastException("Cannot convert char to type '" + type + "'");
                break;

            case Path.SETTER_DOUBLE:
                double doubleValue = (Double)context.getValue();
                type = PropertyImpl.getPropertyXML(prop).getTypeXML();

                if ( BuiltInTypeSystem.DOUBLE.isAssignableFrom(type) )
                    typedValue = doubleValue;
                else if ( BuiltInTypeSystem.STRING.isAssignableFrom(type) )
                    typedValue = String.valueOf(doubleValue);
                else if ( BuiltInTypeSystem.BYTE.isAssignableFrom(type) )
                    typedValue = Byte.valueOf((byte)doubleValue);
                else if ( BuiltInTypeSystem.FLOAT.isAssignableFrom(type) )
                    typedValue = Float.valueOf((float)doubleValue);
                else if ( BuiltInTypeSystem.LONG.isAssignableFrom(type) )
                    typedValue = Long.valueOf((long)doubleValue);
                else if ( BuiltInTypeSystem.INT.isAssignableFrom(type) )
                    typedValue = Integer.valueOf((int)doubleValue);
                else if ( BuiltInTypeSystem.SHORT.isAssignableFrom(type) )
                    typedValue = Short.valueOf((short)doubleValue);
                else if ( BuiltInTypeSystem.DECIMAL.isAssignableFrom(type) )
                    typedValue = BigDecimal.valueOf(doubleValue);
                else if ( BuiltInTypeSystem.INTEGER.isAssignableFrom(type) )
                    typedValue = BigInteger.valueOf((long)doubleValue);
                else
                    throw new ClassCastException("Cannot convert double to type '" + type + "'");
                break;

            case Path.SETTER_INT:
                int intValue = (Integer)context.getValue();
                type = PropertyImpl.getPropertyXML(prop).getTypeXML();

                if ( BuiltInTypeSystem.INT.isAssignableFrom(type) )
                    typedValue = intValue;
                else if (type.getInstanceClass() == int.class)
                    typedValue = intValue;
                else if ( BuiltInTypeSystem.STRING.isAssignableFrom(type) )
                    typedValue = String.valueOf(intValue);
                else if ( BuiltInTypeSystem.BYTE.isAssignableFrom(type) )
                    typedValue = Byte.valueOf((byte)intValue);
                else if ( BuiltInTypeSystem.DOUBLE.isAssignableFrom(type) )
                    typedValue = Double.valueOf((double)intValue);
                else if ( BuiltInTypeSystem.LONG.isAssignableFrom(type) )
                    typedValue = Long.valueOf((long)intValue);
                else if ( BuiltInTypeSystem.FLOAT.isAssignableFrom(type) )
                    typedValue = Float.valueOf((float)intValue);
                else if ( BuiltInTypeSystem.SHORT.isAssignableFrom(type) )
                    typedValue = Short.valueOf((short)intValue);
                else if ( BuiltInTypeSystem.DECIMAL.isAssignableFrom(type) )
                    typedValue = BigDecimal.valueOf(intValue);
                else if ( BuiltInTypeSystem.INTEGER.isAssignableFrom(type) )
                    typedValue = BigInteger.valueOf((long)intValue);
                else
                    throw new ClassCastException("Cannot convert int to type '" + type + "'");
                break;

            case Path.SETTER_FLOAT:
                float floatValue = (Float)context.getValue();
                type = PropertyImpl.getPropertyXML(prop).getTypeXML();

                if ( BuiltInTypeSystem.FLOAT.isAssignableFrom(type) )
                    typedValue = floatValue;
                else if ( BuiltInTypeSystem.STRING.isAssignableFrom(type) )
                    typedValue = String.valueOf(floatValue);
                else if ( BuiltInTypeSystem.BYTE.isAssignableFrom(type) )
                    typedValue = Byte.valueOf((byte)floatValue);
                else if ( BuiltInTypeSystem.DOUBLE.isAssignableFrom(type) )
                    typedValue = Double.valueOf((double)floatValue);
                else if ( BuiltInTypeSystem.LONG.isAssignableFrom(type) )
                    typedValue = Long.valueOf((long)floatValue);
                else if ( BuiltInTypeSystem.INT.isAssignableFrom(type) )
                    typedValue = Integer.valueOf((int)floatValue);
                else if ( BuiltInTypeSystem.SHORT.isAssignableFrom(type) )
                    typedValue = Short.valueOf((short)floatValue);
                else if ( BuiltInTypeSystem.DECIMAL.isAssignableFrom(type) )
                    typedValue = BigDecimal.valueOf(floatValue);
                else if ( BuiltInTypeSystem.INTEGER.isAssignableFrom(type) )
                    typedValue = BigInteger.valueOf((long)floatValue);
                else
                    throw new ClassCastException("Cannot convert float to type '" + type + "'");
                break;

            case Path.SETTER_LONG:
                long longValue = (Long)context.getValue();
                type = PropertyImpl.getPropertyXML(prop).getTypeXML();

                if ( BuiltInTypeSystem.LONG.isAssignableFrom(type) )
                    typedValue = longValue;
                else if ( BuiltInTypeSystem.STRING.isAssignableFrom(type) )
                    typedValue = String.valueOf(longValue);
                else if ( BuiltInTypeSystem.BYTE.isAssignableFrom(type) )
                    typedValue = Byte.valueOf((byte)longValue);
                else if ( BuiltInTypeSystem.DOUBLE.isAssignableFrom(type) )
                    typedValue = Double.valueOf((double)longValue);
                else if ( BuiltInTypeSystem.INT.isAssignableFrom(type) )
                    typedValue = Integer.valueOf((int)longValue);
                else if ( BuiltInTypeSystem.FLOAT.isAssignableFrom(type) )
                    typedValue = Float.valueOf((float)longValue);
                else if ( BuiltInTypeSystem.SHORT.isAssignableFrom(type) )
                    typedValue = Short.valueOf((short)longValue);
                else if ( BuiltInTypeSystem.DECIMAL.isAssignableFrom(type) )
                    typedValue = BigDecimal.valueOf(longValue);
                else if ( BuiltInTypeSystem.INTEGER.isAssignableFrom(type) )
                    typedValue = BigInteger.valueOf((long)longValue);
                else if ( BuiltInTypeSystem.DATE.isAssignableFrom(type) )
                    typedValue = new Date(longValue);
                else
                    throw new ClassCastException("Cannot convert long to type '" + type + "'");
                break;

            case Path.SETTER_SHORT:
                short shortValue = (Short)context.getValue();
                type = PropertyImpl.getPropertyXML(prop).getTypeXML();

                if ( BuiltInTypeSystem.SHORT.isAssignableFrom(type) )
                    typedValue = shortValue;
                else if ( BuiltInTypeSystem.STRING.isAssignableFrom(type) )
                    typedValue = String.valueOf(shortValue);
                else if ( BuiltInTypeSystem.BYTE.isAssignableFrom(type) )
                    typedValue = Byte.valueOf((byte)shortValue);
                else if ( BuiltInTypeSystem.DOUBLE.isAssignableFrom(type) )
                    typedValue = Double.valueOf((double)shortValue);
                else if ( BuiltInTypeSystem.INT.isAssignableFrom(type) )
                    typedValue = Integer.valueOf((int)shortValue);
                else if ( BuiltInTypeSystem.FLOAT.isAssignableFrom(type) )
                    typedValue = Float.valueOf((float)shortValue);
                else if ( BuiltInTypeSystem.LONG.isAssignableFrom(type) )
                    typedValue = Long.valueOf((long)shortValue);
                else
                    throw new ClassCastException("Cannot convert short to type '" + type + "'");
                break;

            case Path.SETTER_BYTES:
                byte[] bytesValue = (byte[])context.getValue();
                type = PropertyImpl.getPropertyXML(prop).getTypeXML();

                if ( BuiltInTypeSystem.BYTES.isAssignableFrom(type) )
                    typedValue = bytesValue;
                else if ( BuiltInTypeSystem.STRING.isAssignableFrom(type) )
                    typedValue = HexBin.bytesToString(bytesValue);
                else if ( BuiltInTypeSystem.INTEGER.isAssignableFrom(type) )
                    typedValue = new BigInteger(bytesValue);
                else
                    throw new ClassCastException("Cannot convert byte[] to type '" + type + "'");
                break;

            case Path.SETTER_BIGDECIMAL:
                BigDecimal bigDecimalValue = (BigDecimal)context.getValue();
                type = PropertyImpl.getPropertyXML(prop).getTypeXML();

                if ( BuiltInTypeSystem.DECIMAL.isAssignableFrom(type) )
                    typedValue = bigDecimalValue;
                else if ( BuiltInTypeSystem.STRING.isAssignableFrom(type) )
                    typedValue = String.valueOf(bigDecimalValue);
//        else if ( BuiltInTypeSystem.BYTE.isAssignableFrom(type) )
//            typedValue = bigDecimalValue.byteValue());
                else if ( BuiltInTypeSystem.DOUBLE.isAssignableFrom(type) )
                    typedValue = bigDecimalValue.doubleValue();
                else if ( BuiltInTypeSystem.FLOAT.isAssignableFrom(type) )
                    typedValue = bigDecimalValue.floatValue();
                else if ( BuiltInTypeSystem.LONG.isAssignableFrom(type) )
                    typedValue = bigDecimalValue.longValue();
                else if ( BuiltInTypeSystem.INT.isAssignableFrom(type) )
                    typedValue = bigDecimalValue.intValue();
//        else if ( BuiltInTypeSystem.SHORT.isAssignableFrom(type) )
//            typedValue = bigDecimalValue.shortValue());
                else if ( BuiltInTypeSystem.INTEGER.isAssignableFrom(type) )
                    typedValue = bigDecimalValue.toBigInteger();
                else
                    throw new ClassCastException("Cannot convert BigDecimal to type '" + type + "'");
                break;

            case Path.SETTER_BIGINTEGER:
                BigInteger bigIntValue = (BigInteger)context.getValue();
                type = PropertyImpl.getPropertyXML(prop).getTypeXML();

                if ( BuiltInTypeSystem.INTEGER.isAssignableFrom(type) )
                    typedValue = bigIntValue;
                else if ( BuiltInTypeSystem.STRING.isAssignableFrom(type) )
                    typedValue = String.valueOf(bigIntValue);
//        else if ( BuiltInTypeSystem.BYTE.isAssignableFrom(type) )
//            typedValue = bigIntValue.byteValue());
                else if ( BuiltInTypeSystem.DOUBLE.isAssignableFrom(type) )
                    typedValue = bigIntValue.doubleValue();
                else if ( BuiltInTypeSystem.FLOAT.isAssignableFrom(type) )
                    typedValue = bigIntValue.floatValue();
                else if ( BuiltInTypeSystem.LONG.isAssignableFrom(type) )
                    typedValue = bigIntValue.longValue();
                else if ( BuiltInTypeSystem.INT.isAssignableFrom(type) )
                    typedValue = bigIntValue.intValue();
//        else if ( BuiltInTypeSystem.SHORT.isAssignableFrom(type) )
//            typedValue = bigIntValue.shortValue());
                else if ( BuiltInTypeSystem.DECIMAL.isAssignableFrom(type) )
                    typedValue = new BigDecimal(bigIntValue);
                else if ( BuiltInTypeSystem.BYTES.isAssignableFrom(type) )
                    typedValue = bigIntValue.toByteArray();
                else
                    throw new ClassCastException("Cannot convert BigInteger to type '" + type + "'");
                break;

            case Path.SETTER_DATAOBJECT:
                typedValue = (DataObject)context.getValue();
                break;

            case Path.SETTER_DATE:
                Date dateValue = (Date)context.getValue();
                type = PropertyImpl.getPropertyXML(prop).getTypeXML();

                if ( BuiltInTypeSystem.DATE.isAssignableFrom(type) )
                    typedValue = dateValue;
                else if ( BuiltInTypeSystem.STRING.isAssignableFrom(type) )
                    typedValue = DataHelperImpl._toDateTime(dateValue);
                else if ( BuiltInTypeSystem.LONG.isAssignableFrom(type) )
                    typedValue = dateValue.getTime();
                else if ( BuiltInTypeSystem.DAY.isAssignableFrom(type) )
                    typedValue = DataHelperImpl._toDay(dateValue);
                else if ( BuiltInTypeSystem.DATETIME.isAssignableFrom(type) )
                    typedValue = DataHelperImpl._toDateTime(dateValue);
                else if ( BuiltInTypeSystem.DURATION.isAssignableFrom(type) )
                    typedValue = DataHelperImpl._toDuration(dateValue);
                else if ( BuiltInTypeSystem.MONTH.isAssignableFrom(type) )
                    typedValue = DataHelperImpl._toMonth(dateValue);
                else if ( BuiltInTypeSystem.MONTHDAY.isAssignableFrom(type) )
                    typedValue = DataHelperImpl._toMonthDay(dateValue);
                else if ( BuiltInTypeSystem.TIME.isAssignableFrom(type) )
                    typedValue = DataHelperImpl._toTime(dateValue);
                else if ( BuiltInTypeSystem.YEAR.isAssignableFrom(type) )
                    typedValue = DataHelperImpl._toYear(dateValue);
                else if ( BuiltInTypeSystem.YEARMONTH.isAssignableFrom(type) )
                    typedValue = DataHelperImpl._toYearMonth(dateValue);
                else if ( BuiltInTypeSystem.YEARMONTHDAY.isAssignableFrom(type) )
                    typedValue = DataHelperImpl._toYearMonthDay(dateValue);
                else
                    throw new ClassCastException("Cannot convert java.util.Date to type '" + type + "'");
                break;

            case Path.SETTER_STRING:
                String stringValue = (String)context.getValue();
                type = PropertyImpl.getPropertyXML(prop).getTypeXML();

                try
                {
                    if ( BuiltInTypeSystem.STRING.isAssignableFrom(type) )
                        typedValue = stringValue;
                    else if ( BuiltInTypeSystem.BOOLEAN.isAssignableFrom(type) )
                        typedValue = Boolean.valueOf(stringValue);
                    else if ( BuiltInTypeSystem.BYTE.isAssignableFrom(type) )
                        typedValue = Byte.valueOf(stringValue);
                    else if ( BuiltInTypeSystem.CHARACTER.isAssignableFrom(type) )
                        try
                        {
                            typedValue = Character.valueOf(stringValue.charAt(0));      //todo is this correct
                        }
                        catch (NullPointerException e) {}
                        catch (IndexOutOfBoundsException e) {}
                    else if ( BuiltInTypeSystem.DOUBLE.isAssignableFrom(type) )
                        typedValue = Double.valueOf(stringValue);
                    else if ( BuiltInTypeSystem.FLOAT.isAssignableFrom(type) )
                        typedValue = Float.valueOf(stringValue);
                    else if ( BuiltInTypeSystem.INT.isAssignableFrom(type) )
                        typedValue = Integer.valueOf(stringValue);
                    else if ( BuiltInTypeSystem.LONG.isAssignableFrom(type) )
                        typedValue = Long.valueOf(stringValue);
                    else if ( BuiltInTypeSystem.SHORT.isAssignableFrom(type) )
                        typedValue = Short.valueOf(stringValue);
                    else if ( BuiltInTypeSystem.BYTES.isAssignableFrom(type) )
                        typedValue = HexBin.stringToBytes(stringValue);
                    else if ( BuiltInTypeSystem.DECIMAL.isAssignableFrom(type) )
                        typedValue = new BigDecimal(stringValue);
                    else if ( BuiltInTypeSystem.INTEGER.isAssignableFrom(type) )
                        typedValue = new BigInteger(stringValue);
                    else if ( BuiltInTypeSystem.DATE.isAssignableFrom(type) )
                        typedValue = DataHelperImpl._toDate(stringValue);
                    else if ( BuiltInTypeSystem.DAY.isAssignableFrom(type) )
                        typedValue = DataHelperImpl._toDay(stringValue);
                    else if ( BuiltInTypeSystem.DATETIME.isAssignableFrom(type) )
                        typedValue = DataHelperImpl._toDateTime(stringValue);
                    else if ( BuiltInTypeSystem.DURATION.isAssignableFrom(type) )
                        typedValue = DataHelperImpl._toDuration(stringValue);
                    else if ( BuiltInTypeSystem.MONTH.isAssignableFrom(type) )
                        typedValue = DataHelperImpl._toMonth(stringValue);
                    else if ( BuiltInTypeSystem.MONTHDAY.isAssignableFrom(type) )
                        typedValue = DataHelperImpl._toMonthDay(stringValue);
                    else if ( BuiltInTypeSystem.TIME.isAssignableFrom(type) )
                        typedValue = DataHelperImpl._toTime(stringValue);
                    else if ( BuiltInTypeSystem.YEAR.isAssignableFrom(type) )
                        typedValue = DataHelperImpl._toYear(stringValue);
                    else if ( BuiltInTypeSystem.YEARMONTH.isAssignableFrom(type) )
                        typedValue = DataHelperImpl._toYearMonth(stringValue);
                    else if ( BuiltInTypeSystem.YEARMONTHDAY.isAssignableFrom(type) )
                        typedValue = DataHelperImpl._toYearMonthDay(stringValue);
                    else if ( BuiltInTypeSystem.STRINGS.isAssignableFrom(type) )
                    {
                        if (stringValue == null)
                            typedValue = null;
                        else
                            typedValue = Arrays.asList(stringValue.trim().split("\\s+"));
                    }
                    else if ( type.isDataType() )
                    {
                        // in case this type was defined using javaClass instance property
                        // instanceClass has to have a public String constructor
                        try
                        {
                            typedValue = type.getInstanceClass().getConstructor(String.class).newInstance(stringValue);
                        }
                        catch (InstantiationException e)
                        {
                            String msg =
                                "Cannot convert String to type '" + type +
                                "'. Unable to instantiate " + type.getInstanceClass() +
                                ((e.getMessage() != null) ?
                                 " due to: " + e.getMessage() : ".");
                            throw new ClassCastException(msg);
                        }
                        catch (IllegalAccessException e)
                        {
                            String msg =
                                "Cannot convert String to type '" + type +
                                "'. Unable to instantiate " + type.getInstanceClass() +
                                ((e.getMessage() != null) ?
                                 " due to: " + e.getMessage() : ".");
                            throw new ClassCastException(msg);
                        }
                        catch (InvocationTargetException e)
                        {
                            String msg =
                                "Cannot convert String to type '" + type +
                                "'. Unable to instantiate " + type.getInstanceClass() +
                                ((e.getMessage() != null) ?
                                 " due to: " + e.getMessage() : ".");
                            throw new ClassCastException(msg);
                        }
                        catch (NoSuchMethodException e)
                        {
                            String msg =
                                "Cannot convert String to type '" + type +
                                "'. Unable to instantiate " + type.getInstanceClass() +
                                ((e.getMessage() != null) ?
                                 " due to: " + e.getMessage() : ".");
                            throw new ClassCastException(msg);
                        }
                    }
                    else
                        throw new ClassCastException("Cannot convert String to type '" + type + "'");
                }
                // do nothing 3.1.3 get<T>(String path) and set<T>(String path) will not throw exceptions.
                catch (NumberFormatException e) {}
                break;

            case Path.SETTER_UNTYPED:
            case Path.SETTER_LIST:
                typedValue = context.getValue();
                break;

            default:
                throw new IllegalStateException("Unknown setter type.");
            }

            return typedValue;
        }

        private static void detachValueIfContainment(Object value, Property property)
        {
            if (property.isContainment())
            {
                if (value instanceof DataObject)
                {
                    DataObject doValue = (DataObject)value;
                    DataObject container = doValue.getContainer();
                    if (container!=null)
                    {
                        doValue.detach();
                    }
                }
            }
        }

        public String toString()
        {
            return _propertyName + "." + _index + childToString();
        }
    }

    public static class Index1Step
        extends Index0Step
    {
        Index1Step(String property, int index)
        {
            super(property, index-1);
        }

        public String toString()
        {
            return _propertyName + "[" + (_index+1) + "]" + childToString();
        }
    }

    public static class ParentStep
        extends Step
    {
        public boolean execute(Path context)
        {
            DataObject parent = context.getParentNode().getContainer();
            if (parent==null)
                return false;

            context.setParentNode(parent);

            if (_childStep!=null)
                return _childStep.execute(context);
            else
                // it's the last step
                switch (context.getOperation())
                {
                case Path.OP_GET:
                    context.setValue(parent);
                    return true;
                case Path.OP_SET:
                    Property prop = parent.getContainmentProperty();
                    DataObject gradParent = parent.getContainer();

                    if (gradParent==null || prop==null)
                        return false;

                    gradParent.set(prop, context.getValue());
                    return true;
                case Path.OP_ISSET:
                    context.setValue(/*_index<items.size() in this case is*/ true);
                    return true;
                case Path.OP_UNSET:
                    prop = parent.getContainmentProperty();
                    gradParent = parent.getContainer();

                    if (gradParent==null || prop==null)
                        return false;

                    gradParent.unset(prop);
                    return true;
                default:
                    throw new IllegalStateException("Unknown operation :" + context.getOperation());
                }
        }

        public String toString()
        {
            return ".." + childToString();
        }
    }

    public static abstract class ValueStep
        extends PropertyStep
    {
        String _attribute;

        ValueStep(String property, String attribute)
        {
            super(property);
            _attribute = attribute;
        }

        abstract boolean hasCheck();
        abstract boolean checkValue(Object obj);

        public String toString()
        {
            return _propertyName + "[" + _attribute + "=";
        }
    }

    public static class LiteralValueStep
        extends ValueStep
    {
        String _value;

        LiteralValueStep(String property, String attribute, String value)
        {
            super(property, attribute);
            assert value!=null;
            _value = value;
        }

        boolean hasCheck()
        {
            return true;
        }

        boolean checkValue(Object obj)
        {
            if ( !(obj instanceof DataObject) )
                return false;

            DataObject currentDo = (DataObject)obj;
            Property attProp = currentDo.getInstanceProperty(_attribute);
            if (attProp==null)
                return false;

            String attVal = currentDo.getString(attProp);
            return _value.equals(attVal);
        }

        public String toString()
        {
            return super.toString() + "'" + _value + "']" + childToString();
        }
    }

    public static class NumeralValueStep
        extends ValueStep
    {
        BigDecimal _value;

        NumeralValueStep(String property, String attribute, BigDecimal value)
        {
            super(property, attribute);
            _value = value;
        }

        boolean hasCheck()
        {
            return true;
        }

        boolean checkValue(Object obj)
        {
            if ( !(obj instanceof DataObject) )
                return false;

            DataObject currentDo = (DataObject)obj;
            Property attProp = currentDo.getInstanceProperty(_attribute);
            if (attProp==null)
                return false;

            BigDecimal attVal = currentDo.getBigDecimal(attProp);
            return _value.equals(attVal);
        }

        public String toString()
        {
            return super.toString() + _value + "]" + childToString();
        }
    }

    public static class BooleanValueStep
        extends ValueStep
    {
        boolean _value;

        BooleanValueStep(String property, String attribute, boolean value)
        {
            super(property, attribute);
            _value = value;
        }

        boolean hasCheck()
        {
            return true;
        }

        boolean checkValue(Object obj)
        {
            if ( !(obj instanceof DataObject) )
                return false;

            DataObject currentDo = (DataObject)obj;
            Property attProp = currentDo.getInstanceProperty(_attribute);
            if (attProp==null)
                return false;

            boolean attVal = currentDo.getBoolean(attProp);
            return _value == attVal;
        }

        public String toString()
        {
            return super.toString() + _value + "]" + childToString();
        }
    }
}
