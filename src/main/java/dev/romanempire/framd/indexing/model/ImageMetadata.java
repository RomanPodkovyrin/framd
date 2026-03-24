package dev.romanempire.framd.indexing.model;

import java.nio.file.Path;
import java.util.Date;
import java.util.Optional;

public record ImageMetadata(Optional<Date> dateTaken, String parentPath, String fileName, String extension, Path path) {
}
