package dev.romanempire.framd.controller;

import dev.romanempire.framd.indexing.service.IndexService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final IndexService indexService;

    @Value("${media.library.path}")
    private String scanPath;


    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

    @GetMapping("/")
    public String index() {

        logger.info("Called Index");
        indexService.getIndexInfo();

        return "index.html";
    }

    @PostMapping("/scan")
    public void scan(){
        indexService.indexPath(scanPath);

    }
}
