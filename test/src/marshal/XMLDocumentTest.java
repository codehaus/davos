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

import javax.sdo.*;
import javax.sdo.helper.*;

import junit.framework.*;
import common.BaseTest;

//import util.XmlComparator;

/** 
 *  This class tests the XMLDocument interface.
 *  getRootObject() is tested extensively elsewhere.
 *  The other methods are tested here.
 *  @author Wing Yew Poon
 */
public class XMLDocumentTest extends BaseTest
{
    public XMLDocumentTest(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        
        TestSuite suite = new TestSuite();
        suite.addTest(new XMLDocumentTest("testXMLHeader0"));
        suite.addTest(new XMLDocumentTest("testXMLHeader1"));
        suite.addTest(new XMLDocumentTest("testXMLHeader2"));
        suite.addTest(new XMLDocumentTest("testXMLHeader3"));
        suite.addTest(new XMLDocumentTest("testXMLHeader4"));
        suite.addTest(new XMLDocumentTest("testXMLHeader5"));
        suite.addTest(new XMLDocumentTest("testXMLHeader6"));
        suite.addTest(new XMLDocumentTest("testSchemaLocation1"));
        suite.addTest(new XMLDocumentTest("testNoNamespaceSchemaLocation1"));
        
        // or
        //TestSuite suite = new TestSuite(XMLDocumentTest.class);
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

    private static final String header1 = "<?xml version='1.0'?>";
    //private static final String header2 = "<?xml version='1.0' encoding='ASCII'?>"; // ASCII -> US-ACSII, is this correct?
    private static final String header2 = "<?xml version='1.0' encoding='ISO-8859-1'?>";
    private static final String header3 = "<?xml version='1.1' encoding='UTF-8'?>";

    private static final String xml0 = "<a><b/><c>xxx</c></a>";
    private static final String xml1 = header1 + xml0;
    private static final String xml2 = header2 + xml0;
    private static final String xml3 = header3 + xml0;

    public void testXMLHeader0() throws IOException
    {
        System.out.println("testXMLHeader0()");
        // no xml header
        // isXMLDeclaration() -> false, getXMLVersion() -> null
        String xml = xml0;
        System.out.println(xml);
        XMLDocument doc = xmlHelper.load(xml);
        assertFalse(doc.isXMLDeclaration());
        assertNull(doc.getXMLVersion());
        assertNull(doc.getEncoding());
        StringWriter out = new StringWriter();
        xmlHelper.save(doc, out, null);
        out.close();
        System.out.println(out.toString());
        assertEquals(xml, out.toString());
    }

    public void testXMLHeader1() throws IOException
    {
        System.out.println("testXMLHeader1()");
        // xml header: version, no encoding
        String xml = xml1;
        System.out.println(xml);
        XMLDocument doc = xmlHelper.load(xml);
        assertTrue(doc.isXMLDeclaration());
        assertEquals("1.0", doc.getXMLVersion());
        // encoding is null if not specified
        assertNull(doc.getEncoding());
        StringWriter out = new StringWriter();
        xmlHelper.save(doc, out, null);
        out.close();
        System.out.println(out.toString());
        assertEquals(xml.replace('\'', '"'), out.toString());
    }

    public void testXMLHeader2() throws IOException
    {
        System.out.println("testXMLHeader2()");
        // xml header: version, encoding
        String xml = xml2;
        System.out.println(xml);
        XMLDocument doc = xmlHelper.load(xml);
        assertTrue(doc.isXMLDeclaration());
        assertEquals("1.0", doc.getXMLVersion());
        assertEquals("ISO-8859-1", doc.getEncoding());
        StringWriter out = new StringWriter();
        xmlHelper.save(doc, out, null);
        out.close();
        System.out.println(out.toString());
        assertEquals(xml.replace('\'', '"'), out.toString());
    }

    public void testXMLHeader3() throws IOException
    {
        System.out.println("testXMLHeader3()");
        // xml header: different version, different encoding
        String xml = xml3;
        System.out.println(xml);
        XMLDocument doc = xmlHelper.load(xml);
        assertTrue(doc.isXMLDeclaration());
        assertEquals("1.1", doc.getXMLVersion());
        assertEquals("UTF-8", doc.getEncoding());
        StringWriter out = new StringWriter();
        xmlHelper.save(doc, out, null);
        out.close();
        System.out.println(out.toString());
        assertEquals(xml.replace('\'', '"'), out.toString());
    }

    public void testXMLHeader4() throws IOException
    {
        System.out.println("testXMLHeader4()");
        // no xml header
        // setXMLDeclaration(), nothing else
        // save, verify (what should the xml header look like??)
        String xml = xml0;
        System.out.println(xml);
        XMLDocument doc = xmlHelper.load(xml);
        doc.setXMLDeclaration(true);
        assertTrue(doc.isXMLDeclaration());
        assertNull(doc.getXMLVersion());
        assertNull(doc.getEncoding());
        StringWriter out = new StringWriter();
        xmlHelper.save(doc, out, null);
        out.close();
        System.out.println(out.toString());
        assertEquals(xml1.replace('\'', '"'), out.toString());
    }

    public void testXMLHeader5() throws IOException
    {
        System.out.println("testXMLHeader5()");
        // no xml header
        // setXMLDeclaration(), setXMLVersion()
        // getEncoding() - null
        // save, verify
        String xml = xml0;
        System.out.println(xml);
        XMLDocument doc = xmlHelper.load(xml);
        doc.setXMLDeclaration(true);
        doc.setXMLVersion("1.1");
        assertTrue(doc.isXMLDeclaration());
        assertEquals("1.1", doc.getXMLVersion());
        assertNull(doc.getEncoding());
        StringWriter out = new StringWriter();
        xmlHelper.save(doc, out, null);
        out.close();
        System.out.println(out.toString());
        String header = "<?xml version=\"1.1\"?>";
        assertEquals(header + xml, out.toString());
    }

    public void testXMLHeader6() throws IOException
    {
        System.out.println("testXMLHeader6()");
        // no xml header
        // setXMLDeclaration(), setXMLVersion(), setEncoding()
        // save, verify
        String xml = xml0;
        System.out.println(xml);
        XMLDocument doc = xmlHelper.load(xml);
        doc.setXMLDeclaration(true);
        doc.setXMLVersion("1.0");
        doc.setEncoding("ISO-8859-1");
        assertTrue(doc.isXMLDeclaration());
        assertEquals("1.0", doc.getXMLVersion());
        assertEquals("ISO-8859-1", doc.getEncoding());
        StringWriter out = new StringWriter();
        xmlHelper.save(doc, out, null);
        out.close();
        System.out.println(out.toString());
        assertEquals(xml2.replace('\'', '"'), out.toString());
    }

    public void testSchemaLocation0() throws IOException
    {
        // root element has namespace declaration, no schema location
        // getRootElementURI(), getRootElementName()
        // setSchemaLocation(), save, verify
        // a. valid - one pair
        // b. valid - two pairs
        // c. invalid - single uri
        // d. invalid - three uri's
    }

    public void testSchemaLocation1() throws IOException
    {
        System.out.println("testSchemaLocation1()");
        // root element has namespace declaration, schema location
        // getRootElementURI(), getRootElementName()
        // setSchemaLocation(), save, verify
        File f = getResourceFile("marshal", "prod_ns_1.xml");
        InputStream in = new FileInputStream(f);
        XMLDocument doc = xmlHelper.load(in, f.toURL().toString(), null);
        in.close();
        //xmlHelper.save(doc.getRootObject(),
        //               doc.getRootElementURI(),
        //               doc.getRootElementName(),
        //               System.out);
        //System.out.println();
        StringWriter out = new StringWriter();
        xmlHelper.save(doc, out, null);
        out.close();
        System.out.println(out.toString());
        assertNotNull(doc.getSchemaLocation());
        assertEquals("http://example.com/prod prod_ns.xsd",
                     doc.getSchemaLocation());
    }

    public void testNoNamespaceSchemaLocation0()
    {
        // root element has no namespace declaration, no schema location
        // getRootElementURI(), getRootElementName()
        // setNoNamespaceSchemaLocation(), save, verify
        // a. valid
        // b. invalid
    }

    public void testNoNamespaceSchemaLocation1() throws IOException
    {
        System.out.println("testNoNamespaceSchemaLocation1()");
        // root element has no namespace declaration, has schema location
        // getRootElementURI(), getRootElementName()
        // setNoNamespaceSchemaLocation(), save, verify
        File f = getResourceFile("marshal", "prod_nons_1.xml");
        InputStream in = new FileInputStream(f);
        XMLDocument doc = xmlHelper.load(in, f.toURL().toString(), null);
        in.close();
        //xmlHelper.save(doc.getRootObject(),
        //               doc.getRootElementURI(),
        //               doc.getRootElementName(),
        //               System.out);
        //System.out.println();
        StringWriter out = new StringWriter();
        xmlHelper.save(doc, out, null);
        out.close();
        System.out.println(out.toString());
        assertNotNull(doc.getNoNamespaceSchemaLocation());
        assertEquals("prod_nons.xsd",
                     doc.getNoNamespaceSchemaLocation());
    }

}
