package org.eea.orchestrator.axon.sagas;

import org.axonframework.commandhandling.GenericCommandMessage;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventhandling.GenericDomainEventMessage;
import org.axonframework.eventhandling.gateway.EventGateway;
import org.axonframework.messaging.MetaData;
import org.axonframework.modelling.saga.EndSaga;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.modelling.saga.StartSaga;
import org.axonframework.spring.stereotype.Saga;
import org.eea.axon.release.commands.*;
import org.eea.axon.release.events.*;
import org.eea.interfaces.vo.ums.enums.SecurityRoleEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;

import java.util.*;
import java.util.stream.Collectors;

@Saga
public class ReleaseSaga {

    /**
     * The Constant LOG.
     */
    private static final Logger LOG = LoggerFactory.getLogger(ReleaseSaga.class);

    @Autowired
    private transient CommandGateway commandGateway;
    @Autowired
    private EventGateway eventGateway;

    @StartSaga
    @SagaEventHandler(associationProperty = "transactionId")
    public void handle(ReleaseStartNotificationCreatedEvent event, MetaData metaData) {
        LOG.info("ReleaseStartNotificationCreatedEvent event received for dataflowId {}, dataProviderId {}, jobId {}", event.getDataflowId(), event.getDataProviderId(), event.getJobId());
        LinkedHashMap auth = (LinkedHashMap) metaData.get("auth");
        SetJobInProgressCommand setJobInProgressCommand = new SetJobInProgressCommand();
        BeanUtils.copyProperties(event, setJobInProgressCommand);
        setJobInProgressCommand.setCommunicationReleaseAggregateId(UUID.randomUUID().toString());

        commandGateway.send(GenericCommandMessage.asCommandMessage(setJobInProgressCommand).withMetaData(MetaData.with("auth", auth))).exceptionally(er -> {
            LOG.error("Error while executing command SetJobInProgressCommand for dataflowId {}, dataProviderId {}, jobId {},{}", event.getDataflowId(), event.getDataProviderId(), event.getJobId(), er.getCause().toString());
            return er;
        });
    }

    @SagaEventHandler(associationProperty = "transactionId")
    public void handle(JobSetInProgressEvent event, MetaData metaData) {
        LOG.info("JobSetInProgressEvent event received for dataflowId {}, dataProviderId {}, jobId {}", event.getDataflowId(), event.getDataProviderId(), event.getJobId());
        LinkedHashMap auth = (LinkedHashMap) metaData.get("auth");
        SendUserNotificationForReleaseStartedCommand sendUserNotificationForReleaseStartedCommand = new SendUserNotificationForReleaseStartedCommand();
        BeanUtils.copyProperties(event, sendUserNotificationForReleaseStartedCommand);
        sendUserNotificationForReleaseStartedCommand.setCommunicationReleaseAggregateId(UUID.randomUUID().toString());

        commandGateway.send(GenericCommandMessage.asCommandMessage(sendUserNotificationForReleaseStartedCommand).withMetaData(MetaData.with("auth", auth))).exceptionally(er -> {
            LOG.error("Error while executing command SendUserNotificationForReleaseStartedCommand for dataflowId {}, dataProviderId {}, jobId {},{}", event.getDataflowId(), event.getDataProviderId(), event.getJobId(), er.getCause().toString());
            return er;
        });
    }

    @SagaEventHandler(associationProperty = "transactionId")
    public void handle(UserNotifationForReleaseSentEvent event, MetaData metaData) {
        LOG.info("UserNotifationForReleaseSentEvent event received for dataflowId {}, dataProviderId {}, jobId {}", event.getDataflowId(), event.getDataProviderId(), event.getJobId());
        AddReleaseLocksCommand addReleaseLocksCommand = new AddReleaseLocksCommand();
        BeanUtils.copyProperties(event, addReleaseLocksCommand);
        addReleaseLocksCommand.setDatasetReleaseAggregateId(UUID.randomUUID().toString());
        LinkedHashMap auth = (LinkedHashMap) metaData.get("auth");

        commandGateway.send(GenericCommandMessage.asCommandMessage(addReleaseLocksCommand).withMetaData(MetaData.with("auth", auth))).exceptionally(er -> {
            LOG.error("Error while executing command AddReleaseLocksCommand for dataflowId {}, dataProvider {}, jobId {}, {}", event.getDataflowId(), event.getDataProviderId(), event.getJobId(), er.getCause().toString());
            return er;
        });
    }

    @SagaEventHandler(associationProperty = "transactionId")
    public void handle(ReleaseLocksAddedEvent event, MetaData metaData) {
        LOG.info("ReleaseLocksAddedEvent event received for dataflowId {}, dataProviderId {}, jobId {}", event.getDataflowId(), event.getDataProviderId(), event.getJobId());
        UpdateRepresentativeVisibilityCommand updateRepresentativeVisibilityCommand = new UpdateRepresentativeVisibilityCommand();
        BeanUtils.copyProperties(event, updateRepresentativeVisibilityCommand);
        updateRepresentativeVisibilityCommand.setDataflowReleaseAggregateId(UUID.randomUUID().toString());

        LinkedHashMap auth = (LinkedHashMap) metaData.get("auth");
        commandGateway.send(GenericCommandMessage.asCommandMessage(updateRepresentativeVisibilityCommand).withMetaData(MetaData.with("auth", auth))).exceptionally(er -> {
            LOG.error("Error while executing command UpdateRepresentativeVisibilityCommand for dataflowId {}, dataProviderId {}, jobId {}, {}", event.getDataflowId(), event.getDataProviderId(), er.getCause().toString());
            RepresentativeVisibilityUpdateFailedEvent representativeVisibilityUpdateFailedEvent = new RepresentativeVisibilityUpdateFailedEvent();
            BeanUtils.copyProperties(event, representativeVisibilityUpdateFailedEvent);
            eventGateway.publish(GenericDomainEventMessage.asEventMessage(event).withMetaData(metaData));
            return er;
        });
    }

    @SagaEventHandler(associationProperty = "transactionId")
    public void handle(RepresentativeVisibilityUpdatedEvent event, MetaData metaData) {
        LOG.info("RepresentativeVisibilityUpdatedEvent event received for dataflowId {}, dataProviderId {}, jobId {}", event.getDataflowId(), event.getDataProviderId(), event.getJobId());
        LinkedHashMap auth = (LinkedHashMap) metaData.get("auth");
        if (!isAdmin(metaData) || event.isValidate()) {
            CreateValidationProcessForReleaseCommand createValidationProcessForReleaseCommand = new CreateValidationProcessForReleaseCommand();
            BeanUtils.copyProperties(event, createValidationProcessForReleaseCommand);
            createValidationProcessForReleaseCommand.setValidationReleaseAggregateId(UUID.randomUUID().toString());

            commandGateway.send(GenericCommandMessage.asCommandMessage(createValidationProcessForReleaseCommand).withMetaData(MetaData.with("auth", auth))).exceptionally(er -> {
                LOG.error("Error while executing command CreateValidationProcessForReleaseCommand for dataflowId {}, dataProviderId {}, jobId {}, {}", event.getDataflowId(), event.getDataProviderId(), event.getJobId(), er.getCause().toString());
                ValidationProcessForReleaseFailedEvent validationProcessForReleaseFailedEvent = new ValidationProcessForReleaseFailedEvent();
                BeanUtils.copyProperties(event, validationProcessForReleaseFailedEvent);
                eventGateway.publish(validationProcessForReleaseFailedEvent);
                return er;
            });
        } else {
            CreateSnapshotRecordRorReleaseInMetabaseCommand createSnapshotRecordRorReleaseInMetabaseCommand = new CreateSnapshotRecordRorReleaseInMetabaseCommand();
            BeanUtils.copyProperties(event, createSnapshotRecordRorReleaseInMetabaseCommand);

            commandGateway.send(GenericCommandMessage.asCommandMessage(createSnapshotRecordRorReleaseInMetabaseCommand).withMetaData(MetaData.with("auth", auth))).exceptionally(er -> {

                LOG.error("Error while executing command CreateSnapshotRecordRorReleaseInMetabaseCommand for dataflowId {}, dataProviderId {},{}", event.getDataflowId(), event.getDataProviderId(), er.getCause().toString());
                CreateSnapshotRecordForReleaseInMetabaseFailedEvent createSnapshotRecordForReleaseInMetabaseFailedEvent = new CreateSnapshotRecordForReleaseInMetabaseFailedEvent();
                BeanUtils.copyProperties(event, createSnapshotRecordForReleaseInMetabaseFailedEvent);
                eventGateway.publish(createSnapshotRecordForReleaseInMetabaseFailedEvent, metaData);
                return er;
            });
        }
    }

