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

import davos.sdo.DataObjectXML;
import davos.sdo.ListXMLIterator;
import davos.sdo.PropertyXML;
import davos.sdo.SDOError;
import davos.sdo.SDOUnmarshalException;
import davos.sdo.SequenceXML;
import davos.sdo.TypeXML;
import davos.sdo.impl.data.ChangeSummaryImpl;
import davos.sdo.impl.data.DataFactoryImpl;
import davos.sdo.impl.type.BuiltInTypeSystem;
import davos.sdo.impl.type.SimpleValueHelper;
import davos.sdo.impl.xpath.XPath;
import davos.sdo.impl.common.Common;
import davos.sdo.impl.common.Names;
import davos.sdo.impl.common.ChangeSummaryXML;
import javax.sdo.DataObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.LinkedHashMap;
import java.util.Iterator;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;

import org.apache.xmlbeans.SchemaType;

public class PlainChangeSummaryUnmarshaller extends Unmarshaller implements ChangeSummaryXML
{
    private Set<Object> deletedPaths = new HashSet<Object>();
    private Set<Object> insertedPaths = new HashSet<Object>();
    private ObjectReference currentObject;
    private List<Change> changeList;
    private Change currentChange;
    private Node currentNode;
    private Node currentSibling;
    private final List<Att> seenXmlns = new ArrayList<Att>();
    private HashMap<ObjectReference, List<Change>> changedObjects =
        new LinkedHashMap<ObjectReference, List<Change>>();
    private ReferenceResolver referenceResolver;
    boolean logging = true;
    private Object savedOptions;

    PlainChangeSummaryUnmarshaller(Object options)
    {
        savedOptions = options;
    }

    /*
    * While reading the change summary, we can be in one of the
    * following states:
    * - on the <changeSummary> element itself: currentObject == null
    * - on one of the direct children of the <changeSummary> element:
    *          currentObject != null && changeList != null
    * - inside a direct child of <changeSummary>: currentChange != null
    * - inside the representation of a complex element (a deleted item):
    *          currentNode != null
    */

    public void attr(String uri, String local, String prefix, String value)
    {
        if (currentObject == null)
        {
            if (ChangeSummaryImpl.CHANGE_SUMMARY_CREATE.equals(local))
            {
                StringTokenizer st = new StringTokenizer(value);
                while (st.hasMoreTokens())
                {
                    String item = st.nextToken();
                    insertedPaths.add(compilePathOrId(item, _nsHandler));
                }
            }
            else if (ChangeSummaryImpl.CHANGE_SUMMARY_DELETE.equals(local))
            {
                StringTokenizer st = new StringTokenizer(value);
                while (st.hasMoreTokens())
                    deletedPaths.add(compilePathOrId(st.nextToken(), _nsHandler));
            }
            else if (PlainChangeSummaryMarshaller.ATTR_LOGGING.equals(local))
            {
                logging = !"false".equalsIgnoreCase(value);
            }
        }
        else if (Names.URI_SDO.equals(uri))
        {
            if (ChangeSummaryImpl.ATTR_REF.equals(local))
            {
                if (currentChange == null)
                {
                    // Reference to the changed object
                    currentObject.setRef(compilePathOrId(value, _nsHandler));
                }
                else
                {
                    // Reference to one of the changes
                    currentChange.setRef(compilePathOrId(value, _nsHandler));
                    currentChange.setKind(Change.REFERENCE);
                }
            }
            else if (ChangeSummaryImpl.ATTR_UNSET.equals(local) &&
                currentObject != null && currentChange == null)
            {
                StringTokenizer st = new StringTokenizer(value);
                List<QName> qnameList = new ArrayList<QName>();
                currentObject.setUnset(qnameList);
                while (st.hasMoreTokens())
                    qnameList.add(resolveQName(st.nextToken(), _nsHandler));
            }
        }
        else
        {
            if (currentChange == null)
            {
                Change c = new Change(uri, local);
                c.setKind(Change.SIMPLE);
                c.setAttribute(true);
                c.setValue(value);
                // We don't know for sure at this point what the type is going to be
                // We have to preserve the prefix declarations in effect at this point in case
                // it turns out it is a path
                if (value.startsWith(Names.FRAGMENT))
                    c.setPrefixMap(_nsHandler.savePrefixMap());
                changeList.add(c);
            }
            else
            {
                if (currentNode == null)
                {
                    currentNode = new Node(currentChange.getUri(),
                        currentChange.getName(), null, null, null, null);
                    currentNode.setPrefixMap(_nsHandler.savePrefixMap());
                    currentChange.setKind(Change.DELETE);
                    currentChange.setValue(currentNode);
                }
                currentNode.addAttribute(uri, local, prefix, value);
                currentSibling = null;
            }
        }
    }

    public void endElement()
    {
        if (currentNode != null)
        {
            Node parent = currentNode.getParent();
            currentSibling = currentNode;
            currentNode = parent;
            if (currentNode == null)
                currentChange = null;
        }
        else if (currentChange != null)
        {
            // We have to check whether we have seen an empty element
            if (currentChange.getRef() == null && currentChange.getValue() == null)
            {
                if (currentChange.isNilled())
                    currentChange.setKind(Change.SIMPLE);
                else
                {
                    currentChange.setKind(Change.DELETE);
                    currentChange.setValue(new Node(currentChange.getUri(),
                        currentChange.getName(), null, null, null, null));
                }
            }
            currentChange = null;
        }
        else if (currentObject != null)
        {
            currentObject = null;
        }
        else
            _loader.changeUnmarshaller(_link); // We are done
    }

    public void sattr(int type, String name, String value)
    {
        if (type == ATTR_XSI)
        {
            if (currentNode != null)
                currentNode.addAttribute(Names.URI_XSD_INSTANCE, name, null, value);
            else if (currentChange != null && Names.XSI_NIL.equals(name) && "true".equalsIgnoreCase(value))
                currentChange.setNilled(true);
        }
    }

    public void startElement(String uri, String name, String prefix,
        String xsiTypeUri, String xsiTypeName)
    {
//        if (EMPTY_STRING.equals(uri) && "changeSummary".equals(name))
//            return;
        if (currentObject == null)
        {
            // New changed object
            currentObject = new ObjectReference(uri, name);
            changeList = new ArrayList<Change>();
            changedObjects.put(currentObject, changeList);
        }
        else if (currentChange == null)
        {
            currentChange = new Change(uri, name);
            changeList.add(currentChange);

            if (xsiTypeName != null)
            {
                currentNode = new Node(uri, name, prefix, xsiTypeUri, xsiTypeName, null);
                currentSibling = null;
                for (Att a : seenXmlns)
                    currentNode.addAttribute(a);
                seenXmlns.clear();
                currentChange.setKind(Change.DELETE);
                currentChange.setValue(currentNode);
            }
        }
        else
        {
            if (currentNode == null)
            {
                currentNode = new Node(currentChange.getUri(), currentChange.
                    getName(), null, null, null, null);
                currentSibling = null;
                for (Att a : seenXmlns)
                    currentNode.addAttribute(a);
                seenXmlns.clear();
                String text =  null;
                if (currentChange.getKind() == Change.SIMPLE)
                    text = (String) currentChange.getValue();
                currentChange.setKind(Change.DELETE);
                currentChange.setValue(currentNode);
                if (text != null)
                {
                    currentNode.setLeadText(text);
                    // We don't know for sure at this point what the type is going to be
                    // We have to preserve the prefix declarations in effect at this point in case
                    // it turns out it is a path
                    if (text.startsWith(Names.FRAGMENT))
                        currentNode.setPrefixMap(_nsHandler.savePrefixMap());
                }
            }
            Node n = new Node(uri, name, prefix, xsiTypeUri, xsiTypeName, currentNode);
            if (currentSibling == null)
                currentNode.setFirstChild(n);
            else
                currentSibling.setNextSibling(n);
            currentNode = n;
            currentSibling = null;
        }
    }

    public void text(char[] buff, int off, int cch)
    {
        text(new String(buff, off, cch));
    }

    public void text(String s)
    {
        if (s.length() == 0)
            return;
        if (currentNode != null)
        {
            if (currentSibling != null)
            {
                String text = currentSibling.getNextText();
                currentSibling.setNextText(text == null ? s : text + s);
            }
            else
            {
                String text = currentNode.getLeadText();
                if (text == null)
                {
                    currentNode.setLeadText(s);
                    if (s.startsWith(Names.FRAGMENT))
                        currentNode.setPrefixMap(_nsHandler.savePrefixMap());
                }
                else
                    currentNode.setLeadText(text + s);
            }
        }
        else if (currentChange != null)
        {
            if (currentChange.getKind() == Change.SIMPLE)
            {
                String text = (String) currentChange.getValue();
                currentChange.setValue(text == null ? s : text + s);
            }
            else if (currentChange.getKind() != Change.REFERENCE)
            {
                currentChange.setKind(Change.SIMPLE);
                currentChange.setValue(s);
            }
        }
        else if (currentObject != null)
        {
            // Text as content of a top-level element
            Change c = new Change(null, null);
            c.setKind(Change.SIMPLE);
            c.setValue(s);
            // We don't know for sure at this point what the type is going to be
            // We have to preserve the prefix declarations in effect at this point in case
            // it turns out it is a path
            if (s.startsWith(Names.FRAGMENT))
                c.setPrefixMap(_nsHandler.savePrefixMap());
            changeList.add(c);
        }
    }

    public void xmlDecl(String version, String encoding)
    {
    }

