package org.eea.document.type;

public enum NodeType {
	FILE("file_node"), FOLDER("folder_node");

    private String nodeValue;

    private NodeType(String nodeType) {
        this.nodeValue = nodeType;
    }

    public String getValue() {
        return nodeValue;
    }

    public static NodeType toEnum(String nodeType) {
        return valueOf(nodeType, NodeType.values());
    }

    // FIXME make it a generic utility method
    private static NodeType valueOf(String nodeType, NodeType[] values) {
        NodeType type = null;
        for (int i = 0; i < values.length && type == null; i++) {
            if (values[i].getValue().equals(nodeType)) {
                type = values[i];
            }
        }

        return type;
}
}
