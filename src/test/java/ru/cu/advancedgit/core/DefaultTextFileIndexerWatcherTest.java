package ru.cu.advancedgit.core;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ru.cu.advancedgit.api.TextFileIndexer;
import ru.cu.advancedgit.tokenizer.SimpleTokenizer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Set;
import java.util.function.BooleanSupplier;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultTextFileIndexerWatcherTest {

    @TempDir
    Path tempDir;

    private TextFileIndexer indexer;

    @AfterEach
    void tearDown() throws Exception {
        if (indexer != null) {
            indexer.close();
        }
    }

    @Test
    void shouldIndexNewFileCreatedInWatchedDirectory() throws Exception {
        indexer = new DefaultTextFileIndexer(new SimpleTokenizer());
        indexer.addDirectory(tempDir);

        Path file = tempDir.resolve("new-file.txt");
        Files.writeString(file, "Java");

        waitUntil(
                () -> indexer.search("java").contains(file.toAbsolutePath().normalize()),
                Duration.ofSeconds(3)
        );

        assertTrue(indexer.search("java").contains(file.toAbsolutePath().normalize()));
    }

    @Test
    void shouldReindexModifiedFileInWatchedDirectory() throws Exception {
        Path file = tempDir.resolve("file.txt");
        Files.writeString(file, "Java");

        indexer = new DefaultTextFileIndexer(new SimpleTokenizer());
        indexer.addDirectory(tempDir);

        waitUntil(
                () -> indexer.search("java").contains(file.toAbsolutePath().normalize()),
                Duration.ofSeconds(3)
        );

        Files.writeString(file, "Python");

        waitUntil(
                () -> indexer.search("python").contains(file.toAbsolutePath().normalize())
                        && indexer.search("java").isEmpty(),
                Duration.ofSeconds(3)
        );

        assertTrue(indexer.search("python").contains(file.toAbsolutePath().normalize()));
        assertTrue(indexer.search("java").isEmpty());
    }

    @Test
    void shouldRemoveDeletedFileFromIndex() throws Exception {
        Path file = tempDir.resolve("file.txt");
        Files.writeString(file, "Java");

        indexer = new DefaultTextFileIndexer(new SimpleTokenizer());
        indexer.addDirectory(tempDir);

        waitUntil(
                () -> indexer.search("java").contains(file.toAbsolutePath().normalize()),
                Duration.ofSeconds(3)
        );

        Files.delete(file);

        waitUntil(
                () -> indexer.search("java").isEmpty(),
                Duration.ofSeconds(3)
        );

        assertTrue(indexer.search("java").isEmpty());
    }

    private void waitUntil(BooleanSupplier condition, Duration timeout) throws InterruptedException {
        long deadline = System.currentTimeMillis() + timeout.toMillis();

        while (System.currentTimeMillis() < deadline) {
            if (condition.getAsBoolean()) {
                return;
            }
            Thread.sleep(50);
        }

        throw new AssertionError("Condition was not met within " + timeout.toMillis() + " ms");
    }
}