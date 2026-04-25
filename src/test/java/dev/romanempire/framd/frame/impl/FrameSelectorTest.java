package dev.romanempire.framd.frame.impl;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

import dev.romanempire.framd.repository.FrameLogRepo;
import dev.romanempire.framd.repository.IndexedMediaRepo;
import dev.romanempire.framd.repository.model.FrameLogStats;
import dev.romanempire.framd.repository.model.IndexedMedia;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

@ExtendWith(MockitoExtension.class)
class FrameSelectorTest {

    @Mock
    IndexedMediaRepo indexedMediaRepo;

    @Mock
    FrameLogRepo frameLogRepo;

    @InjectMocks
    FrameSelector frameSelector;

    @Test
    void refreshFrameQueue_returnsEmpty() {
        when(frameLogRepo.getLogStats()).thenReturn(List.of());

        when(indexedMediaRepo.findAllByCaptureDayAndMonth(anyInt(), anyInt(), anyInt()))
                .thenReturn(List.of());

        when(indexedMediaRepo.findAll(Sort.by(Sort.Direction.ASC, "captureTime")))
                .thenReturn(List.of());

        Queue<IndexedMedia> queue = new LinkedList<>();

        frameSelector.refreshFrameQueue(queue);

        assertTrue(queue.isEmpty());
    }

    @Test
    void refreshFrameQueue_returnsSingle() {
        when(frameLogRepo.getLogStats())
                .thenReturn(List.of(
                        new FrameLogStats("1", 1L, LocalDateTime.of(2026, 1, 3, 10, 0)),
                        new FrameLogStats("2", 3L, LocalDateTime.of(2026, 2, 3, 10, 0))));

        when(indexedMediaRepo.findAllByCaptureDayAndMonth(anyInt(), anyInt(), anyInt()))
                .thenReturn(List.of());

        when(indexedMediaRepo.findAll(Sort.by(Sort.Direction.ASC, "captureTime")))
                .thenReturn(List.of(
                        IndexedMedia.builder().hash("abc").path("/birthday").build()));

        Queue<IndexedMedia> queue = new LinkedList<>();

        frameSelector.refreshFrameQueue(queue);

        assertFalse(queue.isEmpty());
        assertThat(queue).anyMatch(im -> im.getHash().equals("abc"));
    }

    @Test
    void refreshFrameQueue_onThisDayItemsAreFirst() {
        var onThisDay = IndexedMedia.builder()
                .id("onThisDay")
                .hash("onThisDay")
                .path("/onThisDay")
                .build();
        var notOnThisDay = IndexedMedia.builder()
                .id("notOnThisDay")
                .hash("notOnThisDay")
                .path("/notOnThisDay")
                .build();

        when(indexedMediaRepo.findAllByCaptureDayAndMonth(anyInt(), anyInt(), anyInt()))
                .thenReturn(List.of(onThisDay));
        when(indexedMediaRepo.findAll(any(Sort.class))).thenReturn(List.of(notOnThisDay));
        when(frameLogRepo.getLogStats()).thenReturn(List.of());

        Queue<IndexedMedia> queue = new LinkedList<>();
        frameSelector.refreshFrameQueue(queue);

        assertEquals(2, queue.size());
        assertThat(queue.poll()).isEqualTo(onThisDay);
    }

    @Test
    void refreshFrameQueue_lowerFatigueGroupAppearsFirst() {
        var tired = IndexedMedia.builder().id("1").hash("h1").path("/tired").build();
        var fresh = IndexedMedia.builder().id("2").hash("h2").path("/fresh").build();

        when(indexedMediaRepo.findAllByCaptureDayAndMonth(anyInt(), anyInt(), anyInt()))
                .thenReturn(List.of());
        when(indexedMediaRepo.findAll(any(Sort.class))).thenReturn(List.of(tired, fresh));
        when(frameLogRepo.getLogStats())
                .thenReturn(List.of(
                        new FrameLogStats("1", 10L, LocalDateTime.now()), // high fatigue
                        new FrameLogStats("2", 1L, LocalDateTime.now()) // low fatigue
                        ));

        Queue<IndexedMedia> queue = new LinkedList<>();
        frameSelector.refreshFrameQueue(queue);

        assertEquals(2, queue.size());
        assertThat(queue.poll()).isEqualTo(fresh);
    }

