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
package davos.sdo.impl.type;

import javax.sdo.Property;

import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.util.LinkedList;
import java.util.HashMap;

import davos.sdo.impl.common.Common;

/**
 * @author Cezar Andrei (cezar dot andrei at gmail dot com)
 *         Date: Oct 19, 2006
 */
public class MetadataHolder
{
    private Map<Property, Object> _propertyValues;
    private boolean _isImmutable = false;

    void init(Map<Property, Object> propertyValues)
    {
        _propertyValues = propertyValues;
    }

    public void makeImmutable()
    {
        if (_propertyValues == null)
            _propertyValues = Common.EMPTY_PROPERTY_OBJECT_MAP;
        else
            _propertyValues = Collections.unmodifiableMap(_propertyValues);

        _isImmutable = true;
    }

    public List /*Property*/ getInstanceProperties()
    {
        // todo (cezar) BUG BUG for radu
//        assert _propertyValues!=null : "makeImmutable wasn't called for: " + this.toString();
//        if (!_isImmutable)
//            throw new IllegalStateException("MetadataHolder is mutable: " + this.toString());

        if (_propertyValues==null)
            return null;

        return new LinkedList<Property>(_propertyValues.keySet());
    }

    public Object get(Property property)
    {
        if (_propertyValues==null)
            return null;
        
        return _propertyValues.get(property);
    }

    public void addPropertyValue(Property property, Object propertyValue)
    {
        if (_isImmutable)
            throw new IllegalStateException("MetadataHolder is immutable new values cannot be added.");

        if (_propertyValues == null)
        {
            _propertyValues = new HashMap<Property,  Object>();
        }

        _propertyValues.put(property,  propertyValue);
    }
}
