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
package davos.sdo.impl.helpers.schemagen;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.ArrayList;
import java.math.BigInteger;

import javax.xml.namespace.QName;

import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.impl.common.XMLChar;
import org.apache.xmlbeans.impl.xb.xsdschema.SchemaDocument;
import org.apache.xmlbeans.impl.xb.xsdschema.FormChoice;
import org.apache.xmlbeans.impl.xb.xsdschema.TopLevelAttribute;
import org.apache.xmlbeans.impl.xb.xsdschema.TopLevelElement;
import org.apache.xmlbeans.impl.xb.xsdschema.TopLevelComplexType;
import org.apache.xmlbeans.impl.xb.xsdschema.TopLevelSimpleType;
import org.apache.xmlbeans.impl.xb.xsdschema.RestrictionDocument;
import org.apache.xmlbeans.impl.xb.xsdschema.ListDocument;
import org.apache.xmlbeans.impl.xb.xsdschema.ImportDocument;
import org.apache.xmlbeans.impl.xb.xsdschema.LocalElement;
import org.apache.xmlbeans.impl.xb.xsdschema.ComplexContentDocument;
import org.apache.xmlbeans.impl.xb.xsdschema.ExtensionType;
import org.apache.xmlbeans.impl.xb.xsdschema.ExplicitGroup;
import org.apache.xmlbeans.impl.xb.xsdschema.AnyDocument;
import org.apache.xmlbeans.impl.inst2xsd.util.TypeSystemHolder;

import davos.sdo.PropertyXML;
import davos.sdo.SDOError;
import davos.sdo.SDOSchemaGenerationException;
import davos.sdo.TypeXML;
import davos.sdo.impl.common.Names;
import davos.sdo.impl.type.SimpleValueHelper;
import davos.sdo.impl.type.BuiltInTypeSystem;

import javax.sdo.Property;

public class XsdGenerator extends TypeSystemHolder
{
    private static final Map<String, String> EMPTY_MAP =
        new HashMap<String, String>(0);

    public static String generateXsd(List<javax.sdo.Type> originalTypes,
                                     Map<String, String> uriToLocationMap)
    {
        if (uriToLocationMap == null)
            uriToLocationMap = EMPTY_MAP;

        String packageName = null;
        String uri = null;
        Set<String> importURIs = new HashSet<String>(1);
        boolean formDefaultQualified = true;
        // Since we are going to add to the "types" array, it seems prudent
        // to not alter the copy that the caller has sent us
        List<javax.sdo.Type> types = new ArrayList<javax.sdo.Type>(originalTypes);
        List<Object> generated = new ArrayList<Object>(originalTypes.size());
        // Set with all the types that are in the processing queue, to make
        // it easier to detect if a type is being already processed or not
        Set<javax.sdo.Type> typeSet = new HashSet<javax.sdo.Type>(types);

        /* The 2.1.1 spec has been amended to stop generation of the extra global
         * elements (one for each complex type), so we don't need to analyze names anymore
         */

        for (int i = 0; i < types.size(); i++)
        {
            javax.sdo.Type type = types.get(i);
            String typeName = type.getName();
            String typeUri = type.getURI();
            typeUri = typeUri == null ? "" : typeUri;
            Class instanceClass = type.getInstanceClass();

            if (uri == null)
                uri = typeUri;
            else if (!uri.equals(typeUri))
                throw new SDOSchemaGenerationException(SDOError.messageForCode(
                    "xsdgenerator.notsameuri", uri, typeUri));
            if (Names.NAME_OF_CONTAINING_TYPE_FOR_GLOBAL_PROPERTIES.equals(typeName))
            {
                // This is a special case of a synthetic container type for a global property
                Type t = new Type(typeUri, typeName);
                Property prop = (Property)type.getProperties().get(0);
                
                boolean isElem = isElement(prop);
                buildElementOrAttribute(prop, isElem, type, uri, importURIs, types, typeSet, formDefaultQualified, t);
                if (isElem)
                {
                    Element e = (Element) t.getElements().get(0);
                    e.setGlobal(true);
                    generated.add(e);
                }
                else
                {
                    Attribute a = (Attribute) t.getAttributes().get(0);
                    a.setGlobal(true);
                    generated.add(a);
                }
                continue;
            }
            if (!XMLChar.isValidNCName(typeName))
                throw new SDOSchemaGenerationException(SDOError.messageForCode(
                    "xsdgenerator.invalidncname", typeName));
            if (packageName == null && !type.isDataType())
                packageName = packageName(instanceClass);

            Type t = new Type(typeUri, typeName);
            t.setGlobal(true);
            generated.add(t);
            formDefaultQualified = buildType(t, type, uri, typeUri, typeName, importURIs, types, typeSet,
                formDefaultQualified);
        }
        return xsdToString(generated, uri, packageName, formDefaultQualified, importURIs, uriToLocationMap);
    }

