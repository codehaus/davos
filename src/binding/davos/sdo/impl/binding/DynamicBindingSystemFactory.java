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
package davos.sdo.impl.binding;

import davos.sdo.binding.BindingSystemFactory;
import davos.sdo.binding.BindingSystem;
import davos.sdo.binding.BindingException;
import davos.sdo.type.TypeSystem;

/**
 * @author ljones
 *         First created: Mar 6, 2006
 */
public class DynamicBindingSystemFactory implements BindingSystemFactory
{
    public BindingSystem create(TypeSystem ts) throws BindingException
    {
        // Note: type system ignored for this one - we always use the default
        return new DynamicBindingSystem(null, Thread.currentThread().getContextClassLoader(), ts);
    }
}
