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

package org.apache.http.conn.routing;

import java.net.InetAddress;

import org.apache.http.HttpHost;
import org.apache.http.conn.routing.RouteInfo.LayerType;
import org.apache.http.conn.routing.RouteInfo.TunnelType;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for {@link BasicRouteDirector}.
 */
public class TestRouteDirector {

    // a selection of constants for generating routes
    public final static
        HttpHost TARGET1 = new HttpHost("target1.test.invalid", 80);
    public final static
        HttpHost TARGET2 = new HttpHost("target2.test.invalid", 8080);
    // It is not necessary to have extra targets for https.
    // The 'layered' and 'secure' flags are specified explicitly
    // for routes, they will not be determined from the scheme.

    public final static
        HttpHost PROXY1 = new HttpHost("proxy1.test.invalid", 80);
    public final static
        HttpHost PROXY2 = new HttpHost("proxy2.test.invalid", 1080);
    public final static
        HttpHost PROXY3 = new HttpHost("proxy3.test.invalid", 88);

    public final static InetAddress LOCAL41;
    public final static InetAddress LOCAL42;
    public final static InetAddress LOCAL61;
    public final static InetAddress LOCAL62;

    // need static initializer to deal with exceptions
    static {
        try {
            LOCAL41 = InetAddress.getByAddress(new byte[]{ 127, 0, 0, 1 });
            LOCAL42 = InetAddress.getByAddress(new byte[]{ 127, 0, 0, 2 });

            LOCAL61 = InetAddress.getByAddress(new byte[]{
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1
            });
            LOCAL62 = InetAddress.getByAddress(new byte[]{
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2
            });

        } catch (final Exception x) {
            throw new ExceptionInInitializerError(x);
        }
    }

    @Test(expected=IllegalArgumentException.class)
    public void testIllegal() {
        final HttpRouteDirector rowdy = BasicRouteDirector.INSTANCE;
        final HttpRoute route = new HttpRoute(TARGET1);
        rowdy.nextStep(null, route);
    }

    @Test
    public void testDirect() {

        final HttpRouteDirector rowdy = BasicRouteDirector.INSTANCE;
        final HttpRoute route1   = new HttpRoute(TARGET1);
        final HttpRoute route2   = new HttpRoute(TARGET2);
        final HttpRoute route1p1 = new HttpRoute(TARGET1, null, PROXY1, false);

        int step = rowdy.nextStep(route1, null);
        Assert.assertEquals("wrong step to route1",
                     HttpRouteDirector.CONNECT_TARGET, step);

        step = rowdy.nextStep(route2, null);
        Assert.assertEquals("wrong step to route2",
                     HttpRouteDirector.CONNECT_TARGET, step);

        step = rowdy.nextStep(route1, route1);
        Assert.assertEquals("complete route1 not detected",
                     HttpRouteDirector.COMPLETE, step);

        step = rowdy.nextStep(route2, route2);
        Assert.assertEquals("complete route2 not detected",
                     HttpRouteDirector.COMPLETE, step);

        step = rowdy.nextStep(route1, route2);
        Assert.assertEquals("unreachable target not detected",
                     HttpRouteDirector.UNREACHABLE, step);

        step = rowdy.nextStep(route1, route1p1);
        Assert.assertEquals("invalid proxy not detected",
                     HttpRouteDirector.UNREACHABLE, step);
    }

    @Test
    public void testProxy() {

        final HttpRouteDirector rowdy = BasicRouteDirector.INSTANCE;
        final HttpRoute route1p1 = new HttpRoute(TARGET1, null, PROXY1, false);
        final HttpRoute route1p2 = new HttpRoute(TARGET1, null, PROXY2, false);
        final HttpRoute route2p1 = new HttpRoute(TARGET2, null, PROXY1, false);
        final HttpRoute route0   = new HttpRoute(PROXY1);
        final HttpRoute route1   = new HttpRoute(TARGET1);

        int step = rowdy.nextStep(route1p1, null);
        Assert.assertEquals("wrong step to route1p1",
                     HttpRouteDirector.CONNECT_PROXY, step);

        step = rowdy.nextStep(route1p2, null);
        Assert.assertEquals("wrong step to route1p2",
                     HttpRouteDirector.CONNECT_PROXY, step);

        step = rowdy.nextStep(route1p1, route1p1);
        Assert.assertEquals("complete route1p1 not detected",
                     HttpRouteDirector.COMPLETE, step);

        step = rowdy.nextStep(route1p2, route1p2);
        Assert.assertEquals("complete route1p2 not detected",
                     HttpRouteDirector.COMPLETE, step);

        step = rowdy.nextStep(route2p1, route2p1);
        Assert.assertEquals("complete route2p1 not detected",
                     HttpRouteDirector.COMPLETE, step);

        step = rowdy.nextStep(route1p1, route1p2);
        Assert.assertEquals("unreachable route1p1 via route1p2 not detected",
                     HttpRouteDirector.UNREACHABLE, step);

        step = rowdy.nextStep(route1p1, route2p1);
        Assert.assertEquals("unreachable route1p1 via route2p1 not detected",
                     HttpRouteDirector.UNREACHABLE, step);

        step = rowdy.nextStep(route1p1, route0);
        Assert.assertEquals("unreachable route1p1 via route0 not detected",
                     HttpRouteDirector.UNREACHABLE, step);

        step = rowdy.nextStep(route1p1, route1);
        Assert.assertEquals("unreachable route1p1 via route1 not detected",
                     HttpRouteDirector.UNREACHABLE, step);
    }

