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

package org.apache.http.impl.cookie;

import org.apache.http.cookie.CookieAttributeHandler;
import org.apache.http.cookie.CookieOrigin;
import org.apache.http.cookie.MalformedCookieException;
import org.junit.Assert;
import org.junit.Test;

public class TestNetscapeCookieAttribHandlers {

    @Test
    public void testNetscapeDomainValidate1() throws Exception {
        final BasicClientCookie cookie = new BasicClientCookie("name", "value");
        final CookieOrigin origin = new CookieOrigin("somehost", 80, "/", false);
        final CookieAttributeHandler h = NetscapeDomainHandler.INSTANCE;

        cookie.setDomain("somehost");
        h.validate(cookie, origin);

        cookie.setDomain("otherhost");
        try {
            h.validate(cookie, origin);
            Assert.fail("MalformedCookieException should have been thrown");
        } catch (final MalformedCookieException ex) {
            // expected
        }
    }

    @Test
    public void testNetscapeDomainValidate2() throws Exception {
        final BasicClientCookie cookie = new BasicClientCookie("name", "value");
        final CookieOrigin origin = new CookieOrigin("www.somedomain.com", 80, "/", false);
        final CookieAttributeHandler h = NetscapeDomainHandler.INSTANCE;

        cookie.setDomain(".somedomain.com");
        h.validate(cookie, origin);

        cookie.setDomain(".otherdomain.com");
        try {
            h.validate(cookie, origin);
            Assert.fail("MalformedCookieException should have been thrown");
        } catch (final MalformedCookieException ex) {
            // expected
        }
        cookie.setDomain("www.otherdomain.com");
        try {
            h.validate(cookie, origin);
            Assert.fail("MalformedCookieException should have been thrown");
        } catch (final MalformedCookieException ex) {
            // expected
        }
    }

    @Test
    public void testNetscapeDomainValidate3() throws Exception {
        final BasicClientCookie cookie = new BasicClientCookie("name", "value");
        final CookieOrigin origin = new CookieOrigin("www.a.com", 80, "/", false);
        final CookieAttributeHandler h = NetscapeDomainHandler.INSTANCE;

        cookie.setDomain(".a.com");
        h.validate(cookie, origin);

        cookie.setDomain(".com");
        try {
            h.validate(cookie, origin);
            Assert.fail("MalformedCookieException should have been thrown");
        } catch (final MalformedCookieException ex) {
            // expected
        }
    }

    @Test
    public void testNetscapeDomainValidate4() throws Exception {
        final BasicClientCookie cookie = new BasicClientCookie("name", "value");
        final CookieOrigin origin = new CookieOrigin("www.a.b.c", 80, "/", false);
        final CookieAttributeHandler h = NetscapeDomainHandler.INSTANCE;

        cookie.setDomain(".a.b.c");
        h.validate(cookie, origin);

        cookie.setDomain(".b.c");
        try {
            h.validate(cookie, origin);
            Assert.fail("MalformedCookieException should have been thrown");
        } catch (final MalformedCookieException ex) {
            // expected
        }
    }

    @Test
    public void testNetscapeDomainMatch1() throws Exception {
        final BasicClientCookie cookie = new BasicClientCookie("name", "value");
        final CookieOrigin origin = new CookieOrigin("www.somedomain.com", 80, "/", false);
        final CookieAttributeHandler h = NetscapeDomainHandler.INSTANCE;

        cookie.setDomain(null);
        Assert.assertFalse(h.match(cookie, origin));

        cookie.setDomain(".somedomain.com");
        Assert.assertTrue(h.match(cookie, origin));
    }

    @Test
    public void testNetscapeDomainMatch2() throws Exception {
        final BasicClientCookie cookie = new BasicClientCookie("name", "value");
        final CookieOrigin origin = new CookieOrigin("www.whatever.somedomain.com", 80, "/", false);
        final CookieAttributeHandler h = NetscapeDomainHandler.INSTANCE;

        cookie.setDomain(".somedomain.com");
        Assert.assertTrue(h.match(cookie, origin));
    }

    @Test
    public void testNetscapeDomainInvalidInput() throws Exception {
        final CookieAttributeHandler h = NetscapeDomainHandler.INSTANCE;
        try {
            h.match(null, null);
            Assert.fail("IllegalArgumentException must have been thrown");
        } catch (final IllegalArgumentException ex) {
            // expected
        }
        try {
            h.match(new BasicClientCookie("name", "value"), null);
            Assert.fail("IllegalArgumentException must have been thrown");
        } catch (final IllegalArgumentException ex) {
            // expected
        }
    }

}
