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
public class JavaInnerClassName
    implements JavaClassName
{
    private JavaClassName _outterClassName;
    private String _innerIntfName;
    private String _innerImplName;

    JavaInnerClassName(JavaClassName outterClassName, String innerIntfName, String innerImplName)
    {
        _outterClassName = outterClassName;
        _innerIntfName = innerIntfName;
        _innerImplName = innerImplName;
    }


    public boolean isPrimitive()
    {
        return false;
    }

    public String getIntfPackage()
    {
        return _outterClassName.getIntfPackage();
    }

    public String getIntfShortName()
    {
        return _outterClassName.getIntfShortName() + "$" + _innerIntfName;
    }

    public String getIntfInnerName()
    {
        return _innerIntfName;
    }

    public String getIntfFullName()
    {
        return getIntfPackage() + "." + getIntfShortName();
    }

    public String getIntfReferenceName()
    {
        return _outterClassName.getIntfReferenceName() + "." + _innerIntfName;
    }

    public String getImplPackage()
    {
        return _outterClassName.getImplPackage();
    }

    public String getImplShortName()
    {
        return _outterClassName.getImplShortName() + "$" + _innerImplName;
    }

    public String getImplInnerName()
    {
        return _innerImplName;
    }

    public String getImplFullName()
    {
        return getImplPackage() + "." + getImplShortName();
    }

    public String getImplReferenceName()
    {
        return _outterClassName.getImplReferenceName() + "." + _innerImplName;
    }

    public JavaClassName getOutterJavaName()
    {
        return _outterClassName;
    }

    public String toString()
    {
        return getIntfFullName() + " | " + getImplFullName();
    }
}
