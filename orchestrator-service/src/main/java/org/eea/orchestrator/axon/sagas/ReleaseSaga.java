package org.eea.orchestrator.axon.sagas;

import org.axonframework.commandhandling.GenericCommandMessage;
import org.axonframework.commandhandling.gateway.CommandGateway;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Saga
public class ReleaseSaga {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(ReleaseSaga.class);

    @Autowired
    private transient CommandGateway commandGateway;

    @StartSaga
    @SagaEventHandler(associationProperty = "transactionId")
    public void handle(ReleaseStartNotificationCreatedEvent event) {
        SendUserNotificationForReleaseStartedCommand sendUserNotificationForReleaseStartedCommand = SendUserNotificationForReleaseStartedCommand.builder().communicationReleaseAggregateId(UUID.randomUUID().toString()).transactionId(event.getTransactionId()).dataflowId(event.getDataflowId())
                .dataProviderId(event.getDataProviderId()).restrictFromPublic(event.isRestrictFromPublic()).validate(event.isValidate()).build();
        commandGateway.send(sendUserNotificationForReleaseStartedCommand).exceptionally(er -> {
            LOG.error(er.getCause().toString());
            return er;
        });
    }

    @SagaEventHandler(associationProperty = "transactionId")
    public void handle(UserNotifationForReleaseSentEvent event, MetaData metaData) {
        AddReleaseLocksCommand addReleaseLocksCommand = AddReleaseLocksCommand.builder().datasetReleaseAggregateId(UUID.randomUUID().toString()).transactionId(event.getTransactionId()).dataflowId(event.getDataflowId())
                .dataProviderId(event.getDataProviderId()).restrictFromPublic(event.isRestrictFromPublic()).validate(event.isValidate()).communicationReleaseAggregateId(event.getCommunicationReleaseAggregateId())
                .releaseAggregateId(event.getReleaseAggregateId()).build();

        LinkedHashMap auth = (LinkedHashMap) metaData.get("auth");
        commandGateway.send(GenericCommandMessage.asCommandMessage(addReleaseLocksCommand).withMetaData(MetaData.with("auth", auth))).exceptionally(er -> {
            LOG.error(er.getCause().toString());
            ReleaseFailureRemoveLocksCommand releaseFailureRemoveLocksCommand = ReleaseFailureRemoveLocksCommand.builder().datasetReleaseAggregateId(UUID.randomUUID().toString()).transactionId(event.getTransactionId()).dataflowId(event.getDataflowId())
                    .dataProviderId(event.getDataProviderId()).restrictFromPublic(event.isRestrictFromPublic()).validate(event.isValidate()).communicationReleaseAggregateId(event.getCommunicationReleaseAggregateId())
                    .releaseAggregateId(event.getReleaseAggregateId()).build();
            commandGateway.send(releaseFailureRemoveLocksCommand);
            return er;
        });
    }

    @SagaEventHandler(associationProperty = "transactionId")
    public void handle(ReleaseLocksAddedEvent event, MetaData metaData) {
        UpdateRepresentativeVisibilityCommand updateRepresentativeVisibilityCommand = UpdateRepresentativeVisibilityCommand.builder()
                .dataflowReleaseAggregateId(UUID.randomUUID().toString()).transactionId(event.getTransactionId()).dataflowId(event.getDataflowId()).dataProviderId(event.getDataProviderId()).restrictFromPublic(event.isRestrictFromPublic())
                .communicationReleaseAggregateId(event.getCommunicationReleaseAggregateId()).datasetReleaseAggregateId(event.getDatasetReleaseAggregateId()).validate(event.isValidate()).datasetIds(event.getDatasetIds())
                .releaseAggregateId(event.getReleaseAggregateId()).build();

        LinkedHashMap auth = (LinkedHashMap) metaData.get("auth");
        commandGateway.send(GenericCommandMessage.asCommandMessage(updateRepresentativeVisibilityCommand).withMetaData(MetaData.with("auth", auth))).exceptionally(er -> {
            LOG.error(er.getCause().toString());
            return er;
        });
    }

