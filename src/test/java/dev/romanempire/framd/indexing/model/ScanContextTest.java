package dev.romanempire.framd.indexing.model;

import static org.junit.jupiter.api.Assertions.*;

import dev.romanempire.framd.repository.model.IndexedMedia;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class ScanContextTest {

    ScanContext scanContext = new ScanContext();

    @AfterEach
    void tearDown() {
        scanContext.endScan();
    }

    @Test
    void startScan() {
        assertFalse(scanContext.getScanStatus().scanning(), "Should start as not scanning");

        assertTrue(scanContext.startScan(), "Should start first scan");
        assertTrue(scanContext.getScanStatus().scanning(), "Scanning should be in progress");
        assertFalse(scanContext.startScan(), "Should not allow second scan");
        scanContext.endScan();

        assertFalse(scanContext.getScanStatus().scanning(), "Should not be marked as scanning");

        assertTrue(scanContext.startScan(), "Should allow scanning again");
        assertTrue(scanContext.getScanStatus().scanning(), "Scanning should be in progress");
    }

    @Test
    void enqueueMessagesForPersistence() throws InterruptedException {
        // Setup
        scanContext.startScan();
        var numOfMessages = 10;
        // Populate the queue
        IntStream.range(0, numOfMessages).forEach(_ -> {
            try {
                scanContext.enqueueForPersistence(new IndexedMedia());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        // Try inserting invalid message
        var ex = assertThrows(
                IllegalArgumentException.class,
                () -> scanContext.enqueueForPersistence(null),
                "should reject null object");

        assertEquals("item must not be null", ex.getMessage());
        // indicate the queue is done
        scanContext.completePersistQueue();
        var processedList = new ArrayList<>();
        scanContext.drainPersistenceQueue(processedList::add);

        assertEquals(numOfMessages, processedList.size());
        assertEquals(numOfMessages, scanContext.getScanStatus().processed());
    }

    @Test
    void completeEmptyPersistQueue() throws InterruptedException {
        scanContext.startScan();

        scanContext.completePersistQueue();
        var called = new AtomicBoolean(false);
        scanContext.drainPersistenceQueue(_ -> called.set(true));
        assertFalse(called.get(), "Nothing was given to the queue");
    }

    @Test
    void drainPersistenceQueueBlocksWithoutDone() throws InterruptedException {
        var thread = Thread.ofVirtual().start(() -> {
            try {
                scanContext.drainPersistenceQueue(_ -> {});
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        thread.join(500); // wait 500ms
        assertTrue(thread.isAlive(), "Drain should still be blocking");
        scanContext.completePersistQueue();
        thread.join(6000); // wait to terminate
        assertFalse(thread.isAlive(), "Poison pill has been send, should have terminated");
    }
}
