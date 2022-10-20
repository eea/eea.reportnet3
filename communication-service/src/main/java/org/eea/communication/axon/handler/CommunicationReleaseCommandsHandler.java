package org.eea.communication.axon.handler;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.messaging.MetaData;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.eea.axon.release.commands.SendUserNotificationCommand;
import org.eea.axon.release.events.UserNotificationCreatedEvent;
import org.eea.communication.service.NotificationService;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.communication.UserNotificationContentVO;
import org.eea.interfaces.vo.communication.UserNotificationVO;
import org.eea.security.jwt.utils.EeaUserDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.LinkedHashMap;

@Component
public class CommunicationReleaseCommandsHandler {

    private static final Logger LOG = LoggerFactory.getLogger(CommunicationReleaseCommandsHandler.class);

    @Autowired
    private NotificationService notificationService;

    @CommandHandler
    public void handle(SendUserNotificationCommand command, MetaData metaData) throws EEAException {
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
//        notificationService.createUserNotification(userNotificationVO);
        LOG.info("Created release notification for dataflowId " + command.getDataflowId() + " and dataProviderId " + command.getDataProviderId());
    }
}
