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

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import javax.xml.namespace.NamespaceContext;

import org.apache.xmlbeans.impl.common.QNameHelper;

/**
 * Created
 * Date: Jan 10, 2007
 * Time: 4:14:28 PM
 */
public final class NamespaceStack implements NamespaceContext
{
    //
    // Layout of namespace stack:
    //
    //    URI Undo
    //    URI Rename
    //    Prefix Undo
    //    Mapping
    //

    private static final String EMPTY_STRING = Common.EMPTY_STRING;
    private List<String>  _namespaceStack = new ArrayList<String>();
    private Map<String,String> _suggestedPrefixes;
    private Map<String,String> _uriMap;
    private Map<String,String> _prefixMap;
    private int _currentMapping;
    private boolean _useDefaultNamespace;
    private String _initialDefaultUri;

    public NamespaceStack(Map<String,String> suggestedPrefixes, boolean useDefaultNamespace)
    {
        _suggestedPrefixes = suggestedPrefixes;
        _useDefaultNamespace = useDefaultNamespace;
        _uriMap = new HashMap<String,String>();
        _prefixMap = new HashMap<String,String>();
        _initialDefaultUri = EMPTY_STRING;
        addMapping("xml", Names.URI_XML);
        addMapping(EMPTY_STRING, _initialDefaultUri);
    }

    boolean hasMappings ( )
    {
        int i = _namespaceStack.size();

        return i > 0 && _namespaceStack.get( i - 1 ) != null;
    }

    public final void iterateMappings ( )
    {
        _currentMapping = _namespaceStack.size();

        while ( _currentMapping > 0 && _namespaceStack.get( _currentMapping - 1 ) != null )
            _currentMapping -= 8;
    }

    public final boolean hasMapping ( )
    {
        return _currentMapping < _namespaceStack.size();
    }

    public final void nextMapping ( )
    {
        _currentMapping += 8;
    }

    public final String mappingPrefix ( )
    {
        assert hasMapping();
        return _namespaceStack.get( _currentMapping + 6 );
    }

    public final String mappingUri ( )
    {
        assert hasMapping();
        return _namespaceStack.get( _currentMapping + 7 );
    }

    public final void addNewFrameMapping ( String prefix, String uri, boolean ensureDefaultEmpty )
    {
        // If the prefix maps to "", this don't include this mapping 'cause it's not well formed.
        // Also, if we want to make sure that the default namespace is always "", then check that
        // here as well.

        if ((prefix.length() == 0 || uri.length() > 0) &&
            (!ensureDefaultEmpty || prefix.length() > 0 || uri.length() == 0))
        {
            // Make sure that the prefix declaration is not redundant
            // This has the side-effect of making it impossible to set a
            // redundant prefix declaration, but seems that it's better
            // to just never issue a duplicate prefix declaration.
            if (uri.equals(getNamespaceForPrefix(prefix)))
                return;

            // Also make sure the prefix is not already mapped in this frame
            for ( iterateMappings() ; hasMapping() ; nextMapping() )
                if (mappingPrefix().equals( prefix ))
                    return;

            addMapping( prefix, uri );
        }
    }

    public final void addMapping ( String prefix, String uri )
    {
        assert uri != null;
        assert prefix != null;

        // If the prefix being mapped here is already mapped to a uri,
        // that uri will either go out of scope or be mapped to another
        // prefix.

        String renameUri = _prefixMap.get( prefix );
        String renamePrefix = null;

        if (renameUri != null)
        {
            // See if this prefix is already mapped to this uri.  If
            // so, then add to the stack, but there is nothing to rename

            if (renameUri.equals( uri ))
                renameUri = null;
            else
            {
                int i = _namespaceStack.size();

                while ( i > 0 )
                {
                    if (_namespaceStack.get( i - 1 ) == null)
                    {
                        i--;
                        continue;
                    }

                    if (_namespaceStack.get( i - 7 ).equals( renameUri ))
                    {
                        renamePrefix = _namespaceStack.get( i - 8 );

                        if (renamePrefix == null || !renamePrefix.equals( prefix ))
                            break;
                    }

                    i -= 8;
                }

                assert i > 0;
            }
        }

        _namespaceStack.add( _uriMap.get( uri ) );
        _namespaceStack.add( uri );

        if (renameUri != null)
        {
            _namespaceStack.add( _uriMap.get( renameUri ) );
            _namespaceStack.add( renameUri );
        }
        else
        {
            _namespaceStack.add( null );
            _namespaceStack.add( null );
        }

        _namespaceStack.add( prefix );
        _namespaceStack.add( _prefixMap.get( prefix ) );

        _namespaceStack.add( prefix );
        _namespaceStack.add( uri );

        _uriMap.put( uri, prefix );
        _prefixMap.put( prefix, uri );

        if (renameUri != null)
            _uriMap.put( renameUri, renamePrefix );
    }

