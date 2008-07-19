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
import java.util.Collection;

import org.apache.xmlbeans.SchemaField;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.impl.validator.Validator;

import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.Attributes;

import davos.sdo.SDOError;
import davos.sdo.impl.common.Names;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;

class SaxValidator implements ContentHandler
{
    private ContentHandler _delegate;
    private SchemaTypeLoader _stl;
    private NamespaceContext _nsContext;
    private Collection _errorListener;
    private Validator _validator;
    private StringBuilder _buffer = new StringBuilder();
    private Location _location;

    public SaxValidator(ContentHandler delegate, NamespaceContext nsctx, SchemaTypeLoader stl,
        Collection errorListener)
    {
        _delegate = delegate;
        _stl = stl;
        _nsContext = nsctx;
        _errorListener = errorListener;
    }

    public void setDocumentLocator(Locator locator)
    {
        _location = new LocatorToLocation(locator);
        _delegate.setDocumentLocator(locator);
    }

    public void startDocument() throws SAXException
    {
        _delegate.startDocument();
    }

    public void endDocument() throws SAXException
    {
        _delegate.endDocument();
    }

    public void startPrefixMapping(String prefix, String uri) throws SAXException
    {
        _delegate.startPrefixMapping(prefix, uri);
    }

    public void endPrefixMapping(String prefix) throws SAXException
    {
        _delegate.endPrefixMapping(prefix);
    }

    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException
    {
        // Validate
        if (_buffer.length() > 0)
        {
            if (_validator != null)
                _validator.nextEvent(Validator.TEXT, new Event(_buffer.toString()));
            _buffer.setLength(0);
        }
        boolean hasXsi = false;
        String xsiType = null;
        String xsiNil = null;
        String xsiSchemaLoc = null;
        String xsiNoNSchemaLoc = null;
        ArrayList<Event> attributeEvents = new ArrayList<Event>(atts.getLength());
        for (int i = 0; i < atts.getLength(); i++)
        {
            String attURI = atts.getURI(i);
            String attName = atts.getLocalName(i);
            String attValue = atts.getValue(i);
            if (Names.URI_XSD_INSTANCE.equals(attURI))
            {
                if (Names.XSI_TYPE.equals(attName))
                {
                    xsiType = attValue;
                    hasXsi = true;
                }
                else if (Names.XSI_NIL.equals(attName))
                {
                    xsiNil = attValue;
                    hasXsi = true;
                }
                else if (SDOEventModel.SCHEMA_LOCATION.equals(attName))
                {
                    xsiSchemaLoc = attValue;
                    hasXsi = true;
                }
                else if (SDOEventModel.NO_NAMESCAPCE_SCHEMA_LOCATION.equals(attName))
                {
                    xsiNoNSchemaLoc = attValue;
                    hasXsi = true;
                }
                else
                {
                    // Ignore it
                }
            }
            else if (atts.getQName(i).startsWith("xmlns"))
            {
                // Ignore it
            }
            else
            {
                attributeEvents.add(new Event(new QName(attURI, attName), attValue));
            }
        }
        if (_validator == null)
        {
            // Find the document type
            QName globalElName = new QName(uri, localName);
            SchemaType t = _stl.findDocumentType(globalElName);
            if (t == null)
                _errorListener.add(SDOError.messageForCode("validation.globalelement",
                    globalElName.toString()));
            else
            {
                _validator = new Validator(t, null, _stl, null, _errorListener);
                _validator.nextEvent(Validator.BEGIN, new Event((QName) null));
            }
        }
        if (hasXsi)
            _validator.nextEvent(Validator.BEGIN, new Event(new QName(uri, localName), xsiType,
                xsiNil, xsiSchemaLoc, xsiNoNSchemaLoc));
        else
            _validator.nextEvent(Validator.BEGIN, new Event(new QName(uri, localName)));
        for (Event a : attributeEvents)
            _validator.nextEvent(Validator.ATTR, a);
        _validator.nextEvent(Validator.ENDATTRS, new Event((QName) null));
        _delegate.startElement(uri, localName, qName, atts);
    }

    public void endElement(String uri, String localName, String qName) throws SAXException
    {
        if (_buffer.length() > 0)
        {
            _validator.nextEvent(Validator.TEXT, new Event(_buffer.toString()));
            _buffer.setLength(0);
        }
        _validator.nextEvent(Validator.END, new Event(new QName(uri, localName)));
        _delegate.endElement(uri, localName, qName);
    }

