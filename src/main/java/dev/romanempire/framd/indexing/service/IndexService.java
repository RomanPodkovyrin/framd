package dev.romanempire.framd.indexing.service;

import dev.romanempire.framd.hasher.service.HasherService;
import dev.romanempire.framd.indexing.impl.Indexer;
import dev.romanempire.framd.indexing.model.ImageMetadata;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IndexService {

    private final Indexer indexer;

    private final HasherService hasherService;

    private final ThumbnailService thumbnailService;

    public void indexPath(String path) {
        indexer.index(path);
        List<ImageMetadata> imageMetadataList = indexer.index(path);
        imageMetadataList.forEach(System.out::println);
        var hashes = hasherService.hashFiles(imageMetadataList.stream().map(ImageMetadata::path).toList());
        hashes.forEach(System.out::println);
        thumbnailService.generateThumbnails(imageMetadataList.stream().map(ImageMetadata::path).toList());
    }
}
