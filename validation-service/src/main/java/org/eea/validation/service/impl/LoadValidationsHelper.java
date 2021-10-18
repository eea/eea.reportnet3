package org.eea.validation.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.ErrorsValidationVO;
import org.eea.interfaces.vo.dataset.FailedValidationsDatasetVO;
import org.eea.interfaces.vo.dataset.enums.EntityTypeEnum;
import org.eea.interfaces.vo.dataset.enums.ErrorTypeEnum;
import org.eea.multitenancy.DatasetId;
import org.eea.validation.persistence.data.domain.DatasetValue;
import org.eea.validation.persistence.data.domain.Validation;
import org.eea.validation.persistence.data.repository.ValidationRepository;
import org.eea.validation.service.ValidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;


/**
 * The Class LoadValidationsHelper.
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
   * @param levelErrorsFilter the level errors filter
   * @param typeEntitiesFilter the type entities filter
   * @param tableFilter the table filter
   * @param fieldValueFilter the field value filter
   * @return the list validations
   * @throws EEAException the EEA exception
   */
  public FailedValidationsDatasetVO getListValidations(@DatasetId Long datasetId, Pageable pageable,
      String headerField, Boolean asc, List<ErrorTypeEnum> levelErrorsFilter,
      List<EntityTypeEnum> typeEntitiesFilter, String tableFilter, String fieldValueFilter)
      throws EEAException {

    DatasetValue dataset = validationService.getDatasetValuebyId(datasetId);
    FailedValidationsDatasetVO validation = new FailedValidationsDatasetVO();
    validation.setErrors(new ArrayList<>());
    validation.setIdDatasetSchema(dataset.getIdDatasetSchema());
    validation.setIdDataset(datasetId);

    Page<Validation> validationStream =
        validationRepository.findAllRecordsByFilter(datasetId, levelErrorsFilter,
            typeEntitiesFilter, tableFilter, fieldValueFilter, pageable, headerField, asc);

    List<Validation> validations = validationStream.get().collect(Collectors.toList());
    List<Long> idValidations =
        validations.stream().map(Validation::getId).collect(Collectors.toList());

    // PROCESS LIST OF ERRORS VALIDATIONS
    Map<Long, ErrorsValidationVO> errors = new HashMap<>();
    if (!idValidations.isEmpty()) {
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
        if (e instanceof InterruptedException) {
          Thread.currentThread().interrupt();
        }
      }
    }
    if (!errors.isEmpty()) {
      validation
          .setErrors(idValidations.stream().map(id -> errors.get(id)).collect(Collectors.toList()));
    }
    validation.setTotalRecords(validationRepository.count());
    validation.setTotalFilteredRecords(validationRepository.countRecordsByFilter(datasetId,
        levelErrorsFilter, typeEntitiesFilter, tableFilter, fieldValueFilter));
    LOG.info(
        "Total validations founded in datasetId {}: {}. Now in page {}, {} validation errors by page",
        datasetId, errors.size(), pageable.getPageNumber(), pageable.getPageSize());

    return validation;

  }

  /**
   * Gets the list group validations.
   *
   * @param datasetId the dataset id
   * @param pageable the pageable
   * @param levelErrorsFilter the level errors filter
   * @param typeEntitiesFilter the type entities filter
   * @param tableFilter the table filter
   * @param fieldValueFilter the field value filter
   * @param headerField the header field
   * @param asc the asc
   * @return the list group validations
   * @throws EEAException the EEA exception
   */
  public FailedValidationsDatasetVO getListGroupValidations(@DatasetId Long datasetId,
      Pageable pageable, List<ErrorTypeEnum> levelErrorsFilter,
      List<EntityTypeEnum> typeEntitiesFilter, String tableFilter, String fieldValueFilter,
      String headerField, Boolean asc) throws EEAException {

    DatasetValue dataset = validationService.getDatasetValuebyId(datasetId);
    FailedValidationsDatasetVO validation = new FailedValidationsDatasetVO();
    validation.setErrors(new ArrayList<>());
    validation.setIdDatasetSchema(dataset.getIdDatasetSchema());
    validation.setIdDataset(datasetId);

    validation.setErrors(validationRepository.findGroupRecordsByFilter(datasetId, levelErrorsFilter,
        typeEntitiesFilter, tableFilter, fieldValueFilter, pageable, headerField, asc, true));
    validation.setTotalErrors(validationRepository.count());

    validation.setTotalRecords(Long.valueOf(validationRepository.findGroupRecordsByFilter(datasetId,
        new ArrayList<>(), new ArrayList<>(), "", "", pageable, "", asc, false).size()));

    validation.setTotalFilteredRecords(
        Long.valueOf(validationRepository.findGroupRecordsByFilter(datasetId, levelErrorsFilter,
            typeEntitiesFilter, tableFilter, fieldValueFilter, pageable, headerField, asc, false)
            .size()));
    LOG.info(
        "Total validations founded in datasetId {}: {}. Now in page {}, {} validation errors by page",
        datasetId, validation.getErrors().size(), pageable.getPageNumber(), pageable.getPageSize());


    return validation;

  }
}
