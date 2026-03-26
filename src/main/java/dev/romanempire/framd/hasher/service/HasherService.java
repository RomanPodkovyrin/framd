package dev.romanempire.framd.hasher.service;

import dev.romanempire.framd.hasher.impl.Sha256Hasher;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HasherService {

    private static final Logger logger = LoggerFactory.getLogger(HasherService.class);

    private final Sha256Hasher hasher;

    public Map<String,String> hashFiles(List<Path> paths) {
        logger.info("Start Hashing Files");
        return paths.stream()
                .flatMap(p -> hasher.hashFile(p).stream()
                        .map(hash -> Map.entry(p.toString(), hash)))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

}
