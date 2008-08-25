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
package davos.sdo.impl.marshal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import davos.sdo.PropertyXML;
import davos.sdo.SDOContext;
import davos.sdo.SequenceXML;
import davos.sdo.DataObjectXML;
import davos.sdo.Options;
import davos.sdo.impl.data.ChangeSummaryImpl;
import davos.sdo.impl.data.Store;
import davos.sdo.impl.data.DataObjectImpl;
import davos.sdo.impl.data.ChangeSummaryImpl.Change;
import davos.sdo.impl.common.Common;
import davos.sdo.impl.common.Names;
import davos.sdo.impl.common.NamespaceStack;
import davos.sdo.impl.type.SimpleValueHelper;
import davos.sdo.impl.util.XmlPath;
import javax.sdo.DataObject;
import javax.sdo.Property;

public class PlainChangeSummaryMarshaller extends Marshaller
{
    static final String ATTR_LOGGING = "logging";

    private Saver _h;
    private PlainMarshaller _helper;
    private ReferenceBuilder _referenceBuilder;
    private boolean _prettyPrint;
    private int _indentStep = DEFAULT_INDENT_STEP;
    private int _currentIndent = NEWLINE.length();

    public PlainChangeSummaryMarshaller(Object options)
    {
        _helper = new PlainMarshaller(options);
        Map map = null;
        if (options instanceof Map)
            map = (Map) options;
        else if (options instanceof Options)
            map = ((Options) options).getMap();
        if (map != null)
        {
            if (map.containsKey(Options.SAVE_PRETTY_PRINT))
                _prettyPrint = true;
            if (map.containsKey(Options.SAVE_INDENT))
                _indentStep = (Integer) map.get(Options.SAVE_INDENT);
        }
    }

    void marshal(DataObject rootObject, String rootUri, String rootName,
        boolean xmlDecl, String xmlVersion, String encoding,
        String schemaLocation, String noNSSchemaLocation, SDOContext sdoContext)
    {
        _helper.setSdoContext(sdoContext);
        ChangeSummaryImpl cs = (ChangeSummaryImpl) rootObject.getChangeSummary();

        marshalChangeSummaryAttributes(rootObject, cs);

        boolean wasIndentIncremented = false;
        if (_prettyPrint)
            wasIndentIncremented = incrementIndent();
        Map<DataObject, Change[]> modifiedObjects = cs.getModifiedObjects();
        for (Map.Entry<DataObject, Change[]> entry : modifiedObjects.entrySet())
        {
            DataObject object = entry.getKey();
            DataObjectXML objectXML = (DataObjectXML) object;
            if (_prettyPrint)
                indent(_h, _currentIndent);
            marshalChangedObject(objectXML, rootObject, rootUri, rootName, entry.getValue());
        }
        if (_prettyPrint)
        {
            if (wasIndentIncremented)
                decrementIndent();
            if (modifiedObjects.size() > 0)
                indent(_h, _currentIndent);
        }
    }

    protected void marshalChangeSummaryAttributes(DataObject rootObject, ChangeSummaryImpl cs)
    {
        if (!cs.isLogging())
            _h.attr(null, ATTR_LOGGING, null, "false");

        CSReferenceBuilder rb = (CSReferenceBuilder) _referenceBuilder;
        rb.enablePathCaching();
        // Build the list of inserted elements
        StringBuilder sb = new StringBuilder();
        for (DataObject o : cs.getInsertedObjects())
        {
            sb.append(rb.getPathOrId((DataObjectXML) o, rootObject, _h.getNamespaceStack()));
            sb.append(' ');
        }
        if (sb.length() > 0)
        {
            sb.deleteCharAt(sb.length() - 1);
            _h.attr(null, ChangeSummaryImpl.CHANGE_SUMMARY_CREATE, null, sb.toString());
        }
        sb = new StringBuilder();
        for (DataObject o : cs.getDeletedObjects().keySet())
        {
            sb.append(rb.getPathOrId((DataObjectXML) o, rootObject, _h.getNamespaceStack()));
            sb.append(' ');
        }
        if (sb.length() > 0)
        {
            sb.deleteCharAt(sb.length() - 1);
            _h.attr(null, ChangeSummaryImpl.CHANGE_SUMMARY_DELETE, null, sb.toString());
        }
        rb.disablePathCaching();
    }

