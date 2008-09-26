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
package davos.sdo.impl.data;

import javax.sdo.ChangeSummary;
import javax.sdo.DataGraph;
import javax.sdo.DataObject;
import javax.sdo.Property;
import javax.sdo.Sequence;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.ListIterator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.WeakHashMap;
import java.util.Iterator;
import java.io.StringWriter;

import davos.sdo.DataObjectXML;
import davos.sdo.PropertyXML;
import davos.sdo.SequenceXML;
import davos.sdo.SDOContext;
import davos.sdo.impl.common.Common;
import davos.sdo.impl.common.NamespaceStack;
import davos.sdo.impl.common.ChangeSummaryXML;
import davos.sdo.impl.helpers.CopyHelperImpl;
import davos.sdo.impl.type.BuiltInTypeSystem;
import davos.sdo.impl.type.PropertyImpl;
import davos.sdo.impl.util.XmlPath;
import org.apache.xmlbeans.SchemaType;

public class ChangeSummaryImpl implements ChangeSummary, ChangeSummaryXML
{
    public static final String PATH_SEPARATOR = "/".intern();
    public static final String OPEN_BRACKET = "[".intern();
    public static final String CLOSE_BRACKET = "]".intern();
    private boolean logging;
    private DataGraph dataGraph;
    private DataObject dataObject = null;
    /**
     * We keep a HashMap (our own impl) of property to list of changes for that property
     * For sequenced types, this hashmap has entries for all the attribute properties plus
     * one entry for the elements with key the SEQUENCE sentinel property, because all elements
     * have their changes tracked at the same time
     * Arrays are represented with only the modifications in a separate list, with the index of
     * each change representing the offset from the previous change, this way the indexes don't
     * need updating at every insert.
     * Note on representation of arrays: for every insert, we still need to sequentially search the
     * list to find the right place for the newly instered change, so that changes remain sorted;
     * this is O(N) for insert. If O(logN) is required, these would need to be stored in a binary
     * tree and the tree rebalanced from time to time (to avoid the case where all changes are at
     * the end); however this would have a higher constant, so it's not a good idea until there are
     * LOTS of changes
     */
    private Map<DataObject, Change[]> modifiedObjects = new HashMap<DataObject, Change[]>();
    private Map<DataObject, DataObject> deletedObjects = new HashMap<DataObject, DataObject>();
    private WeakHashMap<DataObject, DataObject> liveObjectDeletedObjectMapping = new WeakHashMap<DataObject, DataObject>();
    private Set<DataObject> insertedObjects = new HashSet<DataObject>();
    private Map<DataObject, List<Change>> referredObjects = new HashMap<DataObject, List<Change>>();
    public static final String ATTR_REF = "ref";
    public static final String ATTR_UNSET = "unset";
    public static final String CHANGE_SUMMARY_DELETE = "delete";
    public static final String CHANGE_SUMMARY_CREATE = "create";
    public static final String CHANGE_SUMMARY_URI = "".intern();
    public static final String CHANGE_SUMMARY_ELEM = "changeSummary";
    public static final Property SEQUENCE = PropertyImpl.create(BuiltInTypeSystem.DATAOBJECT,
        "sequence", true, false, null, null, false, false, null, Common.EMPTY_STRING_LIST,
        "sequence", Common.EMPTY_STRING, -1, true, false, true);

    public ChangeSummaryImpl()
    {}

    public ChangeSummaryImpl(DataObject root)
    {
        dataObject = root;
    }

    public ChangeSummaryImpl(DataGraph graph)
    {
        dataGraph = graph;
    }

    public ChangeSummaryImpl(Map<DataObject, Change[]> modified,
        Map<DataObject, DataObject> deleted, Set<DataObject> inserted)
    {
        modifiedObjects = modified;
        deletedObjects = deleted;
        insertedObjects = inserted;
    }

    public boolean isLogging()
    {
        return logging;
    }

    public DataGraph getDataGraph()
    {
        return dataGraph != null ? dataGraph : dataObject.getDataGraph();
    }

    public List /*DataObject*/ getChangedDataObjects()
    {
        ArrayList<DataObject> result = new ArrayList<DataObject>(modifiedObjects.size() +
            insertedObjects.size() + deletedObjects.size());
        result.addAll(modifiedObjects.keySet());
        addSubTree(result, insertedObjects);
        addSubTree(result, deletedObjects.keySet());
        return result;
    }

    private void addSubTree(ArrayList<DataObject> result, Set<DataObject> roots)
    {
        // Add all the root DataObject and all their contained DataObjects to the result
        int i = result.size();
        result.addAll(roots);
        while (i < result.size())
        {
            DataObject o = result.get(i++);
            for (Property p : (List<Property>) o.getInstanceProperties())
                if (p.isContainment() && !p.getType().isDataType())
                    if (p.isMany())
                    {
                        List valueAsList = o.getList(p);
                        for (DataObject oo : (List<DataObject>) valueAsList)
                            if (oo != null)
                                result.add(oo);
                    }
                    else
                    {
                        DataObject value = o.getDataObject(p);
                        if (value != null)
                            result.add(value);
                    }
        }
    }

    public boolean isCreated(DataObject dataObject)
    {
        while (dataObject != null && !insertedObjects.contains(dataObject))
            dataObject = dataObject.getContainer();
        return dataObject != null;
    }

    /*
    * When deleting a DataObject, we have to
    * cover the case when this object has been modified before it was deleted. We also have
    * to avoid keeping references to DataObjects that are no longer part of the current
    * tree (I think this is mandated by the spec when it mentions the use of CopyHelper
    * for deletes; also just to be good citizens it's a good thing to do). At the same time,
    * we need to be able to answer questions like "isDeleted".
    *
    * So, here is the design
    * When a "delete" of object 'do' is logged:
    * 1. Find 'do' in the list of modified objects, let the result be 'found'
    * 2. Copy the object 'do' into 'olddo' using CopyHelper or similar
    * 3. If 'found' then undo changes on the 'olddo' copy, then remove 'do' from
    *    the 'modified' map
    * 4. Store 'do' in a WeakHashMap with the value being 'olddo'
    * If later on, somebody asks 'isDeleted' we first look at the WeakHashMap which will
    * obviously still contain 'do'. The trick here is what happens if someone asks
    * 'isDeleted' right after unmarshalling. We have to detect this case and use the
    * 'deletedObjects' map or use EqualityHelper, need to decide which one.
    * When a change comes to a reference to 'do'
    * 1. We use the WeakHashMap to find 'do' (which still exists, because it is referenced
    *    by this reference
    * 2. We get 'olddo' from this map
    * 3. We set 'olddo' as the 'old value' in the change summary
    */
    public boolean isDeleted(DataObject dataObject)
    {
        if (liveObjectDeletedObjectMapping.containsKey(dataObject))
            return true;
        while (dataObject != null && !deletedObjects.containsKey(dataObject))
            dataObject = dataObject.getContainer();
        return dataObject != null;
    }

