/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//jxpath/src/java/org/apache/commons/jxpath/ri/model/dynamic/DynamicPointer.java,v 1.2 2003/01/11 05:41:26 dmitri Exp $
 * $Revision: 1.2 $
 * $Date: 2003/01/11 05:41:26 $
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
package org.apache.commons.jxpath.ri.model.dynamic;

import java.util.Locale;

import org.apache.commons.jxpath.DynamicPropertyHandler;
import org.apache.commons.jxpath.JXPathIntrospector;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.model.NodeIterator;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.apache.commons.jxpath.ri.model.beans.PropertyOwnerPointer;
import org.apache.commons.jxpath.ri.model.beans.PropertyPointer;

/**
 * A  Pointer that points to an object with Dynamic Properties. It is used for
 * the first element of a path; following elements will by of type
 * PropertyPointer.
 *
 * @author Dmitri Plotnikov
 * @version $Revision: 1.2 $ $Date: 2003/01/11 05:41:26 $
 */
public class DynamicPointer extends PropertyOwnerPointer {
    private QName name;
    private Object bean;
    private DynamicPropertyHandler handler;
    private String[] names;

    public DynamicPointer(QName name, Object bean,
            DynamicPropertyHandler handler, Locale locale)
    {
        super(null, locale);
        this.name = name;
        this.bean = bean;
        this.handler = handler;
    }

    public DynamicPointer(NodePointer parent, QName name,
            Object bean, DynamicPropertyHandler handler)
    {
        super(parent);
        this.name = name;
        this.bean = bean;
        this.handler = handler;
    }

    public PropertyPointer getPropertyPointer() {
        return new DynamicPropertyPointer(this, handler);
    }

    public NodeIterator createNodeIterator(
                String property, boolean reverse, NodePointer startWith)
    {
        return new DynamicPropertyIterator(this, property, reverse, startWith);
    }

    public NodeIterator attributeIterator(QName name) {
        return new DynamicAttributeIterator(this, name);
    }

    public QName getName() {
        return name;
    }

    /**
     * Returns the DP object iself.
     */
    public Object getBaseValue() {
        return bean;
    }
    
    public boolean isLeaf() {
        Object value = getNode();
        return value == null
            || JXPathIntrospector.getBeanInfo(value.getClass()).isAtomic();
    }    
    
    public boolean isCollection() {
        return false;
    }

    /**
     * Returns 1.
     */
    public int getLength() {
        return 1;
    }

    public String asPath() {
        if (parent != null) {
            return super.asPath();
        }
        return "/";
    }

    public int hashCode() {
        return System.identityHashCode(bean) + name.hashCode();
    }

    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }

        if (!(object instanceof DynamicPointer)) {
            return false;
        }

        DynamicPointer other = (DynamicPointer) object;
        return bean == other.bean && name.equals(other.name);
    }
}