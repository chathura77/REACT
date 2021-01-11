package com.force.react.toolbox;

import android.annotation.SuppressLint;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.force.react.AuthFailureError;
import com.force.react.IPCResponse;
import com.force.react.IPCUtil;
import com.force.react.Request;
import com.force.react.VolleyIPCFunction;

import java.io.IOException;

/**
 * Implements Android LocalBroadcastReceiver specific procedures.
 *
 * @author Chathura Sarathchandra
 */
public class LocalBroadcastStack implements IPCStack {
    public static final String TAG = LocalBroadcastStack.class.getName();

    /***
     * The local broadcast manager
     */
    LocalBroadcastManager localBroadcastManager = null;

    /**
     * Request ID to uniquely identify the request and response.
     */
    private String reqID;

    public LocalBroadcastStack() {
        reqID = "";
        localBroadcastManager = IPCUtil.getLocalBroadcastManager(TAG);
    }

    /**
     * Performs an IPC request with the given parameters.
     * <p>
     * <p>A GET request is sent if request.getPostBody() == null. A POST request is sent otherwise,
     * and the Content-Type header is set to request.getPostBodyContentType().</p>
     *
     * @param request      the request to perform
     * @param functionName The name of the function (reversed domain name)
     * @param functionClass
     * @return the HTTP response
     */
    @SuppressLint("NewApi")
    @SuppressWarnings("deprecation")
    @Override
    public IPCResponse performRequest(Request<?> request, String functionName, VolleyIPCFunction functionClass)
            throws IOException, AuthFailureError {

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

                    return new IPCResponse(properties.getRequestResponse());
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

    public String getReqID() {
        return reqID;
    }
}
