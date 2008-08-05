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

import javax.sdo.DataObject;
import javax.sdo.Type;
import javax.sdo.Property;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Test metadata on the metadata.
 * @author Wing Yew Poon
 */
public class PropertyTest extends MetaDataTest
{
    public PropertyTest(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        
        TestSuite suite = new TestSuite();
        suite.addTest(new PropertyTest("testXmlElement"));
        suite.addTest(new PropertyTest("testXmlElement2"));
        suite.addTest(new PropertyTest("testInstanceProperties"));
        suite.addTest(new PropertyTest("testInstanceProperties2"));
        
        // or
        //TestSuite suite = new TestSuite(PropertyTest.class);
        return suite;
    }

    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(suite());
    }

    private void _checkProperty(Property p)
    {
        System.out.println("property: " + p);
        List<Property> properties = (List<Property>)p.getInstanceProperties();
        System.out.println("  instance properties (" + properties.size() + "):");
        for (Property ip : properties)
        {
            System.out.println("    " + ip.getName());
            System.out.println("    " + p.get(ip));
        }
    }

    /* test that {javax.sdo/xml}xmlElement global/open content Property 
       is in the type system */
    public void testXmlElement()
    {
        // xmlElement is a static field defined in the superclass
        assertNotNull(xmlElement);
        assertEquals(booleanType, xmlElement.getType());
    }

    public void testXmlElement2()
    {
        Property xmlElement2 = xsdHelper.getGlobalProperty("commonj.sdo/xml", "xmlElement", false);
        assertNotNull(xmlElement2);
        assertEquals(booleanType, xmlElement2.getType());
    }

    /* test instance properties of properties, of dynamically defined type */
    public void testInstanceProperties()
    {
        String NS = "http://sdo/test/type/metadata";
        DataObject _p = factory.create("commonj.sdo", "Property");
        _p.set("name", "maxOccurs");
        _p.set("type", intType);
        Property p = typeHelper.defineOpenContentProperty(NS, _p);
        DataObject prototype = factory.create("commonj.sdo", "Type");
        prototype.set("uri", NS);
        prototype.set("name", "PropertyTest");
        prototype.set(p, 3);
        DataObject booleanProperty = prototype.createDataObject("property");
        booleanProperty.set("name", "boolean");
        booleanProperty.set("type", booleanType);
        booleanProperty.set(xmlElement, false);
        DataObject stringProperty = prototype.createDataObject("property");
        stringProperty.set("name", "string");
        stringProperty.set("type", stringType);
        stringProperty.set(xmlElement, true);
        stringProperty.set("many", true);
        stringProperty.set(p, 5);
        DataObject doubleProperty = prototype.createDataObject("property");
        doubleProperty.set("name", "double");
        doubleProperty.set("type", doubleType);
        Type t = typeHelper.define(prototype);
        assertNotNull(t);
        assertEquals(3, t.getProperties().size());
        // boolean Property has 1 open content property, xmlElement
        Property p1 = t.getProperty("boolean");
        _checkProperty(p1);
        List props = p1.getInstanceProperties();
        assertNotNull(props);
        assertEquals(1, props.size()); // 1 (xmlElement=false)
        Property p1_p1 = findProperty(props, "xmlElement");
        assertEquals(xmlElement, p1_p1);
        assertEquals(Boolean.FALSE, p1.get(p1_p1));
        // string Property has 2 open content properties, xmlElement and maxOccurs
        Property p2 = t.getProperty("string");
        _checkProperty(p2);
        props = p2.getInstanceProperties();
        assertNotNull(props);
        assertEquals(2, props.size()); // 2 (xmlElement=true, maxOccurs=5)
        Property p2_p1 = findProperty(props, "xmlElement");
        assertEquals(xmlElement, p2_p1);
        assertEquals(Boolean.TRUE, p2.get(p2_p1));
        Property p2_p2 = findProperty(props, "maxOccurs");
        assertEquals(p, p2_p2);
        assertEquals(new Integer(5), p2.get(p2_p2));
        // double Property has no open content properties
        Property p3 = t.getProperty("double");
        _checkProperty(p3);
        props = p3.getInstanceProperties();
        assertNotNull(props);
        assertEquals(0, props.size());
        assertNull(p3.get(xmlElement)); 
    }

    /* test instance properties, in particular xmlElement, of properties
       of a type from schema */
    public void testInstanceProperties2()
    {
        // type from schema
        Type t = typeHelper.getType("http://sdo/test/global", "ProductType");
        assertNotNull(t);
        // has 3 properties - 2 elements and 1 attribute
        // this part also tests the order of properties in a type
        List<Property> pList = t.getProperties();
        assertEquals(3, pList.size());
        Property nameProperty = pList.get(0);
        assertEquals("name", nameProperty.getName());
        assertEquals(stringType, nameProperty.getType());
        Property numberProperty = pList.get(1);
        assertEquals("number", numberProperty.getName());
        assertEquals(stringType, numberProperty.getType());
        Property sizeProperty = pList.get(2);
        assertEquals("size", sizeProperty.getName());
        assertEquals(intType, sizeProperty.getType());
        _checkProperty(nameProperty);
        _checkProperty(numberProperty);
        _checkProperty(sizeProperty);
        // properties each have 1 instance property, xmlElement
        // name is an element
        List<Property> props = nameProperty.getInstanceProperties();
        assertEquals(1, props.size());
        assertEquals(xmlElement, props.get(0));
        assertEquals(Boolean.TRUE, nameProperty.get(xmlElement));
        // number is an element
        props = numberProperty.getInstanceProperties();
        assertEquals(1, props.size());
        assertEquals(xmlElement, props.get(0));
        assertEquals(Boolean.TRUE, numberProperty.get(xmlElement));
        // size is an attribute
        props = sizeProperty.getInstanceProperties();
        assertEquals(1, props.size());
        assertEquals(xmlElement, props.get(0));
        assertEquals(Boolean.FALSE, sizeProperty.get(xmlElement));
    }
}
