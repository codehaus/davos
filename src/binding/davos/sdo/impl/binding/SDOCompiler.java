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

import org.apache.xmlbeans.impl.tool.SchemaCompiler;
import org.apache.xmlbeans.impl.tool.CommandLine;
import org.apache.xmlbeans.impl.tool.CodeGenUtil;
import org.apache.xmlbeans.impl.values.XmlListImpl;
import org.apache.xmlbeans.impl.common.IOUtil;
import org.apache.xmlbeans.impl.common.XmlErrorPrinter;
import org.apache.xmlbeans.impl.common.ResolverUtil;
import org.apache.xmlbeans.impl.common.XmlErrorWatcher;
import org.apache.xmlbeans.impl.common.JarHelper;
import org.apache.xmlbeans.impl.schema.SchemaTypeLoaderImpl;
import org.apache.xmlbeans.impl.schema.SchemaTypeSystemImpl;
import org.apache.xmlbeans.impl.schema.PathResourceLoader;
import org.apache.xmlbeans.impl.schema.SchemaTypeSystemCompiler;
import org.apache.xmlbeans.impl.schema.StscState;
import org.apache.xmlbeans.impl.util.FilerImpl;
import org.apache.xmlbeans.impl.xb.xsdschema.SchemaDocument;
import org.apache.xmlbeans.SystemProperties;
import org.apache.xmlbeans.SchemaTypeSystem;
import org.apache.xmlbeans.ResourceLoader;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlErrorCodes;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.SimpleValue;
import org.xml.sax.EntityResolver;

import java.util.Set;
import java.util.HashSet;
import java.util.Collections;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Collection;
import java.util.Map;
import java.util.HashMap;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URI;

import repackage.Repackager;

import davos.sdo.type.TypeSystem;
import davos.sdo.impl.type.BuiltInTypeSystem;
import davos.sdo.impl.util.DefaultFilerImpl;
import davos.sdo.binding.BindingException;
import davos.sdo.binding.BindingSystem;
import davos.sdo.util.Filer;
import davos.sdo.Options;
import davos.sdo.TypeXML;

/**
 * Created
 * Date: Apr 29, 2006
 * Time: 1:53:38 PM
 */
public class SDOCompiler extends SchemaCompiler
{
    public static void printUsage()
    {
        System.out.println("Compiles a schema into SDO classes and metadata.");
        System.out.println("Usage: sdocomp [opts] [dirs]* [schema.xsd]* [service.wsdl]*");
        System.out.println("Options include:");
        System.out.println("    -cp [a;b;c] - classpath");
        System.out.println("    -d [dir] - target binary directory for .class and .xsb files");
        System.out.println("    -src [dir] - target directory for generated .java files");
        System.out.println("    -srconly - do not compile .java files or jar the output.");
        System.out.println("    -noextramethods - do not generate isSet, unset and create methods.");
        System.out.println("    -out [xmltypes.jar] - the name of the output jar");
        System.out.println("    -dl - permit network downloads for imports and includes (default is off)");
        System.out.println("    -noupa - do not enforce the unique particle attribution rule");
        System.out.println("    -nopvr - do not enforce the particle valid (restriction) rule");
        System.out.println("    -noann - ignore annotations");
        System.out.println("    -novdoc - do not validate contents of <documentation>");
        System.out.println("    -compiler - path to external java compiler");
        System.out.println("    -ms - initial memory for external java compiler (default '" + CodeGenUtil.DEFAULT_MEM_START + "')");
        System.out.println("    -mx - maximum memory for external java compiler (default '" + CodeGenUtil.DEFAULT_MEM_MAX + "')");
        System.out.println("    -debug - compile with debug symbols");
        System.out.println("    -quiet - print fewer informational messages");
        System.out.println("    -verbose - print more informational messages");
        System.out.println("    -version - prints version information");
        System.out.println("    -allowmdef \"[ns] [ns] [ns]\" - ignores multiple defs in given namespaces (use ##local for no-namespace)");
        System.out.println("    -allowmdef_all - ignores multiple defs in all namespaces");
        System.out.println("    -catalog [file] -  catalog file for org.apache.xml.resolver.tools.CatalogResolver. (Note: needs resolver.jar from http://xml.apache.org/commons/components/resolver/index.html)");
        /* Undocumented feature - pass in one schema compiler extension and related parameters
        System.out.println("    -repackage - repackage specification");
        */
        System.out.println("    -sdoanontypenames [namesStandard|namesComposite] - pass in a naming strategy for anonymous Schema types");
        System.out.println();
    }

