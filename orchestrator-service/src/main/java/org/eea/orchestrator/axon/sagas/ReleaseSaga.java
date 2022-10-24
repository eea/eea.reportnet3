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
import org.eea.interfaces.controller.dataset.DatasetController.DataSetControllerZuul;
import org.eea.interfaces.vo.ums.enums.SecurityRoleEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

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

    @Autowired
    private DataSetControllerZuul dataSetControllerZuul;

    @StartSaga
    @SagaEventHandler(associationProperty = "transactionId")
    public void handle(ReleaseStartNotificationCreatedEvent event) {
        SendUserNotificationForReleaseStartedCommand command = SendUserNotificationForReleaseStartedCommand.builder().commReleaseAggregate(UUID.randomUUID().toString()).transactionId(event.getTransactionId()).dataflowId(event.getDataflowId())
                .dataProviderId(event.getDataProviderId()).restrictFromPublic(event.isRestrictFromPublic()).validate(event.isValidate()).build();
        LOG.info("---------------STARTING SAGA FOR DATAFLOWID----------------" + event.getDataflowId());
        commandGateway.send(command);
    }

    @SagaEventHandler(associationProperty = "transactionId")
    public void handle(UserNotifationForReleaseSentEvent event, MetaData metaData) {
        AddReleaseLocksCommand addReleaseLocksCommand = AddReleaseLocksCommand.builder().datasetReleaseAggregate(UUID.randomUUID().toString()).transactionId(event.getTransactionId()).dataflowId(event.getDataflowId())
                .dataProviderId(event.getDataProviderId()).restrictFromPublic(event.isRestrictFromPublic()).validate(event.isValidate()).build();

        LinkedHashMap auth = (LinkedHashMap) metaData.get("auth");
        commandGateway.send(GenericCommandMessage.asCommandMessage(addReleaseLocksCommand).withMetaData(MetaData.with("auth", auth)));
//        commandGateway.send(addReleaseLocksCommand).exceptionally(er -> {
//            System.out.println(er);
//            return er;
//        });
    }

    @SagaEventHandler(associationProperty = "transactionId")
    public void handle(ReleaseLocksAddedEvent event) {
        UpdateRepresentativeVisibilityCommand updateRepresentativeVisibilityCommand = UpdateRepresentativeVisibilityCommand.builder()
                .aggregate(UUID.randomUUID().toString()).transactionId(event.getTransactionId()).dataflowId(event.getDataflowId()).dataProviderId(event.getDataProviderId()).restrictFromPublic(event.isRestrictFromPublic())
                .validate(event.isValidate()).datasetIds(event.getDatasetIds()).build();
        commandGateway.sendAndWait(updateRepresentativeVisibilityCommand);
    }

    @SagaEventHandler(associationProperty = "transactionId")
    public void handle(RepresentativeVisibilityUpdatedEvent event, MetaData metaData) {
        if (!isAdmin(metaData) || event.isValidate()) {
            ExecuteValidationProcessCommand executeValidationProcessCommand = ExecuteValidationProcessCommand.builder().aggregate(event.getAggregate()).transactionId(event.getTransactionId())
                    .dataflowId(event.getDataflowId()).dataProviderId(event.getDataProviderId()).restrictFromPublic(event.isRestrictFromPublic()).validate(event.isValidate()).datasetIds(event.getDatasetIds()).build();
            commandGateway.send(executeValidationProcessCommand);
        } else {
            CreateSnapshotRecordRorReleaseInMetabaseCommand createReleaseSnapshotCommand = CreateSnapshotRecordRorReleaseInMetabaseCommand.builder().aggregate(event.getAggregate()).transactionId(event.getTransactionId())
                    .dataflowId(event.getDataflowId()).dataProviderId(event.getDataProviderId()).restrictFromPublic(event.isRestrictFromPublic())
                    .validate(event.isValidate()).datasetIds(event.getDatasetIds()).build();
            commandGateway.send(createReleaseSnapshotCommand);
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
    public void handle(ValidationProcessForReleaseAddedEvent event) {
        LOG.info("Validation process added for datasets {} of dataflow {} ", event.getDatasetIds(), event.getDataflowId());
//        CreateSnapshotRecordRorReleaseInMetabaseCommand createReleaseSnapshotCommand = CreateSnapshotRecordRorReleaseInMetabaseCommand.builder().aggregate(UUID.randomUUID().toString()).transactionId(event.getTransactionId())
//                .dataflowId(event.getDataflowId()).dataProviderId(event.getDataProviderId()).restrictFromPublic(event.isRestrictFromPublic())
//                .validate(event.isValidate()).datasetIds(event.getDatasetIds()).build();
//        commandGateway.send(createReleaseSnapshotCommand);
    }

    @SagaEventHandler(associationProperty = "transactionId")
    public void handle(SnapshotRecordForReleaseCreatedInMetabaseEvent event) {
        CreateSnapshotFileForReleaseCommand createReleaseSnapshotCommand = CreateSnapshotFileForReleaseCommand.builder().aggregate(event.getAggregate()).transactionId(event.getTransactionId())
                .dataflowId(event.getDataflowId()).dataProviderId(event.getDataProviderId()).restrictFromPublic(event.isRestrictFromPublic())
                .validate(event.isValidate()).datasetIds(event.getDatasetIds()).build();
        commandGateway.send(createReleaseSnapshotCommand);
    }

    @SagaEventHandler(associationProperty = "transactionId")
    public void handle(SnapshotFileForReleaseCreatedEvent event) {
        UpdateDatasetStatusCommand updateDatasetStatusCommand = UpdateDatasetStatusCommand.builder().aggregate(event.getAggregate()).transactionId(event.getTransactionId())
                .dataflowId(event.getDataflowId()).dataProviderId(event.getDataProviderId()).restrictFromPublic(event.isRestrictFromPublic())
                .validate(event.isValidate()).datasetIds(event.getDatasetIds()).build();
        commandGateway.send(updateDatasetStatusCommand);
    }

    @SagaEventHandler(associationProperty = "transactionId")
    public void handle(DatasetStatusUpdatedEvent event) {
        DeleteProviderCommand deleteProviderCommand = DeleteProviderCommand.builder().aggregate(event.getAggregate()).transactionId(event.getTransactionId())
                .dataflowId(event.getDataflowId()).dataProviderId(event.getDataProviderId()).restrictFromPublic(event.isRestrictFromPublic())
                .validate(event.isValidate()).datasetIds(event.getDatasetIds()).build();
        commandGateway.send(deleteProviderCommand);
    }

    @SagaEventHandler(associationProperty = "transactionId")
    public void handle(ProviderDeletedEvent event) {
        UpdateInternalRepresentativeCommand updateInternalRepresentativeCommand = UpdateInternalRepresentativeCommand.builder().aggregate(event.getAggregate()).transactionId(event.getTransactionId())
                .dataflowId(event.getDataflowId()).dataProviderId(event.getDataProviderId()).restrictFromPublic(event.isRestrictFromPublic())
                .validate(event.isValidate()).datasetIds(event.getDatasetIds()).build();
        commandGateway.send(updateInternalRepresentativeCommand);
    }

    @SagaEventHandler(associationProperty = "transactionId")
    public void handle(InternalRepresentativeUpdatedEvent event) {
        UpdateDatasetRunningStatusCommand updateDatasetRunningStatusCommand = UpdateDatasetRunningStatusCommand.builder().aggregate(event.getAggregate()).transactionId(event.getTransactionId())
                .dataflowId(event.getDataflowId()).dataProviderId(event.getDataProviderId()).restrictFromPublic(event.isRestrictFromPublic())
                .validate(event.isValidate()).datasetIds(event.getDatasetIds()).build();
        commandGateway.send(updateDatasetRunningStatusCommand);
    }

    @SagaEventHandler(associationProperty = "transactionId")
    public void handle(DatasetRunningStatusUpdatedEvent event) {
        RestoreDataFromSnapshotCommand restoreDataFromSnapshotCommand = RestoreDataFromSnapshotCommand.builder().aggregate(event.getAggregate()).transactionId(event.getTransactionId())
                .dataflowId(event.getDataflowId()).dataProviderId(event.getDataProviderId()).restrictFromPublic(event.isRestrictFromPublic())
                .validate(event.isValidate()).datasetIds(event.getDatasetIds()).build();
        commandGateway.send(restoreDataFromSnapshotCommand);
    }

    @EndSaga
    @SagaEventHandler(associationProperty = "transactionId")
    public void handle(DataRestoredFromSnapshotEvent event) {
        MarkSnapshotReleasedCommand markSnapshotReleasedCommand = MarkSnapshotReleasedCommand.builder().aggregate(event.getAggregate()).transactionId(event.getTransactionId())
                .dataflowId(event.getDataflowId()).dataProviderId(event.getDataProviderId()).restrictFromPublic(event.isRestrictFromPublic())
                .validate(event.isValidate()).datasetIds(event.getDatasetIds()).build();
        commandGateway.send(markSnapshotReleasedCommand);
    }

    @SagaEventHandler(associationProperty = "transactionId")
    public void handle(ReleaseLocksRemovedEvent event) {
        LOG.info("-----------------------REMOVE RELEASE LOCKS-------------------------------");
    }
}














