package org.eea.rod.persistence.domain;

import com.google.common.base.Objects;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Issue implements Serializable {

  private static final long serialVersionUID = 8824744741414252365L;
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
