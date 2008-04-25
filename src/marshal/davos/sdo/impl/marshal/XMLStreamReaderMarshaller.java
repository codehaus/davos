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
import davos.sdo.Options;
import davos.sdo.PropertyXML;
import davos.sdo.SDOContext;
import davos.sdo.SDOError;
import davos.sdo.SDOMarshalException;
import davos.sdo.SequenceXML;
import davos.sdo.TypeXML;
import davos.sdo.impl.common.Common;
import davos.sdo.impl.common.Names;
import davos.sdo.impl.common.NamespaceStack;
import davos.sdo.impl.data.ChangeSummaryImpl;
import davos.sdo.impl.data.ChangeSummaryImpl.Change;
import davos.sdo.impl.type.BuiltInTypeSystem;
import davos.sdo.impl.type.SimpleValueHelper;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.Location;
import javax.xml.namespace.QName;
import javax.xml.namespace.NamespaceContext;

import org.apache.xmlbeans.SchemaType;

import javax.sdo.DataObject;

import java.util.Iterator;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Stack;

/**
 * User: radup
 * Date: Mar 13, 2007
 */
public class XMLStreamReaderMarshaller extends Saver implements XMLStreamReader
{
    private static final int OPT_NOEXCEPTIONS = 1;
    private static final int OPT_PRETTY_PRINT = 1<<1;
            static final String CDATA = "CDATA";
            static final Location LOCATION = new LocationImpl();
    private int _options;
    private int _indentStep = Marshaller.DEFAULT_INDENT_STEP;
    private PlainMarshaller _marshaller;

    private SDOContext _sdoContext;
    private String _xmlVersion;
    private String _xmlEncoding;
    private String _schemaLocation;
    private String _noNSSchemaLocation;
    private String _rootElementUri;
    private String _rootElementName;
    private DataObjectXML _rootObject;
    private int _state;
    private boolean _more;
    private int _currentIndent = -1;
    private Stack<Context> _stack = new Stack<Context>();
    private NamespaceStack _nsstack = new NamespaceStack(null, false);
    private ChangeSummaryMarshaller _csmarshaller; 

    // Delegation support
    /**
     * The way it works is that if the delegator is set, as long as it still has events
     * (hasNext returns true) events are returned from the delegator
     * In order to ensure well-formedness, the delegator has to begin with a
     * START_ELEMENT event and end with an END_ELEMENT
     */
    private boolean _hasDelegate;
    private XMLStreamReader _delegate;
    // This acts like a stack with a max of 2 levels
    private XMLStreamReader _delegate2;

    private static class Context
    {
        private String _elementName;
        private String _elementUri;
        private String _elementPrefix;
        private List<String> _attributeNames = new ArrayList<String>(5);
        private List<String> _attributeUris = new ArrayList<String>(5);
        private List<String> _attributePrefixes = new ArrayList<String>(5);
        private List<String> _attributeValues = new ArrayList<String>(5);
        private SequenceXML _sequence;
        private int _seqPosition;
        private ListXMLIterator _xmliterator;
        private DataObjectXML _dataObject;
        private String _textValue;
        private boolean _wasIndentIncremented;
        private boolean _hadElements;
        private boolean _insideChangeSummary;
    }

    private String _text;
    // Provides easier access to the top of the Context stack
    private Context _context;
    // If set, then the current CHARACTERS event is just indentation whitespace
    // and _nextState contains the next state
    private boolean _indentFlag;
    private int _nextState;

    public XMLStreamReaderMarshaller(Object options)
    {
        _marshaller = new PlainMarshaller(null);
        _marshaller.setSaver(this);
        // We don't need anything special as far as XPath building
        _marshaller.setReferenceBuilder(_marshaller);
        // We don't use the options right now, we will use the options passed to save() instead
    }

    public void save(DataObject rootObject,
            String rootElementURI, String rootElementName,
            boolean xmlDecl, String xmlVersion, String encoding,
            String schemaLocation, String noNSSchemaLocation, Object options,
            SDOContext sdoctx)
    {
        if (xmlDecl)
        {
            _xmlVersion = xmlVersion;
            _xmlEncoding = encoding;
        }
        _schemaLocation = schemaLocation;
        _noNSSchemaLocation = noNSSchemaLocation;
        _sdoContext = sdoctx;
        _marshaller.setSdoContext(sdoctx);
        if (rootElementName == null || rootElementName.length() == 0)
        {
            rootElementName = Names.SDO_DATAOBJECT;
            rootElementURI = Common.EMPTY_STRING;
        }
        else
        {
            _rootElementUri = rootElementURI;
            _rootElementName = rootElementName;
        }
        _state = START_DOCUMENT;
        if (!(rootObject instanceof DataObjectXML))
            throw new IllegalArgumentException("This marshaller can only handle XML-specific implementations");
        _rootObject = (DataObjectXML) rootObject;
        _more = true;
        _csmarshaller = new ChangeSummaryMarshaller(options, _nsstack);
        _marshaller._helper = _csmarshaller;
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
        if ((_options & OPT_PRETTY_PRINT) != 0)
            _currentIndent = Marshaller.NEWLINE.length();
    }

    // ==========================================
    // XMLStreamReader implementation
    // ==========================================
    // In theory, the next 5 methods are only allowed in the START_DOCUMENT state, but it's
    // probably ok to skip that check
    public String getVersion()
    {
        return _xmlVersion;
    }

    public boolean isStandalone()
    {
        return false; // This is the default in XML
    }

    public boolean standaloneSet()
    {
        return false;
    }

    public String getCharacterEncodingScheme()
    {
        return _xmlEncoding;
    }

    public String getEncoding()
    {
        return null; // No encoding was used since this doc does not come from XML text
    }

