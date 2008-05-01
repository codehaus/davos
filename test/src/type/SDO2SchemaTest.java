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
import java.util.*;

import javax.sdo.DataObject;
import javax.sdo.Type;
import javax.sdo.Property;

import junit.framework.*;

/**
 * @author Wing Yew Poon
 */
public class SDO2SchemaTest extends MetaDataTest
{
    public SDO2SchemaTest(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        
        TestSuite suite = new TestSuite();
        suite.addTest(new SDO2SchemaTest("testDataType"));
        suite.addTest(new SDO2SchemaTest("testDataTypeWithJavaClass"));
        suite.addTest(new SDO2SchemaTest("testSDOTypes"));
        suite.addTest(new SDO2SchemaTest("testSDOJavaTypes"));
        //suite.addTest(new SDO2SchemaTest("testSDOTypesRoundTrip"));
        
        // or
        //TestSuite suite = new TestSuite(SDO2SchemaTest.class);
        return suite;
    }

    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(suite());
    }

    public static File dir;
    static
    {
        dir = new File(OUTPUTROOT + S + "type");
        dir.mkdirs();
    }

    static String testURI = "http://test/sdo/sdo2xsd";

    private void _checkType(Type t)
    {
        System.out.println("type: " + t);
        List<Property> properties = (List<Property>)t.getInstanceProperties();
        System.out.println("  instance properties (" + properties.size() + "):");
        for (Property p : properties)
        {
            System.out.println("    " + p.getName());
            System.out.println("    " + t.get(p));
        }
        System.out.println("  instance class: " + t.getInstanceClass());
    }

    private void _checkProperties(Type t)
    {
        List<Property> properties = (List<Property>)t.getProperties();
        for (Property p : properties)
        {
            System.out.println(p.getName());
            assertFalse(p.isMany());
            assertFalse(p.isContainment());
            assertFalse(p.isNullable());
            assertNull(p.getDefault());
        }
    }

    private static final String FLAGTYPE1 =
    "  <xs:simpleType name=\"FlagType1\">" + newline +
    "    <xs:restriction base=\"xs:boolean\"/>" + newline +
    "  </xs:simpleType>";

    private static final String FLAGTYPE2 =
    "  <xs:simpleType name=\"FlagType2\" sdojava:instanceClass=\"java.lang.Boolean\">" + newline +
    "    <xs:restriction base=\"xs:boolean\"/>" + newline +
    "  </xs:simpleType>";

    /* test defining a data type dynamically */
    public void testDataType()
    {
        System.out.println("testDataType()");
        DataObject prototype = factory.create("commonj.sdo", "Type");
        prototype.set("uri", testURI);
        prototype.set("name", "FlagType1");
        prototype.set("dataType", true);
        prototype.set("baseType", booleanType);
        Type flagType = typeHelper.define(prototype);
        _checkType(flagType);
        assertEquals(boolean.class, flagType.getInstanceClass());
        List<Type> definedTypes = new ArrayList<Type>();
        definedTypes.add(flagType);
        String xsd = xsdHelper.generate(definedTypes);
        System.out.println(xsd);
        assertTrue(xsd.indexOf(FLAGTYPE1) > 0);
    }

    /* test defining a data type dynamically, setting javaClass */
    public void testDataTypeWithJavaClass()
    {
        System.out.println("testDataTypeWithJavaClass()");
        DataObject prototype = factory.create("commonj.sdo", "Type");
        prototype.set("uri", testURI);
        prototype.set("name", "FlagType2");
        prototype.set("dataType", true);
        prototype.set("baseType", booleanType);
        prototype.set(javaClass, "java.lang.Boolean");
        Type flagType = typeHelper.define(prototype);
        _checkType(flagType);
        assertEquals(java.lang.Boolean.class, flagType.getInstanceClass());
        assertEquals("java.lang.Boolean", flagType.get(javaClass));
        assertEquals(1, flagType.getInstanceProperties().size());
        List<Type> definedTypes = new ArrayList<Type>();
        definedTypes.add(flagType);
        String xsd = xsdHelper.generate(definedTypes);
        System.out.println(xsd);
        assertTrue(xsd.indexOf(FLAGTYPE2) > 0);
    }

    /* define a type containing a property of each built-in SDO data type
       - test SDO-to-XSD mapping of the types */
    public void testSDOTypes() throws Exception
    {
        System.out.println("testSDOTypes()");
        DataObject prototype = factory.create("commonj.sdo", "Type");
        prototype.set("uri", testURI);
        prototype.set("name", "TestType1");
        DataObject booleanProperty = prototype.createDataObject("property");
        booleanProperty.set("name", "boolean");
        booleanProperty.set("type", booleanType);
        DataObject byteProperty = prototype.createDataObject("property");
        byteProperty.set("name", "byte");
        byteProperty.set("type", byteType);
        DataObject bytesProperty = prototype.createDataObject("property");
        bytesProperty.set("name", "bytes");
        bytesProperty.set("type", bytesType);
        DataObject characterProperty = prototype.createDataObject("property");
        characterProperty.set("name", "character");
        characterProperty.set("type", characterType);
        DataObject dateProperty = prototype.createDataObject("property");
        dateProperty.set("name", "date");
        dateProperty.set("type", dateType);
        DataObject dateTimeProperty = prototype.createDataObject("property");
        dateTimeProperty.set("name", "dateTime");
        dateTimeProperty.set("type", dateTimeType);
        DataObject dayProperty = prototype.createDataObject("property");
        dayProperty.set("name", "day");
        dayProperty.set("type", dayType);
        DataObject decimalProperty = prototype.createDataObject("property");
        decimalProperty.set("name", "decimal");
        decimalProperty.set("type", decimalType);
        DataObject doubleProperty = prototype.createDataObject("property");
        doubleProperty.set("name", "double");
        doubleProperty.set("type", doubleType);
        DataObject durationProperty = prototype.createDataObject("property");
        durationProperty.set("name", "duration");
        durationProperty.set("type", durationType);
        DataObject floatProperty = prototype.createDataObject("property");
        floatProperty.set("name", "float");
        floatProperty.set("type", floatType);
        DataObject intProperty = prototype.createDataObject("property");
        intProperty.set("name", "int");
        intProperty.set("type", intType);
        DataObject integerProperty = prototype.createDataObject("property");
        integerProperty.set("name", "integer");
        integerProperty.set("type", integerType);
        DataObject longProperty = prototype.createDataObject("property");
        longProperty.set("name", "long");
        longProperty.set("type", longType);
        DataObject monthProperty = prototype.createDataObject("property");
        monthProperty.set("name", "month");
        monthProperty.set("type", monthType);
        DataObject monthDayProperty = prototype.createDataObject("property");
        monthDayProperty.set("name", "monthDay");
        monthDayProperty.set("type", monthDayType);
        DataObject objectProperty = prototype.createDataObject("property");
        objectProperty.set("name", "object");
        objectProperty.set("type", objectType);
        DataObject shortProperty = prototype.createDataObject("property");
        shortProperty.set("name", "short");
        shortProperty.set("type", shortType);
        DataObject stringProperty = prototype.createDataObject("property");
        stringProperty.set("name", "string");
        stringProperty.set("type", stringType);
        DataObject stringsProperty = prototype.createDataObject("property");
        stringsProperty.set("name", "strings");
        stringsProperty.set("type", stringsType);
        DataObject timeProperty = prototype.createDataObject("property");
        timeProperty.set("name", "time");
        timeProperty.set("type", timeType);
        DataObject uriProperty = prototype.createDataObject("property");
        uriProperty.set("name", "uri");
        uriProperty.set("type", uriType);
        DataObject yearProperty = prototype.createDataObject("property");
        yearProperty.set("name", "year");
        yearProperty.set("type", yearType);
        DataObject yearMonthProperty = prototype.createDataObject("property");
        yearMonthProperty.set("name", "yearMonth");
        yearMonthProperty.set("type", yearMonthType);
        DataObject yearMonthDayProperty = prototype.createDataObject("property");
        yearMonthDayProperty.set("name", "yearMonthDay");
        yearMonthDayProperty.set("type", yearMonthDayType);

        Type testType = typeHelper.define(prototype);
        _checkType(testType);
        _checkProperties(testType);
        List<Type> definedTypes = new ArrayList<Type>();
        definedTypes.add(testType);
        String xsd = xsdHelper.generate(definedTypes);
        System.out.println(xsd);
        File f = new File(dir, "basic1.xsd");
        Writer w = new FileWriter(f);
        w.write(xsd);
        w.close();
        compareXMLFiles(getResourceFile("type", "basic1.xsd_"), f);
    }

    /* define a type containing a property of each built-in SDO Java type
       - test SDO-to-XSD mapping of the types */
    public void testSDOJavaTypes() throws Exception
    {
        System.out.println("testSDOJavaTypes()");
        DataObject prototype = factory.create("commonj.sdo", "Type");
        prototype.set("uri", testURI);
        prototype.set("name", "TestType2");
        DataObject booleanProperty = prototype.createDataObject("property");
        booleanProperty.set("name", "boolean");
        booleanProperty.set("type", booleanObjectType);
        booleanProperty.set(xmlElement, false);
        DataObject byteProperty = prototype.createDataObject("property");
        byteProperty.set("name", "byte");
        byteProperty.set("type", byteObjectType);
        //byteProperty.set(xmlElement, true);
        DataObject characterProperty = prototype.createDataObject("property");
        characterProperty.set("name", "character");
        characterProperty.set("type", characterObjectType);
        DataObject doubleProperty = prototype.createDataObject("property");
        doubleProperty.set("name", "double");
        doubleProperty.set("type", doubleObjectType);
        DataObject floatProperty = prototype.createDataObject("property");
        floatProperty.set("name", "float");
        floatProperty.set("type", floatObjectType);
        DataObject intProperty = prototype.createDataObject("property");
        intProperty.set("name", "int");
        intProperty.set("type", intObjectType);
        DataObject longProperty = prototype.createDataObject("property");
        longProperty.set("name", "long");
        longProperty.set("type", longObjectType);
        DataObject shortProperty = prototype.createDataObject("property");
        shortProperty.set("name", "short");
        shortProperty.set("type", shortObjectType);
        Type testType = typeHelper.define(prototype);
        _checkType(testType);
        _checkProperties(testType);
        List<Type> definedTypes = new ArrayList<Type>();
        definedTypes.add(testType);
        String xsd = xsdHelper.generate(definedTypes);
        System.out.println(xsd);
        File f = new File(dir, "basic2.xsd");
        Writer w = new FileWriter(f);
        w.write(xsd);
        w.close();
        compareXMLFiles(getResourceFile("type", "basic2.xsd_"), f);
    }

    public void testSDOTypesRoundTrip() throws IOException
    {
        System.out.println("testSDOTypesRoundTrip()");
        File f = new File(dir, "basic1.xsd");
        InputStream in = new FileInputStream(f);
        List types = xsdHelper.define(in, f.toURL().toString());
        in.close();
        assertEquals(1, types.size());
        Type t = (Type)types.get(0);
        _checkProperties(t);
    }
}
