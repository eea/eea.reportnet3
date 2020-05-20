package org.eea.utils;

/**
 * The Class LiteralConstants.
 */
public final class LiteralConstants {

  /** The Constant DATA_REPORTING_TOPIC. */
  public static final String DATA_REPORTING_TOPIC = "DATA_REPORTING_TOPIC";

  /** The Constant COMMAND_TOPIC. */
  public static final String COMMAND_TOPIC = "COMMAND_TOPIC";

  /** The Constant BROADCAST_TOPIC. */
  public static final String BROADCAST_TOPIC = "BROADCAST_TOPIC";

  /** The Constant DATASET_PREFIX. */
  public static final String DATASET_PREFIX = "dataset_";

  /** The Constant DATASET_NAME. */
  public static final String DATASET_NAME = "dataset_%s";

  /** The Constant SNAPSHOT_EXTENSION. */
  public static final String SNAPSHOT_EXTENSION = ".snap";

  /** The Constant BEARER_TOKEN. */
  public static final String BEARER_TOKEN = "Bearer ";

  /** The Constant AUTHORIZATION_HEADER. */
  public static final String AUTHORIZATION_HEADER = "Authorization";

  /** The Constant ID_DATASET_SCHEMA. */
  public static final String ID_DATASET_SCHEMA = "idDatasetSchema";

  /**
   * Instantiates a new literal constants.
   */
  private LiteralConstants() {
    throw new IllegalStateException("Utility class");
  }
}