    @Test
    public void testProxyChain() {
        final HttpHost[] chainA = { PROXY1 };
        final HttpHost[] chainB = { PROXY1, PROXY2 };
        final HttpHost[] chainC = { PROXY2, PROXY1 };

        final HttpRouteDirector rowdy = BasicRouteDirector.INSTANCE;
        final HttpRoute route1cA  = new HttpRoute(TARGET1, null, chainA, false,
                                            TunnelType.PLAIN, LayerType.PLAIN);
        final HttpRoute route1cB  = new HttpRoute(TARGET1, null, chainB, false,
                                            TunnelType.PLAIN, LayerType.PLAIN);
        final HttpRoute route1cC  = new HttpRoute(TARGET1, null, chainC, false,
                                            TunnelType.PLAIN, LayerType.PLAIN);
        final HttpRoute route1cD  = new HttpRoute(TARGET1, null, chainC, false,
                                            TunnelType.PLAIN, LayerType.PLAIN);

        int step = rowdy.nextStep(route1cA, null);
        Assert.assertEquals("wrong step to route1cA",
                     HttpRouteDirector.CONNECT_PROXY, step);

        step = rowdy.nextStep(route1cB, null);
        Assert.assertEquals("wrong step to route1cB",
                     HttpRouteDirector.CONNECT_PROXY, step);

        step = rowdy.nextStep(route1cC, null);
        Assert.assertEquals("wrong step to route1cC",
                     HttpRouteDirector.CONNECT_PROXY, step);

        step = rowdy.nextStep(route1cD, null);
        Assert.assertEquals("wrong step to route1cD",
                     HttpRouteDirector.CONNECT_PROXY, step);


        step = rowdy.nextStep(route1cB, route1cA);
        Assert.assertEquals("wrong step to route 1cB from 1cA",
                     HttpRouteDirector.TUNNEL_PROXY, step);

        step = rowdy.nextStep(route1cB, route1cB);
        Assert.assertEquals("complete route 1cB not detected",
                     HttpRouteDirector.COMPLETE, step);

        step = rowdy.nextStep(route1cB, route1cC);
        Assert.assertEquals("unreachable route 1cB from 1cC not detected",
                     HttpRouteDirector.UNREACHABLE, step);

        step = rowdy.nextStep(route1cB, route1cD);
        Assert.assertEquals("unreachable route 1cB from 1cD not detected",
                     HttpRouteDirector.UNREACHABLE, step);


        step = rowdy.nextStep(route1cA, route1cB);
        Assert.assertEquals("unreachable route 1cA from 1cB not detected",
                     HttpRouteDirector.UNREACHABLE, step);
    }

