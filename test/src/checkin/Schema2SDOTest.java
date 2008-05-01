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

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import davos.sdo.Options;
import davos.sdo.TypeXML;
import davos.sdo.SDOContextFactory;
import davos.sdo.impl.type.BuiltInTypeSystem;
import davos.sdo.impl.common.Names;
import davos.sdo.type.TypeSystem;
import davos.sdo.type.XSDHelperExt;
import javax.sdo.Property;
import javax.sdo.Type;
import javax.sdo.helper.XSDHelper;

import common.BaseTest;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @author Radu Preotiuc-Pietro
 * Created
 * Date: Apr 26, 2006
 * Time: 5:26:11 PM
 */
public class Schema2SDOTest extends BaseTest
{
    public Schema2SDOTest(String name)
    {
        super(name);
    }

    private static final String testURI = "http://www.example.com/IPO";
    private static XSDHelper xsdHelper = context.getXSDHelper();

    public void testExampleSchema() throws IOException
    {
        File[] schemaFiles = {
            getResourceFile("checkin", "IPO.xsd"), 
            getResourceFile("checkin", "Customer.xsd"), 
            getResourceFile("checkin", "xlink.xsd")
        };

//        try
//        {
//        XmlObject o = XmlObject.Factory.parse(schemaFiles[0]);
//        System.out.println(o.documentProperties().getSourceName());
//        }
//        catch (org.apache.xmlbeans.XmlException e) {}
        TypeSystem ts = davos.sdo.impl.binding.Schema2SDO.createSDOTypeSystem(
            new FileReader(schemaFiles[0]), schemaFiles[0].toURL().toString(), null);

        assertNotNull(ts);
        Set<TypeXML> allTypes = ts.getAllTypes();
        assertEquals(14, allTypes.size());
        // Check all document types
        assertNotNull(ts.getGlobalPropertyByTopLevelElemQName(testURI, "purchaseOrder"));
        assertNotNull(ts.getGlobalPropertyByTopLevelElemQName(testURI, "comment"));

        // Complex Type
        Type t = ts.getTypeBySchemaTypeName(testURI, "PurchaseOrderType");
        assertNotNull(t);

        // Simple Type
        t = ts.getTypeXML(testURI, "QuantityType");
        assertNotNull(t);
        assertTrue(t.isDataType());
        // Commenting out the following line, since that is an error in the spec example
        // assertEquals(t.getBaseTypes().get(0), BuiltInTypeSystem.INT);
        assertEquals(0, t.getBaseTypes().size());
        assertEquals(int.class, t.getInstanceClass());
        t = ts.getTypeBySchemaTypeName(testURI, "SKU");
        assertNotNull(t);
        assertTrue(t.isDataType());
        assertEquals(t.getBaseTypes().get(0), BuiltInTypeSystem.STRING);
        assertEquals(t.getInstanceClass(), String.class);

        // Local Element with Complex Type
        t = ts.getTypeXML(testURI, "PurchaseOrderType");
        assertNotNull(t);
        assertEquals(9, t.getDeclaredProperties().size());
        Property p = t.getProperty("shipTo");
        assertNotNull(p);
        assertTrue(p.isContainment());
        assertEquals("Address", p.getType().getName());
        assertEquals("PurchaseOrderType", p.getContainingType().getName());
        assertFalse(p.isMany());
        p = t.getProperty("billTo");
        assertNotNull(p);
        assertTrue(p.isContainment());
        assertEquals("Address", p.getType().getName());
        assertEquals("PurchaseOrderType", p.getContainingType().getName());
        assertFalse(p.isMany());
        p = t.getProperty("items");
        assertNotNull(p);
        assertTrue(p.isContainment());
        assertEquals("Items", p.getType().getName());
        assertEquals("PurchaseOrderType", p.getContainingType().getName());
        assertFalse(p.isMany());

        // Local Element with Simple Type
        p = t.getProperty("comment");
        assertNotNull(p);
        assertEquals("String", p.getType().getName());
        assertEquals("PurchaseOrderType", p.getContainingType().getName());
        t = ts.getTypeXML(testURI, "Items");
        assertNotNull(t);
        assertEquals(2, t.getDeclaredProperties().size());
        p = t.getProperty("productName");
        assertEquals("String", p.getType().getName());
        assertEquals("Items", p.getContainingType().getName());

        // Local Attribute
        t = ts.getTypeXML(testURI, "PurchaseOrderType");
        p = t.getProperty("orderDate");
        // assertEquals("Date", p.getType().getName());
        assertEquals("YearMonthDay", p.getType().getName());
        assertEquals("PurchaseOrderType", p.getContainingType().getName());
        t = ts.getTypeXML(testURI, "ItemType");
        p = t.getProperty("partNum");
        assertEquals("SKU", p.getType().getName());
        assertEquals("ItemType", p.getContainingType().getName());

        // Type extension
        t = ts.getTypeBySchemaTypeName(testURI, "USAddress");
        assertNotNull(t);
        assertEquals(1, t.getDeclaredProperties().size());
        assertEquals(4, t.getProperties().size());
        assertSame(ts.getTypeXML(testURI, "Address"), t.getBaseTypes().get(0));

        // Local Attribute fixed value declaration
        p = t.getProperty("country");
        assertNotNull(p);
        assertEquals(BuiltInTypeSystem.STRING, p.getType());
        assertSame(t, p.getContainingType());
        assertEquals("US", p.getDefault());

        // Multi-valued local element declaration
        t = ts.getTypeBySchemaTypeName(testURI, "Items");
        p = (Property) t.getProperties().get(1);
        assertSame(t, p.getContainingType());
        assertEquals(ts.getTypeXML(testURI, "ItemType"), p.getType());
        assertTrue(p.isContainment());
        assertTrue(p.isMany());

        // Attribute reference declarations
        t = ts.getTypeXML(testURI, "PurchaseOrderType");
        p = (Property) t.getProperties().get(6);
        assertEquals("customer", p.getName());
        assertEquals("Customer", p.getType().getName());
        assertEquals(t, p.getContainingType());
        assertEquals(p.getOpposite(), ts.getTypeXML(testURI, "Customer").
            getProperty("purchaseOrder"));
        p = t.getProperty("customer2");
        assertNotNull(p);
        assertEquals("Customer", p.getType().getName());
        assertSame(t, p.getContainingType());
        p = (Property) t.getProperties().get(t.getProperties().size() - 1);
        assertEquals(p.getName(), "Customer");
        assertEquals(ts.getTypeXML("http://www.example.com/IPO", "Customer"),
            p.getType());

        // Local Attribute ID declaration
        t = ts.getTypeXML("http://www.example.com/IPO", "Customer");
        p = t.getProperty("primaryKey");
        assertNotNull(p);
        assertEquals("String", p.getType().getName());

        // Local Attribute default value declaration
        t = ts.getTypeXML(testURI, "USAddress");
        // Same as fixed, no difference here

        // Abstract Complex Types
        t = ts.getTypeXML(testURI, "Vehicle");
        assertNotNull(t);
        assertTrue(t.isAbstract());

        // Simple Type unions
        t = ts.getTypeBySchemaTypeName(testURI, "zipUnion");
        assertNotNull(t);
        assertEquals(Object.class, t.getInstanceClass());
        // Not sure what else to check here

        // Try to generate the Schema back
        String xsd = xsdHelper.generate(new ArrayList(allTypes));
        int i1 = findCType("Address", xsd);
        int i2 = findEndCType(i1, xsd);
        assertTrue(containsElement(i1, i2, "name", Names.PREFIX_XSD + ":string", xsd));
        assertTrue(containsElement(i1, i2, "street", Names.PREFIX_XSD + ":string", xsd));
        i1 = findCType("PurchaseOrderType", xsd);
        i2 = findEndCType(i1, xsd);
        assertTrue(containsAttribute(i1, i2, "orderDate", Names.PREFIX_XSD + ":date", xsd));
        assertTrue(containsAttribute(i1, i2, "customer", Names.PREFIX_XSD + ":IDREF", null,
            null, "tns:Customer", "purchaseOrder", xsd));
        assertTrue(containsAttribute(i1, i2, "customer2", Names.PREFIX_XSD + ":anyURI", null,
            null, "tns:Customer", null, xsd));
        assertTrue(containsAttribute(i1, i2, "ns1:href", "Customer",
            "tns:Customer", null, xsd));
        assertTrue(containsElement(i1, i2, "shipTo", "tns:Address", xsd));
        assertTrue(containsElement(i1, i2, "items", "tns:Items", xsd));
        i1 = findSType("QuantityType", xsd);
        i2 = findEndSType(i1, xsd);
        assertTrue(xsd.indexOf(Names.PREFIX_SDOJAVA + ":instanceClass=\"int\"", i1) < i1 + 40);
        i1 = findSType("SKU", xsd);
        i2 = findEndSType(i1, xsd);
        assertTrue(derivesFrom(i1, i2, 2, Names.PREFIX_XSD + ":string", xsd));
        i1 = findCType("Items", xsd);
        i2 = findEndCType(i1, xsd);
        assertTrue(containsElement(i1, i2, "productName", Names.PREFIX_XSD + ":string", xsd));
        assertTrue(containsElement(i1, i2, "item", Names.PREFIX_TNS + ":ItemType", true, null, xsd));
        i1 = findCType("ItemType", xsd);
        i2 = findEndCType(i1, xsd);
        assertTrue(containsAttribute(i1, i2, "partNum", Names.PREFIX_TNS + ":SKU", xsd));
        i1 = findCType("USAddress", xsd);
        i2 = findEndCType(i1, xsd);
        assertTrue(derivesFrom(i1, i2, 1, Names.PREFIX_TNS + ":Address", xsd));
        assertTrue(containsAttribute(i1, i2, "country", Names.PREFIX_XSD + ":string", null, "US",
            null, null, xsd));
        assertTrue(xsd.indexOf("<" + Names.PREFIX_XSD + ":complexType name=\"Vehicle\" abstract=\"true\"") > 0);
        i1 = findSType("zipUnion", xsd);
        i2 = findEndSType(i1, xsd);
        assertTrue(derivesFrom(i1, i2, 0, null, xsd));
        i1 = findSType("USState", xsd);
        i2 = findEndSType(i1, xsd);
        assertTrue(derivesFrom(i1, i2, 2, Names.PREFIX_XSD + ":string", xsd));
        i1 = findSType("myIntType", xsd);
        i2 = findEndSType(i1, xsd);
        assertTrue(derivesFrom(i1, i2, 2, Names.PREFIX_XSD + ":int", xsd));
        i1 = findSType("listOfMyIntType", xsd);
        i2 = findEndSType(i1, xsd);
        assertTrue(derivesFrom(i1, i2, 3, Names.PREFIX_XSD + ":string", xsd));
        i1 = findCType("Customer", xsd);
        i2 = findEndCType(i1, xsd);
        assertTrue(containsElement(i1, i2, "purchaseOrder", Names.PREFIX_TNS + ":PurchaseOrderType",
            true, null, xsd));
        assertTrue(containsAttribute(i1, i2, "primaryKey", Names.PREFIX_XSD + ":ID", xsd));
    }