    public void xmlns(String prefix, String uri)
    {
        if (currentNode != null)
            currentNode.addAttribute(uri, null, prefix, null);
        else
            seenXmlns.add(new Att(uri, null, prefix, null));
    }

    void setReferenceResolver(ReferenceResolver r)
    {
        referenceResolver = r;
    }

    void finish()
    {
    }

    /*
     * This class represents an unresolved reference
     */
    private class ObjectReference implements ChangedObjectXML
    {
        private String uri;
        private String name;
        private Object ref;
        private List<QName> unset;

        ObjectReference(String uri, String name)
        {
            this.uri = uri;
            this.name = name;
        }

        public Object getRef()
        {
            return ref;
        }

        public void setRef(Object ref)
        {
            this.ref = ref;
        }

        public List<QName> getUnset()
        {
            return unset;
        }

        public void setUnset(List<QName> unset)
        {
            this.unset = unset;
        }

        public String getName()
        {
            return name;
        }

        public String getUri()
        {
            return uri;
        }

        public QName getElementName()
        {
            return new QName(uri, name);
        }

        public Iterator getChangesIterator()
        {
            return changedObjects.get(this).iterator();
        }

        public String toString()
        {
            StringBuilder sb = new StringBuilder();
            sb.append(name).append('@').append(uri).append('=').append(ref);
            return sb.toString();
        }
    }

    /*
     * This class represents a tracked change
     */
    private static class Change implements ChangeXML
    {
        private String uri;
        private String name;
        private int kind;
        public static final int UNSET = 0;
        public static final int SIMPLE = 1;
        public static final int REFERENCE = 2;
        public static final int DELETE = 3;
        private Object ref;
        private boolean attribute;
        private Object value;
        private Map<String, String> prefixMap; // in case the value is a path
        private PropertyXML resolvedProperty;
        private Object resolvedValue;
        boolean nilled;

        public Change(String uri, String name)
        {
            this.uri = uri;
            this.name = name;
        }

        public void setKind(int k)
        {
            kind = k;
        }

        public int getKind()
        {
            return kind;
        }

        public String getName()
        {
            return name;
        }

        public String getUri()
        {
            return uri;
        }

        public Object getRef()
        {
            return ref;
        }

        public void setRef(Object ref)
        {
            this.ref = ref;
        }

        public void setAttribute(boolean a)
        {
            attribute = a;
        }

        public boolean isAttribute()
        {
            return attribute;
        }

        public void setValue(Object v)
        {
            value = v;
        }

        public Object getValue()
        {
            return value;
        }

        public Map<String, String> getPrefixMap()
        {
            return prefixMap;
        }

        public void setPrefixMap(Map<String, String> prefixMap)
        {
            this.prefixMap = prefixMap;
        }

        public void setNilled(boolean nilled)
        {
            this.nilled = nilled;
        }

        public boolean isNilled()
        {
            return nilled;
        }

        // =================================================================
        // ChangeSummaryXML.ChangeXML implementation
        // =================================================================
        public QName getQName()
        {
            return new QName(uri, name);
        }

        public boolean isElement()
        {
            return name != null && !attribute;
        }

        public void setResolvedProperty(PropertyXML resolvedProperty)
        {
            this.resolvedProperty = resolvedProperty;
        }

        public PropertyXML getProperty()
        {
            return resolvedProperty;
        }

        public void setResolvedValue(Object resolvedValue)
        {
            this.resolvedValue = resolvedValue;
        }

        public Object getResolvedValue()
        {
            return resolvedValue;
        }

        public String toString()
        {
            StringBuilder sb = new StringBuilder();
            sb.append('<').append(name).append('@').append(uri).append('-');
            switch(kind)
            {
            case SIMPLE:
                sb.append("SIMPLE");
                sb.append(", value=").append(value);
                break;
            case REFERENCE:
                sb.append("REFERENCE");
                sb.append(", value=").append(ref);
                break;
            case DELETE:
                sb.append("DELETE");
                sb.append(", value=").append(value.toString());
                break;
            }
            sb.append('>');
            return sb.toString();
        }
    }

    /**
     * This class represents an XML node
     */
    private static class Node
    {
        private String uri;
        private String name;
        private String prefix;
        private Node firstChild;
        private Node nextSibling;
        private String leadText;
        private Map<String, String> prefixMap; // in case the text is a reference
        private String nextText;
        private Node parent;
        // TODO(radup) Add a separate list for prefix declarations so that they can be looked
        // at before other attributes
        private List<Att> attributes;
        private String xsiTypeUri;
        private String xsiTypeName;

        public Node(String uri, String name, String prefix,
            String xsiTypeUri, String xsiTypeName, Node parent)
        {
            this.uri = uri;
            this.name = name;
            this.prefix = prefix;
            this.xsiTypeUri = xsiTypeUri;
            this.xsiTypeName = xsiTypeName;
            this.parent = parent;
        }

        public String getName()
        {
            return name;
        }

        public String getPrefix()
        {
            return prefix;
        }

        public String getUri()
        {
            return uri;
        }

        public Node getParent()
        {
            return parent;
        }

        public Node getFirstChild()
        {
            return firstChild;
        }

        public void setFirstChild(Node firstChild)
        {
            this.firstChild = firstChild;
        }

        public Node getNextSibling()
        {
            return nextSibling;
        }

        public void setNextSibling(Node nextSibling)
        {
            this.nextSibling = nextSibling;
        }

        public String getLeadText()
        {
            return leadText;
        }

        public void setLeadText(String leadText)
        {
            this.leadText = leadText;
        }

        public Map<String, String> getPrefixMap()
        {
            return prefixMap;
        }

        public void setPrefixMap(Map<String, String> prefixMap)
        {
            this.prefixMap = prefixMap;
        }

        public String getNextText()
        {
            return nextText;
        }

        public void setNextText(String nextText)
        {
            this.nextText = nextText;
        }

        public List<Att> getAttributes()
        {
            return attributes;
        }

        public void addAttribute(String uri, String name, String prefix, String value)
        {
            if (attributes == null)
                attributes = new ArrayList<Att>();
            Att a = new Att(uri, name, prefix, value);
            attributes.add(a);
        }

        public void addAttribute(Att a)
        {
            if (attributes == null)
                attributes = new ArrayList<Att>();
            attributes.add(a);
        }

        public String getXsiTypeUri()
        {
            return xsiTypeUri;
        }

        public String getXsiTypeName()
        {
            return xsiTypeName;
        }
    }

    private static class Att
    {
        private String uri;
        // local == null means an xmlns attribute
        private String local;
        private String prefix;
        private String value;

        public Att(String uri, String local, String prefix, String value)
        {
            this.uri = uri;
            this.local = local;
            this.prefix = prefix;
            this.value = value;
        }

        public String getUri()
        {
            return uri;
        }

        public String getLocal()
        {
            return local;
        }

        public String getPrefix()
        {
            return prefix;
        }

        public String getValue()
        {
            return value;
        }
    }

    private static class CSReferenceResolver implements ReferenceResolver
    {
        private Map<String, DataObject> _idMap;
        private ReferenceResolver _backupReferenceResolver;

        CSReferenceResolver(ChangeSummaryImpl cs,
            ReferenceResolver backUpReferenceResolver)
        {
            _idMap = new HashMap<String, DataObject>();
            cs.buildOldIdMap(_idMap);
            _backupReferenceResolver = backUpReferenceResolver;
        }

        public DataObject resolvePath(XPath path, DataObjectXML contextNode)
        {
            return _backupReferenceResolver.resolvePath(path, contextNode);
        }

        public DataObject resolveId(String id)
        {
            if (_idMap.containsKey(id))
                return _idMap.get(id);
            else
                return _backupReferenceResolver.resolveId(id);
        }

        public void registerId(String id, DataObject node)
        {
            _idMap.put(id, node);
        }

        public void setIdMap(Map<String, DataObject> map)
        {
            _idMap = map;
        }
    }

    private DataFactoryImpl getDataFactoryImpl()
    {
        return (DataFactoryImpl)_sdoContext.getDataFactory();
    }

