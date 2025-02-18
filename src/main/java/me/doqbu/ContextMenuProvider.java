package me.doqbu;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.ToolType;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.ui.contextmenu.ContextMenuEvent;
import burp.api.montoya.ui.contextmenu.ContextMenuItemsProvider;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ContextMenuProvider implements ContextMenuItemsProvider {
    private final MontoyaApi api;


    public ContextMenuProvider(MontoyaApi api) {
        this.api = api;
    }

    @Override
    public List<Component> provideMenuItems(ContextMenuEvent event) {
        if (event.isFromTool(ToolType.LOGGER, ToolType.PROXY, ToolType.REPEATER, ToolType.TARGET, ToolType.INTRUDER)) {
            List<Component> menuItemsList = new ArrayList<>();
            JMenuItem retrieveRequestItem = new JMenuItem("To python requests");

            event.selectedRequestResponses();
            List<HttpRequestResponse> selectedMessages;
            if (event.messageEditorRequestResponse().isPresent()) {
                selectedMessages = List.of(event.messageEditorRequestResponse().get().requestResponse());
            } else {
                selectedMessages = event.selectedRequestResponses();
                Collections.reverse(selectedMessages);
            }

            List<HttpRequestResponse> finalSelectedMessages = selectedMessages;
            retrieveRequestItem.addActionListener(e -> {
                CodeGenerator codeGenerator = new CodeGenerator(api, finalSelectedMessages);
                codeGenerator.generate();
            });
            menuItemsList.add(retrieveRequestItem);

            return menuItemsList;
        }
        return null;
    }
}
