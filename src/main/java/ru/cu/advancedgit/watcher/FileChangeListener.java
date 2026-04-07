package ru.cu.advancedgit.watcher;

import java.nio.file.Path;

public interface FileChangeListener {

    void onCreate(Path path);

    void onModify(Path path);

    void onDelete(Path path);
}