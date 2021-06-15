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
import java.io.InputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;

import javax.net.ssl.SSLException;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit tests for deprecated {@link X509HostnameVerifier} implementations.
 *
 * @deprecated Tests deprecated code.
 */
@Deprecated
public class TestHostnameVerifier {

    @Test
    public void testVerify() throws Exception {
        final X509HostnameVerifier DEFAULT = new BrowserCompatHostnameVerifier();
        final X509HostnameVerifier STRICT = new StrictHostnameVerifier();
        final X509HostnameVerifier ALLOW_ALL = new AllowAllHostnameVerifier();
        final CertificateFactory cf = CertificateFactory.getInstance("X.509");
        InputStream in;
        X509Certificate x509;
        in = new ByteArrayInputStream(CertificatesToPlayWith.X509_FOO);
        x509 = (X509Certificate) cf.generateCertificate(in);

        DEFAULT.verify("foo.com", x509);
        STRICT.verify("foo.com", x509);
        exceptionPlease(DEFAULT, "a.foo.com", x509);
        exceptionPlease(STRICT, "a.foo.com", x509);
        exceptionPlease(DEFAULT, "bar.com", x509);
        exceptionPlease(STRICT, "bar.com", x509);
        ALLOW_ALL.verify("foo.com", x509);
        ALLOW_ALL.verify("a.foo.com", x509);
        ALLOW_ALL.verify("bar.com", x509);

        in = new ByteArrayInputStream(CertificatesToPlayWith.X509_HANAKO);
        x509 = (X509Certificate) cf.generateCertificate(in);
        DEFAULT.verify("\u82b1\u5b50.co.jp", x509);
        STRICT.verify("\u82b1\u5b50.co.jp", x509);
        exceptionPlease(DEFAULT, "a.\u82b1\u5b50.co.jp", x509);
        exceptionPlease(STRICT, "a.\u82b1\u5b50.co.jp", x509);

        in = new ByteArrayInputStream(CertificatesToPlayWith.X509_FOO_BAR);
        x509 = (X509Certificate) cf.generateCertificate(in);
        exceptionPlease(DEFAULT, "foo.com", x509);
        exceptionPlease(STRICT, "foo.com", x509);
        exceptionPlease(DEFAULT, "a.foo.com", x509);
        exceptionPlease(STRICT, "a.foo.com", x509);
        DEFAULT.verify("bar.com", x509);
        STRICT.verify("bar.com", x509);
        exceptionPlease(DEFAULT, "a.bar.com", x509);
        exceptionPlease(STRICT, "a.bar.com", x509);

        in = new ByteArrayInputStream(CertificatesToPlayWith.X509_FOO_BAR_HANAKO);
        x509 = (X509Certificate) cf.generateCertificate(in);
        exceptionPlease(DEFAULT, "foo.com", x509);
        exceptionPlease(STRICT, "foo.com", x509);
        exceptionPlease(DEFAULT, "a.foo.com", x509);
        exceptionPlease(STRICT, "a.foo.com", x509);
        DEFAULT.verify("bar.com", x509);
        STRICT.verify("bar.com", x509);
        exceptionPlease(DEFAULT, "a.bar.com", x509);
        exceptionPlease(STRICT, "a.bar.com", x509);

        /*
           Java isn't extracting international subjectAlts properly.  (Or
           OpenSSL isn't storing them properly).
        */
        // INSTANCE.verify("\u82b1\u5b50.co.jp", x509 );
        // STRICT.verify("\u82b1\u5b50.co.jp", x509 );
        exceptionPlease(DEFAULT, "a.\u82b1\u5b50.co.jp", x509);
        exceptionPlease(STRICT, "a.\u82b1\u5b50.co.jp", x509);

        in = new ByteArrayInputStream(CertificatesToPlayWith.X509_NO_CNS_FOO);
        x509 = (X509Certificate) cf.generateCertificate(in);
        DEFAULT.verify("foo.com", x509);
        STRICT.verify("foo.com", x509);
        exceptionPlease(DEFAULT, "a.foo.com", x509);
        exceptionPlease(STRICT, "a.foo.com", x509);

        in = new ByteArrayInputStream(CertificatesToPlayWith.X509_NO_CNS_FOO);
        x509 = (X509Certificate) cf.generateCertificate(in);
        DEFAULT.verify("foo.com", x509);
        STRICT.verify("foo.com", x509);
        exceptionPlease(DEFAULT, "a.foo.com", x509);
        exceptionPlease(STRICT, "a.foo.com", x509);

        in = new ByteArrayInputStream(CertificatesToPlayWith.X509_THREE_CNS_FOO_BAR_HANAKO);
        x509 = (X509Certificate) cf.generateCertificate(in);
        exceptionPlease(DEFAULT, "foo.com", x509);
        exceptionPlease(STRICT, "foo.com", x509);
        exceptionPlease(DEFAULT, "a.foo.com", x509);
        exceptionPlease(STRICT, "a.foo.com", x509);
        exceptionPlease(DEFAULT, "bar.com", x509);
        exceptionPlease(STRICT, "bar.com", x509);
        exceptionPlease(DEFAULT, "a.bar.com", x509);
        exceptionPlease(STRICT, "a.bar.com", x509);
        DEFAULT.verify("\u82b1\u5b50.co.jp", x509);
        STRICT.verify("\u82b1\u5b50.co.jp", x509);
        exceptionPlease(DEFAULT, "a.\u82b1\u5b50.co.jp", x509);
        exceptionPlease(STRICT, "a.\u82b1\u5b50.co.jp", x509);

        in = new ByteArrayInputStream(CertificatesToPlayWith.X509_WILD_FOO);
        x509 = (X509Certificate) cf.generateCertificate(in);
        exceptionPlease(DEFAULT, "foo.com", x509);
        exceptionPlease(STRICT, "foo.com", x509);
        DEFAULT.verify("www.foo.com", x509);
        STRICT.verify("www.foo.com", x509);
        DEFAULT.verify("\u82b1\u5b50.foo.com", x509);
        STRICT.verify("\u82b1\u5b50.foo.com", x509);
        DEFAULT.verify("a.b.foo.com", x509);
        exceptionPlease(STRICT, "a.b.foo.com", x509);

        in = new ByteArrayInputStream(CertificatesToPlayWith.X509_WILD_CO_JP);
        x509 = (X509Certificate) cf.generateCertificate(in);
        // Silly test because no-one would ever be able to lookup an IP address
        // using "*.co.jp".
        DEFAULT.verify("*.co.jp", x509);
        STRICT.verify("*.co.jp", x509);
        DEFAULT.verify("foo.co.jp", x509);
        exceptionPlease(STRICT, "foo.co.jp", x509);
        DEFAULT.verify("\u82b1\u5b50.co.jp", x509);
        exceptionPlease(STRICT, "\u82b1\u5b50.co.jp", x509);

        in = new ByteArrayInputStream(CertificatesToPlayWith.X509_WILD_FOO_BAR_HANAKO);
        x509 = (X509Certificate) cf.generateCertificate(in);
        // try the foo.com variations
        exceptionPlease(DEFAULT, "foo.com", x509);
        exceptionPlease(STRICT, "foo.com", x509);
        exceptionPlease(DEFAULT, "www.foo.com", x509);
        exceptionPlease(STRICT, "www.foo.com", x509);
        exceptionPlease(DEFAULT, "\u82b1\u5b50.foo.com", x509);
        exceptionPlease(STRICT, "\u82b1\u5b50.foo.com", x509);
        exceptionPlease(DEFAULT, "a.b.foo.com", x509);
        exceptionPlease(STRICT, "a.b.foo.com", x509);
        // try the bar.com variations
        exceptionPlease(DEFAULT, "bar.com", x509);
        exceptionPlease(STRICT, "bar.com", x509);
        DEFAULT.verify("www.bar.com", x509);
        STRICT.verify("www.bar.com", x509);
        DEFAULT.verify("\u82b1\u5b50.bar.com", x509);
        STRICT.verify("\u82b1\u5b50.bar.com", x509);
        DEFAULT.verify("a.b.bar.com", x509);
        exceptionPlease(STRICT, "a.b.bar.com", x509);

        in = new ByteArrayInputStream(CertificatesToPlayWith.X509_MULTIPLE_VALUE_AVA);
        x509 = (X509Certificate) cf.generateCertificate(in);
        ALLOW_ALL.verify("repository.infonotary.com", x509);
        DEFAULT.verify("repository.infonotary.com", x509);
        STRICT.verify("repository.infonotary.com", x509);
    }

