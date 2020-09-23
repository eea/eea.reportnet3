package org.eea.kafka.domain;

import com.google.common.base.Objects;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The type Member description vo.
 */
@Getter
@Setter
@ToString
public class MemberDescriptionVO {

  private String memberId;
  private String clientId;
  private String host;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    MemberDescriptionVO that = (MemberDescriptionVO) o;
    return Objects.equal(memberId, that.memberId);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(memberId, clientId, host);
  }
}