    private static final String sdoManySchema1 =
        "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">" +
        "  <xs:complexType name=\"base\">" +
        "    <xs:choice>" +
        "      <xs:element name=\"elem\" minOccurs=\"0\"/>" +
        "    </xs:choice>" +
        "  </xs:complexType>" +
        "  <xs:complexType name=\"derived\">" +
        "    <xs:complexContent>" +
        "      <xs:restriction base=\"base\">" +
        "        <xs:choice>" +
        "          <xs:element name=\"elem\" sdoxml:many=\"true\"" +
        "            xmlns:sdoxml=\"commonj.sdo/xml\"/>" +
        "        </xs:choice>" +
        "      </xs:restriction>" +
        "    </xs:complexContent>" +
        "  </xs:complexType>" +
        "</xs:schema>";

    private static final String sdoManySchema2 =
        "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">" +
        "  <xs:complexType name=\"type\" mixed=\"true\">" +
        "    <xs:sequence>" +
        "      <xs:element name=\"a\" minOccurs=\"0\"/>" +
        "      <xs:element name=\"b\"/>" +
        "    </xs:sequence>" +
        "  </xs:complexType>" +
        "</xs:schema>";

    public void testSdoMany()
    {
        List types = xsdHelper.define(sdoManySchema1);
        Type type = (Type) types.get(0);
        Property prop = type.getProperty("elem");
        assertNotNull(prop);
        assertTrue(prop.isMany());
        type = (Type) types.get(1);
        prop = type.getProperty("elem");
        assertNotNull(prop);
        assertTrue(prop.isMany());
        types = xsdHelper.define(sdoManySchema2);
        String newXsd = xsdHelper.generate(types);
        assertTrue(newXsd.startsWith("<xs:schema "));
        int i = newXsd.indexOf("many=\"false\"");
        assertTrue(i > 0);
        assertTrue(newXsd.indexOf("many=\"false\"", i + 10) > i);
    }

