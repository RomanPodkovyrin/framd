package dev.romanempire.framd.hasher.impl;

import dev.romanempire.framd.hasher.service.HasherService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Optional;

@Service
public class Sha256Hasher{

    private static final Logger logger = LoggerFactory.getLogger(Sha256Hasher.class);

    public Optional<String> hashFile(Path path) {



        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");

            long totaleBytes = path.toFile().length();
            long bytesRead = 0;
            try (var fis = new FileInputStream(path.toFile());
            var dis = new DigestInputStream(fis, digest)) {
                byte[] buffer = new byte[8192]; // 8KB chunks
                long chunk;
                while ((chunk = dis.read(buffer)) != -1) {
                    bytesRead += chunk;
                    logger.info("Hashing {}, read bytes {} out of {} ({}%)", path.getFileName(),bytesRead, totaleBytes, ((bytesRead* 100) / totaleBytes) );
                    // should track image hashign progress?
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            byte[] hash = digest.digest();
            return Optional.of(HexFormat.of().formatHex(hash));


        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }


//        return Optional.empty();
    }
}
