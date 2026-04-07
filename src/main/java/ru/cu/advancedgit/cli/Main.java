package ru.cu.advancedgit.cli;

import ru.cu.advancedgit.api.TextFileIndexer;
import ru.cu.advancedgit.core.DefaultTextFileIndexer;
import ru.cu.advancedgit.tokenizer.SimpleTokenizer;

import java.nio.file.Path;
import java.util.Scanner;
import java.util.Set;

public class Main {

    public static void main(String[] args) {
        try (TextFileIndexer indexer = new DefaultTextFileIndexer(new SimpleTokenizer());
             Scanner scanner = new Scanner(System.in)) {

            printWelcome();

            while (true) {
                System.out.print("> ");
                String line = scanner.nextLine();

                if (line == null || line.isBlank()) {
                    continue;
                }

                String[] parts = line.trim().split("\\s+", 2);
                String command = parts[0];

                try {
                    switch (command) {
                        case "add-file" -> {
                            requireArgument(parts);
                            indexer.addFile(Path.of(parts[1]));
                            System.out.println("File added to index");
                        }
                        case "add-dir" -> {
                            requireArgument(parts);
                            indexer.addDirectory(Path.of(parts[1]));
                            System.out.println("Directory added to index");
                        }
                        case "search" -> {
                            requireArgument(parts);
                            Set<Path> result = indexer.search(parts[1]);
                            printSearchResult(result);
                        }
                        case "remove" -> {
                            requireArgument(parts);
                            indexer.remove(Path.of(parts[1]));
                            System.out.println("Removed from index");
                        }
                        case "help" -> printHelp();
                        case "exit" -> {
                            System.out.println("Bye!");
                            return;
                        }
                        default -> System.out.println("Unknown command. Type 'help' to see available commands.");
                    }
                } catch (Exception e) {
                    System.out.println("Error: " + e.getMessage());
                }
            }
        }
    }

    private static void printWelcome() {
        System.out.println("Text File Indexer CLI");
        printHelp();
    }

    private static void printHelp() {
        System.out.println("Available commands:");
        System.out.println("  add-file <path>   - add a single file to the index");
        System.out.println("  add-dir <path>    - add a directory to the index");
        System.out.println("  search <word>     - search files containing the word");
        System.out.println("  remove <path>     - remove file or directory from the index");
        System.out.println("  help              - show this help");
        System.out.println("  exit              - exit the program");
    }

    private static void requireArgument(String[] parts) {
        if (parts.length < 2 || parts[1].isBlank()) {
            throw new IllegalArgumentException("Command argument is required");
        }
    }

    private static void printSearchResult(Set<Path> result) {
        if (result.isEmpty()) {
            System.out.println("No files found");
            return;
        }

        System.out.println("Found files:");
        result.forEach(path -> System.out.println("  " + path));
    }
}