    @SagaEventHandler(associationProperty = "transactionId")
    public void handle(ReleaseRefusedEvent event, MetaData metaData) {
        LOG.info("ReleaseRefusedEvent event received for dataflowId {}, dataProviderId {}, jobId {}", event.getDataflowId(), event.getDataProviderId(), event.getJobId());
        LinkedHashMap auth = (LinkedHashMap) metaData.get("auth");
        RevertRepresentativeVisibilityCommand revertRepresentativeVisibilityCommand = new RevertRepresentativeVisibilityCommand();
        BeanUtils.copyProperties(event, revertRepresentativeVisibilityCommand);

        commandGateway.send(GenericCommandMessage.asCommandMessage(revertRepresentativeVisibilityCommand).withMetaData(MetaData.with("auth", auth))).exceptionally(e -> {
            LOG.error("Error while executing command RevertRepresentativeVisibilityCommand for dataflowId {}, dataProviderId {}, jobId {}, {}", event.getDataflowId(), event.getDataProviderId(), event.getJobId(), e.getCause().toString());
            return e;
        });

    }

    private boolean isAdmin(MetaData metaData) {
        String roleAdmin = "ROLE_" + SecurityRoleEnum.ADMIN;
        LinkedHashMap auth = (LinkedHashMap) metaData.get("auth");
        List<LinkedHashMap<String, GrantedAuthority>> authorities = (List<LinkedHashMap<String, GrantedAuthority>>) auth.get("authorities");
        for (LinkedHashMap<String, GrantedAuthority> authority : authorities) {
            for (Map.Entry entry : authority.entrySet()) {
                if (entry.getValue().equals(roleAdmin)) {
                    return true;
                }
            }
        }
        return false;
    }

    @SagaEventHandler(associationProperty = "transactionId")
    public void handle(ValidationProcessForReleaseCreatedEvent event, MetaData metaData) {
        LOG.info("ValidationProcessForReleaseCreatedEvent event received for dataflowId {}, dataProviderId {}, jobId {}", event.getDataflowId(), event.getDataProviderId(), event.getJobId());
        CreateValidationTasksForReleaseCommand createValidationTasksForReleaseCommand = new CreateValidationTasksForReleaseCommand();
        BeanUtils.copyProperties(event, createValidationTasksForReleaseCommand);

        LinkedHashMap auth = (LinkedHashMap) metaData.get("auth");
        commandGateway.send(GenericCommandMessage.asCommandMessage(createValidationTasksForReleaseCommand).withMetaData(MetaData.with("auth", auth))).exceptionally(er -> {
            LOG.error("Error while executing command CreateValidationTasksForReleaseCommand for dataflowId {}, dataProviderId {}, jobId {}, {}", event.getDataflowId(), event.getDataProviderId(), event.getJobId(), er.getCause().toString());
            ValidationTasksForReleaseCreationFailedEvent validationTasksForReleaseCreationFailedEvent = new ValidationTasksForReleaseCreationFailedEvent();
            BeanUtils.copyProperties(event, validationTasksForReleaseCreationFailedEvent);
            eventGateway.publish(validationTasksForReleaseCreationFailedEvent, metaData);
            return er;
        });
    }

    @SagaEventHandler(associationProperty = "transactionId")
    public void handle(ValidationTasksForReleaseCreatedEvent event) {
        LOG.info("ValidationTasksForReleaseCreatedEvent event received for dataflowId {}, dataProviderId {}, jobId {}", event.getDataflowId(), event.getDataProviderId(), event.getJobId());
    }

    @SagaEventHandler(associationProperty = "transactionId")
    public void handle(MaterializedViewShouldBeRefreshedEvent event, MetaData metaData) {
        LOG.info("MaterializedViewShouldBeRefreshedEvent event received for dataflowId {}, dataProviderId {}, jobId {}", event.getDataflowId(), event.getDataProviderId(), event.getJobId());
        RefreshMaterializedViewForReferenceDatasetCommand refreshMaterializedViewForReferenceDatasetCommand = new RefreshMaterializedViewForReferenceDatasetCommand();
        BeanUtils.copyProperties(event, refreshMaterializedViewForReferenceDatasetCommand);

        LinkedHashMap auth = (LinkedHashMap) metaData.get("auth");
        commandGateway.send(GenericCommandMessage.asCommandMessage(refreshMaterializedViewForReferenceDatasetCommand).withMetaData(MetaData.with("auth", auth))).exceptionally(er -> {
            LOG.error("Error while executing command RefreshMaterializedViewForReferenceDatasetCommand for dataflowId {}, dataProviderId {}, jobId {}, {}", event.getDataflowId(), event.getDataProviderId(), event.getJobId(), er.getCause().toString());
            RefreshMaterializedViewForReferencedDatasetFailedEvent refreshMaterializedViewForReferencedDatasetFailedEvent = new RefreshMaterializedViewForReferencedDatasetFailedEvent();
            BeanUtils.copyProperties(event, refreshMaterializedViewForReferencedDatasetFailedEvent);
            eventGateway.publish(refreshMaterializedViewForReferencedDatasetFailedEvent, metaData);
            return er;
        });
    }

    @SagaEventHandler(associationProperty = "transactionId")
    public void handle(MaterializedViewForReferenceDatasetRefreshedEvent event, MetaData metaData) {
        LOG.info("MaterializedViewForReferenceDatasetRefreshedEvent event received for dataflowId {}, dataProviderId {}, jobId {}", event.getDataflowId(), event.getDataProviderId(), event.getJobId());
        UpdateMaterializedViewCommand updateMaterializedViewCommand = new UpdateMaterializedViewCommand();
        BeanUtils.copyProperties(event, updateMaterializedViewCommand);

        LinkedHashMap auth = (LinkedHashMap) metaData.get("auth");
        commandGateway.send(GenericCommandMessage.asCommandMessage(updateMaterializedViewCommand).withMetaData(MetaData.with("auth", auth))).exceptionally(er -> {
            LOG.error("Error while executing command UpdateMaterializedViewCommand for dataflowId {}, dataProviderID {}, jobId {}, {}", event.getDataflowId(), event.getDataProviderId(), event.getJobId(), er.getCause().toString());
            UpdateMaterializedViewFailedEvent updateMaterializedViewFailedEvent = new UpdateMaterializedViewFailedEvent();
            BeanUtils.copyProperties(event, updateMaterializedViewFailedEvent);
            eventGateway.publish(updateMaterializedViewFailedEvent, metaData);
            return er;
        });
    }

    @SagaEventHandler(associationProperty = "transactionId")
    public void handle(MaterializedViewShouldBeUpdatedEvent event, MetaData metaData) {
        LOG.info("MaterializedViewShouldBeUpdatedEvent event received for dataflowId {}, dataProviderId {}", event.getDataflowId(), event.getDataProviderId());
        UpdateMaterializedViewCommand updateMaterializedViewCommand = new UpdateMaterializedViewCommand();
        BeanUtils.copyProperties(event, updateMaterializedViewCommand);

        LinkedHashMap auth = (LinkedHashMap) metaData.get("auth");
        commandGateway.send(GenericCommandMessage.asCommandMessage(updateMaterializedViewCommand).withMetaData(MetaData.with("auth", auth))).exceptionally(er -> {
            LOG.error("Error while executing command UpdateMaterializedViewCommand for dataflowId {}, dataProviderID {}, jobId {}, {}", event.getDataflowId(), event.getDataProviderId(), event.getJobId(), er.getCause().toString());
            UpdateMaterializedViewFailedEvent updateMaterializedViewFailedEvent = new UpdateMaterializedViewFailedEvent();
            BeanUtils.copyProperties(event, updateMaterializedViewFailedEvent);
            eventGateway.publish(updateMaterializedViewFailedEvent, metaData);
            return er;
        });
    }

    @SagaEventHandler(associationProperty = "transactionId")
    public void handle(MaterializedViewUpdatedEvent event, MetaData metaData) {
        LOG.info("MaterializedViewUpdatedEvent event received for dataflowId {}, dataProviderId {}, jobId {}", event.getDataflowId(), event.getDataProviderId(), event.getJobId());
        CreateValidationTasksForReleaseCommand createValidationTasksForReleaseCommand = new CreateValidationTasksForReleaseCommand();
        BeanUtils.copyProperties(event, createValidationTasksForReleaseCommand);

        LinkedHashMap auth = (LinkedHashMap) metaData.get("auth");
        commandGateway.send(GenericCommandMessage.asCommandMessage(createValidationTasksForReleaseCommand).withMetaData(MetaData.with("auth", auth))).exceptionally(er -> {
            LOG.error("Error while executing command CreateValidationTasksForReleaseCommand for dataflowId {}, dataProviderId {}, jobId {}, {}", event.getDataflowId(), event.getDataProviderId(), event.getJobId(), er.getCause().toString());
            ValidationTasksForReleaseCreationFailedEvent validationTasksForReleaseCreationFailedEvent = new ValidationTasksForReleaseCreationFailedEvent();
            BeanUtils.copyProperties(event, validationTasksForReleaseCreationFailedEvent);
            eventGateway.publish(validationTasksForReleaseCreationFailedEvent, metaData);
            return er;
        });
    }

