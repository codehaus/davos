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
package dynamic;

import davos.sdo.DataObjectXML;
import davos.sdo.SDOContext;
import davos.sdo.SDOContextFactory;
import davos.sdo.TypeXML;
import davos.sdo.binding.BindingContext;
import davos.sdo.binding.BindingEngine;
import davos.sdo.binding.BindingException;
import davos.sdo.binding.BindingSystem;
import davos.sdo.impl.binding.CompileBindingSystem;
import davos.sdo.impl.binding.DefaultBindingContext;
import davos.sdo.impl.binding.DefaultBindingEngineImpl;
import davos.sdo.impl.binding.DynamicBindingSystem;
import davos.sdo.impl.helpers.DataGraphHelper;
import davos.sdo.impl.helpers.DataHelperImpl;
import davos.sdo.impl.type.BuiltInTypeSystem;
import davos.sdo.impl.type.PropertyImpl;
import davos.sdo.impl.type.TypeImpl;
import davos.sdo.impl.type.TypeSystemBase;
import davos.sdo.impl.util.DefaultFilerImpl;
import davos.sdo.impl.xpath.XPath;
import davos.sdo.type.TypeSystem;
import davos.sdo.util.Filer;
import javax.sdo.DataObject;
import javax.sdo.Property;
import javax.sdo.Sequence;
import javax.sdo.Type;
import javax.sdo.helper.DataFactory;
import javax.sdo.helper.DataHelper;
import javax.sdo.helper.TypeHelper;
import javax.sdo.helper.XMLDocument;
import javax.sdo.helper.XMLHelper;
import javax.sdo.helper.XSDHelper;
import javax.sdo.impl.HelperProvider;
import org.apache.xmlbeans.GDate;
import org.apache.xmlbeans.GDateBuilder;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.impl.schema.BuiltinSchemaTypeSystem;
import org.apache.xmlbeans.impl.schema.ClassLoaderResourceLoader;
import org.apache.xmlbeans.impl.store.Path;
import org.apache.xmlbeans.impl.util.XsTypeConverter;
import org.apache.xmlbeans.impl.values.XmlObjectBase;
import org.apache.xmlbeans.impl.xb.xsdschema.SchemaDocument;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.Externalizable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.io.StringReader;
import java.io.ByteArrayInputStream;
import java.io.Reader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

/**
 * @author Cezar Andrei (cezar.andrei at bea.com)
 *         Date: Jan 11, 2006
 */
public class Test
{
    private static final int ITER = 30;

    public static void main(String[] args)
        throws Exception
    {
        //createAndTraverse();
        //testBITypes();
        //testDynamicTypes();
        //testXmlLoad();
        //testXmlSave();

        //sdocomp();
        //staticTest();
        //staticTest2();
        //staticTest3();

        //testTSPersistance();
        //testBSPersistance();

        //testLists1();
        //testLists2();
        //testXO();

        //testText();
        //testSequence();
        //testTypeHelper();
        //testIsSet();

        //testSimpleTypeExtension();
        //testOpposite();
        //testSoapBody();
        //testXBns();
        //testXBnsXPath();
        //testXQueryValidation();

        //testSerialize();
        //testSerialize2();
        //testDynSchema();
        //testOpenContentProperty();
        //testDataGraph();
        //testDataConversion();
        //testCursor();
        //testXpath();
        //testMetadataOnTypeAndProperty();
        //testCycle();
        //testSubstitusionGroups();
        //testInstanceClass();
        //testSetUnknown();
        //testPropGlobal();
        //testPropOnDenamd();
        //testStripTrailingZeros();
        //testContexts();
        //testCDATAOld();
        //testCDATA();
        //testTypeHelperGetClass();
        //testEmptyContext();
        //testXbXPath();
        //testSDOList();
        //testParamTypes();
        //testXbSave();
        //testGetMixedText();
        //testSDOContextOnSTL();
        //testGMonth();
        //testDefaultInt();
        //testModifyQName();
        //testPI();
        //testXBSaver();
        //testSDOXPath();
        //testOnDemandCycle();
        //testXbGetSourceName();
        //testSingleGetSetOnManyProp();
        //testSdoTsLoad();
        //testXbCDATA();
        //testXbPath();
        testComplexElementWithSimpleContent();
    }

    static void createAndTraverse()
    {
        DataFactory dataFactory = HelperProvider.getDataFactory();

        DataObject purchaseOrder = dataFactory.create("commonj.sdo", "DataObject");

        DataObject customer = purchaseOrder.createDataObject("customer");
        customer.setString("name", "John Malkovich");

        DataObject lineitem;


        long time = System.nanoTime();
        for(int i=0; i<ITER; i++)
        {
//            System.out.println("    " + i);
            lineitem = purchaseOrder.createDataObject("lineitem");
            lineitem.set("sku", "ITEM " + i);
            lineitem.setInt("qty", i);
        }
        System.out.println("Create: " + (System.nanoTime()-time)/ITER);


        time = System.nanoTime();
        printDO(purchaseOrder);
        System.out.println("Read: " + (System.nanoTime()-time)/ITER);
    }


    private static void testBITypes()
    {
        TypeHelper typeHelper = HelperProvider.getTypeHelper();
        Type type = typeHelper.getType("commonj.sdo", "Int");
        ((TypeImpl)type).dump();
        type = typeHelper.getType("commonj.sdo", "DataObject");
        ((TypeImpl)type).dump();
        type = typeHelper.getType("commonj.sdo", "Type");
        ((TypeImpl)type).dump();
        type = typeHelper.getType("commonj.sdo", "Property");
        ((TypeImpl)type).dump();
        type = typeHelper.getType("commonj.sdo", "ChangeSummaryType");
        ((TypeImpl)type).dump();
        type = typeHelper.getType("commonj.sdo", "TextType");
        ((TypeImpl)type).dump();
    }

    static final String CUST_URI = "http://example.com/customer";
    static final String PO_URI = "po_uri";

    private static void testDynamicTypes()
    {
        TypeHelper types = TypeHelper.INSTANCE;
        Type intType = types.getType("commonj.sdo", "Int");
        Type stringType = types.getType("commonj.sdo", "String");

        // create a new Type for Customers
        DataObject customerTypeDescriptor = DataFactory.INSTANCE.create("commonj.sdo", "Type");
        customerTypeDescriptor.set("uri", CUST_URI);
        customerTypeDescriptor.set("name", "Customer");
//        customerTypeDescriptor.setBoolean("dataType", false);
//        customerTypeDescriptor.setBoolean("open", false);
//        customerTypeDescriptor.setBoolean("sequenced", false);
//        customerTypeDescriptor.setBoolean("abstract", false);

        // create a customer number property
        DataObject custNumProperty = customerTypeDescriptor.createDataObject("property");
        custNumProperty.set("name", "custNum");
//        custNumProperty.set("type", intType);
        custNumProperty.setBoolean("many", false);
//        custNumProperty.setBoolean("containment", false);
//        custNumProperty.setBoolean("readOnly", false);
//        custNumProperty.set("default", null);

        // create a first name property
        DataObject firstNameProperty = customerTypeDescriptor.createDataObject("property");
        firstNameProperty.set("name", "firstName");
        firstNameProperty.set("type", stringType);
//        firstNameProperty.setBoolean("many", false);
//        firstNameProperty.setBoolean("containment", false);
//        firstNameProperty.setBoolean("readOnly", false);
//        firstNameProperty.set("default", null);

        // create a last name property
        DataObject lastNameProperty = customerTypeDescriptor.createDataObject("property");
        lastNameProperty.set("name", "lastName");
        lastNameProperty.set("type", stringType);
//        lastNameProperty.setBoolean("many", false);
//        lastNameProperty.setBoolean("containment", false);
//        lastNameProperty.setBoolean("readOnly", false);
//        lastNameProperty.set("default", null);

        // now define the Customer type so that customers can be made
        System.out.println("Printig dataObject customerTypeDescriptor:");
        printDO(customerTypeDescriptor);

        Type t = types.define(customerTypeDescriptor);

        System.out.println("\ncustomerType.dump()");
        ((TypeImpl)t).dump();
    }

    private static Type getDynamicPOType()
    {
        TypeHelper types = TypeHelper.INSTANCE;
        Type intType = types.getType("commonj.sdo", "Int");
        Type stringType = types.getType("commonj.sdo", "String");

        // create a new Type for PurchaseOrder
        DataObject poTypeDescriptor = DataFactory.INSTANCE.create("commonj.sdo", "Type");
        poTypeDescriptor.set("uri", PO_URI);
        poTypeDescriptor.set("name", "PurchaseOrder");
//        poTypeDescriptor.setBoolean("dataType", false);
        poTypeDescriptor.setBoolean("open", true);
        poTypeDescriptor.setBoolean("sequenced", false);
//        poTypeDescriptor.setBoolean("abstract", false);

        DataObject custNumProperty = poTypeDescriptor.createDataObject("property");
        custNumProperty.set("name", "itemNo");
        custNumProperty.set("type", intType);
        custNumProperty.setBoolean("many", true);
//        custNumProperty.setBoolean("containment", false);
//        custNumProperty.setBoolean("readOnly", false);
//        custNumProperty.set("default", null);

        DataObject firstNameProperty = poTypeDescriptor.createDataObject("property");
        firstNameProperty.set("name", "itemName");
        firstNameProperty.set("type", stringType);
        firstNameProperty.setBoolean("many", true);
//        firstNameProperty.setBoolean("containment", false);
//        firstNameProperty.setBoolean("readOnly", false);
//        firstNameProperty.set("default", null);


        System.out.println("Printig dataObject poTypeDescriptor:");
        printDO(poTypeDescriptor);

        Type t = types.define(poTypeDescriptor);

        System.out.println("\nt.dump()");
        ((TypeImpl)t).dump();

        return t;
    }

    private static DataObject testXmlLoad()
    {
        String xml = "<a><b/><c>text</c></a>";
        XMLHelper xmlHelper = HelperProvider.getXMLHelper();
        XMLDocument xDoc = xmlHelper.load(xml);
        DataObject root = xDoc.getRootObject();
        System.out.println("   "  + xDoc.getRootElementName() + "@" + xDoc.getRootElementURI());
        printDO(root);
        return root;
    }

    private static void testXmlSave()
    {
        DataObject dataObj = testXmlLoad();

        XMLHelper xmlHelper = HelperProvider.getXMLHelper();
        String xml = xmlHelper.save(dataObj, "root_uri", "rootElem");

        System.out.println("Doc:\n" + xml);
    }

    static void xmlLoad()
    {
        String xml = "<purchaseorder xmlns='example'>" +
            "   <customer>" +
            "       <name>John Travolta</name>" +
            "   </customer>" +
            "   <lineitem>" +
            "       <sku>firstSKU</sku>" +
            "       <qty>9</qty>" +
            "   </lineitem>" +
            "</purchaseorder>";
        DataObject doc = XMLHelper.INSTANCE.load(xml).getRootObject();

        System.out.println("\nDoc: ");
        printDO("\t", doc);

        System.out.println("\nPurchaseOrder: ");
        DataObject po = (DataObject)doc.getList("purchaseorder").get(0);
        printDO("\t", po);

        System.out.println("\nCustomer: ");
        DataObject customer = po.getDataObject("customer");
        printDO("\t", customer);

        System.out.println("\nCustomer (modified): ");
        customer.setString("name", "Johnny Depp");
        printDO("\t", customer);

        for(int i=0 ; i<5; i++)
        {
            DataObject newLi = po.createDataObject(po.getType().getProperty("lineitem"));
            newLi.set("sku", "anotherSKU " + i);
            newLi.setInt("qty", i);
        }

        System.out.println("\nDoc (modified): ");
        customer.setString("name", "John Wayne");
        printDO("\t", doc);
    }

    static void printDO(DataObject obj)
    {
        printDO("  ", obj);
    }

    public static void printDO(String pre, DataObject obj)
    {
        if (obj==null)
        {
            System.out.println(pre + " NULL DataObject");
            return;
        }

        System.out.println(pre + "type: " + obj.getType().getName() + "@" + obj.getType().getURI());

        if (obj.getType().isSequenced() )
        {
            System.out.println(pre + "  Sequence:");
            Sequence seq = obj.getSequence();
            int size = seq.size();
            for (int i = 0; i < size; i++)
            {
                Property prop = seq.getProperty(i);
                if (prop==null)
                {
                    //text
                    System.out.println(pre + "    TEXT : " + seq.getValue(i));
                }
                else if (!prop.getType().isDataType())
                {
                    if (prop.isContainment())
                    {
                        System.out.println(pre + "    " + prop.getName());
                        Object value = seq.getValue(i);
                        if (value instanceof DataObject)
                        {
                            printDO(pre + "      ", (DataObject)value);
                        }
                        else
                        {
                            throw new IllegalArgumentException();
                            //System.out.println(pre + "       '" + printToString(seq.getValue(i)) + "'");
                        }
                    }
                    else
                        printO(pre, prop, seq.getValue(i));
                }
                else
                {
                    System.out.println(pre + "    " + prop.getName() + " : '" + printToString(seq.getValue(i)) + "'");
                }
            }
        }
        else
        {
            System.out.println(pre + "  Properties:");
            List props = obj.getInstanceProperties();

            for (Iterator i = props.iterator(); i.hasNext(); )
            {
                Property prop = (Property)i.next();

                if (prop.isMany())
                {
                    List vals = obj.getList(prop);
                    for (Iterator vi = vals.iterator(); vi.hasNext();)
                    {
                        Object o = vi.next();
                        printO(pre, prop, o);
                    }
                }
                else
                {
                    printO(pre, prop, obj.get(prop));
                }
            }
        }
    }

    private static void printO(String pre, Property prop, Object object)
    {
        if (prop.isContainment())
        {
            System.out.print(pre + "    " + prop.getName());
            if (object instanceof DataObject)
            {
                System.out.println("");
                printDO(pre + "    \t", (DataObject)object);
            }
            else
                System.out.println("\t\t:   " + object);
        }
        else
        {
            System.out.println(pre + "    " + prop.getName() + ( prop.getType().isDataType() ? " : \t" : " : REF\t ") + printToString(object));
        }
    }

    private static String printToString(Object object)
    {
        String val = object == null ? "null" : object.toString();
        val = val.replace("\n", "\\n");
        return val;
    }

    static void traverseDO(DataObject obj)
    {
//        String pre = "  ";
//        System.out.println(pre + "type: " + obj.getType().getName() + "@" + obj.getType().getURI());

        if (obj.getType().isSequenced() )
        {
//            System.out.println(pre + " Sequence:");
            Sequence seq = obj.getSequence();
            int size = seq.size();
            for (int i = 0; i < size; i++)
            {
                Property prop = seq.getProperty(i);
                if (prop.isContainment())
                {
//                    System.out.println(pre + "  " + prop.getName());
                    traverseDO((DataObject)seq.getValue(i));
                }
                else
                {
//                    System.out.println(pre + "  " + prop.getName() + " : " + seq.getValue(i));
                }
            }
        }
        else
        {
//            System.out.println(pre + " Properties:");
            List props = obj.getType().getProperties();

            for (Iterator i = props.iterator(); i.hasNext(); )
            {
                Property prop = (Property)i.next();

                if (prop.isMany())
                {
                    List vals = obj.getList(prop);
                    for (Iterator vi = vals.iterator(); vi.hasNext();)
                    {
                        Object o = vi.next();
                        traverseO(prop, o);
                    }
                }
                else
                {
                    traverseO(prop, obj.get(prop));
                }
            }
        }
    }

