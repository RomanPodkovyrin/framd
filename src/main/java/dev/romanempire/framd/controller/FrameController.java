package dev.romanempire.framd.controller;


import dev.romanempire.framd.frame.FrameService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Path;

@RestController
@RequiredArgsConstructor
@RequestMapping("/frame")
public class FrameController {

    private final FrameService frameService;

    @GetMapping("/next")
    public ResponseEntity<FileSystemResource> getNextFrame() {
        var path = Path.of(frameService.getNextFrame());
        return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(new FileSystemResource(path));
    }
}
