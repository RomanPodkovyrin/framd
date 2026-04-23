package dev.romanempire.framd.repository;

import dev.romanempire.framd.repository.model.IndexedMedia;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface IndexedMediaRepo extends JpaRepository<IndexedMedia, String> {
    List<IndexedMedia> getAllByHash(String hash);

    List<IndexedMedia> findAllByPath(String path);

    @Query("""
            Select m FROM IndexedMedia m
            WHERE EXTRACT(DAY FROM m.captureTime) = :day
            AND EXTRACT(MONTH FROM m.captureTime) = :month
            AND EXTRACT(YEAR FROM m.captureTime) != :year
            """)
    List<IndexedMedia> findAllByCaptureDayAndMonth(
            @Param("day") int day, @Param("month") int month, @Param("year") int year);
}
