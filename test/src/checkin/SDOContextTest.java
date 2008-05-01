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

import java.io.*;
import java.util.*;
import java.math.BigDecimal;

import javax.sdo.*;
import javax.sdo.helper.*;

import davos.sdo.SDOContext;
import davos.sdo.SDOContextFactory;

import junit.framework.Test;
import junit.framework.TestSuite;
import common.BaseTest;

/**
 * @author Wing Yew Poon
 */
public class SDOContextTest extends BaseTest
{
    public SDOContextTest(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        
        TestSuite suite = new TestSuite();
        suite.addTest(new SDOContextTest("testEmptyContext"));
        suite.addTest(new SDOContextTest("testDefaultContext"));
        suite.addTest(new SDOContextTest("testContextIsolation1"));
        suite.addTest(new SDOContextTest("testContextIsolation2"));
        suite.addTest(new SDOContextTest("testContextIsolation3"));
        suite.addTest(new SDOContextTest("testContextIsolation4"));
        suite.addTest(new SDOContextTest("testCrossContextUse"));
        
        // or
        //TestSuite suite = new TestSuite(SDOContextTest.class);
        return suite;
    }

    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(suite());
    }

    private void _testBuiltInTypes(TypeHelper typeHelper)
    {
        assertNotNull(typeHelper.getType("commonj.sdo", "DataObject"));
        assertNotNull(typeHelper.getType("commonj.sdo", "Type"));
        assertNotNull(typeHelper.getType("commonj.sdo", "Property"));
        assertNotNull(typeHelper.getType("commonj.sdo", "Boolean"));
        assertNotNull(typeHelper.getType("commonj.sdo/java", "BooleanObject"));
    }

    private void _testCompiledTypes(TypeHelper typeHelper, boolean classesLoaded)
    {
        if (classesLoaded)
        {
            assertNotNull(typeHelper.getType(com.example.simple1.Quote.class));
            assertNotNull(typeHelper.getType("company.xsd", "CompanyType"));
            assertNotNull(typeHelper.getType("company.xsd", "DepartmentType"));
            assertNotNull(typeHelper.getType("company.xsd", "EmployeeType"));
        }
        else
        {
            assertNull(typeHelper.getType(com.example.simple1.Quote.class));
            assertNull(typeHelper.getType("company.xsd", "CompanyType"));
            assertNull(typeHelper.getType("company.xsd", "DepartmentType"));
            assertNull(typeHelper.getType("company.xsd", "EmployeeType"));
        }
    }


    public void testEmptyContext()
    {
        SDOContext ctx = SDOContextFactory.createNewSDOContext();
        TypeHelper typeHelper = ctx.getTypeHelper();
        // types from compiled jars in classpath are not known
        _testCompiledTypes(typeHelper, false);
        // built-in types are known
        _testBuiltInTypes(typeHelper);
    }

    public void testDefaultContext()
    {
        SDOContext ctx = SDOContextFactory.getGlobalSDOContext();
        TypeHelper typeHelper = ctx.getTypeHelper();
        // types from compiled jars in classpath are known
        _testCompiledTypes(typeHelper, true);
        // built-in types are known
        _testBuiltInTypes(typeHelper);
    }

    public void testContextIsolation1() throws Exception
    {
        SDOContext ctx1 = SDOContextFactory.createNewSDOContext(Thread.currentThread().getContextClassLoader());
        SDOContext ctx2 = SDOContextFactory.createNewSDOContext(Thread.currentThread().getContextClassLoader());
        TypeHelper typeHelper1 = ctx1.getTypeHelper();
        TypeHelper typeHelper2 = ctx2.getTypeHelper();
        _testCompiledTypes(typeHelper1, true);
        _testBuiltInTypes(typeHelper1);
        _testCompiledTypes(typeHelper2, true);
        _testBuiltInTypes(typeHelper2);
        assertNull(typeHelper1.getType("http://www.example.com/simple2", "Quote"));
        assertNull(typeHelper2.getType("http://www.example.com/simple2", "Quote"));
        XSDHelper xsdHelper1 = ctx1.getXSDHelper();
        File f = getResourceFile("checkin", "simple2.xsd_");
        InputStream in = new FileInputStream(f);
        xsdHelper1.define(in, f.toURL().toString());
        in.close();
        // now ctx1 knows the type but not ctx2
        assertNotNull(typeHelper1.getType("http://www.example.com/simple2", "Quote"));
        assertNull(typeHelper2.getType("http://www.example.com/simple2", "Quote"));
    }

    public void testContextIsolation2() throws Exception
    {
        SDOContext ctx1 = SDOContextFactory.createNewSDOContext();
        SDOContext ctx2 = SDOContextFactory.createNewSDOContext();
        TypeHelper typeHelper1 = ctx1.getTypeHelper();
        TypeHelper typeHelper2 = ctx2.getTypeHelper();
        assertNull(typeHelper1.getType("http://www.example.com/simple2", "Quote"));
        assertNull(typeHelper2.getType("http://www.example.com/simple2", "Quote"));
        XSDHelper xsdHelper1 = ctx1.getXSDHelper();
        File f = getResourceFile("checkin", "simple2.xsd_");
        InputStream in = new FileInputStream(f);
        xsdHelper1.define(in, f.toURL().toString());
        in.close();
        // now ctx1 knows the type but not ctx2
        assertNotNull(typeHelper1.getType("http://www.example.com/simple2", "Quote"));
        assertNull(typeHelper2.getType("http://www.example.com/simple2", "Quote"));
    }

    public void testContextIsolation3() throws Exception
    {
        SDOContext ctx1 = SDOContextFactory.createNewSDOContext(Thread.currentThread().getContextClassLoader());
        SDOContext ctx2 = SDOContextFactory.createNewSDOContext();
        TypeHelper typeHelper1 = ctx1.getTypeHelper();
        TypeHelper typeHelper2 = ctx2.getTypeHelper();
        _testCompiledTypes(typeHelper1, true);
        _testBuiltInTypes(typeHelper1);
        _testCompiledTypes(typeHelper2, false);
        _testBuiltInTypes(typeHelper2);
        assertNull(typeHelper1.getType("http://www.example.com/simple2", "Quote"));
        assertNull(typeHelper2.getType("http://www.example.com/simple2", "Quote"));
        XSDHelper xsdHelper1 = ctx1.getXSDHelper();
        File f = getResourceFile("checkin", "simple2.xsd_");
        InputStream in = new FileInputStream(f);
        xsdHelper1.define(in, f.toURL().toString());
        in.close();
        // now ctx1 knows the type but not ctx2
        assertNotNull(typeHelper1.getType("http://www.example.com/simple2", "Quote"));
        assertNull(typeHelper2.getType("http://www.example.com/simple2", "Quote"));
    }

    public void testContextIsolation4() throws Exception
    {
        SDOContext ctx1 = SDOContextFactory.createNewSDOContext();
        SDOContext ctx2 = SDOContextFactory.createNewSDOContext(Thread.currentThread().getContextClassLoader());
        TypeHelper typeHelper1 = ctx1.getTypeHelper();
        TypeHelper typeHelper2 = ctx2.getTypeHelper();
        _testCompiledTypes(typeHelper1, false);
        _testBuiltInTypes(typeHelper1);
        _testCompiledTypes(typeHelper2, true);
        _testBuiltInTypes(typeHelper2);
        assertNull(typeHelper1.getType("http://www.example.com/simple2", "Quote"));
        assertNull(typeHelper2.getType("http://www.example.com/simple2", "Quote"));
        XSDHelper xsdHelper1 = ctx1.getXSDHelper();
        File f = getResourceFile("checkin", "simple2.xsd_");
        InputStream in = new FileInputStream(f);
        xsdHelper1.define(in, f.toURL().toString());
        in.close();
        // now ctx1 knows the type but not ctx2
        assertNotNull(typeHelper1.getType("http://www.example.com/simple2", "Quote"));
        assertNull(typeHelper2.getType("http://www.example.com/simple2", "Quote"));
    }

    public void testCrossContextUse()
    {
        SDOContext ctx1 = SDOContextFactory.createNewSDOContext(Thread.currentThread().getContextClassLoader());
        SDOContext ctx2 = SDOContextFactory.createNewSDOContext(Thread.currentThread().getContextClassLoader());
        TypeHelper typeHelper1 = ctx1.getTypeHelper();
        TypeHelper typeHelper2 = ctx2.getTypeHelper();
        Type t1 = typeHelper1.getType("http://www.example.com/simple1", "Quote");
        Type t2 = typeHelper2.getType("http://www.example.com/simple1", "Quote");
        assertNotNull(t1);
        assertNotNull(t2);

        // create data object using ctx1
        DataFactory factory1 = ctx1.getDataFactory();
        try
        {
            DataObject unquote = factory1.create(t2); // this should fail
            fail("an exception should have been thrown on the create");
        }
        catch (Exception e)
        {
            System.out.println(e);
            assertTrue(e instanceof IllegalArgumentException);
        }
        
        DataObject quote = factory1.create(t1);
        assertNotNull(quote);
        quote.setString("symbol", "fbnt");
        quote.setString("companyName", "FlyByNightTechnology");
        quote.setBigDecimal("price", new BigDecimal("1000.0"));
        quote.setBigDecimal("open1", new BigDecimal("1000.0"));
        quote.setBigDecimal("high", new BigDecimal("2000.0"));
        quote.setBigDecimal("low", new BigDecimal("1000.0"));
        quote.setDouble("volume", 1000);
        quote.setDouble("change1", 1000);
        // try to save data object using ctx2
        XMLHelper xmlHelper2 = ctx2.getXMLHelper();
        try
        {
            String out = xmlHelper2.save(quote, "http://www.example.com/simple1", "quote");
            System.out.println(out);
            fail("should not be able to marshal data object across context");
        }
        catch (Exception e)
        {
            assertTrue(e instanceof IllegalArgumentException);
            assertTrue(e.getMessage().indexOf("different context than the one it was created with") > 0);
        }

        // saving using ctx1 works
        XMLHelper xmlHelper1 = ctx1.getXMLHelper();
        String out = xmlHelper1.save(quote, "http://www.example.com/simple1", "quote");
        System.out.println(out);

        DataFactory factory2 = ctx2.getDataFactory();
        DataObject subquote21 = factory2.create(t2);
        subquote21.setBigDecimal("price", new BigDecimal("1000.0"));
        DataObject subquote22 = factory2.create(t2);
        subquote22.setBigDecimal("price", new BigDecimal("2000.0"));
        DataObject subquote23 = factory2.create(t2);
        subquote23.setBigDecimal("price", new BigDecimal("1500.0"));
        List quotes2 = new ArrayList();
        quotes2.add(subquote21);
        quotes2.add(subquote22);
        quotes2.add(subquote23);

        // try setting a property to something from a different context
        try
        {
            quote.setList("quotes", quotes2);
        }
        catch (Exception e)
        {
            assertTrue(e instanceof IllegalArgumentException);
            assertTrue(e.getMessage().indexOf("created in a different context") > 0);
        }

        // try adding a data object from a different context
        try
        {
            List quotes = quote.getList("quotes");
            quotes.add(subquote21);
        }
        catch (Exception e)
        {
            assertTrue(e instanceof IllegalArgumentException);
            assertTrue(e.getMessage().indexOf("created in a different context") > 0);
        }

        DataObject subquote11 = factory1.create(t1);
        subquote11.setBigDecimal("price", new BigDecimal("1000.0"));
        DataObject subquote12 = factory1.create(t1);
        subquote12.setBigDecimal("price", new BigDecimal("2000.0"));
        List quotes = quote.getList("quotes");
        assertEquals(0, quotes.size());
        quotes.add(subquote11);
        quotes.add(subquote12);

        try
        {
            quote.set("quotes[2]", subquote22);
        }
        catch (Exception e)
        {
            assertTrue(e instanceof IllegalArgumentException);
            assertTrue(e.getMessage().indexOf("created in a different context") > 0);
        }
    }
}
