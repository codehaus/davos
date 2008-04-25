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
import java.util.NoSuchElementException;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import davos.sdo.DataObjectXML;
import davos.sdo.PropertyXML;
import davos.sdo.impl.common.Common;
import davos.sdo.impl.common.Names;
import davos.sdo.impl.common.NamespaceStack;

class XMLStreamReaderRecorder extends Saver implements XMLStreamReader
{
    private String _xmlEncoding;
    private String _xmlVersion;
    private List<Record> _events;
    private Record _event;
    private int _currentEvent;
    // If the current event is a START_ELEMENT, this offers the index of the first namespace event
    private int _currentNamespaceStart;
    // If the current event is a START_ELEMENT, this offers the index of the next event after
    // all attribute and namespace events associated to it
    private int _nextEvent;
    private NamespaceStack _nsstack;
    private XMLStreamReaderMarshaller.DataObjectMarshaller _helper;

    XMLStreamReaderRecorder(NamespaceStack nsstack,
        XMLStreamReaderMarshaller.DataObjectMarshaller callback)
    {
        if (nsstack == null)
            _nsstack = new NamespaceStack(null, false);
        else
            _nsstack = nsstack;
        _events = new ArrayList<Record>();
        _helper = callback;
    }

    public String getVersion()
    {
        return _xmlVersion;
    }

    public boolean isStandalone()
    {
        return false;
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
        return null;
    }

    public Object getProperty(String arg0) throws IllegalArgumentException
    {
        return null;  // Not sure what properties should be supported, the JavaDoc only
        // makes reference to notation and entities properties which are not applicable here
    }

    public int next() throws XMLStreamException
    {
        if (_currentEvent >= _events.size() - 1)
            throw new NoSuchElementException();
        Record r = _event;
        if (r != null)
            if (r._type == START_ELEMENT)
            {
                // We need to skip all of the attribute and namespace events
                // since we do not report those as separate entities
                _currentEvent = _nextEvent;
            }
            else
            {
                if (r._type == END_ELEMENT)
                    _nsstack.popMappings();
                _currentEvent++;
            }
        _event = _events.get(_currentEvent);
        int eventType = _event._type;
        if (eventType == START_ELEMENT)
        {
            _nsstack.pushMappings(false);
            // We need to set up _nextEvent and _currentNamespaceStart
            int i;
            _currentNamespaceStart = -1;
            _nextEvent = -1;
            outer:
            for (i = _currentEvent + 1; i < _events.size(); i++)
            {
                int type = _events.get(i)._type;
                switch (type)
                {
                case START_ELEMENT:
                case END_ELEMENT:
                case CHARACTERS:
                    _nextEvent = i;
                    if (_currentNamespaceStart == -1)
                        _currentNamespaceStart = _nextEvent;
                    break outer;
                case ATTRIBUTE:
                    break;
                case NAMESPACE:
                    if (_currentNamespaceStart == -1)
                        _currentNamespaceStart = i;
                    _nsstack.addMapping(_events.get(i)._prefix, _events.get(i)._uri);
                    break;
                default:
                    throw new IllegalStateException("Unknown event type: " + type);    
                }
            }
            if (_nextEvent == -1)
                throw new XMLStreamException("Malformed XML: cannot find end element \"" +
                    _event._name + '@' + _event._uri);
        }
        else if (eventType == DATAOBJECT)
        {
            return _helper.marshalElement(_event._property, _event._value, _event._container,
                _event._textArraySize);
        }
        return eventType;
    }

