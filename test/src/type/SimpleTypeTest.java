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

import java.util.List;

import javax.sdo.Type;
import javax.sdo.helper.TypeHelper;

import junit.framework.Test;
import junit.framework.TestSuite;

import common.BaseTest;

/**
 * @author Wing Yew Poon
 */
public class SimpleTypeTest extends BaseTest
{
    public SimpleTypeTest(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        
        TestSuite suite = new TestSuite();
        suite.addTest(new SimpleTypeTest("testBaseType"));
        
        // or
        //TestSuite suite = new TestSuite(SimpleTypeTest.class);
        return suite;
    }

    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(suite());
    }

    private static TypeHelper typeHelper = context.getTypeHelper();

    private void _testNoBaseType(Type t)
    {
        assertTrue(t.isDataType());
        List baseTypes = t.getBaseTypes();
        assertEquals(0, baseTypes.size());
        assertEquals(int.class, t.getInstanceClass());
        assertEquals("int", t.get(MetaDataTest.javaClass));
    }

    private void _testBaseType(Type t, String baseTypeName, 
                               Class instanceClass, String javaClass)
    {
        assertTrue(t.isDataType());
        List baseTypes = t.getBaseTypes();
        assertEquals(1, baseTypes.size());
        Type baseType = (Type)baseTypes.get(0);
        assertEquals("commonj.sdo", baseType.getURI());
        assertEquals(baseTypeName, baseType.getName());
        assertEquals(instanceClass, t.getInstanceClass());
        assertEquals(javaClass, t.get(MetaDataTest.javaClass));
    }

    /* The spec says (p.79, section 9.2.2):
       "When the XSD type is integer, positiveInteger, negativeInteger, 
       nonPositiveInteger, nonNegativeInteger, long, or unsignedLong, 
       and there are facets (minInclusive, maxInclusive, minExclusive, 
       maxExclusive, enumeration) constraining the range to be within 
       the range of int, then the Java instance class is int and the 
       base is null unless the base Type's instance class is also int."
     */
    public void testBaseType() throws Exception
    {
        String uri = "http://sdo/test/instanceclass1";
        // integer
        Type i1a = typeHelper.getType(uri, "i1a");
        Type i1b = typeHelper.getType(uri, "i1b");
        _testNoBaseType(i1a);
        _testBaseType(i1b, "Integer", java.math.BigInteger.class, "java.math.BigInteger");
        // nonPositiveInteger
        Type i2a = typeHelper.getType(uri, "i2a");
        Type i2b = typeHelper.getType(uri, "i2b");
        _testNoBaseType(i2a);
        _testBaseType(i2b, "Integer", java.math.BigInteger.class, "java.math.BigInteger");
        // negativeInteger
        Type i3a = typeHelper.getType(uri, "i3a");
        Type i3b = typeHelper.getType(uri, "i3b");
        _testNoBaseType(i3a);
        _testBaseType(i3b, "Integer", java.math.BigInteger.class, "java.math.BigInteger");
        // nonNegativeInteger
        Type i4a = typeHelper.getType(uri, "i4a");
        Type i4b = typeHelper.getType(uri, "i4b");
        _testNoBaseType(i4a);
        _testBaseType(i4b, "Integer", java.math.BigInteger.class, "java.math.BigInteger");
        // positiveInteger
        Type i5a = typeHelper.getType(uri, "i5a");
        Type i5b = typeHelper.getType(uri, "i5b");
        _testNoBaseType(i5a);
        _testBaseType(i5b, "Integer", java.math.BigInteger.class, "java.math.BigInteger");
        // long
        Type i6a = typeHelper.getType(uri, "i6a");
        Type i6b = typeHelper.getType(uri, "i6b");
        _testNoBaseType(i6a);
        _testBaseType(i6b, "Long", long.class, "long");
        // unsignedLong
        Type i7a = typeHelper.getType(uri, "i7a");
        Type i7b = typeHelper.getType(uri, "i7b");
        _testNoBaseType(i7a);
        _testBaseType(i7b, "Integer", java.math.BigInteger.class, "java.math.BigInteger");
    }
}
