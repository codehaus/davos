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
import java.util.List;

import javax.sdo.DataObject;
import javax.sdo.Type;
import javax.sdo.Property;
import javax.sdo.helper.CopyHelper;
import javax.sdo.helper.EqualityHelper;
import javax.sdo.helper.TypeHelper;
import javax.sdo.helper.DataFactory;
import javax.sdo.helper.XSDHelper;

import junit.framework.Test;
import junit.framework.TestSuite;
import common.DataTest;

/**
 * test cases for copy and equality
 * @author Wing Yew Poon
 */
public class CopyAndEqualityTest extends DataTest
{
    public CopyAndEqualityTest(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        
        TestSuite suite = new TestSuite();
        suite.addTest(new CopyAndEqualityTest("testEquality"));
        suite.addTest(new CopyAndEqualityTest("testSimpleDataObject"));
        suite.addTest(new CopyAndEqualityTest("testReferenceProperties"));
        suite.addTest(new CopyAndEqualityTest("testReferencesOnly"));
        suite.addTest(new CopyAndEqualityTest("testReadOnlyReference"));
        suite.addTest(new CopyAndEqualityTest("testContainment1"));
        suite.addTest(new CopyAndEqualityTest("testContainment2"));
        suite.addTest(new CopyAndEqualityTest("testOppositeProperties1"));
        suite.addTest(new CopyAndEqualityTest("testOppositeProperties2"));
        suite.addTest(new CopyAndEqualityTest("testByteArray"));
        
        // or
        //TestSuite suite = new TestSuite(CopyAndEqualityTest.class);
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

    private static CopyHelper copyHelper = context.getCopyHelper();
    private static EqualityHelper equalityHelper = context.getEqualityHelper();
    private static TypeHelper typeHelper = context.getTypeHelper();
    private static DataFactory dataFactory = context.getDataFactory();
    private static XSDHelper xsdHelper = context.getXSDHelper();

    /** some basic negative equality tests */
    public void testEquality() throws Exception
    {
        // null parameter
        DataObject dobj1 = getRootDataObject("data", "copy1.xml");
        DataObject dobj2 = getRootDataObject("data", "copy2.xml");
        assertFalse(equalityHelper.equalShallow(dobj1, null));
        assertFalse(equalityHelper.equal(dobj1, null));
        assertTrue(equalityHelper.equalShallow(null, null));
        assertTrue(equalityHelper.equal(null, null));
        // two data objects of different types
        assertFalse(equalityHelper.equalShallow(dobj1, dobj2));
        assertFalse(equalityHelper.equal(dobj1, dobj2));
    }

    private void _testBasic(DataObject dobj, 
                            DataObject shallowCopy, 
                            DataObject deepCopy) throws Exception
    {
        assertFalse(dobj == shallowCopy);
        assertFalse(dobj == deepCopy);
        assertFalse(shallowCopy == deepCopy);
        assertTrue(equalityHelper.equalShallow(dobj, shallowCopy));
        assertFalse(equalityHelper.equal(dobj, shallowCopy));
        assertTrue(equalityHelper.equalShallow(dobj, deepCopy));
        assertTrue(equalityHelper.equal(dobj, deepCopy));
        assertTrue(equalityHelper.equalShallow(shallowCopy, deepCopy));
        assertFalse(equalityHelper.equal(shallowCopy, deepCopy));

        // meta-data are identical
        assertTrue(dobj.getType() == shallowCopy.getType());
        assertTrue(dobj.getType() == deepCopy.getType());
    }

    private void _testEqual(DataObject dobj, DataObject copy) throws Exception
    {
        assertTrue(dobj.getType() == copy.getType());
        assertTrue(equalityHelper.equal(dobj, copy));
    }

    /* simple data object, only has datatype properties:
       shallow copy and deep copy are the same */
    public void testSimpleDataObject() throws Exception
    {
        DataObject quote = getRootDataObject("data", "copy1.xml");
        /*
        Type t = typeHelper.getType("http://sdo/test/copy1", "QuoteType");
        DataObject quote = dataFactory.create(t);
        Property symbol = t.getProperty("symbol");
        Property quantity = t.getProperty("quantity");
        //assertEquals(100, ((Integer)quantity.getDefault()).intValue());
        Property price = t.getProperty("price");
        assertTrue(price.isMany());
        System.out.println("symbol: " + quote.get(symbol));
        System.out.println("quantity(default): " + quantity.getDefault());
        System.out.println("quantity: " + quote.get(quantity));
        quote.setString("symbol", "XYZ");
        quote.setInt("quantity", 300);
        List priceList = quote.getList("price");
        priceList.add(new Double(14.25));
        priceList.add(new Double(14.75));
        priceList.add(new Double(14.5));
        */
        System.out.println(quote.getList("price").size());
        System.out.println(quote.getList("price").get(0));
        System.out.println(quote.getList("price").get(1));
        System.out.println(quote.getList("price").get(2));

        DataObject shallowCopy = copyHelper.copyShallow(quote);
        DataObject deepCopy = copyHelper.copy(quote);
        assertFalse(quote == shallowCopy);
        assertFalse(quote == deepCopy);
        assertFalse(shallowCopy == deepCopy);
        assertTrue(equalityHelper.equalShallow(quote, shallowCopy));
        assertTrue(equalityHelper.equal(quote, shallowCopy));
        assertTrue(equalityHelper.equalShallow(quote, deepCopy));
        assertTrue(equalityHelper.equal(quote, deepCopy));
        assertTrue(equalityHelper.equalShallow(shallowCopy, deepCopy));
        assertTrue(equalityHelper.equal(shallowCopy, deepCopy));

        // meta-data are identical
        assertTrue(quote.getType() == shallowCopy.getType());
        assertTrue(quote.getType() == deepCopy.getType());

        // datatype values are equal
        assertTrue(quote.get("symbol").equals(shallowCopy.get("symbol")));
        assertTrue(quote.get("quantity").equals(shallowCopy.get("quantity")));

        System.out.println("shallow copy:");
        System.out.println(shallowCopy.getList("price").size());
        System.out.println(shallowCopy.getList("price").get(0));
        System.out.println(shallowCopy.getList("price").get(1));
        System.out.println(shallowCopy.getList("price").get(2));
        System.out.println("deep copy:");
        System.out.println(deepCopy.getList("price").size());
        System.out.println(deepCopy.getList("price").get(0));
        System.out.println(deepCopy.getList("price").get(1));
        System.out.println(deepCopy.getList("price").get(2));

        assertTrue(quote.getList("price").size() == shallowCopy.getList("price").size());
        assertTrue(quote.getList("price").get(0).equals(shallowCopy.getList("price").get(0)));
        assertTrue(quote.getList("price").get(1).equals(shallowCopy.getList("price").get(1)));
        assertTrue(quote.getList("price").get(2).equals(shallowCopy.getList("price").get(2)));

        assertTrue(quote.get("symbol").equals(deepCopy.get("symbol")));
        assertTrue(quote.get("quantity").equals(deepCopy.get("quantity")));
        assertTrue(quote.getList("price").size() == deepCopy.getList("price").size());
        assertTrue(quote.getList("price").get(0).equals(deepCopy.getList("price").get(0)));
        assertTrue(quote.getList("price").get(1).equals(deepCopy.getList("price").get(1)));
        assertTrue(quote.getList("price").get(2).equals(deepCopy.getList("price").get(2)));

        // read-only property is copied
        Property comment = quote.getType().getProperty("comment");
        assertTrue(comment.isReadOnly());
        assertEquals("hold", quote.get(comment));
        assertTrue(quote.get(comment).equals(shallowCopy.get(comment)));
        assertTrue(quote.get(comment).equals(deepCopy.get(comment)));
    }

    /* data object with both datatype and reference properties */
    public void testReferenceProperties() throws Exception
    {
        System.out.println("testReferenceProperties()");
        DataObject quote = getRootDataObject("data", "copy2.xml");
        DataObject shallowCopy = copyHelper.copyShallow(quote);
        DataObject deepCopy = copyHelper.copy(quote);
        _testBasic(quote, shallowCopy, deepCopy);

        // datatype values are equal
        assertTrue(quote.get("symbol").equals(shallowCopy.get("symbol")));
        assertTrue(quote.get("quantity").equals(shallowCopy.get("quantity")));
        assertTrue(quote.get("symbol").equals(deepCopy.get("symbol")));
        assertTrue(quote.get("quantity").equals(deepCopy.get("quantity")));

        // compare reference properties
        assertFalse(shallowCopy.isSet("price"));
        assertEquals(0, shallowCopy.getList("price").size());
        List<DataObject> priceList = quote.getList("price");
        List<DataObject> priceListCopy = deepCopy.getList("price");
        assertTrue(priceList.size() == priceListCopy.size());
        assertTrue(equalityHelper.equal(priceList.get(0), priceListCopy.get(0)));
        assertTrue(equalityHelper.equal(priceList.get(1), priceListCopy.get(1)));
        assertTrue(equalityHelper.equal(priceList.get(2), priceListCopy.get(2)));
        assertEquals(priceList.get(0).get("currency"), priceListCopy.get(0).get("currency"));
        assertEquals(priceList.get(0).get("amount"), priceListCopy.get(0).get("amount"));
        assertEquals(priceList.get(1).get("currency"), priceListCopy.get(1).get("currency"));
        assertEquals(priceList.get(1).get("amount"), priceListCopy.get(1).get("amount"));
        assertEquals(priceList.get(2).get("currency"), priceListCopy.get(2).get("currency"));
        assertEquals(priceList.get(2).get("amount"), priceListCopy.get(2).get("amount"));
    }

    /* data object with only reference propertis */
    public void testReferencesOnly() throws Exception
    {
        System.out.println("testReferencesOnly()");
        DataObject quote = getRootDataObject("data", "copy2g.xml");
        DataObject shallowCopy = copyHelper.copyShallow(quote);
        DataObject deepCopy = copyHelper.copy(quote);
        _testBasic(quote, shallowCopy, deepCopy);
    }

    /* data object with read-only containment property */
    public void testReadOnlyReference() throws Exception
    {
        System.out.println("testReadOnlyReference()");
        DataObject quote = getRootDataObject("data", "copy2r.xml");
        DataObject shallowCopy = copyHelper.copyShallow(quote);
        DataObject deepCopy = copyHelper.copy(quote);
        _testBasic(quote, shallowCopy, deepCopy);

        Property reserve = quote.getType().getProperty("reserve");
        assertTrue(reserve.isReadOnly());
        assertFalse(shallowCopy.isSet(reserve));
        assertTrue(deepCopy.isSet(reserve));
        assertTrue(equalityHelper.equal(quote.getDataObject(reserve), 
                                        deepCopy.getDataObject(reserve)));
        assertEquals(14.0, quote.getDouble("reserve/amount"));
        assertEquals(14.0, deepCopy.getDouble("reserve/amount"));
    }

    /* data object with both containment and non-containment references: 
       1. (uni-directional) non-containment reference within copy tree
       2. (uni-directional) non-containment reference outside copy tree
    */
    public void testContainment1() throws Exception
    {
        System.out.println("testContainment1()");
        DataObject a0 = getRootDataObject("data", "copy3a.xml");
        DataObject a1 = copyHelper.copyShallow(a0);
        DataObject a2 = copyHelper.copy(a0);
        //_testBasic(a0, a1, a2);
        _testEqual(a0, a2);

        // shallow copy contains nothing!
        assertFalse(a1.isSet("b"));
        assertNull(a1.get("b"));
        assertFalse(a1.isSet("c"));
        assertNull(a1.get("c"));

        // compare deep copy with original
        DataObject b0 = a0.getDataObject("b");
        DataObject c0 = a0.getDataObject("c");
        DataObject b2 = a2.getDataObject("b");
        DataObject c2 = a2.getDataObject("c");
        _testEqual(b0, b2);
        _testEqual(c0, c2);
        // test non-containment reference
        String testURI = "http://sdo/test/copy3a";
        Type t = typeHelper.getType(testURI, "E");
        Object d0 = b0.get("d");
        assertTrue(d0 instanceof DataObject);
        assertEquals(t, ((DataObject)d0).getType());
        Object d2 = b2.get("d");
        assertTrue(d2 instanceof DataObject);
        assertTrue(equalityHelper.equal((DataObject)d0, (DataObject)d2));
        assertFalse(d0 == d2);
        assertEquals(t, ((DataObject)d2).getType());
        assertEquals("E002", ((DataObject)d0).get("e0"));
        assertEquals("E002", ((DataObject)d2).get("e0"));
        List<DataObject> e0 = b0.getList("e");
        List<DataObject> e2 = b2.getList("e");
        assertTrue(e0.size() == e2.size());
        assertEquals(e0.get(0).get("e0"), e2.get(0).get("e0"));
        assertEquals(e0.get(1).get("e0"), e2.get(1).get("e0"));
        assertEquals(e0.get(2).get("e0"), e2.get(2).get("e0"));

        // marshal the copy and compare with original
        saveDataObject(a2, testURI, "a", new File(dir, "copy3a.xml"));
        compareXMLFiles(getResourceFile("data", "copy3a.xml"),
                        new File(dir, "copy3a.xml"));
    }

    /* data object with both containment and non-containment references: 
       1. (uni-directional) non-containment reference within copy tree
       2. (uni-directional) non-containment reference outside copy tree
    */
    public void testContainment2() throws Exception
    {
        System.out.println("testContainment2()");
        DataObject a = getRootDataObject("data", "copy3b.xml");
        DataObject b1 = a.getDataObject("b");
        DataObject b2 = copyHelper.copy(b1);
        _testEqual(b1, b2);

        // test non-containment reference
        String testURI = "http://sdo/test/copy3b";
        Type t = typeHelper.getType(testURI, "C");
        Object d1 = b1.get("d");
        assertTrue(d1 instanceof DataObject);
        assertEquals(t, ((DataObject)d1).getType());
        assertEquals("C123", ((DataObject)d1).get("c0"));
        Object d2 = b2.get("d");
        assertTrue(d1 == d2);
        // compare the rest
        List<DataObject> e1 = b1.getList("e");
        List<DataObject> e2 = b2.getList("e");
        assertTrue(e1.size() == e2.size());
        assertEquals(e1.get(0).get("e0"), e2.get(0).get("e0"));
        assertEquals(e1.get(1).get("e0"), e2.get(1).get("e0"));
        assertEquals(e1.get(2).get("e0"), e2.get(2).get("e0"));
    }

    /* data object with bi-directional properties:
       1. bi-directional property with opposite within copy tree
       2. bi-directional property with opposite outside copy tree
    */
    public void testOppositeProperties1() throws Exception
    {
        System.out.println("testOppositeProperties1()");
        DataObject a1 = getRootDataObject("data", "copy4.xml");
        DataObject a2 = copyHelper.copy(a1);
        //_testEqual(a1, a2);

        DataObject b1 = a1.getDataObject("b");
        DataObject b2 = a2.getDataObject("b");
        assertEquals(b1.getBoolean("d"), b2.getBoolean("d"));

        // test non-containment reference
        String testURI = "http://sdo/test/copy4";
        Type cType = typeHelper.getType(testURI, "C");
        Type eType = typeHelper.getType(testURI, "E");
        List<DataObject> e1 = b1.getList("e");
        assertEquals(3, e1.size());
        assertEquals(eType, e1.get(0).getType());
        assertEquals(cType, e1.get(0).getDataObject("c").getType());
        assertEquals(eType, e1.get(1).getType());
        assertEquals(cType, e1.get(1).getDataObject("c").getType());
        assertEquals(eType, e1.get(2).getType());
        assertEquals(cType, e1.get(2).getDataObject("c").getType());
        List<DataObject> e2 = b2.getList("e");
        assertEquals(3, e2.size());
        assertEquals(eType, e2.get(0).getType());
        assertEquals(cType, e2.get(0).getDataObject("c").getType());
        assertEquals(eType, e2.get(1).getType());
        assertEquals(cType, e2.get(1).getDataObject("c").getType());
        assertEquals(eType, e2.get(2).getType());
        assertEquals(cType, e2.get(2).getDataObject("c").getType());

        assertEquals("C456", e1.get(0).getDataObject("c").get("c0"));
        assertEquals("C123", e1.get(1).getDataObject("c").get("c0"));
        assertEquals("C456", e1.get(2).getDataObject("c").get("c0"));
        assertEquals("C456", e2.get(0).getDataObject("c").get("c0"));
        assertEquals("C123", e2.get(1).getDataObject("c").get("c0"));
        assertEquals("C456", e2.get(2).getDataObject("c").get("c0"));

        assertFalse(e1.get(0).getDataObject("c") == e2.get(0).getDataObject("c"));
        assertFalse(e1.get(1).getDataObject("c") == e2.get(1).getDataObject("c"));
        assertFalse(e1.get(2).getDataObject("c") == e2.get(2).getDataObject("c"));
        /*
        assertTrue(equalityHelper.equal(e1.get(0).getDataObject("c"),
                                        e2.get(0).getDataObject("c")));
        assertTrue(equalityHelper.equal(e1.get(1).getDataObject("c"),
                                        e2.get(1).getDataObject("c")));
        assertTrue(equalityHelper.equal(e1.get(1).getDataObject("c"),
                                        e2.get(1).getDataObject("c")));
        */

        // marshal the copy and compare with original
        saveDataObject(a2, testURI, "a", new File(dir, "copy4.xml"));
        compareXMLFiles(getResourceFile("data", "copy4.xml"),
                        new File(dir, "copy4.xml"));
    }

    /* data object with bi-directional properties:
       1. bi-directional property with opposite within copy tree
       2. bi-directional property with opposite outside copy tree
    */
    public void testOppositeProperties2() throws Exception
    {
        System.out.println("testOppositeProperties2()");
        DataObject a = getRootDataObject("data", "copy4.xml");
        DataObject b1 = a.getDataObject("b");
        DataObject b2 = copyHelper.copy(b1);
        assertTrue(b1.getType() == b2.getType());
        assertFalse(equalityHelper.equal(b1, b2));

        // test non-containment reference
        String testURI = "http://sdo/test/copy4";
        Type t = typeHelper.getType(testURI, "C");
        List<DataObject> e1 = b1.getList("e");
        assertEquals(3, e1.size());
        List<DataObject> e2 = b2.getList("e");
        assertEquals(3, e2.size());
        assertEquals("C456", e1.get(0).getDataObject("c").get("c0"));
        assertEquals("C123", e1.get(1).getDataObject("c").get("c0"));
        assertEquals("C456", e1.get(2).getDataObject("c").get("c0"));
        // since reference is outside copy tree, it is not copied
        assertFalse(e2.get(0).isSet("c"));
        assertNull(e2.get(0).get("c"));
        assertFalse(e2.get(1).isSet("c"));
        assertNull(e2.get(1).get("c"));
        assertFalse(e2.get(2).isSet("c"));
        assertNull(e2.get(2).get("c"));
        // other values are copied
        assertEquals(100, e1.get(0).get("e1"));
        assertEquals("xxx", e1.get(0).get("e2"));
        assertEquals(200, e1.get(1).get("e1"));
        assertEquals("yyy", e1.get(1).get("e2"));
        assertEquals(300, e1.get(2).get("e1"));
        assertEquals("zzz", e1.get(2).get("e2"));
        assertEquals(100, e1.get(0).get("e1"));
        assertEquals("xxx", e2.get(0).get("e2"));
        assertEquals(200, e2.get(1).get("e1"));
        assertEquals("yyy", e2.get(1).get("e2"));
        assertEquals(300, e2.get(2).get("e1"));
        assertEquals("zzz", e2.get(2).get("e2"));
    }

    public void testByteArray() throws Exception
    {
        File schema = getResourceFile("diffgram/default", "ld.xsd");
        xsdHelper.define(new FileInputStream(schema), schema.toURL().toString());
        DataObject test1 = getRootDataObject("diffgram/default", "ld.xml").getDataObject("ElementTypes[1]");
        DataObject test2 = getRootDataObject("diffgram/default", "ld.xml").getDataObject("ElementTypes[1]");
        assertFalse(test1 == test2);
        byte[] ba1 = test1.getBytes("hexBinary");
        byte[] ba2 = test2.getBytes("hexBinary");
        System.out.println(ba1);
        System.out.println(ba2);
        assertFalse(ba1 == ba2);
        assertFalse(ba1.equals(ba2));
        // equal and equalShallow test equality of the bytes by value
        assertTrue(equalityHelper.equalShallow(test1, test2));
        assertTrue(equalityHelper.equal(test1, test2));
        // copy and copyShallow copy the bytes by value, not reference
        DataObject test3 = copyHelper.copy(test1);
        byte[] ba3 = test3.getBytes("hexBinary");
        System.out.println(ba3);
        assertFalse(ba1 == ba3);
        assertFalse(ba1.equals(ba3));
        assertTrue(equalityHelper.equalShallow(test1, test3));
        assertTrue(equalityHelper.equal(test1, test3));
        DataObject test4 = copyHelper.copyShallow(test1);
        byte[] ba4 = test4.getBytes("hexBinary");
        System.out.println(ba4);
        assertFalse(ba1 == ba4);
        assertFalse(ba1.equals(ba4));
        assertTrue(equalityHelper.equalShallow(test1, test4));
        assertTrue(equalityHelper.equal(test1, test4));
    }

    /* sequenced data object (mixed content) */
    public void testSequencedDataObject()
    {

    }

    /* data object with changeSummary property */
    public void testChangeSummary()
    {
        // for shallow copy,
        // empty changeSummary in copy, logging state the same as in source

        // for deep copy, changeSummary is copied, 
        // new changeSummary refers to objects in new tree,
        // logging state the same as in source
    }

}
