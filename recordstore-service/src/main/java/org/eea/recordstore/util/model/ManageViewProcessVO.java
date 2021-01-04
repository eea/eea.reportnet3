package org.eea.recordstore.util.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * The Class ManageViewProcessVO.
 */
@Getter
@Setter
@AllArgsConstructor
public class ManageViewProcessVO {

  /** The dataset id. */
  private Long datasetId;


  /** The uuid. */
  private String uuid;

  /** The is materialized. */
  private boolean isMaterialized;

}

