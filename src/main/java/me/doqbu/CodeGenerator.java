package me.doqbu;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.http.message.requests.HttpRequest;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CodeGenerator {
    private MontoyaApi api;
    private List<HttpRequestResponse> selectedMessages;
    private String template;
    private Map<String, Object> context = new HashMap<>();

    public CodeGenerator(MontoyaApi api, List<HttpRequestResponse> selectedMessages) {
        this.api = api;
        this.selectedMessages = selectedMessages;

        List<Map<String, Object>> requests = new ArrayList<>();
        for (HttpRequestResponse requestResponse : selectedMessages) {
            Request request = new Request(requestResponse.request());
            api.logging().logToOutput(requestResponse.request().parameters().toString());
            requests.add(request.toMap());
        }
        context.put("requests", requests);

        loadTemplate();
    }

    public void generate() {
        this.template = handleLoops(template, context);
        this.template = handleVariables(this.template, context);
        api.logging().logToOutput(template);
    }

    private void loadTemplate() {
        File templateFile = new File(Main.TEMPLATE_PATH);
        try (BufferedReader reader = new BufferedReader(new FileReader(templateFile))) {
            StringBuilder templateBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                templateBuilder.append(line).append("\n");
            }
            template = templateBuilder.toString();
        } catch (Exception e) {
            api.logging().logToError(e.getMessage());
        }
    }

    private String formatValue(Object object) {
        if (object instanceof Map) {
            Json json = Json.make((Map<String, Object>) object);
            return json.toString();
        } else if (object == null) return "None";

        return object.toString();
    }

    @Nullable
    private String handleMapVariables(String variableName, Map<String, Object> context) {
        String[] keys = variableName.split("\\.", 2);

        Object keyVariable = context.getOrDefault(keys[0], null);
        // api.logging().logToOutput("[DEBUG] Map Variable: " + keys[0] + " ( " + Arrays.toString(keys) + ") = " + keyVariable);
        if (keyVariable == null || keys.length == 1 || !(keyVariable instanceof Map)) return formatValue(keyVariable);

        Map<String, Object> newContext = new HashMap<>(context);
        newContext.putAll((Map<String, Object>) keyVariable);

        return handleMapVariables(keys[1], newContext);
    }

    private String handleVariables(String template, Map<String, Object> context) {
        Pattern pattern = Pattern.compile("\\{\\{\\s*([^\\}]*)\\s*\\}\\}");
        Matcher matcher = pattern.matcher(template);

        while (matcher.find()) {
            String variableName = matcher.group(1);
            // api.logging().logToOutput("[DEBUG] Variable: " + variableName);
            Object variableValue;
            if (variableName.contains(".")) {
                variableValue = handleMapVariables(variableName, context);
            } else {
                variableValue = context.get(variableName);
            }

            if (variableValue != null) {
                template = template.replace(matcher.group(), formatValue(variableValue));
            }
        }
        return template;
    }

    private String handleLoops(String template, Map<String, Object> context) {
        Pattern pattern = Pattern.compile("\\{%\\s*for\\s+(\\S+)\\s+in\\s+(\\S+)\\s*%\\}([\\s\\S]+)\\{%\\s*%\\}");
        Matcher matcher = pattern.matcher(template);
        StringBuilder listLoopContent = new StringBuilder();
        int index = 0;

        while (matcher.find()) {
            String loopVariable = matcher.group(1);
            String loopList = matcher.group(2);
            String loopContent = matcher.group(3);

            Object listObject = context.get(loopList);
            if (!(listObject instanceof List)) return listLoopContent.toString();

            List<?> list = (List<?>) listObject;
            StringBuilder loopContentBuilder = new StringBuilder();
            for (Object item : list) {
                Map<String, Object> newContext = new HashMap<>(context);
                newContext.put(loopVariable, item);
                newContext.put("index", index++);
                loopContentBuilder.append(handleVariables(loopContent, newContext));
            }
            matcher.appendReplacement(listLoopContent, loopContentBuilder.toString());

        }
        matcher.appendTail(listLoopContent);
        return listLoopContent.toString();
    }
}