    @Test
    public void testSubjectAlt() throws Exception {
        final CertificateFactory cf = CertificateFactory.getInstance("X.509");
        final InputStream in = new ByteArrayInputStream(CertificatesToPlayWith.X509_MULTIPLE_SUBJECT_ALT);
        final X509Certificate x509 = (X509Certificate) cf.generateCertificate(in);

        final X509HostnameVerifier verifier = BrowserCompatHostnameVerifier.INSTANCE;

        Assert.assertEquals("CN=localhost, OU=Unknown, O=Unknown, L=Unknown, ST=Unknown, C=CH",
                x509.getSubjectDN().getName());

        verifier.verify("localhost.localdomain", x509);
        verifier.verify("127.0.0.1", x509);

        try {
            verifier.verify("localhost", x509);
            Assert.fail("SSLException should have been thrown");
        } catch (final SSLException ex) {
            // expected
        }
        try {
            verifier.verify("local.host", x509);
            Assert.fail("SSLException should have been thrown");
        } catch (final SSLException ex) {
            // expected
        }
        try {
            verifier.verify("127.0.0.2", x509);
            Assert.fail("SSLException should have been thrown");
        } catch (final SSLException ex) {
            // expected
        }

    }

    public void exceptionPlease(final X509HostnameVerifier hv, final String host,
                                 final X509Certificate x509) {
        try {
            hv.verify(host, x509);
            Assert.fail("HostnameVerifier shouldn't allow [" + host + "]");
        }
        catch(final SSLException e) {
            // whew!  we're okay!
        }
    }

