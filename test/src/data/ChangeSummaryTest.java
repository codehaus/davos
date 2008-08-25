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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.io.StringWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
//import java.util.Iterator;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.Result;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;
//import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import org.xml.sax.InputSource;
import org.xml.sax.ContentHandler;

import javax.sdo.DataGraph;
import javax.sdo.DataObject;
import javax.sdo.ChangeSummary;
import javax.sdo.Type;
import javax.sdo.Property;
import javax.sdo.Sequence;
import javax.sdo.helper.XMLDocument;
import javax.sdo.helper.XMLHelper;
import javax.sdo.helper.XSDHelper;
import javax.sdo.helper.TypeHelper;
import javax.sdo.helper.DataFactory;
import javax.sdo.helper.CopyHelper;
import javax.sdo.helper.EqualityHelper;

import davos.sdo.Options;
import davos.sdo.impl.helpers.DataGraphHelper;
import davos.sdo.impl.helpers.XMLHelperImpl;

import junit.framework.Test;
import junit.framework.TestSuite;
import common.DataTest;
import marshal.XMLHelperTest;

/**
 * @author Wing Yew Poon
 */
public class ChangeSummaryTest extends DataTest
{
    public ChangeSummaryTest(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        
        TestSuite suite = new TestSuite();
        
        suite.addTest(new ChangeSummaryTest("testA0_1"));
        suite.addTest(new ChangeSummaryTest("testA0_2"));
        
        suite.addTest(new ChangeSummaryTest("testA1"));
        suite.addTest(new ChangeSummaryTest("testA2_1"));
        suite.addTest(new ChangeSummaryTest("testA2_2"));
        //suite.addTest(new ChangeSummaryTest("testA3"));
        suite.addTest(new ChangeSummaryTest("testA4"));
        suite.addTest(new ChangeSummaryTest("testA5"));
        
        suite.addTest(new ChangeSummaryTest("testE1"));
        suite.addTest(new ChangeSummaryTest("testE2"));
        suite.addTest(new ChangeSummaryTest("testE3"));
        suite.addTest(new ChangeSummaryTest("testE4"));
        suite.addTest(new ChangeSummaryTest("testE5"));
        suite.addTest(new ChangeSummaryTest("testE6"));
        suite.addTest(new ChangeSummaryTest("testE7"));
        suite.addTest(new ChangeSummaryTest("testE8"));
        
        suite.addTest(new ChangeSummaryTest("testB1"));
        suite.addTest(new ChangeSummaryTest("testB2"));
        suite.addTest(new ChangeSummaryTest("testB3"));
        suite.addTest(new ChangeSummaryTest("testB4"));
        suite.addTest(new ChangeSummaryTest("testB5"));
        suite.addTest(new ChangeSummaryTest("testB6"));
        suite.addTest(new ChangeSummaryTest("testB7"));
        suite.addTest(new ChangeSummaryTest("testB8"));
        suite.addTest(new ChangeSummaryTest("testB9"));
        suite.addTest(new ChangeSummaryTest("testB10"));
        suite.addTest(new ChangeSummaryTest("testB11"));
        
        suite.addTest(new ChangeSummaryTest("testC1"));
        suite.addTest(new ChangeSummaryTest("testC2"));
        suite.addTest(new ChangeSummaryTest("testC3"));
        suite.addTest(new ChangeSummaryTest("testC4"));
        suite.addTest(new ChangeSummaryTest("testC5"));
        suite.addTest(new ChangeSummaryTest("testC6"));
        suite.addTest(new ChangeSummaryTest("testC9"));
        suite.addTest(new ChangeSummaryTest("testC10"));
        suite.addTest(new ChangeSummaryTest("testC7"));
        suite.addTest(new ChangeSummaryTest("testC8"));
        suite.addTest(new ChangeSummaryTest("testC12"));
        suite.addTest(new ChangeSummaryTest("testC13"));
        suite.addTest(new ChangeSummaryTest("testC14"));
        suite.addTest(new ChangeSummaryTest("testC15"));
        suite.addTest(new ChangeSummaryTest("testC11"));
        
        suite.addTest(new ChangeSummaryTest("testCascadingDelete1"));
        suite.addTest(new ChangeSummaryTest("testCascadingDelete2"));
        
        suite.addTest(new ChangeSummaryTest("testD1"));
        suite.addTest(new ChangeSummaryTest("testD2"));
        
        suite.addTest(new ChangeSummaryTest("testModifyOpenContent"));
        suite.addTest(new ChangeSummaryTest("testDeleteOpenContent"));
        suite.addTest(new ChangeSummaryTest("testSetOpenContent"));
        suite.addTest(new ChangeSummaryTest("testDeleteAndSetOpenContent1"));
        suite.addTest(new ChangeSummaryTest("testDeleteAndSetOpenContent2"));
        suite.addTest(new ChangeSummaryTest("testSetAndDeleteOpenAttribute"));
        
        suite.addTest(new ChangeSummaryTest("testG1"));
        suite.addTest(new ChangeSummaryTest("testG2"));
        suite.addTest(new ChangeSummaryTest("testG3"));
        suite.addTest(new ChangeSummaryTest("testG4"));
        suite.addTest(new ChangeSummaryTest("testF"));
        
        suite.addTest(new ChangeSummaryTest("testLoadAndSaveDOM"));
        suite.addTest(new ChangeSummaryTest("testLoadAndSaveSAX"));
        suite.addTest(new ChangeSummaryTest("testLoadAndSaveStream"));
        suite.addTest(new ChangeSummaryTest("testLoadAndSaveStAX1"));
        //suite.addTest(new ChangeSummaryTest("testLoadAndSaveStAX2"));
        suite.addTest(new ChangeSummaryTest("testLoadAndSaveStAX3"));
        
        suite.addTest(new ChangeSummaryTest("testFromUnsetToSetNotNull"));
        suite.addTest(new ChangeSummaryTest("testFromUnsetToSetNull"));
        suite.addTest(new ChangeSummaryTest("testFromSetNullToUnset"));
        suite.addTest(new ChangeSummaryTest("testFromSetNullToSetNotNull"));
        suite.addTest(new ChangeSummaryTest("testFromSetNotNullToUnset"));
        suite.addTest(new ChangeSummaryTest("testFromSetNotNullToSetNull"));
        suite.addTest(new ChangeSummaryTest("testFromUnsetToUnset"));
        suite.addTest(new ChangeSummaryTest("testMultiplePropertiesUnset"));
        
        suite.addTest(new ChangeSummaryTest("testFromEmptyString"));
        suite.addTest(new ChangeSummaryTest("testToEmptyString"));
        
        suite.addTest(new ChangeSummaryTest("testModifySimpleListType1"));
        suite.addTest(new ChangeSummaryTest("testModifySimpleListType2"));
        
        suite.addTest(new ChangeSummaryTest("testModifyListOfSimpleType1a"));
        suite.addTest(new ChangeSummaryTest("testModifyListOfSimpleType1b"));
        suite.addTest(new ChangeSummaryTest("testModifyListOfSimpleType1c"));
        suite.addTest(new ChangeSummaryTest("testModifyListOfSimpleType2a"));
        suite.addTest(new ChangeSummaryTest("testModifyListOfSimpleType2b"));
        suite.addTest(new ChangeSummaryTest("testModifyListOfSimpleType2c"));
        suite.addTest(new ChangeSummaryTest("testModifyListOfSimpleType2d"));
        suite.addTest(new ChangeSummaryTest("testModifyListOfSimpleType2e"));
        suite.addTest(new ChangeSummaryTest("testModifyListOfSimpleType2f"));
        suite.addTest(new ChangeSummaryTest("testModifyListOfSimpleType2g"));
        
        suite.addTest(new ChangeSummaryTest("testCopy1"));
        suite.addTest(new ChangeSummaryTest("testCopy2"));
        
        // or
        //TestSuite suite = new TestSuite(ChangeSummaryTest.class);
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

    private static XMLHelper xmlHelper = context.getXMLHelper();
    private static XSDHelper xsdHelper = context.getXSDHelper();
    private static TypeHelper typeHelper = context.getTypeHelper();
    private static DataFactory dataFactory = context.getDataFactory();
    private static CopyHelper copyHelper = context.getCopyHelper();
    private static EqualityHelper equalityHelper = context.getEqualityHelper();

    private static final Type DATAGRAPHTYPE = typeHelper.getType("commonj.sdo", "DataGraphType");

    private DataObject getCompany() throws Exception
    {
        return getRootDataObjectWrapped("checkin", "company.xml");
    }

    private void changeCompany(DataObject company)
    {
        List departments = company.getList("departments");
        DataObject department = (DataObject) departments.get(0);
        DataObject newEmployee = department.createDataObject("employees");
        newEmployee.set("name", "Al Smith");
        newEmployee.set("SN", "E0004");
        newEmployee.setBoolean("manager", true);
        company.set("employeeOfTheMonth", newEmployee.get("SN"));
    }

    private void changeCompanyName(DataObject company)
    {
        company.set("name", "MegaCorp");
        assertEquals("MegaCorp", company.get("name"));
    }

    private void changeDepartmentLocation(DataObject company)
    {
        List departments = company.getList("departments");
        DataObject department = (DataObject) departments.get(0);
        department.set("location", "NJ");
        assertEquals("NJ", company.get("departments[1]/location"));
    }

    private void expandCompany(DataObject company)
    {
        DataObject newDepartment = company.createDataObject("departments");
        newDepartment.set("name", "Services");
        newDepartment.set("location", "CT");
        newDepartment.set("number", 456);
        DataObject newEmployee = newDepartment.createDataObject("employees");
        newEmployee.set("name", "Joshua Klein");
        newEmployee.set("SN", "E0005");
        newEmployee.setBoolean("manager", true);
    }

    private void renameEmployee(DataObject company, int i, String name)
    {
        String path = "departments[1]/employees[" + i + "]";
        DataObject employee = (DataObject)company.get(path);
        assertNotNull(employee);
        employee.set("name", name);
    }

    private void removeEmployee(DataObject company, int i)
    {
        List employees = company.getList("departments[1]/employees");
        assertNotNull(employees);
        assertTrue(employees.size() > i - 1);
        employees.remove(i - 1);
    }

    private void detachEmployee(DataObject company, int i)
    {
        String path = "departments[1]/employees[" + i + "]";
        DataObject employee = (DataObject)company.get(path);
        assertNotNull(employee);
        //assertEquals("Mary Smith", employee.getString("name"));
        employee.detach();
    }

    private void deleteEmployee(DataObject company, int i)
    {
        String path = "departments[1]/employees[" + i + "]";
        DataObject employee = (DataObject)company.get(path);
        assertNotNull(employee);
        //assertEquals("Mary Smith", employee.getString("name"));
        employee.delete();
    }

    private int findCompany(List dataObjects, int index)
    {
        Type t = typeHelper.getType("company.xsd", "CompanyType");
        return find(t, dataObjects, index);
    }

    private int findDepartment(List dataObjects, int index)
    {
        Type t = typeHelper.getType("company.xsd", "DepartmentType");
        return find(t, dataObjects, index);
    }

    private int findEmployee(List dataObjects, int index)
    {
        Type t = typeHelper.getType("company.xsd", "EmployeeType");
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
        assertEquals("E0002", company.get("employeeOfTheMonth"));
        List departments = company.getList("departments");
        assertEquals(1, departments.size());
        DataObject department = (DataObject) departments.get(0);
        assertEquals("NY", department.get("location"));
        assertEquals(123, department.getInt("number"));
        List employees = department.getList("employees");
        assertEquals(3, employees.size());
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
        // exactly one of isCreated, isDeleted, or isModified can be true
        assertTrue(c.isModified(c1)); 
        assertFalse(c.isCreated(c1));
        assertFalse(c.isDeleted(c1));
        i = findDepartment(changeList);
        assertTrue(i >= 0);
        DataObject c2 = (DataObject)changeList.get(i);
        assertTrue(c.isModified(c2));
        assertFalse(c.isCreated(c2));
        assertFalse(c.isDeleted(c2));
        i = findEmployee(changeList);
        assertTrue(i >= 0);
        DataObject c3 = (DataObject)changeList.get(i);
        assertTrue(c.isCreated(c3));
        assertFalse(c.isDeleted(c3));
        assertFalse(c.isModified(c3));
        // created data object (new employee) has no old values
        List oldValues = c.getOldValues(c3);
        assertNotNull(oldValues);
        assertEquals(0, oldValues.size());
        // modified data object (company) has old values
        oldValues = c.getOldValues(c1);
        assertEquals(1, oldValues.size());
        ChangeSummary.Setting oldValue = (ChangeSummary.Setting)oldValues.get(0);
        assertEquals("employeeOfTheMonth", oldValue.getProperty().getName());
        assertEquals("E0002", oldValue.getValue());
        assertTrue(oldValue.isSet());
        // another modified data object (department)
        oldValues = c.getOldValues(c2);
        assertEquals(1, oldValues.size());
        oldValue = (ChangeSummary.Setting)oldValues.get(0);
        // "employees", a many-valued property
        assertEquals("employees", oldValue.getProperty().getName());
        Object oldEmployees = oldValue.getValue();
        // a List, containing 3 employees
        assertTrue(oldEmployees instanceof List);
        assertEquals(3, ((List)oldEmployees).size());
        assertEquals("John Jones", ((DataObject)((List)oldEmployees).get(0)).get("name"));
        assertEquals("Mary Smith", ((DataObject)((List)oldEmployees).get(1)).get("name"));
        assertEquals("Jane Doe", ((DataObject)((List)oldEmployees).get(2)).get("name"));
        // true
        assertTrue(oldValue.isSet());
    }

    /* verify the ChangeSummary after changeCompanyName() plus 
       changeCompany() */
    private void verifyCompanyChange2(ChangeSummary c)
    {
        List changeList = c.getChangedDataObjects();
        // company modified (name and employeeOfTheMonth, department modified, 
        // employee created
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
        assertEquals(2, oldValues.size());
        ChangeSummary.Setting oldValue = 
            c.getOldValue(c1, c1.getInstanceProperty("name"));
        assertTrue(oldValue.isSet());
        assertEquals("ACME", oldValue.getValue());
        oldValue = c.getOldValue(c1, c1.getInstanceProperty("employeeOfTheMonth"));
        assertTrue(oldValue.isSet());
        assertEquals("E0002", oldValue.getValue());
        // another modified data object (department)
        oldValues = c.getOldValues(c2);
        assertEquals(1, oldValues.size());
        oldValue = (ChangeSummary.Setting)oldValues.get(0);
        // "employees", a many-valued property
        assertEquals("employees", oldValue.getProperty().getName());
        // a List, containing 3 employees
        Object oldEmployees = oldValue.getValue();
        assertTrue(oldEmployees instanceof List);
        assertEquals(3, ((List)oldEmployees).size());
        // true
        assertTrue(oldValue.isSet());
    }

    private int find(String propertyName, List<ChangeSummary.Setting> settings)
    {
        int i;
        boolean found = false;
        for (i = 0; i < settings.size(); i++)
        {
            ChangeSummary.Setting setting = settings.get(i);
            Property property = setting.getProperty();
            assertNotNull(property);
            if (property.getName().equals(propertyName))
            {
                found = true;
                break;
            }
        }
        if (found)
            return i;
        else
            return -1;
    }

    /* verify the ChangeSummary after changeCompany() plus 
       changeDepartmentLocation() */
    private void verifyCompanyChange3(ChangeSummary c)
    {
        List changeList = c.getChangedDataObjects();
        // company modified (however, name change not in change log!),
        // department modified, employee created
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
        ChangeSummary.Setting oldValue = 
            c.getOldValue(c1, c1.getInstanceProperty("name"));
        assertNull(oldValue); // name change not in change log
        oldValue = c.getOldValue(c1, c1.getInstanceProperty("employeeOfTheMonth"));
        assertTrue(oldValue.isSet());
        assertEquals("E0002", oldValue.getValue());
        // modified data object (department)
        oldValues = c.getOldValues(c2);
        assertEquals(2, oldValues.size()); // location, and employees
        i = find("location", oldValues);
        assertTrue(i >= 0);
        oldValue = (ChangeSummary.Setting)oldValues.get(i);
        assertEquals("NY", oldValue.getValue());
        assertTrue(oldValue.isSet());
        i = find("employees", oldValues);
        assertTrue(i >= 0);
        oldValue = (ChangeSummary.Setting)oldValues.get(i);
        assertEquals("employees", oldValue.getProperty().getName());
        // a List, containing 3 employees
        Object oldEmployees = oldValue.getValue();
        assertTrue(oldEmployees instanceof List);
        assertEquals(3, ((List)oldEmployees).size());
        // true
        assertTrue(oldValue.isSet());
    }

    /* verify the ChangeSummary after remove/detach/deleteEmployee() */
    private void verifyCompanyChange4(ChangeSummary c)
    {
        List changeList = c.getChangedDataObjects();
        // department modified, employee deleted
        assertEquals(2, changeList.size());
        int i = findDepartment(changeList);
        assertTrue(i >= 0);
        DataObject c1 = (DataObject)changeList.get(i);
        assertTrue(c.isModified(c1));
        i = findEmployee(changeList);
        assertTrue(i >= 0);
        DataObject c2 = (DataObject)changeList.get(i);
        assertTrue(c.isDeleted(c2));
    }

    private void _printOldValues(List<ChangeSummary.Setting> oldValues)
    {
        System.out.println("number of old values: " + oldValues.size());
        for (ChangeSummary.Setting setting : oldValues)
        {
            System.out.println(setting.getProperty().getName() + ": " +
                               (setting.isSet() ? setting.getValue() : "[not set]"));
        }
    }

    private void verifyEmployee(ChangeSummary c, DataObject employee)
    {
        assertFalse(c.isModified(employee));
        assertTrue(c.isCreated(employee) || c.isDeleted(employee));
        if (c.isCreated(employee))
        {
            System.out.println("created: " + employee.get("name"));
            assertFalse(c.isDeleted(employee));
            assertEquals("Al Smith", employee.get("name"));
        }
        else // c.isDeleted(employee)
        {
            assertTrue(c.isDeleted(employee));
            System.out.println("deleted: " + employee.get("name"));
            assertFalse(c.isCreated(employee));
            List<ChangeSummary.Setting> oldValues = 
                (List<ChangeSummary.Setting>)c.getOldValues(employee);
            assertNotNull(oldValues);
            assertEquals(3, oldValues.size());
            _printOldValues(oldValues);
            ChangeSummary.Setting oldValue = 
                c.getOldValue(employee, employee.getInstanceProperty("name"));
            assertNotNull(oldValue);
            assertEquals("Mary Smith", oldValue.getValue());
        }
    }

    private void verifyEmployee2(ChangeSummary c, DataObject employee,
                                 DataObject oldEmployee, DataObject newEmployee)
    {
        assertFalse(c.isModified(employee));
        assertFalse(c.isModified(oldEmployee));
        assertFalse(c.isModified(newEmployee));
        assertFalse(c.isCreated(oldEmployee));
        assertTrue(c.isCreated(newEmployee));
        assertTrue(c.isDeleted(oldEmployee));
        assertFalse(c.isDeleted(newEmployee));
        assertTrue(c.isCreated(employee) || c.isDeleted(employee));
        if (c.isCreated(employee))
        {
            System.out.println("created: " + employee.get("name"));
            assertFalse(c.isDeleted(employee));
            System.out.println("employee == new employee? " + (employee == newEmployee));
            //assertEquals(newEmployee, employee);
        }
        else // c.isDeleted(employee)
        {
            assertTrue(c.isDeleted(employee));
            System.out.println("deleted: " + employee.get("name"));
            assertFalse(c.isCreated(employee));
            System.out.println("employee == old employee? " + (employee == oldEmployee));
            List<ChangeSummary.Setting> oldValues = 
                (List<ChangeSummary.Setting>)c.getOldValues(employee);
            assertNotNull(oldValues);
            _printOldValues(oldValues);
            oldValues = c.getOldValues(oldEmployee);
            _printOldValues(oldValues);
        }
    }

    private void verifyEmployee3(ChangeSummary c, DataObject employee)
    {
        assertFalse(c.isCreated(employee));
        assertFalse(c.isModified(employee));
        assertTrue(c.isDeleted(employee));
        List<ChangeSummary.Setting> oldValues = 
            (List<ChangeSummary.Setting>)c.getOldValues(employee);
        assertNotNull(oldValues);
        //assertEquals(4, oldValues.size()); // FAILS - 0
        _printOldValues(oldValues);
        /*
        ChangeSummary.Setting oldName = 
            c.getOldValue(employee, employee.getInstanceProperty("name"));
        //assertNotNull(oldName); // FAILS
        assertEquals("Mary Smith", oldName.getValue()); // NPE
        //assertEquals("Mary Smith", employee.get("name")); // this succeeds!
        ChangeSummary.Setting oldSN = 
            c.getOldValue(employee, employee.getInstanceProperty("SN"));
        assertNotNull(oldSN);
        assertEquals("E0002", oldSN.getValue());
        ChangeSummary.Setting oldManager = 
            c.getOldValue(employee, employee.getInstanceProperty("manager"));
        assertNotNull(oldManager);
        assertEquals(Boolean.TRUE, oldManager.getValue());
        ChangeSummary.Setting oldId = 
            c.getOldValue(employee, employee.getInstanceProperty("id"));
        assertNotNull(oldId);
        assertEquals(new Integer(11), oldId.getValue());
        */
    }

    /* verify the ChangeSummary after changeCompany() plus
       remove/detach/deleteEmployee() */
    private void verifyCompanyChange5(ChangeSummary c)
    {
        System.out.println("in verifyCompanyChange5()");
        List changeList = c.getChangedDataObjects();
        /*
        System.out.println(changeList.size());
        for (Iterator iter = changeList.iterator(); iter.hasNext(); )
        {
            DataObject dobj = (DataObject)iter.next();
            System.out.println(dobj.getType().getName());
            System.out.println("created? " + c.isCreated(dobj));
            System.out.println("modified? " + c.isModified(dobj));
            System.out.println("deleted? " + c.isDeleted(dobj));
            if (dobj.getType().getName().equals("EmployeeType"))
            {
                System.out.println(dobj.getString("name"));
                if (c.isModified(dobj))
                {
                    ChangeSummary.Setting oldValue = 
                        c.getOldValue(dobj, dobj.getInstanceProperty("name"));
                    System.out.println(oldValue.getValue());
                }
            }
        }
        */
        // company modified, department modified, employee created, employee deleted
        assertEquals(4, changeList.size());
        int i = findCompany(changeList);
        assertTrue(i >= 0);
        DataObject c1 = (DataObject)changeList.get(i);
        assertTrue(c.isModified(c1));
        assertFalse(c.isCreated(c1));
        assertFalse(c.isDeleted(c1));
        i = findDepartment(changeList);
        assertTrue(i >= 0);
        DataObject c2 = (DataObject)changeList.get(i);
        assertTrue(c.isModified(c2));
        assertFalse(c.isCreated(c2));
        assertFalse(c.isDeleted(c2));
        i = findEmployee(changeList);
        assertTrue(i >= 0);
        DataObject c3 = (DataObject)changeList.get(i);
        verifyEmployee(c, c3);
        int j = findEmployee(changeList, i+1);
        assertTrue(j > i);
        DataObject c4 = (DataObject)changeList.get(j);
        verifyEmployee(c, c4);
    }

    /* verify the ChangeSummary after changeCompany() plus
       remove/detach/deleteEmployee() */
    private void verifyCompanyWithCSChange(ChangeSummary c)
    {
        System.out.println("in verifyCompanyWithCSChange()");
        List changeList = c.getChangedDataObjects();
        /*
        System.out.println(changeList.size());
        for (Iterator iter = changeList.iterator(); iter.hasNext(); )
        {
            DataObject dobj = (DataObject)iter.next();
            System.out.println(dobj.getType().getName());
            System.out.println("created? " + c.isCreated(dobj));
            System.out.println("modified? " + c.isModified(dobj));
            System.out.println("deleted? " + c.isDeleted(dobj));
            if (dobj.getType().getName().equals("EmployeeType"))
            {
                System.out.println(dobj.getString("name"));
                if (c.isModified(dobj))
                {
                    ChangeSummary.Setting oldValue = 
                        c.getOldValue(dobj, dobj.getInstanceProperty("name"));
                    System.out.println(oldValue.getValue());
                }
            }
        }
        */
        // company modified, department modified, employee created, employee deleted
        assertEquals(4, changeList.size());
        String uri = "company_with_cs.xsd";
        Type cT = typeHelper.getType(uri, "CompanyType");
        int i = find(cT, changeList, 0);
        assertTrue(i >= 0);
        DataObject c1 = (DataObject)changeList.get(i);
        assertTrue(c.isModified(c1));
        Type dT = typeHelper.getType(uri, "DepartmentType");
        i = find(dT, changeList, 0);
        assertTrue(i >= 0);
        DataObject c2 = (DataObject)changeList.get(i);
        assertTrue(c.isModified(c2));
        Type eT = typeHelper.getType(uri, "EmployeeType");
        i = find(eT, changeList, 0);
        assertTrue(i >= 0);
        DataObject c3 = (DataObject)changeList.get(i);
        verifyEmployee(c, c3);
        int j = find(eT, changeList, i+1);
        assertTrue(j > i);
        DataObject c4 = (DataObject)changeList.get(j);
        verifyEmployee(c, c4);
    }

    /* delete topmost data object */
    public void testA0_1() throws Exception
    {
        DataObject company = getCompany();
        DataGraph dataGraph = company.getDataGraph();
        ChangeSummary c = company.getChangeSummary();
        c.beginLogging();

        company.detach();
        saveDataGraph(dataGraph, new File(dir, "company_dg0_1a.xml"));
        compareXMLFiles(getResourceFile("data", "company_dg8.xml"), 
                        new File(dir, "company_dg0_1a.xml"));
        
        c.undoChanges();
        saveDataGraph(dataGraph, new File(dir, "company_dg0_1b.xml"));
        compareXMLFiles(getResourceFile("data", "company_dg7.xml"), 
                        new File(dir, "company_dg0_1b.xml"));
    }

    /* delete topmost data object */
    public void testA0_2() throws Exception
    {
        File f = getResourceFile("data", "company_dg0.xml");
        InputStream in = new FileInputStream(f);
        XMLDocument doc = xmlHelper.load(in);
        DataObject datagraph = doc.getRootObject();
        in.close();
        DataObject company = datagraph.getDataObject("company");

        ChangeSummary c = company.getChangeSummary();
        c.beginLogging();

        company.detach();
        OutputStream out = new FileOutputStream(new File(dir, "company_dg0_2a.xml"));
        doc = xmlHelper.createDocument(datagraph, "commonj.sdo", "datagraph");
        doc.setXMLDeclaration(false);
        xmlHelper.save(doc, out, new Options().setSavePrettyPrint());
        out.close();
        compareXMLFiles(getResourceFile("data", "company_dg8.xml"), 
                        new File(dir, "company_dg0_2a.xml"));

        c.undoChanges();
        out = new FileOutputStream(new File(dir, "company_dg0_2b.xml"));
        doc = xmlHelper.createDocument(datagraph, "commonj.sdo", "datagraph");
        doc.setXMLDeclaration(false);
        xmlHelper.save(doc, out, new Options().setSavePrettyPrint());
        out.close();
        compareXMLFiles(getResourceFile("data", "company_dg7.xml"), 
                        new File(dir, "company_dg0_2b.xml"));
    }

    /* basic scenario, starting with xml instance of company */
    public void testA1() throws Exception
    {
        DataObject company = getCompany();
        ChangeSummary c = company.getChangeSummary();
        c.beginLogging();

        changeCompany(company);

        saveDataGraph(company.getDataGraph(), new File(dir, "company_dg1.xml"));
        compareXMLFiles(getResourceFile("data", "company_dg.xml"), 
                        new File(dir, "company_dg1.xml"));
    }

    /* basic scenario, starting with data graph containing company instance */
    public void testA2_1() throws Exception
    {
        File f = getResourceFile("data", "company_dg0.xml");
        InputStream in = new FileInputStream(f);
        XMLDocument doc = xmlHelper.load(in);
        DataObject datagraph = doc.getRootObject();
        in.close();
        assertNotNull(datagraph);
        System.out.println("testA2_1()");
        System.out.println("root of datagraph:");
        System.out.println("  " + doc.getRootElementURI());
        System.out.println("  " + doc.getRootElementName());
        System.out.println("SDO type of datagraph:");
        System.out.println("  " + datagraph.getType().getURI());
        System.out.println("  " + datagraph.getType().getName());
        DataObject company = datagraph.getDataObject("company");
        assertNotNull(company);

        ChangeSummary c = company.getChangeSummary();
        c.beginLogging();
        changeCompany(company);
        
        OutputStream out = new FileOutputStream(new File(dir, "company_dg2_1.xml"));
        doc = xmlHelper.createDocument(datagraph, "commonj.sdo", "datagraph");
        doc.setXMLDeclaration(false);
        xmlHelper.save(doc, out, new Options().setSavePrettyPrint());
        out.close();
        //saveDataGraph(company.getDataGraph(), new File(dir, "company_dg2_1b.xml"));
        compareXMLFiles(getResourceFile("data", "company_dg.xml"), 
                        new File(dir, "company_dg2_1.xml"));
    }

    /* basic scenario, starting with data graph containing company instance */
    public void testA2_2() throws Exception
    {
        DataGraph dataGraph = getDataGraph("data", "company_dg0.xml");
        assertNotNull(dataGraph);
        DataObject rootObject = dataGraph.getRootObject();
        assertNotNull(rootObject);
        DataObject company = rootObject.getDataObject("company");

        ChangeSummary c = dataGraph.getChangeSummary();
        ChangeSummary c2 = company.getChangeSummary();
        assertTrue(c == c2);
        c.beginLogging();
        //c2.beginLogging();
        changeCompany(company);

        saveDataGraph(dataGraph, new File(dir, "company_dg2_2.xml"));
        compareXMLFiles(getResourceFile("data", "company_dg.xml"), 
                        new File(dir, "company_dg2_2.xml"));
    }

    /* set many-valued property to list */
    public void testA3() throws Exception
    {
        DataObject company = getCompany();
        ChangeSummary c = company.getChangeSummary();
        c.beginLogging();

        // add another employee
        List departments = company.getList("departments");
        DataObject department = (DataObject)departments.get(0);
        List employees = (List)department.get("employees");
        DataObject employee1 = (DataObject)employees.get(0);
        Type employeeType = employee1.getType();
        DataObject newEmployee = dataFactory.create(employeeType);
            //department.createDataObject("employees");
        newEmployee.set("name", "Al Smith");
        newEmployee.set("SN", "E0004");
        newEmployee.setBoolean("manager", true);
        employees.add(newEmployee);
        // the next line is redundant, since employees is a live list and is 
        // modified in place; however, we could be setting "employees" to any
        // list, we just happen to set it to one we already have at hand.
        department.set("employees", employees);
        saveDataGraph(company.getDataGraph(), new File(dir, "company_dg3a.xml"));
        //compareXMLFiles(getResourceFile("data", "company_dg3a.xml"), 
        //                new File(dir, "company_dg3a.xml"));
    }

    /* set data object property to a data object 
       - property is many-valued, data object is set using positional path.
       This is comparable to modifying a list in place.
       At the same time, this is in lieu of modifying the data object in place
       (by modifying some data type property).
    */
    public void testA4() throws Exception
    {
        DataObject company = getCompany();
        DataObject copy = copyHelper.copy(company);
        ChangeSummary c = company.getChangeSummary();
        c.beginLogging();

        // modify first employee
        List departments = company.getList("departments");
        DataObject department = (DataObject)departments.get(0);
        List employees = (List)department.get("employees");
        DataObject employee1 = (DataObject)employees.get(0);
        DataObject clone = copyHelper.copy(employee1);
        clone.setBoolean("manager", true);
        assertTrue(employee1 == department.get("employees[1]"));
        department.set("employees[1]", clone);
        assertTrue(clone == department.get("employees[1]"));
        saveDataGraph(company.getDataGraph(), new File(dir, "company_dg4.xml"));
        compareXMLFiles(getResourceFile("data", "company_dg4.xml"), 
                        new File(dir, "company_dg4.xml"));
        c.undoChanges();
        assertTrue(equalityHelper.equal(company, copy));
    }

    /* set data object property to a data object 
       - property is single-valued.
       This is in lieu of modifying the data object in place (by modifying
       some data type property).
     */
    public void testA5() throws Exception
    {
        DataObject employee = getRootDataObjectWrapped("data", "employee.xml");
        DataObject address = employee.getDataObject("address");
        DataObject address2 = copyHelper.copy(address);
        assertEquals("900 Aurora Ave.", employee.get("address/street"));
        assertEquals("900 Aurora Ave.", address.get("street"));
        assertEquals("900 Aurora Ave.", address2.get("street"));
        ChangeSummary c = employee.getChangeSummary();
        c.beginLogging();
        address.set("street", "1215 Elm St.");
        saveDataGraph(employee.getDataGraph(), new File(dir, "employee_dg.xml"));
        compareXMLFiles(getResourceFile("data", "employee_dg.xml"), 
                        new File(dir, "employee_dg.xml"));

        c.undoChanges();
        Writer out = new StringWriter();
        saveDataObject(employee, "http://sdo/test/employee", "employee", out);
        System.out.println(out.toString());
        out.close();
        assertEquals("900 Aurora Ave.", employee.get("address/street"));
        assertEquals("900 Aurora Ave.", address.get("street"));
        address.set("street", "1215 Elm St.");
        employee.set("address", address);
        saveDataGraph(employee.getDataGraph(), new File(dir, "employee_dg2.xml"));
        compareXMLFiles(getResourceFile("data", "employee_dg2.xml"), 
                        new File(dir, "employee_dg2.xml"));

        c.undoChanges();
        out = new StringWriter();
        saveDataObject(employee, "http://sdo/test/employee", "employee", out);
        System.out.println(out.toString());
        out.close();
        assertEquals("900 Aurora Ave.", employee.get("address/street"));
        address2.set("street", "1215 Elm St.");
        employee.set("address", address2);
        saveDataGraph(employee.getDataGraph(), new File(dir, "employee_dg3.xml"));
        compareXMLFiles(getResourceFile("data", "employee_dg2.xml"), 
                        new File(dir, "employee_dg3.xml"));
    }

    /* activate logging, query logging status, 
       deactivate logging, query logging status */
    public void testB1() throws Exception
    {
        DataObject company = getCompany();
        ChangeSummary c = company.getChangeSummary();
        c.beginLogging();
        assertTrue(c.isLogging());
        c.endLogging();
        assertFalse(c.isLogging());
        
        changeCompany(company);
        
        // there are no changes since logging was off
        List changeList = c.getChangedDataObjects();
        assertNotNull(changeList);
        assertEquals(0, changeList.size());
    }

    /* activate logging, make changes, query changes */
    public void testB2() throws Exception
    {
        DataObject company = getCompany();
        ChangeSummary c = company.getChangeSummary();
        c.beginLogging();
        changeCompany(company);
        verifyCompanyChange(c);
    }

    /* activate logging, make changes, deactivate logging, query changes */
    public void testB3() throws Exception
    {
        DataObject company = getCompany();
        ChangeSummary c = company.getChangeSummary();
        c.beginLogging();
        changeCompany(company);
        c.endLogging();
        verifyCompanyChange(c);
    }

    /* activate logging, make changes, 
       deactivate logging, make further changes, query changes */
    public void testB4() throws Exception
    {
        DataObject company = getCompany();
        ChangeSummary c = company.getChangeSummary();
        c.beginLogging();
        changeCompany(company);
        c.endLogging();
        changeCompanyName(company);
        verifyCompanyChange(c);
    }

    /* activate logging, make changes, query changes,
       make further changes, query changes */
    public void testB5() throws Exception
    {
        DataObject company = getCompany();
        ChangeSummary c = company.getChangeSummary();
        c.beginLogging();
        changeCompany(company);
        verifyCompanyChange(c);
        // name hasn't changed
        ChangeSummary.Setting oldName = 
            c.getOldValue(company, company.getInstanceProperty("name"));
        assertNull(oldName);
        changeCompanyName(company);
        // name has changed
        oldName = c.getOldValue(company, company.getInstanceProperty("name"));
        assertNotNull(oldName);
        assertTrue(oldName.isSet());
        assertEquals("ACME", oldName.getValue());
    }

    /* make changes, activate logging, query changes,
       make further changes, query changes */
    public void testB6() throws Exception
    {
        DataObject company = getCompany();
        ChangeSummary c = company.getChangeSummary();
        changeCompanyName(company);
        c.beginLogging();
        changeCompany(company);
        verifyCompanyChange(c);
        // make additional change
        changeDepartmentLocation(company);
        // verify further change
        verifyCompanyChange3(c);
        saveDataGraph(company.getDataGraph(), new File(dir, "company_dg3.xml"));
        compareXMLFiles(getResourceFile("data", "company_dg3.xml"), 
                        new File(dir, "company_dg3.xml"));
    }

    /* activate logging, make changes, activate logging again,
       make other changes, query changes */
    public void testB7() throws Exception
    {
        DataObject company = getCompany();
        ChangeSummary c = company.getChangeSummary();
        c.beginLogging();
        changeCompanyName(company);
        c.beginLogging(); // no-op
        changeCompany(company);
        verifyCompanyChange2(c);
    }

    /* activate logging, make changes, undo changes, query logging status,
       make same changes, query changes */
    public void testB8() throws Exception
    {
        DataObject company = getCompany();
        ChangeSummary c = company.getChangeSummary();
        c.beginLogging();
        changeCompany(company);
        c.undoChanges();
        // check change summary
        assertTrue(c.isLogging());
        List changeList = c.getChangedDataObjects();
        assertNotNull(changeList);
        assertEquals(0, changeList.size());
        verifyCompany(company);
        // redo
        changeCompany(company);
        verifyCompanyChange(c);
    }

    /* activate logging, make changes, undo changes, query logging status,
       make other changes, query changes */
    public void testB9() throws Exception
    {
        DataObject company = getCompany();
        ChangeSummary c = company.getChangeSummary();
        c.beginLogging();
        changeCompanyName(company);
        c.undoChanges();
        verifyCompany(company);
        // check change summary
        assertTrue(c.isLogging());
        changeCompany(company);
        verifyCompanyChange(c);
    }

    /* begin logging, make changes, end logging,
       begin logging again, make other changes, query changes */
    public void testB10() throws Exception
    {
        DataObject company = getCompany();
        ChangeSummary c = company.getChangeSummary();
        c.beginLogging();
        changeCompanyName(company);
        c.endLogging();
        // check change summary
        assertFalse(c.isLogging());
        c.beginLogging();
        assertTrue(c.isLogging());
        // check change summary (should be cleared)
        List changeList = c.getChangedDataObjects();
        assertNotNull(changeList);
        assertEquals(0, changeList.size());
        // however, company name change should persist
        assertEquals("MegaCorp", company.get("name"));
        changeCompany(company);
        verifyCompanyChange(c);
    }

    /* unmarshal datagraph where change summary has logging on, 
       make further changes */
    public void testB11() throws Exception
    {
        DataObject datagraph = getRootDataObject("data", "company_dg.xml");
        DataObject company = datagraph.getDataObject("company");
        ChangeSummary c = company.getChangeSummary();
        assertTrue(c.isLogging());
        verifyCompanyChange(c);
        // name hasn't changed
        ChangeSummary.Setting oldName = 
            c.getOldValue(company, company.getInstanceProperty("name"));
        assertNull(oldName);
        changeCompanyName(company);
        // name has changed
        oldName = c.getOldValue(company, company.getInstanceProperty("name"));
        assertNotNull(oldName);
        assertTrue(oldName.isSet());
        assertEquals("ACME", oldName.getValue());
        File f = new File(dir, "company_dg1b.xml");
        saveDataObject(datagraph, "commonj.sdo", "datagraph", f);
        compareXMLFiles(getResourceFile("data", "company_dg1b.xml"), f);
    }

    /* delete an employee by removing from list */
    public void testC1() throws Exception
    {
        DataObject company = getCompany();
        ChangeSummary c = company.getChangeSummary();
        c.beginLogging();
        removeEmployee(company, 1);
        verifyCompanyChange4(c);
        saveDataGraph(company.getDataGraph(), new File(dir, "company_dg4_1a.xml"));
        compareXMLFiles(getResourceFile("data", "company_dg4a.xml"), 
                        new File(dir, "company_dg4_1a.xml"));
        c.undoChanges();
        removeEmployee(company, 2);
        verifyCompanyChange4(c);
        saveDataGraph(company.getDataGraph(), new File(dir, "company_dg4_1b.xml"));
        compareXMLFiles(getResourceFile("data", "company_dg4b.xml"), 
                        new File(dir, "company_dg4_1b.xml"));
        c.undoChanges();
        removeEmployee(company, 3);
        verifyCompanyChange4(c);
        saveDataGraph(company.getDataGraph(), new File(dir, "company_dg4_1c.xml"));
        compareXMLFiles(getResourceFile("data", "company_dg4c.xml"), 
                        new File(dir, "company_dg4_1c.xml"));
    }

    /* delete an employee by detaching the data object */
    public void testC2() throws Exception
    {
        DataObject company = getCompany();
        ChangeSummary c = company.getChangeSummary();
        c.beginLogging();
        detachEmployee(company, 1);
        verifyCompanyChange4(c);
        saveDataGraph(company.getDataGraph(), new File(dir, "company_dg4_2a.xml"));
        compareXMLFiles(getResourceFile("data", "company_dg4a.xml"), 
                        new File(dir, "company_dg4_2a.xml"));
        c.undoChanges();
        detachEmployee(company, 2);
        verifyCompanyChange4(c);
        saveDataGraph(company.getDataGraph(), new File(dir, "company_dg4_2b.xml"));
        compareXMLFiles(getResourceFile("data", "company_dg4b.xml"), 
                        new File(dir, "company_dg4_2b.xml"));
        c.undoChanges();
        detachEmployee(company, 3);
        verifyCompanyChange4(c);
        saveDataGraph(company.getDataGraph(), new File(dir, "company_dg4_2c.xml"));
        compareXMLFiles(getResourceFile("data", "company_dg4c.xml"), 
                        new File(dir, "company_dg4_2c.xml"));
    }

    /* delete an employee by deleting the data object */
    public void testC3() throws Exception
    {
        DataObject company = getCompany();
        ChangeSummary c = company.getChangeSummary();
        c.beginLogging();
        deleteEmployee(company, 1);
        verifyCompanyChange4(c);
        saveDataGraph(company.getDataGraph(), new File(dir, "company_dg4_3a.xml"));
        compareXMLFiles(getResourceFile("data", "company_dg4a.xml"), 
                        new File(dir, "company_dg4_3a.xml"));
        c.undoChanges();
        deleteEmployee(company, 2);
        verifyCompanyChange4(c);
        saveDataGraph(company.getDataGraph(), new File(dir, "company_dg4_3b.xml"));
        compareXMLFiles(getResourceFile("data", "company_dg4b.xml"), 
                        new File(dir, "company_dg4_3b.xml"));
        c.undoChanges();
        deleteEmployee(company, 3);
        verifyCompanyChange4(c);
        saveDataGraph(company.getDataGraph(), new File(dir, "company_dg4_3c.xml"));
        compareXMLFiles(getResourceFile("data", "company_dg4c.xml"), 
                        new File(dir, "company_dg4_3c.xml"));
    }

    /* test insert, update and delete */
    public void testC4() throws Exception
    {
        DataObject company = getCompany();
        ChangeSummary c = company.getChangeSummary();
        c.beginLogging();
        changeCompany(company);
        removeEmployee(company, 2);
        verifyCompanyChange5(c);
        saveDataGraph(company.getDataGraph(), new File(dir, "company_dg5_1.xml"));
        compareXMLFiles(getResourceFile("data", "company_dg5.xml"), 
                        new File(dir, "company_dg5_1.xml"));
    }

    /* test insert, update and delete */
    public void testC5() throws Exception
    {
        DataObject company = getCompany();
        ChangeSummary c = company.getChangeSummary();
        c.beginLogging();
        changeCompany(company);
        detachEmployee(company, 2);
        verifyCompanyChange5(c);
        saveDataGraph(company.getDataGraph(), new File(dir, "company_dg5_2.xml"));
        compareXMLFiles(getResourceFile("data", "company_dg5.xml"), 
                        new File(dir, "company_dg5_2.xml"));
    }

    /* test insert, update and delete */
    public void testC6() throws Exception
    {
        DataObject company = getCompany();
        ChangeSummary c = company.getChangeSummary();
        c.beginLogging();
        changeCompany(company);
        deleteEmployee(company, 2);
        verifyCompanyChange5(c);
        saveDataGraph(company.getDataGraph(), new File(dir, "company_dg5_3.xml"));
        compareXMLFiles(getResourceFile("data", "company_dg5.xml"), 
                        new File(dir, "company_dg5_3.xml"));
    }

    /* same as testC6(), except rename the employee before deleting */
    public void testC9() throws Exception
    {
        DataObject company = getCompany();
        ChangeSummary c = company.getChangeSummary();
        c.beginLogging();
        changeCompany(company);
        renameEmployee(company, 2, "XXX");
        deleteEmployee(company, 2);
        verifyCompanyChange5(c);
        saveDataGraph(company.getDataGraph(), new File(dir, "company_dg5_4.xml"));
        compareXMLFiles(getResourceFile("data", "company_dg5.xml"), 
                        new File(dir, "company_dg5_4.xml"));
    }

    /* make the same changes as testC6(), to test the data objects in
       ChangeSummary.getChangedDataObjects()
     */
    public void testC10() throws Exception
    {
        DataObject company = getCompany();
        ChangeSummary c = company.getChangeSummary();
        c.beginLogging();
        //changeCompany(company);
        List departments = company.getList("departments");
        DataObject department = (DataObject) departments.get(0);
        DataObject newEmployee = department.createDataObject("employees");
        newEmployee.set("name", "Al Smith");
        newEmployee.set("SN", "E0004");
        newEmployee.setBoolean("manager", true);
        company.set("employeeOfTheMonth", newEmployee.get("SN"));

        //deleteEmployee(company, 2);
        DataObject employee = department.getDataObject("employees[2]");
        assertNotNull(employee);
        assertEquals("Mary Smith", employee.getString("name"));
        employee.delete();

        //verifyCompanyChange5(c);
        List changeList = c.getChangedDataObjects();
        assertEquals(4, changeList.size());
        int i = findCompany(changeList);
        assertTrue(i >= 0);
        DataObject c1 = (DataObject)changeList.get(i);
        System.out.println("c1 == company? " + (c1 == company));
        //assertEquals(company, c1);
        assertTrue(c.isModified(c1));
        assertFalse(c.isCreated(c1));
        assertFalse(c.isDeleted(c1));
        assertTrue(c.isModified(company));
        assertFalse(c.isCreated(company));
        assertFalse(c.isDeleted(company));
        i = findDepartment(changeList);
        assertTrue(i >= 0);
        DataObject c2 = (DataObject)changeList.get(i);
        System.out.println("c2 == department? " + (c2 == department));
        //assertEquals(department, c2);
        assertTrue(c.isModified(c2));
        assertFalse(c.isCreated(c2));
        assertFalse(c.isDeleted(c2));
        assertTrue(c.isModified(department));
        assertFalse(c.isCreated(department));
        assertFalse(c.isDeleted(department));
        i = findEmployee(changeList);
        assertTrue(i >= 0);
        DataObject c3 = (DataObject)changeList.get(i);
        verifyEmployee2(c, c3, employee, newEmployee);
        int j = findEmployee(changeList, i+1);
        assertTrue(j > i);
        DataObject c4 = (DataObject)changeList.get(j);
        verifyEmployee2(c, c4, employee, newEmployee);
    }

    /* test more than one insert */
    public void testC7() throws Exception
    {
        DataObject company = getCompany();
        ChangeSummary c = company.getChangeSummary();
        c.beginLogging();
        changeCompany(company);
        changeDepartmentLocation(company);
        expandCompany(company);
        // verify changes
        // ...
        //xmlHelper.save(company, "company.xsd", "company", System.out);
        File f = new File(dir, "company_dg6.xml");
        saveDataGraph(company.getDataGraph(), f);
        compareXMLFiles(getResourceFile("data", "company_dg6a.xml"),
                        getResourceFile("data", "company_dg6b.xml"), f);
    }

    /* test delete using employees schema */
    public void testC8() throws Exception
    {
        File f = getResourceFile("checkin", "employees.xml");
        InputStream in = new FileInputStream(f);
        XMLDocument doc = xmlHelper.load(in);
        DataObject root = doc.getRootObject();
        in.close();
        String uri = "http://xmlbeans.apache.org/samples/xquery/employees";
        assertEquals(uri, doc.getRootElementURI());
        assertEquals("employees", doc.getRootElementName());
        assertEquals(typeHelper.getType(uri, "employees"),
                     root.getType());
        // marshal back out to check round-tripping
        saveDataObject(root, uri, "employees",
                       new File(dir, "employees.xml"));

        DataGraphHelper.wrapWithDataGraph(root, doc.getRootElementURI(), doc.getRootElementName());
        ChangeSummary c = root.getChangeSummary();
        c.beginLogging();

        DataObject emp = root.getDataObject("employee[2]");
        assertNotNull(emp);
        assertEquals("Sally Smith", emp.get("name"));
        emp.delete();

        emp = root.getDataObject("employee[2]");
        assertNotNull(emp);
        assertEquals("Gladys Kravitz", emp.get("name"));

        saveDataGraph(root.getDataGraph(), new File(dir, "employees_dg.xml"));
        compareXMLFiles(getResourceFile("data", "employees_dg.xml"),
                        new File(dir, "employees_dg.xml"));
    }

    /* test changing "value" property (simple content in complex type) */
    public void testC12() throws Exception
    {
        DataObject root = getRootDataObjectWrapped("checkin", "employees.xml");
        ChangeSummary c = root.getChangeSummary();
        c.beginLogging();

        DataObject emp = root.getDataObject("employee[2]");
        assertNotNull(emp);
        assertEquals("Sally Smith", emp.get("name"));
        assertEquals("(503)555-5152", emp.get("phone[location='mobile']/value"));
        emp.set("phone[location='mobile']/value", "(503)555-3456");

        String uri = "http://xmlbeans.apache.org/samples/xquery/employees";
        saveDataObject(root, uri, "employees",
                       new File(dir, "employees2a.xml"));
        saveDataGraph(root.getDataGraph(), new File(dir, "employees_dg2a.xml"));
        compareXMLFiles(getResourceFile("data", "employees_dg2a.xml"),
                        new File(dir, "employees_dg2a.xml"));
    }

    /* test changing "value" property (simple content in complex type) */
    public void testC13() throws Exception
    {
        DataObject root = getRootDataObjectWrapped("checkin", "employees.xml");
        ChangeSummary c = root.getChangeSummary();
        c.beginLogging();

        DataObject emp = root.getDataObject("employee[2]");
        assertNotNull(emp);
        assertEquals("Sally Smith", emp.get("name"));
        assertEquals("(503)555-5152", emp.get("phone[location='mobile']/value"));
        emp.unset("phone[location='mobile']/value");

        String uri = "http://xmlbeans.apache.org/samples/xquery/employees";
        saveDataObject(root, uri, "employees",
                       new File(dir, "employees2b.xml"));
        saveDataGraph(root.getDataGraph(), new File(dir, "employees_dg2b.xml"));
        compareXMLFiles(getResourceFile("data", "employees_dg2b.xml"),
                        new File(dir, "employees_dg2b.xml"));
    }

    /* test changing "value" property (simple content in complex type) */
    public void testC14() throws Exception
    {
        DataObject root = getRootDataObjectWrapped("checkin", "employees.xml");
        DataObject emp = root.getDataObject("employee[2]");
        assertNotNull(emp);
        assertEquals("Sally Smith", emp.get("name"));
        assertEquals("(503)555-5152", emp.get("phone[location='mobile']/value"));
        emp.unset("phone[location='mobile']/value");
        String uri = "http://xmlbeans.apache.org/samples/xquery/employees";
        saveDataObject(root, uri, "employees",
                       new File(dir, "employees2c.xml"));
        ChangeSummary c = root.getChangeSummary();
        c.beginLogging();

        emp.set("phone[location='mobile']/value", "(503)555-3456");
        saveDataObject(root, uri, "employees",
                       new File(dir, "employees2d.xml"));
        saveDataGraph(root.getDataGraph(), new File(dir, "employees_dg2c.xml"));
        compareXMLFiles(getResourceFile("data", "employees_dg2c.xml"),
                        new File(dir, "employees_dg2c.xml"));
    }

    /* test deleting data object with "value" property */
    public void testC15() throws Exception
    {
        DataObject root = getRootDataObjectWrapped("checkin", "employees.xml");
        ChangeSummary c = root.getChangeSummary();
        c.beginLogging();

        DataObject emp = root.getDataObject("employee[2]");
        assertNotNull(emp);
        assertEquals("Sally Smith", emp.get("name"));
        DataObject mobile = emp.getDataObject("phone[location='mobile']");
        assertEquals("(503)555-5152", mobile.get("value"));
        mobile.delete();        

        String uri = "http://xmlbeans.apache.org/samples/xquery/employees";
        saveDataObject(root, uri, "employees",
                       new File(dir, "employees3.xml"));
        saveDataGraph(root.getDataGraph(), new File(dir, "employees_dg3.xml"));
        compareXMLFiles(getResourceFile("data", "employees_dg3.xml"),
                        new File(dir, "employees_dg3.xml"));
    }

    /* test delete and insert using another schema with 
       elementFormDefault="qualified" */
    public void testC11() throws Exception
    {
        File f = getResourceFile("data", "sdo_lds003b.xml");
        InputStream in = new FileInputStream(f);
        XMLDocument doc = xmlHelper.load(in);
        DataObject root = doc.getRootObject();
        in.close();

        DataGraphHelper.wrapWithDataGraph(root, doc.getRootElementURI(), doc.getRootElementName());
        ChangeSummary c = root.getChangeSummary();
        c.beginLogging();

        DataObject customer = root.getDataObject("SDO_C_CD[1]");
        assertNotNull(customer);
        
        DataObject order3 = customer.getDataObject("SDO_WLCO_CD[3]");
        assertNotNull(order3);
        order3.delete();
        
        DataObject newOrder = customer.createDataObject("SDO_WLCO_CD");
        newOrder.setString("ORDER_DATE", "1997-01-31T09:26:50.240");
        newOrder.setString("ORDER_ID", "ORDER_ID_31");
        newOrder.setString("STORE_ID", "30");
        newOrder.setString("CUSTOMER_ID", "3");
        newOrder.setString("SHIP_METHOD", "AIR");
        newOrder.setString("TOTAL_ORDER_AMOUNT", "5000");
        
        saveDataGraph(root.getDataGraph(), new File(dir, "sdo_lds003b_dg.xml"));
        compareXMLFiles(getResourceFile("data", "sdo_lds003b_dg.xml"),
                        new File(dir, "sdo_lds003b_dg.xml"));
    }

    /* test cascading delete */
    public void testCascadingDelete1() throws Exception
    {
        System.out.println("testCascadingDelete1()");
        String uri = "http://xmlbeans.apache.org/samples/xquery/employees";
        Type employeesType = typeHelper.getType(uri, "employees");
        Type employeeType = typeHelper.getType(uri, "employeeType");
        Type addressType = typeHelper.getType(uri, "addressType");
        Type phoneType = typeHelper.getType(uri, "phoneType");
        DataObject root = getRootDataObjectWrapped("checkin", "employees.xml");
        ChangeSummary c = root.getChangeSummary();
        c.beginLogging();

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

        address2.delete();
        phone1.delete();
        assertFalse(c.isDeleted(address1));
        assertTrue(c.isDeleted(address2));
        assertTrue(c.isDeleted(phone1));
        assertFalse(c.isDeleted(phone2));
        assertFalse(c.isDeleted(phone3));
        List<DataObject> changeList = (List<DataObject>)c.getChangedDataObjects();
        assertEquals(3, changeList.size());
        int i = find(addressType, changeList, 0);
        assertTrue(i >= 0);
        DataObject address = changeList.get(i);
        assertTrue(c.isDeleted(address));
        i = find(phoneType, changeList, 0);
        assertTrue(i >= 0);
        DataObject phone = changeList.get(i);
        assertTrue(c.isDeleted(phone));
        i = find(employeeType, changeList, 0);
        assertTrue(i >= 0);
        DataObject employee = changeList.get(i);
        assertTrue(c.isModified(employee));

        emp.delete();
        assertTrue(c.isDeleted(emp));
        // delete cascades
        assertTrue(c.isDeleted(address1));
        assertTrue(c.isDeleted(address2));
        assertTrue(c.isDeleted(phone1));
        assertTrue(c.isDeleted(phone2));
        assertTrue(c.isDeleted(phone3));

        // deleted employee and descendant data objects 
        // show up in the change list as deleted
        changeList = (List<DataObject>)c.getChangedDataObjects();
        assertEquals(2 + 5, changeList.size());
        // employee
        i = find(employeeType, changeList, 0);
        assertTrue(i >= 0);
        employee = changeList.get(i);
        assertTrue(c.isDeleted(employee));
        // addresses
        i = find(addressType, changeList, 0);
        assertTrue(i >= 0);
        address = changeList.get(i);
        assertTrue(c.isDeleted(address));
        int j = find(addressType, changeList, i + 1);
        assertTrue(j > i);
        address = changeList.get(j);
        assertTrue(c.isDeleted(address));
        // phones
        i = find(phoneType, changeList, 0);
        assertTrue(i >= 0);
        phone = changeList.get(i);
        assertTrue(c.isDeleted(phone));
        j = find(phoneType, changeList, i + 1);
        assertTrue(j > i);
        phone = changeList.get(j);
        assertTrue(c.isDeleted(phone));
        int k = find(phoneType, changeList, j + 1);
        assertTrue(k > j);
        phone = changeList.get(k);
        assertTrue(c.isDeleted(phone));
        // employees (parent of employee)
        i = find(employeesType, changeList, 0);
        assertTrue(i >= 0);
        DataObject employees = changeList.get(i);
        assertTrue(c.isModified(employees));

        saveDataGraph(root.getDataGraph(), new File(dir, "employees_dg1.xml"));
        compareXMLFiles(getResourceFile("data", "employees_dg.xml"),
                        new File(dir, "employees_dg1.xml"));
    }

    private void verifyEntry(ChangeSummary c, DataObject entry)
    {
        assertFalse(c.isCreated(entry));
        assertTrue(c.isModified(entry) || c.isDeleted(entry));
        if (c.isModified(entry))
        {
            assertFalse(entry.isSet("key"));
            assertFalse(entry.isSet("value"));
        }
        else
        {
            assertTrue(c.isDeleted(entry));
            ChangeSummary.Setting oldValue = 
                c.getOldValue(entry, entry.getInstanceProperty("value"));
            assertNotNull(oldValue);
            //assertTrue(entry.getString("value").equals("xxxxxxxxxxxxxxxxxxx") ||
            //           entry.getString("value").equals("yyyyyyyyyyyyyyyyyyy"));
            assertTrue(oldValue.getValue().equals("xxxxxxxxxxxxxxxxxxx") ||
                       oldValue.getValue().equals("yyyyyyyyyyyyyyyyyyy"));
        }
    }

    /* test cascading delete, with read-only properties */
    public void testCascadingDelete2() throws Exception
    {
        System.out.println("testCascadingDelete2()");
        String uri = "company5.xsd";
        Type companyType = typeHelper.getType(uri, "CompanyType");
        Type departmentType = typeHelper.getType(uri, "DepartmentType");
        Type employeeType = typeHelper.getType(uri, "EmployeeType");
        Type registryType = typeHelper.getType(uri, "RegistryType");
        Type entryType = typeHelper.getType(uri, "EntryType");
        DataObject company = getRootDataObjectWrapped("data", "company5.xml");
        DataObject department = company.getDataObject("departments[1]");
        assertNotNull(department);
        DataObject employee = company.getDataObject("departments[1]/employees[2]");
        assertNotNull(employee);
        assertEquals("Mary Smith", employee.get("name"));
        assertEquals("E0002", employee.get("SN"));
        assertEquals(true, employee.getBoolean("manager"));
        assertEquals(11, employee.getInt("id"));

        ChangeSummary c = company.getChangeSummary();
        c.beginLogging();

        employee.delete();
        verifyEmployee3(c, employee);
        List<DataObject> changeList = (List<DataObject>)c.getChangedDataObjects();
        assertEquals(2, changeList.size());
        int i = find(employeeType, changeList, 0);
        assertTrue(i >= 0);
        DataObject c1 = changeList.get(i);
        assertTrue(c.isDeleted(c1));
        i = find(departmentType, changeList, 0);
        assertTrue(i >= 0);
        DataObject c2 = changeList.get(i);
        assertTrue(c.isModified(c2));

        xmlHelper.save(company.getRootObject(), "commonj.sdo", "datagraph", System.out);
        System.out.println();

        c.undoChanges();
        DataObject employee1 = department.getDataObject("employees[1]");
        DataObject employee2 = department.getDataObject("employees[2]");
        DataObject employee3 = department.getDataObject("employees[3]");
        employee3.delete();
        department.delete();
        // delete cascades
        assertTrue(c.isDeleted(employee1));
        assertTrue(c.isDeleted(employee2));
        assertTrue(c.isDeleted(employee3));
        // department and employees show up in change list as deleted
        changeList = (List<DataObject>)c.getChangedDataObjects();
        assertEquals(2 + 3, changeList.size());
        i = find(departmentType, changeList, 0);
        assertTrue(i >= 0);
        c1 = changeList.get(i);
        assertTrue(c.isDeleted(c1));
        i = find(companyType, changeList, 0);
        assertTrue(i >= 0);
        c2 = changeList.get(i);
        assertTrue(c.isModified(c2));
        int j1 = find(employeeType, changeList, 0);
        assertTrue(j1 >= 0);
        DataObject e1 = changeList.get(j1);
        assertTrue(c.isDeleted(e1));
        int j2 = find(employeeType, changeList, j1 + 1);
        assertTrue(j2 > j1);
        DataObject e2 = changeList.get(j2);
        assertTrue(c.isDeleted(e2));
        int j3 = find(employeeType, changeList, j2 + 1);
        assertTrue(j3 > j2);
        DataObject e3 = changeList.get(j3);
        assertTrue(c.isDeleted(e3));

        xmlHelper.save(company.getRootObject(), "commonj.sdo", "datagraph", System.out);
        System.out.println();
        saveDataGraph(company.getDataGraph(), new File(dir, "company5_dg1.xml"));
        compareXMLFiles(getResourceFile("data", "company5_dg1.xml"),
                        new File(dir, "company5_dg1.xml"));

        c.undoChanges();
        
        // delete data object contained in read-only property
        Property p = company.getInstanceProperty("registry");
        assertTrue(p.isReadOnly());
        DataObject registry = company.getDataObject(p);
        assertNotNull(registry);
        DataObject x = registry.getDataObject("x");
        DataObject y = registry.getDataObject("y");
        registry.delete();
        assertFalse(registry.isSet("x"));
        assertFalse(registry.isSet("y"));
        // z is read-only
        assertTrue(registry.isSet("z"));
        DataObject z = registry.getDataObject("z");
        assertNotNull(z);
        // ... but its properties are not!
        assertFalse(z.isSet("key"));
        assertFalse(z.isSet("value"));
        // registry is not detached
        assertTrue(p == registry.getContainmentProperty());
        assertTrue(company == registry.getContainer());

        // from the point of view of the change summary, 
        // registry is NOT deleted, it is modified, and similarly for z;
        // however, x and y are deleted
        assertFalse(c.isDeleted(registry));
        assertTrue(c.isModified(registry));
        assertTrue(c.isDeleted(x));
        assertTrue(c.isDeleted(y));
        assertFalse(c.isDeleted(z));
        assertTrue(c.isModified(z));

        changeList = (List<DataObject>)c.getChangedDataObjects();
        assertEquals(4, changeList.size()); // registry, x, y, z; company is not modified since registry is not detached
        i = find(registryType, changeList, 0);
        assertTrue(i >= 0);
        DataObject reg = changeList.get(i);
        assertTrue(c.isModified(reg));
        i = find(entryType, changeList, 0);
        DataObject entry1 = changeList.get(i);
        verifyEntry(c, entry1);
        int j = find(entryType, changeList, i+1);
        assertTrue(j > i);
        DataObject entry2 = changeList.get(j);
        verifyEntry(c, entry2);
        int k = find(entryType, changeList, j+1);
        assertTrue(k > j);
        DataObject entry3 = changeList.get(k);
        verifyEntry(c, entry3);

        xmlHelper.save(company.getRootObject(), "commonj.sdo", "datagraph", System.out);
        System.out.println();
        saveDataGraph(company.getDataGraph(), new File(dir, "company5_dg2.xml"));
        compareXMLFiles(getResourceFile("data", "company5_dg2a.xml"),
                        getResourceFile("data", "company5_dg2b.xml"),
                        new File(dir, "company5_dg2.xml"));
    }

    /* test change summary for sequenced type (mixed content) */
    public void testD1() throws Exception
    {
        DataObject letter = getRootDataObjectWrapped("data", "letter.xml");
        ChangeSummary c = letter.getChangeSummary();
        c.beginLogging();
        assertTrue(letter.getInstanceProperty("date").getType().isDataType());
        assertTrue(letter.getInstanceProperty("firstName").getType().isDataType());
        assertTrue(letter.getInstanceProperty("lastName").getType().isDataType());
        letter.set("date", "September 1, 2006");
        Sequence seq = letter.getSequence();
        assertEquals(7, seq.size());
        //seq.remove(3);
        //seq.add(3, "firstName", "Davy");
        String text3 = "\n" +
            "  No more shark repellent needed.\n" +
            "  Your premium has been received.\n";
        seq.setValue(6, text3);
        seq.addText("  Best wishes.\n");
        assertEquals(8, seq.size());
        assertEquals("date", seq.getProperty(1).getName());
        assertEquals("September 1, 2006", seq.getValue(1));
        assertEquals(text3, seq.getValue(6));
        assertEquals("  Best wishes.\n", seq.getValue(7));
        c.endLogging();
        String savedLetter = 
            xmlHelper.save(letter, "letter.xsd", "letter");
        OutputStream out = new FileOutputStream(new File(dir, "letter.xml"));
        xmlHelper.save(letter, "letter.xsd", "letter", out);
        out.close();

        InputStream in = new FileInputStream(new File(dir, "letter.xml"));
        XMLDocument doc = xmlHelper.load(in);
        DataObject newLetter = doc.getRootObject();
        in.close();
        Sequence newSeq = newLetter.getSequence();
        assertEquals(7, newSeq.size());
        assertEquals(text3 + "  Best wishes.\n", newSeq.getValue(6));

        List changeList = c.getChangedDataObjects();
        assertNotNull(changeList);
        assertEquals(1, changeList.size());
        DataObject c1 = (DataObject)changeList.get(0);
        assertEquals(letter, c1);
        Sequence oldSeq = c.getOldSequence(letter);
        assertEquals(7, oldSeq.size());
        assertEquals("date", oldSeq.getProperty(1).getName());
        assertEquals("August 1, 2003", oldSeq.getValue(1));
        String oldText3 = "\n" +
            "  Please buy more shark repellent.\n" +
            "  Your premium is past due.\n";
        assertEquals(oldText3, oldSeq.getValue(6));

        saveDataGraph(letter.getDataGraph(), new File(dir, "letter_dg.xml"));
        String dg = xmlHelper.save(letter.getRootObject(), "commonj.sdo", "datagraph");
        String s1 = 
            "<changeSummary logging=\"false\"";
        String s2 =
            "<let:letter sdo:ref=\"#/sdo:datagraph/let:letter[1]\"";
        String s3 = 
            "<date>August 1, 2003</date>";
        String s4 = 
            "  Mutual of Omaha" + newline +
            "  Wild Kingdom, USA" + newline +
            "  Dear" + newline +
            "  ";
        String s5 = 
            "<firstName sdo:ref=\"#/sdo:datagraph/let:letter[1]/firstName[1]\"/>";
        String s6 = 
            "<lastName sdo:ref=\"#/sdo:datagraph/let:letter[1]/lastName[1]\"/>";
        int i1 = dg.indexOf(s1);
        int i2 = dg.indexOf(s2, i1);
        int i3 = dg.indexOf(s3, i2);
        int i4 = dg.indexOf(s4, i3);
        int i5 = dg.indexOf(s5, i4);
        int i6 = dg.indexOf(s6, i5);
        int i7 = dg.indexOf(oldText3.replaceAll("\n", newline), i6);
        int i8 = dg.indexOf("</let:letter>", i7);
        int i9 = dg.indexOf("</changeSummary>", i8);
        int i10 = dg.indexOf(savedLetter, i9);
        assertTrue(i1 >= 0);
        assertTrue(i2 >= i1);
        assertTrue(i3 >= i2);
        assertTrue(i4 >= i3);
        assertTrue(i5 >= i4);
        assertTrue(i6 >= i5);
        assertTrue(i7 >= i6);
        assertTrue(i8 >= i7);
        assertTrue(i9 >= i8);
        assertTrue(i10 >= i9);
    }

    /* test change summary for sequenced type (annotated schema) */
    public void testD2() throws Exception
    {
        DataObject items = getRootDataObjectWrapped("data", "items.xml");
        ChangeSummary c = items.getChangeSummary();
        c.beginLogging();
        String NS = "http://www.example.com/choice";
        Type itemsType = typeHelper.getType(NS, "ItemsType");
        Type shirtType = typeHelper.getType(NS, "ShirtType");
        Type sizeType = typeHelper.getType(NS, "ShirtSize");
        DataObject newShirt = dataFactory.create(shirtType);
        newShirt.set("color", "pink");
        DataObject shirtSize = newShirt.createDataObject("size");
        shirtSize.set("collar", 16);
        shirtSize.set("sleeve", 34);
        newShirt.set("id", "I105");
        Sequence seq = items.getSequence();
        assertEquals(3, seq.size());
        // change the color of the 3rd (last) shirt
        DataObject shirt = (DataObject)seq.getValue(2);
        shirt.set("color", "french blue");
        // add a 4th shirt
        seq.add("shirt", newShirt);
        c.endLogging();
        OutputStream out = new FileOutputStream(new File(dir, "items.xml"));
        xmlHelper.save(items, NS, "items", out);
        out.close();

        List<DataObject> changeList = (List<DataObject>)c.getChangedDataObjects();
        assertNotNull(changeList);
        assertEquals(4, changeList.size());
        int i1 = find(itemsType, changeList, 0);
        assertTrue(i1 >= 0);
        int i2 = find(shirtType, changeList, 0);
        assertTrue(i2 >= 0);
        int i3 = find(shirtType, changeList, i2 + 1);
        assertTrue(i3 > i2);
        int i4 = find(sizeType, changeList, 0);
        assertTrue(i4 >= 0);
        
        DataObject c2 = (DataObject)changeList.get(i2);
        DataObject c3 = (DataObject)changeList.get(i3);        
        assertTrue(c.isModified(c2) || c.isCreated(c2));
        if (c.isModified(c2))
        {
            assertEquals(shirt, c2);
            assertTrue(c.isCreated(c3));
            assertEquals(newShirt, c3);
        }
        else
        {
            assertEquals(newShirt, c2);
            assertTrue(c.isModified(c3));
            assertEquals(shirt, c3);
        }
        DataObject c4 = (DataObject)changeList.get(i4);
        assertTrue(c.isCreated(c4));

        saveDataGraph(items.getDataGraph(), new File(dir, "items_dg.xml"));
        compareXMLFiles(getResourceFile("data", "items_dg.xml"), 
                        new File(dir, "items_dg.xml"), IGNORE_WHITESPACE);
    }

    private static final String OPEN_A0 =
        "<open:a xmlns:open=\"http://sdo/test/opencontent\">" + newline +
        "    <open:name>My open content object</open:name>" + newline +
        "</open:a>";

    private static final String OPEN_A =
        "<open:a xmlns:open=\"http://sdo/test/opencontent\">" + newline +
        "    <open:name>My open content object</open:name>" + newline +
        "    <glob:test xmlns:glob=\"http://sdo/test/global\">" + newline +
        "        <glob:x>Extra! Extra!</glob:x>" + newline +
        "        <glob:y>1000000</glob:y>" + newline +
        "    </glob:test>" + newline +
        "</open:a>";

    private static final String OPEN_URI = "http://sdo/test/opencontent";
    private static final String GLOBAL_URI = "http://sdo/test/global";
    private static final String GLOBAL2_URI = "http://sdo/test/global2";

    private DataObject getOpenA()
    {
        XMLDocument doc = xmlHelper.load(OPEN_A);
        DataObject a = doc.getRootObject();
        DataGraphHelper.wrapWithDataGraph(a, doc.getRootElementURI(), doc.getRootElementName());
        return a;
    }

    /* test change summary when open content is modified */
    public void testModifyOpenContent() throws Exception
    {
        System.out.println("testModifyOpenContent()");
        DataObject a = getOpenA();
        ChangeSummary c = a.getChangeSummary();
        assertNotNull(c);
        c.beginLogging();
        DataObject test = a.getDataObject("test");
        test.set("y", 2000000);
        saveDataGraph(a.getDataGraph(), new File(dir, "opencontent_dg1.xml"));
        compareXMLFiles(getResourceFile("data", "opencontent_dg1.xml"), 
                        new File(dir, "opencontent_dg1.xml"));
    }

    /* test change summary when open content is deleted
       and test undoing the delete */
    public void testDeleteOpenContent() throws Exception
    {
        System.out.println("testDeleteOpenContent()");
        DataObject a = getOpenA();
        ChangeSummary c = a.getChangeSummary();
        assertNotNull(c);
        c.beginLogging();
        DataObject test = a.getDataObject("test");
        test.detach();
        saveDataGraph(a.getDataGraph(), new File(dir, "opencontent_dg2.xml"));
        compareXMLFiles(getResourceFile("data", "opencontent_dg2.xml"), 
                        new File(dir, "opencontent_dg2.xml"));
        c.undoChanges();
        Writer out = new StringWriter();
        saveDataObject(a, OPEN_URI, "a", out);
        assertEquals(OPEN_A, out.toString().trim());
        out.close();
    }

    /* test change summary when open content is inserted
       and test undoing the insert */
    public void testSetOpenContent() throws Exception
    {
        System.out.println("testSetOpenContent()");
        DataObject a = getOpenA();
        DataObject test = a.getDataObject("test");
        test.delete();
        Writer out = new StringWriter();
        saveDataObject(a, OPEN_URI, "a", out);
        assertEquals(OPEN_A0, out.toString().trim());
        out.close();
        ChangeSummary c = a.getChangeSummary();
        assertNotNull(c);
        c.beginLogging();
        Property p = typeHelper.getOpenContentProperty(GLOBAL2_URI, "test2");
        Type pt = p.getType();
        DataObject test2 = dataFactory.create(pt);
        test2.set("x1", "asdfasdf");
        test2.set("x2", "qwerty");
        test2.set("x0", 123);
        a.set(p, test2);
        saveDataGraph(a.getDataGraph(), new File(dir, "opencontent_dg3.xml"));
        compareXMLFiles(getResourceFile("data", "opencontent_dg3.xml"), 
                        new File(dir, "opencontent_dg3.xml"));
        c.undoChanges();
        out = new StringWriter();
        saveDataObject(a, OPEN_URI, "a", out);
        assertEquals(OPEN_A0, out.toString().trim());
        out.close();
    }
    
    /* test change summary when existing open content property is deleted
       and new open content is set, using the same property name */
    public void testDeleteAndSetOpenContent1() throws Exception
    {
        System.out.println("testDeleteAndSetOpenContent1()");
        DataObject a = getOpenA();
        ChangeSummary c = a.getChangeSummary();
        assertNotNull(c);
        c.beginLogging();
        DataObject test = a.getDataObject("test");
        test.delete();
        Property p = typeHelper.getOpenContentProperty(GLOBAL2_URI, "test");
        Type pt = p.getType();
        DataObject test2 = dataFactory.create(pt);
        test2.set("x1", "asdfasdf");
        test2.set("x2", "qwerty");
        test2.set("x0", 123);
        a.set(p, test2);
        saveDataGraph(a.getDataGraph(), new File(dir, "opencontent_dg4.xml"));
        compareXMLFiles(getResourceFile("data", "opencontent_dg4.xml"), 
                        new File(dir, "opencontent_dg4.xml"));
        c.undoChanges();
        Writer out = new StringWriter();
        saveDataObject(a, OPEN_URI, "a", out);
        assertEquals(OPEN_A, out.toString().trim());
        out.close();
    }

    /* test change summary when existing open content property is deleted
       and new open content is set, using a different property name */
    public void testDeleteAndSetOpenContent2() throws Exception
    {
        System.out.println("testDeleteAndSetOpenContent2()");
        DataObject a = getOpenA();
        ChangeSummary c = a.getChangeSummary();
        assertNotNull(c);
        c.beginLogging();
        DataObject test = a.getDataObject("test");
        test.delete();
        Property p = typeHelper.getOpenContentProperty(GLOBAL2_URI, "test2");
        Type pt = p.getType();
        DataObject test2 = dataFactory.create(pt);
        test2.set("x1", "asdfasdf");
        test2.set("x2", "qwerty");
        test2.set("x0", 123);
        a.set(p, test2);
        saveDataGraph(a.getDataGraph(), new File(dir, "opencontent_dg5.xml"));
        compareXMLFiles(getResourceFile("data", "opencontent_dg5.xml"), 
                        new File(dir, "opencontent_dg5.xml"));
        c.undoChanges();
        Writer out = new StringWriter();
        saveDataObject(a, OPEN_URI, "a", out);
        assertEquals(OPEN_A, out.toString().trim());
        out.close();
    }

    /* test change summary when an open attribute is inserted
       and when it is deleted */
    public void testSetAndDeleteOpenAttribute() throws Exception
    {
        System.out.println("testSetAndDeleteOpenAttribute()");
        DataObject a = getOpenA();
        DataObject test = a.getDataObject("test");
        test.delete();
        ChangeSummary c = a.getChangeSummary();
        assertNotNull(c);
        c.beginLogging();
        Property p = typeHelper.getOpenContentProperty(GLOBAL_URI, "xxx");
        a.set(p, false);
        saveDataGraph(a.getDataGraph(), new File(dir, "opencontent_dg6a.xml"));
        compareXMLFiles(getResourceFile("data", "opencontent_dg6a.xml"), 
                        new File(dir, "opencontent_dg6a.xml"));
        c.endLogging();
        c.beginLogging(); // this clears the change summary
        a.unset(p);
        saveDataGraph(a.getDataGraph(), new File(dir, "opencontent_dg6b.xml"));
        compareXMLFiles(getResourceFile("data", "opencontent_dg6b.xml"), 
                        new File(dir, "opencontent_dg6b.xml"));
    }

    /* test unmarshalling: 
       load data graph with non-empty change summary (no logging attribute),
       get change summary
    */
    public void testE1() throws Exception
    {
        DataGraph dataGraph = getDataGraph("data", "company_dg5.xml");
        assertNotNull(dataGraph);
        ChangeSummary c = dataGraph.getChangeSummary();
        // logging status: on
        assertTrue(c.isLogging());
        // verify contents of change summary
        verifyCompanyChange5(c);
    }

    /* logging status:
       load data graph with no change summary */
    public void testE2() throws Exception
    {
        DataGraph dataGraph = getDataGraph("data", "company_dg0.xml");
        assertNotNull(dataGraph);
        ChangeSummary c = dataGraph.getChangeSummary();
        assertNotNull(c);
        // logging status: off
        assertFalse(c.isLogging());
    }

    /* logging status:
       load data graph with empty change summary where logging="false" */
    public void testE3() throws Exception
    {
        DataGraph dataGraph = getDataGraph("data", "company_dg0a.xml");
        assertNotNull(dataGraph);
        ChangeSummary c = dataGraph.getChangeSummary();
        // logging status: off
        assertFalse(c.isLogging());
    }

    /* logging status:
       load data graph with empty change summary where logging="true" */
    public void testE4() throws Exception
    {
        DataGraph dataGraph = getDataGraph("data", "company_dg0b.xml");
        assertNotNull(dataGraph);
        ChangeSummary c = dataGraph.getChangeSummary();
        // logging status: on
        assertTrue(c.isLogging());
    }

    /* logging status:
       load data graph with non-empty change summary where logging="false" */
    public void testE5() throws Exception
    {
        DataGraph dataGraph = getDataGraph("data", "company_dg5a.xml");
        assertNotNull(dataGraph);
        ChangeSummary c = dataGraph.getChangeSummary();
        // logging status: off
        assertFalse(c.isLogging());
    }

    /* logging status:
       load data graph with non-empty change summary where logging="true" */
    public void testE6() throws Exception
    {
        DataGraph dataGraph = getDataGraph("data", "company_dg5b.xml");
        assertNotNull(dataGraph);
        ChangeSummary c = dataGraph.getChangeSummary();
        // logging status: on
        assertTrue(c.isLogging());
    }

    /* start with no change summary, begin logging, make no changes, save
       - data graph should have empty change summary with either logging="true"
         or no logging attribute (in our case, the latter) */
    public void testE7() throws Exception
    {
        DataGraph dataGraph = getDataGraph("data", "company_dg0.xml");
        assertNotNull(dataGraph);
        ChangeSummary c = dataGraph.getChangeSummary();
        c.beginLogging();
        assertTrue(c.isLogging());
        File f = new File(dir, "company_dg7.xml");
        saveDataGraph(dataGraph, f);
        compareXMLFiles(getResourceFile("data", "company_dg7.xml"), f);
    }

    /* logging status:
       load data graph with empty change summary (no logging attribute) */
    public void testE8() throws Exception
    {
        DataGraph dataGraph = getDataGraph("data", "company_dg7.xml");
        assertNotNull(dataGraph);
        ChangeSummary c = dataGraph.getChangeSummary();
        // logging status: on
        assertTrue(c.isLogging());
    }

    /* test ChangeSummary.getOldContainer(DataObject) 
       and ChangeSummary.getOldContainmentProperty(DataObject)
    */
    public void testF() throws Exception
    {
        // define the types to be used
        String testURI = "http://sdo/test/trade";
        Type stringType = typeHelper.getType("commonj.sdo", "String");
        Type intType = typeHelper.getType("commonj.sdo", "Int");
        Type doubleType = typeHelper.getType("commonj.sdo", "Double");
        Type typeType = typeHelper.getType("commonj.sdo", "Type");
        DataObject tradePrototype = dataFactory.create(typeType);
        tradePrototype.set("uri", testURI);
        tradePrototype.set("name", "TradeType");
        DataObject symbolProperty = tradePrototype.createDataObject("property");
        symbolProperty.set("name", "symbol");
        symbolProperty.set("type", stringType);
        DataObject quantityProperty = tradePrototype.createDataObject("property");
        quantityProperty.set("name", "quantity");
        quantityProperty.set("type", intType);
        DataObject priceProperty = tradePrototype.createDataObject("property");
        priceProperty.set("name", "price");
        priceProperty.set("type", doubleType);
        Type tradeType = typeHelper.define(tradePrototype);
        DataObject traderPrototype = dataFactory.create(typeType);
        traderPrototype.set("uri", testURI);
        traderPrototype.set("name", "TraderType");
        DataObject idProperty = traderPrototype.createDataObject("property");
        idProperty.set("name", "id");
        idProperty.set("type", stringType);
        DataObject buyProperty = traderPrototype.createDataObject("property");
        buyProperty.set("name", "buy");
        buyProperty.set("type", tradeType);
        buyProperty.set("containment", true);
        buyProperty.set("many", true);
        DataObject sellProperty = traderPrototype.createDataObject("property");
        sellProperty.set("name", "sell");
        sellProperty.set("type", tradeType);
        sellProperty.set("containment", true);
        sellProperty.set("many", true);
        Type traderType = typeHelper.define(traderPrototype);
        DataObject tradesPrototype = dataFactory.create(typeType);
        tradesPrototype.set("uri", testURI);
        tradesPrototype.set("name", "TradesType");
        DataObject traderProperty = tradesPrototype.createDataObject("property");
        traderProperty.set("name", "trader");
        traderProperty.set("type", traderType);
        traderProperty.set("containment", true);
        traderProperty.set("many", true);
        Type tradesType = typeHelper.define(tradesPrototype);

        // now create the data objects
        DataObject trade1 = dataFactory.create(tradeType);
        trade1.set("symbol", "ABC");
        trade1.set("quantity", 100);
        trade1.set("price", 14.25);
        DataObject trade2 = dataFactory.create(tradeType);
        trade2.set("symbol", "DEF");
        trade2.set("quantity", 200);
        trade2.set("price", 10.5);
        DataObject trade3 = dataFactory.create(tradeType);
        trade3.set("symbol", "XYZ");
        trade3.set("quantity", 500);
        trade3.set("price", 5.75);
        DataObject traderX = dataFactory.create(traderType);
        traderX.set("id", "traderX");
        List buyX = new ArrayList();
        buyX.add(trade1);
        traderX.set("buy", buyX);
        List sellX = new ArrayList();
        sellX.add(trade2);
        traderX.set("sell", sellX);
        DataObject traderY = dataFactory.create(traderType);
        traderY.set("id", "traderY");
        List buyY = new ArrayList();
        buyY.add(trade3);
        traderY.set("buy", buyY);
        DataObject trades = dataFactory.create(tradesType);
        List traders = new ArrayList();
        traders.add(traderX);
        traders.add(traderY);
        trades.set("trader", traders);
        
        // wrap trades in a datagraph in order to get a change summary
        // - this also tests defining a global (open content) property
        DataObject datagraph = dataFactory.create("commonj.sdo", "DataGraphType");
        assertNotNull(datagraph);
        Type propertyType = typeHelper.getType("commonj.sdo", "Property");
        DataObject globalPropertyPrototype = dataFactory.create(propertyType);
        globalPropertyPrototype.set("name", "trades");
        globalPropertyPrototype.set("type", tradesType);
        globalPropertyPrototype.set("containment", true);
        Property globalProperty = typeHelper.defineOpenContentProperty(testURI, globalPropertyPrototype);
        assertNotNull(globalProperty);
        datagraph.set(globalProperty, trades);
        
        DataGraph dg = trades.getDataGraph();
        assertNotNull(dg);
        //DataObject root = dg.getRootObject();
        //assertTrue(root == datagraph); // true
        ChangeSummary cs = dg.getChangeSummary();
        assertNotNull(cs);
        //ChangeSummary c = trades.getChangeSummary();
        //assertNotNull(c);
        //assertTrue(c == cs); // true
        cs.beginLogging();

        // now make changes
        System.out.println("BEFORE:");
        xmlHelper.save(trades, testURI, "trades", System.out);
        System.out.println();
        DataObject c1 = trade1.getContainer();
        assertEquals(traderX, c1);
        Property p1 = trade1.getContainmentProperty();
        Property pb = traderType.getProperty("buy");
        assertEquals(pb, p1);
        // move data object from one containment property to another
        List buy = trades.getList("trader[id='traderX']/buy");
        List sell = trades.getList("trader[id='traderX']/sell");
        DataObject t1 = (DataObject)buy.remove(0);
        sell.add(t1);
        System.out.println("AFTER:");
        xmlHelper.save(trades, testURI, "trades", System.out);
        System.out.println();
        DataObject oldC = cs.getOldContainer(t1);
        System.out.println("[traderX] " + oldC.get("id"));
        assertEquals(traderX, oldC); // old container was traderX
        assertEquals(traderX, t1.getContainer()); // new container is still traderX
        Property ps = traderType.getProperty("sell");
        Property oldP = cs.getOldContainmentProperty(t1);
        assertNotNull(oldP);
        System.out.println("[buy] " + oldP.getName());
        assertEquals(pb, oldP); // was buy
        assertEquals(ps, t1.getContainmentProperty()); // is sell

        saveDataGraph(dg, new File(dir, "trades_dg1.xml"));
        compareXMLFiles(getResourceFile("data", "trades_dg1.xml"),
                        new File(dir, "trades_dg1.xml"));
        // move data object from one container to another
        cs.undoChanges();
        System.out.println("BEFORE:");
        xmlHelper.save(trades, testURI, "trades", System.out);
        System.out.println();
        sell = trades.getList("trader[id='traderX']/sell");
        assertEquals(1, sell.size());
        DataObject t2 = (DataObject)sell.remove(0);
        List sellY = new ArrayList();
        sellY.add(t2);
        traderY.set("sell", sellY);
        System.out.println("AFTER:");
        xmlHelper.save(trades, testURI, "trades", System.out);
        System.out.println();
        oldC = cs.getOldContainer(t2);
        System.out.println("[traderX] " + oldC.get("id"));
        assertEquals(traderX, oldC); // old container was traderX
        assertEquals(traderY, t2.getContainer()); // new container is traderY
        oldP = cs.getOldContainmentProperty(t2);
        System.out.println("[sell] " + oldP.getName());
        assertEquals(ps, oldP); // was sell
        assertEquals(ps, t2.getContainmentProperty()); // is still sell

        saveDataGraph(dg, new File(dir, "trades_dg2.xml"));
        compareXMLFiles(getResourceFile("data", "trades_dg2a.xml"),
                        getResourceFile("data", "trades_dg2b.xml"),
                        new File(dir, "trades_dg2.xml"));
    }

    /* data object containing change summary property */
    public void testG1() throws Exception
    {
        // define the types to be used
        String testURI = "http://sdo/test/dowcs";
        Type stringType = typeHelper.getType("commonj.sdo", "String");
        Type typeType = typeHelper.getType("commonj.sdo", "Type");
        DataObject phonePrototype = dataFactory.create(typeType);
        phonePrototype.set("uri", testURI);
        phonePrototype.set("name", "PhoneType");
        DataObject locationProperty = phonePrototype.createDataObject("property");
        locationProperty.set("name", "location");
        locationProperty.set("type", stringType);
        DataObject numberProperty = phonePrototype.createDataObject("property");
        numberProperty.set("name", "number");
        numberProperty.set("type", stringType);
        Type phoneType = typeHelper.define(phonePrototype);
        DataObject personPrototype = dataFactory.create(typeType);
        personPrototype.set("uri", testURI);
        personPrototype.set("name", "PersonType");
        DataObject nameProperty = personPrototype.createDataObject("property");
        nameProperty.set("name", "name");
        nameProperty.set("type", stringType);
        DataObject phoneProperty = personPrototype.createDataObject("property");
        phoneProperty.set("name", "phone");
        phoneProperty.set("type", phoneType);
        phoneProperty.set("containment", true);
        phoneProperty.set("many", true);
        Type personType = typeHelper.define(personPrototype);
        // the type with the changeSummary property
        DataObject directoryPrototype = dataFactory.create(typeType);
        directoryPrototype.set("uri", testURI);
        directoryPrototype.set("name", "DirectoryType");
        Type csType = typeHelper.getType("commonj.sdo", "ChangeSummaryType");
        DataObject changeSummaryProperty = directoryPrototype.createDataObject("property");
        changeSummaryProperty.set("name", "changeSummary");
        changeSummaryProperty.set("type", csType);
        DataObject listingProperty = directoryPrototype.createDataObject("property");
        listingProperty.set("name", "listing");
        listingProperty.set("type", personType);
        listingProperty.set("containment", true);
        listingProperty.set("many", true);
        Type directoryType = typeHelper.define(directoryPrototype);

        // now we can create the data objects
        DataObject directory = dataFactory.create(directoryType);
        DataObject phone1 = dataFactory.create(phoneType);
        phone1.set("location", "work");
        phone1.set("number", "650-234-7701");
        DataObject phone2 = dataFactory.create(phoneType);
        phone2.set("location", "home");
        phone2.set("number", "650-927-4321");
        DataObject bob = dataFactory.create(personType);
        bob.set("name", "Robert Smith");
        List phones = new ArrayList();
        phones.add(phone1);
        bob.set("phone", phones);
        List listings = new ArrayList();
        listings.add(bob);
        directory.set("listing", listings);

        // start change tracking and make changes
        ChangeSummary c = directory.getChangeSummary();
        assertNotNull(c);
        assertFalse(c.isLogging()); // SDO-88
        c.beginLogging();
        phones = bob.getList("phone");
        phones.add(phone2);
        directory.set("listing[1]/name", "Roberta Smith");

        // verify the change summary
        List<DataObject> changeList = c.getChangedDataObjects();
        // created: phone; modified: person
        assertEquals(2, changeList.size());
        int i = find(phoneType, changeList, 0);
        assertTrue(i >= 0);
        DataObject phone = changeList.get(i);
        assertTrue(c.isCreated(phone));
        assertEquals("650-927-4321", phone.get("number"));
        i = find(personType, changeList, 0);
        assertTrue(i >= 0);
        DataObject person = changeList.get(i);
        assertTrue(c.isModified(person));
        Property name = personType.getProperty("name");
        assertEquals("Roberta Smith", person.get(name));
        assertEquals("Robert Smith", c.getOldValue(person, name).getValue());

        xmlHelper.save(directory, testURI, "directory", System.out);
        System.out.println();
        saveDataObject(directory, testURI, "directory", 
                       new File(dir, "directory.xml"));
        compareXMLFiles(getResourceFile("data", "directory.xml"),
                        new File(dir, "directory.xml"));
    }

    public void testG2() throws Exception
    {
        DataObject company = getRootDataObject("checkin", "company_with_cs.xml");
        assertNotNull(company);
        //util.DataObjectPrinter.printDataObject(company);
        ChangeSummary c = company.getChangeSummary();
        assertNotNull(c);
        assertTrue(c.isLogging());
        changeCompany(company);
        deleteEmployee(company, 2);
        verifyCompanyWithCSChange(c);
        //xmlHelper.save(company, "company_with_cs.xsd", "company", System.out);
        //System.out.println();
        saveDataObject(company, "company_with_cs.xsd", "company",
                       new File(dir, "company_with_cs.xml"));
        compareXMLFiles(getResourceFile("data", "company_with_cs.xml"), 
                        new File(dir, "company_with_cs.xml"));
    }

    private void populateQuote(DataObject quote)
    {
        quote.setString("symbol", "fbnt");
        quote.setString("companyName", "FlyByNightTechnology");
        quote.setBigDecimal("price", new BigDecimal("1000.0"));
        DataObject child = quote.createDataObject("quotes");
        child.setBigDecimal("price", new BigDecimal("1500.0"));
        child = quote.createDataObject("quotes");
        child.setBigDecimal("price", new BigDecimal("2000.0"));
        child = child.createDataObject("quotes");
        child.setBigDecimal("price", new BigDecimal("2000.99"));
        child = quote.createDataObject("quotes");
        child.setBigDecimal("price", new BigDecimal("2500.0"));
    }

    private void modifyQuote(DataObject quote)
    {
        quote.setString("symbol", "FBNT");
        quote.setBigDecimal("price", new BigDecimal("999.0"));
        quote.setDouble("volume", 1000);

        DataObject child = quote.createDataObject("quotes");
        child.setBigDecimal("price", new BigDecimal("3000.0"));
        child = quote.createDataObject("quotes");
        child.setBigDecimal("price", new BigDecimal("4000.0"));

        quote.getDataObject("quotes[2]").delete();
    }

    public void testG3() throws Exception
    {
        String TEST_NAMESPACE = "http://www.example.com/simpleCS";
        File xsdFile = getResourceFile("cts", "simpleCS.xsd");
        InputStream xsdStream = getResourceAsStream("cts", "simpleCS.xsd");
        xsdHelper.define(xsdStream, xsdFile.toURL().toString());
        xsdStream.close();

        Type quoteType = typeHelper.getType(TEST_NAMESPACE, "RootQuote");
        DataObject quote = dataFactory.create(quoteType);

        ChangeSummary cs = quote.getChangeSummary();
        ChangeSummary changes = (ChangeSummary)quote.get("changes");
        assertSame(cs, changes);

        populateQuote(quote);
        cs.beginLogging();
        modifyQuote(quote);
        cs.endLogging();

        OutputStream os = new FileOutputStream(new File(dir, "simpleCS1.xml"));
        XMLDocument doc = xmlHelper.createDocument(quote, TEST_NAMESPACE, "stockQuote");
        Options opt = new Options().setSavePrettyPrint().setSaveIndent(2);
        xmlHelper.save(doc, os, opt);
        os.close();
        compareXMLFiles(getResourceFile("cts", "simpleCS1b.xml"),
                        getResourceFile("cts", "simpleCS1a.xml"),
                        new File(dir, "simpleCS1.xml"));

        cs.undoChanges();

        os = new FileOutputStream(new File(dir, "simpleCS2.xml"));
        doc = xmlHelper.createDocument(quote, TEST_NAMESPACE, "stockQuote");
        xmlHelper.save(doc, os, opt);
        os.close();
        compareXMLFiles(getResourceFile("cts", "simpleCS2.xml"),
                        new File(dir, "simpleCS2.xml"));
    }

    /* the same as G3 except with the data object without a change summary property */
    public void testG4() throws Exception
    {
        String TEST_NAMESPACE = "http://www.example.com/simple1";

        Type quoteType = typeHelper.getType(TEST_NAMESPACE, "Quote");
        DataObject quote = dataFactory.create(quoteType);
        populateQuote(quote);

        DataGraphHelper.wrapWithDataGraph(quote, TEST_NAMESPACE, "quote");
        ChangeSummary cs = quote.getChangeSummary();
        cs.beginLogging();
        modifyQuote(quote);
        cs.endLogging();

        OutputStream os = new FileOutputStream(new File(dir, "simple1_1.xml"));
        XMLDocument doc = xmlHelper.createDocument(quote, TEST_NAMESPACE, "quote");
        Options opt = new Options().setSavePrettyPrint().setSaveIndent(2);
        xmlHelper.save(doc, os, opt);
        os.close();
        compareXMLFiles(getResourceFile("cts", "simple1_1.xml"),
                        new File(dir, "simple1_1.xml"));

        os = new FileOutputStream(new File(dir, "simple1_dg.xml"));
        doc = xmlHelper.createDocument(quote.getRootObject(), "commonj.sdo", "datagraph");
        xmlHelper.save(doc, os, opt);
        os.close();
        compareXMLFiles(getResourceFile("cts", "simple1_dga.xml"),
                        getResourceFile("cts", "simple1_dgb.xml"),
                        new File(dir, "simple1_dg.xml"));

        cs.undoChanges();

        os = new FileOutputStream(new File(dir, "simple1_2.xml"));
        doc = xmlHelper.createDocument(quote, TEST_NAMESPACE, "quote");
        xmlHelper.save(doc, os, opt);
        os.close();
        compareXMLFiles(getResourceFile("cts", "simple1_2.xml"),
                        new File(dir, "simple1_2.xml"));
    }

    /* DataObject.getChangeSummary() and
       ChangeSummary.getRootObject(), ChangeSummary.getDataGraph():
       load a data graph with no top-level change summary, 
       containing a tree of data objects, 
       two or more of which contain (non-overlapping) change summaries */ 
    public void testH1()
    {
        // case 1: data object containing change summary

        // case 2: different data object containing change summary

        // case 3: data object contained in data object of case 1

        // case 4: data object contained in data object of case 2

        // case 5: data object not containing change summary and
        // not contained in any data object with change summary
    }

    /* DataObject.getChangeSummary() and
       ChangeSummary.getRootObject(), ChangeSummary.getDataGraph():
       load a data graph with top-level change summary, 
       containing a tree of data objects;
       call getChangeSummary() on different data objects in the tree.
    */
    public void testH2()
    {

    }

    private DataObject loadFromDOMSource(File f)
        throws ParserConfigurationException, SAXException, IOException
    {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true); // false by default
        DocumentBuilder parser = dbf.newDocumentBuilder(); // throws ParserConfigurationException
        InputStream in = new FileInputStream(f);
        InputSource is = new InputSource(in);
        Document node = parser.parse(is); // throws SAXException, IOException
        Source source = new DOMSource(node);
        XMLDocument doc = xmlHelper.load(source, null, null);
        in.close();
        return doc.getRootObject();
    }

