package org.eea.validation.persistence.schemas;

import java.util.Objects;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Field;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


/**
 * The Class ReferencedFieldSchema.
 */

@Getter
@Setter
@ToString
public class ReferencedFieldSchema {

  /** The id dataset schema. */
  @Field(value = "idDatasetSchema")
  private ObjectId idDatasetSchema;


  /** The id pk. */
  @Field(value = "idPk")
  private ObjectId idPk;



  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    return Objects.hash(idDatasetSchema, idPk);
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
    ReferencedFieldSchema other = (ReferencedFieldSchema) obj;
    return Objects.equals(idDatasetSchema, other.idDatasetSchema)
        && Objects.equals(idPk, other.idPk);
  }



}
