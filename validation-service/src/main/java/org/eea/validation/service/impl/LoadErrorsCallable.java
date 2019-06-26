package org.eea.validation.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import org.eea.interfaces.vo.dataset.ErrorsValidationVO;
import org.eea.validation.persistence.data.domain.DatasetValue;
import org.eea.validation.service.ValidationService;

/**
 * The type Load errors callable.
 */
public class LoadErrorsCallable implements Callable<List<ErrorsValidationVO>> {

  /** The dataset service. */
  private ValidationService validationService;

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
   * @param validationService the validation service
   * @param dataset the dataset
   * @param mapNameTableSchema the map name table schema
   * @param thread the thread
   */
  public LoadErrorsCallable(final ValidationService validationService, final DatasetValue dataset,
      final Map<String, String> mapNameTableSchema, int thread) {
    this.validationService = validationService;
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
        result = validationService.getDatasetErrors(dataset, mapNameTableSchema);
        break;
      case 1:
        result = validationService.getTableErrors(datasetId, mapNameTableSchema);
        break;
      case 2:
        result = validationService.getRecordErrors(datasetId, mapNameTableSchema);
        break;
      case 3:
        result = validationService.getFieldErrors(datasetId, mapNameTableSchema);
        break;
      default:
        break;
    }
    return result;
  }
}
