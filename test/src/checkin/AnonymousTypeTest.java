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

import javax.sdo.Type;
import javax.sdo.helper.TypeHelper;

import sdo.test.anontype.*;

import junit.framework.Test;
import junit.framework.TestSuite;
import common.BaseTest;

/**
 * @author Wing Yew Poon
 */
public class AnonymousTypeTest extends BaseTest
{
    public AnonymousTypeTest(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        /*
        TestSuite suite = new TestSuite();
        suite.addTest(new AnonymousTypeTest("testDynamicType4"));
        */
        // or
        TestSuite suite = new TestSuite(AnonymousTypeTest.class);
        return suite;
    }

    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(suite());
    }

    private static final String TEST_URI = "http://sdo/test/anontype";

    private static TypeHelper typeHelper = context.getTypeHelper();

    /* global element with anonymous complex type */
    public void testGlobalElement()
    {
        Type t = typeHelper.getType(TEST_URI, "gigli");
        assertNotNull(t);
        assertEquals(Gigli.class, t.getInstanceClass());
        assertTrue(t == typeHelper.getType(Gigli.class));
    }

    /* global attribute with anonymous simple type */
    public void testGlobalAttribute()
    {
        Type t = typeHelper.getType(TEST_URI, "fleck");
        assertNotNull(t);
        assertEquals(1, t.getBaseTypes().size());
        Type stringType = typeHelper.getType(SDO_URI, "String");
        assertEquals(stringType, t.getBaseTypes().get(0));
        assertEquals(String.class, t.getInstanceClass());
    }

    /* global element with anonymous type, containing element with anonymous type */
    public void testLocalElement1()
    {
        Type t1 = typeHelper.getType(TEST_URI, "gigino");
        assertNotNull(t1);
        assertEquals(Gigino.class, t1.getInstanceClass());
        assertTrue(t1 == typeHelper.getType(Gigino.class));
        Type t2 = typeHelper.getType(TEST_URI, "gigino$charlie");
        assertNotNull(t2);
        assertNotNull(t2.getInstanceClass());
        assertEquals(Gigino.Charlie.class, t2.getInstanceClass());
        assertEquals("sdo.test.anontype.Gigino$Charlie", t2.getInstanceClass().getName());
        Type t2b = typeHelper.getType(Gigino.Charlie.class);
        assertNotNull(t2b);
        System.out.println(t2b.getURI());
        System.out.println(t2b.getName());
        assertTrue(t2 == t2b);
    }

    /* global type containing element with anonymous type */
    public void testLocalElement2()
    {
        Type t1 = typeHelper.getType(TEST_URI, "Gamma");
        assertNotNull(t1);
        assertEquals(Gamma.class, t1.getInstanceClass());
        assertTrue(t1 == typeHelper.getType(Gamma.class));
        Type t2 = typeHelper.getType(TEST_URI, "Gamma$charlie");
        assertNotNull(t2);
        assertNotNull(t2.getInstanceClass());
        assertEquals(Gamma.Charlie.class, t2.getInstanceClass());
        assertEquals("sdo.test.anontype.Gamma$Charlie", t2.getInstanceClass().getName());
        Type t2b = typeHelper.getType(Gamma.Charlie.class);
        assertNotNull(t2b);
        System.out.println(t2b.getURI());
        System.out.println(t2b.getName());
        assertTrue(t2 == t2b);
    }

    /* global type containing attribute with anonymous type */
    public void testLocalAttribute()
    {
        Type t1 = typeHelper.getType(TEST_URI, "Gamma");
        assertNotNull(t1);
        Type t2 = typeHelper.getType(TEST_URI, "Gamma@ray");
        assertNotNull(t2);
        assertEquals(1, t2.getBaseTypes().size());
        Type intType = typeHelper.getType(SDO_URI, "Int");
        assertEquals(intType, t2.getBaseTypes().get(0));
        assertEquals(int.class, t2.getInstanceClass());
    }

    /* global type containing element with anonymous type, twice nested */
    public void testLocalElement3()
    {
        Type t1 = typeHelper.getType(TEST_URI, "Delta");
        assertNotNull(t1);
        assertEquals(Delta.class, t1.getInstanceClass());
        assertTrue(t1 == typeHelper.getType(Delta.class));
        Type t2 = typeHelper.getType(TEST_URI, "Delta$charlie");
        assertNotNull(t2);
        assertNotNull(t2.getInstanceClass());
        assertEquals(Delta.Charlie.class, t2.getInstanceClass());
        assertEquals("sdo.test.anontype.Delta$Charlie", t2.getInstanceClass().getName());
        assertTrue(t2 == typeHelper.getType(Delta.Charlie.class));
        Type t3 = typeHelper.getType(TEST_URI, "Delta$charlie$omega");
        assertNotNull(t3);
        assertNotNull(t3.getInstanceClass());
        assertEquals(Delta.Charlie.Omega.class, t3.getInstanceClass());
        assertEquals("sdo.test.anontype.Delta$Charlie$Omega", t3.getInstanceClass().getName());
        assertTrue(t3 == typeHelper.getType(Delta.Charlie.Omega.class));
    }
}
