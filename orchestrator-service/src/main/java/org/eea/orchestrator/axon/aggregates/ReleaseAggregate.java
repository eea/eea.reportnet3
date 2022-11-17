package org.eea.orchestrator.axon.aggregates;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;
import org.eea.axon.release.commands.CreateReleaseStartNotificationCommand;
import org.eea.axon.release.events.ReleaseStartNotificationCreatedEvent;
import org.springframework.beans.BeanUtils;

@Aggregate
public class ReleaseAggregate {

    @AggregateIdentifier
    private String releaseAggregate;
    private String transactionId;
    private Long dataProviderId;
    private Long dataflowId;
    private boolean restrictFromPublic;
    private boolean validate;

    public ReleaseAggregate() {

    }

    @CommandHandler
    public ReleaseAggregate(CreateReleaseStartNotificationCommand command) {
        ReleaseStartNotificationCreatedEvent event = new ReleaseStartNotificationCreatedEvent();
        BeanUtils.copyProperties(command, event);
        AggregateLifecycle.apply(event);
    }

    @EventSourcingHandler
    public void on(ReleaseStartNotificationCreatedEvent event) {
        this.releaseAggregate = event.getReleaseAggregateId();
        this.transactionId = event.getTransactionId();
        this.dataflowId = event.getDataflowId();
        this.dataProviderId = event.getDataProviderId();
        this.restrictFromPublic = event.isRestrictFromPublic();
        this.validate = event.isValidate();
    }

}













