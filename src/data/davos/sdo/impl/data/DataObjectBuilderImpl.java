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

import davos.sdo.DataObjectXML;
import davos.sdo.PropertyMapEntry;
import davos.sdo.PropertyXML;
import davos.sdo.SDOContext;
import davos.sdo.TypeXML;

import davos.sdo.impl.common.Common;
import davos.sdo.impl.common.Names;
import davos.sdo.impl.type.BuiltInTypeSystem;
import javax.sdo.DataObject;
import javax.sdo.Sequence;
import javax.sdo.helper.XSDHelper;

import java.text.MessageFormat;
import java.util.*;
import java.math.BigInteger;

import javax.xml.namespace.QName;

public class DataObjectBuilderImpl implements DataObjectBuilder
{
    private XSDHelper _xsdHelper;
    private DataObjectXML _current;
    private DataObjectXML _root;
    private Stack<State> _state = new Stack<State>();
    private SDOContext _sdoContext;

//    public DataObjectBuilderImpl()
//    {
//        _sdoContext = SDOContextFactory.getGlobalSDOContext();
//        _xsdHelper = _sdoContext.getXSDHelper();
//    }
//
    public DataObjectBuilderImpl(SDOContext sdoctx)
    {
        _sdoContext = sdoctx;
        _xsdHelper = _sdoContext.getXSDHelper();
    }

    public DataObjectBuilderImpl(SDOContext sdoctx, DataObject startObject)
    {
        this(sdoctx);
        _root = _current = (DataObjectXML) startObject;
        if (startObject.getSequence() != null)
        {
            State state = new State(true);
            _state.push(state);
            Sequence seq = startObject.getSequence();
            for (int i = 0; i < seq.size(); i++)
                state.incrementArrayIndex();
        }
        else
        {
            State state = new State(false);
            _state.push(state);
            List<PropertyXML> properties = (List<PropertyXML>) startObject.getInstanceProperties();
            for (PropertyXML prop : properties)
            {
                if (prop.isMany())
                {
                    List items = startObject.getList(prop);
                    for (int i = 0; i < items.size(); i++)
                        state.incrementArrayIndexForProperty(prop);
                }
                else if (startObject.isSet(prop))
                    state.incrementArrayIndexForProperty(prop);
            }
        }
    }

