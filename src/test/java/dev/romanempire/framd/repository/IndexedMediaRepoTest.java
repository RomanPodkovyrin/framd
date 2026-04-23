package dev.romanempire.framd.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import dev.romanempire.framd.repository.model.IndexedMedia;
import java.time.LocalDateTime;
import java.util.Objects;
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
public class IndexedMediaRepoTest {

    @Container
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    IndexedMediaRepo indexedMediaRepo;

    @Test
    void findAllByCaptureDayAndMonth_noLastYears_returnsEmpty() {
        indexedMediaRepo.save(IndexedMedia.builder()
                .hash("abc123")
                .path("/photos")
                .name("img")
                .extension("jpg")
                .sizeInBytes(1000L)
                .captureTime(LocalDateTime.of(2026, 1, 1, 10, 0))
                .build());

        indexedMediaRepo.save(IndexedMedia.builder()
                .hash("cba321")
                .path("/photos2")
                .name("cat")
                .extension("png")
                .sizeInBytes(2000L)
                .captureTime(LocalDateTime.of(2026, 1, 2, 10, 0))
                .build());

        var actual = indexedMediaRepo.findAllByCaptureDayAndMonth(1, 1, 2026);

        assertTrue(actual.isEmpty());
    }

    @Test
    void findAllByCaptureDayAndMonth_multipleResults() {
        indexedMediaRepo.save(IndexedMedia.builder()
                .hash("abc123")
                .path("/photos")
                .name("img")
                .extension("jpg")
                .sizeInBytes(1000L)
                .captureTime(LocalDateTime.of(2025, 1, 1, 10, 0))
                .build());

        indexedMediaRepo.save(IndexedMedia.builder()
                .hash("cba321")
                .path("/photos2")
                .name("cat")
                .extension("png")
                .sizeInBytes(2000L)
                .captureTime(LocalDateTime.of(2024, 1, 1, 11, 0))
                .build());

        var actual = indexedMediaRepo.findAllByCaptureDayAndMonth(1, 1, 2026);

        assertEquals(2, actual.size());
        assertThat(actual).anyMatch(im -> Objects.equals(im.getHash(), "abc123"));
        assertThat(actual).anyMatch(im -> Objects.equals(im.getHash(), "cba321"));
    }
}
