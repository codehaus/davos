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

import java.util.List;
import java.util.ArrayList;

import javax.sdo.DataObject;
import javax.sdo.Property;
import javax.sdo.Type;
import javax.sdo.helper.DataFactory;
import javax.sdo.helper.XMLHelper;
import javax.sdo.helper.XMLDocument;
import javax.sdo.helper.TypeHelper;

import junit.framework.Test;
import junit.framework.TestSuite;
import common.DataTest;

/**
 * @author Wing Yew Poon
 */
public class ListBugTest extends DataTest
{
    public ListBugTest(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        /*
        TestSuite suite = new TestSuite();
        //suite.addTest(new ListBugTest("testListUpdate1"));
        //suite.addTest(new ListBugTest("testListUpdate2"));
        suite.addTest(new ListBugTest("testAddNull"));
        suite.addTest(new ListBugTest("testAddNull2"));
        suite.addTest(new ListBugTest("testAddNull3"));
        suite.addTest(new ListBugTest("testAddAll"));
        suite.addTest(new ListBugTest("testAddAll2"));
        */
        // or
        TestSuite suite = new TestSuite(ListBugTest.class);
        return suite;
    }

    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(suite());
    }

    private static DataFactory factory = context.getDataFactory();
    private static XMLHelper xmlHelper = context.getXMLHelper();
    private static TypeHelper typeHelper = context.getTypeHelper();

    // according to the spec:
    // "For many-valued Properties, get() and getList() return a List 
    // containing the current values. Updates through the List interface 
    // operate on the current values of the DataObject immediately.
    // Each access to get() or getList() returns the same List object."
    public void testListUpdate1() throws Exception
    {
        DataObject company = getRootDataObject("checkin", "company.xml");
        Object o = company.get("departments");
        assertTrue(o instanceof List);
        List departments = company.getList("departments");
        assertTrue(departments.equals(o));
        //assertTrue(departments == o); // fails
        DataObject department = (DataObject) departments.get(0);
        List employees = department.getList("employees");
        employees.remove(1);
        // at this point, Mary Smith should be gone
        String out = xmlHelper.save(company, "company.xsd", "company");
        System.out.println(out);
        //String name = company.getDataObject("departments.0").getDataObject("employees.1").getString("name");
        String name = (String)company.get("departments.0/employees.1/name");
        System.out.println(name);
        assertEquals("Jane Doe", name);
    }

    // according to the spec:
    // "Returned Lists actively represent any changes to the DataObject's 
    // values."
    public void testListUpdate2() throws Exception
    {
        DataObject company = getRootDataObject("checkin", "company.xml");
        List departments = company.getList("departments");
        DataObject department = (DataObject) departments.get(0);
        List employees = department.getList("employees");
        company.set("departments.0/employees.1/name", "Mary Allen");
        DataObject mary = (DataObject)employees.get(1);
        String name = (String)mary.get("name");
        assertEquals("Mary Allen", name);
    }

    public void testAddNull() throws Exception
    {
        System.out.println("testAddNull()");
        DataObject company = getRootDataObject("checkin", "company.xml");
        List employees = company.getList("departments[1]/employees");
        assertEquals(3, employees.size());
        try
        {
            employees.add(null);
            fail("no exception was thrown");
        }
        catch (IllegalArgumentException e)
        {
            System.out.println("IllegalArgumentException thrown as expected");
            System.out.println(e.getMessage());
        }
    }

    public void testAddNull2() throws Exception
    {
        System.out.println("testAddNull2()");
        XMLDocument doc = xmlHelper.load("<catalog4><product4 name=\"X\"/></catalog4>");
        DataObject catalog = doc.getRootObject();
        assertTrue(catalog.getType().isSequenced());
        Property p = catalog.getInstanceProperty("product4");
        assertNotNull(p);
        assertTrue(p.isMany());
        assertFalse(p.isNullable());
        List productList = catalog.getList(p);
        assertEquals(1, productList.size());
        try
        {
            productList.add(null);
            fail("no exception was thrown");
        }
        catch (IllegalArgumentException e)
        {
            System.out.println("IllegalArgumentException thrown as expected");
            System.out.println(e.getMessage());
        }
    }

    public void testAddNull3() throws Exception
    {
        System.out.println("testAddNull3()");
        // load xml without schema so that we get a sequenced type
        XMLDocument doc = xmlHelper.load("<catalog><product name=\"X\"/></catalog>");
        DataObject catalog = doc.getRootObject();
        assertTrue(catalog.getType().isSequenced());
        Property p = catalog.getInstanceProperty("product");
        assertNotNull(p);
        assertTrue(p.isMany());
        System.out.println("product is nullable? " + p.isNullable());
        //assertFalse(p.isNullable()); // apparently p is nullable
        List productList = catalog.getList(p);
        assertEquals(1, productList.size());
        try
        {
            productList.add(null);
            //fail("no exception was thrown"); // well, since p is nullable ...
            System.out.println("no exception was thrown");
        }
        catch (IllegalArgumentException e)
        {
            System.out.println("IllegalArgumentException thrown as expected");
            System.out.println(e.getMessage());
        }
    }

    private static DataObject createType(String uri, String name)
    {
        DataObject newType = factory.create("commonj.sdo", "Type");
        newType.set("uri", uri);
        newType.set("name", name);
        return newType;
    }

    private static void createCatalogType()
    {
        Type typeCheck = typeHelper.getType("", "catalog");
        if (typeCheck == null)
        {
            System.out.println("catalog type is not defined");
            DataObject newType = createType("", "catalog");
            DataObject newType2 = createType("", "product");

            DataObject property = newType2.createDataObject("property");
            property.set("name", "name");
            property.set("type", typeHelper.getType("commonj.sdo", "String"));

            Type nt2 = typeHelper.define(newType2);

            DataObject property2 = newType.createDataObject("property");
            property2.set("name", "product");
            property2.set("type", nt2);
            property2.setBoolean("many", true);

            typeHelper.define(newType);
        }
    }

    public void testAddNull4() throws Exception
    {
        System.out.println("testAddNull4()");
        createCatalogType();
        XMLDocument doc = xmlHelper.load("<catalog><product name=\"X\"/></catalog>");
        DataObject catalog = doc.getRootObject();
        Type catalogType = typeHelper.getType("", "catalog");
        System.out.println(catalog.getType());
        System.out.println(catalogType);
        System.out.println(catalogType == catalog.getType());
        Property p = catalog.getInstanceProperty("product");
        assertNotNull(p);
        assertTrue(p.isMany());
        System.out.println("product is nullable? " + p.isNullable());
        List productList = catalog.getList(p);
        assertEquals(1, productList.size());
        try
        {
            productList.add(null);
            //fail("no exception was thrown"); // well, since p is nullable ...
            System.out.println("no exception was thrown");
        }
        catch (IllegalArgumentException e)
        {
            System.out.println("IllegalArgumentException thrown as expected");
            System.out.println(e.getMessage());
        }
    }

    public void testAddAll() throws Exception
    {
        System.out.println("testAddAll()");
        DataObject company = getRootDataObject("checkin", "company.xml");
        List employees = company.getList("departments[1]/employees");
        assertEquals(3, employees.size());

        // construct a list of new employees to be added
        List newEmployees = new ArrayList();
        DataObject newEmployee1 = factory.create("company.xsd", "EmployeeType");
        newEmployee1.set("name", "Dwight Schrute");
        newEmployee1.set("SN", "E0004");
        DataObject newEmployee2 = factory.create("company.xsd", "EmployeeType");
        newEmployee2.set("name", "Jim Halpert");
        newEmployee2.set("SN", "E0005");
        DataObject newEmployee3 = factory.create("company.xsd", "EmployeeType");
        newEmployee3.set("name", "Ryan Howard");
        newEmployee3.set("SN", "E0006");
        newEmployees.add(newEmployee1);
        newEmployees.add(newEmployee2);
        newEmployees.add(newEmployee3);

        // now add the new employees
        employees.addAll(3, newEmployees);
        assertEquals(6, employees.size());
        for (int i = 0; i < 6; i++)
            System.out.println(((DataObject)employees.get(i)).get("name"));
        assertEquals("Dwight Schrute", ((DataObject)employees.get(3)).get("name"));
        assertEquals("Jim Halpert", ((DataObject)employees.get(4)).get("name"));
        assertEquals("Ryan Howard", ((DataObject)employees.get(5)).get("name"));
    }

    public void testAddAll2() throws Exception
    {
        System.out.println("testAddAll2()");
        DataObject company = getRootDataObject("checkin", "company.xml");
        List employees = company.getList("departments[1]/employees");
        assertEquals(3, employees.size());

        // construct a list of new employees to be added
        List newEmployees = new ArrayList();
        DataObject newEmployee1 = factory.create("company.xsd", "EmployeeType");
        newEmployee1.set("name", "Dwight Schrute");
        newEmployee1.set("SN", "E0004");
        DataObject newEmployee2 = factory.create("company.xsd", "EmployeeType");
        newEmployee2.set("name", "Jim Halpert");
        newEmployee2.set("SN", "E0005");
        DataObject newEmployee3 = factory.create("company.xsd", "EmployeeType");
        newEmployee3.set("name", "Ryan Howard");
        newEmployee3.set("SN", "E0006");
        newEmployees.add(newEmployee1);
        newEmployees.add(newEmployee2);
        newEmployees.add(newEmployee3);

        // now add the new employees
        employees.addAll(0, newEmployees);
        assertEquals(6, employees.size());
        for (int i = 0; i < 6; i++)
            System.out.println(((DataObject)employees.get(i)).get("name"));
        assertEquals("Dwight Schrute", ((DataObject)employees.get(0)).get("name"));
        assertEquals("Jim Halpert", ((DataObject)employees.get(1)).get("name"));
        assertEquals("Ryan Howard", ((DataObject)employees.get(2)).get("name"));
    }

    public void testLastIndexOf() throws Exception
    {
        DataObject company = getRootDataObject("checkin", "company.xml");
        List employees = company.getList("departments[1]/employees");
        DataObject employee2 = (DataObject)employees.get(1);
        assertNotNull(employee2);
        int i1 = employees.indexOf(employee2);
        assertEquals(1, i1);
        int i2 = employees.lastIndexOf(employee2);
        assertEquals(1, i2);
    }

    public void testEmptyList()
    {
        System.out.println("testEmptyList()");
        Type stringType = typeHelper.getType("commonj.sdo", "String");

        DataObject zType = factory.create("commonj.sdo", "Type");
        zType.set("uri", "example.com/test");
        zType.set("name", "Z");
        zType.set("open", true);

        DataObject zProperty = zType.createDataObject("property");
        zProperty.set("name", "z");
        zProperty.set("type", stringType);
        zProperty.set("many", true);

        Type t = typeHelper.define(zType);
        DataObject dobj = factory.create(t);

        List list = dobj.getList("z");

        list.add("first");
        list.add("second");
//        list.set(1, null);  // not possible
        dobj.set("z[2]", "third");

        System.out.println(list);

        List list2 = dobj.getList("w");

        list2.add("wfirst");
        list2.add("wsecond");
//        list.set(1, null); // not possible
        dobj.set("w[2]", "wthird"); 

        System.out.println(list2);

        System.out.println(xmlHelper.save(dobj, "example.com/test", "test"));
    }
}
