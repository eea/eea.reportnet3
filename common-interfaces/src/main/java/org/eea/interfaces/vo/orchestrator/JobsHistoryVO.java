package org.eea.interfaces.vo.orchestrator;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class JobsHistoryVO {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -1348263779137653665L;

    /** The process list. */
    private List<JobHistoryVO> jobHistoryVOList;

    /** The total records. */
    private Long totalRecords;

    /** The filtered records. */
    private Long filteredRecords;

    /** The total jobs inside job history filtered. */
    private Long filteredJobs;
}