    public List /*ChangeSummary.Setting*/ getOldValues(DataObject dataObject)
    {
        if (isModified(dataObject))
        {
            Change[] loggedChanges = modifiedObjects.get(dataObject);
            List<Change> result = new ArrayList<Change>();
            for (int i = 0; i < loggedChanges.length; i++)
                for (Change c = loggedChanges[i]; c != null; c = c.next)
                {
                    Property prop = c.getProperty();
                    if (prop == SEQUENCE)
                    {
                        if (dataObject.getSequence() == null)
                            throw new IllegalStateException("Sequence change found on a " +
                                "non-sequenced DataObject, type = " + dataObject.getType());
                        Store currentStore = ((DataObjectImpl) dataObject).getStore();
                        HashSet<Property> seenProperties = new HashSet<Property>();
                        int currentIndex = 0;
                        for (Change cs = c.next2; cs != null; cs = cs.next2)
                        {
                            currentIndex += cs.getArrayPos();
                            prop = cs.getProperty();
                            if (seenProperties.contains(prop))
                                continue;
                            else
                                seenProperties.add(prop);
                            if (prop == null || prop.isMany())
                            {
                                // We need to go through the list of changes
                                // and collect all that pertain to this property
                                List originalList = new ArrayList();
                                int index = 0;
                                int currentIndex2 = currentIndex - cs.getArrayPos();
                                Change c2 = cs;
                                while (c2 != null)
                                {
                                    currentIndex2 += c2.getArrayPos();
                                    if (c2.getProperty() == prop)
                                    {
                                        while (index < currentIndex2)
                                        {
                                           if(currentStore.storeSequenceGetPropertyXML(index)==prop)
                                        originalList.add(currentStore.storeSequenceGetValue(index));
                                            index++;
                                        }
                                        if (c2.isSet())
                                        {
                                            // It was a deletion
                                            originalList.add(c2.getValue());
                                        }
                                        else
                                        {
                                            // It was an insertion
                                            index++;
                                        }
                                    }
                                    c2 = c2.next2;
                                }
                                while (index < currentStore.storeSequenceSize())
                                {
                                    if (currentStore.storeSequenceGetPropertyXML(index) == prop)
                                        originalList.add(currentStore.storeSequenceGetValue(index));
                                    index++;
                                }
                                result.add(new Change(prop, originalList, originalList.size() > 0, -1));
                            }
                            else
                                result.add(cs);
                        }
                    }
                    else if (prop.isMany() && !prop.getType().isDataType())
                    {
                        // For arrays of DataObjects, we keep the individual changes
                        // as separate Settings objects, but for the purpose of this
                        // API we need to aggregate them into one old values list
                        List currentList = dataObject.getList(prop);
                        List originalList = new ArrayList();
                        int index = 0;
                        int currentIndex = 0;
                        Change c2 = c;
                        while (c2 != null)
                        {
                            currentIndex += c2.getArrayPos();
                            while (index < currentIndex)
                                originalList.add(currentList.get(index++));
                            if (c.isSet())
                            {
                                // Deletion
                                originalList.add(c.getValue());
                            }
                            else
                            {
                                // Insertion
                                index++;
                            }
                            c2 = c2.next2;
                        }
                        while (index < currentList.size())
                            originalList.add(currentList.get(index++));
                        result.add(new Change(prop, originalList, originalList.size() > 0, -1));
                    }
                    else
                        result.add(c);
                }
            return result;
        }
        else if (isDeleted(dataObject))
        {
            // We need to build a ChangeSummary.Setting array for all the properties in this
            // DataObject
            if (liveObjectDeletedObjectMapping.containsKey(dataObject))
                dataObject = liveObjectDeletedObjectMapping.get(dataObject);
            List allProperties = dataObject.getInstanceProperties();
            List<Change> result = new ArrayList<Change>(allProperties.size());
            for (Property prop : (List<Property>) allProperties)
                result.add(new Change(prop, dataObject.get(prop), dataObject.isSet(prop), -1));
            return result;
        }
        else
            return Collections.EMPTY_LIST;
    }

    public void beginLogging()
    {
        if (!logging)
        {
            clearChanges();
            logging = true;
        }
    }

    public void setLogging(boolean logging)
    {
        this.logging = logging;
    }

    public void endLogging()
    {
        logging = false;
    }

    public boolean isModified(DataObject dataObject)
    {
        return modifiedObjects.containsKey(dataObject);
    }

    public DataObject getRootObject()
    {
        return dataObject != null ? dataObject : dataGraph.getRootObject();
    }

    public Setting getOldValue(DataObject dataObject, Property property)
    {
        if (isModified(dataObject))
        {
            Change[] chgs = modifiedObjects.get(dataObject);
            if (chgs == null)
                return null;
            if (!property.isMany() || property.getType().isDataType())
            {
                // Even for sequenced DOs, there is a chance that we'll find the property without
                // having to look inside the sequence
                // First change found is the one that we are looking for
                int hash = hashCode(property, chgs.length);
                for (Change c = chgs[hash]; c != null; c = c.next)
                    if (c.getProperty() == property)
                        return c;
            }
            if (dataObject.getSequence() != null)
            {
                int index = 0;
                List originalList = new ArrayList();
                boolean seenModification = false;
                Store currentStore = ((DataObjectImpl) dataObject).getStore();
                int currentIndex = 0;
                for (Change c = getFirstSequenceChange(chgs); c != null; c = c.next2)
                {
                    currentIndex += c.getArrayPos();
                    if (c.getProperty() == property)
                    {
                        seenModification = true;
                        while (index < currentIndex)
                        {
                            if (currentStore.storeSequenceGetPropertyXML(index) == property)
                                originalList.add(currentStore.storeSequenceGetValue(index));
                            index++;
                        }
                        if (c.isSet())
                        {
                            // It was a deletion
                            originalList.add(c.getValue());
                        }
                        else
                        {
                            // It was an insertion
                            index++;
                        }
                    }
                }
                while (index < currentStore.storeSequenceSize())
                {
                    if (currentStore.storeSequenceGetPropertyXML(index) == property)
                    {
                        seenModification = true;
                        originalList.add(currentStore.storeSequenceGetValue(index));
                    }
                    index++;
                }
                if (seenModification)
                    if (property.isMany())
                        return new Change(property, originalList, originalList.size() > 0, -1);
                    else if (originalList.size() > 0)
                        return new Change(property, originalList.get(0), true, -1);
                    else
                        return new Change(property, null, false, -1);
            }
            else if (property.isMany())
            {
                // For arrays of DataObjects, we keep the individual changes
                // as separate Settings objects, but for the purpose of this
                // API we need to aggregate them into one old values list
                List currentList = dataObject.getList(property);
                List originalList = new ArrayList();
                int index = 0;
                for (Change c = chgs[hashCode(property, chgs.length)]; c != null; c = c.next)
                {
                    if (c.getProperty() == property)
                    {
                        int currentIndex = 0;
                        Change c2 = c;
                        while (c2 != null)
                        {
                            currentIndex += c.getArrayPos();
                            while (index < currentIndex)
                                originalList.add(currentList.get(index++));
                            if (c.isSet())
                            {
                                // Deletion
                                originalList.add(c.getValue());
                            }
                            else
                            {
                                // Insertion
                                index++;
                            }
                            c2 = c2.next2;
                        }
                        while (index < currentList.size())
                            originalList.add(currentList.get(index++));
                        return new Change(property, originalList, originalList.size() > 0, -1);
                    }
                }
            }
            return null;
        }
        else if (isDeleted(dataObject))
        {
            // We simply return the value of the given property in the copy at the time logging
            // was started
            if (liveObjectDeletedObjectMapping.containsKey(dataObject))
                dataObject = liveObjectDeletedObjectMapping.get(dataObject);
            return new Change(property, dataObject.get(property), dataObject.isSet(property), -1);
        }
        else
            return null;
    }

    public DataObject getOldContainer(DataObject dataObject)
    {
        if (liveObjectDeletedObjectMapping.containsKey(dataObject))
            dataObject = liveObjectDeletedObjectMapping.get(dataObject);
        DataObject result = deletedObjects.get(dataObject);
        return result == null ? dataObject.getContainer() : result;
    }

