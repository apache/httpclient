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

import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.HttpVersion;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.DecompressingEntity;
import org.apache.http.client.entity.GzipDecompressingEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.junit.Assert;
import org.junit.Test;

public class TestResponseContentEncoding {

    @Test
    public void testContentEncodingNoEntity() throws Exception {
        final HttpResponse response = new BasicHttpResponse(HttpVersion.HTTP_1_1, 200, "OK");
        final HttpContext context = new BasicHttpContext();

        final HttpResponseInterceptor interceptor = ResponseContentEncoding.DEFAULT;
        interceptor.process(response, context);
        final HttpEntity entity = response.getEntity();
        Assert.assertNull(entity);
    }

    @Test
    public void testNoContentEncoding() throws Exception {
        final HttpResponse response = new BasicHttpResponse(HttpVersion.HTTP_1_1, 200, "OK");
        final StringEntity original = new StringEntity("plain stuff");
        response.setEntity(original);
        final HttpContext context = new BasicHttpContext();

        final HttpResponseInterceptor interceptor = ResponseContentEncoding.DEFAULT;
        interceptor.process(response, context);
        final HttpEntity entity = response.getEntity();
        Assert.assertNotNull(entity);
        Assert.assertTrue(entity instanceof StringEntity);
    }

    @Test
    public void testGzipContentEncoding() throws Exception {
        final HttpResponse response = new BasicHttpResponse(HttpVersion.HTTP_1_1, 200, "OK");
        final StringEntity original = new StringEntity("encoded stuff");
        original.setContentEncoding("GZip");
        response.setEntity(original);
        final HttpContext context = new BasicHttpContext();

        final HttpResponseInterceptor interceptor = ResponseContentEncoding.DEFAULT;
        interceptor.process(response, context);
        final HttpEntity entity = response.getEntity();
        Assert.assertNotNull(entity);
        Assert.assertTrue(entity instanceof DecompressingEntity);
    }

    @Test
    public void testGzipContentEncodingZeroLength() throws Exception {
        final HttpResponse response = new BasicHttpResponse(HttpVersion.HTTP_1_1, 200, "OK");
        final StringEntity original = new StringEntity("");
        original.setContentEncoding("GZip");
        response.setEntity(original);
        final HttpContext context = new BasicHttpContext();

        final HttpResponseInterceptor interceptor = ResponseContentEncoding.DEFAULT;
        interceptor.process(response, context);
        final HttpEntity entity = response.getEntity();
        Assert.assertNotNull(entity);
        Assert.assertTrue(entity instanceof StringEntity);
    }

    @Test
    public void testXGzipContentEncoding() throws Exception {
        final HttpResponse response = new BasicHttpResponse(HttpVersion.HTTP_1_1, 200, "OK");
        final StringEntity original = new StringEntity("encoded stuff");
        original.setContentEncoding("x-gzip");
        response.setEntity(original);
        final HttpContext context = new BasicHttpContext();

        final HttpResponseInterceptor interceptor = ResponseContentEncoding.DEFAULT;
        interceptor.process(response, context);
        final HttpEntity entity = response.getEntity();
        Assert.assertNotNull(entity);
        Assert.assertTrue(entity instanceof DecompressingEntity);
    }

    @Test
    public void testDeflateContentEncoding() throws Exception {
        final HttpResponse response = new BasicHttpResponse(HttpVersion.HTTP_1_1, 200, "OK");
        final StringEntity original = new StringEntity("encoded stuff");
        original.setContentEncoding("deFlaTe");
        response.setEntity(original);
        final HttpContext context = new BasicHttpContext();

        final HttpResponseInterceptor interceptor = ResponseContentEncoding.DEFAULT;
        interceptor.process(response, context);
        final HttpEntity entity = response.getEntity();
        Assert.assertNotNull(entity);
        Assert.assertTrue(entity instanceof DecompressingEntity);
    }

    @Test
    public void testIdentityContentEncoding() throws Exception {
        final HttpResponse response = new BasicHttpResponse(HttpVersion.HTTP_1_1, 200, "OK");
        final StringEntity original = new StringEntity("encoded stuff");
        original.setContentEncoding("identity");
        response.setEntity(original);
        final HttpContext context = new BasicHttpContext();

        final HttpResponseInterceptor interceptor = ResponseContentEncoding.DEFAULT;
        interceptor.process(response, context);
        final HttpEntity entity = response.getEntity();
        Assert.assertNotNull(entity);
        Assert.assertTrue(entity instanceof StringEntity);
    }

    @Test(expected=HttpException.class)
    public void testUnknownContentEncoding() throws Exception {
        final HttpResponse response = new BasicHttpResponse(HttpVersion.HTTP_1_1, 200, "OK");
        final StringEntity original = new StringEntity("encoded stuff");
        original.setContentEncoding("whatever");
        response.setEntity(original);
        final HttpContext context = new BasicHttpContext();

        final HttpResponseInterceptor interceptor = new ResponseContentEncoding(false);
        interceptor.process(response, context);
    }

    @Test
    public void testContentEncodingRequestParameter() throws Exception {
        final HttpResponse response = new BasicHttpResponse(HttpVersion.HTTP_1_1, 200, "OK");
        final StringEntity original = new StringEntity("encoded stuff");
        original.setContentEncoding("GZip");
        response.setEntity(original);

        final RequestConfig config = RequestConfig.custom()
                .setDecompressionEnabled(false)
                .build();

        final HttpContext context = new BasicHttpContext();
        context.setAttribute(HttpClientContext.REQUEST_CONFIG, config);

        final HttpResponseInterceptor interceptor = ResponseContentEncoding.DEFAULT;
        interceptor.process(response, context);
        final HttpEntity entity = response.getEntity();
        Assert.assertNotNull(entity);
        Assert.assertFalse(entity instanceof GzipDecompressingEntity);
    }

}
