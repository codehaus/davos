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

import org.apache.xmlbeans.impl.common.XMLChar;

import java.util.List;
import java.util.ArrayList;
import java.math.BigDecimal;

import davos.sdo.SDOError;

/**
 * @author Cezar Andrei (cezar dot andrei at gmail dot com)
 *         Date: Jun 26, 2006
 */
public class Parser
{
    public static void main(String[] args)
        throws SDOPathException
    {
        Parser p = new Parser();
        p.parse("a");
        p.parse("abc");
        p.parse("/a");
        p.parse("@a");
        p.parse("@abc124");
        p.parse("a[1]");
        p.parse("a[123]");
        p.parse("a.0");
        p.parse("a.078");
        p.parse("a[b='a']");
        p.parse("a[b=\"abc\"]");
        p.parse("a[b=1]");
        p.parse("a[b=123]");
        p.parse("a[b=1.]");
        p.parse("a[b=1.2]");
        p.parse("a[b=.1]");
        p.parse("a[b=.123]");
        p.parse("a[b=true]");
        p.parse("a[b=false]");
        p.parse("..");
        p.parse("../..");
        p.parse("a.0/../b/c[d=false]");
        p.parse("a.078/../../..");
        p.parse("@a/..");
        p.parse("@a/b.33/c[4]/d[e=4]/..");
        p.parse("sdo::a");
        p.parse("sdo::/abc");
        p.parse("sdo::@a");
        p.parse("sdo::/a.0");
        p.parse("sdo::/a[5]");
        p.parse("sdo::/a[b='c']/..");
        p.parse("sdo::/../../../../..");
        p.parse("sdo::/a/b/c/..");
        p.parse("sdo::../..");
        p.parse("sdo::/../..");
        p.parse("wer::ewrzdssdfs");
    }

    private CharSequence _expression;
    private int _length;
    private int _index;
    private SDOPathPlan _plan;

    private void init(CharSequence exp)
    {
        _expression = exp;
        _length = exp.length();
        _index = -1;
        _plan = new SDOPathPlan();
    }

    private boolean hasNextChar()
    {
        return _index+1 < _length;
    }

    private char nextChar()
    {
        return _expression.charAt(++_index);
    }

    private char currentChar()
    {
        return _expression.charAt(_index);
    }

