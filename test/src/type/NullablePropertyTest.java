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

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.List;
import java.util.ArrayList;

import javax.sdo.DataObject;
import javax.sdo.Type;
import javax.sdo.Property;
import javax.sdo.helper.XMLDocument;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author Wing Yew Poon
 */
public class NullablePropertyTest extends MetaDataTest
{
    public NullablePropertyTest(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        
        TestSuite suite = new TestSuite();
        
        suite.addTest(new NullablePropertyTest("testNullable"));
        suite.addTest(new NullablePropertyTest("testSetNullable"));
        suite.addTest(new NullablePropertyTest("testSetNullable2"));
        suite.addTest(new NullablePropertyTest("testSetNullable3"));
        
        suite.addTest(new NullablePropertyTest("testUnmarshal1"));
        suite.addTest(new NullablePropertyTest("testUnmarshal2"));
        suite.addTest(new NullablePropertyTest("testUnmarshal3a"));
        suite.addTest(new NullablePropertyTest("testUnmarshal3b"));
        suite.addTest(new NullablePropertyTest("testUnmarshal3c"));
        suite.addTest(new NullablePropertyTest("testUnmarshal4a"));
        suite.addTest(new NullablePropertyTest("testUnmarshal4b"));
        suite.addTest(new NullablePropertyTest("testUnmarshal5a"));
        suite.addTest(new NullablePropertyTest("testUnmarshal5b"));
        suite.addTest(new NullablePropertyTest("testUnmarshal5c"));
        suite.addTest(new NullablePropertyTest("testUnmarshal6a"));
        suite.addTest(new NullablePropertyTest("testUnmarshal6b"));
        
        suite.addTest(new NullablePropertyTest("testDynamic"));
        suite.addTest(new NullablePropertyTest("testDefaultValue0"));
        suite.addTest(new NullablePropertyTest("testDefaultValue1"));
        suite.addTest(new NullablePropertyTest("testDefaultValue2"));
        
        // or
        //TestSuite suite = new TestSuite(PropertyTest.class);
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

    private static final String NS = "http://sdo/test/nillable";

    private static final String TEST1 =
        "<nil:test xmlns:nil=\"" + NS + "\">" +
        "<s1 xsi:nil=\"true\" xmlns:xsi=\"" + XSI_URI + "\"/>" +
        "</nil:test>";

    private static final String TEST2 =
        "<nil:test xmlns:nil=\"" + NS + "\">" +
        "<s2 xsi:nil=\"true\" xmlns:xsi=\"" + XSI_URI + "\"/>" +
        "</nil:test>";

    private static final String TEST3A =
        "<nil:test xmlns:nil=\"" + NS + "\">" +
        "<c1 system=\"US\">12</c1>" +
        "</nil:test>";

    private static final String TEST3B =
        "<nil:test xmlns:nil=\"" + NS + "\">" +
        "<c1 system=\"US\" xsi:nil=\"true\" xmlns:xsi=\"" + XSI_URI + "\"/>" +
        "</nil:test>";

    private static final String TEST3C =
        "<nil:test xmlns:nil=\"" + NS + "\">" +
        "<c1 xsi:nil=\"true\" xmlns:xsi=\"" + XSI_URI + "\"/>" +
        "</nil:test>";

    private static final String TEST4A =
        "<nil:test xmlns:nil=\"" + NS + "\">" +
        "<c2><a>AAA</a><b>BBB</b><c>CCC</c></c2>" +
        "</nil:test>";

    private static final String TEST4B =
        "<nil:test xmlns:nil=\"" + NS + "\">" +
        "<c2 xsi:nil=\"true\" xmlns:xsi=\"" + XSI_URI + "\"/>" +
        "</nil:test>";

    private static final String TEST5A =
        "<nil:test xmlns:nil=\"" + NS + "\">" +
        "<c3><a>AAA</a><b>BBB</b><c>CCC</c></c3>" +
        "</nil:test>";

    private static final String TEST5B =
        "<nil:test xmlns:nil=\"" + NS + "\">" +
        "<c3 xsi:nil=\"true\" xmlns:xsi=\"" + XSI_URI + "\" d=\"true\"/>" +
        "</nil:test>";

    private static final String TEST5C =
        "<nil:test xmlns:nil=\"" + NS + "\">" +
        "<c3 xsi:nil=\"true\" xmlns:xsi=\"" + XSI_URI + "\"/>" +
        "</nil:test>";

    private static final String TEST5D =
        "<nil:test xmlns:nil=\"" + NS + "\">" +
        "<c3 d=\"true\"/>" +
        "</nil:test>";

    // attribute with default value absent
    private static final String TEST6A =
        "<nil:test xmlns:nil=\"" + NS + "\">" +
        "<c4><a>AAA</a><b>BBB</b><c>CCC</c></c4>" +
        "</nil:test>";

    // corner case, nil element with default attribute
    private static final String TEST6B =
        "<nil:test xmlns:nil=\"" + NS + "\">" +
        "<c4 xsi:nil=\"true\" xmlns:xsi=\"" + XSI_URI + "\"/>" +
        "</nil:test>";

    /* test mapping of nillable elements in schema-defined types to nullable 
       properties */
    public void testNullable()
    {
        Type t = typeHelper.getType(NS, "TestType");
        assertNotNull(t);
        List<Property> props = t.getProperties();
        assertEquals(7, props.size());
        Property s0 = props.get(0);
        assertEquals("s0", s0.getName());
        assertFalse(s0.isNullable());
        Property s1 = props.get(1);
        assertEquals("s1", s1.getName());
        assertTrue(s1.isNullable());
        Property s2 = props.get(2);
        assertEquals("s2", s2.getName());
        assertTrue(s2.isNullable());
        Property c1 = props.get(3);
        assertEquals("c1", c1.getName());
        assertTrue(c1.isNullable());
        Property c2 = props.get(4);
        assertEquals("c2", c2.getName());
        assertTrue(c2.isNullable());
        Property c3 = props.get(5);
        assertEquals("c3", c3.getName());
        assertTrue(c3.isNullable());
        Property c4 = props.get(6);
        assertEquals("c4", c4.getName());
        assertTrue(c4.isNullable());
    }

    /* test setting a non-nullable property to null and setting a nullable
       property to null */
    public void testSetNullable()
    {
        DataObject test1 = factory.create(NS, "TestType");
        // set non-nullable property to null
        try
        {
            test1.set("s0", null);
            fail();
        }
        catch (Exception e)
        {
            assertTrue(e.getMessage().indexOf("does not allow null value") >= 0);
        }

        // set nullable property to null
        DataObject test2 = factory.create(NS, "TestType");
        test2.set("s1", null);
        assertNull(test2.get("s1"));
        assertTrue(test2.isSet("s1"));
        // test marshalling
        String out = xmlHelper.save(test2, NS, "test");
        System.out.println(out);
        assertEquals(TEST1, out);
    }

    /* test setting nullable property to null - advanced cases:
       1. complex type with simple content (with attribute)
       2. complex type with complex content (without attribute)
       3. complex type with complex content (with attribute)
     */
    public void testSetNullable2()
    {
        DataObject test1 = factory.create(NS, "TestType");
        // set nullable property to null
        test1.set("c1", null);
        assertNull(test1.get("c1"));
        assertTrue(test1.isSet("c1"));
        // test marshalling
        String out = xmlHelper.save(test1, NS, "test");
        System.out.println(out);
        assertEquals(TEST3C, out);

        DataObject test2 = factory.create(NS, "TestType");
        // set nullable property to null
        test2.set("c2", null);
        assertNull(test2.get("c2"));
        assertTrue(test2.isSet("c2"));
        // test marshalling
        out = xmlHelper.save(test2, NS, "test");
        System.out.println(out);
        assertEquals(TEST4B, out);

        DataObject test3 = factory.create(NS, "TestType");
        // set nullable property to null
        test3.set("c3", null);
        assertNull(test3.get("c3"));
        assertTrue(test3.isSet("c3"));
        // test marshalling
        out = xmlHelper.save(test3, NS, "test");
        System.out.println(out);
        assertEquals(TEST5C, out);
    }    

    /* further tests of marshalling in advanced cases:
       - can we generate <e xsi:nil="true" a="value"/> for the following?
       1. complex type with simple content (with attribute)
       2. complex type with complex content (with attribute)
     */
    public void testSetNullable3()
    {
        DataObject test1 = factory.create(NS, "TestType");
        DataObject c1= test1.createDataObject("c1");
        c1.set("value", null);
        c1.set("system", "US");
        String out = xmlHelper.save(test1, NS, "test");
        System.out.println(out);
        System.out.println(TEST3B);
        assertEquals(TEST3B, out);

        DataObject test3 = factory.create(NS, "TestType");
        DataObject c3 = test3.createDataObject("c3");
        c3.setBoolean("d", true);
        out = xmlHelper.save(test3, NS, "test");
        System.out.println(out);
        System.out.println(TEST5D);
        assertEquals(TEST5D, out);
    }

    /* test unmarshalling nillable element of simple type */
    public void testUnmarshal1()
    {
        System.out.println("testUnmarshal1()");
        System.out.println(TEST1);
        XMLDocument doc = xmlHelper.load(TEST1);
        assertEquals(NS, doc.getRootElementURI());
        assertEquals("test", doc.getRootElementName());
        DataObject test = doc.getRootObject();
        assertNotNull(test);
        assertTrue(test.isSet("s1"));
        assertNull(test.get("s1"));
        // note: test.getInt("s1") returns 0
        assertFalse(test.isSet("s0"));
        assertFalse(test.isSet("s2"));
        assertEquals("N.A.", test.get("s2"));
        assertFalse(test.isSet("c1"));
        assertFalse(test.isSet("c2"));
    }

    /* test unmarshalling nillable element of simple type with default value */
    public void testUnmarshal2()
    {
        System.out.println("testUnmarshal2()");
        System.out.println(TEST2);
        XMLDocument doc = xmlHelper.load(TEST2);
        DataObject test = doc.getRootObject();
        assertTrue(test.isSet("s2"));
        assertNull(test.get("s2"));
        assertFalse(test.isSet("s0"));
        assertFalse(test.isSet("s1"));
        assertFalse(test.isSet("c1"));
        assertFalse(test.isSet("c2"));
    }

    /* test unmarshalling non-nil nillable element of complex type with simple 
       content (attribute present) */
    public void testUnmarshal3a()
    {
        System.out.println("testUnmarshal3a()");
        System.out.println(TEST3A);
        XMLDocument doc = xmlHelper.load(TEST3A);
        DataObject test = doc.getRootObject();
        assertTrue(test.isSet("c1"));
        DataObject c1 = test.getDataObject("c1");
        assertNotNull(c1);
        Type ctsc = typeHelper.getType(NS, "CTSC");
        assertNotNull(ctsc);
        assertEquals(ctsc, c1.getType());
        List<Property> props = ctsc.getProperties();
        assertEquals(2, props.size());
        Property value = ctsc.getProperty("value");
        assertNotNull(value);
        assertTrue(value.isNullable());
        Property system = ctsc.getProperty("system");
        assertNotNull(system);
        assertFalse(system.isNullable());
        assertEquals(12, c1.get(value));
        assertEquals("US", c1.get(system));
    }

    /* test unmarshalling nil nillable element of complex type with simple 
       content (attribute present) */
    public void testUnmarshal3b()
    {
        System.out.println("testUnmarshal3b()");
        System.out.println(TEST3B);
        XMLDocument doc = xmlHelper.load(TEST3B);
        DataObject test = doc.getRootObject();
        assertTrue(test.isSet("c1"));
        DataObject c1 = test.getDataObject("c1");
        assertNotNull(c1);
        assertNull(c1.get("value"));
        assertEquals("US", c1.get("system"));
    }

    /* test unmarshalling nil nillable element of complex type with simple 
       content (attribute absent) */
    public void testUnmarshal3c()
    {
        System.out.println("testUnmarshal3c()");
        System.out.println(TEST3C);
        XMLDocument doc = xmlHelper.load(TEST3C);
        DataObject test = doc.getRootObject();
        assertTrue(test.isSet("c1"));
        DataObject c1 = test.getDataObject("c1");
        assertNull(c1);
    }

    /* test unmarshalling non-nil nillable element of complex type with complex
       content without attribute */
    public void testUnmarshal4a()
    {
        System.out.println("testUnmarshal4a()");
        System.out.println(TEST4A);
        XMLDocument doc = xmlHelper.load(TEST4A);
        DataObject test = doc.getRootObject();
        assertTrue(test.isSet("c2"));
        DataObject c2 = test.getDataObject("c2");
        assertNotNull(c2);
        Type ctccna = typeHelper.getType(NS, "CTCCNA");
        assertNotNull(ctccna);
        assertEquals(ctccna, c2.getType());
        List<Property> props = ctccna.getProperties();
        assertEquals(3, props.size());
        Property a = ctccna.getProperty("a");
        assertNotNull(a);
        assertFalse(a.isNullable());
        Property b = ctccna.getProperty("b");
        assertNotNull(b);
        assertFalse(b.isNullable());
        Property c = ctccna.getProperty("c");
        assertNotNull(c);
        assertFalse(c.isNullable());
        assertEquals("AAA", c2.get(a));
        assertEquals("BBB", c2.get(b));
        assertEquals("CCC", c2.get(c));
    }

    /* test unmarshalling nil nillable element of complex type with complex
       content without attribute */
    public void testUnmarshal4b()
    {
        System.out.println("testUnmarshal4b()");
        System.out.println(TEST4B);
        XMLDocument doc = xmlHelper.load(TEST4B);
        DataObject test = doc.getRootObject();
        assertNotNull(test);
        assertTrue(test.isSet("c2"));
        DataObject c2 = test.getDataObject("c2");
        assertNull(c2);
    }

    /* test unmarshalling non-nil nillable element of complex type with complex
       content with attribute */
    public void testUnmarshal5a()
    {
        System.out.println("testUnmarshal5a()");
        System.out.println(TEST5A);
        XMLDocument doc = xmlHelper.load(TEST5A);
        DataObject test = doc.getRootObject();
        assertTrue(test.isSet("c3"));
        DataObject c3 = test.getDataObject("c3");
        assertNotNull(c3);
        Type ctccwa = typeHelper.getType(NS, "CTCCWA");
        assertNotNull(ctccwa);
        assertEquals(ctccwa, c3.getType());
        List<Property> props = ctccwa.getProperties();
        assertEquals(4, props.size());
        Property a = ctccwa.getProperty("a");
        assertNotNull(a);
        assertFalse(a.isNullable());
        Property b = ctccwa.getProperty("b");
        assertNotNull(b);
        assertFalse(b.isNullable());
        Property c = ctccwa.getProperty("c");
        assertNotNull(c);
        assertFalse(c.isNullable());
        Property d = ctccwa.getProperty("d");
        assertNotNull(d);
        assertFalse(d.isNullable());
        assertTrue(c3.isSet(a));
        assertTrue(c3.isSet(b));
        assertTrue(c3.isSet(c));
        assertFalse(c3.isSet(d));
        assertEquals("AAA", c3.get(a));
        assertEquals("BBB", c3.get(b));
        assertEquals("CCC", c3.get(c));
        assertEquals(Boolean.FALSE, c3.get(d));
    }

    /* test unmarshalling nil nillable element of complex type with complex
       content (attribute present) */
    public void testUnmarshal5b()
    {
        System.out.println("testUnmarshal5b()");
        System.out.println(TEST5B);
        XMLDocument doc = xmlHelper.load(TEST5B);
        DataObject test = doc.getRootObject();
        assertTrue(test.isSet("c3"));
        DataObject c3 = test.getDataObject("c3");
        assertNotNull(c3);
        assertNull(c3.get("a"));
        assertNull(c3.get("b"));
        assertNull(c3.get("c"));
        assertEquals(Boolean.TRUE, c3.get("d"));
        assertTrue(c3.isSet("d"));
    }

    /* test unmarshalling nil nillable element of complex type with complex
       content (attribute absent) */
    public void testUnmarshal5c()
    {
        System.out.println("testUnmarshal5c()");
        System.out.println(TEST5C);
        XMLDocument doc = xmlHelper.load(TEST5C);
        DataObject test = doc.getRootObject();
        assertTrue(test.isSet("c3"));
        DataObject c3 = test.getDataObject("c3");
        assertNull(c3);
    }

    /* test unmarshalling non-nil nillable element of complex type with complex
       content with default attribute */
    public void testUnmarshal6a()
    {
        System.out.println("testUnmarshal6a()");
        System.out.println(TEST6A);
        XMLDocument doc = xmlHelper.load(TEST6A);
        DataObject test = doc.getRootObject();
        assertTrue(test.isSet("c4"));
        DataObject c4 = test.getDataObject("c4");
        assertNotNull(c4);
        Type ctccwad = typeHelper.getType(NS, "CTCCWAD");
        assertNotNull(ctccwad);
        assertEquals(ctccwad, c4.getType());
        List<Property> props = ctccwad.getProperties();
        assertEquals(4, props.size());
        Property a = ctccwad.getProperty("a");
        assertNotNull(a);
        assertFalse(a.isNullable());
        Property b = ctccwad.getProperty("b");
        assertNotNull(b);
        assertFalse(b.isNullable());
        Property c = ctccwad.getProperty("c");
        assertNotNull(c);
        assertFalse(c.isNullable());
        Property d = ctccwad.getProperty("d");
        assertNotNull(d);
        assertFalse(d.isNullable());
        assertEquals(new Integer(1), d.getDefault());
        assertTrue(c4.isSet(a));
        assertTrue(c4.isSet(b));
        assertTrue(c4.isSet(c));
        assertFalse(c4.isSet(d));
        assertEquals("AAA", c4.get(a));
        assertEquals("BBB", c4.get(b));
        assertEquals("CCC", c4.get(c));
        assertEquals(new Integer(1), c4.get(d));
    }

    /* test unmarshalling nil nillable element of complex type with complex
       content (attribute absent but has default value) */
    public void testUnmarshal6b()
    {
        System.out.println("testUnmarshal6b()");
        System.out.println(TEST6B);
        XMLDocument doc = xmlHelper.load(TEST6B);
        DataObject test = doc.getRootObject();
        assertTrue(test.isSet("c4"));
        DataObject c4 = test.getDataObject("c4");
        assertNull(c4);
    }

    private void printInstanceProperties(DataObject dobj)
    {
        System.out.println("........");
        List<Property> props = dobj.getInstanceProperties();
        for (Property prop : props)
            System.out.println(prop.getName());
    }

    /* dynamically define a type with a nullable property */
    public void testDynamic() throws Exception
    {
        String uri = "http://sdo/test/nullable/dynamic";
        DataObject prototype0 = factory.create("commonj.sdo", "Type");
        prototype0.set("uri", uri);
        prototype0.set("name", "BasicType");
        DataObject property01 = prototype0.createDataObject("property");
        printInstanceProperties(property01);
        property01.set("name", "x");
        property01.set("type", booleanType);
        DataObject property02 = prototype0.createDataObject("property");
        property02.set("name", "y");
        property02.set("type", intType);
        DataObject property03 = prototype0.createDataObject("property");
        property03.set("name", "z");
        property03.set("type", stringType);
        Type basicType = typeHelper.define(prototype0);

        DataObject prototype1 = factory.create("commonj.sdo", "Type");
        prototype1.set("uri", uri);
        prototype1.set("name", "TestType");
        DataObject property11 = prototype1.createDataObject("property");
        printInstanceProperties(property11);
        property11.set("name", "a1");
        property11.set("type", stringType);
        property11.set("nullable", false);
        printInstanceProperties(property11);
        DataObject property12 = prototype1.createDataObject("property");
        property12.set("name", "a2");
        property12.set("type", stringType);
        property12.set("nullable", true);
        DataObject property13 = prototype1.createDataObject("property");
        property13.set("name", "b1");
        property13.set("type", basicType);
        property13.set("containment", true);
        property13.set("nullable", false);
        DataObject property14 = prototype1.createDataObject("property");
        property14.set("name", "b2");
        property14.set("type", basicType);
        property14.set("containment", true);
        property14.set("nullable", true);
        DataObject property15 = prototype1.createDataObject("property");
        property15.set("name", "c");
        property15.set("type", basicType);
        property15.set("containment", true);
        property15.set("nullable", true);
        property15.set("many", true);
        Type testType = typeHelper.define(prototype1);

        List types = new ArrayList();
        types.add(basicType);
        types.add(testType);
        String xsd = xsdHelper.generate(types);
        System.out.println(xsd);
        File f = new File(dir, "nullable_dynamic.xsd");
        Writer w = new FileWriter(f);
        w.write(xsd);
        w.close();
        compareXMLFiles(getResourceFile("type", "nullable_dynamic.xsd_"), f);

        Property p1 = testType.getProperty("a1");
        assertNotNull(p1);
        assertFalse(p1.isNullable());
        Property p2 = testType.getProperty("a2");
        assertNotNull(p2);
        assertTrue(p2.isNullable());
        Property p3 = testType.getProperty("b1");
        assertNotNull(p3);
        assertFalse(p3.isNullable());
        Property p4 = testType.getProperty("b2");
        assertNotNull(p4);
        assertTrue(p4.isNullable());
        Property p5 = testType.getProperty("c");
        assertNotNull(p5);
        assertTrue(p5.isNullable());
        assertTrue(p5.isMany());
    }

    // element is absent
    private static final String SIMPLETEST0 =
        "<nil:simpletest xmlns:nil=\"" + NS + "\">" +
        "<s0>true</s0>" +
        "</nil:simpletest>";

    // element is empty
    private static final String SIMPLETEST1 =
        "<nil:simpletest xmlns:nil=\"" + NS + "\">" +
        "<s0>true</s0>" +
        "<s1a xsi:nil=\"false\" xmlns:xsi=\"" + XSI_URI + "\">123</s1a>" +
        "<s1b/>" +
        "<s2a/>" +
        "<s2b/>" +
        "</nil:simpletest>";

    // element is nil
    private static final String SIMPLETEST2 =
        "<nil:simpletest xmlns:nil=\"" + NS + "\" xmlns:xsi=\"" + XSI_URI + "\">" +
        "<s0>true</s0>" +
        "<s1a>123</s1a>" +
        "<s1b xsi:nil=\"true\"/>" +
        "<s2a xsi:nil=\"false\"/>" +
        "<s2b xsi:nil=\"true\"/>" +
        "</nil:simpletest>";

    /* test interaction of nillability with default values */

    public void testDefaultValue0()
    {
        System.out.println(SIMPLETEST0);
        DataObject test = xmlHelper.load(SIMPLETEST0).getRootObject();
        System.out.println("s0: " + test.get("s0"));
        System.out.println("s1a: " + test.get("s1a"));
        System.out.println("s1b: " + test.get("s1b"));
        System.out.println("s2a: " + test.get("s2a"));
        System.out.println("s2b: " + test.get("s2b"));
        Type t = test.getType();
        Property p1 = t.getProperty("s0");
        assertFalse(p1.isNullable());
        Property p2 = t.getProperty("s1a");
        assertTrue(p2.isNullable());
        Property p3 = t.getProperty("s1b");
        assertTrue(p3.isNullable());
        Property p4 = t.getProperty("s2a");
        assertTrue(p4.isNullable());
        Property p5 = t.getProperty("s2b");
        assertTrue(p5.isNullable());
        Type pt1 = p1.getType();
        Type pt2 = p2.getType();
        Type pt3 = p3.getType();
        Type pt4 = p4.getType();
        Type pt5 = p5.getType();
        assertEquals(booleanType, pt1);
        // Object type because property is nullable
        assertEquals(intObjectType, pt2);
        assertEquals(intObjectType, pt3);
        assertEquals(stringType, pt4);
        assertEquals(stringType, pt5);
        assertTrue(test.isSet(p1));
        assertFalse(test.isSet(p2));
        assertFalse(test.isSet(p3));
        assertFalse(test.isSet(p4));
        assertFalse(test.isSet(p5));
        assertEquals(Boolean.TRUE, test.get(p1));
        assertNull(p1.getDefault());
        assertNull(test.get(p2)); // IntObject type, not set, no default; get returns null
        assertEquals(0, test.getInt(p2)); // getInt returns 0
        assertEquals(new Integer(7), test.get(p3));
        assertNull(test.get(p4));
        assertEquals("N.A.", test.get(p5));
    }

    public void testDefaultValue1()
    {
        System.out.println(SIMPLETEST1);
        DataObject test = xmlHelper.load(SIMPLETEST1).getRootObject();
        System.out.println("s0: " + test.get("s0"));
        System.out.println("s1a: " + test.get("s1a"));
        System.out.println("s1b: " + test.get("s1b"));
        System.out.println("s2a: " + test.get("s2a"));
        System.out.println("s2b: " + test.get("s2b"));
        assertTrue(test.isSet("s0"));
        assertTrue(test.isSet("s1a"));
        assertTrue(test.isSet("s1b"));
        assertTrue(test.isSet("s2a"));
        assertTrue(test.isSet("s2b"));
        assertEquals(Boolean.TRUE, test.get("s0"));
        assertEquals(new Integer(123), test.get("s1a"));
        assertEquals(new Integer(7), test.get("s1b"));
        assertEquals("", test.get("s2a"));
        assertEquals("N.A.", test.get("s2b"));
    }

    public void testDefaultValue2()
    {
        System.out.println(SIMPLETEST2);
        DataObject test = xmlHelper.load(SIMPLETEST2).getRootObject();
        System.out.println("s0: " + test.get("s0"));
        System.out.println("s1a: " + test.get("s1a"));
        System.out.println("s1b: " + test.get("s1b"));
        System.out.println("s2a: " + test.get("s2a"));
        System.out.println("s2b: " + test.get("s2b"));
        assertTrue(test.isSet("s0"));
        assertTrue(test.isSet("s1a"));
        assertTrue(test.isSet("s1b"));
        assertTrue(test.isSet("s2a"));
        assertTrue(test.isSet("s2b"));
        assertEquals(Boolean.TRUE, test.get("s0"));
        assertEquals(new Integer(123), test.get("s1a"));
        assertNull(test.get("s1b"));
        assertEquals("", test.get("s2a"));
        assertNull(test.get("s2b"));
    }
}
