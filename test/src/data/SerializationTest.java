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
import javax.sdo.*;
import javax.sdo.helper.*;

import davos.sdo.SDOContext;
import davos.sdo.SDOContextFactory;
import davos.sdo.Options;

import org.apache.xmlbeans.samples.xquery.employees.*;

import junit.framework.*;
import common.DataTest;

/**
 * @author Wing Yew Poon
 */
public class SerializationTest extends DataTest
{
    public SerializationTest(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        TestSuite suite = new TestSuite();
        // first 3 tests rely on static context and automatic deserialization
        suite.addTest(new SerializationTest("testDataObject1"));
        suite.addTest(new SerializationTest("testDataObject2"));
        suite.addTest(new SerializationTest("testDataGraph"));
        // next test uses automatic deserialization too, but does not use
        // the static context and helpers
        suite.addTest(new SerializationTest("testGetThreadLocalContext"));
        // last test controls its context and deserialization
        suite.addTest(new SerializationTest("testCreateObjectInputStream"));
        return suite;
    }

    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(suite());
    }

    public static File dir;
    static
    {
        // use the context set in base class (common.BaseTest)
        // set the context in the ThreadLocal so that automatic deserialization
        // will use it
        SDOContextFactory.setThreadLocalSDOContext(context);
        dir = new File(OUTPUTROOT + S + "data");
        dir.mkdirs();
    }

    private static TypeHelper typeHelper = context.getTypeHelper();
    private static XMLHelper xmlHelper = context.getXMLHelper();
    private static EqualityHelper equalityHelper = context.getEqualityHelper();
    private static final String empURI = 
        "http://xmlbeans.apache.org/samples/xquery/employees";

    private void serializeDO(DataObject dobj, File f) throws IOException
    {
        FileOutputStream fos = new FileOutputStream(f);
        ObjectOutputStream out = new ObjectOutputStream(fos);
        out.writeObject(dobj);
        out.close();
    }

    private DataObject deserializeDO(File f) throws IOException, ClassNotFoundException
    {
        FileInputStream fis = new FileInputStream(f);
        ObjectInputStream in = new ObjectInputStream(fis);
        DataObject dobj = (DataObject)in.readObject();
        in.close();
        return dobj;
    }

    private DataObject deserializeDO(File f, SDOContext ctx) throws IOException, ClassNotFoundException
    {
        FileInputStream fis = new FileInputStream(f);
        ObjectInputStream in = ctx.createObjectInputStream(fis);
        DataObject dobj = (DataObject)in.readObject();
        in.close();
        return dobj;
    }

    private void serializeDG(DataGraph dg, File f) throws IOException
    {
        FileOutputStream fos = new FileOutputStream(f);
        ObjectOutputStream out = new ObjectOutputStream(fos);
        out.writeObject(dg);
        out.close();
    }

    private DataGraph deserializeDG(File f) throws IOException, ClassNotFoundException
    {
        FileInputStream fis = new FileInputStream(f);
        ObjectInputStream in = new ObjectInputStream(fis);
        DataGraph dg = (DataGraph)in.readObject();
        in.close();
        return dg;
    }

    /* generic data object (of known type) */
    public void testDataObject1() throws Exception
    {
        System.out.println("testDataObject1()");
        Type employeesType = typeHelper.getType(empURI, "employees");
        Type employeeType = typeHelper.getType(empURI, "employeeType");
        DataObject employees = getRootDataObject("checkin", "employees.xml");
        assertEquals(employeesType, employees.getType());
        DataObject employee = employees.getDataObject("employee[2]");
        assertEquals(employeeType, employee.getType());

        File f1 = new File(dir, "employees.ser");
        File f2 = new File(dir, "employee2_1.ser");
        serializeDO(employees, f1);
        serializeDO(employee, f2);
        DataObject deser1 = deserializeDO(f1);
        System.out.println("employees deserialized:");
        System.out.println("  " + deser1.getType().getURI());
        System.out.println("  " + deser1.getType().getName());
        assertEquals(employeesType, deser1.getType());
        DataObject deser2 = deserializeDO(f2);
        System.out.println("employee deserialized:");
        System.out.println("  " + deser2.getType().getURI());
        System.out.println("  " + deser2.getType().getName());
        assertEquals(employeeType, deser2.getType());

        String uri = empURI;
        XMLDocument doc = xmlHelper.createDocument(deser1, uri, "employees");
        doc.setXMLDeclaration(false);
        Writer out = new StringWriter();
        Options opt = new Options().setSavePrettyPrint().setSaveIndent(2);
        xmlHelper.save(doc, out, opt);
        System.out.println(out.toString());
        out.close();
        doc = xmlHelper.createDocument(deser2, uri, "employee");
        doc.setXMLDeclaration(false);
        out = new StringWriter();
        xmlHelper.save(doc, out, opt);
        System.out.println(out.toString());
        out.close();

        DataObject deser1_employee = deser1.getDataObject("employee[2]");
        assertTrue(equalityHelper.equal(employees, deser1));
        assertTrue(equalityHelper.equal(employee, deser2));
        assertTrue(equalityHelper.equal(deser1_employee, deser2));
        assertTrue(equalityHelper.equal(deser1, deser2.getRootObject()));
    }

    /* typed data object */
    public void testDataObject2() throws Exception
    {
        System.out.println("testDataObject2()");
        Type employeesType = typeHelper.getType(empURI, "employees");
        DataObject employees = getRootDataObject("checkin", "employees.xml");
        EmployeeType employee = (EmployeeType)employees.getDataObject("employee[2]");
        File f = new File(dir, "employee2_2.ser");
        ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(f));
        out.writeObject(employee);
        out.close();
        ObjectInputStream in = new ObjectInputStream(new FileInputStream(f));
        EmployeeType deser = (EmployeeType)in.readObject();
        in.close();
        assertEquals("Sally Smith", deser.getName());
        DataObject root = deser.getRootObject();
        assertNotNull(root);
        System.out.println(root.getType().getURI());
        System.out.println(root.getType().getName());
        assertEquals(employeesType, root.getType());
        List<DataObject> employeeList = root.getList("employee");
        assertEquals(3, employeeList.size());
        assertTrue(deser == employeeList.get(1));
    }

    public void testDataGraph() throws Exception
    {
        System.out.println("testDataGraph()");
        DataGraph dg = getDataGraph("data", "company_dg0.xml");
        File f = new File(dir, "company_dg.ser");
        serializeDG(dg, f);
        DataGraph deser = deserializeDG(f);
        assertNotNull(deser);
        assertNotNull(deser.getRootObject());
        DataObject company = deser.getRootObject().getDataObject("company");
        f = new File(dir, "company_ser.xml");
        saveDataObject(company, "company.xsd", "company", f);
        compareXMLFiles(getResourceFile("checkin", "company.xml"), f, IGNORE_WHITESPACE);
    }

    private void verifyEmployee(DataObject employee)
    {
        assertEquals("Fred Jones", employee.get("name"));
        DataObject address = employee.getDataObject("address");
        assertEquals("900 Aurora Ave.", address.get("street"));
        assertEquals("Seattle", address.get("city"));
        assertEquals("(425)555-5665", employee.get("phone[location='work']/value"));
        assertEquals("(206)555-5555", employee.get("phone[location='home']/value"));
        assertEquals("(206)555-4321", employee.get("phone[location='mobile']/value"));
    }

    /* test SDOContextFactory.getThreadLocalSDOContext() - 
       start from the context in the ThreadLocal, instantiate a data object,
       serialize it, and then deserialize it, relying on the default mechanism
       that first tries to find the context in the ThreadLocal.
    */
    public void testGetThreadLocalContext() throws Exception
    {
        SDOContext ctx = SDOContextFactory.getThreadLocalSDOContext();
        assertNotNull(ctx);
        XMLHelper xmlh = ctx.getXMLHelper();
        File f = getResourceFile("data", "employee.xml");
        InputStream in = new FileInputStream(f);
        DataObject employee = xmlh.load(in).getRootObject();
        in.close();
        TypeHelper th = ctx.getTypeHelper();
        Type employeeType = th.getType("http://sdo/test/employee", "employeeType");
        assertEquals(employeeType, employee.getType());
        verifyEmployee(employee);
        f = new File(dir, "employee.ser");
        serializeDO(employee, f);
        DataObject deser = deserializeDO(f);
        assertEquals(employeeType, deser.getType());
        verifyEmployee(deser);
    }

    /* test SDOContext.createObjectInputStream() -
       create a new context, instantiate a data object, serialize it,
       and then deserialize it using the ObjectInputStream returned by
       the context.
    */
    public void testCreateObjectInputStream() throws Exception
    {
        SDOContext ctx = SDOContextFactory.createNewSDOContext(SerializationTest.class.getClassLoader());
        XMLHelper xmlh = ctx.getXMLHelper();
        File f = getResourceFile("data", "employee.xml");
        InputStream in = new FileInputStream(f);
        DataObject employee = xmlh.load(in).getRootObject();
        in.close();
        TypeHelper th = ctx.getTypeHelper();
        Type employeeType = th.getType("http://sdo/test/employee", "employeeType");
        assertEquals(employeeType, employee.getType());
        verifyEmployee(employee);
        f = new File(dir, "employee2.ser");
        serializeDO(employee, f);
        DataObject deser = deserializeDO(f, ctx);
        assertEquals(employeeType, deser.getType());
        verifyEmployee(deser);
    }
}
