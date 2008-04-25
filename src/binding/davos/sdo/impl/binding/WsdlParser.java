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
package davos.sdo.impl.binding;

import davos.sdo.binding.BindingSystem;
import davos.sdo.impl.common.Names;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.SchemaTypeSystem;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.impl.xb.xsdschema.ComplexType;
import org.apache.xmlbeans.impl.xb.xsdschema.SchemaDocument;
import org.apache.xmlbeans.impl.xb.xsdschema.TopLevelComplexType;
import org.apache.xmlbeans.impl.xb.xsdschema.TopLevelSimpleType;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Iterator;
import java.util.HashSet;
import java.util.Set;

/**
 * User: radup
 * Date: Mar 25, 2007
 * Time: 6:02:58 PM
 * This class is used to extract Schema information from a WSDL file.
 * The code is based on the WLS9.2 WSEE code, as opposed to the WSDL parsing code in SDOCompiler,
 * which is based on XmlBeans
 */
public class WsdlParser
{
    public static final String DEFINITIONS = "definitions";
    public static final String wsdlNS = "http://schemas.xmlsoap.org/wsdl/";
    public static final String schemaNS = Names.URI_XSD;
    private static final DocumentBuilder parser;
    //
    // list of schemas considered "built-in" to the WSDL
    //
    private static final String[] BASE_SCHEMA_NAMES = new String[] {
        "/davos/sdo/impl/binding/wsdl/soap-encoding-11.xsd"
    };

    //
    // compiled set of schemas considered as "built-in"
    //
    private static final List<SchemaDocument> BASE_SCHEMAS;

    private final String wsdlLocation;
    private String typesLocation;
    private List<WsdlSchema> schemas;

    public static WsdlParser newInstance(String wsdlLocation)
    {
        return new WsdlParser(wsdlLocation);
    }

    private WsdlParser(String wsdlLocation)
    {
        this.wsdlLocation = wsdlLocation;
        schemas = new ArrayList<WsdlSchema>(5);
    }

    static
    {
        try
        {
            DocumentBuilderFactory db = DocumentBuilderFactory.newInstance();
            db.setNamespaceAware(true);
            parser = db.newDocumentBuilder();
        }
        catch (javax.xml.parsers.ParserConfigurationException pce)
        {
            throw new IllegalStateException();
        }
        List<SchemaDocument> schemas = new ArrayList<SchemaDocument>(BASE_SCHEMA_NAMES.length);
        for(String name : BASE_SCHEMA_NAMES)
        {
            try
            {
                InputStream is = WsdlParser.class.getResourceAsStream(name);
                SchemaDocument schemaDoc = SchemaDocument.Factory.parse(is);
                schemas.add(schemaDoc);
            }
            catch(Exception e)
            {
                throw new IllegalStateException("Could not find one of the built-in Schemas: " +
                    name);
            }
        }
        BASE_SCHEMAS = schemas;
    }

    public void parse() throws IOException, org.xml.sax.SAXException, WsdlException
    {
        parse(parser.parse(wsdlLocation));
    }

    public void parse(Document document)
        throws IOException, WsdlException
    {
        Element definitionsEle = document.getDocumentElement();
        if (definitionsEle == null)
            throw new IllegalArgumentException("Invalid Wsdl file found. ");

        checkWsdlDefinitions(DEFINITIONS, definitionsEle);
        checkWsdlNamespace(definitionsEle);
        parse(definitionsEle, /*targetNS*/ null);
    }

    public void parse(Element element, String targetNS)
        throws IOException, WsdlException
    {
//      addDocumentation(element);
//      parseAttributes(element, targetNS);
      NodeList nodes = element.getChildNodes();

      for (int i = 0; i < nodes.getLength(); i++)
      {
          Node node = nodes.item(i);

          if (isWhiteSpace(node))
              continue;

          checkDomElement(node);

          String name = node.getLocalName();
          if ("types".equals(name))
          {
              parseWsdlTypes((Element) node, wsdlLocation);
          }
//        WsdlExtension extension = parseChild((Element)node, targetNS);
//        putExtension(extension);
      }
    }

