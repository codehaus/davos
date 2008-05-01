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
import javax.sdo.DataObject;
import javax.sdo.Type;
import javax.sdo.Property;
import javax.sdo.helper.DataFactory;
import javax.sdo.helper.TypeHelper;

import java.util.List;

/**
 * @author Cezar Andrei (cezar.andrei at bea.com)
 *         Date: Aug 11, 2006
 */
public class DeleteTest
    extends BaseTest
{
    public DeleteTest(String name)
    {
        super(name);
    }

    private static TypeHelper typeHelper = context.getTypeHelper();
    private static DataFactory dataFactory = context.getDataFactory();

    public void testDelete1()
    {
        Type t = typeHelper.getType("http://www.example.com/simple1", "Quote");
        DataObject o = dataFactory.create(t);

        Property quotesProp = t.getProperty("quotes");

        DataObject q1 = o.createDataObject(quotesProp);  q1.set("symbol", "Q1");
        DataObject q2 = o.createDataObject(quotesProp);  q2.set("symbol", "Q2");
        DataObject q3 = o.createDataObject(quotesProp);  q3.set("symbol", "Q3");

        assertEquals("Q1", o.get("quotes.0/symbol"));
        assertEquals("Q2", o.get("quotes.1/symbol"));
        assertEquals("Q3", o.get("quotes.2/symbol"));


        q1.delete();

        assertEquals("Q2", o.get("quotes.0/symbol"));
        assertEquals(null, q1.get("symbol"));


        q2.detach();

        assertEquals("Q3", o.get("quotes.0/symbol"));
        assertEquals("Q2", q2.get("symbol"));


        o.detach();

        assertEquals("Q3", o.get("quotes.0/symbol"));
        assertEquals("Q3", q3.get("symbol"));


        o.delete();

        assertEquals(null, o.get("quotes.0/symbol"));
        assertEquals(null, q3.get("symbol"));
    }
}
