package com.force.react;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.force.react.toolbox.HelperVirtDevService;

/**
 * <p>
 * Provides an API, with which IPC enabled functions may communicate on
 * devices. All classes that handle IPC protocol calls must extend this class.
 * Each object of type VolleyIPCFunction may act as an API endpoint for only one
 * type of IPC. This class supports method chaining.
 * <p>
 * Usage:
 * {@code}
 * new VolleyTest().setLocalBroadcastManager(this)
 * .setDomainName("com.force.service")
 * .setMode(VolleyIPCFunction.IPC_MODE_LOCALBROADCAST)
 * .start();
 * {@code}
 * Where:
 * - VolleyTest instanceof VolleyIPCFunction
 * - 'this' is the application context.
 * - "com.force.service" is the service URL.
 * - VolleyIPCFunction.IPC_MODE_LOCALBROADCAST is the IPC type of
 * the object.
 * <p>
 * This abstract method includes some implementations of common functions
 * such as setMode() and setDomainName().
 * <p>
 * The request handler methods are overloaded based on the type of IPC method.
 * <p>
 *
 * @author Chathura Sarathchandra
 */

public abstract class VolleyIPCFunction extends Thread {

    protected String TAG = VolleyIPCFunction.class.getName();

    /**
     * The URL of the service.
     */
    protected String FORCECOM_FUNCTION_NAME;
    /**
     * The set IPC mode.
     */
    protected int ipcMode;

    /**
     * LocalBroadcastManager for local broadcasts
     * - used only when ipcmode = VolleyIPCFunction.IPC_MODE_LOCALBROADCAST
     */
    protected LocalBroadcastManager broadcastManager;
    /**
     * The supported IPCs within the function
     */
    protected int[] supportedIPCs;

    /**
     * The application context
     */
    protected Context context;
    /**
     * virtDev function registration properties
     */
    private IPCUtil.Properties registrationProperties;
    private BroadcastReceiver broadcastReceiver;
    private VolleyIPCFunction volleyInterface;
    private boolean isBRRegistered = false;
    private Object response;

    /**
     * The constructor
     *
     * @param applicationContext the application context for binding to
     *                           VirtDev Service.
     */
    public VolleyIPCFunction(Context applicationContext) {
        this.context = applicationContext;
    }

    @Override
    public void run() {
        super.run();

        if (FORCECOM_FUNCTION_NAME == null || FORCECOM_FUNCTION_NAME.isEmpty()) {
            Log.e(TAG, "The domain name has not been initialized!");
            throw new RuntimeException("The domain name has not been initialized!");
        }

        if (volleyInterface == null) {
            Log.e(TAG, "The Volley interface object has not been set!");
            throw new RuntimeException("The class object has not been set!");
        }

        registerFunction();

        //Currently supports only one type of IPC method per function
        switch (supportedIPCs[0]) {
            case IPCUtil.FORCE_ANDROID_IPC_TYPE_HEAP:
                Log.i(TAG, "Setting function mode to Heap");
                break;
            case IPCUtil.FORCE_ANDROID_IPC_TYPE_LBHEAP:
                if (broadcastManager == null) {
                    Log.e(TAG, "The broadcast manager has " +
                            "not been initialized!");
                    throw new RuntimeException("The broadcast manager has " +
                            "not been initialized!");
                }
                registerBReceiver();
                break;
            case IPCUtil.FORCE_ANDROID_IPC_TYPE_LBROADCAST:
                if (broadcastManager == null) {
                    Log.e(TAG, "The broadcast manager has " +
                            "not been initialized!");
                    throw new RuntimeException("The broadcast manager has " +
                            "not been initialized!");
                }
                registerBReceiver();
                break;
            case IPCUtil.FORCE_ANDROID_IPC_TYPE_BINDER:
                break;
            case IPCUtil.FORCE_ANDROID_IPC_TYPE_MESSENGER:
                break;
            case IPCUtil.FORCE_ANDROID_IPC_TYPE_AIDL:
                break;
            default:
        }
    }