    public void parseWsdlTypes(Element element, String wsdlLocation)
        throws IOException, WsdlException
    {
//        addDocumentation(element);
        NodeList nodes = element.getChildNodes();
        Map parentNamespaceDefs = new HashMap();
        collectNamespaces(element, parentNamespaceDefs);

//        if (verbose)
//            Verbose.log("Collected namespaces ...." + parentNamespaceDefs);

        typesLocation = wsdlLocation;

        for (int i = 0; i < nodes.getLength(); i++)
        {
            Node node = nodes.item(i);
            if (!isDocumentation(node) && node instanceof Element)
            {
                setNamespaces((Element) node, parentNamespaceDefs);
                WsdlSchema schema = new WsdlSchema();
                schema.parse((Element) node, wsdlLocation);
                addSchema(schema);
            }
        }
    }

    public void addSchema(WsdlSchema schema)
    {
        schemas.add(schema);
    }

    public SchemaTypeSystem generateSchemaTypeSystem(BindingSystem bs) throws XmlException
    {
        SchemaTypeLoader stl = bs.getTypeSystem().getSchemaTypeLoader();
        // check if they contain the built-in types
        ArrayList<String> tnsList = new ArrayList<String>();
        ArrayList<SchemaDocument> schemaDocuments = new ArrayList<SchemaDocument>();
        for (Iterator<WsdlSchema> iterator = schemas.iterator(); iterator.hasNext();)
        {
            SchemaDocument schema = iterator.next().getSchema();
            schemaDocuments.add(schema);
            tnsList.add(schema.getSchema().getTargetNamespace());
        }

        // add built-ins - but only if not already there
        for(SchemaDocument schemadoc : BASE_SCHEMAS)
        {
            String targetNamespace = schemadoc.getSchema().getTargetNamespace(); 
            if (!tnsList.contains(targetNamespace))
            {
                // Also check if the schema is already defined in the BindingSystem
                // Heuristic: get the first global type and get its name, then search the
                // BindingSystem for that name
                String theName = null;
                TopLevelComplexType[] ct = schemadoc.getSchema().getComplexTypeArray();
                if (ct.length > 0)
                    theName = ct[0].getName();
                else
                {
                    TopLevelSimpleType[] st = schemadoc.getSchema().getSimpleTypeArray();
                    if (st.length > 0)
                        theName = st[0].getName();
                }
                if (theName == null || bs.loadTypeBySchemaTypeName(targetNamespace, theName) == null)
                    schemaDocuments.add(schemadoc);
            }
        }

        if (stl == null)
            stl = XmlBeans.getBuiltinTypeSystem();
        SchemaDocument[] schemaArray = schemaDocuments.toArray(new SchemaDocument[schemas.size()]);
        SchemaTypeSystem sts = compile(schemaArray, stl);
        return sts;
    }

    /**
     * compile the list of schemas into a type system
     */
    private SchemaTypeSystem compile(SchemaDocument[] schemas, SchemaTypeLoader parent)
            throws XmlException
    {
        XmlOptions options = new XmlOptions();
        List<XmlError> errors = new ArrayList<XmlError>();
        options.setErrorListener(errors);

        // compile
        SchemaTypeSystem sts = XmlBeans.compileXsd(schemas, parent, options);

        // check validation errors
        for(XmlError error : errors)
        {
            if (error.getSeverity() == XmlError.SEVERITY_ERROR)
            {
                throw new XmlException(error);
            }
        }
        return sts;
    }

    private static class WsdlSchema
    {
        private SchemaDocument schema;
        private List<WsdlSchemaImport> importList;
        private String locationUrl;

        WsdlSchema()
        {
            importList = new ArrayList<WsdlSchemaImport>(5);
        }

        public void parse(Element node, String wsdlLocation)
            throws IOException, WsdlException
        {
            parse(node, wsdlLocation, new HashSet<String>());
        }