    protected void marshalChangedObject(DataObjectXML objectXML, DataObject rootObject,
        String rootUri, String rootName, Change[] changes)
    {
        CSReferenceBuilder rb = (CSReferenceBuilder) _referenceBuilder;
        String elemUri, elemName;
        PropertyXML prop = objectXML.getContainmentPropertyXML();
        if (objectXML == rootObject)
        {
            elemUri = rootUri;
            elemName = rootName;
        }
        else if (prop == null)
        {
            elemUri = Common.EMPTY_STRING;
            elemName = Names.SDO_DATAOBJECT;
        }
        else
        {
            elemUri = prop.getXMLNamespaceURI();
            elemName = prop.getXMLName();
        }
        if (objectXML.getRootObject() != rootObject.getRootObject())
            // It is not necessary to output this object
            return;
        _h.startElement(elemUri, elemName, null, null, null);
        String nodeId = rb.getPathOrId(objectXML, rootObject, _h.getNamespaceStack());
        if (nodeId == null)
            throw new IllegalStateException("The computed path cannot be null at this time");
        _h.attr(Names.URI_SDO, ChangeSummaryImpl.ATTR_REF, null, nodeId);
        // First, we need to build the value of the "unset" attribute
        StringBuilder sb = new StringBuilder();
        for (Change c : changes)
            for (; c != null; c = c.next)
            {
                Property p = c.getProperty();
                if (!c.isSet() && (p.getType().isDataType() || !p.isContainment()) &&
                    p != ChangeSummaryImpl.SEQUENCE)
                {
                    // This needs to be represented as an entry in the unset list
                    // Problem: we can have both an element and an attribute with the same
                    // name and if one of them is unset and the other unchanged, we have no way
                    // of knowing which is which
                    String value = XmlPath.getName((PropertyXML) p, _h.getNamespaceStack());
                    sb.append(value).append(' ');
                }
            }
        if (sb.length() > 0)
        {
            _h.attr(Names.URI_SDO, ChangeSummaryImpl.ATTR_UNSET, null,
                sb.substring(0, sb.length() - 1));
        }
        boolean hasSimpleContent = objectXML.getTypeXML().isSimpleContent();
        Change simpleContentChange = null;
        // Second, we need to find attributes
        for (Change c : changes)
            for (; c != null; c = c.next)
            {
                Property p = c.getProperty();
                if (p == ChangeSummaryImpl.SEQUENCE || !(p instanceof PropertyXML) || !c.isSet())
                    continue;
                PropertyXML pxml = (PropertyXML) p;
                if (!pxml.isXMLElement())
                    if (hasSimpleContent && Names.SIMPLE_CONTENT_PROP_NAME.equals(p.getName()))
                        simpleContentChange = c;
                    else
                    {
                        Object value = c.getValue();
                        _helper.marshalAttributeProperty((PropertyXML) p, value, null, objectXML);
                    }
            }
        if (objectXML.getSequence() == null)
        {
            if (simpleContentChange != null)
            {
                Object value = simpleContentChange.getValue(); 
                if (value == null)
                    // The object itself was not null, but the value of its text
                    // property was, only thing we can do is add an xsi:nil attribute
                    _h.sattr(SDOEventModel.ATTR_XSI, Names.XSI_NIL, Names.TRUE);
                else
                {
                    try {
                        PropertyXML px = (PropertyXML) simpleContentChange.getProperty();
                        _h.text(SimpleValueHelper.getLexicalRepresentation(value, px.getTypeXML(),
                            px.getSchemaTypeCode(), _h.getNamespaceStack()));
                    } catch (SimpleValueHelper.SimpleValueException e)
                    {
                        // Not likely
                    }
                }
            }
            // Now we marshal the elements
            boolean hadElements = false;
            boolean wasIndentIncremented2 = false;
            if (_prettyPrint)
            {
                wasIndentIncremented2 = incrementIndent();
                _helper.setCurrentIndent(_currentIndent);
            }
            // Marshal them in the order in which they appear in the type declaration
            ChangeIterator cit = new ChangeIterator(objectXML, changes);
            Change c;
            while ((c = cit.next()) != null)
            {
                Property p = c.getProperty();
                if ((p instanceof PropertyXML) &&
                    !((PropertyXML) p).isXMLElement())
                    continue;
                if (p.getType().isDataType() || !p.isContainment())
                {
                    if (!c.isSet())
                    {
                        // We don't need to do anything because the property will show up in the
                        // "unset" list
                        ;
                    }
                    else if (p.isMany())
                    {
                        List valueAsList = (List) c.getValue();
                        PropertyXML pXML = (PropertyXML) p;
                        for (Object val : valueAsList)
                        {
                            hadElements = true;
                            if (val == null)
                                _helper.marshalXsiNil(pXML.getXMLNamespaceURI(),
                                    pXML.getXMLName(), null, _prettyPrint);
                            else if (val instanceof DataObject)
                                _helper.marshalSimple(getNodeReference((DataObjectXML) val,
                                    _h.getNamespaceStack()),
                                    pXML.getXMLNamespaceURI(), pXML.getXMLName(), null, null,
                                    pXML.getSchemaTypeCode(), _prettyPrint);
                            else
                                _helper.marshalSimple(val, pXML.getXMLNamespaceURI(),
                                    pXML.getXMLName(), null, pXML.getTypeXML(),
                                    pXML.getSchemaTypeCode(), _prettyPrint);
                        }
                    }
                    else if (c.getValue() instanceof DataObject)
                    {
                        PropertyXML pxml = (PropertyXML) p;
                        hadElements = true;
                        if (c.getValue() == null)
                            _helper.marshalXsiNil(pxml.getXMLNamespaceURI(),
                                pxml.getXMLName(), null, _prettyPrint);
                        else
                            _helper.marshalSimple(getNodeReference((DataObjectXML) c.getValue(), _h.getNamespaceStack()),
                                pxml.getXMLNamespaceURI(), pxml.getXMLName(), null, null,
                                pxml.getSchemaTypeCode(), _prettyPrint);
                    }
                    else
                    {
                        PropertyXML pxml = (PropertyXML) p;
                        hadElements = true;
                        if (c.getValue() == null)
                            _helper.marshalXsiNil(pxml.getXMLNamespaceURI(),
                                pxml.getXMLName(), null, _prettyPrint);
                        else
                            _helper.marshalSimple(c.getValue(), pxml.getXMLNamespaceURI(),
                                pxml.getXMLName(), null, pxml.getTypeXML(),
                                pxml.getSchemaTypeCode(), _prettyPrint);
                    }
                }
                else if (p.isMany())
                {
                    // We need to refer to a new element in the array
                    // for missing indexes, to skip inserted elements
                    // and to fully serialize deleted elements
                    String elName = ((PropertyXML) p).getXMLName();
                    String elUri = ((PropertyXML) p).getXMLNamespaceURI();
                    List currentValues = (List) objectXML.get(p);
                    int offset = 0;
                    int currentArrayPos = 0;
                    rb.enablePathCaching();
                    for (Change c2 = c; c2 != null; c2 = c2.next2)
                    {
                        currentArrayPos += c2.getArrayPos();
                        if (c2.isSet())
                        {
                            // This is a deletion
                            // Add all the elements up to this one as
                            // references
                            for (; offset < currentArrayPos; offset++)
                            {
                                if (_prettyPrint)
                                {
                                    indent(_h, _currentIndent);
                                    hadElements = true;
                                }
                                marshalElementWithReference(elUri, elName,
                                    getNodeReference((DataObjectXML) currentValues.get(offset), _h.getNamespaceStack()));
                            }
                            // Add the missing element
                            hadElements = true;
                            if (c2.getValue() == null)
                                _helper.marshalXsiNil(elemUri, elName, null, _prettyPrint);
                            else
                                marshalElementProperty((PropertyXML) p, c2.getValue(), objectXML,
                                    _prettyPrint);
                        }
                        else
                        {
                            // This is a new element
                            // Add all the elements up to this one but
                            // do not add this, only increment the offset
                            for  (; offset < currentArrayPos; offset++)
                            {
                                if (_prettyPrint)
                                {
                                    indent(_h, _currentIndent);
                                    hadElements = true;
                                }
                                marshalElementWithReference(elUri, elName,
                                    getNodeReference((DataObjectXML) currentValues.
                                        get(offset), _h.getNamespaceStack()));
                            }
                            offset++;
                        }
                    }
                    for (; offset < currentValues.size(); offset++)
                    {
                        if (_prettyPrint)
                        {
                            indent(_h, _currentIndent);
                            hadElements = true;
                        }
                        marshalElementWithReference(elUri, elName,
                            getNodeReference((DataObjectXML) currentValues.get(offset), _h.getNamespaceStack()));
                    }
                    rb.disablePathCaching();
                }
                else
                {
                    if (!c.isSet())
                    {
                        // This is an insertion, so nothing to do at this point, but the insertion might
                        // have been followed by a deletion, so check for that
                        if (c.next2 != null)
                            c = c.next2;
                    }
                    if (c.isSet())
                    {
                        // It was a deletion, so we need to copy the
                        // element here
                        hadElements = true;
                        if (c.getValue() == null)
                            _helper.marshalXsiNil(((PropertyXML) p).getXMLNamespaceURI(),
                                ((PropertyXML) p).getXMLName(), null, _prettyPrint);
                        else
                            marshalElementProperty((PropertyXML) p, c.getValue(), objectXML,
                                _prettyPrint);
                    }
                }
            }
            if (_prettyPrint)
            {
                if (wasIndentIncremented2)
                    decrementIndent();
                if (hadElements)
                    indent(_h, _currentIndent);
            }
        }
        else
        {
            // Sequenced object
            // We serialize the sequence normally, except that we don't
            // include the inserted elements, but instead we include the
            // deleted elements and we only serialize references for
            // the unchanged elements
            // We don't pretty print here
            Store s = ((DataObjectImpl) objectXML).getStore();
            // Use the Sequence to populate the children
            // First, look for changes on attributes and serialize those
            StringBuilder unsetString = new StringBuilder();
            for (Change c = ChangeSummaryImpl.getFirstSequenceChange(changes); c !=null; c=c.next2)
            {
                PropertyXML p = (PropertyXML) c.getProperty();
                if ( p != null /*ie text*/ && !p.isXMLElement())
                    if (c.isSet())
                    {
                        Object value = c.getValue();
                        _helper.marshalAttributeProperty(p, value, null, objectXML);
                    }
                    else
                        unsetString.append(XmlPath.getName(p, _h.getNamespaceStack())).append(' ');
            }
            if (unsetString.length() > 0)
                _h.attr(Names.URI_SDO, ChangeSummaryImpl.ATTR_UNSET, null,
                    unsetString.substring(0, unsetString.length() - 1));

            Change change = ChangeSummaryImpl.getFirstSequenceChange(changes);
            int currentArrayPos = change.getArrayPos();
            HashMap<QName, IntegerHolder> elementCount =
                new HashMap<QName, IntegerHolder>();
            // Now process elements/text
            for (int i = 0; i < s.storeSequenceSize(); i++)
            {
                if (change != null && i == currentArrayPos)
                {
                    PropertyXML p = (PropertyXML) change.getProperty();

                    if (p == null)
                    {
                        if (change.isSet())
                        {
                            _h.text((String) change.getValue());
                            i--;
                        }
                        // If change is not set, then this text has been inserted
                    }
                    else if (p.isXMLElement())
                    {
                        if (change.isSet())
                        {
                            if ((change.getValue() instanceof DataObject) && !p.isContainment())
                            {
                                // Reference
                                _helper.marshalSimple(getNodeReference((DataObjectXML) change.getValue(),
                                    _h.getNamespaceStack()),
                                    p.getXMLNamespaceURI(), p.getXMLName(), null, null,
                                    p.getSchemaTypeCode(), false);
                            }
                            else if (change.getValue() == null)
                            {
                                _helper.marshalXsiNil(p.getXMLNamespaceURI(), p.getXMLName(), null,
                                    false);
                            }
                            else
                            {
                                // Delete
                                marshalElementProperty(p, change.getValue(), objectXML, false);
                            }
                            // We have to come back, because we can
                            // have more deletes in the same place
                            i--;
                        }
                        else
                        {
                            // Insert
                            // don't output anything
                            String xmlName = s.storeSequenceGetPropertyXML(i).getXMLName();
                            QName q = new QName(s.storeSequenceGetPropertyXML(i).
                                getXMLNamespaceURI(), xmlName);
                            IntegerHolder ih = elementCount.get(q);
                            if (ih == null)
                                elementCount.put(q, new IntegerHolder(1));
                            else
                                ih.incrementValue();
                        }
                    }
                    else
                    {
                        // Attribute: already handled
                        i--;
                    }
                    change = change.next2;
                    if (change != null)
                        currentArrayPos += change.getArrayPos();
                }
                else
                {
                    // No change in this position, include the original
                    // element/text
                    PropertyXML p = s.storeSequenceGetPropertyXML(i);

                    if ( p == null /*ie is text*/ )
                    {
                        // We still need to output the text, because we
                        // have no way of referring to the original text
                        _h.text((String) s.storeSequenceGetValue(i));
                    }
                    else if ( p!= null /*ie text*/ && p.isXMLElement())
                    {
                        if (!p.getType().isDataType() && !p.isContainment())
                        {
                            _helper.marshalSimple(getNodeReference((DataObjectXML) s.storeSequenceGetValue(i),
                                _h.getNamespaceStack()),
                                p.getXMLNamespaceURI(), p.getXMLName(),
                                s.storeSequenceGetXMLPrefix(i), null, p.getSchemaTypeCode(), false);
                        }
                        else
                        {
                            // We need to build a reference to the element
                            QName q = new QName(p.getXMLNamespaceURI(), p.getXMLName());
                            String ref = null;
                            if (s.storeSequenceGetValue(i) instanceof DataObject)
                            {
                                DataObjectXML valueObject = (DataObjectXML) s.storeSequenceGetValue(i);
                                ref = getNodeReference(valueObject, _h.getNamespaceStack());
                            }
                            if (ref == null)
                            {
                                StringBuilder pathString = new StringBuilder(rb.getPath(objectXML,
                                    rootObject, _h.getNamespaceStack()));
                                pathString.append(ChangeSummaryImpl.PATH_SEPARATOR);
                                String prefix = _h.getNamespaceStack().ensureMapping(p.getXMLNamespaceURI(), null, false, false);
                                if (prefix != null)
                                    pathString.append(prefix).append(':');
                                pathString.append(p.getXMLName());
                                IntegerHolder ih = elementCount.get(q);
                                if (ih == null)
                                {
                                    ih = new IntegerHolder(1);
                                    elementCount.put(q, ih);
                                }
                                else
                                {
                                    ih.incrementValue();
                                }
                                marshalElementWithReference(p.getXMLNamespaceURI(),
                                    p.getXMLName(), pathString.append('[').append(ih.getValue()).
                                        append(']').toString());
                            }
                            else
                            {
                                IntegerHolder ih = elementCount.get(q);
                                if (ih == null)
                                    elementCount.put(q, new IntegerHolder(1));
                                else
                                    ih.incrementValue();
                                marshalElementWithReference(p.getXMLNamespaceURI(),
                                    p.getXMLName(), ref);
                            }
                        }
                    }
                    else
                    {
                        // Attribute: already handled
                    }
                }
            }
            while (change != null)
            {
                // There are more changes at the end of the sequence
                PropertyXML p = (PropertyXML) change.getProperty();

                if (p == null)
                {
                    if (change.isSet())
                        _h.text((String) change.getValue());
                    // If change is not set, then this text has been inserted
                }
                else if (p.isXMLElement())
                {
                    if (change.isSet())
                    {
                        if ((change.getValue() instanceof DataObject) && !p.isContainment())
                        {
                            // Reference
                            _helper.marshalSimple(getNodeReference((DataObjectXML) change.getValue(),
                                _h.getNamespaceStack()),
                                p.getXMLNamespaceURI(), p.getXMLName(), null, null,
                                p.getSchemaTypeCode(), false);
                        }
                        else
                        {
                            // Delete
                            marshalElementProperty(p, change.getValue(), objectXML, false);
                        }
                    }
                    else
                    {
                        // Insert
                        // don't output anything
                    }
                }
                else
                {
                    // Attribute: already handled
                }
                change = change.next2;
            }
        }
        _h.endElement();
    }

