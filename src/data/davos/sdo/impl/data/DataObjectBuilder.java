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
package davos.sdo.impl.data;

import davos.sdo.DataObjectXML;

/**
 * Loads a DataObject, with change summary from a set of XML events that include
 * type information and change information,
 * as opposed to {@link davos.sdo.impl.marshal.SDOEventModel} which loads a document from
 * a set of "pure" XML events, with change information encoded as per the SDO spec.
 * If the root DataObject has a change summary, then the logging for the change summary is on
 * @author radup
 * <b/>Compared to the SDO unmarshaller, the DataObjectLoader doesn't support the following:
 * <ol>
 * <li>xsi:type</li>
 * <li>untyped content; note that open content is still supported, but the properties must exist
 * as global properties, they will not be generated on the fly</li>
 * <li>mixed content</li>
 * <li>references</li>
 * </ol>
 */
public interface DataObjectBuilder
{
    public enum Kind {ATTRIBUTE, ELEMENT, CONTENT}

    public enum Change {SAME, NEW, OLD}

    /**
     * Start element events map to normal XML start element events with the addition of a
     * changeType field to specify whether this element is new in the document, is
     * unmodified or is deleted.
     * @param uri the namespace URI of the XML element
     * @param name the local name of the XML element
     * @param prefix the prefix of the XML element
     * @param changeType the status of this element with respect to change tracking 
     */
    void startElement(String uri, String name, String prefix, Change changeType);

    /**
     * End element events map to normal XML end element events and their sole
     * purpose is to mark XML containment relationships
     */
    void endElement();

    /**
     * Simple content events map to attribute events and text events in normal XML as well
     * as to both start element, end element and text events of an element with a
     * simple Schema Type.<p/>
     * It is important that the <code>value</code> parameter be of the correct Java type
     * as mapped from the Schema type of the element/attribute as converted via SDO. For
     * efficiency, no checks are done and things may fail later.
     * @param uri the namespace URI of the XML element/attribute or null if this event corresponds
     * to the content of an element of a complex type with simple content
     * @param name the local name of the XML element/attribute or null as above
     * @param prefix the prefix of the XML element/attribute or null as above
     * @param value the value of this XML element/attribute/text as a <code>java.lang.Object</code>
     * of the correct type; in case the Schema Type is a List, this will have the type
     * <code>java.util.List</code>. 
     * @param kind the kind of XML entity that this simple value maps to, one of: element,
     * attribute or simple element content. This in conjunction with the <code>uri</code> and the
     * <code>name</code> help identify the correct SDO property
     * @param changeType the change information, whether this value is unmodified, or if it is
     * modified, whether it's the new or old value of the property
     */
    void simpleContent(String uri, String name, String prefix, Object value, Kind kind,
        Change changeType);

    /**
     * Returns the DataObjectXML built based on the set of events received since initialization
     * @return the root DataObject
     */
    DataObjectXML retrieveRootDataObject();

    public static final java.util.List EMPTY_LIST = java.util.Collections.EMPTY_LIST;
}
