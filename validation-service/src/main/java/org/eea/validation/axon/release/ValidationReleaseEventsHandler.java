package org.eea.validation.axon.release;

import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.eea.axon.release.events.ValidationProcessForReleaseAddedEvent;
import org.eea.interfaces.controller.dataset.DatasetController.DataSetControllerZuul;
import org.eea.interfaces.controller.validation.ValidationController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ProcessingGroup("validation-release-group")
public class ValidationReleaseEventsHandler {

    private static final Logger LOG = LoggerFactory.getLogger(ValidationReleaseEventsHandler.class);

    @Qualifier("validationControllerImpl")
    @Autowired
    ValidationController validationController;

    @Autowired
    DataSetControllerZuul dataSetControllerZuul;

    @EventHandler
    public void on(ValidationProcessForReleaseAddedEvent event) {
        List<Long> datasetIds = dataSetControllerZuul.findDatasetIdsByDataflowId(event.getDataflowId(), event.getDataProviderId());
        datasetIds.forEach(datasetId -> {
            LOG.info("Adding validation process for dataflowId: {} dataProvider: {} dataset ", event.getDataflowId(), event.getDataProviderId(), datasetId);
            validationController.validateDataSetData(datasetId, true);
        });
    }

}
