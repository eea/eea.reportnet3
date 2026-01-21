package org.eea.validation.kafka.command;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.recordstore.enums.ProcessStatusEnum;
import org.eea.kafka.commands.AbstractEEAEventHandlerCommand;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.eea.lock.redis.LockEnum;
import org.eea.lock.redis.RedisLockService;
import org.eea.validation.persistence.data.metabase.domain.Task;
import org.eea.validation.persistence.data.metabase.repository.TaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Optional;

@Component
public class HighPriorityTaskCommand extends AbstractEEAEventHandlerCommand {

    private static final Logger LOG = LoggerFactory.getLogger(HighPriorityTaskCommand.class);

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private KafkaSenderUtils kafkaSenderUtils;

    @Autowired
    private RedisLockService redisLockService;

    @Value("${spring.cloud.consul.discovery.instanceId}")
    private String serviceInstanceId;

    private static final long lockExpirationInMillis = 600000L;

    @Override
    public EventType getEventType() {
        return EventType.HIGH_PRIORITY_TASK_CREATED_EVENT;
    }

    @Override
    public void execute(EEAEventVO eeaEventVO) throws EEAException {

        final long taskId = (long) eeaEventVO.getData().get("taskId");
        final Optional<Task> optionalTask = taskRepository.findById(taskId);

        if (optionalTask.isEmpty()) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ignored) {

            }
            finally {
                kafkaSenderUtils.releaseKafkaEvent(eeaEventVO);
            }
            return;
        }

        final Task task = optionalTask.get();
        final String lockKey = LockEnum.TASK_SCHEDULER.getValue() + "_" + task.getId();
        final String value = task.getStatus().toString();

        try {
            if (redisLockService.checkAndAcquireLock(lockKey, value, lockExpirationInMillis)) {
                task.setStartingDate(new Date());
                task.setPod(serviceInstanceId);
                task.setStatus(ProcessStatusEnum.IN_PROGRESS);
                taskRepository.save(task);
                taskRepository.flush();

                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                EEAEventVO event = objectMapper.readValue(task.getJson(), EEAEventVO.class);
                Message<EEAEventVO> message = MessageBuilder.withPayload(event).build();
                message.getPayload().getData().put("task_id", task.getId());
                kafkaSenderUtils.releaseKafkaEvent(event);
            }
        } catch (JsonProcessingException e) {
            LOG.error("Failed processing the validation task because of", e);
        } finally {
            redisLockService.releaseLock(lockKey, value);
        }
    }
}
