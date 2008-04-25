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
package davos.sdo.impl.helpers;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import davos.sdo.impl.marshal.*;
import davos.sdo.SDOContext;

import javax.sdo.DataObject;
import javax.sdo.helper.XMLDocument;
import javax.sdo.helper.XMLHelper;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Source;
import javax.xml.transform.Result;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

public class XMLHelperImpl implements XMLHelper
{
    private SDOContext _sdoContext;

    public XMLHelperImpl(SDOContext sdoContext)
    {
        _sdoContext = sdoContext;
    }

    /*
     * @see javax.sdo.helper.XMLHelper#load(java.lang.String)
     */
    public XMLDocument load(String inputString)
    {
        StringReader reader = new StringReader(inputString);
        try
        {
            return load(reader, null, null);
        }
        catch (IOException ioe)
        {
            throw new RuntimeException("StringReader should not throw IOException");
        }
    }

    /*
     * @see javax.sdo.helper.XMLHelper#load(java.io.InputStream)
     */
    public XMLDocument load(InputStream inputStream) throws IOException
    {
        return load(inputStream, null, null);
    }

    /*
     * @see javax.sdo.helper.XMLHelper#load(java.io.InputStream, java.lang.String, java.lang.Object)
     */
    public XMLDocument load(InputStream inputStream, String locationURI,
        Object options) throws IOException
    {
        XMLDocument result = getSaxLoader(options).load(inputStream, locationURI, options, _sdoContext);
        inputStream.close();
        return result;
    }

    /*
     * @see javax.sdo.helper.XMLHelper#load(java.io.Reader, java.lang.String, java.lang.Object)
     */
    public XMLDocument load(Reader inputReader, String locationURI,
        Object options) throws IOException
    {
        XMLDocument result = getSaxLoader(options).load(inputReader, locationURI, options, _sdoContext);
        inputReader.close();
        return result;
    }

    /*
     * @see javax.sdo.helper.XMLHelper#load(javax.xml.transform.Source, java.lang.String, java.lang.Object)
     * Supports the following classes of Source:
     * <ul>
     * <li>{@link javax.xml.transform.sax.SAXSource}</li>
     * <li>{@link javax.xml.transform.stream.StreamSource}</li>
     * <li>{@link javax.xml.transform.dom.DOMSource}</li>
     * </ul>
     */
    public XMLDocument load(Source inputSource, String locationURI, Object options)
        throws IOException
    {
        if (inputSource instanceof SAXSource)
        {
            SAXSource sSource = (SAXSource) inputSource;
            SaxLoader sl;
            if (sSource.getXMLReader() != null)
            {
                sl = new SaxLoader.UserDefinedSaxLoader(sSource.getXMLReader());
            }
            else
                sl = getSaxLoader(options);
            return sl.load(sSource.getInputSource(), locationURI, options, _sdoContext);
        }
        else if (inputSource instanceof StreamSource)
        {
            StreamSource sSource = (StreamSource) inputSource;
            if (sSource.getInputStream() != null)
                return load(sSource.getInputStream(), locationURI, options);
            else if (sSource.getReader() != null)
                return load(sSource.getReader(), locationURI, options);
            else if (sSource.getSystemId() != null)
            {
                URL url = new URL(sSource.getSystemId());
                return load(url.openStream(), locationURI, options);
            }
            else
                throw new IllegalArgumentException("StreamSource has the InputStream, Reader " +
                    "and SystemID all null.");
        }
        else if (inputSource instanceof DOMSource)
        {
            DOMSource dSource = (DOMSource) inputSource;
            DOMLoader dl = getDomLoader(options);
            // We ignore dSource.getSystemId() because we have locationURI anyway
            return dl.load(dSource.getNode(), locationURI, options, _sdoContext);
        }
        else
            throw new IllegalArgumentException("Unsupported Source type: " +
                inputSource.getClass().getName());
    }

    /*
     * @see javax.sdo.helper.XMLHelper#save(javax.sdo.DataObject, java.lang.String, java.lang.String)
     */
    public String save(DataObject dataObject, String rootElementURI,
        String rootElementName)
    {
        StringWriter sw = new StringWriter();
        try
        {
            getSaver(null, sw).save(dataObject, rootElementURI, rootElementName,
                false, null, null, null, null, null, _sdoContext);
        }
        catch (IOException ioe)
        {
            throw new RuntimeException("StringWriter should not throw IOException");
        }
        return sw.toString();
    }

