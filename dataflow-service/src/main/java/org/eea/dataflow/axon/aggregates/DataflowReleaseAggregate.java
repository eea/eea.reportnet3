package org.eea.dataflow.axon.aggregates;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.messaging.MetaData;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.spring.stereotype.Aggregate;
import org.eea.axon.release.commands.UpdateInternalRepresentativeCommand;
import org.eea.axon.release.commands.UpdateRepresentativeVisibilityCommand;
import org.eea.axon.release.events.InternalRepresentativeUpdatedEvent;
import org.eea.axon.release.events.RepresentativeVisibilityUpdatedEvent;
import org.eea.dataflow.service.RepresentativeService;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataflow.RepresentativeVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.axonframework.modelling.command.AggregateLifecycle.apply;


@Aggregate
public class DataflowReleaseAggregate {

    @AggregateIdentifier
    private String dataflowReleaseAggregateId;
    private String datasetReleaseAggregateId;
    private String releaseAggregateId;
    private String communicationReleaseAggregateId;
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

    private static final Logger LOG = LoggerFactory.getLogger(DataflowReleaseAggregate.class);

    public DataflowReleaseAggregate() {
    }

    @CommandHandler
    public DataflowReleaseAggregate(UpdateRepresentativeVisibilityCommand command, RepresentativeService representativeService, MetaData metadata) {
        try {
            LOG.info("Updating representative visibility for dataflowId: {} dataProvider: {}", command.getDataflowId(), command.getDataProviderId());
            representativeService.updateRepresentativeVisibilityRestrictions(command.getDataflowId(), command.getDataProviderId(), command.isRestrictFromPublic());
            LOG.info("Updated representative visibility for dataflowId: {} dataProvider: {}", command.getDataflowId(), command.getDataProviderId());
            RepresentativeVisibilityUpdatedEvent event = new RepresentativeVisibilityUpdatedEvent();
            BeanUtils.copyProperties(command, event);
            apply(event, metadata);
        } catch (Exception e) {
            LOG.error("Error while updating representative visibility for dataflowId: {} dataProvider: {}: {}", command.getDataflowId(), command.getDataProviderId(), e.getMessage());
            throw e;
        }
    }

    @EventSourcingHandler
    public void on(RepresentativeVisibilityUpdatedEvent event) {
        this.dataflowReleaseAggregateId = event.getDataflowReleaseAggregateId();
        this.recordStoreReleaseAggregateId = event.getRecordStoreReleaseAggregateId();
        this.collaborationReleaseAggregateId = event.getCollaborationReleaseAggregateId();
        this.communicationReleaseAggregateId = event.getCommunicationReleaseAggregateId();
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
    }

    @CommandHandler
    public void handle(UpdateInternalRepresentativeCommand command, RepresentativeService representativeService, MetaData metaData) throws EEAException {
        try {
            // Mark the receipt button as outdated because a new release has been done, so it would be
            // necessary to generate a new receipt
            List<RepresentativeVO> representatives = null;
            representatives = representativeService.getRepresetativesByIdDataFlow(command.getDataflowId()).stream()
                    .filter(r -> r.getDataProviderId().equals(command.getDataProviderId())).collect(Collectors.toList());
            if (!representatives.isEmpty()) {
                RepresentativeVO representative = representatives.get(0);
                // We only update the representative if the receipt is not outdated
                if (Boolean.FALSE.equals(representative.getReceiptOutdated())) {
                    representative.setReceiptOutdated(true);
                    LOG.info("Mark receipt as outdated: dataflowId={}, providerId={}, representativeId={}", command.getDataflowId(), command.getDataProviderId(), representative.getId());
                    representativeService.updateDataflowRepresentative(representative);
                    LOG.info("Receipt marked as outdated: dataflowId={}, providerId={}, representativeId={}", command.getDataflowId(), command.getDataProviderId(), representative.getId());
                }
            }
            InternalRepresentativeUpdatedEvent event = new InternalRepresentativeUpdatedEvent();
            BeanUtils.copyProperties(command, event);
            apply(event, metaData);
        } catch (Exception e) {
            LOG.error("Error while marking receipt as outdated: dataflowId={}, providerId={}: {}", command.getDataflowId(), command.getDataProviderId(), e.getMessage());
            throw e;
        }
    }

    @EventSourcingHandler
    public void on(InternalRepresentativeUpdatedEvent event) {
        this.dataflowReleaseAggregateId = event.getDataflowReleaseAggregateId();
        this.recordStoreReleaseAggregateId = event.getRecordStoreReleaseAggregateId();
        this.collaborationReleaseAggregateId = event.getCollaborationReleaseAggregateId();
        this.communicationReleaseAggregateId = event.getCommunicationReleaseAggregateId();
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
