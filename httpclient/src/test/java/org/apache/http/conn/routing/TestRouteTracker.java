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
import java.util.HashSet;
import java.util.Set;

import org.apache.http.HttpHost;
import org.apache.http.conn.routing.RouteInfo.LayerType;
import org.apache.http.conn.routing.RouteInfo.TunnelType;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for {@link RouteTracker}.
 */
@SuppressWarnings("boxing") // test code
public class TestRouteTracker {

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

    @SuppressWarnings("unused")
    @Test
    public void testCstrTargetLocal() {

        RouteTracker rt = new RouteTracker(TARGET1, null);
        Assert.assertEquals("wrong target (target,null)",
                     TARGET1, rt.getTargetHost());
        Assert.assertEquals("wrong local address (target,null)",
                     null, rt.getLocalAddress());
        Assert.assertEquals("wrong hop count (target,null)",
                     0, rt.getHopCount());
        Assert.assertEquals("wrong proxy (target,null)",
                     null, rt.getProxyHost());
        Assert.assertEquals("wrong route (target,null)",
                     null, rt.toRoute());
        checkCTLS(rt, false, false, false, false);


        rt = new RouteTracker(TARGET2, LOCAL61);
        Assert.assertEquals("wrong target (target,local)",
                     TARGET2, rt.getTargetHost());
        Assert.assertEquals("wrong local address (target,local)",
                     LOCAL61, rt.getLocalAddress());
        Assert.assertEquals("wrong hop count (target,local)",
                     0, rt.getHopCount());
        Assert.assertEquals("wrong proxy (target,local)",
                     null, rt.getProxyHost());
        Assert.assertEquals("wrong route (target,local)",
                     null, rt.toRoute());
        checkCTLS(rt, false, false, false, false);


        rt = null;
        try {
            new RouteTracker(null, LOCAL41);
            Assert.fail("null target not detected");
        } catch (final IllegalArgumentException iax) {
            // expected
        }
    }

    @SuppressWarnings("unused")
    @Test
    public void testCstrRoute() {

        HttpRoute    r  = new HttpRoute(TARGET1);
        RouteTracker rt = new RouteTracker(r);
        Assert.assertEquals("wrong target (r1)",
                     TARGET1, rt.getTargetHost());
        Assert.assertEquals("wrong local address (r1)",
                     null, rt.getLocalAddress());
        Assert.assertEquals("wrong hop count (r1)",
                     0, rt.getHopCount());
        Assert.assertEquals("wrong proxy (r1)",
                     null, rt.getProxyHost());
        Assert.assertEquals("wrong route (r1)",
                     null, rt.toRoute());
        checkCTLS(rt, false, false, false, false);

        r  = new HttpRoute(TARGET2, LOCAL61, true);
        rt = new RouteTracker(r);
        Assert.assertEquals("wrong target (r2)",
                     TARGET2, rt.getTargetHost());
        Assert.assertEquals("wrong local address (r2)",
                     LOCAL61, rt.getLocalAddress());
        Assert.assertEquals("wrong hop count (r2)",
                     0, rt.getHopCount());
        Assert.assertEquals("wrong proxy (r2)",
                     null, rt.getProxyHost());
        Assert.assertEquals("wrong route (r2)",
                     null, rt.toRoute());
        checkCTLS(rt, false, false, false, false);


        r  = new HttpRoute(TARGET1, LOCAL42, PROXY3, true);
        rt = new RouteTracker(r);
        Assert.assertEquals("wrong target (r3)",
                     TARGET1, rt.getTargetHost());
        Assert.assertEquals("wrong local address (r3)",
                     LOCAL42, rt.getLocalAddress());
        Assert.assertEquals("wrong hop count (r3)",
                     0, rt.getHopCount());
        Assert.assertEquals("wrong proxy (r3)",
                     null, rt.getProxyHost());
        Assert.assertEquals("wrong route (r3)",
                     null, rt.toRoute());
        checkCTLS(rt, false, false, false, false);


        rt = null;
        try {
            new RouteTracker(null);
            Assert.fail("null route not detected");
        } catch (final NullPointerException npx) {
            // expected
        }
    }

