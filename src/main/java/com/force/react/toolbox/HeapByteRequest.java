package com.force.react.toolbox;

import android.util.Log;

import com.force.react.Heap;
import com.force.react.NetworkResponse;
import com.force.react.Response;

import java.nio.ByteBuffer;

/**
 * A canned request for getting a byte[] at a given URL using
 * {@link com.force.react.Heap}. Always receive a reference to the memory
 * block.
 * <p>
 * @author Chathura Sarathchandra
 */
public class HeapByteRequest extends ByteRequest {

    private static String TAG = HeapByteRequest.class.getName();

    /**
     * Creates a new request with the given method.
     *
     * @param method           the request {@link Method} to use
     * @param url              URL to fetch the byte array at
     * @param responseListener Listener to receive the byte array response
     * @param errorListener    Listener to receive the error message
     */
    public HeapByteRequest(int method, String url, Response.Listener<ByteResponse> responseListener, Response.ErrorListener errorListener) {
        super(method, url, responseListener, errorListener);
    }

    /**
     * Creates a new GET request
     *
     * @param url              URL to fetch the byte array at
     * @param responseListener Listener to receive the byte array response
     * @param errorListener    Listener to receive the error message
     */
    public HeapByteRequest(String url, Response.Listener<ByteResponse>
            responseListener, Response.ErrorListener errorListener) {
        super(url, responseListener, errorListener);
    }

    /**
     * Reads the block referenced in the response from {@link Heap} and
     * returns a {@link com.force.react.toolbox.ByteRequest.ByteResponse}
     * with data added as a response (instead of the reference), and the
     * reference added to the .reference variable.
     *
     * @param response a
     *                 {@link com.force.react.toolbox.ByteRequest.ByteResponse} object with
     *                 the reference to a block {@link Heap}.
     * @return ByteResponse with data (instead of the reference)
     */
    public static ByteResponse parseHeapResponse(ByteResponse response) {
        response.reference = ByteBuffer
                .wrap(response.response).getInt(0);

        // currently sets the status code for local Heap responses, only if its
        // an error.
        if (response.reference < 0) {
            response.statusCode = response.reference;
            response.response = null;
            return response;
        }

        response.response = HeapFactory.getInstance().read(response.reference);
        return response;
    }

    /**
     * Stores the incoming response in heap.
     *
     * @param response the response
     * @return the reference to the heap block.
     */
    public static ByteResponse storeAndParseHeapByteResponse(ByteResponse response) {
        Heap heap = HeapFactory.getInstance();
        byte[] res = response.response;

        Log.i(TAG, "Heap object: " + heap.hashCode());
        Log.i(TAG, "Heap framesize: " + res.length);
        int reference = heap.malloc(res.length);

        if (reference == Heap.NULL_REFERENCE) {
            Log.e(TAG, "Heap:malloc Null Reference");
        } else if (reference == Heap.INSUFFICIENT_MEMORY) {
            Log.e(TAG, "Heap: Insufficient Memory");
        } else if (reference == Heap.INVALID_REFERENCE) {
            Log.e(TAG, "Heap: Invalid Reference");
        } else Log.i(TAG, "Heap: Successfully allocated block in Heap! ");

        int wResult = heap.write(reference, res);

        if (wResult == Heap.NULL_REFERENCE) {
            Log.e(TAG, "Heap:write Null Reference");
        } else if (wResult == Heap.INSUFFICIENT_MEMORY) {
            Log.e(TAG, "Heap: Insufficient Memory");
        } else if (wResult == Heap.INVALID_REFERENCE) {
            Log.e(TAG, "Heap: Invalid Reference");
        } else Log.i(TAG, "Heap: Successfully written to Heap! ");

        response.response = ByteBuffer.allocate(4).putInt(reference).array();
        return response;
    }

    /**
     * Stores the response in Heap and passes the reference
     *
     * @param response the reference to heap block
     * @return The parsed response, or null in the case of an error
     */
    @Override
    protected Response<ByteRequest.ByteResponse> parseNetworkResponse(NetworkResponse response) {

        ByteResponse byteResponse = new ByteResponse();
        byteResponse.headers = response.headers;
        byteResponse.response = response.data;
        byteResponse.statusCode = response.statusCode;

        // Only store if the response does not contain following known errors.
        if (byteResponse.statusCode != 404 &&
                byteResponse.statusCode != 204 &&
                byteResponse.statusCode != 500) {
            byteResponse = storeAndParseHeapByteResponse(byteResponse);
        }

        return Response.success(byteResponse, HttpHeaderParser
                .parseCacheHeaders(response));
    }
}
