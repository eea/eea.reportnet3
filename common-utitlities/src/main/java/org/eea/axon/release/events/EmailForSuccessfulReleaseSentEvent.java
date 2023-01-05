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
public class EmailForSuccessfulReleaseSentEvent {

   private String communicationReleaseAggregateId;
   private String datasetReleaseAggregateId;
   private String releaseAggregateId;
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
   private String dataflowName;
   private String datasetName;
   private Long jobId;
   private String user;
}
