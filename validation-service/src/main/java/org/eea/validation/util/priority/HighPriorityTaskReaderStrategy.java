package org.eea.validation.util.priority;

import java.util.List;
import org.eea.validation.persistence.data.metabase.domain.Task;
import org.eea.validation.persistence.data.metabase.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import lombok.NoArgsConstructor;

/**
 * The Class HighPriorityTaskReaderStrategy.
 */
@NoArgsConstructor
public class HighPriorityTaskReaderStrategy implements TaskReadStrategy {

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
    return taskRepository.findLastTask(limit);
  }

}
