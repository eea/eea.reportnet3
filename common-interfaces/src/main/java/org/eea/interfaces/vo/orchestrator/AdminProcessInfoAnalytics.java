package org.eea.interfaces.vo.orchestrator;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class AdminProcessInfoAnalytics {

    private Long jobId;
    private String jobType;
    private Long dataflowId;
    private List<Long> datasetId;
    private Long providerId;
    private Long fmeJobId;
    private String jobStatus;

    /* Metrics/Analytics */
    private Long totalProcesses;
    private Long queuedProcesses;
    private Long inProgressProcesses;
    private Long canceledProcesses;
    private Long finishedProcesses;
    private Long minimumFinishedProcessDurationMs;
    private Long maximumFinishedProcessDurationMs;
    private Long averageFinishedProcessDurationMs;
    private Long minimumInProgressProcessDurationMs;
    private Long maximumInProgressProcessDurationMs;
}