    public void testWsdlParsing() throws java.net.MalformedURLException
    {
        String wsdlLocation = getResourceFile("checkin", "customer_view.wsdl").toURL().toString();
        List<Type> types = ((XSDHelperExt) context.getXSDHelper()).defineSchemasFromWsdl(
            wsdlLocation, new Options().setCompileAnonymousTypeNames(Options.NAMES_COMPOSITE));
        boolean found = false;
        for (Type type : types)
            if ("ld:LogicalDSs/Customer_view.ws".equals(type.getURI()) && "createCUSTOMER_VIEW".equals(type.getName()))
            {
                found = true;
                break;
            }
        assertTrue(found);
    }

    private static final String BXSD =
        "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" targetNamespace=\"Schema2SDOTest.testSchemaImport.B\">\n" +
        "<xs:complexType name=\"B\"/>\n</xs:schema>";
    private static final String AXSD =
        "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" targetNamespace=\"Schema2SDOTest.testSchemaImport.A\" " +
        "xmlns:b=\"Schema2SDOTest.testSchemaImport.B\">\n" +
        "<xs:import namespace=\"Schema2SDOTest.testSchemaImport.B\" schemaLocation=\"B.xsd\"/>\n" +
        "<xs:complexType name=\"A\"><xs:sequence><xs:element name=\"b\" type=\"b:B\"/></xs:sequence></xs:complexType>\n" +
        "</xs:schema>";

