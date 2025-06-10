package org.eea.orchestrator.scheduling;

import org.eea.interfaces.controller.ums.UserManagementController.UserManagementControllerZull;
import org.eea.interfaces.controller.validation.ValidationController.ValidationControllerZuul;
import org.eea.interfaces.vo.ums.TokenVO;
import org.eea.interfaces.vo.validation.TaskVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.math.BigInteger;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;

@Component
public class JobForRestartingDelayedValidationTasks {

    @Value(value = "${scheduling.inProgress.validation.task.max.time}")
    private long maxTimeInMinutesForInProgressTasks;

    @Value(value = "${scheduling.inProgress.validation.task.mid.time}")
    private long midTimeInMinutesForInProgressTasks;

    @Value(value = "${scheduling.inProgress.validation.task.min.time}")
    private long minTimeInMinutesForInProgressTasks;


    /**
     * The admin user.
     */
    @Value("${eea.keycloak.admin.user}")
    private String adminUser;

    /**
     * The admin pass.
     */
    @Value("${eea.keycloak.admin.password}")
    private String adminPass;


    private static final String BEARER = "Bearer ";
    private static final String EVENT_TYPE = "eventType";
    private static final String COMMAND_VALIDATE_TABLE = "COMMAND_VALIDATE_TABLE";

    /**
     * The Constant LOG.
     */
    private static final Logger LOG = LoggerFactory.getLogger(JobForRestartingDelayedValidationTasks.class);

    @Autowired
    private ValidationControllerZuul validationControllerZuul;

    @Autowired
    private UserManagementControllerZull userManagementControllerZull;

    @PostConstruct
    private void init() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.initialize();
        scheduler.schedule(() -> restartDelayedTasks(),
                new CronTrigger("0 */5 * * * *"));
    }

    /**
     * The job runs every 5 minutes. It finds task ids for tasks that have status=IN_PROGRESS for more than maxTimeInMinutesForInProgressTasks
     * and sets their status to status=IN_QUEUE.
     */
    public void restartDelayedTasks() {
        try {
            List<BigInteger> tasksInProgress = validationControllerZuul.listInProgressValidationTasksThatExceedTime(minTimeInMinutesForInProgressTasks);
            if (tasksInProgress.size() > 0) {
                TokenVO tokenVo = userManagementControllerZull.generateToken(adminUser, adminPass);
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(adminUser, BEARER + tokenVo.getAccessToken(), null);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                tasksInProgress.stream().forEach(taskId -> {
                    try {
                        TaskVO taskVO = validationControllerZuul.findTaskById(taskId.longValue());
                        if (taskVO!=null) {
                            long taskAgeMinutes = computeAgeMinutesForInProgressTask(taskVO.getStartingDate());
                            long taskMaxAllowedMinutes = getMaxAllowedTimeForInProgressTaskVersion(taskVO.getVersion());

                            if (taskAgeMinutes > taskMaxAllowedMinutes) {
                                LOG.info("Restarting delayed task {} (version: {}, age: {}m)", taskVO, taskVO.getVersion(), taskAgeMinutes);
                                validationControllerZuul.restartTask(taskId.longValue());
                            }
                        }
                    } catch (Exception e) {
                        LOG.error("Error while running scheduled task restartDelayedTasks for task id {}", taskId);
                    }
                });
            }
        } catch (Exception e) {
            LOG.error("Error while running scheduled task restartDelayedTasks {}", e);
        }
    }

    private long computeAgeMinutesForInProgressTask(Date inProgressTaskStartingDate) {
        if (inProgressTaskStartingDate == null) {
            LOG.warn("Task startingDate is null, assuming 0 minutes in progress.");
            return 0;
        }
        return Duration.between(inProgressTaskStartingDate.toInstant(), Instant.now()).toMinutes();
    }

    private long getMaxAllowedTimeForInProgressTaskVersion(Integer version) {
        if (version == null || version < 2) return minTimeInMinutesForInProgressTasks;
        if (version <= 4) return midTimeInMinutesForInProgressTasks;
        return maxTimeInMinutesForInProgressTasks;
    }
}

