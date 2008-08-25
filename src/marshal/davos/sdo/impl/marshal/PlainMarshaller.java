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

import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import davos.sdo.DataObjectXML;
import davos.sdo.PropertyXML;
import davos.sdo.SDOContext;
import davos.sdo.SDOError;
import davos.sdo.SDOMarshalException;
import davos.sdo.SequenceXML;
import davos.sdo.TypeXML;
import davos.sdo.Options;
import davos.sdo.ListXMLIterator;
import davos.sdo.impl.common.Common;
import davos.sdo.impl.common.Names;
import davos.sdo.impl.common.NamespaceStack;
import davos.sdo.impl.data.ChangeSummaryImpl;
import davos.sdo.impl.data.DataObjectImpl;
import davos.sdo.impl.type.BuiltInTypeSystem;
import davos.sdo.impl.type.PropertyImpl;
import davos.sdo.impl.type.SimpleValueHelper;
import davos.sdo.impl.util.XmlPath;

import javax.sdo.DataObject;

import org.apache.xmlbeans.SchemaType;

class PlainMarshaller extends Marshaller implements ReferenceBuilder
{
    private Saver _h;
    private ReferenceBuilder _referenceBuilder;
    // We are reusing this for XMLStreamReader implementation
    PlainChangeSummaryMarshaller _helper;
    private String _rootUri;
    private String _rootName;
    private DataObject _rootObject;
    private DataObject _currentCSObject = null;

    private Object _optionsObject;
    private static final int OPT_NOEXCEPTIONS = 1;
    private static final int OPT_PRETTY_PRINT = 1<<1;
    private int _options;

    private int _indentStep = DEFAULT_INDENT_STEP;
    private int _currentIndent = NEWLINE.length();
    // Used to get the SDO type for a given Java class when generatin xsi:type for data values
    protected SDOContext _sdoContext;

    PlainMarshaller(Object options)
    {
        Map map = null;
        if (options instanceof Map)
            map = (Map) options;
        else if (options instanceof Options)
            map = ((Options) options).getMap();
        if (map != null)
        {
            if (map.containsKey(Options.SAVE_DONT_THROW_EXCEPTIONS))
                this._options |= OPT_NOEXCEPTIONS;
            if (map.containsKey(Options.SAVE_PRETTY_PRINT))
                this._options |= OPT_PRETTY_PRINT;
            if (map.containsKey(Options.SAVE_INDENT))
                _indentStep = (Integer) map.get(Options.SAVE_INDENT);
        }
        _optionsObject = options;
    }

