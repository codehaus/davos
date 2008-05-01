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
package marshal;

import common.BaseTest;

import javax.sdo.helper.XMLDocument;
import javax.sdo.helper.XMLHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;

import davos.sdo.Options;
import davos.sdo.SDOXmlException;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author Radu Preotiuc-Pietro
 */
public class ValidationTest extends BaseTest
{

    public ValidationTest(String name)
    {
        super(name);
    }

    public static Test suite()
    {

        TestSuite suite = new TestSuite(ValidationTest.class);
        return suite;
    }

    private static XMLHelper xmlHelper = context.getXMLHelper();

    private static String companyInvalid =
        "<company:company xmlns:company=\"company2.xsd\" name=\"ACME\" employeeOfTheMonth=\"E0002\">" +
        "    <departments name=\"AdvancedTechnologies\" location=\"NY\" number=\"123\">" +
        "    <employee name=\"John Jones\" SN=\"E0001\"/>" +
        "    <employees name=\"Mary Smith\" SN=\"E0002\" manager=\"true\"/>" +
        "    <employees name=\"Jane Doe\" SN=\"E0003\"/>" +
        "  </departments>" +
        "</company:company>";
    private static String qnameValid = 
        "<test:QNameRoot xmlns:test=\"test/QName\" date=\"2008-01\" qname=\"ns1:test\" xmlns:ns1=\"urn:ValidationTest\"/>";
    private static String qnameInvalid1 =
        "<test:QNameRoot xmlns:test=\"test/QName\" date=\"2008-01\" qname=\"ns1:test\"/>";
    private static String qnameInvalid2 =
        "<test:QNameRoot xmlns:test=\"test/QName\" date=\"2008-01-01\" qname=\"ns1:test\" xmlns:ns1=\"urn:ValidationTest\"/>";

    public void testLoadCompanyV() throws IOException
    {
        Options o = new Options().setValidate();
        File f = getResourceFile("checkin", "company2.xml");
        XMLDocument doc = xmlHelper.load(new FileInputStream(f), f.toURL().toString(), o);
        assertEquals("CompanyType", doc.getRootObject().getType().getName());
    }

    public void testLoadCompanyI() throws IOException
    {
        Options o = new Options().setValidate();
        boolean exCaught = false;
        try
        {
            XMLDocument doc = xmlHelper.load(new StringReader(companyInvalid), null, o);
        }
        catch (SDOXmlException sxe)
        {
            exCaught = true;
        }
        assertTrue("Invalid document passed validation!", exCaught);
    }

    public void testLoadQNameV() throws IOException
    {
        Options o = new Options().setValidate();
        XMLDocument doc = xmlHelper.load(new StringReader(qnameValid), null, o);
        assertEquals("typeWithQName", doc.getRootObject().getType().getName());
    }

    public void testLoadQNameI() throws IOException
    {
        Options o = new Options().setValidate();
        boolean exCaught = false;
        try
        {
            XMLDocument doc = xmlHelper.load(new StringReader(qnameInvalid1), null, o);
        }
        catch (SDOXmlException sxe)
        {
            exCaught = true;
        }
        assertTrue("Invalid document passed validation!", exCaught);
        exCaught = false;
        try
        {
            XMLDocument doc = xmlHelper.load(new StringReader(qnameInvalid2), null, o);
        }
        catch (SDOXmlException sxe)
        {
            exCaught = true;
        }
        assertTrue("Invalid document passed validation!", exCaught);
    }
}
