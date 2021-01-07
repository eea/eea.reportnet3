package org.eea.interfaces.vo.dataset;

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
public class FailedValidationsDatasetVO {

  /** The id dataset. */
  private Long idDataset;

  /** The id dataset schema. */
  private String idDatasetSchema;

  /** The name data set schema. */
  private String nameDataSetSchema;

  /** The errors. */
  private List<?> errors;

  /** The total records. */
  private Long totalRecords;

  /** The total errors. */
  private Long totalErrors;

  /** The total filters. */
  private Long totalFilteredRecords;
}
