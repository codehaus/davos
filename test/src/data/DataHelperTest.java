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

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.SimpleTimeZone;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import javax.sdo.helper.DataHelper;

//import org.apache.xmlbeans.GDate;
import junit.framework.*;
import common.BaseTest;

/**
 * Tests for DataHelper.
 * @author Wing Yew Poon
 */
public class DataHelperTest extends BaseTest
{
    public DataHelperTest(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        
        TestSuite suite = new TestSuite();
        suite.addTest(new DataHelperTest("testStringToDate"));
        suite.addTest(new DataHelperTest("testStringToCalendar"));
        suite.addTest(new DataHelperTest("testStringToCalendarWithLocale"));
        suite.addTest(new DataHelperTest("testCalendarToDateTime"));
        suite.addTest(new DataHelperTest("testCalendarToDuration"));
        suite.addTest(new DataHelperTest("testCalendarToTime"));
        suite.addTest(new DataHelperTest("testCalendarToDay"));
        suite.addTest(new DataHelperTest("testCalendarToMonth"));
        suite.addTest(new DataHelperTest("testCalendarToMonthDay"));
        suite.addTest(new DataHelperTest("testCalendarToYear"));
        suite.addTest(new DataHelperTest("testCalendarToYearMonth"));
        suite.addTest(new DataHelperTest("testCalendarToYearMonthDay"));
        suite.addTest(new DataHelperTest("testDateToDateTime"));
        suite.addTest(new DataHelperTest("testDateToDuration"));
        suite.addTest(new DataHelperTest("testDateToTime"));
        suite.addTest(new DataHelperTest("testDateToDay"));
        suite.addTest(new DataHelperTest("testDateToMonth"));
        suite.addTest(new DataHelperTest("testDateToMonthDay"));
        suite.addTest(new DataHelperTest("testDateToYear"));
        suite.addTest(new DataHelperTest("testDateToYearMonth"));
        suite.addTest(new DataHelperTest("testDateToYearMonthDay"));
        
        // or
        //TestSuite suite = new TestSuite(DataHelperTest.class);
        return suite;
    }

    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(suite());
    }

    private static DataHelper dataHelper = context.getDataHelper();
    private static final TimeZone tz0 = TimeZone.getTimeZone("GMT");
    private static TimeZone tz1 = 
        new SimpleTimeZone(-28800000,
                           "America/Los_Angeles",
                           Calendar.APRIL, 1, -Calendar.SUNDAY,
                           7200000,
                           Calendar.OCTOBER, -1, Calendar.SUNDAY,
                           7200000,
                           3600000);
    private static TimeZone tz2 = TimeZone.getTimeZone("GMT-08:00");
    private static TimeZone tz3 = TimeZone.getTimeZone("GMT+08:00");

    private static Date toDate(String s)
    {
        /*
        GDate gDate = new GDate(s);
        Calendar cal = gDate.getCalendar();
        if (!cal.isSet(Calendar.ZONE_OFFSET))
            cal.setTimeZone(TimeZone.getTimeZone("GMT"));
        Date dateFromCalendar = cal.getTime();
        return dateFromCalendar;
        */
        return DataTypeConversionTest.toDate(s);
    }

    private static String formatDate(String format, Date date)
    {
        DateFormat f = new SimpleDateFormat(format);
        f.setTimeZone(TimeZone.getTimeZone("GMT"));
        String s = f.format(date);
        return s;
    }

    private void failToDate(String s, Class c)
    {
        try
        {
            dataHelper.toDate(s);
            fail("DataHelper.toDate() should have thrown " + c.getName());
        }
        catch (Exception e)
        {
            if (!e.getClass().equals(c))
                fail("DataHelper.toDate() should have thrown " + c.getName() + 
                     " instead of " + e.getClass().getName());
        }
    }

    private void failToCalendar(String s, Class c)
    {
        try
        {
            dataHelper.toCalendar(s);
            fail("DataHelper.toCalendar() should have thrown " + c.getName());
        }
        catch (Exception e)
        {
            if (!e.getClass().equals(c))
                fail("DataHelper.toCalendar() should have thrown " + 
                     c.getName() + " instead of " + e.getClass().getName());
        }
    }

    private void checkCalendar(Calendar cal, int[] values)
    {
        assertEquals(9, values.length);
        assertEquals(values[0], cal.get(Calendar.ERA));
        assertEquals(values[1], cal.get(Calendar.YEAR));
        assertEquals(values[2], cal.get(Calendar.MONTH));
        assertEquals(values[3], cal.get(Calendar.DAY_OF_MONTH));
        assertEquals(values[4], cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(values[5], cal.get(Calendar.MINUTE));
        assertEquals(values[6], cal.get(Calendar.SECOND));
        assertEquals(values[7], cal.get(Calendar.MILLISECOND));
        assertEquals(values[8], cal.get(Calendar.ZONE_OFFSET));
    }

    private void checkCalendar(Calendar cal, int[] fields, int[] values)
    {
        assertTrue(fields.length == values.length);
        for (int i = 0; i < fields.length; i++)
        {
            assertEquals(values[i], cal.get(fields[i]));
        }
    }

    // NOTE: a Date does not have a timezone, so we normalize to GMT.
    // Following normalization, there is no point to checking the 
    // Calendar.ZONE_OFFSET field.
    private void checkCalendar(Date date, int[] fields, int[] values)
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

    public void testStringToDate()
    {
        System.out.println("string to date");
        Date exp = new Date();
        String s = formatDate("yyyy-MM-dd'T'HH:mm:ss'.'SSS'Z'", exp);
        System.out.println(s);
        Date act = dataHelper.toDate(s);
        System.out.println(act);
        assertEquals(exp, act);
        s = formatDate("yyyy-MM-dd'T'HH:mm:ss'.'SSS", exp);
        System.out.println(s);
        act = dataHelper.toDate(s);
        System.out.println(act);
        assertEquals(exp, act);

        // Test DataHelper.toDate(String) with strings representing
        // Duration, Time, Day, Month, MonthDay, Year, YearMonth, YearMonthDay.
        // Testing DataHelper.toDate(String) has to be a bit different from 
        // testing toCalendar(String), because once the String is converted to 
        // a Date, any timezone information is lost.

        // Time
        s = "13:45:00.678Z";
        Calendar cal = dataHelper.toCalendar(s);
        act = dataHelper.toDate(s);
        assertEquals(cal.getTime(), act);
        checkCalendar(act, new int[] {Calendar.HOUR_OF_DAY,
                                      Calendar.MINUTE,
                                      Calendar.SECOND,
                                      Calendar.MILLISECOND},
                      new int[] {13, 45, 0, 678});
        s = "13:45:00.678+01:00";
        cal = dataHelper.toCalendar(s);
        act = dataHelper.toDate(s);
        assertEquals(cal.getTime(), act);
        checkCalendar(act, new int[] {Calendar.HOUR_OF_DAY,
                                      Calendar.MINUTE,
                                      Calendar.SECOND,
                                      Calendar.MILLISECOND},
                      new int[] {12, 45, 0, 678}); // convert to GMT

        // Day
        s = "---15";
        cal = dataHelper.toCalendar(s);
        act = dataHelper.toDate(s); 
        //assertEquals(cal.getTime(), act); // not equal unless default timezone is GMT since cal uses default timezone in getting the Date while act is in GMT.
        checkCalendar(act, new int[] {Calendar.DAY_OF_MONTH},
                      new int[] {15});
        s = "---15Z";
        cal = dataHelper.toCalendar(s);
        act = dataHelper.toDate(s);
        assertEquals(cal.getTime(), act);
        checkCalendar(act, new int[] {Calendar.DAY_OF_MONTH},
                      new int[] {15});
        s = "---15+02:00";
        cal = dataHelper.toCalendar(s);
        act = dataHelper.toDate(s);
        assertEquals(cal.getTime(), act);
        checkCalendar(act, new int[] {Calendar.DAY_OF_MONTH},
                      new int[] {14}); // at midnight, it's previous day in GMT
        s = "---15-02:00";
        cal = dataHelper.toCalendar(s);
        act = dataHelper.toDate(s);
        assertEquals(cal.getTime(), act);
        checkCalendar(act, new int[] {Calendar.DAY_OF_MONTH},
                      new int[] {15}); // at midnight, it's same day in GMT

        // Month
        s = "--12";
        cal = dataHelper.toCalendar(s);
        act = dataHelper.toDate(s);
        checkCalendar(act, new int[] {Calendar.MONTH},
                      new int[] {11});
        s = "--12Z";
        cal = dataHelper.toCalendar(s);
        act = dataHelper.toDate(s);
        assertEquals(cal.getTime(), act);
        checkCalendar(act, new int[] {Calendar.MONTH},
                      new int[] {11});
        s = "--12+02:00";
        cal = dataHelper.toCalendar(s);
        act = dataHelper.toDate(s);
        assertEquals(cal.getTime(), act);
        checkCalendar(act, new int[] {Calendar.MONTH},
                      new int[] {10});
        s = "--12-02:00";
        cal = dataHelper.toCalendar(s);
        act = dataHelper.toDate(s);
        assertEquals(cal.getTime(), act);
        checkCalendar(act, new int[] {Calendar.MONTH},
                      new int[] {11});

        // MonthDay
        s = "--7-15";
        failToDate(s, IllegalArgumentException.class);

        s = "--07-15";
        cal = dataHelper.toCalendar(s);
        act = dataHelper.toDate(s);
        checkCalendar(act, new int[] {Calendar.MONTH, Calendar.DAY_OF_MONTH},
                      new int[] {6, 15});
        s = "--07-15Z";
        cal = dataHelper.toCalendar(s);
        act = dataHelper.toDate(s);
        assertEquals(cal.getTime(), act);
        checkCalendar(act, new int[] {Calendar.MONTH,
                                      Calendar.DAY_OF_MONTH},
                      new int[] {6, 15});
        s = "--07-15+02:00";
        cal = dataHelper.toCalendar(s);
        act = dataHelper.toDate(s);
        assertEquals(cal.getTime(), act);
        checkCalendar(act, new int[] {Calendar.MONTH,
                                      Calendar.DAY_OF_MONTH},
                      new int[] {6, 14});
        s = "--07-15-02:00";
        cal = dataHelper.toCalendar(s);
        act = dataHelper.toDate(s);
        assertEquals(cal.getTime(), act);
        checkCalendar(act, new int[] {Calendar.MONTH,
                                      Calendar.DAY_OF_MONTH},
                      new int[] {6, 15});

        // Year
        s = "-12";
        failToDate(s, IllegalArgumentException.class);

        s = "2012";
        cal = dataHelper.toCalendar(s);
        act = dataHelper.toDate(s);
        checkCalendar(act, new int[] {Calendar.ERA, Calendar.YEAR},
                      new int[] {1, 2012}); // AD 2012
        s = "-0012";
        cal = dataHelper.toCalendar(s);
        act = dataHelper.toDate(s);
        checkCalendar(act, new int[] {Calendar.ERA, Calendar.YEAR},
                      new int[] {0, 12}); // 12 BC
        s = "2012Z";
        cal = dataHelper.toCalendar(s);
        act = dataHelper.toDate(s);
        assertEquals(cal.getTime(), act);
        checkCalendar(act, new int[] {Calendar.YEAR},
                      new int[] {2012});
        s = "2012-02:00";
        cal = dataHelper.toCalendar(s);
        act = dataHelper.toDate(s);
        assertEquals(cal.getTime(), act);
        checkCalendar(act, new int[] {Calendar.YEAR},
                      new int[] {2012});

        // YearMonth
        s= "2006-1";
        failToDate(s, IllegalArgumentException.class);

        s = "2006-12";
        cal = dataHelper.toCalendar(s);
        act = dataHelper.toDate(s);
        checkCalendar(act, new int[] {Calendar.YEAR,
                                      Calendar.MONTH},
                      new int[] {2006, 11});
        s = "2006-12Z";
        cal = dataHelper.toCalendar(s);
        act = dataHelper.toDate(s);
        assertEquals(cal.getTime(), act);
        checkCalendar(act, new int[] {Calendar.YEAR,
                                      Calendar.MONTH},
                      new int[] {2006, 11});
        s = "2006-12+02:00";
        cal = dataHelper.toCalendar(s);
        act = dataHelper.toDate(s);
        assertEquals(cal.getTime(), act);
        checkCalendar(act, new int[] {Calendar.YEAR,
                                      Calendar.MONTH},
                      new int[] {2006, 10});
        s = "2006-12-02:00";
        cal = dataHelper.toCalendar(s);
        act = dataHelper.toDate(s);
        assertEquals(cal.getTime(), act);
        checkCalendar(act, new int[] {Calendar.YEAR,
                                      Calendar.MONTH},
                      new int[] {2006, 11});

        // YearMonthDay
        s = "2006-12-15";
        cal = dataHelper.toCalendar(s);
        act = dataHelper.toDate(s);
        checkCalendar(act, new int[] {Calendar.YEAR,
                                      Calendar.MONTH,
                                      Calendar.DAY_OF_MONTH},
                      new int[] {2006, 11, 15});
        s = "2006-12-15Z";
        cal = dataHelper.toCalendar(s);
        act = dataHelper.toDate(s);
        assertEquals(cal.getTime(), act);
        checkCalendar(act, new int[] {Calendar.YEAR,
                                      Calendar.MONTH,
                                      Calendar.DAY_OF_MONTH},
                      new int[] {2006, 11, 15});
        s = "2006-12-15+02:00";
        cal = dataHelper.toCalendar(s);
        act = dataHelper.toDate(s);
        assertEquals(cal.getTime(), act);
        checkCalendar(act, new int[] {Calendar.YEAR,
                                      Calendar.MONTH,
                                      Calendar.DAY_OF_MONTH},
                      new int[] {2006, 11, 14});
        s = "2006-12-15-02:00";
        cal = dataHelper.toCalendar(s);
        act = dataHelper.toDate(s);
        assertEquals(cal.getTime(), act);
        checkCalendar(act, new int[] {Calendar.YEAR,
                                      Calendar.MONTH,
                                      Calendar.DAY_OF_MONTH},
                      new int[] {2006, 11, 15});

        // Duration
        int[] fields = {Calendar.ERA,
                        Calendar.YEAR,
                        Calendar.MONTH,
                        Calendar.DAY_OF_MONTH,
                        Calendar.HOUR_OF_DAY,
                        Calendar.MINUTE,
                        Calendar.SECOND,
                        Calendar.MILLISECOND};
        String s1 = "P1Y3M15D";
        Date d1 = dataHelper.toDate(s1);
        checkCalendar(d1, fields, new int[] {1, 1, 3, 1+15, 0, 0, 0, 0});
        String s2 = "-P1Y3M15D";
        Date d2 = dataHelper.toDate(s2);
        checkCalendar(d2, fields, new int[] {0, 1+2, 12-4, 31-15, 0, 0, 0, 0});
        String s3 = "P15M15DT13H";
        Date d3 = dataHelper.toDate(s3);
        checkCalendar(d3, fields, new int[] {1, 1, 3, 1+15, 13, 0, 0, 0});
        String s4 = "-P15M15DT13H";
        Date d4 = dataHelper.toDate(s4);
        checkCalendar(d4, fields, new int[] {0, 1+2, 12-4, 31-16, 24-13, 0, 0, 0});
        String s5 = "-PT11H59M59.999S";
        Date d5 = dataHelper.toDate(s5);
        checkCalendar(d5, fields, new int[] {0, 2, 11, 31, 12, 0, 0, 1});
        String s6 = "PT0S";
        Date d6 = dataHelper.toDate(s6);
        checkCalendar(d6, fields, new int[] {0, 1, 0, 1, 0, 0, 0, 0});

   }

    public void testStringToCalendar()
    {
        System.out.println("string to calendar");
        String s = "2006-12-15T13:45:00.678Z";
        Date date = toDate(s);
        Calendar cal = dataHelper.toCalendar(s);
        System.out.println(cal.getTime());
        assertEquals(date, cal.getTime());
        System.out.println(cal.getTimeZone());
        assertEquals(TimeZone.getTimeZone("GMT"), cal.getTimeZone());
        checkCalendar(cal, new int[] {1, 2006, 11, 15, 13, 45, 0, 678, 0});

        // Test with DataHelper.toCalendar(String) strings representing
        // Duration, Time, Day, Month, MonthDay, Year, YearMonth, YearMonthDay.

        // Time
        s = "13:45:00.678Z";
        cal = dataHelper.toCalendar(s);
        checkCalendar(cal, new int[] {Calendar.HOUR_OF_DAY,
                                      Calendar.MINUTE,
                                      Calendar.SECOND,
                                      Calendar.MILLISECOND,
                                      Calendar.ZONE_OFFSET},
                      new int[] {13, 45, 0, 678, 0});
        s = "13:45:00.678+01:00";
        cal = dataHelper.toCalendar(s);
        checkCalendar(cal, new int[] {Calendar.HOUR_OF_DAY,
                                      Calendar.MINUTE,
                                      Calendar.SECOND,
                                      Calendar.MILLISECOND,
                                      Calendar.ZONE_OFFSET},
                      new int[] {13, 45, 0, 678, 3600000});

        // Day
        s = "---15";
        cal = dataHelper.toCalendar(s);
        checkCalendar(cal, new int[] {Calendar.DAY_OF_MONTH},
                      new int[] {15});
        s = "---15Z";
        cal = dataHelper.toCalendar(s);
        checkCalendar(cal, new int[] {Calendar.DAY_OF_MONTH, 
                                      Calendar.ZONE_OFFSET},
                      new int[] {15, 0});
        s = "---15+02:00";
        cal = dataHelper.toCalendar(s);
        checkCalendar(cal, new int[] {Calendar.DAY_OF_MONTH, 
                                      Calendar.ZONE_OFFSET},
                      new int[] {15, 7200000});
        s = "---15-02:00";
        cal = dataHelper.toCalendar(s);
        checkCalendar(cal, new int[] {Calendar.DAY_OF_MONTH, 
                                      Calendar.ZONE_OFFSET},
                      new int[] {15, -7200000});

        // Month
        s = "--12";
        cal = dataHelper.toCalendar(s);
        checkCalendar(cal, new int[] {Calendar.MONTH},
                      new int[] {11});
        s = "--12Z";
        cal = dataHelper.toCalendar(s);
        checkCalendar(cal, new int[] {Calendar.MONTH, 
                                      Calendar.ZONE_OFFSET},
                      new int[] {11, 0});
        s = "--12+02:00";
        cal = dataHelper.toCalendar(s);
        checkCalendar(cal, new int[] {Calendar.MONTH, 
                                      Calendar.ZONE_OFFSET},
                      new int[] {11, 7200000});
        s = "--12-02:00";
        cal = dataHelper.toCalendar(s);
        checkCalendar(cal, new int[] {Calendar.MONTH, 
                                      Calendar.ZONE_OFFSET},
                      new int[] {11, -7200000});

        // MonthDay
        s = "--7-15";
        failToCalendar(s, IllegalArgumentException.class);

        s = "--07-15";
        cal = dataHelper.toCalendar(s);
        checkCalendar(cal, new int[] {Calendar.MONTH, Calendar.DAY_OF_MONTH},
                      new int[] {6, 15});
        s = "--07-15Z";
        cal = dataHelper.toCalendar(s);
        checkCalendar(cal, new int[] {Calendar.MONTH,
                                      Calendar.DAY_OF_MONTH, 
                                      Calendar.ZONE_OFFSET},
                      new int[] {6, 15, 0});
        s = "--07-15+02:00";
        cal = dataHelper.toCalendar(s);
        checkCalendar(cal, new int[] {Calendar.MONTH,
                                      Calendar.DAY_OF_MONTH, 
                                      Calendar.ZONE_OFFSET},
                      new int[] {6, 15, 7200000});
        s = "--07-15-02:00";
        cal = dataHelper.toCalendar(s);
        checkCalendar(cal, new int[] {Calendar.MONTH,
                                      Calendar.DAY_OF_MONTH, 
                                      Calendar.ZONE_OFFSET},
                      new int[] {6, 15, -7200000});

        // Year
        s = "-12";
        failToCalendar(s, IllegalArgumentException.class);

        s = "2012";
        cal = dataHelper.toCalendar(s);
        checkCalendar(cal, new int[] {Calendar.ERA, Calendar.YEAR},
                      new int[] {1, 2012});
        s = "-0012";
        cal = dataHelper.toCalendar(s);
        checkCalendar(cal, new int[] {Calendar.ERA, Calendar.YEAR},
                      new int[] {0, 12});
        s = "2012Z";
        cal = dataHelper.toCalendar(s);
        checkCalendar(cal, new int[] {Calendar.YEAR, 
                                      Calendar.ZONE_OFFSET},
                      new int[] {2012, 0});
        s = "2012-02:00";
        cal = dataHelper.toCalendar(s);
        checkCalendar(cal, new int[] {Calendar.YEAR, 
                                      Calendar.ZONE_OFFSET},
                      new int[] {2012, -7200000});

        // YearMonth
        s= "2006-1";
        failToCalendar(s, IllegalArgumentException.class);

        s = "2006-12";
        cal = dataHelper.toCalendar(s);
        checkCalendar(cal, new int[] {Calendar.YEAR,
                                      Calendar.MONTH},
                      new int[] {2006, 11});
        s = "2006-12Z";
        cal = dataHelper.toCalendar(s);
        checkCalendar(cal, new int[] {Calendar.YEAR,
                                      Calendar.MONTH,
                                      Calendar.ZONE_OFFSET},
                      new int[] {2006, 11, 0});
        s = "2006-12+02:00";
        cal = dataHelper.toCalendar(s);
        checkCalendar(cal, new int[] {Calendar.YEAR,
                                      Calendar.MONTH,
                                      Calendar.ZONE_OFFSET},
                      new int[] {2006, 11, 7200000});
        s = "2006-12-02:00";
        cal = dataHelper.toCalendar(s);
        checkCalendar(cal, new int[] {Calendar.YEAR,
                                      Calendar.MONTH,
                                      Calendar.ZONE_OFFSET},
                      new int[] {2006, 11, -7200000});

        // YearMonthDay
        s = "2006-12-15";
        cal = dataHelper.toCalendar(s);
        checkCalendar(cal, new int[] {Calendar.YEAR,
                                      Calendar.MONTH,
                                      Calendar.DAY_OF_MONTH},
                      new int[] {2006, 11, 15});
        s = "2006-12-15Z";
        cal = dataHelper.toCalendar(s);
        checkCalendar(cal, new int[] {Calendar.YEAR,
                                      Calendar.MONTH,
                                      Calendar.DAY_OF_MONTH,
                                      Calendar.ZONE_OFFSET},
                      new int[] {2006, 11, 15, 0});
        s = "2006-12-15+02:00";
        cal = dataHelper.toCalendar(s);
        checkCalendar(cal, new int[] {Calendar.YEAR,
                                      Calendar.MONTH,
                                      Calendar.DAY_OF_MONTH,
                                      Calendar.ZONE_OFFSET},
                      new int[] {2006, 11, 15, 7200000});
        s = "2006-12-15-02:00";
        cal = dataHelper.toCalendar(s);
        checkCalendar(cal, new int[] {Calendar.YEAR,
                                      Calendar.MONTH,
                                      Calendar.DAY_OF_MONTH,
                                      Calendar.ZONE_OFFSET},
                      new int[] {2006, 11, 15, -7200000});

        // Duration is special case
        int[] fields = {Calendar.ERA,
                        Calendar.YEAR,
                        Calendar.MONTH,
                        Calendar.DAY_OF_MONTH,
                        Calendar.HOUR_OF_DAY,
                        Calendar.MINUTE,
                        Calendar.SECOND,
                        Calendar.MILLISECOND};
        s = "P1Y3M15D";
        cal = dataHelper.toCalendar(s);
        checkCalendar(cal, fields, new int[] {1, 1, 3, 1+15, 0, 0, 0, 0});
        s = "-P1Y3M15D";
        cal = dataHelper.toCalendar(s);
        checkCalendar(cal, fields, new int[] {0, 1+2, 12-4, 31-15, 0, 0, 0, 0});
        s = "P15M15DT13H";
        cal = dataHelper.toCalendar(s);
        checkCalendar(cal, fields, new int[] {1, 1, 3, 1+15, 13, 0, 0, 0});
        s = "-P15M15DT13H";
        cal = dataHelper.toCalendar(s);
        checkCalendar(cal, fields, new int[] {0, 1+2, 12-4, 31-16, 24-13, 0, 0, 0});
        s = "-PT11H59M59.999S";
        cal = dataHelper.toCalendar(s);
        checkCalendar(cal, fields, new int[] {0, 2, 11, 31, 12, 0, 0, 1});
        s = "PT0S";
        cal = dataHelper.toCalendar(s);
        checkCalendar(cal, fields, new int[] {0, 1, 0, 1, 0, 0, 0, 0});

    }
    
    public void testStringToCalendarWithLocale()
    {
        System.out.println("string to calendar with locale");
        // test with null Locale, default Locale (results should equal)
        Locale loc = Locale.getDefault(); // en_US
        System.out.println(loc);
        String s = "2006-12-15T13:45:00.678Z";
        Date date = toDate(s);
        Calendar cal1 = dataHelper.toCalendar(s);
        Calendar cal2 = dataHelper.toCalendar(s, null);
        Calendar cal3 = dataHelper.toCalendar(s, loc);
        assertEquals(date, cal1.getTime());
        assertEquals(date, cal2.getTime());
        assertEquals(date, cal3.getTime());
        assertEquals(TimeZone.getTimeZone("GMT"), cal1.getTimeZone());
        assertEquals(TimeZone.getTimeZone("GMT"), cal2.getTimeZone());
        assertEquals(TimeZone.getTimeZone("GMT"), cal3.getTimeZone());
        checkCalendar(cal1, new int[] {1, 2006, 11, 15, 13, 45, 0, 678, 0});
        checkCalendar(cal2, new int[] {1, 2006, 11, 15, 13, 45, 0, 678, 0});
        checkCalendar(cal3, new int[] {1, 2006, 11, 15, 13, 45, 0, 678, 0});
    }
    
    public void testCalendarToDateTime()
    {
        System.out.println("calendar to datetime");
        Calendar c = new GregorianCalendar(2006, Calendar.JUNE, 30, 13, 0, 0);
        assertFalse(c.isSet(Calendar.ZONE_OFFSET));
        String exp = "2006-06-30T13:00:00";
        String act = dataHelper.toDateTime(c);
        System.out.println("exp: " + exp);
        System.out.println("act: " + act);
        assertEquals(exp, act);
        assertEquals(TimeZone.getDefault(), c.getTimeZone());
        System.out.println(TimeZone.getDefault() == c.getTimeZone());
        c.setTimeZone(tz0);
        c.clear(Calendar.ZONE_OFFSET);
        c.clear(Calendar.MILLISECOND);
        assertFalse(c.isSet(Calendar.ZONE_OFFSET));
        assertFalse(c.isSet(Calendar.MILLISECOND));
        c.set(Calendar.ZONE_OFFSET, 28800000);
        assertTrue(c.isSet(Calendar.ZONE_OFFSET));
        exp = "2006-06-30T13:00:00+08:00";
        act = dataHelper.toDateTime(c);
        System.out.println("exp: " + exp);
        System.out.println("act: " + act);
        assertEquals(exp, act);
        c.clear(Calendar.ZONE_OFFSET);
        c.setTimeZone(tz3);
        assertEquals(28800000, c.get(Calendar.ZONE_OFFSET));
        c.clear(Calendar.MILLISECOND);
        exp = "2006-06-30T13:00:00+08:00";
        act = dataHelper.toDateTime(c);
        System.out.println("exp: " + exp);
        System.out.println("act: " + act);
        assertEquals(exp, act);
        c.set(Calendar.MINUTE, 59);
        c.set(Calendar.SECOND, 59);
        c.set(Calendar.MILLISECOND, 999);
        exp = "2006-06-30T13:59:59.999+08:00";
        act = dataHelper.toDateTime(c);
        System.out.println("exp: " + exp);
        System.out.println("act: " + act);
        assertEquals(exp, act);
        c.set(Calendar.MILLISECOND, 900);
        exp = "2006-06-30T13:59:59.900+08:00";
        act = dataHelper.toDateTime(c);
        System.out.println("exp: " + exp);
        System.out.println("act: " + act);
        assertEquals(exp, act);
    }

    public void testCalendarToDuration()
    {
        System.out.println("calendar to duration");
        Calendar c = new GregorianCalendar(2006, Calendar.JUNE, 30, 13, 0, 0);
        c.setTimeZone(tz0);
        String exp = "P2006Y5M29DT13H";
        String act = dataHelper.toDuration(c);
        System.out.println("exp: " + exp);
        System.out.println("act: " + act);
        assertEquals(exp, act);
    }

    public void testCalendarToTime()
    {
        System.out.println("calendar to time");
        Calendar c = new GregorianCalendar(2006, Calendar.JUNE, 30, 13, 0, 0);
        assertFalse(c.isSet(Calendar.ZONE_OFFSET));
        String exp = "13:00:00";
        String act = dataHelper.toTime(c);
        System.out.println("exp: " + exp);
        System.out.println("act: " + act);
        assertEquals(exp, act);
        c.setTimeZone(tz0);
        c.clear(Calendar.ZONE_OFFSET);
        c.clear(Calendar.MILLISECOND);
        c.set(Calendar.ZONE_OFFSET, 0);
        exp = "13:00:00Z";
        act = dataHelper.toTime(c);
        System.out.println("exp: " + exp);
        System.out.println("act: " + act);
        assertEquals(exp, act);
        c.clear(Calendar.ZONE_OFFSET);
        c.clear(Calendar.MILLISECOND);
        c.set(Calendar.ZONE_OFFSET, 28800000);
        exp = "13:00:00+08:00";
        act = dataHelper.toTime(c);
        System.out.println("exp: " + exp);
        System.out.println("act: " + act);
        assertEquals(exp, act);
        c.set(Calendar.MINUTE, 59);
        c.set(Calendar.SECOND, 59);
        c.set(Calendar.MILLISECOND, 999);
        c.clear(Calendar.ZONE_OFFSET);
        c.set(Calendar.ZONE_OFFSET, 28800000);
        exp = "13:59:59.999+08:00";
        act = dataHelper.toTime(c);
        System.out.println("exp: " + exp);
        System.out.println("act: " + act);
        assertEquals(exp, act);
        c.set(Calendar.MILLISECOND, 990);
        c.clear(Calendar.ZONE_OFFSET);
        c.set(Calendar.ZONE_OFFSET, 28800000);
        exp = "13:59:59.990+08:00";
        act = dataHelper.toTime(c);
        System.out.println("exp: " + exp);
        System.out.println("act: " + act);
        assertEquals(exp, act);
    }

    public void testCalendarToDay()
    {
        System.out.println("calendar to day");
        Calendar c = new GregorianCalendar(2006, Calendar.JUNE, 30, 13, 0, 0);
        assertFalse(c.isSet(Calendar.ZONE_OFFSET));
        String exp = "---30";
        String act = dataHelper.toDay(c);
        System.out.println("exp: " + exp);
        System.out.println("act: " + act);
        assertEquals(exp, act);
        c.setTimeZone(tz0);
        c.clear(Calendar.ZONE_OFFSET);
        c.clear(Calendar.MILLISECOND);
        c.set(Calendar.ZONE_OFFSET, 0);
        exp = "---30Z";
        act = dataHelper.toDay(c);
        System.out.println("exp: " + exp);
        System.out.println("act: " + act);
        assertEquals(exp, act);
        c.clear(Calendar.ZONE_OFFSET);
        c.clear(Calendar.MILLISECOND);
        c.set(Calendar.ZONE_OFFSET, -28800000);
        exp = "---30-08:00";
        act = dataHelper.toDay(c);
        System.out.println("exp: " + exp);
        System.out.println("act: " + act);
        assertEquals(exp, act);
    }

    public void testCalendarToMonth()
    {
        System.out.println("calendar to month");
        Calendar c = new GregorianCalendar(2006, Calendar.JUNE, 30, 13, 0, 0);
        String exp = "--06";
        String act = dataHelper.toMonth(c);
        System.out.println("exp: " + exp);
        System.out.println("act: " + act);
        assertEquals(exp, act);
        c.setTimeZone(tz0);
        c.clear(Calendar.ZONE_OFFSET);
        c.clear(Calendar.MILLISECOND);
        c.set(Calendar.ZONE_OFFSET, 0);
        exp = "--06Z";
        act = dataHelper.toMonth(c);
        System.out.println("exp: " + exp);
        System.out.println("act: " + act);
        assertEquals(exp, act);
        c.clear(Calendar.ZONE_OFFSET);
        c.clear(Calendar.MILLISECOND);
        c.set(Calendar.ZONE_OFFSET, 28800000);
        exp = "--06+08:00";
        act = dataHelper.toMonth(c);
        System.out.println("exp: " + exp);
        System.out.println("act: " + act);
        assertEquals(exp, act);
    }

    public void testCalendarToMonthDay()
    {
        System.out.println("calendar to monthday");
        Calendar c = new GregorianCalendar(2006, Calendar.JUNE, 30, 13, 0, 0);
        String exp = "--06-30";
        String act = dataHelper.toMonthDay(c);
        System.out.println("exp: " + exp);
        System.out.println("act: " + act);
        assertEquals(exp, act);
        c.setTimeZone(tz0);
        c.clear(Calendar.ZONE_OFFSET);
        c.clear(Calendar.MILLISECOND);
        c.set(Calendar.ZONE_OFFSET, 0);
        exp = "--06-30Z";
        act = dataHelper.toMonthDay(c);
        System.out.println("exp: " + exp);
        System.out.println("act: " + act);
        assertEquals(exp, act);
        c.clear(Calendar.ZONE_OFFSET);
        c.clear(Calendar.MILLISECOND);
        c.set(Calendar.ZONE_OFFSET, -28800000);
        exp = "--06-30-08:00";
        act = dataHelper.toMonthDay(c);
        System.out.println("exp: " + exp);
        System.out.println("act: " + act);
        assertEquals(exp, act);
    }

    public void testCalendarToYear()
    {
        System.out.println("calendar to year");
        Calendar c = new GregorianCalendar(2006, Calendar.JUNE, 30, 13, 0, 0);
        String exp = "2006";
        String act = dataHelper.toYear(c);
        System.out.println("exp: " + exp);
        System.out.println("act: " + act);
        assertEquals(exp, act);
        c.setTimeZone(tz0);
        c.clear(Calendar.ZONE_OFFSET);
        c.clear(Calendar.MILLISECOND);
        c.set(Calendar.ZONE_OFFSET, 0);
        exp = "2006Z";
        act = dataHelper.toYear(c);
        System.out.println("exp: " + exp);
        System.out.println("act: " + act);
        assertEquals(exp, act);
        c.clear(Calendar.ZONE_OFFSET);
        c.clear(Calendar.MILLISECOND);
        c.set(Calendar.ZONE_OFFSET, 28800000);
        exp = "2006+08:00";
        act = dataHelper.toYear(c);
        System.out.println("exp: " + exp);
        System.out.println("act: " + act);
        assertEquals(exp, act);
    }

    public void testCalendarToYearMonth()
    {
        System.out.println("calendar to yearmonth");
        Calendar c = new GregorianCalendar(2006, Calendar.JUNE, 30, 13, 0, 0);
        String exp = "2006-06";
        String act = dataHelper.toYearMonth(c);
        System.out.println("exp: " + exp);
        System.out.println("act: " + act);
        assertEquals(exp, act);
        c.setTimeZone(tz0);
        c.clear(Calendar.ZONE_OFFSET);
        c.clear(Calendar.MILLISECOND);
        c.set(Calendar.ZONE_OFFSET, 0);
        exp = "2006-06Z";
        act = dataHelper.toYearMonth(c);
        System.out.println("exp: " + exp);
        System.out.println("act: " + act);
        assertEquals(exp, act);
        c.clear(Calendar.ZONE_OFFSET);
        c.clear(Calendar.MILLISECOND);
        c.set(Calendar.ZONE_OFFSET, -28800000);
        exp = "2006-06-08:00";
        act = dataHelper.toYearMonth(c);
        System.out.println("exp: " + exp);
        System.out.println("act: " + act);
        assertEquals(exp, act);
    }

    public void testCalendarToYearMonthDay()
    {
        System.out.println("calendar to yearmonthday");
        Calendar c = new GregorianCalendar(2006, Calendar.JUNE, 30, 13, 0, 0);
        String exp = "2006-06-30";
        String act = dataHelper.toYearMonthDay(c);
        System.out.println("exp: " + exp);
        System.out.println("act: " + act);
        assertEquals(exp, act);
        c.setTimeZone(tz0);
        c.clear(Calendar.ZONE_OFFSET);
        c.clear(Calendar.MILLISECOND);
        c.set(Calendar.ZONE_OFFSET, 0);
        exp = "2006-06-30Z";
        act = dataHelper.toYearMonthDay(c);
        System.out.println("exp: " + exp);
        System.out.println("act: " + act);
        assertEquals(exp, act);
        c.clear(Calendar.ZONE_OFFSET);
        c.clear(Calendar.MILLISECOND);
        c.set(Calendar.ZONE_OFFSET, 28800000);
        exp = "2006-06-30+08:00";
        act = dataHelper.toYearMonthDay(c);
        System.out.println("exp: " + exp);
        System.out.println("act: " + act);
        assertEquals(exp, act);
    }

    // For tests of DataHelper.toXXX(Date), since Date does not have timezone
    // information, the timezone is normalized to GMT.

    public void testDateToDateTime()
    {
        System.out.println("date to datetime");
        Calendar c = new GregorianCalendar(2006, Calendar.JUNE, 30, 13, 0, 0);
        c.setTimeZone(tz0);
        c.set(Calendar.ZONE_OFFSET, 0);
        String fromc = dataHelper.toDateTime(c);
        Date d = c.getTime();
        String exp = "2006-06-30T13:00:00Z";
        String act = dataHelper.toDateTime(d);
        System.out.println("exp: " + exp);
        System.out.println("act: " + act);
        assertEquals(exp, act);
        assertEquals(fromc, act);
    }

    public void testDateToDuration()
    {
        System.out.println("date to duration");
        Calendar c = new GregorianCalendar(2006, Calendar.JUNE, 30, 13, 0, 0);
        c.setTimeZone(tz0);
        c.set(Calendar.ZONE_OFFSET, 0);
        Date d = c.getTime();
        System.out.println(d);
        String exp = "P2006Y5M29DT13H";
        String act1 = dataHelper.toDuration(c);
        String act2 = dataHelper.toDuration(d);
        System.out.println("exp: " + exp);
        System.out.println("c2d: " + act1);
        System.out.println("d2d: " + act2);
        assertEquals(exp, act2);
        assertEquals(act1, act2);
        c.clear(Calendar.HOUR_OF_DAY);
        c.clear(Calendar.MINUTE);
        c.clear(Calendar.SECOND);
        c.clear(Calendar.MILLISECOND);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        d = c.getTime();
        exp = "P2006Y5M29D";
        act1 = dataHelper.toDuration(c);
        act2 = dataHelper.toDuration(d);
        System.out.println("exp: " + exp);
        System.out.println("c2d: " + act1);
        System.out.println("d2d: " + act2);
        assertEquals(exp, act2);
        assertEquals(act1, act2);
    }

    public void testDateToTime()
    {
        System.out.println("date to time");
        Calendar c = new GregorianCalendar(2006, Calendar.JUNE, 30, 13, 0, 0);
        c.setTimeZone(tz0);
        c.set(Calendar.ZONE_OFFSET, 0);
        String fromc = dataHelper.toTime(c);
        Date d = c.getTime();
        String exp = "13:00:00Z";
        String act = dataHelper.toTime(d);
        System.out.println("exp: " + exp);
        System.out.println("act: " + act);
        assertEquals(exp, act);
        assertEquals(fromc, act);
    }

    public void testDateToDay()
    {
        System.out.println("date to day");
        Calendar c = new GregorianCalendar(2006, Calendar.JUNE, 30, 13, 0, 0);
        c.setTimeZone(tz0);
        c.set(Calendar.ZONE_OFFSET, 0);
        String fromc = dataHelper.toDay(c);
        Date d = c.getTime();
        String exp = "---30";
        String act = dataHelper.toDay(d);
        System.out.println("exp: " + exp);
        System.out.println("act: " + act);
        assertEquals(exp, act);
        assertEquals(exp + "Z", fromc);
    }

    public void testDateToMonth()
    {
        System.out.println("date to month");
        Calendar c = new GregorianCalendar(2006, Calendar.JUNE, 30, 13, 0, 0);
        c.setTimeZone(tz0);
        c.set(Calendar.ZONE_OFFSET, 0);
        String fromc = dataHelper.toMonth(c);
        Date d = c.getTime();
        String exp = "--06";
        String act = dataHelper.toMonth(d);
        System.out.println("exp: " + exp);
        System.out.println("act: " + act);
        assertEquals(exp, act);
        assertEquals(exp + "Z", fromc);
    }

    public void testDateToMonthDay()
    {
        System.out.println("date to monthday");
        Calendar c = new GregorianCalendar(2006, Calendar.JUNE, 30, 13, 0, 0);
        c.setTimeZone(tz0);
        c.set(Calendar.ZONE_OFFSET, 0);
        String fromc = dataHelper.toMonthDay(c);
        Date d = c.getTime();
        String exp = "--06-30";
        String act = dataHelper.toMonthDay(d);
        System.out.println("exp: " + exp);
        System.out.println("act: " + act);
        assertEquals(exp, act);
        assertEquals(exp + "Z", fromc);
    }

    public void testDateToYear()
    {
        System.out.println("date to year");
        Calendar c = new GregorianCalendar(2006, Calendar.JUNE, 30, 13, 0, 0);
        c.setTimeZone(tz0);
        c.set(Calendar.ZONE_OFFSET, 0);
        String fromc = dataHelper.toYear(c);
        Date d = c.getTime();
        String exp = "2006";
        String act = dataHelper.toYear(d);
        System.out.println("exp: " + exp);
        System.out.println("act: " + act);
        assertEquals(exp, act);
        assertEquals(exp + "Z", fromc);
    }

    public void testDateToYearMonth()
    {
        System.out.println("date to yearmonth");
        Calendar c = new GregorianCalendar(2006, Calendar.JUNE, 30, 13, 0, 0);
        c.setTimeZone(tz0);
        c.set(Calendar.ZONE_OFFSET, 0);
        String fromc = dataHelper.toYearMonth(c);
        Date d = c.getTime();
        String exp = "2006-06";
        String act = dataHelper.toYearMonth(d);
        System.out.println("exp: " + exp);
        System.out.println("act: " + act);
        assertEquals(exp, act);
        assertEquals(exp + "Z", fromc);
    }

    public void testDateToYearMonthDay()
    {
        System.out.println("date to yearmonthday");
        Calendar c = new GregorianCalendar(2006, Calendar.JUNE, 30, 13, 0, 0);
        c.setTimeZone(tz0);
        c.set(Calendar.ZONE_OFFSET, 0);
        String fromc = dataHelper.toYearMonthDay(c);
        Date d = c.getTime();
        String exp = "2006-06-30";
        String act = dataHelper.toYearMonthDay(d);
        System.out.println("exp: " + exp);
        System.out.println("act: " + act);
        assertEquals(exp, act);
        assertEquals(exp + "Z", fromc);
    }

}
