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

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author Wing Yew Poon
 */
public class TypeTest extends MetaDataTest
{
    public TypeTest(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        
        TestSuite suite = new TestSuite();
        suite.addTest(new TypeTest("testInstance"));
        suite.addTest(new TypeTest("testInstance2"));
        suite.addTest(new TypeTest("testBaseTypes"));
        suite.addTest(new TypeTest("testBaseTypesNone"));
        suite.addTest(new TypeTest("testProperties"));
        suite.addTest(new TypeTest("testJavaClass"));
        suite.addTest(new TypeTest("testInstanceProperties"));
        suite.addTest(new TypeTest("testMarshalAndUnmarshal"));
        
        // or
        //TestSuite suite = new TestSuite(TypeTest.class);
        return suite;
    }

    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(suite());
    }

    public void testInstance()
    {
        String NS = "http://sdo/test/derivation";
        Type itemType = typeHelper.getType(NS, "ItemType");
        assertEquals(sdo.test.derivation.ItemType.class,
                     itemType.getInstanceClass());
        // item from class
        DataObject item1 = 
            factory.create(sdo.test.derivation.ItemType.class);
        assertNotNull(item1);
        assertTrue(item1 instanceof sdo.test.derivation.ItemType);
        assertTrue(itemType.isInstance(item1)); 
        // item from type
        DataObject item2 = 
            factory.create(itemType);
        assertNotNull(item2);
        assertTrue(item2 instanceof sdo.test.derivation.ItemType);
        assertTrue(itemType.isInstance(item2));
        Type productType = typeHelper.getType(NS, "ProductType");
        assertEquals(sdo.test.derivation.ProductType.class,
                     productType.getInstanceClass());
        DataObject product = factory.create(productType);
        assertNotNull(product);
        assertTrue(product instanceof sdo.test.derivation.ProductType);
        assertTrue(productType.isInstance(product)); 
    }

    /* test Type.isInstance(Object) for type defined using XSDHelper.define() */
    public void testInstance2() throws Exception
    {
        File f = getResourceFile("type", "derivation2.xsd_");
        InputStream in = new FileInputStream(f);
        xsdHelper.define(in, f.toURL().toString());
        in.close();
        String NS = "http://sdo/test/derivation2";
        Type productType = typeHelper.getType(NS, "ProductType");
        DataObject product = factory.create(productType);
        assertNotNull(product);
        assertTrue(productType.isInstance(product));
    }

    public void testBaseTypes()
    {
        String NS = "http://sdo/test/derivation";
        Type productType = typeHelper.getType(NS, "ProductType");
        Type shirtType = typeHelper.getType(NS, "ShirtType");
        List shirtBaseTypes = shirtType.getBaseTypes();
        assertEquals(1, shirtBaseTypes.size());
        assertEquals(productType, shirtBaseTypes.get(0));
        DataObject shirt = factory.create(shirtType);
        assertTrue(shirt instanceof sdo.test.derivation.ShirtType);
        assertTrue(shirt instanceof sdo.test.derivation.ProductType);
        assertTrue(shirtType.isInstance(shirt));
        assertTrue(productType.isInstance(shirt));
    }

    public void testBaseTypesNone() throws Exception
    {
        System.out.println("testBaseTypesNone()");
        File f = new File(RESOURCES + S + "sdocomp" + S + "bugs" + S + "api_test.xsd");
        InputStream in = new FileInputStream(f);
        List<Type> types = (List<Type>)xsdHelper.define(in, f.toURL().toString());
        in.close();
        System.out.println("types defined:");
        for (Type t : types)
        {
            System.out.println(t);
        }
        Type testType = typeHelper.getType("http://www.example.com/api_test",
                                           "APITest");
        assertNotNull(testType);
        List<Type> baseTypes = (List<Type>)testType.getBaseTypes();
        System.out.println("base types:");
        for (Type t : baseTypes)
        {
            System.out.println(t);
        }
        // CR326414 -
        // Tuscany has no base type
        //assertEquals(0, baseTypes.size());
        // we have DataObject as base type
        assertEquals(1, baseTypes.size());
        Type dataObjectType = typeHelper.getType("commonj.sdo", "DataObject");
        assertEquals(dataObjectType, baseTypes.get(0));
    }

    /*
    private void printProperties(List<Property> props)
    {
        for (Property p: props)
        {
            System.out.println("  " + p.getName());
        }
    }
    */

    public void testProperties()
    {
        String NS = "http://sdo/test/derivation";
        Type productType = typeHelper.getType(NS, "ProductType");
        Type shirtType = typeHelper.getType(NS, "ShirtType");
        List<Property> productProperties = productType.getProperties();
        //System.out.println("product");
        //printProperties(productProperties);
        assertEquals(2, productProperties.size());
        assertEquals("name", productProperties.get(0).getName());
        assertEquals("number", productProperties.get(1).getName());
        List<Property> shirtProperties = shirtType.getProperties();
        //System.out.println("shirt");
        //printProperties(shirtProperties);
        assertEquals(4, shirtProperties.size());
        assertEquals("name", shirtProperties.get(0).getName());
        assertEquals("number", shirtProperties.get(1).getName());
        assertEquals("size", shirtProperties.get(2).getName());
        assertEquals("color", shirtProperties.get(3).getName());
        List<Property> shirtDeclaredProperties = shirtType.getDeclaredProperties();
        //System.out.println("shirt only");
        //printProperties(shirtDeclaredProperties);
        assertEquals(2, shirtDeclaredProperties.size());
        assertEquals("size", shirtDeclaredProperties.get(0).getName());
        assertEquals("color", shirtDeclaredProperties.get(1).getName());
    }

    /* test that {javax.sdo/java}javaClass global/open content Property 
       is in the type system */
    public void testJavaClass()
    {
        // javaClass is a static field defined in the superclass
        assertNotNull(javaClass);
        assertEquals(stringType, javaClass.getType());
    }

    /* test metadata on the metadata */
    public void testInstanceProperties()
    {
        String NS = "http://sdo/test/type/metadata";
        DataObject _p1 = factory.create("commonj.sdo", "Property");
        _p1.set("name", "myBooleanProperty");
        _p1.set("type", booleanType);
        Property p1 = typeHelper.defineOpenContentProperty(NS, _p1);
        DataObject _p2 = factory.create("commonj.sdo", "Property");
        _p2.set("name", "myStringProperty");
        _p2.set("type", stringType);
        Property p2 = typeHelper.defineOpenContentProperty(NS, _p2);

        DataObject prototype = factory.create("commonj.sdo", "Type");
        prototype.set("uri", NS);
        prototype.set("name", "MyType");
        prototype.set(p1, true);
        prototype.set(p2, "myStringValue");
        Type t = typeHelper.define(prototype);
        assertNotNull(t);
        assertEquals(NS, t.getURI());
        assertEquals("MyType", t.getName());
        List<Property> props = (List<Property>)t.getInstanceProperties();
        assertNotNull(props);
        assertEquals(2, props.size());
        Property p = findProperty(props, "myBooleanProperty");
        assertEquals(p1, p);
        p = findProperty(props, "myStringProperty");
        assertEquals(p2, p);
        assertEquals(Boolean.TRUE, t.get(p1));
        assertEquals("myStringValue", t.get(p2));
    }

    /* test marshalling a {javax.sdo}Type DataObject */
    public void testMarshalAndUnmarshal() throws IOException
    {
        // create a new Type for Customers
        DataObject customerType = factory.create("commonj.sdo", "Type");
        customerType.set("uri", "http://example.com/customer");
        customerType.set("name", "Customer");

        // create a customer number property
        DataObject custNumProperty = customerType.createDataObject("property");
        custNumProperty.set("name", "custNum");
        custNumProperty.set("type", intType);

        // create a first name property
        DataObject firstNameProperty = customerType.createDataObject("property");
        firstNameProperty.set("name", "firstName");
        firstNameProperty.set("type", stringType);

        // create a last name property
        DataObject lastNameProperty = customerType.createDataObject("property");
        lastNameProperty.set("name", "lastName");
        lastNameProperty.set("type", stringType);

        Writer out = new StringWriter();
        xmlHelper.save(xmlHelper.createDocument(customerType, "commonj.sdo", "type"), 
                       out, new davos.sdo.Options().setSavePrettyPrint());
        String s = out.toString();
        System.out.println(s);

        // check that the marshalled Type data object can be unmarshalled correctly
        Type typeType = typeHelper.getType("commonj.sdo", "Type");
        DataObject customerType2 = xmlHelper.load(s).getRootObject();
        assertEquals(typeType, customerType2.getType());
        assertEquals("http://example.com/customer", customerType2.get("uri"));
        assertEquals("Customer", customerType2.get("name"));
        DataObject custNumProperty2 = customerType2.getDataObject("property[1]");
        assertNotNull(custNumProperty2);
        assertEquals("custNum", custNumProperty2.get("name"));
        assertEquals(intType, custNumProperty2.get("type"));
        DataObject firstNameProperty2 = customerType2.getDataObject("property[2]");
        assertNotNull(firstNameProperty2);
        assertEquals("firstName", firstNameProperty2.get("name"));
        assertEquals(stringType, firstNameProperty2.get("type"));
        DataObject lastNameProperty2 = customerType2.getDataObject("property[3]");
        assertNotNull(lastNameProperty2);
        assertEquals("lastName", lastNameProperty2.get("name"));
        assertEquals(stringType, lastNameProperty2.get("type"));

        // now define the type using the round-tripped data object
        Type t = typeHelper.define(customerType2);
        assertNotNull(t);
        assertEquals("http://example.com/customer", t.getURI());
        assertEquals("Customer", t.getName());
        List<Property> props = t.getProperties();
        Property p1 = props.get(0);
        assertEquals("custNum", p1.getName());
        assertEquals(intType, p1.getType());
        Property p2 = props.get(1);
        assertEquals("firstName", p2.getName());
        assertEquals(stringType, p2.getType());
        Property p3 = props.get(2);
        assertEquals("lastName", p3.getName());
        assertEquals(stringType, p3.getType());
    }
}