    private static boolean buildType(Type t, javax.sdo.Type type, String uri, String typeUri, String typeName,
        Set<String> importURIs, List<javax.sdo.Type> types, Set<javax.sdo.Type> typeSet,
        boolean formDefaultQualified)
    {
        Class instanceClass = type.getInstanceClass();
        javax.sdo.Type baseType = null;
        List l = type.getBaseTypes();
        if (l != null && l.size() > 0)
            if (l.size() > 1)
                throw new SDOSchemaGenerationException(SDOError.messageForCode(
                    "xsdgenerator.toomanybasetypes", typeName + '@' + typeUri, l.size()));
            else
                baseType = (javax.sdo.Type) l.get(0);

        if (baseType != null)
            addTypeToSchema(uri, baseType, importURIs, types, typeSet);
        t.setAbstract(type.isAbstract());
        t.setAliasNames(type.getAliasNames());
        if (type.isDataType())
        {
            t.setContentType(org.apache.xmlbeans.impl.inst2xsd.util.Type.SIMPLE_TYPE_SIMPLE_CONTENT);
            if (baseType == null)
            {
                if (instanceClass != null && instanceClass != Object.class)
                {
                    String className = instanceClass.getName();
                    if (JAVA_LIST.equals(className))
                    {
                        t.setDerivation(Type.DT_LIST);
                        t.setBaseType(Names.URI_XSD, XS_STRING);
                    }
                    else
                    {
                        // Must be because of an instanceClass attribute
                        t.setInstanceClass(className);
                    }
                }
            }
            else
            {
                String baseUri = baseType.getURI();
                String baseName = baseType.getName();
                t.setDerivation(Type.DT_RESTRICTION);
                if (Names.URI_SDO.equals(baseUri))
                {
                    String schemaTypeName = builtinSchemaType(baseName);
                    if (schemaTypeName != null)
                    {
                        t.setBaseType(Names.URI_XSD, schemaTypeName);
                        if (SDO_STRINGS.equals(baseName))
                            t.setDerivation(Type.DT_LIST);
                    }
                }
                else
                {
                    if (uri == null ? baseUri != null : !uri.equals(baseUri))
                        // Import needed
                        importURIs.add(baseUri);
                    t.setBaseType(baseUri, baseName);
                }
                if (instanceClass != null && !instanceClass.equals(
                    baseType.getInstanceClass()))
                    t.setInstanceClass(instanceClass.getName());
            }
        }
        else
        {
            boolean hasElements = false;
            boolean simpleContent = false;
            String baseTypeUri = null;
            String baseTypeName = null;
            if (baseType == null)
            {
                // This means that the Schema type has simple content or that the
                // type is dynamically defined and should have anyType as base
                if ((type instanceof TypeXML) && ((TypeXML) type).isSimpleContent())
                    simpleContent = true;
            }
            else if (!(Names.URI_SDO.equals(baseType.getURI()) &&
                    "DataObject".equals(baseType.getName())))
            {
                baseTypeUri = baseType.getURI();
                if (uri == null ? baseTypeUri != null : !uri.equals(baseTypeUri))
                    // Import needed
                    importURIs.add(baseTypeUri);
                t.setBaseType(baseTypeUri, baseTypeName=baseType.getName());
            }
            List<Property> properties = type.getDeclaredProperties();
            for (Property prop : properties)
            {
                if (Names.SIMPLE_CONTENT_PROP_NAME.equals(prop.getName()) && simpleContent)
                    continue; // skip this property
                boolean isElem = isElement(prop);
                if (isElem)
                    hasElements = true;
                formDefaultQualified = buildElementOrAttribute(prop, isElem, type, uri,
                    importURIs, types, typeSet, formDefaultQualified, t);
            }
            t.setOpen(type.isOpen() && !(baseType != null &&
                    !(Names.URI_SDO.equals(baseType.getURI()) &&
                    "DataObject".equals(baseType.getName())) &&
                    baseType.isOpen()));
            if (type.isSequenced())
            {
                t.setSequenced(true);
                t.setContentType(org.apache.xmlbeans.impl.inst2xsd.util.Type.COMPLEX_TYPE_MIXED_CONTENT);
                t.setTopParticleForComplexOrMixedContent(org.apache.xmlbeans.impl.inst2xsd.util.Type.PARTICLE_CHOICE_UNBOUNDED);
            }
            else if (hasElements)
            {
                t.setContentType(org.apache.xmlbeans.impl.inst2xsd.util.Type.COMPLEX_TYPE_COMPLEX_CONTENT);
                t.setTopParticleForComplexOrMixedContent(org.apache.xmlbeans.impl.inst2xsd.util.Type.PARTICLE_SEQUENCE);
            }
            else if (simpleContent)
            {
                t.setContentType(org.apache.xmlbeans.impl.inst2xsd.util.Type.COMPLEX_TYPE_SIMPLE_CONTENT);
                t.setExtensionType(new Type(baseTypeUri, baseTypeName));
            }
            else
                t.setContentType(org.apache.xmlbeans.impl.inst2xsd.util.Type.COMPLEX_TYPE_EMPTY_CONTENT);
        }
        return formDefaultQualified;
    }

