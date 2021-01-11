package com.force.react;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.os.Messenger;
import android.support.annotation.RequiresApi;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.force.react.toolbox.HelperVirtDevService;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Contains IPC utility methods and constants
 * <p>
 * The constants in this class are categorised into KEY and VALUE groups.
 * Key -
 * <p>
 * @author Chathura Sarathchandra
 */

public final class IPCUtil {

    /**
     * The address of the com.interdigital.force.virtdevservice.VirtDevBR
     */
    public final static String VIRTDEV_SERVICE_BR_ADDR = "com.interdigital" +
            ".force.virtdevservice/com.interdigital.force.virtdevservice.VirtDevBR";
    /**
     * <p>
     * These IPC types should be set per each function based on priority. The
     * data structure of availalble IPCs (int[]) of each function is sorted
     * based on priority.
     */
    public final static int FORCE_ANDROID_IPC_TYPE_LBROADCAST = 1;
    public final static int FORCE_ANDROID_IPC_TYPE_AIDL = 2;
    public final static int FORCE_ANDROID_IPC_TYPE_MESSENGER = 3;
    public final static int FORCE_ANDROID_IPC_TYPE_BINDER = 4;
    public final static int FORCE_ANDROID_IPC_TYPE_LBHEAP = 5;
    public static final int FORCE_ANDROID_IPC_TYPE_HEAP = 6;

    /**
     * Name of request method
     */
    public final static String METHOD_EXTRA_NAME = "method";
    /**
     * Values of METHOD_EXTRA_NAME
     */
    public final static String IPC_POST = "http.ipc.post";
    /******************************************
     *      Data INTENT - Request/Response
     *****************************************/
    public final static String IPC_GET = "http.ipc.get";
    public final static String IPC_PUT = "http.ipc.put";
    public final static String IPC_DELETE = "http.ipc.delete";
    public final static String IPC_PATCH = "http.ipc.patch";
    public static final String IPC_HEAD = "http.ipc.head";
    public static final String IPC_OPTIONS = "http.ipc.options";
    public static final String IPC_TRACE = "http.ipc.trace";
    public final static String IPC_GET_REPLY = "http.ipc.get_reply";
    public final static String HTTP_SERVICE = "com.force.react.toolbox" +
            ".LocalBroadcastStack";
    /**
     * Name of request ID intent extra - random string created at runtime.
     */
    public final static String REQ_ID_EXTRA_NAME = "reqid";
    /**
     * Name of the complete URL, of the request
     */
    public final static String FUNCURL_EXTRA_NAME = "urlComplete";
    /**
     * Name of POST request body
     */
    public final static String POSTBODY_EXTRA_NAME = "post_body";
    /**
     * Name of GET request body
     */
    public final static String GET_REPLYBODY_EXTRA_NAME = "get_replybody";
    /******************************************
     *       Data INTENT - Request/Response
     *****************************************/

