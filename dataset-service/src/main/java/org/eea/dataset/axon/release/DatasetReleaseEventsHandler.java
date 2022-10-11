package org.eea.dataset.axon.release;

import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.messaging.MetaData;
import org.eea.axon.release.events.*;
import org.eea.dataset.persistence.data.repository.ValidationRepository;
import org.eea.dataset.persistence.metabase.repository.ReportingDatasetRepository;
import org.eea.dataset.service.DatasetSnapshotService;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.recordstore.RecordStoreController.RecordStoreControllerZuul;
import org.eea.interfaces.vo.dataset.CreateSnapshotVO;
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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.TimeZone;

@Component
@ProcessingGroup("dataset-release-group")
public class DatasetReleaseEventsHandler {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(DatasetReleaseEventsHandler.class);

    @Autowired
    ReportingDatasetRepository reportingDatasetRepository;

    @Autowired
    DatasetSnapshotService datasetSnapshotService;

    @Autowired
    RecordStoreControllerZuul recordStoreControllerZuul;

    @Autowired
    ValidationRepository validationRepository;


    @EventHandler
    public void on(ReleaseLocksAddedEvent event, MetaData metaData) throws EEAException {
        LOG.info("Adding release locks for DataflowId: {} DataProviderId: {}", event.getDataflowId(), event.getDataProviderId());
        LinkedHashMap auth = (LinkedHashMap) metaData.get("auth");
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                EeaUserDetails.create(auth.get("name").toString(), new HashSet<>()), auth.get("credentials"), null));
        // Lock all the operations related to the datasets involved
        datasetSnapshotService.addLocksRelatedToRelease(event.getDatasetIds(), event.getDataflowId());
        LOG.info("Release locks added for DataflowId: {} DataProviderId: {}", event.getDataflowId(), event.getDataProviderId());
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
            LOG.info("Updating dataset status for dataflowId: {} dataProvider: {} dataset ", event.getDataflowId(), event.getDataProviderId(), id);
            datasetSnapshotService.updateDatasetStatus(id);
            LOG.info("Status updated for dataflowId: {} dataProvider: {} dataset ", event.getDataflowId(), event.getDataProviderId(), id);
        });
    }

    @EventHandler
    public void on(ProviderDeletedEvent event) {
        event.getDatasetIds().stream().forEach(id -> {
            LOG.info("Deleting provider for dataflowId: {} dataProvider: {} dataset ", event.getDataflowId(), event.getDataProviderId(), id);
            datasetSnapshotService.deleteProvider(id, event.getDataProviderId());
        });
    }

    @EventHandler
    public void on(InternalRepresentativeUpdatedEvent event) {
        event.getDatasetIds().stream().forEach(id -> {
            LOG.info("Updating internal representative for dataflowId: {} dataProvider: {} dataset ", event.getDataflowId(), event.getDataProviderId(), id);
            datasetSnapshotService.updateInternalRepresentative(id, event.getDataProviderId(), event.getDataflowId());
            LOG.info("Internal representative updated for dataflowId: {} dataProvider: {} dataset ", event.getDataflowId(), event.getDataProviderId(), id);
        });
    }
}