    public void testSchemaImport()
    {
        XSDHelper xsdHelper = SDOContextFactory.createNewSDOContext().getXSDHelper();
        List types;
        types = xsdHelper.define(BXSD);
        assertEquals(1, types.size());
        // Should pass without exception
        types = xsdHelper.define(BXSD);
        assertEquals(0, types.size());
        // Should also pass without exception
        types = xsdHelper.define(AXSD);
        assertEquals(1, types.size());
    }

    public void testSchemaImportWithResolvers()
    {
        XSDHelperExt xsdHelper = (XSDHelperExt) SDOContextFactory.createNewSDOContext().getXSDHelper();
        List types;
        org.xml.sax.EntityResolver er = new org.xml.sax.EntityResolver() {
            public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException
            {
                return "ld:/schemas/B.xsd".equals(systemId) ?
                    new InputSource(new java.io.StringReader(BXSD)) : null;
            }
        } ;
        types = xsdHelper.defineSchema(AXSD, "ld:/schemas/A.xsd", new davos.sdo.Options().
            setCompileEntityResolver(er));
        assertEquals(2, types.size());
    }

    private static final String CONFLICT_XSD =
        "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" targetNamespace=\"Schema2SDOTest.testNameConflict\">\n" +
        "<xs:element name=\"A\">\n" +
        "<xs:complexType>\n" +
        "<xs:sequence>\n" +
        "<xs:element name=\"A\">\n" +
        "<xs:complexType/>\n" +
        "</xs:element>\n" +
        "</xs:sequence>\n" +
        "<xs:attribute name=\"A\">\n" +
        "<xs:simpleType>\n" +
        "<xs:restriction base=\"xs:int\"/>\n" +
        "</xs:simpleType>\n" +
        "</xs:attribute>\n" +
        "</xs:complexType>\n" +
        "</xs:element>\n" +
        "</xs:schema>";

