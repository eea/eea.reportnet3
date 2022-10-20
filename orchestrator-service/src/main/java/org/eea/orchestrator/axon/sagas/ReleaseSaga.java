package org.eea.orchestrator.axon.sagas;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.TrackingEventProcessor;
import org.axonframework.messaging.MetaData;
import org.axonframework.modelling.saga.EndSaga;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.modelling.saga.StartSaga;
import org.axonframework.spring.stereotype.Saga;
import org.eea.axon.release.commands.*;
import org.eea.axon.release.events.*;
import org.eea.interfaces.controller.dataset.DatasetController.DataSetControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetSnapshotController.DataSetSnapshotControllerZuul;
import org.eea.interfaces.vo.ums.enums.SecurityRoleEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.GrantedAuthority;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Saga
@ProcessingGroup("releaceproc")
public class ReleaseSaga {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(ReleaseSaga.class);

    @Autowired
    private CommandGateway commandGateway;


    @Autowired
    private DataSetSnapshotControllerZuul dataSetSnapshotControllerZuul;

    @Autowired
    private DataSetControllerZuul dataSetControllerZuul;




    @StartSaga
    @SagaEventHandler(associationProperty = "id")
    public void handle(ReleaseStartNotificationCreatedEvent event) {
        SendUserNotificationForReleaseStartedCommand command  = new SendUserNotificationForReleaseStartedCommand();
     //   List<Long> datasetIds = dataSetControllerZuul.findDatasetIdsByDataflowId(event.getDataflowId(), event.getDataProviderId());
        AddReleaseLocksCommand addReleaseLocksCommand = AddReleaseLocksCommand.builder().aggregate(UUID.randomUUID().toString()).id(event.getId()).dataflowId(event.getDataflowId())
                .dataProviderId(event.getDataProviderId()).restrictFromPublic(event.isRestrictFromPublic()).validate(event.isValidate()).build();
       //This command should go to CommunicationService
        command.setId(event.getId());
        command.setAggregate(event.getAggregate());

        commandGateway.send(command);
    }

    @StartSaga
    @SagaEventHandler(associationProperty = "id")
    public void handle(UserNotifationForReleaseSentEvent event) {
           List<Long> datasetIds = dataSetControllerZuul.findDatasetIdsByDataflowId(event.getDataflowId(), event.getDataProviderId());
        AddReleaseLocksCommand addReleaseLocksCommand = AddReleaseLocksCommand.builder().aggregate(UUID.randomUUID().toString()).id(event.getId()).dataflowId(event.getDataflowId())
                .dataProviderId(event.getDataProviderId()).restrictFromPublic(event.isRestrictFromPublic()).validate(event.isValidate()).datasetIds(datasetIds).build();

        commandGateway.send(addReleaseLocksCommand);
    }

    @SagaEventHandler(associationProperty = "id")
    public void handle(ReleaseLocksAddedEvent event) {
        UpdateRepresentativeVisibilityCommand updateRepresentativeVisibilityCommand = UpdateRepresentativeVisibilityCommand.builder()
                .aggregate(UUID.randomUUID().toString()).id(event.getId()).dataflowId(event.getDataflowId()).dataProviderId(event.getDataProviderId()).restrictFromPublic(event.isRestrictFromPublic())
                .validate(event.isValidate()).datasetIds(event.getDatasetIds()).build();
        commandGateway.sendAndWait(updateRepresentativeVisibilityCommand);
    }