    private static void traverseO(Property prop, Object object)
    {
//        String pre = "    ";
        if (prop.isContainment())
        {
//            System.out.println(pre + "  " + prop.getName());
            traverseDO((DataObject)object);
        }
        else
        {
//            System.out.println(pre + "  " + prop.getName() + " : " + object);
        }
    }

    private static void sdocomp()
        throws BindingException, IOException
    {
        // creates a new type Customer and adds it to the default TypeSystem
        testDynamicTypes();
        TypeSystem ts = SDOContextFactory.getGlobalSDOContext().getTypeSystem();

        BindingEngine eng = new DefaultBindingEngineImpl();

        File sourceFilesOutputDir = new File("./sdocomp");
        sourceFilesOutputDir.mkdirs();

        Filer filer = new DefaultFilerImpl(sourceFilesOutputDir);

        BindingContext bindingContext = new DefaultBindingContext(filer, "myTest");
        eng.setContext(bindingContext);

        BindingSystem cbs = new CompileBindingSystem(new ClassLoaderResourceLoader(ts.getClass().getClassLoader()), BuiltInTypeSystem.INSTANCE);
        boolean rez = eng.bind(ts, cbs, new java.util.HashMap<TypeXML, String>(),
            new java.util.HashMap<TypeXML, String>());

        System.out.println("SDOContextFactory Compiler: " + (rez ? "Succesful" : "Failed"));
    }

    private static void staticTest()
    {
//        // creates a new type Customer and adds it to the default TypeSystem
//        testDynamicTypes();
//
//        TypeSystem ts = SDOContextFactory.getDefaultSDOTypeSystem();
//        DynamicBindingSystem dbs = (DynamicBindingSystem)SDOContextFactory.getGlobalSDOContext().getBindingSystem();
//
//        // add mapping to the right instance class
//        dbs.addMapping(ts.getTypeXML(CUST_URI, "Customer"), com.example.ipo.customer.impl.CustomerImpl.class);
//
//        DataObject dataObj = DataFactory.INSTANCE.create(CUST_URI, "Customer");
//        System.out.println("Cust: " + dataObj);
//
//        Customer customer = (Customer)dataObj;
//
//        customer.setFirstName("John");
//        customer.setCustNum(8);
//
//        System.out.println("  customer.getFirstName(): " + customer.getFirstName());
//        System.out.println("  customer.getCustNum()  : " + customer.getCustNum());
    }

    static String IPO_URI = "http://www.example.com/IPO";

    private static void staticTest2()
        throws FileNotFoundException, MalformedURLException
    {
        TypeSystem ts = SDOContextFactory.getGlobalSDOContext().getTypeSystem();
        DynamicBindingSystem dbs = (DynamicBindingSystem) SDOContextFactory.getGlobalSDOContext().getBindingSystem();

        // adds
        File f = new File("..\\test\\resources\\checkin\\IPO.xsd");

        List<TypeXML> types = XSDHelper.INSTANCE.define(new java.io.FileInputStream(f), f.toURL().toString());
        for (TypeXML t : types)
        {
            ((TypeSystemBase)ts).addTypeMapping(t);
            System.out.println(" type: " + t.getName() + " @ " + t.getURI());
        }

        // add mapping to the right instance class
//        dbs.addMapping(ts.getTypeXML(IPO_URI, "PurchaseOrderType"), com.example.ipo.impl.PurchaseOrderTypeImpl.class);

        DataObject dataObj = DataFactory.INSTANCE.create(IPO_URI, "PurchaseOrderType");
        System.out.println("PurchaseOrder: " + dataObj);

//        PurchaseOrderType po = (PurchaseOrderType)dataObj;
//
//        po.setProductName("SuperCool product");
//
//        System.out.println("  po.getProductName(): " + po.getProductName());
    }

    private static void staticTest3()
        throws IOException, MalformedURLException
    {
//        TypeSystemBase ts = (TypeSystemBase)SDOContextFactory.getDefaultSDOTypeSystem();
//        DynamicBindingSystem bs = (DynamicBindingSystem)SDOContextFactory.getGlobalSDOContext().getBindingSystem();
//
//        // adds
//        ClassLoader cl = Test.class.getClassLoader();
//
//        String id = ts.readIdForTopLevelQName(cl, new QName(IPO_URI, "purchaseOrder"));
//        ts.loadTypeSystemFromId(cl, id);
//        bs.loadBindingSystemWithId(cl, id);

/*        XMLDocument doc = XMLHelper.INSTANCE.load("<purchaseOrder xmlns='http://www.example.com/IPO'>" +
            "<productName>Unmarshaled SuperCool product</productName></purchaseOrder>");

        DataObject dataObj = doc.getRootObject();
        System.out.println("PurchaseOrder: " + dataObj);

        Type t = dataObj.getType();
        Property p = t.getProperty("productName");
        Object o = dataObj.get(p);
        System.out.println("    product name: " + o);

        PurchaseOrderType po = (PurchaseOrderType)dataObj;

        System.out.println("  po.getProductName(): " + po.getProductName());

        po.setProductName("SuperCool product");

        System.out.println("  po.getProductName(): " + po.getProductName());

        XMLHelper.INSTANCE.save(doc, System.out, null);
*/    }


    private static void testTSPersistance()
        throws IOException
    {
        TypeSystem ts = SDOContextFactory.getGlobalSDOContext().getTypeSystem();

        TypeSystemBase tsb = (TypeSystemBase)ts;

        File basedir = new File("./out");
        basedir.delete();

        Filer filer = new DefaultFilerImpl(basedir);
        tsb.saveTypeSystemWithName(filer, "myTypeSystem", "12345", null);

        //TypeSystemBase loaded = TypeSystemLoader.newInstance(Test.class.getClassLoader());
        //loaded.loadTypeSystemFromName( Test.class.getClassLoader(), "myTypeSystem");
    }

    private static void testBSPersistance()
    {
        //InputStream is = ClassLoader.getSystemClassLoader().getResourceAsStream("./checkin/simple.xsd");
        //XSDHelper.INSTANCE.define(is, ".");

        BindingSystem bs = SDOContextFactory.getGlobalSDOContext().getBindingSystem();
        TypeSystem ts = SDOContextFactory.getGlobalSDOContext().getTypeSystem();

        ((TypeSystemBase)ts).dumpWithoutBuiltinTypes();
        ((DynamicBindingSystem)bs).dump();

        //bs.loadTypeByTypeName(Thread.currentThread().getContextClassLoader(), "http://www.example.com/simple1", "Quote");
        //bs.loadTypeBySchemaTypeName(Thread.currentThread().getContextClassLoader(), "http://www.example.com/simple1", "Quote");
        //bs.loadTypeByTopLevelElementName(Thread.currentThread().getContextClassLoader(), "http://www.example.com/simple1", "quote");

        System.out.println("\n\n  " +  ts.getTypeXML("http://www.example.com/simple1", "Quote") + "\n");

        ((TypeSystemBase)ts).dumpWithoutBuiltinTypes();
        ((DynamicBindingSystem)bs).dump();

        //System.out.println("TypeHelper.INSTANCE.getType(Quote.class) : " + TypeHelper.INSTANCE.getType(Quote.class));
        System.out.println("TypeHelper.INSTANCE.getType(uri, typeName) : " + TypeHelper.INSTANCE.getType("http://www.example.com/simple1", "Quote"));
        //System.out.println("Quote.type : " + Quote.type);
    }

    private static void testLists1()
    {
        Type t = TypeHelper.INSTANCE.getType("http://www.example.com/simple1", "Quote");
        DataObject o = DataFactory.INSTANCE.create(t);

        Property quotesProp = t.getProperty("quotes");

        List quotes = o.getList(quotesProp);
        printList("1  o.getList(quotes): ", quotes);

        DataObject q1 = o.createDataObject(quotesProp);  q1.set("symbol", "Q1");
        DataObject q2 = o.createDataObject(quotesProp);  q2.set("symbol", "Q2");
        DataObject q3 = o.createDataObject(quotesProp);  q3.set("symbol", "Q3");
        printList("2  o.getList(quotes): ", quotes);

        quotes.add(q1);
        quotes.add(q2);
        quotes.add(q3);
        printList("3  o.getList(quotes): ", quotes);

        List q1quotes = q1.getList(quotesProp);
        q1quotes.add(q3);
        printList("4  q1.getList(quotes): ", q1quotes);

        q1quotes.remove(q3);
        printList("5  q1.getList(quotes): ", q1quotes);

        quotes.remove(5);
        printList("6  o.getList(quotes): ", quotes);

        printDO(o);
    }

    private static void testLists2()
    {
        Type t = getDynamicPOType();
        DataObject o = DataFactory.INSTANCE.create(t);

        Property propItemNo = t.getProperty("itemNo");
        Property propItemName = t.getProperty("itemName");

        printList("1:  o.getList(propItemNo): ", o.getList(propItemNo));
        printList("2:  o.getList(propItemName): ", o.getList(propItemName));

        List li = new ArrayList();
        li.add(5);
        li.add(6);
        li.add(7);
        o.set(propItemNo, li);
        printList("3:  o.getList(propItemNo): ", o.getList(propItemNo));

        li.clear();
        li.add("Bill");
        o.set(propItemName, li);
        printList("4:  o.getList(propItemName): ", o.getList(propItemName));

        printDO(o);
    }

    private static void printList(String s, List list)
    {
        System.out.println(s + "  size: "+ list.size());
        for (Object o : list)
        {
            System.out.println("    " + o);
        }
    }

    private static void testXO()
        throws IOException, XmlException
    {
        XmlObject xo = XmlObject.Factory.newInstance();
        XmlCursor xc = xo.newCursor();
        xc.toNextToken();
        xc.beginElement(new QName("foo_uri", "foo", ""));
        xc.beginElement(new QName("", "bar"));
        xc.insertElement("foo2", "uri_foo2");
        xc.dispose();

        System.out.print("1-- xo.save(System.out): ");
        xo.save(System.out);

        System.out.print("\n2-- xo.xmlText(): " + xo.xmlText());

        XmlOptions opts = new XmlOptions();
        opts.setSaveAggressiveNamespaces();
        opts.setSavePrettyPrint();
        opts.setSavePrettyPrintIndent(4);

        System.out.print("\n\n3-- xo.save(System.out): ");
        xo.save(System.out, opts);

        System.out.print("\n4-- xo.xmlText(): " + xo.xmlText(opts));


//        xo = XmlObject.Factory.parse("<foo xmlns='foo_uri'><bar xmlns=''/></foo>");
//
//        System.out.print("\n\n3-- xo.save(System.out): ");
//        xo.save(System.out);
//        System.out.print("\n4-- xo.xmlText(): " + xo.xmlText());
//

//
//        Map suMap = new HashMap();
//        suMap.put("foo_uri", "f");
//        suMap.put("", "");
//        opts.setSaveSuggestedPrefixes(suMap);
//
//        Map implMap = new HashMap();
//        implMap.put("f", "foo_uri");
//        implMap.put("", "");
//        opts.setSaveImplicitNamespaces(implMap);
//
//        System.out.print("\n\n5-- xo.save(System.out): ");
//        xo.save(System.out, opts);
//        System.out.print("\n6-- xo.xmlText(): " + xo.xmlText(opts));
    }

    private static void testText()
    {
        String xml = "<root>\n" + "  <simple-type>simple content</simple-type>\n" +
            "  <complex-type type=\"example\">simple content 2</complex-type>\n" +
            "  <complex-type content=\"mixed\">this <b>content</b> is mixed</complex-type>" +
            "</root>";
        XMLDocument doc = XMLHelper.INSTANCE.load(xml);
        DataObject root = doc.getRootObject();

        printDO(root);

        System.out.println("1: " + root.getString("simple-type.0/text.0"));
        System.out.println("2: " + root.getDataObject("complex-type.0").getSequence().getValue(0));
        System.out.println("2.1: " + root.getDataObject("complex-type.0").getSequence().getValue(1));
        System.out.println("3: " + root.getDataObject("complex-type.0").getString("text.0"));
        System.out.println("4: " + root.getDataObject("complex-type.1").getString("text.0"));
        System.out.println("5: " + root.getDataObject("complex-type.1").getString("text.1"));
    }

    private static void testSequence()
    {
        Type t = getDynamicPOType();
        DataObject o = DataFactory.INSTANCE.create(t);

        Property propItemNo = t.getProperty("itemNo");
        Property propItemName = t.getProperty("itemName");

        Sequence seq = o.getSequence();
        seq.add("itemNo", 0);
        seq.add("itemName", "i1");
        seq.add(propItemNo, 2);
        seq.add(propItemName, "i3");

        printList("1:  o.getList(propItemNo): ", o.getList(propItemNo));
        printList("2:  o.getList(propItemName): ", o.getList(propItemName));

        System.out.println("Sequence: ");
        for (int i = 0; i< seq.size(); i++ )
        {
            System.out.println("  " + i + ": " + seq.getProperty(i) + "\t\t" + seq.getValue(i));
        }
    }

    private static void testTypeHelper()
    {
        String uri = "http://www.example.com/IPO";
        String typeName = "Address";
        Type addressType = TypeHelper.INSTANCE.getType(uri, typeName);
    }

    private static void testIsSet()
    {
        DataObject dobj = DataFactory.INSTANCE.create(getDynamicPOType());

        List list = dobj.getList("itemName");
        list.add("first");
        list.add("second");
        //dobj.set("itemName", list); // unnecessary now that list bug is fixed
        dobj.set("itemName[2]", "third");
        System.out.println("1. " + dobj.get("itemName[1]")); // first
        System.out.println("2. " + dobj.get("itemName[2]")); // third
        System.out.println("3. " + dobj.get("itemName[3]")); // null
        // ClassCastException if following two lines are not commented out
        System.out.println("4. " + dobj.isSet("itemName[1]")); // true
        System.out.println("5. " + dobj.isSet("itemName[2]")); // true
        // NPE if following line is not commented out
        System.out.println("6. " + dobj.isSet("itemName[3]"));
        dobj.unset("itemName[1]");
        System.out.println("7. " + dobj.get("itemName[1]")); // third
        System.out.println("8. " + dobj.get("itemName[2]")); // null
    }

    private static void testSimpleTypeExtension()
        throws IOException
    {
//        File f = new File("../test/resources/checkin", "employees.xml");
//        InputStream in = new FileInputStream(f);
//        XMLDocument doc = XMLHelper.INSTANCE.load(in);
//        DataObject root = doc.getRootObject();
//        in.close();
//
//        DataObject emp = root.getDataObject("employee.1");
//        assert emp!=null;
//
//        System.out.println("[Sally Smith] " + emp.get("name"));
//        PhoneType phone = (PhoneType)emp.get("phone[location='work']");
//
//        assert phone!=null;
//        System.out.println("phone location: " + phone.getLocation()); // work
//        System.out.println("phone number: " + phone.getValue()); // null
//        System.out.println(emp.get("phone[location='work']/value")); // null

        TypeXML type = SDOContextFactory.getGlobalSDOContext().getBindingSystem().loadTypeBySchemaTypeName("simpleTypeTest" , "myIntType");

        System.out.println(" Type: " + type);
        System.out.println("   instanceClass: " + type.getInstanceClass());
    }