    /*
     * @see javax.sdo.helper.XMLHelper#save(javax.sdo.DataObject, java.lang.String, java.lang.String, java.io.OutputStream)
     */
    public void save(DataObject dataObject, String rootElementURI,
        String rootElementName, OutputStream outputStream)
        throws IOException
    {
        getSaver(null, outputStream).save(dataObject, rootElementURI, rootElementName,
            false, null, null, null, null, null, _sdoContext);
        outputStream.flush();
    }

    /*
     * @see javax.sdo.helper.XMLHelper#save(javax.sdo.helper.XMLDocument, java.io.OutputStream, java.lang.Object)
     */
    public void save(XMLDocument xmlDocument, OutputStream outputStream,
        Object options) throws IOException
    {
        getSaver(options, outputStream).save(xmlDocument.getRootObject(), xmlDocument.getRootElementURI(),
            xmlDocument.getRootElementName(), xmlDocument.isXMLDeclaration(),
            xmlDocument.getXMLVersion(), xmlDocument.getEncoding(),
            xmlDocument.getSchemaLocation(), xmlDocument.getNoNamespaceSchemaLocation(),
            options, _sdoContext);
        outputStream.flush();
    }

    /*
     * @see javax.sdo.helper.XMLHelper#save(javax.sdo.helper.XMLDocument, java.io.Writer, java.lang.Object)
     */
    public void save(XMLDocument xmlDocument, Writer outputWriter,
        Object options) throws IOException
    {
        getSaver(options, outputWriter).save(xmlDocument.getRootObject(), xmlDocument.getRootElementURI(),
            xmlDocument.getRootElementName(), xmlDocument.isXMLDeclaration(),
            xmlDocument.getXMLVersion(), xmlDocument.getEncoding(),
            xmlDocument.getSchemaLocation(), xmlDocument.getNoNamespaceSchemaLocation(),
            options, _sdoContext);
        outputWriter.flush();
    }

    /*
     * @see javax.sdo.helper.XMLHelper#save(javax.sdo.helper.XMLDocument, javax.xml.transform.Result, java.lang.Object)
     * Supports the following classes of Result:
     * <ul>
     * <li>{@link javax.xml.transform.sax.SAXResult}</li>
     * <li>{@link javax.xml.transform.stream.StreamResult}</li>
     * <li>{@link javax.xml.transform.dom.DOMResult}</li>
     * </ul>
     */
    public void save(XMLDocument xmlDocument, Result outputResult, Object options)
        throws IOException
    {
        if (outputResult instanceof SAXResult)
        {
            SAXResult sResult = (SAXResult) outputResult;
            Saver s = new SaxSaver(sResult.getHandler());
            s.save(xmlDocument.getRootObject(), xmlDocument.getRootElementURI(),
                xmlDocument.getRootElementName(), xmlDocument.isXMLDeclaration(),
                xmlDocument.getXMLVersion(), xmlDocument.getEncoding(),
                xmlDocument.getSchemaLocation(), xmlDocument.getNoNamespaceSchemaLocation(),
                options, _sdoContext);
        }
        else if (outputResult instanceof StreamResult)
        {
            StreamResult sResult = (StreamResult) outputResult;
            if (sResult.getOutputStream() != null)
                save(xmlDocument, sResult.getOutputStream(), options);
            else if (sResult.getWriter() != null)
                save(xmlDocument, sResult.getWriter(), options);
            else if (sResult.getSystemId() != null)
            {
                // Assume that the SystemID refers to a URI that is writeable, such as a file
                OutputStream os = null;
                try
                {
                    URI uri = new URI(sResult.getSystemId());
                    os = uri.toURL().openConnection().getOutputStream();
                }
                catch (URISyntaxException use)
                {
                }
                if (os != null)
                    save(xmlDocument, os, options);
                else
                    throw new IllegalArgumentException("The SystemID of the StreamResult object ("+
                        sResult.getSystemId() + ") is not a valid URI");
            }
            else
                throw new IllegalArgumentException("The StreamResult provided has the " +
                    "OutputStream, Writer and SystemId all null");
        }
        else if (outputResult instanceof DOMResult)
        {
            DOMResult dResult = (DOMResult) outputResult;
            DOMSaver s = new DOMSaver(dResult.getNode(), dResult.getNextSibling());
            s.save(xmlDocument.getRootObject(), xmlDocument.getRootElementURI(),
                xmlDocument.getRootElementName(), xmlDocument.isXMLDeclaration(),
                xmlDocument.getXMLVersion(), xmlDocument.getEncoding(),
                xmlDocument.getSchemaLocation(), xmlDocument.getNoNamespaceSchemaLocation(),
                options, _sdoContext);
            if (dResult.getNode() == null)
                dResult.setNode(s.getRootNode());
        }
        else
            throw new IllegalArgumentException("Unsupported Result type: "+
                outputResult.getClass().getName());
    }

