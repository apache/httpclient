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

public class TestRFC2109CookieAttribHandlers {

    @Test
    public void testRFC2109DomainParse() throws Exception {
        final BasicClientCookie cookie = new BasicClientCookie("name", "value");
        final CookieAttributeHandler h = RFC2109DomainHandler.INSTANCE;

        h.parse(cookie, "somehost");
        Assert.assertEquals("somehost", cookie.getDomain());

        try {
            h.parse(cookie, null);
            Assert.fail("MalformedCookieException should have been thrown");
        } catch (final MalformedCookieException ex) {
            // expected
        }
        try {
            h.parse(cookie, "  ");
            Assert.fail("MalformedCookieException should have been thrown");
        } catch (final MalformedCookieException ex) {
            // expected
        }
    }

    @Test
    public void testRFC2109DomainValidate1() throws Exception {
        final BasicClientCookie cookie = new BasicClientCookie("name", "value");
        final CookieOrigin origin = new CookieOrigin("somehost", 80, "/", false);
        final CookieAttributeHandler h = RFC2109DomainHandler.INSTANCE;

        cookie.setDomain("somehost");
        h.validate(cookie, origin);

        cookie.setDomain("otherhost");
        try {
            h.validate(cookie, origin);
            Assert.fail("MalformedCookieException should have been thrown");
        } catch (final MalformedCookieException ex) {
            // expected
        }
        cookie.setDomain(null);
        try {
            h.validate(cookie, origin);
            Assert.fail("MalformedCookieException should have been thrown");
        } catch (final MalformedCookieException ex) {
            // expected
        }
    }

