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
package davos.sdo.impl.xpath;

import davos.sdo.DataObjectXML;
import davos.sdo.ListXMLIterator;
import davos.sdo.PropertyXML;
import davos.sdo.SequenceXML;
import davos.sdo.SDOError;
import davos.sdo.impl.common.ChangeSummaryXML;
import davos.sdo.impl.common.Common;
import davos.sdo.impl.type.BuiltInTypeSystem;
import davos.sdo.impl.type.SimpleValueHelper;
import javax.sdo.helper.XMLDocument;
import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.impl.common.XMLChar;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
  * XPath   - this XPath engine executes XPath on the XML representation of a DataObject
  *         - returns only selections
  *         - expression can be: /root/a/b/c, //c, //c | //e, //a/b/c, .//c, //c/@at1,
  *             declare default element namespace \"simpleTypeTest\"; /root/a/b/c
  *             declare namespace p=\"simpleTypeTest\"; /p:root/a/b/c/@at1
  *         Note: Expressions starting with / will start the evaluation from xmlObject.getRootObject()
  *
  * Usage:
  *      XPath xpplan = XPath.compile(xpath_expression, prefixes_to_namespaces_map);
  *      XPath.Selection s = XPath.execute(xpplan, (DataObjectXML)dataObject);
  *      for (int i = 0; s.hasNext(); i++)
  *      {
  *          Object v = s.getValue();
  *          System.out.println("   v[" + i + "]: " + v + "\t\t" + s.getPropertyXML());
  *          s.next();
  *      }
  */
public class XPath
{
    // public API
    public static XPath compile(String expression,  Map<String, String> prefixesToUris)
        throws XPathCompileException
    {
        //System.out.println("XP: " + xp);
        return compile(expression, null, new MapToNamespaceContextImpl(prefixesToUris));
    }

    public static XPath compile(String expression, NamespaceContext prefixesToUris)
        throws XPathCompileException
    {
        //System.out.println("XP: " + xp);
        return compile(expression, null, prefixesToUris);
    }

    public static XPath compile( String xpath )
        throws XPathCompileException
    {
        return compile( xpath, null, null );
    }

    public static XPath compile(String xpath, String currentNodeVar, NamespaceContext namespaces )
            throws XPathCompileException
    {
        return  new CompilationContext( namespaces, currentNodeVar ).compile( xpath );
    }

    public static class XPathCompileException extends XmlException
    {
        XPathCompileException ( XmlError err )
        {
            super( err.toString(), null, err );
        }
    }

    public static Selection execute(XPath xpath, DataObjectXML dataObject)
    {
        return new Selection(xpath, dataObject);
    }
    //end public API


    //
    //
    //

    private final Selector _selector;
    private final boolean  _sawDeepDot;
    private final boolean  _startFromRoot;

    private XPath ( Selector selector, boolean sawDeepDot, boolean startFromRoot )
    {
        _selector = selector;
        _sawDeepDot = sawDeepDot;
        _startFromRoot = startFromRoot;
    }

    boolean sawDeepDot()
    {
        return _sawDeepDot;
    }

    public boolean startFromRoot()
    {
        return _startFromRoot;
    }

    public String toString()
    {
        return (_sawDeepDot ? "SawDeepDot " : "" ) + (_startFromRoot ? "StartFromRoot " : "" ) + _selector;
    }

    public static class MapToNamespaceContextImpl
        implements NamespaceContext
    {
        Map<String, String> _prefixesToUris;

        MapToNamespaceContextImpl(Map<String, String> prefixesToUris)
        {
            _prefixesToUris = prefixesToUris;
        }

        public String getNamespaceURI(String prefix)
        {
            return _prefixesToUris.get(prefix);
        }

        public String getPrefix(String namespaceURI)
        {
            throw new IllegalStateException("Not implemented");
        }

        public Iterator getPrefixes(String namespaceURI)
        {
            return _prefixesToUris.keySet().iterator();
        }
    }

    //
    // Compilation
    //
    private static class CompilationContext
    {
        CompilationContext ( NamespaceContext namespaces, String currentNodeVar )
            throws XPathCompileException
        {
            if ( currentNodeVar != null && currentNodeVar.startsWith( "$" ) )
                throw newError(SDOError.messageForCode("path.parse.current.node.variable.name.should.not.start.with.dollar"));

            _startFromRoot = false;

            if (currentNodeVar == null)
                _currentNodeVar = "this";
            else
                _currentNodeVar = currentNodeVar;

            _namespaces = new HashMap<String, String>();

            _externalNamespaces =
                namespaces == null ? new MapToNamespaceContextImpl(new HashMap<String, String>()) : namespaces;
        }

        XPath compile ( String expr ) throws XPathCompileException
        {
            _offset = 0;
            _line = 1;
            _column = 1;
            _expr = expr;

            return tokenizeXPath();
        }

        int currChar ( )
        {
            return currChar( 0 );
        }

        int currChar ( int offset )
        {
            return
                _offset + offset >= _expr.length()
                    ? END_OF_EXPRESSION
                    : _expr.charAt( _offset + offset );
        }

        void advance ( )
        {
            if (_offset < _expr.length())
            {
                char ch = _expr.charAt( _offset );

                _offset++;
                _column++;

                if (ch == '\r' || ch == '\n')
                {
                    _line++;
                    _column = 1;

                    if (_offset + 1 < _expr.length())
                    {
                        char nextCh = _expr.charAt( _offset + 1 );

                        if ((nextCh == '\r' || nextCh == '\n') && ch != nextCh)
                            _offset++;
                    }
                }
            }
        }

        void advance ( int count )
        {
            assert count >= 0;

            while ( count-- > 0 )
                advance();
        }

        boolean isWhitespace ( )
        {
            return isWhitespace( 0 );
        }

        boolean isWhitespace ( int offset )
        {
            int ch = currChar( offset );
            return ch == ' ' || ch == '\t' || ch == '\n' || ch == '\r';
        }

        boolean isDigit ( )
        {
            int c = currChar();
            return c == -1 ? false : ( c>='0' && c<='9');
        }

        boolean isNCNameStart ( )
        {
            int c = currChar();
            return c == -1 ? false : XMLChar.isNCNameStart( c );
        }

        boolean isNCName ( )
        {
            int c = currChar();
            return c == -1 ? false : XMLChar.isNCName( c );
        }

        boolean startsWith ( String s )
        {
            return startsWith( s, 0 );
        }

        boolean startsWith ( String s, int offset )
        {
            if (_offset + offset >= _expr.length())
                return false;

            return _expr.startsWith( s, _offset + offset );
        }

        private XPathCompileException newErrorForCode ( String errorCode, Object... args )
        {
            String msg = SDOError.messageForCode(errorCode, args);
            return newError(msg);
        }

