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
public class JavaOuterClassName
    implements JavaClassName
{
    // member vars
    private String _intfcPackageName;
    private String _intfcShortClassName;
    private transient String _intfcFullClassName;
    private String _implPackageName;
    private String _implShortClassName;
    private transient String _implFullClassName;
    private boolean _isPrimitive = false;
    private transient String _intfRefName;
    private transient String _implRefName;


    public JavaOuterClassName(String intfcPackageName, String intfcShortName,
                         String implPackageName, String implShortName)
    {
        _intfcPackageName = intfcPackageName;
        _intfcShortClassName = intfcShortName;
        _implPackageName = implPackageName;
        _implShortClassName = implShortName;
    }

    public JavaOuterClassName(String intfcFullName, String implFullName)
    {
        init(intfcFullName, implFullName);
    }

    public JavaOuterClassName(Class primitiveClass)
    {
        assert primitiveClass.isPrimitive() || primitiveClass==byte[].class : "Class " + primitiveClass + " should alway be primitive.";

        if (!primitiveClass.isPrimitive() && primitiveClass!=byte[].class)
            throw new IllegalStateException("Class '" + primitiveClass + "' must be a java primitive or byte[] class.");

        String name = primitiveClass.getSimpleName();
        init(name, name);
        _isPrimitive = true;
    }


    private void init(String intfcFullName, String implFullName)
    {
        int dotIndex = intfcFullName.lastIndexOf('.');
        if (dotIndex < 0)
        {
            _intfcPackageName = null;
            _intfcShortClassName = intfcFullName;
        }
        else
        {
            _intfcPackageName = intfcFullName.substring(0,dotIndex);
            _intfcShortClassName = intfcFullName.substring(dotIndex+1);
        }

        dotIndex = implFullName.lastIndexOf('.');
        if (dotIndex < 0)
        {
            _implPackageName = null;
            _implShortClassName = implFullName;
        }
        else
        {
            _implPackageName = implFullName.substring(0,dotIndex);
            _implShortClassName = implFullName.substring(dotIndex+1);
        }
    }

    public boolean isPrimitive()
    {
        return _isPrimitive;
    }

    public String getIntfPackage()
    {
        return _intfcPackageName;
    }

    public String getIntfShortName()
    {
        return _intfcShortClassName;
    }

    public String getIntfInnerName()
    {
        return _intfcShortClassName;
    }

    public String getIntfFullName()
    {
        if (_intfcFullClassName == null)
        {
            _intfcFullClassName = constructFullName(_intfcPackageName, _intfcShortClassName);
        }

        return _intfcFullClassName;
    }

    public String getIntfReferenceName()
    {
        if (_intfRefName == null)
            _intfRefName = constructRefName(_intfcPackageName, _intfcShortClassName);

        return _intfRefName;
    }

    public String getImplPackage()
    {
        return _implPackageName;
    }

    public String getImplShortName()
    {
        return _implShortClassName;
    }

    public String getImplInnerName()
    {
        return _implShortClassName;
    }

    public String getImplFullName()
    {
        if (_implFullClassName == null)
        {
            _implFullClassName = constructFullName(_implPackageName, _implShortClassName);
        }

        return _implFullClassName;
    }

    public String getImplReferenceName()
    {
        if (_implRefName == null)
            _implRefName = constructRefName(_implPackageName, _implShortClassName);

        return _implRefName;
    }

    // utility methods
    public static String constructFullName(String packageName, String shortClassName)
    {
        if (packageName != null && packageName.length() > 0)
        {
            return packageName + "." + shortClassName;
        }
        else
        {
            return shortClassName;
        }
    }

    public static String constructRefName(String packageName, String shortClassName)
    {
        // Inner classes are referred using just their inner class name
        // Global classes are referred using their full name
        int dollarIndex = shortClassName.lastIndexOf('$');
        if (dollarIndex > 0)
            return shortClassName.substring(dollarIndex + 1);
        else
            return constructFullName(packageName, shortClassName);
    }

    public JavaClassName getOutterJavaName()
    {
        return null;
    }

    public String toString()
    {
        return getIntfFullName() + " | " + getImplFullName();
    }
}
