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
package common;

import java.io.*;
import java.util.*;
import java.net.URL;

import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.SchemaTypeSystem;
import org.apache.xmlbeans.XmlObject;

/**
 * @author Wing Yew Poon
 */
public class SchemaTypeLoaderFactory
{
    public static String BASEDIR = getBaseDir();
    public static final String S = File.separator;
    public static String RESOURCES =
        BASEDIR + S + "test" + S + "resources";
    private static String getBaseDir()
    {
        String basedir = System.getProperty("sdo.root");
        if (basedir == null)
            return new File("..").getAbsolutePath();
        else
            return new File(basedir).getAbsolutePath();
    }
    public static File getResourceFile(String dirname, String filename)
    {
        // this assumes that the resources directory is in the classpath
        URL resource = SchemaTypeLoaderFactory.class.getResource("/" + dirname + "/" + filename);
        if (resource != null)
        {
            return new File(resource.getPath());
        }
        // fallback in case the resources directory is not in the classpath
        else
        {
            return new File(RESOURCES + S + dirname, filename);
        }
    }

    private static File[] schemaDirs = {
        getResourceFile("sdocomp", "customers/cbv"),
        getResourceFile("data", "po"),
        getResourceFile("data", "seq")
    };

    private static File[] schemaFiles = {
        // compiletime.schema.files
        getResourceFile("checkin", "IPO.xsd"),
        getResourceFile("checkin", "simple1.xsd"),
        getResourceFile("checkin", "simple3.xsd"),
        getResourceFile("checkin", "employees.xsd"),
        getResourceFile("checkin", "anontype.xsd"),
        getResourceFile("data", "basic.xsd"),
        getResourceFile("data", "company5.xsd"),
        getResourceFile("type", "anil.xsd"),
        getResourceFile("type", "derivation.xsd"),
        getResourceFile("type", "substitution.xsd"),
        // runtime.schema.files
        getResourceFile("checkin", "company_with_cs.xsd"),
        getResourceFile("checkin", "company.xsd"),
        getResourceFile("checkin", "company2.xsd"),
        getResourceFile("checkin", "QName.xsd"),
        getResourceFile("checkin", "substGroup.xsd"),
        getResourceFile("data", "company3a.xsd"),
        getResourceFile("data", "company3b.xsd"),
        getResourceFile("data", "company4a.xsd"),
        getResourceFile("data", "company4b.xsd"),
        getResourceFile("data", "letter.xsd"),
        getResourceFile("data", "copy1.xsd"),
        getResourceFile("data", "copy2.xsd"),
        getResourceFile("data", "copy3a.xsd"),
        getResourceFile("data", "copy3b.xsd"),
        getResourceFile("data", "copy4.xsd"),
        getResourceFile("data", "basic0.xsd"),
        getResourceFile("data", "custom.xsd"),
        getResourceFile("data", "employee.xsd"),
        getResourceFile("data", "sdocs4.xsd"),
        getResourceFile("data", "sdocs5.xsd"),
        getResourceFile("data", "sdo_lds003b.xsd"),
        getResourceFile("marshal", "chartest.xsd"),
        getResourceFile("type", "catalognons2.xsd"),
        getResourceFile("type", "global.xsd"),
        getResourceFile("type", "global2.xsd"),
        getResourceFile("type", "instanceclass1.xsd"),
        getResourceFile("type", "nillable.xsd"),
        getResourceFile("type", "opencontent.xsd")
    };

    private static List<XmlObject[]> groupsOfSchemasToCompile = new ArrayList<XmlObject[]>();

    private static void groupSchemas() throws Exception
    {
        // parse xsd files in each schema dir
        for (int i = 0; i < schemaDirs.length; i++)
        {
            String[] xsdFileNames = schemaDirs[i].list(new FilenameFilter()
                {
                    public boolean accept(File dir, String name)
                    {
                        return name.endsWith(".xsd");
                    }
                });
            XmlObject[] schemas = new XmlObject[xsdFileNames.length];
            for (int j = 0; j < xsdFileNames.length; j++)
            {
                File f = new File(schemaDirs[i], xsdFileNames[j]);
                System.out.println("parsing " + f);
                schemas[j] = XmlObject.Factory.parse(f);
            }
            groupsOfSchemasToCompile.add(schemas);
        }
        // parse each single schema file
        for (int i = 0; i < schemaFiles.length; i++)
        {
            XmlObject[] schemas = new XmlObject[1];
            File f = schemaFiles[i];
            System.out.println("parsing " + f);
            schemas[0] = XmlObject.Factory.parse(f);
            groupsOfSchemasToCompile.add(schemas);
        }
    }

    private static final SchemaTypeLoader instance = constructInstance();

    private static SchemaTypeLoader constructInstance()
    {
        SchemaTypeLoader bits = XmlBeans.getBuiltinTypeSystem();
        SchemaTypeLoader sdots = XmlBeans.typeSystemForClassLoader(davos.sdo.impl.type.BuiltInTypeSystem.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.sSDOSCHEMAS");
        SchemaTypeLoader stl = XmlBeans.typeLoaderUnion(new SchemaTypeLoader[] {bits, sdots});
        try
        {
            groupSchemas();
            for (XmlObject[] schemas : groupsOfSchemasToCompile)
            {
                SchemaTypeSystem sts = XmlBeans.compileXsd(schemas, stl, null);
                // enqueue the new schemas at the end
                stl = XmlBeans.typeLoaderUnion(new SchemaTypeLoader[] {stl, sts});
            }
        }
        catch (Exception e) 
        {
            e.printStackTrace();
        }
        return stl;
    }

    public static SchemaTypeLoader getSchemaTypeLoader()
    {
        return instance;
    }
}
