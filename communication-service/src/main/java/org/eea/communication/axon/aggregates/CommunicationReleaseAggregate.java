package org.eea.communication.axon.aggregates;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.messaging.MetaData;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.spring.stereotype.Aggregate;
import org.eea.axon.release.commands.SendEmailForSuccessfulReleaseCommand;
import org.eea.axon.release.commands.SendUserNotificationForReleaseStartedCommand;
import org.eea.axon.release.events.EmailForSuccessfulReleaseSentEvent;
import org.eea.axon.release.events.UserNotifationForReleaseSentEvent;
import org.eea.communication.service.EmailService;
import org.eea.communication.service.NotificationService;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.DataFlowController.DataFlowControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController.DataSetMetabaseControllerZuul;
import org.eea.interfaces.controller.ums.UserManagementController.UserManagementControllerZull;
import org.eea.interfaces.vo.communication.EmailVO;
import org.eea.interfaces.vo.communication.UserNotificationContentVO;
import org.eea.interfaces.vo.communication.UserNotificationVO;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.interfaces.vo.ums.UserRepresentationVO;
import org.eea.interfaces.vo.ums.enums.ResourceGroupEnum;
import org.eea.security.jwt.utils.EeaUserDetails;
import org.eea.utils.LiteralConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;

import static org.axonframework.modelling.command.AggregateLifecycle.apply;


@Component
@Aggregate
public class CommunicationReleaseAggregate {

    @AggregateIdentifier
    private String communicationReleaseAggregateId;
    private String dataflowName;
    private String datasetName;

    private static final Logger LOG = LoggerFactory.getLogger(CommunicationReleaseAggregate.class);

    public CommunicationReleaseAggregate() {
    }

    @CommandHandler
    public CommunicationReleaseAggregate(SendUserNotificationForReleaseStartedCommand command, MetaData metaData, NotificationService notificationService,
                                         DataFlowControllerZuul dataflowControllerZull) throws EEAException {
        try {
            LinkedHashMap auth = (LinkedHashMap) metaData.get("auth");
            setAuthorities(auth);

            DataFlowVO dataflow = dataflowControllerZull.getMetabaseById(command.getDataflowId());
            if (dataflow!=null && dataflow.isReleasable()) {
                LOG.info("Creating release notification for dataflowId {}, dataProviderId {}, jobId {}", command.getDataflowId(), command.getDataProviderId(), command.getJobId());
                UserNotificationContentVO userNotificationContentVO = new UserNotificationContentVO();
                userNotificationContentVO.setDataflowId(command.getDataflowId());
                userNotificationContentVO.setProviderId(command.getDataProviderId());
                userNotificationContentVO.setUserId(command.getUser());
                UserNotificationVO userNotificationVO = new UserNotificationVO();
                userNotificationVO.setEventType("RELEASE_START_EVENT");
                userNotificationVO.setContent(userNotificationContentVO);
                notificationService.createUserNotification(userNotificationVO);
                LOG.info("Created release notification for dataflowId {}, dataProviderId {}, jobId {}", command.getDataflowId(), command.getDataProviderId(), command.getJobId());
                UserNotifationForReleaseSentEvent event = new UserNotifationForReleaseSentEvent();
                BeanUtils.copyProperties(command, event);
                apply(event, metaData);
            }
        } catch (Exception e) {
            LOG.error("Error while creating release notification for dataflowId {}, dataProviderId {}, jobId {}: {}", command.getDataflowId(), command.getDataProviderId(), command.getJobId(), e.getMessage());
            throw e;
        }
    }

    @EventSourcingHandler
    public void on(UserNotifationForReleaseSentEvent event) {
        this.communicationReleaseAggregateId = event.getCommunicationReleaseAggregateId();
    }

