package dev.romanempire.framd.repository.model;

import java.time.LocalDateTime;

public record FrameLogStats(String mediaId, Long count, LocalDateTime lastShown) {}
