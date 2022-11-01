package org.eea.collaboration.axon.aggregates;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.messaging.MetaData;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.spring.stereotype.Aggregate;
import org.eea.axon.release.commands.CreateMessageForSuccessfulReleaseCommand;
import org.eea.axon.release.events.MessageForSuccessfulReleaseCreatedEvent;
import org.eea.collaboration.service.CollaborationService;
import org.eea.exception.EEAForbiddenException;
import org.eea.exception.EEAIllegalArgumentException;
import org.eea.interfaces.vo.dataflow.MessageVO;
import org.eea.security.jwt.utils.EeaUserDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.*;

import static org.axonframework.modelling.command.AggregateLifecycle.apply;

@Aggregate
public class CollaborationReleaseAggregate {

    @AggregateIdentifier
    private String collaborationReleaseAggregateId;
    private String datasetReleaseAggregateId;
    private String releaseAggregateId;
    private String communicationReleaseAggregateId;
    private String dataflowReleaseAggregateId;
    private String validationReleaseAggregateId;
    private String recordStoreReleaseAggregateId;
    private String transactionId;
    private Long dataProviderId;
    private Long dataflowId;
    private boolean restrictFromPublic;
    private boolean validate;
    private List<Long> datasetIds;
    private Map<Long, Long> datasetSnapshots;
    private Map<Long, Long> datasetDataCollection;
    private Map<Long, Date> datasetDateRelease;
    private String dataflowName;
    private String datasetName;

    private static final Logger LOG = LoggerFactory.getLogger(CollaborationReleaseAggregate.class);

    public CollaborationReleaseAggregate() {
    }

    @CommandHandler
    public CollaborationReleaseAggregate(CreateMessageForSuccessfulReleaseCommand command, CollaborationService collaborationService, MetaData metaData) throws EEAForbiddenException, EEAIllegalArgumentException {
        try {
            LinkedHashMap auth = (LinkedHashMap) metaData.get("auth");
            List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
            List<LinkedHashMap<String,String>> authorities = (List<LinkedHashMap<String, String>>) auth.get("authorities");
            authorities.forEach((k -> k.values().forEach(grantedAuthority -> grantedAuthorities.add(new SimpleGrantedAuthority(grantedAuthority)))));
            SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                    EeaUserDetails.create(auth.get("name").toString(), new HashSet<>()), auth.get("credentials"), grantedAuthorities));

            MessageVO messageVO = new MessageVO();
            messageVO.setProviderId(command.getDataProviderId());
            messageVO.setContent(command.getDatasetName() + " released " + command.getDataflowName() + " successfully");
            messageVO.setAutomatic(true);
            collaborationService.createMessage(command.getDataflowId(), messageVO);
            LOG.info("Automatic feedback message created of dataflow {}. Message: {}", command.getDataflowId(), messageVO.getContent());

            MessageForSuccessfulReleaseCreatedEvent event = new MessageForSuccessfulReleaseCreatedEvent();
            BeanUtils.copyProperties(command, event);
            apply(event);
        } catch(Exception e) {
            LOG.error("Error while creating message for successful release of dataset {}, dataflowId {}, dataProviderId {}: {}", command.getDatasetName(), command.getDataflowId(), command.getDataProviderId(), e.getMessage());
            throw e;
        }
    }

    @EventSourcingHandler
    public void on(MessageForSuccessfulReleaseCreatedEvent event) {
        this.collaborationReleaseAggregateId = event.getCollaborationReleaseAggregateId();
        this.communicationReleaseAggregateId = event.getCommunicationReleaseAggregateId();
        this.dataflowReleaseAggregateId = event.getDataflowReleaseAggregateId();
        this.recordStoreReleaseAggregateId = event.getRecordStoreReleaseAggregateId();
        this.datasetReleaseAggregateId = event.getDatasetReleaseAggregateId();
        this.validationReleaseAggregateId = event.getValidationReleaseAggregateId();
        this.recordStoreReleaseAggregateId = event.getRecordStoreReleaseAggregateId();
        this.releaseAggregateId = event.getReleaseAggregateId();
        this.transactionId = event.getTransactionId();
        this.dataflowId = event.getDataflowId();
        this.dataProviderId = event.getDataProviderId();
        this.restrictFromPublic = event.isRestrictFromPublic();
        this.validate = event.isValidate();
        this.datasetIds = event.getDatasetIds();
        this.datasetSnapshots = event.getDatasetSnapshots();
        this.datasetDataCollection = event.getDatasetDataCollection();
        this.datasetDateRelease = event.getDatasetDateRelease();
        this.dataflowName = event.getDataflowName();
        this.datasetName = event.getDatasetName();
    }
}












