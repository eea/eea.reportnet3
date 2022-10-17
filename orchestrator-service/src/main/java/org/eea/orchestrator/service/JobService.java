package org.eea.orchestrator.service;

import org.eea.interfaces.vo.orchestrator.JobVO;

public interface JobService {

    void testSaveJob();

    JobVO testRetrieveJob(Long id);

}
