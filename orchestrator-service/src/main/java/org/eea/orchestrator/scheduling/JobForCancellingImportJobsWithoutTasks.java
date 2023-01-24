package org.eea.orchestrator.scheduling;

import org.eea.interfaces.controller.dataset.DatasetController.DataSetControllerZuul;
import org.eea.interfaces.controller.recordstore.ProcessController;
import org.eea.interfaces.controller.ums.UserManagementController;
import org.eea.interfaces.controller.validation.ValidationController.ValidationControllerZuul;
import org.eea.interfaces.vo.orchestrator.JobVO;
import org.eea.interfaces.vo.orchestrator.enums.JobStatusEnum;
import org.eea.interfaces.vo.orchestrator.enums.JobTypeEnum;
import org.eea.interfaces.vo.recordstore.ProcessVO;
import org.eea.interfaces.vo.recordstore.enums.ProcessStatusEnum;
import org.eea.interfaces.vo.recordstore.enums.ProcessTypeEnum;
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
import java.math.BigInteger;
import java.util.List;

@Component
public class JobForCancellingImportJobsWithoutTasks {

    /**
     * The Constant LOG.
     */
    private static final Logger LOG = LoggerFactory.getLogger(JobForCancellingImportJobsWithoutTasks.class);

    /* The maximum time in milliseconds for which an import job can be in progress without any tasks */
    @Value(value = "${scheduling.inProgress.import.jobs.without.tasks.max.time}")
    private Long maxTimeInMsForInProgressImportJobsWithoutTasks;

    /* The maximum time in milliseconds for which a fme import job can be in progress without any tasks */
    @Value(value = "${scheduling.inProgress.fme.import.jobs.without.tasks.max.time}")
    private Long maxTimeInMsForInProgressFMEImportJobsWithoutTasks;


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
    private JobProcessService jobProcessService;
    @Autowired
    private JobService jobService;

    @Autowired
    private ValidationControllerZuul validationControllerZuul;

    @Autowired
    private DataSetControllerZuul dataSetControllerZuul;

    @Autowired
    private ProcessController.ProcessControllerZuul processControllerZuul;

    @Autowired
    private UserManagementController.UserManagementControllerZull userManagementControllerZull;

    @PostConstruct
    private void init() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.initialize();
        scheduler.schedule(() -> cancelInProgressImportJobsWithoutTasks(),
                new CronTrigger("0 0 * * * *"));
    }

    /**
     * The job runs every hour. It finds import jobs that have status=IN_PROGRESS for more than maxTimeInMinutesForInProgressImportJobsWithoutTasks
     * and have no tasks created and sets the status of the job and the processes to CANCELED, as well as removed the locks
     */
    public void cancelInProgressImportJobsWithoutTasks() {
        try {
            LOG.info("Running scheduled job cancelInProgressImportJobsWithoutTasks");
            List<JobVO> longRunningJobs = jobService.getJobsByStatusAndTypeAndMaxDuration(JobTypeEnum.IMPORT, JobStatusEnum.IN_PROGRESS, maxTimeInMsForInProgressImportJobsWithoutTasks, maxTimeInMsForInProgressFMEImportJobsWithoutTasks);
            Boolean longRunningProcessWithoutTasks = false;
            for (JobVO job: longRunningJobs){
                //get job processes
                List<String> processIds = jobProcessService.findProcessesByJobId(job.getId());
                for(String processId: processIds){
                    List<BigInteger> tasks = validationControllerZuul.findTasksByProcessId(processId);
                    if(tasks.size() == 0) {
                        ProcessVO processVO = processControllerZuul.findById(processId);
                        //update process status
                        processControllerZuul.updateProcess(processVO.getDatasetId(), processVO.getDataflowId(),
                                ProcessStatusEnum.CANCELED, ProcessTypeEnum.IMPORT, processVO.getProcessId(),
                                processVO.getUser(), processVO.getPriority(), processVO.isReleased());
                        longRunningProcessWithoutTasks = true;
                        LOG.info("Updated import process to status CANCELED for jobId {} and processId {}", job.getId(), processVO.getProcessId());
                    }
                }
                if(longRunningProcessWithoutTasks){
                    //update job status
                    jobService.updateJobStatus(job.getId(), JobStatusEnum.FAILED);
                    LOG.info("Updated import job to status FAILED for jobId {}", job.getId());

                    //remove locks
                    TokenVO tokenVo = userManagementControllerZull.generateToken(adminUser, adminPass);
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(adminUser, BEARER + tokenVo.getAccessToken(), null);
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    dataSetControllerZuul.deleteLocksToImportProcess(job.getDatasetId());
                    LOG.info("Locks removed for failed job {}, datasetId {}", job.getId(), job.getDatasetId());
                }
            }
        } catch (Exception e) {
            LOG.error("Error while running scheduled task cancelInProgressValidationsWithoutTasks " + e.getMessage());
        }
    }
}