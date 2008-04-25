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

import davos.sdo.type.TypeSystem;
import davos.sdo.TypeXML;
import davos.sdo.impl.common.Common;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.util.List;

import org.apache.xmlbeans.ResourceLoader;

/**
 * @author Cezar Andrei (cezar dot andrei at gmail dot com)
 *         Date: Apr 28, 2006
 */
public class TypeSystemLoader
    extends TypeSystemBase
    implements TypeSystem
{
    ResourceLoader _resourceLoader;

    private TypeSystemLoader(ResourceLoader resourceLoader)
    {
        if (resourceLoader==null)
            throw new IllegalArgumentException("A non null resourceLoader is required.");
        
        _resourceLoader = resourceLoader;
    }

    public static TypeSystemLoader newInstance(ResourceLoader resourceLoader)
        throws IOException
    {
        return newInstance(resourceLoader, Common.EMPTY_STRING_LIST);
    }

    public static TypeSystemLoader newInstance(ResourceLoader resourceLoader, List<String> typeSystemNames)
        throws IOException
    {
        TypeSystemLoader loader = new TypeSystemLoader(resourceLoader);

        for (String typeSystemName : typeSystemNames)
        {
            loader.loadTypeSystemFromName(resourceLoader, typeSystemName);
        }

        return loader;
    }

    public TypeXML getTypeBySchemaTypeName(String schemaTypeUri, String schemaTypeLocalName)
    {
        TypeXML type = super.getTypeBySchemaTypeName(schemaTypeUri, schemaTypeLocalName);

        if (type==null)
        {
            try
            {
                loadTypeSystemFromSchemaTypeQName(_resourceLoader, new QName(schemaTypeUri, schemaTypeLocalName));
            }
            catch (IOException e)
            {
                // can't read ignore it, maybe add it to failed QNames set
            }
        }

        return super.getTypeBySchemaTypeName(schemaTypeUri, schemaTypeLocalName);
    }
}
