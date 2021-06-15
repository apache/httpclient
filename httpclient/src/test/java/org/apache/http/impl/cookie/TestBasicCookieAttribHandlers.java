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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

import org.apache.http.client.utils.DateUtils;
import org.apache.http.conn.util.DomainType;
import org.apache.http.conn.util.PublicSuffixMatcher;
import org.apache.http.cookie.ClientCookie;
import org.apache.http.cookie.CookieAttributeHandler;
import org.apache.http.cookie.CookieOrigin;
import org.apache.http.cookie.MalformedCookieException;
import org.junit.Assert;
import org.junit.Test;

public class TestBasicCookieAttribHandlers {

    @Test
    public void testBasicDomainParse() throws Exception {
        final BasicClientCookie cookie = new BasicClientCookie("name", "value");
        final CookieAttributeHandler h = BasicDomainHandler.INSTANCE;
        h.parse(cookie, "www.somedomain.com");
        Assert.assertEquals("www.somedomain.com", cookie.getDomain());
    }

    @Test(expected=MalformedCookieException.class)
    public void testBasicDomainParseInvalid1() throws Exception {
        final BasicClientCookie cookie = new BasicClientCookie("name", "value");
        final CookieAttributeHandler h = BasicDomainHandler.INSTANCE;
        h.parse(cookie, "");
    }

    @Test(expected=MalformedCookieException.class)
    public void testBasicDomainParseInvalid2() throws Exception {
        final BasicClientCookie cookie = new BasicClientCookie("name", "value");
        final CookieAttributeHandler h = BasicDomainHandler.INSTANCE;
        h.parse(cookie, null);
    }

