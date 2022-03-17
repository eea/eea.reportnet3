package org.eea.validation.util.priority;

import java.util.List;
import org.eea.validation.persistence.data.metabase.domain.Task;
import org.eea.validation.util.ValidationHelper;
import lombok.NoArgsConstructor;

/**
 * The Class HighPriorityTaskReaderStrategy.
 */
@NoArgsConstructor
public class HighPriorityTaskReaderStrategy implements TaskReadStrategy {

  /** The validation helper. */
  private ValidationHelper validationHelper;

  /**
   * Instantiates a new high priority task reader strategy.
   *
   * @param validationHelper the validation helper
   */
  public HighPriorityTaskReaderStrategy(ValidationHelper validationHelper) {
    this.validationHelper = validationHelper;
  }

  /**
   * Gets the tasks.
   *
   * @param limit the limit
   * @return the tasks
   */
  @Override
  public List<Task> getTasks(int limit) {
    return validationHelper.getTasksByIds(validationHelper.getLastHighPriorityTask(limit));
  }

}
