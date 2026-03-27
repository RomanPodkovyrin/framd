package dev.romanempire.framd.indexing.service;

import dev.romanempire.framd.hasher.service.HasherService;
import dev.romanempire.framd.indexing.impl.Indexer;
import dev.romanempire.framd.indexing.model.ImageMetadata;
import dev.romanempire.framd.repository.IndexedMedia;
import dev.romanempire.framd.repository.IndexedMediaRepo;
import dev.romanempire.framd.thumbnails.ThumbnailService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IndexService {

    private final Indexer indexer;

    private final HasherService hasherService;

    private final ThumbnailService thumbnailService;

    private final IndexedMediaRepo indexedMediaRepo;

    private static final Logger logger = LoggerFactory.getLogger(IndexService.class);

    public List<IndexedMedia> indexPath(String path) {

        var indexStart = LocalTime.now();
        List<ImageMetadata> imageMetadataList = indexer.index(path);
        var indexEnd = LocalTime.now();
        logger.info("Time to index: {} ms", Duration.between(indexStart, indexEnd).toMillis());


        // should walk it first to get metadata

        var hashStart = LocalTime.now();
        var hashes = hasherService.hashFiles(imageMetadataList.stream().map(ImageMetadata::path).toList());
        var hashEnd = LocalTime.now();
        logger.info("Time to hash: {} ms", Duration.between(hashStart, hashEnd).toMillis());

        var generateStart = LocalTime.now();
        var hashToPath = thumbnailService.generateThumbnails(hashes);
        var generateEnd = LocalTime.now();
        logger.info("Time to generate thumbnails: {} ms", Duration.between(generateStart, generateEnd).toMillis());


        // save into db
        var persistStart = LocalTime.now();
        var indexedMedia = imageMetadataList
                .stream().map(m -> {
                    var hash = hashes.get(m.path().toString());
                    return IndexedMedia.builder()
                            .hash(hash)
                            .path(m.parentPath())
                            .name(m.fileName())
                            .extension(m.extension())
                            .captureTime(m.dateTaken().orElse(LocalDateTime.now())) // todo: not good, need to change, remove optinal
                            .lastIndexedTime(LocalDateTime.now())
                            .lastModifiedTime(LocalDateTime.now())
                            .width(1000)
                            .height(1000)
                            .sizeInBytes(1200L)
                            .thumbnailPath(hashToPath.getOrDefault(hash, null))
                            .build();
                }).toList();
        indexedMediaRepo.saveAll(indexedMedia);
        var persistEnd = LocalTime.now();
        logger.info("Time to Persist: {} ms", Duration.between(persistStart, persistEnd).toMillis());

        // this would be made with virtual threads and a semaphore to limit amount of threads

        return indexedMedia;
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
