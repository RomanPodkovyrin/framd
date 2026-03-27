package dev.romanempire.framd.thumbnails;

import net.coobird.thumbnailator.Thumbnails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

@Service
public class ThumbnailService {

    @Value("${thumbnails.path}")
    private String thumbnailPath;

    private static final Logger logger = LoggerFactory.getLogger(ThumbnailService.class);

    public Map<String, String> generateThumbnails(Map<String, String> hashes) {
        Map<String, String> hashToPath = new HashMap<>();

        logger.info("Generating Thumbnails");

        Path thumbsDir = Path.of(thumbnailPath);
        try {
            Files.createDirectories(thumbsDir); // no-op if already exists
        } catch (IOException e) {
            throw new RuntimeException(e);
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
    }
}
