package org.eea.axon.release.events;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class ReleaseStartNotificationCreatedEvent {

   private String aggregate;
   private String id;
   private Long dataProviderId;
   private Long dataflowId;
   private boolean restrictFromPublic;
   private boolean validate;

}
