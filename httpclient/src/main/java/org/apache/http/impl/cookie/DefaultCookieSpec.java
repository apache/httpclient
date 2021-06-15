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

import java.util.List;

import org.apache.http.FormattedHeader;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.annotation.Contract;
import org.apache.http.annotation.ThreadingBehavior;
import org.apache.http.cookie.Cookie;
import org.apache.http.cookie.CookieOrigin;
import org.apache.http.cookie.CookieSpec;
import org.apache.http.cookie.MalformedCookieException;
import org.apache.http.cookie.SM;
import org.apache.http.cookie.SetCookie2;
import org.apache.http.message.ParserCursor;
import org.apache.http.util.Args;
import org.apache.http.util.CharArrayBuffer;

/**
 * Default cookie specification that picks up the best matching cookie policy based on
 * the format of cookies sent with the HTTP response.
 *
 * @since 4.4
 */
@Contract(threading = ThreadingBehavior.SAFE)
public class DefaultCookieSpec implements CookieSpec {

    private final RFC2965Spec strict;
    private final RFC2109Spec obsoleteStrict;
    private final NetscapeDraftSpec netscapeDraft;

    DefaultCookieSpec(
            final RFC2965Spec strict,
            final RFC2109Spec obsoleteStrict,
            final NetscapeDraftSpec netscapeDraft) {
        this.strict = strict;
        this.obsoleteStrict = obsoleteStrict;
        this.netscapeDraft = netscapeDraft;
    }

    public DefaultCookieSpec(
            final String[] datepatterns,
            final boolean oneHeader) {
        this.strict = new RFC2965Spec(oneHeader,
                RFC2965VersionAttributeHandler.INSTANCE,
                BasicPathHandler.INSTANCE,
                RFC2965DomainAttributeHandler.INSTANCE,
                RFC2965PortAttributeHandler.INSTANCE,
                BasicMaxAgeHandler.INSTANCE,
                BasicSecureHandler.INSTANCE,
                BasicCommentHandler.INSTANCE,
                RFC2965CommentUrlAttributeHandler.INSTANCE,
                RFC2965DiscardAttributeHandler.INSTANCE);
        this.obsoleteStrict = new RFC2109Spec(oneHeader,
                RFC2109VersionHandler.INSTANCE,
                BasicPathHandler.INSTANCE,
                RFC2109DomainHandler.INSTANCE,
                BasicMaxAgeHandler.INSTANCE,
                BasicSecureHandler.INSTANCE,
                new BasicCommentHandler());
        this.netscapeDraft = new NetscapeDraftSpec(
                BasicDomainHandler.INSTANCE,
                BasicPathHandler.INSTANCE,
                BasicSecureHandler.INSTANCE,
                new BasicCommentHandler(),
                new BasicExpiresHandler(
                        datepatterns != null ? datepatterns.clone() : new String[]{NetscapeDraftSpec.EXPIRES_PATTERN}));
    }

    public DefaultCookieSpec() {
        this(null, false);
    }

    @Override
    public List<Cookie> parse(
            final Header header,
            final CookieOrigin origin) throws MalformedCookieException {
        Args.notNull(header, "Header");
        Args.notNull(origin, "Cookie origin");
        HeaderElement[] hElems = header.getElements();
        boolean versioned = false;
        boolean netscape = false;
        for (final HeaderElement hElem: hElems) {
            if (hElem.getParameterByName("version") != null) {
                versioned = true;
            }
            if (hElem.getParameterByName("expires") != null) {
               netscape = true;
            }
        }
        if (netscape || !versioned) {
            // Need to parse the header again, because Netscape style cookies do not correctly
            // support multiple header elements (comma cannot be treated as an element separator)
            final NetscapeDraftHeaderParser parser = NetscapeDraftHeaderParser.DEFAULT;
            final CharArrayBuffer buffer;
            final ParserCursor cursor;
            if (header instanceof FormattedHeader) {
                buffer = ((FormattedHeader) header).getBuffer();
                cursor = new ParserCursor(
                        ((FormattedHeader) header).getValuePos(),
                        buffer.length());
            } else {
                final String hValue = header.getValue();
                if (hValue == null) {
                    throw new MalformedCookieException("Header value is null");
                }
                buffer = new CharArrayBuffer(hValue.length());
                buffer.append(hValue);
                cursor = new ParserCursor(0, buffer.length());
            }
            hElems = new HeaderElement[] { parser.parseHeader(buffer, cursor) };
            return netscapeDraft.parse(hElems, origin);
        }
        return SM.SET_COOKIE2.equals(header.getName())
                        ? strict.parse(hElems, origin)
                        : obsoleteStrict.parse(hElems, origin);
    }

    @Override
    public void validate(
            final Cookie cookie,
            final CookieOrigin origin) throws MalformedCookieException {
        Args.notNull(cookie, "Cookie");
        Args.notNull(origin, "Cookie origin");
        if (cookie.getVersion() > 0) {
            if (cookie instanceof SetCookie2) {
                strict.validate(cookie, origin);
            } else {
                obsoleteStrict.validate(cookie, origin);
            }
        } else {
            netscapeDraft.validate(cookie, origin);
        }
    }

    @Override
    public boolean match(final Cookie cookie, final CookieOrigin origin) {
        Args.notNull(cookie, "Cookie");
        Args.notNull(origin, "Cookie origin");
        if (cookie.getVersion() > 0) {
            return cookie instanceof SetCookie2
                            ? strict.match(cookie, origin)
                            : obsoleteStrict.match(cookie, origin);
        }
        return netscapeDraft.match(cookie, origin);
    }

    @Override
    public List<Header> formatCookies(final List<Cookie> cookies) {
        Args.notNull(cookies, "List of cookies");
        int version = Integer.MAX_VALUE;
        boolean isSetCookie2 = true;
        for (final Cookie cookie: cookies) {
            if (!(cookie instanceof SetCookie2)) {
                isSetCookie2 = false;
            }
            if (cookie.getVersion() < version) {
                version = cookie.getVersion();
            }
        }
        if (version > 0) {
            return isSetCookie2
                            ? strict.formatCookies(cookies)
                            : obsoleteStrict.formatCookies(cookies);
        }
        return netscapeDraft.formatCookies(cookies);
    }

    @Override
    public int getVersion() {
        return strict.getVersion();
    }

    @Override
    public Header getVersionHeader() {
        return null;
    }

    @Override
    public String toString() {
        return "default";
    }

}
