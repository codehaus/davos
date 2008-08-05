/*   Copyright 2004 The Apache Software Foundation
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

import org.apache.xmlbeans.impl.common.NameUtil;
import org.apache.xmlbeans.impl.schema.SchemaPropertyImpl;
import org.apache.xmlbeans.impl.schema.SchemaTypeImpl;
import org.apache.xmlbeans.impl.schema.SchemaStringEnumEntryImpl;
import org.apache.xmlbeans.impl.xb.xsdschema.SchemaDocument;
import org.apache.xmlbeans.SchemaAnnotated;
import org.apache.xmlbeans.SchemaAnnotation;
import org.apache.xmlbeans.SchemaAttributeModel;
import org.apache.xmlbeans.SchemaField;
import org.apache.xmlbeans.SchemaLocalElement;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SchemaParticle;
import org.apache.xmlbeans.SchemaProperty;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.SchemaTypeSystem;
import org.apache.xmlbeans.XmlAnySimpleType;
import org.apache.xmlbeans.SchemaStringEnumEntry;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlDecimal;
import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlQName;
import org.xml.sax.EntityResolver;

import davos.sdo.Options;
import davos.sdo.SDOError;
import davos.sdo.TypeXML;
import davos.sdo.PropertyXML;
import davos.sdo.SDOBindingException;
import davos.sdo.SDOContextFactory;
import davos.sdo.binding.BindingContext;
import davos.sdo.binding.BindingSystem;
import davos.sdo.impl.common.Common;
import davos.sdo.impl.common.Names;
import davos.sdo.impl.type.BuiltInTypeSystem;
import davos.sdo.impl.type.PropertyImpl;
import davos.sdo.impl.type.TypeSystemBase;
import davos.sdo.impl.type.TypeImpl;
import davos.sdo.impl.type.SimpleValueHelper;
import davos.sdo.impl.util.DefaultFilerImpl;
import davos.sdo.impl.util.ExceptionLoggerImpl;
import davos.sdo.type.TypeSystem;

import java.util.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.BigInteger;

import javax.xml.namespace.QName;

public final class Schema2SDO
{
    public static final QName SDO_NAME = new QName(Names.URI_SDOXML, "name");
    public static final QName SDO_MANY = new QName(Names.URI_SDOXML, "many");
    public static final QName SDO_NULLABLE = new QName(Names.URI_SDOXML, "nullable");
    public static final QName SDO_READ_ONLY = new QName(Names.URI_SDOXML, "readOnly");
    public static final QName SDO_PROPERTY_TYPE = new QName(Names.URI_SDOXML, "propertyType");
    public static final QName SDO_OPPOSITE_PROPERTY = new QName(Names.URI_SDOXML, "oppositeProperty");
    public static final QName SDO_DATA_TYPE = new QName(Names.URI_SDOXML, "dataType");
    public static final QName SDO_STRING = new QName(Names.URI_SDOXML, "string");
    public static final QName SDO_SEQUENCE = new QName(Names.URI_SDOXML, "sequence");
    public static final QName SDO_PACKAGE = new QName(Names.URI_SDOJAVA, "package");
    public static final QName SDO_INSTANCE_CLASS = new QName(Names.URI_SDOJAVA, "instanceClass");
    public static final QName SDO_EXTENDED_INSTANCE_CLASS = new QName(Names.URI_SDOJAVA, "extendedInstanceClass");
    public static final QName SDO_ALIAS_NAME = new QName(Names.URI_SDOXML, "aliasName");

    private static final QName SDO_STRING_TYPE = new QName(Names.URI_SDO, "String");

    private static final int MANY_UNSPECIFIED = 0;
    private static final int MANY_TRUE = 1;
    private static final int MANY_FALSE = 2;

    // What kind of naming strategy for types we employ by default
    /*package*/static final int DEFAULT_TYPE_NAMING = Options.NAMES_COMPOSITE;
    private static final char TYPE_NAME_SEPARATOR_ELEMENT = '$';
    private static final char TYPE_NAME_SEPARATOR_ATTRIBUTE = '@';

    private static class Name
    {
        private String uri;
        private String localName;
        private boolean original;
        String getLocalName()
        {
            return localName;
        }
        void setLocalName(String localName)
        {
            this.localName = localName;
        }
        boolean isOriginal()
        {
            return original;
        }
        void setOriginal(boolean original)
        {
            this.original = original;
        }
        String getUri()
        {
            return uri;
        }
        void setUri(String uri)
        {
            this.uri = uri;
        }
    }

    private static class OppositePropertyInfo
    {
        private TypeXML type;
        private PropertyXML property;
        private TypeXML targetType;
        private String targetPropertyName;

        public TypeXML getType()
        {
            return type;
        }

        public PropertyXML getProperty()
        {
            return property;
        }

        public TypeXML getTargetType()
        {
            return targetType;
        }

        public String getTargetPropertyName()
        {
            return targetPropertyName;
        }

        public OppositePropertyInfo(TypeXML type, PropertyXML property, TypeXML targetType, String targetPropertyName)
        {
            this.type = type;
            this.property = property;
            this.targetType = targetType;
            this.targetPropertyName = targetPropertyName;
        }
    }

    private static class SubstitutionsInfo
    {
        private QName[] acceptedNames;
        private PropertyXML[] acceptedProperties;

        public QName[] getAcceptedNames()
        {
            return acceptedNames;
        }

        public PropertyXML[] getAcceptedProperties()
        {
            return acceptedProperties;
        }

        public SubstitutionsInfo(QName[] acceptedNames, PropertyXML[] acceptedProperties)
        {
            this.acceptedNames = acceptedNames;
            this.acceptedProperties = acceptedProperties;
        }
    }

    public static TypeSystem createSDOTypeSystem(String xsd, BindingSystem bs)
    {
        return createSDOTypeSystem(xsd, null, bs, null);
    }

    public static TypeSystem createSDOTypeSystem(String xsd, String location, BindingSystem bs,
        Object options)
    {
        return createSDOTypeSystem(new String[]{xsd}, new String[] {location}, bs, options);
    }

    public static TypeSystem createSDOTypeSystem(String[] xsds, BindingSystem bs, Object options)
    {
        return createSDOTypeSystem(xsds, null, bs, options);
    }

    public static TypeSystem createSDOTypeSystem(String[] xsds, String[] schemaLocations,
        BindingSystem bs, Object options)
    {
        if (schemaLocations != null && schemaLocations.length != xsds.length)
            throw new IllegalArgumentException("The 'schemaLocation' array has to have the same length as the " +
                "'xsds' array");
        try
        {
            XmlObject[] schemas = new XmlObject[xsds.length];
            SchemaTypeLoader currentTypeLoader = XmlBeans.typeLoaderForClassLoader(Schema2SDO.class.getClassLoader());
            for (int i = 0; i < xsds.length; i++)
            {
                schemas[i] = currentTypeLoader.parse(xsds[i], null, null);
                schemas[i].documentProperties().setSourceName(schemaLocations[i]);
            }
            SchemaTypeLoader linker = bs == null ? null : bs.getTypeSystem().getSchemaTypeLoader();
            SchemaTypeSystem sts = XmlBeans.compileXsd(schemas, linker, getXmlOptionsFromObject(schemas, options));
            return createSDOTypeSystem(sts, bs, new HashMap<TypeXML, String>(),
                new HashMap<TypeXML, String>(), null, options);
        }
        catch (XmlException e)
        {
            throw new SDOBindingException(SDOError.messageForCode("binding.schema.readProblem"), e);
        }
    }

    public static TypeSystem createSDOTypeSystem(Reader xsdReader, String schemaLocation,
        BindingSystem bs)
    {
        return createSDOTypeSystem(new Reader[]{xsdReader}, new String[] {schemaLocation}, bs, null);
    }

    public static TypeSystem createSDOTypeSystem(Reader xsdReader, String schemaLocation,
        BindingSystem bs, Object options)
    {
        return createSDOTypeSystem(new Reader[]{xsdReader}, new String[] {schemaLocation}, bs, options);
    }

    public static TypeSystem createSDOTypeSystem(Reader[] xsdReaders, String[] schemaLocations,
        BindingSystem bs, Object options)
    {
        try
        {
            XmlObject schemas[] = new XmlObject[xsdReaders.length];
            SchemaTypeLoader currentTypeLoader = XmlBeans.typeLoaderForClassLoader(Schema2SDO.class.getClassLoader()); 
            for (int i = 0; i < schemas.length; i++)
            {
                schemas[i] = currentTypeLoader.parse(xsdReaders[i], null, null);
                schemas[i].documentProperties().setSourceName(schemaLocations[i]);
            }
            SchemaTypeLoader linker = bs == null ? null : bs.getTypeSystem().getSchemaTypeLoader();
            SchemaTypeSystem sts = XmlBeans.compileXsd(schemas, linker, getXmlOptionsFromObject(schemas, options));
            return createSDOTypeSystem(sts, bs, new HashMap<TypeXML, String>(),
                new HashMap<TypeXML, String>(), null, options);
        }
        catch (XmlException e)
        {
            throw new SDOBindingException(SDOError.messageForCode("binding.schema.compileProblem"), e);
        }
        catch (IOException e)
        {
            throw new SDOBindingException(SDOError.messageForCode("binding.schema.readProblem"), e);
        }
    }

    public static TypeSystem createSDOTypeSystem(InputStream xsdInputStream, String schemaLocation, BindingSystem bs)
    {
        return createSDOTypeSystem(new InputStream[] {xsdInputStream}, new String[] {schemaLocation}, bs, null);
    }

    public static TypeSystem createSDOTypeSystem(InputStream xsdInputStream, String schemaLocation, BindingSystem bs,
        Object options)
    {
        return createSDOTypeSystem(new InputStream[] {xsdInputStream}, new String[] {schemaLocation}, bs, options);
    }

    public static TypeSystem createSDOTypeSystem(InputStream[] xsdInputStreams, String[] schemaLocations, BindingSystem bs,
        Object options)
    {
        try
        {
            XmlObject[] schemas = new XmlObject[xsdInputStreams.length];
            SchemaTypeLoader currentTypeLoader = XmlBeans.typeLoaderForClassLoader(Schema2SDO.class.getClassLoader()); 
            for (int i = 0; i < xsdInputStreams.length; i++)
            {
                schemas[i] = currentTypeLoader.parse(xsdInputStreams[i], null, null);
                schemas[i].documentProperties().setSourceName(schemaLocations[i]);
            }
            SchemaTypeLoader linker = bs == null ? null : bs.getTypeSystem().getSchemaTypeLoader();
            SchemaTypeSystem sts = XmlBeans.compileXsd(schemas, linker, getXmlOptionsFromObject(schemas, options));
            Map<TypeXML, String> instanceClasses = new HashMap<TypeXML, String>();
            TypeSystem newTypeSystem = createSDOTypeSystem(sts, bs, new HashMap<TypeXML, String>(), instanceClasses, null, options);

            return newTypeSystem;
        }
        catch (XmlException e)
        {
            throw new SDOBindingException(SDOError.messageForCode("binding.schema.compileProblem"), e);
        }
        catch (IOException e)
        {
            throw new SDOBindingException(SDOError.messageForCode("binding.schema.readProblem"), e);
        }
    }

    public static TypeSystem createSDOTypeSystemFromWsdl(String wsdlLocation, BindingSystem bs, Object options)
    {
        WsdlParser p = WsdlParser.newInstance(wsdlLocation);
        SchemaTypeSystem sts;
        try
        {
            p.parse();
            sts = p.generateSchemaTypeSystem(bs);
            return createSDOTypeSystem(sts, bs, new HashMap<TypeXML, String>(),
                new HashMap<TypeXML, String>(), null, options);
        }
        catch (IOException e)
        {
            throw new SDOBindingException(SDOError.messageForCode("binding.wsdl.readProblem", wsdlLocation), e);
        }
        catch (WsdlParser.WsdlException we)
        {
            throw new SDOBindingException(SDOError.messageForCode("binding.wsdl.compileProblem", wsdlLocation), we);
        }
        catch (org.xml.sax.SAXException se)
        {
            throw new SDOBindingException(SDOError.messageForCode("binding.wsdl.compileProblem", wsdlLocation), se);
        }
        catch (XmlException xe)
        {
            throw new SDOBindingException(SDOError.messageForCode("binding.wsdl.schemaProblem", wsdlLocation), xe);
        }
    }

    // ==============================================================
    // Class-level fields
    // ==============================================================
    /**
     * An SDO BindingSystem that is used to resolve references to SDO types that are not part
     * of the current compilation process (are precompiled)
     */
    private final BindingSystem linkTo;
    /**
     * Output parameter: the list of package names to be used for different Java classes based on
     * the annotations found in the Schema
     */
    private final Map<TypeXML, String> packageNames;
    /**
     * Output parameter: the list of user-defined type names to be used in the Java generation
     * process in place of the normal Java primitive classes
     */
    private final Map<TypeXML, String> instanceClasses;
    /**
     * A context used for error reporting
     */
    private final BindingContext ctx;
    /**
     * List with all SchemaTypes in the current SchemaTypeSystem
     */
    private final ArrayList<SchemaType> allSeenTypes;
    /**
     * Mapping from Schema type to the name of the corresponding generated SDO type. Used so that
     * names can be resolved before the full type is known (for references)
     */
    private final HashMap<SchemaType, Name> sdoTypeNames;
    /**
     * Output: the resulting SDO type system
     */
    private final TypeSystemBase result;
    /**
     * Mapping from SchemaType to SDO type, also used for resolving references to SchemaTypes to
     * the corresponding SDO type
     */
    private final HashMap<SchemaType, TypeXML> typeMapping;
    /**
     * Map holding all the properties for a given SDO Type, because SDO Types may have additional
     * properties added to them when resolving another type because of opposite properties, but
     * the TypeImpl class is immutable so there is only one shot at adding properties on it
     */
    private final HashMap<TypeXML, List<PropertyXML>> registeredProperties;
    /**
     * Global set with all used SDO names, used to avoid name conflicts
     */
    private final HashSet<String> usedTopLevelTypeNames;
    /**
     * List with declaraions of oppositeProperties which will be fully resolved only after all the
     * types are resolved
     */
    private final List<OppositePropertyInfo> oppositePropertiesDecls;
    /**
     * List with all the properties that have substitutions to be resolved after all the global
     * properties are created
     */
    private final List<SubstitutionsInfo> substitutionProperties;
    /**
     * Local type naming strategy, see {@link davos.sdo.Options#setCompileAnonymousTypeNames(int)}
     */
    private int localTypeNames = DEFAULT_TYPE_NAMING;

    /**
     * Does a walk of all the types to resolve them.
     */
    public static TypeSystem createSDOTypeSystem(SchemaTypeSystem sts,
        BindingSystem linkTo, Map<TypeXML, String> packageNames,
        Map<TypeXML, String> instanceClasses, BindingContext ctx)
    {
        return createSDOTypeSystem(sts, linkTo, packageNames, instanceClasses, ctx, null);
    }

    /**
     * Does a walk of all the types to resolve them.
     */
    public static TypeSystem createSDOTypeSystem(SchemaTypeSystem sts,
        BindingSystem linkTo, Map<TypeXML, String> packageNames,
        Map<TypeXML, String> instanceClasses, BindingContext ctx, Object options)
    {
        return new Schema2SDO(sts, linkTo, packageNames, instanceClasses, ctx, options).
            createSDOTypeSystem();
    }

    private Schema2SDO(SchemaTypeSystem sts,
        BindingSystem linkTo, Map<TypeXML, String> packageNames,
        Map<TypeXML, String> instanceClasses, BindingContext ctx, Object options)
    {
        this.linkTo = linkTo == null ? SDOContextFactory.getGlobalSDOContext().getBindingSystem() : linkTo;
        this.packageNames = packageNames;
        this.instanceClasses = instanceClasses;
        this.ctx = ctx == null ?  new DefaultBindingContext(
            new DefaultFilerImpl(new File("."), new File(".")),
            sts.getName(), new ExceptionLoggerImpl()) : ctx;
        boolean skipTypesFromContext = false;
        boolean useTypeSystem = false;
        if (options != null)
        {
            Map map = null;
            if (options instanceof Map)
                map = (Map) options;
            else if (options instanceof Options)
                map = ((Options) options).getMap();
            if (map != null)
            {
                if (map.containsKey(Options.COMPILE_SKIP_IF_KNOWN))
                {
                    skipTypesFromContext = true;
                    Object value = map.get(Options.COMPILE_SKIP_IF_KNOWN);
                    // Special option passed in by the SDO context based on SchemaTypeLoader 
                    if (value != null && Integer.valueOf(2).equals(value))
                        useTypeSystem = true;
                }
                if (map.containsKey(Options.COMPILE_ANONYMOUS_TYPE_NAMES))
                {
                    Object value = map.get(Options.COMPILE_ANONYMOUS_TYPE_NAMES);
                    if (value instanceof Integer)
                    {
                        localTypeNames = (Integer) value;
                        if (localTypeNames < 1 || localTypeNames > 3)
                            localTypeNames = DEFAULT_TYPE_NAMING;
                    }
                }
            }
        }
        allSeenTypes = new ArrayList<SchemaType>();
        if (skipTypesFromContext)
        {
            // If we're required to skip types that already exist in the context (so that we
            // avoid trying to register the same type into the context twice), go through all
            // the global types and check the binding system for the existence of another global
            // type/element/attribute with the same name. If it exists, don't add it to the list,
            // this way all the references will be resolved against the type from the binding system

            // There are (currently 07/30/2007) two ways to look for types in a BindingSystem: one
            // is to look at the types that the BindingSystem *can* load (the "correct" way) and
            // another is to look at the types that the BindingSystem has already loaded. In some
            // situations, like an SDO Context based on on-demand SDO compilation, we can't use the
            // "correct" way because a request for a type in the current Schema will trigger another
            // compilation of this Schema, leading to stack overflow
            if (useTypeSystem)
            {
                SchemaType[] stArray = sts.documentTypes();
                TypeSystem ts = linkTo.getTypeSystem();
                for (SchemaType st : stArray)
                    if (ts.getGlobalPropertyByTopLevelElemQName(
                        st.getDocumentElementName().getNamespaceURI(),
                        st.getDocumentElementName().getLocalPart()) == null)
                        allSeenTypes.add(st);
                stArray = sts.attributeTypes();
                for (SchemaType st : stArray)
                    if (ts.getGlobalPropertyByTopLevelAttrQName(
                        st.getAttributeTypeAttributeName().getNamespaceURI(),
                        st.getAttributeTypeAttributeName().getLocalPart()) == null)
                        allSeenTypes.add(st);
                stArray = sts.globalTypes();
                for (SchemaType st : stArray)
                    if (ts.getTypeBySchemaTypeName(
                        st.getName().getNamespaceURI(),
                        st.getName().getLocalPart()) == null)
                        allSeenTypes.add(st);
            }
            else
            {
                SchemaType[] stArray = sts.documentTypes();
                for (SchemaType st : stArray)
                    if (linkTo.loadGlobalPropertyByTopLevelElemQName(
                        st.getDocumentElementName().getNamespaceURI(),
                        st.getDocumentElementName().getLocalPart()) == null)
                        allSeenTypes.add(st);
                stArray = sts.attributeTypes();
                for (SchemaType st : stArray)
                    if (linkTo.loadGlobalPropertyByTopLevelAttrQName(
                        st.getAttributeTypeAttributeName().getNamespaceURI(),
                        st.getAttributeTypeAttributeName().getLocalPart()) == null)
                        allSeenTypes.add(st);
                stArray = sts.globalTypes();
                for (SchemaType st : stArray)
                    if (linkTo.loadTypeBySchemaTypeName(
                        st.getName().getNamespaceURI(),
                        st.getName().getLocalPart()) == null)
                        allSeenTypes.add(st);
            }
        }
        else
        {
            allSeenTypes.addAll(Arrays.asList(sts.documentTypes()));
            allSeenTypes.addAll(Arrays.asList(sts.attributeTypes()));
            allSeenTypes.addAll(Arrays.asList(sts.globalTypes()));
        }
        sdoTypeNames = new HashMap<SchemaType, Name>(allSeenTypes.size());
        result = TypeSystemBase.createEmptyTypeSystem();
        result.setSchemaTypeLoader(sts);
        typeMapping = new HashMap<SchemaType, TypeXML>(allSeenTypes.size());
        registeredProperties = new HashMap<TypeXML, List<PropertyXML>>();
        usedTopLevelTypeNames = new HashSet<String>();
        oppositePropertiesDecls = new ArrayList<OppositePropertyInfo>();
        substitutionProperties = new ArrayList<SubstitutionsInfo>();
    }

    private TypeSystem createSDOTypeSystem()
    {
        // Assign names to all the types with sdo:name annotation
        for (int i = 0; i < allSeenTypes.size(); i++)
        {
            SchemaType sType = allSeenTypes.get(i);
            if (!(sType.isDocumentType() || sType.isAttributeType()))
            {
                QName givenName = getSDOConfiguredName(sType);
                if (givenName != null)
                {
                    sdoTypeNames.put(sType, pickFullSDOName(givenName, sType, true));
                }
            }

            // We need to add the anonymous types to the list, recursively
            allSeenTypes.addAll(Arrays.asList(sType.getAnonymousTypes()));
            // The above did not include anonymous types defined inside redefined types
            // so we need to add those as well (except when the redefinition is by restriction
            addAnonymousTypesFromRedefinition(sType, allSeenTypes);
        }

        // Pick names for the remaining types and create all types
        for (int i = 0; i < allSeenTypes.size(); i++)
        {
            SchemaType type = allSeenTypes.get(i);
            if (type.isDocumentType() || type.isAttributeType())
                continue;
            // We don't allow redefinition of any of the built-in SDO types
            if (type.getName() != null &&
                    type.getName().getNamespaceURI().startsWith(Names.URI_SDO))
                continue;
            Name sdoName = sdoTypeNames.get(type);
            if (sdoName == null && (type.getName() != null ||
                    type.isAnonymousType() &&
                    (type.getOuterType().isDocumentType() ||
                            type.getOuterType().isAttributeType())))
            {
                QName name = getSDODefaultName(type);
                sdoName = pickFullSDOName(name, type, false);
                sdoTypeNames.put(type, sdoName);
            }
            if (sdoName == null)
                assert type.isSkippedAnonymousType() : "By this point, either the type was global and " +
                    "the new name was just created, or local, in which case the name should have " +
                    "been created when processing the outer type.";
                else
            {
                    TypeXML sdoType = createSDOType(type, sdoName);
                    typeMapping.put(type, sdoType);
                    assignJavaAnonymousTypeNames((SchemaTypeImpl)type);
            }
        }

        // Resolve all SDO types
        for (SchemaType type : allSeenTypes)
        {
            if (type.isDocumentType())
                addGlobalElementMapping(type);
            else if (type.isAttributeType())
                addGlobalAttributeMapping(type);
            else
                resolveSDOType(type);
        }

        resolveOppositeProperties();
        resolveSubstitutions();

        // Close all types
        for (TypeXML type : typeMapping.values())
            ((TypeImpl) type).makeImmutable();

        return result;
    }

    private void addGlobalElementMapping(SchemaType t)
    {
        // A document type must have a content model consisting of a single elt
        if (t.getContentModel() == null || t.getContentModel().getParticleType() != SchemaParticle.ELEMENT)
            throw new IllegalStateException();
        SchemaProperty theProp = t.getProperties()[0];
        PropertyXML sdoProp = createSDOPropertyFromSchema(theProp, t);
        result.addGlobalProperty(sdoProp);
    }

    private void addGlobalAttributeMapping(SchemaType t)
    {
        // A document type must have a content model consisting of a single elt
        if (t.getAttributeModel() == null || t.getAttributeModel().getAttributes().length != 1)
            throw new IllegalStateException();
        SchemaProperty theProp = t.getProperties()[0];
        PropertyXML sdoProp = createSDOPropertyFromSchema(theProp, t);
        result.addGlobalProperty(sdoProp);
    }

    private TypeXML createSDOType(SchemaType type, Name name)
    {
        TypeImpl result = TypeImpl.create();
        result.init(name.getLocalName(), name.getUri(), type, this.result);

        // Register the newly created type
        this.result.addTypeMapping(result);

        return result;
    }

    private static boolean isStringType(SchemaType type)
    {
        if (type == null || type.getSimpleVariety() != SchemaType.ATOMIC)
            return false;
        return (type.getPrimitiveType().getBuiltinTypeCode() == SchemaType.BTC_STRING);
    }

    static void skipJavaizingType(SchemaTypeImpl sImpl)
    {
        if (sImpl.isJavaized())
            return;

        SchemaTypeImpl baseType = (SchemaTypeImpl)sImpl.getBaseType();
        if (baseType != null)
            skipJavaizingType(baseType);

        sImpl.startJavaizing();
        secondPassProcessType(sImpl);
        sImpl.finishJavaizing();
    }

    static void secondPassProcessType(SchemaTypeImpl sImpl)
    {
        if (isStringType(sImpl))
        {
            XmlAnySimpleType[] enumVals = sImpl.getEnumerationValues();

            // if this is an enumerated string type, values are to be
            // javaized as constants.
            if (enumVals != null)
            {
                SchemaStringEnumEntry[] entryArray = new SchemaStringEnumEntry[enumVals.length];
                SchemaType basedOn = sImpl.getBaseEnumType();
                if (basedOn == sImpl)
                {
                    Set usedNames = new HashSet();
                    for (int i = 0; i < enumVals.length; i++)
                    {
                        String val = enumVals[i].getStringValue();

                        entryArray[i] = new SchemaStringEnumEntryImpl(val, i + 1, pickConstantName(usedNames, val));
                    }
                }
                else
                {
                    for (int i = 0; i < enumVals.length; i++)
                    {
                        String val = enumVals[i].getStringValue();
                        entryArray[i] = basedOn.enumEntryForString(val);
                    }
                }
                sImpl.setStringEnumEntries(entryArray);
            }
        }
    }

    private void resolveSDOType(SchemaType st)
    {
        TypeImpl sdoType = (TypeImpl) typeMapping.get(st);
        if (sdoType == null)
            return;
        if (isResolved(sdoType))
            return;

        TypeXML sdoBaseType;
        Class javaClass;
        boolean dataType;
        boolean open;
        boolean sequenced;
        boolean simpleContent = false;
        boolean mixed;
        List<String> aliasNames;
        List<PropertyXML> declaredProperties;
        String packageName = null;
        String instanceClass = null;
        TypeXML listItemType = null;
        boolean customizedInstanceClass = false;

        SchemaType baseType = st.getBaseType();
        // For redefinitions, the base type name is going to be equal to this
        // type's name
        // Also, we don't include anonymous base types in the hierarchy
        while (baseType.isAnonymousType() || baseType.getName().equals(st.getName()))
            baseType = baseType.getBaseType();
        sdoBaseType = typeMapping.get(baseType);
        if (sdoBaseType == null)
        {
            // Try and import the type
            sdoBaseType = linkTo.loadTypeBySchemaTypeName(baseType.getName().
                getNamespaceURI(), baseType.getName().getLocalPart());
            if (sdoBaseType == null)
                throw new SDOBindingException(SDOError.messageForCode("binding.type.notfound",
                    baseType.getName()));
        }
        else
            resolveSDOType(baseType);

        if (st.getSimpleVariety() == SchemaType.UNION)
        {
            // Resolve the member types of the union
            for (SchemaType memberType : st.getUnionMemberTypes())
            {
                if (typeMapping.containsKey(memberType))
                    resolveSDOType(memberType);
            }
        }

        // secondPassProcessType((SchemaTypeImpl)st);

        aliasNames = getSDOConfiguredAliasNames(st);

        if (st.isSimpleType())
        {
            dataType = true;
            javaClass = null;
            sequenced = false;
            mixed = false;
            declaredProperties = Common.EMPTY_PROPERTYXML_LIST;
            open = false;

            if (st.getSimpleVariety() == SchemaType.UNION ||
                    st.getSimpleVariety() == SchemaType.LIST)
                sdoBaseType = null; // No base type as per spec

            if (isLargerThanInteger(st) && ((st.getDecimalSize() > 0 &&
                st.getDecimalSize() <= Integer.SIZE) || isEnumerationOfInt(st)))
            {
                // As per spec, the instance class becomes int
                javaClass = int.class;
                if (sdoBaseType.getInstanceClass() != int.class)
                    sdoBaseType = null;
            }

            instanceClass = getSDOConfiguredInstanceClass(st);
            String extendedInstanceClass = getSDOConfiguredExtendedInstanceClass(st);
            if (instanceClass != null)
            {
                sdoBaseType = null; // No base type as per spec
                customizedInstanceClass = true;
            }
            else if (extendedInstanceClass != null)
            {
                instanceClass = extendedInstanceClass;
                customizedInstanceClass = true;
            }
            else if (st.getSimpleVariety() == SchemaType.UNION)
            {
                SchemaType[] memberTypes = st.getUnionMemberTypes();
                // As per spec, we check to see if all member types map to the
                // same Java class
                boolean same = true;
                for (SchemaType t : memberTypes)
                {
                    TypeXML s = typeMapping.get(t);
                    if (s == null)
                        s = linkTo.loadTypeBySchemaTypeName(t.getName().
                            getNamespaceURI(), t.getName().getLocalPart());
                    if (s == null)
                        throw new SDOBindingException(SDOError.messageForCode("binding.type.notfound",
                            t.getName()));
                    if (javaClass == null)
                        javaClass = s.getInstanceClass();
                    else if (!javaClass.equals(s.getInstanceClass()))
                    {
                        same = false;
                        break;
                    }
                }
                if (!same)
                    javaClass = Object.class;
            }
            else if (st.getSimpleVariety() == SchemaType.LIST)
            {
                javaClass = List.class;
                SchemaType listSchemaType = st.getListItemType();
                assert listSchemaType != null : "Type '" + st + "' is list but .getListItemType() returned null";
                listItemType = typeMapping.get(listSchemaType);
                if (listItemType == null)
                {
                    // Try and import the type
                    listItemType = linkTo.loadTypeBySchemaTypeName(listSchemaType.getName().
                        getNamespaceURI(), listSchemaType.getName().getLocalPart());
                    if (listItemType == null)
                        throw new SDOBindingException(SDOError.messageForCode("binding.type.notfound",
                            listSchemaType.getName()));
                }
                else
                    resolveSDOType(listSchemaType);
            }
            else
            {
                // Create the instance class for the java.lang types
                javaClass = sdoBaseType == null ? (javaClass == null ? Object.class : javaClass) :
                    sdoBaseType.getInstanceClass();
                if (javaClass == null)
                {
                    // The base type has not class set, must be customized
                    instanceClass = instanceClasses.get(sdoBaseType);
                }
            }
        }
        else
        {
            dataType = false;
            javaClass = null;
            int configuredSequence = getSDOConfiguredSequence(st);
            open = st.hasAttributeWildcards() || st.hasElementWildcards();
            mixed = st.getContentType() == SchemaType.MIXED_CONTENT;

            packageName = getSDOConfiguredPackage(st);
            SchemaProperty[] eltProps = st.getElementProperties();
            SchemaProperty[] attrProps = st.getAttributeProperties();

            // Handing out java names - this permits us to avoid collisions.
            Set<String> usedPropNames = new HashSet<String>();

            // First, copy all used property names from base, since these
            // cannnot be changed at this point and they may be arbitrary
            // because of derivation by restriction and the "nopvr" switch
            List<PropertyXML> baseProps = getPropertiesXML(sdoBaseType);
            for (int i = 0; i < baseProps.size(); i++)
            {
                String propName = baseProps.get(i).getName();
                if (usedPropNames.contains(propName))
                    throw new IllegalStateException();
                usedPropNames.add(propName);
            }

            declaredProperties = new ArrayList<PropertyXML>();
            // If complex type extending from simple type, we need to
            // add the special "value" property
            if (baseType.isSimpleType())
            {
                PropertyImpl valueProp = PropertyImpl.create();
                valueProp.initMutable(sdoBaseType,
                    pickSDOPropertyName(usedPropNames, Names.SIMPLE_CONTENT_PROP_NAME, null, st, false),
                    baseType.getSimpleVariety() == SchemaType.LIST, true,
                    sdoType, null, false, true, null, Common.EMPTY_STRING_LIST, false,
                    Names.SIMPLE_CONTENT_PROP_NAME, Common.EMPTY_STRING,
                    Common.getBuiltinTypeCode(baseType), false, false);
                valueProp.addPropertyValue(BuiltInTypeSystem.P_PROPERTY_XMLELEMENT, Boolean.FALSE);
                valueProp.makeImmutable();
                declaredProperties.add(valueProp);
                sdoBaseType = null;
                simpleContent = true;
            }
            else if (baseType.getContentType() == SchemaType.SIMPLE_CONTENT)
            {
                // Complex type with simple content extending from another complex
                // type with simple content
                simpleContent = true;
            }

            // Assign names in two passes: first inherited names, then others.
            for (boolean doInherited = true; ; doInherited = false)
            {
                if (eltProps.length > 0)
                    assignSDOPropertyNames(usedPropNames, eltProps, declaredProperties, baseType,
                        sdoType, sdoBaseType, doInherited);

                assignSDOPropertyNames(usedPropNames, attrProps, declaredProperties, baseType,
                    sdoType, sdoBaseType, doInherited);

                if (!doInherited)
                    break;
            }

            // determine whether order sensitive
            sequenced = configuredSequence == 0 ? st.getContentType() == SchemaType.MIXED_CONTENT ||
                (st.getContentType() == SchemaType.ELEMENT_CONTENT &&
                isPropertyModelOrderSensitive(st.getContentModel(), new HashSet<QName>())) :
                configuredSequence < 0 ? false: true;
        }

        boolean instanceClassNotFound = false;
        if (instanceClass!=null)
            try {
                javaClass = findJavaClass(instanceClass);
            } catch(SDOBindingException e)
            {
                if (e.getCause()==null || !(e.getCause() instanceof ClassNotFoundException))
                    throw e;
                else
                    instanceClassNotFound = true;
            }
        

        // in case there is an inner class and instanceClass is using "." instead of "$" java.package.OuterClass.InnerClass
        if (instanceClassNotFound)
        {
            String dollarName = instanceClass;
            int dotIndex;
            while ( instanceClassNotFound && (dotIndex = dollarName.lastIndexOf('.')) >= 0 )
            {
                dollarName = dollarName.substring(0, dotIndex) + "$" + dollarName.substring(dotIndex+1, dollarName.length());
                try {
                    javaClass = findJavaClass(dollarName);
                    instanceClassNotFound = false;
                } catch(SDOBindingException e)
                {
                    if (e.getCause()==null || !(e.getCause() instanceof ClassNotFoundException))
                        throw e;
                }
            }

            // still not found throw exception
            if (instanceClassNotFound)
                throw new SDOBindingException(SDOError.messageForCode("binding.annotation.classNotFound", instanceClass));
        }

        // We have all the information: create the type
        sdoType.addResolveInfo(-1, javaClass, dataType, open, sequenced, st.isAbstract(),
            simpleContent, mixed,
            sdoBaseType == null ? Common.EMPTY_TYPEXML_LIST : Collections.singletonList(sdoBaseType),
            declaredProperties, aliasNames == null ? Common.EMPTY_STRING_LIST : aliasNames,
            listItemType, customizedInstanceClass);

        if (packageName != null)
            packageNames.put(sdoType, packageName);

        if (instanceClass != null)
        {
            instanceClasses.put(sdoType, instanceClass);
            sdoType.addPropertyValue(BuiltInTypeSystem.P_TYPE_JAVACLASS, instanceClass);
        }
    }

    private Class findJavaClass(String instanceClassName)
    {
        Class clasz = linkTo.getInstanceClassForJavaName(instanceClassName);
        if (clasz == null)
            try
            {
                clasz = this.getClass().getClassLoader().loadClass(instanceClassName);
            }
            catch (ClassNotFoundException e)
            {
                // do nothing, continue to work as if without instanceClass annotation
            }

        return clasz;
    }

    private static final String[] PREFIXES = new String[]{"get", "xget", "isNil", "isSet", "sizeOf", "set",
                "xset", "addNew", "setNil", "unset", "insert", "add", "insertNew", "addNew", "remove"};

    private void assignJavaAnonymousTypeNames(SchemaTypeImpl outerType)
    {
//        Set<String> usedTypeNames = new HashSet<String>();
        SchemaType[] anonymousTypes = outerType.getAnonymousTypes();
        Name sdoOuterTypeName = sdoTypeNames.get(outerType);
        String outerName = sdoOuterTypeName.getLocalName();
        String uri = sdoOuterTypeName.getUri();

        int nrOfAnonTypes = anonymousTypes.length;
        if (outerType.isRedefinition())
        {
            // We have to add the anonymous types for redefinitions to the list
            // since they don't have another outer class
            ArrayList<SchemaType> list = new ArrayList<SchemaType>();
            addAnonymousTypesFromRedefinition(outerType, list);
            if (list.size() > 0)
            {
                SchemaType[] temp = new SchemaType[nrOfAnonTypes + list.size()];
                list.toArray(temp);
                System.arraycopy(anonymousTypes, 0, temp, list.size(), nrOfAnonTypes);
                anonymousTypes = temp;
            }
        }

//        // Because we generate nested java interfaces, and nested
//        // interface names must not be the same as an ancestor, use up
//        // the ancestors
//        for ( SchemaType scanOuterType = outerType ;
//              scanOuterType != null && !scanOuterType.isDocumentType() &&
//              !scanOuterType.isAttributeType();
//              scanOuterType = scanOuterType.getOuterType() )
//        {
//            usedTypeNames.add(getInnermostName(sdoTypeNames.get(scanOuterType).
//                getLocalName().toLowerCase()));
//        }

        // TODO(radup) Also guard the impl class names
//        for ( SchemaType scanOuterType = outerType ;
//              scanOuterType != null ;
//              scanOuterType = scanOuterType.getOuterType() )
//        {
//            usedTypeNames.add( scanOuterType.getShortJavaImplName() );
//        }

//        // Some of the types have names already assigned by the user
//        for (SchemaType t : anonymousTypes)
//        {
//            Name n = sdoTypeNames.get(t);
//            if (n != null)
//            {
//                String usedName = n.getLocalName().toLowerCase();
//                if (usedTypeNames.contains(usedName))
//                    throw new IllegalStateException("Type name \"" + n.getLocalName() +
//                        "\" is already used in this context");
//                usedTypeNames.add(usedName);
//                if (!n.isOriginal())
//                {
//                    // Change it to a "local" name
//                    String name = n.getLocalName();
//                    n.setLocalName(outerName + "." + name);
//                }
//            }
//        }

        // Since a union type can contain more than one anonymous type inside, we
        // need to make sure that they get assigned distinct names
        int anontypesinunion = 0;
        // assign names
        for (int i = 0; i < anonymousTypes.length; i++)
        {
            SchemaTypeImpl sImpl = (SchemaTypeImpl)anonymousTypes[i];
            if (sImpl == null) // already handled in first pass
                continue;
            if (sImpl.isSkippedAnonymousType())
                continue;
            if (sdoTypeNames.containsKey(sImpl))
                continue;
            String localName;
            Name sdoName;
            char separator = TYPE_NAME_SEPARATOR_ELEMENT;

            SchemaField containerField = sImpl.getContainerField();
            if (containerField != null)
            {
                localName = containerField.getName().getLocalPart();
                if (containerField.isAttribute())
                    separator = TYPE_NAME_SEPARATOR_ATTRIBUTE;
            }
            else
            {
                // not defined inside an Elt or Attr: must be a nested simple type
                switch (sImpl.getOuterType().getSimpleVariety())
                {
                    case SchemaType.UNION:
                        localName = "Member" + ++anontypesinunion; break;
                    case SchemaType.LIST:
                        localName = "Item"; break;
                    case SchemaType.ATOMIC:
                    default:
                        throw new IllegalStateException("Unrecognized type \"" + sImpl.toString() + "\"");
                }
            }

            sdoName = pickInnerSDOName(localName, uri, outerName, separator, sImpl);

//            if (!usedTopLevelTypeNames.contains(sdoName.getUri().toLowerCase() + "#" + sdoName.getLocalName().toLowerCase())
//                    && sdoName.isOriginal())
//            {
//                // As per the spec, make the name global
//                usedTopLevelTypeNames.add(sdoName.getUri().toLowerCase() + "#" + sdoName.getLocalName().toLowerCase());
//                usedTypeNames.add(sdoName.getLocalName().toLowerCase());
//            }
//            else
//            {
//                // Make the name local
//                usedTypeNames.add(sdoName.getLocalName().toLowerCase());
//                sdoName.setLocalName(outerName + "." + sdoName.getLocalName());
//            }

            sdoTypeNames.put(sImpl, sdoName);
        }
    }

    private void assignSDOPropertyNames(Set<String> usedNames,
        SchemaProperty[] props, List<PropertyXML> declaredProperties,
        SchemaType baseType, TypeXML sdoType, TypeXML sdoBaseType,
        boolean doInherited)
    {
        // two passes: first deal with inherited properties, then with new ones.
        // this ensures that we match up with base class definitions cleanly
        // BUGBUG(radup) We have to look for particles that have been removed
        // in the derivation tree for this type using derivation by restriction,
        // because they have not been removed in Java and may collide with
        // this type's properties.

        boolean hasChangeSummary = false;
        for (SchemaProperty sProp : props)
        {
            SchemaProperty baseProp =
               (sProp.isAttribute() ?
                    baseType.getAttributeProperty(sProp.getName()) :
                    baseType.getElementProperty(sProp.getName()));

            if ((baseProp != null) != doInherited)
                continue;

            QName propQName = sProp.getName();

            String theName;

            if (baseProp == null)
                theName = pickSDOPropertyName(usedNames, propQName.getLocalPart(),
                    getSDOConfiguredName(sProp), sProp.getContainerType(), sProp.isAttribute());
            else
                theName = getPropertyXML(sdoBaseType,
                    baseProp.getName().getNamespaceURI(),
                    baseProp.getName().getLocalPart()).getName();

            boolean isArray = (sProp.getMaxOccurs() == null ||
                sProp.getMaxOccurs().compareTo(BigInteger.ONE) > 0);
            boolean isSingleton = !isArray && (sProp.getMaxOccurs().signum() > 0);
            boolean isOption = isSingleton && (sProp.getMinOccurs().signum() == 0);
            SchemaType basedOnType = sProp.getType();
            boolean isContainment = basedOnType.getContentType() != SchemaType.NOT_COMPLEX_TYPE;
            int declaredMany = getSDOConfiguredMany(sProp);
            if (declaredMany == MANY_FALSE)
                isArray = false;
            else if (declaredMany == MANY_TRUE)
                isArray = true;

            if (baseProp != null)
            {
                if (baseProp.extendsJavaArray())
                {
                    isSingleton = false;
                    isOption = false;
                    isArray = true;
                }
                else if (isArray)
                {
                    // According to the spec, we have to go back and change
                    // the base type's properties to make them multi-valued
                    String uri = baseProp.getName().getNamespaceURI();
                    String lcn = baseProp.getName().getLocalPart();
                    TypeXML base = sdoBaseType;

                    while (!Names.URI_SDO.equals(base.getURI()))
                    {
                        boolean done = changeManyToTrue(uri, lcn, !baseProp.isAttribute(), base,
                            !typeMapping.containsKey(baseType));
                        if (!done)
                        {
                            addError(sProp.getContainerType(), "binding.cant.change.arity",
                                lcn, uri, base.getName(), base.getURI());
                        }
                        List baseTypes = base.getBaseTypes();
                        if (baseTypes.size() > 0)
                            base = (TypeXML) baseTypes.get(0);
                        else
                            break;
                    }
                }
                if (baseProp.extendsJavaSingleton())
                {
                    isSingleton = true;
                }
                if (baseProp.extendsJavaOption())
                {
                    isOption = true;
                }
            }
//            ((SchemaPropertyImpl)sProp).setExtendsJava(basedOnType.getRef(),
//                isSingleton, isOption, isArray);

            // Check the dataType annotation
            QName dataTypeName = getSDOConfiguredDataType(sProp);
            TypeXML sdoBasedOnType;
            if (dataTypeName != null)
            {
                if (!basedOnType.isSimpleType())
                {
                    addError(sProp.getContainerType(), "binding.dataType.notsimple",
                        sProp.getName(), basedOnType);
                }

                TypeXML dataType = result.getTypeXML(dataTypeName.getNamespaceURI(), dataTypeName.getLocalPart());
                if (dataType == null)
                    dataType = linkTo.loadTypeByTypeName(dataTypeName.getNamespaceURI(), dataTypeName.getLocalPart());
                if (dataType == null)
                    throw new SDOBindingException(SDOError.messageForCode("binding.type.notfound",
                        dataTypeName));
                sdoBasedOnType = dataType;
            }
            else
            {
                // Get the corresponding SDO type
                sdoBasedOnType = typeMapping.get(basedOnType);
                if (sdoBasedOnType == null)
                {
                    // Try the linker
                    if (basedOnType.getName() == null)
                    {
                        // If this SchemaProperty has a type that is anonymous and that anonymous
                        // type is not in the current Schema, it means that the SchemaProperty is
                        // a reference to a global element/attribute whose type is anonymous
                        PropertyXML globalProp = null;
                        if (sProp.isAttribute())
                            globalProp = linkTo.loadGlobalPropertyByTopLevelAttrQName(
                                sProp.getName().getNamespaceURI(), sProp.getName().getLocalPart());
                        else
                            globalProp = linkTo.loadGlobalPropertyByTopLevelElemQName(
                                sProp.getName().getNamespaceURI(), sProp.getName().getLocalPart());

                        if (globalProp == null)
                            throw new SDOBindingException(SDOError.messageForCode("binding.elemattr.notfound",
                                sProp.isAttribute() ? 1 : 0, sProp.getName()));

                        sdoBasedOnType = globalProp.getTypeXML();
                    }
                    else
                    {
                        sdoBasedOnType = linkTo.loadTypeBySchemaTypeName(
                            basedOnType.getName().getNamespaceURI(),
                            basedOnType.getName().getLocalPart());

                        if (sdoBasedOnType == null)
                            throw new SDOBindingException(SDOError.messageForCode("binding.type.notfound",
                                basedOnType.getName()));
                    }
                }
            }

            String oppositePropertyName = null;
            int basedOnTypeCode = Common.getBuiltinTypeCode(basedOnType);
            // Check the propertyType annotation
            QName propertyTypeName = getSDOConfiguredPropertyType(sProp);
            if (propertyTypeName != null)
            {
                if (basedOnTypeCode != SchemaType.BTC_IDREF &&
                    basedOnTypeCode != SchemaType.BTC_IDREFS &&
                    basedOnTypeCode != SchemaType.BTC_ANY_URI)
                    addError(sProp.getContainerType(), "binding.propertyType.notreference",
                        sProp.getName(), sdoType.getName(), basedOnType);

                String uri = propertyTypeName.getNamespaceURI();
                String name = propertyTypeName.getLocalPart();
                TypeXML targetSdoType = result.getTypeXML(uri, name);
                isContainment = false;
                if (basedOnTypeCode == SchemaType.BTC_IDREFS)
                    isArray = true;
                if (targetSdoType == null)
                    targetSdoType = linkTo.loadTypeByTypeName(uri, name);
                if (targetSdoType == null)
                    throw new SDOBindingException(SDOError.messageForCode("binding.type.notfound",
                        name + '@' + uri));
                else
                    sdoBasedOnType = targetSdoType;

                oppositePropertyName = getSDOConfiguredOppositeProperty(sProp);
            }

            // Get the read-only config
            boolean readOnly = getSDOConfiguredReadOnly(sProp);

            // Check if a ChangeSummary property
            if (sdoBasedOnType == BuiltInTypeSystem.CHANGESUMMARYTYPE)
            {
                if (hasChangeSummary)
                    addError(sProp.getContainerType(), "binding.propertyChangeSummary.duplicate",
                    theName, sdoType.getName());
                hasChangeSummary = true;
                readOnly = true;
                if (isArray)
                    addError(sProp.getContainerType(), "binding.propertyChangeSummary.manyvalued",
                    theName, sdoType.getName());
            }

            // Get the alias names
            List<String> aliasNames = getSDOConfiguredAliasNames(sProp);
            if (aliasNames == null)
                aliasNames = Common.EMPTY_STRING_LIST;

            // Get the substitutions
            QName[] substitutions = sProp.acceptedNames();
            PropertyXML[] acceptedSubstitutions;
            if (substitutions.length > 1)
                acceptedSubstitutions = new PropertyXML[substitutions.length];
            else
                acceptedSubstitutions = null;

            // Process the nillable
            boolean nillable = sProp.hasNillable() != SchemaProperty.NEVER;
            if (nillable)
            {
                // According to the spec, if the type of the property maps to one of the
                // primitive Java types we have to change it to a Java wrapper type
                if (sdoBasedOnType.isPrimitive())
                {
                    switch (sdoBasedOnType.getTypeCode())
                    {
                    case BuiltInTypeSystem.TYPECODE_BOOLEAN:
                        sdoBasedOnType = BuiltInTypeSystem.BOOLEANOBJECT;
                        break;
                    case BuiltInTypeSystem.TYPECODE_BYTE:
                        sdoBasedOnType = BuiltInTypeSystem.BYTEOBJECT;
                        break;
                    case BuiltInTypeSystem.TYPECODE_SHORT:
                        sdoBasedOnType = BuiltInTypeSystem.SHORTOBJECT;
                        break;
                    case BuiltInTypeSystem.TYPECODE_INT:
                        sdoBasedOnType = BuiltInTypeSystem.INTOBJECT;
                        break;
                    case BuiltInTypeSystem.TYPECODE_LONG:
                        sdoBasedOnType = BuiltInTypeSystem.LONGOBJECT;
                        break;
                    case BuiltInTypeSystem.TYPECODE_FLOAT:
                        sdoBasedOnType = BuiltInTypeSystem.FLOATOBJECT;
                        break;
                    case BuiltInTypeSystem.TYPECODE_DOUBLE:
                        sdoBasedOnType = BuiltInTypeSystem.DOUBLEOBJECT;
                        break;
                    case BuiltInTypeSystem.TYPECODE_CHARACTER:
                        sdoBasedOnType = BuiltInTypeSystem.CHARACTEROBJECT;
                        break;
                    }
                }
            }

            // Set up the default value
            Object defaultValue = getDefaultValue(sProp, sdoBasedOnType, basedOnTypeCode);

            PropertyImpl sdoProp = PropertyImpl.create();
            sdoProp.initMutable(sdoBasedOnType, theName, isArray,
                isContainment, sdoType, defaultValue, readOnly, nillable, null,
                aliasNames, false, sProp.getName().getLocalPart(), sProp.getName().getNamespaceURI(),
                basedOnTypeCode, !sProp.isAttribute(), false);
            sdoProp.addPropertyValue(BuiltInTypeSystem.P_PROPERTY_XMLELEMENT, !sProp.isAttribute());
            sdoProp.initSetAcceptedSubstitutions(acceptedSubstitutions);
            sdoProp.makeImmutable();

            if (oppositePropertyName != null)
            {
                // Put the sdoType, sdoProp, sdoBasedOnType and oppositePropertyName
                // in a special list so that we can check them against one
                // another after all the types are analyzed
                oppositePropertiesDecls.add(new OppositePropertyInfo(sdoType, sdoProp,
                    sdoBasedOnType, oppositePropertyName));
            }

            if (acceptedSubstitutions != null)
            {
                acceptedSubstitutions[0] = sdoProp;
                // Put the QNames of the substitutions in a separate list so that they can be
                // resolved to actual SDO properties after all SDO properties are created
                substitutionProperties.add(new SubstitutionsInfo(substitutions,
                    acceptedSubstitutions));
            }

            // It looks like we only need to add properties corresponding to the
            // derived type, not inherited ones
            if (baseProp == null)
                declaredProperties.add(sdoProp);
        }
    }

    private PropertyXML createSDOPropertyFromSchema(SchemaProperty sProp, SchemaType t)
    {
        // The code in this method is similar to the one in
        // assignSDOPropertyNames because it needs to take
        // SDO annotations into account, but it is different,
        // because some things are computed differently
        SchemaType innerType = sProp.getType();
        String theName;
        TypeXML sdoType;
        int basedOnTypeCode;
        Object defaultValue;
        boolean readOnly;
        List<String> aliasNames;
        boolean many;
        boolean containment;

        theName = pickSDOPropertyName(new HashSet<String>(),
            sProp.getName().getLocalPart(), getSDOConfiguredName(sProp), t, sProp.isAttribute());
        QName dataTypeName = getSDOConfiguredDataType(sProp);
        if (dataTypeName != null)
        {
            if (!innerType.isSimpleType())
            {
                addError(t, "binding.dataType.global.notsimple",
                    sProp.getName(), innerType);
            }
            TypeXML dataType = result.getTypeXML(dataTypeName.getNamespaceURI(), dataTypeName.getLocalPart());
            if (dataType == null)
                dataType = linkTo.loadTypeByTypeName(dataTypeName.getNamespaceURI(), dataTypeName.getLocalPart());
            if (dataType == null)
                throw new SDOBindingException(SDOError.messageForCode("binding.type.notfound",
                    dataTypeName));
            sdoType = dataType;
        }
        else
        {
            sdoType = typeMapping.get(innerType);
            if (sdoType == null)
            {
                // If the type of the global element/attribute is anonymous, then it doesn't
                // make sense to search for it in the loader and it is an error
                if (innerType.getName() != null)
                    sdoType = linkTo.loadTypeBySchemaTypeName(innerType.getName().
                        getNamespaceURI(), innerType.getName().getLocalPart());
                if (sdoType == null)
                    throw new SDOBindingException(SDOError.messageForCode(
                        "binding.type.notfound", innerType));
            }
        }
        basedOnTypeCode = Common.getBuiltinTypeCode(innerType);
        readOnly = getSDOConfiguredReadOnly(sProp);
        aliasNames = getSDOConfiguredAliasNames(sProp);
        if (aliasNames == null)
            aliasNames = Common.EMPTY_STRING_LIST;
        //many = getSDOConfiguredMany(sProp) == MANY_TRUE; //CR349108
        many = !sProp.isAttribute() && getSDOConfiguredMany(sProp) != MANY_FALSE;

        containment = innerType.getContentType() != SchemaType.NOT_COMPLEX_TYPE;
        QName propertyTypeName = getSDOConfiguredPropertyType(sProp);
        if (propertyTypeName != null)
        {
            if (basedOnTypeCode != SchemaType.BTC_IDREF &&
                    basedOnTypeCode != SchemaType.BTC_IDREFS &&
                    basedOnTypeCode != SchemaType.BTC_ANY_URI)
                addError(t, "binding.propertyType.global.notreference",
                    sProp.getName(), innerType);

            String uri = propertyTypeName.getNamespaceURI();
            String name = propertyTypeName.getLocalPart();
            TypeXML targetSdoType = result.getTypeXML(uri, name);
            if (targetSdoType == null)
                targetSdoType = linkTo.loadTypeByTypeName(uri, name);
            if (targetSdoType == null)
                throw new SDOBindingException(SDOError.messageForCode("binding.type.notfound",
                    name + '@' + uri));
            else
                sdoType = targetSdoType;
            // TODO(radup) At this point, we should really check that the target
            // type is a Data Object type, but this check is not in the spec??
        }
        // Check if a ChangeSummary property
        if (sdoType == BuiltInTypeSystem.CHANGESUMMARYTYPE)
        {
            readOnly = true;
            if (many)
                addError(t, "binding.propertyChangeSummary.global.manyvalued", theName);
        }

        // Process the nillable
        boolean nillable = sProp.hasNillable() != SchemaProperty.NEVER;
        if (nillable)
        {
            // According to the spec, if the type of the property maps to one of the
            // primitive Java types we have to change it to a Java wrapper type
            if (sdoType.isPrimitive())
            {
                switch (sdoType.getTypeCode())
                {
                case BuiltInTypeSystem.TYPECODE_BOOLEAN:
                    sdoType = BuiltInTypeSystem.BOOLEANOBJECT;
                    break;
                case BuiltInTypeSystem.TYPECODE_BYTE:
                    sdoType = BuiltInTypeSystem.BYTEOBJECT;
                    break;
                case BuiltInTypeSystem.TYPECODE_SHORT:
                    sdoType = BuiltInTypeSystem.SHORTOBJECT;
                    break;
                case BuiltInTypeSystem.TYPECODE_INT:
                    sdoType = BuiltInTypeSystem.INTOBJECT;
                    break;
                case BuiltInTypeSystem.TYPECODE_LONG:
                    sdoType = BuiltInTypeSystem.LONGOBJECT;
                    break;
                case BuiltInTypeSystem.TYPECODE_FLOAT:
                    sdoType = BuiltInTypeSystem.FLOATOBJECT;
                    break;
                case BuiltInTypeSystem.TYPECODE_DOUBLE:
                    sdoType = BuiltInTypeSystem.DOUBLEOBJECT;
                    break;
                case BuiltInTypeSystem.TYPECODE_CHARACTER:
                    sdoType = BuiltInTypeSystem.CHARACTEROBJECT;
                    break;
                }
            }
        }

        TypeImpl containerType = TypeImpl.create();
        // We don't support opposite property on global properties
        // because that would mean that the target type needs to have
        // a property of type the containing type of the global property
        // which is undefined
        defaultValue = getDefaultValue(sProp, sdoType, basedOnTypeCode);
        PropertyImpl result = PropertyImpl.create();
        result.initMutable(sdoType, theName, many,
            containment, containerType, defaultValue, readOnly, nillable, null,
            aliasNames, true, sProp.getName().getLocalPart(), sProp.getName().getNamespaceURI(),
            basedOnTypeCode, !sProp.isAttribute(), false);
        result.addPropertyValue(BuiltInTypeSystem.P_PROPERTY_XMLELEMENT, !sProp.isAttribute());
        result.makeImmutable();
        containerType.init(Names.NAME_OF_CONTAINING_TYPE_FOR_GLOBAL_PROPERTIES,
            sProp.getName().getNamespaceURI(), SchemaType.BTC_NOT_BUILTIN,
            null, false, false, false, true, false, false, Common.EMPTY_TYPEXML_LIST,
            Collections.singletonList((PropertyXML) result), Common.EMPTY_STRING_LIST, t, this.result);
        containerType.makeImmutable();
        return result;
    }

    private Object getDefaultValue(SchemaProperty sProp, TypeXML sdoType, int schemaTypeCode)
    {
        if (!sdoType.isDataType())
            return null;
        Object result;
        if (schemaTypeCode == SchemaType.BTC_QNAME)
        {
            // The type is a QName so we need to get the XmlAnyType value and convert it to a String
            XmlAnySimpleType defaultTyped = sProp.getDefaultValue();
            if (defaultTyped == null)
                result = null;
            else
            {
                QName qname = ((XmlQName) defaultTyped).getQNameValue();
                result = qname.getNamespaceURI() + Names.FRAGMENT + qname.getLocalPart();
            }
        }
        else
        {
            // Otherwise, we are safe to call parseBufferToType
            // Moreover, we are safe to not pass in a NamespaceStack which we don't have anyway
            String defaultAsText = sProp.getDefaultText();
            if (defaultAsText == null)
                result = null;
            else
            {
                try
                {
                    result = SimpleValueHelper.parseBufferToType(defaultAsText, sdoType,
                        schemaTypeCode, null);
                }
                catch (SimpleValueHelper.SimpleValueException e)
                {
                    result = null;
                    switch (e.cause())
                    {
                        case SimpleValueHelper.UNMARSHAL_SIMPLE_NOCONSTRUCTOR:
                            addError(sProp.getContainerType(), SDOError.messageForCode(
                                "unmarshal.simple.noconstructor", e.getParam()));
                            break;
                        case SimpleValueHelper.UNMARSHAL_SIMPLE_CONSTRUCTOREXCEPTION:
                            addError(sProp.getContainerType(), SDOError.messageForCode(
                                "unmarshal.simple.constructorexception", e.getParam(),
                                e.getCause().getMessage()));
                            break;
                        case SimpleValueHelper.UNMARSHAL_SIMPLE_UNKOWNTYPE:
                            addError(sProp.getContainerType(), SDOError.messageForCode(
                                "unmarshal.simple.unknowntype", sdoType, defaultAsText,
                                sProp.getName().getLocalPart(), sProp.getName().getNamespaceURI()));
                            break;
                        case SimpleValueHelper.UNMARSHAL_SIMPLE_CONVERSIONFAILED:
                            // This should not be possible because otherwise the Schema would
                            // be invalid
                        default:
                            throw new IllegalStateException();
                    }
                }
            }
        }
        return result;
    }

    private void resolveOppositeProperties()
    {
        for (OppositePropertyInfo decl : oppositePropertiesDecls)
        {
            // We have two major cases:
            // - both ends already exist (although one may need setting the opposite
            // - one of the ends needs to be created
            // We also need to check:
            // - that both ends have the same value for readOnly
            // - that the types and property names match
            // - if one of the ends has containment, then the other end must have isMany = false

            List<PropertyXML> declaredProperties = decl.getTargetType().getDeclaredPropertiesXML();
            PropertyXML otherProperty = null;
            for (PropertyXML p : declaredProperties)
                if (p.getName().equals(decl.getTargetPropertyName()))
                {
                    otherProperty = p;
                    break;
                }

            if (otherProperty != null)
            {
                if (decl.getType().getSDOTypeSystem() != decl.getTargetType().getSDOTypeSystem())
                {
                    ((PropertyImpl) decl.getProperty()).setOppositeXML(otherProperty);
                    continue; // no point in checking, because the other type is pre-compiled
                }

                if (otherProperty.getTypeXML() != decl.getType())
                    addError(decl.getType().getXMLSchemaType(),
                        "binding.opposite.typenotgood", decl.getProperty().getName(),
                        decl.getType().getName(), otherProperty, decl.getTargetType().getName(),
                            otherProperty.getTypeXML().getName());

                if (decl.getProperty().isReadOnly() != otherProperty.isReadOnly())
                    addError(decl.getType().getXMLSchemaType(), "binding.opposite.readOnly",
                        decl.getProperty().getName(), decl.getType(),
                        otherProperty.getName(), decl.getTargetType().getName());

                if (otherProperty.getOppositeXML() != null &&
                    otherProperty.getOppositeXML() != decl.getProperty())
                    addError(decl.getType().getXMLSchemaType(), "binding.opposite.anotherProperty",
                        decl.getProperty().getName(), decl.getType().getName(),
                        otherProperty.getName(), otherProperty.getType().getName(),
                        otherProperty.getOppositeXML().getName(),
                        otherProperty.getOppositeXML().getContainingTypeXML().getName());

                if (otherProperty.isContainment() && decl.getProperty().isMany())
                    addError(decl.getType().getXMLSchemaType(), "binding.opposite.containment",
                        decl.getProperty().getName(), decl.getType().getName(),
                        otherProperty.getName(), otherProperty.getType().getName());

                // Sets each others opposite
                ((PropertyImpl) decl.getProperty()).setOppositeXML(otherProperty);
                if (otherProperty.getOppositeXML() == null)
                    ((PropertyImpl) otherProperty).setOppositeXML(decl.getProperty());
            }
            else
            {
                if (decl.getType().getSDOTypeSystem() != decl.getTargetType().getSDOTypeSystem())
                    addError(decl.getType().getXMLSchemaType(), "binding.opposite.precompiled",
                        decl.getProperty().getName(), decl.getType().getName(),
                        decl.getTargetType().getName(), decl.getTargetPropertyName());
                TypeXML targetType = decl.getTargetType();
                // We need to create a "special" property and add it to the
                // target type. This property is special in that it does not
                // have any kind of serialization
                otherProperty = PropertyImpl.create(decl.getType(), decl.getTargetPropertyName(),
                    false, false, targetType, Common.EMPTY_STRING_LIST,
                    decl.getProperty().isReadOnly(), decl.getProperty().isReadOnly()/*todo nullable*/, decl.getProperty(), null, null, null,
                    0, false, false, false);
                // Sets it as its opposite
                ((PropertyImpl) decl.getProperty()).setOppositeXML(otherProperty);
                // Now force it into the target type
                declaredProperties.add(otherProperty);
                ((TypeImpl) targetType).addResolveInfo(targetType.getTypeCode(),
                    targetType.getInstanceClass(), targetType.isDataType(),
                    targetType.isOpen(), targetType.isSequenced(), targetType.isAbstract(),
                    targetType.isSimpleContent(), targetType.isMixedContent(),
                    targetType.getBaseTypes(), declaredProperties, targetType.getAliasNames(),
                    targetType.getListItemType(), targetType.hasCustomizedInstanceClass());
            }
        }
    }

    private void resolveSubstitutions()
    {
        for (SubstitutionsInfo subst : substitutionProperties)
        {
            QName[] substitutionQNames = subst.getAcceptedNames();
            PropertyXML[] properties = subst.getAcceptedProperties();
            // At index 0 we always have the property itself
            for (int i = 1, j = 0; i < properties.length; i++, j++)
            {
                String uri = substitutionQNames[j].getNamespaceURI();
                String name = substitutionQNames[j].getLocalPart();
                if (name.equals(properties[0].getXMLName()) &&
                        uri.equals(properties[0].getXMLNamespaceURI()))
                {
                    i--;
                    continue;
                }
                properties[i] = result.getGlobalPropertyByTopLevelElemQName(uri, name);
                if (properties[i] == null)
                    properties[i] = linkTo.loadGlobalPropertyByTopLevelElemQName(uri, name);
                if (properties[i] == null)
                    addError(null, "binding.substitution.notFound",
                        substitutionQNames[j], properties[0]);
            }
        }
    }

    private boolean changeManyToTrue(String uri, String localName, boolean element, TypeXML type,
        boolean immutable)
    {
        List declaredProperties = type.getDeclaredProperties();
        for (ListIterator it = declaredProperties.listIterator(); it.hasNext();)
        {
            PropertyXML prop = (PropertyXML) it.next();
            if (prop.isXMLElement() == element && (((prop.getXMLNamespaceURI() == null && uri == null) ||
                    prop.getXMLNamespaceURI() != null && prop.getXMLNamespaceURI().equals(uri)) &&
                    prop.getXMLName().equals(localName)))
                if (!prop.isMany())
                    if (immutable)
                        return false;
                    else
                    {
                        PropertyXML prop2 = PropertyImpl.create(prop.getTypeXML(), prop.getName(),
                            true, prop.isContainment(), prop.getContainingTypeXML(),
                            prop.getDefault(), prop.isReadOnly(), prop.isNullable(), prop.getOppositeXML(),
                            prop.getAliasNames(), prop.getXMLName(), prop.getXMLNamespaceURI(),
                            prop.getSchemaTypeCode(), prop.isXMLElement(), prop.isDynamic(),
                            prop.isGlobal());
                        it.set(prop2);
                        // We need to refresh the cache of properties for that type now that we
                        // have replaced one of its properties
                        registeredProperties.remove(type);
                        // We also need to update the list of opposite properties with this prop
                        for (OppositePropertyInfo info : oppositePropertiesDecls)
                        {
                            if (prop == info.getProperty())
                            {
                                info.property = prop2;
                                break;
                            }
                        }
                    }
        }
        return true;
    }

    static int javaTypeCodeInCommon(SchemaType[] types)
    {
        if (types == null || types.length == 0)
            return SchemaProperty.XML_OBJECT;

        int code = javaTypeCodeForType(types[0]);
        if (code == SchemaProperty.JAVA_OBJECT)
            return code;
        for (int i = 1; i < types.length; i++)
        {
            // if any two are different, the answer is java.lang.Object
            if (code != javaTypeCodeForType(types[i]))
                return SchemaProperty.JAVA_OBJECT;
        }
        return code;
    }

    static int javaTypeCodeForType(SchemaType sType)
    {
        if (!sType.isSimpleType())
            return SchemaProperty.XML_OBJECT;

        if (sType.getSimpleVariety() == SchemaType.UNION)
        {
            // see if we can find an interesting common base type, e.g., for string enums
            SchemaType baseType = sType.getUnionCommonBaseType();
            if (baseType != null && !baseType.isURType())
                sType = baseType;
            else
                return javaTypeCodeInCommon(sType.getUnionConstituentTypes());
        }

        if (sType.getSimpleVariety() == SchemaType.LIST)
            return SchemaProperty.JAVA_LIST;

        if (sType.isURType())
            return SchemaProperty.XML_OBJECT;

        switch (sType.getPrimitiveType().getBuiltinTypeCode())
        {
            case SchemaType.BTC_ANY_SIMPLE:
                // return SchemaProperty.XML_OBJECT;
                return SchemaProperty.JAVA_STRING;

            case SchemaType.BTC_BOOLEAN:
                return SchemaProperty.JAVA_BOOLEAN;

            case SchemaType.BTC_BASE_64_BINARY:
                return SchemaProperty.JAVA_BYTE_ARRAY;

            case SchemaType.BTC_HEX_BINARY:
                return SchemaProperty.JAVA_BYTE_ARRAY;

            case SchemaType.BTC_ANY_URI:
                return SchemaProperty.JAVA_STRING;

            case SchemaType.BTC_QNAME:
                return SchemaProperty.JAVA_QNAME;

            case SchemaType.BTC_NOTATION:
                return SchemaProperty.XML_OBJECT;

            case SchemaType.BTC_FLOAT:
                return SchemaProperty.JAVA_FLOAT;

            case SchemaType.BTC_DOUBLE:
                return SchemaProperty.JAVA_DOUBLE;

            case SchemaType.BTC_DECIMAL:
                switch (sType.getDecimalSize())
                {
                    case SchemaType.SIZE_BYTE:
                        return SchemaProperty.JAVA_BYTE;
                    case SchemaType.SIZE_SHORT:
                        return SchemaProperty.JAVA_SHORT;
                    case SchemaType.SIZE_INT:
                        return SchemaProperty.JAVA_INT;
                    case SchemaType.SIZE_LONG:
                        return SchemaProperty.JAVA_LONG;
                    case SchemaType.SIZE_BIG_INTEGER:
                        return SchemaProperty.JAVA_BIG_INTEGER;
                    case SchemaType.SIZE_BIG_DECIMAL:
                    default:
                        return SchemaProperty.JAVA_BIG_DECIMAL;
                }

            case SchemaType.BTC_STRING:
                if (isStringType(sType.getBaseEnumType()))
                    return SchemaProperty.JAVA_ENUM;
                return SchemaProperty.JAVA_STRING;

            case SchemaType.BTC_DURATION:
                return SchemaProperty.JAVA_GDURATION;

            case SchemaType.BTC_DATE_TIME:
            case SchemaType.BTC_DATE:
                // return SchemaProperty.JAVA_DATE; // converted to calendar

            case SchemaType.BTC_TIME:
            case SchemaType.BTC_G_YEAR_MONTH:
            case SchemaType.BTC_G_YEAR:
            case SchemaType.BTC_G_MONTH_DAY:
            case SchemaType.BTC_G_DAY:
            case SchemaType.BTC_G_MONTH:
                // return SchemaProperty.JAVA_GDATE; // converted to calendar (JAX-B)
                return SchemaProperty.JAVA_CALENDAR;

            default:
                assert(false) : "unrecognized code " + sType.getPrimitiveType().getBuiltinTypeCode();
                throw new IllegalStateException("unrecognized code " + sType.getPrimitiveType().getBuiltinTypeCode() + " of " + sType.getPrimitiveType().getName());
        }
    }

    private static BigDecimal MAX_INT = new BigDecimal(Integer.MAX_VALUE);
    private static BigDecimal MIN_INT = new BigDecimal(Integer.MIN_VALUE);

    private boolean isEnumerationOfInt(SchemaType st)
    {
        if (st.getDecimalSize() != SchemaType.NOT_DECIMAL &&
                st.getDecimalSize() != SchemaType.SIZE_BIG_DECIMAL &&
                st.getEnumerationValues() != null)
        {
            XmlAnySimpleType[] values = st.getEnumerationValues();
            for (XmlAnySimpleType value : values)
            {
                BigDecimal valueAsBD = ((XmlDecimal) value).getBigDecimalValue();
                if (valueAsBD.compareTo(MIN_INT) < 0 ||
                        valueAsBD.compareTo(MAX_INT) > 0)
                    return false;
            }
            return true;
        }
        return false;
    }

    private boolean isLargerThanInteger(SchemaType st)
    {
        while (!st.isBuiltinType())
            st = st.getBaseType();
        int code = st.getBuiltinTypeCode();
        return code != SchemaType.BTC_BYTE && code != SchemaType.BTC_SHORT &&
            code != SchemaType.BTC_UNSIGNED_BYTE && code != SchemaType.BTC_UNSIGNED_SHORT &&
            code != SchemaType.BTC_INT;
    }

