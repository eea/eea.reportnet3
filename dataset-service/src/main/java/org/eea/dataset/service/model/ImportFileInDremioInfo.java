package org.eea.dataset.service.model;

import lombok.*;
import org.eea.interfaces.vo.dataset.enums.DatasetTypeEnum;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class ImportFileInDremioInfo {

    private Long jobId;
    private String processId;
    private Long datasetId;
    private Long dataflowId;
    private Long providerId;
    private String tableSchemaId;
    private String fileName;
    private Boolean replaceData;
    private Long integrationId;
    private String delimiter;
    private String errorMessage;
    private String warningMessage;
    private String dataProviderCode;
    private Boolean updateReferenceFolder;

    public ImportFileInDremioInfo(Long jobId, Long datasetId, Long dataflowId, Long providerId, String tableSchemaId, String fileName, Boolean replaceData,
                                  String delimiter, Long integrationId, String dataProviderCode) {
        this.jobId = jobId;
        this.datasetId = datasetId;
        this.dataflowId = dataflowId;
        this.providerId = providerId;
        this.tableSchemaId = tableSchemaId;
        this.fileName = fileName;
        this.replaceData = replaceData;
        this.delimiter = delimiter;
        this.integrationId = integrationId;
        this.dataProviderCode = dataProviderCode;
    }

    public ImportFileInDremioInfo(Long jobId, Long dataflowId, Long providerId, Long datasetId) {
        this.jobId = jobId;
        this.dataflowId = dataflowId;
        this.providerId = providerId;
        this.datasetId = datasetId;
    }
}
