package org.eea.interfaces.vo.orchestrator;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class AdminTaskInfoAnalytics {

    private String processId;
    private String processType;
    private Long dataflowId;
    private Long datasetId;
    private String status;

    /* Metrics/Analytics */
    private int totalTasks;
    private int queuedTasks;
    private int inProgressTasks;
    private int canceledTasks;
    private int finishedTasks;

    private long minimumFinishedTaskDurationMs;
    private long maximumFinishedTaskDurationMs;
    private long averageFinishedTaskDurationMs;
    private long minimumInProgressTaskDurationMs;
    private long maximumInProgressTaskDurationMs;

    private int maximumVersionOfAnyTask;


}