    private DataObject loadFromSAXSource(File f)
        throws IOException, java.net.MalformedURLException
    {
        InputStream in = new FileInputStream(f);
        InputSource is = new InputSource(in);
        Source source = new SAXSource(is);
        XMLDocument doc  = xmlHelper.load(source, f.toURL().toString(), null);
        in.close();
        return doc.getRootObject();
    }

    private DataObject loadFromStreamSource(File f)
        throws IOException, java.net.MalformedURLException
    {
        InputStream in = new FileInputStream(f);
        Source source = new StreamSource(in);
        XMLDocument doc = xmlHelper.load(source, f.toURL().toString(), null);
        in.close();
        return doc.getRootObject();
    }

    private DataObject loadFromXMLStreamReader(File f)
        throws XMLStreamException, IOException
    {
        XMLInputFactory xmlif = XMLInputFactory.newInstance();
        Reader reader = new FileReader(f);
        XMLStreamReader xmlsr = xmlif.createXMLStreamReader(f.toURL().toString(), reader);
        XMLDocument doc = ((XMLHelperImpl)xmlHelper).load(xmlsr, f.toURL().toString(), null);
        reader.close(); xmlsr.close();
        return doc.getRootObject();
    }

    private DataObject loadFromXMLEventReader(File f)
        throws XMLStreamException, IOException
    {
        XMLInputFactory xmlif = XMLInputFactory.newInstance();
        Reader reader = new FileReader(f);
        XMLEventReader xmler = xmlif.createXMLEventReader(f.toURL().toString(), reader);
        XMLDocument doc = ((XMLHelperImpl)xmlHelper).load(xmler, f.toURL().toString(), null);
        reader.close(); xmler.close();
        return doc.getRootObject();
    }

