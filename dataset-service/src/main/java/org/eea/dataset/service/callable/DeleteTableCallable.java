package org.eea.dataset.service.callable;

import java.util.concurrent.Callable;
import org.eea.dataset.service.DatasetService;

public class DeleteTableCallable implements Callable<Void> {

  /** The dataset service. */
  private final DatasetService datasetService;

  /** The id table schema. */
  private final String idTableSchema;

  /** The dataset id. */
  private final Long datasetId;

  /**
   * Instantiates a new delete table callable.
   *
   * @param datasetService the dataset service
   * @param idTableSchema the id table schema
   * @param datasetId the dataset id
   */
  public DeleteTableCallable(final DatasetService datasetService, final String idTableSchema,
      final Long datasetId) {
    this.datasetService = datasetService;
    this.idTableSchema = idTableSchema;
    this.datasetId = datasetId;
  }

  /**
   * Call.
   *
   * @return the void
   * @throws Exception the exception
   */
  @Override
  public Void call() throws Exception {
    datasetService.deleteTableBySchema(idTableSchema, datasetId);
    return null;
  }
}