//    private static boolean hasSubstitutions(SchemaType t)
//    {
//        for (SchemaProperty p : t.getElementProperties())
//        {
//            if (p.acceptedNames().length > 1)
//                return true;
//        }
//        return false;
//    }
//
    private static boolean isPropertyModelOrderSensitive(SchemaParticle p, Set<QName> bag)
    {
        // the bag contains QNames of properties that we have previously seen
        switch (p.getParticleType())
        {
        case SchemaParticle.ALL:
            return true;
        case SchemaParticle.SEQUENCE:
            if ((p.getMaxOccurs() == null ||
                p.getMaxOccurs().compareTo(BigInteger.ONE) > 0) &&
                p.getParticleChildren().length > 1)
                return true;
            for (SchemaParticle child : p.getParticleChildren())
                if (isPropertyModelOrderSensitive(child, bag))
                    return true;
            break;
        case SchemaParticle.CHOICE:
            if ((p.getMaxOccurs() == null ||
                p.getMaxOccurs().compareTo(BigInteger.ONE) > 0) &&
                p.getParticleChildren().length > 1)
                return true;
            for (SchemaParticle child : p.getParticleChildren())
                if (isPropertyModelOrderSensitive(child, new HashSet<QName>(bag)))
                    return true;
            break;
        case SchemaParticle.ELEMENT:
            if (bag.contains(p.getName()))
                return true;
            else
            {
                bag.add(p.getName());
                return false;
            }
        case SchemaParticle.WILDCARD:
            return p.getMaxOccurs() == null || p.getMaxOccurs().compareTo(BigInteger.ZERO) > 0;
        }
        return false;
    }

    private void addAnonymousTypesFromRedefinition(SchemaType sType, List<SchemaType> result)
    {
        while (((SchemaTypeImpl)sType).isRedefinition() &&
                (sType.getDerivationType() == SchemaType.DT_EXTENSION ||
                        sType.isSimpleType()))
        {
            sType = sType.getBaseType();
            SchemaType[] newAnonTypes = sType.getAnonymousTypes();
            if (newAnonTypes.length > 0)
                result.addAll(Arrays.asList(newAnonTypes));
        }
    }

    private static boolean isResolved(TypeXML type)
    {
        // More or less a hack that is supposed to tell us quickly if an
        // SDO type was already resolved or not
        return type.getBaseTypes() != null;
    }

    private static XmlOptions getXmlOptionsFromObject(XmlObject[] inputSchemas, Object o)
    {
        if (o == null)
            return null;
        if (o instanceof XmlOptions)
            return (XmlOptions) o;
        Map m = null;
        if (o instanceof Map)
            m = (Map) o;
        if (o instanceof Options)
            m = ((Options) o).getMap();
        if (m != null)
        {
            XmlOptions result = (XmlOptions) m.get(Options.COMPILE_SCHEMA_OPTIONS);
            if (m.containsKey(Options.COMPILE_ENTITY_RESOLVER))
            {
                if (result == null)
                    result = new XmlOptions();
                result.setEntityResolver((EntityResolver) m.get(Options.COMPILE_ENTITY_RESOLVER));
            }
            if (m.containsKey(Options.COMPILE_ALLOW_MULTIPLE_DEFINITIONS))
            {
                if (result == null)
                    result = new XmlOptions();
                Set<String> mdefNamespaces = new HashSet<String>(inputSchemas.length);
                for (int i = 0; i < inputSchemas.length; i++)
                {
                    if (inputSchemas[i] instanceof SchemaDocument)
                    {
                        String targetNamespace = ((SchemaDocument) inputSchemas[i]).getSchema().
                            getTargetNamespace();
                        if (targetNamespace == null)
                            mdefNamespaces.add("##local");
                        else
                            mdefNamespaces.add(targetNamespace);
                    }
                }
                result.setCompileMdefNamespaces(mdefNamespaces);
            }
            return result;
        }
        return null;
    }

    // ==========================================================================
    // TypeXML helpers, because types are not finalized yet
    // ==========================================================================

    private List<PropertyXML> getPropertiesXML(TypeXML type)
    {
        List<PropertyXML> result = registeredProperties.get(type);
        if (result != null)
            return result;
        result = new ArrayList<PropertyXML>();
        result.addAll(type.getDeclaredPropertiesXML());

        for (Object o : type.getBaseTypes())
        {
            TypeXML baseType = (TypeXML) o;
            assert baseType!=null;
            List<PropertyXML> baseProps = getPropertiesXML(baseType);
            result.addAll(baseProps);
        }

        if (result.size()==0)
            result = Common.EMPTY_PROPERTYXML_LIST;
        registeredProperties.put(type, result);
        return result;
    }

    /**
     * Retrieves a PropertyXML by qualified name
     */
    public PropertyXML getPropertyXML(TypeXML type, String uri, String localName)
    {
        for (PropertyXML prop : getPropertiesXML(type))
            if (((prop.getXMLNamespaceURI() == null && uri == null) ||
                    prop.getXMLNamespaceURI() != null && prop.getXMLNamespaceURI().equals(uri)) &&
                    prop.getXMLName().equals(localName))
                return prop;
        return null;
    }

    // ==========================================================================
    // Error reporting
    // ==========================================================================
    /**
     * Reports an error on the given type with the given message
     */
    private void addError(SchemaType location, String errorCode, Object... args)
    {
        SchemaTypeImpl t = (SchemaTypeImpl) location;
        XmlError err;
        String errorMessage = SDOError.messageForCode(errorCode, args);
        if (t.getParseObject() == null)
            err = XmlError.forLocation(errorMessage, location.getSourceName(),
                -1, -1, -1);
        else
            err = XmlError.forObject(errorMessage, t.getParseObject());
        ctx.getLogger().error(err.toString());
    }

    // ==========================================================================
    // Naming
    // ==========================================================================

    private static String pickConstantName(Set usedNames, String words)
    {
        String base = NameUtil.upperCaseUnderbar(words);

        if (base.length() == 0)
        {
            base = "X";
        }

        if (base.startsWith("INT_")) // reserved for int codes
        {
            base = "X_" + base;
        }

        String uniqName;
        int index = 1;
        for (uniqName = base; usedNames.contains(uniqName); )
        {
            index++;
            uniqName = base + "_" + index;
        }

        usedNames.add(uniqName);

        return uniqName;
    }

    private Name pickFullSDOName(QName sdoName, SchemaType type, boolean isConfigured)
    {
        Name name = new Name();
        String sdoNameLowercase = sdoName.getNamespaceURI().toLowerCase() + '#' +
            sdoName.getLocalPart().toLowerCase();

        if (usedTopLevelTypeNames.contains(sdoNameLowercase))
        {
//            if (isConfigured)
//                if (!(type.isAnonymousType() &&
//                        (type.getOuterType().isDocumentType() ||
//                            type.getOuterType().isAttributeType())))
//                    addError(type, "binding.sdoName.type.conflict", sdoName.getLocalPart());
//                else
//                {
//                    // These are turned into inner-types and conflicts resolved later
//                    name.setLocalName(sdoName.getLocalPart());
//                    name.setUri(sdoName.getNamespaceURI());
//                    name.setOriginal(false);
//                }
//            else if (localTypeNames == Options.NAMES_STANDARD ||
//                localTypeNames == Options.NAMES_COMPOSITE)
                addError(type, "binding.sdoName.conflict", sdoName.getLocalPart());
//            else
//            {
//                int index = 1;
//                while (usedTopLevelTypeNames.contains(sdoNameLowercase + index))
//                    index++;
//                usedTopLevelTypeNames.add(sdoNameLowercase + index);
//                name.setLocalName(sdoName.getLocalPart() + index);
//                name.setUri(sdoName.getNamespaceURI());
//                name.setOriginal(false);
//            }
        }
        else
        {
            usedTopLevelTypeNames.add(sdoName.getNamespaceURI().toLowerCase() + '#' + sdoName.getLocalPart().toLowerCase());
            name.setLocalName(sdoName.getLocalPart());
            name.setUri(sdoName.getNamespaceURI());
            name.setOriginal(true);
        }

        return name;
    }

    private Name pickInnerSDOName(String localName, String uri, String parentName, char separator,
        SchemaTypeImpl sImpl/*for error reporting only*/)
    {
        boolean protect = false; // protectReservedInnerClassNames(candidate);
        boolean original = !protect;
        String uniqName;
        switch (localTypeNames)
        {
        case Options.NAMES_STANDARD:
            uniqName = localName;
            if (usedTopLevelTypeNames.contains(uri.toLowerCase() + '#' + localName.toLowerCase()))
            {
                // Error
                addError(sImpl, "binding.sdoName.type.conflict.inner", uniqName);
            }
            break;
//        case Options.NAMES_WITH_NUMBER_SUFFIX:
//            String candidate = localName;
//            int index = 1;
//            if (protect)
//                uniqName = candidate + index;
//            else
//                uniqName = candidate;
//            while (usedNames.contains(uniqName))
//            {
//                index++;
//                uniqName = candidate + index;
//                original = false;
//            }
//            break;
        case Options.NAMES_COMPOSITE:
            uniqName = parentName + separator + localName;
            break;
        default:
            throw new IllegalStateException("Unknown naming strategy: " + localTypeNames);
        }

        usedTopLevelTypeNames.add(uri.toLowerCase() + '#' + uniqName);
        Name result = new Name();
        result.setLocalName(uniqName);
        result.setUri(uri);
        result.setOriginal(original);
        return result;
    }

    /**
     * Returns the default SDO name for a Schema type
     */
    private QName getSDODefaultName(SchemaType t)
    {
        // The rule is that the URI is exactly the same as the Schema URI
        // but the name is javaized
        if (t.getName() != null)
        {
            QName qn = t.getName();
//            String localName = getInnermostName(NameUtil.getClassNameFromQName(qn));
            String localName = qn.getLocalPart();
            return new QName(qn.getNamespaceURI(), localName);
        }
        else if (t.getOuterType() != null &&
            t.getOuterType().isDocumentType())
        {
            QName qn = t.getOuterType().getDocumentElementName();
//            String localName = getInnermostName(NameUtil.getClassNameFromQName(qn));
            String localName = qn.getLocalPart();
            return new QName(qn.getNamespaceURI(), localName);
        }
        else if (t.getOuterType() != null &&
            t.getOuterType().isAttributeType())
        {
            QName qn = t.getOuterType().getAttributeTypeAttributeName();
//            String localName = getInnermostName(NameUtil.getClassNameFromQName(qn));
            String localName = qn.getLocalPart();
            return new QName(qn.getNamespaceURI(), localName);
        }
        else
        {
            throw new IllegalStateException();
        }
    }

    /** Extracts the URI in which a type is defined
     * The type must not be a document or attribute type
     */
    private static String getURIForType(SchemaType t)
    {
        if (t.getName() != null)
            return t.getName().getNamespaceURI();
        else
        {
            // We assume t is anonymous
            if (!t.isAnonymousType())
                throw new IllegalArgumentException();
            SchemaType outer = t.getOuterType();
            while (outer.getName() == null)
            {
                if (outer.isDocumentType())
                    return outer.getDocumentElementName().getNamespaceURI();
                else if (outer.isAttributeType())
                    return outer.getAttributeTypeAttributeName().getNamespaceURI();
                else
                    outer = outer.getOuterType();
            }
            return outer.getName().getNamespaceURI();
        }
    }