    private DataObject saveAndReloadDOM(DataObject datagraph)
        throws IOException
    {
        DOMResult result = new DOMResult();
        XMLDocument doc = xmlHelper.createDocument(datagraph, "commonj.sdo", "datagraph");
        doc.setXMLDeclaration(false);
        xmlHelper.save(doc, result, null);

        // load datagraph back in
        Node node = result.getNode();
        Source source = new DOMSource(node);
        doc = xmlHelper.load(source, null, null);
        return doc.getRootObject();
    }

    private void saveToSAXResult(DataObject datagraph, File f)
        throws IOException
    {
        Writer out = new FileWriter(f);
        ContentHandler handler = new XMLHelperTest.SimpleHandler(out);
        Result result = new SAXResult(handler);
        XMLDocument doc = xmlHelper.createDocument(datagraph, "commonj.sdo", "datagraph");
        doc.setXMLDeclaration(false);
        xmlHelper.save(doc, result, new Options().setSavePrettyPrint());
        out.close();
    }

    private void saveToStreamResult(DataObject datagraph, File f)
        throws IOException
    {
        OutputStream out = new FileOutputStream(f);
        Result result = new StreamResult(out);
        XMLDocument doc = xmlHelper.createDocument(datagraph, "commonj.sdo", "datagraph");
        doc.setXMLDeclaration(false);
        xmlHelper.save(doc, result, new Options().setSavePrettyPrint());
        out.close();
    }

