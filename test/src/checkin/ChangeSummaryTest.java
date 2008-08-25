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

import davos.sdo.DataObjectXML;
import davos.sdo.impl.common.Names;
import davos.sdo.impl.data.ChangeSummaryImpl;
import davos.sdo.impl.data.DataFactoryImpl;
import davos.sdo.impl.data.DataObjectBuilder;
import davos.sdo.impl.data.DataObjectBuilderImpl;
import davos.sdo.impl.data.DataObjectIterator;
import davos.sdo.impl.data.DataObjectIteratorImpl;
import davos.sdo.impl.helpers.DataGraphHelper;
import davos.sdo.impl.type.BuiltInTypeSystem;

import common.BaseTest;

import javax.sdo.DataObject;
import javax.sdo.Property;
import javax.sdo.Sequence;
import javax.sdo.helper.CopyHelper;
import javax.sdo.helper.EqualityHelper;
import javax.sdo.helper.XMLDocument;
import javax.sdo.helper.XMLHelper;
import javax.sdo.helper.DataFactory;

import junit.framework.Test;
import junit.framework.TestSuite;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.custommonkey.xmlunit.Diff;

/**
 * @author Radu Preotiuc-Pietro
 */
public class ChangeSummaryTest extends BaseTest
{
    public ChangeSummaryTest(String name)
    {
        super(name);
    }

