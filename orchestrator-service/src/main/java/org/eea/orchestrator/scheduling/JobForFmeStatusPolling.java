package org.eea.orchestrator.scheduling;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.eea.exception.FmeIntegrationException;
import org.springframework.http.*;
import org.json.JSONObject;
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
import java.io.IOException;
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

    private static final String JSON_STATUS_PARAM="status";
    private static final String FME_TOKEN_HEADER="fmetoken token=";
    private static final String MEDIA_TYPE_JSON="application/json";

    @Value("${integration.fme.polling.token}")
    private String fmeTokenProperty;

    @PostConstruct
    private void init() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.initialize();
        scheduler.schedule(() -> pollingForFmeJobs(),
                new CronTrigger("0 */10 * * * *"));
    }

    /**
     * The job runs every 10 minutes. It finds fme import jobs that have tasks with status=IN_PROGRESS and fme_status not success.
     * Then it polls for fme status and updates the value.
     * If fme_status is ABORTED, FME_FAILURE or JOB_FAILURE the job is failed.
     */
    public void pollingForFmeJobs() {
        try {
            List<JobVO> jobsForPolling = jobService.getFMEImportJobsForPolling();
            if(jobsForPolling == null || jobsForPolling.size() == 0){
                return;
            }
            LOG.info("Running scheduled job pollingForFmeJobs");
            for (JobVO job: jobsForPolling){
                try {
                    String fmeStatus = pollFmeForJobStatus(job.getId().toString(), job.getFmeJobId());
                    FmeJobStatusEnum fmeStatusEnum = FmeJobStatusEnum.valueOf(fmeStatus);
                    if(fmeStatusEnum == null) {
                        String exceptionMessage = "Got unknown status when polling fme for jobId " + job.getId() + " and fmeJobId " + job.getFmeJobId()
                                + " Status was: " + fmeStatus;
                        throw new Exception(exceptionMessage);
                    }

                    //if job's fme status is empty or job's fme status has been modified, we need to update it
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
        //get job to check if it is still in IN_PROGRESS status
        JobVO jobToCheckStatus = jobService.findById(job.getId());
        if(jobToCheckStatus.getJobStatus() != JobStatusEnum.IN_PROGRESS) {
            LOG.info("Job with id {} has already been modified and now has status {}", job.getId(), job.getJobStatus().getValue());
        }
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

    private String pollFmeForJobStatus(String jobId, String fmeJobId) throws FmeIntegrationException, IOException {
        String fmePollingUrl = "https://fme.discomap.eea.europa.eu/fmerest/v3/transformations/jobs/id/" + fmeJobId;

        HttpGet request = new HttpGet(fmePollingUrl);
        request.addHeader(HttpHeaders.ACCEPT, MEDIA_TYPE_JSON);
        request.addHeader(HttpHeaders.AUTHORIZATION, fmeTokenProperty);
        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse response = httpClient.execute(request);

        if (response.getStatusLine().getStatusCode() == HttpStatus.OK.value()) {

            if (response.getEntity() != null) {
                String strResponse = EntityUtils.toString(response.getEntity());
                JSONObject jsonResponse = new JSONObject(strResponse);

                if (jsonResponse.get(JSON_STATUS_PARAM) != null) {
                    FmeJobStatusEnum fmeStatus =  FmeJobStatusEnum.valueOf(jsonResponse.get(JSON_STATUS_PARAM).toString());
                    LOG.info("When polling for fme status for jobId {} and fmeJobId {} received fme status {}", jobId, fmeJobId, fmeStatus.getValue());
                    return fmeStatus.toString();
                } else {
                    String exceptionMessage = "When polling for fme status, received wrong response status " + jsonResponse.get(JSON_STATUS_PARAM) + " for jobId " + jobId + " and fmeJobId " + fmeJobId;
                    throw new FmeIntegrationException(exceptionMessage);
                }
            }
            else{
                String exceptionMessage = "When polling for fme status, received response with null entity for jobId " + jobId + " and fmeJobId " + fmeJobId;
                throw new FmeIntegrationException(exceptionMessage);
            }
        } else {
            String exceptionMessage = "When polling for fme status for jobId " + jobId + " and fmeJobId " + fmeJobId + " got status code " + response.getStatusLine().getStatusCode() + " and response: " + response;
            throw new FmeIntegrationException(exceptionMessage);
        }

    }
}
