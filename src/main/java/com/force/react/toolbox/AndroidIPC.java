package com.force.react.toolbox;

import com.force.react.IPC;
import com.force.react.IPCResponse;
import com.force.react.IPCUtil;
import com.force.react.NetworkResponse;
import com.force.react.Request;
import com.force.react.VolleyError;
import com.force.react.VolleyIPCFunction;
import com.force.react.VolleyLog;

import java.io.IOException;

/**
 * Implements OS specific IPC procedures
 * <p>
 * @author Chathura Sarathchandra
 */

public class AndroidIPC implements IPC {
    protected static final boolean DEBUG = VolleyLog.DEBUG;
    private static final String TAG = AndroidIPC.class.getName();

    /**
     * IPC Object of chosen sub-type
     */
    private IPCStack mIPCStack;

    /**
     * Supported IPC types of the function.
     */
    private int[] functionMeta;

    /**
     * The priority of chosen IPC method.
     * Currently chooses highest priority.
     */
    private int ipcPriority = 0;

    /**
     * AndroidIPC empty constructor
     *
     * @param metaData Supported IPC types of the function
     */
    public AndroidIPC(int[] metaData) {
        //Get handle to the service catalogue data.
        functionMeta = metaData;

        // Check available IPCs of the local function.
        chooseIPC();
    }

    private void chooseIPC() {
        /**
         * Check available IPC types from the service catalogue, but
         * currently selects the one with the highest priority
         * (the first in the list).
         **/
        switch (functionMeta[ipcPriority]) {
            case IPCUtil.FORCE_ANDROID_IPC_TYPE_HEAP:
                mIPCStack = new HeapStack();
                break;
            case IPCUtil.FORCE_ANDROID_IPC_TYPE_LBHEAP:
                mIPCStack = new HeapLBStack();
                break;
            case IPCUtil.FORCE_ANDROID_IPC_TYPE_LBROADCAST:
                mIPCStack = new LocalBroadcastStack();
                break;
            case IPCUtil.FORCE_ANDROID_IPC_TYPE_MESSENGER:
                break;
            case IPCUtil.FORCE_ANDROID_IPC_TYPE_AIDL:
                break;
            case IPCUtil.FORCE_ANDROID_IPC_TYPE_BINDER:
                break;
            default:
                break;
        }

        /**
         * Otherwise: choose the next one in priority.
         * */
    }

    /**
     * Performs the specified request.
     *
     * @param request       Request to process
     * @param functionName  The name of the function (reversed domain name)
     * @param functionClass the class object of the requesting function,
     *                      returned by the local lookup catalogue.
     * @return A {@link NetworkResponse} with data and caching metadata; will never be null
     * @throws VolleyError on errors
     */
    @Override
    public IPCResponse performRequest(Request<?> request, String functionName, VolleyIPCFunction functionClass)
            throws VolleyError {

        //Perform request.
        try {
            return mIPCStack.performRequest(request, functionName, functionClass);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
