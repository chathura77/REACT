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

package com.force.react.toolbox;

import com.force.react.AuthFailureError;
import com.force.react.IPCResponse;
import com.force.react.NetworkResponse;
import com.force.react.Request;
import com.force.react.Response;
import com.force.react.RetryPolicy;
import com.force.react.ServerError;
import com.force.react.TimeoutError;
import com.force.react.VolleyError;
import com.force.react.mock.MockHttpStack;

import org.apache.http.ProtocolVersion;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHttpResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.robolectric.RobolectricTestRunner;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(RobolectricTestRunner.class)
public class BasicNetworkTest {

    @Mock
    private Request<String> mMockRequest;
    @Mock
    private RetryPolicy mMockRetryPolicy;
    private BasicNetwork mNetwork;

    private static Request<String> buildRequest() {
        return new Request<String>(Request.Method.GET, "http://foo", null) {

            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                return null;
            }

            /**
             * Subclasses must implement this to parse the raw IPC response
             * and return an appropriate response type. This method will be
             * called from a worker thread.  The response will not be delivered
             * if you return null.
             *
             * @param response Response from the IPC service
             * @return The parsed response, or null in the case of an error
             */
            @Override
            protected Response<String> parseIPCResponse(IPCResponse response) {
                return null;
            }

            @Override
            protected void deliverResponse(String response) {
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> result = new HashMap<String, String>();
                result.put("requestheader", "foo");
                return result;
            }

            @Override
            public Map<String, String> getParams() {
                Map<String, String> result = new HashMap<String, String>();
                result.put("requestpost", "foo");
                return result;
            }
        };
    }

    @Before
    public void setUp() throws Exception {
        initMocks(this);
    }

    @Test
    public void headersAndPostParams() throws Exception {
        MockHttpStack mockHttpStack = new MockHttpStack();
        BasicHttpResponse fakeResponse = new BasicHttpResponse(new ProtocolVersion("HTTP", 1, 1),
                200, "OK");
        fakeResponse.setEntity(new StringEntity("foobar"));
        mockHttpStack.setResponseToReturn(fakeResponse);
        BasicNetwork httpNetwork = new BasicNetwork(mockHttpStack);
        Request<String> request = buildRequest();
        httpNetwork.performRequest(request);
        assertEquals("foo", mockHttpStack.getLastHeaders().get("requestheader"));
        assertEquals("requestpost=foo&", new String(mockHttpStack.getLastPostBody()));
    }

    @Test
    public void socketTimeout() throws Exception {
        MockHttpStack mockHttpStack = new MockHttpStack();
        mockHttpStack.setExceptionToThrow(new SocketTimeoutException());
        BasicNetwork httpNetwork = new BasicNetwork(mockHttpStack);
        Request<String> request = buildRequest();
        request.setRetryPolicy(mMockRetryPolicy);
        doThrow(new VolleyError()).when(mMockRetryPolicy).retry(any(VolleyError.class));
        try {
            httpNetwork.performRequest(request);
        } catch (VolleyError e) {
            // expected
        }
        // should retry socket timeouts
        verify(mMockRetryPolicy).retry(any(TimeoutError.class));
    }

    @Test
    public void connectTimeout() throws Exception {
        MockHttpStack mockHttpStack = new MockHttpStack();
        mockHttpStack.setExceptionToThrow(new ConnectTimeoutException());
        BasicNetwork httpNetwork = new BasicNetwork(mockHttpStack);
        Request<String> request = buildRequest();
        request.setRetryPolicy(mMockRetryPolicy);
        doThrow(new VolleyError()).when(mMockRetryPolicy).retry(any(VolleyError.class));
        try {
            httpNetwork.performRequest(request);
        } catch (VolleyError e) {
            // expected
        }
        // should retry connection timeouts
        verify(mMockRetryPolicy).retry(any(TimeoutError.class));
    }

    @Test
    public void noConnection() throws Exception {
        MockHttpStack mockHttpStack = new MockHttpStack();
        mockHttpStack.setExceptionToThrow(new IOException());
        BasicNetwork httpNetwork = new BasicNetwork(mockHttpStack);
        Request<String> request = buildRequest();
        request.setRetryPolicy(mMockRetryPolicy);
        doThrow(new VolleyError()).when(mMockRetryPolicy).retry(any(VolleyError.class));
        try {
            httpNetwork.performRequest(request);
        } catch (VolleyError e) {
            // expected
        }
        // should not retry when there is no connection
        verify(mMockRetryPolicy, never()).retry(any(VolleyError.class));
    }

    @Test
    public void unauthorized() throws Exception {
        MockHttpStack mockHttpStack = new MockHttpStack();
        BasicHttpResponse fakeResponse = new BasicHttpResponse(new ProtocolVersion("HTTP", 1, 1),
                401, "Unauthorized");
        mockHttpStack.setResponseToReturn(fakeResponse);
        BasicNetwork httpNetwork = new BasicNetwork(mockHttpStack);
        Request<String> request = buildRequest();
        request.setRetryPolicy(mMockRetryPolicy);
        doThrow(new VolleyError()).when(mMockRetryPolicy).retry(any(VolleyError.class));
        try {
            httpNetwork.performRequest(request);
        } catch (VolleyError e) {
            // expected
        }
        // should retry in case it's an auth failure.
        verify(mMockRetryPolicy).retry(any(AuthFailureError.class));
    }

    @Test
    public void forbidden() throws Exception {
        MockHttpStack mockHttpStack = new MockHttpStack();
        BasicHttpResponse fakeResponse = new BasicHttpResponse(new ProtocolVersion("HTTP", 1, 1),
                403, "Forbidden");
        mockHttpStack.setResponseToReturn(fakeResponse);
        BasicNetwork httpNetwork = new BasicNetwork(mockHttpStack);
        Request<String> request = buildRequest();
        request.setRetryPolicy(mMockRetryPolicy);
        doThrow(new VolleyError()).when(mMockRetryPolicy).retry(any(VolleyError.class));
        try {
            httpNetwork.performRequest(request);
        } catch (VolleyError e) {
            // expected
        }
        // should retry in case it's an auth failure.
        verify(mMockRetryPolicy).retry(any(AuthFailureError.class));
    }

    @Test
    public void redirect() throws Exception {
        for (int i = 300; i <= 399; i++) {
            MockHttpStack mockHttpStack = new MockHttpStack();
            BasicHttpResponse fakeResponse =
                    new BasicHttpResponse(new ProtocolVersion("HTTP", 1, 1), i, "");
            mockHttpStack.setResponseToReturn(fakeResponse);
            BasicNetwork httpNetwork = new BasicNetwork(mockHttpStack);
            Request<String> request = buildRequest();
            request.setRetryPolicy(mMockRetryPolicy);
            doThrow(new VolleyError()).when(mMockRetryPolicy).retry(any(VolleyError.class));
            try {
                httpNetwork.performRequest(request);
            } catch (VolleyError e) {
                // expected
            }
            // should not retry 300 responses.
            verify(mMockRetryPolicy, never()).retry(any(VolleyError.class));
            reset(mMockRetryPolicy);
        }
    }

    @Test
    public void otherClientError() throws Exception {
        for (int i = 400; i <= 499; i++) {
            if (i == 401 || i == 403) {
                // covered above.
                continue;
            }
            MockHttpStack mockHttpStack = new MockHttpStack();
            BasicHttpResponse fakeResponse =
                    new BasicHttpResponse(new ProtocolVersion("HTTP", 1, 1), i, "");
            mockHttpStack.setResponseToReturn(fakeResponse);
            BasicNetwork httpNetwork = new BasicNetwork(mockHttpStack);
            Request<String> request = buildRequest();
            request.setRetryPolicy(mMockRetryPolicy);
            doThrow(new VolleyError()).when(mMockRetryPolicy).retry(any(VolleyError.class));
            try {
                httpNetwork.performRequest(request);
            } catch (VolleyError e) {
                // expected
            }
            // should not retry other 400 errors.
            verify(mMockRetryPolicy, never()).retry(any(VolleyError.class));
            reset(mMockRetryPolicy);
        }
    }

    @Test
    public void serverError_enableRetries() throws Exception {
        for (int i = 500; i <= 599; i++) {
            MockHttpStack mockHttpStack = new MockHttpStack();
            BasicHttpResponse fakeResponse =
                    new BasicHttpResponse(new ProtocolVersion("HTTP", 1, 1), i, "");
            mockHttpStack.setResponseToReturn(fakeResponse);
            BasicNetwork httpNetwork =
                    new BasicNetwork(mockHttpStack, new ByteArrayPool(4096));
            Request<String> request = buildRequest();
            request.setRetryPolicy(mMockRetryPolicy);
            request.setShouldRetryServerErrors(true);
            doThrow(new VolleyError()).when(mMockRetryPolicy).retry(any(VolleyError.class));
            try {
                httpNetwork.performRequest(request);
            } catch (VolleyError e) {
                // expected
            }
            // should retry all 500 errors
            verify(mMockRetryPolicy).retry(any(ServerError.class));
            reset(mMockRetryPolicy);
        }
    }

    @Test
    public void serverError_disableRetries() throws Exception {
        for (int i = 500; i <= 599; i++) {
            MockHttpStack mockHttpStack = new MockHttpStack();
            BasicHttpResponse fakeResponse =
                    new BasicHttpResponse(new ProtocolVersion("HTTP", 1, 1), i, "");
            mockHttpStack.setResponseToReturn(fakeResponse);
            BasicNetwork httpNetwork = new BasicNetwork(mockHttpStack);
            Request<String> request = buildRequest();
            request.setRetryPolicy(mMockRetryPolicy);
            doThrow(new VolleyError()).when(mMockRetryPolicy).retry(any(VolleyError.class));
            try {
                httpNetwork.performRequest(request);
            } catch (VolleyError e) {
                // expected
            }
            // should not retry any 500 error w/ HTTP 500 retries turned off (the default).
            verify(mMockRetryPolicy, never()).retry(any(VolleyError.class));
            reset(mMockRetryPolicy);
        }
    }
}
