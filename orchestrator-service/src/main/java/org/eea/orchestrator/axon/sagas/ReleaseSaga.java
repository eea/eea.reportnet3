package org.eea.orchestrator.axon.sagas;

import org.axonframework.commandhandling.gateway.CommandGateway;
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
import org.springframework.security.core.GrantedAuthority;

import java.util.*;

@Saga
public class ReleaseSaga {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(ReleaseSaga.class);

    @Autowired
    private transient CommandGateway commandGateway;

    @Autowired
    private DataSetSnapshotControllerZuul dataSetSnapshotControllerZuul;

    @Autowired
    private DataSetControllerZuul dataSetControllerZuul;

    @StartSaga
    @SagaEventHandler(associationProperty = "id")
    public void handle(ReleaseStartNotificationCreatedEvent event) {
        List<Long> datasetIds = dataSetControllerZuul.findDatasetIdsByDataflowId(event.getDataflowId(), event.getDataProviderId());
        AddReleaseLocksCommand addReleaseLocksCommand = AddReleaseLocksCommand.builder().id(event.getId()).aggregate(UUID.randomUUID().toString()).dataflowId(event.getDataflowId())
                .dataProviderId(event.getDataProviderId()).restrictFromPublic(event.isRestrictFromPublic()).validate(event.isValidate()).datasetIds(datasetIds).build();
        commandGateway.send(addReleaseLocksCommand);
    }

    @SagaEventHandler(associationProperty = "id")
    public void handle(ReleaseLocksAddedEvent event) {
        UpdateRepresentativeVisibilityCommand updateRepresentativeVisibilityCommand = UpdateRepresentativeVisibilityCommand.builder()
                .id(event.getId()).aggregate(UUID.randomUUID().toString()).dataflowId(event.getDataflowId()).dataProviderId(event.getDataProviderId()).restrictFromPublic(event.isRestrictFromPublic())
                .validate(event.isValidate()).datasetIds(event.getDatasetIds()).build();
        commandGateway.send(updateRepresentativeVisibilityCommand);
    }

    @SagaEventHandler(associationProperty = "id")
    public void handle(RepresentativeVisibilityUpdatedEvent event, MetaData metaData) {
        if (!isAdmin(metaData) || event.isValidate()) {
            ExecuteValidationProcessCommand executeValidationProcessCommand = ExecuteValidationProcessCommand.builder().id(event.getId()).aggregate(UUID.randomUUID().toString())
                    .dataflowId(event.getDataflowId()).dataProviderId(event.getDataProviderId()).restrictFromPublic(event.isRestrictFromPublic()).validate(event.isValidate()).datasetIds(event.getDatasetIds()).build();
            commandGateway.send(executeValidationProcessCommand);
        } else {
            CreateSnapshotRecordRorReleaseInMetabaseCommand createReleaseSnapshotCommand = CreateSnapshotRecordRorReleaseInMetabaseCommand.builder().aggregate(UUID.randomUUID().toString())
                    .id(event.getId()).dataflowId(event.getDataflowId()).dataProviderId(event.getDataProviderId()).restrictFromPublic(event.isRestrictFromPublic())
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
        CreateSnapshotRecordRorReleaseInMetabaseCommand createReleaseSnapshotCommand = CreateSnapshotRecordRorReleaseInMetabaseCommand.builder().aggregate(UUID.randomUUID().toString())
                .id(event.getId()).dataflowId(event.getDataflowId()).dataProviderId(event.getDataProviderId()).restrictFromPublic(event.isRestrictFromPublic())
                .validate(event.isValidate()).datasetIds(event.getDatasetIds()).build();
        commandGateway.send(createReleaseSnapshotCommand);
    }

    @SagaEventHandler(associationProperty = "id")
    public void handle(SnapshotRecordForReleaseCreatedInMetabaseEvent event) {
        CreateSnapshotFileForReleaseCommand createReleaseSnapshotCommand = CreateSnapshotFileForReleaseCommand.builder().aggregate(UUID.randomUUID().toString())
                .id(event.getId()).dataflowId(event.getDataflowId()).dataProviderId(event.getDataProviderId()).restrictFromPublic(event.isRestrictFromPublic())
                .validate(event.isValidate()).datasetIds(event.getDatasetIds()).build();

        event.getDatasetIds().forEach(datasetId -> {
            Long snapshotId = dataSetSnapshotControllerZuul.findSnapshotIdByReportingDataset(datasetId);
            createReleaseSnapshotCommand.setDatasetSnapshots(new HashMap<>());
            createReleaseSnapshotCommand.getDatasetSnapshots().put(datasetId, snapshotId);
        });

        commandGateway.send(createReleaseSnapshotCommand);
    }

    @SagaEventHandler(associationProperty = "id")
    public void handle(SnapshotFileForReleaseCreatedEvent event) {
        UpdateDatasetStatusCommand updateDatasetStatusCommand = UpdateDatasetStatusCommand.builder().aggregate(UUID.randomUUID().toString())
                .id(event.getId()).dataflowId(event.getDataflowId()).dataProviderId(event.getDataProviderId()).restrictFromPublic(event.isRestrictFromPublic())
                .validate(event.isValidate()).datasetIds(event.getDatasetIds()).datasetSnapshots(event.getDatasetSnapshots()).build();
        commandGateway.send(updateDatasetStatusCommand);
    }

    @SagaEventHandler(associationProperty = "id")
    public void handle(DatasetStatusUpdatedEvent event) {
        DeleteProviderCommand deleteProviderCommand = DeleteProviderCommand.builder().aggregate(UUID.randomUUID().toString())
                .id(event.getId()).dataflowId(event.getDataflowId()).dataProviderId(event.getDataProviderId()).restrictFromPublic(event.isRestrictFromPublic())
                .validate(event.isValidate()).datasetIds(event.getDatasetIds()).datasetSnapshots(event.getDatasetSnapshots()).build();
        commandGateway.send(deleteProviderCommand);
    }

    @SagaEventHandler(associationProperty = "id")
    public void handle(ProviderDeletedEvent event) {
        UpdateInternalRepresentativeCommand updateInternalRepresentativeCommand = UpdateInternalRepresentativeCommand.builder().aggregate(UUID.randomUUID().toString())
                .id(event.getId()).dataflowId(event.getDataflowId()).dataProviderId(event.getDataProviderId()).restrictFromPublic(event.isRestrictFromPublic())
                .validate(event.isValidate()).datasetIds(event.getDatasetIds()).datasetSnapshots(event.getDatasetSnapshots()).build();
        commandGateway.send(updateInternalRepresentativeCommand);
    }

    @EndSaga
    @SagaEventHandler(associationProperty = "id")
    public void handle(InternalRepresentativeUpdatedEvent event) {
        UpdateDatasetRunningStatusCommand updateDatasetRunningStatusCommand = UpdateDatasetRunningStatusCommand.builder().aggregate(UUID.randomUUID().toString())
                .id(event.getId()).dataflowId(event.getDataflowId()).dataProviderId(event.getDataProviderId()).restrictFromPublic(event.isRestrictFromPublic())
                .validate(event.isValidate()).datasetIds(event.getDatasetIds()).datasetSnapshots(event.getDatasetSnapshots()).build();
        commandGateway.send(updateDatasetRunningStatusCommand);
    }

//    @SagaEventHandler(associationProperty = "id")
//    public void handle() {
//
//    }
}














