package org.eea.orchestrator.scheduling;

import org.eea.interfaces.controller.dataset.DatasetSnapshotController;
import org.eea.interfaces.controller.recordstore.ProcessController.ProcessControllerZuul;
import org.eea.interfaces.controller.ums.UserManagementController;
import org.eea.interfaces.controller.validation.ValidationController.ValidationControllerZuul;
import org.eea.interfaces.vo.orchestrator.JobVO;
import org.eea.interfaces.vo.orchestrator.enums.JobStatusEnum;
import org.eea.interfaces.vo.orchestrator.enums.JobTypeEnum;
import org.eea.interfaces.vo.recordstore.ProcessVO;
import org.eea.interfaces.vo.recordstore.enums.ProcessStatusEnum;
import org.eea.interfaces.vo.ums.TokenVO;
import org.eea.orchestrator.service.JobProcessService;
import org.eea.orchestrator.service.JobService;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class JobForRestartingInProgressJobsWithInQueueProcess {

    private static final Logger LOG = LoggerFactory.getLogger(JobForRestartingInProgressJobsWithInQueueProcess.class);

    private static final String BEARER = "Bearer ";

    @Value("${eea.keycloak.admin.user}")
    private String adminUser;

    /**
     * The admin pass.
     */
    @Value("${eea.keycloak.admin.password}")
    private String adminPass;

    @Autowired
    private ValidationControllerZuul validationControllerZuul;

    @Autowired
    private JobService jobService;

    @Autowired
    private JobProcessService jobProcessService;

    @Autowired
    private ProcessControllerZuul processControllerZuul;

    @Autowired
    private UserManagementController.UserManagementControllerZull userManagementControllerZull;

    @Autowired
    private DatasetSnapshotController.DataSetSnapshotControllerZuul dataSetSnapshotControllerZuul;

    /* The maximum time in milliseconds for which an in progress job can have in queue process */
    @Value(value = "${scheduling.inQueue.process.inProgress.job.max.ms}")
    private long maxTimeForInQueueProcessInProgressJob;

    @PostConstruct
    private void init() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.initialize();
        scheduler.schedule(this::restartInProgressJobsWithInQueueProcess,
                new CronTrigger("0 */10 * * * *"));
    }

    /**
     * The job runs every 10 minutes. It finds validation jobs (without release) that have status=IN_PROGRESS and their process=IN_QUEUE
     * Then it checks the duration of the job and if needed it clears the locks, removes the process from the process and job_process tables and sets the status of the job to QUEUED.
     */
    public void restartInProgressJobsWithInQueueProcess() {
        try {
            LOG.info("Running scheduled job restartInProgressJobsWithInQueueProcess");

            List<JobVO> jobList = jobService.findByJobTypeInAndJobStatusIn(
                    Collections.singletonList(JobTypeEnum.VALIDATION),
                    Collections.singletonList(JobStatusEnum.IN_PROGRESS));

            if (jobList == null || jobList.isEmpty()) {
                return;
            }

            authenticateAsAdmin();

            for (JobVO job : jobList) {
                try {
                    // find and handle the processes of that job
                    handleJobRestartIfStuck(job);
                } catch (Exception e) {
                    LOG.error("Error when restarting job with id {}", job.getId(), e);
                }
            }
        } catch (Exception e) {
            LOG.error("Error while running scheduled task restartInProgressJobsWithInQueueProcess", e);
        }
    }

    private void handleJobRestartIfStuck(JobVO job) {
        long durationMs = System.currentTimeMillis() - job.getDateStatusChanged().getTime();
        if (durationMs <= maxTimeForInQueueProcessInProgressJob) {
            return; // exit loop, this job doesn't need to be restarted
        }

        List<String> processIds = jobProcessService.findProcessesByJobId(job.getId());
        if (processIds == null || processIds.isEmpty()) {
            LOG.info("No processes found for job {}", job.getId());
            return;
        }

        List<ProcessVO> processes = Optional.ofNullable(processControllerZuul.findByIds(processIds))
                .orElse(Collections.emptyList());

        List<ProcessVO> stuckProcesses = processes.stream()
                .filter(p -> ProcessStatusEnum.IN_QUEUE.name().equalsIgnoreCase(
                        Optional.ofNullable(p.getStatus()).orElse("")))
                .collect(Collectors.toList());

        if (stuckProcesses.isEmpty()) {
            LOG.info("Job {} has no IN_QUEUE processes to restart", job.getId());
            return; // exit loop, this job doesn't need to be restarted
        }

        // clear locks once per job
        clearLocksForJob(job);

        // delete stuck processes
        for (ProcessVO p : stuckProcesses) {
            try {
                processControllerZuul.deleteProcessByProcessId(p.getProcessId());
                jobProcessService.deleteJobProcessByProcessId(p.getProcessId());
            } catch (Exception ex) {
                LOG.warn("Partial failure deleting process {} for job {}", p.getProcessId(), job.getId(), ex);
            }
        }

        // update job status once per job
        jobService.updateJobStatus(job.getId(), JobStatusEnum.QUEUED);

        LOG.info("Job {} restarted: deleted {} stuck processes and set status to QUEUED",
                job.getId(), stuckProcesses.size());
    }

    private void clearLocksForJob(JobVO job) {
        boolean isRelease = job.isRelease();
        if (isRelease) {
            try {
                dataSetSnapshotControllerZuul.releaseLocksFromReleaseDatasets(
                        job.getDataflowId(), job.getProviderId());
                LOG.info("Cleared release locks for job {}, dataflowId={}, providerId={}",
                        job.getId(), job.getDataflowId(), job.getProviderId());
            } catch (Exception e) {
                LOG.warn("Failed to clear release locks for job {}", job.getId(), e);
            }
        } else {
            if (job.getDatasetId() == null) {
                LOG.warn("Job {} is non-release but datasetId is null. Skipping lock deletion.", job.getId());
                return;
            }
            try {
                validationControllerZuul.deleteLocksToReleaseProcess(job.getDatasetId());
                LOG.info("Cleared locks for job {} with datasetId {}", job.getId(), job.getDatasetId());
            } catch (Exception e) {
                LOG.warn("Failed to clear locks for datasetId {} (job {})", job.getDatasetId(), job.getId(), e);
            }
        }
    }

    private void authenticateAsAdmin() {
        TokenVO tokenVo = userManagementControllerZull.generateToken(adminUser, adminPass);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(adminUser, BEARER + tokenVo.getAccessToken(), null);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