        private XPathCompileException newError ( String msg )
        {
            XmlError err =
                XmlError.forLocation(
                    msg, XmlError.SEVERITY_ERROR, null,
                    _line, _column, _offset );

            return new XPathCompileException( err );
        }

        String lookupPrefix ( String prefix ) throws XPathCompileException
        {
            if (_namespaces.containsKey( prefix ))
                return (String) _namespaces.get( prefix );

            String res = _externalNamespaces.getNamespaceURI(prefix);
            if ( res!=null )
                return res;

            if (prefix.equals( "xml" ))
                return "http://www.w3.org/XML/1998/namespace";

            if (prefix.equals( "xs" ))
                return "http://www.w3.org/2001/XMLSchema";

            if (prefix.equals( "xsi" ))
                return "http://www.w3.org/2001/XMLSchema-instance";

            if (prefix.equals( "fn" ))
                return "http://www.w3.org/2002/11/xquery-functions";

            if (prefix.equals( "xdt" ))
                return "http://www.w3.org/2003/11/xpath-datatypes";

            if (prefix.equals( "local" ))
                return "http://www.w3.org/2003/11/xquery-local-functions";

            throw newError(SDOError.messageForCode("path.run.undefined.prefix.0", prefix) );
        }

        private boolean parseWhitespace ( ) throws XPathCompileException
        {
            boolean sawSpace = false;

            while ( isWhitespace() )
            {
                advance();
                sawSpace = true;
            }

            return sawSpace;
        }

        //
        // Tokenizing will consume whitespace followed by the tokens, separated
        // by whitespace.  The whitespace following the last token is not
        // consumed.
        //
        private boolean tokenize ( String s )
        {
            assert s.length() > 0;

            int offset = 0;

            while ( isWhitespace( offset ) )
                offset++;

            if (!startsWith( s, offset ))
                return false;

            offset += s.length();

            advance( offset );

            return true;
        }

        private boolean tokenize ( String s1, String s2 )
        {
            assert s1.length() > 0;
            assert s2.length() > 0;

            int offset = 0;

            while ( isWhitespace( offset ) )
                offset++;

            if (!startsWith( s1, offset ))
                return false;

            offset += s1.length();

            while ( isWhitespace( offset ) )
                offset++;

            if (!startsWith( s2, offset ))
                return false;

            offset += s2.length();

            advance( offset );

            return true;
        }

        private boolean tokenize ( String s1, String s2, String s3)
        {
            assert s1.length() > 0;
            assert s2.length() > 0;
            assert s3.length() > 0;

            int offset = 0;

            while ( isWhitespace( offset ) )
                offset++;

            if (!startsWith( s1, offset ))
                return false;

            offset += s1.length();

            while ( isWhitespace( offset ) )
                offset++;

            if (!startsWith( s2, offset ))
                return false;

            offset += s2.length();

            while ( isWhitespace( offset ) )
                offset++;

            if (!startsWith( s3, offset ))
                return false;

            offset += s3.length();

             while ( isWhitespace( offset ) )
                offset++;

            advance( offset );

            return true;
        }

        private boolean tokenize ( String s1, String s2, String s3, String s4)
        {
            assert s1.length() > 0;
            assert s2.length() > 0;
            assert s3.length() > 0;
            assert s4.length() > 0;

            int offset = 0;

            while ( isWhitespace( offset ) )
                offset++;

            if (!startsWith( s1, offset ))
                return false;

            offset += s1.length();

            while ( isWhitespace( offset ) )
                offset++;

            if (!startsWith( s2, offset ))
                return false;

            offset += s2.length();

            while ( isWhitespace( offset ) )
                offset++;

            if (!startsWith( s3, offset ))
                return false;

            offset += s3.length();

             while ( isWhitespace( offset ) )
                offset++;

            if (!startsWith( s4, offset ))
                return false;

            offset += s4.length();

            advance( offset );

            return true;
        }


        private String tokenizeNCName ( ) throws XPathCompileException
        {
            parseWhitespace();

            if (!isNCNameStart())
                return null;

            StringBuffer sb = new StringBuffer();

            sb.append( (char) currChar() );

            for ( advance() ; isNCName() ; advance() )
                sb.append( (char) currChar() );

            return sb.toString();
        }

        private QName getAnyQName ( )
        {
            return new QName( "", "" );
        }

        private QName tokenizeQName ( ) throws XPathCompileException
        {
            if (tokenize( "*" ))
                return getAnyQName();

            String ncName = tokenizeNCName();
            if (ncName==null)
                return null;

            if (!tokenize( ":" ))
                return new QName( lookupPrefix( "" ), ncName );

            return
                new QName(
                    lookupPrefix( ncName ),
                    tokenize( "*" ) ? "" : tokenizeNCName() );
        }

        private int tokenizeNumber ( )
            throws XPathCompileException
        {
            if (!isDigit())
                return -1;

            StringBuffer sb = new StringBuffer();

            sb.append( (char) currChar() );

            for ( advance() ; isDigit() ; advance() )
                sb.append( (char) currChar() );

            return Integer.parseInt(sb.toString());
        }

        private String tokenizeQuotedUri ( ) throws XPathCompileException
        {
            char quote;

            if (tokenize( "\"" ))
                quote = '"';
            else  if (tokenize( "'" ))
                quote = '\'';
            else
                throw newError(SDOError.messageForCode("path.parse.expected.quote"));

            StringBuffer sb = new StringBuffer();

            for ( ; ; )
            {
                if (currChar() == -1)
                    throw newError(SDOError.messageForCode("path.parse.path.terminated.in.uri.literal"));

                if (currChar() == quote)
                {
                    advance();

                    if (currChar() != quote)
                        break;
                }

                sb.append( (char) currChar() );

                advance();
            }

            return sb.toString();
        }

        private Step addStep ( boolean deep, boolean attr, QName name, Step steps)
        {
            return addStep(deep, attr, name, steps, Step.NO_INDEX, null);
        }

        private Step addStep ( boolean deep, boolean attr, QName name, Step steps, int index,
            List<Predicate> predicates )
        {
            Step step = new Step( deep, attr, name, index, predicates );

            if (steps == null)
                return step;

            Step s = steps;

            while ( steps._next != null )
                steps = steps._next;

            steps._next = step;
            step._prev = steps;

            return s;
        }

