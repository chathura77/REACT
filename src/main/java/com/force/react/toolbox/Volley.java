/*
 * Copyright (C) 2012 The Android Open Source Project
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

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.http.AndroidHttpClient;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import com.force.react.Network;
import com.force.react.RequestQueue;
import com.force.react.VolleyError;

import java.io.File;

public class Volley {

    private static final String TAG = Volley.class.getName();

    /**
     * Default on-disk cache directory.
     */
    private static final String DEFAULT_CACHE_DIR = "react";

    /**
     * Creates a default instance of the worker pool and calls {@link RequestQueue#start()} on it.
     *
     * @param context       A {@link Context} to use for creating the cache dir.
     * @param stack         An {@link HttpStack} to use for the network, or null for default.
     * @param serviceBinder The {@link IBinder} that received from
     *                      virtDevService.
     * @return A started {@link RequestQueue} instance.
     */
    public static RequestQueue newRequestQueue(Context context, HttpStack
            stack, IBinder serviceBinder) {
        File cacheDir = new File(context.getCacheDir(), DEFAULT_CACHE_DIR);

        String userAgent = "react/0";
        try {
            String packageName = context.getPackageName();
            PackageInfo info = context.getPackageManager().getPackageInfo(packageName, 0);
            userAgent = packageName + "/" + info.versionCode;
        } catch (NameNotFoundException e) {
        }

        if (stack == null) {
            if (Build.VERSION.SDK_INT >= 9) {
                stack = new HurlStack();
            } else {
                // Prior to Gingerbread, HttpUrlConnection was unreliable.
                // See: http://android-developers.blogspot.com/2011/09/androids-http-clients.html
                stack = new HttpClientStack(AndroidHttpClient.newInstance(userAgent));
            }
        }

        Network network = new BasicNetwork(stack);

        RequestQueue queue = new RequestQueue(new DiskBasedCache(cacheDir), network);

        Log.i(TAG + "newRequestQueue()", "Service Binder has been set = " +
                (serviceBinder != null));

        // set IBinder for IPC
        if (serviceBinder != null) queue.setServiceBinder(serviceBinder);

        queue.start();

        return queue;
    }

    /**
     * Creates a default instance of the worker pool and calls
     * {@link RequestQueue#start()} on it.
     *
     * @param context A {@link Context} to use for creating the cache dir.
     * @return A started {@link RequestQueue} instance.
     */
    public static RequestQueue newRequestQueue(Context context) {
        return newRequestQueue(context, null, null);
    }

    /**
     * Use this constructor for IPC support.
     *
     * Creates a default instance of the worker pool with an IBinder
     * instance that was returned from virtDevService for IPC, and
     * calls {@link RequestQueue#start()} on it.
     *
     * @param context A {@link Context} to use for creating the cache dir.
     * @return A started {@link RequestQueue} instance.
     */
    public static RequestQueue newRequestQueue(Context context, IBinder
            serviceBinder) throws VolleyError {

        if (serviceBinder==null) throw new VolleyError("The IBinder object " +
                "cannot be Null");

        return newRequestQueue(context, null, serviceBinder);
    }
}
