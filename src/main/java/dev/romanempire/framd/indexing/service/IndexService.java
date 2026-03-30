package dev.romanempire.framd.indexing.service;

import dev.romanempire.framd.extractor.service.MetadataExtractorService;
import dev.romanempire.framd.indexing.impl.Indexer;
import dev.romanempire.framd.repository.IndexedMedia;
import dev.romanempire.framd.repository.IndexedMediaRepo;
import dev.romanempire.framd.thumbnails.ThumbnailService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class IndexService {

    private final Indexer indexer;

    private final MetadataExtractorService metadataExtractorService;

    private final ThumbnailService thumbnailService;

    private final IndexedMediaRepo indexedMediaRepo;

    private static final Logger logger = LoggerFactory.getLogger(IndexService.class);

    public List<IndexedMedia> indexPath(String path) {

        var indexStart = LocalTime.now();
        List<ImageMetadata> imageMetadataList = indexer.index(path);
        var indexEnd = LocalTime.now();
        logger.info("Time to index: {} ms", Duration.between(indexStart, indexEnd).toMillis());
    public void indexPath(String path) {

        List<Path> paths = walkDirectory(path);
        // should walk it first to get metadata

        var hashStart = LocalTime.now();
        var indexedMedia = metadataExtractorService.extractMetadata(paths);
        var hashEnd = LocalTime.now();
        logger.info("Time to hash: {} ms", Duration.between(hashStart, hashEnd).toMillis());

        var generateStart = LocalTime.now();
        var hashToPath = thumbnailService.generateThumbnails(indexedMedia.stream().collect(Collectors.toMap(IndexedMedia::getFullPath, IndexedMedia::getHash)));
        var generateEnd = LocalTime.now();
        logger.info("Time to generate thumbnails: {} ms", Duration.between(generateStart, generateEnd).toMillis());

        // TODO: dont' like the mutation
        indexedMedia
                .forEach(m -> {
                    m.setThumbnailPath(hashToPath.getOrDefault(m.getHash(), null));
                });


        persist(indexedMedia);
    }

    private void persist(List<IndexedMedia> indexedMedia) {
        // save into db
        var persistStart = LocalTime.now();
        indexedMediaRepo.saveAll(indexedMedia);
        var persistEnd = LocalTime.now();
        logger.info("Time to Persist: {} ms", Duration.between(persistStart, persistEnd).toMillis());
    }

    private List<Path> walkDirectory(String path) {
        var indexStart = LocalTime.now();
        List<Path> imageMetadataList = indexer.walk(path);
        var indexEnd = LocalTime.now();
        logger.info("Time to walk: {} ms", Duration.between(indexStart, indexEnd).toMillis());
        return imageMetadataList;
    }

    public List<IndexedMedia> getIndexInfo() {
        return indexedMediaRepo.findAll();
    }

    public Long getCount() {
        return indexedMediaRepo.count();
    }

    public IndexedMedia getIndexInfo(String hash) {
        return indexedMediaRepo.getAllByHash(hash).getFirst();
    }
}
