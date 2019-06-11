/*
 * 
 */
package org.eea.interfaces.vo.dataset.schemas;

import java.util.List;
import java.util.Objects;
import org.eea.interfaces.vo.dataset.schemas.rule.RuleRecordVO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The Class RecordSchema.
 */
@Getter
@Setter
@ToString
public class RecordSchemaVO {

  /** The id record schema. */
  private String idRecordSchema;


  /** The field schema. */
  private List<FieldSchemaVO> fieldSchema;


  /** The rule record. */
  private List<RuleRecordVO> ruleRecord;


  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    return Objects.hash(fieldSchema, idRecordSchema, ruleRecord);
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
    RecordSchemaVO other = (RecordSchemaVO) obj;
    return Objects.equals(fieldSchema, other.fieldSchema)
        && Objects.equals(idRecordSchema, other.idRecordSchema)
        && Objects.equals(ruleRecord, other.ruleRecord);
  }



}
