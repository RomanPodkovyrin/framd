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
    public static final Comparator<IndexedMedia> COMPARISON_BY_CAPTURE_DATE_NATURAL_ORDER =
            Comparator.comparing(IndexedMedia::getCaptureTime, Comparator.nullsLast(Comparator.naturalOrder()));
    private final IndexedMediaRepo indexedMediaRepo;
    private final FrameLogRepo frameLogRepo;

    public static final int MAXIMUM_GROUP_SIZE = 10;
    public static final int MAXIMUM_NUMBER_GROUPS = 10;
    public static final int MAXIMUM_ON_TODAY_SIZE = 10;

    private List<IndexedMedia> getOnThisDay(Map<String, FrameLogStats> frameLogStatsMap) {
        var today = LocalDate.now();
        // filter out images from today
        return indexedMediaRepo
                .findAllByCaptureDayAndMonth(today.getDayOfMonth(), today.getMonthValue(), today.getYear())
                .stream()
                .sorted(Comparator.comparing(im -> switch (frameLogStatsMap.get(im.getId())) {
                    case null -> 0L;
                    case FrameLogStats fls -> fls.count();
                }))
                .limit(MAXIMUM_ON_TODAY_SIZE)
                .sorted(COMPARISON_BY_CAPTURE_DATE_NATURAL_ORDER)
                .toList();
    }

    private float getGroupFatigue(
            List<IndexedMedia> mediaList, Map<String, FrameLogStats> frameLogStatsMap, float groupSize) {
        return mediaList.stream()
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
        var folderGroupList = indexedMediaRepo.findAll(Sort.by(Sort.Direction.DESC, "captureTime")).stream()
                .collect(Collectors.groupingBy(IndexedMedia::getPath));
        var frameLogStatsMap =
                frameLogRepo.getLogStats().stream().collect(Collectors.toMap(FrameLogStats::mediaId, m -> m));

        var enhancedGroupsList = getEnhancedGroupsList(folderGroupList, frameLogStatsMap);

        var sorted = enhancedGroupsList.stream()
                .sorted(Comparator.comparing(GroupStat::groupFatigue))
                .limit(MAXIMUM_NUMBER_GROUPS)
                .toList();

        var onThisDay = getOnThisDay(frameLogStatsMap);
        var others = sorted.stream().flatMap(gs -> gs.indexedMedias.stream()).toList();

        frameQueue.clear();
        frameQueue.addAll(onThisDay);
        frameQueue.addAll(others);
        return true;
    }

    private List<GroupStat> getEnhancedGroupsList(
            Map<String, List<IndexedMedia>> folderGroupList, Map<String, FrameLogStats> frameLogStatsMap) {
        return folderGroupList.entrySet().stream()
                .map(e -> {
                    var groupLastShown = getGroupLastShown(e, frameLogStatsMap);
                    var indexedMedias = e.getValue().size() > 10
                            ? e.getValue().stream()
                                    .sorted(Comparator.comparing(im -> switch (frameLogStatsMap.get(im.getId())) {
                                        case null -> 0L;
                                        case FrameLogStats fls -> fls.count();
                                    }))
                                    .limit(MAXIMUM_GROUP_SIZE)
                                    .sorted(COMPARISON_BY_CAPTURE_DATE_NATURAL_ORDER)
                                    .toList()
                            : e.getValue();
                    var groupSize = indexedMedias.size();
                    var groupFatigue = getGroupFatigue(indexedMedias, frameLogStatsMap, groupSize);
                    return new GroupStat(e.getKey(), indexedMedias, groupLastShown, groupSize, groupFatigue);
                })
                .toList();
    }
}