    public void setRootObject(DataObject root)
    {
        ChangeSummaryImpl cs = (ChangeSummaryImpl) root.getChangeSummary();
        ReferenceResolver rr = new CSReferenceResolver(cs, referenceResolver);
        DataObjectXML rootXML = (DataObjectXML) root;
        // We need to collect unresolved references from all the objects
        // that we parse (deleted objects)
        // and resolve them all at the end
        List<DataObjectXML> io = new ArrayList<DataObjectXML>();
        List<PropertyXML> ip = new ArrayList<PropertyXML>();
        List<Object> iv = new ArrayList<Object>();
        // We also need to keep track of all changes that point to a reference
        // so we can resolve these as well
        List<ChangeSummaryImpl.Change> ic = new ArrayList<ChangeSummaryImpl.Change>();
        List<DataObjectXML> in = new ArrayList<DataObjectXML>();
        Set<DataObject> insertedObjects = cs.getInsertedObjects();
        for (Object p : insertedPaths)
        {
            DataObject o = (DataObject) resolveObjectReference(p, rootXML, rr);
            insertedObjects.add(o);
        }
        Map<DataObject, DataObject> deletedObjects = cs.getDeletedObjects();
        Map<DataObject, ChangeSummaryImpl.Change[]> modifiedObjects =
            cs.getModifiedObjects();
        cs.setChangeSummaryXMLDelegator(this);
        // In order to avoid inadvertently adding a bogus property to the context
        // we create the property using the internal API
        final PropertyXML MARKER_PROPERTY = davos.sdo.impl.type.PropertyImpl.create(
            BuiltInTypeSystem.STRING, Names.PREFIX_SDO, true, true, null, null, false, false,
            null, Common.EMPTY_STRING_LIST);

        for (Map.Entry<ObjectReference, List<Change>> entry : changedObjects.entrySet())
        {
            ObjectReference r = entry.getKey();
            DataObjectXML parent = resolveDataObjectReference(r, rootXML, rr);
            ChangeSummaryImpl.Change[] chgs =
                new ChangeSummaryImpl.Change[ChangeSummaryImpl.computeHashMapSize(parent)];
            modifiedObjects.put(parent, chgs);
            // Change represents a change read from the file
            // We need to turn it into a change object with
            // references to actual DataObjects
            if (parent.getSequence() == null)
            {
                // Process the unsets
                List<QName> unsetList = r.getUnset();
                if (unsetList != null)
                    for (QName unsetPropName : unsetList)
                    {
                        PropertyXML prop = findPropertyByName(parent,
                            unsetPropName.getNamespaceURI(), unsetPropName.getLocalPart(), true);
                        // This is a hack because we can't differentiate between attribute and
                        // element properties
                        if (prop == null)
                            prop = findPropertyByName(parent, unsetPropName.getNamespaceURI(),
                                unsetPropName.getLocalPart(), false);
                        ChangeSummaryImpl.Change newChange = null;
                        if (prop != null && (prop.getType().isDataType() || !prop.isContainment()))
                        {
                            // Add a new 'unset' change
                            newChange = new ChangeSummaryImpl.Change(prop, null, false, -1);
                        }
                        else if (prop == null)
                        {
                            prop =findOnDemandPropertyByName(parent,
                                unsetPropName.getNamespaceURI(), unsetPropName.getLocalPart());
                            if (prop == null)
                            {
                                // Don't do anything: if a property was unset and is currently
                                // still unset, then there is nothing to capture
                            }
                            else if (prop.getType().isDataType() || !prop.isContainment())
                            {
                                newChange = new ChangeSummaryImpl.Change(prop, null, false, -1);
                            }
                        }
                        if (newChange != null)
                        {
                            int hashCode = ChangeSummaryImpl.hashCode(prop, chgs.length);
                            newChange.next = chgs[hashCode];
                            chgs[hashCode] = newChange;
                        }
                    }
                ListIterator<Change> iter = entry.getValue().listIterator();
                while (iter.hasNext())
                {
                    Change c = iter.next();
                    PropertyXML prop;
                    if (c.getName() == null)
                        if (parent.getTypeXML().isSimpleContent())
                            prop = parent.getTypeXML().getPropertyXML(Names.SIMPLE_CONTENT_PROP_NAME);
                        else
                            continue; // Type parentType is not mixed, so it cannot have text changes
                    else
                        prop = findPropertyByName(parent, c.getUri(), c.getName(), c.isElement());
                    if (prop != null && (prop.getType().isDataType() || !prop.isContainment()))
                    {
                        if (c.getKind() != Change.SIMPLE &&
                            !(c.getKind() == Change.DELETE && isSimpleNode((Node) c.getValue()) ||
                                // We allow references to simple values if the property is many
                                // even though we don't generate them
                                c.getKind() == Change.REFERENCE && prop.isMany()))
                            throw new SDOUnmarshalException(SDOError.messageForCodeAndLocation(
                                "unmarshal.changesummary.notmodification", _locator, prop.getName()));
                        c.setResolvedProperty(prop);
                        String oldValue;
                        if (c.getKind() == Change.SIMPLE)
                            oldValue = (String) c.getValue();
                        else if (c.getKind() == Change.REFERENCE)
                            oldValue = Common.EMPTY_STRING;
                        else
                        {
                            oldValue = ((Node) c.getValue()).getLeadText();
                            if (oldValue == null)
                                oldValue = Common.EMPTY_STRING;
                        }
                        int hashCode = ChangeSummaryImpl.hashCode(prop, chgs.length);
                        if (prop.isMany())
                        {
                            List result;
                            if (chgs[hashCode] != null && prop == chgs[hashCode].getProperty())
                                result = (List) chgs[hashCode].getValue();
                            else
                            {
                                result = new ArrayList();
                                ChangeSummaryImpl.Change newChange = new ChangeSummaryImpl.Change(
                                    prop, result, true, -1);
                                c.setResolvedValue(result);
                                newChange.next = chgs[hashCode];
                                chgs[hashCode] = newChange;
                                if (!prop.getType().isDataType())
                                {
                                    ic.add(newChange);
                                    in.add(parent);
                                }
                            }
                            TypeXML type = prop.getTypeXML();
                            int schemaTypeCode = prop.getSchemaTypeCode();
                            if (!type.isDataType())
                                type = BuiltInTypeSystem.STRING;
                            else if (c.getKind() == Change.DELETE)
                            {
                                Node node = (Node) c.getValue();
                                type = getXsiTypeFromNode(node, type);
                                schemaTypeCode = getXsiSchemaTypeCodeFromNode(node, schemaTypeCode);
                            }

                            try {
                                // If this is a reference, we get the value directly
                                Object converted;
                                if (c.getKind() == Change.REFERENCE)
                                    converted = resolveObjectReference(c.getRef(), rootXML, rr);
                                else
                                    converted = oldValue == null ? null : SimpleValueHelper.
                                        parseBufferToType(oldValue, type, schemaTypeCode, _nsHandler);
                                // We need to compile the path if the value represents a path
                                // If this is a reference to a reference, this might not work
                                if (prop.getType().isDataType())
                                    result.add(converted);
                                else
                                    result.add(compilePathOrId((String) converted, c.getPrefixMap()));
                            } catch (SimpleValueHelper.SimpleValueException e)
                            {
                                throw new SDOUnmarshalException(getMessageForSimpleValueException(
                                    e, oldValue, type, prop));
                            }
                        }
                        else
                        {
                            TypeXML type = prop.getTypeXML();
                            int schemaTypeCode = prop.getSchemaTypeCode();
                            if (!type.isDataType())
                                type = BuiltInTypeSystem.STRING;
                            else if (c.getKind() == Change.DELETE)
                            {
                                Node node = (Node) c.getValue();
                                type = getXsiTypeFromNode(node, type);
                                schemaTypeCode = getXsiSchemaTypeCodeFromNode(node, schemaTypeCode);
                            }

                            try {
                            Object converted = oldValue == null ? null : SimpleValueHelper.
                                parseBufferToType(oldValue, type, schemaTypeCode, _nsHandler);
                            ChangeSummaryImpl.Change newChange = new ChangeSummaryImpl.Change(prop,
                                converted, true, -1);
                            c.setResolvedValue(converted);
                            newChange.next = chgs[hashCode];
                            chgs[hashCode] = newChange;
                            if (!prop.getType().isDataType())
                            {
                                ic.add(newChange);
                                newChange.setValue(compilePathOrId((String) converted,
                                    c.getPrefixMap()));
                                in.add(parent);
                            }
                            } catch (SimpleValueHelper.SimpleValueException e)
                            {
                                throw new SDOUnmarshalException(getMessageForSimpleValueException(
                                    e, oldValue, type, prop));
                            }
                        }
                    }
                    else if (prop == null || prop.isMany())
                    {
                        // We take advantage of the fact that on-demand properties, upon
                        // unmarshalling, are multi-valued
                        boolean onDemand = false;
                        if (prop == null)
                        {
                            onDemand = true;
                            prop = findOnDemandPropertyByName(parent, c.getUri(), c.getName());
                            if (prop == null)
                                prop = getDataFactoryImpl().getRootProperty(c.getUri(),
                                    c.getName(), BuiltInTypeSystem.STRING, true);
                        }
                        int arrayIndex = 0;
                        int hashCode = ChangeSummaryImpl.hashCode(prop, chgs.length);
                        // We skip over references to existing nodes and
                        // in a first step, record only deletions
                        PropertyXML p = prop;
                        ChangeSummaryImpl.Change lastChange = null;
                        int currentIndex = 0;
                        ListIterator itnew = parent.getList(prop).listIterator();
                        PropertyXML originalProp = prop;
                        processArray:
                        while (p == prop)
                        {
                            c.setResolvedProperty(prop);
                            if (c.getKind() == Change.SIMPLE)
                            {
                                DataObject oldValue = parseDataObjectFromSimple(
                                    c.getUri(), c.getName(), (String) c.getValue(), c.isNilled(),
                                    parent, prop, io, ip, iv);
                                Object value;
                                if (onDemand)
                                    value = getOpenContentValue(oldValue, prop);
                                else
                                    value = oldValue;
                                ChangeSummaryImpl.Change newChange =
                                    new ChangeSummaryImpl.Change(prop, value, true, arrayIndex -
                                        currentIndex);
                                c.setResolvedValue(value);
                                currentIndex = arrayIndex;
                                if (lastChange == null)
                                {
                                    newChange.next = chgs[hashCode];
                                    chgs[hashCode] = newChange;
                                }
                                else
                                    lastChange.next2 = newChange;
                                lastChange = newChange;
                                if (value == oldValue)
                                    deletedObjects.put(oldValue, parent);
                            }
                            else if (c.getKind() == Change.REFERENCE)
                            {
                                Object newObject = itnew.hasNext() ? itnew.next() : null;
                                DataObject o = (DataObject) resolveObjectReference(c.getRef(),
                                    rootXML, rr);
                                c.setResolvedValue(o);
                                if (o != null && newObject != null)
                                {
                                    while (newObject != null && newObject != o)
                                    {
                                        ChangeSummaryImpl.Change newChange =
                                            new ChangeSummaryImpl.Change(prop, newObject, false,
                                                arrayIndex - currentIndex);
                                        currentIndex = arrayIndex++;
                                        if (lastChange == null)
                                        {
                                            newChange.next = chgs[hashCode];
                                            chgs[hashCode] = newChange;
                                        }
                                        else
                                            lastChange.next2 = newChange;
                                        lastChange = newChange;
                                        newObject = itnew.hasNext() ? itnew.next() : null;
                                    }
                                    if (newObject != null)
                                        arrayIndex++;
                                }
                            }
                            else if (c.getKind() == Change.DELETE)
                            {
                                DataObject oldValue = parseDataObjectFromNode(
                                    (Node) c.getValue(), parent, prop, rr, io, ip, iv);
                                Object value;
                                if (onDemand)
                                {
                                    PropertyXML newProp = getNewOpenContentProperty(oldValue, prop);
                                    value = getOpenContentValue(oldValue, newProp);
                                    if (newProp != prop)
                                    {
                                        if (lastChange != null)
                                            fixChangeSummary(prop, newProp, chgs[hashCode],
                                                deletedObjects, parent);
                                        prop = newProp;
                                        c.setResolvedProperty(prop);
                                    }
                                }
                                else
                                    value = oldValue;
                                ChangeSummaryImpl.Change newChange =
                                    new ChangeSummaryImpl.Change(prop, value, true, arrayIndex -
                                        currentIndex);
                                c.setResolvedValue(value);
                                currentIndex = arrayIndex;
                                if (lastChange == null)
                                {
                                    newChange.next = chgs[hashCode];
                                    chgs[hashCode] = newChange;
                                }
                                else
                                    lastChange.next2 = newChange;
                                lastChange = newChange;
                                if (value == oldValue)
                                    deletedObjects.put(oldValue, parent);
                            }
                            if (iter.hasNext())
                            {
                                c = iter.next();
                                // Move the iterator over all the changes with name==null
                                // or to the last position if all the remaining changes have
                                // name==null
                                while (c.getName() == null)
                                    if (iter.hasNext())
                                        c = iter.next();
                                    else
                                        break processArray;
                                if (c.getName() != null)
                                {
                                    p = findPropertyByName(parent, c.getUri(), c.getName(),
                                        c.isElement());
                                    if (p == null && onDemand &&
                                        prop.getXMLName().equals(c.getName()))
                                        p = prop;
                                }
                            }
                            else
                                break;
                        }
                        // Add inserts for the remaining items
                        while (itnew.hasNext())
                        {
                            ChangeSummaryImpl.Change newChange = new ChangeSummaryImpl.Change(
                                prop, itnew.next(), false, arrayIndex - currentIndex);
                            currentIndex = arrayIndex++;
                            if (lastChange == null)
                            {
                                newChange.next = chgs[hashCode];
                                chgs[hashCode] = newChange;
                            }
                            else
                            {
                                lastChange.next2 = newChange;
                            }
                            lastChange = newChange;
                        }
                        if (prop != originalProp)
                        {
                            // It means we need to update the original object because it had an
                            // on-demand property that changed on it
                            fixObjectNonSequenced(originalProp, prop, parent);
                        }
                        if (p != prop)
                            // If we're here, it means that the iterator still had items
                            iter.previous();
                    }
                    else
                    {
//                        if (prop == null)
//                        {
//                            prop = findOnDemandPropertyByName(parent, c.getName());
//                            if (prop == null)
//                                prop = getDataFactoryImpl().getRootProperty(c.getUri(),
//                                    c.getName(), BuiltInTypeSystem.STRING, true);
//                            DataObject oldValue = c.getKind() == Change.SIMPLE ?
//                                parseDataObjectFromSimple(c.getUri(), c.getName(),
//                                    (String) c.getValue(), parent, prop, true, io, ip, iv) :
//                                parseDataObjectFromNode((Node) c.getValue(),
//                                    parent, prop, true, rr, io, ip, iv);
//                            prop = (PropertyXML) oldValue.getProperty(c.getName());
//                            Object value = oldValue.getList(prop).get(0);
//                            assert prop != null;
//                            int hashCode = ChangeSummaryImpl.hashCode(prop, chgs.length);
//                            // Look forward to see if there are other properties like this one
//                            String uri = prop.getXMLNamespaceURI();
//                            String name = prop.getXMLName();
//                            ChangeSummaryImpl.Change newChange = new ChangeSummaryImpl.Change(
//                                prop, value, true, 0);
//                            newChange.next = chgs[hashCode];
//                            chgs[hashCode] = newChange;
//                            if (value instanceof DataObject)
//                                deletedObjects.put((DataObject) value, parent);
//                            ChangeSummaryImpl.Change lastChange = newChange;
//                            while(iter.hasNext())
//                            {
//                                c = iter.next();
//                                if (c.getKind() == Change.DELETE || c.getKind() == Change.SIMPLE&&
//                                    (uri == null ?
//                                        c.getUri() == null && c.getName().equals(name) :
//                                        uri.equals(c.getUri()) && c.getName().equals(name)))
//                                {
//                                    oldValue = parseDataObjectFromNode(
//                                        (Node) c.getValue(), parent, prop, rr, io, ip, iv);
//                                    newChange =  new ChangeSummaryImpl.Change(prop,oldValue,true,0);
//                                    lastChange.next2 = newChange;
//                                    lastChange = newChange;
//                                    deletedObjects.put(oldValue, parent);
//                                }
//                                else
//                                {
//                                    iter.previous();
//                                    break;
//                                }
//                            }
//                        }
//                        else
                        {
                            if (c.getKind() == Change.REFERENCE)
                                throw new SDOUnmarshalException(SDOError.messageForCodeAndLocation(
                                    "unmarshal.changesummary.notmultiple", _locator, prop.getName()));
                            c.setResolvedProperty(prop);
                            DataObject oldValue = c.getKind() == Change.SIMPLE ?
                                parseDataObjectFromSimple(c.getUri(), c.getName(),
                                    (String) c.getValue(), c.isNilled(), parent, prop, io, ip, iv):
                                parseDataObjectFromNode((Node) c.getValue(),
                                    parent, prop, rr, io, ip, iv);
                            ChangeSummaryImpl.Change newChange =  new ChangeSummaryImpl.Change(
                                prop, oldValue, true, -1);
                            c.setResolvedValue(oldValue);
                            int hashCode = ChangeSummaryImpl.hashCode(prop, chgs.length);
                            newChange.next = chgs[hashCode];
                            chgs[hashCode] = newChange;
                            deletedObjects.put(oldValue, parent);
                            // We need to cover the case where the new value is a 'null'
                            // because the 'null' won't show up in the list of inserted values
                            if (parent.isSet(prop) && parent.get(prop) == null)
                                cs.logInsertionHelper(false, prop, -1, null, chgs);
                        }
                    }
                }
            }
            else
            {
                SequenceXML newSequence = parent.getSequenceXML();
                ArrayList<PropertyXML> oldProperties = new ArrayList<PropertyXML>();
                ArrayList<Object> oldValues = new ArrayList<Object>();
                Map<String, PropertyXML> onDemandProperties = new HashMap<String, PropertyXML>();
                for (Change c : entry.getValue())
                {
                    if (c.isAttribute())
                    {
                        PropertyXML prop = findAttributePropertyByName(newSequence,
                            c.getUri(), c.getName());
                        if (prop == null)
                        {
                            prop = getDataFactoryImpl().getRootProperty(null, c.getName(),
                                BuiltInTypeSystem.STRING, false);
                        }
                        oldProperties.add(prop);
                        TypeXML type = prop.getTypeXML();
                        if (!type.isDataType())
                            type = BuiltInTypeSystem.STRING;

                        try {
                        Object value = SimpleValueHelper.
                            parseBufferToType((String) c.getValue(), type,prop.getSchemaTypeCode(),
                                _nsHandler);
                        if (!prop.getTypeXML().isDataType())
                            value = compilePathOrId((String) value, c.getPrefixMap());
                        oldValues.add(value);
                        c.setResolvedProperty(prop);
                        c.setResolvedValue(value);
                        } catch (SimpleValueHelper.SimpleValueException e)
                        {
                            throw new SDOUnmarshalException(getMessageForSimpleValueException(
                                e, (String) c.getValue(), type, prop));
                        }
                    }
                    else
                    {
                        if (c.getKind() == Change.SIMPLE || c.getKind() == Change.DELETE &&
                            isSimpleNode((Node) c.getValue()))
                        {
                            if (c.getName() == null)
                            {
                                oldProperties.add(null /*ie is text*/);
                                oldValues.add(c.getValue());
                            }
                            else
                            {
                                boolean onDemand = false;
                                PropertyXML prop = findPropertyByName(parent, c.getUri(), c.getName(),
                                    c.isElement());
                                if (prop == null)
                                {
                                    onDemand = true;
                                    prop = findOnDemandPropertyByName(parent, c.getUri(), c.getName());
                                    if (prop == null)
                                        prop = onDemandProperties.get(c.getName());
                                    if (prop == null)
                                    {
                                        prop = getDataFactoryImpl().getRootProperty(null,
                                            c.getName(), BuiltInTypeSystem.STRING, true);
                                        onDemandProperties.put(c.getName(), prop);
                                    }
                                }
                                if (prop.getType().isDataType() || !prop.isContainment())
                                {
                                    TypeXML type = prop.getTypeXML();
                                    int schemaTypeCode = prop.getSchemaTypeCode();
                                    Node node = null;
                                    if (!type.isDataType())
                                        type = prop.getTypeXML();
                                    else if (c.getKind() == Change.DELETE)
                                    {
                                        node = (Node) c.getValue();
                                        type = getXsiTypeFromNode(node, type);
                                        schemaTypeCode = getXsiSchemaTypeCodeFromNode(node,
                                            schemaTypeCode);
                                    }

                                    try {
                                    Object value = SimpleValueHelper.
                                        parseBufferToType(c.getKind() == Change.SIMPLE ?
                                            (String) c.getValue() : node.getLeadText(), type,
                                            schemaTypeCode, _nsHandler);
                                    if (onDemand)
                                    {
                                        if (prop.getTypeXML().getTypeCode() == BuiltInTypeSystem.
                                            TYPECODE_STRING && !(value instanceof String))
                                        {
                                            // We need to upgrade the property type to Object
                                            PropertyXML newProp = getDataFactoryImpl().
                                                getRootProperty(null, c.getName(),
                                                    BuiltInTypeSystem.OBJECT, true);
                                            fixObjectSequenced(prop, newProp, parent, oldValues);
                                            fixOldPropertiesList(prop, newProp, oldProperties,
                                                oldValues, deletedObjects, parent);
                                            prop = newProp;
                                            if (onDemandProperties.containsKey(prop.getName()))
                                                onDemandProperties.put(prop.getName(), prop);
                                        }
                                    }
                                    if (!prop.getType().isDataType())
                                        value = compilePathOrId((String) value, c.getPrefixMap());
                                    oldProperties.add(prop);
                                    oldValues.add(value);
                                    c.setResolvedProperty(prop);
                                    c.setResolvedValue(value);
                                    } catch (SimpleValueHelper.SimpleValueException e)
                                    {
                                        throw new SDOUnmarshalException(getMessageForSimpleValueException(
                                            e, c.getKind() == Change.SIMPLE ? (String) c.getValue()
                                            : node.getLeadText(), type, prop));
                                    }
                                }
                                else
                                {
                                    DataObject oldValue = parseDataObjectFromNode(
                                        (Node) c.getValue(), parent, prop, rr, io, ip, iv);
                                    oldValues.add(oldValue);
                                    oldProperties.add(prop);
                                    if (!(oldValue.getType() == BuiltInTypeSystem.WRAPPERTYPE))
                                        deletedObjects.put(oldValue, parent);
                                    c.setResolvedProperty(prop);
                                    c.setResolvedValue(oldValue);
                                }
                            }
                        }
                        else if (c.getKind() == Change.DELETE)
                        {
                            boolean onDemand = false;
                            PropertyXML prop = findPropertyByName(parent, c.getUri(), c.getName(),
                                c.isElement());
                            if (prop == null)
                            {
                                onDemand = true;
                                prop = findOnDemandPropertyByName(parent, c.getUri(), c.getName());
                                if (prop == null)
                                    prop = onDemandProperties.get(c.getName());
                                if (prop == null)
                                {
                                    prop = getDataFactoryImpl().
                                        getRootProperty(null, c.getName(),
                                            BuiltInTypeSystem.STRING, true);
                                    onDemandProperties.put(c.getName(), prop);
                                }
                            }
                            Node n = (Node) c.getValue();
                            DataObject oldValue = parseDataObjectFromNode(n,
                                parent, prop, rr, io, ip, iv);
                            Object value;
                            if (onDemand)
                            {
                                PropertyXML newProp = getNewOpenContentProperty(oldValue, prop);
                                value = getOpenContentValue(oldValue, newProp);
                                if (newProp != prop)
                                {
                                    fixObjectSequenced(prop, newProp, parent, oldValues);
                                    fixOldPropertiesList(prop, newProp, oldProperties, oldValues,
                                        deletedObjects, parent);
                                    prop = newProp;
                                }
                            }
                            else
                                value = oldValue;
                            oldProperties.add(prop);
                            oldValues.add(value);
                            c.setResolvedProperty(prop);
                            c.setResolvedValue(value);
                            if (value == oldValue)
                                deletedObjects.put(oldValue, parent);
                        }
                        else if (c.getKind() == Change.REFERENCE)
                        {
                            PropertyXML prop = findPropertyByName(parent, c.getUri(), c.getName(),
                                c.isElement());
                            if (prop == null)
                            {
                                prop = findOnDemandPropertyByName(parent, c.getUri(), c.getName());
                                if (prop == null)
                                    prop = onDemandProperties.get(c.getName());
                                if (prop == null)
                                {
                                    prop = getDataFactoryImpl().getRootProperty(null,
                                        c.getName(), BuiltInTypeSystem.STRING, true);
                                    onDemandProperties.put(c.getName(), prop);
                                }
                            }
                            oldProperties.add(MARKER_PROPERTY);
                            Object o = resolveObjectReference(c.getRef(), rootXML, rr);
                            oldValues.add(o);
                            c.setResolvedValue(o);
                            c.setResolvedProperty(prop);
                        }
                    }
                }
                int i, j;
                // We'll simplyfiy things a little bit by adding a "dummy" change at the
                // beginning of the list
                ChangeSummaryImpl.Change lastChange = new ChangeSummaryImpl.Change(
                    null, null, false, 0);
                chgs[0] = lastChange;
                int currentIndex = 0;
                for (i = 0, j = 0; i < newSequence.size(); )
                {
                    PropertyXML prop = newSequence.getPropertyXML(i);
                    Object value = newSequence.getValue(i);
                    PropertyXML oldProp = j >= oldProperties.size() ? MARKER_PROPERTY :
                        oldProperties.get(j);
                    if ( prop!=null && !prop.isXMLElement())
                    {
                        while (oldProp != MARKER_PROPERTY && oldProp != null && !oldProp.isXMLElement())
                        {
                            if (prop == oldProp)
                            {
                                if (oldValues.get(j).equals(value))
                                    ; // We have the attribute in the new Sequence
                                      // and the value is the same
                                else
                                {
                                    ChangeSummaryImpl.Change newChange =
                                        new ChangeSummaryImpl.Change(prop, oldValues.get(j), true,
                                            i - currentIndex);
                                    ChangeSummaryImpl.Change newChange2 =
                                        new ChangeSummaryImpl.Change(prop, null, false, 0);
                                    boolean sameReference = false;
                                    if (!prop.getType().isDataType())
                                    {
                                        // Try to resolve the reference to see if it points to the
                                        // same object as the corresponding attribute in the
                                        // new sequence; if it does, then it's no change
                                        Object reference = resolveObjectReference(oldValues.get(j),
                                            rootXML, rr);
                                        if (reference == value)
                                            sameReference = true;
                                        else
                                        {
                                            ic.add(newChange);
                                            in.add(parent);
                                        }
                                    }
                                    if (!sameReference)
                                    {
                                        currentIndex = i;
                                        lastChange.next = newChange;
                                        newChange.next = newChange2;
                                        lastChange = newChange2;
                                    }
                                }
                                i++; j++;
                                break;
                            }
                            else
                            {
                                // The old attribute doesn't exist anymore at the present
                                // position; log a delete
                                ChangeSummaryImpl.Change newChange = new ChangeSummaryImpl.Change(
                                    oldProp, oldValues.get(j), true, i - currentIndex);
                                currentIndex = i;
                                lastChange.next = newChange;
                                lastChange = newChange;
                                if (!prop.getType().isDataType())
                                {
                                    ic.add(newChange);
                                    in.add(parent);
                                }
                            }
                            oldProp = ++j >= oldProperties.size() ? MARKER_PROPERTY :
                                oldProperties.get(j);
                        }
                        if (prop != oldProp)
                        {
                            // We weren't able to find this attribute in the
                            // list of old values, it means it's new
                            ChangeSummaryImpl.Change newChange = new ChangeSummaryImpl.Change(
                                prop, value, false, i - currentIndex);
                            currentIndex = i++;
                            lastChange.next = newChange;
                            lastChange = newChange;
                            if (!prop.getType().isDataType())
                            {
                                ic.add(newChange);
                                in.add(parent);
                            }
                        }
                    }
                    else if ( prop == null /*ie is text*/)
                    {
                        if ( oldProp == null /*ie is text*/ &&
                            j <= oldValues.size() && value.equals(oldValues.get(j)))
                        {
                            i++; j++;
                            continue;
                        }
                        while (oldProp != MARKER_PROPERTY && oldProp != null /*ie not text*/ &&
                                oldProp.isXMLElement())
                        {
                            // If this position in the old sequence was an element, then
                            // we have a delete
                            ChangeSummaryImpl.Change newChange = new ChangeSummaryImpl.Change(
                                oldProp, oldValues.get(j), true, i - currentIndex);
                            currentIndex = i;
                            lastChange.next = newChange;
                            lastChange = newChange;
                            oldProp = ++j >= oldProperties.size() ? MARKER_PROPERTY :
                                oldProperties.get(j);
                        }
                        int oldj = j;
                        boolean foundMatch = false;
                        while (oldProp != MARKER_PROPERTY)
                        {
                            if (oldProp == null /*ie is text*/ &&
                                value.equals(oldValues.get(j)))
                            {
                                foundMatch = true;
                                break;
                            }
                            oldProp = ++j >= oldProperties.size() ? MARKER_PROPERTY :
                                oldProperties.get(j);
                        }
                        j = oldj;
                        oldProp = oldProperties.get(j);
                        if (foundMatch)
                        {
                            // We assume that everything before the text that matches
                            // is a deletion
                            while (!(oldProp == null /*ie is text*/ &&
                                value.equals(oldValues.get(j))))
                            {
                                ChangeSummaryImpl.Change newChange = new ChangeSummaryImpl.Change(
                                    oldProp, oldValues.get(j), true, i - currentIndex);
                                currentIndex = i;
                                lastChange.next = newChange;
                                lastChange = newChange;
                                oldProp = ++j >= oldProperties.size() ? MARKER_PROPERTY :
                                    oldProperties.get(j);
                            }
                            // We have reached the text property that matches
                            i++; j++;
                        }
                        else
                        {
                            // There is no matching text
                            // Just find the first text property and mark it as
                            // modified
                            while (oldProp != MARKER_PROPERTY &&
                                oldProp != null /*ie is text*/)
                            {
                                ChangeSummaryImpl.Change newChange = new ChangeSummaryImpl.Change(
                                    oldProp, oldValues.get(j), true, i - currentIndex);
                                currentIndex = i;
                                lastChange.next = newChange;
                                lastChange = newChange;
                                oldProp = ++j >= oldProperties.size() ? MARKER_PROPERTY :
                                    oldProperties.get(j);
                            }
                            if (oldProp != MARKER_PROPERTY)
                            {
                                // We have a text change here
                                ChangeSummaryImpl.Change newChange = new ChangeSummaryImpl.Change(
                                    oldProp, oldValues.get(j), true, i - currentIndex);
                                ChangeSummaryImpl.Change newChange2 = new ChangeSummaryImpl.Change(
                                    oldProp, null, false, 0);
                                currentIndex = i;
                                lastChange.next = newChange;
                                newChange.next = newChange2;
                                lastChange = newChange2;
                                j++;
                            }
                            else
                            {
                                // We have new text
                                ChangeSummaryImpl.Change newChange =  new ChangeSummaryImpl.Change(
                                    prop, value, false, i - currentIndex);
                                currentIndex = i;
                                lastChange.next = newChange;
                                lastChange = newChange;
                            }
                            i++;
                        }
                    }
                    else
                    {
                        if (insertedObjects.contains(value))
                        {
                            ChangeSummaryImpl.Change newChange = new ChangeSummaryImpl.Change(
                                prop, value, false, i - currentIndex);
                            currentIndex = i;
                            lastChange.next = newChange;
                            lastChange = newChange;
                            i++;
                        }
                        else
                        {
                            while (oldProp != MARKER_PROPERTY)
                            {
                                ChangeSummaryImpl.Change newChange = new ChangeSummaryImpl.Change(
                                    oldProp, oldValues.get(j), true, i - currentIndex);
                                currentIndex = i;
                                lastChange.next = newChange;
                                lastChange = newChange;
                                oldProp = ++j >= oldProperties.size() ? MARKER_PROPERTY :
                                    oldProperties.get(j);
                            }
                            while (!(j < oldValues.size() && value == oldValues.get(j)))
                            {
                                // We haven't been able to find a match in the old sequence,
                                // therefore it must be insertion of a datatype property
                                ChangeSummaryImpl.Change newChange = new ChangeSummaryImpl.Change(
                                    prop, value, false, i - currentIndex);
                                currentIndex = i;
                                lastChange.next = newChange;
                                lastChange = newChange;
                                i++;
                                if ( prop!=null && !prop.getType().isDataType())
                                {
                                    ic.add(newChange);
                                    ip.add(prop);
                                }
                                if (i >= newSequence.size())
                                    break;
                                prop = newSequence.getPropertyXML(i);
                                value = newSequence.getValue(i);
                            }
                            if (j < oldValues.size() && value == oldValues.get(j))
                            {
                                // We have found a match between the new and old sequences
                                i++; j++;
                            }
                        }
                    }
                }
                // Complete with some deletions at the very end
                while (j < oldProperties.size())
                {
                    PropertyXML oldProp = oldProperties.get(j);
                    if (oldProp == MARKER_PROPERTY)
                    {
                        // Couldn't be
                        throw new IllegalStateException("Did not find expected value " +
                            "in the new sequence");
                    }
                    ChangeSummaryImpl.Change newChange = new ChangeSummaryImpl.Change(
                        oldProp, oldValues.get(j), true, i - currentIndex);
                    currentIndex = i;
                    lastChange.next = newChange;
                    lastChange = newChange;
                    j++;
                }
                chgs[0] = chgs[0].next;
            }
        }
        // Resolve all the references found in deleted objects
        // Delegate this job to an unmarshaller
        PlainUnmarshaller u = new PlainUnmarshaller(savedOptions, _sdoContext);
        u.setIdrefObjects(io);
        u.setIdrefProperties(ip);
        u.setIdrefValues(iv);
        u.resolveIdrefs();
        // Resolve all references from Changes
        for (int i = 0; i < ic.size(); i++)
        {
            ChangeSummaryImpl.Change change = ic.get(i);
            if (change.getValue() instanceof List)
            {
                List<Object> listValue = (List<Object>) change.getValue();
                List<DataObject> result = new ArrayList<DataObject>(listValue.size());
                for (Object ref : listValue)
                {
                    DataObject referred;
                    if (ref instanceof XPath)
                        referred = rr.resolvePath((XPath) ref, in.get(i));
                    else
                        referred = rr.resolveId((String) ref);
                    result.add(referred);
                    cs.addReferredObject(referred, change);
                }
                change.setValue(result);
            }
            else
            {
                Object ref = change.getValue();
                DataObject referred;
                if (ref instanceof XPath)
                    referred = rr.resolvePath((XPath) ref, in.get(i));
                else
                    referred = rr.resolveId((String) ref);
                change.setValue(referred);
                cs.addReferredObject(referred, change);
            }
        }
        // Go through the list of deleted objects to see if there are any that are not included
        // in the deleted list yet
        for (Object p : deletedPaths)
        {
            DataObject o = (DataObject) resolveObjectReference(p, rootXML, rr);
            if (!deletedObjects.containsKey(o))
            {
                deletedObjects.put(o, o.getContainer());
            }
        }
        // Go through the list of inserted objects and check if they are properly recorded
        // in the list of changes for their parents; if they were originally unset, they will
        // need to be set now
        for (Object p : insertedPaths)
        {
            DataObjectXML o = (DataObjectXML) resolveObjectReference(p, rootXML, rr);
            DataObjectXML parent = o.getContainerXML();
            if (parent == null)
                continue; // Don't know what that means: error?
            ChangeSummaryImpl.Change[] chgs = modifiedObjects.get(parent);
            PropertyXML prop = o.getContainmentPropertyXML();
            if (chgs == null)
            {
                // Even though we are not generating ChangeSummaries like that, we are
                // prepared to handle them
                chgs = new ChangeSummaryImpl.Change[ChangeSummaryImpl.computeHashMapSize(parent)];
                modifiedObjects.put(parent, chgs);
            }
            if (parent.getSequence() == null)
            {
                int hashCode = ChangeSummaryImpl.hashCode(prop, chgs.length);
                ChangeSummaryImpl.Change change = chgs[hashCode];
                boolean alreadyInserted = false;
                while (change != null)
                {
                    if (change.getProperty() == prop)
                    {
                        while (change != null)
                        {
                            if (change.getValue() == o)
                            {
                                alreadyInserted = true;
                                break;
                            }
                            change = change.next2;
                        }
                        break;
                    }
                    change = change.next;
                }
                if (alreadyInserted)
                    continue;
                int arrayIndex = getArrayIndex(o, parent, prop, false);
                cs.logInsertionHelper(false, prop, arrayIndex, o, chgs);
            }
            else
            {
                ChangeSummaryImpl.Change change = chgs[0];
                boolean alreadyInserted = false;
                while (change != null)
                {
                    if (change.getValue() == o)
                    {
                        alreadyInserted = true;
                        break;
                    }
                    change = change.next;
                }
                if (alreadyInserted)
                    continue;
                int arrayIndex = getArrayIndex(o, parent, prop, true);
                // If the value object is not found among the children of "parent"
                // then it is a problem: don't log the insertion
                if (arrayIndex > -2)
                    cs.logInsertionHelper(true, prop, arrayIndex, o, chgs);
            }
        }
        cs.deleteChangeSummaryXMLDelegator();
        cs.setLogging(logging);
    }

