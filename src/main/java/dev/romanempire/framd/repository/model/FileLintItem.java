package dev.romanempire.framd.repository.model;

import java.time.LocalDateTime;

public record FileLintItem(String fileName, LocalDateTime CaptureTime, String path) {}
