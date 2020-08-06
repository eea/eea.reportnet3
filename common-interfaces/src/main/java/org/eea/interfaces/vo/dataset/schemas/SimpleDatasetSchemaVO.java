package org.eea.interfaces.vo.dataset.schemas;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The Class SimpleDatasetSchemaVO.
 */
@Getter
@Setter
@ToString
public class SimpleDatasetSchemaVO {

  /** The dataset name. */
  private String datasetName;

  /** The table schemas. */
  private List<SimpleTableSchemaVO> tables;
}