    /******************************************
     *       Data INTENT - Request
     *****************************************/
    /**
     * The random request ID assigned for each virtdev requests.
     */
    public static final String REQ_ID_MSG_QUERY_SERVICE = "com.force" +
            ".service.msgattrib.query_reqid";
    /******************************************
     *        Data INTENT - Response
     *****************************************/
    public static final String REQ_ID_MSG_REGISTER_SERVICE = "com.force" +
            ".service.msgattrib.register_service_reqid";
    /******************************************
     *        Control INTENT - Request
     *****************************************/
    public static final String REQ_ID_MSG_UNREGISTER_SERVICE = "com.force" +
            ".service.msgattrib.unregister_service_reqid";
    /******************************************
     *        Data INTENT - Request
     *****************************************/
    /**
     * The bundle key for function address, used when querying for
     * services.
     */
    public static final String FUNCTION_ADDRESS_MSG_QUERY_SERVICE = "com.force" +
            ".service.msgattrib.func.query_function_address";
    /******************************************
     *        Data INTENT - Response
     *****************************************/
    /**
     * The bundle key for function address, used when registering services
     */
    public static final String FUNCTION_ADDRESS_MSG_REGISTER_SERVICE = "com" +
            ".force" +
            ".service.msgattrib.func.register_service_address";
    /**
     * The bundle key for function address, used when unregistering services
     */
    public static final String FUNCTION_ADDRESS_MSG_UNREGISTER_SERVICE = "com" +
            ".force" +
            ".service.msgattrib.func.unregister_service_address";
    /**
     * The key for the data structure with supported IPCs of the requested
     * function.
     */
    public static final String IPC_AVAILABILITY_MSG_REGISTER_SERVICE = "com" +
            ".force" +
            ".service.msgattrib.func.register_service_availableipcs";
    /**
     * Command for the service to register, receiving callbacks from
     * other service. The message's replyTo field must be a messenger of the
     * client where callbacks should be sent.
     */
    public static final int MSG_REGISTER_SERVICE = 1;
    /**
     * Command to unregister a service, to stop receiving
     * callbacks from the service. The message's replyTo field must be a
     * Messenger of the client as previously given with MSG_REGISTER_SERVICE.
     */
    public static final int MSG_UNREGISTER_SERVICE = 2;
    /**
     * Command to sendMessage services. This message can be sent by any service.
     */
    public static final int MSG_QUERY_SERVICE = 3;
    /**
     * The package name of the FORCE local service. This is used for binding
     * to the service
     */
    public static final String FORCE_SERVICE_PACKAGE_NAME = "com.interdigital" +
            ".force" +
            ".virtdevservice";
    /**
     * The fully qualified class name of the virtdev service class for sending
     * the explicit intent to.
     */
    public static final String FORCE_SERVICE_FQCN =
            FORCE_SERVICE_PACKAGE_NAME + "" +
                    ".VirtDevService";
    /**
     * The key for the data structure with supported IPCs of the requested
     * function.
     */
    public static final String IPC_AVAILABILITY_MSG_QUERY_SERVICE = "com.force" +
            ".service.msgattrib.func.query_availableipcs";
    /**
     * Received if the query does not return any services.
     */
    public static final int MSG_NO_REGISTERED_SERVICE = 0;
    /**
     * The name of the virtdev service. Currently only needed for providing
     * as input into  {@link #isServiceRunning(String, Context)}
     */
    public static final String FORCE_SERVICE_NAME = "VirtDevService";
    private static final String TAG = IPCUtil.class.getName();

    /**
     * Log: number of requests
     */
    static int nRequests = 0;

    /******************************************
     *        Control INTENT - Request
     *****************************************/

    /******************************************
     *        Control INTENT - Response
     *****************************************/
    // This class cannot be instantiated.
    private IPCUtil() {
    }

    /***************************************************************************
     *                                 INTENT
     *
     * Converted LocalBroadcast IPC packet format
     *
     *      Request                       Response
     *  +------------------+        +---------------------------+
     *  | Function Address |        | Function Address -> reqId |
     *  +------------------+        +---------------------------+
     *  | reqid            |        | method                    |
     *  +------------------+        +---------------------------+
     *  | method           |        | post_body                 |
     *  +------------------+        +---------------------------+
     *  | complete_url     |
     *  +------------------+
     *  | post_body        |
     *  +------------------+
     *
     **************************************************************************/

    //testing

