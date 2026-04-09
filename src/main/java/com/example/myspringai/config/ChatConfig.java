package com.example.myspringai.config;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring AI configuration — provides shared beans used by the agent service.
 */
@Configuration
public class ChatConfig {

    /**
     * In-memory chat memory that stores up to 20 messages per session.
     *
     * <p>Replace {@link InMemoryChatMemoryRepository} with a database-backed
     * implementation (e.g., {@code JdbcChatMemoryRepository}) for persistence across restarts.
     */
    @Bean
    public ChatMemory chatMemory() {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(new InMemoryChatMemoryRepository())
                .maxMessages(20)
                .build();
    }
}
