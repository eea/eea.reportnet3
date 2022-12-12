package org.eea.dataset.axon.aggregates;

import io.jsonwebtoken.lang.Collections;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.messaging.MetaData;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.spring.stereotype.Aggregate;
import org.eea.axon.release.commands.*;
import org.eea.axon.release.events.*;
import org.eea.dataset.persistence.data.repository.DatasetExtendedRepository;
import org.eea.dataset.persistence.data.repository.ValidationRepository;
import org.eea.dataset.persistence.metabase.domain.*;
import org.eea.dataset.persistence.metabase.repository.*;
import org.eea.dataset.service.DatasetMetabaseService;
import org.eea.dataset.service.DatasetSnapshotService;
import org.eea.dataset.service.helper.FileTreatmentHelper;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.DataFlowController.DataFlowControllerZuul;
import org.eea.interfaces.controller.dataflow.RepresentativeController.RepresentativeControllerZuul;
import org.eea.interfaces.controller.orchestrator.JobController.JobControllerZuul;
import org.eea.interfaces.controller.orchestrator.JobHistoryController.JobHistoryControllerZuul;
import org.eea.interfaces.controller.orchestrator.JobProcessController.JobProcessControllerZuul;
import org.eea.interfaces.controller.recordstore.ProcessController.ProcessControllerZuul;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
import org.eea.interfaces.vo.dataflow.DataProviderVO;
import org.eea.interfaces.vo.dataset.CreateSnapshotVO;
import org.eea.interfaces.vo.dataset.enums.DatasetRunningStatusEnum;
import org.eea.interfaces.vo.dataset.enums.DatasetStatusEnum;
import org.eea.interfaces.vo.dataset.enums.ErrorTypeEnum;
import org.eea.interfaces.vo.orchestrator.JobProcessVO;
import org.eea.interfaces.vo.orchestrator.JobVO;
import org.eea.interfaces.vo.orchestrator.enums.JobStatusEnum;
import org.eea.interfaces.vo.orchestrator.enums.JobTypeEnum;
import org.eea.interfaces.vo.recordstore.enums.ProcessStatusEnum;
import org.eea.interfaces.vo.recordstore.enums.ProcessTypeEnum;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.domain.NotificationVO;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.eea.multitenancy.TenantResolver;
import org.eea.security.jwt.utils.EeaUserDetails;
import org.eea.utils.LiteralConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.axonframework.modelling.command.AggregateLifecycle.apply;

@Aggregate
public class DatasetReleaseAggregate {

    @AggregateIdentifier
    private String datasetReleaseAggregateId;
    private List<Long> datasetIds;
    private Map<Long, Long> datasetSnapshots;
    private Map<Long, Long> datasetDataCollection;
    private List<Long> dataCollectionForDeletion;
    private Map<Long, Date> datasetDateRelease;
    private Map<Long, String> datasetReleaseProcessId;

    /**
     * The Constant LOG.
     */
    private static final Logger LOG = LoggerFactory.getLogger(DatasetReleaseAggregate.class);

    /**
     * The default release process priority
     */
    private int defaultReleaseProcessPriority = 20;

    public DatasetReleaseAggregate() {
    }