    @SagaEventHandler(associationProperty = "transactionId")
    public void handle(ValidationForReleaseFinishedEvent event, MetaData metaData) {
        LOG.info("ValidationForReleaseFinishedEvent event received for dataflowId {}, dataProviderId {}, jobId {}", event.getDataflowId(), event.getDataProviderId(), event.getJobId());
        LinkedHashMap auth = (LinkedHashMap) metaData.get("auth");
        CreateSnapshotRecordRorReleaseInMetabaseCommand createSnapshotRecordRorReleaseInMetabaseCommand = new CreateSnapshotRecordRorReleaseInMetabaseCommand();
        BeanUtils.copyProperties(event, createSnapshotRecordRorReleaseInMetabaseCommand);

        commandGateway.send(GenericCommandMessage.asCommandMessage(createSnapshotRecordRorReleaseInMetabaseCommand).withMetaData(MetaData.with("auth", auth))).exceptionally(e -> {
            LOG.error("Error while executing command CreateSnapshotRecordRorReleaseInMetabaseCommand for dataflow {}, dataProvider {}, jobId {},{}", event.getDataflowId(), event.getDataProviderId(), event.getJobId(), e.getCause().toString());
            CreateSnapshotRecordForReleaseInMetabaseFailedEvent createSnapshotRecordForReleaseInMetabaseFailedEvent = new CreateSnapshotRecordForReleaseInMetabaseFailedEvent();
            BeanUtils.copyProperties(event, createSnapshotRecordForReleaseInMetabaseFailedEvent);
            eventGateway.publish(createSnapshotRecordForReleaseInMetabaseFailedEvent);
            return e;
        });
    }

    @SagaEventHandler(associationProperty = "transactionId")
    public void handle(SnapshotRecordForReleaseCreatedInMetabaseEvent event, MetaData metaData) {
        LOG.info("SnapshotRecordForReleaseCreatedInMetabaseEvent event received for dataflowId {}, dataProviderId {}, jobId {}", event.getDataflowId(), event.getDataProviderId(), event.getJobId());
        LinkedHashMap auth = (LinkedHashMap) metaData.get("auth");
        CreateSnapshotFileForReleaseCommand createSnapshotFileForReleaseCommand = new CreateSnapshotFileForReleaseCommand();
        BeanUtils.copyProperties(event, createSnapshotFileForReleaseCommand);
        createSnapshotFileForReleaseCommand.setRecordStoreReleaseAggregateId(UUID.randomUUID().toString());

        commandGateway.send(createSnapshotFileForReleaseCommand).exceptionally(e -> {
            LOG.error("Error while executing command CreateSnapshotFileForReleaseCommand for dataflow {}, dataProvider {}, jobId {},{}", event.getDataflowId(), event.getDataProviderId(), event.getJobId(), e.getCause().toString());
            CreateSnapshotFIleForReleaseFailedEvent createSnapshotFIleForReleaseFailedEvent = new CreateSnapshotFIleForReleaseFailedEvent();
            BeanUtils.copyProperties(event, createSnapshotFIleForReleaseFailedEvent);
            eventGateway.publish(createSnapshotFIleForReleaseFailedEvent, metaData);
            return e;
        });
    }

    @SagaEventHandler(associationProperty = "transactionId")
    public void handle(SnapshotFileForReleaseCreatedEvent event, MetaData metaData) {
        LOG.info("SnapshotFileForReleaseCreatedEvent event received for dataflowId {}, dataProviderId {}, jobId {}", event.getDataflowId(), event.getDataProviderId(), event.getJobId());
        UpdateDatasetStatusCommand updateDatasetStatusCommand = new UpdateDatasetStatusCommand();
        BeanUtils.copyProperties(event, updateDatasetStatusCommand);

        LinkedHashMap auth = (LinkedHashMap) metaData.get("auth");
        commandGateway.send(GenericCommandMessage.asCommandMessage(updateDatasetStatusCommand).withMetaData(MetaData.with("auth", auth))).exceptionally(e -> {
            LOG.error("Error while executing command UpdateDatasetStatusCommand for dataflow {},dataProvider {}, jobId {}, {}", event.getDataflowId(), event.getDataProviderId(), event.getJobId(), e.getCause().toString());
            UpdateDatasetStatusFailedEvent updateDatasetStatusFailedEvent = new UpdateDatasetStatusFailedEvent();
            BeanUtils.copyProperties(event, updateDatasetStatusFailedEvent);
            eventGateway.publish(updateDatasetStatusFailedEvent, metaData);
            return e;
        });
    }

    @SagaEventHandler(associationProperty = "transactionId")
    public void handle(DatasetStatusUpdatedEvent event, MetaData metaData) {
        LinkedHashMap auth = (LinkedHashMap) metaData.get("auth");
        LOG.info("DatasetStatusUpdatedEvent event received for dataflowId {}, dataProviderId {}, jobId {}", event.getDataflowId(), event.getDataProviderId(), event.getJobId());
        DeleteProviderCommand deleteProviderCommand = new DeleteProviderCommand();
        BeanUtils.copyProperties(event, deleteProviderCommand);

        commandGateway.send(GenericCommandMessage.asCommandMessage(deleteProviderCommand).withMetaData(MetaData.with("auth", auth))).exceptionally(e -> {
            LOG.error("Error while executing command DeleteProviderCommand for dataflowId {}, dataProviderId {}, jobId {}, {}", event.getDataflowId(), event.getDataProviderId(), event.getJobId(), e.getCause().toString());
            DeleteProviderFailedEvent deleteProviderFailedEvent = new DeleteProviderFailedEvent();
            BeanUtils.copyProperties(event, deleteProviderFailedEvent);
            eventGateway.publish(deleteProviderFailedEvent, metaData);
            return e;
        });
    }

    @SagaEventHandler(associationProperty = "transactionId")
    public void handle(ProviderDeletedEvent event, MetaData metaData) {
        LOG.info("ProviderDeletedEvent event received for dataflowId {}, dataProviderId {}, jobId {}", event.getDataflowId(), event.getDataProviderId(), event.getJobId());
        if (event.getDataCollectionForDeletion().size() > 0) {
            DeleteProviderCommand deleteProviderCommand = new DeleteProviderCommand();
            BeanUtils.copyProperties(event, deleteProviderCommand);

            LinkedHashMap auth = (LinkedHashMap) metaData.get("auth");
            commandGateway.send(GenericCommandMessage.asCommandMessage(deleteProviderCommand).withMetaData(MetaData.with("auth", auth))).exceptionally(e -> {
                LOG.error("Error while executing command DeleteProviderCommand for dataflowId {}, dataProviderId {}, jobId {}, {}", event.getDataflowId(), event.getDataProviderId(), event.getJobId(), e.getCause().toString());
                DeleteProviderFailedEvent deleteProviderFailedEvent = new DeleteProviderFailedEvent();
                BeanUtils.copyProperties(event, deleteProviderFailedEvent);
                eventGateway.publish(deleteProviderFailedEvent, metaData);
                return e;
            });
        } else {
            LinkedHashMap auth = (LinkedHashMap) metaData.get("auth");
            UpdateInternalRepresentativeCommand updateInternalRepresentativeCommand = new UpdateInternalRepresentativeCommand();
            BeanUtils.copyProperties(event, updateInternalRepresentativeCommand);

            commandGateway.send(GenericCommandMessage.asCommandMessage(updateInternalRepresentativeCommand).withMetaData(MetaData.with("auth", auth))).exceptionally(e -> {
                LOG.error("Error while executing command UpdateInternalRepresentativeCommand for dataflowId {}, dataProviderId {}, jobId {}, {}", event.getDataflowId(), event.getDataProviderId(), e.getCause().toString());
                RevertDatasetStatusCommand revertDatasetStatusCommand = new RevertDatasetStatusCommand();
                BeanUtils.copyProperties(event, revertDatasetStatusCommand);

                commandGateway.send(GenericCommandMessage.asCommandMessage(revertDatasetStatusCommand).withMetaData(MetaData.with("auth", auth))).exceptionally(er -> {
                    LOG.error("Error while executing command RevertDatasetStatusCommand for dataflow {}, dataProvider {}, jobId {}, {}", event.getDataflowId(), event.getDataProviderId(), er.getCause().toString());
                    return er;
                });
                return e;
            });
        }
    }

