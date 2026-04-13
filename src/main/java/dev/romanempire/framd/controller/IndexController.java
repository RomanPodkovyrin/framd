package dev.romanempire.framd.controller;

import dev.romanempire.framd.indexing.service.IndexService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class IndexController {

    private final IndexService indexService;

    private static final Logger logger = LoggerFactory.getLogger(IndexController.class);

    @PostMapping("/scan")
    public ResponseEntity<Void> scan() {
        return indexService.tryFullScan()
                ? ResponseEntity.accepted().build()
                : ResponseEntity.status(HttpStatus.CONFLICT).build();
    }

    @GetMapping("/photo-count")
    public ResponseEntity<String> getImageCount() {
        return ResponseEntity.ok(
                switch (indexService.getCount()) {
                    case Long c when c != 1 -> c + " photos";
                    case Long c -> c + " photo";
                });
    }

    @GetMapping("/preview/{hash}")
    public ResponseEntity<FileSystemResource> getPreview(@PathVariable String hash) {
        var path = indexService.getPreviewPath(hash);
        return path.map(p ->
                        ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(new FileSystemResource(p)))
                .orElse(ResponseEntity.notFound().build());
    }
}
