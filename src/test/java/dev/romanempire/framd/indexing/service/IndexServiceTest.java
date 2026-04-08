package dev.romanempire.framd.indexing.service;

import dev.romanempire.framd.extractor.service.MetadataExtractorService;
import dev.romanempire.framd.extractor.service.PreviewService;
import dev.romanempire.framd.indexing.impl.Indexer;
import dev.romanempire.framd.repository.IndexedMediaRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CountDownLatch;
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


    @Test
    void fullScanAtomicCheck() throws InterruptedException {

        var scanStarted = new CountDownLatch(1);
        var scanDone = new CountDownLatch(1);
        Mockito.when(indexedMediaRepo.findAll()).then(c -> {
            scanStarted.countDown();
            scanDone.await();
            return List.of();
        });

        boolean firstScan = indexService.tryFullScan("test");

        scanStarted.await();

        boolean secondScan = indexService.tryFullScan("test");

        scanDone.countDown();
        assertTrue(firstScan);
        assertFalse(secondScan);

        assertTimeout(Duration.ofSeconds(5), () -> {
            while (!indexService.tryFullScan("test")) {
                Thread.sleep(10);
            }
        });

        Thread.sleep(100);


    }

    @Test
    void partialScanAtomicCheck() throws InterruptedException {

        var scanStarted = new CountDownLatch(1);
        var scanDone = new CountDownLatch(1);
        Mockito.when(indexedMediaRepo.findAllByPath("test")).then(c -> {
            scanStarted.countDown();
            scanDone.await();
            return List.of();
        });

        boolean firstScan = indexService.tryPartialScan("test");

        scanStarted.await();

        boolean secondScan = indexService.tryPartialScan("test");

        scanDone.countDown();
        assertTrue(firstScan);
        assertFalse(secondScan);

        assertTimeout(Duration.ofSeconds(5), () -> {
            while (!indexService.tryPartialScan("test")) {
                Thread.sleep(10);
            }
        });


    }
}
