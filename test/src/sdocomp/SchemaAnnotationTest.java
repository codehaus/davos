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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.List;

import javax.sdo.Type;
import javax.sdo.Property;
import javax.sdo.helper.TypeHelper;
import javax.sdo.helper.XSDHelper;

import junit.framework.Test;
import junit.framework.TestSuite;
import common.BaseTest;

/**
 * @author Wing Yew Poon
 */
public class SchemaAnnotationTest extends BaseTest
{
    public SchemaAnnotationTest(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        
        TestSuite suite = new TestSuite();
        suite.addTest(new SchemaAnnotationTest("testName"));
        suite.addTest(new SchemaAnnotationTest("testPropertyType"));
        suite.addTest(new SchemaAnnotationTest("testSequence"));
        suite.addTest(new SchemaAnnotationTest("testString"));
        suite.addTest(new SchemaAnnotationTest("testDataType"));
        suite.addTest(new SchemaAnnotationTest("testAliasName"));
        suite.addTest(new SchemaAnnotationTest("testReadOnly"));
        suite.addTest(new SchemaAnnotationTest("testMany"));
        suite.addTest(new SchemaAnnotationTest("testInstanceClass"));
        
        // or
        //TestSuite suite = new TestSuite(SchemaAnnotationTest.class);
        return suite;
    }

    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(suite());
    }

    private static XSDHelper xsdHelper = context.getXSDHelper();
    private static TypeHelper typeHelper = context.getTypeHelper();
    private static final String testURI = "http://www.example.com/test";

    // do the following in a static initializer so that 
    // the schema is loaded only once;
    // otherwise there will be AssertionErrors
    static
    {
        File resourceFile = new File(RESOURCES + S + "sdocomp" + S +
                                     "annotations.xsd");
        InputStream resourceStream = null;
        try
        {
            resourceStream = new FileInputStream(resourceFile);
            List types = 
                xsdHelper.define(resourceStream, 
                                 resourceFile.toURL().toString());
        }
        catch (Exception e) { e.printStackTrace(); }
        finally
        {
            try { resourceStream.close(); } catch (IOException ioe) {}
        }
    }

    public void testName()
    {
        Type t = typeHelper.getType(testURI, "PurchaseOrder");
        assertNotNull(t);
        List props = t.getProperties();
        assertEquals(6, props.size());
        Property p = (Property)props.get(0);
        assertEquals("shipTo", p.getName());
        p = (Property)props.get(1);
        assertEquals("billTo", p.getName());
        p = (Property)props.get(2);
        assertEquals("comment", p.getName());
        p = (Property)props.get(3);
        //assertEquals("items", p.getName());
        assertEquals("goodies", p.getName()); // name for items element
        p = (Property)props.get(4);
        assertEquals("orderDate", p.getName());
        p = (Property)props.get(5);
        assertEquals("poNumber", p.getName()); // name for poNum attribute

        t = typeHelper.getType(testURI, "Address");
        assertNotNull(t);
        props = t.getProperties();
        assertEquals(5, props.size());
        p = (Property)props.get(4);
        assertEquals("US", p.getDefault());
        t = typeHelper.getType(testURI, "Stuff"); // name for Items complexType
        assertNotNull(t);
        t = typeHelper.getType(testURI, "Qty");
        assertNotNull(t);
        t = typeHelper.getType(testURI, "StockKeepingUnit"); // name for SKU simpleType
        assertNotNull(t);
    }

    public void testPropertyType()
    {
        Type t = typeHelper.getType(testURI, "Stuff");
        List props = t.getProperties();
        Property p = (Property)props.get(1); // bonusItem
        t = p.getType();
        assertEquals(testURI, t.getURI());
        assertEquals("Item", t.getName());
    }

    public void testSequence()
    {
        Type t = typeHelper.getType(testURI, "Address");
        assertFalse(t.isSequenced());
        t = typeHelper.getType(testURI, "Stuff");
        assertTrue(t.isSequenced());        
    }

    public void testString()
    {
        Type t = typeHelper.getType(testURI, "PurchaseOrder");
        List props = t.getProperties();
        Property p = (Property)props.get(4);
        t = p.getType();
        assertEquals("commonj.sdo", t.getURI());
        assertEquals("String", t.getName());
        System.out.println("type of orderDate: "  + t.getName() + "@" + t.getURI());
    }

    public void testDataType()
    {
        Type t = typeHelper.getType(testURI, "Item");
        List props = t.getProperties();
        Property p = (Property)props.get(1); // quantity
        t = p.getType();
        assertEquals(testURI, t.getURI());
        assertEquals("Qty", t.getName());
        System.out.println("type of quantity: "  + t.getName() + "@" + t.getURI());
        System.out.println("java instance class of quantity: "  + t.getInstanceClass());
        p = (Property)props.get(2); // price
        t = p.getType();
        assertEquals("commonj.sdo", t.getURI());
        assertEquals("Double", t.getName());
        System.out.println("type of price: "  + t.getName() + "@" + t.getURI());
        p = (Property)props.get(3); // shipDate
        t = p.getType();
        assertEquals("commonj.sdo", t.getURI());
        assertEquals("String", t.getName());
        System.out.println("type of shipDate: "  + t.getName() + "@" + t.getURI());
    }

    public void testAliasName()
    {
        Type t = typeHelper.getType(testURI, "PurchaseOrder");
        List aliases = t.getAliasNames();
        assertEquals(2, aliases.size());
        assertEquals("po", aliases.get(0));
        assertEquals("PO", aliases.get(1));
        List props = t.getProperties();
        Property p = (Property)props.get(0);
        aliases = p.getAliasNames();
        assertEquals(2, aliases.size());
        assertEquals("shipAddr", aliases.get(0));
        assertEquals("shippingAddress", aliases.get(1));
        p = (Property)props.get(1);
        aliases = p.getAliasNames();
        assertEquals(1, aliases.size());
        assertEquals("billingAddress", aliases.get(0));
        t = typeHelper.getType(testURI, "Address");
        props = t.getProperties();
        p = (Property)props.get(3); // state
        aliases = p.getAliasNames();
        assertEquals(1, aliases.size());
        assertEquals("province", aliases.get(0));
    }

    public void testReadOnly()
    {
        Type t = typeHelper.getType(testURI, "PurchaseOrder");
        List props = t.getProperties();
        Property p = (Property)props.get(2);
        assertTrue(p.isReadOnly());
        p = (Property)props.get(4);
        assertFalse(p.isReadOnly());
    }

    public void testMany()
    {
        String xsd =
            "<xs:schema targetNamespace=\"http://sdo/test/annotations\"" + newline +
            "  xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"" + newline +
            "  xmlns:sdoxml=\"commonj.sdo/xml\">" + newline +
            "  <xs:complexType name=\"A\">" + newline +
            "    <xs:choice maxOccurs=\"unbounded\">" + newline +
            "      <xs:element name=\"p1\" type=\"xs:int\"/>" + newline +
            "      <xs:element name=\"p2\" type=\"xs:string\"/>" + newline +
            "      <xs:element name=\"p3\" type=\"xs:double\" sdoxml:many=\"false\"/>" + newline +
            "    </xs:choice>" + newline +
            "  </xs:complexType>" + newline +
            "</xs:schema>";
        List types = xsdHelper.define(xsd);
        assertEquals(1, types.size());
        Type typeA = (Type)types.get(0);
        assertEquals("A", typeA.getName());
        assertEquals("http://sdo/test/annotations", typeA.getURI());
        assertTrue(typeA.isSequenced());
        List props = typeA.getProperties();
        assertEquals(3, props.size());
        Property p1 = (Property)props.get(0);
        Property p2 = (Property)props.get(1);
        Property p3 = (Property)props.get(2);
        assertEquals("p1", p1.getName());
        assertEquals("p2", p2.getName());
        assertEquals("p3", p3.getName());
        assertEquals(typeHelper.getType("commonj.sdo", "Int"), p1.getType());
        assertEquals(typeHelper.getType("commonj.sdo", "String"), p2.getType());
        assertEquals(typeHelper.getType("commonj.sdo", "Double"), p3.getType());
        assertTrue(p1.isMany());
        assertTrue(p2.isMany());
        assertFalse(p3.isMany());

        String xsd_gen_exp =
            "<xs:schema targetNamespace=\"http://sdo/test/annotations\"" +
            " xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"" +
            " xmlns:sdo=\"commonj.sdo\"" +
            " xmlns:sdoxml=\"commonj.sdo/xml\"" +
            " xmlns:sdojava=\"commonj.sdo/java\"" +
            " xmlns:tns=\"http://sdo/test/annotations\"" +
            ">" + newline +
            "  <xs:complexType name=\"A\" mixed=\"true\">" + newline +
            "    <xs:choice maxOccurs=\"unbounded\" minOccurs=\"0\">" + newline +
            "      <xs:element type=\"xs:int\" name=\"p1\" maxOccurs=\"unbounded\" minOccurs=\"0\"/>" + newline +
            "      <xs:element type=\"xs:string\" name=\"p2\" maxOccurs=\"unbounded\" minOccurs=\"0\"/>" + newline +
            "      <xs:element type=\"xs:double\" name=\"p3\" minOccurs=\"0\" sdoxml:many=\"false\"/>" + newline +
            "    </xs:choice>" + newline +
            "  </xs:complexType>" + newline +
            "</xs:schema>";
        String xsd_gen = xsdHelper.generate(types);
        System.out.println();
        System.out.println(xsd_gen);
        assertEquals(xsd_gen_exp, xsd_gen);
    }

    public void testInstanceClass()
    {
        Type t = typeHelper.getType(testURI, "Qty");
        assertEquals(java.lang.Short.class, t.getInstanceClass());
    }

}
