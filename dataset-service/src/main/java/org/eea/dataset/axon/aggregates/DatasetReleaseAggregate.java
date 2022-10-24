package org.eea.dataset.axon.aggregates;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.messaging.MetaData;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.spring.stereotype.Aggregate;
import org.eea.axon.release.commands.AddReleaseLocksCommand;
import org.eea.axon.release.events.ReleaseLocksAddedEvent;
import org.eea.dataset.persistence.data.repository.ValidationRepository;
import org.eea.dataset.persistence.metabase.domain.ReportingDataset;
import org.eea.dataset.persistence.metabase.repository.DataCollectionRepository;
import org.eea.dataset.persistence.metabase.repository.DataSetMetabaseRepository;
import org.eea.dataset.persistence.metabase.repository.ReportingDatasetRepository;
import org.eea.dataset.persistence.metabase.repository.SnapshotRepository;
import org.eea.dataset.service.DatasetSnapshotService;
import org.eea.exception.EEAException;
import org.eea.security.jwt.utils.EeaUserDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

import static org.axonframework.modelling.command.AggregateLifecycle.apply;

@Component
@Aggregate
public class DatasetReleaseAggregate {

    @AggregateIdentifier
    private String datasetReleaseAggregate;
    private String transactionId;
    private Long dataProviderId;
    private Long dataflowId;
    private boolean restrictFromPublic;
    private boolean validate;
    private List<Long> datasetIds;

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(DatasetReleaseAggregate.class);

//    private ValidationRepository validationRepository;
//    private DataSetMetabaseRepository dataSetMetabaseRepository;
//    private DataCollectionRepository dataCollectionRepository;
//    private SnapshotRepository snapshotRepository;


    public DatasetReleaseAggregate() {
    }

    @CommandHandler
    public DatasetReleaseAggregate(AddReleaseLocksCommand command, MetaData metaData, DatasetSnapshotService datasetSnapshotService, ReportingDatasetRepository reportingDatasetRepository) throws EEAException, InterruptedException {
        LOG.info("Adding release locks for DataflowId: {} DataProviderId: {}", command.getDataflowId(), command.getDataProviderId());
        LinkedHashMap auth = (LinkedHashMap) metaData.get("auth");
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                EeaUserDetails.create(auth.get("name").toString(), new HashSet<>()), auth.get("credentials"), null));
        // Lock all the operations related to the datasets involved
        List<Long> datasetIds = reportingDatasetRepository.findByDataflowId(command.getDataflowId()).stream().filter(rd -> rd.getDataProviderId().equals(command.getDataProviderId())).map(ReportingDataset::getId)
                .distinct().collect(Collectors.toList());
        command.setDatasetIds(datasetIds);
//        datasetSnapshotService.addLocksRelatedToRelease(datasetIds, command.getDataflowId());
        LOG.info("Release locks added for DataflowId: {} DataProviderId: {}", command.getDataflowId(), command.getDataProviderId());
//        if (true) {
//            throw new EEAException();
//        }
        ReleaseLocksAddedEvent event = new ReleaseLocksAddedEvent();
        BeanUtils.copyProperties(command, event);
        apply(event);
    }

    @EventSourcingHandler
    public void on(ReleaseLocksAddedEvent event) {
        this.datasetReleaseAggregate = event.getDatasetReleaseAggregate();
        this.transactionId = event.getTransactionId();
        this.dataflowId = event.getDataflowId();
        this.dataProviderId = event.getDataProviderId();
        this.restrictFromPublic = event.isRestrictFromPublic();
        this.validate = event.isValidate();
        this.datasetIds = event.getDatasetIds();
    }

