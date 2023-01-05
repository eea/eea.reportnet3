package org.eea.axon.release.commands;

import lombok.*;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class RevertRepresentativeVisibilityCommand {

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
    private Long jobId;
    private String user;
}
