/**
 * 
 */
package org.eea.dataset.persistence.schemas.domain;

import java.util.Objects;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
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
public class FieldSchema {

  /** The id field schema. */
  @Id
  @Field(value = "_id")
  private ObjectId idFieldSchema;


  /** The idRecord. */
  @Field(value = "idRecord")
  private ObjectId idRecord;

  /** The type. */
  @Field(value = "typeData")
  private String type;

  /** The type. */
  @Field(value = "headerName")
  private String headerName;

  /**
   * 
   *
   * @return
   */
  @Override
  public int hashCode() {
    return Objects.hash(headerName, idFieldSchema, idRecord, type);
  }

  /**
   * 
   *
   * @param obj
   * @return
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    FieldSchema other = (FieldSchema) obj;
    return Objects.equals(headerName, other.headerName)
        && Objects.equals(idFieldSchema, other.idFieldSchema)
        && Objects.equals(idRecord, other.idRecord) && type == other.type;
  }

}
