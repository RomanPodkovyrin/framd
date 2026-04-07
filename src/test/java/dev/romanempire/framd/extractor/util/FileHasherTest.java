package dev.romanempire.framd.extractor.util;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.*;

class FileHasherTest {

    @TempDir
    static Path testDir;

    static Path testFile;

    @BeforeAll
    static void setUp() throws IOException {
        testFile = testDir.resolve("picture.jpg");

        Files.writeString(testFile, "Picture of a cute cat");
    }


    @Test
    void hashFileSuccess() throws NoSuchAlgorithmException {
        var hash = FileHasher.hashFile(testFile);
        assertTrue(hash.isPresent());
        assertEquals("97e2ab67579714e929a7e1e10f66eb242e061a1c69bfec90e69816d664a21be3", hash.get());
    }

    @Test
    void hashFileNonExistingFile() throws NoSuchAlgorithmException {
        var hash = FileHasher.hashFile(Path.of("i-do-not-exist.oops"));
        assertTrue(hash.isEmpty());
    }
}