    public void testNameConflictResolution()
    {
        XSDHelperExt xsdHelper = (XSDHelperExt) SDOContextFactory.createNewSDOContext().getXSDHelper();
        List types;
        boolean thrown = false;
        try
        {
            types = xsdHelper.defineSchema(CONFLICT_XSD, null, new davos.sdo.Options().
                setCompileAnonymousTypeNames(davos.sdo.Options.NAMES_STANDARD));
        }
        catch (davos.sdo.SDOBindingException sbe)
        {
            thrown = true;
        }
        assertTrue(thrown);
        xsdHelper = (XSDHelperExt) SDOContextFactory.createNewSDOContext().getXSDHelper();
//        types = xsdHelper.defineSchema(CONFLICT_XSD, null, new davos.sdo.Options().
//            setCompileAnonymousTypeNames(davos.sdo.Options.NAMES_WITH_NUMBER_SUFFIX));
//        assertEquals(3, types.size());
//        // We don't know in what order will the types be returned
        HashSet<String> set = new HashSet<String>(3);
//        set.add(((Type) types.get(0)).getName());
//        set.add(((Type) types.get(1)).getName());
//        set.add(((Type) types.get(2)).getName());
//        assertTrue(set.contains("A"));
//        assertTrue(set.contains("A.A"));
//        assertTrue(set.contains("A.A2"));
//        set.clear();
        xsdHelper = (XSDHelperExt) SDOContextFactory.createNewSDOContext().getXSDHelper();
        types = xsdHelper.defineSchema(CONFLICT_XSD, null, new davos.sdo.Options().
            setCompileAnonymousTypeNames(davos.sdo.Options.NAMES_COMPOSITE));
        assertEquals(3, types.size());
        set.add(((Type) types.get(0)).getName());
        set.add(((Type) types.get(1)).getName());
        set.add(((Type) types.get(2)).getName());
        assertTrue(set.contains("A"));
        assertTrue(set.contains("A$A"));
        assertTrue(set.contains("A@A"));
        set.clear();
    }

    private boolean derivesFrom(int i1, int i2, int dtype, String baseType, String xsd)
    {
        Pattern pattern;
        switch (dtype)
        {
        case 0:
            return i1 == i2;
        case 1:
            pattern = Pattern.compile("<" + Names.PREFIX_XSD + ":extension base=\"" + baseType + "\"/?>");
            break;
        case 2:
            pattern = Pattern.compile("<" + Names.PREFIX_XSD + ":restriction base=\"" + baseType + "\"/?>");
            break;
        case 3:
            pattern = Pattern.compile("<" + Names.PREFIX_XSD + ":list itemType=\"" + baseType + "\"/?>");
            break;
        default:
            throw new IllegalStateException();
        }
        Matcher matcher = pattern.matcher(xsd);
        matcher.region(i1, i2);
        return matcher.find();
    }

    private boolean containsElement(int start, int end, String name, String type, String xsd)
    {
        Pattern pattern = Pattern.compile("<" + Names.PREFIX_XSD + ":element .*(name=\"" + name +
            "\" .*type=\"" + type + "\")|(type=\"" + type + "\" .*name=\"" + name + "\")");
        Matcher matcher = pattern.matcher(xsd);
        matcher.region(start ,end);
        return matcher.find();
    }

    private boolean containsElement(int start, int end, String name, String type,
        boolean unbounded, String sdoName, String xsd)
    {
        Pattern pattern = Pattern.compile("<" + Names.PREFIX_XSD + ":element .*name=\"" + name + "\".*" + "/>");
        Matcher matcher = pattern.matcher(xsd);
        matcher.region(start, end);
        assertTrue(matcher.find());
        int ss = matcher.start();
        int ee = matcher.end();
        boolean result = true;
        result &= xsd.indexOf("type=\"" + type + "\"", ss) < ee;
        if (unbounded)
            result &= xsd.indexOf("maxOccurs=\"unbounded\"") < ee;
        if (sdoName != null)
            result &= xsd.indexOf("sdoxml:name=\"" + sdoName + "\"", ss) < ee;
        return result;
    }

