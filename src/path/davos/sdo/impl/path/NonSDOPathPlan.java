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
package davos.sdo.impl.path;

/**
 * @author Cezar Andrei (cezar dot andrei at gmail dot com)
 *         Date: Jul 7, 2006
 */
public class NonSDOPathPlan
    implements PathPlan
{
    private String _scheme;
    private CharSequence _path;

    public NonSDOPathPlan(String scheme, CharSequence path)
    {
        _scheme = scheme;
        _path = path;
    }

    public boolean execute(Path context)
    {
        //todo
        throw new RuntimeException("NYI : " + _scheme + ": " + _path);
    }

    public void optimize()
    {
        //todo
    }

    public boolean isSimplePropertyName()
    {
        return false;
    }

    public String getSimplePropertyName()
    {
        return null;
    }

    public String toString()
    {
        return _scheme + ":" + _path;
    }
}