    private static void testOpposite()
    {
        TypeHelper types = TypeHelper.INSTANCE;
        Type stringType = types.getType("commonj.sdo", "String");

        // create a new Type for Person
        DataObject personTypeDescriptor = DataFactory.INSTANCE.create("commonj.sdo", "Type");
        personTypeDescriptor.set("uri", PO_URI);
        personTypeDescriptor.set("name", "Person");
//        personTypeDescriptor.setBoolean("dataType", false);
//        personTypeDescriptor.setBoolean("open", true);
//        personTypeDescriptor.setBoolean("sequenced", true);

        // Person.name
        DataObject namePProperty = personTypeDescriptor.createDataObject("property");
        namePProperty.set("name", "name");
        namePProperty.set("type", stringType);
//        namePProperty.setBoolean("many", true);
        namePProperty.setBoolean("containment", true);


        // create a new Type for House
        DataObject houseTypeDescriptor = DataFactory.INSTANCE.create("commonj.sdo", "Type");
        houseTypeDescriptor.set("uri", PO_URI);
        houseTypeDescriptor.set("name", "House");
//        houseTypeDescriptor.setBoolean("dataType", false);
//        houseTypeDescriptor.setBoolean("open", true);
//        houseTypeDescriptor.setBoolean("sequenced", true);

        // House.color
        DataObject colorHProperty = houseTypeDescriptor.createDataObject("property");
        colorHProperty.set("name", "color");
        colorHProperty.set("type", stringType);
//        colorHProperty.setBoolean("many", true);
        colorHProperty.setBoolean("containment", true);

        // Person.owns
        DataObject ownsPProperty = personTypeDescriptor.createDataObject("property");
        ownsPProperty.set("name", "owns");
        ownsPProperty.set("type", houseTypeDescriptor);
//        ownsPProperty.setBoolean("many", true);
        ownsPProperty.setBoolean("containment", true);

        // House.ownedBy
        DataObject ownedByHProperty = houseTypeDescriptor.createDataObject("property");
        ownedByHProperty.set("name", "ownedBy");
        ownedByHProperty.set("type", personTypeDescriptor);
//        ownedByHProperty.setBoolean("many", true);
//        ownedByHProperty.setBoolean("containment", true);
        ownedByHProperty.set("opposite", ownsPProperty);

        ownsPProperty.set("opposite", ownedByHProperty);

        // Person.visited
        DataObject visitedPProperty = personTypeDescriptor.createDataObject("property");
        visitedPProperty.set("name", "visited");
        visitedPProperty.set("type", houseTypeDescriptor);
        visitedPProperty.setBoolean("many", true);
        visitedPProperty.setBoolean("containment", true);

        // House.visitedBy
        DataObject visitedByHProperty = houseTypeDescriptor.createDataObject("property");
        visitedByHProperty.set("name", "visitedBy");
        visitedByHProperty.set("type", personTypeDescriptor);
//        ownedByHProperty.setBoolean("many", true);
//        ownedByHProperty.setBoolean("containment", true);

        visitedByHProperty.set("opposite", visitedPProperty);

        visitedPProperty.set("opposite", visitedByHProperty);



        Type persType = TypeHelper.INSTANCE.define(personTypeDescriptor);



        BindingSystem bs = SDOContextFactory.getGlobalSDOContext().getBindingSystem();
        TypeSystem ts = SDOContextFactory.getGlobalSDOContext().getTypeSystem();

        ((TypeSystemBase)ts).dumpWithoutBuiltinTypes();
        ((DynamicBindingSystem)bs).dump();

        DataObject person = DataFactory.INSTANCE.create(persType);
        person.set("name", "John");
        DataObject house = person.createDataObject("owns");
        house.set("color" , "red");

        printDO(person);

        System.out.println("  person.getDataObject(\"owns\").get(\"color\") : " + person.getDataObject("owns").get("color"));
        System.out.println("  person.getDataObject(\"owns\").getDataObject(\"ownedBy\").get(\"name\") : " + person.getDataObject("owns").getDataObject("ownedBy").get("name"));

        Property ownsP = persType.getProperty("owns");
        Property visitedP = persType.getProperty("visited");
        Type houseType = ownsP.getType();
        Property ownedByH = houseType.getProperty("ownedBy");

        house.unset(ownedByH);
        System.out.println("\n\nhouse.unset(ownedByH):");
        printDO(person);
        printDO(house);

        house.set(ownedByH, person);
        System.out.println("\n\nhouse.set(ownedByH, person):");
        printDO(person);

        person.unset(ownsP);
        System.out.println("\n\nperson.unset(ownsP):");
        printDO(person);
        printDO(house);


        System.out.println("\n\nperson.createDO(visited) blue+green:");
        DataObject visitedHouse1 = person.createDataObject("visited");
        visitedHouse1.set("color" , "blue");
        DataObject visitedHouse2 = person.createDataObject("visited");
        visitedHouse2.set("color" , "green");

        printDO(person);

        System.out.println("\n\nperson.unset(visited)");
        person.unset(visitedP);
        printDO(person);
    }

    private static void testSoapBody()
        throws XmlException
    {
        XmlObject env = XmlObject.Factory.parse("<soap:env xmlns:soap='soapuri'><soap:body><payload/></soap:body></soap:env>");
        XmlObject payloadDoc = XmlObject.Factory.newInstance();
        XmlObject oldPayload = env.selectChildren("soapuri", "env")[0].selectChildren("soapuri", "body")[0];

        XmlCursor newPLCursor = payloadDoc.newCursor();
        newPLCursor.toNextToken(); // pass over start doc
        System.out.println("  tokenType: " + newPLCursor.currentTokenType());

        XmlCursor oldPLCursor = oldPayload.newCursor();

        oldPLCursor.moveXmlContents(newPLCursor);

        System.out.println("PayloadDoc: " + payloadDoc);


        XmlObject payloadDoc2 = XmlObject.Factory.newInstance();
        XmlCursor newPLCursor2 = payloadDoc2.newCursor();
        newPLCursor2.toNextToken(); // pass over start doc


        newPLCursor.copyXmlContents(newPLCursor2);

        System.out.println("pl1 " + payloadDoc);
        payloadDoc.dump();
        System.out.println("pl2 " + payloadDoc2);
        payloadDoc2.dump();
        System.out.println("    " + payloadDoc.valueEquals(payloadDoc2));
    }

    private static void testXBns()
        throws XmlException, XMLStreamException
    {
        XmlObject xo1 = XmlObject.Factory.parse("<p1:a xmlns:p1='p1'></p1:a>");
        XmlObject xo2 = XmlObject.Factory.parse("<p2:b xmlns:p2='p2'></p2:b>");

        XmlCursor xc1 = xo1.newCursor();
        XmlCursor xc2 = xo2.newCursor();

        xc1.toFirstChild();

        //XmlObject a = xc1.getObject();
        //a.set(xo2);
        //System.out.println("xo1: " + xo1);

//        xc1.toFirstContentToken();
//
//        xc2.toFirstChild();
//
//        xc2.moveXml(xc1);
//
//        System.out.println(xo1);
//
//        xc1.insertElementWithText(new QName("uri", "local"), "some text");
//        System.out.println(xo1);

        xc1.dispose();
        xc2.dispose();

        XMLStreamReader xsr = XMLInputFactory.newInstance().createXMLStreamReader(new StringReader("<a>text</a>"));
        XmlObject xo = XmlObject.Factory.parse(xsr);
        System.out.println("xo: " + xo);
    }

    private static void testXBnsXPath()
        throws XmlException
    {
        XmlObject xo1 = XmlObject.Factory.parse("<soap-env:Header xmlns:soap-env=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
            "    <y:custom-token xmlns:y=\"http://foo\">\n" +
            "        <y:a>this-is-a-token</y:a>\n" +
            "    </y:custom-token>\n" + "</soap-env:Header>");

        XmlCursor xc1 = xo1.newCursor();

        xc1.toFirstChild();

        XmlOptions opts = new XmlOptions();
        opts.put(Path._useXqrlForXpath);
        xc1.selectPath("declare namespace y=\"http://foo\"; ./y:custom-token/y:a/text()", opts);
        System.out.println("  hasSel: " + xc1.hasNextSelection());
        xc1.toNextSelection();

        System.out.println(" xc1.currentTokenType: " + xc1.currentTokenType());
        System.out.println(" xc1.getTextValue: " + xc1.getTextValue());

        xc1.dispose();
    }

    private static void testXQueryValidation()
        throws XmlException
    {
        String query = "import schema namespace ns= \"http://www.xqrl.com/bib\" at \"file://tests/www.xqrl.com/schemas/bib.xsd\";\n" +
            "(: declare namespace ns = \"http://www.xqrl.com/bib\":)\n" + "\n" + "(\n" + "  validate {\n" +
            "    <bib xmlns=\"http://www.xqrl.com/bib\"> {\n" +
            "      (((validate { fn:doc(\"file://tests/test0v/data/bib.xml\") } )/bib) treat as element(*, BibType))/book\n" +
            "    } </bib>\n" + "  }\n" + ") treat as element(*, ns:BibType)";

        XmlObject xo1 = XmlObject.Factory.parse("<p1:a xmlns:p1='p1'></p1:a>");
        XmlObject[] res = xo1.execQuery(query);
        for (int i = 0; i < res.length; i++)
        {
            XmlObject re = res[i];
            System.out.println(" " + i + " : " + re);
        }
    }

    public static class SerializableClass
        implements Serializable
    {
        String f1;
        int f2;

        public String toString() { return "SerializableClass: f1= " + f1 + " f2= " + f2; }
    }

    public static class ExternalizableClass
        implements Externalizable
    {
        String f1;
        int f2;

        public String toString() { return "ExternalizableClass: f1= " + f1 + " f2= " + f2; }

        public void writeExternal(ObjectOutput out)
            throws IOException
        {
            System.out.println("  -- writeExternal for " + this);
            out.writeObject(f1);
            out.writeInt(f2);
        }

        public void readExternal(ObjectInput in)
            throws IOException, ClassNotFoundException
        {
            f1 = (String) in.readObject();
            f2 = in.readInt();
            System.out.println("  -- readExternal for " + this);
        }
    }

    public static class SDOImpl
        implements Serializable, ExternalizableDelegator.Resolvable
    {
        String _name;
        SDOImpl _prev;
        SDOImpl _next;
        transient int _index;

        public void setNext(SDOImpl next)
        {
            _next = next;
            _next._prev = this;
        }

        public String toString()
        {
            String result = "SDOImpl: ";

            SDOImpl current = this;
            int i = 0;
            while(current._prev!=null)
            {
                i++;
                current = current._prev;
            }

            result = result + i + " [ " + current._name + " ";

            while(current._next!=null)
            {
                current = current._next;
                result = result + current._name + " ";
            }

            return  result + " ]";
        }

        Object writeReplace()
            throws ObjectStreamException
        {
            return new ExternalizableDelegator(this);
        }

        public Object myreadResolve()
            throws ObjectStreamException
        {
            int i = 0;
            SDOImpl current = this;
            while(i<_index)
            {
                i++;
                current = current._next;
            }
            return current;
        }

        public void mywriteExternal(ObjectOutput out)
            throws IOException
        {
            System.out.println("  -- mywriteExternal for " + this);

            SDOImpl current = this;
            int i = 0;
            while(current._prev!=null)
            {
                i++;
                current = current._prev;
            }
            out.writeInt(i);

            if (i==0)
                writeExternalForRoot(out);
            else
                out.writeObject(current);
        }

        public void myreadExternal(ObjectInput in)
            throws IOException, ClassNotFoundException
        {
            int index = in.readInt();
            SDOImpl current = this;

            if (index==0)
                readExternalForRoot(in);
            else
            {
                current = (SDOImpl)in.readObject();
                _next = current._next;
            }

            int i = 0;
            while(i<index)
            {
                i++;
                current = current._next;
            }

            _index = i;
            System.out.println("  -- myreadExternal for " + current);
        }

        public void writeExternalForRoot(ObjectOutput out)
            throws IOException
        {
            System.out.println("  -- writeExternalForRoot for " + this);

            if (_prev!=null) throw new IllegalStateException();

            SDOImpl current = this;
            do
            {
                out.writeObject(current._name + "WE");
                current = current._next;
                out.writeBoolean(current==null);
            }
            while(current!=null);
        }

        public void readExternalForRoot(ObjectInput in)
            throws IOException, ClassNotFoundException
        {
            boolean isNull;
            SDOImpl current = this;
            do
            {
                current._name = (String) in.readObject();
                isNull = in.readBoolean();
                if (!isNull)
                {
                    current.setNext(new SDOImpl());
                    current = current._next;
                }
            }
            while(!isNull);
            System.out.println("  -- readExternalForRoot for " + this);
        }
    }

    public static class ExternalizableDelegator
        implements Externalizable
    {
        public interface Resolvable
            //extends Externalizable
        {
            Object myreadResolve() throws ObjectStreamException;
            void mywriteExternal(ObjectOutput out) throws IOException;
            void myreadExternal(ObjectInput in) throws IOException, ClassNotFoundException;
        }

        static final long serialVersionUID = 1;
        transient Resolvable delegate;

        public ExternalizableDelegator()
        {
            delegate = null;
        }

        public ExternalizableDelegator(Object target)
        {
            delegate = (Resolvable)target;
        }

        public void writeExternal(ObjectOutput out) throws IOException
        {
            delegate.mywriteExternal(out);
        }

        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
        {
            delegate = new SDOImpl();
            delegate.myreadExternal(in);
        }

        public Object readResolve() throws ObjectStreamException
        {
            return delegate.myreadResolve();
        }
    }

    public static class UserClass
        implements Serializable
    {
        String f1;
        SerializableClass f2;
        ExternalizableClass f3;
        SDOImpl f4;
        SDOImpl f5;
        DataObject f6;
        DataObject f7;

        public String toString() { return "UserClass: f1=" + f1 + " f2={" + f2 + "} f3={" + f3 + "}\n  f4={" + f4 + "}\n" +
            "  f5={" + f5 + "}" + "\n  f6={" + f6 + "}" + "\n  f7={" + f7 + "}"; }
    }


