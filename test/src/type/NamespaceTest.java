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

import junit.framework.*;
import common.BaseTest;

/**
 * Test that type with empty/no namespace is found when unmarshalling an
 * instance
 * @author Wing Yew Poon
 */
public class NamespaceTest extends BaseTest
{
    public NamespaceTest(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        
        TestSuite suite = new TestSuite();
        suite.addTest(new NamespaceTest("testDynamicType1"));
        suite.addTest(new NamespaceTest("testDynamicType2"));
        suite.addTest(new NamespaceTest("testStaticType1"));
        suite.addTest(new NamespaceTest("testStaticType2"));
        
        // or
        //TestSuite suite = new TestSuite(NamespaceTest.class);
        return suite;
    }

    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(suite());
    }

    private static DataFactory factory = context.getDataFactory();
    private static TypeHelper typeHelper = context.getTypeHelper();
    private static XMLHelper xmlHelper = context.getXMLHelper();
    private static XSDHelper xsdHelper = context.getXSDHelper();

    /* dynamically defined type
       - use xsi:type to identify the type of the root element */
    public void testDynamicType1()
    {
        DataObject catalogPrototype = factory.create("commonj.sdo", "Type");
        // "uri" may not be null, since it is not defined to be nullable; 
        // so the empty string has to be used to designate no namespace
        catalogPrototype.set("uri", "");
        catalogPrototype.set("name", "catalog1");
        DataObject productPrototype = factory.create("commonj.sdo", "Type");
        productPrototype.set("uri", "");
        productPrototype.set("name", "product1");
        DataObject nameProperty = productPrototype.createDataObject("property");
        nameProperty.set("name", "name");
        nameProperty.set("type", typeHelper.getType("commonj.sdo", "String"));
        Type productType = typeHelper.define(productPrototype);
        DataObject productProperty = catalogPrototype.createDataObject("property");
        productProperty.set("name", "product1");
        productProperty.set("type", productType);
        productProperty.setBoolean("many", true);
        productProperty.setBoolean("containment", true);
        Type catalogType = typeHelper.define(catalogPrototype);
        assertNull(catalogType.getURI());
        List types = new ArrayList();
        types.add(catalogType);
        String xsd = xsdHelper.generate(types);
        System.out.println(xsd);

        String xml = 
            "<catalog1 xsi:type=\"catalog1\" xmlns:xsi=\"" + XSI_URI + "\">" + 
            "<product1 name=\"X\"/></catalog1>";
        System.out.println(xml);
        XMLDocument doc = xmlHelper.load(xml);
        assertEquals("", doc.getRootElementURI());
        DataObject catalog = doc.getRootObject();
        assertEquals(catalogType, catalog.getType());
        Object product = catalog.get("product1");
        assertTrue(product instanceof List);
        List productList = (List)product;
        assertEquals(1, productList.size());
        DataObject product1 = (DataObject)productList.get(0);
        assertEquals(productType, product1.getType());
        assertEquals("X", product1.get("name"));
    }

    /* dynamically defined type
       - along with dynamically defined global property of the same type */
    public void testDynamicType2()
    {
        DataObject catalogPrototype = factory.create("commonj.sdo", "Type");
        catalogPrototype.set("uri", "");
        catalogPrototype.set("name", "catalog2");
        DataObject productPrototype = factory.create("commonj.sdo", "Type");
        productPrototype.set("uri", "");
        productPrototype.set("name", "product2");
        DataObject nameProperty = productPrototype.createDataObject("property");
        nameProperty.set("name", "name");
        nameProperty.set("type", typeHelper.getType("commonj.sdo", "String"));
        Type productType = typeHelper.define(productPrototype);
        DataObject productProperty = catalogPrototype.createDataObject("property");
        productProperty.set("name", "product2");
        productProperty.set("type", productType);
        productProperty.setBoolean("many", true);
        productProperty.setBoolean("containment", true);
        Type catalogType = typeHelper.define(catalogPrototype);
        assertNull(catalogType.getURI());
        DataObject catalogProperty = factory.create("commonj.sdo", "Property");
        catalogProperty.set("name", "catalog2");
        catalogProperty.set("type", catalogType);
        // if global/open content property is defined with null uri, 
        // then it cannot be retrieved using getOpenContentProperty()
        Property globalProperty = typeHelper.defineOpenContentProperty("", catalogProperty);
        Property globalProperty2 = typeHelper.getOpenContentProperty("", "catalog2");
        assertNotNull(globalProperty2);
        assertTrue(globalProperty == globalProperty2);

        List types = new ArrayList();
        types.add(catalogType);
        String xsd = xsdHelper.generate(types);
        System.out.println(xsd);

        String xml = "<catalog2><product2 name=\"X\"/></catalog2>";
        System.out.println(xml);
        XMLDocument doc = xmlHelper.load(xml);
        assertEquals("", doc.getRootElementURI());
        DataObject catalog = doc.getRootObject();
        assertEquals(catalogType, catalog.getType());
        Object product = catalog.get("product2");
        assertTrue(product instanceof List);
        List productList = (List)product;
        assertEquals(1, productList.size());
        DataObject product1 = (DataObject)productList.get(0);
        assertEquals(productType, product1.getType());
        assertEquals("X", product1.get("name"));
    }

    /* type from schema
       - loaded via XSDHelper.define() */
    public void testStaticType1() throws Exception
    {
        File f = getResourceFile("type", "catalognons1.xsd_");
        InputStream in = new FileInputStream(f);
        xsdHelper.define(in, f.toURL().toString());
        // type can be retrieved using either "" or null for uri
        Type catalogType = typeHelper.getType("", "catalog3");
        Type catalogType2 = typeHelper.getType(null, "catalog3");
        assertNull(catalogType.getURI());
        assertNull(catalogType2.getURI());
        assertTrue(catalogType == catalogType2);
        // global element can be retrieved using either "" or null for uri
        Property globalProperty = typeHelper.getOpenContentProperty("", "catalog3");
        Property globalProperty2 = typeHelper.getOpenContentProperty(null, "catalog3");
        Property globalProperty3 = xsdHelper.getGlobalProperty("", "catalog3", true);
        Property globalProperty4 = xsdHelper.getGlobalProperty(null, "catalog3", true);
        assertNotNull(globalProperty);
        assertNotNull(globalProperty2);
        assertNotNull(globalProperty3);
        assertNotNull(globalProperty4);
        assertTrue(globalProperty == globalProperty2);
        assertTrue(globalProperty == globalProperty3);
        assertTrue(globalProperty == globalProperty4);

        String xml = "<catalog3><product3 name=\"X\"/></catalog3>";
        XMLDocument doc = xmlHelper.load(xml);
        assertEquals("", doc.getRootElementURI());
        DataObject catalog = doc.getRootObject();
        assertEquals(catalogType, catalog.getType());
        Object product = catalog.get("product3");
        assertTrue(product instanceof List);
        List productList = (List)product;
        assertEquals(1, productList.size());
        DataObject product1 = (DataObject)productList.get(0);
        Type productType = typeHelper.getType("", "catalog3$product3");
        assertEquals(productType, product1.getType());
        assertEquals("X", product1.get("name"));
        Type productType2 = typeHelper.getType(null, "catalog3$product3");
        assertTrue(productType == productType2);
    }

    /* type from compiled schema */
    public void testStaticType2() throws Exception
    {
        Type catalogType = typeHelper.getType("", "catalog4");
        assertNotNull(catalogType);
        Type catalogType2 = typeHelper.getType(null, "catalog4");
        assertNull(catalogType.getURI());
        assertNull(catalogType2.getURI());
        assertTrue(catalogType == catalogType2);
        Property globalProperty = typeHelper.getOpenContentProperty("", "catalog4");
        Property globalProperty2 = typeHelper.getOpenContentProperty(null, "catalog4");
        Property globalProperty3 = xsdHelper.getGlobalProperty("", "catalog4", true);
        Property globalProperty4 = xsdHelper.getGlobalProperty(null, "catalog4", true);
        assertNotNull(globalProperty);
        assertNotNull(globalProperty2);
        assertNotNull(globalProperty3);
        assertNotNull(globalProperty4);
        assertTrue(globalProperty == globalProperty2);
        assertTrue(globalProperty == globalProperty3);
        assertTrue(globalProperty == globalProperty4);

        String xml = "<catalog4><product4 name=\"X\"/></catalog4>";
        XMLDocument doc = xmlHelper.load(xml);
        assertEquals("", doc.getRootElementURI());
        DataObject catalog = doc.getRootObject();
        assertEquals(catalogType, catalog.getType());
        Object product = catalog.get("product4");
        assertTrue(product instanceof List);
        List productList = (List)product;
        assertEquals(1, productList.size());
        DataObject product1 = (DataObject)productList.get(0);
        Type productType = typeHelper.getType("", "catalog4$product4");
        assertEquals(productType, product1.getType());
        assertEquals("X", product1.get("name"));
        Type productType2 = typeHelper.getType(null, "catalog4$product4");
        assertTrue(productType == productType2);
    }
}