    @CommandHandler
    public DatasetReleaseAggregate(AddReleaseLocksCommand command, MetaData metaData, DatasetSnapshotService datasetSnapshotService, ReportingDatasetRepository reportingDatasetRepository) throws EEAException {
        try {
            LOG.info("Adding release locks for DataflowId: {} DataProviderId: {}", command.getDataflowId(), command.getDataProviderId());
            LinkedHashMap auth = (LinkedHashMap) metaData.get("auth");
            setAuthorities(auth);

            // Lock all the operations related to the datasets involved
            List<Long> datasetIds = reportingDatasetRepository.findByDataflowId(command.getDataflowId()).stream().filter(rd -> rd.getDataProviderId().equals(command.getDataProviderId())).map(ReportingDataset::getId)
                    .distinct().collect(Collectors.toList());
            command.setDatasetIds(datasetIds);
            LOG.info("Adding release locks for dataflowId {}, dataProviderId {}, jobId {}", command.getDataflowId(), command.getDataProviderId(), command.getJobId());
            datasetSnapshotService.addLocksRelatedToRelease(datasetIds, command.getDataflowId());
            LOG.info("Release locks added for dataflowId {}, dataProviderId {}, jobId {}", command.getDataflowId(), command.getDataProviderId(), command.getJobId());
            ReleaseLocksAddedEvent event = new ReleaseLocksAddedEvent();
            BeanUtils.copyProperties(command, event);
            apply(event, metaData);
        } catch (Exception e) {
            LOG.error("Error while adding release locks for dataflowId {}, dataProviderId: {}: {}", command.getDataflowId(), command.getDataProviderId(), e.getMessage());
            throw e;
        }
    }

    @EventSourcingHandler
    public void on(ReleaseLocksAddedEvent event) {
        this.datasetReleaseAggregateId = event.getDatasetReleaseAggregateId();
        this.datasetIds = event.getDatasetIds();
    }

    @CommandHandler
    public void handle(CreateSnapshotRecordRorReleaseInMetabaseCommand command, DatasetSnapshotService datasetSnapshotService, ValidationRepository validationRepository, MetaData metaData,
                       @Autowired KafkaSenderUtils kafkaSenderUtils, JobControllerZuul jobControllerZuul, JobHistoryControllerZuul jobHistoryControllerZuul,
                       JobProcessControllerZuul jobProcessControllerZuul, ProcessControllerZuul processControllerZuul) throws EEAException {
        try {
            LinkedHashMap auth = (LinkedHashMap) metaData.get("auth");
            setAuthorities(auth);

            JobStatusEnum statusToInsert = jobControllerZuul.checkEligibilityOfJob(JobTypeEnum.VALIDATION.toString(), true, command.getDataflowId(), command.getDataProviderId(), command.getDatasetIds());
            if (statusToInsert == JobStatusEnum.REFUSED) {
                return;
            }
            JobVO releaseJob = createReleaseJob(command, jobControllerZuul, jobHistoryControllerZuul, statusToInsert);

            boolean haveBlockers = false;
            for (Long id : command.getDatasetIds()) {
                TenantResolver.setTenantName(String.format(LiteralConstants.DATASET_FORMAT_NAME, id));
                if (validationRepository.existsByLevelError(ErrorTypeEnum.BLOCKER)) {
                    haveBlockers = true;
                    cancelReleaseBecauseOfBlockers(command, kafkaSenderUtils, jobControllerZuul, releaseJob, id);
                    break;
                }
            }
            if (!haveBlockers) {
                CreateSnapshotVO createSnapshotVO = createSnapshotVO();
                datasetSnapshots = new HashMap<>();
                datasetReleaseProcessId = new HashMap<>();
                for (Long id : command.getDatasetIds()) {
                    String processId = createReleaseProcess(command, jobProcessControllerZuul, processControllerZuul, releaseJob, id);
                    LOG.info("Creating snapshot record for dataflowId {}, dataProvider {} dataset {}, jobId {}", command.getDataflowId(), command.getDataProviderId(), id, command.getJobId());
                    Snapshot snapshot = datasetSnapshotService.createSnapshotInMetabase(id, createSnapshotVO);
                    LOG.info("Snapshot record created in metabase for dataflowId {}, dataProvider {}, dataset {}, jobId {}", command.getDataflowId(), command.getDataProviderId(), id, command.getJobId());
                    datasetSnapshots.put(id, snapshot.getId());
                    datasetReleaseProcessId.put(id, processId);
                };
            }
            SnapshotRecordForReleaseCreatedInMetabaseEvent event = new SnapshotRecordForReleaseCreatedInMetabaseEvent();
            BeanUtils.copyProperties(command, event);
            event.setDatasetSnapshots(datasetSnapshots);
            event.setDatasetReleaseProcessId(datasetReleaseProcessId);
            event.setJobId(releaseJob.getId());
            apply(event, metaData);
        } catch (Exception e) {
            LOG.error("Error while Creating snapshot record for dataflowId {}, dataProvider {}, jobId {}, {}", command.getDataflowId(), command.getDataProviderId(), command.getJobId(), e.getMessage());
            throw e;
        }
    }

