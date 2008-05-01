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

import java.util.*;
import javax.xml.namespace.QName;

import javax.sdo.*;
import javax.sdo.helper.*;

import davos.sdo.TypeXML;

import junit.framework.*;

/**
 * This class tests the predefined SDO Types.
 * @author Wing Yew Poon
 */
public class BuiltInTypesTest extends TestCase
{
    public BuiltInTypesTest(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        /*
        TestSuite suite = new TestSuite();
        suite.addTest(new BuiltInTypesTest("testType"));
        suite.addTest(new BuiltInTypesTest("testProperty"));
        */
        // or
        TestSuite suite = new TestSuite(BuiltInTypesTest.class);
        return suite;
    }

    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(suite());
    }

    private static final String SDO_JAVA_URI = "commonj.sdo/java";
    // use the static instance -
    // we are not going to use any types other than built-in types
    private static TypeHelper types = TypeHelper.INSTANCE;
    
    // SDO data types
    private void _testDataType(String name, Class c)
    {
        Type t = types.getType("commonj.sdo", name);
        assertEquals(name, t.getName());
        assertEquals("commonj.sdo", t.getURI());
        assertEquals(c, t.getInstanceClass());
        assertEquals(true, t.isDataType());
        assertEquals(false, t.isOpen());
        assertEquals(false, t.isSequenced());
        assertEquals(false, t.isAbstract());
    }

    public void testBoolean()
    {
        _testDataType("Boolean", Boolean.TYPE);
    }

    public void testByte()
    {
        _testDataType("Byte", Byte.TYPE);
    }

    public void testBytes()
    {
        byte[] bytes = new byte[1];
        _testDataType("Bytes", bytes.getClass());
    }

    public void testCharacter()
    {
        _testDataType("Character", Character.TYPE);
    }

    public void testDate()
    {
        _testDataType("Date", java.util.Date.class);
    }
        
    public void testDateTime()
    {
        _testDataType("DateTime", java.lang.String.class);
    }

    public void testDay()
    {
        _testDataType("Day", java.lang.String.class);
    }

    public void testDecimal()
    {
        _testDataType("Decimal", java.math.BigDecimal.class);
    }
        
    public void testDouble()
    {
        _testDataType("Double", Double.TYPE);
    }

    public void testDuration()
    {
        _testDataType("Duration", java.lang.String.class);
    }

    public void testFloat()
    {
        _testDataType("Float", Float.TYPE);
    }

    public void testInt()
    {
        _testDataType("Int", Integer.TYPE);
    }

    public void testInteger()
    {
        _testDataType("Integer", java.math.BigInteger.class);
    }
        
    public void testLong()
    {
        _testDataType("Long", Long.TYPE);
    }

    public void testMonth()
    {
        _testDataType("Month", java.lang.String.class);
    }

    public void testMonthDay()
    {
        _testDataType("MonthDay", java.lang.String.class);
    }

    public void testShort()
    {
        _testDataType("Short", Short.TYPE);
    }

    public void testString()
    {
        _testDataType("String", java.lang.String.class);
    }

    public void testStrings()
    {
        _testDataType("Strings", java.util.List.class);
    }
    
    public void testTime()
    {
        _testDataType("Time", java.lang.String.class);
    }

    public void testURI()
    {
        _testDataType("URI", java.lang.String.class);
    }

    public void testYear()
    {
        _testDataType("Year", java.lang.String.class);
    }

    public void testYearMonth()
    {
        _testDataType("YearMonth", java.lang.String.class);
    }

    public void testYearMonthDay()
    {
        _testDataType("YearMonthDay", java.lang.String.class);
    }

    // SDO Java data types
    private Type _testJavaDataType(String name, Class c)
    {
        Type t = types.getType("commonj.sdo/java", name);
        assertNotNull(t);
        assertEquals(name, t.getName());
        assertEquals("commonj.sdo/java", t.getURI());
        assertEquals(c, t.getInstanceClass());
        assertEquals(true, t.isDataType());
        assertEquals(false, t.isOpen());
        assertEquals(false, t.isSequenced());
        assertEquals(false, t.isAbstract());
        return t;
    }

    public void testBooleanObject()
    {
        TypeXML t = (TypeXML)_testJavaDataType("BooleanObject", Boolean.class);
        assertEquals(new QName(SDO_JAVA_URI, "BooleanObject"), t.getXMLSchemaType().getName());
    }

    public void testByteObject()
    {
        TypeXML t = (TypeXML)_testJavaDataType("ByteObject", Byte.class);
        assertEquals(new QName(SDO_JAVA_URI, "ByteObject"), t.getXMLSchemaType().getName());
    }

    public void testCharacterObject()
    {
        TypeXML t = (TypeXML)_testJavaDataType("CharacterObject", Character.class);
        assertEquals(new QName(SDO_JAVA_URI, "CharacterObject"), t.getXMLSchemaType().getName());
    }

    public void testDoubleObject()
    {
        TypeXML t = (TypeXML)_testJavaDataType("DoubleObject", Double.class);
        assertEquals(new QName(SDO_JAVA_URI, "DoubleObject"), t.getXMLSchemaType().getName());
    }

    public void testFloatObject()
    {
        TypeXML t = (TypeXML)_testJavaDataType("FloatObject", Float.class);
        assertEquals(new QName(SDO_JAVA_URI, "FloatObject"), t.getXMLSchemaType().getName());
    }

    public void testIntObject()
    {
        TypeXML t = (TypeXML)_testJavaDataType("IntObject", Integer.class);
        assertEquals(new QName(SDO_JAVA_URI, "IntObject"), t.getXMLSchemaType().getName());
    }

    public void testLongObject()
    {
        TypeXML t = (TypeXML)_testJavaDataType("LongObject", Long.class);
        assertEquals(new QName(SDO_JAVA_URI, "LongObject"), t.getXMLSchemaType().getName());
    }

    public void testShortObject()
    {
        TypeXML t = (TypeXML)_testJavaDataType("ShortObject", Short.class);
        assertEquals(new QName(SDO_JAVA_URI, "ShortObject"), t.getXMLSchemaType().getName());
    }

    // SDO abstract types
    public void testChangeSummaryType()
    {
        Type t = types.getType("commonj.sdo", "ChangeSummaryType");
        assertEquals("ChangeSummaryType", t.getName());
        assertEquals("commonj.sdo", t.getURI());
        assertEquals(javax.sdo.ChangeSummary.class, t.getInstanceClass());
        assertEquals(true, t.isDataType());
        assertEquals(false, t.isOpen());
        assertEquals(false, t.isSequenced());
        assertEquals(true, t.isAbstract());
    }

    public void testDataObject()
    {
        Type t = types.getType("commonj.sdo", "DataObject");
        assertEquals("DataObject", t.getName());
        assertEquals("commonj.sdo", t.getURI());
        assertEquals(javax.sdo.DataObject.class, t.getInstanceClass());
        assertEquals(false, t.isDataType());
        //assertEquals(false, t.isOpen());
        //assertEquals(false, t.isSequenced());
        assertEquals(true, t.isAbstract());
    }

    public void testObject()
    {
        Type t = types.getType("commonj.sdo", "Object");
        assertEquals("Object", t.getName());
        assertEquals("commonj.sdo", t.getURI());
        assertEquals(java.lang.Object.class, t.getInstanceClass());
        assertEquals(true, t.isDataType());
        assertEquals(false, t.isOpen());
        assertEquals(false, t.isSequenced());
        assertEquals(true, t.isAbstract());
    }

    // removed in 2.1
    /*
    public void testTextType()
    {
        Type t = types.getType("commonj.sdo", "TextType");
        assertEquals("TextType", t.getName());
        assertEquals("commonj.sdo", t.getURI());
        assertNull(t.getInstanceClass());
        //assertEquals(true, t.isDataType());
        //assertEquals(false, t.isOpen());
        //assertEquals(false, t.isSequenced());
        assertEquals(true, t.isAbstract());
        List properties = t.getDeclaredProperties();
        assertEquals(1, properties.size());
        Property p = (Property)properties.get(0);
        assertEquals("text", p.getName());
        assertEquals("String", p.getType().getName());
        assertTrue(p.isMany());
    }
    */

    // SDO model types
    public void testType()
    {
        Type t = types.getType("commonj.sdo", "Type");
        assertEquals("Type", t.getName());
        assertEquals("commonj.sdo", t.getURI());
        
        //assertEquals(false, t.isDataType());
        assertEquals(true, t.isOpen());
        //assertEquals(false, t.isSequenced());
        //assertEquals(false, t.isAbstract());
        List<Property> properties = t.getDeclaredProperties();
        assertFalse(properties.isEmpty());
        boolean _baseType = false;
        boolean _property = false;
        boolean _aliasName = false;
        boolean _name = false;
        boolean _uri = false;
        boolean _dataType = false;
        boolean _open = false;
        boolean _sequenced = false;
        boolean _abstract = false;
        for (Property p : properties)
        {
            String pName = p.getName();
            if (pName.equals("baseType"))
            {
                _baseType = true;
                assertTrue(p.isMany());
                assertEquals("Type", p.getType().getName());
            }
            if (pName.equals("property"))
            {
                _property = true;
                assertTrue(p.isContainment());
                assertTrue(p.isMany());
            }
            if (pName.equals("aliasName"))
            {
                _aliasName = true;
                assertTrue(p.isMany());
                assertEquals("String", p.getType().getName());
            }
            if (pName.equals("name"))
            {
                _name = true;
                assertEquals("String", p.getType().getName());
            }
            if (pName.equals("uri"))
            {
                _uri = true;
                assertEquals("String", p.getType().getName());
            }
            if (pName.equals("dataType"))
            {
                _dataType = true;
                assertEquals("Boolean", p.getType().getName());
            }
            if (pName.equals("open"))
            {
                _open = true;
                assertEquals("Boolean", p.getType().getName());
            }
            if (pName.equals("sequenced"))
            {
                _sequenced = true;
                assertEquals("Boolean", p.getType().getName());
            }
            if (pName.equals("abstract"))
            {
                _abstract = true;
                assertEquals("Boolean", p.getType().getName());
            }
        }
        assertTrue(_baseType);
        assertTrue(_property);
        assertTrue(_aliasName);
        assertTrue(_name);
        assertTrue(_uri);
        assertTrue(_dataType);
        assertTrue(_open);
        assertTrue(_sequenced);
        assertTrue(_abstract);
    }

    public void testProperty()
    {
        Type t = types.getType("commonj.sdo", "Property");
        assertEquals("Property", t.getName());
        assertEquals("commonj.sdo", t.getURI());
        
        //assertEquals(false, t.isDataType());
        assertEquals(true, t.isOpen());
        //assertEquals(false, t.isSequenced());
        //assertEquals(false, t.isAbstract());
        List<Property> properties = t.getDeclaredProperties();
        assertFalse(properties.isEmpty());
        boolean _aliasName = false; //0
        boolean _name = false; //1
        boolean _many = false; //2
        boolean _containment = false; //3
        boolean _type = false; //6
        boolean _default = false; //4
        boolean _readOnly = false; //5
        boolean _opposite = false; //7
        boolean _nullable = false; //8
        for (Property p : properties)
        {
            String pName = p.getName();
            if (pName.equals("aliasName"))
            {
                _aliasName = true;
                assertTrue(p.isMany());
                assertEquals("String", p.getType().getName());
            }
            if (pName.equals("name"))
            {
                _name = true;
                assertEquals("String", p.getType().getName());
            }
            if (pName.equals("many"))
            {
                _many = true;
                assertEquals("Boolean", p.getType().getName());
            }
            if (pName.equals("containment"))
            {
                _containment = true;
                assertEquals("Boolean", p.getType().getName());
            }
            if (pName.equals("type"))
            {
                _type = true;
                assertEquals("Type", p.getType().getName());
            }
            if (pName.equals("default"))
            {
                _default = true;
                assertEquals("Object", p.getType().getName());
            }
            if (pName.equals("readOnly"))
            {
                _readOnly = true;
                assertEquals("Boolean", p.getType().getName());
            }
            if (pName.equals("opposite"))
            {
                _opposite = true;
                assertEquals("Property", p.getType().getName());
            }
            if (pName.equals("nullable"))
            {
                _nullable = true;
                assertEquals("Boolean", p.getType().getName());
            }
        }
        assertTrue(_aliasName);
        assertTrue(_name);
        assertTrue(_many);
        assertTrue(_containment);
        assertTrue(_type);
        assertTrue(_default);
        assertTrue(_readOnly);
        assertTrue(_opposite);
        assertTrue(_nullable);
    }

}
