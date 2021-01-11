package com.force.react;

import android.util.Log;

import com.force.react.toolbox.HeapFactory;

/**
 * @author Chathura Sarathchandra
 */

public final class HeapUtil {

    /**
     * Return the bytes in the memory block, then free back to the {@link Heap}
     *
     * @param reference the reference to the memory block
     * @param TAG       name of the invoking class (for logging purposes)
     * @return heap block as a byte[]
     */
    public static byte[] getBlockData(int reference, String TAG) {
        Heap heap = HeapFactory.getInstance();
//        Log.i(TAG + " getBlockData()", "Heap object: " + heap.hashCode());
//        Log.i(TAG + " getBlockData()", "Heap received reference: " +
//                reference);
        byte[] response = heap.read(reference); //big-endian by default

        if (reference >= 0 && response != null) {
            try {
                heap.free(reference);
            } catch (Exception e) {
                Log.e(TAG, "Heap: unsuccessful call to free" +
                        "() \n" + e.toString());
            }

//            if (response != null)
//                Log.i(TAG, "Heap, read byte size: " + response.length);
        }
        return response;
    }

    public static String getCallerClassName(Class reqClass) {
        StackTraceElement[] stElements = Thread.currentThread().getStackTrace();
        for (int i = 1; i < stElements.length; i++) {
            StackTraceElement ste = stElements[i];
            if (!ste.getClassName().equals(reqClass.getName()) && ste
                    .getClassName().indexOf("java.lang.Thread") != 0) {
                return ste.getClassName();
            }
        }
        return null;
    }
}