    // Test helper method
    private void checkMatching(final X509HostnameVerifier hv, final String host,
            final String[] cns, final String[] alts, final boolean shouldFail) {
        try {
            hv.verify(host, cns, alts);
            if (shouldFail) {
                Assert.fail("HostnameVerifier should not allow [" + host + "] to match "
                        +Arrays.toString(cns)
                        +" or "
                        +Arrays.toString(alts));
            }
        }
        catch(final SSLException e) {
            if (!shouldFail) {
                Assert.fail("HostnameVerifier should have allowed [" + host + "] to match "
                        +Arrays.toString(cns)
                        +" or "
                        +Arrays.toString(alts));
            }
        }
    }

    @Test
    // Check standard wildcard matching
    public void testMatching() {
        String cns[] = {};
        String alt[] = {};
        final X509HostnameVerifier bhv = new BrowserCompatHostnameVerifier();
        final X509HostnameVerifier shv = new StrictHostnameVerifier();
        checkMatching(bhv, "a.b.c", cns, alt, true); // empty
        checkMatching(shv, "a.b.c", cns, alt, true); // empty

        cns = new String []{"*.b.c"};
        checkMatching(bhv, "a.b.c", cns, alt, false); // OK
        checkMatching(shv, "a.b.c", cns, alt, false); // OK

        checkMatching(bhv, "s.a.b.c", cns, alt, false); // OK
        checkMatching(shv, "s.a.b.c", cns, alt, true); // subdomain not OK

        cns = new String []{};
        alt = new String []{"dummy", "*.b.c"}; // check matches against all alts
        checkMatching(bhv, "a.b.c", cns, alt, false); // OK
        checkMatching(shv, "a.b.c", cns, alt, false); // OK

        checkMatching(bhv, "s.a.b.c", cns, alt, false); // OK
        checkMatching(shv, "s.a.b.c", cns, alt, true); // subdomain not OK

        alt = new String []{"*.gov.uk"};
        checkMatching(bhv, "a.gov.uk", cns, alt, false); // OK
        checkMatching(shv, "a.gov.uk", cns, alt, true); // Bad 2TLD

        checkMatching(bhv, "s.a.gov.uk", cns, alt, false); // OK
        checkMatching(shv, "s.a.gov.uk", cns, alt, true); // Bad 2TLD/no subdomain allowed
        alt = new String []{"*.gov.com"};
        checkMatching(bhv, "a.gov.com", cns, alt, false); // OK, gov not 2TLD here
        checkMatching(shv, "a.gov.com", cns, alt, false); // OK, gov not 2TLD here

        checkMatching(bhv, "s.a.gov.com", cns, alt, false); // OK, gov not 2TLD here
        checkMatching(shv, "s.a.gov.com", cns, alt, true); // no subdomain allowed

        alt = new String []{"a*.gov.uk"}; // 2TLD check applies to wildcards
        checkMatching(bhv, "a.gov.uk", cns, alt, false); // OK
        checkMatching(shv, "a.gov.uk", cns, alt, true); // Bad 2TLD

        checkMatching(bhv, "s.a.gov.uk", cns, alt, true); // Bad 2TLD
        checkMatching(shv, "s.a.gov.uk", cns, alt, true); // Bad 2TLD/no subdomain allowed

    }