    private static void testSerialize()
        throws IOException, ClassNotFoundException
    {
        SerializableClass sc = new SerializableClass();
        sc.f1 = "f1_value in SerializableClass";
        sc.f2 = 1;

        ExternalizableClass ec = new ExternalizableClass();
        ec.f1 = "f1_value in ExternalizableClass";
        ec.f2 = 2;

        UserClass uc = new UserClass();
        uc.f1 = "userInstance";
//        uc.f2 = sc;
//        uc.f3 = ec;
//        SDOImpl si = new SDOImpl();
//        si._name = "si1";
//        si.setNext(new SDOImpl());
//        si._next._name = "si2";
//        si._next.setNext(new SDOImpl());
//        si._next._next._name = "si3";
//        uc.f4 = si._next;
//        uc.f5 = uc.f4._next;


//        DataObject dobj = DataFactory.INSTANCE.create(getDynamicPOType());
//        dobj.set("itemNo", 1);
//        dobj.set("itemName", "name_for_item");
//        uc.f6 = dobj;
//        System.out.println("\nPrint dobj:");
//        printDO(dobj);
//        uc.f7 = dobj;
//
        XMLDocument doc = XMLHelper.INSTANCE.load("<root><a><aa>text1</aa></a><b><bb>text2</bb></b></root>");
        DataObject root = doc.getRootObject();
        uc.f6 = (DataObject)root.get("a.0");
        uc.f7 = (DataObject)root.get("b.0");
        printDO(uc.f6);
        printDO(uc.f7);

        System.out.println("Writing ... ");
        FileOutputStream fos = new FileOutputStream("tmp");
        ObjectOutput oos = new ObjectOutputStream(fos);
        oos.writeObject("Today");
        //System.out.println("  " + sc.getClass());
        //oos.writeObject(sc);
        //System.out.println("  " + ec.getClass());
        //oos.writeObject(ec);
        System.out.println("  " + uc.getClass() + "  " + uc);
        oos.writeObject(uc);
        oos.flush();
/*
        FileInputStream fis = new FileInputStream("tmp");
        ObjectInput ois = new ObjectInputStream(fis);
        System.out.println("Reading: " + ois.readObject());
        //sc = (SerializableClass) ois.readObject();
        //System.out.println(" " + sc );
        //ec = (ExternalizableClass) ois.readObject();
        //System.out.println(" " + ec );

        uc = (UserClass) ois.readObject();
        System.out.println(" " + uc );

        printDO(uc.f6);
        printDO(uc.f7);
*/

        // check threadlocal context
        SDOContext sdoContext = SDOContextFactory.createNewSDOContext(Thread.currentThread().getContextClassLoader());
        SDOContextFactory.setThreadLocalSDOContext(sdoContext);

        FileInputStream fis = new FileInputStream("tmp");
        ObjectInput ois = new ObjectInputStream(fis);
        System.out.println("Reading: " + ois.readObject());
        //sc = (SerializableClass) ois.readObject();
        //System.out.println(" " + sc );
        //ec = (ExternalizableClass) ois.readObject();
        //System.out.println(" " + ec );

        uc = (UserClass) ois.readObject();
        System.out.println(" " + uc );

        printDO(uc.f6);
        printDO(uc.f7);
    }

    private static void testSerialize2()
        throws IOException, ClassNotFoundException
    {
        XMLDocument doc = XMLHelper.INSTANCE.load("<root><a><aa>text1</aa></a><b><bb>text2</bb></b></root>");
        DataObject root = doc.getRootObject();
        DataObject dobj1 = (DataObject)root.get("a.0");
        DataObject dobj2 = (DataObject)root.get("b.0");
        printDO(dobj1);
        printDO(dobj2);

        System.out.println("Writing ... ");
        FileOutputStream fos = new FileOutputStream("tmp");
        ObjectOutput oos = new ObjectOutputStream(fos);

        oos.writeObject(dobj1);
        oos.writeObject(dobj2);


        // check SDOContext.readObject
        System.out.println("Reading - using sdoContext.readObject(ois);");
        SDOContext sdoContext = SDOContextFactory.createNewSDOContext(Thread.currentThread().getContextClassLoader());

        FileInputStream fis = new FileInputStream("tmp");
        ObjectInput ois = sdoContext.createObjectInputStream(fis);

        dobj1 = (DataObject)ois.readObject();
        System.out.println(" " + dobj1 );

        dobj2 = (DataObject)ois.readObject();
        System.out.println(" " + dobj2 );

        printDO(dobj1);
        printDO(dobj2);
    }


    public static void testDynSchema()
    {
        String xsd = "<xs:schema \n" +
            "    targetNamespace=\"testDynSchema\" \n" +
            "    xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" \n" +
            "    xmlns:stt=\"simpleTypeTest\">\n" + "\n" +
            "  <xs:element name=\"root\">\n" +
            "    <xs:complexType>\n" +
            "      <xs:sequence>\n" +
            "        <xs:element name=\"item\" type=\"xs:string\" minOccurs='0' maxOccurs='10' />\n" +
            "      </xs:sequence>\n" +
            "    </xs:complexType>\n" +
            "  </xs:element>\n" + "\n" +
            "</xs:schema>";

        List types  = XSDHelper.INSTANCE.define(xsd);
        for (Iterator it = types.iterator(); it.hasNext(); )
        {
             System.err.println((Type) it.next());
        }

        TypeSystemBase tsb = ((TypeSystemBase)(SDOContextFactory.getGlobalSDOContext().getTypeSystem()));
        tsb.dump(true);

        TypeXML t0 = ((TypeXML)types.get(0));
        //tsb.addGlobalProperty(...);
        tsb.dump(true);

        //((BindingSystemBase)(SDOContextFactory.getGlobalSDOContext().getBindingSystem())



//        String data = "<root xmlns='testDynSchema'><item>item_value</item></root>";
//        XMLDocument xml    = XMLHelper.INSTANCE.load(data);
//        DataObject  dobj   = xml.getRootObject();
//
//        System.out.println("  dobj: " + dobj);
//        System.out.println("  dobj.getType(): " + dobj.getType());
//

//        DataObject dObj = ...;
//        boolean isRootObject = dObj.getContainer() == null;
//
//        // keep in mind that it might be null
//        DataGraph initialDataGraph = dObj.getDataGraph();
//        dObj.detach(); // or
//        dObj.delete();
//
//        if ( isRootObject )
//            assert initialDataGraph==null || initialDataGraph.getRootObject() == null;
//        else
//        {
//            assert initialDataGraph==null || initialDataGraph.getRootObject() != null;
//        }
//
//        assert dObj.getDataGraph() == null;
//        assert dObj.getChangeSummary() == null;
//        assert dObj.getRootObject() == dObj;
//
//        assert dObj.getContainer() == null;

//        // Create a new DataObject of type DataGraphType
//        DataObject rootObject = DataFactory.INSTANCE.create("commonj.sdo", "DataGraphType");
//
//        // will have a DataGraph attached to it
//        DataGraph dataGraph = rootObject.getDataGraph();
//        assert dataGraph != null;
//
//        // detaching the root will make an empty datagraph
//        rootObject.detach();
//        assert dataGraph.getRootObject() == null;
//
//        // creating a new rootObject which doesn't have the DataGraphType
//        dataGraph.createRootObject("po_uri", "PurchaseOrder");
//        DataObject newRootObject = dataGraph.getRootObject();
//
//        XMLDocument xmlDoc = XMLHelper.INSTANCE.createDocument(newRootObject, "po_uri", "purchase-order");
//        XMLHelper.INSTANCE.save(xmlDoc, ...);

//        // has a ChangeSummary attached to it
//        assert rootObject.getChangeSummary() != null;
//        assert rootObject.getChangeSummary() == rootObject.getDataGraph().getChangeSummary();
//
//        // which by default is off
//        assert rootObject.getChangeSummary().isLogging() == false;
//
//        // RootObject is the same
//        assert rootObject.getChangeSummary().getRootObject() == rootObject.getRootObject();
//        assert rootObject.getDataGraph().getRootObject() == rootObject.getRootObject();
//
//        // DataGraph object is the same
//        assert rootObject.getChangeSummary().getDataGraph() == rootObject.getDataGraph();
//
//
//        String uri = "po_uri";
//        // Create a new DataObject that will not have a DataGraph case a)
//        DataObject po = DataFactory.INSTANCE.create(uri, "PurchaseOrder");
//
//        // attach it to a DataGraph
//        rootObject.set("purchase-order", po);
//
//        // it's RootObject is the initial one
//        assert po.getRootObject() == rootObject;
//
//        // has the same DataGraph
//        assert po.getDataGraph() == rootObject.getDataGraph();
//
//        // has a ChangeSummary attached to it
//        assert po.getChangeSummary() != null;
//        assert po.getChangeSummary() == rootObject.getChangeSummary();

//        XMLDocument xmlDoc = XMLHelper.INSTANCE.createDocument(po, uri, "purchase-order");
//        assert xmlDoc.getRootObject() == po.getRootObject();
//        // multiple XMLDocument object can point to the same RootObject
//        XMLDocument xmlDoc2 = XMLHelper.INSTANCE.createDocument(po, uri, "purchase-order2");
//        assert xmlDoc2.getRootObject() == po.getRootObject();
//
//        <purchase-order xmlns='po_uri'></purchase-order>

//        DataObject dObj = null;
//        DataGraph initialDataGraph = dObj.getDataGraph();
//        DataObject initialRootObject = dObj.getRootObject();
//
//        DataObject setter = null;
//        setter.setDataObject(x, dObj);
//
//        assert initialDataGraph != dObj.getDataGraph();
//        assert setter.getDataGraph() == dObj.getDataGraph();
//        assert setter.getRootObject() == dObj.getRootObject();

//        XMLDocument xmlDoc = XMLHelper.INSTANCE.load(xml);
//        DataObject rootObject = xmlDoc.getRootObject();
//        DataObject address = rootObject.getDataObject("address");
//
//        // rootObject will have a DataGraph created by default
//        assert rootObject.getDataGraph() != null;
    }

    private static void testOpenContentProperty()
    {
        String openPropUri = "someUri";
        String openPropDecimalName = "openPropDecimalName";
        DataObject openPropDecimalDefinition = DataFactory.INSTANCE.create("commonj.sdo", "Property");
        openPropDecimalDefinition.set("type", TypeHelper.INSTANCE.getType("commonj.sdo", "Decimal"));
        openPropDecimalDefinition.set("name", openPropDecimalName);
        Property openPropDecimal = TypeHelper.INSTANCE.defineOpenContentProperty(openPropUri, openPropDecimalDefinition);

        DataObject propDoubleDefinition = DataFactory.INSTANCE.create("commonj.sdo", "Property");
        propDoubleDefinition.set("type", TypeHelper.INSTANCE.getType("commonj.sdo", "Double"));
        propDoubleDefinition.set("name", "doubleProp");
        Property openPropDouble = TypeHelper.INSTANCE.defineOpenContentProperty(openPropUri, propDoubleDefinition);

        // Set an instance property on an open type DataObject
        DataObject open = DataFactory.INSTANCE.create(getDynamicPOType());

        open.setBigDecimal(openPropDecimal, new BigDecimal("1100.333333"));

        System.out.println("\n  open.get('openPropDecimalName'):" + open.get(openPropDecimalName));

        ((TypeSystemBase) SDOContextFactory.getGlobalSDOContext().getTypeSystem()).dumpWithoutBuiltinTypes();

        Property ocpProp = TypeHelper.INSTANCE.getOpenContentProperty(openPropUri, openPropDecimalName);
        System.out.println("Property: " + ocpProp);
        ((PropertyImpl)ocpProp).dump();

        Property xsdProp = XSDHelper.INSTANCE.getGlobalProperty(openPropUri, openPropDecimalName, true);
        System.out.println("XSDProperty elem: " + xsdProp);
        System.out.println("XSDProperty attr: " + XSDHelper.INSTANCE.getGlobalProperty(openPropUri, openPropDecimalName, false));
        ((PropertyImpl)xsdProp).dump();

        System.out.println("open.getString(openPropDecimal) : " + open.getString(openPropDecimal));

        double value = 123.45678;
        BigDecimal bd1 = new BigDecimal(value);
        BigDecimal bd2 = BigDecimal.valueOf(value);

        System.out.println("   value : " + value);
        System.out.println("   bd1   : " + bd1);
        System.out.println("   bd2   : " + bd2);

        open.setDouble(openPropDecimal, value);
        System.out.println("open.getString(openPropDecimal) : " + open.getString(openPropDecimal));

        open.setDouble(openPropDecimal, value);
        System.out.println("open.getString(openPropDecimal) : " + open.getString(openPropDecimal));

        open.setDouble(openPropDouble, value);
        System.out.println("open.getBigDecimal(openPropDouble) : " + open.getBigDecimal(openPropDouble));

        open.setString("doubleProp", "987.654321");
        Object o = open.get(openPropDouble);
        System.out.println("open.getBigDecimal(openPropDouble) : " + o.getClass() + "  " + o);

        open.unset(openPropDecimalName);
        open.unset("doubleProp");

        printDO(open);

        open.set("freshNewOnDemandProp", "a value");
        System.out.println("  open.get(\"freshNewOnDemandProp\": " + open.get("freshNewOnDemandProp"));

        printDO(open);
    }

    private static void testDataGraph()
    {
        //todo 302084
        XMLDocument xdoc = XMLHelper.INSTANCE.load("<datagraph xmlns='javax.sdo'><po/></datagraph>");
        System.out.println("rootelem: " + xdoc.getRootElementName() + " @ " + xdoc.getRootElementURI() );
    }