    private void saveToXMLStreamWriter(DataObject datagraph, File f)
        throws XMLStreamException, IOException
    {
        XMLOutputFactory xmlof =  XMLOutputFactory.newInstance();
        Writer out = new FileWriter(f);
        XMLStreamWriter xmlsw = xmlof.createXMLStreamWriter(out);
        XMLDocument doc = xmlHelper.createDocument(datagraph, "commonj.sdo", "datagraph");
        doc.setXMLDeclaration(false);
        ((XMLHelperImpl)xmlHelper).save(doc, xmlsw, new Options().setSavePrettyPrint());
        xmlsw.close();
        out.close();
    }

    private void saveToXMLEventWriter(DataObject datagraph, File f)
        throws XMLStreamException, IOException
    {
        XMLOutputFactory xmlof =  XMLOutputFactory.newInstance();
        Writer out = new FileWriter(f);
        XMLEventWriter xmlew = xmlof.createXMLEventWriter(out);
        XMLDocument doc = xmlHelper.createDocument(datagraph, "commonj.sdo", "datagraph");
        doc.setXMLDeclaration(false);
        ((XMLHelperImpl)xmlHelper).save(doc, xmlew, new Options().setSavePrettyPrint());
        xmlew.close();
        out.close();
    }

    private DataObject saveAndReloadViaXMLStreamReader(DataObject datagraph)
        throws XMLStreamException, IOException
    {
        XMLDocument doc = xmlHelper.createDocument(datagraph, "commonj.sdo", "datagraph");
        doc.setXMLDeclaration(false);
        XMLStreamReader xmlsr = ((XMLHelperImpl)xmlHelper).save(doc, null);
        // and read it back in
        doc = ((XMLHelperImpl)xmlHelper).load(xmlsr, null, null);
        xmlsr.close();
        return doc.getRootObject();
    }

