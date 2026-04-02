package dev.romanempire.framd.indexing.util;

import dev.romanempire.framd.extractor.util.ImageTools;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ImageToolsTest {

    @ParameterizedTest
    @ValueSource(strings = {"/root/file.png", "photo.jpg", "/.jpeg", "/.jPEg","./pic.webp", })
    void isImage(String path) {
        assertTrue(ImageTools.isImage(Path.of(path)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"/root/file.txt", "photo", "/.jpfeg", "./", "", "1223/file.heic"})
    void isNotImage(String path) {
        assertFalse(ImageTools.isImage(Path.of(path)));
    }

//    @Test
//    void getMetadata() {
//    }
}