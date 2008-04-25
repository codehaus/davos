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

import davos.sdo.TypeXML;
import davos.sdo.SDOError;
import davos.sdo.impl.common.Common;
import davos.sdo.impl.common.NamespaceStack;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;

import org.apache.xmlbeans.impl.util.XsTypeConverter;
import org.apache.xmlbeans.impl.common.XMLChar;
import org.apache.xmlbeans.GDuration;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.GDate;
import org.apache.xmlbeans.GDateBuilder;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * User: radup
 * Date: May 17, 2007
 * This class contains a few helper methods used by both the unmarshaller and the binding code
 */
public class SimpleValueHelper
{
    public static Object parseBufferToType(CharSequence stringBuffer, TypeXML type, int schemaTypeCode,
        NamespaceContext nsctx) throws SimpleValueException
    {
        try
        {
            // We select a few of the most often-used types directly based on the type code
            // because it is much faster this way
            switch (type.getTypeCode())
            {
            case BuiltInTypeSystem.TYPECODE_BOOLEAN:
                return XsTypeConverter.lexBoolean(stringBuffer);
            case BuiltInTypeSystem.TYPECODE_DATE:
                return XsTypeConverter.lexDateTime(stringBuffer).getTime();
            case BuiltInTypeSystem.TYPECODE_DATETIME:
                GDate value = XsTypeConverter.lexGDate(stringBuffer);
                if (value.getBuiltinTypeCode() != SchemaType.BTC_DATE_TIME)
                    throw new Exception(SDOError.messageForCode("unmarshal.simple.dateconversion",
                        stringBuffer, BuiltInTypeSystem.DATETIME.getName(), "yyyy-mm-ddThh:mm:ss"));
                return value.toString();
            case BuiltInTypeSystem.TYPECODE_FLOAT:
                return XsTypeConverter.lexFloat(stringBuffer);
            case BuiltInTypeSystem.TYPECODE_INT:
                return XsTypeConverter.lexInt(stringBuffer);
            case BuiltInTypeSystem.TYPECODE_LONG:
                return XsTypeConverter.lexLong(stringBuffer);
            case BuiltInTypeSystem.TYPECODE_STRING:
                return XsTypeConverter.lexString(stringBuffer);
            }
        if (type.getListItemType() != null)
        {
            // Unmarshal a list type
            return parseList(stringBuffer, type.getListItemType(), schemaTypeCode, nsctx);
        }
        if (type.hasCustomizedInstanceClass() && type.getInstanceClass() != null)
        {
            Class c = type.getInstanceClass();
            try
            {
                Constructor constr = c.getConstructor(String.class);
                return constr.newInstance(stringBuffer.toString());
            }
            catch (NoSuchMethodException nsme)
            {
                throw new SimpleValueException(UNMARSHAL_SIMPLE_NOCONSTRUCTOR, c.getName(), nsme);
            }
            catch (IllegalAccessException iae)
            {
                throw new SimpleValueException(UNMARSHAL_SIMPLE_NOCONSTRUCTOR, c.getName(), iae);
            }
            catch (InvocationTargetException ite)
            {
                throw new SimpleValueException(UNMARSHAL_SIMPLE_CONSTRUCTOREXCEPTION, c.getName(), ite);
            }
            catch (InstantiationException ie)
            {
                throw new SimpleValueException(UNMARSHAL_SIMPLE_CONSTRUCTOREXCEPTION, c.getName(), ie);
            }
        }
        if (BuiltInTypeSystem.BOOLEAN.isAssignableFrom(type))
        {
            return XsTypeConverter.lexBoolean(stringBuffer);
        }
        else if(BuiltInTypeSystem.BYTE.isAssignableFrom(type))
        {
            return XsTypeConverter.lexByte(stringBuffer);
        }
        else if(BuiltInTypeSystem.BYTES.isAssignableFrom(type))
        {
            if (schemaTypeCode == SchemaType.BTC_HEX_BINARY)
                return XsTypeConverter.lexHexBinary(stringBuffer);
            else
                return XsTypeConverter.lexBase64Binary(stringBuffer);
        }
        else if(BuiltInTypeSystem.CHARACTER.isAssignableFrom(type))
        {
            return XsTypeConverter.lexString(stringBuffer).charAt(0);
        }
        else if(BuiltInTypeSystem.DATE.isAssignableFrom(type))
        {
            return XsTypeConverter.lexDateTime(stringBuffer).getTime();
        }
        else if(BuiltInTypeSystem.DATETIME.isAssignableFrom(type))
        {
            GDate value = XsTypeConverter.lexGDate(stringBuffer);
            if (value.getBuiltinTypeCode() != SchemaType.BTC_DATE_TIME)
                throw new Exception(SDOError.messageForCode("unmarshal.simple.dateconversion",
                    stringBuffer, BuiltInTypeSystem.DATETIME.getName(), "yyyy-mm-ddThh:mm:ss"));
            return value.toString();
        }
        else if(BuiltInTypeSystem.DAY.isAssignableFrom(type))
        {
            GDate value = XsTypeConverter.lexGDate(stringBuffer);
            if (value.getBuiltinTypeCode() != SchemaType.BTC_G_DAY)
                throw new Exception(SDOError.messageForCode("unmarshal.simple.dateconversion",
                    stringBuffer, BuiltInTypeSystem.DAY.getName(), "---DD"));
            return value.toString();
        }
        else if(BuiltInTypeSystem.DECIMAL.isAssignableFrom(type))
        {
            return XsTypeConverter.lexDecimal(stringBuffer);
        }
        else if(BuiltInTypeSystem.DOUBLE.isAssignableFrom(type))
        {
            return XsTypeConverter.lexDouble(stringBuffer);
        }
        else if(BuiltInTypeSystem.DURATION.isAssignableFrom(type))
        {
            return new GDuration(stringBuffer).toString();
        }
        else if(BuiltInTypeSystem.FLOAT.isAssignableFrom(type))
        {
            return XsTypeConverter.lexFloat(stringBuffer);
        }
        else if(BuiltInTypeSystem.INT.isAssignableFrom(type))
        {
            return XsTypeConverter.lexInt(stringBuffer);
        }
        else if(BuiltInTypeSystem.INTEGER.isAssignableFrom(type))
        {
            return XsTypeConverter.lexInteger(stringBuffer);
        }
        else if(BuiltInTypeSystem.LONG.isAssignableFrom(type))
        {
            return XsTypeConverter.lexLong(stringBuffer);
        }
        else if(BuiltInTypeSystem.MONTH.isAssignableFrom(type))
        {
            GDate value = XsTypeConverter.lexGDate(stringBuffer);
            if (value.getBuiltinTypeCode() != SchemaType.BTC_G_MONTH)
                throw new Exception(SDOError.messageForCode("unmarshal.simple.dateconversion",
                    stringBuffer, BuiltInTypeSystem.MONTH.getName(), "--MM"));
            return value.toString();
        }
        else if(BuiltInTypeSystem.MONTHDAY.isAssignableFrom(type))
        {
            GDate value = XsTypeConverter.lexGDate(stringBuffer);
            if (value.getBuiltinTypeCode() != SchemaType.BTC_G_MONTH_DAY)
                throw new Exception(SDOError.messageForCode("unmarshal.simple.dateconversion",
                    stringBuffer, BuiltInTypeSystem.MONTHDAY.getName(), "--MM-DD"));
            return value.toString();
        }
        else if(BuiltInTypeSystem.SHORT.isAssignableFrom(type))
        {
            return XsTypeConverter.lexShort(stringBuffer);
        }
        else if(BuiltInTypeSystem.STRING.isAssignableFrom(type))
        {
            return XsTypeConverter.lexString(stringBuffer);
        }
        else if(BuiltInTypeSystem.STRINGS.isAssignableFrom(type))
        {
            return parseList(stringBuffer, BuiltInTypeSystem.STRING, 0, nsctx);
        }
        else if(BuiltInTypeSystem.TIME.isAssignableFrom(type))
        {
            GDate value = XsTypeConverter.lexGDate(stringBuffer);
            if (value.getBuiltinTypeCode() != SchemaType.BTC_TIME)
                throw new Exception(SDOError.messageForCode("unmarshal.simple.dateconversion",
                    stringBuffer, BuiltInTypeSystem.TIME.getName(), "hh:mm:ss.sss"));
            return value.toString();
        }
        else if(BuiltInTypeSystem.URI.isAssignableFrom(type))
        {
            if (schemaTypeCode == SchemaType.BTC_QNAME)
            {
                QName qname = XsTypeConverter.lexQName(stringBuffer, nsctx);
                // Aparently, this doesn't check for empty local part, so let's check for that
                if (qname.getLocalPart().length() == 0)
                    throw new Exception(SDOError.messageForCode("unmarshal.qname.zerolength"));
                return qname.getNamespaceURI() + '#' + qname.getLocalPart();
            }
            else
                return XsTypeConverter.lexString(stringBuffer);
        }
        else if(BuiltInTypeSystem.YEAR.isAssignableFrom(type))
        {
            GDate value = XsTypeConverter.lexGDate(stringBuffer);
            if (value.getBuiltinTypeCode() != SchemaType.BTC_G_YEAR)
                throw new Exception(SDOError.messageForCode("unmarshal.simple.dateconversion",
                    stringBuffer, BuiltInTypeSystem.YEAR.getName(), "CCYY"));
            return value.toString();
        }
        else if(BuiltInTypeSystem.YEARMONTH.isAssignableFrom(type))
        {
            GDate value = XsTypeConverter.lexGDate(stringBuffer);
            if (value.getBuiltinTypeCode() != SchemaType.BTC_G_YEAR_MONTH)
                throw new Exception(SDOError.messageForCode("unmarshal.simple.dateconversion",
                    stringBuffer, BuiltInTypeSystem.YEARMONTH.getName(), "CCYY-MM"));
            return value.toString();
        }
        else if(BuiltInTypeSystem.YEARMONTHDAY.isAssignableFrom(type))
        {
            GDate value = XsTypeConverter.lexGDate(stringBuffer);
            if (value.getBuiltinTypeCode() != SchemaType.BTC_DATE)
                throw new Exception(SDOError.messageForCode("unmarshal.simple.dateconversion",
                    stringBuffer, BuiltInTypeSystem.YEARMONTHDAY.getName(), "yyyy-mm-dd"));
            return value.toString();
        }
        else
        {
            // Even if the type is user-defined, if there is an instance class that is one of the
            // "basic" Java instance classes, we still allow the conversion
            // Integer, Boolean, Byte, Short, Long, Float, Double, String, Object, BigInteger, BigDecimal, List, byte[]
            Class c = type.getInstanceClass();
            try
            {
                if (c == Integer.TYPE || c == Integer.class)
                    return new Integer(stringBuffer.toString());
                else if (c == Boolean.TYPE || c == Boolean.class)
                    return new Boolean(stringBuffer.toString());
                else if (c == Byte.TYPE || c == Byte.class)
                    return new Byte(stringBuffer.toString());
                else if (c == Short.TYPE || c == Short.class)
                    return new Short(stringBuffer.toString());
                else if (c == Long.TYPE || c == Long.class)
                    return new Long(stringBuffer.toString());
                else if (c == Float.TYPE || c == Float.class)
                    return new Float(stringBuffer.toString());
                else if (c == Double.TYPE || c == Double.class)
                    return new Double(stringBuffer.toString());
                else if (c == String.class)
                    return stringBuffer.toString();
                else if (c == Object.class)
                    return stringBuffer.toString();
                else if (c == BigInteger.class)
                    return new BigInteger(stringBuffer.toString());
                else if (c == BigDecimal.class)
                    return new BigDecimal(stringBuffer.toString());
                else if (c == List.class)
                    return parseList(stringBuffer.toString(), BuiltInTypeSystem.STRING,
                        SchemaType.BTC_STRING, null);
                else if (c == byte[].class)
                    return XsTypeConverter.lexBase64Binary(stringBuffer);
            }
            catch (NumberFormatException nfe)
            {
                throw new SimpleValueException(UNMARSHAL_SIMPLE_CONVERSIONFAILED, nfe);
            }
        }
        }
        catch (Exception e)
        {
            throw new SimpleValueException(UNMARSHAL_SIMPLE_CONVERSIONFAILED, e);
        }

        throw new SimpleValueException(UNMARSHAL_SIMPLE_UNKOWNTYPE);
    }

