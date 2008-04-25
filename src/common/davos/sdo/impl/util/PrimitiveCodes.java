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

import java.util.Map;
import java.util.HashMap;

/**
 * @author Cezar Andrei (cezar dot andrei at gmail dot com)
 *         Date: Feb 2, 2007
 */
public class PrimitiveCodes
{
    public static Byte      ZERO_BYTE       = new Byte((byte)0);
    public static Character ZERO_CHARACTER  = new Character((char)0);
    public static Double    ZERO_DOUBLE     = new Double((double)0);
    public static Float     ZERO_FLOAT      = new Float((float)0);
    public static Integer   ZERO_INTEGER    = new Integer((int)0);
    public static Long      ZERO_LONG       = new Long((long)0);
    public static Short     ZERO_SHORT      = new Short((short)0);

    static private Class[] _primitiveClasses = {Boolean.TYPE, Byte.TYPE, Character.TYPE,
        Short.TYPE, Integer.TYPE, Long.TYPE, Float.TYPE, Double.TYPE, byte[].class};

    private static Map<String, Class> _primitiveClassNameToClass;

    static
    {
        _primitiveClassNameToClass = new HashMap<String, Class>();
        _primitiveClassNameToClass.put("boolean", boolean.class);
        _primitiveClassNameToClass.put("byte", byte.class);
        _primitiveClassNameToClass.put("char", char.class);
        _primitiveClassNameToClass.put("double", double.class);
        _primitiveClassNameToClass.put("float", float.class);
        _primitiveClassNameToClass.put("int", int.class);
        _primitiveClassNameToClass.put("long", long.class);
        _primitiveClassNameToClass.put("short", short.class);
        _primitiveClassNameToClass.put("byte[]", byte[].class);
    }

    public static int codeForPrimitiveClass(String primitiveClassName)
    {
        for (int i = 0; i<_primitiveClasses.length; i++)
        {
            Class p = _primitiveClasses[i];
            if (p.getSimpleName().equals(primitiveClassName))
            {
                return i;
            }
        }

        throw new IllegalStateException("String '" + primitiveClassName + "' is not a primitive class or byte[].");
    }

    public static Class primitiveClassForCode(int code)
    {
        return _primitiveClasses[code];
    }

    public static Class primitiveClassForName(String name)
    {
        return _primitiveClassNameToClass.get(name);
    }
}
