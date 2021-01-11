package com.force.react.toolbox;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.RequiresApi;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.util.Pair;

import com.force.react.IPCUtil;
import com.force.react.VolleyIPCFunction;

import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

/**
 * @author Chathura Sarathchandra
 */

public final class HelperVirtDevService {
    /**
     * Intent used for starting com.
     */
    final static String startIntent = "com.force.action.start";

    private static String TAG = HelperVirtDevService.class.getName();
    /**
     * Intra-process function catalogue
     */
    private static HashMap<String, Pair<int[], VolleyIPCFunction>> functionCatalogue =
            new
                    HashMap<>();

    /**
     * Register a virtDev function
     *
     * @param context       Context of the registering application.
     * @param funcName      The name for the function to be registered against.
     * @param supportedIPCs Supported IPC mechanisms
     * @param functionClass
     * @param localReg      Register with local catalogue
     * @param remoteReg     Register with remote catalogue
     * @return Connection properties of type
     * {@link IPCUtil.Properties}
     * @throws RemoteException
     */
    public static IPCUtil.Properties registerFunction(final Context context, final
    String funcName, final int[] supportedIPCs, VolleyIPCFunction functionClass,
                                                      boolean
                                                              localReg, boolean
                                                              remoteReg) throws
            RemoteException {

        if (!localReg && !remoteReg) throw new AssertionError(TAG +
                ":registerFunction() both localReg and remoteReg fields can't" +
                " be 'false' ");

        /**
         * Target for VirtDev service to send messages back to the service
         */
        final IPCUtil.Properties serviceProperties =
                createServiceResponseMessenger();

        if (localReg) {
            functionCatalogue.put(funcName, new Pair<int[], VolleyIPCFunction>
                    (supportedIPCs, functionClass));
            serviceProperties.setRegisteredLocally(true);
        }

        Log.i(TAG, "Local catalogue status " + functionCatalogue);

        if (remoteReg) {

            /**
             * Service Connection used for connecting to the VirtDev Service.
             */
            ServiceConnection mConnection = new ServiceConnection() {
                Messenger mService = null;

                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    mService = new Messenger(service);
                    try {
                        doSendRegistrationMessage(funcName, supportedIPCs, mService,
                                serviceProperties.getReplyToMessenger());

                        doUnbindService(context, this);
                    } catch (RemoteException e) {
                        Log.e(TAG, e.toString());
                        e.printStackTrace();
                    }
                    Log.i(TAG, "Remote service connected!");
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    mService = null;
                    Log.i(TAG, "Service disconnected!");
                }
            };

//            doBindService(context, mConnection);
        }
        return serviceProperties;
    }

    /**
     * Creates a replyTO Messenger object for function registration.
     *
     * @return Connection
     * properties of type {{@link IPCUtil.Properties}}
     */
    private static IPCUtil.Properties createServiceResponseMessenger() {

        final IPCUtil.Properties vdServiceConnection = new IPCUtil.Properties();

        // work, and deregister only if .isRegistered() == true.
        Messenger messenger = new Messenger(new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case IPCUtil.MSG_REGISTER_SERVICE:
                        Log.i(TAG, "New service has been registered with the " +
                                "virtDev Service");
                        vdServiceConnection.setRegisteredRemotely(true);
                    default:
                        Log.e(TAG, "Unknown message type");
                }
            }
        });

        vdServiceConnection.setReplyToMessenger(messenger);
        return vdServiceConnection;
    }

    /**
     * Actually sends the registration message to the virtDevService.
     *
     * @param funcName         The name for the function to be registered against.
     * @param supportedIPCs    Supproted IPC mechanisms
     * @param mService         {@link IBinder} object returned from virtDevService
     * @param replyToMessenger The replyTo {@link Messenger} object
     * @throws RemoteException
     */
    private static void doSendRegistrationMessage(String funcName, int[]
            supportedIPCs, Messenger mService, Messenger replyToMessenger) throws RemoteException {
        Bundle bundle = new Bundle();

        //Request ID
        bundle.putString(IPCUtil.REQ_ID_MSG_REGISTER_SERVICE, IPCUtil
                .generateReqID());

        //Function address
        bundle.putString(IPCUtil.FUNCTION_ADDRESS_MSG_REGISTER_SERVICE,
                funcName);

        //available IPCs
        bundle.putIntArray(IPCUtil
                .IPC_AVAILABILITY_MSG_REGISTER_SERVICE, supportedIPCs);

        Message msg = Message.obtain(null, IPCUtil.MSG_REGISTER_SERVICE,
                bundle);

        msg.replyTo = replyToMessenger;
        mService.send(msg);
    }

    /**
     * Deregister a specific function from virtDevService.
     *
     * @param context  Context to be used for sending the deregistration
     *                 message with.
     * @param funcName The name of the function to be deregistered.
     * @throws RemoteException
     */
    public static void deregisterFunction(final Context context, final
    String
            funcName, boolean localDereg, boolean remoteDereg) {

        if (!localDereg && !remoteDereg) throw new AssertionError(TAG +
                ":registerFunction() both localReg and remoteReg fields can't" +
                " be 'false'");

        /**
         * Target for VirtDev service to send messages back to the service
         */
        final IPCUtil.Properties properties = createServiceResponseMessenger();

        if (localDereg) {
            functionCatalogue.remove(funcName);
            properties.setRegisteredLocally(false);
        }

        Log.i(TAG, "Local catalogue status " + functionCatalogue);

        if (remoteDereg) {

            /**
             * Service Connection used for connecting to the VirtDev Service.
             */
            final ServiceConnection mConnection = new ServiceConnection() {
                Messenger mService = null;

                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    mService = new Messenger(service);
                    try {
                        doSendDeRegistrationMessage(funcName, mService,
                                properties);
                    } catch (RemoteException e) {
                        Log.e(TAG, e.toString());
                        e.printStackTrace();
                    }
                    Log.i(TAG, "Remote service connected!");

                    doUnbindService(context, this);
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    mService = null;
                    Log.i(TAG, "Service disconnected!");
                }
            };

//            doBindService(context, mConnection);
        }
    }

    /**
     * Actually sends the deregistration message to virtDevService.
     *
     * @param funcName          The name for the function to be registered against.
     * @param mService          {@link IBinder} object returned from virtDevService
     * @param serviceProperties The replyTo {@link Messenger} object
     * @throws RemoteException
     */
    private static void doSendDeRegistrationMessage(String funcName,
                                                    Messenger mService,
                                                    IPCUtil.Properties serviceProperties) throws RemoteException {
        Bundle bundle = new Bundle();

        //Request ID
        bundle.putString(IPCUtil.REQ_ID_MSG_UNREGISTER_SERVICE,
                IPCUtil
                        .generateReqID());

        //Function address
        bundle.putString(IPCUtil
                        .FUNCTION_ADDRESS_MSG_UNREGISTER_SERVICE,
                funcName);

        Message msg = Message.obtain(null,
                IPCUtil.MSG_UNREGISTER_SERVICE, bundle);
        msg.replyTo = serviceProperties.getReplyToMessenger();
        mService.send(msg);

        // setting this here as the remote service does not acknowledge
        // deregistrations currently
        serviceProperties.setRegisteredRemotely(false);
    }


    /**
     * Start com.interdigital.force.virtdevservice that runs in a separate
     * process
     *
     * @param appContext Context to send the request from
     */
    public static void startVDService(Context appContext) {
        Intent req = new Intent(IPCUtil.VIRTDEV_SERVICE_BR_ADDR);
        req.setAction(startIntent);
        appContext.sendBroadcast(req);
    }

    /**
     * Stop com.interdigital.force.virtdevservice that runs in a separate
     * process
     *
     * @param appContext Context to send the request from
     */
    public static void stopVDService(Context appContext) {
        Intent req = new Intent(IPCUtil.VIRTDEV_SERVICE_BR_ADDR);
        req.setAction(startIntent);
        appContext.sendBroadcast(req);
    }

    /**
     * Establishes a new connection to the virtdevservice and retrieves an
     * IBinder object to be used by all following messages.
     *
     * @return A {@link IPCUtil.Properties} instance with {@link IBinder}
     * and {@link ServiceConnection} objects.
     */
    public static IPCUtil.Properties createConnection() {
        final IPCUtil.Properties virtDevServiceConnection =
                new IPCUtil.Properties();
        ServiceConnection mConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                virtDevServiceConnection.setServiceBinder(service);
                Log.i(TAG, "Service connected! Got a new IBinder object = " +
                        (virtDevServiceConnection.getServiceBinder() != null));
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                // cant clean the HelperVirtDevService object as its 'final'.
                virtDevServiceConnection.setServiceBinder(null);
                virtDevServiceConnection.setServiceConnection(null);

                Log.i(TAG, "Service disconnected!");
            }
        };

        virtDevServiceConnection.setServiceConnection(mConnection);

        return virtDevServiceConnection;
    }

    /**
     * Bind to virtdevservice.
     *
     * @param context     Context to be used for binding.
     * @param mConnection {@link ServiceConnection} instance.
     * @return Binding status
     */
    public static boolean doBindService(Context context, ServiceConnection
            mConnection) {

        Log.i(TAG, "Service Binding. ");
        Intent intent = new Intent();
        intent.setClassName(IPCUtil.FORCE_SERVICE_PACKAGE_NAME, IPCUtil.
                FORCE_SERVICE_FQCN);

        Boolean mIsBound = context.bindService(intent, mConnection,
                Context
                        .BIND_AUTO_CREATE);

        Log.i(TAG, "Service Bound. " + mIsBound);

        return mIsBound;
    }

    /**
     * Unbind from virtDevService.
     *
     * @param context     {@link Context} used in {#HelperVirtDevService
     *                    .doBindService}
     * @param mConnection {@link ServiceConnection} instance received from
     *                    {#HelperVirtDevService.createConnection}
     */
    public static void doUnbindService(Context context, ServiceConnection
            mConnection) {
        context.unbindService(mConnection);
//        mIsBound = false;
        Log.i(TAG, "Service Unbinding.");
    }

    /**
     * Registers a {@link BroadcastReceiver} for receiving the sendMessage
     * response - listening for the response addressed to the request ID.
     *
     * @param reqID                 ID used for the request
     * @param localBroadcastManager {@link LocalBroadcastManager} of the
     *                              application.
     * @param properties            {@link IPCUtil.Properties} object for
     *                              storing the response and keeping track
     *                              of the progress.
     */
    public static void registerBReceiver(final String reqID, LocalBroadcastManager
            localBroadcastManager, final IPCUtil.Properties properties) {
        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                Log.i(TAG, "Hello: reqID " + intent.getAction());

                switch (intent.getStringExtra(IPCUtil.METHOD_EXTRA_NAME)) {
                    case IPCUtil.IPC_GET_REPLY:
                        properties.setRequestResponse(intent.getByteArrayExtra(IPCUtil
                                .GET_REPLYBODY_EXTRA_NAME));

                        properties.setRequestResponseReceived(true);
                        // the following throws errors in case of null
                        // responses
//                        Log.i(TAG, "HTTP GET response " + response.toString());
                        break;
                }
                //}
            }
        };

        //register the broadcast receiver here.
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(reqID);
        localBroadcastManager.registerReceiver(broadcastReceiver, intentFilter);
    }

    /**
     * Looks up for local IPC functions from virtdevservice, using the
     * IBinder instance received from the application (returned by
     * virtDevService when binding).
     * <p>
     * Looks up from intra-process catalogue first, then from inter-process
     * one if the first lookup does not return a match.
     */
    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    public static void lookup(final String functionName,
                              IBinder
                                      serviceBinder, final IPCUtil.Properties properties, Context context, int
                                      WAIT_FOR_VIRT_SERVICE, boolean remoteLookup)
            throws
            ExecutionException, InterruptedException {

        Pair<int[], VolleyIPCFunction> result = functionCatalogue.get(IPCUtil
                .reverseDomain(functionName));

        // looking up the local catalogue
        int[] supportIPC = null;
        VolleyIPCFunction functionClass = null;

        if (result != null) {
            supportIPC = result.first;
            functionClass = result.second;
        }

        if (supportIPC != null) {
            Log.i(TAG, "Found in local catalogue " + functionName +
                    " " + functionCatalogue.toString());
            properties.setLookupResponse(supportIPC);
            properties.setBypassFuncLocal(false);
            properties.setFunctionInterface(functionClass);
        } else if (remoteLookup && serviceBinder != null) {
            Log.i(TAG, "Not found in local catalogue " + functionName +
                    " " + functionCatalogue.toString());
            Messenger mMessenger = new Messenger(new Handler(Looper.getMainLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    switch (msg.what) {
                        case IPCUtil.MSG_QUERY_SERVICE:
                            //TODO check if the message ID matches, therefore making
                            // sure if the response corresponds to the request.
                            int[] functionMeta = ((Bundle) msg.obj).getIntArray
                                    (IPCUtil.
                                            IPC_AVAILABILITY_MSG_QUERY_SERVICE);

                            properties.setLookupResponse(functionMeta);
                            properties.setBypassFuncLocal(false);
                            Log.i(TAG, "Service meta received for " + functionName +
                                    " " + Arrays
                                    .toString(functionMeta) + " Req ID " + ((Bundle)
                                    msg.obj).getString(IPCUtil.REQ_ID_MSG_QUERY_SERVICE));
                            break;
                        case IPCUtil.MSG_NO_REGISTERED_SERVICE:
                            Log.i(TAG, "The service does not exist locally");
                            properties.setBypassFuncLocal(true);
                            break;
                        default:
                            super.handleMessage(msg);
                    }
                }
            });

            Log.i(TAG, "Service binder set = " + (serviceBinder != null));
            Messenger mService = new Messenger(serviceBinder);

            try {

                Log.i(TAG, "Querying for function: " + functionName);
                Bundle bundle = new Bundle();

                //Request ID
                bundle.putString(IPCUtil.REQ_ID_MSG_QUERY_SERVICE, IPCUtil
                        .generateReqID());

                //Function address
                bundle.putString(IPCUtil.FUNCTION_ADDRESS_MSG_QUERY_SERVICE,
                        functionName);

                Message msg = Message.obtain(null, IPCUtil.MSG_QUERY_SERVICE,
                        bundle);

                msg.replyTo = mMessenger;
                mService.send(msg);

            } catch (RemoteException e) {
                Log.e(TAG, e.toString());
            }

            waitForVirtService(functionName, context,
                    WAIT_FOR_VIRT_SERVICE,
                    properties);
        } else {
//            if (serviceBinder == null) Log.e(TAG, "The service binder is not " +
//                    "set!");
            properties.setBypassFuncLocal(true);
        }
    }

    /**
     * Waits for the lookup response from virtDevService.
     *
     * @param waitForVirtS Waiting time
     * @param properties
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    private static void waitForVirtService(final String functionName, final Context
            context, final int waitForVirtS, final IPCUtil.Properties properties)
            throws ExecutionException, InterruptedException {
        // '.get()' blocks until the result
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                long cTime = System.currentTimeMillis();
                long endTime = cTime + waitForVirtS;
                Log.i(TAG, "Waiting for response");

                // does not wait if the service does not exist
                // The sendMessage service name should look as below
                // com.interdigital.force.virtdevservice.VirtDevService
                properties.setBypassFuncLocal(!IPCUtil
                        .isServiceRunning(IPCUtil
                                        .FORCE_SERVICE_PACKAGE_NAME + "." + IPCUtil.FORCE_SERVICE_NAME,
                                context));

                Log.i(TAG, "is virtDevService running for " + functionName
                        + " " + !properties.isBypassFuncLocal());

                while (!properties.isBypassFuncLocal() && properties.getLookupResponse() ==
                        null && System
                        .currentTimeMillis() <
                        endTime) {
                }

                // When the query times out, offload the request to the network.
                if (properties.getLookupResponse() == null) properties
                        .setBypassFuncLocal(true);
                Log.i(TAG, "Waiting for response - Done " + functionName + " " +
                        properties.getLookupResponse() + " " + properties.isBypassFuncLocal());
                return null;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR).get();
    }
}
