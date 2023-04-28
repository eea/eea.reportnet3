package org.eea.interfaces.controller.orchestrator;

import org.eea.interfaces.vo.orchestrator.JobProcessVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** The Interface JobControllerZuul. */
public interface JobProcessController {

    @FeignClient(value = "orchestrator", contextId = "jobProcess", path = "/jobProcess")
    interface JobProcessControllerZuul extends JobProcessController {
    }

    /**
     * Saves jobProcess
     * @param jobProcessVO
     * @return
     */
    @PostMapping(value = "/private/saveJobProcess")
    JobProcessVO save(@RequestBody JobProcessVO jobProcessVO);

    /**
     * Finds jobId by process id
     * @param processId
     * @return
     */
    @GetMapping(value = "/findJobIdByProcessId/{processId}")
    Long findJobIdByProcessId(@PathVariable("processId") String processId);

    /**
     *
     * @param jobId
     * @return
     */
    @GetMapping(value = "/findProcessesByJobId/{jobId}")
    List<String> findProcessesByJobId(@PathVariable("jobId") Long jobId);


    /**
     * Finds jobId by job id
     * @param jobId
     * @return
     */
    @GetMapping(value = "/findStatusByJobId/{jobId}")
    String findStatusByJobId(@PathVariable("jobId") Long jobId);
}
