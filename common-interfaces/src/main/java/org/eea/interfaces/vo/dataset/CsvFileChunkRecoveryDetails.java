package org.eea.interfaces.vo.dataset;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class CsvFileChunkRecoveryDetails {
    private String recordsBulkImporterTemporaryFile;

    private String fieldsBulkImporterTemporaryFile;

    private Long taskId;

    private Integer numberOfRecords;
}
