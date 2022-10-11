package org.eea.axon.release.events;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class ReleaseStartNotificationCreatedEvent {

   private String communicationAggregate;
   private String id;
   private Long dataProviderId;
   private Long dataflowId;
   private boolean restrictFromPublic;
   private boolean validate;

}
