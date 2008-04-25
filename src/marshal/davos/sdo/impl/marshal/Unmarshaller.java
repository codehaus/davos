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

import javax.sdo.DataObject;

import java.util.Map;

import org.xml.sax.Locator;

import davos.sdo.SDOContext;

/**
 *
 */
abstract class Unmarshaller implements SDOEventModel
{
    protected Unmarshaller _link;
    protected Loader _loader;
    protected Locator _locator;
    protected NamespaceHandler _nsHandler;
    protected SDOContext _sdoContext;

    static Unmarshaller get(XMLDocumentImpl root, Object options, SDOContext context)
    {
        // Configure what kind of unmarshaller is needed
        // based on the options passed in
        // For now, we only have one kind
        PlainUnmarshaller pu = new PlainUnmarshaller(root, options, context);
        pu.setReferenceResolver(pu);
        return pu;
    }

    /**
     * This is used to "link" multiple unmarshallers such as they can process
     * different parts of the stream
     *
     * @param linkTo
     */
    public void setLink(Unmarshaller linkTo)
    {
        _link = linkTo;
    }

    /**
     * The handler pushing events to this marshaller
     * @param loader
     */
    public void setLoader(Loader loader)
    {
        _loader = loader;
    }

    /**
     * Sets an <code>org.xml.sax.helpers.NamespaceSupport</code> to be
     * used for resolving QName values
     * @param nsHandler a NamespaceSupport
     */
    public void setNamespaceHandler(NamespaceHandler nsHandler)
    {
        _nsHandler = nsHandler;
    }

    public void setLocator(Locator locator)
    {
        _locator = locator;
    }

    abstract void setReferenceResolver(ReferenceResolver r);

    abstract void finish();

    /**
     * A "root object" provides a piece of context to each unmarshaller
     */
    abstract void setRootObject(DataObject root);

    abstract DataObject getRootObject();

    protected Map<String, String> savePrefixMap()
    {
        return _nsHandler.savePrefixMap();
    }
}
