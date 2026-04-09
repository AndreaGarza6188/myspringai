package com.example.myspringai.tools;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link LocalFileTool}.
 *
 * <p>All tests use a temporary directory and do not require a live Spring context.
 */
class LocalFileToolTest {

    @TempDir
    Path tempDir;

    private LocalFileTool tool;

    @BeforeEach
    void setUp() {
        tool = new LocalFileTool();
        ReflectionTestUtils.setField(tool, "dataDirectory", tempDir.toString());
    }

    @Test
    void listFiles_returnsFileNames() throws IOException {
        Files.writeString(tempDir.resolve("hello.txt"), "hello world");
        Files.writeString(tempDir.resolve("notes.txt"), "some notes");

        String result = tool.listFiles();

        assertThat(result).contains("hello.txt");
        assertThat(result).contains("notes.txt");
    }

    @Test
    void listFiles_emptyDirectory() {
        String result = tool.listFiles();
        assertThat(result).contains("No files found");
    }

    @Test
    void readFile_returnsContent() throws IOException {
        Files.writeString(tempDir.resolve("sample.txt"), "Spring AI is great!");

        String result = tool.readFile("sample.txt");

        assertThat(result).contains("Spring AI is great!");
    }

    @Test
    void readFile_notFound() {
        String result = tool.readFile("nonexistent.txt");
        assertThat(result).contains("not found");
    }

    @Test
    void readFile_pathTraversalBlocked() {
        String result = tool.readFile("../../etc/passwd");
        assertThat(result).contains("Access denied");
    }

    @Test
    void searchFiles_findsKeyword() throws IOException {
        Files.writeString(tempDir.resolve("a.txt"), "The quick brown fox");
        Files.writeString(tempDir.resolve("b.txt"), "Nothing relevant here");

        String result = tool.searchFiles("quick brown");

        assertThat(result).contains("a.txt");
        assertThat(result).doesNotContain("b.txt");
    }

    @Test
    void searchFiles_noMatches() throws IOException {
        Files.writeString(tempDir.resolve("c.txt"), "unrelated content");

        String result = tool.searchFiles("zzznomatch");

        assertThat(result).contains("No files found containing");
    }
}
