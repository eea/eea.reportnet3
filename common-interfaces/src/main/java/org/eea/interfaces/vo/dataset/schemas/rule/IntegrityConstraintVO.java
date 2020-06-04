package org.eea.interfaces.vo.dataset.schemas.rule;

import java.util.List;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The Class IntegrityConstraintVO.
 */
@Getter
@Setter
@ToString
public class IntegrityConstraintVO {

  /** The id. */
  private String id;

  /** The origin. */
  private List<String> originFields;

  /** The referenced. */
  private List<String> referencedFields;

  /** The two way. */
  private Boolean isDoubleReferenced;

  /** The dataset schema id. */
  private String originDatasetSchemaId;

  /** The referenced dataset schema id. */
  private String referencedDatasetSchemaId;

  @Override
  public int hashCode() {
    return Objects.hash(id, originFields, referencedFields, isDoubleReferenced,
        originDatasetSchemaId, referencedDatasetSchemaId);
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
    IntegrityConstraintVO other = (IntegrityConstraintVO) obj;
    return Objects.equals(id, other.id) && Objects.equals(originFields, other.originFields)
        && Objects.equals(referencedFields, other.referencedFields)
        && Objects.equals(isDoubleReferenced, other.isDoubleReferenced)
        && Objects.equals(originDatasetSchemaId, other.originDatasetSchemaId)
        && Objects.equals(referencedDatasetSchemaId, other.referencedDatasetSchemaId);
  }
}
