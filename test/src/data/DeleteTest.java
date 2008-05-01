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

import javax.sdo.DataObject;
import javax.sdo.Type;
import javax.sdo.Property;
import javax.sdo.ChangeSummary;
import javax.sdo.helper.TypeHelper;

import junit.framework.Test;
import junit.framework.TestSuite;
import common.DataTest;

/**
 * @author Wing Yew Poon
 */
public class DeleteTest extends DataTest
{
    public DeleteTest(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        TestSuite suite = new TestSuite();
        suite.addTest(new DeleteTest("testDetach"));
        suite.addTest(new DeleteTest("testDelete"));
        suite.addTest(new DeleteTest("testSingleDetach"));
        suite.addTest(new DeleteTest("testSingleDelete"));
        //suite.addTest(new DeleteTest("testDeleteWithChangeTracking"));
        suite.addTest(new DeleteTest("testDeleteWithReadOnlyProperties"));
        suite.addTest(new DeleteTest("testCascadingDelete"));
        return suite;
    }

    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(suite());
    }

    private static TypeHelper typeHelper = context.getTypeHelper();

    public void testDetach() throws Exception
    {
        System.out.println("testDetach()");
        DataObject company = getRootDataObject("checkin", "company.xml");
        DataObject department = company.getDataObject("departments[1]");
        assertNotNull(department);
        DataObject employee = company.getDataObject("departments[1]/employees[2]");
        assertNotNull(employee);
        assertEquals("Mary Smith", employee.get("name"));
        assertEquals("E0002", employee.get("SN"));
        assertEquals(true, employee.getBoolean("manager"));
        // containing data object and property
        DataObject c = employee.getContainer();
        Property cp = employee.getContainmentProperty();
        assertNotNull(c);
        assertEquals(department, c);
        assertNotNull(cp);
        Property p = department.getInstanceProperty("employees");
        assertNotNull(p);
        assertEquals(p, cp);

        // now detach the employee
        employee.detach();
        // employees properties are unchanged
        assertEquals("Mary Smith", employee.get("name"));
        assertEquals("E0002", employee.get("SN"));
        assertEquals(true, employee.getBoolean("manager"));
        // containment relationship is broken
        assertNull(employee.getContainer());
        assertNull(employee.getContainmentProperty());
    }

    public void testDelete() throws Exception
    {
        System.out.println("testDelete()");
        DataObject company = getRootDataObject("checkin", "company.xml");
        DataObject department = company.getDataObject("departments[1]");
        assertNotNull(department);
        DataObject employee = company.getDataObject("departments[1]/employees[2]");
        assertNotNull(employee);
        assertEquals("Mary Smith", employee.get("name"));
        assertEquals("E0002", employee.get("SN"));
        assertEquals(true, employee.getBoolean("manager"));
        // containing data object and property
        DataObject c = employee.getContainer();
        Property cp = employee.getContainmentProperty();
        assertNotNull(c);
        assertEquals(department, c);
        assertNotNull(cp);
        Property p = department.getInstanceProperty("employees");
        assertNotNull(p);
        assertEquals(p, cp);

        // now delete the employee
        employee.delete();
        // employees properties are unset
        assertNull(employee.get("name"));
        assertNull(employee.get("SN"));
        assertEquals(false, employee.getBoolean("manager"));
        assertFalse(employee.isSet("name"));
        assertFalse(employee.isSet("SN"));
        assertFalse(employee.isSet("manager"));
        // containment relationship is broken
        assertNull(employee.getContainer());
        assertNull(employee.getContainmentProperty());
    }

    public void testSingleDetach() throws Exception
    {
        System.out.println("testSingleDetach()");
        DataObject items = getRootDataObject("data", "items.xml");
        DataObject shirt = items.getDataObject("shirt[1]");
        DataObject size = shirt.getDataObject("size");
        assertEquals(15, size.getInt("collar"));
        assertEquals(33, size.getInt("sleeve"));
        assertEquals(shirt, size.getContainer());
        assertEquals(shirt.getInstanceProperty("size"), size.getContainmentProperty());
        size.detach();
        assertEquals(15, size.getInt("collar"));
        assertEquals(33, size.getInt("sleeve"));
        assertNull(size.getContainer());
        assertNull(size.getContainmentProperty());
    }

    public void testSingleDelete() throws Exception
    {
        System.out.println("testSingleDelete()");
        DataObject items = getRootDataObject("data", "items.xml");
        DataObject shirt = items.getDataObject("shirt[1]");
        DataObject size = shirt.getDataObject("size");
        assertEquals(15, size.getInt("collar"));
        assertEquals(33, size.getInt("sleeve"));
        assertEquals(shirt, size.getContainer());
        assertEquals(shirt.getInstanceProperty("size"), size.getContainmentProperty());
        size.delete();
        assertFalse(size.isSet("collar"));
        assertFalse(size.isSet("sleeve"));
        assertNull(size.getContainer());
        assertNull(size.getContainmentProperty());
    }

    private void verifyEmployee(ChangeSummary cs, DataObject employee)
    {
        assertTrue(cs.isDeleted(employee));
        List<ChangeSummary.Setting> oldValues = 
            (List<ChangeSummary.Setting>)cs.getOldValues(employee);
        System.out.println("number of old values for employee: " + oldValues.size());
        assertEquals(3, oldValues.size());
        boolean name = false;
        boolean sn = false;
        boolean manager = false;
        for (ChangeSummary.Setting setting : oldValues)
        {
            System.out.println(setting.getProperty().getName() + ": " +
                               (setting.isSet() ? setting.getValue() : "[not set]"));
            if (setting.getProperty().getName().equals("name"))
            {
                name = true;
                assertEquals("Mary Smith", setting.getValue());
            }
            if (setting.getProperty().getName().equals("SN"))
            {
                sn = true;
                assertEquals("E0002", setting.getValue());
            }
            if (setting.getProperty().getName().equals("manager"))
            {
                manager = true;
                assertEquals(Boolean.TRUE, setting.getValue());
            }
        }
        assertTrue(name && sn && manager);
        ChangeSummary.Setting oldName = cs.getOldValue(employee, employee.getInstanceProperty("name"));
        System.out.println("old name: " + oldName.getValue());
        assertEquals("Mary Smith", oldName.getValue());
    }

    public void testDeleteWithChangeTracking() throws Exception
    {
        System.out.println("testDeleteWithChangeTracking()");
        DataObject company = getRootDataObjectWrapped("checkin", "company.xml");
        ChangeSummary cs = company.getChangeSummary();
        cs.beginLogging();
        DataObject department = company.getDataObject("departments[1]");
        assertNotNull(department);
        DataObject employee = company.getDataObject("departments[1]/employees[2]");
        assertNotNull(employee);
        assertEquals("Mary Smith", employee.get("name"));
        assertEquals("E0002", employee.get("SN"));
        assertEquals(true, employee.getBoolean("manager"));
        // containing data object and property
        DataObject c = employee.getContainer();
        Property cp = employee.getContainmentProperty();
        assertNotNull(c);
        assertEquals(department, c);
        assertNotNull(cp);
        Property p = department.getInstanceProperty("employees");
        assertNotNull(p);
        assertEquals(p, cp);

        // now delete the employee
        employee.delete();
        // employees properties are unset
        assertNull(employee.get("name"));
        assertNull(employee.get("SN"));
        assertEquals(false, employee.getBoolean("manager"));
        assertFalse(employee.isSet("name"));
        assertFalse(employee.isSet("SN"));
        assertFalse(employee.isSet("manager"));
        // containment relationship is broken
        //assertNull(employee.getContainer());
        //assertNull(employee.getContainmentProperty());

        // what about the deleted data object in the change summary?
        List<DataObject> changeList = (List<DataObject>)cs.getChangedDataObjects();
        Type t = typeHelper.getType("company.xsd", "EmployeeType");
        int i = find(t, changeList, 0);
        assertTrue(i >= 0);
        DataObject deleted = changeList.get(i);
        System.out.println("deleted == employee? " + (deleted == employee));
        assertTrue(cs.isDeleted(deleted));
        //assertNull(deleted.get("name"));
        //assertNull(deleted.get("SN"));
        //assertEquals(false, deleted.getBoolean("manager"));
        assertEquals("Mary Smith", deleted.get("name"));
        assertEquals("E0002", deleted.get("SN"));
        assertEquals(true, deleted.getBoolean("manager"));

        verifyEmployee(cs, deleted);
        verifyEmployee(cs, employee);
    }

    private void verifyEmployee(DataObject employee, int id)
    {
        // non-read-only properties are unset
        assertNull(employee.get("name"));
        assertNull(employee.get("SN"));
        assertNull(employee.get("manager")); // SDO type is BooleanObject, not Boolean
        assertFalse(employee.isSet("name"));
        assertFalse(employee.isSet("SN"));
        assertFalse(employee.isSet("manager"));
        // read-only property is not unset
        assertTrue(employee.isSet("id"));
        assertEquals(id, employee.getInt("id"));
    }

    private void verifyDepartment(DataObject department)
    {
        // non-read-only properties are unset
        assertNull(department.get("name"));
        assertNull(department.get("location"));
        assertEquals(0, department.getInt("number"));
        assertEquals(0, department.getList("employees").size());
        assertFalse(department.isSet("name"));
        assertFalse(department.isSet("location"));
        assertFalse(department.isSet("number"));
        assertFalse(department.isSet("employees"));
        // read-only property is not unset
        assertTrue(department.isSet("id"));
        assertEquals(2, department.getInt("id"));
    }

    /* test delete of data objects with read-only properties;
       also, test cascading delete
     */
    public void testDeleteWithReadOnlyProperties() throws Exception
    {
        System.out.println("testDeleteWithReadOnlyProperties()");
        DataObject company = getRootDataObjectWrapped("data", "company5.xml");
        DataObject department = company.getDataObject("departments[1]");
        assertNotNull(department);
        DataObject employee = company.getDataObject("departments[1]/employees[2]");
        assertNotNull(employee);
        assertEquals("Mary Smith", employee.get("name"));
        assertEquals("E0002", employee.get("SN"));
        Property p = employee.getInstanceProperty("manager");
        assertFalse(p.isReadOnly());
        Type t = typeHelper.getType("commonj.sdo/java", "BooleanObject");
        assertNotNull(t);
        assertEquals(t, p.getType());
        assertEquals(java.lang.Boolean.class, p.getType().getInstanceClass());
        assertEquals(Boolean.TRUE, employee.get(p));
        Property p1 = company.getInstanceProperty("id");
        Property p2 = department.getInstanceProperty("id");
        Property p3 = employee.getInstanceProperty("id");
        assertTrue(p1.isReadOnly());
        assertTrue(p2.isReadOnly());
        assertTrue(p3.isReadOnly());
        assertEquals(11, employee.getInt(p3));

        ChangeSummary cs = company.getChangeSummary();
        cs.beginLogging();

        employee.delete();
        verifyEmployee(employee, 11);

        cs.undoChanges();
        DataObject employee1 = department.getDataObject("employees[1]");
        DataObject employee2 = department.getDataObject("employees[2]");
        DataObject employee3 = department.getDataObject("employees[3]");
        department.delete();
        verifyDepartment(department);
        verifyEmployee(employee1, 7);
        verifyEmployee(employee2, 11);
        verifyEmployee(employee3, 19);

        cs.undoChanges();

        // delete data object contained in read-only property
        Property p4 = company.getInstanceProperty("registry");
        assertTrue(p4.isReadOnly());
        DataObject registry = company.getDataObject(p4);
        assertNotNull(registry);
        assertEquals(1234567890123456789L, registry.getLong("x/key"));
        assertEquals("xxxxxxxxxxxxxxxxxxx", registry.get("x/value"));
        assertEquals(8999999999999999999L, registry.getLong("y/key"));
        assertEquals("yyyyyyyyyyyyyyyyyyy", registry.get("y/value"));
        assertEquals(1000000000000000001L, registry.getLong("z/key"));
        assertEquals("zzzzzzzzzzzzzzzzzzz", registry.get("z/value"));

        registry.delete();
        System.out.println("after deleting registry:");
        assertFalse(registry.isSet("x"));
        assertFalse(registry.isSet("y"));
        assertNull(registry.get("x"));
        System.out.println("  x: key:   " + registry.get("x/key"));
        System.out.println("  x: value: " + registry.get("x/value"));
        assertNull(registry.get("x/key"));
        assertNull(registry.get("x/value"));
        assertNull(registry.get("y"));
        System.out.println("  y: key:   " + registry.get("y/key"));
        System.out.println("  y: value: " + registry.get("y/value"));
        assertNull(registry.get("y/key"));
        assertNull(registry.get("y/value"));
        // z is read-only
        assertTrue(registry.isSet("z"));
        DataObject z = registry.getDataObject("z");
        assertNotNull(z);
        // ... but its properties are not!
        assertFalse(z.isSet("key"));
        assertFalse(z.isSet("value"));
        System.out.println("  z:");
        System.out.println("     key:   " + z.get("key"));
        System.out.println("     value: " + z.get("value"));
        System.out.println("  z: key:   " + registry.get("z/key"));
        System.out.println("  z: value: " + registry.get("z/value"));
        assertEquals((long)0, z.get("key"));
        assertNull(z.get("value"));
        assertEquals((long)0, registry.get("z/key"));
        assertNull(registry.get("z/value"));
        // registry is not detached
        assertTrue(p4 == registry.getContainmentProperty());
        assertTrue(company == registry.getContainer());

        cs.undoChanges();
        // get registry again in case implementation restores a copy
        // of the registry instead of the original
        registry = company.getDataObject(p4);
        assertEquals(1234567890123456789L, registry.getLong("x/key"));
        assertEquals("xxxxxxxxxxxxxxxxxxx", registry.get("x/value"));
        assertEquals(8999999999999999999L, registry.getLong("y/key"));
        assertEquals("yyyyyyyyyyyyyyyyyyy", registry.get("y/value"));
        assertEquals(1000000000000000001L, registry.getLong("z/key"));
        assertEquals("zzzzzzzzzzzzzzzzzzz", registry.get("z/value"));

        // delete company
        company.delete();
        assertEquals(0, company.getList("departments").size());
        assertFalse(company.isSet("departments"));
        // read-only datatype property
        assertTrue(company.isSet(p1));
        assertEquals(1, company.getInt(p1));
        // read-only containment property
        assertTrue(company.isSet(p4));
        assertTrue(registry == company.get(p4));
        assertTrue(company == registry.getContainer());
        assertTrue(p4 == registry.getContainmentProperty());
        // deleting company recursively deletes contained data objects ...
        assertFalse(registry.isSet("x"));
        assertFalse(registry.isSet("y"));
        // ... including those contained by a read-only property
        assertTrue(registry.isSet("z"));
        z = registry.getDataObject("z"); // get z again
        assertNotNull(z);
        assertFalse(z.isSet("key"));
        assertFalse(z.isSet("value"));
    }

    private void verifyAddress(DataObject address)
    {
        assertFalse(address.isSet("location"));
        assertFalse(address.isSet("street"));
        assertFalse(address.isSet("city"));
        assertFalse(address.isSet("state"));
        assertFalse(address.isSet("zip"));
        assertNull(address.get("location"));
        assertNull(address.get("street"));
        assertNull(address.get("city"));
        assertNull(address.get("state"));
        assertNull(address.get("zip"));
    }

    private void verifyPhone(DataObject phone)
    {
        assertFalse(phone.isSet("location"));
        assertFalse(phone.isSet("value"));
        assertNull(phone.get("location"));
        assertNull(phone.get("value"));
    }


    public void testCascadingDelete() throws Exception
    {
        System.out.println("testCascadingDelete()");
        DataObject root = getRootDataObject("checkin", "employees.xml");
        DataObject emp = root.getDataObject("employee[2]");
        assertNotNull(emp);
        assertEquals("Sally Smith", emp.get("name"));
        DataObject address1 = emp.getDataObject("address[1]");
        assertNotNull(address1);
        assertEquals("home", address1.get("location"));
        DataObject address2 = emp.getDataObject("address[2]");
        assertNotNull(address2);
        assertEquals("work", address2.get("location"));
        DataObject phone1 = emp.getDataObject("phone[location='work']");
        assertNotNull(phone1);
        assertEquals("work", phone1.get("location"));
        assertEquals("(503)555-3856", phone1.get("value"));
        DataObject phone2 = emp.getDataObject("phone[location='home']");
        assertNotNull(phone2);
        assertEquals("home", phone2.get("location"));
        assertEquals("(503)555-6951", phone2.get("value"));
        DataObject phone3 = emp.getDataObject("phone[location='mobile']");
        assertNotNull(phone3);
        assertEquals("mobile", phone3.get("location"));
        assertEquals("(503)555-5152", phone3.get("value"));

        phone3.delete();
        verifyPhone(phone3);

        emp.delete();
        assertFalse(emp.isSet("name"));
        assertNull(emp.get("name"));
        assertFalse(emp.isSet("address"));
        assertEquals(0, emp.getList("address").size());
        assertFalse(emp.isSet("phone"));
        assertEquals(0, emp.getList("phone").size());
        verifyAddress(address1);
        verifyAddress(address2);
        verifyPhone(phone1);
        verifyPhone(phone2);
        verifyPhone(phone3);
    }
}