    public final void pushMappings(boolean ensureDefaultEmpty)
    {
        _namespaceStack.add( null );

        if (ensureDefaultEmpty)
        {
            String defaultUri = _prefixMap.get(EMPTY_STRING);

            // I map the default to "" at the very beginning
            assert defaultUri != null;

            if (defaultUri.length() > 0)
                addMapping(EMPTY_STRING, EMPTY_STRING);
        }
    }

    public final void popMappings ( )
    {
        for ( ; ; )
        {
            int i = _namespaceStack.size();

            if (i == 0)
                break;

            if (_namespaceStack.get( i - 1 ) == null)
            {
                _namespaceStack.remove( i - 1 );
                break;
            }

            String oldUri = _namespaceStack.get( i - 7 );
            String oldPrefix = _namespaceStack.get( i - 8 );

            if (oldPrefix == null)
                _uriMap.remove( oldUri );
            else
                _uriMap.put( oldUri, oldPrefix );

            oldPrefix = _namespaceStack.get( i - 4 );
            oldUri = _namespaceStack.get( i - 3 );

            if (oldUri == null)
                _prefixMap.remove( oldPrefix );
            else
                _prefixMap.put( oldPrefix, oldUri );

            String uri = _namespaceStack.get( i - 5 );

            if (uri != null)
                _uriMap.put( uri, _namespaceStack.get( i - 6 ) );

            // Hahahahahaha -- :-(
            _namespaceStack.remove( i - 1 );
            _namespaceStack.remove( i - 2 );
            _namespaceStack.remove( i - 3 );
            _namespaceStack.remove( i - 4 );
            _namespaceStack.remove( i - 5 );
            _namespaceStack.remove( i - 6 );
            _namespaceStack.remove( i - 7 );
            _namespaceStack.remove( i - 8 );
        }
    }

    final void dumpMappings ( )
    {
        for ( int i = _namespaceStack.size() ; i > 0 ; )
        {
            if (_namespaceStack.get( i - 1 ) == null)
            {
                System.out.println( "----------------" );
                i--;
                continue;
            }

            System.out.print( "Mapping: " );
            System.out.print( _namespaceStack.get( i - 2 ) );
            System.out.print( " -> " );
            System.out.print( _namespaceStack.get( i - 1 ) );
            System.out.println();

            System.out.print( "Prefix Undo: " );
            System.out.print( _namespaceStack.get( i - 4 ) );
            System.out.print( " -> " );
            System.out.print( _namespaceStack.get( i - 3 ) );
            System.out.println();

            System.out.print( "Uri Rename: " );
            System.out.print( _namespaceStack.get( i - 5 ) );
            System.out.print( " -> " );
            System.out.print( _namespaceStack.get( i - 6 ) );
            System.out.println();

            System.out.print( "UriUndo: " );
            System.out.print( _namespaceStack.get( i - 7 ) );
            System.out.print( " -> " );
            System.out.print( _namespaceStack.get( i - 8 ) );
            System.out.println();

            System.out.println();

            i -= 8;
        }
    }

    public final String ensureMapping (
        String uri, String candidatePrefix,
        boolean considerCreatingDefault, boolean mustHavePrefix )
    {
        // Can be called for no-namespaced things

        if (uri == null || uri.length() == 0)
            return null;

        String prefix = _uriMap.get( uri );

        if (prefix != null && (prefix.length() > 0 || !mustHavePrefix))
            return prefix;

        //
        // I try prefixes from a number of places, in order:
        //
        //  1) What was passed in
        //  2) The optional suggestions (for uri's)
        //  3) The default mapping is allowed
        //  4) ns#++
        //

        if (candidatePrefix != null && candidatePrefix.length() == 0)
            candidatePrefix = null;

        if (candidatePrefix == null || !tryPrefix( candidatePrefix ))
        {
            if (_suggestedPrefixes != null &&
                _suggestedPrefixes.containsKey( uri ) &&
                tryPrefix(_suggestedPrefixes.get(uri)))
            {
                candidatePrefix = _suggestedPrefixes.get(uri);
            }
            else if (considerCreatingDefault && _useDefaultNamespace && tryPrefix( "" ))
                candidatePrefix = "";
            else
            {
                String basePrefix = QNameHelper.suggestPrefix( uri );
                candidatePrefix = basePrefix;

                for ( int i = 1 ; ; i++ )
                {
                    if (tryPrefix( candidatePrefix ))
                        break;

                    candidatePrefix = basePrefix + i;
                }
            }
        }

        assert candidatePrefix != null;

        addMapping( candidatePrefix, uri );

        return candidatePrefix;
    }

