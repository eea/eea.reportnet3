package org.eea.kafka.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ConsumerGroupDescription;
import org.apache.kafka.clients.admin.DescribeConsumerGroupsResult;
import org.apache.kafka.clients.admin.MemberDescription;
import org.apache.kafka.common.ConsumerGroupState;
import org.apache.kafka.common.KafkaFuture;
import org.eea.kafka.domain.ConsumerGroupVO;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * The Class KafkaAdminUtilsTest.
 */
public class KafkaAdminUtilsTest {

  /** The kafka admin utils. */
  @InjectMocks
  private KafkaAdminUtils kafkaAdminUtils;

  /** The admin client. */
  @Mock
  private AdminClient adminClient;

  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {
    MockitoAnnotations.openMocks(this);
  }

  /**
   * Gets the consumer group info.
   *
   * @return the consumer group info
   * @throws ExecutionException the execution exception
   * @throws InterruptedException the interrupted exception
   */
  @Test
  public void getConsumerGroupInfo() throws ExecutionException, InterruptedException {
    String group = "group";
    ReflectionTestUtils.setField(kafkaAdminUtils, "groupId", group);

    DescribeConsumerGroupsResult groupInfo = Mockito.mock(DescribeConsumerGroupsResult.class);

    KafkaFuture<Map<String, ConsumerGroupDescription>> kafkaFuture =
        Mockito.mock(KafkaFuture.class);
    Map<String, ConsumerGroupDescription> groups = new HashMap<>();
    Collection<MemberDescription> members = new ArrayList<>();
    MemberDescription member = new MemberDescription("member1", "123", "localhost", null);
    members.add(member);
    ConsumerGroupDescription description =
        new ConsumerGroupDescription(group, false, members, "", ConsumerGroupState.STABLE, null);
    groups.put(group, description);
    Mockito.when(kafkaFuture.get()).thenReturn(groups);
    Mockito.when(groupInfo.all()).thenReturn(kafkaFuture);

    Mockito.doReturn(groupInfo).when(adminClient)
        .describeConsumerGroups(Mockito.eq(Arrays.asList(group)));
    ConsumerGroupVO result = kafkaAdminUtils.getConsumerGroupInfo();
    Assert.assertNotNull(result);
    Assert.assertNotNull(result.getMembers());
    Assert.assertEquals(group, result.getGroupId());
    Assert.assertEquals(1, result.getMembers().size());
  }

  /**
   * Gets the consumer group info with input group name.
   *
   * @return the consumer group info with input group name
   * @throws ExecutionException the execution exception
   * @throws InterruptedException the interrupted exception
   */
  @Test
  public void getConsumerGroupInfoWithInputGroupName()
      throws ExecutionException, InterruptedException {
    String group = "group";
    DescribeConsumerGroupsResult groupInfo = Mockito.mock(DescribeConsumerGroupsResult.class);

    KafkaFuture<Map<String, ConsumerGroupDescription>> kafkaFuture =
        Mockito.mock(KafkaFuture.class);
    Map<String, ConsumerGroupDescription> groups = new HashMap<>();
    Collection<MemberDescription> members = new ArrayList<>();
    MemberDescription member = new MemberDescription("member1", "123", "localhost", null);
    members.add(member);
    ConsumerGroupDescription description =
        new ConsumerGroupDescription(group, false, members, "", ConsumerGroupState.STABLE, null);
    groups.put(group, description);
    Mockito.when(kafkaFuture.get()).thenReturn(groups);
    Mockito.when(groupInfo.all()).thenReturn(kafkaFuture);

    Mockito.doReturn(groupInfo).when(adminClient)
        .describeConsumerGroups(Mockito.eq(Arrays.asList(group)));
    ConsumerGroupVO result = kafkaAdminUtils.getConsumerGroupInfo(group);
    Assert.assertNotNull(result);
    Assert.assertNotNull(result.getMembers());
    Assert.assertEquals(group, result.getGroupId());
    Assert.assertEquals(1, result.getMembers().size());
  }
}