    @Test
    public void testIllegalArgs() {

        final RouteTracker rt = new RouteTracker(TARGET2, null);

        try {
            rt.connectProxy(null, true);
            Assert.fail("missing proxy argument not detected (connect/false)");
        } catch (final IllegalArgumentException iax) {
            // expected
        }

        try {
            rt.connectProxy(null, false);
            Assert.fail("missing proxy argument not detected (connect/true)");
        } catch (final IllegalArgumentException iax) {
            // expected
        }

        rt.connectProxy(PROXY1, false);

        try {
            rt.tunnelProxy(null, false);
            Assert.fail("missing proxy argument not detected (tunnel/false)");
        } catch (final IllegalArgumentException iax) {
            // expected
        }

        try {
            rt.tunnelProxy(null, true);
            Assert.fail("missing proxy argument not detected (tunnel/true)");
        } catch (final IllegalArgumentException iax) {
            // expected
        }

        try {
            rt.getHopTarget(-1);
            Assert.fail("negative hop index not detected");
        } catch (final IllegalArgumentException iax) {
            // expected
        }

        try {
            rt.getHopTarget(2);
            Assert.fail("excessive hop index not detected");
        } catch (final IllegalArgumentException iax) {
            // expected
        }
    }

    @Test
    public void testIllegalStates() {

        final RouteTracker rt = new RouteTracker(TARGET1, null);

        try {
            rt.tunnelTarget(false);
            Assert.fail("unconnectedness not detected (tunnelTarget)");
        } catch (final IllegalStateException isx) {
            // expected
        }

        try {
            rt.tunnelProxy(PROXY1, false);
            Assert.fail("unconnectedness not detected (tunnelProxy)");
        } catch (final IllegalStateException isx) {
            // expected
        }

        try {
            rt.layerProtocol(true);
            Assert.fail("unconnectedness not detected (layerProtocol)");
        } catch (final IllegalStateException isx) {
            // expected
        }


        // connect directly
        rt.connectTarget(false);

        try {
            rt.connectTarget(false);
            Assert.fail("connectedness not detected (connectTarget)");
        } catch (final IllegalStateException isx) {
            // expected
        }

        try {
            rt.connectProxy(PROXY2, false);
            Assert.fail("connectedness not detected (connectProxy)");
        } catch (final IllegalStateException isx) {
            // expected
        }

        try {
            rt.tunnelTarget(false);
            Assert.fail("unproxiedness not detected (tunnelTarget)");
        } catch (final IllegalStateException isx) {
            // expected
        }

        try {
            rt.tunnelProxy(PROXY1, false);
            Assert.fail("unproxiedness not detected (tunnelProxy)");
        } catch (final IllegalStateException isx) {
            // expected
        }
    }

    @Test
    public void testDirectRoutes() {

        final HttpRouteDirector rd = BasicRouteDirector.INSTANCE;
        HttpRoute r = new HttpRoute(TARGET1, LOCAL41, false);
        RouteTracker rt = new RouteTracker(r);
        boolean complete = checkVia(rt, r, rd, 2);
        Assert.assertTrue("incomplete route 1", complete);

        r = new HttpRoute(TARGET2, LOCAL62, true);
        rt = new RouteTracker(r);
        complete = checkVia(rt, r, rd, 2);
        Assert.assertTrue("incomplete route 2", complete);
    }

    @Test
    public void testProxyRoutes() {

        final HttpRouteDirector rd = BasicRouteDirector.INSTANCE;
        HttpRoute r = new HttpRoute(TARGET2, null, PROXY1, false);
        RouteTracker rt = new RouteTracker(r);
        boolean complete = checkVia(rt, r, rd, 2);
        Assert.assertTrue("incomplete route 1", complete);

        // tunnelled, but neither secure nor layered
        r = new HttpRoute(TARGET1, LOCAL61, PROXY3, false,
                          TunnelType.TUNNELLED, LayerType.PLAIN);
        rt = new RouteTracker(r);
        complete = checkVia(rt, r, rd, 3);
        Assert.assertTrue("incomplete route 2", complete);

        // tunnelled, layered, but not secure
        r = new HttpRoute(TARGET1, LOCAL61, PROXY3, false,
                          TunnelType.TUNNELLED, LayerType.LAYERED);
        rt = new RouteTracker(r);
        complete = checkVia(rt, r, rd, 4);
        Assert.assertTrue("incomplete route 3", complete);

        // tunnelled, layered, secure
        r = new HttpRoute(TARGET1, LOCAL61, PROXY3, true);
        rt = new RouteTracker(r);
        complete = checkVia(rt, r, rd, 4);
        Assert.assertTrue("incomplete route 4", complete);
    }

