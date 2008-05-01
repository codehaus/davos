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
/*
 * SDO Type: BaseDataGraphType
 *      uri: javax.sdo
 * Java interface: sdo.commonj.BaseDataGraphType
 * 
 * Automatically generated - do not modify.
 */
package davos.sdo.impl.data;

public abstract class BaseDataGraphTypeImpl
    extends davos.sdo.impl.data.DataObjectGeneral
    implements davos.sdo.BaseDataGraphType
{
    public static javax.sdo.Type type = javax.sdo.helper.TypeHelper.INSTANCE.getType("commonj.sdo","BaseDataGraphType");

    public davos.sdo.ModelsType getModels()
    {
        return (davos.sdo.ModelsType)get(davos.sdo.impl.data.BaseDataGraphTypeImpl.type.getProperty("models"));
    }

    public void setModels(davos.sdo.ModelsType models)
    {
        set(davos.sdo.impl.data.BaseDataGraphTypeImpl.type.getProperty("models"), models);
    }

    public boolean isSetModels()
    {
        return isSet(davos.sdo.impl.data.BaseDataGraphTypeImpl.type.getProperty("models"));
    }

    public void unsetModels()
    {
        unset(davos.sdo.impl.data.BaseDataGraphTypeImpl.type.getProperty("models"));
    }

    public davos.sdo.ModelsType createModels()
    {
        return (davos.sdo.ModelsType)createDataObject(davos.sdo.impl.data.BaseDataGraphTypeImpl.type.getProperty("models") );
    }

    public davos.sdo.XSDType getXsd()
    {
        return (davos.sdo.XSDType)get(davos.sdo.impl.data.BaseDataGraphTypeImpl.type.getProperty("xsd"));
    }

    public void setXsd(davos.sdo.XSDType xsd)
    {
        set(davos.sdo.impl.data.BaseDataGraphTypeImpl.type.getProperty("xsd"), xsd);
    }

    public boolean isSetXsd()
    {
        return isSet(davos.sdo.impl.data.BaseDataGraphTypeImpl.type.getProperty("xsd"));
    }

    public void unsetXsd()
    {
        unset(davos.sdo.impl.data.BaseDataGraphTypeImpl.type.getProperty("xsd"));
    }

    public davos.sdo.XSDType createXsd()
    {
        return (davos.sdo.XSDType)createDataObject(davos.sdo.impl.data.BaseDataGraphTypeImpl.type.getProperty("xsd") );
    }
}