    /**
     * Handles incoming broadcast intents based on the request type
     * - used only when ipcmode = VolleyIPCFunction.IPC_MODE_LOCALBROADCAST
     */
    final private void registerBReceiver() {
        broadcastReceiver = new BroadcastReceiver() {
            @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
            @Override
            public void onReceive(Context context, Intent intent) {

                Log.i(TAG, "IPC request received!!!!!!!!!!!!!!--" +
                        intent.getStringExtra(IPCUtil.METHOD_EXTRA_NAME) + " "
                        + IPCUtil.IPC_GET + " "
                        + intent.getStringExtra(IPCUtil.METHOD_EXTRA_NAME)
                        .equals(IPCUtil.IPC_GET));

                switch (intent.getStringExtra(IPCUtil.METHOD_EXTRA_NAME)) {
                    case IPCUtil.IPC_GET:
                        Log.i(TAG, "HTTP get message received");
//                        AsyncTask<Void, Void, Void> responseGET = handleGETreq(intent);
//                        if (responseGET != null) responseGET
//                                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        Runnable taskGET = handleGETreq(intent, null);
                        if (taskGET != null) new Thread(taskGET).start();
                        break;
                    case IPCUtil.IPC_POST:
//                        AsyncTask<Void, Void, Void> responsePOST = handlePOSTreq(intent);
//                        if (responsePOST != null) responsePOST.executeOnExecutor
//                                (AsyncTask.THREAD_POOL_EXECUTOR);
                        Runnable taskPOST = handlePOSTreq(intent);
                        if (taskPOST != null) new Thread(taskPOST).start();
                        break;
                    case IPCUtil.IPC_PUT:
//                        AsyncTask<Void, Void, Void> responsePUT = handlePUTreq(intent);
//                        if (responsePUT != null) responsePUT.executeOnExecutor
//                                (AsyncTask.THREAD_POOL_EXECUTOR);
                        Runnable taskPUT = handlePUTreq(intent);
                        if (taskPUT != null) new Thread(taskPUT).start();
                        break;
                    case IPCUtil.IPC_DELETE:
//                        AsyncTask<Void, Void, Void> responseDEL = handleDELETEreq(intent);
//                        if (responseDEL != null) responseDEL.executeOnExecutor
//                                (AsyncTask.THREAD_POOL_EXECUTOR);
                        Runnable taskDELETE = handleDELETEreq(intent);
                        if (taskDELETE != null) new Thread(taskDELETE).start();
                        break;
                    case IPCUtil.IPC_HEAD:
//                        AsyncTask<Void, Void, Void> responseHEAD = handleHEADreq(intent);
//                        if (responseHEAD != null) responseHEAD.executeOnExecutor
//                                (AsyncTask.THREAD_POOL_EXECUTOR);
                        Runnable taskHEAD = handleHEADreq(intent);
                        if (taskHEAD != null) new Thread(taskHEAD).start();
                        break;
                    case IPCUtil.IPC_OPTIONS:
//                        AsyncTask<Void, Void, Void> responseOPTIONS = handleOPTIONSreq(intent);
//                        if (responseOPTIONS != null)
//                            responseOPTIONS.executeOnExecutor
//                                    (AsyncTask.THREAD_POOL_EXECUTOR);
                        Runnable taskOPTIONS = handleOPTIONSreq(intent);
                        if (taskOPTIONS != null)
                            new Thread(taskOPTIONS).start();
                        break;
                    case IPCUtil.IPC_TRACE:
//                        AsyncTask<Void, Void, Void> responseTRACE = handleTRACEreq(intent);
//                        if (responseTRACE != null)
//                            responseTRACE.executeOnExecutor
//                                    (AsyncTask.THREAD_POOL_EXECUTOR);
                        Runnable taskTRACE = handleTRACEreq(intent);
                        if (taskTRACE != null) new Thread(taskTRACE).start();
                        break;
                    case IPCUtil.IPC_PATCH:
//                        AsyncTask<Void, Void, Void> responsePATCH = handlePATCHreq(intent);
//                        if (responsePATCH != null)
//                            responsePATCH.executeOnExecutor
//                                    (AsyncTask.THREAD_POOL_EXECUTOR);
                        Runnable taskPATCH = handlePATCHreq(intent);
                        if (taskPATCH != null) new Thread(taskPATCH).start();
                        break;
                    default:
//                        AsyncTask<Void, Void, Void> responseDEFAULT = handleUnknownreq(intent);
//                        if (responseDEFAULT != null)
//                            responseDEFAULT.executeOnExecutor
//                                    (AsyncTask.THREAD_POOL_EXECUTOR);
                        Runnable taskUnknown = handleUnknownreq(intent);
                        if (taskUnknown != null)
                            new Thread(taskUnknown).start();
                }
            }
        };

        //register the broadcast receiver here.
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(this.FORCECOM_FUNCTION_NAME);
        broadcastManager.registerReceiver(broadcastReceiver, intentFilter);

        isBRRegistered = true;
    }

    /**
     * Returns the response of synchronous callback requests
     *
     * @return response object
     */
    public abstract Object getResponse();

