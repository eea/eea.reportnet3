package org.eea.interfaces.controller.orchestrator;

import org.eea.interfaces.vo.orchestrator.JobHistoryVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/** The Interface JobHistoryController. */
public interface JobHistoryController {

    @FeignClient(value = "jobHistory", path = "/jobHistory")
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

}
