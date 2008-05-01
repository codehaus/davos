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
package type;

import java.util.List;

import javax.sdo.DataObject;
import javax.sdo.Type;
import javax.sdo.Property;
import javax.sdo.helper.DataFactory;
import javax.sdo.helper.TypeHelper;
import javax.sdo.helper.XSDHelper;
import javax.sdo.helper.XMLHelper;

import junit.framework.*;
import common.BaseTest;

/**
 * @author Wing Yew Poon
 */
public class MetaDataTest extends BaseTest
{
    public MetaDataTest(String name)
    {
        super(name);
    }

    protected static DataFactory factory = context.getDataFactory();
    protected static TypeHelper typeHelper = context.getTypeHelper();
    protected static XSDHelper xsdHelper = context.getXSDHelper();
    protected static XMLHelper xmlHelper = context.getXMLHelper();

    static Type booleanType = typeHelper.getType("commonj.sdo", "Boolean");
    static Type byteType = typeHelper.getType("commonj.sdo", "Byte");
    static Type bytesType = typeHelper.getType("commonj.sdo", "Bytes");
    static Type characterType = typeHelper.getType("commonj.sdo", "Character");
    static Type dateType = typeHelper.getType("commonj.sdo", "Date");
    static Type dateTimeType = typeHelper.getType("commonj.sdo", "DateTime");
    static Type dayType = typeHelper.getType("commonj.sdo", "Day");
    static Type decimalType = typeHelper.getType("commonj.sdo", "Decimal");
    static Type doubleType = typeHelper.getType("commonj.sdo", "Double");
    static Type durationType = typeHelper.getType("commonj.sdo", "Duration");
    static Type floatType = typeHelper.getType("commonj.sdo", "Float");
    static Type intType = typeHelper.getType("commonj.sdo", "Int");
    static Type integerType = typeHelper.getType("commonj.sdo", "Integer");
    static Type longType = typeHelper.getType("commonj.sdo", "Long");
    static Type monthType = typeHelper.getType("commonj.sdo", "Month");
    static Type monthDayType = typeHelper.getType("commonj.sdo", "MonthDay");
    static Type objectType = typeHelper.getType("commonj.sdo", "Object");
    static Type shortType = typeHelper.getType("commonj.sdo", "Short");
    static Type stringType = typeHelper.getType("commonj.sdo", "String");
    static Type stringsType = typeHelper.getType("commonj.sdo", "Strings");
    static Type timeType = typeHelper.getType("commonj.sdo", "Time");
    static Type uriType = typeHelper.getType("commonj.sdo", "URI");
    static Type yearType = typeHelper.getType("commonj.sdo", "Year");
    static Type yearMonthType = typeHelper.getType("commonj.sdo", "YearMonth");
    static Type yearMonthDayType = typeHelper.getType("commonj.sdo", "YearMonthDay");
    static Type booleanObjectType = typeHelper.getType("commonj.sdo/java", "BooleanObject");
    static Type byteObjectType = typeHelper.getType("commonj.sdo/java", "ByteObject");
    static Type characterObjectType = typeHelper.getType("commonj.sdo/java", "CharacterObject");
    static Type doubleObjectType = typeHelper.getType("commonj.sdo/java", "DoubleObject");
    static Type floatObjectType = typeHelper.getType("commonj.sdo/java", "FloatObject");
    static Type intObjectType = typeHelper.getType("commonj.sdo/java", "IntObject");
    static Type longObjectType = typeHelper.getType("commonj.sdo/java", "LongObject");
    static Type shortObjectType = typeHelper.getType("commonj.sdo/java", "ShortObject");

    static Property xmlElement;
    static Property javaClass;
    static
    {
        xmlElement = typeHelper.getOpenContentProperty("commonj.sdo/xml", "xmlElement");
        //if (xmlElement == null) // workaround
        //{
        //    DataObject p = factory.create("commonj.sdo", "Property");
        //    p.set("name", "xmlElement");
        //    p.set("type", booleanType);
        //    xmlElement = typeHelper.defineOpenContentProperty("commonj.sdo/xml", p);
        //}
        javaClass = typeHelper.getOpenContentProperty("commonj.sdo/java", "javaClass");
        //if (javaClass == null) // workaround
        //{
        //    DataObject p = factory.create("commonj.sdo", "Property");
        //    p.set("name", "javaClass");
        //    p.set("type", stringType);
        //    javaClass = typeHelper.defineOpenContentProperty("commonj.sdo/java", p);
        //}
    }

    protected Property findProperty(List<Property> props, String propName)
    {
        Property prop = null;
        for (Property p: props)
        {
            if (p.getName().equals(propName))
            {
                prop = p;
                break;
            }
        }
        return prop;
    }

}