    public static boolean isElement(Property prop)
    {
        return (prop instanceof PropertyXML) ? ((PropertyXML) prop).
            isXMLElement() : prop.isMany() || prop.isContainment() || prop.isNullable() ||
            prop.get(BuiltInTypeSystem.P_PROPERTY_XMLELEMENT) == Boolean.TRUE;
    }

    private static boolean buildElementOrAttribute(Property prop, boolean isElem,
        javax.sdo.Type type, String uri, Set<String> importURIs, List<javax.sdo.Type> types,
        Set<javax.sdo.Type> typeSet, boolean formDefaultQualified, Type t)
    {
        SdoProperty elemOrAttr;
        String propName;
        String propUri;
        List aliasNames = prop.getAliasNames();
        boolean readOnly = prop.isReadOnly();
        String sdoName = null;
        String propTypeName = null;
        String propTypeUri = null;
        String sdoPropertyTypeName = null;
        String sdoPropertyTypeUri = null;
        String sdoDataTypeName = null;
        String sdoDataTypeUri = null;
        String sdoOpposite = null;
        javax.sdo.Type propType = prop.getType();
        String def = null;
        if (prop instanceof PropertyXML)
        {
            PropertyXML propXml = (PropertyXML) prop;
            propName = propXml.getXMLName();
            propUri = propXml.getXMLNamespaceURI();
            sdoName = prop.getName();
            if (propXml.getSchemaTypeCode() == SchemaType.BTC_ID)
            {
                propTypeUri = Names.URI_XSD;
                propTypeName = XS_ID;
            }
        }
        else
        {
            propName = prop.getName();
            propUri = null;
        }
        if (!prop.isContainment() && !propType.isDataType())
        {
            if (!isElem && hasId(type))
            {
                if (prop.isMany())
                    propTypeName = XS_IDREFS;
                else
                    propTypeName = XS_IDREF;
            }
            else
                propTypeName = XS_ANYURI;
            propTypeUri = Names.URI_XSD;
            sdoPropertyTypeName = propType.getName();
            sdoPropertyTypeUri = propType.getURI();
            if (prop.getOpposite() != null)
                sdoOpposite = prop.getOpposite().getName();
        }
        else if (propTypeName == null)
        {
            propTypeName = propType.getName();
            propTypeUri = propType.getURI();
            if (Names.URI_SDO.equals(propTypeUri))
            {
                propTypeUri = Names.URI_XSD;
                if (SDO_CHARACTER.equals(propTypeName) || SDO_DATE.equals(propTypeName))
                {
                    sdoDataTypeUri = Names.URI_SDO;
                    sdoDataTypeName = propTypeName;
                }
                propTypeName = builtinSchemaType(propTypeName);
            }
            else if (Names.URI_SDOJAVA.equals(propTypeUri))
            {
                String baseTypeName = ((javax.sdo.Type) propType.getBaseTypes().get(0)).getName();
                // As per spec, we are using the base type and add a 'dataType' annotation
                sdoDataTypeUri = Names.URI_SDOJAVA;
                sdoDataTypeName = propTypeName;
                propTypeUri = Names.URI_XSD;
                propTypeName = builtinSchemaType(baseTypeName);
            }
        }
        boolean anonType = isAnonType(propTypeName, propName, isElem);
        if (!anonType)
            addTypeToSchema(uri, propType, importURIs, types, typeSet);
        if (prop.getDefault() != null && !sdoDefaultForType(prop))
            if (propType instanceof TypeXML)
            {
                try
                {
                    def = SimpleValueHelper.getLexicalRepresentation(prop.getDefault(),
                        (TypeXML) propType, 0, null);
                }
                catch (SimpleValueHelper.SimpleValueException e)
                {
                    switch (e.cause())
                    {
                        case SimpleValueHelper.MARSHAL_WRONGINSTANCECLASS:
                            throw new SDOSchemaGenerationException(SDOError.messageForCode(
                                "xsdgenerator.invaliddefault", propName, propUri,
                                type.getName() + '@' + type.getURI(), prop.getDefault().getClass().
                                getName(), propType.getInstanceClass().getName()));
                        default:
                            throw new IllegalStateException();
                    }
                }
            }
            else
                def = prop.getDefault().toString();
        if (isElem)
        {
            Element elem = new Element();
            if (prop.isMany())
                elem.setMaxOccurs(-1);
            else if (type.isSequenced())
                elem.setHasMany(true);
            elem.setMinOccurs(0);
            if (prop.isNullable())
                elem.setNillable(true);
            if (propUri != null && propUri.length() > 0 && !propUri.equals(uri))
            {
                Element ref = new Element();
                ref.setName(new QName(propUri, propName));
                ref.setGlobal(true);
                importURIs.add(propUri);
                elem.setRef(ref);
                // We also need to set the local name on the element
                elem.setName(ref.getName());
            }
            else
            {
                if (anonType)
                {
                    Type generatedPropType = new Type();
                    formDefaultQualified = buildType(generatedPropType, propType, uri, null, null,
                        importURIs, types, typeSet, formDefaultQualified);
                    elem.setType(generatedPropType);
                }
                else
                    elem.setType(new Type(propTypeUri, propTypeName));
                elem.setName(new QName(propUri, propName));
            }
            if (propUri == null || propUri.length() == 0)
                formDefaultQualified = false;
            elemOrAttr = elem;
            t.addElement(elem);
        }
        else
        {
            Attribute attr = new Attribute();
            if (propUri != null && propUri.length() > 0 && !propUri.equals(uri))
            {
                Attribute ref = new Attribute();
                ref.setName(new QName(propUri, propName));
                ref.setGlobal(true);
                importURIs.add(propUri);
                attr.setRef(ref);
            }
            else
            {
                if (anonType)
                {
                    Type generatedPropType = new Type();
                    buildType(generatedPropType, propType, uri, null, null, importURIs, types, typeSet,
                        formDefaultQualified);
                    attr.setType(generatedPropType);
                }
                else
                    attr.setType(new Type(propTypeUri, propTypeName));
                attr.setName(new QName(propUri, propName));
            }
            elemOrAttr = attr;
            t.addAttribute(attr);
        }
        elemOrAttr.setDefault(def);
        elemOrAttr.setSdoName(sdoName.equals(propName) ? null : sdoName);
        elemOrAttr.setReadOnly(readOnly);
        elemOrAttr.setAliasNames(aliasNames);
        elemOrAttr.setOpposite(sdoOpposite);
        elemOrAttr.setPropertyType(sdoPropertyTypeUri, sdoPropertyTypeName);
        elemOrAttr.setDataType(sdoDataTypeUri, sdoDataTypeName);
        return formDefaultQualified;
    }

