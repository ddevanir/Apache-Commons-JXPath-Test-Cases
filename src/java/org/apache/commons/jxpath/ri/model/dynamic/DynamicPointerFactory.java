/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//jxpath/src/java/org/apache/commons/jxpath/ri/model/dynamic/DynamicPointerFactory.java,v 1.2 2003/01/11 05:41:26 dmitri Exp $
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
import org.apache.commons.jxpath.JXPathBeanInfo;
import org.apache.commons.jxpath.JXPathIntrospector;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.apache.commons.jxpath.ri.model.NodePointerFactory;
import org.apache.commons.jxpath.ri.model.beans.NullPointer;
import org.apache.commons.jxpath.util.ValueUtils;

/**
 * Implements NodePointerFactory for Dynamic classes like Map.
 *
 * @author Dmitri Plotnikov
 * @version $Revision: 1.2 $ $Date: 2003/01/11 05:41:26 $
 */
public class DynamicPointerFactory implements NodePointerFactory {

    public static final int DYNAMIC_POINTER_FACTORY_ORDER = 800;

    public int getOrder() {
        return DYNAMIC_POINTER_FACTORY_ORDER;
    }

    public NodePointer createNodePointer(
        QName name,
        Object bean,
        Locale locale) 
    {
        JXPathBeanInfo bi = JXPathIntrospector.getBeanInfo(bean.getClass());
        if (bi.isDynamic()) {
            DynamicPropertyHandler handler =
                ValueUtils.getDynamicPropertyHandler(
                    bi.getDynamicPropertyHandlerClass());
            return new DynamicPointer(name, bean, handler, locale);
        }
        return null;
    }

    public NodePointer createNodePointer(
        NodePointer parent,
        QName name,
        Object bean) 
    {
        if (bean == null) {
            return new NullPointer(parent, name);
        }

        JXPathBeanInfo bi = JXPathIntrospector.getBeanInfo(bean.getClass());
        if (bi.isDynamic()) {
            DynamicPropertyHandler handler =
                ValueUtils.getDynamicPropertyHandler(
                    bi.getDynamicPropertyHandlerClass());
            return new DynamicPointer(parent, name, bean, handler);
        }
        return null;
    }
}