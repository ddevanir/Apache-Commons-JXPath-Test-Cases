/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//jxpath/src/java/org/apache/commons/jxpath/ri/model/beans/BeanPointer.java,v 1.10 2003/01/11 05:41:24 dmitri Exp $
 * $Revision: 1.10 $
 * $Date: 2003/01/11 05:41:24 $
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
package org.apache.commons.jxpath.ri.model.beans;

import java.util.Locale;

import org.apache.commons.jxpath.JXPathBeanInfo;
import org.apache.commons.jxpath.JXPathIntrospector;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.model.NodePointer;

/**
 * A Pointer that points to a JavaBean or a collection. It is either
 * the first element of a path or a pointer for a property value.
 * Typically there is a BeanPropertyPointer between two BeanPointers
 * in the chain.
 *
 * @author Dmitri Plotnikov
 * @version $Revision: 1.10 $ $Date: 2003/01/11 05:41:24 $
 */
public class BeanPointer extends PropertyOwnerPointer {
    private QName name;
    private Object bean;
    private JXPathBeanInfo beanInfo;

    public BeanPointer(
            QName name,
            Object bean,
            JXPathBeanInfo beanInfo,
            Locale locale) 
    {
        super(null, locale);
        this.name = name;
        this.bean = bean;
        this.beanInfo = beanInfo;
    }

    /**
     * @param name is the name given to the first node
     */
    public BeanPointer(
            NodePointer parent,
            QName name,
            Object bean,
            JXPathBeanInfo beanInfo) 
    {
        super(parent);
        this.name = name;
        this.bean = bean;
        this.beanInfo = beanInfo;
    }

    public PropertyPointer getPropertyPointer() {
        return new BeanPropertyPointer(this, beanInfo);
    }

    public QName getName() {
        return name;
    }

    /**
     * Returns the bean itself
     */
    public Object getBaseValue() {
        return bean;
    }

    /**
     * Returns false
     */
    public boolean isCollection() {
        return false;
    }

    /**
     * Returns 1.
     */
    public int getLength() {
        return 1;
    }

    public boolean isLeaf() {
        Object value = getNode();
        return value == null
            || JXPathIntrospector.getBeanInfo(value.getClass()).isAtomic();
    }

    public int hashCode() {
        return name == null ? 0 : name.hashCode();
    }

    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }

        if (!(object instanceof BeanPointer)) {
            return false;
        }

        BeanPointer other = (BeanPointer) object;
        if (parent != other.parent) {
            if (parent == null || !parent.equals(other.parent)) {
                return false;
            }
        }

        if ((name == null && other.name != null)
            || (name != null && !name.equals(other.name))) {
            return false;
        }

        int iThis = (index == WHOLE_COLLECTION ? 0 : index);
        int iOther = (other.index == WHOLE_COLLECTION ? 0 : other.index);
        if (iThis != iOther) {
            return false;
        }

        if (bean instanceof Number
            || bean instanceof String
            || bean instanceof Boolean) {
            return bean.equals(other.bean);
        }
        return bean == other.bean;
    }

    /**
     * If the pointer has a parent, then parent's path.
     * If the bean is null, "null()".
     * If the bean is a primitive value, the value itself.
     * Otherwise - an empty string.
     */
    public String asPath() {
        if (parent != null) {
            return super.asPath();
        }
        else if (bean == null) {
            return "null()";
        }
        else if (bean instanceof Number) {
            String string = bean.toString();
            if (string.endsWith(".0")) {
                string = string.substring(0, string.length() - 2);
            }
            return string;
        }
        else if (bean instanceof Boolean) {
            return ((Boolean) bean).booleanValue() ? "true()" : "false()";
        }
        else if (bean instanceof String) {
            return "'" + bean + "'";
        }
        return "/";
    }
}