    /**
     * Looks for the <code>value</code> DataObject among the value of the property <code>prop</code> in
     * the <code>parent</code> DataObject
     */
    private static int getArrayIndex(DataObjectXML value, DataObjectXML parent, PropertyXML prop, boolean isSequence)
    {
        if (isSequence)
        {
            SequenceXML s = parent.getSequenceXML();
            for (int i = 0; i < s.size(); i++)
                if (value == s.getValue(i))
                    return i;
            return -2; // Error
        }
        else if (!prop.isMany())
        {
            return -1;
        }
        else
        {
            ListXMLIterator it = parent.getListXMLIterator(prop);
            int i = 0;
            while (it.next())
            {
                if (it.getValue() == value)
                    return i;
                i++;
            }
            return -2; // Error
        }
    }

    DataObject getRootObject()
    {
        return null;
    }

    private boolean isSimpleNode(Node n)
    {
        if (n.getAttributes() != null)
            for (Att a : n.getAttributes())
                if (a.getLocal() != null)
                    return false;
        return n.getFirstChild() == null;
    }

    private TypeXML getXsiTypeFromNode(Node n, TypeXML propType)
    {
        if (n.getXsiTypeName() == null)
            return propType;
        TypeXML xsiType = _sdoContext.getTypeSystem().getTypeBySchemaTypeName(n.getXsiTypeUri(),
            n.getXsiTypeName());
        if (xsiType == null)
            throw new SDOUnmarshalException(SDOError.messageForCodeAndLocation("unmarshal.xsitype.notfound",
                _locator, n.getXsiTypeName(), n.getXsiTypeUri()));
//        if (!propType.isAssignableFrom(xsiType) &&
//            propType != BuiltInTypeSystem.DATAOBJECT)
//            throw new SDOUnmarshalException(SDOError.messageForCodeAndLocation("unmarshal.xsitype.notassignable",
//                xsiType, propType)));
        return xsiType;
    }

