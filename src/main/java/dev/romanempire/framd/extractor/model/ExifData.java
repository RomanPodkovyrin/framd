package dev.romanempire.framd.extractor.model;

import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.jpeg.JpegDirectory;
import dev.romanempire.framd.extractor.util.ImageTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Optional;

public class ExifData {
    private final Metadata metadata;

    private static final Logger logger = LoggerFactory.getLogger(ExifData.class);

    private ExifData(Metadata metadata) {
        this.metadata = metadata;
    }

    public static Optional<ExifData> from(Path path) {
        return ImageTools
                .getMetadata(path)
                .map(ExifData::new);
    }

    private <T extends Directory> Optional<Date> getDate(Class<T> dirType, int tag) {
        return Optional.ofNullable(metadata.getFirstDirectoryOfType(dirType))
                .map(d -> d.getDate(tag));
    }

    private <T extends Directory> Optional<String> getString(Class<T> dirType, int tag) {
        return Optional.ofNullable(metadata.getFirstDirectoryOfType(dirType))
                .map(d -> d.getString(tag));
    }

    private <T extends Directory> Optional<Integer> getInt(Class<T> dirType, int tag) {
        return Optional.ofNullable(metadata.getFirstDirectoryOfType(dirType))
                .filter(d -> d.containsTag(tag))
                .map(d -> d.getInteger(tag));
    }

    private <T extends Directory> Optional<Double> getDouble(Class<T> dirType, int tag) {
        return Optional.ofNullable(metadata.getFirstDirectoryOfType(dirType))
                .filter(d -> d.containsTag(tag))
                .map(d -> d.getDoubleObject(tag));
    }

    private static final DateTimeFormatter EXIF_DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss");

    public Optional<LocalDateTime> getCaptureDate() {

        return getString(ExifSubIFDDirectory.class, ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL)
                .or(() -> getString(ExifSubIFDDirectory.class, ExifSubIFDDirectory.TAG_DATETIME_DIGITIZED))
                .flatMap(d -> {
                    try {
                        return Optional.of(LocalDateTime.parse(d, EXIF_DATE_FORMAT));
                    } catch (DateTimeException e) {
                        logger.error(e.getMessage());
                        return Optional.empty();
                    }
                });
    }

    public Optional<Integer> getWidth() {
        return getInt(JpegDirectory.class, JpegDirectory.TAG_IMAGE_WIDTH)
                .or(() -> getInt(ExifIFD0Directory.class, ExifIFD0Directory.TAG_IMAGE_WIDTH));
    }

    public Optional<Integer> getHeight() {
        return getInt(JpegDirectory.class, JpegDirectory.TAG_IMAGE_HEIGHT)
                .or(() -> getInt(ExifIFD0Directory.class, ExifIFD0Directory.TAG_IMAGE_HEIGHT));
    }
}

