package dev.romanempire.framd.indexing.model;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Optional;

public record ImageMetadata(Optional<LocalDateTime> dateTaken, String parentPath, String fileName, String extension, Path path) {
}
