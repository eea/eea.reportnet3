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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JobForRestartingDelayedValidationTasks {

    @Value(value = "${scheduling.inProgress.validation.task.min.short.version}")
    private long shortTasksVersion;

    @Value(value = "${scheduling.inProgress.validation.task.min.time}")
    private long maxTimeInMinutesForShortInProgressTasks;

    @Value(value = "${scheduling.inProgress.validation.task.max.time}")
    private long maxTimeInMinutesForLongInProgressTasks;

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
            LOG.info("Restarting validation tasks scheduler");
            // Fetch validate tasks that run for short period of time, between the time windows provided by consul variables above
            List<TaskVO> tasksInProgressForShortPeriodOfTime = getTasksInProgressForShortPeriodOfTime();
            // Fetch validate tasks that run for long period of time that exceed max time provided by consul variable above
            List<TaskVO> tasksInProgressForLongPeriodOfTime = getTasksInProgressForLongPeriodOfTime();

            // combine them at one list and restart them
            List<TaskVO> tasksToBeRestarted = new ArrayList<>();
            tasksToBeRestarted.addAll(tasksInProgressForShortPeriodOfTime);
            tasksToBeRestarted.addAll(tasksInProgressForLongPeriodOfTime);

            if (!tasksToBeRestarted.isEmpty()) {
                TokenVO tokenVo = userManagementControllerZull.generateToken(adminUser, adminPass);
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(adminUser, BEARER + tokenVo.getAccessToken(), null);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                tasksToBeRestarted.stream().forEach(taskVO -> {
                    try {
                        if (taskVO!=null) {
//                            ObjectMapper objectMapper = new ObjectMapper();
//                            JsonNode jsonNode = objectMapper.readTree(taskVO.getJson());
//                            String eventType = jsonNode.get(EVENT_TYPE).asText();
//                            if (!eventType.equals(COMMAND_VALIDATE_TABLE)) {
                                LOG.info("Restarting task id: {}, task: {}", taskVO.getId(), taskVO);
                                validationControllerZuul.restartTask(taskVO.getId().longValue());
//                            }
                        }
                    } catch (Exception e) {
                        LOG.error("Error while running scheduled task restartDelayedTasks for task id: {}, task: {}", taskVO.getId(), taskVO);
                    }
                });
            }
            LOG.info("Restarting validation tasks completed. Short tasks: {}. Long tasks: {}.",
                    tasksInProgressForShortPeriodOfTime.stream().map(TaskVO::getId).collect(Collectors.toList()),
                    tasksInProgressForLongPeriodOfTime.stream().map(TaskVO::getId).collect(Collectors.toList()));
        } catch (Exception e) {
            LOG.error("Error while running scheduled task restartDelayedTasks {}", e);
        }
    }

    private List<TaskVO> getTasksInProgressForLongPeriodOfTime() {
        return validationControllerZuul.listInProgressValidationTasksThatExceedTime(maxTimeInMinutesForLongInProgressTasks);
    }

    private List<TaskVO> getTasksInProgressForShortPeriodOfTime() {
        return validationControllerZuul.listInProgressValidationTasksBetweenTime(maxTimeInMinutesForShortInProgressTasks, maxTimeInMinutesForLongInProgressTasks)
                .stream().filter(taskVO -> taskVO.getVersion() <= shortTasksVersion)
                .collect(Collectors.toList());
    }
}


















