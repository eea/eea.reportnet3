package org.eea.axon.release.events;

import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class ReleaseLocksAddedEvent {

   private String aggregate;
   private String id;
   private Long dataflowId;
   private Long dataProviderId;
   private boolean restrictFromPublic;
   private boolean validate;
   private List<Long> datasetIds;
}
