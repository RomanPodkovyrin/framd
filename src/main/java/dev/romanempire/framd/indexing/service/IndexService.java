package dev.romanempire.framd.indexing.service;

import dev.romanempire.framd.extractor.service.MetadataExtractorService;
import dev.romanempire.framd.indexing.impl.Indexer;
import dev.romanempire.framd.repository.IndexedMedia;
import dev.romanempire.framd.repository.IndexedMediaRepo;
import dev.romanempire.framd.extractor.service.PreviewService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class IndexService {


    private final AtomicBoolean isScanning = new AtomicBoolean(false);

    private final Indexer indexer;

    private final MetadataExtractorService metadataExtractorService;

    private final PreviewService previewService;

    private final IndexedMediaRepo indexedMediaRepo;

    private static final Logger logger = LoggerFactory.getLogger(IndexService.class);

    public boolean tryIndexPath(String path) {

        if (!isScanning.compareAndSet(false, true)) {
            logger.error("failed to start scanning, scanning already in progress");
            return false;
        }

        Thread.ofVirtual().start(() -> {
            try {
                List<Path> paths = walkDirectory(path);

                var indexedMedia = Extraction(paths);

                var indexedMediaWithPreviews = generatePreviews(indexedMedia);



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

    private List<IndexedMedia> generatePreviews(List<IndexedMedia> indexedMedia) {
        var generateStart = LocalTime.now();
        var indexedMediaWithPreviews = previewService.generatePreviews(indexedMedia);
        var generateEnd = LocalTime.now();
        logger.info("Time to generate previews: {} ms", Duration.between(generateStart, generateEnd).toMillis());
        return indexedMediaWithPreviews;
    }

    private List<IndexedMedia> Extraction(List<Path> paths) {
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

    public List<IndexedMedia> getIndexInfoDateOrderedList() {
        return indexedMediaRepo
                .findAll()
                .stream()
                .sorted(
                        Comparator
                                .comparing(
                                        IndexedMedia::getCaptureTime,
                                        Comparator.nullsLast(Comparator.naturalOrder()))).toList();
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
