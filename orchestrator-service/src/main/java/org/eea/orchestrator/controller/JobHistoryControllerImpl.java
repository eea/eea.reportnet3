package org.eea.orchestrator.controller;

import io.swagger.annotations.Api;
import org.eea.exception.EEAErrorMessage;
import org.eea.interfaces.controller.orchestrator.JobHistoryController;
import org.eea.interfaces.vo.orchestrator.JobsHistoryVO;
import org.eea.interfaces.vo.orchestrator.JobHistoryVO;
import org.eea.interfaces.vo.orchestrator.JobVO;
import org.eea.orchestrator.mapper.JobMapper;
import org.eea.orchestrator.service.JobHistoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import springfox.documentation.annotations.ApiIgnore;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/jobHistory")
@Api(tags = "Orchestrator: Job history handling")
@ApiIgnore
public class JobHistoryControllerImpl implements JobHistoryController {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(JobHistoryControllerImpl.class);

    /** The job service. */
    private JobHistoryService jobHistoryService;
    private JobMapper jobMapper;

    /** The valid columns. */
    List<String> validColumns = Arrays.asList("jobId", "creatorUsername", "jobType", "dataflowId", "providerId", "datasetId",
            "jobStatus", "dateAdded", "dateStatusChanged", "fmeJobId", "dataflowName", "datasetName");

    @Autowired
    public JobHistoryControllerImpl(JobHistoryService jobHistoryService, JobMapper jobMapper) {
        this.jobHistoryService = jobHistoryService;
        this.jobMapper = jobMapper;
    }

    @Override
    @GetMapping
    public JobsHistoryVO getJobHistory(
            @RequestParam(value = "pageNum", defaultValue = "0", required = false) Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "20", required = false) Integer pageSize,
            @RequestParam(value = "asc", defaultValue = "true") boolean asc,
            @RequestParam(value = "sortedColumn", defaultValue = "jobId") String sortedColumn,
            @RequestParam(value = "jobId", required = false) Long jobId,
            @RequestParam(value = "jobType", required = false) String jobType,
            @RequestParam(value = "dataflowId", required = false) Long dataflowId,
            @RequestParam(value = "dataflowName", required = false) String dataflowName,
            @RequestParam(value = "providerId", required = false) Long providerId,
            @RequestParam(value = "datasetId", required = false) Long datasetId,
            @RequestParam(value = "datasetName", required = false) String datasetName,
            @RequestParam(value = "creatorUsername", required = false) String creatorUsername,
            @RequestParam(value = "jobStatus", required = false) String jobStatus
    ){
        List<Object> filterParams = Arrays.asList(jobId,jobType,dataflowId,dataflowName,providerId,datasetId,datasetName,creatorUsername,jobStatus);
        boolean atLeastOneFilterIsActive = filterParams.stream().anyMatch(Objects::nonNull);

        if (!atLeastOneFilterIsActive) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.AT_LEAST_ONE_FILTER_SHOULD_BE_ACTIVE);
        }

        try{
            Pageable pageable = PageRequest.of(pageNum, pageSize);
            if (!validColumns.contains(sortedColumn)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Wrong sorting header provided.");
            }

            LOG.info("Retrieving job history for jobId {}, jobType {}, dataflowId {}, dataflowName {}, providerId {}, datasetId {}, datasetName {}, creatorUsername {}, jobStatus {} ",
                    jobId, jobType, dataflowId, dataflowName, providerId, datasetId, datasetName, creatorUsername, jobStatus);
            return jobHistoryService.getJobHistory(pageable, asc, sortedColumn, jobId, jobType, dataflowId, dataflowName, providerId, datasetId, datasetName, creatorUsername, jobStatus);
        } catch (Exception e){
            LOG.error("Unexpected error! Could not retrieve job history for job {}. ", jobId, e);
            throw e;
        }
    }

    @Override
    @GetMapping("/{jobId}")
    public List<JobHistoryVO> getJobHistory(@PathVariable("jobId") Long jobId){
        try{
            LOG.info("Retrieving job history for job {}", jobId);
            return jobHistoryService.getJobHistory(jobId);
        } catch (Exception e){
            LOG.error("Unexpected error! Could not retrieve job history for job {}. ", jobId, e);
            throw e;
        }
    }

    /**
     * Saves job history for job
     * @param jobVO
     * @return
     */
    @Override
    @PostMapping(value = "/save")
    public void save(@RequestBody JobVO jobVO) {
        jobHistoryService.saveJobHistory(jobMapper.classToEntity(jobVO));
    }

}
