package org.eea.recordstore.axon.aggregates;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.messaging.MetaData;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;
import org.eea.axon.release.commands.CreateSnapshotFileForReleaseCommand;
import org.eea.axon.release.commands.CreateValidationProcessForReleaseCommand;
import org.eea.axon.release.commands.RestoreDataFromSnapshotCommand;
import org.eea.axon.release.events.DataRestoredFromSnapshotEvent;
import org.eea.axon.release.events.SnapshotFileForReleaseCreatedEvent;
import org.eea.axon.release.events.ValidationProcessForReleaseCreatedEvent;
import org.eea.interfaces.controller.dataset.DatasetSnapshotController.DataSetSnapshotControllerZuul;
import org.eea.interfaces.controller.validation.ValidationController.ValidationControllerZuul;
import org.eea.interfaces.vo.dataset.enums.DatasetTypeEnum;
import org.eea.interfaces.vo.recordstore.ConnectionDataVO;
import org.eea.interfaces.vo.recordstore.enums.ProcessStatusEnum;
import org.eea.interfaces.vo.recordstore.enums.ProcessTypeEnum;
import org.eea.recordstore.exception.RecordStoreAccessException;
import org.eea.recordstore.service.ProcessService;
import org.eea.recordstore.service.RecordStoreService;
import org.eea.security.jwt.utils.EeaUserDetails;
import org.eea.utils.LiteralConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;

import static org.axonframework.modelling.command.AggregateLifecycle.apply;

@Aggregate
public class RecordStoreReleaseAggregate {

    @AggregateIdentifier
    private String recordStoreReleaseAggregateId;
    private String datasetReleaseAggregateId;
    private String releaseAggregateId;
    private String communicationReleaseAggregateId;
    private String dataflowReleaseAggregateId;
    private String validationReleaseAggregateId;
    private String collaborationReleaseAggregateId;
    private String transactionId;
    private Long dataProviderId;
    private Long dataflowId;
    private boolean restrictFromPublic;
    private boolean validate;
    private List<Long> datasetIds;
    private Map<Long, String> datasetProcessId;
    private Map<Long, Long> datasetSnapshots;
    private Map<Long, Long> datasetDataCollection;

    private static final Logger LOG = LoggerFactory.getLogger(RecordStoreReleaseAggregate.class);

    public RecordStoreReleaseAggregate() {
    }

