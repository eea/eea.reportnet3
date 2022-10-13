package org.eea.orchestrator.axon.aggregates;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;
import org.eea.axon.release.commands.*;
import org.eea.axon.release.events.*;
import org.springframework.beans.BeanUtils;

import java.util.List;

@Aggregate
public class ReleaseAggregate {

    @AggregateIdentifier
    private String aggregate;
    private String id;
    private Long dataProviderId;
    private Long dataflowId;
    private boolean restrictFromPublic;
    private boolean validate;
    private List<Long> datasetIds;

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
        this.aggregate = event.getAggregate();
        this.id = event.getId();
        this.dataflowId = event.getDataflowId();
        this.dataProviderId = event.getDataProviderId();
        this.restrictFromPublic = event.isRestrictFromPublic();
        this.validate = event.isValidate();
    }

    @CommandHandler
    public ReleaseAggregate(AddReleaseLocksCommand command) {
        ReleaseLocksAddedEvent event = new ReleaseLocksAddedEvent();
        BeanUtils.copyProperties(command, event);
        AggregateLifecycle.apply(event);
    }

    @EventSourcingHandler
    public void on(ReleaseLocksAddedEvent event) {
        this.aggregate = event.getAggregate();
        this.id = event.getId();
        this.dataflowId = event.getDataflowId();
        this.dataProviderId = event.getDataProviderId();
        this.restrictFromPublic = event.isRestrictFromPublic();
        this.validate = event.isValidate();
        this.datasetIds = event.getDatasetIds();
    }

    @CommandHandler
    public ReleaseAggregate(UpdateRepresentativeVisibilityCommand command) {
        RepresentativeVisibilityUpdatedEvent event = new RepresentativeVisibilityUpdatedEvent();
        BeanUtils.copyProperties(command, event);
        AggregateLifecycle.apply(event);
    }

    @EventSourcingHandler
    public void on(RepresentativeVisibilityUpdatedEvent event) {
        this.aggregate = event.getAggregate();
        this.id = event.getId();
        this.dataflowId = event.getDataflowId();
        this.dataProviderId = event.getDataProviderId();
        this.restrictFromPublic = event.isRestrictFromPublic();
        this.validate = event.isValidate();
        this.datasetIds = event.getDatasetIds();
    }

    @CommandHandler
    public ReleaseAggregate(ExecuteValidationProcessCommand command) {
        ValidationProcessForReleaseAddedEvent event = new ValidationProcessForReleaseAddedEvent();
        BeanUtils.copyProperties(command, event);
        AggregateLifecycle.apply(event);
    }

    @EventSourcingHandler
    public void on(ValidationProcessForReleaseAddedEvent event) {
        this.aggregate = event.getAggregate();
        this.id = event.getId();
        this.dataflowId = event.getDataflowId();
        this.dataProviderId = event.getDataProviderId();
        this.restrictFromPublic = event.isRestrictFromPublic();
        this.validate = event.isValidate();
        this.datasetIds = event.getDatasetIds();
    }

    @CommandHandler
    public ReleaseAggregate(CreateSnapshotRecordRorReleaseInMetabaseCommand command) {
        SnapshotRecordForReleaseCreatedInMetabaseEvent event = new SnapshotRecordForReleaseCreatedInMetabaseEvent();
        BeanUtils.copyProperties(command, event);
        AggregateLifecycle.apply(event);
    }

    @EventSourcingHandler
    public void on(SnapshotRecordForReleaseCreatedInMetabaseEvent event) {
        this.aggregate = event.getAggregate();
        this.id = event.getId();
        this.dataflowId = event.getDataflowId();
        this.dataProviderId = event.getDataProviderId();
        this.restrictFromPublic = event.isRestrictFromPublic();
        this.validate = event.isValidate();
        this.datasetIds = event.getDatasetIds();
    }

    @CommandHandler
    public ReleaseAggregate(CreateSnapshotFileForReleaseCommand command) {
        SnapshotFileForReleaseCreatedEvent event = new SnapshotFileForReleaseCreatedEvent();
        BeanUtils.copyProperties(command, event);
        AggregateLifecycle.apply(event);
    }

    @EventSourcingHandler
    public void on(SnapshotFileForReleaseCreatedEvent event) {
        this.aggregate = event.getAggregate();
        this.id = event.getId();
        this.dataflowId = event.getDataflowId();
        this.dataProviderId = event.getDataProviderId();
        this.restrictFromPublic = event.isRestrictFromPublic();
        this.validate = event.isValidate();
        this.datasetIds = event.getDatasetIds();
    }

    @CommandHandler
    public ReleaseAggregate(UpdateDatasetStatusCommand command) {
        DatasetStatusUpdatedEvent event = new DatasetStatusUpdatedEvent();
        BeanUtils.copyProperties(command, event);
        AggregateLifecycle.apply(event);
    }

    @EventSourcingHandler
    public void on(DatasetStatusUpdatedEvent event) {
        this.aggregate = event.getAggregate();
        this.id = event.getId();
        this.dataflowId = event.getDataflowId();
        this.dataProviderId = event.getDataProviderId();
        this.restrictFromPublic = event.isRestrictFromPublic();
        this.validate = event.isValidate();
        this.datasetIds = event.getDatasetIds();
    }

    @CommandHandler
    public ReleaseAggregate(DeleteProviderCommand command) {
        ProviderDeletedEvent event = new ProviderDeletedEvent();
        BeanUtils.copyProperties(command, event);
        AggregateLifecycle.apply(event);
    }

    @EventSourcingHandler
    public void on(ProviderDeletedEvent event) {
        this.aggregate = event.getAggregate();
        this.id = event.getId();
        this.dataflowId = event.getDataflowId();
        this.dataProviderId = event.getDataProviderId();
        this.restrictFromPublic = event.isRestrictFromPublic();
        this.validate = event.isValidate();
        this.datasetIds = event.getDatasetIds();
    }

    @CommandHandler
    public ReleaseAggregate(UpdateInternalRepresentativeCommand command) {
        InternalRepresentativeUpdatedEvent event = new InternalRepresentativeUpdatedEvent();
        BeanUtils.copyProperties(command, event);
        AggregateLifecycle.apply(event);
    }

    @EventSourcingHandler
    public void on(InternalRepresentativeUpdatedEvent event) {
        this.aggregate = event.getAggregate();
        this.id = event.getId();
        this.dataflowId = event.getDataflowId();
        this.dataProviderId = event.getDataProviderId();
        this.restrictFromPublic = event.isRestrictFromPublic();
        this.validate = event.isValidate();
        this.datasetIds = event.getDatasetIds();
    }

    @CommandHandler
    public ReleaseAggregate(UpdateDatasetRunningStatusCommand command) {
        DatasetRunningStatusUpdatedEvent event = new DatasetRunningStatusUpdatedEvent();
        BeanUtils.copyProperties(command, event);
        AggregateLifecycle.apply(event);
    }

    @EventSourcingHandler
    public void on(DatasetRunningStatusUpdatedEvent event) {
        this.aggregate = event.getAggregate();
        this.id = event.getId();
        this.dataflowId = event.getDataflowId();
        this.dataProviderId = event.getDataProviderId();
        this.restrictFromPublic = event.isRestrictFromPublic();
        this.validate = event.isValidate();
        this.datasetIds = event.getDatasetIds();
    }

    @CommandHandler
    public ReleaseAggregate(RestoreDataFromSnapshotCommand command) {
        DataRestoredFromSnapshotEvent event = new DataRestoredFromSnapshotEvent();
        BeanUtils.copyProperties(command, event);
        AggregateLifecycle.apply(event);
    }

    @EventSourcingHandler
    public void on(DataRestoredFromSnapshotEvent event) {
        this.aggregate = event.getAggregate();
        this.id = event.getId();
        this.dataflowId = event.getDataflowId();
        this.dataProviderId = event.getDataProviderId();
        this.restrictFromPublic = event.isRestrictFromPublic();
        this.validate = event.isValidate();
        this.datasetIds = event.getDatasetIds();
    }

    @CommandHandler
    public ReleaseAggregate(MarkSnapshotReleasedCommand command) {
        SnapshotMarkedReleasedEvent event = new SnapshotMarkedReleasedEvent();
        BeanUtils.copyProperties(command, event);
        AggregateLifecycle.apply(event);
    }

    @EventSourcingHandler
    public void on(SnapshotMarkedReleasedEvent event) {
        this.aggregate = event.getAggregate();
        this.id = event.getId();
        this.dataflowId = event.getDataflowId();
        this.dataProviderId = event.getDataProviderId();
        this.restrictFromPublic = event.isRestrictFromPublic();
        this.validate = event.isValidate();
        this.datasetIds = event.getDatasetIds();
    }

    @CommandHandler
    public ReleaseAggregate(RemoveReleaseLocksCommand command) {
        ReleaseLocksRemovedEvent event = new ReleaseLocksRemovedEvent();
        BeanUtils.copyProperties(command, event);
        AggregateLifecycle.apply(event);
    }

    @EventSourcingHandler
    public void on(ReleaseLocksRemovedEvent event) {
        this.aggregate = event.getAggregate();
        this.id = event.getId();
        this.dataflowId = event.getDataflowId();
        this.dataProviderId = event.getDataProviderId();
        this.restrictFromPublic = event.isRestrictFromPublic();
        this.validate = event.isValidate();
        this.datasetIds = event.getDatasetIds();
    }
}













