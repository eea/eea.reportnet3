package org.eea.recordstore.axon.release;

import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.eea.axon.release.events.SnapshotFileForReleaseCreatedEvent;
import org.eea.interfaces.controller.dataset.DatasetController.DataSetControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetSnapshotController.DataSetSnapshotControllerZuul;
import org.eea.recordstore.service.RecordStoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;

@Component
@ProcessingGroup("recordstore-release-group")
public class RecordStoreReleaseEventsHandler {

    private static final Logger LOG = LoggerFactory.getLogger(RecordStoreReleaseEventsHandler.class);

    @Autowired
    @Qualifier("jdbcRecordStoreServiceImpl")
    RecordStoreService jdbcRecordStoreService;

    @Autowired
    DataSetSnapshotControllerZuul dataSetSnapshotControllerZuul;

    @Autowired
    DataSetControllerZuul dataSetControllerZuul;


    @EventHandler
    public void on(SnapshotFileForReleaseCreatedEvent event) throws SQLException, IOException, InterruptedException {
        for (Long id : event.getDatasetIds()) {
            LOG.info("Creating snapshot file for dataflowId: {} dataProvider: {} dataset: {}", event.getDataflowId(), event.getDataProviderId(), id);
            Long partitionId = dataSetSnapshotControllerZuul.obtainPartition(id, "root");
            Date dateRelease = Timestamp.valueOf(LocalDateTime.now());
            jdbcRecordStoreService.createDataSnapshotForRelease(id, event.getDatasetSnapshots().get(id), partitionId, dateRelease.toString(), false);
            LOG.info("Created snapshot file for dataflowId: {} dataProvider: {} dataset: {}", event.getDataflowId(), event.getDataProviderId(), id);
        }
    }
}





















