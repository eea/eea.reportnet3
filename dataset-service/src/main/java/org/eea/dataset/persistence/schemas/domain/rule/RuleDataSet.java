package org.eea.dataset.persistence.schemas.domain.rule;

import java.util.Objects;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Field;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The Class RuleDataSet.
 */
@Getter
@Setter
@ToString
public class RuleDataSet extends Rule {

  /** The id data set schema. */
  @Field(value = "idDataSetSchema")
  private ObjectId idDataSetSchema;

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
    result = PRIME * result + Objects.hash(idDataSetSchema);
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

    RuleDataSet other = (RuleDataSet) obj;
    return Objects.equals(idDataSetSchema, other.idDataSetSchema);
  }
}
