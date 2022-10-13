package org.eea.dataflow.axon;

import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.eea.axon.release.events.InternalRepresentativeUpdatedEvent;
import org.eea.axon.release.events.RepresentativeVisibilityUpdatedEvent;
import org.eea.dataflow.service.RepresentativeService;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataflow.RepresentativeVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@ProcessingGroup("release-group")
public class DataflowReleaseEventsHandler {

    private static final Logger LOG = LoggerFactory.getLogger(DataflowReleaseEventsHandler.class);

    private RepresentativeService representativeService;

    @Autowired
    public DataflowReleaseEventsHandler(RepresentativeService representativeService) {
        this.representativeService = representativeService;
    }

    @EventHandler
    public void on(RepresentativeVisibilityUpdatedEvent event) {
        LOG.info("Updating representative visibility for dataflowId: {} dataProvider: {}", event.getDataflowId(), event.getDataProviderId());
        representativeService.updateRepresentativeVisibilityRestrictions(event.getDataflowId(), event.getDataProviderId(), event.isRestrictFromPublic());
        LOG.info("Updated representative visibility for dataflowId: {} dataProvider: {}", event.getDataflowId(), event.getDataProviderId());
    }

    @EventHandler
    public void on(InternalRepresentativeUpdatedEvent event) throws EEAException {
        // Mark the receipt button as outdated because a new release has been done, so it would be
        // necessary to generate a new receipt
        List<RepresentativeVO> representatives = null;
        representatives = representativeService.getRepresetativesByIdDataFlow(event.getDataflowId()).stream()
                .filter(r -> r.getDataProviderId().equals(event.getDataProviderId())).collect(Collectors.toList());
        if (!representatives.isEmpty()) {
            RepresentativeVO representative = representatives.get(0);
            // We only update the representative if the receipt is not outdated
            if (Boolean.FALSE.equals(representative.getReceiptOutdated())) {
                representative.setReceiptOutdated(true);
                LOG.info(
                        "Mark receipt as outdated: dataflowId={}, providerId={}, representativeId={}",
                        event.getDataflowId(), event.getDataProviderId(), representative.getId());
                representativeService.updateDataflowRepresentative(representative);
                LOG.info(
                        "Receipt marked as outdated: dataflowId={}, providerId={}, representativeId={}",
                        event.getDataflowId(), event.getDataProviderId(), representative.getId());
            }
        }
    }
}
