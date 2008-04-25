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
package davos.sdo.impl.util;

import davos.sdo.util.Logger;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 */
public class PrintWriterLoggerImpl implements Logger
{
    // static vars
    public static final int SILENT  = 1;
    public static final int ERROR   = 2;
    public static final int WARNING = 3;
    public static final int INFO    = 4;

    private static final String ERR_PREFIX = "ERROR: ";
    private static final String WARN_PREFIX = "WARN: ";
    private static final String INFO_PREFIX = "INFO: ";
    private static final SimpleDateFormat SDF =
        new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    // member vars
    protected PrintWriter _pw;

    protected int _printLevel = ERROR;

    public PrintWriterLoggerImpl(PrintWriter pw)
    {
        _pw = pw;
    }

    public PrintWriterLoggerImpl(PrintWriter pw, int printLevel)
    {
        _pw = pw;
        _printLevel = printLevel;
    }

    private String getPrefix()
    {
        String threadName = Thread.currentThread().getName();
        String currentDateTime = SDF.format(new Date());
        return currentDateTime + " " + threadName + ": ";
    }

    private void writeMessage(String message, Throwable t)
    {
        writeRecursiveMessage(getPrefix(), message, t);
        _pw.flush();
    }

    private void writeRecursiveMessage(String prefix, String message, Throwable t)
    {
        _pw.println(prefix + message);

        if (t != null)
        {
            _pw.println(prefix + "--- Begin Stack Trace ---");
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            t.printStackTrace(pw);
            pw.flush();
            _pw.println(sw.toString());
            _pw.println(prefix + "--- End Stack Trace ---");
            pw.close();

            Throwable cause = t.getCause();
            if (cause != null)
            {
                _pw.println(prefix + ": --- Begin Caused By ---");
                writeRecursiveMessage(prefix, t.getMessage(), cause);
                _pw.println(prefix + ": --- End Caused By ---");
            }
        }
    }

    public void error(String message)
    {
        if (_printLevel>=ERROR)
            writeMessage(ERR_PREFIX + message, null);
    }

    public void warning(String message)
    {
        if (_printLevel>=WARNING)
            writeMessage(WARN_PREFIX + message, null);
    }

    public void info(String message)
    {
        if (_printLevel>=INFO)
            writeMessage(INFO_PREFIX + message, null);
    }

    public void error(String message, Throwable t)
    {
        if (_printLevel>=ERROR)
            writeMessage(ERR_PREFIX + message, t);
    }

    public void warning(String message, Throwable t)
    {
        if (_printLevel>=WARNING)
            writeMessage(WARN_PREFIX + message, t);
    }

    public void info(String message, Throwable t)
    {
        if (_printLevel>=INFO)
            writeMessage(INFO_PREFIX + message, t);
    }
}