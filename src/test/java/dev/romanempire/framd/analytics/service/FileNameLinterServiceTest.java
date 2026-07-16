package dev.romanempire.framd.analytics.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import dev.romanempire.framd.repository.IndexedMediaRepo;
import dev.romanempire.framd.repository.model.FileLintItem;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FileNameLinterServiceTest {

    @Mock
    IndexedMediaRepo indexedMediaRepo;

    @InjectMocks
    FileNameLinterService fileNameLinterService;

    private static final LocalDateTime SOME_DATE = LocalDateTime.of(2001, 12, 12, 1, 1, 1);
    private static final String CORRECT_NAME = "2001-12-12-01h01m01";
    private static final String PATH = "/home/photos";

    @ParameterizedTest
    @MethodSource("missingExifCases")
    void missingExif_isDetected(String fileName, String path) {
        Mockito.when(indexedMediaRepo.findAllFileLintItem())
                .thenReturn(List.of(new FileLintItem(fileName, null, path)));

        var result = fileNameLinterService.analyseFileNames();

        assertEquals(1, result.missingExif().size());
        assertEquals(fileName, result.missingExif().getFirst().fileName());
        assertEquals(path, result.missingExif().getFirst().path());
        assertTrue(result.wrongFormatting().isEmpty());
        assertTrue(result.wrongDate().isEmpty());
    }

    static Stream<Arguments> missingExifCases() {
        return Stream.of(
                arguments("photo.jpg", "/home"),
                arguments("2001-12-12-01h01m01", "/albums/2001"),
                arguments("IMG_0001", "/camera"),
                arguments("random_name", "/nas/media"));
    }

    @ParameterizedTest
    @MethodSource("wrongFormattingCases")
    void wrongFormatting_isDetected(String fileName, LocalDateTime captureTime, String expectedCorrectName) {
        Mockito.when(indexedMediaRepo.findAllFileLintItem())
                .thenReturn(List.of(new FileLintItem(fileName, captureTime, PATH)));

        var result = fileNameLinterService.analyseFileNames();

        assertEquals(1, result.wrongFormatting().size());
        assertEquals(fileName, result.wrongFormatting().getFirst().fileName());
        assertEquals(expectedCorrectName, result.wrongFormatting().getFirst().correctFileName());
        assertTrue(result.missingExif().isEmpty());
        assertTrue(result.wrongDate().isEmpty());
    }

    static Stream<Arguments> wrongFormattingCases() {
        return Stream.of(
                arguments("roman",               LocalDateTime.of(2001, 12, 12,  1,  1,  1), "2001-12-12-01h01m01"),
                arguments("IMG_20011212",         LocalDateTime.of(2023,  6, 15, 14, 30, 45), "2023-06-15-14h30m45"),
                arguments("photo.jpg",            LocalDateTime.of(2019,  3, 22,  8, 15,  0), "2019-03-22-08h15m00"),
                arguments("2001-12-12",           LocalDateTime.of(2001, 12, 12,  1,  1,  1), "2001-12-12-01h01m01"),
                arguments("2001-12-12-01h01m",    LocalDateTime.of(2001, 12, 12,  1,  1,  1), "2001-12-12-01h01m01"),
                arguments("20011212_010101",       LocalDateTime.of(2020,  8,  7, 22, 45, 30), "2020-08-07-22h45m30"),
                arguments("2001_12_12_01h01m01",  LocalDateTime.of(2015, 11,  1,  9,  0,  0), "2015-11-01-09h00m00"));
    }

    @ParameterizedTest
    @MethodSource("wrongDateCases")
    void wrongDate_isDetected(String fileName, LocalDateTime captureTime, String expectedCorrectName) {
        Mockito.when(indexedMediaRepo.findAllFileLintItem())
                .thenReturn(List.of(new FileLintItem(fileName, captureTime, PATH)));

        var result = fileNameLinterService.analyseFileNames();

        assertEquals(1, result.wrongDate().size());
        assertEquals(fileName, result.wrongDate().getFirst().fileName());
        assertEquals(expectedCorrectName, result.wrongDate().getFirst().correctFileName());
        assertTrue(result.missingExif().isEmpty());
        assertTrue(result.wrongFormatting().isEmpty());
    }

    static Stream<Arguments> wrongDateCases() {
        return Stream.of(
                arguments("2001-12-12-01h01m01", LocalDateTime.of(2002, 1, 1, 0, 0, 0), "2002-01-01-00h00m00"),
                arguments("2001-12-12-01h01m01", LocalDateTime.of(2001, 12, 12, 1, 1, 2), "2001-12-12-01h01m02"),
                arguments("2023-06-15-14h30m45", LocalDateTime.of(2023, 6, 15, 14, 30, 46), "2023-06-15-14h30m46"),
                arguments("2020-01-01-00h00m00", LocalDateTime.of(2019, 12, 31, 23, 59, 59), "2019-12-31-23h59m59"));
    }


    @Test
    void correctlyFormattedFile_producesNoFindings() {
        Mockito.when(indexedMediaRepo.findAllFileLintItem())
                .thenReturn(List.of(new FileLintItem(CORRECT_NAME, SOME_DATE, PATH)));

        var result = fileNameLinterService.analyseFileNames();

        assertTrue(result.missingExif().isEmpty());
        assertTrue(result.wrongFormatting().isEmpty());
        assertTrue(result.wrongDate().isEmpty());
    }

    @Test
    void mixedItems_areCorrectlyCategorized() {
        Mockito.when(indexedMediaRepo.findAllFileLintItem())
                .thenReturn(
                        List.of(
                                new FileLintItem("photo", null, "/a"),
                                new FileLintItem("bad_name", SOME_DATE, "/b"),
                                new FileLintItem(CORRECT_NAME, SOME_DATE, "/c")));

        var result = fileNameLinterService.analyseFileNames();

        assertEquals(1, result.missingExif().size());
        assertEquals("photo", result.missingExif().getFirst().fileName());
        assertEquals(1, result.wrongFormatting().size());
        assertEquals("bad_name", result.wrongFormatting().getFirst().fileName());
        assertTrue(result.wrongDate().isEmpty());
    }

}