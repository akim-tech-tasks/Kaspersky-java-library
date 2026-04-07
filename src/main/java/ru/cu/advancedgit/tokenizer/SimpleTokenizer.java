package ru.cu.advancedgit.tokenizer;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public class SimpleTokenizer implements Tokenizer {

    @Override
    public Set<String> tokenize(String text) {
        if (text == null || text.isBlank()) {
            return Set.of();
        }

        return Arrays.stream(text.toLowerCase(Locale.ROOT).split("\\W+"))
                .filter(token -> !token.isBlank())
                .collect(Collectors.toSet());
    }
}