    private int getXsiSchemaTypeCodeFromNode(Node n, int def)
    {
        int typeCode = def;
        if (n.getXsiTypeName() != null)
        {
            // HACKHACK(radup)
            org.apache.xmlbeans.SchemaTypeLoader stl = _sdoContext.getTypeSystem().getSchemaTypeLoader();
            if (stl == null)
                stl = org.apache.xmlbeans.XmlBeans.getContextTypeLoader();
            SchemaType xsiType = stl.findType(new QName(n.getXsiTypeUri(), n.getXsiTypeName()));
            if (xsiType != null)
                typeCode = Common.getBuiltinTypeCode(xsiType);
        }
        return typeCode;
    }

    private PropertyXML findPropertyByName(DataObjectXML parent, String uri, String name,
        boolean element)
    {
        TypeXML type = parent.getTypeXML();
        PropertyXML prop = type.getPropertyXMLByXmlName(uri, name, element);
        if (prop != null)
            //Static property
            return prop;
        prop = (PropertyXML)
            _sdoContext.getXSDHelper().getGlobalProperty(uri, name, element);
        return prop;
    }

    private PropertyXML findOnDemandPropertyByName(DataObjectXML parent, String uri, String name)
    {
        List list = parent.getInstanceProperties();
        PropertyXML prop;
        for (int i = parent.getTypeXML().getDeclaredProperties().size(); i < list.size(); i++)
        {
            prop = (PropertyXML) list.get(i);
            if ((uri == null ? prop.getXMLNamespaceURI() == null :
                uri.equals(prop.getXMLNamespaceURI())) &&
                prop.getXMLName().equals(name) &&
                prop.getContainingTypeXML() != parent.getTypeXML())
                return prop;
        }
        return null;
    }

