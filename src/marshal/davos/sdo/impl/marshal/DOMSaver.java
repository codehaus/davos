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

import org.w3c.dom.Node;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.util.Stack;

import davos.sdo.impl.common.NamespaceStack;
import davos.sdo.impl.common.Names;

/**
 * User: radup
 * Date: Mar 2, 2007
 */
public class DOMSaver extends Saver
{
    private Node _root;
    private Node _nextSibling;
    private Document _document;
    private Stack<Element> _stack;
    private NamespaceStack _nsstack = new NamespaceStack(null, false);
    private boolean _nsAdded = true;

    public DOMSaver(Node parent, Node nextSibling)
    {
        if (parent == null)
        {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            // We create a Document using the default builder factory
            dbf.setNamespaceAware(true);
            try
            {
                _document = dbf.newDocumentBuilder().newDocument();
            }
            catch (ParserConfigurationException pce)
            {
                throw new IllegalStateException(pce);
            }
            _root = _document;
        }
        else if (parent instanceof Document)
        {
            _document = (Document) parent;
            _root = parent;
        }
        else
        {
            _root = parent;
            _document = parent.getOwnerDocument();
        }
        _nextSibling = nextSibling;
        _stack = new Stack<Element>();
    }

    public Node getRootNode()
    {
        return _root;
    }

    public void startElement(String uri, String name, String prefix, String xsiTypeUri, String xsiTypeName)
    {
        if (!_nsAdded)
            addNsDeclarations();
        _nsstack.pushMappings(false);
        prefix = _nsstack.ensureMapping(uri, prefix, false, true);
        Element elem = _document.createElementNS(uri, prefix == null || prefix.length() == 0 ?
            name : prefix + ':' + name);

        if (xsiTypeName != null)
        {
            String xsiPrefix = _nsstack.ensureMapping(xsiTypeUri, null, false, false);
            String schemaInstancePrefix = _nsstack.ensureMapping(Names.URI_XSD_INSTANCE,
                Names.PREFIX_XSD_INSTANCE, false, false);
            String value = xsiPrefix == null ? xsiTypeName : xsiPrefix + ':' + xsiTypeName;
            elem.setAttributeNS(Names.URI_XSD_INSTANCE, schemaInstancePrefix + ':' + Names.XSI_TYPE,
                value);
        }

        // Append this new node onto current stack node
        if (_stack.size() == 0)
        {
            // It's the first element created, so attach it directly to the root
            if (_nextSibling != null)
                _root.insertBefore(elem, _nextSibling);
            else
                _root.appendChild(elem);
        }
        else
        {
            Element last = _stack.peek();
            last.appendChild(elem);
        }

        // Push this node onto stack
        _stack.push(elem);
        _nsAdded = false;
    }

    public void endElement()
    {
        if (!_nsAdded)
            addNsDeclarations();
        _stack.pop();
        _nsstack.popMappings();
    }

    public void attr(String uri, String local, String prefix, String value)
    {
        Element elem = _stack.peek();
        prefix = _nsstack.ensureMapping(uri, prefix, false, true);
        // Add attribute to element
        elem.setAttributeNS(uri, prefix == null || prefix.length() == 0 ? local :
            prefix + ':' + local, value);
    }

    public void xmlns(String prefix, String uri)
    {
        Element elem = _stack.peek();
        elem.setAttributeNS(Names.URI_DOM_XMLNS, prefix == null ? Names.XMLNS : Names.XMLNS + ':' +
            prefix, uri);
    }

    public void sattr(int type, String name, String value)
    {
        Element elem = _stack.peek();
        switch (type)
        {
        case SDOEventModel.ATTR_XSI:
            String attPrefix = _nsstack.ensureMapping(Names.URI_XSD_INSTANCE,
                Names.PREFIX_XSD_INSTANCE, false, true);
            elem.setAttributeNS(Names.URI_XSD_INSTANCE, attPrefix + ':' + name, value);
            break;
        }
    }

    public void text(char[] buff, int off, int cch)
    {
        text(new String(buff, off, cch));
    }

    public void text(String s)
    {
        if (!_nsAdded)
            addNsDeclarations();
        Element elem = _stack.peek();
        elem.appendChild(_document.createTextNode(s));
    }

    public void xmlDecl(String version, String encoding)
    {
        if (_document == _root && version != null)
            _document.setXmlVersion(version);
    }

    public NamespaceStack getNamespaceStack()
    {
        return _nsstack;
    }

    private void addNsDeclarations()
    {
        Element elem = _stack.peek();
        for (_nsstack.iterateMappings(); _nsstack.hasMapping(); _nsstack.nextMapping())
        {
            String prefix = _nsstack.mappingPrefix();
            String uri = _nsstack.mappingUri();

            if (prefix == null || prefix.length() == 0)
                elem.setAttributeNS(Names.URI_DOM_XMLNS, Names.XMLNS, uri);
            else
                elem.setAttributeNS(Names.URI_DOM_XMLNS, Names.XMLNS + ':' + prefix, uri);
        }
        _nsAdded = true;
    }
}
