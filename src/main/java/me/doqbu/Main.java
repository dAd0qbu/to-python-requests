package me.doqbu;

import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;

import java.io.*;
import java.nio.file.Paths;

public class Main implements BurpExtension {
    public static String TEMPLATE_PATH;

    @Override
    public void initialize(MontoyaApi api) {
        TEMPLATE_PATH = Paths.get("").toAbsolutePath() + "/template.py";

        api.extension().setName("to-python-requests");
        api.logging().logToOutput("Enabled to-python-requests");

        createTemplateFile(api);

        api.userInterface().registerContextMenuItemsProvider(new ContextMenuProvider(api));
    }

    private void createTemplateFile(MontoyaApi api) {
        File templateFile = new File(TEMPLATE_PATH);
        api.logging().logToOutput("Template file: " + templateFile.getAbsolutePath());
        if (!templateFile.exists()) {
            try {
                InputStream defaultTemplate = getClass().getResourceAsStream("/template.py");
                BufferedWriter writer = new BufferedWriter(new FileWriter(templateFile));
                writer.write(new String(defaultTemplate.readAllBytes()));
                writer.close();
            } catch (Exception e) {
                api.logging().logToError("Failed to create template file: " + e.getMessage());
            }
        }
    }

}