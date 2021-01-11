package com.force.react;

/**
 * @author Chathura Sarathchandra
 */

public interface IPC {

    /**
     * Performs the specified request.
     * <p>
     * Subclasses handle OS specific IPC quirks, such as IPC selection,
     * Intra-process and Intra-process IPC handling.
     *
     * @param request      Request to process
     * @param functionName The name of the function (reversed domain name)
     * @param functionClass
     * @return A {@link NetworkResponse} with data and caching metadata; will never be null
     * @throws VolleyError on errors
     */
    IPCResponse performRequest(Request<?> request, String functionName, VolleyIPCFunction functionClass) throws
            VolleyError;
}