    public Object getProperty(String string) throws IllegalArgumentException
    {
        return null;  // Not sure what properties should be supported, the JavaDoc only
        // makes reference to notation and entities properties which are not applicable here
    }

    public int next() throws XMLStreamException
    {
        if (_hasDelegate)
        {
            if (_delegate.hasNext())
            {
                int e = _delegate.next();
                if (e != END_DOCUMENT)
                    return e;
            }
            else
            {
                // There is a problem here: since the delegate ends on an END_ELEMENT, there is no
                // chance for it to pop the namespace stack, so it must be popped here
                // In order to keep things "simple" we use one and only namespace stack, this is
                // why we need to keep it right
                _nsstack.popMappings();
            }
            // Pop the "stack"
            _delegate = _delegate2;
            _delegate2 = null;
            if (_delegate != null)
            {
                if (_delegate.hasNext())
                {
                    int e = _delegate.next();
                    if (e != END_DOCUMENT)
                        return e;
                }
                else
                {
                    _nsstack.popMappings();
                }
            }
            _delegate = null;
            _hasDelegate = false;
            // If there is a change summary element on top of the stack, give it a
            // chance to provide the next element before going to the END_ELEMENT state
            // for the change summary
            // This is done here to take advantage of the fact that only change summary
            // elements add stuff on top of the stack and will need to change if that stops
            // being the case
            if (!_context._insideChangeSummary)
                throw new IllegalStateException("Only the ChangeSummaryMarshaller can set the delegate");
            return _csmarshaller.nextEvent();
        }
        Context c;
        DataObjectXML parent;
        switch (_state)
        {
            case START_DOCUMENT:
                DataObjectXML rootObjectXML = _rootObject;
                PropertyXML rootProp = (PropertyXML) _sdoContext.getXSDHelper().
                    getGlobalProperty(_rootElementUri, _rootElementName, true);
                TypeXML type = rootProp == null ? null : rootProp.getTypeXML();
                TypeXML actualType = rootObjectXML.getTypeXML();

                String prefix = _rootElementUri == Names.URI_SDO ? Names.PREFIX_SDO : null;

                String xsiUri = null, xsiName = null;
                if (type != null && actualType != type && actualType.getTypeCode() !=
                    BuiltInTypeSystem.TYPECODE_BEADATAOBJECT)
                {
                    if (type.isAssignableFrom(actualType))
                    {
                        QName typeName = actualType.getXMLSchemaTypeName();
                        if (typeName != null)
                        {
                            xsiUri = typeName.getNamespaceURI();
                            xsiName = typeName.getLocalPart();
                        }
                        else if ((_options & OPT_NOEXCEPTIONS) == 0)
                            throw new SDOMarshalException(SDOError.messageForCode(
                                "marshal.xsitype.notglobal", actualType));
                    }
                    else if ((_options & OPT_NOEXCEPTIONS) == 0)
                        throw new SDOMarshalException(SDOError.messageForCode(
                            "marshal.xsitype.notassignable", actualType, _rootElementName,
                            _rootElementUri, type));
                }

                startElement(_rootElementUri, _rootElementName, prefix, xsiUri, xsiName);
                c = _context;

                // Add the schemaLocation attributes
                if (_schemaLocation != null)
                {
                    attr(
                        Names.URI_XSD_INSTANCE,
                        SDOEventModel.SCHEMA_LOCATION,
                        Names.PREFIX_XSD_INSTANCE,
                        _schemaLocation);
                }
                if (_noNSSchemaLocation != null)
                {
                    attr(
                        Names.URI_XSD_INSTANCE,
                        SDOEventModel.NO_NAMESCAPCE_SCHEMA_LOCATION,
                        Names.PREFIX_XSD_INSTANCE,
                        _noNSSchemaLocation);
                }

                // Process the rest of the attributes
                addAttributes(c, rootObjectXML);

                // Save the current object to the context
                c._dataObject = rootObjectXML;

                // Everything is ready, move the state to START_ELEMENT
                return _state = START_ELEMENT;
            case START_ELEMENT:
                c =  _context;
                if (c._insideChangeSummary)
                    return _csmarshaller.nextEvent();
                parent = c._dataObject;
                if (parent == null)
                {
                    if (c._textValue != null)
                    {
                        _text = c._textValue;
                        return _state = CHARACTERS;
                    }
                    else
                    {
                        return _state = END_ELEMENT;
                    }
                }
                else if (parent.getSequence() != null)
                {
                    if (_currentIndent >= 0)
                        c._wasIndentIncremented = !parent.getTypeXML().isMixedContent() &&
                            incrementIndent();
                    c._sequence = parent.getSequenceXML();
                    c._seqPosition = -1;
                }
                else
                {
                    if (_currentIndent >= 0)
                        c._wasIndentIncremented = incrementIndent();
                    c._xmliterator = parent.getUnsequencedXML();
                }
                // All the attributes are processed at this point so focus only on text
                // and element events
                return moveToNextTextOrStartElement(c, parent);
            case CHARACTERS:
                if (_indentFlag)
                {
                    _indentFlag = false;
                    if (_delegate != null)
                        _hasDelegate = true;
                    return _state = _nextState;
                }
                c = _context;
                parent = c._dataObject;
                if (parent == null)
                {
                    // Move to END_ELEMENT directly
                    return _state = END_ELEMENT;
                }
                else
                {
                    return moveToNextTextOrStartElement(c, parent);
                }
            case END_ELEMENT:
                // We need to pop the context and continue advancing in the parent context
                // If there is no parent context, move to END_DOCUMENT state
                _nsstack.popMappings();
                _stack.pop();
                if (_stack.empty())
                {
                    _more = false;
                    return _state = END_DOCUMENT;
                }
                else
                {
                    _context = _stack.peek();
                    parent = _context._dataObject;
                    if (parent == null)
                        throw new IllegalStateException("parent can't be null since it had children elements");
                    return moveToNextTextOrStartElement(_context, parent);
                }
            case END_DOCUMENT:
                throw new NoSuchElementException();
        }
        throw new IllegalStateException();
    }

