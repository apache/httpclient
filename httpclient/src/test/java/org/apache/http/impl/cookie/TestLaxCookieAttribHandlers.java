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

import java.util.Calendar;
import java.util.Date;

import org.apache.http.cookie.CookieAttributeHandler;
import org.apache.http.cookie.MalformedCookieException;
import org.junit.Assert;
import org.junit.Test;

public class TestLaxCookieAttribHandlers {

    @Test
    public void testParseMaxAge() throws Exception {
        final BasicClientCookie cookie = new BasicClientCookie("name", "value");
        final CookieAttributeHandler h = LaxMaxAgeHandler.INSTANCE;
        h.parse(cookie, "2000");
        Assert.assertNotNull(cookie.getExpiryDate());
    }

    @Test
    public void testParseMaxNegative() throws Exception {
        final BasicClientCookie cookie = new BasicClientCookie("name", "value");
        final CookieAttributeHandler h = LaxMaxAgeHandler.INSTANCE;
        h.parse(cookie, "-2000");
        Assert.assertNotNull(cookie.getExpiryDate());
    }

    @Test
    public void testParseMaxZero() throws Exception {
        final BasicClientCookie cookie = new BasicClientCookie("name", "value");
        final CookieAttributeHandler h = LaxMaxAgeHandler.INSTANCE;
        h.parse(cookie, "0000");
        Assert.assertNotNull(cookie.getExpiryDate());
    }

    @Test
    public void testBasicMaxAgeParseEmpty() throws Exception {
        final BasicClientCookie cookie = new BasicClientCookie("name", "value");
        final CookieAttributeHandler h = LaxMaxAgeHandler.INSTANCE;
        h.parse(cookie, "  ");
        Assert.assertNull(cookie.getExpiryDate());
    }

    @Test
    public void testBasicMaxAgeParseInvalid() throws Exception {
        final BasicClientCookie cookie = new BasicClientCookie("name", "value");
        final CookieAttributeHandler h = LaxMaxAgeHandler.INSTANCE;
        h.parse(cookie, "garbage");
        Assert.assertNull(cookie.getExpiryDate());
    }

    @Test
    public void testBasicMaxAgeInvalidInput() throws Exception {
        final CookieAttributeHandler h = LaxMaxAgeHandler.INSTANCE;
        try {
            h.parse(null, "stuff");
            Assert.fail("IllegalArgumentException must have been thrown");
        } catch (final IllegalArgumentException ex) {
            // expected
        }
    }

    @Test(expected = MalformedCookieException.class)
    public void testExpiryGarbage() throws Exception {
        final BasicClientCookie cookie = new BasicClientCookie("name", "value");
        final CookieAttributeHandler h = LaxExpiresHandler.INSTANCE;
        h.parse(cookie, ";;blah,blah;yada  ");
    }

    @Test
    public void testParseExpiry() throws Exception {
        final BasicClientCookie cookie = new BasicClientCookie("name", "value");
        final CookieAttributeHandler h = LaxExpiresHandler.INSTANCE;
        h.parse(cookie, "1:0:12 8-jan-2012");

        final Date expiryDate = cookie.getExpiryDate();
        Assert.assertNotNull(expiryDate);
        final Calendar c = Calendar.getInstance();
        c.setTimeZone(LaxExpiresHandler.UTC);
        c.setTime(expiryDate);
        Assert.assertEquals(2012, c.get(Calendar.YEAR));
        Assert.assertEquals(Calendar.JANUARY, c.get(Calendar.MONTH));
        Assert.assertEquals(8, c.get(Calendar.DAY_OF_MONTH));
        Assert.assertEquals(1, c.get(Calendar.HOUR_OF_DAY));
        Assert.assertEquals(0, c.get(Calendar.MINUTE));
        Assert.assertEquals(12, c.get(Calendar.SECOND));
        Assert.assertEquals(0, c.get(Calendar.MILLISECOND));
    }

    @Test
    public void testParseExpiryInvalidTime0() throws Exception {
        final BasicClientCookie cookie = new BasicClientCookie("name", "value");
        final CookieAttributeHandler h = LaxExpiresHandler.INSTANCE;
        h.parse(cookie, null);
        Assert.assertNull(cookie.getExpiryDate());
    }

    @Test(expected = MalformedCookieException.class)
    public void testParseExpiryInvalidTime1() throws Exception {
        final BasicClientCookie cookie = new BasicClientCookie("name", "value");
        final CookieAttributeHandler h = LaxExpiresHandler.INSTANCE;
        h.parse(cookie, "1:0:122 8 dec 1980");
    }

    @Test(expected = MalformedCookieException.class)
    public void testParseExpiryInvalidTime2() throws Exception {
        final BasicClientCookie cookie = new BasicClientCookie("name", "value");
        final CookieAttributeHandler h = LaxExpiresHandler.INSTANCE;
        h.parse(cookie, "24:00:00 8 dec 1980");
    }

