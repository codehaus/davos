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

import javax.sdo.impl.ExternalizableDelegator;
import javax.sdo.DataObject;
import javax.sdo.helper.XMLHelper;
import javax.sdo.helper.XMLDocument;

import java.io.ObjectStreamException;
import java.io.ObjectOutput;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.GZIPInputStream;

import davos.sdo.impl.util.XmlPath;
import davos.sdo.impl.common.Names;
import davos.sdo.DataObjectXML;
import davos.sdo.PropertyXML;
import davos.sdo.SDOContext;
import davos.sdo.SDOContextFactory;

/**
 * @author Cezar Andrei (cezar dot andrei at gmail dot com)
 *         Date: Oct 2, 2006
 */
public class ResolvableImpl
    implements ExternalizableDelegator.Resolvable
{
    private static final byte BYTE_PATH_REQUIRED = 0;
    private static final byte BYTE_ROOT = 1;
    private static final byte BYTE_DATAGRAPH = 2;
    // one of the above
    int _typeOfSerialization;

    private DataObjectImpl _resolvedDataObject;
    private DataGraphImpl  _resolvedDataGraph;

    private ResolvableImpl() {}

    public Object readResolve()
        throws ObjectStreamException
    {
        switch(_typeOfSerialization)
        {
        case BYTE_PATH_REQUIRED:
        case BYTE_ROOT:
            return _resolvedDataObject;
        case BYTE_DATAGRAPH:
            return _resolvedDataGraph;
        default:
            throw new IllegalStateException("Unknown typeOfSerialization " + _typeOfSerialization);
        }
    }

    public void writeExternal(ObjectOutput out)
        throws IOException
    {
//        System.out.print("  -- writeExternal Start for " + _typeOfSerialization + "  " + (_typeOfSerialization==2 ? _resolvableDataGraph ? _resolvedDataObject));

        switch(_typeOfSerialization)
        {
        case BYTE_PATH_REQUIRED:
//            System.out.println(".");
            out.writeByte(BYTE_PATH_REQUIRED);
            String path = XmlPath.getPathForObject(_resolvedDataObject);
            if (path.startsWith("//"))
                path = path.substring(1);
            out.writeUTF(path);
            DataObject root = _resolvedDataObject.getRootObject();
            out.writeObject(root);
            break;

        case BYTE_ROOT:
//            System.out.println(" is root.");
            out.writeByte(BYTE_ROOT);
            GZIPOutputStream gzipper = new GZIPOutputStream(new OutputStreamOverObjectOutput(out));
            PropertyXML prop = (PropertyXML) _resolvedDataObject.getContainmentProperty();
            String uri, name;
            if (prop == null)
            {
                uri = Names.URI_SDO;
                name = Names.SDO_DATAOBJECT;
            }
            else
            {
                uri = prop.getXMLNamespaceURI();
                name = prop.getXMLName();
            }
            XMLHelper.INSTANCE.save(_resolvedDataObject, uri, name, gzipper);
            gzipper.finish();
            break;

        case BYTE_DATAGRAPH:
            out.writeByte(BYTE_DATAGRAPH);
            out.writeObject(_resolvedDataGraph.getRootObject());
            break;

        default:
            throw new IllegalStateException("Unknown typeOfSerialization " + _typeOfSerialization);
        }
//        System.out.println("  -- writeExternal End   for " + _typeOfSerialization + "  " + (_typeOfSerialization==2 ? _resolvableDataGraph ? _resolvedDataObject) + " ");
    }

    public void readExternal(ObjectInput in)
        throws IOException, ClassNotFoundException
    {
        _typeOfSerialization = in.readByte();
//        System.out.print("  -- readExternal Start for " + _typeOfSerialization);
        switch(_typeOfSerialization)
        {
        case BYTE_PATH_REQUIRED:
//            System.out.println(".");
            String path = in.readUTF();

            DataObjectXML docForPath = (DataObjectXML)in.readObject();
            _resolvedDataObject = (DataObjectImpl) XmlPath.getObjectForPath(docForPath, path);
            break;

        case BYTE_ROOT:
//            System.out.println(" is root.");
            SDOContext sdoContext = getSDOContextForDeserialization(in);
            XMLDocument docForRoot = sdoContext.getXMLHelper().load(new GZIPInputStream(new InputStreamOverObjectInput(in)));
            _resolvedDataObject = (DataObjectImpl)docForRoot.getRootObject();
            break;

        case BYTE_DATAGRAPH:
            DataObjectXML docForDG = (DataObjectXML)in.readObject();
            _resolvedDataGraph = (DataGraphImpl)(docForDG.getDataGraph());
            break;

        default:
            throw new IllegalStateException("Unknown typeOfSerialization " + _typeOfSerialization);
        }
//        System.out.println("  -- readExternal End   for " + this + " -> " +
//            (_typeOfSerialization==BYTE_DATAGRAPH ? _resolvedDataGraph : _resolvedDataObject));
    }

    private SDOContext getSDOContextForDeserialization(ObjectInput in)
    {
        SDOContext sdoContext;

        if (in instanceof ObjectInputStreamImpl)
        {
            sdoContext = ((ObjectInputStreamImpl)in).getSDOContext();
            assert sdoContext!=null;

            return  sdoContext;
        }

        sdoContext = SDOContextFactory.getThreadLocalSDOContext();
        if ( sdoContext != null )
        {
            //user set sdoContext is used
            return sdoContext;
        }

        // if user didn't set a ThreadLocal context,
        // a new context is created based on the current thread context classloader.
        sdoContext = SDOContextFactory.createNewSDOContext(Thread.currentThread().getContextClassLoader());
        SDOContextFactory.setThreadLocalSDOContext(sdoContext);

        return sdoContext;
    }

    // impl
    public static ExternalizableDelegator.Resolvable newInstance()
    {
        return new ResolvableImpl();
    }

    public static ExternalizableDelegator.Resolvable newInstance(Object target)
    {
        ResolvableImpl res = new ResolvableImpl();
        assert target instanceof DataObjectImpl || target instanceof DataGraphImpl;
        if (target instanceof DataObjectImpl)
        {
            res._resolvedDataObject = (DataObjectImpl)target;

            DataObject root = res._resolvedDataObject.getRootObject();

            if (root==null || root==res._resolvedDataObject)
                res._typeOfSerialization = BYTE_ROOT;
            else
                res._typeOfSerialization = BYTE_PATH_REQUIRED;
        }
        else if (target instanceof DataGraphImpl)
        {
            res._typeOfSerialization = BYTE_DATAGRAPH;
            res._resolvedDataGraph = (DataGraphImpl)target;
        }
        else
        {
            throw new IllegalArgumentException("Unknown resolvable type for target.");
        }

        return res;
    }

    public static class InputStreamOverObjectInput
        extends InputStream
    {
        private ObjectInput _delegate;

        InputStreamOverObjectInput(ObjectInput in)
        {
            _delegate = in;
        }

        public int read()
            throws IOException
        {
            return _delegate.read();
        }
    }

    public static class OutputStreamOverObjectOutput
        extends OutputStream
    {
        private ObjectOutput _delegate;

        OutputStreamOverObjectOutput(ObjectOutput out)
        {
            _delegate = out;
        }

        public void write(int b)
            throws IOException
        {
            _delegate.write(b);
        }
    }
}
