package org.eea.kafka.utils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ConsumerGroupDescription;
import org.eea.kafka.domain.ConsumerGroupVO;
import org.eea.kafka.domain.MemberDescriptionVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * The type Kafka admin utils.
 */
@Component
public class KafkaAdminUtils {

  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");
  @Autowired
  private AdminClient adminClient;

  @Value(value = "${spring.application.name}")
  private String groupId;


  /**
   * Get consumer group info where the invoker is involved in.
   *
   * @return the consumer group description
   */
  public ConsumerGroupVO getConsumerGroupInfo() {
    return getConsumerGroupInfo(groupId);
  }

  /**
   * Gets consumer group info of the given groupId
   *
   * @param groupId the group id
   *
   * @return the consumer group info
   */
  public ConsumerGroupVO getConsumerGroupInfo(final String groupId) {
    ConsumerGroupVO groupDescription = null;
    try {
      Map<String, ConsumerGroupDescription> groups = adminClient.
          describeConsumerGroups(Arrays.asList(groupId)).all().get();
      groupDescription = mapConsumerGroupDescription(groups.get(groupId));
    } catch (InterruptedException | ExecutionException e) {
      LOG_ERROR
          .error("Error getting information for the Consumer Group {} from Kafka ", groupId, e);
    }
    return groupDescription;
  }

  private ConsumerGroupVO mapConsumerGroupDescription(
      final ConsumerGroupDescription consumerGroupDescription) {

    ConsumerGroupVO consumerGroupVO = new ConsumerGroupVO();
    consumerGroupVO.setGroupId(consumerGroupDescription.groupId());
    consumerGroupVO.setState(consumerGroupDescription.state().toString());
    consumerGroupVO.setMembers(consumerGroupDescription.members().stream()
        .map(member -> {
          MemberDescriptionVO vo = new MemberDescriptionVO();
          vo.setClientId(member.clientId());
          vo.setHost(member.host());
          vo.setMemberId(member.consumerId());
          return vo;
        }).collect(
            Collectors.toList()));
    return consumerGroupVO;
  }
}
