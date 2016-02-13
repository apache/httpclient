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
package org.apache.hc.client5.http.impl.cache;

import java.io.IOException;

import org.apache.hc.client5.http.HttpRoute;
import org.apache.hc.client5.http.impl.sync.ClientExecChain;
import org.apache.hc.client5.http.methods.CloseableHttpResponse;
import org.apache.hc.client5.http.methods.HttpExecutionAware;
import org.apache.hc.client5.http.methods.HttpRequestWrapper;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.ProtocolVersion;
import org.apache.hc.core5.http.message.BasicHttpResponse;

public class DummyBackend implements ClientExecChain {

    private HttpRequest request;
    private HttpResponse response = new BasicHttpResponse(new ProtocolVersion("HTTP",1,1), HttpStatus.SC_OK, "OK");
    private int executions = 0;

    public void setResponse(final HttpResponse resp) {
        response = resp;
    }

    public HttpRequest getCapturedRequest() {
        return request;
    }

    @Override
    public CloseableHttpResponse execute(
            final HttpRoute route,
            final HttpRequestWrapper request,
            final HttpClientContext clientContext,
            final HttpExecutionAware execAware) throws IOException, HttpException {
        this.request = request;
        executions++;
        return Proxies.enhanceResponse(response);
    }

    public int getExecutions() {
        return executions;
    }
}
