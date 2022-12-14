package org.eea.dataset.service.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class TruncateDataset {

    /** The dataset id. */
    private Long datasetId;

    /** The data provider id. */
    private Long dataProviderId;

    /** The dataset name. */
    private String datasetName;

    /** The dataflow name. */
    private String dataflowName;
}