    private static List parseList(CharSequence stringBuffer, TypeXML type, int schemaTypeCode,
        NamespaceContext nsctx) throws SimpleValueException
    {
        if (stringBuffer.length() == 0)
            return Common.EMPTY_STRING_LIST;
        List result = new ArrayList(4);
        int i = 0;
        int start;
        for (;;)
        {
            while (i < stringBuffer.length() && XMLChar.isSpace(stringBuffer.charAt(i)))
                i += 1;
            if (i >= stringBuffer.length())
                return result;
            start = i;
            while (i < stringBuffer.length() && !XMLChar.isSpace(stringBuffer.charAt(i)))
                i += 1;
            result.add(parseBufferToType(stringBuffer.subSequence(start, i), type, schemaTypeCode,
                nsctx));
        }
    }

    public static String getLexicalRepresentation(Object value, TypeXML targetType,
        int schemaTypeCode, NamespaceStack nstck) throws SimpleValueException
    {
        try
        {
        switch (targetType.getTypeCode())
        {
        case BuiltInTypeSystem.TYPECODE_BOOLEAN:
        {
            return XsTypeConverter.printBoolean((Boolean) value);
        }
        case BuiltInTypeSystem.TYPECODE_BYTE:
        {
            return XsTypeConverter.printByte((Byte) value);
        }
        case BuiltInTypeSystem.TYPECODE_BYTES:
        {
            if (schemaTypeCode == SchemaType.BTC_HEX_BINARY)
                return XsTypeConverter.printHexBinary((byte[]) value).toString();
            else
                return XsTypeConverter.printBase64Binary((byte[]) value).toString();
        }
        case BuiltInTypeSystem.TYPECODE_CHARACTER:
        {
            return XsTypeConverter.printString(String.valueOf((Character) value));
        }
        case BuiltInTypeSystem.TYPECODE_DATE:
        {
            return dateToDateTime((Date) value);
        }
        case BuiltInTypeSystem.TYPECODE_DATETIME:
        {
            return /*XsTypeConverter.printString(*/(String) value;
        }
        case BuiltInTypeSystem.TYPECODE_DAY:
        {
            return /*XsTypeConverter.printString(*/(String) value;
        }
        case BuiltInTypeSystem.TYPECODE_DECIMAL:
        {
            return XsTypeConverter.printDecimal((BigDecimal) value);
        }
        case BuiltInTypeSystem.TYPECODE_DOUBLE:
        {
            return XsTypeConverter.printDouble((Double) value);
        }
        case BuiltInTypeSystem.TYPECODE_DURATION:
        {
            return /*XsTypeConverter.printString(*/(String) value;
        }
        case BuiltInTypeSystem.TYPECODE_FLOAT:
        {
            return XsTypeConverter.printFloat((Float) value);
        }
        case BuiltInTypeSystem.TYPECODE_INT:
        {
            return XsTypeConverter.printInt((Integer) value);
        }
        case BuiltInTypeSystem.TYPECODE_INTEGER:
        {
            return XsTypeConverter.printInteger((BigInteger) value);
        }
        case BuiltInTypeSystem.TYPECODE_LONG:
        {
            return XsTypeConverter.printLong((Long) value);
        }
        case BuiltInTypeSystem.TYPECODE_MONTH:
        {
            return /*XsTypeConverter.printGDate(*/(String) value;
        }
        case BuiltInTypeSystem.TYPECODE_MONTHDAY:
        {
            return /*XsTypeConverter.printString(*/(String) value;
        }
        case BuiltInTypeSystem.TYPECODE_OBJECT:
        {
            return value.toString();
        }
        case BuiltInTypeSystem.TYPECODE_SHORT:
        {
            return XsTypeConverter.printShort((Short) value);
        }
        case BuiltInTypeSystem.TYPECODE_STRING:
        {
            return XsTypeConverter.printString((String) value);
        }
        case BuiltInTypeSystem.TYPECODE_STRINGS:
        {
            // We are not using the cast here directly because this will be called for Lists
            // which don't have Strings as elements, in the case of an xsi:type="Strings"
            List values = (List) value;
            switch (values.size())
            {
            case 0:
                return "";
            case 1:
                return values.get(0).toString();
            default:
                StringBuilder b = new StringBuilder(values.get(0).toString());
                for (int i = 1; i < values.size(); i++)
                    b.append(' ').append(values.get(i).toString());
                return b.toString();
            }
        }
        case BuiltInTypeSystem.TYPECODE_TIME:
        {
            return /*XsTypeConverter.printString(*/(String) value;
        }
        case BuiltInTypeSystem.TYPECODE_URI:
        {
            if (schemaTypeCode == SchemaType.BTC_QNAME)
            {
                String valueAsString = (String) value;
                int index = valueAsString.lastIndexOf('#');
                String uri, local;
                if (index < 1)
                {
                    uri = Common.EMPTY_STRING; 
                    local = index == 0 ? valueAsString.substring(1) : valueAsString;
                }
                else
                {
                    uri = valueAsString.substring(0, index);
                    local = valueAsString.substring(index + 1);
                }
                // We could verify here that the local name is a valid NCName
                String prefix = nstck.ensureMapping(uri, null, false, false);
                if (prefix == null || prefix.length() == 0)
                    return local;
                else
                    return prefix + ':' + local;
            }
            else
                return XsTypeConverter.printString((String) value);
        }
        case BuiltInTypeSystem.TYPECODE_YEAR:
        {
            return /*XsTypeConverter.printString(*/(String) value;
        }
        case BuiltInTypeSystem.TYPECODE_YEARMONTH:
        {
            return /*XsTypeConverter.printString(*/(String) value;
        }
        case BuiltInTypeSystem.TYPECODE_YEARMONTHDAY:
        {
            return /*XsTypeConverter.printString(*/(String) value;
        }
        }
        if (targetType.getListItemType() != null)
        {
            // Unmarshal a list type
            return lexList((List) value, targetType.getListItemType(), schemaTypeCode, nstck);
        }

        // Now verify derivations
        if (BuiltInTypeSystem.BOOLEAN.isAssignableFrom(targetType))
        {
            return XsTypeConverter.printBoolean((Boolean) value);
        }
        else if (BuiltInTypeSystem.BYTE.isAssignableFrom(targetType))
        {
            return XsTypeConverter.printByte((Byte) value);
        }
        else if (BuiltInTypeSystem.BYTES.isAssignableFrom(targetType))
        {
            if (schemaTypeCode == SchemaType.BTC_HEX_BINARY)
                return XsTypeConverter.printHexBinary((byte[]) value).toString();
            else
                return XsTypeConverter.printBase64Binary((byte[]) value).toString();
        }
        else if (BuiltInTypeSystem.CHARACTER.isAssignableFrom(targetType))
        {
            return XsTypeConverter.printString(String.valueOf((Character) value));
        }
        else if (BuiltInTypeSystem.DATE.isAssignableFrom(targetType))
        {
            return dateToDateTime((Date) value);
        }
        else if (BuiltInTypeSystem.DATETIME.isAssignableFrom(targetType))
        {
            return /*XsTypeConverter.printString(*/(String) value;
        }
        else if (BuiltInTypeSystem.DAY.isAssignableFrom(targetType))
        {
            return /*XsTypeConverter.printString(*/(String) value;
        }
        else if (BuiltInTypeSystem.DECIMAL.isAssignableFrom(targetType))
        {
            return XsTypeConverter.printDecimal((BigDecimal) value);
        }
        else if (BuiltInTypeSystem.DOUBLE.isAssignableFrom(targetType))
        {
            return XsTypeConverter.printDouble((Double) value);
        }
        else if (BuiltInTypeSystem.DURATION.isAssignableFrom(targetType))
        {
            return /*XsTypeConverter.printString(*/(String) value;
        }
        else if (BuiltInTypeSystem.FLOAT.isAssignableFrom(targetType))
        {
            return XsTypeConverter.printFloat((Float) value);
        }
        else if (BuiltInTypeSystem.INT.isAssignableFrom(targetType))
        {
            return XsTypeConverter.printInt((Integer) value);
        }
        else if (BuiltInTypeSystem.INTEGER.isAssignableFrom(targetType))
        {
            return XsTypeConverter.printInteger((BigInteger) value);
        }
        else if (BuiltInTypeSystem.LONG.isAssignableFrom(targetType))
        {
            return XsTypeConverter.printLong((Long) value);
        }
        else if (BuiltInTypeSystem.MONTH.isAssignableFrom(targetType))
        {
            return /*XsTypeConverter.printGDate(*/(String) value;
        }
        else if (BuiltInTypeSystem.MONTHDAY.isAssignableFrom(targetType))
        {
            return /*XsTypeConverter.printString(*/(String) value;
        }
        else if (BuiltInTypeSystem.OBJECT.isAssignableFrom(targetType))
        {
            return value.toString();
        }
        else if (BuiltInTypeSystem.SHORT.isAssignableFrom(targetType))
        {
            return XsTypeConverter.printShort((Short) value);
        }
        else if (BuiltInTypeSystem.STRING.isAssignableFrom(targetType))
        {
            return XsTypeConverter.printString((String) value);
        }
        else if (BuiltInTypeSystem.STRINGS.isAssignableFrom(targetType))
        {
            List<String> values = (List<String>) value;
            switch (values.size())
            {
            case 0:
                return "";
            case 1:
                return values.get(0);
            default:
                StringBuilder b = new StringBuilder(values.get(0));
                for (int i = 1; i < values.size(); i++)
                    b.append(' ').append(values.get(i));
                return b.toString();
            }
        }
        else if (BuiltInTypeSystem.TIME.isAssignableFrom(targetType))
        {
            return /*XsTypeConverter.printString(*/(String) value;
        }
        else if (BuiltInTypeSystem.URI.isAssignableFrom(targetType))
        {
            if (schemaTypeCode == SchemaType.BTC_QNAME)
            {
                String valueAsString = (String) value;
                int index = valueAsString.lastIndexOf('#');
                String uri, local;
                if (index < 1)
                {
                    uri = Common.EMPTY_STRING; 
                    local = index == 0 ? valueAsString.substring(1) : valueAsString;
                }
                else
                {
                    uri = valueAsString.substring(0, index);
                    local = valueAsString.substring(index + 1);
                }
                String prefix = nstck.ensureMapping(uri, null, false, false);
                if (prefix.length() == 0)
                    return local;
                else
                    return prefix + ':' + local;
            }
            else
                return XsTypeConverter.printString((String) value);
        }
        else if (BuiltInTypeSystem.YEAR.isAssignableFrom(targetType))
        {
            return /*XsTypeConverter.printString(*/(String) value;
        }
        else if (BuiltInTypeSystem.YEARMONTH.isAssignableFrom(targetType))
        {
            return /*XsTypeConverter.printString(*/(String) value;
        }
        else if (BuiltInTypeSystem.YEARMONTHDAY.isAssignableFrom(targetType))
        {
            return /*XsTypeConverter.printString(*/(String) value;
        }
        }
        catch (ClassCastException cce)
        {
            throw new SimpleValueException(MARSHAL_WRONGINSTANCECLASS);
        }
        // This takes care of the case where there is a customized class present, as well as the
        // case where there is no matching SDO type but the instance class is of a "known" type
        // For the List case, what toString() returns is not appropriate, same for byte[]
        if (List.class.isAssignableFrom(value.getClass()))
            return lexList((List) value, BuiltInTypeSystem.STRING, SchemaType.BTC_STRING, null);
        else if (byte[].class.equals(value.getClass()))
            return XsTypeConverter.printBase64Binary((byte[]) value).toString();
        else
            return value.toString();
    }

