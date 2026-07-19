package dev.romanempire.framd.report;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import dev.romanempire.framd.analytics.model.LintResult;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

@Slf4j
@Service
public class ReportService {

    private final TemplateEngine templateEngine = buildEngine();

    public void generateReport(LintResult lintResult) {
        try {
            LocalDateTime now = LocalDateTime.now();

            Context ctx = new Context();
            ctx.setVariable("lintResult", lintResult);
            ctx.setVariable("generatedAt", now);

            String xhtml = templateEngine.process("report/lint-report", ctx);

            String timestamp = now.format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
            Path out = Path.of("reports", "lint-report-" + timestamp + ".pdf");
            Files.createDirectories(out.getParent());

            try (OutputStream os = Files.newOutputStream(out)) {
                new PdfRendererBuilder()
                        .withHtmlContent(xhtml, null)
                        .toStream(os)
                        .run();
            }

            log.info("Report saved to {}", out.toAbsolutePath());
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate lint report", e);
        }
    }

    private static TemplateEngine buildEngine() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCharacterEncoding("UTF-8");

        SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.addTemplateResolver(resolver);
        return engine;
    }
}