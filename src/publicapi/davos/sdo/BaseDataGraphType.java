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
package davos.sdo;

/*
 * SDO Type: BaseDataGraphType
 *      uri: javax.sdo
 *
 * This class, together with XSDType and ModelsType and their implementations in
 * davos.sdo.impl.data are generated from datagraph.xsd and then modified:
 * - the package name is changed from javax.sdo to davos.sdo so it doesn't
 *   invade the public API space
 * - the implementation package is also changed to davos.sdo.impl.data to reuse
 *   one of our existing implementation packages
 * - the changeSummary methods are deleted for aestethic reasons
 */
public interface BaseDataGraphType
    extends javax.sdo.DataObject
{
    public davos.sdo.ModelsType getModels();

    public void setModels(davos.sdo.ModelsType models);

    public boolean isSetModels();

    public void unsetModels();

    public davos.sdo.ModelsType createModels();

    public davos.sdo.XSDType getXsd();

    public void setXsd(davos.sdo.XSDType xsd);

    public boolean isSetXsd();

    public void unsetXsd();

    public davos.sdo.XSDType createXsd();
}
