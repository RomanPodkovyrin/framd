package dev.romanempire.framd.thumbnails;

import net.coobird.thumbnailator.Thumbnails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Service
public class ThumbnailService {

    @Value("${thumbnails.path}")
    private String thumbnailPath;

    private static final Logger logger = LoggerFactory.getLogger(ThumbnailService.class);

    public void generateThumbnails(List<Path> paths) {

        logger.info("Generating Thumbnails");

        Path thumbsDir = Path.of(thumbnailPath);
        try {
            Files.createDirectories(thumbsDir); // no-op if already exists
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        paths.forEach(p -> {
            try {
                var saveLocation = thumbnailPath+p.getParent() + "/";
                Files.createDirectories(Path.of(saveLocation));
                Thumbnails.of(p.toFile())
                        .scale(0.3)
                        .outputQuality(0.7)
                        .toFile( saveLocation+p.getFileName()); // can overwrite file with the same filename
            } catch (IOException e) {
                logger.error(String.valueOf(e));
            }
        });
    }
}
