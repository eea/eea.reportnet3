/**
 *
 */
package org.eea.dataset.persistence.schemas.domain;

import java.util.List;
import java.util.Objects;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import org.bson.types.ObjectId;
import org.eea.dataset.persistence.schemas.domain.rule.RuleField;
import org.eea.interfaces.vo.dataset.enums.TypeData;
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
  private TypeData type;

  /** The type. */
  @Field(value = "headerName")
  private String headerName;

  /** The rule field. */
  @Field(value = "rules")
  private List<RuleField> ruleField;

  /** The name code list. */
  @Field(value = "nameCodeList")
  private String nameCodeList;

  /** The version code list. */
  @Field(value = "versionCodeList")
  private String versionCodeList;

  /** The id code list. */
  @Field(value = "idCodeList")
  private Long idCodeList;

  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    return Objects.hash(headerName, description, idFieldSchema, idRecord, ruleField, type,
        nameCodeList, versionCodeList, idCodeList);
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
        && Objects.equals(description, other.description)
        && Objects.equals(idFieldSchema, other.idFieldSchema)
        && Objects.equals(idRecord, other.idRecord) && Objects.equals(ruleField, other.ruleField)
        && Objects.equals(type, other.type) && Objects.equals(nameCodeList, other.nameCodeList)
        && Objects.equals(versionCodeList, other.versionCodeList)
        && Objects.equals(idCodeList, other.idCodeList);
  }
}