    @SagaEventHandler(associationProperty = "transactionId")
    public void handle(RepresentativeVisibilityUpdatedEvent event, MetaData metaData) {
        LinkedHashMap auth = (LinkedHashMap) metaData.get("auth");
        if (!isAdmin(metaData) || event.isValidate()) {
            CreateValidationProcessForReleaseCommand executeValidationProcessCommand = CreateValidationProcessForReleaseCommand.builder().recordStoreReleaseAggregateId(UUID.randomUUID().toString()).transactionId(event.getTransactionId())
                    .dataflowId(event.getDataflowId()).dataProviderId(event.getDataProviderId()).restrictFromPublic(event.isRestrictFromPublic()).validate(event.isValidate()).datasetIds(event.getDatasetIds()).communicationReleaseAggregateId(event.getCommunicationReleaseAggregateId())
                    .datasetReleaseAggregateId(event.getDatasetReleaseAggregateId()).dataflowReleaseAggregateId(event.getDataflowReleaseAggregateId()).releaseAggregateId(event.getReleaseAggregateId()).build();
            commandGateway.send(GenericCommandMessage.asCommandMessage(executeValidationProcessCommand).withMetaData(MetaData.with("auth", auth))).exceptionally(er -> {
                LOG.error(er.getCause().toString());
                return er;
            });
        } else {
            CreateSnapshotRecordRorReleaseInMetabaseCommand createReleaseSnapshotCommand = CreateSnapshotRecordRorReleaseInMetabaseCommand.builder().datasetReleaseAggregateId(event.getDatasetReleaseAggregateId()).transactionId(event.getTransactionId())
                    .dataflowId(event.getDataflowId()).dataProviderId(event.getDataProviderId()).restrictFromPublic(event.isRestrictFromPublic()).validate(event.isValidate()).datasetIds(event.getDatasetIds())
                    .dataflowReleaseAggregateId(event.getDataflowReleaseAggregateId()).communicationReleaseAggregateId(event.getCommunicationReleaseAggregateId()).recordStoreReleaseAggregateId(event.getRecordStoreReleaseAggregateId())
                    .releaseAggregateId(event.getReleaseAggregateId()).build();
            commandGateway.send(GenericCommandMessage.asCommandMessage(createReleaseSnapshotCommand).withMetaData(MetaData.with("auth", auth))).exceptionally(er -> {
                LOG.error(er.getCause().toString());
                return er;
            });
        }
    }

    private boolean isAdmin(MetaData metaData) {
        String roleAdmin = "ROLE_" + SecurityRoleEnum.ADMIN;
        LinkedHashMap auth = (LinkedHashMap) metaData.get("auth");
        List<LinkedHashMap<String,GrantedAuthority>> authorities = (List<LinkedHashMap<String, GrantedAuthority>>) auth.get("authorities");
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
        LOG.info("Validation process added for datasets {} of dataflow {} ", event.getDatasetIds(), event.getDataflowId());
        CreateValidationTasksForReleaseCommand createValidationTasksForReleaseCommand = CreateValidationTasksForReleaseCommand.builder().validationReleaseAggregateId(UUID.randomUUID().toString()).transactionId(event.getTransactionId())
                .dataflowId(event.getDataflowId()).dataProviderId(event.getDataProviderId()).restrictFromPublic(event.isRestrictFromPublic()).validate(event.isValidate()).datasetIds(event.getDatasetIds()).datasetProcessId(event.getDatasetProcessId())
                .communicationReleaseAggregateId(event.getCommunicationReleaseAggregateId()).dataflowReleaseAggregateId(event.getDataflowReleaseAggregateId()).recordStoreReleaseAggregateId(event.getRecordStoreReleaseAggregateId())
                .datasetReleaseAggregateId(event.getDatasetReleaseAggregateId()).releaseAggregateId(event.getReleaseAggregateId()).build();
        LinkedHashMap auth = (LinkedHashMap) metaData.get("auth");
        commandGateway.send(GenericCommandMessage.asCommandMessage(createValidationTasksForReleaseCommand).withMetaData(MetaData.with("auth", auth))).exceptionally(er -> {
            LOG.error(er.getCause().toString());
            return er;
        });
    }

