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

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Namespace;

import davos.sdo.SDOContext;
import davos.sdo.SDOError;
import davos.sdo.impl.common.Common;
import davos.sdo.impl.common.Names;
import davos.sdo.impl.common.NamespaceStack;
import javax.sdo.DataObject;

public class StAXSaver extends Saver
{
    private XMLStreamWriter _xsw;
    private XMLEventWriter _xew;
    private XMLEventFactory _factory;
    private NamespaceStack _nsstack;
    private boolean _nsAdded = true;
    boolean _useStream;
    private List<Attribute> _attributeList;
    private List<Namespace> _namespaceList;
    private Stack<String> _elementPrefixes;
    private Stack<String> _elementNames;
    private Stack<String> _elementUris;

    public StAXSaver(XMLStreamWriter xStreamWriter)
    {
        _xsw = xStreamWriter;
        // TODO(radup) When we have a Map backed by a NamespaceContext,
        // then use it to initialize the set of "preknown" namespaces
        _nsstack = new NamespaceStack(null, false);
        _useStream = true;
    }

    public StAXSaver(XMLEventWriter xEventWriter)
    {
        _xew = xEventWriter;
        _nsstack = new NamespaceStack(null, false);
        _factory = XMLEventFactory.newInstance();
        _useStream = false;
        _attributeList = new ArrayList<Attribute>();
        _namespaceList = new ArrayList<Namespace>();
        _elementPrefixes = new Stack<String>();
        _elementNames = new Stack<String>();
        _elementUris = new Stack<String>();
    }

    public void save(DataObject rootObject,
        String rootElementURI, String rootElementName,
        boolean isXmlDecl, String xmlVersion, String encoding,
        String schemaLocation, String noNSSchemaLocation,
        Object options, SDOContext sdoContext) throws IOException
    {
        try
        {
            super.save(rootObject, rootElementURI, rootElementName,
                isXmlDecl, xmlVersion, encoding,
                schemaLocation, noNSSchemaLocation,
                options, sdoContext);
        }
        catch (WrapperException we)
        {
            // Exception not visible, uncomment next lines to see it 
            //System.out.println("---WrapperException---");
            //we.printStackTrace(System.out);
            throw new IOException(SDOError.messageForCode("xml.staxsaver.streamexception",
                we.getCause().getMessage()));
        }
        try
        {
            if (_useStream)
                _xsw.flush();
            else
                _xew.flush();
        }
        catch (XMLStreamException xse)
        {
            throw new IOException(SDOError.messageForCode("xml.staxsaver.streamexception",
                xse.getMessage()));
        }
    }

    public void startElement(String uri, String name, String prefix,
        String xsiTypeUri, String xsiTypeName)
    {
        if (!_nsAdded)
            // Make sure that all prefix declarations are output
            addNsDeclarations();

        _nsstack.pushMappings(false);
        prefix = _nsstack.ensureMapping(uri, prefix, false, true);
        // See the comment in attribute()
        if (prefix == null)
            prefix = Common.EMPTY_STRING;
        if (uri == null)
            uri = Common.EMPTY_STRING;
        try
        {
            if (_useStream)
            {
//              _xsw.setPrefix(prefix, uri);
                _xsw.writeStartElement(prefix, name, uri);
            }
            else
            {
                // Clear the list of attributes and namespace declarations
                _attributeList.clear();
                _namespaceList.clear();
                // Save the current element name because it is required by the EndElement event
                _elementPrefixes.add(prefix);
                _elementNames.add(name);
                _elementUris.add(uri);
            }
        }
        catch (XMLStreamException e)
        {
            throw new WrapperException(e);
        }

        if (xsiTypeName != null)
        {
            String xsiPrefix = _nsstack.ensureMapping(xsiTypeUri, null, false, false);
            String schemaInstancePrefix = _nsstack.ensureMapping(Names.URI_XSD_INSTANCE,
                Names.PREFIX_XSD_INSTANCE, false, false);
            String value = xsiPrefix == null ? xsiTypeName : xsiPrefix + ':' + xsiTypeName;
            try
            {
                if (_useStream)
                {
//                  _xsw.setPrefix(schemaInstancePrefix, Names.URI_XSD_INSTANCE);
                    _xsw.writeAttribute(schemaInstancePrefix, Names.URI_XSD_INSTANCE, Names.XSI_TYPE,
                    value);
                }
                else
                {
                    _attributeList.add(_factory.createAttribute(schemaInstancePrefix,
                        Names.URI_XSD_INSTANCE, Names.XSI_TYPE, value));
                }
            }
            catch (XMLStreamException e)
            {
                throw new WrapperException(e);
            }
        }
        _nsAdded = false;
    }