    public void testLoadAndSaveDOM()
        throws Exception
    {
        // load from DOMSource
        File f = getResourceFile("data", "company_dg0.xml");
        DataObject root = loadFromDOMSource(f);
        DataObject company = root.getDataObject("company");

        assertTrue(root.getChangeSummary() == company.getChangeSummary());
        ChangeSummary c = company.getChangeSummary();
        c.beginLogging();
        changeCompany(company);

        // save changed datagraph to DOMResult and load it back
        DataObject datagraph = saveAndReloadDOM(root);
        ChangeSummary c2 = datagraph.getChangeSummary();
        verifyCompanyChange(c2);
        assertTrue(equalityHelper.equal(root, datagraph));
    }

    public void testLoadAndSaveSAX()
        throws Exception
    {
        // load using SAXSource
        File f = getResourceFile("data", "company_dg0.xml");
        DataObject root = loadFromSAXSource(f);
        DataObject company = root.getDataObject("company");

        assertTrue(root.getChangeSummary() == company.getChangeSummary());
        ChangeSummary c = company.getChangeSummary();
        c.beginLogging();
        changeCompany(company);

        // save datagraph using SAXResult
        f = new File(dir, "company_dg_sax.xml");
        saveToSAXResult(root, f);
        compareXMLFiles(getResourceFile("data", "company_dg.xml"), f);

        // load datagraph back in
        DataObject datagraph = loadFromSAXSource(f);
        ChangeSummary c2 = datagraph.getChangeSummary();
        verifyCompanyChange(c2);
        assertTrue(equalityHelper.equal(root, datagraph));
    }

