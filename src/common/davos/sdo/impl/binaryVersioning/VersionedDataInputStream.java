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

import java.io.DataInputStream;
import java.io.InputStream;
import java.io.IOException;

/**
 * @see davos.sdo.impl.binaryVersioning.Versions
 * @author Cezar Andrei (cezar dot andrei at gmail dot com)
 *         Date: Aug 21, 2007
 */
public class VersionedDataInputStream
        extends DataInputStream
{
    private static int UNSET = Integer.MIN_VALUE;

    private int _fileVersionMinor = UNSET;

    public VersionedDataInputStream(InputStream in)
    {
        super(in);
    }

    public boolean readWildcardAndIsSupportedVersion(int corespondingCodeVersion)
        throws IOException
    {
        assert _fileVersionMinor!=UNSET : "Check must be done only after setFileVersionMinor() is called.";

        int numberOfBytesToSkip = readInt();
        if (_fileVersionMinor<=Versions.CODE_VERSION_MINOR && _fileVersionMinor==corespondingCodeVersion)
        {
            //System.out.println("  Skip for " + corespondingCodeVersion + ": " + numberOfBytesToSkip);

            int skipedBytes = skipBytes(numberOfBytesToSkip);
            if (skipedBytes<numberOfBytesToSkip)
                throw new IllegalStateException("Unexpected end of stream.");

            return false;
        }
        else
            return true;
    }

    /**
     * When upgrading minor version replace the calling line of this method:
     * <pre>
     *       vdos.readSkipWildcard(Versions.CODE_VERSION_MINOR_V... version before upgrade);</pre>
     * with:
     * <pre>
     *       // wildcard CODE_VERSION_MINOR_V... version before upgrade
     *       if (vdis.readWildcardAndIsSupportedVersion(Versions.CODE_VERSION_MINOR_V... version before upgrade))
     *       {
     *           //read data added for Versions.CODE_VERSION_MINOR_V... version after upgrade
     *
     *           // wildcard CODE_VERSION_MINOR_V... version after upgrade
     *           if (vdis.readWildcardAndIsSupportedVersion(Versions.CODE_VERSION_MINOR_V... version after upgrade))
     *           {
     *               //future minor versions
     *           }
     *           // end wildcard CODE_VERSION_MINOR_V... version after upgrade
     *       }
     *       // end wildcard CODE_VERSION_MINOR_V... version before upgrade
     *  </pre>
     * @see davos.sdo.impl.binaryVersioning.Versions
     * @throws IOException
     */
    public void readSkipWildcard(int minorVersionWildcardToBeRead)
        throws IOException
    {
        if (readWildcardAndIsSupportedVersion(minorVersionWildcardToBeRead))
        {
            throw new IllegalStateException("Code minor version : " + Versions.CODE_VERSION_MINOR +
                " should skip wilcard for version " + minorVersionWildcardToBeRead);
        }
    }

//  if everything goes to the plan this should not be used    
//    public int getFileVersionMinor()
//    {
//        return _fileVersionMinor;
//    }

    public void setFileVersionMinor(int fileVersionMinor)
    {
        _fileVersionMinor = fileVersionMinor;
    }
}