    private PropertyXML findAttributePropertyByName(SequenceXML s, String uri, String name)
    {
        for (int i = 0; i < s.size(); i++)
        {
            PropertyXML prop = s.getPropertyXML(i);
            if ( prop!=null /*ie text*/ && !prop.isXMLElement() &&
                prop.getXMLName().equals(name) &&
                (uri.length() == 0 ? prop.getXMLNamespaceURI() == null :
                    uri.equals(prop.getXMLNamespaceURI())))
                return prop;
        }
        return
            (PropertyXML) _sdoContext.getXSDHelper().getGlobalProperty(uri, name, true);
    }

    private QName resolveQName(String name, NamespaceContext nsctx)
    {
        int colonIndex = name.indexOf(':');
        String uri, local, prefix;
        if (colonIndex < 0)
        {
            prefix = Common.EMPTY_STRING;
            local = name;
        }
        else
        {
            prefix = name.substring(0, colonIndex);
            local = name.substring(colonIndex + 1);
        }
        uri = nsctx.getNamespaceURI(prefix);
        return new QName(uri, local, prefix);
    }

    private String getMessageForSimpleValueException(SimpleValueHelper.SimpleValueException e,
        String value, TypeXML type, PropertyXML prop)
    {
        switch (e.cause())
        {
            case SimpleValueHelper.UNMARSHAL_SIMPLE_NOCONSTRUCTOR:
                return SDOError.messageForCodeAndLocation("unmarshal.simple.noconstructor",
                    _locator, e.getParam());
            case SimpleValueHelper.UNMARSHAL_SIMPLE_CONSTRUCTOREXCEPTION:
                return SDOError.messageForCodeAndLocation("unmarshal.simple.constructorexception",
                    _locator, e.getParam(), e.getCause().getMessage());
            case SimpleValueHelper.UNMARSHAL_SIMPLE_CONVERSIONFAILED:
                return SDOError.messageForCodeAndLocation("unmarshal.simple.conversionfailed",
                    _locator, value, type, prop.getXMLName(), prop.getXMLNamespaceURI(),
                    e.getCause().getMessage());
            case SimpleValueHelper.UNMARSHAL_SIMPLE_UNKOWNTYPE:
                return SDOError.messageForCodeAndLocation("unmarshal.simple.unknowntype",
                    _locator, type, value, prop.getXMLName(), prop.getXMLNamespaceURI());
            default:
                throw new IllegalStateException();
        }
    }

