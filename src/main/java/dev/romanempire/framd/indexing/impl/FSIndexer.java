package dev.romanempire.framd.indexing.impl;


import dev.romanempire.framd.extractor.util.ImageTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
public class FSIndexer implements Indexer {

    private static final Logger logger = LoggerFactory.getLogger(FSIndexer.class);


    @Override
    public List<Path> walk(String path) {
        Path scan_path = Paths.get(path);
        logger.info("Indexing: {}", path);


        try (var file_stream = Files.walk(scan_path)) {
            return file_stream
                    .filter(Files::isRegularFile)
                    .filter(Files::isReadable)
                    .filter(ImageTools::isImage)
                    .toList();
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
        logger.info("done");

        return List.of( );
    }
}
