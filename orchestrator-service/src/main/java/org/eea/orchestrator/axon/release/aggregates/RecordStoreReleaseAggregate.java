package org.eea.orchestrator.axon.release.aggregates;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;
import org.eea.axon.release.commands.CreateSnapshotFileForReleaseCommand;
import org.eea.axon.release.events.SnapshotFileForReleaseCreatedEvent;
import org.springframework.beans.BeanUtils;

import java.util.HashMap;
import java.util.List;

@Aggregate
public class RecordStoreReleaseAggregate {

    @AggregateIdentifier
    private String recordStoreAggregate;
    private String id;
    private Long dataProviderId;
    private Long dataflowId;
    private boolean restrictFromPublic;
    private boolean validate;
    private List<Long> datasetIds;
    private HashMap<Long, Long> datasetSnapshots;

    public RecordStoreReleaseAggregate() {

    }

    @CommandHandler
    public RecordStoreReleaseAggregate(CreateSnapshotFileForReleaseCommand command) {
        SnapshotFileForReleaseCreatedEvent event = new SnapshotFileForReleaseCreatedEvent();
        BeanUtils.copyProperties(command, event);
        AggregateLifecycle.apply(event);
    }

    @EventSourcingHandler
    public void on(SnapshotFileForReleaseCreatedEvent event) {
        this.recordStoreAggregate = event.getRecordStoreAggregate();
        this.id = event.getId();
        this.dataflowId = event.getDataflowId();
        this.dataProviderId = event.getDataProviderId();
        this.restrictFromPublic = event.isRestrictFromPublic();
        this.validate = event.isValidate();
        this.datasetSnapshots = event.getDatasetSnapshots();
        this.datasetIds = event.getDatasetIds();
    }

}













