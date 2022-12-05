package org.eea.orchestrator.service;

import org.eea.interfaces.vo.orchestrator.JobProcessVO;

public interface JobProcessService {

    JobProcessVO saveJobProcess(JobProcessVO jobProcessVO);

    Long findJobIdByProcessId(String processId);
}
