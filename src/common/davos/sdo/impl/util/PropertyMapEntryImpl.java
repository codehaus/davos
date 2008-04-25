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
public class PropertyMapEntryImpl
    extends HarmonyHashMap.Entry<QName, PropertyXML>
    implements PropertyMapEntry
{
    private PropertyXML _substitutionProperty;

    private PropertyMapEntryImpl(QName theKey, PropertyXML theValue)
    {
        super(theKey, theValue);
    }

    public PropertyMapEntryImpl(QName theKey, PropertyXML property, PropertyXML substitutionProperty)
    {
        super(theKey, property);
        _substitutionProperty = substitutionProperty;
    }

    public PropertyXML getProperty()
    {
        return getValue();
    }

    public void setProperty(PropertyXML property)
    {
        setValue(property);
    }

    public PropertyXML getSubstitutionProperty()
    {
        return _substitutionProperty;
    }

    public void setSubstitutionProperty(PropertyXML substitutionProperty)
    {
        _substitutionProperty = substitutionProperty;
    }
}
