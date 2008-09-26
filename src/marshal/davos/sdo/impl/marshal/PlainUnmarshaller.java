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

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.xmlbeans.SchemaType;

import davos.sdo.impl.common.Common;
import davos.sdo.impl.common.Names;
import davos.sdo.impl.type.SimpleValueHelper;
import davos.sdo.impl.data.DataFactoryImpl;
import davos.sdo.impl.data.DataObjectGeneral;
import davos.sdo.impl.data.Store;
import davos.sdo.impl.data.ChangeSummaryImpl;
import davos.sdo.impl.data.DataObjectImpl;
import davos.sdo.impl.type.BuiltInTypeSystem;
import davos.sdo.impl.type.PropertyImpl;
import davos.sdo.impl.xpath.XPath;
import davos.sdo.impl.xpath.XPath.XPathCompileException;
import davos.sdo.Options;
import davos.sdo.SDOContext;
import davos.sdo.SDOError;
import davos.sdo.SDOUnmarshalException;
import davos.sdo.SequenceXML;
import davos.sdo.TypeXML;
import davos.sdo.DataObjectXML;
import davos.sdo.PropertyXML;
import davos.sdo.PropertyMapEntry;
import davos.sdo.XsiTypeNotFoundException;

import javax.sdo.DataObject;
import javax.sdo.ChangeSummary;

public class PlainUnmarshaller extends Unmarshaller implements ReferenceResolver
{
    private XMLDocumentImpl _root;
    private DataObjectXML _current;
    private PropertyXML _currentProp;
    private TypeXML _currentChildType;
    private int _currentSchemaTypeCode; // for cases where a value has xsi:type="xs:QName"
    private StringBuilder _currentBuffer = new StringBuilder(10);
    private SimpleDataObjectXML _placeHolder = SimpleDataObjectXML.newInstance();
    private String _currentPrefix;
    private PropertyXML _currentXmlProp;
    private boolean _nilled;
    private List<DataObjectXML> _idrefObjects = new ArrayList<DataObjectXML>(2);
    private List<PropertyXML> _idrefProperties = new ArrayList<PropertyXML>(2);
    private List<Object> _idrefValues = new ArrayList<Object>(2);
    private ReferenceResolver _referenceResolver;
    private Map<String, DataObject> _idMap = new HashMap<String, DataObject>();
    private PlainChangeSummaryUnmarshaller _pendingChange = null;
    private DataObjectXML _changeOwner = null;
    private boolean dontthrow;

    /**
     * @param root
     * @param options
     */
    PlainUnmarshaller(XMLDocumentImpl root, Object options, SDOContext sdoctx)
    {
        this(options, sdoctx);
        _root = root;
    }

    PlainUnmarshaller(Object options, SDOContext sdoctx)
    {
        _sdoContext = sdoctx;
        Map map = null;
        if (options instanceof Map)
            map = (Map) options;
        else if (options instanceof Options)
            map = ((Options) options).getMap();
        if (map != null)
        {
            if (map.containsKey(Options.LOAD_DONT_THROW_EXCEPTIONS))
                dontthrow = true;
        }
    }

    private DataFactoryImpl getDataFactoryImpl()
    {
        return (DataFactoryImpl) _sdoContext.getDataFactory();
    }

