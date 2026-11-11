package org.eea.orchestrator.controller;

import io.swagger.annotations.Api;
import org.eea.interfaces.controller.orchestrator.JobProcessController;
import org.eea.interfaces.vo.orchestrator.JobProcessVO;
import org.eea.orchestrator.service.JobProcessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/jobProcess")
@Api(tags = "Orchestrator: Job process handling")
public class JobProcessControllerImpl implements JobProcessController {

    private JobProcessService jobProcessService;

    @Autowired
    public JobProcessControllerImpl(JobProcessService jobProcessService) {
        this.jobProcessService = jobProcessService;
    }

    /**
     * Saves jobProcess
     * @param jobProcessVO
     * @return
     */
    @Override
    @PostMapping(value = "/private/saveJobProcess")
    public JobProcessVO save(@RequestBody JobProcessVO jobProcessVO) {
        return jobProcessService.saveJobProcess(jobProcessVO);
    }

    /**
     * Finds jobId by process id
     * @param processId
     * @return
     */
    @Override
    @GetMapping(value = "/findJobIdByProcessId/{processId}")
    @PreAuthorize("isAuthenticated()")
    public Long findJobIdByProcessId(@PathVariable("processId") String processId) {
        return jobProcessService.findJobIdByProcessId(processId);
    }

    /**
     *
     * @param jobId
     * @return
     */
    @GetMapping(value = "/findProcessesByJobId/{jobId}")
    @PreAuthorize("isAuthenticated()")
    public List<String> findProcessesByJobId(@PathVariable("jobId") Long jobId) {
        return jobProcessService.findProcessesByJobId(jobId);
    }

    @Override
    @GetMapping(value = "/findStatusByJobId/{jobId}")
    @PreAuthorize("isAuthenticated()")
    public String findStatusByJobId(@PathVariable("jobId") Long jobId) {
        return jobProcessService.findStatusByJobId(jobId);
    }
}