    public void startElement(String uri, String name, String prefix, Change changeType)
    {
        if (_current == null)
        {
            // We are at the root
            if (changeType != Change.SAME)
                throw new SDOBuilderException(messageForCode("sdobuilder.rootelementunchanged"));

            PropertyXML rootProp = (PropertyXML) _xsdHelper.getGlobalProperty(uri, name, true);
            if (rootProp == null)
                throw new SDOBuilderException(messageForCode("sdobuilder.elementnotfound.global", name, uri));

            TypeXML rootType = rootProp.getTypeXML();
            DataObject root = _sdoContext.getDataFactory().create(rootType == BuiltInTypeSystem.DATAOBJECT ?
                BuiltInTypeSystem.BEADATAOBJECT : rootType);

            _current = (DataObjectXML) root;
            
            ((davos.sdo.impl.data.DataObjectImpl) _current).setContainmentProperty(rootProp);
            _root = _current;
            _state.push(new State(rootType.isSequenced()));
        }
        else
        {
            DataObjectXML parent = _current;
            TypeXML parentType = parent.getTypeXML();
            PropertyMapEntry propEntry = parentType.getPropertyMapEntryByXmlName(uri, name, true);

            PropertyXML prop, xmlProp;
            if (propEntry == null)
            {
                prop = null;
                xmlProp = null;
            }
            else
            {
                prop = propEntry.getProperty();
                xmlProp = propEntry.getSubstitutionProperty();
            }

            DataObjectXML child;
            /* We support the following cases
             * - prop is defined in the parentType
             * - prop is defined in the Schema as a global property and parentType is open
             */
            if (prop == null)
            {
                prop = (PropertyXML) _xsdHelper.getGlobalProperty(uri, name, true);
                xmlProp = prop;
            }
            if (prop == null)
            {
                QName parentTypeName = parentType.getXMLSchemaTypeName();
                if (parentTypeName != null)
                    throw new SDOBuilderException(messageForCode("sdobuilder.elementnotfound", name, uri,
                        parentTypeName.getLocalPart(), parentTypeName.getNamespaceURI(),
                        parent.getContainmentPropertyXML().getXMLName(),
                        parent.getContainmentPropertyXML().getXMLNamespaceURI()));
                else
                    throw new SDOBuilderException(messageForCode("sdobuilder.elementnotfound.anonymoustype",
                        name, uri, parent.getContainmentPropertyXML().getXMLName(),
                        parent.getContainmentPropertyXML().getXMLNamespaceURI()));
            }
            if (prop.getTypeXML().isDataType())
            {
                QName typeName = prop.getTypeXML().getXMLSchemaTypeName();
                if (typeName != null)
                    throw new SDOBuilderException(messageForCode("sdobuilder.expectedsimplecontent",
                        name, uri, typeName.getLocalPart(), typeName.getNamespaceURI(),
                        parent.getContainmentPropertyXML().getXMLName(),
                        parent.getContainmentPropertyXML().getXMLNamespaceURI()));
                else
                    throw new SDOBuilderException(messageForCode("sdobuilder.expectedsimplecontent.anonymoustype",
                        name, uri, parent.getContainmentPropertyXML().getXMLName(),
                        parent.getContainmentPropertyXML().getXMLNamespaceURI()));
            }
            if (!prop.isContainment())
                throw new SDOBuilderException(messageForCode("sdobuilder.referencenotsupported.element", name, uri));
            TypeXML childType = xmlProp.getTypeXML();
            if (childType == BuiltInTypeSystem.CHANGESUMMARYTYPE)
                throw new SDOBuilderException(messageForCode("sdobuilder.changesummarynotsupported", name, uri));

            ChangeSummaryImpl cs;
            switch (changeType)
            {
                case SAME:
                    child = parent.createDataObjectXML(prop, prefix, xmlProp);
                    _current = child;
                    updateState(prop, parentType.isSequenced());
                    break;
                case NEW:
                    child = parent.createDataObjectXML(prop, prefix, xmlProp);
                    _current = child;
                    cs = (ChangeSummaryImpl) parent.getChangeSummary();
                    if (cs == null)
                        throw new IllegalArgumentException("Element \"" + (prefix == null ||
                                prefix.length() == 0 ? name : prefix + ':' + name) +
                            "\" is set to NEW but there is no containing change summary");
                    cs.logInsertion(parent, prop, getCurrentIndex(prop),child);
                    updateState(prop, parentType.isSequenced());
                    break;
                case OLD:
                    child = (DataObjectXML) _sdoContext.getDataFactory().create(childType);
                    ((davos.sdo.impl.data.DataObjectImpl) child).setContainmentProperty(prop);
                    _current = child;
                    cs = (ChangeSummaryImpl) parent.getChangeSummary();
                    if (cs == null)
                        throw new IllegalArgumentException("Element \"" + (prefix == null ||
                                prefix.length() == 0 ? name : prefix + ':' + name) +
                            "\" is set to OLD but there is no containing change summary");
                    cs.logDeletion(parent, prop, getCurrentIndex(prop), child, false);
                    break;
            }

            // Opposite properties are not supported because one of the ends has to be a reference
            newState(childType.isSequenced(), changeType == Change.OLD, parent);
        }
    }

    public void endElement()
    {
        DataObjectXML container = _current.getContainerXML();
        _current = popState();
        if (_current == null)
            _current = container;
    }