    public Property getOldContainmentProperty(DataObject dataObject)
    {
        // We copy the containment property for deleted DataObjects
        if (liveObjectDeletedObjectMapping.containsKey(dataObject))
            dataObject = liveObjectDeletedObjectMapping.get(dataObject);
        return ((DataObjectXML)dataObject).getContainmentPropertyXML();
    }

    public Sequence getOldSequence(DataObject dataObject)
    {
        if (liveObjectDeletedObjectMapping.containsKey(dataObject))
            dataObject = liveObjectDeletedObjectMapping.get(dataObject);
        if (deletedObjects.containsKey(dataObject) ||
            !modifiedObjects.containsKey(dataObject))
            return dataObject.getSequence();
        if (!dataObject.getType().isSequenced())
            return null;
        Change[] chgs = modifiedObjects.get(dataObject);
        assert chgs != null;
        Store newStore = ((DataObjectImpl) dataObject).getStore();
        DataObjectGeneral impl = new DataObjectGeneral();
        DataObjectXML oXML = (DataObjectXML) dataObject;
        impl.init( oXML.getTypeXML(), null, null, oXML.getContainmentPropertyXML());

        SDOContext sdoContext = ((DataObjectImpl)dataObject).getSDOContext();
        impl.setSDOContext(sdoContext);

        int currentIndex = 0;
        int index = 0;
        for (Change c = getFirstSequenceChange(chgs); c != null; c = c.next2)
        {
            currentIndex += c.getArrayPos();
            while (index < currentIndex)
            {
                PropertyXML prop = newStore.storeSequenceGetPropertyXML(index);
                impl.storeAddNewBasic( prop, newStore.storeSequenceGetValue(index),
                    newStore.storeSequenceGetXMLPrefix(index), prop);
                index++;
            }
            if (c.isSet())
                impl.storeAddNewBasic((PropertyXML) c.getProperty(), c.getValue(), null, (PropertyXML) c.getProperty());
            else
                index++;
        }
        while (index < newStore.storeSequenceSize())
        {
            PropertyXML prop = newStore.storeSequenceGetPropertyXML(index);
            impl.storeAddNewBasic(prop, newStore.storeSequenceGetValue(index),
                newStore.storeSequenceGetXMLPrefix(index), prop);
            index++;
        }
        return impl.storeGetSequenceXML();
    }

    public void undoChanges()
    {
        // Since during this process we need to call back objects in the graph in order
        // to set old values on them and that may call to register changes, we have to
        // turn change tracking off while performing the undo
        boolean savedLogging = logging;
        logging = false;
        for (Map.Entry<DataObject, Change[]> entry : modifiedObjects.entrySet())
        {
            DataObject parent = entry.getKey();
            Change[] changes = entry.getValue();

            undoObject(parent, changes, null);
        }
        clearChanges();
        logging = savedLogging;
    }

    private void undoObject(DataObject dobj, Change[] changes, CopyHelperImpl.CopyContext ctx)
    {
        // Before changing values on the DataObject, we have to disable auto-setting of
        // bidirectional properties, otherwise operation on this DataObject can have as
        // side-effect setting of properties on a "live" (not deleted) DataObject
        ((DataObjectImpl) dobj).setOppositeIgnore(true);
        for (int i = 0; i < changes.length; i++)
            for (Change c = changes[i]; c != null; c = c.next)
            {
                Property prop = c.getProperty();
                if (prop == SEQUENCE)
                {
                    // Revert to the old sequence
                    Store newStore = ((DataObjectImpl) dobj).getStore();
//                    if (newSequence == null)
//                        throw new IllegalStateException("Sequence change found on a " +
//                            "non-sequenced DataObject, type = " + dataObject.getType());
                    int offset = 0;

                    Change cs = c.next2;
                    for (int j = 0; j < newStore.storeSequenceSize() && cs != null; j++)
                    {
                        offset += cs.getArrayPos();
                        j = offset; // Move to the place of the change in the sequence
                        if (j >= newStore.storeSequenceSize())
                            break; // Defensive programming
                        do
                        {
                            PropertyXML propseq = newStore.storeSequenceGetPropertyXML(j);
                            if (propseq == cs.getProperty())
                            {
                                if (cs.isSet())
                                {
                                    // In principle, this is a deletion, but if we have a deletion
                                    // followed by an insertion of the same property in the same
                                    // place, then we can optimize this by doing a set instead
                                    Change c2 = cs.next2;
                                    if (c2 != null && propseq == c2.getProperty() && !c2.isSet() &&
                                        c2.getArrayPos() == 0)
                                    {
                                        if(propseq != null && !propseq.getType().isDataType() &&
                                            ctx != null)
                                        {
                                            if (propseq.isContainment())
                                            {
                                                DataObject deletedObject =(DataObject)cs.getValue();
                                                deletedObjects.remove(deletedObject);
                                                newStore.storeSequenceSet(j, deletedObject);
                                            }
                                            else
                                                newStore.storeSequenceSet(j, ctx.copyReference(
                                                    (DataObjectXML) cs.getValue(), false));
                                        }
                                        else
                                            newStore.storeSequenceSet(j, cs.getValue());
                                        cs = c2;
                                    }
                                    else
                                    {
                                        if (propseq != null && !propseq.getType().isDataType() &&
                                            ctx != null)
                                        {
                                            if (propseq.isContainment())
                                            {
                                                DataObject deletedObject = (DataObject)cs.getValue();
                                                deletedObjects.remove(deletedObject);
                                                newStore.storeSequenceAddNew(j++, propseq,
                                                    deletedObject, null, propseq);
                                            }
                                            else
                                                newStore.storeSequenceAddNew(j++, propseq,
                                                    ctx.copyReference((DataObjectXML) cs.getValue(),
                                                        false), null, propseq);
                                        }
                                        else
                                            newStore.storeSequenceAddNew(j++, propseq, cs.getValue()
                                                , null, propseq);
                                        offset++;
                                    }
                                }
                                else
                                {
                                    newStore.storeSequenceUnset(j--);
                                    offset--;
                                }
                            }
                            else
                            {
                                // Must be the deletion of another property
                                assert cs.getValue() != null && cs.isSet();
                                Property p = cs.getProperty();
                                if (p != null && !p.getType().isDataType() && ctx != null)
                                {
                                    if (p.isContainment())
                                    {
                                        DataObject deletedObject = (DataObject) cs.getValue();
                                        deletedObjects.remove(deletedObject);
                                        newStore.storeSequenceAddNew(j++, (PropertyXML) p,
                                            deletedObject, null, (PropertyXML) p);
                                    }
                                    else
                                        newStore.storeSequenceAddNew(j++,
                                            (PropertyXML) cs.getProperty(),
                                            ctx.copyReference((DataObjectXML) cs.getValue(), false),
                                            null, (PropertyXML) cs.getProperty());
                                }
                                else
                                    newStore.storeSequenceAddNew(j++, (PropertyXML) p,cs.getValue(),
                                        null, (PropertyXML) p);
                                offset++;
                            }
                            cs = cs.next2;
                        } while (cs != null && cs.getArrayPos() == 0);
                    }
                    while (cs != null)
                    {
                        // Deletions from the end
                        if (cs.isSet())
                        {
                            Property p = cs.getProperty();
                            if (p != null && !p.getType().isDataType() && ctx != null)
                            {
                                if (p.isContainment())
                                {
                                    DataObject deletedObject = (DataObject) cs.getValue();
                                    deletedObjects.remove(deletedObject);
                                    newStore.storeAddNew((PropertyXML) p, deletedObject, null,
                                        (PropertyXML) p);
                                }
                                else
                                    newStore.storeAddNew((PropertyXML) cs.getProperty(),
                                        ctx.copyReference((DataObjectXML) cs.getValue(), false),
                                        null, (PropertyXML) p);
                            }
                            else
                                newStore.storeAddNew((PropertyXML) p, cs.getValue(), null,
                                    (PropertyXML) p);
                            offset++;
                        }
                        cs = cs.next2;
                    }
                }
                if (c.getArrayPos() == -1)
                {
                    if (prop.getType().isDataType())
                    {
                        if (prop.isMany())
                        {
                            // If the property is many-valued, we have to set the values
                            // using the special List returned by the DataObject
                            List actualValues = dobj.getList(prop);
                            List oldValues = (List) c.getValue();
                            if (actualValues.size() < oldValues.size())
                            {
                                int j;
                                for (j = 0; j < actualValues.size(); j++)
                                    actualValues.set(j, oldValues.get(j));
                                for (; j < oldValues.size(); j++)
                                    actualValues.add(oldValues.get(j));
                            }
                            else
                            {
                                int j;
                                for (j = 0; j < oldValues.size(); j++)
                                    actualValues.set(j, oldValues.get(j));
                                for (j = actualValues.size() - 1; j >= oldValues.size(); j--)
                                    actualValues.remove(j);
                            }
                        }
                        else if (c.isSet())
                            dobj.set(prop, c.getValue());
                        else
                            dobj.unset(prop);
                    }
                    else if (!prop.isContainment())
                    {
                        if (c.isSet())
                            if (prop.isMany())
                            {
                                List list = new ArrayList();
                                for (Object o : (List) c.getValue())
                                    if (ctx != null)
                                        list.add(ctx.copyReference((DataObjectXML) o, false));
                                    else
                                        list.add(o);
                                dobj.set(prop, list);
                            }
                            else
                            if (ctx != null)
                                dobj.set(prop, ctx.copyReference((DataObjectXML) c.getValue(), false));
                            else
                                dobj.set(prop, c.getValue());
                        else
                            dobj.unset(prop);
                    }
                    else
                    {
                        if (c.isSet())
                        {
                            // This is a deletion. Since the deleted object is now part of
                            // a bigger change, remove its entry from the deleted objects
                            // list, in other words, compact the change
                            DataObject deletedObject = (DataObject) c.getValue();
                            deletedObjects.remove(deletedObject);
                            dobj.set(prop, deletedObject);
                        }
                        else
                        {
                            dobj.unset(prop);
                            // There could be a modification here
                            if (c.next2 != null)
                            {
                                c = c.next2;
                                if (c.isSet())
                                {
                                    DataObject deletedObject = (DataObject) c.getValue();
                                    deletedObjects.remove(deletedObject);
                                    dobj.set(prop, deletedObject);
                                }
                            }
                        }
                    }
                }
                else
                {
                    int offset = 0;
                    assert prop.isMany();
                    List list = dobj.getList(prop);
                    if (prop.getType().isDataType())
                    {
                        int currentIndex = 0;
                        for (Change c2 = c; c2 != null; c2 = c2.next2)
                        {
                            currentIndex += c.getArrayPos();
                            if (c2.isSet())
                                list.set(currentIndex, c.getValue());
                            else
                                throw new IllegalStateException();
                        }
                    }
                    else if (!prop.isContainment())
                    {
                        int currentIndex = 0;
                        for (Change c2 = c; c2 != null; c2 = c2.next2)
                        {
                            currentIndex += c2.getArrayPos();
                            if (c2.isSet())
                                if (ctx != null)
                                    list.set(currentIndex,
                                        ctx.copyReference((DataObjectXML) c2.getValue(), false));
                                else
                                    list.set(currentIndex, c2.getValue());
                            else
                                throw new IllegalStateException();
                        }
                    }
                    else
                    {
                        int currentIndex = 0;
                        for (Change c2 = c; c2 != null; c2 = c2.next2)
                        {
                            currentIndex += c2.getArrayPos();
                            if (c2.isSet())
                            {
                                // This is a deletion. Since the deleted object is now part of
                                // a bigger change, remove its entry from the deleted objects
                                // list, in other words, compact the change
                                DataObjectXML deletedObject = (DataObjectXML) c2.getValue();
                                deletedObjects.remove(deletedObject);
                                list.add(currentIndex + offset, deletedObject);
                                offset++;
                            }
                            else
                            {
                                list.remove(currentIndex + offset);
                                offset--;
                            }
                        }
                    }
                }
            }
        ((DataObjectImpl) dobj).setOppositeIgnore(false);
    }

