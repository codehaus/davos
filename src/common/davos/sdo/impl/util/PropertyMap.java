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
package davos.sdo.impl.util;

import davos.sdo.PropertyXML;
import davos.sdo.PropertyMapEntry;

import javax.xml.namespace.QName;

/**
 * @author Cezar Andrei (cezar dot andrei at gmail dot com)
 *         Date: Jan 29, 2007
 */
public class PropertyMap
    extends HarmonyHashMap<QName, PropertyXML>
{
    public PropertyMapEntry getPropertyMapEntry(String uri, String xmlLocal)
    {
        return getPropertyMapEntry(new QName(uri, xmlLocal));
    }

    private PropertyMapEntryImpl getPropertyMapEntry(QName key)
    {
        return (PropertyMapEntryImpl)super.getEntry(key);
    }

    public PropertyMapEntry putPropertyMapEntry(String uri, String xmlLocal, PropertyXML property, PropertyXML substitutionProperty)
    {
        QName key = new QName(uri, xmlLocal);
        super.put(key , null);

        PropertyMapEntryImpl entry = getPropertyMapEntry(key);
        entry.setProperty(property);
        entry.setSubstitutionProperty(substitutionProperty);

        return entry;
    }

    Entry<QName, PropertyXML> createNewEntry(QName key, PropertyXML value)
    {
        return new PropertyMapEntryImpl(key, null, null);
    }
}