    public void startElement(String uri, String name, String prefix, String xsiTypeUri, String xsiTypeName)
    {
        if (_current == null)
        {
            // Process the document root
            PropertyXML rootProp = getPropertyByName(uri, name);
            TypeXML rootType = rootProp == null ? null : rootProp.getTypeXML();

            TypeXML xsiType = null;
            if (xsiTypeName != null)
            {
                xsiType = getTypeByName(xsiTypeUri, xsiTypeName);
                if (xsiType != null)
                {
                    if (rootType != null && rootType != BuiltInTypeSystem.DATAOBJECT &&
                        !rootType.isAssignableFrom(xsiType))
                        throw new SDOUnmarshalException(SDOError.messageForCodeAndLocation("unmarshal.xsitype.notassignable",
                            _locator, xsiType, rootType));
                    if (xsiType.isDataType())
                    {
                        _currentSchemaTypeCode = getSchemaTypeCode(xsiTypeUri, xsiTypeName, xsiType);
                    }
                    else
                    {
                        rootType = xsiType;
                        xsiType = null;
                    }
                }
                else
                    throwXsiTypeNotFoundException(xsiTypeUri, xsiTypeName);
            }

            if (rootType == null)
            {
                // Dynamic XML
                rootType = BuiltInTypeSystem.DATAOBJECT;
            }

            // At this point, xsiType != null means that xsiType.isDataType() and
            // rootType is DATAOBJECT; create the wrapper type instead
            DataObject root;
            if (xsiType != null)
            {
                root = getDataFactoryImpl().create(BuiltInTypeSystem.WRAPPERTYPE);
                _currentProp = getDataFactoryImpl().getValueProperty(xsiType);
                _currentChildType = xsiType;
            }
            else if (rootType.isDataType())
            {
                root = getDataFactoryImpl().create(BuiltInTypeSystem.WRAPPERTYPE);
                _currentProp = getDataFactoryImpl().getValueProperty(BuiltInTypeSystem.OBJECT);
                _currentChildType = rootType;
            }
            else
            {
                root = getDataFactoryImpl(). create(rootType == BuiltInTypeSystem.DATAOBJECT ?
                    BuiltInTypeSystem.BEADATAOBJECT : rootType);
                if (rootType.isSimpleContent())
                {
                    _currentSchemaTypeCode = SchemaType.BTC_ANY_SIMPLE;
                    PropertyXML simpleContentProperty = rootType.getPropertyXML(
                        Names.SIMPLE_CONTENT_PROP_NAME);
                    if (simpleContentProperty != null &&
                        simpleContentProperty.getTypeXML().isDataType())
                    {
                        _currentProp = simpleContentProperty;
                        _currentXmlProp = null;
                        _currentChildType = simpleContentProperty.getTypeXML();
                        _currentPrefix = prefix;
                        _currentSchemaTypeCode = simpleContentProperty.getSchemaTypeCode();
                    }
                }
            }
            if (_root != null)
            {
                _root.setDataObject(root);
                _root.setRootElementURI(uri);
                _root.setRootElementName(name);
            }

            _current = (DataObjectXML) root;
            // Setting the name of the root element because it is useful to have
            if (rootProp == null)
                rootProp = getDataFactoryImpl().getRootProperty(uri, name, _current.getTypeXML(), true);
            ((davos.sdo.impl.data.DataObjectImpl) _current).setContainmentProperty(rootProp);
        }
        else
        {
            // Inner node
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
            TypeXML childType = null;
            /* We support the following cases:
               - property is defined in parentType - local property
               - property is a global property from the Schema - global property
               - property is a local open-content property
               */
            /*
            This is split into two steps
            1. Figure out the property and create the new child value
            2. Set up the state to go forward
            In step 1, following variables are assigned
            - prop: the property that gets associated to the value represented by this element
            - xmlProp: the real property represented by this element; xmlProp and prop are usually
                identical but they can be different, as in the case of substitution groups and
                on-demand open-content properties
            - child: the child DataObject associated to this element or null if the element
                represents a simple type
            - childType: the type of the child DataObject or of the data value
            - _currentSchemaTypeCode: the schema type code of the element pointed to by xsi:type,
                for cases in which there is more than one Schema type mapping to the same SDO
                type and the difference is significant, ie requires different unmarshalling
                algorithms (one case so far: xs:anyURI and xs:QName)
            In step 2, following variables are assigned
            - _pendingChange: if this element is a changeSummary element, then the
                PlainChangeSummaryUnmarshaller that is handling unmarshalling for this change
            - _changeOwner: if this element is a changeSummary element, then the parent DataObject
                of the change summary; this object will be used to provide context for resolving
                the information in the change summary
            - _currentProp: if this element is a data value, then the property that will be associ-
                ated to this data value when it gets assigned via a call equivalent to
                DataObject.set(prop, value)
            - _currentChildType: if this element is a data value, then the expected type for that
                value; used for unmarshalling the text correctly
            - _currentBuffer: if this element is a data value, a buffer that will collect the text
                representation of the data value, in case it comes via more than one text event
            - _currentPrefix: if this element is a data value, the prefix that was used in the
                element event
            - _currentXmlProp: if this element is a data value, the real xml property represented
                by the element, see "xmlProp"
            - _placeHolder: an instance of a "special" DataObject class that makes it easier to
                handle the data object and data value cases with common code
            - _current: the DataObject element represented by this element, that will be filled
                with the information unmarshalled from the XML sub-tree starting here
            */
            if (prop == null)
            {
                prop = getPropertyByName(uri, name);
                xmlProp = prop;
            }
            if (prop == null)
            {
                // Open content
                if (!parentType.isOpen())
                {
                    // Do some extra checks to see if there is a namespace mismatch problem so we can
                    // report a more specific error
                    for (PropertyXML declaredProp : parentType.getDeclaredPropertiesXML())
                        if (declaredProp.isXMLElement() && declaredProp.getXMLName().equals(name))
                            throw new SDOUnmarshalException(SDOError.messageForCodeAndLocation(
                                "unmarshal.type.notopen.element.badnamespace", _locator, parentType, name,
                                uri, declaredProp.getXMLName(), declaredProp.getXMLNamespaceURI()));
                    throw new SDOUnmarshalException(SDOError.messageForCodeAndLocation("unmarshal.type.notopen.element",
                        _locator, parentType, name, uri));
                }
                // If there is an xsi:type, use it
                if (xsiTypeName != null)
                {
                    childType = getTypeByName(xsiTypeUri, xsiTypeName);
                    if (childType == null)
                        throwXsiTypeNotFoundException(xsiTypeUri, xsiTypeName);
                    else
                        _currentSchemaTypeCode = getSchemaTypeCode(xsiTypeUri, xsiTypeName, childType);
                }

                // Make sure _currentProp, _currentChildType and child are assigned properly
                prop = getOpenContentProperty(uri, name, parent, childType, true);
                xmlProp = prop;
                if (childType == null)
                {
                    child = parent.createDataObjectXML(prop, prefix, xmlProp);
                    childType = BuiltInTypeSystem.DATAOBJECT;
                }
                else if (childType == BuiltInTypeSystem.CHANGESUMMARYTYPE)
                    child = null;
                else if (childType.isDataType())
                {
                    child = prop.getTypeXML().getTypeCode() == BuiltInTypeSystem.TYPECODE_DATAOBJECT
                        ? parent.createDataObjectXML(prop, BuiltInTypeSystem.WRAPPERTYPE, prefix,
                        xmlProp) : null;
                }
                else
                    child = parent.createDataObjectXML(prop, childType, prefix, xmlProp);
            }
            else
            {
                childType = xmlProp.getTypeXML();
                _currentSchemaTypeCode = xmlProp.getSchemaTypeCode();
                TypeXML xsiType = null;
                if (xsiTypeUri != null)
                {
                    xsiType = getTypeByName(xsiTypeUri, xsiTypeName);
                    if (xsiType == null)
                        throwXsiTypeNotFoundException(xsiTypeUri, xsiTypeName);
                    else
                    {
                        if (childType != BuiltInTypeSystem.DATAOBJECT)
                        {
                            if (!childType.isAssignableFrom(xsiType))
                                throw new SDOUnmarshalException(SDOError.messageForCodeAndLocation("unmarshal.xsitype.notassignable",
                                    _locator, xsiType, childType.getName()));
                            if (childType.isDataType() &&
                                    !Common.wrapClass(childType.getInstanceClass()).
                                    isAssignableFrom(Common.wrapClass(xsiType.getInstanceClass())))
                                throw new SDOUnmarshalException(SDOError.messageForCodeAndLocation("unmarshal.xsitype.javaclass",
                                    _locator, xsiType, xsiType.getInstanceClass(), childType, childType.getInstanceClass()));
                        }
                        _currentSchemaTypeCode = getSchemaTypeCode(xsiTypeUri, xsiTypeName, xsiType);
                    }
                }
                if (childType.isDataType() || !prop.isContainment())
                {
                    child = null;
                    if (xsiType != null)
                        childType = xsiType;
                }
                else if (childType == BuiltInTypeSystem.CHANGESUMMARYTYPE)
                    child = null;
                else if (xsiType != null)
                {
                    child = xsiType.isDataType() ? parent.
                        createDataObjectXML(prop, BuiltInTypeSystem.WRAPPERTYPE, prefix, xmlProp) :
                        parent.createDataObjectXML(prop, xsiType, prefix, xmlProp);

                    childType = xsiType;
                }
                else if (prop != xmlProp)
                    child = parent.createDataObjectXML(prop, xmlProp.getTypeXML(), prefix, xmlProp);
                else
                    child = parent.createDataObjectXML(prop, prefix, xmlProp);
            }
            if (child == null)
            {
                if (childType == BuiltInTypeSystem.CHANGESUMMARYTYPE)
                {
                    PlainChangeSummaryUnmarshaller pcu = new PlainChangeSummaryUnmarshaller(null);
                    pcu.setLink(this);
                    pcu.setLoader(_loader);
                    pcu.setNamespaceHandler(_nsHandler);
                    pcu.setLocator(_locator);
                    pcu._sdoContext = _sdoContext;
                    _loader.changeUnmarshaller(pcu);
                    _pendingChange = pcu;
                    _changeOwner = parent;
                }
                else
                {
                    // Simple content
                    _currentProp = prop;
                    _currentChildType = childType;
                    _currentBuffer.setLength(0);
                    _currentPrefix = prefix;
                    _currentXmlProp = xmlProp;
                    _placeHolder.setContainer(_current);
                    _current = _placeHolder;
                }
            }
            else
            {
                // If there is an opposite property, it will be automatically set and since
                // only one end can be containment=true, it will never be set from both ends
                _current = child;
                if (child.getTypeXML().isSimpleContent())
                {
                    // As per the SDO spec, use a property with name "value"
                    // to store the text of the element
                    PropertyXML simpleContentProperty;
                    if (child.getTypeXML().getTypeCode() == BuiltInTypeSystem.TYPECODE_WRAPPERTYPE)
                    {
                        if (childType == null)
                            simpleContentProperty = getDataFactoryImpl().getValueProperty(
                                BuiltInTypeSystem.OBJECT);
                        else
                            simpleContentProperty = getDataFactoryImpl().getValueProperty(
                                childType);
                    }
                    else
                    {
                        simpleContentProperty = child.getTypeXML().getPropertyXML(
                            Names.SIMPLE_CONTENT_PROP_NAME);
                        _currentSchemaTypeCode = simpleContentProperty.getSchemaTypeCode();
                    }
                    if (simpleContentProperty != null && simpleContentProperty.getTypeXML().isDataType())
                    {
                        _currentProp = simpleContentProperty;
                        _currentXmlProp = null;
                        _currentChildType = childType.isDataType() ? childType :
                            simpleContentProperty.getTypeXML();
                        _currentBuffer.setLength(0);
                        _currentPrefix = prefix;
                    }
                }
            }
        }
    }

