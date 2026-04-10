package dev.romanempire.framd.indexing.model;

import static org.junit.jupiter.api.Assertions.*;

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


}