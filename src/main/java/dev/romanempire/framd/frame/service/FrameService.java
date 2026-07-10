package dev.romanempire.framd.frame.service;

import dev.romanempire.framd.dto.IndexMediaDto;
import dev.romanempire.framd.frame.impl.FrameSelector;
import dev.romanempire.framd.repository.*;
import dev.romanempire.framd.repository.model.FrameLog;
import dev.romanempire.framd.repository.model.IndexedMedia;
import java.time.LocalDateTime;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FrameService {

    private final IndexedMediaRepo indexedMediaRepo;

    private final FrameLogRepo frameLogRepo;

    private final FrameSelector frameSelector;

    private final Random random = new Random();

    private static final Logger logger = LoggerFactory.getLogger(FrameService.class);

    private static final int QUEUE_REFRESH_THRESHOLD = 5;

    private final Queue<IndexedMedia> frameQueue = new LinkedList<>();

    public Optional<IndexMediaDto> getNextFrameInfo() {

        if (frameQueue.size() < QUEUE_REFRESH_THRESHOLD) {
            logger.info("Refilling the queue");
            frameSelector.refreshFrameQueue(frameQueue);
        }
        var toShow = frameQueue.poll();
        frameLogRepo.save(FrameLog.builder()
                .indexedMedia(toShow)
                .shownAt(LocalDateTime.now())
                .build());
        return Optional.of(IndexMediaDto.from(toShow));
    }
}
