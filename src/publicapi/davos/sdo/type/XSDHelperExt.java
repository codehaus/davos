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
package davos.sdo.type;

import javax.sdo.helper.XSDHelper;
import javax.sdo.Type;

import java.io.InputStream;
import java.io.Reader;
import java.util.List;

import davos.sdo.PropertyXML;

public interface XSDHelperExt extends XSDHelper
{
    /**
     * Compiles a Schema given by <code>schemaAsReader</code> and adds it to the context
     * @param schemaAsReader the Schema to compile
     * @param schemaLocation the location of the Schema, used to resolve relative imports
     * @param options parameters for the compilation, can be a {@link davos.sdo.Options} object
     *        or a java.util.Map object or an instance of org.apache.xmlbeans.XmlOptions
     * @return the List of SDO types added to the context as a result of this operation
     */
    public List<Type> defineSchema(Reader schemaAsReader, String schemaLocation, Object options);

    /**
     * Compiles a Schema given by <code>schemaAsInputStream</code> and adds it to the context
     * @param schemaAsInputStream the Schema to compile
     * @param schemaLocation the location of the Schema, used to resolve relative imports
     * @param options parameters for the compilation, can be a {@link davos.sdo.Options} object
     *        or a java.util.Map object or an instance of org.apache.xmlbeans.XmlOptions
     * @return the List of SDO types added to the context as a result of this operation
     */
    public List<Type> defineSchema(InputStream schemaAsInputStream, String schemaLocation, Object options);

    /**
     * Compiles a Schema given by <code>schemaAsString</code> and adds it to the context
     * @param schemaAsString the Schema to compile
     * @param schemaLocation the location of the Schema, used to resolve relative imports
     * @param options parameters for the compilation, can be a {@link davos.sdo.Options} object
     *        or a java.util.Map object or an instance of org.apache.xmlbeans.XmlOptions
     * @return the List of SDO types added to the context as a result of this operation
     */
    public List<Type> defineSchema(String schemaAsString, String schemaLocation, Object options);

    /**
     * Compiles an array of Schemas given by <code>schemasAsStrings</code> and add all the types therein
     * to the context. The lengths of <code>schemaAsStrings</code> and <code>schemaLocations</code>
     * arrays have to match
     * @param schemasAsStrings the Schemas to compile
     * @param schemaLocations the locations of the Schemas, used to resolve relative imports
     * @param options parameters for the compilation, can be a {@link davos.sdo.Options} object
     *        or a java.util.Map object or an instance of org.apache.xmlbeans.XmlOptions
     * @return the List of SDO types added to the context as a result of this operation
     */
    public List<Type> defineSchemas(String[] schemasAsStrings, String[] schemaLocations, Object options);

    /**
     * Compiles all the Schemas found in the &lt;types&gt; section of the WSDL file loaded from
     * <code>location</code> and adds all the resulting SDO types to the context
     * @param wsdlLocation the location (as a URL) of the WSDL file
     * @param options parameters for the compilation, can be a {@link davos.sdo.Options} object
     *        or a java.util.Map object or an instance of org.apache.xmlbeans.XmlOptions
     * @return the List of SDO types added to the context as a result of this operation
     */
    public List<Type> defineSchemasFromWsdl(String wsdlLocation, Object options);

    /**
     * Checks whether a property represents the simple content of a complex Schema type with\
     * simple content
     * @param property the property
     * @return true if the property maps to simple content, false if it maps to an attribute or an
     * element
     */
    public boolean isSimpleContent(PropertyXML property);
}
