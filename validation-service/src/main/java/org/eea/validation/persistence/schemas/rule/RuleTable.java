package org.eea.validation.persistence.schemas.rule;

import java.util.Objects;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Field;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The Class RuleTable.
 */
@Getter
@Setter
@ToString
public class RuleTable extends Rule {

  /** The id table schema. */
  @Field(value = "idTableSchema")
  private ObjectId idTableSchema;

  /** The Constant PRIME. */
  private static final int PRIME = 31;

  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = PRIME * result + Objects.hash(idTableSchema);
    return result;
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
    RuleTable other = (RuleTable) obj;
    return Objects.equals(idTableSchema, other.idTableSchema);
  }
}
