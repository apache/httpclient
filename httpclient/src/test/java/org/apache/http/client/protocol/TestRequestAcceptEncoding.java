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
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.message.BasicHttpRequest;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.junit.Assert;
import org.junit.Test;

public class TestRequestAcceptEncoding {

    @Test
    public void testAcceptEncoding() throws Exception {
        final HttpRequest request = new BasicHttpRequest("GET", "/");
        final HttpContext context = new BasicHttpContext();

        final HttpRequestInterceptor interceptor = RequestAcceptEncoding.DEFAULT;
        interceptor.process(request, context);
        final Header header = request.getFirstHeader("Accept-Encoding");
        Assert.assertNotNull(header);
        Assert.assertEquals("gzip,deflate", header.getValue());
    }

    @Test
    public void testAcceptEncodingAlreadyPResent() throws Exception {
        final HttpRequest request = new BasicHttpRequest("GET", "/");
        request.addHeader("Accept-Encoding", "stuff");
        final HttpContext context = new BasicHttpContext();

        final HttpRequestInterceptor interceptor = RequestAcceptEncoding.DEFAULT;
        interceptor.process(request, context);
        final Header header = request.getFirstHeader("Accept-Encoding");
        Assert.assertNotNull(header);
        Assert.assertEquals("stuff", header.getValue());
    }

}
