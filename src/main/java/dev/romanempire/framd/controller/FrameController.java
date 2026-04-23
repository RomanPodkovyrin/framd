package dev.romanempire.framd.controller;

import dev.romanempire.framd.frame.service.FrameService;
import dev.romanempire.framd.indexing.service.IndexService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/frame")
public class FrameController {

    private final FrameService frameService;

    private final IndexService indexService;

    @GetMapping("/next/{hash}")
    public ResponseEntity<FileSystemResource> getPreview(@PathVariable String hash) {
        var path = indexService.getPreviewPath(hash);
        return path.map(value ->
                        ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(new FileSystemResource(value)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