    @SagaEventHandler(associationProperty = "transactionId")
    public void handle(InternalRepresentativeUpdatedEvent event, MetaData metaData) {
        LOG.info("InternalRepresentativeUpdatedEvent event received for dataflowId {}, dataProviderId {}, jobId {}", event.getDataflowId(), event.getDataProviderId(), event.getJobId());
        LinkedHashMap auth = (LinkedHashMap) metaData.get("auth");
        UpdateDatasetRunningStatusCommand updateDatasetRunningStatusCommand = new UpdateDatasetRunningStatusCommand();
        BeanUtils.copyProperties(event, updateDatasetRunningStatusCommand);

        commandGateway.send(GenericCommandMessage.asCommandMessage(updateDatasetRunningStatusCommand).withMetaData(MetaData.with("auth", auth))).exceptionally(er -> {
            LOG.error("Error while executing command UpdateDatasetRunningStatusCommand for dataflowId {}, dataProviderId{}, jobId {}, {}", event.getDataflowId(), event.getDataProviderId(), er.getCause().toString());
            RevertInternalRepresentativeCommand revertInternalRepresentativeCommand = new RevertInternalRepresentativeCommand();
            BeanUtils.copyProperties(event, revertInternalRepresentativeCommand);

            commandGateway.send(GenericCommandMessage.asCommandMessage(revertInternalRepresentativeCommand).withMetaData(MetaData.with("auth", auth))).exceptionally(err -> {
                LOG.error("Error while executing command RevertInternalRepresentativeCommand for dataflowId {}, dataProviderId {}, jobId {}, {}", event.getDataflowId(), event.getDataProviderId(), event.getJobId(), er.getCause().toString());
                return err;
            });
            return er;
        });
    }

    @SagaEventHandler(associationProperty = "transactionId")
    public void handle(DatasetRunningStatusUpdatedEvent event, MetaData metaData) {
        LOG.info("DatasetRunningStatusUpdatedEvent event received for dataflowId {}, dataProviderId {}, jobId {}", event.getDataflowId(), event.getDataProviderId(), event.getJobId());
        LinkedHashMap auth = (LinkedHashMap) metaData.get("auth");
        RestoreDataFromSnapshotCommand restoreDataFromSnapshotCommand = new RestoreDataFromSnapshotCommand();
        BeanUtils.copyProperties(event, restoreDataFromSnapshotCommand);
        restoreDataFromSnapshotCommand.setDatasetsReleased(new ArrayList<>());
        restoreDataFromSnapshotCommand.setDatasetToRelease(event.getDatasetIds().get(0));

        commandGateway.send(GenericCommandMessage.asCommandMessage(restoreDataFromSnapshotCommand).withMetaData(MetaData.with("auth", auth))).exceptionally(er -> {
            LOG.error("Error while executing command RestoreDataFromSnapshotCommand for dataflowId {}, dataProviderId {}, jobId {}, {}", event.getDataflowId(), event.getDataProviderId(), event.getJobId(), er.getCause().toString());
            RevertDatasetStatusCommand revertDatasetStatusCommand = new RevertDatasetStatusCommand();
            BeanUtils.copyProperties(event, revertDatasetStatusCommand);

            commandGateway.send(GenericCommandMessage.asCommandMessage(revertDatasetStatusCommand).withMetaData(MetaData.with("auth", auth))).exceptionally(err -> {
                LOG.error("Error while executing command RevertInternalRepresentativeCommand for dataflowId {}, dataProviderId {}, jobId {}, {}", event.getDataflowId(), event.getDataProviderId(), event.getJobId(), er.getCause().toString());
                return err;
            });
            return er;
        });
    }

    @SagaEventHandler(associationProperty = "transactionId")
    public void handle(DataRestoredFromSnapshotEvent event, MetaData metaData) {
        LinkedHashMap auth = (LinkedHashMap) metaData.get("auth");
        List<Long> datasetsToRelease = event.getDatasetIds().stream().filter(id -> !event.getDatasetsReleased().contains(id)).collect(Collectors.toList());
        if (datasetsToRelease.size()>0) {
            RestoreDataFromSnapshotCommand restoreDataFromSnapshotCommand = new RestoreDataFromSnapshotCommand();
            BeanUtils.copyProperties(event, restoreDataFromSnapshotCommand);
            restoreDataFromSnapshotCommand.setDatasetToRelease(datasetsToRelease.get(0));

            commandGateway.send(GenericCommandMessage.asCommandMessage(restoreDataFromSnapshotCommand).withMetaData(MetaData.with("auth", auth))).exceptionally(er -> {
                LOG.error("Error while executing command RestoreDataFromSnapshotCommand for dataflowId {}, dataProviderId {}, jobId {}, {}", event.getDataflowId(), event.getDataProviderId(), event.getJobId(), er.getCause().toString());
                RevertDatasetRunningStatusCommand revertDatasetRunningStatusCommand = new RevertDatasetRunningStatusCommand();
                BeanUtils.copyProperties(event, revertDatasetRunningStatusCommand);

                commandGateway.send(GenericCommandMessage.asCommandMessage(revertDatasetRunningStatusCommand).withMetaData(MetaData.with("auth", auth))).exceptionally(err -> {
                    LOG.error("Error while executing command RevertDatasetRunningStatusCommand for dataflowId {}, dataProviderId {}, jobId {}, {}", event.getDataflowId(), event.getDataProviderId(), event.getJobId(), er.getCause().toString());
                    return err;
                });
                return er;
            });
        } else {
            LOG.info("DataRestoredFromSnapshotEvent event received for dataflowId {}, dataProviderId {}, jobId {}", event.getDataflowId(), event.getDataProviderId(), event.getJobId());
            MarkSnapshotReleasedCommand markSnapshotReleasedCommand = new MarkSnapshotReleasedCommand();
            BeanUtils.copyProperties(event, markSnapshotReleasedCommand);

            commandGateway.send(GenericCommandMessage.asCommandMessage(markSnapshotReleasedCommand).withMetaData(MetaData.with("auth", auth))).exceptionally(er -> {
                LOG.error("Error while executing command MarkSnapshotReleasedCommand for dataflowId {}, dataProviderId {}, jobId {}, {}", event.getDataflowId(), event.getDataProviderId(), event.getJobId(), er.getCause().toString());
                ReleaseFailureRemoveLocksCommand releaseFailureRemoveLocksCommand = new ReleaseFailureRemoveLocksCommand();
                BeanUtils.copyProperties(event, releaseFailureRemoveLocksCommand);

                commandGateway.send(GenericCommandMessage.asCommandMessage(releaseFailureRemoveLocksCommand).withMetaData(MetaData.with("auth", auth))).exceptionally(err -> {
                    LOG.error("Error while executing command ReleaseFailureRemoveLocksCommand for dataflowId {}, dataProviderId {}, jobId {}, {}", event.getDataflowId(), event.getDataProviderId(), event.getJobId(), er.getCause().toString());
                    return err;
                });
                return er;
            });
        }
    }

    @SagaEventHandler(associationProperty = "transactionId")
    public void handle(SnapshotMarkedReleasedEvent event, MetaData metaData) {
        LOG.info("SnapshotMarkedReleasedEvent event received for dataflowId {}, dataProviderId {}, jobId {}", event.getDataflowId(), event.getDataProviderId(), event.getJobId());
        LinkedHashMap auth = (LinkedHashMap) metaData.get("auth");
        UpdateChangesEuDatasetCommand updateChangesEuDatasetCommand = new UpdateChangesEuDatasetCommand();
        BeanUtils.copyProperties(event, updateChangesEuDatasetCommand);

        commandGateway.send(GenericCommandMessage.asCommandMessage(updateChangesEuDatasetCommand).withMetaData(MetaData.with("auth", auth))).exceptionally(er -> {
            LOG.error("Error while executing command UpdateChangesEuDatasetCommand for dataflowId {}, dataProviderId {}, jobId {}, {}", event.getDataflowId(), event.getDataProviderId(), event.getJobId(), er.getCause().toString());
            ReleaseFailureRemoveLocksCommand releaseFailureRemoveLocksCommand = new ReleaseFailureRemoveLocksCommand();
            BeanUtils.copyProperties(event, releaseFailureRemoveLocksCommand);

            commandGateway.send(GenericCommandMessage.asCommandMessage(releaseFailureRemoveLocksCommand).withMetaData(MetaData.with("auth", auth))).exceptionally(err -> {
                LOG.error("Error while executing command ReleaseFailureRemoveLocksCommand for dataflowId {}, dataProviderId {}, jobId {}, {}", event.getDataflowId(), event.getDataProviderId(), event.getJobId(), er.getCause().toString());
                return err;
            });
            return er;
        });
    }

