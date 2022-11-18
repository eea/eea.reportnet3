package org.eea.orchestrator.controller;

import org.axonframework.commandhandling.GenericCommandMessage;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.messaging.MetaData;
import org.eea.axon.release.commands.CreateReleaseStartNotificationCommand;
import org.eea.interfaces.controller.orchestrator.OrchestratorController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(value = "/processes")
public class OrchestratorControllerImpl implements OrchestratorController {

    private transient CommandGateway commandGateway;
    private static final Logger LOG = LoggerFactory.getLogger(OrchestratorControllerImpl.class);

    @Autowired
    public OrchestratorControllerImpl(CommandGateway commandGateway) {
        this.commandGateway = commandGateway;
    }

    @Override
    @PreAuthorize("secondLevelAuthorize(#dataflowId,'DATAFLOW_LEAD_REPORTER') OR hasAnyRole('ADMIN')")
    @PostMapping(value = "/release/dataflow/{dataflowId}/dataProvider/{dataProviderId}")
    public void release(@PathVariable(value = "dataflowId", required = true) Long dataflowId, @PathVariable(value = "dataProviderId",
            required = true) Long dataProviderId, @RequestParam(name = "restrictFromPublic", required = true,
            defaultValue = "false") boolean restrictFromPublic,
                        @RequestParam(name = "validate", required = false, defaultValue = "true") boolean validate) {
       try {
           Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
           CreateReleaseStartNotificationCommand command = CreateReleaseStartNotificationCommand.builder().transactionId(UUID.randomUUID().toString()).releaseAggregateId(UUID.randomUUID().toString())
                   .dataflowId(dataflowId).dataProviderId(dataProviderId).restrictFromPublic(restrictFromPublic)
                   .validate(validate).build();
           commandGateway.send(GenericCommandMessage.asCommandMessage(command).withMetaData(MetaData.with("auth", authentication)));
       } catch (Exception e) {
            LOG.error("An error occurred while releasing dataflow {}, dataProvider {}", dataflowId, dataProviderId);
           throw e;
       }
    }
}
