package me.doqbu.template;

import java.util.ArrayList;
import java.util.List;

public class Lexer {
    public List<Token> lex(String input) {
        List<Token> tokens = new ArrayList<>();
        int i = 0;
        StringBuilder currentText = new StringBuilder();
        while (i < input.length()) {
            if (i + 1 >= input.length()) {
                break;
            }

            char currentChar = input.charAt(i);
            String delimiter = currentChar + "" + input.charAt(i + 1);
            if (delimiter.equals(Template.VARIABLE_START)) {
                addTextToken(currentText, tokens);
                currentText.setLength(0);
                i = handleVariable(input, i, tokens);
            } else if (delimiter.equals(Template.BLOCK_START)) {
                addTextToken(currentText, tokens);
                currentText.setLength(0);
                i = handleBlock(input, i, tokens);
            } else {
                currentText.append(currentChar);
                i++;
            }
        }
        addTextToken(currentText, tokens);
        return tokens;
    }

    private void addTextToken(StringBuilder currentText, List<Token> tokens) {
        if (currentText.length() > 0) {
            tokens.add(new Token(TokenType.STATIC_TEXT, currentText.toString()));
            currentText.setLength(0);
        }
    }

    private int handleVariable(String input, int index, List<Token> tokens) {
        int variableEnd = input.indexOf(Template.VARIABLE_END, index);
        if (variableEnd == -1) {
            addTextToken(new StringBuilder(input.substring(index)), tokens);
            return input.length();
        }

        tokens.add(new Token(TokenType.VARIABLE, input.substring(index + 2, variableEnd).trim()));
        return variableEnd + 2;
    }

    private int handleBlock(String input, int index, List<Token> tokens) {
        int blockEnd = input.indexOf(Template.BLOCK_END, index);
        if (blockEnd == -1) {
            addTextToken(new StringBuilder(input.substring(index)), tokens);
            return input.length();
        }

        tokens.add(new Token(TokenType.BLOCK, input.substring(index + 2, blockEnd).trim()));
        return blockEnd + 2;
    }
}

