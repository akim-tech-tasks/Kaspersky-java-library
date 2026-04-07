package ru.cu.advancedgit.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ru.cu.advancedgit.api.TextFileIndexer;
import ru.cu.advancedgit.tokenizer.SimpleTokenizer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultTextFileIndexerTest {

    @TempDir
    Path tempDir;

    private TextFileIndexer indexer;

    @BeforeEach
    void setUp() {
        indexer = new DefaultTextFileIndexer(new SimpleTokenizer());
    }

    @Test
    void shouldIndexSingleFile() throws IOException {
        Path file = Files.createFile(tempDir.resolve("a.txt"));
        Files.writeString(file, "Java is great");

        indexer.addFile(file);

        Set<Path> result = indexer.search("java");

        assertEquals(Set.of(file.toAbsolutePath().normalize()), result);
    }

    @Test
    void shouldIndexAllTxtFilesInDirectory() throws IOException {
        Path file1 = Files.createFile(tempDir.resolve("a.txt"));
        Path file2 = Files.createFile(tempDir.resolve("b.txt"));
        Path file3 = Files.createFile(tempDir.resolve("c.md"));

        Files.writeString(file1, "Java");
        Files.writeString(file2, "Spring Java");
        Files.writeString(file3, "Java");

        indexer.addDirectory(tempDir);

        Set<Path> result = indexer.search("java");

        assertEquals(
                Set.of(
                        file1.toAbsolutePath().normalize(),
                        file2.toAbsolutePath().normalize()
                ),
                result
        );
    }

    @Test
    void shouldSearchCaseInsensitively() throws IOException {
        Path file = Files.createFile(tempDir.resolve("a.txt"));
        Files.writeString(file, "Java");

        indexer.addFile(file);

        Set<Path> result = indexer.search("JAVA");

        assertEquals(Set.of(file.toAbsolutePath().normalize()), result);
    }

    @Test
    void shouldRemoveSingleFileFromIndex() throws IOException {
        Path file = Files.createFile(tempDir.resolve("a.txt"));
        Files.writeString(file, "Java Spring");

        indexer.addFile(file);
        indexer.remove(file);

        assertTrue(indexer.search("java").isEmpty());
        assertTrue(indexer.search("spring").isEmpty());
    }

    @Test
    void shouldRemoveDirectoryFromIndex() throws IOException {
        Path file1 = Files.createFile(tempDir.resolve("a.txt"));
        Path file2 = Files.createFile(tempDir.resolve("b.txt"));

        Files.writeString(file1, "Java");
        Files.writeString(file2, "Spring");

        indexer.addDirectory(tempDir);
        indexer.remove(tempDir);

        assertTrue(indexer.search("java").isEmpty());
        assertTrue(indexer.search("spring").isEmpty());
    }

    @Test
    void shouldReturnEmptyResultForUnknownWord() throws IOException {
        Path file = Files.createFile(tempDir.resolve("a.txt"));
        Files.writeString(file, "Java");

        indexer.addFile(file);

        assertTrue(indexer.search("python").isEmpty());
    }
}