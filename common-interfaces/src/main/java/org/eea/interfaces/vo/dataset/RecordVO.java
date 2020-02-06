package org.eea.interfaces.vo.dataset;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import org.eea.interfaces.vo.dataset.enums.TypeErrorEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The Class RecordVO.
 */
@Getter
@Setter
@ToString
public class RecordVO implements Serializable {

  /**
   * The Constant serialVersionUID.
   */
  private static final long serialVersionUID = -5257537261370694057L;

  /**
   * The id mongo.
   */
  private String idRecordSchema;

  /**
   * The id.
   */
  private String id;

  /**
   * The fields.
   */
  private List<FieldVO> fields;

  /**
   * The id partition.
   */
  private Long datasetPartitionId;

  /** The data provider code. */
  private String dataProviderCode;

  /**
   * The validations.
   */
  private List<RecordValidationVO> recordValidations;

  /**
   * The sort criteria.
   */
  private String sortCriteria;

  /** The level error. */
  private TypeErrorEnum levelError;

  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    return Objects.hash(datasetPartitionId, fields, id, idRecordSchema, sortCriteria);
  }

  /**
   * Equals.
   *
   * @param obj the obj
   *
   * @return true, if successful
   */
  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    final RecordVO other = (RecordVO) obj;
    return Objects.equals(datasetPartitionId, other.datasetPartitionId)
        && Objects.equals(fields, other.fields) && Objects.equals(id, other.id)
        && Objects.equals(idRecordSchema, other.idRecordSchema);
  }


}
