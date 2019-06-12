package org.eea.validation.persistence.schemas.rule;

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

  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + Objects.hash(idDataSetSchema);
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
