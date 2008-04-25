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
package davos.sdo.impl.helpers.schemagen;

import java.util.List;

public class Element extends org.apache.xmlbeans.impl.inst2xsd.util.Element
    implements SdoProperty
{
    private List aliasNames;
    private String sdoName;
    private boolean readOnly;
    private String opposite;
    private String propertyTypeUri;
    private String propertyTypeName;
    private String dataTypeUri;
    private String dataTypeName;
    private String def;
    private boolean hasMany;

    public List getAliasNames()
    {
        return aliasNames;
    }

    public void setAliasNames(List aliasNames)
    {
        this.aliasNames = aliasNames;
    }

    public String getSdoName()
    {
        return sdoName;
    }

    public void setSdoName(String sdoName)
    {
        this.sdoName = sdoName;
    }

    public boolean isReadOnly()
    {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly)
    {
        this.readOnly = readOnly;
    }

    public String getOpposite()
    {
        return opposite;
    }

    public void setOpposite(String opposite)
    {
        this.opposite = opposite;
    }

    public String getPropertyTypeUri()
    {
        return propertyTypeUri;
    }

    public String getPropertyTypeName()
    {
        return propertyTypeName;
    }

    public void setPropertyType(String propertyTypeUri, String propertyTypeName)
    {
        this.propertyTypeUri = propertyTypeUri;
        this.propertyTypeName = propertyTypeName;
    }

    public String getDataTypeUri()
    {
        return dataTypeUri;
    }

    public String getDataTypeName()
    {
        return dataTypeName;
    }

    public void setDataType(String dataTypeUri, String dataTypeName)
    {
        this.dataTypeUri = dataTypeUri;
        this.dataTypeName = dataTypeName;
    }

    public String getDefault()
    {
        return def;
    }

    public void setDefault(String def)
    {
        this.def = def;
    }

    public boolean isHasMany()
    {
        return hasMany;
    }

    public void setHasMany(boolean hasMany)
    {
        this.hasMany = hasMany;
    }
}
