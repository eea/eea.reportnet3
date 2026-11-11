package org.eea.orchestrator.scheduling;

import org.eea.interfaces.controller.dataset.DatasetController.DataSetControllerZuul;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class JobForRemovingIcebergTablesWithExpiredEditingLocks {

    private static final Logger LOG = LoggerFactory.getLogger(JobForRemovingIcebergTablesWithExpiredEditingLocks.class);

    @Autowired
    private DataSetControllerZuul dataSetControllerZuul;

    @PostConstruct
    private void init() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.initialize();
        scheduler.schedule(this::removeExpiredIcebergTables,
                new CronTrigger("0 */10 * * * *"));
    }

    /**
     * The job runs every 10 minutes. It finds entries in the DatasetTable where the edit_lock_expires_at column contains
     * an expired date, and the proceeds to remove the username and the edit_lock_expires_at values from the row and also
     * close any open Iceberg tables for that dataset.
     */
    public void removeExpiredIcebergTables() {

        LOG.info("Starting JobForRemovingIcebergTablesWithExpiredEditingLocks");
        try {
            dataSetControllerZuul.clearExpiredDatasetTableLocks();
        }
        catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }
}
