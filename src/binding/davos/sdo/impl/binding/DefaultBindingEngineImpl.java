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

import davos.sdo.PropertyXML;
import davos.sdo.TypeXML;
import davos.sdo.binding.BindingContext;
import davos.sdo.binding.BindingEngine;
import davos.sdo.binding.BindingException;
import davos.sdo.binding.BindingSystem;
import davos.sdo.impl.type.BuiltInTypeSystem;
import davos.sdo.impl.type.TypeSystemBase;
import davos.sdo.type.TypeSystem;
import davos.sdo.util.Filer;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.impl.common.NameUtil;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map;

/**
 * @author Cezar Andrei (cezar dot andrei at gmail dot com)
 */
public class DefaultBindingEngineImpl
    implements BindingEngine
{
    private static List<CodePrinter.NamedArg> EMPTY_NAMED_ARG_LIST = Collections.EMPTY_LIST;
    private static Set<String> ILLEGAL_METHOD_NAMES;
    private static final String LIST = "java.util.List";
    private HashMap<SchemaType, TypeXML> schemaTypeToTypeXML;
    private TypeJavaMapping typeToJavaNames;

    static
    {
        //todo update the list
        String[] illegalMethodNames = new String[]{
            // java.lang.Object methods
            "clone",
            "equals",
            "finalize",
            "getClass",
            "hashCode",
            "notify",
            "notifyAll",
            "toString",
            "wait",
            // javax.sdo.DataObject methods
            "get",
            "set",
            "isSet",
            "unset",
            "getBoolean",
            "getByte",
            "getChar",
            "getDouble",
            "getFloat",
            "getInt",
            "getLong",
            "getShort",
            "getBytes",
            "getBigDecimal",
            "getBigInteger",
            "getDataObject",
            "getDate",
            "getString",
            "getList",
            "getSequence",
            "setBoolean",
            "setByte",
            "setChar",
            "setDouble",
            "setFloat",
            "setInt",
            "setLong",
            "setShort",
            "setBytes",
            "setBigDecimal",
            "setBigInteger",
            "setDataObject",
            "setDate",
            "setString",
            "setList",
            "createDataObject",
            "delete",
            "getContainer",
            "getContainmentProperty",
            "getDataGraph",
            "getType",
            "getInstanceProperties",
            "getProperty",
            "getRootObject",
            "getChangeSummary",
            "detach",
            // davos.sdo.DataObjectXML
            "getTypeXML",
            "getSequenceXML",
            "getXMLPrefix",
            "createDataObjectXML",
            "setXML",
            "getContainerXML",
            // davos.sdo.impl.data.DataObjectGeneral
            "init",
            // davos.sdo.impl.data.DataObjectImpl
            "getStore",
            // davos.sdo.impl.data.Store
            "storeGetProperty",
            "storeGet",
            "storeSet",
            "storeAddNew",
            "storeIsSet",
            "storeUnset",
            "storeSequenceSize",
            "storeSequenceGetPropertyXML",
            "storeSequenceGetValue",
            "storeSequenceGetXMLPrefix",
            "storeSequenceSet",
            "storeSequenceAddNew",
            "storeSequenceIsSet",
            "storeSequenceUnset",
            "storeGetSequenceXML",
            "storeGetInstanceProperties",
        };
        ILLEGAL_METHOD_NAMES = new HashSet<String>();
        ILLEGAL_METHOD_NAMES.addAll(Arrays.asList(illegalMethodNames));
    }

    // member vars
    protected BindingContext _ctx;

    /**
     * must use setContext() if create this way
     */
    public DefaultBindingEngineImpl() { }

    public DefaultBindingEngineImpl(Filer filer, String saveLoadName)
    {
        _ctx = new DefaultBindingContext(filer, saveLoadName);
    }

    public void setContext(BindingContext bindingCtx) {
        _ctx = bindingCtx;
    }

    public boolean bind(TypeSystem ts, BindingSystem bindingSystemOnClasspath, Map<TypeXML, String> packageNames, Map<TypeXML, String> instanceClasses)
        throws BindingException, IOException
    {
        typeToJavaNames = new TypeJavaMapping();

        boolean result = generateSources(ts, typeToJavaNames, bindingSystemOnClasspath,
            packageNames, instanceClasses);
        String id = ((TypeSystemBase)ts).computeId();
        result = result & saveTypeSystemBinaries(id, ts, typeToJavaNames.getInstanceClassMapping());
        result = result & saveBindingSystemBinaries(id, typeToJavaNames);

        return result;
    }

    public TypeJavaMapping getTypeToJavaNames()
    {
        return typeToJavaNames;
    }

    private boolean generateSources(TypeSystem ts,
        TypeJavaMapping typeToJavaNames, BindingSystem bindingSystemOnClasspath,
        Map<TypeXML, String> packageNames, Map<TypeXML, String> instanceClasses)
        throws BindingException
    {
        //add the mappings for BuiltIn types
        generateJavaNames(typeToJavaNames, BuiltInTypeSystem.INSTANCE.getAllTypes(), packageNames, instanceClasses);

        Set<TypeXML> allTypes = ts.getAllTypes();
        schemaTypeToTypeXML = new HashMap<SchemaType, TypeXML>(allTypes.size());
        // We build this reverse lookup table to keep track of inner types
        // Another possibility would be to just add a getOuterType method
        // on TypeXML
        for (TypeXML t : allTypes)
        {
            SchemaType st = t.getXMLSchemaType();
            if (st != null)
                schemaTypeToTypeXML. put(st, t);
        }

        // first resolve any name collisions
        // Actually, this is already done by the TypeSystem compiler
        generateJavaNames(typeToJavaNames, allTypes, packageNames, instanceClasses);

        addExternalNames(typeToJavaNames, allTypes, bindingSystemOnClasspath);

        // now generate the actual code
        for (TypeXML t : allTypes)
        {
            if (t.isBuiltinType())
                continue;

            if (t.getXMLSchemaType() != null &&
                t.getXMLSchemaType().isAnonymousType() &&
                ! (t.getXMLSchemaType().getOuterType().isDocumentType() ||
                t.getXMLSchemaType().getOuterType().isAttributeType()))
                // Skip this type
                continue;

            if (t.isDataType())
                // Nothing to generate
                continue;

            generateInterface(typeToJavaNames, t, null);
            generateImpl(typeToJavaNames, t, null);
        }

        return true;
    }

    private void addExternalNames(TypeJavaMapping typeToJavaNames, Set<TypeXML> allTypes,
        BindingSystem bindingSystemOnClasspath)
    {
        for (TypeXML type : allTypes)
        {
            for ( PropertyXML prop : type.getPropertiesXML())
            {
                TypeXML propType = prop.getTypeXML();
                if (typeToJavaNames.getJavaClass(propType)==null)
                {
                    assert propType.getSDOTypeSystem()!=type.getSDOTypeSystem();

                    String intfName = bindingSystemOnClasspath.getIntfFullNameForType(propType);
                    String implName = bindingSystemOnClasspath.getImplFullNameForType(propType);

                    typeToJavaNames.addMapping(propType, intfName, implName);
                }
            }
        }
    }

    // generate source code for interfaces and implementation
    protected void generateInterface(TypeJavaMapping typeToJavaNames, TypeXML t, JavaCodePrinter out)
        throws BindingException
    {
        JavaClassName j = typeToJavaNames.getJavaClass(t);
        PrintWriter pw = null;
        if (out == null)
        {
            //  New file needed
            Filer f = _ctx.getFiler();
            try
            {
                pw = new PrintWriter(
                    f.createJavaSourceFile(j.getIntfFullName())
                    /* use platform default encoding */ );
            }
            catch (IOException e)
            {
                throw new BindingException("Caught IOException " +
                    "attempting to create interface " + j.getIntfFullName(), e);
            }

            _ctx.getLogger().info("Generating interface " + j.getIntfFullName());

            out = new JavaCodePrinter(pw);

            out.emitMultiLineComment("SDO Type: " + t.getName() + "\n" +
                "     uri: " + t.getURI() + "\n\n" +
                "Automatically generated - do not modify.", false);
            out.emitPackage(j.getIntfPackage());
            out.emitNewLine();
        }
        else
        {
            // Generating in an existing file
            out.emitMultiLineComment("SDO Type: " + t.getName() + "\n" +
                "     uri: " + t.getURI() + "\n\n", false);
        }

        List<String> baseInterfaces = new ArrayList<String>();
        List<TypeXML> baseTypes = (List<TypeXML>)t.getBaseTypes();
        for (TypeXML baseType : baseTypes)
        {
            baseInterfaces.add(typeToJavaNames.getJavaClass(baseType).getIntfReferenceName());
        }

        out.startInterface(j.getIntfInnerName(), baseInterfaces);
        out.emitNewLine();

        // add methods
        Set<String> usedMethodNames = new HashSet<String>();
        List<CodePrinter.NamedArg> args = new ArrayList<CodePrinter.NamedArg>();

        for (PropertyXML p : t.getDeclaredPropertiesXML())
        {
            TypeXML propTypeXML = p.getTypeXML();
            JavaClassName jcName = typeToJavaNames.getJavaClass(propTypeXML);

            if (jcName==null)
                throw new BindingException("Type mapping not found for type: " + p.getTypeXML().getName() + " @ " + p.getTypeXML().getURI());

            String propTypeIntfFullName = jcName.getIntfReferenceName();
            String javaPropName = chooseJavaPropName(p.getName(), usedMethodNames, propTypeXML);

            if (p.isMany())
            {
                out.emitInterfaceMethod(LIST + " /*" + propTypeIntfFullName + "*/", "get" + javaPropName, EMPTY_NAMED_ARG_LIST);
            }
            else
            {
                if (propTypeIntfFullName.equals("boolean") ||
                    propTypeIntfFullName.equals("java.lang.Boolean"))
                {
                    out.emitInterfaceMethod(propTypeIntfFullName, "is" + javaPropName, EMPTY_NAMED_ARG_LIST);
                }
                else
                {
                    out.emitInterfaceMethod(propTypeIntfFullName, "get" + javaPropName, EMPTY_NAMED_ARG_LIST);
                }
            }
            out.emitNewLine();

            if (!p.isMany() && !p.isReadOnly())
            {
                args.clear();
                String argName = NameUtil.lowerCamelCase(p.getName());
                // We know that javaPropName is a valid Java identifier,
                // but we don't know if argName also is
                if (!NameUtil.isValidJavaIdentifier(argName))
                    argName = javaPropName;
                args.add(new CodePrinter.NamedArg(propTypeIntfFullName, argName));
                out.emitInterfaceMethod("void", "set" + javaPropName, args);
                out.emitNewLine();
            }

            if (_ctx.genIsSetMethods())
            {
                out.emitInterfaceMethod("boolean", "isSet" + javaPropName, EMPTY_NAMED_ARG_LIST);
                out.emitNewLine();
            }

            if (_ctx.genUnsetMethods() && !p.isReadOnly())
            {
                out.emitInterfaceMethod("void", "unset" + javaPropName, EMPTY_NAMED_ARG_LIST);
                out.emitNewLine();
            }

            if (_ctx.genCreateMethods() && !propTypeXML.isAbstract() && !propTypeXML.isDataType() && p.isContainment() && !p.isReadOnly())
            {
                out.emitInterfaceMethod(propTypeIntfFullName, "create" + javaPropName, EMPTY_NAMED_ARG_LIST);
                out.emitNewLine();
            }
        }

        SchemaType st = t.getXMLSchemaType();
        if (st!=null)
        {
            for (SchemaType a : st.getAnonymousTypes())
            {
                TypeXML sa = schemaTypeToTypeXML.get(a);
                if (sa == null)
                    throw new IllegalStateException();
                if (!sa.isDataType())
                    generateInterface(typeToJavaNames, sa, out);
            }
        }

        out.endInterface();

        if (pw != null)
        {
            pw.flush();
            pw.close();
        }
    }

    protected void generateImpl(TypeJavaMapping typeToJavaNames, TypeXML t, JavaCodePrinter out)
        throws BindingException
    {
        JavaClassName j = typeToJavaNames.getJavaClass(t);
        boolean staticclass = false;
        PrintWriter pw = null;
        if (out == null)
        {
            // Generate into a new file
            Filer f = _ctx.getFiler();
            try
            {
                pw = new PrintWriter(
                    f.createJavaSourceFile(j.getImplFullName())
                    /* use platform default encoding */ );
            }
            catch (IOException e)
            {
                throw new BindingException("Caught IOException " +
                    "attempting to create interface " + j.getImplFullName(), e);
            }

            out = new JavaCodePrinter(pw);

            out.emitMultiLineComment("SDO Type: " + t.getName() + "\n" +
                "     uri: " + t.getURI() + "\n" +
                "Java interface: " + j.getIntfFullName() + "\n\n" +
                "Automatically generated - do not modify.", false);
            out.emitPackage(j.getImplPackage());
            out.emitNewLine();
        }
        else
        {
            // Generate into an existing file
            out.emitMultiLineComment("SDO Type: " + t.getName() + "\n" +
                "     uri: " + t.getURI() + "\n" +
                "Java interface: " + j.getIntfFullName() + "\n\n", false);
            staticclass = true;
        }

        String baseClass = "davos.sdo.impl.data.DataObjectGeneral";
        List<TypeXML> baseTypes = (List<TypeXML>)t.getBaseTypes();
        if (baseTypes.size()>1)
            throw new IllegalArgumentException("This java SDO implementation doesn't support " +
                "static generation for types with multiple base types. Type: " + t + " baseTypes: " + baseTypes);
        else if (baseTypes.size()==1)
            baseClass = typeToJavaNames.getJavaClass(baseTypes.get(0)).getImplReferenceName();

        out.startClass(j.getImplInnerName(), t.isAbstract(), staticclass,
            baseClass, j.getIntfReferenceName());

        // fields
        out.emitString("public static String typeUri = \"" + t.getURI() + "\";");
        out.emitNewLine();
        out.emitString("public static String typeName = \"" + t.getName() + "\";");

        out.emitNewLine();
        out.emitNewLine();

        // add methods
        Set<String> usedMethodNames = new HashSet<String>();
        List<CodePrinter.NamedArg> args = new ArrayList<CodePrinter.NamedArg>();

        for (PropertyXML p : t.getDeclaredPropertiesXML())
        {
            TypeXML propTypeXML = p.getTypeXML();
            String javaPropName = chooseJavaPropName(p.getName(), usedMethodNames, propTypeXML);
            String propTypeIntfFullName = typeToJavaNames.getJavaClass(propTypeXML).getIntfReferenceName();

            if (!"ChangeSummary".equals(javaPropName))
            {
            if (p.isMany())
            {
                out.startMethod(LIST + " /*" + propTypeIntfFullName + "*/", "get" + javaPropName, EMPTY_NAMED_ARG_LIST);
            }
            else
            {
                if (propTypeIntfFullName.equals("boolean") ||
                    propTypeIntfFullName.equals("java.lang.Boolean"))
                {
                    out.startMethod(propTypeIntfFullName, "is" + javaPropName, EMPTY_NAMED_ARG_LIST);
                }
                else
                {
                    out.startMethod(propTypeIntfFullName, "get" + javaPropName, EMPTY_NAMED_ARG_LIST);
                }
            }
            generateGetterMethod(j.getImplFullName(), p, out, typeToJavaNames);
            out.endMethod();
            out.emitNewLine();
            }

            if (!p.isMany() && !p.isReadOnly())
            {
                args.clear();
                String argName = NameUtil.lowerCamelCase(p.getName());
                // We know that javaPropName is a valid Java identifier,
                // but we don't know if argName also is
                if (!NameUtil.isValidJavaIdentifier(argName))
                    argName = javaPropName;
                args.add(new CodePrinter.NamedArg(propTypeIntfFullName, argName));
                out.startMethod("void", "set" + javaPropName, args);
                generateSetterMethod(j.getImplFullName(), p, out, argName);
                out.endMethod();
                out.emitNewLine();
            }

            if (_ctx.genIsSetMethods())
            {
                out.startMethod("boolean", "isSet" + javaPropName, EMPTY_NAMED_ARG_LIST);
                generateIsSetMethod(j.getImplFullName(), p, out);
                out.endMethod();
                out.emitNewLine();
            }

            if (_ctx.genUnsetMethods() && !p.isReadOnly())
            {
                out.startMethod("void", "unset" + javaPropName, EMPTY_NAMED_ARG_LIST);
                generateUnsetMethod(j.getImplFullName(), p, out);
                out.endMethod();
                out.emitNewLine();
            }

            if (_ctx.genCreateMethods() && !propTypeXML.isAbstract() && !propTypeXML.isDataType() && p.isContainment() && !p.isReadOnly())
            {
                out.startMethod(propTypeIntfFullName, "create" + javaPropName, EMPTY_NAMED_ARG_LIST);
                generateCreateMethod(j.getImplFullName(), p, propTypeIntfFullName, out);
                out.endMethod();
                out.emitNewLine();
            }
        }

        SchemaType st = t.getXMLSchemaType();
        if (st!=null)
        {
            for (SchemaType a : st.getAnonymousTypes())
            {
                TypeXML sa = schemaTypeToTypeXML.get(a);
                if (sa == null)
                    throw new IllegalStateException();
                if (!sa.isDataType())
                    generateImpl(typeToJavaNames, sa, out);
            }
        }

        out.endClass();

        if (pw != null)
        {
            pw.flush();
            pw.close();
        }
    }

    private void generateGetterMethod(String intfFullName, PropertyXML p, JavaCodePrinter out, TypeJavaMapping typeMap)
    {
        TypeXML propTypeXML = p.getTypeXML();
        JavaClassName jcn = typeMap.getJavaClass(propTypeXML);

        String cast;
        String typeOfGet;
        if (p.isMany())
        {
            cast = "(" + LIST + " /*" + jcn.getIntfFullName() + "*/)";
            typeOfGet = "";
        }
        else if (propTypeXML.isPrimitive())
        {
            cast = "";
            typeOfGet = NameUtil.upperCaseFirstLetter(propTypeXML.getInstanceClass().getName());
        }
        else
        {
            cast = "(" + jcn.getIntfReferenceName() + ")";
            typeOfGet = "";
        }

        out.emitString("return " + cast + "get" + typeOfGet + "( super.getSDOContext().getTypeSystem().getTypeXML(typeUri, typeName).getProperty(\"" + p.getName() + "\"));");
        out.emitNewLine();
    }

    private void generateSetterMethod(String intfFullName, PropertyXML p, JavaCodePrinter out, String argName)
    {
        out.emitString("set( super.getSDOContext().getTypeSystem().getTypeXML(typeUri, typeName).getProperty(\"" + p.getName() + "\"), " + argName + ");");
        out.emitNewLine();
    }

    private void generateIsSetMethod(String intfFullName, PropertyXML p, JavaCodePrinter out)
    {
        out.emitString("return isSet( super.getSDOContext().getTypeSystem().getTypeXML(typeUri, typeName).getProperty(\"" + p.getName() + "\"));");
        out.emitNewLine();
    }

    private void generateUnsetMethod(String intfFullName, PropertyXML p, JavaCodePrinter out)
    {
        out.emitString("unset( super.getSDOContext().getTypeSystem().getTypeXML(typeUri, typeName).getProperty(\"" + p.getName() + "\"));");
        out.emitNewLine();
    }

    private void generateCreateMethod(String intfFullName, PropertyXML p, String propType, JavaCodePrinter out)
    {
        out.emitString("return (" + propType + ")createDataObject( super.getSDOContext().getTypeSystem().getTypeXML(typeUri, typeName).getProperty(\"" + p.getName() + "\") );");
        out.emitNewLine();
    }

    private String chooseJavaPropName(String sdoPropertyName, Set<String> usedMethodNames, TypeXML propType)
    {
//        String initialCandidate = NameUtil.upperCamelCase(sdoPropertyName);
        String initialCandidate = makeJavaIdentifier(sdoPropertyName, true);
        String candidate = initialCandidate;
        int i = 0;
        do
        {
            if (!ILLEGAL_METHOD_NAMES.contains(candidate) &&
                !ILLEGAL_METHOD_NAMES.contains("get" + candidate) &&
                !ILLEGAL_METHOD_NAMES.contains("set" + candidate) &&
                !usedMethodNames.contains(candidate))
            {
                usedMethodNames.add(candidate);
                return candidate;
            }

            if ("ChangeSummary".equals(candidate))
            {
                // If the name of the property is "changeSummary" and the type is
                // ChangeSummaryType, then we don't change the name and generate
                // a getChangeSummary() method in the interface, but we skip the
                // implementation for that method, relying on the implementation
                // from DataObjectImpl
                if (BuiltInTypeSystem.CHANGESUMMARYTYPE == propType)
                    return candidate;
            }
            i++;
            candidate = initialCandidate + i;

        } while(i<Integer.MAX_VALUE);

        throw new IllegalArgumentException("Too many similar names used.");
    }

    public static String makeJavaIdentifier(String s, boolean uppercaseFirstLetter)
    {
        assert s.length() > 0;
        if (uppercaseFirstLetter)
        {
        char c = s.charAt(0);
        char upper = Character.toUpperCase(c); 
        if (c == upper && NameUtil.isValidJavaIdentifier(s))
            return s;
        else
        {
            StringBuilder sb;
            // We need to build a valid Java identifier
            // For lack of a better guidance we use a very simple algorithm that
            // adds an 'X' at the beginning of the name if the name does not start
            // with a valid Java identifier start and then we replace each char which is not
            // a valid Java identifier part with an underscore
            if (!Character.isJavaIdentifierStart(upper))
            {
                sb = new StringBuilder(s.length() + 1);
                sb.append('X').append(s);
            }
            else
            {
                sb = new StringBuilder(s);
                sb.setCharAt(0, upper);
            }
            for (int i = 1; i < sb.length(); i++)
                if (!Character.isJavaIdentifierPart(sb.charAt(i)))
                    sb.setCharAt(i, '_');
            return sb.toString();
        }
        }
        else
        {
            if (NameUtil.isValidJavaIdentifier(s))
                return s;
            else
            {
                StringBuilder sb;
                if (!Character.isJavaIdentifierStart(s.charAt(0)))
                {
                    sb = new StringBuilder(s.length() + 1);
                    sb.append('x').append(s);
                }
                else
                    sb = new StringBuilder(s);
                for (int i = 1; i < sb.length(); i++)
                    if (!Character.isJavaIdentifierPart(sb.charAt(i)))
                        sb.setCharAt(i, '_');
                return sb.toString();
            }
        }
    }

    /**
     * Construct a TypeJavaMapping object where all the inputted types have been
     * mapped to the Java names
     * @param allTypes - a set of all the types for which to construct names
     * @param packageNames
     * @param instanceClasses
     * @return typeMap - the output
     * @throws BindingException
     */
    protected TypeJavaMapping generateJavaNames(TypeJavaMapping typeToJavaNames,
        Set<TypeXML> allTypes,
        Map<TypeXML, String> packageNames, Map<TypeXML, String> instanceClasses)
        throws BindingException
    {
        for (TypeXML type : allTypes)
        {
            generateJavaName(typeToJavaNames, type, packageNames, instanceClasses);
        }

        return typeToJavaNames;
    }

    private void generateJavaName(TypeJavaMapping typeToJavaNames, TypeXML type,
        Map<TypeXML, String> packageNames, Map<TypeXML, String> instanceClasses)
    {
        String intfFullName;
        String implFullName;

        if (typeToJavaNames.getJavaClass(type) != null)
            return; // Already mapped

        if (type.isBuiltinType())
        {
            Class cls = type.getInstanceClass();

            if (cls==null) //the only type with instance class == null in the spec page pdf 74
                cls = String.class;

            if (cls.isPrimitive() || cls==byte[].class)
            {
                typeToJavaNames.addMapping(type, cls);
                intfFullName = implFullName = cls.getSimpleName();
            }
            else
            {
                intfFullName = getClassSyntacticName(cls);
                if (cls.isInterface())
                {
                    //special cases
                    if (davos.sdo.BaseDataGraphType.class == cls)
                        implFullName = "davos.sdo.impl.data.BaseDataGraphTypeImpl";
                    else if (davos.sdo.ModelsType.class == cls)
                        implFullName = "davos.sdo.impl.data.ModelsTypeImpl";
                    else if (davos.sdo.XSDType.class == cls)
                        implFullName = "davos.sdo.impl.data.XSDTypeImpl";
                    else if (javax.sdo.DataObject.class == cls)
                        implFullName = "davos.sdo.impl.data.DataObjectGeneral";
                    else if (javax.sdo.ChangeSummary.class == cls)
                        implFullName = "davos.sdo.impl.data.DataObjectGeneral";
                    else if (java.util.List.class == cls)
                        implFullName = "NO_IMPLEMENTATION";
                    else
                        throw new IllegalStateException("Built-in interface " + cls.getName() + " without implementation mapping.");
                }
                else
                    implFullName = intfFullName;
                typeToJavaNames.addMapping(type, intfFullName, implFullName);
            }
        }
        else if (type.isDataType())
        {
            Class cls = type.getInstanceClass();
            String assignedName = instanceClasses.get(type);

            if (assignedName != null)
            {
                intfFullName = implFullName = assignedName;
                typeToJavaNames.addMapping(type, assignedName, assignedName);
            }
            else if (cls.isPrimitive())
            {
                assert cls != null;
                typeToJavaNames.addMapping(type, cls);
                intfFullName = implFullName = cls.getSimpleName();
            }
            else
            {
                assert cls != null;
                intfFullName = getClassSyntacticName(cls);
                implFullName = intfFullName;
                typeToJavaNames.addMapping(type, intfFullName, implFullName);
            }
        }
        else
        {
            // Usually, the interface and implementation names for Java classes are derived directly
            // from the SDO name, but here we cheat a little bit, because for anonymous types the
            // SDO names will not have enough information to decide whether the corresponding Java
            // class should be an inner class or not
            String intfPackageName = packageNames.get(type);
            if (intfPackageName == null)
                intfPackageName = pickPackageFromUri(type.getURI());
            String implPackageName = intfPackageName + ".impl";
            String intfShortNameCandidate = genShortNameFromName(type.getName());
            String implShortNameCandidate = intfShortNameCandidate + "Impl";

            SchemaType st = type.getXMLSchemaType();
            if (st.isAnonymousType() && !st.getOuterType().isDocumentType() &&
                    !st.getOuterType().isAttributeType())
            {
                TypeXML outerSdoType = schemaTypeToTypeXML.get(st.getOuterType());
                generateJavaName(typeToJavaNames, outerSdoType, packageNames, instanceClasses);
                JavaClassName jcn = typeToJavaNames.getJavaClass(outerSdoType);

                intfShortNameCandidate = typeToJavaNames.pickIntfInnerName(jcn, intfShortNameCandidate);
                implShortNameCandidate = typeToJavaNames.pickImplInnerName(jcn, implShortNameCandidate);

                JavaClassName innerJCN = new JavaInnerClassName(jcn, intfShortNameCandidate, implShortNameCandidate);
                typeToJavaNames.addMapping(type, innerJCN);

                intfFullName = innerJCN.getIntfFullName();
                implFullName = innerJCN.getImplFullName();
            }
            else
            {
                intfFullName = typeToJavaNames.pickInterfaceFullName(intfPackageName, intfShortNameCandidate);
                implFullName = typeToJavaNames.pickImplementationFullName(implPackageName, implShortNameCandidate);
                typeToJavaNames.addMapping(type, intfFullName, implFullName);
            }

            // I have the name of the class but the class cannot be loaded at this time, so the type cannot be updated
            // but at load time from the jar should be available
        }

        _ctx.getLogger().info("Binding " + type.getName() + "@" + type.getURI() + " -> " + intfFullName + ":" + implFullName);
    }

    private static String getClassSyntacticName(Class cls)
    {
        String res;
        boolean isArray = cls.isArray();

        if (isArray)
        {
            res = getClassSyntacticName(cls.getComponentType()) + "[]";
        }
        else
        {
            if (cls.isPrimitive())
            {
                res = cls.getSimpleName();
            }
            else
            {
                res = cls.getName();
            }
        }

        return res;
    }

    private String genShortNameFromName(String name)
    {
        // If "name" is a composite name, of the form "outerClass.innerClass" or "outerClass$innerClass"
        // the name we are interested in is just the last part
        int ind = name.lastIndexOf('$');
        if (ind > 0)
            name = name.substring(ind + 1);
        else
        {
            ind = name.lastIndexOf('.');
            if (ind > 0)
                name = name.substring(ind + 1);
        }
//        return NameUtil.upperCamelCase(name);
        return makeJavaIdentifier(name, true);
    }

    private String pickPackageFromUri(String uri)
    {
        return NameUtil.getPackageFromNamespace(uri, true);
    }

//    // When declaring an inner class, we cant' use "class A$B" but we have to use "class B" of course
//    private String getDeclaringName(String shortClassName)
//    {
//        int dollarIndex = shortClassName.lastIndexOf('$');
//        if (dollarIndex > 0)
//            return shortClassName.substring(dollarIndex + 1);
//        else
//            return shortClassName;
//    }
    // end generate sources

    // call type system persistance code
    private boolean saveTypeSystemBinaries(String sdoId, TypeSystem ts, Map<TypeXML, String> typeToInstanceClass)
        throws IOException
    {
        return ((TypeSystemBase)ts).saveTypeSystemWithName(_ctx.getFiler(), _ctx.getSaveLoadName(), sdoId, typeToInstanceClass);
    }

    // save the mapping between Types and Classes
    private boolean saveBindingSystemBinaries(String tsId, TypeJavaMapping typeToJavaNames)
        throws IOException
    {
        return DynamicBindingSystem.saveBindingSystemWithId(_ctx.getFiler(), tsId, typeToJavaNames);
    }
}
