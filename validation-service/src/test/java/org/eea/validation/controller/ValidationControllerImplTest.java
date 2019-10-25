package org.eea.validation.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.FailedValidationsDatasetVO;
import org.eea.kafka.io.KafkaSender;
import org.eea.validation.service.ValidationService;
import org.eea.validation.service.impl.LoadValidationsHelper;
import org.eea.validation.util.ValidationHelper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.server.ResponseStatusException;

@RunWith(MockitoJUnitRunner.class)
public class ValidationControllerImplTest {

  @InjectMocks
  private ValidationControllerImpl validationController;

  @Mock
  private ValidationHelper validationHelper;

  @Mock
  private ValidationService validationService;

  @Mock
  private KafkaSender kafkaSender;

  @Mock
  private LoadValidationsHelper loadValidationsHelper;

  private FailedValidationsDatasetVO failedValidationsDatasetVO;

  @Before
  public void initMocks() {
    failedValidationsDatasetVO = new FailedValidationsDatasetVO();
    MockitoAnnotations.initMocks(this);
  }


  @Test(expected = ResponseStatusException.class)
  public void validateDataSetDataExceptionNullTest() throws EEAException {
    validationController.validateDataSetData(null);
  }

  @Test
  public void testValidateDataSetDataTest() throws EEAException {
    validationController.validateDataSetData(1L);
  }

  @Test
  public void getFailedValidationsByIdDatasetSuccessEmptyFieldsTest() throws EEAException {
    when(loadValidationsHelper.getListValidations(Mockito.any(), Mockito.any(), Mockito.any(),
        Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
            .thenReturn(failedValidationsDatasetVO);
    assertEquals("result not equals to expected", failedValidationsDatasetVO, validationController
        .getFailedValidationsByIdDataset(1L, 1, 10, null, null, null, null, ""));
  }

  @Test
  public void getFailedValidationsByIdDatasetSuccessFieldsFilledTest() throws EEAException {
    when(loadValidationsHelper.getListValidations(Mockito.any(), Mockito.any(), Mockito.any(),
        Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
            .thenReturn(failedValidationsDatasetVO);
    assertEquals("result not equals to expected", failedValidationsDatasetVO, validationController
        .getFailedValidationsByIdDataset(1L, 1, 10, "id", true, null, null, ""));
  }

  @Test
  public void getFailedValidationsByIdDatasetSuccessFieldsFilledDescTest() throws EEAException {
    when(loadValidationsHelper.getListValidations(Mockito.any(), Mockito.any(), Mockito.any(),
        Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
            .thenReturn(failedValidationsDatasetVO);
    assertEquals("result not equals to expected", failedValidationsDatasetVO, validationController
        .getFailedValidationsByIdDataset(1L, 1, 10, "id", false, null, null, ""));
  }

  @Test(expected = ResponseStatusException.class)
  public void getFailedValidationsByIdDatasetExceptionTest() throws EEAException {
    validationController.getFailedValidationsByIdDataset(null, 1, 10, null, null, null, null, "");
  }

  @Test
  public void getFailedValidationsByIdDatasetEEAExceptionTest() throws EEAException {
    doThrow(new EEAException()).when(loadValidationsHelper).getListValidations(Mockito.any(),
        Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
    assertNull("result is not null", validationController.getFailedValidationsByIdDataset(1L, 1, 10,
        null, null, null, null, ""));
  }

}