    public int nextTag() throws XMLStreamException
    {
        if (_hasDelegate)
        {
            if (_delegate.hasNext())
                // Since the last event of the delegator is always END_ELEMENT, if there are
                // more events it means that there also are more non-text events
                return _delegate.nextTag();
            // Otherwise, fall through and wait for the code in next() to do the right thing
            // and set _hasDelegate to false
        }
        int savedCurrentIndent = _currentIndent;
        _currentIndent = -1;
        while (true)
        {
            int eventType = next();
            switch (eventType)
            {
            case START_ELEMENT:
            case END_ELEMENT:
            case END_DOCUMENT:
                _currentIndent = savedCurrentIndent;
                return eventType;
            case CHARACTERS:
                if (!isWhiteSpace())
                {
                    _currentIndent = savedCurrentIndent;
                    return eventType;
                }
                break;
            default:
                throw new IllegalStateException();
            }
        }
    }

    public boolean hasNext() throws XMLStreamException
    {
        return _more;
    }

    public void require(int i, String namespaceURI, String localName) throws XMLStreamException
    {
        if (_hasDelegate)
            _delegate.require(i, namespaceURI, localName);
        if (i == _state)
        {
            if (_state == START_ELEMENT || _state == END_ELEMENT)
            {
                if (namespaceURI != null && !namespaceURI.equals(_context._elementUri))
                    throw new XMLStreamException("Requested namespace URI was \"" +
                        namespaceURI + "\" but the actual namespace URI is \"" +
                        _context._elementUri);
                if (localName != null && !localName.equals(_context._elementName))
                    throw new XMLStreamException("Requested local name was \"" +
                        localName + "\" but the actual local name URI is \"" +
                        _context._elementName);
            }
        }
        else
            throw new XMLStreamException("Requested event was " + eventConstantToString(i) +
                " but the current event is " + eventConstantToString(_state));
    }

    public String getElementText() throws XMLStreamException
    {
        if (_hasDelegate)
            // Again, because the delegate ends in an END_ELEMENT event, it is safe to delegate
            // the whole job to it: if the current event is within the scope of the delegate,
            // then the corresponding end element event is too
            return _delegate.getElementText();
        if (_state != START_ELEMENT)
            throw new XMLStreamException("Current state (" + eventConstantToString(_state) +
                ") is not START_ELEMENT");
        // If we know this is a text-only element, perfect
        if (_context._textValue  != null)
        {
            _state = END_ELEMENT;
            return _context._textValue;
        }
        // If this is a null DataObject, then the text is ""
        if (_context._dataObject == null)
        {
            _state = END_ELEMENT;
            return Common.EMPTY_STRING;
        }
        DataObjectXML parent = _context._dataObject;
        if (parent.getSequence() != null)
        {
            // If this is a sequenced DataObject, we can have a sequence with just
            // text properties inside
            SequenceXML s = parent.getSequenceXML();
            int size = s.size();
            if (size == 0)
            {
                _state = END_ELEMENT;
                return Common.EMPTY_STRING;
            }
            else if (size == 1)
            {
                PropertyXML p = s.getPropertyXML(0); 
                if (p == null)
                {
                    // Only one text property in the sequence
                    _state = END_ELEMENT;
                    return (String) s.getValue(0);
                }
                else if (p.isXMLElement())
                    throw new XMLStreamException("Current element (" + _context._elementName +
                        '@' + _context._elementUri + ") has element content");
                else
                {
                    _state = END_ELEMENT;
                    // Only one property, and that, an attribute
                    return Common.EMPTY_STRING;
                }
            }
            else
            {
                StringBuilder result = new StringBuilder();
                for (int i = 0; i < size; i++)
                {
                    PropertyXML p = s.getPropertyXML(i);
                    if (p == null)
                        result.append(s.getValue(i));
                    else if (p.isXMLElement())
                        throw new XMLStreamException("Current element (" + _context._elementName +
                            '@' + _context._elementUri + ") has children elements");
                    else
                        ; // Attribute
                }
                _state = END_ELEMENT;
                return result.toString();
            }
        }
        else
        {
            // If this is a non-sequenced type, check if it is a simple type, and if it is,
            // get the marshalled value of the simple content property
            boolean simpleContent = parent.getTypeXML().isSimpleContent();
            if (simpleContent)
            {
                PropertyXML p = (PropertyXML) parent.getInstanceProperty(Names.SIMPLE_CONTENT_PROP_NAME);
                Object val = parent.get(p);
                _state = END_ELEMENT;
                if (val == null)
                    return Common.EMPTY_STRING;
                else
                {
                    try
                    {
                        return SimpleValueHelper.getLexicalRepresentation(val, p.getTypeXML(),
                            p.getSchemaTypeCode(), _nsstack);
                    }
                    catch (SimpleValueHelper.SimpleValueException e)
                    {
                        if ((_options & OPT_NOEXCEPTIONS) == 0)
                            throw new SDOMarshalException(PlainMarshaller.
                                getMessageForSimpleValueException(e, val, p.getTypeXML(),
                                p.getXMLNamespaceURI(), p.getXMLName()));
                        else
                            return p.getDefault() == null ? Common.EMPTY_STRING :
                                p.getDefault().toString();
                    }
                }
            }
            else
            {
                // This does not have text content, but there is still a chance that there
                // aren't any element properties or that all element properties are null
                ListXMLIterator it = parent.getUnsequencedXML();
                while (it.next())
                {
                    PropertyXML p = it.getPropertyXML();
                    if (p.isXMLElement() && it.getValue() != null)
                        throw new XMLStreamException("Current element (" + _context._elementName +
                            '@' + _context._elementUri + ") has children elements");
                }
                _state = END_ELEMENT;
                return Common.EMPTY_STRING;
            }
        }
    }

