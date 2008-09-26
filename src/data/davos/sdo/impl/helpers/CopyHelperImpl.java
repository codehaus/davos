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

import javax.sdo.helper.CopyHelper;
import javax.sdo.DataObject;
import javax.sdo.Type;
import javax.sdo.Property;
import davos.sdo.impl.data.DataObjectImpl;
import davos.sdo.impl.data.ChangeSummaryImpl;
import davos.sdo.impl.type.BuiltInTypeSystem;
import davos.sdo.SequenceXML;
import davos.sdo.PropertyXML;
import davos.sdo.DataObjectXML;
import davos.sdo.SDOContext;

import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Set;
import java.util.Map;

/**
 * Created
 * Date: Sep 7, 2006
 * Time: 3:48:01 PM
 */
public class CopyHelperImpl implements CopyHelper
{
    private SDOContext _sdoContext;

    public CopyHelperImpl(SDOContext sdoContext)
    {
        _sdoContext = sdoContext;
    }

    public DataObject copyShallow(DataObject dataObject)
    {
        DataObjectImpl result = (DataObjectImpl) _sdoContext.getDataFactory().
            create(dataObject.getType());
        DataObjectXML dataObjectXML = (DataObjectXML) dataObject;
        // Set the containing property
        result.setContainmentProperty(dataObjectXML.getContainmentPropertyXML());
        copyImpl(dataObjectXML, result, false, null);
        return result;
    }

    /**
     * This copies the source DataObject deeply, according to the algortihm in
     * the SDO spec. It also copies any change summary if present, if part of
     * the given object
     * @param dataObject the source DataObject to copy
     * @return the copy
     */
    public DataObject copy(DataObject dataObject)
    {
        CopyContext ctx = new CopyContext(_sdoContext, dataObject);
        DataObjectXML dataObjectXML = (DataObjectXML) dataObject;
        // Set the containing property
        DataObjectImpl result = ctx.copyObject(dataObjectXML, null, null);
        result.setContainmentProperty(dataObjectXML.getContainmentPropertyXML());
        return result;
    }

