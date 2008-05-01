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
import java.util.*;
import java.math.BigDecimal;
import java.math.BigInteger;

import javax.sdo.*;
import javax.sdo.helper.*;

import davos.sdo.Options;

import junit.framework.Test;
import junit.framework.TestSuite;
import common.BaseTest;

/**
 * @author Wing Yew Poon
 */
public class SDOPathTest extends DataObjectTest //BaseTest
{
    public SDOPathTest(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        
        TestSuite suite = new TestSuite();
        suite.addTest(new SDOPathTest("testW"));
        suite.addTest(new SDOPathTest("testX"));
        suite.addTest(new SDOPathTest("testY"));
        suite.addTest(new SDOPathTest("testZ"));
        suite.addTest(new SDOPathTest("testZ2"));
        suite.addTest(new SDOPathTest("testConversionWithPositionalPath"));
        suite.addTest(new SDOPathTest("testBooleanConversion"));
        suite.addTest(new SDOPathTest("testByteConversion"));
        suite.addTest(new SDOPathTest("testCharacterConversion"));
        suite.addTest(new SDOPathTest("testDoubleConversion"));
        suite.addTest(new SDOPathTest("testFloatConversion"));
        suite.addTest(new SDOPathTest("testIntConversion"));
        suite.addTest(new SDOPathTest("testLongConversion"));
        suite.addTest(new SDOPathTest("testShortConversion"));
        suite.addTest(new SDOPathTest("testBytesConversion"));
        suite.addTest(new SDOPathTest("testDecimalConversion"));
        suite.addTest(new SDOPathTest("testIntegerConversion"));
        
        // or
        //TestSuite suite = new TestSuite(SDOPathTest.class);
        return suite;
    }

    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(suite());
    }

    private static DataFactory factory = context.getDataFactory();
    private static TypeHelper typeHelper = context.getTypeHelper();
    private static XMLHelper xmlHelper = context.getXMLHelper();

    /*
      step = property and step = @property for 
        property = element and property = attribute
      step = property[index_from_1]
      step = propety.index_from_0
      step = reference[attribute=value]
    */
    public void testW() throws Exception
    {
        File f = getResourceFile("checkin", "employees.xml");
        InputStream in = new FileInputStream(f);
        XMLDocument doc = xmlHelper.load(in);
        DataObject employees = doc.getRootObject();
        DataObject employee1 = employees.getDataObject("employee[1]");
        assertNotNull(employee1);
        assertEquals(employee1, employees.get("employee.0"));
        assertEquals("Fred Jones", employee1.get("name"));
        // '@' is ignored; it doesn't matter if the property is an attribute or element
        assertEquals("Fred Jones", employee1.get("@name"));
        assertEquals("900 Aurora Ave.", employee1.get("address[1]/street"));
        assertEquals("900 Aurora Ave.", employee1.getDataObject("address[1]").get("street"));
        assertEquals("900 Aurora Ave.", employee1.getString("address[1]/street"));
        assertEquals(new BigInteger("98115"), employee1.get("address[1]/zip"));
        assertEquals(new BigInteger("98115"), employee1.getBigInteger("address[1]/zip"));
        assertEquals(98115, employee1.getInt("address[1]/zip"));
        // "value" property for simple content
        assertEquals("(425)555-5665", employee1.get("phone[1]/value"));
        assertEquals("(425)555-5665", employee1.get("phone[1]/@value"));
        assertEquals("(425)555-5665", employee1.get("phone[location='work']/value"));
        assertEquals("work", employee1.get("phone[1]/location"));
        // '@' is ignored; it doesn't matter if the property is an attribute or element
        assertEquals("work", employee1.get("phone[1]/@location"));
        DataObject employee2 = employees.getDataObject("employee[2]");
        assertNotNull(employee2);
        DataObject employee3 = employees.getDataObject("employee[3]");
        assertNotNull(employee3);
        // index out of range
        DataObject employee4 = employees.getDataObject("employee[4]");
        assertNull(employee4);
    }

    public void testX() throws Exception
    {
        DataObject company = 
            factory.create(typeHelper.getType("company.xsd", "CompanyType"));
        company.set("name", "Acme");
        DataObject prodDept = company.createDataObject("departments");
        prodDept.set("name", "Product Development");
        prodDept.set("number", 7);
        DataObject emp1 = prodDept.createDataObject("employees");
        emp1.setString("SN", "E1");
        emp1.set("name", "Alpha");
        emp1.setBoolean("manager", true);
        /*
        String xml = xmlHelper.save(company, "company.xsd", "company");
        System.out.println(xml);
        */
        assertEquals("Alpha", company.get("departments[1]/employees[1]/name"));
        assertEquals("Alpha", company.get("departments[1]/employees[manager=true]/name"));
        assertEquals("Alpha", company.get("departments[1]/employees[SN='E1']/name"));
        assertEquals("Alpha", company.get("departments.0/employees.0/name"));
        assertEquals("Alpha", company.get("departments[1]/employees.0/name"));
        assertEquals("Alpha", company.get("departments.0/employees[1]/name"));
        DataObject dept = company.getDataObject("departments[1]");
        assertNotNull(dept);
        assertEquals("Alpha", dept.get("employees[SN='E1']/name"));
        DataObject emp = dept.getDataObject("employees[SN='E1']");
        //DataObject emp = dept.getDataObject("employees[1]");
        assertNotNull(emp);
        assertEquals("Alpha", emp.getString("name"));
        emp = (DataObject)company.get("departments[1]/employees[1]");
        assertNotNull(emp);
        assertEquals("Alpha", emp.getString("name"));
        emp = (DataObject)company.get("departments[1]/employees[manager=true]");
        assertNotNull(emp);
        assertEquals("Alpha", emp.getString("name"));
        emp = (DataObject)company.get("departments[1]/employees[SN='E1']");
        assertNotNull(emp);
        assertEquals("Alpha", emp.getString("name"));
    }

    /*
      step
      /step
      2 steps
      3 or more steps
      ..
    */
    public void testY() throws Exception
    {
        File f = getResourceFile("checkin", "company.xml");
        InputStream in = new FileInputStream(f);
        XMLDocument doc = xmlHelper.load(in);
        DataObject company = doc.getRootObject();
        assertEquals("ACME", company.get("/name"));
        assertEquals("ACME", company.get("name"));
        assertEquals("ACME", company.get("@name"));
        DataObject department = company.getDataObject("departments[1]");
        assertNotNull(department);
        assertEquals(department, company.getDataObject("/departments[1]"));
        assertEquals(department, company.getDataObject("departments.0"));
        assertEquals(department, company.getDataObject("@departments[1]"));
        assertEquals(department, company.getDataObject("@departments.0"));
        assertEquals(department, company.getDataObject("departments[number='123']"));
        assertEquals(department, company.getDataObject("departments[number=\"123\"]"));
        assertEquals(department, company.getDataObject("departments[location='NY']"));
        assertEquals(department, company.getDataObject("departments[location=\"NY\"]"));
        assertEquals("ACME", department.get("/name")); // name of company
        assertEquals("ACME", department.get("/@name"));
        assertEquals("ACME", department.get("../name"));
        assertEquals("ACME", department.get("../@name"));
        assertEquals("AdvancedTechnologies", department.get("name")); // name of department
        assertEquals("AdvancedTechnologies", department.get("@name"));
        assertEquals("Mary Smith", company.get("/departments[1]/employees[1]/../employees[manager=true]/name"));
        DataObject manager = company.getDataObject("/departments[1]/employees[1]/../employees[manager=true]");
        assertNotNull(manager);
        assertEquals("ACME", manager.get("/name")); // name of company
        assertEquals("Mary Smith", manager.get("name")); // name of manager
    }

    // test set/isSet/unset using step = property[index]
    public void testZ()
    {
        Type stringType = typeHelper.getType("commonj.sdo", "String");

        DataObject zType = factory.create("commonj.sdo", "Type");
        zType.set("uri", "example.com/test");
        zType.set("name", "Z");

        DataObject zProperty = zType.createDataObject("property");
        zProperty.set("name", "z");
        zProperty.set("type", stringType);
        zProperty.set("many", true);

        Type t = typeHelper.define(zType);
        DataObject dobj = factory.create(t);

        List list = dobj.getList("z");
        list.add("first");
        list.add("second");
        dobj.set("z[2]", "third");

        assertEquals("first", dobj.get("z[1]"));
        assertEquals("third", dobj.get("z[2]"));
        assertNull(dobj.get("z[3]"));
        assertTrue(dobj.isSet("z[1]"));
        assertTrue(dobj.isSet("z[2]"));
        assertFalse(dobj.isSet("z[3]"));
        dobj.unset("z[1]");
        assertEquals("third", dobj.get("z[1]"));
        assertNull(dobj.get("z[2]"));
    }

    /* test set using paths containing 
      step = property[index_from_1]
      step = propety.index_from_0
      step = reference[attribute=value]
    */
    public void testZ2() throws Exception
    {
        File f = getResourceFile("checkin", "employees.xml");
        InputStream in = new FileInputStream(f);
        XMLDocument doc = xmlHelper.load(in);
        DataObject employees = doc.getRootObject();
        DataObject employee2 = employees.getDataObject("employee[2]");
        assertEquals("Sally Smith", employee2.get("name"));
        DataObject address2_1 = employee2.getDataObject("address[1]");
        employees.set("/employee.1/address[location=\"home\"]/street",
                      "1215 Elm Street");
        assertEquals("1215 Elm Street", address2_1.get("street"));
        employees.set("/employee[2]/address.0/street",
                      "1235 Elm Street");
        assertEquals("1235 Elm Street", address2_1.get("street"));
        DataObject phone2_1 = employee2.getDataObject("phone[1]");
        DataObject phone2_2 = employee2.getDataObject("phone.1");
        DataObject phone2_3 = employee2.getDataObject("phone[3]");
        employees.set("employee[2]/phone[1]/value", "(503)555-3858");
        assertEquals("(503)555-3858", phone2_1.get("value"));
        employees.set("employee[2]/phone.1/value", "(503)555-6958");
        assertEquals("(503)555-6958", phone2_2.get("value"));
        employees.set("employee.1/phone.2/value", "(503)555-5153");
        assertEquals("(503)555-5153", phone2_3.get("value"));
    }

    private static final String SDOCS4_XML1 =
    "<sdoc:C xmlns:sdoc=\"http://aldsp.bea.com/test/sdocs4\">" +
    "<n>xxx</n>" +
    "<i>123</i>" +
    "<t>9997777</t>" +
    "<t>9998787</t>" +
    "<t>9998080</t>" +
    "</sdoc:C>";
    private static final String SDOCS4_XML2 =
    "<sdoc:C xmlns:sdoc=\"http://aldsp.bea.com/test/sdocs4\">" + newline +
    "    <n>xxx</n>" + newline +
    "    <i>123</i>" + newline +
    "    <t>9997777</t>" + newline +
    "    <t>8484444</t>" + newline +
    "    <t>5551234</t>" + newline +
    "</sdoc:C>";

    // test set<T> using step = property[index]
    public void testConversionWithPositionalPath() throws Exception
    {
        DataObject dobj = xmlHelper.load(SDOCS4_XML1).getRootObject();
        dobj.setInt("t[2]", 8484444);
        dobj.setString("t[3]", "5551234");
        //dobj.set("t[3]", new BigInteger("5551234"));
        //dobj.setBigInteger("t[3]", new BigInteger("5551234"));
        Object value1 = dobj.getList("t").get(1);
        assertTrue(value1 instanceof BigInteger);
        Object value2 = dobj.getList("t").get(2);
        assertTrue(value2 instanceof BigInteger);
        Writer out = new StringWriter();
        XMLDocument doc = xmlHelper.createDocument(dobj.getRootObject(), 
                                                   "http://aldsp.bea.com/test/sdocs4", "C");
        doc.setXMLDeclaration(false);
        xmlHelper.save(doc, out, new Options().setSavePrettyPrint());
        String s = out.toString();
        System.out.println(s);
        assertEquals(SDOCS4_XML2, s);
    }

    public void testBooleanConversion()
    {
        System.out.println("testBooleanConversion()");
        DataObject dobj = factory.create(basic_t2);
        List booleanList = dobj.getList("boolean");
        List stringList = dobj.getList("string");
        booleanList.add(true);
        booleanList.add(false);
        stringList.add("true");
        stringList.add("false");
        assertEquals("true", dobj.getString("boolean[1]"));
        assertEquals("false", dobj.getString("boolean[2]"));
        assertEquals("true", dobj.get("string[1]"));
        assertEquals("false", dobj.get("string[2]"));
        dobj.setBoolean("string[1]", false);
        dobj.setBoolean("string[2]", true);
        assertEquals("false", dobj.get("string[1]"));
        assertEquals("true", dobj.get("string[2]"));
    }

    private DataObject createTestDO()
    {
        DataObject dobj = factory.create(basic_t2);
        List byteList = dobj.getList("byte");
        byteList.add((byte)0);
        byteList.add((byte)0);
        byteList.add((byte)0);
        List doubleList = dobj.getList("double");
        doubleList.add(0.0);
        doubleList.add(0.0);
        doubleList.add(0.0);
        List floatList = dobj.getList("float");
        floatList.add((float)0.0);
        floatList.add((float)0.0);
        floatList.add((float)0.0);
        List intList = dobj.getList("int");
        intList.add(0);
        intList.add(0);
        intList.add(0);
        List longList = dobj.getList("long");
        longList.add((long)0);
        longList.add((long)0);
        longList.add((long)0);
        List shortList = dobj.getList("short");
        shortList.add((short)0);
        shortList.add((short)0);
        shortList.add((short)0);
        List stringList = dobj.getList("string");
        stringList.add("");
        stringList.add("");
        stringList.add("");
        List decimalList = dobj.getList("decimal");
        decimalList.add(new BigDecimal("0.0"));
        decimalList.add(new BigDecimal("0.0"));
        decimalList.add(new BigDecimal("0.0"));
        List integerList = dobj.getList("integer");
        integerList.add(new BigInteger("0"));
        integerList.add(new BigInteger("0"));
        integerList.add(new BigInteger("0"));
        return dobj;
    }

    public void testByteConversion()
    {
        //Byte to Double
        //Byte to Float
        //Byte to Int
        //Byte to Long
        //Byte to Short
        //Byte to String
        System.out.println("testByteConversion()");
        DataObject dobj = createTestDO();
        dobj.setByte("double[2]", (byte)2);
        assertEquals(2.0, dobj.get("double[2]"));
        dobj.setByte("float[3]", (byte)3);
        assertEquals((float)3.0, dobj.get("float[3]"));
        dobj.setByte("int[1]", (byte)1);
        assertEquals(1, dobj.get("int[1]"));
        dobj.setByte("long[2]", (byte)2);
        assertEquals((long)2, dobj.get("long[2]"));
        dobj.setByte("short[3]", (byte)3);
        assertEquals((short)3, dobj.get("short[3]"));
        dobj.setByte("string[2]", (byte)2);
        assertEquals("2", dobj.get("string[2]"));
    }

    public void testCharacterConversion()
    {
        //Character to String
        System.out.println("testCharacterConversion()");
        DataObject dobj = createTestDO();
        dobj.setChar("string[2]", 'c');
        assertEquals("c", dobj.get("string[2]"));
    }

    public void testDoubleConversion()
    {
        //Double to Byte
        //Double to Float
        //Double to Int
        //Double to Long
        //Double to Short
        //Double to String
        //Double to Decimal
        //Double to Integer
        System.out.println("testDoubleConversion()");
        DataObject dobj = createTestDO();
        dobj.setDouble("byte[2]", 2.0);
        assertEquals((byte)2, dobj.get("byte[2]"));
        dobj.setDouble("float[3]", 3.0);
        assertEquals((float)3.0, dobj.get("float[3]"));
        dobj.setDouble("int[1]", 1.0);
        assertEquals(1, dobj.get("int[1]"));
        dobj.setDouble("long[2]", 2.0);
        assertEquals((long)2, dobj.get("long[2]"));
        dobj.setDouble("short[3]", 3.0);
        assertEquals((short)3, dobj.get("short[3]"));
        dobj.setDouble("string[1]", 1.0);
        assertEquals("1.0", dobj.get("string[1]"));
        dobj.setDouble("decimal[2]", 2.0);
        assertEquals(new BigDecimal("2.0"), dobj.get("decimal[2]"));
        dobj.setDouble("integer[3]", 3.0);
        assertEquals(new BigInteger("3"), dobj.get("integer[3]"));
    }

    public void testFloatConversion()
    {
        //Float to Byte
        //Float to Double
        //Float to Int
        //Float to Long
        //Float to Short
        //Float to String
        //Float to Decimal
        //Float to Integer
        System.out.println("testFloatConversion()");
        DataObject dobj = createTestDO();
        dobj.setFloat("byte[2]", (float)2.0);
        assertEquals((byte)2, dobj.get("byte[2]"));
        dobj.setFloat("double[3]", (float)3.0);
        assertEquals(3.0, dobj.get("double[3]"));
        dobj.setFloat("int[1]", (float)1.0);
        assertEquals(1, dobj.get("int[1]"));
        dobj.setFloat("long[2]", (float)2.0);
        assertEquals((long)2, dobj.get("long[2]"));
        dobj.setFloat("short[3]", (float)3.0);
        assertEquals((short)3, dobj.get("short[3]"));
        dobj.setFloat("string[1]", (float)1.0);
        assertEquals("1.0", dobj.get("string[1]"));
        dobj.setFloat("decimal[2]", (float)2.0);
        assertEquals(new BigDecimal("2.0"), dobj.get("decimal[2]"));
        dobj.setFloat("integer[3]", (float)3.0);
        assertEquals(new BigInteger("3"), dobj.get("integer[3]"));
    }

    public void testIntConversion()
    {
        //Int to Byte
        //Int to Double
        //Int to Float
        //Int to Long
        //Int to Short
        //Int to String
        //Int to Decimal
        //Int to Integer
        System.out.println("testIntConversion()");
        DataObject dobj = createTestDO();
        dobj.setInt("byte[2]", 2);
        assertEquals((byte)2, dobj.get("byte[2]"));
        dobj.setInt("double[3]", 3);
        assertEquals(3.0, dobj.get("double[3]"));
        dobj.setInt("float[1]", 1);
        assertEquals((float)1.0, dobj.get("float[1]"));
        dobj.setInt("long[2]", 2);
        assertEquals((long)2, dobj.get("long[2]"));
        dobj.setInt("short[3]", 3);
        assertEquals((short)3, dobj.get("short[3]"));
        dobj.setInt("string[1]", 1);
        assertEquals("1", dobj.get("string[1]"));
        dobj.setInt("decimal[2]", 2);
        assertEquals(new BigDecimal("2"), dobj.get("decimal[2]"));
        dobj.setInt("integer[3]", 3);
        assertEquals(new BigInteger("3"), dobj.get("integer[3]"));
    }

    public void testLongConversion()
    {
        //Long to Byte
        //Long to Double
        //Long to Float
        //Long to Int
        //Long to Short
        //Long to String
        //Long to Decimal
        //Long to Integer
        System.out.println("testLongConversion()");
        DataObject dobj = createTestDO();
        dobj.setLong("byte[2]", 2l);
        assertEquals((byte)2, dobj.get("byte[2]"));
        dobj.setLong("double[3]", 3l);
        assertEquals(3.0, dobj.get("double[3]"));
        dobj.setLong("float[1]", 1l);
        assertEquals((float)1.0, dobj.get("float[1]"));
        dobj.setLong("int[2]", 2l);
        assertEquals(2, dobj.get("int[2]"));
        dobj.setLong("short[3]", 3l);
        assertEquals((short)3, dobj.get("short[3]"));
        dobj.setLong("string[1]", 1l);
        assertEquals("1", dobj.get("string[1]"));
        dobj.setLong("decimal[2]", 2l);
        assertEquals(new BigDecimal("2"), dobj.get("decimal[2]"));
        dobj.setLong("integer[3]", 3l);
        assertEquals(new BigInteger("3"), dobj.get("integer[3]"));
        //Long to Date
    }

    public void testShortConversion()
    {
        //Short to Byte
        //Short to Double
        //Short to Float
        //Short to Int
        //Short to Long
        //Short to String
        System.out.println("testShortConversion()");
        DataObject dobj = createTestDO();
        dobj.setShort("byte[2]", (short)2);
        assertEquals((byte)2, dobj.get("byte[2]"));
        dobj.setShort("double[3]", (short)3);
        assertEquals(3.0, dobj.get("double[3]"));
        dobj.setShort("float[1]", (short)1);
        assertEquals((float)1.0, dobj.get("float[1]"));
        dobj.setShort("int[2]", (short)2);
        assertEquals(2, dobj.get("int[2]"));
        dobj.setShort("long[3]", (short)3);
        assertEquals((long)3, dobj.get("long[3]"));
        dobj.setShort("string[1]", (short)1);
        assertEquals("1", dobj.get("string[1]"));
    }

    public void testBytesConversion()
    {
        //Bytes to Integer
        System.out.println("testBytesConversion()");
        DataObject dobj = createTestDO();
        byte[] ba = {(byte)1, (byte)1, (byte)1, (byte)1, (byte)1, (byte)1, (byte)1, (byte)1};
        dobj.setBytes("integer[2]", ba);
        BigInteger bi = new BigInteger(ba);
        assertEquals(bi, dobj.get("integer[2]"));
    }

    public void testDecimalConversion()
    {
        //Decimal to Double
        //Decimal to Float
        //Decimal to Int
        //Decimal to Long
        //Decimal to String
        //Decimal to Integer
        System.out.println("testDecimalConversion()");
        DataObject dobj = createTestDO();
        dobj.setBigDecimal("double[2]", new BigDecimal("2.0"));
        assertEquals(2.0, dobj.get("double[2]"));
        dobj.setBigDecimal("float[3]", new BigDecimal("3.0"));
        assertEquals((float)3.0, dobj.get("float[3]"));
        dobj.setBigDecimal("int[1]", new BigDecimal("1.0"));
        assertEquals(1, dobj.get("int[1]"));
        dobj.setBigDecimal("long[2]", new BigDecimal("2.0"));
        assertEquals((long)2, dobj.get("long[2]"));
        //dobj.setBigDecimal("short[3]", new BigDecimal("3.0"));
        //assertEquals((short)3, dobj.get("short[3]"));
        dobj.setBigDecimal("string[1]", new BigDecimal("1.0"));
        assertEquals("1.0", dobj.get("string[1]"));
        dobj.setBigDecimal("integer[3]", new BigDecimal("3.0"));
        assertEquals(new BigInteger("3"), dobj.get("integer[3]"));
    }

    public void testIntegerConversion()
    {
        //Integer to Double
        //Integer to Float
        //Integer to Int
        //Integer to Long
        //Integer to String
        //Integer to Bytes
        //Integer to Decimal
        System.out.println("testIntegerConversion()");
        DataObject dobj = createTestDO();
        dobj.setBigInteger("double[2]", new BigInteger("2"));
        assertEquals(2.0, dobj.get("double[2]"));
        dobj.setBigInteger("float[3]", new BigInteger("3"));
        assertEquals((float)3.0, dobj.get("float[3]"));
        dobj.setBigInteger("int[1]", new BigInteger("1"));
        assertEquals(1, dobj.get("int[1]"));
        dobj.setBigInteger("long[2]", new BigInteger("2"));
        assertEquals((long)2, dobj.get("long[2]"));
        //dobj.setBigInteger("short[3]", new BigInteger("3"));
        //assertEquals((short)3, dobj.get("short[3]"));
        dobj.setBigInteger("string[1]", new BigInteger("1"));
        assertEquals("1", dobj.get("string[1]"));
        dobj.setBigInteger("decimal[2]", new BigInteger("2"));
        assertEquals(new BigDecimal("2"), dobj.get("decimal[2]"));
    }

    public void testDateConversion()
    {
        //Date to Long
        //Date to String

        //Day
        //DateTime
        //Duration
        //Month
        //MonthDay
        //Time
        //Year
        //YearMonth
        //YearMonthDay
    }

    public void testStringConversion()
    {
        //String to Boolean
        //String to Byte
        //String to Character
        //String to Double
        //String to Float
        //String to Int
        //String to Long
        //String to Short
        //String to Decimal
        //String to Integer
        //String to Date

        //Day
        //DateTime
        //Duration
        //Month
        //MonthDay
        //Time
        //Year
        //YearMonth
        //YearMonthDay

        //String to Strings -> setString on a Strings property, String should be converted to a List<String>
    }

    /* N.A.
    public void testStringsConversion()
    {
        //Strings to String
    }
    */

    /* create a test object with a populated list for a property of type
       with String instance class */
    private DataObject createTestDO(String property)
    {
        DataObject dobj = factory.create(basic_t2);
        List pList = dobj.getList(property);
        pList.add("");
        pList.add("");
        pList.add("");
        return dobj;
    }

    //Day
    //DateTime
    //Duration
    //Month
    //MonthDay
    //Time
    //Year
    //YearMonth
    //YearMonthDay
}