    @EventSourcingHandler
    public void on(SnapshotRecordForReleaseCreatedInMetabaseEvent event) {
        this.datasetSnapshots = event.getDatasetSnapshots();
        this.datasetReleaseProcessId = event.getDatasetReleaseProcessId();
    }

    @CommandHandler
    public void handle(UpdateDatasetStatusCommand command, DatasetSnapshotService datasetSnapshotService, MetaData metaData, DataSetMetabaseRepository metabaseRepository,
                       DataCollectionRepository dataCollectionRepository) {
        try {
            LinkedHashMap auth = (LinkedHashMap) metaData.get("auth");
            setAuthorities(auth);

            datasetDataCollection = new HashMap<>();
            dataCollectionForDeletion = new ArrayList<>();
            command.getDatasetIds().stream().forEach(id -> {
                LOG.info("Updating dataset status for dataflowId {}, dataProviderId {}, dataset {}, jobId {}", command.getDataflowId(), command.getDataProviderId(), id, command.getJobId());
                datasetSnapshotService.updateDatasetStatus(id);
                LOG.info("Status updated for dataflowId {}, dataProviderId {}, dataset {}, jobId {}", command.getDataflowId(), command.getDataProviderId(), id, command.getJobId());

                DataSetMetabase dataset = metabaseRepository.findById(id).orElse(null);
                Optional<DataCollection> dataCollection =
                        dataCollectionRepository.findFirstByDatasetSchema(dataset.getDatasetSchema());
                Long idDataCollection = dataCollection.isPresent() ? dataCollection.get().getId() : null;
                datasetDataCollection.put(id, idDataCollection);
                dataCollectionForDeletion.add(idDataCollection);
            });

            DatasetStatusUpdatedEvent event = new DatasetStatusUpdatedEvent();
            BeanUtils.copyProperties(command, event);
            event.setDatasetDataCollection(datasetDataCollection);
            event.setDataCollectionForDeletion(dataCollectionForDeletion);
            apply(event, metaData);
        } catch (Exception e) {
            LOG.error("Error while updating dataset status for dataflowId {}, dataProviderId {}, jobId {}", command.getDataflowId(), command.getDataProviderId(), command.getJobId());
            throw e;
        }
    }

    @EventSourcingHandler
    public void on(DatasetStatusUpdatedEvent event) {
        this.datasetDataCollection = event.getDatasetDataCollection();
        this.dataCollectionForDeletion = event.getDataCollectionForDeletion();
    }

    @CommandHandler
    public void handle(DeleteProviderCommand command, DatasetSnapshotService datasetSnapshotService, MetaData metaData) {
        try {
            LinkedHashMap auth = (LinkedHashMap) metaData.get("auth");
            setAuthorities(auth);

            Long dataCollectionId = command.getDataCollectionForDeletion().get(0);
            LOG.info("Deleting provider for dataflowId {}, dataProviderId {}, dataCollectionId {}, jobId {}", command.getDataflowId(), command.getDataProviderId(), dataCollectionId, command.getJobId());

            TenantResolver.setTenantName(String.format(LiteralConstants.DATASET_FORMAT_NAME, dataCollectionId));
            datasetSnapshotService.deleteProvider(dataCollectionId, command.getDataProviderId());
            LOG.info("Provider deleted for dataflowId {}, dataProviderId {} dataCollectionId {}", command.getDataflowId(), command.getDataProviderId(), dataCollectionId, command.getJobId());
            ProviderDeletedEvent event = new ProviderDeletedEvent();
            BeanUtils.copyProperties(command, event);
            event.getDataCollectionForDeletion().remove(dataCollectionId);
            apply(event, metaData);
        } catch (Exception e) {
            LOG.error("Error while deleting provider for dataflowId {}, dataProviderId {}, jobId {}, {}", command.getDataflowId(), command.getDataProviderId(), command.getJobId(), e.getMessage());
            throw e;
        }
    }