    /*
     * @see javax.sdo.helper.XMLHelper#createDocument(javax.sdo.DataObject, java.lang.String, java.lang.String)
     */
    public XMLDocument createDocument(DataObject dataObject,
        String rootElementURI, String rootElementName)
    {
        return new XMLDocumentImpl(dataObject, rootElementURI, rootElementName);
    }

    /*
     * Adding support for StAX as implementation-specific methods, because the specific Source and
     * Result interfaces (javax.xml.transform.stax.StAXSource and javax.xml.transform.stax.StAXResult)
     * are not present in JDK1.5 or in jsr173_api.jar; they are present in JDK1.6
     */
    public XMLDocument load(XMLStreamReader xmlReader, String locationURI, Object options)
        throws IOException
    {
        StAXLoader sl = getStAXLoader(options);
        try
        {
            return sl.load(xmlReader, locationURI, options, _sdoContext);
        }
        catch (XMLStreamException xse)
        {
            throw new RuntimeException(xse);
        }
    }

    public void save(XMLDocument xmlDocument, XMLStreamWriter xmlWriter, Object options)
        throws IOException
    {
        StAXSaver s = new StAXSaver(xmlWriter);
        s.save(xmlDocument.getRootObject(), xmlDocument.getRootElementURI(),
            xmlDocument.getRootElementName(), xmlDocument.isXMLDeclaration(),
            xmlDocument.getXMLVersion(), xmlDocument.getEncoding(),
            xmlDocument.getSchemaLocation(), xmlDocument.getNoNamespaceSchemaLocation(),
            options, _sdoContext);
    }

    public XMLDocument load(XMLEventReader xmlReader, String locationURI, Object options)
        throws IOException
    {
        StAXLoader sl = getStAXLoader(options);
        try
        {
            return sl.load(xmlReader, locationURI, options, _sdoContext);
        }
        catch (XMLStreamException xse)
        {
            throw new RuntimeException(xse);
        }
    }

    public void save(XMLDocument xmlDocument, XMLEventWriter xmlWriter, Object options)
        throws IOException
    {
        StAXSaver s = new StAXSaver(xmlWriter);
        s.save(xmlDocument.getRootObject(), xmlDocument.getRootElementURI(),
            xmlDocument.getRootElementName(), xmlDocument.isXMLDeclaration(),
            xmlDocument.getXMLVersion(), xmlDocument.getEncoding(),
            xmlDocument.getSchemaLocation(), xmlDocument.getNoNamespaceSchemaLocation(),
            options, _sdoContext);
    }

    public XMLStreamReader save(XMLDocument xmlDocument, Object options)
    {
        XMLStreamReaderMarshaller result = new XMLStreamReaderMarshaller(options);
        result.save(xmlDocument.getRootObject(), xmlDocument.getRootElementURI(),
            xmlDocument.getRootElementName(), xmlDocument.isXMLDeclaration(),
            xmlDocument.getXMLVersion(), xmlDocument.getEncoding(),
            xmlDocument.getSchemaLocation(), xmlDocument.getNoNamespaceSchemaLocation(),
            options, _sdoContext);
        return StAXLoader.wrapWithValidator(result, options, _sdoContext, true);
    }

    private SaxLoader getSaxLoader(Object options)
    {
        // We can configure different parsers here
        // For now, just use Piccolo
        return Cache.get().getPiccoloSaxLoader();
    }

    private DOMLoader getDomLoader(Object options)
    {
        // We don't worry about it for the moment and return a new object since it is light
        return new DOMLoader();
    }

    private StAXLoader getStAXLoader(Object options)
    {
        // We don't worry about it for the moment and return a new object since it is light
        return new StAXLoader();
    }

    private Saver getSaver(Object options, OutputStream os)
    {
        // We can configure different styles of savers here
        // For now, we just have a default one
        return new WriterSaver(os);
    }

    private Saver getSaver(Object options, Writer w)
    {
        // We can configure different styles of savers here
        // For now, we just have a default one
        return new WriterSaver(w);
    }
}
