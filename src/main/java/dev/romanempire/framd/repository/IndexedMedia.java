package dev.romanempire.framd.repository;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.*;

@Data
@Entity
@Builder
@Table(name = "IndexedMedia")
@NoArgsConstructor
@AllArgsConstructor
@With
public class IndexedMedia {
    @Id
    private String hash;

    @Column(nullable = false)
    private String path;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String extension;

    @Column(nullable = true)
    private LocalDateTime captureTime;

    @EqualsAndHashCode.Exclude
    private LocalDateTime lastIndexedTime;

    @EqualsAndHashCode.Exclude
    private LocalDateTime lastModifiedTime;

    @Column(nullable = true)
    private Integer width;

    @Column(nullable = true)
    private Integer height;

    @Column(nullable = false)
    private Long sizeInBytes;

    @Column(nullable = true)
    @EqualsAndHashCode.Exclude
    private String previewPath;

    public String getFullPath() {
        return path + "/" + name + getPossibleExtension();
    }

    private String getPossibleExtension() {
        return (!extension.isEmpty()) ? "." + extension : "";
    }

    public String generatePreviewPathFromHash() {
        return hash.substring(0, 2) + "/" + hash.substring(2, 4) + "/" + hash.substring(8) + getPossibleExtension();
    }
}