    /*
     * @see davos.sdo.impl.marshal.Marshaller#marshal(javax.sdo.DataObject)
     */
    void marshal(DataObject rootObject,
            String uri, String name,
            boolean xmlDecl, String xmlVersion, String encoding,
            String schemaLocation, String noNSSchemaLocation, SDOContext sdoctx)
    {
        if (_h == null)
            throw new IllegalStateException("SDOEventModel must be set before marshalling can begin");

        if (!(rootObject instanceof DataObjectXML))
            throw new IllegalArgumentException("This marshaller can only handle XML-specific implementations");

        DataObjectXML rootObjectXML = (DataObjectXML) rootObject;
        // Check if we need xsi:type
        if (name == null || name.length() == 0)
        {
            name = Names.SDO_DATAOBJECT;
            uri = Common.EMPTY_STRING;
        }
        PropertyXML rootProp = getPropertyByName(sdoctx, uri, name);
        TypeXML type = rootProp == null ? null : rootProp.getTypeXML();
        TypeXML actualType = rootObjectXML.getTypeXML();

        String xsiUri = null, xsiName = null;
        int schemaTypeCode = -1;
        Object simpleValue = null;
        TypeXML simpleType = null;
        if (actualType.getTypeCode() == BuiltInTypeSystem.TYPECODE_WRAPPERTYPE ||
                actualType.getTypeCode() == BuiltInTypeSystem.TYPECODE_VALUETYPE)
        {
            // This is a wrapper for a simpleType value, so we treat it specially, by first
            // unwrapping the value
            PropertyXML simpleContentProp = (PropertyXML) rootObjectXML.getInstanceProperty(Names.
                SIMPLE_CONTENT_PROP_NAME);
            simpleValue = rootObjectXML.get(simpleContentProp);
            simpleType = type == null ? simpleContentProp.getTypeXML() : type;
            // This is the code from marshalSimpleElement(), copied here because it has 3 results
            if ((simpleType == BuiltInTypeSystem.DATAOBJECT || simpleType == BuiltInTypeSystem.OBJECT) &&
                /* String is the default mapping for simple types */
                simpleValue.getClass() != String.class)
            {
                Class valueClass = Common.unwrapClass(simpleValue.getClass());
                TypeXML valueSdoType = sdoctx.getBindingSystem().getType(valueClass);
                if (valueSdoType != null)
                    if (simpleType == BuiltInTypeSystem.DATAOBJECT ||
                        simpleType.isAssignableFrom(valueSdoType))
                    {
                        QName qname = getXsiTypeName(valueSdoType);
                        if (qname != null)
                        {
                            xsiUri = qname.getNamespaceURI();
                            xsiName = qname.getLocalPart();
                        }
                        SchemaType st = valueSdoType.getXMLSchemaType();
                        if (st != null)
                            schemaTypeCode = Common.getBuiltinTypeCode(st);
                        simpleType = valueSdoType;
                    }
                    else if ((_options & OPT_NOEXCEPTIONS) == 0)
                        throw new SDOMarshalException(SDOError.messageForCode("marshal.xsitype.notassignable",
                            valueSdoType, name, uri, simpleType));
            }
            else if (simpleType != type)
            {
                QName qname = getXsiTypeName(simpleType);
                if (qname != null)
                {
                    xsiUri = qname.getNamespaceURI();
                    xsiName = qname.getLocalPart();
                }
                SchemaType st = simpleType.getXMLSchemaType();
                if (st != null)
                    schemaTypeCode = Common.getBuiltinTypeCode(st);
            }
        }
        else if (actualType != type &&
            actualType.getTypeCode() != BuiltInTypeSystem.TYPECODE_BEADATAOBJECT)
        {
            if (type == null)
            {
                // According to the spec if the type is not known, we serialize xsi:type
                // to make it possible to retrieve the type information upon unmarshalling
                QName typeName = getXsiTypeName(actualType);
                if (typeName != null)
                {
                    xsiUri = typeName.getNamespaceURI();
                    xsiName = typeName.getLocalPart();
                }
            }
            else if (type.isAssignableFrom(actualType))
            {
                QName typeName = getXsiTypeName(actualType);
                if (typeName != null)
                {
                    xsiUri = typeName.getNamespaceURI();
                    xsiName = typeName.getLocalPart();
                }
                else if ((_options & OPT_NOEXCEPTIONS) == 0)
                    throw new SDOMarshalException(SDOError.messageForCode("marshal.xsitype.notglobal",
                        actualType));
            }
            else if ((_options & OPT_NOEXCEPTIONS) == 0)
            {
                if (rootObject instanceof DataObjectImpl && ((DataObjectImpl)rootObject).getSDOContext()!=sdoctx)
                    throw new IllegalArgumentException("Trying to marshal a data object using a different context than the one it was created with.");

                throw new SDOMarshalException(SDOError.messageForCode("marshal.xsitype.notassignable",
                    actualType, name, uri, type));
            }
        }

        // Handle the XML decl
        if (xmlDecl)
        {
            _h.xmlDecl(xmlVersion, encoding);
//            indent(_h, _currentIndent);
        }

        _h.startElement(uri, name, uri == Names.URI_SDO ? Names.PREFIX_SDO : null, xsiUri, xsiName);
        _rootUri = uri;
        _rootName = name;
        _rootObject = rootObject;
        // Add the schemaLocation attributes
        if (schemaLocation != null)
            _h.sattr(SDOEventModel.ATTR_XSI,
                SDOEventModel.SCHEMA_LOCATION, schemaLocation);
        if (noNSSchemaLocation != null)
            _h.sattr(SDOEventModel.ATTR_XSI,
                SDOEventModel.NO_NAMESCAPCE_SCHEMA_LOCATION, noNSSchemaLocation);

        _sdoContext = sdoctx;

        if (simpleValue != null)
        {
            try {
                _h.text(SimpleValueHelper.getLexicalRepresentation(simpleValue, simpleType,
                    schemaTypeCode, _h.getNamespaceStack()));
            } catch (SimpleValueHelper.SimpleValueException e)
            {
                if ((_options & OPT_NOEXCEPTIONS) == 0)
                    throw new SDOMarshalException(getMessageForSimpleValueException(e, simpleValue,
                        simpleType, uri, name));
            }
        }
        else
            marshalInternal(rootObjectXML, actualType);
        _h.endElement();
    }

