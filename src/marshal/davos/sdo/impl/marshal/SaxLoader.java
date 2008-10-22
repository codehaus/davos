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
import java.io.InputStream;
import java.io.Reader;

import org.apache.xmlbeans.impl.piccolo.xml.Piccolo;
import org.apache.xmlbeans.SchemaTypeLoader;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.DeclHandler;
import org.xml.sax.ext.LexicalHandler;

import javax.sdo.helper.XMLDocument;

import davos.sdo.SDOContext;
import davos.sdo.SDOError;
import davos.sdo.SDOXmlException;
import davos.sdo.Options;
import davos.sdo.impl.common.Common;
import davos.sdo.impl.common.Names;


public abstract class SaxLoader extends SimpleNamespaceHandler
implements ContentHandler, DeclHandler, DTDHandler, ErrorHandler, LexicalHandler, Loader,
NamespaceHandler
{
    protected Unmarshaller _m;
    private XMLReader _xr;
    private boolean _nssupportContextPushed;
    private Locator _loc;

    SaxLoader(XMLReader r)
    {
        _xr = r;

        // For now, we don't care about the lexical
        // _xr.setProperty(
        //     "http://xml.org/sax/properties/lexical-handler", this);
        _xr.setContentHandler(this);
        // For now, we don't care about DTDs
        // _xr.setProperty("http://xml.org/sax/properties/declaration-handler", this);
        // _xr.setDTDHandler(this);
        _xr.setErrorHandler(this);
    }

    /**
     * @param inputStream
     * @param locationURI
     * @param options
     * @return the XMLDocument
     */
    public XMLDocument load(InputStream inputStream, String locationURI, Object options, SDOContext sdoctx)
        throws IOException
    {
        XMLDocumentImpl root = new XMLDocumentImpl();
        _m = Unmarshaller.get(root, options, sdoctx);
        _m.setLoader(this);
        _m.setNamespaceHandler(this);
        _m.setLocator(_loc);
        resetNamespaces();
        installValidator(options, sdoctx);
        try
        {
            InputSource is = new InputSource(inputStream);
            is.setSystemId(locationURI);
            _xr.parse(is);
        }
        catch (SAXException se)
        {
            throw new SDOXmlException(se);
        }
        _m.finish();
        return root;
    }

    /**
     * @param inputReader
     * @param locationURI
     * @param options
     * @return the XMLDocument
     */
    public XMLDocument load(Reader inputReader, String locationURI, Object options, SDOContext sdoctx)
        throws IOException
    {
        XMLDocumentImpl root = new XMLDocumentImpl();
        _m = Unmarshaller.get(root, options, sdoctx);
        _m.setLoader(this);
        _m.setNamespaceHandler(this);
        _m.setLocator(_loc);
        resetNamespaces();
        installValidator(options, sdoctx);
        try
        {
            InputSource is = new InputSource(inputReader);
            is.setSystemId(locationURI);
            _xr.parse(is);
        }
        catch (SAXException se)
        {
            throw new SDOXmlException(se);
        }
        _m.finish();
        return root;
    }

    /**
     * @param inputSource
     * @param locationURI
     * @param options
     * @return the XMLDocument
     */
    public XMLDocument load(InputSource inputSource, String locationURI, Object options, SDOContext sdoctx)
        throws IOException
    {
        XMLDocumentImpl root = new XMLDocumentImpl();
        _m = Unmarshaller.get(root, options, sdoctx);
        _m.setLoader(this);
        _m.setNamespaceHandler(this);
        _m.setLocator(_loc);
        resetNamespaces();
        installValidator(options, sdoctx);
        try
        {
            if (locationURI != null)
                inputSource.setSystemId(locationURI);
            _xr.parse(inputSource);
        }
        catch (SAXException se)
        {
            throw new SDOXmlException(se);
        }
        _m.finish();
        return root;
    }

    public void changeUnmarshaller(Unmarshaller m)
    {
        _m = m;
    }

    // ==========================================
    // SAX handler implementation
    // ==========================================

    public void startDocument()
        throws SAXException
    {
        // Do nothing ... start of document is implicit
    }

    public void endDocument()
        throws SAXException
    {
        // Do nothing ... end of document is implicit
    }

    public void startElement(String uri, String local, String qName,
        Attributes atts)
        throws SAXException
    {
        if (local.length() == 0)
            local = qName;

        if (!_nssupportContextPushed)
            pushNamespaceContext();
        _nssupportContextPushed = false;
        String xsiTypeUri = null;
        String xsiTypeName = null;
        int len = atts.getLength();
        if (len > 0)
        {
            String xsiType = atts.getValue(Names.URI_XSD_INSTANCE, Names.XSI_TYPE);
            if (xsiType != null)
            {
                int colon = xsiType.indexOf(':');
                if (colon > 0)
                {
                    // Qualified type name
                    String xsiPrefix = xsiType.substring(0, colon);
                    xsiTypeUri = getNamespaceURI(xsiPrefix);
                    if (xsiTypeUri == null)
                        throw new RuntimeException(SDOError.messageForCode(
                            "xml.prefix.notdeclared.xsitype", xsiPrefix, xsiType));
                    xsiTypeName = xsiType.substring(colon + 1);
                }
                else
                {
                    xsiTypeUri = getNamespaceURI(Common.EMPTY_STRING);
                    xsiTypeName = xsiType;
                }
            }
        }
        int colon = qName.indexOf(':');
        if (colon > 0)
            _m.startElement(uri, local, qName.substring(0, colon), xsiTypeUri, xsiTypeName);
        else
            _m.startElement(uri, local, null, xsiTypeUri, xsiTypeName);

        for (int i = 0; i < len; i++)
        {
            String aqn = atts.getQName(i);

            if (aqn.startsWith("xmlns"))
            {
                if (aqn.length() == 5)
                    _m.xmlns(Common.EMPTY_STRING, atts.getValue(i));
                else
                {
                    String prefix = aqn.substring(6);

                    if (prefix.length() == 0)
                    {
                        throw new SDOXmlException(SDOError.messageForCode("xml.prefix.malformed1"));
                    }

                    String attrUri = atts.getValue(i);

                    if (attrUri.length() == 0)
                    {
                        throw new SDOXmlException(SDOError.messageForCode(
                            "xml.prefix.defaultnamespace", prefix));
                    }

                    _m.xmlns(prefix, attrUri);
                }
            }
            else if (Names.URI_XSD_INSTANCE.equals(atts.getURI(i)))
            {
                // xsi:type is already processed
                if (!Names.XSI_TYPE.equals(atts.getLocalName(i)))
                    _m.sattr(SDOEventModel.ATTR_XSI, atts.getLocalName(i), atts.getValue(i));
            }
            else
            {
                colon = aqn.indexOf(':');

                if (colon < 0)
                    _m.attr(atts.getURI(i), aqn, null,
                        atts.getValue(i));
                else
                {
                    _m.attr(atts.getURI(i), aqn.substring(colon + 1),
                        aqn.substring(0, colon),
                        atts.getValue(i));
                }
            }
        }
    }

    public void endElement(String namespaceURI, String localName,
        String qName)
        throws SAXException
    {
        _m.endElement();
        popNamespaceContext();
    }

    public void characters(char ch[], int start, int length)
        throws SAXException
    {
        _m.text(ch, start, length);
    }

    public void ignorableWhitespace(char ch[], int start, int length)
        throws SAXException
    {
    }

    public void comment(char ch[], int start, int length)
        throws SAXException
    {
    }

    public void processingInstruction(String target, String data)
        throws SAXException
    {
    }

    public void startDTD(String name, String publicId, String systemId)
        throws SAXException
    {
    }

    public void endDTD()
        throws SAXException
    {
    }

    public void startPrefixMapping(String prefix, String uri)
        throws SAXException
    {
        if (davos.sdo.impl.common.NamespaceStack.beginsWithXml(prefix) &&
            !("xml".equals(prefix) && Names.URI_XML.equals(uri)))
        {
            throw new SDOXmlException(SDOError.messageForCode("xml.prefix.malformed2",
                prefix));
        }
        if (!_nssupportContextPushed)
        {
            pushNamespaceContext();
            _nssupportContextPushed = true;
        }
        declarePrefix(prefix, uri);
    }

    public void endPrefixMapping(String prefix)
        throws SAXException
    {
    }

    public void skippedEntity(String name)
        throws SAXException
    {
    }

    public void startCDATA()
        throws SAXException
    {
    }

    public void endCDATA()
        throws SAXException
    {
    }

    public void startEntity(String name)
        throws SAXException
    {
//        throw new RuntimeException( "Not impl: startEntity" );
    }

    public void endEntity(String name)
        throws SAXException
    {
//        throw new RuntimeException( "Not impl: endEntity" );
    }

    public void setDocumentLocator(Locator locator)
    {
        _loc = locator;
    }

    //DeclHandler
    public void attributeDecl(String eName, String aName, String type, String valueDefault, String value)
    {
    }

    public void elementDecl(String name, String model)
    {
    }

    public void externalEntityDecl(String name, String publicId, String systemId)
    {
    }

    public void internalEntityDecl(String name, String value)
    {
    }

    //DTDHandler
    public void notationDecl(String name, String publicId, String systemId)
    {
    }

    public void unparsedEntityDecl(String name, String publicId, String systemId, String notationName)
    {
    }

    public void fatalError(SAXParseException e)
        throws SAXException
    {
        throw e;
    }

    public void error(SAXParseException e)
        throws SAXException
    {
        throw e;
    }

    public void warning(SAXParseException e)
        throws SAXException
    {
        throw e;
    }

    // ====================================
    // Validation code
    // ====================================
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
            SaxValidator v = new SaxValidator(this, this, tl, new SDOValidationErrorListener(false));
            _xr.setContentHandler(v);
        }
        else
        {
            // The validator may be still set from a previous validation operation
            _xr.setContentHandler(this);
        }
    }

    // ============================================
    // Inner class
    //=============================================
    static class PiccoloSaxLoader extends SaxLoader
    {
        private Piccolo _piccolo;

        private PiccoloSaxLoader(Piccolo p)
        {
            super(p);
            _piccolo = p;
        }

        static PiccoloSaxLoader newInstance()
        {
            Piccolo p = new Piccolo();
            try
            {
                p.setFeature(
                    "http://xml.org/sax/features/namespace-prefixes", true);
                p.setFeature("http://xml.org/sax/features/namespaces", true);
                p.setFeature("http://xml.org/sax/features/validation", false);
            }
            catch (Throwable e)
            {
                throw new SDOXmlException(e.getMessage(), e);
            }
            return new PiccoloSaxLoader(p);
        }

        // ==========================================
        // SAX handler implementation
        // ==========================================

        public void startElement(String uri, String local, String qName,
                Attributes atts)
                throws SAXException
        {
            // Piccolo does not error when a
            // namespace is used and not defined.
            // Check for these here

            if (qName.indexOf(':') >= 0 && uri.length() == 0)
            {
                throw new SDOXmlException(SDOError.messageForCode("xml.prefix.notdeclared",
                        qName.substring(0, qName.indexOf(':'))));
            }

            super.startElement(uri, local, qName, atts);
        }

        public void endDocument()
            throws SAXException
        {
            String xmlEncoding = null;
            String xmlVersion = null;
            try
            {
                xmlEncoding = _piccolo.getEncoding();
                xmlVersion  = _piccolo.getVersion();
            }
            catch (NullPointerException npe)
            {
                // In error cases, when the document could not be open, Piccolo will throw an NPE here
            }
            /* Heuristic: piccolo doesn't really tell us whether an XML decl
             was present or not, so assume that null encoding means no decl present */
            if (xmlEncoding != null || xmlVersion != null)
                _m.xmlDecl(xmlVersion, xmlEncoding);
        }
    }

    public static class UserDefinedSaxLoader extends SaxLoader
    {
        public UserDefinedSaxLoader(XMLReader r)
        {
            super(r);
        }
    }
}
