package org.eea.interfaces.controller.orchestrator;

import org.eea.interfaces.vo.orchestrator.JobVO;
import org.eea.interfaces.vo.orchestrator.enums.JobStatusEnum;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** The Interface JobController. */
public interface JobController {

    @FeignClient(value = "orchestrator", path = "/jobs")
    interface JobControllerZuul extends JobController {
    }

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
     * @return
     */
    @PutMapping(value = "/addValidationJob/{datasetId}")
    void addValidationJob(@PathVariable("datasetId") Long datasetId, @RequestParam(value = "released", required = false) boolean released);

    /**
     * Adds a release job
     *
     * @param dataflowId the id of the dataflow
     * @param dataProviderId the dataProviderId
     * @param restrictFromPublic the restrictFromPublic
     * @param validate the validate
     * @param creator the creator's username
     * @return
     */
    @PostMapping("/addRelease/{dataflowId}/{dataProviderId}/{restrictFromPublic}/{validate}/{creator}")
    void addReleaseJob(@PathVariable("dataflowId") Long dataflowId, @PathVariable("dataProviderId") Long dataProviderId, @PathVariable("restrictFromPublic") Boolean restrictFromPublic,
                              @PathVariable("validate") Boolean validate, @PathVariable("creator") String creator);

    /**
     * Update status by process id
     *
     * @param processId the process id
     * @return
     */
    @PostMapping(value = "/updateStatus/{status}/{processId}")
    void updateStatusByProcessId(@PathVariable("status") JobStatusEnum status, @PathVariable("processId") String processId);

    /**
     * Update job's status
     *
     * @param jobId the job id
     * @param status the job's status
     * @param processId the process id
     * @return
     */
    @PostMapping(value = "/updateJobStatus/{id}/{status}/{processId}")
    void updateJobStatus(@PathVariable("id") Long jobId, @PathVariable("status") JobStatusEnum status, @PathVariable("processId") String processId);
}