    /**
     * <br/> SDOPath grammar:
     * <br/> path         = (scheme ':' {1} )? '/'? {2} (step {9} '/')* step {5}
     * <br/> scheme       = NCName ':' {4}
     * <br/> step         = '@'? {3} property
     * <br/>                | property '[' {7} index_from_1 ']'
     * <br/>                | property '.' {8} index_from_0
     * <br/>                | reference '[' {7} attribute '=' {13} value ']'
     * <br/>                | ".."
     * <br/> property     = NCName          ;; may be simple or complex type
     * <br/> attribute    = NCName          ;; must be simple type
     * <br/> reference    = NCName          ;; must be DataObject type
     * <br/> index_from_0 = digits
     * <br/> index_from_1 = [1-9] {11} [0-9]*
     * <br/> value        = literal | number | boolean
     * <br/> literal      = '"' {14} [^"] '"' | "'" {15} [^'] "'"
     * <br/> number       = digits ('.' {19} digits?)? | '.' {20} Digits
     * <br/> boolean      = true | false
     * <br/> digits       = [0-9]+
     * <br/>
     * <br/> ;; leading '/' begins at the root
     * <br/> ;; ".." is the containing DataObject, using containment properties
     * <br/> ;; only the last step have an attribute as the property
     * <br/> ;; between braces {} is represented the state used below
     */
    PathPlan parse(CharSequence exp)
        throws SDOPathException
    {
        init(exp);
        char c;
        int state = 0;
        state = 0;
        StringBuffer buf = new StringBuffer();
        String property = null;
        String attribute = null;

        //System.out.println("Parsing: " + exp);

        while(true)
        {
            if (!hasNextChar())
            {
                if (state == 6 || state == 10)
                    addPropertyStep(buf);
                else if ( state == 9 )
                {
                }
                else if (state == 16)
                    addIndex0Step(property, buf);
                else
                    throw new SDOPathException(SDOError.messageForCode("path.parse.unexpectedEnd"));

                state = 5;
                break;
            }

            c = nextChar();

            switch(state)
            {
            case 0:         // starting state
                switch (c)
                {
                case '/':
                    addRootStep();
                    state = 2;
                    break;
                case '@':
                    state = 3;
                    break;
                case '.':
                    if (nextChar()=='.')
                    {
                        addParentStep();
                        state = 9;
                        break;
                    }
                    else
                        throw new SDOPathException(SDOError.messageForCode("path.parse.expectingSecondDot", c));
                default:
                    if (XMLChar.isNCNameStart(c))
                    {
                        state = 6;
                        assert buf.length()==0;
                        buf.append(c);
                    }
                    else
                        throw new SDOPathException(SDOError.messageForCode("path.parse.expecting3", '/', '@', "NCNameStart", c));
                }
                break;

            case 1:         // after scheme::
                switch (c)
                {
                case '/':
                    addRootStep();
                    state = 2;
                    break;
                case '@':
                    state = 3;
                    break;
                case '.':
                    if (nextChar()=='.')
                    {
                        addParentStep();
                        state = 9;
                        break;
                    }
                    else
                        throw new SDOPathException(SDOError.messageForCode("path.parse.expectingSecondDot",c));
                default:
                    if (XMLChar.isNameStart(c))
                    {
                        state = 10;
                        assert buf.length()==0;
                        buf.append(c);
                    }
                    else
                        throw new SDOPathException(SDOError.messageForCode("path.parse.expecting3", '/', '@', "NCNameStart", c));
                }
                break;

            case 2:             // begining of step
                switch (c)
                {
                case '@':
                    state = 3;
                    break;
                case '.':
                    if (nextChar()=='.')
                    {
                        addParentStep();
                        state = 9;
                        break;
                    }
                    else
                        throw new SDOPathException(SDOError.messageForCode("path.parse.expectingSecondDot",c));
                default:
                    if (XMLChar.isNCNameStart(c))
                    {
                        state = 10;
                        assert buf.length()==0;
                        buf.append(c);
                    }
                    else
                        throw new SDOPathException(SDOError.messageForCode("path.parse.expecting2", '@', "NCName", c));
                }
                break;

            case 3:         // after @
                if (XMLChar.isNCNameStart(c))
                {
                    state = 10;
                    assert buf.length()==0;
                    buf.append(c);
                }
                else
                    throw new SDOPathException(SDOError.messageForCode("path.parse.expecting1", "NCName", c));
                break;

            case 4:         // after scheme:
                switch (c)
                {
                case ':':
                    String scheme = property;
                    if (!"sdo".equals(scheme))
                    {
                        return nonSDOPathPlan(scheme);
                    }
                    state = 1;
                    break;
                default:
                    throw new SDOPathException(SDOError.messageForCode("path.parse.expecting1", ':', c));
                }
                break;

            case 5:         // end
                assert !hasNextChar() ;
                break;

            case 6:         // middle of scheme or property name
                switch (c)
                {
                case ':':   // it's scheme field but save it in property local var for now and use it in state 4
                    property = getBufAndReset(buf);
                    state = 4;
                    break;
                case '[':
                    property = getBufAndReset(buf);
                    state = 7;
                    break;
                case '.':
                    property = getBufAndReset(buf);
                    state = 8;
                    break;
                case '/':
                    addPropertyStep(buf);
                    state = 2;
                    break;
                default:
                    if (XMLChar.isNCName(c))
                    {
                        state = 6;
                        buf.append(c);
                    }
                    else
                        throw new SDOPathException(SDOError.messageForCode("path.parse.expecting4",':' , '[' , '.' , "NCNamePart", c));
                }
                break;

            case 7:     // after property[
                if (c!='0' && Character.isDigit(c))
                {
                    state = 11;
                    assert buf.length()==0;
                    buf.append(c);
                }
                else if (XMLChar.isNCNameStart(c))
                {
                    state = 12;
                    assert buf.length()==0;
                    buf.append(c);
                }
                else
                    throw new SDOPathException(SDOError.messageForCode("path.parse.expecting2","1-9", "NCNameStart", c));
                break;

            case 8:     // after property '.'
                if (Character.isDigit(c))
                {
                    state = 16;
                    assert buf.length()==0;
                    buf.append(c);
                }
                else
                    throw new SDOPathException(SDOError.messageForCode("path.parse.expecting1","digit", c));
                break;

            case 9:         // end of step
                if (c=='/')
                    state = 2;
                //else if (!hasNextChar()) covered before switch(state)
                //    state = 5;
                else
                    throw new SDOPathException(SDOError.messageForCode("path.parse.expecting2",'/', "end", c));
                break;

            case 10:        // in the middle of property
                switch (c)
                {
                case '/':
                    addPropertyStep(buf);
                    state = 2;
                    break;
                case '[':
                    property = getBufAndReset(buf);
                    state = 7;
                    break;
                case '.':
                    property = getBufAndReset(buf);
                    state = 8;
                    break;
                default:
                    if (XMLChar.isNCName(c))
                    {
                        state = 10;
                        buf.append(c);
                    }
                    //else if (!hasNextChar()) covered before switch(state)
                    //    state = 5;
                    else
                        throw new SDOPathException(SDOError.messageForCode("path.parse.expecting5", '/', '[', '.', "NCNamePart", "end", c));
                }
                break;

            case 11:
                if (c==']')
                {
                    addIndex1Step(property, buf);
                    state = 9;
                }
                else if( Character.isDigit(c))
                {
                    state = 11;
                    buf.append(c);
                }
                else
                    throw new SDOPathException(SDOError.messageForCode("path.parse.expecting2", ']', "digit", c));
                break;

            case 12:
                if (c=='=')
                {
                    attribute = getBufAndReset(buf);
                    state = 13;
                }
                else if( XMLChar.isNCName(c))
                {
                    state = 12;
                    buf.append(c);
                }
                else
                    throw new SDOPathException(SDOError.messageForCode("path.parse.expecting2", '=', "NCNamePart", c));
                break;

            case 13:        // start of value
                switch (c)
                {
                case '"':
                    assert buf.length()==0;
                    state = 14;
                    break;
                case '\'':
                    assert buf.length()==0;
                    state = 15;
                    break;
                case '.':
                    state = 20;
                    break;
                case 't':
                    if (nextChar()=='r' && nextChar()=='u' && nextChar()=='e' && nextChar()==']')
                    {
                        addBooleanValueStep(property, attribute, true);
                        state = 9;
                        break;
                    }
                    else
                        throw new SDOPathException(SDOError.messageForCode("path.parse.expecting1", "true", c));
                case 'f':
                    if (nextChar()=='a' && nextChar()=='l' && nextChar()=='s' && nextChar()=='e' && nextChar()==']')
                    {
                        addBooleanValueStep(property, attribute, false);
                        state = 9;
                        break;
                    }
                    else
                        throw new SDOPathException(SDOError.messageForCode("path.parse.expecting1", "false", c));
                default:
                    if (Character.isDigit(c))
                    {
                        state = 18;
                        buf.append(c);
                    }
                    else
                        throw new SDOPathException(SDOError.messageForCode("path.parse.expecting3",
                            "Literal('\"' [^\"] '\"' | \"'\" [^'] \"'\")", "Number (Digits ('.' Digits?)? | '.' Digits)",
                            "Boolean (true|false)", c));
                }
                break;

            case 14:
                if (c=='\"')
                    state = 17;
                else
                {
                    buf.append(c);
                    state = 14;
                }
                break;

            case 15:
                if (c=='\'')
                    state = 17;
                else
                {
                    buf.append(c);
                    state = 15;
                }
                break;

            case 16:
                if (c=='/')
                {
                    addIndex0Step(property, buf);
                    state = 2;
                }
                else if (Character.isDigit(c))
                {
                    state = 16;
                    buf.append(c);
                }
                //else if (!hasNextChar()) covered before switch(state)
                //    state = 5;
                else
                    throw new SDOPathException(SDOError.messageForCode("path.parse.expecting3",
                        '/', "digit", "end", c));
                break;

            case 17:
                if (c==']')
                {
                    addLiteralValueStep(property, attribute, buf);
                    state = 9;
                }
                else
                    throw new SDOPathException(SDOError.messageForCode("path.parse.expecting1", ']', c));
                break;

            case 18:
                switch(c)
                {
                case'.':
                    state = 19;
                    buf.append(c);
                    break;
                case']':
                    addNumeralValueStep(property, attribute, buf);
                    state = 9;
                    break;
                default:
                    if( Character.isDigit(c))
                    {
                        state = 18;
                        buf.append(c);
                    }
                    else
                        throw new SDOPathException(SDOError.messageForCode("path.parse.expecting2", '.', "digit", c));
                }
                break;

            case 19:
                if (c==']')
                {
                    addNumeralValueStep(property, attribute, buf);
                    state = 9;
                }
                else if( Character.isDigit(c))
                {
                    state = 19;
                    buf.append(c);
                }
                else
                    throw new SDOPathException(SDOError.messageForCode("path.parse.expecting2", ']', "digit", c));
                break;

            case 20:
                if ( Character.isDigit(c))
                {
                    state = 21;
                    assert buf.length()==0;
                    buf.append("0.");
                    buf.append(c);
                }
                else
                    throw new SDOPathException(SDOError.messageForCode("path.parse.expecting1", "digit", c));
                break;

            case 21:
                if (c==']')
                {
                    addNumeralValueStep(property, attribute, buf);
                    state = 9;
                }
                else if( Character.isDigit(c))
                {
                    state = 21;
                    buf.append(c);
                }
                else
                    throw new SDOPathException(SDOError.messageForCode("path.parse.expecting2", ']', "digit", c));
                break;

            default:
                throw new IllegalStateException();
            }
        }

        assert state==5;

        return _plan;
    }

