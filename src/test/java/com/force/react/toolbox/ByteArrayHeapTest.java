package com.force.react.toolbox;

import com.force.react.Heap;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * Tests {@link ByteArrayHeap}
 * <p>
 * @author Chathura Sarathchandra
 */

public class ByteArrayHeapTest {

    @Test
    public void heapFactoryTest() {
        System.out.println("memoryFactoryTest");
        HeapFactory.eraseMemory();
        assertTrue(HeapFactory.getInstance() != null);
    }


    @Test
    // do a singleton check
    public void heapInstanceCountCheck() {
        System.out.println("memoryInstanceCountCheck");
        HeapFactory.eraseMemory();
        Heap firstInstance = HeapFactory.getInstance();
        assertSame(firstInstance, HeapFactory.getInstance());
        assertSame(HeapFactory.getInstance(), HeapFactory.getInstance());
        HeapFactory.eraseMemory();
        assertSame(HeapFactory.getInstance(), HeapFactory.getInstance());
        assertSame(HeapFactory.getInstance(), HeapFactory.getInstance());
    }

    @Test
    public void clearHeapTest() {
        System.out.println("clearMemoryTest");
        HeapFactory.eraseMemory();

        Heap firstInstance = HeapFactory.getInstance();

        HeapFactory.eraseMemory();

        assertNotSame(firstInstance, HeapFactory.getInstance());
    }

    @Test
    public void requestHeapTest() {
        System.out.println("requestMemoryTest");
        HeapFactory.eraseMemory();
        ByteArrayHeap memory = (ByteArrayHeap) HeapFactory.getInstance();
        ((ByteArrayHeap) memory).setHeapElastic(false);

        // Check if returning null when buffer overflow
        assertNull(memory.requestMemory(Heap.DEFAULT_MAX_MEM_SIZE + 1));

        HeapBlock blockMeta = memory.requestMemory((int) Math.pow(1024, 2));
        System.out.println("requestMemoryTest: The block reference: " +
                blockMeta.reference);
        assertTrue(blockMeta.reference == 0);
        assertTrue(!blockMeta.free);
        assertTrue(blockMeta.size == (Math.pow(1024, 2)));
    }

    @Test
    public void topTest() {
        System.out.println("topTest");

        HeapFactory.eraseMemory();
        // get the Heap object
        ByteArrayHeap memory = (ByteArrayHeap) HeapFactory.getInstance();

        // request memory block
        memory.requestMemory((int) (Math.pow(1024, 2) * 2));

        // the top is the same as the end position of the created blocks
        assertTrue(memory.getTop() == ((Math.pow(1024, 2)) * 2) + 1);
    }

    @Test
    public void heapArraySizeTest() {
        System.out.println("memoryArraySizeTest");
        HeapFactory.eraseMemory();
        ByteArrayHeap memory = (ByteArrayHeap) HeapFactory.getInstance();

        // request memory block
        memory.requestMemory((int) ((Math.pow(1024, 2)) * 2));

        // The size of the array is same as the size of the array
        assertTrue(memory.getMemSize() == Heap.DEFAULT_MAX_MEM_SIZE);
    }
    //do a reference assignment test

