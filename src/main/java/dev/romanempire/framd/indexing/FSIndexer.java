package dev.romanempire.framd.indexing;


import dev.romanempire.framd.indexing.util.ImageTools;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class FSIndexer implements Indexer {


    @Override
    public void index(String path) {
        Path scan_path = Paths.get(path);
        System.out.printf("Indexing: %s%n", path);


        try(var file_stream = Files.walk(scan_path)) {
            file_stream
                    .filter(Files::isRegularFile)
                    .filter(Files::isReadable)
                    .filter(ImageTools::isImage)
                    .forEach(System.out::println);
        } catch (IOException e) {
            System.out.println(e);
        }
        System.out.println("Done");

    }
}
