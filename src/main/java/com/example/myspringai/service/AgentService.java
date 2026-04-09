package com.example.myspringai.service;

import com.example.myspringai.tools.ToolProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * Core agent service.
 *
 * <p>Handles chat requests by:
 * <ol>
 *   <li>Maintaining per-session conversation history (via {@link ChatMemory})</li>
 *   <li>Automatically registering all {@link ToolProvider} beans as callable tools</li>
 *   <li>Sending the prompt + tools to the configured AI model</li>
 * </ol>
 *
 * <p>Any Spring bean that implements {@link ToolProvider} and contains
 * {@code @Tool}-annotated methods is automatically picked up — no wiring needed.
 */
@Service
public class AgentService {

    private static final Logger log = LoggerFactory.getLogger(AgentService.class);

    private static final String SYSTEM_PROMPT = """
            You are a helpful, knowledgeable AI assistant with access to several tools.

            Available capabilities:
            - Search the web for up-to-date information (searchWeb, fetchWebPage)
            - Read and search files in the local data directory (listFiles, readFile, searchFiles)
            - Get current date/time and perform date calculations (getCurrentDateTime, daysBetween)
            - Perform basic arithmetic (calculate)
            - Any additional tools registered by the user

            Guidelines:
            - Always try to understand the user's intent before choosing a tool.
            - When the user asks about local data or documents, use the file tools first.
            - When the user asks a question needing current or general knowledge, use searchWeb.
            - If you are unsure which tool to use, ask the user for clarification.
            - Respond in the same language as the user (Chinese or English).
            - Be concise, accurate, and helpful.
            """;

    private final ChatClient chatClient;
    private final ToolCallback[] toolCallbacks;

    public AgentService(ChatClient.Builder chatClientBuilder,
                        List<ToolProvider> toolProviders,
                        ChatMemory chatMemory) {

        // Build tool callbacks once from all registered ToolProvider beans
        this.toolCallbacks = toolProviders.stream()
                .flatMap(provider -> Arrays.stream(
                        MethodToolCallbackProvider.builder()
                                .toolObjects(provider)
                                .build()
                                .getToolCallbacks()))
                .toArray(ToolCallback[]::new);

        log.info("Registered {} tool callback(s) from {} ToolProvider bean(s)",
                toolCallbacks.length, toolProviders.size());

        this.chatClient = chatClientBuilder
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
    }

    /**
     * Process a user message within the given session and return the AI response.
     *
     * @param sessionId   unique session identifier (used for conversation memory)
     * @param userMessage the user's input text
     * @return the AI's response text
     */
    public String chat(String sessionId, String userMessage) {
        log.debug("Chat [session={}]: {}", sessionId, userMessage);
        String response = chatClient.prompt()
                .user(userMessage)
                .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, sessionId))
                .toolCallbacks(toolCallbacks)
                .call()
                .content();
        log.debug("Response [session={}]: {}", sessionId, response);
        return response;
    }
}