    public static void main(String[] args)
    {
        System.exit(run(args));
    }

    public static int run(String[] args)
    {
        if (args.length == 0)
        {
            printUsage();
            return 0;
        }

        Set<String> flags = new HashSet<String>();
        flags.add("h");
        flags.add("help");
        flags.add("usage");
        flags.add("quiet");
        flags.add("verbose");
        flags.add("version");
        flags.add("dl");
        flags.add("noupa");
        flags.add("nopvr");
        flags.add("noann");
        flags.add("novdoc");
        flags.add("srconly");
        flags.add("noextramethods");
        flags.add("debug");
        flags.add("allowmdef_all");

        Set<String> opts = new HashSet<String>();
        opts.add("out");
        opts.add("name");
        opts.add("src");
        opts.add("d");
        opts.add("cp");
        opts.add("compiler");
        opts.add("ms");
        opts.add("mx");
        opts.add("repackage");
        opts.add("sdoanontypenames");
        opts.add("allowmdef");
        opts.add("catalog");
        CommandLine cl = new CommandLine(args, flags, opts);

        if (cl.getOpt("h") != null || cl.getOpt("help") != null || cl.getOpt("usage") != null)
        {
            printUsage();
            return 0;
        }

        String[] badopts = cl.getBadOpts();
        if (badopts.length > 0)
        {
            for (int i = 0; i < badopts.length; i++)
                System.out.println("Unrecognized option: " + badopts[i]);
            printUsage();
            return 0;
        }

        if (cl.getOpt("version") != null)
        {
            printVersion();
            return 0;
        }

        boolean verbose = (cl.getOpt("verbose") != null);
        boolean quiet = (cl.getOpt("quiet") != null);
        if (verbose)
            quiet = false;

        if (verbose)
            printVersion();

        String outputfilename = cl.getOpt("out");

        String repackage = cl.getOpt("repackage");

        String name = cl.getOpt("name");

        boolean download = (cl.getOpt("dl") != null);
        boolean noUpa = (cl.getOpt("noupa") != null);
        boolean noPvr = (cl.getOpt("nopvr") != null);
        boolean noAnn = (cl.getOpt("noann") != null);
        boolean noVDoc= (cl.getOpt("novdoc") != null);
        boolean nojavac = (cl.getOpt("srconly") != null);
        boolean debug = (cl.getOpt("debug") != null);
        boolean noextramethods = (cl.getOpt("noextramethods") != null);
        boolean allowmdefall = (cl.getOpt("allowmdef_all") != null);

        String allowmdef = cl.getOpt("allowmdef");
        Set mdefNamespaces = (allowmdef == null ? Collections.EMPTY_SET :
                new HashSet<String>(Arrays.asList(XmlListImpl.split_list(allowmdef))));

        int namingOption = -1;
        String namingForAnonymousTypes = cl.getOpt("sdoanontypenames");
        if (namingForAnonymousTypes != null)
        {
            if ("namesStandard".equalsIgnoreCase(namingForAnonymousTypes))
                namingOption = Options.NAMES_STANDARD;
            if ("namesComposite".equalsIgnoreCase(namingForAnonymousTypes))
                namingOption = Options.NAMES_COMPOSITE;
        }
        String classesdir = cl.getOpt("d");
        File classes = null;
        if (classesdir != null)
            classes = new File(classesdir);

        String srcdir = cl.getOpt("src");
        File src = null;
        if (srcdir != null)
            src = new File(srcdir);
        if (nojavac && srcdir == null && classes != null)
            src = classes;

        // create temp directory
        File tempdir = null;
        if (src == null || classes == null)
        {
            try
            {
                tempdir = createTempDir(null);
            }
            catch (java.io.IOException e)
            {
                System.err.println("Error creating temp dir " + e);
                return 1;
            }
        }

        File jarfile = null;
        if (outputfilename == null && classes == null && !nojavac)
            outputfilename = "sdotypes.jar";
        if (outputfilename != null)
            jarfile = new File(outputfilename);

        if (src == null)
            src = IOUtil.createDir(tempdir, "src");
        if (classes == null)
            classes = IOUtil.createDir(tempdir, "classes");

        File[] classpath;
        String cpString = cl.getOpt("cp");
        if (cpString != null)
        {
            String[] cpparts = cpString.split(File.pathSeparator);
            List<File> cpList = new ArrayList<File>();
            for (int i = 0; i < cpparts.length; i++)
                cpList.add(new File(cpparts[i]));
            classpath = cpList.toArray(new File[cpList.size()]);
        }
        else
        {
            classpath = CodeGenUtil.systemClasspath();
        }

        String compiler = cl.getOpt("compiler");

        String memoryInitialSize = cl.getOpt("ms");
        String memoryMaximumSize = cl.getOpt("mx");

        File[] xsdFiles = cl.filesEndingWith(".xsd");
        File[] wsdlFiles = cl.filesEndingWith(".wsdl");
        URL[] urlFiles = cl.getURLs();

        if (xsdFiles.length + wsdlFiles.length + urlFiles.length == 0)
        {
            System.out.println("Could not find any xsd or wsdl files to process.");
            return 0;
        }
        File baseDir = cl.getBaseDir();
        URI baseURI = baseDir == null ? null : baseDir.toURI();

        XmlErrorPrinter err = new XmlErrorPrinter(verbose, baseURI);

        String catString = cl.getOpt("catalog");

        Parameters params = new Parameters();
        params.setBaseDir(baseDir);
        params.setXsdFiles(xsdFiles);
        params.setWsdlFiles(wsdlFiles);
        params.setUrlFiles(urlFiles);
        params.setClasspath(classpath);
        params.setOutputJar(jarfile);
        params.setName(name);
        params.setSrcDir(src);
        params.setClassesDir(classes);
        params.setCompiler(compiler);
        params.setMemoryInitialSize(memoryInitialSize);
        params.setMemoryMaximumSize(memoryMaximumSize);
        params.setNojavac(nojavac);
        params.setQuiet(quiet);
        params.setVerbose(verbose);
        params.setDownload(download);
        params.setNoUpa(noUpa);
        params.setNoPvr(noPvr);
        params.setNoAnn(noAnn);
        params.setNoVDoc(noVDoc);
        params.setDebug(debug);
        params.setMdefAll(allowmdefall);
        params.setNoExtraMethods(noextramethods);
        params.setErrorListener(err);
        params.setRepackage(repackage);
        params.setNamingOption(namingOption);
        params.setMdefNamespaces(mdefNamespaces);
        params.setCatalogFile(catString);

        boolean result = compile(params);

        if (tempdir != null)
            tryHardToDelete(tempdir);

        if (!result)
            return 1;

        return 0;
    }

