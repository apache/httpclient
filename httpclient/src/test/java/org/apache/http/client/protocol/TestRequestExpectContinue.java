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

import org.apache.http.Header;
import org.apache.http.HttpVersion;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.apache.http.message.BasicHttpRequest;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.junit.Assert;
import org.junit.Test;

public class TestRequestExpectContinue {

    @Test
    public void testRequestExpectContinueGenerated() throws Exception {
        final HttpClientContext context = HttpClientContext.create();
        final RequestConfig config = RequestConfig.custom().setExpectContinueEnabled(true).build();
        context.setAttribute(HttpClientContext.REQUEST_CONFIG, config);
        final BasicHttpEntityEnclosingRequest request = new BasicHttpEntityEnclosingRequest("POST", "/");
        final String s = "whatever";
        final StringEntity entity = new StringEntity(s, "US-ASCII");
        request.setEntity(entity);
        final RequestExpectContinue interceptor = RequestExpectContinue.INSTANCE;
        interceptor.process(request, context);
        final Header header = request.getFirstHeader(HTTP.EXPECT_DIRECTIVE);
        Assert.assertNotNull(header);
        Assert.assertEquals(HTTP.EXPECT_CONTINUE, header.getValue());
    }

    @Test
    public void testRequestExpectContinueNotGenerated() throws Exception {
        final HttpContext context = new BasicHttpContext(null);
        final RequestConfig config = RequestConfig.custom().setExpectContinueEnabled(false).build();
        context.setAttribute(HttpClientContext.REQUEST_CONFIG, config);
        final BasicHttpEntityEnclosingRequest request = new BasicHttpEntityEnclosingRequest("POST", "/");
        final String s = "whatever";
        final StringEntity entity = new StringEntity(s, "US-ASCII");
        request.setEntity(entity);
        final RequestExpectContinue interceptor = RequestExpectContinue.INSTANCE;
        interceptor.process(request, context);
        final Header header = request.getFirstHeader(HTTP.EXPECT_DIRECTIVE);
        Assert.assertNull(header);
    }

    @Test
    public void testRequestExpectContinueHTTP10() throws Exception {
        final HttpContext context = new BasicHttpContext(null);
        final RequestConfig config = RequestConfig.custom().setExpectContinueEnabled(true).build();
        context.setAttribute(HttpClientContext.REQUEST_CONFIG, config);
        final BasicHttpEntityEnclosingRequest request = new BasicHttpEntityEnclosingRequest(
                "POST", "/", HttpVersion.HTTP_1_0);
        final String s = "whatever";
        final StringEntity entity = new StringEntity(s, "US-ASCII");
        request.setEntity(entity);
        final RequestExpectContinue interceptor = RequestExpectContinue.INSTANCE;
        interceptor.process(request, context);
        final Header header = request.getFirstHeader(HTTP.EXPECT_DIRECTIVE);
        Assert.assertNull(header);
    }

    @Test
    public void testRequestExpectContinueZeroContent() throws Exception {
        final HttpContext context = new BasicHttpContext(null);
        final RequestConfig config = RequestConfig.custom().setExpectContinueEnabled(true).build();
        context.setAttribute(HttpClientContext.REQUEST_CONFIG, config);
        final BasicHttpEntityEnclosingRequest request = new BasicHttpEntityEnclosingRequest("POST", "/");
        final String s = "";
        final StringEntity entity = new StringEntity(s, "US-ASCII");
        request.setEntity(entity);
        final RequestExpectContinue interceptor = RequestExpectContinue.INSTANCE;
        interceptor.process(request, context);
        final Header header = request.getFirstHeader(HTTP.EXPECT_DIRECTIVE);
        Assert.assertNull(header);
    }

    @Test
    public void testRequestExpectContinueInvalidInput() throws Exception {
        final RequestExpectContinue interceptor = RequestExpectContinue.INSTANCE;
        try {
            interceptor.process(null, null);
            Assert.fail("IllegalArgumentException should have been thrown");
        } catch (final IllegalArgumentException ex) {
            // expected
        }
    }

    @Test
    public void testRequestExpectContinueIgnoreNonenclosingRequests() throws Exception {
        final HttpContext context = new BasicHttpContext(null);
        final BasicHttpRequest request = new BasicHttpRequest("POST", "/");
        final RequestExpectContinue interceptor = RequestExpectContinue.INSTANCE;
        interceptor.process(request, context);
        Assert.assertEquals(0, request.getAllHeaders().length);
    }

}
