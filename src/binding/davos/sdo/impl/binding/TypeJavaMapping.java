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

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

import davos.sdo.TypeXML;

/**
 * @author ljones
 *         First created: Mar 7, 2006
 */
public class TypeJavaMapping
{
    // member vars
    private Map<TypeXML, JavaClassName> _typeMapping = new HashMap<TypeXML, JavaClassName>();
    private Set<String> _intfcNames = new HashSet<String>();
    private Set<String> _implNames = new HashSet<String>();

    public boolean addMapping(TypeXML t, String fullIntfcName, String fullImplName)
    {
        JavaClassName j = new JavaOuterClassName(fullIntfcName, fullImplName);
        return addMapping(t, j);
    }

    public boolean addMapping(TypeXML t, String intfcPackageName,
        String intfcShortClassName, String implPackageName, String implShortClassName)
    {
        JavaClassName j = new JavaOuterClassName(intfcPackageName,
            intfcShortClassName, implPackageName, implShortClassName);
        return addMapping(t, j);
    }

    public boolean addMapping(TypeXML t, Class intfcImplClass)
    {
        JavaClassName j = new JavaOuterClassName(intfcImplClass);
        return addMapping(t, j);
    }

    public synchronized boolean addMapping(TypeXML t, JavaClassName j)
    {
        _typeMapping.put(t, j);

        String intfcFullName = j.getIntfFullName();
        if (_intfcNames.contains(intfcFullName))
        {
            return false;
        }

        String implFullName = j.getImplFullName();
        if (_implNames.contains(implFullName))
        {
            return false;
        }

        _intfcNames.add(intfcFullName);
        _implNames.add(implFullName);
        return true;
    }


    public JavaClassName getJavaClass(TypeXML t)
    {
        return _typeMapping.get(t);
    }

    public String pickInterfaceFullName(String intfPackageName, String intfShortName)
    {
        String intfName = intfPackageName + "." + intfShortName;
        String candidate = intfName;

        if (!_intfcNames.contains(candidate))
            return candidate;

        for(int i = 1; i<Integer.MAX_VALUE; i++)
        {
            candidate = intfName + i;
            if (!_intfcNames.contains(candidate))
                return candidate;
        }

        throw new RuntimeException("Too many interfaces with the same name: " + intfName);
    }

    public String pickImplementationFullName(String implPackageName, String implShortName)
    {
        String implName = implPackageName + "." + implShortName;
        String candidate = implName;

        if (!_implNames.contains(candidate))
            return candidate;

        for(int i = 1; i<Integer.MAX_VALUE; i++)
        {
            candidate = implName + i;
            if (!_implNames.contains(candidate))
                return candidate;
        }

        throw new RuntimeException("Too many implementation classes with the same name: " + implName);
    }

    public String pickIntfInnerName(JavaClassName outterName, String proposedName)
    {
        String candidate = proposedName;
        if ( checkInnerCandidate(_intfcNames, outterName, candidate, true) )
            return candidate;

        for(int i = 1; i<Integer.MAX_VALUE; i++)
        {
            candidate = proposedName + i;
            if ( checkInnerCandidate(_intfcNames, outterName, candidate, true) )
                return candidate;
        }

        throw new RuntimeException("Too many interfaces with the same name: " + proposedName);
    }

    public String pickImplInnerName(JavaClassName outterName, String proposedName)
    {
        String candidate = proposedName;
        if ( checkInnerCandidate(_implNames, outterName, candidate, false) )
            return candidate;

        for(int i = 1; i<Integer.MAX_VALUE; i++)
        {
            candidate = proposedName + i;
            if ( checkInnerCandidate(_implNames, outterName, candidate, false) )
                return candidate;
        }

        throw new RuntimeException("Too many implementation classes with the same name: " + proposedName);
    }

    private static boolean checkInnerCandidate(Set<String> usedNames, JavaClassName outterName, String candidate, boolean intf)
    {
//        if (usedNames.contains(candidate))
//            return false;

        while (outterName!=null)
        {
            if ( intf )
            {
                if ( outterName.getIntfInnerName().equals(candidate) )
                    return false;
            }
            else // impl
            {
                if ( outterName.getImplInnerName().equals(candidate) )
                    return false;
            }
            outterName = outterName.getOutterJavaName();
        }
        return true;
    }

    public Map<TypeXML, String> getInstanceClassMapping()
    {
        Map<TypeXML, String> saveMaping = new HashMap<TypeXML, String>();
        for(TypeXML t : _typeMapping.keySet())
        {
            saveMaping.put(t, getJavaClass(t).getIntfFullName());
        }
        return saveMaping;
    }

    public Set<TypeXML> getAllTypes()
    {
        return _typeMapping.keySet();
    }
}
