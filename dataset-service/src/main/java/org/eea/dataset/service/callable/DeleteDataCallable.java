package org.eea.dataset.service.callable;

import java.util.concurrent.Callable;
import org.eea.dataset.service.DatasetService;

/**
 * The type Load data callable.
 */
public class DeleteDataCallable implements Callable<Void> {

  /** The dataset service. */
  private final DatasetService datasetService;

  /** The dataset id. */
  private final Long datasetId;

  /**
   * Instantiates a new delete data callable.
   *
   * @param datasetService the dataset service
   * @param dataSetId the data set id
   * @param file the file
   */
  public DeleteDataCallable(final DatasetService datasetService, final Long dataSetId) {
    this.datasetService = datasetService;
    this.datasetId = dataSetId;
  }

  /**
   * Call.
   *
   * @return the void
   * @throws Exception the exception
   */
  @Override
  public Void call() throws Exception {
    datasetService.deleteImportData(datasetId);
    return null;
  }
}
