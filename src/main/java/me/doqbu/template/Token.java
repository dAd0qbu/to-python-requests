package me.doqbu.template;

enum TokenType {
    STATIC_TEXT, VARIABLE, BLOCK,
}

public class Token {
    public final TokenType type;
    public final String value;

    public Token(TokenType type, String value) {
        this.type = type;
        this.value = value;
    }
}
