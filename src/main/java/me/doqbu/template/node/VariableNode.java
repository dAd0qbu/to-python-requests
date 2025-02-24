package me.doqbu.template.node;

import me.doqbu.Json;
import me.doqbu.template.Template;

import java.util.HashMap;
import java.util.Map;

public class VariableNode implements Node {
    private final String name;

    public VariableNode(String name) {
        this.name = name;
    }

    @Override
    public String render(Map<String, Object> context) {
        // Skip format for Request.data
        if (name.endsWith(".data")) return getVariable(context).toString();
        return formatValue(getVariable(context));
    }

    public Object getVariable(Map<String, Object> context) {
        if (name.contains(".")) {
            return handleMapVariables(name, context);
        } else {
            return context.getOrDefault(name, null);
        }
    }

    private Object handleMapVariables(String variableName, Map<String, Object> context) {
        String[] keys = variableName.split("\\.", 2);

        Object keyVariable = context.getOrDefault(keys[0], null);
        if (keyVariable == null || keys.length == 1 || !(keyVariable instanceof Map)) {
            return keyVariable;
        }

        Map<String, Object> newContext = new HashMap<>(context);
        newContext.putAll((Map<String, Object>) keyVariable);

        return handleMapVariables(keys[1], newContext);
    }

    private String formatValue(Object object) {
        if (object instanceof Map) {
            Json json = Json.make(object);
            return json.toString();
        } else if (object == null) return "None";

        return Template.escapeString(object.toString());
    }
}