    public void clearChanges()
    {
        modifiedObjects.clear();
        deletedObjects.clear();
        insertedObjects.clear();
        referredObjects.clear();
        liveObjectDeletedObjectMapping.clear();
    }

    /**
     * This class is supposed to represent a change.
     * The following states exist:
     * 1. if property.getType().isDataType() then the change is a modification
     *     and "value" represents the old value for the property (could be List)
     * 2. else if !property.isContainment() then the change is a modification
     *     of a reference and "value" represents the old reference (may be a
     *     List of references); the references will be found in the list
     *     of reference in the "live" graph
     * 3. else if set then the change is a deletion and "value" represents
     *     the DataObject that was deleted; "value" will also be found among
     *     deletedObjects
     * 4. else the change is an insertion and "value" represents the new
     *     DataObject; it will be found among insertedObjects and among
     *     references to the "live" graph
     */
    public static class Change implements Setting
    {
        private Property property;
        private Object value;
        private boolean set;
        private int arrayPos;
        public Change next;
        // For the first change in an array property, used as pointer to the next change
        // in the same array
        public Change next2;

        public Change()
        {}

        public Change(Property property, Object oldValue, boolean set, int arrayPos)
        {
            this.property = property;
            this.value = oldValue;
            this.set = set;
            this.arrayPos = arrayPos;
        }

        public Property getProperty()
        {
            return property;
        }

        public Object getValue()
        {
            return value;
        }

        public boolean isSet()
        {
            return set;
        }

        public int getArrayPos()
        {
            return arrayPos;
        }

        public void setProperty(Property property)
        {
            this.property = property;
        }

        public void setValue(Object value)
        {
            this.value = value;
        }

        protected void setSet(boolean set)
        {
            this.set = set;
        }

        public void setArrayPos(int arrayPos)
        {
            this.arrayPos = arrayPos;
        }

        protected void incrementArrayPos()
        {
            arrayPos++;
        }

        protected void decrementArrayPos()
        {
            arrayPos--;
        }

        public String toString()
        {
            StringBuffer sb = new StringBuffer();
            sb.append('<').append("prop: ").append(property == null ? "<text>" : property.getName());
            if (arrayPos >= 0)
                sb.append('[').append(arrayPos).append(']');
            sb.append(", ").append(set ? "set" : "---");
            sb.append(", ").append("value: ").append(value);
            sb.append('>').append('\n');
            return sb.toString();
        }
    }

    // ======================================
    // Implementation
    // ======================================
    //

    /*
     * When property is of a simple type, only modification is possible:
     * When the property is of a data object type, then if it is a reference,
     * then we also use modification to track that type of change
     */
    public void logModification(DataObject parent, Property prop,
            Object oldValue, boolean wasSet, int arrayIndex)
    {
        logModification(parent, prop, oldValue, wasSet, arrayIndex, false);
    }