        private Step tokenizeSteps ( )
            throws XPathCompileException
        {
            parseWhitespace();
            

            boolean deep;

            if (tokenize( "$", _currentNodeVar, "//" ) || tokenize( ".", "//" ))
                deep = true;
            else if (tokenize( "$", _currentNodeVar, "/" ) || tokenize( ".", "/" ))
                deep = false;
            else if (tokenize( "$", _currentNodeVar ) || tokenize( "." ))
                return addStep( false, false, null, null );
            else
            {
                if (startsWith( "/" ))
                    _startFromRoot = true;

                deep = false;
            }

            Step steps = null;

            // Compile the steps removing /. and mergind //. with the next step

            boolean deepDot = false;

            for ( ; ; )
            {
                if (tokenize( "attribute", "::" ) || tokenize( "@" ))
                {
                    steps = addStep( deep, true, tokenizeQName(), steps );
                    break;
                }

                QName name;

                if (tokenize( "." ))
                    deepDot = deepDot || deep;
                else if ( (tokenize( "child", "::" ) && (name = tokenizeQName()) != null) ||
                    ( (name = tokenizeQName()) != null ) )
                {
                    int index = Step.NO_INDEX;
                    List<Predicate> predicates = null;

                    if (tokenize("["))
                    {
                        QName propQName;
                        String value = null;
                        boolean isAttribute = false;

                        if ( ( tokenize("position", "(", ")", "=") && (index=tokenizeNumber())>=0 && tokenize("]") ) ||
                            ( (index=tokenizeNumber())>=0 && tokenize("]") )  )
                        {
                            if (index==0)
                                throw newError(SDOError.messageForCode(
                                    "path.parse.index.in.predicate.is.a.number.bigger.than.0"));
                            //System.out.println("  found: " + name + "[" + index + "]");
                        }
                        else
                        {
                            if ( tokenize("@") )
                                isAttribute = true;
                            else if ( tokenize("attribute", "::") )
                                isAttribute = true;
                            else if ( tokenize("child", "::") )
                            {}

                            if ( (propQName = tokenizeQName())!=null && ( tokenize("=") || tokenize("eq") ) &&
                                (value = tokenizeQuotedUri())!=null && tokenize("]"))
                            {
                                //System.out.print("  [ " + (isAttribute ? "@" : "" ) + propQName + " = '" + value + "' ]");
                                predicates = addPredicate(predicates, isAttribute, propQName, value);

                                while (tokenize("["))
                                {
                                    QName propQName2;
                                    String value2 = null;
                                    boolean isAttribute2 = false;
                                    if ( tokenize("@") )
                                        isAttribute2 = true;
                                    else if ( tokenize("attribute", "::") )
                                        isAttribute2 = true;
                                    else if ( tokenize("child", "::") )
                                    {}

                                    if ( (propQName2 = tokenizeQName())!=null && ( tokenize("=") || tokenize("eq") ) &&
                                        (value2 = tokenizeQuotedUri())!=null && tokenize("]"))
                                    {
                                        //System.out.print("  [ " + (isAttribute2 ? "@" : "" ) + propQName2 + " = '" + value2 + "' ]");
                                        predicates = addPredicate(predicates, isAttribute2, propQName2, value2);
                                    }
                                    else
                                        throw newError(SDOError.messageForCode(
                                            "path.parse.invalid.predicate.in.expresion"));
                                }
                                //System.out.println("");
                            }
                        }
                    }

                    steps = addStep( deep, false, name, steps, index, predicates );
                }

                if (tokenize( "//" ))
                {
                    deep = true;
                    deepDot = false;
                }
                else if (tokenize( "/" ))
                {
                    if ( currChar()==END_OF_EXPRESSION )
                        throw newError(SDOError.messageForCode("path.parse.xpath.expression.not.supported.0", _expr));
                    if (deepDot)
                        deep = true;
                    else
                        deep = false;
                }
                else
                    break;
            }

            // If there was a //. at the end of the path, then we need to make
            // two paths, one with * at the end and another with @* at the end.

            if ((_lastDeepDot = deepDot))
            {
                _lastDeepDot = true;
                steps = addStep( true, false, getAnyQName(), steps );
            }

            // Add sentinal step (_name == null)

            return addStep( false, false, null, steps );
        }

        private static List<Predicate> addPredicate(List<Predicate> predicates, boolean isAttr, QName propQName,
            String value)
        {
            if (predicates==null)
                predicates = new ArrayList<Predicate>();

            Predicate candidate = new Predicate(isAttr, propQName, value);
            if (!predicates.contains(candidate))
                predicates.add(candidate);

            return predicates;
        }

        private void computeBacktrack ( Step steps )
            throws XPathCompileException
        {

            //
            // Compute static backtrack information
            //
            // Note that I use the fact that _hasBacktrack is initialized to
            // false and _backtrack to null in the following code.
            //

            Step s, t;

            for ( s = steps ; s != null ; s = t )
            {
                // Compute the segment from [ s, t )

                for ( t = s._next ; t != null && !t._deep ; )
                    t = t._next;

                // If the segment is NOT rooted at //, then the backtrack is
                // null for the entire segment, including possible attr and/or
                // sentinal

                if (!s._deep)
                {
                    for ( Step u = s ; u != t ; u = u._next )
                        u._hasBacktrack = true;

                    continue;
                }

                // Compute the sequence [ s, u ) of length n which contain no
                // wild steps.

                int n = 0;
                Step u = s;

                while ( u != t && u._name != null && !u.isWild() && !u._attr )
                {
                    n++;
                    u = u._next;
                }

                // Now, apply KMP to [ s, u ) for fast backtracking

                QName [] pattern = new QName [ n + 1 ];
                int [] kmp = new int [ n + 1 ];

                Step v = s;

                for ( int i = 0 ; i < n ; i++ )
                {
                    pattern[ i ] = v._name;
                    v = v._next;
                }

                pattern[ n ] = getAnyQName();

                int i = 0;
                int j = kmp[ 0 ] = -1;

                while ( i < n )
                {
                    while ( j > -1 && !pattern[ i ].equals( pattern[ j ] ) )
                        j = kmp[ j ];

                    if (pattern[ ++i ].equals( pattern[ ++j ] ))
                        kmp[ i ] = kmp[ j ];
                    else
                        kmp[ i ] = j;
                }

                i = 0;

                for ( v = s ; v != u ; v = v._next )
                {
                    v._hasBacktrack = true;
                    v._backtrack = s;

                    for ( j = kmp[ i ] ; j > 0 ; j-- )
                        v._backtrack = v._backtrack._next;

                    i++;
                }

                // Compute the success backtrack and stuff it into an attr and
                // sentinal if they exist for this segment

                v = s;

                if (n > 1)
                {
                    for ( j = kmp[ n - 1 ] ; j > 0 ; j-- )
                        v = v._next;
                }

                if (u != t && u._attr)
                {
                    u._hasBacktrack = true;
                    u._backtrack = v;
                    u = u._next;
                }

                if (u != t && u._name == null)
                {
                    u._hasBacktrack = true;
                    u._backtrack = v;
                }

                // The first part of a deep segment always backtracks to itself

                assert s._deep;

                s._hasBacktrack = true;
                s._backtrack = s;
            }
        }