    @SagaEventHandler(associationProperty = "transactionId")
    public void handle(ChangesEuDatasetUpdatedEvent event, MetaData metaData) {
        LOG.info("ChangesEuDatasetUpdatedEvent event received for dataflowId {}, dataProviderId {}, jobId {}", event.getDataflowId(), event.getDataProviderId(), event.getJobId());
        LinkedHashMap auth = (LinkedHashMap) metaData.get("auth");
        SavePublicFilesForReleaseCommand savePublicFilesForReleaseCommand = new SavePublicFilesForReleaseCommand();
        BeanUtils.copyProperties(event, savePublicFilesForReleaseCommand);

        commandGateway.send(GenericCommandMessage.asCommandMessage(savePublicFilesForReleaseCommand).withMetaData(MetaData.with("auth", auth))).exceptionally(er -> {
            LOG.error("Error while executing command SavePublicFilesForReleaseCommand for dataflowId {}, dataProviderId {}, jobId {}, {}", event.getDataflowId(), event.getDataProviderId(), event.getJobId(), er.getCause().toString());
            ReleaseFailureRemoveLocksCommand releaseFailureRemoveLocksCommand = new ReleaseFailureRemoveLocksCommand();
            BeanUtils.copyProperties(event, releaseFailureRemoveLocksCommand);

            commandGateway.send(GenericCommandMessage.asCommandMessage(releaseFailureRemoveLocksCommand).withMetaData(MetaData.with("auth", auth))).exceptionally(err -> {
                LOG.error("Error while executing command ReleaseFailureRemoveLocksCommand for dataflowId {}, dataProviderId {}, jobId {}, {}", event.getDataflowId(), event.getDataProviderId(), event.getJobId(), er.getCause().toString());
                return err;
            });
            return er;
        });
    }

    @SagaEventHandler(associationProperty = "transactionId")
    public void handle(PublicFilesForReleaseSavedEvent event, MetaData metaData) {
        LOG.info("PublicFilesForReleaseSavedEvent event received for dataflowId {}, dataProviderId {}, jobId {}", event.getDataflowId(), event.getDataProviderId(), event.getJobId());
        LinkedHashMap auth = (LinkedHashMap) metaData.get("auth");
        RemoveReleaseLocksCommand removeReleaseLocksCommand = new RemoveReleaseLocksCommand();
        BeanUtils.copyProperties(event, removeReleaseLocksCommand);

        commandGateway.send(GenericCommandMessage.asCommandMessage(removeReleaseLocksCommand).withMetaData(MetaData.with("auth", auth))).exceptionally(er -> {
            LOG.error("Error while executing command RemoveReleaseLocksCommand for dataflowId {}, dataProviderId {}, jobId {}, {}", event.getDataflowId(), event.getDataProviderId(), event.getJobId(), er.getCause().toString());
            SetJobFailedCommand setJobFailedCommand = new SetJobFailedCommand();
            BeanUtils.copyProperties(event, setJobFailedCommand);

            commandGateway.send(setJobFailedCommand).exceptionally(e -> {
                LOG.error("Error while executing command SetJobFailedCommand for dataflowId {}, dataProviderId {}, jobId {}, {}", event.getDataflowId(), event.getDataProviderId(), event.getJobId(), er.getCause().toString());
                return e;
            });
            return er;
        });
    }

    @SagaEventHandler(associationProperty = "transactionId")
    public void handle(ReleaseLocksRemovedEvent event, MetaData metaData) {
        LOG.info("ReleaseLocksRemovedEvent event received for dataflowId {}, dataProviderId {}, jobId {}", event.getDataflowId(), event.getDataProviderId(), event.getJobId());
        LinkedHashMap auth = (LinkedHashMap) metaData.get("auth");
        SetJobFinishedCommand setJobFinishedCommand = new SetJobFinishedCommand();
        BeanUtils.copyProperties(event, setJobFinishedCommand);

        commandGateway.send(GenericCommandMessage.asCommandMessage(setJobFinishedCommand).withMetaData(MetaData.with("auth", auth))).exceptionally(er -> {
            LOG.error("Error while executing command SetJobFinishedCommand for dataflowId {}, dataProviderId {}, jobId {}, {}", event.getDataflowId(), event.getDataProviderId(), event.getJobId(), er.getCause().toString());
            return er;
        });
    }

    @SagaEventHandler(associationProperty = "transactionId")
    public void handle(JobFinishedEvent event, MetaData metaData) {
        LOG.info("JobFinishedEvent event received for dataflowId {}, dataProviderId {}, jobId {}", event.getDataflowId(), event.getDataProviderId(), event.getJobId());
        LinkedHashMap auth = (LinkedHashMap) metaData.get("auth");
        SendEmailForSuccessfulReleaseCommand sendEmailForSuccessfulReleaseCommand = new SendEmailForSuccessfulReleaseCommand();
        BeanUtils.copyProperties(event, sendEmailForSuccessfulReleaseCommand);

        commandGateway.send(GenericCommandMessage.asCommandMessage(sendEmailForSuccessfulReleaseCommand).withMetaData(MetaData.with("auth", auth))).exceptionally(er -> {
            LOG.error("Error while executing command SendEmailForSuccessfulReleaseCommand for dataflowId {}, dataProviderId {}, jobId {}, {}", event.getDataflowId(), event.getDataProviderId(), event.getJobId(), er.getCause().toString());
            return er;
        });
    }

    @SagaEventHandler(associationProperty = "transactionId")
    public void handle(EmailForSuccessfulReleaseSentEvent event, MetaData metaData) {
        LOG.info("EmailForSuccessfulReleaseSentEvent event received for dataflowId {}, dataProviderId {}, jobId {}", event.getDataflowId(), event.getDataProviderId(), event.getJobId());
        LinkedHashMap auth = (LinkedHashMap) metaData.get("auth");
        SendNotificationForSuccessfulReleaseCommand sendNotificationForSuccessfulReleaseCommand = new SendNotificationForSuccessfulReleaseCommand();
        BeanUtils.copyProperties(event, sendNotificationForSuccessfulReleaseCommand);

        commandGateway.send(GenericCommandMessage.asCommandMessage(sendNotificationForSuccessfulReleaseCommand).withMetaData(MetaData.with("auth", auth))).exceptionally(er -> {
            LOG.error("Error while executing command SendNotificationForSuccessfulReleaseCommand for dataflowId {}, dataProviderId {}, jobId {}, {}", event.getDataflowId(), event.getDataProviderId(), event.getJobId(), er.getCause().toString());
            return er;
        });
    }

    @EndSaga
    @SagaEventHandler(associationProperty = "transactionId")
    public void handle(NotificationForSuccessfulReleaseSentEvent event, MetaData metaData) {
        LOG.info("NotificationForSuccessfulReleaseSentEvent event received for dataflowId {}, dataProviderId {}, jobId {}", event.getDataflowId(), event.getDataProviderId(), event.getJobId());
        LinkedHashMap auth = (LinkedHashMap) metaData.get("auth");
        CreateMessageForSuccessfulReleaseCommand createMessageForSuccessfulReleaseCommand = new CreateMessageForSuccessfulReleaseCommand();
        BeanUtils.copyProperties(event, createMessageForSuccessfulReleaseCommand);
        createMessageForSuccessfulReleaseCommand.setCollaborationReleaseAggregateId(UUID.randomUUID().toString());

        commandGateway.send(GenericCommandMessage.asCommandMessage(createMessageForSuccessfulReleaseCommand).withMetaData(MetaData.with("auth", auth))).exceptionally(er -> {
            LOG.error("Error while executing command CreateMessageForSuccessfulReleaseCommand for dataflowId {}, dataProviderId {}, jobId {}, {}", event.getDataflowId(), event.getDataProviderId(), event.getJobId(), er.getCause().toString());
            return er;
        });
    }

    @SagaEventHandler(associationProperty = "transactionId")
    public void handle(DataRestoredFromSnapshotFailedEvent event) {
        LOG.info("DataRestoredFromSnapshotFailedEvent event received for dataflowId {}, dataProviderId {}, jobId {}", event.getDataflowId(), event.getDataProviderId(), event.getJobId());
    }

