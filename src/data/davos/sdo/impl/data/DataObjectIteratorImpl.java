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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import davos.sdo.DataObjectXML;
import davos.sdo.ListXMLIterator;
import davos.sdo.PropertyXML;
import davos.sdo.SequenceXML;

public class DataObjectIteratorImpl implements DataObjectIterator
{
    // State
    private DataObjectXML dataObject;
    private ChangeSummaryImpl.Change[] changes;
    private ChangeSummaryImpl.Change change;
    private LinkedList<Change> buffer;
    // For the no-sequence case
    private Iterator<PropertyXML> propertyIterator;
    private ListXMLIterator valueIterator;
    private int offset;
    private PropertyXML currentProperty;
    // For the sequence case
    private SequenceXML sequence;
    private int sequenceIndex;

    public DataObjectIteratorImpl(DataObjectXML object)
    {
        dataObject = object;
        sequence = object.getSequenceXML();
        sequenceIndex = 0;
        ChangeSummaryImpl c = (ChangeSummaryImpl) object.getChangeSummary();
        if (c != null)
            changes = c.getModifiedObjects().get(dataObject);
        buffer = new LinkedList<Change>();
        if (sequence == null)
        {
            propertyIterator = object.getInstanceProperties().iterator();
            advanceProperty();
        }
        else
        {
            change = changes != null ? changes[0] : null;
            lookForDeletes();
        }
    }

    public boolean hasNext()
    {
        if (sequence == null)
            return !buffer.isEmpty() || currentProperty != null;
        else
            return !buffer.isEmpty() || sequenceIndex < sequence.size();
    }

    public Change next()
    {
        if (!hasNext())
            throw new NoSuchElementException();
        if (!buffer.isEmpty())
            return buffer.removeFirst();
        Change result;
        if (sequence == null)
        {
            if (valueIterator == null)
            {
                if (currentProperty.isMany() && !currentProperty.getType().isDataType() &&
                    currentProperty.isContainment())
                    // The valueIterator should not have been null
                    throw new IllegalStateException();

                if (currentProperty.isMany())
                {
                    // Because this is a many-valued property of simple type, we need to
                    // actually create an event for each value, because this is how it would
                    // appear when marshalled to XML
                    if (change == null)
                    {
                        // At this point, we now for sure that the list is not empty, this is
                        // verified in advanceProperty
                        ListXMLIterator temp = dataObject.getListXMLIterator(currentProperty);
                        temp.next();
                        result = new Change(temp.getSubstitution());
                        result.setValue(temp.getValue());
                        result.setChangeType(Change.SAME);
                        while (temp.next())
                        {
                            Change c = new Change(temp.getSubstitution());
                            c.setValue(temp.getValue());
                            c.setChangeType(Change.SAME);
                            buffer.add(c);
                        }
                    }
                    else
                    {
                        List oldValues = (List) change.getValue();
                        // This is a difficult problem: given two lists, find out what the
                        // differences are and where the common items are
                        // Because one of the lists (current values) is accessed via an iterator
                        // and the other (current value) via a random-access list, we employ the
                        // following algorithm:
                        // Walk the "current values" list. For each "current value", check to see
                        // if it shows up in the old list (to avoid N-square complexity, we use a
                        // maximum lookahead of 10). If it does, it is a "SAME" value and all the
                        // values in the "old values" list (if any) are considered "OLD". If it
                        // doesn't, then it is a "NEW".
                        ListXMLIterator temp = dataObject.getListXMLIterator(currentProperty);
                        int j = 0;
                        result = null;
                        while (temp.next())
                        {
                            Object value = temp.getValue();
                            int i = findValue(value, oldValues, j);
                            if (i >= j)
                            {
                                // found
                                for (; j < i; j++)
                                {
                                    // Add old value
                                    Change c = new Change(currentProperty);
                                    c.setValue(oldValues.get(j));
                                    c.setChangeType(Change.OLD);
                                    if (result == null)
                                        result = c;
                                    else
                                        buffer.add(c);
                                }
                                Change c = new Change(temp.getSubstitution());
                                c.setValue(temp.getValue());
                                c.setChangeType(Change.SAME);
                                if (result == null)
                                    result = c;
                                else
                                    buffer.add(c);
                                j++; // skip this change in the oldValues list
                            }
                            else
                            {
                                // not found: new value
                                Change c = new Change(temp.getSubstitution());
                                c.setValue(temp.getValue());
                                c.setChangeType(Change.NEW);
                                if (result == null)
                                    result = c;
                                else
                                    buffer.add(c);
                            }
                        }
                        for (; j < oldValues.size(); j++)
                        {
                            // There are some deletions at the end
                            Change c = new Change(currentProperty);
                            c.setValue(oldValues.get(j));
                            c.setChangeType(Change.OLD);
                            if (result == null)
                                result = c;
                            else
                                buffer.add(c);
                        }
                        if (result == null)
                            throw new IllegalStateException(); // this should be guarded against in advanceProperty()
                    }
                }
                else
                {
                    // Return the value
                    boolean set = dataObject.isSet(currentProperty);
                    if (set)
                    {
                        ListXMLIterator temp = dataObject.getListXMLIterator(currentProperty);
                        // Since the value is set, temp must not be empty
                        temp.next();
                        result = new Change(temp.getSubstitution());
                        result.setValue(/*getBoxedValue(currentProperty)); not sure if it's needed*/
                            dataObject.get(currentProperty));
                        if (change == null)
                            result.setChangeType(Change.SAME);
                    }
                    else
                        result = null;
                    if (change != null)
                    {
                        // For DataObject values, modification will result in (potentially)
                        // two changes: a delete and an insert; for datatype values, just
                        // one change: a modification
                        if (!currentProperty.getType().isDataType() && !change.isSet() &&
                                change.next2 != null)
                                change = change.next2;
                        Change modification = new Change(currentProperty);
                        modification.setValue(change.getValue());
                        modification.setChangeType(Change.OLD);
                        if (result == null)
                        {
                            if (change.isSet())
                                result = modification;
                        }
                        else
                        {
                            result.setChangeType(Change.NEW);
                            if (change.isSet())
                            {
                                // We have an INSERT followed by a DELETE; as per the contract,
                                // send the DELTE FIRST and then the INSERT
                                buffer.add(result);
                                result = modification;
                            }
                        }
                    }
                    if (result == null)
                        throw new IllegalStateException("If the property \"" + currentProperty.
                            getName() + "\" is not set and the corresponding change is also unset," +
                            "advanceProperty() should not have stopped on it");
                }
                advanceProperty();
            }
            else
            {
                // valueIterator is already positioned at the correct value
                Object value = valueIterator.getValue();
                result = new Change(valueIterator.getSubstitution());
                result.setValue(value);
                result.setChangeType(Change.SAME);
                while (change != null && change.getArrayPos() == offset)
                {
                    if (!change.isSet())
                        result.setChangeType(Change.NEW);
                    else
                    {
                        Change delete = new Change(currentProperty);
                        delete.setValue(change.getValue());
                        delete.setChangeType(Change.OLD);
                        buffer.add(delete);
                    }
                    offset = 0;
                    change = change.next2;
                }
                // Look for deletes at this position
                offset++;
                lookForDeletes2();
                // We are done now, check if there are any more items in the array
                if (!valueIterator.next())
                {
                    valueIterator = null;
                    advanceProperty();
                }
            }
        }
        else
        {
            PropertyXML property = sequence.getSubstitution(sequenceIndex);
            Object value = sequence.getValue(sequenceIndex);
            result = new Change(property);
            result.setValue(value);
            result.setChangeType(Change.SAME);
            while (change != null && change.getArrayPos() == offset)
            {
                if (!change.isSet())
                {
                    // Looks like this entry is a new addition
                    result.setChangeType(Change.NEW);
                }
                else
                {
                    Change delete = new Change((PropertyXML) change.getProperty());
                    delete.setValue(change.getValue());
                    delete.setChangeType(Change.OLD);
                    buffer.add(delete);
                }
                offset = 0;
                change = change.next;
            }
            offset++;
            lookForDeletes();
            sequenceIndex++;
        }
        return result;
    }

