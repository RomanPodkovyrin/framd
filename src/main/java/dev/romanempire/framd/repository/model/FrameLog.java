package dev.romanempire.framd.repository.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Data
@Entity
@Builder
@Table(
        name = "FrameLog",
        indexes = {
            @Index(name = "idx_framelog_media", columnList = "indexed_media_id"),
            @Index(name = "idx_framelog_shown_at", columnList = "shownAt")
        })
@NoArgsConstructor
@AllArgsConstructor
public class FrameLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne
    @JoinColumn(name = "indexed_media_id")
    private IndexedMedia indexedMedia;

    private LocalDateTime shownAt;
}
