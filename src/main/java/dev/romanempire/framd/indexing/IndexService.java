package dev.romanempire.framd.indexing;

import org.springframework.stereotype.Service;

@Service
public class IndexService {

    private final Indexer indexer;

    public IndexService(Indexer indexer) {
        this.indexer = indexer;
    }

    public void indexPath(String path) {
        indexer.index(path);
    }
}
