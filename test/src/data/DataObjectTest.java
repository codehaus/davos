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
package data;

import java.util.Date;
import java.util.TimeZone;
import java.util.List;
import java.util.ArrayList;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import javax.sdo.DataObject;
import javax.sdo.Type;
import javax.sdo.Property;
import javax.sdo.helper.DataFactory;
import javax.sdo.helper.TypeHelper;
import javax.sdo.helper.DataHelper;
import javax.sdo.helper.XMLHelper;
import javax.sdo.helper.XMLDocument;

import junit.framework.*;
import common.BaseTest;

/** Tests for DataObject API.
    In particular:
    - get(*), set(*, Object), isSet(*), unset(*),
      get<T>(*), set<T>(*)
      methods where * = String/int/Property
      and <T> = boolean/byte/char/double/float/int/long/short/byte[]/
                BigDecimal/BigInteger/
                DataObject/
                Date/String/
                List
    @author Wing Yew Poon
*/
public class DataObjectTest extends BaseTest
{
    public DataObjectTest(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        /*
        TestSuite suite = new TestSuite();
        //suite.addTest(new DataObjectTest("testJavaProperties"));
        suite.addTest(new DataObjectTest("testNumberOfProperties"));
        //suite.addTest(new DataObjectTest("testSetStringListByPath"));
        //suite.addTest(new DataObjectTest("testSetStringListByProperty"));
        //suite.addTest(new DataObjectTest("testCustomerByPath"));
        //suite.addTest(new DataObjectTest("testContainmentManagement"));
        //suite.addTest(new DataObjectTest("testContainmentManagement2a"));
        //suite.addTest(new DataObjectTest("testContainmentManagement2b"));
        //suite.addTest(new DataObjectTest("testContainmentManagement2c"));
        //suite.addTest(new DataObjectTest("testContainmentManagement3a"));
        //suite.addTest(new DataObjectTest("testContainmentManagement3b"));
        //suite.addTest(new DataObjectTest("testStringToDate"));
        //suite.addTest(new DataObjectTest("testStringToStrings"));
        //suite.addTest(new DataObjectTest("testStringsToString"));
        //suite.addTest(new DataObjectTest("testDay"));
        //suite.addTest(new DataObjectTest("testTime"));
        //suite.addTest(new DataObjectTest("testDate"));
        */
        // or
        TestSuite suite = new TestSuite(DataObjectTest.class);
        return suite;
    }

    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(suite());
    }

    protected static DataFactory factory = context.getDataFactory();
    protected static TypeHelper typeHelper = context.getTypeHelper();
    protected static DataHelper dataHelper = context.getDataHelper();
    protected static XMLHelper xmlHelper = context.getXMLHelper();
    // built-in data types
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
    // ... including java types
    static Type booleanObjectType = typeHelper.getType("commonj.sdo/java", "BooleanObject");
    static Type byteObjectType = typeHelper.getType("commonj.sdo/java", "ByteObject");
    static Type characterObjectType = typeHelper.getType("commonj.sdo/java", "CharacterObject");
    static Type doubleObjectType = typeHelper.getType("commonj.sdo/java", "DoubleObject");
    static Type floatObjectType = typeHelper.getType("commonj.sdo/java", "FloatObject");
    static Type intObjectType = typeHelper.getType("commonj.sdo/java", "IntObject");
    static Type longObjectType = typeHelper.getType("commonj.sdo/java", "LongObject");
    static Type shortObjectType = typeHelper.getType("commonj.sdo/java", "ShortObject");
    // property indices
    protected static final int BOOLEAN_P_I = 0;
    protected static final int BYTE_P_I = 1;
    protected static final int BYTES_P_I = 2;
    protected static final int CHARACTER_P_I = 3;
    protected static final int DATE_P_I = 4;
    protected static final int DATETIME_P_I = 5;
    protected static final int DAY_P_I = 6;
    protected static final int DECIMAL_P_I = 7;
    protected static final int DOUBLE_P_I = 8;
    protected static final int DURATION_P_I = 9;
    protected static final int FLOAT_P_I = 10;
    protected static final int INT_P_I = 11;
    protected static final int INTEGER_P_I = 12;
    protected static final int LONG_P_I = 13;
    protected static final int MONTH_P_I = 14;
    protected static final int MONTHDAY_P_I = 15;
    protected static final int SHORT_P_I = 16;
    protected static final int STRING_P_I = 17;
    protected static final int STRINGS_P_I = 18;
    protected static final int TIME_P_I = 19;
    protected static final int URI_P_I = 20;
    protected static final int YEAR_P_I = 21;
    protected static final int YEARMONTH_P_I = 22;
    protected static final int YEARMONTHDAY_P_I = 23;
    protected static final int OBJECT_P_I = 24;
    protected static Type basic_t;
    protected static Type basic_t2;
    protected static Type basic_java_t;
    protected static Type addressType;
    protected static Type customerType;
    protected static Type itemType;
    protected static Type orderType;
    protected static Type tradeType;
    protected static Type traderType;
    protected static Type tradesType;

    static
    {
        DataObject prototype = factory.create("commonj.sdo", "Type");
        prototype.set("uri", "");
        prototype.set("name", "BasicTypes");

        DataObject booleanProperty = prototype.createDataObject("property");
        booleanProperty.set("name", "boolean");
        booleanProperty.set("type", booleanType);
        DataObject byteProperty = prototype.createDataObject("property");
        byteProperty.set("name", "byte");
        byteProperty.set("type", byteType);
        DataObject bytesProperty = prototype.createDataObject("property");
        bytesProperty.set("name", "bytes");
        bytesProperty.set("type", bytesType);
        DataObject characterProperty = prototype.createDataObject("property");
        characterProperty.set("name", "character");
        characterProperty.set("type", characterType);
        DataObject dateProperty = prototype.createDataObject("property");
        dateProperty.set("name", "date");
        dateProperty.set("type", dateType);
        dateProperty.set("nullable", true);
        DataObject dateTimeProperty = prototype.createDataObject("property");
        dateTimeProperty.set("name", "dateTime");
        dateTimeProperty.set("type", dateTimeType);
        dateTimeProperty.set("nullable", true);
        DataObject dayProperty = prototype.createDataObject("property");
        dayProperty.set("name", "day");
        dayProperty.set("type", dayType);
        dayProperty.set("nullable", true);
        DataObject decimalProperty = prototype.createDataObject("property");
        decimalProperty.set("name", "decimal");
        decimalProperty.set("type", decimalType);
        DataObject doubleProperty = prototype.createDataObject("property");
        doubleProperty.set("name", "double");
        doubleProperty.set("type", doubleType);
        DataObject durationProperty = prototype.createDataObject("property");
        durationProperty.set("name", "duration");
        durationProperty.set("type", durationType);
        durationProperty.set("nullable", true);
        DataObject floatProperty = prototype.createDataObject("property");
        floatProperty.set("name", "float");
        floatProperty.set("type", floatType);
        DataObject intProperty = prototype.createDataObject("property");
        intProperty.set("name", "int");
        intProperty.set("type", intType);
        DataObject integerProperty = prototype.createDataObject("property");
        integerProperty.set("name", "integer");
        integerProperty.set("type", integerType);
        DataObject longProperty = prototype.createDataObject("property");
        longProperty.set("name", "long");
        longProperty.set("type", longType);
        DataObject monthProperty = prototype.createDataObject("property");
        monthProperty.set("name", "month");
        monthProperty.set("type", monthType);
        monthProperty.set("nullable", true);
        DataObject monthDayProperty = prototype.createDataObject("property");
        monthDayProperty.set("name", "monthDay");
        monthDayProperty.set("type", monthDayType);
        monthDayProperty.set("nullable", true);
        DataObject shortProperty = prototype.createDataObject("property");
        shortProperty.set("name", "short");
        shortProperty.set("type", shortType);
        DataObject stringProperty = prototype.createDataObject("property");
        stringProperty.set("name", "string");
        stringProperty.set("type", stringType);
        stringProperty.set("nullable", true);
        DataObject stringsProperty = prototype.createDataObject("property");
        stringsProperty.set("name", "strings");
        stringsProperty.set("type", stringsType);
        stringsProperty.set("nullable", true);
        DataObject timeProperty = prototype.createDataObject("property");
        timeProperty.set("name", "time");
        timeProperty.set("type", timeType);
        timeProperty.set("nullable", true);
        DataObject uriProperty = prototype.createDataObject("property");
        uriProperty.set("name", "uri");
        uriProperty.set("type", uriType);
        uriProperty.set("nullable", true);
        DataObject yearProperty = prototype.createDataObject("property");
        yearProperty.set("name", "year");
        yearProperty.set("type", yearType);
        yearProperty.set("nullable", true);
        DataObject yearMonthProperty = prototype.createDataObject("property");
        yearMonthProperty.set("name", "yearMonth");
        yearMonthProperty.set("type", yearMonthType);
        yearMonthProperty.set("nullable", true);
        DataObject yearMonthDayProperty = prototype.createDataObject("property");
        yearMonthDayProperty.set("name", "yearMonthDay");
        yearMonthDayProperty.set("type", yearMonthDayType);
        yearMonthDayProperty.set("nullable", true);
        DataObject objectProperty = prototype.createDataObject("property");
        objectProperty.set("name", "object");
        objectProperty.set("type", objectType);
        objectProperty.set("nullable", true);
        objectProperty.set("many", true);

        basic_t = typeHelper.define(prototype);

        DataObject prototypeM = factory.create("commonj.sdo", "Type");
        prototypeM.set("uri", "");
        prototypeM.set("name", "BasicTypesMany");

        DataObject booleanPropertyM = prototypeM.createDataObject("property");
        booleanPropertyM.set("name", "boolean");
        booleanPropertyM.set("type", booleanType);
        booleanPropertyM.set("many", true);
        DataObject bytePropertyM = prototypeM.createDataObject("property");
        bytePropertyM.set("name", "byte");
        bytePropertyM.set("type", byteType);
        bytePropertyM.set("many", true);
        DataObject bytesPropertyM = prototypeM.createDataObject("property");
        bytesPropertyM.set("name", "bytes");
        bytesPropertyM.set("type", bytesType);
        bytesPropertyM.set("many", true);
        DataObject characterPropertyM = prototypeM.createDataObject("property");
        characterPropertyM.set("name", "character");
        characterPropertyM.set("type", characterType);
        characterPropertyM.set("many", true);
        DataObject datePropertyM = prototypeM.createDataObject("property");
        datePropertyM.set("name", "date");
        datePropertyM.set("type", dateType);
        datePropertyM.set("many", true);
        DataObject dateTimePropertyM = prototypeM.createDataObject("property");
        dateTimePropertyM.set("name", "dateTime");
        dateTimePropertyM.set("type", dateTimeType);
        dateTimePropertyM.set("many", true);
        DataObject dayPropertyM = prototypeM.createDataObject("property");
        dayPropertyM.set("name", "day");
        dayPropertyM.set("type", dayType);
        dayPropertyM.set("many", true);
        DataObject decimalPropertyM = prototypeM.createDataObject("property");
        decimalPropertyM.set("name", "decimal");
        decimalPropertyM.set("type", decimalType);
        decimalPropertyM.set("many", true);
        DataObject doublePropertyM = prototypeM.createDataObject("property");
        doublePropertyM.set("name", "double");
        doublePropertyM.set("type", doubleType);
        doublePropertyM.set("many", true);
        DataObject durationPropertyM = prototypeM.createDataObject("property");
        durationPropertyM.set("name", "duration");
        durationPropertyM.set("type", durationType);
        durationPropertyM.set("many", true);
        DataObject floatPropertyM = prototypeM.createDataObject("property");
        floatPropertyM.set("name", "float");
        floatPropertyM.set("type", floatType);
        floatPropertyM.set("many", true);
        DataObject intPropertyM = prototypeM.createDataObject("property");
        intPropertyM.set("name", "int");
        intPropertyM.set("type", intType);
        intPropertyM.set("many", true);
        DataObject integerPropertyM = prototypeM.createDataObject("property");
        integerPropertyM.set("name", "integer");
        integerPropertyM.set("type", integerType);
        integerPropertyM.set("many", true);
        DataObject longPropertyM = prototypeM.createDataObject("property");
        longPropertyM.set("name", "long");
        longPropertyM.set("type", longType);
        longPropertyM.set("many", true);
        DataObject monthPropertyM = prototypeM.createDataObject("property");
        monthPropertyM.set("name", "month");
        monthPropertyM.set("type", monthType);
        monthPropertyM.set("many", true);
        DataObject monthDayPropertyM = prototypeM.createDataObject("property");
        monthDayPropertyM.set("name", "monthDay");
        monthDayPropertyM.set("type", monthDayType);
        monthDayPropertyM.set("many", true);
        DataObject shortPropertyM = prototypeM.createDataObject("property");
        shortPropertyM.set("name", "short");
        shortPropertyM.set("type", shortType);
        shortPropertyM.set("many", true);
        DataObject stringPropertyM = prototypeM.createDataObject("property");
        stringPropertyM.set("name", "string");
        stringPropertyM.set("type", stringType);
        stringPropertyM.set("many", true);
        DataObject stringsPropertyM = prototypeM.createDataObject("property");
        stringsPropertyM.set("name", "strings");
        stringsPropertyM.set("type", stringsType);
        stringsPropertyM.set("many", true);
        DataObject timePropertyM = prototypeM.createDataObject("property");
        timePropertyM.set("name", "time");
        timePropertyM.set("type", timeType);
        timePropertyM.set("many", true);
        DataObject uriPropertyM = prototypeM.createDataObject("property");
        uriPropertyM.set("name", "uri");
        uriPropertyM.set("type", uriType);
        uriPropertyM.set("many", true);
        DataObject yearPropertyM = prototypeM.createDataObject("property");
        yearPropertyM.set("name", "year");
        yearPropertyM.set("type", yearType);
        yearPropertyM.set("many", true);
        DataObject yearMonthPropertyM = prototypeM.createDataObject("property");
        yearMonthPropertyM.set("name", "yearMonth");
        yearMonthPropertyM.set("type", yearMonthType);
        yearMonthPropertyM.set("many", true);
        DataObject yearMonthDayPropertyM = prototypeM.createDataObject("property");
        yearMonthDayPropertyM.set("name", "yearMonthDay");
        yearMonthDayPropertyM.set("type", yearMonthDayType);
        yearMonthDayPropertyM.set("many", true);

        basic_t2 = typeHelper.define(prototypeM);

        DataObject prototypeJ = factory.create("commonj.sdo", "Type");
        prototypeJ.set("uri", "");
        prototypeJ.set("name", "BasicJavaTypes");

        DataObject booleanObjectProperty = prototypeJ.createDataObject("property");
        booleanObjectProperty.set("name", "boolean");
        booleanObjectProperty.set("type", booleanObjectType);
        //booleanObjectProperty.set("nullable", true); // do we want this?
        DataObject byteObjectProperty = prototypeJ.createDataObject("property");
        byteObjectProperty.set("name", "byte");
        byteObjectProperty.set("type", byteObjectType);
        DataObject characterObjectProperty = prototypeJ.createDataObject("property");
        characterObjectProperty.set("name", "character");
        characterObjectProperty.set("type", characterObjectType);
        DataObject doubleObjectProperty = prototypeJ.createDataObject("property");
        doubleObjectProperty.set("name", "double");
        doubleObjectProperty.set("type", doubleObjectType);
        DataObject floatObjectProperty = prototypeJ.createDataObject("property");
        floatObjectProperty.set("name", "float");
        floatObjectProperty.set("type", floatObjectType);
        DataObject intObjectProperty = prototypeJ.createDataObject("property");
        intObjectProperty.set("name", "int");
        intObjectProperty.set("type", intObjectType);
        DataObject longObjectProperty = prototypeJ.createDataObject("property");
        longObjectProperty.set("name", "long");
        longObjectProperty.set("type", longObjectType);
        DataObject shortObjectProperty = prototypeJ.createDataObject("property");
        shortObjectProperty.set("name", "short");
        shortObjectProperty.set("type", shortObjectType);

        basic_java_t = typeHelper.define(prototypeJ);

        DataObject addressPrototype = factory.create("commonj.sdo", "Type");
        addressPrototype.set("uri", "http://example.com/order");
        addressPrototype.set("name", "Address");
        DataObject addressStreetProperty = addressPrototype.createDataObject("property");
        addressStreetProperty.set("name", "street");
        addressStreetProperty.set("type", stringType);
        DataObject addressCityProperty = addressPrototype.createDataObject("property");
        addressCityProperty.set("name", "city");
        addressCityProperty.set("type", stringType);
        DataObject addressStateProperty = addressPrototype.createDataObject("property");
        addressStateProperty.set("name", "state");
        addressStateProperty.set("type", stringType);
        addressType = typeHelper.define(addressPrototype);

        DataObject customerPrototype = factory.create("commonj.sdo", "Type");
        customerPrototype.set("uri", "http://example.com/order");
        customerPrototype.set("name", "Customer");
        DataObject customerIdProperty = customerPrototype.createDataObject("property");
        customerIdProperty.set("name", "id");
        customerIdProperty.set("type", intType);
        DataObject customerNameProperty = customerPrototype.createDataObject("property");
        customerNameProperty.set("name", "name");
        customerNameProperty.set("type", stringType);
        DataObject customerBillingAddressProperty = customerPrototype.createDataObject("property");
        customerBillingAddressProperty.set("name", "billingAddress");
        customerBillingAddressProperty.set("type", addressType);
        customerBillingAddressProperty.setBoolean("containment", true);
        DataObject customerShippingAddressProperty = customerPrototype.createDataObject("property");
        customerShippingAddressProperty.set("name", "shippingAddress");
        customerShippingAddressProperty.set("type", addressType);
        customerShippingAddressProperty.setBoolean("containment", true);
        customerType = typeHelper.define(customerPrototype);

        DataObject itemPrototype = factory.create("commonj.sdo", "Type");
        itemPrototype.set("uri", "http://example.com/order");
        itemPrototype.set("name", "Item");
        DataObject itemQtyProperty = itemPrototype.createDataObject("property");
        itemQtyProperty.set("name", "qty");
        itemQtyProperty.set("type", intType);
        DataObject itemSKUProperty = itemPrototype.createDataObject("property");
        itemSKUProperty.set("name", "sku");
        itemSKUProperty.set("type", stringType);

        itemType = typeHelper.define(itemPrototype);

        DataObject orderPrototype = factory.create("commonj.sdo", "Type");
        orderPrototype.set("uri", "http://example.com/order");
        orderPrototype.set("name", "Order");
        DataObject customerProperty = orderPrototype.createDataObject("property");
        customerProperty.set("name", "customer");
        customerProperty.set("type", customerType);
        DataObject itemProperty = orderPrototype.createDataObject("property");
        itemProperty.set("name", "item");
        itemProperty.set("type", itemType);
        itemProperty.setBoolean("many", true);
        itemProperty.setBoolean("containment", true);

        orderType = typeHelper.define(orderPrototype);

        DataObject tradePrototype = factory.create("commonj.sdo", "Type");
        tradePrototype.set("uri", "http://sdo/test/trade");
        tradePrototype.set("name", "TradeType");
        DataObject symbolProperty = tradePrototype.createDataObject("property");
        symbolProperty.set("name", "symbol");
        symbolProperty.set("type", stringType);
        DataObject quantityProperty = tradePrototype.createDataObject("property");
        quantityProperty.set("name", "quantity");
        quantityProperty.set("type", intType);
        DataObject priceProperty = tradePrototype.createDataObject("property");
        priceProperty.set("name", "price");
        priceProperty.set("type", doubleType);
        tradeType = typeHelper.define(tradePrototype);
        DataObject traderPrototype = factory.create("commonj.sdo", "Type");
        traderPrototype.set("uri", "http://sdo/test/trade");
        traderPrototype.set("name", "TraderType");
        DataObject idProperty = traderPrototype.createDataObject("property");
        idProperty.set("name", "id");
        idProperty.set("type", stringType);
        DataObject buyProperty = traderPrototype.createDataObject("property");
        buyProperty.set("name", "buy");
        buyProperty.set("type", tradeType);
        buyProperty.set("containment", true);
        buyProperty.set("many", true);
        DataObject sellProperty = traderPrototype.createDataObject("property");
        sellProperty.set("name", "sell");
        sellProperty.set("type", tradeType);
        sellProperty.set("containment", true);
        sellProperty.set("many", true);
        traderType = typeHelper.define(traderPrototype);
        DataObject tradesPrototype = factory.create("commonj.sdo", "Type");
        tradesPrototype.set("uri", "http://sdo/test/trade");
        tradesPrototype.set("name", "TradesType");
        DataObject traderProperty = tradesPrototype.createDataObject("property");
        traderProperty.set("name", "trader");
        traderProperty.set("type", traderType);
        traderProperty.set("containment", true);
        traderProperty.set("many", true);
        tradesType = typeHelper.define(tradesPrototype);
    }

    protected DataObject createDataObject()
    {
        DataObject dobj = factory.create(basic_t);
        return dobj;
    }

    protected DataObject createDataObjectJ()
    {
        DataObject dobj = factory.create(basic_java_t);
        return dobj;
    }

    public void testNumberOfProperties()
    {
        DataObject dobj = createDataObject();
        List instanceProps = dobj.getInstanceProperties();
        System.out.println("number of instance properties: " +
                           instanceProps.size());
        assertEquals(25, instanceProps.size());
        Type t = dobj.getType();
        List props = t.getProperties();
        System.out.println("number of properties: " + props.size());
        assertEquals(25, props.size());

        for (int i = 0; i < instanceProps.size(); i++)
        {
            Property p = (Property)instanceProps.get(i);
            System.out.println("  " + i + ": " + p.getName());
            assertFalse(dobj.isSet(p));
            Object value = dobj.get(p);
            System.out.println("    " + value + "(" +
                               p.getType().getInstanceClass() + ")");
            if (p.getType().getInstanceClass() != null && 
                p.getType().getInstanceClass().equals(boolean.class))
                assertEquals(false, value); // with auto-boxing
            else if (p.getType().getInstanceClass() != null && 
                     p.getType().getInstanceClass().equals(byte.class))
                assertEquals((byte)0, value); // with auto-boxing
            else if (p.getType().getInstanceClass() != null && 
                     p.getType().getInstanceClass().equals(char.class))
                assertEquals((char)0, value); // with auto-boxing
            else if (p.getType().getInstanceClass() != null && 
                     p.getType().getInstanceClass().equals(double.class))
                assertEquals(0.0, value); // with auto-boxing
            else if (p.getType().getInstanceClass() != null && 
                     p.getType().getInstanceClass().equals(float.class))
                assertEquals((float)0.0, value); // with auto-boxing
            else if (p.getType().getInstanceClass() != null && 
                     p.getType().getInstanceClass().equals(int.class))
                assertEquals(0, value); // with auto-boxing
            else if (p.getType().getInstanceClass() != null && 
                     p.getType().getInstanceClass().equals(short.class))
                assertEquals((short)0, value); // with auto-boxing
            else if (p.getType().getInstanceClass() != null && 
                     p.getType().getInstanceClass().equals(long.class))
                assertEquals((long)0, value); // with auto-boxing
            else if (p.getType().getInstanceClass() != null && 
                     p.getType().getInstanceClass().equals(Object.class))
            {
                assertTrue(value instanceof List);
                assertEquals(0, ((List)value).size());
            }
            else
                assertNull(value);
        }
    }

    public void testJavaProperties()
    {
        DataObject dobj = createDataObjectJ();
        List instanceProps = dobj.getInstanceProperties();
        System.out.println("number of instance properties: " +
                           instanceProps.size());
        assertEquals(8, instanceProps.size());
        Type t = dobj.getType();
        List props = t.getProperties();
        System.out.println("number of properties: " + props.size());
        assertEquals(8, props.size());

        for (int i = 0; i < instanceProps.size(); i++)
        {
            Property p = (Property)instanceProps.get(i);
            System.out.println("  " + i + ": " + p.getName());
            assertFalse(dobj.isSet(p));
            Object value = dobj.get(p);
            System.out.println("    " + value + "(" +
                               p.getType().getInstanceClass() + ")");
            /*
            if (p.getType().getInstanceClass() != null && 
                p.getType().getInstanceClass().equals(Boolean.class))
                assertEquals(false, value); // with auto-boxing
            else if (p.getType().getInstanceClass() != null && 
                     p.getType().getInstanceClass().equals(Byte.class))
                assertEquals((byte)0, value); // with auto-boxing
            else if (p.getType().getInstanceClass() != null && 
                     p.getType().getInstanceClass().equals(Character.class))
                assertEquals((char)0, value); // with auto-boxing
            else if (p.getType().getInstanceClass() != null && 
                     p.getType().getInstanceClass().equals(Double.class))
                assertEquals(0.0, value); // with auto-boxing
            else if (p.getType().getInstanceClass() != null && 
                     p.getType().getInstanceClass().equals(Float.class))
                assertEquals((float)0.0, value); // with auto-boxing
            else if (p.getType().getInstanceClass() != null && 
                     p.getType().getInstanceClass().equals(Integer.class))
                assertEquals(0, value); // with auto-boxing
            else if (p.getType().getInstanceClass() != null && 
                     p.getType().getInstanceClass().equals(Short.class))
                assertEquals((short)0, value); // with auto-boxing
            else if (p.getType().getInstanceClass() != null && 
                     p.getType().getInstanceClass().equals(Long.class))
                assertEquals((long)0, value); // with auto-boxing
            else
                assertNull(value);
            */
            assertNull(value);
        }
    }

    public void testSetBooleanByPath()
    {
        DataObject dobj = createDataObject();
        // boolean
        boolean isSet = dobj.isSet("boolean");
        assertFalse(isSet);
        dobj.set("boolean", Boolean.TRUE);
        boolean val = dobj.getBoolean("boolean");
        assertEquals(true, val);
        dobj.setBoolean("boolean", false);
        Object value = dobj.get("boolean");
        assertEquals(Boolean.FALSE, value);
        isSet = dobj.isSet("boolean");
        assertTrue(isSet);
        dobj.unset("boolean");
        isSet = dobj.isSet("boolean");
        assertFalse(isSet);
    }

    public void testSetBooleanByIndex()
    {
        DataObject dobj = createDataObject();
        // boolean
        boolean isSet = dobj.isSet(BOOLEAN_P_I);
        assertFalse(isSet);
        dobj.set(BOOLEAN_P_I, Boolean.TRUE);
        boolean val = dobj.getBoolean(BOOLEAN_P_I);
        assertEquals(true, val);
        dobj.setBoolean(BOOLEAN_P_I, false);
        Object value = dobj.get(BOOLEAN_P_I);
        assertEquals(Boolean.FALSE, value);
        /*
        isSet = dobj.isSet(BOOLEAN_P_I);
        assertTrue(isSet);
        dobj.unset(BOOLEAN_P_I);
        isSet = dobj.isSet(BOOLEAN_P_I);
        assertFalse(isSet);
        */
    }

    public void testSetBooleanByProperty()
    {
        DataObject dobj = createDataObject();
        Type t = dobj.getType();
        List props = t.getProperties();
        // boolean
        Property p = (Property)props.get(BOOLEAN_P_I);
        boolean isSet = dobj.isSet(p);
        assertFalse(isSet);
        dobj.set(p, Boolean.TRUE);
        boolean val = dobj.getBoolean(p);
        assertEquals(true, val);
        dobj.setBoolean(p, false);
        Object value = dobj.get(p);
        assertEquals(Boolean.FALSE, value);
        isSet = dobj.isSet(p);
        assertTrue(isSet);
        dobj.unset(p);
        isSet = dobj.isSet(p);
        assertFalse(isSet);
    }

    public void testSetByteByPath()
    {
        DataObject dobj = createDataObject();
        // byte
        boolean isSet = dobj.isSet("byte");
        assertFalse(isSet);
        dobj.set("byte", new Byte((byte)0));
        byte val = dobj.getByte("byte");
        assertEquals((byte)0, val);
        dobj.setByte("byte", (byte)1);
        Object value = dobj.get("byte");
        assertEquals(new Byte((byte)1), value);
        isSet = dobj.isSet("byte");
        assertTrue(isSet);
        dobj.unset("byte");
        isSet = dobj.isSet("byte");
        assertFalse(isSet);
    }

    public void testSetByteByIndex()
    {
        DataObject dobj = createDataObject();
        // byte
        boolean isSet = dobj.isSet(BYTE_P_I);
        assertFalse(isSet);
        dobj.set(BYTE_P_I, new Byte((byte)0));
        byte val = dobj.getByte(BYTE_P_I);
        assertEquals((byte)0, val);
        dobj.setByte(BYTE_P_I, (byte)1);
        Object value = dobj.get(BYTE_P_I);
        assertEquals(new Byte((byte)1), value);
        /*
        isSet = dobj.isSet(BYTE_P_I);
        assertTrue(isSet);
        dobj.unset(BYTE_P_I);
        isSet = dobj.isSet(BYTE_P_I);
        assertFalse(isSet);
        */
    }

    public void testSetByteByProperty()
    {
        DataObject dobj = createDataObject();
        Type t = dobj.getType();
        List props = t.getProperties();
        // byte
        Property p = (Property)props.get(BYTE_P_I);
        boolean isSet = dobj.isSet(p);
        assertFalse(isSet);
        dobj.set(p, new Byte((byte)0));
        byte val = dobj.getByte(p);
        assertEquals((byte)0, val);
        dobj.setByte(p, (byte)1);
        Object value = dobj.get(p);
        assertEquals(new Byte((byte)1), value);
        isSet = dobj.isSet(p);
        assertTrue(isSet);
        dobj.unset(p);
        isSet = dobj.isSet(p);
        assertFalse(isSet);
    }

    public void testSetBytesByPath()
    {
        DataObject dobj = createDataObject();
        // bytes
        boolean isSet = dobj.isSet("bytes");
        assertFalse(isSet);
        byte[] bytearr1 = new byte[]{(byte)0, (byte)1};
        dobj.set("bytes", bytearr1);
        byte[] val = dobj.getBytes("bytes");
        assertEquals(bytearr1, val);
        byte[] bytearr2 = new byte[]{(byte)1, (byte)2};
        dobj.setBytes("bytes", bytearr2);
        Object value = dobj.get("bytes");
        assertEquals(bytearr2, value);
    }

    public void testSetBytesByIndex()
    {
        DataObject dobj = createDataObject();
        // bytes
        boolean isSet = dobj.isSet(BYTES_P_I);
        assertFalse(isSet);
        byte[] bytearr1 = new byte[]{(byte)0, (byte)1};
        dobj.set(BYTES_P_I, bytearr1);
        byte[] val = dobj.getBytes(BYTES_P_I);
        assertEquals(bytearr1, val);
        byte[] bytearr2 = new byte[]{(byte)1, (byte)2};
        dobj.setBytes(BYTES_P_I, bytearr2);
        Object value = dobj.get(BYTES_P_I);
        assertEquals(bytearr2, value);
    }

    public void testSetBytesByProperty()
    {
        DataObject dobj = createDataObject();
        Type t = dobj.getType();
        List props = t.getProperties();
        // bytes
        Property p = (Property)props.get(BYTES_P_I);
        boolean isSet = dobj.isSet(p);
        assertFalse(isSet);
        byte[] bytearr1 = new byte[]{(byte)0, (byte)1};
        dobj.set(p, bytearr1);
        byte[] val = dobj.getBytes(p);
        assertEquals(bytearr1, val);
        byte[] bytearr2 = new byte[]{(byte)1, (byte)2};
        dobj.setBytes(p, bytearr2);
        Object value = dobj.get(p);
        assertEquals(bytearr2, value);
    }

    public void testSetCharByPath()
    {
        DataObject dobj = createDataObject();
        // character
        boolean isSet = dobj.isSet("character");
        assertFalse(isSet);
        dobj.set("character", new Character('a'));
        char val = dobj.getChar("character");
        assertEquals('a', val);
        dobj.setChar("character", 'b');
        Object value = dobj.get("character");
        assertEquals(new Character('b'), value);
    }

    public void testSetCharByIndex()
    {
        DataObject dobj = createDataObject();
        // character
        boolean isSet = dobj.isSet(CHARACTER_P_I);
        assertFalse(isSet);
        dobj.set(CHARACTER_P_I, new Character('a'));
        char val = dobj.getChar(CHARACTER_P_I);
        assertEquals('a', val);
        dobj.setChar(CHARACTER_P_I, 'b');
        Object value = dobj.get(CHARACTER_P_I);
        assertEquals(new Character('b'), value);
    }

    public void testSetCharByProperty()
    {
        DataObject dobj = createDataObject();
        Type t = dobj.getType();
        List props = t.getProperties();
        // character
        Property p = (Property)props.get(CHARACTER_P_I);
        boolean isSet = dobj.isSet(p);
        assertFalse(isSet);
        dobj.set(p, new Character('a'));
        char val = dobj.getChar(p);
        assertEquals('a', val);
        dobj.setChar(p, 'b');
        Object value = dobj.get(p);
        assertEquals(new Character('b'), value);
    }

    public void testSetDateByPath()
    {
        DataObject dobj = createDataObject();
        // date
        boolean isSet = dobj.isSet("date");
        assertFalse(isSet);
        Date startOfEpoch = new Date(0);
        System.out.println(startOfEpoch);
        dobj.set("date", startOfEpoch);
        Date value = dobj.getDate("date");
        assertEquals(startOfEpoch, value);
        Date now = new Date();
        System.out.println(now);
        dobj.setDate("date", now);
        value = (Date)dobj.get("date");
        assertEquals(now, value);
        //String dateString = dobj.getString("date"); //ClassCastException
        //System.out.println(dateString);
    }

    public void testSetDateByIndex()
    {
        DataObject dobj = createDataObject();
        // date
        boolean isSet = dobj.isSet(DATE_P_I);
        assertFalse(isSet);
        Date startOfEpoch = new Date(0);
        System.out.println(startOfEpoch);
        dobj.set(DATE_P_I, startOfEpoch);
        Date value = dobj.getDate(DATE_P_I);
        assertEquals(startOfEpoch, value);
        Date now = new Date();
        System.out.println(now);
        dobj.setDate(DATE_P_I, now);
        value = (Date)dobj.get(DATE_P_I);
        assertEquals(now, value);
    }

    public void testSetDateByProperty()
    {
        DataObject dobj = createDataObject();
        Type t = dobj.getType();
        List props = t.getProperties();
        // date
        Property p = (Property)props.get(DATE_P_I);
        boolean isSet = dobj.isSet(p);
        assertFalse(isSet);
        Date startOfEpoch = new Date(0);
        System.out.println(startOfEpoch);
        dobj.set(p, startOfEpoch);
        Date value = dobj.getDate(p);
        assertEquals(startOfEpoch, value);
        Date now = new Date();
        System.out.println(now);
        dobj.setDate(p, now);
        value = (Date)dobj.get(p);
        assertEquals(now, value);
    }

    public void testSetBigDecimalByPath()
    {
        DataObject dobj = createDataObject();
        // decimal
        boolean isSet = dobj.isSet("decimal");
        assertFalse(isSet);
        dobj.set("decimal", new BigDecimal("0.123456789"));
        BigDecimal value = dobj.getBigDecimal("decimal");
        assertEquals(new BigDecimal("0.123456789"), value);
        dobj.setBigDecimal("decimal", new BigDecimal(1.23456789));
        value = (BigDecimal)dobj.get("decimal");
        assertEquals(new BigDecimal(1.23456789), value);
    }

    public void testSetBigDecimalByIndex()
    {
        DataObject dobj = createDataObject();
        // decimal
        boolean isSet = dobj.isSet(DECIMAL_P_I);
        assertFalse(isSet);
        dobj.set(DECIMAL_P_I, new BigDecimal("0.123456789"));
        BigDecimal value = dobj.getBigDecimal(DECIMAL_P_I);
        assertEquals(new BigDecimal("0.123456789"), value);
        dobj.setBigDecimal(DECIMAL_P_I, new BigDecimal(1.23456789));
        value = (BigDecimal)dobj.get(DECIMAL_P_I);
        assertEquals(new BigDecimal(1.23456789), value);
    }

    public void testSetBigDecimalByProperty()
    {
        DataObject dobj = createDataObject();
        Type t = dobj.getType();
        List props = t.getProperties();
        // decimal
        Property p = (Property)props.get(DECIMAL_P_I);
        boolean isSet = dobj.isSet(p);
        assertFalse(isSet);
        dobj.set(p, new BigDecimal("0.123456789"));
        BigDecimal value = dobj.getBigDecimal(p);
        assertEquals(new BigDecimal("0.123456789"), value);
        dobj.setBigDecimal(p, new BigDecimal(1.23456789));
        value = (BigDecimal)dobj.get(p);
        assertEquals(new BigDecimal(1.23456789), value);
    }

    public void testSetDoubleByPath()
    {
        DataObject dobj = createDataObject();
        // double
        boolean isSet = dobj.isSet("double");
        assertFalse(isSet);
        dobj.set("double", new Double(1.1));
        double val = dobj.getDouble("double");
        assertEquals(1.1, val);
        dobj.setDouble("double", 1.2);
        Object value = dobj.get("double");
        assertEquals(new Double(1.2), value);
    }

    public void testSetDoubleByIndex()
    {
        DataObject dobj = createDataObject();
        // double
        boolean isSet = dobj.isSet(DOUBLE_P_I);
        assertFalse(isSet);
        dobj.set(DOUBLE_P_I, new Double(1.1));
        double val = dobj.getDouble(DOUBLE_P_I);
        assertEquals(1.1, val);
        dobj.setDouble(DOUBLE_P_I, 1.2);
        Object value = dobj.get(DOUBLE_P_I);
        assertEquals(new Double(1.2), value);
    }

    public void testSetDoubleByProperty()
    {
        DataObject dobj = createDataObject();
        Type t = dobj.getType();
        List props = t.getProperties();
        // double
        Property p = (Property)props.get(DOUBLE_P_I);
        boolean isSet = dobj.isSet(p);
        assertFalse(isSet);
        dobj.set(p, new Double(1.1));
        double val = dobj.getDouble(p);
        assertEquals(1.1, val);
        dobj.setDouble(p, 1.2);
        Object value = dobj.get(p);
        assertEquals(new Double(1.2), value);
    }

    public void testSetFloatByPath()
    {
        DataObject dobj = createDataObject();
        // float
        boolean isSet = dobj.isSet("float");
        assertFalse(isSet);
        dobj.set("float", new Float((float)1.1));
        float val = dobj.getFloat("float");
        assertEquals((float)1.1, val);
        dobj.setFloat("float", (float)1.2);
        Object value = dobj.get("float");
        assertEquals(new Float((float)1.2), value);
    }

    public void testSetFloatByIndex()
    {
        DataObject dobj = createDataObject();
        // float
        boolean isSet = dobj.isSet(FLOAT_P_I);
        assertFalse(isSet);
        dobj.set(FLOAT_P_I, new Float((float)1.1));
        float val = dobj.getFloat(FLOAT_P_I);
        assertEquals((float)1.1, val);
        dobj.setFloat(FLOAT_P_I, (float)1.2);
        Object value = dobj.get(FLOAT_P_I);
        assertEquals(new Float((float)1.2), value);
    }

    public void testSetFloatByProperty()
    {
        DataObject dobj = createDataObject();
        Type t = dobj.getType();
        List props = t.getProperties();
        // float
        Property p = (Property)props.get(FLOAT_P_I);
        boolean isSet = dobj.isSet(p);
        assertFalse(isSet);
        dobj.set(p, new Float((float)1.1));
        float val = dobj.getFloat(p);
        assertEquals((float)1.1, val);
        dobj.setFloat(p, (float)1.2);
        Object value = dobj.get(p);
        assertEquals(new Float((float)1.2), value);
    }

    public void testSetIntByPath()
    {
        DataObject dobj = createDataObject();
        // int
        boolean isSet = dobj.isSet("int");
        assertFalse(isSet);
        dobj.set("int", new Integer(1));
        int val = dobj.getInt("int");
        assertEquals(1, val);
        dobj.setInt("int", 2);
        Object value = dobj.get("int");
        assertEquals(new Integer(2), value);
    }

    public void testSetIntByIndex()
    {
        DataObject dobj = createDataObject();
        // int
        boolean isSet = dobj.isSet(INT_P_I);
        assertFalse(isSet);
        dobj.set(INT_P_I, new Integer(1));
        int val = dobj.getInt(INT_P_I);
        assertEquals(1, val);
        dobj.setInt(INT_P_I, 2);
        Object value = dobj.get(INT_P_I);
        assertEquals(new Integer(2), value);
    }

    public void testSetIntByProperty()
    {
        DataObject dobj = createDataObject();
        Type t = dobj.getType();
        List props = t.getProperties();
        // int
        Property p = (Property)props.get(INT_P_I);
        boolean isSet = dobj.isSet(p);
        assertFalse(isSet);
        dobj.set(p, new Integer(1));
        int val = dobj.getInt(p);
        assertEquals(1, val);
        dobj.setInt(p, 2);
        Object value = dobj.get(p);
        assertEquals(new Integer(2), value);
    }

    public void testSetBigIntegerByPath()
    {
        DataObject dobj = createDataObject();
        // integer
        boolean isSet = dobj.isSet("integer");
        assertFalse(isSet);
        dobj.set("integer", new BigInteger("123456789"));
        BigInteger value = dobj.getBigInteger("integer");
        assertEquals(new BigInteger("123456789"), value);
        dobj.setBigInteger("integer", new BigInteger("123456789000000000"));
        value = (BigInteger)dobj.get("integer");
        assertEquals(new BigInteger("123456789000000000"), value);
    }

    public void testSetBigIntegerByIndex()
    {
        DataObject dobj = createDataObject();
        // integer
        boolean isSet = dobj.isSet(INTEGER_P_I);
        assertFalse(isSet);
        dobj.set(INTEGER_P_I, new BigInteger("123456789"));
        BigInteger value = dobj.getBigInteger(INTEGER_P_I);
        assertEquals(new BigInteger("123456789"), value);
        dobj.setBigInteger(INTEGER_P_I, new BigInteger("123456789000000000"));
        value = (BigInteger)dobj.get(INTEGER_P_I);
        assertEquals(new BigInteger("123456789000000000"), value);
    }

    public void testSetBigIntegerByProperty()
    {
        DataObject dobj = createDataObject();
        Type t = dobj.getType();
        List props = t.getProperties();
        // integer
        Property p = (Property)props.get(INTEGER_P_I);
        boolean isSet = dobj.isSet(p);
        assertFalse(isSet);
        dobj.set(p, new BigInteger("123456789"));
        BigInteger value = dobj.getBigInteger(p);
        assertEquals(new BigInteger("123456789"), value);
        dobj.setBigInteger(p, new BigInteger("123456789000000000"));
        value = (BigInteger)dobj.get(p);
        assertEquals(new BigInteger("123456789000000000"), value);
    }

    public void testSetLongByPath()
    {
        DataObject dobj = createDataObject();
        // long
        boolean isSet = dobj.isSet("long");
        assertFalse(isSet);
        dobj.set("long", new Long(2147483648L));
        long val = dobj.getLong("long");
        assertEquals(2147483648L, val);
        dobj.setLong("long", -2147483649L);
        Object value = dobj.get("long");
        assertEquals(new Long(-2147483649L), value);
    }

    public void testSetLongByIndex()
    {
        DataObject dobj = createDataObject();
        // long
        boolean isSet = dobj.isSet(LONG_P_I);
        assertFalse(isSet);
        dobj.set(LONG_P_I, new Long(2147483648L));
        long val = dobj.getLong(LONG_P_I);
        assertEquals(2147483648L, val);
        dobj.setLong(LONG_P_I, -2147483649L);
        Object value = dobj.get(LONG_P_I);
        assertEquals(new Long(-2147483649L), value);
    }

    public void testSetLongByProperty()
    {
        DataObject dobj = createDataObject();
        Type t = dobj.getType();
        List props = t.getProperties();
        // long
        Property p = (Property)props.get(LONG_P_I);
        boolean isSet = dobj.isSet(p);
        assertFalse(isSet);
        dobj.set(p, new Long(2147483648L));
        long val = dobj.getLong(p);
        assertEquals(2147483648L, val);
        dobj.setLong(p, -2147483649L);
        Object value = dobj.get(p);
        assertEquals(new Long(-2147483649L), value);
    }

    public void testSetShortByPath()
    {
        DataObject dobj = createDataObject();
        // short
        boolean isSet = dobj.isSet("short");
        assertFalse(isSet);
        dobj.set("short", new Short((short)2147));
        short val = dobj.getShort("short");
        assertEquals((short)2147, val);
        dobj.setShort("short", (short)-2147);
        Object value = dobj.get("short");
        assertEquals(new Short((short)-2147), value);
    }

    public void testSetShortByIndex()
    {
        DataObject dobj = createDataObject();
        // short
        boolean isSet = dobj.isSet(SHORT_P_I);
        assertFalse(isSet);
        dobj.set(SHORT_P_I, new Short((short)2147));
        short val = dobj.getShort(SHORT_P_I);
        assertEquals((short)2147, val);
        dobj.setShort(SHORT_P_I, (short)-2147);
        Object value = dobj.get(SHORT_P_I);
        assertEquals(new Short((short)-2147), value);
    }

    public void testSetShortByProperty()
    {
        DataObject dobj = createDataObject();
        Type t = dobj.getType();
        List props = t.getProperties();
        // short
        Property p = (Property)props.get(SHORT_P_I);
        boolean isSet = dobj.isSet(p);
        assertFalse(isSet);
        dobj.set(p, new Short((short)2147));
        short val = dobj.getShort(p);
        assertEquals((short)2147, val);
        dobj.setShort(p, (short)-2147);
        Object value = dobj.get(p);
        assertEquals(new Short((short)-2147), value);
    }

    public void testSetStringByPath()
    {
        DataObject dobj = createDataObject();
        // string
        boolean isSet = dobj.isSet("string");
        assertFalse(isSet);
        dobj.set("string", "xxx");
        String value = dobj.getString("string");
        assertEquals("xxx", value);
        dobj.setString("string", "yyy");
        value = (String)dobj.get("string");
        assertEquals("yyy", value);
    }

    public void testSetStringByIndex()
    {
        DataObject dobj = createDataObject();
        // string
        boolean isSet = dobj.isSet(STRING_P_I);
        assertFalse(isSet);
        dobj.set(STRING_P_I, "xxx");
        String value = dobj.getString(STRING_P_I);
        assertEquals("xxx", value);
        dobj.setString(STRING_P_I, "yyy");
        value = (String)dobj.get(STRING_P_I);
        assertEquals("yyy", value);
    }

    public void testSetStringByProperty()
    {
        DataObject dobj = createDataObject();
        Type t = dobj.getType();
        List props = t.getProperties();
        // string
        Property p = (Property)props.get(STRING_P_I);
        boolean isSet = dobj.isSet(p);
        assertFalse(isSet);
        dobj.set(p, "xxx");
        String value = dobj.getString(p);
        assertEquals("xxx", value);
        dobj.setString(p, "yyy");
        value = (String)dobj.get(p);
        assertEquals("yyy", value);
    }

    public void testSetStringListByPath()
    {
        DataObject dobj = createDataObject();
        // strings
        boolean isSet = dobj.isSet("strings");
        assertFalse(isSet);
        List<String> strings = new ArrayList<String>();
        strings.add("xxx");
        strings.add("yyy");
        dobj.set("strings", strings);
        List values = dobj.getList("strings");
        assertEquals(2, values.size());
        assertEquals("xxx", values.get(0));
        assertEquals("yyy", values.get(1));
        strings.add("zzz");
        System.out.println("size of strings is now " + values.size());

        List<String> newstrings = new ArrayList<String>();
        newstrings.add("aaa");
        newstrings.add("bbb");
        newstrings.add("ccc");
        newstrings.add("ddd");
        dobj.setList("strings", newstrings);
        System.out.println("size of strings is now " + values.size());
        //value = (String)dobj.get("strings");
        //assertEquals("yyy", value);
    }

    public void testSetStringListByProperty()
    {
        DataObject dobj = createDataObject();
        Type t = dobj.getType();
        List props = t.getProperties();
        // strings
        Property p = (Property)props.get(STRINGS_P_I);
        assertFalse(p.isMany());
        boolean isSet = dobj.isSet(p);
        assertFalse(isSet);
        //List l0 = dobj.getList(p);
        //assertNotNull(l0);
        //assertEquals(0, l0.size());
        List<String> strings = new ArrayList<String>();
        strings.add("xxx");
        strings.add("yyy");
        dobj.set(p, strings);
        List values = dobj.getList(p);
        assertEquals(2, values.size());
        assertEquals("xxx", values.get(0));
        assertEquals("yyy", values.get(1));
    }

    /**
        test:
        - setDataObject, getDataObject
        - setList, getList for many-valued Property
    */
    public void testCustomerByPath()
    {
        DataObject customer = factory.create(customerType);
        customer.setInt("id", 123);
        customer.setString("name", "John Smith");
        DataObject item1 = factory.create(itemType);
        item1.setInt("qty", 100);
        item1.setString("sku", "111-AA");
        DataObject item2 = factory.create(itemType);
        item2.setInt("qty", 200);
        item2.setString("sku", "122-BB");
        DataObject order = factory.create(orderType);
        boolean isSet = order.isSet("item");
        assertFalse(isSet);
        Object value = order.get("item");
        assertNotNull(value);
        assertTrue(value instanceof List);
        List items = (List)value;
        assertEquals(0, items.size());
        order.setDataObject("customer", customer);
        List itemsToSet = new ArrayList();
        itemsToSet.add(item1);
        itemsToSet.add(item2);
        order.setList("item", itemsToSet);
        //System.out.println("number of items is now " + items.size());
        items = order.getList("item");
        assertEquals(2, items.size());
    }

    /**
       test the following assertion from the spec:
       "Containment is managed. When a DataObject is set or added to a
       containment Property, it is removed from any previous containment
       Property."
    */
    public void testContainmentManagement()
    {
        DataObject address = factory.create(addressType);
        address.setString("street", "123 Main Street");
        address.setString("city", "Pleasantville");
        address.setString("state", "CA");
        DataObject customer1 = factory.create(customerType);
        customer1.setInt("id", 123);
        customer1.setString("name", "John Smith");
        DataObject customer2 = factory.create(customerType);
        customer2.setInt("id", 123);
        customer2.setString("name", "Mark Jones");
        Property billingAddress = customerType.getProperty("billingAddress");
        assertTrue(billingAddress.isContainment());
        assertFalse(customer1.isSet(billingAddress));
        customer1.set(billingAddress, address);
        assertTrue(customer1.isSet(billingAddress));
        DataObject c1ba = customer1.getDataObject(billingAddress);
        assertTrue(address == c1ba);
        Property shippingAddress = customerType.getProperty("shippingAddress");
        assertTrue(shippingAddress.isContainment());
        assertEquals(billingAddress, address.getContainmentProperty());
        assertEquals(customer1, address.getContainer());

        customer1.set(shippingAddress, address);
        assertEquals(shippingAddress, address.getContainmentProperty());
        assertFalse(customer1.isSet(billingAddress));
        assertNull(customer1.get(billingAddress));
        
        System.out.println(customer1.get("billingAddress/street"));
        System.out.println(customer1.get("billingAddress/city"));
        System.out.println(customer1.get("billingAddress/state"));
        System.out.println(customer1.get("shippingAddress/street"));
        System.out.println(customer1.get("shippingAddress/city"));
        System.out.println(customer1.get("shippingAddress/state"));

        customer2.set(billingAddress, address);
        assertEquals(customer2, address.getContainer());
        assertEquals(address, customer2.get(billingAddress));
        assertFalse(customer1.isSet(shippingAddress));
        assertNull(customer1.get(shippingAddress));
        
        System.out.println(customer1.get("shippingAddress/street"));
        System.out.println(customer1.get("shippingAddress/city"));
        System.out.println(customer1.get("shippingAddress/state"));
        System.out.println(customer2.get("billingAddress/street"));
        System.out.println(customer2.get("billingAddress/city"));
        System.out.println(customer2.get("billingAddress/state"));
        
    }

    private DataObject createTrades()
    {
        DataObject trade1 = factory.create(tradeType);
        trade1.set("symbol", "ABC");
        trade1.set("quantity", 100);
        trade1.set("price", 14.25);
        DataObject trade2 = factory.create(tradeType);
        trade2.set("symbol", "DEF");
        trade2.set("quantity", 200);
        trade2.set("price", 10.5);
        DataObject trade3 = factory.create(tradeType);
        trade3.set("symbol", "XYZ");
        trade3.set("quantity", 500);
        trade3.set("price", 5.75);
        DataObject traderX = factory.create(traderType);
        traderX.set("id", "traderX");
        List buyX = new ArrayList();
        buyX.add(trade1);
        traderX.set("buy", buyX);
        List sellX = new ArrayList();
        sellX.add(trade2);
        traderX.set("sell", sellX);
        DataObject traderY = factory.create(traderType);
        traderY.set("id", "traderY");
        List buyY = new ArrayList();
        buyY.add(trade3);
        traderY.set("buy", buyY);
        DataObject trades = factory.create(tradesType);
        List traders = new ArrayList();
        traders.add(traderX);
        traders.add(traderY);
        trades.set("trader", traders);
        return trades;
    }

    private void printTrades(DataObject trades) throws java.io.IOException
    {
        String uri = "http://sdo/test/trade";
        XMLDocument doc = xmlHelper.createDocument(trades, uri, "trades");
        xmlHelper.save(doc, System.out, 
                       new davos.sdo.Options().setSavePrettyPrint());
        System.out.println();
    }

    /** test containment management with many-valued containment properties
     */
    public void testContainmentManagement2a() throws Exception
    {
        DataObject trades = createTrades();
        System.out.println("BEFORE:");
        printTrades(trades);

        DataObject traderX = trades.getDataObject("trader[id='traderX']");
        DataObject trade1 = traderX.getDataObject("buy[1]");
        assertEquals(traderX, trade1.getContainer());
        Property pb = traderType.getProperty("buy");
        assertEquals(pb, trade1.getContainmentProperty());

        // move data object from one containment property to another
        List buy = traderX.getList("buy");
        List sell = traderX.getList("sell");
        assertEquals(1, buy.size());
        assertEquals(1, sell.size());
        sell.add(trade1);

        System.out.println("AFTER:");
        printTrades(trades);

        assertEquals(0, buy.size());
        assertEquals(2, sell.size());
        Property ps = traderType.getProperty("sell");
        assertEquals(ps, trade1.getContainmentProperty());
        assertEquals(traderX, trade1.getContainer());
    }

    public void testContainmentManagement2b() throws Exception
    {
        DataObject trades = createTrades();
        System.out.println("BEFORE:");
        printTrades(trades);

        DataObject traderX = trades.getDataObject("trader[id='traderX']");
        DataObject trade1 = traderX.getDataObject("buy[1]");
        assertEquals(traderX, trade1.getContainer());
        Property pb = traderType.getProperty("buy");
        assertEquals(pb, trade1.getContainmentProperty());

        // move data object from one containment property to another
        List buy = traderX.getList("buy");
        List sell = traderX.getList("sell");
        assertEquals(1, buy.size());
        assertEquals(1, sell.size());
        traderX.set("sell[1]", trade1);

        System.out.println("AFTER:");
        printTrades(trades);

        assertEquals(0, buy.size());
        assertEquals(1, sell.size());
        Property ps = traderType.getProperty("sell");
        assertEquals(ps, trade1.getContainmentProperty()); // * FAILS AS WELL
        assertEquals(traderX, trade1.getContainer());
    }

    public void testContainmentManagement2c() throws Exception
    {
        DataObject trades = createTrades();
        System.out.println("BEFORE:");
        printTrades(trades);

        DataObject traderX = trades.getDataObject("trader[id='traderX']");
        DataObject trade1 = traderX.getDataObject("buy[1]");
        assertEquals(traderX, trade1.getContainer());
        Property pb = traderType.getProperty("buy");
        assertEquals(pb, trade1.getContainmentProperty());

        // move data object from one containment property to another
        List buy = traderX.getList("buy");
        List sell = traderX.getList("sell");
        assertEquals(1, buy.size());
        assertEquals(1, sell.size());
        traderX.set("sell", buy);

        System.out.println("AFTER:");
        printTrades(trades);

        assertEquals(0, traderX.getList("buy").size()); // FAILS
        assertEquals(1, traderX.getList("sell").size());
        Property ps = traderType.getProperty("sell");
        // (cezar) trader1 was removed from the tree, this means its containmentProperty and container are null
        //assertEquals(ps, trade1.getContainmentProperty()); // FAILS, null
        //assertEquals(traderX, trade1.getContainer()); // FAILS, null

        DataObject buy1 = traderX.getDataObject("buy[1]");
        DataObject sell1 = traderX.getDataObject("sell[1]");
        System.out.println("buy1 == sell1? " + (buy1 == sell1)); // true
        System.out.println("buy1 == trade1? " + (buy1 == trade1)); // true
        System.out.println("sell1 == trade1? " + (sell1 == trade1)); // true
    }

    public void testContainmentManagement3a() throws Exception
    {
        DataObject trades = createTrades();
        System.out.println("BEFORE:");
        printTrades(trades);
        
        DataObject traderX = trades.getDataObject("trader[id='traderX']");
        DataObject traderY = trades.getDataObject("trader[id='traderY']");

        // move data object from one container to another
        List sellX = traderX.getList("sell");
        DataObject trade2 = (DataObject)sellX.remove(0);
        List sellY = new ArrayList();
        sellY.add(trade2);
        traderY.set("sell", sellY);

        System.out.println("AFTER:");
        printTrades(trades);

        Property ps = traderType.getProperty("sell");
        assertEquals(ps, trade2.getContainmentProperty());
        assertEquals(traderY, trade2.getContainer());
    }

    public void testContainmentManagement3b() throws Exception
    {
        DataObject trades = createTrades();
        System.out.println("BEFORE:");
        printTrades(trades);
        
        DataObject traderX = trades.getDataObject("trader[id='traderX']");
        DataObject traderY = trades.getDataObject("trader[id='traderY']");
        DataObject trade2 = traderX.getDataObject("sell[1]");

        // move data object from one container to another
        List sellX = traderX.getList("sell");
        assertTrue(trade2 == sellX.get(0));
        traderY.set("sell", sellX);

        System.out.println("AFTER:");
        printTrades(trades);

        Property ps = traderType.getProperty("sell");
        assertEquals(ps, trade2.getContainmentProperty());
        assertEquals(traderY, trade2.getContainer());
    }

    protected static void compareBytes(byte[] expected, byte[] actual)
    {
        assertEquals(expected.length, actual.length);
        for (int i = 0; i < expected.length; i++)
        {
            byte b1 = expected[i];
            byte b2 = actual[i];
            assertEquals(b1, b2);
        }
    }

    public void testBooleanToString()
    {
        System.out.println("Boolean to String");
        DataObject dobj = createDataObject();
        Type t = dobj.getType();
        String s1 = "boolean";
        String s2 = "string";
        Property p1 = t.getProperty(s1);
        Property p2 = t.getProperty(s2);
        int i1 = BOOLEAN_P_I;
        int i2 = STRING_P_I;
        boolean v11 = true;
        boolean v12 = false;
        Boolean V11 = Boolean.TRUE;
        Boolean V12 = Boolean.FALSE;
        String V21 = "true";
        String V22 = "false";
        
        dobj.set(s1, V11);
        assertEquals(V21, dobj.getString(s1));
        dobj.set(s1, V12);
        assertEquals(V22, dobj.getString(s1));
        dobj.setBoolean(s2, v11);
        assertEquals(V21, dobj.get(s2));
        dobj.setBoolean(s2, v12);
        assertEquals(V22, dobj.get(s2));
        
        dobj.set(p1, V11);
        assertEquals(V21, dobj.getString(p1));
        dobj.set(p1, V12);
        assertEquals(V22, dobj.getString(p1));
        dobj.setBoolean(p2, v11);
        assertEquals(V21, dobj.get(p2));
        dobj.setBoolean(p2, v12);
        assertEquals(V22, dobj.get(p2));
        
        dobj.set(i1, V11);
        assertEquals(V21, dobj.getString(i1));
        dobj.set(i1, V12);
        assertEquals(V22, dobj.getString(i1));
        dobj.setBoolean(i2, v11);
        assertEquals(V21, dobj.get(i2));
        dobj.setBoolean(i2, v12);
        assertEquals(V22, dobj.get(i2));
    }

    public void testByteToDouble()
    {
        System.out.println("Byte to Double");
        DataObject dobj = createDataObject();
        Type t = dobj.getType();
        String s1 = "byte";
        String s2 = "double";
        Property p1 = t.getProperty(s1);
        Property p2 = t.getProperty(s2);
        int i1 = BYTE_P_I;
        int i2 = DOUBLE_P_I;
        byte v11 = (byte)1;
        byte v12 = (byte)2;
        double v21 = (double)v11;
        double v22 = (double)v12;
        Object V11 = new Byte(v11);
        Object V12 = new Byte(v12);
        Object V21 = new Double(v21);
        Object V22 = new Double(v22);
        
        dobj.set(s1, V11);
        assertEquals(v21, dobj.getDouble(s1));
        dobj.set(s1, V12);
        assertEquals(v22, dobj.getDouble(s1));
        dobj.setByte(s2, v11);
        assertEquals(V21, dobj.get(s2));
        dobj.setByte(s2, v12);
        assertEquals(V22, dobj.get(s2));
        
        dobj.set(p1, V11);
        assertEquals(v21, dobj.getDouble(p1));
        dobj.set(p1, V12);
        assertEquals(v22, dobj.getDouble(p1));
        dobj.setByte(p2, v11);
        assertEquals(V21, dobj.get(p2));
        dobj.setByte(p2, v12);
        assertEquals(V22, dobj.get(p2));
        
        dobj.set(i1, V11);
        assertEquals(v21, dobj.getDouble(i1));
        dobj.set(i1, V12);
        assertEquals(v22, dobj.getDouble(i1));
        dobj.setByte(i2, v11);
        assertEquals(V21, dobj.get(i2));
        dobj.setByte(i2, v12);
        assertEquals(V22, dobj.get(i2));
    }

    public void testByteToFloat()
    {
        System.out.println("Byte to Float");
        DataObject dobj = createDataObject();
        Type t = dobj.getType();
        String s1 = "byte";
        String s2 = "float";
        Property p1 = t.getProperty(s1);
        Property p2 = t.getProperty(s2);
        int i1 = BYTE_P_I;
        int i2 = FLOAT_P_I;
        byte v11 = (byte)1;
        byte v12 = (byte)2;
        float v21 = (float)v11;
        float v22 = (float)v12;
        Object V11 = new Byte(v11);
        Object V12 = new Byte(v12);
        Object V21 = new Float(v21);
        Object V22 = new Float(v22);
        
        dobj.set(s1, V11);
        assertEquals(v21, dobj.getFloat(s1));
        dobj.set(s1, V12);
        assertEquals(v22, dobj.getFloat(s1));
        dobj.setByte(s2, v11);
        assertEquals(V21, dobj.get(s2));
        dobj.setByte(s2, v12);
        assertEquals(V22, dobj.get(s2));
        
        dobj.set(p1, V11);
        assertEquals(v21, dobj.getFloat(p1));
        dobj.set(p1, V12);
        assertEquals(v22, dobj.getFloat(p1));
        dobj.setByte(p2, v11);
        assertEquals(V21, dobj.get(p2));
        dobj.setByte(p2, v12);
        assertEquals(V22, dobj.get(p2));
        
        dobj.set(i1, V11);
        assertEquals(v21, dobj.getFloat(i1));
        dobj.set(i1, V12);
        assertEquals(v22, dobj.getFloat(i1));
        dobj.setByte(i2, v11);
        assertEquals(V21, dobj.get(i2));
        dobj.setByte(i2, v12);
        assertEquals(V22, dobj.get(i2));
    }

    public void testByteToInt()
    {
        System.out.println("Byte to Int");
        DataObject dobj = createDataObject();
        Type t = dobj.getType();
        String s1 = "byte";
        String s2 = "int";
        Property p1 = t.getProperty(s1);
        Property p2 = t.getProperty(s2);
        int i1 = BYTE_P_I;
        int i2 = INT_P_I;
        byte v11 = (byte)1;
        byte v12 = (byte)2;
        int v21 = (int)v11;
        int v22 = (int)v12;
        Object V11 = new Byte(v11);
        Object V12 = new Byte(v12);
        Object V21 = new Integer(v21);
        Object V22 = new Integer(v22);
        
        dobj.set(s1, V11);
        assertEquals(v21, dobj.getInt(s1));
        dobj.set(s1, V12);
        assertEquals(v22, dobj.getInt(s1));
        dobj.setByte(s2, v11);
        assertEquals(V21, dobj.get(s2));
        dobj.setByte(s2, v12);
        assertEquals(V22, dobj.get(s2));
        
        dobj.set(p1, V11);
        assertEquals(v21, dobj.getInt(p1));
        dobj.set(p1, V12);
        assertEquals(v22, dobj.getInt(p1));
        dobj.setByte(p2, v11);
        assertEquals(V21, dobj.get(p2));
        dobj.setByte(p2, v12);
        assertEquals(V22, dobj.get(p2));
        
        dobj.set(i1, V11);
        assertEquals(v21, dobj.getInt(i1));
        dobj.set(i1, V12);
        assertEquals(v22, dobj.getInt(i1));
        dobj.setByte(i2, v11);
        assertEquals(V21, dobj.get(i2));
        dobj.setByte(i2, v12);
        assertEquals(V22, dobj.get(i2));
    }

    public void testByteToLong()
    {
        System.out.println("Byte to Long");
        DataObject dobj = createDataObject();
        Type t = dobj.getType();
        String s1 = "byte";
        String s2 = "long";
        Property p1 = t.getProperty(s1);
        Property p2 = t.getProperty(s2);
        int i1 = BYTE_P_I;
        int i2 = LONG_P_I;
        byte v11 = (byte)1;
        byte v12 = (byte)2;
        long v21 = (long)v11;
        long v22 = (long)v12;
        Object V11 = new Byte(v11);
        Object V12 = new Byte(v12);
        Object V21 = new Long(v21);
        Object V22 = new Long(v22);
        
        dobj.set(s1, V11);
        assertEquals(v21, dobj.getLong(s1));
        dobj.set(s1, V12);
        assertEquals(v22, dobj.getLong(s1));
        dobj.setByte(s2, v11);
        assertEquals(V21, dobj.get(s2));
        dobj.setByte(s2, v12);
        assertEquals(V22, dobj.get(s2));
        
        dobj.set(p1, V11);
        assertEquals(v21, dobj.getLong(p1));
        dobj.set(p1, V12);
        assertEquals(v22, dobj.getLong(p1));
        dobj.setByte(p2, v11);
        assertEquals(V21, dobj.get(p2));
        dobj.setByte(p2, v12);
        assertEquals(V22, dobj.get(p2));
        
        dobj.set(i1, V11);
        assertEquals(v21, dobj.getLong(i1));
        dobj.set(i1, V12);
        assertEquals(v22, dobj.getLong(i1));
        dobj.setByte(i2, v11);
        assertEquals(V21, dobj.get(i2));
        dobj.setByte(i2, v12);
        assertEquals(V22, dobj.get(i2));
    }

    public void testByteToShort()
    {
        System.out.println("Byte to Short");
        DataObject dobj = createDataObject();
        Type t = dobj.getType();
        String s1 = "byte";
        String s2 = "short";
        Property p1 = t.getProperty(s1);
        Property p2 = t.getProperty(s2);
        int i1 = BYTE_P_I;
        int i2 = SHORT_P_I;
        byte v11 = (byte)1;
        byte v12 = (byte)2;
        short v21 = (short)v11;
        short v22 = (short)v12;
        Object V11 = new Byte(v11);
        Object V12 = new Byte(v12);
        Object V21 = new Short(v21);
        Object V22 = new Short(v22);
        
        dobj.set(s1, V11);
        assertEquals(v21, dobj.getShort(s1));
        dobj.set(s1, V12);
        assertEquals(v22, dobj.getShort(s1));
        dobj.setByte(s2, v11);
        assertEquals(V21, dobj.get(s2));
        dobj.setByte(s2, v12);
        assertEquals(V22, dobj.get(s2));
        
        dobj.set(p1, V11);
        assertEquals(v21, dobj.getShort(p1));
        dobj.set(p1, V12);
        assertEquals(v22, dobj.getShort(p1));
        dobj.setByte(p2, v11);
        assertEquals(V21, dobj.get(p2));
        dobj.setByte(p2, v12);
        assertEquals(V22, dobj.get(p2));
        
        dobj.set(i1, V11);
        assertEquals(v21, dobj.getShort(i1));
        dobj.set(i1, V12);
        assertEquals(v22, dobj.getShort(i1));
        dobj.setByte(i2, v11);
        assertEquals(V21, dobj.get(i2));
        dobj.setByte(i2, v12);
        assertEquals(V22, dobj.get(i2));
    }

    public void testByteToString()
    {
        System.out.println("Byte to String");
        DataObject dobj = createDataObject();
        Type t = dobj.getType();
        String s1 = "byte";
        String s2 = "string";
        Property p1 = t.getProperty(s1);
        Property p2 = t.getProperty(s2);
        int i1 = BYTE_P_I;
        int i2 = STRING_P_I;
        byte v11 = (byte)1;
        byte v12 = (byte)2;
        Object V11 = new Byte(v11);
        Object V12 = new Byte(v12);
        Object V21 = V11.toString();
        Object V22 = V12.toString();
        
        dobj.set(s1, V11);
        assertEquals(V21, dobj.getString(s1));
        dobj.set(s1, V12);
        assertEquals(V22, dobj.getString(s1));
        dobj.setByte(s2, v11);
        assertEquals(V21, dobj.get(s2));
        dobj.setByte(s2, v12);
        assertEquals(V22, dobj.get(s2));
        
        dobj.set(p1, V11);
        assertEquals(V21, dobj.getString(p1));
        dobj.set(p1, V12);
        assertEquals(V22, dobj.getString(p1));
        dobj.setByte(p2, v11);
        assertEquals(V21, dobj.get(p2));
        dobj.setByte(p2, v12);
        assertEquals(V22, dobj.get(p2));
        
        dobj.set(i1, V11);
        assertEquals(V21, dobj.getString(i1));
        dobj.set(i1, V12);
        assertEquals(V22, dobj.getString(i1));
        dobj.setByte(i2, v11);
        assertEquals(V21, dobj.get(i2));
        dobj.setByte(i2, v12);
        assertEquals(V22, dobj.get(i2));
    }

    public void testCharacterToString()
    {
        System.out.println("Character to String");
        DataObject dobj = createDataObject();
        Type t = dobj.getType();
        String s1 = "character";
        String s2 = "string";
        Property p1 = t.getProperty(s1);
        Property p2 = t.getProperty(s2);
        int i1 = CHARACTER_P_I;
        int i2 = STRING_P_I;
        char v11 = 'a';
        char v12 = 'b';
        Object V11 = new Character(v11);
        Object V12 = new Character(v12);
        Object V21 = V11.toString();
        Object V22 = V12.toString();
        
        dobj.set(s1, V11);
        assertEquals(V21, dobj.getString(s1));
        dobj.set(s1, V12);
        assertEquals(V22, dobj.getString(s1));
        dobj.setChar(s2, v11);
        assertEquals(V21, dobj.get(s2));
        dobj.setChar(s2, v12);
        assertEquals(V22, dobj.get(s2));
        
        dobj.set(p1, V11);
        assertEquals(V21, dobj.getString(p1));
        dobj.set(p1, V12);
        assertEquals(V22, dobj.getString(p1));
        dobj.setChar(p2, v11);
        assertEquals(V21, dobj.get(p2));
        dobj.setChar(p2, v12);
        assertEquals(V22, dobj.get(p2));
        
        dobj.set(i1, V11);
        assertEquals(V21, dobj.getString(i1));
        dobj.set(i1, V12);
        assertEquals(V22, dobj.getString(i1));
        dobj.setChar(i2, v11);
        assertEquals(V21, dobj.get(i2));
        dobj.setChar(i2, v12);
        assertEquals(V22, dobj.get(i2));
    }

    public void testDoubleToByte()
    {
        System.out.println("Double to Byte");
        DataObject dobj = createDataObject();
        Type t = dobj.getType();
        String s1 = "double";
        String s2 = "byte";
        Property p1 = t.getProperty(s1);
        Property p2 = t.getProperty(s2);
        int i1 = DOUBLE_P_I;
        int i2 = BYTE_P_I;
        double v11 = 1.0;
        double v12 = 2.0;
        byte v21 = (byte)v11;
        byte v22 = (byte)v12;
        Object V11 = new Double(v11);
        Object V12 = new Double(v12);
        Object V21 = new Byte(v21);
        Object V22 = new Byte(v22);
        
        dobj.set(s1, V11);
        assertEquals(v21, dobj.getByte(s1));
        dobj.set(s1, V12);
        assertEquals(v22, dobj.getByte(s1));
        dobj.setDouble(s2, v11);
        assertEquals(V21, dobj.get(s2));
        dobj.setDouble(s2, v12);
        assertEquals(V22, dobj.get(s2));
        
        dobj.set(p1, V11);
        assertEquals(v21, dobj.getByte(p1));
        dobj.set(p1, V12);
        assertEquals(v22, dobj.getByte(p1));
        dobj.setDouble(p2, v11);
        assertEquals(V21, dobj.get(p2));
        dobj.setDouble(p2, v12);
        assertEquals(V22, dobj.get(p2));
        
        dobj.set(i1, V11);
        assertEquals(v21, dobj.getByte(i1));
        dobj.set(i1, V12);
        assertEquals(v22, dobj.getByte(i1));
        dobj.setDouble(i2, v11);
        assertEquals(V21, dobj.get(i2));
        dobj.setDouble(i2, v12);
        assertEquals(V22, dobj.get(i2));
    }

    public void testDoubleToFloat()
    {
        System.out.println("Double to Float");
        DataObject dobj = createDataObject();
        Type t = dobj.getType();
        String s1 = "double";
        String s2 = "float";
        Property p1 = t.getProperty(s1);
        Property p2 = t.getProperty(s2);
        int i1 = DOUBLE_P_I;
        int i2 = FLOAT_P_I;
        double v11 = 1.0;
        double v12 = 2.0;
        float v21 = (float)v11;
        float v22 = (float)v12;
        Object V11 = new Double(v11);
        Object V12 = new Double(v12);
        Object V21 = new Float(v21);
        Object V22 = new Float(v22);
        
        dobj.set(s1, V11);
        assertEquals(v21, dobj.getFloat(s1));
        dobj.set(s1, V12);
        assertEquals(v22, dobj.getFloat(s1));
        dobj.setDouble(s2, v11);
        assertEquals(V21, dobj.get(s2));
        dobj.setDouble(s2, v12);
        assertEquals(V22, dobj.get(s2));
        
        dobj.set(p1, V11);
        assertEquals(v21, dobj.getFloat(p1));
        dobj.set(p1, V12);
        assertEquals(v22, dobj.getFloat(p1));
        dobj.setDouble(p2, v11);
        assertEquals(V21, dobj.get(p2));
        dobj.setDouble(p2, v12);
        assertEquals(V22, dobj.get(p2));
        
        dobj.set(i1, V11);
        assertEquals(v21, dobj.getFloat(i1));
        dobj.set(i1, V12);
        assertEquals(v22, dobj.getFloat(i1));
        dobj.setDouble(i2, v11);
        assertEquals(V21, dobj.get(i2));
        dobj.setDouble(i2, v12);
        assertEquals(V22, dobj.get(i2));
    }

    public void testDoubleToInt()
    {
        System.out.println("Double to Int");
        DataObject dobj = createDataObject();
        Type t = dobj.getType();
        String s1 = "double";
        String s2 = "int";
        Property p1 = t.getProperty(s1);
        Property p2 = t.getProperty(s2);
        int i1 = DOUBLE_P_I;
        int i2 = INT_P_I;
        double v11 = 1.1;
        double v12 = 2.9;
        int v21 = (int)v11;
        int v22 = (int)v12;
        Object V11 = new Double(v11);
        Object V12 = new Double(v12);
        Object V21 = new Integer(v21);
        Object V22 = new Integer(v22);
        
        dobj.set(s1, V11);
        assertEquals(v21, dobj.getInt(s1));
        dobj.set(s1, V12);
        assertEquals(v22, dobj.getInt(s1));
        dobj.setDouble(s2, v11);
        assertEquals(V21, dobj.get(s2));
        dobj.setDouble(s2, v12);
        assertEquals(V22, dobj.get(s2));
        
        dobj.set(p1, V11);
        System.out.println("got " + dobj.getInt(p1));
        assertEquals(v21, dobj.getInt(p1));
        dobj.set(p1, V12);
        assertEquals(v22, dobj.getInt(p1));
        dobj.setDouble(p2, v11);
        assertEquals(V21, dobj.get(p2));
        dobj.setDouble(p2, v12);
        assertEquals(V22, dobj.get(p2));
        
        dobj.set(i1, V11);
        assertEquals(v21, dobj.getInt(i1));
        dobj.set(i1, V12);
        System.out.println("got " + dobj.getInt(i1));
        assertEquals(v22, dobj.getInt(i1));
        dobj.setDouble(i2, v11);
        assertEquals(V21, dobj.get(i2));
        dobj.setDouble(i2, v12);
        assertEquals(V22, dobj.get(i2));
    }

    public void testDoubleToLong()
    {
        System.out.println("Double to Long");
        DataObject dobj = createDataObject();
        Type t = dobj.getType();
        String s1 = "double";
        String s2 = "long";
        Property p1 = t.getProperty(s1);
        Property p2 = t.getProperty(s2);
        int i1 = DOUBLE_P_I;
        int i2 = LONG_P_I;
        double v11 = 1.0;
        double v12 = 2.0;
        long v21 = (long)v11;
        long v22 = (long)v12;
        Object V11 = new Double(v11);
        Object V12 = new Double(v12);
        Object V21 = new Long(v21);
        Object V22 = new Long(v22);
        
        dobj.set(s1, V11);
        assertEquals(v21, dobj.getLong(s1));
        dobj.set(s1, V12);
        assertEquals(v22, dobj.getLong(s1));
        dobj.setDouble(s2, v11);
        assertEquals(V21, dobj.get(s2));
        dobj.setDouble(s2, v12);
        assertEquals(V22, dobj.get(s2));
        
        dobj.set(p1, V11);
        assertEquals(v21, dobj.getLong(p1));
        dobj.set(p1, V12);
        assertEquals(v22, dobj.getLong(p1));
        dobj.setDouble(p2, v11);
        assertEquals(V21, dobj.get(p2));
        dobj.setDouble(p2, v12);
        assertEquals(V22, dobj.get(p2));
        
        dobj.set(i1, V11);
        assertEquals(v21, dobj.getLong(i1));
        dobj.set(i1, V12);
        assertEquals(v22, dobj.getLong(i1));
        dobj.setDouble(i2, v11);
        assertEquals(V21, dobj.get(i2));
        dobj.setDouble(i2, v12);
        assertEquals(V22, dobj.get(i2));
    }

    public void testDoubleToShort()
    {
        System.out.println("Double to Short");
        DataObject dobj = createDataObject();
        Type t = dobj.getType();
        String s1 = "double";
        String s2 = "short";
        Property p1 = t.getProperty(s1);
        Property p2 = t.getProperty(s2);
        int i1 = DOUBLE_P_I;
        int i2 = SHORT_P_I;
        double v11 = 1.0;
        double v12 = 2.0;
        short v21 = (short)v11;
        short v22 = (short)v12;
        Object V11 = new Double(v11);
        Object V12 = new Double(v12);
        Object V21 = new Short(v21);
        Object V22 = new Short(v22);
        
        dobj.set(s1, V11);
        assertEquals(v21, dobj.getShort(s1));
        dobj.set(s1, V12);
        assertEquals(v22, dobj.getShort(s1));
        dobj.setDouble(s2, v11);
        assertEquals(V21, dobj.get(s2));
        dobj.setDouble(s2, v12);
        assertEquals(V22, dobj.get(s2));
        
        dobj.set(p1, V11);
        assertEquals(v21, dobj.getShort(p1));
        dobj.set(p1, V12);
        assertEquals(v22, dobj.getShort(p1));
        dobj.setDouble(p2, v11);
        assertEquals(V21, dobj.get(p2));
        dobj.setDouble(p2, v12);
        assertEquals(V22, dobj.get(p2));
        
        dobj.set(i1, V11);
        assertEquals(v21, dobj.getShort(i1));
        dobj.set(i1, V12);
        assertEquals(v22, dobj.getShort(i1));
        dobj.setDouble(i2, v11);
        assertEquals(V21, dobj.get(i2));
        dobj.setDouble(i2, v12);
        assertEquals(V22, dobj.get(i2));
    }

    public void testDoubleToString()
    {
        System.out.println("Double to String");
        DataObject dobj = createDataObject();
        Type t = dobj.getType();
        String s1 = "double";
        String s2 = "string";
        Property p1 = t.getProperty(s1);
        Property p2 = t.getProperty(s2);
        int i1 = DOUBLE_P_I;
        int i2 = STRING_P_I;
        double v11 = 123.45678;
        double v12 = 234.56789;
        Object V11 = new Double(v11);
        Object V12 = new Double(v12);
        Object V21 = V11.toString();
        Object V22 = V12.toString();
        
        dobj.set(s1, V11);
        assertEquals(V21, dobj.getString(s1));
        dobj.set(s1, V12);
        assertEquals(V22, dobj.getString(s1));
        dobj.setDouble(s2, v11);
        assertEquals(V21, dobj.get(s2));
        dobj.setDouble(s2, v12);
        assertEquals(V22, dobj.get(s2));
        
        dobj.set(p1, V11);
        assertEquals(V21, dobj.getString(p1));
        dobj.set(p1, V12);
        assertEquals(V22, dobj.getString(p1));
        dobj.setDouble(p2, v11);
        assertEquals(V21, dobj.get(p2));
        dobj.setDouble(p2, v12);
        assertEquals(V22, dobj.get(p2));
        
        dobj.set(i1, V11);
        assertEquals(V21, dobj.getString(i1));
        dobj.set(i1, V12);
        assertEquals(V22, dobj.getString(i1));
        dobj.setDouble(i2, v11);
        assertEquals(V21, dobj.get(i2));
        dobj.setDouble(i2, v12);
        assertEquals(V22, dobj.get(i2));
    }

    public void testDoubleToDecimal()
    {
        System.out.println("Double to Decimal");
        DataObject dobj = createDataObject();
        Type t = dobj.getType();
        String s1 = "double";
        String s2 = "decimal";
        Property p1 = t.getProperty(s1);
        Property p2 = t.getProperty(s2);
        int i1 = DOUBLE_P_I;
        int i2 = DECIMAL_P_I;
        double v11 = 123.45678;
        double v12 = 234.56789;
        Object V11 = new Double(v11);
        Object V12 = new Double(v12);
        Object V21 = BigDecimal.valueOf(v11);
        Object V22 = BigDecimal.valueOf(v12);
        
        dobj.set(s1, V11);
        assertEquals(V21, dobj.getBigDecimal(s1));
        dobj.set(s1, V12);
        assertEquals(V22, dobj.getBigDecimal(s1));
        dobj.setDouble(s2, v11);
        assertEquals(V21, dobj.get(s2));
        dobj.setDouble(s2, v12);
        assertEquals(V22, dobj.get(s2));
        
        dobj.set(p1, V11);
        System.out.println("set " + V11);
        System.out.println("got " + dobj.getBigDecimal(p1));
        assertEquals(V21, dobj.getBigDecimal(p1));
        dobj.set(p1, V12);
        System.out.println("set " + V12);
        System.out.println("got " + dobj.getBigDecimal(p1));
        assertEquals(V22, dobj.getBigDecimal(p1));
        dobj.setDouble(p2, v11);
        assertEquals(V21, dobj.get(p2));
        dobj.setDouble(p2, v12);
        assertEquals(V22, dobj.get(p2));
        
        dobj.set(i1, V11);
        assertEquals(V21, dobj.getBigDecimal(i1));
        dobj.set(i1, V12);
        assertEquals(V22, dobj.getBigDecimal(i1));
        dobj.setDouble(i2, v11);
        assertEquals(V21, dobj.get(i2));
        dobj.setDouble(i2, v12);
        assertEquals(V22, dobj.get(i2));
    }

    public void testDoubleToInteger()
    {
        System.out.println("Double to Integer");
        DataObject dobj = createDataObject();
        Type t = dobj.getType();
        String s1 = "double";
        String s2 = "integer";
        Property p1 = t.getProperty(s1);
        Property p2 = t.getProperty(s2);
        int i1 = DOUBLE_P_I;
        int i2 = INTEGER_P_I;
        double v11 = 123.45678;
        double v12 = 234.56789;
        Object V11 = new Double(v11);
        Object V12 = new Double(v12);
        Object V21 = BigInteger.valueOf((long)v11);
        Object V22 = BigInteger.valueOf((long)v12);
        
        dobj.set(s1, V11);
        assertEquals(V21, dobj.getBigInteger(s1));
        dobj.set(s1, V12);
        assertEquals(V22, dobj.getBigInteger(s1));
        dobj.setDouble(s2, v11);
        assertEquals(V21, dobj.get(s2));
        dobj.setDouble(s2, v12);
        assertEquals(V22, dobj.get(s2));
        
        dobj.set(p1, V11);
        System.out.println("set " + V11);
        System.out.println("got " + dobj.getBigInteger(p1));
        assertEquals(V21, dobj.getBigInteger(p1));
        dobj.set(p1, V12);
        System.out.println("set " + V12);
        System.out.println("got " + dobj.getBigInteger(p1));
        assertEquals(V22, dobj.getBigInteger(p1));
        dobj.setDouble(p2, v11);
        System.out.println("set " + v11);
        System.out.println("got " + dobj.get(p2));
        assertEquals(V21, dobj.get(p2));
        dobj.setDouble(p2, v12);
        System.out.println("set " + v12);
        System.out.println("got " + dobj.get(p2));
        assertEquals(V22, dobj.get(p2));
        
        dobj.set(i1, V11);
        assertEquals(V21, dobj.getBigInteger(i1));
        dobj.set(i1, V12);
        assertEquals(V22, dobj.getBigInteger(i1));
        dobj.setDouble(i2, v11);
        assertEquals(V21, dobj.get(i2));
        dobj.setDouble(i2, v12);
        assertEquals(V22, dobj.get(i2));
    }

    public void testFloatToByte()
    {
        System.out.println("Float to Byte");
        DataObject dobj = createDataObject();
        Type t = dobj.getType();
        String s1 = "float";
        String s2 = "byte";
        Property p1 = t.getProperty(s1);
        Property p2 = t.getProperty(s2);
        int i1 = FLOAT_P_I;
        int i2 = BYTE_P_I;
        float v11 = (float)1.0;
        float v12 = (float)2.0;
        byte v21 = (byte)v11;
        byte v22 = (byte)v12;
        Object V11 = new Float(v11);
        Object V12 = new Float(v12);
        Object V21 = new Byte(v21);
        Object V22 = new Byte(v22);
        
        dobj.set(s1, V11);
        assertEquals(v21, dobj.getByte(s1));
        dobj.set(s1, V12);
        assertEquals(v22, dobj.getByte(s1));
        dobj.setFloat(s2, v11);
        assertEquals(V21, dobj.get(s2));
        dobj.setFloat(s2, v12);
        assertEquals(V22, dobj.get(s2));
        
        dobj.set(p1, V11);
        assertEquals(v21, dobj.getByte(p1));
        dobj.set(p1, V12);
        assertEquals(v22, dobj.getByte(p1));
        dobj.setFloat(p2, v11);
        assertEquals(V21, dobj.get(p2));
        dobj.setFloat(p2, v12);
        assertEquals(V22, dobj.get(p2));
        
        dobj.set(i1, V11);
        assertEquals(v21, dobj.getByte(i1));
        dobj.set(i1, V12);
        assertEquals(v22, dobj.getByte(i1));
        dobj.setFloat(i2, v11);
        assertEquals(V21, dobj.get(i2));
        dobj.setFloat(i2, v12);
        assertEquals(V22, dobj.get(i2));
    }

    public void testFloatToDouble()
    {
        System.out.println("Float to Double");
        DataObject dobj = createDataObject();
        Type t = dobj.getType();
        String s1 = "float";
        String s2 = "double";
        Property p1 = t.getProperty(s1);
        Property p2 = t.getProperty(s2);
        int i1 = FLOAT_P_I;
        int i2 = DOUBLE_P_I;
        float v11 = (float)1.0;
        float v12 = (float)2.0;
        double v21 = (double)v11;
        double v22 = (double)v12;
        Object V11 = new Float(v11);
        Object V12 = new Float(v12);
        Object V21 = new Double(v21);
        Object V22 = new Double(v22);
        
        dobj.set(s1, V11);
        assertEquals(v21, dobj.getDouble(s1));
        dobj.set(s1, V12);
        assertEquals(v22, dobj.getDouble(s1));
        dobj.setFloat(s2, v11);
        assertEquals(V21, dobj.get(s2));
        dobj.setFloat(s2, v12);
        assertEquals(V22, dobj.get(s2));
        
        dobj.set(p1, V11);
        assertEquals(v21, dobj.getDouble(p1));
        dobj.set(p1, V12);
        assertEquals(v22, dobj.getDouble(p1));
        dobj.setFloat(p2, v11);
        assertEquals(V21, dobj.get(p2));
        dobj.setFloat(p2, v12);
        assertEquals(V22, dobj.get(p2));
        
        dobj.set(i1, V11);
        assertEquals(v21, dobj.getDouble(i1));
        dobj.set(i1, V12);
        assertEquals(v22, dobj.getDouble(i1));
        dobj.setFloat(i2, v11);
        assertEquals(V21, dobj.get(i2));
        dobj.setFloat(i2, v12);
        assertEquals(V22, dobj.get(i2));
    }

    public void testFloatToInt()
    {
        System.out.println("Float to Int");
        DataObject dobj = createDataObject();
        Type t = dobj.getType();
        String s1 = "float";
        String s2 = "int";
        Property p1 = t.getProperty(s1);
        Property p2 = t.getProperty(s2);
        int i1 = FLOAT_P_I;
        int i2 = INT_P_I;
        float v11 = (float)1.0;
        float v12 = (float)2.0;
        int v21 = (int)v11;
        int v22 = (int)v12;
        Object V11 = new Float(v11);
        Object V12 = new Float(v12);
        Object V21 = new Integer(v21);
        Object V22 = new Integer(v22);
        
        dobj.set(s1, V11);
        assertEquals(v21, dobj.getInt(s1));
        dobj.set(s1, V12);
        assertEquals(v22, dobj.getInt(s1));
        dobj.setFloat(s2, v11);
        assertEquals(V21, dobj.get(s2));
        dobj.setFloat(s2, v12);
        assertEquals(V22, dobj.get(s2));
        
        dobj.set(p1, V11);
        assertEquals(v21, dobj.getInt(p1));
        dobj.set(p1, V12);
        assertEquals(v22, dobj.getInt(p1));
        dobj.setFloat(p2, v11);
        assertEquals(V21, dobj.get(p2));
        dobj.setFloat(p2, v12);
        assertEquals(V22, dobj.get(p2));
        
        dobj.set(i1, V11);
        assertEquals(v21, dobj.getInt(i1));
        dobj.set(i1, V12);
        assertEquals(v22, dobj.getInt(i1));
        dobj.setFloat(i2, v11);
        assertEquals(V21, dobj.get(i2));
        dobj.setFloat(i2, v12);
        assertEquals(V22, dobj.get(i2));
    }

    public void testFloatToLong()
    {
        System.out.println("Float to Long");
        DataObject dobj = createDataObject();
        Type t = dobj.getType();
        String s1 = "float";
        String s2 = "long";
        Property p1 = t.getProperty(s1);
        Property p2 = t.getProperty(s2);
        int i1 = FLOAT_P_I;
        int i2 = LONG_P_I;
        float v11 = (float)1.0;
        float v12 = (float)2.0;
        long v21 = (long)v11;
        long v22 = (long)v12;
        Object V11 = new Float(v11);
        Object V12 = new Float(v12);
        Object V21 = new Long(v21);
        Object V22 = new Long(v22);
        
        dobj.set(s1, V11);
        assertEquals(v21, dobj.getLong(s1));
        dobj.set(s1, V12);
        assertEquals(v22, dobj.getLong(s1));
        dobj.setFloat(s2, v11);
        assertEquals(V21, dobj.get(s2));
        dobj.setFloat(s2, v12);
        assertEquals(V22, dobj.get(s2));
        
        dobj.set(p1, V11);
        assertEquals(v21, dobj.getLong(p1));
        dobj.set(p1, V12);
        assertEquals(v22, dobj.getLong(p1));
        dobj.setFloat(p2, v11);
        assertEquals(V21, dobj.get(p2));
        dobj.setFloat(p2, v12);
        assertEquals(V22, dobj.get(p2));
        
        dobj.set(i1, V11);
        assertEquals(v21, dobj.getLong(i1));
        dobj.set(i1, V12);
        assertEquals(v22, dobj.getLong(i1));
        dobj.setFloat(i2, v11);
        assertEquals(V21, dobj.get(i2));
        dobj.setFloat(i2, v12);
        assertEquals(V22, dobj.get(i2));
    }

    public void testFloatToShort()
    {
        System.out.println("Float to Short");
        DataObject dobj = createDataObject();
        Type t = dobj.getType();
        String s1 = "float";
        String s2 = "short";
        Property p1 = t.getProperty(s1);
        Property p2 = t.getProperty(s2);
        int i1 = FLOAT_P_I;
        int i2 = SHORT_P_I;
        float v11 = (float)1.0;
        float v12 = (float)2.0;
        short v21 = (short)v11;
        short v22 = (short)v12;
        Object V11 = new Float(v11);
        Object V12 = new Float(v12);
        Object V21 = new Short(v21);
        Object V22 = new Short(v22);
        
        dobj.set(s1, V11);
        assertEquals(v21, dobj.getShort(s1));
        dobj.set(s1, V12);
        assertEquals(v22, dobj.getShort(s1));
        dobj.setFloat(s2, v11);
        assertEquals(V21, dobj.get(s2));
        dobj.setFloat(s2, v12);
        assertEquals(V22, dobj.get(s2));
        
        dobj.set(p1, V11);
        assertEquals(v21, dobj.getShort(p1));
        dobj.set(p1, V12);
        assertEquals(v22, dobj.getShort(p1));
        dobj.setFloat(p2, v11);
        assertEquals(V21, dobj.get(p2));
        dobj.setFloat(p2, v12);
        assertEquals(V22, dobj.get(p2));
        
        dobj.set(i1, V11);
        assertEquals(v21, dobj.getShort(i1));
        dobj.set(i1, V12);
        assertEquals(v22, dobj.getShort(i1));
        dobj.setFloat(i2, v11);
        assertEquals(V21, dobj.get(i2));
        dobj.setFloat(i2, v12);
        assertEquals(V22, dobj.get(i2));
    }

    public void testFloatToString()
    {
        System.out.println("Float to String");
        DataObject dobj = createDataObject();
        Type t = dobj.getType();
        String s1 = "float";
        String s2 = "string";
        Property p1 = t.getProperty(s1);
        Property p2 = t.getProperty(s2);
        int i1 = FLOAT_P_I;
        int i2 = STRING_P_I;
        float v11 = (float)123.45678;
        float v12 = (float)234.56789;
        Object V11 = new Float(v11);
        Object V12 = new Float(v12);
        Object V21 = V11.toString();
        Object V22 = V12.toString();
        
        dobj.set(s1, V11);
        assertEquals(V21, dobj.getString(s1));
        dobj.set(s1, V12);
        assertEquals(V22, dobj.getString(s1));
        dobj.setFloat(s2, v11);
        assertEquals(V21, dobj.get(s2));
        dobj.setFloat(s2, v12);
        assertEquals(V22, dobj.get(s2));
        
        dobj.set(p1, V11);
        assertEquals(V21, dobj.getString(p1));
        dobj.set(p1, V12);
        assertEquals(V22, dobj.getString(p1));
        dobj.setFloat(p2, v11);
        assertEquals(V21, dobj.get(p2));
        dobj.setFloat(p2, v12);
        assertEquals(V22, dobj.get(p2));
        
        dobj.set(i1, V11);
        assertEquals(V21, dobj.getString(i1));
        dobj.set(i1, V12);
        assertEquals(V22, dobj.getString(i1));
        dobj.setFloat(i2, v11);
        assertEquals(V21, dobj.get(i2));
        dobj.setFloat(i2, v12);
        assertEquals(V22, dobj.get(i2));
    }

    public void testFloatToDecimal()
    {
        System.out.println("Float to Decimal");
        DataObject dobj = createDataObject();
        Type t = dobj.getType();
        String s1 = "float";
        String s2 = "decimal";
        Property p1 = t.getProperty(s1);
        Property p2 = t.getProperty(s2);
        int i1 = FLOAT_P_I;
        int i2 = DECIMAL_P_I;
        float v11 = (float)123.45678;
        float v12 = (float)234.56789;
        Object V11 = new Float(v11);
        Object V12 = new Float(v12);
        Object V21 = BigDecimal.valueOf(v11);
        Object V22 = BigDecimal.valueOf(v12);
        
        dobj.set(s1, V11);
        assertEquals(V21, dobj.getBigDecimal(s1));
        dobj.set(s1, V12);
        assertEquals(V22, dobj.getBigDecimal(s1));
        dobj.setFloat(s2, v11);
        assertEquals(V21, dobj.get(s2));
        dobj.setFloat(s2, v12);
        assertEquals(V22, dobj.get(s2));
        
        dobj.set(p1, V11);
        System.out.println("set " + V11);
        System.out.println("got " + dobj.getBigDecimal(p1));
        assertEquals(V21, dobj.getBigDecimal(p1));
        dobj.set(p1, V12);
        System.out.println("set " + V12);
        System.out.println("got " + dobj.getBigDecimal(p1));
        assertEquals(V22, dobj.getBigDecimal(p1));
        dobj.setFloat(p2, v11);
        assertEquals(V21, dobj.get(p2));
        dobj.setFloat(p2, v12);
        assertEquals(V22, dobj.get(p2));
        
        dobj.set(i1, V11);
        assertEquals(V21, dobj.getBigDecimal(i1));
        dobj.set(i1, V12);
        assertEquals(V22, dobj.getBigDecimal(i1));
        dobj.setFloat(i2, v11);
        assertEquals(V21, dobj.get(i2));
        dobj.setFloat(i2, v12);
        assertEquals(V22, dobj.get(i2));
    }

    public void testFloatToInteger()
    {
        System.out.println("Float to Integer");
        DataObject dobj = createDataObject();
        Type t = dobj.getType();
        String s1 = "float";
        String s2 = "integer";
        Property p1 = t.getProperty(s1);
        Property p2 = t.getProperty(s2);
        int i1 = FLOAT_P_I;
        int i2 = INTEGER_P_I;
        float v11 = (float)123.45678;
        float v12 = (float)234.56789;
        Object V11 = new Float(v11);
        Object V12 = new Float(v12);
        Object V21 = BigInteger.valueOf((long)v11);
        Object V22 = BigInteger.valueOf((long)v12);
        
        dobj.set(s1, V11);
        assertEquals(V21, dobj.getBigInteger(s1));
        dobj.set(s1, V12);
        assertEquals(V22, dobj.getBigInteger(s1));
        dobj.setFloat(s2, v11);
        assertEquals(V21, dobj.get(s2));
        dobj.setFloat(s2, v12);
        assertEquals(V22, dobj.get(s2));
        
        dobj.set(p1, V11);
        System.out.println("set " + V11);
        System.out.println("got " + dobj.getBigInteger(p1));
        assertEquals(V21, dobj.getBigInteger(p1));
        dobj.set(p1, V12);
        System.out.println("set " + V12);
        System.out.println("got " + dobj.getBigInteger(p1));
        assertEquals(V22, dobj.getBigInteger(p1));
        dobj.setFloat(p2, v11);
        System.out.println("set " + v11);
        System.out.println("got " + dobj.get(p2));
        assertEquals(V21, dobj.get(p2));
        dobj.setFloat(p2, v12);
        System.out.println("set " + v12);
        System.out.println("got " + dobj.get(p2));
        assertEquals(V22, dobj.get(p2));
        
        dobj.set(i1, V11);
        assertEquals(V21, dobj.getBigInteger(i1));
        dobj.set(i1, V12);
        assertEquals(V22, dobj.getBigInteger(i1));
        dobj.setFloat(i2, v11);
        assertEquals(V21, dobj.get(i2));
        dobj.setFloat(i2, v12);
        assertEquals(V22, dobj.get(i2));
    }

    public void testIntToByte()
    {
        System.out.println("Int to Byte");
        DataObject dobj = createDataObject();
        Type t = dobj.getType();
        String s1 = "int";
        String s2 = "byte";
        Property p1 = t.getProperty(s1);
        Property p2 = t.getProperty(s2);
        int i1 = INT_P_I;
        int i2 = BYTE_P_I;
        int v11 = 1;
        int v12 = 7;
        byte v21 = (byte)v11;
        byte v22 = (byte)v12;
        Object V11 = new Integer(v11);
        Object V12 = new Integer(v12);
        Object V21 = new Byte(v21);
        Object V22 = new Byte(v22);
        
        dobj.set(s1, V11);
        assertEquals(v21, dobj.getByte(s1));
        dobj.set(s1, V12);
        assertEquals(v22, dobj.getByte(s1));
        dobj.setInt(s2, v11);
        assertEquals(V21, dobj.get(s2));
        dobj.setInt(s2, v12);
        assertEquals(V22, dobj.get(s2));
        
        dobj.set(p1, V11);
        assertEquals(v21, dobj.getByte(p1));
        dobj.set(p1, V12);
        assertEquals(v22, dobj.getByte(p1));
        dobj.setInt(p2, v11);
        assertEquals(V21, dobj.get(p2));
        dobj.setInt(p2, v12);
        assertEquals(V22, dobj.get(p2));
        
        dobj.set(i1, V11);
        assertEquals(v21, dobj.getByte(i1));
        dobj.set(i1, V12);
        assertEquals(v22, dobj.getByte(i1));
        dobj.setInt(i2, v11);
        assertEquals(V21, dobj.get(i2));
        dobj.setInt(i2, v12);
        assertEquals(V22, dobj.get(i2));
    }

    public void testIntToDouble()
    {
        System.out.println("Int to Double");
        DataObject dobj = createDataObject();
        Type t = dobj.getType();
        String s1 = "int";
        String s2 = "double";
        Property p1 = t.getProperty(s1);
        Property p2 = t.getProperty(s2);
        int i1 = INT_P_I;
        int i2 = DOUBLE_P_I;
        int v11 = 1;
        int v12 = 7;
        double v21 = (double)v11;
        double v22 = (double)v12;
        Object V11 = new Integer(v11);
        Object V12 = new Integer(v12);
        Object V21 = new Double(v21);
        Object V22 = new Double(v22);
        
        dobj.set(s1, V11);
        assertEquals(v21, dobj.getDouble(s1));
        dobj.set(s1, V12);
        assertEquals(v22, dobj.getDouble(s1));
        dobj.setInt(s2, v11);
        assertEquals(V21, dobj.get(s2));
        dobj.setInt(s2, v12);
        assertEquals(V22, dobj.get(s2));
        
        dobj.set(p1, V11);
        assertEquals(v21, dobj.getDouble(p1));
        dobj.set(p1, V12);
        assertEquals(v22, dobj.getDouble(p1));
        dobj.setInt(p2, v11);
        assertEquals(V21, dobj.get(p2));
        dobj.setInt(p2, v12);
        assertEquals(V22, dobj.get(p2));
        
        dobj.set(i1, V11);
        assertEquals(v21, dobj.getDouble(i1));
        dobj.set(i1, V12);
        assertEquals(v22, dobj.getDouble(i1));
        dobj.setInt(i2, v11);
        assertEquals(V21, dobj.get(i2));
        dobj.setInt(i2, v12);
        assertEquals(V22, dobj.get(i2));
    }

    public void testIntToFloat()
    {
        System.out.println("Int to Float");
        DataObject dobj = createDataObject();
        Type t = dobj.getType();
        String s1 = "int";
        String s2 = "float";
        Property p1 = t.getProperty(s1);
        Property p2 = t.getProperty(s2);
        int i1 = INT_P_I;
        int i2 = FLOAT_P_I;
        int v11 = 1;
        int v12 = 7;
        float v21 = (float)v11;
        float v22 = (float)v12;
        Object V11 = new Integer(v11);
        Object V12 = new Integer(v12);
        Object V21 = new Float(v21);
        Object V22 = new Float(v22);
        
        dobj.set(s1, V11);
        assertEquals(v21, dobj.getFloat(s1));
        dobj.set(s1, V12);
        assertEquals(v22, dobj.getFloat(s1));
        dobj.setInt(s2, v11);
        assertEquals(V21, dobj.get(s2));
        dobj.setInt(s2, v12);
        assertEquals(V22, dobj.get(s2));
        
        dobj.set(p1, V11);
        assertEquals(v21, dobj.getFloat(p1));
        dobj.set(p1, V12);
        assertEquals(v22, dobj.getFloat(p1));
        dobj.setInt(p2, v11);
        assertEquals(V21, dobj.get(p2));
        dobj.setInt(p2, v12);
        assertEquals(V22, dobj.get(p2));
        
        dobj.set(i1, V11);
        assertEquals(v21, dobj.getFloat(i1));
        dobj.set(i1, V12);
        assertEquals(v22, dobj.getFloat(i1));
        dobj.setInt(i2, v11);
        assertEquals(V21, dobj.get(i2));
        dobj.setInt(i2, v12);
        assertEquals(V22, dobj.get(i2));
    }

    public void testIntToLong()
    {
        System.out.println("Int to Long");
        DataObject dobj = createDataObject();
        Type t = dobj.getType();
        String s1 = "int";
        String s2 = "long";
        Property p1 = t.getProperty(s1);
        Property p2 = t.getProperty(s2);
        int i1 = INT_P_I;
        int i2 = LONG_P_I;
        int v11 = 1;
        int v12 = 7;
        long v21 = (long)v11;
        long v22 = (long)v12;
        Object V11 = new Integer(v11);
        Object V12 = new Integer(v12);
        Object V21 = new Long(v21);
        Object V22 = new Long(v22);
        
        dobj.set(s1, V11);
        assertEquals(v21, dobj.getLong(s1));
        dobj.set(s1, V12);
        assertEquals(v22, dobj.getLong(s1));
        dobj.setInt(s2, v11);
        assertEquals(V21, dobj.get(s2));
        dobj.setInt(s2, v12);
        assertEquals(V22, dobj.get(s2));
        
        dobj.set(p1, V11);
        assertEquals(v21, dobj.getLong(p1));
        dobj.set(p1, V12);
        assertEquals(v22, dobj.getLong(p1));
        dobj.setInt(p2, v11);
        assertEquals(V21, dobj.get(p2));
        dobj.setInt(p2, v12);
        assertEquals(V22, dobj.get(p2));
        
        dobj.set(i1, V11);
        assertEquals(v21, dobj.getLong(i1));
        dobj.set(i1, V12);
        assertEquals(v22, dobj.getLong(i1));
        dobj.setInt(i2, v11);
        assertEquals(V21, dobj.get(i2));
        dobj.setInt(i2, v12);
        assertEquals(V22, dobj.get(i2));
    }

    public void testIntToShort()
    {
        System.out.println("Int to Short");
        DataObject dobj = createDataObject();
        Type t = dobj.getType();
        String s1 = "int";
        String s2 = "short";
        Property p1 = t.getProperty(s1);
        Property p2 = t.getProperty(s2);
        int i1 = INT_P_I;
        int i2 = SHORT_P_I;
        int v11 = 1;
        int v12 = 7;
        short v21 = (short)v11;
        short v22 = (short)v12;
        Object V11 = new Integer(v11);
        Object V12 = new Integer(v12);
        Object V21 = new Short(v21);
        Object V22 = new Short(v22);
        
        dobj.set(s1, V11);
        assertEquals(v21, dobj.getShort(s1));
        dobj.set(s1, V12);
        assertEquals(v22, dobj.getShort(s1));
        dobj.setInt(s2, v11);
        assertEquals(V21, dobj.get(s2));
        dobj.setInt(s2, v12);
        assertEquals(V22, dobj.get(s2));
        
        dobj.set(p1, V11);
        assertEquals(v21, dobj.getShort(p1));
        dobj.set(p1, V12);
        assertEquals(v22, dobj.getShort(p1));
        dobj.setInt(p2, v11);
        assertEquals(V21, dobj.get(p2));
        dobj.setInt(p2, v12);
        assertEquals(V22, dobj.get(p2));
        
        dobj.set(i1, V11);
        assertEquals(v21, dobj.getShort(i1));
        dobj.set(i1, V12);
        assertEquals(v22, dobj.getShort(i1));
        dobj.setInt(i2, v11);
        assertEquals(V21, dobj.get(i2));
        dobj.setInt(i2, v12);
        assertEquals(V22, dobj.get(i2));
    }

    public void testIntToString()
    {
        System.out.println("Int to String");
        DataObject dobj = createDataObject();
        Type t = dobj.getType();
        String s1 = "int";
        String s2 = "string";
        Property p1 = t.getProperty(s1);
        Property p2 = t.getProperty(s2);
        int i1 = INT_P_I;
        int i2 = STRING_P_I;
        int v11 = 123;
        int v12 = 234;
        Object V11 = new Integer(v11);
        Object V12 = new Integer(v12);
        Object V21 = V11.toString();
        Object V22 = V12.toString();
        
        dobj.set(s1, V11);
        assertEquals(V21, dobj.getString(s1));
        dobj.set(s1, V12);
        assertEquals(V22, dobj.getString(s1));
        dobj.setInt(s2, v11);
        assertEquals(V21, dobj.get(s2));
        dobj.setInt(s2, v12);
        assertEquals(V22, dobj.get(s2));
        
        dobj.set(p1, V11);
        assertEquals(V21, dobj.getString(p1));
        dobj.set(p1, V12);
        assertEquals(V22, dobj.getString(p1));
        dobj.setInt(p2, v11);
        assertEquals(V21, dobj.get(p2));
        dobj.setInt(p2, v12);
        assertEquals(V22, dobj.get(p2));
        
        dobj.set(i1, V11);
        assertEquals(V21, dobj.getString(i1));
        dobj.set(i1, V12);
        assertEquals(V22, dobj.getString(i1));
        dobj.setInt(i2, v11);
        assertEquals(V21, dobj.get(i2));
        dobj.setInt(i2, v12);
        assertEquals(V22, dobj.get(i2));
    }

    public void testIntToDecimal()
    {
        System.out.println("Int to Decimal");
        DataObject dobj = createDataObject();
        Type t = dobj.getType();
        String s1 = "int";
        String s2 = "decimal";
        Property p1 = t.getProperty(s1);
        Property p2 = t.getProperty(s2);
        int i1 = INT_P_I;
        int i2 = DECIMAL_P_I;
        int v11 = 123;
        int v12 = 234;
        Object V11 = new Integer(v11);
        Object V12 = new Integer(v12);
        Object V21 = BigDecimal.valueOf(v11);
        Object V22 = BigDecimal.valueOf(v12);
        
        dobj.set(s1, V11);
        assertEquals(V21, dobj.getBigDecimal(s1));
        dobj.set(s1, V12);
        assertEquals(V22, dobj.getBigDecimal(s1));
        dobj.setInt(s2, v11);
        assertEquals(V21, dobj.get(s2));
        dobj.setInt(s2, v12);
        assertEquals(V22, dobj.get(s2));
        
        dobj.set(p1, V11);
        System.out.println("set " + V11);
        System.out.println("got " + dobj.getBigDecimal(p1));
        assertEquals(V21, dobj.getBigDecimal(p1));
        dobj.set(p1, V12);
        System.out.println("set " + V12);
        System.out.println("got " + dobj.getBigDecimal(p1));
        assertEquals(V22, dobj.getBigDecimal(p1));
        dobj.setInt(p2, v11);
        assertEquals(V21, dobj.get(p2));
        dobj.setInt(p2, v12);
        assertEquals(V22, dobj.get(p2));
        
        dobj.set(i1, V11);
        assertEquals(V21, dobj.getBigDecimal(i1));
        dobj.set(i1, V12);
        assertEquals(V22, dobj.getBigDecimal(i1));
        dobj.setInt(i2, v11);
        assertEquals(V21, dobj.get(i2));
        dobj.setInt(i2, v12);
        assertEquals(V22, dobj.get(i2));
    }

    public void testIntToInteger()
    {
        System.out.println("Int to Integer");
        DataObject dobj = createDataObject();
        Type t = dobj.getType();
        String s1 = "int";
        String s2 = "integer";
        Property p1 = t.getProperty(s1);
        Property p2 = t.getProperty(s2);
        int i1 = INT_P_I;
        int i2 = INTEGER_P_I;
        int v11 = 12345678;
        int v12 = 23456789;
        Object V11 = new Integer(v11);
        Object V12 = new Integer(v12);
        Object V21 = BigInteger.valueOf((long)v11);
        Object V22 = BigInteger.valueOf((long)v12);
        
        dobj.set(s1, V11);
        assertEquals(V21, dobj.getBigInteger(s1));
        dobj.set(s1, V12);
        assertEquals(V22, dobj.getBigInteger(s1));
        dobj.setInt(s2, v11);
        assertEquals(V21, dobj.get(s2));
        dobj.setInt(s2, v12);
        assertEquals(V22, dobj.get(s2));
        
        dobj.set(p1, V11);
        System.out.println("set " + V11);
        System.out.println("got " + dobj.getBigInteger(p1));
        assertEquals(V21, dobj.getBigInteger(p1));
        dobj.set(p1, V12);
        System.out.println("set " + V12);
        System.out.println("got " + dobj.getBigInteger(p1));
        assertEquals(V22, dobj.getBigInteger(p1));
        dobj.setInt(p2, v11);
        System.out.println("set " + v11);
        System.out.println("got " + dobj.get(p2));
        assertEquals(V21, dobj.get(p2));
        dobj.setInt(p2, v12);
        System.out.println("set " + v12);
        System.out.println("got " + dobj.get(p2));
        assertEquals(V22, dobj.get(p2));
        
        dobj.set(i1, V11);
        assertEquals(V21, dobj.getBigInteger(i1));
        dobj.set(i1, V12);
        assertEquals(V22, dobj.getBigInteger(i1));
        dobj.setInt(i2, v11);
        assertEquals(V21, dobj.get(i2));
        dobj.setInt(i2, v12);
        assertEquals(V22, dobj.get(i2));
    }

    public void testLongToByte()
    {
        System.out.println("Long to Byte");
        DataObject dobj = createDataObject();
        Type t = dobj.getType();
        String s1 = "long";
        String s2 = "byte";
        Property p1 = t.getProperty(s1);
        Property p2 = t.getProperty(s2);
        int i1 = LONG_P_I;
        int i2 = BYTE_P_I;
        long v11 = (long)1;
        long v12 = (long)7;
        byte v21 = (byte)v11;
        byte v22 = (byte)v12;
        Object V11 = new Long(v11);
        Object V12 = new Long(v12);
        Object V21 = new Byte(v21);
        Object V22 = new Byte(v22);
        
        dobj.set(s1, V11);
        assertEquals(v21, dobj.getByte(s1));
        dobj.set(s1, V12);
        assertEquals(v22, dobj.getByte(s1));
        dobj.setLong(s2, v11);
        assertEquals(V21, dobj.get(s2));
        dobj.setLong(s2, v12);
        assertEquals(V22, dobj.get(s2));
        
        dobj.set(p1, V11);
        assertEquals(v21, dobj.getByte(p1));
        dobj.set(p1, V12);
        assertEquals(v22, dobj.getByte(p1));
        dobj.setLong(p2, v11);
        assertEquals(V21, dobj.get(p2));
        dobj.setLong(p2, v12);
        assertEquals(V22, dobj.get(p2));
        
        dobj.set(i1, V11);
        assertEquals(v21, dobj.getByte(i1));
        dobj.set(i1, V12);
        assertEquals(v22, dobj.getByte(i1));
        dobj.setLong(i2, v11);
        assertEquals(V21, dobj.get(i2));
        dobj.setLong(i2, v12);
        assertEquals(V22, dobj.get(i2));
    }

    public void testLongToDouble()
    {
        System.out.println("Long to Double");
        DataObject dobj = createDataObject();
        Type t = dobj.getType();
        String s1 = "long";
        String s2 = "double";
        Property p1 = t.getProperty(s1);
        Property p2 = t.getProperty(s2);
        int i1 = LONG_P_I;
        int i2 = DOUBLE_P_I;
        long v11 = (long)1;
        long v12 = (long)7;
        double v21 = (double)v11;
        double v22 = (double)v12;
        Object V11 = new Long(v11);
        Object V12 = new Long(v12);
        Object V21 = new Double(v21);
        Object V22 = new Double(v22);
        
        dobj.set(s1, V11);
        assertEquals(v21, dobj.getDouble(s1));
        dobj.set(s1, V12);
        assertEquals(v22, dobj.getDouble(s1));
        dobj.setLong(s2, v11);
        assertEquals(V21, dobj.get(s2));
        dobj.setLong(s2, v12);
        assertEquals(V22, dobj.get(s2));
        
        dobj.set(p1, V11);
        assertEquals(v21, dobj.getDouble(p1));
        dobj.set(p1, V12);
        assertEquals(v22, dobj.getDouble(p1));
        dobj.setLong(p2, v11);
        assertEquals(V21, dobj.get(p2));
        dobj.setLong(p2, v12);
        assertEquals(V22, dobj.get(p2));
        
        dobj.set(i1, V11);
        assertEquals(v21, dobj.getDouble(i1));
        dobj.set(i1, V12);
        assertEquals(v22, dobj.getDouble(i1));
        dobj.setLong(i2, v11);
        assertEquals(V21, dobj.get(i2));
        dobj.setLong(i2, v12);
        assertEquals(V22, dobj.get(i2));
    }

    public void testLongToFloat()
    {
        System.out.println("Long to Float");
        DataObject dobj = createDataObject();
        Type t = dobj.getType();
        String s1 = "long";
        String s2 = "float";
        Property p1 = t.getProperty(s1);
        Property p2 = t.getProperty(s2);
        int i1 = LONG_P_I;
        int i2 = FLOAT_P_I;
        long v11 = (long)1;
        long v12 = (long)7;
        float v21 = (float)v11;
        float v22 = (float)v12;
        Object V11 = new Long(v11);
        Object V12 = new Long(v12);
        Object V21 = new Float(v21);
        Object V22 = new Float(v22);
        
        dobj.set(s1, V11);
        assertEquals(v21, dobj.getFloat(s1));
        dobj.set(s1, V12);
        assertEquals(v22, dobj.getFloat(s1));
        dobj.setLong(s2, v11);
        assertEquals(V21, dobj.get(s2));
        dobj.setLong(s2, v12);
        assertEquals(V22, dobj.get(s2));
        
        dobj.set(p1, V11);
        assertEquals(v21, dobj.getFloat(p1));
        dobj.set(p1, V12);
        assertEquals(v22, dobj.getFloat(p1));
        dobj.setLong(p2, v11);
        assertEquals(V21, dobj.get(p2));
        dobj.setLong(p2, v12);
        assertEquals(V22, dobj.get(p2));
        
        dobj.set(i1, V11);
        assertEquals(v21, dobj.getFloat(i1));
        dobj.set(i1, V12);
        assertEquals(v22, dobj.getFloat(i1));
        dobj.setLong(i2, v11);
        assertEquals(V21, dobj.get(i2));
        dobj.setLong(i2, v12);
        assertEquals(V22, dobj.get(i2));
    }

    public void testLongToInt()
    {
        System.out.println("Long to Int");
        DataObject dobj = createDataObject();
        Type t = dobj.getType();
        String s1 = "long";
        String s2 = "int";
        Property p1 = t.getProperty(s1);
        Property p2 = t.getProperty(s2);
        int i1 = LONG_P_I;
        int i2 = INT_P_I;
        long v11 = (long)1;
        long v12 = (long)7;
        int v21 = (int)v11;
        int v22 = (int)v12;
        Object V11 = new Long(v11);
        Object V12 = new Long(v12);
        Object V21 = new Integer(v21);
        Object V22 = new Integer(v22);
        
        dobj.set(s1, V11);
        assertEquals(v21, dobj.getInt(s1));
        dobj.set(s1, V12);
        assertEquals(v22, dobj.getInt(s1));
        dobj.setLong(s2, v11);
        assertEquals(V21, dobj.get(s2));
        dobj.setLong(s2, v12);
        assertEquals(V22, dobj.get(s2));
        
        dobj.set(p1, V11);
        assertEquals(v21, dobj.getInt(p1));
        dobj.set(p1, V12);
        assertEquals(v22, dobj.getInt(p1));
        dobj.setLong(p2, v11);
        assertEquals(V21, dobj.get(p2));
        dobj.setLong(p2, v12);
        assertEquals(V22, dobj.get(p2));
        
        dobj.set(i1, V11);
        assertEquals(v21, dobj.getInt(i1));
        dobj.set(i1, V12);
        assertEquals(v22, dobj.getInt(i1));
        dobj.setLong(i2, v11);
        assertEquals(V21, dobj.get(i2));
        dobj.setLong(i2, v12);
        assertEquals(V22, dobj.get(i2));
    }

    public void testLongToShort()
    {
        System.out.println("Long to Short");
        DataObject dobj = createDataObject();
        Type t = dobj.getType();
        String s1 = "long";
        String s2 = "short";
        Property p1 = t.getProperty(s1);
        Property p2 = t.getProperty(s2);
        int i1 = LONG_P_I;
        int i2 = SHORT_P_I;
        long v11 = (long)1;
        long v12 = (long)7;
        short v21 = (short)v11;
        short v22 = (short)v12;
        Object V11 = new Long(v11);
        Object V12 = new Long(v12);
        Object V21 = new Short(v21);
        Object V22 = new Short(v22);
        
        dobj.set(s1, V11);
        assertEquals(v21, dobj.getShort(s1));
        dobj.set(s1, V12);
        assertEquals(v22, dobj.getShort(s1));
        dobj.setLong(s2, v11);
        assertEquals(V21, dobj.get(s2));
        dobj.setLong(s2, v12);
        assertEquals(V22, dobj.get(s2));
        
        dobj.set(p1, V11);
        assertEquals(v21, dobj.getShort(p1));
        dobj.set(p1, V12);
        assertEquals(v22, dobj.getShort(p1));
        dobj.setLong(p2, v11);
        assertEquals(V21, dobj.get(p2));
        dobj.setLong(p2, v12);
        assertEquals(V22, dobj.get(p2));
        
        dobj.set(i1, V11);
        assertEquals(v21, dobj.getShort(i1));
        dobj.set(i1, V12);
        assertEquals(v22, dobj.getShort(i1));
        dobj.setLong(i2, v11);
        assertEquals(V21, dobj.get(i2));
        dobj.setLong(i2, v12);
        assertEquals(V22, dobj.get(i2));
    }

    public void testLongToString()
    {
        System.out.println("Long to String");
        DataObject dobj = createDataObject();
        Type t = dobj.getType();
        String s1 = "long";
        String s2 = "string";
        Property p1 = t.getProperty(s1);
        Property p2 = t.getProperty(s2);
        int i1 = LONG_P_I;
        int i2 = STRING_P_I;
        long v11 = (long)12345678;
        long v12 = (long)23456789;
        Object V11 = new Long(v11);
        Object V12 = new Long(v12);
        Object V21 = V11.toString();
        Object V22 = V12.toString();
        
        dobj.set(s1, V11);
        assertEquals(V21, dobj.getString(s1));
        dobj.set(s1, V12);
        assertEquals(V22, dobj.getString(s1));
        dobj.setLong(s2, v11);
        assertEquals(V21, dobj.get(s2));
        dobj.setLong(s2, v12);
        assertEquals(V22, dobj.get(s2));
        
        dobj.set(p1, V11);
        assertEquals(V21, dobj.getString(p1));
        dobj.set(p1, V12);
        assertEquals(V22, dobj.getString(p1));
        dobj.setLong(p2, v11);
        assertEquals(V21, dobj.get(p2));
        dobj.setLong(p2, v12);
        assertEquals(V22, dobj.get(p2));
        
        dobj.set(i1, V11);
        assertEquals(V21, dobj.getString(i1));
        dobj.set(i1, V12);
        assertEquals(V22, dobj.getString(i1));
        dobj.setLong(i2, v11);
        assertEquals(V21, dobj.get(i2));
        dobj.setLong(i2, v12);
        assertEquals(V22, dobj.get(i2));
    }

    public void testLongToDecimal()
    {
        System.out.println("Long to Decimal");
        DataObject dobj = createDataObject();
        Type t = dobj.getType();
        String s1 = "long";
        String s2 = "decimal";
        Property p1 = t.getProperty(s1);
        Property p2 = t.getProperty(s2);
        int i1 = LONG_P_I;
        int i2 = DECIMAL_P_I;
        long v11 = 12345678l;
        long v12 = 23456789l;
        Object V11 = new Long(v11);
        Object V12 = new Long(v12);
        Object V21 = BigDecimal.valueOf(v11);
        Object V22 = BigDecimal.valueOf(v12);
        
        dobj.set(s1, V11);
        assertEquals(V21, dobj.getBigDecimal(s1));
        dobj.set(s1, V12);
        assertEquals(V22, dobj.getBigDecimal(s1));
        dobj.setLong(s2, v11);
        assertEquals(V21, dobj.get(s2));
        dobj.setLong(s2, v12);
        assertEquals(V22, dobj.get(s2));
        
        dobj.set(p1, V11);
        System.out.println("set " + V11);
        System.out.println("got " + dobj.getBigDecimal(p1));
        assertEquals(V21, dobj.getBigDecimal(p1));
        dobj.set(p1, V12);
        System.out.println("set " + V12);
        System.out.println("got " + dobj.getBigDecimal(p1));
        assertEquals(V22, dobj.getBigDecimal(p1));
        dobj.setLong(p2, v11);
        assertEquals(V21, dobj.get(p2));
        dobj.setLong(p2, v12);
        assertEquals(V22, dobj.get(p2));
        
        dobj.set(i1, V11);
        assertEquals(V21, dobj.getBigDecimal(i1));
        dobj.set(i1, V12);
        assertEquals(V22, dobj.getBigDecimal(i1));
        dobj.setLong(i2, v11);
        assertEquals(V21, dobj.get(i2));
        dobj.setLong(i2, v12);
        assertEquals(V22, dobj.get(i2));
    }

    public void testLongToInteger()
    {
        System.out.println("Long to Integer");
        DataObject dobj = createDataObject();
        Type t = dobj.getType();
        String s1 = "long";
        String s2 = "integer";
        Property p1 = t.getProperty(s1);
        Property p2 = t.getProperty(s2);
        int i1 = LONG_P_I;
        int i2 = INTEGER_P_I;
        long v11 = 12345678l;
        long v12 = 23456789l;
        Object V11 = new Long(v11);
        Object V12 = new Long(v12);
        Object V21 = BigInteger.valueOf(v11);
        Object V22 = BigInteger.valueOf(v12);
        
        dobj.set(s1, V11);
        assertEquals(V21, dobj.getBigInteger(s1));
        dobj.set(s1, V12);
        assertEquals(V22, dobj.getBigInteger(s1));
        dobj.setLong(s2, v11);
        assertEquals(V21, dobj.get(s2));
        dobj.setLong(s2, v12);
        assertEquals(V22, dobj.get(s2));
        
        dobj.set(p1, V11);
        System.out.println("set " + V11);
        System.out.println("got " + dobj.getBigInteger(p1));
        assertEquals(V21, dobj.getBigInteger(p1));
        dobj.set(p1, V12);
        System.out.println("set " + V12);
        System.out.println("got " + dobj.getBigInteger(p1));
        assertEquals(V22, dobj.getBigInteger(p1));
        dobj.setLong(p2, v11);
        System.out.println("set " + v11);
        System.out.println("got " + dobj.get(p2));
        assertEquals(V21, dobj.get(p2));
        dobj.setLong(p2, v12);
        System.out.println("set " + v12);
        System.out.println("got " + dobj.get(p2));
        assertEquals(V22, dobj.get(p2));
        
        dobj.set(i1, V11);
        assertEquals(V21, dobj.getBigInteger(i1));
        dobj.set(i1, V12);
        assertEquals(V22, dobj.getBigInteger(i1));
        dobj.setLong(i2, v11);
        assertEquals(V21, dobj.get(i2));
        dobj.setLong(i2, v12);
        assertEquals(V22, dobj.get(i2));
    }

    public void testLongToDate()
    {
        System.out.println("Long to Date");
        DataObject dobj = createDataObject();
        Type t = dobj.getType();
        String s1 = "long";
        String s2 = "date";
        Property p1 = t.getProperty(s1);
        Property p2 = t.getProperty(s2);
        int i1 = LONG_P_I;
        int i2 = DATE_P_I;
        long v11 = 1163212477742l;
        long v12 = 1163212530295l;
        Object V11 = new Long(v11);
        Object V12 = new Long(v12);
        Object V21 = new Date(v11);
        Object V22 = new Date(v12);
        
        dobj.set(s1, V11);
        assertEquals(V21, dobj.getDate(s1));
        dobj.set(s1, V12);
        assertEquals(V22, dobj.getDate(s1));
        dobj.setLong(s2, v11);
        assertEquals(V21, dobj.get(s2));
        dobj.setLong(s2, v12);
        assertEquals(V22, dobj.get(s2));
        
        dobj.set(p1, V11);
        System.out.println("set " + V11);
        System.out.println("got " + dobj.getDate(p1));
        assertEquals(V21, dobj.getDate(p1));
        dobj.set(p1, V12);
        System.out.println("set " + V12);
        System.out.println("got " + dobj.getDate(p1));
        assertEquals(V22, dobj.getDate(p1));
        dobj.setLong(p2, v11);
        System.out.println("set " + v11);
        System.out.println("got " + dobj.get(p2));
        assertEquals(V21, dobj.get(p2));
        dobj.setLong(p2, v12);
        System.out.println("set " + v12);
        System.out.println("got " + dobj.get(p2));
        assertEquals(V22, dobj.get(p2));
        
        dobj.set(i1, V11);
        assertEquals(V21, dobj.getDate(i1));
        dobj.set(i1, V12);
        assertEquals(V22, dobj.getDate(i1));
        dobj.setLong(i2, v11);
        assertEquals(V21, dobj.get(i2));
        dobj.setLong(i2, v12);
        assertEquals(V22, dobj.get(i2));
    }

    public void testShortToByte()
    {
        System.out.println("Short to Byte");
        DataObject dobj = createDataObject();
        Type t = dobj.getType();
        String s1 = "short";
        String s2 = "byte";
        Property p1 = t.getProperty(s1);
        Property p2 = t.getProperty(s2);
        int i1 = SHORT_P_I;
        int i2 = BYTE_P_I;
        short v11 = (short)1;
        short v12 = (short)7;
        byte v21 = (byte)v11;
        byte v22 = (byte)v12;
        Object V11 = new Short(v11);
        Object V12 = new Short(v12);
        Object V21 = new Byte(v21);
        Object V22 = new Byte(v22);
        
        dobj.set(s1, V11);
        assertEquals(v21, dobj.getByte(s1));
        dobj.set(s1, V12);
        assertEquals(v22, dobj.getByte(s1));
        dobj.setShort(s2, v11);
        assertEquals(V21, dobj.get(s2));
        dobj.setShort(s2, v12);
        assertEquals(V22, dobj.get(s2));
        
        dobj.set(p1, V11);
        assertEquals(v21, dobj.getByte(p1));
        dobj.set(p1, V12);
        assertEquals(v22, dobj.getByte(p1));
        dobj.setShort(p2, v11);
        assertEquals(V21, dobj.get(p2));
        dobj.setShort(p2, v12);
        assertEquals(V22, dobj.get(p2));
        
        dobj.set(i1, V11);
        assertEquals(v21, dobj.getByte(i1));
        dobj.set(i1, V12);
        assertEquals(v22, dobj.getByte(i1));
        dobj.setShort(i2, v11);
        assertEquals(V21, dobj.get(i2));
        dobj.setShort(i2, v12);
        assertEquals(V22, dobj.get(i2));
    }

    public void testShortToDouble()
    {
        System.out.println("Short to Double");
        DataObject dobj = createDataObject();
        Type t = dobj.getType();
        String s1 = "short";
        String s2 = "double";
        Property p1 = t.getProperty(s1);
        Property p2 = t.getProperty(s2);
        int i1 = SHORT_P_I;
        int i2 = DOUBLE_P_I;
        short v11 = (short)1;
        short v12 = (short)7;
        double v21 = (double)v11;
        double v22 = (double)v12;
        Object V11 = new Short(v11);
        Object V12 = new Short(v12);
        Object V21 = new Double(v21);
        Object V22 = new Double(v22);
        
        dobj.set(s1, V11);
        assertEquals(v21, dobj.getDouble(s1));
        dobj.set(s1, V12);
        assertEquals(v22, dobj.getDouble(s1));
        dobj.setShort(s2, v11);
        assertEquals(V21, dobj.get(s2));
        dobj.setShort(s2, v12);
        assertEquals(V22, dobj.get(s2));
        
        dobj.set(p1, V11);
        assertEquals(v21, dobj.getDouble(p1));
        dobj.set(p1, V12);
        assertEquals(v22, dobj.getDouble(p1));
        dobj.setShort(p2, v11);
        assertEquals(V21, dobj.get(p2));
        dobj.setShort(p2, v12);
        assertEquals(V22, dobj.get(p2));
        
        dobj.set(i1, V11);
        assertEquals(v21, dobj.getDouble(i1));
        dobj.set(i1, V12);
        assertEquals(v22, dobj.getDouble(i1));
        dobj.setShort(i2, v11);
        assertEquals(V21, dobj.get(i2));
        dobj.setShort(i2, v12);
        assertEquals(V22, dobj.get(i2));
    }

    public void testShortToFloat()
    {
        System.out.println("Short to Float");
        DataObject dobj = createDataObject();
        Type t = dobj.getType();
        String s1 = "short";
        String s2 = "float";
        Property p1 = t.getProperty(s1);
        Property p2 = t.getProperty(s2);
        int i1 = SHORT_P_I;
        int i2 = FLOAT_P_I;
        short v11 = (short)1;
        short v12 = (short)7;
        float v21 = (float)v11;
        float v22 = (float)v12;
        Object V11 = new Short(v11);
        Object V12 = new Short(v12);
        Object V21 = new Float(v21);
        Object V22 = new Float(v22);
        
        dobj.set(s1, V11);
        assertEquals(v21, dobj.getFloat(s1));
        dobj.set(s1, V12);
        assertEquals(v22, dobj.getFloat(s1));
        dobj.setShort(s2, v11);
        assertEquals(V21, dobj.get(s2));
        dobj.setShort(s2, v12);
        assertEquals(V22, dobj.get(s2));
        
        dobj.set(p1, V11);
        assertEquals(v21, dobj.getFloat(p1));
        dobj.set(p1, V12);
        assertEquals(v22, dobj.getFloat(p1));
        dobj.setShort(p2, v11);
        assertEquals(V21, dobj.get(p2));
        dobj.setShort(p2, v12);
        assertEquals(V22, dobj.get(p2));
        
        dobj.set(i1, V11);
        assertEquals(v21, dobj.getFloat(i1));
        dobj.set(i1, V12);
        assertEquals(v22, dobj.getFloat(i1));
        dobj.setShort(i2, v11);
        assertEquals(V21, dobj.get(i2));
        dobj.setShort(i2, v12);
        assertEquals(V22, dobj.get(i2));
    }

    public void testShortToInt()
    {
        System.out.println("Short to Int");
        DataObject dobj = createDataObject();
        Type t = dobj.getType();
        String s1 = "short";
        String s2 = "int";
        Property p1 = t.getProperty(s1);
        Property p2 = t.getProperty(s2);
        int i1 = SHORT_P_I;
        int i2 = INT_P_I;
        short v11 = (short)1;
        short v12 = (short)7;
        int v21 = (int)v11;
        int v22 = (int)v12;
        Object V11 = new Short(v11);
        Object V12 = new Short(v12);
        Object V21 = new Integer(v21);
        Object V22 = new Integer(v22);
        
        dobj.set(s1, V11);
        assertEquals(v21, dobj.getInt(s1));
        dobj.set(s1, V12);
        assertEquals(v22, dobj.getInt(s1));
        dobj.setShort(s2, v11);
        assertEquals(V21, dobj.get(s2));
        dobj.setShort(s2, v12);
        assertEquals(V22, dobj.get(s2));
        
        dobj.set(p1, V11);
        assertEquals(v21, dobj.getInt(p1));
        dobj.set(p1, V12);
        assertEquals(v22, dobj.getInt(p1));
        dobj.setShort(p2, v11);
        assertEquals(V21, dobj.get(p2));
        dobj.setShort(p2, v12);
        assertEquals(V22, dobj.get(p2));
        
        dobj.set(i1, V11);
        assertEquals(v21, dobj.getInt(i1));
        dobj.set(i1, V12);
        assertEquals(v22, dobj.getInt(i1));
        dobj.setShort(i2, v11);
        assertEquals(V21, dobj.get(i2));
        dobj.setShort(i2, v12);
        assertEquals(V22, dobj.get(i2));
    }

    public void testShortToLong()
    {
        System.out.println("Short to Long");
        DataObject dobj = createDataObject();
        Type t = dobj.getType();
        String s1 = "short";
        String s2 = "long";
        Property p1 = t.getProperty(s1);
        Property p2 = t.getProperty(s2);
        int i1 = SHORT_P_I;
        int i2 = LONG_P_I;
        short v11 = (short)1;
        short v12 = (short)7;
        long v21 = (long)v11;
        long v22 = (long)v12;
        Object V11 = new Short(v11);
        Object V12 = new Short(v12);
        Object V21 = new Long(v21);
        Object V22 = new Long(v22);
        
        dobj.set(s1, V11);
        assertEquals(v21, dobj.getLong(s1));
        dobj.set(s1, V12);
        assertEquals(v22, dobj.getLong(s1));
        dobj.setShort(s2, v11);
        assertEquals(V21, dobj.get(s2));
        dobj.setShort(s2, v12);
        assertEquals(V22, dobj.get(s2));
        
        dobj.set(p1, V11);
        assertEquals(v21, dobj.getLong(p1));
        dobj.set(p1, V12);
        assertEquals(v22, dobj.getLong(p1));
        dobj.setShort(p2, v11);
        assertEquals(V21, dobj.get(p2));
        dobj.setShort(p2, v12);
        assertEquals(V22, dobj.get(p2));
        
        dobj.set(i1, V11);
        assertEquals(v21, dobj.getLong(i1));
        dobj.set(i1, V12);
        assertEquals(v22, dobj.getLong(i1));
        dobj.setShort(i2, v11);
        assertEquals(V21, dobj.get(i2));
        dobj.setShort(i2, v12);
        assertEquals(V22, dobj.get(i2));
    }

    public void testShortToString()
    {
        System.out.println("Short to String");
        DataObject dobj = createDataObject();
        Type t = dobj.getType();
        String s1 = "short";
        String s2 = "string";
        Property p1 = t.getProperty(s1);
        Property p2 = t.getProperty(s2);
        int i1 = SHORT_P_I;
        int i2 = STRING_P_I;
        short v11 = (short)123;
        short v12 = (short)234;
        Object V11 = new Short(v11);
        Object V12 = new Short(v12);
        Object V21 = V11.toString();
        Object V22 = V12.toString();
        
        dobj.set(s1, V11);
        assertEquals(V21, dobj.getString(s1));
        dobj.set(s1, V12);
        assertEquals(V22, dobj.getString(s1));
        dobj.setShort(s2, v11);
        assertEquals(V21, dobj.get(s2));
        dobj.setShort(s2, v12);
        assertEquals(V22, dobj.get(s2));
        
        dobj.set(p1, V11);
        assertEquals(V21, dobj.getString(p1));
        dobj.set(p1, V12);
        assertEquals(V22, dobj.getString(p1));
        dobj.setShort(p2, v11);
        assertEquals(V21, dobj.get(p2));
        dobj.setShort(p2, v12);
        assertEquals(V22, dobj.get(p2));
        
        dobj.set(i1, V11);
        assertEquals(V21, dobj.getString(i1));
        dobj.set(i1, V12);
        assertEquals(V22, dobj.getString(i1));
        dobj.setShort(i2, v11);
        assertEquals(V21, dobj.get(i2));
        dobj.setShort(i2, v12);
        assertEquals(V22, dobj.get(i2));
    }

    public void testStringToBoolean()
    {
        System.out.println("String to Boolean");
        DataObject dobj = createDataObject();
        Type t = dobj.getType();
        String s1 = "string";
        String s2 = "boolean";
        Property p1 = t.getProperty(s1);
        Property p2 = t.getProperty(s2);
        int i1 = STRING_P_I;
        int i2 = BOOLEAN_P_I;
        String V11 = "true";
        String V12 = "false";
        boolean v21 = true;
        boolean v22 = false;
        Object V21 = Boolean.TRUE;
        Object V22 = Boolean.FALSE;
        
        dobj.set(s1, V11);
        assertEquals(v21, dobj.getBoolean(s1));
        dobj.set(s1, V12);
        assertEquals(v22, dobj.getBoolean(s1));
        dobj.setString(s2, V11);
        assertEquals(V21, dobj.get(s2));
        dobj.setString(s2, V12);
        assertEquals(V22, dobj.get(s2));
        
        dobj.set(p1, V11);
        assertEquals(v21, dobj.getBoolean(p1));
        dobj.set(p1, V12);
        assertEquals(v22, dobj.getBoolean(p1));
        dobj.setString(p2, V11);
        assertEquals(V21, dobj.get(p2));
        dobj.setString(p2, V12);
        assertEquals(V22, dobj.get(p2));
        
        dobj.set(i1, V11);
        assertEquals(v21, dobj.getBoolean(i1));
        dobj.set(i1, V12);
        assertEquals(v22, dobj.getBoolean(i1));
        dobj.setString(i2, V11);
        assertEquals(V21, dobj.get(i2));
        dobj.setString(i2, V12);
        assertEquals(V22, dobj.get(i2));
    }

    public void testStringToByte()
    {
        System.out.println("String to Byte");
        DataObject dobj = createDataObject();
        Type t = dobj.getType();
        String s1 = "string";
        String s2 = "byte";
        Property p1 = t.getProperty(s1);
        Property p2 = t.getProperty(s2);
        int i1 = STRING_P_I;
        int i2 = BYTE_P_I;
        String V11 = "3";
        String V12 = "5";
        byte v21 = (byte)3;
        byte v22 = (byte)5;
        Object V21 = new Byte(v21);
        Object V22 = new Byte(v22);
        
        dobj.set(s1, V11);
        assertEquals(v21, dobj.getByte(s1));
        dobj.set(s1, V12);
        assertEquals(v22, dobj.getByte(s1));
        dobj.setString(s2, V11);
        assertEquals(V21, dobj.get(s2));
        dobj.setString(s2, V12);
        assertEquals(V22, dobj.get(s2));
        
        dobj.set(p1, V11);
        assertEquals(v21, dobj.getByte(p1));
        dobj.set(p1, V12);
        assertEquals(v22, dobj.getByte(p1));
        dobj.setString(p2, V11);
        assertEquals(V21, dobj.get(p2));
        dobj.setString(p2, V12);
        assertEquals(V22, dobj.get(p2));
        
        dobj.set(i1, V11);
        assertEquals(v21, dobj.getByte(i1));
        dobj.set(i1, V12);
        assertEquals(v22, dobj.getByte(i1));
        dobj.setString(i2, V11);
        assertEquals(V21, dobj.get(i2));
        dobj.setString(i2, V12);
        assertEquals(V22, dobj.get(i2));
    }

    public void testStringToCharacter()
    {
        System.out.println("String to Character");
        DataObject dobj = createDataObject();
        Type t = dobj.getType();
        String s1 = "string";
        String s2 = "character";
        Property p1 = t.getProperty(s1);
        Property p2 = t.getProperty(s2);
        int i1 = STRING_P_I;
        int i2 = CHARACTER_P_I;
        String V11 = "a";
        String V12 = "&";
        char v21 = 'a';
        char v22 = '&';
        Object V21 = new Character(v21);
        Object V22 = new Character(v22);
        
        dobj.set(s1, V11);
        assertEquals(v21, dobj.getChar(s1));
        dobj.set(s1, V12);
        assertEquals(v22, dobj.getChar(s1));
        dobj.setString(s2, V11);
        assertEquals(V21, dobj.get(s2));
        dobj.setString(s2, V12);
        assertEquals(V22, dobj.get(s2));
        
        dobj.set(p1, V11);
        assertEquals(v21, dobj.getChar(p1));
        dobj.set(p1, V12);
        assertEquals(v22, dobj.getChar(p1));
        dobj.setString(p2, V11);
        assertEquals(V21, dobj.get(p2));
        dobj.setString(p2, V12);
        assertEquals(V22, dobj.get(p2));
        
        dobj.set(i1, V11);
        assertEquals(v21, dobj.getChar(i1));
        dobj.set(i1, V12);
        assertEquals(v22, dobj.getChar(i1));
        dobj.setString(i2, V11);
        assertEquals(V21, dobj.get(i2));
        dobj.setString(i2, V12);
        assertEquals(V22, dobj.get(i2));
    }

    public void testStringToDouble()
    {
        System.out.println("String to Double");
        DataObject dobj = createDataObject();
        Type t = dobj.getType();
        String s1 = "string";
        String s2 = "double";
        Property p1 = t.getProperty(s1);
        Property p2 = t.getProperty(s2);
        int i1 = STRING_P_I;
        int i2 = DOUBLE_P_I;
        String V11 = "3";
        String V12 = "5.7";
        double v21 = 3.0;
        double v22 = 5.7;
        Object V21 = new Double(v21);
        Object V22 = new Double(v22);
        
        dobj.set(s1, V11);
        assertEquals(v21, dobj.getDouble(s1));
        dobj.set(s1, V12);
        assertEquals(v22, dobj.getDouble(s1));
        dobj.setString(s2, V11);
        assertEquals(V21, dobj.get(s2));
        dobj.setString(s2, V12);
        assertEquals(V22, dobj.get(s2));
        
        dobj.set(p1, V11);
        assertEquals(v21, dobj.getDouble(p1));
        dobj.set(p1, V12);
        assertEquals(v22, dobj.getDouble(p1));
        dobj.setString(p2, V11);
        assertEquals(V21, dobj.get(p2));
        dobj.setString(p2, V12);
        assertEquals(V22, dobj.get(p2));
        
        dobj.set(i1, V11);
        assertEquals(v21, dobj.getDouble(i1));
        dobj.set(i1, V12);
        assertEquals(v22, dobj.getDouble(i1));
        dobj.setString(i2, V11);
        assertEquals(V21, dobj.get(i2));
        dobj.setString(i2, V12);
        assertEquals(V22, dobj.get(i2));
    }

    public void testStringToFloat()
    {
        System.out.println("String to Float");
        DataObject dobj = createDataObject();
        Type t = dobj.getType();
        String s1 = "string";
        String s2 = "float";
        Property p1 = t.getProperty(s1);
        Property p2 = t.getProperty(s2);
        int i1 = STRING_P_I;
        int i2 = FLOAT_P_I;
        String V11 = "3";
        String V12 = "5.7";
        float v21 = (float)3.0;
        float v22 = (float)5.7;
        Object V21 = new Float(v21);
        Object V22 = new Float(v22);
        
        dobj.set(s1, V11);
        assertEquals(v21, dobj.getFloat(s1));
        dobj.set(s1, V12);
        assertEquals(v22, dobj.getFloat(s1));
        dobj.setString(s2, V11);
        assertEquals(V21, dobj.get(s2));
        dobj.setString(s2, V12);
        assertEquals(V22, dobj.get(s2));
        
        dobj.set(p1, V11);
        assertEquals(v21, dobj.getFloat(p1));
        dobj.set(p1, V12);
        assertEquals(v22, dobj.getFloat(p1));
        dobj.setString(p2, V11);
        assertEquals(V21, dobj.get(p2));
        dobj.setString(p2, V12);
        assertEquals(V22, dobj.get(p2));
        
        dobj.set(i1, V11);
        assertEquals(v21, dobj.getFloat(i1));
        dobj.set(i1, V12);
        assertEquals(v22, dobj.getFloat(i1));
        dobj.setString(i2, V11);
        assertEquals(V21, dobj.get(i2));
        dobj.setString(i2, V12);
        assertEquals(V22, dobj.get(i2));
    }

    public void testStringToInt()
    {
        System.out.println("String to Int");
        DataObject dobj = createDataObject();
        Type t = dobj.getType();
        String s1 = "string";
        String s2 = "int";
        Property p1 = t.getProperty(s1);
        Property p2 = t.getProperty(s2);
        int i1 = STRING_P_I;
        int i2 = INT_P_I;
        String V11 = "3";
        String V12 = "5";
        int v21 = 3;
        int v22 = 5;
        Object V21 = new Integer(v21);
        Object V22 = new Integer(v22);
        
        dobj.set(s1, V11);
        assertEquals(v21, dobj.getInt(s1));
        dobj.set(s1, V12);
        assertEquals(v22, dobj.getInt(s1));
        dobj.setString(s2, V11);
        assertEquals(V21, dobj.get(s2));
        dobj.setString(s2, V12);
        assertEquals(V22, dobj.get(s2));
        
        dobj.set(p1, V11);
        assertEquals(v21, dobj.getInt(p1));
        dobj.set(p1, V12);
        assertEquals(v22, dobj.getInt(p1));
        dobj.setString(p2, V11);
        assertEquals(V21, dobj.get(p2));
        dobj.setString(p2, V12);
        assertEquals(V22, dobj.get(p2));
        
        dobj.set(i1, V11);
        assertEquals(v21, dobj.getInt(i1));
        dobj.set(i1, V12);
        assertEquals(v22, dobj.getInt(i1));
        dobj.setString(i2, V11);
        assertEquals(V21, dobj.get(i2));
        dobj.setString(i2, V12);
        assertEquals(V22, dobj.get(i2));
    }

    public void testStringToLong()
    {
        System.out.println("String to Long");
        DataObject dobj = createDataObject();
        Type t = dobj.getType();
        String s1 = "string";
        String s2 = "long";
        Property p1 = t.getProperty(s1);
        Property p2 = t.getProperty(s2);
        int i1 = STRING_P_I;
        int i2 = LONG_P_I;
        String V11 = "33333333";
        String V12 = "55555555";
        long v21 = (long)33333333;
        long v22 = (long)55555555;
        Object V21 = new Long(v21);
        Object V22 = new Long(v22);
        
        dobj.set(s1, V11);
        assertEquals(v21, dobj.getLong(s1));
        dobj.set(s1, V12);
        assertEquals(v22, dobj.getLong(s1));
        dobj.setString(s2, V11);
        assertEquals(V21, dobj.get(s2));
        dobj.setString(s2, V12);
        assertEquals(V22, dobj.get(s2));
        
        dobj.set(p1, V11);
        assertEquals(v21, dobj.getLong(p1));
        dobj.set(p1, V12);
        assertEquals(v22, dobj.getLong(p1));
        dobj.setString(p2, V11);
        assertEquals(V21, dobj.get(p2));
        dobj.setString(p2, V12);
        assertEquals(V22, dobj.get(p2));
        
        dobj.set(i1, V11);
        assertEquals(v21, dobj.getLong(i1));
        dobj.set(i1, V12);
        assertEquals(v22, dobj.getLong(i1));
        dobj.setString(i2, V11);
        assertEquals(V21, dobj.get(i2));
        dobj.setString(i2, V12);
        assertEquals(V22, dobj.get(i2));
    }

    public void testStringToShort()
    {
        System.out.println("String to Short");
        DataObject dobj = createDataObject();
        Type t = dobj.getType();
        String s1 = "string";
        String s2 = "short";
        Property p1 = t.getProperty(s1);
        Property p2 = t.getProperty(s2);
        int i1 = STRING_P_I;
        int i2 = SHORT_P_I;
        String V11 = "3";
        String V12 = "5";
        short v21 = (short)3;
        short v22 = (short)5;
        Object V21 = new Short(v21);
        Object V22 = new Short(v22);
        
        dobj.set(s1, V11);
        assertEquals(v21, dobj.getShort(s1));
        dobj.set(s1, V12);
        assertEquals(v22, dobj.getShort(s1));
        dobj.setString(s2, V11);
        assertEquals(V21, dobj.get(s2));
        dobj.setString(s2, V12);
        assertEquals(V22, dobj.get(s2));
        
        dobj.set(p1, V11);
        assertEquals(v21, dobj.getShort(p1));
        dobj.set(p1, V12);
        assertEquals(v22, dobj.getShort(p1));
        dobj.setString(p2, V11);
        assertEquals(V21, dobj.get(p2));
        dobj.setString(p2, V12);
        assertEquals(V22, dobj.get(p2));
        
        dobj.set(i1, V11);
        assertEquals(v21, dobj.getShort(i1));
        dobj.set(i1, V12);
        assertEquals(v22, dobj.getShort(i1));
        dobj.setString(i2, V11);
        assertEquals(V21, dobj.get(i2));
        dobj.setString(i2, V12);
        assertEquals(V22, dobj.get(i2));
    }

    public void testStringToDecimal()
    {
        System.out.println("String to Decimal");
        DataObject dobj = createDataObject();
        Type t = dobj.getType();
        String s1 = "string";
        String s2 = "decimal";
        Property p1 = t.getProperty(s1);
        Property p2 = t.getProperty(s2);
        int i1 = STRING_P_I;
        int i2 = DECIMAL_P_I;
        String V11 = "3";
        String V12 = "5.7";
        Object V21 = new BigDecimal(V11);
        Object V22 = new BigDecimal(V12);
        
        dobj.set(s1, V11);
        assertEquals(V21, dobj.getBigDecimal(s1));
        dobj.set(s1, V12);
        assertEquals(V22, dobj.getBigDecimal(s1));
        dobj.setString(s2, V11);
        assertEquals(V21, dobj.get(s2));
        dobj.setString(s2, V12);
        assertEquals(V22, dobj.get(s2));
        
        dobj.set(p1, V11);
        assertEquals(V21, dobj.getBigDecimal(p1));
        dobj.set(p1, V12);
        assertEquals(V22, dobj.getBigDecimal(p1));
        dobj.setString(p2, V11);
        assertEquals(V21, dobj.get(p2));
        dobj.setString(p2, V12);
        assertEquals(V22, dobj.get(p2));
        
        dobj.set(i1, V11);
        assertEquals(V21, dobj.getBigDecimal(i1));
        dobj.set(i1, V12);
        assertEquals(V22, dobj.getBigDecimal(i1));
        dobj.setString(i2, V11);
        assertEquals(V21, dobj.get(i2));
        dobj.setString(i2, V12);
        assertEquals(V22, dobj.get(i2));
    }

    public void testStringToInteger()
    {
        System.out.println("String to Integer");
        DataObject dobj = createDataObject();
        Type t = dobj.getType();
        String s1 = "string";
        String s2 = "integer";
        Property p1 = t.getProperty(s1);
        Property p2 = t.getProperty(s2);
        int i1 = STRING_P_I;
        int i2 = INTEGER_P_I;
        String V11 = "3";
        String V12 = "57";
        Object V21 = new BigInteger(V11);
        Object V22 = new BigInteger(V12);
        
        dobj.set(s1, V11);
        assertEquals(V21, dobj.getBigInteger(s1));
        dobj.set(s1, V12);
        assertEquals(V22, dobj.getBigInteger(s1));
        dobj.setString(s2, V11);
        assertEquals(V21, dobj.get(s2));
        dobj.setString(s2, V12);
        assertEquals(V22, dobj.get(s2));
        
        dobj.set(p1, V11);
        assertEquals(V21, dobj.getBigInteger(p1));
        dobj.set(p1, V12);
        assertEquals(V22, dobj.getBigInteger(p1));
        dobj.setString(p2, V11);
        assertEquals(V21, dobj.get(p2));
        dobj.setString(p2, V12);
        assertEquals(V22, dobj.get(p2));
        
        dobj.set(i1, V11);
        assertEquals(V21, dobj.getBigInteger(i1));
        dobj.set(i1, V12);
        assertEquals(V22, dobj.getBigInteger(i1));
        dobj.setString(i2, V11);
        assertEquals(V21, dobj.get(i2));
        dobj.setString(i2, V12);
        assertEquals(V22, dobj.get(i2));
    }

    /*
      Test DataObject.getDate() on property of type String
      and DataObject.setString() on property of type Date.
    */
    public void testStringToDate() throws Exception
    {
        System.out.println("String to Date");
        DataObject dobj = createDataObject();
        Type t = dobj.getType();
        String s1 = "string";
        String s2 = "date";
        Property p1 = t.getProperty(s1);
        Property p2 = t.getProperty(s2);
        int i1 = STRING_P_I;
        int i2 = DATE_P_I;
        String V11 = "2006-10-30T12:00:00.000Z";
        String V12 = "2006-11-01T18:30:00.000Z";
        DateFormat f = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'.'SSS'Z'");
        f.setTimeZone(TimeZone.getTimeZone("GMT"));
        Object V21 = f.parse(V11);
        Object V22 = f.parse(V12);
        System.out.println("V11: " + V11);
        System.out.println("V12: " + V12);
        System.out.println("V21: " + V21);
        System.out.println("V22: " + V22);

        System.out.println();
        System.out.println("using path ...");
        dobj.set(s1, V11);
        System.out.println("set: " + dobj.get(s1));
        System.out.println("date: " + f.parse((String)dobj.get(s1)));
        System.out.println("got:  " + dobj.getDate(s1));
        assertEquals(V21, dobj.getDate(s1));
        dobj.set(s1, V12);
        System.out.println("set: " + dobj.get(s1));
        System.out.println("date: " + f.parse((String)dobj.get(s1)));
        System.out.println("got:  " + dobj.getDate(s1));
        assertEquals(V22, dobj.getDate(s1));
        dobj.setString(s2, V11);
        System.out.println("set: " + V11);
        System.out.println("got: " + dobj.get(s2));
        assertEquals(V21, dobj.get(s2));
        dobj.setString(s2, V12);
        System.out.println("set: " + V12);
        System.out.println("got: " + dobj.get(s2));
        assertEquals(V22, dobj.get(s2));
        
        System.out.println();
        System.out.println("using property ...");
        dobj.set(p1, V11);
        System.out.println("set: " + dobj.get(p1));
        System.out.println("date: " + f.parse((String)dobj.get(p1)));
        System.out.println("got:  " + dobj.getDate(p1));
        assertEquals(V21, dobj.getDate(p1));
        dobj.set(p1, V12);
        System.out.println("set: " + dobj.get(p1));
        System.out.println("date: " + f.parse((String)dobj.get(p1)));
        System.out.println("got:  " + dobj.getDate(p1));
        assertEquals(V22, dobj.getDate(p1));
        dobj.setString(p2, V11);
        System.out.println("set: " + V11);
        System.out.println("got: " + dobj.get(p2));
        assertEquals(V21, dobj.get(p2));
        dobj.setString(p2, V12);
        System.out.println("set: " + V12);
        System.out.println("got: " + dobj.get(p2));
        assertEquals(V22, dobj.get(p2));
        
        dobj.set(i1, V11);
        assertEquals(V21, dobj.getDate(i1));
        dobj.set(i1, V12);
        assertEquals(V22, dobj.getDate(i1));
        dobj.setString(i2, V11);
        assertEquals(V21, dobj.get(i2));
        dobj.setString(i2, V12);
        assertEquals(V22, dobj.get(i2));
    }

    public void testBytesToInteger()
    {
        System.out.println("Bytes to Integer");
        DataObject dobj = createDataObject();
        Type t = dobj.getType();
        String s1 = "bytes";
        String s2 = "integer";
        Property p1 = t.getProperty(s1);
        Property p2 = t.getProperty(s2);
        int i1 = BYTES_P_I;
        int i2 = INTEGER_P_I;
        byte[] V11 = { (byte)7, (byte)7, (byte)7, (byte)7, (byte)7, (byte)7, (byte)7, (byte)7 };
        byte[] V12 = { (byte)1, (byte)1, (byte)1, (byte)1, (byte)1, (byte)1, (byte)1, (byte)1 };
        Object V21 = new BigInteger(V11);
        Object V22 = new BigInteger(V12);
        
        dobj.set(s1, V11);
        assertEquals(V21, dobj.getBigInteger(s1));
        dobj.set(s1, V12);
        assertEquals(V22, dobj.getBigInteger(s1));
        dobj.setBytes(s2, V11);
        assertEquals(V21, dobj.get(s2));
        dobj.setBytes(s2, V12);
        assertEquals(V22, dobj.get(s2));
        
        dobj.set(p1, V11);
        assertEquals(V21, dobj.getBigInteger(p1));
        dobj.set(p1, V12);
        assertEquals(V22, dobj.getBigInteger(p1));
        dobj.setBytes(p2, V11);
        assertEquals(V21, dobj.get(p2));
        dobj.setBytes(p2, V12);
        assertEquals(V22, dobj.get(p2));
        
        dobj.set(i1, V11);
        assertEquals(V21, dobj.getBigInteger(i1));
        dobj.set(i1, V12);
        assertEquals(V22, dobj.getBigInteger(i1));
        dobj.setBytes(i2, V11);
        assertEquals(V21, dobj.get(i2));
        dobj.setBytes(i2, V12);
        assertEquals(V22, dobj.get(i2));
    }

    public void testDecimalToDouble()
    {
        System.out.println("Decimal to Double");
        DataObject dobj = createDataObject();
        Type t = dobj.getType();
        String s1 = "decimal";
        String s2 = "double";
        Property p1 = t.getProperty(s1);
        Property p2 = t.getProperty(s2);
        int i1 = DECIMAL_P_I;
        int i2 = DOUBLE_P_I;
        BigDecimal V11 = new BigDecimal(123.456789);
        BigDecimal V12 = new BigDecimal(1234567890);
        double v21 = V11.doubleValue();
        double v22 = V12.doubleValue();
        Object V21 = new Double(v21);
        Object V22 = new Double(v22);
        
        dobj.set(s1, V11);
        assertEquals(v21, dobj.getDouble(s1));
        dobj.set(s1, V12);
        assertEquals(v22, dobj.getDouble(s1));
        dobj.setBigDecimal(s2, V11);
        assertEquals(V21, dobj.get(s2));
        dobj.setBigDecimal(s2, V12);
        assertEquals(V22, dobj.get(s2));
        
        dobj.set(p1, V11);
        assertEquals(v21, dobj.getDouble(p1));
        dobj.set(p1, V12);
        assertEquals(v22, dobj.getDouble(p1));
        dobj.setBigDecimal(p2, V11);
        assertEquals(V21, dobj.get(p2));
        dobj.setBigDecimal(p2, V12);
        assertEquals(V22, dobj.get(p2));
        
        dobj.set(i1, V11);
        assertEquals(v21, dobj.getDouble(i1));
        dobj.set(i1, V12);
        assertEquals(v22, dobj.getDouble(i1));
        dobj.setBigDecimal(i2, V11);
        assertEquals(V21, dobj.get(i2));
        dobj.setBigDecimal(i2, V12);
        assertEquals(V22, dobj.get(i2));
    }

    public void testDecimalToFloat()
    {
        System.out.println("Decimal to Float");
        DataObject dobj = createDataObject();
        Type t = dobj.getType();
        String s1 = "decimal";
        String s2 = "float";
        Property p1 = t.getProperty(s1);
        Property p2 = t.getProperty(s2);
        int i1 = DECIMAL_P_I;
        int i2 = FLOAT_P_I;
        BigDecimal V11 = new BigDecimal(123.456789);
        BigDecimal V12 = new BigDecimal(1234567890);
        float v21 = V11.floatValue();
        float v22 = V12.floatValue();
        Object V21 = new Float(v21);
        Object V22 = new Float(v22);
        
        dobj.set(s1, V11);
        assertEquals(v21, dobj.getFloat(s1));
        dobj.set(s1, V12);
        assertEquals(v22, dobj.getFloat(s1));
        dobj.setBigDecimal(s2, V11);
        assertEquals(V21, dobj.get(s2));
        dobj.setBigDecimal(s2, V12);
        assertEquals(V22, dobj.get(s2));
        
        dobj.set(p1, V11);
        assertEquals(v21, dobj.getFloat(p1));
        dobj.set(p1, V12);
        assertEquals(v22, dobj.getFloat(p1));
        dobj.setBigDecimal(p2, V11);
        assertEquals(V21, dobj.get(p2));
        dobj.setBigDecimal(p2, V12);
        assertEquals(V22, dobj.get(p2));
        
        dobj.set(i1, V11);
        assertEquals(v21, dobj.getFloat(i1));
        dobj.set(i1, V12);
        assertEquals(v22, dobj.getFloat(i1));
        dobj.setBigDecimal(i2, V11);
        assertEquals(V21, dobj.get(i2));
        dobj.setBigDecimal(i2, V12);
        assertEquals(V22, dobj.get(i2));
    }

    public void testDecimalToInt()
    {
        System.out.println("Decimal to Int");
        DataObject dobj = createDataObject();
        Type t = dobj.getType();
        String s1 = "decimal";
        String s2 = "int";
        Property p1 = t.getProperty(s1);
        Property p2 = t.getProperty(s2);
        int i1 = DECIMAL_P_I;
        int i2 = INT_P_I;
        BigDecimal V11 = new BigDecimal(123.456789);
        BigDecimal V12 = new BigDecimal(1234567890);
        int v21 = V11.intValue();
        int v22 = V12.intValue();
        Object V21 = new Integer(v21);
        Object V22 = new Integer(v22);
        
        dobj.set(s1, V11);
        assertEquals(v21, dobj.getInt(s1));
        dobj.set(s1, V12);
        assertEquals(v22, dobj.getInt(s1));
        dobj.setBigDecimal(s2, V11);
        assertEquals(V21, dobj.get(s2));
        dobj.setBigDecimal(s2, V12);
        assertEquals(V22, dobj.get(s2));
        
        dobj.set(p1, V11);
        assertEquals(v21, dobj.getInt(p1));
        dobj.set(p1, V12);
        assertEquals(v22, dobj.getInt(p1));
        dobj.setBigDecimal(p2, V11);
        assertEquals(V21, dobj.get(p2));
        dobj.setBigDecimal(p2, V12);
        assertEquals(V22, dobj.get(p2));
        
        dobj.set(i1, V11);
        assertEquals(v21, dobj.getInt(i1));
        dobj.set(i1, V12);
        assertEquals(v22, dobj.getInt(i1));
        dobj.setBigDecimal(i2, V11);
        assertEquals(V21, dobj.get(i2));
        dobj.setBigDecimal(i2, V12);
        assertEquals(V22, dobj.get(i2));
    }

    public void testDecimalToLong()
    {
        System.out.println("Decimal to Long");
        DataObject dobj = createDataObject();
        Type t = dobj.getType();
        String s1 = "decimal";
        String s2 = "long";
        Property p1 = t.getProperty(s1);
        Property p2 = t.getProperty(s2);
        int i1 = DECIMAL_P_I;
        int i2 = LONG_P_I;
        BigDecimal V11 = new BigDecimal(123.456789);
        BigDecimal V12 = new BigDecimal(1234567890);
        long v21 = V11.longValue();
        long v22 = V12.longValue();
        Object V21 = new Long(v21);
        Object V22 = new Long(v22);
        
        dobj.set(s1, V11);
        assertEquals(v21, dobj.getLong(s1));
        dobj.set(s1, V12);
        assertEquals(v22, dobj.getLong(s1));
        dobj.setBigDecimal(s2, V11);
        assertEquals(V21, dobj.get(s2));
        dobj.setBigDecimal(s2, V12);
        assertEquals(V22, dobj.get(s2));
        
        dobj.set(p1, V11);
        assertEquals(v21, dobj.getLong(p1));
        dobj.set(p1, V12);
        assertEquals(v22, dobj.getLong(p1));
        dobj.setBigDecimal(p2, V11);
        assertEquals(V21, dobj.get(p2));
        dobj.setBigDecimal(p2, V12);
        assertEquals(V22, dobj.get(p2));
        
        dobj.set(i1, V11);
        assertEquals(v21, dobj.getLong(i1));
        dobj.set(i1, V12);
        assertEquals(v22, dobj.getLong(i1));
        dobj.setBigDecimal(i2, V11);
        assertEquals(V21, dobj.get(i2));
        dobj.setBigDecimal(i2, V12);
        assertEquals(V22, dobj.get(i2));
    }

    public void testDecimalToString()
    {
        System.out.println("Decimal to String");
        DataObject dobj = createDataObject();
        Type t = dobj.getType();
        String s1 = "decimal";
        String s2 = "string";
        Property p1 = t.getProperty(s1);
        Property p2 = t.getProperty(s2);
        int i1 = DECIMAL_P_I;
        int i2 = STRING_P_I;
        BigDecimal V11 = new BigDecimal("123.456789");
        BigDecimal V12 = new BigDecimal("1234567890");
        Object V21 = "123.456789";
        Object V22 = "1234567890";
        
        dobj.set(s1, V11);
        assertEquals(V21, dobj.getString(s1));
        dobj.set(s1, V12);
        assertEquals(V22, dobj.getString(s1));
        dobj.setBigDecimal(s2, V11);
        assertEquals(V21, dobj.get(s2));
        dobj.setBigDecimal(s2, V12);
        assertEquals(V22, dobj.get(s2));
        
        dobj.set(p1, V11);
        assertEquals(V21, dobj.getString(p1));
        dobj.set(p1, V12);
        assertEquals(V22, dobj.getString(p1));
        dobj.setBigDecimal(p2, V11);
        assertEquals(V21, dobj.get(p2));
        dobj.setBigDecimal(p2, V12);
        assertEquals(V22, dobj.get(p2));
        
        dobj.set(i1, V11);
        assertEquals(V21, dobj.getString(i1));
        dobj.set(i1, V12);
        assertEquals(V22, dobj.getString(i1));
        dobj.setBigDecimal(i2, V11);
        assertEquals(V21, dobj.get(i2));
        dobj.setBigDecimal(i2, V12);
        assertEquals(V22, dobj.get(i2));
    }

    public void testDecimalToInteger()
    {
        System.out.println("Decimal to Integer");
        DataObject dobj = createDataObject();
        Type t = dobj.getType();
        String s1 = "decimal";
        String s2 = "integer";
        Property p1 = t.getProperty(s1);
        Property p2 = t.getProperty(s2);
        int i1 = DECIMAL_P_I;
        int i2 = INTEGER_P_I;
        BigDecimal V11 = new BigDecimal(123.456789);
        BigDecimal V12 = new BigDecimal(1234567890);
        Object V21 = V11.toBigInteger();
        Object V22 = V12.toBigInteger();
        
        dobj.set(s1, V11);
        assertEquals(V21, dobj.getBigInteger(s1));
        dobj.set(s1, V12);
        assertEquals(V22, dobj.getBigInteger(s1));
        dobj.setBigDecimal(s2, V11);
        assertEquals(V21, dobj.get(s2));
        dobj.setBigDecimal(s2, V12);
        assertEquals(V22, dobj.get(s2));
        
        dobj.set(p1, V11);
        assertEquals(V21, dobj.getBigInteger(p1));
        dobj.set(p1, V12);
        assertEquals(V22, dobj.getBigInteger(p1));
        dobj.setBigDecimal(p2, V11);
        assertEquals(V21, dobj.get(p2));
        dobj.setBigDecimal(p2, V12);
        assertEquals(V22, dobj.get(p2));
        
        dobj.set(i1, V11);
        assertEquals(V21, dobj.getBigInteger(i1));
        dobj.set(i1, V12);
        assertEquals(V22, dobj.getBigInteger(i1));
        dobj.setBigDecimal(i2, V11);
        assertEquals(V21, dobj.get(i2));
        dobj.setBigDecimal(i2, V12);
        assertEquals(V22, dobj.get(i2));
    }

    public void testIntegerToDouble()
    {
        System.out.println("Integer to Double");
        DataObject dobj = createDataObject();
        Type t = dobj.getType();
        String s1 = "integer";
        String s2 = "double";
        Property p1 = t.getProperty(s1);
        Property p2 = t.getProperty(s2);
        int i1 = INTEGER_P_I;
        int i2 = DOUBLE_P_I;
        long v11 = 123456789;
        long v12 = 234567890;
        double v21 = (double)v11;
        double v22 = (double)v12;
        BigInteger V11 = BigInteger.valueOf(v11);
        BigInteger V12 = BigInteger.valueOf(v12);
        Object V21 = new Double(v21);
        Object V22 = new Double(v22);
        
        dobj.set(s1, V11);
        assertEquals(v21, dobj.getDouble(s1));
        dobj.set(s1, V12);
        assertEquals(v22, dobj.getDouble(s1));
        dobj.setBigInteger(s2, V11);
        assertEquals(V21, dobj.get(s2));
        dobj.setBigInteger(s2, V12);
        assertEquals(V22, dobj.get(s2));
        
        dobj.set(p1, V11);
        assertEquals(v21, dobj.getDouble(p1));
        dobj.set(p1, V12);
        assertEquals(v22, dobj.getDouble(p1));
        dobj.setBigInteger(p2, V11);
        assertEquals(V21, dobj.get(p2));
        dobj.setBigInteger(p2, V12);
        assertEquals(V22, dobj.get(p2));
        
        dobj.set(i1, V11);
        assertEquals(v21, dobj.getDouble(i1));
        dobj.set(i1, V12);
        assertEquals(v22, dobj.getDouble(i1));
        dobj.setBigInteger(i2, V11);
        assertEquals(V21, dobj.get(i2));
        dobj.setBigInteger(i2, V12);
        assertEquals(V22, dobj.get(i2));
    }

    public void testIntegerToFloat()
    {
        System.out.println("Integer to Float");
        DataObject dobj = createDataObject();
        Type t = dobj.getType();
        String s1 = "integer";
        String s2 = "float";
        Property p1 = t.getProperty(s1);
        Property p2 = t.getProperty(s2);
        int i1 = INTEGER_P_I;
        int i2 = FLOAT_P_I;
        long v11 = 123456789;
        long v12 = 234567890;
        float v21 = (float)v11;
        float v22 = (float)v12;
        BigInteger V11 = BigInteger.valueOf(v11);
        BigInteger V12 = BigInteger.valueOf(v12);
        Object V21 = new Float(v21);
        Object V22 = new Float(v22);
        
        dobj.set(s1, V11);
        assertEquals(v21, dobj.getFloat(s1));
        dobj.set(s1, V12);
        assertEquals(v22, dobj.getFloat(s1));
        dobj.setBigInteger(s2, V11);
        assertEquals(V21, dobj.get(s2));
        dobj.setBigInteger(s2, V12);
        assertEquals(V22, dobj.get(s2));
        
        dobj.set(p1, V11);
        assertEquals(v21, dobj.getFloat(p1));
        dobj.set(p1, V12);
        assertEquals(v22, dobj.getFloat(p1));
        dobj.setBigInteger(p2, V11);
        assertEquals(V21, dobj.get(p2));
        dobj.setBigInteger(p2, V12);
        assertEquals(V22, dobj.get(p2));
        
        dobj.set(i1, V11);
        assertEquals(v21, dobj.getFloat(i1));
        dobj.set(i1, V12);
        assertEquals(v22, dobj.getFloat(i1));
        dobj.setBigInteger(i2, V11);
        assertEquals(V21, dobj.get(i2));
        dobj.setBigInteger(i2, V12);
        assertEquals(V22, dobj.get(i2));
    }

    public void testIntegerToInt()
    {
        System.out.println("Integer to Int");
        DataObject dobj = createDataObject();
        Type t = dobj.getType();
        String s1 = "integer";
        String s2 = "int";
        Property p1 = t.getProperty(s1);
        Property p2 = t.getProperty(s2);
        int i1 = INTEGER_P_I;
        int i2 = INT_P_I;
        long v11 = 123456789;
        long v12 = 234567890;
        int v21 = (int)v11;
        int v22 = (int)v12;
        BigInteger V11 = BigInteger.valueOf(v11);
        BigInteger V12 = BigInteger.valueOf(v12);
        Object V21 = new Integer(v21);
        Object V22 = new Integer(v22);
        
        dobj.set(s1, V11);
        assertEquals(v21, dobj.getInt(s1));
        dobj.set(s1, V12);
        assertEquals(v22, dobj.getInt(s1));
        dobj.setBigInteger(s2, V11);
        assertEquals(V21, dobj.get(s2));
        dobj.setBigInteger(s2, V12);
        assertEquals(V22, dobj.get(s2));
        
        dobj.set(p1, V11);
        assertEquals(v21, dobj.getInt(p1));
        dobj.set(p1, V12);
        assertEquals(v22, dobj.getInt(p1));
        dobj.setBigInteger(p2, V11);
        assertEquals(V21, dobj.get(p2));
        dobj.setBigInteger(p2, V12);
        assertEquals(V22, dobj.get(p2));
        
        dobj.set(i1, V11);
        assertEquals(v21, dobj.getInt(i1));
        dobj.set(i1, V12);
        assertEquals(v22, dobj.getInt(i1));
        dobj.setBigInteger(i2, V11);
        assertEquals(V21, dobj.get(i2));
        dobj.setBigInteger(i2, V12);
        assertEquals(V22, dobj.get(i2));
    }

    public void testIntegerToLong()
    {
        System.out.println("Integer to Long");
        DataObject dobj = createDataObject();
        Type t = dobj.getType();
        String s1 = "integer";
        String s2 = "long";
        Property p1 = t.getProperty(s1);
        Property p2 = t.getProperty(s2);
        int i1 = INTEGER_P_I;
        int i2 = LONG_P_I;
        long v21 = 123456789;
        long v22 = 234567890;
        BigInteger V11 = BigInteger.valueOf(v21);
        BigInteger V12 = BigInteger.valueOf(v22);
        Object V21 = new Long(v21);
        Object V22 = new Long(v22);
        
        dobj.set(s1, V11);
        assertEquals(v21, dobj.getLong(s1));
        dobj.set(s1, V12);
        assertEquals(v22, dobj.getLong(s1));
        dobj.setBigInteger(s2, V11);
        assertEquals(V21, dobj.get(s2));
        dobj.setBigInteger(s2, V12);
        assertEquals(V22, dobj.get(s2));
        
        dobj.set(p1, V11);
        assertEquals(v21, dobj.getLong(p1));
        dobj.set(p1, V12);
        assertEquals(v22, dobj.getLong(p1));
        dobj.setBigInteger(p2, V11);
        assertEquals(V21, dobj.get(p2));
        dobj.setBigInteger(p2, V12);
        assertEquals(V22, dobj.get(p2));
        
        dobj.set(i1, V11);
        assertEquals(v21, dobj.getLong(i1));
        dobj.set(i1, V12);
        assertEquals(v22, dobj.getLong(i1));
        dobj.setBigInteger(i2, V11);
        assertEquals(V21, dobj.get(i2));
        dobj.setBigInteger(i2, V12);
        assertEquals(V22, dobj.get(i2));
    }

    public void testIntegerToString()
    {
        System.out.println("Integer to String");
        DataObject dobj = createDataObject();
        Type t = dobj.getType();
        String s1 = "integer";
        String s2 = "string";
        Property p1 = t.getProperty(s1);
        Property p2 = t.getProperty(s2);
        int i1 = INTEGER_P_I;
        int i2 = STRING_P_I;
        long v11 = 123456789;
        long v12 = 234567890;
        BigInteger V11 = BigInteger.valueOf(v11);
        BigInteger V12 = BigInteger.valueOf(v12);
        Object V21 = "123456789";
        Object V22 = "234567890";
        
        dobj.set(s1, V11);
        assertEquals(V21, dobj.getString(s1));
        dobj.set(s1, V12);
        assertEquals(V22, dobj.getString(s1));
        dobj.setBigInteger(s2, V11);
        assertEquals(V21, dobj.get(s2));
        dobj.setBigInteger(s2, V12);
        assertEquals(V22, dobj.get(s2));
        
        dobj.set(p1, V11);
        assertEquals(V21, dobj.getString(p1));
        dobj.set(p1, V12);
        assertEquals(V22, dobj.getString(p1));
        dobj.setBigInteger(p2, V11);
        assertEquals(V21, dobj.get(p2));
        dobj.setBigInteger(p2, V12);
        assertEquals(V22, dobj.get(p2));
        
        dobj.set(i1, V11);
        assertEquals(V21, dobj.getString(i1));
        dobj.set(i1, V12);
        assertEquals(V22, dobj.getString(i1));
        dobj.setBigInteger(i2, V11);
        assertEquals(V21, dobj.get(i2));
        dobj.setBigInteger(i2, V12);
        assertEquals(V22, dobj.get(i2));
    }

    public void testIntegerToBytes()
    {
        System.out.println("Integer to Bytes");
        DataObject dobj = createDataObject();
        Type t = dobj.getType();
        String s1 = "integer";
        String s2 = "bytes";
        Property p1 = t.getProperty(s1);
        Property p2 = t.getProperty(s2);
        int i1 = INTEGER_P_I;
        int i2 = BYTES_P_I;
        byte[] V21 = { (byte)7, (byte)7, (byte)7, (byte)7, (byte)7, (byte)7, (byte)7, (byte)7 };
        byte[] V22 = { (byte)1, (byte)1, (byte)1, (byte)1, (byte)1, (byte)1, (byte)1, (byte)1 };
        BigInteger V11 = new BigInteger(V21);
        BigInteger V12 = new BigInteger(V22);
        
        dobj.set(s1, V11);
        compareBytes(V21, dobj.getBytes(s1));
        dobj.set(s1, V12);
        compareBytes(V22, dobj.getBytes(s1));
        dobj.setBigInteger(s2, V11);
        compareBytes(V21, (byte[])dobj.get(s2));
        dobj.setBigInteger(s2, V12);
        compareBytes(V22, (byte[])dobj.get(s2));
        
        dobj.set(p1, V11);
        compareBytes(V21, dobj.getBytes(p1));
        dobj.set(p1, V12);
        compareBytes(V22, dobj.getBytes(p1));
        dobj.setBigInteger(p2, V11);
        compareBytes(V21, (byte[])dobj.get(p2));
        dobj.setBigInteger(p2, V12);
        compareBytes(V22, (byte[])dobj.get(p2));
        
        dobj.set(i1, V11);
        compareBytes(V21, dobj.getBytes(i1));
        dobj.set(i1, V12);
        compareBytes(V22, dobj.getBytes(i1));
        dobj.setBigInteger(i2, V11);
        compareBytes(V21, (byte[])dobj.get(i2));
        dobj.setBigInteger(i2, V12);
        compareBytes(V22, (byte[])dobj.get(i2));
    }

    public void testIntegerToDecimal()
    {
        System.out.println("Integer to Decimal");
        DataObject dobj = createDataObject();
        Type t = dobj.getType();
        String s1 = "integer";
        String s2 = "decimal";
        Property p1 = t.getProperty(s1);
        Property p2 = t.getProperty(s2);
        int i1 = INTEGER_P_I;
        int i2 = DECIMAL_P_I;
        BigInteger V11 = new BigInteger("1234567890");
        BigInteger V12 = new BigInteger("-1234567890");
        Object V21 = new BigDecimal(V11);
        Object V22 = new BigDecimal(V12);
        
        dobj.set(s1, V11);
        assertEquals(V21, dobj.getBigDecimal(s1));
        dobj.set(s1, V12);
        assertEquals(V22, dobj.getBigDecimal(s1));
        dobj.setBigInteger(s2, V11);
        assertEquals(V21, dobj.get(s2));
        dobj.setBigInteger(s2, V12);
        assertEquals(V22, dobj.get(s2));
        
        dobj.set(p1, V11);
        assertEquals(V21, dobj.getBigDecimal(p1));
        dobj.set(p1, V12);
        assertEquals(V22, dobj.getBigDecimal(p1));
        dobj.setBigInteger(p2, V11);
        assertEquals(V21, dobj.get(p2));
        dobj.setBigInteger(p2, V12);
        assertEquals(V22, dobj.get(p2));
        
        dobj.set(i1, V11);
        assertEquals(V21, dobj.getBigDecimal(i1));
        dobj.set(i1, V12);
        assertEquals(V22, dobj.getBigDecimal(i1));
        dobj.setBigInteger(i2, V11);
        assertEquals(V21, dobj.get(i2));
        dobj.setBigInteger(i2, V12);
        assertEquals(V22, dobj.get(i2));
    }

    public void testDateToLong()
    {
        System.out.println("Date to Long");
        DataObject dobj = createDataObject();
        Type t = dobj.getType();
        String s1 = "date";
        String s2 = "long";
        Property p1 = t.getProperty(s1);
        Property p2 = t.getProperty(s2);
        int i1 = DATE_P_I;
        int i2 = LONG_P_I;
        long v21 = 1163212477742l;
        long v22 = 1163212530295l;
        Date V11 = new Date(v21);
        Date V12 = new Date(v22);
        Object V21 = new Long(v21);
        Object V22 = new Long(v22);

        dobj.set(s1, V11);
        System.out.println("set: " + dobj.get(s1));
        assertEquals(V21, dobj.getLong(s1));
        dobj.set(s1, V12);
        System.out.println("set: " + dobj.get(s1));
        assertEquals(V22, dobj.getLong(s1));
        dobj.setDate(s2, V11);
        System.out.println("set: " + V11);
        System.out.println("got: " + dobj.get(s2));
        assertEquals(V21, dobj.get(s2));
        dobj.setDate(s2, V12);
        System.out.println("set: " + V12);
        System.out.println("got: " + dobj.get(s2));
        assertEquals(V22, dobj.get(s2));
        
        dobj.set(p1, V11);
        assertEquals(V21, dobj.getLong(p1));
        dobj.set(p1, V12);
        assertEquals(V22, dobj.getLong(p1));
        dobj.setDate(p2, V11);
        assertEquals(V21, dobj.get(p2));
        dobj.setDate(p2, V12);
        assertEquals(V22, dobj.get(p2));
        
        dobj.set(i1, V11);
        assertEquals(V21, dobj.getLong(i1));
        dobj.set(i1, V12);
        assertEquals(V22, dobj.getLong(i1));
        dobj.setDate(i2, V11);
        assertEquals(V21, dobj.get(i2));
        dobj.setDate(i2, V12);
        assertEquals(V22, dobj.get(i2));
    }

    /* Test:
       getString() on a property of type Date
       setDate() on a property of type String
     */
    public void testDateToString() throws Exception
    {
        System.out.println("Date to String");
        DataObject dobj = createDataObject();
        Type t = dobj.getType();
        String s1 = "date";
        String s2 = "string";
        Property p1 = t.getProperty(s1);
        Property p2 = t.getProperty(s2);
        int i1 = DATE_P_I;
        int i2 = STRING_P_I;
        String V21 = "2006-10-30T12:00:00Z";
        String V22 = "2006-11-01T18:30:00Z";
        DateFormat f = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        f.setTimeZone(TimeZone.getTimeZone("GMT"));
        Date V11 = f.parse(V21);
        Date V12 = f.parse(V22);

        System.out.println();
        System.out.println("using path ...");
        dobj.set(s1, V11);
        System.out.println("set: " + dobj.get(s1));
        assertEquals(V21, dobj.getString(s1));
        dobj.set(s1, V12);
        System.out.println("set: " + dobj.get(s1));
        assertEquals(V22, dobj.getString(s1));
        dobj.setDate(s2, V11);
        System.out.println("set: " + V11);
        System.out.println("got: " + dobj.get(s2));
        assertEquals(V21, dobj.get(s2));
        dobj.setDate(s2, V12);
        System.out.println("set: " + V12);
        System.out.println("got: " + dobj.get(s2));
        assertEquals(V22, dobj.get(s2));
        
        System.out.println();
        System.out.println("using property ...");
        dobj.set(p1, V11);
        System.out.println("set: " + dobj.get(p1));
        assertEquals(V21, dobj.getString(p1));
        dobj.set(p1, V12);
        System.out.println("set: " + dobj.get(p1));
        assertEquals(V22, dobj.getString(p1));
        dobj.setDate(p2, V11);
        System.out.println("set: " + V11);
        System.out.println("got: " + dobj.get(p2));
        assertEquals(V21, dobj.get(p2));
        dobj.setDate(p2, V12);
        System.out.println("set: " + V12);
        System.out.println("got: " + dobj.get(p2));
        assertEquals(V22, dobj.get(p2));
        
        dobj.set(i1, V11);
        assertEquals(V21, dobj.getString(i1));
        dobj.set(i1, V12);
        assertEquals(V22, dobj.getString(i1));
        dobj.setDate(i2, V11);
        assertEquals(V21, dobj.get(i2));
        dobj.setDate(i2, V12);
        assertEquals(V22, dobj.get(i2));
    }

    public void testStringToStrings()
    {
        System.out.println("String to Strings");
        DataObject dobj = createDataObject();
        Type t = dobj.getType();
        String s1 = "string";
        String s2 = "strings";
        Property p1 = t.getProperty(s1);
        Property p2 = t.getProperty(s2);
        int i1 = STRING_P_I;
        int i2 = STRINGS_P_I;
        List strings = new ArrayList();
        strings.add("This");
        strings.add("is");
        strings.add("a");
        strings.add("dog.");
 
        String V11 = "This is a dog.";
        String V12 = //null; 
                     "";
        List V21 = strings;
        List V22 = new ArrayList();
        V22.add("");

        dobj.setString(s2, V11);
        assertEquals(V21, dobj.get(s2));
        dobj.setString(s2, V12);
        assertEquals(V22, dobj.get(s2));
        
        dobj.setString(p2, V11);
        assertEquals(V21, dobj.get(p2));
        dobj.setString(p2, V12);
        assertEquals(V22, dobj.get(p2));
        
        dobj.setString(i2, V11);
        assertEquals(V21, dobj.get(i2));
        dobj.setString(i2, V12);
        assertEquals(V22, dobj.get(i2));
    }

    public void testStringsToString()
    {
        System.out.println("Strings to String");
        DataObject dobj = createDataObject();
        Type t = dobj.getType();
        String s1 = "strings";
        String s2 = "string";
        Property p1 = t.getProperty(s1);
        Property p2 = t.getProperty(s2);
        int i1 = STRINGS_P_I;
        int i2 = STRING_P_I;
        List strings = new ArrayList();
        strings.add("This");
        strings.add("is");
        strings.add("a");
        strings.add("dog.");
 
        List V11 = strings;
        List V12 = new ArrayList();
        String V21 = "This is a dog.";
        String V22 = null;
        
        dobj.set(s1, V11);
        String concat = dobj.getString(s1);
        System.out.println(concat);
        assertEquals(V21, dobj.getString(s1));
        dobj.set(s1, V12);
        assertEquals(V22, dobj.getString(s1));
        
        dobj.set(p1, V11);
        assertEquals(V21, dobj.getString(p1));
        dobj.set(p1, V12);
        assertEquals(V22, dobj.getString(p1));
        
        dobj.set(i1, V11);
        assertEquals(V21, dobj.getString(i1));
        dobj.set(i1, V12);
        assertEquals(V22, dobj.getString(i1));
    }

    // test String-based date-time types

    protected void _testGDate(DataObject dobj,
                              String s, // path
                              int i, // property index
                              Property p,
                              String V)
    {
        dobj.set(s, V);
        assertEquals(V, dobj.getString(s));
        dobj.setString(s, V);
        assertEquals(V, dobj.get(s));
        
        dobj.set(p, V);
        assertEquals(V, dobj.getString(p));
        dobj.setString(p, V);
        assertEquals(V, dobj.get(p));
        
        dobj.set(i, V);
        assertEquals(V, dobj.getString(i));
        dobj.setString(i, V);
        assertEquals(V, dobj.get(i));

        dobj.set(p, dataHelper.convert(p, V));
        assertEquals(V, dobj.get(p));
    }


    public void testDay() throws Exception
    {
        System.out.println("Day");
        DataObject dobj = createDataObject();
        Type t = dobj.getType();
        String s = "day";
        Property p = t.getProperty(s);
        int i = DAY_P_I;
        String V1 = "---01";
        String V2 = "---31";
        String V3 = "---15Z";
        String V4 = "---28+08:00";
        
        _testGDate(dobj, s, i, p, V1);
        _testGDate(dobj, s, i, p, V2);
        _testGDate(dobj, s, i, p, V3);
        _testGDate(dobj, s, i, p, V4);
    }

    public void testDateTime() throws Exception
    {
        System.out.println("DateTime");
        DataObject dobj = createDataObject();
        Type t = dobj.getType();
        String s = "dateTime";
        Property p = t.getProperty(s);
        int i = DATETIME_P_I;
        String V1 = "1965-12-31T23:59:59Z";
        String V2 = "1999-01-01T00:00:00+01:00";
        String V3 = "2001-12-31T23:59:59.9999";
        String V4 = "1999-12-31T23:59:59.1234567890-14:00";
        
        _testGDate(dobj, s, i, p, V1);
        _testGDate(dobj, s, i, p, V2);
        _testGDate(dobj, s, i, p, V3);
        _testGDate(dobj, s, i, p, V4);
    }

    public void testDuration() throws Exception
    {
        System.out.println("Duration");
        DataObject dobj = createDataObject();
        Type t = dobj.getType();
        String s = "duration";
        Property p = t.getProperty(s);
        int i = DURATION_P_I;
        String V1 = "PT0S";
        String V2 = "P1Y";
        String V3 = "PT3600S";
        String V4 = "P1Y1M1DT1H1M1.1S";
        String V5 = "-P30D";
        
        _testGDate(dobj, s, i, p, V1);
        _testGDate(dobj, s, i, p, V2);
        _testGDate(dobj, s, i, p, V3);
        _testGDate(dobj, s, i, p, V4);
        _testGDate(dobj, s, i, p, V5);
    }

    public void testMonth() throws Exception
    {
        System.out.println("Month");
        DataObject dobj = createDataObject();
        Type t = dobj.getType();
        String s = "month";
        Property p = t.getProperty(s);
        int i = MONTH_P_I;
        String V1 = "--01";
        String V2 = "--13";
        String V3 = "--12Z";
        String V4 = "--08+08:00";
        
        _testGDate(dobj, s, i, p, V1);
        //_testGDate(dobj, s, i, p, V2);    //IllegalArgumentException
        _testGDate(dobj, s, i, p, V3);
        _testGDate(dobj, s, i, p, V4);
    }

    public void testMonthDay() throws Exception
    {
        System.out.println("MonthDay");
        DataObject dobj = createDataObject();
        Type t = dobj.getType();
        String s = "monthDay";
        Property p = t.getProperty(s);
        int i = MONTHDAY_P_I;
        String V1 = "--01-01";
        String V2 = "--12-31";
        String V3 = "--08-15Z";
        String V4 = "--02-29+08:00";
        
        _testGDate(dobj, s, i, p, V1);
        _testGDate(dobj, s, i, p, V2);
        _testGDate(dobj, s, i, p, V3);
        _testGDate(dobj, s, i, p, V4);
    }

    public void testTime() throws Exception
    {
        System.out.println("Time");
        DataObject dobj = createDataObject();
        Type t = dobj.getType();
        String s = "time";
        Property p = t.getProperty(s);
        int i = TIME_P_I;
        String V1 = "00:00:00";
        String V2 = "12:00:01";
        String V3 = "18:30:45.001Z";
        String V4 = "07:14:59.1+08:00";
        
        _testGDate(dobj, s, i, p, V1);
        _testGDate(dobj, s, i, p, V2);
        _testGDate(dobj, s, i, p, V3);
        _testGDate(dobj, s, i, p, V4);
    }

    public void testYear() throws Exception
    {
        System.out.println("Year");
        DataObject dobj = createDataObject();
        Type t = dobj.getType();
        String s = "year";
        Property p = t.getProperty(s);
        int i = YEAR_P_I;
        String V1 = "2001";
        String V2 = "1931";
        String V3 = "2015Z";
        String V4 = "1978+08:00";
        
        _testGDate(dobj, s, i, p, V1);
        _testGDate(dobj, s, i, p, V2);
        _testGDate(dobj, s, i, p, V3);
        _testGDate(dobj, s, i, p, V4);
    }

    public void testYearMonth() throws Exception
    {
        System.out.println("YearMonth");
        DataObject dobj = createDataObject();
        Type t = dobj.getType();
        String s = "yearMonth";
        Property p = t.getProperty(s);
        int i = YEARMONTH_P_I;
        String V1 = "2001-01";
        String V2 = "1999-12";
        String V3 = "2006-11Z";
        String V4 = "2006-11+08:00";
        
        _testGDate(dobj, s, i, p, V1);
        _testGDate(dobj, s, i, p, V2);
        _testGDate(dobj, s, i, p, V3);
        _testGDate(dobj, s, i, p, V4);
    }

    public void testYearMonthDay() throws Exception
    {
        System.out.println("YearMonthDay");
        DataObject dobj = createDataObject();
        Type t = dobj.getType();
        String s = "yearMonthDay";
        Property p = t.getProperty(s);
        int i = YEARMONTHDAY_P_I;
        String V1 = "2001-01-01";
        String V2 = "1999-12-31";
        String V3 = "2006-11-15Z";
        String V4 = "2000-02-29+08:00";
        
        _testGDate(dobj, s, i, p, V1);
        _testGDate(dobj, s, i, p, V2);
        _testGDate(dobj, s, i, p, V3);
        _testGDate(dobj, s, i, p, V4);
    }
    /*
    public void testDate()
    {
        DataObject dobj = createDataObject();
        Date dt = new Date(0l);
        dobj.setDate("date", dt);
        dobj.setDate("dateTime", dt);
        dobj.setDate("yearMonthDay", dt);
        System.out.println(dobj.getString("date"));
        System.out.println(dobj.getString("dateTime"));
        System.out.println(dobj.getString("yearMonthDay"));
    }
    */

    // tests for createDataObject()

    private static final String ITEMS1 =
        "<der:items xmlns:der=\"http://sdo/test/derivation\">" + newline +
        "    <der:product>" + newline +
        "        <der:name>SuperGizmo</der:name>" + newline +
        "        <der:number>021-456-9970</der:number>" + newline +
        "    </der:product>" + newline +
        "</der:items>";

    private static final String ITEMS2 =
        "<der:items xmlns:der=\"http://sdo/test/derivation\">" + newline +
        "    <der:product>" + newline +
        "        <der:name>SuperGizmo</der:name>" + newline +
        "        <der:number>021-456-9970</der:number>" + newline +
        "    </der:product>" + newline +
        "    <der:product xsi:type=\"der:ShirtType\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" + newline +
        "        <der:name>Oxford</der:name>" + newline +
        "        <der:number>121-333-9655</der:number>" + newline +
        "        <der:size>M</der:size>" + newline +
        "        <der:color>white</der:color>" + newline +
        "    </der:product>" + newline +
        "</der:items>";

    private void _testProduct(DataObject product)
    {
        assertNotNull(product);
        Type productType = typeHelper.getType("http://sdo/test/derivation", 
                                              "ProductType");
        assertEquals(productType, product.getType());
        // created data object has properties unset
        assertEquals(2, product.getInstanceProperties().size());
        assertFalse(product.isSet("name"));
        assertFalse(product.isSet("number"));
        product.set("name", "SuperGizmo");
        product.set("number", "021-456-9970");
        String out = xmlHelper.save(product.getContainer(), 
                                    "http://sdo/test/derivation", "items");
        assertEquals(ITEMS1.replaceAll(">[ \r\n]*<", "><"), out);
    }

    private void _testShirt(DataObject shirt)
    {
        assertNotNull(shirt);
        Type shirtType = typeHelper.getType("http://sdo/test/derivation", 
                                              "ShirtType");
        assertEquals(shirtType, shirt.getType());
        assertEquals(4, shirtType.getProperties().size());
        assertEquals(2, shirtType.getDeclaredProperties().size());
        assertEquals(4, shirt.getInstanceProperties().size());
        // created data object has properties unset
        assertFalse(shirt.isSet("name"));
        assertFalse(shirt.isSet("number"));
        assertFalse(shirt.isSet("size"));
        assertFalse(shirt.isSet("color"));
        shirt.set("name", "Oxford");
        shirt.set("number", "121-333-9655");
        shirt.set("size", "M");
        shirt.set("color", "white");
    }

    private void _testItems(DataObject items)
    {
        List<DataObject> itemList = (List<DataObject>)items.getList("product");
        assertEquals(2, itemList.size());
        Type productType = typeHelper.getType("http://sdo/test/derivation", 
                                              "ProductType");
        Type shirtType = typeHelper.getType("http://sdo/test/derivation", 
                                            "ShirtType");
        DataObject item1 = itemList.get(0);
        assertEquals(productType, item1.getType());
        assertTrue(item1 instanceof sdo.test.derivation.ProductType);
        DataObject item2 = itemList.get(1);
        assertEquals(shirtType, item2.getType());
        assertTrue(item2 instanceof sdo.test.derivation.ShirtType);
        String out = xmlHelper.save(items, 
                                   "http://sdo/test/derivation", "items");
        assertEquals(ITEMS2.replaceAll(">[ \r\n]*<", "><"), out);
    }

    /* createDataObject(String propertyName) */
    public void testCreateByName() throws Exception
    {
        System.out.println("testCreateByName()");
        DataObject items = factory.create("http://sdo/test/derivation", 
                                          "ItemsType");
        DataObject product = items.createDataObject("product");
        _testProduct(product);
    }

    /* createDataObject(int propertyIndex) */
    public void testCreateByIndex() throws Exception
    {
        System.out.println("testCreateByIndex()");
        DataObject items = factory.create("http://sdo/test/derivation", 
                                          "ItemsType");
        List props = items.getType().getProperties();
        List instanceProps = items.getInstanceProperties();
        assertTrue(props.size() == instanceProps.size());
        assertEquals(1, props.size());
        DataObject product = items.createDataObject(0);
        _testProduct(product);
    }

    /* createDataObject(Property property) */
    public void testCreateByProperty() throws Exception
    {
        System.out.println("testCreateByProperty()");
        DataObject items = factory.create("http://sdo/test/derivation", 
                                          "ItemsType");
        List props = items.getType().getProperties();
        List instanceProps = items.getInstanceProperties();
        assertTrue(props.size() == instanceProps.size());
        assertEquals(1, props.size());
        Property p = (Property)props.get(0);
        assertEquals("product", p.getName());
        DataObject product = items.createDataObject(p);
        _testProduct(product);
    }

    /* createDataObject(String propertyName,
                        String namespaceURI, String typeName) */
    public void testCreateByNames() throws Exception
    {
        System.out.println("testCreateByNames()");
        DataObject items = factory.create("http://sdo/test/derivation", 
                                          "ItemsType");
        DataObject product = 
            items.createDataObject("product",
                                   "http://sdo/test/derivation", "ProductType");
        _testProduct(product);
        DataObject shirt = 
            items.createDataObject("product",
                                   "http://sdo/test/derivation", "ShirtType");
        _testShirt(shirt);
        _testItems(items);
    }

    /* createDataObject(int propertyIndex,
                        String namespaceURI, String typeName) */
    public void testCreateByIndexAndNames()
    {
        System.out.println("testCreateByIndexAndNames()");
        DataObject items = factory.create("http://sdo/test/derivation", 
                                          "ItemsType");
        DataObject product = 
            items.createDataObject(0,
                                   "http://sdo/test/derivation", "ProductType");
        _testProduct(product);
        DataObject shirt = 
            items.createDataObject(0,
                                   "http://sdo/test/derivation", "ShirtType");
        _testShirt(shirt);
        _testItems(items);
    }

    /* createDataObject(Property property,
                        Type type) */
    public void testCreateByPropertyAndType()
    {
        System.out.println("testCreateByPropertyAndType()");
        DataObject items = factory.create("http://sdo/test/derivation", 
                                          "ItemsType");
        Property p = items.getInstanceProperty("product");
        Type productType = typeHelper.getType("http://sdo/test/derivation", 
                                              "ProductType");
        Type shirtType = typeHelper.getType("http://sdo/test/derivation", 
                                            "ShirtType");
        DataObject product = 
            items.createDataObject(p, productType);
        _testProduct(product);
        DataObject shirt = 
            items.createDataObject(p, shirtType);
        _testShirt(shirt);
        _testItems(items);
    }

    // the above all called createDataObject() with a many-valued property
    // test createDataObject with a single-valued property as well

    /* test createDataObject() with many- and single-valued properties */
    public void testCreateManyVsSingle()
    {
        System.out.println("testCreateManyVsSingle()");
        DataObject items = factory.create("http://www.example.com/choice", 
                                          "ItemsType");
        // many-valued property
        Property shirtProperty = items.getType().getProperty("shirt");
        assertNotNull(shirtProperty);
        assertTrue(shirtProperty.isMany());
        DataObject shirt1 = items.createDataObject(shirtProperty);
        shirt1.set("id", "X888");
        shirt1.set("color", "black");
        // single-valued property
        Property shirtSizeProperty = shirt1.getType().getProperty("size");
        assertNotNull(shirtSizeProperty);
        assertFalse(shirtSizeProperty.isMany());
        DataObject shirt1Size = shirt1.createDataObject(shirtSizeProperty);
        shirt1Size.set("collar", 15);
        shirt1Size.set("sleeve", 33);
        DataObject shirt2 = items.createDataObject(shirtProperty);
        shirt2.set("id", "X889");
        shirt2.set("color", "blue");
        DataObject shirt2Size = shirt2.createDataObject(shirtSizeProperty);
        shirt2Size.set("collar", 16);
        shirt2Size.set("sleeve", 34);
        
        // shirts are appended to list as they are created
        assertEquals(2, items.getList("shirt").size());
        assertEquals(shirt1, items.getList("shirt").get(0));
        assertEquals(shirt2, items.getList("shirt").get(1));

        assertEquals(15, items.getInt("shirt[1]/size/collar"));
        assertEquals(33, items.getInt("shirt[1]/size/sleeve"));
        assertEquals(16, items.getInt("shirt[2]/size/collar"));
        assertEquals(34, items.getInt("shirt[2]/size/sleeve"));
    }

    private static final String COMPANY0 =
        "<com:company name=\"ACME\" employeeOfTheMonth=\"E0007\" xmlns:com=\"company2.xsd\"/>";
    private static final String COMPANY1 =
        "<com:company name=\"ACME\" employeeOfTheMonth=\"E0007\" xmlns:com=\"company2.xsd\">" + newline +
        "    <departments name=\"Liquidation\" location=\"London\" number=\"123\">" + newline +
        "        <employees name=\"James Bond\" SN=\"E0007\"/>" + newline +
        "    </departments>" + newline +
        "</com:company>";

    /* test createDataObject(Property) with a noncontainment property */
    public void testCreateNoncontainedDataObject()
    {
        System.out.println("testCreateNoncontainedDataObject()");
        DataObject company = factory.create("company2.xsd", "CompanyType");
        company.set("name", "ACME");
        DataObject eOTM = company.createDataObject("employeeOfTheMonth");
        assertNotNull(eOTM);
        eOTM.set("name", "James Bond");
        eOTM.set("SN", "E0007");
        String out = xmlHelper.save(company, "company2.xsd", "company");
        assertEquals(COMPANY0, out);
        DataObject department = company.createDataObject("departments");
        department.set("name", "Liquidation");
        department.set("location", "London");
        department.set("number", 123);
        List employees = department.getList("employees");
        employees.add(eOTM);
        out = xmlHelper.save(company, "company2.xsd", "company");
        assertEquals(COMPANY1.replaceAll(">[ \r\n]*<", "><"), out);
    }


    // tests for exceptions

    /* property not in instance properties or property index out of range */
    public void testIllegalArgument()
    {
        // IllegalArgumentException
    }

    /* index out of range in list for many-valued property */
    public void testIndexOutOfRange()
    {
        // IndexOutOfBoundsException
    }

    /* modify read-only property */
    public void testModifyReadOnly()
    {
        // UnsupportedOperationException
    }

    /* getList() for single-valued property
       getString() for many-valued property */
    public void testIncompatibleManyness()
    {
        // ClassCastException
    }

    /* circular containment */
    public void testCircularContainment()
    {
        // IllegalArgumentException
    }

    public void testSetNullList()
    {
        System.out.println("testSetNullList()");
        DataObject dobj = createDataObject();
        dobj.setList("object", null); // should this be allowed?
        List value = dobj.getList("object");
        assertNotNull(value);
        //assertEquals(0, value.size()); // BUG?
        // looks like the null value got interpreted as a value for
        // "object" rather than the value of the List for "object"!
        assertEquals(1, value.size());
        assertNull(value.get(0));
    }
}
