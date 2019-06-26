package org.eea.dataset.service.callable;

import java.util.List;
import org.eea.dataset.service.DatasetService;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.RecordVO;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * The Class UpdateRecordHelper.
 */
@Component
public class UpdateRecordHelper extends KafkaSenderUtils {

  /**
   * The Constant LOG.
   */
  private static final Logger LOG = LoggerFactory.getLogger(UpdateRecordHelper.class);

  /** The dataset service. */
  @Autowired
  @Qualifier("proxyDatasetService")
  private DatasetService datasetService;

  /**
   * Instantiates a new file loader helper.
   */
  public UpdateRecordHelper() {
    super();
  }


  /**
   * Execute update process.
   *
   * @param datasetId the dataset id
   * @param records the records
   * @throws EEAException the EEA exception
   */
  public void executeUpdateProcess(final Long datasetId, List<RecordVO> records)
      throws EEAException {
    datasetService.updateRecords(datasetId, records);
    LOG.info("Records have been modified");
    // after the records have been saved, an event is sent to notify it
    releaseDatasetKafkaEvent(EventType.RECORD_UPDATED_COMPLETED_EVENT, datasetId);
  }

  /**
   * Execute create process.
   *
   * @param datasetId the dataset id
   * @param records the records
   * @throws EEAException the EEA exception
   */
  public void executeCreateProcess(final Long datasetId, List<RecordVO> records)
      throws EEAException {
    datasetService.updateRecords(datasetId, records);
    LOG.info("Records have been created");
    // after the records have been saved, an event is sent to notify it
    releaseDatasetKafkaEvent(EventType.RECORD_CREATED_COMPLETED_EVENT, datasetId);
  }


  /**
   * Execute delete process.
   *
   * @param datasetId the dataset id
   * @param recordIds the record ids
   * @throws EEAException the EEA exception
   */
  public void executeDeleteProcess(Long datasetId, List<Long> recordIds) throws EEAException {
    datasetService.deleteRecords(datasetId, recordIds);
    LOG.info("Records have been deleted");
    // after the records have been deleted, an event is sent to notify it
    releaseDatasetKafkaEvent(EventType.RECORD_UPDATED_COMPLETED_EVENT, datasetId);

  }

}