    @Test
    public void testProxyChainRoutes() {

        final HttpRouteDirector rd = BasicRouteDirector.INSTANCE;
        HttpHost[] proxies = { PROXY1, PROXY2 };
        HttpRoute r = new HttpRoute(TARGET2, LOCAL42, proxies, false,
                                    TunnelType.PLAIN, LayerType.PLAIN);
        RouteTracker rt = new RouteTracker(r);
        boolean complete = checkVia(rt, r, rd, 3);
        Assert.assertTrue("incomplete route 1", complete);

        // tunnelled, but neither secure nor layered
        proxies = new HttpHost[]{ PROXY3, PROXY2 };
        r = new HttpRoute(TARGET1, null, proxies, false,
                          TunnelType.TUNNELLED, LayerType.PLAIN);
        rt = new RouteTracker(r);
        complete = checkVia(rt, r, rd, 4);
        Assert.assertTrue("incomplete route 2", complete);

        // tunnelled, layered, but not secure
        proxies = new HttpHost[]{ PROXY3, PROXY2, PROXY1 };
        r = new HttpRoute(TARGET2, LOCAL61, proxies, false,
                          TunnelType.TUNNELLED, LayerType.LAYERED);
        rt = new RouteTracker(r);
        complete = checkVia(rt, r, rd, 6);
        Assert.assertTrue("incomplete route 3", complete);

        // tunnelled, layered, secure
        proxies = new HttpHost[]{ PROXY1, PROXY3 };
        r = new HttpRoute(TARGET1, LOCAL61, proxies, true,
                          TunnelType.TUNNELLED, LayerType.LAYERED);
        rt = new RouteTracker(r);
        complete = checkVia(rt, r, rd, 5);
        Assert.assertTrue("incomplete route 4", complete);
    }

