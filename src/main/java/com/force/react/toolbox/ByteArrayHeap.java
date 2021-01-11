package com.force.react.toolbox;

import android.util.Log;

import com.force.react.Heap;
import com.force.react.HeapUtil;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.ListIterator;

/**
 * Implements a {@link Heap}, that uses a byte[] as a contiguous memory
 * space.
 * <p>
 *
 * @author Chathura Sarathchandra
 */


final public class ByteArrayHeap implements Heap {

    private static final String TAG = ByteArrayHeap.class.getName();
    /**
     * Meta data of the memory blocks
     */
    private volatile LinkedList<HeapBlock> blockList = new
            LinkedList<HeapBlock>();
    /**
     * The heap
     */
    private byte[] memory;
    /**
     * Top of the heap
     */
    private int top = 0;
    /**
     * True, if the array is scalable, otherwise, false.
     */
    private boolean elasticHeap = false;

    /**
     * Creates a {@link ByteArrayHeap} object of size DEFAULT_MAX_MEM_SIZE
     */
    ByteArrayHeap() {
        this.memory = new byte[DEFAULT_MAX_MEM_SIZE];
    }

    /**
     * Creates a {@link ByteArrayHeap} object with the given heap
     *
     * @param memory the byte[] to be used as the memory source
     */
    ByteArrayHeap(byte[] memory) {
        this.memory = memory;
    }

    /**
     * Creates a {@link ByteArrayHeap} object with the given heap size
     *
     * @param size desired size of the memory
     */
    ByteArrayHeap(int size) {
        memory = new byte[size];
    }

    /**
     * Find a free memory block to be used
     *
     * @param size required memory size
     * @return null if no suitable block found, otherwise, the
     * {@link HeapBlock}
     * of the suitable memory block.
     */
    synchronized HeapBlock findFreeBlock(int size) {
        if (blockList.isEmpty()) return null;
        for (HeapBlock block : blockList)
            if (block.free && block.size >= size) return block;

        // could not find a free block
        return null;
    }

    /**
     * Allocate a block of memory from the heap byte[]
     *
     * @param size required block size
     * @return new instance of {@link HeapBlock}, otherwise null.
     */
    synchronized HeapBlock requestMemory(int size) {
        System.out.println("Heap size " + memory.length + " requested size "
                + size + " top + size " + (top + size));
        HeapBlock newBlock = new HeapBlock();

        // check for overflow
        if (memory.length < (top + size)) {
            // Scale the Heap only if it helps and when requesting no more
            // than (free_space + (DEFAULT_MAX_MEM_SIZE / 2)).
            // It also makes sure that the heap is increased only by
            // (DEFAULT_MAX_MEM_SIZE/ 2) at a time.
            if (elasticHeap && (memory.length - top + (DEFAULT_MEM_STEP))
                    >= size) {
                System.out.println("Inside Heap size " + memory.length + " " +
                        "requested size "
                        + size + " top + size " + (top + size) + " elastic: "
                        + elasticHeap);
                byte[] newMemory = new byte[memory.length + (DEFAULT_MEM_STEP)];
                System.arraycopy(memory, 0, newMemory, 0, memory.length);
                memory = newMemory;
            } else return null; // asking too much space
        }

        // initialize new block
        newBlock.reference = top;
        newBlock.size = size;
        blockList.add(newBlock);

        // set new top; top+size+size(reference)
        top = top + size + 1;

        return newBlock;
    }

    @Override
    public synchronized int malloc(int size) {
        if (size <= 0) return NULL_REFERENCE;

//        Log.i(TAG, "Size of  the LinkedList " + getNBlocks());
//        if (getBlock(0) == null) Log.e(TAG, "First block disappeared!");

        HeapBlock newBlock = findFreeBlock(size);

        // first call or no free block found
        if (newBlock == null) {
            newBlock = requestMemory(size);

            // failed requestingMemory -- ran out of memory
            if (newBlock == null) return INSUFFICIENT_MEMORY;
            else {
                newBlock.free = false;
//                return newBlock.reference;
            }

            //TODO: below here is not unit tested
        } else if (newBlock.size > size) { // found a free block that is larger
//            Log.i(TAG, "Found block is larger " + newBlock.size + " than " +
//                    "requested " + size);
            split(newBlock, size);
//            Log.i(TAG, "Found block, resized " + newBlock.size + " requested "
//                    + size + " n blocks " + getNBlocks() + "Heap size " +
//                    memory.length + " The heap \n" + blockList);
            newBlock.free = false;

        } else {
            newBlock.free = false;

        }// found a free block of requested size

//        Log.i(TAG, "malloc newly allocated block is " + newBlock.free);
        return newBlock.reference;
    }

    private void merge() {
        ListIterator<HeapBlock> iterator = blockList.listIterator();

        while (iterator.hasNext()) {
            HeapBlock current = iterator.next();

            // Current block is not free
            if (!current.isFree()) continue;

            // remove last block if its free.
            if (!iterator.hasNext()) {
                top = current.reference; // reset the top
                iterator.remove();
                return;
            }

            HeapBlock next = iterator.next();

            // combine all free blocks to the right side of the heap
            while (next.isFree()) {
                current.size = current.size + (next.size + 1); // resize
                iterator.remove(); // removes the next free block

                if (iterator.hasNext()) {
                    next = iterator.next();
                } else { // reached the last element
                    iterator.previous();
                    break;
                }
            }
        }
    }

