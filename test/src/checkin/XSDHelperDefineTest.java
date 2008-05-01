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
import java.io.InputStream;
import java.io.IOException;
import java.util.List;

import javax.sdo.Type;
import javax.sdo.helper.XSDHelper;

import junit.framework.*;
import common.BaseTest;

/**
 * @author Wing Yew Poon
 */
public class XSDHelperDefineTest extends BaseTest
{
    public XSDHelperDefineTest(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        
        TestSuite suite = new TestSuite();
        suite.addTest(new XSDHelperDefineTest("testExampleSchema"));
        
        // or
        //TestSuite suite = new TestSuite(XSDHelperDefineTest.class);
        return suite;
    }

    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(suite());
    }

    public void testExampleSchema() throws IOException
    {
        InputStream resourceStream = 
            getResourceAsStream("checkin", "IPO.xsd");
        File resourceFile = getResourceFile("checkin", "IPO.xsd");
        XSDHelper xsdHelper = context.getXSDHelper();
        List<Type> types = 
            xsdHelper.define(resourceStream, 
                             resourceFile.toURL().toString());
        resourceStream.close();
        for (Type t : types)
        {
            System.out.println(" type: " + t.getName() + " @ " + t.getURI());
        }
    }
}
