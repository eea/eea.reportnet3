package org.eea.validation.service.impl;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.List;
import org.bson.types.ObjectId;
import org.eea.interfaces.vo.dataset.enums.TypeEntityEnum;
import org.eea.interfaces.vo.dataset.enums.TypeErrorEnum;
import org.eea.validation.persistence.data.domain.DatasetValidation;
import org.eea.validation.persistence.data.domain.DatasetValue;
import org.eea.validation.persistence.data.domain.FieldValidation;
import org.eea.validation.persistence.data.domain.FieldValue;
import org.eea.validation.persistence.data.domain.RecordValidation;
import org.eea.validation.persistence.data.domain.RecordValue;
import org.eea.validation.persistence.data.domain.TableValidation;
import org.eea.validation.persistence.data.domain.TableValue;
import org.eea.validation.persistence.data.domain.Validation;
import org.eea.validation.persistence.data.repository.ValidationRepository;
import org.eea.validation.persistence.schemas.DataSetSchema;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.AsyncResult;

/**
 * The Class LoadValidationsHelperTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class LoadValidationsHelperTest {

  /** The load validations helper. */
  @InjectMocks
  private LoadValidationsHelper loadValidationsHelper;

  /** The dataset service. */
  @Mock
  private ValidationServiceImpl validationService;

  @Mock
  private ValidationRepository validationRepository;

  /** The pageable. */
  private Pageable pageable;

  /** The table value. */
  private TableValue tableValue;

  /** The validation. */
  private Validation validation;

  /** The record value. */
  private RecordValue recordValue;

  /** The dataset value. */
  private DatasetValue datasetValue;

  /** The field value. */
  private FieldValue fieldValue;


  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {
    validation = new Validation();
    fieldValue = new FieldValue();
    recordValue = new RecordValue();
    recordValue.setIdRecordSchema("");
    recordValue.setLevelError(TypeErrorEnum.ERROR);
    recordValue.setFields(new ArrayList<>());
    tableValue = new TableValue();
    tableValue.setId(1L);
    tableValue.setTableValidations(new ArrayList<>());
    recordValue.setTableValue(tableValue);
    datasetValue = new DatasetValue();
    datasetValue.setIdDatasetSchema("5cf0e9b3b793310e9ceca190");
    datasetValue.setDatasetValidations(new ArrayList<>());
    tableValue.setDatasetId(datasetValue);
    tableValue.setIdTableSchema("5cf0e9b3b793310e9ceca190");
    pageable = PageRequest.of(1, 10);
    MockitoAnnotations.initMocks(this);
  }

  /**
   * Test get list validations.
   *
   * @throws Exception the exception
   */
  @Test
  public void testGetListValidations() throws Exception {

    TableValidation tableValidation = new TableValidation();
    tableValidation.setId(1L);
    tableValidation.setTableValue(tableValue);
    validation.setId(1L);
    validation.setLevelError(TypeErrorEnum.ERROR);
    validation.setTypeEntity(TypeEntityEnum.TABLE);
    tableValidation.setValidation(validation);
    List<TableValidation> tableValidations = new ArrayList<>();
    tableValidations.add(tableValidation);
    RecordValidation recordValidation = new RecordValidation();
    recordValidation.setRecordValue(recordValue);
    recordValidation.setValidation(validation);
    List<RecordValidation> recordValidations = new ArrayList<>();
    recordValidations.add(recordValidation);
    DatasetValidation datasetValidation = new DatasetValidation();
    datasetValidation.setValidation(validation);
    datasetValidation.setDatasetValue(datasetValue);
    List<DatasetValidation> datasetValidations = new ArrayList<>();
    datasetValidations.add(datasetValidation);
    datasetValue.setDatasetValidations(datasetValidations);
    FieldValidation fieldValidation = new FieldValidation();
    recordValue.setTableValue(tableValue);
    fieldValue.setRecord(recordValue);
    fieldValidation.setFieldValue(fieldValue);
    fieldValidation.setValidation(validation);
    List<FieldValidation> fieldValidations = new ArrayList<>();
    fieldValidations.add(fieldValidation);

    DataSetSchema schema = new DataSetSchema();
    schema.setTableSchemas(new ArrayList<>());
    schema.setIdDataSetSchema(new ObjectId("5cf0e9b3b793310e9ceca190"));
    when(validationService.getDatasetValuebyId(Mockito.any())).thenReturn(datasetValue);
    when(validationService.getfindByIdDataSetSchema(Mockito.any(), Mockito.any()))
        .thenReturn(schema);
    Page<Validation> pageValidation = Page.empty(pageable);
    when(validationRepository.findAll(Mockito.any(Pageable.class))).thenReturn(pageValidation);
    when(validationRepository.count()).thenReturn(10L);
    when(validationService.getDatasetErrors(Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(new AsyncResult<>(new ArrayList<>()));
    when(validationService.getTableErrors(Mockito.any(), Mockito.any()))
        .thenReturn(new AsyncResult<>(new ArrayList<>()));
    when(validationService.getRecordErrors(Mockito.any(), Mockito.any()))
        .thenReturn(new AsyncResult<>(new ArrayList<>()));
    when(validationService.getFieldErrors(Mockito.any(), Mockito.any()))
        .thenReturn(new AsyncResult<>(new ArrayList<>()));

    loadValidationsHelper.getListValidations(0L, pageable, "typeEntity", false);
    Mockito.verify(validationService, times(1)).getDatasetValuebyId(Mockito.any());

  }
}