//    @CommandHandler
//    public void handle(CreateSnapshotRecordRorReleaseInMetabaseCommand event, DatasetSnapshotService datasetSnapshotService) {
//        boolean haveBlockers = false;
//        for (Long id : event.getDatasetIds()) {
//            setTenant(id);
//            if (validationRepository.existsByLevelError(ErrorTypeEnum.BLOCKER)) {
//                haveBlockers = true;
//
//                //TO DO
//
//                LOG.error(
//                        "Error in the releasing process of the dataflowId {} and dataProviderId {}, the datasets have blocker errors",
//                        event.getDataflowId(), event.getDataProviderId());
//                break;
//            }
//        }
//
//        if (!haveBlockers) {
//            CreateSnapshotVO createSnapshotVO = new CreateSnapshotVO();
//            createSnapshotVO.setReleased(true);
//            createSnapshotVO.setAutomatic(Boolean.TRUE);
//            TimeZone.setDefault(TimeZone.getTimeZone("CET"));
//            Date date = new Date();
//            SimpleDateFormat formateador = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//            createSnapshotVO.setDescription("Release " + formateador.format(date) + " CET");
//
//            for (Long id : event.getDatasetIds()) {
//                LOG.info("Creating snapshot record for dataflowId: {} dataProvider: {} dataset: {}", event.getDataflowId(), event.getDataProviderId(), id);
//                datasetSnapshotService.createSnapshotInMetabase(id, createSnapshotVO);
//                LOG.info("Snapshot record created in metabase for dataflowId: {} dataProvider: {} dataset: {}", event.getDataflowId(), event.getDataProviderId(), id);
//            }
//        }
//    }
//
//    /**
//     * Sets the tenant.
//     *
//     * @param idDataset the new tenant
//     */
//    private void setTenant(Long idDataset) {
//        TenantResolver.setTenantName(String.format(LiteralConstants.DATASET_FORMAT_NAME, idDataset));
//    }
//
//    @CommandHandler
//    public void handle(UpdateDatasetStatusCommand event, DatasetSnapshotService datasetSnapshotService) {
//        event.getDatasetIds().stream().forEach(id -> {
//            LOG.info("Updating dataset status for dataflowId: {} dataProvider: {} dataset {}", event.getDataflowId(), event.getDataProviderId(), id);
//            datasetSnapshotService.updateDatasetStatus(id);
//            LOG.info("Status updated for dataflowId: {} dataProvider: {} dataset {}", event.getDataflowId(), event.getDataProviderId(), id);
//        });
//    }
//
//    @CommandHandler
//    public void handle(DeleteProviderCommand event, DatasetSnapshotService datasetSnapshotService) {
//        event.getDatasetIds().stream().forEach(id -> {
//            LOG.info("Deleting provider for dataflowId: {} dataProvider: {} dataset {}", event.getDataflowId(), event.getDataProviderId(), id);
//            datasetSnapshotService.deleteProvider(id, event.getDataProviderId());
//            LOG.info("Provider deleted for dataflowId: {} dataProvider: {} dataset {}", event.getDataflowId(), event.getDataProviderId(), id);
//        });
//    }
//
//    @CommandHandler
//    public void handle(DatasetRunningStatusUpdatedEvent event) {
//        event.getDatasetIds().stream().forEach(id -> {
//            DataSetMetabase dataset = dataSetMetabaseRepository.findById(id).orElse(null);
//            String datasetSchema = null;
//            if (dataset != null) {
//                datasetSchema = dataset.getDatasetSchema();
//            }
//            Optional<DataCollection> dataCollection =
//                    dataCollectionRepository.findFirstByDatasetSchema(datasetSchema);
//            Long idDataCollection = dataCollection.isPresent() ? dataCollection.get().getId() : null;
//            try {
//                DataSetMetabase datasetMetabase = dataSetMetabaseRepository.findById(idDataCollection).orElse(null);
//                LOG.info("Updating dataset running status to {} for datasetId {} of dataflow {} and dataProvider {}.", DatasetRunningStatusEnum.RESTORING_SNAPSHOT,
//                        id, event.getDataflowId(), event.getDataProviderId());
//                if (datasetMetabase != null) {
//                    datasetMetabase.setDatasetRunningStatus(DatasetRunningStatusEnum.RESTORING_SNAPSHOT);
//                    dataSetMetabaseRepository.save(datasetMetabase);
//                    LOG.info("Updated dataset running status to {} for datasetId {} of dataflow {} and dataProvider {}.", DatasetRunningStatusEnum.RESTORING_SNAPSHOT,
//                            id, event.getDataflowId(), event.getDataProviderId());
//                }
//            } catch (Exception e) {
//
//            }
//        });
//    }
//
//    @CommandHandler
//    public void handle(MarkSnapshotReleasedCommand event, DatasetSnapshotService datasetSnapshotService) {
//        for (Long id : event.getDatasetIds()) {
//            Long snapshotId = datasetSnapshotService.findFirstByReportingDatasetId(id);
//            LOG.info("Mark snapshot with id {} released for dataflow {}, dataProvider {}, dataset {}", snapshotId, event.getDataflowId(), event.getDataProviderId(), id);
//            // Mark the snapshot released
//            snapshotRepository.releaseSnaphot(id, snapshotId);
//            // Add the date of the release
//            Optional<Snapshot> snapshot = snapshotRepository.findById(snapshotId);
//            if (snapshot.isPresent()) {
//                Date dateRelease = java.sql.Timestamp.valueOf(LocalDateTime.now());
//                snapshot.get().setDateReleased(dateRelease);
//                snapshotRepository.save(snapshot.get());
//            }
//            LOG.info("Snapshot with id {} marked released for dataflow {}, dataProvider {}, dataset {}", snapshotId, event.getDataflowId(), event.getDataProviderId(), id);
//        }
//    }
}









