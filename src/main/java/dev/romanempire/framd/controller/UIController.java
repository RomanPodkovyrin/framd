package dev.romanempire.framd.controller;

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

    private static final Logger logger = LoggerFactory.getLogger(UIController.class);

    @GetMapping("/gallery")
    public String gallery(Model model) {
        model.addAttribute("images", indexService.getIndexInfoDateOrderedList());
        return "gallery :: grid";
    }

    ///
    /// Returns the gallery grid fragment for HTMX partial page updates.
    /// Renders indexed images
    @GetMapping("/")
    public String index() {

        logger.info("Called Index");

        return "index.html";
    }

    @GetMapping("/admin")
    public String admin() {
        return "admin.html";
    }

}
