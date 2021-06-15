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

package org.apache.http.conn.util;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestPublicSuffixMatcher {

    private static final String SOURCE_FILE = "suffixlistmatcher.txt";

    private PublicSuffixMatcher matcher;

    private static final Charset UTF_8 = Charset.forName("UTF-8");

    @Before
    public void setUp() throws Exception {
        final ClassLoader classLoader = getClass().getClassLoader();
        final InputStream in = classLoader.getResourceAsStream(SOURCE_FILE);
        Assert.assertNotNull(in);
        final List<PublicSuffixList> lists = PublicSuffixListParser.INSTANCE.parseByType(
                new InputStreamReader(in, UTF_8));
        matcher = new PublicSuffixMatcher(lists);
    }

    @Test
    public void testGetDomainRootAnyType() {
        // Private
        Assert.assertEquals("xx", matcher.getDomainRoot("example.XX"));
        Assert.assertEquals("xx", matcher.getDomainRoot("www.example.XX"));
        Assert.assertEquals("xx", matcher.getDomainRoot("www.blah.blah.example.XX"));
        Assert.assertEquals("appspot.com", matcher.getDomainRoot("example.appspot.com"));
        // Too short
        Assert.assertEquals(null, matcher.getDomainRoot("jp"));
        Assert.assertEquals(null, matcher.getDomainRoot("ac.jp"));
        Assert.assertEquals(null, matcher.getDomainRoot("any.tokyo.jp"));
        // ICANN
        Assert.assertEquals("metro.tokyo.jp", matcher.getDomainRoot("metro.tokyo.jp"));
        Assert.assertEquals("blah.blah.tokyo.jp", matcher.getDomainRoot("blah.blah.tokyo.jp"));
        Assert.assertEquals("blah.ac.jp", matcher.getDomainRoot("blah.blah.ac.jp"));
        // Unknown
        Assert.assertEquals("garbage", matcher.getDomainRoot("garbage"));
        Assert.assertEquals("garbage", matcher.getDomainRoot("garbage.garbage"));
        Assert.assertEquals("garbage", matcher.getDomainRoot("*.garbage.garbage"));
        Assert.assertEquals("garbage", matcher.getDomainRoot("*.garbage.garbage.garbage"));
    }

    @Test
    public void testGetDomainRootOnlyPRIVATE() {
        // Private
        Assert.assertEquals("xx", matcher.getDomainRoot("example.XX", DomainType.PRIVATE));
        Assert.assertEquals("xx", matcher.getDomainRoot("www.example.XX", DomainType.PRIVATE));
        Assert.assertEquals("xx", matcher.getDomainRoot("www.blah.blah.example.XX", DomainType.PRIVATE));
        Assert.assertEquals("appspot.com", matcher.getDomainRoot("example.appspot.com"));
        // Too short
        Assert.assertEquals(null, matcher.getDomainRoot("jp", DomainType.PRIVATE));
        Assert.assertEquals(null, matcher.getDomainRoot("ac.jp", DomainType.PRIVATE));
        Assert.assertEquals(null, matcher.getDomainRoot("any.tokyo.jp", DomainType.PRIVATE));
        // ICANN
        Assert.assertEquals(null, matcher.getDomainRoot("metro.tokyo.jp", DomainType.PRIVATE));
        Assert.assertEquals(null, matcher.getDomainRoot("blah.blah.tokyo.jp", DomainType.PRIVATE));
        Assert.assertEquals(null, matcher.getDomainRoot("blah.blah.ac.jp", DomainType.PRIVATE));
        // Unknown
        Assert.assertEquals(null, matcher.getDomainRoot("garbage", DomainType.PRIVATE));
        Assert.assertEquals(null, matcher.getDomainRoot("garbage.garbage", DomainType.PRIVATE));
        Assert.assertEquals(null, matcher.getDomainRoot("*.garbage.garbage", DomainType.PRIVATE));
        Assert.assertEquals(null, matcher.getDomainRoot("*.garbage.garbage.garbage", DomainType.PRIVATE));
    }

    @Test
    public void testGetDomainRootOnlyICANN() {
        // Private
        Assert.assertEquals(null, matcher.getDomainRoot("example.XX", DomainType.ICANN));
        Assert.assertEquals(null, matcher.getDomainRoot("www.example.XX", DomainType.ICANN));
        Assert.assertEquals(null, matcher.getDomainRoot("www.blah.blah.example.XX", DomainType.ICANN));
        // Too short
        Assert.assertEquals(null, matcher.getDomainRoot("xx", DomainType.ICANN));
        Assert.assertEquals(null, matcher.getDomainRoot("jp", DomainType.ICANN));
        Assert.assertEquals(null, matcher.getDomainRoot("ac.jp", DomainType.ICANN));
        Assert.assertEquals(null, matcher.getDomainRoot("any.tokyo.jp", DomainType.ICANN));
        // ICANN
        Assert.assertEquals("metro.tokyo.jp", matcher.getDomainRoot("metro.tokyo.jp", DomainType.ICANN));
        Assert.assertEquals("blah.blah.tokyo.jp", matcher.getDomainRoot("blah.blah.tokyo.jp", DomainType.ICANN));
        Assert.assertEquals("blah.ac.jp", matcher.getDomainRoot("blah.blah.ac.jp", DomainType.ICANN));
        // Unknown
        Assert.assertEquals(null, matcher.getDomainRoot("garbage", DomainType.ICANN));
        Assert.assertEquals(null, matcher.getDomainRoot("garbage.garbage", DomainType.ICANN));
        Assert.assertEquals(null, matcher.getDomainRoot("*.garbage.garbage", DomainType.ICANN));
        Assert.assertEquals(null, matcher.getDomainRoot("*.garbage.garbage.garbage", DomainType.ICANN));
    }


    @Test
    public void testMatch() {
        Assert.assertTrue(matcher.matches(".jp"));
        Assert.assertTrue(matcher.matches(".ac.jp"));
        Assert.assertTrue(matcher.matches(".any.tokyo.jp"));
        // exception
        Assert.assertFalse(matcher.matches(".metro.tokyo.jp"));
        Assert.assertFalse(matcher.matches(".xx"));
        Assert.assertFalse(matcher.matches(".appspot.com"));
    }

    @Test
    public void testMatchUnicode() {
        Assert.assertTrue(matcher.matches(".h\u00E5.no")); // \u00E5 is <aring>
        Assert.assertTrue(matcher.matches(".xn--h-2fa.no"));
        Assert.assertTrue(matcher.matches(".h\u00E5.no"));
        Assert.assertTrue(matcher.matches(".xn--h-2fa.no"));
    }

}
