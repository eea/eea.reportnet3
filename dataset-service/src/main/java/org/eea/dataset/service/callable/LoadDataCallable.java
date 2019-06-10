package org.eea.dataset.service.callable;

import java.io.InputStream;
import java.util.concurrent.Callable;
import org.eea.dataset.service.DatasetService;
import org.eea.dataset.service.file.FileTreatmentHelper;
import org.eea.kafka.io.KafkaSender;

/**
 * The type Load data callable.
 */
public class LoadDataCallable implements Callable<Void> {

  /** The file name. */
  private final String fileName;

  /** The dataset id. */
  private final Long datasetId;

  /** The is. */
  private final InputStream is;

  /** The dataset service. */
  private DatasetService datasetService;

  /** The kafka sender. */
  private KafkaSender kafkaSender;

  /**
   * Instantiates a new Load data callable.
   *
   * @param kafkaSender the kafka sender
   * @param datasetService the dataset service
   * @param dataSetId the data set id
   * @param fileName the file
   * @param is the is
   */
  public LoadDataCallable(final KafkaSender kafkaSender, final DatasetService datasetService,
      final Long dataSetId, final String fileName, InputStream is) {

    this.fileName = fileName;
    this.datasetId = dataSetId;
    this.is = is;
    this.datasetService = datasetService;
    this.kafkaSender = kafkaSender;
  }

  /**
   * Call.
   *
   * @return the void
   * @throws Exception the exception
   */
  @Override
  public Void call() throws Exception {
    FileTreatmentHelper.executeFileProcess(kafkaSender, this.datasetService, datasetId, fileName, is);
    return null;
  }
}