    @Test
    public void testLocalDirect() {

        final HttpRouteDirector rowdy = BasicRouteDirector.INSTANCE;
        final HttpRoute route1l41 = new HttpRoute(TARGET1, LOCAL41, false);
        final HttpRoute route1l42 = new HttpRoute(TARGET1, LOCAL42, false);
        final HttpRoute route1l61 = new HttpRoute(TARGET1, LOCAL61, false);
        final HttpRoute route1l00 = new HttpRoute(TARGET1, null, false);

        int step = rowdy.nextStep(route1l41, null);
        Assert.assertEquals("wrong step to route1l41",
                     HttpRouteDirector.CONNECT_TARGET, step);

        step = rowdy.nextStep(route1l42, null);
        Assert.assertEquals("wrong step to route1l42",
                     HttpRouteDirector.CONNECT_TARGET, step);

        step = rowdy.nextStep(route1l61, null);
        Assert.assertEquals("wrong step to route1l61",
                     HttpRouteDirector.CONNECT_TARGET, step);

        step = rowdy.nextStep(route1l00, null);
        Assert.assertEquals("wrong step to route1l00",
                     HttpRouteDirector.CONNECT_TARGET, step);

        step = rowdy.nextStep(route1l41, route1l41);
        Assert.assertEquals("complete route1l41 not detected",
                     HttpRouteDirector.COMPLETE, step);

        step = rowdy.nextStep(route1l42, route1l42);
        Assert.assertEquals("complete route1l42 not detected",
                     HttpRouteDirector.COMPLETE, step);

        step = rowdy.nextStep(route1l61, route1l61);
        Assert.assertEquals("complete route1l61 not detected",
                     HttpRouteDirector.COMPLETE, step);

        step = rowdy.nextStep(route1l00, route1l00);
        Assert.assertEquals("complete route1l00 not detected",
                     HttpRouteDirector.COMPLETE, step);


        step = rowdy.nextStep(route1l41, route1l42);
        Assert.assertEquals("unreachable route1l41 via route1l42 not detected",
                     HttpRouteDirector.UNREACHABLE, step);

        step = rowdy.nextStep(route1l41, route1l61);
        Assert.assertEquals("unreachable route1l41 via route1l61 not detected",
                     HttpRouteDirector.UNREACHABLE, step);

        step = rowdy.nextStep(route1l41, route1l00);
        Assert.assertEquals("unreachable route1l41 via route1l00 not detected",
                     HttpRouteDirector.UNREACHABLE, step);


        step = rowdy.nextStep(route1l00, route1l41);
        Assert.assertEquals("complete route1l00 as route1l41 not detected",
                     HttpRouteDirector.COMPLETE, step);

        step = rowdy.nextStep(route1l00, route1l42);
        Assert.assertEquals("complete route1l00 as route1l42 not detected",
                     HttpRouteDirector.COMPLETE, step);

        step = rowdy.nextStep(route1l00, route1l61);
        Assert.assertEquals("complete route1l00 as route1l61 not detected",
                     HttpRouteDirector.COMPLETE, step);
    }

    @Test
    public void testDirectSecure() {

        final HttpRouteDirector rowdy = BasicRouteDirector.INSTANCE;
        final HttpRoute route1u   = new HttpRoute(TARGET1, null, false);
        final HttpRoute route1s   = new HttpRoute(TARGET1, null, true);
        final HttpRoute route1p1u = new HttpRoute(TARGET1, null, PROXY1, false);
        final HttpRoute route1p1s = new HttpRoute(TARGET1, null, PROXY1, true);

        int step = rowdy.nextStep(route1u, null);
        Assert.assertEquals("wrong step to route1u",
                     HttpRouteDirector.CONNECT_TARGET, step);

        step = rowdy.nextStep(route1s, null);
        Assert.assertEquals("wrong step to route1s",
                     HttpRouteDirector.CONNECT_TARGET, step);

        // unrequested security is currently not tolerated
        step = rowdy.nextStep(route1u, route1s);
        Assert.assertEquals("unreachable route 1u from 1s not detected",
                     HttpRouteDirector.UNREACHABLE, step);

        // secure layering of direct connections is currently not supported
        step = rowdy.nextStep(route1s, route1u);
        Assert.assertEquals("unreachable route 1s from 1u not detected",
                     HttpRouteDirector.UNREACHABLE, step);



        step = rowdy.nextStep(route1s, route1p1u);
        Assert.assertEquals("unreachable route 1s from 1p1u not detected",
                     HttpRouteDirector.UNREACHABLE, step);

        step = rowdy.nextStep(route1s, route1p1s);
        Assert.assertEquals("unreachable route 1s from 1p1s not detected",
                     HttpRouteDirector.UNREACHABLE, step);
    }

