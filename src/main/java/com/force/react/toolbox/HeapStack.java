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

public class HeapStack implements IPCStack {
    public static final String TAG = HeapStack.class.getName();

    /***
     * The local broadcast manager
     */
    LocalBroadcastManager localBroadcastManager = null;

    /**
     * Request ID to uniquely identify the request and response.
     */
    private String reqID;

    public HeapStack() {
        reqID = "";
        localBroadcastManager = IPCUtil.getLocalBroadcastManager(TAG);
    }

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    @Override
    public IPCResponse performRequest(Request<?> request, String
            functionName, VolleyIPCFunction functionInterface) throws
            IOException, AuthFailureError {
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

//                    AsyncTask<Void, Void, Integer> responseTask =
//                            functionInterface.handleGETreq(null);
//
//                    responseTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                    Integer response = null;
//                    try {
//                        response = responseTask.get();
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    } catch (ExecutionException e) {
//                        e.printStackTrace();
//                    }

                    functionInterface.handleGETreq(null, request).run();
                    response = (Integer) functionInterface.getResponse();

                    // check if the response contains an exception
                    if (response == null) {
//                        Log.e(TAG, "Null response received! ");
                        return new IPCResponse(null);
                    }

                    if (request.getClass() == ByteRequest.class && response.intValue() > 0) {
                        byte[] result = HeapUtil.getBlockData(response.intValue(), TAG);
                        return new IPCResponse(result);
                    }

                    return new IPCResponse(ByteBuffer.allocate(4).putInt
                            (response).array());
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
