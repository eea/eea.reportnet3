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
public class JobsVO {

    /** The process list. */
    private List<JobVO> jobsList;

    /** The total records. */
    private Long totalRecords;

    /** The filtered records. */
    private Long filteredRecords;

    /** The remaining jobs. */
    private Long remainingJobs;
}
