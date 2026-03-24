package dev.romanempire.framd;

import dev.romanempire.framd.indexing.IndexService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final IndexService indexService;

    @RequestMapping("/")
    public String index() {

        indexService.indexPath("/Users/roman/Downloads");
        return "index.html";
    }
}
