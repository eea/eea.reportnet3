package org.eea.interfaces.vo.dataset;

import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The Class CreateSnapshotVO.
 */
@Getter
@Setter
@ToString
public class CreateSnapshotVO {

  /** The description. */
  private String description;

  /** The released. */
  private Boolean released;

  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    return Objects.hash(description, released);
  }

  /**
   * Equals.
   *
   * @param obj the obj
   * @return true, if successful
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    CreateSnapshotVO other = (CreateSnapshotVO) obj;
    return Objects.equals(description, other.description)
        && Objects.equals(released, other.released);
  }

}