    public QName getName()
    {
        if (_hasDelegate)
            return _delegate.getName();
        if (_state != START_ELEMENT && _state != END_ELEMENT)
            throw new IllegalStateException("Current state (" + eventConstantToString(_state) +
                ") is not START_ELEMENT or END_ELEMENT");
        return new QName(_context._elementUri, _context._elementName, _context._elementPrefix);
    }

    public String getLocalName()
    {
        if (_hasDelegate)
            return _delegate.getLocalName();
        if (_state != START_ELEMENT && _state != END_ELEMENT)
            throw new IllegalStateException("Current state (" + eventConstantToString(_state) +
                ") is not START_ELEMENT or END_ELEMENT");
        return _context._elementName;
    }

    public boolean hasName()
    {
        if (_hasDelegate)
            return _delegate.hasName();
        return _state == START_ELEMENT || _state == END_ELEMENT;
    }

    public String getNamespaceURI()
    {
        if (_hasDelegate)
            return _delegate.getNamespaceURI();
        if (_state != START_ELEMENT && _state != END_ELEMENT)
            return null;
        return _context._elementUri;
    }

    public String getPrefix()
    {
        if (_hasDelegate)
            return _delegate.getPrefix();
        if (_state != START_ELEMENT && _state != END_ELEMENT)
            return null;
        return _context._elementPrefix;
    }

    public int getEventType()
    {
        if (_hasDelegate)
            return _delegate.getEventType();
        return _state;
    }

    public boolean isStartElement()
    {
        if (_hasDelegate)
            return _delegate.isStartElement();
        return _state == START_ELEMENT;
    }

    public boolean isEndElement()
    {
        if (_hasDelegate)
            return _delegate.isEndElement();
        return _state == END_ELEMENT;
    }

    public boolean isCharacters()
    {
        if (_hasDelegate)
            return _delegate.isCharacters();
        return _state == CHARACTERS;
    }

    public boolean isWhiteSpace()
    {
        if (_hasDelegate)
            return _delegate.isWhiteSpace();
        if (_state != CHARACTERS)
            return false;
        if (_indentFlag)
            return true; // This is an indentation event
        for (int i = 0; i < _text.length(); i++)
            switch (_text.charAt(i))
            {
            case '\r':
            case '\n':
            case ' ':
            case '\t':
                break;
            default:
                return false;
            }
        return true;
    }

    public String getAttributeValue(String namespaceURI, String localName)
    {
        if (_hasDelegate)
            return _delegate.getAttributeValue(namespaceURI, localName);
        if (_state != START_ELEMENT)
            throw new IllegalStateException("Current state (" + eventConstantToString(_state) +
            ") is not START_ELEMENT");
        // This is not optimized
        if (namespaceURI == null)
        {
            for (int i = 0; i < _context._attributeNames.size(); i++)
                if (localName.equals(_context._attributeNames.get(i)))
                    return _context._attributeValues.get(i);
        }
        else
        {
            for (int i = 0; i < _context._attributeNames.size(); i++)
                if (localName.equals(_context._attributeNames.get(i)) &&
                        namespaceURI.equals(_context._attributeUris.get(i)))
                    return _context._attributeValues.get(i);
        }
        return null;
    }

    public int getAttributeCount()
    {
        if (_hasDelegate)
            return _delegate.getAttributeCount();
        if (_state != START_ELEMENT)
            throw new IllegalStateException("Current state (" + eventConstantToString(_state) +
            ") is not START_ELEMENT");
        return _context._attributeNames.size();
    }

    public QName getAttributeName(int i)
    {
        if (_hasDelegate)
            return _delegate.getAttributeName(i);
        if (_state != START_ELEMENT)
            throw new IllegalStateException("Current state (" + eventConstantToString(_state) +
            ") is not START_ELEMENT");
        if (i >= _context._attributeNames.size())
            return null;
        return new QName(_context._attributeUris.get(i), _context._attributeNames.get(i),
            _context._attributeValues.get(i));
    }

    public String getAttributeNamespace(int i)
    {
        if (_hasDelegate)
            return _delegate.getAttributeNamespace(i);
        if (_state != START_ELEMENT)
            throw new IllegalStateException("Current state (" + eventConstantToString(_state) +
            ") is not START_ELEMENT");
        if (i >= _context._attributeUris.size())
            return null;
        return _context._attributeUris.get(i);
    }

    public String getAttributeLocalName(int i)
    {
        if (_hasDelegate)
            return _delegate.getAttributeLocalName(i);
        if (_state != START_ELEMENT)
            throw new IllegalStateException("Current state (" + eventConstantToString(_state) +
            ") is not START_ELEMENT");
        if (i >= _context._attributeNames.size())
            return null;
        return _context._attributeNames.get(i);
    }

    public String getAttributePrefix(int i)
    {
        if (_hasDelegate)
            return _delegate.getAttributePrefix(i);
        if (_state != START_ELEMENT)
            throw new IllegalStateException("Current state (" + eventConstantToString(_state) +
            ") is not START_ELEMENT");
        if (i >= _context._attributePrefixes.size())
            return null;
        return _context._attributePrefixes.get(i);
    }

    public String getAttributeType(int i)
    {
        if (_hasDelegate)
            return _delegate.getAttributeType(i);
        if (_state != START_ELEMENT)
            throw new IllegalStateException("Current state (" + eventConstantToString(_state) +
            ") is not START_ELEMENT");
        if (i >= _context._attributeNames.size())
            return null;
        // We don't generate DTDs, so all the types are CDATA
        return CDATA;
    }

