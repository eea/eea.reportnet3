package org.eea.interfaces.vo.orchestrator;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.eea.interfaces.vo.validation.TaskVO;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AdminTaskInfoVO {

    private TaskVO taskVO;
    private Long taskDurationMs;
    private String taskDurationFormatted;

}