    /*
     * This version of the method is specifically built for sequenced types,
     * where it is possible to delete values of datatypes too
     */
    public void logModification(DataObject parent, Property prop,
        Object oldValue, boolean wasSet, int arrayIndex, boolean delete)
    {
        if (prop != null && !prop.getType().isDataType() && prop.isContainment())
            throw new IllegalArgumentException("Property \"" + prop.getName() +
                "\" is not of a data type, so cannot be modified");
        DataObject o = parent;
        while (o != null)
        {
            if (insertedObjects.contains(o))
                return;
            o = o.getContainer();
        }

        Change[] chgs;
        Change newChange = null;
        if (!modifiedObjects.containsKey(parent))
        {
            chgs = new Change[computeHashMapSize(parent)];
            modifiedObjects.put(parent, chgs);
        }
        else
            chgs = modifiedObjects.get(parent);

        if (parent.getSequence() != null && arrayIndex >= 0)
        {
            // For sequenced types, we need the index as a pointer into the
            // array of children elements and text of the containing object
            Change lastChange = getcreateSequenceChange(chgs); // the sentinel
            Change change = lastChange.next2;
            int currentIndex = 0;
            boolean logged = false;
            while(change != null)
            {
                currentIndex += change.getArrayPos();
                if (arrayIndex == currentIndex)
                {
                    Change change2 = change.next2;
                    if (!change.isSet() || change2 != null && change.getProperty() == change2.getProperty() &&
                            change2.getArrayPos() == 0 && !change2.isSet())
                    {
                        // We already have a modification for this index
                        logged = true;
                        if (delete)
                        {
                            if (!change.isSet())
                            {
                                // Deletion of a value that had been added previously
                                // Remove the change
                                lastChange.next2 = change.next2;
                                // Because we have removed a value previously in the sequence
                                // we need to decrement the index of the next change
                                if (change.next2 != null)
                                    change.next2.arrayPos--;
                            }
                            else // !change2.isSet()
                            {
                                // A value that had been modified is now deleted
                                // Remove the "new value" part of the change
                                change.next2 = change2.next2;
                                // Because we have removed a value previously in the sequence
                                // we need to decrement the index of the next change
                                if (change2.next2 != null)
                                    change2.next2.arrayPos--;
                            }
                        }
                        else if (!wasSet)
                        {
                            // Insertion of a new value before the previously inserted value
                            // at this index
                            logged = false;
                        }
                        break;
                    }
                }
                else if (arrayIndex < currentIndex)
                {
                    // Insert a new modification here
                    break;
                }
                lastChange = change;
                change = change.next2;
            }
            if (!logged)
            {
                Change newChange2 = null;
                if (wasSet && !delete)
                    newChange2 = new Change(prop, null, false, 0);
                if (change == null)
                {
                    newChange = new Change(prop, oldValue, wasSet, arrayIndex - currentIndex);
                    newChange.next2 = newChange2;
                }
                else
                {
                    newChange = new Change(prop, oldValue, wasSet, arrayIndex - currentIndex +
                        change.getArrayPos());
                    if (newChange2 != null)
                    {
                        change.setArrayPos(change.getArrayPos() - newChange.getArrayPos());
                        newChange.next2 = newChange2;
                        newChange2.next2 = change;
                    }
                    else
                    {
                        change.setArrayPos(change.getArrayPos() - newChange.getArrayPos() +
                            (delete ? -1 : 1));
                        newChange.next2 = change;
                    }
                }
                lastChange.next2 = newChange;
            }
        }
        else
        {
            int hashCode = hashCode(prop, chgs.length);
            Change change = chgs[hashCode];
            while (change != null)
            {
                if (change.getProperty() == prop)
                    break;
                change = change.next;
            }
            if (change == null)
            {
                // Track a new change
                newChange = new Change(prop, oldValue, wasSet, arrayIndex);
                newChange.next = chgs[hashCode];
                chgs[hashCode] = newChange;
            }
            // else we don't need to o anything, since the value that was changed
            // was *not* the original value anyway
        }

        // If the oldValue is a List, we need to duplicate it, because the original
        // list may get changed
        if ((oldValue instanceof List) && newChange != null)
            newChange.setValue(new ArrayList((List) oldValue));

        if (prop != null && !prop.getType().isDataType() && newChange != null && oldValue != null)
        {
            // Since this is a reference, there exists the possibility that
            // it will be deleted in the future so we need to keep track of it
            // so we can let go of the reference when it gets deleted
            if (oldValue instanceof DataObject)
            {
                DataObject dooldValue = (DataObject) oldValue;
                if (liveObjectDeletedObjectMapping.containsKey(dooldValue))
                    newChange.setValue(liveObjectDeletedObjectMapping.get(oldValue));
                else
                    addReferredObject(dooldValue, newChange);
            }
            else
            {
                List<DataObject> list = (List<DataObject>) newChange.getValue();
                for (int i = 0; i < list.size(); i++)
                {
                    DataObject olddo = list.get(i);
                    if (liveObjectDeletedObjectMapping.containsKey(olddo))
                        list.set(i, liveObjectDeletedObjectMapping.get(olddo));
                    else
                        addReferredObject(olddo, newChange);
                }
            }
        }
    }

    public void logInsertion(DataObject parent, Property prop, int arrayIndex,
        DataObject newValue)
    {
        if (prop.getType().isDataType())
            throw new IllegalArgumentException("Property \"" + prop.getName() +
                "\" is not of a DataObject type so it can not be added/removed");

        DataObject o = newValue;
        while (o != null)
        {
            if (insertedObjects.contains(o))
                return;
            o = o.getContainer();
        }

        if (newValue != null)
            insertedObjects.add(newValue);
        Change[] chgs;
        if (!modifiedObjects.containsKey(parent))
        {
            chgs = new Change[computeHashMapSize(parent)];
            modifiedObjects.put(parent, chgs);
        }
        else
            chgs = modifiedObjects.get(parent);
        logInsertionHelper(parent.getSequence() != null, prop, arrayIndex, newValue, chgs);
    }

    public void logInsertionHelper(boolean sequenced, Property prop, int arrayIndex, DataObject newValue, Change[] chgs)
    {
        if (sequenced)
        {
            // For sequenced types, we need the index as a pointer into the
            // array of children elements and text of the containing object
            if (arrayIndex < 0)
                throw new IllegalArgumentException();
            Change lastChange = getcreateSequenceChange(chgs); // the sentinel
            Change change = lastChange.next2;
            int currentIndex = 0;
            while (change != null)
            {
                currentIndex += change.getArrayPos();
                if (arrayIndex <= currentIndex)
                    break;
                lastChange = change;
                change = change.next2;
            }
            Change newChange;
            if (change == null)
            {
                newChange = new Change(prop, newValue, false, arrayIndex - currentIndex);
            }
            else
            {
                newChange = new Change(prop, newValue, false, arrayIndex - currentIndex +
                    change.getArrayPos());
                newChange.next2 = change;
                change.setArrayPos(change.getArrayPos() - newChange.getArrayPos() + 1);
            }
            lastChange.next2 = newChange;
        }
        else
        {
            // For multi-valued properties, we have to record that there is a new
            // property inserted and all the array indexes are shifted
            int hashCode = hashCode(prop, chgs.length);
            Change change = chgs[hashCode];
            Change lastChange = null;
            while (change != null)
            {
                if (change.getProperty() == prop)
                    break;
                lastChange = change;
                change = change.next;
            }
            if (change == null)
            {
                change = new Change(prop, newValue, false, arrayIndex);
                change.next = chgs[hashCode];
                chgs[hashCode] = change;
            }
            else
            {
                int currentIndex = 0;
                Change lastChange2 = null;
                while (change != null)
                {
                    currentIndex += change.getArrayPos();
                    if (arrayIndex <= currentIndex)
                        break;
                    lastChange2 = change;
                    change = change.next2;
                }
                if (lastChange2 == null)
                {
                    assert change != null;
                    Change newChange = new Change(prop, newValue, false, arrayIndex);
                    newChange.next = change.next;
                    if (lastChange != null)
                    {
                        lastChange.next = newChange;
                    }
                    else
                    {
                        chgs[hashCode] = newChange;
                    }
                    newChange.next2 = change;
                    change.setArrayPos(change.getArrayPos() - arrayIndex + 1);
                }
                else
                {
                    Change newChange;
                    if (change == null)
                    {
                        newChange = new Change(prop, newValue, false, arrayIndex - currentIndex);
                    }
                    else
                    {
                        newChange = new Change(prop, newValue, false, arrayIndex - currentIndex +
                            change.getArrayPos());
                        change.setArrayPos(change.getArrayPos() - newChange.getArrayPos() + 1);
                        newChange.next2 = change;
                    }
                    lastChange2.next2 = newChange;
                }
            }
        }
    }

