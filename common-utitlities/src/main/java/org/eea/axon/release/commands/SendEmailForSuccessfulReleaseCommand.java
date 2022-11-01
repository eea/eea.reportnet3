package org.eea.axon.release.commands;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Builder
@AllArgsConstructor
@Getter
@Setter
public class SendEmailForSuccessfulReleaseCommand {

    @TargetAggregateIdentifier
    private String communicationReleaseAggregateId;
    private String datasetReleaseAggregateId;
    private String releaseAggregateId;
    private String dataflowReleaseAggregateId;
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
    private Map<Long, Long> datasetDataCollection;
    private Map<Long, Date> datasetDateRelease;
    private String dataflowName;
    private String datasetName;
}
