package dev.romanempire.framd.extractor.service;

import dev.romanempire.framd.extractor.impl.FileHasher;
import dev.romanempire.framd.indexing.model.ExifData;
import dev.romanempire.framd.indexing.util.ImageTools;
import dev.romanempire.framd.repository.IndexedMedia;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

@Service
@RequiredArgsConstructor
public class MetadataExtractorService {

    private static final Logger logger = LoggerFactory.getLogger(MetadataExtractorService.class);

    public List<IndexedMedia> extractMetadata(List<Path> paths) {

        var semaphore = new Semaphore(20);

        logger.info("Start Hashing Files");
        List<IndexedMedia> indexedMedia = Collections.synchronizedList(new ArrayList<>());

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            paths.forEach(p ->
                    executor.submit(() -> {
                        try {
                            semaphore.acquire();
                            ExifData exifData = ImageTools.getMetadata(p).get();
                            var hash = FileHasher.hashFile(p).get();

                            indexedMedia.add(IndexedMedia.builder()
                                    .hash(hash)
                                    .path(exifData.parentPath())
                                    .name(exifData.fileName())
                                    .extension(exifData.extension())
                                    .captureTime(exifData.dateTaken().orElse(LocalDateTime.now())) // todo: not good, need to change, remove optional
                                    .lastIndexedTime(LocalDateTime.now()) //Todo
                                    .lastModifiedTime(LocalDateTime.now()) // todo
                                    .width(1000)// todo
                                    .height(1000)// todo
                                    .sizeInBytes(1200L) // todo
//                                    .thumbnailPath(null)
                                    .build());

                        } catch (InterruptedException e) {
                            logger.error("Semaphore Interrupted: {}", e.getMessage());
                        } finally {
                            semaphore.release();
                        }

                    }));
        }
        return indexedMedia;
    }

}