    /**
     * Construct a {@link Runnable} with procedures to handle request types that
     * are not supported by the service.
     *
     * @param intent Incoming request intent
     * @return Thread for handling request
     */
    public abstract Runnable handleUnknownreq(Intent intent);

    /**
     * Construct a {@link Runnable} with procedures to handle PATCH request
     *
     * @param intent Incoming request intent
     * @return Thread for handling request
     */
    public abstract Runnable handlePATCHreq(Intent intent);

    /**
     * Construct a {@link Runnable} with procedures to handle TRACE request
     *
     * @param intent Incoming request intent
     * @return Thread for handling request
     */
    public abstract Runnable handleTRACEreq(Intent intent);

    /**
     * Construct a {@link Runnable} with procedures to handle OPTIONS request
     *
     * @param intent Incoming request intent
     * @return Thread for handling request
     */
    public abstract Runnable handleOPTIONSreq(Intent intent);

    /**
     * Construct a {@link Runnable} with procedures to handle HEAD request
     *
     * @param intent Incoming request intent
     * @return Thread for handling request
     */
    public abstract Runnable handleHEADreq(Intent intent);

    /**
     * Construct a {@link Runnable} with procedures to handle DELETE request
     *
     * @param intent Incoming request intent
     * @return Thread for handling request
     */
    public abstract Runnable handleDELETEreq(Intent intent);

    /**
     * Construct a {@link Runnable} with procedures to handle PUT request
     *
     * @param intent Incoming request intent
     * @return Thread for handling request
     */
    public abstract Runnable handlePUTreq(Intent intent);

    /**
     * Construct a {@link Runnable} with procedures to handle POST request
     *
     * @param intent Incoming request intent
     * @return Thread for handling request
     */
    public abstract Runnable handlePOSTreq(Intent intent);

    /**
     * Construct a {@link Runnable} with procedures to handle GET request
     *
     * @param intent  Incoming request intent
     * @param request
     * @return Thread for handling request
     */
    public abstract Runnable handleGETreq(Intent intent, Request<?> request);


    /**
     * Get the LocalBroadcastManager instance to be used with local broadcasts.
     *
     * @param context The application context
     * @return The VolleyIPCFunction object
     */
    public final synchronized VolleyIPCFunction setLocalBroadcastManager(Context context) {
        broadcastManager = LocalBroadcastManager.getInstance(context);
        Log.i(TAG, "Broadcast manager has been set");
        return this;
    }

    /**
     * Sets the domain name of the service
     *
     * @param serviceName The domain name of the IPC service
     * @return The VolleyIPCFunction object
     */
    public final synchronized VolleyIPCFunction setDomainName(String
                                                                      serviceName) {
        FORCECOM_FUNCTION_NAME = serviceName;
        Log.i(TAG, "Domain name has been set");
        return this;
    }

    /**
     * Registers the function with the virtDev service.
     */
    private void registerFunction() {
        try {
            registrationProperties =
                    HelperVirtDevService
                            .registerFunction(context,
                                    FORCECOM_FUNCTION_NAME,
                                    supportedIPCs, volleyInterface, true, true);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            e.printStackTrace();
        }
    }

    /**
     * Unregisters the function with virtDev service.
     */
    private void deRegisterFunction() {

//        Log.i(TAG, "The service was registered =" + registrationProperties
//                .isRegistered());

        //TODO: unregister only if the function has been registered

        HelperVirtDevService.deregisterFunction
                (context, FORCECOM_FUNCTION_NAME, true, true);
    }

    /**
     * Sets the supported list of IPCs,
     *
     * @param ipcs an int[] with supported IPC methods with highest priority
     *             first
     */
    protected final synchronized void setSupportedIPCs(int[] ipcs) {
        supportedIPCs = ipcs;
    }

    protected final synchronized void setVolleyInterface(VolleyIPCFunction
                                                                 vInterface) {
        this.volleyInterface = vInterface;
    }

    /**
     * Unregister from VirtDev service
     *
     * @throws Throwable the {@code Exception} raised by this method
     */
    @Override
    protected void finalize() throws Throwable {
        super.finalize();
    }

    /**
     * Unregisters the function from the virtDev service
     */
    public void stopFunction() {
        Log.i(TAG, "Stopping function!");
        deRegisterFunction();

        if (isBRRegistered)
            broadcastManager.unregisterReceiver(broadcastReceiver);

        broadcastReceiver = null;
        broadcastManager = null;
        try {
            finalize();
        } catch (Throwable throwable) {
            Log.e(TAG, throwable.toString());
            throwable.printStackTrace();
        }
    }
}
