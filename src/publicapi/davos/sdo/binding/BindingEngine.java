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
package davos.sdo.binding;

import davos.sdo.type.TypeSystem;
import davos.sdo.TypeXML;

import java.io.IOException;
import java.util.Map;

/**
 * Code-generation engine - to generate code representing a given SDOTypeSystem
 */
public interface BindingEngine
{
    public void setContext(BindingContext bindingCtx);

    /**
     * Generate the code to represent the bound types within the SDO TypeSystem
     * passed in
     * @param ts - the SDO TypeSystem from which to generate the java classes
     * @param packageNames
     * @param instanceClasses
     * @return true if successful, false otherwise
     */
    public boolean bind(TypeSystem ts, BindingSystem bindingSystemOnClasspath, Map<TypeXML, String> packageNames, Map<TypeXML, String> instanceClasses)
        throws BindingException, IOException;
}
