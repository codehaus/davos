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
package davos.sdo.impl.marshal;

import davos.sdo.SDOXmlException;
import davos.sdo.SDOError;

import java.util.AbstractList;

class SDOExceptionListener<T> extends AbstractList<T>
{
    private boolean _marshal;

    public SDOExceptionListener(boolean marshal)
    {
        _marshal = marshal;
    }

    public boolean add(T o)
    {
        throw new SDOXmlException(SDOError.messageForCode(_marshal ?
            "marshal.validation" : "unmarshal.validation", o.toString()));
    }

    public T get(int index)
    {
        return null;
    }

    public int size()
    {
        return 0;
    }
}