    protected void marshalElementProperty(PropertyXML p, Object value, DataObjectXML parent,
        boolean prettyPrint)
    {
        _helper.marshalElementProperty(p, value, null, parent, true, prettyPrint); 
    }

    void setCurrentIndent(int indent)
    {
        _currentIndent = indent;
    }

    protected int getCurrentIndent()
    {
        return _currentIndent;
    }

    private boolean incrementIndent()
    {
        int x = _currentIndent + _indentStep;
        if (x > MAX_INDENT)
            return false;
        _currentIndent = x;
        return true;
    }

    private void decrementIndent()
    {
        _currentIndent -= _indentStep;
    }

    public void setReferenceBuilder(ReferenceBuilder ref)
    {
        _referenceBuilder = ref;
        _helper.setReferenceBuilder(ref);
    }

    private void marshalElementWithReference(String elUri, String elName, String reference)
    {
        _h.startElement(elUri, elName, null, null, null);
        _h.attr(Names.URI_SDO, ChangeSummaryImpl.ATTR_REF, null, reference);
        _h.endElement();
    }

    static class CSReferenceBuilder implements ReferenceBuilder
    {
        private ChangeSummaryImpl cs;
        private HashMap<DataObject, String> pathCache;
        private HashMap<DataObject, String> csPathCache;
        private String basePath;
        private String baseUri;
        private String baseName;
        private String rootUri;
        private String rootName;
        private DataObject xpathStart;

