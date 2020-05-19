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

  /** The Constant STRING. */
  public static final String STRING = "String";

  /** The Constant UNSUPPORTED: {@value}. */
  public static final String UNSUPPORTED = "Unsupported";

  /**
   * Instantiates a new java type.
   */
  private JavaType() {
    throw new IllegalStateException("Utility class");
  }
}
