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
package xpath;

import java.io.*;

import javax.sdo.*;
import javax.sdo.helper.*;
import davos.sdo.DataObjectXML;
import davos.sdo.impl.xpath.XPath;

import junit.framework.*;
import common.DataTest;
import util.DataObjectPrinter;

/**
   Tests for davos.sdo.impl.xpath.XPath.
   To test:
   static methods:
   XPath compile(String expression, Map<String, String> prefixesToUris);
   XPath compile(String expression, NamespaceContext namespaces);
   XPath compile(String expression);
   XPath compile(String expression, String currentNodeVariable, NamespaceContext namespaces);
   Selection execute(XPath xpath, DataObjectXML dataObject);
   static nested class:
   XPath.MapToNamespaceContextImpl implements NamespaceContext
   and its constructor:
     MapToNamespaceContextImpl(Map<String, String> prefixesToUris);
     methods of NamespaceContext interface:
     String getNamespaceURI(String prefix);
     String getPrefix(String namespaceURI); // not implemented by MapToNamespaceContextImpl
     Iterator getPrefixes(String namespaceURI);
   static nested class:
   XPath.Selection
   and its methods:
     boolean hasNext();
     Object next();

   @author Wing Yew Poon
*/
public class XPathTest extends DataTest
{
    public XPathTest(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        
        TestSuite suite = new TestSuite();
        //suite.addTest(new XPathTest("testCompilePath0"));
        //suite.addTest(new XPathTest("testCompilePath1"));
        //suite.addTest(new XPathTest("testCompilePath2"));
        //suite.addTest(new XPathTest("testExecutePath1"));
        //suite.addTest(new XPathTest("testExecutePath2"));
        //suite.addTest(new XPathTest("testExecutePath3"));
        //suite.addTest(new XPathTest("testExecutePath4"));
        suite.addTest(new XPathTest("testExecutePathSinglePredicate1"));
        suite.addTest(new XPathTest("testExecutePathSinglePredicate2"));
        suite.addTest(new XPathTest("testExecutePathSinglePredicate3"));
        suite.addTest(new XPathTest("testExecutePathSinglePredicate4"));
        suite.addTest(new XPathTest("testExecutePathSinglePredicate5"));
        suite.addTest(new XPathTest("testExecutePathSinglePredicate6"));
        suite.addTest(new XPathTest("testExecutePathMultiplePredicates1"));
        suite.addTest(new XPathTest("testExecutePathMultiplePredicates2"));
        
        // or
        //TestSuite suite = new TestSuite(XPathTest.class);
        return suite;
    }

    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(suite());
    }

    protected static XSDHelper xsdHelper = context.getXSDHelper();
    protected static XMLHelper xmlHelper = context.getXMLHelper();

    static
    {
        File f1 = new File(RESOURCES + S + "xpath" + S + "sdocs1.xsd");
        File f2 = new File(RESOURCES + S + "xpath" + S + "sdocs6.xsd");
        InputStream in1 = null;
        InputStream in2 = null;
        try
        {
            in1 = new FileInputStream(f1);
            xsdHelper.define(in1, f1.toURL().toString());
            in2 = new FileInputStream(f2);
            xsdHelper.define(in2, f2.toURL().toString());
        }
        catch (Exception e) { e.printStackTrace(); }
        finally
        {
            if (in1 != null)
                try { in1.close(); } catch (IOException ioe) {}
            if (in2 != null)
                try { in2.close(); } catch (IOException ioe) {}
        }
    }

    private static void printSelection(XPath.Selection sel)
    {
        int i = 0;
        while (sel.hasNext())
        {
            Object res = sel.next();
            i++;
            if (res instanceof DataObject)
            {
                System.out.println("#" + i + ": ");
                //DataObjectPrinter.printDataObject((DataObject)res);
                System.out.println(xmlHelper.save((DataObject)res, "", "result"));
            }
            else
                System.out.println("#" + i+ ": " + res);
        }
        System.out.println("number selected: " + i);
    }

    public void testCompilePath0() throws Exception
    {
        String xml = "<a><b/><c/></a>";
        DataObject dobj = xmlHelper.load(xml).getRootObject();
        String path = "/";
        XPath xp = XPath.compile(path);
        System.out.println(xp);
        printSelection(XPath.execute(xp, (DataObjectXML)dobj));
    }

    public void testCompilePath1() throws Exception
    {
        String xml = "<a><b/><c/></a>";
        DataObject dobj = xmlHelper.load(xml).getRootObject();
        String path = "/a";
        XPath xp = XPath.compile(path);
        System.out.println(xp);
        printSelection(XPath.execute(xp, (DataObjectXML)dobj));
    }

    public void testCompilePath2() throws Exception
    {
        String xml = "<ns:a xmlns:ns=\"xxx\"><b/><c/></ns:a>";
        DataObject dobj = xmlHelper.load(xml).getRootObject();
        String path = "/ns:a";
        XPath xp = XPath.compile("declare namespace ns='xxx'; " + path);
        System.out.println(xp);
        printSelection(XPath.execute(xp, (DataObjectXML)dobj));
    }

    public void testExecutePath1() throws Exception
    {
        String xml = 
            "<a>" +
            "<b><c>1</c><d>4</d>t3</b>" +
            "<b>t1</b>" +
            "<b>t2</b>" +
            "<b><c>1</c><d>2</d>t4</b>" +
            "</a>";
        DataObject dobj = xmlHelper.load(xml).getRootObject();
        String path1 = "/a/b[c='1']";
        XPath xp1 = XPath.compile(path1);
        System.out.println("****************");
        System.out.println("XPath: " + xp1);
        printSelection(XPath.execute(xp1, (DataObjectXML)dobj));
        String path2 = "/a/b[c='1'][d='4']";
        XPath xp2 = XPath.compile(path2);
        System.out.println("****************");
        System.out.println("XPath: " + xp2);
        printSelection(XPath.execute(xp2, (DataObjectXML)dobj));
        String path3 = "/a/b[c='1'][d='2']";
        XPath xp3 = XPath.compile(path3);
        System.out.println("****************");
        System.out.println("XPath: " + xp3);
        printSelection(XPath.execute(xp3, (DataObjectXML)dobj));
        String path4 = "/a/b[c='1'][d='3']";
        XPath xp4 = XPath.compile(path4);
        System.out.println("****************");
        System.out.println("XPath: " + xp4);
        printSelection(XPath.execute(xp4, (DataObjectXML)dobj));
    }

    public void testExecutePath2() throws Exception
    {
        String xml = 
            "<a>" +
            "<b><c>1</c><d>4</d>t3</b>" +
            "<b>t1</b>" +
            "<b>t2</b>" +
            "<b><c>1</c><d>2</d>t4</b>" +
            "</a>";
        DataObject dobj = xmlHelper.load(xml).getRootObject();
        /*
        String path1 = "/a/b[c='1'][position()=2]";
        XPath xp1 = XPath.compile(path1);
        System.out.println("****************");
        System.out.println("XPath: " + xp1);
        printSelection(XPath.execute(xp1, (DataObjectXML)dobj));
        */
        String path2 = //"/a/b[position()=2]"; 
            "/a/b[position()=2][c='1']";
        XPath xp2 = XPath.compile(path2);
        System.out.println("****************");
        System.out.println("XPath: " + xp2);
        printSelection(XPath.execute(xp2, (DataObjectXML)dobj));
    }

    public void testExecutePath3() throws Exception
    {
        String xml = 
            "<a>" +
            "<b><c>1</c><d>4</d>t3</b>" +
            "<b>t1</b>" +
            "<b>t2</b>" +
            "<b><c>1</c><d>2</d>t4</b>" +
            "</a>";
        DataObject dobj = xmlHelper.load(xml).getRootObject().getDataObject("b[1]");
        
        String path1 = "/b"; //"b[c='1']";
        XPath xp1 = XPath.compile(path1);
        System.out.println("****************");
        System.out.println("XPath: " + xp1);
        printSelection(XPath.execute(xp1, (DataObjectXML)dobj));
        String path2 = "/a";
        XPath xp2 = XPath.compile(path2);
        System.out.println("****************");
        System.out.println("XPath: " + xp2);
        printSelection(XPath.execute(xp2, (DataObjectXML)dobj));
    }

    public void testExecutePath4() throws Exception
    {
        String xml = 
            "<a>" +
            "<b><c>1</c><d>4</d>t3</b>" +
            "<b>t1</b>" +
            "<b x=\"false\">t2</b>" +
            "<b><c>1</c><d>2</d>t4</b>" +
            "</a>";
        DataObject dobj = xmlHelper.load(xml).getRootObject();
        String path0 = "b[3]";
        XPath xp0 = XPath.compile(path0);
        System.out.println("****************");
        System.out.println("XPath: " + xp0);
        printSelection(XPath.execute(xp0, (DataObjectXML)dobj));
        String path1 = "b[3]/@x";
        XPath xp1 = XPath.compile(path1);
        System.out.println("****************");
        System.out.println("XPath: " + xp1);
        printSelection(XPath.execute(xp1, (DataObjectXML)dobj));
        String path2 = "/a/b[3]/@x";
        XPath xp2 = XPath.compile(path2);
        System.out.println("****************");
        System.out.println("XPath: " + xp2);
        printSelection(XPath.execute(xp2, (DataObjectXML)dobj));
        String path3 = "/a/b[3]/attribute::x";
        XPath xp3 = XPath.compile(path3);
        System.out.println("****************");
        System.out.println("XPath: " + xp3);
        printSelection(XPath.execute(xp3, (DataObjectXML)dobj));
    }

    public void testExecutePathSinglePredicate1() throws Exception
    {
        System.out.println("testExecutePathSinglePredicate1()");
        DataObject dobj = getRootDataObject("xpath", "sdocs6_c3.xml");
        String path = "declare namespace ns='http://aldsp.bea.com/test/sdocs6'; ns:tel_no[location='MOBILE']";
        System.out.println(path);
        XPath xp = XPath.compile(path);
        printSelection(XPath.execute(xp, (DataObjectXML)dobj));
        System.out.println("EXPECTED number selected: 1");
    }

    public void testExecutePathSinglePredicate2() throws Exception
    {
        System.out.println("testExecutePathSinglePredicate2()");
        DataObject dobj = getRootDataObject("xpath", "sdocs6_c3.xml");
        String path = "declare namespace ns='http://aldsp.bea.com/test/sdocs6'; ns:tel_no[location='MOBILE']/number";
        System.out.println(path);
        XPath xp = XPath.compile(path);
        printSelection(XPath.execute(xp, (DataObjectXML)dobj));
        System.out.println("EXPECTED number selected: 1");
    }

    public void testExecutePathSinglePredicate3() throws Exception
    {
        System.out.println("testExecutePathSinglePredicate3()");
        DataObject dobj = getRootDataObject("xpath", "sdocs6_c3.xml");
        String path = "declare namespace ns='http://aldsp.bea.com/test/sdocs6'; ns:tel_no[@listed='0']";
        System.out.println(path);
        XPath xp = XPath.compile(path);
        printSelection(XPath.execute(xp, (DataObjectXML)dobj));
        System.out.println("EXPECTED number selected: 1");
    }

    public void testExecutePathSinglePredicate4() throws Exception
    {
        System.out.println("testExecutePathSinglePredicate4()");
        DataObject dobj = getRootDataObject("xpath", "sdocs6_c3.xml");
        String path = "declare namespace ns='http://aldsp.bea.com/test/sdocs6'; ns:tel_no[@listed='0']/number";
        System.out.println(path);
        XPath xp = XPath.compile(path);
        printSelection(XPath.execute(xp, (DataObjectXML)dobj));
        System.out.println("EXPECTED number selected: 1");
    }

    public void testExecutePathSinglePredicate5() throws Exception
    {
        System.out.println("testExecutePathSinglePredicate5()");
        DataObject dobj = getRootDataObject("xpath", "sdocs6_c3.xml");
        String path = "declare namespace ns='http://aldsp.bea.com/test/sdocs6'; ns:tel_no[@listed='1']";
        System.out.println(path);
        XPath xp = XPath.compile(path);
        printSelection(XPath.execute(xp, (DataObjectXML)dobj));
        System.out.println("EXPECTED number selected: 2");
    }

    public void testExecutePathSinglePredicate6() throws Exception
    {
        System.out.println("testExecutePathSinglePredicate6()");
        DataObject dobj = getRootDataObject("xpath", "sdocs6_c3.xml");
        String path = "declare namespace ns='http://aldsp.bea.com/test/sdocs6'; ns:tel_no[@listed='1']/number";
        System.out.println(path);
        XPath xp = XPath.compile(path);
        printSelection(XPath.execute(xp, (DataObjectXML)dobj));
        System.out.println("EXPECTED number selected: 2");
    }

    public void testExecutePathMultiplePredicates1() throws Exception
    {
        System.out.println("testExecutePathMultiplePredicates1()");
        DataObject dobj = getRootDataObject("xpath", "sdocs1_col1.xml");
        String path = "declare namespace ns='http://aldsp.bea.com/test/sdocs1'; ns:ORDER[2]/ns:LINE_ITEM[ns:LINE_ID='LINE_ID_3'][ns:ORDER_ID='ORDER_ID_1'][ns:STORE_ID='1']";
        System.out.println(path);
        XPath xp = XPath.compile(path);
        printSelection(XPath.execute(xp, (DataObjectXML)dobj));
        System.out.println("EXPECTED number selected: 1");
    }

    public void testExecutePathMultiplePredicates2() throws Exception
    {
        System.out.println("testExecutePathMultiplePredicates2()");
        DataObject dobj = getRootDataObject("xpath", "sdocs1_col1.xml");
        String path = "declare namespace ns='http://aldsp.bea.com/test/sdocs1'; ns:ORDER[2]/ns:LINE_ITEM[ns:LINE_ID='LINE_ID_3'][ns:ORDER_ID='ORDER_ID_1'][ns:STORE_ID='1']/ns:QUANTITY";
        System.out.println(path);
        XPath xp = XPath.compile(path);
        printSelection(XPath.execute(xp, (DataObjectXML)dobj));
        System.out.println("EXPECTED number selected: 1");
    }

}
