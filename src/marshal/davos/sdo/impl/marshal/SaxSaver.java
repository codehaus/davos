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
import java.util.Stack;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.apache.xmlbeans.SchemaTypeLoader;

import davos.sdo.SDOContext;
import davos.sdo.SDOError;
import davos.sdo.SDOXmlException;
import davos.sdo.Options;
import davos.sdo.impl.common.Common;
import davos.sdo.impl.common.Names;
import davos.sdo.impl.common.NamespaceStack;

import javax.sdo.DataObject;

public class SaxSaver extends Saver
{
    private static final String CDATA = "CDATA";
    private ContentHandler _h;
    private AttributesImpl _currentAttributes;
    private String _currentElementURI;
    private String _currentElementName;
    private String _currentElementQName;

    private NamespaceStack _nsstack = new NamespaceStack(null, false);
    private boolean _elStarted;
    private Stack<String> _elNameStack = new Stack<String>();
    private Stack<String> _elUriStack = new Stack<String>();

    public SaxSaver(ContentHandler ch)
    {
        _h = ch;
        _currentAttributes = new AttributesImpl();
    }

    @Override
    public void save(DataObject rootObject, String rootElementURI, String rootElementName,
        boolean hasXmlDecl, String xmlVersion, String encoding, String schemaLocation,
        String noNSSchemaLocation, Object options, SDOContext sdoContext) throws IOException
    {
        try
        {
            _h.startDocument();
        }
        catch (SAXException e)
        {
            throw new SDOXmlException(SDOError.messageForCode("xml.saxsaver.userexception",
                e.getMessage()), e);
        }
        // Check if we have to perform validation
        installValidator(options, sdoContext);
        super.save(rootObject, rootElementURI, rootElementName, hasXmlDecl, xmlVersion,
            encoding, schemaLocation, noNSSchemaLocation, options, sdoContext);
        try
        {
            _h.endDocument();
        }
        catch (SAXException e)
        {
            throw new SDOXmlException(SDOError.messageForCode("xml.saxsaver.userexception",
                e.getMessage()), e);
        }
    }

    private void installValidator(Object options, SDOContext sdoctx)
    {
        boolean validate = false;
        if (options != null)
        {
            if (options instanceof Options)
                validate = ((Options) options).getMap().containsKey(Options.VALIDATE);
            else if (options instanceof Options)
                validate = ((java.util.Map) options).containsKey(Options.VALIDATE);
        }
        if (validate)
        {
            SchemaTypeLoader tl = sdoctx.getTypeSystem().getSchemaTypeLoader();
            SaxValidator v = new SaxValidator(_h, _nsstack, tl, new SDOValidationErrorListener(true));
            _h = v;
        }
    }

    public void startElement(String uri, String name, String prefix,
        String xsiTypeUri, String xsiTypeName)
    {
        if (_elStarted)
            startElementEvent();
        _nsstack.pushMappings(false);
        String elPrefix = _nsstack.ensureMapping(uri, prefix, false, true);
        _currentAttributes.clear();
        _currentElementURI = uri == null ? Common.EMPTY_STRING : uri;
        _currentElementName = name;
        if (elPrefix != null)
            _currentElementQName = elPrefix + ':' + name;
        else
            _currentElementQName = name;
        if (xsiTypeName != null)
        {
            String xsiPrefix = _nsstack.ensureMapping(xsiTypeUri, null, false, false);
            String schemaInstancePrefix = _nsstack.ensureMapping(Names.URI_XSD_INSTANCE,
                Names.PREFIX_XSD_INSTANCE, false, false);
            String value = xsiPrefix == null ? xsiTypeName : xsiPrefix + ':' + xsiTypeName;
            _currentAttributes.addAttribute(Names.URI_XSD_INSTANCE, Names.XSI_TYPE,
                schemaInstancePrefix + ':' + Names.XSI_TYPE, CDATA, value);
        }
        _elNameStack.push(_currentElementName);
        _elUriStack.push(_currentElementURI);
        _elStarted = true;
    }

    public void attr(String uri, String local, String prefix, String value)
    {
        String attPrefix = _nsstack.ensureMapping(uri, prefix, false, true);
        _currentAttributes.addAttribute(uri, local, attPrefix == null ? local :
            attPrefix + ':' + local, CDATA, value);
    }

    public void sattr(int type, String name, String value)
    {
        switch (type)
        {
        case SDOEventModel.ATTR_XSI:
            String attPrefix = _nsstack.ensureMapping(Names.URI_XSD_INSTANCE,
                Names.PREFIX_XSD_INSTANCE, false, true);
            _currentAttributes.addAttribute(Names.URI_XSD_INSTANCE, name, attPrefix + ':' + name,
                CDATA, value);
            break;
        }
    }

    public void endElement()
    {
        String lastElementName = _elNameStack.pop();
        String lastElementUri = _elUriStack.pop();
        if (_elStarted)
            startElementEvent();
        try
        {
            if (lastElementUri == null || lastElementUri.length() == 0)
            {
                _h.endElement(Common.EMPTY_STRING, lastElementName,
                    lastElementName);
            }
            else
            {
                String prefix = _nsstack.getUriMapping(lastElementUri);
                String qName = prefix == null ? lastElementName : prefix + ':'
                    + lastElementName;
                _h.endElement(lastElementUri, lastElementName, qName);
            }
            for (_nsstack.iterateMappings(); _nsstack.hasMapping(); _nsstack.nextMapping())
            {
                String prefix = _nsstack.mappingPrefix();
                _h.endPrefixMapping(prefix);
            }
        }
        catch (SAXException e)
        {
            throw new SDOXmlException(SDOError.messageForCode("xml.saxsaver.userexception",
                e.getMessage()), e);
        }
        _nsstack.popMappings();
    }

    public void text(char[] buff, int off, int cch)
    {
        if (_elStarted)
            startElementEvent();
        try
        {
            _h.characters(buff, off, cch);
        }
        catch (SAXException e)
        {
            throw new SDOXmlException(SDOError.messageForCode("xml.saxsaver.userexception",
                e.getMessage()), e);
        }        
    }

    public void text(String s)
    {
        if (_elStarted)
            startElementEvent();
        char[] buffer = new char[s.length()];
        s.getChars(0, s.length(), buffer, 0);
        try
        {
            _h.characters(buffer, 0, s.length());
        }
        catch (SAXException e)
        {
            throw new SDOXmlException(SDOError.messageForCode("xml.saxsaver.userexception",
                e.getMessage()), e);
        }        
    }

    public void xmlDecl(String version, String encoding)
    {
        // Nothing to report for SAX
    }

    public void xmlns(String prefix, String uri)
    {
        try
        {
            _h.startPrefixMapping(prefix, uri);
        }
        catch (SAXException e)
        {
            throw new SDOXmlException(SDOError.messageForCode("xml.saxsaver.userexception",
                e.getMessage()), e);
        }
    }

    private void startElementEvent()
    {
        _elStarted = false;
        try
        {
            for (_nsstack.iterateMappings(); _nsstack.hasMapping(); _nsstack.nextMapping())
            {
                String prefix = _nsstack.mappingPrefix();
                String uri = _nsstack.mappingUri();
                _h.startPrefixMapping(prefix, uri);
            }
            _h.startElement(_currentElementURI, _currentElementName, _currentElementQName,
                _currentAttributes);
        }
        catch (SAXException e)
        {
            throw new SDOXmlException(SDOError.messageForCode("xml.saxsaver.userexception",
                e.getMessage()), e);
        }
    }

    NamespaceStack getNamespaceStack()
    {
        return _nsstack;
    }
}
