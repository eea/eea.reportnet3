package org.eea.recordstore.axon;

import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.eea.axon.release.events.DataRestoredFromSnapshotEvent;
import org.eea.axon.release.events.SnapshotFileForReleaseCreatedEvent;
import org.eea.interfaces.controller.dataset.DatasetSnapshotController.DataSetSnapshotControllerZuul;
import org.eea.interfaces.vo.dataset.enums.DatasetTypeEnum;
import org.eea.interfaces.vo.recordstore.ConnectionDataVO;
import org.eea.recordstore.exception.RecordStoreAccessException;
import org.eea.recordstore.service.RecordStoreService;
import org.eea.utils.LiteralConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Component
@ProcessingGroup("release-group")
public class RecordStoreReleaseEventsHandler {

    private static final Logger LOG = LoggerFactory.getLogger(RecordStoreReleaseEventsHandler.class);

    private RecordStoreService jdbcRecordStoreService;
    private DataSetSnapshotControllerZuul dataSetSnapshotControllerZuul;

    @Autowired
    public RecordStoreReleaseEventsHandler(@Qualifier("jdbcRecordStoreServiceImpl") RecordStoreService jdbcRecordStoreService, DataSetSnapshotControllerZuul dataSetSnapshotControllerZuul) {
        this.jdbcRecordStoreService = jdbcRecordStoreService;
        this.dataSetSnapshotControllerZuul = dataSetSnapshotControllerZuul;
    }

    @EventHandler
    public void on(SnapshotFileForReleaseCreatedEvent event) throws SQLException, IOException, InterruptedException {
        for (Long id : event.getDatasetIds()) {
            LOG.info("Creating snapshot file for dataflowId: {} dataProvider: {} dataset: {}", event.getDataflowId(), event.getDataProviderId(), id);
            Long partitionId = dataSetSnapshotControllerZuul.obtainPartition(id, "root");
            Long snapshotId = dataSetSnapshotControllerZuul.findSnapshotIdByReportingDataset(id);
            jdbcRecordStoreService.createDataSnapshotForRelease(id, snapshotId, partitionId, false);
            LOG.info("Created snapshot file for snapshot for dataflowId: {} dataProvider: {} dataset: {}", snapshotId, event.getDataflowId(), event.getDataProviderId(), id);
        }
    }

    @EventHandler
    public void on(DataRestoredFromSnapshotEvent event) throws SQLException, RecordStoreAccessException, IOException {
        for (Long id : event.getDatasetIds()) {
            Long snapshotId = dataSetSnapshotControllerZuul.findSnapshotIdByReportingDataset(id);
            Long dataCollectionId = dataSetSnapshotControllerZuul.findDataCollectionIdByIdSnapshotId(snapshotId);
            LOG.info("Restoring snapshot {} for dataCollection {} of dataflow {}", snapshotId, dataCollectionId, event.getDataflowId());
            ConnectionDataVO datasetConnection = jdbcRecordStoreService.getConnectionDataForDataset(LiteralConstants.DATASET_PREFIX + dataCollectionId);
            try (Connection con = DriverManager.getConnection(datasetConnection.getConnectionString(),
                    datasetConnection.getUser(), datasetConnection.getPassword())) {
                con.setAutoCommit(true);
                jdbcRecordStoreService.restoreFromSnapshot(dataCollectionId, snapshotId, DatasetTypeEnum.REPORTING, con);
                LOG.info("Snapshot {} restored for dataCollection {} of dataflow {}", snapshotId, dataCollectionId, event.getDataflowId());
            } catch (Exception e) {

            }
        }
    }
}





