    public int nextTag() throws XMLStreamException
    {
        while (true)
        {
            int eventType = next();
            switch (eventType)
            {
            case START_ELEMENT:
            case END_ELEMENT:
                return eventType;
            case CHARACTERS:
                if (!isWhiteSpace())
                {
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
        return _currentEvent < _events.size() - 1;
    }

    public void require(int eventType, String namespaceURI, String localName)
        throws XMLStreamException
    {
        Record e = _event;
        if (eventType == e._type)
        {
            if (eventType == START_ELEMENT || eventType == END_ELEMENT)
            {
                if (namespaceURI != null && !namespaceURI.equals(e._uri))
                    throw new XMLStreamException("Requested namespace URI was \"" +
                        namespaceURI + "\" but the actual namespace URI is \"" +
                        e._uri);
                if (localName != null && !localName.equals(e._name))
                    throw new XMLStreamException("Requested local name was \"" +
                        localName + "\" but the actual local name URI is \"" +
                        e._name);
            }
        }
        else
            throw new XMLStreamException("Requested event was " +
                XMLStreamReaderMarshaller.eventConstantToString(eventType) +
                " but the current event is " +
                XMLStreamReaderMarshaller.eventConstantToString(e._type));
    }

    public String getElementText() throws XMLStreamException
    {
        Record e = _event;
        if (e._type != START_ELEMENT)
            throw new XMLStreamException("Current state (" +
                XMLStreamReaderMarshaller.eventConstantToString(e._type) +
                ") is not START_ELEMENT");
        Record next = _events.get(_nextEvent);
        String result = null;
        StringBuilder result2 = null;
        outer:
        while (true)
        {
            switch (next._type)
            {
            case START_ELEMENT:
                throw new XMLStreamException("Current element (" + e._name +
                    '@' + e._uri + ") has children elements");
            case END_ELEMENT:
                break outer;
            case CHARACTERS:
                if (next._text != null) // We ignore what we assume it is "indenting"
                {
                    if (result == null)
                        result = next._text;
                    else
                    {
                        if (result2 == null)
                            result2 = new StringBuilder(result);
                        result2.append(next._text);
                    }
                }
                break;
            default:
                throw new IllegalStateException("Unknown event: " + next._type);
            }
        }
        if (result2 != null)
            return result2.toString();
        else if (result == null)
            return Common.EMPTY_STRING;
        else
            return result;
    }

    public QName getName()
    {
        if (_event._type != START_ELEMENT && _event._type != END_ELEMENT)
            throw new IllegalStateException("Current state (" +
                XMLStreamReaderMarshaller.eventConstantToString(_event._type) +
                ") is not START_ELEMENT or END_ELEMENT");
        return new QName(_event._uri, _event._name, _event._prefix);
    }

    public String getLocalName()
    {
        if (_event._type != START_ELEMENT && _event._type != END_ELEMENT)
            throw new IllegalStateException("Current state (" +
                XMLStreamReaderMarshaller.eventConstantToString(_event._type) +
                ") is not START_ELEMENT or END_ELEMENT");
        return _event._name;
    }

    public boolean hasName()
    {
        return _event._type == START_ELEMENT || _event._type == END_ELEMENT;
    }

    public String getNamespaceURI()
    {
        if (_event._type != START_ELEMENT && _event._type != END_ELEMENT)
            return null;
        return _event._uri;
    }

    public String getPrefix()
    {
        if (_event._type != START_ELEMENT && _event._type != END_ELEMENT)
            return null;
        return _event._prefix;
    }

    public int getEventType()
    {
        return _event._type;
    }

    public boolean isStartElement()
    {
        return _event._type == START_ELEMENT;
    }

    public boolean isEndElement()
    {
        return _event._type == END_ELEMENT;
    }

    public boolean isCharacters()
    {
        return _event._type == CHARACTERS;
    }

    public boolean isWhiteSpace()
    {
        if (_event._type != CHARACTERS)
            return false;
        if (_event._text == null)
            // We assume this means indentation
            return true;
        for (int i = 0; i < _event._text.length(); i++)
            switch (_event._text.charAt(i))
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
        if (_event._type != START_ELEMENT)
            throw new IllegalStateException("Current state (" +
                XMLStreamReaderMarshaller.eventConstantToString(_event._type) +
            ") is not START_ELEMENT");
        // This is not optimized
        if (namespaceURI == null)
        {
            for (int i = _currentEvent + 1; i < _currentNamespaceStart; i++)
            {
                Record att = _events.get(i);
                if (localName.equals(att._name))
                    return att._text;
            }
        }
        else
        {
            for (int i = _currentEvent + 1; i < _currentNamespaceStart; i++)
            {
                Record att = _events.get(i);
                if (localName.equals(att._name) &&
                        namespaceURI.equals(att._uri))
                    return att._text;
            }
        }
        return null;
    }

    public int getAttributeCount()
    {
        if (_event._type != START_ELEMENT)
            throw new IllegalStateException("Current state (" +
                XMLStreamReaderMarshaller.eventConstantToString(_event._type) +
            ") is not START_ELEMENT");
        return _currentNamespaceStart - _currentEvent - 1;
    }

    public QName getAttributeName(int index)
    {
        if (_event._type != START_ELEMENT)
            throw new IllegalStateException("Current state (" +
                XMLStreamReaderMarshaller.eventConstantToString(_event._type) +
            ") is not START_ELEMENT");
        index += _currentEvent + 1;
        if (index >= _currentNamespaceStart)
            return null;
        Record e = _events.get(index);
        return new QName(e._uri, e._name, e._prefix);
    }

    public String getAttributeNamespace(int index)
    {
        if (_event._type != START_ELEMENT)
            throw new IllegalStateException("Current state (" +
                XMLStreamReaderMarshaller.eventConstantToString(_event._type) +
            ") is not START_ELEMENT");
        index += _currentEvent + 1;
        if (index >= _currentNamespaceStart)
            return null;
        return _events.get(index)._uri;
    }

    public String getAttributeLocalName(int index)
    {
        if (_event._type != START_ELEMENT)
            throw new IllegalStateException("Current state (" +
                XMLStreamReaderMarshaller.eventConstantToString(_event._type) +
            ") is not START_ELEMENT");
        index += _currentEvent + 1;
        if (index >= _currentNamespaceStart)
            return null;
        return _events.get(index)._name;
    }

    public String getAttributePrefix(int index)
    {
        if (_event._type != START_ELEMENT)
            throw new IllegalStateException("Current state (" +
                XMLStreamReaderMarshaller.eventConstantToString(_event._type) +
            ") is not START_ELEMENT");
        index += _currentEvent + 1;
        if (index >= _currentNamespaceStart)
            return null;
        return _events.get(index)._prefix;
    }

    public String getAttributeType(int index)
    {
        if (_event._type != START_ELEMENT)
            throw new IllegalStateException("Current state (" +
                XMLStreamReaderMarshaller.eventConstantToString(_event._type) +
            ") is not START_ELEMENT");
        index += _currentEvent + 1;
        if (index >= _currentNamespaceStart)
            return null;
        return XMLStreamReaderMarshaller.CDATA;
    }

    public String getAttributeValue(int index)
    {
        if (_event._type != START_ELEMENT)
            throw new IllegalStateException("Current state (" +
                XMLStreamReaderMarshaller.eventConstantToString(_event._type) +
            ") is not START_ELEMENT");
        index += _currentEvent + 1;
        if (index >= _currentNamespaceStart)
            return null;
        return _events.get(index)._text;
    }

    public boolean isAttributeSpecified(int index)
    {
        if (_event._type != START_ELEMENT)
            throw new IllegalStateException("Current state (" +
                XMLStreamReaderMarshaller.eventConstantToString(_event._type) +
            ") is not START_ELEMENT");
        index += _currentEvent + 1;
        if (index >= _currentNamespaceStart)
            return false;
        return true;
    }

    public int getNamespaceCount()
    {
        if (_event._type == START_ELEMENT)
        {
            return _nextEvent - _currentNamespaceStart;
        }
        else if (_event._type == END_ELEMENT)
        {
            int start = _event._elementStartIndex;
            int count = 0;
            outer:
            for (int i = start + 1; i < _events.size(); i++)
                switch (_events.get(i)._type)
                {
                case NAMESPACE:
                    count++;
                case ATTRIBUTE:
                    break;
                case START_ELEMENT:
                case END_ELEMENT:
                case CHARACTERS:
                    break outer;
                default:
                    throw new IllegalStateException("Unknown event type: " + _events.get(i)._type);
                }
            return count;
        }
        else
            throw new IllegalStateException("Current state (" +
                XMLStreamReaderMarshaller.eventConstantToString(_event._type) +
                ") is not START_ELEMENT or END_ELEMENT");
    }

    public String getNamespacePrefix(int index)
    {
        if (_event._type == START_ELEMENT)
        {
            index += _currentNamespaceStart;
            if (index >= _nextEvent)
                return null;
            else
                return _events.get(index)._prefix;
        }
        else if (_event._type == END_ELEMENT)
        {
            int start = _event._elementStartIndex;
            int count = 0;
            outer:
            for (int i = start + 1; i < _events.size(); i++)
                switch (_events.get(i)._type)
                {
                case NAMESPACE:
                    if (count++ == index)
                        return _events.get(i)._prefix;
                case ATTRIBUTE:
                    break;
                case START_ELEMENT:
                case END_ELEMENT:
                case CHARACTERS:
                    break outer;
                default:
                    throw new IllegalStateException("Unknown event type: " + _events.get(i)._type);
                }
            return null;
        }
        else
            throw new IllegalStateException("Current state (" +
                XMLStreamReaderMarshaller.eventConstantToString(_event._type) +
                ") is not START_ELEMENT or END_ELEMENT");
    }

    public String getNamespaceURI(int index)
    {
        if (_event._type == START_ELEMENT)
        {
            index += _currentNamespaceStart;
            if (index >= _nextEvent)
                return null;
            else
                return _events.get(index)._uri;
        }
        else if (_event._type == END_ELEMENT)
        {
            int start = _event._elementStartIndex;
            int count = 0;
            outer:
            for (int i = start + 1; i < _events.size(); i++)
                switch (_events.get(i)._type)
                {
                case NAMESPACE:
                    if (count++ == index)
                        return _events.get(i)._uri;
                case ATTRIBUTE:
                    break;
                case START_ELEMENT:
                case END_ELEMENT:
                case CHARACTERS:
                    break outer;
                default:
                    throw new IllegalStateException("Unknown event type: " + _events.get(i)._type);
                }
            return null;
        }
        else
            throw new IllegalStateException("Current state (" +
                XMLStreamReaderMarshaller.eventConstantToString(_event._type) +
                ") is not START_ELEMENT or END_ELEMENT");
    }

    public NamespaceContext getNamespaceContext()
    {
        return _nsstack;
    }

    public String getNamespaceURI(String prefix)
    {
        return _nsstack.getNamespaceURI(prefix);
    }

    public boolean hasText()
    {
        return _event._type == CHARACTERS;
    }

    public String getText()
    {
        if (_event._type != CHARACTERS)
            throw new IllegalStateException("Current state (" +
                XMLStreamReaderMarshaller.eventConstantToString(_event._type) +
                ") is not CHARACTERS");
        if (_event._text == null)
            return new String(_event._textArray, 0, _event._textArraySize);
        else
            return _event._text;
    }

    public char[] getTextCharacters()
    {
        if (_event._type != CHARACTERS)
            throw new IllegalStateException("Current state (" +
                XMLStreamReaderMarshaller.eventConstantToString(_event._type) +
                ") is not CHARACTERS");
        if (_event._text == null)
            return _event._textArray;
        else
            return _event._text.toCharArray();
    }

    public int getTextStart()
    {
        if (_event._type != CHARACTERS)
            throw new IllegalStateException("Current state (" +
                XMLStreamReaderMarshaller.eventConstantToString(_event._type) +
                ") is not CHARACTERS");
        return 0;
    }

    public int getTextLength()
    {
        if (_event._type != CHARACTERS)
            throw new IllegalStateException("Current state (" +
                XMLStreamReaderMarshaller.eventConstantToString(_event._type) +
                ") is not CHARACTERS");
        if (_event._text == null)
            return _event._textArraySize;
        else
            return _event._text.length();
    }

    public int getTextCharacters(int sourceStart, char[] target, int targetStart, int length)
        throws XMLStreamException
    {
        if (_event._type != CHARACTERS)
            throw new IllegalStateException("Current state (" +
                XMLStreamReaderMarshaller.eventConstantToString(_event._type) +
                ") is not CHARACTERS");
        if (target == null)
            throw new NullPointerException("The \"target\" argument cannot be null");
        if (targetStart < 0 || targetStart >= target.length)
            throw new IllegalArgumentException("The \"targetStart\" argument is out of range");
        if (length < 0 || length + targetStart > target.length)
            throw new IllegalArgumentException("The \"length\" argument is out of range");
        if (sourceStart < 0)
            sourceStart = 0; // The spec doesn't say that an exception should be thrown in this case
        int result;
        if (_event._text == null)
        {
            if (sourceStart + length > _event._textArraySize)
                result = _event._textArraySize - sourceStart;
            else
                result = length;
            System.arraycopy(_event._textArray, sourceStart, target, targetStart, result);
        }
        else
        {
            if (sourceStart + length > _event._text.length())
                result = _event._text.length() - sourceStart;
            else
                result = length;
            _event._text.getChars(sourceStart, sourceStart + result, target, targetStart);
        }
        return result;
    }

    public Location getLocation()
    {
        return XMLStreamReaderMarshaller.LOCATION;
    }

    public String getPITarget()
    {
        return null;
    }

    public String getPIData()
    {
        return null;
    }

    public void close() throws XMLStreamException
    {
        _xmlEncoding = _xmlVersion = null;
        _currentEvent = _nextEvent = _currentNamespaceStart = 0;
        _event = null;
        _events.clear();
        _elOpen = false;
    }

    // =============================================
    // Saver implementation
    // =============================================
    NamespaceStack getNamespaceStack()
    {
        return _nsstack;
    }

    // ======================================
    // SDOEventModel implementation
    // ======================================
    private boolean _elOpen = false;

    public void startElement(String uri, String name, String prefix,
        String xsiTypeUri, String xsiTypeName)
    {
        if (_elOpen)
            addNamespaceDeclarations();
        _nsstack.pushMappings(false);
        Record r = new Record();
        r._type = START_ELEMENT;
        r._uri = uri == null ? Common.EMPTY_STRING : uri;
        r._name = name;
        r._prefix = _nsstack.ensureMapping(uri, prefix, false, true);
        _events.add(r);

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

        _elOpen = true;
    }

    public void endElement()
    {
        if (_elOpen)
            addNamespaceDeclarations();
        _nsstack.popMappings();
        Record r = new Record();
        r._type = END_ELEMENT;
        // Set the _elementStartIndex value, a nice little algorithm
        // Search backwards for the first START_ELEMENT event, ignoring the elements
        // already closed
        int i;
        outer:
        for (i = _events.size() - 1; i >=0 ; i--)
        {
            Record e = _events.get(i);
            switch (e._type)
            {
            case START_ELEMENT:
                r._elementStartIndex = i;
                break outer;
            case END_ELEMENT:
                i = e._elementStartIndex;
                break;
            }
        }
        if (i < 0)
            throw new IllegalStateException("End element without start element");
        _events.add(r);
    }

    public void attr(String uri, String local, String prefix, String value)
    {
        Record r = new Record();
        r._type = ATTRIBUTE;
        r._uri = uri == null ? Common.EMPTY_STRING : uri;
        r._name = local;
        r._prefix = _nsstack.ensureMapping(uri, prefix, false, true);
        r._text = value;
        _events.add(r);
    }

    public void sattr(int type, String name, String value)
    {
        Record r = new Record();
        r._type = ATTRIBUTE;
        switch (type)
        {
        case ATTR_XSI:
            String attPrefix = _nsstack.ensureMapping(Names.URI_XSD_INSTANCE,
                Names.PREFIX_XSD_INSTANCE, false, true);
            r._uri = Names.URI_XSD_INSTANCE;
            r._name = name;
            r._prefix = attPrefix;
            r._text = value;
            break;
        }
        _events.add(r);
    }

    public void text(char[] buff, int off, int cch)
    {
        if (_elOpen)
            addNamespaceDeclarations();
        Record r = new Record();
        r._type = CHARACTERS;
        // A small optimization here
        if (buff == Marshaller.INDENT && off == 0)
        {
            r._textArray = Marshaller.INDENT;
            r._textArraySize = cch;
        }
        else
        {
            r._text = new String(buff, off, cch);
        }
        _events.add(r);
    }

    public void text(String s)
    {
        if (_elOpen)
            addNamespaceDeclarations();
        Record r = new Record();
        r._type = CHARACTERS;
        r._text = s;
        _events.add(r);
    }

    public void xmlDecl(String version, String encoding)
    {
        _xmlEncoding = encoding;
        _xmlVersion = version;
    }

    public void xmlns(String prefix, String uri)
    {
        Record r = new Record();
        r._type = NAMESPACE;
        r._prefix = prefix;
        r._uri = uri;
        _events.add(r);
    }

    private void addNamespaceDeclarations()
    {
        _elOpen = false;
        for (_nsstack.iterateMappings(); _nsstack.hasMapping(); _nsstack.nextMapping())
        {
            String prefix = _nsstack.mappingPrefix();
            String uri = _nsstack.mappingUri();
            xmlns(prefix, uri);
        }
    }

    /**
     * Constant that is not taken by javax.xml.stream.XMLStreamConstants
     */
    private static int DATAOBJECT = 1265621;

    /**
     * In order to reduce the number of events buffered, we support an additional
     * type of event, which holds on to an element in its DataObject form, rather than
     * its serialized form. <p/>
     * The information is recorded in the events buffer and will be fed back to the
     * caller at marshalling time via a call to the 
     * {@link davos.sdo.impl.marshal.XMLStreamReaderMarshaller.DataObjectMarshaller.marshalElement}
     * method.
     * @param p the property
     * @param value the value, usually a DataObject
     * @param container the containing DataObject of the value
     * @param indent the indentation level to be used when marshalling this
     */
    public void element(PropertyXML p, Object value, DataObjectXML container, int indent)
    {
        Record r = new Record();
        r._type = DATAOBJECT;
        r._property = p;
        r._value = value;
        r._container = container;
        // Reusing this field
        r._textArraySize = indent;        
        _events.add(r);
    }

    /**
     * This class is used to represent XML information: elements, attributes, namespaces and
     * text, for the purposes of recording it and playing it back later
     * @author radup
     *
     */
    private static class Record
    {
        int _type;
        String _uri;
        String _name;
        String _prefix;
        String _text;
        int _elementStartIndex;
        char[] _textArray;
        int _textArraySize;
        PropertyXML _property;
        Object _value;
        DataObjectXML _container;

        public String toString()
        {
            StringBuilder s = new StringBuilder();
            s.append(XMLStreamReaderMarshaller.eventConstantToString(_type)).append(": ");
            s.append("uri =").append(_uri).append(", name=").append(_name).append(", prefix=").append(_prefix);
            if (_text != null)
                s.append(", text=" + _text);
            else if (_textArray != null)
                s.append(", textArray=" + new String(_textArray, 0, _textArraySize));
            return s.toString();
        }
    }
}
