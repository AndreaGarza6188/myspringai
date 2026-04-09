package com.example.myspringai.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * Example custom tool that ships with the project to demonstrate how users can
 * write their own tools for the AI agent to call.
 *
 * <h2>Steps to add YOUR OWN custom tool:</h2>
 * <ol>
 *   <li>Create a new class in this package (or any Spring-scanned package)</li>
 *   <li>Implement {@link ToolProvider}</li>
 *   <li>Annotate the class with {@code @Component}</li>
 *   <li>Add public methods annotated with {@code @Tool(description = "...")}</li>
 *   <li>Restart the app — the tool is automatically registered with the AI agent</li>
 * </ol>
 *
 * <p>The {@code description} in {@code @Tool} is critical: the AI reads it to decide
 * <em>when</em> to invoke your tool. Write it in plain, descriptive language.
 */
@Component
public class DateTimeTool implements ToolProvider {

    private static final DateTimeFormatter DATE_TIME_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Returns the current date and time.
     */
    @Tool(description = "Get the current local date and time. Use this whenever the user asks what time or date it is today.")
    public String getCurrentDateTime() {
        return "Current date and time: " + LocalDateTime.now().format(DATE_TIME_FMT);
    }

    /**
     * Calculates the number of days between two dates.
     *
     * @param fromDate start date in yyyy-MM-dd format
     * @param toDate   end date in yyyy-MM-dd format
     */
    @Tool(description = "Calculate the number of days between two dates. Provide dates in yyyy-MM-dd format (e.g. 2024-01-01). Use this for date arithmetic questions.")
    public String daysBetween(String fromDate, String toDate) {
        try {
            LocalDate from = LocalDate.parse(fromDate, DATE_FMT);
            LocalDate to = LocalDate.parse(toDate, DATE_FMT);
            long days = ChronoUnit.DAYS.between(from, to);
            return "There are " + Math.abs(days) + " days between " + fromDate + " and " + toDate
                    + (days < 0 ? " (the end date is before the start date)." : ".");
        } catch (Exception e) {
            return "Invalid date format. Please use yyyy-MM-dd (e.g. 2024-03-15).";
        }
    }

    /**
     * Performs basic arithmetic.
     *
     * @param operation one of: add, subtract, multiply, divide
     * @param a         first operand
     * @param b         second operand
     */
    @Tool(description = "Perform basic arithmetic: add, subtract, multiply, or divide two numbers. Use operation names like 'add', 'subtract', 'multiply', 'divide'.")
    public String calculate(String operation, double a, double b) {
        return switch (operation.toLowerCase().trim()) {
            case "add", "plus", "+"          -> a + " + " + b + " = " + (a + b);
            case "subtract", "minus", "-"    -> a + " - " + b + " = " + (a - b);
            case "multiply", "times", "*", "x" -> a + " × " + b + " = " + (a * b);
            case "divide", "/"               -> b == 0
                    ? "Cannot divide by zero."
                    : a + " ÷ " + b + " = " + (a / b);
            default -> "Unknown operation '" + operation + "'. Use: add, subtract, multiply, or divide.";
        };
    }
}
