package org.eea.dataflow.persistence.domain;

import java.util.Objects;
import javax.persistence.Entity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The Class DataProviderCode.
 */
@Entity
@Getter
@Setter
@ToString
public class DataProviderCode {

  /** The data provider group id. */
  private Long dataProviderGroupId;

  /** The label. */
  private String label;

  /**
   * Equals.
   *
   * @param o the o
   * @return true, if successful
   */
  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final DataProviderCode dataProviderCode = (DataProviderCode) o;
    return dataProviderGroupId.equals(dataProviderCode.dataProviderGroupId);
  }

  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    return Objects.hash(dataProviderGroupId, label);
  }
}