    private static boolean isAnonType(String typeName, String propName, boolean element)
    {
        char separator = element ? '$' : '@';
        int separatorIndex = typeName.lastIndexOf(separator);
        return separatorIndex > 0 && typeName.substring(separatorIndex + 1).equals(propName);
    }

    private static void addTypeToSchema(String targetUri, javax.sdo.Type type,
        Set<String> importURIs, List<javax.sdo.Type> typesToProcess,
        Set<javax.sdo.Type> typesAdded)
    {
        if (!targetUri.equals(type.getURI()))
        {
            if (!Names.URI_SDO.equals(type.getURI()) &&
                !Names.URI_SDOJAVA.equals(type.getURI()))
                importURIs.add(type.getURI());
        }
        else
        {
            if (!typesAdded.contains(type))
            {
                typesAdded.add(type);
                typesToProcess.add(type);
            }
        }
    }

    private static String xsdToString(List<Object> componentList, String uri, String packageName,
        boolean formDefaultQualified, Set<String> importURIs, Map<String, String> uriToLocationMap)
    {
        SchemaDocument doc = (SchemaDocument) XmlBeans.typeLoaderForClassLoader(XsdGenerator.class.getClassLoader()).
            newInstance(SchemaDocument.type, null);
        SchemaDocument.Schema schema = doc.addNewSchema();
        // Set the target namespace
        if (uri != null && uri.length() > 0)
            schema.setTargetNamespace(uri);
        XsdGenerator gen = new XsdGenerator();
        // Set the elementFormDefault, if necessary
        if (formDefaultQualified)
        {
            schema.setElementFormDefault(FormChoice.QUALIFIED);
            gen.elementFormDefaultQualified = true;
        }
        // Set the namespace declarations
        setupPrefixDecls(schema, uri);
        // Set the Java package name
        if (packageName != null)
            addCustomAttribute(schema, Names.URI_SDOJAVA, "package", packageName);
        // Add import declarations
        XmlCursor importCursor = null;
        if (importURIs.size() > 0)
            importCursor = schema.newCursor();
        for (String importURI : importURIs)
        {
            String location = uriToLocationMap.get(importURI);
            ImportDocument.Import i = schema.addNewImport();
            i.setNamespace(importURI);
            if (location != null)
                i.setSchemaLocation(location);
            ensureNamespaceDefined(importCursor, importURI);
        }
        if (importURIs.size() > 0)
            importCursor.dispose();
        for (int i = 0; i < componentList.size(); i++)
        {
            Object component = componentList.get(i);
            if (component instanceof Type)
            {
                Type t = (Type) component;
                if (t.getContentType() == Type.SIMPLE_TYPE_SIMPLE_CONTENT)
                {
                    TopLevelSimpleType tlSimpleType = schema.addNewSimpleType();
                    processSimpleType(tlSimpleType, t);
                }
                else
                {
                    TopLevelComplexType tlComplexType = schema.addNewComplexType();
                    gen.processComplexType(tlComplexType, t, uri);
                }
            }
            else if (component instanceof Element)
            {
                Element e = (Element) component;
                TopLevelElement tlElement = schema.addNewElement();
                tlElement.setName(e.getName().getLocalPart());
                tlElement.setType(e.getType(). getName());
                addCustomAttributesProperty(tlElement, e);
            }
            else if (component instanceof Attribute)
            {
                Attribute a = (Attribute) component;
                TopLevelAttribute tlAtt = schema.addNewAttribute();
                tlAtt.setName(a.getName().getLocalPart());
                tlAtt.setType(a.getType().getName());
            }
        }
        return doc.xmlText(new XmlOptions().setSavePrettyPrint());
    }

