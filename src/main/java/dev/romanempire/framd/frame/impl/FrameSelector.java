package dev.romanempire.framd.frame.impl;

import dev.romanempire.framd.repository.FrameLogRepo;
import dev.romanempire.framd.repository.IndexedMediaRepo;
import dev.romanempire.framd.repository.model.FrameLogStats;
import dev.romanempire.framd.repository.model.IndexedMedia;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FrameSelector {
    private final IndexedMediaRepo indexedMediaRepo;
    private final FrameLogRepo frameLogRepo;

    private List<IndexedMedia> getOnThisDay() {
        var today = LocalDate.now();
        return indexedMediaRepo.findAllByCaptureDayAndMonth(
                today.getDayOfMonth(), today.getMonthValue(), today.getYear());
    }

    private float getGroupFatigue(
            Map.Entry<String, List<IndexedMedia>> e, Map<String, FrameLogStats> frameLogStatsMap, float groupSize) {
        return e.getValue().stream()
                        .mapToLong(im -> switch (frameLogStatsMap.get(im.getId())) {
                            case null -> 0;
                            case FrameLogStats fls -> fls.count();
                        })
                        .sum()
                / groupSize;
    }

    private LocalDateTime getGroupLastShown(
            Map.Entry<String, List<IndexedMedia>> e, Map<String, FrameLogStats> frameLogStatsMap) {
        return e.getValue().stream()
                .map(im -> switch (frameLogStatsMap.get(im.getId())) {
                    case null -> LocalDateTime.MIN;
                    case FrameLogStats fls -> fls.lastShown();
                })
                .min(Comparator.naturalOrder())
                .orElse(LocalDateTime.MIN);
    }

    private record GroupStat(
            String groupPath,
            List<IndexedMedia> indexedMedias,
            LocalDateTime groupLastShown,
            Integer groupSize,
            Float groupFatigue) {}

    public boolean refreshFrameQueue(Queue<IndexedMedia> frameQueue) {
        var list = indexedMediaRepo.findAll(Sort.by(Sort.Direction.DESC, "captureTime")).stream()
                .collect(Collectors.groupingBy(IndexedMedia::getPath));
        var frameLogStatsMap =
                frameLogRepo.getLogStats().stream().collect(Collectors.toMap(FrameLogStats::mediaId, m -> m));

        var processList = list.entrySet().stream()
                .map(e -> {
                    var groupLastShown = getGroupLastShown(e, frameLogStatsMap);
                    var groupSize = e.getValue().size();
                    var groupFatigue = getGroupFatigue(e, frameLogStatsMap, (float) groupSize);
                    return new GroupStat(e.getKey(), e.getValue(), groupLastShown, groupSize, groupFatigue);
                })
                .toList();

        var sorted = processList.stream()
                .sorted(Comparator.comparing(GroupStat::groupFatigue))
                .toList();

        var onThisDay = getOnThisDay();
        var others = sorted.stream().flatMap(gs -> gs.indexedMedias.stream()).toList();

        frameQueue.clear();
        frameQueue.addAll(onThisDay);
        frameQueue.addAll(others);
        return true;
    }
}