    @Test
    public void testRFC2109DomainValidate2() throws Exception {
        final BasicClientCookie cookie = new BasicClientCookie("name", "value");
        final CookieOrigin origin = new CookieOrigin("www.somedomain.com", 80, "/", false);
        final CookieAttributeHandler h = RFC2109DomainHandler.INSTANCE;

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
    public void testRFC2109DomainValidate3() throws Exception {
        final BasicClientCookie cookie = new BasicClientCookie("name", "value");
        final CookieOrigin origin = new CookieOrigin("www.a.com", 80, "/", false);
        final CookieAttributeHandler h = RFC2109DomainHandler.INSTANCE;

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
    public void testRFC2109DomainValidate4() throws Exception {
        final BasicClientCookie cookie = new BasicClientCookie("name", "value");
        final CookieOrigin origin = new CookieOrigin("www.a.b.c", 80, "/", false);
        final CookieAttributeHandler h = RFC2109DomainHandler.INSTANCE;

        cookie.setDomain(".a.b.c");
        h.validate(cookie, origin);

        cookie.setDomain(".b.c");
        try {
            h.validate(cookie, origin);
            Assert.fail("MalformedCookieException should have been thrown");
        } catch (final MalformedCookieException ex) {
            // expected
        }
        cookie.setDomain(".a.a.b.c");
        try {
            h.validate(cookie, origin);
            Assert.fail("MalformedCookieException should have been thrown");
        } catch (final MalformedCookieException ex) {
            // expected
        }
    }

    @Test
    public void testRFC2109DomainMatch1() throws Exception {
        final BasicClientCookie cookie = new BasicClientCookie("name", "value");
        final CookieOrigin origin = new CookieOrigin("www.somedomain.com", 80, "/", false);
        final CookieAttributeHandler h = RFC2109DomainHandler.INSTANCE;

        cookie.setDomain(null);
        Assert.assertFalse(h.match(cookie, origin));

        cookie.setDomain(".somedomain.com");
        Assert.assertTrue(h.match(cookie, origin));
    }

    @Test
    public void testRFC2109DomainMatch2() throws Exception {
        final BasicClientCookie cookie = new BasicClientCookie("name", "value");
        final CookieOrigin origin = new CookieOrigin("www.whatever.somedomain.com", 80, "/", false);
        final CookieAttributeHandler h = RFC2109DomainHandler.INSTANCE;

        cookie.setDomain(".somedomain.com");
        Assert.assertTrue(h.match(cookie, origin));
    }

    @Test
    public void testRFC2109DomainMatch3() throws Exception {
        final BasicClientCookie cookie = new BasicClientCookie("name", "value");
        final CookieOrigin origin = new CookieOrigin("somedomain.com", 80, "/", false);
        final CookieAttributeHandler h = RFC2109DomainHandler.INSTANCE;

        cookie.setDomain("somedomain.com");
        Assert.assertTrue(h.match(cookie, origin));
    }

    @Test
    public void testRFC2109DomainMatch4() throws Exception {
        final BasicClientCookie cookie = new BasicClientCookie("name", "value");
        final CookieOrigin origin = new CookieOrigin("www.somedomain.com", 80, "/", false);
        final CookieAttributeHandler h = RFC2109DomainHandler.INSTANCE;

        cookie.setDomain("somedomain.com");
        Assert.assertFalse(h.match(cookie, origin));
    }

    @Test
    public void testRFC2109DomainInvalidInput() throws Exception {
        final CookieAttributeHandler h = RFC2109DomainHandler.INSTANCE;
        try {
            h.parse(null, null);
            Assert.fail("IllegalArgumentException must have been thrown");
        } catch (final IllegalArgumentException ex) {
            // expected
        }
        try {
            h.validate(null, null);
            Assert.fail("IllegalArgumentException must have been thrown");
        } catch (final IllegalArgumentException ex) {
            // expected
        }
        try {
            h.validate(new BasicClientCookie("name", "value"), null);
            Assert.fail("IllegalArgumentException must have been thrown");
        } catch (final IllegalArgumentException ex) {
            // expected
        }
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

    @Test
    public void testRFC2109VersionParse() throws Exception {
        final BasicClientCookie cookie = new BasicClientCookie("name", "value");
        final CookieAttributeHandler h = RFC2109VersionHandler.INSTANCE;
        h.parse(cookie, "12");
        Assert.assertEquals(12, cookie.getVersion());
    }

    @Test
    public void testRFC2109VersionParseInvalid() throws Exception {
        final BasicClientCookie cookie = new BasicClientCookie("name", "value");
        final CookieAttributeHandler h = RFC2109VersionHandler.INSTANCE;
        try {
            h.parse(cookie, "garbage");
            Assert.fail("MalformedCookieException must have been thrown");
        } catch (final MalformedCookieException ex) {
            // expected
        }
        try {
            h.parse(cookie, null);
            Assert.fail("MalformedCookieException must have been thrown");
        } catch (final MalformedCookieException ex) {
            // expected
        }
        try {
            h.parse(cookie, "  ");
            Assert.fail("MalformedCookieException must have been thrown");
        } catch (final MalformedCookieException ex) {
            // expected
        }
    }

    @Test
    public void testRFC2109VersionValidate() throws Exception {
        final BasicClientCookie cookie = new BasicClientCookie("name", "value");
        final CookieOrigin origin = new CookieOrigin("somedomain.com", 80, "/", false);
        final CookieAttributeHandler h = RFC2109VersionHandler.INSTANCE;

        cookie.setVersion(12);
        h.validate(cookie, origin);

        cookie.setVersion(-12);
        try {
            h.validate(cookie, origin);
            Assert.fail("MalformedCookieException must have been thrown");
        } catch (final MalformedCookieException ex) {
            // expected
        }
    }

    @Test
    public void testRFC2109VersionInvalidInput() throws Exception {
        final CookieAttributeHandler h = RFC2109VersionHandler.INSTANCE;
        try {
            h.parse(null, null);
            Assert.fail("IllegalArgumentException must have been thrown");
        } catch (final IllegalArgumentException ex) {
            // expected
        }
        try {
            h.validate(null, null);
            Assert.fail("IllegalArgumentException must have been thrown");
        } catch (final IllegalArgumentException ex) {
            // expected
        }
    }

}
