package org.eea.interfaces.vo.dataset;

import lombok.*;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class CsvLineAndRecordFieldsHolder {
    private Integer csvLine;

    private String recordId;

    private  List<String>  recordFieldsIds ;
}
