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

import javax.sdo.Type;
import org.apache.xmlbeans.SchemaType;

import java.util.List;

import davos.sdo.type.TypeSystem;

import javax.xml.namespace.QName;

/**
 * @author Cezar Andrei (cezar dot andrei at gmail dot com)
 *         Date: Mar 21, 2006
 */
public interface TypeXML
    extends Type
{
    public boolean isAssignableFrom(TypeXML typeXml);

    /**
     * Same as Type.getProperties().
     * @see javax.sdo.Type#getProperties()
     */
    public List<PropertyXML> getPropertiesXML();

    /**
     * Same as Type.getDeclaredProperties().
     * @see javax.sdo.Type#getDeclaredProperties()
     */
    public List<PropertyXML> getDeclaredPropertiesXML();

    /**
     * Same as Type.getProperty(String propertyName)
     * @see javax.sdo.Type#getProperty(String propertyName)
     */
    public PropertyXML getPropertyXML(String propertyName);

    /**
     * Gets a PropertyXML based on the name and uri of an attribute
     * or element in the XML
     */
    public PropertyXML getPropertyXMLByXmlName(String uri, String name, boolean isElement);

    /**
     * @return a PropertyMapEntry where the key is the QName formed by uri and name
     * Note: PropretyMapEntry gives access to the original Property in the Type
     *       substitution Property is not valid in this context.
     */
    public PropertyMapEntry getPropertyMapEntryByXmlName(String uri, String name, boolean isElement);

    /**
     * All built-in types have a unique value, all other types have a type-code equal to -1.
     * @see #isBuiltinType()
     */
    public int getTypeCode();

    /**
     * Is a Built-in type and maps to a primitive java type?
     */
    public boolean isPrimitive();

    /**
     * Is one of the types defined in the SDO spec
     * @see #getTypeCode()
     * */
    public boolean isBuiltinType();

    /**
     * Returns the associated XML SchemaType
     * Note: if type wasn't constructed from XMLSchema it returns null.
     */
    public SchemaType getXMLSchemaType();

    /**
     * Returns the associated XML SchemaType
     * Note: if type wasn't constructed from XMLSchema it returns null.
     */
    public String getXMLSchemaTypeSignature();

    /**
     * Returns the associated XML SchemaType
     * Note: if type wasn't constructed from XMLSchema it returns null.
     */
    public QName getXMLSchemaTypeName();

    /**
     * Return the SDOTypeSystems in which it was created/loaded
     */
    public TypeSystem getSDOTypeSystem();

    /**
     * Boolean indicating if the type has simple content
     * The reason this is here is because otherwise the unmarshaller has no
     * way of knowing that a "value" property belonging to this type is to be
     * serialized as content or a separate child element
     */
    public boolean isSimpleContent();

    /**
     * Boolean indicating if the type has mixed content.
     * This allows us to differentiate between sequenced types that accept text
     * and sequenced types that don't and avoid adding extra text entries when
     * unmarshalling the sequenced if they're not needed.
     */
    public boolean isMixedContent();

    //public DFA getMarshalingDFA(); todo

    /**
     * If current type is a Datatype that represents a list this returns the type of the items
     */
    public TypeXML getListItemType();

    /**
     * Boolean indicating if the type has a user customization for instance class
     */
    public boolean hasCustomizedInstanceClass();
}
