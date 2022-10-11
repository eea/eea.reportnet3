package org.eea.axon.release.commands;

import lombok.*;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

import java.util.List;

@Builder
@AllArgsConstructor
@Getter
@Setter
public class AddReleaseLocksCommand {

    @TargetAggregateIdentifier
    private final String datasetAggregate;
    private final String id;
    private final Long dataflowId;
    private final Long dataProviderId;
    private final boolean restrictFromPublic;
    private final boolean validate;
    private List<Long> datasetIds;
}
