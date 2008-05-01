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

import common.BaseTest;
import javax.sdo.Type;
import javax.sdo.DataObject;
import javax.sdo.Property;
import javax.sdo.helper.TypeHelper;
import javax.sdo.helper.DataFactory;
import javax.sdo.helper.CopyHelper;

import java.util.List;
import java.util.ArrayList;

/**
 * @author Cezar Andrei (cezar.andrei at bea.com)
 *         Date: Aug 7, 2006
 */
public class ListsTest
    extends BaseTest
{
    public ListsTest(String name)
    {
        super(name);
    }

    private static TypeHelper typeHelper = context.getTypeHelper();
    private static DataFactory dataFactory = context.getDataFactory();
    private static CopyHelper copyHelper = context.getCopyHelper();

    public static void testLists1()
    {
        Type t = typeHelper.getType("http://www.example.com/simple1", "Quote");
        DataObject o = dataFactory.create(t);

        Property quotesProp = t.getProperty("quotes");

        List quotes = o.getList(quotesProp);
        //printList("1  o.getList(quotes): ", quotes);
        assertEquals(0, quotes.size());

        DataObject q1 = o.createDataObject(quotesProp);  q1.set("symbol", "Q1");
        DataObject q2 = o.createDataObject(quotesProp);  q2.set("symbol", "Q2");
        DataObject q3 = o.createDataObject(quotesProp);  q3.set("symbol", "Q3");
        //printList("2  o.getList(quotes): ", quotes);
        assertEquals(3, quotes.size());
        assertEquals("Q1", ((DataObject)quotes.get(0)).get("symbol"));
        assertEquals("Q2", o.get("quotes[2]/symbol"));
        assertEquals("Q3", o.get("quotes.2/symbol"));

        DataObject q4 = copyHelper.copy(q1);
        DataObject q5 = copyHelper.copy(q2);
        DataObject q6 = copyHelper.copy(q3);

        quotes.add(q4);
        quotes.add(q5);
        quotes.add(q6);
        //printList("3  o.getList(quotes): ", quotes);
        assertEquals(6, quotes.size());
        assertEquals("Q1", ((DataObject)quotes.get(3)).get("symbol"));
        assertEquals("Q2", o.get("quotes[5]/symbol"));
        assertEquals("Q3", o.get("quotes.5/symbol"));

        quotes.add(q1); // this first detaches q1 and then adds it (to the end)
        assertEquals(6, quotes.size());
        assertEquals("Q2", ((DataObject)quotes.get(0)).get("symbol"));
        assertEquals("Q3", ((DataObject)quotes.get(1)).get("symbol"));
        assertEquals("Q1", ((DataObject)quotes.get(2)).get("symbol"));
        assertEquals("Q2", ((DataObject)quotes.get(3)).get("symbol"));
        assertEquals("Q3", ((DataObject)quotes.get(4)).get("symbol"));
        assertEquals("Q1", ((DataObject)quotes.get(5)).get("symbol"));

        List q1quotes = q1.getList(quotesProp);
        assertTrue(o == q3.getContainer());
        assertTrue(quotesProp == q3.getContainmentProperty());
        q1quotes.add(q3); // this detaches q3 from o and attaches it to q1
        assertTrue(q1 == q3.getContainer());
        assertTrue(quotesProp == q3.getContainmentProperty());
        //printList("4  q1.getList(quotes): ", q1quotes);
        //printList("4.1            quotes: ", quotes);
        assertEquals(1, q1quotes.size());
        assertEquals(5, quotes.size());
        assertEquals("Q3", ((DataObject)q1quotes.get(0)).get("symbol"));
        assertEquals("Q3", o.get("quotes.4/quotes.0/symbol"));

        q1quotes.remove(q3);
        assertNull(q3.getContainer());
        assertNull(q3.getContainmentProperty());
        //printList("5  q1.getList(quotes): ", q1quotes);
        assertEquals(0, q1quotes.size());
        assertEquals(0, q1.getList(quotesProp).size());

        quotes.remove(4);
        assertNull(q1.getContainer());
        assertNull(q1.getContainmentProperty());
        //printList("6  o.getList(quotes): ", quotes);
        assertEquals(4, quotes.size());
        assertEquals(4, o.getList(quotesProp).size());
        assertEquals("Q2", ((DataObject)quotes.get(0)).get("symbol"));
        assertEquals("Q1", ((DataObject)quotes.get(1)).get("symbol"));
        assertEquals("Q2", ((DataObject)quotes.get(2)).get("symbol"));
        assertEquals("Q3", ((DataObject)quotes.get(3)).get("symbol"));

        quotes.set(0, q3); // this detaches q2 and q3 and attaches q3 at index 0
        assertNull(q2.getContainer());
        assertNull(q2.getContainmentProperty());
        assertTrue(o == q3.getContainer());
        assertTrue(quotesProp == q3.getContainmentProperty());
        //printList("7  o.getList(quotes): ", quotes);
        assertEquals(4, quotes.size());
        assertEquals(4, o.getList(quotesProp).size());
        assertEquals("Q3", o.get("quotes.0/symbol"));
        assertEquals("Q1", o.get("quotes.1/symbol"));
        assertEquals("Q2", o.get("quotes.2/symbol"));
        assertEquals("Q3", o.get("quotes.3/symbol"));
    }

    public static void testLists2()
    {
        Type t = getDynamicPOType();
        DataObject o = dataFactory.create(t);

        Property propItemNo = t.getProperty("itemNo");
        Property propItemName = t.getProperty("itemName");

        //printList("1:  o.getList(propItemNo): ", o.getList(propItemNo));
        assertEquals(0, o.getList(propItemNo).size());
        //printList("2:  o.getList(propItemName): ", o.getList(propItemName));
        assertEquals(0, o.getList(propItemName).size());

        List li = new ArrayList();
        li.add(5);
        li.add(6);
        li.add(7);
        o.set(propItemNo, li);
        //printList("3:  o.getList(propItemNo): ", o.getList(propItemNo));
        assertEquals(3, o.getList(propItemNo).size());
        assertEquals(5, o.getList(propItemNo).get(0));
        assertEquals(6, ((List)o.get("itemNo")).get(1));
        assertEquals(7, o.get("itemNo.2"));
        li.add(8);
        //there are still three - a new list is kept internaly
        assertEquals(3, o.getList(propItemNo).size());

        li.clear();
        li.add("Bill");
        o.set(propItemName, li);
        //printList("4:  o.getList(propItemName): ", o.getList(propItemName));
        assertEquals(1, o.getList(propItemName).size());
        assertEquals("Bill", o.getList(propItemName).get(0));
    }

    static final String PO_URI = "po_uri";

    private static Type getDynamicPOType()
    {
        TypeHelper types = typeHelper;
        Type intType = types.getType("commonj.sdo", "Int");
        Type stringType = types.getType("commonj.sdo", "String");

        // create a new Type for PurchaseOrder
        DataObject poTypeDescriptor = dataFactory.create("commonj.sdo", "Type");
        poTypeDescriptor.set("uri", PO_URI);
        poTypeDescriptor.set("name", "PurchaseOrder");
//        poTypeDescriptor.setBoolean("dataType", false);
        poTypeDescriptor.setBoolean("open", true);
        poTypeDescriptor.setBoolean("sequenced", true);
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


//        System.out.println("Printig dataObject poTypeDescriptor:");
//        printDO(poTypeDescriptor);

        Type t = types.define(poTypeDescriptor);

//        System.out.println("\nt.dump()");
//        ((TypeImpl)t).dump();

        return t;
    }

    private static void printList(String s, List list)
    {
        System.out.println(s + "  size: " + list.size());
        for (Object o : list)
        {
            System.out.println("    " + o);
        }
    }
}
