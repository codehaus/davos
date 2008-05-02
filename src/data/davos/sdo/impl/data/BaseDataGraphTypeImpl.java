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
    public static String typeUri = "commonj.sdo";
    public static String typeName = "BaseDataGraphType";

    public davos.sdo.ModelsType getModels()
    {
        return (davos.sdo.ModelsType)get( super.getSDOContext().getTypeSystem().getTypeXML(typeUri, typeName).getProperty("models"));
    }

    public void setModels(davos.sdo.ModelsType models)
    {
        set( super.getSDOContext().getTypeSystem().getTypeXML(typeUri, typeName).getProperty("models"), models);
    }

    public boolean isSetModels()
    {
        return isSet( super.getSDOContext().getTypeSystem().getTypeXML(typeUri, typeName).getProperty("models"));
    }

    public void unsetModels()
    {
        unset( super.getSDOContext().getTypeSystem().getTypeXML(typeUri, typeName).getProperty("models"));
    }

    public davos.sdo.ModelsType createModels()
    {
        return (davos.sdo.ModelsType)createDataObject( super.getSDOContext().getTypeSystem().getTypeXML(typeUri, typeName).getProperty("models") );
    }

    public davos.sdo.XSDType getXsd()
    {
        return (davos.sdo.XSDType)get( super.getSDOContext().getTypeSystem().getTypeXML(typeUri, typeName).getProperty("xsd"));
    }

    public void setXsd(davos.sdo.XSDType xsd)
    {
        set( super.getSDOContext().getTypeSystem().getTypeXML(typeUri, typeName).getProperty("xsd"), xsd);
    }

    public boolean isSetXsd()
    {
        return isSet( super.getSDOContext().getTypeSystem().getTypeXML(typeUri, typeName).getProperty("xsd"));
    }

    public void unsetXsd()
    {
        unset( super.getSDOContext().getTypeSystem().getTypeXML(typeUri, typeName).getProperty("xsd"));
    }

    public davos.sdo.XSDType createXsd()
    {
        return (davos.sdo.XSDType)createDataObject( super.getSDOContext().getTypeSystem().getTypeXML(typeUri, typeName).getProperty("xsd") );
    }
}