        CSReferenceBuilder(ChangeSummaryImpl cs, String pathToCS, String csUri, String csName,
            String rootUri, String rootName, DataObject xpathStart)
        {
            this.cs = cs;
            basePath = pathToCS;
            baseUri = csUri;
            baseName = csName;
            this.rootUri = rootUri;
            this.rootName = rootName;
            this.xpathStart = xpathStart;
            disablePathCaching();
        }

        /**
         * Caching paths improves performance, but because computing a path has as a side-effect
         * declaration of new prefixes, it is only safe to cache them when the containing
         * namespace context doesn't change, like for example during the construction of 'create'
         * and 'delete' attributes in the change summary
         */
        public void enablePathCaching()
        {
            pathCache = new HashMap<DataObject, String>();
            csPathCache = new HashMap<DataObject, String>();
        }

        public void disablePathCaching()
        {
            pathCache = new UnmodifiableHashMap<DataObject, String>();
            csPathCache = new UnmodifiableHashMap<DataObject, String>();
        }

        public String getPathOrId(DataObjectXML node, DataObject contextNode, NamespaceStack nsstck)
        {
            String result = getId(node);
            if (result != null)
                return result;
            else
                return getPath(node, contextNode, nsstck);
        }

        public String getId(DataObjectXML node)
        {
            return cs.getNodeId(node);
        }