    @EventSourcingHandler
    public void on(ProviderDeletedEvent event) {
        this.dataCollectionForDeletion = event.getDataCollectionForDeletion();
    }

    @CommandHandler
    public void handle(UpdateDatasetRunningStatusCommand command, DataSetMetabaseRepository dataSetMetabaseRepository, MetaData metaData, @Qualifier("datasetExtendedRepositoryImpl") DatasetExtendedRepository datasetExtendedRepository) {
        try {
            command.getDatasetIds().stream().forEach(id -> {
                Long idDataCollection = command.getDatasetDataCollection().get(id);
                DataSetMetabase dataCollectionMetabase = dataSetMetabaseRepository.findById(idDataCollection).orElse(null);
                LOG.info("Updating dataset running status to {} for datasetId {}, dataflowId {}, dataProviderId {}, jobId {}", DatasetRunningStatusEnum.RESTORING_SNAPSHOT,
                        idDataCollection, command.getDataflowId(), command.getDataProviderId(), command.getJobId());
                if (dataCollectionMetabase != null) {
                    dataCollectionMetabase.setDatasetRunningStatus(DatasetRunningStatusEnum.RESTORING_SNAPSHOT);
                    dataSetMetabaseRepository.save(dataCollectionMetabase);
                    LOG.info("Updated dataset running status to {} for datasetId {}, dataflowId {}, dataProviderId {}, jobId {}", DatasetRunningStatusEnum.RESTORING_SNAPSHOT,
                            idDataCollection, command.getDataflowId(), command.getDataProviderId(), command.getJobId());
                }
            });

            DatasetRunningStatusUpdatedEvent event = new DatasetRunningStatusUpdatedEvent();
            BeanUtils.copyProperties(command, event);
            event.setDatasetDataCollection(datasetDataCollection);
            apply(event, metaData);
        } catch (Exception e) {
            LOG.error("Error while updating dataset running status to {} for dataset of dataflowId {} and dataProviderId {}, jobId {}, {]", DatasetRunningStatusEnum.RESTORING_SNAPSHOT,
                    command.getDataflowId(), command.getDataProviderId(), command.getJobId(), e.getMessage());
            throw e;
        }
    }

    @EventSourcingHandler
    public void on(DatasetRunningStatusUpdatedEvent event) {
        this.datasetDataCollection = event.getDatasetDataCollection();
    }

    @CommandHandler
    public void handle(MarkSnapshotReleasedCommand command, SnapshotRepository snapshotRepository, MetaData metaData) {
        try {
            datasetDateRelease = new HashMap<>();
            command.getDatasetSnapshots().entrySet().stream().forEach(entry -> {
                Long datasetId = entry.getKey();
                Long snapshotId = entry.getValue();
                LOG.info("Mark snapshot with id {} released for dataflowId {}, dataProviderId {}, dataset {}, jobId {}", snapshotId, command.getDataflowId(), command.getDataProviderId(), datasetId, command.getJobId());
                // Mark the snapshot released
                snapshotRepository.releaseSnaphot(datasetId, snapshotId);
                // Add the date of the release
                Optional<Snapshot> snapshot = snapshotRepository.findById(snapshotId);
                if (snapshot.isPresent()) {
                    Date dateRelease = java.sql.Timestamp.valueOf(LocalDateTime.now());
                    snapshot.get().setDateReleased(dateRelease);
                    snapshotRepository.save(snapshot.get());
                    datasetDateRelease.put(datasetId, dateRelease);
                }
                LOG.info("Snapshot with id {} marked released for dataflowId {}, dataProviderId {}, dataset {}, jobId {}", snapshotId, command.getDataflowId(), command.getDataProviderId(), datasetId, command.getJobId());
            });
            SnapshotMarkedReleasedEvent event = new SnapshotMarkedReleasedEvent();
            BeanUtils.copyProperties(command, event);
            event.setDatasetDateRelease(datasetDateRelease);
            apply(event, metaData);
        } catch (Exception e) {
            LOG.error("Error while marking snapshot released for dataset of dataflowId {}, dataProviderId {}, jobId {}, {}", command.getDataflowId(), command.getDataProviderId(), command.getJobId(), e.getMessage());
            throw e;
        }
    }

