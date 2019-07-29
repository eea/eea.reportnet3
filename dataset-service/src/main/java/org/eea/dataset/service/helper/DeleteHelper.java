package org.eea.dataset.service.helper;

import java.io.IOException;
import org.eea.dataset.service.DatasetService;
import org.eea.exception.EEAException;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * The Class FileTreatmentHelper.
 */
@Component
public class DeleteHelper {

  /**
   * The Constant LOG.
   */
  private static final Logger LOG = LoggerFactory.getLogger(DeleteHelper.class);

  /** The kafka sender helper. */
  @Autowired
  private KafkaSenderUtils kafkaSenderUtils;


  /** The dataset service. */
  @Autowired
  @Qualifier("proxyDatasetService")
  private DatasetService datasetService;

  /**
   * Instantiates a new file loader helper.
   */
  public DeleteHelper() {
    super();
  }


  /**
   * Execute delete process.
   *
   * @param datasetId the dataset id
   * @param idTableSchema the id table schema
   * @throws EEAException the EEA exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws InterruptedException the interrupted exception
   */
  @Async
  public void executeDeleteProcess(final Long datasetId, String idTableSchema) throws EEAException {
    LOG.info("Deleting table {} from dataset {}", idTableSchema, datasetId);
    datasetService.deleteTableBySchema(idTableSchema, datasetId);

    // after the table has been deleted, an event is sent to notify it
    kafkaSenderUtils.releaseDatasetKafkaEvent(EventType.DELETED_TABLE, datasetId);
  }

}