        public void parse(Element node, String wsdlLocation, Set<String> knownSchemas)
            throws IOException, WsdlException
        {
            // Here we need to make sure the source location gets passed along to the
            // xbean.  If we don't do this, some relative schema imports will not be
            // resolved correctly (particularly when the importing schema is
            // inside a JAR - see CR184851).
            try
            {
                XmlOptions opts = new XmlOptions();
                locationUrl = wsdlLocation;
                if (locationUrl == null)
                    locationUrl = node.getOwnerDocument().getDocumentURI();
                {
                    String loc = wsdlLocation;
                    //FIXME this is a workaround to fix broken urls - see CR188867
                    if (loc != null && loc.startsWith("file:") && loc.charAt(5) != '/')
                    {
                        loc = "file:/" + wsdlLocation.substring(5);
                    }
                    opts.setDocumentSourceName(loc);
                }
                schema = SchemaDocument.Factory.parse(node, opts);
            }
            catch (XmlException e)
            {
                throw new WsdlException("Failed to parse schema", e);
            }

            // (radup) I am assuming that the imports are only necessary so that all the Schemas
            // may be inlined when saving the WSDL. Since XmlBeans does its own Schema importing
            // it doesn't seem necessary to handle the imports here, but the code is here in case
            // it turns out that there are cases in which we must
            List list = findImportAndIncludeNodes(node);
            for (Iterator i = list.iterator(); i.hasNext();)
            {
                Element element = (Element) i.next();
                WsdlSchemaImport schemaImport = new WsdlSchemaImport(this);
                schemaImport.parse(element, knownSchemas);
                if (schemaImport.getSchema() != null)
                {
                    importList.add(schemaImport);
                }
            }
        }

        public String getLocationUrl()
        {
          return locationUrl;
        }

        public SchemaDocument getSchema()
        {
            return schema;
        }

        private List<Node> findImportAndIncludeNodes(Element element)
        {
            List<Node> result = new ArrayList<Node>();
            NodeList list = element.getChildNodes();
            for (int i = 0; i < list.getLength(); i++)
            {
                if (list.item(i) instanceof Element)
                {
                    Element node = (Element) list.item(i);
                    if (schemaNS.equals(node.getNamespaceURI())
                        && ("import".equals(node.getLocalName())
                        || "include".equals(node.getLocalName())
                        || "redefine".equals(node.getLocalName())))
                    {
                        result.add(node);
                    }
                }
            }

            return result;
        }

        public static WsdlSchema parse(String locationUrl, Set<String> knownSchemas)
            throws IOException, WsdlException
        {
          WsdlSchema result = new WsdlSchema();
          Document document = WsdlParser.getDocument(locationUrl);
          result.parse(document.getDocumentElement(), locationUrl, knownSchemas);

          return result;
        }
    }

    private static class WsdlSchemaImport
    {
        private WsdlSchema parent;
        private WsdlSchema schema;
        private String namespace;
        private String schemaLocation;
        private boolean relative;
        private boolean isInclude;

        WsdlSchemaImport(WsdlSchema parent)
        {
            this.parent = parent;
        }

        public void parse(Element element, Set<String> knownSchemas)
            throws IOException, WsdlException
        {
//            addDocumentation(element);
            namespace = element.getAttributeNS(null, "namespace");
            schemaLocation = element.getAttributeNS(null, "schemaLocation");

            // check if already processed
            if (knownSchemas.contains(schemaLocation))
            {
//                if( verbose )
//                    Verbose.log("Ignoring schema at " + schemaLocation + ": already processed");
                return;
            }

            knownSchemas.add(schemaLocation);

            isInclude = "include".equals(element.getNodeName());

//            if (verbose)
//            {
//                Verbose.log("import/include namespace: " + namespace);
//                Verbose.log("import/include schemaLocation: " + schemaLocation);
//            }

            if (schemaLocation == null || "".equals(schemaLocation))
            {
                relative = false;
                return;
            }

            String importedLocation = schemaLocation;
            try
            {
                URL url = new URL(schemaLocation);
                relative = false;
                // servicebus hack
                if (url.getProtocol().equals("servicebus"))
                    importedLocation = WsdlParser.constructRelativeLocation(schemaLocation, parent.getLocationUrl());
            }
            catch (MalformedURLException e)
            {
                relative = true;
                importedLocation = WsdlParser.constructRelativeLocation(schemaLocation, parent.getLocationUrl());
            }

            schema = WsdlSchema.parse(importedLocation, knownSchemas);

            schemaLocation = schemaLocation.replace('?', '_');
            element.setAttributeNS(null, "schemaLocation", schemaLocation);
        }

        WsdlSchema getSchema()
        {
            return schema;
        }
    }

    private void collectNamespaces(Element element, Map namespaces)
    {
        NamedNodeMap attributes = element.getAttributes();

        for (int i = 0; i < attributes.getLength(); i++)
        {
            Attr attr = (Attr) attributes.item(i);

            if ("xmlns".equals(attr.getPrefix()))
            {
                String name = attr.getLocalName();

                if (!namespaces.containsKey(name))
                    namespaces.put(name, attr.getValue());
            }
        }

        Node parent = element.getParentNode();

        if (parent != null && parent instanceof Element)
            collectNamespaces((Element) parent, namespaces);
    }

