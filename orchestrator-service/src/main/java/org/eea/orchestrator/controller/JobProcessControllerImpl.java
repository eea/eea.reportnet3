package org.eea.orchestrator.controller;

import io.swagger.annotations.Api;
import org.eea.interfaces.controller.orchestrator.JobProcessController;
import org.eea.interfaces.vo.orchestrator.JobProcessVO;
import org.eea.orchestrator.service.JobProcessService;
import org.springframework.beans.factory.annotation.Autowired;
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
    @PostMapping(value = "/saveJobProcess")
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
    public Long findJobIdByProcessId(@PathVariable("processId") String processId) {
        return jobProcessService.findJobIdByProcessId(processId);
    }

    /**
     *
     * @param jobId
     * @return
     */
    @GetMapping(value = "/findProcessesByJobId/{jobId}")
    public List<String> findProcessesByJobId(@PathVariable("jobId") Long jobId) {
        return jobProcessService.findProcessesByJobId(jobId);
    }
}
