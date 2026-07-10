package dev.romanempire.framd.repository;

import static org.junit.jupiter.api.Assertions.*;

import dev.romanempire.framd.repository.model.FrameLog;
import dev.romanempire.framd.repository.model.IndexedMedia;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class FrameLogRepoTest {

    @Container
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    FrameLogRepo frameLogRepo;

    @Autowired
    IndexedMediaRepo indexedMediaRepo;

    @Test
    void getLogStats_countsAndLastShown() {
        var media = indexedMediaRepo.save(IndexedMedia.builder()
                .hash("abc123")
                .path("/photos")
                .name("img")
                .extension("jpg")
                .sizeInBytes(1000L)
                .build());

        var media2 = indexedMediaRepo.save(IndexedMedia.builder()
                .hash("cba321")
                .path("/photos2")
                .name("cat")
                .extension("png")
                .sizeInBytes(2000L)
                .build());

        var t1 = LocalDateTime.of(2026, 1, 1, 10, 0);
        var t2 = LocalDateTime.of(2026, 1, 2, 10, 0);
        var t3 = LocalDateTime.of(2026, 1, 3, 10, 0);
        frameLogRepo.save(FrameLog.builder().indexedMedia(media).shownAt(t1).build());
        frameLogRepo.save(FrameLog.builder().indexedMedia(media).shownAt(t2).build());
        frameLogRepo.save(FrameLog.builder().indexedMedia(media2).shownAt(t3).build());

        var stats = frameLogRepo.getLogStats();

        assertEquals(2, stats.size());
        assertEquals(media.getId(), stats.getFirst().mediaId());
        assertEquals(2L, stats.getFirst().count());
        assertEquals(t2, stats.getFirst().lastShown());
        assertEquals(media2.getId(), stats.get(1).mediaId());
        assertEquals(t3, stats.get(1).lastShown());
    }

    @Test
    void getLogStats_noLogs_returnsEmpty() {
        assertTrue(frameLogRepo.getLogStats().isEmpty());
    }
}
