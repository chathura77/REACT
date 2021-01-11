package com.force.react;

import com.force.react.toolbox.HeapBlock;

import java.util.LinkedList;

/**
 * Types of this interface will provide a dynamically manageable memory space.
 * All {@link Heap} types must be singletons.
 * <p>
 * All operations on the Heap should be thread safe.
 * <p>
 *
 * @author Chathura Sarathchandra
 */

public interface Heap {
    /**
     * Default maximum size of the heap
     */
    int DEFAULT_MAX_MEM_SIZE = (int) Math.pow(1024, 2) * 170;

    /**
     * Step for increasing Heap size
     */
    int DEFAULT_MEM_STEP = (int) Math.pow(1024, 2) * 10;
    /**
     * ByteArrayHeap response codes
     */
    int NULL_REFERENCE = -1;
    int INSUFFICIENT_MEMORY = -2;
    int INVALID_REFERENCE = -3;

    String HEAP_REF = "HEAP_REF";

    /**
     * If no memory is allocated, then allocate. If there are previously
     * allocated memory, then check if any can be re-used. If yes, then
     * reuse, otherwise, allocate new memory.
     *
     * @param size requested memory size
     * @return reference to the beginning of the allocated memory region.
     * NULL_REFERENCE - memory cannot be allocated
     * INSUFFICIENT_MEMORY - insufficient memory
     */
    int malloc(int size);

    /**
     * Free up allocated memory
     *
     * @param reference reference to memory
     */
    void free(int reference);

    /**
     * Adjust the size of a memory block
     *
     * @param reference Reference to the memory block
     * @param size      size to adjust to
     * @return reference to the resized memory block
     * NULL_REFERENCE - memory cannot be allocated
     * INSUFFICIENT_MEMORY - insufficient memory
     */
    int realloc(int reference, int size);

    /**
     * Allocate memory and initialize it with 0
     *
     * @param nelem  number of elements
     * @param elsize size of an element
     * @return reference to the memory block
     * NULL_REFERENCE - memory cannot be allocated
     * INSUFFICIENT_MEMORY - insufficient memory
     */
    int calloc(int nelem, int elsize);

    /**
     * Write data to the specified memory block
     *
     * @param reference reference to the memory block
     * @param data      data to write
     * @return bytes written if > 0, ERROR otherwise.
     * NULL_REFERENCE - memory cannot be allocated
     * INSUFFICIENT_MEMORY - insufficient memory
     * INVALID_REFERENCE - the provided reference is invalid
     */
    int write(int reference, byte[] data);

    /**
     * Read a memory block and returns a copy of the data in the memory block.
     * Does not maintain any references back to the Heap. Therefore, changes to
     * the returned array does not affect the original memory block in the Heap.
     *
     * @param reference the reference to the memory block
     * @return the read byte[], or null if read was unsuccessful
     */
    byte[] read(int reference);

    /**
     * Get heap object.
     * <p>
     * Do not directly write to the memory, instead use the write() method.
     *
     * @return the base memory object
     */
    byte[] getMemory();

    /**
     * Get the current size of the heap
     */
    int getSize();

    /**
     * Get the number of blocks created
     */
    int getNBlocks();

    /**
     * Get the LinkedList of memory blocks
     */
    LinkedList<HeapBlock> getBlocks();

    /**
     * Get the specified block in the Heap
     *
     * @param reference the reference number
     * @return the heap block if exists, null otherwise.
     */
    HeapBlock getBlock(int reference);
}
