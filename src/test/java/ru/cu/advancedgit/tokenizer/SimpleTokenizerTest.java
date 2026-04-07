package ru.cu.advancedgit.tokenizer;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SimpleTokenizerTest {

    private final SimpleTokenizer tokenizer = new SimpleTokenizer();

    @Test
    void shouldSplitTextIntoWords() {
        String text = "Java is great";

        Set<String> result = tokenizer.tokenize(text);

        assertEquals(Set.of("java", "is", "great"), result);
    }

    @Test
    void shouldIgnoreCase() {
        String text = "Java JAVA java";

        Set<String> result = tokenizer.tokenize(text);

        assertEquals(Set.of("java"), result);
    }

    @Test
    void shouldRemovePunctuation() {
        String text = "Hello, world! Java.";

        Set<String> result = tokenizer.tokenize(text);

        assertEquals(Set.of("hello", "world", "java"), result);
    }

    @Test
    void shouldReturnEmptySetForEmptyText() {
        String text = "";

        Set<String> result = tokenizer.tokenize(text);

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnEmptySetForBlankText() {
        String text = "   \n  \t  ";

        Set<String> result = tokenizer.tokenize(text);

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldHandleNullInput() {
        Set<String> result = tokenizer.tokenize(null);

        assertTrue(result.isEmpty());
    }
}