    public void characters(char ch[], int start, int length) throws SAXException
    {
        _buffer.append(ch, start, length);
        _delegate.characters(ch, start, length);
    }

    public void ignorableWhitespace(char ch[], int start, int length) throws SAXException
    {
        _delegate.ignorableWhitespace(ch, start, length);
    }

    public void processingInstruction(String target, String data) throws SAXException
    {
        _delegate.processingInstruction(target, data);
    }

    public void skippedEntity(String name) throws SAXException
    {
        _delegate.skippedEntity(name);
    }

    private class Event implements Validator.Event
    {
        private QName _name;
        private String _xsiT;
        private String _xsiN;
        private String _xsiSL;
        private String _xsiNSL;
        private String _text;

        Event(QName name)
        {
            _name = name;
        }

        Event(QName name, String xsiType, String xsiNil,
            String schemaLocation, String noNamespaceSchemaLocation)
        {
            this(name);
            _xsiT = xsiType;
            _xsiN = xsiNil;
            _xsiSL = schemaLocation;
            _xsiNSL = noNamespaceSchemaLocation;
        }

        Event(QName name, String text)
        {
            this(name);
            _text = text;
        }

        Event(String text)
        {
            _text = text;
        }

        public XmlCursor getLocationAsCursor()
        {
            return null;
        }

        public Location getLocation()
        {
            return _location;
        }

        public String getXsiType() // BEGIN xsi:type
        {
            return _xsiT;
        }

        public String getXsiNil() // BEGIN xsi:nil
        {
            return _xsiN;
        }

        public String getXsiLoc() // BEGIN xsi:schemaLocation
        {
            return _xsiSL;
        }

        public String getXsiNoLoc() // BEGIN xsi:noNamespaceSchemaLocation
        {
            return _xsiNSL;
        }

        public QName getName()
        {
            return _name;
        }

        // On TEXT and ATTR
        public String getText()
        {
            return _text;
        }

        public String getText(int wsr)
        {
            switch (wsr)
            {
                case PRESERVE:
                    return _text;
                case REPLACE:
                    return replace(_text);
                case COLLAPSE:
                    return collapse(_text);
                default:
                    return _text;
            }
        }

        public boolean textIsWhitespace()
        {
            if (_text != null)
            {
                for (int i = 0; i < _text.length(); i++)
                {
                    char c = _text.charAt(i);
                    if (c != 0x20 && c != 0x09 && c != 0x0A && c != 0x0D)
                        return false;
                }
            }
            return true;
        }

        private String replace(String text)
        {
            StringBuilder b;
            int i;
            for (i = 0; i < text.length(); i++)
            {
                char c = text.charAt(i);
                if (c == 0x09 || c == 0x0A || c == 0x0D)
                {
                    b = new StringBuilder(text);
                    b.setCharAt(i, ' ');
                    for (i++; i < text.length(); i++)
                    {
                        c = text.charAt(i);
                        if (c == 0x09 || c == 0x0A || c == 0x0D)
                            b.setCharAt(i, ' ');
                    }
                    return b.toString();
                }
            }
            return text;
        }

        private String collapse(String text)
        {
            int i;
            boolean space = false;
            for (i = 0; i < text.length(); i++)
            {
                char c = text.charAt(i);
                if (c == 0x09 || c == 0x0A || c == 0x0D)
                    return collapse1(text, i);
                else if (c == ' ')
                    if (space)
                        return collapse1(text, i);
                    else
                        space = true;
                else
                    space = false;
            }
            return text;
        }

        private String collapse1(String text, int start)
        {
            boolean space = text.charAt(start) == ' ';
            StringBuilder b = new StringBuilder(text);
            b.setLength(start - 1);
            for (int i = start; i < text.length(); i++)
            {
                char c = text.charAt(i);
                if (c == 0x20 || c == 0x09 || c == 0x0A || c == 0x0D)
                    if (space)
                        ;
                    else
                    {
                        space = true;
                        b.append(' ');
                    }
                else
                {
                    space = false;
                    b.append(c);
                }
            }
            return b.toString();
        }

        public String getNamespaceForPrefix(String prefix)
        {
            return _nsContext.getNamespaceURI(prefix);
        }
    }
}