    /**
     * This is just like marshalling except that instead of writing the properties
     * to XML, we write them to another DataObject.
     * One other important difference is that the difference between attributes
     * and elements is irrelevant for the purpose of copying, which is why
     * @param src the object to copy
     * @param dest the object to copy to
     * @param deepCopy whether non-datavalue children are to be copied
     * @param ctx the copy context
     */
    private static void copyImpl(DataObjectXML src, DataObjectImpl dest,
        boolean deepCopy, CopyContext ctx)
    {
        SequenceXML seq = src.getSequenceXML();
        ChangeSummaryImpl cs = null;
        // When copying opposite properties the two ends are copied separately, so we need to
        // disable automatic setting of bidirectional properties
        dest.setOppositeIgnore(true);
        List props = src.getInstanceProperties();
        int size = props.size();
        boolean sequenced = seq != null;
        for (int i = 0; i < size; i++)
        {
            PropertyXML p = (PropertyXML) props.get(i);
            if (!src.isSet(p))
                continue;
            Object value = src.get(p);
            Type t = p.getTypeXML();
            boolean attr = !p.isXMLElement();
            if (t == BuiltInTypeSystem.CHANGESUMMARYTYPE)
            {
                if (!sequenced)
                {
                    // Properties of type ChangeSummary get initialized when the object is created
                    ChangeSummaryImpl newCS = (ChangeSummaryImpl) dest.get(p);
                    if (deepCopy)
                        copyChangeSummary((ChangeSummaryImpl) value, newCS, ctx);
                    // If the src ChangeSummary has logging on, we need to set logging to on on the
                    // copy ChangeSummary as well, but only after the copy is finished
                    if (((ChangeSummaryImpl) value).isLogging())
                        cs = newCS;
                }
            }
            else if (t.isDataType())
            {
                if (attr || !sequenced)
                {
                    if (!p.isReadOnly())
                        dest.set(p, copySimpleValue(value));
                    else
                        dest.getStore().storeSet(p, copySimpleValue(value));
                }
            }
            else if (!deepCopy)
                ;
            else if (p.isContainment())
            {
                if (!sequenced)
                    if (p.isMany())
                    {
                        List<DataObjectXML> list = (List<DataObjectXML>) value;
                        List<DataObjectXML> result = new ArrayList<DataObjectXML>(list.size());
                        for (DataObjectXML object : list)
                        {
                            DataObjectImpl copy = ctx.copyObject(object, dest, p);
                            result.add(copy);
                        }
                        if (!p.isReadOnly())
                            dest.setList(p, result);
                        else
                            dest.getStore().storeSet(p, result);
                    }
                    else
                    {
                        DataObjectXML object = (DataObjectXML) value;
                        DataObjectImpl copy = ctx.copyObject(object, dest, p);
                        if (!p.isReadOnly())
                            dest.setDataObject(p, copy);
                        else
                            dest.getStore().storeSet(p, copy);
                    }
            }
            else
            {
                if (attr || !sequenced)
                    if (p.isMany())
                    {
                        List<DataObject> list = new ArrayList<DataObject>();
                        for (Object o : (List) value)
                        {
                            DataObjectXML referred = (DataObjectXML) o;
                            DataObject copy = ctx.copyReference(referred, p.getOppositeXML() != null);
                            if (copy != null) // Doesn't make sense to include null refs in the list
                                list.add(copy);
                        }
                        if (!p.isReadOnly())
                            dest.setList(p, list);
                        else
                            dest.getStore().storeSet(p, list);
                    }
                    else
                    {
                        DataObjectXML referred = (DataObjectXML) value;
                        DataObject copy = ctx.copyReference(referred, p.getOppositeXML() != null);
                        if (copy != null || p.isNullable())
                        {
                            if (!p.isReadOnly())
                                dest.setDataObject(p, copy);
                            else
                                dest.getStore().storeSet(p, copy);
                        }
                        // Otherwise, don't bother to try to set it
                    }
            }
        }
        if (seq != null)
        {
            // Use the Sequence to populate the children
            SequenceXML dSeq = dest.getSequenceXML();
            assert dSeq != null : "When copying a sequenced object " +
                "the destination must also be sequenced";
            for (int i = 0; i < seq.size(); i++)
            {
                PropertyXML p = seq.getPropertyXML(i);
                if (p == null)
                {
                    dSeq.addText((String) seq.getValue(i));
                    continue;
                }
                Type t = p.getTypeXML();
                if (t == BuiltInTypeSystem.CHANGESUMMARYTYPE)
                {
                    // Properties of type ChangeSummary get initialized when the object is created
                    ChangeSummaryImpl newCS = (ChangeSummaryImpl) dest.get(p);
                    if (deepCopy)
                        copyChangeSummary((ChangeSummaryImpl) seq.getValue(i), newCS, ctx);
                    // If the src ChangeSummary has logging on, we need to set logging to on on the
                    // copy ChangeSummary as well, but only after the copy is finished
                    if (((ChangeSummaryImpl) seq.getValue(i)).isLogging())
                        cs = newCS;
                }
                else if (t.isDataType())
                {
                    if (!p.isReadOnly())
                        dSeq.add(p, copySimpleValue(seq.getValue(i)));
                    else
                        dest.getStore().storeAddNew(p, copySimpleValue(seq.getValue(i)));
                }
                else if (!deepCopy)
                {
                    // Don't copy it
                }
                else if (p.isContainment())
                {
                    DataObjectXML object = (DataObjectXML) seq.getValue(i);
                    DataObjectImpl copy = ctx.copyObject(object, dest, p);
                    if (!p.isReadOnly())
                        dSeq.add(p, copy);
                    else
                        dest.getStore().storeAddNew(p, copy);
                }
                else
                {
                    // It's an object reference
                    // If the reference is in the current "copy tree", then
                    // point to the copied object, if it is in the same tree
                    // keep pointing to the same object, if it's outside the
                    // tree, then set it to null
                    // Note that this could potentially get the user into trouble
                    // if he decides to then marshal the result, since the copied
                    // object is not part of the "root tree" of the original
                    // unless the user sets it himself
                    Object value = seq.getValue(i);
                    // To be as generic as possible we handle both the case
                    // in which array of references are represented as a List
                    // value and the case when they are represented as separate
                    // entries at different indexes in the Sequence
                    if (value instanceof List)
                    {
                        List<DataObject> list = new ArrayList<DataObject>();
                        for (Object o : (List) value)
                        {
                            DataObjectXML referred = (DataObjectXML) o;
                            DataObject copy = ctx.copyReference(referred, p.getOppositeXML() != null);
                            if (copy != null) // Doesn't make sense to add null refs in the list
                                list.add(copy);
                        }
                        if (!p.isReadOnly())
                            dSeq.add(p, list);
                        else
                            dest.getStore().storeAddNew(p, list);
                    }
                    else
                    {
                        DataObjectXML referred = (DataObjectXML) value;
                        // If the object has already been copied, add the copy
                        // as a reference here; this includes the case where
                        // the object was not part of the copy tree
                        DataObject copy = ctx.copyReference(referred, p.getOppositeXML() != null);
                        if (copy != null || p.isNullable())
                        {
                            if (!p.isReadOnly())
                                dSeq.add(p, copy);
                            else
                                dest.getStore().storeAddNew(p, copy);
                        }
                        // Otherwise, don't try to set it
                    }
                }
            }
        }
        else
        {
        }
        if (cs != null)
        {
            // The 'cs' is set to the copied ChangeSummary only when logging on the
            // source ChangeSummary was on
            cs.setLogging(true);
        }
        dest.setOppositeIgnore(false);
    }