    // ===============================================
    // Methods that deal with on-demand properties
    // ===============================================
    private Object getOpenContentValue(DataObject dObject, PropertyXML prop)
    {
        TypeXML type = prop.getTypeXML();
        switch (type.getTypeCode())
        {
        case BuiltInTypeSystem.TYPECODE_OBJECT:
            if (dObject.getSequence() != null)
                return dObject.getSequence().getValue(0);
            else
                return dObject.get(0);
        case BuiltInTypeSystem.TYPECODE_STRING:
            if (dObject.getSequence() != null)
                return dObject.getSequence().getValue(0);
            else
                return null;
        default:
            return dObject;
        }
    }

    private PropertyXML getNewOpenContentProperty(DataObject dataObject, PropertyXML prop)
    {
        TypeXML type = prop.getTypeXML();
        if (type.getTypeCode() == BuiltInTypeSystem.TYPECODE_DATAOBJECT)
            return prop;
        else if (dataObject.getSequence() != null && dataObject.getSequence().size() == 1 &&
                dataObject.getSequence().getProperty(0) == null /*ie is text*/)
            return prop;
        else
            return getDataFactoryImpl().getRootProperty(prop.getXMLNamespaceURI(),
                prop.getXMLName(), BuiltInTypeSystem.DATAOBJECT, true);
    }

    private DataObject convertStringToDataObject(Object value)
    {
        DataObject dObject = getDataFactoryImpl().create(BuiltInTypeSystem.WRAPPERTYPE);
        dObject.set(getDataFactoryImpl().getValueProperty(BuiltInTypeSystem.OBJECT), value);
        return dObject;
    }

    private DataObject convertObjectToDataObject(Object value)
    {
        DataObject dObject = getDataFactoryImpl().create(BuiltInTypeSystem.WRAPPERTYPE);
        Class valueClass = Common.unwrapClass(value.getClass());
        TypeXML valueSdoType = _sdoContext.getBindingSystem().getType(valueClass);
        dObject.set(getDataFactoryImpl().getValueProperty(valueSdoType), value);
        return dObject;
    }

    private static final int CONVERSION_STRING_DATAOBJECT = 1;
    private static final int CONVERSION_OBJECT_DATAOBJECT = 2;
    private static final int CONVERSION_NOTNEEDED = 3;

    private int getConversionMethod(PropertyXML prop, PropertyXML newProp)
    {
        if (newProp.getTypeXML().getTypeCode() == BuiltInTypeSystem.TYPECODE_DATAOBJECT)
            if (prop.getTypeXML().getTypeCode() == BuiltInTypeSystem.TYPECODE_STRING)
                return CONVERSION_STRING_DATAOBJECT;
            else if (prop.getTypeXML().getTypeCode() == BuiltInTypeSystem.TYPECODE_OBJECT)
                return CONVERSION_OBJECT_DATAOBJECT;
            else
                return CONVERSION_NOTNEEDED;
        else
            return CONVERSION_NOTNEEDED;
    }

    /**
     * Takes a list of changes and replaces one property with the other, making sure that the
     * values are also converted accordingly
     * @param deletedObjects
     */
    private void fixChangeSummary(PropertyXML prop, PropertyXML newProp,
        ChangeSummaryImpl.Change chg, Map<DataObject, DataObject> deletedObjects,
        DataObject parent)
    {
        final int conversion = getConversionMethod(prop, newProp);
        // We are going to assume that indeed all the changes have the property prop
        while (chg != null)
        {
            chg.setProperty(newProp);
            DataObject value;
            switch (conversion)
            {
            case CONVERSION_STRING_DATAOBJECT:
                value = convertStringToDataObject(chg.getValue());
                chg.setValue(value);
                deletedObjects.put(value, parent);
                break;
            case CONVERSION_OBJECT_DATAOBJECT:
                if (chg.getValue() instanceof String)
                    value = convertStringToDataObject(chg.getValue());
                else
                    value = convertObjectToDataObject(chg.getValue());
                chg.setValue(value);
                deletedObjects.put(value, parent);
                break;
            }
        }
    }

    /**
     * Takes a DataObject and replaces occurences of one property with another, converting values
     * of the former property
     */
    private void fixObjectNonSequenced(PropertyXML prop, PropertyXML newProp, DataObjectXML parent)
    {
        final int conversion = getConversionMethod(prop, newProp);

        List values = new ArrayList(parent.getList(prop));
        parent.unset(prop);
        for (int i = 0; i < values.size(); i++)
            switch(conversion)
            {
            case CONVERSION_STRING_DATAOBJECT:
                values.set(i, convertStringToDataObject(values.get(i)));
                break;
            case CONVERSION_OBJECT_DATAOBJECT:
                if (values.get(i) instanceof String)
                    values.set(i, convertStringToDataObject(values.get(i)));
                else
                    values.set(i, convertObjectToDataObject(values.get(i)));
            }
        parent.set(newProp, values);
    }

