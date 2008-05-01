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

import javax.sdo.DataObject;
import javax.sdo.helper.XMLDocument;
import javax.sdo.helper.XMLHelper;

import java.io.Serializable;
import java.io.IOException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.FileInputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;

import junit.framework.Test;
import junit.framework.TestSuite;
import common.BaseTest;

/**
 * @author Cezar Andrei (cezar.andrei at bea.com)
 *         Date: Oct 6, 2006
 */
public class JavaSerializationTest
    extends BaseTest
{
    public JavaSerializationTest(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        TestSuite suite = new TestSuite();
        suite.addTest(new JavaSerializationTest("testSerialize"));
        return suite;
    }

    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(suite());
    }

    public static File dir;
    static
    {
        dir = new File(OUTPUTROOT + S + "checkin");
        dir.mkdirs();
    }

    public static class UserClass
        implements Serializable
    {
        String f1;
        DataObject f2;
        DataObject f3;

        public String toString() { return "UserClass: f1=" + f1 + "\n  f2={" + f2 + "}" + "\n  f3={" + f3 + "}"; }
    }


    public void testSerialize()
        throws IOException, ClassNotFoundException
    {
        UserClass uc = new UserClass();
        uc.f1 = "userInstance";

        davos.sdo.SDOContext ctx = 
            davos.sdo.SDOContextFactory.getGlobalSDOContext();
        XMLHelper xmlHelper = ctx.getXMLHelper();
        XMLDocument doc = xmlHelper.load("<root><a><aa>text1</aa></a><b><bb>text2</bb></b></root>");
        DataObject root = doc.getRootObject();
        uc.f2 = (DataObject)root.get("a.0");
        uc.f3 = (DataObject)root.get("b.0");

        assertEquals("text1", uc.f2.getString("aa.0"));
        assertEquals("text2", uc.f3.getString("bb.0"));

        System.out.println("Writing ... ");
        File f = new File(dir, "tmp.ser");
        FileOutputStream fos = new FileOutputStream(f);
        ObjectOutput oos = new ObjectOutputStream(fos);
        oos.writeObject("Today");
//        System.out.println("  " + uc.getClass() + "  " + uc);
        oos.writeObject(uc);
        oos.flush();

        FileInputStream fis = new FileInputStream(f);
        ObjectInput ois = new ObjectInputStream(fis);
//        System.out.println("Reading: " + ois.readObject());
        assertEquals("Today", ois.readObject());

        uc = (UserClass) ois.readObject();
        System.out.println(" " + uc );

//        printDO(uc.f2);
//        printDO(uc.f3);

        assertEquals("text1", uc.f2.get("aa.0"));
        assertEquals("text2", uc.f3.get("bb.0"));

        assertEquals(true, uc.f2.getRootObject()==uc.f3.getRootObject());
    }
}
