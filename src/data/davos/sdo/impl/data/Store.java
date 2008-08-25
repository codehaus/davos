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
package davos.sdo.impl.data;

import davos.sdo.PropertyXML;
import davos.sdo.SequenceXML;
import davos.sdo.ListXMLIterator;
import javax.sdo.Property;

import java.util.List;

/**
 * @author Cezar Andrei (cezar dot andrei at gmail dot com)
 *         Date: Jan 11, 2006
 */
public interface Store
{
    /**
     * Returns the instance property that has propertyName as name or alias.
     */
    public PropertyXML storeGetProperty(String propertyName);

    public Object      storeGet(PropertyXML property);

    public boolean     storeSet(PropertyXML property, Object value);
    public boolean     storeSet(PropertyXML property, Object value, String prefix, PropertyXML substitution);

    public boolean     storeAddNew(PropertyXML property, Object value);
    public boolean     storeAddNew(PropertyXML property, Object value, String prefix, PropertyXML substitution);

    /** @return returns true if at least one entry of property exists */
    public boolean     storeIsSet(PropertyXML property);

    /** Removes all entries of this property */
    public void        storeUnset(PropertyXML property);

//    /** Removes this instance value */
//    public void        storeUnsetInstance(Object value);

    /** All storeSequenceXXX methods operate with sequenceIndex which is a global index accross
     * all properties of this instance */
    public int         storeSequenceSize();
    public PropertyXML storeSequenceGetPropertyXML(int sequenceIndex);
    public PropertyXML storeSequenceGetSubstitution(int sequenceIndex);
    public Object      storeSequenceGetValue(int sequenceIndex);
    public String      storeSequenceGetXMLPrefix(int sequenceIndex);
    public void        storeSequenceSet(int sequenceIndex, Object value);
    public boolean     storeSequenceAddNew(int sequenceIndex, PropertyXML property, Object value,
                                           String prefix, PropertyXML substitution);
    public boolean     storeSequenceIsSet(int sequenceIndex);
    public void        storeSequenceUnset(int sequenceIndex);

    public SequenceXML storeGetSequenceXML();

    public ListXMLIterator storeGetUnsequencedXML();
    public ListXMLIterator storeGetListXMLIterator(PropertyXML property);

    public List<PropertyXML> storeGetInstanceProperties();
}