    /**
     * This class abstract the behaviour of copying a containment node or
     * a reference node
     */
    public static class CopyContext
    {
        protected SDOContext _sdoContext;
        protected HashMap<DataObject, DataObjectImpl> _objectMap;
        private DataObject _copyTreeRoot;

        /*
        * @param objectMap object map to be used to find the copy if already created earlier
        *     or put the copy in if created in this method
        * @param copyTreeRoot the root of the copy tree
        */
        public CopyContext(SDOContext sdoContext, DataObject copyTreeRoot)
        {
            _sdoContext = sdoContext;
            _objectMap = new HashMap<DataObject, DataObjectImpl>();
            _copyTreeRoot = copyTreeRoot;
        }

        /**
         * Creates or fills in the copy of an object
         * @param object the object to copy (the source object)
         * @param container the container of the copy
         * @param containingProperty the containment property of the copy
         * @return the copied object
         */
        public DataObjectImpl copyObject(DataObjectXML object,
            DataObjectImpl container,
            PropertyXML containingProperty)
        {
            if (object == null)
                return null;
            DataObjectImpl copy = _objectMap.get(object);
            if (copy == null)
            {
                copy = (DataObjectImpl) _sdoContext.getDataFactory().create(object.getTypeXML());
                _objectMap.put(object, copy);
            }

            // If the copy has its type DataGraphType, then everything is set up correctly
            // and we can't call init() because that will initialize the _changeSummary to the
            // wrong value
            if (object.getTypeXML() != BuiltInTypeSystem.DATAGRAPHTYPE)
                copy.init(object.getTypeXML(), null, container, containingProperty);

            copyImpl(object, copy, true, this);
            return copy;
        }

