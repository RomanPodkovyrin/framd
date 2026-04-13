package dev.romanempire.framd.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IndexedMediaRepo extends JpaRepository<IndexedMedia, String> {
    List<IndexedMedia> getAllByHash(String hash);

    List<IndexedMedia> findAllByPath(String path);
}