    public void endElement()
    {
        PropertyXML containmentProperty = _current.getContainmentPropertyXML();
        // We have to save the container here because part of fixing the property for open content,
        // _current.getContainerXML() may become null
        DataObjectXML container = _current.getContainerXML();
        // If there is text present, assign the property
        if (_currentProp != null)
        {
            TypeXML type = _currentChildType;
            Object value;
            try {
            if (!type.isDataType())
            {
                // We need to overwrite the type for references, because it needs to
                // be parsed as a string
                if (_currentProp.isMany())
                {
                    value = _nilled ? null :
                        _currentBuffer.length() == 0 && _currentProp.getDefault() != null ?
                            _currentProp.getDefault() :
                        SimpleValueHelper.parseBufferToType(_currentBuffer,
                            BuiltInTypeSystem.STRINGS, _currentSchemaTypeCode, _nsHandler);
                    if (value != null)
                    {
                        _idrefObjects.add(container);
                        _idrefProperties.add(_currentProp);
                        List valueAsList = (List) value;
                        if (_currentSchemaTypeCode == SchemaType.BTC_IDREFS)
                            _idrefValues.add(valueAsList);
                        else
                        {
                            List<XPath> xpathList = new ArrayList<XPath>();
                            _idrefValues.add(xpathList);
                            for (int i = 0; i < valueAsList.size(); i++)
                            {
                                String path = (String) valueAsList.get(i);
                                if (path.startsWith(Names.FRAGMENT))
                                    path = path.substring(Names.FRAGMENT.length());
                                try
                                {
                                    xpathList.add(XPath.compile(path, _nsHandler));
                                }
                                catch (XPathCompileException xpce)
                                {
                                    throw new SDOUnmarshalException(SDOError.messageForCodeAndLocation(
                                        "unmarshal.xpath.compile", _locator, path, xpce.getMessage()));
                                }
                            }
                        }
                    }
                }
                else
                {
                    value = _nilled ? null :
                        _currentBuffer.length() == 0 && _currentProp.getDefault() != null ?
                            _currentProp.getDefault() :
                        SimpleValueHelper.parseBufferToType(_currentBuffer,
                            BuiltInTypeSystem.STRING, _currentSchemaTypeCode, _nsHandler);
                    if (value != null)
                    {
                        _idrefObjects.add(container);
                        _idrefProperties.add(_currentProp);
                        String path = (String) value;
                        XPath xpath;
                        // We need to special-case the properties of type javax.sdo.Type because
                        // they are in fact idrefs in a very strange way
                        if (type == BuiltInTypeSystem.TYPE)
                            _idrefValues.add(path);
                        // Even thought the Schema spec discourages use of type IDREF(S) for
                        // elements, we allow it during compilation, so therefore we must support
                        // it at runtime also
                        else if (_currentSchemaTypeCode == SchemaType.BTC_IDREF)
                            _idrefValues.add(path);
                        else
                        {
                            if (path.startsWith(Names.FRAGMENT))
                                path = path.substring(Names.FRAGMENT.length());
                            try
                            {
                                xpath = XPath.compile(path, _nsHandler);
                            }
                            catch (XPathCompileException xpce)
                            {
                                throw new SDOUnmarshalException(SDOError.messageForCodeAndLocation(
                                    "unmarshal.xpath.compile", _locator, path, xpce.getMessage()));
                            }
                            _idrefValues.add(xpath);
                        }
                    }
                }
            }
            else
            {
                value = _nilled ? null :
                    _currentBuffer.length() == 0 && _currentProp.getDefault() != null ?
                        _currentProp.getDefault() :
                    SimpleValueHelper.parseBufferToType(_currentBuffer, type,
                        _currentSchemaTypeCode, _nsHandler);
                if (_current == _placeHolder)
                {
                    if (_currentProp.isMany())
                        if (container.getSequenceXML() != null)
                            container.getSequenceXML().addXML(_currentProp, value,
                                _currentPrefix, _currentXmlProp);
                        else
                            container.getList(_currentProp).add(value);
                    else
                        container.setXML(_currentProp, value, _currentPrefix, _currentXmlProp);
                }
                else
                    _current.set(_currentProp, value);
            }
            } catch (SimpleValueHelper.SimpleValueException e)
            {
                //e.printStackTrace(System.out);
                throw new SDOUnmarshalException(getMessageForSimpleValueException(e, _currentBuffer.
                    toString(), type, _currentProp), chainSimpleValueException(e) ? e.getCause() : null);
            }
            _currentProp = null;
            _currentChildType = null;
        }

        if (_current.getTypeXML() == BuiltInTypeSystem.BEADATAOBJECT && container != null &&
            containmentProperty != null)
        if (containmentProperty.isDynamic() && !containmentProperty.isGlobal())
        {
            //SequenceXML seq = _current.getSequenceXML();
            //if (_nilled || (seq.size() == 1 && seq.getProperty(0) == null /* ie is text */))
            DataObjectImpl dObjImpl = (DataObjectImpl)_current;
            if (_nilled || (dObjImpl.getStore().storeSequenceSize() == 1 && dObjImpl.getStore().storeSequenceGetPropertyXML(0) == null /* ie is text */))
            {
                // We have a candidate for turning into a simple type
                fixOnDemandPropertyDataObjectToObject(containmentProperty, container, _current,
                    _nilled);
                _nilled = false;
            }
            else
            {
                // This is a complex type, we need to update all previous existing
                // properties with this name to the new property
                fixOnDemandPropertyObjectToDataObject(containmentProperty, container, _current);
            }
        }
        else  if (_nilled)
        {
            _nilled = false;
            if (_current != _placeHolder)
            {
                // The object currently on the stack was already added to its container
                // We need to replace that object by a 'null'
                fixNilElement(_current, containmentProperty, container);
            }
        }
        else
        {
            // We may need to convert a sequence containing only one String to a wrapper dataObject
            SequenceXML seq = _current.getSequenceXML();
            if (seq.size() == 1 && seq.getProperty(0) == null /* ie is text */)
            {
                String text = (String) seq.getValue(0);
                changeSequenceToWrapper(containmentProperty, container, _current, text);
            }
        }
        else if (_nilled)
        {
            _nilled = false;
            if (_current != _placeHolder)
            {
                // The object currently on the stack was already added to its container
                // We need to replace that object by a 'null'
                fixNilElement(_current, containmentProperty, container);
            }
        }
        if (_pendingChange != null && _current == _changeOwner)
        {
            _changeOwner = null;
            _pendingChange.setReferenceResolver(_referenceResolver);
            _pendingChange.setRootObject(_current);
            _pendingChange = null;
        }
        if (_current.getTypeXML().getTypeCode() == BuiltInTypeSystem.TYPECODE_TYPE)
        {
            // We need the record the 'uri+#+local' String
            String uri = _current.getString(4);
            String localName = _current.getString(3);
            if (uri != null && localName != null)
                _referenceResolver.registerId(uri + '#' + localName, _current);
        }
        _current = container;
        if (_current == null)
        {
            if (_link != null)
                _loader.changeUnmarshaller(_link); // Relinquish control
        }
    }

