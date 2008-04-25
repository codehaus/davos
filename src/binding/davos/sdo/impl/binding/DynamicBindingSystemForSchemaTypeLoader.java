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

import java.util.HashMap;
import java.util.Map;

import davos.sdo.SDOContext;
import davos.sdo.TypeXML;
import davos.sdo.Options;
import davos.sdo.type.TypeSystem;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.SchemaGlobalElement;
import org.apache.xmlbeans.SchemaTypeSystem;
import org.apache.xmlbeans.SchemaGlobalAttribute;
import org.apache.xmlbeans.SchemaType;

import javax.xml.namespace.QName;

/**
 * @author Cezar Andrei (cezar dot andrei at gmail dot com)
 *         Date: Mar 9, 2007
 */
public class DynamicBindingSystemForSchemaTypeLoader
    extends DynamicBindingSystem
{
    SchemaTypeLoader _schemaTypeLoader;
    Object _options;

    public DynamicBindingSystemForSchemaTypeLoader(SDOContext sdoContext, ClassLoader classLoader,
        TypeSystem typeSystem)
    {
        super(sdoContext, classLoader, typeSystem);
    }

    public void setSchemaTypeLoader(SchemaTypeLoader schemaTypeLoader)
    {
        _schemaTypeLoader = schemaTypeLoader;
    }

    public void setOptions(Object options)
    {
        _options = options;
    }

    protected boolean loadFromTopLevelElemQName(String uri, String localName)
    {
        SchemaGlobalElement sge = _schemaTypeLoader.findElement(new QName(uri, localName));

        if (sge==null)
            return false;

        return defineSchemaTypeSystem(sge.getTypeSystem());
    }

    protected boolean loadFromTopLevelAttrQName(String uri, String localName)
    {
        SchemaGlobalAttribute sga = _schemaTypeLoader.findAttribute(new QName(uri, localName));

        if (sga==null)
            return false;

        return defineSchemaTypeSystem(sga.getTypeSystem());
    }

    protected boolean loadFromSchemaTypeName(String uri, String localName)
    {
        SchemaType schemaType = _schemaTypeLoader.findType(new QName(uri, localName));

        if (schemaType==null)
            return false;

        return defineSchemaTypeSystem(schemaType.getTypeSystem());
    }

    private boolean defineSchemaTypeSystem(SchemaTypeSystem schemaTypeSystem)
    {
        // We can't pass _options straight in, like would be nice
        // We need to pass a "special" flavor of the COMPILE_SKIP_IF_KNOWN flag
        Map map = null;
        if (_options instanceof Map)
            map = (Map) _options;
        else if (_options instanceof Options)
            map = ((Options) _options).getMap();
        if (map != null && map.containsKey(Options.COMPILE_SKIP_IF_KNOWN))
            map.put(Options.COMPILE_SKIP_IF_KNOWN, Integer.valueOf(2));
        TypeSystem ts = Schema2SDO.createSDOTypeSystem(schemaTypeSystem, this, new HashMap<TypeXML, String>(),
            new HashMap<TypeXML, String>(), null, _options);
        ((davos.sdo.impl.type.TypeSystemBase) _sdoContext.getTypeSystem()).addTypeSystem(ts, false);
        return true;
    }
}
