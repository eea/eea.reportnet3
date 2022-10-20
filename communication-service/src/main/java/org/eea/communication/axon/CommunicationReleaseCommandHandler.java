package org.eea.communication.axon;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.eea.axon.release.commands.SendUserNotificationForReleaseStartedCommand;
import org.eea.axon.release.events.UserNotifationForReleaseSentEvent;

import org.springframework.stereotype.Component;


import static org.axonframework.modelling.command.AggregateLifecycle.apply;

@Component
public class CommunicationReleaseCommandHandler  {


    public CommunicationReleaseCommandHandler(){
    }
    @CommandHandler
    public void handle(SendUserNotificationForReleaseStartedCommand command){

        UserNotifationForReleaseSentEvent event  = new UserNotifationForReleaseSentEvent();
        event.setId(command.getId());
        event.setAggregate(command.getAggregate());
        apply(event);
    }

    @EventSourcingHandler
    public void on(UserNotifationForReleaseSentEvent event) {
    }

}
