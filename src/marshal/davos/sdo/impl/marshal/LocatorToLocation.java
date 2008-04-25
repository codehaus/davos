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
package davos.sdo.impl.marshal;

import javax.xml.stream.Location;

import org.xml.sax.Locator;

public class LocatorToLocation implements Location
{
    private Locator _l;

    public LocatorToLocation(Locator l)
    {
        _l = l;
    }

    public int getColumnNumber()
    {
        return _l.getColumnNumber();
    }

    public int getLineNumber()
    {
        return _l.getLineNumber();
    }

    public String getPublicId()
    {
        return _l.getPublicId();
    }

    public String getSystemId()
    {
        return _l.getSystemId();
    }

    public int getCharacterOffset()
    {
        return -1;
    }
}
