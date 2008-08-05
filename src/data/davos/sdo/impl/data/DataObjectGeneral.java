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

import davos.sdo.ListXMLIterator;
import davos.sdo.PropertyXML;
import davos.sdo.SequenceXML;
import davos.sdo.TypeXML;
import davos.sdo.impl.common.Common;
import davos.sdo.impl.type.PropertyImpl;
import davos.sdo.impl.util.PrimitiveCodes;
import javax.sdo.DataObject;
import javax.sdo.Property;

import java.util.AbstractSequentialList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.Collection;
import java.util.Iterator;


/**
 * @author Cezar Andrei (cezar dot andrei at gmail dot com)
 *         Date: Jan 13, 2006
 */
public class DataObjectGeneral
    extends DataObjectImpl
    implements DataObject, Store
{
    // DataObjectImpl implementation
    public Store getStore()
    {
        return this;
    }

    //Store implementation
    private static final int INITIAL_LENGTH = 10;
    private static final float GROWTH_RATE = 3;

    private Object[] _values;
    private PropertyXML[] _properties;          // can contain null for mixed content text values
    private PropertyXML[] _substitutions;       // must contain a global property or the same value as in _properties
    private String[] _prefixes;
    private int _size;
//    private Map<PropertyXML, SDOList> _lists;
    private int _version;                       // _version is incremented every time a change is made to this object

    public DataObjectGeneral()
    {
        _values        = new Object[INITIAL_LENGTH];
        _properties    = new PropertyXML[INITIAL_LENGTH];
        _substitutions = new PropertyXML[INITIAL_LENGTH];
        _prefixes      = new String[INITIAL_LENGTH];
        _size = 0;
//        _lists = new HashMap<PropertyXML, SDOList>();
    }

    private void ensureSpace(int needed)
    {
        if (_size+needed < _values.length)
            return;

        int newSize = (int)(_values.length * GROWTH_RATE);
        Object[] newValues = new Object[newSize];
        PropertyXML[] newProperties = new PropertyXML[newSize];
        PropertyXML[] newSubstitutions = new PropertyXML[newSize];
        String[] newPrefixes = new String[newSize];

        System.arraycopy(_values, 0, newValues, 0, _size);
        System.arraycopy(_properties, 0, newProperties, 0, _size);
        System.arraycopy(_substitutions, 0, newSubstitutions, 0, _size);
        System.arraycopy(_prefixes, 0, newPrefixes, 0, _size);

        _values        = newValues;
        _properties    = newProperties;
        _substitutions = newSubstitutions;
        _prefixes      = newPrefixes;
    }

    public Object storeGet(PropertyXML p)
    {
        if (p==null)
            return null;

        if (p.isMany())
        {
            return ensureList(p);
        }

        for (int i = 0; i < _size; i++)
        {
            Property property = _properties[i];
            if (property!=null && property.equals(p))
                return ensureCopyOfList(_values[i]);
        }

        // hadn't been set, return the default value
        Object defaultValue = p.getDefault();
        if (defaultValue!=null)
            return defaultValue;

        TypeXML propType = p.getTypeXML();
        if ( propType.isDataType() )
        {
            Class instanceClass = propType.getInstanceClass();
            if ( instanceClass == boolean.class)
                return Boolean.FALSE;
            if ( instanceClass == byte.class)
                return PrimitiveCodes.ZERO_BYTE;
            if ( instanceClass == char.class)
                return PrimitiveCodes.ZERO_CHARACTER;
            if ( instanceClass == double.class)
                return PrimitiveCodes.ZERO_DOUBLE;
            if ( instanceClass == float.class)
                return PrimitiveCodes.ZERO_FLOAT;
            if ( instanceClass == int.class)
                return PrimitiveCodes.ZERO_INTEGER;
            if ( instanceClass == long.class)
                return PrimitiveCodes.ZERO_LONG;
            if ( instanceClass == short.class)
                return PrimitiveCodes.ZERO_SHORT;
        }
        
        return null;
    }

    private Object ensureCopyOfList(Object value)
    {
        if (value instanceof List)
        {
            return new ArrayList((List)value);
        }
        return value;
    }

    private List<Object> ensureList(PropertyXML p)
    {
//        SDOList propertyList = _lists.get(p);
//        if (propertyList==null)
//        {
//            propertyList = new SDOList(this, p);
//            _lists.put(p, propertyList);
//        }
//
//        return propertyList;
        return new SDOList(this, p);
    }

    private static class SDOList<E>
        extends AbstractSequentialList<E>
        implements List<E>
    {
        private DataObjectGeneral _dataObjGen;
        private PropertyXML _property;

        SDOList(DataObjectGeneral dataObjGen, PropertyXML property)
        {
            _dataObjGen = dataObjGen;
            _property = property;
        }

        public int size()
        {
            return SDOListIterator.size(_dataObjGen, _property);
        }

        public boolean add(E o)
        {
            // since it has to cover opposites which are encapsulated in DataObjectImpl call DataObjectImpl.addNewProperty
            return _dataObjGen.addNewProperty(_property, o, null, _property);
        }

        public boolean addAll(int index, Collection<? extends E> c) 
        {
            boolean modified = false;
            int i = 0;
            for (Iterator it = c.iterator(); it.hasNext();)
            {
                E e = (E) it.next();
                this.add(index + i, e);
                modified = true;
                i++;
            }
            return modified;
        }

        // optimization to compare only one time per property and obj 
        public int lastIndexOf(Object o)
        {
            ListIterator<E> e = listIterator(0);
            if (o==null)
            {
                while (e.hasNext())
                if (e.next()==null)
                    return e.previousIndex();
            }
            else
            {
                while (e.hasNext())
                if (o.equals(e.next()))
                    return e.previousIndex();
            }
            return -1;
        }

        public void clear()
        {
            int i = _dataObjGen._size-1;
            while (i>=0)
            {
                if (_dataObjGen._properties[i] == _property) // using ==
                    _dataObjGen.sequenceUnset(_property, i);
                i--;
            }
        }

        public ListIterator<E> listIterator(int index)
        {
            ListIterator<E> li = new SDOListIterator<E>(_dataObjGen, _property);
            for ( int i = 0; i<index; i++)
            {
                assert li.hasNext();
                li.next();
            }
            return li;
        }

        private static class SDOListIterator<E>
            implements ListIterator<E>
        {
            // special values of _prevDogIndex and _nextDogIndex
            // 0 or more means available at that index
            private static int NOT_AVAILABLE = -1;

            private DataObjectGeneral _dataObjGen;
            private PropertyXML _property;
            private int _dogIndex;                  // index in DataObjectGeneral
            private int _index;                     // the index of this iterator
            private int _expectedVersion;

            SDOListIterator(DataObjectGeneral dataObjGen, PropertyXML property)
            {
                _dataObjGen = dataObjGen;
                _property = property;
                recordVersion();
                reset();
            }

            private static int size(DataObjectGeneral dataObjGen, Property listProp)
            {
                int size = 0;
                for (int i = 0; i < dataObjGen._size; i++)
                {
                    Property property = dataObjGen._properties[i];
                    if (property==listProp) // == is used
                        size++;
                }
                return size;
            }

            private void ensureVersion()
            {
                if (_expectedVersion==_dataObjGen._version)
                    return;
                else
                    throw new ConcurrentModificationException();
            }

            private void recordVersion()
            {
                _expectedVersion = _dataObjGen._version;
            }

            private int findNext()
            {
                int i = _dogIndex + 1;
                while(i<_dataObjGen._size)
                {
                    Property property = _dataObjGen._properties[i];
                    if (property==_property) // == is used
                    {
                        return i;
                    }
                    i++;
                }
                return NOT_AVAILABLE;
            }

            private int findPrev()
            {
                int i = _dogIndex - 1;
                while(i>=0)
                {
                    Property property = _dataObjGen._properties[i];
                    if (property==_property) // == is used
                    {
                        return i;
                    }
                    i--;
                }
                return NOT_AVAILABLE;
            }

            public boolean hasNext()
            {
                ensureVersion();

                int nextDogIndex = findNext();
                if (nextDogIndex==NOT_AVAILABLE)
                    return false;
                else
                    return true;
            }

            public E next()
            {
                ensureVersion();

                int nextDogIndex = findNext();
                if (nextDogIndex==NOT_AVAILABLE)
                    return null;

                // else there is one available
                _index++;
                _dogIndex = nextDogIndex;
                return (E)(_dataObjGen._values[_dogIndex]);
            }

            public boolean hasPrevious()
            {
                ensureVersion();

                int prevDogIndex = findPrev();
                if (prevDogIndex==NOT_AVAILABLE)
                    return false;
                else
                    return true;
            }

            public E previous()
            {
                ensureVersion();

                int prevDogIndex = findPrev();
                if (prevDogIndex==NOT_AVAILABLE)
                    return null;

                // else there is one available
                _index--;
                _dogIndex = prevDogIndex;
                return (E)(_dataObjGen._values[_dogIndex]);
            }

            public int nextIndex()
            {
                ensureVersion();

                return _index + 1;
            }

            public int previousIndex()
            {
                ensureVersion();

                return _index -1;
            }

            public void remove()
            {
                ensureVersion();
                ensureDogIndex();

                _dataObjGen.sequenceUnset(_property, _dogIndex);
                _dogIndex--;
                _index--;
                
                recordVersion();
            }

            public void set(Object o)
            {
                ensureVersion();
                ensureDogIndex();

                _dataObjGen.sequenceSet(_property, _dogIndex, o);

                recordVersion();
            }

            public void add(Object o)
            {
                ensureVersion();

                if ( hasNext() )
                {
                    int i = findNext();
                    _dataObjGen.sequenceAddNew(_property, i , o, null, _property);
                }
                else
                {
                    _dataObjGen.sequenceAddNew(_property, _dogIndex + 1, o, null, _property);
                }

                recordVersion();
            }

            private void reset()
            {
                _index = 0;
                _dogIndex = NOT_AVAILABLE;
            }

            private void ensureDogIndex()
            {
                if (_dogIndex == NOT_AVAILABLE)
                {
                    findNext();
                    if ( _dogIndex == NOT_AVAILABLE )
                        throw new IndexOutOfBoundsException("Index: " + _index + ", Size: " + size(_dataObjGen, _property));
                }
            }
        }
    }

    public int storeSequenceSize()
    {
        return _size;
    }

    public PropertyXML storeSequenceGetPropertyXML(int sequenceIndex)
    {
        if( sequenceIndex >= _size || sequenceIndex<0 )
            throw new IndexOutOfBoundsException("size: " + _size + " sequnceIndex: " + sequenceIndex );

        // can return null for mixed content text values
        return _properties[sequenceIndex];
    }

    public Object storeSequenceGetValue(int sequenceIndex)
    {
        if( sequenceIndex >= _size || sequenceIndex<0 )
            throw new IndexOutOfBoundsException("size: " + _size + " sequnceIndex: " + sequenceIndex );

        return _values[sequenceIndex];
    }

    public PropertyXML storeGetProperty(String propertyName)
    {
        List<PropertyXML> typeProps = getTypeXML().getPropertiesXML();
        for( PropertyXML prop : typeProps )
        {
            if ( prop.getName().equals(propertyName) )
                return prop;

            for ( String alias : (List<String>)prop.getAliasNames() )
            {
                if ( alias.equals(propertyName) )
                    return prop;
            }
        }

        for( int i=0 ; i<_size ; i++)
        {
            PropertyXML prop = _properties[i];

            if ( prop==null )
                continue;
            
            if ( prop.getName().equals(propertyName) )
                return prop;

            for ( String alias : (List<String>)prop.getAliasNames() )
            {
                if ( alias.equals(propertyName) )
                    return prop;
            }
        }
        return null;
    }

    public String storeSequenceGetXMLPrefix(int sequenceIndex)
    {
        return _prefixes[sequenceIndex];
    }

    public PropertyXML storeSequenceGetSubstitution(int sequenceIndex)
    {
        return _substitutions[sequenceIndex];
    }

    public boolean storeSet(PropertyXML property, Object value)
    {
        return storeSet(property, value, null, property);
    }

    public boolean storeSet(PropertyXML property, Object value, String prefix, PropertyXML substitution)
    {
        assert property!=null;

        if (property.isMany() && value instanceof List)
        {
            // no need to check for !nullable && value==null since instanceof returns null
            List values = (List)value;

            // make a copy of the list to avoid messing up collectionIndex in case of a detach
            int collectionIndex = 0, collectionSize = values.size();
            int i;
            Object itemValue;

            List copyOfValues = new ArrayList();
            copyOfValues.addAll(values);

            // before tracking changes detach all object to be set/added 
            if ( property.isContainment())
                for (Object copyOfValue : copyOfValues)
                {
                    detachValue(copyOfValue);
                }

            trackChangeInSetInsideList(values, property);

            // this is an optimization that avoids shuffling the values in the arrays
            // this optimization will not take place when values contains objects that are children of this object 
            collectionIndex = 0;

            for (i = 0; i < _size && collectionIndex<collectionSize; i++)
            {
                Property p = _properties[i];
                if (p.equals(property))
                {
                    _properties[i] = property;
                    // this is also used for keeping the namespaces for untyped case when
                    // two or more elements have the same local name but different uris
                    // todo check if this is untyped and avoid removing the namespace
                    _substitutions[i] = (substitution==null ? property : substitution);
                    _values[i] = copyOfValues.get(collectionIndex);
                    _prefixes[i] = prefix;
                    collectionIndex++;
                }
            }


            // if no items in list left but store has more items like this remove them
            storeUnset(property, i);

            // if there are remaining items in the list add them to the end
            for(; collectionIndex < collectionSize; )
            {
                itemValue = copyOfValues.get(collectionIndex);
                storeAddNew(property, itemValue);
                collectionIndex++;
            }

            return true;
        }
        else
        {
            // check nullable
            if (value==null && !property.isNullable())
                throw new IllegalArgumentException("Property '" + property + "' does not allow null values.'");

            if (getChangeSummary() != null && getChangeSummary().isLogging())
            {
                // IMPORTANT!
                // Maintain this change tracking code in sync with the following
                // part, which deals with making the modifications
                // This can't be refactored instead, because we are trying to
                // optimize the case where change tracking is not enabled
                ChangeSummaryImpl cs = (ChangeSummaryImpl) getChangeSummary();
                boolean sequence = getType().isSequenced();
                for (int i = 0; i < _size; i++)
                {
                    Property p = _properties[i];
                    if ( p!=null && p.equals(property))
                    {
                        if (property.getType().isDataType() || !property.isContainment())
                            cs.logModification(this, p, _values[i], true, sequence ? i : -1);
                        else
                        {
                            cs.logDeletion(this, p, sequence ? i : -1, (DataObject)_values[i]);
                            cs.logInsertion(this, p, sequence ? i : -1, (DataObject) value);
                        }
                        break;
                    }
                }
            }

            detachValueIfContainment(value, property);

            value = ensureCopyOfList(value);

            for (int i = 0; i < _size; i++)
            {
                Property p = _properties[i];
                if ( p!=null && p.equals(property))
                {
                    checkForContext(value);
                    checkForCycle(value, p);

                    _version++;
                    _properties[i] = property;
                    _substitutions[i] = (substitution==null ? property : substitution);
                    _values[i] = value;
                    _prefixes[i] = prefix;

                    setContainment(value, property);

                    return true;
                }
            }

            return storeAddNew(PropertyImpl.getPropertyXML(property), value);
        }
    }

    private void trackChangeInSetInsideList(List values, PropertyXML property) {
        if (getChangeSummary() != null && getChangeSummary().isLogging())
        {
            // IMPORTANT!
            // Maintain this change tracking code in sync with the following
            // part, which deals with making the modifications
            // This can't be refactored instead, because we are trying to
            // optimize the case where change tracking is not enabled
            ChangeSummaryImpl cs = (ChangeSummaryImpl) getChangeSummary();
            int valuesSize = values.size();
            if (getType().isSequenced())
            {
                int i,j;
                if (property.getType().isDataType() || !property.isContainment())
                {
                    for (i = 0, j = 0; i < _size && j < valuesSize; i++)
                    {
                        Property p = _properties[i];
                        if (property.equals(p))
                        {
                            cs.logModification(this, p, _values[i], true, i);
                            j++;
                        }
                    }
                    // Track the deleted items
                    // We do this here because storeSequenceUnset() does not do change tracking
                    for (; i < _size; i++)
                    {
                        Property p = _properties[i];
                        if (property.equals(p))
                            cs.logModification(this, p, _values[i], true, i, true);
                    }
                    // We do NOT log the additions here, because
                    // the update code calls into storeSequenceAddNew() which
                    // does its own change tracking
                }
                else
                {
                    for (i = 0, j = 0; i < _size && j < valuesSize; i++)
                    {
                        Property p = _properties[i];
                        if (property.equals(p))
                        {
                            cs.logDeletion(this, p, i, (DataObject)_values[i]);
                            cs.logInsertion(this, p, i, (DataObject) values.get(j));
                            j++;
                        }
                    }
                    // Deleted
                    for (; i < _size; i++)
                    {
                        Property p = _properties[i];
                        if (property.equals(p))
                            cs.logDeletion(this, p, i, (DataObject)_values[i]);
                    }
                    // Same comment applies as above
                }
            }
            else
            {
                if (property.getType().isDataType() || !property.isContainment())
                {
                    cs.logModification(this, property, this.getList(property), true, -1);
                }
                else
                {
                    // Delete all the elements in the list, then add
                    // some of them back, but also be careful that
                    // the subsequent call to storeSequenceAddNew will also add elements
                    int j = 0;
                    Property p = null;
                    for (int i = 0; i < _size; i++)
                    {
                        if (property.equals(_properties[i]))
                        {
                            if (p == null)
                                p = _properties[i];
                            cs.logDeletion(this, p, 0, (DataObject)_values[i]);
                            j++;
                        }
                    }
                    // Insert some back
                    if (p == null)
                        p = property;
                    for (int i = 0; i < j && i < valuesSize; i++)
                        cs.logInsertion(this, p, i, (DataObject) values.get(i));
                }
            }
        }
    }

    public static void clearContainer(Object value, Property property)
    {
        if (property!=null && property.isContainment() && value!=null)
            if ( value instanceof DataObjectImpl)
                ((DataObjectImpl)value).clearContainer();
    }

    public static void detachValueIfContainment(Object value, Property property)
    {
        if (property!=null && property.isContainment())
            detachValue(value);
    }

    // this method will check if the value to be set is attached
    // and if it is it will get it detached from its parent
    private static void detachValue(Object value)
    {
        if (value instanceof DataObject)
        {
            DataObject doValue = (DataObject)value;
            DataObject container = doValue.getContainer();
            if (container!=null)
            {
                doValue.detach();
            }
        }
    }

    // this method checks to see if the valueToBeSet isn't one of its parents
    private boolean checkForCycle(Object valueToBeSet, Property p)
    {
        if (valueToBeSet==null || !(valueToBeSet instanceof DataObject))
            return true;
        if (p==null)
            return true;
        if (!p.isContainment())
            return true;

        if (!(valueToBeSet instanceof DataObject) ||
            ((DataObject)valueToBeSet).getRootObject() != getRootObject() )
            return true;

        DataObject parent = getContainer();
        while (parent!=null)
        {
            if (parent==valueToBeSet)
                throw new IllegalArgumentException("Circular containment");

            parent = parent.getContainer();
        }
        return true;
    }

    // this method checks to see if the valueToBeSet is in the same context
    private boolean checkForContext(Object valueToBeSet)
    {
        if (valueToBeSet==null || !(valueToBeSet instanceof DataObject))
            return true;

        if ( getSDOContext()!=((DataObjectImpl)valueToBeSet).getSDOContext() )
            throw new IllegalArgumentException("Trying to set a DataObject created in a different context.");

        return true;
    }

    public boolean storeAddNew(PropertyXML property, Object value)
    {
        return storeAddNew(property, value, null, property);
    }

    public boolean storeAddNew(PropertyXML property, Object value, String prefix, PropertyXML substitution)
    {
        //assert property !=null;

        checkPropertyForAdd(getTypeXML(), property);

        // check nullable
        if (value==null && !property.isNullable())
            throw new IllegalArgumentException("Property '" + property + "' does not allow null values.'");

        if (getChangeSummary() != null && getChangeSummary().isLogging())
        {
            ChangeSummaryImpl cs = (ChangeSummaryImpl) getChangeSummary();
            if (getType().isSequenced())
            {
                if (property == null || property.getType().isDataType() || !property.isContainment())
                    cs.logModification(this, property, null, false, _size);
                else
                    cs.logInsertion(this, property, _size, (DataObject) value);
            }
            else if (property.getType().isDataType() || !property.isContainment())
                cs.logModification(this, property, null, false, -1);
            else if (property.isMany())
            {
                // Find the last index of the array to which we are appending
                int size = 0;
                for (int i = 0; i < _size; i++)
                    if (property.equals(_properties[i]))
                        size++;
                cs.logInsertion(this, property, size, (DataObject) value);
            }
            else
                cs.logInsertion(this, property, -1, (DataObject) value);
        }

        checkForContext(value);
        checkForCycle(value, property);

        detachValueIfContainment(value, property);

        return storeAddNewBasic(property, value, prefix, substitution);
    }

    public boolean storeAddNewBasic(PropertyXML property, Object value, String prefix, PropertyXML substitution)
    {
        assert checkPropertyForAdd(getTypeXML(), property);

        // check nullable
        assert !(value==null && !property.isNullable()) : "Property '" + property + "' does not allow null values.'";
        assert checkForCycle(value, property);

        // NO DETACH - this is used from ChangeSumaryImpl.getOldSequence()
        //detachValueIfContainment(value, property);

        ensureSpace(1);

        _version++;
        _values        [_size] = value;
        _properties    [_size] = property;
        _substitutions [_size] = (substitution==null ? property : substitution);
        _prefixes      [_size] = prefix;
        _size++;

        checkForContext(value);
        setContainment(value, property);

        return true;
    }

    public boolean storeSequenceAddNew(int sequenceIndex, PropertyXML property, Object value,
        String prefix, PropertyXML substitution)
    {
        // property can be null if it's text

        // check nullable
        if (value==null && property!=null && !property.isNullable())
            throw new IllegalArgumentException("Property '" + property + "' does not allow null values.'");

        if(sequenceIndex >_size || sequenceIndex <0)
            throw new IndexOutOfBoundsException("Index: " + sequenceIndex + " size: " + _size);

        checkPropertyForAdd(getTypeXML(), property);

        if (getChangeSummary() != null && getChangeSummary().isLogging())
        {
            ChangeSummaryImpl cs = (ChangeSummaryImpl) getChangeSummary();
            if (getType().isSequenced())
            {
                if (property == null || property.getType().isDataType() || !property.isContainment())
                    cs.logModification(this, property, null, false, sequenceIndex);
                else
                    cs.logInsertion(this, property, sequenceIndex, (DataObject) value);
            }
            else if (property.getType().isDataType() || !property.isContainment())
                cs.logModification(this, property, null, false, -1);
            else if (property.isMany())
            {
                // Find the last sequenceIndex of the array to which we are appending
                int size = 0;
                for (int i = 0; i < sequenceIndex; i++)
                    if (property.equals(_properties[i]))
                        size++;
                cs.logInsertion(this, property, size, (DataObject) value);
            }
            else
                cs.logInsertion(this, property, -1, (DataObject) value);
        }

        checkForContext(value);
        checkForCycle(value, property);

        detachValueIfContainment(value, property);

        ensureSpace(1);

        _version++;
        System.arraycopy(_values,        sequenceIndex, _values,        sequenceIndex +1, _size-sequenceIndex);
        System.arraycopy(_properties,    sequenceIndex, _properties,    sequenceIndex +1, _size-sequenceIndex);
        System.arraycopy(_substitutions, sequenceIndex, _substitutions, sequenceIndex +1, _size-sequenceIndex);
        System.arraycopy(_prefixes,      sequenceIndex, _prefixes,      sequenceIndex +1, _size-sequenceIndex);
        _size++;
        _values       [sequenceIndex] = value;
        _properties   [sequenceIndex] = property;
        _substitutions[sequenceIndex] = (substitution==null ? property : substitution);
        _prefixes     [sequenceIndex] = prefix;

        setContainment(value, property);

        return true;
    }

    private static boolean checkPropertyForAdd(TypeXML parentType, Property property)
    {
        if (parentType.isSequenced() && property==null)
            return true;

        if (parentType.isOpen())
        {
            if (!property.equals(parentType.getPropertyXML(property.getName())) &&
                !(PropertyImpl.getPropertyXML(property).isGlobal() ||
                  PropertyImpl.getPropertyXML(property).isDynamic()))
                throw new IllegalArgumentException("Property '" + property + "' must be global or dynamic.") ;
        }
        else
        {
            // TODO: fix this properly
            if (!property.equals(parentType.getPropertyXML(property.getName())))
                 throw new IllegalArgumentException("Instance of '" + parentType + "' cannot accept property '" + property + "'.");
        }
        return true;
    }

    public void storeSequenceSet(int sequenceIndex, Object value)
    {
        if (sequenceIndex>=_size || sequenceIndex<0)
            throw new IndexOutOfBoundsException("Sequence index: " + sequenceIndex + " size: " + _size);

        PropertyXML property = _properties[sequenceIndex];
        // check read-only
        if (property != null && property.isReadOnly())
            throw new UnsupportedOperationException("Read-only property '" + property + "' cannot be modified.");

        // check nullable
        if (value==null && !property.isNullable())
            throw new IllegalArgumentException("Property '" + property + "' does not allow null values.'");

        checkForContext(value);
        checkForCycle(value, property);

        detachValueIfContainment(value, property);

        Object oldValue = _values[sequenceIndex];
        PropertyXML oldProp = _properties[sequenceIndex];

        // This has to happen after detachValue() because detachValue() can have side-effect
        // (for bidirectional properties)
        if (getChangeSummary() != null && getChangeSummary().isLogging())
            logSimpleChange(sequenceIndex, value, true);

        //this doesn't change the property, only the value so _version doesn't change
        _values[sequenceIndex] = value;
        _substitutions[sequenceIndex] = property;

        setContainment(value, _properties[sequenceIndex]);

        clearContainer(oldValue, oldProp);
    }

    private void setContainment(Object value, PropertyXML property)
    {
        if (value instanceof DataObjectGeneral && property!=null && property.isContainment())
        {
            DataObjectGeneral dog = ((DataObjectGeneral)value);
            dog.init(dog.getTypeXML(), getDataGraph(), this, property);
        }
    }

    private void logSimpleChange(int propIndex, Object value, boolean set)
    {
        ChangeSummaryImpl cs = (ChangeSummaryImpl) getChangeSummary();
        PropertyXML p = _properties[propIndex];
        if (p == null || p.getType().isDataType() || !p.isContainment())
            if (getType().isSequenced())
                cs.logModification(this, p, _values[propIndex], true, propIndex, !set);
            else
            {
                assert p != null;
                if (p.isMany())
                {
                    // Because this is a value type we need to log a
                    // modification for the whole arrray
                    List oldValue = getList(p);
                    cs.logModification(this, p, oldValue, true, -1);
                }
                else
                    cs.logModification(this, p, _values[propIndex], true, -1);
            }
        else
        {
            int index = 0;
            if (getType().isSequenced())
                index = propIndex;
            else if (p.isMany())
            {
                // Get the index in the array
                // We could do this faster, but then we would need
                // to keep in sych with the implementation of storeSequenceGetValue
                Object currentValue = _values[propIndex];
                List values = (List) storeGet(p);
                for (int i = 0; i < values.size(); i++)
                    if (currentValue == values.get(i))
                    {
                        index = i;
                        break;
                    }
            }
            else
                index = -1;
            cs.logDeletion(this, p, index, (DataObject) _values[propIndex]);
            if (set)
                cs.logInsertion(this, p, index, (DataObject) value);
        }
    }

    public boolean storeIsSet(Property property)
    {
        assert property!=null;
        for (int i = 0; i < _size; i++)
        {
            Property p = _properties[i];
            if (p !=null && p.equals(property))
                return true;
        }
        return false;
    }

    public boolean storeSequenceIsSet(int sequenceIndex)
    {
        if (sequenceIndex <_size)
            return true;
        return false;
    }

    /**
     * Removes all entries of property
     */
    public void storeUnset(PropertyXML property)
    {
        if (getChangeSummary() != null && getChangeSummary().isLogging())
        {
            ChangeSummaryImpl cs = (ChangeSummaryImpl) getChangeSummary();
            // TODO(radup) Clarify what this method is supposed to do
            // Currently, I think that because of the way the property
            // equality is implemented, it's possible that we'll erase
            // more than one property, in which case we'll need to
            // first get all the properties that are actually deleted
            // and take care of each
            // (cezar) removes all entries of property
            // same property means: same name, same containing type name, same property type name
            if (getType().isSequenced())
            {
                for (int i = 0; i < _size; i++)
                {
                    Property p = _properties[i];
                    if (property.equals(p))
                    {
                        if (p.getType().isDataType() || !p.isContainment())
                            cs.logModification(this, p, _values[i], true, i, true);
                        else
                            cs.logDeletion(this, p, i, (DataObject) _values[i]);
                    }
                }
            }
            else
            {
                if (property.getType().isDataType() || !property.isContainment())
                {
                    for (int i = 0; i < _size; i++)
                    {
                        Property p = _properties[i];
                        if (property.equals(p))
                        {
                            if (p.isMany())
                            {
                                List values = getList(p);
                                cs.logModification(this, p, values, true, -1);
                            }
                            else
                                cs.logModification(this, p, _values[i], true, -1);
                            break;
                        }
                    }
                }
                else
                {
                    for (int i = 0, j = 0; i < _size; i++)
                    {
                        Property p = _properties[i];
                        if (property.equals(p))
                        {
                            if (p.isMany())
                                cs.logDeletion(this, p, j, (DataObject) _values[i]);
                            else
                                cs.logDeletion(this, p, -1, (DataObject) _values[i]);
                            j++;
                        }
                    }
                }
            }
        }
        storeUnset(property, 0);
    }

    /**
     * Removes all entries of property from startingIndex to the end
     */
    private void storeUnset(PropertyXML property, int startingIndex)
    {
        assert property!=null;
        for (int i = startingIndex; i < _size; i++)
        {
            PropertyXML p = _properties[i];
            if (property.equals(p))
            {
                _version++;

                Object value = _values[i];
                if (p.isContainment() && value instanceof DataObjectImpl)
                    ((DataObjectImpl)value).clearContainer();

                if (i+1==_size)
                {
                    _size--;  //is the last one, avoid calling System.arraycopy
                }
                else
                {
                    System.arraycopy(_properties, i+1, _properties, i, _size-i);
                    System.arraycopy(_substitutions, i+1, _substitutions, i, _size-i);
                    System.arraycopy(_values,     i+1, _values,     i, _size-i);
                    _size--;
                    i--;
                }
            }
        }
    }

    public void storeSequenceUnset(int sequenceIndex)
    {
        if (sequenceIndex>=_size || sequenceIndex<0)
            throw new IndexOutOfBoundsException("Sequence index: " + sequenceIndex + " size: " + _size);

        PropertyXML property = _properties[sequenceIndex];
        if (property != null && property.isReadOnly())
            throw new UnsupportedOperationException("Read-only property '" + property + "' cannot be modified.");

        if (getChangeSummary() != null && getChangeSummary().isLogging())
            logSimpleChange(sequenceIndex, null, false);

        Object value = _values[sequenceIndex];
        if (property!=null && property.isContainment() && value instanceof DataObjectImpl)
            ((DataObjectImpl)value).clearContainer();

        _version++;
        if (sequenceIndex+1<_size) //copy arrays only if it's not the last one
        {
            System.arraycopy(_properties, sequenceIndex +1, _properties, sequenceIndex, _size-sequenceIndex);
            System.arraycopy(_substitutions, sequenceIndex +1, _substitutions, sequenceIndex, _size-sequenceIndex);
            System.arraycopy(_values, sequenceIndex +1, _values, sequenceIndex, _size-sequenceIndex);
            System.arraycopy(_prefixes, sequenceIndex +1, _prefixes, sequenceIndex, _size-sequenceIndex);
        }
        _size--;
    }

    public List<PropertyXML> storeGetInstanceProperties()
    {
        List<PropertyXML> typeProps = getTypeXML().getPropertiesXML();
        if (_size<0)
        {
            return typeProps;
        }
        else
        {
            List<PropertyXML> l = new ArrayList<PropertyXML>();
            l.addAll(typeProps);

            Set<PropertyXML> typePropHash = new HashSet<PropertyXML>();
            typePropHash.addAll(typeProps);

            Set<PropertyXML> set = new HashSet<PropertyXML>();
            for (int i = 0; i < _size; i++)
            {
                if (_properties[i]==null)
                    continue;
                
                if (!typePropHash.contains(_properties[i]))
                    set.add(_properties[i]);
            }
            l.addAll(set);
            return Collections.unmodifiableList(l);
        }
    }

    public SequenceXML storeGetSequenceXML()
    {
        assert getType().isSequenced();
        return new SequenceImpl(this);
    }

    private class SequenceImpl
        implements SequenceXML
    {
        private DataObjectGeneral _dataObject;

        private SequenceImpl(DataObjectGeneral dataObject)
        {
            _dataObject = dataObject;
        }

        public int size()
        {
            return _dataObject.storeSequenceSize();
        }

        public Property getProperty(int index)
        {
            return getPropertyXML(index);
        }

        public PropertyXML getPropertyXML(int index)
        {
            return _dataObject.storeSequenceGetPropertyXML(index);
        }

        public String getPrefixXML(int index)
        {
            return _dataObject.storeSequenceGetXMLPrefix(index);
        }

        public PropertyXML getSubstitution(int index)
        {
            return _dataObject.storeSequenceGetSubstitution(index);
        }

        public Object getValue(int index)
        {
            return _dataObject.storeSequenceGetValue(index);
        }

        public Object setValue(int index, Object value)
        {
            PropertyXML prop = getPropertyXML(index);
            Object oldValue = _dataObject.storeSequenceGetValue(index);
            PropertyXML oldProp = _dataObject.storeSequenceGetPropertyXML(index);
            unsetOppositePropertySingle(_dataObject, oldProp, oldValue);

            checkOppositeUniqueObjectConstraint(_dataObject, prop, value);
            if (_dataObject.storeSequenceSize()==0)
                _dataObject.storeSequenceAddNew(0, prop, value, null, null);
            else
                _dataObject.storeSequenceSet(index, value);
            setOppositeProperty(_dataObject, prop, value, null);
            return  oldValue;
        }

        public boolean add(String propertyName, Object value)
        {
            PropertyXML prop = _dataObject.getTypeXML().getPropertyXML(propertyName);
            if (prop==null)
                prop = PropertyImpl.createOnDemand(getSDOContext(), propertyName, value, true);

            return add(prop, value);
        }

        public boolean add(int propertyIndex, Object value)
        {
            return add(Common.getProperty(_dataObject, propertyIndex), value);
        }

        public boolean add(Property property, Object value)
        {
            PropertyXML propertyXML = PropertyImpl.getPropertyXML(property);
            return addXML(propertyXML, value, null, propertyXML);
        }

        public boolean addXML(PropertyXML property, Object value, String prefix, PropertyXML substitution)
        {
            checkOppositeUniqueObjectConstraint(_dataObject, property, value);
            boolean rezult = _dataObject.storeAddNew(property, value, prefix, substitution);
            setOppositeProperty(_dataObject, property, value, null);
            return rezult;
        }

        public void add(int index, String propertyName, Object value)
        {
            PropertyXML prop = PropertyImpl.getPropertyXML(_dataObject.getInstanceProperty(propertyName));
            add(index, prop, value);
        }

        public void add(int index, int propertyIndex, Object value)
        {
            PropertyXML prop = PropertyImpl.getPropertyXML((Property)_dataObject.getInstanceProperties().get(propertyIndex));
            add(index, prop, value);
        }

        public void add(int index, Property property, Object value)
        {
            PropertyXML prop = PropertyImpl.getPropertyXML(property);
            checkOppositeUniqueObjectConstraint(_dataObject, prop, value);
            _dataObject.storeSequenceAddNew(index, prop, value, null, prop);
            setOppositeProperty(_dataObject, prop, value, null);
        }

        public void remove(int index)
        {
            Object value = getValue(index);
            PropertyXML prop = getPropertyXML(index);
            _dataObject.storeSequenceUnset(index);
            unsetOppositePropertySeq(_dataObject, prop, value);
        }

        public void move(int toIndex, int fromIndex)
        {
            int size = _dataObject._size;
            if (toIndex<0 || toIndex>=size || fromIndex<0 || fromIndex>=size)
                throw new IndexOutOfBoundsException("toIndex: " + toIndex + " fromIndex: " + fromIndex + " size: " + size);

            _dataObject._version++;
            PropertyXML prop = _dataObject.storeSequenceGetPropertyXML(fromIndex);
            Object value = _dataObject.storeSequenceGetValue(fromIndex);
            String prefix = _dataObject.storeSequenceGetXMLPrefix(fromIndex);

            _dataObject.storeSequenceUnset(fromIndex);
            _dataObject.storeSequenceAddNew(toIndex, prop, value, prefix, prop);
        }

        public void addText(String text)
        {
            add((Property)null, text);
        }

        public void addText(int index, String text)
        {
            _dataObject.storeSequenceAddNew(index, null, text, null, null);
        }

        /**
         * @deprecated
         */
        public void add(String text)
        {
            addText(text);
        }

        /**
         * @deprecated
         */
        public void add(int index, String text)
        {
            addText(index, text);
        }
    }


    /**
     * Use when the type of this DataObject is NOT sequenced.
     */
    public ListXMLIterator storeGetUnsequencedXML()
    {
        assert !getType().isSequenced();
        return new UnsequencedListXMLIteratorImpl(this);
    }

    /**
     * Implementation of UnsequencedXML, the order of the iterator is the same as getInstanceProperties.
     */
    private class UnsequencedListXMLIteratorImpl
        implements ListXMLIterator
    {
        private DataObjectGeneral _dataObject;
        private List<PropertyXML> _instanceProps;
        private int _propIndex;
        private int _seqIndex;

        private UnsequencedListXMLIteratorImpl(DataObjectGeneral dataObject)
        {
            _dataObject = dataObject;
            _instanceProps = _dataObject.getStore().storeGetInstanceProperties();
            _propIndex  = -1;
            _seqIndex   = -1;
        }

        public boolean next()
        {
            if (_seqIndex>=0)
            {
                do
                {
                    _seqIndex++;
                    if (_instanceProps.get(_propIndex)==_dataObject._properties[_seqIndex])
                        return true;
                }
                while(_seqIndex<_dataObject._size);
                _seqIndex = -1;
            }


            assert _seqIndex<0;

            while(_propIndex < _instanceProps.size())
            {
                _propIndex++;

                if (_propIndex >= _instanceProps.size())
                    return false;

                _seqIndex = -1;
                do
                {
                    _seqIndex++;
                    if (_instanceProps.get(_propIndex)==_dataObject._properties[_seqIndex])
                        return true;
                }
                while(_seqIndex<_dataObject._size);
            }
            return false;
        }

        public PropertyXML getPropertyXML()
        {
            return _dataObject._properties[_seqIndex];
        }

        public Object getValue()
        {
            return _dataObject._values[_seqIndex];
        }

        public PropertyXML getSubstitution()
        {
            return _dataObject._substitutions[_seqIndex];
        }

        public String getPrefix()
        {
            return _dataObject._prefixes[_seqIndex];
        }
    }


    public ListXMLIterator storeGetListXMLIterator(PropertyXML property)
    {
        return new PropertyFilterListXMLIteratorImpl(this, property);
    }

    /**
     * Implementation of UnsequencedXML, the order of the iterator is the same as getInstanceProperties.
     */
    private class PropertyFilterListXMLIteratorImpl
        implements ListXMLIterator
    {
        private DataObjectGeneral _dataObject;
        private PropertyXML _filterProperty;
        private int _seqIndex;
        private int _expectedVersion;

        private PropertyFilterListXMLIteratorImpl(DataObjectGeneral dataObject, PropertyXML property)
        {
            _dataObject = dataObject;
            _filterProperty = property;
            _seqIndex   = -1;
            recordVersion();
        }

        public boolean next()
        {
            ensureVersion();
            _seqIndex++;
            while(_seqIndex<_dataObject._size)
            {
                if (_filterProperty==_dataObject._properties[_seqIndex])
                    return true;
                _seqIndex++;
            }

            return false;
        }

        public PropertyXML getPropertyXML()
        {
            ensureVersion();
            return _filterProperty;
        }

        public Object getValue()
        {
            ensureVersion();
            return _dataObject._values[_seqIndex];
        }

        public PropertyXML getSubstitution()
        {
            ensureVersion();
            return _dataObject._substitutions[_seqIndex];
        }

        public String getPrefix()
        {
            ensureVersion();
            return _dataObject._prefixes[_seqIndex];
        }

        private void ensureVersion()
        {
            if (_expectedVersion==_dataObject._version)
                return;
            else
                throw new ConcurrentModificationException();
        }

        private void recordVersion()
        {
            _expectedVersion = _dataObject._version;
        }
    }
}
