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

import java.util.Set;
import davos.sdo.TypeXML;
import davos.sdo.PropertyXML;
import org.apache.xmlbeans.SchemaTypeLoader;
import javax.sdo.Type;

/**
 * @author Cezar Andrei (cezar dot andrei at gmail dot com)
 *         Date: Jan 13, 2006
 */
public interface TypeSystem
{
    /**
     * Returns the TypeXML that has the SDO name: uri, localName.
     * Note: This method does not load the type from the classpath, use
     *       davos.sdo.binding.BindingSystem#loadTypeByTypeName(String, String)
     * @see davos.sdo.binding.BindingSystem#loadTypeByTypeName(String, String)
     */
    public TypeXML getTypeXML(String uri, String typeName);

    /**
     * Returns the same object if type is TypeXML or an equivalent TypeXML object
     */
    public TypeXML getTypeXML(Type type);

//    /**
//     * Returns the same object if property is PropertyXML or an equivalent PropertyXML object
//     */
//    public PropertyXML getPropertyXML(Property property);

    /**
     * Returns all the types contained by this TypeSystem.
     * Note: Can contain all built0in types.
     */
    public Set<TypeXML> getAllTypes();

    /**
     * Returns the global PropertyXML asociated to the top level element with uri, elemName.
     * Note: This method does not load the type system from the classpath, use
     *       davos.sdo.binding.BindingSystem#loadGlobalPropertyByTopLevelElemQName(String, String)
     * @see davos.sdo.binding.BindingSystem#loadGlobalPropertyByTopLevelElemQName(String, String)
     */
    public PropertyXML getGlobalPropertyByTopLevelElemQName(String uri, String elemName);

    /**
     * Returns the global PropertyXML asociated to the top level attribute with uri, attrName.
     * Note: This method does not load the type system from the classpath, use
     *       davos.sdo.binding.BindingSystem#loadGlobalPropertyByTopLevelAttrQName(String, String)
     * @see davos.sdo.binding.BindingSystem#loadGlobalPropertyByTopLevelAttrQName(String, String)
     */
    public PropertyXML getGlobalPropertyByTopLevelAttrQName(String uri, String attrName);

    /**
     * Returns the TypeXML asociated to the schema type with uri, localName.
     * Note: This method does not load the type system from the classpath, use
     *       davos.sdo.binding.BindingSystem#loadTypeBySchemaTypeName(String, String)
     * @see davos.sdo.binding.BindingSystem#loadTypeBySchemaTypeName(String, String)
     */
    public TypeXML getTypeBySchemaTypeName(String schemaTypeUri, String schemaTypeLocalName);

    //public PropertyXML getGlobalPropertyXML(String uri, String sdoName);
    //public PropertyXML getGlobalElement(String uri, String schemaElementName);
    //public PropertyXML getGlobalAttribute(String uri, String shemaAttributeName);

    /**
     * @return The SchemaTypeSystem associated to this SDO TypeSystem
     */
    public SchemaTypeLoader getSchemaTypeLoader();
}
