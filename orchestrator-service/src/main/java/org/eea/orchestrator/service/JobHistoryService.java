package org.eea.orchestrator.service;

import org.eea.interfaces.vo.orchestrator.JobHistoryVO;

import java.util.List;

public interface JobHistoryService {
    List<JobHistoryVO> testRetrieveJobHistory(Long jobId);
    void testSaveJobHistory();
}
