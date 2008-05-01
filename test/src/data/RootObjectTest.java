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
package data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
//import java.util.List;

import javax.sdo.DataGraph;
import javax.sdo.DataObject;
import javax.sdo.ChangeSummary;
import javax.sdo.Type;
import javax.sdo.Property;
import javax.sdo.helper.TypeHelper;
import javax.sdo.helper.DataFactory;
import javax.sdo.helper.XMLDocument;
import javax.sdo.helper.XMLHelper;
import javax.sdo.helper.XSDHelper;

import davos.sdo.Options;

import junit.framework.Test;
import junit.framework.TestSuite;
import common.BaseTest;

/**
 * Tests for the interrelation among DataGraph, DataObject, and ChangeSummary.
 * @author Wing Yew Poon
*/
public class RootObjectTest extends BaseTest
{
    public RootObjectTest(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        
        TestSuite suite = new TestSuite();
        suite.addTest(new RootObjectTest("testCreateDataObject"));
        suite.addTest(new RootObjectTest("testCreateDataObjectWithChangeSummary"));
        suite.addTest(new RootObjectTest("testCreateDataGraphType"));
        //suite.addTest(new RootObjectTest("testEffectOfSet"));
        //suite.addTest(new RootObjectTest("testEffectOfDetach"));
        //suite.addTest(new RootObjectTest("testEffectOfDelete"));
        //suite.addTest(new RootObjectTest("testDeleteRootObject"));
        
        suite.addTest(new RootObjectTest("testUnmarshal1a"));
        suite.addTest(new RootObjectTest("testUnmarshal1b"));
        suite.addTest(new RootObjectTest("testUnmarshal1c"));
        suite.addTest(new RootObjectTest("testUnmarshal2a"));
        suite.addTest(new RootObjectTest("testUnmarshal2b"));
        suite.addTest(new RootObjectTest("testUnmarshal2c"));
        suite.addTest(new RootObjectTest("testUnmarshal2d"));
        
        // or
        //TestSuite suite = new TestSuite(RootObjectTest.class);
        return suite;
    }

    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(suite());
    }

    public static File dir;
    static
    {
        dir = new File(OUTPUTROOT + S + "data");
        dir.mkdirs();
    }

    private static DataFactory factory = context.getDataFactory();
    private static TypeHelper typeHelper = context.getTypeHelper();
    private static XMLHelper xmlHelper = context.getXMLHelper();
    private static XSDHelper xsdHelper = context.getXSDHelper();

    private static final Type DATAGRAPHTYPE =
        typeHelper.getType("commonj.sdo", "DataGraphType");

    // Create a new DataObject. Test:
    // DataObject.getRootObject()
    //           .getDataGraph()
    // DataGraph.getRootObject()
    //          .getChangeSummary()
    // ChangeSummary.getDataGraph()
    //              .getRootObject()

    /* create a new DataObject (with no ChangeSummary property) */
    public void testCreateDataObject()
    {
        String uri = "http://sdo/test/po1";
        // create a new DataObject
        DataObject po = factory.create(uri, "PurchaseOrder");
        assertNotNull(po);
        // its root object is itself
        assertTrue(po.getRootObject() == po);
        // by default, does not have a DataGraph
        assertNull(po.getDataGraph());
        // does not have a ChangeSummary
        assertNull(po.getChangeSummary());
        
        XMLDocument doc = xmlHelper.createDocument(po, uri, "purchaseOrder");
        assertTrue(doc.getRootObject() == po);
        // multiple XMLDocument instances can point to the same root object
        XMLDocument doc2 = xmlHelper.createDocument(po, uri, "purchaseOrder");
        assertTrue(doc2.getRootObject() == po);
    }

    /* create a new DataObject with a ChangeSummary property */
    public void testCreateDataObjectWithChangeSummary()
    {
        String uri = "http://sdo/test/po2";
        // create a new DataObject
        DataObject po = factory.create(uri, "PurchaseOrder");
        assertNotNull(po);
        // its root object is itself
        assertTrue(po.getRootObject() == po);
        // by default, does not have a DataGraph
        assertNull(po.getDataGraph());
        // has a ChangeSummary
        assertNotNull(po.getChangeSummary());
        // which, by default, has logging off
        assertFalse(po.getChangeSummary().isLogging());
        // ChangeSummary has no DataGraph
        assertNull(po.getChangeSummary().getDataGraph());
        // ChangeSummary's root object is the root object
        assertTrue(po.getChangeSummary().getRootObject() == po);
    }

    /* create a new DataObject of DataGraphType */
    public void testCreateDataGraphType()
    {
        // create a new DataObject of DataGraphType
        DataObject root = factory.create(DATAGRAPHTYPE);
        // has a DataGraph attached
        DataGraph dg = root.getDataGraph();
        assertNotNull(dg);
        // has a ChangeSummary
        ChangeSummary cs = root.getChangeSummary();
        assertNotNull(cs);
        // which is the same as the DataGraph ChangeSummary
        assertTrue(cs == dg.getChangeSummary());
        assertTrue(cs.getDataGraph() == dg);
        // ChangeSummary, by default, has logging off
        assertFalse(cs.isLogging());

        // one root object
        assertTrue(root.getRootObject() == root);
        assertTrue(dg.getRootObject() == root);
        assertTrue(cs.getRootObject() == root);

        String uri = "http://sdo/test/po1";
        // create a new DataObject
        DataObject po = factory.create(uri, "PurchaseOrder");
        // attach it to a data graph
        Property p = xsdHelper.getGlobalProperty(uri, "purchaseOrder", true);
        //root.set("purchaseOrder", po);
        root.set(p, po);
        // its root object is the data graph
        assertTrue(po.getRootObject() == root);
        // it has the same DataGraph
        assertTrue(po.getDataGraph() == dg);
        // has ChangeSummary
        assertNotNull(po.getChangeSummary());
        assertTrue(po.getChangeSummary() == cs);
    }

    /* set a DataObject containment property to another DataObject */
    /*
    public void testEffectOfSet()
    {
        DataObject parent = ...; // no DataGraph
        DataObject child = ...;
        parent.set("", child);

        assertTrue(child.getContainer() == parent);
        // child has same root object as parent
        assertTrue(child.getRootObject() == parent.getRootObject());
        // child is assigned to parent's DataGraph, if it exists
        assertTrue(child.getDataGraph() == parent.getDataGraph());
        // child is assigned to parent's ChangeSummary, if it exists
        assertTrue(child.getChangeSummary() == parent.getChangeSummary());

        DataObject newParent = ...; // has DataGraph
        newParent.set("", child);
        assertTrue(child.getRootObject() == newParent.getRootObject());
        assertTrue(child.getDataGraph() == newParent.getDataGraph());
        assertTrue(child.getChangeSummary() == parent.getChangeSummary());
    }

    public void testEffectOfDetach()
    {
        DataObject parent = ...; // has DataGraph
        DataObject child = parent.getDataObject(...);
        assertTrue(child.getDataGraph() == parent.getDataGraph());
        assertTrue(child.getChangeSummmary() == parent.getChangeSummary());
        child.detach();

        // child is detached from parent
        assertNull(child.getContainer());
        // child has no DataGraph or ChangeSummary
        assertNull(child.getDataGraph());
        assertNull(child.getChangeSummary());

        parent.detach();
        assertNull(parent.getContainer());
        assertNull(parent.getDataGraph());
        assertNull(parent.getChangeSummary());
    }

    public void testEffectOfDelete()
    {
        DataObject parent = ...; // has DataGraph
        DataObject child = parent.getDataObject(...);
        assertTrue(child.getDataGraph() == parent.getDataGraph());
        assertTrue(child.getChangeSummmary() == parent.getChangeSummary());
        child.delete();

        // child is detached from parent
        assertNull(child.getContainer());
        // child has no DataGraph or ChangeSummary
        assertNull(child.getDataGraph());
        assertNull(child.getChangeSummary());

        parent.detach();
        assertNull(parent.getContainer());
        assertNull(parent.getDataGraph());
        assertNull(parent.getChangeSummary());
    }
    */

    /* edge case: delete root object of DataGraph - do we allow it?
       - in this case, the DataObject and the DataGraph are disconnected,
       leaving an empty DataGraph and a DataObject (of DataGraphType)
       without a DataGraph! */
    
    public void testDeleteRootObject()
    {
        // Create a new DataObject of type DataGraphType
        DataObject rootObject = 
            factory.create(DATAGRAPHTYPE);

        // will have a DataGraph attached to it
        DataGraph dataGraph = rootObject.getDataGraph();
        assertNotNull(dataGraph);

        // detaching the root will leave an empty DataGraph
        rootObject.detach();
        //assertNull(dataGraph.getRootObject()); // apparently NOT!

        // creating a new rootObject which doesn't have the DataGraphType
        // - this is not allowed!
        dataGraph.createRootObject("po_uri", "PurchaseOrder"); // IllegalArgumentException: Root object already exists
        DataObject newRootObject = dataGraph.getRootObject();

    }
    

    // Unmarshal a DataObject. Test:
    // DataObject.getRootObject()
    //            getDataGraph()
    //            getChangeSummary()


    /* unmarshal a DataObject whose type does not contain a ChangeSummary
       property:
       a. not wrapped in a datagraph element
    */
    public void testUnmarshal1a() throws Exception
    {
        File f = getResourceFile("data", "po1.xml");
        InputStream in = new FileInputStream(f);
        XMLDocument doc = xmlHelper.load(in);
        in.close();

        assertEquals("purchaseOrder", doc.getRootElementName());
        DataObject rootObject = doc.getRootObject();
        DataObject shipTo = rootObject.getDataObject("shipTo");
        assertNotNull(shipTo);
        
        // rootObject will not belong to any DataGraph
        assertNull(rootObject.getDataGraph());

        // rootObject will not have a change summary
        assertNull(rootObject.getChangeSummary());
    }

    private DataObject _testUnmarshalDataGraph(XMLDocument doc)
    {
        assertEquals("datagraph", doc.getRootElementName());
        DataObject rootObject = doc.getRootObject();
        assertEquals(DATAGRAPHTYPE, rootObject.getType());
        DataObject po = rootObject.getDataObject("purchaseOrder");
        assertNotNull(po);
        assertTrue(rootObject == po.getRootObject());
        
        // rootObject will have a DataGraph created by default
        // that will contain any changes specified in the document
        DataGraph dg = rootObject.getDataGraph();
        assertNotNull(dg);
        assertTrue(po.getDataGraph() == dg);

        // rootObject will have a change summary
        ChangeSummary cs = rootObject.getChangeSummary();
        assertNotNull(cs);
        assertTrue(po.getChangeSummary() == cs);

        assertTrue(cs == dg.getChangeSummary());
        assertTrue(cs.getDataGraph() == dg);

        // one root object
        assertTrue(rootObject.getRootObject() == rootObject);
        assertTrue(dg.getRootObject() == rootObject);
        assertTrue(cs.getRootObject() == rootObject);
        return rootObject;
    }


    /* unmarshal a DataObject whose type does not contain a ChangeSummary
       property:
       b. wrapped in a datagraph element, no changeSummary element
    */
    public void testUnmarshal1b() throws Exception
    {
        File f = getResourceFile("data", "po1_dg0.xml");
        InputStream in = new FileInputStream(f);
        XMLDocument doc = xmlHelper.load(in);
        in.close();
        _testUnmarshalDataGraph(doc);
    }

    /* unmarshal a DataObject whose type does not contain a ChangeSummary
       property:
       c. wrapped in a datagraph element, with changeSummary element
    */
    public void testUnmarshal1c() throws Exception
    {
        File f = getResourceFile("data", "po1_dg1.xml");
        InputStream in = new FileInputStream(f);
        XMLDocument doc = xmlHelper.load(in);
        in.close();
        _testUnmarshalDataGraph(doc);
    }

    /* unmarshal a DataObject whose type contains a ChangeSummary property:
       a. not wrapped in a datagraph element
    */
    public void testUnmarshal2a() throws Exception
    {
        File f = getResourceFile("data", "po2.xml");
        InputStream in = new FileInputStream(f);
        XMLDocument doc = xmlHelper.load(in);
        in.close();

        assertEquals("purchaseOrder", doc.getRootElementName());
        DataObject rootObject = doc.getRootObject();
        DataObject shipTo = rootObject.getDataObject("shipTo");
        assertNotNull(shipTo);
        
        // rootObject will not belong to any DataGraph
        assertNull(rootObject.getDataGraph());

        // rootObject will have a change summary
        assertNotNull(rootObject.getChangeSummary());
        assertFalse(rootObject.getChangeSummary().isLogging());
    }

    /* unmarshal a DataObject whose type contains a ChangeSummary property:
       b. wrapped in a datagraph element, no changeSummary element at any level
    */
    public void testUnmarshal2b() throws Exception
    {
        File f = getResourceFile("data", "po2_dg0.xml");
        InputStream in = new FileInputStream(f);
        XMLDocument doc = xmlHelper.load(in);
        in.close();
        DataObject rootObject = _testUnmarshalDataGraph(doc);
        rootObject.getChangeSummary().beginLogging();
        f = new File(dir, "po2_dg.xml");
        OutputStream out = new FileOutputStream(f);
        doc = xmlHelper.createDocument(rootObject, "commonj.sdo", "datagraph");
        xmlHelper.save(doc, out, new Options().setSavePrettyPrint());
        out.close();
        compareXMLFiles(getResourceFile("data", "po2_dg1.xml"), f, IGNORE_WHITESPACE);
        compareXMLFiles(getResourceFile("data", "po2_dg1.xml"), f);
    }

    /* unmarshal a DataObject whose type contains a ChangeSummary property:
       c. wrapped in a datagraph element, with changeSummary element at
          datagraph level
    */
    public void testUnmarshal2c() throws Exception
    {
        File f = getResourceFile("data", "po2_dg1.xml");
        InputStream in = new FileInputStream(f);
        XMLDocument doc = xmlHelper.load(in);
        in.close();
        DataObject rootObject = _testUnmarshalDataGraph(doc);
        assertTrue(rootObject.getChangeSummary().isLogging());
    }

    /* unmarshal a DataObject whose type contains a ChangeSummary property:
       d. wrapped in a datagraph element, with changeSummary element inside
          the DataObject element -- this is NOT valid, but we don't throw
          an exception
    */
    public void testUnmarshal2d() throws Exception
    {
        File f = getResourceFile("data", "po2_dg2.xml");
        InputStream in = new FileInputStream(f);
        XMLDocument doc = xmlHelper.load(in);
        in.close();
    }

    /* marshal a DataObject with no ChangeSummary property */
    public void testMarshalDataObject1()
    {

    }

    /* marshal a DataObject with ChangeSummary property */
    public void testMarshalDataObject2()
    {

    }

    /* marshal a DataObject of DataGraphType */
    public void testMarshalDataGraph()
    {

    }
    
}