    public void simpleContent(String uri, String name, String prefix, Object value, Kind kind, Change changeType)
    {
        DataObjectXML parent = _current;
        TypeXML parentType = parent.getTypeXML();
        PropertyXML prop;

        if (kind == Kind.CONTENT)
            prop = parentType.getPropertyXML(Names.SIMPLE_CONTENT_PROP_NAME);
        else
            prop = parentType.getPropertyXMLByXmlName(uri, name, kind == Kind.ELEMENT);

        /* We support the following cases
         * - prop is defined in the parentType
         * - prop is defined in the Schema as a global property and parentType is open
         */
        if (prop == null)
            prop = (PropertyXML) _xsdHelper.getGlobalProperty(uri, name, kind == Kind.ELEMENT);
        if (prop == null)
        {
            QName parentTypeName = parentType.getXMLSchemaTypeName();
            String containerName = parent.getContainmentPropertyXML().getXMLName();
            String containerUri = parent.getContainmentPropertyXML().getXMLNamespaceURI();
            switch (kind)
            {
            case ELEMENT:
                if (parentTypeName != null)
                    throw new SDOBuilderException(messageForCode("sdobuilder.elementnotfound", name, uri,
                        parentTypeName.getLocalPart(), parentTypeName.getNamespaceURI(),
                        containerName, containerUri));
                else
                    throw new SDOBuilderException(messageForCode("sdobuilder.elementnotfound.anonymoustype",
                        name, uri, containerName, containerUri));
            case ATTRIBUTE:
                if (parentTypeName != null)
                    throw new SDOBuilderException(messageForCode("sdobuilder.attributenotfound", name, uri,
                        parentTypeName.getLocalPart(), parentTypeName.getNamespaceURI(),
                        containerName, containerUri));
                else
                    throw new SDOBuilderException(messageForCode("sdobuilder.attributenotfound.anonymoustype",
                        name, uri, containerName, containerUri));
            case CONTENT:
                if (parentTypeName != null)
                    throw new SDOBuilderException(messageForCode("sdobuilder.expectedcomplextypesimplecontent",
                        parentTypeName.getLocalPart(), parentTypeName.getNamespaceURI(), containerName, containerUri));
                else
                    throw new SDOBuilderException(messageForCode("sdobuilder.expectedcomplextypesimplecontent.anonymoustype",
                        containerName, containerUri));
            }
        }

        TypeXML propType = prop.getTypeXML();
        if (!propType.isDataType())
        {
            switch (kind)
            {
            case ELEMENT:
                // If the value is null, then it's ok, it means it was a nillable element
                if (value == null)
                {
                    nullComplexElement(uri, name, prefix, changeType);
                    return;
                }
                else if ((value instanceof String) && value.toString().length() == 0)
                {
                    emptyComplexElement(uri, name, prefix, changeType);
                    return;
                }
                QName typeName = prop.getTypeXML().getXMLSchemaTypeName();
                if (typeName != null)
                    throw new SDOBuilderException(messageForCode("sdobuilder.expectedcomplextype",
                        name, uri, typeName.getLocalPart(), typeName.getNamespaceURI(), value.toString()));
                else
                    throw new SDOBuilderException(messageForCode("sdobuilder.expectedcomplextype.anonymoustype",
                        name, uri, value.toString()));
            case ATTRIBUTE:
                throw new SDOBuilderException(messageForCode("sdobuilder.referencenotsupported.attribute", name, uri));
            case CONTENT:
                throw new IllegalArgumentException("Type \"" + parentType.getName() + "@" +
                    parentType.getURI() + "\" has simple content, but its \"" +
                    Names.SIMPLE_CONTENT_PROP_NAME + "\" property is not of a data type");
            }
        }
        // HACK Because List values of length 1 can come "unwrapped" from the list, we need
        // to check whether we are expecting a List; on the other hand, empty Strings can
        // actually come as empty Lists, so we need to check for that too
        if (propType.getListItemType() != null)
        {
            if (!(value instanceof List)) 
            {
                List l = new ArrayList(1);
                l.add(value);
                value = l;
            }
        }
        else
        {
            if (value == EMPTY_LIST)
                value = _sdoContext.getDataHelper().convert(prop, Common.EMPTY_STRING);
        }

        // Values for all types derived from xs:integer are passed in as "long"s so they need
        // to be converted to the right SDO type
        if (value instanceof Long)
        {
            value = convertLong(propType, (Long) value, prop, kind);
        }

        // IMPORTANT: the following assumes that "value" is of the right type with respect to "prop"
        // This is not verified anywhere
        if (changeType == Change.SAME || changeType == Change.NEW)
        {
            if (prop.isMany())
                if (parentType.isSequenced())
                    parent.getSequenceXML().addXML(prop, value, prefix, prop);
                else
                    parent.getList(prop).add(value);
            else
                parent.setXML(prop, value, prefix, prop);
            if (parentType.isSequenced())
                updateState();
        }

        if (changeType == Change.SAME && (!prop.isMany() || parentType.isSequenced()))
            return;

        ChangeSummaryImpl cs = (ChangeSummaryImpl) parent.getChangeSummary();
        if (cs == null && (changeType == Change.NEW || changeType == Change.OLD))
            throw new IllegalArgumentException("Simple content \"" + (prefix == null ||
                    prefix.length() == 0 ? name : prefix + ':' + name) +
                "\" is set to " + (changeType == Change.NEW ? "NEW" : "OLD") +
                " but there is no containing change summary");
        if (parentType.isSequenced())
        {
            if (changeType == Change.NEW)
                // Log an 'insert'
                cs.logModification(parent, prop, null, false, getCurrentIndex(prop));
            if (changeType == Change.OLD)
                // Log a 'delete'
                cs.logModification(parent, prop, value, true, getCurrentIndex(prop), true);
        }
        else
        {
            if (prop.isMany())
            {
                // We need to find the change and add to the list kept in that change or
                // create a new change
                ChangeSummaryImpl.Change c = findChange(cs, prop, parent);
                if (c == null)
                {
                    if (changeType != Change.SAME)
                    {
                        // Since this is the first change to that property, it means that until now
                        // the before and after lists are the same, except for the newly added
                        // value if the modification is an insertion
                        List oldValue = new ArrayList(parent.getList(prop));
                        if (changeType == Change.OLD)
                            oldValue.add(value);
                        else if (changeType == Change.NEW)
                            oldValue.remove(oldValue.size() - 1);
                        cs.logModification(parent, prop, oldValue, true, -1);
                    }
                    // else we don't need to do anything, the whole list may be the same
                }
                else if (changeType == Change.OLD || changeType == Change.SAME)
                {
                    // Even if there is not change at the current index, if we already know this
                    // list is being changed, we need to add the value to the old list as well
                    List oldValue = (List) c.getValue();
                    oldValue.add(value);
                }
            }
            else
            {
                // This is trickier
                // For data value properties in non-sequenced types, we only keep one change in the
                // change summary, but we may get two events, both a NEW and an OLD
                if (changeType == Change.NEW)
                    // Don't log anything, update the state to mark that the respective property has
                    // a new value
                    addPendingNew(prop);
                else
                {
                    // Log a modification and make sure that if there are any pending "new", they
                    // are deleted
                    cs.logModification(parent, prop, value, true, -1);
                    removePendingNew(prop);
                }
            }
        }
    }

