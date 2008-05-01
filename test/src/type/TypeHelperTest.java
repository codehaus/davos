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
package type;

import javax.sdo.Type;
import javax.sdo.Property;
import javax.sdo.helper.TypeHelper;

import junit.framework.Test;
import junit.framework.TestSuite;

import common.BaseTest;

/**
 * @author Wing Yew Poon
 */
public class TypeHelperTest extends BaseTest
{
    public TypeHelperTest(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        
        TestSuite suite = new TestSuite();
        suite.addTest(new TypeHelperTest("testGetType"));
        suite.addTest(new TypeHelperTest("testGetType1"));
        
        // or
        //TestSuite suite = new TestSuite(TypeHelperTest.class);
        return suite;
    }

    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(suite());
    }

    private static TypeHelper typeHelper = context.getTypeHelper();

    private void _testGetType(String uri, String name)
    {
        Type t = typeHelper.getType(uri, name);
        assertNotNull(t);
        System.out.println(t);
    }

    private void _testGetType(Class c)
    {
        Type t = typeHelper.getType(c);
        assertNotNull(t);
        System.out.println(t);
    }

    public void testGetType()
    {
        // compiled classes
        // checkin/IPO.xsd
        _testGetType(com.example.ipo.Address.class);
        _testGetType(com.example.ipo.Customer.class);
        _testGetType(com.example.ipo.CustomerList.class);
        _testGetType(com.example.ipo.Items.class);
        _testGetType(com.example.ipo.Items.ItemType.class);
        _testGetType(com.example.ipo.PurchaseOrderType.class);
        _testGetType(com.example.ipo.USAddress.class);
        _testGetType(com.example.ipo.Vehicle.class);
        // checkin/simple1.xsd
        _testGetType(com.example.simple1.Quote.class);
        // checkin/simple3.xsd
        _testGetType(test.simple3.A.class);
        _testGetType(test.simple3.A.B.class);
        _testGetType(test.simple3.A.C.class);
        _testGetType(test.simple3.Document.class);
        // checkin/employees.xsd
        _testGetType(org.apache.xmlbeans.samples.xquery.employees.AddressType.class);
        _testGetType(org.apache.xmlbeans.samples.xquery.employees.Employees.class);
        _testGetType(org.apache.xmlbeans.samples.xquery.employees.EmployeeType.class);
        _testGetType(org.apache.xmlbeans.samples.xquery.employees.PhoneType.class);
        // data/basic.xsd
        _testGetType(sdo.test.basic.A.class);
        _testGetType(sdo.test.basic.B.class);
        _testGetType(sdo.test.basic.D.class);
        // data/company5.xsd
        _testGetType(xsd.company5.CompanyType.class);
        _testGetType(xsd.company5.DepartmentType.class);
        _testGetType(xsd.company5.EmployeeType.class);
        _testGetType(xsd.company5.EntryType.class);
        _testGetType(xsd.company5.RegistryType.class);
        // type/anil.xsd
        _testGetType(sdo.test.anil.NormalizedCustomer1.class);
        _testGetType(sdo.test.anil.NormalizedCustomer2Type.class);
        // type/derivation.xsd
        _testGetType(sdo.test.derivation.HatType.class);
        _testGetType(sdo.test.derivation.ItemsType.class);
        _testGetType(sdo.test.derivation.ItemType.class);
        _testGetType(sdo.test.derivation.ProductType.class);
        _testGetType(sdo.test.derivation.ShirtType.class);
        // type/substitution.xsd
        _testGetType(sdo.test.substitution.Hat.class);
        _testGetType(sdo.test.substitution.ItemsType.class);
        _testGetType(sdo.test.substitution.ItemType.class);
        _testGetType(sdo.test.substitution.ProductType.class);
        _testGetType(sdo.test.substitution.ShirtType.class);

        // schemas available at run-time
        // checkin/company_with_cs.xsd
        _testGetType("company_with_cs.xsd", "CompanyType");
        _testGetType("company_with_cs.xsd", "DepartmentType");
        _testGetType("company_with_cs.xsd", "EmployeeType");
        // checkin/company.xsd
        // checkin/company2.xsd
        // checkin/QName.xsd
        _testGetType("test/QName", "typeWithQName");
        // checkin/substGroup.xsd
        _testGetType("checkin/substitution", "base");
        _testGetType("checkin/substitution", "derived");
        _testGetType("checkin/substitution", "derived2");
        _testGetType("checkin/substitution", "root"); // anonymous type

        // data/company3a.xsd
        // data/company3b.xsd
        // data/company4a.xsd
        // data/company4b.xsd
        // data/letter.xsd
        _testGetType("letter.xsd", "FormLetter");
        // data/copy1.xsd
        // data/copy2.xsd
        // data/copy3a.xsd
        // data/copy3b.xsd
        // data/copy4.xsd
        // data/basic0.xsd
        // data/sdo_lds003b.xsd
        _testGetType("sdo_lds003b.xsd", "WLCustOrdersCD"); // anonymous type
        //_testGetType("sdo_lds003b.xsd", "SDO_C_CD"); // anonymous type
        //_testGetType("sdo_lds003b.xsd", "SDO_WLCO_CD"); // anonymous type
        // marshal/chartest.xsd
        // type/global.xsd
        _testGetType("http://sdo/test/global", "ProductType");
        _testGetType("http://sdo/test/global", "SimpleType");
        // type/global2.xsd
        _testGetType("http://sdo/test/global2", "SimpleType");
        // type/nillable.xsd
        _testGetType("http://sdo/test/nillable", "SimpleTestType");
        _testGetType("http://sdo/test/nillable", "TestType");
        _testGetType("http://sdo/test/nillable", "CTSC");
        _testGetType("http://sdo/test/nillable", "CTCCNA");
        _testGetType("http://sdo/test/nillable", "CTCCWA");
        // type/opencontent.xsd
        _testGetType("http://sdo/test/opencontent", "A" );
        _testGetType("http://sdo/test/opencontent", "B" );
        _testGetType("http://sdo/test/opencontent", "C" );
        _testGetType("http://sdo/test/opencontent", "D" );
        _testGetType("http://sdo/test/opencontent", "E" );
    }

    public void testGetType1()
    {
        Type t0 = typeHelper.getType("sdo_lds003b.xsd", "WLCustOrdersCD");
        assertNotNull(t0);
        Property p1 = t0.getProperty("SDO_C_CD");
        assertNotNull(p1);
        Type t1 = p1.getType();
        assertEquals("sdo_lds003b.xsd", t1.getURI());
        assertEquals("WLCustOrdersCD$SDO_C_CD", t1.getName());
        Property p2 = t1.getProperty("SDO_WLCO_CD");
        assertNotNull(p2);
        Type t2 = p2.getType();
        assertEquals("sdo_lds003b.xsd", t2.getURI());
        assertEquals("WLCustOrdersCD$SDO_C_CD$SDO_WLCO_CD", t2.getName());
    }
}
