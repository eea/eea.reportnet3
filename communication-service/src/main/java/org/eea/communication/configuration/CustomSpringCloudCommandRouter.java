package org.eea.communication.configuration;

import org.axonframework.commandhandling.distributed.ConsistentHash;
import org.axonframework.commandhandling.distributed.ConsistentHashChangeListener;
import org.axonframework.extensions.springcloud.commandhandling.SpringCloudCommandRouter;
import org.axonframework.extensions.springcloud.commandhandling.mode.CapabilityDiscoveryMode;
import org.axonframework.extensions.springcloud.commandhandling.mode.RestCapabilityDiscoveryMode;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.event.HeartbeatEvent;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.context.event.EventListener;
import org.springframework.web.client.RestTemplate;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CustomSpringCloudCommandRouter extends SpringCloudCommandRouter {

    private DiscoveryClient discoveryClient;
    private Registration localServiceInstance;
    private CapabilityDiscoveryMode capabilityDiscoveryMode;
    private final AtomicReference<ConsistentHash> atomicConsistentHash = new AtomicReference(new ConsistentHash());
    private  ConsistentHashChangeListener consistentHashChangeListener;
    private Predicate<ServiceInstance> serviceInstanceFilter = (serviceInstance) -> {
        return true;
    };
    protected CustomSpringCloudCommandRouter(Builder builder, DiscoveryClient discoveryClient , Registration localServiceInstance, RestTemplate restTemplate) {
        super(builder);
        this.discoveryClient = discoveryClient;
        this.localServiceInstance = localServiceInstance;
        this.capabilityDiscoveryMode = RestCapabilityDiscoveryMode.builder().restTemplate(restTemplate).build();
        this.consistentHashChangeListener  = ConsistentHashChangeListener.noOp();
    }

    @Override
    @EventListener
    public void updateMemberships(HeartbeatEvent event) {
        this.updateMemberships();
    }


    private void updateMemberships() {
        AtomicReference<ConsistentHash> updatedConsistentHash = new AtomicReference(new ConsistentHash());

        Stream var10000 = this.discoveryClient.getServices().stream();
        DiscoveryClient var10001 = this.discoveryClient;
        var10001.getClass();
        List<ServiceInstance> instances = discoveryClient.getServices().stream()
                .map(discoveryClient::getInstances)
                .flatMap(Collection::stream)
                .filter(serviceInstanceFilter)
                .collect(Collectors.toList());

        if (instances.isEmpty()) {
            instances.add(this.localServiceInstance);
        }

        Iterator var3 = instances.iterator();

        while(var3.hasNext()) {
            ServiceInstance serviceInstance = (ServiceInstance)var3.next();
             this.capabilityDiscoveryMode.capabilities(serviceInstance).ifPresent((memberCapabilities) -> {
                ConsistentHash var100003 = (ConsistentHash)updatedConsistentHash.updateAndGet((consistentHash) -> {
                    return consistentHash.with(this.buildMember(serviceInstance), memberCapabilities.getLoadFactor(), memberCapabilities.getCommandFilter());
                });
            });
        }

        ConsistentHash newConsistentHash = (ConsistentHash)updatedConsistentHash.get();
        this.atomicConsistentHash.set(newConsistentHash);
        this.consistentHashChangeListener.onConsistentHashChanged(newConsistentHash);
    }
}
