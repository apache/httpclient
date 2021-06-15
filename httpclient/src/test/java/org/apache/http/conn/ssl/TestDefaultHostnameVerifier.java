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

package org.apache.http.conn.ssl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.net.ssl.SSLException;

import org.apache.http.conn.util.DomainType;
import org.apache.http.conn.util.PublicSuffixList;
import org.apache.http.conn.util.PublicSuffixListParser;
import org.apache.http.conn.util.PublicSuffixMatcher;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link org.apache.http.conn.ssl.DefaultHostnameVerifier}.
 */
public class TestDefaultHostnameVerifier {

    private DefaultHostnameVerifier impl;
    private PublicSuffixMatcher publicSuffixMatcher;
    private DefaultHostnameVerifier implWithPublicSuffixCheck;

    private static final String PUBLIC_SUFFIX_MATCHER_SOURCE_FILE = "suffixlistmatcher.txt";

    private static final Charset UTF_8 = Charset.forName("UTF-8");

    @Before
    public void setup() throws IOException {
        impl = new DefaultHostnameVerifier();

        // Load the test PublicSuffixMatcher
        final ClassLoader classLoader = getClass().getClassLoader();
        final InputStream in = classLoader.getResourceAsStream(PUBLIC_SUFFIX_MATCHER_SOURCE_FILE);
        Assert.assertNotNull(in);
        final List<PublicSuffixList> lists = PublicSuffixListParser.INSTANCE.parseByType(
                new InputStreamReader(in, UTF_8));
        publicSuffixMatcher = new PublicSuffixMatcher(lists);

        implWithPublicSuffixCheck = new DefaultHostnameVerifier(publicSuffixMatcher);
    }

