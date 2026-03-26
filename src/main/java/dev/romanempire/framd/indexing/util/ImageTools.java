package dev.romanempire.framd.indexing.util;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import dev.romanempire.framd.indexing.model.ImageMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.Set;

public class ImageTools {

    private static final Logger logger = LoggerFactory.getLogger(ImageTools.class);

    private record FileNameParts(String name, String extension) {}

    private static final Set<String>  IMAGE_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "webp", "heic"
    );

    public static Boolean isImage(Path path) {
        FileNameParts fileNameParts = getFileParts(path);


        return IMAGE_EXTENSIONS.contains(fileNameParts.extension().toLowerCase());
    }

    private static FileNameParts getFileParts(Path path) {
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


    public static Optional<ImageMetadata> getMetadata(Path path) {

        Metadata metadata;
        try {
            metadata = ImageMetadataReader.readMetadata(path.toFile());
        } catch (ImageProcessingException | IOException e) {
            logger.error(e.getMessage());
            return Optional.empty();
        }

        var optionalDate = getFileDate(metadata);


        FileNameParts fileNameParts = getFileParts(path);

        return Optional.of(new ImageMetadata(
                optionalDate,
                path.getParent().toString(),
                fileNameParts.name(),
                fileNameParts.extension(),
                path
        ));
    }

    private static Optional<LocalDateTime> getFileDate(Metadata metadata) {
        // SubIFD contains Date Taken
        ExifSubIFDDirectory exifSubIFDDirectory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
        if (exifSubIFDDirectory != null){
            var date = exifSubIFDDirectory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
            if (date !=null) return Optional.of(LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()));
        }

        ExifIFD0Directory exifIFD0Descriptor = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
        if (exifIFD0Descriptor != null) {
            var date = exifIFD0Descriptor.getDate(ExifIFD0Directory.TAG_DATETIME);
            if (date !=null) return Optional.of(LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()));
        }

        return Optional.empty();
    }
}
