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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import davos.sdo.impl.common.Common;

/**
 * 
 * @author radup
 * This implementation of NamespaceHandler is very fast at pushing and popping contexts
 * but not as fast at retrieving a given prefix mapping.
 * This is ok because in our use of it we only need to look up prefixes for xsi:type,
 * QName values and XPaths, so not as often.
 * This implementation contains the default mapping for the "" prefix but it does not
 * support queries for the "xml" or "xmlns" prefixes (returns null)
 * Also, queries for the <code>null</code> prefix or the <code>null</code> namespace URI are
 * treated as queries for the default prefix or namespace URI 
 */
public class SimpleNamespaceHandler implements NamespaceHandler
{
    // Holds all prefix -> namespace mappings declared in the document
    // data[2*i] = prefix
    // data[2*i+1] = namespace
    String[] data;
    // Holds a stack of indices in data where context pushes happened
    int[] stack;
    // Current size of the data array
    int length;
    // The size of the context array
    int contexts;

    public SimpleNamespaceHandler()
    {
        data = new String[14]; // magic number of 7 prefixes
        stack = new int[10]; // magic number of 10 levels of nesting
        // We initialize the mapping with "" -> ""
        data[0] = data[1] = Common.EMPTY_STRING;
        length = 2;
        contexts = 0;
    }

    public Map<String, String> savePrefixMap()
    {
        Map<String, String> result = new HashMap<String, String>();
        for (int i = length; i > 0; )
        {
            String uri = data[--i];
            String prefix = data[--i];
            if (!result.containsKey(prefix))
                result.put(prefix, uri);
        }
        return result;
    }

    public String getNamespaceURI(String prefix)
    {
        if (prefix == null)
            prefix = Common.EMPTY_STRING;
        for (int i = length; i > 0; )
        {
            i -= 2;
            if (data[i].equals(prefix))
                return data[i + 1];
        }
        return null;
    }

    public String getPrefix(String namespaceURI)
    {
        if (namespaceURI == null)
            namespaceURI = Common.EMPTY_STRING;
        for (int i = length - 1; i > 0; i-=2)
        {
            if (data[i].equals(namespaceURI))
                return data[i - 1];
        }
        return null;
    }

    public Iterator getPrefixes(String namespaceURI)
    {
        if (namespaceURI == null)
            namespaceURI = Common.EMPTY_STRING;
        List<String> result = new ArrayList<String>();
        Map<String, Object> usedPrefixes = new HashMap<String, Object>();
        for (int i = length - 1; i > 0; i-=2)
        {
            String prefix = data[i - 1];
            if (data[i].equals(namespaceURI))
            {
                if (!usedPrefixes.containsKey(prefix))
                    result.add(prefix);
            }
            usedPrefixes.put(prefix, null);
        }
        return result.iterator();
    }

    public void declarePrefix(String prefix, String uri)
    {
        ensureSpace(length + 2);
        data[length++] = prefix;
        data[length++] = uri;
    }

    public void pushNamespaceContext()
    {
        if (contexts >= stack.length)
        {
            // Expand the stack, also linearily
            int[] temp = new int[stack.length + 10];
            System.arraycopy(stack, 0, temp, 0, contexts);
            stack = temp;
        }
        stack[contexts++] = length;
    }

    public void popNamespaceContext()
    {
        length = stack[--contexts];
    }

    public void resetNamespaces()
    {
        length = 2;
        contexts = 0;
    }

    private void ensureSpace(int max)
    {
        if (max > data.length)
        {
            // We are linearily increasing the size of the array
            // I have never heard of documents with more than 10 namespace declarations at a given point
            int newLength = data.length + 14;
            while (newLength < max)
                newLength += 14;
            String[] temp = new String[newLength];
            System.arraycopy(data, 0, temp, 0, length);
            data = temp;
        }
    }
}
