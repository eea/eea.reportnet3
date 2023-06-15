package org.eea.interfaces.vo.validation;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.eea.interfaces.vo.dataset.enums.EntityTypeEnum;
import org.eea.interfaces.vo.dataset.enums.ErrorTypeEnum;

@Getter
@Setter
@ToString
public class DremioValidationVO {

    private Long pk;
    private String recordId;
    private ErrorTypeEnum validationLevel;
    private EntityTypeEnum validationArea;
    private String message;
    private String tableName;
    private String fieldName;
    private Long datasetId;
    private String qcCode;

}
