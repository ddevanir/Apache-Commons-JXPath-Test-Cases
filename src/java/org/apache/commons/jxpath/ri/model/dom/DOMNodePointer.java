/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//jxpath/src/java/org/apache/commons/jxpath/ri/model/dom/DOMNodePointer.java,v 1.5 2002/05/08 23:05:05 dmitri Exp $
 * $Revision: 1.5 $
 * $Date: 2002/05/08 23:05:05 $
 *
 * ====================================================================
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 1999-2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Commons", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation and was
 * originally based on software copyright (c) 2001, Plotnix, Inc,
 * <http://www.plotnix.com/>.
 * For more information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
package org.apache.commons.jxpath.ri.model.dom;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.jxpath.AbstractFactory;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathException;
import org.apache.commons.jxpath.ri.Compiler;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.compiler.NodeNameTest;
import org.apache.commons.jxpath.ri.compiler.NodeTest;
import org.apache.commons.jxpath.ri.compiler.NodeTypeTest;
import org.apache.commons.jxpath.ri.compiler.ProcessingInstructionTest;
import org.apache.commons.jxpath.ri.model.NodeIterator;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.apache.commons.jxpath.util.TypeUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Comment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;

/**
 * A Pointer that points to a DOM node.
 *
 * @author Dmitri Plotnikov
 * @version $Revision: 1.5 $ $Date: 2002/05/08 23:05:05 $
 */
public class DOMNodePointer extends NodePointer {
    private Node node;
    private Map namespaces;
    private String defaultNamespace;

    public static final String XML_NAMESPACE_URI = "http://www.w3.org/XML/1998/namespace";
    public static final String XMLNS_NAMESPACE_URI = "http://www.w3.org/2000/xmlns/";

    public DOMNodePointer(Node node, Locale locale){
        super(null, locale);
        this.node = node;
    }

    public DOMNodePointer(NodePointer parent, Node node){
        super(parent);
        this.node = node;
    }

    public boolean testNode(NodeTest test){
        return testNode(this, node, test);
    }

    public static boolean testNode(NodePointer pointer, Node node, NodeTest test){
        if (test == null){
            return true;
        }
        else if (test instanceof NodeNameTest){
            if (node.getNodeType() != Node.ELEMENT_NODE){
                return false;
            }

            QName testName = ((NodeNameTest)test).getNodeName();
            String testLocalName = testName.getName();
            if (testLocalName.equals("*") || testLocalName.equals(DOMNodePointer.getLocalName(node))){
                String testPrefix = testName.getPrefix();
                String nodePrefix = DOMNodePointer.getPrefix(node);
                if (equalStrings(testPrefix, nodePrefix)){
                    return true;
                }

                String testNS = pointer.getNamespaceURI(testPrefix);
                String nodeNS = pointer.getNamespaceURI(nodePrefix);
                return equalStrings(testNS, nodeNS);
            }
        }
        else if (test instanceof NodeTypeTest){
            int nodeType = node.getNodeType();
            switch (((NodeTypeTest)test).getNodeType()){
                case Compiler.NODE_TYPE_NODE:
                    return nodeType == Node.ELEMENT_NODE;
                case Compiler.NODE_TYPE_TEXT:
                    return nodeType == Node.CDATA_SECTION_NODE ||
                            nodeType == Node.TEXT_NODE;
                case Compiler.NODE_TYPE_COMMENT:
                    return nodeType == Node.COMMENT_NODE;
                case Compiler.NODE_TYPE_PI:
                    return nodeType == Node.PROCESSING_INSTRUCTION_NODE;
            }
            return false;
        }
        else if (test instanceof ProcessingInstructionTest){
            if (node.getNodeType() == Node.PROCESSING_INSTRUCTION_NODE){
                String testPI = ((ProcessingInstructionTest)test).getTarget();
                String nodePI = ((ProcessingInstruction)node).getTarget();
                return testPI.equals(nodePI);
            }
        }
        return false;
    }

    private static boolean equalStrings(String s1, String s2){
        if (s1 == null && s2 != null){
            return false;
        }
        if (s1 != null && s2 == null){
            return false;
        }

        if (s1 != null && !s1.trim().equals(s2.trim())){
            return false;
        }

        return true;
    }

    public QName getName(){
        int type = node.getNodeType();
        if (type == Node.ELEMENT_NODE){
            return new QName(DOMNodePointer.getPrefix(node), DOMNodePointer.getLocalName(node));
        }
        else if (type == Node.PROCESSING_INSTRUCTION_NODE){
            return new QName(null, ((ProcessingInstruction)node).getTarget());
        }
        return null;
    }

    public String getNamespaceURI(){
        if (node.getNodeType() == Node.ELEMENT_NODE){
            return getNamespaceURI(getName().getPrefix());
        }
        return null;
    }

    public QName getExpandedName(){
        return new QName(getNamespaceURI(), getName().getName());
    }

    public NodeIterator childIterator(NodeTest test, boolean reverse, NodePointer startWith){
        return new DOMNodeIterator(this, test, reverse, startWith);
    }

