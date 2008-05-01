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

import java.io.*;
import java.util.*;
import javax.sdo.*;
import javax.sdo.helper.*;
import davos.sdo.type.TypeSystem;
import davos.sdo.impl.type.TypeSystemBase;
import junit.framework.*;
import common.BaseTest;

/**
 * @author Wing Yew Poon
 */
public class CustomerBaseViewTest extends BaseTest
{
    public CustomerBaseViewTest(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        
        TestSuite suite = new TestSuite();
        suite.addTest(new CustomerBaseViewTest("testLoad"));
        suite.addTest(new CustomerBaseViewTest("testTypes"));
        
        // or
        //TestSuite suite = new TestSuite(CustomerBaseViewTest.class);
        return suite;
    }

    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(suite());
    }

    private static TypeHelper typeHelper = context.getTypeHelper();
    private static XMLHelper xmlHelper = context.getXMLHelper();

    private void dump(List props)
    {
        Iterator i;
        int j;
        for (i = props.iterator(), j = 1; i.hasNext(); j++)
        {
            System.out.println(j);
            Property p = (Property)i.next();
            System.out.println(p.getName());
            Type t = p.getType();
            System.out.println(t.getName() + "@" + t.getURI());
        }
    }

    private void _testProfile(List props)
    {
        assertEquals(11, props.size());
        Property p;
        Type t;
        p = (Property)props.get(0);
        t = p.getType();
        assertEquals("CustomerID", p.getName());
        assertEquals(typeHelper.getType("commonj.sdo", "String"), t);
        p = (Property)props.get(1);
        t = p.getType();
        assertEquals("FirstName", p.getName());
        assertEquals(typeHelper.getType("commonj.sdo", "String"), t);
        p = (Property)props.get(2);
        t = p.getType();
        assertEquals("LastName", p.getName());
        assertEquals(typeHelper.getType("commonj.sdo", "String"), t);
        p = (Property)props.get(3);
        t = p.getType();
        assertEquals("CustomerSince", p.getName());
        assertEquals(typeHelper.getType("commonj.sdo", "YearMonthDay"), t);
        p = (Property)props.get(4);
        t = p.getType();
        assertEquals("EmailAddress", p.getName());
        assertEquals(typeHelper.getType("commonj.sdo", "String"), t);
        p = (Property)props.get(5);
        t = p.getType();
        assertEquals("TelephoneNumber", p.getName());
        assertEquals(typeHelper.getType("commonj.sdo", "String"), t);
        p = (Property)props.get(6);
        t = p.getType();
        assertEquals("DefaultShipmentMethod", p.getName());
        assertEquals("GROUND", (String)p.getDefault());
        assertEquals(typeHelper.getType("commonj.sdo", "String"), t);
        p = (Property)props.get(7);
        t = p.getType();
        assertEquals("EmailNotification", p.getName());
        assertEquals(new Short((short)1), (Short)p.getDefault());
        assertEquals(typeHelper.getType("commonj.sdo", "Short"), t);
        p = (Property)props.get(8);
        t = p.getType();
        assertEquals("OnlineStatement", p.getName());
        assertEquals(typeHelper.getType("commonj.sdo", "Short"), t);
        p = (Property)props.get(9);
        t = p.getType();
        assertEquals("LoginID", p.getName());
        assertEquals(typeHelper.getType("commonj.sdo", "String"), t);
        p = (Property)props.get(10);
        t = p.getType();
        assertEquals("ADDRESS", p.getName());
        assertEquals("ADDRESS_TYPE", t.getName());
        assertEquals("urn:Retail", t.getURI());
        List props1 = t.getProperties();
        dump(props1);
        _testAddress(props1);
    }

    private void _testAddress(List props)
    {
        assertEquals(15, props.size());
        Property p;
        Type t;
        p = (Property)props.get(0);
        t = p.getType();
        assertEquals("AddressID", p.getName());
        assertEquals(typeHelper.getType("commonj.sdo", "String"), t);
        p = (Property)props.get(1);
        t = p.getType();
        assertEquals("CustomerID", p.getName());
        assertEquals(typeHelper.getType("commonj.sdo", "String"), t);
        p = (Property)props.get(2);
        t = p.getType();
        assertEquals("FirstName", p.getName());
        assertEquals(typeHelper.getType("commonj.sdo", "String"), t);
        p = (Property)props.get(3);
        t = p.getType();
        assertEquals("LastName", p.getName());
        assertEquals(typeHelper.getType("commonj.sdo", "String"), t);
        p = (Property)props.get(4);
        t = p.getType();
        assertEquals("StreetAddress_1", p.getName());
        assertEquals(typeHelper.getType("commonj.sdo", "String"), t);
        p = (Property)props.get(5);
        t = p.getType();
        assertEquals("StreetAddress_2", p.getName());
        assertEquals(typeHelper.getType("commonj.sdo", "String"), t);
        p = (Property)props.get(6);
        t = p.getType();
        assertEquals("City", p.getName());
        assertEquals(typeHelper.getType("commonj.sdo", "String"), t);
        p = (Property)props.get(7);
        t = p.getType();
        assertEquals("State", p.getName());
        assertEquals(typeHelper.getType("commonj.sdo", "String"), t);
        p = (Property)props.get(8);
        t = p.getType();
        assertEquals("ZipCode", p.getName());
        assertEquals(typeHelper.getType("commonj.sdo", "String"), t);
        p = (Property)props.get(9);
        t = p.getType();
        assertEquals("Country", p.getName());
        assertEquals("USA", (String)p.getDefault());
        assertEquals(typeHelper.getType("commonj.sdo", "String"), t);
        p = (Property)props.get(10);
        t = p.getType();
        assertEquals("DayPhone", p.getName());
        assertEquals(typeHelper.getType("commonj.sdo", "String"), t);
        p = (Property)props.get(11);
        t = p.getType();
        assertEquals("EveningPhone", p.getName());
        assertEquals(typeHelper.getType("commonj.sdo", "String"), t);
        p = (Property)props.get(12);
        t = p.getType();
        assertEquals("Alias", p.getName());
        assertEquals(typeHelper.getType("commonj.sdo", "String"), t);
        p = (Property)props.get(13);
        t = p.getType();
        assertEquals("Status", p.getName());
        assertEquals(typeHelper.getType("commonj.sdo", "String"), t);
        p = (Property)props.get(14);
        t = p.getType();
        assertEquals("IsDefault", p.getName());
        assertEquals(typeHelper.getType("commonj.sdo", "Short"), t);
    }

    public void testTypes()
    {
        TypeSystem ts = context.getTypeSystem();
        Type cbv_by_class = typeHelper.getType(customerbaseview.CUSTOMER_BASE_VIEW.class);
        Type cbv_by_name = typeHelper.getType("urn:CustomerBaseView", "CUSTOMER_BASE_VIEW");
        //((TypeSystemBase)ts).dumpWithoutBuiltinTypes();
        assertNotNull(cbv_by_class);
        assertNotNull(cbv_by_name);
        assertEquals(cbv_by_class, cbv_by_name);
        Type cbv = cbv_by_name;
        List props = cbv.getProperties();
        dump(props);
        assertEquals(4, props.size());
        Property p1 = (Property)props.get(0);
        assertEquals("PROFILE", p1.getName());
        Type t1 = p1.getType();
        Property p2 = (Property)props.get(1);
        assertEquals("CREDIT_CARDS", p2.getName());
        Type t2 = p2.getType();
        Property p3 = (Property)props.get(2);
        assertEquals("ORDERS", p3.getName());
        Type t3 = p3.getType();
        Property p4 = (Property)props.get(3);
        assertEquals("CASES", p4.getName());
        Type t4 = p4.getType();

        assertEquals("PROFILE_TYPE", t1.getName());
        assertEquals("urn:Retail", t1.getURI());
        List props1 = t1.getProperties(); // profile properties
        dump(props1);
        _testProfile(props1);
    }

    public void testLoad() throws Exception
    {
        TypeSystem ts = context.getTypeSystem();
        ((TypeSystemBase)ts).dumpWithoutBuiltinTypes();
        
        File f = new File(RESOURCES + S + 
                          "sdocomp" + S + "customers" + S + "cbv" + S + 
                          "cbv.xml");
        InputStream in = new FileInputStream(f);
        XMLDocument doc = xmlHelper.load(in);
        in.close();

        ((TypeSystemBase)ts).dumpWithoutBuiltinTypes();

        DataObject root = doc.getRootObject();
        DataObject view = root.getDataObject("CUSTOMER_BASE_VIEW[1]");
        assertNotNull(view);
        DataObject profile = view.getDataObject("PROFILE");
        assertNotNull(profile);
        assertEquals("string", profile.get("DefaultShipmentMethod"));
        // when a property is unset, get should return the default
        profile.unset("DefaultShipmentMethod");
        assertEquals("GROUND", profile.get("DefaultShipmentMethod"));
        assertEquals((short)1, profile.get("EmailNotification"));
        profile.unset("EmailNotification");
        assertEquals((short)1, profile.get("EmailNotification"));
        DataObject address = profile.getDataObject("ADDRESS[1]");
        assertNotNull(address);
        address.unset("Country");
        assertEquals("USA", address.get("Country"));
    }
}
