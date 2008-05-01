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
package sdocomp;

import java.util.List;
import javax.sdo.DataObject;
import junit.framework.Test;
import junit.framework.TestSuite;
import common.DataTest;
import xsd.company5.CompanyType;
import xsd.company5.DepartmentType;
import xsd.company5.EmployeeType;

/** This class is for testing the Java interface generation for a schema
    with an element with sdoxml:dataType="sdojava:*Object", where * may be
    Boolean, etc.
    The fact that it _compiles_ is demonstration that the generated interface
    has the signatures we are looking for.
    @author Wing Yew Poon
 */
public class SDOJavaTypeTest extends DataTest
{
    public SDOJavaTypeTest(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        
        TestSuite suite = new TestSuite();
        suite.addTest(new SDOJavaTypeTest("testType"));
        
        // or
        //TestSuite suite = new TestSuite(SDOJavaTypeTest.class);
        return suite;
    }

    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(suite());
    }

    /* test that if a property has type an SDO Java data type,
       the generated getter has return type a Java wrapper class */
    public void testType() throws Exception
    {
        DataObject root = getRootDataObject("data", "company5.xml");
        
        assertTrue(root instanceof CompanyType);
        CompanyType company = (CompanyType)root;
        List<DepartmentType> departments = company.getDepartments();
        DepartmentType department = departments.get(0);
        List<EmployeeType> employees = department.getEmployees();        
        EmployeeType john = employees.get(0);
        EmployeeType mary = employees.get(1);
        assertEquals("John Jones", john.getName());
        assertEquals("Mary Smith", mary.getName());
        Boolean johnIsManager = 
            //Boolean.valueOf(root.getBoolean("departments[1]/employees[1]/manager"));
            john.isManager();
        System.out.println("johnIsManager: " + johnIsManager);
        Boolean maryIsManager = 
            //Boolean.valueOf(root.getBoolean("departments[1]/employees[2]/manager"));
            mary.isManager();
        assertTrue(mary.isSetManager());
        assertEquals(Boolean.TRUE, maryIsManager);
        assertFalse(john.isSetManager());
        assertNull(johnIsManager);
    }
}
