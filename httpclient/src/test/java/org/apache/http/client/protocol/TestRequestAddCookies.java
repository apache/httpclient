/*
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */
package org.apache.http.client.protocol;

import java.util.Date;

import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Lookup;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.routing.RouteInfo.LayerType;
import org.apache.http.conn.routing.RouteInfo.TunnelType;
import org.apache.http.cookie.CookieOrigin;
import org.apache.http.cookie.CookieSpec;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.cookie.SM;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.impl.cookie.BasicClientCookie2;
import org.apache.http.impl.cookie.DefaultCookieSpecProvider;
import org.apache.http.impl.cookie.IgnoreSpecProvider;
import org.apache.http.impl.cookie.NetscapeDraftSpec;
import org.apache.http.impl.cookie.NetscapeDraftSpecProvider;
import org.apache.http.impl.cookie.RFC2965SpecProvider;
import org.apache.http.message.BasicHttpRequest;
import org.apache.http.protocol.HttpCoreContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

public class TestRequestAddCookies {

    private HttpHost target;
    private CookieStore cookieStore;
    private Lookup<CookieSpecProvider> cookieSpecRegistry;

    @Before
    public void setUp() {
        this.target = new HttpHost("localhost.local", 80);
        this.cookieStore = new BasicCookieStore();
        final BasicClientCookie2 cookie1 = new BasicClientCookie2("name1", "value1");
        cookie1.setVersion(1);
        cookie1.setDomain("localhost.local");
        cookie1.setPath("/");
        this.cookieStore.addCookie(cookie1);
        final BasicClientCookie2 cookie2 = new BasicClientCookie2("name2", "value2");
        cookie2.setVersion(1);
        cookie2.setDomain("localhost.local");
        cookie2.setPath("/");
        this.cookieStore.addCookie(cookie2);

        this.cookieSpecRegistry = RegistryBuilder.<CookieSpecProvider>create()
            .register(CookieSpecs.DEFAULT, new DefaultCookieSpecProvider())
            .register(CookieSpecs.STANDARD, new RFC2965SpecProvider())
            .register(CookieSpecs.NETSCAPE, new NetscapeDraftSpecProvider())
            .register(CookieSpecs.IGNORE_COOKIES, new IgnoreSpecProvider())
            .build();
    }