    @Test(expected = MalformedCookieException.class)
    public void testParseExpiryInvalidTime3() throws Exception {
        final BasicClientCookie cookie = new BasicClientCookie("name", "value");
        final CookieAttributeHandler h = LaxExpiresHandler.INSTANCE;
        h.parse(cookie, "23:60:00 8 dec 1980");
    }

    @Test(expected = MalformedCookieException.class)
    public void testParseExpiryInvalidTime4() throws Exception {
        final BasicClientCookie cookie = new BasicClientCookie("name", "value");
        final CookieAttributeHandler h = LaxExpiresHandler.INSTANCE;
        h.parse(cookie, "23:00:60 8 dec 1980");
    }

    @Test
    public void testParseExpiryFunnyTime() throws Exception {
        final BasicClientCookie cookie = new BasicClientCookie("name", "value");
        final CookieAttributeHandler h = LaxExpiresHandler.INSTANCE;
        h.parse(cookie, "1:59:00blah; 8-feb-2000");

        final Date expiryDate = cookie.getExpiryDate();
        Assert.assertNotNull(expiryDate);
        final Calendar c = Calendar.getInstance();
        c.setTimeZone(LaxExpiresHandler.UTC);
        c.setTime(expiryDate);
        Assert.assertEquals(2000, c.get(Calendar.YEAR));
        Assert.assertEquals(Calendar.FEBRUARY, c.get(Calendar.MONTH));
        Assert.assertEquals(8, c.get(Calendar.DAY_OF_MONTH));
        Assert.assertEquals(1, c.get(Calendar.HOUR_OF_DAY));
        Assert.assertEquals(59, c.get(Calendar.MINUTE));
        Assert.assertEquals(0, c.get(Calendar.SECOND));
        Assert.assertEquals(0, c.get(Calendar.MILLISECOND));
    }

    @Test(expected = MalformedCookieException.class)
    public void testParseExpiryInvalidDayOfMonth1() throws Exception {
        final BasicClientCookie cookie = new BasicClientCookie("name", "value");
        final CookieAttributeHandler h = LaxExpiresHandler.INSTANCE;
        h.parse(cookie, "12:00:00 888 mar 1880");
    }

    @Test(expected = MalformedCookieException.class)
    public void testParseExpiryInvalidDayOfMonth2() throws Exception {
        final BasicClientCookie cookie = new BasicClientCookie("name", "value");
        final CookieAttributeHandler h = LaxExpiresHandler.INSTANCE;
        h.parse(cookie, "12:00:00 0 mar 1880");
    }

    @Test(expected = MalformedCookieException.class)
    public void testParseExpiryInvalidDayOfMonth3() throws Exception {
        final BasicClientCookie cookie = new BasicClientCookie("name", "value");
        final CookieAttributeHandler h = LaxExpiresHandler.INSTANCE;
        h.parse(cookie, "12:00:00 32 mar 1880");
    }

    @Test
    public void testParseExpiryFunnyDayOfMonth() throws Exception {
        final BasicClientCookie cookie = new BasicClientCookie("name", "value");
        final CookieAttributeHandler h = LaxExpiresHandler.INSTANCE;
        h.parse(cookie, "12:00:00 8blah;mar;1880");

        final Date expiryDate = cookie.getExpiryDate();
        Assert.assertNotNull(expiryDate);
        final Calendar c = Calendar.getInstance();
        c.setTimeZone(LaxExpiresHandler.UTC);
        c.setTime(expiryDate);
        Assert.assertEquals(1880, c.get(Calendar.YEAR));
        Assert.assertEquals(Calendar.MARCH, c.get(Calendar.MONTH));
        Assert.assertEquals(8, c.get(Calendar.DAY_OF_MONTH));
        Assert.assertEquals(12, c.get(Calendar.HOUR_OF_DAY));
        Assert.assertEquals(0, c.get(Calendar.MINUTE));
        Assert.assertEquals(0, c.get(Calendar.SECOND));
        Assert.assertEquals(0, c.get(Calendar.MILLISECOND));
    }

    @Test(expected = MalformedCookieException.class)
    public void testParseExpiryInvalidMonth() throws Exception {
        final BasicClientCookie cookie = new BasicClientCookie("name", "value");
        final CookieAttributeHandler h = LaxExpiresHandler.INSTANCE;
        h.parse(cookie, "1:00:00 8 dek 80");
    }

