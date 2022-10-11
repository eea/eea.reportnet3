package org.eea.dataflow.axon.release;

import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.eea.axon.release.events.RepresentativeVisibilityUpdatedEvent;
import org.eea.dataflow.service.RepresentativeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@ProcessingGroup("dataflow-release-group")
public class DataflowReleaseEventsHandler {

    private static final Logger LOG = LoggerFactory.getLogger(DataflowReleaseEventsHandler.class);

    @Autowired
    RepresentativeService representativeService;

    @EventHandler
    public void on(RepresentativeVisibilityUpdatedEvent event) {
        LOG.info("Updating representative visibility for dataflowId: {} dataProvider: {}", event.getDataflowId(), event.getDataProviderId());
        representativeService.updateRepresentativeVisibilityRestrictions(event.getDataflowId(), event.getDataProviderId(), event.isRestrictFromPublic());
        LOG.info("Updated representative visibility for dataflowId: {} dataProvider: {}", event.getDataflowId(), event.getDataProviderId());
    }
}
