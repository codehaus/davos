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

import javax.sdo.Sequence;

/**
 * @author Cezar Andrei (cezar dot andrei at gmail dot com)
 *         Date: Mar 21, 2006
 */
public interface SequenceXML
    extends Sequence
{
    PropertyXML getPropertyXML(int index);

    PropertyXML getSubstitution(int index);

    String getPrefixXML(int index);

    /** Same as boolean add(Property property, Object value); method in javax.sdo.Sequence.
     * @see javax.sdo.Sequence#add(javax.sdo.Property, Object)
     */
    boolean addXML(PropertyXML property, Object value, String prefix, PropertyXML substitution);
}
