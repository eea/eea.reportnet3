package org.eea.axon.release.commands;

import lombok.*;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

@AllArgsConstructor
@Builder
@Getter
@Setter
public class CreateReleaseStartNotificationCommand {

    @TargetAggregateIdentifier
    private final String communicationAggregate;
    private final String id;
    private final Long dataflowId;
    private final Long dataProviderId;
    private final boolean restrictFromPublic;
    private final boolean validate;
}
