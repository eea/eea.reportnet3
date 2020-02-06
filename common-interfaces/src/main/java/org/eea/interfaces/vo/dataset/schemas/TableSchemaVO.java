package org.eea.interfaces.vo.dataset.schemas;

import java.util.List;
import java.util.Objects;
import org.eea.interfaces.vo.dataset.schemas.rule.RuleTableVO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The Class TableSchemaVO.
 */
@Getter
@Setter
@ToString
public class TableSchemaVO {

  /** The id table schema. */
  private String idTableSchema;

  /** The description. */
  private String description;

  /** The table name schema. */
  private String nameTableSchema;

  /** The record schema. */
  private RecordSchemaVO recordSchema;

  /** The rule table. */
  private List<RuleTableVO> ruleTable;

  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    return Objects.hash(idTableSchema, description, nameTableSchema, recordSchema, ruleTable);
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
    TableSchemaVO other = (TableSchemaVO) obj;
    return Objects.equals(idTableSchema, other.idTableSchema)
        && Objects.equals(description, other.description)
        && Objects.equals(nameTableSchema, other.nameTableSchema)
        && Objects.equals(recordSchema, other.recordSchema)
        && Objects.equals(ruleTable, other.ruleTable);
  }
}
