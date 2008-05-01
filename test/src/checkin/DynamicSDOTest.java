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

import java.io.*;
import java.util.List;
import java.util.ArrayList;
import java.math.BigDecimal;

import javax.sdo.DataObject;
import javax.sdo.Type;
import javax.sdo.Property;
import javax.sdo.Sequence;
import javax.sdo.helper.TypeHelper;
import javax.sdo.helper.XMLHelper;
import javax.sdo.helper.XSDHelper;
import javax.sdo.helper.DataFactory;
import javax.sdo.helper.XMLDocument;

import davos.sdo.impl.type.BuiltInTypeSystem;
import davos.sdo.Options;
import davos.sdo.PropertyXML;

import junit.framework.Test;
import junit.framework.TestSuite;
import common.BaseTest;

/**
 * @author Wing Yew Poon
 */
public class DynamicSDOTest extends BaseTest
{
    public DynamicSDOTest(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        /*
        TestSuite suite = new TestSuite();
        suite.addTest(new DynamicSDOTest("testDynamicType4"));
        */
        // or
        TestSuite suite = new TestSuite(DynamicSDOTest.class);
        return suite;
    }

    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(suite());
    }

    public static File dir;
    static
    {
        dir = new File(OUTPUTROOT + S + "checkin");
        dir.mkdirs();
    }

    private static final String CUST_URI = "http://www.example.com/customer";
    private static final String SIMPLE_URI = "http://www.example.com/simple2";
    private static final String TEST_URI = "http://sdo/test/dynamic";
    private static final String CUST_XML =
        "<cus:customer xsi:type=\"cus:Customer\" custNum=\"714\" firstName=\"John\" lastName=\"Smith\" " +
            "xmlns:cus=\"http://www.example.com/customer\" " +
            "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"/>" + newline;

    private static TypeHelper typeHelper = context.getTypeHelper();
    private static XMLHelper xmlHelper = context.getXMLHelper();
    private static XSDHelper xsdHelper = context.getXSDHelper();
    private static DataFactory factory = context.getDataFactory();

    private void loadXSDTypes(String dir, String xsdFile) throws Exception
    {
        InputStream resourceStream = 
            getResourceAsStream(dir, xsdFile);
        File resourceFile = getResourceFile(dir, xsdFile);
        List<Type> types = 
            xsdHelper.define(resourceStream, 
                             resourceFile.toURL().toString());
        resourceStream.close();
    }

    private XMLDocument loadXML(String dir, String xmlFile) throws Exception
    {
        File f = getResourceFile(dir, xmlFile);
        InputStream in = new FileInputStream(f);
        XMLDocument doc = xmlHelper.load(in);
        in.close();
        return doc;
    }

    /** define a type dynamically and then use it to create a data object;
        use dynamic API to set the properties */
    public void testDynamicType1() throws Exception
    {
        Type intType = typeHelper.getType("commonj.sdo", "Int");
        Type stringType = typeHelper.getType("commonj.sdo", "String");
        Type typeType = typeHelper.getType("commonj.sdo", "Type");

        // create a new Type for customers
        DataObject customerType = factory.create(typeType);
        customerType.set("uri", CUST_URI);
        customerType.set("name", "Customer");

        // create a customer number property
        DataObject custNumProperty = customerType.createDataObject("property");
        custNumProperty.set("name", "custNum");
        custNumProperty.set("type", intType);

        // create a first name property
        DataObject firstNameProperty = customerType.createDataObject("property");
        firstNameProperty.set("name", "firstName");
        firstNameProperty.set("type", stringType);

        // create a last name property
        DataObject lastNameProperty = customerType.createDataObject("property");
        lastNameProperty.set("name", "lastName");
        lastNameProperty.set("type", stringType);
        lastNameProperty.set("containment", Boolean.TRUE); // this has no effect if the type is a data type

        // now define the Customer type so that customers can be made
        Type t = typeHelper.define(customerType);
        Property p1 = t.getProperty("custNum");
        assertTrue(p1.getType().isDataType());
        assertEquals(intType, p1.getType());
        assertFalse(p1.isContainment());
        Property p2 = t.getProperty("firstName");
        assertTrue(p2.getType().isDataType());
        assertEquals(stringType, p2.getType());
        assertFalse(p2.isContainment());
        Property p3 = t.getProperty("lastName");
        assertTrue(p3.getType().isDataType());
        assertEquals(stringType, p3.getType());
        assertFalse(p3.isContainment());

        // create a customer instance
        DataObject customer = factory.create(t); //(CUST_URI, "Customer");
        customer.setInt("custNum", 714);
        customer.set("firstName", "John");
        customer.set("lastName", "Smith");

        List props = customer.getInstanceProperties();
        assertEquals(3, props.size());
        Property prop = (Property) props.get(0);
        assertEquals("custNum", prop.getName());
        assertEquals(714, customer.get(prop));
        prop = (Property) props.get(1);
        assertEquals("firstName", prop.getName());
        assertEquals("John", customer.get(prop));
        prop = (Property) props.get(2);
        assertEquals("lastName", prop.getName());
        assertEquals("Smith", customer.get(prop));

        // now save the customer instance
        File f = new File(dir, "customer.xml");
        OutputStream out = new FileOutputStream(f);
        XMLDocument doc = xmlHelper.createDocument(customer, CUST_URI, "customer");
        doc.setXMLDeclaration(false);
        xmlHelper.save(doc, out, new Options().setSavePrettyPrint());
        out.close();

        // verify the saved file
        BufferedReader br = new BufferedReader(new FileReader(f));
        StringBuffer sb = new StringBuffer();
        String line = null;
        while ((line = br.readLine()) != null)
        {
            sb.append(line).append(newline);
        }
        assertEquals(CUST_XML, sb.toString());
        br.close();
    }

    /** load a type dynamically from xsd and use it to create a data object;
        use dynamic API to set the properties */
    public void testDynamicType2() throws Exception
    {
        loadXSDTypes("checkin", "simple2.xsd_");
        Type t = typeHelper.getType(SIMPLE_URI, "Quote");
        assertNotNull(t);
        DataObject quote = factory.create(t);
        
        quote.setString("symbol", "fbnt");
        quote.setString("companyName", "FlyByNightTechnology");
        quote.setBigDecimal("price", new BigDecimal("1000.0"));
        quote.setBigDecimal("open1", new BigDecimal("1000.0"));
        quote.setBigDecimal("high", new BigDecimal("2000.0"));
        quote.setBigDecimal("low", new BigDecimal("1000.0"));
        quote.setDouble("volume", 1000);
        quote.setDouble("change1", 1000);

        DataObject child1 = quote.createDataObject("quotes");
        child1.setBigDecimal("price", new BigDecimal("1000.0"));
        DataObject child2 = factory.create(t);
        child2.setBigDecimal("price", new BigDecimal("1500.0"));
        List quotes = quote.getList("quotes");
        assertNotNull(quotes);
        assertEquals(1, quotes.size());
        assertEquals(child1, quotes.get(0));
        quotes.add(child2);
        DataObject child3 = quote.createDataObject("quotes");
        child3.setBigDecimal("price", new BigDecimal("2000.0"));
        assertEquals(3, quotes.size());
        assertEquals(child3, quotes.get(2));

        File f = new File(dir, "quote.xml");
        OutputStream out = new FileOutputStream(f);
        xmlHelper.save(xmlHelper.createDocument(quote, SIMPLE_URI, "quote"), out, new Options().setSavePrettyPrint());
        out.close();
        compareXMLFiles(getResourceFile("checkin", "simple2.xml"), f, STRICT);
    }

    /** define a type dynamically and then use it to create a data object;
        define types with containment properties so that we can create nested
        data objects; use dynamic API to set the properties */
    public void testDynamicType3() throws Exception
    {
        // define the types to be used
        Type stringType = typeHelper.getType("commonj.sdo", "String");
        Type typeType = typeHelper.getType("commonj.sdo", "Type");
        DataObject phonePrototype = factory.create(typeType);
        phonePrototype.set("uri", TEST_URI);
        phonePrototype.set("name", "PhoneType");
        DataObject locationProperty = phonePrototype.createDataObject("property");
        locationProperty.set("name", "location");
        locationProperty.set("type", stringType);
        DataObject numberProperty = phonePrototype.createDataObject("property");
        numberProperty.set("name", "number");
        numberProperty.set("type", stringType);
        Type phoneType = typeHelper.define(phonePrototype);
        DataObject personPrototype = factory.create(typeType);
        personPrototype.set("uri", TEST_URI);
        personPrototype.set("name", "PersonType");
        DataObject nameProperty = personPrototype.createDataObject("property");
        nameProperty.set("name", "name");
        nameProperty.set("type", stringType);
        DataObject phoneProperty = personPrototype.createDataObject("property");
        phoneProperty.set("name", "phone");
        phoneProperty.set("type", phoneType);
        phoneProperty.set("containment", Boolean.TRUE);
        phoneProperty.set("many", Boolean.TRUE);
        Type personType = typeHelper.define(personPrototype);
        // the type with the changeSummary property
        DataObject directoryPrototype = factory.create(typeType);
        directoryPrototype.set("uri", TEST_URI);
        directoryPrototype.set("name", "DirectoryType");
        DataObject listingProperty = directoryPrototype.createDataObject("property");
        listingProperty.set("name", "listing");
        listingProperty.set("type", personType);
        listingProperty.set("containment", Boolean.TRUE);
        listingProperty.set("many", Boolean.TRUE);
        Type directoryType = typeHelper.define(directoryPrototype);

        Property p11 = personType.getProperty("name");
        assertTrue(p11.getType().isDataType());
        assertFalse(p11.isContainment());
        Property p12 = personType.getProperty("phone");
        assertFalse(p12.getType().isDataType());
        assertTrue(p12.isContainment());

        // now we can create the data objects
        DataObject directory = factory.create(directoryType);
        DataObject phone1 = factory.create(phoneType);
        phone1.set("location", "work");
        phone1.set("number", "650-234-7701");
        DataObject phone2 = factory.create(phoneType);
        phone2.set("location", "home");
        phone2.set("number", "650-927-4321");
        DataObject bob = factory.create(personType);
        bob.set("name", "Robert Smith");
        List phones = new ArrayList();
        phones.add(phone1);
        phones.add(phone2);
        bob.set("phone", phones);
        List listings = new ArrayList();
        listings.add(bob);
        directory.set("listing", listings);

        // save and verify
        File f = new File(dir, "directory.xml");
        OutputStream out = new FileOutputStream(f);
        xmlHelper.save(xmlHelper.createDocument(directory, TEST_URI, "directory"), out, new Options().setSavePrettyPrint());
        out.close();
        compareXMLFiles(getResourceFile("checkin", "directory.xml"), f, STRICT);

        // test schema generation
        List typeList = new ArrayList(3);
        typeList.add(phoneType);
        typeList.add(personType);
        typeList.add(directoryType);
        String xsd = xsdHelper.generate(typeList);
        System.out.println("  xsd: " + xsd);
        f = new File(dir, "directory.xsd");
        Writer wr = new FileWriter(f);
        wr.write(xsd);
        wr.close();
        compareXMLFiles(getResourceFile("checkin", "directory.xsd_"), f, STRICT);
    }

    /** load a type dynamically from xsd and use it to unmarshal an xml 
        instance */
    public void testDynamicType4() throws Exception
    {
        loadXSDTypes("marshal", "prod_ns.xsd");
        Type t1 = typeHelper.getType("http://example.com/prod", "ProductType");
        assertNotNull(t1);
        XMLDocument doc = loadXML("marshal", "prod_1.xml");
        DataObject root = doc.getRootObject();
        assertEquals(t1, root.getType());
        doc = loadXML("marshal", "prod_2.xml");
        root = doc.getRootObject();
        assertEquals(t1, root.getType());
        doc = loadXML("marshal", "prod_ns_1.xml");
        root = doc.getRootObject();
        assertEquals(t1, root.getType());

        loadXSDTypes("marshal", "prod_nons.xsd");
        Type t2 = typeHelper.getType("", "ProductType");
        assertNotNull(t2);
        doc = loadXML("marshal", "prod_0.xml");
        root = doc.getRootObject();
        assertEquals(t2, root.getType());
        doc = loadXML("marshal", "prod_nons_1.xml");
        root = doc.getRootObject();
        assertEquals(t2, root.getType());
    }

    public void testMetadataOnTypeAndProperty()
    {
        final String OPEN_PROP_NAME = "myOpenProp";
        final String OPEN_PROP_URI  = "myOpenProp_Uri";
        DataObject openPropDefinition = factory.create("commonj.sdo", "Property");
        openPropDefinition.set(BuiltInTypeSystem.P_PROPERTY_NAME, OPEN_PROP_NAME);
        openPropDefinition.set(BuiltInTypeSystem.P_PROPERTY_TYPE, BuiltInTypeSystem.STRING);
        Property openProperty = typeHelper.defineOpenContentProperty(OPEN_PROP_URI, openPropDefinition);

        // Create a new Type and with an open content property set
        DataObject myTypeDefinition = factory.create("commonj.sdo", "Type");
        myTypeDefinition.set("name", "MyType");
        myTypeDefinition.set("uri", "uri");

        Property openContentProperty = typeHelper.getOpenContentProperty(OPEN_PROP_URI, OPEN_PROP_NAME);
        myTypeDefinition.set(openContentProperty, "myValueOnType");

        DataObject myPropDefinition = myTypeDefinition.createDataObject("property");
        myPropDefinition.set("name", "myProperty");
        myPropDefinition.set("type", BuiltInTypeSystem.STRING);
        myPropDefinition.set(openContentProperty, "myValueOnProperty");

        // Define the Type
        Type definedType = typeHelper.define(myTypeDefinition);

        //System.out.println(" value on Type    : " + definedType.get(openContentProperty));
        assertEquals("myValueOnType", definedType.get(openContentProperty));

        Property p = definedType.getProperty("myProperty");
        //System.out.println(" value on Property: " + p.get(openContentProperty));
        assertEquals("myValueOnProperty", p.get(openContentProperty));
    }

    public static class MyBoolean
    {
        boolean val;
        public MyBoolean(String text)
        {
            if (text!=null && "myTrue".equals(text))
                val = true;
            else
                val = false;
        }

        public String toString()
        {
            return val ? "myTrue" : "myFalse";
        }
    }

    public void testXmlElementAndJavaClass()
    {
        //System.out.println("  " + this.getClass().getName() + ".testXmlElementAndJavaClass()");
        String uri = "any_unique_uri";
        // Create a new Type and with an open content property set
        DataObject myTypeDefinition = factory.create("commonj.sdo", "Type");
        myTypeDefinition.set("name", "MyType2");
        myTypeDefinition.set("uri", uri);

        DataObject myPropDefinition = myTypeDefinition.createDataObject("property");
        myPropDefinition.set("name", "myProperty");
        myPropDefinition.set("type", BuiltInTypeSystem.STRING);
        
        DataObject myType2Definition = factory.create("commonj.sdo", "Type");
        myType2Definition.set("name", "MyBoolean");
        myType2Definition.set("uri", uri);
        myType2Definition.set("dataType", true);
        myType2Definition.set(BuiltInTypeSystem.P_TYPE_JAVACLASS, MyBoolean.class.getName());

        myPropDefinition = myTypeDefinition.createDataObject("property");
        myPropDefinition.set("name", "myBoolean");
        myPropDefinition.set("type", myType2Definition);
        myPropDefinition.set(BuiltInTypeSystem.P_PROPERTY_XMLELEMENT, false);

        // Define the Type
        Type definedType = typeHelper.define(myTypeDefinition);

        Property p = definedType.getProperty("myBoolean");

        //System.out.println("    prop myBoolean - instProp xmlElement: " + p.get(BuiltInTypeSystem.P_PROPERTY_XMLELEMENT));
        assertEquals(false, p.get(BuiltInTypeSystem.P_PROPERTY_XMLELEMENT));

        //System.out.println("    prop myBoolean - type: " + p.getType());
        assertEquals("MyBoolean", p.getType().getName());
        assertEquals(uri, p.getType().getURI());

        //System.out.println("    prop myBoolean - getType().getInstanceClass(): " + p.getType().getInstanceClass());
        assertEquals(MyBoolean.class, p.getType().getInstanceClass());

        //System.out.println("    prop myBoolean - getType().get(JAVACLASS): " + p.getType().get(BuiltInTypeSystem.P_TYPE_JAVACLASS));
        assertEquals(MyBoolean.class.getName(), p.getType().get(BuiltInTypeSystem.P_TYPE_JAVACLASS));

        DataObject instance = factory.create(definedType);
        instance.setString("myBoolean", "myTrue");
        MyBoolean myBool = (MyBoolean)instance.get("myBoolean");
        //System.out.println("    myBool:" + myBool );
        assertEquals("myTrue", "" + myBool);
    }

    public void testCycle()
    {
        DataObject dobj = xmlHelper.load("<a><b><c><d/></c></b></a>").getRootObject();
        DataObject b = dobj.getDataObject("b.0");
        DataObject c = b.getDataObject("c.0");
        try
        {
            c.createDataObject("bb");
            c.set("bb", b);
            fail("set should have failed due to circular containment");
        }
        catch (Exception e)
        {
            e.printStackTrace();
            assertTrue(e instanceof IllegalArgumentException);
            String msg = e.getMessage();
            assertTrue((msg.indexOf("Circular containment") >= 0) ||
                       (msg.indexOf("circular containment") >= 0));
        }
    }

    static final String PO_URI = "po_uri";

    private static Type getDynamicPOType()
    {
        Type intType = typeHelper.getType("commonj.sdo", "Int");
        Type stringType = typeHelper.getType("commonj.sdo", "String");

        // create a new Type for PurchaseOrder
        DataObject poTypeDescriptor = factory.create("commonj.sdo", "Type");
        poTypeDescriptor.set("uri", PO_URI);
        poTypeDescriptor.set("name", "PurchaseOrder");
//        poTypeDescriptor.setBoolean("dataType", false);
        poTypeDescriptor.setBoolean("open", true);
        poTypeDescriptor.setBoolean("sequenced", true);
//        poTypeDescriptor.setBoolean("abstract", false);

        DataObject itemNumProperty = poTypeDescriptor.createDataObject("property");
        itemNumProperty.set("name", "itemNo");
        itemNumProperty.set("type", intType);
        itemNumProperty.setBoolean("many", true);
//        itemNumProperty.setBoolean("containment", false);
//        itemNumProperty.setBoolean("readOnly", false);
//        itemNumProperty.set("default", null);

        DataObject itemNameProperty = poTypeDescriptor.createDataObject("property");
        itemNameProperty.set("name", "itemName");
        itemNameProperty.set("type", stringType);
        itemNameProperty.setBoolean("many", true);
//        itemNameProperty.setBoolean("containment", false);
//        itemNameProperty.setBoolean("readOnly", false);
//        itemNameProperty.set("default", null);


        //System.out.println("Printig dataObject poTypeDescriptor:");
        //printDO(poTypeDescriptor);

        Type t = typeHelper.define(poTypeDescriptor);

        //System.out.println("\nt.dump()");
        //((TypeImpl)t).dump();

        return t;
    }

    public void testGlobalProperty()
    {
        String openPropUri = "someUri";
        String openPropDecimalName = "openPropDecimalName";
        DataObject openPropDecimalDefinition = factory.create("commonj.sdo", "Property");
        openPropDecimalDefinition.set("type", typeHelper.getType("commonj.sdo", "Decimal"));
        openPropDecimalDefinition.set("name", openPropDecimalName);
        Property openPropDecimal = typeHelper.defineOpenContentProperty(openPropUri, openPropDecimalDefinition);

        DataObject propDoubleDefinition = factory.create("commonj.sdo", "Property");
        propDoubleDefinition.set("type", typeHelper.getType("commonj.sdo", "Double"));
        propDoubleDefinition.set("name", "doubleProp");
        Property openPropDouble = typeHelper.defineOpenContentProperty(openPropUri, propDoubleDefinition);


        Type openType = typeHelper.getType(PO_URI, "PurchaseOrder");
        if ( openType==null )
            openType = getDynamicPOType();

        // Set an instance property on an open type DataObject
        DataObject open = factory.create(openType);

        BigDecimal bigDec = new BigDecimal("1100.333333");
        open.setBigDecimal(openPropDecimal, bigDec);

        //System.out.println("\n  open.get('openPropDecimalName'):" + open.get(openPropDecimalName));
        assertEquals(bigDec, open.get(openPropDecimalName));

        //((TypeSystemBase) SDOContextFactory.getDefaultSDOTypeSystem()).dumpWithoutBuiltinTypes();

        Property ocpProp = typeHelper.getOpenContentProperty(openPropUri, openPropDecimalName);
        //System.out.println("Property: " + ocpProp);
        //((PropertyImpl)ocpProp).dump();
        assertEquals(openPropDecimal, ocpProp);


        Property xsdElemProp = xsdHelper.getGlobalProperty(openPropUri, openPropDecimalName, true);
        //System.out.println("XSDProperty elem: " + xsdElemProp);
        //((PropertyImpl)xsdElemProp).dump();
        assertEquals(openPropDecimal, xsdElemProp);

        Property xsdAttrProp = xsdHelper.getGlobalProperty(openPropUri, openPropDecimalName, false);
        //System.out.println("XSDProperty attr: " + xsdAttrProp);
        assertEquals(null, xsdAttrProp);

        //System.out.println("open.getString(openPropDecimal) : " + open.getString(openPropDecimal));
        assertEquals("1100.333333", open.getString(openPropDecimal));

        double value = 123.45678;

        open.setDouble(openPropDecimal, value);
        //System.out.println("open.getString(openPropDecimal) : " + open.getString(openPropDecimal));
        assertEquals("123.45678", open.getString(openPropDecimal));

        open.setDouble(openPropDouble, value);
        //System.out.println("open.getBigDecimal(openPropDouble) : " + open.getBigDecimal(openPropDouble));
        assertEquals(new BigDecimal("123.45678"), open.getBigDecimal(openPropDouble));

        open.setString("doubleProp", "987.654321");
        Object o = open.get(openPropDouble);
        //System.out.println("open.getBigDecimal(openPropDouble) : " + o.getClass() + "  " + o);
        assertEquals(Double.class, o.getClass());
        assertEquals(new Double("987.654321"), o);
    }

    public void testOnDemandProperty()
    {
        Type openType = typeHelper.getType(PO_URI, "PurchaseOrder");
        if ( openType==null )
            openType = getDynamicPOType();

        // Set an instance property on an open type DataObject
        DataObject open = factory.create(openType);
        //System.out.println("\nopen:");
        //printDO(open);

        //set
        //System.out.println("\nopen.set");
        open.set("freshNewOnDemandProp", "a value");
        Property p1 = open.getInstanceProperty("freshNewOnDemandProp");
        assertNotNull(p1);
        assertTrue(p1.isOpenContent());
        assertFalse(p1.isMany());
        assertTrue(((PropertyXML)p1).isXMLElement());
        //get
        //System.out.println("\nopen.get(\"freshNewOnDemandProp\"): " + open.get("freshNewOnDemandProp"));
        //printDO(open);
        assertEquals("a value", open.get("freshNewOnDemandProp"));

        //seq  - add + get
        //System.out.println("\n\nSequence operations");
        Sequence seq = open.getSequence();
        //System.out.println("  Sequence:");
        //for (int i=0; i<seq.size(); i++)
        //    System.out.println("    prop: " + seq.getProperty(i) + "  val:'" + seq.getValue(i) + "'");
        assertEquals(1, seq.size());
        assertEquals(p1, seq.getProperty(0));
        assertEquals("a value", seq.getValue(0));

        //System.out.println("\nseq.add");
        seq.add("anotherOnDemandProperty", "value-for-anotherOnDemandProperty");
        Property p2 = open.getInstanceProperty("anotherOnDemandProperty");
        assertNotNull(p2);
        assertTrue(p2.isOpenContent());
        assertTrue(p2.isMany());
        assertTrue(((PropertyXML)p2).isXMLElement());
        //System.out.println("  Sequence:");
        //for (int i=0; i<seq.size(); i++)
        //    System.out.println("    prop: " + seq.getProperty(i) + "  val:'" + seq.getValue(i) + "'");
        assertEquals(2, seq.size());
        assertEquals(p1, seq.getProperty(0));
        assertEquals("a value", seq.getValue(0));
        assertEquals(p2, seq.getProperty(1));
        assertEquals("value-for-anotherOnDemandProperty", seq.getValue(1));


        //System.out.println("\nseq.move");
        seq.move(0, 1);
        //System.out.println("  Sequence:");
        //for (int i=0; i<seq.size(); i++)
        //    System.out.println("    prop: " + seq.getProperty(i) + "  val:'" + seq.getValue(i) + "'");
        assertEquals(2, seq.size());
        assertEquals(p2, seq.getProperty(0));
        assertEquals("value-for-anotherOnDemandProperty", seq.getValue(0));
        assertEquals(p1, seq.getProperty(1));
        assertEquals("a value", seq.getValue(1));

        //System.out.println("\nseq.remove");
        seq.remove(0);
        //System.out.println("  Sequence:");
        //for (int i=0; i<seq.size(); i++)
        //    System.out.println("    prop: " + seq.getProperty(i) + "  val:'" + seq.getValue(i) + "'");
        assertEquals(1, seq.size());
        assertEquals(p1, seq.getProperty(0));
        assertEquals("a value", seq.getValue(0));
        assertNull(open.getInstanceProperty("anotherOnDemandProperty"));

        //System.out.println("\nseq.remove");
        seq.remove(0);
        //System.out.println("  Sequence:");
        //for (int i=0; i<seq.size(); i++)
        //    System.out.println("    prop: " + seq.getProperty(i) + "  val:'" + seq.getValue(i) + "'");
        assertEquals(0, seq.size());
        assertNull(open.getInstanceProperty("freshNewOnDemandProp"));


        //list - add + get
        //System.out.println("\n\nList operations");
        List l = open.getList("yetAnotherOnDemandProperty");
        //System.out.println("open.getList: " + l.size());
        //for (int i = 0; i < l.size(); i++)
        //    System.out.println("    " + i + ": '" + l.get(i) + "'");
        assertTrue(l!=null);
        assertEquals(0, l.size());

        //System.out.println("\n  l.add");
        l.add("value_of_yetAnotherOnDemandProperty");
        //System.out.println("open.getList: " + l.size());
        //for (int i = 0; i < l.size(); i++)
        //    System.out.println("    " + i + ": '" + l.get(i) + "'");
        assertEquals(1, l.size());
        assertEquals("value_of_yetAnotherOnDemandProperty", l.get(0));
        Property p3 = open.getInstanceProperty("yetAnotherOnDemandProperty");
        assertNotNull(p3);
        assertTrue(p3.isOpenContent());
        assertTrue(p3.isMany());
        assertTrue(((PropertyXML)p3).isXMLElement());

        //System.out.println("\nl.remove");
        l.remove("value_of_yetAnotherOnDemandProperty");
        //System.out.println("open.getList: " + l.size());
        //for (int i = 0; i < l.size(); i++)
        //    System.out.println("    " + i + ": '" + l.get(i) + "'");
        assertEquals(0, seq.size());
        assertFalse(open.isSet(p3));
    }

    public void testXsdGeneration()
    {
        DataObject typeObject = factory.create("commonj.sdo", "Type");
        typeObject.set("uri", "checkin.DynamicSDOTest.testXsdGeneration");
        typeObject.set("name", "Type");
        DataObject propertyObject = typeObject.createDataObject("property");
        propertyObject.set("name", "a");
        propertyObject.set("type", typeHelper.getType("commonj.sdo", "Int"));
        propertyObject = typeObject.createDataObject("property");
        propertyObject.set("name", "b");
        propertyObject.set("type", typeHelper.getType("commonj.sdo", "Int"));
        List<DataObject> typesToDefine = new ArrayList<DataObject>(1);
        typesToDefine.add(typeObject);
        List definedTypes = typeHelper.define(typesToDefine);
        propertyObject = factory.create("commonj.sdo", "Property");
        propertyObject.set("name", "root");
        propertyObject.set("type", definedTypes.get(0));
        propertyObject.set("containment", true);
        typeHelper.defineOpenContentProperty("checkin.DynamicSDOTest.testXsdGeneration",
            propertyObject);
        List<Type> typesToGenerate = new ArrayList<Type>(2);
        typesToGenerate.add(typeHelper.getType("checkin.DynamicSDOTest.testXsdGeneration", "Type"));
        typesToGenerate.add(typeHelper.getOpenContentProperty(
            "checkin.DynamicSDOTest.testXsdGeneration", "root").getContainingType());
        String schema = xsdHelper.generate(typesToGenerate);
        int i1, i2, i3;
        i1 = schema.indexOf("<xs:complexType");
        assertTrue(i1 > 0 && schema.indexOf("<xs:complexType", i1 + 1) < 0);
        i1 = schema.indexOf("<xs:element");
        i2 = schema.indexOf("/>", i1);
        i3 = schema.indexOf("type=", i1);
        boolean result = i1 > 0;
        result &= schema.indexOf("<xs:element>", i1 + 1) < 0;
        result &= i2 > 0;
        result &= i3 > 0;
        result &= i3 < i2;
        assertTrue(result);
    }
}