    public String getAttributeValue(int i)
    {
        if (_hasDelegate)
            return _delegate.getAttributeValue(i);
        if (_state != START_ELEMENT)
            throw new IllegalStateException("Current state (" + eventConstantToString(_state) +
            ") is not START_ELEMENT");
        if (i >= _context._attributeValues.size())
            return null;
        return _context._attributeValues.get(i);
    }

    public boolean isAttributeSpecified(int i)
    {
        if (_hasDelegate)
            return _delegate.isAttributeSpecified(i);
        if (_state != START_ELEMENT)
            throw new IllegalStateException("Current state (" + eventConstantToString(_state) +
            ") is not START_ELEMENT");
        if (i >= _context._attributeValues.size())
            return false;
        // Since we don't generate DTDs, we don't generate defaulted attributes
        // If an attribute is not set, it's not generated at all
        return true;
    }

    public int getNamespaceCount()
    {
        if (_hasDelegate)
            return _delegate.getNamespaceCount();
        if (_state != START_ELEMENT && _state != END_ELEMENT)
            throw new IllegalStateException("Current state (" + eventConstantToString(_state) +
                ") is not START_ELEMENT or END_ELEMENT");
        int i = 0;
        for (_nsstack.iterateMappings(); _nsstack.hasMapping(); _nsstack.nextMapping(), i++);
        return i;
    }

    public String getNamespacePrefix(int index)
    {
        if (_hasDelegate)
            return _delegate.getNamespacePrefix(index);
        if (_state != START_ELEMENT && _state != END_ELEMENT)
            throw new IllegalStateException("Current state (" + eventConstantToString(_state) +
                ") is not START_ELEMENT or END_ELEMENT");
        // Unfortunately, _nsstack only offers an iterator model of going though prefixes
        int i = 0;
        for (_nsstack.iterateMappings(); _nsstack.hasMapping() && i < index; _nsstack.nextMapping(), i++);
        if (i == index)
            return _nsstack.mappingPrefix();
        else
            return null;
    }

    public String getNamespaceURI(int index)
    {
        if (_hasDelegate)
            return _delegate.getNamespaceURI(index);
        if (_state != START_ELEMENT && _state != END_ELEMENT)
            throw new IllegalStateException("Current state (" + eventConstantToString(_state) +
                ") is not START_ELEMENT or END_ELEMENT");
        // Unfortunately, _nsstack only offers an iterator model of going though prefixes
        int i = 0;
        for (_nsstack.iterateMappings(); _nsstack.hasMapping() && i < index; _nsstack.nextMapping(), i++);
        if (i == index)
            return _nsstack.mappingUri();
        else
            return null;
    }

    public NamespaceContext getNamespaceContext()
    {
        return _nsstack;
    }

    public String getNamespaceURI(String prefix)
    {
        if (prefix == null)
            throw new IllegalArgumentException("The prefix must not be null");
        return _nsstack.getNamespaceForPrefix(prefix);
    }

    public boolean hasText()
    {
        if (_hasDelegate)
            return _delegate.hasText();
        return _state == CHARACTERS; // This is the only type of text event that we generate
    }

    public String getText()
    {
        if (_hasDelegate)
            return _delegate.getText();
        if (_state != CHARACTERS)
            throw new IllegalStateException("Current state (" + eventConstantToString(_state) +
                ") is not CHARACTERS");
        if (_indentFlag)
            return new String(Marshaller.INDENT, 0, _currentIndent);
        else
            return _text;
    }

    public char[] getTextCharacters()
    {
        if (_hasDelegate)
            return _delegate.getTextCharacters();
        if (_state != CHARACTERS)
            throw new IllegalStateException("Current state (" + eventConstantToString(_state) +
                ") is not CHARACTERS");
        if (_indentFlag)
            return Marshaller.INDENT;
        else
            return _text.toCharArray();
    }

    public int getTextStart()
    {
        if (_hasDelegate)
            return _delegate.getTextStart();
        if (_state != CHARACTERS)
            throw new IllegalStateException("Current state (" + eventConstantToString(_state) +
                ") is not CHARACTERS");
        return 0;
    }

    public int getTextLength()
    {
        if (_hasDelegate)
            return _delegate.getTextLength();
        if (_state != CHARACTERS)
            throw new IllegalStateException("Current state (" + eventConstantToString(_state) +
                ") is not CHARACTERS");
        if (_indentFlag)
            return _currentIndent;
        else
            return _text.length();
    }

    public int getTextCharacters(int sourceStart, char[] target, int targetStart, int length) throws XMLStreamException
    {
        if (_hasDelegate)
            return _delegate.getTextCharacters(sourceStart, target, targetStart, length);
        if (_state != CHARACTERS)
            throw new IllegalStateException("Current state (" + eventConstantToString(_state) +
                ") is not CHARACTERS");
        if (target == null)
            throw new NullPointerException("The \"target\" argument cannot be null");
        if (targetStart < 0 || targetStart >= target.length)
            throw new IllegalArgumentException("The \"targetStart\" argument is out of range");
        if (length < 0 || length + targetStart > target.length)
            throw new IllegalArgumentException("The \"length\" argument is out of range");
        if (sourceStart < 0)
            sourceStart = 0; // The spec doesn't say that an exception should be thrown in this case
        if (_indentFlag)
        {
            int result;
            if (sourceStart + length > _currentIndent)
                result = _currentIndent - sourceStart;
            else
                result = length;
            System.arraycopy(Marshaller.INDENT, sourceStart, target, targetStart, result);
            return result;
        }
        else
        {
            int result;
            if (sourceStart + length > _text.length())
                result = _text.length() - sourceStart;
            else
                result = length;
            _text.getChars(sourceStart, sourceStart + result, target, targetStart);
            return result;
        }
    }

    public Location getLocation()
    {
        return LOCATION;
    }