    private void fixNilElement(DataObjectXML current, PropertyXML containmentProperty, DataObjectXML container)
    {
        // First, check if there were any attributes
        boolean hasSetAttributes = false;
        if (current.getTypeXML().isSimpleContent())
            for (PropertyXML prop : current.getInstancePropertiesXML())
            {
                if (current.isSet(prop))
                    if (prop.isXMLElement())
                        current.unset(prop);
                    else if (Names.SIMPLE_CONTENT_PROP_NAME.equals(prop.getName()))
                        ;
                    else
                    {
                        hasSetAttributes = true;
                        break;
                    }
            }
        else
            for (PropertyXML prop : current.getInstancePropertiesXML())
            {
                if (current.isSet(prop))
                    if (prop.isXMLElement())
                        current.unset(prop);
                    else
                    {
                        hasSetAttributes = true;
                        break;
                    }
            }

        if (hasSetAttributes)
            return;
        if (container == null)
        {
            // The root element has xsi:nil=true
            if (_root != null)
                _root.setDataObject(null);
            return;
        }
        Store s = (Store) container;
        PropertyXML prop = containmentProperty;
        PropertyXML xmlProp = s.storeSequenceGetSubstitution(s.storeSequenceSize()  -1);
        String prefix = s.storeSequenceGetXMLPrefix(s.storeSequenceSize() - 1);
        _current.detach();
        s.storeAddNew(prop, null, prefix, xmlProp);
    }

