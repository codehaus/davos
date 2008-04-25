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
package davos.sdo.impl.type;

import davos.sdo.TypeXML;
import davos.sdo.PropertyXML;
import davos.sdo.type.TypeSystem;
import org.apache.xmlbeans.SchemaTypeLoader;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.sdo.Type;

/**
 * @author Cezar Andrei (cezar dot andrei at gmail dot com)
 *         Date: Jan 13, 2006
 */
public class TypeSystemUnion
    implements TypeSystem
{
    private List<TypeSystem> _typeSystems;

    private TypeSystemUnion(){}

    public static TypeSystem union(List<TypeSystem> typeSystems)
    {
        TypeSystemUnion tsu = new TypeSystemUnion();
        tsu._typeSystems = new ArrayList<TypeSystem>(typeSystems.size());
        tsu._typeSystems.addAll(typeSystems);
        return tsu;
    }

    public static TypeSystem union(TypeSystem primary, TypeSystem secondary)
    {
        TypeSystemUnion tsu = new TypeSystemUnion();
        tsu._typeSystems = new ArrayList<TypeSystem>(2);
        tsu._typeSystems.add(primary);
        tsu._typeSystems.add(secondary);
        return tsu;
    }

    public TypeXML getTypeXML(String uri, String typeName)
    {
        for (int i = 0; i < _typeSystems.size(); i++)
        {
            TypeSystem typeSystem = (TypeSystem) _typeSystems.get(i);
            TypeXML type = typeSystem.getTypeXML(uri, typeName);
            if (type!=null)
                return type;
        }
        return null;
    }

    public TypeXML getTypeXML(Type type)
    {
        throw new IllegalStateException(this.getClass() + " does not support this method.");
    }

    public Set<TypeXML> getAllTypes()
    {
        Set<TypeXML> types = new HashSet<TypeXML>();

        for (TypeSystem typeSystem : _typeSystems)
        {
            types.addAll(typeSystem.getAllTypes());
        }

        return types;
    }

    public TypeXML getTypeBySchemaTypeName(String uri, String localName)
    {
        for (int i = 0; i < _typeSystems.size(); i++)
        {
            TypeSystem typeSystem = (TypeSystem) _typeSystems.get(i);
            TypeXML type = typeSystem.getTypeBySchemaTypeName(uri, localName);
            if (type!=null)
                return type;
        }
        return null;
    }

    public PropertyXML getGlobalPropertyByTopLevelElemQName(String uri, String elemName)
    {
        for (int i = 0; i < _typeSystems.size(); i++)
        {
            TypeSystem typeSystem = (TypeSystem) _typeSystems.get(i);
            PropertyXML elemGlobalProp = typeSystem.getGlobalPropertyByTopLevelElemQName(uri, elemName);
            if (elemGlobalProp!=null)
                return elemGlobalProp;
        }
        return null;
    }

    public PropertyXML getGlobalPropertyByTopLevelAttrQName(String uri, String attrName)
    {
        for (int i = 0; i < _typeSystems.size(); i++)
        {
            TypeSystem typeSystem = (TypeSystem) _typeSystems.get(i);
            PropertyXML attrGlobalProp = typeSystem.getGlobalPropertyByTopLevelAttrQName(uri, attrName);
            if (attrGlobalProp!=null)
                return attrGlobalProp;
        }
        return null;
    }

    public SchemaTypeLoader getSchemaTypeLoader()
    {
        // should a type system union be created?
        return _typeSystems.get(0).getSchemaTypeLoader();
    }
}