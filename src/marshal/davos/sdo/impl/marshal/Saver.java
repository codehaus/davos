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

import java.io.IOException;

import javax.sdo.DataObject;

import davos.sdo.SDOContext;
import davos.sdo.impl.common.NamespaceStack;


public abstract class Saver implements SDOEventModel
{
    protected Marshaller _m;

    Saver()
    {
    }

    /**
     * @param rootObject
     * @param rootElementURI
     * @param rootElementName
     * @param encoding
     * @param options
     */
    public void save(DataObject rootObject,
            String rootElementURI, String rootElementName,
            boolean hasXmlDecl, String xmlVersion, String encoding,
            String schemaLocation, String noNSSchemaLocation,
            Object options, SDOContext sdoctx)
        throws IOException
    {
        _m = Marshaller.get(options);
        _m.setSaver(this);
        _m.marshal(rootObject, rootElementURI, rootElementName,
            hasXmlDecl, xmlVersion, encoding,
            schemaLocation, noNSSchemaLocation, sdoctx);
    }

    abstract NamespaceStack getNamespaceStack();
}
