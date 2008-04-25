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

import davos.sdo.PropertyXML;
import davos.sdo.SequenceXML;
import davos.sdo.SDOContext;
import javax.sdo.DataObject;
import javax.sdo.helper.EqualityHelper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.HashMap;

/**
 * Created
 * Date: Sep 12, 2006
 * Time: 12:51:32 AM
 */
public class EqualityHelperImpl implements EqualityHelper
{
    private SDOContext _sdoContext;

    public EqualityHelperImpl(SDOContext sdoContext)
    {
        _sdoContext = sdoContext;
    }

    public boolean equalShallow(DataObject dataObject1, DataObject dataObject2)
    {
        return equalImpl(dataObject1, dataObject2, false, null);
    }

    public boolean equal(DataObject dataObject1, DataObject dataObject2)
    {
        EqualityContext ctx = new EqualityContext(dataObject1);
        return equalImpl(dataObject1, dataObject2, true, ctx) &&
            ctx.allReferencesResolved();
    }

    private boolean equalImpl(DataObject dataObject1, DataObject dataObject2,
        boolean deep, EqualityContext ctx)
    {
        if (dataObject1 == null || dataObject2 == null)
            return dataObject1 == dataObject2;
        if (dataObject1.equals(dataObject2))
            return true;
        if (dataObject1.getType() != dataObject2.getType())
            return false;

        if (dataObject1.getType().isSequenced())
        {
            // Compare using the sequences
            SequenceXML seq1 = (SequenceXML) dataObject1.getSequence();
            SequenceXML seq2 = (SequenceXML) dataObject2.getSequence();
            int size = seq1.size();
            if (size != seq2.size())
                return false;
            for (int i = 0; i < size; i++)
            {
                PropertyXML p1 = seq1.getPropertyXML(i);
                PropertyXML p2 = seq2.getPropertyXML(i);
                if (!propertyEquals(p1, p2))
                    return false;
                // Text is represented by null property
                if (p1 == null)
                {
                    if (!seq1.getValue(i).equals(seq2.getValue(i)))
                        return false;
                }
                else if (!objectEquals(seq1.getValue(i), seq2.getValue(i), p1, deep, ctx))
                    return false;
            }
        }
        else
        {
            // Compare "static" properties first
            List properties = dataObject1.getType().getProperties();
            for (Object o : properties)
            {
                PropertyXML p = (PropertyXML) o;
                Object o1 = dataObject1.get(p);
                Object o2 = dataObject2.get(p);
                if (!objectEquals(o1, o2, p, deep, ctx))
                    return false;
            }
            // Now compare "open-content" properties
            // This is tricky because we can have different objects representing
            // "equal" properties
            // The algorithm:
            // 1. put open-content properties from dataObject2 in a list
            //    (this could be optimized by using a map of lists, since we can
            //    maybe have the same property show up multiple times)
            // 2. for each propery in the list of open-content properties on dataObject1
            // 2.1. search for the first property in the list that is "equal" to it
            // 2.2. remove that property from the list
            // 2.3. compare the respective Objects
            List instanceProperties = dataObject2.getInstanceProperties();
            int size = properties.size();
            List<PropertyXML> list = new ArrayList<PropertyXML>(instanceProperties.size() - size);
            for (Iterator it = instanceProperties.listIterator(size); it.hasNext(); )
                list.add((PropertyXML) it.next());
            instanceProperties = dataObject1.getInstanceProperties();
            outer:
            for (Iterator it = instanceProperties.listIterator(size); it.hasNext(); )
            {
                PropertyXML p = (PropertyXML) it.next();
                for (ListIterator<PropertyXML> lit = list.listIterator(); lit.hasNext(); )
                {
                    PropertyXML p2 = lit.next();
                    if (propertyEquals(p, p2))
                    {
                        if (!objectEquals(dataObject1.get(p), dataObject2.get(p2), p, deep, ctx))
                            return false;
                        lit.remove();
                        continue outer;
                    }
                }
                // If we got here, it means that we were not able to find a
                // matching property for 'p'
                return false;
            }
            // If we got here, it means that all properties in dataObject1 were
            // matched by a corresponding property on dataObject2 and the corresponding
            // values were equal; not bad, make sure that there aren't properties left
            // in the list
            if (list.size() > 0)
                return false;
        }
        return ctx == null || ctx.registerObjectEquals(dataObject1, dataObject2);
    }

