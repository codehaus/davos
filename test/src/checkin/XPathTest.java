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

import davos.sdo.DataObjectXML;
import davos.sdo.impl.xpath.XPath;
import common.BaseTest;
import javax.sdo.DataObject;
import javax.sdo.helper.XMLHelper;
import util.DataObjectPrinter;

/**
 * @author Cezar Andrei (cezar.andrei at bea.com)
 *         Date: Jan 25, 2008
 */
public class XPathTest extends BaseTest
{
    public XPathTest(String name)
    {
        super(name);
    }

    public void testCompileOnly()
        throws XPath.XPathCompileException
    {
        XPath xp;
        String path;
        path = "/a/b/c";
        xp = XPath.compile(path);
        System.out.println("1: " + path + " : " + xp);

        path = "/a/b/c[ a = '1' ]";
        xp = XPath.compile(path);
        System.out.println("2: " + path + " : " + xp);

        path = "/a/b[b='2']/c";
        xp = XPath.compile(path);
        System.out.println("3: " + path + " : " + xp);

        path = "/a/b[c='3'] [d='4']/c";
        xp = XPath.compile(path);
        System.out.println("4: " + path + " : " + xp);

        path = "/a/b[@e='3']/c";
        xp = XPath.compile(path);
        System.out.println("5: " + path + " : " + xp);

        path = "/a/b[child :: f='3'][attribute  :: d='4']/c";
        xp = XPath.compile(path);
        System.out.println("6: " + path + " : " + xp);

        path = "/a/b[child = '3' ][attribute  ='4']/c";
        xp = XPath.compile(path);
        System.out.println("7: " + path + " : " + xp);

        path = "$this";
        xp = XPath.compile(path);
        System.out.println("8: " + path + " : " + xp);

        path = ".";
        xp = XPath.compile(path);
        System.out.println("9: " + path + " : " + xp);

        path = "./a/b";
        xp = XPath.compile(path);
        System.out.println("10: " + path + " : " + xp);

        path = "$this//b//c";
        xp = XPath.compile(path);
        System.out.println("11: " + path + " : " + xp);

//        path = "/";
//        xp = XPath.compile(path);
//        System.out.println("11: " + path + " : " + xp);
    }

    public void testCompilationError()
    {
        XPath xp;
        String path;
        try
        {
            path = "/";
            xp = XPath.compile(path);
            assertTrue("Expected xpath compilation error.", false);
        }
        catch (XPath.XPathCompileException e)
        {
            assertTrue(true);
            System.out.println(e.getMessage());
        }

    }

    public void test1()
        throws XPath.XPathCompileException
    {
        DataObject dobj;
        XPath xp;
        XPath.Selection selection;
        String path;

        dobj = XMLHelper.INSTANCE.load("<a>" +
            "<b>   <c>1</c> <d>4</d>   t3</b>" +
            "<b>t1</b>" +
            "<b>t2</b>" +
            "<b>   <d>2</d>           t4</b>" +
            "</a>").getRootObject();
        path = "/a/b[ c='1' ][ d='4' ]";
        xp = XPath.compile(path);
        selection = XPath.execute(xp, (DataObjectXML)dobj);
        //System.out.println("8: " + path + " : " + xp);
        //printSelection(selection);    // this will exost the selection
        assertTrue(selection.hasNext());
        Object obj1 = selection.next();
        if (obj1 instanceof DataObject)
        {
            DataObject dobj1 = (DataObject)obj1;
            assertTrue(dobj1.isSet("c"));
            assertTrue(dobj1.isSet("d"));
            assertTrue("1".equals(dobj1.get("c.0")));
            assertTrue("4".equals(dobj1.get("d.0")));
        }
        else
            assertTrue("Result not a DataObject, as expected.", false);

        assertFalse(selection.hasNext());
    }

    public void test2()
        throws XPath.XPathCompileException
    {
        DataObject dobj;
        XPath xp;
        XPath.Selection selection;

        dobj = XMLHelper.INSTANCE.load("<a><b>t1</b><b c='1' d='2'>t2</b><b c='1' d='4'>t3</b><b d='2'>t4</b></a>").getRootObject();
        String path = "/a/b[ @c='1' ][ @d='4' ]";
        xp = XPath.compile(path);
        selection = XPath.execute(xp, (DataObjectXML)dobj);

//        System.out.println("9: " + path + " : " + xp);
//        printSelection(selection);    // this will exost the selection

        assertTrue(selection.hasNext());
        Object obj1 = selection.next();
        if (obj1 instanceof DataObject)
        {
            DataObject dobj1 = (DataObject)obj1;
            assertTrue(dobj1.isSet("c"));
            assertTrue(dobj1.isSet("d"));
            assertTrue("1".equals(dobj1.get("c")));
            assertTrue("4".equals(dobj1.get("d")));
        }
        else
            assertTrue("Result not a DataObject, as expected.", false);

        assertFalse(selection.hasNext());
    }

    public void test3()
        throws XPath.XPathCompileException
    {
        DataObject dobj;
        XPath xp;
        XPath.Selection selection;

        String xml = "<customer ssn=\"XXX-XX-XXX\">\n" +
              "<name>\n" +
                "<first>Alfy</first>\n" +
                "<last>Eli</last>\n" +
              "</name>\n" +
              "<address>\n<city>San Jose</city>\n</address>\n" +
              "<address>\n<city>Seattle</city>\n</address>\n" +
              "<orders>\n" +
                "<order ref=\"1\"/>\n" +
                "<order ref=\"2\"/>\n" +
              "</orders>\n" +
            "</customer>";

        dobj = XMLHelper.INSTANCE.load(xml).getRootObject();
        String path = "address[2]";
        xp = XPath.compile(path);
        selection = XPath.execute(xp, (DataObjectXML)dobj);

//        System.out.println("10: " + path + " : " + xp);
//        printSelection(selection);    // this will exost the selection

        assertTrue(selection.hasNext());
        Object obj1 = selection.next();
        if (obj1 instanceof DataObject)
        {
            DataObject dobj1 = (DataObject)obj1;
            assertTrue(dobj1.isSet("city"));
            assertEquals("Seattle", dobj1.getString("city"));
        }
        else
            assertTrue("Result not a DataObject, as expected.", false);

        assertFalse(selection.hasNext());
    }

    static private void printSelection(XPath.Selection sel)
    {
        int i = 0;
        while(sel.hasNext())
        {
            Object res = sel.next();
            i++;
            if (res instanceof DataObject)
            {
                System.out.print(" #" + i + " ");
                DataObjectPrinter.printDataObject((DataObject)res , 2);
            }
            else
                System.out.println(" #" + i+ " " + res );
        }
    }
}
