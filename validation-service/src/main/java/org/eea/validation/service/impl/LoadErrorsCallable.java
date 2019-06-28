package org.eea.validation.service.impl;

import java.util.ArrayList;
import java.util.List;
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

  /** The id validations. */
  private List<Long> idValidations;

  /** The thread. */
  private int thread;


  /**
   * Instantiates a new Load data callable.
   *
   * @param validationService the validation service
   * @param dataset the dataset
   * @param idValidations the id validations
   * @param thread the thread
   */
  public LoadErrorsCallable(final ValidationService validationService, DatasetValue dataset,
      List<Long> idValidations, int thread) {
    this.validationService = validationService;
    this.thread = thread;
    this.datasetId = dataset.getId();
    this.dataset = dataset;
    this.idValidations = idValidations;
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
        result = validationService.getDatasetErrors(datasetId, dataset, idValidations);
        break;
      case 1:
        result = validationService.getTableErrors(datasetId, idValidations);
        break;
      case 2:
        result = validationService.getRecordErrors(datasetId, idValidations);
        break;
      case 3:
        result = validationService.getFieldErrors(datasetId, idValidations);
        break;
      default:
        break;
    }
    return result;
  }
}
