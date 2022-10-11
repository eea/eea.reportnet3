package org.eea.orchestrator.axon.release.aggregates;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;
import org.eea.axon.release.commands.UpdateRepresentativeVisibilityCommand;
import org.eea.axon.release.events.RepresentativeVisibilityUpdatedEvent;
import org.springframework.beans.BeanUtils;

import java.util.List;

@Aggregate
public class DataflowReleaseAggregate {

    @AggregateIdentifier
    private String dataflowAggregate;
    private String id;
    private Long dataProviderId;
    private Long dataflowId;
    private boolean restrictFromPublic;
    private boolean validate;
    private List<Long> datasetIds;

    public DataflowReleaseAggregate() {

    }

    @CommandHandler
    public DataflowReleaseAggregate(UpdateRepresentativeVisibilityCommand command) {
        RepresentativeVisibilityUpdatedEvent event = new RepresentativeVisibilityUpdatedEvent();
        BeanUtils.copyProperties(command, event);
        AggregateLifecycle.apply(event);
    }

    @EventSourcingHandler
    public void on(RepresentativeVisibilityUpdatedEvent event) {
        this.dataflowAggregate = event.getDataflowAggregate();
        this.id = event.getId();
        this.dataflowId = event.getDataflowId();
        this.dataProviderId = event.getDataProviderId();
        this.restrictFromPublic = event.isRestrictFromPublic();
        this.validate = event.isValidate();
        this.datasetIds = event.getDatasetIds();
    }
}













