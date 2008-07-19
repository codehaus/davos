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

import javax.sdo.Type;
import javax.sdo.Property;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.io.Reader;
import java.io.InputStream;

import davos.sdo.Options;
import davos.sdo.TypeXML;
import davos.sdo.PropertyXML;
import davos.sdo.SDOContext;
import davos.sdo.impl.binding.Schema2SDO;
import davos.sdo.impl.helpers.schemagen.XsdGenerator;
import davos.sdo.impl.type.TypeSystemBase;
import davos.sdo.impl.common.Names;
import davos.sdo.type.TypeSystem;
import davos.sdo.type.XSDHelperExt;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SchemaAnnotation;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.SchemaAttributeModel;
import org.apache.xmlbeans.SchemaLocalAttribute;
import org.apache.xmlbeans.SchemaLocalElement;
import org.apache.xmlbeans.SchemaParticle;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.impl.common.XMLChar;

import javax.xml.namespace.QName;

/**
 * Created
 * Date: Aug 12, 2006
 * Time: 12:51:32 AM
 */
public class XSDHelperImpl
    implements XSDHelperExt
{
    private static final QName SOURCE_QNAME = new QName("", "source");
    private static final String DOWNLOADS_PROP_NAME = "SDO_DOWNLOADS_ENABLED";

    private SDOContext _sdoContext;

    public XSDHelperImpl(SDOContext sdoContext)
    {
        _sdoContext = sdoContext;
    }

    public String getLocalName(Type type)
    {
        if (type instanceof TypeXML)
        {
            TypeXML typeXML = (TypeXML) type;
            QName schemaTypeName = typeXML.getXMLSchemaTypeName();
            if (typeXML.getXMLSchemaType() != null)
                return schemaTypeName == null ? null : schemaTypeName.getLocalPart();
        }
        // Compute the type name that will be generated (see XsdGenerator)
        String typeName = type.getName();
        if (!XMLChar.isValidNCName(typeName))
            return null;
        return typeName;
    }

    // SDO must be able to serialize instances of all properties to XML even if the property did
    // not come from Schema and that involves coming
    // up with a name and attribute/element setting which will be returned in that case
    public String getLocalName(Property property)
    {
        if (property instanceof PropertyXML)
        {
            PropertyXML propertyXML = (PropertyXML) property;
            return propertyXML.getXMLName();
        }
        return property.getName();
    }

    public String getNamespaceURI(Property property)
    {
        if (property instanceof PropertyXML)
        {
            PropertyXML propertyXML = (PropertyXML) property;
            return propertyXML.getXMLNamespaceURI();
        }
        else
            return null;
    }

    public boolean isAttribute(Property property)
    {
        if (property instanceof PropertyXML)
        {
            PropertyXML propertyXML = (PropertyXML) property;
            return !propertyXML.isXMLElement() && !isSimpleContent(propertyXML);
        }
        else
            return !XsdGenerator.isElement(property);
    }

    public boolean isElement(Property property)
    {
        if (property instanceof PropertyXML)
        {
            PropertyXML propertyXML = (PropertyXML) property;
            return propertyXML.isXMLElement();
        }
        else
            return XsdGenerator.isElement(property);
    }

    public boolean isSimpleContent(PropertyXML property)
    {
        TypeXML containingType = property.getContainingTypeXML();
        return containingType != null && containingType.isSimpleContent() &&
            Names.SIMPLE_CONTENT_PROP_NAME.equals(property.getName());
    }

    public boolean isMixed(Type type)
    {
        if (type instanceof TypeXML)
        {
            SchemaType st = ((TypeXML) type).getXMLSchemaType();
            if (st != null)
                return st.getContentType() == SchemaType.MIXED_CONTENT;
        }
        return type.isSequenced();
    }

    public boolean isXSD(Type type)
    {
        return (type instanceof TypeXML) && fromSchema((TypeXML) type);
    }

    private boolean fromSchema(TypeXML type)
    {
        return type.getXMLSchemaType() != null;
    }

    public Property getGlobalProperty(String uri, String propertyName, boolean isElement)
    {
        //(radup) The values used here are XML names, PlainUnmarshaller relies on that
        //todo check if the names used in the XSDHelper are indeed the same as SDO names
        if ( isElement )
            return _sdoContext.getBindingSystem().loadGlobalPropertyByTopLevelElemQName(uri, propertyName);
        else
            return _sdoContext.getBindingSystem().loadGlobalPropertyByTopLevelAttrQName(uri, propertyName);
    }

    public String getAppinfo(Type type, String source)
    {
        SchemaType st;
        st = getSchemaType(type);
        if (st == null)
            return null;
        else
            return appInfoToString(st.getAnnotation(), source);
    }

    public String getAppinfo(Property property, String source)
    {
        // TODO(radup) Decide how we represent global properties
        // Those don't necessary have a containing type so this algorithm won't
        // work for them
        SchemaType st = getSchemaType(property.getContainingType());
        if (st == null)
            return null;
        QName propName;
        if (property instanceof PropertyXML)
        {
            PropertyXML propertyXML = (PropertyXML) property;
            propName = new QName(propertyXML.getXMLNamespaceURI(),
                propertyXML.getXMLName());
            if (propertyXML.isXMLElement())
            {
                SchemaLocalElement sle = findLocalElement(st, propName);
                if (sle != null)
                    return appInfoToString(sle.getAnnotation(), source);
                else
                    return null;
            }
            else
            {
                SchemaAttributeModel sam = st.getAttributeModel();
                if (sam != null)
                {
                    SchemaLocalAttribute att = sam.getAttribute(propName);
                    if (att != null)
                        return appInfoToString(att.getAnnotation(), source);
                    else
                        return null;
                }
                else
                    return null;
            }
        }
        else
        {
            propName = new QName(null, property.getName());
            SchemaAttributeModel sam = st.getAttributeModel();
            if (sam != null)
            {
                SchemaLocalAttribute att = sam.getAttribute(propName);
                if (att != null)
                    return appInfoToString(att.getAnnotation(), source);
            }
            SchemaLocalElement sle = findLocalElement(st, propName);
            if (sle != null)
                return appInfoToString(sle.getAnnotation(), source);
            else
                return null;
        }
    }

    public List /*Type*/ define(String xsd)
    {
        return defineTypeSystem(Schema2SDO.createSDOTypeSystem(xsd, null,
            _sdoContext.getBindingSystem(), getDefaultOptions()));
    }

    public List /*Type*/ define(Reader xsdReader, String schemaLocation)
    {
        return defineTypeSystem(Schema2SDO.createSDOTypeSystem(xsdReader, schemaLocation,
            _sdoContext.getBindingSystem(), getDefaultOptions()));
    }

    public List /*Type*/ define(InputStream xsdInputStream, String schemaLocation)
    {
        return defineTypeSystem(Schema2SDO.createSDOTypeSystem(xsdInputStream, schemaLocation,
            _sdoContext.getBindingSystem(), getDefaultOptions()));
    }

    public String generate(List /*Type*/ types)
    {
        return generate(types, null);
    }

    public String generate(List /*Type*/ types, Map /*String, String*/ namespaceToSchemaLocation)
    {
        return XsdGenerator.generateXsd(types, namespaceToSchemaLocation);
    }

    // ======================================================
    // XSDHelperExt methods
    // ======================================================
    public List<Type> defineSchema(String schema, String schemaLocation, Object options)
    {
        return defineTypeSystem(Schema2SDO.createSDOTypeSystem(schema, schemaLocation,
            _sdoContext.getBindingSystem(), addDefaultOptions(options)));
    }

    public List<Type> defineSchemas(String[] schemasAsStrings, String[] schemaLocations, Object options)
    {
        return defineTypeSystem(Schema2SDO.createSDOTypeSystem(schemasAsStrings, schemaLocations,
            _sdoContext.getBindingSystem(), addDefaultOptions(options)));
    }

    public List<Type> defineSchemasFromWsdl(String wsdlLocation, Object options)
    {
        return defineTypeSystem(Schema2SDO.createSDOTypeSystemFromWsdl(wsdlLocation,
            _sdoContext.getBindingSystem(), addDefaultOptions(options)));
    }

    public List<Type> defineSchema(InputStream schemaAsInputStream, String schemaLocation, Object options)
    {
        return defineTypeSystem(Schema2SDO.createSDOTypeSystem(schemaAsInputStream, schemaLocation,
            _sdoContext.getBindingSystem(), addDefaultOptions(options)));
    }

    public List<Type> defineSchema(Reader schemaAsReader, String schemaLocation, Object options)
    {
        return defineTypeSystem(Schema2SDO.createSDOTypeSystem(schemaAsReader, schemaLocation,
            _sdoContext.getBindingSystem(), addDefaultOptions(options)));
    }

    private Object getDefaultOptions()
    {
        Options opt = new Options().setCompileSkipTypesFromContext();
        if (isDownloadsEnabled())
            opt.setCompileSchemaOptions(new XmlOptions().setCompileDownloadUrls());
        return opt;
    }

    private Object addDefaultOptions(Object options)
    {
        if (options == null)
        {
            Options opt = new Options().setCompileSkipTypesFromContext();
            if (isDownloadsEnabled())
                opt.setCompileSchemaOptions(new XmlOptions().setCompileDownloadUrls());
            return opt;
        }
        else if (options instanceof XmlOptions)
        {
            XmlOptions xmlOptions = (XmlOptions) options;
            if (isDownloadsEnabled())
                xmlOptions.setCompileDownloadUrls();
            Options opt = new Options().setCompileSkipTypesFromContext();
            opt.setCompileSchemaOptions(xmlOptions);
            return opt;
        }
        else if (options instanceof Map)
        {
            Map opt = (Map) options;
            opt.put(Options.COMPILE_SKIP_IF_KNOWN, null);
            if (isDownloadsEnabled())
            {
                XmlOptions xmlOptions = (XmlOptions) opt.get(Options.COMPILE_SCHEMA_OPTIONS);
                if (xmlOptions == null)
                    opt.put(Options.COMPILE_SCHEMA_OPTIONS, new XmlOptions().setCompileDownloadUrls());
                else
                    xmlOptions.setCompileDownloadUrls();
            }
        }
        else if (options instanceof Options)
        {
            Options opt = (Options) options;
            opt.setCompileSkipTypesFromContext();
            if (isDownloadsEnabled())
            {
                XmlOptions xmlOptions = (XmlOptions) opt.getMap().get(Options.COMPILE_SCHEMA_OPTIONS);
                if (xmlOptions == null)
                    opt.setCompileSchemaOptions(new XmlOptions().setCompileDownloadUrls());
                else
                    xmlOptions.setCompileDownloadUrls();
            }
        }
        return options;
    }

    private boolean isDownloadsEnabled()
    {
        try
        {
            String propValue = System.getenv(DOWNLOADS_PROP_NAME);
            if (propValue != null && (propValue.equalsIgnoreCase("true") ||
                propValue.equals("1")))
                return true;
        }
        catch (SecurityException se)
        { }
        return false;
    }

    // ======================================================
    // Helper methods
    // ======================================================
    private List defineTypeSystem(TypeSystem typeSystem)
    {
        if (typeSystem != null)
        {
            TypeSystem ts = _sdoContext.getTypeSystem();
            ((TypeSystemBase) ts).addTypeSystem(typeSystem, true);
            // At this point, we now that the additions were accepted
            return new ArrayList(typeSystem.getAllTypes());
        }
        return null;
    }

    private String appInfoToString(SchemaAnnotation ann, String source)
    {
        if (ann == null)
            return null;
        XmlObject[] appinfo = ann.getApplicationInformation();
        if (appinfo == null)
            return null;
        StringBuilder sb = new StringBuilder();
        for (XmlObject xobj : appinfo)
        {
            XmlCursor c = xobj.newCursor();
            String value = c.getAttributeText(SOURCE_QNAME);
            c.dispose();
            if (source == null ? value == null : source.equals(value))
                sb.append(xobj.xmlText(new XmlOptions().setSaveOuter()));
        }
        return sb.length() == 0 ? null : sb.toString();
    }

    private SchemaType getSchemaType(Type type)
    {
        SchemaType st = null;
        if (type instanceof TypeXML)
            st = ((TypeXML) type).getXMLSchemaType();
        return st;
    }

    private SchemaLocalElement findLocalElement(SchemaType st, QName name)
    {
        // Finds the _first_ (there may be more than one) element declaration
        // with the given name in the content model of the given type
        SchemaParticle sp = st.getContentModel();
        if (sp == null)
            return null;
        return findLocalElement(sp, name);
    }

    private SchemaLocalElement findLocalElement(SchemaParticle sp, QName name)
    {
        switch (sp.getParticleType())
        {
        case SchemaParticle.ALL:
        case SchemaParticle.CHOICE:
        case SchemaParticle.SEQUENCE:
            for (SchemaParticle child : sp.getParticleChildren())
            {
                SchemaLocalElement result = findLocalElement(child, name);
                if (result != null)
                    return result;
            }
            break;
        case SchemaParticle.ELEMENT:
            if (name.equals(sp.getName()))
                return (SchemaLocalElement) sp;
            else
                return null;
        case SchemaParticle.WILDCARD:
            return null;
        }
        return null;
    }
}
