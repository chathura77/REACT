package com.force.react.toolbox;

import com.force.react.Heap;

import org.junit.Test;

import java.io.UnsupportedEncodingException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests {@link Heap}
 * <p>
 * @author Chathura Sarathchandra
 */

public class HeapTest {

    @Test
    public void freeBlockTest() {
        System.out.println("freeBlockTest");

        HeapFactory.eraseMemory();
        Heap memory = HeapFactory.getInstance();

        // request memory block
        int block = memory.malloc((int) Math.pow(1024, 2) / 2);

        int block1 = memory.malloc((int) (Math.pow(1024, 2)) * 2);

        int block2 = memory.malloc((int) (Math.pow(1024, 2) * 3));

        ((ByteArrayHeap) memory).setHeapElastic(true);

        int block3 = memory.malloc((int) ((Math.pow(1024, 2)) / 2));

        int block4 = memory.malloc((int) (Math.pow(1024, 2) * 3));


        assertTrue(block >= 0 && block1 >= 0 && block2 >= 0 && block3 >= 0);

        memory.free(block);
        memory.free(block1);
        memory.free(block2);
        memory.free(block3);

//        assertSame(((ByteArrayHeap) memory).getBlock(block1), ((ByteArrayHeap)
//                memory)
//                .findFreeBlock
//                        (((ByteArrayHeap) memory).getBlock(block1).size));
//        assertSame(((ByteArrayHeap) memory).getBlock(block2), ((ByteArrayHeap) memory).findFreeBlock
//                (((ByteArrayHeap) memory).getBlock(block2).size));

        // pick either the block or the block3
//        assertTrue(((ByteArrayHeap) memory).findFreeBlock(((ByteArrayHeap) memory).getBlock
//                (block).size).equals(((ByteArrayHeap) memory).getBlock(block)) ||
//                ((ByteArrayHeap) memory).findFreeBlock(((ByteArrayHeap) memory).getBlock(block).size)
//                        .equals(((ByteArrayHeap) memory).getBlock(block3)));
    }

    @Test
    public void mallocTest() {
        System.out.println("mallocTest");

        HeapFactory.eraseMemory();
        Heap memory = HeapFactory.getInstance();

        assertTrue(memory.malloc(-8) == Heap.NULL_REFERENCE);

        int reference = memory.malloc((int) (Math
                .pow(1024, 2)));
        // block test
        assertTrue(reference == ((ByteArrayHeap) memory).getBlock(reference).reference);

        // free flat test
        assertTrue(!((ByteArrayHeap) memory).getBlock(reference).free);

        // size test
        assertTrue((((ByteArrayHeap) memory).getBlock(reference).size == (int) (Math
                .pow(1024, 2))));

        memory.free(reference);

        assertTrue(memory.malloc((int) (Math.pow(1024, 2) * 2)) == reference);

        // findFreeBlock success
        int reference2 = memory.malloc((int) (Math.pow(1024, 2)));
        assertTrue(reference2 != reference);
        assertTrue(!((ByteArrayHeap) memory).getBlock(reference2).free);

        // out of memory malloc
        assertTrue(memory.malloc(Heap.DEFAULT_MAX_MEM_SIZE + 1) ==
                Heap.INSUFFICIENT_MEMORY);
    }


    @Test
    public void reallocTest() {
        System.out.println("reallocTest");

        HeapFactory.eraseMemory();
        Heap memory = HeapFactory.getInstance();

        // not null reference
        assertNotNull(memory.realloc(Heap.NULL_REFERENCE, (int) (Math
                .pow(1024, 2))));

        // negative reference
        assertNotNull(memory.realloc(-1, (int) (Math
                .pow(1024, 2))));

        int block = memory.malloc((int) (Math
                .pow(1024, 2)));

        // does not need resizing, and return the same reference
        assertTrue(memory.realloc(block, (int) (Math
                .pow(1024, 2))) == block);

        // cant resize and return ByteArrayHeap.NULL_REFERENCE
        assertTrue(memory.realloc(block, Heap.DEFAULT_MAX_MEM_SIZE + 1
        ) == Heap.INSUFFICIENT_MEMORY);

        // reallocating a block
        int block2 = memory.realloc(block, (int) (Math
                .pow(1024, 2) * 2));

        // check if a new block is allocated
        assertTrue(block2 != block);

        // check if the old block is free
        assertTrue((((ByteArrayHeap) memory)).getBlock(block).free);

        // check if the top of the blocks has grown accordingly
        assertTrue(((ByteArrayHeap) memory).getTop() == block2 + (int)
                (Math.pow(1024, 2) * 2) + 1);
    }

    @Test
    public void callocTest() {
        System.out.println("callocTest");

        HeapFactory.eraseMemory();
        Heap memory = HeapFactory.getInstance();

        int reference = memory.calloc((int) Math.pow(1024, 2), 2);

        // check the size of the block
        assertTrue((((ByteArrayHeap) memory).getBlock(reference).size ==
                (int) Math.pow(1024, 2) * 2));
    }

    @Test
    public void writeTest() {
        System.out.println("writeTest");

        HeapFactory.eraseMemory();
        Heap memory = HeapFactory.getInstance();

        String testData = "com.force.react";

        int reference = 0;

        for (int i = 0; i < 20; i++) {
            reference = memory.malloc(testData.getBytes().length);

            // negative reference
            assertTrue(memory.write(-232323232, new byte[23]) == Heap
                    .NULL_REFERENCE);

            // unknown reference
            assertTrue(memory.write(223982, new byte[23]) == Heap
                    .INVALID_REFERENCE);

            // too large data
            assertTrue(memory.write(reference, new byte[testData.getBytes()
                    .length + 1]) ==
                    Heap.INSUFFICIENT_MEMORY);

            memory.write(reference, testData.getBytes());

            try {
                assertTrue(((ByteArrayHeap) memory).getBlock(reference).size == testData.getBytes().length);

                System.out.println("Read String: " + new String(memory.read
                        (reference), "UTF-8"));

                // block not null
                assertNotNull(memory.read(reference));

                assertEquals(testData, new String(testData.getBytes()));

                // data in Heap is correct
                assertEquals(testData, new String(memory.read(reference)));

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        // free block read
        memory.free(reference);
        assertTrue(memory.write(reference, new byte[23]) == Heap
                .INVALID_REFERENCE);
    }

    @Test
    public void readTest() {
        System.out.println("writeTest");

        HeapFactory.eraseMemory();
        Heap memory = HeapFactory.getInstance();

        String testData = "com.force.react";

        int reference = memory.malloc(testData.getBytes().length);
        memory.write(reference, testData.getBytes());

        // null reference
        assertNull(memory.read(-99));

        // wrong reference
        assertNull(memory.read(93849));

        // content check
        assertEquals(testData, new String(memory.read(reference)));

        // free block read
        memory.free(reference);
        assertNull(memory.read(reference));
    }
}