    /*
     * @see davos.sdo.impl.marshal.Marshaller#setSaver(davos.sdo.impl.marshal.SDOEventModel)
     */
    void setSaver(Saver e)
    {
        _h = e;
    }

    public void setReferenceBuilder(ReferenceBuilder referenceBuilder)
    {
        _referenceBuilder = referenceBuilder;
    }

    /*
     * This marshals the "contents" of the element, but _without_ the open and end tags
     * The main reason for this is that the root element needs to take care of the
     * schemaLocation stuff
     */
    private void marshalInternal(DataObjectXML object, TypeXML type)
    {
        boolean doIndent = (_options & OPT_PRETTY_PRINT) != 0;
        boolean wasIndentIncremented = false;
        // Serialize all properties
        List props = object.getInstanceProperties();
        int size = props.size();
        int indexOfFirstElement = size;
        boolean simpleContent = type.isSimpleContent();
        for (int i = 0; i < size; i++)
        {
            PropertyXML p = (PropertyXML) props.get(i);
            Object value = object.get(p);
            if (p.isXMLElement())
            {
                if (indexOfFirstElement == size)
                    indexOfFirstElement = i;
            }
            else
            {
                // Marshal attribute
                if (object.isSet(p))
                {
                    if (simpleContent && Names.SIMPLE_CONTENT_PROP_NAME.equals(p.getName()))
                        indexOfFirstElement = i;
                    else if (value != null)
                    {
                        ListXMLIterator it = object.getListXMLIterator(p);
                        it.next();
                        marshalAttributeProperty(it.getSubstitution(), value, it.getPrefix(), object);
                    }
                }
            }
        }
        if (object.getType().isSequenced())
        {
            if (doIndent)
                if (!object.getTypeXML().isMixedContent())
                    wasIndentIncremented = incrementIndent();
                else
                    doIndent = false;
            SequenceXML s = object.getSequenceXML();
            // Use the Sequence to populate the children
            int seqSize = s.size();
            // Now process elements/text
            for (int i = 0; i < seqSize; i++)
            {
                PropertyXML p = s.getPropertyXML(i);

                if ( p == null /*ie is text*/ )
                    _h.text((String) s.getValue(i));
                else if (p.isXMLElement())
                {
                    Object val = s.getValue(i);
                    if (val == null)
                        marshalXsiNil(s.getSubstitution(i).getXMLNamespaceURI(),
                            s.getSubstitution(i).getXMLName(), s.getPrefixXML(i), doIndent);
                    else
                        marshalElementProperty(s.getSubstitution(i), val, s.getPrefixXML(i),
                            object, true, doIndent);
                }
            }
        }
        else
        {
            if (doIndent)
                wasIndentIncremented = incrementIndent();
            if (simpleContent && indexOfFirstElement < size)
            {
                PropertyXML p = (PropertyXML) props.get(indexOfFirstElement);
                Object value = object.get(p);
                if (value == null)
                    // The object itself was not null, but the value of its text
                    // property was, only thing we can do is add an xsi:nil attribute
                    _h.sattr(SDOEventModel.ATTR_XSI, Names.XSI_NIL, Names.TRUE);
                else
                {
                    try {
                        _h.text(SimpleValueHelper.getLexicalRepresentation(value, p.getTypeXML(),
                            p.getSchemaTypeCode(), _h.getNamespaceStack()));
                    } catch (SimpleValueHelper.SimpleValueException e)
                    {
                        if ((_options & OPT_NOEXCEPTIONS) == 0)
                            throw new SDOMarshalException(getMessageForSimpleValueException(e,
                                value, p.getTypeXML(), p.getXMLNamespaceURI(), p.getXMLName()));
                    }
                }
                if (wasIndentIncremented)
                    decrementIndent();
                return;
            }
            if (indexOfFirstElement >= size)
            {
                // No elements
                if (wasIndentIncremented)
                    decrementIndent();
                return;
            }
            // Now on to elements
            for (int i = indexOfFirstElement; i < size; i++)
            {
                PropertyXML p = (PropertyXML) props.get(i);
                Object value = object.get(p);
                // For the time being, use a greedy strategy and
                // serialize the properties in the order they are declared
                if (!p.isXMLElement())
                {
                }
                else if (value == null)
                {
                    if (object.isSet(p))
                    {
                        ListXMLIterator it = object.getListXMLIterator(p);
                        if (it.next())
                        {
                            marshalXsiNil(it.getSubstitution().getXMLNamespaceURI(),
                                it.getSubstitution().getXMLName(), it.getPrefix(), doIndent);
                        }
                    }
                    // Property is unset, so no element
                }
                else if (p.isMany())
                {
                    ListXMLIterator xmlit = object.getListXMLIterator(p);
                    while (xmlit.next())
                    {
                        Object val = xmlit.getValue();
                        PropertyXML xmlprop = xmlit.getSubstitution();
                        if (val == null)
                            marshalXsiNil(xmlprop.getXMLNamespaceURI(), xmlprop.getXMLName(),
                                xmlit.getPrefix(), doIndent);
                        else
                            marshalElementProperty(xmlprop, val, xmlit.getPrefix(), object, true,
                                doIndent);
                    }
                }
                else
                {
                    if ( object.isSet(p) )
                    {
                        ListXMLIterator it = object.getListXMLIterator(p);
                        boolean hasNext = it.next();
                        assert hasNext;  // must return true, because there is a non-null value p is Set
                        marshalElementProperty(it.getSubstitution(), value, it.getPrefix(), object,
                            true, doIndent);
                    }
                }
            }
        }
        if (doIndent)
        {
            if (wasIndentIncremented)
                decrementIndent();
            indent(_h, _currentIndent);
        }
        if (object == _currentCSObject)
            _currentCSObject = null;
    }

