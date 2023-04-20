package org.eea.orchestrator.service;

import org.eea.interfaces.vo.orchestrator.JobProcessVO;

import java.util.List;

public interface JobProcessService {

    JobProcessVO saveJobProcess(JobProcessVO jobProcessVO);

    Long findJobIdByProcessId(String processId);

    List<String> findProcessesByJobId(Long jobId);

    String findStatusByJobId(Long jobId);
}
