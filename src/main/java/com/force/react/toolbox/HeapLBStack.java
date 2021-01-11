package com.force.react.toolbox;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.force.react.AuthFailureError;
import com.force.react.HeapUtil;
import com.force.react.IPCResponse;
import com.force.react.IPCUtil;
import com.force.react.Request;
import com.force.react.VolleyIPCFunction;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * @author Chathura Sarathchandra
 */

public class HeapLBStack implements IPCStack {
    public static final String TAG = HeapLBStack.class.getName();

    /***
     * The local broadcast manager
     */
    LocalBroadcastManager localBroadcastManager = null;

    /**
     * Request ID to uniquely identify the request and response.
     */
    private String reqID;

    public HeapLBStack() {
        reqID = "";
        localBroadcastManager = IPCUtil.getLocalBroadcastManager(TAG);
    }

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    @Override
    public IPCResponse performRequest(Request<?> request, String functionName, VolleyIPCFunction functionClass) throws IOException, AuthFailureError {
        switch (request.getMethod()) {
            case Request.Method.DEPRECATED_GET_OR_POST:
                Log.e(TAG, "DEPRECATED_GET_OR_POST");
                break;
            case Request.Method.GET:
//                // Not necessary to set the request method because connection defaults to GET but
//                // being explicit here.
                Log.i(TAG, "Entering Request.Method.GET");

                Log.i(TAG, "The function name " + functionName);

                byte[] postBody = request.getPostBody();
                if (postBody == null) {
                    reqID = IPCUtil.generateReqID();

                    IPCUtil.Properties
                            properties = new IPCUtil.Properties();

                    IPCUtil.sendMessage(reqID, functionName, request,
                            IPCUtil.IPC_GET,
                            localBroadcastManager, properties);

                    byte[] response = properties
                            .getRequestResponse();

                    int reference = ByteArrayHeap.NULL_REFERENCE;
                    if (response != null) { // if a valid response
                        // do not have to use 'instanceof' as we know exactly
                        // which class in use.
                        if (request.getClass() == ByteRequest.class) {
                            reference = ByteBuffer.wrap(response).getInt(0);
                            response = HeapUtil.getBlockData(reference, TAG);
                        }
                    } else Log.e(TAG, "Null response received! ");
                    return new IPCResponse(response);
                }
                break;
            case Request.Method.DELETE:
                break;
            case Request.Method.POST:
                break;
            case Request.Method.PUT:
                break;
            case Request.Method.HEAD:
//                connection.setRequestMethod("HEAD");
                break;
            case Request.Method.OPTIONS:
//                connection.setRequestMethod("OPTIONS");
                break;
            case Request.Method.TRACE:
//                connection.setRequestMethod("TRACE");
                break;
            case Request.Method.PATCH:
//                connection.setRequestMethod("PATCH");
//                addBodyIfExists(connection, request);
                break;
            default:
                throw new IllegalStateException("Unknown method type.");
        }
        return null;
    }

}
