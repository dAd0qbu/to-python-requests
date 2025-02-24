package me.doqbu.template.node;

import java.util.List;
import java.util.Map;

public class ForLoopNode implements Node {
    private final String variable;
    private final String iterable;
    private final List<Node> body;

    public ForLoopNode(String variable, String iterable, List<Node> body) {
        this.variable = variable;
        this.iterable = iterable;
        this.body = body;
    }

    @Override
    public String render(Map<String, Object> context) {
        VariableNode variableNode = new VariableNode(iterable);
        Object iterableVariable = variableNode.getVariable(context);

        if (iterableVariable == null) return "";

        StringBuilder result = new StringBuilder();
        int i = 0;
        List<?> items = (List<?>) iterableVariable;
        for (Object item : items) {
            context.put(variable, item);
            context.put("index", i++);
            for (Node node : body) {
                result.append(node.render(context));
            }
        }

        return result.toString();
    }
}