    @CommandHandler
    public void handle(SendEmailForSuccessfulReleaseCommand command, DataSetMetabaseControllerZuul dataSetMetabaseControllerZuul, UserManagementControllerZull userManagementControllerZuul,
                                         DataFlowControllerZuul dataFlowControllerZuul, EmailService emailService, MetaData metaData) {
        DataSetMetabaseVO dataset = null;
        try {
            LinkedHashMap auth = (LinkedHashMap) metaData.get("auth");
            setAuthorities(auth);

            dataset = dataSetMetabaseControllerZuul.findDatasetMetabaseById(command.getDatasetIds().get(0));
            // get custodian and stewards emails
            List<UserRepresentationVO> custodians = userManagementControllerZuul
                    .getUsersByGroup(ResourceGroupEnum.DATAFLOW_CUSTODIAN.getGroupName(command.getDataflowId()));
            List<UserRepresentationVO> stewards = userManagementControllerZuul
                    .getUsersByGroup(ResourceGroupEnum.DATAFLOW_STEWARD.getGroupName(command.getDataflowId()));
            List<UserRepresentationVO> observers = userManagementControllerZuul
                    .getUsersByGroup(ResourceGroupEnum.DATAFLOW_OBSERVER.getGroupName(command.getDataflowId()));
            List<UserRepresentationVO> custodianSupport = userManagementControllerZuul.getUsersByGroup(
                    ResourceGroupEnum.DATAFLOW_STEWARD_SUPPORT.getGroupName(command.getDataflowId()));
            List<String> emails = new ArrayList<>();
            if (null != custodians) {
                custodians.stream().forEach(custodian -> emails.add(custodian.getEmail()));
            }
            if (null != stewards) {
                stewards.stream().forEach(steward -> emails.add(steward.getEmail()));
            }
            if (null != observers) {
                observers.stream().forEach(observer -> emails.add(observer.getEmail()));
            }
            if (null != custodianSupport) {
                custodianSupport.stream().forEach(support -> emails.add(support.getEmail()));
            }

            DataFlowVO dataflowVO = dataFlowControllerZuul.getMetabaseById(dataset.getDataflowId());

            EmailVO emailVO = new EmailVO();
            emailVO.setBbc(emails);
            emailVO.setSubject(String.format(LiteralConstants.RELEASESUBJECT, dataset.getDataSetName(), dataflowVO.getName()));
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String dateRelease = dateFormat.format(command.getDatasetDateRelease().values().stream().findFirst().get());
            emailVO.setText(String.format(LiteralConstants.RELEASEMESSAGE, dataset.getDataSetName(), dataflowVO.getName(), dateRelease));
            emailService.sendMessage(emailVO);
            LOG.info("Email for successful release sent for dataset {}, dataProviderId {}, dataflowId {}, jobId {}", dataset.getDataSetName(), command.getDataProviderId(), command.getDataflowId(), command.getJobId());

            EmailForSuccessfulReleaseSentEvent event = new EmailForSuccessfulReleaseSentEvent();
            BeanUtils.copyProperties(command, event);
            event.setDataflowName(dataflowVO.getName());
            event.setDatasetName(dataset.getDataSetName());
            apply(event, metaData);
        } catch (Exception e) {
            LOG.error("Error while sending email for successful release for datasetd {}, dataProviderId {}, dataflowId {}, jobId {}, {}", dataset.getDataSetName(), command.getDataProviderId(), command.getDataflowId(), command.getJobId(), e.getMessage());
            throw e;
        }
    }

    private void setAuthorities(LinkedHashMap auth) {
        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        List<LinkedHashMap<String, String>> authorities = (List<LinkedHashMap<String, String>>) auth.get("authorities");
        authorities.forEach((k -> k.values().forEach(grantedAuthority -> grantedAuthorities.add(new SimpleGrantedAuthority(grantedAuthority)))));
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                EeaUserDetails.create(auth.get("name").toString(), new HashSet<>()), auth.get("credentials"), grantedAuthorities));
    }

    @EventSourcingHandler
    public void on(EmailForSuccessfulReleaseSentEvent event) {
        this.dataflowName = event.getDataflowName();
        this.datasetName = event.getDatasetName();
    }
}