    public void logDeletion(DataObject parent, Property prop, int arrayIndex,
        DataObject oldValue)
    {
        logDeletion(parent, prop, arrayIndex, oldValue, true);
    }

    public void logDeletion(DataObject parent, Property prop, int arrayIndex,
        DataObject oldValue, boolean makeCopy)
    {
        if (prop.getType().isDataType())
            throw new IllegalArgumentException("Property \"" + prop.getName() +
                "\" is not of a DataObject type so it can not be added/removed");

        DataObject o = parent;
        while (o != null)
        {
            if (insertedObjects.contains(o))
                return;
            o = o.getContainer();
        }

        DataObject copiedValue = makeCopy ? copyDeletedObject((DataObjectXML) oldValue) : oldValue;
        if (copiedValue != null)
            deletedObjects.put(copiedValue, parent);
        Change[] chgs;
        if (!modifiedObjects.containsKey(parent))
        {
            chgs = new Change[computeHashMapSize(parent)];
            modifiedObjects.put(parent, chgs);
        }
        else
            chgs = modifiedObjects.get(parent);
        if (parent.getSequence() != null)
        {
            // For sequenced types, we need the index as a pointer into the
            // array of children elements and text of the containing object
            if (arrayIndex < 0)
                throw new IllegalArgumentException();
            Change lastChange = getcreateSequenceChange(chgs);
            Change change = lastChange.next2;
            int currentIndex = 0;
            while (change != null)
            {
                currentIndex += change.getArrayPos();
                if (arrayIndex < currentIndex)
                {
                    break;
                }
                else if (arrayIndex == currentIndex && prop == change.getProperty() &&
                    !change.isSet())
                {
                    // An inserted object has now been deleted
                    deletedObjects.remove(copiedValue);
                    insertedObjects.remove(oldValue);
                    lastChange.next2 = change.next2;
                    if (change.next2 != null)
                    {
                       change.next2.setArrayPos(change.next2.getArrayPos()+change.getArrayPos()-1);
                    }
                    return;
                }
                lastChange = change;
                change = change.next2;
            }
            Change newChange;
            if (change == null)
            {
                newChange = new Change(prop, copiedValue, true, arrayIndex - currentIndex);
            }
            else
            {
                newChange = new Change(prop, copiedValue, true, arrayIndex - currentIndex +
                    change.getArrayPos());
                change.setArrayPos(change.getArrayPos() - newChange.getArrayPos() - 1);
                newChange.next2 = change;
            }
            lastChange.next2 = newChange;
        }
        else
        {
            // For multi-valued properties, we have to record that one property
            // was deleted so all the array indexes are shifted
            int hashCode = hashCode(prop, chgs.length);
            Change change = chgs[hashCode];
            Change lastChange = null;
            while (change != null)
            {
                if (change.getProperty() == prop)
                    break;
                lastChange = change;
                change = change.next;
            }
            if (change == null)
            {
                change = new Change(prop, copiedValue, true, arrayIndex);
                change.next = chgs[hashCode];
                chgs[hashCode] = change;
            }
            else
            {
                int currentIndex = 0;
                Change lastChange2 = null;
                while (change != null)
                {
                    currentIndex += change.getArrayPos();
                    if (arrayIndex < currentIndex)
                        break;
                    else if (arrayIndex == currentIndex && !change.isSet())
                    {
                        // Deletion of an object that was inserted eariler;
                        deletedObjects.remove(copiedValue);
                        insertedObjects.remove(oldValue);
                        if (lastChange2 != null)
                            lastChange2.next2 = change.next2;
                        else if (lastChange != null)
                        {
                            if (change.next2 != null)
                            {
                                lastChange.next = change.next2;
                                change.next2.next = change.next;
                                change.next2.setArrayPos(change.next2.getArrayPos() +
                                    change.getArrayPos() - 1);
                            }
                            else
                                lastChange.next = change.next;
                        }
                        else
                        {
                            if (change.next2 != null)
                            {
                                chgs[hashCode] = change.next2;
                                change.next2.next = change.next;
                                change.next2.setArrayPos(change.next2.getArrayPos() +
                                    change.getArrayPos() - 1);
                            }
                            else
                                chgs[hashCode] = change.next;
                        }
                        return;
                    }
                    lastChange2 = change;
                    change = change.next2;
                }
                if (lastChange2 == null)
                {
                    // Deleted entry is the first
                    assert change != null;
                    Change newChange = new Change(prop, copiedValue, true, arrayIndex);
                    newChange.next = change.next;
                    if (lastChange == null)
                    {
                        chgs[hashCode] = newChange;
                    }
                    else
                    {
                        lastChange.next = newChange;
                    }
                    newChange.next2 = change;
                    change.setArrayPos(change.getArrayPos() - arrayIndex - 1);
                }
                else
                {
                    Change newChange;
                    if (change == null)
                    {
                        newChange = new Change(prop, copiedValue, true, arrayIndex - currentIndex);
                    }
                    else
                    {
                        newChange = new Change(prop, copiedValue, true, arrayIndex - currentIndex +
                            change.getArrayPos());
                        change.setArrayPos(change.getArrayPos() - newChange.getArrayPos() - 1);
                        newChange.next2 = change;
                    }
                    lastChange2.next2 = newChange;
                }
            }
        }
    }

    /**
     * Heuristic to figure out the optimum property hashmap size for keeping changes
     * @param dobj the object for which we track changes
     * @return the maximum expected number of modified properties
     */
    public static int computeHashMapSize(DataObject dobj)
    {
        if (dobj.getSequence() != null)
            return 1; // For sequences, everything is kept into one big array so no hashmap
        if (dobj.getType().isOpen())
            return 10; // Just a generic number, since it's impossible to predit how many open
                       // content properties will be added or removed
        int prediction = dobj.getType().getProperties().size() * 3 / 4; // Prediction: 3/4 of the number of
                       // properties will be modified
        return prediction > 0 ? prediction : 1;
    }

    public static int hashCode(Property prop, int length)
    {
        int result = prop.hashCode() % length;
        return result > 0 ? result : -result;
    }

    /*
     * Return the change corresponding to the sequence, creating it if necessary
     */
    private static Change getcreateSequenceChange(Change[] chgs)
    {
        int hashCode = hashCode(SEQUENCE, chgs.length);
        Change change = chgs[hashCode];
        Change lastChange = change;
        for (; change != null && change.property != SEQUENCE; change = change.next)
            lastChange = change;
        if (change != null)
            return change;
        else
        {
            change = new Change(SEQUENCE, null, false, -1);
            if (lastChange != null)
                lastChange.next = change;
            else
                chgs[hashCode] = change;
            return change;
        }
    }

    /*
     * Return the first change in the sequence, null if not found
     */
    public static Change getFirstSequenceChange(Change[] chgs)
    {
        Change change = chgs[hashCode(SEQUENCE, chgs.length)];
        for (; change != null && change.property != SEQUENCE; change = change.next);
        return change == null ? null : change.next2;
    }

