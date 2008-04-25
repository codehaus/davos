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

import java.io.PrintWriter;

/**
 * @author ljones
 *         First created: Mar 6, 2006
 */
public abstract class CodePrinter
{
    /**
     * represents an arg in an arg list (the arg type and the arg name)
     */
    public static final class NamedArg
    {
        // member vars
        private String _type;
        private String _name;

        public NamedArg(String argType, String argName)
        {
            _type = argType;
            _name = argName;
        }

        public String getType() { return _type; }
        public String getName() { return _name; }
    }

    // static vars
    public final int defaultIndentIncrement = 4;

    // member vars
    protected PrintWriter _output;
    protected int _indent = 0; // indent as # of spaces
    protected int _currIndentIncrement = defaultIndentIncrement;


    public CodePrinter(PrintWriter output)
    {
        _output = output;
    }

    public CodePrinter(PrintWriter output, int indentIncrement)
    {
        _output = output;
        _currIndentIncrement = indentIncrement;
    }

    public void setIndentIncrement(int indentIncr)
    {
        _currIndentIncrement = indentIncr;
    }

    /**
     * indent by current indent increment amount
     */
    public void indent()
    {
        _indent += _currIndentIncrement;
    }

    /**
     * outdent by current indent increment amount
     */
    public void outdent()
    {
        _indent -= _currIndentIncrement;
        if (_indent < 0)
        {
            _indent = 0;
        }
    }



    public void emitNewLine()
    {
        _output.println();
    }

    public void emitIndent()
    {
        for (int i=0; i<_indent; i++)
        {
            _output.print(' ');
        }
    }

    public void emitString(String s)
    {
        emitIndent();
        _output.print(s);
    }
}