    @Test
    void refreshFrameQueue_itemsGroupedByFolder() {
        var folder1a =
                IndexedMedia.builder().id("1a").hash("h1a").path("/folder1").build();
        var folder1b =
                IndexedMedia.builder().id("1b").hash("h1b").path("/folder1").build();
        var folder2a =
                IndexedMedia.builder().id("2a").hash("h2a").path("/folder2").build();
        var folder2b =
                IndexedMedia.builder().id("2b").hash("h2b").path("/folder2").build();

        when(indexedMediaRepo.findAllByCaptureDayAndMonth(anyInt(), anyInt(), anyInt()))
                .thenReturn(List.of());
        when(indexedMediaRepo.findAll(any(Sort.class))).thenReturn(List.of(folder1a, folder2b, folder2a, folder1b));
        when(frameLogRepo.getLogStats())
                .thenReturn(List.of(
                        new FrameLogStats("2a", 10L, LocalDateTime.now()) // high fatigue
                        ));

        Queue<IndexedMedia> queue = new LinkedList<>();
        frameSelector.refreshFrameQueue(queue);

        assertThat(new ArrayList<>(queue))
                .extracting(IndexedMedia::getPath)
                .containsExactly("/folder1", "/folder1", "/folder2", "/folder2");
    }

    @Test
    void refreshFrameQueue_maintainMaxGroupSize() {
        var files = IntStream.range(0, FrameSelector.MAXIMUM_GROUP_SIZE * 2)
                .boxed()
                .map(i -> IndexedMedia.builder()
                        .id(i.toString())
                        .hash(i.toString())
                        .path("/folder")
                        .build())
                .toList();

        when(indexedMediaRepo.findAllByCaptureDayAndMonth(anyInt(), anyInt(), anyInt()))
                .thenReturn(List.of());
        when(indexedMediaRepo.findAll(any(Sort.class))).thenReturn(files);
        when(frameLogRepo.getLogStats()).thenReturn(List.of());

        Queue<IndexedMedia> queue = new LinkedList<>();
        frameSelector.refreshFrameQueue(queue);

        assertEquals(FrameSelector.MAXIMUM_GROUP_SIZE, queue.size());
    }

    @Test
    void refreshFrameQueue_distributeImagesFromGroup() {
        var files = IntStream.range(0, FrameSelector.MAXIMUM_ON_TODAY_SIZE * 2)
                .boxed()
                .map(i -> IndexedMedia.builder()
                        .id(i.toString())
                        .hash(i.toString())
                        .path("/folder")
                        .captureTime(LocalDateTime.now())
                        .build())
                .toList();

        var lastHalfIsFatigued = IntStream.range(0, FrameSelector.MAXIMUM_ON_TODAY_SIZE * 2)
                .boxed()
                .filter(i -> i > files.size() / 2)
                .map(i -> new FrameLogStats(i.toString(), 10L, LocalDateTime.now()))
                .toList();

        when(indexedMediaRepo.findAllByCaptureDayAndMonth(anyInt(), anyInt(), anyInt()))
                .thenReturn(List.of());
        when(indexedMediaRepo.findAll(any(Sort.class))).thenReturn(files);
        when(frameLogRepo.getLogStats()).thenReturn(lastHalfIsFatigued);

        Queue<IndexedMedia> queue = new LinkedList<>();
        frameSelector.refreshFrameQueue(queue);

        assertEquals(FrameSelector.MAXIMUM_ON_TODAY_SIZE, queue.size());

        assertThat(queue)
                .allMatch(
                        im -> Integer.parseInt(im.getId()) % 2 == 0,
                        "Should only return even ids distributed across the group");
    }

    @Test
    void refreshFrameQueue_maintainMaximumSizeOnThisDay() {
        var files = IntStream.range(0, FrameSelector.MAXIMUM_ON_TODAY_SIZE * 2)
                .boxed()
                .map(i -> IndexedMedia.builder()
                        .id(i.toString())
                        .hash(i.toString())
                        .path("/folder")
                        .build())
                .toList();

        when(indexedMediaRepo.findAllByCaptureDayAndMonth(anyInt(), anyInt(), anyInt()))
                .thenReturn(files);
        when(indexedMediaRepo.findAll(any(Sort.class))).thenReturn(List.of());
        when(frameLogRepo.getLogStats()).thenReturn(List.of());

        Queue<IndexedMedia> queue = new LinkedList<>();
        frameSelector.refreshFrameQueue(queue);

        assertEquals(FrameSelector.MAXIMUM_ON_TODAY_SIZE, queue.size());
    }

    @Test
    void refreshFrameQueue_maintainMaxNumberOfGroups() {
        var files = IntStream.range(0, FrameSelector.MAXIMUM_NUMBER_GROUPS * 2)
                .boxed()
                .map(i -> IndexedMedia.builder()
                        .id(i.toString())
                        .hash(i.toString())
                        .path("/folder" + i)
                        .build())
                .toList();

        when(indexedMediaRepo.findAllByCaptureDayAndMonth(anyInt(), anyInt(), anyInt()))
                .thenReturn(List.of());
        when(indexedMediaRepo.findAll(any(Sort.class))).thenReturn(files);
        when(frameLogRepo.getLogStats()).thenReturn(List.of());

        Queue<IndexedMedia> queue = new LinkedList<>();
        frameSelector.refreshFrameQueue(queue);

        assertEquals(FrameSelector.MAXIMUM_NUMBER_GROUPS, queue.size());
    }
}
