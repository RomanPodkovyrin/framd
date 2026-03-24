package dev.romanempire.framd.indexing.util;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;

public class ImageTools {
    private static final Set<String>  IMAGE_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "webp", "heic", "bmp", "avif"
    );

    public static Boolean isImage(Path path) {
        var fileName = path.getFileName().toString();

        var dot = fileName.lastIndexOf(".");
        if (dot == -1) return false;

        return IMAGE_EXTENSIONS.contains(fileName.substring(dot +1).toLowerCase());
    }


    public static Optional<Metadata> getMetadata(Path path) {

        Metadata metadata;
        try {
            metadata = ImageMetadataReader.readMetadata(path.toFile());
        } catch (ImageProcessingException | IOException e) {
            System.out.println(e);
            return Optional.empty();
        }
        return Optional.of(metadata); // Todo return my own Metadata object instead
    }
}