    @EventSourcingHandler
    public void on(SnapshotMarkedReleasedEvent event) {
        this.datasetDateRelease = event.getDatasetDateRelease();
    }

    @CommandHandler
    public void handle(UpdateChangesEuDatasetCommand command, RepresentativeControllerZuul representativeControllerZuul, ChangesEUDatasetRepository changesEUDatasetRepository, MetaData metaData) {
        try {
            LinkedHashMap auth = (LinkedHashMap) metaData.get("auth");
            setAuthorities(auth);

            command.getDatasetDataCollection().values().forEach(dataCollectionId -> {
                ChangesEUDataset providerRelease = new ChangesEUDataset();
                providerRelease.setDatacollection(dataCollectionId);
                DataProviderVO provider = representativeControllerZuul.findDataProviderById(command.getDataProviderId());
                providerRelease.setProvider(provider.getCode());
                LOG.info("Updating table changes_eudataset for dataCollectionId {}, dataProviderId {} of dataflowId {}, jobId {}", dataCollectionId, command.getDataProviderId(), command.getDataflowId(), command.getJobId());
                changesEUDatasetRepository.saveAndFlush(providerRelease);
                LOG.info("Updated table changes_eudataset for dataCollectionId {}, dataProviderId {} of dataflowId {}, jobId {}", dataCollectionId, command.getDataProviderId(), command.getDataflowId(), command.getJobId());
            });
            ChangesEuDatasetUpdatedEvent event = new ChangesEuDatasetUpdatedEvent();
            BeanUtils.copyProperties(command, event);
            apply(event, metaData);
        } catch (Exception e) {
            LOG.error("Error while updating table changes_eudataset for dataProviderId {} of dataflowId {}, jobId {}, {}", command.getDataProviderId(), command.getDataflowId(), command.getJobId(), e.getMessage());
            throw e;
        }
    }

    @CommandHandler
    public void handle(SavePublicFilesForReleaseCommand command, DataFlowControllerZuul dataflowControllerZuul, FileTreatmentHelper fileTreatmentHelper, MetaData metaData) throws IOException {
        try {
            LinkedHashMap auth = (LinkedHashMap) metaData.get("auth");
            setAuthorities(auth);

            DataFlowVO dataflowVO = dataflowControllerZuul.getMetabaseById(command.getDataflowId());
            if (dataflowVO.isShowPublicInfo()) {
                fileTreatmentHelper.savePublicFiles(command.getDataflowId(), command.getDataProviderId());
                LOG.info("Public files for release created for dataflowId {} with dataProviderId {}, jobId {}", command.getDataflowId(), command.getDataProviderId(), command.getJobId());
            }
            PublicFilesForReleaseSavedEvent event = new PublicFilesForReleaseSavedEvent();
            BeanUtils.copyProperties(command, event);
            apply(event, metaData);
        } catch (Exception e) {
            LOG.error("Folder not created in dataflowId {} with dataProviderId {}, jobId {}, {}", command.getDataflowId(), command.getDataProviderId(), command.getJobId(), e.getMessage());
            throw e;
        }
    }

