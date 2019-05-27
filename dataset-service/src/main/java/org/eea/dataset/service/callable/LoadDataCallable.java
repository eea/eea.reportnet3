package org.eea.dataset.service.callable;

import java.io.InputStream;
import java.util.concurrent.Callable;
import org.eea.dataset.service.DatasetService;

/**
 * The type Load data callable.
 */
public class LoadDataCallable implements Callable<Void> {

  private final DatasetService datasetService;
  private final String fileName;
  private final Long datasetId;
  private final InputStream is;

  /**
   * Instantiates a new Load data callable.
   *
   * @param datasetService the dataset service
   * @param dataSetId the data set id
   * @param fileName the file
   */
  public LoadDataCallable(final DatasetService datasetService, final Long dataSetId,
      final String fileName, InputStream is) {
    this.datasetService = datasetService;
    this.fileName = fileName;
    this.datasetId = dataSetId;
    this.is = is;
  }

  @Override
  public Void call() throws Exception {
    datasetService.processFile(datasetId, fileName, is);
    return null;
  }
}
