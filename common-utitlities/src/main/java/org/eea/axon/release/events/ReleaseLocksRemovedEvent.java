package org.eea.axon.release.events;

import lombok.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class ReleaseLocksRemovedEvent {

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
   private Map<Long, Long> datasetSnapshots;
   private Map<Long, Long> datasetDataCollection;
   private Map<Long, Date> datasetDateRelease;
}