    @Test
    public void testVerify() throws Exception {
        final CertificateFactory cf = CertificateFactory.getInstance("X.509");
        InputStream in;
        X509Certificate x509;
        in = new ByteArrayInputStream(CertificatesToPlayWith.X509_FOO);
        x509 = (X509Certificate) cf.generateCertificate(in);

        impl.verify("foo.com", x509);
        exceptionPlease(impl, "a.foo.com", x509);
        exceptionPlease(impl, "bar.com", x509);

        in = new ByteArrayInputStream(CertificatesToPlayWith.X509_HANAKO);
        x509 = (X509Certificate) cf.generateCertificate(in);
        impl.verify("\u82b1\u5b50.co.jp", x509);
        exceptionPlease(impl, "a.\u82b1\u5b50.co.jp", x509);

        in = new ByteArrayInputStream(CertificatesToPlayWith.X509_FOO_BAR);
        x509 = (X509Certificate) cf.generateCertificate(in);
        exceptionPlease(impl, "foo.com", x509);
        exceptionPlease(impl, "a.foo.com", x509);
        impl.verify("bar.com", x509);
        exceptionPlease(impl, "a.bar.com", x509);

        in = new ByteArrayInputStream(CertificatesToPlayWith.X509_FOO_BAR_HANAKO);
        x509 = (X509Certificate) cf.generateCertificate(in);
        exceptionPlease(impl, "foo.com", x509);
        exceptionPlease(impl, "a.foo.com", x509);
        impl.verify("bar.com", x509);
        exceptionPlease(impl, "a.bar.com", x509);

        /*
           Java isn't extracting international subjectAlts properly.  (Or
           OpenSSL isn't storing them properly).
        */
        // INSTANCE.verify("\u82b1\u5b50.co.jp", x509 );
        // impl.verify("\u82b1\u5b50.co.jp", x509 );
        exceptionPlease(impl, "a.\u82b1\u5b50.co.jp", x509);

        in = new ByteArrayInputStream(CertificatesToPlayWith.X509_NO_CNS_FOO);
        x509 = (X509Certificate) cf.generateCertificate(in);
        impl.verify("foo.com", x509);
        exceptionPlease(impl, "a.foo.com", x509);

        in = new ByteArrayInputStream(CertificatesToPlayWith.X509_NO_CNS_FOO);
        x509 = (X509Certificate) cf.generateCertificate(in);
        impl.verify("foo.com", x509);
        exceptionPlease(impl, "a.foo.com", x509);

        in = new ByteArrayInputStream(CertificatesToPlayWith.X509_THREE_CNS_FOO_BAR_HANAKO);
        x509 = (X509Certificate) cf.generateCertificate(in);
        exceptionPlease(impl, "foo.com", x509);
        exceptionPlease(impl, "a.foo.com", x509);
        exceptionPlease(impl, "bar.com", x509);
        exceptionPlease(impl, "a.bar.com", x509);
        impl.verify("\u82b1\u5b50.co.jp", x509);
        exceptionPlease(impl, "a.\u82b1\u5b50.co.jp", x509);

        in = new ByteArrayInputStream(CertificatesToPlayWith.X509_WILD_FOO);
        x509 = (X509Certificate) cf.generateCertificate(in);
        exceptionPlease(impl, "foo.com", x509);
        impl.verify("www.foo.com", x509);
        impl.verify("\u82b1\u5b50.foo.com", x509);
        exceptionPlease(impl, "a.b.foo.com", x509);

        in = new ByteArrayInputStream(CertificatesToPlayWith.X509_WILD_CO_JP);
        x509 = (X509Certificate) cf.generateCertificate(in);
        // Silly test because no-one would ever be able to lookup an IP address
        // using "*.co.jp".
        impl.verify("*.co.jp", x509);
        impl.verify("foo.co.jp", x509);
        impl.verify("\u82b1\u5b50.co.jp", x509);

        exceptionPlease(implWithPublicSuffixCheck, "foo.co.jp", x509);
        exceptionPlease(implWithPublicSuffixCheck, "\u82b1\u5b50.co.jp", x509);

        in = new ByteArrayInputStream(CertificatesToPlayWith.X509_WILD_FOO_BAR_HANAKO);
        x509 = (X509Certificate) cf.generateCertificate(in);
        // try the foo.com variations
        exceptionPlease(impl, "foo.com", x509);
        exceptionPlease(impl, "www.foo.com", x509);
        exceptionPlease(impl, "\u82b1\u5b50.foo.com", x509);
        exceptionPlease(impl, "a.b.foo.com", x509);
        // try the bar.com variations
        exceptionPlease(impl, "bar.com", x509);
        impl.verify("www.bar.com", x509);
        impl.verify("\u82b1\u5b50.bar.com", x509);
        exceptionPlease(impl, "a.b.bar.com", x509);

        in = new ByteArrayInputStream(CertificatesToPlayWith.X509_MULTIPLE_VALUE_AVA);
        x509 = (X509Certificate) cf.generateCertificate(in);
        impl.verify("repository.infonotary.com", x509);

        in = new ByteArrayInputStream(CertificatesToPlayWith.S_GOOGLE_COM);
        x509 = (X509Certificate) cf.generateCertificate(in);
        impl.verify("*.google.com", x509);

        in = new ByteArrayInputStream(CertificatesToPlayWith.S_GOOGLE_COM);
        x509 = (X509Certificate) cf.generateCertificate(in);
        impl.verify("*.Google.com", x509);

        in = new ByteArrayInputStream(CertificatesToPlayWith.IP_1_1_1_1);
        x509 = (X509Certificate) cf.generateCertificate(in);
        impl.verify("1.1.1.1", x509);

        exceptionPlease(impl, "1.1.1.2", x509);
        exceptionPlease(impl, "dummy-value.com", x509);

        in = new ByteArrayInputStream(CertificatesToPlayWith.EMAIL_ALT_SUBJECT_NAME);
        x509 = (X509Certificate) cf.generateCertificate(in);
        impl.verify("www.company.com", x509);
    }

    @Test
    public void testSubjectAlt() throws Exception {
        final CertificateFactory cf = CertificateFactory.getInstance("X.509");
        final InputStream in = new ByteArrayInputStream(CertificatesToPlayWith.X509_MULTIPLE_SUBJECT_ALT);
        final X509Certificate x509 = (X509Certificate) cf.generateCertificate(in);

        Assert.assertEquals("CN=localhost, OU=Unknown, O=Unknown, L=Unknown, ST=Unknown, C=CH",
                x509.getSubjectDN().getName());

        impl.verify("localhost.localdomain", x509);
        impl.verify("127.0.0.1", x509);

        try {
            impl.verify("localhost", x509);
            Assert.fail("SSLException should have been thrown");
        } catch (final SSLException ex) {
            // expected
        }
        try {
            impl.verify("local.host", x509);
            Assert.fail("SSLException should have been thrown");
        } catch (final SSLException ex) {
            // expected
        }
        try {
            impl.verify("127.0.0.2", x509);
            Assert.fail("SSLException should have been thrown");
        } catch (final SSLException ex) {
            // expected
        }
    }

