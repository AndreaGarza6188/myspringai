package com.example.myspringai;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

/**
 * Smoke test — verifies the Spring application context loads.
 *
 * <p>Requires a configured OpenAI API key. In CI environments the key is
 * provided via the {@code OPENAI_API_KEY} environment variable.
 * A placeholder value is used here to allow the context to load without
 * making real API calls.
 */
@SpringBootTest
@TestPropertySource(properties = {
        "spring.ai.openai.api-key=test-key-placeholder",
        "app.data.directory=${java.io.tmpdir}/myspringai-test-data"
})
class MySpringAiApplicationTests {

    @Test
    void contextLoads() {
        // Verifies that all beans are created and wired correctly
    }
}
