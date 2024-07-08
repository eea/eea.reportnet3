package org.eea.dataset.service.model;

import lombok.*;

import java.io.File;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class FileWithRecordNum {
    private File file;
    private Long numberOfRecords;
}
