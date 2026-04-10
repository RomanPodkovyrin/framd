package dev.romanempire.framd.indexing.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

/// Holds the shared state for an active media indexing scan.
///
/// Tracks scan state including progress and completion status.
@Component
public class ScanContext {

    private final AtomicBoolean isScanning = new AtomicBoolean(false);

    private Long total = 0L;

    private Long current = 0L;


    /// Attempts to start a scan by atomically setting the scanning flag.
    ///
    /// @return `true` if the scan was started successfully, `false` if a scan is already in progress
    public boolean startScan() {
        return isScanning.compareAndSet(false, true);
    }

    /// Marks the current scan as complete by clearing the scanning flag.
    public void endScan() {
        isScanning.set(false);
    }


    public record ScanStatus(boolean scanning, Long processed, Long total) {
    }

    /// Returns a snapshot of the current scan state.
    ///
    /// @return a [ScanStatus] record.
    public ScanStatus getScanStatus() {
        return new ScanStatus(isScanning.get(), current, total);
    }

}