    /**
     * Checks if an activity/service is running.
     *
     * @param serviceClass    the class to look up.
     * @param queryingContext the context to search from
     * @return true if running, otherwise false.
     */
    public static boolean isServiceRunning(Class<?> serviceClass, Context
            queryingContext) {
        try {
            ActivityManager manager = (ActivityManager) queryingContext.getSystemService(Context.ACTIVITY_SERVICE);
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if (serviceClass.getName().equals(service.service.getClassName()))
                    return true;
            }
        } catch (Exception ex) {
            Log.e(queryingContext.getClass().getName(), "Error checking service " +
                    "status" + ex
                    .toString());
        }
        return false;
    }

    //testing

    /**
     * Checks if an activity/service is running.
     *
     * @param serviceClassName name of the class to look up.
     * @param queryingContext  the context to search from
     * @return true if running, otherwise false.
     */
    public static boolean isServiceRunning(String serviceClassName, Context
            queryingContext) {
        try {
            ActivityManager manager = (ActivityManager) queryingContext.getSystemService(Context.ACTIVITY_SERVICE);
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
//                Log.i("IPCUtil", "comparing " + serviceClassName + " with"
//                        + service.service.getClassName());
                if (serviceClassName.equals(service.service.getClassName()))
                    return true;
            }
        } catch (Exception ex) {
            Log.e(queryingContext.getClass().getName(), "Error checking service " +
                    "status" + ex
                    .toString());
        }
        return false;
    }

    /**
     * Get the instance of LocalBroadcastManager of the local application.
     *
     * @param TAG The debug TAG of the application class.
     * @return LocalBroadcastManager - instance of the local application.
     */
    public static LocalBroadcastManager getLocalBroadcastManager(String TAG) {
        LocalBroadcastManager broadcastManager = null;
        try {
            broadcastManager = LocalBroadcastManager.getInstance((Application)
                    Class.forName
                            ("android.app.AppGlobals")
                            .getMethod("getInitialApplication")
                            .invoke(null, (Object[]) null));
        } catch (IllegalAccessException e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
        }
        return broadcastManager;
    }

    /**
     * Get the context of the application that the library imported into.
     *
     * @return Context - the context of the local application.
     */
    public static Context getLocalContext() {
        Context context = null;
        try {
            context = (Application) Class.forName
                    ("android.app.AppGlobals")
                    .getMethod("getInitialApplication")
                    .invoke(null, (Object[]) null);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return context;
    }

    /**
     * Choose 130 bits from a cryptographically secure random
     * bit generator, and encoding them in base-32.
     * <p>
     * Complies with:
     * RFC 1750: Randomness Recommendations for Security.
     * FIPS 140-2, Security Requirements for Cryptographic Modules, section
     * 4.9.1 tests
     */
    public static String generateReqID() {
        return new BigInteger(130, new SecureRandom()).toString(32);
    }

    /***
     * Maps the domain names of web service functions to the domain names of
     * IPC functions (reversed internet domain names).
     *
     * @param domain    Domain name to be reversed
     * @return Reversed domain name
     */
    public static String reverseDomain(final String domain) {
        final List<String> components = Arrays.asList(domain.split("\\."));
        Collections.reverse(components);
        return TextUtils.join(".", components);
    }

    /**
     * Extracts the domain name out from a URL
     *
     * @param url Complete URL
     * @return FQDN extracted from the provided URL
     * @throws URISyntaxException
     */
    public static String getDomainName(String url) throws URISyntaxException {
        URI uri = new URI(url);
        String domain = uri.getHost();
        return domain.startsWith("www.") ? domain.substring(4) : domain;
    }

    /**
     * TODO: check if this is a redundant method
     * Reply to LocalBroadcastMessage at the Function
     *
     * @param broadcastManager the corresponding instance of
     *                         LocalBroadcastManager
     * @param reqID            the reqid field of the request - copied
     * @param messageBody      the message body, if any
     */
    public static void replyLBroadcastMessage(LocalBroadcastManager
                                                      broadcastManager, String reqID,
                                              byte[] messageBody) {
        //Uses the request ID as the action, instead of the destination address.
        Intent iVidIntent = new Intent(reqID);
        //iVidIntent.putExtra(IPCUtil.REQ_ID_EXTRA_NAME, reqID);
        iVidIntent.putExtra(IPCUtil.METHOD_EXTRA_NAME,
                IPCUtil.IPC_GET_REPLY);
        iVidIntent.putExtra(IPCUtil.GET_REPLYBODY_EXTRA_NAME,
                messageBody);
        broadcastManager.sendBroadcast(iVidIntent);
    }

    /**
     * Broadcast Http request as a LocalBroadcast message.
     *
     * @param localBroadcastManager the corresponding instance of
     *                              LocalBroadcastManager
     * @param functionName          The name of the function
     * @param ipcUrl                The address of the function
     * @param method                Message method
     * @param messageBody           Message body
     * @param reqID                 Randomly generated request ID.
     */
    public static void broadcastRequest(LocalBroadcastManager
                                                localBroadcastManager,
                                        String functionName, String ipcUrl,
                                        String method,
                                        byte[] messageBody,
                                        String reqID) {

        Intent iVidIntent = new Intent(IPCUtil.reverseDomain(functionName));
        iVidIntent.putExtra(IPCUtil.REQ_ID_EXTRA_NAME, reqID);
        iVidIntent.putExtra(IPCUtil.METHOD_EXTRA_NAME, method);
        iVidIntent.putExtra(IPCUtil.FUNCURL_EXTRA_NAME, ipcUrl);
        iVidIntent.putExtra(IPCUtil.POSTBODY_EXTRA_NAME, messageBody);
        //TODO: use OrderedBroadcast here to limit the delivery to one receiver.
        localBroadcastManager.sendBroadcast(iVidIntent);
    }

    /***************************************************************************
     *                                 INTENT
     **************************************************************************/

    /**
     * Convert byte lenths/counts/sizes into a human readable format
     *
     * @param bytes byte number
     * @param si    true if SI unit, false if binary units
     * @return formateed human readable count as a {@link String}
     */
    public static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    /**
     * Send virtDev message to a function
     *
     * @param reqID                 Request ID.
     * @param functionName          Name of the function
     * @param request               {@link Request} object
     * @param ipcmode               IPC request method, e.g., IPCUtil.IPC_GET
     * @param localBroadcastManager {@link LocalBroadcastManager} of the
     *                              application.
     * @param properties            {@link IPCUtil.Properties} object for
     *                              storing the response and keeping track
     *                              of the progress.
     */
    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    public static void sendMessage(String reqID, String functionName, Request<?>
            request, String ipcmode, LocalBroadcastManager
                                           localBroadcastManager, IPCUtil.Properties properties) throws AuthFailureError {

        HelperVirtDevService.registerBReceiver(reqID,
                localBroadcastManager, properties);
        IPCUtil.broadcastRequest(localBroadcastManager,
                functionName, request.getUrl(),
                ipcmode, request.getPostBody(), reqID);

        Log.i(TAG, "Request timeout " + request.getTimeoutMs());
        try {
            waitForResponse(request
                    .getTimeoutMs(), properties);
        } catch (ExecutionException e) {
            Log.e(TAG, e.toString());
            e.printStackTrace();
        } catch (InterruptedException e) {
            Log.e(TAG, e.toString());
            e.printStackTrace();
        }

        // log the current request number and the amount of memory blocks
        // created
//        log(new SimpleDateFormat("dd/MM/yy-HH:mm:ss").format(new Date
//                (System.currentTimeMillis())) + ", " + nRequests++ + ", " +
//                HeapFactory.getInstance().getNBlocks());
    }

    /**
     * Log
     *
     * @param text text to be logged
     */
    public static void log(String text) {
        File log = new File("sdcard/log.txt");

//        Log.i(TAG, "Log: File exists == " + log.exists());

        try {
            if (!log.exists()) log.createNewFile();

            BufferedWriter writer = new BufferedWriter(new FileWriter(log, true));

            writer.append(text);
            writer.newLine();
            writer.close();
        } catch (IOException e) {
            Log.e(TAG, e.toString());
            e.printStackTrace();
        }
    }

    /**
     * Log
     *
     * @param text text to be logged
     */
    public static void log(String text, File log) {
//        File log = new File("sdcard/log.txt");

//        Log.i(TAG, "Log: File exists == " + log.exists());

        try {
            if (!log.exists()) log.createNewFile();

            BufferedWriter writer = new BufferedWriter(new FileWriter(log, true));

            writer.append(text);
            writer.newLine();
            writer.close();
        } catch (IOException e) {
            Log.e(TAG, e.toString());
            e.printStackTrace();
        }
    }

    /**
     * Waits until sendMessage response is received
     *
     * @param waitForResponse timeout
     */
    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    public static void waitForResponse(final long waitForResponse,
                                       final IPCUtil.Properties properties)
            throws
            ExecutionException, InterruptedException {

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                // Wait 5s for the response
                // TODO: This blocks the thread AsyncTask?
                long cTime = System.currentTimeMillis();
                long endTime = cTime + waitForResponse;

                Log.i(TAG, "Waiting for response");
                while (!properties.isRequestResponseReceived() && System
                        .currentTimeMillis() <
                        endTime) {
                }
                // the following throws errors in case of null
                // responses
                Log.i(TAG, "Timed out Waiting for response = " +
                        !properties.isRequestResponseReceived());
                return null;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR).get();
    }

    /**
     * Used for storing related objects, and keeping states of one
     * virtDevService connectivity.
     */
    public static final class Properties {

        /**
         * Generic
         */
        public volatile boolean binderSet = false;
        private IBinder serviceBinder = null;
        private ServiceConnection serviceConnection = null;
        private Messenger replyToMessenger = null;

        /**
         * Function registration
         */
        private boolean registered = false;
        private boolean registeredLocally = false;
        private boolean registeredRemotely = true;


        /**
         * Requests
         */
        private byte[] requestResponse = null;
        private boolean requestResponseReceived = false;

        /**
         * Lookup
         */
        private int[] lookupResponse = null;

        /**
         * Setting this to true if a local function exists. Defaulting to
         * offloading, and setting to false when the function is available
         * locally.
         */
        private boolean bypassFuncLocal = true;

        /**
         * The class object of the local function is set, if it exists locally.
         */
        private VolleyIPCFunction functionInterface = null;

        /**
         * Not possible to assign values during instantiation.
         */
        public Properties() {
        }

        /**
         * Get stored {@link ServiceConnection} object.
         */
        public ServiceConnection getServiceConnection() {
            return serviceConnection;
        }

        /**
         * Store a {@link ServiceConnection} object
         */
        public void setServiceConnection(ServiceConnection connection) {
            this.serviceConnection = connection;
        }

        /**
         * Get stored {@link IBinder} object
         */
        public IBinder getServiceBinder() {
            return serviceBinder;
        }

        /**
         * Store an {@link IBinder} object
         */
        public void setServiceBinder(IBinder binder) {
            this.serviceBinder = binder;
            binderSet = true;
        }

        /**
         * Get stored replyTO {@link Messenger} object
         */
        public Messenger getReplyToMessenger() {
            return replyToMessenger;
        }

        /**
         * Store replyTO {@link Messenger} object
         */
        public void setReplyToMessenger(Messenger replyToMessenger) {
            this.replyToMessenger = replyToMessenger;
        }

        /**
         * Get stored IPC message respnose
         *
         * @return response as a byte[]
         */
        public byte[] getRequestResponse() {
            return requestResponse;
        }

        /**
         * Store an IPC response of type byte[]
         */
        public void setRequestResponse(byte[] requestResponse) {
            this.requestResponse = requestResponse;
        }

        /**
         * For checking if a reponse has been received from a function over IPC.
         *
         * @return true, if a response has received, false otherwise.
         */
        public boolean isRequestResponseReceived() {
            return requestResponseReceived;
        }

        /**
         * Flag set to indicate if an IPC response has received.
         *
         * @param responseReceived true, if response received, false otherwise.
         */
        public void setRequestResponseReceived(boolean responseReceived) {
            this.requestResponseReceived = responseReceived;
        }

        /**
         * Generic method for checking whether if the function is either
         * registered with the local catalogue or the remote catalogue, or
         * both.
         */
        public boolean isRegistered() {
            return registered;
        }

        /**
         * Check if the function is registered with the local catalogue.
         */
        public boolean isRegisteredLocally() {
            return registeredLocally;
        }

        /**
         * Set if registered locally
         *
         * @param registeredLocally
         */
        public void setRegisteredLocally(boolean registeredLocally) {
            if (registeredLocally) registered = true;
            else if (!registeredLocally && !registeredRemotely) registered =
                    false;

            this.registeredLocally = registeredLocally;
        }

        /**
         * Check if the function is registered with the remote catalogue.
         */
        public boolean isRegisteredRemotely() {
            return registeredRemotely;
        }

        /**
         * Set if registered remotely
         *
         * @param registeredRemotely
         */
        public void setRegisteredRemotely(boolean registeredRemotely) {
            if (registeredRemotely) registered = true;
            else if (!registeredRemotely && !registeredLocally) registered =
                    false;

            this.registeredRemotely = registeredRemotely;
        }

        /**
         * Get stored lookup response
         */
        public int[] getLookupResponse() {
            return lookupResponse;
        }

        /**
         * Store received lookup response
         *
         * @param lookupResponse
         */
        public void setLookupResponse(int[] lookupResponse) {
            this.lookupResponse = lookupResponse;
        }

        /**
         * Indicates if to bypass indirecting to locally residing functions
         * over IPC, or to offload the request to the network.
         *
         * @return true, if offloading, false otherwise.
         */
        public boolean isBypassFuncLocal() {
            return bypassFuncLocal;
        }

        /**
         * Set flag to indicate, whether to bypass indirecting to locally residing functions
         * over IPC, or to offload the request to the network.
         *
         * @param funcLocal true, if offloading, false otherwise.
         */
        public void setBypassFuncLocal(boolean funcLocal) {
            this.bypassFuncLocal = funcLocal;
        }

        public void setFunctionInterface(VolleyIPCFunction functionClass) {
            this.functionInterface = functionClass;
        }

        public VolleyIPCFunction getFunctionInterface() {
            return functionInterface;
        }
    }
}
