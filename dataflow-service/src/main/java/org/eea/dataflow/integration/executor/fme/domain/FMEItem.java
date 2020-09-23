package org.eea.dataflow.integration.executor.fme.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The Class Item.
 */
@Getter
@Setter
@ToString
public class FMEItem {

  /** The description. */
  private String description;

  /** The last publish date. */
  private String lastPublishDate;

  /** The last save date. */
  private String lastSaveDate;

  /** The name. */
  private String name;

  /** The repository name. */
  private String repositoryName;

  /** The title. */
  private String title;

  /**
   * The type: UNKNOWN, WORKSPACE, CUSTOMFORMAT, CUSTOMTRANSFORM or TEMPLATE.
   */
  private String type;

  /** The user name. */
  private String userName;
}
