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

import javax.sdo.helper.XMLDocument;
import javax.sdo.helper.XMLHelper;
import javax.sdo.helper.XSDHelper;
//import javax.sdo.helper.DataFactory;
import javax.sdo.DataObject;
import javax.sdo.Sequence;

import davos.sdo.DataObjectXML;
import davos.sdo.Options;
import davos.sdo.PropertyXML;
import davos.sdo.SequenceXML;
import davos.sdo.TypeXML;
import davos.sdo.impl.type.BuiltInTypeSystem;
import davos.sdo.impl.helpers.DataGraphHelper;
//import davos.sdo.type.TypeSystem;

import junit.framework.Test;
import junit.framework.TestSuite;
import common.BaseTest;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamResult;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * This class tests basic load (unmarshal) and save (marshal).
 * @author Wing Yew Poon
 */
public class MarshalTest extends BaseTest
{
    public MarshalTest(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        TestSuite suite = new TestSuite();
        suite.addTest(new MarshalTest("testLoadAndSave1"));
        suite.addTest(new MarshalTest("testLoadAndSave2"));
        suite.addTest(new MarshalTest("testLoad3"));
        suite.addTest(new MarshalTest("testQName"));
        suite.addTest(new MarshalTest("testQNameDOM"));
        suite.addTest(new MarshalTest("testQNameStAX"));
        suite.addTest(new MarshalTest("testSubstitutions"));
        suite.addTest(new MarshalTest("testAnyTypeWithSimpleContent"));
        suite.addTest(new MarshalTest("testUntypedXmlWithNamespace"));
        suite.addTest(new MarshalTest("testListType"));
        // or
        //TestSuite suite = new TestSuite(MarshalTest.class);
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

    private static XMLHelper xmlHelper = context.getXMLHelper();
    private static XSDHelper xsdHelper = context.getXSDHelper();

    private static final String xml1 = "<a><b/><c>xxx</c></a>";

    public void testLoadAndSave1() throws IOException
    {
        XMLDocument doc = xmlHelper.load(xml1);
        File f = new File(dir, "abc.xml");
        OutputStream out = new FileOutputStream(f);
        xmlHelper.save(doc, out, new Options().setSavePrettyPrint());
        out.close();

        BufferedReader br = new BufferedReader(new FileReader(f));
        StringBuffer sb = new StringBuffer();
        String line = null;
        while ((line = br.readLine()) != null)
        {
            sb.append(line).append(newline);
        }
        assertEquals(xml1 + newline, sb.toString());
        br.close();
    }

    public void testLoadAndSave2() throws IOException
    {
        XMLDocument doc = xmlHelper.load(xml1);
        Writer out = new StringWriter();
        xmlHelper.save(doc, out, new Options().setSavePrettyPrint());
        //out.flush(); // not necessary
        assertEquals(xml1, out.toString());
        out.close();
    }

    private static final String schema3 = "<xs:schema xmlns:xs=\"" + XSD_URI + "\">\n" +
        "<xs:complexType name=\"B\"> <xs:choice maxOccurs=\"unbounded\"> <xs:element name=\"c\" " +
        "type=\"cTT\" minOccurs='0' maxOccurs='10' /> <xs:element name=\"d\" " +
        "type=\"xs:string\" minOccurs='0' maxOccurs='1' /> </xs:choice> </xs:complexType> " +
        "<xs:complexType name=\"cTT\"> <xs:attribute name=\"at1\" type=\"xs:string\"/> " +
        "<xs:attribute name=\"at2\" type=\"xs:string\"/> </xs:complexType>" +
        "<xs:element name=\"b\" type=\"B\"/>" +
        "</xs:schema>";
    private static final String xml3 = "<b>" + " " +
        " <c at1='val_at1' at2='val_at2'>c_val1</c>" + " <c>c_val2</c>" + " <d>d_val</d>" +
        " <c>c_val3</c>" + " </b>";

    public void testLoad3() throws IOException
    {
        xsdHelper.define(schema3);
        XMLDocument doc = xmlHelper.load(xml3);
        DataObject root = doc.getRootObject();
        Sequence seq = root.getSequence();
        assertEquals("c", seq.getProperty(0).getName());
        assertEquals("c", seq.getProperty(1).getName());
        assertEquals("d", seq.getProperty(2).getName());
        assertEquals("c", seq.getProperty(3).getName());
    }

    private static final String schema4 = "<xs:schema xmlns:xs=\"" + XSD_URI + "\">\n" +
        "<xs:element name=\"root4\">\n" +
        "<xs:complexType>\n" +
        "<xs:simpleContent>\n" +
        "<xs:extension base=\"xs:QName\"/>\n" +
        "</xs:simpleContent>\n" +
        "</xs:complexType>\n" +
        "</xs:element>\n" +
        "</xs:schema>\n";
    private static final String xml4 = "<root4 xmlns:ns=\"testLoad4\">ns:foo</root4>\n";
    private static final String xml5 =
        "<QNameRoot xmlns=\"test/QName\"\n" +
        "date=\"2004-06\"\n" +
        "qname=\"oly:athens\"\n"+
        "xmlns:oly=\"http://www.olympic.org/\"/>"
        ;
    private static final String xml6 =
        "<root xmlns:xsi=\"" + XSI_URI + "\" xmlns:xs=\"" + XSD_URI + "\">" +
        "<a1 xsi:type=\"xs:QName\" xmlns:ns=\"test1\">ns:value</a1>" +
        "<a2 xsi:type=\"xs:QName\" xmlns=\"test2\">value</a2>" +
        "</root>";

    /* test loading and saving QName */
    public void testQName() throws IOException
    {
        xsdHelper.define(schema4);
        XMLDocument doc = xmlHelper.load(xml4);
        DataObject root = doc.getRootObject();
        assertEquals("testLoad4#foo", root.get("value"));
        root = xmlHelper.load(xml5).getRootObject();
        checkQNames5(root);
        String s = xmlHelper.save(root, "test/QName", "QNameRoot");
        int i1 = s.indexOf("qname=\"");
        int i2 = s.indexOf(":athens\"", i1);
        assertTrue(0 < i1 && i1 < i2);
        root = xmlHelper.load(xml6).getRootObject();
        checkQNames6(root);
    }

    public void testQNameDOM() throws IOException, SAXException
    {
        DataObject root = loadViaDOM(xml5);
        checkQNames5(root);

        // Roundtrip it via DOM
        root = saveAndLoadDOM(root, "test/QName", "QNameRoot");
        checkQNames5(root);

        root = loadViaDOM(xml6);
        checkQNames6(root);

        // Roundtrip it via DOM
        root = saveAndLoadDOM(root, "", "root");
        checkQNames6(root);
    }

    public void testQNameStAX() throws IOException, XmlException, XMLStreamException
    {
        DataObject root = loadViaXMLStreamReader(xml5);
        checkQNames5(root);

        // Save it via XMLStreamWriter
        root = saveAndLoadViaXMLStreamWriter(root, "test/QName", "QNameRoot");
        checkQNames5(root);

        root = loadViaXMLStreamReader(xml6);
        checkQNames6(root);

        // Save it via XMLStreamWriter
        root = saveAndLoadViaXMLStreamWriter(root, "", "root");
        checkQNames6(root);

        // Doesn't work because we don't have an XMLEventReader implementation
//        root = loadViaXMLEventReader(xml5);
//        checkQNames5(root);
        root = xmlHelper.load(xml5).getRootObject();

        // Save it via XMLEventWriter
        root = saveAndLoadViaXMLEventWriter(root, "test/QName", "QNameRoot");
        checkQNames5(root);

        // Doesn't work because we don't have an XMLEventReader implementation
//        root = loadViaXMLEventReader(xml6);
//        checkQNames6(root);
        root = xmlHelper.load(xml6).getRootObject();

        // Save it via XMLEventWriter
        root = saveAndLoadViaXMLEventWriter(root, "", "root");
        checkQNames6(root);

        root = xmlHelper.load(xml5).getRootObject();

        // Save it via XMLStreamReader
        root = saveAndLoadViaXMLStreamReader(root, "test/QName", "QNameRoot");
        checkQNames5(root);

        root = xmlHelper.load(xml6).getRootObject();

        // Save it via XMLStreamReader
        root = saveAndLoadViaXMLStreamReader(root, "", "root");
        checkQNames6(root);
    }

    private void checkQNames5(DataObject dataObject)
    {
        assertEquals(BuiltInTypeSystem.YEARMONTH, dataObject.getInstanceProperty("date").getType());
        assertEquals("2004-06", dataObject.get("date"));
        assertEquals(BuiltInTypeSystem.URI, dataObject.getInstanceProperty("qname").getType());
        assertEquals("http://www.olympic.org/#athens", dataObject.get("qname"));
    }

    private void checkQNames6(DataObject dataObject)
    {
        SequenceXML seq = ((DataObjectXML)dataObject).getSequenceXML();
        assertEquals("a1", seq.getPropertyXML(0).getName());
        assertEquals("test1#value", ((DataObject) seq.getValue(0)).get("value"));
        assertEquals("a2", seq.getPropertyXML(1).getName());
        assertEquals("test2#value", ((DataObject) seq.getValue(1)).get("value"));
    }

    private static final String anyTypeWithSimpleContent1 =
        "<root xsi:type=\"xs:int\" xmlns:xsi=\"" + XSI_URI + "\" xmlns:xs=\"" +
            XSD_URI + "\">20</root>";
    private static final String anyTypeWithSimpleContent2 =
        "<root xmlns:xsi=\"" + XSI_URI + "\" xmlns:xs=\"" + XSD_URI + "\">\n" +
        "<a xsi:type=\"xs:int\">20</a>\n" +
        "<a>abc</a>\n" +
        "<a><b/><c/></a>\n" +
        "</root>\n";
    private static final String anyTypeWithSimpleContent3 =
        "<root xmlns:xsi=\"" + XSI_URI + "\" xmlns:xs=\"" + XSD_URI + "\">\n" +
        "<a>abc</a>\n" +
        "<a><b/><c/></a>\n" +
        "<a xsi:type=\"xs:int\">7</a>\n" +
        "</root>\n";

    private static final String anyTypeWithSimpleContent4 =
        "<sdo:datagraph xmlns:sdo=\"" + SDO_URI + "\" xmlns:xsi=\"" + XSI_URI +
            "\" xmlns:xs=\"" + XSD_URI + "\">\n" +
        "<changeSummary delete=\"#/sdo:datagraph/changeSummary/root[1]/a[2]\">\n" +
            "<root sdo:ref=\"#/sdo:datagraph/root\">\n" +
            "<a sdo:ref=\"#/sdo:datagraph/root/a[1]\"/>\n" +
            "<a><b/><c/></a>\n" +
            "<a sdo:ref=\"#/sdo:datagraph/root/a[2]\"/>\n" +
            "</root>\n" +
        "</changeSummary>\n" +
        "<root>\n" +
        "<a>abc</a>\n" +
        "<a xsi:type=\"xs:int\">7</a>\n" +
        "</root>\n" +
        "</sdo:datagraph>\n";

    private static final String anyTypeWithSimpleContent5 =
        "<sdo:datagraph xmlns:sdo=\"" + SDO_URI + "\" xmlns:xsi=\"" + XSI_URI +
            "\" xmlns:xs=\"" + XSD_URI + "\">\n" +
        "<changeSummary>\n" +
            "<root sdo:ref=\"#/sdo:datagraph/root\">\n" +
            "<a sdo:ref=\"#/sdo:datagraph/root/a[1]\"/>\n" +
            "<a sdo:ref=\"#/sdo:datagraph/root/a[2]\"/>\n" +
            "<a xsi:type=\"xs:int\">7</a>\n" +
            "</root>\n" +
        "</changeSummary>\n" +
        "<root>\n" +
        "<a><b/><c/></a>\n" +
        "<a>abc</a>\n" +
        "</root>\n" +
        "</sdo:datagraph>\n";

    private static final String anyTypeWithSimpleContent6 =
        "<root xmlns:xsi=\"" + XSI_URI + "\" xmlns:xs=\"" + XSD_URI + "\">\n" +
        "<a xsi:type=\"xs:string\">abc</a>\n" +
        "<a xsi:type=\"xs:int\">7</a>\n" +
        "</root>\n";

    public void testAnyTypeWithSimpleContent()
    {
        XMLDocument doc = xmlHelper.load(anyTypeWithSimpleContent1);
        DataObject root = doc.getRootObject();
        Object val = root.get("value");
        assertEquals(20, val);
        StringWriter sw = new StringWriter();

        doc = xmlHelper.load(anyTypeWithSimpleContent2);
        root = doc.getRootObject();
        PropertyXML aProp = (PropertyXML) root.getInstanceProperty("a");
        assertEquals(BuiltInTypeSystem.TYPECODE_DATAOBJECT, aProp.getTypeXML().getTypeCode());
        List aList = root.getList(aProp);
        assertEquals(20, ((DataObject) aList.get(0)).get("value"));
        assertEquals("abc", ((DataObject) aList.get(1)).get("value"));
        assertNotNull(((DataObject) aList.get(2)).get("b"));
        assertNotNull(((DataObject) aList.get(2)).get("c"));

        doc = xmlHelper.load(anyTypeWithSimpleContent3);
        root = doc.getRootObject();
        aProp = (PropertyXML) root.getInstanceProperty("a");
        assertEquals(BuiltInTypeSystem.TYPECODE_DATAOBJECT, aProp.getTypeXML().getTypeCode());
        aList = root.getList(aProp);
        assertEquals("abc", ((DataObject) aList.get(0)).get("value"));
        assertNotNull(((DataObject) aList.get(1)).get("b"));
        assertNotNull(((DataObject) aList.get(1)).get("c"));
        assertEquals(7, ((DataObject) aList.get(2)).get("value"));

        doc = xmlHelper.load(anyTypeWithSimpleContent4);
        root = doc.getRootObject().getDataObject("root[1]");
        aProp = (PropertyXML) root.getInstanceProperty("a");
        assertEquals(BuiltInTypeSystem.TYPECODE_DATAOBJECT, aProp.getTypeXML().getTypeCode());
        aList = root.getList(aProp);
        assertEquals("abc", ((DataObject) aList.get(0)).get("value"));
        assertEquals(7, ((DataObject) aList.get(1)).get("value"));
        try {
            sw = new StringWriter();
            xmlHelper.save(doc, sw, new Options().setSavePrettyPrint());
            assertTrue(sw.toString().indexOf("delete=\"#/sdo:datagraph/changeSummary/root[1]/a[2]\"") > 0);
        } catch (IOException ioe) { /* Can't happen */ }
        root.getChangeSummary().undoChanges();
        aList = root.getList(aProp);
        assertEquals("abc", ((DataObject) aList.get(0)).get("value"));
        assertNotNull(((DataObject) aList.get(1)).get("b"));
        assertNotNull(((DataObject) aList.get(1)).get("c"));
        assertEquals(7, ((DataObject) aList.get(2)).get("value"));

        doc = xmlHelper.load(anyTypeWithSimpleContent5);
        root = doc.getRootObject().getDataObject("root[1]");
        aProp = (PropertyXML) root.getInstanceProperty("a");
        assertEquals(BuiltInTypeSystem.TYPECODE_DATAOBJECT, aProp.getTypeXML().getTypeCode());
        aList = root.getList(aProp);
        assertNotNull(((DataObject) aList.get(0)).get("b"));
        assertNotNull(((DataObject) aList.get(0)).get("c"));
        assertEquals("abc", ((DataObject) aList.get(1)).get("value"));
        root.getChangeSummary().undoChanges();
        aList = root.getList(aProp);
        assertNotNull(((DataObject) aList.get(0)).get("b"));
        assertNotNull(((DataObject) aList.get(0)).get("c"));
        assertEquals("abc", ((DataObject) aList.get(1)).get("value"));
        assertEquals(7, ((DataObject) aList.get(2)).get("value"));

        doc = xmlHelper.load(anyTypeWithSimpleContent6);
        try {
            sw = new StringWriter();
            xmlHelper.save(doc, sw, new Options().setSavePrettyPrint());
            assertTrue(sw.toString().indexOf("\"xs:string\"") > 0);
        } catch (IOException ioe) { /* Can't happen */ }
    }

    private static final String untypedXMLWithNamespace =
        "<?xml version=\"1.0\"?><ld:root xmlns:ld=\"ld:test/foo\"><a>bob</a></ld:root>";

    public void testUntypedXmlWithNamespace()
    {
        XMLDocument doc = xmlHelper.load(untypedXMLWithNamespace);
        DataObject dobj = doc.getRootObject();
        // Wrapping with datagraph and enabling logging...
        final PropertyXML p = ((DataObjectXML)dobj).getContainmentPropertyXML();
        final String localname = p.getName();
        final String namespace = p.getContainingType().getURI();
        DataGraphHelper.wrapWithDataGraph(dobj, namespace, localname);
        dobj.getChangeSummary().beginLogging();
        // Making a change...
        dobj.setString("a", "bill");
        String s = xmlHelper.save(dobj.getRootObject(), "commonj.sdo", "datagraph");
        // Trying to read datagraph back in...
        dobj = xmlHelper.load(s).getRootObject();
        dobj = dobj.getDataObject("root[1]");
        PropertyXML a = (PropertyXML) dobj.getInstanceProperty("a");
        assertEquals("bill", dobj.getList(a).get(0));
        assertEquals("bob", dobj.getChangeSummary().getOldValue(dobj, a).getValue());
    }

    public void testSubstitutions() throws IOException
    {
        XMLDocument doc = xmlHelper.load(getResourceAsStream("checkin", "substGroup.xml"));
        DataObject root = doc.getRootObject();
        assertEquals("root", root.getType().getName());
        assertEquals("base", root.getDataObject("a[1]").getType().getName());
        assertEquals("derived", root.getDataObject("a[2]").getType().getName());
        assertEquals("derived2", root.getDataObject("a[3]").getType().getName());
        String s = xmlHelper.save(root, doc.getRootElementURI(), doc.getRootElementName());
        int i1, i2, i3;
        i1 = s.indexOf(":a>");
        i2 = s.indexOf(":b a=");
        i3 = s.indexOf(":b xsi:type=");
        assertTrue(0 < i1 && i1 < i2 && i2 < i3);
    }

    private static final String CUSTOM_LIST_SCHEMA =
        "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" targetNamespace=\"MarshalTest.testListType\" " +
            "xmlns:sdoJava=\"commonj.sdo/java\" xmlns:tns=\"MarshalTest.testListType\" xmlns:sdo=\"commonj.sdo\">\n" +
        "<xs:simpleType name=\"MyInt\" sdoJava:instanceClass=\"checkin.MarshalTest.MyInt\">\n" +
        "<xs:restriction base=\"xs:int\">\n" +
        "<xs:minInclusive value=\"1000000\"/>\n" +
        "<xs:maxInclusive value=\"1000000\"/>\n" +
        "</xs:restriction>\n" +
        "</xs:simpleType>\n" +
        "\n" +
        "<xs:simpleType name=\"MyListInt\">\n" +
        "<xs:list itemType=\"tns:MyInt\"/>\n" +
        "</xs:simpleType>\n" +
        "\n" +
        "<xs:element name=\"root\">\n" +
        "<xs:complexType>\n" +
        "<xs:sequence>\n" +
        "<xs:element name=\"list\" type=\"tns:MyListInt\"/>\n" +
        "<xs:element name=\"changeSummary\" minOccurs=\"0\" type=\"sdo:ChangeSummaryType\"/>\n" +
        "</xs:sequence>\n" +
        "<xs:attribute name=\"att\" type=\"tns:MyInt\"/>\n" +
        "</xs:complexType>\n" +
        "</xs:element>\n" +
        "</xs:schema>\n";

    private static final String LIST_TYPE_XML =
        "<ns:root xmlns:ns=\"MarshalTest.testListType\" att=\"100\">\n" +
        "<list>1 10 1000 10000 100000 1000000</list>\n" +
        "</ns:root>\n";

    public void testListType() throws IOException
    {
        xsdHelper.define(CUSTOM_LIST_SCHEMA);
        TypeXML listType = (TypeXML) context.getTypeHelper().getType("MarshalTest.testListType","MyListInt");
        assertNotNull(listType);
        TypeXML itemType = listType.getListItemType();
        assertNotNull(itemType);
        assertTrue(itemType.hasCustomizedInstanceClass());
        assertTrue(itemType.getInstanceClass()!=null);
        assertTrue(itemType.getInstanceClass().getName().equals("checkin.MarshalTest$MyInt"));
        XMLDocument doc = xmlHelper.load(LIST_TYPE_XML);
        DataObject root = doc.getRootObject();
        assertEquals("root", root.getType().getName());
        PropertyXML prop = (PropertyXML) root.getInstanceProperty("list");
        List listValue = (List) root.get(prop);
        assertNotNull(listValue);
        assertEquals(6, listValue.size());
        MyInt intValue = (MyInt) root.get("att");
        assertEquals(100, intValue.getValue());
        root.getChangeSummary().beginLogging();
        listValue.remove(listValue.size() - 1);
        listValue.remove(0);
        root.set(prop, listValue);
        StringWriter sw = new StringWriter();
        xmlHelper.save(doc, sw, new Options().setSavePrettyPrint());
        String s = sw.toString();
        assertTrue(s.indexOf("<list>10 1000 10000 100000</list>") > 0);
        // TODO(radup) enable this CR329714
        assertTrue(s.indexOf("<list>1 10 1000 10000 100000 1000000</list>") > 0);
    }

    public static class MyInt
    {
        int value;

        public MyInt(String value) throws NumberFormatException
        {
            this.value = Integer.parseInt(value);
            if (this.value < -1000000 || this.value > 1000000)
                throw new NumberFormatException("Value \"" + value + "\" is out of range");
        }

        public int getValue()
        {
            return value;
        }

        public String toString()
        {
            return String.valueOf(value);
        }
    }

    private static final String EXPECTED_CREATE_XML_FROM_DATA_OBJECTS =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<purchaseOrder orderDate=\"1999-10-20\">\n" +
            "  <shipTo country=\"US\">\n" +
            "    <name>Alice Smith</name>\n" +
            "    <street>123 Maple Street</street>\n" +
            "    <city>Mill Valley</city>\n" +
            "    <state>PA</state>\n" +
            "    <zip>90952</zip>\n" +
            "  </shipTo>\n" +
            "  <billTo country=\"US\">\n" +
            "    <name>Robert Smith</name>\n" +
            "    <street>8 Oak Avenue</street>\n" +
            "    <city>Mill Valley</city>\n" +
            "    <zip>95819</zip>\n" +
            "  </billTo>\n" +
            "  <comment>Hurry, my lawn is going wild!</comment>\n" +
            "  <items>\n" +
            "    <item partNum=\"872-AA\">\n" +
            "      <productName>Lawnmower</productName>\n" +
            "      <quantity>1</quantity>\n" +
            "      <USPrice>148.95</USPrice>\n" +
            "      <comment>Confirm this is electric</comment>\n" +
            "    </item>\n" +
            "    <item partNum=\"926-AA\">\n" +
            "      <productName>Baby Monitor</productName>\n" +
            "      <USPrice>39.98</USPrice>\n" +
            "      <shipDate>1999-05-21</shipDate>\n" +
            "    </item>\n" +
            "  </items>\n" +
            "</purchaseOrder>";
    /*
    public void testCreateXMLFromDataObjects() throws Exception
    {
        File schemaFile = getResourceFile("checkin", "IPOnons.xsd");
        TypeSystem ts = Schema2SDO.createSDOTypeSystem(
            new FileReader(schemaFile), schemaFile.toURL().toString(), null);
        DataFactoryImpl.INSTANCE.setTypeSystem(ts);
        DataObject purchaseOrder =
            DataFactory.INSTANCE.create(null, "PurchaseOrderType");
        purchaseOrder.setString("orderDate", "1999-10-20");
        DataObject shipTo = purchaseOrder.createDataObject("shipTo");
        shipTo.set("country", "US");
        shipTo.set("name", "Alice Smith");
        shipTo.set("street", "123 Maple Street");
        shipTo.set("city", "Mill Valley");
        shipTo.set("state", "CA");
        shipTo.setString("zip", "90952");
        DataObject billTo = purchaseOrder.createDataObject("billTo");
        billTo.set("country", "US");
        billTo.set("name", "Robert Smith");
        billTo.set("street", "8 Oak Avenue");
        billTo.set("city", "Mill Valley");
        shipTo.set("state", "PA");
        billTo.setString("zip", "95819");
        purchaseOrder.set("comment", "Hurry, my lawn is going wild!");
        DataObject items = purchaseOrder.createDataObject("items");
        DataObject item1 = items.createDataObject("item");
        item1.set("partNum", "872-AA");
        item1.set("productName", "Lawnmower");
        item1.setInt("quantity", 1);
        item1.setString("usPrice", "148.95");
        item1.set("comment", "Confirm this is electric");
        DataObject item2 = items.createDataObject("item");
        item2.set("partNum", "926-AA");
        item2.set("productName", "Baby Monitor");
        item2.setInt("quantity", 1);
        item2.setString("usPrice", "39.98");
        item2.setString("shipDate", "1999-05-21");
        String result = xmlHelper.save(purchaseOrder, null, "purchaseOrder");
        assertEquals(EXPECTED_CREATE_XML_FROM_DATA_OBJECTS, result);
    }
    */

    private static final String BILL_TO_XML =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
        "<billTo country=\"US\"\n" +
        "  <name>Robert Smith</name>\n" +
        "  <street>8 Oak Avenue</street>\n" +
        "  <city>Mill Valley</city>\n" +
        "  <zip>95819</zip>\n" +
        "</billTo>";
    /*
    public void testCreateDataObjectTreesFromXMLdocuments()
    {
        String shipToXML =
            "<shipTo country='US'>" +
            "  <name>Alice Smith</name>" +
            "  <street>123 Maple Street</street>" +
            "  <city>Mill Valley</city>" +
            "  <state>PA</state>" +
            "  <zip>90952</zip>" +
            "</shipTo>";

        DataObject shipTo = xmlHelper.load(shipToXML).getRootObject();
        DataObject billTo = shipTo;
        billTo.setString("name", "Robert Smith");
        billTo.setString("street", "8 Oak Avenue");
        billTo.setString("state", null);
        billTo.setString("zip", "95819");
        String billToXML = xmlHelper.save(billTo, null, "billTo");
        assertEquals(BILL_TO_XML, billToXML);
    }
    */

    public static DataObject loadViaDOM(String xml) throws IOException, SAXException
    {
        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        f.setNamespaceAware(true);
        DocumentBuilder parser = null;
        try
        { parser = f.newDocumentBuilder(); }
        catch (ParserConfigurationException pce)
        { /* Shouldn't happen for us */}

        Document doc = parser.parse(new InputSource(new StringReader(xml)));
        DOMSource s = new DOMSource(doc);
        return xmlHelper.load(s, null, null).getRootObject();
    }

    public static String saveViaDOM(DataObject root, String uri, String name) throws IOException,
        TransformerException
    {
        XMLDocument doc = xmlHelper.createDocument(root, uri, name);
        doc.setXMLDeclaration(false);
        DOMResult r = new DOMResult();
        xmlHelper.save(doc, r, new Options().setSavePrettyPrint());
        Node n = r.getNode();
        TransformerFactory f = TransformerFactory.newInstance();
        f.setAttribute("indent-number", 4);
        Transformer t = f.newTransformer();
        StringWriter sw = new StringWriter();
        t.transform(new DOMSource(n), new StreamResult(sw));
        return sw.toString();
    }

    public static DataObject saveAndLoadDOM(DataObject root, String uri, String name)
        throws IOException
    {
        XMLDocument doc = xmlHelper.createDocument(root, uri, name);
        DOMResult r = new DOMResult();
        xmlHelper.save(doc, r, null);
        Node n = r.getNode();
        DOMSource s = new DOMSource(n);
        return xmlHelper.load(s, null, null).getRootObject();
    }

    public static DataObject loadViaXMLStreamReader(String xml) throws IOException, XmlException
    {
        XmlObject o = XmlObject.Factory.parse(xml);
        XMLStreamReader xsReader = o.newXMLStreamReader();
        XMLDocument doc = ((davos.sdo.impl.helpers.XMLHelperImpl) xmlHelper).load(xsReader, null, null);
        return doc.getRootObject();
    }

    // Disabled because it needs an StAX-compliant parser
//    public static DataObject loadViaXMLEventReader(String xml) throws IOException, XmlException, XMLStreamException
//    {
//        XMLInputFactory f = XMLInputFactory.newInstance();
//        XmlObject o = XmlObject.Factory.parse(xml);
//        XMLEventReader xeReader = f.createXMLEventReader(o.newXMLStreamReader());
//        return ((davos.sdo.impl.helpers.XMLHelperImpl) xmlHelper).load(xeReader, null, null).getRootObject();
//    }

    public static String saveViaXMLStreamWriter(DataObject root, String uri, String name)
        throws IOException, XMLStreamException
    {
        XMLOutputFactory f = XMLOutputFactory.newInstance();
        StringWriter sw = new StringWriter();
        XMLStreamWriter xsw = f.createXMLStreamWriter(sw);
        XMLDocument doc = xmlHelper.createDocument(root, uri, name);
        doc.setXMLDeclaration(false);
        ((davos.sdo.impl.helpers.XMLHelperImpl) xmlHelper).save(doc, xsw,
            new Options().setSavePrettyPrint());
        return sw.toString();
    }

    public static DataObject saveAndLoadViaXMLStreamWriter(DataObject root, String uri, String name)
        throws IOException, XMLStreamException
    {
        String s = saveViaXMLStreamWriter(root, uri, name);
        return xmlHelper.load(s).getRootObject();
    }

    public static String saveViaXMLEventWriter(DataObject root, String uri, String name)
        throws IOException, XMLStreamException
    {
        XMLOutputFactory f = XMLOutputFactory.newInstance();
        StringWriter sw = new StringWriter();
        XMLEventWriter xsw = f.createXMLEventWriter(sw);
        XMLDocument doc = xmlHelper.createDocument(root, uri, name);
        doc.setXMLDeclaration(false);
        ((davos.sdo.impl.helpers.XMLHelperImpl) xmlHelper).save(doc, xsw,
            new Options().setSavePrettyPrint());
        return sw.toString();
    }

    public static DataObject saveAndLoadViaXMLEventWriter(DataObject root, String uri, String name)
        throws IOException, XMLStreamException
    {
        String s = saveViaXMLEventWriter(root, uri, name);
        return xmlHelper.load(s).getRootObject();
    }

    public static DataObject saveAndLoadViaXMLStreamReader(DataObject root, String uri, String name)
        throws IOException, XMLStreamException
    {
        XMLDocument doc = xmlHelper.createDocument(root, uri, name);
        doc.setXMLDeclaration(false);
        XMLStreamReader xsr = ((davos.sdo.impl.helpers.XMLHelperImpl) xmlHelper).save(doc, new Options().setSavePrettyPrint());
        XMLDocument doc2 = ((davos.sdo.impl.helpers.XMLHelperImpl) xmlHelper).load(xsr, null, null);
        assertFalse(doc2.isXMLDeclaration());
        return doc2.getRootObject();
    }
}
