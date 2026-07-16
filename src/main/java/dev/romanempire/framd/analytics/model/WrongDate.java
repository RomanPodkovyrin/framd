package dev.romanempire.framd.analytics.model;

import java.time.LocalDateTime;

public record WrongDate (String fileName, String correctFileName, LocalDateTime CaptureTime, String path){
}
