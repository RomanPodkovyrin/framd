package dev.romanempire.framd.analytics.model;

import java.time.LocalDateTime;

public record WrongFormatting(String fileName, String correctFileName, LocalDateTime CaptureTime, String path) {
}
