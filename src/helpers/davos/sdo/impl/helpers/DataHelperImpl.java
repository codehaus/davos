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
package davos.sdo.impl.helpers;

import javax.sdo.helper.DataHelper;
import javax.sdo.Type;
import javax.sdo.Property;
import org.apache.xmlbeans.GDateBuilder;
import org.apache.xmlbeans.GDuration;
import org.apache.xmlbeans.GDurationBuilder;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.XmlCalendar;
import org.apache.xmlbeans.impl.util.HexBin;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.List;
import java.util.Arrays;

import davos.sdo.TypeXML;
import davos.sdo.SDOContext;
import davos.sdo.impl.type.TypeImpl;
import davos.sdo.impl.type.BuiltInTypeSystem;

/**
 * @author Cezar Andrei (cezar dot andrei at gmail dot com)
 *         Date: Jan 13, 2006
 */
public class DataHelperImpl
    implements DataHelper
{
    private SDOContext _sdoContext;

    public DataHelperImpl(SDOContext sdoContext)
    {
        _sdoContext = sdoContext;
    }


    public Date toDate(String dateString)
    {
        return _toDate(dateString);
    }

    public Calendar toCalendar(String dateString)
    {
        return _toCalendar(dateString, null);
    }

    public Calendar toCalendar(String dateString, Locale locale)
    {
        return _toCalendar(dateString, locale);
    }

    public String toDateTime(Date date)
    {
        return _toDateTime(date);
    }

    public String toDuration(Date date)
    {
        return _toDuration(date);
    }

    public String toTime(Date date)
    {
        return _toTime(date);
    }

    public String toDay(Date date)
    {
        return _toDay(date);
    }

    public String toMonth(Date date)
    {
        return _toMonth(date);
    }

    public String toMonthDay(Date date)
    {
        return _toMonthDay(date);
    }

    public String toYear(Date date)
    {
        return _toYear(date);
    }

    public String toYearMonth(Date date)
    {
        return _toYearMonth(date);
    }

    public String toYearMonthDay(Date date)
    {
        return _toYearMonthDay(date);
    }

    public String toDateTime(Calendar calendar)
    {
        GDateBuilder dgb = new GDateBuilder(calendar);
        return dgb.toString();
    }

    public String toDuration(Calendar calendar)
    {
        return _toDuration(calendar);
    }

    public static String _toDuration(Calendar calendar)
    {
//        GDateBuilder yearZero = new GDateBuilder("-0001-01-01T00:00:00Z");
//        GDateBuilder gdbForCalendar = new GDateBuilder(toDateTime(calendar));
//
//        GDurationBuilder gdb = new GDurationBuilder(1, 0 ,0, 0, 0, 0, 0, new BigDecimal( BigInteger.valueOf(gdbForCalendar.getDate().getTime() - yearZero.getDate().getTime()), 3));
//        gdb.normalize();
//        return gdb.toString();

        //return toDuration(calendar.getTime());

        if (!calendar.isSet(Calendar.ZONE_OFFSET))
            calendar.setTimeZone(TimeZone.getTimeZone("GMT"));

        GDateBuilder gd = new GDateBuilder(calendar);
        //gd.normalizeToTimeZone(0);

        int sign = 1;
        int year   = gd.getYear();

        int month  = gd.getMonth();
        int day    = gd.getDay();
        int hour   = gd.getHour();
        int minute = gd.getMinute();
        int sec    = gd.getSecond();
        int ms     = gd.getMillisecond();

        if (year<1)
        {
            if (year<-1)
                sign = -1;
            year =  -1 - year;
        }
        if (sign<0)
        {
            int carry = 0;

            if (ms>0)
            {
                ms = 1000 - ms;
                carry = 1;
            }
            if (carry>0 || sec>0)
            {
                sec = 60 - sec - carry;
                carry = 1;
            }
            if (carry>0 || minute>0)
            {
                minute = 60 - minute - carry;
                carry = 1;
            }
            if (carry>0 || hour>0)
            {
                hour = 24 - hour - carry;
                carry = 1;
            }
            if (carry>0 || day>1)
            {
                day = monthLength(month, gd.getYear()) + 2 - day - carry;
                carry = 1;
            }
            if (carry>0 || month>1)
            {
                month = 12 + 2 - month - carry;
                carry = 1;
            }
            if (carry>0 || year>0)
            {
                year = year - carry;
            }
        }

        BigDecimal fraction = new BigDecimal( BigInteger.valueOf(ms), 3);
        GDurationBuilder gdur = new GDurationBuilder(sign, year, month-1, day-1, hour, minute, sec, fraction);
        gdur.normalize();
        return gdur.toString();
    }

    private final static int[] MONTHS_LENGTHES = new int[] {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};

    private static int monthLength(int month, int year)
    {
        assert month>0 && month<13;

        if (month!=2)
            return MONTHS_LENGTHES[month - 1];
        else
            return isLeapYear(year) ? 29 : 28;
    }

    private static boolean isLeapYear(int year)
    {
        return year % 4 == 0 && ( year % 100 != 0 || year % 400 == 0); 
    }

    public String toTime(Calendar calendar)
    {
        GDateBuilder gdb = new GDateBuilder(calendar);
        gdb.setBuiltinTypeCode(SchemaType.BTC_TIME);
        return gdb.toString();
    }

    public String toDay(Calendar calendar)
    {
        GDateBuilder gdb = new GDateBuilder(calendar);
        gdb.setBuiltinTypeCode(SchemaType.BTC_G_DAY);
        return gdb.toString();
    }

    public String toMonth(Calendar calendar)
    {
        GDateBuilder gdb = new GDateBuilder(calendar);
        gdb.setBuiltinTypeCode(SchemaType.BTC_G_MONTH);
        return gdb.toString();
    }

    public String toMonthDay(Calendar calendar)
    {
        GDateBuilder gdb = new GDateBuilder(calendar);
        gdb.setBuiltinTypeCode(SchemaType.BTC_G_MONTH_DAY);
        return gdb.toString();
    }

    public String toYear(Calendar calendar)
    {
        GDateBuilder gdb = new GDateBuilder(calendar);
        gdb.setBuiltinTypeCode(SchemaType.BTC_G_YEAR);
        return gdb.toString();
    }

    public String toYearMonth(Calendar calendar)
    {
        GDateBuilder gdb = new GDateBuilder(calendar);
        gdb.setBuiltinTypeCode(SchemaType.BTC_G_YEAR_MONTH);
        return gdb.toString();
    }

    public String toYearMonthDay(Calendar calendar)
    {
        GDateBuilder gdb = new GDateBuilder(calendar);
        gdb.setBuiltinTypeCode(SchemaType.BTC_DATE);
        return gdb.toString();
    }

    public Object convert(Type type, Object value)
    {
        if (value==null || type==null)
            return null;

        Class instClass = type.getInstanceClass();

        if (instClass.isAssignableFrom(boolean.class))
        {
            if ( value instanceof Boolean)
                return value;
            if ( value instanceof String)
                return Boolean.parseBoolean((String)value);
        }
        else if (instClass.isAssignableFrom(byte.class))
        {
            if ( value instanceof Byte)
                return value;
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
        }
        else if (instClass.isAssignableFrom(char.class))
        {
            if ( value instanceof Character)
                return value;
            if ( value instanceof String)
            {
                String stringValue = (String)value;
                if ( stringValue.length() == 0 )
                    return '\0';
                else
                    return stringValue.charAt(0);
            }
        }
        else if (instClass.isAssignableFrom(double.class))
        {
            if ( value instanceof Double)
                return value;
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
        }
        else if (instClass.isAssignableFrom(float.class))
        {
            if ( value instanceof Float)
                return value;
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
        }
        else if (instClass.isAssignableFrom(int.class))
        {
            if ( value instanceof Integer)
                return value;
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
        }
        else if (instClass.isAssignableFrom(long.class))
        {
            if ( value instanceof Long)
                return value;
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
        }
        else if (instClass.isAssignableFrom(short.class))
        {
            if ( value instanceof Short)
                return value;
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
        }
        else if (instClass.isAssignableFrom(String.class))
        {
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
            {
                TypeXML typeXML = _sdoContext.getTypeSystem().getTypeXML(type);
                if (BuiltInTypeSystem.DATETIME.isAssignableFrom(typeXML))
                    return DataHelperImpl._toDateTime((Date)value);
                if (BuiltInTypeSystem.DAY.isAssignableFrom(typeXML))
                    return DataHelperImpl._toDay((Date)value);
                if (BuiltInTypeSystem.DURATION.isAssignableFrom(typeXML))
                    return DataHelperImpl._toDuration((Date)value);
                if (BuiltInTypeSystem.MONTH.isAssignableFrom(typeXML))
                    return DataHelperImpl._toMonth((Date)value);
                if (BuiltInTypeSystem.MONTHDAY.isAssignableFrom(typeXML))
                    return DataHelperImpl._toMonthDay((Date)value);
                if (BuiltInTypeSystem.TIME.isAssignableFrom(typeXML))
                    return DataHelperImpl._toTime((Date)value);
                if (BuiltInTypeSystem.YEAR.isAssignableFrom(typeXML))
                    return DataHelperImpl._toYear((Date)value);
                if (BuiltInTypeSystem.YEARMONTH.isAssignableFrom(typeXML))
                    return DataHelperImpl._toYearMonth((Date)value);
                if (BuiltInTypeSystem.YEARMONTHDAY.isAssignableFrom(typeXML))
                    return DataHelperImpl._toYearMonthDay((Date)value);
                if (BuiltInTypeSystem.STRING.isAssignableFrom(typeXML))
                    return DataHelperImpl._toDateTime((Date)value);
            }
            if ( value instanceof List)
            {
                //TypeXML typeXML = TypeImpl.getTypeXML(type);
                if (BuiltInTypeSystem.STRINGS.isInstance(value))
                {
                    List<String> strings = ((List<String>)value);
                    if (strings.size() == 0) return null;
                    String res = "";
                    for (int i = 0; i < strings.size(); i++)
                    {
                        if (i>0)
                            res += " ";
                        String s = strings.get(i);
                        res += (s == null ? "" : s);
                    }
                    return res;
                }
                else return ((List)value).toString();
            }
        }
        else if (instClass.isAssignableFrom(byte[].class))
        {
            if ( value instanceof byte[])
                return value;
            if ( value instanceof BigInteger)
                return ((BigInteger)value).toByteArray();
            if ( value instanceof String)
                return HexBin.stringToBytes((String)value);
        }
        else if (instClass.isAssignableFrom(BigDecimal.class))
        {
            if ( value instanceof BigDecimal)
                return value;
            if ( value instanceof String)
                return new BigDecimal((String)value);
            if ( value instanceof Byte)
                return BigDecimal.valueOf((Byte)value);
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
            if ( value instanceof BigInteger)
                return new BigDecimal((BigInteger)value);
        }
        else if (instClass.isAssignableFrom(BigInteger.class))
        {
            if ( value instanceof BigInteger)
                return value;
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
        }
        else if (instClass.isAssignableFrom(Date.class))
        {
            if ( value instanceof Date)
                return value;
            if ( value instanceof Long)
                return new Date((Long)value);
            if ( value instanceof String)
                return DataHelperImpl._toDate((String)value);
        }
        else if (instClass.isAssignableFrom(List.class))
        {
            if ( value instanceof List)
                return value;
            else
            {
                TypeXML typeXML = _sdoContext.getTypeSystem().getTypeXML(type);
                if (BuiltInTypeSystem.STRINGS.isAssignableFrom(typeXML))
                {
                    // only Strings and String are convertible to Strings
                    // value is not of type Strings since it is not instanceof List
                    // so we need only convert a String here
                    // if value is not a String, a ClassCastException will be thrown
                    // which is the correct behavior
                    return Arrays.asList(((String)value).trim().split("\\s+"));
                }
            }
        }

        throw new IllegalArgumentException("Unable to convert value '" + value + "' to instanceClass '" + instClass + "' of type '" + type + "'.");
    }

    public Object convert(Property property, Object value)
    {
        if (value==null || property==null)
            return null;

        return convert(property.getType(), value);
    }

    public static String _toDateTime(Date date)
    {
        if (date == null) return null;
        GDateBuilder gdb = new GDateBuilder(date);
        gdb.normalizeToTimeZone(0);
        gdb.setBuiltinTypeCode(SchemaType.BTC_DATE_TIME);
        return gdb.canonicalString();
    }

    public static String _toDuration(Date date)
    {
        if (date == null) return null;
        GDateBuilder gdb = new GDateBuilder(date);
        gdb.normalizeToTimeZone(0);
        Calendar cal = new XmlCalendar(gdb);
        return _toDuration(cal);
    }

    public static String _toTime(Date date)
    {
        if (date == null) return null;
        GDateBuilder gdb = new GDateBuilder(date);
        gdb.normalizeToTimeZone(0);
        gdb.setBuiltinTypeCode(SchemaType.BTC_TIME);
        return gdb.canonicalString();
    }

    public static String _toDay(Date date)
    {
        if (date == null) return null;
        GDateBuilder gdb = new GDateBuilder(date);
        gdb.normalizeToTimeZone(0);
        gdb.setBuiltinTypeCode(SchemaType.BTC_G_DAY);
        String s = gdb.canonicalString();
        int k = s.lastIndexOf('Z');
        return ((k > -1) ? s.substring(0, k) : s);
    }

    public static String _toMonth(Date date)
    {
        if (date == null) return null;
        GDateBuilder gdb = new GDateBuilder(date);
        gdb.normalizeToTimeZone(0);
        gdb.setBuiltinTypeCode(SchemaType.BTC_G_MONTH);
        //return gdb.canonicalString();
        String s = gdb.canonicalString();
        int k = s.lastIndexOf('Z');
        return ((k > -1) ? s.substring(0, k) : s);
    }

    public static String _toMonthDay(Date date)
    {
        if (date == null) return null;
        GDateBuilder gdb = new GDateBuilder(date);
        gdb.normalizeToTimeZone(0);
        gdb.setBuiltinTypeCode(SchemaType.BTC_G_MONTH_DAY);
        //return gdb.canonicalString();
        String s = gdb.canonicalString();
        int k = s.lastIndexOf('Z');
        return ((k > -1) ? s.substring(0, k) : s);
    }

    public static String _toYear(Date date)
    {
        if (date == null) return null;
        GDateBuilder gdb = new GDateBuilder(date);
        gdb.normalizeToTimeZone(0);
        gdb.setBuiltinTypeCode(SchemaType.BTC_G_YEAR);
        //return gdb.canonicalString();
        String s = gdb.canonicalString();
        int k = s.lastIndexOf('Z');
        return ((k > -1) ? s.substring(0, k) : s);
    }

    public static String _toYearMonth(Date date)
    {
        if (date == null) return null;
        GDateBuilder gdb = new GDateBuilder(date);
        gdb.normalizeToTimeZone(0);
        gdb.setBuiltinTypeCode(SchemaType.BTC_G_YEAR_MONTH);
        //return gdb.canonicalString();
        String s = gdb.canonicalString();
        int k = s.lastIndexOf('Z');
        return ((k > -1) ? s.substring(0, k) : s);
    }

    public static String _toYearMonthDay(Date date)
    {
        if (date == null) return null;
        GDateBuilder gdb = new GDateBuilder(date);
        gdb.normalizeToTimeZone(0);
        gdb.setBuiltinTypeCode(SchemaType.BTC_DATE);
        //return gdb.canonicalString();
        String s = gdb.canonicalString();
        int k = s.lastIndexOf('Z');
        return ((k > -1) ? s.substring(0, k) : s);
    }

    public static Date _toDate(String dateString)
    {
        if (dateString == null) return null;
        Calendar cal = _toCalendar(dateString, null);
        if (!cal.isSet(Calendar.ZONE_OFFSET))
            cal.setTimeZone(TimeZone.getTimeZone("GMT"));

        cal.setLenient(true);
        // [W.Y.] Q. for Cezar: are the two lines below necessary?
        // I'm commenting them out to fix the failures of CR305225.
        //if (!cal.isSet(Calendar.YEAR))
        //    cal.set(Calendar.YEAR, -1);
        
        return cal.getTime();
    }

    public static Calendar _toCalendar(String dateString, Locale locale)
    {
        try
        {
            GDateBuilder gdb = new GDateBuilder(dateString);
            return gdb.getCalendar();
        }
        catch (IllegalArgumentException iae)
        {
            GDuration gDuration = new GDuration(dateString);

//            int sign = gDuration.getSign();
//            int year = gDuration.getYear();
//            int era = GregorianCalendar.AD;
//            if (sign<0 || year==0 )
//            {
//                era = GregorianCalendar.BC;
//                //year = -1 - year; without ERA set
//                year = year + 2;
//            }
//
//            int month = 1 + sign * gDuration.getMonth();
//            int day = 1 + sign * gDuration.getDay();
//            int hour = sign * gDuration.getHour();
//            int minute = sign * gDuration.getMinute();
//            int sec = sign * gDuration.getSecond();
//
//            BigDecimal durationFraction = gDuration.getFraction();
//            BigDecimal fraction = ( sign>0 ? durationFraction : durationFraction.negate() ) /* + gdb.getMillisecond() which is always 0 */;
//            int ms = sign * fraction.multiply(new BigDecimal(1000)).stripTrailingZeros().intValue();
//
//            XmlCalendar cal = new XmlCalendar();
//            cal.setLenient(true);
//            cal.set(Calendar.ERA, era);
//            cal.set(year, month-1 /*zero based*/, day, hour, minute, sec);
//            cal.set(Calendar.MILLISECOND, ms);
//            cal.set(Calendar.ZONE_OFFSET, 0);
//
//            return cal;

            GDateBuilder gdb = new GDateBuilder("-0001-01-01T00:00:00Z");
            gdb.addGDuration(gDuration);
            return gdb.getCalendar();
        }
    }

    public static String _toTime(String date)
    {
        GDateBuilder gdb = new GDateBuilder(date);
        int btc = gdb.getBuiltinTypeCode();
        if (btc != SchemaType.BTC_DATE_TIME &&
            btc != SchemaType.BTC_TIME)
            throw new IllegalArgumentException();
        gdb.setBuiltinTypeCode(SchemaType.BTC_TIME);
        return gdb.toString();
    }

    public static String _toDateTime(String date)
    {
        GDateBuilder gdb = new GDateBuilder(date);
        int btc = gdb.getBuiltinTypeCode();
        if (btc != SchemaType.BTC_DATE_TIME)
            throw new IllegalArgumentException();
        //gdb.setBuiltinTypeCode(SchemaType.BTC_DATE_TIME);
        return gdb.toString();
    }

    public static String _toDuration(String date)
    {
        try
        {
            return DatatypeFactory.newInstance().newDuration(date).toString();
        }
        catch (DatatypeConfigurationException e)
        {
            throw new ClassCastException("String value '" + date + "' cannot be converted to Duration.");
        }
    }

    public static String _toDay(String date)
    {
        GDateBuilder gdb = new GDateBuilder(date);
        int btc = gdb.getBuiltinTypeCode();
        if (btc != SchemaType.BTC_DATE_TIME &&
            btc != SchemaType.BTC_DATE &&
            btc != SchemaType.BTC_G_MONTH_DAY &&
            btc != SchemaType.BTC_G_DAY)
            throw new IllegalArgumentException();
        gdb.setBuiltinTypeCode(SchemaType.BTC_G_DAY);
        return gdb.toString();
    }

    public static String _toMonth(String date)
    {
        GDateBuilder gdb = new GDateBuilder(date);
        int btc = gdb.getBuiltinTypeCode();
        if (btc != SchemaType.BTC_DATE_TIME &&
            btc != SchemaType.BTC_DATE &&
            btc != SchemaType.BTC_G_YEAR_MONTH &&
            btc != SchemaType.BTC_G_MONTH_DAY &&
            btc != SchemaType.BTC_G_MONTH)
            throw new IllegalArgumentException();
        gdb.setBuiltinTypeCode(SchemaType.BTC_G_MONTH);
        return gdb.toString();
    }

    public static String _toMonthDay(String date)
    {
        GDateBuilder gdb = new GDateBuilder(date);
        int btc = gdb.getBuiltinTypeCode();
        if (btc != SchemaType.BTC_DATE_TIME &&
            btc != SchemaType.BTC_DATE &&
            btc != SchemaType.BTC_G_MONTH_DAY)
            throw new IllegalArgumentException();
        gdb.setBuiltinTypeCode(SchemaType.BTC_G_MONTH_DAY);
        return gdb.toString();
    }

    public static String _toYear(String date)
    {
        GDateBuilder gdb = new GDateBuilder(date);
        int btc = gdb.getBuiltinTypeCode();
        if (btc != SchemaType.BTC_DATE_TIME &&
            btc != SchemaType.BTC_DATE &&
            btc != SchemaType.BTC_G_YEAR_MONTH &&
            btc != SchemaType.BTC_G_YEAR)
            throw new IllegalArgumentException();
        gdb.setBuiltinTypeCode(SchemaType.BTC_G_YEAR);
        return gdb.toString();
    }

    public static String _toYearMonth(String date)
    {
        GDateBuilder gdb = new GDateBuilder(date);
        int btc = gdb.getBuiltinTypeCode();
        if (btc != SchemaType.BTC_DATE_TIME &&
            btc != SchemaType.BTC_DATE &&
            btc != SchemaType.BTC_G_YEAR_MONTH)
            throw new IllegalArgumentException();
        gdb.setBuiltinTypeCode(SchemaType.BTC_G_YEAR_MONTH);
        return gdb.toString();
    }

    public static String _toYearMonthDay(String date)
    {
        GDateBuilder gdb = new GDateBuilder(date);
        int btc = gdb.getBuiltinTypeCode();
        if (btc != SchemaType.BTC_DATE_TIME &&
            btc != SchemaType.BTC_DATE)
            throw new IllegalArgumentException();
        gdb.setBuiltinTypeCode(SchemaType.BTC_DATE);
        return gdb.toString();
    }
}