        public String getPath(DataObjectXML node, DataObject contextNode, NamespaceStack nsstck)
        {
            return Names.FRAGMENT + cs.getNodePath(node, contextNode, xpathStart, 
                pathCache, csPathCache, nsstck, basePath, baseUri, baseName, rootUri, rootName,
                false);
        }

        private static class UnmodifiableHashMap<K, V> extends HashMap<K, V>
        {
            public V put(K key, V value)
            {
                // Cannot be modified, but doesn't throw exception if someone tries to
                return null;
            }
        }
    }

    private String getNodeReference(DataObjectXML node, NamespaceStack nsstck)
    {
        String tryid = _referenceBuilder.getId(node);
        if (tryid != null)
            return tryid;
        else
            return _referenceBuilder.getPath(node, null, nsstck);
    }

    private static class IntegerHolder
    {
        int i;

        IntegerHolder(int i)
        {
            this.i = i;
        }

        int getValue()
        {
            return i;
        }

        void incrementValue()
        {
            i++;
        }
    }

    /**
     * Used to iterate over all the changes corresponding to elements in the order in which
     * they appear in the type declaration
     */
    private static class ChangeIterator
    {
        private Change[] _changes;
        List<PropertyXML> _properties;
        DataObjectXML _parent;
        int _index, _indexChanges;
        Change _currentChange;