        private void tokenizePath ( ArrayList<Step> paths )
            throws XPathCompileException
        {
            _lastDeepDot = false;

            Step steps = tokenizeSteps();

            computeBacktrack( steps );

            paths.add( steps );

            // If the last path ended in //., that path will match all
            // elements, here I make a path which matches all attributes.

            if (_lastDeepDot)
            {
                _sawDeepDot = true;

                Step s = null;

                for ( Step t = steps ; t != null ; t = t._next )
                {
                    if (t._next != null && t._next._next == null)
                        s = addStep( t._deep, true, t._name, s );
                    else
                        s = addStep( t._deep, t._attr, t._name, s );
                }

                computeBacktrack( s );

                paths.add( s );
            }
        }

        private Selector tokenizeSelector ( ) throws XPathCompileException
        {
            ArrayList<Step> paths = new ArrayList<Step>();

            tokenizePath( paths );

            while ( tokenize( "|" ) )
                tokenizePath( paths );

            // Check if there are predicates: currently, "|" and predicates are mutually exclusive
            if (paths.size() > 1)
            {
                for (Step path : paths)
                {
                    Step step = path;
                    while (step != null)
                    {
                        if (step._predicates != null)
                            throw newErrorForCode("path.parse.xpath.union.predicate.not.supported", _expr);
                        step = step._next;
                    }
                }
            }

            return new Selector( (Step[]) paths.toArray( new Step [ 0 ] ) );
        }

        private XPath tokenizeXPath ( ) throws XPathCompileException
        {
            for ( ; ; )
            {
                if (tokenize( "declare", "namespace" ))
                {
                    if (!parseWhitespace())
                        throw newError(SDOError.messageForCode("path.parse.expected.prefix.after.declare.namespace") );

                    String prefix = tokenizeNCName();

                    if (!tokenize( "=" ))
                        throw newError( "Expected '='" );

                    String uri = tokenizeQuotedUri();

                    if (_namespaces.containsKey( prefix ))
                    {
                        throw newError(SDOError.messageForCode("path.parse.redefinition.of.namespace.prefix.0", prefix) );
                    }

                    _namespaces.put( prefix, uri );

                    //return these to saxon:? Is it an error to pass external NS
                    //that conflicts? or should we just override it?
                    String res = _externalNamespaces.getNamespaceURI(prefix);
                    if ( res!=null && !res.equals(uri))
                    {
                        throw newError(SDOError.messageForCode("path.redefinition.of.namespace.prefix.1", prefix, uri));
                    }
                    // Mapping of prefix->uri is in _namespaces it will not affect _externalNamespaces 
                    //_externalNamespaces.put( prefix, uri );

                    if (! tokenize( ";" ))
                    {
//			            throw newError(
//                            "Namespace declaration must end with ;" );
			        }

                    continue;
                }

                if (tokenize( "declare","default", "element", "namespace" ))
                {
                    String uri = tokenizeQuotedUri();

                    if (_namespaces.containsKey( "" ))
                    {
                        throw newError(SDOError.messageForCode("path.parse.redefinition.of.default.element.namespace") );
                    }

                    _namespaces.put( "", uri );

                    if (! tokenize( ";" ))
                        throw newError(SDOError.messageForCode(
                            "path.parse.default.namespace.declaration.must.end.with.semicolon") );

                    continue;
                }


                break;
            }

            // Add the default prefix mapping if it has not been redefined

            if (!_namespaces.containsKey( "" ))
                _namespaces.put( "", "" );

            Selector selector = tokenizeSelector();

            parseWhitespace();

            if (currChar() != END_OF_EXPRESSION)
            {
                // '/' xpath not supported because there isn't a Document object available to return
                throw newError(SDOError.messageForCode("unexpected.char.0", currChar()) );
            }

            return new XPath( selector, _sawDeepDot, _startFromRoot);
        }

        private static int END_OF_EXPRESSION = -1;
        private String _expr;

        private boolean _sawDeepDot;  // Saw one overall
        private boolean _lastDeepDot;
        boolean _startFromRoot;

        private String _currentNodeVar;

       // private Map _namespaces;
        protected Map<String, String> _namespaces;
        private NamespaceContext _externalNamespaces;

        private int _offset;
        private int _line;
        private int _column;
    }

    /**
     * Constructs an already-compiled XPath which is missing the last XPath step.
     * The XPath must not have disjunctions in it
     * @return the new constructed XPath
     */
    public XPath getXPathToParent() throws XmlException
    {
        Selector sel = _selector;
        if (sel._paths.length != 1)
            throw new XmlException("Could not compute parent path");
        Step cur = sel._paths[0];
        if (cur._name == null) // last 'sentinel' step
            return null; // no 'parent' XPath
        if (cur._next._name == null) // the path only had one step
        {
            // Check to see if that one step has backtracking or not
            if (cur._deep)
                throw new XmlException("Could not compute parent path");
            return null; // no 'parent' XPath
        }
        Selector newsel = new Selector(new Step[1]);
        Step newstep = copyStep(cur, new HashMap<Step,Step>());
        newsel._paths[0] = newstep;
        cur = newstep;
        // Eliminate the last step
        while (cur._next._name != null)
            cur = cur._next;
        cur._prev._next = cur._next;
        cur._next._prev = cur._prev;
        cur._next._backtrack = cur._backtrack;
        // Check what was the step eliminated
        if (cur._deep)
            throw new XmlException("Could not compute parent path");
        return new XPath(newsel, _sawDeepDot, _startFromRoot);
    }

    private Step copyStep(Step s, HashMap<Step,Step> copied)
    {
        Step r = new Step(s._deep, s._attr, s._name, s._index, s._predicates);
        copied.put(s, r);
        if (s._hasBacktrack)
        {
            r._hasBacktrack = true;
            if (s._backtrack == null)
                r._backtrack = null;
            else if (copied.containsKey(s._backtrack))
                r._backtrack = copied.get(s._backtrack);
            else
                r._backtrack = copyStep(s._backtrack, copied);
        }
        if (s._next != null)
        {
            Step rr = copyStep(s._next, copied);
            r._next = rr;
            rr._prev = r;
        }
        return r;
    }

    public int getFinalStepIndex()
    {
        if (_selector._paths.length != 1)
            return Step.NO_INDEX;
        Step cur = _selector._paths[0];
        if (cur._name == null)
            return Step.NO_INDEX;
        while (cur._next._name != null)
            cur = cur._next;
        return cur._index;
    }

    private static final class Step
    {
        Step ( boolean deep, boolean attr, QName name, int index, List<Predicate> predicates )
        {
            _name = name;
            _index = index;
            _predicates = predicates;

            _deep = deep;
            _attr = attr;

            int flags = 0;

            if (_predicates!=null)
                flags |= ExecutionContext.DESCEND | ExecutionContext.ATTRS;

            if (_deep || !_attr)
                flags |= ExecutionContext.DESCEND;

            if (_attr)
                flags |= ExecutionContext.ATTRS;

            _flags = flags;
        }

        boolean isWild ( )
        {
            return _name.getLocalPart().length() == 0;
        }

