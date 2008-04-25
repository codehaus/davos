/*   Copyright 2008 BEA Systems Inc.
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
 *  limitations under the License.
 */
package davos.sdo.impl.helpers;

import javax.sdo.DataObject;
import javax.sdo.Property;

import davos.sdo.DataObjectXML;
import davos.sdo.SDOContext;
import davos.sdo.impl.common.Common;
import davos.sdo.impl.type.BuiltInTypeSystem;
import davos.sdo.impl.type.PropertyImpl;
import davos.sdo.impl.data.DataObjectImpl;

/**
 * Created
 * Date: Jul 18, 2006
 * Time: 10:34:48 PM
 */
public class DataGraphHelper
{
    /**
     * @deprecated
     */
    public static final DataGraphHelper INSTANCE = new DataGraphHelper();

    private DataGraphHelper()
    {}

    public static void wrapWithDataGraph(DataObject o)
    {
        wrapWithDataGraph(o, null, null);
    }

    public static void wrapWithDataGraph(DataObject o, String uri, String name)
    {
        Property property;
        if (name == null)
        {
            property = ((DataObjectXML) o).getContainmentPropertyXML();
            if (property == null)
                property = BuiltInTypeSystem.P_DATAOBJECT;
        }
        else
        {
            SDOContext sdoContext = ((DataObjectImpl)o).getSDOContext();
            property = sdoContext.getTypeSystem().
                getGlobalPropertyByTopLevelElemQName(uri, name);
            if (property == null)
                property = PropertyImpl.create(BuiltInTypeSystem.BEADATAOBJECT, name, false, true,
                    null, null, false, true, null, Common.EMPTY_STRING_LIST, name, uri, -1, true, true, false);
        }
        wrapWithDataGraph(o, property);
    }

    public static void wrapWithDataGraph(DataObject o, Property p)
    {
        if (o == null)
            throw new IllegalArgumentException("The DataObject cannot be null");
        if (!p.isContainment())
            throw new IllegalArgumentException("Property \"" + p.getName() + "\" must have " +
                "containment set to true");
        SDOContext sdoContext = ((DataObjectImpl)o).getSDOContext();
        DataObject dataGraphObject = sdoContext.getDataFactory().create(BuiltInTypeSystem.DATAGRAPHTYPE);
        ((DataObjectImpl) dataGraphObject).setContainmentProperty(BuiltInTypeSystem.P_DATAGRAPH);

        dataGraphObject.set(p, o);
    }
}
