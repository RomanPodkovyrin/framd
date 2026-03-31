package dev.romanempire.framd.extractor.util;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;

public class ImageTools {

    private static final Logger logger = LoggerFactory.getLogger(ImageTools.class);

    public record FileNameParts(String name, String extension) {
    }

    private static final Set<String> IMAGE_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "webp", "heic"
    );

    public static Boolean isImage(Path path) {
        FileNameParts fileNameParts = getFileParts(path);


        return IMAGE_EXTENSIONS.contains(fileNameParts.extension().toLowerCase());
    }

    public static FileNameParts getFileParts(Path path) {
        var fileName = path.getFileName().toString();

        var dotIndex = fileName.lastIndexOf(".");
        if (dotIndex == -1) {
            // no extension
            return new FileNameParts(fileName, "");
        }
        var name = fileName.substring(0, dotIndex);
        var extension = fileName.substring(dotIndex + 1);
        return new FileNameParts(name, extension);
    }


    public static Optional<Metadata> getMetadata(Path path) {

        try {
            Metadata metadata = ImageMetadataReader.readMetadata(path.toFile());
            return Optional.of(metadata);
        } catch (ImageProcessingException | IOException e) {
            logger.error(e.getMessage());
            return Optional.empty();
        }
    }
}
