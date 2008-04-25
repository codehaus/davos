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
import davos.sdo.impl.util.PrimitiveCodes;
import davos.sdo.type.TypeSystem;
import javax.sdo.DataGraph;

import java.util.Map;
import java.util.HashMap;

import org.apache.xmlbeans.ResourceLoader;

/**
 * CompilerBindingSystem is able to read a binding file and keep around
 * the interface and implementation class names, without instantiating classes.
 * @author Cezar Andrei (cezar dot andrei at gmail dot com)
 *         Date: Jul 26, 2006
 */
public class CompileBindingSystem
    extends BindingSystemBase
    implements BindingSystem
{
    private Map<TypeXML, JavaClassName> _typeMapping = new HashMap<TypeXML, JavaClassName>();

    public CompileBindingSystem(ResourceLoader resourceLoader, TypeSystem typeSystem)
    {
        super(null, resourceLoader, typeSystem);
    }

    protected DataObjectXML getDataObjectForType(TypeXML typeXML, DataGraph datagraph, DataObjectXML container,
        PropertyXML containmentProperty)
    {
        throw new IllegalStateException(this.getClass().getName() + " doesn't implement getDataObjectForType(..) method.");
    }

    public String getIntfFullNameForType(TypeXML type)
    {
        JavaClassName jcn = _typeMapping.get(type);
        if (jcn==null)
            throw new IllegalArgumentException("Type '" + type + "' is not available in the current compile binding system.");
        else
            return jcn.getIntfFullName();
    }

    public String getImplFullNameForType(TypeXML type)
    {
        JavaClassName jcn = _typeMapping.get(type);
        if (jcn==null)
            throw new IllegalArgumentException();
        else
            return jcn.getImplFullName();
    }

    public Class getInstanceClassForType(TypeXML type)
    {
        return null;    
    }

    public Class getInstanceClassForJavaName(String name)
    {
        return PrimitiveCodes.primitiveClassForName(name);
    }

    public void addMapping(TypeXML type, Class intfImplName)
    {
        _typeMapping.put(type, new JavaOuterClassName(intfImplName));
    }

    public void addMapping(TypeXML type, String fullIntfName, String fullImplName)
    {
        _typeMapping.put(type, new JavaOuterClassName(fullIntfName, fullImplName));
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
