package org.eea.validation.util;

import java.util.Date;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.lock.enums.LockSignature;
import org.eea.interfaces.vo.recordstore.enums.ProcessStatusEnum;
import org.eea.job.JobScheduler;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.lock.redis.LockEnum;
import org.eea.lock.redis.RedisLockService;
import org.eea.message.MessageReceiver;
import org.eea.validation.persistence.data.metabase.domain.Task;
import org.eea.validation.persistence.data.metabase.repository.TaskRepository;
import org.eea.validation.util.priority.HighPriorityTaskReaderStrategy;
import org.eea.validation.util.priority.LowPriorityTaskReaderStrategy;
import org.eea.validation.util.priority.TaskReadStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The Class ValidationScheduler.
 */
@Component
public class ValidationScheduler extends MessageReceiver {

  /** The Constant LOG_ERROR. */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory.getLogger(ValidationScheduler.class);

  /** The task repository. */
  @Autowired
  private TaskRepository taskRepository;

  /** The validation helper. */
  @Autowired
  private ValidationHelper validationHelper;

  /** The scheduler. */
  @Autowired
  private JobScheduler scheduler;

  @Autowired
  private RedisLockService redisLockService;

  /** The delay. */
  @Value("${validation.scheduled.consumer}")
  private Long delay;

  /** The max running tasks. */
  @Value("${validation.tasks.parallelism}")
  private int maxRunningTasks;

  /** The service instance id. */
  @Value("${spring.cloud.consul.discovery.instanceId}")
  private String serviceInstanceId;

  /** The instance priority. */
  @Value("${validation.instance.priority}")
  private String instancePriority;

  /** The task read strategy. */
  private TaskReadStrategy taskReadStrategy;

  private static final long lockExpirationInMillis = 600000L;


  /**
   * Inits the scheduler.
   */
  @PostConstruct
  void init() {
    if ("HIGH".equals(instancePriority)) {
      taskReadStrategy = new HighPriorityTaskReaderStrategy(validationHelper);
    } else {
      taskReadStrategy = new LowPriorityTaskReaderStrategy(validationHelper);
    }
    scheduler.schedule(() -> scheduledConsumer(), delay, TimeUnit.MILLISECONDS);
  }

  /**
   * Scheduled consumer.
   */
  public void scheduledConsumer() {
    Long newDelay = delay;
    try {
      int freeThreads = checkFreeThreads();
      if (freeThreads > 0) {
        for (Task task : taskReadStrategy.getTasks(freeThreads)) {
          String lockKey = LockEnum.TASK_SCHEDULER.getValue() + "_" + task.getId();
          String value = task.getStatus().toString();
          try {
            if (redisLockService.checkAndAcquireLock(lockKey, value, lockExpirationInMillis)) {
              task.setStartingDate(new Date());
              task.setPod(serviceInstanceId);
              if (task.getStatus() == ProcessStatusEnum.IN_PROGRESS) {
                continue;
              }
              task.setStatus(ProcessStatusEnum.IN_PROGRESS);
              taskRepository.save(task);
              taskRepository.flush();
              ObjectMapper objectMapper = new ObjectMapper();
              objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
              EEAEventVO event = objectMapper.readValue(task.getJson(), EEAEventVO.class);
              Message<EEAEventVO> message = MessageBuilder.withPayload(event).build();
              message.getPayload().getData().put("task_id", task.getId());
              consumeMessage(message);
            }
          } catch (EEAException | JsonProcessingException e) {
            LOG_ERROR.error("failed the validation task shedule because of {} ", e);
          } catch (ObjectOptimisticLockingFailureException e) {
            newDelay = 1L;
          } finally {
            redisLockService.releaseLock(lockKey, value);
          }
        }
      }

    } finally {
      scheduler.schedule(() -> scheduledConsumer(), newDelay, TimeUnit.MILLISECONDS);

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
   * @return the int
   */
  private int checkFreeThreads() {
    return maxRunningTasks - validationHelper.getUsedExecutionThreads();
  }

}