    @SagaEventHandler(associationProperty = "transactionId")
    public void handle(ValidationTasksForReleaseCanceledEvent event, MetaData metaData) {
        LOG.info("ValidationTasksForReleaseCanceledEvent event received for dataflowId {}, dataProviderId {}, jobId {}", event.getDataflowId(), event.getDataProviderId(), event.getJobId());
        LinkedHashMap auth = (LinkedHashMap) metaData.get("auth");
        CancelValidationProcessForReleaseCommand cancelValidationProcessForReleaseCommand = new CancelValidationProcessForReleaseCommand();
        BeanUtils.copyProperties(event, cancelValidationProcessForReleaseCommand);

        commandGateway.send(GenericCommandMessage.asCommandMessage(cancelValidationProcessForReleaseCommand).withMetaData(MetaData.with("auth", auth))).exceptionally(e -> {
            LOG.error("Error while executing command CancelValidationProcessForReleaseCommand for dataflowId {}, dataProviderId {}, jobId {}, {}", event.getDataflowId(), event.getDataProviderId(), event.getJobId(), e.getCause().toString());
            CancelValidationProcessForReleaseFailedEvent cancelValidationProcessForReleaseFailedEvent = new CancelValidationProcessForReleaseFailedEvent();
            BeanUtils.copyProperties(event, cancelValidationProcessForReleaseFailedEvent);
            eventGateway.publish(cancelValidationProcessForReleaseFailedEvent, metaData);
            return e;
        });
    }

    @SagaEventHandler(associationProperty = "transactionId")
    public void handle(ValidationTasksForReleaseCreationFailedEvent event, MetaData metaData) {
        LOG.info("ValidationTasksForReleaseCreationFailedEvent event received for dataflowId {}, dataProviderId {}, jobId {}", event.getDataflowId(), event.getDataProviderId(), event.getJobId());
        LinkedHashMap auth = (LinkedHashMap) metaData.get("auth");
        CancelValidationTasksForReleaseCommand cancelValidationTasksForReleaseCommand = new CancelValidationTasksForReleaseCommand();
        BeanUtils.copyProperties(event, cancelValidationTasksForReleaseCommand);

        commandGateway.send(GenericCommandMessage.asCommandMessage(cancelValidationTasksForReleaseCommand).withMetaData(MetaData.with("auth", auth))).exceptionally(e -> {
            LOG.error("Error while executing command CancelValidationProcessForReleaseCommand for dataflowId {}, dataProviderId {}, jobId {}, {}", event.getDataflowId(), event.getDataProviderId(), event.getJobId(), e.getCause().toString());
            CancelValidationTasksForReleaseFailedEvent cancelValidationTasksForReleaseFailedEvent = new CancelValidationTasksForReleaseFailedEvent();
            BeanUtils.copyProperties(event, cancelValidationTasksForReleaseFailedEvent);
            eventGateway.publish(cancelValidationTasksForReleaseFailedEvent, metaData);
            return e;
        });
    }

    @SagaEventHandler(associationProperty = "transactionId")
    public void handle(CancelValidationTasksForReleaseFailedEvent event, MetaData metaData) {
        LOG.info("CancelValidationTasksForReleaseFailedEvent event received for dataflowId {}, dataProviderId {}, jobId {}", event.getDataflowId(), event.getDataProviderId(), event.getJobId());
        LinkedHashMap auth = (LinkedHashMap) metaData.get("auth");
        RevertRepresentativeVisibilityCommand revertRepresentativeVisibilityCommand = new RevertRepresentativeVisibilityCommand();
        BeanUtils.copyProperties(event, revertRepresentativeVisibilityCommand);

        commandGateway.send(GenericCommandMessage.asCommandMessage(revertRepresentativeVisibilityCommand).withMetaData(MetaData.with("auth", auth))).exceptionally(err -> {
            LOG.error("Error while executing command RevertRepresentativeVisibilityCommand for dataflowId {}, dataProviderId {}, jobId {}, {}", event.getDataflowId(), event.getDataProviderId(), event.getJobId(), err.getCause().toString());
            return err;
        });
    }

    @SagaEventHandler(associationProperty = "transactionId")
    public void handle(ValidationProcessForReleaseFailedEvent event, MetaData metaData) {
        LOG.info("NotificationForSuccessfulReleaseSentEvent event received for dataflowId {}, dataProviderId {}, jobId {}", event.getDataflowId(), event.getDataProviderId(), event.getJobId());
        LinkedHashMap auth = (LinkedHashMap) metaData.get("auth");
        CancelValidationProcessForReleaseCommand cancelValidationProcessForReleaseCommand = new CancelValidationProcessForReleaseCommand();
        BeanUtils.copyProperties(event, cancelValidationProcessForReleaseCommand);

        commandGateway.send(GenericCommandMessage.asCommandMessage(cancelValidationProcessForReleaseCommand).withMetaData(MetaData.with("auth", auth))).exceptionally(e -> {
            LOG.error("Error while executing command CancelValidationProcessForReleaseCommand for dataflowId {}, dataProviderId {}, jobId {}, {}", event.getDataflowId(), event.getDataProviderId(), event.getJobId(), e.getCause().toString());
            CancelValidationProcessForReleaseFailedEvent cancelValidationProcessForReleaseFailedEvent = new CancelValidationProcessForReleaseFailedEvent();
            BeanUtils.copyProperties(event, cancelValidationProcessForReleaseFailedEvent);
            eventGateway.publish(cancelValidationProcessForReleaseFailedEvent, metaData);
            return e;
        });
    }

    @SagaEventHandler(associationProperty = "transationId")
    public void handle(UpdateMaterializedViewFailedEvent event, MetaData metaData) {
        LOG.info("UpdateMaterializedViewFailedEvent event received for dataflowId {}, dataProviderId {}, jobId {}", event.getDataflowId(), event.getDataProviderId(), event.getJobId());
        LinkedHashMap auth = (LinkedHashMap) metaData.get("auth");
        CancelValidationProcessForReleaseCommand cancelValidationProcessForReleaseCommand = new CancelValidationProcessForReleaseCommand();
        BeanUtils.copyProperties(event, cancelValidationProcessForReleaseCommand);

        commandGateway.send(GenericCommandMessage.asCommandMessage(cancelValidationProcessForReleaseCommand).withMetaData(MetaData.with("auth", auth))).exceptionally(e -> {
            LOG.error("Error while executing command CancelValidationProcessForReleaseCommand for dataflowId {}, dataProviderId {}, jobId {}, {}", event.getDataflowId(), event.getDataProviderId(), event.getJobId(), e.getCause().toString());
            CancelValidationProcessForReleaseFailedEvent cancelValidationProcessForReleaseFailedEvent = new CancelValidationProcessForReleaseFailedEvent();
            BeanUtils.copyProperties(event, cancelValidationProcessForReleaseFailedEvent);
            eventGateway.publish(cancelValidationProcessForReleaseFailedEvent, metaData);
            return e;
        });
    }

    @SagaEventHandler(associationProperty = "transactionId")
    public void handle(RefreshMaterializedViewForReferencedDatasetFailedEvent event, MetaData metaData) {
        LOG.info("RefreshMaterializedViewForReferencedDatasetFailedEvent event received for dataflowId {}, dataProviderId {}, jobId {}", event.getDataflowId(), event.getDataProviderId(), event.getJobId());
        LinkedHashMap auth = (LinkedHashMap) metaData.get("auth");
        CancelValidationProcessForReleaseCommand cancelValidationProcessForReleaseCommand = new CancelValidationProcessForReleaseCommand();
        BeanUtils.copyProperties(event, cancelValidationProcessForReleaseCommand);

        commandGateway.send(GenericCommandMessage.asCommandMessage(cancelValidationProcessForReleaseCommand).withMetaData(MetaData.with("auth", auth))).exceptionally(e -> {
            LOG.error("Error while executing command CancelValidationProcessForReleaseCommand for dataflowId {}, dataProviderId {}, jobId {}, {}", event.getDataflowId(), event.getDataProviderId(), event.getJobId(), e.getCause().toString());
            CancelValidationProcessForReleaseFailedEvent cancelValidationProcessForReleaseFailedEvent = new CancelValidationProcessForReleaseFailedEvent();
            BeanUtils.copyProperties(event, cancelValidationProcessForReleaseFailedEvent);
            eventGateway.publish(cancelValidationProcessForReleaseFailedEvent, metaData);
            return e;
        });
    }

