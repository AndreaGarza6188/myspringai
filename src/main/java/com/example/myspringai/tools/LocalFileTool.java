package com.example.myspringai.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Built-in local file tool — allows the AI agent to list, read, and search files
 * in a configurable data directory.
 *
 * <p>The data directory is set via the {@code app.data.directory} property (default: {@code ./data}).
 * Sample data files are copied there automatically on first startup.
 */
@Component
public class LocalFileTool implements ToolProvider {

    private static final Logger log = LoggerFactory.getLogger(LocalFileTool.class);
    private static final int MAX_CONTENT_LENGTH = 4000;

    @Value("${app.data.directory:./data}")
    private String dataDirectory;

    /**
     * List all files available in the data directory.
     */
    @Tool(description = "List all files available in the local data directory. Use this to discover what files are available before reading them.")
    public String listFiles() {
        Path dir = Paths.get(dataDirectory).toAbsolutePath().normalize();
        if (!Files.exists(dir)) {
            return "Data directory does not exist: " + dir
                    + ". Configure app.data.directory or create the directory.";
        }
        try (Stream<Path> stream = Files.walk(dir)) {
            List<String> files = stream
                    .filter(Files::isRegularFile)
                    .map(p -> dir.relativize(p).toString())
                    .sorted()
                    .collect(Collectors.toList());
            if (files.isEmpty()) {
                return "No files found in data directory: " + dir;
            }
            return "Files in data directory (" + dir + "):\n" + String.join("\n", files);
        } catch (IOException e) {
            log.error("Error listing files in {}", dir, e);
            return "Error listing files: " + e.getMessage();
        }
    }

    /**
     * Read the content of a specific file from the data directory.
     */
    @Tool(description = "Read the full content of a specific file from the local data directory. Provide the file name exactly as shown by listFiles(). Returns the file content as text.")
    public String readFile(String fileName) {
        Path dir = Paths.get(dataDirectory).toAbsolutePath().normalize();
        Path filePath = dir.resolve(fileName).normalize();

        // Security: prevent path traversal outside the data directory
        if (!filePath.startsWith(dir)) {
            return "Access denied: '" + fileName + "' is outside the data directory.";
        }
        if (!Files.exists(filePath)) {
            return "File not found: '" + fileName + "'. Use listFiles() to see available files.";
        }
        try {
            String content = Files.readString(filePath);
            if (content.length() > MAX_CONTENT_LENGTH) {
                content = content.substring(0, MAX_CONTENT_LENGTH)
                        + "\n...[truncated, showing first " + MAX_CONTENT_LENGTH + " chars]";
            }
            return "=== " + fileName + " ===\n" + content;
        } catch (IOException e) {
            log.error("Error reading file '{}'", fileName, e);
            return "Error reading file '" + fileName + "': " + e.getMessage();
        }
    }

    /**
     * Search for files whose content contains a given text snippet.
     */
    @Tool(description = "Search all files in the local data directory for files containing a specific keyword or phrase. Returns the names of matching files and the lines where the text was found.")
    public String searchFiles(String keyword) {
        Path dir = Paths.get(dataDirectory).toAbsolutePath().normalize();
        if (!Files.exists(dir)) {
            return "Data directory does not exist: " + dir;
        }
        StringBuilder results = new StringBuilder();
        try (Stream<Path> stream = Files.walk(dir)) {
            stream.filter(Files::isRegularFile).forEach(file -> {
                try {
                    List<String> lines = Files.readAllLines(file);
                    List<String> matching = lines.stream()
                            .filter(line -> line.toLowerCase().contains(keyword.toLowerCase()))
                            .collect(Collectors.toList());
                    if (!matching.isEmpty()) {
                        results.append("\nFile: ").append(dir.relativize(file)).append("\n");
                        matching.stream().limit(5).forEach(line ->
                                results.append("  > ").append(line.strip()).append("\n"));
                        if (matching.size() > 5) {
                            results.append("  ... and ").append(matching.size() - 5).append(" more matching lines\n");
                        }
                    }
                } catch (IOException e) {
                    log.warn("Could not read file for search: {}", file, e);
                }
            });
        } catch (IOException e) {
            log.error("Error searching files in {}", dir, e);
            return "Error searching files: " + e.getMessage();
        }

        if (results.isEmpty()) {
            return "No files found containing: \"" + keyword + "\"";
        }
        return "Files containing \"" + keyword + "\":" + results;
    }
}