    private static SchemaTypeSystem loadTypeSystem(String name, File[] xsdFiles, File[] wsdlFiles, URL[] urlFiles,
        ResourceLoader cpResourceLoader,
        boolean download, boolean noUpa, boolean noPvr, boolean noAnn, boolean noVDoc,
        boolean mdefall,
        Set mdefNamespaces, File baseDir, Map sourcesToCopyMap,
        Collection outerErrorListener, File schemasDir, EntityResolver entResolver)
    {
        XmlErrorWatcher errorListener = new XmlErrorWatcher(outerErrorListener);

        // construct the state (have to initialize early in case of errors)
        StscState state = StscState.start();
        state.setErrorListener(errorListener);

        // For parsing XSD and WSDL files, we should use the SchemaDocument
        // classloader rather than the thread context classloader.  This is
        // because in some situations (such as when being invoked by ant
        // underneath the ide) the context classloader is potentially weird
        // (because of the design of ant).

        SchemaTypeLoader loader = XmlBeans.typeLoaderForClassLoader(SchemaDocument.class.getClassLoader());

        // step 1, parse all the XSD files.
        ArrayList scontentlist = new ArrayList();
        if (xsdFiles != null)
        {
            for (int i = 0; i < xsdFiles.length; i++)
            {
                try
                {
                    XmlOptions options = new XmlOptions();
                    options.setLoadLineNumbers();
                    options.setLoadMessageDigest();
                    options.setEntityResolver(entResolver);

                    XmlObject schemadoc = loader.parse(xsdFiles[i], null, options);
                    if (!(schemadoc instanceof SchemaDocument))
                    {
                        StscState.addError(errorListener, XmlErrorCodes.INVALID_DOCUMENT_TYPE,
                            new Object[] { xsdFiles[i], "schema" }, schemadoc);
                    }
                    else
                    {
                        addSchema(xsdFiles[i].toString(), (SchemaDocument)schemadoc,
                            errorListener, noVDoc, scontentlist);
                    }
                }
                catch (XmlException e)
                {
                    errorListener.add(e.getError());
                }
                catch (Exception e)
                {
                    StscState.addError(errorListener, XmlErrorCodes.CANNOT_LOAD_FILE,
                        new Object[] { "xsd", xsdFiles[i], e.getMessage() }, xsdFiles[i]);
                }
            }
        }

        // step 2, parse all WSDL files
        if (wsdlFiles != null)
        {
            for (int i = 0; i < wsdlFiles.length; i++)
            {
                try
                {
                    XmlOptions options = new XmlOptions();
                    options.setLoadLineNumbers();
                    options.setLoadSubstituteNamespaces(Collections.singletonMap(
                            "http://schemas.xmlsoap.org/wsdl/", "http://www.apache.org/internal/xmlbeans/wsdlsubst"
                    ));
                    options.setEntityResolver(entResolver);

                    XmlObject wsdldoc = loader.parse(wsdlFiles[i], null, options);

                    if (!(wsdldoc instanceof org.apache.xmlbeans.impl.xb.substwsdl.DefinitionsDocument))
                        StscState.addError(errorListener, XmlErrorCodes.INVALID_DOCUMENT_TYPE,
                            new Object[] { wsdlFiles[i], "wsdl" }, wsdldoc);
                    else
                    {
                        addWsdlSchemas(wsdlFiles[i].toString(), (org.apache.xmlbeans.impl.xb.substwsdl.DefinitionsDocument)wsdldoc, errorListener, noVDoc, scontentlist);
                    }
                }
                catch (XmlException e)
                {
                    errorListener.add(e.getError());
                }
                catch (Exception e)
                {
                    StscState.addError(errorListener, XmlErrorCodes.CANNOT_LOAD_FILE,
                        new Object[] { "wsdl", wsdlFiles[i], e.getMessage() }, wsdlFiles[i]);
                }
            }
        }

        // step 3, parse all URL files
        // XMLBEANS-58 - Ability to pass URLs instead of Files for Wsdl/Schemas
        if (urlFiles != null)
        {
            for (int i = 0; i < urlFiles.length; i++)
            {
                try
                {
                    XmlOptions options = new XmlOptions();
                    options.setLoadLineNumbers();
                    options.setLoadSubstituteNamespaces(Collections.singletonMap("http://schemas.xmlsoap.org/wsdl/", "http://www.apache.org/internal/xmlbeans/wsdlsubst"));
                    options.setEntityResolver(entResolver);

                    XmlObject urldoc = loader.parse(urlFiles[i], null, options);

                    if ((urldoc instanceof org.apache.xmlbeans.impl.xb.substwsdl.DefinitionsDocument))
                    {
                        addWsdlSchemas(urlFiles[i].toString(), (org.apache.xmlbeans.impl.xb.substwsdl.DefinitionsDocument)urldoc, errorListener, noVDoc, scontentlist);
                    }
                    else if ((urldoc instanceof SchemaDocument))
                    {
                        addSchema(urlFiles[i].toString(), (SchemaDocument)urldoc,
                            errorListener, noVDoc, scontentlist);
                    }
                    else
                    {
                        StscState.addError(errorListener, XmlErrorCodes.INVALID_DOCUMENT_TYPE,
                            new Object[]{urlFiles[i], "wsdl or schema"}, urldoc);
                    }

                }
                catch (XmlException e)
                {
                    errorListener.add(e.getError());
                }
                catch (Exception e)
                {
                    StscState.addError(errorListener, XmlErrorCodes.CANNOT_LOAD_FILE,
                        new Object[]{"url", urlFiles[i], e.getMessage()}, urlFiles[i]);
                }
            }
        }

        SchemaDocument.Schema[] sdocs = (SchemaDocument.Schema[])scontentlist.toArray(new SchemaDocument.Schema[scontentlist.size()]);

        SchemaTypeLoader linkTo = SchemaTypeLoaderImpl.build(null, cpResourceLoader, null);

        if (mdefall && mdefNamespaces.isEmpty())
        {
            // Extract all namespaces from the schema documents
            mdefNamespaces = new HashSet();
            for (int i = 0; i < sdocs.length; i++)
            {
                String targetNamespace = sdocs[i].getTargetNamespace();
                if (targetNamespace == null)
                    mdefNamespaces.add("##local");
                else
                    mdefNamespaces.add(targetNamespace);
            }
        }
        URI baseURI = null;
        if (baseDir != null)
            baseURI = baseDir.toURI();

        XmlOptions opts = new XmlOptions();
        if (download)
            opts.setCompileDownloadUrls();
        if (noUpa)
            opts.setCompileNoUpaRule();
        if (noPvr)
            opts.setCompileNoPvrRule();
        if (noAnn)
            opts.setCompileNoAnnotations();
        if (mdefNamespaces != null)
            opts.setCompileMdefNamespaces(mdefNamespaces);
        opts.setCompileNoValidation(); // already validated here
        opts.setEntityResolver(entResolver);

        // now pass it to the main compile function
        SchemaTypeSystemCompiler.Parameters params = new SchemaTypeSystemCompiler.Parameters();
        params.setName(name);
        params.setSchemas(sdocs);
        params.setLinkTo(linkTo);
        params.setOptions(opts);
        params.setErrorListener(errorListener);
        params.setJavaize(false);
        params.setBaseURI(baseURI);
        params.setSourcesToCopyMap(sourcesToCopyMap);
        params.setSchemasDir(schemasDir);
        return SchemaTypeSystemCompiler.compile(params);
    }