        /**
         * This method returns the copy of a given DataObject, if that DataObject
         * was already copied, otherwise it creates a new copy and returns it.
         * The <tt>objectMap</tt> is used to search for copies and is updated as
         * necessary
         * @param referred the reference to look for
         * @param hasOpposite
         * @return the referenced object (which will be created in this method if necessary)
         */
        public DataObject copyReference(DataObjectXML referred, boolean hasOpposite)
        {
            if (referred == null)
                return null;
            // If the object has already been copied, return that
            // reference; this includes the case where the object was not part
            // of the copy tree
            DataObjectImpl copy = _objectMap.get(referred);
            if (copy != null)
                return copy;
            else if (!sameCopyTree(referred))
            {
                if (hasOpposite)
                    return null;
                else
                {
                    // We add the same reference
                    _objectMap.put(referred, (DataObjectImpl) referred);
                    return referred;
                }
            }
            else
            {
                // We might as well create the reference if we do
                // not find it in the map already; since we have
                // already established that the reference is in this
                // copy tree, it means that it will be copied later
                copy = (DataObjectImpl) _sdoContext.getDataFactory().
                    create(referred.getTypeXML());
                _objectMap.put(referred, copy);
                return copy;
            }
        }

        protected boolean sameCopyTree(DataObject object)
        {
            // See if we can get from object to source by traversing the container field
            while (object != null)
            {
                if (_copyTreeRoot == object)
                    return true;
                object = object.getContainer();
            }
            return false;
        }
    }

    private static Object copySimpleValue(Object obj)
    {
        // In most cases, we can just return the reference, because in Java simple types are
        // immutable, but there are exceptions
        if (obj == null)
            return null;
        Class c = obj.getClass();
        if (c == byte[].class)
        {
            byte[] src = (byte[]) obj;
            byte[] dest = new byte[src.length];
            System.arraycopy(src, 0, dest, 0, src.length);
            return dest;
        }
        else if (List.class.isAssignableFrom(c))
        {
            return new ArrayList((List) obj);
        }
        else
            return obj;
    }

    private static void copyChangeSummary(ChangeSummaryImpl src, ChangeSummaryImpl dest,
        CopyContext ctx)
    {
        // What this basically comes down to is copying the three maps that the
        // change summary holds on to: the modified objects, the deleted objects
        // and the inserted objects
        Set<DataObject> destInserted = dest.getInsertedObjects();
        Map<DataObject, DataObject> destDeleted = dest.getDeletedObjects();
        Map<DataObject, ChangeSummaryImpl.Change[]> destModified = dest.getModifiedObjects();
        Set<DataObject> srcInserted = src.getInsertedObjects();
        Map<DataObject, DataObject> srcDeleted = src.getDeletedObjects();
        Map<DataObject, ChangeSummaryImpl.Change[]> srcModified = src.getModifiedObjects();

        // Copy the inserted map
        // These are references to objects in the tree that will be handled by
        // the copy routine, so we treat them as references
        for (DataObject dobj : srcInserted)
            destInserted.add(ctx.copyReference((DataObjectXML) dobj, false));

        Map<DataObject, DataObject> deletedMap = new HashMap<DataObject, DataObject>();
        // This was easy, now copy the deleted map; this maps objects to their former parents
        // The deleted object may have references to other objects in the source tree
        // It may even have references to other deleted objects, which is the tricky part.
        // This is why we need a "special" context that is able to resolve these references
        // in the source tree, while at the same time keeping references within the
        // deleted object separate from the rest
        for (Map.Entry<DataObject, DataObject> entry : srcDeleted.entrySet())
        {
            DataObjectXML srcObject = (DataObjectXML) entry.getKey();
            DataObjectXML srcParent = (DataObjectXML) entry.getValue();
            DataObjectImpl copyObject = new DeletedObjectCopyContext(ctx, srcObject).
                copyObject(srcObject, null, null);
            destDeleted.put(copyObject, ctx.copyReference(srcParent, false));
            deletedMap.put(srcObject, copyObject);
        }

        ChangeSummaryCopyContext cctx = new ChangeSummaryCopyContext(ctx, deletedMap);
        // Move on to the modified object map
        for (Map.Entry<DataObject, ChangeSummaryImpl.Change[]> entry : srcModified.entrySet())
        {
            ChangeSummaryImpl.Change[] sourceChanges = entry.getValue();
            ChangeSummaryImpl.Change[] copyChanges =
                new ChangeSummaryImpl.Change[sourceChanges.length];
            for (int i = 0; i < sourceChanges.length; i++)
            {
                ChangeSummaryImpl.Change chg = sourceChanges[i];
                if (chg != null)
                {
                    copyChanges[i] = copyChange(chg, cctx);
                    ChangeSummaryImpl.Change lastChg2 = copyChanges[i];
                    for (ChangeSummaryImpl.Change chg2 = chg.next2; chg2 != null; chg2 = chg2.next2)
                    {
                        lastChg2.next2 = copyChange(chg2, cctx);
                        lastChg2 = lastChg2.next2;
                    }
                    ChangeSummaryImpl.Change lastChg = copyChanges[i];
                    for (chg = chg.next; chg != null; chg = chg.next)
                    {
                        lastChg.next = copyChange(chg, cctx);
                        lastChg = lastChg.next;
                        lastChg2 = lastChg;
                        for (ChangeSummaryImpl.Change chg2 = chg.next2;chg2 != null;chg2 =chg2.next2)
                        {
                            lastChg2.next2 = copyChange(chg2, cctx);
                            lastChg2 = lastChg2.next2;
                        }
                    }
                }
            }
            destModified.put(ctx.copyReference((DataObjectXML) entry.getKey(), false), copyChanges);
        }
    }

