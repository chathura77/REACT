package com.force.react.toolbox;

import com.force.react.AuthFailureError;
import com.force.react.IPCResponse;
import com.force.react.Request;
import com.force.react.VolleyIPCFunction;

import java.io.IOException;

/**
 * Interface for all IPC specific implementations.
 * <p>
 * @author Chathura Sarathchandra
 */

public interface IPCStack {

    /**
     * Performs an HTTP request with the given parameters.
     * <p>
     * <p>A GET request is sent if request.getPostBody() == null. A POST request is sent otherwise,
     * and the Content-Type header is set to request.getPostBodyContentType().</p>
     *
     * @param request      the request to perform
     * @param functionName The name of the function (reversed domain name)
     * @param functionClass
     * @return the HTTP response
     */
    IPCResponse performRequest(Request<?> request, String functionName, VolleyIPCFunction functionClass)
            throws IOException, AuthFailureError;
}
