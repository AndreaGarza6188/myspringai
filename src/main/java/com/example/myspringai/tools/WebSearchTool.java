package com.example.myspringai.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * Built-in web tool — allows the AI agent to search the internet and fetch web content.
 *
 * <p>Provides two capabilities:
 * <ul>
 *   <li>{@link #searchWeb(String)} — search via DuckDuckGo Instant Answers (no API key required)</li>
 *   <li>{@link #fetchWebPage(String)} — retrieve raw content from any URL</li>
 * </ul>
 */
@Component
public class WebSearchTool implements ToolProvider {

    private static final Logger log = LoggerFactory.getLogger(WebSearchTool.class);
    private static final int MAX_CONTENT_LENGTH = 4000;
    private static final String USER_AGENT = "Mozilla/5.0 (compatible; SpringAI-Agent/1.0)";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public WebSearchTool(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    /**
     * Search the web using DuckDuckGo Instant Answers API (free, no API key needed).
     * Returns a summary answer and related topics.
     */
    @Tool(description = "Search the web for information about a query. Returns a summary and relevant results from DuckDuckGo. Use this when the user asks a question that requires up-to-date or general knowledge from the internet.")
    public String searchWeb(String query) {
        try {
            String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8);
            String url = "https://api.duckduckgo.com/?q=" + encoded
                    + "&format=json&no_html=1&skip_disambig=1";

            String body = get(url);
            JsonNode root = objectMapper.readTree(body);

            StringBuilder result = new StringBuilder();
            result.append("Search results for: ").append(query).append("\n\n");

            String abstractText = root.path("AbstractText").asText();
            if (!abstractText.isBlank()) {
                result.append("Summary: ").append(abstractText).append("\n");
                String source = root.path("AbstractSource").asText();
                String sourceUrl = root.path("AbstractURL").asText();
                if (!source.isBlank()) {
                    result.append("Source: ").append(source);
                    if (!sourceUrl.isBlank()) {
                        result.append(" (").append(sourceUrl).append(")");
                    }
                    result.append("\n");
                }
            }

            JsonNode relatedTopics = root.path("RelatedTopics");
            if (relatedTopics.isArray() && !relatedTopics.isEmpty()) {
                result.append("\nRelated Topics:\n");
                int count = 0;
                for (JsonNode topic : relatedTopics) {
                    if (count >= 5) break;
                    String text = topic.path("Text").asText();
                    if (!text.isBlank()) {
                        result.append("- ").append(text).append("\n");
                        count++;
                    }
                }
            }

            if (result.toString().equals("Search results for: " + query + "\n\n")) {
                return "No instant-answer results found for \"" + query
                        + "\". Try fetchWebPage() with a specific URL for more details.";
            }
            return result.toString();
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Web search failed for query '{}': {}", query, e.getMessage());
            return "Web search failed: " + e.getMessage();
        }
    }

    /**
     * Fetch the raw content of a web page at the given URL.
     */
    @Tool(description = "Fetch and return the content of a specific web page by its URL. Use this when the user provides a URL or when you need to retrieve detailed information from a known web address.")
    public String fetchWebPage(String url) {
        try {
            String content = get(url);
            if (content.length() > MAX_CONTENT_LENGTH) {
                content = content.substring(0, MAX_CONTENT_LENGTH) + "\n...[content truncated, showing first "
                        + MAX_CONTENT_LENGTH + " characters]";
            }
            return "Content from " + url + ":\n\n" + content;
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Failed to fetch URL '{}': {}", url, e.getMessage());
            return "Failed to fetch URL '" + url + "': " + e.getMessage();
        }
    }

    private String get(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(15))
                .header("User-Agent", USER_AGENT)
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }
}
