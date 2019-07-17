package org.eea.document.type;

/**
 * The Enum NodeType.
 */
public enum NodeType {

  /** The file. */
  FILE("file_node"),
  /** The folder. */
  FOLDER("folder_node");

  /** The node value. */
  private String nodeValue;

  /**
   * Instantiates a new node type.
   *
   * @param nodeType the node type
   */
  NodeType(String nodeType) {
    this.nodeValue = nodeType;
  }

  /**
   * Gets the value.
   *
   * @return the value
   */
  public String getValue() {
    return nodeValue;
  }

  /**
   * To enum.
   *
   * @param nodeType the node type
   * @return the node type
   */
  public static NodeType toEnum(String nodeType) {
    return valueOf(nodeType, NodeType.values());
  }

  /**
   * Value of.
   *
   * @param nodeType the node type
   * @param values the values
   * @return the node type
   */
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
