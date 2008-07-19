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

import java.io.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.math.BigDecimal;
import java.math.BigInteger;

import javax.sdo.DataObject;
import javax.sdo.Property;
import javax.sdo.Type;
import javax.sdo.ChangeSummary;
import javax.sdo.helper.DataFactory;
import javax.sdo.helper.TypeHelper;
import javax.sdo.helper.XMLHelper;
import javax.sdo.helper.XMLDocument;

import davos.sdo.DataObjectXML;
import davos.sdo.Options;
import davos.sdo.impl.helpers.DataGraphHelper;

import org.apache.xmlbeans.impl.util.XsTypeConverter;

import sdo.test.basic.*;

import junit.framework.*;
import common.DataTest;

/**
 * @author Wing Yew Poon
 */
public class BasicTest extends DataTest
{
    public BasicTest(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        
        TestSuite suite = new TestSuite();
        
        suite.addTest(new BasicTest("testMapping"));
        suite.addTest(new BasicTest("testGetAndSetList"));
        suite.addTest(new BasicTest("testGetAndSet1"));
        suite.addTest(new BasicTest("testGetAndSet2"));
        suite.addTest(new BasicTest("testCreate"));
        
        suite.addTest(new BasicTest("testBasicTypes"));
        suite.addTest(new BasicTest("testUnmarshalValidEmptyContent"));
        suite.addTest(new BasicTest("testUnmarshalInvalidEmptyContent"));
        suite.addTest(new BasicTest("testBasicTypes2"));
        suite.addTest(new BasicTest("testSetString"));
        
        suite.addTest(new BasicTest("testDuration"));
        suite.addTest(new BasicTest("testFloat"));
        suite.addTest(new BasicTest("testBinary"));
        //suite.addTest(new BasicTest("testDate"));
        suite.addTest(new BasicTest("testStrings"));
        suite.addTest(new BasicTest("testSetNull"));
        
        suite.addTest(new BasicTest("testSetSimpleListType"));
        suite.addTest(new BasicTest("testSetSimpleListType2"));
        suite.addTest(new BasicTest("testSetSimpleListType3"));
        suite.addTest(new BasicTest("testMarshalSimpleListType"));
        suite.addTest(new BasicTest("testUnmarshalSimpleListType"));
        suite.addTest(new BasicTest("testGetSimpleListType"));
        
        suite.addTest(new BasicTest("testUnmarshalSimpleUnionType1"));
        suite.addTest(new BasicTest("testUnmarshalSimpleUnionType2"));
        suite.addTest(new BasicTest("testSetAndMarshalSimpleUnionType"));
        suite.addTest(new BasicTest("testSetBaselessIntType"));
        
        suite.addTest(new BasicTest("testUnmarshalAnyType"));
        suite.addTest(new BasicTest("testUnmarshalAnyType2"));
        suite.addTest(new BasicTest("testUnmarshalAnyType3"));
        suite.addTest(new BasicTest("testUnmarshalAnySimpleType"));
        suite.addTest(new BasicTest("testUnmarshalAnySimpleType2"));
        suite.addTest(new BasicTest("testUnmarshalAnySimpleType3"));
        suite.addTest(new BasicTest("testMarshalAnyType1"));
        suite.addTest(new BasicTest("testMarshalAnyType2"));
        suite.addTest(new BasicTest("testGetAndSetQName"));
        suite.addTest(new BasicTest("testUnmarshalQName"));
        suite.addTest(new BasicTest("testSetAndMarshalQName"));
        suite.addTest(new BasicTest("testUnmarshalSimpleTypeRootElement"));
        suite.addTest(new BasicTest("testCharacter0"));
        suite.addTest(new BasicTest("testDataFactoryWrapperTypes"));
        
        // or
        //TestSuite suite = new TestSuite(BasicTest.class);
        return suite;
    }

    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(suite());
    }

    public static File dir;
    static
    {
        dir = new File(OUTPUTROOT + S + "data");
        dir.mkdirs();
    }

    private static final String BASIC_URI = "http://sdo/test/basic";
    private static DataFactory factory = context.getDataFactory();
    private static TypeHelper typeHelper = context.getTypeHelper();
    private static XMLHelper xmlHelper = context.getXMLHelper();

    protected static void compareBytes(byte[] expected, byte[] actual)
    {
        assertEquals(expected.length, actual.length);
        for (int i = 0; i < expected.length; i++)
        {
            byte b1 = expected[i];
            byte b2 = actual[i];
            assertEquals(b1, b2);
        }
    }

    private void _testProperty(Property p, String sdoTypeName, Class instanceClass)
    {
        Type t = typeHelper.getType("commonj.sdo", sdoTypeName);
        assertNotNull(t);
        assertEquals(t, p.getType());
        System.out.println(p.getName());
        System.out.println(p.getType());
        System.out.println(t.getInstanceClass());
        assertEquals(instanceClass, t.getInstanceClass());
    }

    public void testMapping()
    {
        String uri = BASIC_URI;
        String name = "A";
        Type t = typeHelper.getType(uri, name);
        assertNotNull(t);
        /*
        int i = 0;
        for (Property p : (List<Property>)t.getProperties())
        {
            System.out.println(++i + ": " + p.getName());
            System.out.println("  " + p.getType());
        }
        */
        Property p = t.getProperty("any0"); // typeless element -> anyType
        _testProperty(p, "DataObject", DataObject.class);
        p = t.getProperty("any1"); // anyType
        _testProperty(p, "DataObject", DataObject.class);
        p = t.getProperty("any2"); // anySimpleType
        _testProperty(p, "Object", Object.class);
        p = t.getProperty("any3"); // typeless attribute -> anySimpleType
        _testProperty(p, "Object", Object.class);
        p = t.getProperty("uri1"); // anyURI
        _testProperty(p, "URI", String.class);
        p = t.getProperty("bytes1"); // base64Binary
        _testProperty(p, "Bytes", byte[].class);
        p = t.getProperty("boolean0"); // boolean
        _testProperty(p, "Boolean", boolean.class);
        p = t.getProperty("byte0"); // byte
        _testProperty(p, "Byte", byte.class);
        p = t.getProperty("yearMonthDay"); // date
        _testProperty(p, "YearMonthDay", String.class);
        p = t.getProperty("dateTime"); // dateTime
        _testProperty(p, "DateTime", String.class);
        p = t.getProperty("decimal"); // decimal
        _testProperty(p, "Decimal", BigDecimal.class);
        p = t.getProperty("double0"); // double
        _testProperty(p, "Double", double.class);
        p = t.getProperty("duration"); // duration
        _testProperty(p, "Duration", String.class);
        p = t.getProperty("float0"); // float
        _testProperty(p, "Float", float.class);
        p = t.getProperty("day"); // gDay
        _testProperty(p, "Day", String.class);
        p = t.getProperty("month"); // gMonth
        _testProperty(p, "Month", String.class);
        p = t.getProperty("monthDay"); // gMonthDay
        _testProperty(p, "MonthDay", String.class);
        p = t.getProperty("year"); // gYear
        _testProperty(p, "Year", String.class);
        p = t.getProperty("yearMonth"); //gYearMonth
        _testProperty(p, "YearMonth", String.class);
        p = t.getProperty("bytes2"); // hexBinary
        _testProperty(p, "Bytes", byte[].class);
        p = t.getProperty("int0"); // int
        _testProperty(p, "Int", int.class);
        p = t.getProperty("integer0"); // integer
        _testProperty(p, "Integer", BigInteger.class);
        p = t.getProperty("string1"); // language
        _testProperty(p, "String", String.class);
        p = t.getProperty("long0"); // long
        _testProperty(p, "Long", long.class);
        p = t.getProperty("string2"); // Name
        _testProperty(p, "String", String.class);
        p = t.getProperty("string3"); // NCName
        _testProperty(p, "String", String.class);
        p = t.getProperty("integer1"); // negativeInteger
        _testProperty(p, "Integer", BigInteger.class);
        p = t.getProperty("integer2"); // nonNegativeInteger
        _testProperty(p, "Integer", BigInteger.class);
        p = t.getProperty("integer3"); // nonPositiveInteger
        _testProperty(p, "Integer", BigInteger.class);
        p = t.getProperty("string4"); // normalizedString
        _testProperty(p, "String", String.class);
        p = t.getProperty("integer4"); // positiveInteger
        _testProperty(p, "Integer", BigInteger.class);
        p = t.getProperty("uri2"); // QName
        _testProperty(p, "URI", String.class);
        p = t.getProperty("short0"); // short
        _testProperty(p, "Short", short.class);
        p = t.getProperty("string0"); // string
        _testProperty(p, "String", String.class);
        p = t.getProperty("time"); // time
        _testProperty(p, "Time", String.class);
        p = t.getProperty("string5"); // token
        _testProperty(p, "String", String.class);
        p = t.getProperty("short1"); // unsignedByte
        _testProperty(p, "Short", short.class);
        p = t.getProperty("long1"); // unsignedInt
        _testProperty(p, "Long", long.class);
        p = t.getProperty("integer5"); // unsignedLong
        _testProperty(p, "Integer", BigInteger.class);
        p = t.getProperty("int1"); // unsignedShort
        _testProperty(p, "Int", int.class);

        p = t.getProperty("id");
        _testProperty(p, "String", String.class);
        p = t.getProperty("idref");
        _testProperty(p, "String", String.class);
        p = t.getProperty("idrefs");
        _testProperty(p, "Strings", List.class);
        p = t.getProperty("entity");
        _testProperty(p, "String", String.class);
        p = t.getProperty("entities");
        _testProperty(p, "Strings", List.class);
        p = t.getProperty("nmtoken");
        _testProperty(p, "String", String.class);
        p = t.getProperty("nmtokens");
        _testProperty(p, "Strings", List.class);

        p = t.getProperty("notation");
        Type t1 = (Type)p.getType().getBaseTypes().get(0);
        assertEquals(typeHelper.getType("commonj.sdo", "String"), t1);
    }

    /* test getting and setting a List */
    public void testGetAndSetList() throws Exception
    {
        String uri = "http://sdo/test/basic0";

        // getList() returns non-null but empty list for
        // unset many-valued property
        // updates to the list are reflected in the current values of the
        // data object directly and immediately
        DataObject root = getRootDataObject("data", "basic0a.xml");
        List cList = root.getList("c");
        assertNotNull(cList);
        assertEquals(0, cList.size());
        assertFalse(root.isSet("c"));
        cList.add("xxx");
        cList.add("zzz");
        assertTrue(root.isSet("c"));
        assertEquals("xxx", root.get("c[1]"));
        assertEquals("zzz", root.get("c[2]"));
        //xmlHelper.save(root, uri, "a", System.out);
        //System.out.println();
        saveDataObject(root, uri, "a", new File(dir, "basic0b.xml"));
        compareXMLFiles(getResourceFile("data", "basic0b.xml"),
                        new File(dir, "basic0b.xml"));

        root = getRootDataObject("data", "basic0b.xml");
        cList = root.getList("c");
        assertNotNull(cList);
        assertEquals(2, cList.size());
        assertTrue(root.isSet("c"));
        assertEquals("xxx", cList.get(0));
        assertEquals("zzz", cList.get(1));
        cList.add(1, "yyy");
        //xmlHelper.save(root, uri, "a", System.out);
        //System.out.println();
        saveDataObject(root, uri, "a", new File(dir, "basic0c.xml"));
        compareXMLFiles(getResourceFile("data", "basic0c.xml"),
                        new File(dir, "basic0c.xml"));

        // get(property) always returns a list for a many-valued property
        // in this case instance has one occurence of the property
        // we set the property with a list this time
        // this is equivalent to getList(property).clear() followed by
        // getList(property).addAll(value)
        root = getRootDataObject("data", "basic0d.xml");
        cList = (List)root.get("c");
        assertNotNull(cList);
        assertEquals(1, cList.size());
        assertTrue(root.isSet("c"));
        assertEquals("aaa", root.get("c[1]"));
        assertEquals("aaa", cList.get(0));
        List newList = new ArrayList();
        newList.add("xxx");
        newList.add("zzz");
        root.setList("c", newList);
        saveDataObject(root, uri, "a", new File(dir, "basic0d.xml"));
        compareXMLFiles(getResourceFile("data", "basic0b.xml"),
                        new File(dir, "basic0d.xml"));
    }

    /* test get and set using dynamic SDO */
    public void testGetAndSet1() throws Exception
    {
        // load instance of basic.xsd with root element "a"
        File f = getResourceFile("data", "basic.xml"); // this instance does not contain "e"
        InputStream in = new FileInputStream(f);
        XMLDocument doc = xmlHelper.load(in);
        in.close();
        assertEquals(BASIC_URI, doc.getRootElementURI());
        assertEquals("a", doc.getRootElementName());
        DataObject root = doc.getRootObject();
        Type t = typeHelper.getType(BASIC_URI, "A");
        assertEquals(t, root.getType());

        BigDecimal decimal = root.getBigDecimal("decimal");
        assertEquals(new BigDecimal("9.999999999"), decimal);
        BigInteger integer = root.getBigInteger("integer0");
        assertEquals(new BigInteger("1234567890"), integer);
        List cList = root.getList("c");
        assertEquals(3, cList.size());
        assertEquals("xxx", cList.get(0));
        assertEquals("yyy", cList.get(1));
        assertEquals("zzz", cList.get(2));

        List eList = new ArrayList();
        eList.add(new Integer(1));
        eList.add(new Integer(2));
        eList.add(new Integer(3));
        root.set("e", eList);

        Object e = root.get("e");
        assertTrue(e instanceof List);
        List eList2 = (List)e;
        System.out.println(eList2 == eList);
        assertEquals(new Integer(1), eList2.get(0));
        assertEquals(new Integer(2), eList2.get(1));
        assertEquals(new Integer(3), eList2.get(2));

        xmlHelper.save(root, BASIC_URI, "a", System.out);
        System.out.println();
    }

    /* test get and set using static SDO */
    public void testGetAndSet2() throws Exception
    {
        // load instance of basic.xsd with root element "a"
        File f = getResourceFile("data", "basic.xml");
        InputStream in = new FileInputStream(f);
        XMLDocument doc = xmlHelper.load(in);
        in.close();
        assertEquals(BASIC_URI, doc.getRootElementURI());
        assertEquals("a", doc.getRootElementName());

        A root = (A)doc.getRootObject();
        BigDecimal decimal = root.getDecimal();
        assertEquals(new BigDecimal("9.999999999"), decimal);
        BigInteger integer = root.getInteger0();
        assertEquals(new BigInteger("1234567890"), integer);
        List cList = root.getC();
        assertEquals(3, cList.size());
        assertEquals("xxx", cList.get(0));
        assertEquals("yyy", cList.get(1));
        assertEquals("zzz", cList.get(2));

        List eList = new ArrayList();
        eList.add(new Integer(1));
        eList.add(new Integer(2));
        eList.add(new Integer(3));
        root.setE(eList);

        List eList2 = root.getE();
        System.out.println(eList2 == eList);
        assertEquals(new Integer(1), eList2.get(0));
        assertEquals(new Integer(2), eList2.get(1));
        assertEquals(new Integer(3), eList2.get(2));

        xmlHelper.save((DataObject)root, BASIC_URI, "a", System.out);
        System.out.println();
    }

    public void testCreate() throws Exception
    {
        String uri = BASIC_URI;
        String name = "A";
        Type t = typeHelper.getType(uri, name);
        assertNotNull(t);
        /*
        int i = 0;
        for (Property p : (List<Property>)t.getProperties())
        {
            System.out.println(++i + ": " + p.getName());
            System.out.println("  " + p.getType());
        }
        */
        A a = (A)factory.create(t);
        assertNotNull(a);
        BigDecimal decimal = new BigDecimal("9.999999999");
        a.setDecimal(decimal);
        BigInteger integer = new BigInteger("1234567890");
        a.setInteger0(integer);
        // many-valued property of simple type
        List cList = a.getC();
        cList.add("aaa");
        cList.add("bbb");
        cList.add("ccc");
        // single-valued property of simple list type
        List eList = new ArrayList();
        eList.add(new Integer(1));
        eList.add(new Integer(2));
        eList.add(new Integer(3));
        a.setE(eList);
        xmlHelper.save((DataObject)a, uri, "a", System.out);
        System.out.println();
    }

    private void _testType(String propName, Object propValue,
                           String elemName, String elemValue)
    {
        Type t = typeHelper.getType(BASIC_URI, "A");
        DataObject a = factory.create(t);
        a.set(propName, propValue);
        Object value = a.get(propName);
        assertEquals(propValue, value);
        String begin = "<bas:a xmlns:bas=\"" + BASIC_URI + "\">";
        String end = "</bas:a>";
        String beginElem = "<" + elemName + ">";
        String endElem = "</" + elemName + ">";
        String xml = begin + beginElem + elemValue + endElem + end;
        String out = xmlHelper.save(a, BASIC_URI, "a");
        System.out.println(out);
        assertEquals(xml, out);

        XMLDocument doc = xmlHelper.load(xml);
        a = doc.getRootObject();
        value = a.get(propName);
        assertEquals(propValue, value);
    }

    private String getBasicAString(String elemName, String elemValue)
    {
        String begin = "<bas:a xmlns:bas=\"" + BASIC_URI + "\">";
        String end = "</bas:a>";
        String nil =
            " xsi:nil=\"true\" xmlns:xsi=\"" + XSI_URI + "\"";
        String beginElem =
            "<" + elemName +
            (elemValue == null ? nil + "/>" : ">");
        String endElem = "</" + elemName + ">";
        String xml =
            begin + beginElem +
            (elemValue == null ? "" : elemValue + endElem) +
            end;
        return xml;
    }

    private DataObject getBasicA(String elemName, String elemValue)
    {
        String xml = getBasicAString(elemName, elemValue);
        XMLDocument doc = xmlHelper.load(xml);
        return doc.getRootObject();
    }

    /* test marshalling and unmarshalling of SDO types mapped to by
       builtin xs types */
    public void testBasicTypes()
    {
        // test the String-based date-time types
        _testType("yearMonthDay", "2007-03-21", "yearMonthDay", "2007-03-21");
        _testType("dateTime", "2007-03-20T20:07:00-04:00", "dateTime", "2007-03-20T20:07:00-04:00");
        _testType("dateTime", "1970-01-01T00:00:00.123456789", "dateTime", "1970-01-01T00:00:00.123456789");
        _testType("day", "---02", "day", "---02");
        _testType("month", "--04", "month", "--04");
        _testType("monthDay", "--04-02", "monthDay", "--04-02");
        _testType("year", "2007", "year", "2007");
        _testType("yearMonth", "2007-03", "yearMonth", "2007-03");
        _testType("duration", "P1DT2H", "duration", "P1DT2H");
        _testType("duration", "P1Y2M3DT10H30M5S", "duration", "P1Y2M3DT10H30M5S");
    }

    private void _testEmptyElement(String propName, String elemName, 
                                   boolean valid)
    {
        String begin = "<bas:a xmlns:bas=\"" + BASIC_URI + "\">";
        String end = "</bas:a>";
        String beginElem = "<" + elemName + ">";
        String endElem = "</" + elemName + ">";
        String elem = "<" + elemName + "/>";
        String xml1 = begin + beginElem + endElem + end;
        String xml2 = begin + elem + end;
        if (valid)
        {
            DataObject a1 = xmlHelper.load(xml1).getRootObject();
            assertTrue(a1.isSet(propName));
            if (propName.equals("bytes1") || propName.equals("bytes2"))
            {
                Object value = a1.get(propName);
                assertTrue(value instanceof byte[]);
                byte[] bytes = (byte[])value;
                assertEquals(0, bytes.length);
            }
            else
                assertEquals("", a1.get(propName));
            DataObject a2 = xmlHelper.load(xml2).getRootObject();
            assertTrue(a2.isSet(propName));
            if (propName.equals("bytes1") || propName.equals("bytes2"))
            {
                Object value = a1.get(propName);
                assertTrue(value instanceof byte[]);
                byte[] bytes = (byte[])value;
                assertEquals(0, bytes.length);
            }
            else
                assertEquals("", a2.get(propName));
        }
        else
        {
            try
            {
                xmlHelper.load(xml1);
                fail("loading the following should have failed: " + xml1);
            }
            catch (Exception e)
            {
                assertTrue(e instanceof davos.sdo.SDOUnmarshalException);
                assertTrue(e.getMessage().indexOf("Could not convert value ''") > 1);
            }
            try
            {
                xmlHelper.load(xml2);
                fail("loading the following should have failed: " + xml2);
            }
            catch (Exception e)
            {
                assertTrue(e instanceof davos.sdo.SDOUnmarshalException);
                assertTrue(e.getMessage().indexOf("Could not convert value ''") > 1);
            }
        }
    }

    public void testUnmarshalValidEmptyContent()
    {
        _testEmptyElement("uri1", "uri1", true); // anyURI - ?
        _testEmptyElement("string1", "string1", true); // language
        _testEmptyElement("string2", "string2", true); // Name
        _testEmptyElement("string3", "string3", true); // NCName
        _testEmptyElement("string4", "string4", true); // normalizedString
        _testEmptyElement("string0", "string0", true); // string
        _testEmptyElement("string5", "string5", true); // token

        _testEmptyElement("bytes1", "bytes1", true); // base64Binary
        _testEmptyElement("bytes2", "bytes2", true); // hexBinary
        /*
        _testEmptyElement("", "", true);
        */
    }

    public void testUnmarshalInvalidEmptyContent()
    {
        _testEmptyElement("boolean0", "boolean0", false);
        _testEmptyElement("byte0", "byte0", false);
        _testEmptyElement("decimal", "decimal", false);
        _testEmptyElement("double0", "double0", false);
        _testEmptyElement("float0", "float0", false);
        _testEmptyElement("int0", "int0", false);
        _testEmptyElement("integer0", "integer0", false);
        _testEmptyElement("long0", "long0", false);
        _testEmptyElement("integer1", "integer1", false); // negativeInteger
        _testEmptyElement("integer2", "integer2", false); // nonNegativeInteger
        _testEmptyElement("integer3", "integer3", false); // nonPositiveInteger
        _testEmptyElement("integer4", "integer4", false); // positiveInteger
        _testEmptyElement("short0", "short0", false);
        _testEmptyElement("short1", "short1", false); // unsignedByte
        _testEmptyElement("long1", "long1", false); // unsignedInt
        _testEmptyElement("integer5", "integer5", false); // unsignedLong
        _testEmptyElement("int1", "int1", false); // unsignedShort

        _testEmptyElement("uri2", "uri2", false); // QName
        
        _testEmptyElement("yearMonthDay", "yearMonthDay", false); // date
        _testEmptyElement("dateTime", "dateTime", false);
        _testEmptyElement("duration", "duration", false);
        _testEmptyElement("day", "day", false); // gDay
        _testEmptyElement("month", "month", false); // gMonth
        _testEmptyElement("monthDay", "monthDay", false); // gMonthDay
        _testEmptyElement("year", "year", false); // gYear
        _testEmptyElement("yearMonth", "yearMonth", false); //gYearMonth
        
    }

    private void _testUnmarshal(String elemName, String elemValue, 
                                boolean valid, 
                                String propName, Object propValue)
    {
        String begin = "<bas:a xmlns:bas=\"" + BASIC_URI + "\">";
        String end = "</bas:a>";
        String beginElem = "<" + elemName + ">";
        String endElem = "</" + elemName + ">";
        String xml = begin + beginElem + elemValue + endElem + end;
        if (valid)
        {
            DataObject a = xmlHelper.load(xml).getRootObject();
            assertTrue(a.isSet(propName));
            assertEquals(propValue, a.get(propName));
        }
        else
        {
            try
            {
                xmlHelper.load(xml);
                fail("loading the following should have failed: " + xml);
            }
            catch (Exception e)
            {
                //System.out.println(e);
                assertTrue(e instanceof davos.sdo.SDOUnmarshalException);
                assertTrue(e.getMessage().indexOf("Could not convert value '" + elemValue + "'" ) > 1);
            }
        }
    }

    /* test unmarshalling of SDO types mapped to by builtin xs types, 
       with valid and invalid values */
    public void testBasicTypes2()
    {
        _testUnmarshal("int0", "100", true, "int0", 100);
        _testUnmarshal("int0", "abc", false, "int0", null);
        // test the String-based date-time types
        _testUnmarshal("dateTime", "2007-03-21T00:00:00", true, "dateTime", "2007-03-21T00:00:00");
        _testUnmarshal("dateTime", "2007-03-21", false, "dateTime", null);
        _testUnmarshal("dateTime", "zzz", false, "dateTime", null);
        _testUnmarshal("yearMonthDay", "2007-03-21T00:00:00", false, "yearMonthDay", null);
        _testUnmarshal("yearMonthDay", "2007-03-21", true, "yearMonthDay", "2007-03-21");
        _testUnmarshal("yearMonthDay", "2007-03", false, "yearMonthDay", null);
        _testUnmarshal("duration", "2007-03-21T00:00:00", false, "duration", null);
        _testUnmarshal("duration", "PT1S", true, "duration", "PT1S");
        _testUnmarshal("time", "2007-03-21T12:00:00", false, "time", null);
        _testUnmarshal("time", "12:00:00", true, "time", "12:00:00");
        _testUnmarshal("time", "2007-03-21", false, "time", null);
        _testUnmarshal("yearMonth", "2007-03-21T00:00:00", false, "yearMonth", null);
        _testUnmarshal("yearMonth", "2007-03-21", false, "yearMonth", null);
        _testUnmarshal("yearMonth", "2007-03", true, "yearMonth", "2007-03");
        _testUnmarshal("yearMonth", "2007", false, "yearMonth", null);
        _testUnmarshal("year", "2007-03-21T00:00:00", false, "year", null);
        _testUnmarshal("year", "2007-03-21", false, "year", null);
        _testUnmarshal("year", "2007-03", false, "year", null);
        _testUnmarshal("year", "2007", true, "year", "2007");
        _testUnmarshal("year", "0", false, "year", null);
        _testUnmarshal("monthDay", "2007-03-21T00:00:00", false, "monthDay", null);
        _testUnmarshal("monthDay", "2007-03-21", false, "monthDay", null);
        _testUnmarshal("monthDay", "--03-21", true, "monthDay", "--03-21");
        _testUnmarshal("monthDay", "2007-03", false, "monthDay", null);
        _testUnmarshal("month", "2007-03-21T00:00:00", false, "month", null);
        _testUnmarshal("month", "2007-03-21", false, "month", null);
        _testUnmarshal("month", "2007-03", false, "month", null);
        _testUnmarshal("month", "--03-21", false, "month", null);
        _testUnmarshal("month", "--03", true, "month", "--03");
        _testUnmarshal("month", "---21", false, "month", null);
        _testUnmarshal("day", "2007-03-21T00:00:00", false, "day", null);
        _testUnmarshal("day", "2007-03-21", false, "day", null);
        _testUnmarshal("day", "--03-21", false, "day", null);
        _testUnmarshal("day", "---21", true, "day", "---21");
        _testUnmarshal("day", "2007-03", false, "day", null);
    }

    private void _testSetString(String propName, String value, 
                                boolean valid, Object propValue)
    {
        Type t = typeHelper.getType(BASIC_URI, "A");
        DataObject a = factory.create(t);
        if (valid)
        {
            a.setString(propName, value);
            assertEquals(propValue, a.get(propName));
        }
        else
        {
            try
            {
                a.setString(propName, value);
                fail("should have thrown an exception but instead " + propName + " was set to " + a.get(propName));
            }
            catch (Exception e)
            {
                assertTrue(e instanceof ClassCastException);
                System.out.println(e.getMessage());
            }
        }
    }

    /* test the use of setString to set values for basic types 
       using valid and invalid values */
    public void testSetString()
    {
        System.out.println("testSetString()");
        _testSetString("boolean0", "", false, null);
        _testSetString("byte0", "", false, null);
        _testSetString("decimal", "", false, null);
        _testSetString("double0", "", false, null);
        _testSetString("float0", "", false, null);
        _testSetString("int0", "", false, null);
        _testSetString("integer0", "", false, null);
        _testSetString("long0", "", false, null);
        _testSetString("integer1", "", false, null); // negativeInteger
        _testSetString("integer2", "", false, null); // nonNegativeInteger
        _testSetString("integer3", "", false, null); // nonPositiveInteger
        _testSetString("integer4", "", false, null); // positiveInteger
        _testSetString("short0", "", false, null);
        _testSetString("short1", "", false, null); // unsignedByte
        _testSetString("long1", "", false, null); // unsignedInt
        _testSetString("integer5", "", false, null); // unsignedLong
        _testSetString("int1", "", false, null); // unsignedShort

        _testSetString("boolean0", null, false, null);
        _testSetString("boolean0", "True", false, null);
        _testSetString("boolean0", "TRUE", false, null);
        _testSetString("boolean0", "False", false, null);
        _testSetString("boolean0", "FALSE", false, null);
        _testSetString("boolean0", "2", false, null);
        _testSetString("boolean0", "true", true, Boolean.TRUE);
        _testSetString("boolean0", "1", true, Boolean.TRUE);
        _testSetString("boolean0", "false", true, Boolean.FALSE);
        _testSetString("boolean0", "0", true, Boolean.FALSE);
        _testSetString("int0", "100", true, 100);
        _testSetString("int0", "abc", false, null);

        _testSetString("dateTime", "2007-03-21T00:00:00", true, "2007-03-21T00:00:00");
        _testSetString("dateTime", "2007-03-21", false, null);
        _testSetString("dateTime", "zzz", false, null);
        _testSetString("yearMonthDay", "2007-03-21T00:00:00", true, "2007-03-21");
        _testSetString("yearMonthDay", "2007-03-21", true, "2007-03-21");
        _testSetString("yearMonthDay", "2007-03", false, null);
        _testSetString("duration", "2007-03-21T00:00:00", false, null);
        _testSetString("duration", "PT1S", true, "PT1S");
        _testSetString("time", "2007-03-21T12:00:00", true, "12:00:00");
        _testSetString("time", "12:00:00", true, "12:00:00");
        _testSetString("time", "2007-03-21", false, null);
        _testSetString("yearMonth", "2007-03-21T00:00:00", true, "2007-03");
        _testSetString("yearMonth", "2007-03-21", true, "2007-03");
        _testSetString("yearMonth", "2007-03", true, "2007-03");
        _testSetString("yearMonth", "2007", false, null);
        _testSetString("year", "2007-03-21T00:00:00", true, "2007");
        _testSetString("year", "2007-03-21", true, "2007");
        _testSetString("year", "2007-03", true, "2007");
        _testSetString("year", "2007", true, "2007");
        _testSetString("year", "0", false, null);
        _testSetString("monthDay", "2007-03-21T00:00:00", true, "--03-21");
        _testSetString("monthDay", "2007-03-21", true, "--03-21");
        _testSetString("monthDay", "--03-21", true, "--03-21");
        _testSetString("monthDay", "2007-03", false, null);
        _testSetString("month", "2007-03-21T00:00:00", true, "--03");
        _testSetString("month", "2007-03-21", true, "--03");
        _testSetString("month", "2007-03", true, "--03");
        _testSetString("month", "--03-21", true, "--03");
        _testSetString("month", "--03", true, "--03");
        _testSetString("month", "---21", false, null);
        _testSetString("day", "2007-03-21T00:00:00", true, "---21");
        _testSetString("day", "2007-03-21", true, "---21");
        _testSetString("day", "--03-21", true, "---21");
        _testSetString("day", "---21", true, "---21");
        _testSetString("day", "2007-03", false, null);
    }

    public void testDuration() throws Exception
    {
        String uri = BASIC_URI;
        String name = "A";
        Type t = typeHelper.getType(uri, name);
        A a = (A)factory.create(t);
        assertNotNull(a);
        a.setDuration("PT0S");
        xmlHelper.save(a, BASIC_URI, "a", System.out);
        System.out.println();
        assertEquals("<bas:a xmlns:bas=\"http://sdo/test/basic\"><duration>PT0S</duration></bas:a>",
                     xmlHelper.save(a, BASIC_URI, "a"));

        String xml = "<bas:a xmlns:bas=\"http://sdo/test/basic\"><duration>P1DT2H</duration></bas:a>";
        XMLDocument doc = xmlHelper.load(xml);
        A root = (A)doc.getRootObject();
        String duration = root.getDuration();
        assertEquals("P1DT2H", duration);
    }

    public void testFloat() throws Exception
    {
        DataObject a = getBasicA("float0", "9.9999E125");
        System.out.println(xmlHelper.save(a, BASIC_URI, "a"));
        System.out.println(a.get("float0"));
        System.out.println(a.getFloat("float0"));
        assertEquals(Float.POSITIVE_INFINITY, a.get("float0"));
        assertEquals(Float.POSITIVE_INFINITY, a.getFloat("float0"));
        System.out.println(xmlHelper.save(a, BASIC_URI, "a"));
        String exp = getBasicAString("float0", "INF");
        assertEquals(exp, xmlHelper.save(a, BASIC_URI, "a"));
        a.setString("float0", "9.9999E125");
        System.out.println(xmlHelper.save(a, BASIC_URI, "a"));
        assertEquals(exp, xmlHelper.save(a, BASIC_URI, "a"));
        a.setFloat("float0", (float)2.33);
        DataGraphHelper.wrapWithDataGraph(a);
        ChangeSummary cs = a.getChangeSummary();
        cs.beginLogging();
        a.setFloat("float0", (float)5.55555);
        a.setFloat("double0", (float)5.55555);
        System.out.println(xmlHelper.save(a, BASIC_URI, "a"));
        Writer out = new StringWriter();
        saveDataObject(a.getRootObject(), SDO_URI, "datagraph", out);
        System.out.println(out.toString());
    }

    private int getHexDigit(char c)
    {
        switch (c)
        {
        case ('0'):
            return 0;
        case ('1'):
            return 1;
        case ('2'):
            return 2;
        case ('3'):
            return 3;
        case ('4'):
            return 4;
        case ('5'):
            return 5;
        case ('6'):
            return 6;
        case ('7'):
            return 7;
        case ('8'):
            return 8;
        case ('9'):
            return 9;
        case ('A'):
            return 10;
        case ('B'):
            return 11;
        case ('C'):
            return 12;
        case ('D'):
            return 13;
        case ('E'):
            return 14;
        case ('F'):
            return 15;
        default:
            return -1;
        }
    }

    private byte getByteFromHex(char[] hex, int start)
    {
        assert (hex.length > start + 1);
        int one = getHexDigit(hex[start + 1]);
        assert (one >= 0);
        int sixteen = getHexDigit(hex[start]);
        assert (sixteen >= 0);
        return (byte)(one + 16*sixteen);
    }

    public void testBinary() throws Exception
    {

        DataObject a0 = getBasicA("bytes2", "A9FD64E12C"); // hexBinary
        byte[] bytes2 = a0.getBytes("bytes2");
        assertNotNull(bytes2);
        assertEquals(5, bytes2.length);
        char[] hex = "A9FD64E12C".toCharArray();
        for (int i = 0; i < bytes2.length; i++)
        {
            int j = i*2;
            //System.out.println(bytes2[i]);
            byte b = getByteFromHex(hex, j);
            //System.out.println(b);
            assertEquals(b, bytes2[i]);
        }
        Type t = typeHelper.getType(BASIC_URI, "A");
        DataObject a1 = factory.create(t);
        /*
        byte[] ba1 = new byte[] { (byte)0, (byte)1, (byte)2, (byte)3,
                                  (byte)4, (byte)5, (byte)6, (byte)7 };
        a1.set("bytes2", ba1);
        System.out.println(xmlHelper.save(a, BASIC_URI, "a"));
        byte[] ba2 = new byte[] { (byte)8, (byte)9, (byte)10, (byte)11,
                                  (byte)12, (byte)13, (byte)14, (byte)15 };
        a1.set("bytes2", ba2);
        System.out.println(xmlHelper.save(a, BASIC_URI, "a"));
        */
        byte[] ba3 = new byte[] { (byte)64, (byte)65, (byte)66, (byte)67 };
        a1.set("bytes1", ba3);
        a1.set("bytes2", ba3);
        String xml = xmlHelper.save(a1, BASIC_URI, "a");
        System.out.println(xml);
        byte[] ba11 = a1.getBytes("bytes1");
        byte[] ba12 = a1.getBytes("bytes2");
        System.out.println(ba11 == ba12);
        System.out.println(ba11 == ba3);
        System.out.println(ba12 == ba3);
        compareBytes(ba3, ba11);
        compareBytes(ba3, ba12);
        System.out.println(XsTypeConverter.printBase64Binary(ba3));
        DataObject a2 = xmlHelper.load(xml).getRootObject();
        byte[] ba21 = a2.getBytes("bytes1");
        byte[] ba22 = a2.getBytes("bytes2");
        System.out.println(ba21 == ba22);
        System.out.println(ba21 == ba3);
        System.out.println(ba22 == ba3);
        compareBytes(ba3, ba21);
        compareBytes(ba3, ba22);
        byte[] ba4 = "Man".getBytes("US-ASCII");
        a1.set("bytes1", ba4);
        a1.unset("bytes2");
        xml = xmlHelper.save(a1, BASIC_URI, "a");
        System.out.println(xml);
        String exp = getBasicAString("bytes1", "TWFu");
        assertEquals(exp, xml);
    }

    public void testDate() throws Exception
    {
        Type t = typeHelper.getType(BASIC_URI, "A");
        DataObject a = factory.create(t);
        /*
        Date dt = new SimpleDateFormat("yyyy-MM-dd").parse("2007-03-21");
        //Date dt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2007-03-21 13:45:30");
        a.setDate("dateTime", dt);
        */
        a.setString("dateTime", "2007-03-21T00:00:00");
        //a.setString("dateTime", "zzz");
        String out = xmlHelper.save(a, BASIC_URI, "a");
        System.out.println(out);
    }

    public void testStrings()
    {
        Type t = typeHelper.getType(BASIC_URI, "A");
        DataObject a = factory.create(t);
        a.setString("nmtokens", "token1 token2 token3");
        System.out.println(a.get("nmtokens"));
        Object nmtokens = a.get("nmtokens");
        assertTrue(nmtokens instanceof List);
        List tokenList = (List)nmtokens;
        assertEquals(3, tokenList.size());
        assertEquals("token1", tokenList.get(0));
        assertEquals("token2", tokenList.get(1));
        assertEquals("token3", tokenList.get(2));
        String out = xmlHelper.save(a, BASIC_URI, "a");
        System.out.println(out);
        String exp = "<bas:a nmtokens=\"token1 token2 token3\" xmlns:bas=\"http://sdo/test/basic\"/>";
        assertEquals(exp, out);
    }

    public void testSetNull() throws Exception
    {
        DataObject a = getBasicA("integer0",
                                 //"1234567890");
                                 null);
        //assertEquals(new BigInteger("1234567890"), a.get("integer0"));
        assertNull(a.get("integer0"));
        assertTrue(a.isSet("integer0"));
        System.out.println(xmlHelper.save(a, BASIC_URI, "a"));
        String exp = getBasicAString("integer0", null);
        assertEquals(exp, xmlHelper.save(a, BASIC_URI, "a"));
    }

    private static final String simpleListType =
        "<bas:a xmlns:bas=\"http://sdo/test/basic0\">" +
            "<e>1 2 3</e>" +
        "</bas:a>";

    private static final String simpleUnionType1 =
        "<bas:a xmlns:bas=\"http://sdo/test/basic\">" +
            "<f>1 2 3</f>" +
        "</bas:a>";
    private static final String simpleUnionType2 =
        "<bas:a xmlns:bas=\"http://sdo/test/basic\">" +
            "<f>xxx</f>" +
        "</bas:a>";
    private static final String simpleUnionType3 =
        "<bas:a xmlns:bas=\"http://sdo/test/basic\">" +
            "<f2>xxx</f2>" +
            "<f2>B</f2>" +
        "</bas:a>";
    private static final String simpleUnionType4 =
        "<bas:a xmlns:bas=\"http://sdo/test/basic\">" +
            "<f>1 2 3</f>" +
            "<f2>xxx</f2>" +
            "<f2>B</f2>" +
        "</bas:a>";

    private static final String baselessIntType =
        "<bas:a xmlns:bas=\"http://sdo/test/basic\">" +
            "<g>99</g>" +
        "</bas:a>";

    /* test setting and getting an element of simple list type */
    public void testSetSimpleListType() throws Exception
    {
        System.out.println("testSetSimpleListType()");
        String uri = "http://sdo/test/basic0";
        String name = "A";
        Type t = typeHelper.getType(uri, name);
        assertNotNull(t);

        DataObject a = factory.create(t);
        assertNotNull(a);
        
        List eList = new ArrayList();
        eList.add(new Integer(1));
        eList.add(new Integer(2));
        eList.add(new Integer(3));
        a.set("e", eList);
        
        Property p = t.getProperty("e");
        assertNotNull(p);
        String e1 = a.getString("e");
        String e2 = a.getString(p);
        System.out.println(e1);
        System.out.println(e2);
        assertTrue(e1.equals(e2));
        Object e = a.get(p);
        assertTrue(e instanceof List);
        assertEquals(3, ((List)e).size());
        assertEquals(new Integer(1), ((List)e).get(0));
        assertEquals(new Integer(2), ((List)e).get(1));
        assertEquals(new Integer(3), ((List)e).get(2));
    }

    /* test setting an element of simple list type using setString */
    public void testSetSimpleListType2() throws Exception
    {
        System.out.println("testSetSimpleListType2()");
        String uri = "http://sdo/test/basic0";
        String name = "A";
        Type t = typeHelper.getType(uri, name);
        assertNotNull(t);

        DataObject a = factory.create(t);
        assertNotNull(a);
        try
        {
            a.setString("e", "1 2 3");
            fail("should have thrown a ClassCastException");
        }
        catch (Exception e)
        {
            assertTrue(e instanceof ClassCastException);
        }
        
        Property p = t.getProperty("e");
        assertNotNull(p);
        try
        {
            a.setString(p, "1 2 3");
            fail("should have thrown a ClassCastException");
        }
        catch (Exception e)
        {
            assertTrue(e instanceof ClassCastException);
        }
    }

    /* test setting an element of simple list type (list of string) 
       using setString */
    public void testSetSimpleListType3() throws Exception
    {
        System.out.println("testSetSimpleListType3()");
        String uri = "http://sdo/test/basic0";
        String name = "A";
        Type t = typeHelper.getType(uri, name);
        assertNotNull(t);
        DataObject a = factory.create(t);
        assertNotNull(a);

        Property p = t.getProperty("e2");
        assertNotNull(p);
        Type pt = p.getType();
        System.out.println(pt);
        System.out.println(pt.getInstanceClass());
        try
        {
            a.setString(p, "1 2 3");
            fail("should have thrown a ClassCastException");
        }
        catch (Exception e)
        {
            assertTrue(e instanceof ClassCastException);
        }
    }

    /* test setting and marshalling an element of simple list type */
    public void testMarshalSimpleListType() throws Exception
    {
        String uri = "http://sdo/test/basic0";
        String name = "A";
        Type t = typeHelper.getType(uri, name);
        assertNotNull(t);

        DataObject a = factory.create(t);
        assertNotNull(a);
        List eList = new ArrayList();
        eList.add(new Integer(1));
        eList.add(new Integer(2));
        eList.add(new Integer(3));
        a.set("e", eList);
        xmlHelper.save(a, uri, "a", System.out);
        System.out.println();
        String out = xmlHelper.save(a, uri, "a");
        assertEquals(simpleListType, out);
    }

    /* test unmarshalling an element of simple list type */
    public void testUnmarshalSimpleListType() throws Exception
    {
        //File f = getResourceFile("data", "basic2.xml"); // this instance contains "e"
        //InputStream in = new FileInputStream(f);
        String in = simpleListType;
        XMLDocument doc = xmlHelper.load(in);
        //in.close();
        DataObject root = doc.getRootObject();

        List eList = root.getList("e");
        assertNotNull(eList);
        assertEquals(3, eList.size());
        assertEquals(1, ((Integer)eList.get(0)).intValue());
        assertEquals(2, ((Integer)eList.get(1)).intValue());
        assertEquals(3, ((Integer)eList.get(2)).intValue());
    }

    /* test getting an element of simple list type */
    public void testGetSimpleListType() throws Exception
    {
        String in = simpleListType;
        XMLDocument doc = xmlHelper.load(in);
        DataObject root = doc.getRootObject();

        Object e = root.get("e");
        assertNotNull(e);
        assertTrue(e instanceof List);
        List eList = (List)e;
        assertEquals(3, eList.size());
        assertEquals(1, ((Integer)eList.get(0)).intValue());
        assertEquals(2, ((Integer)eList.get(1)).intValue());
        assertEquals(3, ((Integer)eList.get(2)).intValue());
        // list is not live?
        eList.add(new Integer(4));
        assertEquals(4, eList.size());
        assertEquals(4, ((Integer)eList.get(3)).intValue());
        List e2 = (List)root.get("e");
        System.out.println("e2 size: " + e2.size());
        for (int i = 0; i < e2.size(); i++)
            System.out.println(e2.get(i));
        System.out.println(root.get("e"));
        System.out.println(root.get("e[1]")); // null
        assertEquals(3, e2.size());
    }

    /* test unmarshalling an element of simple union type -
       case 1: member types map to the same SDO type */
    public void testUnmarshalSimpleUnionType1() throws Exception
    {
        String in = simpleUnionType3;
        XMLDocument doc = xmlHelper.load(in);
        DataObject root = doc.getRootObject();
        Property p = root.getInstanceProperty("f2");
        Type t = p.getType();
        assertEquals(java.lang.String.class, t.getInstanceClass());

        Object f = root.get("f2[1]");
        assertNotNull(f);
        assertEquals("xxx", f);
        f = root.get("f2[2]");
        assertNotNull(f);
        assertEquals("B", f);
    }

    /* test unmarshalling an element of simple union type -
       case 2: member types map to different SDO types */
    public void testUnmarshalSimpleUnionType2() throws Exception
    {
        String in = simpleUnionType1;
        XMLDocument doc = xmlHelper.load(in);
        DataObject root = doc.getRootObject();
        Property p = root.getInstanceProperty("f");
        Type t = p.getType();
        assertEquals(java.lang.Object.class, t.getInstanceClass());

        Object f = root.get("f");
        assertNotNull(f);
        System.out.println(f);
        assertEquals("1 2 3", f);

        in = simpleUnionType2;
        doc = xmlHelper.load(in);
        root = doc.getRootObject();

        f = root.get("f");
        assertNotNull(f);
        System.out.println(f);
        assertEquals("xxx", f);
    }

    /* test setting a property of simple union type and marshalling */
    public void testSetAndMarshalSimpleUnionType()
    {
        Type t = typeHelper.getType(BASIC_URI, "A");
        DataObject a = factory.create(t);
        a.set("f", "1 2 3");
        List f2 = a.getList("f2");
        assertNotNull(f2);
        assertEquals(0, f2.size());
        f2.add("xxx");
        f2.add("B");
        String s = xmlHelper.save(a, BASIC_URI, "a");
        System.out.println(s);
        assertEquals(simpleUnionType4, s);
    }

    /* test setting a data type property of int instance class and no base type */
    public void testSetBaselessIntType()
    {
        Type t = typeHelper.getType(BASIC_URI, "A");
        DataObject a = factory.create(t);
        assertFalse(a.isSet("g"));
        a.setInt("g", 99);
        String s = xmlHelper.save(a, BASIC_URI, "a");
        System.out.println(s);
        assertEquals(baselessIntType, s);
        DataObject a2 = xmlHelper.load(baselessIntType).getRootObject();
        assertNotNull(a2);
        Object g = a2.get("g");
        assertTrue(g instanceof Integer);
        assertEquals(99, ((Integer)g).intValue());
    }

    private static final String anyTypeWithSimpleContent =
        "    <any1 xsi:type=\"xs:int\" " +
        "xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" " +
        "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">100</any1>";

    private static final String anyTypeWithSimpleContent2 =
        "    <any1>100</any1>";

    //private static final String anyTypeWithSimpleContent3 =
    //    "    <any1 xsi:type=\"xs:dateTime\" " +
    //    "xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" " +
    //    "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">2001-01-01T12:00:00</any1>";

    private static final String anySimpleType =
        "    <any2 xsi:type=\"xs:int\" " +
        "xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" " +
        "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">3</any2>";

    private static final String anySimpleType2 =
        "    <any2>3</any2>";

    private static final String anySimpleType3 =
        "    <any2 xsi:type=\"xs:dateTime\" " +
        "xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" " +
        "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">2001-01-01T12:00:00</any2>";

    private static final String anySimpleType3b =
        "    <any2>2001-01-01T12:00:00</any2>";

    private static final String anyTypeWithComplexContent = 
        "    <any1 xsi:type=\"bas1:B\" b0=\"true\" " +
        "xmlns:bas1=\"http://sdo/test/basic\" " +
        "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" + newline +
        "        <b1>100</b1>" + newline +
        "        <b2>abc</b2>" + newline +
        "    </any1>";

    private static final String anyType1 = 
        "<bas:a xmlns:bas=\"http://sdo/test/basic0\">" + newline +
        anyTypeWithSimpleContent + newline +
        "</bas:a>";

    private static final String anyType1b = 
        "<bas:a xmlns:bas=\"http://sdo/test/basic0\">" + newline +
        anyTypeWithSimpleContent2 + newline +
        "</bas:a>";

    private static final String anyType1c = 
        "<bas:a xmlns:bas=\"http://sdo/test/basic0\">" + newline +
        anyTypeWithComplexContent + newline +
        "</bas:a>";

    private static final String anyType2 = 
        "<bas:a xmlns:bas=\"http://sdo/test/basic0\">" + newline +
        anySimpleType + newline +
        "</bas:a>";

    private static final String anyType2b = 
        "<bas:a xmlns:bas=\"http://sdo/test/basic0\">" + newline +
        anySimpleType2 + newline +
        "</bas:a>";

    private static final String anyType2c = 
        "<bas:a xmlns:bas=\"http://sdo/test/basic0\">" + newline +
        anySimpleType3 + newline +
        "</bas:a>";

    private static final String anyType2d = 
        "<bas:a xmlns:bas=\"http://sdo/test/basic0\">" + newline +
        anySimpleType3b + newline +
        "</bas:a>";

    private static final String anyType3 = 
        "<bas:a xmlns:bas=\"http://sdo/test/basic0\">" + newline +
        anyTypeWithSimpleContent + newline +
        anySimpleType + newline +
        "</bas:a>";

    private static final String anyType4 = 
        "<bas:a xmlns:bas=\"http://sdo/test/basic0\">" + newline +
        anyTypeWithComplexContent + newline +
        anySimpleType + newline +
        "</bas:a>";

    private static final String anyURIAndQName1 =
        "<bas:a xmlns:bas=\"http://sdo/test/basic0\">" + newline +
        "    <uri1>http://www.w3.org/2001/XMLSchema#decimal</uri1>" + newline +
        "    <uri2 xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">xs:decimal</uri2>" + newline +
        "</bas:a>";

    private static final String anyURIAndQName2 =
        "<bas:a xmlns:bas=\"http://sdo/test/basic0\">" + newline +
        "    <uri1>http://www.example.com/test#decimal</uri1>" + newline +
        "    <uri2 xmlns:xs=\"http://www.example.com/test\">xs:decimal</uri2>" + newline +
        "</bas:a>";

    private static final String anyURIAndQName3 =
        "<bas:a xmlns:bas=\"http://sdo/test/basic0\">" + newline +
        "    <uri1>http://www.example.com/test#decimal</uri1>" + newline +
        "    <uri2 xmlns:test=\"http://www.example.com/test#decimal\">test:place</uri2>" + newline +
        "</bas:a>";

    private static final String QName1 =
        "<bas:a xmlns:bas=\"http://sdo/test/basic0\">" + newline +
        "    <uri2 xmlns:test=\"http://www.example.com/test#foo\">test:bar</uri2>" + newline +
        "</bas:a>";

    private static final String QName2 =
        "<bas:a xmlns:bas=\"http://sdo/test/basic0\">" + newline +
        "    <uri2 xmlns:test=\"http://www.example.com/test#foo#bar\">test:zzz</uri2>" + newline +
        "</bas:a>";

    private static final String QName3 =
        "<bas:a xmlns:bas=\"http://sdo/test/basic0\">" + newline +
        "    <uri2>bar</uri2>" + newline +
        "</bas:a>";

    private String saveToString(DataObject dobj, String uri, String name)
        throws Exception
    {
        StringWriter out = new StringWriter();
        saveDataObject(dobj, uri, name, out);
        String s = out.toString();
        out.close();
        return s;
    }

    public void testUnmarshalAnyType() throws Exception
    {
        System.out.println("testUnmarshalAnyType()");
        String uri = "http://sdo/test/basic0";
        String name = "A";
        Type t = typeHelper.getType(uri, name);
        assertNotNull(t);
        Property p = t.getProperty("any1"); // anyType
        _testProperty(p, "DataObject", DataObject.class);
        System.out.println("any1 is of type DataObject");
        System.out.println(anyType1);
        XMLDocument doc = xmlHelper.load(anyType1);
        assertEquals(uri, doc.getRootElementURI());
        assertEquals("a", doc.getRootElementName());
        DataObject root = doc.getRootObject();
        assertNull(root.getContainer());
        assertNull(root.getContainmentProperty());
        assertEquals("a", ((DataObjectXML)root).getContainmentPropertyXML().getName());
        assertEquals(uri, ((DataObjectXML)root).getContainmentPropertyXML().getContainingType().getURI());

        // When an element of anyType is used with xsi:type specifying a
        // simple type, a wrapper DataObject must be used 
        // with a property named "value" that is set to the wrapped type.
        Object any = root.get("any1");
        assertTrue(any instanceof DataObject);
        DataObject any1 = (DataObject)any;
        // any1 contains a "value" property of type SDO Int 
        // (mapped to by the xsi:type, xs:int)
        Type T_INT = typeHelper.getType("commonj.sdo", "Int");
        Property valueP = any1.getInstanceProperty("value");
        assertNotNull(valueP);
        assertEquals(T_INT, valueP.getType());
        // the value itself in an Integer
        Object value = any1.get("value");
        assertTrue(value instanceof Integer);
        assertEquals(new Integer(100), value);
        assertEquals(100, any1.getInt("value"));

        // round-trippable
        String xml = saveToString(root, uri, "a");
        assertEquals(anyType1, xml);
    }

    public void testUnmarshalAnyType2() throws Exception
    {
        System.out.println("testUnmarshalAnyType2()");
        System.out.println(anyType1b);
        XMLDocument doc = xmlHelper.load(anyType1b);
        assertEquals("http://sdo/test/basic0", doc.getRootElementURI());
        assertEquals("a", doc.getRootElementName());
        DataObject root = doc.getRootObject();

        // When an element of anyType contains a simple type
        // (with no xsi:type specification), a wrapper DataObject is used 
        // with a property named "value" set to a String.
        Object any = root.get("any1");
        assertTrue(any instanceof DataObject);
        DataObject any1 = (DataObject)any;
        Property valueP = any1.getInstanceProperty("value");
        assertNotNull(valueP);
        // the type of "value" is Object
        Type T_OBJECT = typeHelper.getType("commonj.sdo", "Object");
        assertEquals(T_OBJECT, valueP.getType());
        // the value itself is a String
        Object value = any1.get("value");
        assertTrue(value instanceof String);
        assertEquals("100", value);
        assertEquals(100, any1.getInt("value"));

        // round-trippable
        String xml = saveToString(root, "http://sdo/test/basic0", "a");
        assertEquals(anyType1b, xml);
    }

    public void testUnmarshalAnyType3() throws Exception
    {
        System.out.println("testUnmarshalAnyType3()");
        System.out.println(anyType1c);
        XMLDocument doc = xmlHelper.load(anyType1c);
        assertEquals("http://sdo/test/basic0", doc.getRootElementURI());
        assertEquals("a", doc.getRootElementName());
        DataObject root = doc.getRootObject();
        assertNull(root.getContainer());
        assertNull(root.getContainmentProperty());
        assertEquals("a", ((DataObjectXML)root).getContainmentPropertyXML().getName());
        assertEquals("http://sdo/test/basic0", ((DataObjectXML)root).getContainmentPropertyXML().getContainingType().getURI());

        Object any = root.get("any1");
        assertTrue(any instanceof DataObject);
        DataObject any1 = (DataObject)any;
        Object b0 = any1.get("b0");
        assertTrue(b0 instanceof Boolean);
        assertEquals(Boolean.TRUE, b0);
        Object b1 = any1.get("b1");
        assertTrue(b1 instanceof Integer);
        assertEquals(new Integer(100), b1);
        Object b2 = any1.get("b2");
        assertTrue(b2 instanceof String);
        assertEquals("abc", b2);

        // round-trippable
        String xml = saveToString(root, "http://sdo/test/basic0", "a");
        assertEquals(anyType1c, xml);
    }

    public void testUnmarshalAnySimpleType() throws Exception
    {
        System.out.println("testUnmarshalAnySimpleType()");
        String uri = "http://sdo/test/basic0";
        String name = "A";
        Type t = typeHelper.getType(uri, name);
        assertNotNull(t);
        Property p = t.getProperty("any2"); // anyType
        _testProperty(p, "Object", Object.class);
        System.out.println("any2 is of type Object");
        System.out.println(anyType2);
        XMLDocument doc = xmlHelper.load(anyType2);
        DataObject root = doc.getRootObject();

        Object any2 = root.get("any2");
        assertTrue(any2 instanceof Integer);
        assertEquals(new Integer(3), any2);

        // round-trippable
        String xml = saveToString(root, uri, "a");
        assertEquals(anyType2, xml);
    }

    public void testUnmarshalAnySimpleType2() throws Exception
    {
        System.out.println("testUnmarshalAnySimpleType2()");
        System.out.println(anyType2b);
        XMLDocument doc = xmlHelper.load(anyType2b);
        DataObject root = doc.getRootObject();

        Object any2 = root.get("any2");
        assertTrue(any2 instanceof String);
        assertEquals("3", any2);

        // round-trippable
        String xml = saveToString(root, "http://sdo/test/basic0", "a");
        assertEquals(anyType2b, xml);
    }

    public void testUnmarshalAnySimpleType3() throws Exception
    {
        System.out.println("testUnmarshalAnySimpleType3()");
        System.out.println(anyType2c);
        XMLDocument doc = xmlHelper.load(anyType2c);
        DataObject root = doc.getRootObject();

        Object any2 = root.get("any2");
        assertTrue(any2 instanceof String);
        assertEquals("2001-01-01T12:00:00", any2);

        // not round-trippable
        String xml = saveToString(root, "http://sdo/test/basic0", "a");
        assertEquals(anyType2d, xml);
    }

    /* set an anyType element to a datatype value and marshal */
    public void testMarshalAnyType1() throws Exception
    {
        System.out.println("testMarshalAnyType1()");
        String uri = "http://sdo/test/basic0";
        String name = "A";
        Type tA = typeHelper.getType(uri, name);
        assertNotNull(tA);

        DataObject a = factory.create(tA);
        assertNotNull(a);
        a.set("any1", 100);
        a.set("any2", 3);
        xmlHelper.save(a, uri, "a", System.out);
        System.out.println();
        StringWriter out = new StringWriter();
        xmlHelper.save(xmlHelper.createDocument(a, uri, "a"), 
                       out, new Options().setSavePrettyPrint());
        assertEquals(XML_HEADER + newline + anyType3, out.toString());
    }

    /* set an anyType element to a data object and marshal */
    public void testMarshalAnyType2() throws Exception
    {
        System.out.println("testMarshalAnyType2()");
        String uri = "http://sdo/test/basic0";
        String name = "A";
        Type tA = typeHelper.getType(uri, name);
        assertNotNull(tA);
        Type tB = typeHelper.getType(BASIC_URI, "B");
        assertNotNull(tB);

        DataObject a = factory.create(tA);
        assertNotNull(a);
        DataObject b = factory.create(tB);
        assertNotNull(b);
        b.set("b1", 100);
        b.set("b2", "abc");
        b.set("b0", true);
        a.set("any1", b);
        a.set("any2", 3);
        xmlHelper.save((DataObject)b, BASIC_URI, "b", System.out);
        System.out.println();
        xmlHelper.save(a, uri, "a", System.out);
        System.out.println();
        StringWriter out = new StringWriter();
        xmlHelper.save(xmlHelper.createDocument(a, uri, "a"), 
                       out, new Options().setSavePrettyPrint());
        assertEquals(XML_HEADER + newline + anyType4, out.toString());
    }

    private void _testGetAndSetQName(String xml_in,
                                     String uri, String qname,
                                     String xml_out)
        throws Exception
    {
        System.out.println("_testGetAndSetQName()");
        System.out.println(xml_in);
        XMLDocument doc = xmlHelper.load(xml_in);
        DataObject root = doc.getRootObject();
        assertEquals(uri, root.get("uri1"));
        assertEquals(qname, root.get("uri2"));

        String ns = "http://sdo/test/basic0";
        Type tA = typeHelper.getType(ns, "A");
        assertNotNull(tA);
        DataObject a = factory.create(tA);
        assertNotNull(a);
        a.set("uri1", uri);
        a.set("uri2", qname);
        xmlHelper.save(a, ns, "a", System.out);
        System.out.println();
        StringWriter out = new StringWriter();
        xmlHelper.save(xmlHelper.createDocument(a, ns, "a"), 
                       out, new Options().setSavePrettyPrint());
        assertEquals(XML_HEADER + newline + xml_out, out.toString());
    }

    public void testGetAndSetQName() throws Exception
    {
        _testGetAndSetQName(anyURIAndQName1,
                            "http://www.w3.org/2001/XMLSchema#decimal",
                            "http://www.w3.org/2001/XMLSchema#decimal",
                            anyURIAndQName1);
        _testGetAndSetQName(anyURIAndQName2,
                            "http://www.example.com/test#decimal",
                            "http://www.example.com/test#decimal",
                            anyURIAndQName2.replaceAll("xs", "test"));
        _testGetAndSetQName(anyURIAndQName3,
                            "http://www.example.com/test#decimal",
                            "http://www.example.com/test#decimal#place",
                            anyURIAndQName3);
    }

    // more tests for unmarshalling QNames
    public void testUnmarshalQName()
    {
        System.out.println("testUnmarshalQName()");
        XMLDocument doc = xmlHelper.load(QName1);
        DataObject root = doc.getRootObject();
        assertEquals("http://www.example.com/test#foo#bar", root.get("uri2"));
        doc = xmlHelper.load(QName2);
        root = doc.getRootObject();
        assertEquals("http://www.example.com/test#foo#bar#zzz", root.get("uri2"));
        doc = xmlHelper.load(QName3);
        root = doc.getRootObject();
        assertEquals("#bar", root.get("uri2"));
    }

    public void testSetAndMarshalQName()
    {
        String ns = "http://sdo/test/basic0";
        Type tA = typeHelper.getType(ns, "A");
        assertNotNull(tA);
        DataObject a1 = factory.create(tA);
        assertNotNull(a1);
        String qname = "ld:default-type-test#AttributeTypes";
        a1.set("uri2", qname);
        String xml1 = xmlHelper.save(a1, ns, "a");
        System.out.println(xml1);
        XMLDocument doc = xmlHelper.load(xml1);
        DataObject a2 = doc.getRootObject();
        assertEquals(qname, a2.get("uri2"));
        String xml2 = xmlHelper.save(a2, ns, "a");
        System.out.println(xml2);
    }

    private static final String simpleStringTypeRoot =
        "<bas:c xmlns:bas=\"http://sdo/test/basic\">aaa</bas:c>";
    private static final String simpleListTypeRoot =
        "<bas:e xmlns:bas=\"http://sdo/test/basic\">1 2 3</bas:e>";

    /* test unmarshalling root element of simple type */
    public void testUnmarshalSimpleTypeRootElement()
    {
        System.out.println("testUnmarshalSimpleTypeRootElement()");
        System.out.println(simpleStringTypeRoot);
        XMLDocument doc = xmlHelper.load(simpleStringTypeRoot);
        assertEquals("http://sdo/test/basic", doc.getRootElementURI());
        assertEquals("c", doc.getRootElementName());
        DataObject root = doc.getRootObject();
        assertNotNull(root);
        System.out.println(root.getType()); //ValueType@http://www.bea.com/sdo/types
        Object value = root.get("value");
        assertTrue(value instanceof String);
        assertEquals("aaa", value);

        doc = xmlHelper.load(simpleListTypeRoot);
        assertEquals("http://sdo/test/basic", doc.getRootElementURI());
        assertEquals("e", doc.getRootElementName());
        root = doc.getRootObject();
        assertNotNull(root);
        value = root.get("value");
        assertTrue(value instanceof List);
        List e = (List)value;
        assertEquals(new Integer(1), e.get(0));
        assertEquals(new Integer(2), e.get(1));
        assertEquals(new Integer(3), e.get(2));
    }

    /* test handling of character with code point 0 */
    public void testCharacter0()
    {
        System.out.println("testCharacter0");
        DataObject h = factory.create("http://sdo/test/basic", "H");
        char defaultValue = h.getChar("character");
        h.setChar("character", defaultValue);
        String xml = xmlHelper.save(h, "http://sdo/test/basic", "h");
        System.out.println(xml);
        assertTrue(xml.indexOf("character></") > 0);
        h = xmlHelper.load(xml).getRootObject();
        assertEquals(0, h.getChar("character"));
        assertTrue(h.isSet("character"));
    }

    /* test DataFactory with simple types = wrapper types */
    public void testDataFactoryWrapperTypes()
    {
        System.out.println("testDataFactoryWrapperTypes");
        DataObject dobj = factory.create("commonj.sdo", "Int");
        List instanceProperties = dobj.getInstanceProperties();
        assertEquals(1, instanceProperties.size());
        assertEquals("value", ((Property) instanceProperties.get(0)).getName());
        DataObject a = factory.create("http://sdo/test/basic", "A");
        dobj.set("value", 6);
        a.set("any0", dobj);
        String xml = xmlHelper.save(a, "http://sdo/test/basic", "a");
        System.out.println(xml);
        assertTrue(xml.indexOf("any0 xsi:type=\"xs:int") > 0);
    }
}
