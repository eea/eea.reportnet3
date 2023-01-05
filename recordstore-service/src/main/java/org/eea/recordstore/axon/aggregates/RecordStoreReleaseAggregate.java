package org.eea.recordstore.axon.aggregates;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.messaging.MetaData;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;
import org.eea.axon.release.commands.CancelReleaseProcessCommand;
import org.eea.axon.release.commands.CreateSnapshotFileForReleaseCommand;
import org.eea.axon.release.commands.RestoreDataFromSnapshotCommand;
import org.eea.axon.release.events.DataRestoredFromSnapshotEvent;
import org.eea.axon.release.events.DataRestoredFromSnapshotFailedEvent;
import org.eea.axon.release.events.ReleaseProcessCancelledEvent;
import org.eea.axon.release.events.SnapshotFileForReleaseCreatedEvent;
import org.eea.interfaces.controller.dataset.DatasetSnapshotController.DataSetSnapshotControllerZuul;
import org.eea.interfaces.vo.dataset.enums.DatasetTypeEnum;
import org.eea.interfaces.vo.recordstore.ConnectionDataVO;
import org.eea.interfaces.vo.recordstore.ProcessVO;
import org.eea.interfaces.vo.recordstore.enums.ProcessStatusEnum;
import org.eea.interfaces.vo.recordstore.enums.ProcessTypeEnum;
import org.eea.recordstore.service.ProcessService;
import org.eea.recordstore.service.RecordStoreService;
import org.eea.security.jwt.utils.EeaUserDetails;
import org.eea.utils.LiteralConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

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
    private List<Long> datasetsReleased;

    private static final Logger LOG = LoggerFactory.getLogger(RecordStoreReleaseAggregate.class);

    /**
     * The default release process priority
     */
    private int defaultReleaseProcessPriority = 20;

    public RecordStoreReleaseAggregate() {
    }

    @CommandHandler
    public RecordStoreReleaseAggregate(CreateSnapshotFileForReleaseCommand command, DataSetSnapshotControllerZuul dataSetSnapshotControllerZuul, @Qualifier("jdbcRecordStoreServiceImpl") RecordStoreService jdbcRecordStoreService, MetaData metaData) throws SQLException, IOException, InterruptedException {
        try {
            for (Map.Entry<Long, Long> entry : command.getDatasetSnapshots().entrySet()) {
                Long datasetId = entry.getKey();
                Long snapshotId = entry.getValue();
                LOG.info("Creating snapshot file for dataflowId {}, dataProvider {}, dataset {}, jobId {}", command.getDataflowId(), command.getDataProviderId(), datasetId, command.getJobId());
                Long partitionId = dataSetSnapshotControllerZuul.obtainPartition(entry.getKey(), "root");
                jdbcRecordStoreService.createDataSnapshotForRelease(datasetId, snapshotId, partitionId, false, null);
                LOG.info("Created snapshot file for snapshot for dataflowId {}, dataProvider {}, dataset {}, jobId {}", snapshotId, command.getDataflowId(), command.getDataProviderId(), datasetId, command.getJobId());
            }
            SnapshotFileForReleaseCreatedEvent event = new SnapshotFileForReleaseCreatedEvent();
            BeanUtils.copyProperties(command, event);
            apply(event, metaData);
        } catch (Exception e) {
            LOG.error("Error while creating snapshot file for dataset of dataflowId {}, dataProvider {}, jobId {}, {}", command.getDataflowId(), command.getDataProviderId(), command.getJobId(), e.getMessage());
            throw e;
        }
    }

    @EventSourcingHandler
    public void on(SnapshotFileForReleaseCreatedEvent event) {
        this.recordStoreReleaseAggregateId = event.getRecordStoreReleaseAggregateId();
    }

    @CommandHandler
    public void handle(RestoreDataFromSnapshotCommand command, @Qualifier("jdbcRecordStoreServiceImpl") RecordStoreService jdbcRecordStoreService, MetaData metaData) {
        try {
            LinkedHashMap auth = (LinkedHashMap) metaData.get("auth");
            List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
            List<LinkedHashMap<String, String>> authorities = (List<LinkedHashMap<String, String>>) auth.get("authorities");
            authorities.forEach((k -> k.values().forEach(grantedAuthority -> grantedAuthorities.add(new SimpleGrantedAuthority(grantedAuthority)))));
            SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                    EeaUserDetails.create(auth.get("name").toString(), new HashSet<>()), auth.get("credentials"), grantedAuthorities));

            Long datasetId = command.getDatasetToRelease();
            Long snapshotId = command.getDatasetSnapshots().get(datasetId);
            String processId = command.getDatasetReleaseProcessId().get(datasetId);
            Long dataCollectionId = command.getDatasetDataCollection().get(datasetId);
            LOG.info("Restoring snapshot {} for dataCollection {}, dataflowId {}, jobId {}", snapshotId, dataCollectionId, command.getDataflowId(), command.getJobId());
            ConnectionDataVO datasetConnection = jdbcRecordStoreService.getConnectionDataForDataset(LiteralConstants.DATASET_PREFIX + dataCollectionId);
            try (Connection con = DriverManager.getConnection(datasetConnection.getConnectionString(),
                    datasetConnection.getUser(), datasetConnection.getPassword())) {
                con.setAutoCommit(true);
                jdbcRecordStoreService.restoreFromSnapshot(command.getDataflowId(), dataCollectionId, snapshotId, DatasetTypeEnum.REPORTING, con, processId, command.getJobId());
                LOG.info("Snapshot {} restored for dataCollection {}, dataflowId {}, jobId {}", snapshotId, dataCollectionId, command.getDataflowId(), command.getJobId());
            } catch (Exception e) {
                LOG.error("Error while restoring snapshot for dataCollection {}, dataflowId {}, jobId {}, {}", dataCollectionId, command.getDataflowId(), command.getJobId(), e.getMessage());
                throw e;
            }
            DataRestoredFromSnapshotEvent event = new DataRestoredFromSnapshotEvent();
            BeanUtils.copyProperties(command, event);
            event.getDatasetsReleased().add(datasetId);
            AggregateLifecycle.apply(event, metaData);
        } catch (Exception e) {
            DataRestoredFromSnapshotFailedEvent event = new DataRestoredFromSnapshotFailedEvent();
            BeanUtils.copyProperties(command, event);
            AggregateLifecycle.apply(event);
        }
    }

    @EventSourcingHandler
    public void on(DataRestoredFromSnapshotEvent event) {
        this.datasetsReleased = event.getDatasetsReleased();
    }

    @CommandHandler
    public void handle(CancelReleaseProcessCommand command, MetaData metaData, ProcessService processService) {
        try {
            if (command.getDatasetReleaseProcessId().size() > 0) {
                command.getDatasetReleaseProcessId().values().forEach(processId -> {
                    LOG.info("Updating release process status of process with processId {} to CANCELED for dataflowId {}, dataProviderId {}, jobId {}", processId, command.getDataProviderId(), command.getJobId());
                    ProcessVO processVO = processService.getByProcessId(processId);
                    processService.updateProcess(processVO.getDatasetId(), command.getDataflowId(),
                            ProcessStatusEnum.CANCELED, ProcessTypeEnum.RELEASE, processId, command.getUser(), defaultReleaseProcessPriority, true);
                    LOG.info("Updated release process status of process with processId {} to CANCELED for dataflowId {}, dataProviderId {}, jobId {}", processId, command.getDataProviderId(), command.getJobId());
                });
            }

            ReleaseProcessCancelledEvent event = new ReleaseProcessCancelledEvent();
            BeanUtils.copyProperties(command, event);
            AggregateLifecycle.apply(event, metaData);
        } catch (Exception e) {
            LOG.error("Error while cancelling release processes for dataflowId {}, dataProviderId {}, jobId {}, {}", command.getDataflowId(), command.getDataProviderId(), command.getJobId(), e.getMessage());
            throw e;
        }
    }
}





