    @SagaEventHandler(associationProperty = "transactionId")
    public void handle(CancelValidationProcessForReleaseFailedEvent event, MetaData metaData) {
        LOG.info("CancelValidationProcessForReleaseFailedEvent event received for dataflowId {}, dataProviderId {}, jobId {}", event.getDataflowId(), event.getDataProviderId(), event.getJobId());
        LinkedHashMap auth = (LinkedHashMap) metaData.get("auth");
        RevertRepresentativeVisibilityCommand revertRepresentativeVisibilityCommand = new RevertRepresentativeVisibilityCommand();
        BeanUtils.copyProperties(event, revertRepresentativeVisibilityCommand);

        commandGateway.send(GenericCommandMessage.asCommandMessage(revertRepresentativeVisibilityCommand).withMetaData(MetaData.with("auth", auth))).exceptionally(e -> {
            LOG.error("Error while executing command RevertRepresentativeVisibilityCommand for dataflowId {}, dataProviderId {}, jobId {}, {}", event.getDataflowId(), event.getDataProviderId(), event.getJobId(), e.getCause().toString());
            return e;
        });
    }

    @SagaEventHandler(associationProperty = "transactionId")
    public void handle(RepresentativeVisibilityUpdateFailedEvent event, MetaData metaData) {
        LOG.info("RepresentativeVisibilityUpdateFailedEvent event received for dataflowId {}, dataProviderId {}, jobId {}", event.getDataflowId(), event.getDataProviderId(), event.getJobId());
        LinkedHashMap auth = (LinkedHashMap) metaData.get("auth");
        ReleaseFailureRemoveLocksCommand releaseFailureRemoveLocksCommand = new ReleaseFailureRemoveLocksCommand();
        BeanUtils.copyProperties(event, releaseFailureRemoveLocksCommand);

        commandGateway.send(GenericCommandMessage.asCommandMessage(releaseFailureRemoveLocksCommand).withMetaData(MetaData.with("auth", auth))).exceptionally(e -> {
            LOG.error("Error while executing command ReleaseFailureRemoveLocksCommand for dataflowId {}, dataProviderId {}, jobId {}, {}", event.getDataflowId(), event.getDataProviderId(), e.getCause().toString());
            return e;
        });
    }

    @SagaEventHandler(associationProperty = "transactionId")
    public void handle(ValidationProcessForReleaseCanceledEvent event, MetaData metaData) {
        LOG.info("ValidationProcessForReleaseCanceledEvent event received for dataflowId {}, dataProviderId {}, jobId {}", event.getDataflowId(), event.getDataProviderId(), event.getJobId());
        LinkedHashMap auth = (LinkedHashMap) metaData.get("auth");
        RevertRepresentativeVisibilityCommand revertRepresentativeVisibilityCommand = new RevertRepresentativeVisibilityCommand();
        BeanUtils.copyProperties(event, revertRepresentativeVisibilityCommand);

        commandGateway.send(GenericCommandMessage.asCommandMessage(revertRepresentativeVisibilityCommand).withMetaData(MetaData.with("auth", auth))).exceptionally(err -> {
            LOG.error("Error while executing command CancelValidationProcessForReleaseCommand for dataflowId {}, dataProviderId {}, jobId {}, {}", event.getDataflowId(), event.getDataProviderId(), event.getJobId(), err.getCause().toString());
            return err;
        });
    }

    @SagaEventHandler(associationProperty = "transactionId")
    public void handle(DatasetRunningStatusRevertedEvent event, MetaData metaData) {
        LOG.info("DatasetRunningStatusRevertedEvent event received for dataflowId {}, dataProviderId {}, jobId {}", event.getDataflowId(), event.getDataProviderId(), event.getJobId());
        LinkedHashMap auth = (LinkedHashMap) metaData.get("auth");
        RevertInternalRepresentativeCommand revertInternalRepresentativeCommand = new RevertInternalRepresentativeCommand();
        BeanUtils.copyProperties(event, revertInternalRepresentativeCommand);

        commandGateway.send(GenericCommandMessage.asCommandMessage(revertInternalRepresentativeCommand).withMetaData(MetaData.with("auth", auth))).exceptionally(err -> {
            LOG.error("Error while executing command RevertInternalRepresentativeCommand for dataflowId {}, dataProviderId {}, jobId {}, {}", event.getDataflowId(), event.getDataProviderId(), event.getJobId(), err.getCause().toString());
            return err;
        });
    }

    @SagaEventHandler(associationProperty = "transactionId")
    public void handle(InternalRepresentativeRevertedEvent event, MetaData metaData) {
        LOG.info("InternalRepresentativeRevertedEvent event received for dataflowId {}, dataProviderId {}, jobId {}", event.getDataflowId(), event.getDataProviderId(), event.getJobId());
        LinkedHashMap auth = (LinkedHashMap) metaData.get("auth");
        RevertDatasetStatusCommand revertDatasetStatusCommand = new RevertDatasetStatusCommand();
        BeanUtils.copyProperties(event, revertDatasetStatusCommand);

        commandGateway.send(GenericCommandMessage.asCommandMessage(revertDatasetStatusCommand).withMetaData(MetaData.with("auth", auth))).exceptionally(err -> {
            LOG.error("Error while executing command RevertDatasetStatusCommand for dataflowId {}, dataProviderId {}, jobId {}, {}", event.getDataflowId(), event.getDataProviderId(), event.getJobId(), err.getCause().toString());
            return err;
        });
    }

    @SagaEventHandler(associationProperty = "transactionId")
    public void handle(UpdateDatasetStatusFailedEvent event, MetaData metaData) {
        LOG.info("UpdateDatasetStatusFailedEvent event received for dataflowId {}, dataProviderId {}, jobId {}", event.getDataflowId(), event.getDataProviderId(), event.getJobId());
        LinkedHashMap auth = (LinkedHashMap) metaData.get("auth");
        CancelReleaseProcessCommand cancelReleaseProcessCommand = new CancelReleaseProcessCommand();
        BeanUtils.copyProperties(event, cancelReleaseProcessCommand);

        commandGateway.send(GenericCommandMessage.asCommandMessage(cancelReleaseProcessCommand).withMetaData(MetaData.with("auth", auth))).exceptionally(err -> {
            LOG.error("Error while executing command CancelReleaseProcessCommand for dataflowId {}, dataProviderId {}, jobId {}, {}", event.getDataflowId(), event.getDataProviderId(), event.getJobId(), err.getCause().toString());
            CancelReleaseProcessFailedEvent cancelReleaseProcessFailedEvent = new CancelReleaseProcessFailedEvent();
            BeanUtils.copyProperties(event, cancelReleaseProcessFailedEvent);
            eventGateway.publish(cancelReleaseProcessFailedEvent, metaData);
            return err;
        });
    }

    @SagaEventHandler(associationProperty = "transactionId")
    public void handle(DeleteProviderFailedEvent event, MetaData metaData) {
        LOG.info("DeleteProviderFailedEvent event received for dataflowId {}, dataProviderId {}, jobId {}", event.getDataflowId(), event.getDataProviderId(), event.getJobId());
        LinkedHashMap auth = (LinkedHashMap) metaData.get("auth");
        RevertDatasetStatusCommand revertDatasetStatusCommand = new RevertDatasetStatusCommand();
        BeanUtils.copyProperties(event, revertDatasetStatusCommand);

        commandGateway.send(GenericCommandMessage.asCommandMessage(revertDatasetStatusCommand).withMetaData(MetaData.with("auth", auth))).exceptionally(er -> {
            LOG.error("Error while executing command RevertDatasetStatusCommand for dataflow {}, dataProvider {}, jobId {}, {}", event.getDataflowId(), event.getDataProviderId(), er.getCause().toString());
            RevertDatasetStatusFailedEvent revertDatasetStatusFailedEvent = new RevertDatasetStatusFailedEvent();
            BeanUtils.copyProperties(event, revertDatasetStatusFailedEvent);
            eventGateway.publish(revertDatasetStatusFailedEvent, metaData);
            return er;
        });
    }

    @SagaEventHandler(associationProperty = "transactionId")
    public void handle(RevertDatasetStatusFailedEvent event, MetaData metaData) {
        LOG.info("RevertDatasetStatusFailedEvent event received for dataflowId {}, dataProviderId {}, jobId {}", event.getDataflowId(), event.getDataProviderId(), event.getJobId());
        LinkedHashMap auth = (LinkedHashMap) metaData.get("auth");
        CancelReleaseProcessCommand cancelReleaseProcessCommand = new CancelReleaseProcessCommand();
        BeanUtils.copyProperties(event, cancelReleaseProcessCommand);

        commandGateway.send(GenericCommandMessage.asCommandMessage(cancelReleaseProcessCommand).withMetaData(MetaData.with("auth", auth))).exceptionally(err -> {
            LOG.error("Error while executing command CancelReleaseProcessCommand for dataflow {}, dataProvider {}, jobId {}, {}", event.getDataflowId(), event.getDataProviderId(), err.getCause().toString());
            CancelReleaseProcessFailedEvent cancelReleaseProcessFailedEvent = new CancelReleaseProcessFailedEvent();
            BeanUtils.copyProperties(event, cancelReleaseProcessFailedEvent);
            eventGateway.publish(cancelReleaseProcessFailedEvent, metaData);
            return err;
        });
    }