    @Test
    public void referenceAssignmentTest() {
        System.out.println("referenceAssignmentTest");
        HeapFactory.eraseMemory();
        ByteArrayHeap memory = (ByteArrayHeap) HeapFactory.getInstance();

        // request memory block
        HeapBlock block = memory.requestMemory((int) Math.pow(1024, 2));

        assertTrue(block.reference == 0);

        block = memory.requestMemory((int) ((Math.pow(1024, 2)) * 2));

        System.out.println("referenceAssignmentTest: " + block.reference);

        assertTrue(block.reference == (Math.pow(1024, 2)) + 1);

        block = memory.requestMemory((int) ((Math.pow(1024, 2)) * 2));

        System.out.println("referenceAssignmentTest: " + block.reference);
        assertTrue(block.reference == (Math.pow(1024, 2) + 1) + ((Math.pow
                (1024, 2)) * 2) + 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void freeNullExceptionTest() {
        System.out.println("freeNullExceptionTest");

        HeapFactory.eraseMemory();
        ByteArrayHeap memory = (ByteArrayHeap) HeapFactory.getInstance();
        memory.free(50);
    }

    @Test(expected = IllegalStateException.class)
    public void freeNotFoundExceptionTest() {
        System.out.println("freeNotFoundExceptionTest");

        HeapFactory.eraseMemory();
        ByteArrayHeap memory = (ByteArrayHeap) HeapFactory.getInstance();
        // request memory block
        HeapBlock block = memory.requestMemory((int) Math.pow
                (1024, 2) / 2);

        memory.requestMemory((int) Math.pow
                (1024, 2) / 2);

        memory.free(block.reference);

        memory.free(block.reference); //fails
    }

    @Test
    public void findFreeBlockTest() {
        System.out.println("findFreeBlockTest");

        HeapFactory.eraseMemory();
        ByteArrayHeap memory = (ByteArrayHeap) HeapFactory.getInstance();

        assertTrue(memory.findFreeBlock(50) == null);

        HeapBlock block = memory.requestMemory((int) Math.pow
                (1024, 2) / 2);

        memory.free(block.reference);

//        assertSame(block, memory.findFreeBlock(block.size));

        assertTrue(memory.findFreeBlock((int) (Math.pow(1024, 2))) ==
                null);
    }

    @Test
    public void getBlockMetaTest() {
        System.out.println("getBlockMetaTest");

        HeapFactory.eraseMemory();
        ByteArrayHeap memory = (ByteArrayHeap) HeapFactory.getInstance();

        HeapBlock block = memory.requestMemory((int) Math.pow(1024, 2) / 2);

        assertSame(block, memory.getBlock(block.reference));

        assertNull(memory.getBlock(323212344));
    }

    @Test
    public void mallocScalabilityTest() {
        System.out.println("mallocScalableTest ");

        HeapFactory.eraseMemory();
        Heap memory = HeapFactory.getInstance();

        // make the Heap scalable
        ((ByteArrayHeap) memory).setHeapElastic(true);

        assertTrue(memory.getSize() == Heap.DEFAULT_MAX_MEM_SIZE);

        // scalability
        int reference = memory.malloc(Heap.DEFAULT_MAX_MEM_SIZE + 1);
        assertFalse(reference == Heap.NULL_REFERENCE);
        assertTrue(((ByteArrayHeap) memory).getBlock(reference).size == Heap.DEFAULT_MAX_MEM_SIZE + 1);

        // scalability test but ask too much space
        reference = memory.malloc(Heap.DEFAULT_MAX_MEM_SIZE * 2);
        assertTrue(reference == Heap.INSUFFICIENT_MEMORY);

        // scale with maximum scalable amount ByteArrayHeap.DEFAULT_MAX_MEM_SIZE / 2
        assertTrue(memory.getSize() == Heap.DEFAULT_MAX_MEM_SIZE
                + Heap.DEFAULT_MAX_MEM_SIZE / 2);

        int iterations = 100;
        for (int i = 0; i < iterations; i++) {
            reference = memory.malloc(Heap.DEFAULT_MAX_MEM_SIZE / 2);
            assertFalse(reference == Heap.NULL_REFERENCE);
            assertTrue(((ByteArrayHeap) memory).getBlock(reference).size
                    == Heap.DEFAULT_MAX_MEM_SIZE / 2);
        }

        assertTrue(memory.getSize() == (Heap.DEFAULT_MAX_MEM_SIZE
                + (Heap.DEFAULT_MAX_MEM_SIZE / 2)) +
                ((Heap.DEFAULT_MAX_MEM_SIZE /
                        2) * iterations));
    }

    @Test
    public void mergeTest() {
        System.out.println("mergeTest");

        HeapFactory.eraseMemory();
        Heap memory = HeapFactory.getInstance();

        int first = memory.malloc(Heap.DEFAULT_MAX_MEM_SIZE / 2);
        int block1 = memory.malloc(Heap.DEFAULT_MAX_MEM_SIZE / 2);
        int block2 = memory.malloc(Heap.DEFAULT_MAX_MEM_SIZE / 2);
        memory.malloc(Heap.DEFAULT_MAX_MEM_SIZE / 2);

        memory.free(block1);
        memory.free(block2);

        System.out.println("N blocks after merge " + memory.getNBlocks());

        assertTrue(memory.getNBlocks() == 3);
        assertTrue(((ByteArrayHeap) memory).getBlock(block1).size == (Heap
                .DEFAULT_MAX_MEM_SIZE + 1));
        System.out.println("First block " + ((ByteArrayHeap) memory).getBlock
                (0));
        assertNotNull(((ByteArrayHeap) memory).getBlock(0));

        HeapFactory.eraseMemory();
        memory = HeapFactory.getInstance();
        memory.malloc(Heap.DEFAULT_MAX_MEM_SIZE / 2);
        int block3 = memory.malloc(Heap.DEFAULT_MAX_MEM_SIZE / 2);
        int block4 = memory.malloc(Heap.DEFAULT_MAX_MEM_SIZE / 2);
        int block5 = memory.malloc(Heap.DEFAULT_MAX_MEM_SIZE / 2);
        int block6 = memory.malloc(Heap.DEFAULT_MAX_MEM_SIZE / 2);
        int block7 = memory.malloc(Heap.DEFAULT_MAX_MEM_SIZE / 2);
        int block8 = memory.malloc(Heap.DEFAULT_MAX_MEM_SIZE / 2);
        int block9 = memory.malloc(Heap.DEFAULT_MAX_MEM_SIZE / 2);
        memory.malloc(Heap.DEFAULT_MAX_MEM_SIZE / 2);

        memory.free(block3);
        memory.free(block4);
        memory.free(block5);
        memory.free(block6);
        memory.free(block7);
        memory.free(block8);
        memory.free(block9);

        System.out.println("N blocks after merge 2 " + memory.getNBlocks());
        assertTrue(memory.getNBlocks() == 3);
        assertTrue(((ByteArrayHeap) memory).getBlock(block3).size == (7 * (Heap
                .DEFAULT_MAX_MEM_SIZE / 2) + 6));
        assertNotNull(((ByteArrayHeap) memory).getBlock(0));

        HeapFactory.eraseMemory();
        memory = HeapFactory.getInstance();
        memory.malloc(Heap.DEFAULT_MAX_MEM_SIZE / 2);

        for (int i = 0; i < 100; i++) {
            int ref = memory.malloc(Heap.DEFAULT_MAX_MEM_SIZE / 2);
            memory.free(ref);
        }

        memory.malloc(Heap.DEFAULT_MAX_MEM_SIZE / 2);

        System.out.println("N blocks after merge 3 " + memory.getNBlocks());
        assertTrue(memory.getNBlocks() == 2);
        assertNotNull(((ByteArrayHeap) memory).getBlock(0));

        HeapFactory.eraseMemory();
        memory = HeapFactory.getInstance();
        memory.malloc(Heap.DEFAULT_MAX_MEM_SIZE / 2);

        int ref1 = memory.malloc(Heap.DEFAULT_MAX_MEM_SIZE / 2);
        int ref2 = memory.malloc(Heap.DEFAULT_MAX_MEM_SIZE / 2);
        int ref3 = memory.malloc(Heap.DEFAULT_MAX_MEM_SIZE / 2);
//        int ref4 = memory.malloc(Heap.DEFAULT_MAX_MEM_SIZE / 2);
//        int ref5 = memory.malloc(Heap.DEFAULT_MAX_MEM_SIZE / 2);

        memory.free(ref1);
        memory.free(ref2);
        memory.free(ref3);
//        memory.free(ref4);
//        memory.free(ref5);

        memory.malloc(Heap.DEFAULT_MAX_MEM_SIZE / 2);

        System.out.println("N blocks after merge 3 " + memory.getNBlocks());
        assertTrue(memory.getNBlocks() == 2);
        assertNotNull(((ByteArrayHeap) memory).getBlock(0));
    }
}
