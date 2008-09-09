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

import java.io.*;
import java.util.*;

import javax.sdo.*;
import javax.sdo.helper.*;

import davos.sdo.TypeXML;
import davos.sdo.PropertyXML;
import davos.sdo.Options;

import junit.framework.*;
import common.BaseTest;

/**
 * @author Wing Yew Poon
 */
public class XMLWithoutSchemaTest extends BaseTest
{
    public XMLWithoutSchemaTest(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        /*
        TestSuite suite = new TestSuite();
        //suite.addTest(new XMLWithoutSchemaTest("testXMLWithNamespace"));
        //suite.addTest(new XMLWithoutSchemaTest("testXMLWithoutNamespace"));
        suite.addTest(new XMLWithoutSchemaTest("testNamespace"));
        */
        // or
        TestSuite suite = new TestSuite(XMLWithoutSchemaTest.class);
        return suite;
    }

    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(suite());
    }

    public static File dir;
    static
    {
        dir = new File(OUTPUTROOT + S + "type");
        dir.mkdirs();
    }

    private static XMLHelper xmlHelper = context.getXMLHelper();
    private static TypeHelper typeHelper = context.getTypeHelper();
    private static EqualityHelper equalityHelper = context.getEqualityHelper();
    private static XSDHelper xsdHelper = context.getXSDHelper();
    private static final Type T_BOOLEAN = typeHelper.getType("commonj.sdo", "Boolean");
    private static final Type T_INT = typeHelper.getType("commonj.sdo", "Int");
    private static final Type T_STRING = typeHelper.getType("commonj.sdo", "String");
    private static final Type T_OBJECT = typeHelper.getType("commonj.sdo", "Object");
    private static final Type T_DATAOBJECT = typeHelper.getType("commonj.sdo", "DataObject");
    private static final Type T_BEADATAOBJECT = typeHelper.getType("http://www.bea.com/sdo/types", "DataObject");
    private static final Type T_WRAPPER = typeHelper.getType("http://www.bea.com/sdo/types", "WrapperType");

    private Property getPropertyByName(List properties, String name)
    {
        Property p = null;
        for (Iterator i = properties.iterator(); i.hasNext(); )
        {
            Property prop = (Property)i.next();
            if (prop.getName().equals(name))
            {
                p = prop;
            }
        }
        return p;
    }

    public void testXMLWithNamespace()
    {
        String xml =
            "<product xmlns=\"http://example.com/prod\"" + //newline +
            "         xmlns:app=\"http://example.com/app\">" + //newline +
            "<number>557</number>" + //newline + 
            "<size system=\"US-DRESS\" app:code=\"R32\">10</size>" + //newline +
            "</product>";

        XMLDocument doc = xmlHelper.load(xml);
        DataObject root = doc.getRootObject();
        Type t = root.getType();
        System.out.println(t.getName() + "@" + t.getURI());
        System.out.println(doc.getRootElementName() +"@" + doc.getRootElementURI());
        util.DataObjectPrinter.printDataObject2(root);
        assertTrue(T_BEADATAOBJECT.isInstance(root));
        assertTrue(T_DATAOBJECT.isInstance(root));
        assertTrue(root instanceof DataObject);
        List properties = root.getInstanceProperties();
        Property number = getPropertyByName(properties, "number");
        assertNotNull(number);
        System.out.println("number is many? " + number.isMany());
        assertTrue(number.getType().isDataType());
        assertEquals(T_OBJECT, number.getType());
        Type ct = number.getContainingType();
        //assertEquals("DocumentRoot", ct.getName());
        assertEquals("http://example.com/prod", ct.getURI());
        Property size = getPropertyByName(properties, "size");
        assertNotNull(size);
        ct = size.getContainingType();
        //assertEquals("DocumentRoot", ct.getName());
        assertEquals("http://example.com/prod", ct.getURI());
        Object sizeObj = root.get(size);
        assertTrue(size.isMany());
        assertTrue(sizeObj instanceof List);
        
        List subproperties = ((DataObject)((List)sizeObj).get(0)).getInstanceProperties();
        Property system = getPropertyByName(subproperties, "system");
        assertNotNull(system);
        ct = system.getContainingType();
        //assertEquals("DocumentRoot", ct.getName());
        assertNull(ct.getURI());
        Property code = getPropertyByName(subproperties, "code");
        assertNotNull(code);
        ct = code.getContainingType();
        //assertEquals("DocumentRoot", ct.getName());
        assertEquals("http://example.com/app", ct.getURI());
    }

    public void testXMLWithoutNamespace() throws Exception
    {
        assertNotNull(T_WRAPPER);
        System.out.println(T_WRAPPER);
        System.out.println("open? " + T_WRAPPER.isOpen());
        System.out.println("sequenced? " + T_WRAPPER.isSequenced());
        System.out.println("mixed content? " + ((TypeXML)T_WRAPPER).isMixedContent());
        assertNotNull(T_BEADATAOBJECT);
        System.out.println(T_BEADATAOBJECT);
        System.out.println("open? " + T_BEADATAOBJECT.isOpen());
        System.out.println("sequenced? " + T_BEADATAOBJECT.isSequenced());
        System.out.println("mixed content? " + ((TypeXML)T_BEADATAOBJECT).isMixedContent());
        
        File f = getResourceFile("data", "mixed_no_schema.xml");
        InputStream in = new FileInputStream(f);
        XMLDocument doc = xmlHelper.load(in);
        in.close();
        DataObject root = doc.getRootObject();
        Type t = root.getType();
        assertTrue(t.isOpen());
        assertTrue(t.isSequenced());
        assertTrue(((TypeXML)t).isMixedContent());

        Property p1 = root.getInstanceProperty("st1");
        Property p2 = root.getInstanceProperty("st2");
        Property p3 = root.getInstanceProperty("st3");
        Property p4 = root.getInstanceProperty("st4");
        Property p5 = root.getInstanceProperty("ct1");
        Property p6 = root.getInstanceProperty("ct2");

        assertEquals(T_OBJECT, p1.getType());
        assertEquals(T_DATAOBJECT, p2.getType());
        assertEquals(T_DATAOBJECT, p3.getType());
        assertEquals(T_OBJECT, p4.getType());
        assertEquals(T_DATAOBJECT, p5.getType());
        assertEquals(T_DATAOBJECT, p6.getType());

        Type ct = p1.getContainingType();
        //assertEquals("DocumentRoot", ct.getName());
        assertNull(ct.getURI());
        ct = p2.getContainingType();
        assertNull(ct.getURI());
        ct = p3.getContainingType();
        assertNull(ct.getURI());
        ct = p4.getContainingType();
        assertNull(ct.getURI());
        ct = p5.getContainingType();
        assertNull(ct.getURI());
        ct = p6.getContainingType();
        assertNull(ct.getURI());

        Object st1 = root.get("st1[1]");
        assertTrue(st1 instanceof String);
        assertEquals("simple content", st1);
        Object st4 = root.get("st4[1]");
        assertTrue(st4 instanceof Integer);
        assertEquals(new Integer(200), st4);
        
        Object st2 = root.get("st2[1]");
        assertTrue(st2 instanceof DataObject);
        assertEquals(T_WRAPPER, ((DataObject)st2).getType());
        Object st3_1 = root.get("st3[1]");
        assertTrue(st3_1 instanceof DataObject);
        assertEquals(T_WRAPPER, ((DataObject)st3_1).getType());
        Object st3_2 = root.get("st3[2]");
        assertTrue(st3_2 instanceof DataObject);
        assertEquals(T_WRAPPER, ((DataObject)st3_2).getType());

        Object ct1 = root.get("ct1[1]");
        assertTrue(ct1 instanceof DataObject);
        assertEquals(T_BEADATAOBJECT, ((DataObject)ct1).getType());
        Object ct2 = root.get("ct2[1]");
        assertTrue(ct2 instanceof DataObject);
        assertEquals(T_BEADATAOBJECT, ((DataObject)ct2).getType());
    }

    private void checkAttribute(Property p)
    {
        // attribute is a single-valued, open content property
        // of type SDO String
        assertFalse(p.isMany());
        //assertTrue(p.isOpenContent());
        assertEquals(T_STRING, p.getType());
    }

    // attribute is known
    public void testAttribute0()
    {
        System.out.println("testAttribute0()");
        String xml_in =
            "<root xmlns:glob=\"http://sdo/test/global\" glob:xxx=\"true\"/>";
        XMLDocument doc = xmlHelper.load(xml_in);
        DataObject o = doc.getRootObject();
        List props = o.getInstanceProperties();
        assertEquals(1, props.size());
        Property p = (Property)props.get(0);
        assertEquals("xxx", p.getName());
        assertFalse(((PropertyXML)p).isXMLElement());
        assertEquals("http://sdo/test/global", ((PropertyXML)p).getXMLNamespaceURI());
        assertEquals(p, typeHelper.getOpenContentProperty("http://sdo/test/global", "xxx"));
        assertEquals(p, xsdHelper.getGlobalProperty("http://sdo/test/global", "xxx", false));
        assertEquals(T_BOOLEAN, p.getType());
        Object value = o.get(p);
        assertTrue(value instanceof Boolean);
        assertEquals(true, value);
    }

    public void testAttribute1()
    {
        System.out.println("testAttribute1()");
        String xml = "<ns:a xmlns:ns=\"xxx\" b=\"1\"><ns:b b=\"true\"/></ns:a>";
        XMLDocument doc = xmlHelper.load(xml);
        DataObject root = doc.getRootObject();
        List props = root.getInstanceProperties();
        for (Object prop : props)
        {
            Property p = (Property)prop;
            System.out.println(p.getName());
            System.out.println(p.getContainingType().getURI());
            System.out.println(p.getContainingType().getName());
        }
        assertEquals(2, props.size());
        Type aType = root.getType();
        assertTrue(aType.isSequenced());
        assertTrue(aType.isOpen());
        Sequence aseq = root.getSequence();
        assertEquals(1, aseq.size());
        Property b1 = root.getInstanceProperty("b");
        assertFalse(((PropertyXML)b1).isXMLElement());
        assertEquals("", ((PropertyXML)b1).getXMLNamespaceURI());
        checkAttribute(b1);
        Object b1val = root.get(b1);
        assertTrue(b1val instanceof String);
        assertEquals("1", b1val);
        Property b2 = aseq.getProperty(0);
        assertEquals("b", b2.getName());
        assertEquals("xxx", ((PropertyXML)b2).getXMLNamespaceURI());
        assertEquals("xxx", b2.getContainingType().getURI());
        assertEquals(T_DATAOBJECT, b2.getType());
        Object b2val = aseq.getValue(0);
        assertTrue(b2val instanceof DataObject);
        assertEquals(T_BEADATAOBJECT, ((DataObject)b2val).getType());
        Sequence bseq = ((DataObject)b2val).getSequence();
        assertNotNull(bseq);
        assertEquals(0, bseq.size());
        Property b3 = ((DataObject)b2val).getInstanceProperty("b");
        assertFalse(b3 == b1);
        assertFalse(((PropertyXML)b3).isXMLElement());
        assertEquals("", ((PropertyXML)b3).getXMLNamespaceURI());
        checkAttribute(b1);
        Object b3val = ((DataObject)b2val).get(b3);
        assertTrue(b3val instanceof String);
        assertEquals("true", b3val);
    }

    public void testAttribute2()
    {
        System.out.println("testAttribute2()");
        String xml = "<a b=\"1\"><b b=\"true\"/></a>";
        XMLDocument doc = xmlHelper.load(xml);
        DataObject root = doc.getRootObject();
        List props = root.getInstanceProperties();
        for (Object prop : props)
        {
            Property p = (Property)prop;
            System.out.println(p.getName());
            System.out.println(p.getContainingType().getURI());
            System.out.println(p.getContainingType().getName());
        }
        Type aType = root.getType();
        assertTrue(aType.isSequenced());
        assertTrue(aType.isOpen());
        Sequence aseq = root.getSequence();
        assertEquals(1, aseq.size());
        Property b1 = root.getInstanceProperty("b");
        assertFalse(((PropertyXML)b1).isXMLElement());
        assertEquals("", ((PropertyXML)b1).getXMLNamespaceURI());
        checkAttribute(b1);
        Object b1val = root.get(b1);
        assertTrue(b1val instanceof String);
        assertEquals("1", b1val);
        Property b2 = aseq.getProperty(0);
        assertEquals("b", b2.getName());
        assertTrue(((PropertyXML)b2).isXMLElement());
        assertEquals("", ((PropertyXML)b2).getXMLNamespaceURI());
        assertNull(b2.getContainingType().getURI());
        assertEquals(T_DATAOBJECT, b2.getType());
        Object b2val = aseq.getValue(0);
        assertTrue(b2val instanceof DataObject);
        assertEquals(T_BEADATAOBJECT, ((DataObject)b2val).getType());
        Sequence bseq = ((DataObject)b2val).getSequence();
        assertNotNull(bseq);
        assertEquals(0, bseq.size());
        Property b3 = ((DataObject)b2val).getInstanceProperty("b");
        assertFalse(b3 == b1);
        assertFalse(((PropertyXML)b3).isXMLElement());
        assertEquals("", ((PropertyXML)b3).getXMLNamespaceURI());
        checkAttribute(b1);
        Object b3val = ((DataObject)b2val).get(b3);
        assertTrue(b3val instanceof String);
        assertEquals("true", b3val);        
    }

    private Property findProperty(List props, String xmlNamespaceURI, String xmlName, boolean isElement)
    {
        Property tbr = null;
        for (Object prop : props)
        {
            PropertyXML p = (PropertyXML)prop;
            if (p.getXMLNamespaceURI().equals(xmlNamespaceURI) &&
                p.getXMLName().equals(xmlName) &&
                (p.isXMLElement() == isElement))
                tbr = p;
        }
        return tbr;
    }

    public void testNamespace()
    {
        System.out.println("testNamespace()");
        String xml = 
            "<ns1:a xmlns:ns1=\"xxx\" xmlns:ns2=\"yyy\" b=\"1\" ns2:b=\"2\">" +
            "<ns1:b b=\"true\"/><ns2:b b=\"false\" ns2:b=\"3\"/></ns1:a>";
        XMLDocument doc = xmlHelper.load(xml);
        DataObject root = doc.getRootObject();
        Sequence aseq = root.getSequence();
        assertEquals(2, aseq.size());
        List props = root.getInstanceProperties();
        Property b1 = findProperty(props, "", "b", false); // attribute b
        Property b2 = findProperty(props, "yyy", "b", false); // attribute ns2:b
        Property b3 = findProperty(props, "xxx", "b", true); // element ns1:b
        Property b4 = findProperty(props, "yyy", "b", true); // element ns2:b
        Property b3s = aseq.getProperty(0); // element ns2:b
        Property b4s = aseq.getProperty(1); // element ns2:b
        assertEquals("b", b1.getName());
        assertEquals("b", b2.getName());
        assertEquals("b", b3.getName());
        assertEquals("b", b4.getName());
        assertTrue(b3 == b3s);
        assertTrue(b4 == b4s);
        Object b1val = root.get(b1); // attribute b
        Object b2val = root.get(b2); // attribute ns2:b
        Object b3val = aseq.getValue(0); // element ns1:b
        Object b4val = aseq.getValue(1); // element ns2:b
        assertTrue(b1val instanceof String);
        assertTrue(b2val instanceof String);
        assertTrue(b3val instanceof DataObject);
        assertTrue(b4val instanceof DataObject);
        assertEquals("1", b1val);
        assertEquals("2", b2val);
        assertTrue(b3val == root.getList(b3).get(0));
        assertTrue(b4val == root.getList(b4).get(0));
        // ns1:b element
        Sequence b3seq = ((DataObject)b3val).getSequence();
        assertEquals(0, b3seq.size());
        Property b31 = findProperty(((DataObject)b3val).getInstanceProperties(),
                                    "", "b", false);
        assertEquals("b", b31.getName());
        assertEquals("true", ((DataObject)b3val).get(b31));
        // ns2:b element
        Sequence b4seq = ((DataObject)b4val).getSequence();
        assertEquals(0, b4seq.size());
        Property b41 = findProperty(((DataObject)b4val).getInstanceProperties(),
                                    "", "b", false);
        assertEquals("b", b41.getName());
        assertEquals("false", ((DataObject)b4val).get(b41));
        Property b42 = findProperty(((DataObject)b4val).getInstanceProperties(),
                                    "yyy", "b", false);
        assertEquals("b", b42.getName());
        assertEquals("3", ((DataObject)b4val).get(b42));
        // attributes with same namespace in different containers are different
        assertFalse(b31 == b41);
        assertFalse(b1 == b31);
        assertFalse(b1 == b41);
        assertFalse(b2 == b42);
    }

    // element is known
    public void testElement0()
    {
        System.out.println("testElement0()");
        String xml_in =
            "<root>" +
            "<glob:size xmlns:glob=\"http://sdo/test/global\">12</glob:size>" +
            "</root>";
        XMLDocument doc = xmlHelper.load(xml_in);
        DataObject o = doc.getRootObject();
        List props = o.getInstanceProperties();
        assertEquals(1, props.size());
        Property p = (Property)props.get(0);
        assertEquals("size", p.getName());
        assertTrue(((PropertyXML)p).isXMLElement());
        assertEquals("http://sdo/test/global", ((PropertyXML)p).getXMLNamespaceURI());
        assertEquals(p, typeHelper.getOpenContentProperty("http://sdo/test/global", "size"));
        assertEquals(p, xsdHelper.getGlobalProperty("http://sdo/test/global", "size", true));
        assertTrue(p.isMany());
        Type t = typeHelper.getType("http://sdo/test/global", "SizeType");
        assertEquals(t, p.getType());
        Object value = o.get(p);
        assertTrue(value instanceof List);
        assertEquals(1, ((List)value).size());
        assertTrue(((List)value).get(0) instanceof String);
        assertEquals("12", ((List)value).get(0));
        Object value2 = o.get("size");
        assertTrue(value2 instanceof List);
        assertEquals(1, ((List)value2).size());
        assertTrue(((List)value2).get(0) instanceof String);
        assertEquals("12", ((List)value2).get(0));
        Object value3 = o.getString(p);
        assertEquals("12", value3);
        Object value4 = o.getString("size");
        assertEquals("12", value4);
        int value3b = o.getInt(p);
        assertEquals(12, value3b);
        int value4b = o.getInt("size");
        assertEquals(12, value4b);
        Object value5 = o.get("size[1]");
        assertTrue(value5 instanceof String);
        assertEquals("12", value5);
        Object value6 = o.getString("size[1]");
        assertEquals("12", value6);
        Object value6b = o.getInt("size[1]");
        assertEquals(12, value6b);
    }

    // element all occurences of which have simple type
    // and any xsi:type specified is "invertible" from java instance class
    // - maps to property of type Object
    public void testElement1()
    {
        String xml_in =
            "<root>" +
            "<a>100</a>" +
            "<a xsi:type=\"xs:int\" "+
            "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
            "xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">100</a>" +
            "<a xsi:type=\"xs:double\" "+
            "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
            "xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">100</a>" +
            "</root>";
        System.out.println(xml_in);
        XMLDocument doc = xmlHelper.load(xml_in);
        DataObject o = doc.getRootObject();
        List props = o.getInstanceProperties();
        assertEquals(1, props.size());
        Property p = (Property)props.get(0);
        assertEquals("a", p.getName());
        assertTrue(((PropertyXML)p).isXMLElement());
        assertEquals("", ((PropertyXML)p).getXMLNamespaceURI());
        assertNull(typeHelper.getOpenContentProperty("", "a"));
        assertNull(xsdHelper.getGlobalProperty("", "a", true));
        assertTrue(p.isMany());
        assertEquals(T_OBJECT, p.getType());
        Object a1 = o.get("a[1]");
        assertTrue(a1 instanceof String);
        assertEquals("100", a1);
        Object a2 = o.get("a[2]");
        assertTrue(a2 instanceof Integer);
        assertEquals(100, a2);
        Object a3 = o.get("a[3]");
        assertTrue(a3 instanceof Double);
        assertEquals(100.0, a3);
    }

    // element some occurence of which has 
    // xsi:type of "non-invertible" simple type
    // - maps to property of type DataObject
    public void testElement2()
    {
        String xml_in =
            "<root>" +
            "<a xsi:type=\"xs:int\" "+
            "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
            "xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">100</a>" +
            "<a xsi:type=\"xs:string\" "+
            "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
            "xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">100</a>" +
            "</root>";
        System.out.println(xml_in);
        XMLDocument doc = xmlHelper.load(xml_in);
        DataObject o = doc.getRootObject();
        List props = o.getInstanceProperties();
        assertEquals(1, props.size());
        Property p = (Property)props.get(0);
        assertEquals("a", p.getName());
        assertTrue(((PropertyXML)p).isXMLElement());
        assertEquals("", ((PropertyXML)p).getXMLNamespaceURI());
        assertNull(typeHelper.getOpenContentProperty("", "a"));
        assertNull(xsdHelper.getGlobalProperty("", "a", true));
        assertTrue(p.isMany());
        assertEquals(T_DATAOBJECT, p.getType());
        // values are wrapper data objects, wrapping "value" property
        // of type corresponding to xsi:type
        Object a1 = o.get("a[1]");
        assertTrue(a1 instanceof DataObject);
        assertEquals(T_WRAPPER, ((DataObject)a1).getType());
        assertEquals(T_INT, ((DataObject)a1).getInstanceProperty("value").getType());
        assertEquals(100, ((DataObject)a1).get("value"));
        Object a2 = o.get("a[2]");
        assertTrue(a2 instanceof DataObject);
        assertEquals(T_WRAPPER, ((DataObject)a2).getType());
        assertEquals(T_STRING, ((DataObject)a2).getInstanceProperty("value").getType());
        assertEquals("100", ((DataObject)a2).get("value"));
    }

    // element some occurence of which has complex type
    // - maps to property of type DataObject
    public void testElement3()
    {
        String xml_in =
            "<root>" +
            "<a>100</a>" +
            "<a><b>x</b>y<c>z</c>/</a>" +
            "<a xsi:type=\"glob:SimpleType\" "+
            "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
            "xmlns:glob=\"http://sdo/test/global\">" +
            "<glob:x>Extra! Extra!</glob:x>" +
            "<glob:y>1000000</glob:y>" +
            "</a>" +
            "</root>";
        System.out.println(xml_in);
        XMLDocument doc = xmlHelper.load(xml_in);
        DataObject o = doc.getRootObject();
        List props = o.getInstanceProperties();
        assertEquals(1, props.size());
        Property p = (Property)props.get(0);
        assertEquals("a", p.getName());
        assertTrue(((PropertyXML)p).isXMLElement());
        assertEquals("", ((PropertyXML)p).getXMLNamespaceURI());
        assertNull(typeHelper.getOpenContentProperty("", "a"));
        assertNull(xsdHelper.getGlobalProperty("", "a", true));
        assertTrue(p.isMany());
        assertEquals(T_DATAOBJECT, p.getType());
        // simple type, no xsi:type -> wrapped string
        Object a1 = o.get("a[1]");
        assertTrue(a1 instanceof DataObject);
        assertEquals(T_WRAPPER, ((DataObject)a1).getType());
        assertEquals(T_OBJECT, ((DataObject)a1).getInstanceProperty("value").getType());
        assertEquals("100", ((DataObject)a1).get("value"));
        // complex type with no xsi:type -> open, sequenced data object
        Object a2 = o.get("a[2]");
        assertTrue(a2 instanceof DataObject);
        assertEquals(T_BEADATAOBJECT, ((DataObject)a2).getType());
        // xsi:type of complex type -> data object of specified type
        Object a3 = o.get("a[3]");
        assertTrue(a3 instanceof DataObject);
        Type t = typeHelper.getType("http://sdo/test/global", "SimpleType");
        assertEquals(t, ((DataObject)a3).getType());
    }

    public void testRoundTrip() throws Exception
    {
        File f_in = getResourceFile("data", "mixed_no_schema.xml");
        InputStream in = new FileInputStream(f_in);
        XMLDocument doc = xmlHelper.load(in);
        in.close();
        DataObject root = doc.getRootObject();
        File f_out = new File(dir, "mixed_no_schema.xml");
        OutputStream out = new FileOutputStream(f_out);
        XMLDocument doc2 = xmlHelper.createDocument(root, "", "root");
        doc2.setXMLDeclaration(false);
        xmlHelper.save(doc2, out, new Options().setSavePrettyPrint().setSaveIndent(2));
        out.close();
        compareXMLFiles(f_in, f_out);
        in = new FileInputStream(f_out);
        XMLDocument doc3 = xmlHelper.load(in);
        in.close();
        DataObject root2 = doc3.getRootObject();
        assertTrue(equalityHelper.equal(root, root2));
    }
}