    @CommandHandler
    public void handle(RemoveReleaseLocksCommand command, DatasetSnapshotService datasetSnapshotService, MetaData metaData) throws EEAException {
        try {
            datasetSnapshotService.releaseLocksRelatedToRelease(command.getDataflowId(), command.getDataProviderId());
            LOG.info("Release locks removed for dataflowId {}, dataProviderId {}, jobId {}", command.getDataflowId(), command.getDataProviderId(), command.getJobId());
            ReleaseLocksRemovedEvent event = new ReleaseLocksRemovedEvent();
            BeanUtils.copyProperties(command, event);
            apply(event, metaData);
        } catch (Exception e) {
            LOG.error("Error while releasing locks for dataflowId {}, dataProvider {}, jobId {}, {}", command.getDataflowId(), command.getDataProviderId(), command.getJobId(), e.getMessage());
            throw e;
        }
    }

    @CommandHandler
    public void handle(SendNotificationForSuccessfulReleaseCommand command, DatasetMetabaseService datasetMetabaseService, MetaData metaData,
                       @Autowired KafkaSenderUtils kafkaSenderUtils) throws EEAException {
        try {
            LinkedHashMap auth = (LinkedHashMap) metaData.get("auth");
            setAuthorities(auth);

            List<Long> datasetMetabaseListIds = datasetMetabaseService.getDatasetIdsByDataflowIdAndDataProviderId(command.getDataflowId(), command.getDataProviderId());

            // we send different notification if have more than one dataset or have only one to redirect
            if (!Collections.isEmpty(datasetMetabaseListIds) && datasetMetabaseListIds.size() > 1) {
                kafkaSenderUtils.releaseNotificableKafkaEvent(EventType.RELEASE_PROVIDER_COMPLETED_EVENT,
                        null,
                        NotificationVO.builder()
                                .user(SecurityContextHolder.getContext().getAuthentication().getName())
                                .dataflowId(command.getDataflowId()).dataflowName(command.getDataflowName())
                                .providerId(command.getDataProviderId()).build());
            } else {
                kafkaSenderUtils.releaseNotificableKafkaEvent(EventType.RELEASE_COMPLETED_EVENT, null,
                        NotificationVO.builder()
                                .user(SecurityContextHolder.getContext().getAuthentication().getName())
                                .dataflowId(command.getDataflowId()).dataflowName(command.getDataflowName())
                                .providerId(command.getDataProviderId()).build());
            }
            LOG.info("Notification sent for successful release of dataset {}, dataflowId {}, dataProviderId {}, jobId {}", command.getDatasetName(), command.getDataflowId(), command.getDataProviderId(), command.getJobId());
            NotificationForSuccessfulReleaseSentEvent event = new NotificationForSuccessfulReleaseSentEvent();
            BeanUtils.copyProperties(command, event);
            apply(event, metaData);
        } catch (Exception e) {
            LOG.error("Error while sending notification for successful release of dataset {}, dataflowId {}, dataProviderId {}, jobId {}, {}", command.getDatasetName(), command.getDataflowId(), command.getDataProviderId(), command.getJobId(), e.getMessage());
            throw e;
        }
    }

    @CommandHandler
    public void handle(RevertDatasetRunningStatusCommand command, DataSetMetabaseRepository dataSetMetabaseRepository, MetaData metaData, @Qualifier("datasetExtendedRepositoryImpl") DatasetExtendedRepository datasetExtendedRepository) {
        try {
            command.getDatasetIds().stream().forEach(id -> {
                Long idDataCollection = command.getDatasetDataCollection().get(id);
                DataSetMetabase dataCollectionMetabase = dataSetMetabaseRepository.findById(idDataCollection).orElse(null);
                LOG.info("Reverting dataset running for datasetId {}, dataflowId {}, dataProviderId {}, jobId {}", idDataCollection, command.getDataflowId(), command.getDataProviderId(), command.getJobId());
                if (dataCollectionMetabase != null) {
                    dataCollectionMetabase.setDatasetRunningStatus(DatasetRunningStatusEnum.ERROR_IN_RELEASE);
                    dataSetMetabaseRepository.save(dataCollectionMetabase);
                    LOG.info("Reverted dataset running for datasetId {}, dataflowId {}, dataProviderId {}, jobId {}", idDataCollection, command.getDataflowId(), command.getDataProviderId(), command.getJobId());
                }
            });

            DatasetRunningStatusUpdatedEvent event = new DatasetRunningStatusUpdatedEvent();
            BeanUtils.copyProperties(command, event);
            event.setDatasetDataCollection(datasetDataCollection);
            apply(event, metaData);
        } catch (Exception e) {
            LOG.error("Error while reverting dataset running for dataset of dataflowId {}, dataProviderId {}, jobId {}, {]", command.getDataflowId(), command.getDataProviderId(), command.getJobId(), e.getMessage());
            throw e;
        }
    }

