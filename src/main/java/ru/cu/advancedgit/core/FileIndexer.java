package ru.cu.advancedgit.core;

import ru.cu.advancedgit.index.InMemoryInvertedIndex;
import ru.cu.advancedgit.tokenizer.Tokenizer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Set;

/**
 * Reads files, tokenizes their content and updates the inverted index.
 */
public class FileIndexer {

    private final Tokenizer tokenizer;
    private final InMemoryInvertedIndex index;

    public FileIndexer(Tokenizer tokenizer, InMemoryInvertedIndex index) {
        this.tokenizer = Objects.requireNonNull(tokenizer, "tokenizer must not be null");
        this.index = Objects.requireNonNull(index, "index must not be null");
    }

    public void indexFile(Path file) {
        validateFile(file);

        try {
            String content = Files.readString(file);
            Set<String> words = tokenizer.tokenize(content);
            index.updateFile(file.toAbsolutePath().normalize(), words);
        } catch (IOException e) {
            throw new RuntimeException("Failed to index file: " + file, e);
        }
    }

    public void removeFile(Path file) {
        Objects.requireNonNull(file, "file must not be null");
        index.removeFile(file.toAbsolutePath().normalize());
    }

    private void validateFile(Path file) {
        Objects.requireNonNull(file, "file must not be null");

        if (!Files.exists(file)) {
            throw new IllegalArgumentException("File does not exist: " + file);
        }
        if (!Files.isRegularFile(file)) {
            throw new IllegalArgumentException("Path is not a regular file: " + file);
        }
        if (!Files.isReadable(file)) {
            throw new IllegalArgumentException("File is not readable: " + file);
        }
    }
}