package dev.romanempire.framd.hasher.service;

import dev.romanempire.framd.hasher.impl.FileHasher;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

@Service
@RequiredArgsConstructor
public class HasherService {

    private static final Logger logger = LoggerFactory.getLogger(HasherService.class);

    public Map<String,String> hashFiles(List<Path> paths) {

        var semaphore = new Semaphore(20);

        logger.info("Start Hashing Files");
        Map<String, String> hashByPath = new ConcurrentHashMap<>();

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            paths.forEach(p ->
            executor.submit(() ->{
                var hash = ""; //TODO: don't like this
                try {
                    semaphore.acquire();
                    hash = FileHasher.hashFile(p).get();
                } catch (InterruptedException e) {
                    logger.error("Semaphore Interrupted: {}", e.getMessage());
                } finally {
                    semaphore.release();
                }


                hashByPath.put(p.toString(), hash);
            }));
        }
        return hashByPath;
    }

}