    private static void testDataConversion()
    {
        System.out.println("--1--");
        String s1 = "2001-12-31T23:59:59";
        System.out.println("strDate: " + s1);
        Date d1 = DataHelper.INSTANCE.toDate(s1);
        System.out.println("Date   : " + d1);

        System.out.println("--2--");
        String s2 = "2001-01-01T00:00:00.1Z";
        Date d2 = DataHelper.INSTANCE.toDate(s2);
        String s2actual = DataHelper.INSTANCE.toDateTime(d2);
        System.out.println("Date  : " + d2);
        System.out.println("DateTime  : " + s2actual);

        System.out.println("--3--");
        String s3 = "2001-12-31T18:30:45.12345+02:00";
        Date d3 = DataHelper.INSTANCE.toDate(s3);
        String s3actual = DataHelper.INSTANCE.toDateTime(d3);
        System.out.println("Date  : " + d2);
        System.out.println("DateTime  : " + s2actual);

        System.out.println("--4--");
        String s4 = "23:59:59";
        Date d4 = DataHelperImpl._toDate(s4);
        String s4a = DataHelper.INSTANCE.toTime(d4);
        System.out.println("s4a :" + s4a);

        System.out.println("--5--");
        String s5 = "00:00:00.1Z";
        Date d5 = DataHelper.INSTANCE.toDate(s5);
        String s5actual = DataHelper.INSTANCE.toTime(d5);
        System.out.println("s5actual  :" + s5actual);

        System.out.println("--6--");
        String s6 = "---14";
        Date d6 = DataHelper.INSTANCE.toDate(s6);
        String s6actual = DataHelper.INSTANCE.toDay(d6);
        System.out.println("s6actual  :" + s6actual);

        System.out.println("--7--");
        String s7 = "2001-07";
        Date d7 = DataHelper.INSTANCE.toDate(s7);
        String s7actual = DataHelper.INSTANCE.toYearMonth(d7);
        System.out.println("s7actual  :" + s7actual);

        System.out.println("--8--");
        String s8 = "--02-29Z";
        Date d8 = DataHelper.INSTANCE.toDate(s8);
        System.out.println("d8:     " + d8);
        String s8actual = DataHelper.INSTANCE.toMonthDay(d8);
        System.out.println("DataHelper.INSTANCE.toMonthDay(DataHelper.INSTANCE.toDate('--02-29'))  :" + s8actual);

        Calendar c = new GregorianCalendar();
        c.setTime(d8);
        c.setTimeZone(TimeZone.getTimeZone("GMT"));
        
        System.out.println("c: --" + c.get(Calendar.MONTH) + "-" + c.get(Calendar.DAY_OF_MONTH));

        System.out.println("Duration --- ");
        System.out.println("toCalendar('-P3Y')     " + DataHelper.INSTANCE.toCalendar("-P3Y"));
        System.out.println("toCalendar('-P2Y')     " + DataHelper.INSTANCE.toCalendar("-P2Y"));
        System.out.println("toCalendar('-P1Y')     " + DataHelper.INSTANCE.toCalendar("-P1Y"));
        System.out.println("toCalendar('P1D')     " + DataHelper.INSTANCE.toCalendar("P1D"));
        System.out.println("toCalendar('P1Y')     " + DataHelper.INSTANCE.toCalendar("P1Y"));
        System.out.println("toCalendar('P2Y')     " + DataHelper.INSTANCE.toCalendar("P2Y"));
        System.out.println("toCalendar('P3Y')     " + DataHelper.INSTANCE.toCalendar("P3Y"));

        System.out.println("");
        System.out.println("toCalendar('-P3M')     " + DataHelper.INSTANCE.toCalendar("-P3M"));
        System.out.println("toCalendar('-P2M')     " + DataHelper.INSTANCE.toCalendar("-P2M"));
        System.out.println("toCalendar('-P1M')     " + DataHelper.INSTANCE.toCalendar("-P1M"));
        System.out.println("toCalendar('P1M')     " + DataHelper.INSTANCE.toCalendar("P1M"));
        System.out.println("toCalendar('P1M')     " + DataHelper.INSTANCE.toCalendar("P1M"));
        System.out.println("toCalendar('P2M')     " + DataHelper.INSTANCE.toCalendar("P2M"));
        System.out.println("toCalendar('P3M')     " + DataHelper.INSTANCE.toCalendar("P3M"));

        System.out.println("\ntoCalendar('P1Y1D')     " + DataHelper.INSTANCE.toCalendar("P1Y1D"));

        System.out.println("toCalendar('-P1Y')    " + DataHelper.INSTANCE.toCalendar("-P1Y"));
        System.out.println("toCalendar('-P1Y1D')    " + DataHelper.INSTANCE.toCalendar("-P1Y1D"));
        Calendar c1 = DataHelper.INSTANCE.toCalendar("-P1Y1D");
        System.out.println("toCalendar('-P1Y1D')  E: " + ( c1.isSet(Calendar.ERA) ? c.get(Calendar.ERA) : "N/A" ) );
        System.out.println("                Y: " + ( c1.isSet(Calendar.YEAR) ? c.get(Calendar.YEAR) : "N/A" ) );
        System.out.println("                M: " + ( c1.isSet(Calendar.MONTH) ? c.get(Calendar.MONTH) : "N/A" ) );
        System.out.println("                D: " + ( c1.isSet(Calendar.DAY_OF_MONTH) ? c.get(Calendar.DAY_OF_MONTH) : "N/A" ) );

        System.out.println("toCalendar('PT0S')      " + DataHelper.INSTANCE.toCalendar("PT0S"));
        System.out.println("toCalendar('P1D')       " + DataHelper.INSTANCE.toCalendar("P1D"));
        System.out.println("toCalendar('PT1.2345S') " + DataHelper.INSTANCE.toCalendar("PT1.2345S"));
        System.out.println("toCalendar('PT0.01S')   " + DataHelper.INSTANCE.toCalendar("PT0.01S"));
        System.out.println("toCalendar('PT0.012000S')   " + DataHelper.INSTANCE.toCalendar("PT0.012000S"));
        System.out.println("toCalendar('-PT0.01S')   " + DataHelper.INSTANCE.toCalendar("-PT0.01S"));

        System.out.println("toDate('P1Y1D')         " + DataHelperImpl._toDateTime(DataHelper.INSTANCE.toDate("P1Y1D")));
        System.out.println("toDate('-P1Y1D')        " + DataHelperImpl._toDateTime(DataHelper.INSTANCE.toDate("-P1Y1D")));
        System.out.println("toDate('PT0S')          " + DataHelperImpl._toDateTime(DataHelper.INSTANCE.toDate("PT0S")));

        System.out.println("ToDuration: ---");
        System.out.println("PT0S:       " + DataHelper.INSTANCE.toDuration(DataHelper.INSTANCE.toCalendar("PT0S")));
        System.out.println("PT1S:       " + DataHelper.INSTANCE.toDuration(DataHelper.INSTANCE.toCalendar("PT1S")));
        System.out.println("PT1M:       " + DataHelper.INSTANCE.toDuration(DataHelper.INSTANCE.toCalendar("PT1M")));
        System.out.println("PT1H:       " + DataHelper.INSTANCE.toDuration(DataHelper.INSTANCE.toCalendar("PT1H")));
        System.out.println("P1D:        " + DataHelper.INSTANCE.toDuration(DataHelper.INSTANCE.toCalendar("P1D")));
        System.out.println("P1M:        " + DataHelper.INSTANCE.toDuration(DataHelper.INSTANCE.toCalendar("P1M")));
        System.out.println("P1Y:        " + DataHelper.INSTANCE.toDuration(DataHelper.INSTANCE.toCalendar("P1Y")));
        System.out.println("PT0.1S:     " + DataHelper.INSTANCE.toDuration(DataHelper.INSTANCE.toCalendar("PT0.1S")));
        System.out.println("PT0.01S:    " + DataHelper.INSTANCE.toDuration(DataHelper.INSTANCE.toCalendar("PT0.01S")));

        System.out.println("");
        System.out.println("-PT0S:      " + DataHelper.INSTANCE.toDuration(DataHelper.INSTANCE.toCalendar("-PT0S")) + " \t" + DataHelper.INSTANCE.toCalendar("-PT0S"));
        System.out.println("-PT1S:      " + DataHelper.INSTANCE.toDuration(DataHelper.INSTANCE.toCalendar("-PT1S")) + " \t" + DataHelper.INSTANCE.toCalendar("-PT1S"));
        System.out.println("-PT1M:      " + DataHelper.INSTANCE.toDuration(DataHelper.INSTANCE.toCalendar("-PT1M")) + " \t" + DataHelper.INSTANCE.toCalendar("-PT1M"));
        System.out.println("-PT1H:      " + DataHelper.INSTANCE.toDuration(DataHelper.INSTANCE.toCalendar("-PT1H")) + " \t" + DataHelper.INSTANCE.toCalendar("-PT1H"));
        System.out.println("-P1D:       " + DataHelper.INSTANCE.toDuration(DataHelper.INSTANCE.toCalendar("-P1D")) + " \t" + DataHelper.INSTANCE.toCalendar("-P1D"));
        System.out.println("-P1M:       " + DataHelper.INSTANCE.toDuration(DataHelper.INSTANCE.toCalendar("-P1M")) + " \t" + DataHelper.INSTANCE.toCalendar("-P1M"));
        System.out.println("-P1Y:       " + DataHelper.INSTANCE.toDuration(DataHelper.INSTANCE.toCalendar("-P1Y")) + " \t" + DataHelper.INSTANCE.toCalendar("-P1Y"));
        System.out.println("-PT0.1S:    " + DataHelper.INSTANCE.toDuration(DataHelper.INSTANCE.toCalendar("-PT0.1S")) + " \t" + DataHelper.INSTANCE.toCalendar("-PT0.1S"));
        System.out.println("-PT0.01S:   " + DataHelper.INSTANCE.toDuration(DataHelper.INSTANCE.toCalendar("-PT0.01S")) + " \t" + DataHelper.INSTANCE.toCalendar("-PT0.01S"));

        System.out.println("");
        System.out.println("-P10MT1S  :       " + DataHelper.INSTANCE.toDuration(DataHelper.INSTANCE.toCalendar("-P10MT1S")) + " \t" + DataHelper.INSTANCE.toCalendar("-P10MT1S"));
        System.out.println("-P1Y10MT1S:       " + DataHelper.INSTANCE.toDuration(DataHelper.INSTANCE.toCalendar("-P1Y10MT1S")) + " \t" + DataHelper.INSTANCE.toCalendar("-P1Y10MT1S"));
        System.out.println("-P2Y10MT1S:       " + DataHelper.INSTANCE.toDuration(DataHelper.INSTANCE.toCalendar("-P2Y10MT1S")) + " \t" + DataHelper.INSTANCE.toCalendar("-P2Y10MT1S"));
        System.out.println("-P3Y10MT1S:       " + DataHelper.INSTANCE.toDuration(DataHelper.INSTANCE.toCalendar("-P3Y10MT1S")) + " \t" + DataHelper.INSTANCE.toCalendar("-P3Y10MT1S"));
        System.out.println("-P4Y10MT1S:       " + DataHelper.INSTANCE.toDuration(DataHelper.INSTANCE.toCalendar("-P4Y10MT1S")) + " \t" + DataHelper.INSTANCE.toCalendar("-P4Y10MT1S"));
        System.out.println("-P5Y10MT1S:       " + DataHelper.INSTANCE.toDuration(DataHelper.INSTANCE.toCalendar("-P5Y10MT1S")) + " \t" + DataHelper.INSTANCE.toCalendar("-P5Y10MT1S"));
        System.out.println("-P6Y10MT1S:       " + DataHelper.INSTANCE.toDuration(DataHelper.INSTANCE.toCalendar("-P6Y10MT1S")) + " \t" + DataHelper.INSTANCE.toCalendar("-P6Y10MT1S"));

        System.out.println("");
        System.out.println("-P1898Y10M0DT1S:    " + DataHelper.INSTANCE.toDuration(DataHelper.INSTANCE.toCalendar("-P1898Y10M0DT1S")) + " \t" + DataHelper.INSTANCE.toCalendar("-P1898Y10M0DT1S"));
        System.out.println("-P1994Y10M0DT1S:    "  + DataHelper.INSTANCE.toDuration(DataHelper.INSTANCE.toCalendar("-P1994Y10M0DT1S")) + " \t" + DataHelper.INSTANCE.toCalendar("-P1994Y10M0DT1S"));
        System.out.println("-P1998Y10M0DT1S:    "  + DataHelper.INSTANCE.toDuration(DataHelper.INSTANCE.toCalendar("-P1998Y10M0DT1S")) + " \t" + DataHelper.INSTANCE.toCalendar("-P1998Y10M0DT1S"));
    }

    private static void testCursor()
        throws IOException
    {
/*        EnvelopeDocument soapDoc = EnvelopeDocument.Factory.newInstance();
        Envelope soapEnv = soapDoc.addNewEnvelope();
        Body soapBody = soapEnv.addNewBody();

        RequestDocument doc = RequestDocument.Factory.newInstance();
        Request request = doc.addNewRequest();

        soapBody.set(doc);
        request = (Request)soapBody.selectChildren("example/soap", "request")[0];

        request.setRequestElement(new GDate(Calendar.getInstance()).toString());
        request.setRequestAttribute("REQ123");
*/

//        XmlCursor cursor = soapBody.newCursor();
//        cursor.toNextToken();
//
//        // (A) if not wrapping in dummy doc/element, results in output ...<soapenv:Body><urn:AttributeQuery...
//
//        XmlCursor reqcursor = doc.newCursor(); // typs = STARTDOC
//        //reqcursor.toFirstChild(); // (B) this results in output ...<soapenv:Body><urn:AttributeQuery...
//        reqcursor.moveXmlContents(cursor);
//
//        reqcursor.dispose();
//        cursor.dispose();

//        Request.type.getName().getNamespaceURI();
//        request.schemaType().getName().getNamespaceURI();
//        XmlCursor xc = request.newCursor();
//        xc.getName().getNamespaceURI();

/*        XmlOptions opts = new XmlOptions();
        opts.setSavePrettyPrint();
        soapDoc.save(System.out, opts);
*/    }

    private static void testXpath()
        throws XPath.XPathCompileException, FileNotFoundException
    {
        String xml = "<root>" +
            "<a>" +
            "  <b>" +
            "    <c at1='val_at1' at2='val_at2'>c_val1</c>" +
            "    <c>c_val2</c>" +
            "    <d>d_val</d>" +
            "    <c>c_val3</c>" +
            "  </b>" +
            "  <e>e_val</e>" +
            "  <c>c_val4</c>" +
            "</a></root>";
        XMLDocument xmlDoc = XMLHelper.INSTANCE.load(xml);
        DataObject rootObj = xmlDoc.getRootObject();

        String exp = "/root/a/b/c";
        xp(exp, rootObj);
//
//        xp("//c", rootObj);
//
//        xp("//e", rootObj);
//
//        xp("//c | //e", rootObj);
//
//        xp("/root/a/b/c", rootObj);
//
//        xp("//a/c", rootObj);
//        xp("//a/b/c", rootObj);
//
//
//        xp("//e | //d", rootObj);
//
//        DataObject a = rootObj.getRootObject().getDataObject("a.0/b.0");
//        //printDO(a);
//        xp(".//c", a);
//        xp("//c" , a);
//        xp("//c/@at1" , a);
//
//        xml = "<root xmlns='simpleTypeTest'>" +
//            "<a>" +
//              "<b>" +
//                "<c at1='val_at1' at2='val_at2'>c_val1</c>" +
//                "<c>c_val2</c>" +
//                "<d>d_val</d>" +
//                "<c>c_val3</c>" +
//              "</b>" +
//              "<e>e_val</e>" +
//              "<c>c_val4</c>" +
//            "</a></root>";
//        XSDHelper.INSTANCE.define(new FileInputStream(new File("simpleType.xsd")), ".");
//        XMLDocument doc = XMLHelper.INSTANCE.load(xml);
//        rootObj = doc.getRootObject();
//        printDO("\t", rootObj);
//
//        xp("/root/a/b/c", rootObj);
//        xp("//a/b/c", rootObj);
//        xp("/root/a/b/c", rootObj);
//        xp("declare default element namespace \"simpleTypeTest\"; /root/a/b/c", rootObj);
//        xp("declare namespace p=\"simpleTypeTest\"; /p:root/a/b/c", rootObj);
//        xp("declare namespace p=\"simpleTypeTest\"; /p:root/a/b/c/@at1", rootObj);

        xp("declare namespace sdo='sdo'; declare namespace com='com'; /sdo:datagraph/com:company/departments[1]", rootObj);

        /* NOTE: The location path //para[1] does not mean the same as the location path /descendant::para[1].
                 The latter selects the first descendant para element; the former selects all descendant para
                 elements that are the first para children of their parents.*/
        xp("//c[1]", rootObj);
        xp("//c[position()=1]", rootObj);
    }

    private static void xp(String exp, DataObject dataObject)
        throws XPath.XPathCompileException
    {
        System.out.println("\n" + exp + " : " );
        XPath xpplan = XPath.compile(exp);
        System.out.println(exp + " : " + xpplan);
        XPath.Selection s = XPath.execute(xpplan, (DataObjectXML)dataObject);
        for (int i = 0; s.hasNext(); i++)
        {
            Object v = s.getValue();
            System.out.println("   v[" + i + "]: " + v + "\t\t" + s.getPropertyXML());
            s.next();
        }
    }

    public static class MyBoolean
    {
        boolean val;
        public MyBoolean(String text)
        {
            if (text!=null && "myTrue".equals(text))
                val = true;
            else
                val = false;
        }

        public String toString()
        {
            return val ? "myTrue" : "myFalse";
        }
    }