        ChangeIterator(DataObjectXML parent, Change[] changes)
        {
            _properties = parent.getTypeXML().getPropertiesXML();
            _parent = parent;
            _changes = changes;
            _index = _indexChanges = 0;
        }

        /**
         * First, walk through all the properties declared in the parent type, in order
         * Then walk over all properties in the changes list that are "open-content"
         * @return the next Change object or null
         */
        Change next()
        {
            Change result = null;
            while (_index < _properties.size())
            {
                PropertyXML prop = _properties.get(_index++);
                if (prop.isXMLElement())
                    result = findChange(prop);
                if (result != null)
                    return result;
            }
            // Walk the _changes array now
            advanceChange();
            do
            {
                while (_currentChange == null && _indexChanges < _changes.length)
                    advanceChange();
                if (_currentChange == null ||
                    _currentChange.getProperty().isOpenContent())
                    break;
                else
                    advanceChange();
            }
            while (true);
            return _currentChange;
        }

        private void advanceChange()
        {
            if (_currentChange != null)
                _currentChange = _currentChange.next;
            else if (_indexChanges < _changes.length)
                _currentChange = _changes[_indexChanges++];
            else
                _currentChange = null;
        }

        private Change findChange(PropertyXML prop)
        {
            int hashCode = ChangeSummaryImpl.hashCode(prop, _changes.length);
            Change change = _changes[hashCode];
            while (change != null)
            {
                if (change.getProperty() == prop)
                    break;
                change = change.next;
            }
            return change;
        }
    }

    /*
     * @see davos.sdo.impl.marshal.Marshaller#setSaver(davos.sdo.impl.marshal.SDOEventModel)
     */
    void setSaver(Saver e)
    {
        _h = e;
        _helper.setSaver(e);
    }
}
