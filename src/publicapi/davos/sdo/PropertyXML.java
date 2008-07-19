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

import javax.sdo.Property;

import javax.xml.namespace.QName;

/**
 * @author Cezar Andrei (cezar dot andrei at gmail dot com)
 *         Date: Mar 21, 2006
 */
public interface PropertyXML
    extends Property
{
    /** @return true if this is pammed to an xml element. */
    public boolean isXMLElement();

    /** @return Local XML name. */
    public String getXMLName();

    /** @return Uri of XML name. */
    public String getXMLNamespaceURI();

    /** @return Other accepted global properties, in case substitutions were defined for this Property. */
    public PropertyXML[] getAcceptedSubstitutions();

    public int getSchemaTypeCode();

    /**
     * @return true if property doesn't belong to a type: i.e. global and on-demand properties
     *         false if defined inside a type (all cases, through TypeHelper.define(), XSDHelper.define() or sdocomp)
     */
    public boolean isDynamic();

    /**
     * @return true for global properties - per spec definition
     */
    public boolean isGlobal();

    public TypeXML getTypeXML();

    public TypeXML getContainingTypeXML();

    public PropertyXML getOppositeXML();
}
