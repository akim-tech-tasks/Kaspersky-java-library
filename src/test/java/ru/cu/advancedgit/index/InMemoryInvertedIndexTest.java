package ru.cu.advancedgit.index;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InMemoryInvertedIndexTest {

    private InMemoryInvertedIndex index;

    @BeforeEach
    void setUp() {
        index = new InMemoryInvertedIndex();
    }

    @Test
    void shouldReturnFilesForIndexedWord() {
        Path file = Path.of("docs/a.txt");

        index.updateFile(file, Set.of("java", "spring"));

        Set<Path> result = index.search("java");

        assertEquals(Set.of(file), result);
    }

    @Test
    void shouldReturnEmptySetForUnknownWord() {
        Set<Path> result = index.search("unknown");

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldUpdateFileContentWhenReindexed() {
        Path file = Path.of("docs/a.txt");

        index.updateFile(file, Set.of("java", "spring"));
        index.updateFile(file, Set.of("python"));

        assertTrue(index.search("java").isEmpty());
        assertTrue(index.search("spring").isEmpty());
        assertEquals(Set.of(file), index.search("python"));
    }

    @Test
    void shouldKeepBothFilesForSameWord() {
        Path file1 = Path.of("docs/a.txt");
        Path file2 = Path.of("docs/b.txt");

        index.updateFile(file1, Set.of("java"));
        index.updateFile(file2, Set.of("java"));

        Set<Path> result = index.search("java");

        assertEquals(Set.of(file1, file2), result);
    }

    @Test
    void shouldRemoveFileFromIndex() {
        Path file = Path.of("docs/a.txt");

        index.updateFile(file, Set.of("java", "spring"));
        index.removeFile(file);

        assertTrue(index.search("java").isEmpty());
        assertTrue(index.search("spring").isEmpty());
    }

    @Test
    void shouldNotAffectOtherFilesWhenRemovingFile() {
        Path file1 = Path.of("docs/a.txt");
        Path file2 = Path.of("docs/b.txt");

        index.updateFile(file1, Set.of("java"));
        index.updateFile(file2, Set.of("java"));

        index.removeFile(file1);

        assertEquals(Set.of(file2), index.search("java"));
    }
}