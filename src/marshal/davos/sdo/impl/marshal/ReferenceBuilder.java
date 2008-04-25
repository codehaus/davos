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

import javax.sdo.DataObject;
import davos.sdo.impl.common.NamespaceStack;
import davos.sdo.DataObjectXML;

/**
 * This is used to build idrefs and paths to use in serialization
 */
interface ReferenceBuilder
{
    /**
     * Builds an SDO path referring to <code>node</code>. The path is absolute
     * if <code>contextNode</code> is null, otherwise relative
     * @param node the target node of the SDO path
     * @param contextNode if not null, then the resulting path will be relative to
     * this node
     * @param nsstck a namespace builder that can be used to generate QNames
     * @return an SDO path
     */
    String  getPathOrId(DataObjectXML node, DataObject contextNode, NamespaceStack nsstck);

    String getId(DataObjectXML node);

    String getPath(DataObjectXML node, DataObject contextNode, NamespaceStack nsstck);
}