    public void attr(String uri, String local, String prefix, String value)
    {
        prefix = _nsstack.ensureMapping(uri, prefix, false, true);
        // Even though the StAX documentation says that a null prefix is treated the same
        // as "", the implementation is happy to write 'null:attr="value"' so we have to check for that
        if (prefix == null)
            prefix = Common.EMPTY_STRING;
        try
        {
            if (_useStream)
            {
//              _xsw.setPrefix(prefix, uri);
                if (uri==null)
                    uri = "";
                _xsw.writeAttribute(prefix, uri, local, value);
            }
            else
            {
                _attributeList.add(_factory.createAttribute(prefix, uri, local, value));
            }
        }
        catch (XMLStreamException e)
        {
            throw new WrapperException(e);
        }
    }

    public void endElement()
    {
        if (!_nsAdded)
            addNsDeclarations();
        try
        {
            if (_useStream)
                _xsw.writeEndElement();
            else
            {
                // Unregister prefixes...
                for (_nsstack.iterateMappings(); _nsstack.hasMapping(); _nsstack.nextMapping())
                {
                    String prefix = _nsstack.mappingPrefix();
                    String uri = _nsstack.mappingUri();
                    _namespaceList.add(_factory.createNamespace(prefix, uri));
                }
                _xew.add(_factory.createEndElement(_elementPrefixes.pop(), _elementUris.pop(),
                    _elementNames.pop(), _namespaceList.iterator()));
            }
        }
        catch (XMLStreamException e)
        {
            throw new WrapperException(e);
        }
        _nsstack.popMappings();
    }

    public void sattr(int type, String name, String value)
    {
        switch (type)
        {
        case SDOEventModel.ATTR_XSI:
            String attPrefix = _nsstack.ensureMapping(Names.URI_XSD_INSTANCE,
                Names.PREFIX_XSD_INSTANCE, false, true);
            try
            {
                if (_useStream)
                {
//                  _xsw.setPrefix(attPrefix, Names.URI_XSD_INSTANCE);
                    _xsw.writeAttribute(attPrefix, Names.URI_XSD_INSTANCE, Names.XSI_TYPE, value);
                }
                else
                {
                    _attributeList.add(_factory.createAttribute(attPrefix, Names.URI_XSD_INSTANCE,
                        Names.XSI_TYPE, value));
                }
            }
            catch (XMLStreamException e)
            {
                throw new WrapperException(e);
            }
        }
    }

    public void text(char[] buff, int off, int cch)
    {
        if (!_nsAdded)
            addNsDeclarations();
        try
        {
            if (_useStream)
                _xsw.writeCharacters(buff, off, cch);
            else
                _xew.add(_factory.createCharacters(new String(buff, off, cch)));
        }
        catch (XMLStreamException e)
        {
            throw new WrapperException(e);
        }
    }

    public void text(String s)
    {
        if (!_nsAdded)
            addNsDeclarations();
        try
        {
            if (_useStream)
                _xsw.writeCharacters(s);
            else
                _xew.add(_factory.createCharacters(s));
        }
        catch (XMLStreamException e)
        {
            throw new WrapperException(e);
        }
    }

    public void xmlDecl(String version, String encoding)
    {
        try
        {
            if (_useStream)
                _xsw.writeStartDocument(encoding, version);
            else
                _xew.add(_factory.createStartDocument(encoding, version));
        }
        catch (XMLStreamException xse)
        {
            throw new WrapperException(xse);
        }
    }

    public void xmlns(String prefix, String uri)
    {
        try
        {
            if (_useStream)
            {
                if (prefix == null || prefix.length() == 0)
                    _xsw.writeDefaultNamespace(uri);
                else
                    _xsw.writeNamespace(prefix, uri);
            }
            else
            {
                if (prefix == null || prefix.length() == 0)
                    _namespaceList.add(_factory.createNamespace(uri));
                else
                    _namespaceList.add(_factory.createNamespace(prefix, uri));
            }
        }
        catch (XMLStreamException e)
        {
            throw new WrapperException(e);
        }
    }

    NamespaceStack getNamespaceStack()
    {
        return _nsstack;
    }

    private void addNsDeclarations()
    {
        for (_nsstack.iterateMappings(); _nsstack.hasMapping(); _nsstack.nextMapping())
        {
            String prefix = _nsstack.mappingPrefix();
            String uri = _nsstack.mappingUri();

            try
            {
                if (_useStream)
                {
                    if (prefix == null || prefix.length() == 0)
                        _xsw.writeDefaultNamespace(uri);
                    else
                        _xsw.writeNamespace(prefix, uri);
                }
                else
                {
                    if (prefix == null || prefix.length() == 0)
                        _namespaceList.add(_factory.createNamespace(uri));
                    else
                        _namespaceList.add(_factory.createNamespace(prefix, uri));
                }
            }
            catch (XMLStreamException e)
            {
                throw new WrapperException(e);
            }
        }
        _nsAdded = true;
        if (!_useStream)
        {
            try
            {
                _xew.add(_factory.createStartElement(_elementPrefixes.peek(), _elementUris.peek(),
                    _elementNames.peek(), _attributeList.iterator(), _namespaceList.iterator()));
            } catch (XMLStreamException e)
            {
                throw new WrapperException(e);
            }
        }
    }

    private static class WrapperException extends RuntimeException
    {
        WrapperException(XMLStreamException t)
        {
            super(t);
        }
    }
}
