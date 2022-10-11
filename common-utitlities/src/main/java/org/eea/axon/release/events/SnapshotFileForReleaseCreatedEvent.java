package org.eea.axon.release.events;

import lombok.*;

import java.util.HashMap;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class SnapshotFileForReleaseCreatedEvent {

    private String recordStoreAggregate;
    private String id;
    private Long dataProviderId;
    private Long dataflowId;
    private boolean restrictFromPublic;
    private boolean validate;
    private List<Long> datasetIds;
    private HashMap<Long, Long> datasetSnapshots;
}