    void setSdoContext(SDOContext c)
    {
        _sdoContext = c;
    }

    // Changes the context
    void marshalAttributeProperty(PropertyXML p, Object value, String prefix,
        DataObjectXML parent/* for error reporting purposes only */)
    {
        TypeXML t = p.getTypeXML();
        String val;
        try {

        if (t.isDataType() || p.isContainment())
            val = SimpleValueHelper.getLexicalRepresentation(value, t, p.getSchemaTypeCode(),
                _h.getNamespaceStack());
        else
        {
            if (t == BuiltInTypeSystem.TYPE)
                val = getTypeReference(value);
            else
                val = getIdrefs(p, value, parent, _h.getNamespaceStack());
        }

        // p.getXMLName() can be null for "synthetic" properties,
        // which are generated as opposites of an existing "real" property
        if (val != null && p.getXMLName() != null)
            _h.attr(p.getXMLNamespaceURI(), p.getXMLName(), prefix, val);

        } catch (SimpleValueHelper.SimpleValueException e)
        {
            throw new SDOMarshalException(getMessageForSimpleValueException(e, value, t,
                p.getXMLNamespaceURI(), p.getXMLName()));
        }
    }

    void marshalElementProperty(PropertyXML p, Object value, String prefix,
        DataObjectXML parent/* for error reporting purposes only */,
        boolean recurse, boolean indent)
    {
        TypeXML t = p.getTypeXML();
        String elUri = p.getXMLNamespaceURI();
        String elName = p.getXMLName();

        if (t.isDataType() || p.isContainment())
        {
            if (value instanceof DataObject)
            {
                DataObjectXML valueXML = (DataObjectXML) value;
                TypeXML actualType = valueXML.getTypeXML();
                String xsiUri = null, xsiName = null;
                if (actualType.getTypeCode() == BuiltInTypeSystem.TYPECODE_VALUETYPE)
                {
                    // We special-case this and extract the value of the "value" property
                    marshalSimple(valueXML.get(BuiltInTypeSystem.P_VALUETYPE_VALUE), elUri, elName,
                        prefix, t, p.getSchemaTypeCode(), indent);
                    return;
                }
                if (actualType.getTypeCode() == BuiltInTypeSystem.TYPECODE_WRAPPERTYPE)
                {
                    // We may have an xsi:type already
                    PropertyXML contentProperty = (PropertyXML) valueXML.getInstanceProperty(
                        Names.SIMPLE_CONTENT_PROP_NAME);
                    TypeXML simpleValueType = contentProperty.getTypeXML();
                    Object simpleValue = valueXML.get(Names.SIMPLE_CONTENT_PROP_NAME);
                    if (simpleValueType.getTypeCode() == BuiltInTypeSystem.TYPECODE_OBJECT)
                        marshalSimple(simpleValue, elUri, elName, prefix, t, p.getSchemaTypeCode(),
                            indent);
                    else
                    {
                        String xsiTypeName = null, xsiTypeUri = null;
                        int schemaTypeCode = p.getSchemaTypeCode();
                        QName qname = getXsiTypeName(simpleValueType);
                        if (qname != null)
                        {
                            xsiTypeUri = qname.getNamespaceURI();
                            xsiTypeName = qname.getLocalPart();
                        }
                        SchemaType st = simpleValueType.getXMLSchemaType();
                        if (st != null)
                            schemaTypeCode = Common.getBuiltinTypeCode(st);
                        
                        marshalSimple(simpleValue, elUri, elName, prefix, t, schemaTypeCode,
                            indent, xsiTypeUri, xsiTypeName);
                    }
                    return;
                }
                if (actualType != t &&
                    actualType.getTypeCode() != BuiltInTypeSystem.TYPECODE_BEADATAOBJECT)
                {
                    if (t.isAssignableFrom(actualType))
                    {
                        // Check for substitutions
                        PropertyXML substitution = findMatchingSubstitution(p, actualType);
                        if (substitution != null)
                        {
                            t = substitution.getTypeXML();
                            elUri = substitution.getXMLNamespaceURI();
                            elName = substitution.getXMLName();
                        }
                        else
                        {
                            QName typeName = getXsiTypeName(actualType);
                            if (typeName != null)
                            {
                                xsiUri = typeName.getNamespaceURI();
                                xsiName = typeName.getLocalPart();
                            }
                            else if ((_options & OPT_NOEXCEPTIONS) == 0)
                                throw new SDOMarshalException(SDOError.messageForCode("marshal.xsitype.notglobal",
                                    actualType));
                        }
                    }
                    else if ((_options & OPT_NOEXCEPTIONS) == 0)
                        throw new SDOMarshalException(SDOError.messageForCode("marshal.xsitype.notassignable",
                            actualType, elName, elUri, t));
                }
                else if (p.isDynamic() && !p.isGlobal() &&
                    t.getTypeCode() != BuiltInTypeSystem.TYPECODE_DATAOBJECT)
                {
                    // In the case of on-demand open-content properties, we need to serialize
                    // an xsi:type if the property type is different than Object, DataObject or String,
                    // so the generic code doesn't work
                    QName typeName = getXsiTypeName(t);
                    if (typeName != null)
                    {
                        xsiUri = typeName.getNamespaceURI();
                        xsiName = typeName.getLocalPart();
                    }
                }
                if (indent)
                    indent(_h, _currentIndent);
                _h.startElement(elUri, elName, prefix, xsiUri, xsiName);
                // This is to support the XMLStreamReader which reuses this method
                if (recurse)
                    marshalInternal((DataObjectXML) value, t);
                _h.endElement();
            }
            else if (t == BuiltInTypeSystem.CHANGESUMMARYTYPE)
            {
                // Marshal a change summary
                if (_helper == null)
                {
                    _helper = new PlainChangeSummaryMarshaller(_optionsObject);
                    _helper.setSaver(_h);
                    _helper.setCurrentIndent(_currentIndent);
                }
                if (_currentCSObject == null)
                {
                    ChangeSummaryImpl cs = (ChangeSummaryImpl) value;
                    DataObject csParent = cs.getRootObject();
                    _currentCSObject = csParent;
                    if (indent)
                        indent(_h, _currentIndent);
                    _h.startElement(elUri, elName, prefix, null, null);
                    // Prepare the reference builder for the change summary
                    ReferenceBuilder rb = new PlainChangeSummaryMarshaller.CSReferenceBuilder(cs,
                        _referenceBuilder.getPath((DataObjectXML) csParent, null,
                            _h.getNamespaceStack()).substring(1), elUri, elName,
                        _rootUri, _rootName, _rootObject);
                    _helper.setReferenceBuilder(rb);
                    _helper.marshal(csParent, _rootUri, _rootName, false, null, null, null, null, _sdoContext);
                    _h.endElement();
                }
            }
            else if (p.isDynamic() && !p.isGlobal() &&
                t.getTypeCode() != BuiltInTypeSystem.TYPECODE_OBJECT &&
                t.getTypeCode() != BuiltInTypeSystem.TYPECODE_STRING)
            {
                Class valueClass = Common.unwrapClass(value.getClass());
                TypeXML valueSdoType = _sdoContext.getBindingSystem().getType(valueClass);
                String xsiUri = null, xsiName = null;
                if (valueSdoType != null)
                {
                    QName qname = getXsiTypeName(valueSdoType);
                    if (qname != null)
                    {
                        xsiUri = qname.getNamespaceURI();
                        xsiName = qname.getLocalPart();
                    }
                }
                marshalSimple(value, elUri, elName, prefix, xsiName == null ? t : valueSdoType,
                    p.getSchemaTypeCode(), indent, xsiUri, xsiName);
            }
            else
            {
                marshalSimple(value, elUri, elName, prefix, t, p.getSchemaTypeCode(), indent);
            }
        }
        else
        {
            String idref;
            if (t == BuiltInTypeSystem.TYPE)
            {
                idref = getTypeReference(value);

            }
            else
                idref = getIdrefs(p, value, parent, _h.getNamespaceStack());
            if (idref != null)
            {
                if (indent)
                    indent(_h, _currentIndent);
                marshalIdRef(idref, elUri, elName, prefix);
            }
        }
    }