    @Test
    public void testProxyTLS() {

        final HttpRouteDirector rowdy = BasicRouteDirector.INSTANCE;
        final HttpRoute route1    = new HttpRoute
            (TARGET1, null, PROXY1, false,
             TunnelType.PLAIN, LayerType.PLAIN);
        final HttpRoute route1t   = new HttpRoute
            (TARGET1, null, PROXY1, false,
             TunnelType.TUNNELLED, LayerType.PLAIN);
        final HttpRoute route1tl  = new HttpRoute
            (TARGET1, null, PROXY1, false,
             TunnelType.TUNNELLED, LayerType.LAYERED);
        final HttpRoute route1s   = new HttpRoute
            (TARGET1, null, PROXY1, true,
             TunnelType.PLAIN, LayerType.PLAIN);
        final HttpRoute route1ts  = new HttpRoute
            (TARGET1, null, PROXY1, true,
             TunnelType.TUNNELLED, LayerType.PLAIN);
        final HttpRoute route1tls = new HttpRoute
            (TARGET1, null, PROXY1, true,
             TunnelType.TUNNELLED, LayerType.LAYERED);

        // we don't consider a route that is layered but not tunnelled

        int step = rowdy.nextStep(route1, null);
        Assert.assertEquals("wrong step to route1",
                     HttpRouteDirector.CONNECT_PROXY, step);

        step = rowdy.nextStep(route1t, null);
        Assert.assertEquals("wrong step to route1t",
                     HttpRouteDirector.CONNECT_PROXY, step);

        step = rowdy.nextStep(route1tl, null);
        Assert.assertEquals("wrong step to route1tl",
                     HttpRouteDirector.CONNECT_PROXY, step);

        step = rowdy.nextStep(route1s, null);
        Assert.assertEquals("wrong step to route1s",
                     HttpRouteDirector.CONNECT_PROXY, step);

        step = rowdy.nextStep(route1ts, null);
        Assert.assertEquals("wrong step to route1ts",
                     HttpRouteDirector.CONNECT_PROXY, step);

        step = rowdy.nextStep(route1tls, null);
        Assert.assertEquals("wrong step to route1tls",
                     HttpRouteDirector.CONNECT_PROXY, step);


        step = rowdy.nextStep(route1, route1);
        Assert.assertEquals("complete route1 not detected",
                     HttpRouteDirector.COMPLETE, step);

        step = rowdy.nextStep(route1t, route1t);
        Assert.assertEquals("complete route1t not detected",
                     HttpRouteDirector.COMPLETE, step);

        step = rowdy.nextStep(route1tl, route1tl);
        Assert.assertEquals("complete route1tl not detected",
                     HttpRouteDirector.COMPLETE, step);

        step = rowdy.nextStep(route1s, route1s);
        Assert.assertEquals("complete route1s not detected",
                     HttpRouteDirector.COMPLETE, step);

        step = rowdy.nextStep(route1ts, route1ts);
        Assert.assertEquals("complete route1ts not detected",
                     HttpRouteDirector.COMPLETE, step);

        step = rowdy.nextStep(route1tls, route1tls);
        Assert.assertEquals("complete route1tls not detected",
                     HttpRouteDirector.COMPLETE, step);



        step = rowdy.nextStep(route1, route1t);
        Assert.assertEquals("unreachable route1 from 1t not detected",
                     HttpRouteDirector.UNREACHABLE, step);

        step = rowdy.nextStep(route1, route1tl);
        Assert.assertEquals("unreachable route1 from 1tl not detected",
                     HttpRouteDirector.UNREACHABLE, step);

        // unrequested security is currently not tolerated
        step = rowdy.nextStep(route1, route1s);
        Assert.assertEquals("unreachable route1 from 1s not detected",
                     HttpRouteDirector.UNREACHABLE, step);

        step = rowdy.nextStep(route1, route1ts);
        Assert.assertEquals("unreachable route1 from 1ts not detected",
                     HttpRouteDirector.UNREACHABLE, step);

        step = rowdy.nextStep(route1, route1tls);
        Assert.assertEquals("unreachable route1 from 1tls not detected",
                     HttpRouteDirector.UNREACHABLE, step);


        // securing requires layering
        step = rowdy.nextStep(route1s, route1);
        Assert.assertEquals("unreachable route1s from 1 not detected",
                     HttpRouteDirector.UNREACHABLE, step);

        // securing requires layering, and multiple layers are not supported
        step = rowdy.nextStep(route1tls, route1tl);
        Assert.assertEquals("unreachable route1tls from 1tl not detected",
                     HttpRouteDirector.UNREACHABLE, step);


        // cases where tunnelling to the target is required
        step = rowdy.nextStep(route1t, route1);
        Assert.assertEquals("wrong step to route1t from 1",
                     HttpRouteDirector.TUNNEL_TARGET, step);

        step = rowdy.nextStep(route1tl, route1);
        Assert.assertEquals("wrong step to route1tl from 1",
                     HttpRouteDirector.TUNNEL_TARGET, step);

        step = rowdy.nextStep(route1tls, route1);
        Assert.assertEquals("wrong step to route1tls from 1",
                     HttpRouteDirector.TUNNEL_TARGET, step);


        // cases where layering on the tunnel is required
        step = rowdy.nextStep(route1tl, route1t);
        Assert.assertEquals("wrong step to route1tl from 1t",
                     HttpRouteDirector.LAYER_PROTOCOL, step);

        step = rowdy.nextStep(route1tl, route1ts);
        Assert.assertEquals("wrong step to route1tl from 1ts",
                     HttpRouteDirector.LAYER_PROTOCOL, step);

        step = rowdy.nextStep(route1tls, route1t);
        Assert.assertEquals("wrong step to route1tls from 1t",
                     HttpRouteDirector.LAYER_PROTOCOL, step);

        step = rowdy.nextStep(route1tls, route1ts);
        Assert.assertEquals("wrong step to route1tls from 1ts",
                     HttpRouteDirector.LAYER_PROTOCOL, step);

        // There are some odd cases left over, like having a secure tunnel
        // that becomes unsecure by layering, or a secure connection to a
        // proxy that becomes unsecure by tunnelling to another proxy.
    }

}
