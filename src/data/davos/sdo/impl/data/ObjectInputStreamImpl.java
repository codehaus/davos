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

import davos.sdo.SDOContext;

import java.io.ObjectInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Cezar Andrei (cezar dot andrei at gmail dot com)
 *         Date: Jul 30, 2007
 */

public class ObjectInputStreamImpl
    extends ObjectInputStream
{
    private SDOContext _sdoContext;

    protected ObjectInputStreamImpl()
        throws IOException, SecurityException
    {
        super();
    }

    public ObjectInputStreamImpl(InputStream in)
        throws IOException
    {
        super(in);
    }

    public SDOContext getSDOContext()
    {
        return _sdoContext;
    }

    public void setSDOContext(SDOContext sdoContext)
    {
        _sdoContext = sdoContext;
        assert sdoContext!=null;
    }
}