    static void testMetadataOnTypeAndProperty()
    {
        final String OPEN_PROP_NAME = "myOpenProp";
        final String OPEN_PROP_URI  = "myOpenProp_Uri";
        DataObject openPropDefinition = DataFactory.INSTANCE.create("commonj.sdo", "Property");
        openPropDefinition.set(BuiltInTypeSystem.P_PROPERTY_NAME, OPEN_PROP_NAME);
        openPropDefinition.set(BuiltInTypeSystem.P_PROPERTY_TYPE, BuiltInTypeSystem.STRING);
        Property openProperty = TypeHelper.INSTANCE.defineOpenContentProperty(OPEN_PROP_URI, openPropDefinition);

        // Create a new Type and with an open content property set
        DataObject myTypeDefinition = DataFactory.INSTANCE.create("commonj.sdo", "Type");
        myTypeDefinition.set("name", "MyType");
        myTypeDefinition.set("uri", "uri");

        Property openContentProperty = TypeHelper.INSTANCE.getOpenContentProperty(OPEN_PROP_URI, OPEN_PROP_NAME);
        myTypeDefinition.set(openContentProperty, "myValueOnType");

        DataObject myPropDefinition = myTypeDefinition.createDataObject("property");
        myPropDefinition.set("name", "myProperty");
        myPropDefinition.set("type", BuiltInTypeSystem.STRING);
        myPropDefinition.set("containingType", myTypeDefinition);
        myPropDefinition.set(openContentProperty, "myValueOnProperty");

        DataObject myType2Definition = DataFactory.INSTANCE.create("commonj.sdo", "Type");
        myType2Definition.set("name", "MyBoolean");
        myType2Definition.set("uri", "uri");
        myType2Definition.set("dataType", true);
        myType2Definition.set(BuiltInTypeSystem.P_TYPE_JAVACLASS, MyBoolean.class.getName());

        myPropDefinition = myTypeDefinition.createDataObject("property");
        myPropDefinition.set("name", "myBoolean");
        myPropDefinition.set("type", myType2Definition);
        myPropDefinition.set("containingType", myTypeDefinition);
        myPropDefinition.set(BuiltInTypeSystem.P_PROPERTY_XMLELEMENT, false);

        // Define the Type
        Type definedType = TypeHelper.INSTANCE.define(myTypeDefinition);

        System.out.println(" value on Type    : " + definedType.get(openContentProperty));

        Property p = definedType.getProperty("myProperty");
        System.out.println(" value on Property: " + p.get(openContentProperty));

        p = definedType.getProperty("myBoolean");
        System.out.println(" prop myBoolean - instProp xmlElement: " + p.get(BuiltInTypeSystem.P_PROPERTY_XMLELEMENT));
        System.out.println(" prop myBoolean - type: " + p.getType());
        System.out.println(" prop myBoolean - getType().getInstanceClass(): " + p.getType().getInstanceClass());
        System.out.println(" prop myBoolean - getType().get(JAVACLASS): " + p.getType().get(BuiltInTypeSystem.P_TYPE_JAVACLASS));

        DataObject instance = DataFactory.INSTANCE.create(definedType);
        instance.setString("myBoolean", "myTrue");
        MyBoolean myBool = (MyBoolean)instance.get("myBoolean");
        System.out.println("myBool:" + myBool );
    }

    public static void testCycle()
    {
        DataObject dobj = XMLHelper.INSTANCE.load("<a><b><c><d/></c></b></a>").getRootObject();

        printDO("dboj: ", dobj);

        DataObject b = dobj.getDataObject("b.0");

        printDO("b:    ", b);
        
        DataObject c = b.getDataObject("c.0");

        c.createDataObject("bb");
        c.set("bb", b);

        printDO("dobj: ", dobj);
        printDO("c:    ", c);
    }

    public static void testSubstitusionGroups()
        throws FileNotFoundException, XmlException
    {
        //XSDHelper.INSTANCE.define(new FileInputStream("sg.xsd"), "sg");
    }

    public static void testInstanceClass()
    {
//        String NS = "http://sdo/test/derivation";
//        Type itemType = TypeHelper.INSTANCE.getType(NS, "ItemType");
//
//        System.out.println("  itemType: " + itemType);
//        System.out.println("    itemType.getInstanceClass(): " + itemType.getInstanceClass());
//
//        DataObject item = DataFactory.INSTANCE.create(sdo.test.derivation.ItemType.class);
//        System.out.println("\nitem: " + item);
//        System.out.println("   item instanceof sdo.test.derivation.ItemType: " + (item instanceof sdo.test.derivation.ItemType)); //passes
//        System.out.println("   itemType.isInstance(item)):" + itemType.isInstance(item));
//
//        Type productType = TypeHelper.INSTANCE.getType(NS, "ProductType");
//        System.out.println("\nproductType:" + productType);
//        System.out.println("   productType.getInstanceClass():" + productType.getInstanceClass());
//
//        DataObject product = DataFactory.INSTANCE.create(productType);
//        System.out.println("\nproduct: " + product);
//        System.out.println("    product instanceof sdo.test.derivation.ProductType: " + (product instanceof sdo.test.derivation.ProductType)); //passes
//        System.out.println("    productType.isInstance(product): " + productType.isInstance(product)); // fails
//
//        System.out.println("\n\n");
//        System.out.println("productType.isOpen(): " + productType.isOpen());
//        for (int i = 0; i < productType.getDeclaredProperties().size(); i++)
//        {
//            Property declProp = (Property) productType.getDeclaredProperties().get(i);
//            System.out.println("  decl prop: " + declProp + "\t\tmany:" + declProp.isMany());
//        }
//        for (int i = 0; i < product.getInstanceProperties().size(); i++)
//        {
//            Property instProp = (Property) product.getInstanceProperties().get(i);
//            System.out.println("  inst prop: " + instProp + "\t\tmany:" + instProp.isMany());
//        }
//        System.out.println("product    : " + product);
//        product.set("number", 1);
//        product.set("number", 2);
//        product.set("name", "Joe");
//        product.set("name", "Doe");
//
//        Store s = (Store)product;
//        ListXMLIterator us = s.storeGetUnsequencedXML();
//        while (us.next())
//        {
//            System.out.println("  prop: " + us.getPropertyXML());
//            System.out.println("    val:  " + us.getValue());
//            System.out.println("    pref: " + us.getPrefix());
//            System.out.println("    subs: " + us.getSubstitution());
//        }
    }


    private static final String xml =
        "<?xml version=\"1.0\"?>" +
            "<ld:root xmlns:ld=\"ld:test\">" +
            "  <ld:UPPER_CASE>foo</ld:UPPER_CASE>" +
            "  <ld:lower>bar</ld:lower>" +
            "</ld:root>"
        ;

    private static final String xsd = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>" +
        "<xs:schema targetNamespace=\"ld:test\" elementFormDefault='qualified'" +
        "    xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"" +
        "    xmlns:ceej=\"ld:test\">" +
        "   <xs:element name=\"root\">" +
        "      <xs:complexType>" +
        "         <xs:sequence>" +
        "            <xs:element name=\"UPPER_CASE\" type=\"xs:string\" minOccurs=\"0\"/>" +
        "            <xs:element name=\"lower\" type=\"xs:string\" minOccurs=\"0\"/>" +
        "         </xs:sequence>" +
        "      </xs:complexType>" +
        "   </xs:element>" +
        "</xs:schema>";


    static void testSetUnknown()
    {
        System.err.println("Usage: java TestSDO [ 0 | 1 ]");

        int x = 1;

        if (x == 1)
        {
            XSDHelper xsdhelp = HelperProvider.getXSDHelper();
            System.out.println("loading schema...");
            List<Type> types = xsdhelp.define(xsd);
        }

        XMLHelper helper = HelperProvider.getXMLHelper();
        XMLDocument f = helper.load(xml);
        DataObject dobj = f.getRootObject();
        System.out.println("Loaded XML : " + helper.save(dobj, null, "temp"));

        System.out.println();
        System.out.println("Wrapping with datagraph and enabling logging...");
        final Property p = ((DataObjectXML) dobj).getContainmentPropertyXML();
//         final String localname = p.getName().toLowerCase();
//         final String namespace = p.getContainingType().getURI();
        final String localname = f.getRootElementName();
        final String namespace = f.getRootElementURI();
        DataGraphHelper.wrapWithDataGraph(dobj, namespace, localname);
        dobj.getRootObject().getChangeSummary().beginLogging();
        System.out.println("Resulting datagraph: " + helper.save(dobj.getRootObject(), "commonj.sdo", "datagraph"));

        System.out.println();
        System.out.println("Making a change to an existing element...");
        dobj.setString("lower", "zot");
        System.out.println("Resulting datagraph: " + helper.save(dobj.getRootObject(), "commonj.sdo", "datagraph"));

        System.out.println();
        System.out.println("Making a change to an non-existing element...");
        System.out.println("Note that change is silently ignored!");
        dobj.setString("nobody", "grump");
        System.out.println("Resulting datagraph: " + helper.save(dobj.getRootObject(), "commonj.sdo", "datagraph"));

        System.out.println();
        System.out.println("Trying to read datagraph back in..");
        DataObject dg = helper.load(helper.save(dobj.getRootObject(), "commonj.sdo", "datagraph"))
            .getRootObject();
        ;
        System.out.println("Result: " + helper.save(dg.getRootObject(), "commonj.sdo", "datagraph"));
    }

    static void testPropGlobal()
    {
        //set
        //get
        //seq  - add + get
        //list - add + get
    }

    static void testPropOnDenamd()
    {
        // Set an instance property on an open type DataObject
        DataObject open = DataFactory.INSTANCE.create(getDynamicPOType());
        System.out.println("\nopen:");
        printDO(open);

        //set
        System.out.println("\nopen.set");
        open.set("freshNewOnDemandProp", "a value");
        //get
        System.out.println("\nopen.get(\"freshNewOnDemandProp\"): " + open.get("freshNewOnDemandProp"));
        printDO(open);

        //seq  - add + get
        System.out.println("\n\nSequence operations");
        Sequence seq = open.getSequence();
        System.out.println("  Sequence:");
        for (int i=0; i<seq.size(); i++)
        {
            System.out.println("    prop: " + seq.getProperty(i) + "  val:'" + seq.getValue(i) + "'");
        }

        System.out.println("\nseq.add");
        seq.add("anotherOnDemandProperty", "value-for-anotherOnDemantProperty");
        System.out.println("  Sequence:");
        for (int i=0; i<seq.size(); i++)
        {
            System.out.println("    prop: " + seq.getProperty(i) + "  val:'" + seq.getValue(i) + "'");
        }

        System.out.println("\nseq.move");
        seq.move(0, 1);
        System.out.println("  Sequence:");
        for (int i=0; i<seq.size(); i++)
        {
            System.out.println("    prop: " + seq.getProperty(i) + "  val:'" + seq.getValue(i) + "'");
        }

        System.out.println("\nseq.remove");
        seq.remove(0);
        System.out.println("  Sequence:");
        for (int i=0; i<seq.size(); i++)
        {
            System.out.println("    prop: " + seq.getProperty(i) + "  val:'" + seq.getValue(i) + "'");
        }

        System.out.println("\nseq.remove");
        seq.remove(0);
        System.out.println("  Sequence:");
        for (int i=0; i<seq.size(); i++)
        {
            System.out.println("    prop: " + seq.getProperty(i) + "  val:'" + seq.getValue(i) + "'");
        }

        //list - add + get
        System.out.println("\n\nList operations");
        List l = open.getList("yetAnotherOnDemandProperty");
        System.out.println("open.getList: " + l.size());
        for (int i = 0; i < l.size(); i++)
        {
            System.out.println("    " + i + ": '" + l.get(i) + "'");
        }

        System.out.println("\n  l.add");
        l.add("value_of_yetAnotherOnDemandProperty");
        System.out.println("open.getList: " + l.size());
        for (int i = 0; i < l.size(); i++)
        {
            System.out.println("    " + i + ": '" + l.get(i) + "'");
        }

        System.out.println("\nl.remove");
        l.remove("value_of_yetAnotherOnDemandProperty");
        System.out.println("open.getList: " + l.size());
        for (int i = 0; i < l.size(); i++)
        {
            System.out.println("    " + i + ": '" + l.get(i) + "'");
        }
    }

    static void testStripTrailingZeros()
    {
        BigDecimal s;
        s = new BigDecimal("123.450000");
        strip(s);
        s = new BigDecimal("123.45");
        strip(s);
        s = new BigDecimal("123");
        strip(s);
        s = new BigDecimal("1230000");
        strip(s);
        s = new BigDecimal(".450000");
        strip(s);
        s = new BigDecimal(".0000");
        strip(s);
        s = new BigDecimal("0");
        strip(s);
        s = new BigDecimal("+0");
        strip(s);
        s = new BigDecimal("-0");
        strip(s);
        s = new BigDecimal("0E+7");
        strip(s);
        s = new BigDecimal("0E-10");
        strip(s);
        s = new BigDecimal("0E+10");
        strip(s);
        s = new BigDecimal("1.5E-10");
        strip(s);
        s = new BigDecimal("3.2E+10");
        strip(s);
        s = new BigDecimal("1234.56789E-2");
        strip(s);
        s = new BigDecimal("9876.54321E+2");
        strip(s);
    }

    static void strip(BigDecimal bd)
    {
        System.out.println(" s: '" + bd.stripTrailingZeros().toPlainString() + "' \t\t s_new: '" +
            //GDurationBuilder.stripTrailingZeros(GDurationBuilder.toPlainString(
            bd
            //))
            + "'");
    }

    private static void testContexts()
        throws XmlException
    {
        SDOContext staticContext = SDOContextFactory.getGlobalSDOContext();
        System.out.println("Dump1 staticContext:");
        ((TypeSystemBase)staticContext.getTypeSystem()).dumpWithoutBuiltinTypes();

        SDOContext contextClassLoader = SDOContextFactory.createNewSDOContext(String.class.getClassLoader());

        System.out.println("\n\nDefine POType:");
        Type t = getDynamicPOType();

        System.out.println("\n\nDump2 staticContext:");
        ((TypeSystemBase)staticContext.getTypeSystem()).dumpWithoutBuiltinTypes();
        System.out.println("\n\nDump2 contextClassLoader:");
        ((TypeSystemBase) contextClassLoader.getTypeSystem()).dumpWithoutBuiltinTypes();

        //contextClassLoader.getXMLHelper().load("");
//        System.out.println("\n\nLoad staticContext.getDataFactory().create(sdo.test.derivation.ItemType.class):");
//        DataObject item = staticContext.getDataFactory().create(sdo.test.derivation.ItemType.class);
//        assert item instanceof sdo.test.derivation.ItemType;
//        System.out.println("\n\nDump staticContext:");
//        ((TypeSystemBase)staticContext.getTypeSystem()).dumpWithoutBuiltinTypes();

        System.out.println("\n\nLoad contextClassLoader.getTypeHelper().getType(NS, \"ItemType\"):");
        String NS = "http://sdo/test/derivation";
        Type itemType = contextClassLoader.getTypeHelper().getType(NS, "ItemType");

//        System.out.println("\n\nLoad contextClassLoader.getDataFactory().create(sdo.test.derivation.ItemType.class):");
//        DataObject item1 = contextClassLoader.getDataFactory().create(sdo.test.derivation.ItemType.class);
//        assert item1 instanceof sdo.test.derivation.ItemType;
        System.out.println("\n\nDump3 contextClassLoader:");
        ((TypeSystemBase) contextClassLoader.getTypeSystem()).dumpWithoutBuiltinTypes();

        System.out.println("\n\nDump3 staticContext:");
        ((TypeSystemBase)staticContext.getTypeSystem()).dumpWithoutBuiltinTypes();


        String xsd =
        "<xs:schema targetNamespace='contextsTest' elementFormDefault='qualified'" +
        "    xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"" +
        "    xmlns:ceej='contextsTest'>" +
        "   <xs:element name=\"root\">" +
        "      <xs:complexType>" +
        "         <xs:sequence>" +
        "            <xs:element name='a' type=\"xs:string\" minOccurs=\"0\"/>" +
        "            <xs:element name='b' type=\"xs:string\" minOccurs=\"0\"/>" +
        "         </xs:sequence>" +
        "      </xs:complexType>" +
        "   </xs:element>" +
        "</xs:schema>";

        XmlObject[] xobjs = new XmlObject[] {XmlObject.Factory.parse(xsd)};
        SchemaTypeLoader stl = XmlBeans.loadXsd(xobjs);
        SDOContext contextSTL = SDOContextFactory.createNewSDOContext(stl, null);
        System.out.println("\n\nDump3 contextSTL:");
        ((TypeSystemBase) contextSTL.getTypeSystem()).dumpWithoutBuiltinTypes();

        XMLDocument xDoc = contextSTL.getXMLHelper().load("<xs:schema targetNamespace=\"ld:test\" elementFormDefault='qualified'" +
        "    xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"" +
        "    xmlns:ceej=\"ld:test\">" +
        "   <xs:element name=\"root\">" +
        "      <xs:complexType>" +
        "         <xs:sequence>" +
        "            <xs:element name=\"UPPER_CASE\" type=\"xs:string\" minOccurs=\"0\"/>" +
        "            <xs:element name=\"lower\" type=\"xs:string\" minOccurs=\"0\"/>" +
        "         </xs:sequence>" +
        "      </xs:complexType>" +
        "   </xs:element>" +
        "</xs:schema>");

        System.out.println("\n\nDump4 contextClassLoader:");
        ((TypeSystemBase) contextClassLoader.getTypeSystem()).dumpWithoutBuiltinTypes();

        System.out.println("\n\nDump4 staticContext:");
        ((TypeSystemBase)staticContext.getTypeSystem()).dumpWithoutBuiltinTypes();

        System.out.println("\n\nDump4 contextSTL:");
        ((TypeSystemBase) contextSTL.getTypeSystem()).dumpWithoutBuiltinTypes();
    }

