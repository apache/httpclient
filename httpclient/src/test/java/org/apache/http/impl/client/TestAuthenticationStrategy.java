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
package org.apache.http.impl.client;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.auth.AUTH;
import org.apache.http.auth.AuthOption;
import org.apache.http.auth.AuthScheme;
import org.apache.http.auth.AuthSchemeProvider;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.auth.BasicSchemeFactory;
import org.apache.http.impl.auth.DigestScheme;
import org.apache.http.impl.auth.DigestSchemeFactory;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicHttpResponse;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Simple tests for {@link AuthenticationStrategyImpl}.
 */
@SuppressWarnings("boxing") // test code
public class TestAuthenticationStrategy {

    @Test(expected=IllegalArgumentException.class)
    public void testIsAuthenticationRequestedInvalidInput() throws Exception {
        final TargetAuthenticationStrategy authStrategy = new TargetAuthenticationStrategy();
        final HttpHost host = new HttpHost("localhost", 80);
        final HttpClientContext context = HttpClientContext.create();
        authStrategy.isAuthenticationRequested(host, null, context);
    }

    @Test
    public void testTargetAuthRequested() throws Exception {
        final TargetAuthenticationStrategy authStrategy = new TargetAuthenticationStrategy();
        final HttpResponse response = new BasicHttpResponse(HttpVersion.HTTP_1_1, HttpStatus.SC_UNAUTHORIZED, "UNAUTHORIZED");
        final HttpHost host = new HttpHost("localhost", 80);
        final HttpClientContext context = HttpClientContext.create();
        Assert.assertTrue(authStrategy.isAuthenticationRequested(host, response, context));
    }

