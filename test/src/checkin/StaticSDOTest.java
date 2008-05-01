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
import java.util.List;
//import java.util.ArrayList;
import java.util.Iterator;
import java.math.BigDecimal;
import java.math.BigInteger;

import javax.sdo.DataObject;
import javax.sdo.Type;
import javax.sdo.Property;
import javax.sdo.helper.DataFactory;
import javax.sdo.helper.TypeHelper;
import javax.sdo.helper.XMLHelper;
import javax.sdo.helper.XMLDocument;

import davos.sdo.Options;
import davos.sdo.SDOContextFactory;
import davos.sdo.binding.BindingSystem;

import com.example.simple1.Quote;
import org.apache.xmlbeans.samples.xquery.employees.*;

import junit.framework.*;
import common.BaseTest;

/**
 * @author Wing Yew Poon
 */
public class StaticSDOTest extends BaseTest
{
    public StaticSDOTest(String name)
    {
        super(name);
    }

    public static Test suite()
    {

        TestSuite suite = new TestSuite();
        //suite.addTest(new StaticSDOTest("testTriggers"));
        suite.addTest(new StaticSDOTest("testCreate1"));
        suite.addTest(new StaticSDOTest("testCreate2"));
        suite.addTest(new StaticSDOTest("testCreate3"));
        suite.addTest(new StaticSDOTest("testCreate4"));
        suite.addTest(new StaticSDOTest("testLoad"));

        // or
        //TestSuite suite = new TestSuite(StaticSDOTest.class);
        return suite;
    }

    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(suite());
    }

    public static File dir;
    static
    {
        dir = new File(OUTPUTROOT + S + "checkin");
        dir.mkdirs();
    }

    private static final String SIMPLE_URI = "http://www.example.com/simple1";
    private static DataFactory factory = context.getDataFactory();
    private static TypeHelper typeHelper = context.getTypeHelper();
    private static XMLHelper xmlHelper = context.getXMLHelper();

    public void _testType(Type t)
    {
        List props = t.getProperties();
        for (Iterator i = props.iterator(); i.hasNext(); )
        {
            Property p = (Property)i.next();
            System.out.println(p.getName());
            System.out.println(p.getType());
            System.out.println("many? " + p.isMany());
        }
    }

    private void _triggerLoading1()
    {
        BindingSystem bs = context.getBindingSystem();
        // to trigger loading of BS and TS saved in the jar
        Type t = bs.loadGlobalPropertyByTopLevelElemQName(SIMPLE_URI, "quote").getType();
        assertNotNull(t);
        _testType(t);
    }

    private void _triggerLoading2()
    {
        BindingSystem bs = context.getBindingSystem();
        // to trigger loading of BS and TS saved in the jar
        Type t = bs.loadTypeByTypeName(SIMPLE_URI, "Quote");
        assertNotNull(t);
        _testType(t);
    }

    private void _triggerLoading3()
    {
        BindingSystem bs = context.getBindingSystem();
        // to trigger loading of BS and TS saved in the jar
        Type t = bs.getType(Quote.class);
        assertNotNull(t);
        _testType(t);
    }

    private Quote _create1()
    {
        return (Quote)factory.create(SIMPLE_URI, "Quote");
    }

    private Quote _create2()
    {
        return (Quote)factory.create(Quote.class);
    }

    private Quote _create3()
    {
        Type quoteType = typeHelper.getType(SIMPLE_URI, "Quote");
        assertNotNull(quoteType);
        _testType(quoteType);
        return (Quote)factory.create(quoteType);
    }

    private Quote _create4()
    {
        Type quoteType = typeHelper.getType(Quote.class);
        assertNotNull(quoteType);
        _testType(quoteType);
        return (Quote)factory.create(quoteType);
    }

    private void _set(Quote quote)
    {
        quote.setSymbol("fbnt");
        quote.setCompanyName("FlyByNightTechnology");
        quote.setPrice(new BigDecimal("1000.0"));
        quote.setOpen1(new BigDecimal("1000.0"));
        quote.setHigh(new BigDecimal("2000.0"));
        quote.setLow(new BigDecimal("1000.0"));
        quote.setVolume(1000);
        quote.setChange1(1000);

        Quote child1 = quote.createQuotes();
        child1.setPrice(new BigDecimal("1000.0"));
        Quote child2 = (Quote)factory.create(SIMPLE_URI, "Quote");
        child2.setPrice(new BigDecimal("1500.0"));
        List quotes = quote.getQuotes();
        assertNotNull(quotes);
        assertEquals(1, quotes.size());
        assertEquals(child1, quotes.get(0));
        quotes.add(child2);
        Quote child3 = quote.createQuotes();
        child3.setPrice(new BigDecimal("2000.0"));
        assertEquals(3, quotes.size());
        assertEquals(child3, quotes.get(2));
     }

    private void _save(Quote quote) throws Exception
    {
        //util.DataObjectPrinter.printDataObject((DataObject)quote);
        File f = new File(dir, "quote.xml");
        OutputStream out = new FileOutputStream(f);
        xmlHelper.save(xmlHelper.createDocument((DataObject)quote, SIMPLE_URI, "quote"), out,
            new Options().setSavePrettyPrint());
        out.close();
        compareXMLFiles(getResourceFile("checkin", "simple1.xml"), f, STRICT);
    }

    public void testCreate1() throws Exception
    {
        Quote quote = _create1();
        _set(quote);
        _save(quote);
    }

    public void testCreate2() throws Exception
    {
        Quote quote = _create2();
        _set(quote);
        _save(quote);
    }

    public void testCreate3() throws Exception
    {
        Quote quote = _create3();
        _set(quote);
        _save(quote);
    }

    public void testCreate4() throws Exception
    {
        Quote quote = _create4();
        _set(quote);
        _save(quote);
    }

    public void testTriggers()
    {
        //_triggerLoading1();
        //_triggerLoading2();
        _triggerLoading3();
    }

    public void testLoad() throws Exception
    {
        File f = getResourceFile("checkin", "employees.xml");
        InputStream in = new FileInputStream(f);
        XMLDocument doc = xmlHelper.load(in);
        in.close();
        /*
        System.out.println();
        System.out.println("root element:");
        System.out.println("  " + doc.getRootElementURI());
        System.out.println("  " + doc.getRootElementName());
        Employees employees = (Employees)doc.getRootObject();
        System.out.println("type:");
        System.out.println("  " + ((DataObject)employees).getType().getURI());
        System.out.println("  " + ((DataObject)employees).getType().getName());
        System.out.println(((DataObject)employees).getType().getProperties().size());
        System.out.println(((Property)((DataObject)employees).getType().getProperties().get(0)).getName());
        System.out.println(((Property)((DataObject)employees).getType().getProperties().get(0)).isMany());
        System.out.println(((DataObject)employees).getInstanceProperties().size());
        System.out.println(((Property)((DataObject)employees).getInstanceProperties().get(0)).getName());
        System.out.println(((Property)((DataObject)employees).getType().getProperties().get(0)).isMany());
        EmployeeType employee = employees.getEmployee(); // this leads to
        // java.lang.ClassCastException: java.util.ArrayList
        */
        DataObject employees = doc.getRootObject();
        //util.DataObjectPrinter.printDataObject(employees);
        List employeeList = employees.getList("employee");
        EmployeeType employee1 = (EmployeeType)employeeList.get(0);
        assertEquals("Fred Jones", employee1.getName());
        AddressType homeAddress = (AddressType)((DataObject)employee1).getList("address").get(0);
        assertEquals("900 Aurora Ave.", homeAddress.getStreet());
        assertEquals("Seattle", homeAddress.getCity());
        assertEquals("WA", homeAddress.getState());
        assertEquals(new BigInteger("98115"), homeAddress.getZip());
        // test use of path
        AddressType workAddress = (AddressType)((DataObject)employee1).get("address[location='work']");
        assertEquals("2011 152nd Avenue NE", workAddress.getStreet());
        assertEquals("Redmond", workAddress.getCity());
        assertEquals("WA", workAddress.getState());
        assertEquals(new BigInteger("98052"), workAddress.getZip());
        EmployeeType employee2 = (EmployeeType)employeeList.get(1);
        assertEquals("Sally Smith", employee2.getName());
        // test fix for CR290411 (complexType extending simpleContent)
        PhoneType phone = (PhoneType)((DataObject)employee2).get("phone[location='work']");
        assertEquals("work", phone.getLocation());
        assertEquals("(503)555-3856", phone.getValue());
        phone.setValue("(503)555-3859");
        assertEquals("(503)555-3859", ((DataObject)employee2).get("phone[location='work']/value"));
        assertEquals("(503)555-6951", ((DataObject)employee2).get("phone[location='home']/value"));
        assertEquals("(503)555-5152", ((DataObject)employee2).get("phone[location='mobile']/value"));
    }
}
