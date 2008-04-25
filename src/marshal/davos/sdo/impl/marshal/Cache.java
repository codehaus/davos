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

import java.lang.ref.SoftReference;

public class Cache
{
    private static Cache INSTANCE = new Cache();

    public static Cache get()
    {
        return INSTANCE;
    }

    private ThreadLocal tl_piccoloSaxLoader = new ThreadLocal();

    public SaxLoader getPiccoloSaxLoader()
    {
        SoftReference sr = (SoftReference) tl_piccoloSaxLoader.get();
        SaxLoader sl;
        if (sr == null)
        {
            sl = SaxLoader.PiccoloSaxLoader.newInstance();
            tl_piccoloSaxLoader.set(new SoftReference(sl));
        }
        else
        {
            sl = (SaxLoader) sr.get();
            if (sl == null)
            {
                sl = SaxLoader.PiccoloSaxLoader.newInstance();
                tl_piccoloSaxLoader.set(new SoftReference(sl));
            }
        }
        return sl;
    }
}
