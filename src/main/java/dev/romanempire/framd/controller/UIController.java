package dev.romanempire.framd.controller;

import dev.romanempire.framd.frame.service.FrameService;
import dev.romanempire.framd.indexing.service.IndexService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class UIController {

    private final IndexService indexService;

    private final FrameService frameService;

    private static final Logger logger = LoggerFactory.getLogger(UIController.class);

    @GetMapping("/gallery")
    public String gallery(Model model) {
        model.addAttribute("images", indexService.getIndexInfoDateOrderedList());
        return "gallery :: grid";
    }

    @GetMapping("/scan/status")
    public String getScanStatus(Model model) {
        model.addAttribute("status", indexService.getScanStatus());
        return "fragments :: scan-status(status=${status})";
    }

    ///
    /// Returns the gallery grid fragment for HTMX partial page updates.
    /// Renders indexed images
    @GetMapping("/")
    public String index() {

        logger.info("Called Index");

        return "index.html";
    }

    @GetMapping("/frame")
    public String frame() {
        return "frame.html";
    }

    @GetMapping("/frame/info")
    public String getFrameInfo(Model model) {
        model.addAttribute("info", frameService.getNextFrameInfo());
        return "frame :: info";
    }

    @GetMapping("/admin")
    public String admin() {
        return "admin.html";
    }
}
