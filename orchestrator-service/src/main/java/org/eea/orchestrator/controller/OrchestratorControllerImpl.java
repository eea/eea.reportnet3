package org.eea.orchestrator.controller;

import org.axonframework.commandhandling.GenericCommandMessage;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.messaging.MetaData;
import org.eea.axon.release.commands.CreateReleaseStartNotificationCommand;
import org.eea.interfaces.controller.orchestrator.OrchestratorController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping(value = "/orchestrator")
public class OrchestratorControllerImpl implements OrchestratorController {

    private transient CommandGateway commandGateway;

    @Autowired
    public OrchestratorControllerImpl(CommandGateway commandGateway) {
        this.commandGateway = commandGateway;
    }

    @Override
    @PreAuthorize("secondLevelAuthorize(#dataflowId,'DATAFLOW_LEAD_REPORTER') OR hasAnyRole('ADMIN')")
    @PostMapping(value = "/release/dataflow/{dataflowId}/dataProvider/{dataProviderId}")
    public void release(Long dataflowId, Long dataProviderId, boolean restrictFromPublic, boolean validate) {
       try {
           restrictFromPublic = true;
           validate = true;
           System.out.println("---------------GOT REQUEST FOR DATAFLOW ID---------------------------" + dataflowId);
           Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
           CreateReleaseStartNotificationCommand command = CreateReleaseStartNotificationCommand.builder().transactionId(UUID.randomUUID().toString()).releaseAggregateId(UUID.randomUUID().toString())
                   .dataflowId(dataflowId).dataProviderId(dataProviderId).restrictFromPublic(restrictFromPublic)
                   .validate(validate).build();
           commandGateway.send(GenericCommandMessage.asCommandMessage(command).withMetaData(MetaData.with("auth", authentication)));
       } catch (Exception e) {
           System.out.println("------------------EXCEPTION------------------------------");
       }
    }
}