    public DataObjectXML retrieveRootDataObject()
    {
        ChangeSummaryImpl cs = (ChangeSummaryImpl) _root.getChangeSummary();
        if (cs != null)
            cs.setLogging(true);
        return _root;
    }

    private final void nullComplexElement(String uri, String name, String prefix, Change changeType)
    {
        if (_current == null)
        {
            _root = null;
            return;
        }
        DataObjectXML parent = _current;
        TypeXML parentType = parent.getTypeXML();
        PropertyMapEntry propEntry = parentType.getPropertyMapEntryByXmlName(uri, name, true);

        PropertyXML prop, xmlProp;
        if (propEntry == null)
        {
            prop = null;
            xmlProp = null;
        }
        else
        {
            prop = propEntry.getProperty();
            xmlProp = propEntry.getSubstitutionProperty();
        }

        /* We support the following cases
         * - prop is defined in the parentType
         * - prop is defined in the Schema as a global property and parentType is open
         */
        if (prop == null)
        {
            prop = (PropertyXML) _xsdHelper.getGlobalProperty(uri, name, true);
            xmlProp = prop;
        }
        if (prop == null || prop.getTypeXML().isDataType())
            throw new IllegalStateException("prop must be of a complex type, this has been " +
                "already checked before calling this method!");
        if (!prop.isContainment())
            throw new SDOBuilderException("Reference types not supported for element \"" + name +
                "@" + uri + "\"");

        TypeXML childType = xmlProp.getTypeXML();
        if (childType == BuiltInTypeSystem.CHANGESUMMARYTYPE)
            throw new SDOBuilderException("ChangeSummary type not supported for element \"" +
                name + "@" + uri + "\"");

        ChangeSummaryImpl cs;
        switch (changeType)
        {
            case SAME:
                ((Store) parent).storeAddNew(prop, null);
                break;
            case NEW:
                ((Store) parent).storeAddNew(prop, null);
                cs = (ChangeSummaryImpl) parent.getChangeSummary();
                if (cs == null)
                    throw new IllegalArgumentException("Element \"" + (prefix == null ||
                            prefix.length() == 0 ? name : prefix + ':' + name) +
                        "\" is set to NEW but there is no containing change summary");
                cs.logInsertion(parent, prop, getCurrentIndex(prop), null);
                break;
            case OLD:
                cs = (ChangeSummaryImpl) parent.getChangeSummary();
                if (cs == null)
                    throw new IllegalArgumentException("Element \"" + (prefix == null ||
                            prefix.length() == 0 ? name : prefix + ':' + name) +
                        "\" is set to OLD but there is no containing change summary");
                cs.logDeletion(parent, prop, getCurrentIndex(prop), null, false);
                break;
        }
    }

