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

import davos.sdo.impl.common.Common;

import java.io.PrintWriter;
import java.util.List;


/**
 * @author Cezar Andrei (cezar dot andrei at gmail dot com)
 */
public class JavaCodePrinter extends CodePrinter
{
    // static vars

    // member vars


    public JavaCodePrinter(PrintWriter output)
    {
        super(output);
    }

    public JavaCodePrinter(PrintWriter output, int indentIncrement)
    {
        super(output, indentIncrement);
    }


    public void emitSingleLineComment(String s)
    {
        _output.print("// " + s);
    }

    public void emitMultiLineComment(String comment, boolean isJavadoc)
    {
        emitIndent();
        _output.println("/* " + (isJavadoc ? "*" : ""));
        String[] lines = comment.split("\n");
        for (String line : lines)
        {
            emitIndent();
            _output.println(" * " + line);
        }
        emitIndent();
        _output.println(" */");
    }

    public void emitPackage(String packageName)
    {
        emitString("package " + packageName + ";");
        emitNewLine();
    }

    public void emitImport(String importName)
    {
        emitString("import " + importName);
    }

    public void startClass(String className, boolean isAbstract, String implementsInterface)
    {
        startClass(className, isAbstract, false, null, implementsInterface);
    }

    public void startClass(String className, boolean isAbstract, boolean isStatic, String extendsClasses, String implementsInterfaces)
    {
        emitString("public " + (isStatic ? "static " : "") + (isAbstract ? "abstract " : "") +
                "class " + className);
        emitNewLine();
        if (extendsClasses != null)
        {
            indent();
            emitString("extends " + extendsClasses);
            outdent();
            emitNewLine();
        }
        if (implementsInterfaces != null)
        {
            indent();
            emitString("implements " + implementsInterfaces);
            outdent();
            emitNewLine();
        }
        startBlock();
    }

    public void endClass()
    {
        endBlock();
    }

    public void startInterface(String className, List<String> baseInterfaces)
    {
        emitString("public interface " + className);

        String extendsList = "";
        for (int i = 0; i < baseInterfaces.size(); i++)
        {
            String baseIntf = (String) baseInterfaces.get(i);
            if (i==0)
            {
                emitNewLine();
                extendsList += "extends " + baseIntf;
            }
            else
                extendsList += ", " + baseIntf;
        }

        indent();
        emitString(extendsList);
        outdent();

        emitNewLine();
        startBlock();
    }

    public void endInterface()
    {
        endBlock();
    }

    public void emitInterfaceMethod(String returnType, String methodName, List<NamedArg> argList)
    {
        emitInterfaceMethod(returnType, methodName, argList, Common.EMPTY_STRING_LIST);
    }

    public void emitInterfaceMethod(String returnType, String methodName,
        List<NamedArg> argList, List<String> thrownExceptionTypes)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("public " + returnType + " " + methodName + "(");
        int count = 0;
        if (argList != null) {
            for (NamedArg arg : argList) {
                if (count > 0) {
                    sb.append(", ");
                }
                sb.append(arg.getType() + " " + arg.getName());
                count++;
            }
        }
        sb.append(")");

        if (thrownExceptionTypes != null && thrownExceptionTypes.size() > 0)
        {
            emitString(sb.toString());
            emitNewLine();
            // clear StringBuilder
            sb.delete(0, sb.length());

            indent();
            sb.append("throws ");
            count = 0;
            for (String exceptionType : thrownExceptionTypes) {
                if (count > 0) {
                    sb.append(", ");
                }
                sb.append(exceptionType);
                count++;
            }
            sb.append(";");
            emitString(sb.toString());
            outdent();
        }
        else
        {
            sb.append(";");
            emitString(sb.toString());
        }

        emitNewLine();
    }

    public void startMethod(String returnType, String methodName, List<NamedArg> argList)
    {
        startMethod(returnType, methodName, false, argList, Common.EMPTY_STRING_LIST);
    }

    public void startMethod(String returnType, String methodName, boolean isStatic,
        List<NamedArg> argList, List<String> thrownExceptionTypes)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("public " + (isStatic ? "static " : "") + returnType + " " + methodName + "(");
        int count = 0;
        if (argList != null) {
            for (NamedArg arg : argList) {
                if (count > 0) {
                    sb.append(", ");
                }
                sb.append(arg.getType() + " " + arg.getName());
                count++;
            }
        }
        sb.append(")");
        emitString(sb.toString());
        emitNewLine();

        if (thrownExceptionTypes != null && thrownExceptionTypes.size() > 0)
        {
            // clear StringBuilder
            sb.delete(0, sb.length());

            indent();
            sb.append("throws ");
            count = 0;
            for (String exceptionType : thrownExceptionTypes) {
                if (count > 0) {
                    sb.append(", ");
                }
                sb.append(exceptionType);
                count++;
            }
            emitString(sb.toString());
            emitNewLine();
            outdent();
        }

        startBlock();
    }

    public void endMethod()
    {
        endBlock();
    }

    public void startBlock()
    {
        emitIndent();
        _output.println('{');
        indent();
    }

    public void endBlock()
    {
        outdent();
        emitIndent();
        _output.println('}');
    }
}
