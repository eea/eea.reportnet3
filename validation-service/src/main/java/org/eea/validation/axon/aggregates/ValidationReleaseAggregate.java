package org.eea.validation.axon.aggregates;

import org.apache.commons.collections.CollectionUtils;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.messaging.MetaData;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.spring.stereotype.Aggregate;
import org.bson.types.ObjectId;
import org.eea.axon.release.commands.CreateValidationProcessForReleaseCommand;
import org.eea.axon.release.commands.CreateValidationTasksForReleaseCommand;
import org.eea.axon.release.events.ValidationProcessForReleaseCreatedEvent;
import org.eea.axon.release.events.ValidationProcessForReleaseFailedEvent;
import org.eea.axon.release.events.ValidationTasksForReleaseCreatedEvent;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController.DataSetMetabaseControllerZuul;
import org.eea.interfaces.controller.recordstore.ProcessController.ProcessControllerZuul;
import org.eea.interfaces.controller.validation.ValidationController;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.interfaces.vo.recordstore.enums.ProcessStatusEnum;
import org.eea.interfaces.vo.recordstore.enums.ProcessTypeEnum;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.eea.multitenancy.TenantResolver;
import org.eea.security.jwt.utils.EeaUserDetails;
import org.eea.utils.LiteralConstants;
import org.eea.validation.persistence.repository.RulesRepository;
import org.eea.validation.persistence.schemas.rule.Rule;
import org.eea.validation.util.ValidationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static org.axonframework.modelling.command.AggregateLifecycle.apply;

@Aggregate
public class ValidationReleaseAggregate {

    @AggregateIdentifier
    private String validationReleaseAggregateId;
    private String datasetReleaseAggregateId;
    private String releaseAggregateId;
    private String communicationReleaseAggregateId;
    private String dataflowReleaseAggregateId;
    private String collaborationReleaseAggregateId;
    private String recordStoreReleaseAggregateId;
    private String transactionId;
    private Long dataProviderId;
    private Long dataflowId;
    private boolean restrictFromPublic;
    private boolean validate;
    private List<Long> datasetIds;
    private Map<Long, String> datasetProcessId;

    private static final Logger LOG = LoggerFactory.getLogger(ValidationReleaseAggregate.class);

    public ValidationReleaseAggregate() {
    }

    @CommandHandler
    public ValidationReleaseAggregate(CreateValidationProcessForReleaseCommand command, MetaData metaData, ValidationController.ValidationControllerZuul validationControllerZuul, ProcessControllerZuul processControllerZuul) {
        try {
            LinkedHashMap auth = (LinkedHashMap) metaData.get("auth");
            List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
            List<LinkedHashMap<String,String>> authorities = (List<LinkedHashMap<String, String>>) auth.get("authorities");
            authorities.forEach((k -> k.values().forEach(grantedAuthority -> grantedAuthorities.add(new SimpleGrantedAuthority(grantedAuthority)))));
            SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                    EeaUserDetails.create(auth.get("name").toString(), new HashSet<>()), auth.get("credentials"), grantedAuthorities));

