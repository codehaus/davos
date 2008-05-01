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

import javax.sdo.*;
import javax.sdo.helper.*;

import junit.framework.Test;
import junit.framework.TestSuite;
import common.BaseTest;

/**
 * A test for unmarshalling an xml instance where there is an element and
 * an attribute of the same name ("foo").
 * @author Wing Yew Poon
 */
public class FooTest extends BaseTest
{
    public FooTest(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        TestSuite suite = new TestSuite();
        suite.addTest(new FooTest("testUnmarshal"));
        return suite;
    }

    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(suite());
    }

    private static XSDHelper xsdHelper = context.getXSDHelper();
    private static XMLHelper xmlHelper = context.getXMLHelper();

    public void testUnmarshal() throws Exception
    {
        File f = getResourceFile("type", "names.xsd");
        InputStream in = new FileInputStream(f);
        List types = xsdHelper.define(in, f.toURL().toString());
        in.close();
        String xml = 
            "<nam:test foo=\"true\" id=\"xxx\" " +
            "xmlns:nam=\"http://sdo/test/names\">" +
            "<foo>abc</foo>" +
            "<bar>3.14</bar>" +
            "</nam:test>";
        DataObject test = xmlHelper.load(xml).getRootObject();
        assertEquals("abc", test.get("foo"));
        assertEquals(3.14, test.get("bar"));
        assertEquals(true, test.get("foo_attr"));
        assertEquals("xxx", test.get("id"));
        assertEquals(xml, xmlHelper.save(test, "http://sdo/test/names", "test"));
    }
}