    @SagaEventHandler(associationProperty = "transactionId")
    public void handle(ValidationTasksForReleaseCreatedEvent event) {
        LOG.info("ValidationTasksForReleaseCreatedEvent event received for dataflowId {}, dataProviderId {}", event.getDataflowId(), event.getDataProviderId());
    }

    @SagaEventHandler(associationProperty = "transactionId")
    public void handle(ValidationForReleaseFinishedEvent event, MetaData metaData) {
        CreateSnapshotRecordRorReleaseInMetabaseCommand createReleaseSnapshotCommand = CreateSnapshotRecordRorReleaseInMetabaseCommand.builder().datasetReleaseAggregateId(event.getDatasetReleaseAggregateId()).transactionId(event.getTransactionId())
                .dataflowId(event.getDataflowId()).dataProviderId(event.getDataProviderId()).restrictFromPublic(event.isRestrictFromPublic()).validate(event.isValidate()).datasetIds(event.getDatasetIds()).releaseAggregateId(event.getReleaseAggregateId())
                .dataflowReleaseAggregateId(event.getDataflowReleaseAggregateId()).recordStoreReleaseAggregateId(event.getRecordStoreReleaseAggregateId()).validationReleaseAggregateId(event.getValidationReleaseAggregateId())
                .communicationReleaseAggregateId(event.getCommunicationReleaseAggregateId()).build();
        LinkedHashMap auth = (LinkedHashMap) metaData.get("auth");
        commandGateway.send(GenericCommandMessage.asCommandMessage(createReleaseSnapshotCommand).withMetaData(MetaData.with("auth", auth))).exceptionally(er -> {
            LOG.error(er.getCause().toString());
            return er;
        });
    }

    @SagaEventHandler(associationProperty = "transactionId")
    public void handle(SnapshotRecordForReleaseCreatedInMetabaseEvent event) {
        CreateSnapshotFileForReleaseCommand createReleaseSnapshotCommand = CreateSnapshotFileForReleaseCommand.builder().recordStoreReleaseAggregateId(event.getRecordStoreReleaseAggregateId()).transactionId(event.getTransactionId())
                .dataflowId(event.getDataflowId()).dataProviderId(event.getDataProviderId()).restrictFromPublic(event.isRestrictFromPublic()).validate(event.isValidate()).datasetIds(event.getDatasetIds()).datasetSnapshots(event.getDatasetSnapshots())
                .dataflowReleaseAggregateId(event.getDataflowReleaseAggregateId()).communicationReleaseAggregateId(event.getCommunicationReleaseAggregateId()).datasetReleaseAggregateId(event.getDatasetReleaseAggregateId())
                .validationReleaseAggregateId(event.getValidationReleaseAggregateId()).releaseAggregateId(event.getReleaseAggregateId()).build();
        commandGateway.send(createReleaseSnapshotCommand).exceptionally(er -> {
            LOG.error(er.getCause().toString());
            return er;
        });
    }

