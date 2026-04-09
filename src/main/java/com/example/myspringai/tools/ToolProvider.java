package com.example.myspringai.tools;

/**
 * Marker interface for custom AI tools.
 *
 * <p>Implement this interface to register your own tool with the AI agent.
 * Any Spring bean that implements this interface and contains methods annotated
 * with {@code @Tool} will be automatically discovered and made available to the AI.
 *
 * <h2>How to create a custom tool:</h2>
 * <pre>{@code
 * @Component
 * public class MyCustomTool implements ToolProvider {
 *
 *     @Tool(description = "Description of what this tool does. Be specific so the AI knows when to use it.")
 *     public String doSomething(String inputParam) {
 *         // Implement your tool logic here
 *         return "result";
 *     }
 *
 *     @Tool(description = "Another tool function with multiple parameters")
 *     public String doSomethingElse(String param1, int param2) {
 *         return "result for " + param1 + " with count " + param2;
 *     }
 * }
 * }</pre>
 *
 * <h2>Guidelines for writing tools:</h2>
 * <ul>
 *   <li>Keep tool methods focused on a single task</li>
 *   <li>Write descriptive {@code @Tool} descriptions — the AI decides when to use a tool based on the description</li>
 *   <li>Return String results that are human-readable</li>
 *   <li>Handle errors gracefully and return meaningful error messages</li>
 *   <li>Avoid side effects that cannot be undone (or clearly document them)</li>
 * </ul>
 */
public interface ToolProvider {
    // Marker interface — no methods required.
    // Add @Tool-annotated methods to your implementing class.
}
