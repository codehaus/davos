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
package davos.sdo.impl.binaryVersioning;

import java.io.DataOutput;
import java.io.Closeable;
import java.io.OutputStream;
import java.io.IOException;
import java.io.DataOutputStream;
import java.io.ByteArrayOutputStream;
import java.util.Stack;

/**
 * @author Cezar Andrei (cezar dot andrei at gmail dot com)
 *         Date: Aug 21, 2007
 */
public class VersionedDataOutputStream
    implements DataOutput, Closeable
{
    private Stack<DataOutputExtended> _doeStack = new Stack<DataOutputExtended>();

    public VersionedDataOutputStream(OutputStream out)
    {
        _doeStack.push( new DataOutputStreamExtended(out));
    }

    public void startWildcard()
    {
        _doeStack.push( ByteArrayDataOutputExtended.create() );
    }

    public void endWildcard()
        throws IOException
    {
        if (_doeStack.size()<=1)
            throw new IllegalStateException("Too many endWildcard() calls.");

        DataOutputExtended topDOE = _doeStack.pop();

        //System.out.println("  writing Skip " + _doeStack.size() + " : " + topDOE.getSize());

        DataOutputExtended parentDOE = _doeStack.peek();
        parentDOE.writeInt(topDOE.getSize());
        topDOE.writeTo((OutputStream)parentDOE);
    }

    /**
     * When upgrading minor version replace the calling line of this method:
     * <pre>
     *       vdos.writeEmptyWildcard()</pre>
     * with:
     * <pre>
     *       vdos.startWildcard();  // wildcard CODE_VERSION_MINOR_V... version before upgrade
     *       {
     *           // write new data
     *
     *           vdos.writeEmptyWildcard();  // wildcard CODE_VERSION_MINOR_V... version after upgrade
     *       }
     *       vdos.endWildcard();  // end wildcard CODE_VERSION_MINOR_V... version before upgrade
     * </pre>
     * @see davos.sdo.impl.binaryVersioning.Versions
     * @throws IOException
     */
    public void writeEmptyWildcard()
        throws IOException
    {
        startWildcard();
        endWildcard();
    }


    // impl methods for Closeable
    public void close()
        throws IOException
    {
        assert _doeStack.size()>0;
        if (_doeStack.size()>1)
            throw new IllegalStateException("Wildcard not closed.");
        _doeStack.peek().close();
    }

    // impl methods for DataOutput
    public void write(byte[] b)
        throws IOException
    {
        _doeStack.peek().write(b);
    }

    public void write(int b)
        throws IOException
    {
        _doeStack.peek().write(b);
    }

    public void write(byte[] b, int off, int len)
        throws IOException
    {
        _doeStack.peek().write(b, off, len);
    }

    public void writeBoolean(boolean v)
        throws IOException
    {
        _doeStack.peek().writeBoolean(v);
    }

    public void writeByte(int v)
        throws IOException
    {
        _doeStack.peek().writeByte(v);
    }

    public void writeShort(int v)
        throws IOException
    {
        _doeStack.peek().writeShort(v);
    }

    public void writeChar(int v)
        throws IOException
    {
        _doeStack.peek().writeChar(v);
    }

    public void writeInt(int v)
        throws IOException
    {
        _doeStack.peek().writeInt(v);
    }

    public void writeLong(long v)
        throws IOException
    {
        _doeStack.peek().writeLong(v);
    }

    public void writeFloat(float v)
        throws IOException
    {
        _doeStack.peek().writeFloat(v);
    }

    public void writeDouble(double v)
        throws IOException
    {
        _doeStack.peek().writeDouble(v);
    }

    public void writeBytes(String s)
        throws IOException
    {
        _doeStack.peek().writeBytes(s);
    }

    public void writeChars(String s)
        throws IOException
    {
        _doeStack.peek().writeChars(s);
    }

    public void writeUTF(String str)
        throws IOException
    {
        _doeStack.peek().writeUTF(str);
    }


    // utility interfaces and implementation
    public static interface DataOutputExtended
        extends DataOutput, Closeable
    {
        void writeTo(OutputStream out)
            throws IOException;

        int getSize();
    }

    public static class ByteArrayDataOutputExtended
        extends DataOutputStream
        implements DataOutputExtended
    {
        private ByteArrayOutputStream _baos;

        private ByteArrayDataOutputExtended(OutputStream out)
        {
            super(out);
        }

        public static ByteArrayDataOutputExtended create()
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ByteArrayDataOutputExtended result = new ByteArrayDataOutputExtended(baos);
            result._baos = baos;
            return result;
        }

        public void writeTo(OutputStream out)
            throws IOException
        {
            _baos.writeTo(out);
        }

        public int getSize()
        {
            return super.size();
        }
    }

    public static class DataOutputStreamExtended
        extends DataOutputStream
        implements DataOutputExtended
    {
        public DataOutputStreamExtended(OutputStream out)
        {
            super(out);
        }

        public void writeTo(OutputStream out)
            throws IOException
        {
            throw new IllegalStateException("Illegal invocation of DataOutputStreamExtended.writeTo().");
        }

        public int getSize()
        {
            return super.size();
        }
    }
}
