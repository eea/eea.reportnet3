package org.eea.validation.controller;

import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.communication.NotificationController.NotificationControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController.DataSetMetabaseControllerZuul;
import org.eea.interfaces.controller.recordstore.ProcessController.ProcessControllerZuul;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.interfaces.vo.dataset.FailedValidationsDatasetVO;
import org.eea.interfaces.vo.recordstore.enums.ProcessStatusEnum;
import org.eea.kafka.io.KafkaSender;
import org.eea.lock.service.impl.LockServiceImpl;
import org.eea.validation.service.ValidationService;
import org.eea.validation.service.impl.LoadValidationsHelper;
import org.eea.validation.util.ValidationHelper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.*;


/**
 * The Class ValidationControllerImplTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class ValidationControllerImplTest {

  /** The validation controller. */
  @InjectMocks
  private ValidationControllerImpl validationController;

  /** The validation helper. */
  @Mock
  private ValidationHelper validationHelper;

  /** The validation service. */
  @Mock
  private ValidationService validationService;

  /** The kafka sender. */
  @Mock
  private KafkaSender kafkaSender;

  /** The lock service impl. */
  @Mock
  private LockServiceImpl lockServiceImpl;

  /** The load validations helper. */
  @Mock
  private LoadValidationsHelper loadValidationsHelper;

  /** The notification controller zuul. */
  @Mock
  private NotificationControllerZuul notificationControllerZuul;

  /** The failed validations dataset VO. */
  private FailedValidationsDatasetVO failedValidationsDatasetVO;

  /** The process controller zuul. */
  @Mock
  private ProcessControllerZuul processControllerZuul;

  @Mock
  private DataSetMetabaseControllerZuul datasetMetabaseControllerZuul;

  /** The security context. */
  SecurityContext securityContext;

  /** The authentication. */
  Authentication authentication;

  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {
    authentication = Mockito.mock(Authentication.class);
    securityContext = Mockito.mock(SecurityContext.class);
    securityContext.setAuthentication(authentication);
    SecurityContextHolder.setContext(securityContext);
    failedValidationsDatasetVO = new FailedValidationsDatasetVO();
    MockitoAnnotations.openMocks(this);
  }

  /**
   * Validate data set data test 1.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void validateDataSetDataTest1() throws EEAException {
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");
    try {
      validationController.validateDataSetData(null, false);
    } catch (ResponseStatusException e) {
      Assert.assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
      Assert.assertEquals(EEAErrorMessage.DATASET_INCORRECT_ID, e.getReason());
    }
  }

  /**
   * Validate data set data test 2.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void validateDataSetDataTest2() throws EEAException {
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");
    Mockito.when(datasetMetabaseControllerZuul.findDatasetMetabaseById(Mockito.any()))
        .thenReturn(new DataSetMetabaseVO());
    Mockito.when(validationHelper.getPriority(Mockito.any())).thenReturn(60);
    validationController.validateDataSetData(1L, false);
    Mockito.verify(validationHelper, times(1)).executeValidation(Mockito.any(), Mockito.any(),
        Mockito.anyBoolean(), Mockito.anyBoolean());
  }

  /**
   * Validate data set data test 3.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void validateDataSetDataTest3() throws EEAException {
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");
    Mockito.when(datasetMetabaseControllerZuul.findDatasetMetabaseById(Mockito.any()))
        .thenReturn(new DataSetMetabaseVO());
    Mockito.when(validationHelper.getPriority(Mockito.any())).thenReturn(60);
    try {
      validationController.validateDataSetData(1L, false);
    } catch (ResponseStatusException e) {
      Assert.assertEquals(HttpStatus.LOCKED, e.getStatus());
      Assert.assertEquals(EEAErrorMessage.METHOD_LOCKED, e.getReason());
    }
  }

  @Test
  public void validateDataSetDataTestEEAException() throws EEAException {
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");
    Mockito.when(validationHelper.getPriority(Mockito.any())).thenReturn(60);
    Mockito.when(datasetMetabaseControllerZuul.findDatasetMetabaseById(Mockito.any()))
        .thenReturn(new DataSetMetabaseVO());
    doThrow(new EEAException("e")).when(validationHelper).executeValidation(Mockito.anyLong(),
        Mockito.any(), Mockito.anyBoolean(), Mockito.anyBoolean());
    validationController.validateDataSetData(1L, false);

    Mockito.verify(validationHelper, times(1)).deleteLockToReleaseProcess(Mockito.any());
  }

  /**
   * Gets the failed validations by id dataset success empty fields test.
   *
   * @return the failed validations by id dataset success empty fields test
   * @throws EEAException the EEA exception
   */
  @Test
  public void getFailedValidationsByIdDatasetSuccessEmptyFieldsTest() throws EEAException {
    when(loadValidationsHelper.getListValidations(Mockito.any(), Mockito.any(), Mockito.any(),
        Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
            .thenReturn(failedValidationsDatasetVO);
    assertEquals("result not equals to expected", failedValidationsDatasetVO, validationController
        .getFailedValidationsByIdDataset(1L, 1, 10, null, false, null, null, "", ""));
  }

  /**
   * Gets the failed validations by id dataset success fields filled test.
   *
   * @return the failed validations by id dataset success fields filled test
   * @throws EEAException the EEA exception
   */
  @Test
  public void getFailedValidationsByIdDatasetSuccessFieldsFilledTest() throws EEAException {
    when(loadValidationsHelper.getListValidations(Mockito.any(), Mockito.any(), Mockito.any(),
        Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
            .thenReturn(failedValidationsDatasetVO);
    assertEquals("result not equals to expected", failedValidationsDatasetVO, validationController
        .getFailedValidationsByIdDataset(1L, 1, 10, "id", true, null, null, "", ""));
  }

  /**
   * Gets the failed validations by id dataset success fields filled desc test.
   *
   * @return the failed validations by id dataset success fields filled desc test
   * @throws EEAException the EEA exception
   */
  @Test
  public void getFailedValidationsByIdDatasetSuccessFieldsFilledDescTest() throws EEAException {
    when(loadValidationsHelper.getListValidations(Mockito.any(), Mockito.any(), Mockito.any(),
        Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
            .thenReturn(failedValidationsDatasetVO);
    assertEquals("result not equals to expected", failedValidationsDatasetVO, validationController
        .getFailedValidationsByIdDataset(1L, 1, 10, "id", false, null, null, "", ""));
  }

  /**
   * Gets the failed validations by id dataset exception test.
   *
   * @return the failed validations by id dataset exception test
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void getFailedValidationsByIdDatasetExceptionTest() throws EEAException {
    validationController.getFailedValidationsByIdDataset(null, 1, 10, null, false, null, null, "",
        "");
  }

  /**
   * Gets the failed validations by id dataset EEA exception test.
   *
   * @return the failed validations by id dataset EEA exception test
   * @throws EEAException the EEA exception
   */
  @Test
  public void getFailedValidationsByIdDatasetEEAExceptionTest() throws EEAException {
    doThrow(new EEAException()).when(loadValidationsHelper).getListValidations(Mockito.any(),
        Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
        Mockito.any());
    assertNull("result is not null", validationController.getFailedValidationsByIdDataset(1L, 1, 10,
        null, false, null, null, "", ""));
  }

  /**
   * Gets the group failed validations test.
   *
   * @return the group failed validations test
   * @throws EEAException the EEA exception
   */
  @Test
  public void getGroupFailedValidationsTest() throws EEAException {
    when(loadValidationsHelper.getListGroupValidations(Mockito.any(), Mockito.any(), Mockito.any(),
        Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
            .thenReturn(failedValidationsDatasetVO);
    assertEquals("result not equals to expected", failedValidationsDatasetVO, validationController
        .getGroupFailedValidationsByIdDataset(1L, 1, 10, "id", false, null, null, "", ""));
  }

  /**
   * Gets the group failed validations by id dataset exception test.
   *
   * @return the group failed validations by id dataset exception test
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void getGroupFailedValidationsByIdDatasetExceptionTest() throws EEAException {
    try {
      validationController.getGroupFailedValidationsByIdDataset(null, 1, 10, null, false, null,
          null, "", "");
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
      assertEquals(EEAErrorMessage.DATASET_INCORRECT_ID, e.getReason());
      throw e;
    }
  }

  /**
   * Gets the group failed validations by id dataset EEA exception test.
   *
   * @return the group failed validations by id dataset EEA exception test
   * @throws EEAException the EEA exception
   */
  @Test
  public void getGroupFailedValidationsByIdDatasetEEAExceptionTest() throws EEAException {
    doThrow(new EEAException()).when(loadValidationsHelper).getListGroupValidations(Mockito.any(),
        Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
        Mockito.any());
    assertNull("result is not null", validationController.getGroupFailedValidationsByIdDataset(1L,
        1, 10, null, false, null, null, "", ""));
  }

  @Test
  public void restartTaskTest() {
    doNothing().when(validationHelper).updateTaskStatus(anyLong(), any(ProcessStatusEnum.class));
    validationController.restartTask(Long.valueOf(1));
    verify(validationHelper).updateTaskStatus(anyLong(), any(ProcessStatusEnum.class));
  }

}
