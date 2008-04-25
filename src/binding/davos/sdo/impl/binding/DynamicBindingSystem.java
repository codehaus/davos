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
package davos.sdo.impl.binding;

import davos.sdo.binding.BindingSystem;
import davos.sdo.DataObjectXML;
import davos.sdo.TypeXML;
import davos.sdo.PropertyXML;
import davos.sdo.SDOContext;
import davos.sdo.SDOBindingException;
import davos.sdo.SDOError;
import davos.sdo.type.TypeSystem;
import davos.sdo.impl.data.DataObjectGeneral;
import davos.sdo.impl.type.BuiltInTypeSystem;
import davos.sdo.impl.util.PrimitiveCodes;
import javax.sdo.DataGraph;
import javax.sdo.DataObject;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.HashMap;

import org.apache.xmlbeans.ResourceLoader;
import org.apache.xmlbeans.impl.schema.ClassLoaderResourceLoader;

/**
 * @author Cezar Andrei (cezar dot andrei at gmail dot com)
 */
public class DynamicBindingSystem
    extends BindingSystemBase
    implements BindingSystem
{
    // static vars
    public static Class[] EMPTY_CLASS_ARRAY = new Class[]{};
    public static Class[] STRING_CLASS_ARRAY = new Class[]{String.class};
    public static Object[] EMPTY_OBJECT_ARRAY = new Object[]{};

    private Map<TypeXML, Class> _typeMapping = new HashMap<TypeXML, Class>();
    private Map<TypeXML, Constructor> _typeToConstructors = new HashMap<TypeXML, Constructor>();
    private ClassLoader _classLoader;

    public DynamicBindingSystem(SDOContext sdoContext, ClassLoader classLoader, TypeSystem typeSystem)
    {
        super(sdoContext, getResourceLoaderForClassLoader(classLoader), typeSystem);
        _classLoader = classLoader;
    }

    protected DataObjectXML getDataObjectForType(TypeXML typeXML, DataGraph datagraph, DataObjectXML container, PropertyXML containmentProperty)
    {
        DataObjectGeneral d;
        Constructor instClassConstructor = _typeToConstructors.get(typeXML);

        if (instClassConstructor==null)
        {
            d = new DataObjectGeneral();
        }
        else
        {
            try
            {
                d = (DataObjectGeneral) instClassConstructor.newInstance(EMPTY_OBJECT_ARRAY);
            }
            catch (InstantiationException e)
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

        d.init(typeXML == BuiltInTypeSystem.DATAOBJECT ? BuiltInTypeSystem.BEADATAOBJECT : typeXML,
            datagraph, container, containmentProperty);
        d.setSDOContext(_sdoContext);
        return d;
    }

    public void addMapping(TypeXML typeXML, Class instanceClass)
    {
        _typeMapping.put(typeXML, instanceClass);
        try
        {
            if (instanceClass.isPrimitive() || instanceClass==byte[].class
                || instanceClass.isInterface() || Object.class == instanceClass)
                return;

            Constructor constructor;
            if ( typeXML.isDataType() )
                constructor = instanceClass.getConstructor(STRING_CLASS_ARRAY);
            else
                constructor = instanceClass.getConstructor(EMPTY_CLASS_ARRAY);

            constructor.setAccessible(true);
            _typeToConstructors.put(typeXML, constructor);
        }
        catch (NoSuchMethodException e)
        {
                throw new RuntimeException(e);
        }
    }

    public void addMapping(TypeXML typeXML, String fullIntfName, String fullImplName)
    {
        addMapping(typeXML, getClassForName(fullImplName, _classLoader));
    }

    private Class getClassForName(String fullImplName, ClassLoader classLoader)
    {
        Class clasz = PrimitiveCodes.primitiveClassForName(fullImplName);
        if (clasz!=null)
            return clasz;

        try
        {
            return classLoader.loadClass(fullImplName);
        }
        catch (ClassNotFoundException e)
        {
            throw new SDOBindingException(SDOError.messageForCode("binding.annotation.classNotFound", fullImplName), e);
        }
    }

    private static ResourceLoader getResourceLoaderForClassLoader(ClassLoader classLoader)
    {
        return new ClassLoaderResourceLoader(classLoader);
    }

    public String getIntfFullNameForType(TypeXML type)
    {
        if (type.isDataType())
            return type.getInstanceClass().getName();
        else
            return DataObject.class.getName();
    }

    public String getImplFullNameForType(TypeXML type)
    {
        Class instClass = _typeMapping.get(type);
        if (instClass==null)
            instClass = DataObjectGeneral.class;

        return instClass.getName();
    }

    public Class getInstanceClassForType(TypeXML type)
    {
        Class instClass = _typeMapping.get(type);
        if (instClass==null)
            instClass = DataObjectGeneral.class;

        return instClass;
    }

    public Class getInstanceClassForJavaName(String name)
    {
        return getClassForName(name, _classLoader);
    }

    public void dump()
    {
        System.out.println("BindingSystem : " + this.getClass().getName());

        for (TypeXML t : _typeMapping.keySet())
        {
            System.out.println("    type " + t + " -> " + _typeMapping.get(t));
        }
    }
}
