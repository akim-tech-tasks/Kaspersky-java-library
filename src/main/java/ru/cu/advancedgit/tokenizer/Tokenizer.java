package ru.cu.advancedgit.tokenizer;

import java.util.Set;

public interface Tokenizer {
    Set<String> tokenize(String text);
}