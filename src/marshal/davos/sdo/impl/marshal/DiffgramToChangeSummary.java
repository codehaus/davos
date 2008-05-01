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
package davos.sdo.impl.marshal;

import org.apache.xmlbeans.impl.common.QNameHelper;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.namespace.QName;
import javax.imageio.metadata.IIOMetadataNode;
import java.util.*;
import java.io.FileOutputStream;
import java.io.File;
import java.io.IOException;

import davos.sdo.impl.common.Names;

/**
 * Does the conversion of an ADO diffgram to a SDO change summary
 *
 * @author		Warren Wong(wawong@bea.com)
 */
public class DiffgramToChangeSummary
{
    private static final char INDENT[] = new char[20];
    private static final String NEWLINE;

    static
    {
        NEWLINE = WriterSaver.getSafeNewLine();
        int length = NEWLINE.length();
        NEWLINE.getChars(0, length, INDENT, 0);
        for (int i = length; i < 20; i++)
            INDENT[i] = ' ';
    }
    /**
     * A global id that keeps track of the paths to the items being deleted.
     * Used for effiency and code complexity reasons.
     */
    private List<String> deletedPaths = new ArrayList<String>();
    private SimpleNamespaceHelper nshelper = new SimpleNamespaceHelper();

    /**
     * A class which does the comparison of two elements based on their row order.
     * Used during sorting.
     */
    public class RowOrderComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            Element elem1 = (Element)o1;
            Element elem2 = (Element)o2;
            int rowOrder1 = Integer.parseInt(elem1.getAttribute("msdata:rowOrder"));
            int rowOrder2 = Integer.parseInt(elem2.getAttribute("msdata:rowOrder"));
            if(rowOrder1 < rowOrder2)
                return -1;
            else if (rowOrder1 > rowOrder2)
                return 1;
            else
                return 0;
        }

        public boolean equals(Object o) {
            return o instanceof RowOrderComparator;
        }
    }

    /**
     * This class is similar to NamespaceSupport
     * The difference is that all the namespace declarations are added at the same level
     * This is used in order to provide prefixes for building XPaths
     * Because the code is generated "bottom-up" meaning building blocks of the change summary
     * are assembled before the <changeSummary> element itself is generated along with the
     * "created" and "deleted" attributes, and because these attributes will need prefix
     * declarations, we can simplify things by adding all prefix declarations needed for XPaths
     * (the "created" and "deleted" attributes on the <changeSummary> and the various "sdo:ref"
     * attributes) at this level so that they are visible throughout the changeSummary content.
     * In addition to that, the code makes an important assumption: that all prefix declarations
     * are default prefix declarations. Since inside XPaths the default prefix has no significance,
     * it means that all prefixes created by this class will be non-default prefixes and that they
     * will NOT collide with prefix declarations on elements inside the change summary. This
     * eliminates the need to keep a prefix stack containing all "current" prefix declarations in
     * order to avoid redeclaring a prefix already in use.
     */
    private static class SimpleNamespaceHelper
    {
        private List<String> prefixes;
        private List<String> uris;
        private Set<String> usedPrefixes;

        SimpleNamespaceHelper()
        {
            prefixes = new ArrayList<String>();
            uris = new ArrayList<String>();
            usedPrefixes = new HashSet<String>();
            usedPrefixes.add("sdo");
        }

        public String ensurePrefix(String namespaceURI)
        {
            if (namespaceURI == null || namespaceURI.length() == 0)
                return null;
            String result = searchPrefix(namespaceURI);
            if (result != null)
                return result;
            result = QNameHelper.suggestPrefix(namespaceURI);
            if (usedPrefixes.contains(result))
            {
                int i = 2;
                while (usedPrefixes.contains(result + i))
                    i++;
                result = result + i;
            }
            addMapping(result, namespaceURI);
            usedPrefixes.add(result);
            return result;
        }

        private String searchPrefix(String namespaceURI)
        {
            for (int i = 0; i < uris.size(); i++)
            {
                if (uris.get(i).equals(namespaceURI))
                    return prefixes.get(i);
            }
            return null;
        }

        private void addMapping(String prefix, String uri)
        {
            prefixes.add(prefix);
            uris.add(uri);
        }

        public void clear()
        {
            prefixes.clear();
            uris.clear();
            usedPrefixes.clear();
            usedPrefixes.add("sdo");
        }

        public void addPrefixDeclarations(StringBuilder s)
        {
            for (int i = 0; i < prefixes.size(); i++)
                s.append(' ').append(Names.XMLNS).append(':').append(prefixes.get(i)).
                    append('=').append('"').append(uris.get(i)).append('"');
        }
    }

    public static void main(String args[])
    {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        //factory.setValidating(true);
        factory.setNamespaceAware(true);
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            FileOutputStream output = new FileOutputStream("xqueryResult.xml");
            Document document = builder.parse( new File(args[0]) );
            DiffgramToChangeSummary converter = new DiffgramToChangeSummary();
            String[] convertedDoc = converter.doConversion(document);
            for(int outputIdx=0; outputIdx<convertedDoc.length; outputIdx++)
                 output.write(convertedDoc[outputIdx].getBytes());
        } catch (SAXException sxe) {
           // Error generated during parsing)
           Exception  x = sxe;
           if (sxe.getException() != null)
               x = sxe.getException();
           x.printStackTrace();

        } catch (ParserConfigurationException pce) {
            // Parser with specified options can't be built
            pce.printStackTrace();

        } catch (IOException ioe) {
           // I/O error
           ioe.printStackTrace();
        }
    }

    // Gets all child nodes of type element
    public List getAllChildElements (Element root) {
        List returnNodeList = new ArrayList();
        NodeList childNodesOfRoot = root.getChildNodes();
        for(int childIdx = 0; childIdx<childNodesOfRoot.getLength(); childIdx++) {
            Node curChildNode = childNodesOfRoot.item(childIdx);
            if(curChildNode.getNodeType() == Node.ELEMENT_NODE)
                returnNodeList.add(curChildNode);
        }
        return returnNodeList;
    }

    // Gets the ith child element of root
    public Element getChildElement (Element root, int i) {
        NodeList childNodesOfRoot = root.getChildNodes();
        int childCount=1;
        for(int childIdx = 0; childIdx<childNodesOfRoot.getLength(); childIdx++) {
            Node curChildNode = childNodesOfRoot.item(childIdx);
            if(curChildNode.getNodeType() == Node.ELEMENT_NODE) {
                if(childCount == i)
                    return (Element)curChildNode;
                childCount ++;
            }
        }
        return null;
    }

    /**
     * Do the conversion of diffgram -> changesummary given a diffgram.
     * @param document			the document representing the diffgram
     * @return					the change summary in XML form
     */
    public String[] doConversion(Document document)
    {
        return doConversion(document.getDocumentElement());
    }

    /**
     * Do the conversion of diffgram -> changesummary given a diffgram.
     * @param rootElement		the root element of the document representing the diffgram
     * @return					the change summary in XML form
     */
    public String[] doConversion(Element rootElement) {
        List childElements = getAllChildElements(rootElement);

        ArrayList returnStrings = new ArrayList();
        if(childElements != null && childElements.size() > 0) {

            Element DSElem = (Element)childElements.get(0);
            Element diffgramBefore = new IIOMetadataNode();
            if(childElements.size() == 2)
                diffgramBefore = (Element)childElements.get(1);
            String rootNamespace = DSElem.getAttribute("xmlns");

            //make a set of all the diffgram ids in diffgramChanges
            //which we need to determine which nodes are deleted at the top
            HashSet diffgrids = new HashSet();
            Stack elementToProcess = new Stack();
            elementToProcess.push(DSElem);
            while(!elementToProcess.empty()) {
                Element curElem = (Element)elementToProcess.pop();
                List childElems = getAllChildElements(curElem);
                for(int childIdx=0;childIdx<childElems.size(); childIdx++)
                    elementToProcess.push(childElems.get(childIdx));
                diffgrids.add(curElem.getAttribute("diffgr:id"));
            }

            //break up the diffgram before elements into corresponding sets
            HashMap parentIdToDeletedNodes = new HashMap();
            HashMap deletedNodeToRowNumber = new HashMap();
            HashMap diffgramBeforeModified = new HashMap();
            List diffgramBeforeDeletedTop = new ArrayList();
            NodeList diffgramBeforeNodes = diffgramBefore.getChildNodes();
            for(int nodeIdx=0; nodeIdx<diffgramBeforeNodes.getLength(); nodeIdx++) {
                Node curChildNode = diffgramBeforeNodes.item(nodeIdx);
                if(curChildNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element curChildElement = (Element)curChildNode;
                    if(!curChildElement.getAttribute("diffgr:parentId").equals("")) {
                        String parentid = curChildElement.getAttribute("diffgr:parentId");
                        if(!parentIdToDeletedNodes.containsKey(parentid)) {
                            HashMap nodeNameToDeletedNodes = new HashMap();
                            List childElems = new ArrayList();
                            childElems.add(curChildElement);
                            nodeNameToDeletedNodes.put(new QName(curChildElement.getNamespaceURI(),
                                curChildElement.getLocalName()),childElems);
                            parentIdToDeletedNodes.put(parentid,nodeNameToDeletedNodes);
                        }
                        else {
                            HashMap nodeNameToDeletedNodes = (HashMap)parentIdToDeletedNodes.get(parentid);
                            QName qname = new QName(curChildElement.getNamespaceURI(), curChildElement.getLocalName());
                            if(!nodeNameToDeletedNodes.containsKey(qname)) {
                                List childElems = new ArrayList();
                                childElems.add(curChildElement);
                                nodeNameToDeletedNodes.put(qname,childElems);
                            }
                            else {
                                List childElems = (List)nodeNameToDeletedNodes.get(qname);
                                childElems.add(curChildElement);
                            }
                        }

                    }
                    else if (!diffgrids.contains(curChildElement.getAttribute("diffgr:id"))) {
                        diffgramBeforeDeletedTop.add(curChildElement);
                    }
                    else
                        diffgramBeforeModified.put(curChildElement.getAttribute("diffgr:id"),
                                                   curChildElement);
                }
            }

            returnStrings = genDeletedDatagraphs(diffgramBeforeDeletedTop,parentIdToDeletedNodes,rootNamespace);

            //create the output string
            List allDiffgramChanges = getAllChildElements(DSElem);
            for(int changeIdx=0;changeIdx<allDiffgramChanges.size();changeIdx++) {
                Element curDiffgramChange = (Element)allDiffgramChanges.get(changeIdx);
                deletedPaths.clear();
                nshelper.clear();
                String prefix = nshelper.ensurePrefix(curDiffgramChange.getNamespaceURI());
                String path;
                String deletesPath;
                if (prefix == null)
                {
                    path = PREPEND_PATH + '/' + curDiffgramChange.getLocalName();
                    deletesPath = PREPEND_PATH_CS + '/' + curDiffgramChange.getLocalName() + "[1]";
                }
                else
                {
                    path = PREPEND_PATH + '/' + prefix + ':' + curDiffgramChange.getLocalName();
                    deletesPath = PREPEND_PATH_CS + '/' + prefix + ':' + curDiffgramChange.getLocalName() + "[1]";
                }
                String output = genDatagraph(curDiffgramChange,path,deletesPath,diffgramBeforeModified,parentIdToDeletedNodes,rootNamespace);
                if(output.length() > 0)
                    returnStrings.add(output);
            }
        }

        return (String[])returnStrings.toArray(new String[returnStrings.size()]);
    }

    /**
     * Generates the change summary given an element in the diffgram
     * @param curNode					the diffgram node to generate a change summary from
     * @param path						the path to the current node
     * @param diffgramBeforeModified	an index from modified nodes to their previous contents
     * @param parentIdToDeletedNodes	an index from parent id of a deleted node to their previous contents
     * @param rootNamespace				the namespace of the root element
     * @return							the output xml
     */
    private String genDatagraph(Element curNode,
                                String path,
                                String deletesPath,
                                HashMap diffgramBeforeModified,
                                HashMap parentIdToDeletedNodes,
                                String rootNamespace) {

        StringBuffer output = new StringBuffer();
        genDatagraphBody(output,curNode,path,deletesPath,diffgramBeforeModified,
            parentIdToDeletedNodes,new HashMap<QName,Integer>());

        //generate the inserted list ids
        StringBuffer insertedList = new StringBuffer();
        genInsertedItems(insertedList,curNode,path);

        //generate the deleted list ids
        StringBuffer deletedList = new StringBuffer();
        for(int delId=0; delId<deletedPaths.size(); delId++)
             deletedList.append(" "+deletedPaths.get(delId));

        //get the original tree
        StringBuffer diffgramAfter = new StringBuffer();
        if(curNode.getAttribute("xmlns").equals(""))
            curNode.setAttribute("xmlns",rootNamespace);
        printEntireTree(diffgramAfter,curNode);

        return createChangeSummary(insertedList.toString().trim(),
                                   deletedList.toString().trim(),
                                   output.toString(),
                                   diffgramAfter.toString());
    }

    /**
     * Helper function for genDatagraph.
     * Generates a list of inserted items
     * @param output			the output buffer to recieve the paths to the elements
     * @param curNode			the current to process
     * @param path				the path to the current element
     */
    private void genInsertedItems(StringBuffer output,Element curNode,String path) {
        List childElems = getAllChildElements(curNode);
        if(curNode.getAttribute("diffgr:hasChanges").equals("inserted") && childElems != null && !isSimpleType(curNode))
            output.append(path + " ");

        //build a map to keep track of the count for each group name
        if(childElems != null) {
            HashMap nodeNameToCount = new HashMap();
            for(int childIdx=0;childIdx<childElems.size();childIdx++) {
                Element curElem = (Element)childElems.get(childIdx);
                Integer curCount = new Integer(1);
                if(nodeNameToCount.get(curElem.getLocalName()) == null)
                    nodeNameToCount.put(curElem.getLocalName(),curCount);
                else {
                    curCount = (Integer)nodeNameToCount.get(curElem.getLocalName());
                    curCount = new Integer(curCount.intValue() + 1);
                    nodeNameToCount.put(curElem.getLocalName(),curCount);
                }

                String newPath = generateXPath(path, curElem, curCount);
                genInsertedItems(output,curElem,newPath);
            }
        }
    }

    /**
     * Checks to see if an element is a simple type
     *
     * @param curElem		the element to check
     * @return				true if simple, false otherwise
     */
    private boolean isSimpleType(Element curElem) {
        StringBuffer temp = new StringBuffer();
        genAttributeList(temp,curElem,false);
        if(temp.toString().trim().equals("") &&
           getAllChildElements(curElem).size() == 0)
            return true;
        return false;
    }

    /**
     * Helper function for genDatagraph.
     *
     * Generates the change summary body given a node in the diffgram.
     * @param outputCS					a string buffer to dump the output xml into
     * @param curNode					the diffgram node to generate a change summary from
     * @param path						the path to the current node
     * @param diffgramBeforeModified	an index from modified nodes to their previous contents
     * @param parentIdToDeletedNodes	an index from parent id of a deleted node to their previous contents
     * @param modifiedObjectsCount      counts how many elements with a given QName appear as direct
     *     children of the <changeSummary> elements; used to compute Xpaths to deleted objects
     */
    private void genDatagraphBody(StringBuffer outputCS,
                                  Element curNode,
                                  String path,
                                  String deletesPath,
                                  HashMap diffgramBeforeModified,
                                  HashMap parentIdToDeletedNodes,
                                  HashMap<QName, Integer> modifiedObjectsCount) {

        //group the children by their node names (qualified name)
        HashMap<QName, HashSet<Element>> nodeNameToChildElems = new HashMap<QName, HashSet<Element>>();
        List childElems = getAllChildElements(curNode);
        if (childElems != null) {
            for(int childIdx=0;childIdx<childElems.size();childIdx++) {
                Element curChild = (Element)childElems.get(childIdx);
                if(!curChild.getAttribute("diffgr:id").equals("")) {
                    QName childQName = new QName(curChild.getNamespaceURI(), curChild.getLocalName());
                    if(nodeNameToChildElems.get(childQName) == null) {
                        HashSet<Element> nodeNameSet = new HashSet<Element>();
                        nodeNameSet.add(curChild);
                        nodeNameToChildElems.put(childQName,nodeNameSet);
                    }
                    else {
                        HashSet<Element> nodeNameSet = nodeNameToChildElems.get(childQName);
                        nodeNameSet.add(curChild);
                    }
                }
            }
        }

        //do a diff if this element has been modified
        StringBuffer summarybody = new StringBuffer();
        StringBuilder unsetString = new StringBuilder();
        Element curNodeBefore = (Element)diffgramBeforeModified.get(curNode.getAttribute("diffgr:id"));
        if(curNode.getAttribute("diffgr:hasChanges").equals("modified")) {

            //create an index for the values of the elements before this
            HashMap modifiedSimpleElements = new HashMap();
            List curNodeBeforeChildElems = getAllChildElements(curNodeBefore);
            for(int childIdx=0;childIdx<curNodeBeforeChildElems.size();childIdx++) {
                Element curChildElem = (Element)curNodeBeforeChildElems.get(childIdx);
                modifiedSimpleElements.put(curChildElem.getLocalName(),curChildElem);
            }

            //Do the join of elements before & after
            for(int childIdx=0; childIdx<childElems.size();childIdx++) {
                Element curChildElem = (Element)childElems.get(childIdx);

                if(curChildElem.getAttribute("diffgr:id").equals("")) {
                    Element curChildElemBefore = (Element)modifiedSimpleElements.get(curChildElem.getLocalName());
                    modifiedSimpleElements.remove(curChildElem.getLocalName());
                    if(curChildElemBefore != null) {
                        Node curText = curChildElem.getLastChild();
                        Node curTextBefore = curChildElemBefore.getLastChild();
                        if((curText == null && curTextBefore != null) ||
                           (curText != null && curTextBefore == null) ||
                           (curText != null && curTextBefore != null &&
                            !curText.getNodeValue().equals(curTextBefore.getNodeValue()))) {
                               // The name can be namespace-qualified
                               String nsUri = curChildElem.getNamespaceURI();
                               String prefix = nshelper.ensurePrefix(nsUri);
                               if (prefix == null)
                                  summarybody.append("<" + curChildElem.getLocalName());
                               else
                                  summarybody.append('<').append(prefix).append(':').
                                      append(curChildElem.getLocalName());
                               genAttributeList(summarybody,curChildElem,false);
                               summarybody.append(">");
                               if(curTextBefore != null)
                                summarybody.append(curTextBefore.getNodeValue());
                            summarybody.append("</");
                            if (prefix != null)
                                summarybody.append(prefix).append(':');
                            summarybody.append(curChildElem.getLocalName() + ">");
                        }

                    }
                    else {
//                        summarybody.append("<" + curChildElem.getLocalName());
//                        summarybody.append(" ns0:nil=\"true\" xmlns:ns0=\"http://www.w3.org/2001/XMLSchema-instance\">");
//                        summarybody.append("</" + curChildElem.getLocalName() + ">");
                        unsetString.append(' ').append(curChildElem.getLocalName());
                    }
                }
            }

            //Print out the extra elements left in the before version (these are the ones that are deleted)
            Element[] extraDeletedElements = (Element[])modifiedSimpleElements.values().toArray(new Element[modifiedSimpleElements.size()]);
            for(int elemIdx=0;elemIdx<extraDeletedElements.length;elemIdx++) {
                Element curElem = extraDeletedElements[elemIdx];
                createElementNode(summarybody,curElem,null,curElem.getLastChild().getNodeValue(),null,null);
            }
        }

        //process each group of child nodes
        Set<QName> nodeGroups = nodeNameToChildElems.keySet();
        Iterator<QName> nodeGroupIt = nodeGroups.iterator();
        StringBuffer restOfNodes = new StringBuffer();
        while(nodeGroupIt.hasNext()) {

            //get the group and sort it by the row order
            QName curNodeGroupName = nodeGroupIt.next();
            HashSet<Element> curNodeGroupSet = nodeNameToChildElems.get(curNodeGroupName);
            Object[] curNodeGroup = curNodeGroupSet.toArray();
            Arrays.sort(curNodeGroup,new RowOrderComparator());

            //check if the ordering for this group has changed by seeing if
            //there are any nodes inserted or deleted
            boolean hasInserted = false;
            boolean isSimpleType = false;
            for(int nodeIdx=0;nodeIdx<curNodeGroup.length;nodeIdx++) {
                Element curElem = (Element)curNodeGroup[nodeIdx];
                if(curElem.getAttribute("diffgr:hasChanges").equals("inserted"))
                    hasInserted = true;
                if(isSimpleType(curElem) && !curElem.getAttribute("diffgr:hasChanges").equals(""))
                    isSimpleType = true;
                if(hasInserted && isSimpleType)
                    break;
            }
            boolean hasDeleted = false;
            HashMap nodeGroupsToDelete = (HashMap)parentIdToDeletedNodes.get(curNode.getAttribute("diffgr:id"));
            if (nodeGroupsToDelete != null)
                hasDeleted = nodeGroupsToDelete.get(curNodeGroupName) != null;
            boolean orderChanged = hasInserted || hasDeleted || isSimpleType;
            if(orderChanged) {
                //look up the nodes that were deleted under this one
                HashMap deletedSubNodeGroups = (HashMap)parentIdToDeletedNodes.get(curNode.getAttribute("diffgr:id"));
                Object[] deletedNodes = new Element[0];
                if(deletedSubNodeGroups != null) {
                    List deletedSubNodes = (List)deletedSubNodeGroups.get(curNodeGroupName);
                    if(deletedSubNodes != null) {
                        deletedSubNodeGroups.remove(curNodeGroupName);
                        deletedNodes = deletedSubNodes.toArray();
                        Arrays.sort(deletedNodes,new RowOrderComparator());
                    }
                }

                //loop through the group of child nodes
                int childIdx=0,delIdx=0;
                RowOrderComparator compareRowOrder = new RowOrderComparator();
                while(childIdx<curNodeGroup.length || delIdx<deletedNodes.length) {

                    Element curChild=null;
                    Element curDeletedNode=null;
                    if(childIdx < curNodeGroup.length)
                        curChild = (Element)curNodeGroup[childIdx];
                    if(delIdx<deletedNodes.length)
                        curDeletedNode = (Element)deletedNodes[delIdx];

                    if((curChild != null && curDeletedNode != null && compareRowOrder.compare(curChild,curDeletedNode) < 0) ||
                       (curChild != null && curDeletedNode == null)) {
                        if(!curChild.getAttribute("diffgr:hasChanges").equals("inserted")) {

                            if(isSimpleType) {
                                String nextpath = generateXPath(path, curChild, childIdx+1);
                                Element diffgramBeforeCurChild = (Element)diffgramBeforeModified.get(curChild.getAttribute("diffgr:id"));
                                String nodeValue = "";
//                                if(diffgramBeforeCurChild.getLastChild() != null)
//                                    nodeValue = diffgramBeforeCurChild.getLastChild().getNodeValue();
                                createElementNode(summarybody,
                                                  diffgramBeforeCurChild,
                                                  null,
                                                  nodeValue,
                                                  nextpath,null);
                            }
                            else {
                                String nextpath = generateXPath(path, curChild, childIdx+1);
                                // Put a reference to this element inside the current modified object
                                // but also generate a top-level entry for this object as well
                                // because it may contain modifications of its own
                                createElementNode(summarybody,curChild,null,"",nextpath,null);
                                String nextDeletesPath = generateXPathForDelete(
                                    deletesPath.substring(0, deletesPath.lastIndexOf('/')), curChild,
                                        modifiedObjectsCount);
                                genDatagraphBody(restOfNodes,curChild,nextpath,nextDeletesPath,
                                    diffgramBeforeModified,parentIdToDeletedNodes,
                                    modifiedObjectsCount);
                            }
                        }
                        childIdx++;
                    }
                    else {
                        if(isSimpleType) {
//                            String nextpath = generateXPath(path, curDeletedNode, childIdx+delIdx+1);
                            createElementNode(summarybody,curDeletedNode,null,curDeletedNode.getLastChild().getNodeValue(),null,null);
                        }
                        else if(!(parentIdToDeletedNodes.get(curNode.getAttribute("diffgr:id")) == null &&
                                 getAllChildElements(curNode).size() == 0)) {
                            String xpath = generateXPath(deletesPath, curDeletedNode, childIdx+delIdx+1);
                            createDeletedNode(summarybody,curDeletedNode,parentIdToDeletedNodes,curNode.getNamespaceURI()!=null,xpath);
                        }
                        delIdx++;
                    }
                }
            }
               else {
                   for(int childIdx=0; childIdx<curNodeGroup.length;childIdx++) {
                       Element curChild = (Element)curNodeGroup[childIdx];
                       String nextpath = generateXPath(path, curChild, childIdx+1);
                       String nextDeletesPath = generateXPathForDelete(
                           deletesPath.substring(0, deletesPath.lastIndexOf('/')), curChild,
                               modifiedObjectsCount);
                       genDatagraphBody(restOfNodes,
                                        curChild,
                                        nextpath,
                                        nextDeletesPath,
                                        diffgramBeforeModified,
                                        parentIdToDeletedNodes,
                                        modifiedObjectsCount);
                   }
               }
        }

        //remove the rest of the deleted nodes
         HashMap childNodeGroups = (HashMap)parentIdToDeletedNodes.get(curNode.getAttribute("diffgr:id"));
        if (childNodeGroups != null) {
            Iterator childNodeGroupIt = childNodeGroups.values().iterator();
            while(childNodeGroupIt.hasNext()) {
                List childNodes = (List)childNodeGroupIt.next();
                if(childNodes != null) {
                    for(int childIdx=0; childIdx<childNodes.size(); childIdx++) {
                        Element curChildNode = (Element)childNodes.get(childIdx);
                        String xpath = generateXPath(deletesPath, curChildNode, childIdx+1);
                        createDeletedNode(summarybody,curChildNode,parentIdToDeletedNodes,curChildNode.getNamespaceURI()!=null,xpath);
                    }
                }
            }
        }

        String changedAttrList = "";
        if(curNode.getAttribute("diffgr:hasChanges").equals("modified"))
            changedAttrList = genChangedAttributeList(curNode,curNodeBefore);
        if (summarybody.length() > 0 ||
            unsetString.length() > 0 ||
            (curNode.getAttribute("diffgr:hasChanges").equals("modified") &&
               changedAttrList.length() > 0))
        {
            String unset = unsetString.length() > 0 ? unsetString.toString().trim() : null;
            QName qname;
            if(curNodeBefore != null)
            {
                qname = new QName(curNodeBefore.getNamespaceURI(), curNodeBefore.getLocalName());
                createElementNode(outputCS,curNodeBefore,changedAttrList,summarybody.toString(),path,unset);
            }
            else
            {
                qname = new QName(curNode.getNamespaceURI(), curNode.getLocalName());
                createElementNode(outputCS,curNode,changedAttrList,summarybody.toString(),path,unset);
            }
            if (modifiedObjectsCount.containsKey(qname))
                modifiedObjectsCount.put(qname, modifiedObjectsCount.get(qname) + 1);
            else
                modifiedObjectsCount.put(qname, 1);
        }
        outputCS.append(restOfNodes);
    }

    /**
     * See if any attributes are different between the two nodes.
     * @return		true if different, false otherwise.
     */
    private String genChangedAttributeList(Element node1,Element node2) {

        //loop through the attributes of node2 (need to do this to remove the prefix)
        HashMap node2Nodes = new HashMap();
        NamedNodeMap attributes2 = node2.getAttributes();
        for(int childIdx=0; childIdx<attributes2.getLength(); childIdx++) {
            Node curChildNode = attributes2.item(childIdx);
            String curNodeName = curChildNode.getNodeName();
            if(curNodeName.lastIndexOf(':') != -1)
                curNodeName = curNodeName.substring(curNodeName.lastIndexOf(':')+1);
            node2Nodes.put(curNodeName,curChildNode.getNodeValue());
        }

        //now do the diff
        StringBuffer changedAttributeList = new StringBuffer();
        NamedNodeMap attributes = node1.getAttributes();
        for(int childIdx=0; childIdx<attributes.getLength(); childIdx++) {
            Node curChildNode = attributes.item(childIdx);
            String curNamespaceURI = curChildNode.getNamespaceURI();
            if(curNamespaceURI == null ||
               (!curNamespaceURI.equals("urn:schemas-microsoft-com:xml-msdata") &&
               !curNamespaceURI.equals("urn:schemas-microsoft-com:xml-diffgram-v1") &&
               !curNamespaceURI.equals("http://www.w3.org/2000/xmlns/"))) {
//                String curNodeName = curChildNode.getNodeName();
//                if(curNodeName.lastIndexOf(':') != -1)
//                    curNodeName = curNodeName.substring(curNodeName.lastIndexOf(':')+1);
                String curNodeName = curChildNode.getLocalName();
                String nsUri = curChildNode.getNamespaceURI();
                String node2Value = (String)node2Nodes.get(curNodeName);
                if (nsUri != null)
                    curNodeName = nshelper.ensurePrefix(nsUri) + ':' + curNodeName;
                if(!node2Value.equals(curChildNode.getNodeValue()))
                    changedAttributeList.append(" " + curNodeName + "=\"" + node2Value + "\"");
            }
        }
        return changedAttributeList.toString();
    }

    /**
     * Helper function. Creates a new element given the old element node and the path and body to replace it with.
     *
     * @param output				the output buffer to place the new element node into
     * @param curNode				the current element node
     * @param attrList				the new attribute list for this element
     * @param body					the new body of the new element node
     * @param path					the path to the current element node
     */
    private void createElementNode(StringBuffer output,Element curNode,String attrList,String body,String path,String unset) {
        // Try and reuse the old node's prefix if != "sdo" and != empty string
        // We don't want to create problems by binding the default prefix
        String prefix = nshelper.ensurePrefix(curNode.getNamespaceURI());
        String name;
        if (prefix == null)
            name = curNode.getLocalName();
        else
            name = prefix + ':' + curNode.getLocalName();
        output.append('<').append(name);
        if(path != null)
            output.append(" sdo:ref=\"" + path + "\"");
        if(unset != null)
            output.append(" sdo:unset=\"" + unset + '"');
        if(attrList != null)
            output.append(attrList);
        else
            genAttributeList(output,curNode,false);
        output.append(">");
        output.append(body);
        output.append("</" + name + ">");
    }

    /**
     * Generates a list of attributes given the current element.
     *
     * @param output		the output buffer to append the attribute list to
     * @param curNode 		the node whose attributes we want
     * @param preserveNS	return NS as an attribute?
     */
    private void genAttributeList(StringBuffer output,Element curNode,boolean preserveNS)  {
        NamedNodeMap attributes = curNode.getAttributes();
        for(int childIdx=0; childIdx<attributes.getLength(); childIdx++) {
            Node curChildNode = attributes.item(childIdx);
            String curNamespaceURI = curChildNode.getNamespaceURI();
            boolean append;
            if (curNamespaceURI == null)
                append = true;
            else if (curNamespaceURI.equals(Names.URI_DOM_XMLNS))
                if (preserveNS)
                    append = true;
                else
                {
                    append = false;
//                    if (!Names.XMLNS.equals(curChildNode.getNodeName()))
//                        // Safeguard so that we now if non-default prefix declarations are used
//                        throw new IllegalStateException("Non-default prefix declarations not "+
//                            "supported in this context: " + curChildNode.getNodeName() + "=" +
//                            curChildNode.getNodeValue());
                }
            else if (curNamespaceURI.equals("urn:schemas-microsoft-com:xml-msdata") ||
                curNamespaceURI.equals("urn:schemas-microsoft-com:xml-diffgram-v1"))
                append = false;
            else
                append = true;
            if (append)
                output.append(" " + curChildNode.getNodeName() + "=\"" + curChildNode.getNodeValue() + "\"");
        }
    }

    /**
     * Generates a list of datagraphs given a list of input elements
     * which are elements that are deleted from the very root node (the DS it self)
     * These are treated specially because they require a separate datagraph each
     * and do not have parent ids in the diffgram.
     *
     * @param deletedTopNodes			the list of deleted top level nodes that we are generating diffgrams for
     * @param parentIdToDeletedNodes 	an index from the parent ids of deleted nodes to their content
     * @param rootNamespace				the namespace of top level deleted nodes
     *
     * @return							an array of datagraphs that is the result of the conversion
     */
    private ArrayList genDeletedDatagraphs(List deletedTopNodes,HashMap parentIdToDeletedNodes,String rootNamespace) {
        ArrayList datagraphs = new ArrayList(deletedTopNodes.size());
        for(int delIdx=0; delIdx<deletedTopNodes.size(); delIdx++) {
            Element curNode = (Element)(deletedTopNodes.get(delIdx));
            curNode.setAttribute("xmlns",rootNamespace);
            deletedPaths.clear();
            nshelper.clear();
            StringBuffer body = new StringBuffer();
            // Create a replica of the top-level SDO datagraph with all the deleted nodes as children
            body.append("<sdo:datagraph sdo:ref=\"#/sdo:datagraph\">");
            String path;
            String prefix = nshelper.ensurePrefix(curNode.getAttribute("xmlns"));
            if (prefix == null)
                path = PREPEND_PATH_CS + "/sdo:datagraph/" + curNode.getLocalName();
            else
                path = PREPEND_PATH_CS + "/sdo:datagraph/" + prefix + ':' + curNode.getLocalName();
            createDeletedNode(body,curNode,parentIdToDeletedNodes,true,path);
            body.append("</sdo:datagraph>");
            StringBuffer deletedList = new StringBuffer();
            for(int delId=0; delId<deletedPaths.size(); delId++)
                 deletedList.append(" "+deletedPaths.get(delId));

            datagraphs.add(createChangeSummary("",deletedList.toString().trim(),body.toString(),""));
        }
        return datagraphs;
    }

    /**
     * Deletes a node (not literally in the DOM) by printing it out along with its log number
     * @param output			the output buffer to print this deleted node to
     * @param curNode			the current node to delete
     * @param preserveNS		indicates whether to preserve the namespace of the node. Only used for root nodes that are deleted.
     */
    private void createDeletedNode(StringBuffer output,Element curNode,
        HashMap parentIdToDeletedNodes,boolean preserveNS,String pathToCurrent)
    {

        String name = curNode.getTagName();
        preserveNS &= name.indexOf(':') < 0;
        if(preserveNS)
            name = "ns:" + name;
        output.append("<"+name);
        genAttributeList(output,curNode,true);
        if(preserveNS)
            output.append(" xmlns:ns=\"" + curNode.getAttribute("xmlns") + "\"");
        output.append(">");
        deletedPaths.add(pathToCurrent);

        //print out the subnodes of the current node
        printTree(output,curNode);

        //process other nodes to delete that is the child node of this node
        HashMap childNodeGroups = (HashMap)parentIdToDeletedNodes.get(curNode.getAttribute("diffgr:id"));
        if (childNodeGroups != null) {
            Iterator childNodeGroupIt = childNodeGroups.values().iterator();
            while(childNodeGroupIt.hasNext()) {
                List childNodes = (List)childNodeGroupIt.next();
                if(childNodes != null) {
                    for(int childIdx=0; childIdx<childNodes.size(); childIdx++) {
                        Element curChildNode = (Element)childNodes.get(childIdx);
                        String path = generateXPath(pathToCurrent, curChildNode, childIdx+1);
                        createDeletedNode(output,curChildNode,parentIdToDeletedNodes,
                            curChildNode.getNamespaceURI()!=null,path);
                    }
                }
            }
        }

        output.append("</"+name+">");
    }

    private void printTree(StringBuffer output,Element curNode) {
        NodeList childNodes = curNode.getChildNodes();
        for(int childIdx = 0; childIdx<childNodes.getLength(); childIdx++) {
            Node curChildNode = childNodes.item(childIdx);
            if(curChildNode.getNodeType() == Node.ELEMENT_NODE) {
                String tagName = curChildNode.getLocalName();
                boolean NS = false;
                if (tagName.equals(curChildNode.getNodeName()) && curChildNode.getNamespaceURI() != null)
                {
                    tagName = "ns:" + tagName;
                    NS = true;
                }
                output.append("<" +  tagName);
                genAttributeList(output,(Element)curChildNode,true);
                if (NS)
                    output.append(" xmlns:ns=\"" + curChildNode.getNamespaceURI() + "\"");
                output.append(">");
                printTree(output,(Element)curChildNode);
                output.append("</" +  tagName + ">");
            }
            if(curChildNode.getNodeType() == Node.TEXT_NODE)
                output.append(curChildNode.getNodeValue());
        }
    }

    private void printEntireTree(StringBuffer output, Element root) {
        output.append("<" + root.getTagName());
        genAttributeList(output,root,true);
        output.append(">");

        NodeList childNodes = root.getChildNodes();
        for(int childIdx=0; childIdx<childNodes.getLength(); childIdx++) {
            Node curChildNode = childNodes.item(childIdx);
            if(curChildNode.getNodeType() == Node.ELEMENT_NODE)  {
                printEntireTree(output,(Element)curChildNode);
            }
            if(curChildNode.getNodeType() == Node.TEXT_NODE)
                output.append(curChildNode.getNodeValue());
        }
        output.append("</" + root.getTagName() + ">");
    }

    private String generateXPath(String prependPath, Element elem, int index)
    {
        String prefix = nshelper.ensurePrefix(elem.getNamespaceURI());
        if (prefix == null)
            return prependPath + '/' + elem.getLocalName() + '[' + index + ']';
        else
            return prependPath + '/' + prefix + ':' + elem.getLocalName() + '[' + index + ']';
    }

    private String generateXPathForDelete(String prependPath, Element elem,
        Map<QName, Integer> qnameCount)
    {
        QName qname = new QName(elem.getNamespaceURI(), elem.getLocalName());
        int count;
        if (qnameCount.containsKey(qname))
            count = qnameCount.get(qname) + 1;
        else
            count = 1;
        return generateXPath(prependPath, elem, count);
    }

    private static final String PREPEND_PATH = "#/sdo:datagraph";
    private static final String PREPEND_PATH_CS = "#/sdo:datagraph/changeSummary";

    private String createChangeSummary(String insertList,String deleteList,String summary,String diffgramAfter) {
        StringBuilder s = new StringBuilder();
        int indent = NEWLINE.length() + 4;
        s.append("<sdo:datagraph xmlns:sdo=\"commonj.sdo\">");
        s.append(INDENT, 0, indent);
        s.append("<changeSummary create=\"").append(insertList);
        s.append("\" delete=\"").append(deleteList).append('"');
        nshelper.addPrefixDeclarations(s);
        s.append( ">");
        indent += 4;
        s.append(INDENT, 0, indent);
        s.append(summary);
        indent -= 4;
        s.append(INDENT, 0, indent);
        s.append("</changeSummary>");
        s.append(INDENT, 0, indent);
        s.append(diffgramAfter);
        indent -= 4;
        s.append(INDENT, 0, indent);
        s.append("</sdo:datagraph>");
        return s.toString();
    }
}
