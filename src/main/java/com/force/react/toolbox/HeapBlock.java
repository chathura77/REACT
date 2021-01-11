package com.force.react.toolbox;

import java.util.ArrayList;

import static com.force.react.Heap.NULL_REFERENCE;

/**
 * @author Chathura Sarathchandra
 */

// Holds metadata of a memory block
final public class HeapBlock {
    private final ArrayList<String> accessLog = new ArrayList<>();
    int reference = NULL_REFERENCE;
    int size;
    boolean free = false;

    /**
     * Get the reference of the memory block
     */
    public synchronized int getReference() {
        return reference;
    }

    /**
     * Get the size of the memory block
     */
    public synchronized int getSize() {
        return size;
    }

    /**
     * Block status
     *
     * @return true, if free, false otherwise.
     */
    public synchronized boolean isFree() {
        return free;
    }

    @Override
    public synchronized String toString() {
        return "The Heap block\n[Reference: " + reference + "\nSize: " + size
                + "\nFree = " + free + "\n " +
                "Access log:\n" + accessLog + "]\n";
    }

    /**
     * Add a name to the access log
     *
     * @param name name of the accessing entity
     */
    public synchronized void addAccess(String name) {
        accessLog.add(name);
    }

    public synchronized void clearAccessLog() {
        accessLog.clear();
    }
}
