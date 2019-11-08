package org.eea.interfaces.vo.dataset;

import java.io.Serializable;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


/**
 * The class StatisticsVO.
 *
 */
@Getter
@Setter
@ToString
public class StatisticsVO implements Serializable {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 3252008086898104546L;

  /** The dataset id. */
  private String idDataSetSchema;

  /** The name data set schema. */
  private String nameDataSetSchema;

  /** The dataset errors. */
  private Boolean datasetErrors;

  /** The tables. */
  private List<TableStatisticsVO> tables;

}
