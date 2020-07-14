package org.eea.dataflow.integration.executor.fme.domain;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The Class Collection.
 */
@Getter
@Setter
@ToString
public class FMECollection {

  /** The limit. */
  private Integer limit;

  /** The offset. */
  private Integer offset;

  /** The total count. */
  private Integer totalCount;

  /** The items. */
  private List<FMEItem> items;

}
