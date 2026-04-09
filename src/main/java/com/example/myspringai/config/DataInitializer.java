package com.example.myspringai.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.stream.Stream;

/**
 * Initializes the data directory on startup.
 *
 * <p>If the data directory is empty (or does not exist), sample files are copied
 * from {@code classpath:data/} so the application is usable out of the box.
 */
@Component
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    /** Sample files to copy from the classpath into the data directory. */
    private static final String[] SAMPLE_FILES = {
            "data/products.txt",
            "data/faq.txt",
            "data/readme.txt"
    };

    @Value("${app.data.directory:./data}")
    private String dataDirectory;

    @EventListener(ApplicationReadyEvent.class)
    public void initDataDirectory() throws IOException {
        Path dir = Paths.get(dataDirectory).toAbsolutePath().normalize();
        Files.createDirectories(dir);

        if (isDirectoryEmpty(dir)) {
            log.info("Data directory is empty — copying sample files to {}", dir);
            for (String resource : SAMPLE_FILES) {
                copyClasspathResource(resource, dir);
            }
            log.info("Sample data files copied to {}", dir);
        } else {
            log.info("Data directory already contains files: {}", dir);
        }
    }

    private boolean isDirectoryEmpty(Path dir) throws IOException {
        try (Stream<Path> entries = Files.list(dir)) {
            return entries.findFirst().isEmpty();
        }
    }

    private void copyClasspathResource(String resourcePath, Path targetDir) {
        ClassPathResource resource = new ClassPathResource(resourcePath);
        if (!resource.exists()) {
            log.warn("Sample resource not found on classpath: {}", resourcePath);
            return;
        }
        Path fileName = Paths.get(resourcePath).getFileName();
        Path target = targetDir.resolve(fileName);
        try (InputStream in = resource.getInputStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            log.debug("Copied {} -> {}", resourcePath, target);
        } catch (IOException e) {
            log.warn("Could not copy sample file '{}': {}", resourcePath, e.getMessage());
        }
    }
}