    public void attr(String uri, String name, String prefix, String value)
    {
        DataObjectXML parent = _current;
        if (_current == _placeHolder)
            // Simple types cannot have attributes; skip it
            return;
        TypeXML parentType = parent.getTypeXML();
        PropertyXML prop = parentType.getPropertyXMLByXmlName(uri, name, false/*!isElement*/);
        TypeXML childType;
        /* We support the following cases:
           - property is defined in parentType
           - property is a global property from the Schema
           - property is not found, but there is an xsi:type
           - no property found and no xsi:type
           */
        if (prop == null)
            prop = getPropertyByName(uri, name);
        if (prop == null)
        {
            // Open attributes
            if (!parentType.isOpen())
                throw new SDOUnmarshalException(SDOError.messageForCodeAndLocation("unmarshal.type.notopen.attribute",
                    _locator, parentType, name, uri));
            childType = BuiltInTypeSystem.STRING;
            prop = getOpenContentProperty(uri, name, parent, childType, false);
            parent.setXML(prop, value, prefix, prop);
        }
        else
        {
            if (!prop.getType().isDataType())
            {
                _idrefObjects.add(parent);
                _idrefProperties.add(prop);
                if (prop.isMany())
                {
                    childType = BuiltInTypeSystem.STRINGS;
                    try {
                    List as = (List) SimpleValueHelper.parseBufferToType(value, childType,
                        prop.getSchemaTypeCode(), _nsHandler);
                    if (prop.getSchemaTypeCode() == SchemaType.BTC_IDREFS)
                        _idrefValues.add(as);
                    else
                    {
                        List<XPath> xpath = new ArrayList<XPath>(as.size());
                        _idrefValues.add(xpath);
                        for (int i = 0; i < as.size(); i++)
                        {
                            String path = (String) as.get(i);
                            if (path.startsWith(Names.FRAGMENT))
                                path = path.substring(Names.FRAGMENT.length());
                            try
                            {
                                xpath.add(XPath.compile(path, _nsHandler));
                            }
                            catch (XPathCompileException xpce)
                            {
                                throw new SDOUnmarshalException(SDOError.messageForCodeAndLocation(
                                    "unmarshal.xpath.compile", _locator, path, xpce.getMessage()));
                            }
                        }
                    }
                    } catch (SimpleValueHelper.SimpleValueException e)
                    {
                        throw new SDOUnmarshalException(getMessageForSimpleValueException(e, value,
                            childType, prop), chainSimpleValueException(e) ? e.getCause() : null);
                    }
                }
                else
                {
                    childType = BuiltInTypeSystem.STRING;
                    try {
                    value = (String) SimpleValueHelper.parseBufferToType(value, childType, prop.getSchemaTypeCode(),
                        _nsHandler);
                    if (prop.getSchemaTypeCode() == SchemaType.BTC_IDREF ||
                        prop.getTypeXML() == BuiltInTypeSystem.TYPE)
                        _idrefValues.add(value);
                    else
                    {
                        String path;
                        XPath xpath;
                        if (value.startsWith(Names.FRAGMENT))
                            path = value.substring(Names.FRAGMENT.length());
                        else
                            path = value;
                        try
                        {
                            xpath = XPath.compile(path, _nsHandler);
                        }
                        catch (XPathCompileException xpce)
                        {
                            throw new SDOUnmarshalException(SDOError.messageForCodeAndLocation(
                                "unmarshal.xpath.compile", _locator, path, xpce.getMessage()));
                        }
                        _idrefValues.add(xpath);
                    }
                    } catch (SimpleValueHelper.SimpleValueException e)
                    {
                        throw new SDOUnmarshalException(getMessageForSimpleValueException(e, value,
                            childType, prop), chainSimpleValueException(e) ? e.getCause() : null);
                    }
                }
            }
            else
            {
                childType = prop.getTypeXML();
                try {
                Object parsedValue = SimpleValueHelper.parseBufferToType(value, childType,
                    prop.getSchemaTypeCode(), _nsHandler);
                parent.setXML(prop, parsedValue, prefix, prop);
                int schemaTypeCode = prop.getSchemaTypeCode();
                if (schemaTypeCode == SchemaType.BTC_ID)
                    _referenceResolver.registerId((String) parsedValue, parent);
                } catch (SimpleValueHelper.SimpleValueException e)
                {
                    throw new SDOUnmarshalException(getMessageForSimpleValueException(e, value,
                        childType, prop), chainSimpleValueException(e) ? e.getCause() : null);
                }
            }
        }
    }

    public void sattr(int type, String name, String value)
    {
        switch (type)
        {
        case ATTR_XSI:
            if (_current.getContainerXML() == null)
            {
                if (SCHEMA_LOCATION.equals(name))
                    _root.setSchemaLocation(value);
                else if (NO_NAMESCAPCE_SCHEMA_LOCATION.equals(name))
                    _root.setNoNamespaceSchemaLocation(value);
            }
            else if (Names.XSI_NIL.equals(name) && Names.TRUE.equalsIgnoreCase(value))
                _nilled = true;
            break;
        }
    }

    public void xmlns(String prefix, String uri)
    {
        // Nothing special
    }

    public void text(char[] buff, int off, int cch)
    {
        if (cch == 0)
            return;
        if (_current == null)
            return; // Characters before the start of the root element: ignore
        if (_current.getTypeXML().isMixedContent())
            _current.getSequence().addText(new String(buff, off, cch));
        else if (_currentProp != null)
        {
            //if (_currentBufferAsString != null)
            //_currentBuffer.append(_currentBufferAsString);
            //_currentBufferAsString =  null;
            _currentBuffer.append(buff, off, cch);
        }
    }

    private String _currentBufferAsString;

    public void text(String s)
    {
        if (s.length() == 0)
            return;
        if (_current == null)
            return; // Characters before the start of the root element: ignore
        if (_current.getTypeXML().isMixedContent())
            _current.getSequence().addText(s);
        else if (_currentProp != null)
        {
            //if (_currentBuffer.length() == 0 && _currentBufferAsString == null)
            //    _currentBufferAsString = s;
            //else if (_currentBufferAsString != null)
            //{
            //    _currentBuffer.append(_currentBufferAsString).append(s);
            //    _currentBufferAsString = null;
            //}
            //else
                _currentBuffer.append(s);
        }
    }

    public void xmlDecl(String version, String encoding)
    {
        _root.setEncoding(encoding);
        _root.setXMLVersion(version);
        _root.setXMLDeclaration(true);
    }

    void setRootObject(DataObject root)
    {
        _current = (DataObjectXML) root;
    }

    DataObject getRootObject()
    {
        return _root.getRootObject();
    }

    void setReferenceResolver(ReferenceResolver ref)
    {
        _referenceResolver = ref;
    }

    void finish()
    {
        resolveIdrefs();
    }

    // ==========================================
    // ReferenceResolver implementation
    // ==========================================
    public DataObject resolvePath(XPath path, DataObjectXML contextNode)
    {
        XPath.Selection result = XPath.execute(path, contextNode);
        if (result.hasNext())
        {
            Object obj = result.next();
            if (obj instanceof DataObject)
                return (DataObject) obj;
            else
                return null;
        }
        return null;
    }

    public DataObject resolveId(String id)
    {
        return _idMap.get(id);
    }

    public void registerId(String id, DataObject node)
    {
        _idMap.put(id, node);
    }

    public void setIdMap(Map<String, DataObject> map)
    {
        _idMap = map;
    }

    // ========================================
    // Package-level access methods
    // ========================================
    void setIdrefObjects(List<DataObjectXML> _idrefObjects)
    {
        this._idrefObjects = _idrefObjects;
    }

    void setIdrefProperties(List<PropertyXML> _idrefProperties)
    {
        this._idrefProperties = _idrefProperties;
    }

    void setIdrefValues(List<Object> _idrefValues)
    {
        this._idrefValues = _idrefValues;
    }