    public void testLoadAndSaveStream()
        throws Exception
    {
        // load using StreamSource
        File f = getResourceFile("data", "company_dg0.xml");
        DataObject root = loadFromStreamSource(f);
        DataObject company = root.getDataObject("company");

        assertTrue(root.getChangeSummary() == company.getChangeSummary());
        ChangeSummary c = company.getChangeSummary();
        c.beginLogging();
        changeCompany(company);

        // save datagraph using StreamResult
        f = new File(dir, "company_dg_stream.xml");
        saveToStreamResult(root, f);
        compareXMLFiles(getResourceFile("data", "company_dg.xml"), f);

        // load datagraph back in
        DataObject datagraph = loadFromStreamSource(f);
        ChangeSummary c2 = datagraph.getChangeSummary();
        verifyCompanyChange(c2);
        assertTrue(equalityHelper.equal(root, datagraph));
    }

    public void testLoadAndSaveStAX1()
        throws Exception
    {
        // load using XMLStreamReader
        File f = getResourceFile("data", "company_dg0.xml");
        DataObject root = loadFromXMLStreamReader(f);
        DataObject company = root.getDataObject("company");

        assertTrue(root.getChangeSummary() == company.getChangeSummary());
        ChangeSummary c = company.getChangeSummary();
        c.beginLogging();
        changeCompany(company);

        // save datagraph using XMLStreamWriter
        f = new File(dir, "company_dg_xmlsw.xml");
        saveToXMLStreamWriter(root, f);
        compareXMLFiles(getResourceFile("data", "company_dg.xml"), f);

        // load datagraph back in
        DataObject datagraph = loadFromXMLStreamReader(f);
        ChangeSummary c2 = datagraph.getChangeSummary();
        verifyCompanyChange(c2);
        assertTrue(equalityHelper.equal(root, datagraph));
    }