    @Test
    public void testEqualsHashcodeCloneToString()
        throws CloneNotSupportedException {

        final RouteTracker rt0 = new RouteTracker(TARGET1, null);
        final RouteTracker rt1 = new RouteTracker(TARGET2, null);
        final RouteTracker rt2 = new RouteTracker(TARGET1, null);
        final RouteTracker rt3 = new RouteTracker(TARGET1, null);
        final RouteTracker rt4 = new RouteTracker(TARGET1, LOCAL41);
        final RouteTracker rt6 = new RouteTracker(TARGET1, LOCAL62);

        Assert.assertFalse("rt0", rt0.equals(null));
        Assert.assertTrue("rt0", rt0.equals(rt0));
        Assert.assertFalse("rt0", rt0.equals(rt0.toString()));

        Assert.assertFalse("rt0 == rt4", rt0.equals(rt4));
        Assert.assertFalse("rt0 == rt1", rt0.equals(rt1)); // Check host takes part in equals

        // Check that connection takes part in equals
        Assert.assertTrue("rt0 != rt2", rt0.equals(rt2));
        rt2.connectTarget(false);
        Assert.assertFalse("rt0 == rt2", rt0.equals(rt2));

        Assert.assertTrue("rt0 != rt3", rt0.equals(rt3));
        rt3.connectTarget(true);
        Assert.assertFalse("rt0 == rt3", rt0.equals(rt3));
        Assert.assertFalse("rt2 == rt3", rt2.equals(rt3)); // Test secure takes part

        // TODO needs tests for tunnel and layered

        Assert.assertFalse("rt4 == rt0", rt4.equals(rt0));
        Assert.assertFalse("rt0 == rt6", rt0.equals(rt6));
        Assert.assertFalse("rt6 == rt0", rt6.equals(rt0));
        Assert.assertFalse("rt4 == rt6", rt4.equals(rt6));
        Assert.assertFalse("rt6 == rt4", rt6.equals(rt4));

        // it is likely but not guaranteed that the hashcodes are different
        Assert.assertFalse("rt0 == rt4 (hashcode)", rt0.hashCode() == rt4.hashCode());
        Assert.assertFalse("rt0 == rt6 (hashcode)", rt0.hashCode() == rt6.hashCode());
        Assert.assertFalse("rt6 == rt4 (hashcode)", rt6.hashCode() == rt4.hashCode());

        Assert.assertEquals("rt0 (clone)", rt0, rt0.clone());
        Assert.assertEquals("rt4 (clone)", rt4, rt4.clone());
        Assert.assertEquals("rt6 (clone)", rt6, rt6.clone());


        // we collect (clones of) the different tracked routes along the way
        // rt0 -> direct connection
        // rt1 -> via single proxy
        // rt2 -> via proxy chain
        final Set<RouteTracker> hs = new HashSet<RouteTracker>();

        // we also collect hashcodes for the different paths
        // since we can't guarantee what influence the HttpHost hashcodes have,
        // we keep separate sets here
        final Set<Integer> hc0 = new HashSet<Integer>();
        final Set<Integer> hc4 = new HashSet<Integer>();
        final Set<Integer> hc6 = new HashSet<Integer>();

        RouteTracker rt = null;

        Assert.assertTrue(hs.add(rt0));
        Assert.assertTrue(hs.add(rt4));
        Assert.assertTrue(hs.add(rt6));

        Assert.assertTrue(hc0.add(Integer.valueOf(rt0.hashCode())));
        Assert.assertTrue(hc4.add(Integer.valueOf(rt4.hashCode())));
        Assert.assertTrue(hc6.add(Integer.valueOf(rt6.hashCode())));

        rt = (RouteTracker) rt0.clone();
        rt.connectTarget(false);
        Assert.assertTrue(hs.add(rt));
        Assert.assertTrue(hc0.add(Integer.valueOf(rt.hashCode())));

        rt = (RouteTracker) rt0.clone();
        rt.connectTarget(true);
        Assert.assertTrue(hs.add(rt));
        Assert.assertTrue(hc0.add(Integer.valueOf(rt.hashCode())));


        // proxy (insecure) -> tunnel (insecure) -> layer (secure)
        rt = (RouteTracker) rt4.clone();
        rt.connectProxy(PROXY1, false);
        Assert.assertTrue(hs.add((RouteTracker) rt.clone()));
        // this is not guaranteed to be unique...
        Assert.assertTrue(hc4.add(Integer.valueOf(rt.hashCode())));

        rt.tunnelTarget(false);
        Assert.assertTrue(hs.add((RouteTracker) rt.clone()));
        Assert.assertTrue(hc4.add(Integer.valueOf(rt.hashCode())));

        rt.layerProtocol(true);
        Assert.assertTrue(hs.add((RouteTracker) rt.clone()));
        Assert.assertTrue(hc4.add(Integer.valueOf(rt.hashCode())));


        // proxy (secure) -> tunnel (secure) -> layer (insecure)
        rt = (RouteTracker) rt4.clone();
        rt.connectProxy(PROXY1, true);
        Assert.assertTrue(hs.add((RouteTracker) rt.clone()));
        // this is not guaranteed to be unique...
        Assert.assertTrue(hc4.add(Integer.valueOf(rt.hashCode())));

        rt.tunnelTarget(true);
        Assert.assertTrue(hs.add((RouteTracker) rt.clone()));
        Assert.assertTrue(hc4.add(Integer.valueOf(rt.hashCode())));

        rt.layerProtocol(false);
        Assert.assertTrue(hs.add((RouteTracker) rt.clone()));
        Assert.assertTrue(hc4.add(Integer.valueOf(rt.hashCode())));


        // PROXY1/i -> PROXY2/i -> tunnel/i -> layer/s
        rt = (RouteTracker) rt6.clone();
        rt.connectProxy(PROXY1, false);
        Assert.assertTrue(hs.add((RouteTracker) rt.clone()));
        // this is not guaranteed to be unique...
        Assert.assertTrue(hc6.add(Integer.valueOf(rt.hashCode())));

        rt.tunnelProxy(PROXY2, false);
        Assert.assertTrue(hs.add((RouteTracker) rt.clone()));
        // this is not guaranteed to be unique...
        Assert.assertTrue(hc6.add(Integer.valueOf(rt.hashCode())));

        rt.tunnelTarget(false);
        Assert.assertTrue(hs.add((RouteTracker) rt.clone()));
        Assert.assertTrue(hc6.add(Integer.valueOf(rt.hashCode())));

        rt.layerProtocol(true);
        Assert.assertTrue(hs.add((RouteTracker) rt.clone()));
        Assert.assertTrue(hc6.add(Integer.valueOf(rt.hashCode())));


        // PROXY1/s -> PROXY2/s -> tunnel/s -> layer/i
        rt = (RouteTracker) rt6.clone();
        rt.connectProxy(PROXY1, true);
        Assert.assertTrue(hs.add((RouteTracker) rt.clone()));
        // this is not guaranteed to be unique...
        Assert.assertTrue(hc6.add(Integer.valueOf(rt.hashCode())));

        rt.tunnelProxy(PROXY2, true);
        Assert.assertTrue(hs.add((RouteTracker) rt.clone()));
        // this is not guaranteed to be unique...
        Assert.assertTrue(hc6.add(Integer.valueOf(rt.hashCode())));

        rt.tunnelTarget(true);
        Assert.assertTrue(hs.add((RouteTracker) rt.clone()));
        Assert.assertTrue(hc6.add(Integer.valueOf(rt.hashCode())));

        rt.layerProtocol(false);
        Assert.assertTrue(hs.add((RouteTracker) rt.clone()));
        Assert.assertTrue(hc6.add(Integer.valueOf(rt.hashCode())));


        // PROXY2/i -> PROXY1/i -> tunnel/i -> layer/s
        rt = (RouteTracker) rt6.clone();
        rt.connectProxy(PROXY2, false);
        Assert.assertTrue(hs.add((RouteTracker) rt.clone()));
        // this is not guaranteed to be unique...
        Assert.assertTrue(hc6.add(Integer.valueOf(rt.hashCode())));

        rt.tunnelProxy(PROXY1, false);
        Assert.assertTrue(hs.add((RouteTracker) rt.clone()));
        // proxy chain sequence does not affect hashcode, so duplicate:
        // Assert.assertTrue(hc6.add(Integer.valueOf(rt.hashCode())));

        rt.tunnelTarget(false);
        Assert.assertTrue(hs.add((RouteTracker) rt.clone()));
        // proxy chain sequence does not affect hashcode, so duplicate:
        // Assert.assertTrue(hc6.add(Integer.valueOf(rt.hashCode())));

        rt.layerProtocol(true);
        Assert.assertTrue(hs.add((RouteTracker) rt.clone()));
        // proxy chain sequence does not affect hashcode, so duplicate:
        // Assert.assertTrue(hc6.add(Integer.valueOf(rt.hashCode())));


        // check that all toString are OK and different
        final Set<String> rtstrings = new HashSet<String>();
        for (final RouteTracker current: hs) {
            final String rts = checkToString(current);
            Assert.assertTrue("duplicate toString: " + rts, rtstrings.add(rts));
        }
    }


