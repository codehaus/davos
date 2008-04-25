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

/**
 * Writer sample:
<pre>
    private static void writeBinaryFile_V2()
        throws IOException
    {
        int codeVersionMajor = CODE_VERSION_MAJOR;
        int codeVersionMinor = CODE_VERSION_MINOR;

        System.out.println("writeBinaryFile_V2()\n{");
        OutputStream outStream = new FileOutputStream(file);
        VersionedDataOutputStream vdos = new VersionedDataOutputStream(outStream);

        vdos.writeInt(FILE_MAGIC_NUMBER);

        vdos.writeInt(codeVersionMajor);
        vdos.writeInt(codeVersionMinor);

        String textData = "Some text by CODE_VERSION_MINOR_V0";
        int intData = 7;
        vdos.writeUTF(textData);
        vdos.writeInt(intData);
        System.out.println("  writing: " + textData + " " + intData);



        vdos.startWildcard();  // wildcard CODE_VERSION_MINOR_V0
        {

            textData = "Some more text by CODE_VERSION_MINOR_V1";
            intData = 5;
            vdos.writeUTF(textData);
            vdos.writeInt(intData);
            System.out.println("  writing: " + textData + " " + intData);


            vdos.startWildcard();  // wildcard CODE_VERSION_MINOR_V1
            {

                textData = "Even more text by CODE_VERSION_MINOR_V2";
                intData = 11;
                vdos.writeUTF(textData);
                vdos.writeInt(intData);
                System.out.println("  writing: " + textData + " " + intData);


                vdos.startWildcard();  // wildcard CODE_VERSION_MINOR_V2
                {
                }
                vdos.endWildcard();  // end wildcard CODE_VERSION_MINOR_V2


            }
            vdos.endWildcard();  // end wildcard CODE_VERSION_MINOR_V1


        }
        vdos.endWildcard();  // end wildcard CODE_VERSION_MINOR_V0


        // write the rest of data
        vdos.writeUTF("END");
        System.out.println("  writing: END");

        vdos.close();
        System.out.println("}   //  writeBinaryFile_V2()\n");
    }
 </pre>
 * Reader sample:
 * <pre>
    private static void readBinaryFile_V2()
        throws IOException
    {
        int codeVersionMajor = CODE_VERSION_MAJOR;
        int codeVersionMinor = CODE_VERSION_MINOR;

        System.out.println("readBinaryFile_V2()\n{");
        InputStream inStream = new FileInputStream(file);
        VersionedDataInputStream vdis = new VersionedDataInputStream(inStream);


        int magicNumber = vdis.readInt();
        if (magicNumber!=FILE_MAGIC_NUMBER)
            throw new IllegalStateException("Wrong magic number.");


        int fileVersionMajor = vdis.readInt();
        if (fileVersionMajor>codeVersionMajor)
            throw new IllegalStateException("Incompatible versions: Read ver: " + codeVersionMajor +
                " found file ver: " + fileVersionMajor + "." + fileVersionMinor);

        int fileVersionMinor = vdis.readInt();
        vdis.setFileVersionMinor(fileVersionMinor);

        String textData = vdis.readUTF();
        int intData = vdis.readInt();
        System.out.println("  read: " + textData + " " + intData);

        // wildcard CODE_VERSION_MINOR_V0
        if (vdis.readWildcardAndIsSupportedVersion(CODE_VERSION_MINOR_V0))
        {
            textData = vdis.readUTF();
            intData = vdis.readInt();
            System.out.println("  read: " + textData + " " + intData);

            // wildcard CODE_VERSION_MINOR_V1
            if (vdis.readWildcardAndIsSupportedVersion(CODE_VERSION_MINOR_V1))
            {
                textData = vdis.readUTF();
                intData = vdis.readInt();
                System.out.println("  read: " + textData + " " + intData);

                // wildcard CODE_VERSION_MINOR_V2
                if (vdis.readWildcardAndIsSupportedVersion(CODE_VERSION_MINOR_V2))
                {
                    //future minor versions
                }
                // end wildcard CODE_VERSION_MINOR_V2
            }
            // end wildcard CODE_VERSION_MINOR_V1
        }
        // end wildcard CODE_VERSION_MINOR_V0

        String end = vdis.readUTF();
        System.out.println("  read: " + end);

        vdis.close();
        System.out.println("}   //  readBinaryFile_V2()\n");
    }
 * </pre>
 * @author Cezar Andrei (cezar dot andrei at gmail dot com)
 *         Date: Aug 23, 2007
 */
public class Versions
{
    public static int CODE_VERSION_MINOR_V0 = 0;
    //public static int CODE_VERSION_MINOR_V1 = 1;
    //public static int CODE_VERSION_MINOR_V2 = 2;

    public static int CODE_VERSION_MAJOR = 4;
    public static int CODE_VERSION_MINOR = CODE_VERSION_MINOR_V0;

    // .sdotsb FILE constants
    public static final int FILE_MAGIC_NUMBER = 0xDA7A15D0;

    public static final int FILE_TYPE_ID = 1;
    public static final int FILE_TYPE_TYPESYSTEM = 2;
    public static final int FILE_TYPE_BINDSYSTEM = 3;
}