    public void remove()
    {
        throw new UnsupportedOperationException();
    }

    private ChangeSummaryImpl.Change findCurrentChange()
    {
        if (changes == null)
            return null;
        int hashCode = ChangeSummaryImpl.hashCode(currentProperty, changes.length);
        for (ChangeSummaryImpl.Change c = changes[hashCode]; c != null; c = c.next)
            if (c.getProperty() == currentProperty)
                return c;
        return null;
    }

    /**
     * Updates currentProperty, valueIterator, change, offset
     * Can add to buffer
     */
    private void advanceProperty()
    {
        while (propertyIterator.hasNext())
        {
            currentProperty = propertyIterator.next();
            change = findCurrentChange();
            offset = 0;
            // If the property is not set (or set to an empty array for multivalued properties)
            // and there is no change associated to it, then we skip it
            if (currentProperty.isMany() && currentProperty.isContainment() &&
                !currentProperty.getType().isDataType())
            {
                valueIterator = dataObject.getListXMLIterator(currentProperty);
                lookForDeletes2();
                if (!valueIterator.next())
                {
                    valueIterator = null;
                    if (change != null && change.isSet())
                        // lookForDeletes2() above should have taken care of all deletes
                        throw new IllegalStateException();
                    continue;
                }
            }
            else
            {
                if (!dataObject.isSet(currentProperty) && (change == null || !change.isSet()))
                    continue;
                if (currentProperty.isMany() && dataObject.getList(currentProperty).size() == 0)
                {
                    if (change == null || !change.isSet())
                        continue;
                    // Even if there is a change, the list in that change can still have size 0
                    if (((List) change.getValue()).size() == 0)
                        continue;
                }
            }
            return;
        }
        currentProperty = null;
    }

    /**
     * Updates change and offset and can add to buffer
     */
    private void lookForDeletes()
    {
        while (change != null && change.getArrayPos() == offset && change.isSet())
        {
            // Deletions at this position
            Change delete = new Change((PropertyXML) change.getProperty());
            delete.setValue(change.getValue());
            delete.setChangeType(Change.OLD);
            buffer.add(delete);
            offset = 0;
            change = change.next;
        }
    }

    /**
     * Updates change and offset and can add to buffer
     */
    private void lookForDeletes2()
    {
        while (change != null && change.getArrayPos() == offset && change.isSet())
        {
            // Deletions at this position
            Change delete = new Change((PropertyXML) change.getProperty());
            delete.setValue(change.getValue());
            delete.setChangeType(Change.OLD);
            buffer.add(delete);
            offset = 0;
            change = change.next2;
        }
    }

    private static final int LOOKAHEAD = 10;
    private static int findValue(Object value, List oldValues, int start)
    {
        int n = oldValues.size();
        if (value == null)
        {
            for (int i = start; i < n && i < LOOKAHEAD + start; i++)
                if (oldValues.get(i) == null)
                    return i;
        }
        else
        {
            for (int i = start; i < n && i < LOOKAHEAD + start; i++)
                if (value.equals(oldValues.get(i)))
                    return i;
        }
        return -1;
    }
}