    @SagaEventHandler(associationProperty = "transactionId")
    public void handle(SnapshotFileForReleaseCreatedEvent event, MetaData metaData) {
        UpdateDatasetStatusCommand updateDatasetStatusCommand = UpdateDatasetStatusCommand.builder().datasetReleaseAggregateId(event.getDatasetReleaseAggregateId()).transactionId(event.getTransactionId())
                .dataflowId(event.getDataflowId()).dataProviderId(event.getDataProviderId()).restrictFromPublic(event.isRestrictFromPublic()).validate(event.isValidate()).datasetIds(event.getDatasetIds()).datasetSnapshots(event.getDatasetSnapshots())
                .dataflowReleaseAggregateId(event.getDataflowReleaseAggregateId()).recordStoreReleaseAggregateId(event.getRecordStoreReleaseAggregateId()).validationReleaseAggregateId(event.getValidationReleaseAggregateId())
                .communicationReleaseAggregateId(event.getCommunicationReleaseAggregateId()).releaseAggregateId(event.getReleaseAggregateId()).build();
        LinkedHashMap auth = (LinkedHashMap) metaData.get("auth");
        commandGateway.send(GenericCommandMessage.asCommandMessage(updateDatasetStatusCommand).withMetaData(MetaData.with("auth", auth))).exceptionally(er -> {
            LOG.error(er.getCause().toString());
            return er;
        });
    }

    @SagaEventHandler(associationProperty = "transactionId")
    public void handle(DatasetStatusUpdatedEvent event, MetaData metaData) {
        DeleteProviderCommand deleteProviderCommand = DeleteProviderCommand.builder().datasetReleaseAggregateId(event.getDatasetReleaseAggregateId()).transactionId(event.getTransactionId()).dataflowId(event.getDataflowId())
                .dataProviderId(event.getDataProviderId()).restrictFromPublic(event.isRestrictFromPublic()).validate(event.isValidate()).datasetIds(event.getDatasetIds()).datasetSnapshots(event.getDatasetSnapshots())
                .dataflowReleaseAggregateId(event.getDataflowReleaseAggregateId()).communicationReleaseAggregateId(event.getCommunicationReleaseAggregateId()).recordStoreReleaseAggregateId(event.getRecordStoreReleaseAggregateId())
                .validationReleaseAggregateId(event.getValidationReleaseAggregateId()).releaseAggregateId(event.getReleaseAggregateId()).dataCollectionForDeletion(event.getDataCollectionForDeletion()).datasetDataCollection(event.getDatasetDataCollection()).build();

        LinkedHashMap auth = (LinkedHashMap) metaData.get("auth");
        commandGateway.send(GenericCommandMessage.asCommandMessage(deleteProviderCommand).withMetaData(MetaData.with("auth", auth))).exceptionally(er -> {
            LOG.error(er.getCause().toString());
            return er;
        });
    }

    @SagaEventHandler(associationProperty = "transactionId")
    public void handle(ProviderDeletedEvent event, MetaData metaData) {
        if (event.getDataCollectionForDeletion().size()>0) {
            DeleteProviderCommand deleteProviderCommand = DeleteProviderCommand.builder().datasetReleaseAggregateId(event.getDatasetReleaseAggregateId()).transactionId(event.getTransactionId()).dataflowId(event.getDataflowId())
                    .dataProviderId(event.getDataProviderId()).restrictFromPublic(event.isRestrictFromPublic()).validate(event.isValidate()).datasetIds(event.getDatasetIds()).datasetSnapshots(event.getDatasetSnapshots())
                    .dataflowReleaseAggregateId(event.getDataflowReleaseAggregateId()).communicationReleaseAggregateId(event.getCommunicationReleaseAggregateId()).recordStoreReleaseAggregateId(event.getRecordStoreReleaseAggregateId())
                    .validationReleaseAggregateId(event.getValidationReleaseAggregateId()).releaseAggregateId(event.getReleaseAggregateId()).dataCollectionForDeletion(event.getDataCollectionForDeletion()).datasetDataCollection(event.getDatasetDataCollection()).build();

            LinkedHashMap auth = (LinkedHashMap) metaData.get("auth");
            commandGateway.send(GenericCommandMessage.asCommandMessage(deleteProviderCommand).withMetaData(MetaData.with("auth", auth))).exceptionally(er -> {
                LOG.error(er.getCause().toString());
                return er;
            });
        } else {
            LinkedHashMap auth = (LinkedHashMap) metaData.get("auth");
            UpdateInternalRepresentativeCommand updateInternalRepresentativeCommand = UpdateInternalRepresentativeCommand.builder().dataflowReleaseAggregateId(event.getDataflowReleaseAggregateId()).transactionId(event.getTransactionId()).dataflowId(event.getDataflowId()).dataProviderId(event.getDataProviderId())
                    .restrictFromPublic(event.isRestrictFromPublic()).validate(event.isValidate()).datasetIds(event.getDatasetIds()).datasetSnapshots(event.getDatasetSnapshots()).datasetReleaseAggregateId(event.getDatasetReleaseAggregateId()).communicationReleaseAggregateId(event.getCommunicationReleaseAggregateId())
                    .validationReleaseAggregateId(event.getValidationReleaseAggregateId()).releaseAggregateId(event.getReleaseAggregateId()).recordStoreReleaseAggregateId(event.getRecordStoreReleaseAggregateId()).datasetDataCollection(event.getDatasetDataCollection()).build();
            commandGateway.send(GenericCommandMessage.asCommandMessage(updateInternalRepresentativeCommand).withMetaData(MetaData.with("auth", auth))).exceptionally(er -> {
                LOG.error(er.getCause().toString());
                return er;
            });
        }
    }