    @Test
    public void testBasicDomainValidate1() throws Exception {
        final BasicClientCookie cookie = new BasicClientCookie("name", "value");
        final CookieOrigin origin = new CookieOrigin("www.somedomain.com", 80, "/", false);
        final CookieAttributeHandler h = BasicDomainHandler.INSTANCE;

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
    public void testBasicDomainValidate2() throws Exception {
        final BasicClientCookie cookie = new BasicClientCookie("name", "value");
        final CookieOrigin origin = new CookieOrigin("somehost", 80, "/", false);
        final CookieAttributeHandler h = BasicDomainHandler.INSTANCE;

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
    public void testBasicDomainValidate3() throws Exception {
        final BasicClientCookie cookie = new BasicClientCookie("name", "value");
        final CookieOrigin origin = new CookieOrigin("somedomain.com", 80, "/", false);
        final CookieAttributeHandler h = BasicDomainHandler.INSTANCE;

        cookie.setDomain(".somedomain.com");
        h.validate(cookie, origin);
    }

    @Test
    public void testBasicDomainValidate4() throws Exception {
        final BasicClientCookie cookie = new BasicClientCookie("name", "value");
        final CookieOrigin origin = new CookieOrigin("somedomain.com", 80, "/", false);
        final CookieAttributeHandler h = BasicDomainHandler.INSTANCE;

        cookie.setDomain(null);
        try {
            h.validate(cookie, origin);
            Assert.fail("MalformedCookieException should have been thrown");
        } catch (final MalformedCookieException ex) {
            // expected
        }
    }

    @Test
    public void testBasicDomainMatch1() throws Exception {
        final BasicClientCookie cookie = new BasicClientCookie("name", "value");
        final CookieOrigin origin = new CookieOrigin("somedomain.com", 80, "/", false);
        final CookieAttributeHandler h = BasicDomainHandler.INSTANCE;

        cookie.setDomain("somedomain.com");
        cookie.setAttribute(ClientCookie.DOMAIN_ATTR, "somedomain.com");
        Assert.assertTrue(h.match(cookie, origin));

        cookie.setDomain(".somedomain.com");
        Assert.assertTrue(h.match(cookie, origin));
    }

    @Test
    public void testBasicDomainMatch2() throws Exception {
        final BasicClientCookie cookie = new BasicClientCookie("name", "value");
        final CookieOrigin origin = new CookieOrigin("www.somedomain.com", 80, "/", false);
        final CookieAttributeHandler h = BasicDomainHandler.INSTANCE;

        cookie.setDomain("somedomain.com");
        cookie.setAttribute(ClientCookie.DOMAIN_ATTR, "somedomain.com");
        Assert.assertTrue(h.match(cookie, origin));

        cookie.setDomain(".somedomain.com");
        Assert.assertTrue(h.match(cookie, origin));

        cookie.setDomain(null);
        Assert.assertFalse(h.match(cookie, origin));
    }

    @Test
    public void testBasicDomainMatchOneLetterPrefix() throws Exception {
        final BasicClientCookie cookie = new BasicClientCookie("name", "value");
        final CookieOrigin origin = new CookieOrigin("a.somedomain.com", 80, "/", false);
        final CookieAttributeHandler h = BasicDomainHandler.INSTANCE;

        cookie.setDomain("somedomain.com");
        cookie.setAttribute(ClientCookie.DOMAIN_ATTR, "somedomain.com");
        Assert.assertTrue(h.match(cookie, origin));
    }

    @Test
    public void testBasicDomainMatchMixedCase() throws Exception {
        final BasicClientCookie cookie = new BasicClientCookie("name", "value");
        final CookieOrigin origin = new CookieOrigin("a.SomeDomain.com", 80, "/", false);
        final CookieAttributeHandler h = BasicDomainHandler.INSTANCE;

        cookie.setDomain("somedoMain.Com");
        cookie.setAttribute(ClientCookie.DOMAIN_ATTR, "somedoMain.Com");
        Assert.assertTrue(h.match(cookie, origin));
    }

    @Test
    public void testBasicDomainInvalidInput() throws Exception {
        final CookieAttributeHandler h = BasicDomainHandler.INSTANCE;
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
    public void testBasicPathParse() throws Exception {
        final BasicClientCookie cookie = new BasicClientCookie("name", "value");
        final CookieAttributeHandler h = BasicPathHandler.INSTANCE;
        h.parse(cookie, "stuff");
        Assert.assertEquals("stuff", cookie.getPath());
        h.parse(cookie, "");
        Assert.assertEquals("/", cookie.getPath());
        h.parse(cookie, null);
        Assert.assertEquals("/", cookie.getPath());
    }

    @Test
    public void testBasicPathMatch1() throws Exception {
        final BasicClientCookie cookie = new BasicClientCookie("name", "value");
        final CookieOrigin origin = new CookieOrigin("somehost", 80, "/stuff", false);
        final CookieAttributeHandler h = BasicPathHandler.INSTANCE;
        cookie.setPath("/stuff");
        Assert.assertTrue(h.match(cookie, origin));
    }

    @Test
    public void testBasicPathMatch2() throws Exception {
        final BasicClientCookie cookie = new BasicClientCookie("name", "value");
        final CookieOrigin origin = new CookieOrigin("somehost", 80, "/stuff/", false);
        final CookieAttributeHandler h = BasicPathHandler.INSTANCE;
        cookie.setPath("/stuff");
        Assert.assertTrue(h.match(cookie, origin));
    }

    @Test
    public void testBasicPathMatch3() throws Exception {
        final BasicClientCookie cookie = new BasicClientCookie("name", "value");
        final CookieOrigin origin = new CookieOrigin("somehost", 80, "/stuff/more-stuff", false);
        final CookieAttributeHandler h = BasicPathHandler.INSTANCE;
        cookie.setPath("/stuff");
        Assert.assertTrue(h.match(cookie, origin));
    }

    @Test
    public void testBasicPathMatch4() throws Exception {
        final BasicClientCookie cookie = new BasicClientCookie("name", "value");
        final CookieOrigin origin = new CookieOrigin("somehost", 80, "/stuffed", false);
        final CookieAttributeHandler h = BasicPathHandler.INSTANCE;
        cookie.setPath("/stuff");
        Assert.assertFalse(h.match(cookie, origin));
    }

    @Test
    public void testBasicPathMatch5() throws Exception {
        final BasicClientCookie cookie = new BasicClientCookie("name", "value");
        final CookieOrigin origin = new CookieOrigin("somehost", 80, "/otherstuff", false);
        final CookieAttributeHandler h = BasicPathHandler.INSTANCE;
        cookie.setPath("/stuff");
        Assert.assertFalse(h.match(cookie, origin));
    }

    @Test
    public void testBasicPathMatch6() throws Exception {
        final BasicClientCookie cookie = new BasicClientCookie("name", "value");
        final CookieOrigin origin = new CookieOrigin("somehost", 80, "/stuff", false);
        final CookieAttributeHandler h = BasicPathHandler.INSTANCE;
        cookie.setPath("/stuff/");
        Assert.assertTrue(h.match(cookie, origin));
    }

    @Test
    public void testBasicPathMatch7() throws Exception {
        final BasicClientCookie cookie = new BasicClientCookie("name", "value");
        final CookieOrigin origin = new CookieOrigin("somehost", 80, "/stuff", false);
        final CookieAttributeHandler h = BasicPathHandler.INSTANCE;
        Assert.assertTrue(h.match(cookie, origin));
    }

    @Test
    public void testBasicPathInvalidInput() throws Exception {
        final CookieAttributeHandler h = BasicPathHandler.INSTANCE;
        try {
            h.parse(null, null);
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
    public void testBasicMaxAgeParse() throws Exception {
        final BasicClientCookie cookie = new BasicClientCookie("name", "value");
        final CookieAttributeHandler h = BasicMaxAgeHandler.INSTANCE;
        h.parse(cookie, "2000");
        Assert.assertNotNull(cookie.getExpiryDate());
    }

    @Test
    public void testBasicMaxAgeParseInvalid() throws Exception {
        final BasicClientCookie cookie = new BasicClientCookie("name", "value");
        final CookieAttributeHandler h = BasicMaxAgeHandler.INSTANCE;
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
    }

    @Test
    public void testBasicMaxAgeInvalidInput() throws Exception {
        final CookieAttributeHandler h = BasicMaxAgeHandler.INSTANCE;
        try {
            h.parse(null, null);
            Assert.fail("IllegalArgumentException must have been thrown");
        } catch (final IllegalArgumentException ex) {
            // expected
        }
    }

    @Test
    public void testBasicCommentParse() throws Exception {
        final BasicClientCookie cookie = new BasicClientCookie("name", "value");
        final CookieAttributeHandler h = BasicCommentHandler.INSTANCE;
        h.parse(cookie, "whatever");
        Assert.assertEquals("whatever", cookie.getComment());
        h.parse(cookie, null);
        Assert.assertEquals(null, cookie.getComment());
    }

    @Test
    public void testBasicCommentInvalidInput() throws Exception {
        final CookieAttributeHandler h = BasicCommentHandler.INSTANCE;
        try {
            h.parse(null, null);
            Assert.fail("IllegalArgumentException must have been thrown");
        } catch (final IllegalArgumentException ex) {
            // expected
        }
    }

    @Test
    public void testBasicSecureParse() throws Exception {
        final BasicClientCookie cookie = new BasicClientCookie("name", "value");
        final CookieAttributeHandler h = BasicSecureHandler.INSTANCE;
        h.parse(cookie, "whatever");
        Assert.assertTrue(cookie.isSecure());
        h.parse(cookie, null);
        Assert.assertTrue(cookie.isSecure());
    }

    @Test
    public void testBasicSecureMatch() throws Exception {
        final BasicClientCookie cookie = new BasicClientCookie("name", "value");
        final CookieAttributeHandler h = BasicSecureHandler.INSTANCE;

        final CookieOrigin origin1 = new CookieOrigin("somehost", 80, "/stuff", false);
        cookie.setSecure(false);
        Assert.assertTrue(h.match(cookie, origin1));
        cookie.setSecure(true);
        Assert.assertFalse(h.match(cookie, origin1));

        final CookieOrigin origin2 = new CookieOrigin("somehost", 80, "/stuff", true);
        cookie.setSecure(false);
        Assert.assertTrue(h.match(cookie, origin2));
        cookie.setSecure(true);
        Assert.assertTrue(h.match(cookie, origin2));
    }

    @Test
    public void testBasicSecureInvalidInput() throws Exception {
        final CookieAttributeHandler h = BasicSecureHandler.INSTANCE;
        try {
            h.parse(null, null);
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
    public void testBasicExpiresParse() throws Exception {
        final BasicClientCookie cookie = new BasicClientCookie("name", "value");
        final CookieAttributeHandler h = new BasicExpiresHandler(new String[] {DateUtils.PATTERN_RFC1123});

        final DateFormat dateformat = new SimpleDateFormat(DateUtils.PATTERN_RFC1123, Locale.US);
        dateformat.setTimeZone(DateUtils.GMT);

        final Date now = new Date();

        h.parse(cookie, dateformat.format(now));
        Assert.assertNotNull(cookie.getExpiryDate());
    }

    @Test
    public void testBasicExpiresParseInvalid() throws Exception {
        final BasicClientCookie cookie = new BasicClientCookie("name", "value");
        final CookieAttributeHandler h = new BasicExpiresHandler(new String[] {DateUtils.PATTERN_RFC1123});
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
    }

    @SuppressWarnings("unused")
    @Test
    public void testBasicExpiresInvalidInput() throws Exception {
        try {
            new BasicExpiresHandler(null);
            Assert.fail("IllegalArgumentException must have been thrown");
        } catch (final IllegalArgumentException ex) {
            // expected
        }
        final CookieAttributeHandler h = new BasicExpiresHandler(new String[] {DateUtils.PATTERN_RFC1123});
        try {
            h.parse(null, null);
            Assert.fail("IllegalArgumentException must have been thrown");
        } catch (final IllegalArgumentException ex) {
            // expected
        }
    }

    @Test
    public void testPublicSuffixFilter() throws Exception {
        final BasicClientCookie cookie = new BasicClientCookie("name", "value");

        final PublicSuffixMatcher matcher = new PublicSuffixMatcher(DomainType.ICANN, Arrays.asList("co.uk", "com"), null);
        final PublicSuffixDomainFilter h = new PublicSuffixDomainFilter(RFC2109DomainHandler.INSTANCE, matcher);

        cookie.setDomain(".co.uk");
        Assert.assertFalse(h.match(cookie, new CookieOrigin("apache.co.uk", 80, "/stuff", false)));

        cookie.setDomain("co.uk");
        Assert.assertFalse(h.match(cookie, new CookieOrigin("apache.co.uk", 80, "/stuff", false)));

        cookie.setDomain(".co.com");
        Assert.assertTrue(h.match(cookie, new CookieOrigin("apache.co.com", 80, "/stuff", false)));

        cookie.setDomain("co.com");
        Assert.assertFalse(h.match(cookie, new CookieOrigin("apache.co.com", 80, "/stuff", false)));

        cookie.setDomain(".com");
        Assert.assertFalse(h.match(cookie, new CookieOrigin("apache.com", 80, "/stuff", false)));

        cookie.setDomain("com");
        Assert.assertFalse(h.match(cookie, new CookieOrigin("apache.com", 80, "/stuff", false)));

        cookie.setDomain("apache.com");
        Assert.assertTrue(h.match(cookie, new CookieOrigin("apache.com", 80, "/stuff", false)));

        cookie.setDomain(".apache.com");
        Assert.assertTrue(h.match(cookie, new CookieOrigin("www.apache.com", 80, "/stuff", false)));

        cookie.setDomain("localhost");
        Assert.assertTrue(h.match(cookie, new CookieOrigin("localhost", 80, "/stuff", false)));
    }

}
