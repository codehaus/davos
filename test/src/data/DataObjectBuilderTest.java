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

import java.io.*;
import java.util.*;

import javax.sdo.*;
import javax.sdo.helper.*;

import davos.sdo.impl.data.DataObjectBuilder;
import davos.sdo.impl.data.DataObjectBuilderImpl;

import junit.framework.Test;
import junit.framework.TestSuite;

import common.BaseTest;

/**
 * @author Wing Yew Poon
 */
public class DataObjectBuilderTest extends BaseTest
{
    public DataObjectBuilderTest(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        /*
        TestSuite suite = new TestSuite();
        suite.addTest(new DataObjectBuilderTest("testDataObjectBuilder"));
        */
        TestSuite suite = new TestSuite(DataObjectBuilderTest.class);
        return suite;
    }

    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(suite());
    }

    public void testDataObjectBuilder()
    {
        DataObjectBuilder l = new DataObjectBuilderImpl(context);
        DataObjectBuilder.Change same = DataObjectBuilder.Change.SAME;
        DataObjectBuilder.Change alt = DataObjectBuilder.Change.OLD;
        DataObjectBuilder.Change neu = DataObjectBuilder.Change.NEW;
        DataObjectBuilder.Kind attr = DataObjectBuilder.Kind.ATTRIBUTE;
        DataObjectBuilder.Kind elem = DataObjectBuilder.Kind.ELEMENT;
        DataObjectBuilder.Kind cont = DataObjectBuilder.Kind.CONTENT;

        l.startElement("commonj.sdo", "datagraph", "sdo", same);
        l.startElement("http://sdo/test/basic", "a", "bas", same);
        l.simpleContent(null, "uri1", null, "http://www.w3.org/2001/XMLSchema#string", elem, same);
        l.simpleContent(null, "boolean0", null, false, elem, neu);
        l.simpleContent(null, "boolean0", null, true, elem, alt);
        l.simpleContent(null, "yearMonthDay", null, "2001-01-01", elem, same);
        l.simpleContent(null, "duration", null, "PT1S", elem, neu);
        l.simpleContent(null, "duration", null, "PT0S", elem, alt);
        l.simpleContent(null, "uri2", null, "http://www.w3.org/2001/XMLSchema#string", elem, neu);
        l.endElement();
        l.endElement();
        DataObject root = l.retrieveRootDataObject();
        assertEquals(false, root.getBoolean("a/boolean0"));
        assertEquals("2001-01-01", root.get("a/yearMonthDay"));
        assertEquals("PT1S", root.get("a/duration"));
        assertEquals("http://www.w3.org/2001/XMLSchema#string", root.get("a/uri1"));
        assertEquals("http://www.w3.org/2001/XMLSchema#string", root.get("a/uri2"));
        ChangeSummary cs = root.getChangeSummary();
        List changelist = cs.getChangedDataObjects();
        assertEquals(1, changelist.size());
        DataObject changed = (DataObject)changelist.get(0);
        List oldlist = cs.getOldValues(changed);
        assertEquals(3, oldlist.size());
        boolean boolean0Changed = false;
        boolean durationChanged = false;
        boolean uri2Changed = false;
        for (Object o : oldlist)
        {
            ChangeSummary.Setting old = (ChangeSummary.Setting)o;
            System.out.println(old);
            Property p = old.getProperty();
            if (p.getName().equals("boolean0"))
            {
                boolean0Changed = true;
                assertTrue(old.isSet());
                assertEquals(Boolean.TRUE, old.getValue());
            }
            if (p.getName().equals("duration"))
            {
                durationChanged = true;
                assertTrue(old.isSet());
                assertEquals("PT0S", old.getValue());
            }
            if (p.getName().equals("uri2"))
            {
                uri2Changed = true;
                assertFalse(old.isSet());
                assertNull(old.getValue());
            }
        }
        assertTrue(boolean0Changed);
        assertTrue(durationChanged);
        assertTrue(uri2Changed);
        Property p = changed.getType().getProperty("uri2");
        ChangeSummary.Setting old = cs.getOldValue(changed, p);
        System.out.println("uri2 was set? " + old.isSet());
        System.out.println("old value of uri2: " + old.getValue());
    }
}
