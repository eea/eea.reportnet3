package org.eea.axon.release.events;

import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class ValidationProcessForReleaseAddedEvent {

    private String aggregate;
    private String transactionId;
    private Long dataProviderId;
    private Long dataflowId;
    private boolean restrictFromPublic;
    private boolean validate;
    private List<Long> datasetIds;
}
