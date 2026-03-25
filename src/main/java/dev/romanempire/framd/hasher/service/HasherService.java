package dev.romanempire.framd.hasher.service;

import dev.romanempire.framd.hasher.impl.Sha256Hasher;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class HasherService {

    private static final Logger logger = LoggerFactory.getLogger(HasherService.class);

    private final Sha256Hasher hasher;

    public List<String> hashFiles(List<Path> paths) {
        logger.info("Start Hashing Files");
        var fileHashes = paths.stream().map(hasher::hashFile).flatMap(Optional::stream).toList();
        return fileHashes;
    }

}
