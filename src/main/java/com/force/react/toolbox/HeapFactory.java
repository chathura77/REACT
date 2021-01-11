package com.force.react.toolbox;

import com.force.react.Heap;

/**
 * Factory for creating {@link Heap} singleton instance.
 * <p>
 * @author Chathura Sarathchandra
 */

public final class HeapFactory {

    private static volatile Heap instance = null;

    public synchronized static Heap getInstance() {
        return instance == null ? (instance = new ByteArrayHeap()
                .setHeapElastic(true)) : instance;
    }

    public synchronized static void eraseMemory() {
        instance = null;
    }
}
