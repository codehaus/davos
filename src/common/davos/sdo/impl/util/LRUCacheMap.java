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
package davos.sdo.impl.util;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Limited size cache hashmap. Only the last maxSize items will remain in memory.
 */
public class LRUCacheMap<K, V> extends LinkedHashMap<K, V>
{
    static final long serialVersionUID = 1L;

    public static final int ZERO_SIZE = 0;
    public static final int DEFAULT_SIZE;
    static
    {
        int defaultSize = 0;
        String property = System.getProperty("SDO_LRU_CACHE_SIZE");
        try
        {
            defaultSize = property!=null?
                          Integer.parseInt(property):
                          200;
        }
        catch(NumberFormatException nfe)
        {
            defaultSize = 200;
        }

        DEFAULT_SIZE = defaultSize;
    }

    protected int maxSize;

    public LRUCacheMap()
    {
        this(DEFAULT_SIZE);
    }

    public LRUCacheMap(int maxSize)
    {
        super(16, 0.75f, true);

        if (maxSize<0)
            throw new IllegalArgumentException();

        this.maxSize=maxSize;
    }

    public int getCapacity() { return maxSize; }

    protected boolean removeEldestEntry(Map.Entry<K, V> eldest)
    {
        return size()>maxSize;
    }
}