    public void exceptionPlease(final DefaultHostnameVerifier hv, final String host,
                                final X509Certificate x509) {
        try {
            hv.verify(host, x509);
            Assert.fail("HostnameVerifier shouldn't allow [" + host + "]");
        }
        catch(final SSLException e) {
            // whew!  we're okay!
        }
    }

    @Test
    public void testDomainRootMatching() {

        Assert.assertFalse(DefaultHostnameVerifier.matchDomainRoot("a.b.c", null));
        Assert.assertTrue(DefaultHostnameVerifier.matchDomainRoot("a.b.c", "a.b.c"));
        Assert.assertFalse(DefaultHostnameVerifier.matchDomainRoot("aa.b.c", "a.b.c"));
        Assert.assertFalse(DefaultHostnameVerifier.matchDomainRoot("a.b.c", "aa.b.c"));
        Assert.assertTrue(DefaultHostnameVerifier.matchDomainRoot("a.a.b.c", "a.b.c"));
    }

    @Test
    public void testIdentityMatching() {

        Assert.assertTrue(DefaultHostnameVerifier.matchIdentity("a.b.c", "*.b.c"));
        Assert.assertTrue(DefaultHostnameVerifier.matchIdentityStrict("a.b.c", "*.b.c"));

        Assert.assertTrue(DefaultHostnameVerifier.matchIdentity("s.a.b.c", "*.b.c"));
        Assert.assertFalse(DefaultHostnameVerifier.matchIdentityStrict("s.a.b.c", "*.b.c")); // subdomain not OK

        Assert.assertFalse(DefaultHostnameVerifier.matchIdentity("a.gov.uk", "*.gov.uk", publicSuffixMatcher));
        Assert.assertFalse(DefaultHostnameVerifier.matchIdentityStrict("a.gov.uk", "*.gov.uk", publicSuffixMatcher));  // Bad 2TLD

        Assert.assertTrue(DefaultHostnameVerifier.matchIdentity("s.a.gov.uk", "*.a.gov.uk", publicSuffixMatcher));
        Assert.assertTrue(DefaultHostnameVerifier.matchIdentityStrict("s.a.gov.uk", "*.a.gov.uk", publicSuffixMatcher));

        Assert.assertFalse(DefaultHostnameVerifier.matchIdentity("s.a.gov.uk", "*.gov.uk", publicSuffixMatcher));
        Assert.assertFalse(DefaultHostnameVerifier.matchIdentityStrict("s.a.gov.uk", "*.gov.uk", publicSuffixMatcher));  // BBad 2TLD/no subdomain allowed

        Assert.assertTrue(DefaultHostnameVerifier.matchIdentity("a.gov.com", "*.gov.com", publicSuffixMatcher));
        Assert.assertTrue(DefaultHostnameVerifier.matchIdentityStrict("a.gov.com", "*.gov.com", publicSuffixMatcher));

        Assert.assertTrue(DefaultHostnameVerifier.matchIdentity("s.a.gov.com", "*.gov.com", publicSuffixMatcher));
        Assert.assertFalse(DefaultHostnameVerifier.matchIdentityStrict("s.a.gov.com", "*.gov.com", publicSuffixMatcher)); // no subdomain allowed

        Assert.assertFalse(DefaultHostnameVerifier.matchIdentity("a.gov.uk", "a*.gov.uk", publicSuffixMatcher));
        Assert.assertFalse(DefaultHostnameVerifier.matchIdentityStrict("a.gov.uk", "a*.gov.uk", publicSuffixMatcher)); // Bad 2TLD

        Assert.assertFalse(DefaultHostnameVerifier.matchIdentity("s.a.gov.uk", "a*.gov.uk", publicSuffixMatcher)); // Bad 2TLD
        Assert.assertFalse(DefaultHostnameVerifier.matchIdentityStrict("s.a.gov.uk", "a*.gov.uk", publicSuffixMatcher)); // Bad 2TLD/no subdomain allowed

        Assert.assertFalse(DefaultHostnameVerifier.matchIdentity("a.b.c", "*.b.*"));
        Assert.assertFalse(DefaultHostnameVerifier.matchIdentityStrict("a.b.c", "*.b.*"));

        Assert.assertFalse(DefaultHostnameVerifier.matchIdentity("a.b.c", "*.*.c"));
        Assert.assertFalse(DefaultHostnameVerifier.matchIdentityStrict("a.b.c", "*.*.c"));
    }

