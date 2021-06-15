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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.http.Consts;
import org.junit.Assert;
import org.junit.Test;

public class TestPublicSuffixListParser {

    @Test
    public void testParse() throws Exception {
        final ClassLoader classLoader = getClass().getClassLoader();
        final InputStream in = classLoader.getResourceAsStream("suffixlist.txt");
        Assert.assertNotNull(in);
        final PublicSuffixList suffixList;
        try {
            final PublicSuffixListParser parser = PublicSuffixListParser.INSTANCE;
            suffixList = parser.parse(new InputStreamReader(in, Consts.UTF_8));
        } finally {
            in.close();
        }
        Assert.assertNotNull(suffixList);
        Assert.assertEquals(Arrays.asList("xx", "jp", "ac.jp", "*.tokyo.jp", "no", "h\u00E5.no"), suffixList.getRules());
        Assert.assertEquals(Arrays.asList("metro.tokyo.jp"), suffixList.getExceptions());
    }

    @Test
    public void testParseByType() throws Exception {
        final ClassLoader classLoader = getClass().getClassLoader();
        final InputStream in = classLoader.getResourceAsStream("suffixlist2.txt");
        Assert.assertNotNull(in);
        final List<PublicSuffixList> suffixLists;
        try {
            final PublicSuffixListParser parser = PublicSuffixListParser.INSTANCE;
            suffixLists = parser.parseByType(new InputStreamReader(in, Consts.UTF_8));
        } finally {
            in.close();
        }
        Assert.assertNotNull(suffixLists);
        Assert.assertEquals(2, suffixLists.size());
        final PublicSuffixList publicSuffixList1 = suffixLists.get(0);
        Assert.assertNotNull(publicSuffixList1);
        Assert.assertEquals(DomainType.ICANN, publicSuffixList1.getType());
        Assert.assertEquals(Arrays.asList("jp", "ac.jp", "*.tokyo.jp"), publicSuffixList1.getRules());
        Assert.assertEquals(Arrays.asList("metro.tokyo.jp"), publicSuffixList1.getExceptions());

        final PublicSuffixList publicSuffixList2 = suffixLists.get(1);
        Assert.assertNotNull(publicSuffixList2);
        Assert.assertEquals(DomainType.PRIVATE, publicSuffixList2.getType());
        Assert.assertEquals(Arrays.asList("googleapis.com", "googlecode.com"), publicSuffixList2.getRules());
        Assert.assertEquals(Collections.<String>emptyList(), publicSuffixList2.getExceptions());

    }
}
