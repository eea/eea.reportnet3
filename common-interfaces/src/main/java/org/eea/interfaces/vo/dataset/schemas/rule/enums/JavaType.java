package org.eea.interfaces.vo.dataset.schemas.rule.enums;

/**
 * The Class JavaType.
 */
public final class JavaType {

  /** The Constant BOOLEAN: {@value}. */
  public static final String BOOLEAN = "Boolean";

  /** The Constant NUMBER: {@value}. */
  public static final String NUMBER = "Number";

  /** The Constant DATE: {@value}. */
  public static final String DATE = "Date";

  /** The Constant STRING: {@value}. */
  public static final String STRING = "String";

  /** The Constant JSON: {@value}. */
  public static final String JSON = "JsonNode";

  /** The Constant OBJECT: {@value}. */
  public static final String OBJECT = "Object";

  /** The Constant UNSUPPORTED: {@value}. */
  public static final String UNSUPPORTED = "Unsupported";

  /** The Constant TIMESTAMP: {@value}. */
  public static final String TIMESTAMP = "Timestamp";

  /**
   * Instantiates a new java type.
   */
  private JavaType() {
    throw new IllegalStateException("Utility class");
  }
}
