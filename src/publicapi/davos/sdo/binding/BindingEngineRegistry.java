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

import davos.sdo.util.Logger;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Singleton class storing the registry of binding engines
 */
public class BindingEngineRegistry
{
    // static vars
    private static BindingEngineRegistry INSTANCE = new BindingEngineRegistry();
    private static final String[][] DEF_ENTRIES = new String[][] {
            {"default", "davos.sdo.binding.impl.DefaultBindingEngineImpl"}
    };

    // member vars
    private Map<String, BindingEngine> _bindings;

    private BindingEngineRegistry()
    {
        // want to return keys in name order - so keep in ordered map
        _bindings = new TreeMap();
    }

    public synchronized boolean registerBindingEngine(String name, BindingEngine engine)
    {
        if (_bindings.get(name) != null)
        {
            return false; // need to deregister BindingEngine at this name first
        }

        _bindings.put(name, engine);
        return true;
    }

    public synchronized void deregisterBindingEngine(String name)
    {
        _bindings.remove(name);
    }

    public BindingEngine getBindingEngine(String name)
    {
        return _bindings.get(name);
    }

    public Set<String> getAllBoundNames()
    {
        return _bindings.keySet();
    }

    public Set<Map.Entry<String, BindingEngine>> getAllEntries()
    {
        return _bindings.entrySet();
    }

    public void loadDefaultEntries(Logger l)
    {
        BindingEngineRegistry reg = getInstance();
        for (int i=0; i<DEF_ENTRIES.length; i++)
        {
            String name = DEF_ENTRIES[i][0];
            String className = DEF_ENTRIES[i][1];
            Class c;
            try {
                c = Class.forName(className);
            } catch (ClassNotFoundException e) {
                l.warning(BindingEngineRegistry.class.getName() + ": Could not " +
                    "find default binding engine with class " + className, e);
                continue;
            }

            if (!BindingEngine.class.isAssignableFrom(c))
            {
                l.warning(BindingEngineRegistry.class.getName() + ": Default " +
                    "binding engine with class name " + className + " does not " +
                    "implement interface " + BindingEngine.class.getName());
                continue;
            }

            BindingEngine engine;
            try {
                engine = (BindingEngine)c.newInstance();
            } catch (InstantiationException e) {
                l.warning(BindingEngineRegistry.class.getName() + ": Default " +
                    "binding engine with class name " + className + " could not " +
                    "be instantiated ", e);
                continue;
            } catch (IllegalAccessException e) {
                l.warning(BindingEngineRegistry.class.getName() + ": Default " +
                    "binding engine with class name " + className + " could not " +
                    "be instantiated ", e);
                continue;
            }

            if (!reg.registerBindingEngine(name, engine) )
            {
                l.warning(BindingEngineRegistry.class.getName() + ": Default " +
                    "binding engine with class name " + className + " could not be " +
                    "registered - annother engine had already been registered " +
                    "under name " + name);
                continue;
            }
        }
    }

    public static BindingEngineRegistry getInstance()
    {
        return INSTANCE;
    }

}
