package org.eea.validation.axon;

import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.messaging.MetaData;
import org.eea.axon.release.events.ValidationProcessForReleaseAddedEvent;
import org.eea.interfaces.controller.dataset.DatasetController.DataSetControllerZuul;
import org.eea.interfaces.controller.validation.ValidationController;
import org.eea.security.jwt.utils.EeaUserDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;

@Component
@ProcessingGroup("release-group")
public class ValidationReleaseEventsHandler {

    private static final Logger LOG = LoggerFactory.getLogger(ValidationReleaseEventsHandler.class);

    @Qualifier("validationControllerImpl")
    @Autowired
    ValidationController validationController;

    @Autowired
    DataSetControllerZuul dataSetControllerZuul;

    @EventHandler
    public void on(ValidationProcessForReleaseAddedEvent event, MetaData metaData) {
        LinkedHashMap auth = (LinkedHashMap) metaData.get("auth");
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                EeaUserDetails.create(auth.get("name").toString(), new HashSet<>()), auth.get("credentials"), null));
        List<Long> datasetIds = dataSetControllerZuul.findDatasetIdsByDataflowId(event.getDataflowId(), event.getDataProviderId());
        datasetIds.forEach(datasetId -> {
            LOG.info("Adding validation process for dataflowId: {} dataProvider: {} dataset ", event.getDataflowId(), event.getDataProviderId(), datasetId);
            validationController.validateDataSetData(datasetId, true);
        });
    }

}
