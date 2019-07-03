package org.eea.validation.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import org.bson.types.ObjectId;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.ErrorsValidationVO;
import org.eea.interfaces.vo.dataset.FailedValidationsDatasetVO;
import org.eea.validation.persistence.data.domain.DatasetValue;
import org.eea.validation.persistence.data.domain.Validation;
import org.eea.validation.persistence.data.repository.ValidationRepository;
import org.eea.validation.persistence.schemas.DataSetSchema;
import org.eea.validation.service.ValidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

/**
 * The Class FileTreatmentHelper.
 */
@Component
public class LoadValidationsHelper {

  /**
   * The Constant LOG_ERROR.
   */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");
  /**
   * The Constant LOG.
   */
  private static final Logger LOG = LoggerFactory.getLogger(LoadValidationsHelper.class);

  /** The validation service. */
  @Autowired
  @Qualifier("proxyValidationService")
  private ValidationService validationService;

  /** The validation repository. */
  @Autowired
  private ValidationRepository validationRepository;

  /**
   * Instantiates a new file loader helper.
   */
  public LoadValidationsHelper() {
    super();
  }

  /**
   * Gets the list validations.
   *
   * @param datasetId the dataset id
   * @param pageable the pageable
   * @param headerField the header field
   * @param asc the asc
   *
   * @return the list validations
   *
   * @throws EEAException the EEA exception
   */
  public FailedValidationsDatasetVO getListValidations(Long datasetId, Pageable pageable,
      String headerField, Boolean asc) throws EEAException {

    DatasetValue dataset = validationService.getDatasetValuebyId(datasetId);
    FailedValidationsDatasetVO validation = new FailedValidationsDatasetVO();
    validation.setErrors(new ArrayList<>());
    validation.setIdDatasetSchema(dataset.getIdDatasetSchema());
    validation.setIdDataset(datasetId);
    DataSetSchema schema = validationService.getfindByIdDataSetSchema(datasetId,
        new ObjectId(dataset.getIdDatasetSchema()));
    validation.setNameDataSetSchema(schema.getNameDataSetSchema());
    Page<Validation> validationStream = validationRepository.findAll(pageable);
    List<Validation> validations = validationStream.get().collect(Collectors.toList());
    List<Long> idValidations =
        validations.stream().map(Validation::getId).collect(Collectors.toList());

    // PROCESS LIST OF ERRORS VALIDATIONS
    Map<Long, ErrorsValidationVO> errors = processErrors(idValidations, dataset);

    validation
        .setErrors(idValidations.stream().map(id -> errors.get(id)).collect(Collectors.toList()));

    validation.setTotalErrors(validationRepository.count());
    LOG.info(
        "Total validations founded in datasetId {}: {}. Now in page {}, {} validation errors by page",
        datasetId, errors.size(), pageable.getPageNumber(), pageable.getPageSize());

    return validation;

  }


  /**
   * Process errors.
   *
   * @param idValidations the id validations
   * @param dataset the dataset
   * @return the list
   */
  private Map<Long, ErrorsValidationVO> processErrors(List<Long> idValidations,
      DatasetValue dataset) {

    Map<Long, ErrorsValidationVO> errors = new HashMap<>();

    try {
      Future<Map<Long, ErrorsValidationVO>> datasetErrors =
          validationService.getDatasetErrors(dataset.getId(), dataset, idValidations);
      Future<Map<Long, ErrorsValidationVO>> tableErrors =
          validationService.getTableErrors(dataset.getId(), idValidations);
      Future<Map<Long, ErrorsValidationVO>> recordErrors =
          validationService.getRecordErrors(dataset.getId(), idValidations);
      Future<Map<Long, ErrorsValidationVO>> fieldErrors =
          validationService.getFieldErrors(dataset.getId(), idValidations);

      errors.putAll(datasetErrors.get());
      errors.putAll(tableErrors.get());
      errors.putAll(recordErrors.get());
      errors.putAll(fieldErrors.get());
    } catch (InterruptedException | ExecutionException e) {
      LOG_ERROR.error("Error obtaining the errors ", e);
    }

    return errors;
  }
}