    public NodeIterator attributeIterator(QName name){
        return new DOMAttributeIterator(this, name);
    }

    public NodePointer namespacePointer(String prefix){
        return new NamespacePointer(this, prefix);
    }

    public NodeIterator namespaceIterator(){
        return new DOMNamespaceIterator(this);
    }

    public String getNamespaceURI(String prefix){
        if (prefix == null || prefix.equals("")){
            return getDefaultNamespaceURI();
        }

        if (prefix.equals("xml")){
            return XML_NAMESPACE_URI;
        }

        if (prefix.equals("xmlns")){
            return XMLNS_NAMESPACE_URI;
        }

        String namespace = null;
        if (namespaces == null){
            namespaces = new HashMap();
        }
        else {
            namespace = (String)namespaces.get(prefix);
        }

        if (namespace == null){
            String qname = "xmlns:" + prefix;
            Node aNode = node;
            while (aNode != null){
                if (aNode.getNodeType() == Node.ELEMENT_NODE){
                    Attr attr = ((Element)aNode).getAttributeNode(qname);
                    if (attr != null){
                        namespace = attr.getValue();
                        break;
                    }
                }
                aNode = aNode.getParentNode();
            }
            if (namespace == null || namespace.equals("")){
                namespace = NodePointer.UNKNOWN_NAMESPACE;
            }
        }

        namespaces.put(prefix, namespace);
        // TBD: We are supposed to resolve relative URIs to absolute ones.
        return namespace;
    }

    public String getDefaultNamespaceURI(){
        if (defaultNamespace == null){
            Node aNode = node;
            while (aNode != null){
                if (aNode.getNodeType() == Node.ELEMENT_NODE){
                    Attr attr = ((Element)aNode).getAttributeNode("xmlns");
                    if (attr != null){
                        defaultNamespace = attr.getValue();
                        break;
                    }
                }
                aNode = aNode.getParentNode();
            }
        }
        if (defaultNamespace == null){
            defaultNamespace = "";
        }
        // TBD: We are supposed to resolve relative URIs to absolute ones.
        return defaultNamespace.equals("") ? null : defaultNamespace;
    }

    public Object getBaseValue(){
        return node;
    }

    public Object getNodeValue(){
        return node;
    }

    public boolean isActual(){
        return true;
    }

    public boolean isCollection(){
        return false;
    }

    public int getLength(){
        return 1;
    }

    public boolean isLeaf(){
        return !node.hasChildNodes();
    }

    /**
     * Returns true if the xml:lang attribute for the current node
     * or its parent has the specified prefix <i>lang</i>.
     * If no node has this prefix, calls <code>super.isLanguage(lang)</code>.
     */
    public boolean isLanguage(String lang){
        String current = getLanguage();
        if (current == null){
            return super.isLanguage(lang);
        }
        return current.toUpperCase().startsWith(lang.toUpperCase());
    }

    protected String getLanguage(){
        Node n = node;
        while (n != null){
            if (n.getNodeType() == Node.ELEMENT_NODE){
                Element e = (Element)n;
                String attr = e.getAttribute("xml:lang");
                if (attr != null && !attr.equals("")){
                    return attr;
                }
            }
            n = n.getParentNode();
        }
        return null;
    }

    /**
     * Sets text contents of the node to the specified value
     */
    public void setValue(Object value){
        String string = null;
        if (value != null){
            string = (String)TypeUtils.convert(value, String.class);
            if (string.equals("")){
                string = null;
            }
        }

        if (node.getNodeType() == Node.TEXT_NODE){
            if (string != null){
                node.setNodeValue(string);
            }
            else {
                node.getParentNode().removeChild(node);
            }
        }
        else {
            NodeList children = node.getChildNodes();
            int count = children.getLength();
            for (int i = 0; i < count; i++){
                Node child = children.item(i);
                if (child.getNodeType() == Node.TEXT_NODE ||
                        child.getNodeType() == Node.CDATA_SECTION_NODE){
                    node.removeChild(child);
                }
            }
            if (string != null){
                Node text = node.getOwnerDocument().createTextNode(string);
                node.appendChild(text);
            }
        }
    }

    public NodePointer createChild(JXPathContext context, QName name, int index){
        if (index == WHOLE_COLLECTION){
            index = 0;
        }
        if (!getAbstractFactory(context).createObject(context, this, node, name.toString(), index)){
            throw new JXPathException("Factory could not create a child node for path: " +
                    asPath() + "/" + name + "[" + (index+1) + "]");
        }
        NodeIterator it = childIterator(new NodeNameTest(name), false, null);
        if (it == null || !it.setPosition(index + 1)){
            throw new JXPathException("Factory could not create a child node for path: " +
                    asPath() + "/" + name + "[" + (index+1) + "]");
        }
        return it.getNodePointer();
    }

    public NodePointer createChild(JXPathContext context, QName name, int index, Object value){
        NodePointer ptr = createChild(context, name, index);
        ptr.setValue(value);
        return ptr;
    }

