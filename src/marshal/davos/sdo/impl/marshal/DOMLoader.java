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

import javax.sdo.helper.XMLDocument;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import davos.sdo.SDOContext;
import davos.sdo.SDOError;
import davos.sdo.SDOXmlException;
import davos.sdo.impl.common.Common;
import davos.sdo.impl.common.Names;

/**
 * DOMLoader class walks over a DOM Node representing the Document or an Element and turns it
 * into a string of SDOEvents
 */
public class DOMLoader implements Loader
{
    Unmarshaller _m;
    private Node _currentNode;
    /**
     * We are not relying on the DOM implementation to provide NamespaceContext-like methods
     * like lookupNamespaceURI() because these lookup* methods are DOM Level 3, and XmlBeans
     * doesn't support DOM Level 3, so if we are unmarshalling from an XmlBeans DOM it would fail
     */
    private NamespaceSupportDelegator _ns = new NamespaceSupportDelegator();

    public XMLDocument load(Node n, String locationURI, Object options, SDOContext sdoctx)
    {
        XMLDocumentImpl root = new XMLDocumentImpl();
        _m = Unmarshaller.get(root, options, sdoctx);
        _m.setLoader(this);
        _m.setNamespaceHandler(_ns);
        if (n instanceof Document)
        {
            Document d = (Document) n;
            String xmlVersion = null;
            String xmlEncoding = null;
            try
            {
                xmlVersion = d.getXmlVersion();
                xmlEncoding = d.getXmlEncoding();
            }
            catch (RuntimeException e)
            {
                // Unsupported on the DOM impl, we'll leave them null
            }
            if (xmlEncoding != null || (xmlVersion != null && !xmlVersion.equals("1.0")))
            {
                _m.xmlDecl(xmlVersion, xmlEncoding);
            }
            Element rootElement = d.getDocumentElement();
            processElement(rootElement);
        }
        else if (n instanceof Element)
            processElement((Element) n);
        else
            throw new IllegalArgumentException("Can only process Document or Element Nodes: " +
                n.getClass().getName());
        _m.finish();
        return root;
    }

    public void processElement(Element e)
    {
        final Node parentNode = _currentNode;
        _currentNode = e;
        // Push namespace declarations
        _ns.pushNamespaceContext();
        // We need to walk through the attribute list twice
        NamedNodeMap attributes = e.getAttributes();
        for (int i = 0; i < attributes.getLength(); i++)
        {
            Node attr = attributes.item(i);
            String qnameAttr = attr.getNodeName();

            if (qnameAttr.startsWith(Names.XMLNS))
            {
                String uri = attr.getNodeValue();
                int colon = qnameAttr.lastIndexOf(':');
                String prefix = (colon > 0) ? qnameAttr.substring(colon + 1) :
                    Common.EMPTY_STRING;
                _ns.declarePrefix(prefix, uri);
            }
        }

        // Get any xsi:type attribute
        String xsiTypeString = e.getAttributeNS(Names.URI_XSD_INSTANCE, Names.XSI_TYPE);
        String xsiTypeUri = null, xsiTypeName = null;
        if (xsiTypeString != null && xsiTypeString.length() > 0)
        {
            int colonIndex = xsiTypeString.indexOf(':');
            if (colonIndex < 0)
            {
                xsiTypeUri = _ns.getNamespaceURI(null);
                xsiTypeName = xsiTypeString;
            }
            else
            {
                xsiTypeUri = _ns.getNamespaceURI(xsiTypeString.substring(0, colonIndex));
                if (xsiTypeUri == null)
                    throw new SDOXmlException(SDOError.messageForCode(
                        "xml.prefix.notdeclared.xsitype",
                        xsiTypeString.substring(0, colonIndex), xsiTypeString));
                xsiTypeName = xsiTypeString.substring(colonIndex + 1);
            }
        }
        String nsUri = e.getNamespaceURI();
        _m.startElement(nsUri == null ? Common.EMPTY_STRING : nsUri, e.getLocalName(),
            e.getPrefix(), xsiTypeUri, xsiTypeName);
        for (int i = 0; i < attributes.getLength(); i++)
        {
            Node attr = attributes.item(i);
            String qnameAttr = attr.getNodeName();

            if (qnameAttr.startsWith(Names.XMLNS))
            {
                String uri = attr.getNodeValue();
                int colon = qnameAttr.lastIndexOf(':');
                String prefix = (colon > 0) ? qnameAttr.substring(colon + 1) :
                    Common.EMPTY_STRING;
                _m.xmlns(prefix, uri);
            }
            else
            {
                String uriAttr = attr.getNamespaceURI();
                String localNameAttr = attr.getLocalName();

                if (uriAttr == null)
                    _m.attr(Common.EMPTY_STRING, localNameAttr, attr.getPrefix(), attr.getNodeValue());
                else if (Names.URI_XSD_INSTANCE.equals(uriAttr))
                    if (Names.XSI_TYPE.equals(localNameAttr))
                        ;
                    else
                        _m.sattr(SDOEventModel.ATTR_XSI, localNameAttr, attr.getNodeValue());
                else
                    // Add attribute to list
                    _m.attr(uriAttr, localNameAttr, attr.getPrefix(), attr.getNodeValue());
            }
        }
        Node child = e.getFirstChild();
        while (child != null)
        {
            switch (child.getNodeType())
            {
            case Node.ATTRIBUTE_NODE:
                // already processed, should not be returned anyway
                break;
            case Node.DOCUMENT_FRAGMENT_NODE:
            case Node.DOCUMENT_TYPE_NODE :
            case Node.ENTITY_NODE :
            case Node.ENTITY_REFERENCE_NODE:
            case Node.NOTATION_NODE :
                // These node types are ignored!!!
                break;
            case Node.CDATA_SECTION_NODE:
                final String cdata = child.getNodeValue();
                _m.text(cdata);
                break;
            case Node.COMMENT_NODE:
            case Node.DOCUMENT_NODE:
                // We don't handle those either at this point
                break;
            case Node.ELEMENT_NODE:
                processElement((Element) child);
                break;
            case Node.PROCESSING_INSTRUCTION_NODE:
                // We don't handle processing instructions
                break;
            case Node.TEXT_NODE:
                final String data = child.getNodeValue();
                _m.text(data);
                break;
            }
            child = child.getNextSibling();
        }
        _m.endElement();
        _ns.popNamespaceContext();
        _currentNode = parentNode;
    }

    public void changeUnmarshaller(Unmarshaller m)
    {
        _m = m;
    }
}
