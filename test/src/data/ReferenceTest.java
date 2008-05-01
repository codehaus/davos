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

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.ArrayList;

import javax.sdo.DataObject;
import javax.sdo.ChangeSummary;
import javax.sdo.Type;
import javax.sdo.Property;
import javax.sdo.helper.DataFactory;
import javax.sdo.helper.TypeHelper;
import javax.sdo.helper.XMLHelper;
import javax.sdo.helper.XSDHelper;
import javax.sdo.helper.XMLDocument;

import davos.sdo.Options;

import junit.framework.Test;
import junit.framework.TestSuite;
import common.DataTest;

/** 
    A Property whose Type is for a DataObject is called a reference.
    An attribute or element whose xsd type is IDREF or IDREFS or anyURI or
    any restriction thereof may have an sdo:propertyType annotation (the
    type specified must have datatype=false); such an annotation turns the 
    property from a datatype property to a reference property, specifically 
    a <em>non-containment</em> reference.
    Such an attribute/element may have an sdo:oppositeProperty annotation 
    (specifying a property within the type specified by sdo:propertyType)
    in addition, which makes the property <em>bi-directional</em>.
    
    This class contains tests for the above aspects of Property.
    
    @author Wing Yew Poon
 */
public class ReferenceTest extends DataTest
{
    public ReferenceTest(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        
        TestSuite suite = new TestSuite();
        
        suite.addTest(new ReferenceTest("testUnmarshalIDREF1"));
        suite.addTest(new ReferenceTest("testUnmarshalIDREF2"));
        suite.addTest(new ReferenceTest("testMarshalIDREF"));
        suite.addTest(new ReferenceTest("testMarshalIDREF2"));
        suite.addTest(new ReferenceTest("testIDREFS1"));
        suite.addTest(new ReferenceTest("testIDREFS2_1"));
        suite.addTest(new ReferenceTest("testIDREFS2_2"));
        suite.addTest(new ReferenceTest("testAnyURI1"));
        suite.addTest(new ReferenceTest("testAnyURI2"));
        suite.addTest(new ReferenceTest("testOppositeProperty1"));
        suite.addTest(new ReferenceTest("testOppositeProperty2"));
        suite.addTest(new ReferenceTest("testOppositeProperty3"));
        
        // or
        //TestSuite suite = new TestSuite(ReferenceTest.class);
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

    private static TypeHelper typeHelper = context.getTypeHelper();
    private static XSDHelper xsdHelper = context.getXSDHelper();
    private static DataFactory dataFactory = context.getDataFactory();
    private static XMLHelper xmlHelper = context.getXMLHelper();

    /* xsd type (of attribute) is IDREF, no sdo:propertyType annotation.
       Verify that the property has the correct type. */
    public void testUnmarshalIDREF1() throws Exception
    {
        DataObject company = getRootDataObject("checkin", "company.xml");
        Object eOTM = company.get("employeeOfTheMonth");
        assertTrue(eOTM instanceof String);
        assertEquals("E0002", eOTM);
    }

    /* xsd type (of attribute) is IDREF, sdo:propertyType annotation present.
       Verify that the property has the correct type. */
    public void testUnmarshalIDREF2() throws Exception
    {
        DataObject company = getRootDataObject("checkin", "company2.xml");
        Object eOTM = company.get("employeeOfTheMonth");
        assertTrue(eOTM instanceof DataObject);
        assertEquals("Mary Smith", ((DataObject)eOTM).get("name"));
    }

    private void changeCompany(DataObject company)
    {
        List departments = company.getList("departments");
        DataObject department = (DataObject) departments.get(0);
        DataObject newEmployee = department.createDataObject("employees");
        newEmployee.set("name", "Al Smith");
        newEmployee.set("SN", "E0004");
        newEmployee.setBoolean("manager", true);
        company.set("employeeOfTheMonth", newEmployee);
    }

    private int findCompany(List dataObjects, int index)
    {
        Type t = typeHelper.getType("company2.xsd", "CompanyType");
        return find(t, dataObjects, index);
    }

    private int findDepartment(List dataObjects, int index)
    {
        Type t = typeHelper.getType("company2.xsd", "DepartmentType");
        return find(t, dataObjects, index);
    }

    private int findEmployee(List dataObjects, int index)
    {
        Type t = typeHelper.getType("company2.xsd", "EmployeeType");
        return find(t, dataObjects, index);
    }

    private int findCompany(List dataObjects)
    {
        return findCompany(dataObjects, 0);
    }

    private int findDepartment(List dataObjects)
    {
        return findDepartment(dataObjects, 0);
    }

    private int findEmployee(List dataObjects)
    {
        return findEmployee(dataObjects, 0);
    }

    private void verifyCompany(DataObject company)
    {
        assertEquals("ACME", company.get("name"));
        assertEquals("E0004", company.getDataObject("employeeOfTheMonth").get("SN"));
        List departments = company.getList("departments");
        assertEquals(1, departments.size());
        DataObject department = (DataObject) departments.get(0);
        assertEquals("NY", department.get("location"));
        assertEquals(123, department.getInt("number"));
        List employees = department.getList("employees");
        assertEquals(4, employees.size());
        DataObject employee = (DataObject)employees.get(0);
        assertEquals("John Jones", employee.get("name"));
        assertEquals("E0001", employee.get("SN"));
        Property manager = employee.getInstanceProperty("manager");
        assertFalse(employee.isSet(manager));
        employee = (DataObject)employees.get(1);
        assertEquals("Mary Smith", employee.get("name"));
        assertEquals("E0002", employee.get("SN"));
        assertTrue(employee.isSet(manager));
        assertTrue(employee.getBoolean(manager));
        employee = (DataObject)employees.get(2);
        assertEquals("Jane Doe", employee.get("name"));
        assertEquals("E0003", employee.get("SN"));
        assertFalse(employee.isSet(manager));
        employee = (DataObject)employees.get(3);
        assertEquals("Al Smith", employee.get("name"));
        assertEquals("E0004", employee.get("SN"));
        assertTrue(employee.isSet(manager));
        assertTrue(employee.getBoolean(manager));
    }

    /* verify the ChangeSummary after changeCompany() */
    private void verifyCompanyChange(ChangeSummary c)
    {
        List changeList = c.getChangedDataObjects();
        // company modified, department modified, employee created
        assertEquals(3, changeList.size());
        int i = findCompany(changeList);
        assertTrue(i >= 0);
        DataObject c1 = (DataObject)changeList.get(i);
        assertTrue(c.isModified(c1));
        i = findDepartment(changeList);
        assertTrue(i >= 0);
        DataObject c2 = (DataObject)changeList.get(i);
        assertTrue(c.isModified(c2));
        i = findEmployee(changeList);
        assertTrue(i >= 0);
        DataObject c3 = (DataObject)changeList.get(i);
        assertTrue(c.isCreated(c3));
        // created data object (new employee) has no old values
        List oldValues = c.getOldValues(c3);
        assertNotNull(oldValues);
        assertEquals(0, oldValues.size());
        // modified data object (company) has old values
        oldValues = c.getOldValues(c1);
        assertEquals(1, oldValues.size());
        ChangeSummary.Setting oldValue = (ChangeSummary.Setting)oldValues.get(0);
        assertEquals("employeeOfTheMonth", oldValue.getProperty().getName());
        assertTrue(oldValue.isSet());
        DataObject oldEOTM = (DataObject)oldValue.getValue();
        assertEquals("E0002", oldEOTM.get("SN"));
        assertEquals("Mary Smith", oldEOTM.get("name"));
    }

    /* xsd type (of attribute) is IDREF, sdo:propertyType annotation present.
       Test serialization of change summary. */
    public void testMarshalIDREF() throws Exception
    {
        DataObject company = getRootDataObjectWrapped("checkin", "company2.xml");
        ChangeSummary c = company.getChangeSummary();
        c.beginLogging();
        changeCompany(company);
        verifyCompany(company);
        verifyCompanyChange(c);
        saveDataObject(company, "company2.xsd", "company", 
                       new File(dir, "company2.xml"));
        compareXMLFiles(getResourceFile("data", "company2.xml"), 
                        new File(dir, "company2.xml"));
        saveDataGraph(company.getDataGraph(), 
                      new File(dir, "company2_dg.xml"));
        compareXMLFiles(getResourceFile("data", "company2_dg.xml"), 
                        new File(dir, "company2_dg.xml"));
    }

    /* xsd type (of attribute) is IDREF, sdo:propertyType annotation present.
       Delete data object IDREF refers to.
       Test serialization of change summary. */
    public void testMarshalIDREF2() throws Exception
    {
        DataObject company = getRootDataObjectWrapped("checkin", "company2.xml");
        ChangeSummary c = company.getChangeSummary();
        c.beginLogging();
        Object eOTM = company.get("employeeOfTheMonth");
        assertTrue(eOTM instanceof DataObject);
        DataObject employee2 = company.getDataObject("departments[1]/employees[2]");
        assertNotNull(employee2);
        assertTrue(eOTM == employee2);
        employee2.delete();
        try
        {
            String s = xmlHelper.save(company, "company2.xsd", "company");
            System.out.println(s);
            fail("an SDOMarshalException should have been thrown");
        }
        catch (Exception e)
        {
            System.err.println(e.getMessage());
            assertTrue(e instanceof davos.sdo.SDOMarshalException);
            assertTrue(e.getMessage().indexOf("reference to an object outside") > -1);
        }
        company.unset("employeeOfTheMonth");
        File f = new File(dir, "company2_2.xml");
        saveDataObject(company, "company2.xsd", "company", f);
        compareXMLFiles(getResourceFile("data", "company2_2.xml"), f);
        f = new File(dir, "company2_dg2.xml");
        saveDataGraph(company.getDataGraph(), f);
        compareXMLFiles(getResourceFile("data", "company2_dg2.xml"), f);
    }

    /* xsd type (of attribute) is IDREFS, no sdo:propertyType annotation.
       Test unmarshalling and marshalling (including change summary). */
    public void testIDREFS1() throws Exception
    {
        //System.out.println("testIDREFS1()");
        DataObject company = getRootDataObjectWrapped("data", "company3a.xml");
        Property p = company.getInstanceProperty("employeesOfTheMonth");
        Type t = p.getType();
        //System.out.println("SDOContextFactory type of employeesOfTheMonth: ");
        //System.out.println("  " + t.getURI());
        //System.out.println("  " + t.getName());
        assertEquals(typeHelper.getType("commonj.sdo", "Strings"), t);
        Object eOTM = company.get("employeesOfTheMonth");
        assertTrue(eOTM instanceof List);
        assertEquals(2, ((List)eOTM).size());
        assertTrue(((List)eOTM).get(0) instanceof String);
        assertTrue(((List)eOTM).get(1) instanceof String);
        assertEquals("E0004", ((List)eOTM).get(0));
        assertEquals("E0005", ((List)eOTM).get(1));
        File f = new File(dir, "company3a.xml");
        saveDataObject(company, "company3a.xsd", "company", f);
        compareXMLFiles(getResourceFile("data", "company3a.xml"), f);
        ChangeSummary c = company.getChangeSummary();
        c.beginLogging();
        List employeeList = new ArrayList();
        employeeList.add("E0002");
        employeeList.add("E0003");
        company.set("employeesOfTheMonth", employeeList);
        f = new File(dir, "company3a_dg.xml");
        saveDataGraph(company.getDataGraph(), f);
        compareXMLFiles(getResourceFile("data", "company3a_dg.xml"), f);
    }

    /* xsd type (of attribute) is IDREFS, sdo:propertyType annotation present.
       Test unmarshalling and marshalling (including change summary). */
    public void testIDREFS2_1() throws Exception
    {
        //System.out.println("testIDREFS2()");
        DataObject company = getRootDataObjectWrapped("data", "company3b.xml");
        Property p = company.getInstanceProperty("employeesOfTheMonth");
        assertTrue(p.isMany());
        Type t = p.getType();
        //System.out.println("SDOContextFactory type of employeesOfTheMonth: ");
        //System.out.println("  " + t.getURI());
        //System.out.println("  " + t.getName());
        List eOTM = company.getList("employeesOfTheMonth");
        assertNotNull(eOTM);
        assertEquals(2, eOTM.size());
        Type employeeType = 
            typeHelper.getType("company3b.xsd", "EmployeeType");
        assertEquals(employeeType, ((DataObject)eOTM.get(0)).getType());
        assertEquals("Al Smith", ((DataObject)eOTM.get(0)).get("name"));
        assertEquals(employeeType, ((DataObject)eOTM.get(1)).getType());
        assertEquals("Joshua Klein", ((DataObject)eOTM.get(1)).get("name"));
        File f = new File(dir, "company3b.xml");
        saveDataObject(company, "company3b.xsd", "company", f);
        compareXMLFiles(getResourceFile("data", "company3b.xml"), f);
        ChangeSummary c = company.getChangeSummary();
        c.beginLogging();
        List employeeList = new ArrayList();
        DataObject employee2 = //company.getDataObject("departments[number=123]/employees[SN='E0002']");
            company.getDataObject("departments[1]/employees[2]");
        assertNotNull(employee2);
        DataObject employee3 = //company.getDataObject("departments[number=123]/employees[SN='E0003']");
            company.getDataObject("departments[1]/employees[3]");
        assertNotNull(employee3);
        employeeList.add(employee2);
        employeeList.add(employee3);
        company.setList("employeesOfTheMonth", employeeList);
        saveDataObject(company, "company3b.xsd", "company", 
                       new File(dir, "company3b_1.xml"));
        f = new File(dir, "company3b_dg.xml");
        saveDataGraph(company.getDataGraph(), f);
        compareXMLFiles(getResourceFile("data", "company3b_dg.xml"), f);
    }

    /* xsd type (of attribute) is IDREFS, sdo:propertyType annotation present.
       Test unmarshalling and marshalling (including change summary). */
    public void testIDREFS2_2() throws Exception
    {
        //System.out.println("testIDREFS2()");
        DataObject company = getRootDataObjectWrapped("data", "company3b.xml");
        Property p = company.getInstanceProperty("employeesOfTheMonth");
        assertTrue(p.isMany());
        Type t = p.getType();
        //System.out.println("SDOContextFactory type of employeesOfTheMonth: ");
        //System.out.println("  " + t.getURI());
        //System.out.println("  " + t.getName());
        List eOTM = company.getList("employeesOfTheMonth");
        assertNotNull(eOTM);
        assertEquals(2, eOTM.size());
        Type employeeType = 
            typeHelper.getType("company3b.xsd", "EmployeeType");
        assertEquals(employeeType, ((DataObject)eOTM.get(0)).getType());
        assertEquals("Al Smith", ((DataObject)eOTM.get(0)).get("name"));
        assertEquals(employeeType, ((DataObject)eOTM.get(1)).getType());
        assertEquals("Joshua Klein", ((DataObject)eOTM.get(1)).get("name"));
        File f = new File(dir, "company3b.xml");
        saveDataObject(company, "company3b.xsd", "company", f);
        compareXMLFiles(getResourceFile("data", "company3b.xml"), f);
        ChangeSummary c = company.getChangeSummary();
        c.beginLogging();
        DataObject employee2 = //company.getDataObject("departments[number=123]/employees[SN='E0002']");
            company.getDataObject("departments[1]/employees[2]");
        assertNotNull(employee2);
        DataObject employee3 = //company.getDataObject("departments[number=123]/employees[SN='E0003']");
            company.getDataObject("departments[1]/employees[3]");
        assertNotNull(employee3);
        eOTM.clear();
        eOTM.add(employee2);
        eOTM.add(employee3);
        saveDataObject(company, "company3b.xsd", "company", 
                       new File(dir, "company3b_2.xml"));
        f = new File(dir, "company3b_dg.xml");
        saveDataGraph(company.getDataGraph(), f);
        compareXMLFiles(getResourceFile("data", "company3b_dg.xml"), f);
    }

    /* xsd type (of attribute) is anyURI, no sdo:propertyType annotation.
       Test unmarshalling and marshalling (including change summary). */
    public void testAnyURI1() throws Exception
    {
        DataObject company = getRootDataObjectWrapped("data", "company4a.xml");
        Property p = company.getInstanceProperty("employeeOfTheMonth");
        Type t = p.getType();
        //System.out.println("SDOContextFactory type of employeeOfTheMonth: ");
        //System.out.println("  " + t.getURI());
        //System.out.println("  " + t.getName());
        assertEquals(typeHelper.getType("commonj.sdo", "URI"), t);
        Object eOTM = company.get("employeeOfTheMonth");
        assertTrue(eOTM instanceof String);
        //System.out.println(eOTM);
        assertEquals("#departments[1]/employees[2]", eOTM);
        saveDataObject(company, "company4a.xsd", "company", 
                       new File(dir, "company4a.xml"));
        compareXMLFiles(getResourceFile("data", "company4a.xml"), 
                        new File(dir, "company4a.xml"));
        ChangeSummary c = company.getChangeSummary();
        c.beginLogging();
        company.set("employeeOfTheMonth", "#departments[1]/employees[3]");
        saveDataGraph(company.getDataGraph(), 
                      new File(dir, "company4a_dg.xml"));
        compareXMLFiles(getResourceFile("data", "company4a_dg.xml"), 
                        new File(dir, "company4a_dg.xml"));
    }

    /* xsd type (of attribute) is anyURI, sdo:propertyType annotation present.
       Test unmarshalling and marshalling (including change summary). */
    public void testAnyURI2() throws Exception
    {
        DataObject company = getRootDataObjectWrapped("data", "company4b.xml");
        Property p = company.getInstanceProperty("employeeOfTheMonth");
        Type t = p.getType();
        //System.out.println("SDOContextFactory type of employeeOfTheMonth: ");
        //System.out.println("  " + t.getURI());
        //System.out.println("  " + t.getName());
        assertEquals(typeHelper.getType("company4b.xsd", "EmployeeType"), t);
        Object eOTM = company.get("employeeOfTheMonth");
        assertNotNull(eOTM);
        assertTrue(eOTM instanceof DataObject);
        assertEquals("Mary Smith", ((DataObject)eOTM).get("name"));
        saveDataObject(company, "company4b.xsd", "company", 
                       new File(dir, "company4b.xml"));
        // when serialized, the xpath in the anyURI is absolute
        compareXMLFiles(getResourceFile("data", "company4b_1.xml"), 
                        new File(dir, "company4b.xml"));
        ChangeSummary c = company.getChangeSummary();
        c.beginLogging();
        DataObject emp3 = company.getDataObject("departments[1]/employees[3]");
        assertNotNull(emp3);
        company.set("employeeOfTheMonth", emp3);
        assertEquals("Jane Doe", ((DataObject)company.get("employeeOfTheMonth")).get("name"));
        saveDataGraph(company.getDataGraph(), 
                      new File(dir, "company4b_dg.xml"));
        compareXMLFiles(getResourceFile("data", "company4b_dg.xml"), 
                        new File(dir, "company4b_dg.xml"));
    }

    /* xsd type (of attribute/element) is IDREF, sdo:propertyType and
       sdo:oppositeProperty annotations present.
       Test setting and unsetting of property and opposite property. */
    public void testOppositeProperty1() throws Exception
    {
        String testURI = "http://sdo/test/opposite";
        InputStream resourceStream = 
            getResourceAsStream("data", "opposite.xsd");
        File resourceFile = getResourceFile("data", "opposite.xsd");
        List types = 
            xsdHelper.define(resourceStream, 
                             resourceFile.toString());
        resourceStream.close();

        Type customerType = typeHelper.getType(testURI, "CustomerType");
        assertNotNull(customerType);
        Type addressType = typeHelper.getType(testURI, "AddressType");
        assertNotNull(addressType);
        Type orderType = typeHelper.getType(testURI, "OrderType");
        assertNotNull(orderType);
        Property customerAddressProperty = customerType.getProperty("address");
        assertNotNull(customerAddressProperty);
        Property addressCustomerProperty = addressType.getProperty("customer");
        assertNotNull(addressCustomerProperty);
        assertFalse(customerAddressProperty.isContainment());
        assertFalse(addressCustomerProperty.isContainment());
        // each is the other's opposite
        assertEquals(customerAddressProperty, addressCustomerProperty.getOpposite());
        assertEquals(addressCustomerProperty, customerAddressProperty.getOpposite());
        Property customerOrderProperty = customerType.getProperty("order");
        assertNotNull(customerOrderProperty);
        Property orderCustomerProperty = orderType.getProperty("customer");
        assertNotNull(orderCustomerProperty);
        // NOTE: if one end is containment, the other end must NOT be many
        assertTrue(customerOrderProperty.isContainment());
        assertTrue(customerOrderProperty.isMany());
        assertFalse(orderCustomerProperty.isContainment());
        assertFalse(orderCustomerProperty.isMany());
        // each is the other's opposite
        assertEquals(customerOrderProperty, orderCustomerProperty.getOpposite());
        assertEquals(orderCustomerProperty, customerOrderProperty.getOpposite());

        DataObject address = dataFactory.create(addressType);
        address.set("street", "123 Main Street");
        address.set("city", "Pleasantville");
        address.set("state", "CA");
        address.set("code", "00012-34567");

        DataObject customer = dataFactory.create(customerType);
        customer.set("name", "John Smith");
        customer.set("id", "010-2345-789-JS");
        customer.set("address", address);

        // setting customer's address sets its opposite, address's customer
        assertEquals("John Smith", address.getDataObject("customer").get("name"));
        // unsetting one unsets the opposite
        address.unset("customer");
        assertFalse(address.isSet("customer"));
        assertNull(address.get("customer"));
        assertFalse(customer.isSet("address"));
        assertNull(customer.get("address"));

        // now from the opposite direction
        address.set("customer", customer);
        assertEquals("123 Main Street", customer.getDataObject("address").get("street"));
        customer.unset("address");
        assertFalse(customer.isSet("address"));
        assertNull(customer.get("address"));
        assertFalse(address.isSet("customer"));
        assertNull(address.get("customer"));

        xmlHelper.save(customer, testURI, "customer", System.out);
        System.out.println();
        xmlHelper.save(address, testURI, "address", System.out);
        System.out.println();

        DataObject order = dataFactory.create(orderType);
        order.set("sku", "036000-291452");
        order.set("quantity", 45);
        order.set("customer", customer);
        assertEquals("John Smith", order.getDataObject("customer").get("name"));

        // setting order's customer adds to customer's order (many-valued)
        assertEquals(1, customer.getList("order").size());
        assertEquals("036000-291452", customer.get("order[1]/sku"));
        assertEquals(45, customer.getInt("order[1]/quantity"));

        DataObject order2 = dataFactory.create(orderType);
        order2.set("sku", "036000-874819");
        order2.set("quantity", 100);
        order2.set("customer", customer);
        assertEquals(2, customer.getList("order").size());
        assertEquals("036000-874819", customer.get("order[2]/sku"));
        assertEquals(100, customer.getInt("order[2]/quantity"));

        order.unset("customer");
        assertEquals(1, customer.getList("order").size());
        assertEquals("036000-874819", customer.get("order[1]/sku"));
        assertEquals(100, customer.getInt("order[1]/quantity"));

        order2.unset("customer");
        assertEquals(0, customer.getList("order").size());

        DataObject customer2 = dataFactory.create(customerType);
        customer2.set("name", "Mark Jones");
        customer2.set("id", "020-1234-567-MJ");
        customer2.set("address", address);

        assertEquals("Mark Jones", address.getDataObject("customer").get("name"));

        DataObject order3 = customer2.createDataObject("order");
        order3.set("sku", "036000-874819");
        order3.set("quantity", 50);
        assertEquals(customer2, order3.get("customer"));
        DataObject order4 = customer2.createDataObject("order");
        order4.set("sku", "036000-291452");
        order4.set("quantity", 75);
        assertEquals(customer2, order4.get("customer"));

        DataObject customerRecord = dataFactory.create(testURI, "CustomerRecordType");
        customerRecord.set("customer", customer2);
        customerRecord.set("address", address);
        xmlHelper.save(customerRecord, testURI, "customerRecord", System.out);
        System.out.println();

        customer2.unset("address");
        xmlHelper.save(customer2, testURI, "customer", System.out);
        System.out.println();

        customer2.unset("order");
        assertFalse(order3.isSet("customer"));
        assertNull(order3.get("customer"));
        assertFalse(order4.isSet("customer"));
        assertNull(order4.get("customer"));
        xmlHelper.save(customer2, testURI, "customer", System.out);
        System.out.println();
    }

    /* xsd type (of attribute) is IDREF, sdo:propertyType and
       sdo:oppositeProperty annotations present; 
       opposite property is many-valued (xsd type IDREFS).
    */
    public void testOppositeProperty2() throws Exception
    {
        DataObject a = getRootDataObject("data", "copy4.xml");

        // test non-containment reference
        String testURI = "http://sdo/test/copy4";
        Type cT = typeHelper.getType(testURI, "C");
        Type eT = typeHelper.getType(testURI, "E");
        Property cTeP = cT.getProperty("e");
        Property eTcP = eT.getProperty("c");
        assertTrue(cTeP.isMany());
        assertFalse(eTcP.isMany());
        assertEquals(eTcP, cTeP.getOpposite());
        assertEquals(cTeP, eTcP.getOpposite());

        DataObject b = a.getDataObject("b");
        assertEquals(true, b.getBoolean("d"));
        List<DataObject> e = b.getList("e");
        assertEquals(eT, e.get(0).getType());
        assertEquals(cT, e.get(0).getDataObject("c").getType());
        assertEquals(eT, e.get(1).getType());
        assertEquals(cT, e.get(1).getDataObject("c").getType());
        assertEquals(eT, e.get(2).getType());
        assertEquals(cT, e.get(2).getDataObject("c").getType());
        assertEquals("C456", e.get(0).getDataObject("c").get("c0"));
        assertEquals("C123", e.get(1).getDataObject("c").get("c0"));
        assertEquals("C456", e.get(2).getDataObject("c").get("c0"));
        List<DataObject> c = a.getList("c");
        assertEquals(cT, c.get(0).getType());
        assertEquals(cT, c.get(1).getType());
        assertEquals(1, c.get(0).getList("e").size());
        assertEquals(e.get(1), c.get(0).getList("e").get(0));
        assertEquals(2, c.get(1).getList("e").size());
        assertEquals(e.get(0), c.get(1).getList("e").get(0));
        assertEquals(e.get(2), c.get(1).getList("e").get(1));
    }

    public void testOppositeProperty3() throws Exception
    {
        String testURI = "http://sdo/test/copy4";
        Type cT = typeHelper.getType(testURI, "C");
        Type eT = typeHelper.getType(testURI, "E");
        
        DataObject e1 = dataFactory.create(eT);
        e1.set("e0", "E001");
        e1.setInt("e1", 100);
        e1.setString("e2", "xxx");
        DataObject e2 = dataFactory.create(eT);
        e2.set("e0", "E002");
        e2.setInt("e1", 200);
        e2.setString("e2", "yyy");
        DataObject e3 = dataFactory.create(eT);
        e3.set("e0", "E003");
        e3.setInt("e1", 300);
        e3.setString("e2", "zzz");

        DataObject c1 = dataFactory.create(cT);
        c1.set("c0", "C123");
        c1.setInt("c1", 5);
        c1.setString("c2", "abc");
        DataObject c2 = dataFactory.create(cT);
        c2.set("c0", "C456");
        c2.setInt("c1", 7);
        c2.setString("c2", "def");

        List c1e = c1.getList("e");
        c1e.add(e2);
        assertTrue(c1 == e2.get("c"));

        List c2e = c2.getList("e");
        c2e.add(e1);
        c2e.add(e3);
        assertTrue(c2 == e1.get("c"));
        assertTrue(c2 == e3.get("c"));

        Type aT = typeHelper.getType(testURI, "A");
        Type bT = typeHelper.getType(testURI, "B");
        DataObject a = dataFactory.create(aT);
        DataObject b = dataFactory.create(bT);
        b.setBoolean("d", true);
        List be = b.getList("e");
        be.add(e1);
        be.add(e2);
        be.add(e3);
        a.set("b", b);
        List ac = a.getList("c");
        ac.add(c1);
        ac.add(c2);
        /*
        XMLDocument doc = xmlHelper.createDocument(a, testURI, "a");
        doc.setXMLDeclaration(false);
        xmlHelper.save(doc, System.out, new Options().setSavePrettyPrint());
        System.out.println();
        */
        File f = new File(dir, "copy4_out.xml");
        saveDataObject(a, testURI, "a", f);
        compareXMLFiles(getResourceFile("data", "copy4.xml"), f);
        
        // add the same e again - this violates the constraint that
        // references in a many-valued bidirectional property must be
        // to distinct data objects
        try
        {
            c2e.add(e1);
            fail("an exception should have been thrown");
        }
        catch (Exception e)
        {
        }
    }
}