    @SagaEventHandler(associationProperty = "transactionId")
    public void handle(InternalRepresentativeUpdatedEvent event, MetaData metaData) {
        LinkedHashMap auth = (LinkedHashMap) metaData.get("auth");
        UpdateDatasetRunningStatusCommand updateDatasetRunningStatusCommand = UpdateDatasetRunningStatusCommand.builder().datasetReleaseAggregateId(event.getDatasetReleaseAggregateId()).transactionId(event.getTransactionId()).dataflowId(event.getDataflowId())
                .dataProviderId(event.getDataProviderId()).restrictFromPublic(event.isRestrictFromPublic()).validate(event.isValidate()).datasetIds(event.getDatasetIds()).datasetSnapshots(event.getDatasetSnapshots()).releaseAggregateId(event.getReleaseAggregateId())
                .communicationReleaseAggregateId(event.getCommunicationReleaseAggregateId()).dataflowReleaseAggregateId(event.getDataflowReleaseAggregateId()).validationReleaseAggregateId(event.getValidationReleaseAggregateId())
                .recordStoreReleaseAggregateId(event.getRecordStoreReleaseAggregateId()).datasetDataCollection(event.getDatasetDataCollection()).build();
        commandGateway.send(GenericCommandMessage.asCommandMessage(updateDatasetRunningStatusCommand).withMetaData(MetaData.with("auth", auth))).exceptionally(er -> {
            LOG.error(er.getCause().toString());
            return er;
        });
    }

    @SagaEventHandler(associationProperty = "transactionId")
    public void handle(DatasetRunningStatusUpdatedEvent event, MetaData metaData) {
        LinkedHashMap auth = (LinkedHashMap) metaData.get("auth");
        RestoreDataFromSnapshotCommand restoreDataFromSnapshotCommand = RestoreDataFromSnapshotCommand.builder().recordStoreReleaseAggregateId(event.getRecordStoreReleaseAggregateId()).transactionId(event.getTransactionId()).dataflowId(event.getDataflowId())
                .dataProviderId(event.getDataProviderId()).restrictFromPublic(event.isRestrictFromPublic()).validate(event.isValidate()).datasetIds(event.getDatasetIds()).datasetSnapshots(event.getDatasetSnapshots()).datasetDataCollection(event.getDatasetDataCollection())
                .releaseAggregateId(event.getReleaseAggregateId()).communicationReleaseAggregateId(event.getCommunicationReleaseAggregateId()).datasetReleaseAggregateId(event.getDatasetReleaseAggregateId())
                .dataflowReleaseAggregateId(event.getDataflowReleaseAggregateId()).validationReleaseAggregateId(event.getValidationReleaseAggregateId()).build();
        commandGateway.send(GenericCommandMessage.asCommandMessage(restoreDataFromSnapshotCommand).withMetaData(MetaData.with("auth", auth))).exceptionally(er -> {
            LOG.error(er.getCause().toString());
            return er;
        });
    }

