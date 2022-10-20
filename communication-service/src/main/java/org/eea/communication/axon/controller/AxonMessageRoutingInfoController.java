//package org.eea.communication.axon.controller;
//
//import org.axonframework.extensions.springcloud.commandhandling.MessageRoutingInformation;
//import org.axonframework.extensions.springcloud.commandhandling.mode.RestCapabilityDiscoveryMode;
//import org.axonframework.extensions.springcloud.commandhandling.mode.SerializedMemberCapabilities;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//import org.springframework.web.client.RestTemplate;
//
//@RestController
//@RequestMapping("/message-routing-information")
//public class AxonMessageRoutingInfoController {
//    private RestCapabilityDiscoveryMode restDiscoveryMode;
//
//    public AxonMessageRoutingInfoController() {
//        this.restDiscoveryMode = RestCapabilityDiscoveryMode.builder().messageCapabilitiesEndpoint("/message-routing-information").restTemplate(new RestTemplate()).build();
//
//    }
//
//    @GetMapping
//    public MessageRoutingInformation getLocalMessageRoutingInformation() {
//        SerializedMemberCapabilities serializedCapabilities = this.restDiscoveryMode.getLocalMemberCapabilities();
//        return new MessageRoutingInformation(serializedCapabilities.getLoadFactor(), serializedCapabilities.getSerializedCommandFilter(), serializedCapabilities.getSerializedCommandFilterType());
//    }
//
//}