    @Test
    public void testHTTPCLIENT_1097() {
        Assert.assertTrue(DefaultHostnameVerifier.matchIdentity("a.b.c", "a*.b.c"));
        Assert.assertTrue(DefaultHostnameVerifier.matchIdentityStrict("a.b.c", "a*.b.c"));

        Assert.assertTrue(DefaultHostnameVerifier.matchIdentity("a.a.b.c", "a*.b.c"));
        Assert.assertFalse(DefaultHostnameVerifier.matchIdentityStrict("a.a.b.c", "a*.b.c"));
    }

    @Test
    public void testHTTPCLIENT_1255() {
        Assert.assertTrue(DefaultHostnameVerifier.matchIdentity("mail.a.b.c.com", "m*.a.b.c.com"));
        Assert.assertTrue(DefaultHostnameVerifier.matchIdentityStrict("mail.a.b.c.com", "m*.a.b.c.com"));
    }


    @Test
    public void testHTTPCLIENT_1997_ANY() { // Only True on all domains
        String domain;
        // Unknown
        domain = "dev.b.cloud.a";
        Assert.assertTrue(DefaultHostnameVerifier.matchIdentity(        "service.apps." + domain, "*.apps." + domain));
        Assert.assertTrue(DefaultHostnameVerifier.matchIdentityStrict(  "service.apps." + domain, "*.apps." + domain));
        Assert.assertTrue(DefaultHostnameVerifier.matchIdentity(        "service.apps." + domain, "*.apps." + domain, publicSuffixMatcher));
        Assert.assertTrue(DefaultHostnameVerifier.matchIdentityStrict(  "service.apps." + domain, "*.apps." + domain, publicSuffixMatcher));

        // ICANN
        domain = "dev.b.cloud.com";
        Assert.assertTrue(DefaultHostnameVerifier.matchIdentity(        "service.apps." + domain, "*.apps." + domain));
        Assert.assertTrue(DefaultHostnameVerifier.matchIdentityStrict(  "service.apps." + domain, "*.apps." + domain));
        Assert.assertTrue(DefaultHostnameVerifier.matchIdentity(        "service.apps." + domain, "*.apps." + domain, publicSuffixMatcher));
        Assert.assertTrue(DefaultHostnameVerifier.matchIdentityStrict(  "service.apps." + domain, "*.apps." + domain, publicSuffixMatcher));

        // PRIVATE
        domain = "dev.b.cloud.lan";
        Assert.assertTrue(DefaultHostnameVerifier.matchIdentity(        "service.apps." + domain, "*.apps." + domain));
        Assert.assertTrue(DefaultHostnameVerifier.matchIdentityStrict(  "service.apps." + domain, "*.apps." + domain));
        Assert.assertTrue(DefaultHostnameVerifier.matchIdentity(        "service.apps." + domain, "*.apps." + domain, publicSuffixMatcher));
        Assert.assertTrue(DefaultHostnameVerifier.matchIdentityStrict(  "service.apps." + domain, "*.apps." + domain, publicSuffixMatcher));
    }

