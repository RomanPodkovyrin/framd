package dev.romanempire.framd.extractor.service;

import dev.romanempire.framd.repository.IndexedMedia;
import net.coobird.thumbnailator.Thumbnails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

@Service
public class PreviewService {


    @Value("${preview.path}")
    private String previewPath;

    private static final Logger logger = LoggerFactory.getLogger(PreviewService.class);


    ///
    /// Generates Previews for each Path passed
    ///
    public List<IndexedMedia> generatePreviews(List<IndexedMedia> indexedMedia) {
        List<IndexedMedia> indexedMediaWithPreview = Collections.synchronizedList(new ArrayList<>());

        logger.info("Generating Previews");

        Path thumbsDir = Path.of(previewPath);
        try {
            Files.createDirectories(thumbsDir); // no-op if already exists
        } catch (IOException e) {
            logger.error("Failed to make Preview Directory {} with error: {}", previewPath, e.getMessage());
            return indexedMedia;
        }

        var semaphore = new Semaphore(20);


        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            indexedMedia.forEach(media ->
                    executor.submit(() -> {
                        try {
                            semaphore.acquire();
                            var outputPath = generatePreview(media.getFullPath());
                            indexedMediaWithPreview.add(media.withPreviewPath(outputPath.toString()));
                        } catch (IOException e) {
                            logger.error("Failed generating hash: {} with error {}", media.getHash(), e.getMessage());
                        } catch (InterruptedException e) {
                            logger.error("Semaphore acquire got interrupted: {}", e.getMessage());
                        } finally {
                            semaphore.release();
                        }
                    }));
        }
        logger.info("Generated {}/{} files", indexedMediaWithPreview.size(), indexedMediaWithPreview.size());
        return indexedMediaWithPreview;
    }


    ///
    /// Generates a preview
    /// Will override if already exists
    private Path generatePreview(String path) throws IOException {
        var sourcePath = Path.of(path);
        var outputDir = Path.of(previewPath).resolve(sourcePath.getRoot().relativize(sourcePath.getParent()));
        var outputPath = outputDir.resolve(sourcePath.getFileName());
        Files.createDirectories(outputDir);
        Thumbnails.of(sourcePath.toFile())
                .scale(0.3)
                .outputQuality(0.7)
                .toFile(outputPath.toFile());
        return outputPath;
    }
}
