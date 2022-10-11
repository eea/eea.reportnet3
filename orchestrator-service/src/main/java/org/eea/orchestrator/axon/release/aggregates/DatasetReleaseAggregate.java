package org.eea.orchestrator.axon.release.aggregates;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;
import org.eea.axon.release.commands.*;
import org.eea.axon.release.events.*;
import org.springframework.beans.BeanUtils;

import java.util.HashMap;
import java.util.List;

@Aggregate
public class DatasetReleaseAggregate {

    @AggregateIdentifier
    private String datasetAggregate;
    private String id;
    private Long dataflowId;
    private Long dataProviderId;
    private boolean restrictFromPublic;
    private boolean validate;
    private List<Long> datasetIds;
    private HashMap<Long, Long> datasetSnapshots;

    DatasetReleaseAggregate() {
    }

    @CommandHandler
    public DatasetReleaseAggregate(AddReleaseLocksCommand command) {
        ReleaseLocksAddedEvent event = new ReleaseLocksAddedEvent();
        BeanUtils.copyProperties(command, event);
        AggregateLifecycle.apply(event);
    }

    @EventSourcingHandler
    public void on(ReleaseLocksAddedEvent event) {
        this.datasetAggregate = event.getDatasetAggregate();
        this.id = event.getId();
        this.dataflowId = event.getDataflowId();
        this.dataProviderId = event.getDataProviderId();
        this.restrictFromPublic = event.isRestrictFromPublic();
        this.validate = event.isValidate();
        this.datasetIds = event.getDatasetIds();
    }

    @CommandHandler
    public DatasetReleaseAggregate(CreateSnapshotRecordRorReleaseInMetabaseCommand command) {
        SnapshotRecordForReleaseCreatedInMetabaseEvent event = new SnapshotRecordForReleaseCreatedInMetabaseEvent();
        BeanUtils.copyProperties(command, event);
        AggregateLifecycle.apply(event);
    }

    @EventSourcingHandler
    public void on(SnapshotRecordForReleaseCreatedInMetabaseEvent event) {
        this.datasetAggregate = event.getDatasetAggregate();
        this.id = event.getId();
        this.dataflowId = event.getDataflowId();
        this.dataProviderId = event.getDataProviderId();
        this.restrictFromPublic = event.isRestrictFromPublic();
        this.validate = event.isValidate();
        this.datasetIds = event.getDatasetIds();
    }

    @CommandHandler
    public DatasetReleaseAggregate(UpdateDatasetStatusCommand command) {
        DatasetStatusUpdatedEvent event = new DatasetStatusUpdatedEvent();
        BeanUtils.copyProperties(command, event);
        AggregateLifecycle.apply(event);
    }

    @EventSourcingHandler
    public void on(DatasetStatusUpdatedEvent event) {
        this.datasetAggregate = event.getDatasetAggregate();
        this.id = event.getId();
        this.dataflowId = event.getDataflowId();
        this.dataProviderId = event.getDataProviderId();
        this.restrictFromPublic = event.isRestrictFromPublic();
        this.validate = event.isValidate();
        this.datasetIds = event.getDatasetIds();
        this.datasetSnapshots = event.getDatasetSnapshots();
    }

    @CommandHandler
    public DatasetReleaseAggregate(DeleteProviderCommand command) {
        ProviderDeletedEvent event = new ProviderDeletedEvent();
        BeanUtils.copyProperties(command, event);
        AggregateLifecycle.apply(event);
    }

    @EventSourcingHandler
    public void on(ProviderDeletedEvent event) {
        this.datasetAggregate = event.getDatasetAggregate();
        this.id = event.getId();
        this.dataflowId = event.getDataflowId();
        this.dataProviderId = event.getDataProviderId();
        this.restrictFromPublic = event.isRestrictFromPublic();
        this.validate = event.isValidate();
        this.datasetIds = event.getDatasetIds();
        this.datasetSnapshots = event.getDatasetSnapshots();
    }

    @CommandHandler
    public DatasetReleaseAggregate(UpdateInternalRepresentativeCommand command) {
        InternalRepresentativeUpdatedEvent event = new InternalRepresentativeUpdatedEvent();
        BeanUtils.copyProperties(command, event);
        AggregateLifecycle.apply(event);
    }

    @EventSourcingHandler
    public void on(InternalRepresentativeUpdatedEvent event) {
        this.datasetAggregate = event.getDatasetAggregate();
        this.id = event.getId();
        this.dataflowId = event.getDataflowId();
        this.dataProviderId = event.getDataProviderId();
        this.restrictFromPublic = event.isRestrictFromPublic();
        this.validate = event.isValidate();
        this.datasetIds = event.getDatasetIds();
        this.datasetSnapshots = event.getDatasetSnapshots();
    }
}











