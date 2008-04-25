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

import davos.sdo.binding.BindingSystem;
import davos.sdo.type.TypeSystem;
import javax.sdo.helper.HelperContext;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;

/**
 * SDOContext extends Helper Context interface that gives access to all SDO related
 * helpers and factories of a context.
 * @author Cezar Andrei (cezar dot andrei at gmail dot com)
 *         Date: Mar 2, 2007
 */
public interface SDOContext
    extends HelperContext
{
    /**
     * @return Returns the associated Binding System
     */
    BindingSystem getBindingSystem();

    /**
     * @return Returns the associated Type System
     */
    TypeSystem getTypeSystem();

    /**
     * @return Returns the associated Type System
     */
    ClassLoader getClassLoader();

    /**
     * Creates an ObjectInputStream that will deserialize DataObjects in this context.
     * @param inputStream used for deserialization
     * @return the context aware ObjectInputStream
     * @throws IOException from the underlying inputStream
     */
    public ObjectInputStream createObjectInputStream(InputStream inputStream)
        throws IOException;
}