    // =================================================================
    // Path and IDREF-related methods
    // =================================================================
    public void buildOldIdMap(Map<String, DataObject> idMap)
    {
        // Iterate through all the changed objects and look for changes
        // on ID properties
        for (Map.Entry<DataObject, Change[]> entry : modifiedObjects.entrySet())
        {
            for (Change c : entry.getValue())
                while(c != null)
                {
                    if (((PropertyXML) c.getProperty()).getSchemaTypeCode() == SchemaType.BTC_ID &&
                        c.isSet())
                        idMap.put((String) c.getValue(), entry.getKey());
                    c = c.next;
                }
        }

        for (DataObject obj : deletedObjects.keySet())
        {
            List instanceProperties = obj.getInstanceProperties();
            for (int i = 0; i < instanceProperties.size(); i++)
            {
                PropertyXML prop = (PropertyXML) instanceProperties.get(i);
                SchemaType propType = prop.getTypeXML().getXMLSchemaType();
                if (propType != null && propType.getBuiltinTypeCode() == SchemaType.BTC_ID)
                    idMap.put(obj.getString(prop), obj);
            }
        }
    }

    public String getNodeId(DataObject node)
    {
        if (liveObjectDeletedObjectMapping.containsKey(node))
            node = liveObjectDeletedObjectMapping.get(node);
        List instanceProperties = node.getInstanceProperties();
        for (int i = 0; i < instanceProperties.size(); i++)
        {
            PropertyXML prop = (PropertyXML) instanceProperties.get(i);
            if (prop.getSchemaTypeCode() == SchemaType.BTC_ID)
                return node.getString(prop);
        }
        return null;
    }

    /**
     *
     * Returns the id of the specified deleted node.
     * Searches for the parent node in the deleted map and
     * returns null if any of the ancestors were themselves deleted
     */
    public String getNodePath(DataObject node, DataObject contextNode, DataObject rootNode,
        HashMap<DataObject, String>computedPaths, HashMap<DataObject, String> computedCSPaths,
        NamespaceStack nsstck, String pathToCSParent, String csUri, String csName,
        String rootUri, String rootName, boolean insideChangeSummary)
    {
        if (!insideChangeSummary && computedPaths.containsKey(node))
            return computedPaths.get(node);
        if (insideChangeSummary && computedCSPaths.containsKey(node))
            return computedCSPaths.get(node);
        // We are assuming that if a node was deleted, it is still in the current tree and
        // will compute the path of its current position in the tree rather than the path to
        // its deleted copy
//        if (liveObjectDeletedObjectMapping.containsKey(node))
//            node = liveObjectDeletedObjectMapping.get(node);
        boolean deleted = deletedObjects.containsKey(node);
        StringBuilder sb = new StringBuilder();
        DataObject parent;
        PropertyXML prop = ((DataObjectXML) node).getContainmentPropertyXML();
        if (deleted)
        {
            parent = deletedObjects.get(node);
            String pathToParent = getNodePath(parent, contextNode, rootNode,
                computedPaths, computedCSPaths, nsstck,
                pathToCSParent, csUri, csName, rootUri, rootName, true);
            if (pathToParent == null)
                return null;
            sb.append(pathToParent);
            if (pathToParent.length() > 1)
                sb.append(PATH_SEPARATOR);
            sb.append(XmlPath.getName(prop, nsstck));
            if (prop.isMany())
            {
                if (parent.getSequence() == null)
                {
                    // We need to compute an index
                    // The index that we need is in this case an index
                    // in the _original_ array, but what we keep are
                    // indexes in the _current_ array, so we need to
                    // adjust it
                    Change[] modifications = modifiedObjects.get(parent);
                    assert modifications != null;
                    int offset = 0;
                    int arrayIndex = -1;
                    for (Change change = modifications[hashCode(prop, modifications.length)];
                        change != null; change = change.next)
                        if (prop == change.getProperty())
                            while (change != null)
                            {
                                offset += change.getArrayPos();
                                if (node == change.getValue() && change.isSet())
                                {
                                    arrayIndex = offset;
                                    break;
                                }
                                else if (change.isSet())
                                {
                                    // We have a deletion of an item other
                                    // than this one, meaning that we need
                                    // to make room for an extra item
                                    offset++;
                                }
                                else
                                {
                                    // We have an insertion, meaning that
                                    // the original doc did not have an
                                    // item in this place
                                    offset--;
                                }
                                change = change.next2;
                            }
                    assert arrayIndex >= 0;
                    sb.append(OPEN_BRACKET);
                    sb.append(arrayIndex + 1);
                    sb.append(CLOSE_BRACKET);
                }
                else
                {
                    // The element is part of a sequence
                    // Count the number of elements with the same name currently
                    // in the sequence, then substract the number of elements
                    // with the same name that were added and add the number
                    // of elements with the same name that were deleted.
                    // The result is the element's index in the original array.
                    Change[] modifications = modifiedObjects.get(parent);
                    assert modifications != null;
                    int count = 0;
                    int currentIndex = 0;
                    Change change = null;
                    for (Change c = getFirstSequenceChange(modifications); c != null; c = c.next2)
                    {
                        PropertyXML p = (PropertyXML) c.getProperty();
                        currentIndex += c.getArrayPos();
                        if (sameQName(prop, p))
                        {
                            if (node == c.getValue())
                            {
                                change = c;
                                break;
                            }
                            if (c.isSet())
                                // Deletion
                                count++;
                            else
                                // Insertion
                                count--;
                        }
                    }
                    assert change != null : "Deleted object has to appear in the list of changes";
                    Sequence s = parent.getSequence();
                    for (int i = 0; i < currentIndex; i++)
                    {
                        PropertyXML p = (PropertyXML) s.getProperty(i);
                        if (p != null && sameQName(prop, p) && p.isXMLElement())
                            count++;
                    }
                    assert count >= 0;
                    sb.append(OPEN_BRACKET);
                    sb.append(count + 1);
                    sb.append(CLOSE_BRACKET);
                }
            }
        }
        else if (insideChangeSummary)
        {
            int index = 1;
            for (Map.Entry<DataObject, Change[]> entry : modifiedObjects.entrySet())
            {
                DataObject d = entry.getKey();
                if (d == node)
                {
                    StringBuilder csPath;
                    if (computedCSPaths.containsKey(null))
                    {
                        String s = computedCSPaths.get(null);
                        assert s != null;
                        csPath = new StringBuilder(s);
                    }
                    else
                    {
                        csPath = new StringBuilder(pathToCSParent);
                        csPath.append(XmlPath.PATH_SEPARATOR);
                        String prefix = nsstck.ensureMapping(csUri, null, false, false);
                        if (prefix != null)
                            csPath.append(prefix).append(':').append(csName);
                        else
                            csPath.append(csName);
                        computedCSPaths.put(null, csPath.toString());
                    }

                    String qualName = node == rootNode ? XmlPath.getName(rootUri, rootName, nsstck) :
                        XmlPath.getName(prop, nsstck);
                    csPath.append(XmlPath.PATH_SEPARATOR).append(qualName).
                        append(XmlPath.OPEN_BRACKET).append(index).append(XmlPath.CLOSE_BRACKET);
                    String result = csPath.toString();
                    computedCSPaths.put(node, result);
                    return result;
                }
                PropertyXML p = ((DataObjectXML) d).getContainmentPropertyXML();
                if (p == null ? sameQName(rootUri, rootName, prop) : sameQName(p, prop))
                    index++;
            }
            throw new IllegalStateException("DataObject + " + node.toString() + " is not recorded "+
                "in the change summary");
        }
        else if (node.getContainer() == null || node == rootNode)
        {
            // We are at the root
            PropertyXML rootContainer = ((DataObjectXML) node).getContainmentPropertyXML();
            sb.append(PATH_SEPARATOR);
            if (rootContainer == null)
                sb.append(XmlPath.getName(rootUri, rootName, nsstck));
            else
                sb.append(XmlPath.getName(rootContainer, nsstck));
        }
        else if (node == contextNode)
            return "";
        else if (rootNode != null && node.getRootObject() != rootNode.getRootObject())
            return null; // the node is no longer in the current tree
        else
        {
            // This node is in the current document
            // Compute its path
            parent = node.getContainer();
            String path = getNodePath(parent, contextNode, rootNode,
                computedPaths, computedCSPaths, nsstck, pathToCSParent, csUri, csName,
                rootUri, rootName, false);
            if (path == null)
                return null;
            sb.append(path);
            if (path.length() > 0)
                sb.append(PATH_SEPARATOR);
            sb.append(XmlPath.getName(prop, nsstck));
            if (prop.isMany())
            {
                List values = parent.getList(prop);
                int i = 0;
                for (; i < values.size(); i++)
                    if (node == values.get(i))
                        break;
                assert i < values.size();
                sb.append(OPEN_BRACKET);
                sb.append(i + 1);
                sb.append(CLOSE_BRACKET);
            }
        }
        String result = sb.toString();
        computedPaths.put(node, result);
        return result;
    }

