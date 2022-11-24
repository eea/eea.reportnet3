package org.eea.interfaces.vo.validation;

import lombok.*;
import org.eea.interfaces.vo.recordstore.enums.ProcessStatusEnum;

import java.util.Date;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class TaskVO {

    private Long id;

    private String processId;

    private ProcessStatusEnum status;

    private Date createDate;

    private Date startingDate;

    private Date finishDate;

    private String json;

    private int version;

    private String pod;
}
