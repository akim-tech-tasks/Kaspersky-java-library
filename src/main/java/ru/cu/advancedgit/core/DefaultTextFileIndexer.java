package ru.cu.advancedgit.core;

import ru.cu.advancedgit.api.TextFileIndexer;
import ru.cu.advancedgit.index.InMemoryInvertedIndex;
import ru.cu.advancedgit.tokenizer.Tokenizer;
import ru.cu.advancedgit.watcher.DirectoryWatcher;
import ru.cu.advancedgit.watcher.FileChangeListener;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

public class DefaultTextFileIndexer implements TextFileIndexer {

    private final InMemoryInvertedIndex index;
    private final FileIndexer fileIndexer;
    private final DirectoryWatcher directoryWatcher;
    private final AtomicBoolean closed = new AtomicBoolean(false);

    public DefaultTextFileIndexer(Tokenizer tokenizer) {
        Objects.requireNonNull(tokenizer, "tokenizer must not be null");

        this.index = new InMemoryInvertedIndex();
        this.fileIndexer = new FileIndexer(tokenizer, index);
        this.directoryWatcher = new DirectoryWatcher(createFileChangeListener());
        this.directoryWatcher.start();
    }

    @Override
    public void addFile(Path file) {
        ensureOpen();
        fileIndexer.indexFile(file);
    }

    @Override
    public void addDirectory(Path directory) {
        ensureOpen();
        validateDirectory(directory);

        Path normalizedDirectory = directory.toAbsolutePath().normalize();

        try (Stream<Path> paths = Files.walk(normalizedDirectory)) {
            paths.filter(Files::isRegularFile)
                    .filter(this::isSupportedTextFile)
                    .forEach(fileIndexer::indexFile);
        } catch (IOException e) {
            throw new RuntimeException("Failed to index directory: " + directory, e);
        }

        directoryWatcher.registerDirectory(normalizedDirectory);
    }

    @Override
    public Set<Path> search(String word) {
        ensureOpen();

        if (word == null || word.isBlank()) {
            return Set.of();
        }

        return index.search(normalizeWord(word));
    }

    @Override
    public void remove(Path path) {
        ensureOpen();
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
        if (closed.compareAndSet(false, true)) {
            directoryWatcher.close();
        }
    }

    private FileChangeListener createFileChangeListener() {
        return new FileChangeListener() {
            @Override
            public void onCreate(Path path) {
                if (isSupportedTextFile(path) && Files.isRegularFile(path)) {
                    fileIndexer.indexFile(path);
                }
            }

            @Override
            public void onModify(Path path) {
                if (isSupportedTextFile(path) && Files.isRegularFile(path)) {
                    fileIndexer.indexFile(path);
                }
            }

            @Override
            public void onDelete(Path path) {
                if (isSupportedTextFile(path)) {
                    fileIndexer.removeFile(path);
                }
            }
        };
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

    private void ensureOpen() {
        if (closed.get()) {
            throw new IllegalStateException("Indexer is already closed");
        }
    }

    private String normalizeWord(String word) {
        return word.toLowerCase(Locale.ROOT).trim();
    }

    private boolean isSupportedTextFile(Path path) {
        Path fileName = path.getFileName();
        if (fileName == null) {
            return false;
        }

        return fileName.toString().toLowerCase(Locale.ROOT).endsWith(".txt");
    }
}