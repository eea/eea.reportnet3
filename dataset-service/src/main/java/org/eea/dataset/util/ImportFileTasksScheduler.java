package org.eea.dataset.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eea.dataset.persistence.metabase.domain.Task;
import org.eea.dataset.persistence.metabase.repository.TaskRepository;
import org.eea.dataset.service.helper.FileTreatmentHelper;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.metabase.TaskType;
import org.eea.interfaces.vo.recordstore.enums.ProcessStatusEnum;
import org.eea.job.JobScheduler;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.message.MessageReceiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * The Class ValidationScheduler.
 */
@Component
public class ImportFileTasksScheduler extends MessageReceiver {

  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory.getLogger(ImportFileTasksScheduler.class);

  /** The task repository. */
  @Autowired
  private TaskRepository taskRepository;

  /** The validation helper. */

  @Autowired
  private FileTreatmentHelper fileTreatmentHelper;

  /** The scheduler. */
  @Autowired
  private JobScheduler scheduler;

  /** The delay. */
  private Long delay=5000l;

  /** The max running tasks. */
  private int maxRunningTasks=4;

  /** The service instance id. */
  @Value("${spring.cloud.consul.discovery.instanceId}")
  private String serviceInstanceId;

  /** The instance priority. */
  private String instancePriority="HIGH";




  /**
   * Inits the scheduler.
   */
  @PostConstruct
  void init() {

    scheduler.schedule(() -> scheduledConsumer(), delay, TimeUnit.MILLISECONDS);
  }

  /**
   * Scheduled consumer.
   */
  @Transactional
  public void scheduledConsumer() {
    Long newDelay = delay;
    try {
      Task task = taskRepository.findFirstByTaskTypeAndStatusOrderByVersionAscIdAsc(TaskType.IMPORT_TASK, ProcessStatusEnum.IN_QUEUE);
      if (task!=null) {
        try {
          ObjectMapper objectMapper = new ObjectMapper();
          objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
          EEAEventVO event = objectMapper.readValue(task.getJson(), EEAEventVO.class);
          String filePath = String.valueOf(event.getData().get("filePath"));
          InputStream inputStream = null;
          try {
            inputStream = Files.newInputStream(Path.of(filePath));
          } catch (IOException er) {
            return;
          } finally {
            if (inputStream != null) {
              inputStream.close();
            }
          }
          task.setStartingDate(new Date());
          task.setPod(serviceInstanceId);
          task.setStatus(ProcessStatusEnum.IN_PROGRESS);
          taskRepository.saveAndFlush(task);

          Message<EEAEventVO> message = MessageBuilder.withPayload(event).build();
          message.getPayload().getData().put("task_id", task.getId());
          message.getPayload().getData().put("service_instance_id", serviceInstanceId);

          consumeMessage(message);
        } catch (EEAException | JsonProcessingException e) {
          LOG.error("failed the import task schedule because of {} ", e);
        } catch (ObjectOptimisticLockingFailureException | IOException e) {
          newDelay = 1L;
          LOG.error(e.getMessage());
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
        LOG.error("Error processing event {} due to reason {}", message.getPayload(), e);
      } catch (Exception e) {
        LOG.error("Undetermined  processing message {} due to reason {}", message, e);
      }
    }
  }


  /**
   * Check free threads.
   *
   * @return the int
   */
  private int checkFreeThreads() {
    return maxRunningTasks - fileTreatmentHelper.getUsedExecutionThreads();
  }

}

