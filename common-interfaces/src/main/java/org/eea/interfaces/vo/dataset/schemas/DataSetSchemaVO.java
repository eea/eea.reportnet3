package org.eea.interfaces.vo.dataset.schemas;

import java.util.List;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The Class DataSetSchemaVO.
 */

/**
 * Checks if is available in public.
 *
 * @return true, if is available in public
 */
@Getter

/**
 * Sets the available in public.
 *
 * @param availableInPublic the new available in public
 */
@Setter

/**
 * To string.
 *
 * @return the java.lang. string
 */
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

  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    return Objects.hash(idDataSetSchema, description, nameDatasetSchema, tableSchemas, webform,
        availableInPublic);
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
        && Objects.equals(availableInPublic, other.availableInPublic);
  }
}
