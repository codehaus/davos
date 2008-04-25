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
import javax.sdo.helper.XMLDocument;

public class XMLDocumentImpl implements XMLDocument
{
    private DataObject _dataObject;
    private String _rootElementURI;
    private String _rootElementName;
    private boolean _xmlDeclaration;
    private String _xmlVersion;
    private String _encoding;
    private String _schemaLocation;
    private String _noNamespaceSchemaLocation;

    public XMLDocumentImpl()
    {
    }

    /**
     * @param dataObject
     * @param rootElementURI
     * @param rootElementName
     */
    public XMLDocumentImpl(DataObject dataObject, String rootElementURI, String rootElementName)
    {
        _dataObject = dataObject;
        _rootElementURI = rootElementURI;
        _rootElementName = rootElementName;
        _xmlDeclaration = true;
        _xmlVersion = "1.0";
        _encoding = "UTF-8";
    }

    public String getEncoding()
    {
        return _encoding;
    }

    public String getNoNamespaceSchemaLocation()
    {
        return _noNamespaceSchemaLocation;
    }

    public String getRootElementName()
    {
        return _rootElementName;
    }

    public String getRootElementURI()
    {
        return _rootElementURI;
    }

    public DataObject getRootObject()
    {
        return _dataObject;
    }
 
    public String getSchemaLocation()
    {
        return _schemaLocation;
    }

    public String getXMLVersion()
    {
        return _xmlVersion;
    }

    public boolean isXMLDeclaration()
    {
        return _xmlDeclaration;
    }

    public void setEncoding(String encoding)
    {
        _encoding = encoding;
    }

    public void setNoNamespaceSchemaLocation(String schemaLocation)
    {
        _noNamespaceSchemaLocation = schemaLocation;
    }

    public void setSchemaLocation(String schemaLocation)
    {
        _schemaLocation = schemaLocation;
    }

    public void setXMLDeclaration(boolean xmlDeclaration)
    {
        _xmlDeclaration = xmlDeclaration;
    }

    public void setXMLVersion(String xmlVersion)
    {
        _xmlVersion = xmlVersion;
    }
    
    void setDataObject(DataObject dataObject)
    {
        _dataObject = dataObject;
    }

    void setRootElementName(String elementName)
    {
        _rootElementName = elementName;
    }

    void setRootElementURI(String elementURI)
    {
        _rootElementURI = elementURI;
    }
}
