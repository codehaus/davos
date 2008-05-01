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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.net.URL;

import davos.sdo.SDOContext;
import davos.sdo.SDOContextFactory;
import org.apache.xmlbeans.SchemaTypeLoader;

import junit.framework.TestCase;
import org.custommonkey.xmlunit.Diff;

/**
 *  Base class for SDOContextFactory TestCases
 *  - collects together some directory locations
 *  @author Wing Yew Poon
 */
public class BaseTest extends TestCase
{
    public BaseTest(String name)
    {
        super(name);
    }

    public static final String XSD_URI = "http://www.w3.org/2001/XMLSchema";
    public static final String XSI_URI = "http://www.w3.org/2001/XMLSchema-instance";
    public static final String SDO_URI = "commonj.sdo";
    public static final String SDO_XML_URI = "commonj.sdo/xml";
    public static final String SDO_JAVA_URI = "commonj.sdo/java";

    public static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

    public static final String newline = System.getProperty("line.separator");
    public static final String S = File.separator;

    public static final int STRICT = 1;
    public static final int NEUTRAL = 0;
    public static final int IGNORE_WHITESPACE = -1;

    public static String BASEDIR = getBaseDir();
    public static String RESOURCES =
        BASEDIR + S + "test" + S + "resources";
    public static String BUILDDIR = getBuildDir();
    public static String OUTPUTROOT = getOutputRoot();

    protected static SDOContext context = getContext();
        //SDOContextFactory.getGlobalSDOContext(); // default
        //SDOContextFactory.createNewSDOContext(Thread.currentThread().getContextClassLoader());
        //SDOContextFactory.createNewSDOContext(BaseTest.class.getClassLoader());
        //SDOContextFactory.createNewSDOContext(XmlBeans.getContextTypeLoader());

    private static SDOContext getContext()
    {
        String ctxType = System.getProperty("sdo.context.type");
        System.out.println("sdo.context.type is " + ctxType);
        if (ctxType != null)
        {
            if (ctxType.equals("global"))
                return SDOContextFactory.getGlobalSDOContext(); // default
            else if (ctxType.equals("classloader"))
                return SDOContextFactory.createNewSDOContext(BaseTest.class.getClassLoader());
                //return SDOContextFactory.createNewSDOContext(Thread.currentThread().getContextClassLoader());
            else if (ctxType.equals("schematypeloader"))
            {
                SchemaTypeLoader stl = SchemaTypeLoaderFactory.getSchemaTypeLoader();
                return SDOContextFactory.createNewSDOContext(stl, null);
            }
        }
        // else return the default
        return SDOContextFactory.getGlobalSDOContext();
    }

    private static String getBaseDir()
    {
        String basedir = System.getProperty("sdo.root");
        if (basedir == null)
            return new File("..").getAbsolutePath();
        else
            return new File(basedir).getAbsolutePath();
    }

    private static String getBuildDir()
    {
        String builddir = System.getProperty("build.root");
        if (builddir == null)
            return new File(".." + S + "build").getAbsolutePath();
        else
            return new File(builddir).getAbsolutePath();
    }

    private static String getOutputRoot()
    {
        String outputroot = System.getProperty("test.output.root");
        if (outputroot == null)
            return new File(".." + S + "build" + S + "test" + S + "output").getAbsolutePath();
        else
            return new File(outputroot).getAbsolutePath();
    }

