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
package davos.sdo.impl.data;

import davos.sdo.impl.type.TypeImpl;
import davos.sdo.impl.type.BuiltInTypeSystem;
import davos.sdo.TypeXML;
import davos.sdo.SDOContext;
import javax.sdo.ChangeSummary;
import javax.sdo.DataGraph;
import javax.sdo.DataObject;
import javax.sdo.Type;
import javax.sdo.impl.ExternalizableDelegator;

import java.io.ObjectStreamException;

/**
 * @author Cezar Andrei (cezar dot andrei at gmail dot com)
 *         Date: Jan 6, 2006
 */
public class DataGraphImpl
    implements DataGraph
{
    private SDOContext _sdoContext;
    private DataObject _root;
    private ChangeSummary _changeSummary;
    //todo add a pointer to it's DataBinding object

    DataGraphImpl(SDOContext sdoContext)
    {
        _sdoContext = sdoContext;
        _changeSummary = new ChangeSummaryImpl(this);
    }

    public DataObject getRootObject()
    {
        return _root;
    }

    public void setRootObject(DataObject root)
    {
        if (root.getType() != BuiltInTypeSystem.DATAGRAPHTYPE)
            throw new IllegalArgumentException("The root object of a DataGraph has to have " +
                "the type {" + BuiltInTypeSystem.DATAGRAPHTYPE.getURI() + '}' +
                BuiltInTypeSystem.DATAGRAPHTYPE.getName());
        _root = root;
        _changeSummary = root.getChangeSummary();
    }

    public ChangeSummary getChangeSummary()
    {
        return _changeSummary;
    }

    public Type getType(String uri, String typeName)
    {
        return _sdoContext.getBindingSystem().loadTypeByTypeName(uri, typeName);
    }

    public DataObject createRootObject(String namespaceURI, String typeName)
    {
        if (_root != null)
            throw new IllegalArgumentException("Root object already exists");

        TypeXML type = _sdoContext.getBindingSystem().loadTypeByTypeName(namespaceURI, typeName);
        DataObject root = _sdoContext.getBindingSystem().
            createDataObjectForType(_sdoContext.getTypeSystem().getTypeXML(type), this);

        setRootObject(root);
        return root;
    }

    public DataObject createRootObject(Type type)
    {
        if (_root != null)
            throw new IllegalArgumentException("Root object already exists");

        DataObject root = _sdoContext.getBindingSystem().
            createDataObjectForType(_sdoContext.getTypeSystem().getTypeXML(type), this);
        setRootObject(root);
        return root;
    }

    // java Serialization

    // java Serialization will first call this method which will replace the object to be persisted with
    // an ExternalizableDelegator object that delegates to a ResolvableImpl object
    protected Object writeReplace()
        throws ObjectStreamException
    {
        return new ExternalizableDelegator(this);
    }
}