    private boolean propertyEquals(PropertyXML p1, PropertyXML p2)
    {
        if (p1 == null)
            return p2 == null;
        if (p1.isDynamic())
        {
            if (p1.getXMLNamespaceURI() == null || p1.getXMLNamespaceURI().length() == 0)
                return p1.getXMLName().equals(p2.getXMLName()) &&
                    p1.getName().equals(p2.getName()) &&
                    p1.getType().isDataType() == p2.getType().isDataType() &&
                    p1.isMany() == p2.isMany() &&
                    p1.isContainment() == p2.isContainment();
            else
                return p1 == p2;
        }
        else
            return p1 == p2;
    }

    private boolean objectEquals(Object obj1, Object obj2, PropertyXML p,
        boolean deepEquals, EqualityContext ctx)
    {
        if (p.getType().isDataType())
        {
            // Compare two dataType values
            if (obj1 == null)
                return obj2 == null;
            if (obj1 instanceof List)
            {
                if (!(obj2 instanceof List))
                    return false;
                // Compare two lists
                List l1 = (List) obj1;
                List l2 = (List) obj2;
                if (l1.size() != l2.size())
                    return false;
                for (int i = 0; i < l1.size(); i++)
                    if (!l1.get(i).equals(l2.get(i)))
                        return false;
                return true;
            }
            else if (obj1 instanceof byte[])
            {
                if (!(obj2 instanceof byte[]))
                    return false;
                // Compare two byte arrays
                return java.util.Arrays.equals((byte[]) obj1, (byte[]) obj2);
            }
            else if ((obj1 instanceof javax.sdo.ChangeSummary) &&
                (obj2 instanceof javax.sdo.ChangeSummary))
                return true; // We don't really care what changes there are as long as the final
                // objects are equal
            else
                return obj1.equals(obj2);
        }
        else if (!deepEquals)
            return true;
        else if (p.isContainment())
        {
            if (obj1 == null)
                return obj2 == null;
            // Even if the property is multi-valued, for sequences we still
            // compare them object-by-object, so an instanceof check is better
            if (obj1 instanceof List)
            {
                if (!(obj2 instanceof List))
                    return false;
                List l1 = (List) obj1;
                List l2 = (List) obj2;
                if (l1.size() != l2.size())
                    return false;
                for (int i = 0; i < l1.size(); i++)
                    if (!equalImpl((DataObject) l1.get(i), (DataObject) l2.get(i),
                        deepEquals, ctx))
                        return false;
                return true;
            }
            else
                return equalImpl((DataObject) obj1, (DataObject) obj2, deepEquals, ctx);
        }
        else
        {
            if (obj1 instanceof List)
            {
                List l1 = (List) obj1;
                List l2 = (List) obj2;
                if (l1.size() != l2.size())
                    return false;
                for (int i = 0; i < l1.size(); i++)
                    if (!ctx.registerRefEquals((DataObject) l1.get(i), (DataObject) l2.get(i)))
                        return false;
                return true;
            }
            else
                return ctx.registerRefEquals((DataObject) obj1, (DataObject) obj2);
        }
    }

    private static class EqualityContext
    {
        private DataObject _equalityTreeRoot;
        private HashMap<DataObject, DataObject> _objectMap;
        private HashMap<DataObject, DataObject> _referenceMap;

        EqualityContext(DataObject equalityTreeRoot)
        {
            _equalityTreeRoot = equalityTreeRoot;
            _objectMap = new HashMap<DataObject, DataObject>();
            _referenceMap = new HashMap<DataObject, DataObject>();
        }

        boolean registerRefEquals(DataObject obj1, DataObject obj2)
        {
            // Checks if two references can be equal or not
            // Two cases:
            // 1. if the references are to objects inside the "equality tree"
            //    they are equal if they point to objects that have been compared
            //    equal during the current equality check
            // 2. if the reference are to objects outside the "equality tree"
            //    they are equal if they point to the same object
            if (contained(obj1))
            {
                if (_objectMap.containsKey(obj1))
                    return _objectMap.get(obj1) == obj2;
                else if (_referenceMap.containsKey(obj1))
                    return _referenceMap.get(obj1) == obj2;
                else
                {
                    _referenceMap.put(obj1, obj2);
                    return true; // they may be equal, we don't know yet
                }
            }
            else
                return obj1 == obj2;
        }

        boolean registerObjectEquals(DataObject obj1, DataObject obj2)
        {
            _objectMap.put(obj1, obj2);
            if (_referenceMap.containsKey(obj1))
            {
                boolean result = _referenceMap.get(obj1) == obj2;
                _referenceMap.remove(obj1);
                return result;
            }
            return true;
        }

        boolean allReferencesResolved()
        {
            return _referenceMap.size() == 0;
        }

        private boolean contained(DataObject object)
        {
            // See if we can get from object to source by traversing the container field
            while (object != null)
            {
                if (_equalityTreeRoot == object)
                    return true;
                object = object.getContainer();
            }
            return false;
        }
    }
}