    @Test
    // Check compressed IPv6 hostname matching
    public void testHTTPCLIENT_1316() throws Exception{
        final String cns[] = {"2001:0db8:aaaa:bbbb:cccc:0:0:0001"};
        final String alt[] = {};
        final X509HostnameVerifier bhv = new BrowserCompatHostnameVerifier();
        final X509HostnameVerifier shv = new StrictHostnameVerifier();
        checkMatching(bhv, "2001:0db8:aaaa:bbbb:cccc:0:0:0001", cns, alt, false);
        checkMatching(shv, "2001:0db8:aaaa:bbbb:cccc:0:0:0001", cns, alt, false);
        checkMatching(bhv, "2001:0db8:aaaa:bbbb:cccc::1", cns, alt, false);
        checkMatching(shv, "2001:0db8:aaaa:bbbb:cccc::1", cns, alt, false);
        checkMatching(bhv, "2001:0db8:aaaa:bbbb:cccc::10", cns, alt, true);
        checkMatching(shv, "2001:0db8:aaaa:bbbb:cccc::10", cns, alt, true);
        // TODO need some more samples
    }


    @Test
    public void testHTTPCLIENT_1097() {
        final String cns[];
        final String alt[] = {};
        final X509HostnameVerifier bhv = new BrowserCompatHostnameVerifier();
        final X509HostnameVerifier shv = new StrictHostnameVerifier();

        cns = new String []{"a*.b.c"}; // component part
        checkMatching(bhv, "a.b.c", cns, alt, false); // OK
        checkMatching(shv, "a.b.c", cns, alt, false); // OK

        checkMatching(bhv, "a.a.b.c", cns, alt, false); // OK
        checkMatching(shv, "a.a.b.c", cns, alt, true); // subdomain not OK
    }

    @Test
    public void testHTTPCLIENT_1255() {
        final X509HostnameVerifier bhv = new BrowserCompatHostnameVerifier();
        final X509HostnameVerifier shv = new StrictHostnameVerifier();

        final String cns[] = new String []{"m*.a.b.c.com"}; // component part
        final String alt[] = {};
        checkMatching(bhv, "mail.a.b.c.com", cns, alt, false); // OK
        checkMatching(shv, "mail.a.b.c.com", cns, alt, false); // OK
    }

}
