package org.eea.dataset.service.file;

import java.io.IOException;
import java.io.InputStream;
import org.eea.dataset.service.DatasetService;
import org.eea.exception.EEAException;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * The Class FileTreatmentHelper.
 */
@Component
public class FileTreatmentHelper {

  /**
   * The Constant LOG.
   */
  private static final Logger LOG = LoggerFactory.getLogger(FileTreatmentHelper.class);

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
  public FileTreatmentHelper() {
    super();
  }

  /**
   * Execute file process.
   *
   * @param datasetId the dataset id
   * @param fileName the file name
   * @param is the input stream
   * @param idTableSchema the id table schema
   * @throws EEAException the EEA exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws InterruptedException the interrupted exception
   */
  public void executeFileProcess(final Long datasetId, final String fileName, final InputStream is,
      String idTableSchema) throws EEAException, IOException, InterruptedException {
    LOG.info("Processing file");
    datasetService.processFile(datasetId, fileName, is, idTableSchema);

    // after the dataset has been saved, an event is sent to notify it
    kafkaSenderUtils.releaseDatasetKafkaEvent(EventType.LOAD_DATA_COMPLETED_EVENT, datasetId);
  }

}
