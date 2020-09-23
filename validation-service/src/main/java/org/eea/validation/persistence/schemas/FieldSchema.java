package org.eea.validation.persistence.schemas;

import java.util.Objects;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import org.bson.types.ObjectId;
import org.eea.interfaces.vo.dataset.enums.DataType;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The Class FieldSchema.
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

  /** The description. */
  @Field(value = "description")
  private String description;

  /** The idRecord. */
  @Field(value = "idRecord")
  private ObjectId idRecord;

  /** The type. */
  @Field(value = "typeData")
  @Enumerated(EnumType.STRING)
  private DataType type;

  /** The type. */
  @Field(value = "headerName")
  private String headerName;

  /** The code list items. */
  @Field(value = "codelistItems")
  private String[] codelistItems;

  /** The required. */
  @Field(value = "required")
  private Boolean required;

  /** The is PK. */
  @Field(value = "isPK")
  private Boolean isPK;


  /** The is P kreferenced. */
  @Field(value = "isPKreferenced")
  private Boolean isPKreferenced;

  /** The reference FK. */
  @Field(value = "referencedField")
  private ReferencedFieldSchema referencedField;

  /** The pk must be used. */
  @Field(value = "pkMustBeUsed")
  private Boolean pkMustBeUsed;

  /** The pk has multiple values. */
  @Field(value = "pkHasMultipleValues")
  private Boolean pkHasMultipleValues;

  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    return Objects.hash(headerName, idFieldSchema, idRecord, type, codelistItems, required,
        description, isPK, pkMustBeUsed, pkHasMultipleValues);
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
    FieldSchema other = (FieldSchema) obj;
    return Objects.equals(headerName, other.headerName)
        && Objects.equals(idFieldSchema, other.idFieldSchema)
        && Objects.equals(idRecord, other.idRecord) && Objects.equals(required, other.required)
        && Objects.equals(isPK, other.isPK) && Objects.equals(description, other.description)
        && Objects.equals(pkMustBeUsed, other.pkMustBeUsed)
        && Objects.equals(pkHasMultipleValues, other.pkHasMultipleValues);
  }


}
