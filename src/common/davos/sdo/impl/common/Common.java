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
package davos.sdo.impl.common;

import javax.sdo.Property;
import javax.sdo.Type;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.xmlbeans.SchemaType;

import davos.sdo.TypeXML;
import davos.sdo.PropertyXML;

/**
 * @author Cezar Andrei (cezar dot andrei at gmail dot com)
 *         Date: Jan 13, 2006
 */
public class Common
{
    public static List<String> EMPTY_STRING_LIST = Collections.EMPTY_LIST;
    public static List<Property> EMPTY_PROPERTY_LIST = Collections.EMPTY_LIST;
    public static List<Type> EMPTY_TYPE_LIST = Collections.EMPTY_LIST;
    public static List<PropertyXML> EMPTY_PROPERTYXML_LIST = Collections.EMPTY_LIST;
    public static List<TypeXML> EMPTY_TYPEXML_LIST = Collections.EMPTY_LIST;
    public static final String EMPTY_STRING = "".intern();
    public static Map<Property, Object> EMPTY_PROPERTY_OBJECT_MAP = Collections.EMPTY_MAP;

    public static PropertyXML getProperty(Type type, int propertyIndex)
    {
        return (PropertyXML)type.getProperties().get(propertyIndex);
    }

    public static int getBuiltinTypeCode(SchemaType type)
    {
        int code = type.getBuiltinTypeCode();
        while (code == SchemaType.BTC_NOT_BUILTIN && type != null)
        {
            if (type.getListItemType() != null)
                type = type.getListItemType();
            else
                type = type.getBaseType();
            code = type.getBuiltinTypeCode();
        }
        return code;
    }

    public static Class unwrapClass(Class javaClass)
    {
        // Convert the java class for the wrapper types to the corresponding primitive
        if (javaClass == Byte.class)
            javaClass = Byte.TYPE;
        else if (javaClass == Boolean.class)
            javaClass = Boolean.TYPE;
        else if (javaClass == Short.class)
            javaClass = Short.TYPE;
        else if (javaClass == Integer.class)
            javaClass = Integer.TYPE;
        else if (javaClass == Long.class)
            javaClass = Long.TYPE;
        else if (javaClass == Float.class)
            javaClass = Float.TYPE;
        else if (javaClass == Double.class)
            javaClass = Double.TYPE;
        else if (javaClass == Character.class)
            javaClass = Character.TYPE;
        else if (javaClass == java.util.ArrayList.class)
            javaClass = java.util.List.class;
        return javaClass;
    }

    public static Class wrapClass(Class javaClass)
    {
        if (!javaClass.isPrimitive())
            return javaClass;
        if (javaClass == Byte.TYPE)
            javaClass = Byte.class;
        else if (javaClass == Boolean.TYPE)
            javaClass = Boolean.class;
        else if (javaClass == Short.TYPE)
            javaClass = Short.class;
        else if (javaClass == Integer.TYPE)
            javaClass = Integer.class;
        else if (javaClass == Long.TYPE)
            javaClass = Long.class;
        else if (javaClass == Float.TYPE)
            javaClass = Float.class;
        else if (javaClass == Double.TYPE)
            javaClass = Double.class;
        else if (javaClass == Character.TYPE)
            javaClass = Character.class;
        else
            throw new IllegalStateException();
        return javaClass;
    }
}