    private boolean containsAttribute(int start, int end, String name, String type, String xsd)
    {
        Pattern pattern = Pattern.compile("<" + Names.PREFIX_XSD + ":attribute .*(name=\"" + name +
            "\" .*type=\"" + type + "\")|(type=\"" + type + "\" .*name=\"" + name + "\")");
        Matcher matcher = pattern.matcher(xsd);
        matcher.region(start, end);
        return matcher.find();
    }

    private boolean containsAttribute(int start, int end, String name, String type,
        String sdoName, String def, String propertyType, String opposite, String xsd)
    {
        Pattern pattern = Pattern.compile("<" + Names.PREFIX_XSD + ":attribute .*name=\"" + name + "\".*" + "/>");
        Matcher matcher = pattern.matcher(xsd);
        matcher.region(start, end);
        assertTrue(matcher.find());
        int ss = matcher.start();
        int ee = matcher.end();
        boolean result = true;
        result &= xsd.indexOf("type=\"" + type + "\"", ss) < ee;
        if (def != null)
            result &= xsd.indexOf("default=\"" + def + "\"", ss) < ee;
        if (sdoName != null)
            result &= xsd.indexOf(Names.PREFIX_SDOXML + ":name=\"" + sdoName + "\"", ss) < ee;
        if (propertyType != null)
            result &= xsd.indexOf(Names.PREFIX_SDOXML + ":propertyType=\"" + propertyType + "\"", ss) < ee;
        if (opposite != null)
            result &= xsd.indexOf(Names.PREFIX_SDOXML + ":opposite=\"" + opposite + "\"", ss) < ee;
        return result;
    }

    private boolean containsAttribute(int start, int end, String ref,
        String sdoName, String propertyType, String opposite, String xsd)
    {
        Pattern pattern = Pattern.compile("<" + Names.PREFIX_XSD + ":attribute .*ref=\"" + ref + "\".*" + "/>");
        Matcher matcher = pattern.matcher(xsd);
        matcher.region(start, end);
        assertTrue(matcher.find());
        int ss = matcher.start();
        int ee = matcher.end();
        boolean result = true;
        if (sdoName != null)
            result &= xsd.indexOf(Names.PREFIX_SDOXML + ":name=\"" + sdoName + "\"", ss) < ee;
        if (propertyType != null)
            result &= xsd.indexOf(Names.PREFIX_SDOXML + ":propertyType=\"" + propertyType + "\"", ss) < ee;
        if (opposite != null)
            result &= xsd.indexOf(Names.PREFIX_SDOXML + ":opposite=\"" + opposite + "\"", ss) < ee;
        return result;
    }

    private int findCType(String name, String xsd)
    {
        Pattern pattern = Pattern.compile("<" + Names.PREFIX_XSD + ":complexType.*name=\"" + name + "\"");
        Matcher matcher = pattern.matcher(xsd);
        assertTrue("Could not find complex type \"" + name + "\" in the generated xsd.", matcher.find());
        return matcher.start();
    }

    private int findEndCType(int start, String xsd)
    {
        Pattern pattern = Pattern.compile("(<" + Names.PREFIX_XSD + ":complexType[^>]*/>)|(</" +
            Names.PREFIX_XSD + ":complexType>)");
        Matcher matcher = pattern.matcher(xsd);
        assertTrue(matcher.find(start));
        return matcher.start();
    }

    private int findSType(String name, String xsd)
    {
        Pattern pattern = Pattern.compile("<" + Names.PREFIX_XSD + ":simpleType.*name=\"" + name + "\"");
        Matcher matcher = pattern.matcher(xsd);
        assertTrue("Could not find complex type \"" + name + "\" in the generated xsd.", matcher.find());
        return matcher.start();
    }

    private int findEndSType(int start, String xsd)
    {
        Pattern pattern = Pattern.compile("(<" + Names.PREFIX_XSD + ":simpleType[^>]*/>)|(</" +
            Names.PREFIX_XSD + ":simpleType>)");
        Matcher matcher = pattern.matcher(xsd);
        assertTrue(matcher.find(start));
        return matcher.start();
    }
}
