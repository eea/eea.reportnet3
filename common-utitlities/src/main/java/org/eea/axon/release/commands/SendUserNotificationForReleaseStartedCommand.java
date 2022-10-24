package org.eea.axon.release.commands;

import lombok.*;
import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.messaging.MetaData;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

import java.util.Map;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class SendUserNotificationForReleaseStartedCommand   {
    @TargetAggregateIdentifier
    private  String commReleaseAggregate;
    private  String transactionId;
    private  Long dataflowId;
    private  Long dataProviderId;
    private  boolean restrictFromPublic;
    private  boolean validate;


}
