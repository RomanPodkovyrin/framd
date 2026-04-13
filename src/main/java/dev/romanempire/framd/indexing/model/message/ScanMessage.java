package dev.romanempire.framd.indexing.model.message;

public sealed interface ScanMessage<T> permits ScanMessage.Data, ScanMessage.Done {
    record Data<T>(T data) implements ScanMessage<T> {
    }

    record Done<T>() implements ScanMessage<T> {
    }
}

