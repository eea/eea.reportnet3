package org.eea.dataset.axon.aggregates;

import io.jsonwebtoken.lang.Collections;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.messaging.MetaData;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.spring.stereotype.Aggregate;
import org.eea.axon.release.commands.*;
import org.eea.axon.release.events.*;
import org.eea.dataset.persistence.data.repository.ValidationRepository;
import org.eea.dataset.persistence.metabase.domain.*;
import org.eea.dataset.persistence.metabase.repository.*;
import org.eea.dataset.service.DatasetMetabaseService;
import org.eea.dataset.service.DatasetSnapshotService;
import org.eea.dataset.service.helper.FileTreatmentHelper;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.DataFlowController.DataFlowControllerZuul;
import org.eea.interfaces.controller.dataflow.RepresentativeController.RepresentativeControllerZuul;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
import org.eea.interfaces.vo.dataflow.DataProviderVO;
import org.eea.interfaces.vo.dataset.CreateSnapshotVO;
import org.eea.interfaces.vo.dataset.enums.DatasetRunningStatusEnum;
import org.eea.interfaces.vo.dataset.enums.ErrorTypeEnum;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.axonframework.modelling.command.AggregateLifecycle.apply;

@Aggregate
public class DatasetReleaseAggregate {

    @AggregateIdentifier
    private String datasetReleaseAggregateId;
    private String releaseAggregateId;
    private String communicationReleaseAggregateId;
    private String dataflowReleaseAggregateId;
    private String validationReleaseAggregateId;
    private String collaborationReleaseAggregateId;
    private String recordStoreReleaseAggregateId;
    private String transactionId;
    private Long dataProviderId;
    private Long dataflowId;
    private boolean restrictFromPublic;
    private boolean validate;
    private List<Long> datasetIds;
    private Map<Long, Long> datasetSnapshots;
    private Map<Long, Long> datasetDataCollection;
    private List<Long> dataCollectionForDeletion;
    private Map<Long, Date> datasetDateRelease;
    private String dataflowName;
    private String datasetName;

    /**
     * The Constant LOG.
     */
    private static final Logger LOG = LoggerFactory.getLogger(DatasetReleaseAggregate.class);

    public DatasetReleaseAggregate() {
    }

    @CommandHandler
    public DatasetReleaseAggregate(AddReleaseLocksCommand command, MetaData metaData, DatasetSnapshotService datasetSnapshotService, ReportingDatasetRepository reportingDatasetRepository) throws EEAException {
        try {
            LOG.info("Adding release locks for DataflowId: {} DataProviderId: {}", command.getDataflowId(), command.getDataProviderId());
            LinkedHashMap auth = (LinkedHashMap) metaData.get("auth");
            SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                    EeaUserDetails.create(auth.get("name").toString(), new HashSet<>()), auth.get("credentials"), null));

