package org.eea.orchestrator.scheduling;

import org.eea.interfaces.controller.dataset.DatasetController.DataSetControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetSnapshotController.DataSetSnapshotControllerZuul;
import org.eea.interfaces.controller.dataset.EUDatasetController.EUDatasetControllerZuul;
import org.eea.interfaces.controller.ums.UserManagementController.UserManagementControllerZull;
import org.eea.interfaces.controller.validation.ValidationController.ValidationControllerZuul;
import org.eea.interfaces.vo.orchestrator.JobVO;
import org.eea.interfaces.vo.orchestrator.enums.JobStatusEnum;
import org.eea.interfaces.vo.orchestrator.enums.JobTypeEnum;
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
import java.math.BigInteger;
import java.util.List;

@Component
public class JobForCancellingJobsWithoutProcess {

    @Value(value = "${scheduling.inProgress.job.without.process.max.time}")
    private long maxTimeInMinutesForInProgressJobsWithoutProcess;

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
    private static final Logger LOG = LoggerFactory.getLogger(JobForCancellingJobsWithoutProcess.class);

    @Autowired
    private JobProcessService jobProcessService;
    @Autowired
    private JobService jobService;
    @Autowired
    private ValidationControllerZuul validationControllerZuul;
    @Autowired
    private DataSetControllerZuul dataSetControllerZuul;
    @Autowired
    private DataSetSnapshotControllerZuul dataSetSnapshotControllerZuul;
    @Autowired
    private EUDatasetControllerZuul euDatasetControllerZuul;
    @Autowired
    private UserManagementControllerZull userManagementControllerZull;

    @PostConstruct
    private void init() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.initialize();
        scheduler.schedule(() -> cancelInProgressJobsWithoutProcess(),
                new CronTrigger("0 */30 * * * *"));
    }

    /**
     * The job runs every 30 minutes. It finds jobs that have status=IN_PROGRESS for more than maxTimeInMinutesForInProgressJobsWithoutProcess
     * and have no process created and sets their status to FAILED
     */
    public void cancelInProgressJobsWithoutProcess() {
        try {
            LOG.info("Running scheduled job cancelInProgressJobsWithoutProcess");
            List<BigInteger> jobs = jobService.listJobsThatExceedTimeWithSpecificStatus(ProcessStatusEnum.IN_PROGRESS.toString(), maxTimeInMinutesForInProgressJobsWithoutProcess);
            TokenVO tokenVo = userManagementControllerZull.generateToken(adminUser, adminPass);
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(adminUser, BEARER + tokenVo.getAccessToken(), null);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            for (BigInteger id : jobs) {
                try {
                    List<String> processes = jobProcessService.findProcessesByJobId(id.longValue());
                    if (processes.size()==0) {
                        LOG.info("Setting job {} without process to failed", id);
                        jobService.updateJobStatus(id.longValue(), JobStatusEnum.FAILED);
                        JobVO job = jobService.findById(id.longValue());
                        if ((job.getJobType().equals(JobTypeEnum.VALIDATION) && job.isRelease()) || job.getJobType().equals(JobTypeEnum.RELEASE)) {
                            dataSetSnapshotControllerZuul.releaseLocksFromReleaseDatasets(job.getDataflowId(), job.getProviderId());
                        } else if (job.getJobType().equals(JobTypeEnum.VALIDATION) && !job.isRelease()) {
                            validationControllerZuul.deleteLocksToReleaseProcess(job.getDatasetId());
                        } else if (job.getJobType().equals(JobTypeEnum.IMPORT)) {
                            dataSetControllerZuul.deleteLocksToImportProcess(job.getDatasetId());
                        } else if (job.getJobType().equals(JobTypeEnum.COPY_TO_EU_DATASET)) {
                            euDatasetControllerZuul.removeLocksRelatedToPopulateEU(job.getDataflowId());
                        }
                    }
                } catch (Exception er) {
                    LOG.error("Error while running scheduled job cancelInProgressJobsWithoutProcess for job {}, {}", id, er.getMessage());
                }
            }
        } catch (Exception e) {
            LOG.error("Error while running scheduled job cancelInProgressJobsWithoutProcess " + e.getMessage());
        }
    }
}

















