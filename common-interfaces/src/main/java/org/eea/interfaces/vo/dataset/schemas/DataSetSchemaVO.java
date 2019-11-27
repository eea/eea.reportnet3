package org.eea.interfaces.vo.dataset.schemas;


import java.util.List;
import java.util.Objects;
import org.eea.interfaces.vo.dataset.schemas.rule.RuleDataSetVO;
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

  /** The name data set schema. */
  private String nameDatasetSchema;

  /** The table schemas. */
  private List<TableSchemaVO> tableSchemas;


  /** The rule data set. */
  private List<RuleDataSetVO> ruleDataSet;


  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    return Objects.hash(idDataSetSchema, nameDatasetSchema, ruleDataSet, tableSchemas);
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
        && Objects.equals(nameDatasetSchema, other.nameDatasetSchema)
        && Objects.equals(ruleDataSet, other.ruleDataSet)
        && Objects.equals(tableSchemas, other.tableSchemas);
  }

}
