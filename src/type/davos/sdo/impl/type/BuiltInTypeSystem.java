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
package davos.sdo.impl.type;

import davos.sdo.PropertyXML;
import davos.sdo.TypeXML;
import davos.sdo.impl.common.Common;
import davos.sdo.impl.common.Names;
import davos.sdo.type.TypeSystem;
import javax.sdo.ChangeSummary;
import javax.sdo.DataObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;

import javax.xml.namespace.QName;

import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.XmlBeans;

/**
 * @author Cezar Andrei (cezar dot andrei at gmail dot com)
 *         Date: Jan 12, 2006
 */
public class BuiltInTypeSystem
    extends TypeSystemBase
    implements TypeSystem
{
    public static final TypeSystem INSTANCE;

    public static final TypeXML TYPE;
    public static final int TYPECODE_TYPE = 1;
    public static final PropertyXML P_TYPE_BASETYPE;            // baseType
    public static final PropertyXML P_TYPE_PROPERTY;            // property
    public static final PropertyXML P_TYPE_ALIASNAME;           // aliasName
    public static final PropertyXML P_TYPE_NAME;                // name
    public static final PropertyXML P_TYPE_URI;                 // uri
    public static final PropertyXML P_TYPE_DATATYPE;            // dataType
    public static final PropertyXML P_TYPE_OPEN;                // open
    public static final PropertyXML P_TYPE_SEQUENCED;           // sequenced
    public static final PropertyXML P_TYPE_ABSTRACT;            // abstract
    public static final PropertyXML P_TYPE_JAVACLASS;           // javaClass

    public static final TypeXML TYPES;
    public static final int TYPECODE_TYPES = 50;
    public static final PropertyXML P_TYPES_TYPE;               // type

    public static final PropertyXML P_TYPES;
    public static final PropertyXML P_TYPE;

    public static final TypeXML PROPERTY;
    public static final int TYPECODE_PROPERTY = 2;
    public static final PropertyXML P_PROPERTY_ALIASNAME;       // aliasName
    public static final PropertyXML P_PROPERTY_NAME;            // name
    public static final PropertyXML P_PROPERTY_MANY;            // many
    public static final PropertyXML P_PROPERTY_CONTAINMENT;     // ontainment
    public static final PropertyXML P_PROPERTY_TYPE;            // type
    public static final PropertyXML P_PROPERTY_DEFAULT;         // default
    public static final PropertyXML P_PROPERTY_READONLY;        // readOnly
    public static final PropertyXML P_PROPERTY_OPPOSITE;        // opposite
    public static final PropertyXML P_PROPERTY_NULLABLE;        // nullable
    public static final PropertyXML P_PROPERTY_XMLELEMENT;      // xmlElement

    public static final TypeXML BOOLEAN;
    public static final int TYPECODE_BOOLEAN = 3;

    public static final TypeXML BYTE;
    public static final int TYPECODE_BYTE = 4;

    public static final TypeXML BYTES;
    public static final int TYPECODE_BYTES = 5;

    public static final TypeXML CHARACTER;
    public static final int TYPECODE_CHARACTER = 6;

    public static final TypeXML DOUBLE;
    public static final int TYPECODE_DOUBLE = 7;

    public static final TypeXML FLOAT;
    public static final int TYPECODE_FLOAT = 8;

    public static final TypeXML INT;
    public static final int TYPECODE_INT = 9;

    public static final TypeXML LONG;
    public static final int TYPECODE_LONG = 10;

    public static final TypeXML SHORT;
    public static final int TYPECODE_SHORT = 11;

    public static final TypeXML DATE;
    public static final int TYPECODE_DATE = 12;

    public static final TypeXML DATETIME;
    public static final int TYPECODE_DATETIME = 13;

    public static final TypeXML DAY;
    public static final int TYPECODE_DAY = 14;

    public static final TypeXML DECIMAL;
    public static final int TYPECODE_DECIMAL = 15;

    public static final TypeXML DURATION;
    public static final int TYPECODE_DURATION = 16;

    public static final TypeXML INTEGER;
    public static final int TYPECODE_INTEGER = 17;

    public static final TypeXML MONTH;
    public static final int TYPECODE_MONTH = 18;

    public static final TypeXML MONTHDAY;
    public static final int TYPECODE_MONTHDAY = 19;

    public static final TypeXML STRING;
    public static final int TYPECODE_STRING = 20;

    public static final TypeXML STRINGS;
    public static final int TYPECODE_STRINGS = 21;

    public static final TypeXML TIME;
    public static final int TYPECODE_TIME = 22;

    public static final TypeXML URI;
    public static final int TYPECODE_URI = 23;

    public static final TypeXML YEAR;
    public static final int TYPECODE_YEAR = 24;

    public static final TypeXML YEARMONTH;
    public static final int TYPECODE_YEARMONTH = 25;

    public static final TypeXML YEARMONTHDAY;
    public static final int TYPECODE_YEARMONTHDAY = 26;


    public static final TypeXML OBJECT;
    public static final int TYPECODE_OBJECT = 27;

    public static final TypeXML DATAOBJECT;
    public static final int TYPECODE_DATAOBJECT = 28;


    public static final TypeXML DATAGRAPHTYPE;
    public static final int TYPECODE_DATAGRAPHTYPE = 29;
    public static final PropertyXML P_DATAGRAPH;
    public static final PropertyXML P_DATAOBJECT;

    public static final TypeXML BASEDATAGRAPHTYPE;
    public static final int TYPECODE_BASEDATAGRAPHTYPE = 30;

    public static final TypeXML MODELSTYPE;
    public static final int TYPECODE_MODELSTYPE = 30;

    public static final TypeXML XSDTYPE;
    public static final int TYPECODE_XSDTYPE = 30;

    public static final TypeXML CHANGESUMMARYTYPE;
    public static final int TYPECODE_CHANGESUMMARYTYPE = 31;

    //public static final int TYPECODE_TEXTYPE = 32;

    public static final TypeXML VALUETYPE;
    public static final int TYPECODE_VALUETYPE = 33;
    public static final PropertyXML P_VALUETYPE_VALUE;

    public static final TypeXML BEADATAOBJECT;
    public static final int TYPECODE_BEADATAOBJECT = 34;


    public static final TypeXML BOOLEANOBJECT;
    public static final int TYPECODE_BOOLEANOBJECT = 36;

    public static final TypeXML BYTEOBJECT;
    public static final int TYPECODE_BYTEOBJECT = 37;

    public static final TypeXML CHARACTEROBJECT;
    public static final int TYPECODE_CHARACTEROBJECT = 38;

    public static final TypeXML DOUBLEOBJECT;
    public static final int TYPECODE_DOUBLEOBJECT = 39;

    public static final TypeXML FLOATOBJECT;
    public static final int TYPECODE_FLOATOBJECT = 40;

    public static final TypeXML INTOBJECT;
    public static final int TYPECODE_INTOBJECT = 41;

    public static final TypeXML LONGOBJECT;
    public static final int TYPECODE_LONGOBJECT = 42;

    public static final TypeXML SHORTOBJECT;
    public static final int TYPECODE_SHORTOBJECT = 43;

    public static final TypeXML WRAPPERTYPE;
    public static final int TYPECODE_WRAPPERTYPE = 44;

    // all spec defined have a positive typecode
    // all user defined types have -1 as a typecode
    public static final int TYPECODE_USERDEFINED = -1;

//    public static final int TYPECODE_PRIMITIVE_FIRST = TYPECODE_BOOLEAN;
//    public static final int TYPECODE_PRIMITIVE_LAST = TYPECODE_SHORT;

    public static Map<Class, TypeXML> INSTANCECLASSTOTYPE;


    static
    {
        BuiltInTypeSystem bits = new BuiltInTypeSystem();

        TypeImpl t_type = TypeImpl.create();
        TYPE = t_type;
        List<PropertyXML> t_type_propList = new ArrayList<PropertyXML>();
        SchemaTypeLoader builtinSchemaTypeSystem = XmlBeans.getBuiltinTypeSystem();
        SchemaTypeLoader schematypeLoaderForClassLoader = XmlBeans.typeLoaderForClassLoader(BuiltInTypeSystem.class.getClassLoader());
        SchemaType st;
        st = schematypeLoaderForClassLoader.findType(new QName(Names.URI_SDO, "Type"));
        t_type.init("Type", Names.URI_SDO, TYPECODE_TYPE, TypeImpl.class, false, true, false, false, false, false,
            Common.EMPTY_TYPEXML_LIST, t_type_propList, Common.EMPTY_STRING_LIST, st, bits);
        // the properties will be added to the list further down when all of their types are defined

        // This is the "types" type from sdoModel.xsd
        TypeImpl t_types = TypeImpl.create();
        TYPES = t_types;
        List<PropertyXML> t_types_propList = new ArrayList<PropertyXML>(1);
        st = schematypeLoaderForClassLoader.findType(new QName(Names.URI_SDO, "Types"));
        t_types.init("Types", Names.URI_SDO, TYPECODE_TYPES, null, false, true, false, false, false, false,
            Common.EMPTY_TYPEXML_LIST, t_types_propList, Common.EMPTY_STRING_LIST, st, bits);

        P_TYPE = PropertyImpl.create(TYPE, "type", false, true, null, null, false, false, null,
            Common.EMPTY_STRING_LIST, "type", Names.URI_SDO, -1, true, false, true);
        P_TYPES = PropertyImpl.create(TYPES, "types", false, true, null, null, false, false, null,
            Common.EMPTY_STRING_LIST, "types", Names.URI_SDO, 0, true, false, true);

        TypeImpl t_property = TypeImpl.create();
        PROPERTY = t_property;
        st = schematypeLoaderForClassLoader.findType(new QName(Names.URI_SDO, "Property"));
        List<PropertyXML> t_property_propList = new ArrayList<PropertyXML>();
        t_property.init("Property", Names.URI_SDO, TYPECODE_PROPERTY, PropertyImpl.class, false, true, false, false, false, false,
            Common.EMPTY_TYPEXML_LIST, t_property_propList, Common.EMPTY_STRING_LIST, st, bits);
        // the properties will be added to the list further down when all of their types are defined

        TypeImpl t_boolean = TypeImpl.create();
        st = builtinSchemaTypeSystem.findType(new QName(Names.URI_XSD, "boolean"));
        t_boolean.init("Boolean", Names.URI_SDO, TYPECODE_BOOLEAN, boolean.class, true, false, false, false, false, false,
            Common.EMPTY_TYPEXML_LIST, Common.EMPTY_PROPERTYXML_LIST, Common.EMPTY_STRING_LIST, st, bits);
        BOOLEAN = t_boolean;

        TypeImpl t_byte = TypeImpl.create();
        st = builtinSchemaTypeSystem.findType(new QName(Names.URI_XSD, "byte"));
        t_byte.init("Byte", Names.URI_SDO, TYPECODE_BYTE, byte.class, true, false, false, false, false, false,
            Common.EMPTY_TYPEXML_LIST, Common.EMPTY_PROPERTYXML_LIST, Common.EMPTY_STRING_LIST, st, bits);
        BYTE = t_byte;

        TypeImpl t_bytes = TypeImpl.create();
        st = builtinSchemaTypeSystem.findType(new QName(Names.URI_XSD, "hexBinary"));
        t_bytes.init("Bytes", Names.URI_SDO, TYPECODE_BYTES, byte[].class, true, false, false, false, false, false,
            Common.EMPTY_TYPEXML_LIST, Common.EMPTY_PROPERTYXML_LIST, Common.EMPTY_STRING_LIST, st, bits);
        BYTES = t_bytes;

        TypeImpl t_character = TypeImpl.create();
        // In sdoModel.xsd, each predefined SDO data type has a Schema type that it maps to
        // Each such defined Schema type has as base a built-in Schema type
        // We use the built-in Schema types directly because they are more useful in practice
        // (Can be understood by non-SDO aware processors)
        st = builtinSchemaTypeSystem.findType(new QName(Names.URI_XSD, "string"));
        t_character.init("Character", Names.URI_SDO, TYPECODE_CHARACTER, char.class, true, false, false, false, false, false,
            Common.EMPTY_TYPEXML_LIST, Common.EMPTY_PROPERTYXML_LIST, Common.EMPTY_STRING_LIST, st, bits);
        CHARACTER = t_character;

        TypeImpl t_date = TypeImpl.create();
        st = builtinSchemaTypeSystem.findType(new QName(Names.URI_XSD, "dateTime"));
        t_date.init("Date", Names.URI_SDO, TYPECODE_DATE, java.util.Date.class, true, false, false, false, false, false,
            Common.EMPTY_TYPEXML_LIST, Common.EMPTY_PROPERTYXML_LIST, Common.EMPTY_STRING_LIST, st, bits);
        DATE = t_date;

        TypeImpl t_datetime = TypeImpl.create();
        st = builtinSchemaTypeSystem.findType(new QName(Names.URI_XSD, "dateTime"));
        t_datetime.init("DateTime", Names.URI_SDO, TYPECODE_DATETIME, String.class, true, false, false, false, false, false,
            Common.EMPTY_TYPEXML_LIST, Common.EMPTY_PROPERTYXML_LIST, Common.EMPTY_STRING_LIST, st, bits);
        DATETIME = t_datetime;

        TypeImpl t_day = TypeImpl.create();
        st = builtinSchemaTypeSystem.findType(new QName(Names.URI_XSD, "gDay"));
        t_day.init("Day", Names.URI_SDO, TYPECODE_DAY, String.class, true, false, false, false, false, false,
            Common.EMPTY_TYPEXML_LIST, Common.EMPTY_PROPERTYXML_LIST, Common.EMPTY_STRING_LIST, st, bits);
        DAY = t_day;

        TypeImpl t_decimal = TypeImpl.create();
        st = builtinSchemaTypeSystem.findType(new QName(Names.URI_XSD, "decimal"));
        t_decimal.init("Decimal", Names.URI_SDO, TYPECODE_DECIMAL, java.math.BigDecimal.class, true, false, false, false,
            false, false, Common.EMPTY_TYPEXML_LIST, Common.EMPTY_PROPERTYXML_LIST, Common.EMPTY_STRING_LIST, st, bits);
        DECIMAL = t_decimal;

        TypeImpl t_double = TypeImpl.create();
        st = builtinSchemaTypeSystem.findType(new QName(Names.URI_XSD, "double"));
        t_double.init("Double", Names.URI_SDO, TYPECODE_DOUBLE, double.class, true, false, false, false, false, false,
            Common.EMPTY_TYPEXML_LIST, Common.EMPTY_PROPERTYXML_LIST, Common.EMPTY_STRING_LIST, st, bits);
        DOUBLE = t_double;

        TypeImpl t_duration = TypeImpl.create();
        st = builtinSchemaTypeSystem.findType(new QName(Names.URI_XSD, "duration"));
        t_duration.init("Duration", Names.URI_SDO, TYPECODE_DURATION, String.class, true, false, false, false, false, false,
            Common.EMPTY_TYPEXML_LIST, Common.EMPTY_PROPERTYXML_LIST, Common.EMPTY_STRING_LIST, st, bits);
        DURATION = t_duration;

        TypeImpl t_float = TypeImpl.create();
        st = builtinSchemaTypeSystem.findType(new QName(Names.URI_XSD, "float"));
        t_float.init("Float", Names.URI_SDO, TYPECODE_FLOAT, float.class, true, false, false, false, false, false,
            Common.EMPTY_TYPEXML_LIST, Common.EMPTY_PROPERTYXML_LIST, Common.EMPTY_STRING_LIST, st, bits);
        FLOAT = t_float;

        TypeImpl t_int = TypeImpl.create();
        st = builtinSchemaTypeSystem.findType(new QName(Names.URI_XSD, "int"));
        t_int.init("Int", Names.URI_SDO, TYPECODE_INT, int.class, true, false, false, false, false, false,
            Common.EMPTY_TYPEXML_LIST, Common.EMPTY_PROPERTYXML_LIST, Common.EMPTY_STRING_LIST, st, bits);
        INT = t_int;

        TypeImpl t_integer = TypeImpl.create();
        st = builtinSchemaTypeSystem.findType(new QName(Names.URI_XSD, "integer"));
        t_integer.init("Integer", Names.URI_SDO, TYPECODE_INTEGER, java.math.BigInteger.class, true, false, false, false,
            false, false, Common.EMPTY_TYPEXML_LIST, Common.EMPTY_PROPERTYXML_LIST, Common.EMPTY_STRING_LIST, st, bits);
        INTEGER = t_integer;

        TypeImpl t_long = TypeImpl.create();
        st = builtinSchemaTypeSystem.findType(new QName(Names.URI_XSD, "long"));
        t_long.init("Long", Names.URI_SDO, TYPECODE_LONG, long.class, true, false, false, false, false, false,
            Common.EMPTY_TYPEXML_LIST, Common.EMPTY_PROPERTYXML_LIST, Common.EMPTY_STRING_LIST, st, bits);
        LONG = t_long;

        TypeImpl t_month = TypeImpl.create();
        st = builtinSchemaTypeSystem.findType(new QName(Names.URI_XSD, "gMonth"));
        t_month.init("Month", Names.URI_SDO, TYPECODE_MONTH, String.class, true, false, false, false, false, false,
            Common.EMPTY_TYPEXML_LIST, Common.EMPTY_PROPERTYXML_LIST, Common.EMPTY_STRING_LIST, st, bits);
        MONTH = t_month;

        TypeImpl t_monthday = TypeImpl.create();
        st = builtinSchemaTypeSystem.findType(new QName(Names.URI_XSD, "gMonthDay"));
        t_monthday.init("MonthDay", Names.URI_SDO, TYPECODE_MONTHDAY, String.class, true, false, false, false, false, false,
            Common.EMPTY_TYPEXML_LIST, Common.EMPTY_PROPERTYXML_LIST, Common.EMPTY_STRING_LIST, st, bits);
        MONTHDAY = t_monthday;

        TypeImpl t_short = TypeImpl.create();
        st = builtinSchemaTypeSystem.findType(new QName(Names.URI_XSD, "short"));
        t_short.init("Short", Names.URI_SDO, TYPECODE_SHORT, short.class, true, false, false, false, false, false,
            Common.EMPTY_TYPEXML_LIST, Common.EMPTY_PROPERTYXML_LIST, Common.EMPTY_STRING_LIST, st, bits);
        SHORT = t_short;

        TypeImpl t_string = TypeImpl.create();
        st = builtinSchemaTypeSystem.findType(new QName(Names.URI_XSD, "string"));
        t_string.init("String", Names.URI_SDO, TYPECODE_STRING, String.class, true, false, false, false, false, false,
            Common.EMPTY_TYPEXML_LIST, Common.EMPTY_PROPERTYXML_LIST, Common.EMPTY_STRING_LIST, st, bits);
        STRING = t_string;

        TypeImpl t_strings = TypeImpl.create();
        st = builtinSchemaTypeSystem.findType(new QName(Names.URI_XSD, "NMTOKENS"));
        t_strings.init("Strings", Names.URI_SDO, TYPECODE_STRINGS, List.class, true, false, false, false, false, false,
            Common.EMPTY_TYPEXML_LIST, Common.EMPTY_PROPERTYXML_LIST, Common.EMPTY_STRING_LIST, st, bits);
        STRINGS = t_strings;

        TypeImpl t_time = TypeImpl.create();
        st = builtinSchemaTypeSystem.findType(new QName(Names.URI_XSD, "time"));
        t_time.init("Time", Names.URI_SDO, TYPECODE_TIME, String.class, true, false, false, false, false, false,
            Common.EMPTY_TYPEXML_LIST, Common.EMPTY_PROPERTYXML_LIST, Common.EMPTY_STRING_LIST, st, bits);
        TIME = t_time;

        TypeImpl t_uri = TypeImpl.create();
        st = builtinSchemaTypeSystem.findType(new QName(Names.URI_XSD, "anyURI"));
        t_uri.init("URI", Names.URI_SDO, TYPECODE_URI, String.class, true, false, false, false, false, false,
            Common.EMPTY_TYPEXML_LIST, Common.EMPTY_PROPERTYXML_LIST, Common.EMPTY_STRING_LIST, st, bits);
        URI = t_uri;

        TypeImpl t_year = TypeImpl.create();
        st = builtinSchemaTypeSystem.findType(new QName(Names.URI_XSD, "gYear"));
        t_year.init("Year", Names.URI_SDO, TYPECODE_YEAR, String.class, true, false, false, false, false, false,
            Common.EMPTY_TYPEXML_LIST, Common.EMPTY_PROPERTYXML_LIST, Common.EMPTY_STRING_LIST, st, bits);
        YEAR = t_year;

        TypeImpl t_yearmonth = TypeImpl.create();
        st = builtinSchemaTypeSystem.findType(new QName(Names.URI_XSD, "gYearMonth"));
        t_yearmonth.init("YearMonth", Names.URI_SDO, TYPECODE_YEARMONTH, String.class, true, false, false, false, false, false,
            Common.EMPTY_TYPEXML_LIST, Common.EMPTY_PROPERTYXML_LIST, Common.EMPTY_STRING_LIST, st, bits);
        YEARMONTH = t_yearmonth;

        TypeImpl t_yearmonthday = TypeImpl.create();
        st = builtinSchemaTypeSystem.findType(new QName(Names.URI_XSD, "date"));
        t_yearmonthday.init("YearMonthDay", Names.URI_SDO, TYPECODE_YEARMONTHDAY, String.class, true, false, false, false,
            false, false, Common.EMPTY_TYPEXML_LIST, Common.EMPTY_PROPERTYXML_LIST, Common.EMPTY_STRING_LIST, st, bits);
        YEARMONTHDAY = t_yearmonthday;

        // rest of special types
        TypeImpl t_object = TypeImpl.create();
        st = builtinSchemaTypeSystem.findType(new QName(Names.URI_XSD, "anySimpleType"));
        t_object.init("Object", Names.URI_SDO, TYPECODE_OBJECT, Object.class, true, false, false, true, false, false,
            Common.EMPTY_TYPEXML_LIST, Common.EMPTY_PROPERTYXML_LIST, Common.EMPTY_STRING_LIST, st, bits);
        OBJECT = t_object;

        TypeImpl t_dataobject = TypeImpl.create();
        st = builtinSchemaTypeSystem.findType(new QName(Names.URI_XSD, "anyType"));
        t_dataobject.init("DataObject", Names.URI_SDO, TYPECODE_DATAOBJECT, DataObject.class, false, true, true, true, false, true,
            Common.EMPTY_TYPEXML_LIST, Common.EMPTY_PROPERTYXML_LIST, Common.EMPTY_STRING_LIST, st, bits);
        DATAOBJECT = t_dataobject;


        TypeImpl t_changesummarytype = TypeImpl.create();
        PropertyImpl p_changesummarytype_create = PropertyImpl.create(STRING, "create", false,
            false, t_changesummarytype, null, false, false, null, Common.EMPTY_STRING_LIST, "create",
            Common.EMPTY_STRING, SchemaType.BTC_STRING, false, false, false);
        PropertyImpl p_changesummarytype_delete = PropertyImpl.create(STRING, "delete", false,
            false, t_changesummarytype, null, false, false, null, Common.EMPTY_STRING_LIST, "delete",
            Common.EMPTY_STRING, SchemaType.BTC_STRING, false, false, false);
        PropertyImpl p_changesummarytype_logging = PropertyImpl.create(BOOLEAN, "logging", false,
            false, t_changesummarytype, null, false, false, null, Common.EMPTY_STRING_LIST, "logging",
            Common.EMPTY_STRING, SchemaType.BTC_BOOLEAN, false, false, false);
        List<PropertyXML> t_changesummarytype_propList = new ArrayList<PropertyXML>(3);
        t_changesummarytype_propList.add(p_changesummarytype_create);
        t_changesummarytype_propList.add(p_changesummarytype_delete);
        t_changesummarytype_propList.add(p_changesummarytype_logging);
        st = schematypeLoaderForClassLoader.findType(new QName(Names.URI_SDO, "ChangeSummaryType"));
        t_changesummarytype.init("ChangeSummaryType", Names.URI_SDO, TYPECODE_CHANGESUMMARYTYPE,
            ChangeSummary.class, true, false, false, true, false, false, Common.EMPTY_TYPEXML_LIST,
            t_changesummarytype_propList, Common.EMPTY_STRING_LIST, st, bits);
        CHANGESUMMARYTYPE = t_changesummarytype;

        TypeImpl t_modelstype = TypeImpl.create();
        st = schematypeLoaderForClassLoader.findType(new QName(Names.URI_SDO, "ModelsType"));
        t_modelstype.init("ModelsType", Names.URI_SDO, TYPECODE_MODELSTYPE,
            davos.sdo.ModelsType.class, false, true, false, false, false, false,
            Collections.singletonList(DATAOBJECT), Common.EMPTY_PROPERTYXML_LIST,
            Common.EMPTY_STRING_LIST, st, bits);
        MODELSTYPE = t_modelstype;

        TypeImpl t_xsdtype = TypeImpl.create();
        st = schematypeLoaderForClassLoader.findType(new QName(Names.URI_SDO, "XSDType"));
        t_xsdtype.init("XSDType", Names.URI_SDO, TYPECODE_XSDTYPE, davos.sdo.XSDType.class, false,
            true, false, false, false, false, Collections.singletonList(DATAOBJECT),
            Common.EMPTY_PROPERTYXML_LIST, Common.EMPTY_STRING_LIST, st, bits);
        XSDTYPE = t_xsdtype;

        TypeImpl t_basedatagraphtype = TypeImpl.create();
        PropertyImpl p_basedatagraphtype_models = PropertyImpl.create(MODELSTYPE, "models", false,
            true, t_basedatagraphtype, null, false, false, null, Common.EMPTY_STRING_LIST, "models",
            Common.EMPTY_STRING, -1, true, false, false);
        PropertyImpl p_basedatagraphtype_xsd = PropertyImpl.create(XSDTYPE, "xsd", false,
            true, t_basedatagraphtype, null, false, false, null, Common.EMPTY_STRING_LIST, "xsd",
            Common.EMPTY_STRING, -1, true, false, false);
        PropertyImpl p_basedatagraphtype_changesummary = PropertyImpl.create(CHANGESUMMARYTYPE,
            "changeSummary", false, true, t_basedatagraphtype, null, false, false, null,
            Common.EMPTY_STRING_LIST, "changeSummary", Common.EMPTY_STRING, -1, true, false,false);
        List<PropertyXML> t_basedatagraphtype_propList = new ArrayList<PropertyXML>(3);
        t_basedatagraphtype_propList.add(p_basedatagraphtype_models);
        t_basedatagraphtype_propList.add(p_basedatagraphtype_xsd);
        t_basedatagraphtype_propList.add(p_basedatagraphtype_changesummary);
        st = schematypeLoaderForClassLoader.findType(new QName(Names.URI_SDO, "BaseDataGraphType"));
        t_basedatagraphtype.init("BaseDataGraphType", Names.URI_SDO, TYPECODE_BASEDATAGRAPHTYPE,
            davos.sdo.BaseDataGraphType.class, false, false, false, true, false, false,
            Collections.singletonList(DATAOBJECT), t_basedatagraphtype_propList,
            Common.EMPTY_STRING_LIST, st, bits);
        BASEDATAGRAPHTYPE = t_basedatagraphtype;

        TypeImpl t_datagraphtype = TypeImpl.create();
        st = schematypeLoaderForClassLoader.findType(new QName(Names.URI_SDO, "DataGraphType"));
        t_datagraphtype.init("DataGraphType", Names.URI_SDO, TYPECODE_DATAGRAPHTYPE,
            DataObject.class, false, true, false, false, false, false,
            Collections.singletonList(BASEDATAGRAPHTYPE), Common.EMPTY_PROPERTYXML_LIST,
            Common.EMPTY_STRING_LIST, st, bits);
        DATAGRAPHTYPE = t_datagraphtype;

        // TODO(radup) Make the container type be ?@javax.sdo
        P_DATAGRAPH = PropertyImpl.create(DATAGRAPHTYPE, Names.SDO_DATAGRAPH, false, true, null,
            null, false, false, null, Common.EMPTY_STRING_LIST, Names.SDO_DATAGRAPH, Names.URI_SDO, -1,
            true, false, true);
        P_DATAOBJECT = PropertyImpl.create(DATAOBJECT, Names.SDO_DATAOBJECT, false, true, null,
            null, false, false, null, Common.EMPTY_STRING_LIST, Names.SDO_DATAOBJECT, Names.URI_SDO,
            -1, true, false, true);


        TypeImpl t_valuetype = TypeImpl.create();
        List<PropertyXML> t_valuetype_propList = new ArrayList<PropertyXML>();
        st = null;
        t_valuetype.init("ValueType", Names.URI_BEA_TYPES, TYPECODE_VALUETYPE, DataObject.class,
            false, false, false, false, true, false, Collections.singletonList(DATAOBJECT),
            t_valuetype_propList, Common.EMPTY_STRING_LIST, st, bits);
        VALUETYPE = t_valuetype;
        PropertyImpl p_valuetype_value = PropertyImpl.create(OBJECT, "value", false, true,
            VALUETYPE, null, false, false, null, Common.EMPTY_STRING_LIST, "VALUE", null,
            SchemaType.BTC_ANY_SIMPLE, false, false, false);
        P_VALUETYPE_VALUE = p_valuetype_value;
        t_valuetype_propList.add(p_valuetype_value);

        TypeImpl t_wrappertype = TypeImpl.create();
        st = null;
        t_wrappertype.init("WrapperType", Names.URI_BEA_TYPES, TYPECODE_WRAPPERTYPE, DataObject.class,
            false, true, false, false, true, false, Collections.singletonList(DATAOBJECT),
            Common.EMPTY_PROPERTYXML_LIST, Common.EMPTY_STRING_LIST, st, bits);
        WRAPPERTYPE = t_wrappertype;

        TypeImpl t_bea_dataobject = TypeImpl.create();
        st = null;
        t_bea_dataobject.init("DataObject", Names.URI_BEA_TYPES, TYPECODE_BEADATAOBJECT,
            DataObject.class, false, true, true, false, false, true,
            Collections.singletonList(DATAOBJECT), Common.EMPTY_PROPERTYXML_LIST,
            Common.EMPTY_STRING_LIST, st, bits);
        BEADATAOBJECT = t_bea_dataobject;


        TypeImpl t_booleanObject = TypeImpl.create();
        st = schematypeLoaderForClassLoader.findType(new QName(Names.URI_SDOJAVA, "BooleanObject"));
        t_booleanObject.init("BooleanObject", Names.URI_SDOJAVA, TYPECODE_BOOLEANOBJECT, Boolean.class, true, false, false, false, false, false,
            Collections.singletonList(BOOLEAN), Common.EMPTY_PROPERTYXML_LIST, Common.EMPTY_STRING_LIST, st, bits);
        BOOLEANOBJECT = t_booleanObject;

        TypeImpl t_byteObject = TypeImpl.create();
        st = schematypeLoaderForClassLoader.findType(new QName(Names.URI_SDOJAVA, "ByteObject"));
        t_byteObject.init("ByteObject", Names.URI_SDOJAVA, TYPECODE_BYTEOBJECT, Byte.class, true, false, false, false, false, false,
            Collections.singletonList(BYTE), Common.EMPTY_PROPERTYXML_LIST, Common.EMPTY_STRING_LIST, st, bits);
        BYTEOBJECT = t_byteObject;

        TypeImpl t_characterObject = TypeImpl.create();
        st = schematypeLoaderForClassLoader.findType(new QName(Names.URI_SDOJAVA, "CharacterObject"));
        t_characterObject.init("CharacterObject", Names.URI_SDOJAVA, TYPECODE_CHARACTEROBJECT, Character.class, true, false, false, false, false, false,
            Collections.singletonList(CHARACTER), Common.EMPTY_PROPERTYXML_LIST, Common.EMPTY_STRING_LIST, st, bits);
        CHARACTEROBJECT = t_characterObject;

        TypeImpl t_doubleObject = TypeImpl.create();
        st = schematypeLoaderForClassLoader.findType(new QName(Names.URI_SDOJAVA, "DoubleObject"));
        t_doubleObject.init("DoubleObject", Names.URI_SDOJAVA, TYPECODE_DOUBLEOBJECT, Double.class, true, false, false, false, false, false,
            Collections.singletonList(DOUBLE), Common.EMPTY_PROPERTYXML_LIST, Common.EMPTY_STRING_LIST, st, bits);
        DOUBLEOBJECT = t_doubleObject;

        TypeImpl t_floatObject = TypeImpl.create();
        st = schematypeLoaderForClassLoader.findType(new QName(Names.URI_SDOJAVA, "FloatObject"));
        t_floatObject.init("FloatObject", Names.URI_SDOJAVA, TYPECODE_FLOATOBJECT, Float.class, true, false, false, false, false, false,
            Collections.singletonList(FLOAT), Common.EMPTY_PROPERTYXML_LIST, Common.EMPTY_STRING_LIST, st, bits);
        FLOATOBJECT = t_floatObject;

        TypeImpl t_intObject = TypeImpl.create();
        st = schematypeLoaderForClassLoader.findType(new QName(Names.URI_SDOJAVA, "IntObject"));
        t_intObject.init("IntObject", Names.URI_SDOJAVA, TYPECODE_INTOBJECT, Integer.class, true, false, false, false, false, false,
            Collections.singletonList(INT), Common.EMPTY_PROPERTYXML_LIST, Common.EMPTY_STRING_LIST, st, bits);
        INTOBJECT = t_intObject;

        TypeImpl t_longObject = TypeImpl.create();
        st = schematypeLoaderForClassLoader.findType(new QName(Names.URI_SDOJAVA, "LongObject"));
        t_longObject.init("LongObject", Names.URI_SDOJAVA, TYPECODE_LONGOBJECT, Long.class, true, false, false, false, false, false,
            Collections.singletonList(LONG), Common.EMPTY_PROPERTYXML_LIST, Common.EMPTY_STRING_LIST, st, bits);
        LONGOBJECT = t_longObject;

        TypeImpl t_shortObject = TypeImpl.create();
        st = schematypeLoaderForClassLoader.findType(new QName(Names.URI_SDOJAVA, "ShortObject"));
        t_shortObject.init("ShortObject", Names.URI_SDOJAVA, TYPECODE_SHORTOBJECT, Short.class, true, false, false, false, false, false,
            Collections.singletonList(SHORT), Common.EMPTY_PROPERTYXML_LIST, Common.EMPTY_STRING_LIST, st, bits);
        SHORTOBJECT = t_shortObject;


        // since all the types are defined, the remaining properties can be defined
        // properties of TYPE type
        PropertyImpl p_type_basetype = PropertyImpl.create(TYPE, "baseType", true, false, TYPE, null, false, false, null, Common.EMPTY_STRING_LIST,
            "baseType", Names.URI_SDO, -1, true, false, false);
        P_TYPE_BASETYPE = p_type_basetype;

        PropertyImpl p_type_property = PropertyImpl.create(PROPERTY, "property", true, true, TYPE, null, false, false, null, Common.EMPTY_STRING_LIST,
            "property", Names.URI_SDO, -1, true, false, false);
        P_TYPE_PROPERTY = p_type_property;

        PropertyImpl p_type_aliasName = PropertyImpl.create(STRING, "aliasName", true, false, TYPE, null, false, false, null, Common.EMPTY_STRING_LIST,
            "aliasName", Names.URI_SDO, SchemaType.BTC_STRING, true, false, false);
        P_TYPE_ALIASNAME = p_type_aliasName;

        PropertyImpl p_type_name = PropertyImpl.create(STRING, "name", false, false, TYPE, null, false, false, null, Common.EMPTY_STRING_LIST,
            "name", Names.URI_SDO, SchemaType.BTC_ID, false, false, false);
        P_TYPE_NAME = p_type_name;

        PropertyImpl p_type_uri = PropertyImpl.create(STRING, "uri", false, false, TYPE, null, false, false, null, Common.EMPTY_STRING_LIST,
            "uri", Names.URI_SDO, SchemaType.BTC_ANY_URI, false, false, false);
        P_TYPE_URI = p_type_uri;

        PropertyImpl p_type_datatype = PropertyImpl.create(BOOLEAN, "dataType", false, false, TYPE, null, false, false, null, Common.EMPTY_STRING_LIST,
            "dataType", Names.URI_SDO, SchemaType.BTC_BOOLEAN, false, false, false);
        P_TYPE_DATATYPE = p_type_datatype;

        PropertyImpl p_type_open = PropertyImpl.create(BOOLEAN, "open", false, false, TYPE, null, false, false, null, Common.EMPTY_STRING_LIST,
            "open", Names.URI_SDO, SchemaType.BTC_BOOLEAN, false, false, false);
        P_TYPE_OPEN = p_type_open;

        PropertyImpl p_type_sequenced = PropertyImpl.create(BOOLEAN, "sequenced", false, false, TYPE, null, false, false, null, Common.EMPTY_STRING_LIST,
            "sequenced", Names.URI_SDO, SchemaType.BTC_BOOLEAN, false, false, false);
        P_TYPE_SEQUENCED = p_type_sequenced;

        PropertyImpl p_type_abstract = PropertyImpl.create(BOOLEAN, "abstract", false, false, TYPE, null, false, false, null, Common.EMPTY_STRING_LIST,
            "abstract", Names.URI_SDO, SchemaType.BTC_BOOLEAN, false, false, false);
        P_TYPE_ABSTRACT = p_type_abstract;

        // instance property of type "Type"
        P_TYPE_JAVACLASS = PropertyImpl.create(STRING, "javaClass", false, false, TYPE, null, false, false, null,
            Common.EMPTY_STRING_LIST, "javaClass", Names.URI_SDOJAVA, SchemaType.BTC_STRING, false, true, true);

        // properties of TYPES type
        PropertyImpl p_types_type = PropertyImpl.create(TYPE, "type", true, true, TYPES, null, false, false, null, Common.EMPTY_STRING_LIST, 
            "type", Names.URI_SDO, -1, true, false, false);
        P_TYPES_TYPE = p_types_type;


        // properties of PROPERTY type
        PropertyImpl p_property_aliasName = PropertyImpl.create(STRING, "aliasName", true, false, PROPERTY, null, false, false, null, Common.EMPTY_STRING_LIST,
            "aliasName", Names.URI_SDO, SchemaType.BTC_STRING, true, false, false);
        P_PROPERTY_ALIASNAME = p_property_aliasName;

        PropertyImpl p_property_name = PropertyImpl.create(STRING, "name", false, false, PROPERTY, null, false, false, null, Common.EMPTY_STRING_LIST,
            "name", Names.URI_SDO, SchemaType.BTC_STRING, false, false, false);
        P_PROPERTY_NAME = p_property_name;

        PropertyImpl p_property_many = PropertyImpl.create(BOOLEAN, "many", false, false, PROPERTY, null, false, false, null, Common.EMPTY_STRING_LIST,
            "many", Names.URI_SDO, SchemaType.BTC_BOOLEAN, false, false, false);
        P_PROPERTY_MANY = p_property_many;

        PropertyImpl p_property_containment = PropertyImpl.create(BOOLEAN, "containment", false, false, PROPERTY, null, false, false, null, Common.EMPTY_STRING_LIST,
            "containment", Names.URI_SDO, SchemaType.BTC_BOOLEAN, false, false, false);
        P_PROPERTY_CONTAINMENT = p_property_containment;

        PropertyImpl p_property_type = PropertyImpl.create(TYPE, "type", false, false, PROPERTY, null, false, false, null, Common.EMPTY_STRING_LIST,
            "type", Names.URI_SDO, SchemaType.BTC_ANY_URI, false, false, false);
        P_PROPERTY_TYPE = p_property_type;

        PropertyImpl p_property_default = PropertyImpl.create(OBJECT, "default", false, false, PROPERTY, null, false, false, null, Common.EMPTY_STRING_LIST,
            "default", Names.URI_SDO, SchemaType.BTC_STRING, false, false, false);
        P_PROPERTY_DEFAULT = p_property_default;

        PropertyImpl p_property_readOnly = PropertyImpl.create(BOOLEAN, "readOnly", false, false, PROPERTY, null, false, false, null, Common.EMPTY_STRING_LIST,
            "readOnly", Names.URI_SDO, SchemaType.BTC_BOOLEAN, false, false, false);
        P_PROPERTY_READONLY = p_property_readOnly;

        PropertyImpl p_property_opposite = PropertyImpl.create(PROPERTY, "opposite", false, false, PROPERTY, null, false, false, null, Common.EMPTY_STRING_LIST,
            "opposite", Names.URI_SDO, SchemaType.BTC_ANY_URI, false, false, false);
        P_PROPERTY_OPPOSITE = p_property_opposite;

        PropertyImpl p_property_nullable = PropertyImpl.create(BOOLEAN, "nullable", false, false, PROPERTY, null, false, false, null, Common.EMPTY_STRING_LIST,
            "nullable", Names.URI_SDO, SchemaType.BTC_BOOLEAN, false, false, false);
        P_PROPERTY_NULLABLE = p_property_nullable;

        // instance property of type "Property"
        P_PROPERTY_XMLELEMENT = PropertyImpl.create(BOOLEAN, "xmlElement", false, false, PROPERTY, null, false, false, null,
            Common.EMPTY_STRING_LIST, "xmlElement", Names.URI_SDOXML, SchemaType.BTC_BOOLEAN, false, true, true);

        // now that we have the properties we add them to the type's list of properties before makeing the type immutable
        t_type_propList.add(p_type_basetype);
        t_type_propList.add(p_type_property);
        t_type_propList.add(p_type_aliasName);
        t_type_propList.add(p_type_name);
        t_type_propList.add(p_type_uri);
        t_type_propList.add(p_type_datatype);
        t_type_propList.add(p_type_open);
        t_type_propList.add(p_type_sequenced);
        t_type_propList.add(p_type_abstract);

        t_types_propList.add(p_types_type);

        t_property_propList.add(p_property_aliasName);
        t_property_propList.add(p_property_name);
        t_property_propList.add(p_property_many);
        t_property_propList.add(p_property_containment);
        t_property_propList.add(p_property_default);
        t_property_propList.add(p_property_readOnly);
        t_property_propList.add(p_property_type);
        t_property_propList.add(p_property_opposite);
        t_property_propList.add(p_property_nullable);


        // makeImmutable
        t_type.makeImmutable();
        t_types.makeImmutable();
        t_property.makeImmutable();
        t_basedatagraphtype.makeImmutable();
        t_boolean.makeImmutable();
        t_booleanObject.makeImmutable();
        t_bea_dataobject.makeImmutable();
        t_byte.makeImmutable();
        t_byteObject.makeImmutable();
        t_bytes.makeImmutable();
        t_changesummarytype.makeImmutable();
        t_character.makeImmutable();
        t_characterObject.makeImmutable();
        t_datagraphtype.makeImmutable();
        t_dataobject.makeImmutable();
        t_date.makeImmutable();
        t_datetime.makeImmutable();
        t_day.makeImmutable();
        t_decimal.makeImmutable();
        t_double.makeImmutable();
        t_doubleObject.makeImmutable();
        t_duration.makeImmutable();
        t_float.makeImmutable();
        t_floatObject.makeImmutable();
        t_int.makeImmutable();
        t_intObject.makeImmutable();
        t_integer.makeImmutable();
        t_long.makeImmutable();
        t_longObject.makeImmutable();
        t_modelstype.makeImmutable();
        t_month.makeImmutable();
        t_monthday.makeImmutable();
        t_object.makeImmutable();
        t_short.makeImmutable();
        t_shortObject.makeImmutable();
        t_string.makeImmutable();
        t_strings.makeImmutable();
        t_time.makeImmutable();
        t_uri.makeImmutable();
        t_valuetype.makeImmutable();
        t_wrappertype.makeImmutable();
        t_xsdtype.makeImmutable();
        t_year.makeImmutable();
        t_yearmonth.makeImmutable();
        t_yearmonthday.makeImmutable();


        bits.addTypeMapping(TYPE);
        bits.addTypeMapping(TYPES);
        bits.addTypeMapping(PROPERTY);

        bits.addTypeMapping(BOOLEANOBJECT);
        bits.addTypeMapping(BYTEOBJECT);
        bits.addTypeMapping(CHARACTEROBJECT);
        bits.addTypeMapping(DOUBLEOBJECT);
        bits.addTypeMapping(FLOATOBJECT);
        bits.addTypeMapping(INTOBJECT);
        bits.addTypeMapping(LONGOBJECT);
        bits.addTypeMapping(SHORTOBJECT);

        bits.addTypeMapping(BASEDATAGRAPHTYPE);
        bits.addTypeMapping(BOOLEAN);
        bits.addTypeMapping(BYTE);
        bits.addTypeMapping(BYTES);
        bits.addTypeMapping(CHARACTER);
        bits.addTypeMapping(DATAGRAPHTYPE);
        bits.addTypeMapping(DATE);
        bits.addTypeMapping(DATETIME);
        bits.addTypeMapping(DAY);
        bits.addTypeMapping(DECIMAL);
        bits.addTypeMapping(DOUBLE);
        bits.addTypeMapping(DURATION);
        bits.addTypeMapping(FLOAT);
        bits.addTypeMapping(INT);
        bits.addTypeMapping(INTEGER);
        bits.addTypeMapping(LONG);
        bits.addTypeMapping(MODELSTYPE);
        bits.addTypeMapping(MONTH);
        bits.addTypeMapping(MONTHDAY);
        bits.addTypeMapping(SHORT);
        bits.addTypeMapping(STRING);
        bits.addTypeMapping(STRINGS);
        bits.addTypeMapping(TIME);
        bits.addTypeMapping(URI);
        bits.addTypeMapping(XSDTYPE);
        bits.addTypeMapping(YEAR);
        bits.addTypeMapping(YEARMONTH);
        bits.addTypeMapping(YEARMONTHDAY);

        bits.addTypeMapping(OBJECT);
        bits.addTypeMapping(CHANGESUMMARYTYPE);
        bits.addTypeMapping(DATAOBJECT);
        bits.addTypeMapping(VALUETYPE);
        bits.addTypeMapping(BEADATAOBJECT);
        bits.addTypeMapping(WRAPPERTYPE);

        // Add mappings for the remaining Schema types
        bits.addSpecialTypeMapping(Names.URI_XSD, "ENTITIES", t_strings);
        bits.addSpecialTypeMapping(Names.URI_XSD, "ENTITY", t_string);
        bits.addSpecialTypeMapping(Names.URI_XSD, "base64Binary", t_bytes);
        bits.addSpecialTypeMapping(Names.URI_XSD, "ID", t_string);
        bits.addSpecialTypeMapping(Names.URI_XSD, "IDREF", t_string);
        bits.addSpecialTypeMapping(Names.URI_XSD, "IDREFS", t_strings);
        bits.addSpecialTypeMapping(Names.URI_XSD, "language", t_string);
        bits.addSpecialTypeMapping(Names.URI_XSD, "Name", t_string);
        bits.addSpecialTypeMapping(Names.URI_XSD, "NCName", t_string);
        bits.addSpecialTypeMapping(Names.URI_XSD, "negativeInteger", t_integer);
        bits.addSpecialTypeMapping(Names.URI_XSD, "NMTOKEN", t_string);
        bits.addSpecialTypeMapping(Names.URI_XSD, "nonNegativeInteger", t_integer);
        bits.addSpecialTypeMapping(Names.URI_XSD, "nonPositiveInteger", t_integer);
        bits.addSpecialTypeMapping(Names.URI_XSD, "normalizedString", t_string);
        bits.addSpecialTypeMapping(Names.URI_XSD, "NOTATION", t_string);
        bits.addSpecialTypeMapping(Names.URI_XSD, "positiveInteger", t_integer);
        bits.addSpecialTypeMapping(Names.URI_XSD, "QName", t_uri);
        bits.addSpecialTypeMapping(Names.URI_XSD, "token", t_string);
        bits.addSpecialTypeMapping(Names.URI_XSD, "unsignedByte", t_short);
        bits.addSpecialTypeMapping(Names.URI_XSD, "unsignedInt", t_long);
        bits.addSpecialTypeMapping(Names.URI_XSD, "unsignedLong", t_integer);
        bits.addSpecialTypeMapping(Names.URI_XSD, "unsignedShort", t_int);
        bits.addSpecialTypeMapping(Names.URI_SDO, "ChangeSummaryType", t_changesummarytype);

        // Add mappings for the SDO Schema types
        // TODO(radup) what to do about the javax.sdo/Types
        // The comment in the XSD says it is not part of the model, but then why it's there?
        bits.addSpecialTypeMapping(Names.URI_SDO, "DataObject", t_dataobject);
        bits.addSpecialTypeMapping(Names.URI_SDO, "Boolean", t_boolean);
        bits.addSpecialTypeMapping(Names.URI_SDO, "Byte", t_byte);
        bits.addSpecialTypeMapping(Names.URI_SDO, "Bytes", t_bytes);
        bits.addSpecialTypeMapping(Names.URI_SDO, "Character", t_character);
        bits.addSpecialTypeMapping(Names.URI_SDO, "Date", t_date);
        bits.addSpecialTypeMapping(Names.URI_SDO, "DateTime", t_datetime);
        bits.addSpecialTypeMapping(Names.URI_SDO, "Day", t_day);
        bits.addSpecialTypeMapping(Names.URI_SDO, "Decimal", t_decimal);
        bits.addSpecialTypeMapping(Names.URI_SDO, "Double", t_double);
        bits.addSpecialTypeMapping(Names.URI_SDO, "Duration", t_duration);
        bits.addSpecialTypeMapping(Names.URI_SDO, "Float", t_float);
        bits.addSpecialTypeMapping(Names.URI_SDO, "Int", t_int);
        bits.addSpecialTypeMapping(Names.URI_SDO, "Integer", t_integer);
        bits.addSpecialTypeMapping(Names.URI_SDO, "Long", t_long);
        bits.addSpecialTypeMapping(Names.URI_SDO, "Month", t_month);
        bits.addSpecialTypeMapping(Names.URI_SDO, "MonthDay", t_monthday);
        bits.addSpecialTypeMapping(Names.URI_SDO, "Object", t_object);
        bits.addSpecialTypeMapping(Names.URI_SDO, "Short", t_short);
        bits.addSpecialTypeMapping(Names.URI_SDO, "String", t_string);
        bits.addSpecialTypeMapping(Names.URI_SDO, "Strings", t_strings);
        bits.addSpecialTypeMapping(Names.URI_SDO, "Time", t_time);
        bits.addSpecialTypeMapping(Names.URI_SDO, "Year", t_year);
        bits.addSpecialTypeMapping(Names.URI_SDO, "YearMonth", t_yearmonth);
        bits.addSpecialTypeMapping(Names.URI_SDO, "YearMonthDay", t_yearmonthday);
        bits.addSpecialTypeMapping(Names.URI_SDO, "URI", t_uri);

        bits.addGlobalProperty(P_TYPE);
        bits.addGlobalProperty(P_TYPES);
        bits.addGlobalProperty(P_DATAGRAPH);
        bits.addGlobalProperty(P_DATAOBJECT);
        bits.addGlobalProperty(P_TYPE_JAVACLASS);
        bits.addGlobalProperty(P_PROPERTY_XMLELEMENT);
        INSTANCE = bits;

        // setup instnce class -> Type Map
        INSTANCECLASSTOTYPE = new HashMap<Class, TypeXML>();
        INSTANCECLASSTOTYPE.put(BASEDATAGRAPHTYPE.getInstanceClass() , BASEDATAGRAPHTYPE);      // davos.sdo.BaseDataGraphType.class
        INSTANCECLASSTOTYPE.put(BOOLEAN.getInstanceClass() , BOOLEAN);                          // boolean.class
        INSTANCECLASSTOTYPE.put(BOOLEANOBJECT.getInstanceClass() , BOOLEANOBJECT);              // Boolean.class
        INSTANCECLASSTOTYPE.put(BYTE.getInstanceClass() , BYTE);                                // byte.class
        INSTANCECLASSTOTYPE.put(BYTEOBJECT.getInstanceClass() , BYTEOBJECT);                    // Byte.class
        INSTANCECLASSTOTYPE.put(BYTES.getInstanceClass() , BYTES);                              // byte[].class
        INSTANCECLASSTOTYPE.put(CHANGESUMMARYTYPE.getInstanceClass() , CHANGESUMMARYTYPE);      // ChangeSummary.class
        INSTANCECLASSTOTYPE.put(CHARACTER.getInstanceClass() , CHARACTER);                      // char.class
        INSTANCECLASSTOTYPE.put(CHARACTEROBJECT.getInstanceClass() , CHARACTEROBJECT);          // Character.class
        INSTANCECLASSTOTYPE.put(DATAOBJECT.getInstanceClass() , DATAOBJECT);                    // DataObject.class
        //INSTANCECLASSTOTYPE.put(DATAGRAPHTYPE.getInstanceClass() , DATAGRAPHTYPE);              // DataObject.class
        //INSTANCECLASSTOTYPE.put(VALUETYPE.getInstanceClass() , VALUETYPE);                      // DataObject.class
        //INSTANCECLASSTOTYPE.put(BEADATAOBJECT.getInstanceClass() , BEADATAOBJECT);              // DataObject.class
        INSTANCECLASSTOTYPE.put(DATE.getInstanceClass() , DATE);                                // java.util.Date.class
        INSTANCECLASSTOTYPE.put(DECIMAL.getInstanceClass() , DECIMAL);                          // java.math.BigDecimal.class
        INSTANCECLASSTOTYPE.put(DOUBLE.getInstanceClass() , DOUBLE);                            // double.class
        INSTANCECLASSTOTYPE.put(DOUBLEOBJECT.getInstanceClass() , DOUBLEOBJECT);                // Double.class
        INSTANCECLASSTOTYPE.put(FLOAT.getInstanceClass() , FLOAT);                              // float.class
        INSTANCECLASSTOTYPE.put(FLOATOBJECT.getInstanceClass() , FLOATOBJECT);                  // Float.class
        INSTANCECLASSTOTYPE.put(INT.getInstanceClass() , INT);                                  // int.class
        INSTANCECLASSTOTYPE.put(INTEGER.getInstanceClass() , INTEGER);                          // java.math.BigInteger.class
        INSTANCECLASSTOTYPE.put(INTOBJECT.getInstanceClass() , INTOBJECT);                      // Integer.class
        INSTANCECLASSTOTYPE.put(LONG.getInstanceClass() , LONG);                                // long.class
        INSTANCECLASSTOTYPE.put(LONGOBJECT.getInstanceClass() , LONGOBJECT);                    // Long.class
        INSTANCECLASSTOTYPE.put(MODELSTYPE.getInstanceClass() , MODELSTYPE);                    // davos.sdo.ModelsType.class
        INSTANCECLASSTOTYPE.put(OBJECT.getInstanceClass() , OBJECT);                            // Object.class
        INSTANCECLASSTOTYPE.put(SHORT.getInstanceClass() , SHORT);                              // short.class
        INSTANCECLASSTOTYPE.put(SHORTOBJECT.getInstanceClass() , SHORTOBJECT);                  // Short.class
        INSTANCECLASSTOTYPE.put(STRING.getInstanceClass() , STRING);                            // String.class
        //INSTANCECLASSTOTYPE.put(DATETIME.getInstanceClass() , DATETIME);                        // String.class
        //INSTANCECLASSTOTYPE.put(DAY.getInstanceClass() , DAY);                                  // String.class
        //INSTANCECLASSTOTYPE.put(DURATION.getInstanceClass() , DURATION);                        // String.class
        //INSTANCECLASSTOTYPE.put(MONTH.getInstanceClass() , MONTH);                              // String.class
        //INSTANCECLASSTOTYPE.put(MONTHDAY.getInstanceClass() , MONTHDAY);                        // String.class
        //INSTANCECLASSTOTYPE.put(TIME.getInstanceClass() , TIME);                                // String.class
        //INSTANCECLASSTOTYPE.put(YEAR.getInstanceClass() , YEAR);                                // String.class
        //INSTANCECLASSTOTYPE.put(YEARMONTH.getInstanceClass() , YEARMONTH);                      // String.class
        //INSTANCECLASSTOTYPE.put(YEARMONTHDAY.getInstanceClass() , YEARMONTHDAY);                // String.class
        //INSTANCECLASSTOTYPE.put(URI.getInstanceClass() , URI);                                  // String.class
        //INSTANCECLASSTOTYPE.put(TEXTTYPE.getInstanceClass() , TEXTTYPE);
        INSTANCECLASSTOTYPE.put(STRINGS.getInstanceClass() , STRINGS);                          // List.class
        INSTANCECLASSTOTYPE.put(TYPE.getInstanceClass() , TYPE);                                // TypeImpl.class
        //INSTANCECLASSTOTYPE.put(PROPERTY.getInstanceClass() , PROPERTY);                        // TypeImpl.class
        INSTANCECLASSTOTYPE.put(XSDTYPE.getInstanceClass() , XSDTYPE);                          // davos.sdo.XSDType.class
    }

    private BuiltInTypeSystem()
    {
    }

    public static TypeSystem getInstance()
    {
        return INSTANCE;
    }
}
