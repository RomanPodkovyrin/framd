package dev.romanempire.framd.indexing.service;

import dev.romanempire.framd.extractor.service.MetadataExtractorService;
import dev.romanempire.framd.extractor.service.PreviewService;
import dev.romanempire.framd.indexing.model.ScanContext;
import dev.romanempire.framd.indexing.impl.Indexer;
import dev.romanempire.framd.repository.IndexedMediaRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class IndexServiceTest {


    @Mock
    Indexer indexer;

    @Mock
    MetadataExtractorService metadataExtractorService;

    @Mock
    PreviewService previewService;

    @Mock
    IndexedMediaRepo indexedMediaRepo;

    @Mock
    ScanContext scanContext;

    @InjectMocks
    IndexService indexService;


    @Test
    void listPathsSuccessfully() {
        var testDir = "/testDir";
        var expectedOutput = Stream.of(
                "/home",
                "photo.jpg",
                ".gitignore"
        ).map(Path::of).toList();
        Mockito.when(indexer.list(testDir)).thenReturn(expectedOutput);


        List<Path> paths = indexService.list(testDir);

        assertFalse(paths.isEmpty());
        assertEquals(expectedOutput, paths);
    }
}
