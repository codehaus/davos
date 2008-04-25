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

import javax.sdo.DataObject;
import davos.sdo.SDOContext;

/**
 * @author Cezar Andrei (cezar dot andrei at gmail dot com)
 *         Date: Jul 11, 2006
 */
public class Path
{
    public static final int OP_GET   = 1;
    public static final int OP_SET   = 2;
    public static final int OP_ISSET = 3;
    public static final int OP_UNSET = 4;

    public static final int SETTER_UNTYPED    = 1;
    public static final int SETTER_BOOLEAN    = 2;
    public static final int SETTER_BYTE       = 3;
    public static final int SETTER_CHAR       = 4;
    public static final int SETTER_DOUBLE     = 5;
    public static final int SETTER_INT        = 6;
    public static final int SETTER_FLOAT      = 7;
    public static final int SETTER_LONG       = 8;
    public static final int SETTER_SHORT      = 9;
    public static final int SETTER_BYTES      = 10;
    public static final int SETTER_BIGDECIMAL = 11;
    public static final int SETTER_BIGINTEGER = 12;
    public static final int SETTER_DATAOBJECT = 13;
    public static final int SETTER_DATE       = 14;
    public static final int SETTER_STRING     = 15;
    public static final int SETTER_LIST       = 16;


    private int _operation;
    private int _typedSetter;    // what kind of typed setter was called, contains an interesting value only if operation is SET
    private boolean _succesfull;
    private Object _value;          // if op is GET _value will contain the result value
                                    // if op is SET _value will contain the value to be set
                                    // id op is ISSET _value will contain the result
    private SDOContext _sdoContext; // sdoContext of the parent, it's needed if an onDemandProp needs to be created
    private DataObject _parentNode;
    private PathPlan _plan;

    private Path()
    {}

    public static PathPlan prepare(String pathExpression)
        throws Parser.SDOPathException
    {
        return new Parser().parse(pathExpression);
    }

    public static Path execute(DataObject currentNode, String pathExpression, int operation)
        throws Parser.SDOPathException
    {
        PathPlan plan = new Parser().parse(pathExpression);

        Path context = new Path();
        context._operation = operation;
        context._parentNode = currentNode;
        context._plan = plan;

        context._succesfull = plan.execute(context);

        if (!context._succesfull)
        {
            context._value = null;
            context._parentNode = null;
        }

        return context;
    }

    public static Object executeGet(DataObject currentNode, String pathExpression)
    {
        try
        {
            Path rez = execute(currentNode, pathExpression, OP_GET);
            return rez.isSuccesfull() ? rez.getValue() : null;
        }
        catch (Parser.SDOPathException e)
        {   //do nothing just
            return null;
        }
        catch (IllegalArgumentException e)
        {   // do nothing just
            return false;
        }
    }

    public static void executeSet(SDOContext sdoContext, DataObject currentNode, String pathExpression, Object value)
    {
        executeSet(sdoContext, currentNode, pathExpression, Path.SETTER_UNTYPED, value);
    }

    public static void executeSet(SDOContext sdoContext, DataObject currentNode, String pathExpression, int typedSetter, Object value)
    {
        try
        {
            PathPlan plan = new Parser().parse(pathExpression);

            Path context = new Path();
            context._operation = OP_SET;
            //assert sdoContext==((DataObjectImpl)currentNode).getSDOContext();
            context._sdoContext = sdoContext;
            context._parentNode = currentNode;
            context._value = value;
            context._typedSetter = typedSetter;

            context._succesfull = plan.execute(context);
        }
        catch (Parser.SDOPathException e)
        {
            throw new IllegalArgumentException(e);
        }
    }

    public static void executeUnset(DataObject currentNode, String pathExpression)
    {
        try
        {
            Path rez = execute(currentNode, pathExpression, OP_UNSET);
            rez.isSuccesfull();
        }
        catch (Parser.SDOPathException e)
        {
            throw new IllegalArgumentException(e);
        }
    }

    public static boolean executeIsSet(DataObject currentNode, String pathExpression)
    {
        try
        {
            Path rez = execute(currentNode, pathExpression, OP_ISSET);
            return rez.isSuccesfull();
        }
        catch (Parser.SDOPathException e)
        {   // do nothing just
            return false;
        }
        catch (IllegalArgumentException e)
        {   // do nothing just
            return false;
        }
    }

    int getOperation()
    {
        return _operation;
    }

    void setOperation(int op)
    {
        _operation = op;
    }

    int getTypedSetter()
    {
        return _typedSetter;
    }

    void setTypedSetter(int typedSetter)
    {
        _typedSetter = typedSetter;
    }

    public boolean isSuccesfull()
    {
        return _succesfull;
    }

    public Object getValue()
    {
        return _value;
    }

    void setValue(Object result)
    {
        _value = result;
    }

    public SDOContext getSDOContext()
    {
        return _sdoContext;
    }

    public DataObject getParentNode()
    {
        return _parentNode;
    }

    void setParentNode(DataObject currentNode)
    {
        _parentNode = currentNode;
    }

    public String toString()
    {
        return "Path:\n  Op: " + (_operation==OP_GET ? "GET" : "SET") + "\n  Succ: " + _succesfull +
            "\n  Res: " + _value + "\n  CurrentNode: " + _parentNode;
    }

    public boolean canCreateOnDemandProperty()
    {
        if (_succesfull)
            return false;

        return _plan.isSimplePropertyName();
    }
    
    public String getOnDemandPropertyName()
    {
        assert canCreateOnDemandProperty();

        return _plan.getSimplePropertyName();
    }
}
