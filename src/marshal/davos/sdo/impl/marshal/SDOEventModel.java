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
package davos.sdo.impl.marshal;

interface SDOEventModel
{
    public static final int ATTR_XSI = 1;
    public static final String SCHEMA_LOCATION = "schemaLocation";
    public static final String NO_NAMESCAPCE_SCHEMA_LOCATION = "noNamespaceSchemaLocation";

    void startElement(String uri, String name, String prefix, String xsiTypeUri, String xsiTypeName);

    void endElement();

    void attr(String uri, String local, String prefix, String value);

    void xmlns(String prefix, String uri);

    void sattr(int type, String name, String value);

    void text(char[] buff, int off, int cch);

    void text(String s);

    void xmlDecl(String version, String encoding);
}