    static void testCDATAOld()
        throws XmlException, IOException
    {
        String origxml = "<Doc><a><![CDATA[<b>test</b>]]></a><c><![CDATA[test2]]></c></Doc>";
        System.out.println("This is the input xml string.\n" + origxml);
        XmlOptions xo = new XmlOptions();
        xo.setSaveCDataEntityCountThreshold(-1);
        xo.setSaveCDataLengthThreshold(0);
        //xo.setSavePrettyPrint();
        XmlObject xbean = XmlObject.Factory.parse(origxml);
        System.out.println("This is the xbean output string.\n" + xbean.xmlText(xo));
        //xbean.save(System.out, xo);

        System.out.println("\n\nComment: ");
        String fooXml = "<root><!-- comment --><baz>a</baz></root>";

        XmlObject xObj = XmlObject.Factory.parse(fooXml);
        System.out.println("1 with comment:\n" + xObj.xmlText());

        XmlOptions xo1 = new XmlOptions();
        xo1.setLoadStripComments();
        xObj = XmlObject.Factory.parse(fooXml, xo1);
        System.out.println("2 without comment:\n" + xObj.xmlText());

        XmlCursor xc = xObj.newCursor();
        xc.toFirstChild();
        xc.toNextToken();
        xc.insertComment("this is a newly inserted comment using XMLCursor");
        System.out.println("3 with comment:\n" + xObj.xmlText());
        xc.insertComment("another comment inserted with an XMLCursor");
        System.out.println("4 with two comments:\n" + xObj.xmlText());
        xc.toPrevToken();
        xc.toPrevToken();
        xc.removeXml();
        System.out.println("5 with one comment:\n" + xObj.xmlText());

        System.out.println("Dump");
        XmlCursor cursor = xObj.newCursor();
        cursor.dump();

        ((XmlObjectBase)xObj).dump();

        cursor.dispose();
        xc.dispose();

        ((XmlObjectBase)xObj).dump();
                
    }

    static void testCDATAold2()
        throws XmlException, IOException
    {
        File f = new File("./mytest/CDATA/CurrencyConvertor.wsdl");
        XmlOptions xo = new XmlOptions();
        xo.setSaveCDataEntityCountThreshold(-1);
        xo.setSaveCDataLengthThreshold(0);
        //xo.setSavePrettyPrint();

        XmlObject xbean = XmlObject.Factory.parse(f);
        System.out.println("\n\n--------------------" +
            "This is the xbean output string.\n" + xbean.xmlText(xo));
        System.out.println("\n\n-------------------- save(System.out)\n\n");
        xbean.save(System.out, xo);

        System.out.println("\n\n--------------------\n\n");
        System.out.println("Reading file " + f.getAbsolutePath());
        System.out.println("Size is " + f.length());
        XmlObject sd = XmlObject.Factory.parse(f);

        f = new File("./mytest/CDATA/sd-string.xml");
        FileOutputStream fos = new FileOutputStream(f);
        fos.write(sd.toString().getBytes());
        fos.close();
        System.out.println("Written the toString output to " + f.getAbsolutePath());
        System.out.println("Size is " + f.length());

        f = new File("./mytest/CDATA/sd-InputStream.xml");
        fos = new FileOutputStream(f);
        XmlOptions options = new XmlOptions().setSavePrettyPrint().setSaveAggressiveNamespaces();
        InputStream is = sd.newInputStream(options);

        int read, length = 0;
        byte[] bytes = new byte[1000];
        while(true)
        {
            read = is.read(bytes, 0, 1000);
            if(read == -1) break;
            length += read;
            fos.write(bytes, 0, read);
        }
        fos.close();
        System.out.println("Written the newInputStream output to " + f.getAbsolutePath());
        System.out.println("Size is " + f.length() + "  length: " + length);



//        System.out.println("\n\nComment: ");
//        String fooXml = "<root><!-- comment --><baz>a</baz></root>";
//
//        XmlObject xObj = XmlObject.Factory.parse(fooXml);
//        System.out.println("1 with comment:\n" + xObj.xmlText());
//
//        XmlOptions xo1 = new XmlOptions();
//        xo1.setLoadStripComments();
//        xObj = XmlObject.Factory.parse(fooXml, xo1);
//        System.out.println("2 without comment:\n" + xObj.xmlText());
//
//        XmlCursor xc = xObj.newCursor();
//        xc.toFirstChild();
//        xc.toNextToken();
//        xc.insertComment("this is a newly inserted comment using XMLCursor");
//        System.out.println("3 with comment:\n" + xObj.xmlText());
//        xc.insertComment("another comment inserted with an XMLCursor");
//        System.out.println("4 with two comments:\n" + xObj.xmlText());
//        xc.toPrevToken();
//        xc.toPrevToken();
//        xc.removeXml();
//        System.out.println("5 with one comment:\n" + xObj.xmlText());
//
//        System.out.println("Dump");
//        XmlCursor cursor = xObj.newCursor();
//        cursor.dump();
//
//        ((XmlObjectBase)xObj).dump();
//
//        cursor.dispose();
//        xc.dispose();
//
//        ((XmlObjectBase)xObj).dump();
//
    }

    static void testCDATA()
        throws XmlException, IOException
    {
        GDateBuilder gdb = new GDateBuilder("1982-08-15T12:00:00.000");
        System.out.println("  toS: " + gdb.toGDate() );
        System.out.println("  can: " + gdb.canonicalString() );

        File f = new File("./mytest/CDATA/CurrencyConvertor.wsdl");
        XmlOptions xo = new XmlOptions();
        xo.setSaveCDataEntityCountThreshold(10000);
        xo.setSaveCDataLengthThreshold(1000);
        XmlObject xbean = XmlObject.Factory.parse(f);
        System.out.println("\n\n--------------------" +
            "This is the xbean output string.\n" + xbean.xmlText(xo));
        System.out.println("\n\n-------------------- save(System.out)\n\n");

        xbean.save(System.out, xo);

        System.out.println("\n\n--------------------\n\n");
        System.out.println("Reading file " + f.getAbsolutePath());
        System.out.println("Size is " + f.length());

        XmlObject sd = XmlObject.Factory.parse(f);

        f = new File("./mytest/CDATA/sd-string.xml");
        FileOutputStream fos = new FileOutputStream(f);
        fos.write(sd.toString().getBytes());
        fos.close();
        System.out.println("Written the toString output to " + f.getAbsolutePath());
        System.out.println("Size is " + f.length());

        f = new File("./mytest/CDATA/sd-InputStream.xml");
        fos = new FileOutputStream(f);
        XmlOptions options = new XmlOptions().setSavePrettyPrint();//.setSaveAggressiveNamespaces();
        InputStream is = sd.newInputStream(options);

        int read, length = 0;
        byte[] bytes = new byte[1000];
        while(true)
        {
            read = is.read(bytes, 0, 1000);
            if(read == -1) break;
            length += read;
            fos.write(bytes, 0, read);
        }
        fos.close();
        System.out.println("Written the newInputStream output to " + f.getAbsolutePath());
        System.out.println("Size is " + f.length() + "  length: " + length);

        XmlObject sd2 = XmlObject.Factory.parse(f);
    }

    static void testTypeHelperGetClass()
        throws XmlException
    {
        //Type prType = TypeHelper.INSTANCE.getType(ProductType.class);
        //System.out.println("Type: " + prType);
    }

    static void testEmptyContext()
    {
        SDOContext context = SDOContextFactory.createNewSDOContext();

        Type t = context.getTypeHelper().getType("uri","no_such_type");

        System.out.println("Type: " + t);

        TypeSystem ts = context.getTypeSystem();
        ((TypeSystemBase)ts).dumpWithoutBuiltinTypes();
    }

    static void testXbXPath()
        throws XmlException
    {
        XmlBeans.compilePath("fn:string('.')");
        //XmlBeans.compileQuery("<myxml><foo/></myxml>");

        XmlObject xo = XmlObject.Factory.parse("<abc>\n" +
            " <tag1>TAG1</tag1>\n" +
            " <tag1>TAG2</tag1>\n" +
            " <tag1>TAG3</tag1>\n" +
            "</abc> ");

        XmlObject[] rezs;
        String path;

//        path = "./node()";
//        System.out.println("\nPath:\t" + path);
//        rezs = xo.selectPath(path);
//        for (int i = 0; i < rezs.length; i++)
//        {
//            XmlObject rez = rezs[i];
//            System.out.println("" + i  + ":\t" + rez);
//        }
//
//        path = "/node()";
//        System.out.println("\nPath:\t" + path);
//        rezs = xo.selectPath(path);
//        for (int i = 0; i < rezs.length; i++)
//        {
//            XmlObject rez = rezs[i];
//            System.out.println("" + i  + ":\t" + rez);
//        }
//
//        path = "./abc/node()";
//        System.out.println("\nPath:\t" + path);
//        rezs = xo.selectPath(path);
//        for (int i = 0; i < rezs.length; i++)
//        {
//            XmlObject rez = rezs[i];
//            System.out.println("" + i  + ":\t" + rez);
//        }

//        path = "/abc/node()";
//        System.out.println("\nPath:\t" + path);
//        rezs = xo.selectPath(path);
//        for (int i = 0; i < rezs.length; i++)
//        {
//            XmlObject rez = rezs[i];
//            System.out.println("" + i  + ":\t" + rez);
//        }
    }

    static void testSDOList()
    {
        TypeHelper typeHelper = TypeHelper.INSTANCE;
        DataFactory factory = DataFactory.INSTANCE;

        Type stringType = typeHelper.getType("commonj.sdo", "String");

        DataObject zType = factory.create("commonj.sdo", "Type");
        zType.set("uri", "example.com/test");
        zType.set("name", "Z");
        zType.set("open", true);

        DataObject zProperty = zType.createDataObject("property");
        zProperty.set("name", "z");
        zProperty.set("type", stringType);
        zProperty.set("many", true);

        Type t = typeHelper.define(zType);
        DataObject dobj = factory.create(t);

        List list = dobj.getList("z");

        printDO(dobj);

        list.add("first");
        list.add("second");
//        list.set(1, null);
        dobj.set("z[2]", "third");

        System.out.println("\ntestSDOList: " + list);


        List list2 = dobj.getList("w");

        printDO(dobj);

        list2.add("wfirst");
        list2.add("wsecond");
//        list.set(1, null);
        dobj.set("w[2]", "wthird");

        System.out.println("testSDOList: " + list2);
        printDO(dobj);
        
    }

    static class DO
    {
        public <T>T get(Class<T> paramType)
        {
            if (paramType==String.class)
                return (T)"string value";
            if (paramType==int.class)
                return (T)new Integer(1);

            return null;
        }

        /** @deprecated */
        public String getString()
        {
            return get(String.class);
        }
    }

    static void testParamTypes()
        throws ClassNotFoundException
    {
        DO dobj = new DO();
        System.out.println("  dobj.get(String.class): " + dobj.get(String.class));
        System.out.println("  dobj.getString(): " + dobj.getString());
        System.out.println("  dobj.get(int.class): " + dobj.get(int.class));
        System.out.println("  dobj.get(Object.class): " + dobj.get(Object.class));
    }

    static void testXbSave()
        throws XmlException, XMLStreamException, org.apache.xmlbeans.xml.stream.XMLStreamException
    {
        String xml = "<s0:definitions name='SimpleSoap12ImplServiceDefinitions' targetNamespace='http:\n" +
            "//example.org' xmlns:s0='http://schemas.xmlsoap.org/wsdl/' xmlns:s1='http://exam ple.org' xmlns:s2='http://schemas.xmlsoap.org/wsdl/soap12/'>\n" +
            "  <s0:types>\n" + "    <xs:schema attributeFormDefault='unqualified' elementFormDefault='qualified'\n" +
            " targetNamespace='http://example.org' xmlns:xs='http://www.w3.org/2001/XMLSchema\n" + "'>\n" +
            "      <xs:element name='sayHello'>\n" + "        <xs:complexType>\n" + "          <xs:sequence>\n" +
            "            <xs:element name='message' type='xs:string'/>\n" + "          </xs:sequence>\n" +
            "        </xs:complexType>\n" + "      </xs:element>\n" + "      <xs:element name='sayHelloResponse'>\n" +
            "        <xs:complexType>\n" + "          <xs:sequence>\n" +
            "            <xs:element name='return' type='xs:string'/>\n" + "          </xs:sequence>\n" +
            "        </xs:complexType>\n" + "      </xs:element>\n" + "    </xs:schema>\n" + "  </s0:types>\n" +
            "  <s0:message name='sayHello'>\n" + "    <s0:part element='s1:sayHello' name='parameters'/>\n" +
            "  </s0:message>\n" + "  <s0:message name='sayHelloResponse'>\n" +
            "    <s0:part element='s1:sayHelloResponse' name='parameters'/>\n" + "  </s0:message>\n" +
            "  <s0:portType name='SimpleSoap12'>\n" +
            "    <s0:operation name='sayHello' parameterOrder='parameters'>\n" +
            "      <s0:input message='s1:sayHello'/>\n" + "      <s0:output message='s1:sayHelloResponse'/>\n" +
            "    </s0:operation>\n" + "  </s0:portType>\n" +
            "  <s0:binding name='SimpleSoap12ImplServiceSoapBinding' type='s1:SimpleSoap12'>\n" +
            "    <s2:binding style='document' transport='http://schemas.xmlsoap.org/soap/http\n" + "'/>\n" +
            "    <s0:operation name='sayHello'>\n" +
            "      <s2:operation soapAction='sayHelloAction' style='document'/>\n" + "      <s0:input>\n" +
            "        <s2:body parts='parameters' use='literal'/>\n" + "      </s0:input>\n" + "      <s0:output>\n" +
            "        <s2:body parts='parameters' use='literal'/>\n" + "      </s0:output>\n" + "    </s0:operation>\n" +
            "  </s0:binding>\n" + "  <s0:service name='SimpleSoap12ImplService'>\n" +
            "    <s0:port binding='s1:SimpleSoap12ImplServiceSoapBinding' name='SimpleSoap12S oapPort'>\n" +
            "      <s2:address location='http://localhost:7001/tests/SimpleSoap12Service'/>\n" + "    </s0:port>\n" +
            "  </s0:service>\n" + "</s0:definitions>";
        XmlObject xo = XmlObject.Factory.parse(xml);

        System.out.println("XO : " + xo);

//        XMLStreamReader xsr = xo.newXMLStreamReader();
//        while(xsr.hasNext())
//        {
//            if (xsr.isStartElement()) System.out.println(" SE: " + xsr.getName());
//            else if (xsr.isEndElement()) System.out.println(" EE: " + xsr.getName());
//            else if (xsr.isCharacters()) System.out.println(" CH:  " + xsr.getText());
//
//            xsr.next();
//        }

//        XMLInputStream xis = xo.newXMLInputStream();
//        while(xis.hasNext())
//        {
//            XMLEvent xe = xis.next();
//
//            if (xe.isStartElement()) System.out.println(" SE: " + xe.getName());
//            else if (xe.isEndElement()) System.out.println(" EE: " + xe.getName());
//            else if (xe.isCharacterData())
//            {
//                CharacterData cd = (CharacterData)xe;
//                System.out.println(" CH:  " + cd.getContent());
//            }
//        }

    }

