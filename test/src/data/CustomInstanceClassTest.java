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
package data;

import java.io.*;
import java.util.*;

import javax.sdo.DataObject;
import javax.sdo.Property;
import javax.sdo.Type;
import javax.sdo.ChangeSummary;
import javax.sdo.helper.DataFactory;
import javax.sdo.helper.TypeHelper;
import javax.sdo.helper.XMLHelper;
import javax.sdo.helper.XMLDocument;
import javax.sdo.helper.XSDHelper;

import davos.sdo.test.MyIntList;
import davos.sdo.Options;
import davos.sdo.impl.helpers.DataGraphHelper;

import junit.framework.*;
import common.DataTest;

/**
 * @author Wing Yew Poon
 */
public class CustomInstanceClassTest extends DataTest
{
    public CustomInstanceClassTest(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        
        TestSuite suite = new TestSuite();
        
        suite.addTest(new CustomInstanceClassTest("testSetAndGet"));
        suite.addTest(new CustomInstanceClassTest("testUnmarshalAndMarshal"));
        suite.addTest(new CustomInstanceClassTest("testDefine"));
        suite.addTest(new CustomInstanceClassTest("testChangeSummary"));
        
        // or
        //TestSuite suite = new TestSuite(CustomInstanceClassTest.class);
        return suite;
    }

    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(suite());
    }

    private static DataFactory factory = context.getDataFactory();
    private static TypeHelper typeHelper = context.getTypeHelper();
    private static XMLHelper xmlHelper = context.getXMLHelper();
    private static XSDHelper xsdHelper = context.getXSDHelper();

    private static final String CUSTOM_URI = "http://sdo/test/custom";
    private static final String customSimpleListType =
        "<cus:a xmlns:cus=\"http://sdo/test/custom\">" +
            "<e>1 2 3</e>" +
        "</cus:a>";
    private static final String customSimpleListType2 =
        "<cus:a xmlns:cus=\"http://sdo/test/custom\">" +
            "<e>5 6 7 8</e>" +
        "</cus:a>";
    private static final String customSimpleListTypeDG =
    "<sdo:datagraph xmlns:sdo=\"commonj.sdo\">" + newline +
    "    <changeSummary>" + newline +
    "        <cus:a sdo:ref=\"#/sdo:datagraph/cus:a[1]\" xmlns:cus=\"http://sdo/test/custom\">" + newline +
    "            <e>1 2 3</e>" + newline +
    "        </cus:a>" + newline +
    "    </changeSummary>" + newline +
    "    <cus:a xmlns:cus=\"http://sdo/test/custom\">" + newline +
    "        <e>5 6 7 8</e>" + newline +
    "    </cus:a>" + newline +
    "</sdo:datagraph>";

    public void testSetAndGet()
    {
        String uri = CUSTOM_URI;
        String name = "A";
        Type t = typeHelper.getType(uri, name);
        assertNotNull(t);

        DataObject a = factory.create(t);
        assertNotNull(a);
        
        Property p = t.getProperty("e");
        assertNotNull(p);
        a.setString(p, "1 2 3");
        String s = a.getString(p);
        System.out.println(s);
        assertEquals("1 2 3", s);

        a.unset(p);
        a.setString("e", "1 2 3");
        s = a.getString("e");
        System.out.println(s);
        assertEquals("1 2 3", s);

        MyIntList mil = new MyIntList("5 6 7 8");
        a.set(p, mil);
        Object o = a.get(p);
        assertTrue(o instanceof MyIntList);
        assertEquals("5 6 7 8", o.toString());

        try
        {
            List l = a.getList(p);
            fail("should have thrown a ClassCastException");
        }
        catch (Exception e)
        {
            assertTrue(e instanceof ClassCastException);
        }
    }

    public void testUnmarshalAndMarshal()
    {
        XMLDocument doc = xmlHelper.load(customSimpleListType);
        DataObject a = doc.getRootObject();
        String s = a.getString("e");
        assertEquals("1 2 3", s);
        String out = xmlHelper.save(a, CUSTOM_URI, "a");
        System.out.println(out);
        assertEquals(customSimpleListType, out);
    }

    /* test the same thing as above, but with a schema loaded using 
       XSDHelper.define() */
    public void testDefine() throws Exception
    {
        File f = getResourceFile("data", "custom2.xsd_");
        InputStream in = new FileInputStream(f);
        List types = xsdHelper.define(in, f.toURL().toString());
        in.close();

        String uri = "http://sdo/test/custom2";
        String name = "A";
        Type t = typeHelper.getType(uri, name);
        assertNotNull(t);

        DataObject a = factory.create(t);
        assertNotNull(a);
        
        Property p = t.getProperty("e");
        assertNotNull(p);
        Type pType = p.getType();
        System.out.println(pType.getInstanceClass());
        a.setString(p, "1 2 3");
        String s = a.getString(p);
        System.out.println(s);
        assertEquals("1 2 3", s);

        a.unset(p);
        a.setString("e", "1 2 3");
        s = a.getString("e");
        System.out.println(s);
        assertEquals("1 2 3", s);

        MyIntList mil = new MyIntList("5 6 7 8");
        a.set(p, mil);
        Object o = a.get(p);
        assertTrue(o instanceof MyIntList);
        assertEquals("5 6 7 8", o.toString());
    }

    public void testChangeSummary() throws Exception
    {
        XMLDocument doc = xmlHelper.load(customSimpleListType);
        DataObject a = doc.getRootObject();
        String s = a.getString("e");
        assertEquals("1 2 3", s);

        DataGraphHelper.wrapWithDataGraph(a);
        ChangeSummary cs = a.getChangeSummary();
        assertNotNull(cs);
        cs.beginLogging();

        a.setString("e", "5 6 7 8");
        String a_out = xmlHelper.save(a, CUSTOM_URI, "a");
        System.out.println(a_out);
        assertEquals(customSimpleListType2, a_out);
        doc = xmlHelper.createDocument(a.getRootObject(), "commonj.sdo", "datagraph");
        doc.setXMLDeclaration(false);
        Writer out = new StringWriter();
        xmlHelper.save(doc, out, new Options().setSavePrettyPrint());
        System.out.println(out.toString());
        assertEquals(customSimpleListTypeDG, out.toString());
        out.close();
    }
}
