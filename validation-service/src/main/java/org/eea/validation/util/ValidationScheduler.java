package org.eea.validation.util;

import org.eea.exception.EEAException;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.message.MessageReceiver;
import org.eea.validation.persistence.data.metabase.domain.Task;
import org.eea.validation.persistence.data.metabase.repository.TaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.google.gson.Gson;

/**
 * The Class ValidationScheduler.
 */
@Component
public class ValidationScheduler extends MessageReceiver {

  /** The task repository. */
  @Autowired
  private TaskRepository taskRepository;

  /** The validation helper. */
  @Autowired
  private ValidationHelper validationHelper;

  /** The Constant LOG_ERROR. */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /**
   * Scheduled consumer.
   *
   * @throws EEAException the EEA exception
   */
  @Scheduled(fixedDelayString = "${validation.scheduled.consumer}")
  public void scheduledConsumer() throws EEAException {
    if (checkFreeThreads()) {
      Task task = taskRepository.findLastTask();
      if (task != null) {
        Gson g = new Gson();
        EEAEventVO event = g.fromJson(task.getJson(), EEAEventVO.class);
        Message<EEAEventVO> message = MessageBuilder.withPayload(event).build();
        message.getPayload().getData().put("task_id", task.getId());
        consumeMessage(message);
      }
    }
  }

  /**
   * Consume message.
   *
   * @param message the message
   * @throws EEAException the EEA exception
   */
  @Override
  public void consumeMessage(Message<EEAEventVO> message) throws EEAException {
    if (null != handler) {
      try {
        handler.processMessage(message.getPayload());
      } catch (EEAException e) {
        LOG_ERROR.error("Error processing event {} due to reason {}", message.getPayload(), e);
      } catch (Exception e) {
        LOG_ERROR.error("Undetermined  processing message {} due to reason {}", message, e);
      }
    }
  }


  /**
   * Check free threads.
   *
   * @return true, if successful
   */
  private boolean checkFreeThreads() {
    return validationHelper.getAvailableExecutionThreads() > 0;
  }

}