        boolean match ( QName name )
        {
            String local = _name.getLocalPart();
            String nameLocal = name.getLocalPart();
            String uri;
            String nameUri;

            int localLength = local.length();
            int uriLength;

            // match any name to _name when _name is empty ""@""
            if (localLength==0)
            {
                uri = _name.getNamespaceURI();
                uriLength = uri.length();

                if (uriLength==0)
                    return true;

                return uri.equals(name.getNamespaceURI());
            }

            if (localLength!=nameLocal.length())
                return false;

            uri = _name.getNamespaceURI();
            nameUri = name.getNamespaceURI();

            if (uri.length()!=nameUri.length())
                return false;

            return local.equals(nameLocal) && uri.equals(nameUri);
        }

        boolean matchIndex ( int index )
        {
            // match any any index if NO_INDEX is set
            if ( _index == NO_INDEX )
                return true;

            return _index == index;
        }

        public String toString()
        {
            return "[" +
                (_attr ? "@" : "") +
                (_deep ? "/" : "") +
                ((_flags&ExecutionContext.ATTRS) > 0 ? "ATTRS" : "" ) + " " +
                ((_flags&ExecutionContext.DESCEND) > 0 ? "DESCEND" : "" ) + " " +
                ((_flags&ExecutionContext.HIT) > 0 ? "HIT" : "" ) + " " +
                (_hasBacktrack ? "Backtrack:" + (_backtrack!=null ? _backtrack._name : null) : "") + " " +
                _name + (_index>0 ? "[" + _index + "]" : "") + (_predicates==null ? "" : _predicates.toString())
                + " " + _next + "]";
        }

        final boolean _attr;
        final boolean _deep;

        int _flags;

        final QName _name;
        final int _index;
        final List<Predicate> _predicates;

        final static int NO_INDEX = -1;

        Step _next, _prev;

        boolean _hasBacktrack;
        Step    _backtrack;
    }

    private static final class Predicate
    {
        final boolean _isAttr;
        final QName _qname;
        final String _value;

        Predicate(boolean isAttr, QName qname, String value)
        {
            _isAttr = isAttr;
            _qname = qname;
            _value = value;
        }

        public String toString()
        {
            return " " + (_isAttr ? "@" : "" ) + _qname + " = '" + _value + "' ";
        }
    }

    private static final class Selector
    {
        Selector ( Step[] paths )
        {
            _paths = paths;
        }

        public String toString()
        {
            String res = "";
            for (int i = 0; i < _paths.length; i++)
            {
                res += ", " + _paths[i];
            }
            return res;
        }

        final Step[] _paths;
    }

    //
    // Execution
    //

    // ExecutionContext class implements the xpath logic
    public static class ExecutionContext
    {
        public static final int HIT     = 0x1;
        public static final int DESCEND = 0x2;
        public static final int ATTRS   = 0x4;
        // Can appear on element() to indicate that there are predicates present, so all the
        // subsequent HITs are condional upon the predicates matching
        public static final int COND    = 0x8;
        // Can appear on end() and indicate whether the predicates on the element matched or not
        public static final int COND_TRUE  = 0x02;
        public static final int COND_FALSE = 0x04;

        private static final Integer ZERO = new Integer(0);

        private XPath            _xpath;
        private ArrayList<Level> _stack;
        private PathContext[]    _pathContexts;


        static private class Level
        {
            QName _qname;
            Map<QName, Integer> _childCounts;
        }

        private int stackSize()
        {
            return _stack.size();
        }

        private void stackAdd(QName qname)
        {
            Level level = new Level();
            level._qname = qname;
            level._childCounts = new HashMap<QName, Integer>();
            _stack.add(level);

            if (_stack.size()==1)
                return;

            Level parentLevel = _stack.get(_stack.size() - 2 );
            Map<QName, Integer> parentLevelChildCounts = parentLevel._childCounts;
            Integer count = parentLevelChildCounts.get(qname);
            if (count==null)
                count = ZERO;

            parentLevelChildCounts.put(qname, count+1);
        }

        private QName stackGetQName(int depth)
        {
            assert depth >= 0;
            return _stack.get(depth)._qname;
        }

        private int stackGetTopIndex()
        {
            int depth = stackSize() - 1;
            assert depth >= 0;

            if (depth==0)
                return 1;

            QName qname = stackGetQName(depth);
            int res = _stack.get(depth-1)._childCounts.get(qname);
            assert res > 0;

            return res;
        }

        private void stackPop()
        {
            _stack.remove( _stack.size() - 1 );
        }

        public ExecutionContext ( )
        {
            _stack = new ArrayList<Level>();
        }

        public final void init ( XPath xpath )
        {
            if (_xpath != xpath)
            {
                _xpath = xpath;

                _pathContexts = new PathContext [ xpath._selector._paths.length ];

                for ( int i = 0 ; i < _pathContexts.length ; i++ )
                    _pathContexts[ i ] = new PathContext();
            }

            _stack.clear();
            stackAdd(new QName("NotImportant", "NoName"));  // this is to have a parent Level to count children even for top level elements
            // ex: <name/><address>San Jose</address><address>Seattle</address><orders/>  and xpath: address[2]
            
            for ( int i = 0 ; i < _pathContexts.length ; i++ )
                _pathContexts[ i ].init( xpath._selector._paths[ i ] );
        }

        public final int start ( )
        {
//            System.out.println("     . start: ");
            int result = 0;

            for ( int i = 0 ; i < _pathContexts.length ; i++ )
                result |= _pathContexts[ i ].start();

            return result;
        }

        public final int element ( QName name )
        {
//            System.out.println("     . elem: " + name);
            assert name != null;

            stackAdd( name );

            int result = 0;

            for ( int i = 0 ; i < _pathContexts.length ; i++ )
                result |= _pathContexts[ i ].element( name );

            return result;
        }

        public final int characters ( String chars )
        {
//            System.out.println("     . chars: " + chars);
            int result = 0;
            for ( int i = 0 ; i < _pathContexts.length ; i++ )
                result |= _pathContexts[ i ].characters( chars );

            return result;
        }

        public final boolean attr ( QName name, String value )
        {
//            System.out.println("     . attr: " + name);
            boolean hit = false;

            for ( int i = 0 ; i < _pathContexts.length ; i++ )
                hit = hit | _pathContexts[ i ].attr( name, value );

            return hit;
        }

        public final int end ( )
        {
//            System.out.println("     . end : " + (_stack.size() - 1));
            stackPop();

            int result = 0;
            for ( int i = 0 ; i < _pathContexts.length ; i++ )
                result |= _pathContexts[ i ].end();

            return result;
        }

        private static class ElementInfo
        {
            private QName _name;
            private StringBuffer _text;

            ElementInfo(QName name)
            {
                _name = name;
            }

            void append(String text)
            {
                if (text==null)
                    return;
                
                if (_text==null)
                    _text = new StringBuffer(text);
                else
                    _text.append(text);
            }

