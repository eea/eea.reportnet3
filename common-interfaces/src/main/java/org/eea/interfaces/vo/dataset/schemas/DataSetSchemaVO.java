package org.eea.interfaces.vo.dataset.schemas;


import java.util.List;
import java.util.Objects;
import org.bson.types.ObjectId;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class DataSetSchemaVO {

  private ObjectId idDataSetSchema;

  /** The table schemas. */
  private List<TableSchemaVO> tableSchemas;

  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    return Objects.hash(idDataSetSchema, tableSchemas);
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
        && Objects.equals(tableSchemas, other.tableSchemas);
  }

}