    public String getPITarget()
    {
        if (_hasDelegate)
            return _delegate.getPITarget();
        return null;  // We don't generate processing instructions
    }

    public String getPIData()
    {
        if (_hasDelegate)
            return _delegate.getPIData();
        return null;  // We don't generate processing instructions
    }

    public void close() throws XMLStreamException
    {
        // Do some clean-up
        _sdoContext = null;
        _xmlVersion = null;
        _xmlEncoding = null;
        _schemaLocation = null;
        _noNSSchemaLocation = null;
        _rootElementUri = null;
        _rootElementName = null;
        _rootObject = null;
        _state = _nextState = 0;
        _more = false;
        _currentIndent = Marshaller.NEWLINE.length();
        _stack.clear();
        _nsstack = new NamespaceStack(null, false);
        _text = null;
        _context = null;
        _indentFlag = false;
        _options = 0;
        _currentIndent = -1;

        _hasDelegate = false;
        _delegate = null;
    }

    // ===============================================
    // Location implementation
    // ===============================================
    private static class LocationImpl implements Location
    {
        /**
         * Since this event stream comes from generated XML and not from
         * text, the location information can't really be made available
         */
        public int getCharacterOffset()
        {
            return -1;
        }

        public int getColumnNumber()
        {
            return -1;
        }

        public int getLineNumber()
        {
            return -1;
        }

        public String getPublicId()
        {
            return null;
        }

        /**
         * This is the only debatable one: should it return the locationURI on the XMLDocument?
         * I think not, because that locationURI doesn't point to the XML representation
         * of the current document (because it is not loaded from that location)
         */
        public String getSystemId()
        {
            return null;
        }
    }

    // ============================================
    // Helper methods
    // ============================================
    public void addDelegate(XMLStreamReader delegate)
    {
        _delegate2 = _delegate;
        _delegate = delegate;
    }

    private void addAttributes(Context c, DataObjectXML parent)
    {
        if (parent.getType().isSequenced())
        {
            SequenceXML s = parent.getSequenceXML();
            // Use the Sequence to populate the attributes
            int size = s.size();
            for (int i = 0; i < size; i++)
            {
                PropertyXML p = s.getPropertyXML(i);
                if (p != null && !p.isXMLElement())
                {
                    // We found an attribute, add it to the context
                    _marshaller.marshalAttributeProperty(p, s.getValue(i),
                        s.getPrefixXML(i), parent);
                }
            }
        }
        else
        {
            // One important task here is to check if there is a text child that needs
            // a namespace prefix declaration so we can add that as well
            // Also if there is a text child that is null, we need to add the xsi:nil attribute
            List props = parent.getInstanceProperties();
            int size = props.size();
            boolean simpleContent = parent.getTypeXML().isSimpleContent();
            for (int i = 0; i < size; i++)
            {
                PropertyXML p = (PropertyXML) props.get(i);
                if (!p.isXMLElement())
                {
                    Object value = parent.get(p);
                    if (simpleContent && Names.SIMPLE_CONTENT_PROP_NAME.equals(p.getName()))
                    {
                        if (value == null)
                            attr(Names.URI_XSD_INSTANCE, Names.XSI_NIL, Names.PREFIX_XSD_INSTANCE,
                                Names.TRUE);
                        else
                        {
                            // We have a typed text on our hands
                            // Check to see if it's a QName
                            if (p.getSchemaTypeCode() == SchemaType.BTC_QNAME)
                            {
                                // We need to parse the value and throw it away
                                // This will have the side-effect that it will create a prefix
                                // mapping, which is something we need for the start element
                                try {
                                SimpleValueHelper.getLexicalRepresentation(value, p.getTypeXML(),
                                    SchemaType.BTC_QNAME, _nsstack);
                                } catch (SimpleValueHelper.SimpleValueException e) {}
                            }
                        }
                    }
                    else if (value != null && parent.isSet(p))
                    {
                        ListXMLIterator it = parent.getListXMLIterator(p);
                        it.next();
                        _marshaller.marshalAttributeProperty(p, value, it.getPrefix(), parent);
                    }
                }
            }
        }
    }

