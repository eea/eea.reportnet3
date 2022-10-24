package org.eea.axon.release.events;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class UserNotifationForReleaseSentEvent {
    private String commReleaseAggregate;
    private String transactionId;
    private Long dataProviderId;
    private Long dataflowId;
    private boolean restrictFromPublic;
    private boolean validate;

}
