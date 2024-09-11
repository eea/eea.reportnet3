package org.eea.orchestrator.service;

import org.eea.interfaces.vo.orchestrator.JobsHistoryVO;
import org.eea.interfaces.vo.orchestrator.JobHistoryVO;
import org.eea.interfaces.vo.orchestrator.enums.JobInfoEnum;
import org.eea.orchestrator.persistence.domain.Job;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface JobHistoryService {

    void saveJobHistory(Job job);

    List<JobHistoryVO> getJobHistory(Long jobId);

    JobsHistoryVO getJobHistory(Pageable pageable, boolean asc, String sortedColumn,
                                Long jobId, String jobTypes, Long dataflowId, String dataflowName, Long providerId,
                                Long datasetId, String datasetName, String creatorUsername, String jobStatuses);

    void updateJobInfoOfLastHistoryEntry(Long jobId, JobInfoEnum jobInfo, Integer lineNumber);
}