    @CommandHandler
    public RecordStoreReleaseAggregate(CreateValidationProcessForReleaseCommand command, MetaData metaData, ValidationControllerZuul validationControllerZuul, ProcessService processService) {
        try {
            LinkedHashMap auth = (LinkedHashMap) metaData.get("auth");
            SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                    EeaUserDetails.create(auth.get("name").toString(), new HashSet<>()), auth.get("credentials"), null));
            datasetProcessId = new HashMap<>();
            command.getDatasetIds().forEach(datasetId -> {
                int priority = validationControllerZuul.getPriority(command.getDataflowId());
                LOG.info("Adding validation process for dataflowId: {} dataProvider: {} dataset ", command.getDataflowId(), command.getDataProviderId(), datasetId);
                String processId = UUID.randomUUID().toString();
                datasetProcessId.put(datasetId, processId);
                processService.updateProcess(datasetId, command.getDataflowId(),
                        ProcessStatusEnum.IN_QUEUE, ProcessTypeEnum.VALIDATION, processId,
                        SecurityContextHolder.getContext().getAuthentication().getName(), priority, true);
                processService.insertSagaTransactionIdAndAggregateId(command.getTransactionId(), command.getRecordStoreReleaseAggregateId(), processId);
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
    }

    @CommandHandler
    public void handle(CreateSnapshotFileForReleaseCommand command, DataSetSnapshotControllerZuul dataSetSnapshotControllerZuul, @Qualifier("jdbcRecordStoreServiceImpl") RecordStoreService jdbcRecordStoreService, MetaData metaData) throws SQLException, IOException, InterruptedException {
        try {
            for (Map.Entry<Long, Long> entry : command.getDatasetSnapshots().entrySet()) {
                Long datasetId = entry.getKey();
                Long snapshotId = entry.getValue();
                LOG.info("Creating snapshot file for dataflowId: {} dataProvider: {} dataset: {}", command.getDataflowId(), command.getDataProviderId(), datasetId);
                Long partitionId = dataSetSnapshotControllerZuul.obtainPartition(entry.getKey(), "root");
                jdbcRecordStoreService.createDataSnapshotForRelease(datasetId, snapshotId, partitionId, false);
                LOG.info("Created snapshot file for snapshot for dataflowId: {} dataProvider: {} dataset: {}", snapshotId, command.getDataflowId(), command.getDataProviderId(), datasetId);
            }
            SnapshotFileForReleaseCreatedEvent event = new SnapshotFileForReleaseCreatedEvent();
            BeanUtils.copyProperties(command, event);
            apply(event, metaData);
        } catch (Exception e) {
            LOG.error("Error while creating snapshot file for dataset of dataflowId: {} dataProvider: {}: {}", command.getDataflowId(), command.getDataProviderId(), e.getMessage());
            throw e;
        }
    }

    @EventSourcingHandler
    public void on(SnapshotFileForReleaseCreatedEvent event) {
        this.recordStoreReleaseAggregateId = event.getRecordStoreReleaseAggregateId();
        this.collaborationReleaseAggregateId = event.getCollaborationReleaseAggregateId();
        this.communicationReleaseAggregateId = event.getCommunicationReleaseAggregateId();
        this.dataflowReleaseAggregateId = event.getDataflowReleaseAggregateId();
        this.recordStoreReleaseAggregateId = event.getRecordStoreReleaseAggregateId();
        this.datasetReleaseAggregateId = event.getDatasetReleaseAggregateId();
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
    public void handle(RestoreDataFromSnapshotCommand command, DataSetSnapshotControllerZuul dataSetSnapshotControllerZuul, @Qualifier("jdbcRecordStoreServiceImpl") RecordStoreService jdbcRecordStoreService,
                                       MetaData metaData) throws IOException, SQLException, RecordStoreAccessException {
        try {
            for (Long snapshotId : command.getDatasetSnapshots().values()) {
                Long dataCollectionId = dataSetSnapshotControllerZuul.findDataCollectionIdBySnapshotId(snapshotId);
                LOG.info("Restoring snapshot {} for dataCollection {} of dataflow {}", snapshotId, dataCollectionId, command.getDataflowId());
                ConnectionDataVO datasetConnection = jdbcRecordStoreService.getConnectionDataForDataset(LiteralConstants.DATASET_PREFIX + dataCollectionId);
                try (Connection con = DriverManager.getConnection(datasetConnection.getConnectionString(),
                        datasetConnection.getUser(), datasetConnection.getPassword())) {
                    con.setAutoCommit(true);
                    jdbcRecordStoreService.restoreFromSnapshot(dataCollectionId, snapshotId, DatasetTypeEnum.REPORTING, con);
                    LOG.info("Snapshot {} restored for dataCollection {} of dataflow {}", snapshotId, dataCollectionId, command.getDataflowId());
                } catch (Exception e) {
                    throw e;
                }
            }
            DataRestoredFromSnapshotEvent event = new DataRestoredFromSnapshotEvent();
            BeanUtils.copyProperties(command, event);
            AggregateLifecycle.apply(event, metaData);
        } catch (Exception e) {
            LOG.error("Error while restoring snapshot for dataCollection of dataflow {}, {}", command.getDataflowId(), e.getMessage());
            throw e;
        }
    }

    @EventSourcingHandler
    public void on(DataRestoredFromSnapshotEvent event) {
        this.recordStoreReleaseAggregateId = event.getRecordStoreReleaseAggregateId();
        this.collaborationReleaseAggregateId = event.getCollaborationReleaseAggregateId();
        this.communicationReleaseAggregateId = event.getCommunicationReleaseAggregateId();
        this.dataflowReleaseAggregateId = event.getDataflowReleaseAggregateId();
        this.recordStoreReleaseAggregateId = event.getRecordStoreReleaseAggregateId();
        this.datasetReleaseAggregateId = event.getDatasetReleaseAggregateId();
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
    }
}





