    private void setNamespaces(Element element, Map parentNamespaceDefs)
    {
        for (Iterator it = parentNamespaceDefs.entrySet().iterator(); it.hasNext();)
        {
            Map.Entry entry = (Map.Entry) it.next();

            if (isDefined((String) entry.getKey(), element))
            {
//                if (verbose) Verbose.log("Namespace redefined -- " + entry.getKey());
                continue;
            }

            if ("".equals(entry.getKey()))
                element.setAttribute("xmlns", (String) entry.getValue());
            else
                element.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:" + entry.getKey(), (String) entry.getValue());
        }
    }

    private boolean isDefined(String s, Element element)
    {
        NamedNodeMap attribs = element.getAttributes();

        for (int i = 0; i < attribs.getLength(); i++)
        {
            Node node = attribs.item(i);

            if ("xmlns".equals(node.getPrefix()) && node.getLocalName().equals(s))
            {
//                if (verbose) Verbose.log("Namespace found:" + node.getLocalName());

                return true;
            }
        }

        return false;
    }

    public static void checkDomElement(Node node) throws WsdlException
    {
        if (node.getNodeType() != Node.ELEMENT_NODE)
        {
            throw new WsdlException("Found an un expeced Node " +
                    node.getNodeName() + " with name = " + node.getLocalName() +
                    " and with text content = " + node.getTextContent());
        }
    }

    public static boolean isDocumentation(Node node)
    {
        if (node.getNodeType() != Node.ELEMENT_NODE) return false;
        return "documentation".equals(node.getLocalName());
    }

    public static boolean isWhiteSpace(Node node)
    {
        if (node.getNodeType() == Node.COMMENT_NODE)
        {
            return true;
        }

        if (node.getNodeType() == Node.TEXT_NODE)
        {
            String value = node.getNodeValue();

            if (value == null || "".equals(value.trim()))
            {
                return true;
            }
        }

        return false;
    }

    /**
     * check if this element is in the WSDL namespace
     */
    public static void checkWsdlNamespace(Element element) throws WsdlException
    {
        String namespace = element.getNamespaceURI();

        if (!wsdlNS.equals(namespace))
        {
            throw new WsdlException("Found an element with unexpected " +
                "namespace '" + namespace + "' . Was expecting '" +
                wsdlNS + "'");
        }
    }

    /**
     * check if the given element is definitions
     */
    static void checkWsdlDefinitions(String name, Element definitionsEle) throws WsdlException
    {
        if (!name.equals(definitionsEle.getLocalName()))
        {
            throw new WsdlException("The XML document specified " +
                "is not a valid WSDL document. The name of the top level element " +
                "should be '" + name + "' but found '" +
                definitionsEle.getLocalName() + "'");
        }
    }

    public static Document getDocument(String url) throws IOException, WsdlException
    {
        // TODO(radup) Check out weblogic.wsee.util.dom.DOMParser;
        // It may be necessary to delegate the parsing back to the caller
        try
        {
            return parser.parse(url);
//            return DOMParser.getDocument(info, url);
        }
        catch (org.xml.sax.SAXException e)
        {
            throw new WsdlException("Failed to parse wsdl file from \"" + url + "\" due to --" +
                e, e);
        }
    }

    public static String constructRelativeLocation(String location, String rootLocation)
        throws WsdlException
    {
        URL rootURL = null;
        try
        {
            rootURL = new URL(rootLocation);
        }
        catch (MalformedURLException e)
        {
            //try file protocol:
            try
            {
                rootURL = new URL("file:" + rootLocation);
            }
            catch (MalformedURLException e1)
            {
                throw new WsdlException("Failed to construct relative URL," +
                    " Base url " + rootLocation + ", relative location " + location + e);
            }
        }
        try
        {
            return new URL(rootURL, location).toExternalForm();
        }
        catch (MalformedURLException e)
        {
            throw new WsdlException("Failed to construct relative URL " + e);
        }
    }

    public static class WsdlException extends Exception
    {
        WsdlException(String message)
        {
            super(message);
        }

        WsdlException(String message, Throwable cause)
        {
            super(message, cause);
        }
    }
}