    private int moveToNextTextOrStartElement(Context c, DataObjectXML parent)
    {
        boolean doIndent;
        if (c._sequence != null)
        {
            SequenceXML s = c._sequence;
            int size = s.size();
            for (int i = c._seqPosition + 1; i < size; i++)
            {
                PropertyXML p = s.getPropertyXML(i);

                if (p == null)
                {
                    c._seqPosition = i;
                    _text = (String) s.getValue(i);
                    _state = CHARACTERS;
                    return CHARACTERS;
                }
                else if (p.isXMLElement())
                {
                    Object val = s.getValue(i);
                    if (val == null)
                        marshalNilElement(s.getSubstitution(i).getXMLNamespaceURI(),
                            s.getSubstitution(i).getXMLName(), s.getPrefixXML(i));
                    else
                        _marshaller.marshalElementProperty(s.getSubstitution(i), val,
                            s.getPrefixXML(i), parent, false, false);
                    c._seqPosition = i;
                    c._hadElements = true;
                    // If necessary, save the value for the start element
                    // If the element is of a data value type, then a text event is received
                    // So conversely, if a text event was not received, it means the
                    // value is a DataObject or ChangeSummary
                    if (_context._textValue == null)
                        if (val instanceof DataObjectXML)
                        {
                            DataObjectXML dataObject = (DataObjectXML) val; 
                            _context._dataObject = dataObject;
                            if (dataObject != null)
                                addAttributes(_context, dataObject);
                        }
                        else
                            _context._insideChangeSummary = true;
                    if (_currentIndent < 0 || parent.getTypeXML().isMixedContent())
                        return _state = START_ELEMENT;
                    else
                    {
                        // Indent
                        _indentFlag = true;
                        _nextState = START_ELEMENT;
                        return _state = CHARACTERS;
                    }
                }
                // Otherwise it is an attribute; skip it because it was already processed
            }
            // End element
            doIndent = _currentIndent > 0 && !parent.getTypeXML().isMixedContent();
        }
        else
        {
            TypeXML type = parent.getTypeXML();
            boolean simpleContent = type.isSimpleContent();
            ListXMLIterator it = c._xmliterator;
            while (it.next())
            {
                PropertyXML p = it.getPropertyXML();
                assert p != null;
                if (p.isXMLElement())
                {
                    Object val = it.getValue();
                    if (val == null)
                        marshalNilElement(it.getSubstitution().getXMLNamespaceURI(),
                            it.getSubstitution().getXMLName(), it.getPrefix());
                    else
                        _marshaller.marshalElementProperty(it.getSubstitution(), it.getValue(),
                            it.getPrefix(), parent, false, false);
                    c._hadElements = true;
                    // If necessary, save the value for the start element
                    // If the element is of a data value type, then a text event is received
                    // So conversely, if a text event was not received, it means the
                    // value is a DataObject or a ChangeSummary
                    if (_context._textValue == null)
                        if (val instanceof DataObject)
                        {
                            DataObjectXML dataObject = (DataObjectXML) val; 
                            _context._dataObject = dataObject;
                            if (dataObject != null)
                                addAttributes(_context, dataObject);
                        }
                        else
                            _context._insideChangeSummary = true;
                    if (_currentIndent < 0)
                        return _state = START_ELEMENT;
                    else
                    {
                        // Indent
                        _indentFlag = true;
                        _nextState = START_ELEMENT;
                        return _state = CHARACTERS;
                    }
                }
                else if (simpleContent && Names.SIMPLE_CONTENT_PROP_NAME.equals(p.getName()))
                {
                    _text = (String) it.getValue();
                    if (_text != null)
                    {
                        return _state = CHARACTERS;
                    }
                    else
                    {
                        return _state = END_ELEMENT;
                    }
                }
                // Otherwise it is an attribute; skip it because it was already processed
            }
            // End element
            doIndent = _currentIndent > 0;
        }
        if (doIndent)
        {
            // See if we need to indent: we need to indent if there were any elements
            if (c._wasIndentIncremented)
                decrementIndent();
            if (c._hadElements)
            {
                _indentFlag = true;
                _nextState = END_ELEMENT;
                return _state = CHARACTERS;
            }
            else
                return _state = END_ELEMENT;
        }
        else
            return _state = END_ELEMENT;
    }

    private void marshalNilElement(String uri, String name, String prefix)
    {
        startElement(uri, name, prefix, null, null);
        attr(Names.URI_XSD_INSTANCE, Names.XSI_NIL, Names.PREFIX_XSD_INSTANCE,
            Names.TRUE);
    }

    private boolean incrementIndent()
    {
        int x = _currentIndent + _indentStep;
        if (x > Marshaller.MAX_INDENT)
            return false;
        _currentIndent = x;
        return true;
    }

    private void decrementIndent()
    {
        _currentIndent -= _indentStep;
    }

    static String eventConstantToString(int i)
    {
        switch (i)
        {
        case START_DOCUMENT:
            return "START_DOCUMENT";
        case START_ELEMENT:
            return "START_ELEMENT";
        case END_ELEMENT:
            return "END_ELEMENT";
        case END_DOCUMENT:
            return "END_DOCUMENT";
        case CHARACTERS:
            return "CHARACTERS";
        default:
            return String.valueOf(i);
        }
    }

    // ==================================================
    // Saver implementation
    // This class is actually a combination between a Marshaller and a Saver, but in order to
    // reuse code from Marshallers it will delegate to them, setting itself up as
    // a Saver that the respective Marshaller will use
    // ==================================================
    NamespaceStack getNamespaceStack()
    {
        return _nsstack;
    }

    public void attr(String uri, String local, String prefix, String value)
    {
        _context._attributeUris.add(uri == null ? Common.EMPTY_STRING : uri);
        _context._attributeNames.add(local);
        _context._attributePrefixes.add(_nsstack.ensureMapping(uri, prefix, false, true));
        _context._attributeValues.add(value);
    }

    public void sattr(int type, String name, String value)
    {
        throw new IllegalStateException();
    }

    /**
     * Creates a new Context, pushes it and initializes it with basic information
     */
    public void startElement(String uri, String name, String prefix, String xsiTypeUri, String xsiTypeName)
    {
        _nsstack.pushMappings(false);
        Context c = new Context();
        c._elementUri = uri == null ? Common.EMPTY_STRING : uri;
        c._elementName = name;
        c._elementPrefix = _nsstack.ensureMapping(uri, prefix, false, true);

        _stack.push(c);
        _context = c;

        // Handle xsi:type
        if (xsiTypeName != null)
        {
            String xsiPrefix = _nsstack.ensureMapping(xsiTypeUri, null, false, false);
            attr(
                Names.URI_XSD_INSTANCE,
                Names.XSI_TYPE,
                Names.PREFIX_XSD_INSTANCE,
                xsiPrefix == null ? xsiTypeName : xsiPrefix + ':' + xsiTypeName);
        }
    }

    public void endElement()
    {
        // This must do nothing because in general it will be called when
        // we are preparing a START_ELEMENT
    }

    public void text(char[] buff, int off, int cch)
    {
        throw new IllegalStateException();
    }

    public void text(String s)
    {
        _context._textValue = s;
    }

    public void xmlDecl(String version, String encoding)
    {
        throw new IllegalStateException();
    }

    public void xmlns(String prefix, String uri)
    {
        throw new IllegalStateException();
    }

