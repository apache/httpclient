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

package org.apache.hc.client5.http.classic.methods;

import java.net.URI;


/**
 * HTTP methods defined in RFC2616.
 *
 * @since 5.0-beta2
 */
public enum ClassicHttpRequests {

    DELETE {
        @Override
        public HttpUriRequestBase create(final URI uri) {
            return new HttpDelete(uri);
        }
    },

    GET {
        @Override
        public HttpUriRequestBase create(final URI uri) {
            return new HttpGet(uri);
        }
    },

    HEAD {
        @Override
        public HttpUriRequestBase create(final URI uri) {
            return new HttpHead(uri);
        }
    },

    OPTIONS {
        @Override
        public HttpUriRequestBase create(final URI uri) {
            return new HttpOptions(uri);
        }
    },

    PATCH {
        @Override
        public HttpUriRequestBase create(final URI uri) {
            return new HttpPatch(uri);
        }
    },

    POST {
        @Override
        public HttpUriRequestBase create(final URI uri) {
            return new HttpPost(uri);
        }
    },

    PUT {
        @Override
        public HttpUriRequestBase create(final URI uri) {
            return new HttpPut(uri);
        }
    },

    TRACE {
        @Override
        public HttpUriRequestBase create(final URI uri) {
            return new HttpTrace(uri);
        }
    };

    /**
     * Creates a request object of the exact subclass of {@link HttpUriRequestBase}.
     *
     * @param uri
     *            a non-null URI String.
     * @return a new subclass of HttpUriRequestBase
     */
    public HttpUriRequestBase create(final String uri) {
        return create(URI.create(uri));
    }

    /**
     * Creates a request object of the exact subclass of {@link HttpUriRequestBase}.
     *
     * @param uri
     *            a non-null URI.
     * @return a new subclass of HttpUriRequestBase
     */
    public abstract HttpUriRequestBase create(URI uri);

}