    public void testLoadAndSaveStAX2()
        throws Exception
    {
        // load using XMLEventReader
        File f = getResourceFile("data", "company_dg0.xml");
        DataObject root = loadFromXMLEventReader(f);
        DataObject company = root.getDataObject("company");

        assertTrue(root.getChangeSummary() == company.getChangeSummary());
        ChangeSummary c = company.getChangeSummary();
        c.beginLogging();
        changeCompany(company);

        // save datagraph using XMLEventWriter
        f = new File(dir, "company_dg_xmlew.xml");
        saveToXMLEventWriter(root, f);
        compareXMLFiles(getResourceFile("data", "company_dg.xml"), f);

        // load datagraph back in
        DataObject datagraph = loadFromXMLEventReader(f);
        ChangeSummary c2 = datagraph.getChangeSummary();
        verifyCompanyChange(c2);
        assertTrue(equalityHelper.equal(root, datagraph));
    }

    public void testLoadAndSaveStAX3()
        throws Exception
    {
        // load using XMLStreamReader
        File f = getResourceFile("data", "company_dg0.xml");
        DataObject root = loadFromXMLStreamReader(f);
        DataObject company = root.getDataObject("company");

        assertTrue(root.getChangeSummary() == company.getChangeSummary());
        ChangeSummary c = company.getChangeSummary();
        c.beginLogging();
        changeCompany(company);

        // save datagraph using XMLStreamReader and read it back in
        DataObject datagraph = saveAndReloadViaXMLStreamReader(root);
        ChangeSummary c2 = datagraph.getChangeSummary();
        verifyCompanyChange(c2);
        assertTrue(equalityHelper.equal(root, datagraph));
    }

    private static final String BASIC_URI = "http://sdo/test/basic";

