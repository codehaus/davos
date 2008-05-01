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

import javax.sdo.*;
import javax.sdo.helper.*;

import davos.sdo.Options;
import davos.sdo.PropertyXML;

import junit.framework.*;
import common.BaseTest;

/**
 * @author Wing Yew Poon
 */
public class ElementVsAttributeTest extends MetaDataTest
{
    public ElementVsAttributeTest(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        
        TestSuite suite = new TestSuite();
        //suite.addTest(new ElementVsAttributeTest("testDefault"));
        suite.addTest(new ElementVsAttributeTest("testXmlElement"));
        suite.addTest(new ElementVsAttributeTest("testMany"));
        
        // or
        //TestSuite suite = new TestSuite(ElementVsAttributeTest.class);
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

    private static final String testURI = "http://sdo/test/xml/elemVsAttr";

    public void testDefault()
    {
    }

    public void testXmlElement() throws Exception
    {
        DataObject prototype = factory.create("commonj.sdo", "Type");
        prototype.set("name", "Test1");
        prototype.set("uri", testURI);
        DataObject booleanProperty = prototype.createDataObject("property");
        booleanProperty.set("name", "boolean");
        booleanProperty.set("type", booleanType);
        booleanProperty.set(xmlElement, false);
        DataObject intProperty = prototype.createDataObject("property");
        intProperty.set("name", "int");
        intProperty.set("type", intType);
        intProperty.set(xmlElement, true);
        DataObject stringProperty = prototype.createDataObject("property");
        stringProperty.set("name", "string");
        stringProperty.set("type", stringType);
        stringProperty.set(xmlElement, true);
        Type t = typeHelper.define(prototype);

        // generate xsd
        // complexType Test1 has 2 elements and 1 attribute
        // Q. the elements and the attribute are of form qualified;
        // should this be so?
        List l = new ArrayList();
        l.add(t);
        String xsd = xsdHelper.generate(l);
        System.out.println(xsd);

        // marshal the data object
        // boolean is an atrribute and the int and string are elements
        DataObject dobj = factory.create(t);
        dobj.setBoolean("boolean", true);
        dobj.setInt("int", 100);
        dobj.setString("string", "test");
        File f = new File(dir, "test1.xml");
        OutputStream out = new FileOutputStream(f);
        XMLDocument doc = xmlHelper.createDocument(dobj, testURI, "test1");
        doc.setXMLDeclaration(false);
        xmlHelper.save(doc, out, new Options().setSavePrettyPrint());
        out.close();
    }

    // if property type is a data type, it is an element if it is many-valued
    public void testMany()
    {
        DataObject prototype = factory.create("commonj.sdo", "Type");
        prototype.set("name", "Test2");
        prototype.set("uri", testURI);
        DataObject property1 = prototype.createDataObject("property");
        property1.set("name", "a");
        property1.set("type", stringType);
        DataObject property2 = prototype.createDataObject("property");
        property2.set("name", "b");
        property2.set("type", stringType);
        property2.set("many", Boolean.TRUE);
        Type t = typeHelper.define(prototype);
        Property p1 = t.getProperty("a");
        assertFalse(p1.isMany());
        assertFalse(((PropertyXML)p1).isXMLElement());
        Property p2 = t.getProperty("b");
        assertTrue(p2.isMany());
        assertTrue(((PropertyXML)p2).isXMLElement());

        List l = new ArrayList();
        l.add(t);
        String xsd = xsdHelper.generate(l);
        System.out.println(xsd);
    }
}
