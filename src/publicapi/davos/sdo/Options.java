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
package davos.sdo;

import java.util.Map;
import java.util.HashMap;

import org.apache.xmlbeans.XmlOptions;
import org.xml.sax.EntityResolver;

/**
 * This class encapsulates some implementation-dependent options controlling various behaviours of
 * SDO. See the Javadoc associated to each property
 * Date: Dec 14, 2006
 */
public class Options
{
    private Map<String, Object> map = new HashMap<String, Object>();

    public static final String SAVE_DONT_THROW_EXCEPTIONS = "marshal.dontThrowExceptions";
    public static final String SAVE_PRETTY_PRINT = "marshal.prettyPrint";
    public static final String SAVE_INDENT = "marshal.indent";
    public static final String LOAD_DONT_THROW_EXCEPTIONS = "unmarshal.dontThrowExceptions";
    public static final String VALIDATE = "validate";
    public static final String COMPILE_SCHEMA_OPTIONS = "compile.schemaOptions";
    public static final String COMPILE_SKIP_IF_KNOWN = "compile.skipIfKnown";
    public static final String COMPILE_ENTITY_RESOLVER = "compile.entityResolver";
    public static final String COMPILE_ANONYMOUS_TYPE_NAMES = "compile.anonymousTypeNames";
    public static final String COMPILE_ALLOW_MULTIPLE_DEFINITIONS = "compile.allowMultipleDefinitions";

    /**
     * Standard names are used for anonymous Schema types, as per the SDO spec. The name is the same name
     * as the enclosing element/attribute. This is simple and spec-compliant, but it can introduce collisions
     * that didn't exist in XMLSchema, causing SDO to fail to process valid XMLSchema files
     */
    public static final int NAMES_STANDARD = 1;
//    /**
//     * Standard (spec) names are used for anonymous types, but if there is a conflict with a global type with
//     * the same name or with another anonymous types, numbers are appended to the name as necessary. This is
//     * also spec-compliant for the cases in which there are no name collisions and in addition can also process
//     * Schemas that would otherwise fail. The disadvantage is that the same Schema type can end up with different
//     * names depending on whether the "default" name was already "taken"
//     */
//    public static final int NAMES_WITH_NUMBER_SUFFIX = 2;
    /**
     * The name used is a concatenation of all the enclosing particles, separated by '$' or '@' depending on
     * what the enclosing particle is (element or attribute). This has the advantage of being deterministic and
     * in line with what the name of the corresponding generated Java class (which will be anonymous), but has the
     * disadvantage of not being SDO spec compliant
     */
    public static final int NAMES_COMPOSITE = 3;

    /**
     * Can be set on {@link javax.sdo.helper.XMLHelper} <code>save</code> methods.
     * If set, the marshaller will not throw exceptions for what would be error conditions otherwise
     * <p/>
     * <b>NOTE: </b> Enabling this may result in data loss, because data that can't be marshalled
     * will be silently ignored.
     */
    public final Options setSaveDontThrowExceptions()
    {
        map.put(SAVE_DONT_THROW_EXCEPTIONS, null);
        return this;
    }

    /**
     * Can be set on {@link javax.sdo.helper.XMLHelper} <code>save</code> methods.
     * If set, the marshaller will pretty-print the resulting XML independent of output format.<p/>
     * Pretty-printing will affect only complex types with mixed=false
     */
    public final Options setSavePrettyPrint()
    {
        map.put(SAVE_PRETTY_PRINT, null);
        return this;
    }

    /**
     * Can be set on {@link javax.sdo.helper.XMLHelper} <code>save</code> methods.
     * Sets the amount of indentation that is used for pretty-printing {@see setMarshalPrettyPrint}
     * @param step the amount of indentation to use if pretty-printing is on; default is 4
     */
    public final Options setSaveIndent(int step)
    {
        map.put(SAVE_INDENT, step);
        return this;
    }

    /**
     * Can be set on {@link javax.sdo.helper.XMLHelper} <code>load</code> methods.
     * If set, the unmarshaller will not throw exceptions for recoverable error conditions
     * <p/>
     * Note. This only applies to "recoverable" error conditions, so the calling code must still be
     * prepared to handle SDO exceptions.
     * Currently, the only recoverable error condition is "xsi:type not found"
     */
    public final Options setLoadDontThrowExceptions()
    {
        map.put(LOAD_DONT_THROW_EXCEPTIONS, null);
        return this;
    }

    /**
     * Can be set on both {@link davos.sdo.impl.helpers.XMLHelperImpl} <code>load</code> and
     * <code>save</code> methods, but will only be taken into account by the methods that
     * use an <code>javax.xml.stream.XMLStreamReader</code>.
     */
    public final Options setValidate()
    {
        map.put(VALIDATE, null);
        return this;
    }

    /**
     * Can be set on {@link davos.sdo.type.XSDHelperExt} (and {@link javax.sdo.helper.XSDHelper})
     * <code>define</code> methods.
     * @param options Schema compilation options that will be passed directly to the Schema compiler
     */
    public final Options setCompileSchemaOptions(XmlOptions options)
    {
        map.put(COMPILE_SCHEMA_OPTIONS, options);
        return this;
    }

    /**
     * Can be set on {@link davos.sdo.type.XSDHelperExt} (and {@link javax.sdo.helper.XSDHelper})
     * <code>define</code> methods.
     * If set, SDO types will be genrated only for those Schema types that are <b>not</b> found in the
     * {@link davos.sdo.SDOContext}. This way, if the same Schema is compiled multiple times, types
     * will only get generated the first time
     */
    public final Options setCompileSkipTypesFromContext()
    {
        map.put(COMPILE_SKIP_IF_KNOWN, null);
        return this;
    }

    /**
     * Can be set on {@link davos.sdo.type.XSDHelperExt} (and {@link javax.sdo.helper.XSDHelper})
     * <code>define</code> methods.
     * @param er an instance of <code>org.xml.sax.EntityResolver</code> that will be used by the
     *     Schema compilation process to delegate to the caller when resolving Schema <i>import</i>s
     *     and <i>include</i>s
     */
    public final Options setCompileEntityResolver(EntityResolver er)
    {
        map.put(COMPILE_ENTITY_RESOLVER, er);
        return this;
    }

    /**
     * Can be set on {@link davos.sdo.type.XSDHelperExt} (and {@link javax.sdo.helper.XSDHelper})
     * <code>define</code> methods. Controls the manner in which names for SDO types corresponding to
     * anonymous Schema types are generated.
     * @param value one of {@link #NAMES_STANDARD}, {@link #NAMES_WITH_NUMBER_SUFFIX} or {@link #NAMES_COMPOSITE}
     */
    public final Options setCompileAnonymousTypeNames(int value)
    {
        map.put(COMPILE_ANONYMOUS_TYPE_NAMES, value);
        return this;
    }

    /**
     * Can be set on {@link davos.sdo.type.XSDHelperExt} (and {@link javax.sdo.helper.XSDHelper})
     * <code>define</code> methods. Allows compilation of Schemas with conflicting global components
     * (like for example multiple global types with the same name and namespace URI). Normally, this
     * would be an error condition in XMLSchema. <br/>
     * NOTE: Use with care, because in case conflicting definitions do exist, then the first definition
     * (in file and text order) will be used and the rest ignored, which may cause an unexpected result
     */
    public final Options setCompileAllowMultipleDefinitions()
    {
        map.put(COMPILE_ALLOW_MULTIPLE_DEFINITIONS, null);
        return this;
    }

    public Map getMap()
    {
        return map;
    }
}