    @SagaEventHandler(associationProperty = "transactionId")
    public void handle(DataRestoredFromSnapshotEvent event, MetaData metaData) {
        LinkedHashMap auth = (LinkedHashMap) metaData.get("auth");
        MarkSnapshotReleasedCommand markSnapshotReleasedCommand = MarkSnapshotReleasedCommand.builder().datasetReleaseAggregateId(event.getDatasetReleaseAggregateId()).transactionId(event.getTransactionId()).dataflowId(event.getDataflowId()).dataProviderId(event.getDataProviderId())
                .restrictFromPublic(event.isRestrictFromPublic()).validate(event.isValidate()).datasetIds(event.getDatasetIds()).datasetSnapshots(event.getDatasetSnapshots()).datasetDataCollection(event.getDatasetDataCollection()).releaseAggregateId(event.getReleaseAggregateId())
                .dataflowReleaseAggregateId(event.getDataflowReleaseAggregateId()).communicationReleaseAggregateId(event.getCommunicationReleaseAggregateId()).recordStoreReleaseAggregateId(event.getRecordStoreReleaseAggregateId()).validationReleaseAggregateId(event.getValidationReleaseAggregateId()).build();
        commandGateway.send(GenericCommandMessage.asCommandMessage(markSnapshotReleasedCommand).withMetaData(MetaData.with("auth", auth))).exceptionally(er -> {
            LOG.error(er.getCause().toString());
            return er;
        });
    }

    @SagaEventHandler(associationProperty = "transactionId")
    public void handle(SnapshotMarkedReleasedEvent event, MetaData metaData) {
        LinkedHashMap auth = (LinkedHashMap) metaData.get("auth");
        UpdateChangesEuDatasetCommand updateChangesEuDatasetCommand = UpdateChangesEuDatasetCommand.builder().datasetReleaseAggregateId(event.getDatasetReleaseAggregateId()).transactionId(event.getTransactionId()).dataflowId(event.getDataflowId()).dataProviderId(event.getDataProviderId())
                .restrictFromPublic(event.isRestrictFromPublic()).validate(event.isValidate()).datasetIds(event.getDatasetIds()).datasetSnapshots(event.getDatasetSnapshots()).datasetDataCollection(event.getDatasetDataCollection()).datasetDateRelease(event.getDatasetDateRelease())
                .dataflowReleaseAggregateId(event.getDataflowReleaseAggregateId()).recordStoreReleaseAggregateId(event.getRecordStoreReleaseAggregateId()).communicationReleaseAggregateId(event.getCommunicationReleaseAggregateId()).releaseAggregateId(event.getReleaseAggregateId())
                .validationReleaseAggregateId(event.getValidationReleaseAggregateId()).build();
        commandGateway.send(GenericCommandMessage.asCommandMessage(updateChangesEuDatasetCommand).withMetaData(MetaData.with("auth", auth))).exceptionally(er -> {
            LOG.error(er.getCause().toString());
            return er;
        });
    }

