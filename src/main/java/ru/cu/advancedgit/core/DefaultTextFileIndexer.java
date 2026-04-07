package ru.cu.advancedgit.core;

import ru.cu.advancedgit.api.TextFileIndexer;
import ru.cu.advancedgit.index.InMemoryInvertedIndex;
import ru.cu.advancedgit.tokenizer.Tokenizer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Default in-memory implementation of the text file indexer.
 */
public class DefaultTextFileIndexer implements TextFileIndexer {

    private final InMemoryInvertedIndex index;
    private final FileIndexer fileIndexer;

    public DefaultTextFileIndexer(Tokenizer tokenizer) {
        Objects.requireNonNull(tokenizer, "tokenizer must not be null");
        this.index = new InMemoryInvertedIndex();
        this.fileIndexer = new FileIndexer(tokenizer, index);
    }

    @Override
    public void addFile(Path file) {
        fileIndexer.indexFile(file);
    }

    @Override
    public void addDirectory(Path directory) {
        validateDirectory(directory);

        try (Stream<Path> paths = Files.walk(directory)) {
            paths.filter(Files::isRegularFile)
                    .filter(this::isSupportedTextFile)
                    .forEach(fileIndexer::indexFile);
        } catch (IOException e) {
            throw new RuntimeException("Failed to index directory: " + directory, e);
        }
    }

    @Override
    public Set<Path> search(String word) {
        if (word == null || word.isBlank()) {
            return Set.of();
        }

        return index.search(normalizeWord(word));
    }

    @Override
    public void remove(Path path) {
        Objects.requireNonNull(path, "path must not be null");

        Path normalizedPath = path.toAbsolutePath().normalize();

        if (Files.isRegularFile(normalizedPath)) {
            fileIndexer.removeFile(normalizedPath);
            return;
        }

        if (Files.isDirectory(normalizedPath)) {
            try (Stream<Path> paths = Files.walk(normalizedPath)) {
                paths.filter(Files::isRegularFile)
                        .forEach(fileIndexer::removeFile);
            } catch (IOException e) {
                throw new RuntimeException("Failed to remove directory from index: " + path, e);
            }
            return;
        }

        fileIndexer.removeFile(normalizedPath);
    }

    @Override
    public void close() {
    }

    private void validateDirectory(Path directory) {
        Objects.requireNonNull(directory, "directory must not be null");

        if (!Files.exists(directory)) {
            throw new IllegalArgumentException("Directory does not exist: " + directory);
        }
        if (!Files.isDirectory(directory)) {
            throw new IllegalArgumentException("Path is not a directory: " + directory);
        }
        if (!Files.isReadable(directory)) {
            throw new IllegalArgumentException("Directory is not readable: " + directory);
        }
    }

    private String normalizeWord(String word) {
        return word.toLowerCase(Locale.ROOT).trim();
    }

    private boolean isSupportedTextFile(Path path) {
        String fileName = path.getFileName().toString().toLowerCase(Locale.ROOT);
        return fileName.endsWith(".txt");
    }
}