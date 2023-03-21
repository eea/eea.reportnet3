package org.eea.orchestrator.scheduling;

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
import java.util.List;

@Component
public class JobForExecutingQueuedJobs {

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

    @Autowired
    private AdminUserAuthorization adminUserAuthorization;

    /**
     * The Constant LOG.
     */
    private static final Logger LOG = LoggerFactory.getLogger(JobForExecutingQueuedJobs.class);


    @Autowired
    private UserManagementController.UserManagementControllerZull userManagementControllerZull;

    @Autowired
    private JobService jobService;

    @PostConstruct
    private void init() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.initialize();
        scheduler.schedule(() -> executeQueuedJobs(),
                new CronTrigger("0 */1 * * * *"));
    }

    /**
     * The job runs every 1 minute. It finds jobs that have status=QUEUED and executes them
     */
    public void executeQueuedJobs() {
        try {
            List<JobVO> jobs = jobService.getJobsByStatus(JobStatusEnum.QUEUED);
            if(jobs == null || jobs.size() == 0){
                return;
            }
            LOG.info("Running scheduled task executeQueuedJobs");
            TokenVO tokenVo = userManagementControllerZull.generateToken(adminUser, adminPass);
            for (JobVO job: jobs) {
                try {
                    adminUserAuthorization.setAdminSecurityContextAuthenticationWithJobUserRoles(tokenVo, job);
                    if (!jobService.canJobBeExecuted(job)) {
                        LOG.info("Job with id {} and of type {} can not be executed right now.", job.getId(), job.getJobType().getValue());
                        continue;
                    }
                    LOG.info("Trying to execute job with id {} and of type {}", job.getId(), job.getJobType().getValue());
                    if (job.getJobType() == JobTypeEnum.VALIDATION && !job.isRelease()) {
                        LOG.info("Job with id {} and of type {} will be executed.", job.getId(), job.getJobType().getValue());
                        //call validation mechanism
                        jobService.prepareAndExecuteValidationJob(job);
                    } else if (job.getJobType() == JobTypeEnum.VALIDATION && job.isRelease()) {
                        //check if another release is already running for the dataflow, but for another provider
                        if (!jobService.canExecuteReleaseOnDataflow(job.getDataflowId())) {
                            continue;
                        }
                        LOG.info("Job with id {} and of type {} will be executed.", job.getId(), job.getJobType().getValue());
                        //call release mechanism
                        jobService.prepareAndExecuteReleaseJob(job);
                    } else if (job.getJobType() == JobTypeEnum.FILE_EXPORT) {
                        //call export file mechanism
                        //todo
                    } else if (job.getJobType() == JobTypeEnum.COPY_TO_EU_DATASET) {
                        LOG.info("Job with id {} and of type {} will be executed.", job.getId(), job.getJobType().getValue());
                        jobService.prepareAndExecuteCopyToEUDatasetJob(job);
                    } else {
                        LOG.error("Error trying to execute queued job with id {}. Job type is {}", job.getId(), job.getJobType().getValue());
                        jobService.deleteJob(job);
                    }
                } catch (Exception e) {
                    LOG.error("Unexpected error! Error while handling job with id {}. ", job.getId(), e);
                }
            }
        } catch (Exception e) {
            LOG.error("Unexpected error! Error while running scheduled task executeQueuedJobs. ", e);
        }
    }
}




