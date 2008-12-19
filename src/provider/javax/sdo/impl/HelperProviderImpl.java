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
package javax.sdo.impl;

import davos.sdo.impl.data.ResolvableImpl;
import davos.sdo.impl.context.SDOContextImpl;
import davos.sdo.SDOContext;

import javax.sdo.helper.CopyHelper;
import javax.sdo.helper.DataFactory;
import javax.sdo.helper.DataHelper;
import javax.sdo.helper.EqualityHelper;
import javax.sdo.helper.TypeHelper;
import javax.sdo.helper.XMLHelper;
import javax.sdo.helper.XSDHelper;
import javax.sdo.helper.HelperContext;


/**
 * @author Cezar Andrei (cezar dot andrei at gmail dot com)
 *         Date: Jan 13, 2006
 */
public class HelperProviderImpl
    extends HelperProvider
{
    private HelperContext _helperContext;

    HelperProviderImpl ()
    {
        _helperContext = helperContext();
    }

    CopyHelper copyHelper()
    {
        return _helperContext.getCopyHelper();
    }

    DataFactory dataFactory()
    {
        return _helperContext.getDataFactory();
    }

    DataHelper dataHelper()
    {
        return _helperContext.getDataHelper();
    }

    EqualityHelper equalityHelper()
    {
        return _helperContext.getEqualityHelper();
    }

    TypeHelper typeHelper()
    {
        return _helperContext.getTypeHelper();
    }

    XMLHelper xmlHelper()
    {
        return _helperContext.getXMLHelper();
    }

    XSDHelper xsdHelper()
    {
        return _helperContext.getXSDHelper();
    }


    ExternalizableDelegator.Resolvable resolvable()
    {
        // this is called durring java Serialization, first time it starts to deserialize
        return ResolvableImpl.newInstance();
    }

    ExternalizableDelegator.Resolvable resolvable(Object target)
    {
        // this is called durring java Serialization, when ExternalizableDelegator needs a delegate for target object
        return ResolvableImpl.newInstance(target);
    }

    HelperContext helperContext()
    {
        return INSTANCE;
    }

    private static final SDOContext INSTANCE = new SDOContextImpl();
}
