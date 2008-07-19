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

import javax.sdo.Property;
import javax.sdo.Type;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import davos.sdo.impl.common.Common;
import davos.sdo.impl.util.PropertyMap;
import davos.sdo.TypeXML;
import davos.sdo.PropertyXML;
import davos.sdo.PropertyMapEntry;
import davos.sdo.DataObjectXML;
import davos.sdo.type.TypeSystem;
import org.apache.xmlbeans.SchemaType;

import javax.xml.namespace.QName;

/**
 * @author Cezar Andrei (cezar dot andrei at gmail dot com)
 *         Date: Jan 6, 2006
 */
public class TypeImpl
    extends MetadataHolder
    implements Type, TypeXML
{
    private String _name;
    private String _uri;
    private Class _instanceClass;
    private boolean _isDatatype;
    private boolean _isOpen;
    private boolean _isSequenced;
    private boolean _isAbstract;
    private boolean _isSimpleContent;
    private boolean _isMixedContent;
    private List<TypeXML> _baseTypes;
    private List<PropertyXML> _declaredProperties;
    private List<String> _aliasNames;

    private TypeXML _listItemType;
    private boolean _hasCustomizedInstanceClass;
    private int _typeCode;

    private Map<String, PropertyXML> _nameToProperty;
    private PropertyMap _elmXmlNameToPropertyWithSubst;
    private PropertyMap _attXmlNameToPropertyWithSubst;
    private List<PropertyXML> _properties;

    private SchemaType _schemaType;
    private String _schemaTypeSignature;    // all schema types have a signature
    private QName _schemaTypeName;          // only nonanonymous schema types have names

    private TypeSystem _sdoTypeSystem;

    private TypeImpl() {}

    public static TypeImpl create()
    {
        return new TypeImpl();
    }

    public void init(String name, String uri, SchemaType schemaType, TypeSystem typeSystem)
    {
        assert name != null;
        _name = name;
        _uri = uri == null ? "" : uri;

        assert schemaType!=null;
        _schemaType = schemaType;
        _schemaTypeSignature = schemaType.toString();
        _schemaTypeName = schemaType.getName();

        _sdoTypeSystem = typeSystem;
    }

    public void init(String name, String uri, int typeCode, Class instanceClass, boolean isDatatype, boolean isOpen,
        boolean isSequenced, boolean isAbstract, boolean isSimpleContent, boolean isMixedContent, List<TypeXML> baseTypes,
        List<PropertyXML> declaredProperties, List<String> aliasNames, SchemaType schemaType, TypeSystem sdoTypeSystem)
    {
        init(name, uri, typeCode, instanceClass, isDatatype, isOpen, isSequenced, isAbstract, isSimpleContent,
                isMixedContent, baseTypes, declaredProperties, aliasNames, schemaType, sdoTypeSystem, null, false);
    }

    public void init(String name, String uri, int typeCode, Class instanceClass, boolean isDatatype, boolean isOpen,
        boolean isSequenced, boolean isAbstract, boolean isSimpleContent, boolean isMixedContent, List<TypeXML> baseTypes,
        List<PropertyXML> declaredProperties, List<String> aliasNames, SchemaType schemaType, TypeSystem sdoTypeSystem,
        TypeXML listItemType, boolean hasCustomizedInstanceClass)
    {
        assert name!=null;
        assert declaredProperties!=null;
        assert aliasNames!=null;
        //assert !isDatatype || (isDatatype && instanceClass!=null) : "Spec page 38. instanceClass: " + instanceClass;

        _name = name;
        _uri = uri == null ? "" : uri;
        _instanceClass = instanceClass;
        _isDatatype = isDatatype;
        _isOpen = isOpen;
        _isSequenced = isSequenced;
        _isAbstract = isAbstract;
        _isSimpleContent = isSimpleContent;
        _isMixedContent = isMixedContent;
        _baseTypes = baseTypes;
        _declaredProperties = declaredProperties;
        _aliasNames = aliasNames;
        _typeCode = typeCode;
        if (schemaType != null)
        {
            _schemaType = schemaType;
            _schemaTypeSignature = schemaType.toString();
            _schemaTypeName = schemaType.getName();
        }
        _sdoTypeSystem = sdoTypeSystem;

        if (_isDatatype && _instanceClass!=null)
            addPropertyValue(BuiltInTypeSystem.P_TYPE_JAVACLASS, _instanceClass.getName());

        _listItemType = listItemType;
        _hasCustomizedInstanceClass = hasCustomizedInstanceClass;
    }

    public void addResolveInfo(int typeCode, Class instanceClass, boolean isDatatype, boolean isOpen,
        boolean isSequenced, boolean isAbstract, boolean isSimpleContent, boolean isMixedContent,
        List<TypeXML> baseTypes, List<PropertyXML> declaredProperties, List<String> aliasNames,
        TypeXML listItemType, boolean hasCustomizedInstanceClass)
    {
        assert declaredProperties!=null;
        assert aliasNames!=null;
        // You can have dataTypes represented by user-defined classes not available at compile-time
        _instanceClass = instanceClass;
        _isDatatype = isDatatype;
        _isOpen = isOpen;
        _isSequenced = isSequenced;
        _isAbstract = isAbstract;
        _isSimpleContent = isSimpleContent;
        _isMixedContent = isMixedContent;
        _baseTypes = baseTypes;
        _declaredProperties = declaredProperties;
        _aliasNames = aliasNames;
        _typeCode = typeCode;
        _listItemType = listItemType;
        _hasCustomizedInstanceClass = hasCustomizedInstanceClass;
    }

    public void makeImmutable()
    {
        // all dataType types must have a javaClass property
        if (_isDatatype && _instanceClass!=null && get(BuiltInTypeSystem.P_TYPE_JAVACLASS)==null )
            addPropertyValue(BuiltInTypeSystem.P_TYPE_JAVACLASS, _instanceClass.getName());

        super.makeImmutable();
        
        _declaredProperties = Collections.unmodifiableList(_declaredProperties);
        _aliasNames = Collections.unmodifiableList(_aliasNames);
        _baseTypes = Collections.unmodifiableList(_baseTypes);

        //todo it seems that _properties field can be avoided not sure if would improve perf
        _properties = new ArrayList<PropertyXML>();

        for (TypeXML baseType : _baseTypes)
        {
            assert baseType!=null;
            List<PropertyXML> baseProps = (List<PropertyXML>)baseType.getProperties();
            if (baseProps==null)
            {
                // the base type is not yet immutable
                ((TypeImpl)baseType).makeImmutable();

                baseProps = (List<PropertyXML>)baseType.getProperties();
                if (baseProps==null)
                {
                    throw new IllegalStateException();
                }
            }
            _properties.addAll(baseProps);
        }

        _properties.addAll(_declaredProperties);

        if (_properties.size()==0)
            _properties = Common.EMPTY_PROPERTYXML_LIST;


        _nameToProperty = new HashMap<String, PropertyXML>();

        _elmXmlNameToPropertyWithSubst = new PropertyMap();
        _attXmlNameToPropertyWithSubst = new PropertyMap();

        for (PropertyXML prop : _properties)
        {
            assert _nameToProperty.get(prop.getName())==null : "";
            _nameToProperty.put(prop.getName(), prop);

            for (String aliasName : (List<String>)prop.getAliasNames())
            {
                assert _nameToProperty.get(aliasName)==null : "";
                _nameToProperty.put(aliasName, prop);
            }

            // index by xmlnames including substitutions
            PropertyXML[] substitutions = prop.getAcceptedSubstitutions();
            if (prop.isXMLElement())
            {
                if (substitutions==null || substitutions.length == 0)
                    _elmXmlNameToPropertyWithSubst.putPropertyMapEntry(prop.getXMLNamespaceURI(), prop.getXMLName(), prop, prop);
                else
                    for (int i = 0; i < substitutions.length; i++)
                        _elmXmlNameToPropertyWithSubst.putPropertyMapEntry(substitutions[i].getXMLNamespaceURI(),
                            substitutions[i].getXMLName(), prop, substitutions[i]);
            }
            else
            {
                if (substitutions==null || substitutions.length == 0)
                    _attXmlNameToPropertyWithSubst.putPropertyMapEntry(prop.getXMLNamespaceURI(), prop.getXMLName(), prop, prop);
                else
                    for (int i = 0; i < substitutions.length; i++)
                        _attXmlNameToPropertyWithSubst.putPropertyMapEntry(substitutions[i].getXMLNamespaceURI(),
                            substitutions[i].getXMLName(), prop, substitutions[i]);
            }
        }
    }


    public String getName()
    {
        return _name;
    }

    public String getURI()
    {
        return "".equals(_uri ) ? null : _uri;
    }

    public Class getInstanceClass()
    {
        //if (_instanceClass==null)
        //    _instanceClass = SDOContextFactory.getDefaultBindingSystem().getInstanceClassForType(this);
        return _instanceClass;
    }

    public boolean isInstance(Object object)
    {
        if ( object instanceof DataObjectXML)
        {
            DataObjectXML dObj = (DataObjectXML)object;
            return this.isAssignableFrom(dObj.getTypeXML());
        }
        else
        {
            Class instanceClass = getInstanceClass();
            if ( instanceClass!=null )
                return instanceClass.isInstance(object);
            else
                    return false;
        }
    }

    public List /*Property*/ getProperties()
    {
        return getPropertiesXML();
    }

    public List<PropertyXML> getPropertiesXML()
    {
        return _properties;
    }

    public Property getProperty(String propertyName)
    {
        return getPropertyXML(propertyName);
    }

    public PropertyXML getPropertyXML(String propertyName)
    {
        return _nameToProperty.get(propertyName);
    }

    public PropertyXML getPropertyXMLByXmlName(String uri, String name, boolean isElement)
    {
//        if (isElement)
//            return _elmXmlNameToProperty.get(new QName(uri, name));
//        else
//            return _attXmlNameToProperty.get(new QName(uri, name));
        PropertyMapEntry pme = getPropertyMapEntryByXmlName(uri, name, isElement);
        if (pme!=null)
            return pme.getProperty();

        return null;
    }

    public PropertyMapEntry getPropertyMapEntryByXmlName(String uri, String name, boolean isElement)
    {
        if (isElement)
            return _elmXmlNameToPropertyWithSubst.getPropertyMapEntry(uri, name);
        else
            return _attXmlNameToPropertyWithSubst.getPropertyMapEntry(uri, name);
    }

    public boolean isDataType()
    {
        return _isDatatype;
    }

    public boolean isOpen()
    {
        return _isOpen;
    }

    public boolean isSequenced()
    {
        return _isSequenced;
    }

    public boolean isAbstract()
    {
        return _isAbstract;
    }

    public boolean isSimpleContent()
    {
        return _isSimpleContent;
    }
    
    public boolean isMixedContent()
    {
        return _isMixedContent;
    }

    public List /*Type*/ getBaseTypes()
    {
        return _baseTypes;
    }

    public List /*Property*/ getDeclaredProperties()
    {
        return getDeclaredPropertiesXML();
    }

    public List<PropertyXML> getDeclaredPropertiesXML()
    {
        return _declaredProperties;
    }

    public List /*String*/ getAliasNames()
    {
        return _aliasNames;
    }

    public String toString()
    {
        return "sdoType " + _name + "@" + _uri;
    }

    public void dump()
    {
        System.out.print("Type: " + _name + "@" + _uri);

        if (_isDatatype)
            System.out.print(" datatype");
        if (_isOpen)
            System.out.print(" open");
        if (_isSequenced)
            System.out.print(" sequenced");
        if (_isAbstract)
            System.out.print(" abstract");

        if (_instanceClass!=null)
            System.out.print(" " + _instanceClass.getName());

        System.out.print("\n  basetypes:");
        for (Type baseType : _baseTypes)
        {
            System.out.print(" " + baseType);
        }

        System.out.print("\n  declProp:");
        for (Property p : _declaredProperties)
        {
            System.out.print("\n    " + p);
        }

        System.out.print("\n  aliasNames:");
        for (String aliasName : _aliasNames)
        {
            System.out.print(" " + aliasName);
        }

        System.out.println("");
    }

    public boolean isAssignableFrom(TypeXML typeXml)
    {
        if (equals(typeXml))
            return true;

        if (this == BuiltInTypeSystem.OBJECT)
            return typeXml.isDataType();

        List<Type> baseTypes = new ArrayList<Type>();
        baseTypes.addAll((List<Type>)typeXml.getBaseTypes());

        for (int i = 0; i < baseTypes.size(); i++)
        {
            Type base = baseTypes.get(i);
            if (equals(base))
                return true;
            else
                baseTypes.addAll((List<Type>)base.getBaseTypes());
        }
        return false;
    }

    public SchemaType getXMLSchemaType()
    {
        if (_schemaType!=null)
            return _schemaType;

        if (_schemaTypeSignature!=null && _sdoTypeSystem!=null && _sdoTypeSystem.getSchemaTypeLoader()!=null)
            _schemaType = _sdoTypeSystem.getSchemaTypeLoader().typeForSignature(_schemaTypeSignature);

        return _schemaType;
    }

    public String getXMLSchemaTypeSignature()
    {
        return _schemaTypeSignature;
    }

    public QName getXMLSchemaTypeName()
    {
        return _schemaTypeName;
    }

    void setXMLSchemaType(String schemaTypeSignature, QName schemaTypeName)
    {
        _schemaTypeSignature = schemaTypeSignature;
        _schemaTypeName = schemaTypeName;
    }

    public int getTypeCode()
    {
        return _typeCode;
    }

    public TypeXML getListItemType()
    {
        return _listItemType;
    }

    public boolean hasCustomizedInstanceClass()
    {
        return _hasCustomizedInstanceClass;
    }

    /** Maps to a primitive java type? */
    public boolean isPrimitive()
    {
        return _instanceClass!=null && _instanceClass.isPrimitive();
    }

    /** Is one of the types defined in the SDO spec */
    public boolean isBuiltinType()
    {
        return _typeCode > 0;
    }

    public TypeSystem getSDOTypeSystem()
    {
        return _sdoTypeSystem;
    }

    public int hashCode()
    {
        int hash =0;
        hash += _name.hashCode();
        hash += _uri.hashCode();
        hash += _instanceClass==null ? 0 : _instanceClass.hashCode();
        hash += _isDatatype ? 3 : 0;
        hash += _isOpen ? 5 : 0;
        hash += _isSequenced ? 11 : 0;
        hash += _isAbstract ? 13 : 0;
        hash += _isSimpleContent ? 17 : 0;
        hash += _isMixedContent ? 19 : 0;
        // Not include these in the hash because hashCode() for Arrays is slow
//        hash += _baseTypes.hashCode();
//        hash += _declaredProperties.hashCode();
//        hash += _aliasNames.hashCode();
        hash += _listItemType==null ? 0 : _listItemType.hashCode();
        hash += _hasCustomizedInstanceClass ? 23 : 0;
        hash += _typeCode;
        hash += _schemaTypeSignature==null ? 0 : _schemaTypeSignature.hashCode();

        return hash;
    }
}
