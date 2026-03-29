package dev.romanempire.framd.thumbnails;

import net.coobird.thumbnailator.Thumbnails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

@Service
public class ThumbnailService {


    @Value("${thumbnails.path}")
    private String thumbnailPath;

    private static final Logger logger = LoggerFactory.getLogger(ThumbnailService.class);


    ///
    /// Generates Thumbnails for each Path passed
    ///
    /// @param hashesByPath Map of String path to String Hash
    /// @return Map of String hash to generated thumbnail path
    public Map<String, String> generateThumbnails(Map<String, String> hashesByPath) {
        Map<String, String> thumbnailPathsByHash = new ConcurrentHashMap<>();

        logger.info("Generating Thumbnails");

        Path thumbsDir = Path.of(thumbnailPath);
        try {
            Files.createDirectories(thumbsDir); // no-op if already exists
        } catch (IOException e) {
            logger.error("Failed to make Thumbnail Directory {} with error: {}", thumbnailPath, e.getMessage());
            return thumbnailPathsByHash;
        }

        hashes.forEach((path, hash) -> {
            try {
                var p = Path.of(path);
                var saveDirection = thumbnailPath + p.getParent();
                var saveLocation = saveDirection + "/" + p.getFileName();
                Files.createDirectories(Path.of(saveDirection));
                Thumbnails.of(p.toFile())
                        .scale(0.3)
                        .outputQuality(0.7)
                        .toFile(saveLocation); // can overwrite file with the same filename

                hashToPath.put(hash, saveLocation);
            } catch (IOException e) {
                logger.error(String.valueOf(e));
            }
        });
        return hashToPath;
        var semaphore = new Semaphore(20);


        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            hashesByPath.forEach((path, hash) ->
                    executor.submit(() -> {
                        try {
                            semaphore.acquire();
                            var outputPath = generateThumbnail(path);
                            thumbnailPathsByHash.put(hash, outputPath.toString());
                        } catch (IOException e) {
                            logger.error("Failed generating hash: {} with error {}", hash, e.getMessage());
                        } catch (InterruptedException e) {
                            logger.error("Semaphore acquire got interrupted: {}", e.getMessage());
                        } finally {
                            semaphore.release();
                        }
                    }));
        }
        logger.info("Generated {}/{} files", thumbnailPathsByHash.size(), hashesByPath.size());
        return thumbnailPathsByHash;
    }

    private Path generateThumbnail(String path) throws IOException {
        var sourcePath = Path.of(path);
        var outputDir = Path.of(thumbnailPath).resolve(sourcePath.getRoot().relativize(sourcePath.getParent()));
        var outputPath = outputDir.resolve(sourcePath.getFileName());
        Files.createDirectories(outputDir);
        Thumbnails.of(sourcePath.toFile())
                .scale(0.3)
                .outputQuality(0.7)
                .toFile(outputPath.toFile());
        return outputPath;
    }
}
