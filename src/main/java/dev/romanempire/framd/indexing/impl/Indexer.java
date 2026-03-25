package dev.romanempire.framd.indexing.impl;

import dev.romanempire.framd.indexing.model.ImageMetadata;

import java.util.List;

public interface Indexer {
    List<ImageMetadata> index(String path);
}
