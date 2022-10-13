package org.eea.communication.axon;

import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.messaging.MetaData;
import org.eea.axon.release.events.ReleaseStartNotificationCreatedEvent;
import org.eea.communication.service.NotificationService;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.communication.UserNotificationContentVO;
import org.eea.interfaces.vo.communication.UserNotificationVO;
import org.eea.security.jwt.utils.EeaUserDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.LinkedHashMap;

@Component
@ProcessingGroup("release-group")
public class CommunicationReleaseEventsHandler {

    private static final Logger LOG = LoggerFactory.getLogger(CommunicationReleaseEventsHandler.class);

    private NotificationService notificationService;

    @Autowired
    public CommunicationReleaseEventsHandler(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @EventHandler
    public void on(ReleaseStartNotificationCreatedEvent event, MetaData metaData) throws EEAException {
        LOG.info("Creating release notification for dataflowId " + event.getDataflowId() + " and dataProviderId " + event.getDataProviderId());
        LinkedHashMap auth = (LinkedHashMap) metaData.get("auth");
        UserNotificationContentVO userNotificationContentVO = new UserNotificationContentVO();
        userNotificationContentVO.setDataflowId(event.getDataflowId());
        userNotificationContentVO.setProviderId(event.getDataProviderId());
        UserNotificationVO userNotificationVO = new UserNotificationVO();
        userNotificationVO.setEventType("RELEASE_START_EVENT");
        userNotificationVO.setContent(userNotificationContentVO);
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                EeaUserDetails.create(auth.get("name").toString(), new HashSet<>()), auth.get("credentials"), null));
        notificationService.createUserNotification(userNotificationVO);
        LOG.info("Created release notification for dataflowId " + event.getDataflowId() + " and dataProviderId " + event.getDataProviderId());
    }
}