    private static void addSchema(String name, SchemaDocument schemadoc,
        XmlErrorWatcher errorListener, boolean noVDoc, List scontentlist)
    {
        StscState.addInfo(errorListener, "Loading schema file " + name);
        XmlOptions opts = new XmlOptions().setErrorListener(errorListener);
        if (noVDoc)
            opts.setValidateTreatLaxAsSkip();
        if (schemadoc.validate(opts))
            scontentlist.add((schemadoc).getSchema());
    }

    private static void addWsdlSchemas(String name,
        org.apache.xmlbeans.impl.xb.substwsdl.DefinitionsDocument wsdldoc,
        XmlErrorWatcher errorListener, boolean noVDoc, List scontentlist)
    {
        if (wsdlContainsEncoded(wsdldoc))
            StscState.addWarning(errorListener, "The WSDL " + name + " uses SOAP encoding. SOAP encoding is not compatible with literal XML Schema.", XmlErrorCodes.GENERIC_ERROR, wsdldoc);
        StscState.addInfo(errorListener, "Loading wsdl file " + name);
        XmlOptions opts = new XmlOptions().setErrorListener(errorListener);
        if (noVDoc)
        opts.setValidateTreatLaxAsSkip();
        XmlObject[] types = wsdldoc.getDefinitions().getTypesArray();
        int count = 0;
        for (int j = 0; j < types.length; j++)
        {
            XmlObject[] schemas = types[j].selectPath("declare namespace xs=\"http://www.w3.org/2001/XMLSchema\" xs:schema");
            if (schemas.length == 0)
            {
                StscState.addWarning(errorListener, "The WSDL " + name + " did not have any schema documents in namespace 'http://www.w3.org/2001/XMLSchema'", XmlErrorCodes.GENERIC_ERROR, wsdldoc);
                continue;
            }

            for (int k = 0; k < schemas.length; k++)
            {
                if (schemas[k] instanceof SchemaDocument.Schema &&
                    schemas[k].validate(opts))
                {
                    count++;
                    scontentlist.add(schemas[k]);
                }
            }
        }
        StscState.addInfo(errorListener, "Processing " + count + " schema(s) in " + name);
    }

