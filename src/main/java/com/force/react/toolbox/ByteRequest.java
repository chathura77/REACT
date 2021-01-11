package com.force.react.toolbox;

import com.force.react.IPCResponse;
import com.force.react.NetworkResponse;
import com.force.react.Request;
import com.force.react.Response;

import org.apache.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

/**
 * A canned request for getting a byte[] at a given URL.
 * <p>
 * @author Chathura Sarathchandra
 */

public class ByteRequest extends Request<ByteRequest.ByteResponse> {

    private final Response.Listener<ByteRequest.ByteResponse> mListener;

    /**
     * Creates a new request with the given method.
     *
     * @param method           the request {@link Method} to use
     * @param url              URL to fetch the byte array at
     * @param responseListener Listener to receive the byte array response
     * @param errorListener    Listener to receive the error message
     */
    public ByteRequest(int method, String url, Response.Listener<ByteRequest.ByteResponse>
            responseListener,
                       Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        mListener = responseListener;
    }

    /**
     * Creates a new GET request
     *
     * @param url              URL to fetch the byte array at
     * @param responseListener Listener to receive the byte array response
     * @param errorListener    Listener to receive the error message
     */
    public ByteRequest(String url, Response.Listener<ByteRequest.ByteResponse> responseListener,
                       Response
                               .ErrorListener
                               errorListener) {
        this(Method.GET, url, responseListener, errorListener);
    }

    /**
     * Subclasses must implement this to parse the raw network response
     * and return an appropriate response type. This method will be
     * called from a worker thread.  The response will not be delivered
     * if you return null.
     *
     * @param response Response from the network
     * @return The parsed response, or null in the case of an error
     */
    @Override
    protected Response<ByteRequest.ByteResponse> parseNetworkResponse(NetworkResponse response) {

        ByteResponse byteResponse = new ByteResponse();
        byteResponse.headers = response.headers;
        byteResponse.response = response.data;
        byteResponse.statusCode = response.statusCode;

        return Response.success(byteResponse, HttpHeaderParser
                .parseCacheHeaders(response));
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
    protected Response<ByteRequest.ByteResponse> parseIPCResponse(IPCResponse response) {

        ByteResponse byteResponse = new ByteResponse();

        // TODO: this is only for testing purposes -- very static
        byteResponse.statusCode = response.data != null && response.data
                .length > 0 ?
                HttpStatus.SC_OK :
                HttpStatus.SC_NOT_FOUND; //might even not reach here

        byteResponse.response = response.data;

        return Response.success(byteResponse, HttpHeaderParser
                .parseCacheHeaders(response));
    }

    /**
     * Subclasses must implement this to perform delivery of the parsed
     * response to their listeners.  The given response is guaranteed to
     * be non-null; responses that fail to parse are not delivered.
     *
     * @param response The parsed response returned by
     *                 {@link #parseNetworkResponse(NetworkResponse)}
     */
    @Override
    protected void deliverResponse(ByteRequest.ByteResponse response) {
        if (mListener != null) {
            mListener.onResponse(response);
        }
    }

    /**
     * A Class that wraps the response and the HTTP response headers
     */
    public static class ByteResponse {

        // HTTP headers -- null for IPCResponse
        public Map<String, String> headers = new HashMap<>();

        // response.data
        public byte[] response = null;

        // status code
        public int statusCode = 0;

        public int reference = 0;
    }
}
