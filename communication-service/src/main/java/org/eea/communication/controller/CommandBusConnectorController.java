package org.eea.communication.controller;

import org.axonframework.commandhandling.CommandBus;
import org.axonframework.extensions.springcloud.commandhandling.MessageRoutingInformation;
import org.axonframework.extensions.springcloud.commandhandling.SpringHttpCommandBusConnector;
import org.axonframework.extensions.springcloud.commandhandling.mode.RestCapabilityDiscoveryMode;
import org.axonframework.extensions.springcloud.commandhandling.mode.SerializedMemberCapabilities;
import org.axonframework.serialization.Serializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping({"/spring-command-bus-connector"})
public class CommandBusConnectorController extends SpringHttpCommandBusConnector {

    @Autowired
    public CommandBusConnectorController(@Qualifier("localSegment") CommandBus localSegment, RestTemplate restTemplate, @Qualifier("messageSerializer") Serializer serializer) {
        super(SpringHttpCommandBusConnector.builder().localCommandBus(localSegment).restOperations(restTemplate).serializer(serializer));
    }
}