    @SagaEventHandler(associationProperty = "id")
    public void handle(RepresentativeVisibilityUpdatedEvent event, MetaData metaData) {
        if (!isAdmin(metaData) || event.isValidate()) {
            ExecuteValidationProcessCommand executeValidationProcessCommand = ExecuteValidationProcessCommand.builder().aggregate(UUID.randomUUID().toString()).id(event.getId())
                    .dataflowId(event.getDataflowId()).dataProviderId(event.getDataProviderId()).restrictFromPublic(event.isRestrictFromPublic()).validate(event.isValidate()).datasetIds(event.getDatasetIds()).build();
            commandGateway.send(executeValidationProcessCommand);
        } else {
            CreateSnapshotRecordRorReleaseInMetabaseCommand createReleaseSnapshotCommand = CreateSnapshotRecordRorReleaseInMetabaseCommand.builder().aggregate(UUID.randomUUID().toString()).id(event.getId())
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

    @SagaEventHandler(associationProperty = "id")
    public void handle(ValidationProcessForReleaseAddedEvent event) {
        LOG.info("Validation process added for datasets {} of dataflow {} ", event.getDatasetIds(), event.getDataflowId());
//        CreateSnapshotRecordRorReleaseInMetabaseCommand createReleaseSnapshotCommand = CreateSnapshotRecordRorReleaseInMetabaseCommand.builder().aggregate(UUID.randomUUID().toString()).id(event.getId())
//                .dataflowId(event.getDataflowId()).dataProviderId(event.getDataProviderId()).restrictFromPublic(event.isRestrictFromPublic())
//                .validate(event.isValidate()).datasetIds(event.getDatasetIds()).build();
//        commandGateway.send(createReleaseSnapshotCommand);
    }

    @SagaEventHandler(associationProperty = "id")
    public void handle(SnapshotRecordForReleaseCreatedInMetabaseEvent event) {
        CreateSnapshotFileForReleaseCommand createReleaseSnapshotCommand = CreateSnapshotFileForReleaseCommand.builder().aggregate(UUID.randomUUID().toString()).id(event.getId())
               .dataflowId(event.getDataflowId()).dataProviderId(event.getDataProviderId()).restrictFromPublic(event.isRestrictFromPublic())
                .validate(event.isValidate()).datasetIds(event.getDatasetIds()).build();
        commandGateway.send(createReleaseSnapshotCommand);
    }

    @SagaEventHandler(associationProperty = "id")
    public void handle(SnapshotFileForReleaseCreatedEvent event) {
        UpdateDatasetStatusCommand updateDatasetStatusCommand = UpdateDatasetStatusCommand.builder().aggregate(UUID.randomUUID().toString()).id(event.getId())
                .dataflowId(event.getDataflowId()).dataProviderId(event.getDataProviderId()).restrictFromPublic(event.isRestrictFromPublic())
                .validate(event.isValidate()).datasetIds(event.getDatasetIds()).build();
        commandGateway.send(updateDatasetStatusCommand);
    }

    @SagaEventHandler(associationProperty = "id")
    public void handle(DatasetStatusUpdatedEvent event) {
        DeleteProviderCommand deleteProviderCommand = DeleteProviderCommand.builder().aggregate(UUID.randomUUID().toString()).id(event.getId())
                .dataflowId(event.getDataflowId()).dataProviderId(event.getDataProviderId()).restrictFromPublic(event.isRestrictFromPublic())
                .validate(event.isValidate()).datasetIds(event.getDatasetIds()).build();
        commandGateway.send(deleteProviderCommand);
    }

    @SagaEventHandler(associationProperty = "id")
    public void handle(ProviderDeletedEvent event) {
        UpdateInternalRepresentativeCommand updateInternalRepresentativeCommand = UpdateInternalRepresentativeCommand.builder().aggregate(UUID.randomUUID().toString()).id(event.getId())
                .dataflowId(event.getDataflowId()).dataProviderId(event.getDataProviderId()).restrictFromPublic(event.isRestrictFromPublic())
                .validate(event.isValidate()).datasetIds(event.getDatasetIds()).build();
        commandGateway.send(updateInternalRepresentativeCommand);
    }

    @SagaEventHandler(associationProperty = "id")
    public void handle(InternalRepresentativeUpdatedEvent event) {
        UpdateDatasetRunningStatusCommand updateDatasetRunningStatusCommand = UpdateDatasetRunningStatusCommand.builder().aggregate(UUID.randomUUID().toString()).id(event.getId())
                .dataflowId(event.getDataflowId()).dataProviderId(event.getDataProviderId()).restrictFromPublic(event.isRestrictFromPublic())
                .validate(event.isValidate()).datasetIds(event.getDatasetIds()).build();
        commandGateway.send(updateDatasetRunningStatusCommand);
    }

    @SagaEventHandler(associationProperty = "id")
    public void handle(DatasetRunningStatusUpdatedEvent event) {
        RestoreDataFromSnapshotCommand restoreDataFromSnapshotCommand = RestoreDataFromSnapshotCommand.builder().aggregate(UUID.randomUUID().toString()).id(event.getId())
                .dataflowId(event.getDataflowId()).dataProviderId(event.getDataProviderId()).restrictFromPublic(event.isRestrictFromPublic())
                .validate(event.isValidate()).datasetIds(event.getDatasetIds()).build();
        commandGateway.send(restoreDataFromSnapshotCommand);
    }

    @EndSaga
    @SagaEventHandler(associationProperty = "id")
    public void handle(DataRestoredFromSnapshotEvent event) {
        MarkSnapshotReleasedCommand markSnapshotReleasedCommand = MarkSnapshotReleasedCommand.builder().aggregate(UUID.randomUUID().toString()).id(event.getId())
                .dataflowId(event.getDataflowId()).dataProviderId(event.getDataProviderId()).restrictFromPublic(event.isRestrictFromPublic())
                .validate(event.isValidate()).datasetIds(event.getDatasetIds()).build();
        commandGateway.send(markSnapshotReleasedCommand);
    }

    @SagaEventHandler(associationProperty = "id")
    public void handle(ReleaseLocksRemovedEvent event) {
        LOG.info("-----------------------REMOVE RELEASE LOCKS-------------------------------");
    }
}














