package org.eea.interfaces.vo.validation;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ReleaseTaskVO {

    private String splitFileName;
    private Long snapshotId;
    private Integer splitFileId;
    private Integer numberOfSplitFiles;
    private Long datasetId;
    private Long dataflowId;
    private String firstFieldId;
    private String lastFieldId;
}