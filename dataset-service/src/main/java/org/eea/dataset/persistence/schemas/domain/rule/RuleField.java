package org.eea.dataset.persistence.schemas.domain.rule;

import java.util.Objects;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Field;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The Class RuleField.
 */
@Getter
@Setter
@ToString
public class RuleField extends Rule {

  /** The rule name. */
  @Field(value = "idFieldSchema")
  private ObjectId idFieldSchema;

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
    result = PRIME * result + Objects.hash(idFieldSchema);
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

    RuleField other = (RuleField) obj;
    return Objects.equals(idFieldSchema, other.idFieldSchema);
  }

}
