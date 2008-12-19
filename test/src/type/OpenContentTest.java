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

import davos.sdo.Options;
import davos.sdo.PropertyXML;
import davos.sdo.TypeXML;
import javax.sdo.DataObject;
import javax.sdo.Property;
import javax.sdo.Sequence;
import javax.sdo.Type;
import javax.sdo.helper.XMLDocument;
import junit.framework.Test;
import junit.framework.TestSuite;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Wing Yew Poon
 */
public class OpenContentTest extends MetaDataTest
{
    public OpenContentTest(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        
        TestSuite suite = new TestSuite();
        
        suite.addTest(new OpenContentTest("testDefineOpenContentProperty"));
        suite.addTest(new OpenContentTest("testGlobalPropertyInSchema"));
        suite.addTest(new OpenContentTest("testGlobalAttributeVsGlobalElement"));
        suite.addTest(new OpenContentTest("testManynessOfGlobalProperty"));
        suite.addTest(new OpenContentTest("testOpenContentAllowed1"));
        suite.addTest(new OpenContentTest("testOpenContentAllowed2"));
        suite.addTest(new OpenContentTest("testOpenContentAllowed3"));
        suite.addTest(new OpenContentTest("testOpenContentNotAllowed"));
        suite.addTest(new OpenContentTest("testUnmarshalOpenContent"));
        suite.addTest(new OpenContentTest("testUnmarshalOpenContentList1"));
        suite.addTest(new OpenContentTest("testUnmarshalOpenContentList2"));
        suite.addTest(new OpenContentTest("testCreateDataObject1"));
        suite.addTest(new OpenContentTest("testCreateDataObject2"));
        suite.addTest(new OpenContentTest("testAddToList0"));
        suite.addTest(new OpenContentTest("testAddToList1"));
        suite.addTest(new OpenContentTest("testAddToList2"));
        suite.addTest(new OpenContentTest("testAddToSequence"));
        
        suite.addTest(new OpenContentTest("testSetOnDemand0"));
        suite.addTest(new OpenContentTest("testSetOnDemand1"));
        suite.addTest(new OpenContentTest("testSetOnDemand2"));
        suite.addTest(new OpenContentTest("testSetOnDemand3"));
        suite.addTest(new OpenContentTest("testSetOnDemand4"));
        suite.addTest(new OpenContentTest("testSetOnDemand5"));
        suite.addTest(new OpenContentTest("testSetOnDemand6"));
        suite.addTest(new OpenContentTest("testSetOnDemand7"));
        suite.addTest(new OpenContentTest("testSetOnDemand8"));
        suite.addTest(new OpenContentTest("testSetOnDemand9"));
        suite.addTest(new OpenContentTest("testSetOnDemand9b"));
        suite.addTest(new OpenContentTest("testSetOnDemand10"));
        suite.addTest(new OpenContentTest("testSetOnDemand11"));
        
        suite.addTest(new OpenContentTest("testAddToSequenceOnDemand1"));
        suite.addTest(new OpenContentTest("testAddToSequenceOnDemand2"));
        suite.addTest(new OpenContentTest("testAddToSequenceOnDemand3"));
        
        suite.addTest(new OpenContentTest("testCreateDataObjectOnDemand1"));
        suite.addTest(new OpenContentTest("testCreateDataObjectOnDemand2"));
        
        suite.addTest(new OpenContentTest("testGetListOnDemand1"));
        suite.addTest(new OpenContentTest("testGetListOnDemand1b"));
        
        suite.addTest(new OpenContentTest("testUnmarshalKnownOpenContentAttribute"));
        suite.addTest(new OpenContentTest("testUnmarshalUnknownOpenContentAttribute"));
        suite.addTest(new OpenContentTest("testUnmarshalKnownOpenContentElement"));
        suite.addTest(new OpenContentTest("testUnmarshalUnknownOpenContentElementA"));
        suite.addTest(new OpenContentTest("testUnmarshalUnknownOpenContentElementB1"));
        suite.addTest(new OpenContentTest("testUnmarshalUnknownOpenContentElementB2"));
        
        // or
        //TestSuite suite = new TestSuite(OpenContentTest.class);
        return suite;
    }

    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(suite());
    }

    private static final Type T_DATAOBJECT = typeHelper.getType("commonj.sdo", "DataObject");
    private static final Type T_BEADATAOBJECT = typeHelper.getType("http://www.bea.com/sdo/types", "DataObject");
    private static final Type T_WRAPPER = typeHelper.getType("http://www.bea.com/sdo/types", "WrapperType");


    public void testDefineOpenContentProperty()
    {
        System.out.println("testDefineOpenContentProperty()");
        String uri = "http://www.example.com/prod";
        // define an open content property using TypeHelper
        Type propertyType = typeHelper.getType("commonj.sdo", "Property");
        DataObject globalPropertyPrototype = factory.create(propertyType);
        globalPropertyPrototype.set("name", "size");
        globalPropertyPrototype.set("type", intType);
        Property globalProperty = typeHelper.defineOpenContentProperty(uri, globalPropertyPrototype);
        assertNotNull(globalProperty);

        Type containingType = globalProperty.getContainingType();
        assertNotNull(containingType);
        System.out.println(containingType.getName() + "@" + containingType.getURI());
        assertEquals(uri, containingType.getURI());

        // XSDHelper should know about the property
        // it should be a global element
        Property sizeProperty1 = xsdHelper.getGlobalProperty(uri, "size", true);
        assertNotNull(sizeProperty1);
        assertTrue(sizeProperty1 == globalProperty);
        Property sizeProperty2 = xsdHelper.getGlobalProperty(uri, "size", false);
        assertNull(sizeProperty2);

        // TODO: generate the xsd containing the global element
        // by generating xsd for its containing type
    }
    
    public void testGlobalPropertyInSchema()
    {
        System.out.println("testGlobalPropertyInSchema()");
        String uri = "http://www.example.com/choice";
        Property itemsProperty = xsdHelper.getGlobalProperty(uri, "items", true);
        assertNotNull(itemsProperty);
        Property itemsProperty2 = xsdHelper.getGlobalProperty("http://www.example.com/choice", "items", false);
        assertNull(itemsProperty2);
        assertTrue(xsdHelper.isElement(itemsProperty));
        assertFalse(xsdHelper.isAttribute(itemsProperty));
        Type itemsType = typeHelper.getType(uri, "ItemsType");
        assertEquals(itemsType, itemsProperty.getType());
        Type containingType = itemsProperty.getContainingType();
        assertNotNull(containingType);
        System.out.println(containingType.getName() + "@" + containingType.getURI());
        assertEquals(uri, containingType.getURI());

        // global property is available as open content property
        // it should be accessible from TypeHelper.getOpenContentProperty()
        Property p = typeHelper.getOpenContentProperty(uri, "items");
        assertNotNull(p);
        assertTrue(p.isOpenContent());
        assertEquals(itemsProperty, p);
    }
    
    public void testGlobalAttributeVsGlobalElement()
    {
        System.out.println("testGlobalAttributeVsGlobalElement()");
        String uri = "http://sdo/test/global";
        Property sizeElement = xsdHelper.getGlobalProperty(uri, "size", true);
        assertNotNull(sizeElement);
        Property sizeAttribute = xsdHelper.getGlobalProperty(uri, "size", false);
        assertNotNull(sizeAttribute);
        assertFalse(sizeElement == sizeAttribute);
        assertTrue(xsdHelper.isElement(sizeElement));
        assertFalse(xsdHelper.isAttribute(sizeElement));
        assertFalse(xsdHelper.isElement(sizeAttribute));
        assertTrue(xsdHelper.isAttribute(sizeAttribute));
        Type sizeType = typeHelper.getType(uri, "SizeType");
        assertNotNull(sizeType);
        assertEquals(sizeType, sizeElement.getType());
        assertEquals(intType, sizeAttribute.getType());

        // properties have different SDO names than XSD names,
        // since they have to be unique?
        String elementName = sizeElement.getName();
        String attributeName = sizeAttribute.getName();
        assertEquals(uri, sizeElement.getContainingType().getURI());
        assertEquals(uri, sizeAttribute.getContainingType().getURI());
        System.out.println(elementName); // size
        System.out.println(attributeName); // size
        // which one is returned by TypeHelper.getOpenContentProperty()?
        Property p = typeHelper.getOpenContentProperty(uri, "size");
        System.out.println("element? " + (p == sizeElement)); // true
        System.out.println("attribute? " + (p == sizeAttribute)); // false
        // TODO: generate xsd containing the element and the attribute
        // test round-tripping
    }

    public void testManynessOfGlobalProperty()
    {
        System.out.println("testManynessOfGlobalProperty()");
        String uri = "http://sdo/test/global";
        // <xs:element name="test" type="SimpleType"/>
        // <xs:element name="test2" type="SimpleType" sdoxml:many="true"/>
        // <xs:element name="test3" type="SimpleType" sdoxml:many="false"/>
        Property p1 = xsdHelper.getGlobalProperty(uri, "test", true);
        assertNotNull(p1);
        Property p2 = xsdHelper.getGlobalProperty(uri, "test2", true);
        assertNotNull(p2);
        Property p3 = xsdHelper.getGlobalProperty(uri, "test3", true);
        assertNotNull(p3);
        assertTrue(p1.isMany()); // global property is now many by default
        assertTrue(p2.isMany());
        assertFalse(p3.isMany());
    }

    private static final String OPEN_A =
        "<open:a xmlns:open=\"http://sdo/test/opencontent\">" + newline +
        "    <open:name>My open content object</open:name>" + newline +
        "    <glob:test xmlns:glob=\"http://sdo/test/global\">" + newline +
        "        <glob:x>Extra! Extra!</glob:x>" + newline +
        "        <glob:y>1000000</glob:y>" + newline +
        "    </glob:test>" + newline +
        "</open:a>";

    private static final String OPEN_B =
        "<open:b glob:xxx=\"true\" xmlns:open=\"http://sdo/test/opencontent\" xmlns:glob=\"http://sdo/test/global\">" + newline +
        "    <open:name>My open content object</open:name>" + newline +
       "</open:b>";

    private static final String CUSTOMER =
        "<open:customer xmlns:open=\"http://sdo/test/opencontent\">" + newline +
        "    <open:id>123456789</open:id>" + newline +
        "    <open:name>Sanjay Kumar</open:name>" + newline +
        "    <glob:test xmlns:glob=\"http://sdo/test/global\">" + newline +
        "        <glob:x>Extra! Extra!</glob:x>" + newline +
        "        <glob:y>1000000</glob:y>" + newline +
        "    </glob:test>" + newline +
        "</open:customer>";

    private static final String OPEN_D =
        "<open:d xmlns:open=\"http://sdo/test/opencontent\">" + newline +
        "    <open:name>My open content object</open:name>" + newline +
        "    <open:number>2</open:number>" + newline +
        "    <glob:test xmlns:glob=\"http://sdo/test/global\">" + newline +
        "        <glob:x>Extra! Extra!</glob:x>" + newline +
        "        <glob:y>1000000</glob:y>" + newline +
        "    </glob:test>" + newline +
        "    <glob:test xmlns:glob=\"http://sdo/test/global\">" + newline +
        "        <glob:x>More! More!</glob:x>" + newline +
        "        <glob:y>2000000</glob:y>" + newline +
        "    </glob:test>" + newline +
        "</open:d>";

    private static final String OPEN_Dt =
        "<open:d xmlns:open=\"http://sdo/test/opencontent\">" + newline +
        "    <open:name>My open content object</open:name>" + newline +
        "    <open:number>2</open:number>" + newline +
        "    <t xsi:type=\"glob:SimpleType\" xmlns:glob=\"http://sdo/test/global\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" + newline +
        "        <glob:x>Extra! Extra!</glob:x>" + newline +
        "        <glob:y>1000000</glob:y>" + newline +
        "    </t>" + newline +
        "    <t xsi:type=\"glob:SimpleType\" xmlns:glob=\"http://sdo/test/global\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" + newline +
        "        <glob:x>More! More!</glob:x>" + newline +
        "        <glob:y>2000000</glob:y>" + newline +
        "    </t>" + newline +
        "</open:d>";

    private static final String OPEN_D1 =
        "<open:d xmlns:open=\"http://sdo/test/opencontent\">" + newline +
        "    <open:name>My open content object</open:name>" + newline +
        "    <open:number>1</open:number>" + newline +
        "    <glob:test2 xmlns:glob=\"http://sdo/test/global\">" + newline +
        "        <glob:x>Extra! Extra!</glob:x>" + newline +
        "        <glob:y>1000000</glob:y>" + newline +
        "    </glob:test2>" + newline +
        "</open:d>";

    private static final String OPEN_D2 =
        "<open:d xmlns:open=\"http://sdo/test/opencontent\">" + newline +
        "    <open:name>My open content object</open:name>" + newline +
        "    <open:number>2</open:number>" + newline +
        "    <glob:test2 xmlns:glob=\"http://sdo/test/global\">" + newline +
        "        <glob:x>Extra! Extra!</glob:x>" + newline +
        "        <glob:y>1000000</glob:y>" + newline +
        "    </glob:test2>" + newline +
        "    <glob:test2 xmlns:glob=\"http://sdo/test/global\">" + newline +
        "        <glob:x>More! More!</glob:x>" + newline +
        "        <glob:y>2000000</glob:y>" + newline +
        "    </glob:test2>" + newline +
        "</open:d>";

    private static final String OPEN_E =
        "<open:e xmlns:open=\"http://sdo/test/opencontent\">" + newline +
        "    <open:name>My open content object</open:name>" + newline +
        "    <glob:test xmlns:glob=\"http://sdo/test/global\">" + newline +
        "        <glob:x>Extra! Extra!</glob:x>" + newline +
        "        <glob:y>1000000</glob:y>" + newline +
        "    </glob:test>" + newline +
        "</open:e>";

    // In testOpenContentAllowedn(), we test setting open content in a 
    // DataObject using set(Property, Object).

    /* open content is allowed when type is open:
       case 1: type defined by schema, has any
     */
    public void testOpenContentAllowed1() throws Exception
    {
        System.out.println("testOpenContentAllowed1()");
        Type t = typeHelper.getType("http://sdo/test/opencontent", "A");
        assertNotNull(t);
        assertTrue(t.isOpen());
        DataObject a = factory.create(t);
        a.set("name", "My open content object");
        Property p = typeHelper.getOpenContentProperty("http://sdo/test/global", "test");
        assertNotNull(p);
        Type pt = p.getType();
        DataObject test = factory.create(pt);
        test.set("x", "Extra! Extra!");
        test.set("y", 1000000);
        a.set(p, test);
        //xmlHelper.save(a, "http://sdo/test/opencontent", "a", System.out);
        //System.out.println();
        StringWriter out = new StringWriter();
        XMLDocument doc = xmlHelper.createDocument(a, "http://sdo/test/opencontent", "a");
        doc.setXMLDeclaration(false);
        xmlHelper.save(doc, out, new Options().setSavePrettyPrint());
        assertEquals(OPEN_A, out.toString());
    }

    /* open content is allowed when type is open:
       case 2: type defined by schema, has anyAttribute
     */
    public void testOpenContentAllowed2() throws Exception
    {
        System.out.println("testOpenContentAllowed2()");
        Type t = typeHelper.getType("http://sdo/test/opencontent", "B");
        assertNotNull(t);
        assertTrue(t.isOpen());
        DataObject b = factory.create(t);
        b.set("name", "My open content object");
        Property p = typeHelper.getOpenContentProperty("http://sdo/test/global", "xxx");
        assertNotNull(p);
        b.set(p, true);
        //xmlHelper.save(b, "http://sdo/test/opencontent", "b", System.out);
        //System.out.println();
        StringWriter out = new StringWriter();
        XMLDocument doc = xmlHelper.createDocument(b, "http://sdo/test/opencontent", "b");
        doc.setXMLDeclaration(false);
        xmlHelper.save(doc, out, new Options().setSavePrettyPrint());
        assertEquals(OPEN_B, out.toString());
    }

    /* open content is allowed when type is open:
       case 3: type defined dynamically, open set to true
     */
    public void testOpenContentAllowed3() throws Exception
    {
        System.out.println("testOpenContentAllowed3()");
        String uri = "http://sdo/test/opencontent";
        DataObject customerPrototype = factory.create("commonj.sdo", "Type");
        customerPrototype.set("uri", uri);
        customerPrototype.set("name", "Customer");
        //customerPrototype.set("open", true);
        DataObject customerIdProperty = customerPrototype.createDataObject("property");
        customerIdProperty.set("name", "id");
        customerIdProperty.set("type", intType);
        DataObject customerNameProperty = customerPrototype.createDataObject("property");
        customerNameProperty.set("name", "name");
        customerNameProperty.set("type", stringType);
        Type customerType = typeHelper.define(customerPrototype);
        //assertTrue(customerType.isOpen());
        DataObject cust = factory.create(customerType);
        cust.set("name", "Sanjay Kumar");
        cust.set("id", 123456789);
        Property p = typeHelper.getOpenContentProperty("http://sdo/test/global", "test");
        assertNotNull(p);
        Type pt = p.getType();
        DataObject test = factory.create(pt);
        test.set("x", "Extra! Extra!");
        test.set("y", 1000000);
        //cust.set(p, test);
        StringWriter out = new StringWriter();
        XMLDocument doc = xmlHelper.createDocument(cust, "http://sdo/test/opencontent", "customer");
        doc.setXMLDeclaration(false);
        xmlHelper.save(doc, out, new Options().setSavePrettyPrint());
        System.out.println(out.toString());
        //assertEquals(CUSTOMER, out.toString());
    }

    /* open content is allowed only when type is open
     */
    public void testOpenContentNotAllowed() throws Exception
    {
        System.out.println("testOpenContentNotAllowed()");
        Type t = typeHelper.getType("http://sdo/test/opencontent", "C");
        assertNotNull(t);
        assertFalse(t.isOpen());
        DataObject c = factory.create(t);
        c.set("name", "My non-open content object");
        c.set("number", 123);
        Property p = typeHelper.getOpenContentProperty("http://sdo/test/global", "xxx");
        assertNotNull(p);
        try
        {
            c.set(p, true);
            fail("should have thrown an IllegalArgumentException");
        }
        catch (Exception e)
        {
            assertTrue(e instanceof IllegalArgumentException);
        }
    }

    /* unmarshal data object with open content;
       test characteristics of open content property:
       - Property.isOpenContent() returns true
       - property appears in DataObject.getInstanceProperties()
         but not in getType().getProperties()
       - DataObject.isSet(Property) returns true
    */
    public void testUnmarshalOpenContent()
    {
        System.out.println("testUnmarshalOpenContent()");
        XMLDocument doc = xmlHelper.load(OPEN_A);
        DataObject a = doc.getRootObject();
        Type t = typeHelper.getType("http://sdo/test/opencontent", "A");
        assertEquals(t, a.getType());
        List<Property> properties = (List<Property>)t.getProperties();
        assertEquals(2, properties.size());
        Property p1 = properties.get(0);
        assertEquals("name", p1.getName());
        Property p2 = properties.get(1); // optional property, not in instance
        assertEquals("id", p2.getName());
        assertEquals(p1, a.getInstanceProperty("name"));
        assertEquals(p2, a.getInstanceProperty("id"));
        // all type properties, even if absent, appear in instance properties
        List<Property> instanceProperties = (List<Property>)a.getInstanceProperties();
        assertEquals(3, instanceProperties.size());
        assertEquals(p1, instanceProperties.get(0));
        assertEquals(p2, instanceProperties.get(1));
        Property p3 = instanceProperties.get(2);
        assertEquals("test", p3.getName());
        assertEquals("http://sdo/test/global", p3.getContainingType().getURI());
        assertTrue(p3.isOpenContent());
        assertTrue(a.isSet(p3));
        assertEquals(p3, a.getInstanceProperty("test"));
        System.out.println("instance property \"test\" found");

        assertEquals("My open content object", a.get("name"));
        assertEquals("My open content object", a.get(p1));
        assertFalse(a.isSet(p2));
        DataObject test = a.getDataObject("test");
        assertNotNull(test);
        DataObject v3 = a.getDataObject(p3);
        assertNotNull(v3);
        assertTrue(test == v3);
        assertEquals("Extra! Extra!", test.get("x"));
        assertEquals(1000000, test.get("y"));
    }

    public void testUnmarshalOpenContentList1() throws Exception
    {
        System.out.println("testUnmarshalOpenContentList1()");
        DataObject root = xmlHelper.load(OPEN_D).getRootObject();
        Type t = typeHelper.getType("http://sdo/test/opencontent", "D");
        assertTrue(t.isOpen());
        assertTrue(t.isSequenced());
        assertEquals(t, root.getType());
        assertEquals("My open content object", root.get("name"));
        assertEquals(2, root.get("number"));
        List<Property> instanceProperties = (List<Property>)root.getInstanceProperties();
        assertEquals(3, instanceProperties.size());
        for (Property prop : instanceProperties)
            System.out.println(prop);
        Property p = typeHelper.getOpenContentProperty("http://sdo/test/global", "test");
        Property p3 = instanceProperties.get(2);
        assertTrue(p3.isOpenContent());
        assertEquals(p, p3);
        Property testp = root.getInstanceProperty("test");
        assertEquals(p, testp);
        //assertTrue(testp.isMany());
        //List openList = root.getList("test"); // CCE
        //assertNotNull(openList);
        //assertEquals(2, openList.size());
        Sequence seq = root.getSequence();
        System.out.println(OPEN_D);
        System.out.println(seq.size());
        System.out.println(seq.getProperty(0));
        System.out.println(seq.getValue(0));
        System.out.println(seq.getProperty(1));
        System.out.println(seq.getValue(1));
        System.out.println(seq.getProperty(2));
        System.out.println(seq.getValue(2));
        System.out.println(((DataObject)seq.getValue(2)).get("x"));
        System.out.println(((DataObject)seq.getValue(2)).get("y"));
    }

    public void testUnmarshalOpenContentList2() throws Exception
    {
        System.out.println("testUnmarshalOpenContentList2()");
        DataObject root = xmlHelper.load(OPEN_D2).getRootObject();
        Type t = typeHelper.getType("http://sdo/test/opencontent", "D");
        assertEquals(t, root.getType());
        assertEquals("My open content object", root.get("name"));
        assertEquals(2, root.get("number"));
        List<Property> instanceProperties = (List<Property>)root.getInstanceProperties();
        assertEquals(3, instanceProperties.size());
        for (Property prop : instanceProperties)
            System.out.println(prop);
        Property p = typeHelper.getOpenContentProperty("http://sdo/test/global", "test2");
        Property p3 = instanceProperties.get(2);
        assertTrue(p3.isOpenContent());
        assertEquals(p, p3);
        Property testp = root.getInstanceProperty("test2");
        assertEquals(p, testp);
        assertTrue(testp.isMany());
        List openList = root.getList("test2");
        assertNotNull(openList);
        assertEquals(2, openList.size());
    }

    // Test other ways of setting open content in a DataObject 
    // (besides using set(Property, Object)):
    // 1. test createDataObject(Property) and createDataObject(Property, Type) 
    // with open content Property
    // 2. test getList(Property) with open content Property and add to List
    // 3. test getSequence() and add(Property, Object) or 
    // add(int, Property, Object) with open content Property

    private DataObject createOpenA()
    {
        Type t = typeHelper.getType("http://sdo/test/opencontent", "A");
        DataObject a = factory.create(t);
        a.set("name", "My open content object");
        return a;
    }

    public void testCreateDataObject1() throws Exception
    {
        System.out.println("testCreateDataObject1()");
        DataObject a = createOpenA();
        Property p = typeHelper.getOpenContentProperty("http://sdo/test/global", "test");
        DataObject test = a.createDataObject(p);
        test.set("x", "Extra! Extra!");
        test.set("y", 1000000);
        //xmlHelper.save(a, "http://sdo/test/opencontent", "a", System.out);
        //System.out.println();
        StringWriter out = new StringWriter();
        XMLDocument doc = xmlHelper.createDocument(a, "http://sdo/test/opencontent", "a");
        doc.setXMLDeclaration(false);
        xmlHelper.save(doc, out, new Options().setSavePrettyPrint());
        assertEquals(OPEN_A, out.toString());
    }

    public void testCreateDataObject2() throws Exception
    {
        System.out.println("testCreateDataObject2()");
        DataObject a = createOpenA();
        Property p = typeHelper.getOpenContentProperty("http://sdo/test/global", "test");
        Type pt = p.getType();
        DataObject test = a.createDataObject(p, pt);
        test.set("x", "Extra! Extra!");
        test.set("y", 1000000);
        //xmlHelper.save(a, "http://sdo/test/opencontent", "a", System.out);
        //System.out.println();
        StringWriter out = new StringWriter();
        XMLDocument doc = xmlHelper.createDocument(a, "http://sdo/test/opencontent", "a");
        doc.setXMLDeclaration(false);
        xmlHelper.save(doc, out, new Options().setSavePrettyPrint());
        assertEquals(OPEN_A, out.toString());
    }

    public void testAddToList0() throws Exception
    {
        System.out.println("testAddToList0()");
        Type t = typeHelper.getType("http://sdo/test/opencontent", "D");
        assertTrue(t.isOpen());
        DataObject root = factory.create(t);
        root.set("name", "My open content object");
        root.set("number", 2);
        Property p = typeHelper.getOpenContentProperty("http://sdo/test/global", "test3");
        assertNotNull(p);
        assertTrue(p.isOpenContent());
        assertFalse(p.isMany());
        List openList = root.getList(p);
        assertNull(openList);
    }

    public void testAddToList1() throws Exception
    {
        System.out.println("testAddToList1()");
        Type t = typeHelper.getType("http://sdo/test/opencontent", "D");
        assertTrue(t.isOpen());
        DataObject root = factory.create(t);
        root.set("name", "My open content object");
        root.set("number", 2);
        Property p = typeHelper.getOpenContentProperty("http://sdo/test/global", "test2");
        assertNotNull(p);
        assertTrue(p.isOpenContent());
        assertTrue(p.isMany());
        List openList = root.getList(p);
        assertNotNull(openList);
        assertEquals(0, openList.size());
        Type pt = p.getType();
        DataObject test1 = factory.create(pt);
        test1.set("x", "Extra! Extra!");
        test1.set("y", 1000000);
        openList.add(test1);
        DataObject test2 = factory.create(pt);
        test2.set("x", "More! More!");
        test2.set("y", 2000000);
        openList.add(test2);
        //xmlHelper.save(root, "http://sdo/test/opencontent", "d", System.out);
        //System.out.println();
        StringWriter out = new StringWriter();
        XMLDocument doc = xmlHelper.createDocument(root, "http://sdo/test/opencontent", "d");
        doc.setXMLDeclaration(false);
        xmlHelper.save(doc, out, new Options().setSavePrettyPrint());
        assertEquals(OPEN_D2, out.toString());
    }

    public void testAddToList2() throws Exception
    {
        System.out.println("testAddToList2()");
        DataObject root = xmlHelper.load(OPEN_D1).getRootObject();
        Type t = typeHelper.getType("http://sdo/test/opencontent", "D");
        assertEquals(t, root.getType());
        root.set("number", 2);
        List<Property> instanceProperties = (List<Property>)root.getInstanceProperties();
        assertEquals(3, instanceProperties.size());
        Property p = typeHelper.getOpenContentProperty("http://sdo/test/global", "test2");
        Property p3 = instanceProperties.get(2);
        assertTrue(p3.isOpenContent());
        assertTrue(p3.isMany());
        assertEquals(p, p3);
        List openList = root.getList("test2");
        assertNotNull(openList);
        assertEquals(1, openList.size());
        Type pt = p.getType();
        DataObject test2 = factory.create(pt);
        test2.set("x", "More! More!");
        test2.set("y", 2000000);
        openList.add(test2);
        //xmlHelper.save(root, "http://sdo/test/opencontent", "d", System.out);
        //System.out.println();
        StringWriter out = new StringWriter();
        XMLDocument doc = xmlHelper.createDocument(root, "http://sdo/test/opencontent", "d");
        doc.setXMLDeclaration(false);
        xmlHelper.save(doc, out, new Options().setSavePrettyPrint());
        assertEquals(OPEN_D2, out.toString());
    }

    public void testAddToSequence() throws Exception
    {
        System.out.println("testAddToSequence()");
        String uri = "http://sdo/test/opencontent";
        Type t = typeHelper.getType(uri, "E");
        assertTrue(t.isOpen());
        assertTrue(t.isSequenced());
        DataObject root = factory.create(t);
        Sequence seq = root.getSequence();
        Property p = typeHelper.getOpenContentProperty("http://sdo/test/global", "test");
        Type pt = p.getType();
        DataObject test = factory.create(pt);
        test.set("x", "Extra! Extra!");
        test.set("y", 1000000);
        seq.add("symbol", "XXX");
        seq.add("number", 1000);
        seq.add(p, test);
        seq.add("symbol", "FIN");
        xmlHelper.save(root, "http://sdo/test/opencontent", "e", System.out);
        System.out.println();
    }

    // Test creation of open content properties on demand (on the fly):
    // 1. set(String, Object)
    // 2. getSequence() and add(String, Object) or add(int, String, Object)
    // where String is the name of a property that does not exist in the
    // (open) type.
    // 3. createDataObject(String) or createDataObject(String, String, String)
    // 4. getList(String) and add to List

    // 1. set(String, Object)
    // data object is NOT of open type (negative test case)
    public void testSetOnDemand0() throws Exception
    {
        System.out.println("testSetOnDemand0()");
        Type t = typeHelper.getType("http://sdo/test/opencontent", "C");
        assertNotNull(t);
        assertFalse(t.isOpen());
        DataObject c = factory.create(t);
        c.set("name", "My non-open content object");
        c.set("number", 123);
        try
        {
            c.set("open", true);
            fail("should have thrown an IllegalArgumentException");
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
            // "Property 'open' not allowed on this type: sdoType C@http://sdo/test/opencontent"
            assertTrue(e instanceof IllegalArgumentException);
            String msg = e.getMessage();
            int i = msg.indexOf("Property");
            assertTrue(i >= 0);
            int j = msg.indexOf("not allowed on this type");
            assertTrue(j > i);
        }
    }

    // Object is a String
    public void testSetOnDemand1() throws Exception
    {
        System.out.println("testSetOnDemand1()");
        DataObject a = createOpenA();
        assertTrue(a.getType().isOpen());
        a.set("o", "open content");
        Property p = a.getInstanceProperty("o");
        assertTrue(p.isOpenContent());
        assertEquals(stringType, p.getType());
        assertFalse(p.isContainment());
        assertFalse(p.isMany());
        assertNull(((PropertyXML)p).getXMLNamespaceURI());
        assertTrue(((PropertyXML)p).isXMLElement());
        StringWriter out = new StringWriter();
        XMLDocument doc = xmlHelper.createDocument(a, "http://sdo/test/opencontent", "a");
        doc.setXMLDeclaration(false);
        xmlHelper.save(doc, out, new Options().setSavePrettyPrint());
        // no xsi:type written for o (property type is String)
        String exp =
            "<open:a xmlns:open=\"http://sdo/test/opencontent\">" + newline +
            "    <open:name>My open content object</open:name>" + newline +
            "    <o>open content</o>" + newline +
            "</open:a>";
        assertEquals(exp, out.toString());
    }

    // Object is an Integer
    public void testSetOnDemand2() throws Exception
    {
        System.out.println("testSetOnDemand2()");
        DataObject a = createOpenA();
        assertTrue(intType == typeHelper.getType(int.class));
        assertTrue(intObjectType == typeHelper.getType(Integer.class));
        a.set("o", new Integer(100));
        Property p = a.getInstanceProperty("o");
        assertTrue(p.isOpenContent());
        //assertEquals(intObjectType, p.getType()); // would be better if it were intType; but this needs spec amendment/clarification
        assertEquals(intType, p.getType());
        assertFalse(p.isContainment());
        assertFalse(p.isMany());
        assertNull(((PropertyXML)p).getXMLNamespaceURI());
        assertTrue(((PropertyXML)p).isXMLElement());
        StringWriter out = new StringWriter();
        XMLDocument doc = xmlHelper.createDocument(a, "http://sdo/test/opencontent", "a");
        doc.setXMLDeclaration(false);
        xmlHelper.save(doc, out, new Options().setSavePrettyPrint());
        String exp =
            "<open:a xmlns:open=\"http://sdo/test/opencontent\">" + newline +
            "    <open:name>My open content object</open:name>" + newline +
            "    <o xsi:type=\"xs:int\" xmlns:xs=\"" + XSD_URI + "\" xmlns:xsi=\"" + XSI_URI + "\">100</o>" + newline +
            "</open:a>";
        assertEquals(exp, out.toString());
    }

    // Object is a (typed) DataObject, not contained
    public void testSetOnDemand3() throws Exception
    {
        System.out.println("testSetOnDemand3()");
        DataObject a = createOpenA();
        assertTrue(a.getType().isOpen());
        Type t = typeHelper.getType("http://sdo/test/global", "SimpleType");
        DataObject test = factory.create(t); //("http://sdo/test/global", "SimpleType");
        test.set("x", "Extra! Extra!");
        test.set("y", 1000000);
        assertNull(test.getContainer()); // not contained
        a.set("o", test);
        assertTrue(a == test.getContainer());
        Property p = a.getInstanceProperty("o");
        assertTrue(p.isOpenContent());
        assertEquals(t, p.getType());
        assertFalse(t.isDataType());
        assertTrue(p.isContainment());
        assertFalse(p.isMany());
        assertNull(((PropertyXML)p).getXMLNamespaceURI());
        StringWriter out = new StringWriter();
        XMLDocument doc = xmlHelper.createDocument(a, "http://sdo/test/opencontent", "a");
        doc.setXMLDeclaration(false);
        xmlHelper.save(doc, out, new Options().setSavePrettyPrint());
        String exp =
            "<open:a xmlns:open=\"http://sdo/test/opencontent\">" + newline +
            "    <open:name>My open content object</open:name>" + newline +
            "    <o xsi:type=\"glob:SimpleType\" xmlns:glob=\"http://sdo/test/global\" xmlns:xsi=\"" + XSI_URI + "\">" + newline +
            "        <glob:x>Extra! Extra!</glob:x>" + newline +
            "        <glob:y>1000000</glob:y>" + newline +
            "    </o>" + newline +
            "</open:a>";
        assertEquals(exp, out.toString());
    }

    // Object is a DataObject, contained
    public void testSetOnDemand4() throws Exception
    {
        System.out.println("testSetOnDemand4()");
        DataObject a = createOpenA();
        assertTrue(a.getType().isOpen());
        Type t = typeHelper.getType("http://sdo/test/global", "SimpleType");
        DataObject test = factory.create(t); //("http://sdo/test/global", "SimpleType");
        test.set("x", "Extra! Extra!");
        test.set("y", 1000000);
        assertNull(test.getContainer());
        a.set("o", test);
        assertTrue(a == test.getContainer());
        a.set("r", test);
        // containment relation has not changed
        assertTrue(a == test.getContainer());
        Property p = a.getInstanceProperty("r");
        assertTrue(p.isOpenContent());
        assertEquals(t, p.getType());
        assertFalse(t.isDataType());
        assertFalse(p.isContainment()); // "r" is a non-containment reference
        assertFalse(p.isMany());
        assertNull(((PropertyXML)p).getXMLNamespaceURI());
        StringWriter out = new StringWriter();
        XMLDocument doc = xmlHelper.createDocument(a, "http://sdo/test/opencontent", "a");
        doc.setXMLDeclaration(false);
        xmlHelper.save(doc, out, new Options().setSavePrettyPrint());
        String exp =
            "<open:a xmlns:open=\"http://sdo/test/opencontent\">" + newline +
            "    <open:name>My open content object</open:name>" + newline +
            "    <o xsi:type=\"glob:SimpleType\" xmlns:glob=\"http://sdo/test/global\" xmlns:xsi=\"" + XSI_URI + "\">" + newline +
            "        <glob:x>Extra! Extra!</glob:x>" + newline +
            "        <glob:y>1000000</glob:y>" + newline +
            "    </o>" + newline +
            "    <r>#/open:a/o</r>" + newline +
            "</open:a>";
        assertEquals(exp, out.toString());
    }

    // Object is a List, homogeneous contents
    public void testSetOnDemand5() throws Exception
    {
        System.out.println("testSetOnDemand5()");
        DataObject a = createOpenA();
        List l = new ArrayList();
        l.add(new Integer(1));
        l.add(new Integer(2));
        l.add(new Integer(3));
        a.set("o", l);
        Property p = a.getInstanceProperty("o");
        assertTrue(p.isOpenContent());
        //assertEquals(intObjectType, p.getType()); // would be better if it were intType; but this needs spec amendment/clarification
        assertEquals(intType, p.getType());
        assertFalse(p.isContainment());
        assertTrue(p.isMany());
        assertNull(((PropertyXML)p).getXMLNamespaceURI());
        StringWriter out = new StringWriter();
        XMLDocument doc = xmlHelper.createDocument(a, "http://sdo/test/opencontent", "a");
        doc.setXMLDeclaration(false);
        xmlHelper.save(doc, out, new Options().setSavePrettyPrint());
        String exp =
            "<open:a xmlns:open=\"http://sdo/test/opencontent\">" + newline +
            "    <open:name>My open content object</open:name>" + newline +
            "    <o xsi:type=\"xs:int\" xmlns:xs=\"" + XSD_URI + "\" xmlns:xsi=\"" + XSI_URI + "\">1</o>" + newline +
            "    <o xsi:type=\"xs:int\" xmlns:xs=\"" + XSD_URI + "\" xmlns:xsi=\"" + XSI_URI + "\">2</o>" + newline +
            "    <o xsi:type=\"xs:int\" xmlns:xs=\"" + XSD_URI + "\" xmlns:xsi=\"" + XSI_URI + "\">3</o>" + newline +
            "</open:a>";
        assertEquals(exp, out.toString());
    }

    // Object is a List, heterogeneous contents (simple types)
    public void testSetOnDemand6() throws Exception
    {
        System.out.println("testSetOnDemand6()");
        DataObject a = createOpenA();
        List l = new ArrayList();
        l.add(new Integer(1));
        l.add("two");
        l.add(new Date(0l));
        a.set("o", l);
        Property p = a.getInstanceProperty("o");
        assertTrue(p.isOpenContent());
        assertEquals(objectType, p.getType());
        assertFalse(p.isContainment());
        assertTrue(p.isMany());
        assertNull(((PropertyXML)p).getXMLNamespaceURI());
        StringWriter out = new StringWriter();
        XMLDocument doc = xmlHelper.createDocument(a, "http://sdo/test/opencontent", "a");
        doc.setXMLDeclaration(false);
        xmlHelper.save(doc, out, new Options().setSavePrettyPrint());
        String exp =
            "<open:a xmlns:open=\"http://sdo/test/opencontent\">" + newline +
            "    <open:name>My open content object</open:name>" + newline +
            "    <o xsi:type=\"xs:int\" xmlns:xs=\"" + XSD_URI + "\" xmlns:xsi=\"" + XSI_URI + "\">1</o>" + newline +
            "    <o>two</o>" + newline +
            "    <o xsi:type=\"xs:dateTime\" xmlns:xs=\"" + XSD_URI + "\" xmlns:xsi=\"" + XSI_URI + "\">1970-01-01T00:00:00Z</o>" + newline +
            "</open:a>";
        assertEquals(exp, out.toString());
    }

    // Object is a Date
    public void testSetOnDemand7() throws Exception
    {
        System.out.println("testSetOnDemand7()");
        Date dt = new Date(0l);
        System.out.println(dt.toString());
        DataObject a = createOpenA();
        a.set("o", dt);
        Property p = a.getInstanceProperty("o");
        assertTrue(p.isOpenContent());
        assertEquals(dateType, p.getType());
        assertFalse(p.isContainment());
        assertFalse(p.isMany());
        assertNull(((PropertyXML)p).getXMLNamespaceURI());
        assertTrue(((PropertyXML)p).isXMLElement());
        StringWriter out = new StringWriter();
        XMLDocument doc = xmlHelper.createDocument(a, "http://sdo/test/opencontent", "a");
        doc.setXMLDeclaration(false);
        xmlHelper.save(doc, out, new Options().setSavePrettyPrint());
        String exp =
            "<open:a xmlns:open=\"http://sdo/test/opencontent\">" + newline +
            "    <open:name>My open content object</open:name>" + newline +
            "    <o xsi:type=\"xs:dateTime\" xmlns:xs=\"" + XSD_URI + "\" xmlns:xsi=\"" + XSI_URI + "\">1970-01-01T00:00:00Z</o>" + newline +
            "</open:a>";
        assertEquals(exp, out.toString());
    }

    // Object is a List, homogeneous (typed DataObject)
    public void testSetOnDemand8() throws Exception
    {
        System.out.println("testSetOnDemand8()");
        DataObject a = createOpenA();
        assertTrue(a.getType().isOpen());
        Type t = typeHelper.getType("http://sdo/test/global", "SimpleType");
        DataObject test1 = factory.create(t);
        test1.set("x", "Extra! Extra!");
        test1.set("y", 1000000);
        DataObject test2 = factory.create(t);
        test2.set("x", "Read all about it!");
        test2.set("y", 2000000);
        List l = new ArrayList();
        l.add(test1);
        l.add(test2);
        a.set("o", l);
        Property p = a.getInstanceProperty("o");
        assertTrue(p.isOpenContent());
        assertEquals(t, p.getType());
        assertTrue(p.isContainment());
        assertTrue(p.isMany());
        assertNull(((PropertyXML)p).getXMLNamespaceURI());
        StringWriter out = new StringWriter();
        XMLDocument doc = xmlHelper.createDocument(a, "http://sdo/test/opencontent", "a");
        doc.setXMLDeclaration(false);
        xmlHelper.save(doc, out, new Options().setSavePrettyPrint());
        String exp =
            "<open:a xmlns:open=\"http://sdo/test/opencontent\">" + newline +
            "    <open:name>My open content object</open:name>" + newline +
            "    <o xsi:type=\"glob:SimpleType\" xmlns:glob=\"http://sdo/test/global\" xmlns:xsi=\"" + XSI_URI + "\">" + newline +
            "        <glob:x>Extra! Extra!</glob:x>" + newline +
            "        <glob:y>1000000</glob:y>" + newline +
            "    </o>" + newline +
            "    <o xsi:type=\"glob:SimpleType\" xmlns:glob=\"http://sdo/test/global\" xmlns:xsi=\"" + XSI_URI + "\">" + newline +
            "        <glob:x>Read all about it!</glob:x>" + newline +
            "        <glob:y>2000000</glob:y>" + newline +
            "    </o>" + newline +
            "</open:a>";
        assertEquals(exp, out.toString());
    }

    // Object is a List, heterogeneous (includes DataObject)
    public void testSetOnDemand9() throws Exception
    {
        // property type is T_DATAOBJECT
        System.out.println("testSetOnDemand9()");
        DataObject a = createOpenA();
        assertTrue(a.getType().isOpen());
        Type t = typeHelper.getType("http://sdo/test/global", "SimpleType");
        DataObject test = factory.create(t);
        test.set("x", "Extra! Extra!");
        test.set("y", 1000000);
        List l = new ArrayList();
        l.add(test);
        l.add(new Integer(1));
        l.add("xxx");
        a.set("o", l);
        Property p = a.getInstanceProperty("o");
        assertTrue(p.isOpenContent());
        assertEquals(T_DATAOBJECT, p.getType());
        assertTrue(p.isContainment());
        assertTrue(p.isMany());
        assertNull(((PropertyXML)p).getXMLNamespaceURI());
        StringWriter out = new StringWriter();
        XMLDocument doc = xmlHelper.createDocument(a, "http://sdo/test/opencontent", "a");
        doc.setXMLDeclaration(false);
        xmlHelper.save(doc, out, new Options().setSavePrettyPrint());
        String exp =
            "<open:a xmlns:open=\"http://sdo/test/opencontent\">" + newline +
            "    <open:name>My open content object</open:name>" + newline +
            "    <o xsi:type=\"glob:SimpleType\" xmlns:glob=\"http://sdo/test/global\" xmlns:xsi=\"" + XSI_URI + "\">" + newline +
            "        <glob:x>Extra! Extra!</glob:x>" + newline +
            "        <glob:y>1000000</glob:y>" + newline +
            "    </o>" + newline +
            "    <o xsi:type=\"xs:int\" xmlns:xs=\"" + XSD_URI + "\" xmlns:xsi=\"" + XSI_URI + "\">1</o>" + newline +
            "    <o xsi:type=\"xs:string\" xmlns:xs=\"" + XSD_URI + "\" xmlns:xsi=\"" + XSI_URI + "\">xxx</o>" + newline +
            "</open:a>";
        assertEquals(exp, out.toString());
    }

    public void testSetOnDemand9b() throws Exception
    {
        System.out.println("testSetOnDemand9b()");
        DataObject a = createOpenA();
        assertTrue(a.getType().isOpen());
        Type t = typeHelper.getType("http://sdo/test/global", "SimpleType");
        DataObject test = factory.create(t);
        test.set("x", "Extra! Extra!");
        test.set("y", 1000000);
        List l = new ArrayList();
        l.add(new Integer(1));
        l.add(test);
        l.add("xxx");
        a.set("o", l);
        Property p = a.getInstanceProperty("o");
        assertTrue(p.isOpenContent());
        assertEquals(T_DATAOBJECT, p.getType());
        assertTrue(p.isContainment());
        assertTrue(p.isMany());
        assertNull(((PropertyXML)p).getXMLNamespaceURI());
        StringWriter out = new StringWriter();
        XMLDocument doc = xmlHelper.createDocument(a, "http://sdo/test/opencontent", "a");
        doc.setXMLDeclaration(false);
        xmlHelper.save(doc, out, new Options().setSavePrettyPrint());
        String exp =
            "<open:a xmlns:open=\"http://sdo/test/opencontent\">" + newline +
            "    <open:name>My open content object</open:name>" + newline +
            "    <o xsi:type=\"xs:int\" xmlns:xs=\"" + XSD_URI + "\" xmlns:xsi=\"" + XSI_URI + "\">1</o>" + newline +
            "    <o xsi:type=\"glob:SimpleType\" xmlns:glob=\"http://sdo/test/global\" xmlns:xsi=\"" + XSI_URI + "\">" + newline +
            "        <glob:x>Extra! Extra!</glob:x>" + newline +
            "        <glob:y>1000000</glob:y>" + newline +
            "    </o>" + newline +
            "    <o xsi:type=\"xs:string\" xmlns:xs=\"" + XSD_URI + "\" xmlns:xsi=\"" + XSI_URI + "\">xxx</o>" + newline +
            "</open:a>";
        assertEquals(exp, out.toString());
    }

    // Object is an empty List -> no property is created
    public void testSetOnDemand10() throws Exception
    {
        System.out.println("testSetOnDemand10()");
        DataObject a = createOpenA();
        a.set("o", new ArrayList());
        assertFalse(a.isSet("o"));
        assertNull(a.getInstanceProperty("o"));
    }

    // Object is a List
    // test altering live list
    public void testSetOnDemand11() throws Exception
    {
        System.out.println("testSetOnDemand11()");
        DataObject a = createOpenA();
        List l = new ArrayList();
        l.add(new Integer(1));
        l.add(new Integer(2));
        l.add(new Integer(3));
        a.set("o", l);
        Property p = a.getInstanceProperty("o");
        assertTrue(p.isOpenContent());
        //assertEquals(intObjectType, p.getType());
        assertEquals(intType, p.getType());
        assertFalse(p.isContainment());
        assertTrue(p.isMany());
        assertNull(((PropertyXML)p).getXMLNamespaceURI());
        assertTrue(((PropertyXML)p).isXMLElement());
        List ol = a.getList("o"); // this is a live list, l is not
        /* this doesn't work in general, not just for on-demand properties
        try
        {
            ol.add("string");
            fail("adding incompatible type should have failed");
        }
        catch (Exception e)
        {
            // check the exception
            e.printStackTrace();
        }
        */
        // clearing list unsets the property
        ol.clear();
        assertFalse(a.isSet("o"));
        assertFalse(a.isSet(p));
        // at this point, a no longer has an instance property "o"
        // is this correct?
        assertNull(a.getInstanceProperty("o"));
        // but does the open content property still exist in the types system?
        /* this fails for same reason as above
        try
        {
            ol.add("string");
            fail("adding incompatible type should have failed");
        }
        catch (Exception e)
        {
            // check the exception
            e.printStackTrace();
        }
        */
        //ol.add(new Integer(42));
        ol.add("xxx");
        assertTrue(a.isSet("o"));
        assertTrue(a.isSet(p));
        assertNotNull(a.getInstanceProperty("o"));
        assertTrue(p == a.getInstanceProperty("o"));
        System.out.println(p.getType()); // still "sdoType IntObject@javax.sdo/java"
        //assertEquals(42, a.getInt("o[1]"));
        StringWriter out = new StringWriter();
        XMLDocument doc = xmlHelper.createDocument(a, "http://sdo/test/opencontent", "a");
        doc.setXMLDeclaration(false);
        xmlHelper.save(doc, out, new Options().setSavePrettyPrint());
        System.out.println(out.toString());
    }

    // 2. getSequence() and add(String, Object) or add(int, String, Object)

    public void testAddToSequenceOnDemand1() throws Exception
    {
        System.out.println("testAddToSequenceOnDemand1()");
        String uri = "http://sdo/test/opencontent";
        Type t0 = typeHelper.getType(uri, "E");
        assertTrue(t0.isOpen());
        assertTrue(t0.isSequenced());
        DataObject root = factory.create(t0);
        Sequence seq = root.getSequence();
        seq.add("o", "xxx");
        Property p = root.getInstanceProperty("o");
        assertNotNull(p);
        assertTrue(p.isOpenContent());
        assertEquals(stringType, p.getType());
        assertFalse(p.isContainment());
        assertTrue(p.isMany());
        assertNull(((PropertyXML)p).getXMLNamespaceURI());
        assertTrue(((PropertyXML)p).isXMLElement());
        StringWriter out = new StringWriter();
        XMLDocument doc = xmlHelper.createDocument(root, uri, "e");
        doc.setXMLDeclaration(false);
        xmlHelper.save(doc, out, new Options().setSavePrettyPrint());
        String exp =
            "<open:e xmlns:open=\"http://sdo/test/opencontent\">" + newline +
            "    <o>xxx</o>" + newline +
            "</open:e>";
        assertEquals(exp, out.toString());
    }

    public void testAddToSequenceOnDemand2() throws Exception
    {
        System.out.println("testAddToSequenceOnDemand2()");
        String uri = "http://sdo/test/opencontent";
        Type t0 = typeHelper.getType(uri, "E");
        DataObject root = factory.create(t0);
        Sequence seq = root.getSequence();
        seq.add("o", new Integer(0));
        Property p = root.getInstanceProperty("o");
        assertNotNull(p);
        assertTrue(p.isOpenContent());
        //assertEquals(intObjectType, p.getType());
        assertEquals(intType, p.getType());
        assertFalse(p.isContainment());
        assertTrue(p.isMany());
        assertNull(((PropertyXML)p).getXMLNamespaceURI());
        assertTrue(((PropertyXML)p).isXMLElement());
        StringWriter out = new StringWriter();
        XMLDocument doc = xmlHelper.createDocument(root, uri, "e");
        doc.setXMLDeclaration(false);
        xmlHelper.save(doc, out, new Options().setSavePrettyPrint());
        String exp =
            "<open:e xmlns:open=\"http://sdo/test/opencontent\">" + newline +
            "    <o xsi:type=\"xs:int\" xmlns:xs=\"" + XSD_URI + "\" xmlns:xsi=\"" + XSI_URI + "\">0</o>" + newline +
            "</open:e>";
        assertEquals(exp, out.toString());
    }

    public void testAddToSequenceOnDemand3() throws Exception
    {
        System.out.println("testAddToSequenceOnDemand3()");
        String uri = "http://sdo/test/opencontent";
        Type t0 = typeHelper.getType(uri, "E");
        DataObject root = factory.create(t0);
        Sequence seq = root.getSequence();
        Type t1 = typeHelper.getType("http://sdo/test/global", "SimpleType");
        DataObject test = factory.create(t1);
        test.set("x", "Extra! Extra!");
        test.set("y", 1000000);
        seq.add("symbol", "XXX");
        seq.add("number", 1000);
        seq.add("o", test);
        Property p = root.getInstanceProperty("o");
        assertNotNull(p);
        assertTrue(p.isOpenContent());
        assertEquals(t1, p.getType());
        assertTrue(p.isContainment());
        assertTrue(p.isMany());
        assertNull(((PropertyXML)p).getXMLNamespaceURI());
        StringWriter out = new StringWriter();
        XMLDocument doc = xmlHelper.createDocument(root, uri, "e");
        doc.setXMLDeclaration(false);
        xmlHelper.save(doc, out, new Options().setSavePrettyPrint());
        String exp =
            "<open:e xmlns:open=\"http://sdo/test/opencontent\">" + newline +
            "    <open:symbol>XXX</open:symbol>" + newline +
            "    <open:number>1000</open:number>" + newline +
            "    <o xsi:type=\"glob:SimpleType\" xmlns:glob=\"http://sdo/test/global\" xmlns:xsi=\"" + XSI_URI + "\">" + newline +
            "        <glob:x>Extra! Extra!</glob:x>" + newline +
            "        <glob:y>1000000</glob:y>" + newline +
            "    </o>" + newline +
            "</open:e>";
        assertEquals(exp, out.toString());
    }

    // 3. createDataObject

    // generic DataObject
    public void testCreateDataObjectOnDemand1() throws Exception
    {
        System.out.println("testCreateDataObjectOnDemand1()");
        DataObject a = createOpenA();
        DataObject test = a.createDataObject("t");
        Property p = a.getInstanceProperty("t");
        assertNotNull(p);
        assertTrue(p.isOpenContent());
        // type of property is SDO DataObject, type of value is BEA DataObject
        assertEquals(T_DATAOBJECT, p.getType());
        assertEquals(T_BEADATAOBJECT, test.getType());
        assertTrue(p.isContainment());
        assertFalse(p.isMany()); 
        assertNull(((PropertyXML)p).getXMLNamespaceURI());
        test.set("x", "Extra! Extra!");
        test.set("y", 1000000);
        StringWriter out = new StringWriter();
        XMLDocument doc = xmlHelper.createDocument(a, "http://sdo/test/opencontent", "a");
        doc.setXMLDeclaration(false);
        xmlHelper.save(doc, out, new Options().setSavePrettyPrint());
        String exp =
            "<open:a xmlns:open=\"http://sdo/test/opencontent\">" + newline +
            "    <open:name>My open content object</open:name>" + newline +
            "    <t>" +
            "<x>Extra! Extra!</x>" +
            "<y xsi:type=\"xs:int\" xmlns:xs=\"" + XSD_URI + "\" xmlns:xsi=\"" + XSI_URI + "\">1000000</y>" +
            "</t>" + newline +
            "</open:a>";
        assertEquals(exp, out.toString());
    }

    // typed DataObject
    public void testCreateDataObjectOnDemand2() throws Exception
    {
        System.out.println("testCreateDataObjectOnDemand2()");
        DataObject a = createOpenA();
        DataObject test = a.createDataObject("t", "http://sdo/test/global", "SimpleType");
        Type t1 = typeHelper.getType("http://sdo/test/global", "SimpleType");
        Property p = a.getInstanceProperty("t");
        assertNotNull(p);
        assertTrue(p.isOpenContent());
        assertEquals(t1, p.getType());
        assertTrue(p.isContainment());
        assertFalse(p.isMany());
        assertNull(((PropertyXML)p).getXMLNamespaceURI());
        test.set("x", "Extra! Extra!");
        test.set("y", 1000000);
        StringWriter out = new StringWriter();
        XMLDocument doc = xmlHelper.createDocument(a, "http://sdo/test/opencontent", "a");
        doc.setXMLDeclaration(false);
        xmlHelper.save(doc, out, new Options().setSavePrettyPrint());
        String exp =
            "<open:a xmlns:open=\"http://sdo/test/opencontent\">" + newline +
            "    <open:name>My open content object</open:name>" + newline +
            "    <t xsi:type=\"glob:SimpleType\" xmlns:glob=\"http://sdo/test/global\" xmlns:xsi=\"" + XSI_URI + "\">" + newline +
            "        <glob:x>Extra! Extra!</glob:x>" + newline +
            "        <glob:y>1000000</glob:y>" + newline +
            "    </t>" + newline +
            "</open:a>";
        assertEquals(exp, out.toString());
    }

    // 4. getList(String) and add to List

    public void testGetListOnDemand1() throws Exception
    {
        System.out.println("testGetListOnDemand1()");
        Type t = typeHelper.getType("http://sdo/test/opencontent", "D");
        assertTrue(t.isOpen());
        DataObject root = factory.create(t);
        root.set("name", "My open content object");
        root.set("number", 2);
        List openList = root.getList("t");
        assertNotNull(openList);
        assertEquals(0, openList.size());
        Type t1 = typeHelper.getType("http://sdo/test/global", "SimpleType");
        DataObject test1 = factory.create("http://sdo/test/global", "SimpleType");
        test1.set("x", "Extra! Extra!");
        test1.set("y", 1000000);
        openList.add(test1);
        Property p = root.getInstanceProperty("t");
        assertTrue(p.isOpenContent());
        //assertEquals(t1, p.getType()); // FAILS, is T_DATAOBJECT instead
        assertTrue(p.isContainment());
        assertTrue(p.isMany());
        assertNull(((PropertyXML)p).getXMLNamespaceURI());
        DataObject test2 = factory.create("http://sdo/test/global", "SimpleType");
        test2.set("x", "More! More!");
        test2.set("y", 2000000);
        openList.add(test2);
        StringWriter out = new StringWriter();
        XMLDocument doc = xmlHelper.createDocument(root, "http://sdo/test/opencontent", "d");
        doc.setXMLDeclaration(false);
        xmlHelper.save(doc, out, new Options().setSavePrettyPrint());
        assertEquals(OPEN_Dt, out.toString());
    }

    public void testGetListOnDemand1b() throws Exception
    {
        System.out.println("testGetListOnDemand1b()");
        Type t = typeHelper.getType("http://sdo/test/opencontent", "D");
        assertTrue(t.isOpen());
        DataObject root = factory.create(t);
        root.set("name", "My open content object");
        root.set("number", 2);
        List openList = root.getList("t");
        assertNotNull(openList);
        assertEquals(0, openList.size());
        Type t1 = typeHelper.getType("http://sdo/test/global", "SimpleType");
        DataObject test1 = factory.create("http://sdo/test/global", "SimpleType");
        test1.set("x", "Extra! Extra!");
        test1.set("y", 1000000);
        openList.add(test1);
        Property p = root.getInstanceProperty("t");
        assertTrue(p.isOpenContent());
        //assertEquals(t1, p.getType()); // FAILS, is T_DATAOBJECT instead
        assertTrue(p.isContainment());
        assertTrue(p.isMany());
        assertNull(((PropertyXML)p).getXMLNamespaceURI());
        openList.add("incompatible type");
        StringWriter out = new StringWriter();
        XMLDocument doc = xmlHelper.createDocument(root, "http://sdo/test/opencontent", "d");
        doc.setXMLDeclaration(false);
        xmlHelper.save(doc, out, new Options().setSavePrettyPrint());
        System.out.println(out.toString());
    }

    // unmarshalling xs:anyAttribute

    // attribute is known
    public void testUnmarshalKnownOpenContentAttribute()
    {
        System.out.println("testUnmarshalKnownOpenContentAttribute()");
        String xml_in =
            "<bas:o xmlns:bas=\"http://sdo/test/basic0\" " +
            "xmlns:glob=\"http://sdo/test/global\" glob:xxx=\"true\"/>";
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
        assertEquals(booleanType, p.getType());
        Object value = o.get(p);
        assertTrue(value instanceof Boolean);
        assertEquals(true, value);
    }

    // attribute is unknown, new property is generated on-the-fly
    public void testUnmarshalUnknownOpenContentAttribute()
    {
        String xml_in =
            "<bas:o xmlns:bas=\"http://sdo/test/basic0\" value=\"x\"/>";
        XMLDocument doc = xmlHelper.load(xml_in);
        DataObject o = doc.getRootObject();
        List props = o.getInstanceProperties();
        assertEquals(1, props.size());
        Property p = (Property)props.get(0);
        assertEquals("value", p.getName());
        assertFalse(((PropertyXML)p).isXMLElement());
        assertEquals("", ((PropertyXML)p).getXMLNamespaceURI());
        assertNull(typeHelper.getOpenContentProperty("", "value"));
        assertNull(xsdHelper.getGlobalProperty("", "value", false));
        assertFalse(p.isMany());
        assertEquals(stringType, p.getType());
        Object value = o.get(p);
        assertTrue(value instanceof String);
        assertEquals("x", value);
    }

    // unmarshalling xs:any

    // element is known
    public void testUnmarshalKnownOpenContentElement()
    {
        System.out.println("testUnmarshalKnownOpenContentElement()");
        String xml_in =
            "<bas:o xmlns:bas=\"http://sdo/test/basic0\">" +
            "<glob:size xmlns:glob=\"http://sdo/test/global\">12</glob:size>" +
            "</bas:o>";
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

    // element is unknown, property is generated on-the-fly

    // case A 
    // - element all occurences of which have simple type
    //   and any xsi:type specified is "invertible" from java instance class

    public void testUnmarshalUnknownOpenContentElementA()
    {
        String xml_in =
            "<bas:o xmlns:bas=\"http://sdo/test/basic0\">" +
            "<a>100</a>" +
            "<a xsi:type=\"xs:int\" "+
            "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
            "xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">100</a>" +
            "<a xsi:type=\"xs:double\" "+
            "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
            "xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">100</a>" +
            "</bas:o>";
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
        assertEquals(objectType, p.getType());
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

    // case B
    // - element some occurence of which has complex type
    //  or has xsi:type of "non-invertible" simple type

    public void testUnmarshalUnknownOpenContentElementB1()
    {
        String xml_in =
            "<bas:o xmlns:bas=\"http://sdo/test/basic0\">" +
            "<a xsi:type=\"xs:int\" "+
            "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
            "xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">100</a>" +
            "<a xsi:type=\"xs:string\" "+
            "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
            "xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">100</a>" +
            "</bas:o>";
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
        Object a1 = o.get("a[1]");
        assertTrue(a1 instanceof DataObject);
        assertEquals(T_WRAPPER, ((DataObject)a1).getType());
        assertEquals(intType, ((DataObject)a1).getInstanceProperty("value").getType());
        assertEquals(100, ((DataObject)a1).get("value"));
        Object a2 = o.get("a[2]");
        assertTrue(a2 instanceof DataObject);
        assertEquals(T_WRAPPER, ((DataObject)a2).getType());
        assertEquals(stringType, ((DataObject)a2).getInstanceProperty("value").getType());
        assertEquals("100", ((DataObject)a2).get("value"));
    }

    public void testUnmarshalUnknownOpenContentElementB2()
    {
        String xml_in =
            "<bas:o xmlns:bas=\"http://sdo/test/basic0\">" +
            "<a>100</a>" +
            "<a><b>x</b>y<c>z</c>/</a>" +
            "<a xsi:type=\"glob:SimpleType\" "+
            "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
            "xmlns:glob=\"http://sdo/test/global\">" +
            "<glob:x>Extra! Extra!</glob:x>" +
            "<glob:y>1000000</glob:y>" +
            "</a>" +
            "</bas:o>";
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
        assertEquals(objectType, ((DataObject)a1).getInstanceProperty("value").getType());
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
}
