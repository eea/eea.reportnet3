//package org.eea.communication.axon.aggregates;
//
//import org.axonframework.commandhandling.CommandHandler;
//import org.axonframework.eventsourcing.EventSourcingHandler;
//import org.axonframework.modelling.command.AggregateIdentifier;
//import org.axonframework.modelling.command.AggregateLifecycle;
//import org.axonframework.spring.stereotype.Aggregate;
//import org.eea.axon.release.commands.SendUserNotificationCommand;
//import org.eea.axon.release.events.UserNotificationCreatedEvent;
//import org.springframework.beans.BeanUtils;
//import org.springframework.stereotype.Component;
//
//import java.util.List;
//
//@Component
//@Aggregate
//public class CommunicationAggregate {
//
//    @AggregateIdentifier
//    private String aggregate;
//    private String id;
//    private Long dataProviderId;
//    private Long dataflowId;
//    private boolean restrictFromPublic;
//    private boolean validate;
//    private List<Long> datasetIds;
//
//    public CommunicationAggregate() {
//
//    }
//
//    @CommandHandler
//    public CommunicationAggregate(SendUserNotificationCommand command) {
//        UserNotificationCreatedEvent event = new UserNotificationCreatedEvent();
//        BeanUtils.copyProperties(command, event);
//        AggregateLifecycle.apply(event);
//    }
//
//    @EventSourcingHandler
//    public void on(UserNotificationCreatedEvent event) {
//        this.aggregate = event.getAggregate();
//        this.id = event.getId();
//        this.dataflowId = event.getDataflowId();
//        this.dataProviderId = event.getDataProviderId();
//        this.restrictFromPublic = event.isRestrictFromPublic();
//        this.validate = event.isValidate();
//    }
//}
//
//
//
//
//
//
//
//
//
//
//
//
//
