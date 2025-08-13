package org.eea.orchestrator.scheduling;

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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Component
public class JobForRestartingInProgressJobsWithInQueueProcess {

    /**
     * The Constant LOG.
     */
    private static final Logger LOG = LoggerFactory.getLogger(JobForRestartingInProgressJobsWithInQueueProcess.class);

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


    /* The maximum time in milliseconds for which an in progress job can have in queue process */
    @Value(value = "${scheduling.inQueue.process.inProgress.job.max.ms}")
    private long maxTimeForInQueueProcessInProgressJob;

    @PostConstruct
    private void init() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.initialize();
        scheduler.schedule(() -> restartInProgressJobsWithInQueueProcess(),
                new CronTrigger("0 */2 * * * *"));
    }

    /**
     * The job runs every 10 minutes. It finds validation jobs (without release) that have status=IN_PROGRESS and their process=IN_QUEUE
     * Then it checks the duration of the job and if needed it clears the locks, removes the process from the process and job_process tables and sets the status of the job to QUEUED.
     */
    public void restartInProgressJobsWithInQueueProcess() {
        try {
            LOG.info("[CHRIS] restartInProgressJobsWithInQueueProcess started");
            List<JobVO> jobList = jobService.findByJobTypeInAndJobStatusIn(Arrays.asList(JobTypeEnum.VALIDATION), Arrays.asList(JobStatusEnum.IN_PROGRESS));
            if(jobList == null || jobList.size() == 0){
                return;
            }
            LOG.info("[CHRIS] restartInProgressJobsWithInQueueProcess jobList size: {}", jobList.size());
            LOG.info("Running scheduled job restartInProgressJobsWithInQueueProcess");
            TokenVO tokenVo = userManagementControllerZull.generateToken(adminUser, adminPass);
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(adminUser, BEARER + tokenVo.getAccessToken(), null);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            for (JobVO job: jobList){
                try {
                    List<String> processIds = jobProcessService.findProcessesByJobId(job.getId());
                    if (processIds == null || processIds.isEmpty()) {
                        LOG.info("No processes found for job {}", job.getId());
                        continue;
                    }

                    List<ProcessVO> processVOList =
                            Optional.ofNullable(processControllerZuul.findByIds(processIds))
                                    .orElse(Collections.emptyList());
                    LOG.info("[CHRIS] restartInProgressJobsWithInQueueProcess processVOList size: {}",
                            processVOList.size());

                    long duration = new Date().getTime() - job.getDateStatusChanged().getTime();

                    for (ProcessVO processVO: processVOList) {
                        LOG.info("[CHRIS] restartInProgressJobsWithInQueueProcess processVO: {}", processVO);

                        if(ProcessStatusEnum.IN_QUEUE.name().equalsIgnoreCase(
                                Optional.ofNullable(processVO.getStatus()).orElse(""))
                                && duration > maxTimeForInQueueProcessInProgressJob){
                            validationControllerZuul.deleteLocksToReleaseProcess(job.getDatasetId());
                            processControllerZuul.deleteProcessByProcessId(processVO.getProcessId());
                            jobProcessService.deleteJobProcessByProcessId(processVO.getProcessId());
                            jobService.updateJobStatus(job.getId(), JobStatusEnum.QUEUED);
                        }
                    }
                }
                catch (Exception e){
                    LOG.error("Error when restarting job with id {}", job.getId(), e);
                }
            }
        } catch (Exception e) {
            LOG.error("Error while running scheduled task restartInProgressJobsWithInQueueProcess ", e);
        }
    }
}
