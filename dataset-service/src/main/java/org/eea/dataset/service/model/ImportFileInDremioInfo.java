package org.eea.dataset.service.model;

import lombok.*;

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
    private Boolean sendWrongFileNameWarning;
    private Boolean sendEmptyFileWarning;
    private String dataProviderCode;

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
}
