/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights
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
 * 4. The names "The Jakarta Project", "Tomcat", and "Apache Software
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
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

package org.apache.commons.jxpath.servlet;

import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.PageContext;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathContextFactory;
import org.apache.commons.jxpath.JXPathIntrospector;

/**
 * Static methods that allocate and cache JXPathContexts bound to PageContext,
 * ServletRequest, HttpSession and ServletContext.
 * <p>
 * The JXPathContext returned by {@link #getPageContext getPageContext()}
 * provides access to all scopes via the PageContext.findAttribute()
 * method.  Thus, an expression like "foo" will first look for the attribute
 * named "foo" in the "page" context, then the "request" context, then
 * the "session" one and finally in the "application" context.
 * <p>
 * If you need to limit the attibute lookup to just one scope, you can use the
 * pre-definded variables "page", "request", "session" and "application".
 * For example, the expression "$session/foo" extracts the value of the
 * session attribute named "foo".
 * <p>
 * Following are some implementation details. There is a separate JXPathContext
 * for each of the four scopes. These contexts are chained according to the
 * nesting of the scopes.  So, the parent of the "page" JXPathContext is a
 * "request" JXPathContext, whose parent is a "session" JXPathContext (that is
 * if there is a session), whose parent is an "application" context.
 * <p>
 * The  XPath context node for each context is the corresponding object:
 * PageContext, ServletRequest, HttpSession or ServletContext.  This feature can
 * be used by servlets.  A servlet can use one of the methods declared by this
 * class and work with a specific JXPathContext for any scope.
 * <p>
 * Since JXPath chains lookups for variables and extension functions, variables
 * and extension function declared in the outer scopes are also available in
 * the inner scopes.
 * <p>
 * Each  of the four context declares exactly one variable, the value of which
 * is the corresponding object: PageContext, etc.
 * <p>
 * The  "session" variable will be undefined if there is no session for this
 * servlet. JXPath does not automatically create sessions.
 *
 * @author Dmitri Plotnikov
 * @version $Revision: 1.3 $ $Date: 2003/01/11 05:41:26 $
 */
public final class JXPathServletContexts {

    private static JXPathContextFactory factory;

    static {
        JXPathIntrospector.registerDynamicClass(
            PageScopeContext.class,
            PageScopeContextHandler.class);
        factory = JXPathContextFactory.newInstance();
    }

    /**
     * Returns a JXPathContext bound to the "page" scope. Caches that context
     * within the PageContext itself.
     */
    public static JXPathContext getPageContext(PageContext pageContext) {
        JXPathContext context =
            (JXPathContext) pageContext.getAttribute(Constants.JXPATH_CONTEXT);
        if (context == null) {
            JXPathIntrospector.registerDynamicClass(
                pageContext.getClass(),
                PageContextHandler.class);
            JXPathContext parentContext =
                getRequestContext(
                    pageContext.getRequest(),
                    pageContext.getServletContext());
            context = factory.newContext(parentContext, pageContext);
            context.setVariables(
                new KeywordVariables(
                    Constants.PAGE_SCOPE,
                    new PageScopeContext(pageContext)));
            pageContext.setAttribute(Constants.JXPATH_CONTEXT, context);
        }
        return context;
    }

    /**
     * Returns a JXPathContext bound to the "request" scope. Caches that context
     * within the request itself.
     */
    public static JXPathContext getRequestContext(
        ServletRequest request,
        ServletContext servletContext) 
    {
        JXPathContext context =
            (JXPathContext) request.getAttribute(Constants.JXPATH_CONTEXT);
        if (context == null) {
            JXPathContext parentContext = null;
            if (request instanceof HttpServletRequest) {
                HttpSession session =
                    ((HttpServletRequest) request).getSession(false);
                if (session != null) {
                    parentContext = getSessionContext(session, servletContext);
                }
                else {
                    parentContext = getApplicationContext(servletContext);
                }
            }
            JXPathIntrospector.registerDynamicClass(
                request.getClass(),
                ServletRequestHandler.class);
            context = factory.newContext(parentContext, request);
            context.setVariables(
                new KeywordVariables(Constants.REQUEST_SCOPE, request));
            request.setAttribute(Constants.JXPATH_CONTEXT, context);
        }
        return context;
    }

    /**
     * Returns a JXPathContext bound to the "session" scope. Caches that context
     * within the session itself.
     */
    public static JXPathContext getSessionContext(
        HttpSession session,
        ServletContext servletContext) 
    {
        JXPathContext context =
            (JXPathContext) session.getAttribute(Constants.JXPATH_CONTEXT);
        if (context == null) {
            JXPathIntrospector.registerDynamicClass(
                session.getClass(),
                HttpSessionHandler.class);
            JXPathContext parentContext = getApplicationContext(servletContext);
            context = factory.newContext(parentContext, session);
            context.setVariables(
                new KeywordVariables(Constants.SESSION_SCOPE, session));
            session.setAttribute(Constants.JXPATH_CONTEXT, context);
        }
        return context;
    }

    /**
     * Returns  a JXPathContext bound to the "application" scope. Caches that
     * context within the servlet context itself.
     */
    public static JXPathContext getApplicationContext(
            ServletContext servletContext) 
    {
        JXPathContext context =
            (JXPathContext) servletContext.getAttribute(
                Constants.JXPATH_CONTEXT);
        if (context == null) {
            JXPathIntrospector.registerDynamicClass(
                servletContext.getClass(),
                ServletContextHandler.class);
            context = factory.newContext(null, servletContext);
            context.setVariables(
                new KeywordVariables(
                    Constants.APPLICATION_SCOPE,
                    servletContext));
            servletContext.setAttribute(Constants.JXPATH_CONTEXT, context);
        }
        return context;
    }
}
