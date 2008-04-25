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

import davos.sdo.util.Filer;

import java.io.OutputStream;
import java.io.IOException;
import java.io.File;
import java.io.FileOutputStream;

/**
 *
 */
public class DefaultFilerImpl implements Filer
{
    // member vars
    protected File _sourceFilesDir;
    protected File _binaryFilesDir;

    public DefaultFilerImpl(File sourceFilesDir, File binaryFilesDir)
    {
        _sourceFilesDir = sourceFilesDir;
        _binaryFilesDir = binaryFilesDir;
    }

    public DefaultFilerImpl(File filesDir)
    {
        _sourceFilesDir = filesDir;
        _binaryFilesDir = filesDir;
    }

    public OutputStream createSourceFile(String pathName, String extension)
        throws IOException
    {
        if (pathName == null) {
            throw new IOException("Cannot create new source file for null path");
        }

        String path = pathName.replace('.', File.separatorChar) +
             (extension == null ? "" : "." + extension);

        File f = new File(_sourceFilesDir, path);

        // ensure file exists - do not care if it already exists
        if (!f.exists())
        {
            f.getParentFile().mkdirs();
            f.createNewFile();
        }

        return new FileOutputStream(f);
    }

    public OutputStream createJavaSourceFile(String pathName) throws IOException {
        return createSourceFile(pathName, "java");
    }

    public OutputStream createBinaryFile(String resourcePath)
        throws IOException
    {
        if (resourcePath == null) {
            throw new IOException("Cannot create new binary file for null path");
        }

        if (File.separatorChar != '/') {
            resourcePath = resourcePath.replace('/', File.separatorChar);
        }

        File f = new File(_binaryFilesDir, resourcePath);

        // ensure file can be created - do not care if it already exists
        if (!f.exists())
        {
            f.getParentFile().mkdirs();
            f.createNewFile();
        }

        return new FileOutputStream(f);
    }
}
