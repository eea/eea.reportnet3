package org.eea.validation.service.impl;

import java.util.concurrent.Callable;
import org.eea.validation.util.ValidationHelper;

/**
 * The type Load errors callable.
 */
public class LoadValidationCallable implements Callable<Void> {

  /** The dataset id. */
  private Long datasetId;


  /** The validation helper. */
  private ValidationHelper validationHelper;


  /**
   * Instantiates a new load validation callable.
   *
   * @param validationHelper the validation helper
   * @param datasetId the dataset id
   */
  public LoadValidationCallable(final ValidationHelper validationHelper, final Long datasetId) {
    this.datasetId = datasetId;
    this.validationHelper = validationHelper;
  }

  /**
   * Call.
   *
   * @return the void
   * @throws Exception the exception
   */
  @Override
  public Void call() throws Exception {
    validationHelper.executeValidation(datasetId);
    return null;
  }
}

