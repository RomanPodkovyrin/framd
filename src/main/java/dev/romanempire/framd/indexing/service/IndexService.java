package dev.romanempire.framd.indexing.service;

import dev.romanempire.framd.extractor.service.MetadataExtractorService;
import dev.romanempire.framd.indexing.impl.Indexer;
import dev.romanempire.framd.repository.IndexedMedia;
import dev.romanempire.framd.repository.IndexedMediaRepo;
import dev.romanempire.framd.extractor.service.PreviewService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@RequiredArgsConstructor
public class IndexService {


    private final AtomicBoolean isScanning = new AtomicBoolean(false);

    private final Indexer indexer;

    private final MetadataExtractorService metadataExtractorService;

    private final PreviewService previewService;

    private final IndexedMediaRepo indexedMediaRepo;

    private static final Logger logger = LoggerFactory.getLogger(IndexService.class);

    @Value("${media.library.path}")
    private String scanPath;

    public boolean tryFullScan() {
        return startFullScan(scanPath, true);
    }

    public boolean tryFullScan(String path) {
        return startFullScan(path, true);
    }

    public boolean tryFullScan(String path, boolean isRecursive) {
        return startFullScan(path, isRecursive);
    }

    private boolean startFullScan(String path, boolean isRecursive) {
        if (!isScanning.compareAndSet(false, true)) {
            logger.error("Failed to start Full Scan, scanning already in progress");
            return false;
        }

        Thread.ofVirtual().start(() -> {
            try {
                List<Path> paths = isRecursive ? walkDirectory(path) : list(path);

                var indexedMedia = extractMetadata(paths);

                // Determine stale and new files
                var indexedMediaToDelete = new HashSet<>(getIndexInfoList());
                var indexedMediaCurrent = new HashSet<>(indexedMediaToDelete);
                var newIndexedMedia = new HashSet<>(indexedMedia);
                indexedMediaToDelete.removeAll(newIndexedMedia);
                newIndexedMedia.removeAll(indexedMediaCurrent);

                logger.info("Will Delete {} stale files, and add {} new ones", indexedMediaToDelete.size(), newIndexedMedia.size());

                indexedMediaRepo.deleteAllByIdInBatch(indexedMediaToDelete.stream().map(IndexedMedia::getHash).toList());

                var indexedMediaWithPreviews = generatePreviews(newIndexedMedia.stream().toList());

                persist(indexedMediaWithPreviews);
            } catch (Exception e) {
                logger.error("Scan Failed: {}", e);
            } finally {
                ;
                logger.debug("Scanning flag is released");
                isScanning.set(false);
            }
        });

        logger.info("Started Indexing for {}", path);

        return true;
    }

    public boolean tryPartialScan(String path) {
        return startPartialScan(path);
    }

    public boolean startPartialScan(String path) {
        if (!isScanning.compareAndSet(false, true)) {
            logger.error("Failed to start partial scan, scanning already in progress");
            return false;
        }

        logger.info("Starting Partial Scan for {}", path);
        Thread.ofVirtual().start(() -> {
            try {
                List<Path> paths = list(path);

                var indexedMedia = extractMetadata(paths);

                // Determine stale and new files
                var indexedMediaToDelete = new HashSet<>(getIndexInfoByPath(path));
                System.out.println(indexedMediaToDelete);
                var indexedMediaCurrent = new HashSet<>(indexedMediaToDelete);
                var newIndexedMedia = new HashSet<>(indexedMedia);
                indexedMediaToDelete.removeAll(newIndexedMedia);
                newIndexedMedia.removeAll(indexedMediaCurrent);

                logger.info("Will Delete {} stale files, and add {} new ones", indexedMediaToDelete.size(), newIndexedMedia.size());
                newIndexedMedia.forEach(System.out::println);

                indexedMediaRepo.deleteAllByIdInBatch(indexedMediaToDelete.stream().map(IndexedMedia::getHash).toList());

                var indexedMediaWithPreviews = generatePreviews(newIndexedMedia.stream().toList());

                persist(indexedMediaWithPreviews);
            } catch (Exception e) {
                logger.error("Scan Failed: {}", e);
            } finally {
                logger.debug("Scanning flag is released");
                isScanning.set(false);
            }
        });

        logger.info("Started Indexing for {}", path);
        return true;
    }

    private List<IndexedMedia> generatePreviews(List<IndexedMedia> indexedMedia) {
        var generateStart = LocalTime.now();
        var indexedMediaWithPreviews = previewService.generatePreviews(indexedMedia);
        var generateEnd = LocalTime.now();
        logger.info("Time to generate previews: {} ms", Duration.between(generateStart, generateEnd).toMillis());
        return indexedMediaWithPreviews;
    }

    private List<IndexedMedia> extractMetadata(List<Path> paths) {
        var hashStart = LocalTime.now();
        var indexedMedia = metadataExtractorService.extractMetadata(paths);
        var hashEnd = LocalTime.now();
        logger.info("Time to Extract metadata: {} ms", Duration.between(hashStart, hashEnd).toMillis());
        return indexedMedia;
    }

    private void persist(List<IndexedMedia> indexedMedia) {
        var persistStart = LocalTime.now();
        indexedMediaRepo.saveAll(indexedMedia);
        var persistEnd = LocalTime.now();
        logger.info("Time to Persist: {} ms", Duration.between(persistStart, persistEnd).toMillis());
    }

    private List<Path> walkDirectory(String path) {
        var indexStart = LocalTime.now();
        List<Path> imageMetadataList = indexer.walk(path);
        var indexEnd = LocalTime.now();
        logger.info("Time to walk: {} ms {} files", Duration.between(indexStart, indexEnd).toMillis(), imageMetadataList.size());
        return imageMetadataList;
    }

    public List<Path> walkAndListDirsRecursively(String path) {
        var indexStart = LocalTime.now();
        List<Path> imageMetadataList = indexer.walkDirRecursively(path);
        var indexEnd = LocalTime.now();
        logger.info("Time to walk: {} ms {} files", Duration.between(indexStart, indexEnd).toMillis(), imageMetadataList.size());
        return imageMetadataList;
    }

    public List<Path> list(String path) {
        var indexStart = LocalTime.now();
        List<Path> imageMetadataList = indexer.list(path);
        var indexEnd = LocalTime.now();
        logger.info("Time to walk: {} ms {} files", Duration.between(indexStart, indexEnd).toMillis(), imageMetadataList.size());
        return imageMetadataList;
    }

    public List<IndexedMedia> getIndexInfoList() {
        return indexedMediaRepo.findAll();
    }

    private List<IndexedMedia> getIndexInfoByPath(String path) {
        return indexedMediaRepo.findAllByPath(path);
    }

    public List<IndexedMedia> getIndexInfoDateOrderedList() {
        return getIndexInfoList()
                .stream()
                .sorted(
                        Comparator
                                .comparing(
                                        IndexedMedia::getCaptureTime,
                                        Comparator.nullsLast(Comparator.reverseOrder()))).toList();
    }

    public Long getCount() {
        return indexedMediaRepo.count();
    }

    public Optional<IndexedMedia> getIndexMediaByHash(String hash) {
        try {
            return Optional.of(indexedMediaRepo.getAllByHash(hash).getFirst());
        } catch (NoSuchElementException e) {
            return Optional.empty();
        }
    }

    public Optional<Path> getPreviewPath(String hash) {
        return getIndexMediaByHash(hash)
                .map(p -> Path.of(p.getPreviewPath()));
    }
}
