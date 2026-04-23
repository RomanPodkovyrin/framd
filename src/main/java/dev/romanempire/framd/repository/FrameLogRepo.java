package dev.romanempire.framd.repository;

import dev.romanempire.framd.repository.model.FrameLog;
import dev.romanempire.framd.repository.model.FrameLogStats;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface FrameLogRepo extends JpaRepository<FrameLog, String> {

    @Query("""
            SELECT new dev.romanempire.framd.repository.model.FrameLogStats(f.indexedMedia.id, COUNT(f),  MAX(f.shownAt))
            FROM FrameLog f
            GROUP BY f.indexedMedia.id, f.indexedMedia.hash
            """)
    List<FrameLogStats> getLogStats();
}
