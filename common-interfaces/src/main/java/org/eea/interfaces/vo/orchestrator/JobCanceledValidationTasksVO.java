package org.eea.interfaces.vo.orchestrator;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * Represents a container of canceled validation tasks, plus summary stats.
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class JobCanceledValidationTasksVO implements Serializable {

    private static final long serialVersionUID = -8922037724334609179L;

    /** The list of canceled tasks. */
    private List<JobCanceledValidationTaskVO> tasksList;

    /** The total number of tasks. */
    private Long totalRecords;

    /** The number of tasks after any filter is applied. */
    private Long filteredRecords;

    /** The number of remaining tasks if partial page. */
    private Long remainingTasks;

}