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

import java.util.Iterator;

import davos.sdo.PropertyXML;

/**
 * Created
 * Date: Nov 21, 2006
 * Time: 4:44:50 PM
 * This class is used to iterate through values of a <code>DataObject</code> and it includes the
 * new and old values in the same iterator.
 * If the DataObject used to construct this is a Sequence, then the iterator iterates over the
 * properties in the sequence order.
 * If the DataObject is not a Sequence, then the iterator will iterate over all properties in the
 * order returned by {@link javax.sdo.DataObject#getInstanceProperties()}.
 * If a property is not set and there are no changes associated to it, then it will be skipped.
 * If a property is multi-valued and its type is a DataObject type, then the iterator will iterate
 * over the values in the list and will include new values and old values in relative order
 * (marked appropriately), if any. Otherwise, one single entry will be returned, containing the current
 * value (which can be itself a List). If the value is new, then two entries are returned, first
 * for the old value, then the second for the new value
 */
public interface DataObjectIterator extends Iterator
{
    public Change next();

    public static class Change
    {
        /**
         *
         */
        public static final int SAME = 0;
        /**
         *
         */
        public static final int NEW = 1;
        /**
         *
         */
        public static final int OLD = 2;

        private int changeType;
        private Object value;
        private PropertyXML property;

        public Change(PropertyXML property)
        {
            this.property = property;
        }

        public int getChangeType()
        {
            return changeType;
        }

        public Object getValue()
        {
            return value;
        }

        public PropertyXML getProperty()
        {
            return property;
        }

        public void setChangeType(int changeType)
        {
            this.changeType = changeType;
        }

        public void setValue(Object value)
        {
            this.value = value;
        }
    }
}