    @CommandHandler
    public void handle(ReleaseFailureRemoveLocksCommand command, MetaData metaData, DatasetSnapshotService datasetSnapshotService) throws EEAException {
        try {
            datasetSnapshotService.releaseLocksRelatedToRelease(command.getDataflowId(), command.getDataProviderId());
            LOG.info("Release locks removed for dataflowId {}, dataProviderId {}, jobId {} ", command.getDataflowId(), command.getDataProviderId(), command.getJobId());
            FailureReleaseLocksRemovedEvent event = new FailureReleaseLocksRemovedEvent();
            BeanUtils.copyProperties(command, event);
            apply(event, metaData);
        } catch (Exception e) {
            LOG.error("Error while releasing locks for dataflowId {}, dataProviderId {}, jobId {}, {}", command.getDataflowId(), command.getDataProviderId(), command.getJobId(), e.getMessage());
            throw e;
        }
    }

    @CommandHandler
    public void handle(RevertDatasetStatusCommand command, MetaData metaData, DatasetSnapshotService datasetSnapshotService) {
        try {
            LinkedHashMap auth = (LinkedHashMap) metaData.get("auth");
            setAuthorities(auth);

            command.getDatasetIds().stream().forEach(id -> {
                LOG.info("Reverting dataset status to Pending for dataflowId {}, dataProviderId {}, dataset {}, jobId {}", command.getDataflowId(), command.getDataProviderId(), id, command.getJobId());
                datasetSnapshotService.changeDatasetStatus(id, DatasetStatusEnum.PENDING);
                LOG.info("Status reverted to Pending for dataflowId {}, dataProviderId {}, dataset {}", command.getDataflowId(), command.getDataProviderId(), id, command.getJobId());
            });

            DatasetStatusRevertedEvent event = new DatasetStatusRevertedEvent();
            BeanUtils.copyProperties(command, event);
            apply(event, metaData);
        } catch (Exception e) {
            LOG.error("Error while reverting dataset status to Pending for dataflowId {}, dataProviderId {}, jobId {}", command.getDataflowId(), command.getDataProviderId(), command.getJobId());
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

    private JobVO createReleaseJob(CreateSnapshotRecordRorReleaseInMetabaseCommand command, JobControllerZuul jobControllerZuul, JobHistoryControllerZuul jobHistoryControllerZuul, JobStatusEnum statusToInsert) {
        Timestamp ts = new Timestamp(System.currentTimeMillis());
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("dataflowId", command.getDataflowId());
        parameters.put("dataProviderId", command.getDataProviderId());
        JobVO releaseJob = new JobVO(null, JobTypeEnum.RELEASE, JobStatusEnum.IN_PROGRESS, ts, ts, parameters, SecurityContextHolder.getContext().getAuthentication().getName(),true, command.getDataflowId(), command.getDataProviderId(), null);

        LOG.info("Adding release job for dataflowId {}, dataProviderId {} and creator {} with status {}", command.getDataflowId(), command.getDataProviderId(), SecurityContextHolder.getContext().getAuthentication().getName(), statusToInsert);
        releaseJob = jobControllerZuul.save(releaseJob);
        jobHistoryControllerZuul.save(releaseJob);
        LOG.info("Added release job for dataflowId {}, dataProviderId {} and creator {} with status {} and jobId {}", command.getDataflowId(), command.getDataProviderId(), SecurityContextHolder.getContext().getAuthentication().getName(), statusToInsert, releaseJob.getId());
        return releaseJob;
    }

    private String createReleaseProcess(CreateSnapshotRecordRorReleaseInMetabaseCommand command, JobProcessControllerZuul jobProcessControllerZuul, ProcessControllerZuul processControllerZuul, JobVO releaseJob, Long datasetId) {
        LOG.info("Creating release process for dataflowId {}, dataProviderId {}, jobId {}", command.getDataflowId(), command.getDataProviderId(), releaseJob.getId());
        String processId = UUID.randomUUID().toString();
        processControllerZuul.updateProcess(datasetId, command.getDataflowId(),
                ProcessStatusEnum.IN_QUEUE, ProcessTypeEnum.RELEASE, processId,
                SecurityContextHolder.getContext().getAuthentication().getName(), defaultReleaseProcessPriority, true);
        LOG.info("Created release process for dataflowId {}, dataProviderId {}, jobId {} and processId {}", command.getDataflowId(), command.getDataProviderId(), releaseJob.getId(), processId);

        LOG.info("Creating jobProcess for dataflowId {}, dataProviderId {}, jobId {} and release processId {}", command.getDataflowId(), command.getDataProviderId(), releaseJob.getId(), processId);
        JobProcessVO jobProcessVO = new JobProcessVO(null, releaseJob.getId(), processId, datasetId, command.getTransactionId(), command.getDatasetReleaseAggregateId());
        jobProcessControllerZuul.save(jobProcessVO);
        LOG.info("Created jobProcess for dataflowId {}, dataProviderId {}, jobId {} and release processId {}", command.getDataflowId(), command.getDataProviderId(), releaseJob.getId(), processId);

        LOG.info("Updating release process for dataflowId {}, dataProviderId {}, dataset {}, jobId {} and release processId {} to status IN_PROGRESS", command.getDataflowId(), command.getDataProviderId(), datasetId, releaseJob.getId(), processId);
        processControllerZuul.updateProcess(datasetId, command.getDataflowId(),
                ProcessStatusEnum.IN_PROGRESS, ProcessTypeEnum.RELEASE, processId,
                SecurityContextHolder.getContext().getAuthentication().getName(), defaultReleaseProcessPriority, true);
        LOG.info("Updated release process for dataflowId {}, dataProviderId {}, dataset {}, jobId {} and release processId {} to status IN_PROGRESS", command.getDataflowId(), command.getDataProviderId(), datasetId, releaseJob.getId(), processId);
        return processId;
    }

    private CreateSnapshotVO createSnapshotVO() {
        CreateSnapshotVO createSnapshotVO = new CreateSnapshotVO();
        createSnapshotVO.setReleased(true);
        createSnapshotVO.setAutomatic(Boolean.TRUE);
        TimeZone.setDefault(TimeZone.getTimeZone("CET"));
        Date date = new Date();
        SimpleDateFormat formateador = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        createSnapshotVO.setDescription("Release " + formateador.format(date) + " CET");
        return createSnapshotVO;
    }

    private void cancelReleaseBecauseOfBlockers(CreateSnapshotRecordRorReleaseInMetabaseCommand command, KafkaSenderUtils kafkaSenderUtils, JobControllerZuul jobControllerZuul, JobVO releaseJob, Long id) throws EEAException {
        LOG.error(
                "Error in the releasing process of the dataflowId {} and dataProviderId {}, the datasets have blocker errors",
                command.getDataflowId(), command.getDataProviderId());

        releaseJob.setJobStatus(JobStatusEnum.FAILED);
        jobControllerZuul.updateJobStatus(releaseJob.getId(), JobStatusEnum.FAILED);

        kafkaSenderUtils.releaseNotificableKafkaEvent(EventType.RELEASE_BLOCKERS_FAILED_EVENT, null,
                NotificationVO.builder()
                        .user(SecurityContextHolder.getContext().getAuthentication().getName())
                        .datasetId(id)
                        .error("One or more datasets have blockers errors, Release aborted")
                        .providerId(command.getDataProviderId()).build());
    }
}