    private static boolean wsdlContainsEncoded(XmlObject wsdldoc)
    {
        // search for any <soap:body use="encoded"/> etc.
        XmlObject[] useAttrs = wsdldoc.selectPath(
                "declare namespace soap='http://schemas.xmlsoap.org/wsdl/soap/' " +
                ".//soap:body/@use|.//soap:header/@use|.//soap:fault/@use");
        for (int i = 0; i < useAttrs.length; i++)
        {
            if ("encoded".equals(((SimpleValue)useAttrs[i]).getStringValue()))
                return true;
        }
        return false;
    }

    public static boolean compile(Parameters params)
    {
        File baseDir = params.getBaseDir();
        File[] xsdFiles = params.getXsdFiles();
        File[] wsdlFiles = params.getWsdlFiles();
        URL[] urlFiles = params.getUrlFiles();
        File[] classpath = params.getClasspath();
        File outputJar = params.getOutputJar();
        String name = params.getName();
        File srcDir = params.getSrcDir();
        File classesDir = params.getClassesDir();
        String compiler = params.getCompiler();
        String memoryInitialSize = params.getMemoryInitialSize();
        String memoryMaximumSize = params.getMemoryMaximumSize();
        boolean nojavac = params.isNojavac();
        boolean debug = params.isDebug();
        boolean mdefall = params.isMdefAll();
        boolean verbose = params.isVerbose();
        boolean quiet = params.isQuiet();
        boolean download = params.isDownload();
        boolean noUpa = params.isNoUpa();
        boolean noPvr = params.isNoPvr();
        boolean noAnn = params.isNoAnn();
        boolean noVDoc = params.isNoVDoc();
        Collection outerErrorListener = params.getErrorListener();

        String repackage = params.getRepackage();

        if (repackage!=null)
        {
            SchemaTypeLoaderImpl.METADATA_PACKAGE_LOAD = SchemaTypeSystemImpl.METADATA_PACKAGE_GEN;

            String stsPackage = SchemaTypeSystem.class.getPackage().getName();
            Repackager repackager = new Repackager( repackage );

            SchemaTypeSystemImpl.METADATA_PACKAGE_GEN = repackager.repackage(new StringBuffer(stsPackage)).toString().replace('.','_');

            System.out.println("\n\n\n" + stsPackage + ".SchemaCompiler  Metadata LOAD:" + SchemaTypeLoaderImpl.METADATA_PACKAGE_LOAD + " GEN:" + SchemaTypeSystemImpl.METADATA_PACKAGE_GEN);
        }

        Set mdefNamespaces = params.getMdefNamespaces();

        EntityResolver cmdLineEntRes = params.getEntityResolver() == null ?
            ResolverUtil.resolverForCatalog(params.getCatalogFile()) : params.getEntityResolver();

        if (srcDir == null || classesDir == null)
            throw new IllegalArgumentException("src and class gen directories may not be null.");

        long start = System.currentTimeMillis();

        // Calculate the usenames based on the relativized filenames on the filesystem
        if (baseDir == null)
            baseDir = new File(SystemProperties.getProperty("user.dir"));

        ResourceLoader cpResourceLoader = null;

        Map sourcesToCopyMap = new HashMap();

        if (classpath != null)
            cpResourceLoader = new PathResourceLoader(classpath);

        boolean result = true;

        File schemasDir = IOUtil.createDir(classesDir, "schema" + SchemaTypeSystemImpl.METADATA_PACKAGE_GEN + "/src");

        // build the in-memory type system
        XmlErrorWatcher errorListener = new XmlErrorWatcher(outerErrorListener);
        SchemaTypeSystem system = loadTypeSystem(name, xsdFiles, wsdlFiles, urlFiles,
            cpResourceLoader, download, noUpa, noPvr, noAnn, noVDoc, mdefall, mdefNamespaces,
            baseDir, sourcesToCopyMap, errorListener, schemasDir, cmdLineEntRes);
        if (errorListener.hasError())
            result = false;
        long finish = System.currentTimeMillis();
        if (!quiet)
            System.out.println("Time to build schema type system: " + ((double)(finish - start) / 1000.0) + " seconds" );

        // now code generate and compile the JAR
        if (result && system != null) // todo: don't check "result" here if we want to compile anyway, ignoring invalid schemas
        {
            start = System.currentTimeMillis();

            result = sdoCompileAndSave(system, cpResourceLoader, params, false) != null;

            // We may want to enable incremental source generation here
//            if (incrSrcGen)
//            {
//                // We have to delete extra source files that may be out of date
//                SchemaCodeGenerator.deleteObsoleteFiles(srcDir, srcDir,
//                    new HashSet(filer.getSourceFiles()));
//            }

            if (result)
            {
                finish = System.currentTimeMillis();
                if (!quiet)
                    System.out.println("Time to generate code: " + ((double)(finish - start) / 1000.0) + " seconds" );
            }

            // compile source
            if (result && !nojavac)
            {
                start = System.currentTimeMillis();

                List<File> sourcefiles = /*filer.getSourceFiles()*;*/new ArrayList<File>();
                sourcefiles.add(srcDir);

                if (srcDir.listFiles().length == 0)
                    ; // Skip compilation if there are no files being generated
                else
                if (!CodeGenUtil.externalCompile(sourcefiles, classesDir, classpath, debug, compiler, "1.5", memoryInitialSize, memoryMaximumSize, quiet, verbose))
                    result = false;

                finish = System.currentTimeMillis();
                if (result && !params.isQuiet())
                    System.out.println("Time to compile code: " + ((double)(finish - start) / 1000.0) + " seconds" );

                // jar classes and .xsb
                if (result && outputJar != null)
                {
                    try
                    {
                        new JarHelper().jarDir(classesDir, outputJar);
                    }
                    catch (IOException e)
                    {
                        System.err.println("IO Error " + e);
                        result = false;
                    }

                    if (result && !params.isQuiet())
                        System.out.println("Compiled types to: " + outputJar);
                }
            }
        }

        if (!result && !quiet)
        {
            System.out.println("BUILD FAILED");
        }

        if (cpResourceLoader != null)
            cpResourceLoader.close();
        return result;
    }