            Map<Long, String> datasetProcessId = new HashMap<>();
            command.getDatasetIds().forEach(datasetId -> {
                int priority = validationControllerZuul.getPriority(command.getDataflowId());
                LOG.info("Adding validation process for dataflowId: {} dataProvider: {} dataset ", command.getDataflowId(), command.getDataProviderId(), datasetId);
                String processId = UUID.randomUUID().toString();
                datasetProcessId.put(datasetId, processId);
                processControllerZuul.updateProcess(datasetId, command.getDataflowId(),
                        ProcessStatusEnum.IN_QUEUE, ProcessTypeEnum.VALIDATION, processId,
                        SecurityContextHolder.getContext().getAuthentication().getName(), priority, true);
                processControllerZuul.insertSagaTransactionIdAndAggregateId(command.getTransactionId(), command.getValidationReleaseAggregateId(), processId);
            });
            ValidationProcessForReleaseCreatedEvent event = new ValidationProcessForReleaseCreatedEvent();
            BeanUtils.copyProperties(command, event);
            event.setDatasetProcessId(datasetProcessId);
            apply(event, metaData);
        } catch (Exception e) {
            LOG.error("Error while adding validation process for dataflowId: {} dataProvider: {}: {}", command.getDataflowId(), command.getDataProviderId(), e.getMessage());
            throw e;
        }
    }

    @EventSourcingHandler
    public void on(ValidationProcessForReleaseCreatedEvent event) {
        this.recordStoreReleaseAggregateId = event.getRecordStoreReleaseAggregateId();
        this.collaborationReleaseAggregateId = event.getCollaborationReleaseAggregateId();
        this.communicationReleaseAggregateId = event.getCommunicationReleaseAggregateId();
        this.dataflowReleaseAggregateId = event.getDataflowReleaseAggregateId();
        this.recordStoreReleaseAggregateId = event.getRecordStoreReleaseAggregateId();
        this.datasetReleaseAggregateId = event.getDatasetReleaseAggregateId();
        this.validationReleaseAggregateId = event.getValidationReleaseAggregateId();
        this.transactionId = event.getTransactionId();
        this.dataflowId = event.getDataflowId();
        this.dataProviderId = event.getDataProviderId();
        this.restrictFromPublic = event.isRestrictFromPublic();
        this.validate = event.isValidate();
        this.datasetIds = event.getDatasetIds();
        this.datasetProcessId = event.getDatasetProcessId();
    }

    @CommandHandler
    public void handle(CreateValidationTasksForReleaseCommand command, MetaData metaData, DataSetMetabaseControllerZuul datasetMetabaseControllerZuul, RulesRepository rulesRepository,
                                      ProcessControllerZuul processControllerZuul, @Autowired ValidationHelper validationHelper, @Autowired KafkaSenderUtils kafkaSenderUtils) throws EEAException {
        try {
            AtomicReference<Boolean> validation = new AtomicReference<>(true);
            LinkedHashMap auth = (LinkedHashMap) metaData.get("auth");
            List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
            List<LinkedHashMap<String,String>> authorities = (List<LinkedHashMap<String, String>>) auth.get("authorities");
            authorities.forEach((k -> k.values().forEach(grantedAuthority -> grantedAuthorities.add(new SimpleGrantedAuthority(grantedAuthority)))));
            SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                    EeaUserDetails.create(auth.get("name").toString(), new HashSet<>()), auth.get("credentials"), grantedAuthorities));

            Long datasetId = command.getDatasetIds().get(0);
            DataSetMetabaseVO dataset = datasetMetabaseControllerZuul.findDatasetMetabaseById(datasetId);
            List<Rule> listSql = rulesRepository.findSqlRulesEnabled(new ObjectId(dataset.getDatasetSchema()));
            Boolean hasSqlEnabled = true;
            if (CollectionUtils.isEmpty(listSql)) {
                hasSqlEnabled = false;
            }

            validationHelper.addLockToReleaseProcess(datasetId);
            if (processControllerZuul.updateProcess(datasetId, dataset.getDataflowId(),
                    ProcessStatusEnum.IN_PROGRESS, ProcessTypeEnum.VALIDATION, command.getDatasetProcessId().get(datasetId),
                    SecurityContextHolder.getContext().getAuthentication().getName(), 0, true)) {

                // If there's no SQL rules enabled, no need to refresh the views, so directly start the
                // validation
                TenantResolver.setTenantName(LiteralConstants.DATASET_PREFIX + dataset.getId());

                if (Boolean.FALSE.equals(hasSqlEnabled)) {
                    validationHelper.executeValidationProcess(dataset, command.getDatasetProcessId().get(datasetId));
                } else {
                    validationHelper.deleteLockToReleaseProcess(datasetId);
                    validation.set(false);
                    Map<String, Object> values = new HashMap<>();
                    values.put(LiteralConstants.DATASET_ID, datasetId);
                    values.put("released", true);
                    values.put("referencesToRefresh",
                            List.copyOf(validationHelper.updateMaterializedViewsOfReferenceDatasetsInSQL(datasetId,
                                    dataset.getDataflowId(), dataset.getDatasetSchema())));
                    values.put("processId", command.getDatasetProcessId().get(datasetId));
                    kafkaSenderUtils.releaseKafkaEvent(EventType.REFRESH_MATERIALIZED_VIEW_EVENT, values);
                }
            }

            if (validation.get()) {
                ValidationTasksForReleaseCreatedEvent event = new ValidationTasksForReleaseCreatedEvent();
                BeanUtils.copyProperties(command, event);
                apply(event, metaData);
            } else {
                ValidationProcessForReleaseFailedEvent event = new ValidationProcessForReleaseFailedEvent();
                BeanUtils.copyProperties(command, event);
                apply(event);
            }
        } catch (Exception e) {
            LOG.error("Error while adding validation process for dataflowId: {} dataProvider: {}: {}", command.getDataflowId(), command.getDataProviderId(), e.getMessage());
            throw e;
        }
    }

    @EventSourcingHandler
    public void on(ValidationTasksForReleaseCreatedEvent event) {
        this.validationReleaseAggregateId = event.getValidationReleaseAggregateId();
        this.recordStoreReleaseAggregateId = event.getRecordStoreReleaseAggregateId();
        this.collaborationReleaseAggregateId = event.getCollaborationReleaseAggregateId();
        this.communicationReleaseAggregateId = event.getCommunicationReleaseAggregateId();
        this.dataflowReleaseAggregateId = event.getDataflowReleaseAggregateId();
        this.recordStoreReleaseAggregateId = event.getRecordStoreReleaseAggregateId();
        this.datasetReleaseAggregateId = event.getDatasetReleaseAggregateId();
        this.releaseAggregateId = event.getReleaseAggregateId();
        this.transactionId = event.getTransactionId();
        this.dataflowId = event.getDataflowId();
        this.dataProviderId = event.getDataProviderId();
        this.restrictFromPublic = event.isRestrictFromPublic();
        this.validate = event.isValidate();
        this.datasetIds = event.getDatasetIds();

    }

}
