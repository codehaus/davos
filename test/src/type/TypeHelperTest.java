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

import java.util.List;
import java.util.ArrayList;

import javax.sdo.DataObject;
import javax.sdo.Type;
import javax.sdo.Property;
import javax.sdo.helper.DataFactory;
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
        suite.addTest(new TypeHelperTest("testDefine"));
        
        // or
        //TestSuite suite = new TestSuite(TypeHelperTest.class);
        return suite;
    }

    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(suite());
    }

    private static DataFactory factory = context.getDataFactory();
    private static TypeHelper typeHelper = context.getTypeHelper();

    static Type booleanType = typeHelper.getType("commonj.sdo", "Boolean");
    static Type doubleType = typeHelper.getType("commonj.sdo", "Double");
    static Type intType = typeHelper.getType("commonj.sdo", "Int");
    static Type integerType = typeHelper.getType("commonj.sdo", "Integer");
    static Type stringType = typeHelper.getType("commonj.sdo", "String");

    private static DataObject prototype1, prototype2, prototype3;
    static
    {
        prototype1 = factory.create("commonj.sdo", "Type");
        prototype1.set("uri", "xxx");
        prototype1.set("name", "TestType1");
        DataObject booleanProperty = prototype1.createDataObject("property");
        booleanProperty.set("name", "qualified");
        booleanProperty.set("type", booleanType);
        DataObject stringProperty = prototype1.createDataObject("property");
        stringProperty.set("name", "name");
        stringProperty.set("type", stringType);

        prototype2 = factory.create("commonj.sdo", "Type");
        prototype2.set("uri", "xxx");
        prototype2.set("name", "TestType2");
        DataObject intProperty = prototype2.createDataObject("property");
        intProperty.set("name", "quantity");
        intProperty.set("type", intType);
        DataObject doubleProperty = prototype2.createDataObject("property");
        doubleProperty.set("name", "amount");
        doubleProperty.set("type", doubleType);

        prototype3 = factory.create("commonj.sdo", "Type");
        prototype3.set("uri", "xxx");
        prototype3.set("name", "TestType3");
        DataObject integerProperty = prototype3.createDataObject("property");
        integerProperty.set("name", "value");
        integerProperty.set("type", integerType);
    }

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

    private void checkType(Type t)
    {
        System.out.println(t);
        List props = t.getProperties();
        System.out.println("properties [" + props.size() + "] :");
        for (Object o : props)
        {
            Property p = (Property)o;
            System.out.println("  " + p.getName());
            System.out.println("    " + p.getType());
        }
    }

    public void testDefine()
    {
        List l1 = new ArrayList();
        l1.add(prototype1);
        l1.add(prototype2);
        List types = typeHelper.define(l1);
        assertEquals(2, types.size());
        Type t1 = (Type)types.get(0);
        checkType(t1);
        assertEquals("xxx", t1.getURI());
        assertEquals("TestType1", t1.getName());
        List t1Properties = t1.getProperties();
        assertEquals(2, t1Properties.size());
        Property t1p1 = (Property)t1Properties.get(0);
        assertEquals("qualified", t1p1.getName());
        assertEquals(booleanType, t1p1.getType());
        Property t1p2 = (Property)t1Properties.get(1);
        assertEquals("name", t1p2.getName());
        assertEquals(stringType, t1p2.getType());
        Type t2 = (Type)types.get(1);
        checkType(t2);
        assertEquals("xxx", t2.getURI());
        assertEquals("TestType2", t2.getName());
        List t2Properties = t2.getProperties();
        assertEquals(2, t2Properties.size());
        Property t2p1 = (Property)t2Properties.get(0);
        assertEquals("quantity", t2p1.getName());
        assertEquals(intType, t2p1.getType());
        Property t2p2 = (Property)t2Properties.get(1);
        assertEquals("amount", t2p2.getName());
        assertEquals(doubleType, t2p2.getType());

        // repeated define
        List types2 = typeHelper.define(l1);
        assertEquals(2, types2.size());
        Type t21 = (Type)types2.get(0);
        checkType(t21);
        assertEquals("xxx", t21.getURI());
        assertEquals("TestType1", t21.getName());
        Type t22 = (Type)types2.get(1);
        checkType(t22);
        assertEquals("xxx", t22.getURI());
        assertEquals("TestType2", t22.getName());

        List l2 = new ArrayList();
        l2.add(prototype1);
        l2.add(prototype2);
        l2.add(prototype3);
        List types3 = typeHelper.define(l2);
        assertEquals(3, types3.size());
        Type t31 = (Type)types3.get(0);
        checkType(t31);
        assertEquals("xxx", t31.getURI());
        assertEquals("TestType1", t31.getName());
        Type t32 = (Type)types3.get(1);
        checkType(t32);
        assertEquals("xxx", t32.getURI());
        assertEquals("TestType2", t32.getName());
        Type t33 = (Type)types3.get(2);
        checkType(t33);
        assertEquals("xxx", t33.getURI());
        assertEquals("TestType3", t33.getName());
        List t3Properties = t33.getProperties();
        assertEquals(1, t3Properties.size());
        Property t3p1 = (Property)t3Properties.get(0);
        assertEquals("value", t3p1.getName());
        assertEquals(integerType, t3p1.getType());
    }
}
