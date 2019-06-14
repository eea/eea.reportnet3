package org.eea.validation.controller;

import org.eea.kafka.io.KafkaSender;
import org.eea.validation.service.ValidationService;
import org.eea.validation.util.ValidationHelper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.server.ResponseStatusException;

@RunWith(MockitoJUnitRunner.class)
public class ValidationControllerImplTest {



  @InjectMocks
  private ValidationControllerImpl validationController;

  @Mock
  ValidationHelper helper;

  @Mock
  ValidationService validationService;

  @Mock
  KafkaSender kafkaSender;

  @Before
  public void initMocks() {
    MockitoAnnotations.initMocks(this);
  }


  @Test(expected = ResponseStatusException.class)
  public void validateDataSetDataTestException() {
    validationController.validateDataSetData(null);
  }

  @Test
  public void validateDataSetDataTest() {
    validationController.validateDataSetData(1L);
  }

}
