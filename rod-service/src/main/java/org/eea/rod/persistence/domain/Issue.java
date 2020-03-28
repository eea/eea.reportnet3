package org.eea.rod.persistence.domain;

import com.google.common.base.Objects;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Issue {

  private Integer issueId;
  private String issueName;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Issue issue = (Issue) o;
    return Objects.equal(issueId, issue.issueId) &&
        Objects.equal(issueName, issue.issueName);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(issueId, issueName);
  }
}