    @Test(expected=IllegalArgumentException.class)
    public void testRequestParameterCheck() throws Exception {
        final HttpClientContext context = HttpClientContext.create();
        final HttpRequestInterceptor interceptor = RequestAddCookies.INSTANCE;
        interceptor.process(null, context);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testContextParameterCheck() throws Exception {
        final HttpRequest request = new BasicHttpRequest("GET", "/");
        final HttpRequestInterceptor interceptor = RequestAddCookies.INSTANCE;
        interceptor.process(request, null);
    }

    @Test
    public void testAddCookies() throws Exception {
        final HttpRequest request = new BasicHttpRequest("GET", "/");

        final HttpRoute route = new HttpRoute(this.target, null, false);

        final HttpClientContext context = HttpClientContext.create();
        context.setAttribute(HttpCoreContext.HTTP_TARGET_HOST, this.target);
        context.setAttribute(HttpClientContext.HTTP_ROUTE, route);
        context.setAttribute(HttpClientContext.COOKIE_STORE, this.cookieStore);
        context.setAttribute(HttpClientContext.COOKIESPEC_REGISTRY, this.cookieSpecRegistry);

        final HttpRequestInterceptor interceptor = RequestAddCookies.INSTANCE;
        interceptor.process(request, context);

        final Header[] headers1 = request.getHeaders(SM.COOKIE);
        Assert.assertNotNull(headers1);
        Assert.assertEquals(2, headers1.length);
        Assert.assertEquals("$Version=1; name1=\"value1\"", headers1[0].getValue());
        Assert.assertEquals("$Version=1; name2=\"value2\"", headers1[1].getValue());
        final Header[] headers2 = request.getHeaders(SM.COOKIE2);
        Assert.assertNotNull(headers2);
        Assert.assertEquals(0, headers2.length);

        final CookieOrigin cookieOrigin = context.getCookieOrigin();
        Assert.assertNotNull(cookieOrigin);
        Assert.assertEquals(this.target.getHostName(), cookieOrigin.getHost());
        Assert.assertEquals(this.target.getPort(), cookieOrigin.getPort());
        Assert.assertEquals("/", cookieOrigin.getPath());
        Assert.assertFalse(cookieOrigin.isSecure());
    }

    @Test
    public void testCookiesForConnectRequest() throws Exception {
        final HttpRequest request = new BasicHttpRequest("CONNECT", "www.somedomain.com");

        final HttpRoute route = new HttpRoute(this.target, null, false);

        final HttpClientContext context = HttpClientContext.create();
        context.setAttribute(HttpCoreContext.HTTP_TARGET_HOST, this.target);
        context.setAttribute(HttpClientContext.HTTP_ROUTE, route);
        context.setAttribute(HttpClientContext.COOKIE_STORE, this.cookieStore);
        context.setAttribute(HttpClientContext.COOKIESPEC_REGISTRY, this.cookieSpecRegistry);

        final HttpRequestInterceptor interceptor = RequestAddCookies.INSTANCE;
        interceptor.process(request, context);

        final Header[] headers1 = request.getHeaders(SM.COOKIE);
        Assert.assertNotNull(headers1);
        Assert.assertEquals(0, headers1.length);
        final Header[] headers2 = request.getHeaders(SM.COOKIE2);
        Assert.assertNotNull(headers2);
        Assert.assertEquals(0, headers2.length);
    }

    @Test
    public void testNoCookieStore() throws Exception {
        final HttpRequest request = new BasicHttpRequest("GET", "/");

        final HttpRoute route = new HttpRoute(this.target, null, false);

        final HttpClientContext context = HttpClientContext.create();
        context.setAttribute(HttpCoreContext.HTTP_TARGET_HOST, this.target);
        context.setAttribute(HttpClientContext.HTTP_ROUTE, route);
        context.setAttribute(HttpClientContext.COOKIE_STORE, null);
        context.setAttribute(HttpClientContext.COOKIESPEC_REGISTRY, this.cookieSpecRegistry);

        final HttpRequestInterceptor interceptor = RequestAddCookies.INSTANCE;
        interceptor.process(request, context);

        final Header[] headers1 = request.getHeaders(SM.COOKIE);
        Assert.assertNotNull(headers1);
        Assert.assertEquals(0, headers1.length);
        final Header[] headers2 = request.getHeaders(SM.COOKIE2);
        Assert.assertNotNull(headers2);
        Assert.assertEquals(0, headers2.length);
    }

    @Test
    public void testNoCookieSpecRegistry() throws Exception {
        final HttpRequest request = new BasicHttpRequest("GET", "/");

        final HttpRoute route = new HttpRoute(this.target, null, false);

        final HttpClientContext context = HttpClientContext.create();
        context.setAttribute(HttpCoreContext.HTTP_TARGET_HOST, this.target);
        context.setAttribute(HttpClientContext.HTTP_ROUTE, route);
        context.setAttribute(HttpClientContext.COOKIE_STORE, this.cookieStore);
        context.setAttribute(HttpClientContext.COOKIESPEC_REGISTRY, null);

        final HttpRequestInterceptor interceptor = RequestAddCookies.INSTANCE;
        interceptor.process(request, context);

        final Header[] headers1 = request.getHeaders(SM.COOKIE);
        Assert.assertNotNull(headers1);
        Assert.assertEquals(0, headers1.length);
        final Header[] headers2 = request.getHeaders(SM.COOKIE2);
        Assert.assertNotNull(headers2);
        Assert.assertEquals(0, headers2.length);
    }

    @Test
    public void testNoTargetHost() throws Exception {
        final HttpRequest request = new BasicHttpRequest("GET", "/");

        final HttpRoute route = new HttpRoute(this.target, null, false);

        final HttpClientContext context = HttpClientContext.create();
        context.setAttribute(HttpCoreContext.HTTP_TARGET_HOST, null);
        context.setAttribute(HttpClientContext.HTTP_ROUTE, route);
        context.setAttribute(HttpClientContext.COOKIE_STORE, this.cookieStore);
        context.setAttribute(HttpClientContext.COOKIESPEC_REGISTRY, this.cookieSpecRegistry);

        final HttpRequestInterceptor interceptor = RequestAddCookies.INSTANCE;
        interceptor.process(request, context);

        final Header[] headers1 = request.getHeaders(SM.COOKIE);
        Assert.assertNotNull(headers1);
        Assert.assertEquals(0, headers1.length);
        final Header[] headers2 = request.getHeaders(SM.COOKIE2);
        Assert.assertNotNull(headers2);
        Assert.assertEquals(0, headers2.length);
    }

    @Test
    public void testNoHttpConnection() throws Exception {
        final HttpRequest request = new BasicHttpRequest("GET", "/");

        final HttpClientContext context = HttpClientContext.create();
        context.setAttribute(HttpCoreContext.HTTP_TARGET_HOST, this.target);
        context.setAttribute(HttpCoreContext.HTTP_CONNECTION, null);
        context.setAttribute(HttpClientContext.COOKIE_STORE, this.cookieStore);
        context.setAttribute(HttpClientContext.COOKIESPEC_REGISTRY, this.cookieSpecRegistry);

        final HttpRequestInterceptor interceptor = RequestAddCookies.INSTANCE;
        interceptor.process(request, context);

        final Header[] headers1 = request.getHeaders(SM.COOKIE);
        Assert.assertNotNull(headers1);
        Assert.assertEquals(0, headers1.length);
        final Header[] headers2 = request.getHeaders(SM.COOKIE2);
        Assert.assertNotNull(headers2);
        Assert.assertEquals(0, headers2.length);
    }

    @Test
    public void testAddCookiesUsingExplicitCookieSpec() throws Exception {
        final HttpRequest request = new BasicHttpRequest("GET", "/");
        final RequestConfig config = RequestConfig.custom()
            .setCookieSpec(CookieSpecs.NETSCAPE).build();
        final HttpRoute route = new HttpRoute(this.target, null, false);

        final HttpClientContext context = HttpClientContext.create();
        context.setAttribute(HttpCoreContext.HTTP_TARGET_HOST, this.target);
        context.setAttribute(HttpClientContext.HTTP_ROUTE, route);
        context.setAttribute(HttpClientContext.REQUEST_CONFIG, config);
        context.setAttribute(HttpClientContext.COOKIE_STORE, this.cookieStore);
        context.setAttribute(HttpClientContext.COOKIESPEC_REGISTRY, this.cookieSpecRegistry);

        final HttpRequestInterceptor interceptor = RequestAddCookies.INSTANCE;
        interceptor.process(request, context);

        final CookieSpec cookieSpec = context.getCookieSpec();
        Assert.assertTrue(cookieSpec instanceof NetscapeDraftSpec);

        final Header[] headers1 = request.getHeaders(SM.COOKIE);
        Assert.assertNotNull(headers1);
        Assert.assertEquals(1, headers1.length);
        Assert.assertEquals("name1=value1; name2=value2", headers1[0].getValue());
    }

    @Test
    public void testAuthScopeInvalidRequestURI() throws Exception {
        final HttpRequest request = new BasicHttpRequest("GET", "crap:");

        final HttpRoute route = new HttpRoute(this.target, null, false);

        final HttpClientContext context = HttpClientContext.create();
        context.setAttribute(HttpCoreContext.HTTP_TARGET_HOST, this.target);
        context.setAttribute(HttpClientContext.HTTP_ROUTE, route);
        context.setAttribute(HttpClientContext.COOKIE_STORE, this.cookieStore);
        context.setAttribute(HttpClientContext.COOKIESPEC_REGISTRY, this.cookieSpecRegistry);

        final HttpRequestInterceptor interceptor = RequestAddCookies.INSTANCE;
        interceptor.process(request, context);
    }

    @Test
    public void testAuthScopeRemotePortWhenDirect() throws Exception {
        final HttpRequest request = new BasicHttpRequest("GET", "/stuff");

        this.target = new HttpHost("localhost.local");
        final HttpRoute route = new HttpRoute(new HttpHost("localhost.local", 1234), null, false);

        final HttpClientContext context = HttpClientContext.create();
        context.setAttribute(HttpCoreContext.HTTP_TARGET_HOST, this.target);
        context.setAttribute(HttpClientContext.HTTP_ROUTE, route);
        context.setAttribute(HttpClientContext.COOKIE_STORE, this.cookieStore);
        context.setAttribute(HttpClientContext.COOKIESPEC_REGISTRY, this.cookieSpecRegistry);

        final HttpRequestInterceptor interceptor = RequestAddCookies.INSTANCE;
        interceptor.process(request, context);

        final CookieOrigin cookieOrigin = context.getCookieOrigin();
        Assert.assertNotNull(cookieOrigin);
        Assert.assertEquals(this.target.getHostName(), cookieOrigin.getHost());
        Assert.assertEquals(1234, cookieOrigin.getPort());
        Assert.assertEquals("/stuff", cookieOrigin.getPath());
        Assert.assertFalse(cookieOrigin.isSecure());
    }

    @Test
    public void testAuthDefaultHttpPortWhenProxy() throws Exception {
        final HttpRequest request = new BasicHttpRequest("GET", "/stuff");

        this.target = new HttpHost("localhost.local");
        final HttpRoute route = new HttpRoute(
                new HttpHost("localhost.local", 80), null, new HttpHost("localhost", 8888), false);

        final HttpClientContext context = HttpClientContext.create();
        context.setAttribute(HttpCoreContext.HTTP_TARGET_HOST, this.target);
        context.setAttribute(HttpClientContext.HTTP_ROUTE, route);
        context.setAttribute(HttpClientContext.COOKIE_STORE, this.cookieStore);
        context.setAttribute(HttpClientContext.COOKIESPEC_REGISTRY, this.cookieSpecRegistry);

        final HttpRequestInterceptor interceptor = RequestAddCookies.INSTANCE;
        interceptor.process(request, context);

        final CookieOrigin cookieOrigin = context.getCookieOrigin();
        Assert.assertNotNull(cookieOrigin);
        Assert.assertEquals(this.target.getHostName(), cookieOrigin.getHost());
        Assert.assertEquals(80, cookieOrigin.getPort());
        Assert.assertEquals("/stuff", cookieOrigin.getPath());
        Assert.assertFalse(cookieOrigin.isSecure());
    }

    @Test
    public void testAuthDefaultHttpsPortWhenProxy() throws Exception {
        final HttpRequest request = new BasicHttpRequest("GET", "/stuff");

        this.target = new HttpHost("localhost", -1, "https");
        final HttpRoute route = new HttpRoute(
                new HttpHost("localhost", 443, "https"), null,
                new HttpHost("localhost", 8888), true, TunnelType.TUNNELLED, LayerType.LAYERED);

        final HttpClientContext context = HttpClientContext.create();
        context.setAttribute(HttpCoreContext.HTTP_TARGET_HOST, this.target);
        context.setAttribute(HttpClientContext.HTTP_ROUTE, route);
        context.setAttribute(HttpClientContext.COOKIE_STORE, this.cookieStore);
        context.setAttribute(HttpClientContext.COOKIESPEC_REGISTRY, this.cookieSpecRegistry);

        final HttpRequestInterceptor interceptor = RequestAddCookies.INSTANCE;
        interceptor.process(request, context);

        final CookieOrigin cookieOrigin = context.getCookieOrigin();
        Assert.assertNotNull(cookieOrigin);
        Assert.assertEquals(this.target.getHostName(), cookieOrigin.getHost());
        Assert.assertEquals(443, cookieOrigin.getPort());
        Assert.assertEquals("/stuff", cookieOrigin.getPath());
        Assert.assertTrue(cookieOrigin.isSecure());
    }

    @Test
    public void testExcludeExpiredCookies() throws Exception {
        final HttpRequest request = new BasicHttpRequest("GET", "/");

        final BasicClientCookie2 cookie3 = new BasicClientCookie2("name3", "value3");
        cookie3.setVersion(1);
        cookie3.setDomain("localhost.local");
        cookie3.setPath("/");
        cookie3.setExpiryDate(new Date(System.currentTimeMillis() + 100));
        this.cookieStore.addCookie(cookie3);

        Assert.assertEquals(3, this.cookieStore.getCookies().size());

        this.cookieStore = Mockito.spy(this.cookieStore);

        final HttpRoute route = new HttpRoute(this.target, null, false);

        final HttpClientContext context = HttpClientContext.create();
        context.setAttribute(HttpCoreContext.HTTP_TARGET_HOST, this.target);
        context.setAttribute(HttpClientContext.HTTP_ROUTE, route);
        context.setAttribute(HttpClientContext.COOKIE_STORE, this.cookieStore);
        context.setAttribute(HttpClientContext.COOKIESPEC_REGISTRY, this.cookieSpecRegistry);

        // Make sure the third cookie expires
        Thread.sleep(200);

        final HttpRequestInterceptor interceptor = RequestAddCookies.INSTANCE;
        interceptor.process(request, context);

        final Header[] headers1 = request.getHeaders(SM.COOKIE);
        Assert.assertNotNull(headers1);
        Assert.assertEquals(2, headers1.length);
        Assert.assertEquals("$Version=1; name1=\"value1\"", headers1[0].getValue());
        Assert.assertEquals("$Version=1; name2=\"value2\"", headers1[1].getValue());
        final Header[] headers2 = request.getHeaders(SM.COOKIE2);
        Assert.assertNotNull(headers2);
        Assert.assertEquals(0, headers2.length);

        Mockito.verify(this.cookieStore, Mockito.times(1)).clearExpired(Matchers.<Date>any());
    }

    @Test
    public void testNoMatchingCookies() throws Exception {
        final HttpRequest request = new BasicHttpRequest("GET", "/");

        this.cookieStore.clear();
        final BasicClientCookie cookie3 = new BasicClientCookie("name3", "value3");
        cookie3.setDomain("www.somedomain.com");
        cookie3.setPath("/");
        this.cookieStore.addCookie(cookie3);

        final HttpRoute route = new HttpRoute(this.target, null, false);

        final HttpClientContext context = HttpClientContext.create();
        context.setAttribute(HttpCoreContext.HTTP_TARGET_HOST, this.target);
        context.setAttribute(HttpClientContext.HTTP_ROUTE, route);
        context.setAttribute(HttpClientContext.COOKIE_STORE, this.cookieStore);
        context.setAttribute(HttpClientContext.COOKIESPEC_REGISTRY, this.cookieSpecRegistry);

        final HttpRequestInterceptor interceptor = RequestAddCookies.INSTANCE;
        interceptor.process(request, context);

        final Header[] headers1 = request.getHeaders(SM.COOKIE);
        Assert.assertNotNull(headers1);
        Assert.assertEquals(0, headers1.length);
        final Header[] headers2 = request.getHeaders(SM.COOKIE2);
        Assert.assertNotNull(headers2);
        Assert.assertEquals(0, headers2.length);
    }

    // Helper method
    private BasicClientCookie makeCookie(final String name, final String value, final String domain, final String path) {
        final BasicClientCookie cookie = new BasicClientCookie(name, value);
        cookie.setDomain(domain);
        cookie.setPath(path);
        return cookie;
    }

    @Test
    // Test for ordering adapted from test in Commons HC 3.1
    public void testCookieOrder() throws Exception {
        final HttpRequest request = new BasicHttpRequest("GET", "/foobar/yada/yada");

        this.cookieStore.clear();

        cookieStore.addCookie(makeCookie("nomatch", "value", "localhost.local", "/noway"));
        cookieStore.addCookie(makeCookie("name2",   "value", "localhost.local", "/foobar/yada"));
        cookieStore.addCookie(makeCookie("name3",   "value", "localhost.local", "/foobar"));
        cookieStore.addCookie(makeCookie("name1",   "value", "localhost.local", "/foobar/yada/yada"));

        final HttpRoute route = new HttpRoute(this.target, null, false);

        final HttpClientContext context = HttpClientContext.create();
        context.setAttribute(HttpCoreContext.HTTP_TARGET_HOST, this.target);
        context.setAttribute(HttpClientContext.HTTP_ROUTE, route);
        context.setAttribute(HttpClientContext.COOKIE_STORE, this.cookieStore);
        context.setAttribute(HttpClientContext.COOKIESPEC_REGISTRY, this.cookieSpecRegistry);

        final HttpRequestInterceptor interceptor = RequestAddCookies.INSTANCE;
        interceptor.process(request, context);

        final Header[] headers1 = request.getHeaders(SM.COOKIE);
        Assert.assertNotNull(headers1);
        Assert.assertEquals(1, headers1.length);

        Assert.assertEquals("name1=value; name2=value; name3=value", headers1[0].getValue());
    }

}
