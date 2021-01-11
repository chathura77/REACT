/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.force.react;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class RequestTest {

    @Test
    public void compareTo() {
        int sequence = 0;
        TestRequest low = new TestRequest(Request.Priority.LOW);
        low.setSequence(sequence++);
        TestRequest low2 = new TestRequest(Request.Priority.LOW);
        low2.setSequence(sequence++);
        TestRequest high = new TestRequest(Request.Priority.HIGH);
        high.setSequence(sequence++);
        TestRequest immediate = new TestRequest(Request.Priority.IMMEDIATE);
        immediate.setSequence(sequence++);

        // "Low" should sort higher because it's really processing order.
        assertTrue(low.compareTo(high) > 0);
        assertTrue(high.compareTo(low) < 0);
        assertTrue(low.compareTo(low2) < 0);
        assertTrue(low.compareTo(immediate) > 0);
        assertTrue(immediate.compareTo(high) < 0);
    }

    @Test
    public void urlParsing() {
        UrlParseRequest nullUrl = new UrlParseRequest(null);
        assertEquals(0, nullUrl.getTrafficStatsTag());
        UrlParseRequest emptyUrl = new UrlParseRequest("");
        assertEquals(0, emptyUrl.getTrafficStatsTag());
        UrlParseRequest noHost = new UrlParseRequest("http:///");
        assertEquals(0, noHost.getTrafficStatsTag());
        UrlParseRequest badProtocol = new UrlParseRequest("bad:http://foo");
        assertEquals(0, badProtocol.getTrafficStatsTag());
        UrlParseRequest goodProtocol = new UrlParseRequest("http://foo");
        assertFalse(0 == goodProtocol.getTrafficStatsTag());
    }

    private class TestRequest extends Request<Object> {
        private Priority mPriority = Priority.NORMAL;

        public TestRequest(Priority priority) {
            super(Request.Method.GET, "", null);
            mPriority = priority;
        }

        @Override
        public Priority getPriority() {
            return mPriority;
        }

        @Override
        protected void deliverResponse(Object response) {
        }

        @Override
        protected Response<Object> parseNetworkResponse(NetworkResponse response) {
            return null;
        }

        /**
         * !! a STUB for now!!
         * Subclasses must implement this to parse the raw IPC response
         * and return an appropriate response type. This method will be
         * called from a worker thread.  The response will not be delivered
         * if you return null.
         *
         * @param response Response from the IPC service
         * @return The parsed response, or null in the case of an error
         */
        @Override
        protected Response<Object> parseIPCResponse(IPCResponse response) {
            return null;
        }
    }

    private class UrlParseRequest extends Request<Object> {
        public UrlParseRequest(String url) {
            super(Request.Method.GET, url, null);
        }

        @Override
        protected void deliverResponse(Object response) {
        }

        @Override
        protected Response<Object> parseNetworkResponse(NetworkResponse response) {
            return null;
        }

        /**
         * !! a STUB for now!!
         * Subclasses must implement this to parse the raw IPC response
         * and return an appropriate response type. This method will be
         * called from a worker thread.  The response will not be delivered
         * if you return null.
         *
         * @param response Response from the IPC service
         * @return The parsed response, or null in the case of an error
         */
        @Override
        protected Response<Object> parseIPCResponse(IPCResponse response) {
            return null;
        }
    }
}
