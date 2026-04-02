package dev.romanempire.framd.indexing.impl;

import java.nio.file.Path;
import java.util.List;

public interface Indexer {
    /// Recursively walks the directory tree and collects all image files.
    ///
    /// @param path the root directory to start walking from
    /// @return a list of paths to image files
    List<Path> walk(String path);

    /// Recursively walks the directory tree and collects all subdirectories.
    ///
    /// @param path the root directory to start walking from
    /// @return a list of directory paths
    List<Path> walkDirRecursively(String path);

    /// Lists image files in the given directory without recursing into subdirectories.
    ///
    /// @param path the directory to list
    /// @return a list of paths to image files
    List<Path> list(String path);
}
