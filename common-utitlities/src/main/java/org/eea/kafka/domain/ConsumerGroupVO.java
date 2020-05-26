package org.eea.kafka.domain;

import com.google.common.base.Objects;
import java.util.Collection;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.kafka.clients.admin.MemberDescription;
import org.apache.kafka.common.ConsumerGroupState;

/**
 * The type Consumer group vo.
 */
@Getter
@Setter
@ToString
public class ConsumerGroupVO {

  private String groupId;
  private Collection<MemberDescriptionVO> members;
  private String state;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ConsumerGroupVO that = (ConsumerGroupVO) o;
    return Objects.equal(groupId, that.groupId);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(groupId, members, state);
  }
}
