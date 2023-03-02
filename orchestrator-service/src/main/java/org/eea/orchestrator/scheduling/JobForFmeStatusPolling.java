package org.eea.orchestrator.scheduling;

import org.eea.interfaces.controller.dataset.DatasetController.DataSetControllerZuul;
import org.eea.interfaces.controller.recordstore.ProcessController.ProcessControllerZuul;
import org.eea.interfaces.controller.ums.UserManagementController.UserManagementControllerZull;
import org.eea.interfaces.vo.orchestrator.JobVO;
import org.eea.interfaces.vo.orchestrator.enums.FmeJobStatusEnum;
import org.eea.interfaces.vo.orchestrator.enums.JobStatusEnum;
import org.eea.interfaces.vo.recordstore.ProcessVO;
import org.eea.interfaces.vo.recordstore.enums.ProcessStatusEnum;
import org.eea.interfaces.vo.recordstore.enums.ProcessTypeEnum;
import org.eea.interfaces.vo.ums.TokenVO;
import org.eea.kafka.domain.EventType;
import org.eea.orchestrator.service.JobProcessService;
import org.eea.orchestrator.service.JobService;
import org.eea.orchestrator.utils.JobUtils;
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
import java.util.Arrays;
import java.util.List;

@Component

public class JobForFmeStatusPolling {

    /**
     * The Constant LOG.
     */
    private static final Logger LOG = LoggerFactory.getLogger(JobForFmeStatusPolling.class);

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
    private DataSetControllerZuul dataSetControllerZuul;


    @Autowired
    private JobService jobService;

    @Autowired
    private JobProcessService jobProcessService;

    @Autowired
    private ProcessControllerZuul processControllerZuul;

    @Autowired
    private UserManagementControllerZull userManagementControllerZull;

    @Autowired
    private JobUtils jobUtils;

    @PostConstruct
    private void init() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.initialize();
        scheduler.schedule(() -> pollingForFmeJobs(),
                new CronTrigger("0 */5 * * * *"));
    }

    /**
     * The job runs every 5 minutes. It finds fme import jobs that have tasks with status=IN_PROGRESS and fme_status not success.
     * Then it polls for fme status and updates the value.
     * If fme_status is ABORTED, FME_FAILURE or JOB_FAILURE the job is failed.
     */
    public void pollingForFmeJobs() {
        try {
            List<JobVO> jobsForPolling = jobService.getFMEImportJobsForPolling();
            for (JobVO job: jobsForPolling){
                try {
                    //TODO
                    //poll for status
                    // if response code is 200 get status
                    String fmeStatus= null;
                    FmeJobStatusEnum fmeStatusEnum = FmeJobStatusEnum.valueOf(fmeStatus);
                    if(fmeStatusEnum == null) {
                        String exceptionMessage = "Got unknown status when polling fme for jobId " + job.getId() + " and fmeJobId " + job.getFmeJobId()
                                + " Status was: " + fmeStatus;
                        throw new Exception(exceptionMessage);
                    }

                    if(job.getFmeStatus() == null || (job.getFmeStatus() != null && !job.getFmeStatus().getValue().equals(fmeStatus))){
                        jobService.updateFmeStatus(job.getId(), fmeStatusEnum);
                    }

                    String [] failedStatuses = {FmeJobStatusEnum.ABORTED.getValue(), FmeJobStatusEnum.FME_FAILURE.getValue(), FmeJobStatusEnum.JOB_FAILURE.getValue()};
                    if ( Arrays.stream(failedStatuses).anyMatch(fmeStatusEnum.getValue()::equals)){
                        failJob(job);
                    }
                }
                catch (Exception e){
                    LOG.error("Error when polling for status for job with id {} and fmeJobId {} ", job.getId(), job.getFmeJobId(), e);
                }
            }
        } catch (Exception e) {
            LOG.error("Error while running scheduled task pollingForFmeJobs ", e);
        }
    }

    private void failJob(JobVO job){
        List<String> processIds = jobProcessService.findProcessesByJobId(job.getId());

        for(String processId: processIds){
            ProcessVO processVO = processControllerZuul.findById(processId);
            //update process status
            processControllerZuul.updateProcess(processVO.getDatasetId(), processVO.getDataflowId(),
                    ProcessStatusEnum.CANCELED, ProcessTypeEnum.IMPORT, processVO.getProcessId(),
                    processVO.getUser(), processVO.getPriority(), processVO.isReleased());
            LOG.info("Updated fme import process to status CANCELED for jobId {} and processId {}", job.getId(), processVO.getProcessId());
        }

        jobService.updateJobStatus(job.getId(), JobStatusEnum.FAILED);
        LOG.info("Updated fme import job to FAILED for jobId {}", job.getId());

        //remove locks
        TokenVO tokenVo = userManagementControllerZull.generateToken(adminUser, adminPass);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(adminUser, BEARER + tokenVo.getAccessToken(), null);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        dataSetControllerZuul.deleteLocksToImportProcess(job.getDatasetId());
        LOG.info("Locks removed for failed jobId {}, datasetId {}", job.getId(), job.getDatasetId());

        jobUtils.sendKafkaImportNotification(job, EventType.FME_IMPORT_JOB_FAILED_EVENT, "Fme job failed");
        LOG.info("Sent notification FME_IMPORT_JOB_FAILED_EVENT for jobId {} and fmeJobId {}", job.getId(), job.getFmeJobId());
    }
}
