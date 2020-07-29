package org.eea.rod.persistence.domain;

import java.io.Serializable;
import com.google.common.base.Objects;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


/**
 * The Class Issue.
 */
@Getter
@Setter
@ToString
public class Issue implements Serializable {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 8824744741414252365L;

  /** The issue id. */
  private Integer issueId;

  /** The issue name. */
  private String issueName;

  /**
   * Equals.
   *
   * @param o the o
   * @return true, if successful
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Issue issue = (Issue) o;
    return Objects.equal(issueId, issue.issueId) && Objects.equal(issueName, issue.issueName);
  }

  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    return Objects.hashCode(issueId, issueName);
  }
}
