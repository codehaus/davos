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
import davos.sdo.impl.type.BuiltInTypeSystem;
import davos.sdo.PropertyXML;

/**
 * @author Wing Yew Poon
 */
public class XSDHelperTest extends BaseTest
{
    public XSDHelperTest(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        
        TestSuite suite = new TestSuite();
        suite.addTest(new XSDHelperTest("testType"));
        suite.addTest(new XSDHelperTest("testProperty"));
        suite.addTest(new XSDHelperTest("testDefineAndGenerate"));
        suite.addTest(new XSDHelperTest("testDuplicateDefine"));
        suite.addTest(new XSDHelperTest("testGenerate"));
        suite.addTest(new XSDHelperTest("testGenerateComplexType"));
        suite.addTest(new XSDHelperTest("testGenerateSequenced1"));
        suite.addTest(new XSDHelperTest("testGenerateSequenced2"));
        suite.addTest(new XSDHelperTest("testGenerateNonSequenced"));
        suite.addTest(new XSDHelperTest("testGlobalProperty"));
        suite.addTest(new XSDHelperTest("testCompositeNames"));
        suite.addTest(new XSDHelperTest("testGenerateWithNSToSLMap"));
        suite.addTest(new XSDHelperTest("testAppinfo"));
        suite.addTest(new XSDHelperTest("testNoNamespace"));
        
        // or
        //TestSuite suite = new TestSuite(XSDHelperTest.class);
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

    private static DataFactory factory = context.getDataFactory();
    private static TypeHelper typeHelper = context.getTypeHelper();
    private static XSDHelper xsdHelper = context.getXSDHelper();
    private static Type simpleType;
    private static Type sequencedType;
    private static Type openNotSequencedType;
    static String testURI = "http://www.bea.com/test/xsd";
    static Type booleanType = typeHelper.getType("commonj.sdo", "Boolean");
    static Type intType = typeHelper.getType("commonj.sdo", "Int");
    static Type stringType = typeHelper.getType("commonj.sdo", "String");
    static Type timeType = typeHelper.getType("commonj.sdo", "Time");
    static
    {
        // initialize (define) simpleType
        DataObject prototype = factory.create("commonj.sdo", "Type");
        prototype.set("uri", testURI);
        prototype.set("name", "SimpleType");
        DataObject booleanProperty = prototype.createDataObject("property");
        booleanProperty.set("name", "boolean");
        booleanProperty.set("type", booleanType);
        DataObject intProperty = prototype.createDataObject("property");
        intProperty.set("name", "int");
        intProperty.set("type", intType);
        DataObject stringProperty = prototype.createDataObject("property");
        stringProperty.set("name", "string");
        stringProperty.set("type", stringType);
        DataObject timeProperty = prototype.createDataObject("property");
        timeProperty.set("name", "time");
        timeProperty.set("type", timeType);
        simpleType = typeHelper.define(prototype);

        // initialize (define) sequencedType
        DataObject prototype2 = factory.create("commonj.sdo", "Type");
        prototype2.set("uri", testURI);
        prototype2.set("name", "SequencedType");
        prototype2.setBoolean("sequenced", true);
        DataObject stringProperty1 = prototype2.createDataObject("property");
        stringProperty1.set("name", "s1");
        stringProperty1.set("type", stringType);
        DataObject stringProperty2 = prototype2.createDataObject("property");
        stringProperty2.set("name", "s2");
        stringProperty2.set("type", stringType);
        DataObject stringProperty3 = prototype2.createDataObject("property");
        stringProperty3.set("name", "s3");
        stringProperty3.set("type", stringType);
        sequencedType = typeHelper.define(prototype2);

        // initialize (define) open, but not sequenced, type
        DataObject prototype3 = factory.create("commonj.sdo", "Type");
        prototype3.set("uri", testURI);
        prototype3.set("name", "OpenNotSequencedType");
        prototype3.setBoolean("open", true);
        DataObject intProperty1 = prototype3.createDataObject("property");
        intProperty1.set("name", "i1");
        intProperty1.set("type", intType);
        openNotSequencedType = typeHelper.define(prototype3);

        // load annotations.xsd types
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

    public void testType()
    {
        Type t0 = typeHelper.getType("http://www.example.com/test", "Stuff");
        assertNotNull(t0); // xsd type (loaded using XSDHelper.define())
        Type t1 = typeHelper.getType("http://www.example.com/choice", "ShirtType"); // xsd type (loaded from compiled schema jar)
        assertNotNull(t1);
        Type t2a = typeHelper.getType("http://www.example.com/choice", "ItemsType"); // xsd type, sequenced but not mixed
        assertNotNull(t2a);
        Type t2b = typeHelper.getType("letter.xsd", "FormLetter"); // xsd type, mixed
        assertNotNull(t2b);
        Type t3 = simpleType; // dynamic type
        assertNotNull(t3);
        assertFalse(t3.isSequenced());
        Type t4 = sequencedType; // dynamic type, mixed
        assertNotNull(t4);
        assertTrue(t4.isSequenced());
        // is xsd
        assertTrue(xsdHelper.isXSD(t0));
        assertTrue(xsdHelper.isXSD(t1));
        assertTrue(xsdHelper.isXSD(t2a));
        assertTrue(xsdHelper.isXSD(t2b));
        assertFalse(xsdHelper.isXSD(t3));
        assertFalse(xsdHelper.isXSD(t4));
        // local name
        assertEquals("Items", xsdHelper.getLocalName(t0)); // name is "Stuff"
        assertEquals("ShirtType", xsdHelper.getLocalName(t1));
        assertEquals("ItemsType", xsdHelper.getLocalName(t2a));
        assertEquals("FormLetter", xsdHelper.getLocalName(t2b));
        assertEquals("SimpleType", xsdHelper.getLocalName(t3));
        assertEquals("SequencedType", xsdHelper.getLocalName(t4));
        // is mixed
        assertFalse(xsdHelper.isMixed(t1));
        assertFalse(xsdHelper.isMixed(t2a));
        assertTrue(xsdHelper.isMixed(t2b));
        assertFalse(xsdHelper.isMixed(t3));
        assertTrue(xsdHelper.isMixed(t4));
        assertFalse(((davos.sdo.TypeXML)t3).isMixedContent());
        assertTrue(((davos.sdo.TypeXML)t4).isMixedContent());
    }

    public void testProperty()
    {
        Type t0 = typeHelper.getType("http://www.example.com/test", "PurchaseOrder");
        Property p01 = t0.getProperty("goodies");
        Property p02 = t0.getProperty("orderDate");
        Type t = typeHelper.getType("http://www.example.com/choice", "ShirtType");
        Property p1 = t.getProperty("color"); // element
        Property p2 = t.getProperty("id"); // attribute
        Property p3 = simpleType.getProperty("string");
        Property p4 = simpleType.getProperty("time");
        // local name
        assertEquals("items", xsdHelper.getLocalName(p01)); // name is "goodies"
        assertEquals("orderDate", xsdHelper.getLocalName(p02));
        assertEquals("color", xsdHelper.getLocalName(p1));
        assertEquals("id", xsdHelper.getLocalName(p2));
        assertEquals("string", xsdHelper.getLocalName(p3));
        assertEquals("time", xsdHelper.getLocalName(p4));
        // namespace uri
        assertEquals("http://www.example.com/choice", xsdHelper.getNamespaceURI(p1));
        assertEquals("http://www.example.com/choice", xsdHelper.getNamespaceURI(p2));
        assertNull(xsdHelper.getNamespaceURI(p3));
        assertNull(xsdHelper.getNamespaceURI(p4));
        // is attribute
        assertFalse(xsdHelper.isAttribute(p1));
        assertTrue(xsdHelper.isAttribute(p2));
        assertTrue(xsdHelper.isAttribute(p3));
        assertTrue(xsdHelper.isAttribute(p4));
        // is element
        assertTrue(xsdHelper.isElement(p1));
        assertFalse(xsdHelper.isElement(p2));
        assertFalse(xsdHelper.isElement(p3));
        assertFalse(xsdHelper.isElement(p4));
    }

    private static final String APPINFO_START_FRAG = "<xs:appinfo";
    private static final String SOURCE1 = "source=\"uri1\"";
    private static final String SOURCE2 = "source=\"uri2\"";
    private static final String XS_DECL = "xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"";
    private static final String TNS_DECL = "xmlns:tns=\"http://sdo/test/profile\"";
    private static final String APPINFO_END = "</xs:appinfo>";
    private static final String APPINFO_START1 = APPINFO_START_FRAG + " " + SOURCE1 + " " + XS_DECL + " " + TNS_DECL + ">";
    private static final String APPINFO_START2 = APPINFO_START_FRAG + " " + SOURCE2 + " " + XS_DECL + " " + TNS_DECL + ">";

    public void testAppinfo()
    {
        Type t1 = typeHelper.getType("http://sdo/test/profile", "Profile");
        String appinfo = xsdHelper.getAppinfo(t1, "uri1");
        assertNull(appinfo);
        appinfo = xsdHelper.getAppinfo(t1, "uri2");
        assertEquals(APPINFO_START2 + "apap" + APPINFO_END, appinfo);
        Type t2 = typeHelper.getType("http://sdo/test/profile", "Address");
        appinfo = xsdHelper.getAppinfo(t2, "uri1");
        assertNull(appinfo);
        appinfo = xsdHelper.getAppinfo(t2, "uri2");
        assertNull(appinfo);
        Property p1 = t1.getProperty("name");
        appinfo = xsdHelper.getAppinfo(p1, "uri1");
        assertEquals(APPINFO_START1 + "nnn" + APPINFO_END, appinfo);
        appinfo = xsdHelper.getAppinfo(p1, "uri2");
        assertNull(appinfo);
        Property p2 = t1.getProperty("address");
        appinfo = xsdHelper.getAppinfo(p2, "uri1");
        assertEquals(APPINFO_START1 + "aaa" + APPINFO_END, appinfo);
        appinfo = xsdHelper.getAppinfo(p2, "uri2");
        assertNull(appinfo);
    }

    public void testGlobalProperty() throws Exception
    {
        String uri = "http://sdo/test/anil";

        String name1 = "NormalizedCustomer1";
        Type t1 = typeHelper.getType(uri, name1); // in-lined (anonymous) type
        assertNotNull(t1);
        // global element with the above in-lined type
        Property p1a = xsdHelper.getGlobalProperty(uri, name1, true);
        assertNotNull(p1a);
        assertEquals(name1, p1a.getName());
        assertEquals(t1, p1a.getType());
        Property p1b = typeHelper.getOpenContentProperty(uri, name1);
        assertTrue(p1a == p1b);

        String name2 = "NormalizedCustomer2Type";
        Type t2 = typeHelper.getType(uri, name2);
        assertNotNull(t2);
        String name2a = "NormalizedCustomer2";
        Property p2a = xsdHelper.getGlobalProperty(uri, name2a, true);
        assertEquals(name2a, p2a.getName());
        assertEquals(t2, p2a.getType());
        Property p2b = typeHelper.getOpenContentProperty(uri, name2a);
        assertTrue(p2a == p2b);

        List types = new ArrayList();
        types.add(t1);
        types.add(t2);
        String xsd = xsdHelper.generate(types);
        System.out.println(xsd);
        File f0 = new File(dir, "anil.xsd");
        Writer w = new FileWriter(f0);
        w.write(xsd);
        w.close();
        compareXMLFiles(getResourceFile("type", "anil_gen.xsd"), f0);

        types.clear();
        types.add(p1a.getContainingType());
        xsd = xsdHelper.generate(types);
        System.out.println(xsd);
        File f1 = new File(dir, "anil1.xsd");
        w = new FileWriter(f1);
        w.write(xsd);
        w.close();
        compareXMLFiles(getResourceFile("type", "anil_gen1.xsd"), f1);

        types.clear();
        types.add(p2a.getContainingType());
        xsd = xsdHelper.generate(types);
        System.out.println(xsd);
        File f2 = new File(dir, "anil2.xsd");
        w = new FileWriter(f2);
        w.write(xsd);
        w.close();
        compareXMLFiles(getResourceFile("type", "anil_gen2.xsd"), f2);
    }

    private boolean _equal(List<Type> l1, List<Type> l2)
    {
        boolean result = true;
        if (l1.size() == l2.size())
        {
            for (int i = 0; i < l1.size(); i++)
            {
                if (!l1.get(i).equals(l2.get(i)))
                {
                    result = false;
                    break;
                }
            }
        }
        else
        {
            result = false;
        }
        return result;
    }

    private static final String xsdString = 
    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + 
    "<xsd:schema " + 
    "    targetNamespace=\"http://www.example.com/simple3\" " + 
    "    xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" " + 
    "    xmlns:simple=\"http://www.example.com/simple3\">" + 
    "    <xsd:element name=\"quote\" type=\"simple:Quote\"/>" + 
    "    <xsd:complexType name=\"Quote\">" + 
    "        <xsd:sequence>" + 
    "            <xsd:element name=\"symbol\" type=\"xsd:string\"/>" + 
    "            <xsd:element name=\"companyName\" type=\"xsd:string\"/>" + 
    "            <xsd:element name=\"price\" type=\"xsd:decimal\"/>" + 
    "            <xsd:element name=\"open1\" type=\"xsd:decimal\"/>" + 
    "            <xsd:element name=\"high\" type=\"xsd:decimal\"/>" + 
    "            <xsd:element name=\"low\" type=\"xsd:decimal\"/>" + 
    "            <xsd:element name=\"volume\" type=\"xsd:double\"/>" + 
    "            <xsd:element name=\"change1\" type=\"xsd:double\"/>" + 
    "            <xsd:element name=\"quotes\" type=\"simple:Quote\" minOccurs=\"0\" maxOccurs=\"unbounded\"/>" + 
    "        </xsd:sequence>" + 
    "    </xsd:complexType>" + 
    "</xsd:schema>";

    /* complex types from schema
       - no elementFormDefault in original schema (so unqualified)
         -> no elementFormDefault in generated xsd
    */
    public void testDefineAndGenerate() throws Exception
    {
        List<Type> typeList1 = xsdHelper.define(xsdString);
        List<Type> typeList1b = xsdHelper.define(xsdString); // duplicate define
        assertEquals(1, typeList1.size());
        assertEquals(0, typeList1b.size()); // second define should return empty list

        File f = getResourceFile("checkin", "simple2.xsd_");
        Reader xsdReader = new FileReader(f);
        String schemaLocation = f.toURL().toString();
        List<Type> typeList2 = xsdHelper.define(xsdReader, schemaLocation);
        
        // define again, this time using InputStream
        InputStream xsdStream = getResourceAsStream("checkin", "simple2.xsd_");
        List<Type> typeList3 = xsdHelper.define(xsdStream, schemaLocation);
        // second define should return empty list
        assertEquals(0, typeList3.size());
        
        String xsd1 = xsdHelper.generate(typeList1);
        System.out.println(xsd1);
        Map<String, String> nsToSL = null;//new HashMap<String, String>();
        //nsToSL.put("", "");
        String xsd2 = xsdHelper.generate(typeList2, nsToSL);
        System.out.println(xsd2);
        
    }

    public void testDuplicateDefine() throws Exception
    {
        File f0 = getResourceFile("type", "duplicate_define0.xsd_");
        Reader r0 = new FileReader(f0);
        List types0 = xsdHelper.define(r0, f0.toURL().toString());
        r0.close();
        assertEquals(2, types0.size());
        Type t1 = (Type)types0.get(0);
        System.out.println(t1);
        Type t2 = (Type)types0.get(1);
        System.out.println(t2);
        assertEquals("http://sdo/test/duplicate_define", t1.getURI());
        assertEquals("http://sdo/test/duplicate_define", t2.getURI());
        // it's not clear what order the types are found,
        // so we do the following
        boolean quoteFound = false;
        boolean sideFound = false;
        if (t1.getName().equals("Quote")) quoteFound = true;
        if (t2.getName().equals("Quote")) quoteFound = true;
        if (t1.getName().equals("Side")) sideFound = true;
        if (t2.getName().equals("Side")) sideFound = true;
        assertTrue(quoteFound);
        assertTrue(sideFound);

        // define again using xsd containing t1, t2, and in addition, t3
        File f1 = getResourceFile("type", "duplicate_define1.xsd_");
        Reader r1 = new FileReader(f1);
        List types1 = xsdHelper.define(r1, f1.toURL().toString());
        r1.close();
        // types1 contains only t3
        assertEquals(1, types1.size());
        Type t3 = (Type)types1.get(0);
        System.out.println(t3);
        assertEquals("http://sdo/test/duplicate_define", t3.getURI());
        assertEquals("Order", t3.getName());
        Type t3a = typeHelper.getType("http://sdo/test/duplicate_define", "Order");
        assertTrue(t3 == t3a);
        List t3aprops = t3a.getProperties();
        assertEquals(2, t3aprops.size());
        assertEquals("quote", ((Property)t3aprops.get(0)).getName());
        assertEquals("quantity", ((Property)t3aprops.get(1)).getName());

        // define again using xsd containing t1, t1, and a modified t3
        File f2 = getResourceFile("type", "duplicate_define2.xsd_");
        Reader r2 = new FileReader(f2);
        List types2 = xsdHelper.define(r2, f2.toURL().toString());
        r2.close();
        // this define should be a no-op
        assertEquals(0, types2.size());
        Type t3b = typeHelper.getType("http://sdo/test/duplicate_define", "Order");
        assertTrue(t3 == t3b);
        List t3bprops = t3b.getProperties();
        assertEquals(2, t3bprops.size());
        assertEquals("quote", ((Property)t3bprops.get(0)).getName());
        // duplicate xsd has "qty" instead of "quantity", but t3 should not have changed
        assertEquals("quantity", ((Property)t3bprops.get(1)).getName());
    }

    /* a simple case of a dynamically defined complex type
       - attributes only (unqualified)
    */
    public void testGenerate() throws Exception
    {
        List<Type> definedTypes = new ArrayList<Type>();
        definedTypes.add(simpleType);
        String xsd1 = xsdHelper.generate(definedTypes);
        System.out.println(xsd1);
        String xsd = getXML(getResourceFile("type", "simple_dynamic.xsd_")).trim();
        assertEquals(xsd, xsd1);
    }

    /* complex types from schema, non-sequenced 
       - sdojava:package annotation
       - elementFormDefault="qualified" in original schema
       - attributeFormDefault="qualified" in original schema, 
         so attributes are unqualified
     */
    public void testGenerateComplexType() throws Exception
    {
        List<Type> loadedTypes = new ArrayList<Type>();
        String uri = //"http://www.example.com/test";
            "http://www.example.com/choice";
        Type t1 = //typeHelper.getType(uri, "Address");
            typeHelper.getType(uri, "ShirtType");
        Type t2 = //typeHelper.getType(uri, "PurchaseOrder");
            typeHelper.getType(uri, "PantsType");
        loadedTypes.add(t1);
        loadedTypes.add(t2);
        String xsd1 = xsdHelper.generate(loadedTypes);
        System.out.println(xsd1);
        String xsd = getXML(getResourceFile("type", "choice1_gen.xsd_")).trim();
        assertEquals(xsd, xsd1);
    }

    /* complex type from schema, sequenced */
    public void testGenerateSequenced1() throws Exception
    {
        String uri = "http://www.example.com/choice";
        List<Type> loadedTypes = new ArrayList<Type>();
        Type t = 
            typeHelper.getType(uri, "ItemsType");
        loadedTypes.add(t);
        String xsd1 = xsdHelper.generate(loadedTypes);
        System.out.println(xsd1);
        String xsd = getXML(getResourceFile("type", "choice2_gen.xsd_")).trim();
        assertEquals(xsd, xsd1);
    }

    /* dynamically defined sequenced type */
    public void testGenerateSequenced2() throws Exception
    {
        List<Type> loadedTypes = new ArrayList<Type>();
        loadedTypes.add(sequencedType);
        String xsd1 = xsdHelper.generate(loadedTypes);
        System.out.println(xsd1);
        String xsd = getXML(getResourceFile("type", "sequenced_dynamic.xsd_")).trim();
        assertEquals(xsd, xsd1);
    }

    /* dynamically defined open type, not sequenced */
    public void testGenerateNonSequenced() throws Exception
    {
        List<Type> types = new ArrayList<Type>();
        types.add(openNotSequencedType);
        String xsdl = xsdHelper.generate(types);
        System.out.println(xsdl);
        String xsd = getXML(getResourceFile("type", "open_notsequenced_dynamic.xsd_")).trim();
        assertEquals(xsd, xsdl);
    }

    public void testCompositeNames() throws Exception
    {
        Type cbv = typeHelper.getType("urn:CustomerBaseView", "CUSTOMER_BASE_VIEW");
        List<Type> types = new ArrayList<Type>();
        types.add(cbv);
        String xsd = xsdHelper.generate(types);
        System.out.println(xsd);
        File f = new File(dir, "cbv1.xsd");
        Writer w = new FileWriter(f);
        w.write(xsd);
        w.close();
        compareXMLFiles(getResourceFile("type", "cbv_gen1.xsd_"), f);
    }

    public void testGenerateWithNSToSLMap() throws Exception
    {
        Type cbv = typeHelper.getType("urn:CustomerBaseView", "CUSTOMER_BASE_VIEW");
        List<Type> types = new ArrayList<Type>();
        types.add(cbv);
        Map<String, String> ns2sl = new HashMap<String, String>();
        ns2sl.put("urn:Retail", "Profile.xsd Address.xsd CreditCard.xsd Order.xsd Item.xsd Case.xsd");
        ns2sl.put("urn:Product", "Product.xsd");
        String xsd = xsdHelper.generate(types, ns2sl);
        System.out.println(xsd);
        File f = new File(dir, "cbv2.xsd");
        Writer w = new FileWriter(f);
        w.write(xsd);
        w.close();
        compareXMLFiles(getResourceFile("type", "cbv_gen2.xsd_"), f);
    }

    public void testNoNamespace() throws Exception
    {
        // Check that passing "" or null as parameter finds a global element with no namespace URI
        Property globalNoNSElem = xsdHelper.getGlobalProperty("", "catalog4", true);
        assertNotNull(globalNoNSElem);
        assertEquals("catalog4", globalNoNSElem.getType().getName());
        globalNoNSElem = xsdHelper.getGlobalProperty(null, "catalog4", true);
        assertNotNull(globalNoNSElem);
        assertEquals("catalog4", globalNoNSElem.getType().getName());
        // Check that getNamespaceURI(property) returns "" if the property has the default ns URI
        assertEquals("", xsdHelper.getNamespaceURI(globalNoNSElem));
        // Check that type.getURI() returns null for a type with no namespace URI
        assertNull(globalNoNSElem.getType().getURI());
        // Check that TypeHelper interprets "" or null as meaning a property/type with no
        // namespace when doing look-ups
        Type catalogType = typeHelper.getType(null, "catalog4");
        assertNotNull(catalogType);
        assertEquals(1, catalogType.getProperties().size());
        catalogType =  typeHelper.getType("", "catalog4");
        assertNotNull(catalogType);
        assertEquals(1, catalogType.getProperties().size());
        globalNoNSElem = typeHelper.getOpenContentProperty(null, "catalog4");
        assertNotNull(globalNoNSElem);
        assertEquals("catalog4", globalNoNSElem.getType().getName());
        globalNoNSElem = typeHelper.getOpenContentProperty("", "catalog4");
        assertNotNull(globalNoNSElem);
        assertEquals("catalog4", globalNoNSElem.getType().getName());
        DataObject o = factory.create(null, "catalog4");
        assertSame(o.getType(), catalogType);
        o = factory.create("", "catalog4");
        assertSame(o.getType(), catalogType);
        // Check that calling TypeHelper.defineOpenContentProperty() will create an on-demand
        // open-content property if the URI passed in is null and a global property with empty URI
        // if the URI passed in is ""
        DataObject propAsDO = factory.create(BuiltInTypeSystem.PROPERTY);
        propAsDO.set("name", "XSDHelperTest.testNoNamespace.Prop1");
        propAsDO.set("type", BuiltInTypeSystem.STRING);
        propAsDO.set(BuiltInTypeSystem.P_PROPERTY_XMLELEMENT, true);
        PropertyXML onDemandProp = (PropertyXML) typeHelper.defineOpenContentProperty(null, propAsDO);
        assertTrue(onDemandProp.isXMLElement());
        assertTrue(onDemandProp.isDynamic());
        assertFalse(onDemandProp.isGlobal());
        assertNull(typeHelper.getOpenContentProperty(null, "XSDHelperTest.testNoNamespace.Prop1"));
        propAsDO.set("name", "XSDHelperTest.testNoNamespace.Prop2");
        onDemandProp = (PropertyXML) typeHelper.defineOpenContentProperty("", propAsDO);
        assertTrue(onDemandProp.isXMLElement());
        assertTrue(onDemandProp.isDynamic());
        assertTrue(onDemandProp.isGlobal());
        assertNotNull(typeHelper.getOpenContentProperty(null, "XSDHelperTest.testNoNamespace.Prop2"));
        // Check that if the "uri" property on a Type DataObject is set to "", after TypeHelper.define
        // the getURI() methods will return null for consistency
        DataObject typeAsDO = factory.create(BuiltInTypeSystem.TYPE);
        typeAsDO.set("uri", "");
        typeAsDO.set("name", "XSDHelperTest.testNoNamespace.Type1");
        Type definedType = typeHelper.define(typeAsDO);
        assertNull(definedType.getURI());
        assertEquals("XSDHelperTest.testNoNamespace.Type1", definedType.getName());
    }
}
