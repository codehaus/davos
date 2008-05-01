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
package checkin;

import com.example.ipo.*;
import davos.sdo.impl.helpers.DataGraphHelper;
import davos.sdo.impl.common.Names;
import davos.sdo.impl.type.BuiltInTypeSystem;

import junit.framework.Test;
import junit.framework.TestSuite;
import common.BaseTest;
import javax.sdo.helper.XMLDocument;
import javax.sdo.helper.XMLHelper;
import javax.sdo.helper.DataFactory;
import javax.sdo.helper.TypeHelper;
import javax.sdo.DataObject;
import javax.sdo.ChangeSummary;
import javax.sdo.Type;
import javax.sdo.Property;
import javax.sdo.Sequence;

import java.io.File;
import java.io.FileReader;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;

/**
 * @author Radu Preotiuc-Pietro
 */
public class IdRefTest extends BaseTest
{

    public IdRefTest(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        TestSuite suite = new TestSuite();
        suite.addTest(new IdRefTest("testMarshal"));
        suite.addTest(new IdRefTest("testUnmarshal"));

        return suite;
    }

    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(suite());
    }

    private static DataFactory dataFactory = context.getDataFactory();
    private static XMLHelper xmlHelper = context.getXMLHelper();
    private static TypeHelper typeHelper = context.getTypeHelper();


    public void testMarshal()
    {
        // Build a Customer and PurchaseOrder from scratch and serialize them
        DataObject o = dataFactory.create("http://www.example.com/IPO", "customerList");

        Address address1 = (Address) dataFactory.create("http://www.example.com/IPO", "Address");
        address1.setName("John Jones");
        address1.setStreet("10 12th St");
        address1.setCity("Atlanta, GA");

        Address address2 = (Address) dataFactory.create("http://www.example.com/IPO", "Address");
        address2.setName("Martin Smith");
        address2.setStreet("333 North St");
        address2.setCity("Nashville, TN");

        Items items1 = (Items) dataFactory.create("http://www.example.com/IPO", "Items");
        items1.setProductName("Gizmo");

        Items items2 = (Items) dataFactory.create("http://www.example.com/IPO", "Items");
        items2.setProductName("iPod");

        PurchaseOrderType po1 = (PurchaseOrderType) dataFactory.create("http://www.example.com/IPO", "PurchaseOrderType");
        po1.setProductName("Gizmo");
        po1.setBillTo(address1);
        po1.setShipTo(address1);
        po1.setItems(items1);

        PurchaseOrderType po2 = (PurchaseOrderType) dataFactory.create("http://www.example.com/IPO", "PurchaseOrderType");
        po2.setProductName("iPod");
        po2.setBillTo(address2);
        po2.setShipTo(address2);
        po2.setItems(items2);

        Customer c01 = (Customer) dataFactory.create("http://www.example.com/IPO", "Customer");
        c01.setPrimaryKey("C01");

        Customer c02 = (Customer) dataFactory.create("http://www.example.com/IPO", "Customer");
        c02.setPrimaryKey("C02");

        List l = o.getList("customer");
        l.add(c01);
        l.add(c02);

        // Now on to the meat
        List poListOfc01 = c01.getPurchaseOrder();
        poListOfc01.add(po1);

        List poListOfc02 = c02.getPurchaseOrder();
        poListOfc02.add(po2);

        assertSame(c01, po1.getCustomer());
        assertSame(c02, po2.getCustomer());

        System.out.println("  Marshal 1: \n" + xmlHelper.save(o, "custListURI", "custList"));

        poListOfc01.set(0, po2);
        assertSame(c01, po2.getCustomer());
        assertSame(null, po1.getCustomer());

        poListOfc02.add(po1);
        assertSame(c01, po2.getCustomer());
        assertSame(c02, po1.getCustomer());

        System.out.println("  Marshal 2: \n" + xmlHelper.save(o, "custListURI", "custList"));

        poListOfc01.remove(0);
        assertSame(null, po2.getCustomer());

        poListOfc02.clear();
        assertSame(null, po1.getCustomer());

        System.out.println("  Marshal 3: \n" + xmlHelper.save(o, "custListURI", "custList"));

        poListOfc01.add(0, po1);
        poListOfc02.add(0, po2);
        assertSame(c01, po1.getCustomer());
        assertSame(c02, po2.getCustomer());

        System.out.println("  Marshal 4: \n" + xmlHelper.save(o, "custListURI", "custList"));
    }

    public void testUnmarshal()
        throws java.io.IOException
    {
        File f = getResourceFile("checkin", "purchaseOrder.xml");
        XMLDocument doc = xmlHelper.load(new FileReader(f), null, null);
        DataGraphHelper.wrapWithDataGraph(doc.getRootObject(), doc.getRootElementURI(),
            doc.getRootElementName());
        ChangeSummary c = doc.getRootObject().getChangeSummary();
        c.beginLogging();
        CustomerList custlist = (CustomerList) doc.getRootObject();

        Customer cust1 = (Customer) ((DataObject) custlist).getList("customer").get(0);
        PurchaseOrderType po = (PurchaseOrderType) ((DataObject) cust1).getList("purchaseOrder").get(0);
        Customer cust1_copy = po.getCustomer();
        assertSame(cust1, cust1_copy);

        System.out.println("  OUT1:\n" + xmlHelper.save(((DataObject) custlist).getRootObject(),
            Names.URI_SDO, Names.SDO_DATAGRAPH));

        Customer cust2 = (Customer) ((DataObject) custlist).getList("customer").get(1);
        PurchaseOrderType po2 = (PurchaseOrderType) ((DataObject) cust2).getList("purchaseOrder").get(0);
        Customer cust2_copy = po2.getCustomer();
        assertSame(cust2, cust2_copy);

        List l = ((DataObject) cust2).getList("purchaseOrder");        
        l.set(0, po);

        assertSame(cust2, po.getCustomer());
        assertSame(cust2, po.getContainer());
        assertNull(cust1.get("purchaseOrder[1]"));
        assertSame(po, cust2.get("purchaseOrder[1]"));
        // Let's serialize this as a DataGraph
        String out = xmlHelper.save(((DataObject) custlist).getRootObject(),
            Names.URI_SDO, Names.SDO_DATAGRAPH);
        System.out.println("out:\n" + out);
        
        int endChangeSummaryIndex = out.indexOf("</changeSummary>");
        // Gizmo product should appear only once in the final document
        int indexpo2 = out.indexOf("<productName>Gizmo</productName>", endChangeSummaryIndex);
        assertTrue(indexpo2 > 0);
        indexpo2 = out.indexOf("<productName>Gizmo</productName>", indexpo2 + 10);
        indexpo2 = out.indexOf("<productName>Gizmo</productName>", indexpo2 + 10);
        assertTrue(indexpo2 < 0);
        assertTrue(out.indexOf("customer=\"C01\"") > 0);
        // And once in the change summary
        indexpo2 = out.indexOf("<productName>Gizmo</productName>");
        assertTrue(indexpo2 < endChangeSummaryIndex);
        /* The serialized DataGraph should have one inserted object (po[productName="Gizmo"]
           inserted in C02) and two deleted objects (po[productName="Gizmo"] deleted from C01 and
           po[productName="iPod"] deleted from C02) */
        int i1, i2, i3;
        i1 = out.indexOf("create=\"") + 8;
        assertTrue(i1 > 8);
        i2 = out.indexOf("\"", i1 );
        i3 = out.indexOf("sdo:datagraph/ipo:customerList[1]/customer[2]/purchaseOrder[1]");
        assertTrue(i1 <= i3 && i3 < i2);
        i1 = out.indexOf("delete=\"") + 8;
        assertTrue(i1 > 8);
        i2 = out.indexOf("\"", i1);
        i3 = out.indexOf("sdo:datagraph/changeSummary/customer[1]/purchaseOrder[1]");
        assertTrue(i1 <= i3 && i3 < i2);
        i3 = out.indexOf("sdo:datagraph/changeSummary/customer[2]/purchaseOrder[1]");
        assertTrue(i1 <= i3 && i3 < i2);

        c.undoChanges();
        po = (PurchaseOrderType) cust1.get("purchaseOrder[1]");
        assertEquals("Gizmo", po.getProductName());
        assertSame(cust1, po.getContainer());
        assertSame(cust1, po.getCustomer());
        po = (PurchaseOrderType) cust2.get("purchaseOrder[1]");
        assertEquals("iPod", po.getProductName());
        assertSame(cust2, po.getContainer());
        assertSame(cust2, po.getCustomer());
    }
}
