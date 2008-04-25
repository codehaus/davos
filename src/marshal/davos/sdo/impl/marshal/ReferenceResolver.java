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

import java.util.Map;

import davos.sdo.DataObjectXML;
import davos.sdo.impl.xpath.XPath;

import javax.sdo.DataObject;

/**
 * This is used by unmarshallers to resolve idrefs and paths
 */
interface ReferenceResolver
{
    DataObject resolvePath(XPath path, DataObjectXML contextNode);

    DataObject resolveId(String id);

    /**
     * Used to register id's as they are encountered. It's basically a Map.put(id, node)
     */
    void registerId(String id, DataObject node);

    void setIdMap(Map<String, DataObject> map);
}
