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


import javax.sdo.impl.HelperProvider;
import org.apache.xmlbeans.SchemaTypeLoader;

import java.lang.reflect.InvocationTargetException;
import java.lang.ref.SoftReference;

/**
 * @author Cezar Andrei (cezar dot andrei at gmail dot com)
 *         Date: Mar 8, 2006
 */
public class SDOContextFactory
{
    private static String SDOCONTEXTIMPL = "davos.sdo.impl.context.SDOContextImpl";

    /**
     * Creates an SDOContext that will use the given classLoader to load up precompiled types and properties.
     */
    public static SDOContext createNewSDOContext(ClassLoader classLoader)
    {
        try
        {
            Class classSDOContextImpl = Class.forName(SDOCONTEXTIMPL);
            return (SDOContext)classSDOContextImpl.getMethod("newInstance", ClassLoader.class).
                           invoke(null, classLoader);

        }
        catch (ClassNotFoundException e)
        {
            throw new RuntimeException(e);
        }
        catch (NoSuchMethodException e)
        {
            throw new RuntimeException(e);
        }
        catch (IllegalAccessException e)
        {
            throw new RuntimeException(e);
        }
        catch (InvocationTargetException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates an SDOContext that contains only the built-in types and properties.
     */
    public static SDOContext createNewSDOContext()
    {
        try
        {
            Class classSDOContextImpl = Class.forName(SDOCONTEXTIMPL);
            return (SDOContext)classSDOContextImpl.getMethod("newInstance", ClassLoader.class).
                           invoke(null, (ClassLoader)null);

        }
        catch (ClassNotFoundException e)
        {
            throw new RuntimeException(e);
        }
        catch (NoSuchMethodException e)
        {
            throw new RuntimeException(e);
        }
        catch (IllegalAccessException e)
        {
            throw new RuntimeException(e);
        }
        catch (InvocationTargetException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates an SDOContext that will use the schemaTypeLoader to load precompiled types and properties.
     * Note: This is used by ALDSP to load on demand necesary schemas. 
     * @deprecated Use {@link #createNewSDOContext(SchemaTypeLoader,Object)} instead
     */
    public static SDOContext createNewSDOContext(SchemaTypeLoader schemaTypeLoader)
    {
        return createNewSDOContext(schemaTypeLoader, null);
    }

    /**
     * Creates an SDOContext that will use the schemaTypeLoader to load precompiled types and properties.
     * Note: This is used by ALDSP to load on demand necesary schemas. 
     * @param options TODO
     */
    public static SDOContext createNewSDOContext(SchemaTypeLoader schemaTypeLoader, Object options)
    {
        try
        {
            Class classSDOContextImpl = Class.forName(SDOCONTEXTIMPL + "$SDOContextForSchemaTypeLoader");
            return (SDOContext)classSDOContextImpl.getMethod("newInstance", SchemaTypeLoader.class, Object.class).
                           invoke(null, schemaTypeLoader, options);
        }
        catch (ClassNotFoundException e)
        {
            throw new RuntimeException(e);
        }
        catch (NoSuchMethodException e)
        {
            throw new RuntimeException(e);
        }
        catch (IllegalAccessException e)
        {
            throw new RuntimeException(e);
        }
        catch (InvocationTargetException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * @return Returns the global static SDOContext.
     * Warning: This context should not be used in an application server environment.
     */
    public static SDOContext getGlobalSDOContext()
    {
        return (SDOContext)HelperProvider.getDefaultContext();
    }

    /** SDOContext reference on ThreadLocal - it's user settable/gettable */
    private static ThreadLocal<SoftReference<SDOContext>> THREADLOCAL_SDOContext =
            new ThreadLocal<SoftReference<SDOContext>>()
    {
        protected synchronized SoftReference<SDOContext> initialValue()
        {
            return new SoftReference<SDOContext>(null);
        }
    };

    /**
     * @return the SDOContext that was set with setThreadLocalSDOContext(SDOContext sdoContext) method,
     * null if a ThreadLocal context wasn't set.
     * @see #setThreadLocalSDOContext(SDOContext)
     */
    public static SDOContext getThreadLocalSDOContext()
    {
        return THREADLOCAL_SDOContext.get().get();
    }

    /**
     * @param sdoContext will be set as the ThreadLocal context.
     * @see #getThreadLocalSDOContext()
     */
    public static void setThreadLocalSDOContext(SDOContext sdoContext)
    {
        THREADLOCAL_SDOContext.set(new SoftReference<SDOContext>(sdoContext));
    }
}
