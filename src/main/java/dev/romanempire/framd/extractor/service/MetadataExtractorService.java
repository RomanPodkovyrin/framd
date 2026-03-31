package dev.romanempire.framd.extractor.service;

import dev.romanempire.framd.extractor.impl.FileHasher;
import dev.romanempire.framd.indexing.model.ExifData;
import dev.romanempire.framd.extractor.util.ImageTools;
import dev.romanempire.framd.repository.IndexedMedia;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

@Service
@RequiredArgsConstructor
public class MetadataExtractorService {

    private static final Logger logger = LoggerFactory.getLogger(MetadataExtractorService.class);

    public List<IndexedMedia> extractMetadata(List<Path> paths) {

        var semaphore = new Semaphore(20);

        logger.info("Start Metadata Extraction of {} files", paths.size());
        List<IndexedMedia> indexedMedia = Collections.synchronizedList(new ArrayList<>());

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            paths.forEach(path ->
                    executor.submit(() -> {
                        try {
                            semaphore.acquire();
                            ExifData exifData = ImageTools.getMetadata(p).get();
                            var hash = FileHasher.hashFile(p).get();
                            Optional<ExifData> exifDataOptional =  ExifData.from(path);
                            Optional<String> hashOptional = FileHasher.hashFile(path);

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
                            if (exifDataOptional.isPresent() && hashOptional.isPresent()){
                                var exif = exifDataOptional.get();
                                ImageTools.FileNameParts fileNameParts = ImageTools.getFileParts(path);
                                indexedMedia.add(IndexedMedia.builder()
                                        .hash(hashOptional.get())
                                        .path(path.getParent().toString())
                                        .name(fileNameParts.name())
                                        .extension(fileNameParts.extension())
                                        .captureTime(exif.getCaptureDate().orElse(null))
                                        .lastIndexedTime(LocalDateTime.now())
//                                        .lastModifiedTime(LocalDateTime.now()) // todo
                                        .width(exif.getWidth().orElse(null))
                                        .height(exif.getHeight().orElse(null))
                                        .sizeInBytes(Files.size(path))
//                                    .thumbnailPath(null)
                                        .build());
                            } else {
                                logger.error("Exif or hash not made for {}", path);
                            }


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
