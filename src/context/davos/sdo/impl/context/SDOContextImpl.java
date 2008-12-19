package davos.sdo.impl.context;

import davos.sdo.SDOContext;
import davos.sdo.type.TypeSystem;
import davos.sdo.impl.type.TypeHelperImpl;
import davos.sdo.impl.type.TypeSystemBase;
import davos.sdo.impl.type.BuiltInTypeSystem;
import davos.sdo.impl.data.DataFactoryImpl;
import davos.sdo.impl.data.ObjectInputStreamImpl;
import davos.sdo.impl.binding.DynamicBindingSystem;
import davos.sdo.impl.binding.DynamicBindingSystemForSchemaTypeLoader;
import davos.sdo.impl.helpers.*;
import davos.sdo.binding.BindingSystem;
import javax.sdo.helper.*;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.XmlBeans;

import java.io.ObjectInputStream;
import java.io.InputStream;
import java.io.IOException;

/**
 * Created by Cezar Andrei (cezar dot andrei at gmail dot com)
 * Date: Nov 18, 2008
 */
public class SDOContextImpl
    implements SDOContext, HelperContext
{
    private CopyHelper _copyHelper;
    private DataFactory _dataFactory;
    private DataHelper _dataHelper;
    private EqualityHelper _equalityHelper;
    private TypeHelper      _typeHelper;
    private XMLHelper       _xmlHelper;
    private XSDHelper       _xsdHelper;

    private ClassLoader     _classLoader;
    private BindingSystem _bindingSystem;

    public SDOContextImpl()
    {
        _classLoader     = initClassLoader();
        _bindingSystem   = initBindingSystem();
        _copyHelper      = new CopyHelperImpl(this);
        _dataHelper      = new DataHelperImpl(this);
        _equalityHelper  = new EqualityHelperImpl(this);
        _typeHelper      = new TypeHelperImpl(this);
        _xmlHelper       = new XMLHelperImpl(this);
        _xsdHelper       = new XSDHelperImpl(this);
        _dataFactory     = new DataFactoryImpl(this);
    }

    private SDOContextImpl(ClassLoader classLoader)
    {
        if (classLoader==null)
            classLoader = new EmptyClassLoader();

        _classLoader = classLoader;
        _bindingSystem   = initBindingSystem();
        _copyHelper      = new CopyHelperImpl(this);
        _dataHelper      = new DataHelperImpl(this);
        _equalityHelper  = new EqualityHelperImpl(this);
        _typeHelper      = new TypeHelperImpl(this);
        _xmlHelper       = new XMLHelperImpl(this);
        _xsdHelper       = new XSDHelperImpl(this);
        _dataFactory     = new DataFactoryImpl(this);
    }

    protected ClassLoader initClassLoader()
    {
        return Thread.currentThread().getContextClassLoader();
    }

    protected BindingSystem initBindingSystem()
    {
        TypeSystemBase tsb = TypeSystemBase.createEmptyTypeSystem();
        tsb.addTypeSystem(BuiltInTypeSystem.INSTANCE, false);
        SchemaTypeLoader stl = XmlBeans.typeLoaderUnion(new SchemaTypeLoader[]
            {   XmlBeans.getBuiltinTypeSystem(),
                XmlBeans.typeSystemForClassLoader(this.getClass().getClassLoader(), "schemaorg_apache_xmlbeans.system.sSDOSCHEMAS"),
                XmlBeans.typeLoaderForClassLoader(_classLoader)});
        tsb.setSchemaTypeLoader(stl);
        tsb.setClassLoader(_classLoader);

        return new DynamicBindingSystem(this, _classLoader,
            tsb  /*BuiltInTypeSystem.INSTANCE*/);
    }


    public CopyHelper getCopyHelper()
    {
        return _copyHelper;
    }

    public DataFactory getDataFactory()
    {
        return _dataFactory;
    }

    public DataHelper getDataHelper()
    {
        return _dataHelper;
    }

    public EqualityHelper getEqualityHelper()
    {
        return _equalityHelper;
    }

    public TypeHelper getTypeHelper()
    {
        return _typeHelper;
    }

    public XMLHelper getXMLHelper()
    {
        return _xmlHelper;
    }

    public XSDHelper getXSDHelper()
    {
        return _xsdHelper;
    }

    public BindingSystem getBindingSystem()
    {
        return _bindingSystem;
    }

    public TypeSystem getTypeSystem()
    {
        return _bindingSystem.getTypeSystem();
    }

    public ClassLoader getClassLoader()
    {
        return _classLoader;
    }

    public ObjectInputStream createObjectInputStream(InputStream inputStream)
        throws IOException
    {
        ObjectInputStreamImpl ois = new ObjectInputStreamImpl(inputStream);
        ois.setSDOContext(this);
        return ois;
    }

    public static SDOContext newInstance()
    {
        return new SDOContextImpl();
    }

    public static SDOContext newInstance(ClassLoader classLoader)
    {
        return new SDOContextImpl(classLoader);
    }


    public static class SDOContextForSchemaTypeLoader
        extends SDOContextImpl
        implements SDOContext, HelperContext
    {
        SDOContextForSchemaTypeLoader(SchemaTypeLoader schemaTypeLoader, Object options)
        {
            super();
            SchemaTypeLoader stl = XmlBeans.typeLoaderUnion(new SchemaTypeLoader[]
                {   XmlBeans.getBuiltinTypeSystem(),
                    XmlBeans.typeSystemForClassLoader(this.getClass().getClassLoader(), "schemaorg_apache_xmlbeans.system.sSDOSCHEMAS"),
                    schemaTypeLoader});
            DynamicBindingSystemForSchemaTypeLoader dyn = (DynamicBindingSystemForSchemaTypeLoader)getBindingSystem();
            dyn.setSchemaTypeLoader(stl);
            dyn.setOptions(options);
            TypeSystemBase tsb = (TypeSystemBase)((DynamicBindingSystemForSchemaTypeLoader)getBindingSystem()).getTypeSystem();
            tsb.setSchemaTypeLoader(stl);
        }

        protected BindingSystem initBindingSystem()
        {
            TypeSystemBase tsb = TypeSystemBase.createEmptyTypeSystem();
            tsb.addTypeSystem(BuiltInTypeSystem.INSTANCE, false);
            //this is set after in the constructor just ahead

            return new DynamicBindingSystemForSchemaTypeLoader(this,
                Thread.currentThread().getContextClassLoader(), tsb);
        }

        public static SDOContext newInstance(SchemaTypeLoader loader, Object options)
        {
            return new SDOContextForSchemaTypeLoader(loader, options);
        }
    }

    private static class EmptyClassLoader
        extends ClassLoader
    {
        private EmptyClassLoader()
        {
            super(null);
        }

        private EmptyClassLoader(ClassLoader parent)
        {
            super(parent);
        }

        protected synchronized Class<?> loadClass(String name, boolean resolve)
            throws ClassNotFoundException
        {
            throw new ClassNotFoundException(name);
        }

        protected Class<?> findClass(String name)
            throws ClassNotFoundException
        {
            throw new ClassNotFoundException(name);
        }
    }
}
