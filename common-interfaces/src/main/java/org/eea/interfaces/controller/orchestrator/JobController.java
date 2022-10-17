package org.eea.interfaces.controller.orchestrator;

import org.eea.interfaces.vo.orchestrator.JobVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

/** The Interface JobController. */
public interface JobController {

    @FeignClient(value = "jobs", path = "/jobs")
    interface JobControllerZuul extends JobController {
    }

    /**
     * Get a job entry.
     *
     * @param id the job id
     * @return a job entry
     */
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    JobVO getJob(@PathVariable("id") Long id);

    /**
     * Adds a job.
     **
     */
    @PostMapping("/add")
    void addJob();

}
