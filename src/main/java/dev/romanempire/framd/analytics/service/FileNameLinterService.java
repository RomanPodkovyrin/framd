package dev.romanempire.framd.analytics.service;

import dev.romanempire.framd.analytics.model.LintResult;
import dev.romanempire.framd.analytics.model.MissingExif;
import dev.romanempire.framd.analytics.model.WrongDate;
import dev.romanempire.framd.analytics.model.WrongFormatting;
import dev.romanempire.framd.repository.IndexedMediaRepo;
import dev.romanempire.framd.repository.model.FileLintItem;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class FileNameLinterService {
    private static final Logger logger = LoggerFactory.getLogger(FileNameLinterService.class);


    private final IndexedMediaRepo indexedMediaRepo;


    public LintResult analyseFileNames() {

        Pattern pattern = Pattern.compile("\\d{4}-\\d{2}-\\d{2}-\\d{2}h\\d{2}m\\d{2}");
        DateTimeFormatter fileNameFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH'h'mm'm'ss");

        List<MissingExif> missingExif = new ArrayList<>();
        List<WrongFormatting> wrongFormatting = new ArrayList<>();
        List<WrongDate> wrongDates = new ArrayList<>();

        List<FileLintItem> allFiles = indexedMediaRepo.findAllFileLintItem();
        for (var f : allFiles) {

            switch (f) {
                case FileLintItem(_, LocalDateTime date, _) when date == null -> {
                    logger.info("Missing EXIF date: {} {}", f.fileName(), f.path());
                    missingExif.add(new MissingExif(f.fileName(), f.path()));
                }
                case FileLintItem(String name, _, _) when !pattern.matcher(name).matches() -> {
                    logger.info("Wrong filename format: {} ({})", f.fileName(), f.path());
                    wrongFormatting.add(new WrongFormatting(f.fileName(), fileNameFormatter.format(f.CaptureTime()), f.CaptureTime(), f.path()));
                }
                case FileLintItem(
                        String name, LocalDateTime dateTime, _
                ) when !name.equals(fileNameFormatter.format(dateTime)) -> {
                    logger.info("Date mismatch: {}, expected: {}", f.fileName(), fileNameFormatter.format(dateTime));
                    wrongDates.add(new WrongDate(f.fileName(), fileNameFormatter.format(dateTime), f.CaptureTime(), f.path()));
                }
                default -> {  // Happy Path, passes all linting
                }
            }
        }

        logger.info("LintResult: Total: {}  Missing EXIF: {}  Wrong Formatting: {}  Wrong Dates: {}", allFiles.size(), missingExif.size(), wrongFormatting.size(), wrongDates.size());
        return new LintResult(wrongDates, missingExif, wrongFormatting);
    }


}
