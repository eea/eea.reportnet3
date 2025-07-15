package org.eea.orchestrator.scheduling;

import org.apache.commons.collections.ListUtils;
import org.eea.interfaces.controller.dataflow.DataFlowController.DataFlowControllerZuul;
import org.eea.interfaces.controller.ums.UserManagementController;
import org.eea.interfaces.vo.orchestrator.JobVO;
import org.eea.interfaces.vo.orchestrator.enums.JobStatusEnum;
import org.eea.interfaces.vo.orchestrator.enums.JobTypeEnum;
import org.eea.interfaces.vo.ums.TokenVO;
import org.eea.orchestrator.service.JobService;
import org.eea.security.authorization.AdminUserAuthorization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;
import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JobForRestartingLongRunningImportJobs {

    /**
     * The Constant LOG.
     */
    private static final Logger LOG = LoggerFactory.getLogger(JobForRestartingLongRunningImportJobs.class);

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

    @Value(value = "${scheduling.inProgress.import.task.max.ms.restart}")
    private long maxTimeForInProgressImportJobs;

    @Autowired
    private AdminUserAuthorization adminUserAuthorization;

    @Autowired
    private UserManagementController.UserManagementControllerZull userManagementControllerZull;

    @Autowired
    private JobService jobService;

    @Autowired
    DataFlowControllerZuul dataFlowControllerZuul;


    @PostConstruct
    private void init() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.initialize();
        scheduler.schedule(() -> restartLongRunningImportJobs(),
                new CronTrigger("0 */30 * * * *"));
    }

    /**
     * The job runs every 30 minutes. It finds stuck in progress jobs and tries to restart them
     */
    public void restartLongRunningImportJobs() {
        try {
            TokenVO tokenVo = null;
            List<JobVO> longRunningJobs = jobService.getJobsByTypeAndStatus(JobTypeEnum.IMPORT, JobStatusEnum.IN_PROGRESS);
            if(longRunningJobs != null && !longRunningJobs.isEmpty()){
                List<Long> jobIds = longRunningJobs.stream().map(JobVO::getId).collect(Collectors.toList());
                LOG.info("Trying to restart import jobs {}", jobIds);
                tokenVo = userManagementControllerZull.generateToken(adminUser, adminPass);
            }
            for (JobVO job: longRunningJobs){
                adminUserAuthorization.setAdminSecurityContextAuthenticationWithJobUserRoles(tokenVo, job);
                Long durationOfJob = new Timestamp(System.currentTimeMillis()).getTime() - job.getDateStatusChanged().getTime();
                if(durationOfJob > maxTimeForInProgressImportJobs){
                    jobService.restartImportJob(job.getId(), false);
                }
            }
        } catch (Exception e) {
            LOG.error("Unexpected error! Error while running scheduled task restartLongRunningImportTasks.", e);
        }

    }
}
