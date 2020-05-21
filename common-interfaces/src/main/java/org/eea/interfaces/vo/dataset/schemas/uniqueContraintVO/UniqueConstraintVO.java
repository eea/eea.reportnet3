package org.eea.interfaces.vo.dataset.schemas.uniqueContraintVO;

import java.util.List;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;



/**
 * The Class UniqueConstraintVO.
 */
@Getter
@Setter
@ToString
public class UniqueConstraintVO {

  /** The rules schema id. */
  private String uniqueId;

  /** The rules. */
  private List<String> fieldSchemaIds;

  /** The id dataset schema. */
  private String datasetSchemaId;

  /** The table schema id. */
  private String tableSchemaId;

  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    return Objects.hash(fieldSchemaIds, datasetSchemaId, tableSchemaId, uniqueId);
  }

  /**
   * Equals.
   *
   * @param obj the obj
   * @return true, if successful
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    UniqueConstraintVO other = (UniqueConstraintVO) obj;
    return Objects.equals(fieldSchemaIds, other.fieldSchemaIds)
        && Objects.equals(datasetSchemaId, other.datasetSchemaId)
        && Objects.equals(tableSchemaId, other.tableSchemaId)
        && Objects.equals(uniqueId, other.uniqueId);
  }



}
