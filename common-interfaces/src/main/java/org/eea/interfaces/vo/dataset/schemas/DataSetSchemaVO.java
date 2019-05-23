package org.eea.interfaces.vo.dataset.schemas;


import java.util.List;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter
@ToString
public class DataSetSchemaVO {


  /** The id data set schema. */
  private String idDataSetSchema;

  /** The name data set schema. */
  private String nameDataSetSchema;
  
  /** The table schemas. */
  private List<TableSchemaVO> tableSchemas;

  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    return Objects.hash(idDataSetSchema, tableSchemas, nameDataSetSchema);
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
    DataSetSchemaVO other = (DataSetSchemaVO) obj;
    return Objects.equals(idDataSetSchema, other.idDataSetSchema)
        && Objects.equals(tableSchemas, other.tableSchemas)
        && Objects.equals(nameDataSetSchema, other.nameDataSetSchema);
  }

}