    private static String lexList(List values, TypeXML listItemType, int schemaTypeCode,
        NamespaceStack nstck) throws SimpleValueException
    {
        switch (values.size())
        {
        case 0:
            return "";
        case 1:
            return getLexicalRepresentation(values.get(0), listItemType, schemaTypeCode, nstck);
        default:
            StringBuilder b = new StringBuilder(getLexicalRepresentation(values.get(0), listItemType, schemaTypeCode, nstck));
            for (int i = 1; i < values.size(); i++)
                b.append(' ').append(getLexicalRepresentation(values.get(i), listItemType, schemaTypeCode, nstck));
            return b.toString();
        }
    }

    public static String dateToDateTime(Date date)
    {
        GDateBuilder gdb = new GDateBuilder(date);
        gdb.normalizeToTimeZone(0);
        gdb.setBuiltinTypeCode(SchemaType.BTC_DATE_TIME);
        return gdb.canonicalString();
    }

    public static final int UNMARSHAL_SIMPLE_NOCONSTRUCTOR = 1;
    public static final int UNMARSHAL_SIMPLE_CONSTRUCTOREXCEPTION = 2;
    public static final int UNMARSHAL_SIMPLE_CONVERSIONFAILED = 3;
    public static final int UNMARSHAL_SIMPLE_UNKOWNTYPE = 4;
    public static final int MARSHAL_WRONGINSTANCECLASS = 5;

    public static class SimpleValueException extends Exception
    {
        private int _cause;
        private String _param;

        SimpleValueException(int cause)
        {
            _cause = cause;
        }

        SimpleValueException(int c, Throwable cause)
        {
            super(cause);
            _cause = c;
        }

        SimpleValueException(int c, String param, Throwable cause)
        {
            super(cause);
            _cause = c;
            _param = param;
        }

        public int cause()
        {
            return _cause;
        }

        public String getParam()
        {
            return _param;
        }
    }
}
