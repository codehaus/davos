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
package marshal;

import java.io.*;
import java.util.List;
import java.util.Enumeration;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.Result;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.w3c.dom.Node;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.NamespaceSupport;

import javax.sdo.*;
import javax.sdo.helper.*;
import davos.sdo.Options;
import davos.sdo.impl.helpers.XMLHelperImpl;

import junit.framework.*;
import common.BaseTest;

/**
 * This class tests the load and save methods provided by XMLHelper.
 * @author Wing Yew Poon
 */
public class XMLHelperTest extends BaseTest
{
    public XMLHelperTest(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        
        TestSuite suite = new TestSuite();
        
        suite.addTest(new XMLHelperTest("testLoadFromString"));
        suite.addTest(new XMLHelperTest("testLoadFromStream"));
        suite.addTest(new XMLHelperTest("testLoadFromReader"));
        suite.addTest(new XMLHelperTest("testLoadFromStreamSource"));
        suite.addTest(new XMLHelperTest("testLoadFromSAXSource"));
        suite.addTest(new XMLHelperTest("testLoadFromDOMSource"));
        suite.addTest(new XMLHelperTest("testLoadFromXMLStreamReader1"));
        //suite.addTest(new XMLHelperTest("testLoadFromXMLStreamReader2a"));
        //suite.addTest(new XMLHelperTest("testLoadFromXMLStreamReader2b"));
        suite.addTest(new XMLHelperTest("testLoadFromXMLEventReader1"));
        //suite.addTest(new XMLHelperTest("testLoadFromXMLEventReader2a"));
        //suite.addTest(new XMLHelperTest("testLoadFromXMLEventReader2b"));
        suite.addTest(new XMLHelperTest("testLoadXMLHeaderFromDOMSource"));
        suite.addTest(new XMLHelperTest("testLoadXMLHeaderFromXMLStreamReader"));
        suite.addTest(new XMLHelperTest("testLoadXMLHeaderFromXMLEventReader"));
        suite.addTest(new XMLHelperTest("testSaveToString"));
        suite.addTest(new XMLHelperTest("testSaveToStream"));
        suite.addTest(new XMLHelperTest("testSerializeToStream"));
        suite.addTest(new XMLHelperTest("testSerializeToWriter"));
        suite.addTest(new XMLHelperTest("testSerializeToStreamResult"));
        suite.addTest(new XMLHelperTest("testSerializeToSAXResult"));
        suite.addTest(new XMLHelperTest("testSerializeToDOMResult"));
        suite.addTest(new XMLHelperTest("testSerializeToXMLStreamWriter"));
        suite.addTest(new XMLHelperTest("testSerializeToXMLEventWriter"));
        suite.addTest(new XMLHelperTest("testSaveAndLoadViaXMLStreamReader"));
        suite.addTest(new XMLHelperTest("testLoadAndSave"));
        suite.addTest(new XMLHelperTest("testLoadAndSaveSAX"));
        suite.addTest(new XMLHelperTest("testCreateDocument"));
        
        // or
        //TestSuite suite = new TestSuite(XMLHelperTest.class);
        return suite;
    }

    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(suite());
    }

    public static File dir;
    static
    {
        dir = new File(OUTPUTROOT + S + "marshal");
        dir.mkdirs();
    }

    private static XMLHelper xmlHelper = context.getXMLHelper();
    private static TypeHelper typeHelper = context.getTypeHelper();
    private static DataFactory dataFactory = context.getDataFactory();
    private static EqualityHelper equalityHelper = context.getEqualityHelper();

    private static final String xml0 =
        "<?xml version='1.0'?>" + newline +
        "<a><b/><c>xxx</c></a>";
    private static final String xml0b =
        "<?xml version='1.0' encoding='UTF-8'?>" + newline +
        "<a><b/><c>xxx</c></a>";
    private static final String xml0c =
        "<?xml version='1.0' encoding='ISO-8859-1'?>" + newline +
        "<a><b/><c>xxx</c></a>";
    private static final String xml1 =
        "<bas:a xmlns:bas=\"http://sdo/test/basic0\">" + newline +
        "    <uri1>http://www.w3.org/2001/XMLSchema#decimal</uri1>" + newline +
        "    <uri2 xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">xs:decimal</uri2>" + newline +
        "</bas:a>";
    // minimal escaping
    private static final String xml2a =
        "<char:testList xmlns:char=\"chartest.xsd\">" + newline +
            "  <test>This is a greater than sign: ></test>" + newline +
            "  <test>This is a less than sign: &lt;</test>" + newline +
            "  <test esc=\"'\">This is a single quote: '</test>" + newline +
            "  <test esc=\"&quot;\">This is a double quote: \"</test>" + newline +
            "  <test>\u03B1&amp;\u03C9</test>" + newline +
            "  <test>Character data may not contain the three-character sequence ]]&gt; with the > unescaped.</test>" + newline +
            "  <test>In particular, character data in a CDATA section may not contain the three-character sequence ]]&amp;gt; with the > unescaped.</test>" + newline +
        "</char:testList>";
    // escape '>' as well
    private static final String xml2b =
        "<char:testList xmlns:char=\"chartest.xsd\">" + newline +
            "  <test>This is a greater than sign: &gt;</test>" + newline +
            "  <test>This is a less than sign: &lt;</test>" + newline +
            "  <test esc=\"'\">This is a single quote: '</test>" + newline +
            "  <test esc=\"&quot;\">This is a double quote: \"</test>" + newline +
            "  <test>\u03B1&amp;\u03C9</test>" + newline +
            "  <test>Character data may not contain the three-character sequence ]]&gt; with the &gt; unescaped.</test>" + newline +
            "  <test>In particular, character data in a CDATA section may not contain the three-character sequence ]]&amp;gt; with the &gt; unescaped.</test>" + newline +
        "</char:testList>";
    private static final String xml3 =
        "<root xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
        "xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">" +
        "<a1 xsi:type=\"xs:QName\" xmlns:ns=\"test1\">ns:value</a1>" +
        "<a2 xsi:type=\"xs:QName\" xmlns=\"test2\">value</a2>" +
        "</root>";

    private void verifyDO1(DataObject a)
    {
        Type t = a.getType();
        assertEquals("http://sdo/test/basic0", t.getURI());
        assertEquals("A", t.getName());
        assertEquals("http://www.w3.org/2001/XMLSchema#decimal", a.get("uri1"));
        assertEquals("http://www.w3.org/2001/XMLSchema#decimal", a.get("uri2"));
    }

    private void _verifyDO2(DataObject root)
    {
        List testList = root.getList("test");
        assertNotNull(testList);
        assertEquals(7, testList.size());
        Object test1Value = root.get("test[1]/value");
        assertNotNull(test1Value);
        assertEquals("This is a greater than sign: >", test1Value);
        Object test2Value = root.get("test[2]/value");
        assertNotNull(test2Value);
        assertEquals("This is a less than sign: <", test2Value);
        DataObject test3 = root.getDataObject("test[3]");
        assertNotNull(test3);
        assertEquals("'", test3.get("esc"));
        assertEquals("This is a single quote: '", test3.get("value"));
        DataObject test4 = root.getDataObject("test[4]");
        assertNotNull(test4);
        assertEquals("\"", test4.get("esc"));
        assertEquals("This is a double quote: \"", test4.get("value"));
        Object test6Value = root.get("test[6]/value");
        assertNotNull(test6Value);
        assertEquals("Character data may not contain the three-character sequence ]]> with the > unescaped.", test6Value);
        Object test7Value = root.get("test[7]/value");
        assertNotNull(test7Value);
        assertEquals("In particular, character data in a CDATA section may not contain the three-character sequence ]]&gt; with the > unescaped.", test7Value);
    }

    private void verifyDO2a(DataObject root)
    {
        _verifyDO2(root);
        Object test5Value = root.get("test[5]/value");
        assertNotNull(test5Value);
        assertEquals("\u03B1&\u03C9", test5Value);
    }

    private void verifyDO2b(DataObject root)
    {
        _verifyDO2(root);
        Object test5Value = root.get("test[5]/value");
        assertNotNull(test5Value);
        assertEquals("&alpha;&&omega;", test5Value);
    }

    public void testLoadFromString()
    {
        // load(String) -> XMLDocument
        XMLDocument doc = xmlHelper.load(xml1);
        DataObject root = doc.getRootObject();
        verifyDO1(root);
    }

    public void testLoadFromStream() throws IOException
    {
        // load(InputStream) -> XMLDocument
        // what about load(InputStream input, String locationURI, Object options)?
        // what role does the locationURI play?
        InputStream in = new ByteArrayInputStream(xml1.getBytes());
        XMLDocument doc = xmlHelper.load(in, null, null);
        in.close();
        DataObject root = doc.getRootObject();
        verifyDO1(root);

        // provide locationURI, external entities are resolved
        File f = getResourceFile("marshal", "chartest.xml");
        in = new FileInputStream(f);
        doc = xmlHelper.load(in, f.toURL().toString(), null);
        in.close();
        root = doc.getRootObject();
        verifyDO2a(root);

        // do not provide locationURI, external entities cannot be resolved
        in = new FileInputStream(f);
        doc = null;
        try
        {
            doc = xmlHelper.load(in, null, null);
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
            assertTrue(e.getMessage().indexOf("greek.ent") >= 0);
        }
        finally
        {
            in.close();
        }
        if (doc != null)
        {
            System.out.println("xml got parsed");
            root = doc.getRootObject();
            verifyDO2b(root);
        }        
    }

    public void testLoadFromReader() throws IOException
    {
        // load(Reader input, String locationURI, Object options) -> XMLDocument
        // what role does the locationURI play?
        Reader reader = new StringReader(xml1);
        XMLDocument doc = xmlHelper.load(reader, null, null);
        reader.close();
        DataObject root = doc.getRootObject();
        verifyDO1(root);

        // provide locationURI, external entities are resolved
        File f = getResourceFile("marshal", "chartest.xml");
        reader = new FileReader(f);
        doc = xmlHelper.load(reader, f.toURL().toString(), null);
        reader.close();
        root = doc.getRootObject();
        verifyDO2a(root);

        // do not provide locationURI, external entities cannot be resolved
        reader = new FileReader(f);
        doc = null;
        try
        {
            doc = xmlHelper.load(reader, null, null);
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
            assertTrue(e.getMessage().indexOf("greek.ent") >= 0);
        }
        finally
        {
            reader.close();
        }
        if (doc != null)
        {
            System.out.println("xml got parsed");
            root = doc.getRootObject();
            verifyDO2b(root);
        }
    }

    public void testLoadFromStreamSource() throws IOException
    {
        Source source = new StreamSource(new StringReader(xml1));
        DataObject root = xmlHelper.load(source, null, null).getRootObject();
        verifyDO1(root);

        // provide locationURI, external entities are resolved
        File f = getResourceFile("marshal", "chartest.xml");
        InputStream in = new FileInputStream(f);
        // f.toURL().toString() returns the same String as f.toURL().toExternalForm()
        // even if source is constructed using a systemId, the systemId
        // does not get used by XMLHelper.load(); only locationURI is used
        //source = new StreamSource(in, f.toURL().toExternalForm());
        source = new StreamSource(in);
        System.out.println(source.getSystemId());
        XMLDocument doc = xmlHelper.load(source, f.toURL().toString(), null);
        in.close();
        root = doc.getRootObject();
        verifyDO2a(root);

        // do not provide locationURI, external entities cannot be resolved
        in = new FileInputStream(f);
        source = new StreamSource(in);
        doc = null;
        try
        {
            doc = xmlHelper.load(source, null, null);
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
            assertTrue(e.getMessage().indexOf("greek.ent") >= 0);
        }
        finally
        {
            in.close();
        }
        if (doc != null)
        {
            System.out.println("xml got parsed");
            root = doc.getRootObject();
            verifyDO2b(root);
        }
    }

    public void testLoadFromSAXSource() throws IOException
    {
        InputSource is = new InputSource(new StringReader(xml1));
        Source source = new SAXSource(is);
        DataObject root = xmlHelper.load(source, null, null).getRootObject();
        verifyDO1(root);

        // provide locationURI, external entities are resolved
        File f = getResourceFile("marshal", "chartest.xml");
        InputStream in = new FileInputStream(f);
        is = new InputSource(in);
        //is.setSystemId(f.toURL().toString());
        source = new SAXSource(is);
        //System.out.println(source.getSystemId());
        // NOTE: if systemId is set load still succeeds if locationURI is null
        XMLDocument doc = xmlHelper.load(source, f.toURL().toString(), null);
        in.close();
        root = doc.getRootObject();
        verifyDO2a(root);

        // do not provide locationURI, external entities cannot be resolved
        in = new FileInputStream(f);
        is = new InputSource(in); // do not set systemId
        source = new SAXSource(is);
        doc = null;
        try
        {
            doc = xmlHelper.load(source, null, null);
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
            assertTrue(e.getMessage().indexOf("greek.ent") >= 0);
        }
        finally
        {
            in.close();
        }
        if (doc != null)
        {
            System.out.println("xml got parsed");
            root = doc.getRootObject();
            verifyDO2b(root);
        }
    }

    public void testLoadFromDOMSource()
        throws ParserConfigurationException, SAXException, IOException
    {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true); // false by default
        DocumentBuilder parser = factory.newDocumentBuilder(); // throws ParserConfigurationException
        InputSource is = new InputSource(new StringReader(xml1));
        Document node = parser.parse(is); // throws SAXException, IOException
        Source source = new DOMSource(node);
        DataObject root = xmlHelper.load(source, null, null).getRootObject();
        verifyDO1(root);

        // provide locationURI, external entities are resolved
        File f = getResourceFile("marshal", "chartest.xml");
        InputStream in = new FileInputStream(f);
        is = new InputSource(in);
        // if systemId is not set on the InputSource, 
        // the parser will not be able to parse it successfully
        is.setSystemId(f.toURL().toString());
        node = parser.parse(is);
        source = new DOMSource(node);
        //System.out.println(source.getSystemId()); // null
        // NOTE: load still succeeds if locationURI is null
        XMLDocument doc = xmlHelper.load(source, /*f.toURL().toString()*/null, null);
        in.close();
        root = doc.getRootObject();
        verifyDO2a(root);
    }

    /* StAX loading: XMLStreamReader */
    public void testLoadFromXMLStreamReader1()
        throws XMLStreamException, IOException
    {
        XMLInputFactory xmlif = XMLInputFactory.newInstance();
        Reader reader = new StringReader(xml1);
        XMLStreamReader xmlsr = xmlif.createXMLStreamReader(reader);
        DataObject root = ((XMLHelperImpl)xmlHelper).load(xmlsr, null, null).getRootObject();
        reader.close(); xmlsr.close();
        verifyDO1(root);
    }

    public void testLoadFromXMLStreamReader2a()
        throws XMLStreamException, IOException
    {
        XMLInputFactory xmlif = XMLInputFactory.newInstance();
        File f = getResourceFile("marshal", "chartest.xml");
        Reader reader = new FileReader(f);
        // create XMLStreamReader with systemId
        XMLStreamReader xmlsr = xmlif.createXMLStreamReader(f.toURL().toString(), reader);
        XMLDocument doc = ((XMLHelperImpl)xmlHelper).load(xmlsr, f.toURL().toString(), null);
        reader.close(); xmlsr.close();
        DataObject root = doc.getRootObject();
        verifyDO2a(root);
    }

    public void testLoadFromXMLStreamReader2b()
        throws XMLStreamException, IOException
    {
        XMLInputFactory xmlif = XMLInputFactory.newInstance();
        File f = getResourceFile("marshal", "chartest2.xml");
        Reader reader = new FileReader(f);
        // create XMLStreamReader with systemId
        XMLStreamReader xmlsr = xmlif.createXMLStreamReader(f.toURL().toString(), reader);
        XMLDocument doc = ((XMLHelperImpl)xmlHelper).load(xmlsr, f.toURL().toString(), null);
        reader.close(); xmlsr.close();
        DataObject root = doc.getRootObject();
        verifyDO2a(root);
    }

    /* StAX loading XMLEventReader */
    public void testLoadFromXMLEventReader1()
        throws XMLStreamException, IOException
    {
        XMLInputFactory xmlif = XMLInputFactory.newInstance();
        Reader reader = new StringReader(xml1);
        XMLEventReader xmler = xmlif.createXMLEventReader(reader);
        DataObject root = ((XMLHelperImpl)xmlHelper).load(xmler, null, null).getRootObject();
        reader.close(); xmler.close();
        verifyDO1(root);
    }

    public void testLoadFromXMLEventReader2a()
        throws XMLStreamException, IOException
    {
        XMLInputFactory xmlif = XMLInputFactory.newInstance();
        File f = getResourceFile("marshal", "chartest.xml");
        Reader reader = new FileReader(f);
        XMLEventReader xmler = xmlif.createXMLEventReader(f.toURL().toString(), reader);
        XMLDocument doc = ((XMLHelperImpl)xmlHelper).load(xmler, f.toURL().toString(), null);
        reader.close(); xmler.close();
        DataObject root = doc.getRootObject();
        verifyDO2a(root);
    }

    public void testLoadFromXMLEventReader2b()
        throws XMLStreamException, IOException
    {
        XMLInputFactory xmlif = XMLInputFactory.newInstance();
        File f = getResourceFile("marshal", "chartest2.xml");
        Reader reader = new FileReader(f);
        XMLEventReader xmler = xmlif.createXMLEventReader(f.toURL().toString(), reader);
        XMLDocument doc = ((XMLHelperImpl)xmlHelper).load(xmler, f.toURL().toString(), null);
        reader.close(); xmler.close();
        DataObject root = doc.getRootObject();
        verifyDO2a(root);
    }

    // test loading xml header using DOM and StAX

    public void testLoadXMLHeaderFromDOMSource()
        throws ParserConfigurationException, SAXException, IOException
    {
        System.out.println("testLoadXMLHeaderFromDOMSource()");
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true); // false by default
        DocumentBuilder parser = factory.newDocumentBuilder(); // throws ParserConfigurationException
        InputSource is = new InputSource(new StringReader(xml0b));
        Document node = parser.parse(is); // throws SAXException, IOException
        Source source = new DOMSource(node);
        XMLDocument doc = xmlHelper.load(source, null, null);
        assertTrue(doc.isXMLDeclaration());
        assertEquals("1.0", doc.getXMLVersion());
        assertEquals("UTF-8", doc.getEncoding());
    }

    public void testLoadXMLHeaderFromXMLStreamReader()
        throws XMLStreamException, IOException
    {
        System.out.println("testLoadXMLHeaderFromXMLStreamReader()");
        XMLInputFactory xmlif = XMLInputFactory.newInstance();
        Reader reader = new StringReader(xml0);
        XMLStreamReader xmlsr = xmlif.createXMLStreamReader(reader);
        XMLDocument doc = ((XMLHelperImpl)xmlHelper).load(xmlsr, null, null);
        reader.close(); xmlsr.close();
        assertTrue(doc.isXMLDeclaration());
        System.out.println(doc.getXMLVersion());
        System.out.println(doc.getEncoding());
        assertEquals("1.0", doc.getXMLVersion());
        // encoding is null if not specified
        assertNull(doc.getEncoding());
    }

    public void testLoadXMLHeaderFromXMLEventReader()
        throws XMLStreamException, IOException
    {
        System.out.println("testLoadXMLHeaderFromXMLEventReader()");
        XMLInputFactory xmlif = XMLInputFactory.newInstance();
        Reader reader = new StringReader(xml0c);
        XMLEventReader xmler = xmlif.createXMLEventReader(reader);
        XMLDocument doc = ((XMLHelperImpl)xmlHelper).load(xmler, null, null);
        reader.close(); xmler.close();
        assertTrue(doc.isXMLDeclaration());
        System.out.println(doc.getXMLVersion());
        System.out.println(doc.getEncoding());
        assertEquals("1.0", doc.getXMLVersion());
        assertEquals("ISO-8859-1", doc.getEncoding());
    }

    public void testSaveToString()
    {
        // save(DataObject obj, String rootElementURI, String rootElementName)
        // -> String
    }

    public void testSaveToStream() throws IOException
    {
        // save(DataObject obj, String rootElementURI, String rootElementName,
        //      OutputStream output)
        String xml_in = "<a><b/><c>xxx</c></a>";
        XMLDocument doc = xmlHelper.load(xml_in);
        DataObject obj = doc.getRootObject();
        File f = new File(dir, "abc1.xml");
        OutputStream out = new FileOutputStream(f);
        xmlHelper.save(obj, "", "a", out);
        //out.flush();
        out.close();
        String xml_out = xml_in + newline;
            //"<a>" + newline +
            //"    <b/>" + newline +
            //"    <c>xxx</c>" + newline +
            //"</a>" + newline;
        BufferedReader br = new BufferedReader(new FileReader(f));
        StringBuffer sb = new StringBuffer();
        String line = null;
        while ((line = br.readLine()) != null)
        {
            sb.append(line).append(newline);
        }
        assertEquals(xml_out, sb.toString());
    }

    public void testSerializeToStream() throws IOException
    {
        // save(XMLDocument doc, OutputStream output, Object options)
        String xml_in = "<a><b/><c>xxx</c></a>";
        XMLDocument doc = xmlHelper.load(xml_in);
        File f = new File(dir, "abc2.xml");
        OutputStream out = new FileOutputStream(f);
        xmlHelper.save(doc, out, null);
        out.close();
        String xml_out = xml_in + newline;
            //"<a>" + newline +
            //"    <b/>" + newline +
            //"    <c>xxx</c>" + newline +
            //"</a>" + newline;
        BufferedReader br = new BufferedReader(new FileReader(f));
        StringBuffer sb = new StringBuffer();
        String line = null;
        while ((line = br.readLine()) != null)
        {
            sb.append(line).append(newline);
        }
        assertEquals(xml_out, sb.toString());
    }

    public void testSerializeToWriter() throws IOException
    {
        // save(XMLDocument doc, Writer output, Object options)
        String xml_in = "<a><b/><c>xxx</c></a>";
        XMLDocument doc = xmlHelper.load(xml_in);
        Writer out = new StringWriter();
        xmlHelper.save(doc, out, null);
        out.flush();
        String xml_out = xml_in;
            //"<a>" + newline +
            //"    <b/>" + newline +
            //"    <c>xxx</c>" + newline +
            //"</a>"; // + newline;
        assertEquals(xml_out, out.toString());
    }

    private DataObject createDO1()
    {
        Type t = typeHelper.getType("http://sdo/test/basic0", "A");
        DataObject a = dataFactory.create(t);
        assertNotNull(a);
        a.set("uri1", "http://www.w3.org/2001/XMLSchema#decimal");
        a.set("uri2", "http://www.w3.org/2001/XMLSchema#decimal");
        return a;
    }

    public void testSerializeToStreamResult() throws IOException
    {
        DataObject a = createDO1();
        StreamResult result = new StreamResult(new StringWriter());
        XMLDocument doc = xmlHelper.createDocument(a, "http://sdo/test/basic0", "a");
        xmlHelper.save(doc, result, new Options().setSavePrettyPrint());
        StringWriter sw = (StringWriter)result.getWriter();
        System.out.println(sw.toString());
        assertEquals(XML_HEADER + newline + xml1, sw.toString());
    }

    public void testSerializeToSAXResult() throws IOException
    {
        System.out.println("testSerializeToSAXResult()");
        DataObject a = createDO1();
        Writer out = new StringWriter();
        ContentHandler handler = new SimpleHandler(out);
        SAXResult result = new SAXResult(handler);
        XMLDocument doc1 = xmlHelper.createDocument(a, "http://sdo/test/basic0", "a");
        xmlHelper.save(doc1, result, null);
        assertEquals(xml1.replaceAll(">[ \r\n]*<", "><"),
                     out.toString());
        StringBuffer sb = ((StringWriter)out).getBuffer();
        sb.delete(0, sb.length());
        XMLDocument doc2 = xmlHelper.createDocument(a, "http://sdo/test/basic0", "a");
        //doc2.setXMLDeclaration(false);
        xmlHelper.save(doc2, result, new Options().setSavePrettyPrint());
        Writer fw = new FileWriter(new File(dir, "sax.xml"));
        fw.write(out.toString());
        fw.close();
        assertEquals(xml1,
                     out.toString());
    }

    public void testSerializeToDOMResult() throws IOException
    {
        DataObject a = createDO1();
        DOMResult result = new DOMResult();
        XMLDocument doc = xmlHelper.createDocument(a, "http://sdo/test/basic0", "a");
        xmlHelper.save(doc, result, null);
        Node node = result.getNode();
        DOMSource source = new DOMSource(node);
        DataObject root = xmlHelper.load(source, null, null).getRootObject();
        verifyDO1(root);
    }

    /* StAX saving: XMLStreamWriter */
    public void testSerializeToXMLStreamWriter()
        throws XMLStreamException, IOException
    {
        XMLOutputFactory xmlof =  XMLOutputFactory.newInstance();
        Writer out = new StringWriter();
        //Writer out = new FileWriter(new File(dir, "xmlsw1.xml"));
        XMLStreamWriter xmlsw = xmlof.createXMLStreamWriter(out);
        DataObject a = createDO1();
        XMLDocument doc = xmlHelper.createDocument(a, "http://sdo/test/basic0", "a");
        doc.setXMLDeclaration(false);
        ((XMLHelperImpl)xmlHelper).save(doc, xmlsw, 
                                        null); 
                                        //new Options().setSavePrettyPrint());
        out.close(); xmlsw.close();
        assertEquals(xml1.replaceAll(">[ \r\n]*<", "><"), 
                     ((StringWriter)out).toString());
    }

    /* StAX saving: XMLEventWriter */
    public void testSerializeToXMLEventWriter()
        throws XMLStreamException, IOException
    {
        XMLOutputFactory xmlof =  XMLOutputFactory.newInstance();
        Writer out = new StringWriter();
        //Writer out = new FileWriter(new File(dir, "xmlew1.xml"));
        XMLEventWriter xmlew = xmlof.createXMLEventWriter(out);
        DataObject a = createDO1();
        XMLDocument doc = xmlHelper.createDocument(a, "http://sdo/test/basic0", "a");
        doc.setXMLDeclaration(false);
        ((XMLHelperImpl)xmlHelper).save(doc, xmlew, 
                                        null); 
                                        //new Options().setSavePrettyPrint());
        out.close(); xmlew.close();
        assertEquals(xml1.replaceAll(">[ \r\n]*<", "><"), 
                     ((StringWriter)out).toString());
    }

    /* StAX saving: XMLStreamReader */
    public void testSaveAndLoadViaXMLStreamReader()
        throws XMLStreamException, IOException
    {
        System.out.println("testSaveAndLoadViaXMLStreamReader()");
        DataObject a = createDO1();
        XMLDocument doc = xmlHelper.createDocument(a, "http://sdo/test/basic0", "a");
        //doc.setXMLDeclaration(false);
        XMLStreamReader xmlsr = 
            ((XMLHelperImpl)xmlHelper).save(doc,
                                            null);
                                            //new Options().setSavePrettyPrint());
        XMLDocument doc2 = ((XMLHelperImpl)xmlHelper).load(xmlsr, null, null);
        //assertFalse(doc2.isXMLDeclaration());
        DataObject a2 = doc2.getRootObject();
        verifyDO1(a2);
        assertTrue(equalityHelper.equal(a, a2));
        xmlsr.close();
    }

    public void testLoadAndSave() throws IOException
    {
        System.out.println("testLoadAndSave()");
        File f = getResourceFile("marshal", "chartest.xml");
        Reader r = new FileReader(f);
        XMLDocument doc = xmlHelper.load(r, f.toURL().toString(), null);
        r.close();
        assertTrue(doc.isXMLDeclaration());
        doc.setXMLDeclaration(false);
        Writer w = new StringWriter();
        Options o = new Options().setSavePrettyPrint().setSaveIndent(2);
        xmlHelper.save(doc, w, o);
        FileWriter fw = new FileWriter(new File(dir, "chartest.xml"));
        xmlHelper.save(doc, fw, o);
        try
        {
            assertEquals(xml2a, ((StringWriter)w).toString());
        }
        catch (ComparisonFailure e)
        {
            assertEquals(xml2b, ((StringWriter)w).toString());
        }
    }

    public void testLoadAndSaveSAX() throws IOException
    {
        System.out.println("testLoadAndSaveSAX()");
        File f = getResourceFile("marshal", "chartest.xml");
        InputSource in = new InputSource(new FileReader(f));
        Source source = new SAXSource(in);
        XMLDocument doc = xmlHelper.load(source, f.toURL().toString(), null);
        Writer out = new StringWriter();
        ContentHandler handler = new SimpleHandler(out);
        SAXResult result = new SAXResult(handler);
        Options o = new Options().setSavePrettyPrint().setSaveIndent(2);
        xmlHelper.save(doc, result, o);
        System.out.print(out.toString());
        try
        {
            assertEquals(xml2a, out.toString());
        }
        catch (ComparisonFailure e)
        {
            assertEquals(xml2b, out.toString());
        }
    }

    public void testCreateDocument()
    {
        // createDocument(DataObject obj, String rootElementURI, String rootElementName) -> XMLDocument
    }

    public static class SimpleHandler extends DefaultHandler
    {
        private static final String LT = "&lt;";
        private static final String GT = "&gt;";
        private static final String AMP = "&amp;";
        private static final String QUOT = "&quot;";

        private static Writer out;
        private NamespaceSupport namespaces;
        private boolean needNewContext;
        private boolean needNsDecl;

        private int i = 0;

        public SimpleHandler(Writer out)
        {
            this.out = out;
        }

        //===========================================================
        // SAX DocumentHandler methods
        //===========================================================

        public void startDocument()
            throws SAXException
        {
            //emit("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            //nl();
            namespaces = new NamespaceSupport();
            needNewContext = true;
        }

        public void endDocument()
            throws SAXException
        {
            try
            {
                //nl();
                out.flush();
            }
            catch (IOException e)
            {
                throw new SAXException("I/O error", e);
            }
        }

        public void startPrefixMapping(String prefix, String uri)
        {
            System.out.println("startPrefixMapping ...");
            //System.out.println("needNewContext: " + needNewContext);
            if (needNewContext)
            {
                namespaces.pushContext(); i++; System.out.println("/" + i);
                needNsDecl = true;
                needNewContext = false;
            }
            namespaces.declarePrefix(prefix, uri);
        }

        public void endPrefixMapping(String prefix)
        {
            System.out.println("... endPrefixMapping");
        }

        private void _enumeratePrefixes()
        {
            String margin = "";
            for (int j = 0; j < i; j++)
                margin += "  ";
            System.out.println(margin + "context:");
            Enumeration e = namespaces.getDeclaredPrefixes();
            for (; e.hasMoreElements(); )
            {
                String prefix = (String)e.nextElement();
                String uri = namespaces.getURI(prefix);
                System.out.println(margin + "  " + prefix + "->" + uri);
            }
        }

        private void enumeratePrefixes() throws SAXException
        {
            Enumeration e = namespaces.getDeclaredPrefixes();
            for (; e.hasMoreElements(); )
            {
                String prefix = (String)e.nextElement();
                String uri = namespaces.getURI(prefix);
                emit(" ");
                if (prefix.equals("") && uri != null && !uri.equals(""))
                    emit("xmlns=\"" + uri + "\"");
                else
                    emit("xmlns:" + prefix + "=\"" + uri + "\"");
            }
        }

        private String processAttrValue(String value)
        {
            System.out.println("processing attr value [" + value + "]");
            StringBuffer sb = new StringBuffer();
            int pos = 0;
            for (int i = 0; i < value.length(); i++)
            {
                switch (value.charAt(i))
                {
                  case '<':
                    sb.append(value, pos, i);
                    sb.append(LT);
                    pos = i + 1;
                    break;
                  case '>':
                    sb.append(value, pos, i);
                    sb.append(GT);
                    pos = i + 1;
                    break;
                  case '&':
                    sb.append(value, pos, i);
                    sb.append(AMP);
                    pos = i + 1;
                    break;
                  case '"':
                    sb.append(value, pos, i);
                    sb.append(QUOT);
                    pos = i + 1;
                    break;
                }
            }
            sb.append(value, pos, value.length());
            return sb.toString();
        }

        public void startElement(String namespaceURI,
                                 String localName,
                                 String qName,
                                 Attributes attrs)
            throws SAXException
        {
            System.out.println("startElement ...");
            //System.out.println("needNewContext: " + needNewContext);
            System.out.println("{" + namespaceURI + "}" + localName + "<->" + qName);
            if (needNewContext)
            {
                namespaces.pushContext(); i++; System.out.println("/" + i);
                needNsDecl = true;
            }
            String prefix = namespaces.getPrefix(namespaceURI);
            String elemName;
            if (prefix == null || prefix.equals(""))
                elemName = localName;
            else
                elemName = prefix + ":" + localName;
            emit("<"+elemName);
            if (needNsDecl)
                enumeratePrefixes();
            if (attrs != null)
            {
                for (int i = 0; i < attrs.getLength(); i++)
                {
                    String aLocal = attrs.getLocalName(i);
                    String aURI = attrs.getURI(i);
                    String aQName = attrs.getQName(i);
                    System.out.println("attr " + i + ": " + aURI + " | " + aLocal + " | " + aQName);
                    String aName;
                    if ((aURI != null) && !aURI.equals(""))
                        aName = namespaces.getPrefix(aURI) + ":" + aLocal;
                    else
                        aName = aLocal;
                    if ("".equals(aName)) aName = attrs.getQName(i);
                    emit(" ");
                    emit(aName+"=\""+processAttrValue(attrs.getValue(i))+"\"");
                }
            }
            emit(">");
            needNewContext = true;
        }

        public void endElement(String namespaceURI,
                               String localName,
                               String qName)
            throws SAXException
        {
            System.out.println("... endElement");
            System.out.println("{" + namespaceURI + "}" + localName + "<->" + qName);
            String prefix = namespaces.getPrefix(namespaceURI);
            String elemName;
            if (prefix == null || prefix.equals(""))
                elemName = localName;
            else
                elemName = prefix + ":" + localName;
            emit("</"+elemName+">");
            _enumeratePrefixes();
            namespaces.popContext(); System.out.println("\\" + i); i--;
        }

        public void characters(char buf[], int offset, int len)
            throws SAXException
        {
            StringBuffer sb = new StringBuffer();
            int pos = offset;
            for (int i = offset; i < offset + len; i++)
            {
                switch (buf[i])
                {
                  case '<':
                    sb.append(buf, pos, i - pos);
                    sb.append(LT);
                    pos = i + 1;
                    break;
                  case '>':
                    sb.append(buf, pos, i - pos);
                    sb.append(GT);
                    pos = i + 1;
                    break;
                  case '&':
                    sb.append(buf, pos, i - pos);
                    sb.append(AMP);
                    pos = i + 1;
                    break;
                  case '\n':
                    if (!newline.equals("\n"))
                    {
                        sb.append(buf, pos, i - pos);
                        sb.append(newline);
                        pos = i + 1;
                    }
                    break;
                }
            }
            sb.append(buf, pos, len - pos + offset);
            String s = sb.toString(); //new String(buf, offset, len);
            emit(s);
            System.out.println("characters [offset: " + offset + " length: " + len + "] [" + s + "](" + sb.length() + ")");
        }

        //===========================================================
        // Utility Methods ...
        //===========================================================

        // Wrap I/O exceptions in SAX exceptions, to
        // suit handler signature requirements
        private void emit(String s) throws SAXException
        {
            try
            {
                out.write(s);
                out.flush();
            } 
            catch (IOException e)
            {
                throw new SAXException("I/O error", e);
            }
        }

        // Start a new line
        private void nl() throws SAXException
        {
            try 
            {
                out.write(newline);
            } 
            catch (IOException e)
            {
                throw new SAXException("I/O error", e);
            }
        }        
    }
}
