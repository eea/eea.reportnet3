package org.eea.axon.release.events;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class ReleaseStartNotificationCreatedEvent {

   private String releaseAggregateId;
   private String datasetReleaseAggregateId;
   private String communicationReleaseAggregateId;
   private String dataflowReleaseAggregateId;
   private String validationReleaseAggregateId;
   private String collaborationReleaseAggregateId;
   private String recordStoreReleaseAggregateId;
   private String transactionId;
   private Long dataProviderId;
   private Long dataflowId;
   private boolean restrictFromPublic;
   private boolean validate;
   private Long jobId;
   private String user;
}