    public final String getUriMapping ( String uri )
    {
        assert _uriMap.get( uri ) != null;
        return _uriMap.get( uri );
    }

    String getNonDefaultUriMapping ( String uri )
    {
        String prefix = _uriMap.get( uri );

        if (prefix != null && prefix.length() > 0)
            return prefix;

        for (String s : _prefixMap.keySet())
        {
            if (s.length() > 0 && _prefixMap.get(s).equals(uri))
                return s;
        }

        assert false : "Could not find non-default mapping";

        return null;
    }

    private boolean tryPrefix ( String prefix )
    {
        if (prefix == null || beginsWithXml( prefix ))
            return false;

        String existingUri = _prefixMap.get( prefix );

        // If the prefix is currently mapped, then try another prefix.  A
        // special case is that of trying to map the default prefix ("").
        // Here, there always exists a default mapping.  If this is the
        // mapping we found, then remap it anyways. I use != to compare
        // strings because I want to test for the specific initial default
        // uri I added when I initialized the saver.

        if (existingUri != null && (prefix.length() > 0 || existingUri != _initialDefaultUri))
            return false;

        return true;
    }

    public final String getNamespaceForPrefix ( String prefix )
    {
        assert !prefix.equals( "xml" ) || _prefixMap.get( prefix ).equals( Names.URI_XML );

        return _prefixMap.get( prefix );
    }

    public static boolean beginsWithXml(String name)
    {
        if (name.length() < 3)
            return false;

        char ch;

        if (((ch = name.charAt(0)) == 'x' || ch == 'X') &&
            ((ch = name.charAt(1)) == 'm' || ch == 'M') &&
            ((ch = name.charAt(2)) == 'l' || ch == 'L'))
        {
            return true;
        }

        return false;
    }

    // ===============================================
    // NamespaceContext implementation
    // ===============================================
    public String getNamespaceURI(String prefix)
    {
        if (prefix == null)
            throw new IllegalArgumentException("Argument \"prefix\" must not be null");
        if (Names.XMLNS.equals(prefix))
            return Names.URI_DOM_XMLNS;
        String result = getNamespaceForPrefix(prefix);
        if (result == null)
            return Common.EMPTY_STRING; // It's the spec...
        else
            return result;
    }

    public String getPrefix(String namespaceURI)
    {
        if (namespaceURI == null)
            throw new IllegalArgumentException("Argument \"namespaceURI\" must not be null");
        if (namespaceURI.equals(Common.EMPTY_STRING))
            return Common.EMPTY_STRING;
        if (namespaceURI.equals(Names.URI_DOM_XMLNS))
            return Names.XMLNS;
        return _uriMap.get(namespaceURI);
    }

    public Iterator getPrefixes(String namespaceURI)
    {
        if (namespaceURI == null)
            throw new IllegalArgumentException("Argument \"namespaceURI\" must not be null");
        if (namespaceURI.equals(Names.URI_DOM_XMLNS))
            return Collections.singleton(Names.XMLNS).iterator();
        List<String> prefixList = new ArrayList<String>(2);
        // Go through the list of mapping and get all the prefixes that were ever mapped to the URI
        // Out of those, keep the ones that are still mapped
        for (int i = _namespaceStack.size() - 1; i > 0; i -= 8)
        {
            String uri = _namespaceStack.get(i);
            while (uri == null)
                // Frame end marker
                uri = _namespaceStack.get(--i);
            if (namespaceURI.equals(uri))
            {
                String prefix = _namespaceStack.get(i - 1);
                if (_prefixMap.get(prefix) == uri)
                    prefixList.add(prefix);
            }
        }
        return prefixList.iterator();
    }
}
