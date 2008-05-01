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
package marshal;

import java.io.File;
import java.io.OutputStream;
import java.io.FileOutputStream;

import javax.sdo.DataObject;
import javax.sdo.Type;
import javax.sdo.Sequence;
import javax.sdo.helper.DataFactory;
import javax.sdo.helper.TypeHelper;
import javax.sdo.helper.XMLHelper;
import javax.sdo.helper.XMLDocument;

import junit.framework.Test;
import junit.framework.TestSuite;
import common.BaseTest;

/**
 * Test for marshalling of a non-closed datagraph.
 * @author Wing Yew Poon
 */
public class NonclosedDatagraphTest extends BaseTest
{
    public NonclosedDatagraphTest(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        
        TestSuite suite = new TestSuite();
        
        suite.addTest(new NonclosedDatagraphTest("testMarshal"));
        
        // or
        //TestSuite suite = new TestSuite(NonclosedDatagraphTest.class);
        return suite;
    }

    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(suite());
    }

    public static File dir;
    static
    {
        dir = new File(OUTPUTROOT + S + "marshal");
        dir.mkdirs();
    }

    private static DataFactory factory = context.getDataFactory();
    private static TypeHelper typeHelper = context.getTypeHelper();
    private static XMLHelper xmlHelper = context.getXMLHelper();

    public void testMarshal()
    {
        Type intType = typeHelper.getType("commonj.sdo", "Int");
        Type doubleType = typeHelper.getType("commonj.sdo", "Double");
        Type stringType = typeHelper.getType("commonj.sdo", "String");
        Type typeType = typeHelper.getType("commonj.sdo", "Type");
        String testURI = "http://www.bea.com/test/data/SequenceTest";
        DataObject buyType = factory.create(typeType);
        buyType.set("uri", testURI);
        buyType.set("name", "Buy");
        DataObject buySymbolProp = buyType.createDataObject("property");
        buySymbolProp.set("name", "symbol");
        buySymbolProp.set("type", stringType);
        DataObject buyQtyProp = buyType.createDataObject("property");
        buyQtyProp.set("name", "qty");
        buyQtyProp.set("type", intType);
        DataObject buyPriceProp = buyType.createDataObject("property");
        buyPriceProp.set("name", "price");
        buyPriceProp.set("type", doubleType);
        DataObject sellType = factory.create(typeType);
        sellType.set("uri", testURI);
        sellType.set("name", "Sell");
        DataObject sellSymbolProp = sellType.createDataObject("property");
        sellSymbolProp.set("name", "symbol");
        sellSymbolProp.set("type", stringType);
        DataObject sellQtyProp = sellType.createDataObject("property");
        sellQtyProp.set("name", "qty");
        sellQtyProp.set("type", intType);
        DataObject sellPriceProp = sellType.createDataObject("property");
        sellPriceProp.set("name", "price");
        sellPriceProp.set("type", doubleType);
        DataObject ordersType = factory.create(typeType);
        ordersType.set("uri", testURI);
        ordersType.set("name", "Orders");
        ordersType.set("sequenced", Boolean.TRUE);
        Type buy = typeHelper.define(buyType);
        Type sell = typeHelper.define(sellType);
        DataObject buyOrder = ordersType.createDataObject("property");
        buyOrder.set("name", "buy");
        buyOrder.set("type", buy);
        buyOrder.set("many", Boolean.TRUE);
        //buyOrder.set("containment", Boolean.TRUE);
        DataObject sellOrder = ordersType.createDataObject("property");
        sellOrder.set("name", "sell");
        sellOrder.set("type", sell);
        sellOrder.set("many", Boolean.TRUE);
        //sellOrder.set("containment", Boolean.TRUE);
        Type orders = typeHelper.define(ordersType);
        assertNotNull(orders);
        assertTrue(orders.isSequenced());

        // now create some orders
        DataObject o = factory.create(orders);
        DataObject b1 = factory.create(buy);
        b1.set("symbol", "ABC");
        b1.set("qty", 100);
        b1.set("price", 10.25);
        DataObject b2 = factory.create(buy);
        b2.set("symbol", "XYZ");
        b2.set("qty", 500);
        b2.set("price", 17.55);
        DataObject b3 = factory.create(buy);
        b3.set("symbol", "ABC");
        b3.set("qty", 200);
        b3.set("price", 10.35);
        DataObject s1 = factory.create(sell);
        s1.set("symbol", "DEF");
        s1.set("qty", 300);
        s1.set("price", 5.25);
        DataObject s2 = factory.create(sell);
        s2.set("symbol", "ABC");
        s2.set("qty", 300);
        s2.set("price", 10.5);
        Sequence seq = o.getSequence();
        seq.add("buy", b1);
        seq.add("sell", s1);
        seq.add("buy", b2);
        seq.add("buy", b3);
        seq.add("sell", s2);

        XMLDocument doc = xmlHelper.createDocument(o, testURI, "orders");

        try
        {
            System.out.println("first attempt to marshal");
            xmlHelper.save(doc, System.out, null);
            fail("Should have thrown a RuntimeException");
        }
        catch (Exception e)
        {
            //e.printStackTrace();
            assertTrue(e instanceof RuntimeException);
            String errorMsg = e.getMessage();
            String fragment1 = "reference to an object outside of the";
            String fragment2 = "tree";
            int i1 = errorMsg.indexOf(fragment1);
            int i2 = errorMsg.indexOf(fragment2, i1);
            assertTrue((i1 >= 0) && (i2 >= 0));
        }

        try
        {
            System.out.println("second attempt to marshal");
            davos.sdo.Options opt = new davos.sdo.Options();
            opt.setSaveDontThrowExceptions();
            xmlHelper.save(doc, System.out, opt);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            fail("No exception should have been thrown");
        }
    }
}
