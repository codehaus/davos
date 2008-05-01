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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.util.List;

import javax.sdo.DataObject;
import javax.sdo.DataGraph;
import javax.sdo.Type;
import javax.sdo.helper.XMLDocument;
import javax.sdo.helper.XMLHelper;

import davos.sdo.Options;
import davos.sdo.impl.common.Names;
import davos.sdo.impl.helpers.DataGraphHelper;

/**
 * @author Wing Yew Poon
 */
public class DataTest extends BaseTest
{
    public DataTest(String name)
    {
        super(name);
    }

    protected int find(Type t, List<DataObject> dataObjects, int index)
    {
        int i;
        boolean found = false;
        for (i = index; i < dataObjects.size(); i++)
        {
            DataObject o = dataObjects.get(i);
            if (o.getType().equals(t))
            {
                found = true;
                break;
            }
        }
        if (found)
            return i;
        else
            return -1;
    }

    protected DataObject getRootDataObject(String dir, String file)
        throws Exception
    {
        File f = getResourceFile(dir, file);
        InputStream in = new FileInputStream(f);
        XMLHelper xmlHelper = context.getXMLHelper();
        Options o = new Options().setValidate();
        XMLDocument doc = xmlHelper.load(in, f.toURL().toString(), o);
        DataObject root = doc.getRootObject();
        in.close();
        return root;
    }

    protected DataObject getRootDataObjectWrapped(String dir, String file)
        throws Exception
    {
        File f = getResourceFile(dir, file);
        InputStream in = new FileInputStream(f);
        XMLHelper xmlHelper = context.getXMLHelper();
        Options o = new Options().setValidate();
        XMLDocument doc = xmlHelper.load(in, f.toURL().toString(), o);
        DataObject root = doc.getRootObject();
        DataGraphHelper.wrapWithDataGraph(root);
        in.close();
        return root;
    }

    protected DataGraph getDataGraph(String dir, String file)
        throws Exception
    {
        File f = getResourceFile(dir, file);
        InputStream in = new FileInputStream(f);
        XMLHelper xmlHelper = context.getXMLHelper();
        Options o = new Options().setValidate();
        XMLDocument doc = xmlHelper.load(in, f.toURL().toString(), o);
        if (!Names.URI_SDO.equals(doc.getRootElementURI()) ||
            !Names.SDO_DATAGRAPH.equals(doc.getRootElementName()))
            throw new RuntimeException("File \"" + dir + '/' + file +
                "\" does not contain a datagraph");
        DataGraph dataGraph = doc.getRootObject().getDataGraph();
        in.close();
        return dataGraph;
    }

    protected void saveDataGraph(DataGraph dg, File f) throws Exception
    {
        OutputStream out = new FileOutputStream(f);
        XMLHelper xmlHelper = context.getXMLHelper();
        XMLDocument doc = xmlHelper.createDocument(dg.getRootObject(), 
            Names.URI_SDO, Names.SDO_DATAGRAPH);
        doc.setXMLDeclaration(false);
        xmlHelper.save(doc, out, new Options().setSavePrettyPrint());
        out.close();
    }

    protected void saveDataObject(DataObject dobj,
                                  String rootElementURI,
                                  String rootElementName,
                                  File f)
        throws Exception
    {
        OutputStream out = new FileOutputStream(f);
        XMLHelper xmlHelper = context.getXMLHelper();
        XMLDocument doc = xmlHelper.createDocument(dobj, 
            rootElementURI, rootElementName);
        doc.setXMLDeclaration(false);
        xmlHelper.save(doc, out, new Options().setSavePrettyPrint());
        out.close();
    }

    protected void saveDataObject(DataObject dobj,
                                  String rootElementURI,
                                  String rootElementName,
                                  Writer out)
        throws Exception
    {
        XMLHelper xmlHelper = context.getXMLHelper();
        XMLDocument doc = xmlHelper.createDocument(dobj, 
            rootElementURI, rootElementName);
        doc.setXMLDeclaration(false);
        xmlHelper.save(doc, out, new Options().setSavePrettyPrint());
    }
}
