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
package davos.sdo.util;

import java.io.OutputStream;
import java.io.IOException;

/**
 * Provides a context which can be asked to create new files
 */
public interface Filer
{
    /**
     * Creates a new text file with path pathName and extension ext
     * Encoding is decided by wrapping the OutputStream in a Writer
     * @param pathName - path of the text file to create ('.'s are
     *        replaced by separator chars)
     * @param ext - file extension of the text file to create (null implies no extension)
     * @return
     * @throws IOException - if pathName is null, or if problems creating OutputStream
     */
    public OutputStream createSourceFile(String pathName, String ext) throws IOException;

    /**
     * As for createSourceFile() above using ".java" as the file extension
     * @param pathName
     * @return
     * @throws IOException
     */
    public OutputStream createJavaSourceFile(String pathName) throws IOException;

    /**
     * Creates binary resource. Similar to createSourceFile() above except that the
     * file extension must be inside the resourcePath and '/'s are replaced instead
     * of '.'s.
     * @param resourcePath - path of the binary file to create ('/'s are
     *        replaced by separator chars)
     * @return
     * @throws IOException
     */
    public OutputStream createBinaryFile(String resourcePath) throws IOException;
}
