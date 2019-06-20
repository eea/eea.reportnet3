package org.eea.dataset.service.validation;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.eea.dataset.persistence.data.domain.DatasetValue;
import org.eea.dataset.persistence.schemas.domain.DataSetSchema;
import org.eea.dataset.service.DatasetService;
import org.eea.dataset.service.callable.LoadErrorsCallable;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.ErrorsValidationVO;
import org.eea.interfaces.vo.dataset.FailedValidationsDatasetVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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

  /** The dataset service. */
  @Autowired
  @Qualifier("proxyDatasetService")
  private DatasetService datasetService;

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

    DatasetValue dataset = datasetService.getDatasetValuebyId(datasetId);
    FailedValidationsDatasetVO validation = new FailedValidationsDatasetVO();
    validation.setErrors(new ArrayList<>());
    validation.setIdDatasetSchema(dataset.getIdDatasetSchema());
    validation.setIdDataset(datasetId);
    DataSetSchema schema = datasetService.getfindByIdDataSetSchema(datasetId,
        new ObjectId(dataset.getIdDatasetSchema()));
    validation.setNameDataSetSchema(schema.getNameDataSetSchema());
    Map<String, String> mapNameTableSchema = new HashMap<>();
    for (int i = 0; i < schema.getTableSchemas().size(); i++) {
      mapNameTableSchema.put(schema.getTableSchemas().get(i).getIdTableSchema().toString(),
          schema.getTableSchemas().get(i).getNameTableSchema());
    }
    mapNameTableSchema.put(schema.getIdDataSetSchema().toString(), schema.getNameDataSetSchema());

    // PROCESS LIST OF ERRORS VALIDATIONS
    List<ErrorsValidationVO> errors = processErrors(dataset, mapNameTableSchema);

    // SORTING
    if (StringUtils.isNotBlank(headerField)) {
      sortingValidationErrors(errors, headerField, asc);
    }

    // PAGINATION
    int tamPage = 20;
    if (pageable.getPageSize() != 0) {
      tamPage = pageable.getPageSize();
    }
    int initIndex = pageable.getPageNumber() * tamPage;
    int endIndex =
        (pageable.getPageNumber() + 1) * pageable.getPageSize() > errors.size() ? errors.size()
            : ((pageable.getPageNumber() + 1) * tamPage);

    if (!errors.isEmpty()) {
      if (endIndex > errors.size()) {
        endIndex = errors.size();
      }
      validation.setErrors(errors.subList(initIndex, endIndex));
    }
    validation.setTotalErrors(Long.valueOf(errors.size()));
    LOG.info(
        "Total validations founded in datasetId {}: {}. Now in page {}, {} validation errors by page",
        datasetId, errors.size(), pageable.getPageNumber(), pageable.getPageSize());

    return validation;

  }


  /**
   * Process errors.
   *
   * @param dataset the dataset
   * @param mapNameTableSchema the map name table schema
   * @return the list
   */
  private List<ErrorsValidationVO> processErrors(DatasetValue dataset,
      Map<String, String> mapNameTableSchema) {

    List<ErrorsValidationVO> errors = new ArrayList<>();

    ExecutorService es = Executors.newCachedThreadPool();
    List<Future<List<ErrorsValidationVO>>> futures = new ArrayList<>();
    try {
      for (int i = 0; i < 4; i++) {
        LoadErrorsCallable task =
            new LoadErrorsCallable(datasetService, dataset, mapNameTableSchema, i);
        Future<List<ErrorsValidationVO>> result = es.submit(task);
        futures.add(result);
      }
      es.shutdown();
      es.awaitTermination(1, TimeUnit.MINUTES);
      for (Future<List<ErrorsValidationVO>> future : futures) {
        errors.addAll(future.get());
      }
    } catch (InterruptedException | ExecutionException e) {
      LOG_ERROR.error("Error obtaining the errors ", e);
    }
    return errors;
  }

  /**
   * Sorting validation errors.
   *
   * @param errors the errors
   * @param headerField the header field
   * @param asc the asc
   *
   * @return the list
   */
  private List<ErrorsValidationVO> sortingValidationErrors(List<ErrorsValidationVO> errors,
      String headerField, Boolean asc) {

    Method valueGetter = retrieveGetMethod(headerField);
    errors.sort((ErrorsValidationVO v1, ErrorsValidationVO v2) -> {

      String sortCriteria1 = "";
      String sortCriteria2 = "";
      try {
        sortCriteria1 = (String) valueGetter.invoke(v1);
        sortCriteria2 = (String) valueGetter.invoke(v2);
      } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
        LOG_ERROR.error(e.getMessage());
      }
      // process the sort criteria
      // it could happen that some values has no sortCriteria due to a matching error
      // during the load process. If this is the case we need to ensure that sort logic does not
      // fail
      int sort = 0;
      if (null == sortCriteria1) {
        if (null != sortCriteria2) {
          sort = -1;
        }
      } else {
        if (null != sortCriteria2) {
          sort = asc ? sortCriteria1.compareTo(sortCriteria2)
              : (sortCriteria1.compareTo(sortCriteria2) * -1);
        } else {
          sort = 1;
        }
      }
      return sort;
    });

    return errors;
  }

  /**
   * Retrieve get method.
   *
   * @param fieldName the field name
   *
   * @return the method
   */
  private Method retrieveGetMethod(String fieldName) {
    Method valueGetter = null;
    try {
      for (PropertyDescriptor pd : Introspector.getBeanInfo(ErrorsValidationVO.class)
          .getPropertyDescriptors()) {
        if (pd.getName().equals(fieldName)) {
          valueGetter = pd.getReadMethod();
          break;
        }
      }
    } catch (IntrospectionException e) {
      LOG_ERROR.error(e.getMessage());
    }
    return valueGetter;

  }
}
