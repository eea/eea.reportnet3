package org.eea.communication.axon.aggregates;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.messaging.MetaData;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.spring.stereotype.Aggregate;
import org.eea.axon.release.commands.SendUserNotificationForReleaseStartedCommand;
import org.eea.axon.release.events.UserNotifationForReleaseSentEvent;
import org.eea.communication.service.NotificationService;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.communication.UserNotificationContentVO;
import org.eea.interfaces.vo.communication.UserNotificationVO;
import org.eea.security.jwt.utils.EeaUserDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;

import static org.axonframework.modelling.command.AggregateLifecycle.apply;


@Component
@Aggregate
public class CommunicationReleaseAggregate {

    @AggregateIdentifier
    private String commReleaseAggregate;
    private String transactionId;
    private Long dataProviderId;
    private Long dataflowId;
    private boolean restrictFromPublic;
    private boolean validate;
    private List<Long> datasetIds;

    private static final Logger LOG = LoggerFactory.getLogger(CommunicationReleaseAggregate.class);

    public CommunicationReleaseAggregate() {
    }

    @CommandHandler
    public CommunicationReleaseAggregate(SendUserNotificationForReleaseStartedCommand command, MetaData metaData, NotificationService notificationService) throws EEAException, InterruptedException {
        LOG.info("Creating release notification for dataflowId " + command.getDataflowId() + " and dataProviderId " + command.getDataProviderId());
        LinkedHashMap auth = (LinkedHashMap) metaData.get("auth");
        UserNotificationContentVO userNotificationContentVO = new UserNotificationContentVO();
        userNotificationContentVO.setDataflowId(command.getDataflowId());
        userNotificationContentVO.setProviderId(command.getDataProviderId());
        UserNotificationVO userNotificationVO = new UserNotificationVO();
        userNotificationVO.setEventType("RELEASE_START_command");
        userNotificationVO.setContent(userNotificationContentVO);
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                EeaUserDetails.create(auth.get("name").toString(), new HashSet<>()), auth.get("credentials"), null));
        notificationService.createUserNotification(userNotificationVO);
        LOG.info("Created release notification for dataflowId " + command.getDataflowId() + " and dataProviderId " + command.getDataProviderId());
        UserNotifationForReleaseSentEvent event = new UserNotifationForReleaseSentEvent();
        BeanUtils.copyProperties(command, event);
        apply(event, metaData);
    }

    @EventSourcingHandler
    public void on(UserNotifationForReleaseSentEvent event) {
        this.commReleaseAggregate = event.getCommReleaseAggregate();
        this.transactionId = event.getTransactionId();
        this.dataflowId = event.getDataflowId();
        this.dataProviderId = event.getDataProviderId();
        this.restrictFromPublic = event.isRestrictFromPublic();
        this.validate = event.isValidate();
    }

}
