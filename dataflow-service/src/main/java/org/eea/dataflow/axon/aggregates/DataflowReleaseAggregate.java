package org.eea.dataflow.axon.aggregates;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.messaging.MetaData;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.spring.stereotype.Aggregate;
import org.eea.axon.release.commands.RevertInternalRepresentativeCommand;
import org.eea.axon.release.commands.RevertRepresentativeVisibilityCommand;
import org.eea.axon.release.commands.UpdateInternalRepresentativeCommand;
import org.eea.axon.release.commands.UpdateRepresentativeVisibilityCommand;
import org.eea.axon.release.events.InternalRepresentativeRevertedEvent;
import org.eea.axon.release.events.InternalRepresentativeUpdatedEvent;
import org.eea.axon.release.events.RepresentativeVisibilityRevertedEvent;
import org.eea.axon.release.events.RepresentativeVisibilityUpdatedEvent;
import org.eea.dataflow.service.RepresentativeService;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataflow.RepresentativeVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

import java.util.List;
import java.util.stream.Collectors;

import static org.axonframework.modelling.command.AggregateLifecycle.apply;


@Aggregate
public class DataflowReleaseAggregate {

    @AggregateIdentifier
    private String dataflowReleaseAggregateId;

    private static final Logger LOG = LoggerFactory.getLogger(DataflowReleaseAggregate.class);

    public DataflowReleaseAggregate() {
    }

    @CommandHandler
    public DataflowReleaseAggregate(UpdateRepresentativeVisibilityCommand command, RepresentativeService representativeService, MetaData metadata) {
        try {
            LOG.info("Updating representative visibility for dataflowId {}, dataProvider {}, jobId {}", command.getDataflowId(), command.getDataProviderId(), command.getJobId());
            representativeService.updateRepresentativeVisibilityRestrictions(command.getDataflowId(), command.getDataProviderId(), command.isRestrictFromPublic());
            LOG.info("Updated representative visibility for dataflowId {}, dataProvider {}, jobId {}", command.getDataflowId(), command.getDataProviderId(), command.getJobId());
            RepresentativeVisibilityUpdatedEvent event = new RepresentativeVisibilityUpdatedEvent();
            BeanUtils.copyProperties(command, event);
            apply(event, metadata);
        } catch (Exception e) {
            LOG.error("Error while updating representative visibility for dataflowId {}, dataProvider {}, jobId {}, {}", command.getDataflowId(), command.getDataProviderId(), command.getJobId(), e.getMessage());
            throw e;
        }
    }

    @EventSourcingHandler
    public void on(RepresentativeVisibilityUpdatedEvent event) {
        this.dataflowReleaseAggregateId = event.getDataflowReleaseAggregateId();
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
                    LOG.info("Mark receipt as outdated: dataflowId {}, dataProviderId {}, representativeId {}, jobId {}", command.getDataflowId(), command.getDataProviderId(), representative.getId(), command.getJobId());
                    representativeService.updateDataflowRepresentative(representative);
                    LOG.info("Receipt marked as outdated: dataflowId {}, dataProviderId {}, representativeId {}, jobId {}", command.getDataflowId(), command.getDataProviderId(), representative.getId(), command.getJobId());
                }
            }
            InternalRepresentativeUpdatedEvent event = new InternalRepresentativeUpdatedEvent();
            BeanUtils.copyProperties(command, event);
            apply(event, metaData);
        } catch (Exception e) {
            LOG.error("Error while marking receipt as outdated: dataflowId {}, dataProviderId {}, jobId {}, {}", command.getDataflowId(), command.getDataProviderId(), command.getJobId(), e.getMessage());
            throw e;
        }
    }

    @CommandHandler
    public void handle(RevertRepresentativeVisibilityCommand command, MetaData metaData, RepresentativeService representativeService) {
        try {
            LOG.info("Reverting representative visibility for dataflowId {}, dataProviderId {}, jobId {}", command.getDataflowId(), command.getDataProviderId(), command.getJobId());
            representativeService.updateRepresentativeVisibilityRestrictions(command.getDataflowId(), command.getDataProviderId(), false);
            LOG.info("Reverted representative visibility for dataflowId {}, dataProviderId {}, jobId {}", command.getDataflowId(), command.getDataProviderId(), command.getJobId());
            RepresentativeVisibilityRevertedEvent event = new RepresentativeVisibilityRevertedEvent();
            BeanUtils.copyProperties(command, event);
            apply(event, metaData);
        } catch (Exception e) {
            LOG.error("Error Reverting representative visibility for dataflowId {}, dataProviderId {}, jobId {}", command.getDataflowId(), command.getDataProviderId(), command.getJobId());
        }
    }

    @CommandHandler
    public void handle(RevertInternalRepresentativeCommand command, MetaData metaData, RepresentativeService representativeService) throws EEAException {
        try {
            // Mark the receipt button as outdated because a new release has been done, so it would be
            // necessary to generate a new receipt
            List<RepresentativeVO> representatives = null;
            representatives = representativeService.getRepresetativesByIdDataFlow(command.getDataflowId()).stream()
                    .filter(r -> r.getDataProviderId().equals(command.getDataProviderId())).collect(Collectors.toList());
            if (!representatives.isEmpty()) {
                RepresentativeVO representative = representatives.get(0);
                representative.setReceiptOutdated(false);
                LOG.info("Revert outdated receipt: dataflowId={}, providerId={}, representativeId={}", command.getDataflowId(), command.getDataProviderId(), representative.getId());
                representativeService.updateDataflowRepresentative(representative);
                LOG.info("Receipt outdated reverted: dataflowId={}, providerId={}, representativeId={}", command.getDataflowId(), command.getDataProviderId(), representative.getId());
            }
            InternalRepresentativeRevertedEvent event = new InternalRepresentativeRevertedEvent();
            BeanUtils.copyProperties(command, event);
            apply(event, metaData);
        } catch (Exception e) {
            LOG.error("Error while reverting outdated receipt: dataflowId={}, providerId={}: {}", command.getDataflowId(), command.getDataProviderId(), e.getMessage());
            throw e;
        }
    }

}




