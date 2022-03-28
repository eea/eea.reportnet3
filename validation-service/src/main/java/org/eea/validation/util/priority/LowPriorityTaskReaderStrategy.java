package org.eea.validation.util.priority;

import java.util.List;
import org.apache.commons.collections.CollectionUtils;
import org.eea.validation.persistence.data.metabase.domain.Task;
import org.eea.validation.util.ValidationHelper;
import lombok.NoArgsConstructor;

/**
 * The Class LowPriorityTaskReaderStrategy.
 */
@NoArgsConstructor
public class LowPriorityTaskReaderStrategy implements TaskReadStrategy {

  /** The validation helper. */
  private ValidationHelper validationHelper;

  /**
   * Instantiates a new low priority task reader strategy.
   *
   * @param validationHelper the validation helper
   */
  public LowPriorityTaskReaderStrategy(ValidationHelper validationHelper) {
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
    List<Long> tasksIds = validationHelper.getLastLowPriorityTask(limit);
    if (CollectionUtils.isEmpty(tasksIds)) {
      tasksIds = validationHelper.getLastHighPriorityTask(limit);
    }
    return validationHelper.getTasksByIds(tasksIds);
  }

}