    /**
     * Compare an xml file with a reference xml file. 
     * A junit.framework.AssertionFailedError is thrown if the file does not
     * match the reference.
     * @param f1 The reference xml file.
     * @param f2 The xml file.
     * @param strict Whether to use a strict comparison or not.
     * @throws Exception if there is a problem reading any of the files.
     */
    protected void compareXMLFiles(File f1, File f2, int strictness) 
        throws Exception
    {
        if ((strictness != STRICT) &&
            (strictness != NEUTRAL) &&
            (strictness != IGNORE_WHITESPACE))
            throw new IllegalArgumentException("strictness " + strictness + " not recognized");
        boolean strict = (strictness == STRICT);
        boolean ignore_ws = (strictness == IGNORE_WHITESPACE);
        BufferedReader br1 = new BufferedReader(new FileReader(f1));
        StringBuffer sb1 = new StringBuffer();
        String line = null;
        while ((line = br1.readLine()) != null)
        {
            sb1.append(line).append(newline);
        }
        br1.close();
        BufferedReader br2 = new BufferedReader(new FileReader(f2));
        StringBuffer sb2 = new StringBuffer();
        line = null;
        while ((line = br2.readLine()) != null)
        {
            sb2.append(line).append(newline);
        }
        br2.close();
        String xml1 = (ignore_ws ? sb1.toString().replaceAll(">[ \r\n]*<", "><") : sb1.toString());
        String xml2 = (ignore_ws ? sb2.toString().replaceAll(">[ \r\n]*<", "><") : sb2.toString());
        Diff diff = new Diff(xml1, xml2);
        if (strict)
            assertTrue(diff.toString(), diff.identical());
        else
            assertTrue(diff.toString(), diff.similar());
    }

    protected void compareXMLFiles(File f1, File f2) throws Exception
    {
        compareXMLFiles(f1, f2, NEUTRAL);
    }

    /**
     * Compare an xml file with two reference xml files. 
     * A junit.framework.AssertionFailedError is thrown if the file does not
     * match either reference.
     * @param f1 The first reference xml file.
     * @param f2 The second reference xml file.
     * @param f3 The xml file.
     * @throws Exception if there is a problem reading any of the files.
     */
    protected void compareXMLFiles(File f1, File f2, File f3) 
        throws Exception
    {
        BufferedReader br1 = new BufferedReader(new FileReader(f1));
        StringBuffer sb1 = new StringBuffer();
        String line = null;
        while ((line = br1.readLine()) != null)
        {
            sb1.append(line).append(newline);
        }
        br1.close();
        BufferedReader br2 = new BufferedReader(new FileReader(f2));
        StringBuffer sb2 = new StringBuffer();
        line = null;
        while ((line = br2.readLine()) != null)
        {
            sb2.append(line).append(newline);
        }
        br2.close();
        BufferedReader br3 = new BufferedReader(new FileReader(f3));
        StringBuffer sb3 = new StringBuffer();
        line = null;
        while ((line = br3.readLine()) != null)
        {
            sb3.append(line).append(newline);
        }
        br3.close();
        String xml1 = sb1.toString();
        String xml2 = sb2.toString();
        String xml3 = sb3.toString();
        Diff diff1 = new Diff(xml1, xml3);
        Diff diff2 = new Diff(xml2, xml3);
        if (!diff1.similar() && !diff2.similar())
        {
            assertTrue(diff1.toString(), diff1.similar());
            assertTrue(diff2.toString(), diff2.similar());
        }
    }

    /** 
     * Convenience method to return the contents of an xml file as a String.
     * @param f file
     * @return the String
     */
    public String getXML(File f) throws Exception
    {
        BufferedReader br = new BufferedReader(new FileReader(f));
        StringBuffer sb = new StringBuffer();
        String line = null;
        while ((line = br.readLine()) != null)
        {
            sb.append(line).append(newline);
        }
        br.close();
        String xml = sb.toString();
        return xml;
    }

    /** 
     * Convenience method to return a resource file.
     * @param dirname name of subdirectory of resources directory
     * @param filename name of file
     * @return the File
     */
    public File getResourceFile(String dirname, String filename)
    {
        // this assumes that the resources directory is in the classpath
        URL resource = getClass().getResource("/" + dirname + "/" + filename);
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

    /** 
     * Convenience method to return a resource file as a stream.
     * @param dirname name of subdirectory of resources directory
     * @param filename name of file
     * @return an InputStream to the file
     */
    public InputStream getResourceAsStream(String dirname, String filename)
    {
        // this assumes that the resources directory is in the classpath;
        // there is no fallback
        return getClass().getResourceAsStream("/" + dirname + "/" + filename);
    }
}
