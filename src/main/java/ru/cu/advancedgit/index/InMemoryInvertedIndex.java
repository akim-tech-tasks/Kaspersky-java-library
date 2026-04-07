package ru.cu.advancedgit.index;

import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class InMemoryInvertedIndex {

    private final ConcurrentMap<String, Set<Path>> wordToFiles = new ConcurrentHashMap<>();
    private final ConcurrentMap<Path, Set<String>> fileToWords = new ConcurrentHashMap<>();

    /**
     * Replaces indexed content for the given file.
     * Removes old word mappings for the file and adds new ones.
     */
    public synchronized void updateFile(Path file, Set<String> newWords) {
        Objects.requireNonNull(file, "file must not be null");
        Objects.requireNonNull(newWords, "newWords must not be null");

        Set<String> oldWords = fileToWords.getOrDefault(file, Collections.emptySet());

        for (String oldWord : oldWords) {
            Set<Path> files = wordToFiles.get(oldWord);
            if (files != null) {
                files.remove(file);
                if (files.isEmpty()) {
                    wordToFiles.remove(oldWord, files);
                }
            }
        }

        Set<String> wordsToIndex = new HashSet<>(newWords);

        for (String newWord : wordsToIndex) {
            wordToFiles.computeIfAbsent(newWord, key -> ConcurrentHashMap.newKeySet())
                    .add(file);
        }

        fileToWords.put(file, wordsToIndex);
    }

    /**
     * Completely removes the file and all its word mappings from the index.
     */
    public synchronized void removeFile(Path file) {
        Objects.requireNonNull(file, "file must not be null");

        Set<String> words = fileToWords.remove(file);
        if (words == null) {
            return;
        }

        for (String word : words) {
            Set<Path> files = wordToFiles.get(word);
            if (files != null) {
                files.remove(file);
                if (files.isEmpty()) {
                    wordToFiles.remove(word, files);
                }
            }
        }
    }

    /**
     * Returns a copy of files containing the given word.
     */
    public Set<Path> search(String word) {
        Objects.requireNonNull(word, "word must not be null");

        Set<Path> files = wordToFiles.get(word);
        if (files == null) {
            return Set.of();
        }
        return Set.copyOf(files);
    }
}