            QName getName()
            {
                return _name;
            }

            String getText()
            {
                return _text==null ? "" : _text.toString();
            }
        }

        private final class PathContext
        {
            private Step       _step;       // current step, might be null if element is at the last step or beyond
            private List<Step> _stepStack;
            private List<List<Predicate>> _predicateStack;  // a stack of lists of predicates that matched true
                // at the respective step. When pop at end(), if all predicates matched than current node is a match.
            private List<ElementInfo> _elementInfoStack;  // a stack of test at a given level

            PathContext ( )
            {
                _stepStack = new ArrayList<Step>();
                _predicateStack = new ArrayList<List<Predicate>>();
                _elementInfoStack = new ArrayList<ElementInfo>();
            }

            void init ( Step steps )
            {
                _step = steps;
                _stepStack.clear();
                _predicateStack.clear();
                _elementInfoStack.clear();
            }

            private QName top ( int i )
            {
                return (QName) ExecutionContext.this.stackGetQName( stackSize() - 1 - i );
            }

            private Step getPreviousStep()
            {
                if (_stepStack ==null || _stepStack.isEmpty())
                    return null;

                return _stepStack.get(_stepStack.size()-1);
            }

            private void addPredicateOnPreviousStep(Predicate pred)
            {
                assert _predicateStack !=null && !_predicateStack.isEmpty();
                List<Predicate> predList = _predicateStack.get(_predicateStack.size()-1);
                if (!predList.contains(pred))
                    predList.add(pred);
            }

            private void backtrack ( )
            {
                assert _step != null;

                if (_step._hasBacktrack)
                {
                    _step = _step._backtrack;
                    return;
                }

                assert !_step._deep;

                _step = _step._prev;

                search: for ( ; !_step._deep ; _step = _step._prev )
                {
                    int t = 0;

                    for ( Step s = _step; !s._deep ; s = s._prev )
                    {
                        if (!s.match( top( t++ )))
                            continue search;
                    }

                    break;
                }
            }

            int start ( )
            {
                assert _step != null;
                assert _step._prev == null;

                //System.out.println("> START: "+ _step._predicates);

                if (_step._name != null)
                    return _step._flags;

                // If the steps consist on only a terminator, then the path can
                // only be '.'.  In this case, we get a hit, but there is
                // nothing else to match.  No need to backtrack.

                _step = null;

                return HIT;
            }

            int element ( QName name )
            {
//                System.out.println("> ELM " + name + ": "+ (_step==null ? "NoStep" : _step._predicates));
                _stepStack.add(_step);
                _predicateStack.add(null);
                _elementInfoStack.add(new ElementInfo(name));

                if (_step == null)
                    return 0;

                assert _step._name != null;

                if (!_step._attr && _step.match( name ) && _step.matchIndex( ExecutionContext.this.stackGetTopIndex() ))
                {
                    Step prevStep = _step;
                    _step = _step._next;
                    boolean predicates = prevStep._predicates != null;
                    if (predicates)
                        _predicateStack.set(_predicateStack.size()-1, new ArrayList<Predicate>());
                    if ( _step._name != null )
                        if (predicates)
                            return COND | _step._flags;
                        else
                            return _step._flags;

                    backtrack();
                    if (predicates)
                        return COND | HIT | prevStep._flags; // result is conditioned by predicates matching
                    else
                        return _step == null ? HIT : HIT | _step._flags;
                }

                for ( ; ; )
                {
                    backtrack();

                    if (_step == null)
                        return 0;

                    if (_step.match( name ))
                    {
                        _step = _step._next;
                        break;
                    }

                    if (_step._deep)
                        break;
                }

                return _step._flags;
            }

            int characters ( String chars )
            {
//                System.out.println("> CHR " + chars + ": "+ (_step==null ? "NoStep" : _step._predicates));
                _elementInfoStack.get(_elementInfoStack.size()-1).append(chars);

                return 0;
            }

            boolean attr ( QName name, String value )
            {
//                System.out.println("> AT " + name + ": "+ ( _step==null? "NoStep" : _step._predicates));
                Step prevStep = getPreviousStep();
                if (prevStep!=null && prevStep._predicates!=null)
                {
                    //check all predicated mark ones that match
                    for (int i = 0; i < prevStep._predicates.size(); i++)
                    {
                        Predicate predicate = (Predicate) prevStep._predicates.get(i);
                        if (predicate._isAttr && predicate._qname.equals(name) && predicate._value.equals(value) )
                        {
                            // pred matched
                            //System.out.println("    - pred matched: " + predicate);
                            // now mark it as matched
                            addPredicateOnPreviousStep(predicate);
                        }
                    }
                }

                return _step != null && _step._attr && _step.match( name );
            }

            int end ( )
            {
//                System.out.println("> END: " + (_step==null ? "NoStep" : _step._predicates));
                _step = _stepStack.remove( _stepStack.size() - 1 );
                List<Predicate> matchedPredicateList = _predicateStack.remove(_predicateStack.size()-1);
                ElementInfo topElementInfo = _elementInfoStack.remove(_elementInfoStack.size()-1);

                Step prevStep = getPreviousStep();
                if (prevStep!=null && _predicateStack.get(_predicateStack.size() - 1) != null
                        /* don't bother checking predicates if the corresponding step didn't "match" */)
                {
                    //check all predicated mark ones that match
                    for (int i = 0; i < prevStep._predicates.size(); i++)
                    {
                        Predicate predicate = prevStep._predicates.get(i);
                        if (!predicate._isAttr && predicate._qname.equals(topElementInfo.getName()) && predicate._value.equals(topElementInfo.getText()) )
                        {
                            // pred matched
                            //System.out.println("    - pred matched: " + predicate);
                            // now mark it as matched
                            addPredicateOnPreviousStep(predicate);
                        }
                    }
                }

                if (matchedPredicateList != null)
                    if (matchedPredicateList.size()==_step._predicates.size())
                        return COND_TRUE; // There are predicates and they matched
                    else
                        return COND_FALSE; // There are predicates but they haven't matched
                else
                    return 0; // There aren't any predicates at the current level
            }
        }
    }

