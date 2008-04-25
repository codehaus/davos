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

import davos.sdo.binding.BindingContext;
import davos.sdo.impl.util.PrintWriterLoggerImpl;
import davos.sdo.util.Filer;
import davos.sdo.util.Logger;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

/**
 * @author Cezar Andrei (cezar dot andrei at gmail dot com)
 */
public class DefaultBindingContext
    implements BindingContext
{
    private Filer _filer;
    private Logger _logger;
    private String _saveLoadName;

    public DefaultBindingContext(Filer filer, String saveLoadName)
    {
        _filer = filer;
        _saveLoadName = saveLoadName;

        try
        {
            _logger = new PrintWriterLoggerImpl(new PrintWriter(new OutputStreamWriter(System.err, "UTF-8")),
                PrintWriterLoggerImpl.WARNING);
        }
        catch (UnsupportedEncodingException e)
        {
            // UTF-8 is always supported
            throw new RuntimeException(e);
        }
    }

    public DefaultBindingContext(Filer filer, String saveLoadName, Logger logger)
    {
        _filer = filer;
        _saveLoadName = saveLoadName;
        _logger = logger;
    }

    public Filer getFiler()
    {
        return _filer;
    }

    public String getSaveLoadName()
    {
        return _saveLoadName;
    }

    public Logger getLogger()
    {
        return _logger;
    }

    public boolean genIsSetMethods()
    {
        return true;
    }

    public boolean genUnsetMethods()
    {
        return true;
    }

    public boolean genCreateMethods()
    {
        return true;
    }
}
