package dev.romanempire.framd.dto;

import dev.romanempire.framd.repository.model.IndexedMedia;
import java.nio.file.Path;
import java.time.LocalDateTime;

public record IndexMediaDto(
        String hash,
        String name,
        String extension,
        String path,
        String folder,
        LocalDateTime captureTime,
        Integer width,
        Integer height,
        Long sizeInBytes) {

    public static IndexMediaDto from(IndexedMedia indexedMedia) {
        return new IndexMediaDto(
                indexedMedia.getHash(),
                indexedMedia.getName(),
                indexedMedia.getExtension(),
                indexedMedia.getPath(),
                Path.of(indexedMedia.getPath()).getFileName().toString(),
                indexedMedia.getCaptureTime(),
                indexedMedia.getWidth(),
                indexedMedia.getHeight(),
                indexedMedia.getSizeInBytes());
    }
}
