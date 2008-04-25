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

import davos.sdo.SDOContext;

import javax.sdo.DataObject;

abstract class Marshaller
{
    static final String NEWLINE = "\n";
    protected static final int DEFAULT_INDENT_STEP = 4;
    protected static final int MAX_INDENT = 80;
    static final char[] INDENT = new char[MAX_INDENT];

    static
    {
        final int l = NEWLINE.length();
        NEWLINE.getChars(0, l, INDENT, 0);
        for (int i = l; i < MAX_INDENT; i++)
            INDENT[i] = ' ';
    }

    static Marshaller get(Object options)
    {
        // Configure what kind of unmarshaller is needed
        // based on the options passed in
        // For now, we only have one kind
        PlainMarshaller pm = new PlainMarshaller(options);
        pm.setReferenceBuilder(pm);
        return pm;
    }

    abstract void setSaver(Saver e);

    abstract void setReferenceBuilder(ReferenceBuilder b);

    abstract void marshal(DataObject rootObject,
            String rootElementURI, String rootElementName,
            boolean xmlDecl, String xmlVersion, String encoding,
            String schemaLocation, String noNSSchemaLocation, SDOContext sdoctx);

    abstract void setCurrentIndent(int indent);

    protected static final void indent(Saver s, int amount)
    {
        s.text(INDENT, 0, amount);
    }
}