    // DataObjectExecutionContext engine extends ExecutionContext to implement the details of traversal over a DataObject
    private static final class DataObjectExecutionContext
        extends ExecutionContext
    {
        private Selection _selection;
        private Stack<Slice> _stackOfSlices = new Stack<Slice>();
        private boolean _isFinished;

        DataObjectExecutionContext(XPath xpath, DataObjectXML dataObject, Selection selection)
        {
            assert dataObject!=null;
            assert !dataObject.getTypeXML().isDataType();

            _isFinished = false;
            _selection = selection;
            init(xpath);

            if (xpath.startFromRoot())
            {
                dataObject = (DataObjectXML)dataObject.getRootObject();
            }

            int ret = start();
            if ((ret & HIT) != 0)
                selection.addToSelection(dataObject);

            if (xpath.startFromRoot())
            {
                PropertyXML rootProp = dataObject.getContainmentPropertyXML();
                assert rootProp.isXMLElement() && rootProp.isContainment();
                boolean rez = false;
                ret = element(new QName(rootProp.getXMLNamespaceURI(), rootProp.getXMLName()));
                if ( ( (ret&DESCEND)!=0 || (ret&ATTRS)!=0 ))
                {
                    push(dataObject, 0);
                    rez = true;
                }
                if ( (ret&HIT)!=0)
                    _selection.addToSelection(dataObject, rootProp);

                if ( !rez )
                    _isFinished = true;
            }
            else
            {
                push(dataObject, 0);
            }
        }

        private void advance()
        {
            assert !isFinished();

            Slice slice = getTopLevelSlice();
            _isFinished = slice.advance();
        }

        boolean isFinished()
        {
            return _isFinished;
        }

        boolean isAtBottom()
        {
            return _stackOfSlices.size()==1;
        }

        public boolean next()
        {
            if (!_stackOfSlices.isEmpty())
            {
                advance();
                return true;
            }

            return false;
        }

        void pop()
        {
            _stackOfSlices.pop();
        }

        private Slice getTopLevelSlice()
        {
            return _stackOfSlices.peek();
        }

        void push(DataObjectXML dataObject, int propertyIndex)
        {
            _stackOfSlices.push(new DataObjectXMLSlice(dataObject, propertyIndex));
        }

        ChangeSummaryXML getTopChangeSummary()
        {
            return ((ChangeSummarySlice) _stackOfSlices.peek())._changeSummary;
        }

        void pushCS(ChangeSummaryXML cs)
        {
            _stackOfSlices.push(new ChangeSummarySlice(cs));
        }

        void pushCS(ChangeSummaryXML.ChangedObjectXML co)
        {
            _stackOfSlices.push(new ChangeSummarySlice(co));
        }


        // There are two kinds of Slices on the stack:
        //      DataObjectXMLSlice and ChangeSummarySlice
        private abstract static class Slice
        {
            abstract boolean advance();
        }


        private class DataObjectXMLSlice
            extends Slice
        {
            private DataObjectXML _dataObject;
            // if dataObject isSequenced use onli itemIndex,
            // otherwise use both porp and item Indexes to go through all
            int _propertyIndex;
            int _itemIndex;
            ListXMLIterator _itemIterator;

            DataObjectXMLSlice(DataObjectXML dataObjectXML, int propertyIndex)
            {
                _dataObject = dataObjectXML;
                _propertyIndex = propertyIndex;
            }

            // This method is called by Selection.next() and it
            // has to call ExecutionContext start() element() attr() end()
            boolean advance()
            {
                if ( _dataObject.getType().isSequenced() )
                {
                    SequenceXML seq = _dataObject.getSequenceXML();
                    int size = seq.size();
                    if ( _itemIndex < size )
                    {
                        PropertyXML prop = seq.getPropertyXML(_itemIndex);
                        Object value = seq.getValue(_itemIndex);
                        _itemIndex++;
                        if (prop!=null)
                        {
                            if (adv2(prop, value))
                                return false;
                            if (prop.isXMLElement())
                            {
                                int isHit = end();
                                if ((isHit&COND_TRUE)!=0 || (isHit&COND_FALSE)!=0)
                                    _selection.popCondition((isHit&COND_TRUE)!=0);

                                if (isFinished())
                                    return true;
                            }
                        }
                    }
                    else
                    {
                        if (isAtBottom())
                        {
                            return true;
                        }

                        int isHit = end();
                        if ((isHit&COND_TRUE)!=0 || (isHit&COND_FALSE)!=0)
                            _selection.popCondition((isHit&COND_TRUE)!=0);

                        pop();
                    }
                }
                else
                {
                    List<PropertyXML> props = _dataObject.getInstancePropertiesXML();

                    int propsSize = props.size();
                    if ( _propertyIndex < propsSize )
                    {
                        PropertyXML prop = props.get(_propertyIndex);

                        if (prop.isMany())
                        {
                            if (_itemIterator == null)
                                // Means it's the first time we see this property
                                _itemIterator =  _dataObject.getListXMLIterator(prop);
                            if ( _itemIterator.next() )
                            {
                                Object value = _itemIterator.getValue();
                                if ( adv2(_itemIterator.getSubstitution(), value) )
                                    return false;
                                if (prop.isXMLElement())
                                {
                                    int isHit = end();
                                    if ((isHit&COND_TRUE)!=0 || (isHit&COND_FALSE)!=0)
                                        _selection.popCondition((isHit&COND_TRUE)!=0);

                                    if (isFinished())
                                        return true;
                                }
                            }
                            else
                            {
                                _propertyIndex++;
                                _itemIterator = null;
                            }
                        }
                        else
                        {
                            _propertyIndex++;
                            ListXMLIterator iterator = _dataObject.getListXMLIterator(prop);
                            if (iterator.next())
                            {
                                PropertyXML p = iterator.getPropertyXML();
                                if( !(p.getType().isDataType() &&
                                      p.getName().equals(BuiltInTypeSystem.P_VALUETYPE_VALUE.getName()) &&
                                      p.getType().equals(BuiltInTypeSystem.P_VALUETYPE_VALUE.getType()) ))
                                {
                                    Object value = iterator.getValue();
                                    if ( adv2(iterator.getSubstitution(), value) )
                                        return false;
                                    if (prop.isXMLElement())
                                    {
                                        int isHit = end();
                                        if ((isHit&COND_TRUE)!=0 || (isHit&COND_FALSE)!=0)
                                            _selection.popCondition((isHit&COND_TRUE)!=0);

                                        if (isFinished())
                                            return true;
                                    }
                                }
                            }//for the wrapped object case do nothing
                        }
                    }
                    else
                    {
                        if (isAtBottom())
                        {
                            return true;
                        }

                        int isHit = end();
                        if ((isHit&COND_TRUE)!=0 || (isHit&COND_FALSE)!=0)
                            _selection.popCondition((isHit&COND_TRUE)!=0);

                        pop();
                    }
                }
                return false;
            }

            private boolean adv2(PropertyXML prop, Object value)
            {
                boolean rez = false;
                if (prop.isXMLElement())
                {
                    int ret = element(new QName(prop.getXMLNamespaceURI(), prop.getXMLName()));
                    if ( ( (ret&DESCEND)!=0 || (ret&ATTRS)!=0 ) && prop.isContainment())
                    {
                        if (value instanceof DataObjectXML)
                        {
                                push((DataObjectXML)value, 0);
                                rez = true;
                        }
                        else if (value instanceof ChangeSummaryXML)
                        {
                            pushCS((ChangeSummaryXML)value);
                            rez = true;
                        }
                        else
                        {
                            emitCharactersIfDataType(prop, value);
                        }
                    }
                    else
                    {
                        emitCharactersIfDataType(prop, value);
                    }

                    if ( (ret&COND)!=0)
                        _selection.pushCondition();
                    if ( (ret&HIT)!=0)
                        _selection.addToSelection(value, prop);
                }
                else
                {
                    if ( attr(new QName(prop.getXMLNamespaceURI(), prop.getXMLName()), "" + value ) )
                        _selection.addToSelection(value, prop);
                }
                return rez;
            }

            private void emitCharactersIfDataType(PropertyXML prop, Object value)
            {
                if ( prop.getType().isDataType() )
                {
                    SchemaType st = prop.getTypeXML().getXMLSchemaType();
                    int schemaTypeCode = -1;
                    if (st != null)
                        schemaTypeCode = Common.getBuiltinTypeCode(st);
                    try
                    {
                        if (value != null)
                            characters(SimpleValueHelper.getLexicalRepresentation(value,
                                prop.getTypeXML(), schemaTypeCode, null));
                    }
                    catch (SimpleValueHelper.SimpleValueException e)
                    {
                        throw new IllegalStateException(e);
                    }
                }
            }
        }


        private class ChangeSummarySlice
            extends Slice
        {
            private ChangeSummaryXML _changeSummary;
            private Iterator<ChangeSummaryXML.ChangedObjectXML> _changedObjectsIterator;
            private Iterator<ChangeSummaryXML.ChangeXML> _changesIterator;
            private int _level;

            ChangeSummarySlice(ChangeSummaryXML changeSummary)
            {
                _changeSummary = changeSummary;
                _changedObjectsIterator = _changeSummary.getChangedObjectsIterator();
                _level = 1;
            }

            ChangeSummarySlice(ChangeSummaryXML.ChangedObjectXML cObject)
            {
                _changesIterator = cObject.getChangesIterator();
                _level = 2;
            }

            boolean advance()
            {
                if (_level == 1)
                {
                    if (_changedObjectsIterator.hasNext())
                    {
                        ChangeSummaryXML.ChangedObjectXML obj = _changedObjectsIterator.next();
                        boolean advanced = false;
                        int ret = element(obj.getElementName());
                        if ((ret & DESCEND) != 0 || (ret & ATTRS) != 0)
                        {
                            pushCS(obj);
                            advanced = true;
                        }
                        if ((ret & COND) != 0)
                            _selection.pushCondition();
                        if ((ret & HIT) != 0)
                            _selection.addToSelection((DataObjectXML) null);
                        if (advanced)
                            return false;
                        ret = end();
                        if ((ret & COND_TRUE) != 0)
                            _selection.popCondition(true);
                        else if ((ret & COND_FALSE) != 0)
                            _selection.popCondition(false);
                        if (isFinished())
                            return true;
                    }
                    else
                    {
                        if (isAtBottom())
                        {
                            return true;
                        }
                        int ret = end();
                        if ((ret & COND_TRUE) != 0)
                            _selection.popCondition(true);
                        else if ((ret & COND_FALSE) != 0)
                            _selection.popCondition(false);
                        pop();
                    }
                }
                else if (_level == 2)
                {
                    ChangeSummaryXML.ChangeXML change = null;
                    PropertyXML prop = null;
                    while (prop == null && _changesIterator.hasNext())
                    {
                        change = _changesIterator.next();
                        prop = change.getProperty();
                    }
                    if (prop != null)
                    {
                        boolean advanced = false;
                        Object value = change.getResolvedValue();
                        QName qName = change.getQName();
                        if (change.isElement())
                        {
                            int ret = element(qName);
                            if ((ret & DESCEND) != 0 || (ret & ATTRS) != 0)
                            {
                                if (value instanceof DataObjectXML)
                                {
                                    push((DataObjectXML) value, 0);
                                    advanced = true;
                                }
                            }
                            if ((ret & COND) != 0)
                                _selection.pushCondition();
                            if ((ret & HIT) != 0)
                                _selection.addToSelection(value, prop);
                        }
                        else if (attr(qName, "" + value))
                            _selection.addToSelection(value, prop);
                        if (advanced)
                            return false;
                        if (change.isElement())
                        {
                            int ret = end();
                            if ((ret & COND_TRUE) != 0)
                                _selection.popCondition(true);
                            else if ((ret & COND_FALSE) != 0)
                                _selection.popCondition(false);
                            if (isFinished())
                                return true;
                        }
                    }
                    else
                    {
                        if (isAtBottom())
                        {
                            return true;
                        }
                        int ret = end();
                        if ((ret & COND_TRUE) != 0)
                            _selection.popCondition(true);
                        else if ((ret & COND_FALSE) != 0)
                            _selection.popCondition(false);
                        pop();
                    }
                }
                return false;
            }

            int getLevel()
            {
                return _level;
            }
        }
    }

