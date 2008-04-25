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
package davos.sdo.binding;

import davos.sdo.DataObjectXML;
import davos.sdo.PropertyXML;
import davos.sdo.TypeXML;
import davos.sdo.type.TypeSystem;
import javax.sdo.DataGraph;

/**
 * The implementation behind this interface has to make the link between SDO types (i.e. any TypeXML) and
 * a certain DataObjectXML implementation class.
 * <br/><b>Note:</b>Only BindingSystem implementations can keep a pointer to a TypeSystem, a reverse pointer is not
 * alowed. The reason is to support multiple implementations of DataObject instances that have the same SDO Type, by
 * using multiple contexts each one having it's own BindingSystem and the same TypeSystem. 
 */
public interface BindingSystem
{
    public TypeSystem getTypeSystem();
    public DataObjectXML createDataObjectForType(TypeXML type, DataGraph datagraph);
    public DataObjectXML createChildForDataObject(DataObjectXML parent, PropertyXML prop, TypeXML childType);
    public DataObjectXML createChildForDataObject(DataObjectXML parent, PropertyXML prop, TypeXML childType,
        String prefix, PropertyXML substitution);


    public String getIntfFullNameForType(TypeXML type);
    public String getImplFullNameForType(TypeXML type);
    public Class getInstanceClassForType(TypeXML type);
    public Class getInstanceClassForJavaName(String name);


    /**
     * Returns the TypeXML associated to this interfaceClass.
     * @see javax.sdo.helper.TypeHelper#getType(Class interfaceClass)
     */
    public TypeXML getType(Class interfaceClass);

    /**
     * Returns the TypeXML associated to this qname. This if not already loaded,
     * this method will search and load the type (by it's name) from its associated ResourceLoader.
     */
    public TypeXML loadTypeByTypeName(String uri, String typeName);

    /**
     * Returns the global PropertyXML asociated to the top level (global) element with uri, elemName.
     * elemName is the XMLSchema name.
     * <br/>Note: If the element is not in the context, this will trigger loading from
     *       its associated ResourceLoader.
     */
    public PropertyXML loadGlobalPropertyByTopLevelElemQName(String uri, String elemName);

    /**
     * Returns the global PropertyXML asociated to the top level (global) attribute with uri, attrName.
     * attrName is the XMLSchema name.
     * <br/>Note: If the attribute is not in the context, this will trigger loading from
     *       its associated ResourceLoader.
     */
    public PropertyXML loadGlobalPropertyByTopLevelAttrQName(String uri, String attrName);

    /**
     * Returns the type associated to the schema type having this uri and name.
     * <br/>Note: This will not work for annonimus schema types since they don't have a name.
     * <br/>Note: If the schema type is not in the context, this will trigger loading from
     *       its associated ResourceLoader.
     */
    public TypeXML loadTypeBySchemaTypeName(String uri, String localName);
}
