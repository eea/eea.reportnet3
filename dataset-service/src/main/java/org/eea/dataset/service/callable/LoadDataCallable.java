package org.eea.dataset.service.callable;

import java.io.InputStream;
import java.util.concurrent.Callable;
import org.eea.dataset.service.DatasetService;

/**
 * The type Load data callable.
 */
public class LoadDataCallable implements Callable<Void> {

  /** The dataset service. */
  private final DatasetService datasetService;

  /** The file name. */
  private final String fileName;

  /** The dataset id. */
  private final Long datasetId;

  /** The is. */
  private final InputStream is;

  /** The id table schema. */
  private final String idTableSchema;

  /**
   * Instantiates a new Load data callable.
   *
   * @param datasetService the dataset service
   * @param dataSetId the data set id
   * @param fileName the file
   * @param is the is
   */
  public LoadDataCallable(final DatasetService datasetService, final Long dataSetId,
      final String fileName, InputStream is, final String idTableSchema) {
    this.datasetService = datasetService;
    this.fileName = fileName;
    this.datasetId = dataSetId;
    this.is = is;
    this.idTableSchema = idTableSchema;
  }

  /**
   * Call.
   *
   * @return the void
   * @throws Exception the exception
   */
  @Override
  public Void call() throws Exception {
    datasetService.processFile(datasetId, fileName, is, idTableSchema);
    return null;
  }
}
