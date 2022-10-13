package org.eea.orchestrator.controller;

import org.axonframework.commandhandling.GenericCommandMessage;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.messaging.MetaData;
import org.eea.interfaces.controller.orchestrator.OrchestratorController;
import org.eea.axon.release.commands.CreateReleaseStartNotificationCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping(value = "/orchestrator")
public class OrchestratorControllerImpl implements OrchestratorController {

    private final CommandGateway commandGateway;

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
           validate = false;

           Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
           CreateReleaseStartNotificationCommand command = CreateReleaseStartNotificationCommand.builder().id(UUID.randomUUID().toString()).aggregate(UUID.randomUUID().toString())
                   .dataflowId(dataflowId).dataProviderId(dataProviderId).restrictFromPublic(restrictFromPublic)
                   .validate(validate).build();
           Map<String, String> map = new HashMap<>();
           map.put("auth", authentication.getName());
           commandGateway.send(GenericCommandMessage.asCommandMessage(command).withMetaData(MetaData.with("auth", authentication)));
       } catch (Exception e) {
           System.out.println("------------------EXCEPTION------------------------------");
       }
    }
}
