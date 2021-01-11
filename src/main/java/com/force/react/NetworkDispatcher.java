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

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.net.TrafficStats;
import android.os.Build;
import android.os.IBinder;
import android.os.Process;
import android.os.SystemClock;
import android.util.Log;

import com.force.react.toolbox.AndroidIPC;
import com.force.react.toolbox.HelperVirtDevService;

import java.util.concurrent.BlockingQueue;

/**
 * Provides a thread for performing network dispatch from a queue of requests.
 * <p>
 * Requests added to the specified queue are processed from the network via a
 * specified {@link Network} interface. Responses are committed to cache, if
 * eligible, using a specified {@link Cache} interface. Valid responses and
 * errors are posted back to the caller via a {@link ResponseDelivery}.
 */
public class NetworkDispatcher extends Thread {

    /**
     * Logging tag
     */
    private static final String TAG = NetworkDispatcher.class.getName();
    /**
     * The context of the application
     */
    private final Context context = IPCUtil.getLocalContext();
    /**
     * The queue of requests to service.
     */
    private final BlockingQueue<Request<?>> mQueue;
    /**
     * The network interface for processing requests.
     */
    private final Network mNetwork;
    /**
     * The cache to write to.
     */
    private final Cache mCache;
    /**
     * For posting responses and errors.
     */
    private final ResponseDelivery mDelivery;
    /**
     * Lookup properties
     */
    IPCUtil.Properties properties = new IPCUtil
            .Properties();
    /**
     * The IPC interface for processing requests.
     */
    private IPC mIPC;
    /**
     * Used for telling us to die.
     */
    private volatile boolean mQuit = false;
    /**
     * The address of the local function (reverse domain name)
     */
    private String functionName;
    /**
     * Time to wait for available local function query.
     */
    private int WAIT_FOR_VIRT_SERVICE = 2000;

    /**
     * Used for sending Messenger requests to the virtDevService
     */
    private IBinder serviceBinder;

    /**
     * Creates a new network dispatcher thread.  You must call {@link #start()}
     * in order to begin processing.
     *
     * @param queue    Queue of incoming requests for triage
     * @param network  Network interface to use for performing requests
     * @param cache    Cache interface to use for writing responses to cache
     * @param delivery Delivery interface to use for posting responses
     */
    public NetworkDispatcher(BlockingQueue<Request<?>> queue,
                             Network network, Cache cache,
                             ResponseDelivery delivery) {
        mQueue = queue;
        mNetwork = network;
        mCache = cache;
        mDelivery = delivery;
    }

    /**
     * Forces this dispatcher to quit immediately.  If any requests are still in
     * the queue, they are not guaranteed to be processed.
     */
    public void quit() {
        mQuit = true;
        interrupt();
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void addTrafficStatsTag(Request<?> request) {
        // Tag the request (if API >= 14)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            TrafficStats.setThreadStatsTag(request.getTrafficStatsTag());
        }
    }

    @SuppressLint("NewApi")
    @Override
    public void run() {
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
        while (true) {
            long startTimeMs = SystemClock.elapsedRealtime();
            Request<?> request;
            Response<?> response = null;

            try {
                // Take a request from the queue.
                request = mQueue.take();
                Log.i(TAG, "The size of request Queue:" + functionName + " " +
                        mQueue.size());

            } catch (InterruptedException e) {
                // We may have been interrupted because it was time to quit.
                if (mQuit) {
                    return;
                }
                continue;
            }

            try {
                request.addMarker("network-queue-take");

                // If the request was cancelled already, do not perform the
                // network request.
                if (request.isCanceled()) {
                    request.finish("network-discard-cancelled");
                    continue;
                }

                addTrafficStatsTag(request);

                // TODO: Extracts only the domain name and drops everything else
                functionName = IPCUtil.getDomainName(request.getUrl());

                properties = new IPCUtil.Properties();

                HelperVirtDevService.lookup(functionName, serviceBinder,
                        properties, context,
                        WAIT_FOR_VIRT_SERVICE, false);

                Log.i(TAG, "Local vs Remote check " + functionName + " " +
                        !properties.isBypassFuncLocal() +
                        " " +
                        (properties.getLookupResponse() != null));

                // If IPC redirection is not set to be bypassed, and the local
                // function has been registered.
                if (!properties.isBypassFuncLocal()) {
                    //if local execution is not bypassed and received IPC info from
                    //virtDev service.

                    Log.i(TAG, "Request is converted to an IPC call " +
                            functionName);
                    Log.i(TAG, "Request headers: " + request.toString());

                    request.addMarker("ipc-path-chosen");
                    // convert HTTP url to IPC url.
                    //request.setUrl(IPCUtil.reverseDomain(functionName));

                    // Android IPC
                    mIPC = new AndroidIPC(properties.getLookupResponse());

                    response = request.parseIPCResponse(
                            mIPC.performRequest(request, functionName,
                                    properties.getFunctionInterface()));

//                    Log.i(TAG, "IPC Response received " +
//                            response.result.toString());

                    request.addMarker("ipc-parse-complete");
                } else {
                    Log.i(TAG, "Network path chosen! " + request.getUrl());

                    request.addMarker("network-path-chosen");

                    // If else perform network request
                    NetworkResponse networkResponse =
                            mNetwork.performRequest(request);

                    request.addMarker("network-http-complete");

                    // If the server returned 304 AND we delivered a response already,
                    // we're done -- don't deliver a second identical response.
                    if (networkResponse.notModified && request.hasHadResponseDelivered()) {
                        request.finish("not-modified");
                        Log.i(TAG, "Network response not modified");
                        continue;
                    }

                    // Parse the response here on the worker thread.
                    response = request.parseNetworkResponse(networkResponse);
                    request.addMarker("network-parse-complete");
                }

                // Write to cache if applicable.
                // TODO: Only update cache metadata instead of entire record for 304s.
                if (request.shouldCache() && response.cacheEntry != null) {
                    mCache.put(request.getCacheKey(), response.cacheEntry);
                    request.addMarker("network-cache-written");
                }

                // Post the response back.
                request.markDelivered();
                mDelivery.postResponse(request, response);

            } catch (VolleyError volleyError) {
                volleyError.setNetworkTimeMs(SystemClock.elapsedRealtime() - startTimeMs);
                parseAndDeliverNetworkError(request, volleyError);
            } catch (Exception e) {
                VolleyLog.e(e, "Unhandled exception %s", e.toString());
                VolleyError volleyError = new VolleyError(e);
                volleyError.setNetworkTimeMs(SystemClock.elapsedRealtime() - startTimeMs);
                mDelivery.postError(request, volleyError);
            }
        }
    }

    private void parseAndDeliverNetworkError(Request<?> request, VolleyError error) {
        error = request.parseNetworkError(error);
        mDelivery.postError(request, error);
    }

    /**
     * Sets the {@link IBinder} object received from virtdevservice to be
     * used for lookups
     *
     * @param serviceBinder {@link IBinder} instance.
     */
    public void setServiceBinder(IBinder serviceBinder) {
        this.serviceBinder = serviceBinder;
        Log.i(TAG + ".setServiceBinder()", "Service binder has been set = "
                + (serviceBinder != null));
    }
}
