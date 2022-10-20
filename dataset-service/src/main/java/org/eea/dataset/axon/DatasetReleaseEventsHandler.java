package org.eea.dataset.axon.handler;

import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.messaging.MetaData;
import org.eea.axon.release.events.*;
import org.eea.dataset.persistence.data.repository.ValidationRepository;
import org.eea.dataset.persistence.metabase.domain.DataCollection;
import org.eea.dataset.persistence.metabase.domain.DataSetMetabase;
import org.eea.dataset.persistence.metabase.domain.Snapshot;
import org.eea.dataset.persistence.metabase.repository.DataCollectionRepository;
import org.eea.dataset.persistence.metabase.repository.DataSetMetabaseRepository;
import org.eea.dataset.persistence.metabase.repository.SnapshotRepository;
import org.eea.dataset.service.DatasetSnapshotService;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.CreateSnapshotVO;
import org.eea.interfaces.vo.dataset.enums.DatasetRunningStatusEnum;
import org.eea.interfaces.vo.dataset.enums.ErrorTypeEnum;
import org.eea.multitenancy.TenantResolver;
import org.eea.security.jwt.utils.EeaUserDetails;
import org.eea.utils.LiteralConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

@Component
@ProcessingGroup("release-group")
public class DatasetReleaseEventsHandler {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(DatasetReleaseEventsHandler.class);

    private DatasetSnapshotService datasetSnapshotService;
    private ValidationRepository validationRepository;
    private DataSetMetabaseRepository dataSetMetabaseRepository;
    private DataCollectionRepository dataCollectionRepository;
    private SnapshotRepository snapshotRepository;

    @Autowired
    public DatasetReleaseEventsHandler(DatasetSnapshotService datasetSnapshotService, ValidationRepository validationRepository, DataSetMetabaseRepository dataSetMetabaseRepository,
                                       DataCollectionRepository dataCollectionRepository, SnapshotRepository snapshotRepository) {
        this.datasetSnapshotService = datasetSnapshotService;
        this.validationRepository = validationRepository;
        this.dataSetMetabaseRepository = dataSetMetabaseRepository;
        this.dataCollectionRepository = dataCollectionRepository;
        this.snapshotRepository = snapshotRepository;
    }

    @ExceptionHandler(value=EEAException.class)
    public void handle(EEAException eeaException) throws EEAException {
        throw eeaException;
    }

    @EventHandler
    public void on(ReleaseLocksAddedEvent event, MetaData metaData) throws EEAException, InterruptedException {
        LOG.info("Adding release locks for DataflowId: {} DataProviderId: {}", event.getDataflowId(), event.getDataProviderId());
        LinkedHashMap auth = (LinkedHashMap) metaData.get("auth");
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                EeaUserDetails.create(auth.get("name").toString(), new HashSet<>()), auth.get("credentials"), null));
        // Lock all the operations related to the datasets involved
        datasetSnapshotService.addLocksRelatedToRelease(event.getDatasetIds(), event.getDataflowId());
        LOG.info("Release locks added for DataflowId: {} DataProviderId: {}", event.getDataflowId(), event.getDataProviderId());