    private PathPlan nonSDOPathPlan(String scheme)
    {
        CharSequence subExp = _expression.subSequence(_index, _length);
        //System.out.println("  NonSDOPath: " + scheme + ": " + subExp);
        return new NonSDOPathPlan(scheme, subExp);
    }

    private String getBufAndReset(StringBuffer buf)
    {
        String prop = buf.toString();
        buf.delete(0, buf.length());
        return prop;
    }

    private void addRootStep()
    {
        //System.out.println("  Step: /" );
        _plan.addRootStep();
    }

    private void addPropertyStep(StringBuffer buf)
    {
        String property = buf.toString();
        buf.delete(0, buf.length());
        //System.out.println("  Step: " + property );
        _plan.addPropertyStep(property);
    }

    private void addIndex0Step(String property, StringBuffer buf)
    {
        int index_from_0 = Integer.valueOf(buf.toString());
        buf.delete(0, buf.length());
        //System.out.println("  Step: " + property + "." + index_from_0);
        _plan.addIndex0Step(property, index_from_0);
    }

    private void addIndex1Step(String property, StringBuffer buf)
    {
        int index_from_1 = Integer.valueOf(buf.toString());
        buf.delete(0, buf.length());
        //System.out.println("  Step: " + property + "[ " + index_from_1 + " ]");
        _plan.addIndex1Step(property, index_from_1);
    }

    private void addParentStep()
    {
        //System.out.println("  Step: ..");
        _plan.addParentStep();
    }

    private void addLiteralValueStep(String property, String attribute, StringBuffer buf)
    {
        String value = buf.toString();
        buf.delete(0, buf.length());
        //System.out.println("  Step: " + property + "[ " + attribute + " = '" + value + "' ]");
        _plan.addLiteralStep(property, attribute, value);
    }

    private void addNumeralValueStep(String property, String attribute, StringBuffer buf)
    {
        BigDecimal numeral = new BigDecimal(buf.toString());
        buf.delete(0, buf.length());
        //System.out.println("  Step: " + property + "[ " + attribute + " = " + numeral + " ]");
        _plan.addNumeralStep(property, attribute, numeral);
    }

    private void addBooleanValueStep(String property, String attribute, boolean value)
    {
        //System.out.println("  Step: " + property + "[ " + attribute + " = " + value + " ]");
        _plan.addBooleanStep(property, attribute, value);
    }

    public class SDOPathException extends Exception
    {
        public SDOPathException(String msg)
        {
            super(msg);
        }
    }
}
