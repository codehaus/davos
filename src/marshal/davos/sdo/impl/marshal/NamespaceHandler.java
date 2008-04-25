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

import javax.xml.namespace.NamespaceContext;

/**
 * This interface extends the standard <code>NamespaceContext</code>
 * because we need to be able to "save" the state of the prefix mappings
 *
 * @author Cezar Andrei (cezar dot andrei at gmail dot com)
 */
public interface NamespaceHandler extends NamespaceContext
{
    Map<String, String> savePrefixMap();

    void pushNamespaceContext();

    void popNamespaceContext();

    void declarePrefix(String prefix, String uri);

    void resetNamespaces();
}
