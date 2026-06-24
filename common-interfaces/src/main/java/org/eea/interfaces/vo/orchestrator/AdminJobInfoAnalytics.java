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
public class AdminJobInfoAnalytics {

    private List<Long> datasetId;
    private Long providerId;
    private Long fmeJobId;
    private String jobStatus;

    /* Metrics/Analytics */
    List<AdminProcessInfoAnalytics> adminProcessInfoAnalytics;
}
