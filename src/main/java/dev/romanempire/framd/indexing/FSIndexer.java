package dev.romanempire.framd.indexing;


import dev.romanempire.framd.indexing.util.ImageTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

@Service
public class FSIndexer implements Indexer {

    private static final Logger logger = LoggerFactory.getLogger(FSIndexer.class);


    @Override
    public void index(String path) {
        Path scan_path = Paths.get(path);
        logger.info("Indexing: {}", path);


        try(var file_stream = Files.walk(scan_path)) {
            file_stream
                    .filter(Files::isRegularFile)
                    .filter(Files::isReadable)
                    .filter(ImageTools::isImage)
                    .map(ImageTools::getMetadata)
                    .filter(Optional::isPresent) // do we really want to ignore it?
                    .forEach(System.out::println);
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
        logger.info("done");

    }
}