    @Test
    public void testHTTPCLIENT_1997_ICANN() { // Only True on ICANN domains
        String domain;
        // Unknown
        domain = "dev.b.cloud.a";
        Assert.assertFalse(DefaultHostnameVerifier.matchIdentity(        "service.apps." + domain, "*.apps." + domain, publicSuffixMatcher, DomainType.ICANN));
        Assert.assertFalse(DefaultHostnameVerifier.matchIdentityStrict(  "service.apps." + domain, "*.apps." + domain, publicSuffixMatcher, DomainType.ICANN));

        // ICANN
        domain = "dev.b.cloud.com";
        Assert.assertTrue(DefaultHostnameVerifier.matchIdentity(        "service.apps." + domain, "*.apps." + domain, publicSuffixMatcher, DomainType.ICANN));
        Assert.assertTrue(DefaultHostnameVerifier.matchIdentityStrict(  "service.apps." + domain, "*.apps." + domain, publicSuffixMatcher, DomainType.ICANN));

        // PRIVATE
        domain = "dev.b.cloud.lan";
        Assert.assertFalse(DefaultHostnameVerifier.matchIdentity(        "service.apps." + domain, "*.apps." + domain, publicSuffixMatcher, DomainType.ICANN));
        Assert.assertFalse(DefaultHostnameVerifier.matchIdentityStrict(  "service.apps." + domain, "*.apps." + domain, publicSuffixMatcher, DomainType.ICANN));
    }

    @Test
    public void testHTTPCLIENT_1997_PRIVATE() { // Only True on PRIVATE domains
        String domain;
        // Unknown
        domain = "dev.b.cloud.a";
        Assert.assertFalse(DefaultHostnameVerifier.matchIdentity(        "service.apps." + domain, "*.apps." + domain, publicSuffixMatcher, DomainType.PRIVATE));
        Assert.assertFalse(DefaultHostnameVerifier.matchIdentityStrict(  "service.apps." + domain, "*.apps." + domain, publicSuffixMatcher, DomainType.PRIVATE));

        // ICANN
        domain = "dev.b.cloud.com";
        Assert.assertFalse(DefaultHostnameVerifier.matchIdentity(        "service.apps." + domain, "*.apps." + domain, publicSuffixMatcher, DomainType.PRIVATE));
        Assert.assertFalse(DefaultHostnameVerifier.matchIdentityStrict(  "service.apps." + domain, "*.apps." + domain, publicSuffixMatcher, DomainType.PRIVATE));

        // PRIVATE
        domain = "dev.b.cloud.lan";
        Assert.assertTrue(DefaultHostnameVerifier.matchIdentity(        "service.apps." + domain, "*.apps." + domain, publicSuffixMatcher, DomainType.PRIVATE));
        Assert.assertTrue(DefaultHostnameVerifier.matchIdentityStrict(  "service.apps." + domain, "*.apps." + domain, publicSuffixMatcher, DomainType.PRIVATE));
    }

    @Test
    public void testHTTPCLIENT_1997_UNKNOWN() { // Only True on all domains (same as ANY)
        String domain;
        // Unknown
        domain = "dev.b.cloud.a";
        Assert.assertTrue(DefaultHostnameVerifier.matchIdentity(        "service.apps." + domain, "*.apps." + domain, publicSuffixMatcher, DomainType.UNKNOWN));
        Assert.assertTrue(DefaultHostnameVerifier.matchIdentityStrict(  "service.apps." + domain, "*.apps." + domain, publicSuffixMatcher, DomainType.UNKNOWN));

        // ICANN
        domain = "dev.b.cloud.com";
        Assert.assertTrue(DefaultHostnameVerifier.matchIdentity(        "service.apps." + domain, "*.apps." + domain, publicSuffixMatcher, DomainType.UNKNOWN));
        Assert.assertTrue(DefaultHostnameVerifier.matchIdentityStrict(  "service.apps." + domain, "*.apps." + domain, publicSuffixMatcher, DomainType.UNKNOWN));

        // PRIVATE
        domain = "dev.b.cloud.lan";
        Assert.assertTrue(DefaultHostnameVerifier.matchIdentity(        "service.apps." + domain, "*.apps." + domain, publicSuffixMatcher, DomainType.UNKNOWN));
        Assert.assertTrue(DefaultHostnameVerifier.matchIdentityStrict(  "service.apps." + domain, "*.apps." + domain, publicSuffixMatcher, DomainType.UNKNOWN));
    }

