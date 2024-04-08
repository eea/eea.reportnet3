package org.eea.orchestrator.service;

import org.eea.interfaces.vo.orchestrator.JobHistoryVO;
import org.eea.interfaces.vo.orchestrator.enums.JobInfoEnum;
import org.eea.orchestrator.persistence.domain.Job;

import java.util.List;

public interface JobHistoryService {

    void saveJobHistory(Job job);

    List<JobHistoryVO> getJobHistory(Long jobId);

    void updateJobInfoOfLastHistoryEntry(Long jobId, JobInfoEnum jobInfo, Integer lineNumber);
}