    public static Test suite()
    {
/*
        TestSuite suite = new TestSuite();
        suite.addTest(new ChangeSummaryTest("testTypedUndo"));
//        suite.addTest(new ChangeSummaryTest("testUntypedUndo"));
        suite.addTest(new ChangeSummaryTest("testTypedChangeSummary"));
//        suite.addTest(new ChangeSummaryTest("testUntypedChangeSummary"));
//        suite.addTest(new ChangeSummaryTest("testCopyHelper"));
        suite.addTest(new ChangeSummaryTest("testEqualityHelper"));
//        suite.addTest(new ChangeSummaryTest("testCascadingDeletes"));
        suite.addTest(new ChangeSummaryTest("testDataObjectBuilder"));
        suite.addTest(new ChangeSummaryTest("testStAXChangeSummaryTyped"));
        suite.addTest(new ChangeSummaryTest("testSimpleList"));
*/
        TestSuite suite = new TestSuite(ChangeSummaryTest.class);
        return suite;
    }

    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(suite());
    }

    private static XMLHelper xmlHelper = context.getXMLHelper();
    private static CopyHelper copyHelper = context.getCopyHelper();
    private static EqualityHelper equalityHelper = context.getEqualityHelper();
    private static DataFactory dataFactory = context.getDataFactory();

    private void changeHelperTyped(DataObject company)
    {
        company.setString("name", "MegaCorp");
        List departments = company.getList("departments");
        DataObject department = (DataObject) departments.get(0);
        List employees = department.getList("employees");
        employees.remove(1);
        // We sould be able to test a simple removal here, without
        // setting the List back, but it's maybe more interesting this way
        department.set("employees", employees);
        DataObject newEmployee = department.createDataObject("employees");
        newEmployee.set("name", "Al Smith");
        newEmployee.set("SN", "E0004");
        newEmployee.setBoolean("manager", true);
        company.set("employeeOfTheMonth", newEmployee);
        // TODO(radup) test deletion of open content
    }

    public void testTypedUndo() throws IOException
    {
        File f = getResourceFile("checkin", "company2.xml");
        InputStream in = new FileInputStream(f);
        XMLDocument doc = xmlHelper.load(in);
        DataObject company = doc.getRootObject();
        in.close();

        DataObject copy = copyHelper.copy(company);
        DataGraphHelper.wrapWithDataGraph(company);
        ChangeSummaryImpl c = (ChangeSummaryImpl) company.getChangeSummary();
        c.beginLogging();
        changeHelperTyped(company);
        assertEquals(3, c.getInsertedObjects().size());
        assertEquals(3, c.getDeletedObjects().size());
        assertEquals("MegaCorp", company.get("name"));
        assertEquals("E0004", ((DataObject) company.get("employeeOfTheMonth")).get("SN"));
        List employees = ((DataObject) company.getList("departments").get(0)).getList("employees");
        assertEquals("Jane Doe", ((DataObject) employees.get(1)).get("name"));
        assertEquals("Al Smith", ((DataObject) employees.get(2)).get("name"));

        verifyIterator(company);
        c.undoChanges();
        assertEquals("ACME", company.get("name"));
        assertEquals("E0002", ((DataObject) company.get("employeeOfTheMonth")).get("SN"));
        employees = ((DataObject) company.getList("departments").get(0)).getList("employees");
        assertEquals(3, employees.size());
        assertEquals("Mary Smith", ((DataObject) employees.get(1)).get("name"));
        assertEquals("Jane Doe", ((DataObject) employees.get(2)).get("name"));
        assertTrue(equalityHelper.equal(copy, company));
    }

    private void verifyIterator(DataObject company)
    {
        DataObjectIterator doIterator = new DataObjectIteratorImpl((DataObjectXML) company);
        DataObjectIterator.Change entry;
        entry = doIterator.next();
        assertEquals("departments", entry.getProperty().getName());
        DataObjectXML department = (DataObjectXML) entry.getValue();
        assertEquals("AdvancedTechnologies", department.get("name"));
        assertEquals(DataObjectIterator.Change.SAME, entry.getChangeType());
        entry = doIterator.next();
        assertEquals("name", entry.getProperty().getName());
        assertEquals("ACME", entry.getValue());
        assertEquals(DataObjectIterator.Change.OLD, entry.getChangeType());
        entry = doIterator.next();
        assertEquals("name", entry.getProperty().getName());
        assertEquals("MegaCorp", entry.getValue());
        assertEquals(DataObjectIterator.Change.NEW, entry.getChangeType());
        entry = doIterator.next();
        assertEquals("employeeOfTheMonth", entry.getProperty().getName());
        assertEquals("E0002", ((DataObject) entry.getValue()).get("SN"));
        assertEquals(DataObjectIterator.Change.OLD, entry.getChangeType());
        entry = doIterator.next();
        assertEquals("employeeOfTheMonth", entry.getProperty().getName());
        assertEquals("E0004", ((DataObject) entry.getValue()).get("SN"));
        assertEquals(DataObjectIterator.Change.NEW, entry.getChangeType());
        assertFalse(doIterator.hasNext());
        doIterator = new DataObjectIteratorImpl(department);
        entry = doIterator.next();
        assertEquals("employees", entry.getProperty().getName());
        assertEquals("John Jones", ((DataObject) entry.getValue()).get("name"));
        assertEquals(DataObjectIterator.Change.NEW, entry.getChangeType());
        entry = doIterator.next();
        assertEquals("employees", entry.getProperty().getName());
        assertEquals("Jane Doe", ((DataObject) entry.getValue()).get("name"));
        assertEquals(DataObjectIterator.Change.NEW, entry.getChangeType());
        entry = doIterator.next();
        assertEquals("employees", entry.getProperty().getName());
        assertEquals("Al Smith", ((DataObject) entry.getValue()).get("name"));
        assertEquals(DataObjectIterator.Change.NEW, entry.getChangeType());
        entry = doIterator.next();
        assertEquals("employees", entry.getProperty().getName());
        assertEquals("John Jones", ((DataObject) entry.getValue()).get("name"));
        assertEquals(DataObjectIterator.Change.OLD, entry.getChangeType());
        entry = doIterator.next();
        assertEquals("employees", entry.getProperty().getName());
        assertEquals("Mary Smith", ((DataObject) entry.getValue()).get("name"));
        assertEquals(DataObjectIterator.Change.OLD, entry.getChangeType());
        entry = doIterator.next();
        assertEquals("employees", entry.getProperty().getName());
        assertEquals("Jane Doe", ((DataObject) entry.getValue()).get("name"));
        assertEquals(DataObjectIterator.Change.OLD, entry.getChangeType());
        entry = doIterator.next();
        assertEquals("name", entry.getProperty().getName());
        assertEquals("AdvancedTechnologies", entry.getValue());
        assertEquals(DataObjectIterator.Change.SAME, entry.getChangeType());
        entry = doIterator.next();
        assertEquals("location", entry.getProperty().getName());
        assertEquals(DataObjectIterator.Change.SAME, entry.getChangeType());
        entry = doIterator.next();
        assertEquals("number", entry.getProperty().getName());
        assertEquals(DataObjectIterator.Change.SAME, entry.getChangeType());
        assertFalse(doIterator.hasNext());
    }

    public void testTypedChangeSummary() throws IOException
    {
        File f = getResourceFile("checkin", "company2.xml");
        InputStream in = new FileInputStream(f);
        XMLDocument doc = xmlHelper.load(in);
        DataObject company = doc.getRootObject();
        in.close();
        DataGraphHelper.wrapWithDataGraph(company, doc.getRootElementURI(), doc.getRootElementName());
        ChangeSummaryImpl c = (ChangeSummaryImpl) company.getChangeSummary();
        c.beginLogging();
        changeHelperTyped(company);

        String s = xmlHelper.save(company.getRootObject(), Names.URI_SDO, Names.SDO_DATAGRAPH);
        // We check the value of s with substr because it is easier
        // and more robust to random allowable variations in the output content
        int i1, i2, i3;
        i1 = s.indexOf(" create=\""); i2 = s.indexOf('"', i1 + 9);
        i3 = s.indexOf("E0004", i1);
        assertTrue(i3 < i2 && i3 > i1);
        i1 = s.indexOf(" delete=\""); i2 = s.indexOf('"', i1 + 9);
        i3 = s.indexOf("E0002", i1);
        assertTrue(i3 < i2 && i3 > i1);
        i2 = s.indexOf("<employees name=\"Mary Smith\" SN=\"E0002\" manager=\"true\"/>");
        i1 = s.indexOf("<employees name=\"John Jones\" SN=\"E0001\"/>");
        i3 = s.indexOf("<employees name=\"Jane Doe\" SN=\"E0003\"/>");
//        i1 = s.indexOf("<employees sdo:ref=\"#/company/departments[1]/employees[1]\"/>");
//        i3 = s.indexOf("<employees sdo:ref=\"#/company/departments[1]/employees[2]\"/>");
        assertTrue(0 < i1 && i1 < i2 && i2 < i3);

        company = xmlHelper.load(serializedTypedGraph).getRootObject().getDataObject("company");
        c = (ChangeSummaryImpl) company.getChangeSummary();
        assertEquals(1, c.getInsertedObjects().size());
        assertEquals(1, c.getDeletedObjects().size());
        ChangeSummaryImpl.Change[] changes = c.getModifiedObjects().get(company);
        Property prop = company.getInstanceProperty("name");
        ChangeSummaryImpl.Change change = findChange(changes, prop);
        assertNotNull(change);
        assertTrue(change.isSet());
        assertEquals("ACME", change.getValue());
        prop = company.getInstanceProperty("employeeOfTheMonth");
        change = findChange(changes, prop);
        assertNotNull(change);
        assertTrue(change.isSet());
        assertEquals("E0002", ((DataObject) change.getValue()).get("SN"));

        DataObject department = (DataObject) company.getList("departments").get(0);
        changes = c.getModifiedObjects().get(department);
        prop = department.getInstanceProperty("employees");
        change = findChange(changes, prop);
        assertNotNull(change);
        assertTrue(change.isSet());
        assertEquals(1, change.getArrayPos());
        assertEquals("Mary Smith", ((DataObject) change.getValue()).getString("name"));
        assertTrue(((DataObject) change.getValue()).getBoolean("manager"));
        change = change.next2;
        assertNotNull(change);
        assertFalse(change.isSet());
        assertEquals(1, change.getArrayPos());
        assertEquals("Al Smith", ((DataObject) change.getValue()).getString("name"));
    }

    public static final String serializedTypedGraph =
        "<sdo:datagraph xmlns:sdo=\"commonj.sdo\">\n" +
        "<changeSummary create=\"E0004\" delete=\"E0002\">\n" +
            "    <com:company sdo:ref=\"#/sdo:datagraph/com:company\" name=\"ACME\" employeeOfTheMonth=\"E0002\" xmlns:com=\"company2.xsd\"/>\n" +
            "    <departments sdo:ref=\"#/sdo:datagraph/com:company/departments[1]\" xmlns:com=\"company2.xsd\">\n" +
            "        <employees sdo:ref=\"#/sdo:datagraph/com:company/departments[1]/employees[1]\"/>\n" +
            "        <employees name=\"Mary Smith\" SN=\"E0002\" manager=\"true\"/>\n" +
            "        <employees sdo:ref=\"#/sdo:datagraph/com:company/departments[1]/employees[2]\"/>\n" +
            "    </departments>\n" +
            "</changeSummary>\n" +
        "<company:company xmlns:company=\"company2.xsd\"\n" +
            "    name=\"MegaCorp\" employeeOfTheMonth=\"E0004\">\n" +
            "  <departments name=\"AdvancedTechnologies\" location=\"NY\" number=\"123\">\n" +
            "    <employees name=\"John Jones\" SN=\"E0001\"/>\n" +
            "    <employees name=\"Jane Doe\" SN=\"E0003\"/>\n" +
            "    <employees name=\"Al Smith\" SN=\"E0004\" manager=\"true\"/>\n" +
            "  </departments>\n" +
            "</company:company>\n" +
            "</sdo:datagraph>";

    public static final String untypedXML =
        "<document att1=\"1\">\n" +
        "abcd\n" +
        "<a xsi:type=\"xsd:int\" xmlns:xsi=\"" + Names.URI_XSD_INSTANCE + "\" xmlns:xsd=\"" +
        Names.URI_XSD + "\">20</a>\n" +
        "<b att=\"foo\"><elem>bar</elem></b>\n" +
        "<c>100</c><a>40</a>\n" +
        "</document>";

    private void changeHelperUntyped(DataObject root)
    {
        Sequence s = root.getSequence();
        s.setValue(7, "n");
        root.unset(root.getInstanceProperty("att1"));
        Property att2 = ((DataFactoryImpl)dataFactory).getRootProperty("", "att2",
            BuiltInTypeSystem.STRING, false);
        
        root.set(att2, "2");
        Property aProp = s.getProperty(1);
        s.add(aProp, "newA");
        s.remove(1);
        Property cProp = s.getProperty(4);
        s.add(1, cProp, "newC");
        s.setValue(s.size()-1, "newnewA");
        DataObject o = (DataObject) s.getValue(3);
        o.set("att", "foo2");
    }

    public void testUntypedUndo()
    {
        XMLDocument doc = xmlHelper.load(untypedXML);
        DataObject root = doc.getRootObject();
        DataObject copy = copyHelper.copy(root);

        List<Property> savedPropertyList = new ArrayList<Property>();
        Sequence seq = root.getSequence();
        for (int i = 0; i < seq.size(); i++)
            savedPropertyList.add(seq.getProperty(i));

        DataGraphHelper.wrapWithDataGraph(root, doc.getRootElementURI(), doc.getRootElementName());
        ChangeSummaryImpl c = (ChangeSummaryImpl) root.getChangeSummary();
        c.beginLogging();
        changeHelperUntyped(root);
        List changedDOs = c.getChangedDataObjects();
        assertEquals(2, changedDOs.size());
        List changesForRoot = c.getOldValues(root);
        assertEquals(5, changesForRoot.size());

        verifyIteratorSequence(root);
        Sequence oldSeq = c.getOldSequence(root);
        checkSameSequence(oldSeq, savedPropertyList); 
        // The values of elements inside the sequence is still the new one
        assertEquals("foo2", ((DataObject) oldSeq.getValue(3)).getString("att"));

        c.undoChanges();
        checkSameSequence(seq, savedPropertyList);
        // In addition to that, we check that the old value of <b>'s "att"
        // attribute has been reverted
        assertEquals("foo", root.getDataObject("b[1]").getString("att"));
        assertTrue(equalityHelper.equal(copy, root));
    }

    private void verifyIteratorSequence(DataObject object)
    {
        DataObjectIterator doIterator = new DataObjectIteratorImpl((DataObjectXML) object);
        DataObjectIterator.Change entry = doIterator.next();
        assertEquals("att1", entry.getProperty().getName());
        assertEquals("1", entry.getValue());
        assertEquals(DataObjectIterator.Change.OLD, entry.getChangeType());
        entry = doIterator.next();
        assertEquals(null, entry.getProperty());
        assertEquals("\nabcd\n", entry.getValue());
        assertEquals(DataObjectIterator.Change.SAME, entry.getChangeType());
        entry = doIterator.next();
        assertEquals("a", entry.getProperty().getName());
        assertEquals(20, entry.getValue());
        assertEquals(DataObjectIterator.Change.OLD, entry.getChangeType());
        entry = doIterator.next();
        assertEquals("c", entry.getProperty().getName());
        assertEquals("newC", entry.getValue());
        assertEquals(DataObjectIterator.Change.NEW, entry.getChangeType());
        entry = doIterator.next();
        assertEquals(null, entry.getProperty());
        assertEquals("\n", entry.getValue());
        assertEquals(DataObjectIterator.Change.SAME, entry.getChangeType());
        entry = doIterator.next();
        DataObjectXML b = (DataObjectXML) entry.getValue();
        assertEquals("b", entry.getProperty().getName());
        assertEquals("bar", b.getList("elem").get(0));
        assertEquals(DataObjectIterator.Change.SAME, entry.getChangeType());
        doIterator.next();
        entry = doIterator.next();
        assertEquals("c", entry.getProperty().getName());
        assertEquals(DataObjectIterator.Change.SAME, entry.getChangeType());
        entry = doIterator.next();
        assertEquals("a", entry.getProperty().getName());
        assertEquals(DataObjectIterator.Change.SAME, entry.getChangeType());
        entry = doIterator.next();
        assertEquals(null, entry.getProperty());
        assertEquals("\n", entry.getValue());
        assertEquals(DataObjectIterator.Change.OLD, entry.getChangeType());
        entry = doIterator.next();
        assertEquals(null, entry.getProperty());
        assertEquals("n", entry.getValue());
        assertEquals(DataObjectIterator.Change.NEW, entry.getChangeType());
        entry = doIterator.next();
        assertEquals("att2", entry.getProperty().getName());
        assertEquals("2", entry.getValue());
        assertEquals(DataObjectIterator.Change.NEW, entry.getChangeType());
        entry = doIterator.next();
        assertEquals("a", entry.getProperty().getName());
        assertEquals("newnewA", entry.getValue());
        assertEquals(DataObjectIterator.Change.NEW, entry.getChangeType());
        assertFalse(doIterator.hasNext());
        doIterator = new DataObjectIteratorImpl(b);
        entry = doIterator.next();
        assertEquals("att", entry.getProperty().getName());
        assertEquals("foo", entry.getValue());
        assertEquals(DataObjectIterator.Change.OLD, entry.getChangeType());
        entry = doIterator.next();
        assertEquals("att", entry.getProperty().getName());
        assertEquals("foo2", entry.getValue());
        assertEquals(DataObjectIterator.Change.NEW, entry.getChangeType());
        doIterator.next();
        assertFalse(doIterator.hasNext());
    }

    private static final String serializedUntypedGraph =
        "<sdo:datagraph xmlns:sdo=\"commonj.sdo\">\n" +
        "<changeSummary>\n" +
            "    <document sdo:ref=\"#/sdo:datagraph/document\" att1=\"1\">\n" +
            "abcd\n" +
            "<a xsi:type=\"xs:int\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">20</a>\n" +
            "<b sdo:ref=\"#/sdo:datagraph/document/b[1]\"/>\n" +
            "<c sdo:ref=\"#/sdo:datagraph/document/c[2]\"/>" +
            "<a sdo:ref=\"#/sdo:datagraph/document/a[1]\"/>\n" +
            "</document>\n" +
            "    <b sdo:ref=\"#/sdo:datagraph/document/b[1]\" att=\"foo\">" +
            "<elem sdo:ref=\"#/sdo:datagraph/document/b[1]/elem[1]\"/></b>\n" +
            "</changeSummary>\n" +
        "<document att2=\"2\">\n" +
        "abcd\n" +
        "<c>newC</c>\n" +
        "<b att=\"foo2\"><elem>bar</elem></b>\n" +
        "<c>100</c><a>40</a>n<a>newnewA</a>" +
        "</document>\n" +
        "</sdo:datagraph>";

    public void testUntypedChangeSummary() throws IOException
    {
        XMLDocument doc = xmlHelper.load(untypedXML);
        DataObject root = doc.getRootObject();
        DataGraphHelper.wrapWithDataGraph(root, doc.getRootElementURI(), doc.getRootElementName());
        ChangeSummaryImpl c = (ChangeSummaryImpl) root.getChangeSummary();
        c.beginLogging();
        changeHelperUntyped(root);
//        System.out.println(c.toString());

        String s = xmlHelper.save(root.getRootObject(), Names.URI_SDO, Names.SDO_DATAGRAPH);
        // We check the value of s with substr because it is easier
        // and more robust to random allowable variations in the output content
        int i1, i2, i3;
        i1 = s.indexOf(" create=\"");
        assertTrue(i1 < 0);
        i1 = s.indexOf(" delete=\"");
        assertTrue(i1 < 0);
        i2 = s.indexOf("<document sdo:ref");
        assertTrue(s.indexOf(">20</a>") > i2);
        i2 = s.indexOf("</document", i1);
        assertTrue(s.indexOf("<a>newnewA</a>", i1) > i2);
        i1 = s.indexOf("<b sdo:ref=\"#/sdo:datagraph/document/b[1]\"/>" + newline);
        i2 = s.indexOf("<c sdo:ref=\"#/sdo:datagraph/document/c[2]\"/>");
        i3 = s.indexOf("<a sdo:ref=\"#/sdo:datagraph/document/a[1]\"/>" + newline);
        assertTrue(0 < i1 && i1 < i2 && i2 < i3);

        root = xmlHelper.load(serializedUntypedGraph).getRootObject().getDataObject("document[1]");
        c = (ChangeSummaryImpl) root.getChangeSummary();
        assertEquals(0, c.getInsertedObjects().size());
        assertEquals(0, c.getDeletedObjects().size());
        ChangeSummaryImpl.Change change = ChangeSummaryImpl.getFirstSequenceChange(c.getModifiedObjects().get(root));
        assertNotNull(change);
        assertEquals("att1", change.getProperty().getName());
        assertEquals(0, change.getArrayPos());
        assertEquals("1", change.getValue());
        change = change.next2;
        assertNotNull(change);
        assertEquals("att2", change.getProperty().getName());
        assertEquals(0, change.getArrayPos());
        assertEquals("2", change.getValue());
        change = change.next2;
        assertNotNull(change);
        assertEquals("a", change.getProperty().getName());
        assertEquals(2, change.getArrayPos());
        assertEquals(20, change.getValue());
        change = change.next2;
        // Here we skip over a text property that has not really changed but is included because
        // of the way the diff algorithm works
        change = change.next2;
        assertNotNull(change);
        assertEquals("c", change.getProperty().getName());
        assertEquals(0, change.getArrayPos());
        assertEquals("newC", change.getValue());
        change = change.next2;
        // Here we skip over a text property that has not really changed but is included because
        // of the way the diff algorithm works
        change = change.next2;
        assertNotNull(change);
        assertEquals( null /*ie text*/, change.getProperty());
        assertEquals(5, change.getArrayPos());
        assertEquals("\n", change.getValue());
        change = change.next2.next2; // skip a text change
        assertNotNull(change);
        assertEquals("a", change.getProperty().getName());
        assertEquals(1, change.getArrayPos());
        assertEquals("newnewA", change.getValue());

        change = ChangeSummaryImpl.getFirstSequenceChange(c.getModifiedObjects().get(root.get("b[1]")));
        assertNotNull(change);
        assertEquals("att", change.getProperty().getName());
        assertEquals(0, change.getArrayPos());
        assertEquals("foo", change.getValue());
    }

    private void checkSameSequence(Sequence s, List<Property> savedPropertyList)
    {
        assertEquals(8, s.size());
        for (int i = 0; i < s.size(); i++)
            assertSame(savedPropertyList.get(i), s.getProperty(i));
        assertEquals("\nabcd\n", s.getValue(0));
        assertEquals(20, s.getValue(1));
        assertEquals("\n", s.getValue(7));
        assertEquals("40", s.getValue(6));
    }

    private ChangeSummaryImpl.Change findChange(ChangeSummaryImpl.Change[] changes, Property prop)
    {
        ChangeSummaryImpl.Change result;
        for (result = changes[prop.hashCode() % changes.length];result != null;result = result.next)
            if (result.getProperty() == prop)
                break;
        return result;
    }

    public void testCopyHelper() throws IOException
    {
        File f = getResourceFile("checkin", "company2.xml");
        InputStream in = new FileInputStream(f);
        XMLDocument doc = xmlHelper.load(in);
        DataObject company = doc.getRootObject();
        in.close();

        DataObject copy = copyHelper.copy(company);
        assertTrue(equalityHelper.equal(company, copy));
        company.set("departments", new ArrayList());
        company.set("name", "ABC");
        company.unset("employeeOfTheMonth");
        assertEquals("ACME", copy.get("name"));
        assertEquals("E0002", ((DataObject) copy.get("employeeOfTheMonth")).get("SN"));
        assertNotSame(company.get("employeeOfTheMonth"), copy.get("employeeOfTheMonth"));
        List employees = ((DataObject) copy.getList("departments").get(0)).getList("employees");
        assertEquals(3, employees.size());
        assertEquals("Mary Smith", ((DataObject) employees.get(1)).get("name"));
        assertEquals("Jane Doe", ((DataObject) employees.get(2)).get("name"));

        assertFalse(equalityHelper.equal(company, copy));
        doc = xmlHelper.load(untypedXML);
        DataObject root = doc.getRootObject();

        List<Property> savedPropertyList = new ArrayList<Property>();
        Sequence seq = root.getSequence();
        for (int i = 0; i < seq.size(); i++)
            savedPropertyList.add(seq.getProperty(i));

        copy = copyHelper.copy(root);
        assertTrue(equalityHelper.equal(root, copy));
        Sequence s = root.getSequence();
        int size = s.size();
        for (int i = 0; i < size; i++)
            s.remove(0);
        checkSameSequence(copy.getSequence(), savedPropertyList);
        assertFalse(equalityHelper.equal(company, copy));
    }

    public void testEqualityHelper() throws IOException
    {
        File f = getResourceFile("checkin", "company2.xml");
        InputStream in = new FileInputStream(f);
        XMLDocument doc = xmlHelper.load(in);
        DataObject company = doc.getRootObject();
        in.close();

        // Perform a marshal-unmarshal cycle and see if they are still equal
        DataObject copy;
        copy = copyHelper.copyShallow(company);
        assertTrue(equalityHelper.equalShallow(company, copy));
        copy = copyHelper.copy(company);
        String marshalled = xmlHelper.save(company, doc.getRootElementURI(), doc.getRootElementName());
        company = xmlHelper.load(marshalled).getRootObject();
        assertTrue(equalityHelper.equal(copy, company));

        doc = xmlHelper.load(untypedXML);
        DataObject root = doc.getRootObject();
        copy = copyHelper.copyShallow(root);
        assertTrue(equalityHelper.equalShallow(root, copy));
        copy = copyHelper.copy(root);
        marshalled = xmlHelper.save(root, doc.getRootElementURI(), doc.getRootElementName());
        root = xmlHelper.load(marshalled).getRootObject();
        assertTrue(equalityHelper.equal(copy, root));
    }

    private static final String cascadingDeletesXMLTyped =
        "<document xmlns=\"test/simple3\">\n" +
        "<a><b name=\"b\"></b><c name=\"c\"></c></a>\n" +
        "</document>";

    private static final String cascadingDeletesXMLUntyped =
        "<document>\n" +
        "<a><b name=\"b\">20</b><c name=\"c\">30</c></a>\n" +
        "</document>";

    private static final String cascadingDeletesChangeSummaryTyped =
        "<sdo:datagraph xmlns:sdo=\"commonj.sdo\">\n" +
        "    <changeSummary delete=\"#/sdo:datagraph/changeSummary/sim:document/sim:a" +
            " #/sdo:datagraph/changeSummary/sim:document/sim:a/sim:b" +
            " #/sdo:datagraph/changeSummary/sim:document/sim:a/sim:c\"" +
            " xmlns:sim=\"test/simple3\">\n" +
        "        <sim:document sdo:ref=\"#/sdo:datagraph/sim:document\" sdo:unset=\"node\">\n" +
        "            <sim:a>\n" +
        "                <sim:b name=\"b\"/>\n" +
        "                <sim:c name=\"c\"/>\n" +
        "            </sim:a>\n" +
        "        </sim:document>\n" +
        "    </changeSummary>\n" +
        "    <sim:document xmlns:sim=\"test/simple3\" node=\"a\"/>\n" +
        "</sdo:datagraph>\n";

    private static final String cascadingDeletesChangeSummaryUntyped =
        "<sdo:datagraph xmlns:sdo=\"commonj.sdo\">\n" +
        "    <changeSummary delete=\"#/sdo:datagraph/changeSummary/document[1]/a[1]\">\n" +
        "        <document sdo:ref=\"#/sdo:datagraph/document[1]\">\n" +
        "<a><b name=\"b\">20</b><c name=\"c\">30</c></a>\n" +
        "</document>\n" +
        "    </changeSummary>\n" +
        "    <document node=\"a\">\n" +
        "    \n" +
        "</document></sdo:datagraph>\n";

    public void testCascadingDeletes()
    {
        XMLDocument doc = xmlHelper.load(cascadingDeletesXMLTyped);
        DataObject root = doc.getRootObject();
        DataGraphHelper.wrapWithDataGraph(root);
        ChangeSummaryImpl cs = (ChangeSummaryImpl) root.getChangeSummary();
        cs.beginLogging();
        DataObject a = root.getDataObject("a");
        a.getDataObject("b").delete();
        a.getDataObject("c").delete();
        assertEquals(2, cs.getDeletedObjects().size());
        a.delete();
        assertEquals(1, cs.getDeletedObjects().size());
        root.set("node", "a");
        String s = xmlHelper.save(root.getRootObject(), Names.URI_SDO, Names.SDO_DATAGRAPH);
        assertTrue(s.indexOf("delete=\"#/sdo:datagraph/changeSummary/sim:document[1]/sim:a\"")
            > 0);
        int i1 = s.indexOf("<sim:b name=\"b\"/>");
        int i2 = s.indexOf("<sim:c name=\"c\"/>");
        assertTrue(0 < i1 && i1 < i2);
        assertTrue(s.indexOf(" sdo:unset=\"node\"") > 0);

        doc = xmlHelper.load(cascadingDeletesChangeSummaryTyped);
        root = doc.getRootObject();
        cs = (ChangeSummaryImpl) root.getChangeSummary();
        assertEquals(3, cs.getDeletedObjects().size());
        assertEquals("a", root.getDataObject("document").get("node"));
        root = root.getDataObject("document");
        ChangeSummaryImpl.Setting c = cs.getOldValue(root, root.getInstanceProperty("node"));
        assertTrue(c != null && !c.isSet());

        doc = xmlHelper.load(cascadingDeletesXMLUntyped);
        root = doc.getRootObject();
        DataGraphHelper.wrapWithDataGraph(root);
        cs = (ChangeSummaryImpl) root.getChangeSummary();
        cs.beginLogging();
        a = root.getDataObject("a[1]");
        a.getDataObject("b[1]").delete();
        a.getDataObject("c[1]").delete();
        assertEquals(2, cs.getDeletedObjects().size());
        a.delete();
        assertEquals(1, cs.getDeletedObjects().size());
        s = xmlHelper.save(root.getRootObject(), Names.URI_SDO, Names.SDO_DATAGRAPH);
        assertTrue(s.indexOf("delete=\"#/sdo:datagraph/changeSummary/document[1]/a[1]\"")
            > 0);
        i1 = s.indexOf("<b name=\"b\">20</b>");
        i2 = s.indexOf("<c name=\"c\">30</c>");
        assertTrue(0 < i1 && i1 < i2);

        doc = xmlHelper.load(cascadingDeletesChangeSummaryUntyped);
        root = doc.getRootObject();
        cs = (ChangeSummaryImpl) root.getChangeSummary();
        assertEquals(1, cs.getDeletedObjects().size());
        assertEquals("a", root.getDataObject("document[1]").get("node"));
        root = root.getDataObject("document[1]");
        c = cs.getOldValue(root, root.getInstanceProperty("node"));
        assertTrue(c != null && !c.isSet());
    }

    public void testDataObjectBuilder()
    {
        DataObjectBuilder l = new DataObjectBuilderImpl(context);
        DataObjectBuilder.Change same = DataObjectBuilder.Change.SAME;
        DataObjectBuilder.Change old = DataObjectBuilder.Change.OLD;
        DataObjectBuilder.Change ne = DataObjectBuilder.Change.NEW;
        DataObjectBuilder.Kind attr = DataObjectBuilder.Kind.ATTRIBUTE;
        // Loading DataGraph:
//        <sdo:datagraph xmlns:sdo="commonj.sdo">
//            <changeSummary create="E0004" delete="E0002">
//                <com:company sdo:ref="#/sdo:datagraph/com:company" name="ACME" xmlns:com="company2.xsd"/>
//                <departments sdo:ref="#/sdo:datagraph/com:company/departments[1]">
//                    <employees sdo:ref="#/sdo:datagraph/com:company/departments[1]/employees[1]"/>
//                    <employees name="Mary Smith" SN="E0002" manager="true"/>
//                    <employees sdo:ref="#/sdo:datagraph/com:company/departments[1]/employees[2]"/>
//                </departments>
//            </changeSummary>
//            <company:company xmlns:company="company2.xsd"
//                name="MegaCorp">
//              <departments name="AdvancedTechnologies" location="NY" number="123">
//                <employees name="John Jones" SN="E0001"/>
//                <employees name="Jane Doe" SN="E0003"/>
//                <employees name="Al Smith" SN="E0004" manager="true"/>
//              </departments>
//            </company:company>
//       </sdo:datagraph>
        l.startElement(Names.URI_SDO, Names.SDO_DATAGRAPH, "sdo", same);
        l.startElement("company2.xsd", "company", "company", same);
        l.simpleContent(null, "name", null, "MegaCorp", attr, ne);
        l.simpleContent(null, "name", null, "ACME", attr, old);
        l.startElement(null, "departments", null, same);
        l.simpleContent(null, "name", null, "AdvancedTechnologies", attr, same);
        l.simpleContent(null, "location", null, "NY", attr, same);
        l.simpleContent(null, "number", null, 123, attr, same);
        l.startElement(null, "employees", null, same);
        l.simpleContent(null, "name", null, "John Jones", attr, same);
        l.simpleContent(null, "SN", null, "E0001", attr, same);
        l.endElement();
        l.startElement(null, "employees", null, old);
        l.simpleContent(null, "name", null, "Mary Smith", attr, same);
        l.simpleContent(null, "SN", null, "E0002", attr, same);
        l.simpleContent(null, "manager", null, true, attr, same);
        l.endElement();
        l.startElement(null, "employees", null, same);
        l.simpleContent(null, "name", null, "Jane Doe", attr, same);
        l.simpleContent(null, "SN", null, "E0003", attr, same);
        l.endElement();
        l.startElement(null, "employees", null, ne);
        l.simpleContent(null, "name", null, "Al Smith", attr, same);
        l.simpleContent(null, "SN", null, "E0004", attr, same);
        l.simpleContent(null, "manager", null, true, attr, same);
        l.endElement();
        l.endElement();
        l.endElement();
        l.endElement();
        DataObjectXML root = l.retrieveRootDataObject();
        assertEquals(123, root.get("company/departments[1]/number"));
        assertEquals("Jane Doe", root.get("company/departments[1]/employees[2]/name"));
        assertEquals("Al Smith", root.get("company/departments[1]/employees[3]/name"));
        assertEquals(true, root.get("company/departments[1]/employees[3]/manager"));
        assertEquals(3, root.getList("company/departments[1]/employees").size());
        assertEquals("MegaCorp", root.get("company/name"));
        Property companyProperty = root.getInstanceProperty("company");
        assertTrue(root.getDataObject(companyProperty).getClass().getName().indexOf("Company") >= 0);
        ChangeSummaryImpl c = (ChangeSummaryImpl) root.getChangeSummary();
        assertEquals(1, c.getDeletedObjects().size());
        assertEquals(1, c.getInsertedObjects().size());
        assertEquals(2, c.getModifiedObjects().size());
        c.undoChanges();
        assertEquals("ACME", root.get("company/name"));
        assertEquals("Mary Smith", root.get("company/departments[1]/employees[2]/name"));
        assertEquals(true, root.get("company/departments[1]/employees[2]/manager"));
        assertEquals("Jane Doe", root.get("company/departments[1]/employees[3]/name"));
        assertEquals(false, root.getBoolean("company/departments[1]/employees[3]/manager"));
    }

    public void testDOMChangeSummaryTyped() throws Exception
    {
        DataObject dataGraph = MarshalTest.loadViaDOM(serializedTypedGraph);
        checkTypedDataGraphModified(dataGraph);

        String s = MarshalTest.saveViaDOM(dataGraph, Names.URI_SDO, Names.SDO_DATAGRAPH);
        Diff diff = new Diff(serializedTypedGraph, s);
//        assertTrue(diff.toString(), diff.similar());

        dataGraph.getChangeSummary().undoChanges();
        checkTypedDataGraphOriginal(dataGraph);
    }

    public void testDOMChangeSummaryUntyped() throws Exception
    {
        DataObject dataGraph = MarshalTest.loadViaDOM(serializedUntypedGraph);
        checkUntypedDataGraphModified(dataGraph);

        String st = MarshalTest.saveViaDOM(dataGraph, Names.URI_SDO, Names.SDO_DATAGRAPH);
        Diff diff = new Diff(serializedUntypedGraph, st);
//        assertTrue(diff.toString(), diff.identical());

        DataObject dataGraph2 = MarshalTest.saveAndLoadDOM(dataGraph, Names.URI_SDO, Names.SDO_DATAGRAPH);
        assertTrue(equalityHelper.equal(dataGraph, dataGraph2));

        dataGraph.getChangeSummary().undoChanges();
        checkUntypedDataGraphOriginal(dataGraph);

        dataGraph2.getChangeSummary().undoChanges();
        assertTrue(equalityHelper.equal(dataGraph, dataGraph2));
    }

    public void testStAXChangeSummaryTyped() throws Exception
    {
        DataObject dataGraph = MarshalTest.loadViaXMLStreamReader(serializedTypedGraph);
        checkTypedDataGraphModified(dataGraph);

        DataObject dataGraph2 = MarshalTest.saveAndLoadViaXMLStreamWriter(dataGraph, Names.URI_SDO, Names.SDO_DATAGRAPH);
        assertTrue(equalityHelper.equal(dataGraph, dataGraph2));

        dataGraph2 = MarshalTest.saveAndLoadViaXMLEventWriter(dataGraph, Names.URI_SDO, Names.SDO_DATAGRAPH);
        assertTrue(equalityHelper.equal(dataGraph, dataGraph2));

        dataGraph.getChangeSummary().undoChanges();
        checkTypedDataGraphOriginal(dataGraph);

        dataGraph = xmlHelper.load(serializedTypedGraph).getRootObject();
        dataGraph2 = MarshalTest.saveAndLoadViaXMLStreamReader(dataGraph, Names.URI_SDO, Names.SDO_DATAGRAPH);
        assertTrue(equalityHelper.equal(dataGraph, dataGraph2));
        dataGraph.getChangeSummary().undoChanges();
        dataGraph2.getChangeSummary().undoChanges();
        assertTrue(equalityHelper.equal(dataGraph, dataGraph2));
    }

    public void testStAXChangeSummaryUntyped() throws Exception
    {
        DataObject dataGraph = MarshalTest.loadViaXMLStreamReader(serializedUntypedGraph);
        checkUntypedDataGraphModified(dataGraph);

        DataObject dataGraph2 = MarshalTest.saveAndLoadViaXMLStreamWriter(dataGraph, Names.URI_SDO, Names.SDO_DATAGRAPH);
        assertTrue(equalityHelper.equal(dataGraph, dataGraph2));

        dataGraph2 = MarshalTest.saveAndLoadViaXMLEventWriter(dataGraph, Names.URI_SDO, Names.SDO_DATAGRAPH);
        assertTrue(equalityHelper.equal(dataGraph, dataGraph2));

        dataGraph.getChangeSummary().undoChanges();
        checkUntypedDataGraphOriginal(dataGraph);

        dataGraph = xmlHelper.load(serializedUntypedGraph).getRootObject();
        dataGraph2 = MarshalTest.saveAndLoadViaXMLStreamReader(dataGraph, Names.URI_SDO, Names.SDO_DATAGRAPH);
        assertTrue(equalityHelper.equal(dataGraph, dataGraph2));
        dataGraph.getChangeSummary().undoChanges();
        dataGraph2.getChangeSummary().undoChanges();
        assertTrue(equalityHelper.equal(dataGraph, dataGraph2));
    }

    /**
     * This tests everything related to many-valued simple-type properties: marshal, unmarshal,
     * change summary, iterators, undo.
     */
    public void testSimpleList() throws Exception
    {
        final String schema =
            "<xs:schema targetNamespace=\"checkin.ChangeSummaryTest.testSimpleList\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">\n" +
                "<xs:element name=\"a\">\n" +
                "<xs:complexType>\n" +
                "<xs:sequence>\n" +
                "<xs:element name=\"item\" type=\"xs:string\" maxOccurs=\"unbounded\"/>\n" +
                "</xs:sequence>\n" +
                "</xs:complexType>\n" +
                "</xs:element>\n" +
                "</xs:schema>\n";
        final String xml =
            "<chec:a xmlns:chec=\"checkin.ChangeSummaryTest.testSimpleList\"><item>aaa</item><item>bbb</item></chec:a>";

        // Schema compilation
        context.getXSDHelper().define(schema);

        // Unmarshalling
        XMLDocument doc = xmlHelper.load(xml);
        DataObject a = doc.getRootObject();
        assertEquals("aaa", a.get("item[1]"));
        assertEquals("bbb", a.get("item[2]"));

        // Marshalling
        String s = xmlHelper.save(a, doc.getRootElementURI(), doc.getRootElementName());
        assertEquals(xml, s);

        // Change summary marshal
        DataGraphHelper.wrapWithDataGraph(a, doc.getRootElementURI(), doc.getRootElementName());
        ChangeSummaryImpl cs = (ChangeSummaryImpl) a.getChangeSummary();
        cs.beginLogging();
        a.getList("item").remove(0);
        a.getList("item").add("ccc");
        assertEquals("bbb", a.get("item[1]"));
        assertEquals("ccc", a.get("item[2]"));
        s = xmlHelper.save(a.getRootObject(), Names.URI_SDO, Names.SDO_DATAGRAPH);

        // Change summary unmarshal
        doc = xmlHelper.load(s);
        a = doc.getRootObject().getDataObject("a");
        assertEquals("bbb", a.get("item[1]"));
        assertEquals("ccc", a.get("item[2]"));

        // Change summary undo
        cs = (ChangeSummaryImpl) a.getChangeSummary();
        cs.undoChanges();
        assertEquals("aaa", a.get("item[1]"));
        assertEquals("bbb", a.get("item[2]"));

        // DataObject builder
        DataObjectBuilder l = new DataObjectBuilderImpl(context);
        DataObjectBuilder.Change same = DataObjectBuilder.Change.SAME;
        DataObjectBuilder.Change old = DataObjectBuilder.Change.OLD;
        DataObjectBuilder.Change ne = DataObjectBuilder.Change.NEW;
        l.startElement(Names.URI_SDO, Names.SDO_DATAGRAPH, "sdo", same);
        l.startElement("checkin.ChangeSummaryTest.testSimpleList", "a", "chec", same);
        l.simpleContent(null, "item", null, "aaa", DataObjectBuilder.Kind.ELEMENT, old);
        l.simpleContent(null, "item", null, "bbb", DataObjectBuilder.Kind.ELEMENT, same);
        l.simpleContent(null, "item", null, "ccc", DataObjectBuilder.Kind.ELEMENT, ne);
        l.endElement();
        l.endElement();
        a = l.retrieveRootDataObject().getDataObject("a");
        assertEquals("bbb", a.get("item[1]"));
        assertEquals("ccc", a.get("item[2]"));

        // DataObject iterator
        DataObjectIterator dit = new DataObjectIteratorImpl((DataObjectXML) a);
        DataObjectIterator.Change entry;
        entry = dit.next();
        assertEquals("aaa", entry.getValue());
        assertEquals(DataObjectIterator.Change.OLD, entry.getChangeType());
        entry = dit.next();
        assertEquals("bbb", entry.getValue());
        assertEquals(DataObjectIterator.Change.SAME, entry.getChangeType());
        entry = dit.next();
        assertEquals("ccc", entry.getValue());
        assertEquals(DataObjectIterator.Change.NEW, entry.getChangeType());
        assertFalse(dit.hasNext());
    }

    private void checkTypedDataGraphModified(DataObject dataGraph)
    {
        DataObject company = dataGraph.getDataObject("company");
        assertEquals("MegaCorp", company.get("name"));
        DataObject department = company.getDataObject("departments[1]");
        assertSame(company.get("employeeOfTheMonth"), department.get("employees[3]"));
        assertEquals("NY", department.get("location"));
        assertEquals(123, department.get("number"));
        DataObject employee = department.getDataObject("employees[1]");
        assertEquals("John Jones", employee.get("name"));
        assertEquals("E0001", employee.get("SN"));
        assertFalse(employee.getBoolean("manager"));
        employee = department.getDataObject("employees[2]");
        assertEquals("Jane Doe", employee.get("name"));
        assertEquals("E0003", employee.get("SN"));
        assertFalse(employee.getBoolean("manager"));
        employee = department.getDataObject("employees[3]");
        assertEquals("Al Smith", employee.get("name"));
        assertEquals("E0004", employee.get("SN"));
        assertTrue(employee.getBoolean("manager"));
    }

    private void checkTypedDataGraphOriginal(DataObject dataGraph)
    {
        DataObject company = dataGraph.getDataObject("company");
        assertEquals("ACME", company.get("name"));
        DataObject department = company.getDataObject("departments[1]");
        assertSame(company.get("employeeOfTheMonth"), department.get("employees[2]"));
        assertEquals("NY", department.get("location"));
        assertEquals(123, department.get("number"));
        DataObject employee = department.getDataObject("employees[1]");
        assertEquals("John Jones", employee.get("name"));
        assertEquals("E0001", employee.get("SN"));
        assertFalse(employee.getBoolean("manager"));
        employee = department.getDataObject("employees[2]");
        assertEquals("Mary Smith", employee.get("name"));
        assertEquals("E0002", employee.get("SN"));
        assertTrue(employee.getBoolean("manager"));
        employee = department.getDataObject("employees[3]");
        assertEquals("Jane Doe", employee.get("name"));
        assertEquals("E0003", employee.get("SN"));
        assertFalse(employee.getBoolean("manager"));
    }

    private void checkUntypedDataGraphModified(DataObject dataGraph)
    {
        DataObject root = dataGraph.getDataObject("document[1]");
        Sequence s = root.getSequence();
        assertEquals("att2", root.getInstanceProperty("att2").getName());
        assertEquals("2", root.get("att2"));
        assertEquals(null, s.getProperty(0));
        assertEquals("\nabcd\n", s.getValue(0));
        assertEquals("c", s.getProperty(1).getName());
        assertEquals("newC", s.getValue(1));
        assertEquals("b", s.getProperty(3).getName());
        assertEquals("foo2", ((DataObject) s.getValue(3)).get("att"));
        assertEquals(null, s.getProperty(4));
        assertEquals("\n", s.getValue(4));
        assertEquals("c", s.getProperty(5).getName());
        assertEquals("100", s.getValue(5));
        assertEquals("a", s.getProperty(6).getName());
        assertEquals("40", s.getValue(6));
        assertEquals(null, s.getProperty(7));
        assertEquals("n", s.getValue(7));
        assertEquals("a", s.getProperty(8).getName());
        assertEquals("newnewA", s.getValue(8));
        assertEquals(9, s.size());
    }

    private void checkUntypedDataGraphOriginal(DataObject dataGraph)
    {
        DataObject root = dataGraph.getDataObject("document[1]");
        Sequence s = root.getSequence();
        assertEquals("att1", root.getInstanceProperty("att1").getName());
        assertEquals("1", root.get("att1"));
        assertEquals("a", s.getProperty(1).getName());
        assertEquals(20, s.getValue(1));
        assertEquals(null, s.getProperty(0));
        assertEquals("\nabcd\n", s.getValue(0));
        assertEquals(null, s.getProperty(7));
        assertEquals("\n", s.getValue(7));
        assertEquals(8, s.size());
    }
}