    private final void emptyComplexElement(String uri, String name, String prefix, Change changeType)
    {
        startElement(uri, name, prefix, changeType);
        endElement();
    }

    private Object convertLong(TypeXML type, Long value, PropertyXML prop, Kind kind)
    {
        // The following code is copied from DataHelperImpl.convert(Type, Object),
        // It has to be kept in sync in case that changes
        // The reason is copied here is that the conversion in DataHelperImpl is extremely slow
        // and can be optimized because we know the type of the value
        Class instClass = type.getInstanceClass();

        if (instClass.isAssignableFrom(int.class))
        {
            return value.intValue();
        }
        else if (instClass.isAssignableFrom(long.class))
        {
            return value;
        }
        else if (instClass.isAssignableFrom(BigInteger.class))
        {
            return BigInteger.valueOf(value);
        }
        else if (instClass.isAssignableFrom(byte.class))
        {
            return value.byteValue();
        }
        else if (instClass.isAssignableFrom(short.class))
        {
            return value.shortValue();
        }

        QName typeName = type.getXMLSchemaTypeName();
        if (typeName == null)
            typeName = new QName("anonymous");
        boolean element = true;
        if (kind == Kind.ATTRIBUTE)
            element = false;
        else if (kind == Kind.CONTENT)
            prop = _current.getContainmentPropertyXML();
        if (element)
            throw new SDOBuilderException(messageForCode("sdobuilder.conversion.long.element",
                value, typeName.getLocalPart(), typeName.getNamespaceURI(),
                prop.getXMLNamespaceURI(), prop.getXMLName()));
        else
            throw new SDOBuilderException(messageForCode("sdobuilder.conversion.long.attribute",
                value, typeName.getLocalPart(), typeName.getNamespaceURI(),
                prop.getXMLNamespaceURI(), prop.getXMLName()));
    }

    // =============================
    // State management
    // =============================
    private static class State
    {
        private Map<PropertyXML, IntegerHolder> counterMap;
        private int counter;
        private DataObjectXML parent;
        private Set<PropertyXML> pendingProperties;

        State(boolean sequenced)
        {
            if (!sequenced)
            {
                counterMap = new HashMap<PropertyXML, IntegerHolder>();
                pendingProperties = new HashSet<PropertyXML>();
            }
        }

        public DataObjectXML getParent()
        {
            return parent;
        }

        public void setParent(DataObjectXML parent)
        {
            this.parent = parent;
        }