//        if (true) {
//            throw new EEAException();
//        }
    }

    @EventHandler
    public void on(SnapshotRecordForReleaseCreatedInMetabaseEvent event) {
        boolean haveBlockers = false;
        for (Long id : event.getDatasetIds()) {
            setTenant(id);
            if (validationRepository.existsByLevelError(ErrorTypeEnum.BLOCKER)) {
                haveBlockers = true;

                //TO DO

                LOG.error(
                        "Error in the releasing process of the dataflowId {} and dataProviderId {}, the datasets have blocker errors",
                        event.getDataflowId(), event.getDataProviderId());
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

            for (Long id : event.getDatasetIds()) {
                LOG.info("Creating snapshot record for dataflowId: {} dataProvider: {} dataset: {}", event.getDataflowId(), event.getDataProviderId(), id);
                datasetSnapshotService.createSnapshotInMetabase(id, createSnapshotVO);
                LOG.info("Snapshot record created in metabase for dataflowId: {} dataProvider: {} dataset: {}", event.getDataflowId(), event.getDataProviderId(), id);
            }
        }
    }

    /**
     * Sets the tenant.
     *
     * @param idDataset the new tenant
     */
    private void setTenant(Long idDataset) {
        TenantResolver.setTenantName(String.format(LiteralConstants.DATASET_FORMAT_NAME, idDataset));
    }

    @EventHandler
    public void on(DatasetStatusUpdatedEvent event) {
        event.getDatasetIds().stream().forEach(id -> {
            LOG.info("Updating dataset status for dataflowId: {} dataProvider: {} dataset {}", event.getDataflowId(), event.getDataProviderId(), id);
            datasetSnapshotService.updateDatasetStatus(id);
            LOG.info("Status updated for dataflowId: {} dataProvider: {} dataset {}", event.getDataflowId(), event.getDataProviderId(), id);
        });
    }

    @EventHandler
    public void on(ProviderDeletedEvent event) {
        event.getDatasetIds().stream().forEach(id -> {
            LOG.info("Deleting provider for dataflowId: {} dataProvider: {} dataset {}", event.getDataflowId(), event.getDataProviderId(), id);
            datasetSnapshotService.deleteProvider(id, event.getDataProviderId());
            LOG.info("Provider deleted for dataflowId: {} dataProvider: {} dataset {}", event.getDataflowId(), event.getDataProviderId(), id);
        });
    }

    @EventHandler
    public void on(DatasetRunningStatusUpdatedEvent event) {
        event.getDatasetIds().stream().forEach(id -> {
            DataSetMetabase dataset = dataSetMetabaseRepository.findById(id).orElse(null);
            String datasetSchema = null;
            if (dataset != null) {
                datasetSchema = dataset.getDatasetSchema();
            }
            Optional<DataCollection> dataCollection =
                    dataCollectionRepository.findFirstByDatasetSchema(datasetSchema);
            Long idDataCollection = dataCollection.isPresent() ? dataCollection.get().getId() : null;
            try {
                DataSetMetabase datasetMetabase = dataSetMetabaseRepository.findById(idDataCollection).orElse(null);
                LOG.info("Updating dataset running status to {} for datasetId {} of dataflow {} and dataProvider {}.", DatasetRunningStatusEnum.RESTORING_SNAPSHOT,
                        id, event.getDataflowId(), event.getDataProviderId());
                if (datasetMetabase != null) {
                    datasetMetabase.setDatasetRunningStatus(DatasetRunningStatusEnum.RESTORING_SNAPSHOT);
                    dataSetMetabaseRepository.save(datasetMetabase);
                    LOG.info("Updated dataset running status to {} for datasetId {} of dataflow {} and dataProvider {}.", DatasetRunningStatusEnum.RESTORING_SNAPSHOT,
                            id, event.getDataflowId(), event.getDataProviderId());
                }
            } catch (Exception e) {

            }
        });
    }

    @EventHandler
    public void on(SnapshotMarkedReleasedEvent event) {
        for (Long id : event.getDatasetIds()) {
            Long snapshotId = datasetSnapshotService.findFirstByReportingDatasetId(id);
            LOG.info("Mark snapshot with id {} released for dataflow {}, dataProvider {}, dataset {}", snapshotId, event.getDataflowId(), event.getDataProviderId(), id);
            // Mark the snapshot released
            snapshotRepository.releaseSnaphot(id, snapshotId);
            // Add the date of the release
            Optional<Snapshot> snapshot = snapshotRepository.findById(snapshotId);
            if (snapshot.isPresent()) {
                Date dateRelease = java.sql.Timestamp.valueOf(LocalDateTime.now());
                snapshot.get().setDateReleased(dateRelease);
                snapshotRepository.save(snapshot.get());
            }
            LOG.info("Snapshot with id {} marked released for dataflow {}, dataProvider {}, dataset {}", snapshotId, event.getDataflowId(), event.getDataProviderId(), id);
        }
    }
}