    void marshalSimple(Object value, String uri, String name, String prefix,
        TypeXML type, int schemaTypeCode, boolean indent)
    {
        String xsiUri = null, xsiName = null;
        // The code in this "if" block is copied in the top-level marshal() method, so if this
        // changes, it need to be kept in sync
        // We only support xsi:type for simple types when the expected type is DATAOBJECT or OBJECT
        // This is for performance reasons, to avoid the call to Common.unwrapClass() every single
        // time, since in 99.9% of cases, type.getInstanceClass() will be a primitive Java type
        // whereas value.getClass() will return the "boxed" version of that type
        if ((type == BuiltInTypeSystem.DATAOBJECT || type == BuiltInTypeSystem.OBJECT) &&
            /* String is the default mapping for simple types */
            value.getClass() != String.class)
        {
            Class valueClass = Common.unwrapClass(value.getClass());
            TypeXML valueSdoType = _sdoContext.getBindingSystem().getType(valueClass);
            if (valueSdoType != null)
                if (type == BuiltInTypeSystem.DATAOBJECT ||
                    type.isAssignableFrom(valueSdoType))
                {
                    QName qname = getXsiTypeName(valueSdoType);
                    if (qname != null)
                    {
                        xsiUri = qname.getNamespaceURI();
                        xsiName = qname.getLocalPart();
                    }
                    SchemaType st = valueSdoType.getXMLSchemaType();
                    if (st != null)
                        schemaTypeCode = Common.getBuiltinTypeCode(st);
                    type = valueSdoType;
                }
                else if ((_options & OPT_NOEXCEPTIONS) == 0)
                    throw new SDOMarshalException(SDOError.messageForCode("marshal.xsitype.notassignable",
                        valueSdoType, name, uri, type));
        }
        marshalSimple(value, uri, name, prefix, type, schemaTypeCode, indent, xsiUri, xsiName);
    }