//    static boolean protectReservedGlobalClassNames(String name)
//    {
//        int i = name.lastIndexOf('.');
//        String lastSegment = name.substring(i + 1);
//        if (lastSegment.endsWith("Document") && !lastSegment.equals("Document"))
//            return true;
//        return false;
//    }
//
//    static boolean protectReservedInnerClassNames(String name)
//    {
//        return (name.equals("Enum") || name.equals("Factory"));
//    }
//
//    static String[] PROTECTED_PROPERTIES = {
//        "String",
//        "Boolean",
//        "Byte",
//        "Short",
//        "Int",
//        "Long",
//        "BigInteger",
//        "BigDecimal",
//        "Float",
//        "Double",
//        "Bytes",
//        "Date",
//        "ListValue",
//        "DataObject",
//        "Class",
//        "RootDataObject",
//        "Container",
//        "ContainmentProperty",
//        "DataGraph",
//        "Sequence",
//        "InstanceProperties"
//    };
//    static Set<String> PROTECTED_PROPERTIES_SET =
//        new HashSet<String>(Arrays.asList(PROTECTED_PROPERTIES));
//
//    static boolean protectReservedPropertyNames(String name)
//    {
//        return PROTECTED_PROPERTIES_SET.contains(name);
//    }
//
//    static String getOutermostPackage(String fqcn)
//    {
//        if (fqcn == null)
//            return "";
//
//        // remove class name
//        int lastdot = fqcn.indexOf('.');
//        if (lastdot < 0)
//            return "";
//
//        // remove outer package names
//        return fqcn.substring(0, lastdot);
//    }

    private static String getInnermostName(String qn)
    {
        int index = qn.lastIndexOf('.');
        if (index == -1)
            return qn;
        else
            return qn.substring(index + 1);
    }

    private String pickSDOPropertyName(Set<String> usedNames, String localName,
            String configuredName, SchemaType loc, boolean attr)
    {
//        String sdoName = configuredName == null ? NameUtil.lowerCamelCase(localName) : configuredName;
        String sdoName = configuredName == null ? localName : configuredName;
        boolean protect = false; //protectReservedPropertyNames(sdoName);
        String uniqName;
        int index = 1;
        if (protect)
            uniqName = sdoName + index;
        else
            uniqName = sdoName;
        if (attr && usedNames.contains(uniqName))
            uniqName = uniqName + "_attr";
        while (usedNames.contains(uniqName))
        {
            index++;
            uniqName = sdoName + index;
        }

        usedNames.add(uniqName);

        if (configuredName != null && !configuredName.equals(uniqName))
        {
            if (protect)
                addError(loc, "binding.sdoName.property.predefined", configuredName,
                    loc != null ? loc.getName().getLocalPart() : Common.EMPTY_STRING);
            else
                addError(loc, "binding.sdoName.property.conflict", configuredName,
                    loc != null ? loc.getName().getLocalPart() : Common.EMPTY_STRING);
        }
        return uniqName;
    }

    // ===========================================================================================
    // SDO configuration
    // ===========================================================================================

    /**
     * Returns the given annotation from an attribute annotation
     */
    public static String getSDOAnnotation(SchemaAnnotated t, QName qName)
    {
        SchemaAnnotation ann = t.getAnnotation();
        if (ann == null)
            return null;
        SchemaAnnotation.Attribute[] atts = ann.getAttributes();
        for (SchemaAnnotation.Attribute a : atts)
        {
            if (qName.equals(a.getName()))
                return a.getValue();
        }
        return null;
    }

    public static QName getSDOAnnotationAsQName(SchemaAnnotated t, QName qName)
    {
        SchemaAnnotation ann = t.getAnnotation();
        if (ann == null)
            return null;
        SchemaAnnotation.Attribute[] atts = ann.getAttributes();
        for (SchemaAnnotation.Attribute a : atts)
        {
            if (qName.equals(a.getName()))
            {
                String value = a.getValue();
                String uri = a.getValueUri();
                QName result;
                int index = value.indexOf(':');
                if (index > 0)
                {
                    result = new QName(uri, value.substring(index + 1), value.substring(index));
                }
                else
                    result = new QName(uri, value, null);
                return result;
            }
        }
        return null;
    }

    private static SchemaAnnotated getDeclaringComponent(SchemaProperty p)
    {
        SchemaAnnotated result;
        SchemaType t = p.getContainerType();
        if (p.isAttribute())
        {
            SchemaAttributeModel m = t.getAttributeModel();
            result = m.getAttribute(p.getName());
        }
        else
        {
            SchemaParticle content = t.getContentModel();
            result = getParticleByName(content, p.getName());
        }
        if (result == null)
            throw new IllegalStateException("Schema type \"" + t.toString() + "\" " +
                "declares a property named \"" + p.getName() + "\" as " +
                (p.isAttribute() ? "attribute" : "element") + ", " +
                "but no component name found");
        return result;
    }

    private static SchemaAnnotated getParticleByName(SchemaParticle parent, QName name)
    {
        switch (parent.getParticleType())
        {
        case SchemaParticle.ALL:
        case SchemaParticle.CHOICE:
        case SchemaParticle.SEQUENCE:
            for (int i = 0; i < parent.countOfParticleChild(); i++)
            {
                SchemaAnnotated result = getParticleByName(parent.getParticleChild(i), name);
                if (result != null)
                    return result;
            }
            break;
        case SchemaParticle.ELEMENT:
            if (name.equals(parent.getName()))
                return (SchemaLocalElement)parent;
        case SchemaParticle.WILDCARD:
        }
        return null;
    }

    /**
     * Returns the "configured" SDO name (via Schema annotations)
     */
    public static QName getSDOConfiguredName(SchemaType t)
    {
        if (t.isDocumentType())
        {
            throw new IllegalStateException();
/*            // A document type must have a content model consisting of a single elt
            if (t.getContentModel() == null || t.getContentModel().getParticleType() != SchemaParticle.ELEMENT)
                throw new IllegalStateException();
            SchemaType innerType = t.getContentModel().getType();
            if (givenName != null)
            {
                String uri = t.getDocumentElementName().getNamespaceURI();
                return new QName(uri, givenName);
            }
            else
                return null;
*/        }
        else if (t.isAttributeType())
        {
            throw new IllegalStateException();
/*            // An attribute type must have an attribute model consisting of a single attribute
            if (t.getAttributeModel() == null || t.getAttributeModel().getAttributes().length != 1)
                    throw new IllegalStateException();
            SchemaType innerType = t.getAttributeModel().getAttributes()[0].getType();
            String givenName = getSDOAnnotation(innerType, SDO_NAME);
            if (givenName != null)
            {
                String uri = t.getAttributeTypeAttributeName().getNamespaceURI();
                return new QName(uri, givenName);
            }
            else
                return null;
*/        }
        else
        {
            String givenName = getSDOAnnotation(t, SDO_NAME);
            if (givenName != null)
            {
                String uri = getURIForType(t);
                return new QName(uri, givenName);
            }
            else return null;
        }
    }

    /**
     * Returns the "configured" SDO name (via Schema annotations)
     */
    public static String getSDOConfiguredName(SchemaProperty p)
    {
        // We need to find the element or attribute declaring the property
        // and get the relevant annotations
        SchemaAnnotated d = getDeclaringComponent(p);
        return getSDOAnnotation(d, SDO_NAME);
    }

    /**
     * Returns the configured alias names
     */
    public static List<String> getSDOConfiguredAliasNames(SchemaType t)
    {
        // Compute the list of aliasNames
        String aliasNameAnn = getSDOAnnotation(t, SDO_ALIAS_NAME);
        if (aliasNameAnn != null)
        {
            List<String> result;
            StringTokenizer tk = new StringTokenizer(aliasNameAnn);
            result = new ArrayList<String>(3);
            while (tk.hasMoreTokens())
                result.add(tk.nextToken());
            return result;
        }
        else
            return null;
    }

    /**
     * Returns the configured alias names for a property
     */
    public static List<String> getSDOConfiguredAliasNames(SchemaProperty p)
    {
        SchemaAnnotated a = getDeclaringComponent(p);
        String aliasNameAnn = getSDOAnnotation(a, SDO_ALIAS_NAME);
        if (aliasNameAnn != null)
        {
            List<String> result;
            StringTokenizer tk = new StringTokenizer(aliasNameAnn);
            result = new ArrayList<String>(3);
            while (tk.hasMoreTokens())
                result.add(tk.nextToken());
            return result;
        }
        else
            return null;
    }

    /**
     * Returns the sequence configuration
     */
    public static int getSDOConfiguredSequence(SchemaType t)
    {
        String ann = getSDOAnnotation(t, SDO_SEQUENCE);
        if (ann == null)
            return 0;
        return booleanValue(ann) ? 1 : -1;
    }

    private static final String SCHEMA = "schema";

    public static String getSDOConfiguredPackage(SchemaType t)
    {
        XmlObject obj = ((SchemaTypeImpl) t).getParseObject();
        if (obj == null)
            return null;
        XmlCursor c = obj.newCursor();
        QName q = c.getName();
        while (!(SCHEMA.equals(q.getLocalPart()) && Names.URI_XSD.equals(q.getNamespaceURI())) &&
            c.toParent())
            q = c.getName();
        if (c.isStart())
        {
            // We are on the schema element: search for the package attribute
            if (c.toFirstAttribute())
                while (!SDO_PACKAGE.equals(c.getName()) && c.toNextAttribute());
            if (SDO_PACKAGE.equals(c.getName()))
            {
                String value = c.getTextValue();
                c.dispose();
                return value;
            }
        }
        c.dispose();
        return null;
    }

     /**
     * Returns the confgiured instance class
     */
    public static String getSDOConfiguredInstanceClass(SchemaType t)
    {
        return getSDOAnnotation(t, SDO_INSTANCE_CLASS);
    }

    /**
     * Returns the configured extended instance class
     */
    public static String getSDOConfiguredExtendedInstanceClass(SchemaType t)
    {
        return getSDOAnnotation(t, SDO_EXTENDED_INSTANCE_CLASS);
    }

    /**
     * Returns the configured property type
     */
    private QName getSDOConfiguredPropertyType(SchemaProperty p)
    {
        SchemaAnnotated elemOrAtt = getDeclaringComponent(p);
        QName ann = getSDOAnnotationAsQName(elemOrAtt, SDO_PROPERTY_TYPE);
        if (ann == null)
            return null;
        if (ann.getNamespaceURI() == null &&
                ann.getPrefix() != null)
        {
            addError(p.getContainerType(), "binding.propertyType.prefixnotfound",
                ann.getPrefix(), p.getName().getLocalPart());
            return null;
        }
        return ann;
    }

    /**
     * Returns the opposite property
     */
    public static String getSDOConfiguredOppositeProperty(SchemaProperty p)
    {
        SchemaAnnotated a = getDeclaringComponent(p);
        return getSDOAnnotation(a, SDO_OPPOSITE_PROPERTY);
    }

    /**
     * Returns the configured property data type
     */
    private QName getSDOConfiguredDataType(SchemaProperty p)
    {
        SchemaAnnotated elemOrAtt = getDeclaringComponent(p);
        QName dataType = getSDOAnnotationAsQName(elemOrAtt, SDO_DATA_TYPE);
        if (dataType != null && dataType.getNamespaceURI() == null &&
            dataType.getPrefix() != null)
        {
            addError(p.getContainerType(), "binding.dataType.prefixnotfound",
                dataType.getPrefix(), p.getName().getLocalPart());
            dataType = null;
        }

        String annst = getSDOAnnotation(elemOrAtt, SDO_STRING);

        if (annst == null)
            if (dataType == null)
                return null;
            else
                return dataType;
        else
            if (dataType == null)
                return booleanValue(annst) ? SDO_STRING_TYPE : null;
            else
            {
                boolean isString = booleanValue(annst);
                if (isString &&
                    !(SDO_STRING_TYPE.getNamespaceURI().equals(dataType.getNamespaceURI()) &&
                        SDO_STRING_TYPE.getLocalPart().equals(dataType.getLocalPart())))
                {
                    addError(p.getContainerType(), "binding.dataType.sdoString",
                        p.getName().getLocalPart());
                    return null;
                }
                else
                    return SDO_STRING_TYPE;
            }
    }

    /**
     * Returns the configured many
     */
    public static int getSDOConfiguredMany(SchemaProperty p)
    {
        // We don't support sdo:many on attributes
        if (p.isAttribute())
            return MANY_UNSPECIFIED;
        SchemaAnnotated a = getDeclaringComponent(p);
        String value = getSDOAnnotation(a, SDO_MANY);
        if (value == null)
            return MANY_UNSPECIFIED;
        if (value.equalsIgnoreCase("true") || value.equals("1"))
            return MANY_TRUE;
        if (value.equalsIgnoreCase("false") || value.equalsIgnoreCase("0"))
            return MANY_FALSE;
        return MANY_UNSPECIFIED;
    }

    /**
     * Returns the read-only configuration
     */
    public static boolean getSDOConfiguredReadOnly(SchemaProperty p)
    {
        SchemaAnnotated a = getDeclaringComponent(p);
        String val = getSDOAnnotation(a, SDO_READ_ONLY);
        if (val == null)
            return false;
        else
            return booleanValue(val);
    }

    private static boolean booleanValue(String s)
    {
        return s.equalsIgnoreCase("true") || s.equals("1");
    }
}
