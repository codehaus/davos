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
import java.util.List;

import javax.sdo.DataObject;
import javax.sdo.Type;
import javax.sdo.Property;
import javax.sdo.helper.TypeHelper;
import javax.sdo.helper.XSDHelper;

import junit.framework.Test;
import junit.framework.TestSuite;
import common.BaseTest;

/**
 * @author Wing Yew Poon
 */
public class Schema2SDONameTest extends BaseTest
{
    public Schema2SDONameTest(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        
        TestSuite suite = new TestSuite();
        suite.addTest(new Schema2SDONameTest("testPropertyName"));
        
        // or
        //TestSuite suite = new TestSuite(PropertyTest.class);
        return suite;
    }

    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(suite());
    }

    private static TypeHelper typeHelper = context.getTypeHelper();
    private static XSDHelper xsdHelper = context.getXSDHelper();

    public void testPropertyName() throws Exception
    {
        File f = getResourceFile("type", "names.xsd");
        InputStream in = new FileInputStream(f);
        List types = xsdHelper.define(in, f.toURL().toString());
        in.close();
        assertEquals(1, types.size());
        Type testType = (Type)types.get(0);
        assertEquals("http://sdo/test/names", testType.getURI());
        assertEquals("Test", testType.getName());
        Type stringType = typeHelper.getType("commonj.sdo", "String");
        Type doubleType = typeHelper.getType("commonj.sdo", "Double");
        Type booleanType = typeHelper.getType("commonj.sdo", "Boolean");
        List props = testType.getProperties();
        assertEquals(4, props.size());
        Property foo1 = (Property)props.get(0);
        assertEquals("foo", foo1.getName());
        assertEquals(stringType, foo1.getType());
        Property bar = (Property)props.get(1);
        assertEquals("bar", bar.getName());
        assertEquals(doubleType, bar.getType());
        Property foo2 = (Property)props.get(2);
        assertEquals("foo_attr", foo2.getName());
        assertEquals(booleanType, foo2.getType());
        Property id = (Property)props.get(3);
        assertEquals("id", id.getName());
        assertEquals(stringType, id.getType());
    }
}
