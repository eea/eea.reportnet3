package org.eea.validation.persistence.schemas;


import java.util.List;
import java.util.Objects;
import javax.persistence.Id;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The Class DataSetSchema.
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

  /** The idDataFlow. */
  @Field(value = "idDataFlow")
  @Indexed(unique = false)
  private Long idDataFlow;

  /** The table schemas. */
  @Field(value = "tableSchemas")
  private List<TableSchema> tableSchemas;

  /** The available in public. */
  @Field(value = "availableInPublic")
  private boolean availableInPublic;


  /** The reference dataset. */
  @Field(value = "referenceDataset")
  private boolean referenceDataset;


  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    return Objects.hash(idDataFlow, idDataSetSchema, tableSchemas);
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
    DataSetSchema other = (DataSetSchema) obj;
    return Objects.equals(idDataFlow, other.idDataFlow)
        && Objects.equals(idDataSetSchema, other.idDataSetSchema)
        && Objects.equals(tableSchemas, other.tableSchemas);
  }


}
