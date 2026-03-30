package dev.romanempire.framd.indexing.impl;

import java.nio.file.Path;
import java.util.List;

public interface Indexer {
    List<Path> walk(String path);
}