    @Test
    public void testProxyAuthRequested() throws Exception {
        final ProxyAuthenticationStrategy authStrategy = new ProxyAuthenticationStrategy();
        final HttpResponse response = new BasicHttpResponse(HttpVersion.HTTP_1_1, HttpStatus.SC_PROXY_AUTHENTICATION_REQUIRED, "UNAUTHORIZED");
        final HttpHost host = new HttpHost("localhost", 80);
        final HttpClientContext context = HttpClientContext.create();
        Assert.assertTrue(authStrategy.isAuthenticationRequested(host, response, context));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testGetChallengesInvalidInput() throws Exception {
        final TargetAuthenticationStrategy authStrategy = new TargetAuthenticationStrategy();
        final HttpHost host = new HttpHost("localhost", 80);
        final HttpClientContext context = HttpClientContext.create();
        authStrategy.getChallenges(host, null, context);
    }

    @Test
    public void testGetChallenges() throws Exception {
        final TargetAuthenticationStrategy authStrategy = new TargetAuthenticationStrategy();
        final HttpClientContext context = HttpClientContext.create();
        final HttpHost host = new HttpHost("localhost", 80);
        final HttpResponse response = new BasicHttpResponse(HttpVersion.HTTP_1_1, HttpStatus.SC_UNAUTHORIZED, "UNAUTHORIZED");
        final Header h1 = new BasicHeader(AUTH.WWW_AUTH, "  Basic  realm=\"test\"");
        final Header h2 = new BasicHeader(AUTH.WWW_AUTH, "\t\tDigest   realm=\"realm1\", nonce=\"1234\"");
        final Header h3 = new BasicHeader(AUTH.WWW_AUTH, "WhatEver realm=\"realm1\", stuff=\"1234\"");
        response.addHeader(h1);
        response.addHeader(h2);
        response.addHeader(h3);

        final Map<String, Header> challenges = authStrategy.getChallenges(host, response, context);

        Assert.assertNotNull(challenges);
        Assert.assertEquals(3, challenges.size());
        Assert.assertSame(h1, challenges.get("basic"));
        Assert.assertSame(h2, challenges.get("digest"));
        Assert.assertSame(h3, challenges.get("whatever"));
    }

    @Test
    public void testSelectInvalidInput() throws Exception {
        final TargetAuthenticationStrategy authStrategy = new TargetAuthenticationStrategy();
        final Map<String, Header> challenges = new HashMap<String, Header>();
        final HttpResponse response = new BasicHttpResponse(HttpVersion.HTTP_1_1, HttpStatus.SC_UNAUTHORIZED, "UNAUTHORIZED");
        final HttpHost authhost = new HttpHost("locahost", 80);
        final HttpClientContext context = HttpClientContext.create();
        try {
            authStrategy.select(null, authhost, response, context);
            Assert.fail("IllegalArgumentException expected");
        } catch (final IllegalArgumentException ex) {
        }
        try {
            authStrategy.select(challenges, null, response, context);
            Assert.fail("IllegalArgumentException expected");
        } catch (final IllegalArgumentException ex) {
        }
        try {
            authStrategy.select(challenges, authhost, null, context);
            Assert.fail("IllegalArgumentException expected");
        } catch (final IllegalArgumentException ex) {
        }
        try {
            authStrategy.select(challenges, authhost, response, null);
            Assert.fail("IllegalArgumentException expected");
        } catch (final IllegalArgumentException ex) {
        }
    }

    @Test
    public void testSelectNoSchemeRegistry() throws Exception {
        final TargetAuthenticationStrategy authStrategy = new TargetAuthenticationStrategy();
        final HttpResponse response = new BasicHttpResponse(HttpVersion.HTTP_1_1, HttpStatus.SC_UNAUTHORIZED, "UNAUTHORIZED");
        final HttpHost authhost = new HttpHost("locahost", 80);
        final HttpClientContext context = HttpClientContext.create();

        final Map<String, Header> challenges = new HashMap<String, Header>();
        challenges.put("basic", new BasicHeader(AUTH.WWW_AUTH, "Basic realm=\"test\""));
        challenges.put("digest", new BasicHeader(AUTH.WWW_AUTH, "Digest realm=\"realm1\", nonce=\"1234\""));

        final Queue<AuthOption> options = authStrategy.select(challenges, authhost, response, context);
        Assert.assertNotNull(options);
        Assert.assertEquals(0, options.size());
    }

    @Test
    public void testSelectNoCredentialsProvider() throws Exception {
        final TargetAuthenticationStrategy authStrategy = new TargetAuthenticationStrategy();
        final HttpResponse response = new BasicHttpResponse(HttpVersion.HTTP_1_1, HttpStatus.SC_UNAUTHORIZED, "UNAUTHORIZED");
        final HttpHost authhost = new HttpHost("locahost", 80);
        final HttpClientContext context = HttpClientContext.create();

        final Map<String, Header> challenges = new HashMap<String, Header>();
        challenges.put("basic", new BasicHeader(AUTH.WWW_AUTH, "Basic realm=\"test\""));
        challenges.put("digest", new BasicHeader(AUTH.WWW_AUTH, "Digest realm=\"realm1\", nonce=\"1234\""));

        final Registry<AuthSchemeProvider> authSchemeRegistry = RegistryBuilder.<AuthSchemeProvider>create()
            .register("basic", BasicSchemeFactory.DEFAULT)
            .register("digest", DigestSchemeFactory.DEFAULT).build();
        context.setAuthSchemeRegistry(authSchemeRegistry);

        final Queue<AuthOption> options = authStrategy.select(challenges, authhost, response, context);
        Assert.assertNotNull(options);
        Assert.assertEquals(0, options.size());
    }

    @Test
    public void testNoCredentials() throws Exception {
        final TargetAuthenticationStrategy authStrategy = new TargetAuthenticationStrategy();
        final HttpResponse response = new BasicHttpResponse(HttpVersion.HTTP_1_1, HttpStatus.SC_UNAUTHORIZED, "UNAUTHORIZED");
        final HttpHost authhost = new HttpHost("locahost", 80);
        final HttpClientContext context = HttpClientContext.create();

        final Map<String, Header> challenges = new HashMap<String, Header>();
        challenges.put("basic", new BasicHeader(AUTH.WWW_AUTH, "Basic realm=\"realm1\""));
        challenges.put("digest", new BasicHeader(AUTH.WWW_AUTH, "Digest realm=\"realm2\", nonce=\"1234\""));

        final Registry<AuthSchemeProvider> authSchemeRegistry = RegistryBuilder.<AuthSchemeProvider>create()
            .register("basic", BasicSchemeFactory.DEFAULT)
            .register("digest", DigestSchemeFactory.DEFAULT).build();
        context.setAuthSchemeRegistry(authSchemeRegistry);

        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        context.setCredentialsProvider(credentialsProvider);

        final Queue<AuthOption> options = authStrategy.select(challenges, authhost, response, context);
        Assert.assertNotNull(options);
        Assert.assertEquals(0, options.size());
    }

    @Test
    public void testCredentialsFound() throws Exception {
        final TargetAuthenticationStrategy authStrategy = new TargetAuthenticationStrategy();
        final HttpResponse response = new BasicHttpResponse(HttpVersion.HTTP_1_1, HttpStatus.SC_UNAUTHORIZED, "UNAUTHORIZED");
        final HttpHost authhost = new HttpHost("somehost", 80);
        final HttpClientContext context = HttpClientContext.create();

        final Map<String, Header> challenges = new HashMap<String, Header>();
        challenges.put("basic", new BasicHeader(AUTH.WWW_AUTH, "Basic realm=\"realm1\""));
        challenges.put("digest", new BasicHeader(AUTH.WWW_AUTH, "Digest realm=\"realm2\", nonce=\"1234\""));

        final Registry<AuthSchemeProvider> authSchemeRegistry = RegistryBuilder.<AuthSchemeProvider>create()
            .register("basic", BasicSchemeFactory.DEFAULT)
            .register("digest", DigestSchemeFactory.DEFAULT).build();
        context.setAuthSchemeRegistry(authSchemeRegistry);

        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(new AuthScope("somehost", 80, "realm2"),
                new UsernamePasswordCredentials("user", "pwd"));
        context.setCredentialsProvider(credentialsProvider);

        final Queue<AuthOption> options = authStrategy.select(challenges, authhost, response, context);
        Assert.assertNotNull(options);
        Assert.assertEquals(1, options.size());
        final AuthOption option = options.remove();
        Assert.assertTrue(option.getAuthScheme() instanceof DigestScheme);
    }

    @Test
    public void testUnsupportedScheme() throws Exception {
        final TargetAuthenticationStrategy authStrategy = new TargetAuthenticationStrategy();
        final HttpResponse response = new BasicHttpResponse(HttpVersion.HTTP_1_1, HttpStatus.SC_UNAUTHORIZED, "UNAUTHORIZED");
        final HttpHost authhost = new HttpHost("somehost", 80);
        final HttpClientContext context = HttpClientContext.create();

        final Map<String, Header> challenges = new HashMap<String, Header>();
        challenges.put("basic", new BasicHeader(AUTH.WWW_AUTH, "Basic realm=\"realm1\""));
        challenges.put("digest", new BasicHeader(AUTH.WWW_AUTH, "Digest realm=\"realm2\", nonce=\"1234\""));
        challenges.put("whatever", new BasicHeader(AUTH.WWW_AUTH, "Whatever realm=\"realm3\""));

        final Registry<AuthSchemeProvider> authSchemeRegistry = RegistryBuilder.<AuthSchemeProvider>create()
            .register("basic", BasicSchemeFactory.DEFAULT)
            .register("digest", DigestSchemeFactory.DEFAULT).build();
        context.setAuthSchemeRegistry(authSchemeRegistry);

        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(new AuthScope("somehost", 80),
                new UsernamePasswordCredentials("user", "pwd"));
        context.setCredentialsProvider(credentialsProvider);

        final Queue<AuthOption> options = authStrategy.select(challenges, authhost, response, context);
        Assert.assertNotNull(options);
        Assert.assertEquals(2, options.size());
        final AuthOption option1 = options.remove();
        Assert.assertTrue(option1.getAuthScheme() instanceof DigestScheme);
        final AuthOption option2 = options.remove();
        Assert.assertTrue(option2.getAuthScheme() instanceof BasicScheme);
    }

    @Test
    public void testCustomAuthPreference() throws Exception {
        final TargetAuthenticationStrategy authStrategy = new TargetAuthenticationStrategy();
        final HttpResponse response = new BasicHttpResponse(HttpVersion.HTTP_1_1, HttpStatus.SC_UNAUTHORIZED, "UNAUTHORIZED");
        final RequestConfig config = RequestConfig.custom()
            .setTargetPreferredAuthSchemes(Arrays.asList(AuthSchemes.BASIC))
            .build();

        final HttpHost authhost = new HttpHost("somehost", 80);
        final HttpClientContext context = HttpClientContext.create();

        final Map<String, Header> challenges = new HashMap<String, Header>();
        challenges.put("basic", new BasicHeader(AUTH.WWW_AUTH, "Basic realm=\"realm1\""));
        challenges.put("digest", new BasicHeader(AUTH.WWW_AUTH, "Digest realm=\"realm2\", nonce=\"1234\""));

        final Registry<AuthSchemeProvider> authSchemeRegistry = RegistryBuilder.<AuthSchemeProvider>create()
            .register("basic", BasicSchemeFactory.DEFAULT)
            .register("digest", DigestSchemeFactory.DEFAULT).build();
        context.setAuthSchemeRegistry(authSchemeRegistry);
        context.setRequestConfig(config);

        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(new AuthScope("somehost", 80),
                new UsernamePasswordCredentials("user", "pwd"));
        context.setCredentialsProvider(credentialsProvider);

        final Queue<AuthOption> options = authStrategy.select(challenges, authhost, response, context);
        Assert.assertNotNull(options);
        Assert.assertEquals(1, options.size());
        final AuthOption option1 = options.remove();
        Assert.assertTrue(option1.getAuthScheme() instanceof BasicScheme);
    }

    @Test
    public void testAuthSucceededInvalidInput() throws Exception {
        final TargetAuthenticationStrategy authStrategy = new TargetAuthenticationStrategy();
        final HttpHost authhost = new HttpHost("locahost", 80);
        final BasicScheme authScheme = new BasicScheme();
        final HttpClientContext context = HttpClientContext.create();
        try {
            authStrategy.authSucceeded(null, authScheme, context);
            Assert.fail("IllegalArgumentException expected");
        } catch (final IllegalArgumentException ex) {
        }
        try {
            authStrategy.authSucceeded(authhost, null, context);
            Assert.fail("IllegalArgumentException expected");
        } catch (final IllegalArgumentException ex) {
        }
        try {
            authStrategy.authSucceeded(authhost, authScheme, null);
            Assert.fail("IllegalArgumentException expected");
        } catch (final IllegalArgumentException ex) {
        }
    }

    @Test
    public void testAuthSucceeded() throws Exception {
        final TargetAuthenticationStrategy authStrategy = new TargetAuthenticationStrategy();
        final HttpHost authhost = new HttpHost("somehost", 80);
        final BasicScheme authScheme = new BasicScheme();
        authScheme.processChallenge(new BasicHeader(AUTH.WWW_AUTH, "Basic realm=test"));

        final AuthCache authCache = Mockito.mock(AuthCache.class);

        final HttpClientContext context = HttpClientContext.create();
        context.setAuthCache(authCache);

        authStrategy.authSucceeded(authhost, authScheme, context);
        Mockito.verify(authCache).put(authhost, authScheme);
    }

    @Test
    public void testAuthSucceededNoCache() throws Exception {
        final TargetAuthenticationStrategy authStrategy = new TargetAuthenticationStrategy();
        final HttpHost authhost = new HttpHost("somehost", 80);
        final BasicScheme authScheme = new BasicScheme();
        authScheme.processChallenge(new BasicHeader(AUTH.WWW_AUTH, "Basic realm=test"));

        final HttpClientContext context = HttpClientContext.create();
        context.setAuthCache(null);

        authStrategy.authSucceeded(authhost, authScheme, context);
        final AuthCache authCache = context.getAuthCache();
        Assert.assertNotNull(authCache);
    }

    @Test
    public void testAuthScemeNotCompleted() throws Exception {
        final TargetAuthenticationStrategy authStrategy = new TargetAuthenticationStrategy();
        final HttpHost authhost = new HttpHost("somehost", 80);
        final BasicScheme authScheme = new BasicScheme();

        final AuthCache authCache = Mockito.mock(AuthCache.class);

        final HttpClientContext context = HttpClientContext.create();
        context.setAuthCache(authCache);

        authStrategy.authSucceeded(authhost, authScheme, context);
        Mockito.verify(authCache, Mockito.never()).put(authhost, authScheme);
    }

    @Test
    public void testAuthScemeNonCacheable() throws Exception {
        final TargetAuthenticationStrategy authStrategy = new TargetAuthenticationStrategy();
        final HttpHost authhost = new HttpHost("somehost", 80);
        final AuthScheme authScheme = Mockito.mock(AuthScheme.class);
        Mockito.when(authScheme.isComplete()).thenReturn(true);
        Mockito.when(authScheme.getSchemeName()).thenReturn("whatever");

        final AuthCache authCache = Mockito.mock(AuthCache.class);

        final HttpClientContext context = HttpClientContext.create();
        context.setAuthCache(authCache);

        authStrategy.authSucceeded(authhost, authScheme, context);
        Mockito.verify(authCache, Mockito.never()).put(authhost, authScheme);
    }

    @Test
    public void testAuthFailedInvalidInput() throws Exception {
        final TargetAuthenticationStrategy authStrategy = new TargetAuthenticationStrategy();
        final HttpHost authhost = new HttpHost("locahost", 80);
        final BasicScheme authScheme = new BasicScheme();
        final HttpClientContext context = HttpClientContext.create();
        try {
            authStrategy.authFailed(null, authScheme, context);
            Assert.fail("IllegalArgumentException expected");
        } catch (final IllegalArgumentException ex) {
        }
        try {
            authStrategy.authFailed(authhost, authScheme, null);
            Assert.fail("IllegalArgumentException expected");
        } catch (final IllegalArgumentException ex) {
        }
    }

    @Test
    public void testAuthFailed() throws Exception {
        final TargetAuthenticationStrategy authStrategy = new TargetAuthenticationStrategy();
        final HttpHost authhost = new HttpHost("somehost", 80);
        final BasicScheme authScheme = new BasicScheme();
        authScheme.processChallenge(new BasicHeader(AUTH.WWW_AUTH, "Basic realm=test"));

        final AuthCache authCache = Mockito.mock(AuthCache.class);

        final HttpClientContext context = HttpClientContext.create();
        context.setAuthCache(authCache);

        authStrategy.authFailed(authhost, authScheme, context);
        Mockito.verify(authCache).remove(authhost);
    }

    @Test
    public void testAuthFailedNoCache() throws Exception {
        final TargetAuthenticationStrategy authStrategy = new TargetAuthenticationStrategy();
        final HttpHost authhost = new HttpHost("somehost", 80);
        final BasicScheme authScheme = new BasicScheme();

        final HttpClientContext context = HttpClientContext.create();
        context.setAuthCache(null);

        authStrategy.authFailed(authhost, authScheme, context);
    }

}