    @Test
    public void testParseExpiryFunnyMonth() throws Exception {
        final BasicClientCookie cookie = new BasicClientCookie("name", "value");
        final CookieAttributeHandler h = LaxExpiresHandler.INSTANCE;
        h.parse(cookie, "23:59:59; 1-ApriLLLLL-2008");

        final Date expiryDate = cookie.getExpiryDate();
        Assert.assertNotNull(expiryDate);
        final Calendar c = Calendar.getInstance();
        c.setTimeZone(LaxExpiresHandler.UTC);
        c.setTime(expiryDate);
        Assert.assertEquals(2008, c.get(Calendar.YEAR));
        Assert.assertEquals(Calendar.APRIL, c.get(Calendar.MONTH));
        Assert.assertEquals(1, c.get(Calendar.DAY_OF_MONTH));
        Assert.assertEquals(23, c.get(Calendar.HOUR_OF_DAY));
        Assert.assertEquals(59, c.get(Calendar.MINUTE));
        Assert.assertEquals(59, c.get(Calendar.SECOND));
        Assert.assertEquals(0, c.get(Calendar.MILLISECOND));
    }

    @Test(expected = MalformedCookieException.class)
    public void testParseExpiryInvalidYearTooShort() throws Exception {
        final BasicClientCookie cookie = new BasicClientCookie("name", "value");
        final CookieAttributeHandler h = LaxExpiresHandler.INSTANCE;
        h.parse(cookie, "1:00:00 8 dec 8");
    }

    @Test(expected = MalformedCookieException.class)
    public void testParseExpiryInvalidYearTooLong() throws Exception {
        final BasicClientCookie cookie = new BasicClientCookie("name", "value");
        final CookieAttributeHandler h = LaxExpiresHandler.INSTANCE;
        h.parse(cookie, "1:00:00 8 dec 88888");
    }

    @Test(expected = MalformedCookieException.class)
    public void testParseExpiryInvalidYearTooLongAgo() throws Exception {
        final BasicClientCookie cookie = new BasicClientCookie("name", "value");
        final CookieAttributeHandler h = LaxExpiresHandler.INSTANCE;
        h.parse(cookie, "1:00:00 8 dec 1600");
    }

    @Test
    public void testParseExpiryFunnyYear() throws Exception {
        final BasicClientCookie cookie = new BasicClientCookie("name", "value");
        final CookieAttributeHandler h = LaxExpiresHandler.INSTANCE;
        h.parse(cookie, "23:59:59; 1-Apr-2008blah");

        final Date expiryDate = cookie.getExpiryDate();
        Assert.assertNotNull(expiryDate);
        final Calendar c = Calendar.getInstance();
        c.setTimeZone(LaxExpiresHandler.UTC);
        c.setTime(expiryDate);
        Assert.assertEquals(2008, c.get(Calendar.YEAR));
        Assert.assertEquals(Calendar.APRIL, c.get(Calendar.MONTH));
        Assert.assertEquals(1, c.get(Calendar.DAY_OF_MONTH));
        Assert.assertEquals(23, c.get(Calendar.HOUR_OF_DAY));
        Assert.assertEquals(59, c.get(Calendar.MINUTE));
        Assert.assertEquals(59, c.get(Calendar.SECOND));
        Assert.assertEquals(0, c.get(Calendar.MILLISECOND));
    }

    @Test
    public void testParseExpiryYearTwoDigit1() throws Exception {
        final BasicClientCookie cookie = new BasicClientCookie("name", "value");
        final CookieAttributeHandler h = LaxExpiresHandler.INSTANCE;
        h.parse(cookie, "23:59:59; 1-Apr-70");

        final Date expiryDate = cookie.getExpiryDate();
        Assert.assertNotNull(expiryDate);
        final Calendar c = Calendar.getInstance();
        c.setTimeZone(LaxExpiresHandler.UTC);
        c.setTime(expiryDate);
        Assert.assertEquals(1970, c.get(Calendar.YEAR));
    }

    @Test
    public void testParseExpiryYearTwoDigit2() throws Exception {
        final BasicClientCookie cookie = new BasicClientCookie("name", "value");
        final CookieAttributeHandler h = LaxExpiresHandler.INSTANCE;
        h.parse(cookie, "23:59:59; 1-Apr-99");

        final Date expiryDate = cookie.getExpiryDate();
        Assert.assertNotNull(expiryDate);
        final Calendar c = Calendar.getInstance();
        c.setTimeZone(LaxExpiresHandler.UTC);
        c.setTime(expiryDate);
        Assert.assertEquals(1999, c.get(Calendar.YEAR));
    }

    @Test
    public void testParseExpiryYearTwoDigit3() throws Exception {
        final BasicClientCookie cookie = new BasicClientCookie("name", "value");
        final CookieAttributeHandler h = LaxExpiresHandler.INSTANCE;
        h.parse(cookie, "23:59:59; 1-Apr-00");

        final Date expiryDate = cookie.getExpiryDate();
        Assert.assertNotNull(expiryDate);
        final Calendar c = Calendar.getInstance();
        c.setTimeZone(LaxExpiresHandler.UTC);
        c.setTime(expiryDate);
        Assert.assertEquals(2000, c.get(Calendar.YEAR));
    }

}
