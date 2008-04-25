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

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.io.IOException;
import java.util.Map;
import java.util.Stack;

import org.apache.xmlbeans.impl.common.EncodingMap;

import javax.sdo.DataObject;

import davos.sdo.Options;
import davos.sdo.SDOContext;
import davos.sdo.SDOError;
import davos.sdo.SDOXmlException;
import davos.sdo.impl.common.Names;
import davos.sdo.impl.common.NamespaceStack;
import davos.sdo.impl.data.DataObjectImpl;


public class WriterSaver extends Saver
{
    private static final String LT = "&lt;";
    private static final String GT = "&gt;";
    private static final String AMP = "&amp;";
    private static final String QUOT = "&quot;";
    private static final String NEWLINE = getSafeNewLine();
    private static final boolean SPECIAL_NEWLINE = !NEWLINE.equals(Marshaller.NEWLINE);

    private NamespaceStack _nsstack = new NamespaceStack(null, false);
    private boolean _elStarted;
    private boolean _textStarted = true;
    private Stack<char[]> _elStack = new Stack<char[]>();
    // Used specifically for the XML declaration
    private boolean _indent;

    private Writer _w;
    private OutputStream _os;

    public WriterSaver(Writer outputWriter)
    {
        _w = outputWriter;
    }

    public WriterSaver(OutputStream outputStream)
    {
        _os = outputStream;
    }

    /*
     * @see davos.sdo.impl.marshal.Saver#save(javax.sdo.DataObject, java.lang.String, java.lang.String, java.lang.String, java.lang.Object)
     */
    public void save(DataObject rootObject,
            String rootElementURI, String rootElementName,
            boolean isXmlDecl, String xmlVersion, String encoding,
            String schemaLocation, String noNSSchemaLocation,
            Object options, SDOContext sdoContext) throws IOException
    {
// moved down in PlainMarshaller when we first detect it         
//        if (rootObject instanceof DataObjectImpl && ((DataObjectImpl)rootObject).getSDOContext()!=sdoContext)
//            throw new IllegalArgumentException("Trying to use a diffrent context than the one it was created.");

        if (_w == null)
        {
            String javaEncoding = encoding == null ? "UTF-8" :
                EncodingMap.getIANA2JavaMapping(encoding);
            if (javaEncoding == null)
                throw new SDOXmlException(SDOError.messageForCode(
                    "xml.writer.encoding.unrecognized", encoding));
            try
            {
                _w = new OutputStreamWriter(_os, javaEncoding);
            }
            catch (UnsupportedEncodingException uee)
            {
                throw new SDOXmlException(SDOError.messageForCode(
                    "xml.writer.encoding.unsupported", encoding), uee);
            }
        }
        Map map = null;
        if (options instanceof Map)
            map = (Map) options;
        else if (options instanceof Options)
            map = ((Options) options).getMap();
        if (map != null && map.containsKey(Options.SAVE_PRETTY_PRINT))
                _indent = true;
        try
        {
            super.save(rootObject, rootElementURI, rootElementName,
                isXmlDecl, xmlVersion, encoding,
                schemaLocation, noNSSchemaLocation,
                options, sdoContext);
        }
        catch (WrapperException we)
        {
            throw (IOException) we.getCause();
        }
        _w.flush();
    }