    //
    // Result   - this XPath engine executes and retirns only selections
    //
    public static class Selection
    {
        private List<Object>   _values = new ArrayList<Object>();
        private List<PropertyXML> _properties = new ArrayList<PropertyXML>();
        private int _index = 0;
        private DataObjectExecutionContext _engine;
        private Stack<Integer> _conditionStack = new Stack<Integer>();

        Selection(XPath xpath, DataObjectXML dataObject)
        {
            _engine = new DataObjectExecutionContext(xpath, dataObject, this);
        }

        void addToSelection(XMLDocument xmlDocument)
        {
            _values.add(xmlDocument);
            _properties.add(null);
        }

        void addToSelection(DataObjectXML dataObjectXML)
        {
            _values.add(dataObjectXML);
            _properties.add(null);
        }

        void addToSelection(Object value, PropertyXML prop)
        {
            _values.add(value);
            _properties.add(prop);
        }

        // Signals that the there the subsequent hits are contitional
        void pushCondition()
        {
            _conditionStack.push(_values.size());
        }

        // Removes one condition from the stack, signaling if the condition was true or not
        void popCondition(boolean validateResults)
        {
            if (validateResults)
                // Pops the condition stack, leaves the results in the selection
                _conditionStack.pop();
            else
            {
                // Pops the condition stack, removes any results added after last push
                int newSize = _conditionStack.pop();
                int size = _values.size();
                while (size > newSize)
                    _values.remove(--size);
            }
        }

        // The current selection is only valid as long as there are no pending conditions
        boolean noConditions()
        {
            return _conditionStack.isEmpty();
        }

        public Object getValue()
        {
            return _values.get(_index);
        }

        public PropertyXML getPropertyXML()
        {
            return _properties.get(_index);
        }

        public boolean hasNext()
        {
            if (_index<_values.size())
                return true;

            while(!_engine.isFinished())
            {
                _engine.advance();
                if (_index<_values.size() && noConditions())
                    return true;
            }
            return false;
        }

        public Object next()
        {
            if (hasNext())
            {
                _index ++;
                return _values.get(_index-1);
            }

            return null;
        }
    }
}