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

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.xml.sax.helpers.NamespaceSupport;

/** ===========================================
 * NamespaceHandler implementation
 * We should probably implement our own prefix handling if we want
 * every ounce of performance
 * DONE -> SimpleNamespaceHandler
 * ===========================================
 */
class NamespaceSupportDelegator implements NamespaceHandler
{
    private NamespaceSupport _nssupport;

    public NamespaceSupportDelegator()
    {
        _nssupport = new NamespaceSupport();
    }

    public String getNamespaceURI(String prefix)
    {
        return _nssupport.getURI(prefix);
    }

    public String getPrefix(String namespaceURI)
    {
        return _nssupport.getPrefix(namespaceURI);
    }

    public Iterator getPrefixes(String namespaceURI)
    {
        ArrayList<Object> currentMappingsForUri = new ArrayList<Object>();
        Enumeration e = _nssupport.getPrefixes(namespaceURI);
        for (; e.hasMoreElements(); )
            currentMappingsForUri.add(e.nextElement());
        return currentMappingsForUri.iterator();
    }

    public Map<String, String> savePrefixMap()
    {
        Map<String, String> result = new HashMap<String, String>();
        for (Enumeration e = _nssupport.getPrefixes(); e.hasMoreElements(); )
        {
            String prefix = (String) e.nextElement();
            result.put(prefix, _nssupport.getURI(prefix));
        }
        return result;
    }

    public void declarePrefix(String prefix, String uri)
    {
        _nssupport.declarePrefix(prefix, uri);
    }

    public void popNamespaceContext()
    {
        _nssupport.popContext();
    }

    public void pushNamespaceContext()
    {
        _nssupport.pushContext();
    }

    public void resetNamespaces()
    {
        _nssupport.reset();
    }
}
