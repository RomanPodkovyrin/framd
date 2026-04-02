package dev.romanempire.framd.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IndexedMediaRepo extends JpaRepository<IndexedMedia, String> {
    List<IndexedMedia> getAllByHash(String hash);

    List<IndexedMedia> findAllByPath(String path);
}