    /** Helper to check the status of the four flags. */
    public final static void checkCTLS(final RouteTracker rt,
                                       final boolean c, final boolean t,
                                       final boolean l, final boolean s) {
        final String rts = rt.toString();
        Assert.assertEquals("wrong flag connected: " + rts, c, rt.isConnected());
        Assert.assertEquals("wrong flag tunnelled: " + rts, t, rt.isTunnelled());
        Assert.assertEquals("wrong enum tunnelled: " + rts,
                     t ? TunnelType.TUNNELLED : TunnelType.PLAIN,
                     rt.getTunnelType());
        Assert.assertEquals("wrong flag layered: "   + rts, l, rt.isLayered());
        Assert.assertEquals("wrong enum layered: "   + rts,
                     l ? LayerType.LAYERED : LayerType.PLAIN,
                     rt.getLayerType());
        Assert.assertEquals("wrong flag secure: "    + rts, s, rt.isSecure());
    }


    /**
     * Helper to check tracking of a route.
     * This uses a {@link HttpRouteDirector} to fake establishing the route,
     * checking the intermediate steps.
     *
     * @param rt        the tracker to check with
     * @param r         the route to establish
     * @param rd        the director to check with
     * @param steps     the step count for this invocation
     *
     * @return  {@code true} iff the route is complete
     */
    public final static boolean checkVia(final RouteTracker rt, final HttpRoute r,
                                         final HttpRouteDirector rd, final int steps) {

        final String msg = r.toString() + " @ " + rt.toString();

        boolean complete = false;
        int n = steps;
        while (!complete && (n > 0)) {

            final int action = rd.nextStep(r, rt.toRoute());
            switch (action) {

            case HttpRouteDirector.COMPLETE:
                complete = true;
                Assert.assertEquals(r, rt.toRoute());
                break;

            case HttpRouteDirector.CONNECT_TARGET: {
                final boolean sec = r.isSecure();
                rt.connectTarget(sec);
                checkCTLS(rt, true, false, false, sec);
                Assert.assertEquals("wrong hop count "+msg,
                             1, rt.getHopCount());
                Assert.assertEquals("wrong hop0 "+msg,
                             r.getTargetHost(), rt.getHopTarget(0));
            } break;

            case HttpRouteDirector.CONNECT_PROXY: {
                // we assume an insecure proxy connection
                final boolean sec = false;
                rt.connectProxy(r.getProxyHost(), sec);
                checkCTLS(rt, true, false, false, sec);
                Assert.assertEquals("wrong hop count "+msg,
                             2, rt.getHopCount());
                Assert.assertEquals("wrong hop0 "+msg,
                             r.getProxyHost(), rt.getHopTarget(0));
                Assert.assertEquals("wrong hop1 "+msg,
                             r.getTargetHost(), rt.getHopTarget(1));
            } break;

            case HttpRouteDirector.TUNNEL_TARGET: {
                final int hops = rt.getHopCount();
                // we assume an insecure tunnel
                final boolean sec = false;
                rt.tunnelTarget(sec);
                checkCTLS(rt, true, true, false, sec);
                Assert.assertEquals("wrong hop count "+msg,
                             hops, rt.getHopCount());
                Assert.assertEquals("wrong hop0 "+msg,
                             r.getProxyHost(), rt.getHopTarget(0));
                Assert.assertEquals("wrong hopN "+msg,
                             r.getTargetHost(), rt.getHopTarget(hops-1));
            } break;

            case HttpRouteDirector.TUNNEL_PROXY: {
                final int hops = rt.getHopCount(); // before tunnelling
                // we assume an insecure tunnel
                final boolean  sec = false;
                final HttpHost pxy = r.getHopTarget(hops-1);
                rt.tunnelProxy(pxy, sec);
                // Since we're tunnelling to a proxy and not the target,
                // the 'tunelling' flag is false: no end-to-end tunnel.
                checkCTLS(rt, true, false, false, sec);
                Assert.assertEquals("wrong hop count "+msg,
                             hops+1, rt.getHopCount());
                Assert.assertEquals("wrong hop0 "+msg,
                             r.getProxyHost(), rt.getHopTarget(0));
                Assert.assertEquals("wrong hop"+hops+" "+msg,
                             pxy, rt.getHopTarget(hops-1));
                Assert.assertEquals("wrong hopN "+msg,
                             r.getTargetHost(), rt.getHopTarget(hops));
            } break;

            case HttpRouteDirector.LAYER_PROTOCOL: {
                final int    hops = rt.getHopCount();
                final boolean tun = rt.isTunnelled();
                final boolean sec = r.isSecure();
                rt.layerProtocol(sec);
                checkCTLS(rt, true, tun, true, sec);
                Assert.assertEquals("wrong hop count "+msg,
                             hops, rt.getHopCount());
                Assert.assertEquals("wrong proxy "+msg,
                             r.getProxyHost(), rt.getProxyHost());
                Assert.assertEquals("wrong target "+msg,
                             r.getTargetHost(), rt.getTargetHost());
            } break;


            // UNREACHABLE
            default:
                Assert.fail("unexpected action " + action + " from director, "+msg);
                break;

            } // switch
            n--;
        }

        return complete;
    } // checkVia


    /**
     * Checks the output of {@code toString}.
     *
     * @param rt        the tracker for which to check the output
     *
     * @return  the result of {@code rt.toString()}
     */
    public final static String checkToString(final RouteTracker rt) {
        if (rt == null) {
            return null;
        }

        final String rts = rt.toString();

        if (rt.getLocalAddress() != null) {
            final String las = rt.getLocalAddress().toString();
            Assert.assertFalse("no local address in toString(): " + rts,
                    !rts.contains(las));
        }

        for (int i=0; i<rt.getHopCount(); i++) {
            final String hts = rt.getHopTarget(i).toString();
            Assert.assertFalse("hop "+i+" ("+hts+") missing in toString(): " + rts,
                    !rts.contains(hts));
        }

        return rts;
    }

}