    private static ChangeSummaryImpl.Change copyChange(ChangeSummaryImpl.Change chg,CopyContext ctx)
    {
        Object value = chg.getValue();
        Object copyValue;
        Property prop = chg.getProperty();
        Type type = prop.getType();
        if (value instanceof List)
        {
            List list = (List) value;
            List copyList = new ArrayList(list.size());
            for (Object value2 : list)
            {
                Object copyValue2;
                if (type.isDataType() || value2 == null)
                    copyValue2 = value2;
                else
                    copyValue2 = ctx.copyReference((DataObjectXML) value2, false);
                copyList.add(copyValue2);
            }
            copyValue = copyList;
        }
        else if (prop.getType().isDataType() || value == null)
            copyValue = value;
        else
            copyValue = ctx.copyReference((DataObjectXML) value, false);
        return new ChangeSummaryImpl.Change(prop, copyValue, chg.isSet(), chg.getArrayPos());
    }

    private static class ChangeSummaryCopyContext extends CopyContext
    {
        CopyContext _parentCtx;
        Map<DataObject, DataObject> _deletedMap;

        ChangeSummaryCopyContext(CopyContext ctx, Map<DataObject, DataObject> deletedMap)
        {
            super(ctx._sdoContext, null);
            _parentCtx = ctx;
            _deletedMap = deletedMap;
        }

        /*
         * If the reference that is being looked up is a reference to a deleted object,
         * satisfy it from the local map, otherwise delegate
         */
        public DataObject copyReference(DataObjectXML referred, boolean hasOpposite)
        {
            if (_deletedMap.containsKey(referred))
                return _deletedMap.get(referred);
            else
                return _parentCtx.copyReference(referred, hasOpposite);
        }

        public DataObjectImpl copyObject(DataObjectXML object, DataObjectImpl container, PropertyXML containingProperty)
        {
            return _parentCtx.copyObject(object, container, containingProperty);
        }

        protected boolean sameCopyTree(DataObject object)
        {
            return _parentCtx.sameCopyTree(object);
        }
    }

    private static class DeletedObjectCopyContext extends CopyContext
    {
        CopyContext _parentCtx;
        DataObject _deletedObject;

        DeletedObjectCopyContext(CopyContext ctx, DataObject deletedObject)
        {
            super(ctx._sdoContext, deletedObject);
            _parentCtx = ctx;
            _deletedObject = deletedObject;
        }

        /*
          If the reference the code is looking for
          is a reference outside of the deleted copy, resolve the reference
          against the supplied context
         */
        public DataObject copyReference(DataObjectXML referred, boolean hasOpposite)
        {
            if (referred.getRootObject() == _deletedObject)
                return super.copyReference(referred, hasOpposite);
            else
                return _parentCtx.copyReference(referred, hasOpposite);
        }
    }
}
