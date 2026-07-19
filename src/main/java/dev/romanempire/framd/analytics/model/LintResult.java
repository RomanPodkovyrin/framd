package dev.romanempire.framd.analytics.model;

import java.util.List;

public record LintResult(
        List<WrongDate> wrongDate, List<MissingExif> missingExif, List<WrongFormatting> wrongFormatting) {}
