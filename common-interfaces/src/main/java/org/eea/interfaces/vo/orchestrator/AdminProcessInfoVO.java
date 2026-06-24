package org.eea.interfaces.vo.orchestrator;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.eea.interfaces.vo.recordstore.ProcessVO;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AdminProcessInfoVO {

    private Long jobId;
    private ProcessVO processVO;
    private Long processDurationMs;
    private String processDurationFormatted;

    private AdminTaskInfoAnalytics adminTaskInfoAnalytics;
    //TODO: refine the task retrieval - convert to count and metrics
    private List<AdminTaskInfoVO> adminTaskInfoVOS;

}
