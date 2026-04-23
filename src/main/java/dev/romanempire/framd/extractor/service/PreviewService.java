package dev.romanempire.framd.extractor.service;

import dev.romanempire.framd.indexing.model.ScanContext;
import dev.romanempire.framd.indexing.model.ScanStage;
import dev.romanempire.framd.repository.model.IndexedMedia;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicLong;
import lombok.RequiredArgsConstructor;
import net.coobird.thumbnailator.Thumbnails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PreviewService {

    @Value("${preview.path}")
    private String previewPath;

    private static final Logger logger = LoggerFactory.getLogger(PreviewService.class);

    private final ScanContext scanContext;

    ///
    /// Generates Previews for each Path passed
    ///
    public void generatePreviews(List<IndexedMedia> indexedMedia) {

        logger.info("Generating Previews");
        var generatedCount = new AtomicLong(0L);

        Path thumbsDir = Path.of(previewPath);
        try {
            Files.createDirectories(thumbsDir); // no-op if already exists
        } catch (IOException e) {
            logger.error("Failed to make Preview Directory {} with error: {}", previewPath, e.toString());

            scanContext.completePersistQueue();
        }

        var semaphore = new Semaphore(20);

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            indexedMedia.forEach(media -> executor.submit(() -> {
                try {
                    semaphore.acquire();
                    var sourcePath = Path.of(media.getFullPath());
                    var outputPath = Path.of(previewPath).resolve(Path.of(media.generatePreviewPathFromHash()));
                    generatePreview(sourcePath, outputPath);
                    scanContext.enqueueForPersistence(media.withPreviewPath(outputPath.toString()));
                    generatedCount.incrementAndGet();
                    scanContext.incrementStageStat(ScanStage.PREVIEW);
                } catch (IOException e) {
                    logger.error("Failed generating preview: {} with error {}", media.getHash(), e.getMessage());
                } catch (InterruptedException e) {
                    logger.error("InterruptedException: {}", e.getMessage());
                } catch (IllegalArgumentException e) {
                    logger.error("Tried inserting illegal argument into queue", e);
                } catch (Exception e) {
                    logger.error("Generic error: ", e);
                } finally {
                    semaphore.release();
                }
            }));
        }

        scanContext.completePersistQueue();

        logger.info("Generated {}/{} files", generatedCount.get(), indexedMedia.size());
    }

    ///
    /// Generates a preview
    /// Will override if already exists
    private void generatePreview(Path sourcePath, Path outputPath) throws IOException {
        Files.createDirectories(outputPath.getParent());
        Thumbnails.of(sourcePath.toFile()).scale(0.3).outputQuality(0.7).toFile(outputPath.toFile());
    }
}