    @SagaEventHandler(associationProperty = "transactionId")
    public void handle(DatasetStatusRevertedEvent event, MetaData metaData) {
        LOG.info("DatasetStatusRevertedEvent event received for dataflowId {}, dataProviderId {}, jobId {}", event.getDataflowId(), event.getDataProviderId(), event.getJobId());
        LinkedHashMap auth = (LinkedHashMap) metaData.get("auth");
        CancelReleaseProcessCommand cancelReleaseProcessCommand = new CancelReleaseProcessCommand();
        BeanUtils.copyProperties(event, cancelReleaseProcessCommand);

        commandGateway.send(GenericCommandMessage.asCommandMessage(cancelReleaseProcessCommand).withMetaData(MetaData.with("auth", auth))).exceptionally(err -> {
            LOG.error("Error while executing command CancelReleaseProcessCommand for dataflowId {}, dataProviderId {}, jobId {}, {}", event.getDataflowId(), event.getDataProviderId(), event.getJobId(), err.getCause().toString());
            CancelReleaseProcessFailedEvent cancelReleaseProcessFailedEvent = new CancelReleaseProcessFailedEvent();
            BeanUtils.copyProperties(event, cancelReleaseProcessFailedEvent);
            eventGateway.publish(cancelReleaseProcessFailedEvent, metaData);
            return err;
        });
    }

    @SagaEventHandler(associationProperty = "transactionId")
    public void handle(CreateSnapshotFIleForReleaseFailedEvent event, MetaData metaData) {
        LOG.info("CreateSnapshotFIleForReleaseFailedEvent event received for dataflowId {}, dataProviderId {}, jobId {}", event.getDataflowId(), event.getDataProviderId(), event.getJobId());
        LinkedHashMap auth = (LinkedHashMap) metaData.get("auth");
        CancelReleaseProcessCommand cancelReleaseProcessCommand = new CancelReleaseProcessCommand();
        BeanUtils.copyProperties(event, cancelReleaseProcessCommand);

        commandGateway.send(GenericCommandMessage.asCommandMessage(cancelReleaseProcessCommand).withMetaData(MetaData.with("auth", auth))).exceptionally(er -> {
            LOG.error("Error while executing command CancelReleaseProcessCommand for dataflow {}, dataProvider {}, jobId {}, {}", event.getDataflowId(), event.getDataProviderId(), er.getCause().toString());
            CancelReleaseProcessFailedEvent cancelReleaseProcessFailedEvent = new CancelReleaseProcessFailedEvent();
            BeanUtils.copyProperties(event, cancelReleaseProcessFailedEvent);
            eventGateway.publish(cancelReleaseProcessFailedEvent, metaData);
            return er;
        });
    }

    @SagaEventHandler(associationProperty = "transactionId")
    public void handle(ReleaseProcessCancelledEvent event, MetaData metaData) {
        LOG.info("ReleaseProcessCancelledEvent event received for dataflowId {}, dataProviderId {}, jobId {}", event.getDataflowId(), event.getDataProviderId(), event.getJobId());
        LinkedHashMap auth = (LinkedHashMap) metaData.get("auth");
        RevertRepresentativeVisibilityCommand revertRepresentativeVisibilityCommand = new RevertRepresentativeVisibilityCommand();
        BeanUtils.copyProperties(event, revertRepresentativeVisibilityCommand);

        commandGateway.send(GenericCommandMessage.asCommandMessage(revertRepresentativeVisibilityCommand).withMetaData(MetaData.with("auth", auth))).exceptionally(er -> {
            LOG.error("Error while executing command ReleaseFailureRemoveLocksCommand for dataflowId {}, dataProviderId {}, jobId {}, {}", event.getDataflowId(), event.getDataProviderId(), event.getJobId(), er.getCause().toString());
            return er;
        });
    }

    @SagaEventHandler(associationProperty = "transactionId")
    public void handle(CreateSnapshotRecordForReleaseInMetabaseFailedEvent event, MetaData metaData) {
        LOG.info("CreateSnapshotRecordForReleaseInMetabaseFailedEvent event received for dataflowId {}, dataProviderId {}, jobId {}", event.getDataflowId(), event.getDataProviderId(), event.getJobId());
        LinkedHashMap auth = (LinkedHashMap) metaData.get("auth");
        RevertRepresentativeVisibilityCommand revertRepresentativeVisibilityCommand = new RevertRepresentativeVisibilityCommand();
        BeanUtils.copyProperties(event, revertRepresentativeVisibilityCommand);

        commandGateway.send(GenericCommandMessage.asCommandMessage(revertRepresentativeVisibilityCommand).withMetaData(MetaData.with("auth", auth))).exceptionally(err -> {
            LOG.error("Error while executing command RevertRepresentativeVisibilityCommand for dataflow {}, dataProvider {}, jobId {}, {}", event.getDataflowId(), event.getDataProviderId(), err.getCause().toString());
            return err;
        });
    }

    @SagaEventHandler(associationProperty = "transactionId")
    public void handle(CancelReleaseProcessFailedEvent event, MetaData metaData) {
        LOG.info("CancelReleaseProcessFailedEvent event received for dataflowId {}, dataProviderId {}, jobId {}", event.getDataflowId(), event.getDataProviderId(), event.getJobId());
        LinkedHashMap auth = (LinkedHashMap) metaData.get("auth");
        RevertRepresentativeVisibilityCommand revertRepresentativeVisibilityCommand = new RevertRepresentativeVisibilityCommand();
        BeanUtils.copyProperties(event, revertRepresentativeVisibilityCommand);

        commandGateway.send(GenericCommandMessage.asCommandMessage(revertRepresentativeVisibilityCommand).withMetaData(MetaData.with("auth", auth))).exceptionally(err -> {
            LOG.error("Error while executing command RevertRepresentativeVisibilityCommand for dataflow {}, dataProvider {}, jobId {}, {}", event.getDataflowId(), event.getDataProviderId(), err.getCause().toString());
            return err;
        });
    }

    @SagaEventHandler(associationProperty = "transactionId")
    public void handle(RepresentativeVisibilityRevertedEvent event, MetaData metaData) {
        LOG.info("RepresentativeVisibilityRevertedEvent event received for dataflowId {}, dataProviderId {}, jobId {}", event.getDataflowId(), event.getDataProviderId(), event.getJobId());
        LinkedHashMap auth = (LinkedHashMap) metaData.get("auth");
        ReleaseFailureRemoveLocksCommand releaseFailureRemoveLocksCommand = new ReleaseFailureRemoveLocksCommand();
        BeanUtils.copyProperties(event, releaseFailureRemoveLocksCommand);

        commandGateway.send(GenericCommandMessage.asCommandMessage(releaseFailureRemoveLocksCommand).withMetaData(MetaData.with("auth", auth))).exceptionally(er -> {
            LOG.error("Error while executing command ReleaseFailureRemoveLocksCommand for dataflowId {}, dataProviderId {}, jobId {}, {}", event.getDataflowId(), event.getDataProviderId(), event.getJobId(), er.getCause().toString());
            return er;
        });
    }

    @SagaEventHandler(associationProperty = "transactionId")
    public void handle(FailureReleaseLocksRemovedEvent event) {
        LOG.info("FailureReleaseLocksRemovedEvent event received for dataflowId {}, dataProviderId {}, jobId {}", event.getDataflowId(), event.getDataProviderId(), event.getJobId());
        SetJobFailedCommand setJobFailedCommand = new SetJobFailedCommand();
        BeanUtils.copyProperties(event, setJobFailedCommand);

        commandGateway.send(setJobFailedCommand).exceptionally(er -> {
            LOG.error("Error while executing command SetJobFailedCommand for dataflowId {}, dataProviderId {}, jobId {}, {}", event.getDataflowId(), event.getDataProviderId(), event.getJobId(), er.getCause().toString());
            return er;
        });
    }

    @SagaEventHandler(associationProperty = "transactionId")
    public void handle(JobFailedEvent event) {
        LOG.info("JobFailedEvent event received for dataflowId {}, dataProviderId {}, jobId {}", event.getDataflowId(), event.getDataProviderId(), event.getJobId());
    }

}














