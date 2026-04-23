package dev.romanempire.framd.indexing.impl;

import dev.romanempire.framd.extractor.util.ImageTools;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class FSIndexer implements Indexer {

    private static final Logger logger = LoggerFactory.getLogger(FSIndexer.class);

    @Override
    public List<Path> walk(String path) {
        Path scanPath = Path.of(path);
        logger.info("Walking: {}", path);

        try (var file_stream = Files.walk(scanPath)) {
            return file_stream
                    .filter(Files::isRegularFile)
                    .filter(Files::isReadable)
                    .filter(ImageTools::isImage)
                    .toList();
        } catch (IOException e) {
            logger.error(e.getMessage());
        }

        return List.of();
    }

    @Override
    public List<Path> walkDirRecursively(String path) {
        Path scanPath = Path.of(path);
        logger.info("Walking Dirs Recursively: {}", path);

        try (var file_stream = Files.walk(scanPath)) {
            return file_stream
                    .filter(Files::isDirectory)
                    .filter(Files::isReadable)
                    .toList();
        } catch (IOException e) {
            logger.error(e.getMessage());
        }

        return List.of();
    }

    @Override
    public List<Path> list(String path) {
        logger.info("Listing Directories: {}", path);

        var scanPath = Path.of(path);
        try (var fileStream = Files.list(scanPath)) {
            return fileStream
                    .filter(Files::isRegularFile)
                    .filter(ImageTools::isImage)
                    .toList();
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
        return List.of();
    }
}
