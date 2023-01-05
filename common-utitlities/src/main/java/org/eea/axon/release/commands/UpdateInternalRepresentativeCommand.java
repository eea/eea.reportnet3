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
public class UpdateInternalRepresentativeCommand {

    @TargetAggregateIdentifier
    private String dataflowReleaseAggregateId;
    private String datasetReleaseAggregateId;
    private String releaseAggregateId;
    private String communicationReleaseAggregateId;
    private String validationReleaseAggregateId;
    private String collaborationReleaseAggregateId;
    private String recordStoreReleaseAggregateId;
    private String transactionId;
    private Long dataflowId;
    private Long dataProviderId;
    private boolean restrictFromPublic;
    private boolean validate;
    private List<Long> datasetIds;
    private Map<Long, Long> datasetSnapshots;
    private Map<Long, String> datasetReleaseProcessId;
    private Map<Long, Long> datasetDataCollection;
    private Long jobId;
    private String user;
}
