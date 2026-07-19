package dev.romanempire.framd.report;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.romanempire.framd.analytics.model.LintResult;
import dev.romanempire.framd.analytics.model.MissingExif;
import dev.romanempire.framd.analytics.model.WrongDate;
import dev.romanempire.framd.analytics.model.WrongFormatting;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

class ReportServiceTest {

    private static final LocalDateTime DATE = LocalDateTime.of(2024, Month.JUNE, 15, 14, 30, 0);
    private static final Path REPORTS = Path.of("reports");

    @Test
    void generateReport_writesPdfToDisk() throws Exception {
        ReportService service = new ReportService();

        LintResult lintResult = new LintResult(
                List.of(new WrongDate("IMG_0001.jpg", "2024-06-15-14h30m00", DATE, "/photos/trip")),
                List.of(new MissingExif("scan001.jpg", "/scans"), new MissingExif("scan002.jpg", "/scans")),
                List.of(new WrongFormatting("vacation photo.jpg", "2024-06-15-14h30m00", DATE, "/albums/2024")));

        long before = countReports();
        service.generateReport(lintResult);
        assertEquals(before + 1, countReports());
    }

    @Test
    void generateReport_withEmptyLists_writesPdf() throws Exception {
        ReportService service = new ReportService();

        long before = countReports();
        service.generateReport(new LintResult(List.of(), List.of(), List.of()));
        assertEquals(before + 1, countReports());
    }

    @Test
    void generateReport_withLargeDataset_writesPdf() throws Exception {
        ReportService service = new ReportService();

        List<WrongDate> wrongDates = IntStream.rangeClosed(1, 100)
                .mapToObj(i -> new WrongDate(
                        "IMG_%04d.jpg".formatted(i),
                        "2024-%02d-%02d-%02dh%02dm%02d".formatted(i % 12 + 1, i % 28 + 1, i % 24, i % 60, i % 60),
                        DATE.plusDays(i),
                        "/nas/photos/family/holidays/%04d/subfolder-deep/another-level/IMG_%04d.jpg".formatted(2000 + i, i)))
                .toList();

        List<MissingExif> missingExif = IntStream.rangeClosed(1, 100)
                .mapToObj(i -> new MissingExif(
                        "scan_%04d.tiff".formatted(i),
                        "/nas/scans/archive/documents/year-%d/month-%02d/week-%d/scan_%04d.tiff".formatted(
                                2000 + i % 24, i % 12 + 1, i % 4 + 1, i)))
                .toList();

        List<WrongFormatting> wrongFormatting = IntStream.rangeClosed(1, 100)
                .mapToObj(i -> new WrongFormatting(
                        "vacation photo %04d.jpg".formatted(i),
                        "2024-%02d-%02d-%02dh%02dm%02d".formatted(i % 12 + 1, i % 28 + 1, i % 24, i % 60, i % 60),
                        DATE.plusHours(i),
                        "/nas/photos/travel/europe/country-%d/city-%d/vacation photo %04d.jpg".formatted(i % 10, i % 5, i)))
                .toList();

        long before = countReports();
        service.generateReport(new LintResult(wrongDates, missingExif, wrongFormatting));
        assertEquals(before + 1, countReports());
    }

    private long countReports() throws IOException {
        if (!Files.exists(REPORTS)) return 0;
        try (var stream = Files.list(REPORTS)) {
            return stream.count();
        }
    }

}