    @Test // Check compressed IPv6 hostname matching
    public void testHTTPCLIENT_1316() throws Exception{
        final String host1 = "2001:0db8:aaaa:bbbb:cccc:0:0:0001";
        DefaultHostnameVerifier.matchIPv6Address(host1, Arrays.asList(SubjectName.IP("2001:0db8:aaaa:bbbb:cccc:0:0:0001")));
        DefaultHostnameVerifier.matchIPv6Address(host1, Arrays.asList(SubjectName.IP("2001:0db8:aaaa:bbbb:cccc::1")));
        try {
            DefaultHostnameVerifier.matchIPv6Address(host1, Arrays.asList(SubjectName.IP("2001:0db8:aaaa:bbbb:cccc::10")));
            Assert.fail("SSLException expected");
        } catch (final SSLException expected) {
        }
        final String host2 = "2001:0db8:aaaa:bbbb:cccc::1";
        DefaultHostnameVerifier.matchIPv6Address(host2, Arrays.asList(SubjectName.IP("2001:0db8:aaaa:bbbb:cccc:0:0:0001")));
        DefaultHostnameVerifier.matchIPv6Address(host2, Arrays.asList(SubjectName.IP("2001:0db8:aaaa:bbbb:cccc::1")));
        try {
            DefaultHostnameVerifier.matchIPv6Address(host2, Arrays.asList(SubjectName.IP("2001:0db8:aaaa:bbbb:cccc::10")));
            Assert.fail("SSLException expected");
        } catch (final SSLException expected) {
        }
    }

    @Test
    public void testExtractCN() throws Exception {
        Assert.assertEquals("blah", DefaultHostnameVerifier.extractCN("cn=blah, ou=blah, o=blah"));
        Assert.assertEquals("blah", DefaultHostnameVerifier.extractCN("cn=blah, cn=yada, cn=booh"));
        Assert.assertEquals("blah", DefaultHostnameVerifier.extractCN("c = pampa ,  cn  =    blah    , ou = blah , o = blah"));
        Assert.assertEquals("blah", DefaultHostnameVerifier.extractCN("cn=\"blah\", ou=blah, o=blah"));
        Assert.assertEquals("blah  blah", DefaultHostnameVerifier.extractCN("cn=\"blah  blah\", ou=blah, o=blah"));
        Assert.assertEquals("blah, blah", DefaultHostnameVerifier.extractCN("cn=\"blah, blah\", ou=blah, o=blah"));
        Assert.assertEquals("blah, blah", DefaultHostnameVerifier.extractCN("cn=blah\\, blah, ou=blah, o=blah"));
        Assert.assertEquals("blah", DefaultHostnameVerifier.extractCN("c = cn=uuh, cn=blah, ou=blah, o=blah"));
        try {
            DefaultHostnameVerifier.extractCN("blah,blah");
            Assert.fail("SSLException expected");
        } catch (final SSLException expected) {
        }
        try {
            DefaultHostnameVerifier.extractCN("cn,o=blah");
            Assert.fail("SSLException expected");
        } catch (final SSLException expected) {
        }
    }

    @Test
    public void testMatchDNSName() throws Exception {
        DefaultHostnameVerifier.matchDNSName(
                "host.domain.com",
                Collections.singletonList(SubjectName.DNS("*.domain.com")),
                publicSuffixMatcher);
        DefaultHostnameVerifier.matchDNSName(
                "host.xx",
                Collections.singletonList(SubjectName.DNS("*.xx")),
                publicSuffixMatcher);
        DefaultHostnameVerifier.matchDNSName(
                "host.appspot.com",
                Collections.singletonList(SubjectName.DNS("*.appspot.com")),
                publicSuffixMatcher);
        DefaultHostnameVerifier.matchDNSName(
                "demo-s3-bucket.s3.eu-central-1.amazonaws.com",
                Collections.singletonList(SubjectName.DNS("*.s3.eu-central-1.amazonaws.com")),
                publicSuffixMatcher);
        DefaultHostnameVerifier.matchDNSName(
                "hostname-workspace-1.local",
                Collections.singletonList(SubjectName.DNS("hostname-workspace-1.local")),
                publicSuffixMatcher);
    }

}
