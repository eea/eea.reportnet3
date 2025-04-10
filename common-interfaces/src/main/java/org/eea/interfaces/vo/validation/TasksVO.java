package org.eea.interfaces.vo.validation;

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
public class TasksVO implements Serializable {

    private static final long serialVersionUID = -8922037724334609179L;

    /** The list of canceled tasks. */
    private List<?> tasksList;

    /** The total number of tasks (before pagination/filter). */
    private Long totalRecords;

    /** The number of tasks after any filter is applied. */
    private Long filteredRecords;

    /** The number of remaining tasks if partial page. */
    private Long remainingTasks;
}