package org.eea.orchestrator.service;

import org.eea.orchestrator.persistence.domain.Job;

public interface JobHistoryService {

    void saveJobHistory(Job job);
}
