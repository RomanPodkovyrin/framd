package dev.romanempire.framd.frame.impl;

import dev.romanempire.framd.repository.FrameLogRepo;
import dev.romanempire.framd.repository.IndexedMediaRepo;
import dev.romanempire.framd.repository.model.FrameLogStats;
import dev.romanempire.framd.repository.model.IndexedMedia;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private LocalDateTime lastOnThisDay = LocalDate.MIN.atStartOfDay();
    private static final Logger logger = LoggerFactory.getLogger(FrameSelector.class);

    private List<IndexedMedia> getOnThisDay(Map<String, FrameLogStats> frameLogStatsMap) {
        var today = LocalDate.now();
        // filter out images from today
        var onThisDayList = indexedMediaRepo
                .findAllByCaptureDayAndMonth(today.getDayOfMonth(), today.getMonthValue(), today.getYear());

        return enhanceFolderGroupDistribution(frameLogStatsMap, onThisDayList);
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
        var folderGroupList = indexedMediaRepo.findAll(Sort.by(Sort.Direction.ASC, "captureTime")).stream()
                .collect(Collectors.groupingBy(IndexedMedia::getPath));
        var frameLogStatsMap =
                frameLogRepo.getLogStats().stream().collect(Collectors.toMap(FrameLogStats::mediaId, m -> m));

        var enhancedGroupsList = new ArrayList<>(getEnhancedGroupsList(folderGroupList, frameLogStatsMap));
        Collections.shuffle(enhancedGroupsList);

        var sorted = enhancedGroupsList.stream()
                .sorted(Comparator.comparing(GroupStat::groupFatigue))
                .limit(MAXIMUM_NUMBER_GROUPS)
                .toList();

        System.out.println("Groups to show");
        sorted.forEach(s -> System.out.println(s.groupPath));

        List<IndexedMedia> onThisDay = List.of();
        if (!lastOnThisDay.isEqual(LocalDate.now().atStartOfDay()) ){
            logger.info("Generating on this day");
            onThisDay = getOnThisDay(frameLogStatsMap);
            lastOnThisDay = LocalDate.now().atStartOfDay();
        }
        var others = sorted.stream().flatMap(gs -> gs.indexedMedias.stream()).toList();

        frameQueue.addAll(onThisDay);
        frameQueue.addAll(others);
        return true;
    }

    private List<GroupStat> getEnhancedGroupsList(
            Map<String, List<IndexedMedia>> folderGroupList, Map<String, FrameLogStats> frameLogStatsMap) {

        return folderGroupList.entrySet().stream()
                .map(e -> {
                    var folderName = e.getKey();
                    var folderMedias = e.getValue();
                    logger.info("Enhancing Group {} with {} items", folderName, folderMedias.size());
                    var groupLastShown = getGroupLastShown(e, frameLogStatsMap);
                    var indexedMedias = enhanceFolderGroupDistribution(frameLogStatsMap, folderMedias);
                    var groupSize = indexedMedias.size();
                    var groupFatigue = getGroupFatigue(indexedMedias, frameLogStatsMap, groupSize);
                    return new GroupStat(folderName, indexedMedias, groupLastShown, groupSize, groupFatigue);
                })
                .toList();
    }

    /// Selects up to [MAXIMUM_GROUP_SIZE] images from a folder group by dividing the list into
    /// equal buckets and picking the least-fatigued image from each bucket. This ensures temporal
    /// spread across the selected images rather than clustering around the lowest-fatigue items.
    ///
    /// **Requires:** `groupList` must be pre-sorted by capture time so that each bucket represents
    /// a distinct time period.
    ///
    /// @param frameLogStatsMap map of media ID to fatigue stats, used to pick the least-shown image per bucket
    /// @param groupList        images in a single folder group, must be pre-sorted by capture time
    /// @return a list of up to [MAXIMUM_GROUP_SIZE] images with temporal spread, or the original list if it is
    // small enough
    private static List<IndexedMedia> enhanceFolderGroupDistribution(
            Map<String, FrameLogStats> frameLogStatsMap, List<IndexedMedia> groupList) {

        var imgNumber = groupList.size();
        if (imgNumber <= MAXIMUM_GROUP_SIZE) return groupList;
        var distributed = IntStream.range(0, MAXIMUM_GROUP_SIZE)
                .mapToObj(i -> {
                    var start = (i * imgNumber) / MAXIMUM_GROUP_SIZE;
                    var end = ((i + 1) * imgNumber) / MAXIMUM_GROUP_SIZE;
                    return groupList.subList(start, end).stream()
                            .sorted(byFatigueCount(frameLogStatsMap))
                            .toList()
                            .getFirst();
                })
                .toList();
        logger.info("Distributed from {} to {}", imgNumber, distributed.size());
        distributed.forEach(d -> System.out.println(d.getCaptureTime()));
        return distributed;
    }

    private static Comparator<IndexedMedia> byFatigueCount(Map<String, FrameLogStats> frameLogStatsMap) {
        return Comparator.comparing(im -> switch (frameLogStatsMap.get(im.getId())) {
            case null -> 0L;
            case FrameLogStats fls -> fls.count();
        });
    }

    private static List<IndexedMedia> enhanceFolderGroup(
            Map<String, FrameLogStats> frameLogStatsMap, List<IndexedMedia> groupList) {
        return groupList.size() > 10
                ? groupList.stream()
                        .sorted(byFatigueCount(frameLogStatsMap))
                        .limit(MAXIMUM_GROUP_SIZE)
                        .sorted(COMPARISON_BY_CAPTURE_DATE_NATURAL_ORDER)
                        .toList()
                : groupList;
    }
}