    private void fixObjectSequenced(PropertyXML prop, PropertyXML newProp, DataObjectXML parent,
        ArrayList<Object> oldValues)
    {
        final int conversion = getConversionMethod(prop, newProp);

        SequenceXML seq = parent.getSequenceXML();
        for (int i = 0; i < seq.size(); i++)
            if (seq.getPropertyXML(i) == prop)
            {
                Object value = seq.getValue(i);
                Object newValue = value;
                switch (conversion)
                {
                case CONVERSION_STRING_DATAOBJECT:
                    newValue = convertStringToDataObject(value);
                    break;
                case CONVERSION_OBJECT_DATAOBJECT:
                    if (value instanceof String)
                        newValue = convertStringToDataObject(value);
                    else
                        newValue = convertObjectToDataObject(value);
                    break;
                }
                seq.remove(i);
                seq.add(i, newProp, newValue); // prefix as well somehow
                // TODO(this is N-square, we now need a reverse map so we can
                // replace those values which have to be == with the ones in the doc
                for (int j = 0; j < oldValues.size(); j++)
                    if (oldValues.get(j) == value)
                        oldValues.set(j, newValue);
            }
    }

    /**
     * Replaces <code>prop</code> with <code>newProp</code> in the given list of properties
     */
    private void fixOldPropertiesList(PropertyXML prop, PropertyXML newProp,
        ArrayList<PropertyXML> oldProperties, ArrayList<Object> oldValues,
        Map<DataObject, DataObject> deletedObjects, DataObject parent)
    {
        final int conversion = getConversionMethod(prop, newProp);
        for (int i = 0; i < oldProperties.size(); i++)
            if (oldProperties.get(i) == prop)
            {
                Object value = oldValues.get(i);
                switch (conversion)
                {
                case CONVERSION_STRING_DATAOBJECT:
                    value = convertStringToDataObject(value);
                    deletedObjects.put((DataObject) value, parent);
                    break;
                case CONVERSION_OBJECT_DATAOBJECT:
                    if (value instanceof String)
                        value = convertStringToDataObject(value);
                    else
                        value = convertObjectToDataObject(value);
                    deletedObjects.put((DataObject) value, parent);
                }
                oldProperties.set(i, newProp);
                oldValues.set(i, value);
            }
    }

    // ================================================
    // ChangeSummaryXML implementation
    // ================================================
    public Iterator getChangedObjectsIterator()
    {
        return changedObjects.keySet().iterator();
    }

    // ================================================
    // Reference resolver helpers
    // ================================================
    Object compilePathOrId(String s, NamespaceContext nsc)
    {
        if (s.startsWith(Names.FRAGMENT))
        {
            try
            {
                return XPath.compile(s.substring(Names.FRAGMENT.length()), nsc);
            }
            catch (XPath.XPathCompileException xpce)
            {
                throw new SDOUnmarshalException(SDOError.messageForCodeAndLocation("unmarshal.xpath.compile",
                    _locator, s, xpce.getMessage()));
            }
        }
        else
            return s;
    }

    Object compilePathOrId(String s, Map<String, String> prefixMap)
    {
        if (s.startsWith(Names.FRAGMENT))
        {
            if (prefixMap == null)
                throw new IllegalStateException();
            try
            {
                return XPath.compile(s.substring(Names.FRAGMENT.length()), prefixMap);
            }
            catch (XPath.XPathCompileException xpce)
            {
                throw new SDOUnmarshalException(SDOError.messageForCodeAndLocation("unmarshal.xpath.compile",
                    _locator, s, xpce.getMessage()));
            }
        }
        else
            return s;
    }

    private DataObjectXML resolveDataObjectReference(ObjectReference or, DataObjectXML root,
        ReferenceResolver r)
    {
        Object o = resolveObjectReference(or.getRef(), root, r);
        if (!(o instanceof DataObjectXML))
            throw new SDOUnmarshalException(SDOError.messageForCodeAndLocation("unmarshal.xpath.notdataobject",
                _locator, or.getRef(), o.getClass().getName()));
        return (DataObjectXML) o;
    }

    private Object resolveObjectReference(Object pathOrId, DataObjectXML root, ReferenceResolver r)
    {
        Object result;
        if (pathOrId instanceof XPath)
        {
//            result = r.resolvePath((XPath) pathOrId, root);
            // We don't call the ReferenceResolver because for sequences, we
            // may get a non-DataObject back
            XPath.Selection selection = XPath.execute((XPath) pathOrId, root);
            if (selection.hasNext())
            {
                result = selection.next();
            }
            else
                result = null;
        }
        else
        {
            result = r.resolveId((String) pathOrId);
        }
        if (result == null)
            throw new SDOUnmarshalException(SDOError.messageForCodeAndLocation((pathOrId instanceof XPath) ?
                "unmarshal.xpath.result" : "unmarshal.idref.result", _locator, pathOrId));
        return result;
    }

    private DataObject parseDataObjectFromSimple(String uri, String name,
        String text, boolean nilled, DataObjectXML parent, PropertyXML prop,
        List<DataObjectXML> io, List<PropertyXML> ip, List<Object> iv)
    {
        PlainUnmarshaller u = new PlainUnmarshaller(savedOptions, _sdoContext);
        u.setIdrefObjects(io);
        u.setIdrefProperties(ip);
        u.setIdrefValues(iv);
        Object result;
        // TODO(radup) Check whether the newly created deleted object has to have the same
        // DataGraph as the parent
        DataObject simul = getDataFactoryImpl().create(parent.getTypeXML());
        u.setRootObject(simul);
        if (prop.isXMLElement())
        {
            u.startElement(uri, name, null, null, null);
            if (nilled)
                u.sattr(ATTR_XSI, Names.XSI_NIL, Names.TRUE);
            if (text != null)
                u.text(text);
            u.endElement();
        }
        else
            u.attr(uri, name, null, text);
        if (!simul.isSet(prop))
        {
            throw new IllegalStateException("Could not retrieve property \"" +
                prop.getName() + "\" after parsing an element \"" + name +
                "@" + uri + "\"");
        }
        result = simul.get(prop.getName());
        if (result instanceof List)
        {
            List list = (List) result;
            Object value = list.size() == 0 ? Common.EMPTY_STRING : list.get(0);
            if (value instanceof DataObject)
                return (DataObject) value;
            else if (value instanceof String)
            {
                return convertStringToDataObject(value);
            }
            else if (value == null)
                return null;
            else
                throw new SDOUnmarshalException(SDOError.messageForCodeAndLocation("unmarshal.changesummary.conversion1",
                    _locator, value, name, uri));
        }
        else if (result instanceof DataObject)
            return (DataObject) result;
        else if (result == null)
            return null;
        else
            throw new SDOUnmarshalException(SDOError.messageForCodeAndLocation("unmarshal.changesummary.conversion2",
                _locator, name, uri));
    }

    private DataObject parseDataObjectFromNode(Node top, DataObjectXML parent,
        PropertyXML prop, ReferenceResolver rr,
        List<DataObjectXML> io, List<PropertyXML> ip, List<Object> iv)
    {
        // Use the normal unmarshaller and feed events into it
        PlainUnmarshaller u = new PlainUnmarshaller(savedOptions, _sdoContext);
        Object result;
        // TODO(radup) Check whether the newly created deleted object has to have the same
        // DataGraph as the parent
        DataObject simul = getDataFactoryImpl().create(parent.getTypeXML());
        NamespaceSupportDelegator nssupport = new NamespaceSupportDelegator();
        u.setRootObject(simul);
        u.setReferenceResolver(rr);
        u.setIdrefObjects(io);
        u.setIdrefProperties(ip);
        u.setIdrefValues(iv);
        u.setNamespaceHandler(nssupport);
        Node n = top;
        while (n != null)
        {
            u.startElement(n.getUri(), n.getName(), n.getPrefix(),
                n.getXsiTypeUri(), n.getXsiTypeName());
            nssupport.pushNamespaceContext();

            List<Att> atts = n.getAttributes();
            if (atts != null)
                for (Att att : atts)
                    if (att.getLocal() == null)
                    {
                        u.xmlns(att.getPrefix(), att.getUri());
                        nssupport.declarePrefix(att.getPrefix(), att.getUri());
                    }
                    else
                        u.attr(att.getUri(), att.getLocal(), att.getPrefix(), att.getValue());
            if (n.getLeadText() != null)
                u.text(n.getLeadText());
            if (n.getFirstChild() != null)
                n = n.getFirstChild();
            else
            {
                // Finish the n node
                while (n != top)
                {
                    u.endElement();
                    nssupport.popNamespaceContext();
                    if (n.getNextText() != null)
                        u.text(n.getNextText());
                    if (n.getNextSibling() != null)
                    {
                        n = n.getNextSibling();
                        break;
                    }
                    n = n.getParent();
                }
                if (n == top)
                {
                    u.endElement();
                    nssupport.popNamespaceContext();
                    break;
                }
            }
        }
        result = simul.get(prop.getName());
        if (result == null)
        {
            throw new IllegalStateException("Could not retrieve property \"" +
                prop.getName() + "\" after parsing an element \"" + top.getName() +
                "@" + top.getUri() + "\"");
        }
        if (result instanceof List)
        {
            List list = (List) result;
            Object value = list.size() == 0 ? Common.EMPTY_STRING : list.get(0);
            if (value instanceof DataObject)
                return (DataObject) value;
            else if (value instanceof String)
            {
                return convertStringToDataObject(value);
            }
            else
            {
                return convertObjectToDataObject(value);
            }
        }
        else if (result instanceof DataObject)
            return (DataObject) result;
        else
            throw new SDOUnmarshalException(SDOError.messageForCodeAndLocation("unmarshal.changesummary.conversion1",
                _locator, top.getName(), top.getUri()));
    }
}
