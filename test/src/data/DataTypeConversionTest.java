/*   Copyright 2008 BEA Systems, Inc.
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
 *   limitations under the License.
 */
package data;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.TimeZone;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import javax.sdo.DataObject;
import javax.sdo.Type;
import javax.sdo.Property;
import javax.sdo.helper.TypeHelper;
import javax.sdo.helper.DataHelper;

import org.apache.xmlbeans.GDate;
import org.apache.xmlbeans.GDuration;
import org.apache.xmlbeans.XmlCalendar;

import junit.framework.*;

/**
 * Tests for data type conversion.
 * @author Wing Yew Poon
 */
public class DataTypeConversionTest extends DataObjectTest
{
    public DataTypeConversionTest(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        
        TestSuite suite = new TestSuite();
        
        suite.addTest(new DataTypeConversionTest("testBooleanConversion"));
        suite.addTest(new DataTypeConversionTest("testByteConversion"));
        suite.addTest(new DataTypeConversionTest("testCharacterConversion"));
        suite.addTest(new DataTypeConversionTest("testDoubleConversion"));
        suite.addTest(new DataTypeConversionTest("testFloatConversion"));
        suite.addTest(new DataTypeConversionTest("testIntConversion"));
        suite.addTest(new DataTypeConversionTest("testLongConversion"));
        suite.addTest(new DataTypeConversionTest("testShortConversion"));
        suite.addTest(new DataTypeConversionTest("testStringConversion"));
        suite.addTest(new DataTypeConversionTest("testBytesConversion"));
        suite.addTest(new DataTypeConversionTest("testDecimalConversion"));
        suite.addTest(new DataTypeConversionTest("testIntegerConversion"));
        suite.addTest(new DataTypeConversionTest("testDateConversion"));
        suite.addTest(new DataTypeConversionTest("testStringsConversion"));
        
        suite.addTest(new DataTypeConversionTest("testDurationConversion"));
        suite.addTest(new DataTypeConversionTest("testDateTimeConversion"));
        suite.addTest(new DataTypeConversionTest("testTimeConversion"));
        suite.addTest(new DataTypeConversionTest("testDayConversion"));
        suite.addTest(new DataTypeConversionTest("testMonthConversion"));
        suite.addTest(new DataTypeConversionTest("testMonthDayConversion"));
        suite.addTest(new DataTypeConversionTest("testYearConversion"));
        suite.addTest(new DataTypeConversionTest("testYearMonthConversion"));
        suite.addTest(new DataTypeConversionTest("testYearMonthDayConversion"));
        
        // or
        //TestSuite suite = new TestSuite(DataTypeConversionTest.class);
        return suite;
    }

    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(suite());
    }

    private static DataHelper dataHelper = context.getDataHelper();

    static final Date YEAR_ZERO; // = (new GDate("0000-01-01T00:00:00Z")).getDate();
    static
    {
        Calendar xcal = new XmlCalendar();
        //System.out.println("xcal:");
        //dumpCalendar(xcal);
        xcal.clear();
        if (xcal.getTimeZone() != null)
            xcal.setTimeZone(TimeZone.getTimeZone("GMT"));
        Date xd0 = xcal.getTime();
        /*
        GDate base = new GDate("0000-01-01T00:00:00Z");
        Date d0 = base.getDate();
        DateFormat f = new SimpleDateFormat("G yyyy-MM-dd'T'HH:mm:ss'.'SSSz");
        f.setTimeZone(xcal.getTimeZone());
        System.out.println(f.format(xd0));
        System.out.println(f.format(d0));
        System.out.println(xd0.equals(d0) ? 
                           "xcal date equals year zero" :
                           "xcal date does not equal year zero");
        */
        YEAR_ZERO = xd0;
    }

    // SDO type name, method suffix, java class, property name, property index
    static final Object[][] TYPES = 
        { {"Boolean", "Boolean", boolean.class, "boolean", BOOLEAN_P_I},
          {"Byte", "Byte", byte.class, "byte", BYTE_P_I},
          {"Character", "Char", char.class, "character", CHARACTER_P_I},
          {"Double", "Double", double.class, "double", DOUBLE_P_I},
          {"Float", "Float", float.class, "float", FLOAT_P_I},
          {"Int", "Int", int.class, "int", INT_P_I},
          {"Long", "Long", long.class, "long", LONG_P_I},
          {"Short", "Short", short.class, "short", SHORT_P_I},
          {"String", "String", String.class, "string", STRING_P_I},
          {"Bytes", "Bytes", byte[].class, "bytes", BYTES_P_I},
          {"Decimal", "BigDecimal", BigDecimal.class, "decimal", DECIMAL_P_I},
          {"Integer", "BigInteger", BigInteger.class, "integer", INTEGER_P_I},
          {"Date", "Date", Date.class, "date", DATE_P_I},
          {"Day", "String", String.class, "day", DAY_P_I},
          {"DateTime", "String", String.class, "dateTime", DATETIME_P_I},
          {"Duration", "String", String.class, "duration", DURATION_P_I},
          {"Month", "String", String.class, "month", MONTH_P_I},
          {"MonthDay", "String", String.class, "monthDay", MONTHDAY_P_I},
          {"Time", "String", String.class, "time", TIME_P_I},
          {"Year", "String", String.class, "year", YEAR_P_I},
          {"YearMonth", "String", String.class, "yearMonth", YEARMONTH_P_I},
          {"YearMonthDay", "String", String.class, "yearMonthDay", YEARMONTHDAY_P_I},
          {"Strings", "List", List.class, "strings", STRINGS_P_I}
        };

    static Map typeMap = new HashMap();
    static
    {
        for (int i = 0; i < TYPES.length; i++)
        {
            Object[] row = TYPES[i];
            Map m = new HashMap();
            m.put("method_suffix", row[1]);
            m.put("java_class", row[2]);
            m.put("path", row[3]);
            m.put("index", row[4]);
            m.put("property", basic_t.getProperty((String)row[3]));
            typeMap.put(row[0], m);
        }
    }

    protected static class ConversionTest
    {
        String T1, T2;
        Class T1class;
        Type T2type;

        Method get_by_path;
        Method set_by_path;
        Method get_by_index;
        Method set_by_index;
        Method get_by_property;
        Method set_by_property;
        Method getT2_by_path;
        Method setT1_by_path;
        Method getT2_by_index;
        Method setT1_by_index;
        Method getT2_by_property;
        Method setT1_by_property;

        String s1;
        String s2;
        int i1;
        int i2;
        Property p1;
        Property p2;

        public ConversionTest(String T1, String T2) throws Exception
        {
            this.T1 = T1;
            this.T2 = T2;
            T1class = (Class)((Map)typeMap.get(T1)).get("java_class");
            T2type = typeHelper.getType("commonj.sdo", T2);
            get_by_path = DataObject.class.getMethod("get", String.class);
            set_by_path = DataObject.class.getMethod("set", String.class, Object.class);
            get_by_index = DataObject.class.getMethod("get", int.class);
            set_by_index = DataObject.class.getMethod("set", int.class, Object.class);
            get_by_property = DataObject.class.getMethod("get", Property.class);
            set_by_property = DataObject.class.getMethod("set", Property.class, Object.class);
            getT2_by_path = DataObject.class.getMethod("get" + ((Map)typeMap.get(T2)).get("method_suffix"), String.class);
            setT1_by_path = DataObject.class.getMethod("set" + ((Map)typeMap.get(T1)).get("method_suffix"), String.class, T1class);
            getT2_by_index = DataObject.class.getMethod("get" + ((Map)typeMap.get(T2)).get("method_suffix"), int.class);
            setT1_by_index = DataObject.class.getMethod("set" + ((Map)typeMap.get(T1)).get("method_suffix"), int.class, T1class);
            getT2_by_property = DataObject.class.getMethod("get" + ((Map)typeMap.get(T2)).get("method_suffix"), Property.class);
            setT1_by_property = DataObject.class.getMethod("set" + ((Map)typeMap.get(T1)).get("method_suffix"), Property.class, T1class);
            s1 = (String)((Map)typeMap.get(T1)).get("path");
            s2 = (String)((Map)typeMap.get(T2)).get("path");
            i1 = (Integer)((Map)typeMap.get(T1)).get("index");
            i2 = (Integer)((Map)typeMap.get(T2)).get("index");
            p1 = (Property)((Map)typeMap.get(T1)).get("property");
            p2 = (Property)((Map)typeMap.get(T2)).get("property");
        }

        public void convert(DataObject dobj, Object from, Object to)
            throws Exception
        {
            Type t = T2type;
            assertNotNull(t);
            Property p = p2;
            assertEquals(t, p2.getType());
            Object o1 = dataHelper.convert(t, from);
            Object o2 = dataHelper.convert(p, from);
            if (to instanceof byte[])
            {
                compareBytes((byte[])to, (byte[])o1);
                compareBytes((byte[])to, (byte[])o2);
            }
            else
            {
                assertEquals(to, o1);
                assertEquals(to, o2);
            }
            
            set_by_path.invoke(dobj, s1, from);
            if (to instanceof byte[])
                compareBytes((byte[])to, (byte[])getT2_by_path.invoke(dobj, s1));
            else
                assertEquals(to, getT2_by_path.invoke(dobj, s1));
            setT1_by_path.invoke(dobj, s2, from);
            if (to instanceof byte[])
                compareBytes((byte[])to, (byte[])get_by_path.invoke(dobj, s2));
            else
                assertEquals(to, get_by_path.invoke(dobj, s2));

            set_by_index.invoke(dobj, i1, from);
            if (to instanceof byte[])
                compareBytes((byte[])to, (byte[])getT2_by_index.invoke(dobj, i1));
            else
                assertEquals(to, getT2_by_index.invoke(dobj, i1));
            setT1_by_index.invoke(dobj, i2, from);
            if (to instanceof byte[])
                compareBytes((byte[])to, (byte[])get_by_index.invoke(dobj, i2));
            else
                assertEquals(to, get_by_index.invoke(dobj, i2));

            set_by_property.invoke(dobj, p1, from);
            if (to instanceof byte[])
                compareBytes((byte[])to, (byte[])getT2_by_property.invoke(dobj, p1));
            else
                assertEquals(to, getT2_by_property.invoke(dobj, p1));
            setT1_by_property.invoke(dobj, p2, from);
            if (to instanceof byte[])
                compareBytes((byte[])to, (byte[])get_by_property.invoke(dobj, p2));
            else
                assertEquals(to, get_by_property.invoke(dobj, p2));
            
        }

        private boolean checkException(Exception e, Class c)
        {
            if (e.getClass().equals(c)) return true;
            else if ((e.getCause() != null)
                     && e.getCause().getClass().equals(c)) return true;
            else return false;
        }

        public void failToConvert(DataObject dobj, Object from, Class exceptionClass)
            throws Exception
        {
            Class c = exceptionClass;
            boolean failOnGetByPath = false;
            boolean failOnSetByPath = false;
            boolean failOnGetByIndex = false;
            boolean failOnSetByIndex = false;
            boolean failOnGetByProperty = false;
            boolean failOnSetByProperty = false;
            boolean failOnConvertByType = false;
            boolean failOnConvertByProperty = false;

            set_by_path.invoke(dobj, s1, from);
            try { getT2_by_path.invoke(dobj, s1); }
            catch (Exception e) { failOnGetByPath = checkException(e, c); }
            try { getT2_by_index.invoke(dobj, i1); }
            catch (Exception e) { failOnGetByIndex = checkException(e, c); }
            try { getT2_by_property.invoke(dobj, p1); }
            catch (Exception e) { failOnGetByProperty = checkException(e, c); }

            try { setT1_by_path.invoke(dobj, s2, from); }
            catch (Exception e) { failOnSetByPath = checkException(e, c); }
            try { setT1_by_index.invoke(dobj, i2, from); }
            catch (Exception e) { failOnSetByIndex = checkException(e, c); }
            try { setT1_by_property.invoke(dobj, p2, from); }
            catch (Exception e) { failOnSetByProperty = checkException(e, c); }

            Type t = T2type;
            assertNotNull(t);
            Property p = p2;
            assertEquals(t, p2.getType());
            // DataHelper.convert() methods throw IllegalArgumentException
            // if the value cannot be converted
            try { dataHelper.convert(t, from); }
            catch (Exception e) { failOnConvertByType = checkException(e, IllegalArgumentException.class); }
            try { dataHelper.convert(p, from); }
            catch (Exception e) { failOnConvertByProperty = checkException(e, IllegalArgumentException.class); }

            //System.out.println("failOnGetByPath: " + failOnGetByPath);
            //System.out.println("failOnGetByIndex: " + failOnGetByIndex);
            //System.out.println("failOnGetByProperty: " + failOnGetByProperty);
            //System.out.println("failOnSetByPath: " + failOnSetByPath);
            //System.out.println("failOnSetByIndex: " + failOnSetByIndex);
            //System.out.println("failOnSetByProperty: " + failOnSetByProperty);
            //System.out.println("failOnConvertByType: " + failOnConvertByType);
            //System.out.println("failOnConvertByProperty: " + failOnConvertByProperty);

            if (!failOnGetByPath) System.out.println(getT2_by_path.getName() + "(String) on property of type " + T1 + " should have thrown " + c.getName());
            if (!failOnGetByIndex) System.out.println(getT2_by_index.getName() + "(int) on property of type " + T1 + " should have thrown " + c.getName());
            if (!failOnGetByProperty) System.out.println(getT2_by_property.getName() + "(Property) on property of type " + T1 + " should have thrown " + c.getName());
            if (!failOnSetByPath) System.out.println(setT1_by_path.getName() + "(String, " + T1class.getSimpleName() + ") on property of type " + T2 + " should have thrown " + c.getName());
            if (!failOnSetByIndex) System.out.println(setT1_by_index.getName() + "(int, " + T1class.getSimpleName() + ") on property of type " + T2 + " should have thrown " + c.getName());
            if (!failOnSetByProperty) System.out.println(setT1_by_property.getName() + "(Property, " + T1class.getSimpleName() + ") on property of type " + T2 + " should have thrown " + c.getName());
            if (!failOnConvertByType) System.out.println("DataHelper.convert(Type type, Object value) with type " + T2 + " and value " + from + " should have thrown IllegalArgumentException");
            if (!failOnConvertByProperty) System.out.println("DataHelper.convert(Property property, Object value) with property of type " + T2 + " and value " + from + " should have thrown IllegalArgumentException");

            assertTrue(failOnGetByPath &&
                       failOnGetByIndex &&
                       failOnGetByProperty &&
                       failOnSetByPath &&
                       failOnSetByIndex &&
                       failOnSetByProperty &&
                       failOnConvertByType &&
                       failOnConvertByProperty);
        }
    }

    // ConversionToDateTest, ConversionToStringTest
    protected static class ConversionTestA extends ConversionTest
    {
        public ConversionTestA(String T1, String T2) throws Exception
        {
            super(T1, T2); // T2 = Date/String
        }

        // override
        public void convert(DataObject dobj, Object from, Object to)
            throws Exception
        {
            Type t = T2type;
            assertNotNull(t);
            Property p = p2;
            assertEquals(t, p2.getType());
            Object o1 = dataHelper.convert(t, from);
            Object o2 = dataHelper.convert(p, from);
            if (to instanceof byte[])
            {
                compareBytes((byte[])to, (byte[])o1);
                compareBytes((byte[])to, (byte[])o2);
            }
            else
            {
                assertEquals(to, o1);
                assertEquals(to, o2);
            }
            
            set_by_path.invoke(dobj, s1, from);
            assertEquals(to, getT2_by_path.invoke(dobj, s1));

            set_by_index.invoke(dobj, i1, from);
            assertEquals(to, getT2_by_index.invoke(dobj, i1));

            set_by_property.invoke(dobj, p1, from);
            assertEquals(to, getT2_by_property.invoke(dobj, p1));
            
        }

        public void failToConvert(DataObject dobj, Object from, Class exceptionClass)
            throws Exception
        {
            throw new UnsupportedOperationException();
        }
    }

    // ConversionFromDateTest, ConversionFromStringTest
    protected static class ConversionTestB extends ConversionTest
    {
        public ConversionTestB(String T1, String T2) throws Exception
        {
            super(T1, T2); // T1 = Date/String
        }

        // override
        public void convert(DataObject dobj, Object from, Object to)
            throws Exception
        {
            Type t = T2type;
            assertNotNull(t);
            Property p = p2;
            assertEquals(t, p2.getType());
            Object o1 = dataHelper.convert(t, from);
            Object o2 = dataHelper.convert(p, from);
            if (to instanceof byte[])
            {
                compareBytes((byte[])to, (byte[])o1);
                compareBytes((byte[])to, (byte[])o2);
            }
            else
            {
                assertEquals(to, o1);
                assertEquals(to, o2);
            }
            
            setT1_by_path.invoke(dobj, s2, from);
            assertEquals(to, get_by_path.invoke(dobj, s2));

            setT1_by_index.invoke(dobj, i2, from);
            assertEquals(to, get_by_index.invoke(dobj, i2));

            setT1_by_property.invoke(dobj, p2, from);
            assertEquals(to, get_by_property.invoke(dobj, p2));
            
        }

        public void failToConvert(DataObject dobj, Object from, Class exceptionClass)
            throws Exception
        {
            throw new UnsupportedOperationException();
        }
    }

    protected static class ConversionToDateTest extends ConversionTestA
    {
        public ConversionToDateTest(String T) throws Exception
        {
            super(T, "Date");
        }
    }

    protected static class ConversionFromDateTest extends ConversionTestB
    {
        public ConversionFromDateTest(String T) throws Exception
        {
            super("Date", T);
        }
    }

    private static void dumpCalendar(Calendar cal)
    {
        if (cal.isSet(Calendar.ERA))
            System.out.println("era: " + cal.get(Calendar.ERA));
        if (cal.isSet(Calendar.YEAR))
            System.out.println("year: " + cal.get(Calendar.YEAR));
        if (cal.isSet(Calendar.MONTH))
            System.out.println("month: " + (cal.get(Calendar.MONTH)+1));
        if (cal.isSet(Calendar.DAY_OF_MONTH))
            System.out.println("day: " + cal.get(Calendar.DAY_OF_MONTH));
        if (cal.isSet(Calendar.HOUR_OF_DAY))
            System.out.println("hour: " + cal.get(Calendar.HOUR_OF_DAY));
        if (cal.isSet(Calendar.MINUTE))
            System.out.println("minute: " + cal.get(Calendar.MINUTE));
        if (cal.isSet(Calendar.SECOND))
            System.out.println("second: " + cal.get(Calendar.SECOND));
        if (cal.isSet(Calendar.MILLISECOND))
            System.out.println("millisecond: " + cal.get(Calendar.MILLISECOND));
        if (cal.isSet(Calendar.ZONE_OFFSET))
            System.out.println("zone offset: " +
                               ((cal.get(Calendar.ZONE_OFFSET)/1000)/3600) + ":" +
                               ((cal.get(Calendar.ZONE_OFFSET)/1000)%3600));
        else
            System.out.println("zone offset is not set.");
    }

    private static void dumpDuration(GDuration gDur)
    {
        System.out.println("duration:");
        System.out.println((gDur.getSign() > 0) ? "+" : "-");
        System.out.println(gDur.getYear());
        System.out.println(gDur.getMonth());
        System.out.println(gDur.getDay());
        System.out.println(gDur.getHour());
        System.out.println(gDur.getMinute());
        System.out.println(gDur.getSecond());
        System.out.println(gDur.getFraction().toPlainString());
    }

    static Date toDate(String s)
    {
        GDate gDate = new GDate(s);
        //Date date = gDate.getDate();
        // we cannot always get a Date directly from a GDate,
        // could get
        // java.lang.IllegalStateException: cannot do date math without a complete date
        // instead, get a Calendar from the GDate and then a Date from the Calendar
        Calendar cal = gDate.getCalendar();
        //System.out.println(cal.isSet(Calendar.ZONE_OFFSET));
        //System.out.println(cal.getTimeZone());
        if (!cal.isSet(Calendar.ZONE_OFFSET))
            cal.setTimeZone(TimeZone.getTimeZone("GMT"));
        //dumpCalendar(cal);
        Date dateFromCalendar = cal.getTime();
        /*
        Calendar xcal = new XmlCalendar();
        xcal.setTime(dateFromCalendar);
        if (cal.getTimeZone() != null)
            xcal.setTimeZone(cal.getTimeZone());
        //dumpCalendar(xcal);

        DateFormat f = new SimpleDateFormat("G yyyy-MM-dd'T'HH:mm:ss'.'SSSz");
        f.setTimeZone(cal.getTimeZone());
        System.out.println(s);
        System.out.println(f.format(dateFromCalendar));
        */
        return dateFromCalendar;
    }

    static Date toDateFromDuration(String s) throws Exception
    {
        //String posDur = s.startsWith("-") ? s.substring(1) : s;
        GDuration gDur = new GDuration(s); //(posDur);
        //dumpDuration(gDur);
        GDate base = new GDate("-0001-01-01T00:00:00Z");
        //GDate gDate = s.startsWith("-") ? base.subtract(gDur) : base.add(gDur);
        GDate gDate = base.add(gDur);
        Calendar cal = gDate.getCalendar();
        Date date = gDate.getDate();
        Date dateFromCalendar = cal.getTime();
        Assert.assertEquals(date, dateFromCalendar);
        /*
        DatatypeFactory dtf = DatatypeFactory.newInstance();
        Duration dur = dtf.newDuration(s);
        Date date = (Date)YEAR_ZERO.clone();
        dur.addTo(date);
        */
        DateFormat f = new SimpleDateFormat("G yyyy-MM-dd'T'HH:mm:ss'.'SSSz");
        f.setTimeZone(TimeZone.getTimeZone("GMT"));
        //System.out.println(s);
        //System.out.println(f.format(date));
        return date;
    }

    public void testBooleanConversion() throws Exception
    {
        ConversionTest fromBooleanToByte = new ConversionTest("Boolean", "Byte");
        ConversionTest fromBooleanToCharacter = new ConversionTest("Boolean", "Character");
        ConversionTest fromBooleanToDouble = new ConversionTest("Boolean", "Double");
        ConversionTest fromBooleanToFloat = new ConversionTest("Boolean", "Float");
        ConversionTest fromBooleanToInt = new ConversionTest("Boolean", "Int");
        ConversionTest fromBooleanToLong = new ConversionTest("Boolean", "Long");
        ConversionTest fromBooleanToShort = new ConversionTest("Boolean", "Short");
        ConversionTest fromBooleanToString = new ConversionTest("Boolean", "String");
        ConversionTest fromBooleanToBytes = new ConversionTest("Boolean", "Bytes");
        ConversionTest fromBooleanToDecimal = new ConversionTest("Boolean", "Decimal");
        ConversionTest fromBooleanToInteger = new ConversionTest("Boolean", "Integer");
        ConversionTest fromBooleanToDate = new ConversionTest("Boolean", "Date");

        DataObject dobj = createDataObject();

        // supported
        fromBooleanToString.convert(dobj, true, "true");
        fromBooleanToString.convert(dobj, false, "false");

        // unsupported
        fromBooleanToByte.failToConvert(dobj, true, ClassCastException.class);
        fromBooleanToCharacter.failToConvert(dobj, true, ClassCastException.class);
        fromBooleanToDouble.failToConvert(dobj, true, ClassCastException.class);
        fromBooleanToFloat.failToConvert(dobj, true, ClassCastException.class);
        fromBooleanToInt.failToConvert(dobj, true, ClassCastException.class);
        fromBooleanToLong.failToConvert(dobj, true, ClassCastException.class);
        fromBooleanToShort.failToConvert(dobj, true, ClassCastException.class);
        fromBooleanToBytes.failToConvert(dobj, true, ClassCastException.class);
        fromBooleanToDecimal.failToConvert(dobj, true, ClassCastException.class);
        fromBooleanToInteger.failToConvert(dobj, true, ClassCastException.class);
        fromBooleanToDate.failToConvert(dobj, true, ClassCastException.class);
    }

    public void testByteConversion() throws Exception
    {
        ConversionTest fromByteToBoolean = new ConversionTest("Byte", "Boolean");
        ConversionTest fromByteToCharacter = new ConversionTest("Byte", "Character");
        ConversionTest fromByteToDouble = new ConversionTest("Byte", "Double");
        ConversionTest fromByteToFloat = new ConversionTest("Byte", "Float");
        ConversionTest fromByteToInt = new ConversionTest("Byte", "Int");
        ConversionTest fromByteToLong = new ConversionTest("Byte", "Long");
        ConversionTest fromByteToShort = new ConversionTest("Byte", "Short");
        ConversionTest fromByteToString = new ConversionTest("Byte", "String");
        ConversionTest fromByteToBytes = new ConversionTest("Byte", "Bytes");
        ConversionTest fromByteToDecimal = new ConversionTest("Byte", "Decimal");
        ConversionTest fromByteToInteger = new ConversionTest("Byte", "Integer");
        ConversionTest fromByteToDate = new ConversionTest("Byte", "Date");

        DataObject dobj = createDataObject();

        // supported
        fromByteToDouble.convert(dobj, (byte)1, 1.0);
        fromByteToFloat.convert(dobj, (byte)1, (float)1.0);
        fromByteToInt.convert(dobj, (byte)1, 1);
        fromByteToLong.convert(dobj, (byte)1, 1l);
        fromByteToShort.convert(dobj, (byte)1, (short)1);
        fromByteToDecimal.convert(dobj, (byte)1, BigDecimal.valueOf(1));
        fromByteToInteger.convert(dobj, (byte)1, BigInteger.valueOf(1));
        fromByteToString.convert(dobj, (byte)1, "1");

        // unsupported
        fromByteToBoolean.failToConvert(dobj, (byte)1, ClassCastException.class);
        fromByteToCharacter.failToConvert(dobj, (byte)1, ClassCastException.class);
        fromByteToBytes.failToConvert(dobj, (byte)1, ClassCastException.class);
        //fromByteToDecimal.failToConvert(dobj, (byte)1, ClassCastException.class);
        //fromByteToInteger.failToConvert(dobj, (byte)1, ClassCastException.class);
        fromByteToDate.failToConvert(dobj, (byte)1, ClassCastException.class);
    }

    public void testCharacterConversion() throws Exception
    {
        ConversionTest fromCharacterToBoolean = new ConversionTest("Character", "Boolean");
        ConversionTest fromCharacterToByte = new ConversionTest("Character", "Byte");
        ConversionTest fromCharacterToDouble = new ConversionTest("Character", "Double");
        ConversionTest fromCharacterToFloat = new ConversionTest("Character", "Float");
        ConversionTest fromCharacterToInt = new ConversionTest("Character", "Int");
        ConversionTest fromCharacterToLong = new ConversionTest("Character", "Long");
        ConversionTest fromCharacterToShort = new ConversionTest("Character", "Short");
        ConversionTest fromCharacterToString = new ConversionTest("Character", "String");
        ConversionTest fromCharacterToBytes = new ConversionTest("Character", "Bytes");
        ConversionTest fromCharacterToDecimal = new ConversionTest("Character", "Decimal");
        ConversionTest fromCharacterToInteger = new ConversionTest("Character", "Integer");
        ConversionTest fromCharacterToDate = new ConversionTest("Character", "Date");

        DataObject dobj = createDataObject();

        // supported
        fromCharacterToString.convert(dobj, 'a', "a");
        fromCharacterToString.convert(dobj, 'Z', "Z");
        fromCharacterToString.convert(dobj, (char)0, "");

        // unsupported
        fromCharacterToBoolean.failToConvert(dobj, 'a', ClassCastException.class);
        fromCharacterToByte.failToConvert(dobj, 'a', ClassCastException.class);
        fromCharacterToDouble.failToConvert(dobj, 'a', ClassCastException.class);
        fromCharacterToFloat.failToConvert(dobj, 'a', ClassCastException.class);
        fromCharacterToInt.failToConvert(dobj, 'a', ClassCastException.class);
        fromCharacterToLong.failToConvert(dobj, 'a', ClassCastException.class);
        fromCharacterToShort.failToConvert(dobj, 'a', ClassCastException.class);
        fromCharacterToBytes.failToConvert(dobj, 'a', ClassCastException.class);
        fromCharacterToDecimal.failToConvert(dobj, 'a', ClassCastException.class);
        fromCharacterToInteger.failToConvert(dobj, 'a', ClassCastException.class);
        fromCharacterToDate.failToConvert(dobj, 'a', ClassCastException.class);
    }

    public void testDoubleConversion() throws Exception
    {
        ConversionTest fromDoubleToBoolean = new ConversionTest("Double", "Boolean");
        ConversionTest fromDoubleToByte = new ConversionTest("Double", "Byte");
        ConversionTest fromDoubleToCharacter = new ConversionTest("Double", "Character");
        ConversionTest fromDoubleToFloat = new ConversionTest("Double", "Float");
        ConversionTest fromDoubleToInt = new ConversionTest("Double", "Int");
        ConversionTest fromDoubleToLong = new ConversionTest("Double", "Long");
        ConversionTest fromDoubleToShort = new ConversionTest("Double", "Short");
        ConversionTest fromDoubleToString = new ConversionTest("Double", "String");
        ConversionTest fromDoubleToBytes = new ConversionTest("Double", "Bytes");
        ConversionTest fromDoubleToDecimal = new ConversionTest("Double", "Decimal");
        ConversionTest fromDoubleToInteger = new ConversionTest("Double", "Integer");
        ConversionTest fromDoubleToDate = new ConversionTest("Double", "Date");

        DataObject dobj = createDataObject();

        // supported
        fromDoubleToByte.convert(dobj, 3.14159, (byte)3);
        fromDoubleToFloat.convert(dobj, 3.14159, (float)3.14159);
        fromDoubleToInt.convert(dobj, 3.14159, 3);
        fromDoubleToLong.convert(dobj, 3.14159, 3l);
        fromDoubleToShort.convert(dobj, 3.14159, (short)3);
        fromDoubleToString.convert(dobj, 3.14159, "3.14159");
        fromDoubleToDecimal.convert(dobj, 3.14159, new BigDecimal("3.14159"));
        fromDoubleToInteger.convert(dobj, 3.14159, BigInteger.valueOf(3l));

        // unsupported
        fromDoubleToBoolean.failToConvert(dobj, 3.14159, ClassCastException.class);
        fromDoubleToCharacter.failToConvert(dobj, 3.14159, ClassCastException.class);
        fromDoubleToBytes.failToConvert(dobj, 3.14159, ClassCastException.class);
        fromDoubleToDate.failToConvert(dobj, 3.14159, ClassCastException.class);
    }

    public void testFloatConversion() throws Exception
    {
        ConversionTest fromFloatToBoolean = new ConversionTest("Float", "Boolean");
        ConversionTest fromFloatToByte = new ConversionTest("Float", "Byte");
        ConversionTest fromFloatToCharacter = new ConversionTest("Float", "Character");
        ConversionTest fromFloatToDouble = new ConversionTest("Float", "Double");
        ConversionTest fromFloatToInt = new ConversionTest("Float", "Int");
        ConversionTest fromFloatToLong = new ConversionTest("Float", "Long");
        ConversionTest fromFloatToShort = new ConversionTest("Float", "Short");
        ConversionTest fromFloatToString = new ConversionTest("Float", "String");
        ConversionTest fromFloatToBytes = new ConversionTest("Float", "Bytes");
        ConversionTest fromFloatToDecimal = new ConversionTest("Float", "Decimal");
        ConversionTest fromFloatToInteger = new ConversionTest("Float", "Integer");
        ConversionTest fromFloatToDate = new ConversionTest("Float", "Date");

        DataObject dobj = createDataObject();

        // supported
        fromFloatToByte.convert(dobj, (float)3.14159, (byte)3);
        fromFloatToDouble.convert(dobj, (float)3.14159, (double)((float)3.14159));
        fromFloatToInt.convert(dobj, (float)3.14159, 3);
        fromFloatToLong.convert(dobj, (float)3.14159, 3l);
        fromFloatToShort.convert(dobj, (float)3.14159, (short)3);
        fromFloatToString.convert(dobj, (float)3.14159, "3.14159");
        fromFloatToDecimal.convert(dobj, (float)3.14159, BigDecimal.valueOf((double)((float)3.14159)));
        fromFloatToInteger.convert(dobj, (float)3.14159, BigInteger.valueOf(3l));

        // unsupported
        fromFloatToBoolean.failToConvert(dobj, (float)3.14159, ClassCastException.class);
        fromFloatToCharacter.failToConvert(dobj, (float)3.14159, ClassCastException.class);
        fromFloatToBytes.failToConvert(dobj, (float)3.14159, ClassCastException.class);
        fromFloatToDate.failToConvert(dobj, (float)3.14159, ClassCastException.class);
    }

    public void testIntConversion() throws Exception
    {
        ConversionTest fromIntToBoolean = new ConversionTest("Int", "Boolean");
        ConversionTest fromIntToByte = new ConversionTest("Int", "Byte");
        ConversionTest fromIntToCharacter = new ConversionTest("Int", "Character");
        ConversionTest fromIntToDouble = new ConversionTest("Int", "Double");
        ConversionTest fromIntToFloat = new ConversionTest("Int", "Float");
        ConversionTest fromIntToLong = new ConversionTest("Int", "Long");
        ConversionTest fromIntToShort = new ConversionTest("Int", "Short");
        ConversionTest fromIntToString = new ConversionTest("Int", "String");
        ConversionTest fromIntToBytes = new ConversionTest("Int", "Bytes");
        ConversionTest fromIntToDecimal = new ConversionTest("Int", "Decimal");
        ConversionTest fromIntToInteger = new ConversionTest("Int", "Integer");
        ConversionTest fromIntToDate = new ConversionTest("Int", "Date");

        DataObject dobj = createDataObject();

        // supported
        fromIntToByte.convert(dobj, 3, (byte)3);
        fromIntToDouble.convert(dobj, 3, 3.0);
        fromIntToFloat.convert(dobj, 3, (float)3.0);
        fromIntToLong.convert(dobj, 3, 3l);
        fromIntToShort.convert(dobj, 3, (short)3);
        fromIntToString.convert(dobj, 3, "3");
        fromIntToDecimal.convert(dobj, 3, new BigDecimal(3));
        fromIntToInteger.convert(dobj, 3, BigInteger.valueOf(3l));

        // unsupported
        fromIntToBoolean.failToConvert(dobj, 3, ClassCastException.class);
        fromIntToCharacter.failToConvert(dobj, 3, ClassCastException.class);
        fromIntToBytes.failToConvert(dobj, 3, ClassCastException.class);
        fromIntToDate.failToConvert(dobj, 3, ClassCastException.class);
    }

    public void testLongConversion() throws Exception
    {
        ConversionTest fromLongToBoolean = new ConversionTest("Long", "Boolean");
        ConversionTest fromLongToByte = new ConversionTest("Long", "Byte");
        ConversionTest fromLongToCharacter = new ConversionTest("Long", "Character");
        ConversionTest fromLongToDouble = new ConversionTest("Long", "Double");
        ConversionTest fromLongToFloat = new ConversionTest("Long", "Float");
        ConversionTest fromLongToInt = new ConversionTest("Long", "Int");
        ConversionTest fromLongToShort = new ConversionTest("Long", "Short");
        ConversionTest fromLongToString = new ConversionTest("Long", "String");
        ConversionTest fromLongToBytes = new ConversionTest("Long", "Bytes");
        ConversionTest fromLongToDecimal = new ConversionTest("Long", "Decimal");
        ConversionTest fromLongToInteger = new ConversionTest("Long", "Integer");
        ConversionTest fromLongToDate = new ConversionTest("Long", "Date");

        DataObject dobj = createDataObject();

        // supported
        fromLongToByte.convert(dobj, 3l, (byte)3);
        fromLongToDouble.convert(dobj, 3l, 3.0);
        fromLongToFloat.convert(dobj, 3l, (float)3.0);
        fromLongToInt.convert(dobj, 3l, 3);
        fromLongToShort.convert(dobj, 3l, (short)3);
        fromLongToString.convert(dobj, 3l, "3");
        fromLongToDecimal.convert(dobj, 3l, new BigDecimal(3l));
        fromLongToInteger.convert(dobj, 3l, BigInteger.valueOf(3l));
        fromLongToDate.convert(dobj, 1163212477742l, new Date(1163212477742l));

        // unsupported
        fromLongToBoolean.failToConvert(dobj, 3, ClassCastException.class);
        fromLongToCharacter.failToConvert(dobj, 3, ClassCastException.class);
        fromLongToBytes.failToConvert(dobj, 3, ClassCastException.class);
    }

    public void testShortConversion() throws Exception
    {
        ConversionTest fromShortToBoolean = new ConversionTest("Short", "Boolean");
        ConversionTest fromShortToByte = new ConversionTest("Short", "Byte");
        ConversionTest fromShortToCharacter = new ConversionTest("Short", "Character");
        ConversionTest fromShortToDouble = new ConversionTest("Short", "Double");
        ConversionTest fromShortToFloat = new ConversionTest("Short", "Float");
        ConversionTest fromShortToInt = new ConversionTest("Short", "Int");
        ConversionTest fromShortToLong = new ConversionTest("Short", "Long");
        ConversionTest fromShortToString = new ConversionTest("Short", "String");
        ConversionTest fromShortToBytes = new ConversionTest("Short", "Bytes");
        ConversionTest fromShortToDecimal = new ConversionTest("Short", "Decimal");
        ConversionTest fromShortToInteger = new ConversionTest("Short", "Integer");
        ConversionTest fromShortToDate = new ConversionTest("Short", "Date");

        DataObject dobj = createDataObject();

        // supported
        fromShortToByte.convert(dobj, (short)1, (byte)1);
        fromShortToDouble.convert(dobj, (short)1, 1.0);
        fromShortToFloat.convert(dobj, (short)1, (float)1.0);
        fromShortToInt.convert(dobj, (short)1, 1);
        fromShortToLong.convert(dobj, (short)1, 1l);
        fromShortToDecimal.convert(dobj, (short)1, BigDecimal.valueOf(1));
        fromShortToInteger.convert(dobj, (short)1, BigInteger.valueOf(1));
        fromShortToString.convert(dobj, (short)1, "1");

        // unsupported
        fromShortToBoolean.failToConvert(dobj, (short)1, ClassCastException.class);
        fromShortToCharacter.failToConvert(dobj, (short)1, ClassCastException.class);
        fromShortToBytes.failToConvert(dobj, (short)1, ClassCastException.class);
        //fromShortToDecimal.failToConvert(dobj, (short)1, ClassCastException.class);
        //fromShortToInteger.failToConvert(dobj, (short)1, ClassCastException.class);
        fromShortToDate.failToConvert(dobj, (short)1, ClassCastException.class);
    }

    public void testStringConversion() throws Exception
    {
        ConversionTest fromStringToBoolean = new ConversionTest("String", "Boolean");
        ConversionTest fromStringToByte = new ConversionTest("String", "Byte");
        ConversionTest fromStringToCharacter = new ConversionTest("String", "Character");
        ConversionTest fromStringToDouble = new ConversionTest("String", "Double");
        ConversionTest fromStringToFloat = new ConversionTest("String", "Float");
        ConversionTest fromStringToInt = new ConversionTest("String", "Int");
        ConversionTest fromStringToLong = new ConversionTest("String", "Long");
        ConversionTest fromStringToShort = new ConversionTest("String", "Short");
        ConversionTest fromStringToBytes = new ConversionTest("String", "Bytes");
        ConversionTest fromStringToDecimal = new ConversionTest("String", "Decimal");
        ConversionTest fromStringToInteger = new ConversionTest("String", "Integer");
        ConversionTest fromStringToDate = new ConversionTest("String", "Date");
        ConversionTest fromStringToStrings = new ConversionTestB("String", "Strings");

        DataObject dobj = createDataObject();

        // supported
        fromStringToBoolean.convert(dobj, "true", true);
        fromStringToByte.convert(dobj, "3", (byte)3);
        fromStringToCharacter.convert(dobj, "3", '3');
        fromStringToCharacter.convert(dobj, "", (char)0);
        fromStringToDouble.convert(dobj, "3.14159", 3.14159);
        fromStringToFloat.convert(dobj, "3.14159", (float)3.14159);
        fromStringToInt.convert(dobj, "3", 3);
        fromStringToLong.convert(dobj, "3", 3l);
        fromStringToShort.convert(dobj, "3", (short)3);
        fromStringToBytes.convert(dobj, "0A64", new byte[] {(byte)10, (byte)100});
        fromStringToDecimal.convert(dobj, "3.14159", new BigDecimal("3.14159"));
        fromStringToInteger.convert(dobj, "3", BigInteger.valueOf(3l));
        String dateString = "2006-11-01T18:30:00.000Z";
        String dateString2 = "2006-11-01T18:00:00.000Z";
        String dateString3 = "2006-11-01T00:00:00.000Z";
        DateFormat f = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'.'SSS'Z'");
        f.setTimeZone(TimeZone.getTimeZone("GMT"));
        Date date = f.parse(dateString);
        Date date2 = f.parse(dateString2);
        Date date3 = f.parse(dateString3);
        fromStringToDate.convert(dobj, dateString, date);
        dateString = "2006-11-01T18:30:00.000123Z"; // valid datetime representation
        fromStringToDate.convert(dobj, dateString, date); // loss of precision
        dateString = "2006-11-01T18:30:00.0Z"; // valid datetime representation
        fromStringToDate.convert(dobj, dateString, date);
        dateString = "2006-11-01T18:30:00Z"; // valid datetime representation
        fromStringToDate.convert(dobj, dateString, date);
        dateString = "2006-11-01T18:30:00"; // valid datetime representation
        fromStringToDate.convert(dobj, dateString, date);
        dateString = "2006-11-01T16:30:00-02:00"; // valid datetime representation
        fromStringToDate.convert(dobj, dateString, date);

        dateString = "2006-11-01T16:30"; // right-truncated datetime representation
        //fromStringToDate.convert(dobj, dateString, date);
        fromStringToDate.failToConvert(dobj, dateString, ClassCastException.class);
        dateString = "2006-11-01T16"; // right-truncated datetime representation
        //fromStringToDate.convert(dobj, dateString, date2);
        fromStringToDate.failToConvert(dobj, dateString, ClassCastException.class);
        dateString = "2006-11-01"; // valid date (YearMonthDay) representation
        fromStringToDate.convert(dobj, dateString, date3);
        dateString = "2006-11-"; // right-overtruncated datetime representation
        fromStringToDate.failToConvert(dobj, dateString, ClassCastException.class);
        dateString = "2006-11"; // valid gYearMonth (YearMonth) representation
        fromStringToDate.convert(dobj, dateString, date3);

        fromStringToDate.convert(dobj, null, null);

        List strings1 = new ArrayList(); // ["This", "is", "a", "dog."]
        strings1.add("This");
        strings1.add("is");
        strings1.add("a");
        strings1.add("dog.");
        List strings2 = new ArrayList(); // [""]
        strings2.add("");
        List strings3 = new ArrayList(); // []
        List strings4 = new ArrayList();
        strings4.add("This");
        strings4.add("is");
        strings4.add("a");
        strings4.add("");
        strings4.add("dog.");
        fromStringToStrings.convert(dobj, "This is a dog.", strings1);
        fromStringToStrings.convert(dobj, "This is a\ndog.", strings1);
        //fromStringToStrings.convert(dobj, "This is a\n\tdog.", strings4); // incorrect, now fixed
        fromStringToStrings.convert(dobj, "This is a\n\tdog.", strings1);
        fromStringToStrings.convert(dobj, "This is a\r\ndog.", strings1);
        fromStringToStrings.convert(dobj, "\r\n    This is a\r\n\tdog. ", strings1);
        fromStringToStrings.convert(dobj, "", strings2);
        //fromStringToStrings.convert(dobj, null, strings3); // is this correct?
        fromStringToStrings.convert(dobj, null, null); // or this?
    }

    public void testBytesConversion() throws Exception
    {
        ConversionTest fromBytesToBoolean = new ConversionTest("Bytes", "Boolean");
        ConversionTest fromBytesToByte = new ConversionTest("Bytes", "Byte");
        ConversionTest fromBytesToCharacter = new ConversionTest("Bytes", "Character");
        ConversionTest fromBytesToDouble = new ConversionTest("Bytes", "Double");
        ConversionTest fromBytesToFloat = new ConversionTest("Bytes", "Float");
        ConversionTest fromBytesToInt = new ConversionTest("Bytes", "Int");
        ConversionTest fromBytesToLong = new ConversionTest("Bytes", "Long");
        ConversionTest fromBytesToShort = new ConversionTest("Bytes", "Short");
        ConversionTest fromBytesToString = new ConversionTest("Bytes", "String");
        ConversionTest fromBytesToDecimal = new ConversionTest("Bytes", "Decimal");
        ConversionTest fromBytesToInteger = new ConversionTest("Bytes", "Integer");
        ConversionTest fromBytesToDate = new ConversionTest("Bytes", "Date");

        DataObject dobj = createDataObject();

        // supported
        fromBytesToString.convert(dobj, new byte[] {(byte)10, (byte)100}, "0A64");
        byte[] V11 = { (byte)7, (byte)7, (byte)7, (byte)7, (byte)7, (byte)7, (byte)7, (byte)7 };
        byte[] V12 = { (byte)1, (byte)1, (byte)1, (byte)1, (byte)1, (byte)1, (byte)1, (byte)1 };
        BigInteger V21 = new BigInteger(V11);
        BigInteger V22 = new BigInteger(V12);
        fromBytesToInteger.convert(dobj, V11, V21);
        fromBytesToInteger.convert(dobj, V12, V22);

        // unsupported
        fromBytesToBoolean.failToConvert(dobj, V11, ClassCastException.class);
        fromBytesToByte.failToConvert(dobj, V11, ClassCastException.class);
        fromBytesToCharacter.failToConvert(dobj, V11, ClassCastException.class);
        fromBytesToDouble.failToConvert(dobj, V11, ClassCastException.class);
        fromBytesToFloat.failToConvert(dobj, V11, ClassCastException.class);
        fromBytesToInt.failToConvert(dobj, V11, ClassCastException.class);
        fromBytesToLong.failToConvert(dobj, V11, ClassCastException.class);
        fromBytesToShort.failToConvert(dobj, V11, ClassCastException.class);
        fromBytesToDecimal.failToConvert(dobj, V11, ClassCastException.class);
        fromBytesToDate.failToConvert(dobj, V11, ClassCastException.class);
    }

    public void testDecimalConversion() throws Exception
    {
        ConversionTest fromDecimalToBoolean = new ConversionTest("Decimal", "Boolean");
        ConversionTest fromDecimalToByte = new ConversionTest("Decimal", "Byte");
        ConversionTest fromDecimalToCharacter = new ConversionTest("Decimal", "Character");
        ConversionTest fromDecimalToDouble = new ConversionTest("Decimal", "Double");
        ConversionTest fromDecimalToFloat = new ConversionTest("Decimal", "Float");
        ConversionTest fromDecimalToInt = new ConversionTest("Decimal", "Int");
        ConversionTest fromDecimalToLong = new ConversionTest("Decimal", "Long");
        ConversionTest fromDecimalToShort = new ConversionTest("Decimal", "Short");
        ConversionTest fromDecimalToString = new ConversionTest("Decimal", "String");
        ConversionTest fromDecimalToBytes = new ConversionTest("Decimal", "Bytes");
        ConversionTest fromDecimalToInteger = new ConversionTest("Decimal", "Integer");
        ConversionTest fromDecimalToDate = new ConversionTest("Decimal", "Date");

        DataObject dobj = createDataObject();

        // supported
        BigDecimal pi = new BigDecimal("3.141592653589793");
        fromDecimalToByte.convert(dobj, pi, (byte)3);
        fromDecimalToDouble.convert(dobj, pi, 3.141592653589793);
        fromDecimalToFloat.convert(dobj, pi, (float)3.141592653589793);
        fromDecimalToInt.convert(dobj, pi, 3);
        fromDecimalToLong.convert(dobj, pi, 3l);
        fromDecimalToShort.convert(dobj, pi, (short)3);
        fromDecimalToString.convert(dobj, pi, "3.141592653589793");
        fromDecimalToInteger.convert(dobj, pi, pi.toBigInteger());

        // unsupported
        fromDecimalToBoolean.failToConvert(dobj, pi, ClassCastException.class);
        //fromDecimalToByte.failToConvert(dobj, pi, ClassCastException.class);
        fromDecimalToCharacter.failToConvert(dobj, pi, ClassCastException.class);
        //fromDecimalToShort.failToConvert(dobj, pi, ClassCastException.class);
        fromDecimalToBytes.failToConvert(dobj, pi, ClassCastException.class);
        fromDecimalToDate.failToConvert(dobj, pi, ClassCastException.class);
    }

    public void testIntegerConversion() throws Exception
    {
        ConversionTest fromIntegerToBoolean = new ConversionTest("Integer", "Boolean");
        ConversionTest fromIntegerToByte = new ConversionTest("Integer", "Byte");
        ConversionTest fromIntegerToCharacter = new ConversionTest("Integer", "Character");
        ConversionTest fromIntegerToDouble = new ConversionTest("Integer", "Double");
        ConversionTest fromIntegerToFloat = new ConversionTest("Integer", "Float");
        ConversionTest fromIntegerToInt = new ConversionTest("Integer", "Int");
        ConversionTest fromIntegerToLong = new ConversionTest("Integer", "Long");
        ConversionTest fromIntegerToShort = new ConversionTest("Integer", "Short");
        ConversionTest fromIntegerToString = new ConversionTest("Integer", "String");
        ConversionTest fromIntegerToBytes = new ConversionTest("Integer", "Bytes");
        ConversionTest fromIntegerToDecimal = new ConversionTest("Integer", "Decimal");
        ConversionTest fromIntegerToDate = new ConversionTest("Integer", "Date");

        DataObject dobj = createDataObject();

        // supported
        BigInteger V11 = new BigInteger("3141592653589793");
        fromIntegerToByte.convert(dobj, V11, V11.byteValue());
        fromIntegerToDouble.convert(dobj, V11, V11.doubleValue());
        fromIntegerToFloat.convert(dobj, V11, V11.floatValue());
        fromIntegerToInt.convert(dobj, V11, V11.intValue());
        fromIntegerToLong.convert(dobj, V11, V11.longValue());
        fromIntegerToShort.convert(dobj, V11, V11.shortValue());
        fromIntegerToString.convert(dobj, V11, "3141592653589793");
        fromIntegerToBytes.convert(dobj, V11, V11.toByteArray());
        fromIntegerToDecimal.convert(dobj, V11, new BigDecimal(V11));

        // unsupported
        fromIntegerToBoolean.failToConvert(dobj, V11, ClassCastException.class);
        //fromIntegerToByte.failToConvert(dobj, V11, ClassCastException.class);
        fromIntegerToCharacter.failToConvert(dobj, V11, ClassCastException.class);
        //fromIntegerToShort.failToConvert(dobj, V11, ClassCastException.class);
        fromIntegerToDate.failToConvert(dobj, V11, ClassCastException.class);
    }

    public void testDateConversion() throws Exception
    {
        ConversionTest fromDateToBoolean = new ConversionTest("Date", "Boolean");
        ConversionTest fromDateToByte = new ConversionTest("Date", "Byte");
        ConversionTest fromDateToCharacter = new ConversionTest("Date", "Character");
        ConversionTest fromDateToDouble = new ConversionTest("Date", "Double");
        ConversionTest fromDateToFloat = new ConversionTest("Date", "Float");
        ConversionTest fromDateToInt = new ConversionTest("Date", "Int");
        ConversionTest fromDateToLong = new ConversionTest("Date", "Long");
        ConversionTest fromDateToShort = new ConversionTest("Date", "Short");
        ConversionTest fromDateToString = new ConversionTest("Date", "String");
        ConversionTest fromDateToBytes = new ConversionTest("Date", "Bytes");
        ConversionTest fromDateToDecimal = new ConversionTest("Date", "Decimal");
        ConversionTest fromDateToInteger = new ConversionTest("Date", "Integer");

        DataObject dobj = createDataObject();

        // supported
        Date V11 = new Date(1164160702569l);
        fromDateToLong.convert(dobj, V11, 1164160702569l);
        fromDateToString.convert(dobj, V11, "2006-11-22T01:58:22.569Z");
        fromDateToString.convert(dobj, null, null);

        // unsupported
        fromDateToBoolean.failToConvert(dobj, V11, ClassCastException.class);
        fromDateToByte.failToConvert(dobj, V11, ClassCastException.class);
        fromDateToCharacter.failToConvert(dobj, V11, ClassCastException.class);
        fromDateToDouble.failToConvert(dobj, V11, ClassCastException.class);
        fromDateToFloat.failToConvert(dobj, V11, ClassCastException.class);
        fromDateToInt.failToConvert(dobj, V11, ClassCastException.class);
        fromDateToShort.failToConvert(dobj, V11, ClassCastException.class);
        fromDateToBytes.failToConvert(dobj, V11, ClassCastException.class);
        fromDateToDecimal.failToConvert(dobj, V11, ClassCastException.class);
        fromDateToInteger.failToConvert(dobj, V11, ClassCastException.class);
    }

    public void testStringsConversion() throws Exception
    {
        ConversionTest fromStringsToString = new ConversionTestA("Strings", "String");

        DataObject dobj = createDataObject();
        List strings1 = new ArrayList(); // ["This", "is", "a", "dog."]
        strings1.add("This");
        strings1.add("is");
        strings1.add("a");
        strings1.add("dog.");
        List strings2 = new ArrayList(); // [""] -> ""
        strings2.add("");
        List strings3 = new ArrayList(); // ["", ""] -> "" + " " + "" = " "
        strings3.add("");
        strings3.add("");
        List strings4 = new ArrayList(); // [] -> null
        List strings5 = new ArrayList();
        strings5.add(null);
        List strings6 = new ArrayList();
        strings6.add("a");
        strings6.add(null);
        strings6.add("b");
        strings6.add("");
        strings6.add("c");
        fromStringsToString.convert(dobj, strings1, "This is a dog.");
        fromStringsToString.convert(dobj, strings2, "");
        fromStringsToString.convert(dobj, strings3, " ");
        fromStringsToString.convert(dobj, strings4, null);
        fromStringsToString.convert(dobj, strings5, "");
        fromStringsToString.convert(dobj, strings6, "a  b  c");
    }

    private void checkDate(Date date, int[] fields, int[] values)
    {
        assertTrue(fields.length == values.length);
        Calendar cal = new GregorianCalendar();
        ((GregorianCalendar)cal).setGregorianChange(new Date(Long.MIN_VALUE));
        cal.clear(Calendar.ZONE_OFFSET);
        cal.setTimeZone(TimeZone.getTimeZone("GMT"));
        cal.setTime(date);
        for (int i = 0; i < fields.length; i++)
        {
            assertEquals(values[i], cal.get(fields[i]));
        }
    }

    private void checkDateForDuration(Date date, int[] values)
    {
        assertEquals(8, values.length);
        Calendar cal = new GregorianCalendar();
        ((GregorianCalendar)cal).setGregorianChange(new Date(Long.MIN_VALUE));
        cal.clear(Calendar.ZONE_OFFSET);
        cal.setTimeZone(TimeZone.getTimeZone("GMT"));
        cal.setTime(date);

        assertEquals(values[0], cal.get(Calendar.ERA));
        assertEquals(values[1], cal.get(Calendar.YEAR));
        assertEquals(values[2], cal.get(Calendar.MONTH));
        assertEquals(values[3], cal.get(Calendar.DAY_OF_MONTH));
        assertEquals(values[4], cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(values[5], cal.get(Calendar.MINUTE));
        assertEquals(values[6], cal.get(Calendar.SECOND));
        assertEquals(values[7], cal.get(Calendar.MILLISECOND));
    }

    public void testDayConversion() throws Exception
    {
        System.out.println("Day");
        ConversionTest fromDayToDate = new ConversionToDateTest("Day");
        ConversionTest fromDateToDay = new ConversionFromDateTest("Day");

        DataObject dobj = createDataObject();

        int[] fields = {Calendar.DAY_OF_MONTH};
        String s1 = "---14";
        Date d1 = toDate(s1);
        checkDate(d1, fields, new int[] {14});
        String s2 = "---14Z"; 
        Date d2 = toDate(s2);
        checkDate(d2, fields, new int[] {14});
        String s3 = "---04-02:00";
        Date d3 = toDate(s3);
        checkDate(d3, fields, new int[] {4});
        String s4 = "---31Z";
        Date d4 = toDate(s4);
        checkDate(d4, fields, new int[] {31});
        String s5 = "---04+02:00"; // in GMT, it is still the 3rd
        Date d5 = toDate(s5);
        checkDate(d5, fields, new int[] {3});

        fromDayToDate.convert(dobj, s1, d1);
        fromDateToDay.convert(dobj, d1, s1);
        fromDayToDate.convert(dobj, s2, d2);
        fromDateToDay.convert(dobj, d2, s1);
        fromDayToDate.convert(dobj, s3, d3);
        fromDateToDay.convert(dobj, d3, "---04");
        fromDayToDate.convert(dobj, s4, d4);
        fromDateToDay.convert(dobj, d4, "---31");
        fromDayToDate.convert(dobj, s5, d5);
        fromDateToDay.convert(dobj, d5, "---03");
    }

    public void testDateTimeConversion() throws Exception
    {
        System.out.println("DateTime");
        ConversionTest fromDateTimeToDate = new ConversionToDateTest("DateTime");
        ConversionTest fromDateToDateTime = new ConversionFromDateTest("DateTime");

        DataObject dobj = createDataObject();

        String s1 = "2001-12-31T23:59:59";
        Date d1 = toDate(s1);
        String s2 = "2001-01-01T00:00:00.1Z";
        Date d2 = toDate(s2);
        String s3 = "2001-12-31T18:30:45.12345+02:00";
        Date d3 = toDate(s3);

        fromDateTimeToDate.convert(dobj, s1, d1);
        fromDateToDateTime.convert(dobj, d1, s1+"Z");
        fromDateTimeToDate.convert(dobj, s2, d2);
        fromDateToDateTime.convert(dobj, d2, s2);
        fromDateTimeToDate.convert(dobj, s3, d3);
        fromDateToDateTime.convert(dobj, d3, "2001-12-31T16:30:45.123Z");
    }

    public void testDurationConversion() throws Exception
    {
        System.out.println("Duration");
        ConversionTest fromDurationToDate = new ConversionToDateTest("Duration");
        ConversionTest fromDateToDuration = new ConversionFromDateTest("Duration");

        DataObject dobj = createDataObject();

        String s1 = "P1Y3M15D";
        Date d1 = toDateFromDuration(s1);
        checkDateForDuration(d1, new int[] {1, 1, 3, 1+15, 0, 0, 0, 0});
        String s2 = "-P1Y3M15D";
        Date d2 = toDateFromDuration(s2);
        checkDateForDuration(d2, new int[] {0, 1+2, 12-4, 31-15, 0, 0, 0, 0});
        String s3 = "P15M15DT13H";
        Date d3 = toDateFromDuration(s3);
        checkDateForDuration(d3, new int[] {1, 1, 3, 1+15, 13, 0, 0, 0});
        String s3b = "P1Y3M15DT13H";
        String s4 = "-P15M15DT13H";
        Date d4 = toDateFromDuration(s4);
        checkDateForDuration(d4, new int[] {0, 1+2, 12-4, 31-16, 24-13, 0, 0, 0});
        String s4b = "-P1Y3M15DT13H";
        String s5 = "-PT11H59M59.999S";
        Date d5 = toDateFromDuration(s5);
        checkDateForDuration(d5, new int[] {0, 2, 11, 31, 12, 0, 0, 1});
        String s6 = "PT0S";
        Date d6 = toDateFromDuration(s6);
        checkDateForDuration(d6, new int[] {0, 1, 0, 1, 0, 0, 0, 0});

        fromDurationToDate.convert(dobj, s1, d1);
        fromDateToDuration.convert(dobj, d1, s1);
        fromDurationToDate.convert(dobj, s2, d2);
        fromDateToDuration.convert(dobj, d2, s2);
        fromDurationToDate.convert(dobj, s3, d3);
        fromDateToDuration.convert(dobj, d3, s3b);
        fromDurationToDate.convert(dobj, s4, d4);
        fromDateToDuration.convert(dobj, d4, s4b);
    }

    public void testMonthConversion() throws Exception
    {
        System.out.println("Month");
        ConversionTest fromMonthToDate = new ConversionToDateTest("Month");
        ConversionTest fromDateToMonth = new ConversionFromDateTest("Month");

        DataObject dobj = createDataObject();

        int[] fields = {Calendar.MONTH};
        int[] values = {Calendar.MARCH};
        String s1 = "--03";
        Date d1 = toDate(s1);
        checkDate(d1, fields, values);
        String s2 = "--03Z";
        Date d2 = toDate(s2);
        checkDate(d2, fields, values);
        String s3 = "--03-02:00";
        Date d3 = toDate(s3);
        checkDate(d3, fields, values);

        fromMonthToDate.convert(dobj, s1, d1);
        fromDateToMonth.convert(dobj, d1, s1);
        fromMonthToDate.convert(dobj, s2, d2);
        fromDateToMonth.convert(dobj, d2, s1);
        fromMonthToDate.convert(dobj, s3, d3);
        fromDateToMonth.convert(dobj, d3, s1);
    }

    public void testMonthDayConversion() throws Exception
    {
        System.out.println("MonthDay");
        ConversionTest fromMonthDayToDate = new ConversionToDateTest("MonthDay");
        ConversionTest fromDateToMonthDay = new ConversionFromDateTest("MonthDay");

        DataObject dobj = createDataObject();

        int[] fields = {Calendar.MONTH, Calendar.DAY_OF_MONTH};
        String s1 = "--07-14";
        Date d1 = toDate(s1);
        checkDate(d1, fields, new int[] {Calendar.JULY, 14});
        String s2 = "--07-14Z";
        Date d2 = toDate(s2);
        checkDate(d2, fields, new int[] {Calendar.JULY, 14});
        String s3 = "--07-14-02:00";
        Date d3 = toDate(s3);
        checkDate(d3, fields, new int[] {Calendar.JULY, 14});
        String s4 = "--02-29Z";
        Date d4 = toDate(s4);
        checkDate(d4, fields, new int[] {Calendar.FEBRUARY, 29});

        fromMonthDayToDate.convert(dobj, s1, d1);
        fromDateToMonthDay.convert(dobj, d1, s1);
        fromMonthDayToDate.convert(dobj, s2, d2);
        fromDateToMonthDay.convert(dobj, d2, s1);
        fromMonthDayToDate.convert(dobj, s3, d3);
        fromDateToMonthDay.convert(dobj, d3, s1);
        fromMonthDayToDate.convert(dobj, s4, d4);
        fromDateToMonthDay.convert(dobj, d4, "--02-29");
    }

    public void testTimeConversion() throws Exception
    {
        System.out.println("Time");
        ConversionTest fromTimeToDate = new ConversionToDateTest("Time");
        ConversionTest fromDateToTime = new ConversionFromDateTest("Time");

        DataObject dobj = createDataObject();

        int[] fields = {Calendar.HOUR_OF_DAY, Calendar.MINUTE, Calendar.SECOND, Calendar.MILLISECOND};
        String s1 = "23:59:59";
        Date d1 = toDate(s1);
        checkDate(d1, fields, new int[] {23, 59, 59, 0});
        String s2 = "00:00:00.1Z";
        Date d2 = toDate(s2);
        checkDate(d2, fields, new int[] {0, 0, 0, 100});
        String s3 = "18:30:45.12345+02:00";
        Date d3 = toDate(s3);
        checkDate(d3, fields, new int[] {16, 30, 45, 123});

        fromTimeToDate.convert(dobj, s1, d1);
        fromDateToTime.convert(dobj, d1, s1+"Z");
        fromTimeToDate.convert(dobj, s2, d2);
        fromDateToTime.convert(dobj, d2, s2);
        fromTimeToDate.convert(dobj, s3, d3);
        fromDateToTime.convert(dobj, d3, "16:30:45.123Z");
    }

    public void testYearConversion() throws Exception
    {
        System.out.println("Year");
        ConversionTest fromYearToDate = new ConversionToDateTest("Year");
        ConversionTest fromDateToYear = new ConversionFromDateTest("Year");

        DataObject dobj = createDataObject();

        int[] fields = {Calendar.YEAR};
        String s1 = "2001";
        Date d1 = toDate(s1);
        checkDate(d1, fields, new int[] {2001});
        String s2 = "2001Z";
        Date d2 = toDate(s2);
        checkDate(d2, fields, new int[] {2001});
        String s3 = "1999-02:00";
        Date d3 = toDate(s3);
        checkDate(d3, fields, new int[] {1999});

        fromYearToDate.convert(dobj, s1, d1);
        fromDateToYear.convert(dobj, d1, s1);
        fromYearToDate.convert(dobj, s2, d2);
        fromDateToYear.convert(dobj, d2, s1);
        fromYearToDate.convert(dobj, s3, d3);
        fromDateToYear.convert(dobj, d3, "1999");
    }

    public void testYearMonthConversion() throws Exception
    {
        System.out.println("YearMonth");
        ConversionTest fromYearMonthToDate = new ConversionToDateTest("YearMonth");
        ConversionTest fromDateToYearMonth = new ConversionFromDateTest("YearMonth");

        DataObject dobj = createDataObject();

        int[] fields = {Calendar.YEAR, Calendar.MONTH};
        int[] values = {2001, Calendar.JULY};
        String s1 = "2001-07";
        Date d1 = toDate(s1);
        checkDate(d1, fields, values);
        String s2 = "2001-07Z";
        Date d2 = toDate(s2);
        checkDate(d2, fields, values);
        String s3 = "2001-07-02:00";
        Date d3 = toDate(s3);
        checkDate(d3, fields, values);

        fromYearMonthToDate.convert(dobj, s1, d1);
        fromDateToYearMonth.convert(dobj, d1, s1);
        fromYearMonthToDate.convert(dobj, s2, d2);
        fromDateToYearMonth.convert(dobj, d2, s1);
        fromYearMonthToDate.convert(dobj, s3, d3);
        fromDateToYearMonth.convert(dobj, d3, s1);
    }

    public void testYearMonthDayConversion() throws Exception
    {
        System.out.println("YearMonthDay");
        ConversionTest fromYearMonthDayToDate = new ConversionToDateTest("YearMonthDay");
        ConversionTest fromDateToYearMonthDay = new ConversionFromDateTest("YearMonthDay");

        DataObject dobj = createDataObject();

        int[] fields = {Calendar.YEAR, Calendar.MONTH, Calendar.DAY_OF_MONTH};
        int[] values = {2001, Calendar.JULY, 14};
        String s1 = "2001-07-14";
        Date d1 = toDate(s1);
        checkDate(d1, fields, values);
        String s2 = "2001-07-14Z";
        Date d2 = toDate(s2);
        checkDate(d2, fields, values);
        String s3 = "2001-07-14-02:00";
        Date d3 = toDate(s3);
        checkDate(d3, fields, values);
        //String s4 = "-0001-01-01Z";
        //Date d4 = toDate(s4);
        //fields = new int[] {Calendar.ERA, Calendar.YEAR, Calendar.MONTH, Calendar.DAY_OF_MONTH};
        //values = new int[] {0, 1, Calendar.JANUARY, 1};
        //checkDate(d4, fields, values); // FAILS!
        String s4 = "2001-07-15+02:00";
        Date d4 = toDate(s4);
        checkDate(d4, fields, values);
        long l0 = d1.getTime();
        long l1 = (23 * 60 * 60 * 1000)
            + (59 * 60 * 1000)
            + (59 * 1000)
            + 999;
        long l = l0 + l1;
        Date d5 = new Date(l);
        checkDate(d5, fields, values);

        fromYearMonthDayToDate.convert(dobj, s1, d1);
        fromDateToYearMonthDay.convert(dobj, d1, s1);
        fromYearMonthDayToDate.convert(dobj, s2, d2);
        fromDateToYearMonthDay.convert(dobj, d2, s1);
        fromYearMonthDayToDate.convert(dobj, s3, d3);
        fromDateToYearMonthDay.convert(dobj, d3, s1);
        //fromYearMonthDayToDate.convert(dobj, s4, d4);
        //fromDateToYearMonthDay.convert(dobj, d4, s4);
        fromYearMonthDayToDate.convert(dobj, s4, d4);
        fromDateToYearMonthDay.convert(dobj, d4, s1);
        fromDateToYearMonthDay.convert(dobj, d5, s1);
    }

    public void testCalendar()
    {
        
        Calendar cal = new XmlCalendar(); // at this point, fields are unset
        System.out.println("cal:");
        dumpCalendar(cal);
        cal.clear(); // redundant
        if (cal.getTimeZone() != null) // true, timezone is default timezone
        {
            cal.setTimeZone(TimeZone.getTimeZone("GMT"));
        }
        //cal.set(Calendar.YEAR, 0);
        System.out.println("cal again:");
        dumpCalendar(cal); // fields are still unset
        System.out.println("era: " + cal.get(Calendar.ERA));
        System.out.println("year: " + cal.get(Calendar.YEAR));
        System.out.println("month: " + cal.get(Calendar.MONTH));
        System.out.println("day: " + cal.get(Calendar.DAY_OF_MONTH));
        System.out.println("hour: " + cal.get(Calendar.HOUR_OF_DAY));
        System.out.println("minute: " + cal.get(Calendar.MINUTE));
        System.out.println("second: " + cal.get(Calendar.SECOND));
        System.out.println("millisecond: " + cal.get(Calendar.MILLISECOND));
        System.out.println("zone offset: " + cal.get(Calendar.ZONE_OFFSET));
        /*
        Calendar cal = new GregorianCalendar(0, 0, 1, 0, 0, 0);
        cal.clear(Calendar.ZONE_OFFSET);
        cal.setTimeZone(TimeZone.getTimeZone("GMT"));
        dumpCalendar(cal);
        cal.set(Calendar.YEAR, 0);
        System.out.println("era: " + cal.get(Calendar.ERA));
        System.out.println("year: " + cal.get(Calendar.YEAR));
        System.out.println("month: " + cal.get(Calendar.MONTH));
        System.out.println("day: " + cal.get(Calendar.DAY_OF_MONTH));
        System.out.println("hour: " + cal.get(Calendar.HOUR_OF_DAY));
        System.out.println("minute: " + cal.get(Calendar.MINUTE));
        System.out.println("second: " + cal.get(Calendar.SECOND));
        System.out.println("millisecond: " + cal.get(Calendar.MILLISECOND));
        System.out.println("zone offset: " + cal.get(Calendar.ZONE_OFFSET));
        */
        Date d0 = cal.getTime();
        DateFormat f = new SimpleDateFormat("G yyyy-MM-dd HH:mm:ss'.'SSS z");
        f.setTimeZone(cal.getTimeZone());
        System.out.println(f.format(d0));
    }
}
