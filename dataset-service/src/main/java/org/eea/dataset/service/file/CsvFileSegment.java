package org.eea.dataset.service.file;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CsvFileSegment {
    Integer segmentNumber;
    Long startLine;
    Long endLine;
}