            // Lock all the operations related to the datasets involved
            List<Long> datasetIds = reportingDatasetRepository.findByDataflowId(command.getDataflowId()).stream().filter(rd -> rd.getDataProviderId().equals(command.getDataProviderId())).map(ReportingDataset::getId)
                    .distinct().collect(Collectors.toList());
            command.setDatasetIds(datasetIds);
            datasetSnapshotService.addLocksRelatedToRelease(datasetIds, command.getDataflowId());
            LOG.info("Release locks added for DataflowId: {} DataProviderId: {}", command.getDataflowId(), command.getDataProviderId());
            ReleaseLocksAddedEvent event = new ReleaseLocksAddedEvent();
            BeanUtils.copyProperties(command, event);
            apply(event, metaData);
        } catch (Exception e) {
            LOG.error("Error while adding release locks for DataflowId: {} DataProviderId: {}: {}", command.getDataflowId(), command.getDataProviderId(), e.getMessage());
            throw e;
        }
    }

    @EventSourcingHandler
    public void on(ReleaseLocksAddedEvent event) {
        this.datasetReleaseAggregateId = event.getDatasetReleaseAggregateId();
        this.recordStoreReleaseAggregateId = event.getRecordStoreReleaseAggregateId();
        this.collaborationReleaseAggregateId = event.getCollaborationReleaseAggregateId();
        this.communicationReleaseAggregateId = event.getCommunicationReleaseAggregateId();
        this.dataflowReleaseAggregateId = event.getDataflowReleaseAggregateId();
        this.recordStoreReleaseAggregateId = event.getRecordStoreReleaseAggregateId();
        this.validationReleaseAggregateId = event.getValidationReleaseAggregateId();
        this.releaseAggregateId = event.getReleaseAggregateId();
        this.transactionId = event.getTransactionId();
        this.dataflowId = event.getDataflowId();
        this.dataProviderId = event.getDataProviderId();
        this.restrictFromPublic = event.isRestrictFromPublic();
        this.validate = event.isValidate();
        this.datasetIds = event.getDatasetIds();
    }

    @CommandHandler
    public void handle(CreateSnapshotRecordRorReleaseInMetabaseCommand command, DatasetSnapshotService datasetSnapshotService, ValidationRepository validationRepository, MetaData metaData,
                       @Autowired KafkaSenderUtils kafkaSenderUtils) throws EEAException {
        try {
            LinkedHashMap auth = (LinkedHashMap) metaData.get("auth");
            SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                    EeaUserDetails.create(auth.get("name").toString(), new HashSet<>()), auth.get("credentials"), null));

            boolean haveBlockers = false;
            for (Long id : command.getDatasetIds()) {
                TenantResolver.setTenantName(String.format(LiteralConstants.DATASET_FORMAT_NAME, id));
                if (validationRepository.existsByLevelError(ErrorTypeEnum.BLOCKER)) {
                    haveBlockers = true;
                    LOG.error(
                            "Error in the releasing process of the dataflowId {} and dataProviderId {}, the datasets have blocker errors",
                            command.getDataflowId(), command.getDataProviderId());

                    kafkaSenderUtils.releaseNotificableKafkaEvent(EventType.RELEASE_BLOCKERS_FAILED_EVENT, null,
                            NotificationVO.builder()
                                    .user(SecurityContextHolder.getContext().getAuthentication().getName())
                                    .datasetId(id)
                                    .error("One or more datasets have blockers errors, Release aborted")
                                    .providerId(command.getDataProviderId()).build());
                    break;
                }
            }

            if (!haveBlockers) {
                CreateSnapshotVO createSnapshotVO = new CreateSnapshotVO();
                createSnapshotVO.setReleased(true);
                createSnapshotVO.setAutomatic(Boolean.TRUE);
                TimeZone.setDefault(TimeZone.getTimeZone("CET"));
                Date date = new Date();
                SimpleDateFormat formateador = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                createSnapshotVO.setDescription("Release " + formateador.format(date) + " CET");
                datasetSnapshots = new HashMap<>();

                command.getDatasetIds().stream().forEach(id -> {
                    LOG.info("Creating snapshot record for dataflowId: {} dataProvider: {} dataset: {}", command.getDataflowId(), command.getDataProviderId(), id);
                    Snapshot snapshot = datasetSnapshotService.createSnapshotInMetabase(id, createSnapshotVO);
                    LOG.info("Snapshot record created in metabase for dataflowId: {} dataProvider: {} dataset: {}", command.getDataflowId(), command.getDataProviderId(), id);
                    datasetSnapshots.put(id, snapshot.getId());
                });
            }
            SnapshotRecordForReleaseCreatedInMetabaseEvent event = new SnapshotRecordForReleaseCreatedInMetabaseEvent();
            BeanUtils.copyProperties(command, event);
            event.setDatasetSnapshots(datasetSnapshots);
            apply(event, metaData);
        } catch (Exception e) {
            LOG.error("Error while Creating snapshot record for dataflowId: {} dataProvider: {}: {}", command.getDataflowId(), command.getDataProviderId(), e.getMessage());
            throw e;
        }
    }

    @EventSourcingHandler
    public void on(SnapshotRecordForReleaseCreatedInMetabaseEvent event) {
        this.datasetReleaseAggregateId = event.getDatasetReleaseAggregateId();
        this.recordStoreReleaseAggregateId = event.getRecordStoreReleaseAggregateId();
        this.collaborationReleaseAggregateId = event.getCollaborationReleaseAggregateId();
        this.communicationReleaseAggregateId = event.getCommunicationReleaseAggregateId();
        this.dataflowReleaseAggregateId = event.getDataflowReleaseAggregateId();
        this.recordStoreReleaseAggregateId = event.getRecordStoreReleaseAggregateId();
        this.validationReleaseAggregateId = event.getValidationReleaseAggregateId();
        this.releaseAggregateId = event.getReleaseAggregateId();
        this.transactionId = event.getTransactionId();
        this.dataflowId = event.getDataflowId();
        this.dataProviderId = event.getDataProviderId();
        this.restrictFromPublic = event.isRestrictFromPublic();
        this.validate = event.isValidate();
        this.datasetIds = event.getDatasetIds();
        this.datasetSnapshots = event.getDatasetSnapshots();
    }

    @CommandHandler
    public void handle(UpdateDatasetStatusCommand command, DatasetSnapshotService datasetSnapshotService, MetaData metaData, DataSetMetabaseRepository metabaseRepository,
                       DataCollectionRepository dataCollectionRepository) {
        try {
            LinkedHashMap auth = (LinkedHashMap) metaData.get("auth");
            SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                    EeaUserDetails.create(auth.get("name").toString(), new HashSet<>()), auth.get("credentials"), null));

            datasetDataCollection = new HashMap<>();
            dataCollectionForDeletion = new ArrayList<>();
            command.getDatasetIds().stream().forEach(id -> {
                LOG.info("Updating dataset status for dataflowId: {} dataProvider: {} dataset {}", command.getDataflowId(), command.getDataProviderId(), id);
                datasetSnapshotService.updateDatasetStatus(id);
                LOG.info("Status updated for dataflowId: {} dataProvider: {} dataset {}", command.getDataflowId(), command.getDataProviderId(), id);

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
            LOG.error("Error while updating dataset status for dataflowId: {} dataProvider: {}", command.getDataflowId(), command.getDataProviderId());
            throw e;
        }
    }

    @EventSourcingHandler
    public void on(DatasetStatusUpdatedEvent event) {
        this.datasetReleaseAggregateId = event.getDatasetReleaseAggregateId();
        this.recordStoreReleaseAggregateId = event.getRecordStoreReleaseAggregateId();
        this.collaborationReleaseAggregateId = event.getCollaborationReleaseAggregateId();
        this.communicationReleaseAggregateId = event.getCommunicationReleaseAggregateId();
        this.dataflowReleaseAggregateId = event.getDataflowReleaseAggregateId();
        this.recordStoreReleaseAggregateId = event.getRecordStoreReleaseAggregateId();
        this.validationReleaseAggregateId = event.getValidationReleaseAggregateId();
        this.releaseAggregateId = event.getReleaseAggregateId();
        this.transactionId = event.getTransactionId();
        this.dataflowId = event.getDataflowId();
        this.dataProviderId = event.getDataProviderId();
        this.restrictFromPublic = event.isRestrictFromPublic();
        this.validate = event.isValidate();
        this.datasetIds = event.getDatasetIds();
        this.datasetSnapshots = event.getDatasetSnapshots();
        this.datasetDataCollection = event.getDatasetDataCollection();
        this.dataCollectionForDeletion = event.getDataCollectionForDeletion();
    }

    @CommandHandler
    public void handle(DeleteProviderCommand command, DatasetSnapshotService datasetSnapshotService, MetaData metaData) {
        try {
            LinkedHashMap auth = (LinkedHashMap) metaData.get("auth");
            SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                    EeaUserDetails.create(auth.get("name").toString(), new HashSet<>()), auth.get("credentials"), null));

            Long dataCollectionId = command.getDataCollectionForDeletion().get(0);
            LOG.info("Deleting provider for dataflowId: {} dataProvider: {} dataCollection {}", command.getDataflowId(), command.getDataProviderId(), dataCollectionId);
            datasetSnapshotService.deleteProvider(dataCollectionId, command.getDataProviderId());
            LOG.info("Provider deleted for dataflowId: {} dataProvider: {} dataCollection {}", command.getDataflowId(), command.getDataProviderId(), dataCollectionId);
            ProviderDeletedEvent event = new ProviderDeletedEvent();
            BeanUtils.copyProperties(command, event);
            event.getDataCollectionForDeletion().remove(dataCollectionId);
            apply(event, metaData);
        } catch (Exception e) {
            LOG.error("Error while deleting provider for dataflowId: {} dataProvider: {}: {}", command.getDataflowId(), command.getDataProviderId(), e.getMessage());
            throw e;
        }
    }

    @EventSourcingHandler
    public void on(ProviderDeletedEvent event) {
        this.datasetReleaseAggregateId = event.getDatasetReleaseAggregateId();
        this.recordStoreReleaseAggregateId = event.getRecordStoreReleaseAggregateId();
        this.collaborationReleaseAggregateId = event.getCollaborationReleaseAggregateId();
        this.communicationReleaseAggregateId = event.getCommunicationReleaseAggregateId();
        this.dataflowReleaseAggregateId = event.getDataflowReleaseAggregateId();
        this.recordStoreReleaseAggregateId = event.getRecordStoreReleaseAggregateId();
        this.validationReleaseAggregateId = event.getValidationReleaseAggregateId();
        this.releaseAggregateId = event.getReleaseAggregateId();
        this.transactionId = event.getTransactionId();
        this.dataflowId = event.getDataflowId();
        this.dataProviderId = event.getDataProviderId();
        this.restrictFromPublic = event.isRestrictFromPublic();
        this.validate = event.isValidate();
        this.datasetIds = event.getDatasetIds();
        this.datasetSnapshots = event.getDatasetSnapshots();
        this.datasetDataCollection = event.getDatasetDataCollection();
        this.dataCollectionForDeletion = event.getDataCollectionForDeletion();
    }

    @CommandHandler
    public void handle(UpdateDatasetRunningStatusCommand command, DataSetMetabaseRepository dataSetMetabaseRepository, MetaData metaData) {
        try {
            command.getDatasetIds().stream().forEach(id -> {
                Long idDataCollection = command.getDatasetDataCollection().get(id);

                DataSetMetabase dataCollectionMetabase = dataSetMetabaseRepository.findById(idDataCollection).orElse(null);
                LOG.info("Updating dataset running status to {} for datasetId {} of dataflow {} and dataProvider {}.", DatasetRunningStatusEnum.RESTORING_SNAPSHOT,
                        idDataCollection, command.getDataflowId(), command.getDataProviderId());
                if (dataCollectionMetabase != null) {
                    dataCollectionMetabase.setDatasetRunningStatus(DatasetRunningStatusEnum.RESTORING_SNAPSHOT);
                    dataSetMetabaseRepository.save(dataCollectionMetabase);
                    LOG.info("Updated dataset running status to {} for datasetId {} of dataflow {} and dataProvider {}.", DatasetRunningStatusEnum.RESTORING_SNAPSHOT,
                            idDataCollection, command.getDataflowId(), command.getDataProviderId());
                }
            });
            DatasetRunningStatusUpdatedEvent event = new DatasetRunningStatusUpdatedEvent();
            BeanUtils.copyProperties(command, event);
            event.setDatasetDataCollection(datasetDataCollection);
            apply(event, metaData);
        } catch (Exception e) {
            LOG.error("Error while updating dataset running status to {} for dataset of dataflow {} and dataProvider {}: {]", DatasetRunningStatusEnum.RESTORING_SNAPSHOT,
                    command.getDataflowId(), command.getDataProviderId(), e.getMessage());
            throw e;
        }
    }

    @EventSourcingHandler
    public void on(DatasetRunningStatusUpdatedEvent event) {
        this.datasetReleaseAggregateId = event.getDatasetReleaseAggregateId();
        this.recordStoreReleaseAggregateId = event.getRecordStoreReleaseAggregateId();
        this.collaborationReleaseAggregateId = event.getCollaborationReleaseAggregateId();
        this.communicationReleaseAggregateId = event.getCommunicationReleaseAggregateId();
        this.dataflowReleaseAggregateId = event.getDataflowReleaseAggregateId();
        this.recordStoreReleaseAggregateId = event.getRecordStoreReleaseAggregateId();
        this.validationReleaseAggregateId = event.getValidationReleaseAggregateId();
        this.releaseAggregateId = event.getReleaseAggregateId();
        this.transactionId = event.getTransactionId();
        this.dataflowId = event.getDataflowId();
        this.dataProviderId = event.getDataProviderId();
        this.restrictFromPublic = event.isRestrictFromPublic();
        this.validate = event.isValidate();
        this.datasetIds = event.getDatasetIds();
        this.datasetSnapshots = event.getDatasetSnapshots();
    }

    @CommandHandler
    public void handle(MarkSnapshotReleasedCommand command, SnapshotRepository snapshotRepository, MetaData metaData) {
        try {
            command.getDatasetSnapshots().entrySet().stream().forEach(entry -> {
                Long datasetId = entry.getKey();
                Long snapshotId = entry.getValue();
                datasetDateRelease = new HashMap<>();
                LOG.info("Mark snapshot with id {} released for dataflow {}, dataProvider {}, dataset {}", snapshotId, command.getDataflowId(), command.getDataProviderId(), datasetId);
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
                LOG.info("Snapshot with id {} marked released for dataflow {}, dataProvider {}, dataset {}", snapshotId, command.getDataflowId(), command.getDataProviderId(), datasetId);
            });
            SnapshotMarkedReleasedEvent event = new SnapshotMarkedReleasedEvent();
            BeanUtils.copyProperties(command, event);
            event.setDatasetDateRelease(datasetDateRelease);
            apply(event, metaData);
        } catch (Exception e) {
            LOG.error("Error while marking snapshot released for dataset of dataflow {}, dataProvider {}: {}", command.getDataflowId(), command.getDataProviderId(), e.getMessage());
            throw e;
        }
    }

    @EventSourcingHandler
    public void on(SnapshotMarkedReleasedEvent event) {
        this.datasetReleaseAggregateId = event.getDatasetReleaseAggregateId();
        this.recordStoreReleaseAggregateId = event.getRecordStoreReleaseAggregateId();
        this.collaborationReleaseAggregateId = event.getCollaborationReleaseAggregateId();
        this.communicationReleaseAggregateId = event.getCommunicationReleaseAggregateId();
        this.dataflowReleaseAggregateId = event.getDataflowReleaseAggregateId();
        this.recordStoreReleaseAggregateId = event.getRecordStoreReleaseAggregateId();
        this.validationReleaseAggregateId = event.getValidationReleaseAggregateId();
        this.releaseAggregateId = event.getReleaseAggregateId();
        this.transactionId = event.getTransactionId();
        this.dataflowId = event.getDataflowId();
        this.dataProviderId = event.getDataProviderId();
        this.restrictFromPublic = event.isRestrictFromPublic();
        this.validate = event.isValidate();
        this.datasetIds = event.getDatasetIds();
        this.datasetSnapshots = event.getDatasetSnapshots();
        this.datasetDateRelease = event.getDatasetDateRelease();
        this.datasetDataCollection = event.getDatasetDataCollection();
    }

    @CommandHandler
    public void handle(UpdateChangesEuDatasetCommand command, RepresentativeControllerZuul representativeControllerZuul, ChangesEUDatasetRepository changesEUDatasetRepository, MetaData metaData) {
        try {
            LinkedHashMap auth = (LinkedHashMap) metaData.get("auth");
            SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                    EeaUserDetails.create(auth.get("name").toString(), new HashSet<>()), auth.get("credentials"), null));

            command.getDatasetDataCollection().values().forEach(dataCollectionId -> {
                ChangesEUDataset providerRelease = new ChangesEUDataset();
                providerRelease.setDatacollection(dataCollectionId);
                DataProviderVO provider = representativeControllerZuul.findDataProviderById(command.getDataProviderId());
                providerRelease.setProvider(provider.getCode());
                LOG.info("Updating table changes_eudataset for dataCollectionId {}, dataProviderId {} of dataflowId {}", dataCollectionId, command.getDataProviderId(), command.getDataflowId());
                changesEUDatasetRepository.saveAndFlush(providerRelease);
                LOG.info("Updated table changes_eudataset for dataCollectionId {}, dataProviderId {} of dataflowId {}", dataCollectionId, command.getDataProviderId(), command.getDataflowId());
            });
            ChangesEuDatasetUpdatedEvent event = new ChangesEuDatasetUpdatedEvent();
            BeanUtils.copyProperties(command, event);
            apply(event, metaData);
        } catch (Exception e) {
            LOG.error("Error while updating table changes_eudataset for dataProvider {} of dataflow {}: {}", command.getDataProviderId(), command.getDataflowId(), e.getMessage());
            throw e;
        }
    }

    @EventSourcingHandler
    public void on(ChangesEuDatasetUpdatedEvent event) {
        this.datasetReleaseAggregateId = event.getDatasetReleaseAggregateId();
        this.recordStoreReleaseAggregateId = event.getRecordStoreReleaseAggregateId();
        this.collaborationReleaseAggregateId = event.getCollaborationReleaseAggregateId();
        this.communicationReleaseAggregateId = event.getCommunicationReleaseAggregateId();
        this.dataflowReleaseAggregateId = event.getDataflowReleaseAggregateId();
        this.recordStoreReleaseAggregateId = event.getRecordStoreReleaseAggregateId();
        this.validationReleaseAggregateId = event.getValidationReleaseAggregateId();
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
    }

    @CommandHandler
    public void handle(SavePublicFilesForReleaseCommand command, DataFlowControllerZuul dataflowControllerZuul, FileTreatmentHelper fileTreatmentHelper, MetaData metaData) throws IOException {
        try {
            LinkedHashMap auth = (LinkedHashMap) metaData.get("auth");
            SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                    EeaUserDetails.create(auth.get("name").toString(), new HashSet<>()), auth.get("credentials"), null));

            DataFlowVO dataflowVO = dataflowControllerZuul.getMetabaseById(command.getDataflowId());
            if (dataflowVO.isShowPublicInfo()) {
                fileTreatmentHelper.savePublicFiles(command.getDataflowId(), command.getDataProviderId());
                LOG.info("Public files for release created for dataflow {} with dataprovider {} ", command.getDataflowId(), command.getDataProviderId());
            }
            PublicFilesForReleaseSavedEvent event = new PublicFilesForReleaseSavedEvent();
            BeanUtils.copyProperties(command, event);
            apply(event, metaData);
        } catch (Exception e) {
            LOG.error("Folder not created in dataflow {} with dataprovider {}: {}", command.getDataflowId(), command.getDataProviderId(), e.getMessage());
            throw e;
        }
    }

    @EventSourcingHandler
    public void on(PublicFilesForReleaseSavedEvent event) {
        this.datasetReleaseAggregateId = event.getDatasetReleaseAggregateId();
        this.recordStoreReleaseAggregateId = event.getRecordStoreReleaseAggregateId();
        this.collaborationReleaseAggregateId = event.getCollaborationReleaseAggregateId();
        this.communicationReleaseAggregateId = event.getCommunicationReleaseAggregateId();
        this.dataflowReleaseAggregateId = event.getDataflowReleaseAggregateId();
        this.recordStoreReleaseAggregateId = event.getRecordStoreReleaseAggregateId();
        this.validationReleaseAggregateId = event.getValidationReleaseAggregateId();
        this.transactionId = event.getTransactionId();
        this.dataflowId = event.getDataflowId();
        this.dataProviderId = event.getDataProviderId();
        this.restrictFromPublic = event.isRestrictFromPublic();
        this.validate = event.isValidate();
        this.datasetIds = event.getDatasetIds();
        this.datasetSnapshots = event.getDatasetSnapshots();
        this.datasetDataCollection = event.getDatasetDataCollection();
        this.datasetDateRelease = event.getDatasetDateRelease();
    }

    @CommandHandler
    public void handle(RemoveReleaseLocksCommand command, DatasetSnapshotService datasetSnapshotService, MetaData metaData) throws EEAException {
        try {
            datasetSnapshotService.releaseLocksRelatedToRelease(command.getDataflowId(), command.getDataProviderId());
            LOG.info("Release locks removed for dataflow {} and dataProvider {} ", command.getDataflowId(), command.getDataProviderId());
            ReleaseLocksRemovedEvent event = new ReleaseLocksRemovedEvent();
            BeanUtils.copyProperties(command, event);
            apply(event, metaData);
        } catch (Exception e) {
            LOG.error("Error while releasing locks for dataflow {} and dataProvider {}: {}", command.getDataflowId(), command.getDataProviderId(), e.getMessage());
            throw e;
        }
    }

    @EventSourcingHandler
    public void on(ReleaseLocksRemovedEvent event) {
        this.datasetReleaseAggregateId = event.getDatasetReleaseAggregateId();
        this.recordStoreReleaseAggregateId = event.getRecordStoreReleaseAggregateId();
        this.collaborationReleaseAggregateId = event.getCollaborationReleaseAggregateId();
        this.communicationReleaseAggregateId = event.getCommunicationReleaseAggregateId();
        this.dataflowReleaseAggregateId = event.getDataflowReleaseAggregateId();
        this.recordStoreReleaseAggregateId = event.getRecordStoreReleaseAggregateId();
        this.validationReleaseAggregateId = event.getValidationReleaseAggregateId();
        this.transactionId = event.getTransactionId();
        this.dataflowId = event.getDataflowId();
        this.dataProviderId = event.getDataProviderId();
        this.restrictFromPublic = event.isRestrictFromPublic();
        this.validate = event.isValidate();
        this.datasetIds = event.getDatasetIds();
        this.datasetDataCollection = event.getDatasetDataCollection();
        this.datasetDateRelease = event.getDatasetDateRelease();
    }

    @CommandHandler
    public void handle(SendNotificationForSuccessfulReleaseCommand command, DatasetMetabaseService datasetMetabaseService, MetaData metaData,
                       @Autowired KafkaSenderUtils kafkaSenderUtils) throws EEAException {
        try {
            LinkedHashMap auth = (LinkedHashMap) metaData.get("auth");
            SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                    EeaUserDetails.create(auth.get("name").toString(), new HashSet<>()), auth.get("credentials"), null));

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
            LOG.info("Notification sent for successful release of dataset {}, dataflowId {}, dataProviderId {}", command.getDatasetName(), command.getDataflowId(), command.getDataProviderId());
            NotificationForSuccessfulReleaseSentEvent event = new NotificationForSuccessfulReleaseSentEvent();
            BeanUtils.copyProperties(command, event);
            apply(event, metaData);
        } catch (Exception e) {
            LOG.error("Error while sending notification for successful release of dataset {}, dataflowId {}, dataProviderId {}: {}", command.getDatasetName(), command.getDataflowId(), command.getDataProviderId(), e.getMessage());
            throw e;
        }
    }

    @EventSourcingHandler
    public void on(NotificationForSuccessfulReleaseSentEvent event) {
        this.datasetReleaseAggregateId = event.getDatasetReleaseAggregateId();
        this.recordStoreReleaseAggregateId = event.getRecordStoreReleaseAggregateId();
        this.collaborationReleaseAggregateId = event.getCollaborationReleaseAggregateId();
        this.communicationReleaseAggregateId = event.getCommunicationReleaseAggregateId();
        this.dataflowReleaseAggregateId = event.getDataflowReleaseAggregateId();
        this.recordStoreReleaseAggregateId = event.getRecordStoreReleaseAggregateId();
        this.validationReleaseAggregateId = event.getValidationReleaseAggregateId();
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

    @CommandHandler
    public void handle(ReleaseFailureRemoveLocksCommand command, DatasetSnapshotService datasetSnapshotService) throws EEAException {
        try {
            datasetSnapshotService.releaseLocksRelatedToRelease(command.getDataflowId(), command.getDataProviderId());
            LOG.info("Release locks removed for dataflow {} and dataProvider {} ", command.getDataflowId(), command.getDataProviderId());
            FailureReleaseLocksRemovedEvent event = new FailureReleaseLocksRemovedEvent();
            BeanUtils.copyProperties(command, event);
            apply(event);
        } catch (Exception e) {
            LOG.error("Error while releasing locks for dataflow {} and dataProvider {}: {}", command.getDataflowId(), command.getDataProviderId(), e.getMessage());
            throw e;
        }
    }

    @EventSourcingHandler
    public void on(FailureReleaseLocksRemovedEvent event) {
        this.datasetReleaseAggregateId = event.getDatasetReleaseAggregateId();
        this.recordStoreReleaseAggregateId = event.getRecordStoreReleaseAggregateId();
        this.collaborationReleaseAggregateId = event.getCollaborationReleaseAggregateId();
        this.communicationReleaseAggregateId = event.getCommunicationReleaseAggregateId();
        this.dataflowReleaseAggregateId = event.getDataflowReleaseAggregateId();
        this.recordStoreReleaseAggregateId = event.getRecordStoreReleaseAggregateId();
        this.validationReleaseAggregateId = event.getValidationReleaseAggregateId();
        this.transactionId = event.getTransactionId();
        this.dataflowId = event.getDataflowId();
        this.dataProviderId = event.getDataProviderId();
        this.restrictFromPublic = event.isRestrictFromPublic();
        this.validate = event.isValidate();
        this.datasetIds = event.getDatasetIds();
    }
}









