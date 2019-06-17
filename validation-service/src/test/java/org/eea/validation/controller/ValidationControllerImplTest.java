package org.eea.validation.controller;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.kafka.io.KafkaSender;
import org.eea.validation.service.ValidationService;
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
  private ValidationService validationService;

  @Mock
  private KafkaSender kafkaSender;

  @Before
  public void initMocks() {
    MockitoAnnotations.initMocks(this);
  }


  @Test(expected = ResponseStatusException.class)
  public void validateDataSetDataExceptionNullTest() throws EEAException {
    validationController.validateDataSetData(null);
  }

  @Test(expected = ResponseStatusException.class)
  public void validateDataSetDataExceptionBadIdTest() throws EEAException {
    doNothing().when(validationService).deleteAllValidation(Mockito.any());
    doThrow(new EEAException(EEAErrorMessage.DATASET_INCORRECT_ID)).when(validationService)
        .validateDataSetData(Mockito.any());
    validationController.validateDataSetData(1L);
  }

  @Test(expected = ResponseStatusException.class)
  public void validateDataSetDataExceptionDatasetNotFoundTest() throws EEAException {
    doNothing().when(validationService).deleteAllValidation(Mockito.any());
    doThrow(new EEAException(EEAErrorMessage.DATASET_NOTFOUND)).when(validationService)
        .validateDataSetData(Mockito.any());
    validationController.validateDataSetData(1L);
  }

  @Test(expected = ResponseStatusException.class)
  public void validateDataSetDataExceptionInternalErrorTest() throws EEAException {
    doNothing().when(validationService).deleteAllValidation(Mockito.any());
    doThrow(new EEAException("")).when(validationService).validateDataSetData(Mockito.any());
    validationController.validateDataSetData(1L);
  }

  @Test
  public void validateDataSetDataTest() {
    validationController.validateDataSetData(1L);
    Mockito.verify(validationService, times(1)).deleteAllValidation(Mockito.any());
  }

}
