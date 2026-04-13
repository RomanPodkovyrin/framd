package dev.romanempire.framd.extractor.util;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class ImageToolsTest {

    @ParameterizedTest
    @ValueSource(
            strings = {
                "/root/file.png",
                "photo.jpg",
                "./pic.webp",
            })
    void isImage(String path) {
        assertTrue(ImageTools.isImage(Path.of(path)));
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "/root/file.txt",
                "photo",
                "/.jpfeg",
                "./",
                "",
                "1223/file.heic",
                "/.jpeg",
                "/.jPEg",
            })
    void isNotImage(String path) {
        assertFalse(ImageTools.isImage(Path.of(path)));
    }

    @ParameterizedTest
    @CsvSource({
        "/root/file.txt, file, txt", // normal file
        ".file, file, ''", // hidden file without extension
        "file, file, ''", // file without extension
        ".file.txt, file, txt", // hidden file with extension
        "file., file, ''", // trailing dot
        ".file., file, ''", // hidden file with trailing dot
        "file.tar.gz, file.tar, gz", // multiple extensions
        ".file.tar.gz, file.tar, gz" // hidden file with multiple extensions
    })
    void testGetFileParts(String path, String expectedFileName, String expectedFileExtension) {

        var fileParts = ImageTools.getFileParts(Path.of(path));

        assertEquals(expectedFileName, fileParts.name());
        assertEquals(expectedFileExtension, fileParts.extension());
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "/", ".", "..", "",
            })
    void testGetFilePartsMalformed(String input) {

        Exception ex = assertThrows(Exception.class, () -> ImageTools.getFileParts(Path.of(input)));

        assertEquals("Invalid file name", ex.getMessage());
    }
}
