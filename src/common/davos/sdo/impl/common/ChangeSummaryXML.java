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
package davos.sdo.impl.common;

import davos.sdo.PropertyXML;

import javax.xml.namespace.QName;
import java.util.Iterator;

/**
 * Created
 * Date: Jan 13, 2007
 * Time: 4:06:04 PM
 */
public interface ChangeSummaryXML
{
    Iterator<ChangedObjectXML> getChangedObjectsIterator();

    public interface ChangedObjectXML
    {
        QName getElementName();

        Iterator<ChangeXML> getChangesIterator();
    }

    public interface ChangeXML
    {
        QName getQName();

        boolean isElement();

        PropertyXML getProperty();

        Object getResolvedValue();
    }
}