    private static void setupPrefixDecls(SchemaDocument.Schema schema, String uri)
    {
        XmlCursor c = schema.newCursor();
        c.toNextToken();
        c.insertNamespace(Names.PREFIX_XSD, Names.URI_XSD);
        c.insertNamespace(Names.PREFIX_SDO, Names.URI_SDO);
        c.insertNamespace(Names.PREFIX_SDOXML, Names.URI_SDOXML);
        c.insertNamespace(Names.PREFIX_SDOJAVA, Names.URI_SDOJAVA);
        if (uri != null && uri.length() > 0)
            c.insertNamespace(Names.PREFIX_TNS, uri);
        c.dispose();
    }

    private static void addCustomAttribute(XmlObject object, String uri, String name, String value)
    {
        XmlCursor c = object.newCursor();
        if (c.toLastAttribute())
            c.toNextToken();
        c.insertAttributeWithValue(name, uri, value);
        c.dispose();
    }

    private static void addCustomAttribute(XmlObject object, String uri, String name, List value)
    {
        if (value.size() == 0)
            return;
        if (value.size() == 1)
        {
            addCustomAttribute(object, uri, name, value.get(0).toString());
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (Object o : value)
            sb.append(o.toString()).append(' ');
        sb.delete(sb.length() - 1, 1);
        addCustomAttribute(object, uri, name, sb.toString());
    }

    private static void addCustomAttributesType(XmlObject o, Type t)
    {
        if (t.getAliasNames() != null)
            addCustomAttribute(o, Names.URI_SDOXML, "aliasNames", t.getAliasNames());
        if (t.getInstanceClass() != null)
            addCustomAttribute(o, Names.URI_SDOJAVA, "instanceClass", t.getInstanceClass());
        if (t.isOpen() && !t.isSequenced())
            addCustomAttribute(o, Names.URI_SDOXML, "sequence", "false");
    }

    private static void addCustomAttributesProperty(XmlObject o, SdoProperty p)
    {
        if (p.getSdoName() != null)
            addCustomAttribute(o, Names.URI_SDOXML, "name", p.getSdoName());
        if (p.getAliasNames() != null)
            addCustomAttribute(o, Names.URI_SDOXML, "aliasNames", p.getAliasNames());
        if (p.isReadOnly())
            addCustomAttribute(o, Names.URI_SDOXML, "readOnly", "true");
        if (p.getPropertyTypeName() != null)
        {
            XmlCursor c = o.newCursor();
            String uri = p.getPropertyTypeUri();
            if (uri == null || uri.length() == 0)
            {
                if (c.toLastAttribute())
                    c.toNextToken();
                c.insertAttributeWithValue("propertyType", Names.URI_SDOXML, p.getPropertyTypeName());
            }
            else
            {
                String prefix = c.prefixForNamespace(uri);
                if (prefix == null)
                    prefix = ensureNamespaceDefined(c, uri);
                if (c.toLastAttribute())
                    c.toNextToken();
                c.insertAttributeWithValue("propertyType", Names.URI_SDOXML, prefix +
                    ':' + p.getPropertyTypeName());
            }
            c.dispose();
        }
        if (p.getDataTypeName() != null)
        {
            XmlCursor c = o.newCursor();
            String uri = p.getDataTypeUri();
            if (uri == null || uri.length() == 0)
            {
                if (c.toLastAttribute())
                    c.toNextToken();
                c.insertAttributeWithValue("dataType", Names.URI_SDOXML, p.getDataTypeName());
            }
            else
            {
                String prefix = c.prefixForNamespace(uri);
                if (prefix == null)
                    prefix = ensureNamespaceDefined(c, uri);
                if (c.toLastAttribute())
                    c.toNextToken();
                c.insertAttributeWithValue("dataType", Names.URI_SDOXML, prefix + ':' + p.getDataTypeName());
            }
            c.dispose();
        }
        if (p.getOpposite() != null)
            addCustomAttribute(o, Names.URI_SDOXML, "opposite", p.getOpposite());
    }

    private static String ensureNamespaceDefined(XmlCursor c, String uri)
    {
        String prototype = "ns";
        int index = 1;
        while (c.namespaceForPrefix(prototype + index) != null)
            index++;
        c.push();
        // c.toLastAttribute() doesn't work here because it ignores ns attributes
        c.toFirstContentToken();
        c.insertNamespace(prototype + index, uri);
        c.pop();
        return prototype + index;
    }

    private static void processSimpleType(TopLevelSimpleType simpleType, Type t)
    {
        simpleType.setName(t.getName().getLocalPart());
        addCustomAttributesType(simpleType, t);
        switch (t.getDerivation())
        {
        case Type.DT_RESTRICTION:
            RestrictionDocument.Restriction restriction = simpleType.addNewRestriction();
            restriction.setBase(new QName(t.getBaseTypeUri(), t.getBaseTypeName()));
            break;
        case Type.DT_LIST:
            ListDocument.List list = simpleType.addNewList();
            list.setItemType(new QName(t.getBaseTypeUri(), t.getBaseTypeName()));
            break;
        case 0:
            // No derivation
            break;
        default:
            throw new IllegalStateException("Unrecognized simple type derivation kind: " +
            t.getDerivation() + " when generating Schema for type \"" + t.getName() + "\"");
        }
    }

    private void processComplexType(TopLevelComplexType complexType, Type t, String uri)
    {
        complexType.setName(t.getName().getLocalPart());
        if (t.isAbstract())
            complexType.setAbstract(true);
        fillUpContentForComplexType(t, complexType, uri);
        addCustomAttributesType(complexType, t);
        if (t.getBaseTypeName() != null)
        {
            ComplexContentDocument.ComplexContent cc = complexType.getComplexContent();
            ExtensionType ext;
            if (cc == null)
            {
                cc = complexType.addNewComplexContent();
                ext = cc.addNewExtension();
                if (complexType.getSequence() != null)
                {
                    ext.setSequence(complexType.getSequence());
                    complexType.unsetSequence();
                }
                if (complexType.getChoice() != null)
                {
                    ext.setChoice(complexType.getChoice());
                    complexType.unsetChoice();
                }
                if (complexType.getAttributeArray().length > 0)
                {
                    for (org.apache.xmlbeans.impl.xb.xsdschema.Attribute att :
                        complexType.getAttributeArray())
                        ext.addNewAttribute().set(att);
                    complexType.setAttributeArray(EMPTY_ATTRIBUTE_ARRAY);
                }
            }
            else
                ext = cc.getExtension();
            ext.setBase(new QName(t.getBaseTypeUri(), t.getBaseTypeName()));
        }
        if (t.isOpen())
        {
            ExplicitGroup g;
            if (t.getBaseTypeName() != null)
            {
                ExtensionType e = complexType.getComplexContent().getExtension();
                g = e.getSequence();
                if (g == null)
                    g = e.getChoice();
                if (g == null)
                    g = e.addNewSequence();
            }
            else
            {
                g = complexType.getSequence();
                if (g == null)
                    g = complexType.getChoice();
                if (g == null)
                    g = complexType.addNewSequence();
            }
            AnyDocument.Any any = g.addNewAny();
            any.setMinOccurs(BigInteger.ZERO);
            any.setMaxOccurs("unbounded");
            any.setNamespace("##other");
            complexType.addNewAnyAttribute();
        }
    }

    private boolean elementFormDefaultQualified;

    protected void fillUpLocalElement(org.apache.xmlbeans.impl.inst2xsd.util.Element e,
        LocalElement localElement, String uri)
    {
        Element elem = (Element) e;
        super.fillUpLocalElement(e, localElement, uri);
        if (!e.isRef())
        {
            String elemUri = e.getName().getNamespaceURI();
            if (!elementFormDefaultQualified &&
                elemUri != null && elemUri.length() > 0)
                localElement.setForm(FormChoice.QUALIFIED);
        }
        if (elem.getDefault() != null)
            localElement.setDefault(elem.getDefault());
        addCustomAttributesProperty(localElement, elem);
        if (elem.isHasMany())
            addCustomAttribute(localElement, Names.URI_SDOXML, "many", "false");
    }

    protected void fillUpLocalAttribute(org.apache.xmlbeans.impl.inst2xsd.util.Attribute a,
        org.apache.xmlbeans.impl.xb.xsdschema.Attribute attribute, String uri)
    {
        Attribute attr = (Attribute) a;
        super.fillUpLocalAttribute(a, attribute, uri);
        if (!a.isRef())
        {
            String attUri = a.getName().getNamespaceURI();
            if (attUri != null && attUri.length() > 0)
                attribute.setForm(FormChoice.QUALIFIED);
        }
        if (attr.getDefault() != null)
            attribute.setDefault(attr.getDefault());
        addCustomAttributesProperty(attribute, attr);
    }

    private static boolean hasId(javax.sdo.Type type)
    {
        if (type instanceof TypeXML)
            for (PropertyXML prop : ((TypeXML) type).getPropertiesXML())
            if (prop.getSchemaTypeCode() == SchemaType.BTC_ID)
                return true;
        return false;
    }

    private static String builtinSchemaType(String sdoName)
    {
        return sdoTypeToJavaType.get(sdoName);
    }

    private static String packageName(Class cls)
    {
        if (cls == null)
            return null;
        return cls.getPackage().getName();
    }

    private static boolean sdoDefaultForType(Property prop)
    {
        // Checks if the default value for the property is the same as the "default" default so to speak
        // We rely on the Java class for the property's type
        Class c = prop.getType().getInstanceClass();
        if (c != null && c.isPrimitive())
        {
            if (c.equals(Boolean.TYPE))
                return Boolean.FALSE.equals(prop.getDefault());
            else if (c.equals(Character.TYPE))
                return new Character((char) 0).equals(prop.getDefault());
            else if (c.equals(Byte.TYPE))
                return Byte.valueOf((byte) 0).equals(prop.getDefault());
            else if (c.equals(Short.TYPE))
                return Short.valueOf((short) 0).equals(prop.getDefault());
            else if (c.equals(Integer.TYPE))
                return Integer.valueOf(0).equals(prop.getDefault());
            else if (c.equals(Long.TYPE))
                return Long.valueOf(0l).equals(prop.getDefault());
            else if (c.equals(Float.TYPE))
                return Float.valueOf(0f).equals(prop.getDefault());
            else if (c.equals(Double.TYPE))
                return Double.valueOf(0d).equals(prop.getDefault());
            else if (c.equals(Character.TYPE))
                return new Character((char) 0).equals(prop.getDefault());
            else
                throw new IllegalStateException();
        }
        return false;
    }

    // Names
    // XSD Types
    private static final String XS_BOOLEAN = "boolean";
    private static final String XS_BYTE = "byte";
    private static final String XS_HEXBINARY = "hexBinary";
    private static final String XS_STRING = "string";
    private static final String XS_ANYTYPE = "anyType";
    private static final String XS_DATETIME = "dateTime";
    private static final String XS_GDAY = "gDay";
    private static final String XS_DECIMAL = "decimal";
    private static final String XS_DOUBLE = "double";
    private static final String XS_DURATION = "duration";
    private static final String XS_FLOAT = "float";
    private static final String XS_INT = "int";
    private static final String XS_INTEGER = "integer";
    private static final String XS_LONG = "long";
    private static final String XS_GMONTH = "gMonth";
    private static final String XS_GMONTHDAY = "gMonthDay";
    private static final String XS_ANYSIMPLETYPE = "anySimpleType";
    private static final String XS_SHORT = "short";
    private static final String XS_TIME = "time";
    private static final String XS_GYEAR = "gYear";
    private static final String XS_GYEARMONTH = "gYearMonth";
    private static final String XS_DATE = "date";
    private static final String XS_ANYURI = "anyURI";
    private static final String XS_ID = "ID";
    private static final String XS_IDREF = "IDREF";
    private static final String XS_IDREFS = "IDREFS";

    // URI_SDO Types
    private static final String SDO_BOOLEAN = "Boolean";
    private static final String SDO_BYTE = "Byte";
    private static final String SDO_BYTES = "Bytes";
    private static final String SDO_CHARACTER = "Character";
    private static final String SDO_DATAOBJECT = "DataObject";
    private static final String SDO_DATE = "Date";
    private static final String SDO_DATETIME = "DateTime";
    private static final String SDO_DAY = "Day";
    private static final String SDO_DECIMAL = "Decimal";
    private static final String SDO_DOUBLE = "Double";
    private static final String SDO_DURATION = "Duration";
    private static final String SDO_FLOAT = "Float";
    private static final String SDO_INT = "Int";
    private static final String SDO_INTEGER = "Integer";
    private static final String SDO_LONG = "Long";
    private static final String SDO_MONTH = "Month";
    private static final String SDO_MONTHDAY = "MonthDay";
    private static final String SDO_OBJECT = "Object";
    private static final String SDO_SHORT = "Short";
    private static final String SDO_STRING = "String";
    private static final String SDO_STRINGS = "Strings";
    private static final String SDO_TIME = "Time";
    private static final String SDO_YEAR = "Year";
    private static final String SDO_YEARMONTH = "YearMonth";
    private static final String SDO_YEARMONTHDAY = "YearMonthDay";
    private static final String SDO_URI = "URI";

    private static final String JAVA_LIST = "java.util.List";

    private static final Map<String, String> sdoTypeToJavaType = new HashMap<String, String>(40);
    static
    {
        sdoTypeToJavaType.put(SDO_BOOLEAN, XS_BOOLEAN);
        sdoTypeToJavaType.put(SDO_BYTE, XS_BYTE);
        sdoTypeToJavaType.put(SDO_BYTES, XS_HEXBINARY);
        sdoTypeToJavaType.put(SDO_CHARACTER, XS_STRING);
        sdoTypeToJavaType.put(SDO_DATAOBJECT, XS_ANYTYPE);
        sdoTypeToJavaType.put(SDO_DATE, XS_DATETIME);
        sdoTypeToJavaType.put(SDO_DATETIME, XS_DATETIME);
        sdoTypeToJavaType.put(SDO_DAY, XS_GDAY);
        sdoTypeToJavaType.put(SDO_DECIMAL, XS_DECIMAL);
        sdoTypeToJavaType.put(SDO_DOUBLE, XS_DOUBLE);
        sdoTypeToJavaType.put(SDO_DURATION, XS_DURATION);
        sdoTypeToJavaType.put(SDO_FLOAT, XS_FLOAT);
        sdoTypeToJavaType.put(SDO_INT, XS_INT);
        sdoTypeToJavaType.put(SDO_INTEGER, XS_INTEGER);
        sdoTypeToJavaType.put(SDO_LONG, XS_LONG);
        sdoTypeToJavaType.put(SDO_MONTH, XS_GMONTH);
        sdoTypeToJavaType.put(SDO_MONTHDAY, XS_GMONTHDAY);
        sdoTypeToJavaType.put(SDO_OBJECT, XS_ANYSIMPLETYPE);
        sdoTypeToJavaType.put(SDO_SHORT, XS_SHORT);
        sdoTypeToJavaType.put(SDO_STRING, XS_STRING);
        sdoTypeToJavaType.put(SDO_STRINGS, XS_STRING);
        sdoTypeToJavaType.put(SDO_TIME, XS_TIME);
        sdoTypeToJavaType.put(SDO_YEAR, XS_GYEAR);
        sdoTypeToJavaType.put(SDO_YEARMONTH, XS_GYEARMONTH);
        sdoTypeToJavaType.put(SDO_YEARMONTHDAY, XS_DATE);
        sdoTypeToJavaType.put(SDO_URI, XS_ANYURI);
    }

    private static final org.apache.xmlbeans.impl.xb.xsdschema.Attribute[] EMPTY_ATTRIBUTE_ARRAY =
        new org.apache.xmlbeans.impl.xb.xsdschema.Attribute[0];
}