    public void startElement(String uri, String name, String prefix,
            String xsiTypeUri, String xsiTypeName)
    {
        try
        {
            if (_textStarted)
            {
                _textStarted = false;
            }
            else
            {
                if (_elStarted)
                    finishElement(false);
            }

            _nsstack.pushMappings(false);
            String elPrefix = _nsstack.ensureMapping(uri, prefix, false, true);
            String xsiPrefix = null;
            String schemaInstancePrefix = null;
            if (xsiTypeName != null)
            {
                xsiPrefix = _nsstack.ensureMapping(xsiTypeUri, null, false, false);
                schemaInstancePrefix = _nsstack.ensureMapping(Names.URI_XSD_INSTANCE,
                    Names.PREFIX_XSD_INSTANCE, false, false);
            }

            _w.write('<');
            int nameLength = name.length();
            int elPrefixLength = elPrefix==null ? 0 : elPrefix.length();
            char[] elName;
            if (elPrefixLength > 0)
            {
                elName = new char[elPrefixLength + 1 + nameLength];
                elPrefix.getChars(0, elPrefixLength, elName, 0);
                elName[elPrefixLength] = ':';
                name.getChars(0, nameLength, elName, elPrefixLength + 1);
                _w.write(elName);
            }
            else
            {
                elName = new char[nameLength];
                name.getChars(0, nameLength, elName, 0);
                _w.write(elName);
            }
            if (xsiTypeName != null)
            {
                _w.write(' ');
                _w.write(schemaInstancePrefix);
                _w.write(':');
                _w.write(Names.XSI_TYPE);
                _w.write('=');
                _w.write('"');
                if (xsiPrefix != null)
                {
                    _w.write(xsiPrefix);
                    _w.write(':');
                }
                _w.write(xsiTypeName);
                _w.write('"');
            }
            _elStack.push(elName);
        }
        catch (IOException e)
        {
            throw new WrapperException(e);
        }
        _elStarted = true;
    }

    private void finishElement(boolean endTag)
    {
        _elStarted = false;
        try
        {
            for (_nsstack.iterateMappings(); _nsstack.hasMapping(); _nsstack.nextMapping())
            {
                String prefix = _nsstack.mappingPrefix();
                String uri = _nsstack.mappingUri();
                _w.write(' ');
                if (prefix == null || prefix.length() == 0)
                    _w.write(Names.XMLNS);
                else
                {
                    _w.write(Names.XMLNS);
                    _w.write(':');
                    _w.write(prefix);
                }
                _w.write('=');
                _w.write('"');
                _w.write(uri);
                _w.write('"');
            }
            if (endTag)
                _w.write('/');
            _w.write('>');
        }
        catch (IOException e)
        {
            throw new WrapperException(e);
        }
    }

    public void attr(String uri, String local, String prefix, String value)
    {
        String attPrefix = _nsstack.ensureMapping(uri, prefix, false, true);
        try
        {
            _w.write(' ');
            if (attPrefix != null)
            {
                _w.write(attPrefix);
                _w.write(':');
            }
            _w.write(local);
            _w.write('=');
            _w.write('"');
            // Escape attribute values
            int pos = 0;
            for (int i = 0; i < value.length(); i++)
            {
                switch (value.charAt(i))
                {
                case '<':
                    _w.write(value, pos, i - pos);
                    _w.write(LT);
                    pos = i + 1;
                    break;
                case '>':
                    _w.write(value, pos, i - pos);
                    _w.write(GT);
                    pos = i + 1;
                    break;
                case '&':
                    _w.write(value, pos, i - pos);
                    _w.write(AMP);
                    pos = i + 1;
                    break;
                case '"':
                    _w.write(value, pos, i - pos);
                    _w.write(QUOT);
                    pos = i + 1;
                    break;
                }
            }
            _w.write(value, pos, value.length() - pos);
            _w.write('"');
        }
        catch (IOException e)
        {
            throw new WrapperException(e);
        }
    }

    public void sattr(int type, String name, String value)
    {
        switch (type)
        {
        case SDOEventModel.ATTR_XSI:
            String attPrefix = _nsstack.ensureMapping(Names.URI_XSD_INSTANCE,
                Names.PREFIX_XSD_INSTANCE, false, true);
            try
            {
                _w.write(' ');
                _w.write(attPrefix);
                _w.write(':');
                _w.write(name);
                _w.write('=');
                _w.write('"');
                _w.write(value);
                _w.write('"');
            }
            catch (IOException e)
            {
                throw new WrapperException(e);
            }
            break;
        }
    }

