package org.eea.validation.axon.aggregates;

import org.apache.commons.collections.CollectionUtils;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.messaging.MetaData;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.spring.stereotype.Aggregate;
import org.bson.types.ObjectId;
import org.eea.axon.release.commands.*;
import org.eea.axon.release.events.*;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController.DataSetMetabaseControllerZuul;
import org.eea.interfaces.controller.orchestrator.JobProcessController.JobProcessControllerZuul;
import org.eea.interfaces.controller.recordstore.ProcessController.ProcessControllerZuul;
import org.eea.interfaces.controller.recordstore.RecordStoreController.RecordStoreControllerZuul;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.interfaces.vo.dataset.enums.DatasetRunningStatusEnum;
import org.eea.interfaces.vo.orchestrator.JobProcessVO;
import org.eea.interfaces.vo.recordstore.ProcessVO;
import org.eea.interfaces.vo.recordstore.enums.ProcessStatusEnum;
import org.eea.interfaces.vo.recordstore.enums.ProcessTypeEnum;
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
    private Map<Long, String> datasetValidationProcessId;
    private Long datasetIForMaterializedViewEvent;
    private List<Long> referencesToRefresh;

    private static final Logger LOG = LoggerFactory.getLogger(ValidationReleaseAggregate.class);

    public ValidationReleaseAggregate() {
    }

    @CommandHandler
    public ValidationReleaseAggregate(CreateValidationProcessForReleaseCommand command, MetaData metaData, @Autowired ValidationHelper validationHelper, ProcessControllerZuul processControllerZuul,
                                      JobProcessControllerZuul jobProcessControllerZuul) {
        try {
            LinkedHashMap auth = (LinkedHashMap) metaData.get("auth");
            setAuthorities(auth);

            Map<Long, String> datasetProcessId = new HashMap<>();
            int priority = validationHelper.getPriority(command.getDataflowId());
            command.getDatasetIds().forEach(datasetId -> {
                LOG.info("Adding validation process for dataflowId {}, dataProvider {}, dataset {}, jobId {}", command.getDataflowId(), command.getDataProviderId(), datasetId, command.getJobId());
                String processId = UUID.randomUUID().toString();
                datasetProcessId.put(datasetId, processId);
                processControllerZuul.updateProcess(datasetId, command.getDataflowId(),
                        ProcessStatusEnum.IN_QUEUE, ProcessTypeEnum.VALIDATION, processId,
                        SecurityContextHolder.getContext().getAuthentication().getName(), priority, true);

                if (command.getJobId()!=null) {
                    JobProcessVO jobProcessVO = new JobProcessVO(null, command.getJobId(), processId, datasetId, command.getTransactionId(), command.getValidationReleaseAggregateId());
                    jobProcessControllerZuul.save(jobProcessVO);
                }
            });
            ValidationProcessForReleaseCreatedEvent event = new ValidationProcessForReleaseCreatedEvent();
            BeanUtils.copyProperties(command, event);
            event.setDatasetValidationProcessId(datasetProcessId);
            apply(event, metaData);
        } catch (Exception e) {
            LOG.error("Error while adding validation process for dataflowId {}, dataProvider {}, jobId {}, {}", command.getDataflowId(), command.getDataProviderId(), command.getJobId(), e.getMessage());
            throw e;
        }
    }

    @EventSourcingHandler
    public void on(ValidationProcessForReleaseCreatedEvent event) {
        this.validationReleaseAggregateId = event.getValidationReleaseAggregateId();
        this.datasetValidationProcessId = event.getDatasetValidationProcessId();
    }

    @CommandHandler
    public void handle(CreateValidationTasksForReleaseCommand command, MetaData metaData, DataSetMetabaseControllerZuul datasetMetabaseControllerZuul, RulesRepository rulesRepository,
                       ProcessControllerZuul processControllerZuul, @Autowired ValidationHelper validationHelper, @Autowired KafkaSenderUtils kafkaSenderUtils) throws EEAException {
        try {
            LinkedHashMap auth = (LinkedHashMap) metaData.get("auth");
            setAuthorities(auth);

            Long datasetId;
            if (command.getDatasetIForMaterializedViewEvent()!=null) {
                datasetId = command.getDatasetIForMaterializedViewEvent();
            } else {
                datasetId = command.getDatasetIds().get(0);
            }
            DataSetMetabaseVO dataset = datasetMetabaseControllerZuul.findDatasetMetabaseById(datasetId);
            List<Rule> listSql = rulesRepository.findSqlRulesEnabled(new ObjectId(dataset.getDatasetSchema()));
            Boolean hasSqlEnabled = true;
            if (CollectionUtils.isEmpty(listSql)) {
                hasSqlEnabled = false;
            }

            validationHelper.addLockToReleaseProcess(datasetId);
            if (processControllerZuul.updateProcess(datasetId, dataset.getDataflowId(),
                    ProcessStatusEnum.IN_PROGRESS, ProcessTypeEnum.VALIDATION, command.getDatasetValidationProcessId().get(datasetId),
                    SecurityContextHolder.getContext().getAuthentication().getName(), 0, true)) {

                // If there's no SQL rules enabled, no need to refresh the views, so directly start the
                // validation
                TenantResolver.setTenantName(LiteralConstants.DATASET_PREFIX + dataset.getId());
                if (Boolean.FALSE.equals(hasSqlEnabled) || command.getDatasetIForMaterializedViewEvent()!=null) {
                    validationHelper.executeValidationProcess(dataset, command.getDatasetValidationProcessId().get(datasetId));

                    ValidationTasksForReleaseCreatedEvent event = new ValidationTasksForReleaseCreatedEvent();
                    BeanUtils.copyProperties(command, event);
                    apply(event, metaData);
                } else {
                    validationHelper.deleteLockToReleaseProcess(datasetId);
                    refreshOrUpdateMaterializedView(command, metaData, validationHelper, datasetId, dataset);
                }
            }
        } catch (Exception e) {
            LOG.error("Error while adding validation tasks for dataflowId {}, dataProvider {}, jobId {}, {}", command.getDataflowId(), command.getDataProviderId(), command.getJobId(), e.getMessage());
            throw e;
        }
    }

    @EventSourcingHandler
    public void on(MaterializedViewShouldBeRefreshedEvent event) {
        this.datasetIForMaterializedViewEvent = event.getDatasetIForMaterializedViewEvent();
        this.referencesToRefresh = event.getReferencesToRefresh();
    }

    @CommandHandler
    public void handle(RefreshMaterializedViewForReferenceDatasetCommand command, MetaData metaData, RecordStoreControllerZuul recordStoreControllerZuul) {
        LinkedHashMap auth = (LinkedHashMap) metaData.get("auth");
        setAuthorities(auth);

        recordStoreControllerZuul.refreshMaterializedViewV2(command.getReferencesToRefresh());

        MaterializedViewForReferenceDatasetRefreshedEvent event = new MaterializedViewForReferenceDatasetRefreshedEvent();
        BeanUtils.copyProperties(command, event);
        apply(event, metaData);
    }

    private void setAuthorities(LinkedHashMap auth) {
        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        List<LinkedHashMap<String, String>> authorities = (List<LinkedHashMap<String, String>>) auth.get("authorities");
        authorities.forEach((k -> k.values().forEach(grantedAuthority -> grantedAuthorities.add(new SimpleGrantedAuthority(grantedAuthority)))));
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                EeaUserDetails.create(auth.get("name").toString(), new HashSet<>()), auth.get("credentials"), grantedAuthorities));
    }

    @CommandHandler
    public void handle(UpdateMaterializedViewCommand command, MetaData metaData, RecordStoreControllerZuul recordStoreControllerZuul, ProcessControllerZuul processControllerZuul, DataSetMetabaseControllerZuul datasetMetabaseControllerZuul) {
        LinkedHashMap auth = (LinkedHashMap) metaData.get("auth");
        setAuthorities(auth);

        if (command.getReferencesToRefresh() != null && !org.springframework.util.CollectionUtils.isEmpty(command.getReferencesToRefresh())) {
            command.getReferencesToRefresh().stream().forEach(dataset -> {
                try {
                    recordStoreControllerZuul.launchUpdateMaterializedQueryView(Long.valueOf(dataset));
                } catch (Exception e) {
                    LOG.error("Error refreshing the materialized view of the dataset {}", dataset, e);
                    processControllerZuul.updateProcess(command.getDatasetIForMaterializedViewEvent(), -1L, ProcessStatusEnum.CANCELED,
                            ProcessTypeEnum.VALIDATION, command.getDatasetValidationProcessId().get(command.getDatasetIForMaterializedViewEvent()),
                            SecurityContextHolder.getContext().getAuthentication().getName(), 0, null);
                    datasetMetabaseControllerZuul.updateDatasetRunningStatus(command.getDatasetIForMaterializedViewEvent(),
                            DatasetRunningStatusEnum.ERROR_IN_VALIDATION);
                }
            });
        }
        LOG.info("Updating materialized view for dataflowId {}, dataProviderId {}, datasetId {}, jobId {}", command.getDataflowId(), command.getDataProviderId(), command.getDatasetIForMaterializedViewEvent(), command.getJobId());
        recordStoreControllerZuul.updateMaterializedView(command.getDatasetIForMaterializedViewEvent());

        MaterializedViewUpdatedEvent event = new MaterializedViewUpdatedEvent();
        BeanUtils.copyProperties(command, event);
        apply(event, metaData);
    }

    @CommandHandler
    public void handle(CancelValidationProcessForReleaseCommand command, MetaData metaData, ProcessControllerZuul processControllerZuul, @Autowired ValidationHelper validationHelper) {
        LinkedHashMap auth = (LinkedHashMap) metaData.get("auth");
        setAuthorities(auth);

        List<String> statuses = new ArrayList<>();
        statuses.add(ProcessStatusEnum.IN_QUEUE.toString());
        statuses.add(ProcessStatusEnum.IN_PROGRESS.toString());
        int priority = validationHelper.getPriority(command.getDataflowId());
        command.getDatasetIds().forEach(datasetId -> {
           List<ProcessVO> datasetProcesses =  processControllerZuul.getProcessByDataflowAndDatasetAndStatus(command.getDataflowId(), datasetId, statuses);
           datasetProcesses.forEach(process -> {
                if (!process.getStatus().equals(ProcessStatusEnum.FINISHED) && !process.getStatus().equals(ProcessStatusEnum.CANCELED)) {
                    LOG.info("Cancelling validation process for dataflowId {}, dataProviderId {}, datasetId {}, jobId {}, processId {}", command.getDataflowId(), command.getDataProviderId(), datasetId, command.getJobId(), process.getProcessId());
                    if (processControllerZuul.updateProcess(datasetId, command.getDataflowId(),
                            ProcessStatusEnum.CANCELED, ProcessTypeEnum.VALIDATION, process.getProcessId(),
                            SecurityContextHolder.getContext().getAuthentication().getName(), priority, true)) {
                        LOG.info("Canceled validation process for dataflowId {}, dataProviderId {}, datasetId {}, jobId {}, processId {}", command.getDataflowId(), command.getDataProviderId(), datasetId, command.getJobId(), process.getProcessId());
                    }
                }
           });
        });
        ValidationProcessForReleaseCanceledEvent event = new ValidationProcessForReleaseCanceledEvent();
        BeanUtils.copyProperties(command, event);
        apply(event, metaData);
    }

    @CommandHandler
    public void handle(CancelValidationTasksForReleaseCommand command, MetaData metaData, @Autowired ValidationHelper validationHelper) {
        LinkedHashMap auth = (LinkedHashMap) metaData.get("auth");
        setAuthorities(auth);

        command.getDatasetValidationProcessId().values().forEach(processId -> {
            List<Long> taskIds = validationHelper.getTaskIdsByProcessId(processId);
            taskIds.forEach(taskId -> {
                LOG.info("Cancelling validation task for dataflowId {}, dataProviderId {}, jobId {}, processId {}", command.getDataflowId(), command.getDataProviderId(), command.getJobId(), processId);
                validationHelper.cancelTask(taskId, new Date());
                LOG.info("Cancelled validation task for dataflowId {}, dataProviderId {}, jobId {}, processId {}", command.getDataflowId(), command.getDataProviderId(), command.getJobId(), processId);
            });
        });

        ValidationTasksForReleaseCanceledEvent event = new ValidationTasksForReleaseCanceledEvent();
        BeanUtils.copyProperties(command, event);
        apply(event, metaData);
    }

    private void refreshOrUpdateMaterializedView(CreateValidationTasksForReleaseCommand command, MetaData metaData, ValidationHelper validationHelper, Long datasetId, DataSetMetabaseVO dataset) {
        List<Long> referencesToRefresh = List.copyOf(validationHelper.updateMaterializedViewsOfReferenceDatasetsInSQL(datasetId,
                dataset.getDataflowId(), dataset.getDatasetSchema()));
        if (referencesToRefresh.size()>0) {
            MaterializedViewShouldBeRefreshedEvent event = new MaterializedViewShouldBeRefreshedEvent();
            BeanUtils.copyProperties(command, event);
            event.setDatasetIForMaterializedViewEvent(datasetId);
            event.setReferencesToRefresh(referencesToRefresh);
            apply(event, metaData);
        } else {
            MaterializedViewUpdatedEvent event = new MaterializedViewUpdatedEvent();
            BeanUtils.copyProperties(command, event);
            event.setDatasetIForMaterializedViewEvent(datasetId);
            event.setReferencesToRefresh(referencesToRefresh);
            apply(event, metaData);
        }
    }
}



