    @SagaEventHandler(associationProperty = "transactionId")
    public void handle(ChangesEuDatasetUpdatedEvent event, MetaData metaData) {
        LinkedHashMap auth = (LinkedHashMap) metaData.get("auth");
        SavePublicFilesForReleaseCommand savePublicFilesForReleaseCommand = SavePublicFilesForReleaseCommand.builder().datasetReleaseAggregateId(event.getDatasetReleaseAggregateId()).transactionId(event.getTransactionId()).dataflowId(event.getDataflowId()).dataProviderId(event.getDataProviderId())
                .restrictFromPublic(event.isRestrictFromPublic()).validate(event.isValidate()).datasetIds(event.getDatasetIds()).datasetDataCollection(event.getDatasetDataCollection()).datasetSnapshots(event.getDatasetSnapshots()).datasetDateRelease(event.getDatasetDateRelease())
                .dataflowReleaseAggregateId(event.getDataflowReleaseAggregateId()).recordStoreReleaseAggregateId(event.getRecordStoreReleaseAggregateId()).communicationReleaseAggregateId(event.getCommunicationReleaseAggregateId()).releaseAggregateId(event.getReleaseAggregateId())
                .validationReleaseAggregateId(event.getValidationReleaseAggregateId()).build();
        commandGateway.send(GenericCommandMessage.asCommandMessage(savePublicFilesForReleaseCommand).withMetaData(MetaData.with("auth", auth))).exceptionally(er -> {
            LOG.error(er.getCause().toString());
            return er;
        });
    }

    @SagaEventHandler(associationProperty = "transactionId")
    public void handle(PublicFilesForReleaseSavedEvent event, MetaData metaData) {
        LinkedHashMap auth = (LinkedHashMap) metaData.get("auth");
        RemoveReleaseLocksCommand removeReleaseLocksCommand = RemoveReleaseLocksCommand.builder().datasetReleaseAggregateId(event.getDatasetReleaseAggregateId()).transactionId(event.getTransactionId()).dataflowId(event.getDataflowId()).dataProviderId(event.getDataProviderId())
                .restrictFromPublic(event.isRestrictFromPublic()).validate(event.isValidate()).datasetIds(event.getDatasetIds()).datasetDataCollection(event.getDatasetDataCollection()).datasetSnapshots(event.getDatasetSnapshots()).datasetDateRelease(event.getDatasetDateRelease())
                .dataflowReleaseAggregateId(event.getDataflowReleaseAggregateId()).recordStoreReleaseAggregateId(event.getRecordStoreReleaseAggregateId()).communicationReleaseAggregateId(event.getCommunicationReleaseAggregateId()).releaseAggregateId(event.getReleaseAggregateId())
                .validationReleaseAggregateId(event.getValidationReleaseAggregateId()).build();
        commandGateway.send(GenericCommandMessage.asCommandMessage(removeReleaseLocksCommand).withMetaData(MetaData.with("auth", auth))).exceptionally(er -> {
            LOG.error(er.getCause().toString());
            return er;
        });
    }

    @SagaEventHandler(associationProperty = "transactionId")
    public void handle(ReleaseLocksRemovedEvent event, MetaData metaData) {
        LinkedHashMap auth = (LinkedHashMap) metaData.get("auth");
        SendEmailForSuccessfulReleaseCommand sendEmailForSuccessfulReleaseCommand = SendEmailForSuccessfulReleaseCommand.builder().communicationReleaseAggregateId(event.getCommunicationReleaseAggregateId()).transactionId(event.getTransactionId()).dataflowId(event.getDataflowId()).dataProviderId(event.getDataProviderId())
                .restrictFromPublic(event.isRestrictFromPublic()).validate(event.isValidate()).datasetIds(event.getDatasetIds()).datasetDataCollection(event.getDatasetDataCollection()).datasetSnapshots(event.getDatasetSnapshots()).datasetDateRelease(event.getDatasetDateRelease()).releaseAggregateId(event.getReleaseAggregateId())
                .dataflowReleaseAggregateId(event.getDataflowReleaseAggregateId()).recordStoreReleaseAggregateId(event.getRecordStoreReleaseAggregateId()).datasetReleaseAggregateId(event.getDatasetReleaseAggregateId()).validationReleaseAggregateId(event.getValidationReleaseAggregateId()).build();
        commandGateway.send(GenericCommandMessage.asCommandMessage(sendEmailForSuccessfulReleaseCommand).withMetaData(MetaData.with("auth", auth))).exceptionally(er -> {
            LOG.error(er.getCause().toString());
            return er;
        });
    }