    // =======================================================
    // ChangeSummary handling
    // =======================================================
    private class ChangeSummaryMarshaller extends PlainChangeSummaryMarshaller
        implements DataObjectMarshaller
    {
        XMLStreamReaderRecorder _recorder;
        Iterator<Map.Entry<DataObject, Change[]>> _iterator;
        DataObject _rootObject;
        String _rootURI;
        String _rootName;
        boolean _hadModifiedObjects;
        boolean _wasIndentIncremented;
        XMLStreamReaderMarshaller _helper;

        public ChangeSummaryMarshaller(Object options, NamespaceStack nsstack)
        {
            super(options);
            _recorder = new XMLStreamReaderRecorder(nsstack, this);
            _helper = new XMLStreamReaderMarshaller(null);
        }

        void marshal(DataObject rootObject, String rootUri, String rootName, boolean xmlDecl, String xmlVersion, String encoding, String schemaLocation, String noNSSchemaLocation, SDOContext sdoContext)
        {
            ChangeSummaryImpl cs = (ChangeSummaryImpl) rootObject.getChangeSummary();

            _helper._marshaller.setSdoContext(sdoContext);
            // Record attributes in the current XMLStreamReader
            setSaver(XMLStreamReaderMarshaller.this);
            marshalChangeSummaryAttributes(rootObject, cs);

            Map<DataObject, Change[]> modifiedObjects = cs.getModifiedObjects();
            _iterator = modifiedObjects.entrySet().iterator();
            _rootObject = rootObject;
            _rootURI = rootUri;
            _rootName = rootName;
            _hadModifiedObjects = !modifiedObjects.isEmpty();
            setSaver(_recorder);
        }

        int nextEvent()
        {
            if (_iterator.hasNext())
            {
                Map.Entry<DataObject, Change[]> entry = _iterator.next();
                // Swap out contexts
                _hasDelegate = true;
                addDelegate(_recorder);
                try
                { _recorder.close(); }
                catch (XMLStreamException e)
                {}

                if (XMLStreamReaderMarshaller.this._currentIndent > 0)
                {
                    _wasIndentIncremented = incrementIndent();
                    setCurrentIndent(XMLStreamReaderMarshaller.this._currentIndent);
                }

                DataObject object = entry.getKey();
                DataObjectXML objectXML = (DataObjectXML) object;
                marshalChangedObject(objectXML, _rootObject, _rootURI, _rootName, entry.getValue());

                // Call next() on the recorder to initialize its state
                try
                {
                    int event = _recorder.next();
                    if (event != START_ELEMENT)
                        throw new IllegalStateException("First event of a delegate must be a START_ELEMENT");
                }
                catch (XMLStreamException e)
                {   throw new IllegalStateException("XMLStreamException thrown from the delegate??", e); }

                // Set the indentation level
                if (XMLStreamReaderMarshaller.this._currentIndent > 0)
                {
                    _indentFlag = true;
                    _nextState = START_ELEMENT;
                    // We have potentially initiated a delegate object;
                    // In order for the text insertion to work, temporarily disable that
                    _hasDelegate = false;
                    return _state = CHARACTERS;
                }
                else
                    return _state = START_ELEMENT;
            }
            // At this point, there is no delegator, it was automatically removed after
            // all of its events have been processed
            if (XMLStreamReaderMarshaller.this._currentIndent > 0)
            {
                if (_wasIndentIncremented)
                    decrementIndent();
                if (_hadModifiedObjects)
                {
                    _indentFlag = true;
                    _nextState = END_ELEMENT;
                    return _state = CHARACTERS;
                }
                else
                    return _state = END_ELEMENT;
            }
            else
                return _state = END_ELEMENT;
        }

        protected void marshalElementProperty(PropertyXML p, Object value, DataObjectXML parent, boolean prettyPrint)
        {
            if (prettyPrint)
                _recorder.text(Marshaller.INDENT, 0, getCurrentIndent());
            // This avoid having to keep all the events associated with this element in memory
            _recorder.element(p, value, parent, prettyPrint ? getCurrentIndent() : -1);
        }

        public int marshalElement(PropertyXML p, Object value, DataObjectXML parent, int indent)
        {
            // Init the xsrmarshaller
            try
            {   _helper.close(); }
            catch (XMLStreamException e)
            {}
            _helper.setNamespaceStack(_nsstack);
            _helper.initWithDataObject(p, value, parent, indent);
            // Add to the stack of the "main" xsrmarshaller
            addDelegate(_helper);
            return START_ELEMENT;
        }
    }

    interface DataObjectMarshaller
    {
        int marshalElement(PropertyXML prop, Object value, DataObjectXML container, int indent);
    }

    /**
     * This method initailizes a new <code>XMLStreamReaderMarshaller</code> with a
     * START_ELEMENT event instead of a START_DOCUMENT event as is usually the case.
     * <p/>
     * In addition to that, it calls the normal code to create a context based on
     * an element DataObject and then goes from there
     */
    public void initWithDataObject(PropertyXML p, Object value, DataObjectXML parent, int indent)
    {
        _state = START_ELEMENT;
        _more = true;
        _csmarshaller = null; // We don't support change summary in this mode
        _currentIndent = indent;
        if (value == null)
            marshalNilElement(p.getXMLNamespaceURI(), p.getXMLName(), null);
        else
            _marshaller.marshalElementProperty(p, value, null, parent, false, false);
        // If necessary, save the value for the start element
        // If the element is of a data value type, then a text event is received
        // So conversely, if a text event was not received, it means the
        // value is a DataObject or a ChangeSummary
        if (_context._textValue == null && value instanceof DataObject)
        {
            DataObjectXML dataObject = (DataObjectXML) value; 
            _context._dataObject = dataObject;
            if (dataObject != null)
                addAttributes(_context, dataObject);
        }
    }

    protected void setNamespaceStack(NamespaceStack nsstack)
    {
        _nsstack = nsstack;
    }
}
