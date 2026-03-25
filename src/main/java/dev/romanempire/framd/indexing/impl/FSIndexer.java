package dev.romanempire.framd.indexing.impl;


import dev.romanempire.framd.indexing.model.ImageMetadata;
import dev.romanempire.framd.indexing.util.ImageTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

@Service
public class FSIndexer implements Indexer {

    private static final Logger logger = LoggerFactory.getLogger(FSIndexer.class);


    @Override
    public List<ImageMetadata> index(String path) {
        Path scan_path = Paths.get(path);
        logger.info("Indexing: {}", path);


        try(var file_stream = Files.walk(scan_path)) {
            return file_stream
                    .filter(Files::isRegularFile)
                    .filter(Files::isReadable)
                    .filter(ImageTools::isImage)
                    .map(ImageTools::getMetadata)
                    .flatMap(Optional::stream)// this ignores failed images
                    .toList();
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
        logger.info("done");

        return List.of();
    }
}