    void resolveIdrefs()
    {
        for (int i = 0; i < _idrefObjects.size(); i++)
        {
            PropertyXML property = _idrefProperties.get(i);
            DataObjectXML object = _idrefObjects.get(i);
            PropertyXML oppositeProperty = property.getOppositeXML();
            // If "object" is covered by a ChangeSummary that was unmarshalled with logging=true
            // we need to temporarily turn it off to avoid changes being logged
            ChangeSummary cs = object.getChangeSummary();
            boolean logging = false;
            if (cs != null && cs.isLogging())
            {
                logging = true;
                ((ChangeSummaryImpl) cs).setLogging(false);
            }
            if (_idrefValues.get(i) instanceof List)
            {
                if (oppositeProperty != null && object.isSet(property))
                {
                    // This means that one of the opposites has already been set
                    List<DataObject> list = object.getList(property);
                    for (Object ref : (List) _idrefValues.get(i))
                    {
                        DataObject referred = resolveReference(ref, object);
                        if (checkListForOpposite(object, property, list, referred,
                            oppositeProperty, ref.toString()))
                            list.add(referred);
                    }
                }
                else
                {
                    List<DataObject> result = new ArrayList<DataObject>(((List) _idrefValues.get(i)).size());
                    for (Object ref : (List) _idrefValues.get(i))
                    {
                        DataObject referred = resolveReference(ref, object);
                        result.add(referred);
                    }
                    object.setList(property, result);
                }
            }
            else
            {
                Object ref = _idrefValues.get(i);
                if (property.getType() == BuiltInTypeSystem.TYPE)
                {
                    Object referred = resolveTypeReference(ref);

                    if (referred == null)
                        throw new SDOUnmarshalException(SDOError.messageForCode(
                            "unmarshal.idref.result", ref.toString()));
                    else
                        object.set(property, referred);
                }
                else
                {
                    DataObject referred = resolveReference(ref, _idrefObjects.get(i));
                    if (referred == null)
                    {
                        throw new SDOUnmarshalException(SDOError.messageForCode(
                            ref instanceof XPath ? "unmarshal.xpath.result" : "unmarshal.idref.result",
                            ref.toString()));
                    }
                    else if (oppositeProperty != null)
                    {
                        if (checkForOpposite(object, property, referred, oppositeProperty,
                            ref.toString()))
                            object.setDataObject(property, referred);
                    }
                    else
                        object.setDataObject(property, referred);
                }
            }
            if (logging)
                ((ChangeSummaryImpl) cs).setLogging(true);
        }
    }

    // ==========================================
    // Private methods
    //===========================================
    private static boolean checkForOpposite(DataObjectXML object, PropertyXML property,
        DataObject oppositeObject, PropertyXML oppositeProperty, String oppositePath)
    {
        // Because when a DataObject reference is set, if it is a bidirectional property
        // the other end is automatically set, we may find the property already set, so
        // instead of setting it again, we check for consistency
        if (object.isSet(property))
        {
            if (object.get(property) == oppositeObject)
                return false;
            else
                throw new SDOUnmarshalException(SDOError.messageForCode("unmarshal.bidirectional",
                    property.getName(), oppositePath, oppositeProperty.getName())); 
        }
        else
            return true;
    }

