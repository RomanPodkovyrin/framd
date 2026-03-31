package dev.romanempire.framd.controller;

import dev.romanempire.framd.indexing.service.IndexService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Path;

@RestController
@RequiredArgsConstructor
public class IndexController {


    private final IndexService indexService;

    // TODO: should probably not be used here, controller doesn't need to know this or controller this
    @Value("${media.library.path}")
    private String scanPath;


    private static final Logger logger = LoggerFactory.getLogger(IndexController.class);

    @PostMapping("/scan")
    public ResponseEntity<Void> scan() {
        return indexService.tryIndexPath(scanPath)
                ? ResponseEntity.accepted().build()
                : ResponseEntity.status(HttpStatus.CONFLICT).build();
    }

    @GetMapping("/photo-count")
    public ResponseEntity<String> getImageCount() {
        return ResponseEntity.ok(switch (indexService.getCount()) {
            case Long c when c != 1 -> c + " photos";
            case Long c -> c + " photo";
        });
    }

    @GetMapping("/thumbnail/{hash}")
    public ResponseEntity<Resource> getThumbnail(@PathVariable String hash) {
        var meta = indexService.getIndexInfo(hash); // TODO: handle 404
        var path = Path.of(meta.getThumbnailPath());
        Resource resource = new FileSystemResource(path);
        return ResponseEntity
                .ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(resource);
    }


}
