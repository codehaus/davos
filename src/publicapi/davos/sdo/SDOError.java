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

import java.text.MessageFormat;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import org.xml.sax.Locator;


/**
 * Represents a particular user-facing message, usually for an exception or
 * an error reported during SDO compilation
 */
public class SDOError
{
    private static final ResourceBundle _bundle = PropertyResourceBundle.getBundle("davos.sdo.message");

    public static final String messageForCode(String errorCode,Object... args)
    {
        return messageForCodeAndLocation(errorCode, null, args);
    }

    public static final String messageForCodeAndLocation(String errorCode, Locator location, Object... args)
    {
        if (errorCode == null)
            return null;

        String message;

        try
        {
            message = MessageFormat.format(_bundle.getString(errorCode), args);
        }
        catch (java.util.MissingResourceException e)
        {
            return MessageFormat.format(_bundle.getString("message.missing.resource"),
                new Object[] { e.getMessage() });
        }
        catch (IllegalArgumentException e)
        {
            return MessageFormat.format(_bundle.getString("message.pattern.invalid"),
                new Object[] { e.getMessage() });
        }

        if (location != null)
        {
            StringBuilder locationAsString = new StringBuilder("(");
            if (location.getSystemId() != null)
                locationAsString.append(location.getSystemId()).append(':');
            if (location.getLineNumber() >= 0)
            {
                locationAsString.append(location.getLineNumber());
                if (location.getColumnNumber() >= 0)
                    locationAsString.append(',').append(location.getColumnNumber());
            }
            if (locationAsString.length() > 1)
            {
                locationAsString.append(')').append(message);
                message = locationAsString.toString();
            }
        }

        return message;
    }
}
