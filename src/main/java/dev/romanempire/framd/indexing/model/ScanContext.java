package dev.romanempire.framd.indexing.model;

import dev.romanempire.framd.indexing.model.message.ScanMessage;
import dev.romanempire.framd.repository.IndexedMedia;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/// Holds the shared state for an active media indexing scan.
///
/// Tracks scan state including progress and completion status.
@Component
public class ScanContext {
    private static final Logger logger = LoggerFactory.getLogger(ScanContext.class);
    private final AtomicBoolean isScanning = new AtomicBoolean(false);

    private final LinkedBlockingQueue<ScanMessage<IndexedMedia>> persistQueue = new LinkedBlockingQueue<>();

    private final Map<ScanStage, AtomicLong> processed = new EnumMap<>(ScanStage.class);
    private final Map<ScanStage, AtomicLong> total = new EnumMap<>(ScanStage.class);

    {
        initState();
    }

    private void initState() {
        for (ScanStage stage : ScanStage.values()) {
            processed.put(stage, new AtomicLong(0));
            total.put(stage, new AtomicLong(0));
        }
    }


    private Long getStageProcessedStat(ScanStage stage) {
        return processed.get(stage).get();
    }

    private Long getTotalProcessedStat(ScanStage stage) {
        return total.get(stage).get();
    }

    /// Increments the processed count for the given stage by one.
    ///
    /// @param stage the [ScanStage] to increment
    /// @return the updated count
    public Long incrementStageStat(ScanStage stage) {
        return processed.get(stage).incrementAndGet();
    }

    /// Sets the total expected item count for the given stage.
    ///
    /// @param stage    the [ScanStage] to update
    /// @param newTotal the total number of items expected for this stage
    public void setStageStatTotal(ScanStage stage, Long newTotal) {
        total.get(stage).set(newTotal);
    }

    /// Attempts to start a scan by atomically setting the scanning flag.
    ///
    /// @return `true` if the scan was started successfully, `false` if a scan is already in progress
    public boolean startScan() {
        var isFreeToStart = isScanning.compareAndSet(false, true);
        if (isFreeToStart) {
            initState();
            persistQueue.clear();
        }
        return isFreeToStart;
    }

    /// Marks the current scan as complete by clearing the scanning flag.
    public void endScan() {
        isScanning.set(false);
    }


    /// A snapshot of the current scan state.
    ///
    /// @param scanning  whether a scan is currently in progress
    /// @param processed the number of items processed in the persistence stage
    /// @param total     the total number of items expected in the persistence stage
    public record ScanStatus(boolean scanning, Long processed, Long total) {
    }

    /// Returns a snapshot of the current scan state.
    ///
    /// @return a [ScanStatus] record.
    public ScanStatus getScanStatus() {
        return new ScanStatus(
                isScanning.get(),
                getStageProcessedStat(ScanStage.PERSISTENCE),
                getTotalProcessedStat(ScanStage.PERSISTENCE));
    }

    /// Adds an item to the persistence queue to be saved by the drain loop.
    ///
    /// @param item the media to enqueue
    /// @throws IllegalArgumentException if item is null
    /// @throws InterruptedException     if the thread is interrupted while waiting to enqueue
    public void enqueueForPersistence(IndexedMedia item) throws InterruptedException, IllegalArgumentException {
        if (item == null) throw new IllegalArgumentException("item must not be null");
        persistQueue.put(new ScanMessage.Data<>(item));

    }

    /// Signals the drain loop that all items have been enqueued by sending a [ScanMessage.Done] message.
    ///
    /// @return `true` if the signal was sent, `false` if the thread was interrupted
    public boolean completePersistQueue() {
        try {
            persistQueue.put(new ScanMessage.Done<>());
            return true;
        } catch (InterruptedException e) {
            logger.error(String.valueOf(e));
            return false;
        }
    }

    /// Blocks and drains the persistence queue, applying the given action to each item until a
    /// [ScanMessage.Done] message is received.
    ///
    /// @param action the consumer to apply to each dequeued item (e.g. repository save)
    /// @throws InterruptedException if the thread is interrupted while waiting for the next item
    public void drainPersistenceQueue(Consumer<IndexedMedia> action) throws InterruptedException {
        while (true) {
            ScanMessage<IndexedMedia> msg = persistQueue.poll(2, TimeUnit.SECONDS);

            if (msg == null) {
                continue;
            }
            switch (msg) {
                case ScanMessage.Data<IndexedMedia>(var data) -> {
                    action.accept(data);
                    incrementStageStat(ScanStage.PERSISTENCE);
                }
                case ScanMessage.Done<IndexedMedia> _ -> {
                    return;
                }
            }
        }
    }
}
