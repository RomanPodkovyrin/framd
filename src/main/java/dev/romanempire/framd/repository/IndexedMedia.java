package dev.romanempire.framd.repository;


import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@Builder
@Table(name = "IndexedMedia")
@NoArgsConstructor
@AllArgsConstructor
public class IndexedMedia {
    @Id
    private String hash;
    @Column(nullable = false)
    private String path;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String extension;
    @Column(nullable = false)
    private LocalDateTime captureTime;
    private LocalDateTime lastIndexedTime;
    private LocalDateTime lastModifiedTime;
    @Column(nullable = false)
    private Integer width;
    @Column(nullable = false)
    private Integer height;
    @Column(nullable = false)
    private Long sizeInBytes;
    @Column(nullable = true)
    private String thumbnailPath;

    public String getFullPath() {
        return path + "/" + name + ((!extension.isEmpty()) ? "." + extension : "");
    }
}