    public static BindingSystem sdoCompileAndSave(SchemaTypeSystem system, Parameters params)
    {
        return sdoCompileAndSave(system, null, params, true);
    }

    private static BindingSystem sdoCompileAndSave(SchemaTypeSystem system,
        ResourceLoader cpResourceLoader, Parameters params, boolean buildBindingSystem)
    {
        String repackage = params.getRepackage();
        File classesDir = params.getClassesDir();
        File srcDir = params.getSrcDir();
        boolean verbose = params.isVerbose();
        boolean incrSrcGen = params.isIncrementalSrcGen();
        boolean noExtraMethods = params.isNoExtraMethods();
        int namingOption = params.getNamingOption();
        Collection outerErrorListener = params.getErrorListener();

        // filer implementation writes binary .xsd and generated source to disk
        Repackager repackager = (repackage == null ? null : new Repackager(repackage));
        FilerImpl filer = new FilerImpl(classesDir, srcDir, repackager, verbose, incrSrcGen);

        // save .xsb files
        system.save(filer);

        // gen source files
        // TODO(radup) need to get SDO types from the classpath and inject them into the mix
        TypeJavaMapping jmapping;
        TypeSystem sdoTypeSystemFromSchema;
        CompileBindingSystem result = null;
        try
        {
//                TypeSystemLoader sdoTSLoader = TypeSystemLoader.newInstance(cpResourceLoader);
//                sdoTSLoader.setSchemaTypeLoader(XmlBeans.typeLoaderForResource(cpResourceLoader));
//
//                TypeSystem sdoClasspathTS = TypeSystemUnion.union(BuiltInTypeSystem.getInstance(), sdoTSLoader);
//
//                BindingSystem cpBindingSystem = new CompileBindingSystem(cpResourceLoader, sdoClasspathTS);
            BindingSystem cpBindingSystem = new CompileBindingSystem(cpResourceLoader, BuiltInTypeSystem.getInstance());
            //TypeSystem sdoClasspathTS = cpBindingSystem.getTypeSystem();

            Map<TypeXML, String> packageNames = new HashMap<TypeXML, String>();
            Map<TypeXML, String> instanceClasses = new HashMap<TypeXML, String>();
            Options opts = null;
            if (namingOption > 0)
                opts = new Options().setCompileAnonymousTypeNames(namingOption);
            sdoTypeSystemFromSchema = Schema2SDO.createSDOTypeSystem(system, cpBindingSystem, packageNames, instanceClasses, null, opts);

            Filer sdoFiler = new DefaultFilerImpl(srcDir, classesDir);

            DefaultBindingContext bctx = new SDOCompilerBindingContext(sdoFiler, getNameFromSchemaTypeSystem(system), !noExtraMethods);
            DefaultBindingEngineImpl javaBindingEngine = new DefaultBindingEngineImpl();
            javaBindingEngine.setContext(bctx);
            boolean bound = javaBindingEngine.bind(sdoTypeSystemFromSchema, cpBindingSystem, packageNames, instanceClasses);
            if (bound)
            {
                jmapping = javaBindingEngine.getTypeToJavaNames();
                result = new CompileBindingSystem(EMPTY_RESOURCE_LOADER, sdoTypeSystemFromSchema);
                if (buildBindingSystem)
                    for (TypeXML sdoType : sdoTypeSystemFromSchema.getAllTypes())
                    {
                        JavaClassName jcn = jmapping.getJavaClass(sdoType);
                        if (jcn != null)
                            result.addMapping(sdoType, jcn.getIntfFullName(), jcn.getImplFullName());
                    }
            }
        }
        catch (RuntimeException e)
        {
            outerErrorListener.add(XmlError.forMessage(e.getMessage()));
            e.printStackTrace();
        }
        catch (BindingException e)
        {
            outerErrorListener.add(XmlError.forMessage(e.getMessage()));
        }
        catch (IOException e)
        {
            outerErrorListener.add(XmlError.forMessage(e.getMessage()));
        }
        return result;
    }