    static void testGetMixedText()
        throws XmlException
    {
        XmlObject xo = XmlObject.Factory.parse("<Descriptions>\n" +
            "      <Description Role=\"Summary\">\n" +
            "          <Title>Title text</Title>\n" +
            "          How do I acces here ??\n" +
            "          <Media>\n" +
            "            <URL>http://www.google.com</URL>\n" + "\n" +
            "          </Media>\n" +
            "      </Description>\n" +
            "</Descriptions>");

        XmlCursor cursor = xo.newCursor();

        cursor.toFirstChild();
        System.out.println(" c-> " + cursor.getName());
        cursor.toFirstChild();
        System.out.println(" c-> " + cursor.getName());


        if (!cursor.toNextToken().isNone())
        {
            do
            {
                System.out.println(" c-> " + cursor.currentTokenType());
                if (cursor.isStart())
                    cursor.toEndToken();
                else if (cursor.isText())
                    System.out.println("Text " + cursor.getTextValue());
            }
            while(!cursor.toNextToken().isNone());
        }
    }

    private static void testSDOContextOnSTL()
    {
        SDOContext sdoContext = SDOContextFactory.createNewSDOContext(BuiltinSchemaTypeSystem.get());
        ((TypeSystemBase)sdoContext.getTypeSystem()).dump();


    }

    private static void testGMonth()
    {
        String txt = "--01";
        GDate gMonth = XsTypeConverter.lexGDate(txt);
        System.out.println(txt + " : " + gMonth + "   " + gMonth.canonicalString());

        txt = "--02--";
        gMonth = XsTypeConverter.lexGDate(txt);
        System.out.println(txt + " : " + gMonth + "   " + gMonth.canonicalString());
    }

    private static void testDefaultInt()
    {
        String schema = "<xsd:schema\n" + "  xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"\n" +
            "  xmlns:tns=\"tns\"\n" + "  targetNamespace=\"tns\" elementFormDefault='qualified'>\n" + "\n" +
            "  <xsd:element name=\"a\" type=\"tns:Complex\"/>" +
            "  <xsd:complexType name=\"Complex\">" +
            "    <xsd:sequence>\n" +
            "      <xsd:element name=\"int\" type=\"xsd:int\" />\n" +
            "    </xsd:sequence>\n" +
            "  </xsd:complexType>" +
            "</xsd:schema>";
        List l = XSDHelper.INSTANCE.define(schema);

        DataObject dobj = DataFactory.INSTANCE.create((Type)l.get(0));


        XMLDocument xdoc = XMLHelper.INSTANCE.load("<a xmlns='tns'><int></int></a>");
        dobj = xdoc.getRootObject();
        System.out.println("  int prop is set? : " + dobj.isSet("int"));
        System.out.println("  int prop value   : " + dobj.get("int"));

        printDO(dobj);


        System.out.println("  " + XMLHelper.INSTANCE.save(dobj, "uri", "out"));
    }

    private static void testModifyQName()
        throws XmlException
    {
        XmlObject xo = XmlObject.Factory.parse("<p1:a xmlns:p1='p1_uri'><p1:b>p1:qname</p1:b></p1:a>");

        XmlObject axo = xo.selectChildren("p1_uri", "a")[0];
        XmlCursor xc = axo.newCursor();
        xc.setName(new QName("p2_uri", "a"));
        System.out.println("  toNextToken: " + xc.toNextToken());
        System.out.println("  isNamesp: " + xc.isNamespace());
        if (xc.isNamespace())
            xc.setTextValue("some_other_uri");

        System.out.println("Case 1: \n" + xo.xmlText());

        System.out.println("\nCase 1.1: \n" + xo.xmlText(new XmlOptions().setSaveAggressiveNamespaces()));



        XmlObject xo2 = XmlObject.Factory.newInstance();
        XmlCursor xc2 = xo2.newCursor();
        xc2.toFirstContentToken();
        xc2.beginElement("a", "p1_uri");
        xc2.insertElementWithText("b", "p1_uri", "p1:qname");

        System.out.println("\nCase 2: original\n" + xo2);

        axo = xo2.selectChildren("p1_uri", "a")[0];
        xc = axo.newCursor();
        xc.setName(new QName("p2_uri", "a"));

        System.out.println("\nCase 2: modified" + xo2.xmlText());
    }

    private static void testPI()
        throws XmlException
    {
        XmlObject xo = XmlObject.Factory.parse("<?pi pitext?><a><?pi2 pi2text?></a>");
        System.out.println("xo:\n" + xo);
        System.out.println("\nxo.xmlText():\n" + xo.xmlText());
    }

    private static void testXBSaver()
        throws XmlException
    {
        XmlObject xo = XmlObject.Factory.parse("<a></a>");
        System.out.println("xo:\n" + xo.xmlText());

        XmlCursor xc = xo.newCursor();
        xc.toFirstContentToken();
        xc.toFirstContentToken();

        for (int i=0; i<4096; i++)
        {
            xc.insertChars("<");
            System.out.println("\n" + i + "\txo.xmlText():\n" + xo.xmlText());
        }

        xc.insertChars("<");
        System.out.println("\n" + "\txo.xmlText():\n" + xo.xmlText());
    }

    private static void testSDOXPath()
        throws XPath.XPathCompileException, IOException
    {
        XPath xp;
        String path;

//        path = "/a/b/c";
//        xp = XPath.compile(path);
//        System.out.println("1: " + path + " : " + xp);
//
//        path = "/a/b/c[ a = '1' ]";
//        xp = XPath.compile(path);
//        System.out.println("2: " + path + " : " + xp);
//
//        path = "/a/b[b='2']/c";
//        xp = XPath.compile(path);
//        System.out.println("3: " + path + " : " + xp);
//
//        path = "/a/b[c='3'] [d='4']/c";
//        xp = XPath.compile(path);
//        System.out.println("4: " + path + " : " + xp);
//
//        path = "/a/b[@e='3']/c";
//        xp = XPath.compile(path);
//        System.out.println("5: " + path + " : " + xp);
//
//        path = "/a/b[child :: f='3'][attribute  :: d='4']/c";
//        xp = XPath.compile(path);
//        System.out.println("6: " + path + " : " + xp);
//
//        path = "/a/b[child = '3' ][attribute  ='4']/c";
//        xp = XPath.compile(path);
//        System.out.println("7: " + path + " : " + xp);

        DataObject dobj;

//        dobj = XMLHelper.INSTANCE.load("<a><b>t1</b><b c='1' d='2'>t2</b><b c='1' d='4'>t3</b><b d='2'>t4</b></a>").getRootObject();
//        path = "/a/b[ @c='1' ][ @d='4' ]";
//        xp = XPath.compile(path);
//        System.out.println("7: " + path + " : " + xp);
//        printSelection(XPath.execute(xp, (DataObjectXML)dobj));

//        dobj = XMLHelper.INSTANCE.load("<a>" +
//            "<b>   <c>1</c> <d>4</d>   t3</b>" +
//            "<b>t1</b>" +
//            "<b>t2</b>" +
//            "<b>   <d>2</d>           t4</b>" +
//            "</a>").getRootObject();
//        path = "/a/b[2]";
//        xp = XPath.compile(path);
//        System.out.println("8: " + path + " : " + xp);
//        printSelection(XPath.execute(xp, (DataObjectXML)dobj));

//        DataObject b3 = dobj.getDataObject("b.3");
//        printDO(b3);

//        xp = XPath.compile("$this");
//        printSelection(XPath.execute(xp, (DataObjectXML)b3));

//        path = "/a";
//        xp = XPath.compile(path);
//        System.out.println("9: " + path + " : " + xp);
//        printSelection(XPath.execute(xp, (DataObjectXML)b3));

        String xml = "<customer ssn=\"XXX-XX-XXX\">\n" +
              "<name>\n" +
                "<first>Alfred</first>\n" +
                "<last>Ellison</last>\n" +
              "</name>\n" +
              "<address>\n<city>San Jose</city>\n</address>\n" +
              "<address>\n<city>Seattle</city>\n</address>\n" +
              "<orders>\n" +
                "<order ref=\"1\"/>\n" +
                "<order ref=\"2\"/>\n" +
              "</orders>\n" + 
            "</customer>";

        path = //"orders/order[2]";
            "address[2]";
        xp = XPath.compile(path);
        System.out.println("9: " + path + " : " + xp);
        dobj = XMLHelper.INSTANCE.load(xml).getRootObject();
        printSelection(XPath.execute(xp, (DataObjectXML)dobj));
    }

    static private void printSelection(XPath.Selection sel)
    {
        int i = 0;
        while(sel.hasNext())
        {
            Object res = sel.next();
            i++;
            if (res instanceof DataObject)
            {
                System.out.print(" #" + i + " ");
                printDO("    -> ", (DataObject)res );
            }
            else
                System.out.println(" #" + i+ " " + res );
        }
    }

    static void testOnDemandCycle()
    {
        DataObject dobj = XMLHelper.INSTANCE.load("<a><b><c><d/></c></b></a>").getRootObject();
        DataObject b = dobj.getDataObject("b.0");
        DataObject c = b.getDataObject("c.0");
        try
        {
            // TODO this test should work without the following create line CR307056
            //c.createDataObject("bb");
            c.set("bb", b);
            assert false : "set should have failed due to circular containment";
        }
        catch (Exception e)
        {
            e.printStackTrace();
            assert e instanceof IllegalArgumentException;
            String msg = e.getMessage();
            assert (msg.indexOf("Circular containment") >= 0) ||
                       (msg.indexOf("circular containment") >= 0);
        }
    }

    static void testXbGetSourceName()
        throws IOException
    {
        SchemaType st = SchemaDocument.type;
        InputStream is = st.getTypeSystem().getSourceAsStream(st.getSourceName());
        Reader r = new InputStreamReader(is);
        Writer w = new PrintWriter(System.out);
        char[] buf = new char[100];
        int l;
        while((l=r.read(buf))>=0)
        {
            w.write(buf, 0, l);
        }
        w.close();
        r.close();
        is.close();
    }

    static void testSingleGetSetOnManyProp()
    {
        Type poType = getDynamicPOType();

        System.out.println("Prop: " + poType.getProperty("itemNo"));
        System.out.println("Prop: " + poType.getProperty("itemName"));

        DataObject po = DataFactory.INSTANCE.create(poType);

        System.out.println("po.getInt(\"itemNo\") : " + po.getInt("itemNo"));
        System.out.println("po.getString(\"itemName\") : " + po.getString("itemName"));

        po.setInt("itemNo", 5);
        po.setString("itemName", "socks");

        System.out.println("po.getInt(\"itemNo\") : " + po.getInt("itemNo"));
        System.out.println("po.getString(\"itemName\") : " + po.getString("itemName"));
    }

    static void testSdoTsLoad()
    {
        SDOContext sdoContext = SDOContextFactory.getGlobalSDOContext();
        TypeXML type = sdoContext.getBindingSystem().loadTypeBySchemaTypeName("http://www.w3.org/2004/07/xpath-datatypes",
                "yearMonthDuration");
        System.out.println("Type: " + type);
    }

    static void testXbCDATA()
            throws XmlException
    {
        String xmlText = /*"<a>\n" +
                "<a><![CDATA[cdata text]]></a>\n" +
                "<b><![CDATA[cdata text]]> regular text</b>\n" +
                "<c>text <![CDATA[cdata text]]></c>\n" +
                "</a>"; */
                "<ns1:MarksAndNumber xmlns:ns1='ns1'>\n" +
                        " <![CDATA[\n" +
                        "\n" +
                        "\"NOT AVAILABLE\"\n" +
                        "!@#$!@#$!@#$$%^&$&*&*()&()&()L:|OP{}O}{<>?\n" +
                        "\n" +
                        "  ]]>\n" +
                        " </ns1:MarksAndNumber>";
        System.out.println(xmlText);

        XmlOptions opts = new XmlOptions();
        //opts.setUseCDataBookmarks();
        
        XmlObject xo = XmlObject.Factory.parse( xmlText , opts);

        System.out.println("xo1:\n" + xo.xmlText(opts));
        System.out.println("\n");

        opts.setSavePrettyPrint();
        System.out.println("xo2:\n" + xo.xmlText(opts));
    }

    static void testXbPath()
        throws XmlException
    {
//        final XmlObject obj = XmlObject.Factory.parse("<a><b><c>val1</c><d><c>val2</c></d></b><c>val3</c></a>");
//        final XmlCursor c = obj.newCursor();
//
//        c.selectPath(".//b/c");
//
//        System.out.println(c.getSelectionCount());
//
//        while ( c.hasNextSelection() )
//        {
//            c.toNextSelection();
//            System.out.println(" -> " + c.getObject());
//        }
//        c.dispose();

        String schema = "<xsd:schema xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" \n" +
            "  xmlns:tns=\"tns\"\n" + "  targetNamespace=\"tns\" elementFormDefault='qualified'>\n" + 
            "  <xsd:element name=\"a\" >" +
            "    <xsd:complexType>" +
            "     <xsd:sequence>\n" +
            "      <xsd:element name=\"int\" type=\"xsd:int\" />\n" +
            "     </xsd:sequence>\n" +
            "   </xsd:complexType>" +
            "  </xsd:element>" +
            "</xsd:schema>";
       List l = XSDHelper.INSTANCE.define(schema);

       System.out.println("l: " + l);
    }

    static void testComplexElementWithSimpleContent() 
    {
        String xml = "<root><name lang=\"en_US\">Adam</name></root>";
        XMLDocument doc = XMLHelper.INSTANCE.load( xml );
        DataObject root = doc.getRootObject();
        Property nameProperty = root.getInstanceProperty( "name" );

        assert "commonj.sdo".equals( nameProperty.getType().getURI() );
        //assert "DataObject".equals( nameProperty.getType().getName() );

        DataObject dobj = root.getDataObject( "name.0" );
        assert "en_US".equals( dobj.getString( "lang" ) );

        Sequence seq = dobj.getSequence();
        assert "Adam".equals( seq.getValue(0) );
  }
}
