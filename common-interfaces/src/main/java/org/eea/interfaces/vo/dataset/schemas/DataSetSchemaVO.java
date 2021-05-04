package org.eea.interfaces.vo.dataset.schemas;

import java.util.List;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The Class DataSetSchemaVO.
 */
@Getter
@Setter
@ToString
public class DataSetSchemaVO {

  /** The id data set schema. */
  private String idDataSetSchema;

  /** The description. */
  private String description;

  /** The name data set schema. */
  private String nameDatasetSchema;

  /** The table schemas. */
  private List<TableSchemaVO> tableSchemas;

  /** The webform. */
  private WebformVO webform;

  /** The available in public. */
  private boolean availableInPublic;

  /** The reference dataset. */
  private boolean referenceDataset;

  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    return Objects.hash(idDataSetSchema, description, nameDatasetSchema, tableSchemas, webform,
        availableInPublic, referenceDataset);
  }

  /**
   * Equals.
   *
   * @param obj the obj
   * @return true, if successful
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    DataSetSchemaVO other = (DataSetSchemaVO) obj;
    return Objects.equals(idDataSetSchema, other.idDataSetSchema)
        && Objects.equals(description, other.description)
        && Objects.equals(nameDatasetSchema, other.nameDatasetSchema)
        && Objects.equals(tableSchemas, other.tableSchemas)
        && Objects.equals(webform, other.webform)
        && Objects.equals(availableInPublic, other.availableInPublic)
        && Objects.equals(referenceDataset, other.referenceDataset);
  }
}
