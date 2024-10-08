package org.eea.interfaces.controller.orchestrator;

import org.eea.interfaces.vo.orchestrator.JobsHistoryVO;
import org.eea.interfaces.vo.orchestrator.JobHistoryVO;
import org.eea.interfaces.vo.orchestrator.JobVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/** The Interface JobHistoryController. */
public interface JobHistoryController {

    @FeignClient(value = "orchestrator", contextId = "jobHistory", path = "/jobHistory")
    interface JobHistoryControllerZuul extends JobHistoryController {
    }

    /**
     * Get job history
     *
     * @param jobId the job id
     * @return a list of job history entries
     */
    @GetMapping(value = "/{jobId}", produces = MediaType.APPLICATION_JSON_VALUE)
    List<JobHistoryVO> getJobHistory(@PathVariable("jobId") Long jobId);

    /**
     * Saves job history for job
     * @param jobVO
     * @return
     */
    @PostMapping(value = "/save")
    void save(@RequestBody JobVO jobVO);

    @GetMapping
    JobsHistoryVO getJobHistory(
            @RequestParam(value = "pageNum", defaultValue = "0", required = false) Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "20", required = false) Integer pageSize,
            @RequestParam(value = "asc", defaultValue = "true") boolean asc,
            @RequestParam(value = "sortedColumn", defaultValue = "jobId") String sortedColumn,
            @RequestParam(value = "jobId", required = false) Long jobId,
            @RequestParam(value = "jobType", required = false) String jobTypes,
            @RequestParam(value = "dataflowId", required = false) Long dataflowId,
            @RequestParam(value = "dataflowName", required = false) String dataflowName,
            @RequestParam(value = "providerId", required = false) Long providerId,
            @RequestParam(value = "datasetId", required = false) Long datasetId,
            @RequestParam(value = "datasetName", required = false) String datasetName,
            @RequestParam(value = "creatorUsername", required = false) String creatorUsername,
            @RequestParam(value = "jobStatus", required = false) String jobStatuses
    );

}
