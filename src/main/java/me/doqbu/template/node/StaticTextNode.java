package me.doqbu.template.node;

import java.util.Map;

public class StaticTextNode implements Node {
    private final String text;

    public StaticTextNode(String text) {
        this.text = text;
    }

    @Override
    public String render(Map<String, Object> context) {
        return text;
    }
}
