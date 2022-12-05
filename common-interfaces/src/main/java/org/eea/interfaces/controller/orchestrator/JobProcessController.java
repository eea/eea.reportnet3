package org.eea.interfaces.controller.orchestrator;

import org.eea.interfaces.vo.orchestrator.JobProcessVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

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
    @PostMapping(value = "/saveJobProcess")
    JobProcessVO save(@RequestBody JobProcessVO jobProcessVO);

    /**
     * Finds jobId by process id
     * @param processId
     * @return
     */
    @GetMapping(value = "/findJobIdByProcessId/{processId}")
    Long findJobIdByProcessId(@PathVariable("processId") String processId);
}
