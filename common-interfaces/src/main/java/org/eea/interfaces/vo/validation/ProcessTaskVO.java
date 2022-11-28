package org.eea.interfaces.vo.validation;

import lombok.Data;

import java.util.List;

@Data
public class ProcessTaskVO {

    private String processId;
    private List<TaskVO> tasks;
}
