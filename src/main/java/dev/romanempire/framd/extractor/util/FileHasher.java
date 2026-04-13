package dev.romanempire.framd.extractor.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileHasher {

    private static final Logger logger = LoggerFactory.getLogger(FileHasher.class);

    public static Optional<String> hashFile(Path path) throws NoSuchAlgorithmException {

        MessageDigest digest;
        digest = MessageDigest.getInstance("SHA-256");
        try (var fis = new FileInputStream(path.toFile());
                var dis = new DigestInputStream(fis, digest)) {
            byte[] buffer = new byte[8192]; // 8KB chunks
            while (dis.read(buffer) != -1) {}
        } catch (IOException e) {
            return Optional.empty();
        }

        byte[] hash = digest.digest();
        return Optional.of(HexFormat.of().formatHex(hash));
    }
}
