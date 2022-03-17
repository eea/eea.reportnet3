package org.eea.validation.util.priority;

import java.util.List;
import org.apache.commons.collections.CollectionUtils;
import org.eea.validation.persistence.data.metabase.domain.Task;
import org.eea.validation.persistence.data.metabase.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import lombok.NoArgsConstructor;

/**
 * The Class LowPriorityTaskReaderStrategy.
 */
@NoArgsConstructor
public class LowPriorityTaskReaderStrategy implements TaskReadStrategy {

  /** The task repository. */
  @Autowired
  private TaskRepository taskRepository;

  /**
   * Gets the tasks.
   *
   * @param limit the limit
   * @return the tasks
   */
  @Override
  public List<Task> getTasks(int limit) {
    List<Long> tasksIds = taskRepository.findLastLowPriorityTask(limit);
    if (CollectionUtils.isEmpty(tasksIds)) {
      tasksIds = taskRepository.findLastTask(limit);
    }
    return taskRepository.findAllWithIds(tasksIds);
  }

}