    private void marshalSimple(Object value, String uri, String name, String prefix, TypeXML type,
        int schemaTypeCode, boolean indent, String xsiUri, String xsiName)
    {
        if (indent)
            indent(_h, _currentIndent);
        _h.startElement(uri, name, prefix, xsiUri, xsiName);
        try {
            String s = SimpleValueHelper.getLexicalRepresentation(value, type, schemaTypeCode,
                _h.getNamespaceStack());
            _h.text(s);
        } catch (SimpleValueHelper.SimpleValueException e)
        {
            if ((_options & OPT_NOEXCEPTIONS) == 0)
                throw new SDOMarshalException(getMessageForSimpleValueException(e, value, type, uri,
                    name));
        }
        _h.endElement();
    }

    void marshalIdRef(String s, String uri, String name, String prefix)
    {
        _h.startElement(uri, name, prefix, null, null);
        _h.text(s);
        _h.endElement();
    }

    void marshalXsiNil(String uri, String name, String prefix, boolean indent)
    {
        if (indent)
            indent(_h, _currentIndent);

        _h.startElement(uri, name, prefix, null, null);
        _h.sattr(SDOEventModel.ATTR_XSI, Names.XSI_NIL, Names.TRUE);
        _h.endElement();
    }

    void setCurrentIndent(int indent)
    {
        _currentIndent = indent;
    }

