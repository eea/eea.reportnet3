package org.eea.interfaces.controller.orchestrator;

import org.eea.interfaces.vo.orchestrator.JobVO;
import org.eea.interfaces.vo.orchestrator.enums.JobStatusEnum;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

/** The Interface JobController. */
public interface JobController {

    @FeignClient(value = "orchestrator", path = "/jobs")
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
     * Get jobs based on status
     *
     * @param status the job status
     * @return a list of job entries
     */
    @GetMapping(value = "/{status}", produces = MediaType.APPLICATION_JSON_VALUE)
    List<JobVO> getJobsByStatus(@PathVariable("status") JobStatusEnum status);

    /**
     * Adds a validation job
     *
     * @param datasetId the id of the dataset that will be validated
     * @param released the released
     * @param creator the creator's username
     * @return
     */
    @PostMapping(value = "/addValidation/{datasetId}/{released}/{creator}")
    void addValidationJob(@PathVariable("datasetId") Long datasetId, @PathVariable("released") Boolean released, @PathVariable("creator") String creator);

    /**
     * Update status by process id
     *
     * @param processId the process id
     * @return
     */
    @PostMapping(value = "/updateStatus/{status}/{processId}")
    void updateStatusByProcessId(@PathVariable("status") String status, @PathVariable("processId") String processId);

    /**
     * Update job to in progress
     *
     * @param jobId the job id
     * @param processId the process id
     * @return
     */
    @PostMapping(value = "/updateJobInProgress/{id}/{processId}")
    void updateJobInProgress(@PathVariable("id") Long jobId, @PathVariable("processId") String processId);

    /**
     * Adds a job.
     **
     */
    @PostMapping("/add")
    void addJob();

}
