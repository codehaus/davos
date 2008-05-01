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
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.util.List;
import java.util.ArrayList;

import javax.sdo.DataObject;
import javax.sdo.Type;
import javax.sdo.Property;
import javax.sdo.helper.TypeHelper;
import javax.sdo.helper.XMLHelper;
import javax.sdo.helper.XMLDocument;
import javax.sdo.helper.DataFactory;

import davos.sdo.Options;

import junit.framework.Test;
import junit.framework.TestSuite;
import common.DataTest;

/** 
 *  Test cases for use of a derived type in a property of a given type
 *  and for support of substitution groups.
 *  @author Wing Yew Poon
 */
public class PolymorphismTest extends DataTest
{
    public PolymorphismTest(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        
        TestSuite suite = new TestSuite();
        suite.addTest(new PolymorphismTest("testCreateDerivation"));
        suite.addTest(new PolymorphismTest("testLoadSubstitution1"));
        suite.addTest(new PolymorphismTest("testLoadSubstitution2"));
        suite.addTest(new PolymorphismTest("testLoadSubstitution3"));
        suite.addTest(new PolymorphismTest("testCreateSubstitution"));
        
        // or
        //TestSuite suite = new TestSuite(PolymorphismTest.class);
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

    private static final String NS1 = "http://sdo/test/derivation";
    private static final String NS2 = "http://sdo/test/substitution";
    private static TypeHelper typeHelper = context.getTypeHelper();
    private static DataFactory dataFactory = context.getDataFactory();
    private static XMLHelper xmlHelper = context.getXMLHelper();

    private void printType(DataObject dobj)
    {
        Type t = dobj.getType();
        System.out.println(t.getURI());
        System.out.println(t.getName());
        //System.out.println("  open: " + t.isOpen());
        //System.out.println("  sequenced: " + t.isSequenced());
    }

    private void printProperties(DataObject dobj)
    {
        List<Property> props = dobj.getInstanceProperties();
        for (Property p: props)
        {
            System.out.println("  " + p.getName());
            //System.out.println(p.isMany());
        }        
    }

    public void testCreateDerivation() throws Exception
    {
        Type itemType = typeHelper.getType(NS1, "ItemType");
        DataObject item = dataFactory.create(itemType);
        assertNotNull(item);
        Type productType = typeHelper.getType(NS1, "ProductType");
        Type shirtType = typeHelper.getType(NS1, "ShirtType");
        List shirtBaseTypes = shirtType.getBaseTypes();
        assertEquals(1, shirtBaseTypes.size());
        assertEquals(productType, shirtBaseTypes.get(0));
        System.out.println("properties of product:");
        for (Property p : (List<Property>)productType.getProperties())
        {
            System.out.println(p.getName());
        }
        System.out.println("properties declared in shirt:");
        for (Property p : (List<Property>)shirtType.getDeclaredProperties())
        {
            System.out.println(p.getName());
        }
        System.out.println("properties of shirt:");
        for (Property p : (List<Property>)shirtType.getProperties())
        {
            System.out.println(p.getName());
        }
        DataObject shirt = dataFactory.create(shirtType);
        System.out.println("instance properties of shirt:");
        for (Property p : (List<Property>)shirt.getInstanceProperties())
        {
            System.out.println(p.getName());
        }
        shirt.set("name", "classic T");
        shirt.set("number", "I213");
        shirt.set("size", "M");
        shirt.set("color", "navy");
        item.set("product", shirt);
        DataObject product = (DataObject)item.get("product");
        assertEquals(shirtType, product.getType());
        File f1 = new File(dir, "deriv_product.xml");
        OutputStream out = new FileOutputStream(f1);
        XMLDocument doc = xmlHelper.createDocument(product, NS1, "product");
        doc.setXMLDeclaration(false);
        xmlHelper.save(doc, out, new Options().setSavePrettyPrint());
        out.close();
        File f2 = new File(dir, "deriv_item.xml");
        out = new FileOutputStream(f2);
        doc = xmlHelper.createDocument(item, NS1, "item");
        doc.setXMLDeclaration(false);
        xmlHelper.save(doc, out, new Options().setSavePrettyPrint());
        out.close();
        //compareXMLFiles(getResourceFile("type", "deriv_product.xml"), f1, IGNORE_WHITESPACE);
        //compareXMLFiles(getResourceFile("type", "deriv_item.xml"), f2, IGNORE_WHITESPACE);
        compareXMLFiles(getResourceFile("type", "deriv_product.xml"), f1, STRICT);
        compareXMLFiles(getResourceFile("type", "deriv_item.xml"), f2, STRICT);
    }

    /* load an instance containing substitutions */
    public void testLoadSubstitution1() throws Exception
    {
        System.out.println("loading subst_items.xml ...");
        File f = getResourceFile("type", "subst_items.xml");
        InputStream in = new FileInputStream(f);
        XMLDocument doc = xmlHelper.load(in);
        in.close();
        DataObject items = doc.getRootObject();
        Type t = typeHelper.getType(NS2, "ItemsType");
        assertEquals(t, items.getType());
        assertFalse(t.isSequenced());
        assertFalse(t.isOpen());
        List<Property> props = //t.getProperties();
            items.getInstanceProperties();
        for (Property p: props)
        {
            System.out.println(p.getName());
            System.out.println("  many? " + p.isMany());
            System.out.println("  " + p.getType().getName());
        }
        // items (ItemsType) has 1 property, product, which is many-valued.
        assertEquals(1, props.size());
        Property p = props.get(0);
        assertEquals("product", p.getName());
        assertTrue(p.isMany());
        List<DataObject> products = items.getList(p);
        System.out.println("number of products: " + products.size());
        assertEquals(5, products.size());
        for (DataObject product: products)
        {
            printType(product);
            printProperties(product);
        }
        Type productType = typeHelper.getType(NS2, "ProductType");
        Type shirtType = typeHelper.getType(NS2, "ShirtType");
        Type hatType = typeHelper.getType(NS2, "hat");
        DataObject product1 = products.get(0);
        assertEquals(productType, product1.getType());
        DataObject product2 = products.get(1);
        assertEquals(shirtType, product2.getType());
        DataObject product3 = products.get(2);
        assertEquals(shirtType, product3.getType());
        DataObject product4 = products.get(3);
        assertEquals(hatType, product4.getType());
        DataObject product5 = products.get(4); // umbrella
        assertEquals(productType, product5.getType());
        
        assertNull(items.get("shirt"));
        assertNull(items.get("hat"));
        assertNull(items.get("umbrella"));
    }

    private String qualify(String xml)
    {
        return 
            xml.replaceAll("xmlns=", "xmlns:sub=")
               .replaceAll("item", "sub:item")
               .replaceAll("ShirtType", "sub:ShirtType")
               .replaceAll("product", "sub:product")
               .replaceAll("shirt", "sub:shirt")
               .replaceAll("hat", "sub:hat")
               .replaceAll("umbrella", "sub:umbrella")
               .replaceAll("name", "sub:name")
               .replaceAll("number", "sub:number")
               .replaceAll("size", "sub:size")
               .replaceAll("color", "sub:color")
               .replaceAll("  ", "    ")
               .trim();
    }

    private void _testItem(DataObject item, Type productType)
    {
        Type t = typeHelper.getType(NS2, "ItemType");
        assertEquals(t, item.getType());
        assertFalse(t.isSequenced());
        //assertFalse(t.isOpen()); // FAILS
        List<Property> props = //t.getProperties();
            item.getInstanceProperties();
        for (Property p: props)
        {
            System.out.println(p.getName());
            System.out.println("  many? " + p.isMany());
            System.out.println("  " + p.getType().getName());
        }
        // item (ItemType) has 1 property, product, which is single-valued.
        assertEquals(1, props.size());
        Property p = props.get(0);
        assertEquals("product", p.getName());
        assertFalse(p.isMany());
        DataObject product = (DataObject)item.get(p);
        printType(product);
        assertEquals(productType, product.getType());
    }

    /* load different instances containing different substitutions */
    public void testLoadSubstitution2() throws Exception
    {
        Type productType = typeHelper.getType(NS2, "ProductType");
        Type shirtType = typeHelper.getType(NS2, "ShirtType");
        Type hatType = typeHelper.getType(NS2, "hat");
        System.out.println("1");
        DataObject item1 = getRootDataObject("type", "subst_item1.xml");
        _testItem(item1, productType);
        System.out.println("2");
        DataObject item2 = getRootDataObject("type", "subst_item2.xml");
        _testItem(item2, shirtType);
        System.out.println("3");
        DataObject item3 = getRootDataObject("type", "subst_item3.xml");
        _testItem(item3, hatType);
        System.out.println("4");
        DataObject item4 = getRootDataObject("type", "subst_item4.xml");
        _testItem(item4, productType);

        // check round-tripping
        String xml1 = qualify(getXML(getResourceFile("type", "subst_item1.xml")));
        String xml2 = qualify(getXML(getResourceFile("type", "subst_item2.xml")));
        String xml3 = qualify(getXML(getResourceFile("type", "subst_item3.xml")));
        String xml4 = qualify(getXML(getResourceFile("type", "subst_item4.xml")));
        assertEquals(xml1.replaceAll(">[ \r\n]*<", "><"), xmlHelper.save(item1, NS2, "item"));
        assertEquals(xml2.replaceAll(">[ \r\n]*<", "><"), xmlHelper.save(item2, NS2, "item"));
        assertEquals(xml3.replaceAll(">[ \r\n]*<", "><"), xmlHelper.save(item3, NS2, "item"));
        assertEquals(xml4.replaceAll(">[ \r\n]*<", "><"), xmlHelper.save(item4, NS2, "item"));
    }

    /* load instance that uses derived type instead of substitution */
    public void testLoadSubstitution3() throws Exception
    {
        System.out.println("loading subst_items2b.xml ...");
        DataObject item = getRootDataObject("type", "subst_item2b.xml");
        // item contains "product" (not "shirt") but with xsi:type="ShirtType"
        Type shirtType = typeHelper.getType(NS2, "ShirtType");
        _testItem(item, shirtType);
        // does not round-trip because on marshalling, substitution is used
        String xml = qualify(getXML(getResourceFile("type", "subst_item2.xml")));
        //System.out.println(xml);
        System.out.println("marshalling back out ...");
        xmlHelper.save(item, NS2, "item", System.out);
        System.out.println();
        assertEquals(xml.replaceAll(">[ \r\n]*<", "><"), xmlHelper.save(item, NS2, "item"));
    }

    /* create an instance using the types and properties */
    // might be useful to have example( schema)(s) with elements
    // containing final and block attributes.
    public void testCreateSubstitution() throws Exception
    {
        System.out.println("testCreateSubstitution()");
        Type itemType = typeHelper.getType(NS2, "ItemType");
        DataObject item = dataFactory.create(itemType);
        //dataFactory.create(sdo.test.substitution.ItemType.class);
        assertNotNull(item);
        assertTrue(item instanceof sdo.test.substitution.ItemType);
        Type productType = typeHelper.getType(NS2, "ProductType");
        Type shirtType = typeHelper.getType(NS2, "ShirtType");
        System.out.println(productType.getInstanceClass());
        System.out.println(shirtType.getInstanceClass());
        List shirtBaseTypes = shirtType.getBaseTypes();
        assertEquals(1, shirtBaseTypes.size());
        assertEquals(productType, shirtBaseTypes.get(0));
        DataObject shirt = dataFactory.create(shirtType);
        //dataFactory.create(sdo.test.substitution.ShirtType.class);
        assertTrue(shirt instanceof sdo.test.substitution.ShirtType);
        assertTrue(shirt instanceof sdo.test.substitution.ProductType);
        //assertTrue(shirtType.isInstance(shirt));
        shirt.set("name", "classic T");
        shirt.set("number", "I213");
        shirt.set("size", "M");
        shirt.set("color", "navy");
        //((sdo.test.substitution.ItemType)item).setProduct(shirt);
        item.set("product", shirt);
        xmlHelper.save(shirt, NS2, "shirt", System.out);
        System.out.println();
        xmlHelper.save(item, NS2, "item", System.out);
        System.out.println();
        String xml = qualify(getXML(getResourceFile("type", "subst_item2.xml")));
        assertEquals(xml.replaceAll(">[ \r\n]*<", "><"), xmlHelper.save(item, NS2, "item"));
    }
}
