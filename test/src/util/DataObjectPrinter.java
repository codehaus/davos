/*   Copyright 2008 BEA Systems, Inc.
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
 *   limitations under the License.
 */
package util;

import java.io.*;
import java.util.List;

import javax.sdo.DataObject;
import javax.sdo.Type;
import javax.sdo.Property;
import javax.sdo.Sequence;
import javax.sdo.helper.XMLDocument;
import javax.sdo.helper.XMLHelper;

/**
 * @author Wing Yew Poon
 */
public class DataObjectPrinter
{
    public static void main(String[] args) throws Exception
    {
        if (args.length != 1)
        {
            System.out.println("USAGE: java util.DataObjectPrinter <xml file>");
            System.exit(1);
        }
        File f = new File(args[0]);
        InputStream in = new FileInputStream(f);
        XMLDocument doc = XMLHelper.INSTANCE.load(in);
        DataObject root = doc.getRootObject();
        System.out.println(doc.getRootElementName() + "@" + 
                           doc.getRootElementURI());
        printDataObject(root);
        in.close();
    }

    public static void printDataObject(DataObject dataObject)
    {
        printDataObject(dataObject, 0);
    }

    public static void printDataObject2(DataObject dataObject)
    {
        printDataObject2(dataObject, 0);
    }

    public static void printDataObject(DataObject dataObject, int indent)
    {
        Type type = dataObject.getType();
        printType(type, indent);
        // For each Property
        List properties = dataObject.getInstanceProperties();
        for (int i = 0, size = properties.size(); i < size; i++)
        {
            //System.out.println(indent + ":" + i);
            if (dataObject.isSet(i))
            {
                Property property = (Property) properties.get(i);
                if (property.isMany())
                {
                    System.out.println(property.getName() + " is many-valued");
                    // For many-valued properties, process a list of values
                    List values = dataObject.getList(i); //(i);
                    for (int v=0, count=values.size(); v < count; v++)
                    {
                        printValue(values.get(v), property, indent);
                    }
                }
                else
                {
                    // For single-valued properties, print out the value
                    printValue(dataObject.get(i), property, indent);
                }
            }
        }
    }

    /* use sequence if DataObject's Type is sequenced */
    public static void printDataObject2(DataObject dataObject, int indent)
    {
        Type type = dataObject.getType();
        printType(type, indent);
        if (type.isSequenced())
        {
            Sequence seq = dataObject.getSequence();
            int size = seq.size();
            for (int i = 0; i < size; i++)
            {
                Property property = seq.getProperty(i);
                Object value = seq.getValue(i);
                printValue2(value, property, indent);
            }
        }
        else
        {
            // For each Property
            List properties = dataObject.getInstanceProperties();
            for (int p = 0, size = properties.size(); p < size; p++)
            {
                //System.out.println(indent + ":" + p);
                if (dataObject.isSet(p))
                {
                    Property property = (Property) properties.get(p);
                    if (property.isMany())
                    {
                        System.out.println(property.getName() + " is many-valued");
                        // For many-valued properties, process a list of values
                        List values = dataObject.getList(property); //(p);
                        for (int v = 0, count = values.size(); v < count; v++)
                        {
                            printValue2(values.get(v), property, indent);
                        }
                    }
                    else
                    {
                        // For single-valued properties, print out the value
                        printValue2(dataObject.get(p), property, indent);
                    }
                }
            }
        }
    }

    private static void printType(Type t, int indent)
    {
        String margin = "";
        for (int i = 0; i < indent; i++)
            margin += "  ";
        System.out.println(margin + "type: " + 
                           t.getName() + 
                           "@" + t.getURI() + 
                           " (" + t.getInstanceClass() + ")" + 
                           " - " + 
                           (t.isAbstract() ? "abstract " : "") + 
                           (t.isDataType() ? "datatype " : "") + 
                           (t.isSequenced() ? "sequenced " : "") + 
                           (t.isOpen() ? "open" : ""));
    }

    private static void printValue(Object value, Property property, int indent)
    {
        // Get the name of the property
        String propertyName = property.getName();
        // Construct a string for the proper indentation
        String margin = "";
        for (int i = 0; i < indent; i++)
            margin += "  ";
        Type containingType = property.getContainingType();
        //System.out.println(margin + "[containing type: " + 
        //                   containingType.getName() + "@" +
        //                   containingType.getURI() + "]");
        if (value != null && property.isContainment())
        {
            // For containment properties, display the value
            // with printDataObject
            Type type = property.getType();
            String typeName = type.getName();
            String typeURI = type.getURI();
            if (value instanceof DataObject)
            {
                System.out.println(margin + propertyName + ":");
                                   //" (" + typeName + "@" + typeURI + "):");
                printDataObject((DataObject) value, indent + 1);
            }
            else
            {
                System.out.println(margin + propertyName + ": " +
                                   //" (" + typeName + "@" + typeURI + "): " + 
                                   nice(value));
            }
        }
        else
        {
            // For non-containment properties, just print the value
            System.out.println(margin + propertyName + ": " + nice(value));
        }
    }

    private static String nice(Object value)
    {
        String v = "'" + value + "'";
        return v.replaceAll("\n", "\\n").replaceAll("\t", "\\t");
    }

    private static void printValue2(Object value, Property property, int indent)
    {
        // Get the name of the property
        String propertyName = 
            ((property != null) ? property.getName() : "[text]");
        // Construct a string for the proper indentation
        String margin = "";
        for (int i = 0; i < indent; i++)
            margin += "  ";
        //Type containingType = property.getContainingType();
        //System.out.println(margin + "[containing type: " + 
        //                   containingType.getName() + "@" +
        //                   containingType.getURI() + "]");
        if (value != null && property != null && property.isContainment())
        {
            // For containment properties, display the value
            // with printDataObject
            Type type = property.getType();
            String typeName = type.getName();
            String typeURI = type.getURI();
            if (value instanceof DataObject)
            {
                System.out.println(margin + propertyName + ":");
                                   //" (" + typeName + "@" + typeURI + "):");
                printDataObject2((DataObject) value, indent + 1);
            }
            else
            {
                System.out.println(margin + propertyName + ": " +
                                   //" (" + typeName + "@" + typeURI + "): " + 
                                   nice(value));
            }
        }
        else
        {
            // For non-containment properties, just print the value
            System.out.println(margin + propertyName + ": " + nice(value));
        }
    }
}
