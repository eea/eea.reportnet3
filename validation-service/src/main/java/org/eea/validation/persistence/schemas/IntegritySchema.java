package org.eea.validation.persistence.schemas;

import java.util.List;
import java.util.Objects;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


/**
 * The Class IntegritySchema.
 */
@Getter
@Setter
@ToString
@Document(collection = "IntegritySchema")
public class IntegritySchema {

  /** The id. */
  @Id
  @Field(value = "_id")
  private ObjectId id;

  /** The origin. */
  @Field(value = "originFields")
  private List<ObjectId> originFields;

  /** The referenced. */
  @Field(value = "referencedFields")
  private List<ObjectId> referencedFields;

  /** The two way. */
  @Field(value = "isDoubleReferenced")
  private Boolean isDoubleReferenced;

  /** The dataset schema id. */
  @Field(value = "originDatasetSchemaId")
  private ObjectId originDatasetSchemaId;

  /** The referenced dataset schema id. */
  @Field(value = "referencedDatasetSchemaId")
  private ObjectId referencedDatasetSchemaId;

  /** The rule id. */
  @Field(value = "ruleId")
  private ObjectId ruleId;


  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    return Objects.hash(originFields, referencedFields, isDoubleReferenced, originDatasetSchemaId,
        referencedDatasetSchemaId, ruleId);
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
    IntegritySchema other = (IntegritySchema) obj;
    return Objects.equals(originFields, other.originFields)
        && Objects.equals(referencedFields, other.referencedFields)
        && Objects.equals(isDoubleReferenced, other.isDoubleReferenced)
        && Objects.equals(originDatasetSchemaId, other.originDatasetSchemaId)
        && Objects.equals(referencedDatasetSchemaId, other.referencedDatasetSchemaId)
        && Objects.equals(ruleId, other.ruleId);
  }

}
