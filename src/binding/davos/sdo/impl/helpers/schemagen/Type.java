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

import javax.xml.namespace.QName;
import java.util.List;

public class Type extends org.apache.xmlbeans.impl.inst2xsd.util.Type
{
    static final int DT_EXTENSION = 1;
    static final int DT_RESTRICTION = 2;
    static final int DT_LIST = 3;

    private boolean abstrac;
    private boolean open;
    private String baseTypeUri;
    private String baseTypeName;
    private int derivation;
    private List aliasNames;
    private String instanceClass;
    private String dataTypeUri;
    private String dataTypeName;
    private boolean openContent;

    Type(String typeUri, String typeName)
    {
        super();
        super.setName(new QName(typeUri, typeName));
    }

    Type()
    {
        // Anonymous type
        super();
    }

    public boolean isAbstract()
    {
        return abstrac;
    }

    public void setAbstract(boolean abstrac)
    {
        this.abstrac = abstrac;
    }

    public boolean isOpen()
    {
        return open;
    }

    public void setOpen(boolean open)
    {
        this.open = open;
    }

    public String getBaseTypeUri()
    {
        return baseTypeUri;
    }

    public String getBaseTypeName()
    {
        return baseTypeName;
    }

    public void setBaseType(String baseTypeUri, String baseTypeName)
    {
        this.baseTypeUri = baseTypeUri;
        this.baseTypeName = baseTypeName;
    }

    public int getDerivation()
    {
        return derivation;
    }

    public void setDerivation(int derivation)
    {
        this.derivation = derivation;
    }

    public List getAliasNames()
    {
        return aliasNames;
    }

    public void setAliasNames(List aliasNames)
    {
        this.aliasNames = aliasNames;
    }

    public String getInstanceClass()
    {
        return instanceClass;
    }

    public void setInstanceClass(String instanceClass)
    {
        this.instanceClass = instanceClass;
    }

    public boolean isOpenContent()
    {
        return openContent;
    }

    public void setOpenContent(boolean openContent)
    {
        this.openContent = openContent;
    }
}
