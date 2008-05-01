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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

import javax.sdo.Type;
import javax.sdo.Property;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author Wing Yew Poon
 */
public class InstanceClassTest extends MetaDataTest
{
    public InstanceClassTest(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        
        TestSuite suite = new TestSuite();
        suite.addTest(new InstanceClassTest("testInstance1"));
        suite.addTest(new InstanceClassTest("testInstance2"));
        suite.addTest(new InstanceClassTest("testInstance3"));
        suite.addTest(new InstanceClassTest("testInstance4"));
        
        // or
        //TestSuite suite = new TestSuite(InstanceClassTest.class);
        return suite;
    }

    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(suite());
    }

    /* compiled schema, jar in classpath */
    public void testInstance1()
    {
        System.out.println("testInstance1()");
        // non-data types (i.e. complex types)
        String uri = "http://www.example.com/choice";
        Type t1 = typeHelper.getType(uri, "ShirtType");
        Type t2 = typeHelper.getType(uri, "PantsType");
        assertEquals(com.example.choice.ShirtType.class, t1.getInstanceClass());
        assertEquals(com.example.choice.PantsType.class, t2.getInstanceClass());
        // javaClass is only set on data types
        assertNull(t1.get(javaClass));
        assertNull(t2.get(javaClass));
        assertEquals(0, t1.getInstanceProperties().size());
        assertEquals(0, t2.getInstanceProperties().size());

        // data types
        Type t3 = typeHelper.getType("http://sdo/test/global", "NumberSizeType");
        assertEquals(int.class, t3.getInstanceClass());
        assertEquals("int", t3.get(javaClass));
        Type t4 = typeHelper.getType("http://sdo/test/global", "SMLXSizeType");
        assertEquals(java.lang.String.class, t4.getInstanceClass());
        assertEquals("java.lang.String", t4.get(javaClass));
    }

    private static final String SIMPLE_XSD =
    "<xs:schema targetNamespace=\"http://sdo/test/simple\"" + newline +
    "  xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"" + newline +
    "  xmlns:tns=\"http://sdo/test/simple\">" + newline +
    "  <xs:simpleType name=\"SMLSizeType\">" + newline +
    "    <xs:restriction base=\"xs:token\">" + newline +
    "      <xs:enumeration value=\"S\"/>" + newline +
    "      <xs:enumeration value=\"M\"/>" + newline +
    "      <xs:enumeration value=\"L\"/>" + newline +
    "    </xs:restriction>" + newline +
    "  </xs:simpleType>" + newline +
    "</xs:schema>";

    /* schema loaded using XSDHelper.define() */
    public void testInstance2() throws Exception
    {
        System.out.println("testInstance2()");
        // non-data type (i.e. complex type)
        File f = getResourceFile("checkin", "simple2.xsd_");
        InputStream in = new FileInputStream(f);
        List<Type> types = xsdHelper.define(in, f.toURL().toString());
        in.close();
        // instance class is null (not known) since schema is not compiled
        Type quoteType = typeHelper.getType("http://www.example.com/simple2", "Quote");
        assertNotNull(quoteType);
        assertEquals(1, types.size());
        Type t = types.get(0);
        assertEquals(quoteType, t);
        assertNull(t.getInstanceClass());
        assertNull(t.get(javaClass));
        assertEquals(0, t.getInstanceProperties().size());

        // data type
        types = xsdHelper.define(SIMPLE_XSD);
        Type sizeType = typeHelper.getType("http://sdo/test/simple", "SMLSizeType");
        assertNotNull(sizeType);
        assertEquals(1, types.size());
        t = types.get(0);
        assertEquals(sizeType, t);
        // instance class is known for data type
        assertEquals(java.lang.String.class, t.getInstanceClass());
        assertEquals("java.lang.String", t.get(javaClass));
        assertEquals(1, t.getInstanceProperties().size());
        assertEquals(javaClass, t.getInstanceProperties().get(0));
    }

    /* compiled schema, jar in classpath, sdojava:instanceClass annotation */
    public void testInstance3()
    {
        System.out.println("testInstance3()");
        // xs:int -> int 
        Type t1 = typeHelper.getType("http://sdo/test/instanceclass1", "a1");
        assertNotNull(t1);
        assertEquals(int.class, t1.getInstanceClass());
        assertEquals("int", t1.get(javaClass));
        // xs:integer restricted to int range -> int
        Type t2 = typeHelper.getType("http://sdo/test/instanceclass1", "a2");
        assertNotNull(t2);
        assertEquals(int.class, t2.getInstanceClass());
        assertEquals("int", t2.get(javaClass));
        // xs:integer -> java.math.BigInteger
        Type t3 = typeHelper.getType("http://sdo/test/instanceclass1", "a3");
        assertNotNull(t3);
        assertEquals(java.math.BigInteger.class, t3.getInstanceClass());
        assertEquals("java.math.BigInteger", t3.get(javaClass));
        // xs:integer with sdojava:instanceClass="java.lang.Short"
        Type t4 = typeHelper.getType("http://sdo/test/instanceclass1", "a4");
        assertNotNull(t4);
        assertEquals(java.lang.Short.class, t4.getInstanceClass());
        assertEquals("java.lang.Short", t4.get(javaClass));
        // another data type from schema with sdojava:instanceClass annotation
        Type t5 = typeHelper.getType("http://sdo/test/custom", "E");
        assertNotNull(t5);
        assertEquals(davos.sdo.test.MyIntList.class, t5.getInstanceClass());
        assertEquals("davos.sdo.test.MyIntList", t5.get(javaClass));
    }

    /* schema loaded using XSDHelper.define(), 
       sdojava:instanceClass annotation */
    public void testInstance4() throws Exception
    {
        System.out.println("testInstance4()");
        File f = new File(RESOURCES + S + "sdocomp" + S + "annotations.xsd");
        InputStream in = new FileInputStream(f);
        xsdHelper.define(in, f.toURL().toString());
        in.close();
        Type t = typeHelper.getType("http://www.example.com/test", "Qty");
        assertNotNull(t);
        assertEquals(java.lang.Short.class, t.getInstanceClass());
        assertEquals("java.lang.Short", t.get(javaClass));
        f = getResourceFile("type", "instanceclass2.xsd_");
        in = new FileInputStream(f);
        xsdHelper.define(in, f.toURL().toString());
        in.close();
        // xs:int -> int 
        Type t1 = typeHelper.getType("http://sdo/test/instanceclass2", "a1");
        assertNotNull(t1);
        assertEquals(int.class, t1.getInstanceClass());
        assertEquals("int", t1.get(javaClass));
        // xs:integer restricted to int range -> int
        Type t2 = typeHelper.getType("http://sdo/test/instanceclass2", "a2");
        assertNotNull(t2);
        assertEquals(int.class, t2.getInstanceClass());
        assertEquals("int", t2.get(javaClass));
        // xs:integer -> java.math.BigInteger
        Type t3 = typeHelper.getType("http://sdo/test/instanceclass2", "a3");
        assertNotNull(t3);
        assertEquals(java.math.BigInteger.class, t3.getInstanceClass());
        assertEquals("java.math.BigInteger", t3.get(javaClass));
        // xs:integer with sdojava:instanceClass="java.lang.Short"
        Type t4 = typeHelper.getType("http://sdo/test/instanceclass2", "a4");
        assertNotNull(t4);
        assertEquals(java.lang.Short.class, t4.getInstanceClass());
        assertEquals("java.lang.Short", t4.get(javaClass));
    }

}