    private void merge2() {
        ListIterator<HeapBlock> iterator = (ListIterator<HeapBlock>) blockList.iterator();

        while (iterator.hasNext()) {
            HeapBlock current = iterator.next();

            // Current block is not free
            if (!current.isFree()) continue;

            // remove last block if its free.
            if (!iterator.hasNext()) {
                top = current.reference; // reset the top
                iterator.remove();
                return;
            }

            HeapBlock next = iterator.next();

            // combine all free blocks to the right side of the heap
            if (next.isFree()) {
                current.size = current.size + (next.size + 1); // resize
                iterator.remove(); // removes the next free block

//                if (iterator.hasNext()) {
//                    next = iterator.next();
//                } else { // reached the last element
//                    iterator.previous();
//                    break;
//                }
            }
        }
    }

    /**
     * Splits an existing {@link HeapBlock} into two, and add the second
     * {@link HeapBlock} back into free blocks
     *
     * @param block
     * @param size
     */
    private void split(HeapBlock block, int size) {
        //oldblock.size - (newblocksize + size(new_block_reference))
        // Create a new block from the remainder of the memory,and add it back
        // to the pool.
        HeapBlock newBlock = new HeapBlock();
        newBlock.size = block.size - (size + 1);
        newBlock.reference = block.reference + size + 1;
        newBlock.free = true;
        blockList.add(newBlock);

//        Log.i(TAG, "Found block, split new block of size " + newBlock.size +
//                " original block size " + block.size + " requested size " + size);
        block.size = size;  // resize the original block
    }

    @Override
    public synchronized HeapBlock getBlock(int reference) {
        if (reference == NULL_REFERENCE) return null;

        for (HeapBlock block : blockList)
            if (block.reference == reference) return block;

        // could not find the referred block
        return null;
    }

    @Override
    public synchronized int getSize() {
        return memory.length;
    }

    @Override
    public synchronized int getNBlocks() {
        return blockList.size();
    }

    @Override
    public synchronized LinkedList<HeapBlock> getBlocks() {
        return blockList;
    }

    /**
     * Set if the Heap is scalable.
     *
     * @param elastic true, if yes, false otherwise.
     */
    public synchronized ByteArrayHeap setHeapElastic(boolean elastic) {
        this.elasticHeap = elastic;
        return this;
    }

    @Override
    public synchronized void free(int reference) {
        if (reference < 0) throw new IllegalArgumentException("Invalid " +
                "Reference" + reference);

        HeapBlock block = getBlock(reference);

        if (block == null)
            throw new IllegalArgumentException("Memory block "
                    + reference + " could not be found! in Heap of size " + getNBlocks());

        if (block.free) throw new IllegalStateException("The referred " +
                " block has already been freed!");

        block.free = true;
        Log.i(TAG, "Heap freed ref: " + HeapUtil.getCallerClassName
                (ByteArrayHeap.class) +
                " " +
                reference);
        block.clearAccessLog();
        merge();
    }

    @Override
    public synchronized int realloc(int reference, int size) {
        if (reference == NULL_REFERENCE || reference < 0) return malloc(size);

        HeapBlock block = getBlock(reference);

        // does not need resizing
        if (block.size >= size) return reference;

        // need to resize the block
        int newReference = malloc(size);

        // could not allocate a new block
        if (newReference == NULL_REFERENCE || newReference == INSUFFICIENT_MEMORY)
            return newReference;

        // copy bytes from the old block to the new block
        System.arraycopy(memory, reference, memory, newReference, size);
        free(reference);

        return newReference;
    }

    @Override
    public synchronized int calloc(int nelem, int elsize) {
        int size = nelem * elsize;
        // the byte[] is already initialized to 0 in java
        int reference = malloc(size);

        return reference;
    }

    @Override
    public synchronized int write(int reference, byte[] data) {
        //reference can't be less than 0
        if (reference < 0) return NULL_REFERENCE;

        //check if the reference is valid
        HeapBlock block = getBlock(reference);
        if (block == null || block.free) return INVALID_REFERENCE;

        //check bounds
        if (block.size < data.length) return INSUFFICIENT_MEMORY;

        //write to Heap
        System.arraycopy(data, 0, memory, reference, data.length);

        return data.length;
    }

    @Override
    public synchronized byte[] read(int reference) {
        //reference can't be less negative
        if (reference < 0) return null;

        //check if the reference is valid
        HeapBlock block = getBlock(reference);
//        Log.i(this.getClass().getName(), "Heap block: " + block.toString());
        if (block == null || block.free) return null;

        try {
            return Arrays.copyOfRange(memory, reference, reference + block.size);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, e.toString());
            return null;
        }
    }

    @Override
    public synchronized byte[] getMemory() {
        return memory;
    }

    /**
     * Get the reference to the top of the heap
     *
     * @return top
     */
    synchronized int getTop() {
        return top;
    }

    /**
     * Get the size of the heap
     *
     * @return the size
     */
    synchronized int getMemSize() {
        return memory.length;
    }
}

