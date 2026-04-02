package dev.romanempire.framd.extractor.service;

import dev.romanempire.framd.extractor.util.ImageTools;
import dev.romanempire.framd.indexing.service.IndexService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FileWatcherService {

    private static final Logger logger = LoggerFactory.getLogger(FileWatcherService.class);

    @Value("${media.library.path}")
    private String scanPath;

    private final IndexService indexService;


    @EventListener(ApplicationReadyEvent.class)
    public void startWatching() {
        logger.info("Starting Watching Service");

        try (WatchService watchService = FileSystems.getDefault().newWatchService()) {

            logger.info("Registering sub directories of the scan path");
            List<Path> paths = indexService.walkAndListDirsRecursively(scanPath);
            paths.forEach(path -> {
                try {
                    path.register(
                            watchService,
                            StandardWatchEventKinds.ENTRY_CREATE,
                            StandardWatchEventKinds.ENTRY_DELETE,
                            StandardWatchEventKinds.ENTRY_MODIFY
                    );
                } catch (IOException e) {
                    logger.error("Failed to register path {} with error: {} ", path, e.getMessage());
                }
            });

            WatchKey key;
            while ((key = watchService.take()) != null) {
                Path dir = (Path) key.watchable();
                List<Path> pathsToScan = new ArrayList<>();
                for (var event : key.pollEvents()) {
                    Path fullPath = dir.resolve((Path) event.context());
                    logger.info("{} Detected at {}", event.kind(), fullPath);
                    pathsToScan.add(fullPath);
                }

                var directoriesToScan = pathsToScan
                        .stream()
                        .filter(p -> Files.isRegularFile(p) || !Files.exists(p)) // TODO: check also for event kind
                        .filter(ImageTools::isImage)
                        .map(Path::getParent)
                        .collect(Collectors.toSet());

                directoriesToScan.forEach(directoryToScan -> {
                    logger.info("Attempting Partial Scan for {}", directoryToScan);
                    indexService.tryPartialScan(directoryToScan.toString());
                });

                key.reset();
            }
        } catch (IOException e) {
            logger.error("e: ", e);
        } catch (InterruptedException e) {
            logger.error("e: ", e);
        }

    }
}