    static String getMessageForSimpleValueException(SimpleValueHelper.SimpleValueException e,
        Object value, TypeXML type, String uri, String name)
    {
        switch (e.cause())
        {
            case SimpleValueHelper.MARSHAL_WRONGINSTANCECLASS:
                return SDOError.messageForCode("marshal.wronginstanceclass", value.toString(),
                    type, name, uri, value.getClass().getName(), type.getInstanceClass());
            default:
                throw new IllegalStateException();
        }
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

    private static PropertyXML findMatchingSubstitution(PropertyXML p, TypeXML actualType)
    {
        PropertyXML[] candidates = p.getAcceptedSubstitutions();
        if (candidates != null)
            for (PropertyXML prop : candidates)
            {
                if (prop.getTypeXML() == actualType && prop.isXMLElement())
                    return prop;
            }
        return null;
    }

    private String getTypeReference(Object value)
    {
        // We need to treat this specially, two reasons
        // - the value can be either the DataObject representation of a type or a javax.sdo.Type
        // - the ID of the type is composed of two properties (uri and name), concatenated
        String idref;
        if (value instanceof TypeXML)
        {
            TypeXML typeReference = (TypeXML) value;
            idref = typeReference.getURI() + '#' + typeReference.getName();
        }
        else // assume DataObject
        {
            DataObject typeValue = (DataObject) value;
            idref = typeValue.getString(4) + '#' + typeValue.getString(3);
        }
        return idref;
    }

    private QName getXsiTypeName(TypeXML sdoType)
    {
        QName result = sdoType.getXMLSchemaTypeName();
        if (result == null)
            result = new QName(sdoType.getURI(), sdoType.getName());
        return result;
    }

    private String getIdrefs(PropertyXML p, Object value,
        DataObjectXML parent/* for error reporting purposes only */, NamespaceStack nsstck)
    {
        if (p.isMany() && value instanceof List/*this is necessary because we may
        have lists of elements of reference types and those are serialized separately*/)
        {
            StringBuilder sb = new StringBuilder();
            for (Object object : (List) value)
            {
                DataObjectXML dataObject = (DataObjectXML) object;
                String idref = getIdref(p, dataObject, parent, nsstck);
                // We ignore objects without an id associated to them
                // We could log a warning here
                if (idref != null)
                    sb.append(idref).append(' ');
            }
            if (sb.length() > 0)
                sb.deleteCharAt(sb.length() - 1);
            return sb.toString();
        }
        else
            return getIdref(p, (DataObjectXML) value, parent, nsstck);
    }

    private String getIdref(PropertyXML p, DataObjectXML object, DataObjectXML parent,
        NamespaceStack nsstck)
    {
        if (!sameTree(object))
            if ((_options & OPT_NOEXCEPTIONS) == 0)
                throw new SDOMarshalException(SDOError.messageForCode("marshal.reference.notintree",
                    p.getName(), _referenceBuilder.getPathOrId(parent, null, nsstck)));
            else
                return null;
        if (p.getSchemaTypeCode() == SchemaType.BTC_IDREF ||
            p.getSchemaTypeCode() == SchemaType.BTC_IDREFS)
        {
            // Search for the object's id
            return _referenceBuilder.getId(object);
        }
        else
        {
            return _referenceBuilder.getPath(object, null, nsstck);
        }
    }

    private boolean sameTree(DataObject object)
    {
        return _rootObject == null /* not marshalling a tree */ ||
            object.getRootObject() == _rootObject.getRootObject();
    }

    private PropertyXML getPropertyByName(SDOContext sdoctx, String uri, String name)
    {
        PropertyXML result = PropertyImpl.getPropertyXML(sdoctx.getXSDHelper().getGlobalProperty(uri, name, true));
        if (result == null)
            result = PropertyImpl.getPropertyXML(sdoctx.getTypeHelper().getOpenContentProperty(uri, name)); 
        return result;
    }

    // ======================================
    // ReferenceBuilder implementation
    // ======================================
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
     * This method is similar to the one used for ChangeSummary serialization
     * with the exception that here we don't have to deal with deleted objects
     */
    public String getPath(DataObjectXML node, DataObject contextNode, NamespaceStack nsstck)
    {
        return Names.FRAGMENT + XmlPath.getPathForObject(node, _rootObject, _rootUri, _rootName, nsstck);
    }
}
