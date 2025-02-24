package me.doqbu.template;

import me.doqbu.template.node.ForLoopNode;
import me.doqbu.template.node.Node;
import me.doqbu.template.node.StaticTextNode;
import me.doqbu.template.node.VariableNode;

import java.util.ArrayList;
import java.util.List;

public class Parser {
    public List<Node> parse(List<Token> tokens) {
        List<Node> nodes = new ArrayList<>();
        int i = 0;
        while (i < tokens.size()) {
            Token token = tokens.get(i);
            switch (token.type) {
                case STATIC_TEXT:
                    nodes.add(new StaticTextNode(token.value));
                    i++;
                    break;
                case VARIABLE:
                    nodes.add(new VariableNode(token.value));
                    i++;
                    break;
                case BLOCK:
                    i = handleBlock(tokens, i, nodes);
                    break;
            }
        }
        return nodes;
    }

    private int handleBlock(List<Token> tokens, int index, List<Node> nodes) {
        Token token = tokens.get(index);
        if (token.value.startsWith("for")) {
            return handleForBlock(tokens, index, nodes);
        } else {
            return index + 1;
        }
    }

    private int handleForBlock(List<Token> tokens, int index, List<Node> nodes) {
        Token token = tokens.get(index);
        String[] parts = token.value.split("\\s+");
        // FOR var IN iterable
        if (parts.length != 4 || !parts[0].equals("for") || !parts[2].equals("in")) {
            nodes.add(new StaticTextNode(token.value));
            return index;
        }

        String var = parts[1];
        String iterable = parts[3];
        List<Node> body = new ArrayList<>();
        int i = index + 1;
        while (i < tokens.size()) {
            token = tokens.get(i);
            if (token.type == TokenType.STATIC_TEXT) {
                body.add(new StaticTextNode(token.value));
            } else if (token.type == TokenType.VARIABLE) {
                body.add(new VariableNode(token.value));
            } else {
                if (token.value.isEmpty()) {
                    break;
                } else {
                    i = handleBlock(tokens, i, body);
                }
            }
            i++;
        }
        nodes.add(new ForLoopNode(var, iterable, body));
        return i;
    }
}
