package org.eea.axon.release.events;

import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class ReleaseLocksAddedEvent {

   private String datasetReleaseAggregateId;
   private String releaseAggregateId;
   private String communicationReleaseAggregateId;
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
   private Long jobId;
}
