package org.eea.axon.release.commands;

import lombok.*;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

import java.util.List;
import java.util.Map;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UpdateMaterializedViewCommand {

    @TargetAggregateIdentifier
    private String validationReleaseAggregateId;
    private String recordStoreReleaseAggregateId;
    private String datasetReleaseAggregateId;
    private String releaseAggregateId;
    private String communicationReleaseAggregateId;
    private String dataflowReleaseAggregateId;
    private String collaborationReleaseAggregateId;
    private String transactionId;
    private Long dataflowId;
    private Long dataProviderId;
    private boolean restrictFromPublic;
    private boolean validate;
    private List<Long> datasetIds;
    private Map<Long, String> datasetValidationProcessId;
    private Long datasetIForMaterializedViewEvent;
    private List<Long> referencesToRefresh;
    private Long jobId;
    private String user;
}
