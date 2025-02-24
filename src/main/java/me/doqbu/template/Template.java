package me.doqbu.template;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.http.message.HttpRequestResponse;
import me.doqbu.Main;
import me.doqbu.Request;
import me.doqbu.template.node.Node;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Template {
    public static final String BLOCK_START = "{%";
    public static final String BLOCK_END = "%}";
    public static final String VARIABLE_START = "{{";
    public static final String VARIABLE_END = "}}";
    private static final String[] escapeCharacters = new String[0x100];

    static {
        for (int i = 0x00; i <= 0xFF; i++) escapeCharacters[i] = String.format("\\x%02x", i);
        for (int i = 0x20; i < 0x80; i++) escapeCharacters[i] = String.valueOf((char) i);
        escapeCharacters['\n'] = "\\n";
        escapeCharacters['\r'] = "\\r";
        escapeCharacters['\t'] = "\\t";
        escapeCharacters['"'] = "\\\"";
        escapeCharacters['\\'] = "\\\\";
    }

    private final MontoyaApi api;
    private final Map<String, Object> context = new HashMap<>();
    private String template;

    public Template(MontoyaApi api, List<HttpRequestResponse> selectedMessages) {
        this.api = api;

        List<Map<String, Object>> requests = new ArrayList<>();
        for (HttpRequestResponse requestResponse : selectedMessages) {
            Request request = new Request(requestResponse.request());
            requests.add(request.toMap());
        }
        context.put("requests", requests);

        loadTemplate();
    }

    public static String escapeString(String string) {
        StringBuilder escapedString = new StringBuilder();
        for (char c : string.toCharArray()) {
            escapedString.append(escapeCharacters[c]);
        }
        return escapedString.toString();
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

    public void generate() {
        Lexer lexer = new Lexer();
        Parser parser = new Parser();
        StringBuilder result = new StringBuilder();
        List<Token> tokens = lexer.lex(template);
        List<Node> nodes = parser.parse(tokens);
        for (Node node : nodes) {
//            api.logging().logToOutput(node.getClass().getName() + ": '" + node.render(context) + "'");
            result.append(node.render(context));
        }
        api.logging().logToOutput("=== Generated code ===");
        api.logging().logToOutput(result.toString());
        api.logging().logToOutput("======================");

        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(result.toString()), null);
    }
}