    private static boolean sameQName(PropertyXML p1, PropertyXML p2)
    {
        if (p1.getXMLNamespaceURI() == null)
            return p2.getXMLNamespaceURI() == null && p1.getXMLName().equals(p2.getXMLName());
        else
            return p1.getXMLNamespaceURI().equals(p2.getXMLNamespaceURI()) &&
                p1.getXMLName().equals(p2.getXMLName());
    }

    private static boolean sameQName(String namespaceURI, String localName, PropertyXML p)
    {
        if (namespaceURI == null)
            return p.getXMLNamespaceURI() == null && localName.equals(p.getXMLName());
        else
            return namespaceURI.equals(p.getXMLNamespaceURI()) &&
                localName.equals(p.getXMLName());
    }

    // ============================================================
    // Implementation-specific accessors
    // ============================================================
    public Map<DataObject, DataObject> getDeletedObjects()
    {
        return deletedObjects;
    }

    public Set<DataObject> getInsertedObjects()
    {
        return insertedObjects;
    }

    public Map<DataObject, Change[]> getModifiedObjects()
    {
        return modifiedObjects;
    }

    // =============================================================
    // Implementation of ChangeSummaryXML
    // =============================================================
    public Iterator<ChangedObjectXML> getChangedObjectsIterator()
    {
        if (_csxml != null)
            return _csxml.getChangedObjectsIterator();
        else
        {
            // Return an empty iterator rather than throwing exception to reflect the fact
            // that we don't allow selections inside the change summary from the API
            // throw new UnsupportedOperationException();
            List <ChangedObjectXML> l = Collections.emptyList();
            return l.iterator();
        }
    }

    public void setChangeSummaryXMLDelegator(ChangeSummaryXML csxml)
    {
        _csxml = csxml;
    }

    public void deleteChangeSummaryXMLDelegator()
    {
        _csxml = null;
    }

    private ChangeSummaryXML _csxml;

    // ==============================================================
    // Reference counting and cleaning
    // ==============================================================
    public void addReferredObject(DataObject oldObject, Change newChange)
    {
        List<Change> l = referredObjects.get(oldObject);
        if (l == null)
        {
            l = new ArrayList<Change>();
            referredObjects.put(oldObject, l);
        }
        l.add(newChange);
    }

    private void replaceReferredObject(DataObject oldObject, DataObject newObject)
    {
        List<Change> changes = referredObjects.get(oldObject);
        if (changes == null)
            return;
        for (Change c : changes)
        {
            if (c.getValue() instanceof List)
            {
                // Search this object in the list and then replace it
                List<DataObject> list = (List<DataObject>) c.getValue();
                for (ListIterator<DataObject> it = list.listIterator(); it.hasNext(); )
                    if (it.next() == oldObject)
                        it.set(newObject);
            }
            else
                c.setValue(newObject);
        }
    }

    // ===============================================================
    // CopyHelper for deleted objects
    // ===============================================================
    private DataObject copyDeletedObject(DataObjectXML obj)
    {
        boolean logging = this.logging;
        this.logging = false;
        CopyHelperImpl.CopyContext ctx = new DeleteCopyContext(obj);
        DataObject result = ctx.copyObject(obj, null, obj.getContainmentPropertyXML());
        this.logging = logging;
        return result;
    }

    private class DeleteCopyContext extends CopyHelperImpl.CopyContext
    {
        DeleteCopyContext(DataObject deletedObject)
        {
            super(((DataObjectImpl)deletedObject).getSDOContext(), deletedObject);
        }

        public DataObjectImpl copyObject(DataObjectXML object,
            DataObjectImpl container, PropertyXML containingProperty)
        {
            // We need to not only copy this object, but also add it to the
            // deleted objects list (unless it was inserted earlier); also
            // after the copying is done, we need to revert all changes made
            // to it and remove the entry in the modified list
            DataObjectImpl result = super.copyObject(object, container, containingProperty);
            liveObjectDeletedObjectMapping.put(object, result);
            replaceReferredObject(object, result);
            if (modifiedObjects.containsKey(object))
            {
                Change[] changes = modifiedObjects.get(object);
                modifiedObjects.remove(object);
                // Now undo the changes on the result object by reading the
                // list of changes on the source object, since we can't undo the
                // changes on the source object before the copy
                // This is trickier than the normal undo
                undoObject(result, changes, this);
            }
            return result;
        }

        public DataObject copyReference(DataObjectXML referred, boolean hasOpposite)
        {
            // A reference can be to an object that's part of the copy tree
            // or to an object that was inserted or to an object that
            // was deleted or to an object that was in tree originally and still is
            DataObjectImpl copy = _objectMap.get(referred);
            if (copy != null)
                return copy;
            if (insertedObjects.contains(referred))
                return null;
            else if (liveObjectDeletedObjectMapping.containsKey(referred))
                return liveObjectDeletedObjectMapping.get(referred);
            else if (deletedObjects.containsKey(referred))
                return referred;
            else if (!sameCopyTree(referred))
                return referred; // TODO record the reference to be replaced later
            else
            {
                // Same thing, create the reference now
                copy = (DataObjectImpl) DataFactoryImpl.INSTANCE.
                    create(referred.getTypeXML());
                _objectMap.put(referred, copy);
                return copy;
            }
        }
    }

    public String toString()
    {
        StringWriter w = new StringWriter();
        w.write(super.toString());
        w.write("\nDeleted objects:\n");
        for (DataObject o : deletedObjects.keySet())
        {
            w.write(o.toString());
            w.write("\n");
        }
        w.write("Inserted objects: ");
        w.write(insertedObjects.size());
        w.write("\nModified objects:\n");
        for (Map.Entry<DataObject, Change[]> e : modifiedObjects.entrySet())
        {
            DataObject o = e.getKey();
            w.write(o.toString());
            w.write("\n");
            for (Change c : e.getValue())
                while (c != null)
                {
                    w.write("\t");
                    w.write(c.toString());
                    w.write("\n");
                    Change c2 = c.next2;
                    while (c2 != null)
                    {
                        w.write(" ");
                        w.write(c2.toString());
                        w.write(" ");
                        c2 = c2.next2;
                    }
                    c = c.next;
                }
        }
        return w.toString();
    }
}
