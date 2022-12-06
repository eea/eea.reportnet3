package org.eea.interfaces.controller.orchestrator;

import org.eea.interfaces.vo.orchestrator.JobHistoryVO;
import org.eea.interfaces.vo.orchestrator.JobVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

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

}
