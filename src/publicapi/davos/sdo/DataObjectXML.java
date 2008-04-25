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

import javax.sdo.DataObject;

import java.util.List;

/**
 * @author Cezar Andrei (cezar dot andrei at gmail dot com)
 *         Date: Mar 21, 2006
 */
public interface DataObjectXML
    extends DataObject
{
    TypeXML getTypeXML();
    SequenceXML getSequenceXML();
    ListXMLIterator getUnsequencedXML();
    ListXMLIterator getListXMLIterator(PropertyXML property);

    // todo cezar remove after radu changes the marshaller to use the ListXMLIterator
    String getXMLPrefix(int index);

    DataObjectXML createDataObjectXML(PropertyXML propertyXML, String prefix, PropertyXML substitution);
    DataObjectXML createDataObjectXML(PropertyXML propertyXML, TypeXML typeXML,
        String prefix, PropertyXML substitution);

    void setXML(PropertyXML propertyXML, Object value, String prefix, PropertyXML substitution);
    DataObjectXML getContainerXML();
    PropertyXML getContainmentPropertyXML();

    /**
     * If this DataObjectXML represents an XML element, it will have to carry the name of
     * the element, which is set by this method. Note that the call will fail if this
     * DataObjectXML is already attached to a parent DataObject.
     * @param prop the global property corresponding to the XML element
     */
    void setContainmentPropertyXML(PropertyXML prop);

    List<PropertyXML> getInstancePropertiesXML();
}