    private static ResourceLoader EMPTY_RESOURCE_LOADER = new ResourceLoader ()
    {
        public InputStream getResourceAsStream(String resourceName)
        {
            return null;
        }

        public void close()
        {
        }
    };

    private static String getNameFromSchemaTypeSystem(SchemaTypeSystem typeSystem)
    {
        String result = typeSystem.getName();
        int lastDot = result.lastIndexOf('.');
        if (lastDot>-1 && lastDot<result.length()-1)
            result = result.substring(lastDot+1, result.length());

        return result;
    }

    private static void printVersion()
    {
        System.out.println("SDO specification version 2.1");
    }

    public static File createTempDir(String dirName) throws IOException
    {
        // Some beta builds of JDK1.5 are having troubles creating temp directories
        // if the java.io.tmpdir doesn't exist.  This seems to help.
        try
        {
            File tmpDirFile = new File(SystemProperties.getProperty("java.io.tmpdir"));
            tmpDirFile.mkdirs();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        File tmpFile = File.createTempFile(dirName == null ? "xbean" : dirName, null);
        String path = tmpFile.getAbsolutePath();
        if (!path.endsWith(".tmp"))
            throw new IOException("Error: createTempFile did not create a file ending with .tmp");
        path = path.substring(0, path.length() - 4);
        File tmpSrcDir = null;

        for (int count = 0; count < 100; count++)
        {
            String name = path + ".d" + (count == 0 ? "" : Integer.toString(count++));

            tmpSrcDir = new File(name);

            if (!tmpSrcDir.exists())
            {
                boolean created = tmpSrcDir.mkdirs();
                assert created : "Could not create " + tmpSrcDir.getAbsolutePath();
                break;
            }
        }
        tmpFile.deleteOnExit();

        return tmpSrcDir;
    }

    public static void tryHardToDelete(File dir)
    {
        tryToDelete(dir);
        if (dir.exists())
            tryToDeleteLater(dir);
    }

    private static void tryToDelete(File dir)
    {
        if (dir.exists())
        {
            if (dir.isDirectory())
            {
                String[] list = dir.list(); // can return null if I/O error
                if (list != null)
                    for (int i = 0; i < list.length; i++)
                        tryToDelete(new File(dir, list[i]));
            }
            if (!dir.delete())
                return; // don't try very hard, because we're just deleting tmp
        }
    }

    private static Set<File> deleteFileQueue = new HashSet<File>();
    private static int triesRemaining = 0;

    private static boolean tryNowThatItsLater()
    {
        List files;

        synchronized (deleteFileQueue)
        {
            files = new ArrayList<File>(deleteFileQueue);
            deleteFileQueue.clear();
        }

        List<File> retry = new ArrayList<File>();

        for (Iterator i = files.iterator(); i.hasNext(); )
        {
            File file = (File)i.next();
            tryToDelete(file);
            if (file.exists())
                retry.add(file);
        }

        synchronized (deleteFileQueue)
        {
            if (triesRemaining > 0)
                triesRemaining -= 1;

            if (triesRemaining <= 0 || retry.size() == 0) // done?
                triesRemaining = 0;
            else
                deleteFileQueue.addAll(retry); // try again?

            return (triesRemaining <= 0);
        }
    }

    private static void giveUp()
    {
        synchronized (deleteFileQueue)
        {
            deleteFileQueue.clear();
            triesRemaining = 0;
        }
    }

    private static void tryToDeleteLater(File dir)
    {
        synchronized (deleteFileQueue)
        {
            deleteFileQueue.add(dir);
            if (triesRemaining == 0)
            {
                new Thread()
                {
                    public void run()
                    {
                        // repeats tryNow until triesRemaining == 0
                        try
                        {
                            for (;;)
                            {
                                if (tryNowThatItsLater())
                                    return; // succeeded
                                Thread.sleep(1000 * 3); // wait three seconds
                            }
                        }
                        catch (InterruptedException e)
                        {
                            giveUp();
                        }
                    }
                };
            }

            if (triesRemaining < 10)
                triesRemaining = 10;
        }
    }

    public static class Parameters extends org.apache.xmlbeans.impl.tool.SchemaCompiler.Parameters
    {
        boolean noExtraMethods;
        int namingOption;
        boolean mdefall;

        public boolean isNoExtraMethods()
        {
            return noExtraMethods;
        }

        public void setNoExtraMethods(boolean noExtraMethods)
        {
            this.noExtraMethods = noExtraMethods;
        }

        public int getNamingOption()
        {
            return namingOption;
        }

        public void setNamingOption(int namingOptions)
        {
            this.namingOption = namingOptions;
        }

        public boolean isMdefAll()
        {
            return mdefall;
        }

        public void setMdefAll(boolean allowmdefall)
        {
            this.mdefall = allowmdefall;
        }
    }

    protected static class SDOCompilerBindingContext extends davos.sdo.impl.binding.DefaultBindingContext
    {
        private boolean _genExtraMethods;

        public SDOCompilerBindingContext(Filer filer, String saveLoadName, boolean genExtraMethods)
        {
            super(filer, saveLoadName);
            _genExtraMethods = genExtraMethods;
        }

        public boolean genIsSetMethods()
        {
            return _genExtraMethods;
        }

        public boolean genUnsetMethods()
        {
            return _genExtraMethods;
        }

        public boolean genCreateMethods()
        {
            return _genExtraMethods;
        }
    }
}