    public void remove(){
        Node parent = node.getParentNode();
        if (parent == null){
            throw new JXPathException("Cannot remove root DOM node");
        }
        parent.removeChild(node);
    }

    public String asPath(){
        StringBuffer buffer = new StringBuffer();
        if (parent != null){
            buffer.append(parent.asPath());
        }
        switch(node.getNodeType()){
            case Node.ELEMENT_NODE:
                // If the parent pointer is not a DOMNodePointer, it is
                // the parent's responsibility to produce the node test part
                // of the path
                if (parent instanceof DOMNodePointer){
                    buffer.append('/');
                    buffer.append(getName());
                    buffer.append('[').append(getRelativePositionByName()).append(']');
                }
                break;
            case Node.TEXT_NODE:
            case Node.CDATA_SECTION_NODE:
                buffer.append("/text()");
                buffer.append('[').append(getRelativePositionOfTextNode()).append(']');
                break;
            case Node.PROCESSING_INSTRUCTION_NODE:
                String target = ((ProcessingInstruction)node).getTarget();
                buffer.append("/processing-instruction(\'").append(target).append("')");
                buffer.append('[').append(getRelativePositionOfPI(target)).append(']');
                break;
            case Node.DOCUMENT_NODE:
                // That'll be empty
        }
        return buffer.toString();
    }

    private int getRelativePositionByName(){
        int count = 1;
        Node n = node.getPreviousSibling();
        while (n != null){
            if (n.getNodeType() == Node.ELEMENT_NODE){
                String nm = n.getNodeName();
                if (nm.equals(node.getNodeName())){
                    count ++;
                }
            }
            n = n.getPreviousSibling();
        }
        return count;
    }

    private int getRelativePositionOfTextNode(){
        int count = 1;
        Node n = node.getPreviousSibling();
        while (n != null){
            if (n.getNodeType() == Node.TEXT_NODE || n.getNodeType() == Node.CDATA_SECTION_NODE){
                count ++;
            }
            n = n.getPreviousSibling();
        }
        return count;
    }

    private int getRelativePositionOfPI(String target){
        int count = 1;
        Node n = node.getPreviousSibling();
        while (n != null){
            if (n.getNodeType() == Node.PROCESSING_INSTRUCTION_NODE &&
                    ((ProcessingInstruction)n).getTarget().equals(target)){
                count ++;
            }
            n = n.getPreviousSibling();
        }
        return count;
    }

    public int hashCode(){
        return System.identityHashCode(node);
    }

    public boolean equals(Object object){
        if (object == this){
            return true;
        }

        if (!(object instanceof DOMNodePointer)){
            return false;
        }

        DOMNodePointer other = (DOMNodePointer)object;
        return node == other.node;
    }

    public static String getPrefix(Node node){
        String prefix = node.getPrefix();
        if (prefix != null){
            return prefix;
        }

        String name = node.getNodeName();
        int index = name.lastIndexOf(':');
        if (index == -1){
            return null;
        }

        return name.substring(0, index);
    }

    public static String getLocalName(Node node){
        String localName = node.getLocalName();
        if (localName != null){
            return localName;
        }

        String name = node.getNodeName();
        int index = name.lastIndexOf(':');
        if (index == -1){
            return name;
        }

        return name.substring(index + 1);
    }

    public Object getValue(){
        return stringValue(node);
    }

    private String stringValue(Node node){
        int nodeType = node.getNodeType();
        if (nodeType == Node.COMMENT_NODE){
            String text = ((Comment)node).getData();
            return text == null ? "" : text.trim();
        }
        else if (nodeType == Node.TEXT_NODE ||
                nodeType == Node.CDATA_SECTION_NODE){
            String text = node.getNodeValue();
            return text == null ? "" : text.trim();
        }
        else if (nodeType == Node.PROCESSING_INSTRUCTION_NODE){
            String text = ((ProcessingInstruction)node).getData();
            return text == null ? "" : text.trim();
        }
        else {
            NodeList list = node.getChildNodes();
            StringBuffer buf = new StringBuffer(16);
            for(int i = 0; i < list.getLength();i++) {
                Node child = list.item(i);
                if (child.getNodeType() == Node.TEXT_NODE){
                    buf.append(child.getNodeValue());
                }
                else {
                    buf.append(stringValue(child));
                }
            }
            return buf.toString().trim();
        }
    }

    private AbstractFactory getAbstractFactory(JXPathContext context){
        AbstractFactory factory = context.getFactory();
        if (factory == null){
            throw new JXPathException("Factory is not set on the JXPathContext - cannot create path: " + asPath());
        }
        return factory;
    }

    public int compareChildNodePointers(NodePointer pointer1, NodePointer pointer2){
        Node node1 = (Node)pointer1.getNodeValue();
        Node node2 = (Node)pointer2.getNodeValue();
        if (node1 == node2){
            return 0;
        }

        Node current = node.getFirstChild();
        while (current != null){
            if (current == node1){
                return -1;
            }
            else if (current == node2){
                return 1;
            }
            current = current.getNextSibling();
        }

        return 0;
    }
}