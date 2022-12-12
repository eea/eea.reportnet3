package org.eea.axon.release.commands;

import lombok.*;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class SetReleaseJobInProgressCommand {

    @TargetAggregateIdentifier
    private String releaseAggregateId;
    private String communicationReleaseAggregateId;
    private String datasetReleaseAggregateId;
    private String dataflowReleaseAggregateId;
    private String validationReleaseAggregateId;
    private String collaborationReleaseAggregateId;
    private String recordStoreReleaseAggregateId;
    private String transactionId;
    private Long dataflowId;
    private Long dataProviderId;
    private boolean restrictFromPublic;
    private boolean validate;
    private Long jobId;
}
