package org.eea.orchestrator.axon.release.aggregates;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;
import org.eea.axon.release.commands.ExecuteValidationProcessCommand;
import org.eea.axon.release.events.ValidationProcessForReleaseAddedEvent;
import org.springframework.beans.BeanUtils;

import java.util.List;

@Aggregate
public class ValidationReleaseAggregate {

    @AggregateIdentifier
    private String validationAggregate;
    private String id;
    private Long dataProviderId;
    private Long dataflowId;
    private boolean restrictFromPublic;
    private boolean validate;

    public ValidationReleaseAggregate() {

    }

    @CommandHandler
    public ValidationReleaseAggregate(ExecuteValidationProcessCommand command) {
            ValidationProcessForReleaseAddedEvent event = new ValidationProcessForReleaseAddedEvent();
            BeanUtils.copyProperties(command, event);
            AggregateLifecycle.apply(event);
    }

    @EventSourcingHandler
    public void on(ValidationProcessForReleaseAddedEvent event) {
        this.validationAggregate = event.getValidationAggregate();
        this.id = event.getId();
        this.dataflowId = event.getDataflowId();
        this.dataProviderId = event.getDataProviderId();
        this.restrictFromPublic = event.isRestrictFromPublic();
        this.validate = event.isValidate();
    }

}













