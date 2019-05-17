package org.eea.dataset.schemas.domain;


import java.util.List;
import java.util.Objects;
import javax.persistence.Id;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author Mario Severa
 *
 */


@Getter
@Setter
@ToString
@Document(collection = "DataSetSchema")
public class DataSetSchema {


  /** The id. */
  @Id
  @Field(value = "_id")
  private ObjectId idDataSetSchema;

  /** The table schemas. */
  @Field(value = "tableSchemas")
  private List<TableSchema> tableSchemas;

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
    DataSetSchema other = (DataSetSchema) obj;
    return Objects.equals(idDataSetSchema, other.idDataSetSchema)
        && Objects.equals(tableSchemas, other.tableSchemas);
  }
}