    @SagaEventHandler(associationProperty = "transactionId")
    public void handle(EmailForSuccessfulReleaseSentEvent event, MetaData metaData) {
        LinkedHashMap auth = (LinkedHashMap) metaData.get("auth");
        SendNotificationForSuccessfulReleaseCommand sendNotificationForSuccessfulReleaseCommand = SendNotificationForSuccessfulReleaseCommand.builder().datasetReleaseAggregateId(event.getDatasetReleaseAggregateId()).transactionId(event.getTransactionId()).dataflowId(event.getDataflowId()).dataProviderId(event.getDataProviderId())
                .restrictFromPublic(event.isRestrictFromPublic()).validate(event.isValidate()).datasetIds(event.getDatasetIds()).datasetDataCollection(event.getDatasetDataCollection()).datasetSnapshots(event.getDatasetSnapshots()).datasetDateRelease(event.getDatasetDateRelease()).dataflowName(event.getDataflowName())
                .datasetName(event.getDatasetName()).dataflowReleaseAggregateId(event.getDataflowReleaseAggregateId()).communicationReleaseAggregateId(event.getCommunicationReleaseAggregateId()).recordStoreReleaseAggregateId(event.getRecordStoreReleaseAggregateId()).validationReleaseAggregateId(event.getValidationReleaseAggregateId())
                .releaseAggregateId(event.getReleaseAggregateId()).build();
        commandGateway.send(GenericCommandMessage.asCommandMessage(sendNotificationForSuccessfulReleaseCommand).withMetaData(MetaData.with("auth", auth))).exceptionally(er -> {
            LOG.error(er.getCause().toString());
            return er;
        });
    }

    @EndSaga
    @SagaEventHandler(associationProperty = "transactionId")
    public void handle(NotificationForSuccessfulReleaseSentEvent event, MetaData metaData) {
        LinkedHashMap auth = (LinkedHashMap) metaData.get("auth");
        CreateMessageForSuccessfulReleaseCommand createMessageForSuccessfulReleaseCommand = CreateMessageForSuccessfulReleaseCommand.builder().collaborationReleaseAggregateId(UUID.randomUUID().toString()).transactionId(event.getTransactionId()).dataflowId(event.getDataflowId()).dataProviderId(event.getDataProviderId())
                .restrictFromPublic(event.isRestrictFromPublic()).validate(event.isValidate()).datasetIds(event.getDatasetIds()).datasetDataCollection(event.getDatasetDataCollection()).datasetSnapshots(event.getDatasetSnapshots()).datasetDateRelease(event.getDatasetDateRelease()).dataflowName(event.getDataflowName()).datasetName(event.getDatasetName())
                .dataflowReleaseAggregateId(event.getDataflowReleaseAggregateId()).recordStoreReleaseAggregateId(event.getRecordStoreReleaseAggregateId()).communicationReleaseAggregateId(event.getCommunicationReleaseAggregateId()).validationReleaseAggregateId(event.getValidationReleaseAggregateId())
                .datasetReleaseAggregateId(event.getDatasetReleaseAggregateId()).releaseAggregateId(event.getReleaseAggregateId()).build();
        commandGateway.send(GenericCommandMessage.asCommandMessage(createMessageForSuccessfulReleaseCommand).withMetaData(MetaData.with("auth", auth))).exceptionally(er -> {
            LOG.error(er.getCause().toString());
            return er;
        });
    }

    @SagaEventHandler(associationProperty = "transactionId")
    public void handle(FailureReleaseLocksRemovedEvent event) {
        LOG.info("-----------------------REMOVE RELEASE LOCKS-------------------------------");
    }
}














