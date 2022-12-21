package org.eea.orchestrator.scheduling;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eea.interfaces.controller.ums.UserManagementController.UserManagementControllerZull;
import org.eea.interfaces.controller.recordstore.RecordStoreController.RecordStoreControllerZuul;
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
import java.util.List;

@Component
public class JobForRestartingReleaseTasks {

    @Value(value = "${scheduling.inProgress.release.task.max.time}")
    private long maxTimeInMinutesForInProgressReleaseTasks;

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
    /**
     * The Constant LOG.
     */
    private static final Logger LOG = LoggerFactory.getLogger(JobForRestartingReleaseTasks.class);

    @Autowired
    private RecordStoreControllerZuul recordStoreControllerZuul;

    @Autowired
    private UserManagementControllerZull userManagementControllerZull;

    @PostConstruct
    private void init() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.initialize();
        scheduler.schedule(() -> restartReleaseTasks(),
            new CronTrigger("0 */30 * * * *"));
    }

    /**
     * The job runs every 5 minutes. It finds task ids for tasks that have status=IN_PROGRESS for more than maxTimeInMinutesForInProgressTasks
     * and sets their status to status=IN_QUEUE.
     */
    public void restartReleaseTasks() {
        try {
            LOG.info("Method restartReleaseTask started");
            //Get the release tasks which are in progress
            List<BigInteger> releaseTasksInProgress = recordStoreControllerZuul.findReleaseTasksInProgress(maxTimeInMinutesForInProgressReleaseTasks);
            ObjectMapper objectMapper = new ObjectMapper();
            LOG.info("Release tasks in progress {}", releaseTasksInProgress);

            if (releaseTasksInProgress.size() > 0) {
                TokenVO tokenVo = userManagementControllerZull.generateToken(adminUser, adminPass);
                UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(adminUser, BEARER + tokenVo.getAccessToken(), null);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                releaseTasksInProgress.stream().forEach(taskId -> {
                    try {
                        TaskVO releaseTask = recordStoreControllerZuul.findReleaseTaskByTaskId(taskId.longValue());
                        LOG.info("Release task data {}", releaseTask);
                        JsonNode jsonNode = objectMapper.readTree(releaseTask.getJson());
                        long datasetId = jsonNode.get("datasetId").asLong();
                        long snapshotId = jsonNode.get("snapshotId").asLong();
                        int splitFileId = jsonNode.get("splitFileId").asInt();
                        int numberOfSplitFiles = jsonNode.get("numberOfSplitFiles").asInt();
                        String firstFieldId = jsonNode.get("firstFieldId").asText();
                        String lastFieldId = jsonNode.get("lastFieldId").asText();

                        boolean currentFileHasBeenCopied = recordStoreControllerZuul.recoverCheck(datasetId, firstFieldId, lastFieldId);
                        if (currentFileHasBeenCopied) {
                            splitFileId++;
                        }
                        LOG.info("Release task currentFileHasBeenCopied: {} with splitFileId: {}", currentFileHasBeenCopied, splitFileId);

                        recordStoreControllerZuul.restoreSpecificFileSnapshotData(datasetId, snapshotId, splitFileId, numberOfSplitFiles, releaseTask.getProcessId());
                    } catch (Exception e) {
                        LOG.error("Error while restarting release task for task id {}", taskId);
                    }
                });
            }
        } catch (Exception e) {
            LOG.error("Error while restarting release task", e.getMessage());
        }
    }
}
