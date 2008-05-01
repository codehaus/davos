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
package checkin;

import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;

import javax.sdo.DataObject;
import javax.sdo.helper.XMLHelper;
import javax.sdo.helper.XMLDocument;

import junit.framework.Test;
import junit.framework.TestSuite;
import common.MultiThreadedTest;

/**
 * @author Wing Yew Poon
 */
public class MultiThreadedLoadingTest extends MultiThreadedTest
{
    public MultiThreadedLoadingTest(String name)
    {
        super(name);
        //noiseLevel = VERBOSE; // default is NORMAL
    }

    public static Test suite()
    {
        
        TestSuite suite = new TestSuite();
        suite.addTest(new MultiThreadedLoadingTest("testLoad"));
        
        // or
        //TestSuite suite = new TestSuite(MultiThreadedLoadingTest.class);
        return suite;
    }

    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(suite());
    }

    private static XMLHelper xmlHelper = context.getXMLHelper();
    private static String xml;
    static
    {
        try
        {
            File f = new File(RESOURCES + S + "checkin" + S + "employees.xml");
            BufferedReader r = new BufferedReader(new FileReader(f));
            StringBuffer sb = new StringBuffer();
            String line = null;
            while ((line = r.readLine()) != null)
                sb.append(line);
            r.close();
            xml = sb.toString();
        }
        catch (IOException e) 
        {
            System.out.println("Error in initializing MultiThreadedLoadingTest: error in reading xml file due to " + e);
        }
    }

    public class Loader extends TestCaseRunnable
    {
        public void runTestCase()
        {
            XMLDocument doc = xmlHelper.load(xml);
            assertEquals("http://xmlbeans.apache.org/samples/xquery/employees", doc.getRootElementURI());
            assertEquals("employees", doc.getRootElementName());
            DataObject root = doc.getRootObject();
            assertNotNull(root);
            //if (Thread.currentThread().isInterrupted())
            //    return;
        }
    }

    public void testLoad() throws Exception
    {
        int N = 25;
        TestCaseRunnable[] runnables = new Loader[N];
        for (int i = 0; i < N; i++)
        {
            runnables[i] = new Loader();
        }
        runTestCaseRunnables(runnables);
    }
}
