package org.eea.interfaces.vo.dataset;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ReleasedDatasetDataInfoVO {

    private Long reportingDatasetNumberOfRecords;

    private Long collectionDatasetNumberOfRecords;

    private Boolean hasReleased;

    private Boolean modifiedAfterRelease;
}
