package org.eea.axon.release.commands;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

import java.util.List;

@Builder
@AllArgsConstructor
@Getter
@Setter
public class CreateSnapshotRecordRorReleaseInMetabaseCommand {

    @TargetAggregateIdentifier
    private String aggregate;
    private final String transactionId;
    private final Long dataflowId;
    private final Long dataProviderId;
    private final boolean restrictFromPublic;
    private final boolean validate;
    private List<Long> datasetIds;
}
