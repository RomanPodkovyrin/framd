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

    private static final Set<String> IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp", "gif", "bmp", "wbmp");


    private static final Set<String> ILLEGAL_EXTENSIONS = Set.of("", "/", ".", "..");

    public static Boolean isImage(Path path) {
        FileNameParts fileNameParts;
        try {
            fileNameParts = getFileParts(path);
        } catch (IllegalArgumentException ex) {
            logger.error(ex.getMessage());
            return false;
        }

        System.out.println(fileNameParts.extension);


        return IMAGE_EXTENSIONS.contains(fileNameParts.extension().toLowerCase());
    }

    public static FileNameParts getFileParts(Path path) throws IllegalArgumentException {
        if (ILLEGAL_EXTENSIONS.contains(path.toString())) {
            throw new IllegalArgumentException("Invalid file name");
        }
        var fileName = path.getFileName().toString();

        var dotIndex = fileName.lastIndexOf(".");
        if (dotIndex == -1) {
            // no extension
            return new FileNameParts(fileName, "");
        } else if (dotIndex == 0) { // hidden file without extension
            // There are no other "." in the string
            // safe to assume no extension
            return new FileNameParts(fileName.substring(1), "");
        }


        var name = fileName.substring(0, dotIndex);
        var extension = fileName.substring(dotIndex + 1);
        return new FileNameParts(name.charAt(0) == '.' ? name.substring(1) : name, extension);
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
