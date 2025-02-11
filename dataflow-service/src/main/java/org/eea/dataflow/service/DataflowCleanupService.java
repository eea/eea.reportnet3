package org.eea.dataflow.service;

import org.eea.exception.EEAException;


public interface DataflowCleanupService {

    void deleteDataflowsOlderThanNumberOfMonths() throws EEAException;

}