    public void endElement()
    {
        char[] elName = _elStack.pop();
        try
        {
            if (_elStarted)
            {
                finishElement(true);
            }
            else
            {
                if (_textStarted)
                {
                    _textStarted = false;
                }
                _w.write('<');
                _w.write('/');
                _w.write(elName);
                _w.write('>');
            }
        }
        catch (IOException e)
        {
            throw new WrapperException(e);
        }
        _nsstack.popMappings();
    }

    public void text(char[] buff, int off, int cch)
    {
        if (_elStarted)
            finishElement(false);
        try
        {
            // Escape text
            int pos = off;
            for (int i = off; i < off + cch; i++)
            {
                switch (buff[i])
                {
                case '<':
                    _w.write(buff, pos, i - pos);
                    _w.write(LT);
                    pos = i + 1;
                    break;
                case '>':
                    _w.write(buff, pos, i - pos);
                    _w.write(GT);
                    pos = i + 1;
                    break;
                case '&':
                    _w.write(buff, pos, i - pos);
                    _w.write(AMP);
                    pos = i + 1;
                    break;
                case '\n':
                    // We need to make sure we write \r\n on Windows: lame
                    if (SPECIAL_NEWLINE)
                    {
                        _w.write(buff, pos, i - pos);
                        _w.write(NEWLINE);
                        pos = i + 1;
                    }
                    break;
                }
            }
            _w.write(buff, pos, cch - pos + off);
        }
        catch (IOException e)
        {
            throw new WrapperException(e);
        }
        _textStarted = true;
    }

    public void text(String s)
    {
        if (_elStarted)
            finishElement(false);
        try
        {
            // Escape text
            int pos = 0;
            for (int i = 0; i < s.length(); i++)
            {
                switch (s.charAt(i))
                {
                case '<':
                    _w.write(s, pos, i - pos);
                    _w.write(LT);
                    pos = i + 1;
                    break;
                case '>':
                    _w.write(s, pos, i - pos);
                    _w.write(GT);
                    pos = i + 1;
                    break;
                case '&':
                    _w.write(s, pos, i - pos);
                    _w.write(AMP);
                    pos = i + 1;
                    break;
                case '\n':
                    // We need to make sure we write \r\n on Windows: lame
                    if (SPECIAL_NEWLINE)
                    {
                        _w.write(s, pos, i - pos);
                        _w.write(NEWLINE);
                        pos = i + 1;
                    }
                    break;
                }
            }
            _w.write(s, pos, s.length() - pos);
        }
        catch (IOException e)
        {
            throw new WrapperException(e);
        }
        _textStarted = true;
    }

    public void xmlns(String prefix, String uri)
    {
        try
        {
            _w.write(' ');
            if (prefix == null || prefix.length() == 0)
                _w.write(Names.XMLNS);
            else
            {
                _w.write(Names.XMLNS);
                _w.write(':');
                _w.write(prefix);
            }
            _w.write('=');
            _w.write('"');
            _w.write(uri);
            _w.write('"');
            _nsstack.ensureMapping(uri, prefix, false, true);
        }
        catch (IOException e)
        {
            throw new WrapperException(e);
        }
    }

    public void xmlDecl(String version, String encoding)
    {
        try
        {
            _w.write("<?xml");
            _w.write(" version=\"");
            if (version != null)
                _w.write(version);
            else
                _w.write("1.0");
            _w.write('"');
            if (encoding != null)
            {
                _w.write(" encoding=\"");
                _w.write(encoding);
                _w.write('"');
            }
            _w.write('?');
            _w.write('>');
            if (_indent)
                _w.write(NEWLINE);
        }
        catch (IOException e)
        {
            throw new WrapperException(e);
        }
    }

    NamespaceStack getNamespaceStack()
    {
        return _nsstack;
    }

    public static String getSafeNewLine()
    {
        String newline;
        try
        {
            newline = System.getProperty("line.separator");
        }
        catch (Exception e)
        {
            newline = "\n";
        }
        return newline;
    }

    private static class WrapperException extends RuntimeException
    {
        WrapperException(IOException e)
        {
            super(e);
        }
    }
}
