package ru.cu.advancedgit.api;

import java.nio.file.Path;
import java.util.Set;

public interface TextFileIndexer extends AutoCloseable {

    void addFile(Path file);

    void addDirectory(Path directory);

    Set<Path> search(String word);

    void remove(Path path);

    @Override
    void close();
}