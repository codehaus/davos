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
import java.util.Iterator;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.Namespace;
import javax.xml.stream.events.StartDocument;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.XmlOptions;

import davos.sdo.Options;
import davos.sdo.SDOContext;
import davos.sdo.SDOError;
import davos.sdo.SDOXmlException;
import davos.sdo.impl.common.Common;
import davos.sdo.impl.common.Names;

import javax.sdo.helper.XMLDocument;

public class StAXLoader extends NamespaceSupportDelegator
    implements Loader, NamespaceHandler
{
    Unmarshaller _m;
    XMLStreamReader _xsr;
    LocationToLocator _locator;

    public StAXLoader()
    {
    }

    public XMLDocument load(XMLStreamReader xmlReader, String locationURI, Object options, SDOContext sdoctx)
        throws IOException, XMLStreamException
    {
        XMLDocumentImpl root = new XMLDocumentImpl();
        _m = Unmarshaller.get(root, options, sdoctx);
        _m.setLoader(this);
        _m.setNamespaceHandler(this);
        _locator = new LocationToLocator(xmlReader.getLocation());
        _m.setLocator(_locator);
        processStream(wrapWithValidator(xmlReader, options, sdoctx, false));
        _m.finish();
        return root;
    }

    public XMLDocument load(XMLEventReader xmlReader, String locationURI, Object options, SDOContext sdoctx)
        throws IOException, XMLStreamException
    {
        XMLDocumentImpl root = new XMLDocumentImpl();
        _m = Unmarshaller.get(root, options, sdoctx);
        _m.setLoader(this);
        _m.setNamespaceHandler(this);
        _locator = new LocationToLocator(null);
        _m.setLocator(_locator);
        processStream(xmlReader);
        _m.finish();
        return root;
    }

    private void processStream(XMLStreamReader xsr)
        throws IOException, XMLStreamException
    {
        int depth = 0;
        _xsr = xsr;
        events:
        for (int eventType = xsr.getEventType(); ; eventType = xsr.next())
        {
            switch (eventType)
            {
            case XMLStreamReader.START_DOCUMENT:
                depth++;

                String encoding = xsr.getCharacterEncodingScheme();
                String version = xsr.getVersion();

                if (encoding != null || version != null)
                    _m.xmlDecl(version, encoding);
                break;

            case XMLStreamReader.END_DOCUMENT:
                depth--;

                break events;

            case XMLStreamReader.START_ELEMENT:
                depth++;

                String xsiTypeString = xsr.getAttributeValue(Names.URI_XSD_INSTANCE, Names.XSI_TYPE);

                String xsiTypeURI = null, xsiTypeName = null;
                if (xsiTypeString != null && xsiTypeString.length() > 0)
                {
                    int colonIndex = xsiTypeString.indexOf(':');
                    if (colonIndex < 0)
                    {
                        xsiTypeURI = getNamespaceURI(Common.EMPTY_STRING);
                        xsiTypeName = xsiTypeString;
                    }
                    else
                    {
                        xsiTypeURI = getNamespaceURI(xsiTypeString.substring(0, colonIndex));
                        if (xsiTypeURI == null)
                            throw new SDOXmlException(SDOError.messageForCode(
                                "xml.prefix.notdeclared.xsitype",
                                xsiTypeString.substring(0, colonIndex), xsiTypeString));
                        xsiTypeName = xsiTypeString.substring(colonIndex + 1);
                    }
                }

                _m.startElement(xsr.getNamespaceURI(), xsr.getLocalName(), xsr.getPrefix(), xsiTypeURI, xsiTypeName);

                /* TODO(radup) Figure out if we really need to bother with the
                 * NamespaceSupport, which is quite slow. The only reason we need it is
                 * because Xpath.compile() takes a Map<String, String> as argument; if it
                 * took a NamespaceContext, then it would not be necessary
                 */
                /*
                 * Another problem with this is that if there are additional namespaces in
                 * scope already declared, before this started, then there is no way to
                 * get to them
                 */
                pushNamespaceContext();
                int n = xsr.getNamespaceCount();

                for (int a = 0; a < n; a++)
                {
                    String prefix = xsr.getNamespacePrefix(a);
                    String uri = xsr.getNamespaceURI(a);

                    declarePrefix(prefix, uri);
                }

                n = xsr.getAttributeCount();

                for (int a = 0; a < n; a++)
                {
                    String attrUri = xsr.getAttributeNamespace(a);
                    String attrLocalName = xsr.getAttributeLocalName(a);

                    if (Names.URI_XSD_INSTANCE.equals(attrUri))
                        if (Names.XSI_TYPE.equals(attrLocalName))
                            ;
                        else
                            _m.sattr(SDOEventModel.ATTR_XSI, attrLocalName, xsr.getAttributeValue(a));
                    else
                        // Add attribute to list
                        _m.attr(attrUri, attrLocalName, xsr.getAttributePrefix(a), xsr.getAttributeValue(a));
                }

                break;

            case XMLStreamReader.END_ELEMENT:
                depth--;
                _m.endElement();
                popNamespaceContext();

                break;

            case XMLStreamReader.CHARACTERS:
            case XMLStreamReader.CDATA:
                _m.text(xsr.getTextCharacters(), xsr.getTextStart(), xsr.getTextLength());

                break;

            case XMLStreamReader.COMMENT:
            case XMLStreamReader.PROCESSING_INSTRUCTION:
                // Ignoring those in SDO
                break;

            case XMLStreamReader.ATTRIBUTE:
            case XMLStreamReader.NAMESPACE:
                // According to the spec, these can only occur when results of a query
                // but in SDO we only support well-formed XML because we have to
                // return a DataObject
                break;

            case XMLStreamReader.ENTITY_REFERENCE:
            case XMLStreamReader.SPACE:
            case XMLStreamReader.DTD:
                // Ignoring those as well
                break;

            default :
                throw new RuntimeException(
                    "Unhandled xml event type: " + eventType);
            }

            if (!xsr.hasNext() || depth <= 0)
                break;
        }
        _xsr = null;
    }

    private static QName XSI_TYPE = new QName(Names.URI_XSD_INSTANCE, Names.XSI_TYPE); 

    private void processStream(XMLEventReader xer)
        throws IOException, XMLStreamException
    {
        int depth = 0;
        int startDepth = 0;
        events:
        for (XMLEvent xe = xer.peek(); ; xe = xer.nextEvent())
        {
            _locator.setNewLocation(xe.getLocation());
            switch (xe.getEventType())
            {
            case XMLEvent.START_DOCUMENT:
                depth++;
                StartDocument doc = (StartDocument) xe;

                String encoding = doc.getCharacterEncodingScheme();
                String version = doc.getVersion();

                if (encoding != null || version != null)
                    _m.xmlDecl(version, encoding);
                break;

            case XMLEvent.END_DOCUMENT:
                depth--;

                break events;

            case XMLEvent.START_ELEMENT:
                depth++;
                if (startDepth == 0)
                    startDepth = depth;
                StartElement sexe = xe.asStartElement();
                Attribute xsiType = sexe.getAttributeByName(XSI_TYPE);

                String xsiTypeURI = null, xsiTypeName = null;
                if (xsiType != null)
                {
                    String xsiTypeString = xsiType.getValue();
                    int colonIndex = xsiTypeString.indexOf(':');
                    if (colonIndex < 0)
                    {
                        xsiTypeURI = getNamespaceURI(Common.EMPTY_STRING);
                        xsiTypeName = xsiTypeString;
                    }
                    else
                    {
                        xsiTypeURI = getNamespaceURI(xsiTypeString.substring(0, colonIndex));
                        if (xsiTypeURI == null)
                            throw new SDOXmlException(SDOError.messageForCode(
                                "xml.prefix.notdeclared.xsitype",
                                xsiTypeString.substring(0, colonIndex), xsiTypeString));
                        xsiTypeName = xsiTypeString.substring(colonIndex + 1);
                    }
                }

                QName name = sexe.getName();
                _m.startElement(name.getNamespaceURI(), name.getLocalPart(), name.getPrefix(), xsiTypeURI, xsiTypeName);

                // Same comments as in processStream(XMLStreamReader)
                pushNamespaceContext();
                for (Iterator i = sexe.getNamespaces(); i.hasNext();)
                {
                    Namespace ns = (Namespace) i.next();
                    String prefix = ns.getPrefix();
                    String uri = ns.getNamespaceURI();

                    declarePrefix(prefix, uri);
                    _m.xmlns(prefix, uri);
                }

                for (Iterator i = sexe.getAttributes(); i.hasNext();)
                {
                    Attribute attr = (Attribute) i.next();

                    QName attrName = attr.getName();
                    String attrUri = attrName.getNamespaceURI();
                    String attrLocalName = attrName.getLocalPart();

                    if (Names.URI_XSD_INSTANCE.equals(attrUri))
                        if (Names.XSI_TYPE.equals(attrLocalName))
                            ;
                        else
                            _m.sattr(SDOEventModel.ATTR_XSI, attrLocalName, attr.getValue());
                    else
                        // Add attribute to list
                        _m.attr(attrUri, attrLocalName, attrName.getPrefix(), attr.getValue());
                }

                break;

            case XMLEvent.END_ELEMENT:
                depth--;
                _m.endElement();
                popNamespaceContext();

                break;

            case XMLEvent.CHARACTERS:
            case XMLEvent.CDATA:
                Characters cexe = xe.asCharacters();

                // For some reason, it looks like using this interface CHARACTERS events can be reported
                // before the root element instead of SPACE events (like characters inbetween the DTD and
                // the root element)
                // SDOEventModel requires that no text() events are sent before at least one startElement
                // was sent
                if (depth >= startDepth && startDepth > 0)
                    _m.text(cexe.getData());

                break;

            case XMLEvent.COMMENT:
            case XMLEvent.PROCESSING_INSTRUCTION:
                // Ignoring those in SDO
                break;

            case XMLEvent.ATTRIBUTE:
            case XMLEvent.NAMESPACE:
                // According to the spec, these can only occur when results of a query
                // but in SDO we only support well-formed XML because we have to
                // return a DataObject
                break;

            case XMLEvent.ENTITY_REFERENCE:
            case XMLEvent.SPACE:
            case XMLEvent.DTD:
                // Ignoring those as well
                break;

            default :
                throw new RuntimeException(
                    "Unhandled xml event type: " + xe.toString());
            }

            if (!xer.hasNext() || depth <= 0)
                break;
        }
    }

    public void changeUnmarshaller(Unmarshaller m)
    {
        _m = m;
    }

    public String getNamespaceURI(String prefix)
    {
        if (_xsr != null)
            return _xsr.getNamespaceContext().getNamespaceURI(prefix);
        else
            return super.getNamespaceURI(prefix);
    }

    public String getPrefix(String namespaceURI)
    {
        if (_xsr != null)
            return _xsr.getNamespaceContext().getPrefix(namespaceURI);
        else
            return super.getPrefix(namespaceURI);
    }

    public Iterator getPrefixes(String namespaceURI)
    {
        if (_xsr != null)
            return _xsr.getNamespaceContext().getPrefixes(namespaceURI);
        else
            return super.getPrefixes(namespaceURI);
    }

    public static XMLStreamReader wrapWithValidator(XMLStreamReader xsr, Object options,
        SDOContext sdoCtx, boolean marshal)
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
            org.apache.xmlbeans.impl.validator.ValidatingXMLStreamReader validator = 
                new org.apache.xmlbeans.impl.validator.ValidatingXMLStreamReader();
            SchemaTypeLoader tl = sdoCtx.getTypeSystem().getSchemaTypeLoader(); 
            validator.init(xsr, true, null, tl, null, new SDOExceptionListener(marshal));
            xsr = validator;
        }
        return xsr;
    }
}
