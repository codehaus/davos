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

import davos.sdo.PropertyXML;
import davos.sdo.DataObjectXML;
import davos.sdo.impl.common.NamespaceStack;
import davos.sdo.impl.common.Common;
import javax.sdo.DataObject;
import javax.sdo.Property;

import java.util.List;

/**
 * @author Cezar Andrei (cezar dot andrei at gmail dot com)
 *         Date Oct 12, 2006
 */
public class XmlPath
{
    public static String PATH_SEPARATOR = "/";
    public static String OPEN_BRACKET   = "[";
    public static String CLOSE_BRACKET  = "]";

    /**
     */
    public static String getPathForObject(DataObjectXML node, DataObject root,
        String rootUri, String rootName, NamespaceStack nsstck)
    {
        if (node.getContainer() == null || node == root)
        {
            // We are at the root
            PropertyXML containerProperty = node.getContainmentPropertyXML();
            if (containerProperty == null)
                return PATH_SEPARATOR + getName(rootUri, rootName, nsstck);
            else
                return PATH_SEPARATOR + getName(containerProperty, nsstck);
        }
        else
        {
            // This node is in the current document
            // Compute its path
            PropertyXML prop = (PropertyXML) node.getContainmentProperty();
            if (prop==null)
                return null;

            StringBuilder sb = new StringBuilder();
            DataObjectXML parent = node.getContainerXML();
            String path = getPathForObject(parent, root, rootUri, rootName, nsstck);
            if (path == null)
                return null;
            sb.append(path);
            sb.append(PATH_SEPARATOR);
            sb.append(getName(prop, nsstck));
            if (prop.isMany())
            {
                List values = parent.getList(prop);
                int i = 0;
                for (; i < values.size(); i++)
                    if (node == values.get(i))
                        break;
                assert i < values.size();
                sb.append(OPEN_BRACKET);
                sb.append(i + 1);
                sb.append(CLOSE_BRACKET);
            }
            return sb.toString();
        }
    }

    public static String getPathForObject(DataObject node)
    {
        if (node.getContainer() == null)
        {
            // We are at the root
            return PATH_SEPARATOR;
        }
        else
        {
            // This node is in the current document
            // Compute its path
            Property prop = node.getContainmentProperty();
            if (prop==null)
                return "";

            StringBuilder sb = new StringBuilder();
            String propName = prop.getName();
            DataObject parent = node.getContainer();
            String path = getPathForObject(parent);
            if (path == null)
                return null;
            sb.append(path);
            sb.append(PATH_SEPARATOR);
            sb.append(propName);
            if (prop.isMany())
            {
                List values = parent.getList(prop);
                int i = 0;
                for (; i < values.size(); i++)
                    if (node == values.get(i))
                        break;
                assert i < values.size();
                sb.append(OPEN_BRACKET);
                sb.append(i + 1);
                sb.append(CLOSE_BRACKET);
            }
            return sb.toString();
        }
    }

    public static DataObject getObjectForPath(DataObject doc, String path)
    {
        return doc.getDataObject(path);
    }

    // TODO(radup) Maybe we need to move this method into Saver and not pass the NamespaceStack around
    public static String getName(PropertyXML prop, NamespaceStack nsstck)
    {
        String uri = prop.getXMLNamespaceURI();
        String name = prop.getXMLName();

        return getName(uri, name, nsstck);
    }

    public static String getName(String uri, String local, NamespaceStack nsstck)
    {
        String prefix = nsstck.ensureMapping(uri, null, false, false);
        if (prefix != null)
            return prefix + ':' + local;
        else
            return local;
    }
}
