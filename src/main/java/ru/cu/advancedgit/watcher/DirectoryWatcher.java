package ru.cu.advancedgit.watcher;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class DirectoryWatcher implements AutoCloseable {

    private final WatchService watchService;
    private final Map<WatchKey, Path> keysToDirectories = new ConcurrentHashMap<>();
    private final FileChangeListener listener;

    private volatile boolean running;
    private Thread workerThread;

    public DirectoryWatcher(FileChangeListener listener) {
        this.listener = Objects.requireNonNull(listener, "listener must not be null");

        try {
            this.watchService = FileSystems.getDefault().newWatchService();
        } catch (IOException e) {
            throw new RuntimeException("Failed to create WatchService", e);
        }
    }

    public void registerDirectory(Path directory) {
        Objects.requireNonNull(directory, "directory must not be null");

        if (!Files.exists(directory)) {
            throw new IllegalArgumentException("Directory does not exist: " + directory);
        }
        if (!Files.isDirectory(directory)) {
            throw new IllegalArgumentException("Path is not a directory: " + directory);
        }

        Path normalizedDirectory = directory.toAbsolutePath().normalize();

        try {
            WatchKey key = normalizedDirectory.register(
                    watchService,
                    StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_MODIFY,
                    StandardWatchEventKinds.ENTRY_DELETE
            );
            keysToDirectories.put(key, normalizedDirectory);
        } catch (IOException e) {
            throw new RuntimeException("Failed to register directory: " + directory, e);
        }
    }

    public synchronized void start() {
        if (running) {
            return;
        }

        running = true;
        workerThread = new Thread(this::processEvents, "directory-watcher");
        workerThread.setDaemon(true);
        workerThread.start();
    }

    private void processEvents() {
        while (running) {
            WatchKey key;
            try {
                key = watchService.take();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                if (running) {
                    throw new RuntimeException("Watcher loop failed", e);
                }
                break;
            }

            Path directory = keysToDirectories.get(key);
            if (directory == null) {
                key.reset();
                continue;
            }

            for (WatchEvent<?> event : key.pollEvents()) {
                Kind<?> kind = event.kind();

                if (kind == StandardWatchEventKinds.OVERFLOW) {
                    continue;
                }

                Path relativePath = (Path) event.context();
                Path fullPath = directory.resolve(relativePath).toAbsolutePath().normalize();

                if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                    listener.onCreate(fullPath);
                } else if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
                    listener.onModify(fullPath);
                } else if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
                    listener.onDelete(fullPath);
                }
            }

            boolean valid = key.reset();
            if (!valid) {
                keysToDirectories.remove(key);
            }
        }
    }

    @Override
    public synchronized void close() {
        running = false;

        if (workerThread != null) {
            workerThread.interrupt();
        }

        try {
            watchService.close();
        } catch (IOException e) {
            throw new RuntimeException("Failed to close WatchService", e);
        }
    }
}