        int getArrayIndex(PropertyXML prop)
        {
            if (counterMap != null)
            {
                IntegerHolder i = counterMap.get(prop);
                if (i != null)
                    return i.get();
                else
                    return 0;
            }
            else
                return counter;
        }

        void incrementArrayIndex()
        {
            counter++;
        }

        public void incrementArrayIndexForProperty(PropertyXML prop)
        {
            if (counterMap != null)
            {
                IntegerHolder i = counterMap.get(prop);
                if (i == null)
                {
                    i = new IntegerHolder();
                    counterMap.put(prop, i);
                }
                i.increment();
            }
            else
                counter++;
        }

        public void addPendingProperty(PropertyXML prop)
        {
            pendingProperties.add(prop);
        }

        public void removePendingProperty(PropertyXML prop)
        {
            pendingProperties.remove(prop);
        }

        public Iterator<PropertyXML> getPendingProperties()
        {
            return pendingProperties == null ? null : pendingProperties.iterator();
        }

        private static class IntegerHolder
        {
            int value;

            void increment()
            {
                value++;
            }

            int get()
            {
                return value;
            }

            public String toString()
            {
                return String.valueOf(value);
            }
        }
    }

    private void addPendingNew(PropertyXML prop)
    {
        State state = _state.peek();
        state.addPendingProperty(prop);
    }

    private void removePendingNew(PropertyXML prop)
    {
        State state = _state.peek();
        state.removePendingProperty(prop);
    }

    private void updateState(PropertyXML prop, boolean sequenced)
    {
        State state = _state.peek();
        if (sequenced)
            state.incrementArrayIndex();
        else if (prop.isMany())
            state.incrementArrayIndexForProperty(prop);
    }

    private void newState(boolean sequenced, boolean isDeleted, DataObjectXML parent)
    {
        State state = new State(sequenced);
        if (isDeleted)
            state.setParent(parent);
        _state.add(state);
    }

    private void updateState()
    {
        State state = _state.peek();
        state.incrementArrayIndex();
    }

    private int getCurrentIndex(PropertyXML prop)
    {
        if (prop.isMany() || _current.getSequenceXML() != null)
        {
            State state = _state.peek();
            return state.getArrayIndex(prop);
        }
        else
            return -1;
    }

    private DataObjectXML popState()
    {
        State state = _state.pop();
        Iterator<PropertyXML> it = state.getPendingProperties();
        if (it != null && it.hasNext())
        {
            ChangeSummaryImpl cs = (ChangeSummaryImpl) _current.getChangeSummary();
            // At this point, we are preparing to finish processing the current DataObject
            // If there are still data value properties for which a 'new' has been registered
            // but no 'old', this means that the respective property was initially not set
            while (it.hasNext())
                cs.logModification(_current, it.next(), null, false, -1);
        }
        return state.getParent();
    }

    private ChangeSummaryImpl.Change findChange(ChangeSummaryImpl cs, PropertyXML prop, DataObjectXML parent)
    {
        ChangeSummaryImpl.Change[] changes = cs.getModifiedObjects().get(parent);
        if (changes == null)
            return null;
        int hashCode = ChangeSummaryImpl.hashCode(prop, changes.length);
        for (ChangeSummaryImpl.Change c = changes[hashCode]; c != null; c = c.next)
            if (c.getProperty() == prop)
                return c;
        return null;
    }

    // ===========================================
    // Error reporting
    // ===========================================
    private static final ResourceBundle _bundle = PropertyResourceBundle.getBundle("davos.sdo.impl.data.sdobuilderexception");

    private static String messageForCode(String errorCode, Object... args)
    {
        if (errorCode == null)
            return null;

        String message;

        try
        {
            message = MessageFormat.format(_bundle.getString(errorCode), args);
        }
        catch (java.util.MissingResourceException e)
        {
            return MessageFormat.format(_bundle.getString("message.missing.resource"),
                new Object[] { e.getMessage() });
        }
        catch (IllegalArgumentException e)
        {
            return MessageFormat.format(_bundle.getString("message.pattern.invalid"),
                new Object[] { e.getMessage() });
        }

        return message;
    }
}
