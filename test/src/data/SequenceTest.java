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
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.List;

import javax.sdo.DataObject;
import javax.sdo.Sequence;
import javax.sdo.Type;
import javax.sdo.Property;
import javax.sdo.helper.DataFactory;
import javax.sdo.helper.TypeHelper;
import javax.sdo.helper.XMLHelper;
import javax.sdo.helper.XSDHelper;
import javax.sdo.helper.XMLDocument;
import davos.sdo.TypeXML;
import davos.sdo.Options;

import junit.framework.Test;
import junit.framework.TestSuite;
import common.DataTest;

/**
 * @author Wing Yew Poon
 */
public class SequenceTest extends DataTest
{
    public SequenceTest(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        
        TestSuite suite = new TestSuite();
        suite.addTest(new SequenceTest("testXMLWithoutSchema"));
        suite.addTest(new SequenceTest("testAnnotatedSchema"));
        suite.addTest(new SequenceTest("testUnannotatedSchema"));
        suite.addTest(new SequenceTest("testNonrecurringModelGroup"));
        suite.addTest(new SequenceTest("testRecurringModelGroup"));
        suite.addTest(new SequenceTest("testMixedContent1"));
        suite.addTest(new SequenceTest("testMixedContent2"));
        suite.addTest(new SequenceTest("testDynamicType"));
        suite.addTest(new SequenceTest("testSetPropertiesDirectly"));
        suite.addTest(new SequenceTest("testSequence"));
        suite.addTest(new SequenceTest("testIndexOutOfBounds"));
        suite.addTest(new SequenceTest("testLoad"));
        
        // or
        //TestSuite suite = new TestSuite(SequenceTest.class);
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

    private static final String TEST_NS1 = "http://www.example.com/choice";
    private static final String TEST_NS2 = "http://www.example.com/mixed";
    private static final String TEST_NS3 = "http://www.example.com/seq0";
    private static final String TEST_NS4 = "http://www.example.com/seq1";
    private static final String TEST_NS5 = "http://www.example.com/seq2";

    private static DataFactory factory = context.getDataFactory();
    private static TypeHelper typeHelper = context.getTypeHelper();
    private static XMLHelper xmlHelper = context.getXMLHelper();
    private static XSDHelper xsdHelper = context.getXSDHelper();

    static Type intType = typeHelper.getType("commonj.sdo", "Int");
    static Type doubleType = typeHelper.getType("commonj.sdo", "Double");
    static Type stringType = typeHelper.getType("commonj.sdo", "String");
    static Type objectType = typeHelper.getType("commonj.sdo", "Object");
    static Type dataObjectType = typeHelper.getType("commonj.sdo", "DataObject");
    static Type typeType = typeHelper.getType("commonj.sdo", "Type");
    private static Type buy;
    private static Type sell;
    private static Type orders;
    static
    {
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
        buy = typeHelper.define(buyType);
        sell = typeHelper.define(sellType);
        DataObject buyOrder = ordersType.createDataObject("property");
        buyOrder.set("name", "buy");
        buyOrder.set("type", buy);
        buyOrder.set("many", Boolean.TRUE);
        buyOrder.set("containment", Boolean.TRUE);
        DataObject sellOrder = ordersType.createDataObject("property");
        sellOrder.set("name", "sell");
        sellOrder.set("type", sell);
        sellOrder.set("many", Boolean.TRUE);
        sellOrder.set("containment", Boolean.TRUE);
        orders = typeHelper.define(ordersType);
    }

    private void _printProperties(Type t)
    {
        System.out.println("++++++++++++++++");
        List<Property> props = (List<Property>)t.getProperties();
        for (Property prop : props)
        {
            System.out.println(prop.getName());
        }
        System.out.println("++++++++++++++++");
    }

    private void _printProperties(DataObject dobj)
    {
        System.out.println("----------------");
        List<Property> props = (List<Property>)dobj.getInstanceProperties();
        for (Property prop : props)
        {
            System.out.println(prop.getName());
        }
        System.out.println("----------------");
    }

    private void _printSequence(Sequence sequence)
    {
        System.out.println("::::::::::::::::" + sequence.size());
        for (int i = 0; i < sequence.size(); i++)
        {
            Property p = sequence.getProperty(i);
            System.out.println(p.getName());
            System.out.println("  " + p.getType().getName());
            System.out.println("  " + p.getType().getURI());
            Object o = sequence.getValue(i);
            if (o instanceof DataObject)
                System.out.println("data object");
            else
                System.out.println(o);
        }
        System.out.println("::::::::::::::::");
    }

    private void _testText(Property p)
    {
        assertNull(p); // this is the 2.1 behavior
    }

    public void testXMLWithoutSchema() throws Exception
    {
        System.out.println("testXMLWithoutSchema()");
        File f = getResourceFile("data", "mixed_no_schema.xml");
        InputStream in = new FileInputStream(f);
        XMLHelper xmlHelper = context.getXMLHelper();
        XMLDocument doc = xmlHelper.load(in);
        in.close();
        DataObject root = doc.getRootObject();
        Type t = root.getType();
        assertTrue(t.isOpen());
        assertTrue(t.isSequenced());
        List props = root.getInstanceProperties();
        assertEquals(6, props.size());
        Property p1 = root.getInstanceProperty("st1");
        Property p2 = root.getInstanceProperty("st2");
        Property p3 = root.getInstanceProperty("st3");
        Property p4 = root.getInstanceProperty("st4");
        Property p5 = root.getInstanceProperty("ct1");
        Property p6 = root.getInstanceProperty("ct2");
        // all properties are open content, many-valued
        assertTrue(p1.isOpenContent());
        assertTrue(p2.isOpenContent());
        assertTrue(p3.isOpenContent());
        assertTrue(p4.isOpenContent());
        assertTrue(p5.isOpenContent());
        assertTrue(p6.isOpenContent());
        assertTrue(p1.isMany());
        assertTrue(p2.isMany());
        assertTrue(p3.isMany());
        assertTrue(p4.isMany());
        assertTrue(p5.isMany());
        assertTrue(p6.isMany());
        // st1, st4 - data type (Object)
        // st2, st3 - data object type (DataObject), containment
        // ct1, ct2 - data object type (DataObject), containment
        // difference between the last two is that
        // the former properties contain open, non-sequenced, wrapper
        // data objects and the latter properties contain open, sequenced
        // (and mixed content) data objects
        assertTrue(p1.getType().isDataType());
        assertFalse(p2.getType().isDataType());
        assertFalse(p3.getType().isDataType());
        assertTrue(p4.getType().isDataType());
        assertFalse(p5.getType().isDataType());
        assertFalse(p6.getType().isDataType());
        assertEquals(objectType, p1.getType());
        assertEquals(dataObjectType, p2.getType());
        assertEquals(dataObjectType, p3.getType());
        assertEquals(objectType, p4.getType());
        assertEquals(dataObjectType, p5.getType());
        assertEquals(dataObjectType, p6.getType());
        assertFalse(p1.isContainment());
        assertTrue(p2.isContainment());
        assertTrue(p3.isContainment());
        assertFalse(p4.isContainment());
        assertTrue(p5.isContainment());
        assertTrue(p6.isContainment());
        // object (String)
        assertEquals("simple content", root.getString("st1.0"));
        // wrapper data object
        assertEquals("more simple content", root.getDataObject("st2.0").get("value"));
        // wrapper data object
        assertEquals(100, root.getDataObject("st3.0").get("value"));
        assertEquals("xxx", root.getDataObject("st3.1").get("value"));
        // object (Integer)
        assertEquals(200, root.getInt("st4.0"));

        Sequence seq = root.getSequence();
        //_printSequence(seq);
        assertEquals(15, seq.size());
        Property p = seq.getProperty(0);
        _testText(p);
        String text = (String)seq.getValue(0);
        assertEquals("", text.trim());
        p = seq.getProperty(1);
        assertEquals("st1", p.getName());
        assertTrue(p == p1);
        assertEquals("simple content", seq.getValue(1));
        p = seq.getProperty(2);
        _testText(p);
        text = (String)seq.getValue(2);
        assertEquals("", text.trim());
        p = seq.getProperty(3);
        assertEquals("st2", p.getName());
        assertTrue(p == p2);
        Object st2 = seq.getValue(3);
        assertTrue(st2 instanceof DataObject);
        assertEquals("more simple content", ((DataObject)st2).get("value"));
        p = seq.getProperty(5);
        assertEquals("st3", p.getName());
        assertTrue(p == p3);
        assertEquals(100, ((DataObject)seq.getValue(5)).get("value"));
        assertTrue(p == seq.getProperty(7));
        assertEquals("xxx", ((DataObject)seq.getValue(7)).get("value"));
        p = seq.getProperty(9);
        assertEquals("st4", p.getName());
        assertTrue(p == p4);
        assertEquals(200, seq.getValue(9));
        
        p = seq.getProperty(11);
        assertEquals("ct1", p.getName());
        assertTrue(p == p5);
        DataObject ct1 = root.getDataObject("ct1.0");
        assertTrue(ct1 == seq.getValue(11));
        assertTrue(ct1.getType().isSequenced());
        Sequence ct1seq = ct1.getSequence();
        assertEquals(2, ct1seq.size());
        assertEquals("example", ct1.get("text"));
        assertEquals("text", ct1seq.getProperty(0).getName());
        assertEquals(stringType, ct1seq.getProperty(0).getType());
        assertEquals("example", ct1seq.getValue(0));
        _testText(ct1seq.getProperty(1));
        assertEquals("simple content", ct1seq.getValue(1));

        p = seq.getProperty(13);
        assertEquals("ct2", p.getName());
        assertTrue(p.isContainment());
        DataObject ct2 = root.getDataObject("ct2.0");
        assertTrue(ct2 == seq.getValue(13));
        assertTrue(ct2.getType().isSequenced());
        Sequence ct2seq = ct2.getSequence();
        assertEquals(4, ct2seq.size());
        assertEquals("mixed", ct2.get("content"));
        assertEquals("content", ct2seq.getProperty(0).getName());
        assertEquals(stringType, ct1seq.getProperty(0).getType());
        assertEquals("mixed", ct2seq.getValue(0));
        _testText(ct2seq.getProperty(1));
        assertEquals("this ", ct2seq.getValue(1));
        assertEquals("b", ct2seq.getProperty(2).getName());
        assertEquals("content", ct2seq.getValue(2));
        _testText(ct2seq.getProperty(3));
        assertEquals(" is mixed", ct2seq.getValue(3));
    }

    private void _testShirt(Sequence seq, int i)
    {
        Property p = seq.getProperty(i);
        assertEquals("shirt", p.getName());
        Type shirtType = typeHelper.getType(TEST_NS1, "ShirtType");
        assertNotNull(shirtType);
        assertEquals(shirtType, p.getType());
        Object shirt = seq.getValue(i);
        assertTrue(shirt instanceof DataObject);
    }

    private void _testPants(Sequence seq, int i)
    {
        Property p = seq.getProperty(i);
        assertEquals("pants", p.getName());
        Type pantsType = typeHelper.getType(TEST_NS1, "PantsType");
        assertNotNull(pantsType);
        assertEquals(pantsType, p.getType());
        Object pants = seq.getValue(i);
        assertTrue(pants instanceof DataObject);
    }

    // complex type with sequence model group and sequence="true" annotation
    public void testAnnotatedSchema() throws Exception
    {
        System.out.println("testAnnotatedSchema()");
        DataObject items = getRootDataObject("data", "seqitems1.xml");
        Type t = items.getType();
        assertEquals("ItemsType", t.getName());
        assertEquals(TEST_NS4, t.getURI());
        assertFalse(t.isOpen());
        assertTrue(t.isSequenced());
        Sequence seq = items.getSequence();
        assertEquals(3, seq.size());
        _testShirt(seq, 0);
        _testShirt(seq, 1);
        _testPants(seq, 2);
    }

    // complex type with sequence model group and no sequence="true" annotation
    public void testUnannotatedSchema() throws Exception
    {
        System.out.println("testUnannotatedSchema()");
        DataObject items = getRootDataObject("data", "seqitems0.xml");
        Type t = items.getType();
        assertEquals("ItemsType", t.getName());
        assertEquals(TEST_NS3, t.getURI());
        assertFalse(t.isOpen());
        assertFalse(t.isSequenced());
    }

    // complex type with sequence model group and group maxOccurs 1 but
    // with interleaved elements
    public void testNonrecurringModelGroup() throws Exception
    {
        System.out.println("testNonrecurringModelGroup()");
        DataObject items = getRootDataObject("data", "seqitems2.xml");
        Type t = items.getType();
        assertEquals("ItemsType", t.getName());
        assertEquals(TEST_NS5, t.getURI());
        assertFalse(t.isOpen());
        assertTrue(t.isSequenced());
        Sequence seq = items.getSequence();
        assertEquals(3, seq.size());
        _testShirt(seq, 0);
        _testPants(seq, 1);
        _testShirt(seq, 2);
    }

    // complex type with choice model group and group maxOccurs > 1
    public void testRecurringModelGroup() throws Exception
    {
        System.out.println("testRecurringModelGroup()");
        DataObject items = getRootDataObject("data", "items.xml");
        Type t = items.getType();
        assertEquals("ItemsType", t.getName());
        assertEquals(TEST_NS1, t.getURI());
        assertFalse(t.isOpen());
        assertTrue(t.isSequenced());
        Sequence seq = items.getSequence();
        assertEquals(3, seq.size());
        _testShirt(seq, 0);
        _testPants(seq, 1);
        _testShirt(seq, 2);
    }

    public void testMixedContent1() throws Exception
    {
        System.out.println("testMixedContent1()");
        InputStream resourceStream = 
            getResourceAsStream("data", "mixed.xsd");
        File resourceFile = getResourceFile("data", "mixed.xsd");
        xsdHelper.define(resourceStream, 
                         resourceFile.toString());
        resourceStream.close();
        Type quoteType = typeHelper.getType(TEST_NS2, "MixedQuote");
        assertFalse(quoteType.isOpen());
        assertTrue(quoteType.isSequenced());
        assertTrue(xsdHelper.isXSD(quoteType));
        assertTrue(xsdHelper.isMixed(quoteType));
        assertTrue(((TypeXML)quoteType).isMixedContent());
        DataObject quote = factory.create(quoteType);
        Sequence sequence = quote.getSequence();
        sequence.addText("\n  "); // 0
        quote.setString("symbol", "fbnt"); // 1
        sequence.addText("\n  "); // 2
        quote.setString("companyName", "FlyByNightTechnology"); // 3
        sequence.addText("\n  some text\n  "); // 4
        DataObject child = quote.createDataObject("quotes"); //5
        child.setBigDecimal("price", new BigDecimal("2000.0"));
        sequence.addText("\n  more text\n  "); // 6
        // quote.setBigDecimal("price", new BigDecimal("1000.0"));
        sequence.add("price", new BigDecimal("1000.0")); // 7
        sequence.addText("\n"); // 8
        assertEquals(9, sequence.size());
        sequence.addText(6, "yet");
        assertEquals(10, sequence.size());
        OutputStream out = new FileOutputStream(new File(dir, "mixed.xml"));
        xmlHelper.save(quote, TEST_NS2, "quote", out);
        out.close();
        compareXMLFiles(getResourceFile("data", "mixed.xml"), 
                        new File(dir, "mixed.xml"));
    }

    public void testMixedContent2() throws Exception
    {
        System.out.println("testMixedContent2()");
        DataObject letter = getRootDataObject("data", "letter.xml");
        //_printProperties(letter.getType());
        //_printProperties(letter);
        assertEquals("August 1, 2003", letter.getString("date"));
        assertEquals("Casy", letter.getString("firstName"));
        assertEquals("Crocodile", letter.getString("lastName"));
        String text1 = "\n" +
            "  Mutual of Omaha\n" + //newline +
            "  Wild Kingdom, USA\n" + //newline +
            "  Dear\n  "; // + newline + "  ";
        String text3 = "\n" +
            "  Please buy more shark repellent.\n" +
            "  Your premium is past due.\n";
        assertTrue(letter.getType().isSequenced());
        Sequence seq = letter.getSequence();
        assertEquals(7, seq.size());
        assertNull(seq.getProperty(0));
        assertEquals("", ((String)seq.getValue(0)).trim());
        assertNull(seq.getProperty(2));
        assertEquals(text1, seq.getValue(2));
        assertNull(seq.getProperty(4));
        assertEquals("", ((String)seq.getValue(4)).trim());
        assertNull(seq.getProperty(6));
        assertEquals(text3, seq.getValue(6));
        assertEquals("date", seq.getProperty(1).getName());
        assertEquals("August 1, 2003", seq.getValue(1));
        assertEquals("firstName", seq.getProperty(3).getName());
        assertEquals("Casy", seq.getValue(3));
        assertEquals("lastName", seq.getProperty(5).getName());
        assertEquals("Crocodile", seq.getValue(5));
    }

    private void _checkOrders(DataObject o)
    {
        Sequence seq = o.getSequence();
        assertNotNull(seq);
        for (int i = 0; i < seq.size(); i++)
        {
            System.out.println(seq.getProperty(i).getName());
            DataObject order = (DataObject)seq.getValue(i);
            System.out.println(order.get("symbol"));
            System.out.println(order.get("qty"));
            System.out.println(order.get("price"));
        }
        
        assertEquals(5, seq.size());
        assertEquals("buy", seq.getProperty(0).getName());
        assertEquals("sell", seq.getProperty(1).getName());
        assertEquals("buy", seq.getProperty(2).getName());
        assertEquals("buy", seq.getProperty(3).getName());
        assertEquals("sell", seq.getProperty(4).getName());
        assertEquals("ABC", ((DataObject)seq.getValue(0)).get("symbol"));
        assertEquals("DEF", ((DataObject)seq.getValue(1)).get("symbol"));
        assertEquals("XYZ", ((DataObject)seq.getValue(2)).get("symbol"));
        assertEquals("ABC", ((DataObject)seq.getValue(3)).get("symbol"));
        assertEquals("ABC", ((DataObject)seq.getValue(4)).get("symbol"));
        
        List buys = (List)o.get("buy");
        assertNotNull(buys);
        assertEquals(3, buys.size());
        System.out.println("**** BUYS ****");
        for (int i = 0; i < buys.size(); i++)
        {
            DataObject order = (DataObject)buys.get(i);
            System.out.println(order.get("symbol"));
            System.out.println(order.get("qty"));
            System.out.println(order.get("price"));
        }        
        List sells = (List)o.get("sell");
        assertNotNull(sells);
        assertEquals(2, sells.size());
        System.out.println("**** SELLS ****");
        for (int i = 0; i < sells.size(); i++)
        {
            DataObject order = (DataObject)sells.get(i);
            System.out.println(order.get("symbol"));
            System.out.println(order.get("qty"));
            System.out.println(order.get("price"));
        }
        /*
        util.DataObjectPrinter.printDataObject2(o);
        util.DataObjectPrinter.printDataObject(o);
        xmlHelper.save(o, testURI, "orders", System.out);
        */
    }

    // dynamically defined sequenced type
    public void testDynamicType() throws Exception
    {
        System.out.println("testDynamicType()");
        assertTrue(orders.isSequenced());

        // create some orders
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

        DataObject o = factory.create(orders);
        Sequence seq = o.getSequence();
        seq.add("buy", b1);
        seq.add("sell", s1);
        seq.add("buy", b2);
        seq.add("buy", b3);
        seq.add("sell", s2);
        _checkOrders(o);
    }

    /* set sequenced data object by setting its properties directly */
    public void testSetPropertiesDirectly() throws Exception
    {
        System.out.println("testSetPropertiesDirectly()");

        // create some orders
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

        DataObject o = factory.create(orders);
        List b = (List)o.get("buy");
        List s = (List)o.get("sell");
        b.add(b1);
        s.add(s1);
        b.add(b2);
        b.add(b3);
        s.add(s2);
        _checkOrders(o);
    }

    /* test Sequence interface;
       add(String) and add(int, String) is tested in testMixedContent1() */
    public void testSequence() throws Exception
    {
        System.out.println("testSequence()");
        Type itemsType = typeHelper.getType(TEST_NS1, "ItemsType");
        assertTrue(itemsType.isSequenced());
        // create some shirts and pants
        Property shirtProp = itemsType.getProperty("shirt");
        Property pantsProp = itemsType.getProperty("pants");
        assertEquals(shirtProp, (Property)itemsType.getProperties().get(0));
        assertEquals(pantsProp, (Property)itemsType.getProperties().get(1));
        Type shirtType = typeHelper.getType(TEST_NS1, "ShirtType");
        Type pantsType = typeHelper.getType(TEST_NS1, "PantsType");
        DataObject shirt1 = factory.create(shirtType);
        shirt1.set("color", "white");
        DataObject shirtSize = shirt1.createDataObject("size");
        shirtSize.set("collar", 15);
        shirtSize.set("sleeve", 33);
        shirt1.set("id", "RL-005");
        DataObject shirt2 = factory.create(shirtType);
        shirt2.set("color", "blue");
        shirtSize = shirt2.createDataObject("size");
        shirtSize.set("collar", 16);
        shirtSize.set("sleeve", 34);
        shirt2.set("id", "RL-006");
        DataObject shirt3 = factory.create(shirtType);
        shirt3.set("color", "french blue");
        shirtSize = shirt3.createDataObject("size");
        shirtSize.set("collar", 15);
        shirtSize.set("sleeve", 33);
        shirt3.set("id", "TH-134");
        DataObject shirt4 = factory.create(shirtType);
        shirt4.set("color", "pink");
        shirtSize = shirt4.createDataObject("size");
        shirtSize.set("collar", 16);
        shirtSize.set("sleeve", 34);
        shirt4.set("id", "RL-005");
        DataObject pants1 = factory.create(pantsType);
        pants1.set("color", "black");
        DataObject pantsSize = pants1.createDataObject("size");
        pantsSize.set("waist", 32);
        pantsSize.set("inseam", 32);
        pants1.set("id", "RL-799");
        DataObject pants2 = factory.create(pantsType);
        pants2.set("color", "navy");
        pantsSize = pants2.createDataObject("size");
        pantsSize.set("waist", 32);
        pantsSize.set("inseam", 32);
        pants2.set("id", "RL-799");
        DataObject pants3 = factory.create(pantsType);
        pants3.set("color", "slate");
        pantsSize = pants3.createDataObject("size");
        pantsSize.set("waist", 32);
        pantsSize.set("inseam", 32);
        pants3.set("id", "RL-799");
        // now we're ready to play
        DataObject items = factory.create(itemsType);
        Sequence seq = items.getSequence();
        assertTrue(seq.add("shirt", shirt1));
        OutputStream out = new FileOutputStream(new File(dir, "items1.xml"));
        XMLDocument doc = xmlHelper.createDocument(items, TEST_NS1, "items");
        xmlHelper.save(doc, out, new Options().setSavePrettyPrint());
        out.close();
        compareXMLFiles(getResourceFile("data", "items1.xml"), 
                        new File(dir, "items1.xml"), IGNORE_WHITESPACE);
        assertTrue(seq.add(0, shirt2));
        assertTrue(seq.add(shirtProp, shirt3));
        assertEquals(3, seq.size());
        out = new FileOutputStream(new File(dir, "items2.xml"));
        doc = xmlHelper.createDocument(items, TEST_NS1, "items");
        xmlHelper.save(doc, out, new Options().setSavePrettyPrint());
        out.close();
        compareXMLFiles(getResourceFile("data", "items2.xml"), 
                        new File(dir, "items2.xml"), IGNORE_WHITESPACE);
        assertEquals(shirtProp, seq.getProperty(0));
        assertEquals(shirtProp, seq.getProperty(1));
        assertEquals(shirtProp, seq.getProperty(2));
        DataObject shirt = (DataObject)seq.getValue(0);
        assertEquals(shirt1, shirt);
        shirt = (DataObject)seq.getValue(1);
        assertEquals(shirt2, shirt);
        shirt = (DataObject)seq.getValue(2);
        assertEquals(shirt3, shirt);
        seq.add(0, "pants", pants1); // add at beginning
        assertEquals(4, seq.size());
        out = new FileOutputStream(new File(dir, "items3.xml"));
        doc = xmlHelper.createDocument(items, TEST_NS1, "items");
        xmlHelper.save(doc, out, new Options().setSavePrettyPrint());
        out.close();
        compareXMLFiles(getResourceFile("data", "items3.xml"), 
                        new File(dir, "items3.xml"), IGNORE_WHITESPACE);
        seq.add(2, 1, pants2); // add in middle
        assertEquals(5, seq.size()); 
        seq.add(5, pantsProp, pants3); // add at end
        assertEquals(6, seq.size());
        assertEquals(pantsProp, seq.getProperty(0));
        assertEquals(shirtProp, seq.getProperty(1));
        assertEquals(pantsProp, seq.getProperty(2));
        assertEquals(shirtProp, seq.getProperty(3));
        assertEquals(shirtProp, seq.getProperty(4));
        assertEquals(pantsProp, seq.getProperty(5));
        assertEquals("black", ((DataObject)seq.getValue(0)).get("color"));
        assertEquals("white", ((DataObject)seq.getValue(1)).get("color"));
        assertEquals("navy", ((DataObject)seq.getValue(2)).get("color"));
        assertEquals("blue", ((DataObject)seq.getValue(3)).get("color"));
        assertEquals("french blue", ((DataObject)seq.getValue(4)).get("color"));
        assertEquals("slate", ((DataObject)seq.getValue(5)).get("color"));
        // test set
        seq.setValue(4, shirt4);
        assertEquals("pink", ((DataObject)seq.getValue(4)).get("color"));

        // test move
        // before
        assertEquals(pants1, seq.getValue(0));
        assertEquals(shirt1, seq.getValue(1));
        assertEquals(pants2, seq.getValue(2));
        assertEquals(shirt2, seq.getValue(3));
        assertEquals(shirt4, seq.getValue(4));
        assertEquals(pants3, seq.getValue(5));
        seq.move(4, 1); // 1 -> 4
        //after
        assertEquals(pants1, seq.getValue(0));
        assertEquals(pants2, seq.getValue(1));
        assertEquals(shirt2, seq.getValue(2));
        assertEquals(shirt4, seq.getValue(3));
        assertEquals(shirt1, seq.getValue(4));
        assertEquals(pants3, seq.getValue(5));
        seq.move(1, 4); // 4 -> 1
        assertEquals(pants1, seq.getValue(0));
        assertEquals(shirt1, seq.getValue(1));
        assertEquals(pants2, seq.getValue(2));
        assertEquals(shirt2, seq.getValue(3));
        assertEquals(shirt4, seq.getValue(4));
        assertEquals(pants3, seq.getValue(5));

        // test remove
        seq.remove(2);
        seq.remove(4);
        assertEquals(4, seq.size());
        assertEquals(pantsProp, seq.getProperty(0));
        assertEquals(shirtProp, seq.getProperty(1));
        assertEquals(shirtProp, seq.getProperty(2));
        assertEquals(shirtProp, seq.getProperty(3));
        assertEquals("black", ((DataObject)seq.getValue(0)).get("color"));
        assertEquals("white", ((DataObject)seq.getValue(1)).get("color"));
        assertEquals("blue", ((DataObject)seq.getValue(2)).get("color"));
        //assertEquals("french blue", ((DataObject)seq.getValue(3)).get("color"));
        assertEquals("pink", ((DataObject)seq.getValue(3)).get("color"));
        // test move
        seq.move(1, 0);
        //seq.move(0, 1);
        //System.out.println(((DataObject)seq.getValue(0)).get("color"));
        //System.out.println(((DataObject)seq.getValue(1)).get("color"));
        //System.out.println(((DataObject)seq.getValue(2)).get("color"));
        //System.out.println(((DataObject)seq.getValue(3)).get("color"));
        //System.out.println("--------");
        seq.move(2, 0);
        //seq.move(0, 2);
        //System.out.println(((DataObject)seq.getValue(0)).get("color"));
        //System.out.println(((DataObject)seq.getValue(1)).get("color"));
        //System.out.println(((DataObject)seq.getValue(2)).get("color"));
        //System.out.println(((DataObject)seq.getValue(3)).get("color"));
        //System.out.println("--------");
        seq.move(3, 0);
        //seq.move(1, 3);
        //System.out.println(((DataObject)seq.getValue(0)).get("color"));
        //System.out.println(((DataObject)seq.getValue(1)).get("color"));
        //System.out.println(((DataObject)seq.getValue(2)).get("color"));
        //System.out.println(((DataObject)seq.getValue(3)).get("color"));
        //System.out.println("--------");
        //seq.move(4, 0); //IndexOutOfBoundException
        assertEquals(shirtProp, seq.getProperty(0));
        assertEquals(shirtProp, seq.getProperty(1));
        assertEquals(shirtProp, seq.getProperty(2));
        assertEquals(pantsProp, seq.getProperty(3));
        //System.out.println(((DataObject)seq.getValue(0)).get("color"));
        //System.out.println(((DataObject)seq.getValue(1)).get("color"));
        //System.out.println(((DataObject)seq.getValue(2)).get("color"));
        //System.out.println(((DataObject)seq.getValue(3)).get("color"));
        //System.out.println("--------");
        assertEquals(shirt2, seq.getValue(0));
        assertEquals(shirt1, seq.getValue(1));
        assertEquals(shirt4, seq.getValue(2));
        assertEquals(pants1, seq.getValue(3));
        // test more remove
        seq.remove(3);
        assertEquals(3, seq.size());
        //System.out.println(((DataObject)seq.getValue(0)).get("color"));
        //System.out.println(((DataObject)seq.getValue(1)).get("color"));
        //System.out.println(((DataObject)seq.getValue(2)).get("color"));
        //System.out.println("::::::::");
        seq.remove(1);
        assertEquals(2, seq.size());
        //System.out.println(((DataObject)seq.getValue(0)).get("color"));
        //System.out.println(((DataObject)seq.getValue(1)).get("color"));
        //System.out.println("::::::::");
        seq.remove(0);
        assertEquals(1, seq.size());
        //System.out.println(((DataObject)seq.getValue(0)).get("color"));
        assertEquals(shirt4, seq.getValue(0));
    }

    public void testIndexOutOfBounds() throws Exception
    {
        System.out.println("testIndexOutOfBounds()");
        DataObject items = getRootDataObject("data", "items.xml");
        assertTrue(items.getType().isSequenced());
        Sequence seq = items.getSequence();
        assertEquals(3, seq.size());
        try
        {
            Object value = seq.getValue(4);
            fail("should have thrown an IndexOutOfBoundsException");
        }
        catch (Exception e)
        {
            assertTrue(e instanceof IndexOutOfBoundsException);
        }
    }

    public void testLoad() throws Exception
    {
        DataObject items;
        items = getRootDataObject("data", "items.xml");
        System.out.println(items.getType());
        items = getRootDataObject("data", "seqitems0.xml");
        System.out.println(items.getType());
        items = getRootDataObject("data", "seqitems1.xml");
        System.out.println(items.getType());
        items = getRootDataObject("data", "seqitems2.xml");
        System.out.println(items.getType());
        items = getRootDataObject("data", "items.xml");
        System.out.println(items.getType());
    }
}
