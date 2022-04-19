package org.eea.validation.util.priority;

import java.util.List;
import org.eea.validation.persistence.data.metabase.domain.Task;

/**
 * The Interface TaskReadStrategy.
 */
public interface TaskReadStrategy {

  /**
   * Gets the tasks.
   *
   * @return the tasks
   */
  List<Task> getTasks(int limit);
}
