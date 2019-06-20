package org.eea.validation.controller;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.kafka.io.KafkaSender;
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
    doThrow(new EEAException(EEAErrorMessage.DATASET_INCORRECT_ID)).when(validationHelper)
        .executeValidation(Mockito.any());
    validationController.validateDataSetData(1L);
  }

  @Test(expected = ResponseStatusException.class)
  public void validateDataSetDataExceptionDatasetNotFoundTest() throws EEAException {
    doThrow(new EEAException(EEAErrorMessage.DATASET_NOTFOUND)).when(validationHelper)
        .executeValidation(Mockito.any());
    validationController.validateDataSetData(1L);
  }

  @Test(expected = ResponseStatusException.class)
  public void validateDataSetDataExceptionInternalErrorTest() throws EEAException {
    doThrow(new EEAException("")).when(validationHelper).executeValidation(Mockito.any());
    validationController.validateDataSetData(1L);
  }

  @Test
  public void validateDataSetDataTest() throws EEAException {
    doNothing().when(validationHelper).executeValidation(Mockito.any());
    validationController.validateDataSetData(1L);
    Mockito.verify(validationHelper, times(1)).executeValidation(Mockito.any());
  }

}
