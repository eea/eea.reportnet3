package org.eea.validation.model;

import java.util.List;
import java.util.Objects;
import javax.persistence.Id;
import org.bson.types.ObjectId;
import org.eea.interfaces.vo.dataset.enums.TypeData;
import org.eea.validation.model.rules.FieldRule;
import org.springframework.data.mongodb.core.mapping.Field;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter
@ToString
public class FieldSchema {

  /** The id field schema. */
  @Id
  @Field(value = "_id")
  private ObjectId idFieldSchema;

  /** The type. */
  @Field(value = "typeData")
  private TypeData type;

  /** The type. */
  @Field(value = "headerName")
  private String headerName;

  /** The field rule list. */
  @Field(value = "rules")
  private List<FieldRule> fieldRuleList;

  /**
   * 
   *
   * @return
   */
  @Override
  public int hashCode() {
    return Objects.hash(fieldRuleList, headerName, idFieldSchema, type);
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
    return Objects.equals(fieldRuleList, other.fieldRuleList)
        && Objects.equals(headerName, other.headerName)
        && Objects.equals(idFieldSchema, other.idFieldSchema) && type == other.type;
  }


}
