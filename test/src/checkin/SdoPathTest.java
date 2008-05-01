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

import common.BaseTest;
import davos.sdo.impl.path.Parser;
import davos.sdo.impl.path.Path;
import davos.sdo.impl.xpath.XPath;
import davos.sdo.DataObjectXML;
import javax.sdo.DataObject;
import javax.sdo.helper.TypeHelper;
import javax.sdo.helper.DataFactory;
import javax.sdo.helper.XMLDocument;
import javax.sdo.helper.XMLHelper;
import javax.sdo.helper.XSDHelper;

import java.io.FileNotFoundException;
import java.io.FileInputStream;
import java.io.File;
import java.io.StringReader;
import java.util.List;
import java.util.ArrayList;

/**
 * @author Cezar Andrei (cezar.andrei at bea.com)
 *         Date: Jul 17, 2006
 */
public class SdoPathTest
    extends BaseTest
{
    public SdoPathTest(String name)
    {
        super(name);
    }

    private static DataFactory dataFactory = context.getDataFactory();
    private static TypeHelper typeHelper = context.getTypeHelper();
    private static XMLHelper xmlHelper = context.getXMLHelper();
    private static XSDHelper xsdHelper = context.getXSDHelper();

    public void testParse()
        throws Parser.SDOPathException
    {
        assertEquals("a" , Path.prepare("a").toString());
        assertEquals("abc" , Path.prepare("abc").toString());
        assertEquals("//a" , Path.prepare("/a").toString());
        assertEquals("a" , Path.prepare("@a").toString());
        assertEquals("abc124" , Path.prepare("@abc124").toString());
        assertEquals("a[1]" , Path.prepare("a[1]").toString());
        assertEquals("a[123]" , Path.prepare("a[123]").toString());
        assertEquals("a.0" , Path.prepare("a.0").toString());
        assertEquals("a.78" , Path.prepare("a.078").toString());
        assertEquals("a[b='a']" , Path.prepare("a[b='a']").toString());
        assertEquals("a[b='abc']" , Path.prepare("a[b=\"abc\"]").toString());
        assertEquals("a[b=1]" , Path.prepare("a[b=1]").toString());
        assertEquals("a[b=123]" , Path.prepare("a[b=123]").toString());
        assertEquals("a[b=1]" , Path.prepare("a[b=1.]").toString());
        assertEquals("a[b=1.2]" , Path.prepare("a[b=1.2]").toString());
        assertEquals("a[b=0.1]" , Path.prepare("a[b=.1]").toString());
        assertEquals("a[b=0.123]" , Path.prepare("a[b=.123]").toString());
        assertEquals("a[b=true]" , Path.prepare("a[b=true]").toString());
        assertEquals("a[b=false]" , Path.prepare("a[b=false]").toString());
        assertEquals(".." , Path.prepare("..").toString());
        assertEquals("../.." , Path.prepare("../..").toString());
        assertEquals("a.0/../b/c[d=false]" , Path.prepare("a.0/../b/c[d=false]").toString());
        assertEquals("a.78/../../.." , Path.prepare("a.078/../../..").toString());
        assertEquals("a/.." , Path.prepare("@a/..").toString());
        assertEquals("a/b.33/c[4]/d[e=4]/.." , Path.prepare("@a/b.33/c[4]/d[e=4]/..").toString());
        assertEquals("a" , Path.prepare("sdo::a").toString());
        assertEquals("//abc" , Path.prepare("sdo::/abc").toString());
        assertEquals("a" , Path.prepare("sdo::@a").toString());
        assertEquals("//a.0" , Path.prepare("sdo::/a.0").toString());
        assertEquals("//a[5]" , Path.prepare("sdo::/a[5]").toString());
        assertEquals("//a[b='c']/.." , Path.prepare("sdo::/a[b='c']/..").toString());
        assertEquals("//../../../../.." , Path.prepare("sdo::/../../../../..").toString());
        assertEquals("//a/b/c/.." , Path.prepare("sdo::/a/b/c/..").toString());
        assertEquals("../.." , Path.prepare("sdo::../..").toString());
        assertEquals("//../.." , Path.prepare("sdo::/../..").toString());
        assertEquals("s::~!@#" , Path.prepare("s::~!@#").toString());
        assertEquals("wer::ewrzdssdfs=-053=9358-63497623629';'\":\"?>,.`~!@#$%^&*()_+=-" , Path.prepare("wer::ewrzdssdfs=-053=9358-63497623629';'\":\"?>,.`~!@#$%^&*()_+=-").toString());
    }

    public void testExecute()
    {
        DataObject qRoot = dataFactory.create(typeHelper.getType("http://www.example.com/simple1", "Quote"));
        qRoot.set("symbol", "qRoot_symbol_string_val");
        assertEquals("qRoot_symbol_string_val" , qRoot.get("symbol"));

        DataObject q10 = qRoot.createDataObject("quotes");
        q10.set("symbol", "q10_symbol_val");
        assertEquals("q10_symbol_val", q10.get("symbol"));
        assertEquals("q10_symbol_val", qRoot.getDataObject("quotes.0").get("symbol"));

        DataObject q20 = q10.createDataObject("quotes");
        q20.set("symbol", "q20_symbol_val");
        assertEquals("q20_symbol_val", q20.get("symbol"));
        assertEquals("q20_symbol_val", qRoot.getDataObject("quotes.0").getDataObject("quotes.0").get("symbol"));

        DataObject q30 = q20.createDataObject("quotes");
        q30.set("symbol", "q30_symbol_val");
        assertEquals("q30_symbol_val", q30.get("symbol"));
        assertEquals("q30_symbol_val", qRoot.getDataObject("quotes.0").getDataObject("quotes.0").getDataObject("quotes.0").get("symbol"));

        //System.out.println("1 qRoot = ");
        //Test.printDO("  ", qRoot);


        assertEquals("q30_symbol_val", Path.executeGet(qRoot, "quotes/quotes/quotes/symbol"));

        Path.executeSet(context, qRoot, "quotes/quotes/quotes/symbol", "q3_string_second_value");
        assertEquals("q3_string_second_value", qRoot.getDataObject("quotes.0").getDataObject("quotes.0").getDataObject("quotes.0").get("symbol"));

        //System.out.println("-     symbol= " + q30.get("symbol"));
        assertEquals("q3_string_second_value", Path.executeGet(qRoot, "quotes/quotes/quotes/symbol"));
        assertEquals("q3_string_second_value", Path.executeGet(qRoot, "quotes/quotes.0/quotes/symbol"));
        assertEquals("q3_string_second_value", Path.executeGet(qRoot, "quotes/quotes[1]/quotes/symbol"));

        Path.executeSet(context, qRoot, "quotes/quotes/quotes[symbol='q3_string_second_value']/symbol", "q3_string_third_value");
        assertEquals("q3_string_third_value", Path.executeGet(qRoot, "quotes/quotes[1]/quotes/symbol"));
        assertEquals("q3_string_third_value", Path.executeGet(qRoot, "quotes/quotes/quotes/../../quotes/quotes/symbol"));

        //System.out.println("\n\n2 qRoot = ");
        //Test.printDO("  ", qRoot);

        DataObject q21 = dataFactory.create(typeHelper.getType("http://www.example.com/simple1", "Quote"));
        //System.out.println("-   q21 = " + q21);
        q21.set("symbol", "q21_string_val");

        Path.executeSet(context, qRoot, "quotes/quotes/quotes/..", q21);
        assertEquals("q21_string_val", Path.executeGet(qRoot, "quotes/quotes/symbol"));
        assertEquals(null, Path.executeGet(qRoot, "quotes/quotes/quotes/symbol"));

        //System.out.println("\n\n3 qRoot = ");
        //Test.printDO("  ", qRoot);

        DataObject q11 = qRoot.createDataObject("quotes");
        q11.set("symbol", "q11_symbol");

        assertEquals("q10_symbol_val", Path.executeGet(qRoot, "quotes/symbol"));
        assertEquals("q11_symbol", Path.executeGet(qRoot, "quotes.1/symbol"));
        assertEquals(null, Path.executeGet(qRoot, "quotes.2/symbol"));

        DataObject q12 = qRoot.createDataObject("quotes");
        q12.set("symbol", "q12_symbol");
        q12.set("price", 1.5);
        q12.set("volume", 50.77);

        assertEquals(1.5, q12.get("price"));
        assertEquals(50.77, q12.get("volume"));


        assertEquals("q10_symbol_val", Path.executeGet(qRoot, "quotes/symbol"));
        assertEquals("q12_symbol", Path.executeGet(qRoot, "quotes.2/symbol"));
        assertEquals("q12_symbol", Path.executeGet(qRoot, "quotes[3]/symbol"));

        //System.out.println("\n\n4 qRoot = ");
        //Test.printDO("  ", qRoot);

        assertEquals(1.5, Path.executeGet(qRoot, "quotes.2/price"));
        assertEquals(50.77, Path.executeGet(qRoot, "quotes.2/volume"));

        assertEquals(50.77, Path.executeGet(qRoot, "quotes[price=1.5]/volume"));
        Path.executeSet(context, qRoot, "quotes[price=1.5]/volume", 300);
        assertEquals(300, Path.executeGet(qRoot, "quotes[price=1.5]/volume"));
    }

    public void testListOrNot()
    {
        DataObject qRoot = dataFactory.create(typeHelper.getType("http://www.example.com/simple1", "Quote"));
        qRoot.set("symbol", "qRoot_symbol_string_val");
        assertEquals("qRoot_symbol_string_val" , qRoot.get("symbol"));

        DataObject q1 = qRoot.createDataObject("quotes");
        q1.set("symbol", "q1_symbol_val");
        assertEquals("q1_symbol_val", q1.get("symbol"));
        assertEquals("q1_symbol_val", qRoot.getDataObject("quotes[1]").get("symbol"));

        DataObject q2 = qRoot.createDataObject("quotes");
        q2.set("symbol", "q2_symbol_val");
        assertEquals("q2_symbol_val", q2.get("symbol"));
        assertEquals("q2_symbol_val", qRoot.getDataObject("quotes[2]").get("symbol"));

        DataObject q3 = qRoot.createDataObject("quotes");
        q3.set("symbol", "q3_symbol_val");
        assertEquals("q3_symbol_val", q3.get("symbol"));
        assertEquals("q3_symbol_val", qRoot.getDataObject("quotes[3]").get("symbol"));

        //System.out.println("1 qRoot = ");
        //Test.printDO("  ", qRoot);

        String path;
        path = "quotes/symbol";
        //System.out.println(" 1: " + qRoot.get(path));
        assertEquals("q1_symbol_val", qRoot.get(path));

        path = "quotes[1]/symbol";
        //System.out.println(" 2: " + qRoot.get(path));
        assertEquals("q1_symbol_val", qRoot.get(path));

        path = "quotes[1]";
        System.out.println(" 3.1: " + qRoot.get(path));
        assertNotNull(qRoot.get(path));

        path = "quotes[1]";
        //System.out.println(" 3.2: " + qRoot.getDataObject(path).get("symbol"));
        assertEquals("q1_symbol_val", qRoot.getDataObject(path).get("symbol"));

        path = "quotes";
        //System.out.println(" 4.1: " + qRoot.get(path));
        assertNotNull(qRoot.get(path));

        path = "quotes";
        //System.out.println(" 4.2: " + ((DataObject)qRoot.getList(path).get(1)).get("symbol"));
        assertEquals("q2_symbol_val", ((DataObject)qRoot.getList(path).get(1)).get("symbol"));

        path = "quotes[symbol='q3_symbol_val']";
        //System.out.println(" 5.1: " + qRoot.get(path));
        assertNotNull(qRoot.get(path));

        path = "quotes[symbol='q3_symbol_val']";
        //System.out.println(" 5.2: " + qRoot.getDataObject(path).get("symbol"));
        assertEquals("q3_symbol_val", qRoot.getDataObject(path).get("symbol"));
    }

    public void testXPathUntyped()
        throws XPath.XPathCompileException, FileNotFoundException
    {
        String xml = "<root>" +
            "<a>" +
            "  <b>" +
            "    <c at1='val_at1' at2='val_at2'>c_val1</c>" +
            "    <c>c_val2</c>" +
            "    <d>d_val</d>" +
            "    <c>c_val3</c>" +
            "  </b>" +
            "  <e>e_val</e>" +
            "  <c>c_val4</c>" +
            "</a></root>";
        XMLDocument xmlDoc = xmlHelper.load(xml);
        DataObject rootObj = xmlDoc.getRootObject();

        List<Object> expected = new ArrayList<Object>();
        Object val;

        expected.add(rootObj.get("/a/b/c.0"));
        expected.add(rootObj.get("/a/b/c.1"));
        expected.add(rootObj.get("/a/b/c.2"));
        xp("/root/a/b/c", rootObj, expected);
        xp("//a/b/c", rootObj, expected);

        expected.add(rootObj.get("/a/c.0"));
        xp("//c", rootObj, expected);
        //xp("//a/c", xmlDoc);

        expected.clear();
        xp("/a/b/c", rootObj, expected);

        expected.clear();
        expected.add(rootObj.get("/a/e.0"));
        xp("//e", rootObj, expected);

        expected.clear();
        expected.add(rootObj.get("/a/b/c.0"));
        expected.add(rootObj.get("/a/b/c.1"));
        expected.add(rootObj.get("/a/b/c.2"));
        expected.add(rootObj.get("/a/e.0"));
        expected.add(rootObj.get("/a/c.0"));
        xp("//c | //e", rootObj, expected);

        expected.clear();
        expected.add(rootObj.get("/a/b/c.1"));
        xp("/root/a/b/c[2]", rootObj, expected);

        expected.clear();
        expected.add(rootObj.get("/a/b/c.0"));
        expected.add(rootObj.get("/a/c.0"));
        xp("//c[1]", rootObj, expected);



        expected.clear();
        expected.add(rootObj.get("/a/b/d.0"));
        expected.add(rootObj.get("/a/e.0"));
        xp("//e | //d", rootObj, expected);

        DataObject a = xmlDoc.getRootObject().getDataObject("a.0/b.0");
        //printDO(a);
        expected.clear();
        expected.add(rootObj.get("/a/b/c.0"));
        expected.add(rootObj.get("/a/b/c.1"));
        expected.add(rootObj.get("/a/b/c.2"));
        xp(".//c", a, expected);
        expected.add(rootObj.get("/a/c.0"));
        xp("//c" , a, expected);

        val = rootObj.get("/a/b/c.0/at1");
        assertEquals("val_at1", val);
        expected.clear();
        expected.add(val);
        xp("//c/@at1" , a, expected);
    }

    public void testXPathTyped()
        throws XPath.XPathCompileException, FileNotFoundException
    {
        String xsd;

        xsd = "<xs:schema \n" + "    targetNamespace=\"simpleTypeTest\" \n" +
            "    xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" \n" +
            "    xmlns:stt=\"simpleTypeTest\">\n" + "\n" +
            "    \n" +
            "  <xs:element name=\"root\">\n" +
            "    <xs:complexType>\n" +
            "      <xs:sequence>\n" +
            "        <xs:element name=\"item\" type=\"xs:string\" minOccurs='0' maxOccurs='10' />\n" +
            "        <xs:element name=\"a\" type=\"stt:A\" minOccurs='0' maxOccurs='10' />\n" +
            "      </xs:sequence>\n" +
            "    </xs:complexType>\n" +
            "  </xs:element>\n" +
            "     \n" +
            "  <xs:complexType name=\"A\">\n" +
            "    <xs:sequence>\n" +
            "      <xs:element name=\"b\" type=\"stt:B\" minOccurs='0' maxOccurs='1' />\n" +
            "      <xs:element name=\"e\" type=\"xs:string\" minOccurs='0' maxOccurs='1' />\n" +
            "      <xs:element name=\"c\" type=\"stt:C\" minOccurs='0' maxOccurs='1' />\n" +
            "    </xs:sequence>\n" +
            "  </xs:complexType>\n" + "\n" +
            "  <xs:complexType name=\"B\">\n" +
            "    <xs:choice maxOccurs=\"unbounded\">\n" +
            "      <xs:element name=\"c\" type=\"stt:C\" minOccurs='0' maxOccurs='10' />\n" +
            "      <xs:element name=\"d\" type=\"xs:string\" minOccurs='0' maxOccurs='1' />\n" +
            "    </xs:choice>\n" +
            "  </xs:complexType>\n" + "\n" +
            "  <xs:complexType name=\"C\">\n" +
            "    <xs:simpleContent>\n" +
            "      <xs:extension base=\"xs:string\">\n" +
            "        <xs:attribute name=\"at1\" type=\"xs:string\" use=\"required\"/>\n" +
            "        <xs:attribute name=\"at2\" type=\"xs:string\" use=\"required\"/>\n" +
            "      </xs:extension>\n" +
            "    </xs:simpleContent>\n" +
            "  </xs:complexType>\n" + "\n" +
            "</xs:schema>";

        String xml;

        xml = "<s:root xmlns:s='simpleTypeTest'>" +
            "<a>" +
              "<b>" +
                "<c at1='val_at1' at2='val_at2'>c_val1</c>" +
                "<c>c_val2</c>" +
                "<d>d_val</d>" +
                "<c>c_val3</c>" +
              "</b>" +
              "<e>e_val</e>" +
              "<c>c_val4</c>" +
            "</a></s:root>";
        xsdHelper.define(new StringReader(xsd), ".");

        XMLDocument doc = xmlHelper.load(xml);
        DataObject rootObj = doc.getRootObject();
        //printDO("\t", rootObj);

        List<Object> expected = new ArrayList<Object>();

        expected.clear(); // no results since root has a namespace
        xp("/root/a/b/c", rootObj, expected);

        // bugbug radu please fix
        // no results because expression matches elements named simpleTypeTest@c but actual values of c don't have uries set
        xp("declare default element namespace \"simpleTypeTest\"; /root/a/b/c", rootObj, expected);

        expected.clear();
        expected.add(rootObj.get("/a/b/c.0"));
        expected.add(rootObj.get("/a/b/c.1"));
        expected.add(rootObj.get("/a/b/c.2"));        
        xp("//a/b/c", rootObj, expected);

        xp("declare namespace p=\"simpleTypeTest\"; /p:root/a/b/c", rootObj, expected);

        Object val = rootObj.get("/a/b/c.0/at1");
        assertEquals("val_at1", val);
        expected.clear();
        expected.add(val);
        xp("declare namespace p=\"simpleTypeTest\"; /p:root/a/b/c/@at1", rootObj, expected);
    }

//    private static void xp(String exp, XMLDocument xmlDocument, List<Object> expected)
//        throws XPath.XPathCompileException
//    {
//        System.out.println("\n" + exp + " : " );
//        XPath xpplan = XPath.compile(exp, null);
//        //System.out.println(exp + " : " + xpplan);
//        XPath.Selection s = XPath.execute(xpplan, xmlDocument);
//        int i =0;
//        for (; s.hasNext(); i++)
//        {
//            Object v = s.getValue();
//            System.out.println("   v[" + i + "]: " + v + "\t\t" + s.getPropertyXML());
//            assertEquals(expected.get(i), v);
//            s.next();
//        }
//        if (i!=expected.size())
//            assertTrue("Inconsistent result number: expected " + expected.size() + " actual " + i, false);
//    }

    private static void xp(String exp, DataObject dataObject, List<Object> expected)
        throws XPath.XPathCompileException
    {
        System.out.println("\n" + exp + " : " );
        XPath xpplan = XPath.compile(exp);
        //System.out.println(exp + " : " + xpplan);
        XPath.Selection s = XPath.execute(xpplan, (DataObjectXML)dataObject);
        int i = 0;
        for (; s.hasNext(); i++)
        {
            Object v = s.getValue();
            System.out.println("   v[" + i + "]: " + v + "\t\t" + s.getPropertyXML());
            assertEquals(expected.get(i), v);
            s.next();
        }
        if (i!=expected.size())
            assertTrue("Inconsistent result number: expected " + expected.size() + " actual " + i, false);
    }
}