    private DataObject getBasicA(String elemName, String elemValue)
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
        XMLDocument doc = xmlHelper.load(xml);
        return doc.getRootObject();
    }

    /* test use of sdo:unset in changeSummary */
    public void testFromUnsetToSetNotNull() throws Exception
    {
        DataObject a = getBasicA("integer0", 
                                 "1234567890");
        DataGraphHelper.wrapWithDataGraph(a);
        ChangeSummary cs = a.getChangeSummary();
        cs.beginLogging();
        assertFalse(a.isSet("decimal"));
        assertNull(a.get("decimal"));
        a.set("decimal", new BigDecimal("9.999999999"));
        saveDataObject(a.getRootObject(), SDO_URI, "datagraph",
                       new File(dir, "basic_dg1.xml"));
        //compareXMLFiles(getResourceFile("data", "basic_dg1.xml"), 
        //                new File(dir, "basic_dg1.xml"));

        DataObject datagraph = getRootDataObject("data", "basic_dg1.xml");
        assertNotNull(datagraph);
        ChangeSummary cs1 = datagraph.getChangeSummary();
        DataObject a1 = datagraph.getDataObject("a");
        assertNotNull(a1);
        ChangeSummary cs2 = a1.getChangeSummary();
        assertTrue(cs1 == cs2);
        // one changed property
        List oldValues = cs1.getOldValues(a1);
        assertEquals(1, oldValues.size());
        ChangeSummary.Setting oldValue = (ChangeSummary.Setting)oldValues.get(0);
        // "decimal" was unset
        assertEquals("decimal", oldValue.getProperty().getName());
        assertFalse(oldValue.isSet());
        assertNull(oldValue.getValue());
        // new value
        assertEquals(new BigDecimal("9.999999999"), a1.get("decimal"));
    }

    /* test use of sdo:unset in changeSummary and use of xsi:nil */
    public void testFromUnsetToSetNull() throws Exception
    {
        DataObject a = getBasicA("integer0", 
                                 "1234567890");
        DataGraphHelper.wrapWithDataGraph(a);
        ChangeSummary cs = a.getChangeSummary();
        cs.beginLogging();
        assertFalse(a.isSet("decimal"));
        assertNull(a.get("decimal"));
        a.set("decimal", null);
        assertTrue(a.isSet("decimal"));
        assertNull(a.get("decimal"));
        saveDataObject(a.getRootObject(), SDO_URI, "datagraph",
                       new File(dir, "basic_dg2.xml"));
        //compareXMLFiles(getResourceFile("data", "basic_dg2.xml"), 
        //                new File(dir, "basic_dg2.xml"));

        DataObject datagraph = getRootDataObject("data", "basic_dg2.xml");
        assertNotNull(datagraph);
        ChangeSummary cs1 = datagraph.getChangeSummary();
        DataObject a1 = datagraph.getDataObject("a");
        assertNotNull(a1);
        // one changed property
        List oldValues = cs1.getOldValues(a1);
        assertEquals(1, oldValues.size());
        ChangeSummary.Setting oldValue = (ChangeSummary.Setting)oldValues.get(0);
        // "decimal" was unset
        assertEquals("decimal", oldValue.getProperty().getName());
        assertFalse(oldValue.isSet());
        assertNull(oldValue.getValue());
        // new value
        assertTrue(a1.isSet("decimal"));
        assertNull(a1.get("decimal"));
    }

    public void testFromSetNullToUnset() throws Exception
    {
        DataObject a = getBasicA("integer0", 
                                 "1234567890");
        DataGraphHelper.wrapWithDataGraph(a);
        ChangeSummary cs = a.getChangeSummary();
        a.set("decimal", null);
        assertTrue(a.isSet("decimal"));
        assertNull(a.get("decimal"));
        cs.beginLogging();
        a.unset("decimal");
        assertFalse(a.isSet("decimal"));
        assertNull(a.get("decimal"));
        saveDataObject(a.getRootObject(), SDO_URI, "datagraph",
                       new File(dir, "basic_dg3.xml"));
        compareXMLFiles(getResourceFile("data", "basic_dg3.xml"), 
                        new File(dir, "basic_dg3.xml"));

        DataObject datagraph = getRootDataObject("data", "basic_dg3.xml");
        assertNotNull(datagraph);
        ChangeSummary cs1 = datagraph.getChangeSummary();
        DataObject a1 = datagraph.getDataObject("a");
        assertNotNull(a1);
        // one changed property
        List oldValues = cs1.getOldValues(a1);
        assertEquals(1, oldValues.size());
        ChangeSummary.Setting oldValue = (ChangeSummary.Setting)oldValues.get(0);
        // "decimal" was set to null
        assertEquals("decimal", oldValue.getProperty().getName());
        assertTrue(oldValue.isSet());
        assertNull(oldValue.getValue());
        // new value
        assertFalse(a1.isSet("decimal"));
        assertNull(a1.get("decimal"));
    }

    public void testFromSetNullToSetNotNull() throws Exception
    {
        DataObject a = getBasicA("integer0", 
                                 null);
        DataGraphHelper.wrapWithDataGraph(a);
        ChangeSummary cs = a.getChangeSummary();
        cs.beginLogging();
        assertTrue(a.isSet("integer0"));
        assertNull(a.get("integer0"));
        a.set("integer0", new BigInteger("1234567890"));
        saveDataObject(a.getRootObject(), SDO_URI, "datagraph",
                       new File(dir, "basic_dg4.xml"));
        compareXMLFiles(getResourceFile("data", "basic_dg4.xml"), 
                        new File(dir, "basic_dg4.xml"));

        DataObject datagraph = getRootDataObject("data", "basic_dg4.xml");
        assertNotNull(datagraph);
        ChangeSummary cs1 = datagraph.getChangeSummary();
        DataObject a1 = datagraph.getDataObject("a");
        assertNotNull(a1);
        // one changed property
        List oldValues = cs1.getOldValues(a1);
        assertEquals(1, oldValues.size());
        ChangeSummary.Setting oldValue = (ChangeSummary.Setting)oldValues.get(0);
        // "integer0" was set to null
        assertEquals("integer0", oldValue.getProperty().getName());
        assertTrue(oldValue.isSet());
        assertNull(oldValue.getValue());
        // new value
        assertEquals(new BigInteger("1234567890"), a1.get("integer0"));
    }

    public void testFromSetNotNullToUnset() throws Exception
    {
        DataObject a = getBasicA("integer0", 
                                 "1234567890");
        DataGraphHelper.wrapWithDataGraph(a);
        ChangeSummary cs = a.getChangeSummary();
        a.set("decimal", new BigDecimal("9.999999999"));
        assertTrue(a.isSet("decimal"));
        assertEquals(new BigDecimal("9.999999999"), a.get("decimal"));
        cs.beginLogging();
        a.unset("decimal");
        assertFalse(a.isSet("decimal"));
        assertNull(a.get("decimal"));
        saveDataObject(a.getRootObject(), SDO_URI, "datagraph",
                       new File(dir, "basic_dg5.xml"));
        compareXMLFiles(getResourceFile("data", "basic_dg5.xml"), 
                        new File(dir, "basic_dg5.xml"));

        DataObject datagraph = getRootDataObject("data", "basic_dg5.xml");
        assertNotNull(datagraph);
        ChangeSummary cs1 = datagraph.getChangeSummary();
        DataObject a1 = datagraph.getDataObject("a");
        assertNotNull(a1);
        // one changed property
        List oldValues = cs1.getOldValues(a1);
        assertEquals(1, oldValues.size());
        ChangeSummary.Setting oldValue = (ChangeSummary.Setting)oldValues.get(0);
        // "decimal" was set
        assertEquals("decimal", oldValue.getProperty().getName());
        assertTrue(oldValue.isSet());
        assertEquals(new BigDecimal("9.999999999"), oldValue.getValue());
        // new value
        assertFalse(a1.isSet("decimal"));
        assertNull(a1.get("decimal"));
    }

    public void testFromSetNotNullToSetNull() throws Exception
    {
        DataObject a = getBasicA("integer0", 
                                 "1234567890");
        DataGraphHelper.wrapWithDataGraph(a);
        ChangeSummary cs = a.getChangeSummary();
        a.set("decimal", new BigDecimal("9.999999999"));
        assertTrue(a.isSet("decimal"));
        assertEquals(new BigDecimal("9.999999999"), a.get("decimal"));
        cs.beginLogging();
        a.set("decimal", null);
        assertTrue(a.isSet("decimal"));
        assertNull(a.get("decimal"));
        saveDataObject(a.getRootObject(), SDO_URI, "datagraph",
                       new File(dir, "basic_dg6.xml"));
        compareXMLFiles(getResourceFile("data", "basic_dg6.xml"), 
                        new File(dir, "basic_dg6.xml"));

        DataObject datagraph = getRootDataObject("data", "basic_dg6.xml");
        assertNotNull(datagraph);
        ChangeSummary cs1 = datagraph.getChangeSummary();
        DataObject a1 = datagraph.getDataObject("a");
        assertNotNull(a1);
        // one changed property
        List oldValues = cs1.getOldValues(a1);
        assertEquals(1, oldValues.size());
        ChangeSummary.Setting oldValue = (ChangeSummary.Setting)oldValues.get(0);
        // "decimal" was set
        assertEquals("decimal", oldValue.getProperty().getName());
        assertTrue(oldValue.isSet());
        assertEquals(new BigDecimal("9.999999999"), oldValue.getValue());
        // new value
        assertTrue(a1.isSet("decimal"));
        assertNull(a1.get("decimal"));
    }

    public void testFromUnsetToUnset() throws Exception
    {
        DataObject a = getBasicA("integer0", 
                                 "1234567890");
        DataGraphHelper.wrapWithDataGraph(a);
        ChangeSummary cs = a.getChangeSummary();
        cs.beginLogging();
        xmlHelper.save(a, BASIC_URI, "a", System.out);
        assertFalse(a.isSet("decimal"));
        assertNull(a.get("decimal"));
        a.set("decimal", new BigDecimal("9.999999999"));
        a.unset("decimal");
        xmlHelper.save(a, BASIC_URI, "a", System.out);
        cs.undoChanges();
        xmlHelper.save(a, BASIC_URI, "a", System.out); // no change
        saveDataObject(a.getRootObject(), SDO_URI, "datagraph",
                       new File(dir, "basic_dg7.xml"));
        compareXMLFiles(getResourceFile("data", "basic_dg7.xml"), 
                        new File(dir, "basic_dg7.xml"));
    }

    /* test use of sdo:unset with multiple properties */
    public void testMultiplePropertiesUnset() throws Exception
    {
        DataObject a = getBasicA("decimal", 
                                 "9.999999999");
        DataGraphHelper.wrapWithDataGraph(a);
        ChangeSummary cs = a.getChangeSummary();
        cs.beginLogging();
        assertFalse(a.isSet("int0"));
        assertFalse(a.isSet("integer0"));
        a.setInt("int0", 1234567890);
        a.set("integer0", new BigInteger("1234567890"));
        xmlHelper.save(a, BASIC_URI, "a", System.out);
        saveDataObject(a.getRootObject(), SDO_URI, "datagraph",
                       new File(dir, "basic_dg8.xml"));
        compareXMLFiles(getResourceFile("data", "basic_dg8a.xml"), 
                        getResourceFile("data", "basic_dg8b.xml"), 
                        new File(dir, "basic_dg8.xml"));
    }

    private static final String SDOCS5_XML1 =
    "<tns:C xmlns:tns=\"http://aldsp.bea.com/test/sdocs5\">" +
    "<n>xxx</n>" +
    "<i>123</i>" +
    "<t>9997777</t>" +
    "<t>9998787</t>" +
    "<t></t>" +
    "</tns:C>";

    private static final String SDOCS5_XML2 =
    "<tns:C xmlns:tns=\"http://aldsp.bea.com/test/sdocs5\">" +
    "<n>xxx</n>" +
    "<i>123</i>" +
    "<t>9997777</t>" +
    "<t>9998787</t>" +
    "<t>5551234</t>" +
    "</tns:C>";

    private void verifySDOCS5_1(ChangeSummary cs)
    {
        List cl = cs.getChangedDataObjects();
        assertEquals(1, cl.size());
        DataObject cdo = (DataObject)cl.get(0);
        Type t = typeHelper.getType("http://aldsp.bea.com/test/sdocs5", "C");
        assertEquals(t, cdo.getType());
        List oldValues = cs.getOldValues(cdo);
        assertEquals(1, oldValues.size());
        ChangeSummary.Setting setting = (ChangeSummary.Setting)oldValues.get(0);
        Property p = t.getProperty("t");
        assertEquals(p, setting.getProperty());
        Object v = setting.getValue();
        assertTrue(v instanceof List);
        List vList = (List)v;
        assertEquals(3, vList.size());
        assertEquals("9997777", vList.get(0));
        assertEquals("9998787", vList.get(1));
        assertEquals("", vList.get(2));
    }

    private void verifySDOCS5_2(ChangeSummary cs)
    {
        List cl = cs.getChangedDataObjects();
        assertEquals(1, cl.size());
        DataObject cdo = (DataObject)cl.get(0);
        Type t = typeHelper.getType("http://aldsp.bea.com/test/sdocs5", "C");
        assertEquals(t, cdo.getType());
        List oldValues = cs.getOldValues(cdo);
        assertEquals(1, oldValues.size());
        ChangeSummary.Setting setting = (ChangeSummary.Setting)oldValues.get(0);
        Property p = t.getProperty("t");
        assertEquals(p, setting.getProperty());
        Object v = setting.getValue();
        assertTrue(v instanceof List);
        List vList = (List)v;
        assertEquals(3, vList.size());
        assertEquals("9997777", vList.get(0));
        assertEquals("9998787", vList.get(1));
        assertEquals("5551234", vList.get(2));
    }

    /* test setting a many-valued string property -
       from empty string to non-empty string */
    public void testFromEmptyString()  throws Exception
    {
        DataObject dobj = xmlHelper.load(SDOCS5_XML1).getRootObject();
        DataGraphHelper.wrapWithDataGraph(dobj);
        ChangeSummary cs = dobj.getChangeSummary();
        assertNotNull(cs);
        cs.beginLogging();
        assertEquals("", dobj.get("t[3]"));
        dobj.set("t[3]", "5551234");
        assertEquals("5551234", dobj.get("t[3]"));
        verifySDOCS5_1(cs);
        XMLDocument doc = xmlHelper.createDocument(dobj.getRootObject(), 
                                                   "commonj.sdo", "datagraph");
        doc.setXMLDeclaration(false);
        Writer out = new StringWriter();
        xmlHelper.save(doc, out, new Options().setSavePrettyPrint());
        String s = out.toString();
        System.out.println(s);
        // re-read the datagraph
        DataObject dg = xmlHelper.load(s).getRootObject();
        ChangeSummary cs2 = dg.getChangeSummary();
        verifySDOCS5_1(cs2);
        DataObject dobj2 = dg.getDataObject("C");
        assertEquals("9997777", dobj2.get("t[1]"));
        assertEquals("9998787", dobj2.get("t[2]"));
        assertEquals("5551234", dobj2.get("t[3]"));
    }

    /* test setting a many-valued string property -
       from non-empty string to empty string */
    public void testToEmptyString()  throws Exception
    {
        DataObject dobj = xmlHelper.load(SDOCS5_XML2).getRootObject();
        DataGraphHelper.wrapWithDataGraph(dobj);
        ChangeSummary cs = dobj.getChangeSummary();
        assertNotNull(cs);
        cs.beginLogging();
        assertEquals("5551234", dobj.get("t[3]"));
        dobj.set("t[3]", "");
        assertEquals("", dobj.get("t[3]"));
        verifySDOCS5_2(cs);
        XMLDocument doc = xmlHelper.createDocument(dobj.getRootObject(), 
                                                   "commonj.sdo", "datagraph");
        doc.setXMLDeclaration(false);
        Writer out = new StringWriter();
        xmlHelper.save(doc, out, new Options().setSavePrettyPrint());
        String s = out.toString();
        System.out.println(s);
        // re-read the datagraph
        DataObject dg = xmlHelper.load(s).getRootObject();
        ChangeSummary cs2 = dg.getChangeSummary();
        verifySDOCS5_2(cs2);
        DataObject dobj2 = dg.getDataObject("C");
        assertEquals("9997777", dobj2.get("t[1]"));
        assertEquals("9998787", dobj2.get("t[2]"));
        assertEquals("", dobj2.get("t[3]"));
    }

    private static final String simpleListType =
        "<bas:a xmlns:bas=\"http://sdo/test/basic0\">" +
            "<e>1 2 3</e>" +
        "</bas:a>";

    private static final String simpleListTypeDG =
        "<sdo:datagraph xmlns:sdo=\"commonj.sdo\">" +
            "<changeSummary>" +
                "<bas:a sdo:ref=\"#/sdo:datagraph/bas:a[1]\" xmlns:bas=\"http://sdo/test/basic0\">" +
                    "<e>1 2 3</e>" +
                "</bas:a>" +
            "</changeSummary>" +
            "<bas:a xmlns:bas=\"http://sdo/test/basic0\">" +
                "<e>1 4 3</e>" +
            "</bas:a>" +
        "</sdo:datagraph>";

    private DataObject getSimpleList()
    {
        DataObject dobj = xmlHelper.load(simpleListType).getRootObject();
        Object e = dobj.get("e");
        assertNotNull(e);
        assertTrue(e instanceof List);
        List eList = (List)e;
        assertEquals(3, eList.size());
        assertEquals(new Integer(1), eList.get(0));
        assertEquals(new Integer(2), eList.get(1));
        assertEquals(new Integer(3), eList.get(2));
        DataGraphHelper.wrapWithDataGraph(dobj);
        return dobj;
    }

    private void verifySimpleListChange(ChangeSummary cs, DataObject a)
    {
        String out = xmlHelper.save(a.getRootObject(), "commonj.sdo", "datagraph");
        System.out.println(out);
        Property p = a.getInstanceProperty("e");
        assertNotNull(p);
        ChangeSummary.Setting old = cs.getOldValue(a, p);
        List l = (List)old.getValue();
        assertEquals(3, l.size());
        assertEquals(new Integer(1), l.get(0));
        assertEquals(new Integer(2), l.get(1));
        assertEquals(new Integer(3), l.get(2));
        assertEquals(simpleListTypeDG, out);
    }

    public void testModifySimpleListType1()
    {
        System.out.println("testModifySimpleListType1()");
        DataObject dobj = getSimpleList();
        ChangeSummary cs = dobj.getChangeSummary();
        assertNotNull(cs);
        cs.beginLogging();
        List eList = dobj.getList("e");
        eList.set(1, new Integer(4));
        dobj.set("e", eList);
        eList.add(new Integer(5)); // this makes "e" [1, 4, 3, 5] when "e" should remain unchanged (eList is "live" when it shouldn't be)
        // however, the old value is still [1, 4, 3] in this case
        verifySimpleListChange(cs, dobj);
    }

    public void testModifySimpleListType2()
    {
        System.out.println("testModifySimpleListType2()");
        DataObject dobj = getSimpleList();
        ChangeSummary cs = dobj.getChangeSummary();
        assertNotNull(cs);
        cs.beginLogging();
        List eList = dobj.getList("e");
        List newE = new ArrayList(eList);
        newE.set(1, new Integer(4));
        dobj.set("e", newE);
        verifySimpleListChange(cs, dobj);
    }

    private static final String SDOCS4_XML1 =
    "<tns:C xmlns:tns=\"http://aldsp.bea.com/test/sdocs4\">" +
    "<n>xxx</n>" +
    "<i>123</i>" +
    "<t>9997777</t>" +
    "<t>9998787</t>" +
    "<t xsi:nil=\"true\" xmlns:xsi=\"" + XSI_URI + "\"/>" +
    "</tns:C>";

    private static final String SDOCS4_XML2 =
    "<tns:C xmlns:tns=\"http://aldsp.bea.com/test/sdocs4\">" +
    "<n>xxx</n>" +
    "<i>123</i>" +
    "<t>9997777</t>" +
    "<t>9998787</t>" +
    "<t>5551234</t>" +
    "</tns:C>";

    private void verifySDOCS4_1(ChangeSummary cs)
    {
        List cl = cs.getChangedDataObjects();
        assertEquals(1, cl.size());
        DataObject cdo = (DataObject)cl.get(0);
        Type t = typeHelper.getType("http://aldsp.bea.com/test/sdocs4", "C");
        assertEquals(t, cdo.getType());
        List oldValues = cs.getOldValues(cdo);
        assertEquals(1, oldValues.size());
        ChangeSummary.Setting setting = (ChangeSummary.Setting)oldValues.get(0);
        Property p = t.getProperty("t");
        assertEquals(p, setting.getProperty());
        Object v = setting.getValue();
        assertTrue(v instanceof List);
        List vList = (List)v;
        assertEquals(3, vList.size());
        assertEquals(new BigInteger("9997777"), vList.get(0));
        assertEquals(new BigInteger("9998787"), vList.get(1));
        assertNull(vList.get(2));
    }

    private void verifyReloadSDOCS4_1(DataObject dobj) throws Exception
    {
        XMLDocument doc = xmlHelper.createDocument(dobj.getRootObject(), 
                                                   "commonj.sdo", "datagraph");
        doc.setXMLDeclaration(false);
        Writer out = new StringWriter();
        xmlHelper.save(doc, out, new Options().setSavePrettyPrint());
        String s = out.toString();
        System.out.println(s);
        // re-read the datagraph
        DataObject dg = xmlHelper.load(s).getRootObject();
        ChangeSummary cs2 = dg.getChangeSummary();
        verifySDOCS4_1(cs2);
        DataObject dobj2 = dg.getDataObject("C");
        assertEquals(new BigInteger("9997777"), dobj2.get("t[1]"));
        assertEquals(new BigInteger("9998787"), dobj2.get("t[2]"));
        assertEquals(new BigInteger("5551234"), dobj2.get("t[3]"));
        assertEquals(3, dobj2.getList("t").size());
        assertEquals(new BigInteger("9997777"), dobj2.getList("t").get(0));
        assertEquals(new BigInteger("9998787"), dobj2.getList("t").get(1));
        assertEquals(new BigInteger("5551234"), dobj2.getList("t").get(2));
    }

    private void verifySDOCS4_2(ChangeSummary cs)
    {
        List cl = cs.getChangedDataObjects();
        assertEquals(1, cl.size());
        DataObject cdo = (DataObject)cl.get(0);
        Type t = typeHelper.getType("http://aldsp.bea.com/test/sdocs4", "C");
        assertEquals(t, cdo.getType());
        List oldValues = cs.getOldValues(cdo);
        assertEquals(1, oldValues.size());
        ChangeSummary.Setting setting = (ChangeSummary.Setting)oldValues.get(0);
        Property p = t.getProperty("t");
        assertEquals(p, setting.getProperty());
        Object v = setting.getValue();
        assertTrue(v instanceof List);
        List vList = (List)v;
        assertEquals(3, vList.size());
        assertEquals(new BigInteger("9997777"), vList.get(0));
        assertEquals(new BigInteger("9998787"), vList.get(1));
        assertEquals(new BigInteger("5551234"), vList.get(2));
    }

    private DataObject verifyReloadSDOCS4_2(DataObject dobj) throws Exception
    {
        XMLDocument doc = xmlHelper.createDocument(dobj.getRootObject(), 
                                                   "commonj.sdo", "datagraph");
        doc.setXMLDeclaration(false);
        Writer out = new StringWriter();
        xmlHelper.save(doc, out, new Options().setSavePrettyPrint());
        String s = out.toString();
        System.out.println(s);
        // re-read the datagraph
        DataObject dg = xmlHelper.load(s).getRootObject();
        ChangeSummary cs2 = dg.getChangeSummary();
        verifySDOCS4_2(cs2);
        DataObject dobj2 = dg.getDataObject("C");
        return dobj2;
    }

    public void testModifyListOfSimpleType1a() throws Exception
    {
        DataObject dobj = xmlHelper.load(SDOCS4_XML1).getRootObject();
        DataGraphHelper.wrapWithDataGraph(dobj);
        ChangeSummary cs = dobj.getChangeSummary();
        assertNotNull(cs);
        cs.beginLogging();
        assertNull(dobj.get("t[3]"));
        dobj.set("t[3]", new BigInteger("5551234"));
        assertEquals(new BigInteger("5551234"), dobj.get("t[3]"));
        verifySDOCS4_1(cs);
        verifyReloadSDOCS4_1(dobj);
    }

    public void testModifyListOfSimpleType1b() throws Exception
    {
        DataObject dobj = xmlHelper.load(SDOCS4_XML1).getRootObject();
        DataGraphHelper.wrapWithDataGraph(dobj);
        ChangeSummary cs = dobj.getChangeSummary();
        assertNotNull(cs);
        cs.beginLogging();
        List tList = dobj.getList("t");
        assertEquals(3, tList.size());
        tList.set(2, new BigInteger("5551234"));
        assertEquals(new BigInteger("5551234"), dobj.get("t[3]"));
        verifySDOCS4_1(cs);
        verifyReloadSDOCS4_1(dobj);
    }

    public void testModifyListOfSimpleType1c() throws Exception
    {
        DataObject dobj = xmlHelper.load(SDOCS4_XML1).getRootObject();
        DataGraphHelper.wrapWithDataGraph(dobj);
        ChangeSummary cs = dobj.getChangeSummary();
        assertNotNull(cs);
        cs.beginLogging();
        List tList = dobj.getList("t");
        assertEquals(3, tList.size());
        tList.remove(2);
        tList.add(new BigInteger("5551234"));
        assertEquals(new BigInteger("5551234"), dobj.get("t.2"));
        verifySDOCS4_1(cs);
        verifyReloadSDOCS4_1(dobj);
    }

    public void testModifyListOfSimpleType2a() throws Exception
    {
        DataObject dobj = xmlHelper.load(SDOCS4_XML2).getRootObject();
        DataGraphHelper.wrapWithDataGraph(dobj);
        ChangeSummary cs = dobj.getChangeSummary();
        assertNotNull(cs);
        cs.beginLogging();
        dobj.set("t[3]", null);
        assertNull(dobj.get("t[3]"));
        verifySDOCS4_2(cs);
        DataObject dobj2 = verifyReloadSDOCS4_2(dobj);
        assertEquals(new BigInteger("9997777"), dobj2.get("t[1]"));
        assertEquals(new BigInteger("9998787"), dobj2.get("t[2]"));
        assertNull(dobj2.get("t[3]"));
    }

    public void testModifyListOfSimpleType2b() throws Exception
    {
        DataObject dobj = xmlHelper.load(SDOCS4_XML2).getRootObject();
        DataGraphHelper.wrapWithDataGraph(dobj);
        ChangeSummary cs = dobj.getChangeSummary();
        assertNotNull(cs);
        cs.beginLogging();
        dobj.set("t[2]", new BigInteger("2223456"));
        verifySDOCS4_2(cs);
        DataObject dobj2 = verifyReloadSDOCS4_2(dobj);
        assertEquals(new BigInteger("9997777"), dobj2.get("t[1]"));
        assertEquals(new BigInteger("2223456"), dobj2.get("t[2]"));
        assertEquals(new BigInteger("5551234"), dobj2.get("t[3]"));
    }

    public void testModifyListOfSimpleType2c() throws Exception
    {
        DataObject dobj = xmlHelper.load(SDOCS4_XML2).getRootObject();
        DataGraphHelper.wrapWithDataGraph(dobj);
        ChangeSummary cs = dobj.getChangeSummary();
        assertNotNull(cs);
        cs.beginLogging();
        List tList = dobj.getList("t");
        tList.set(1, new BigInteger("2223456"));
        assertEquals(new BigInteger("2223456"), dobj.get("t.1"));
        assertEquals(3, tList.size());
        verifySDOCS4_2(cs);
        DataObject dobj2 = verifyReloadSDOCS4_2(dobj);
        assertEquals(new BigInteger("9997777"), dobj2.get("t[1]"));
        assertEquals(new BigInteger("2223456"), dobj2.get("t[2]"));
        assertEquals(new BigInteger("5551234"), dobj2.get("t[3]"));
        assertEquals(3, dobj2.getList("t").size());
        assertEquals(new BigInteger("9997777"), dobj2.getList("t").get(0));
        assertEquals(new BigInteger("2223456"), dobj2.getList("t").get(1));
        assertEquals(new BigInteger("5551234"), dobj2.getList("t").get(2));
    }

    public void testModifyListOfSimpleType2d() throws Exception
    {
        DataObject dobj = xmlHelper.load(SDOCS4_XML2).getRootObject();
        DataGraphHelper.wrapWithDataGraph(dobj);
        ChangeSummary cs = dobj.getChangeSummary();
        assertNotNull(cs);
        cs.beginLogging();
        List tList = dobj.getList("t");
        tList.remove(2);
        assertEquals(2, tList.size());
        assertNull(dobj.get("t[3]"));
        verifySDOCS4_2(cs);
        DataObject dobj2 = verifyReloadSDOCS4_2(dobj);
        assertEquals(new BigInteger("9997777"), dobj2.get("t[1]"));
        assertEquals(new BigInteger("9998787"), dobj2.get("t[2]"));
        assertNull(dobj2.get("t[3]"));
        assertEquals(2, dobj2.getList("t").size());
        assertEquals(new BigInteger("9997777"), dobj2.getList("t").get(0));
        assertEquals(new BigInteger("9998787"), dobj2.getList("t").get(1));
    }

    public void testModifyListOfSimpleType2e() throws Exception
    {
        DataObject dobj = xmlHelper.load(SDOCS4_XML2).getRootObject();
        DataGraphHelper.wrapWithDataGraph(dobj);
        ChangeSummary cs = dobj.getChangeSummary();
        assertNotNull(cs);
        cs.beginLogging();
        assertEquals(3, dobj.getList("t").size());
        dobj.unset("t");
        assertEquals(0, dobj.getList("t").size());
        System.out.println(xmlHelper.save(dobj, "http://aldsp.bea.com/test/sdocs4", "C"));
        verifySDOCS4_2(cs);
        DataObject dobj2 = verifyReloadSDOCS4_2(dobj);
        assertEquals(0, dobj2.getList("t").size());
    }

    public void testModifyListOfSimpleType2f() throws Exception
    {
        DataObject dobj = xmlHelper.load(SDOCS4_XML2).getRootObject();
        DataGraphHelper.wrapWithDataGraph(dobj);
        ChangeSummary cs = dobj.getChangeSummary();
        assertNotNull(cs);
        cs.beginLogging();
        List tList = dobj.getList("t");
        assertEquals(3, tList.size());
        tList.clear();
        assertEquals(0, tList.size());
        System.out.println(xmlHelper.save(dobj, "http://aldsp.bea.com/test/sdocs4", "C"));
        verifySDOCS4_2(cs);
        DataObject dobj2 = verifyReloadSDOCS4_2(dobj);
        assertEquals(0, dobj2.getList("t").size());
    }

    public void testModifyListOfSimpleType2g() throws Exception
    {
        DataObject dobj = xmlHelper.load(SDOCS4_XML2).getRootObject();
        DataGraphHelper.wrapWithDataGraph(dobj);
        ChangeSummary cs = dobj.getChangeSummary();
        assertNotNull(cs);
        cs.beginLogging();
        List tList = dobj.getList("t");
        assertEquals(3, tList.size());
        tList.remove(2);
        tList.remove(1);
        tList.remove(0);
        assertEquals(0, tList.size());
        System.out.println(xmlHelper.save(dobj, "http://aldsp.bea.com/test/sdocs4", "C"));
        verifySDOCS4_2(cs);
        DataObject dobj2 = verifyReloadSDOCS4_2(dobj);
        assertEquals(0, dobj2.getList("t").size());
    }

    /* change summary in datagraph */
    public void testCopy1() throws Exception
    {
        System.out.println("testCopy1()");
        DataObject company = getCompany();
        ChangeSummary c = company.getChangeSummary();
        c.beginLogging();
        changeCompany(company);
        DataObject root = company.getRootObject();
        assertEquals(DATAGRAPHTYPE, root.getType());
        assertTrue(c == root.getChangeSummary());
        DataGraph dg = root.getDataGraph();
        assertTrue(company.getDataGraph() == dg);
        assertTrue(c == dg.getChangeSummary());
        assertTrue(c.getDataGraph() == dg);
        verifyCompanyChange(c);
        DataObject root2 = copyHelper.copy(root);
        ChangeSummary c2 = root2.getChangeSummary();
        assertNotNull(c2);
        DataGraph dg2 = root2.getDataGraph();
        assertTrue(c2 == dg2.getChangeSummary());
        assertTrue(c2.getDataGraph() == dg2);
        verifyCompanyChange(c2);
        File f = new File(dir, "company_dg_copy.xml");
        saveDataObject(root2, "commonj.sdo", "datagraph", f);
        compareXMLFiles(getResourceFile("data", "company_dg.xml"), f);
    }

    /* change summary is company data object */
    public void testCopy2() throws Exception
    {
        System.out.println("testCopy2()");
        DataObject company = getRootDataObject("checkin", "company_with_cs.xml");
        assertNotNull(company);
        ChangeSummary c = company.getChangeSummary();
        assertNotNull(c);
        assertTrue(c.isLogging());
        changeCompany(company);
        deleteEmployee(company, 2);
        verifyCompanyWithCSChange(c);

        DataObject company2 = copyHelper.copy(company);
        ChangeSummary c2 = company2.getChangeSummary();
        assertNotNull(c2);
        assertTrue(c2.isLogging());
        verifyCompanyWithCSChange(c2);
        File f = new File(dir, "company_with_cs_copy.xml");
        saveDataObject(company2, "company_with_cs.xsd", "company", f);
        compareXMLFiles(getResourceFile("data", "company_with_cs.xml"), f);
    }
}
