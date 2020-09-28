package org.eea.interfaces.vo.dataset;

import java.io.Serializable;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;



/**
 * The Class FailedValidationsDatasetVO.
 */
@Getter
@Setter
@ToString
public class FailedValidationsDatasetVO implements Serializable {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 6074897632789190703L;

  /** The id dataset. */
  private Long idDataset;

  /** The id dataset schema. */
  private String idDatasetSchema;

  /** The name data set schema. */
  private String nameDataSetSchema;

  /** The errors. */
  private List<?> errors;

  /** The total errors. */
  private Long totalRecords;

  /** The total filters. */
  private Long totalFilteredRecords;
}