    private static boolean checkListForOpposite(DataObjectXML object, PropertyXML property,
        List value, DataObject oppositeObject, PropertyXML oppositeProperty, String oppositePath)
    {
        // When the current property is a list of references that is also bidirectional
        // we need to make sure that the reference is not already set in this list and
        // that if the opposite property is single-valued, is not already set to another
        // object
        if (!oppositeProperty.isMany())
        {
            if (oppositeObject.isSet(oppositeProperty))
                if (object == oppositeObject.get(oppositeProperty))
                    return false;
                else
                    throw new SDOUnmarshalException(SDOError.messageForCode("unmarshal.bidirectional",
                        property.getName(), oppositePath, oppositeProperty.getName()));
            else
                return true;
        }
        else
        {
            if (value.contains(oppositeObject))
                return false;
            else
                return true;
        }
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

    private boolean chainSimpleValueException(SimpleValueHelper.SimpleValueException e)
    {
        switch (e.cause())
        {
            case SimpleValueHelper.UNMARSHAL_SIMPLE_NOCONSTRUCTOR:
            case SimpleValueHelper.UNMARSHAL_SIMPLE_CONSTRUCTOREXCEPTION:
                return true;
            default:
                return false;
        }
    }

    private void throwXsiTypeNotFoundException(String xsiTypeUri, String xsiTypeName)
    {
        if (!dontthrow)
            throw new XsiTypeNotFoundException(SDOError.messageForCodeAndLocation("unmarshal.xsitype.notfound",
                _locator, xsiTypeName, xsiTypeUri), xsiTypeUri, xsiTypeName);
    }

    private PropertyXML getPropertyByName(String uri, String name)
    {
        java.util.Set<String> entry = null;
        if (uri == null)
        {
            for (int i = 0; i < _cacheURIs.size(); i++)
                if (_cacheURIs.get(i) == null)
                    entry = _cacheNotFoundNames.get(i);
        }
        else
            for (int i = 0; i < _cacheURIs.size(); i++)
                if (uri.equals(_cacheURIs.get(i)))
                    entry = _cacheNotFoundNames.get(i);
        if (entry != null && entry.contains(name))
        {
            return null;
        }

        PropertyXML result = PropertyImpl.getPropertyXML(_sdoContext.getXSDHelper().
            getGlobalProperty(uri, name, true));
        if (result == null)
            result = PropertyImpl.getPropertyXML(_sdoContext.getTypeHelper().getOpenContentProperty(uri, name)); 
        if (result == null)
        {
            if (entry == null)
            {
                _cacheURIs.add(uri);
                entry = new java.util.HashSet<String>();
                _cacheNotFoundNames.add(entry);
            }
            entry.add(name);
        }
        return result;
    }

    private List<java.util.Set<String>> _cacheNotFoundNames = new ArrayList<java.util.Set<String>>();
    private List<String> _cacheURIs = new ArrayList<String>();

    private TypeXML getTypeByName(String uri, String name)
    {
        TypeXML result = _sdoContext.getBindingSystem().loadTypeBySchemaTypeName(uri, name);
        if (result == null)
        {
            // We didn't find a type with the given XML name, let's try and see if we find
            // a type with that SDO name
            result = _sdoContext.getBindingSystem().loadTypeByTypeName(uri, name);
        }
        return result;
    }

    private int getSchemaTypeCode(String xsiTypeUri, String xsiTypeName, TypeXML xsiType)
    {
        // We need to find the schemaTypeCode for the type referenced
        // via xsi:type; this implies looking up the Schema type in the
        // loader, since the SDO type URI maps to both xs:anyURI and xs:QName
        // and we need to know which one it is
        if (BuiltInTypeSystem.URI.isAssignableFrom(xsiType))
        {
            org.apache.xmlbeans.SchemaTypeLoader stl = _sdoContext.getTypeSystem().getSchemaTypeLoader();
            // HACKHACK(radup) this should not be null normally
            if (stl == null)
                stl = org.apache.xmlbeans.XmlBeans.getContextTypeLoader();
            SchemaType schemaXsiType = stl.findType(new QName(xsiTypeUri, xsiTypeName));
            if (schemaXsiType != null)
                return Common.getBuiltinTypeCode(schemaXsiType);
        }
        return SchemaType.BTC_ANY_SIMPLE;
    }

    private DataObject resolveReference(Object ref, DataObjectXML parent)
    {
        if (ref instanceof XPath)
        {
            // It's an SDO path
            return _referenceResolver.resolvePath((XPath) ref, parent);
        }
        else
        {
            // It's an IDREF
            return _referenceResolver.resolveId((String) ref);
        }
    }

    private Object resolveTypeReference(Object ref)
    {
        String refAsString = (String) ref;
        Object result = _referenceResolver.resolveId(refAsString);
        if (result != null)
            return result;
        int hashIndex = refAsString.indexOf('#');
        if (hashIndex < 0)
            return null;
        String uri = refAsString.substring(0, hashIndex);
        String name = refAsString.substring(hashIndex + 1);
        return _sdoContext.getTypeSystem().getTypeXML(uri, name);
    }

    private PropertyXML getOpenContentProperty(String uri, String name,
        DataObjectXML context, TypeXML xsiType, boolean element)
    {
        PropertyXML prop;
        prop = (PropertyXML) context.getInstanceProperty(name);
        if (prop != null && prop.isXMLElement() != element)
            prop = null;
        if (prop != null &&
                ((uri == null ? prop.getXMLNamespaceURI() != null :
                    !uri.equals(prop.getXMLNamespaceURI()))
                || !prop.isDynamic() || prop.isGlobal()))
            prop = null; /* we need a new property */
        // Attributes are always single-valued, String type properties
        if (!element)
            return getDataFactoryImpl().getRootProperty(uri, name,
                BuiltInTypeSystem.STRING, element);
        /* We have 9 cases, depending on prop type
        - null
        - DATAOBJECT
        - OBJECT
        and on xsiType
        - null
        - simple
        - complex
        If xsiType is null, then a new property of type DataObject is
        returned because we need to be able to handle the case where the current
        element is going to have element content.
        If xsiType is simple, then:
        - if propertyType is null, a new property of type Object is created
        - if propertyType is OBJECT, the same property is returned
        - if propertyType is DATAOBJECT, the property is returned and a wrapper type
          will be used, whose "value" property will be of the type xsiType.
        If xsiType is complex, then:
        - if propertyType is null, then a new property of type DATAOBJECT is created and returned
        - if propertyType is OBJECT, then it is coerced to DATAOBJECT and the newly created
          property is returned
        - if propertyType is DATAOBJECT already, then the current property is returned.
        */
        if (xsiType == null)
        {
            prop = getDataFactoryImpl().getRootProperty(uri, name,
                BuiltInTypeSystem.DATAOBJECT, element);
        }
        else if (xsiType.isDataType())
        {
            if (prop == null)
            {
                if (isDefaultType(xsiType))
                    prop = getDataFactoryImpl().getRootProperty(uri, name,
                        BuiltInTypeSystem.OBJECT, element);
                else
                    prop = getDataFactoryImpl().getRootProperty(uri, name,
                        BuiltInTypeSystem.DATAOBJECT, element);
            }
            else if (prop.getType() == BuiltInTypeSystem.OBJECT)
            {
                if (!isDefaultType(xsiType))
                {
                    prop = getDataFactoryImpl().getRootProperty(uri, name,
                        BuiltInTypeSystem.DATAOBJECT, element);
                    fixOnDemandPropertyObjectToDataObject(prop, context, null);
                }
            }
            else if (prop.getType() == BuiltInTypeSystem.DATAOBJECT)
                ;
            else
                throw new IllegalStateException("On-demand open content property has type: " +
                    prop.getType());
        }
        else
        {
            if (prop == null)
                prop = getDataFactoryImpl().getRootProperty(uri, name,
                    BuiltInTypeSystem.DATAOBJECT, element);
            else if (prop.getType() == BuiltInTypeSystem.OBJECT)
            {
                prop = getDataFactoryImpl().getRootProperty(uri, name,
                    BuiltInTypeSystem.DATAOBJECT, element);
                fixOnDemandPropertyObjectToDataObject(prop, context, null);
            }
            else if (prop.getType() == BuiltInTypeSystem.DATAOBJECT)
                ;
            else
                throw new IllegalStateException("On-demand open content property has type: " +
                    prop.getType());
        }

        return prop;
    }

    /**
     * Verifies if the <code>type</code> argument represents an SDO type which is the default
     * type for its instance class. For example, <code>Int</code> satisfies this condition
     * because it's instance class is <code>int.class</code> and the default SDO type for
     * <code>int.class</code> is <code>Int</code>, But <code>String</code> does not because
     * the corresponding instance class is <code>String.class</code> which is considered to
     * map to <code>Object</code> type.
     * @param type the type to check
     * @return
     */
    private boolean isDefaultType(TypeXML type)
    {
        Class instanceClass = type.getInstanceClass();
        return instanceClass != null && !instanceClass.equals(String.class) &&
            _sdoContext.getBindingSystem().getType(instanceClass) == type;
    }

    /**
     * Changes the type of the given on-demand property on the given object to
     * {@link davos.sdo.impl.type.BuiltInTypeSystem#DATAOBJECT} from
     * {@link davos.sdo.impl.type.BuiltInTypeSystem#OBJECT}. It also updates all
     * uses of the property <code>prop</code>.
     * @param newProp the property whose type needs to be updated
     * @param container the object on which this on-demand property has been defined
     * @return a new property with all the details of the given property and the type
     * DATAOBJECT; all uses are updated to this property.
     */
    private void fixOnDemandPropertyObjectToDataObject(PropertyXML newProp, DataObjectXML container,
        DataObjectXML currentValue)
    {
        Store s = (Store) container;
        PropertyXML oldProp = null;
        for (int i = 0; i < s.storeSequenceSize(); i++)
        {
            PropertyXML prop = s.storeSequenceGetPropertyXML(i);
            Object value = s.storeSequenceGetValue(i);
            if (prop == null)
                continue;

            if (value == currentValue)
            {
                if (oldProp != null && oldProp.getType() == BuiltInTypeSystem.DATAOBJECT)
                {
                    // A property with that name of type DATAOBJECT already existed
                    String prefix = s.storeSequenceGetXMLPrefix(i);
                    s.storeSequenceUnset(i);
                    s.storeSequenceAddNew(i, oldProp, value, prefix, oldProp);
                }
                break;
            }
            if (oldProp == null && newProp.getName().equals(prop.getName()) && prop.isDynamic() &&
                !prop.isGlobal() && prop.isXMLElement() && (newProp.getXMLNamespaceURI() == null ?
                    prop.getXMLNamespaceURI() == null : newProp.getXMLNamespaceURI().
                    equals(prop.getXMLNamespaceURI())))
            {
                oldProp = prop;
                if (prop.getType() != BuiltInTypeSystem.OBJECT &&
                        prop.getType() != BuiltInTypeSystem.DATAOBJECT)
                    throw new IllegalArgumentException("On-demand open content property has type: "+
                        prop.getType());
            }
            if (prop == oldProp && prop != null && oldProp.getType() == BuiltInTypeSystem.OBJECT)
            {
                // Update the value
                String prefix = s.storeSequenceGetXMLPrefix(i);
                DataObject newValue = getDataFactoryImpl().create(BuiltInTypeSystem.WRAPPERTYPE);
                if (String.class.equals(value.getClass()))
                    newValue.set(getDataFactoryImpl().getValueProperty(BuiltInTypeSystem.OBJECT), value);
                else
                {
                    // Pick a suitable type for the "value" property
                    Class valueClass = Common.unwrapClass(value.getClass());
                    TypeXML valueSdoType = _sdoContext.getBindingSystem().getType(valueClass);
                    newValue.set(getDataFactoryImpl().getValueProperty(valueSdoType), value);
                }
                s.storeSequenceUnset(i);
                s.storeSequenceAddNew(i, newProp, newValue, prefix, newProp);
            }
        }
    }

    /**
     * Looks for a property with the same name as <code>currentProp</code>. If found, then
     * <code>currentProp</code> is replaced with that property, if not found, then a new property
     * of type OBJECT is created and <code>currentProp</code> is replaced with that property 
     * @param currentProp the property candidate for conversion
     * @param container the object on which this on-demand property has been defined
     * @param currentObject the object just parsed when this method was called; used as a stop cond
     * @param nilled true if xsi:nil was present
     */
    private void fixOnDemandPropertyDataObjectToObject(PropertyXML currentProp,
        DataObjectXML container, DataObjectXML currentObject, boolean nilled)
    {
        Store s = (Store) container;
        PropertyXML newProp = null;
        for (int i = 0; i < s.storeSequenceSize(); i++)
        {
            PropertyXML prop = s.storeSequenceGetPropertyXML(i);
            Object value = s.storeSequenceGetValue(i);

            if (value == currentObject)
            {
                if (currentProp != prop)
                    throw new IllegalArgumentException();
                // Handle the newly inserted value: conversion or not?
                if (newProp == null) // first property of this name
                {
                    newProp = getDataFactoryImpl().getRootProperty(prop.getXMLNamespaceURI(),
                        prop.getXMLName(), BuiltInTypeSystem.OBJECT, prop.isXMLElement());
                }
                if (newProp.getType() == BuiltInTypeSystem.OBJECT)
                {
                    // Convert
                    String text = nilled ? null : currentObject.getSequence().size() == 0 ? 
                        Common.EMPTY_STRING : (String) currentObject.getSequence().getValue(0);
                    String prefix = s.storeSequenceGetXMLPrefix(i);
                    s.storeSequenceUnset(i);
                    s.storeSequenceAddNew(i, newProp, text, prefix, newProp);
                }
                else if (newProp.getType() == BuiltInTypeSystem.DATAOBJECT)
                {
                    // We still need to convert the sequence with only one text element inside
                    // into a wrapper object
                    String prefix = s.storeSequenceGetXMLPrefix(i);
                    String text = nilled ? null : currentObject.getSequence().size() == 0 ? 
                        Common.EMPTY_STRING : (String) currentObject.getSequence().getValue(0);
                    s.storeSequenceUnset(i);
                    DataObject newValue = getDataFactoryImpl().create(BuiltInTypeSystem.WRAPPERTYPE);
                    PropertyXML valueProp = getDataFactoryImpl().getValueProperty(
                        BuiltInTypeSystem.OBJECT);
                    newValue.set(valueProp, text);
                    s.storeSequenceAddNew(i, newProp, nilled ? null : newValue, prefix, newProp);
                }
                return;
            }
            else if (prop != null && newProp == null && currentProp.getName().equals(prop.getName())
                    && prop.isDynamic() && !prop.isGlobal() && prop.isXMLElement() &&
                    (currentProp.getXMLNamespaceURI() == null ? prop.getXMLNamespaceURI() == null :
                        currentProp.getXMLNamespaceURI().equals(prop.getXMLNamespaceURI())))
            {
                // Previous open-content property with same name
                newProp = prop;
                // The way the code works it is never the case that we'll need to convert
                // multiple sequences of one text property, because each would
                // have been converted immediately upon being parsed
           }
        }
    }

    private void changeSequenceToWrapper(PropertyXML property, DataObjectXML parent,
        DataObjectXML child, String text)
    {
        Store s = (Store) parent;
        int i = s.storeSequenceSize();
        //while (i > 0 && child != s.storeSequenceGetValue(--i)); // This should not be needed
        i--;
        PropertyXML xmlProp = s.storeSequenceGetPropertyXML(i);
        String prefix = s.storeSequenceGetXMLPrefix(i);
        s.storeSequenceUnset(i);
        DataObjectXML wrapper = parent.createDataObjectXML(property, BuiltInTypeSystem.WRAPPERTYPE,
            prefix, xmlProp);
        wrapper.set(getDataFactoryImpl().getValueProperty(BuiltInTypeSystem.OBJECT), text);
    }

    private static class SimpleDataObjectXML extends DataObjectGeneral
    {
        private DataObjectXML _container;

        private SimpleDataObjectXML()
        {
        }

        public static SimpleDataObjectXML newInstance()
        {
            return new SimpleDataObjectXML();
        }

        public void setContainer(DataObjectXML container)
        {
            _container = container;
        }

        public DataObjectXML getContainerXML()
        {
            return _container;
        }

        public TypeXML getTypeXML()
        {
            return BuiltInTypeSystem.STRING;
        }
    }
}
