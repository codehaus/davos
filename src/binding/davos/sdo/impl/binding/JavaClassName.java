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
package davos.sdo.impl.binding;

/**
 * @author Cezar Andrei (cezar dot andrei at gmail dot com)
 *         Date: Sep 12, 2007
 */
public interface JavaClassName
{
    /**
     * @return Returns true only for JavaClassNames that represent java primitive types: boolean, char, int, short, long, double
     */
    boolean isPrimitive();

    /**
     * @return Returns the package name. Ex. for davos.sdo.Product$Item$Type returns davos.sdo
     */
    String getIntfPackage();

    /**
     * @return Returns the name without package. Ex. for davos.sdo.Product$Item$Type returns Product$Item$Type
     */
    String getIntfShortName();

    /**
     * @return Returns the name of the inner interface.
     * Ex. for davos.sdo.Product$Item$Type returns Type
     *     for davos.sdo.Product returns Product
     */
    String getIntfInnerName();

    /**
     * @return Returns the full name. Ex. for davos.sdo.Product$Item$Type returns davos.sdo.Product$Item$Type
     */
    String getIntfFullName();

    /**
     * @return Returns the full name used for refence in java sources.
     * Ex. for davos.sdo.Product$Item$Type returns davos.sdo.Product.Item.Type
     */
    String getIntfReferenceName();

    /**
     * @return Returns the package name. Ex. for davos.sdo.impl.ProductImpl$Item$Type returns davos.sdo.impl
     */
    String getImplPackage();

    /**
     * @return Returns the name without package. Ex. for davos.sdo.impl.ProductImpl$Item$Type returns ProductImpl$Item$Type
     */
    String getImplShortName();

    /**
     * @return Returns the name of the inner class.
     * Ex. for davos.sdo.impl.ProductImpl$Item$Type returns Type
     *     for davos.sdo.impl.ProductImpl returns ProductImpl
     */
    String getImplInnerName();

    /**
     * @return Return the full name. Ex. for davos.sdo.impl.ProductImpl$Item$Type returns davos.sdo.impl.ProductImpl$Item$Type
     */
    String getImplFullName();

    /**
     * @return Returns the full name used for refence in java sources.
     * Ex. for davos.sdo.impl.ProductImpl$Item$Type returns davos.sdo.impl.ProductImpl.Item.Type 
     */
    String getImplReferenceName();

    /**
     * @return Returns the JavaClassName for the outter type or null if not available. Ex. for davos.sdo.impl.ProductImpl$Item$Type returns davos.sdo.impl.ProductImpl$Item
     */
    JavaClassName getOutterJavaName();
}
