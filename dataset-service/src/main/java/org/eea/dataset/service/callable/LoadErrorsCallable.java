package org.eea.dataset.service.callable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import org.eea.dataset.persistence.data.domain.DatasetValue;
import org.eea.dataset.service.DatasetService;
import org.eea.interfaces.vo.dataset.ErrorsValidationVO;

/**
 * The type Load errors callable.
 */
public class LoadErrorsCallable implements Callable<List<ErrorsValidationVO>> {

  /** The dataset service. */
  private DatasetService datasetService;

  /** The dataset id. */
  private Long datasetId;

  /** The dataset. */
  private DatasetValue dataset;

  /** The map name table schema. */
  private Map<String, String> mapNameTableSchema;

  /** The thread. */
  private int thread;


  /**
   * Instantiates a new Load data callable.
   *
   * @param datasetService the dataset service
   * @param dataset the dataset
   * @param mapNameTableSchema the map name table schema
   * @param thread the thread
   */
  public LoadErrorsCallable(final DatasetService datasetService, final DatasetValue dataset,
      final Map<String, String> mapNameTableSchema, int thread) {
    this.datasetService = datasetService;
    this.dataset = dataset;
    this.datasetId = dataset.getId();
    this.mapNameTableSchema = mapNameTableSchema;
    this.thread = thread;
  }

  /**
   * Call.
   *
   * @return the list
   * @throws Exception the exception
   */
  @Override
  public List<ErrorsValidationVO> call() throws Exception {
    List<ErrorsValidationVO> result = new ArrayList<>();
    switch (thread) {
      case 0:
        result = datasetService.getDatasetErrors(dataset, mapNameTableSchema);
        break;
      case 1:
        result = datasetService.getTableErrors(datasetId, mapNameTableSchema);
        break;
      case 2:
        result = datasetService.getRecordErrors(datasetId, mapNameTableSchema);
        break;
      case 3:
        result = datasetService.getFieldErrors(datasetId, mapNameTableSchema);
        break;
      default:
        break;
    }
    return result;
  }
}
