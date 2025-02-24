package me.doqbu.template.node;

import java.util.Map;

public interface Node {
    String render(Map<String, Object> context);
}
